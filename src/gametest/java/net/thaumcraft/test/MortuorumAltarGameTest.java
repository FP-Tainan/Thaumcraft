package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.mortuorum.MinionAttributes;
import net.thaumcraft.mortuorum.MinionEntity;
import net.thaumcraft.mortuorum.MinionParts;
import net.thaumcraft.mortuorum.MortuorumBlocks;
import net.thaumcraft.mortuorum.MortuorumEntities;
import net.thaumcraft.mortuorum.MortuorumItems;
import net.thaumcraft.mortuorum.SummoningAltarBlockEntity;

/**
 * O Altar de Invocação e o Lacaio: o que o altar pede para acordar um, e o que as peças lhe dão.
 */
public class MortuorumAltarGameTest {
    /** Sem o sangue e sem a alma, o altar não acorda ninguém. */
    @GameTest
    public void theAltarNeedsBloodAndSoul(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, MortuorumBlocks.SUMMONING_ALTAR);
        var altar = helper.getBlockEntity(onde, SummoningAltarBlockEntity.class);
        if (altar == null) helper.fail("o altar devia ter entidade de bloco");
        if (altar.canSpawn()) helper.fail("vazio, o altar não acorda nada");

        altar.setItem(SummoningAltarBlockEntity.BLOOD, new ItemStack(MortuorumItems.JAR_OF_BLOOD));
        if (altar.canSpawn()) helper.fail("só com o sangue, ainda não");
        altar.setItem(SummoningAltarBlockEntity.SOUL, new ItemStack(MortuorumItems.SOUL_IN_A_JAR));
        if (!altar.canSpawn()) helper.fail("com o sangue e a alma, sim");
        helper.succeed();
    }

    /** As peças postas nas casas são as peças com que o lacaio acorda. */
    @GameTest
    public void theAltarReadsItsParts(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, MortuorumBlocks.SUMMONING_ALTAR);
        var altar = helper.getBlockEntity(onde, SummoningAltarBlockEntity.class);
        if (altar == null) helper.fail("o altar devia ter entidade de bloco");
        if (!altar.parts().isEmpty()) helper.fail("vazio, ele não tem peça nenhuma");

        altar.setItem(SummoningAltarBlockEntity.TORSO, new ItemStack(MortuorumItems.PART_ITEMS.get("skeleton_torso")));
        altar.setItem(SummoningAltarBlockEntity.LEGS, new ItemStack(MortuorumItems.PART_ITEMS.get("enderman_legs")));
        MinionParts pecas = altar.parts();
        if (!pecas.torso().equals("skeleton_torso")) helper.fail("o tronco devia ser o de esqueleto; veio " + pecas.torso());
        if (!pecas.legs().equals("enderman_legs")) helper.fail("as pernas deviam ser as de enderman; vieram " + pecas.legs());
        if (!pecas.head().isEmpty()) helper.fail("a cabeça ficou vazia");
        helper.succeed();
    }

    /** O que cada peça soma é o que o bicho dela somava naquele lugar, no original. */
    @GameTest
    public void thePartsAddWhatTheirMobGave(GameTestHelper helper) {
        var doEsqueleto = MinionAttributes.of("Skeleton", "Torso");
        var deNinguem = MinionAttributes.of("NaoExiste", "Torso");
        if (deNinguem != MinionAttributes.Bonus.NONE) helper.fail("bicho que não existe não soma nada");
        if (doEsqueleto == MinionAttributes.Bonus.NONE) helper.fail("o tronco de esqueleto soma alguma coisa");

        MinionEntity lacaio = MortuorumEntities.MINION.create(helper.getLevel(),
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (lacaio == null) helper.fail("o lacaio devia nascer");
        double vidaNua = lacaio.getAttributeValue(Attributes.MAX_HEALTH);
        lacaio.setParts(new MinionParts("", "skeleton_torso", "", "", ""));
        double comTronco = lacaio.getAttributeValue(Attributes.MAX_HEALTH);
        if (Math.abs(comTronco - (vidaNua + doEsqueleto.health())) > 0.001) {
            helper.fail("o tronco devia somar " + doEsqueleto.health() + " de vida; foi de " + vidaNua + " para " + comTronco);
        }

        // e tirada a peça, o que ela somava sai junto
        lacaio.setParts(MinionParts.EMPTY);
        if (Math.abs(lacaio.getAttributeValue(Attributes.MAX_HEALTH) - vidaNua) > 0.001) {
            helper.fail("sem peça nenhuma, ele volta ao que era");
        }
        lacaio.discard();
        helper.succeed();
    }

    /** O altar acorda o lacaio com as peças postas, e gasta uma de cada casa. */
    @GameTest
    public void theAltarWakesTheMinion(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, MortuorumBlocks.SUMMONING_ALTAR);
        var altar = helper.getBlockEntity(onde, SummoningAltarBlockEntity.class);
        if (altar == null) helper.fail("o altar devia ter entidade de bloco");
        altar.setItem(SummoningAltarBlockEntity.BLOOD, new ItemStack(MortuorumItems.JAR_OF_BLOOD, 2));
        altar.setItem(SummoningAltarBlockEntity.SOUL, new ItemStack(MortuorumItems.SOUL_IN_A_JAR, 2));
        altar.setItem(SummoningAltarBlockEntity.TORSO,
                new ItemStack(MortuorumItems.PART_ITEMS.get("zombie_torso"), 2));

        var dono = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        if (!altar.spawn(helper.getLevel(), dono)) helper.fail("com sangue, alma e uma peça, ele devia acordar");

        var lacaios = helper.getLevel().getEntitiesOfClass(MinionEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(onde)).inflate(4.0));
        if (lacaios.size() != 1) helper.fail("devia ter acordado um lacaio; achei " + lacaios.size());
        else if (!lacaios.get(0).parts().torso().equals("zombie_torso")) {
            helper.fail("com o tronco de zumbi que estava na casa");
        }
        if (altar.getItem(SummoningAltarBlockEntity.BLOOD).getCount() != 1) helper.fail("gastou um pote de sangue");
        if (altar.getItem(SummoningAltarBlockEntity.TORSO).getCount() != 1) helper.fail("e um tronco");

        for (var lacaio : lacaios) lacaio.discard();
        helper.succeed();
    }

    /** A mesa do altar ocupa mais dois blocos para o lado para onde ele olha. */
    @GameTest
    public void theAltarTakesThreeBlocks(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 2);
        var estado = MortuorumBlocks.SUMMONING_ALTAR.defaultBlockState()
                .setValue(net.thaumcraft.mortuorum.SummoningAltarBlock.FACING, net.minecraft.core.Direction.EAST);
        helper.setBlock(onde, estado);
        MortuorumBlocks.SUMMONING_ALTAR.setPlacedBy(helper.getLevel(), helper.absolutePos(onde), estado, null,
                ItemStack.EMPTY);
        for (int passo = 1; passo <= 2; passo++) {
            helper.assertBlockPresent(MortuorumBlocks.SUMMONING_ALTAR_PART, onde.east(passo));
        }
        helper.succeed();
    }
}
