package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.crimson.AncientAltarBlockEntity;
import net.thaumcraft.crimson.AncientAltarFloor;
import net.thaumcraft.crimson.CrimsonBlocks;
import net.thaumcraft.registry.TCResources;

/** O Crimson Warfare: o altar antigo, o rito e os três chamados. */
public class CrimsonGameTest {
    /** O desenho do chão é o disco de treze por treze com a orla de tijolo. */
    @GameTest
    public void theFloorIsThirteenAcross(GameTestHelper helper) {
        if (AncientAltarFloor.PLAN.length != 13) helper.fail("o disco tem treze filas");
        for (String fila : AncientAltarFloor.PLAN) {
            if (fila.length() != 13) helper.fail("e treze casas em cada uma: " + fila);
        }
        if (AncientAltarFloor.size() != 121) {
            helper.fail("são cento e vinte e um blocos; achei " + AncientAltarFloor.size());
        }
        if (AncientAltarFloor.MIN_X != -6 || AncientAltarFloor.MIN_Z != -6) {
            helper.fail("o altar fica no meio do disco");
        }
        // o meio é o tijolo debaixo do altar
        if (AncientAltarFloor.PLAN[6].charAt(6) != '#') helper.fail("debaixo do altar é tijolo");
        helper.succeed();
    }

    /** Sem o rito aprendido, o altar não responde a ninguém. */
    @GameTest
    public void theAltarIgnoresTheUnlearned(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, CrimsonBlocks.ANCIENT_ALTAR);
        var altar = helper.getBlockEntity(onde, AncientAltarBlockEntity.class);
        if (altar == null) helper.fail("o altar devia ter entidade de bloco");
        if (altar.hasSeed()) helper.fail("ele nasce vazio");

        var quemNãoSabe = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack semente = new ItemStack(TCResources.get("void_seed"));
        CrimsonBlocks.ANCIENT_ALTAR.defaultBlockState().useItemOn(semente, helper.getLevel(), quemNãoSabe,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(onde)),
                        net.minecraft.core.Direction.UP, helper.absolutePos(onde), false));
        if (altar.hasSeed()) helper.fail("sem saber o rito, a semente não entra");
        if (semente.isEmpty()) helper.fail("e nem se gasta");
        helper.succeed();
    }

    /** Com a semente posta, a conta corre e no fim vem um dos três. */
    @GameTest
    public void theSeedCallsSomethingUp(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, CrimsonBlocks.ANCIENT_ALTAR);
        var altar = helper.getBlockEntity(onde, AncientAltarBlockEntity.class);
        if (altar == null) {
            helper.fail("o altar devia ter entidade de bloco");
            return;
        }
        altar.put(new ItemStack(TCResources.get("void_seed")));
        if (!altar.hasSeed()) helper.fail("a semente devia ficar no altar");

        // o original espera trezentos tiques; aqui contam-se de uma vez
        for (int passo = 0; passo <= AncientAltarBlockEntity.DELAY; passo++) {
            AncientAltarBlockEntity.tick(helper.getLevel(), helper.absolutePos(onde),
                    CrimsonBlocks.ANCIENT_ALTAR.defaultBlockState(), altar);
        }
        if (altar.hasSeed()) helper.fail("no fim da conta a semente some");
        helper.assertBlockNotPresent(CrimsonBlocks.ANCIENT_ALTAR, onde);

        var chamados = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(onde)).inflate(4.0),
                bicho -> bicho.getType() == net.thaumcraft.registry.TCEntities.CULTIST_PORTAL
                        || bicho.getType() == net.thaumcraft.registry.TCEntities.ELDRITCH_GOLEM
                        || bicho.getType() == net.thaumcraft.registry.TCEntities.ELDRITCH_WARDEN);
        if (chamados.size() != 1) helper.fail("devia subir um dos três; achei " + chamados.size());
        for (var bicho : chamados) bicho.discard();
        helper.succeed();
    }
}
