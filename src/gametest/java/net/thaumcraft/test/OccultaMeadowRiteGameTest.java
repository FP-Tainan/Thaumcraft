package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PoppetItem;
import net.thaumcraft.occulta.Poppets;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;

/**
 * O prado que o Poder da Natureza planta, e as bonecas que o outro rito quebra.
 *
 * <p>O segundo é o único rito deste porte que <b>exige um familiar para correr</b>, e é a prova
 * {@code withoutTheCatItRefuses} que guarda essa decisão.
 */
public class OccultaMeadowRiteGameTest {
    private static void piso(GameTestHelper helper, net.minecraft.world.level.block.Block qual) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)), qual.defaultBlockState());
            }
        }
    }

    /** Os dois estão na lista. */
    @GameTest(maxTicks = 20)
    public void bothAreRegistered(GameTestHelper helper) {
        for (String chave : List.of("tc.rite.naturespower", "tc.rite.corruptvoodooprotection")) {
            if (RiteRegistry.all().stream().noneMatch(r -> r.key().equals(chave))) {
                helper.fail("falta o rito " + chave);
                return;
            }
        }
        helper.succeed();
    }

    /** O Poder da Natureza vira pedra em grama, e planta coisa em cima. */
    @GameTest(maxTicks = 80)
    public void naturesPowerTurnsStoneIntoAMeadow(GameTestHelper helper) {
        piso(helper, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        var rito = new Rites.NaturesPower(3, 4, 150, 2);
        var corrido = new ActiveRite("tc.rite.naturespower", rito, List.of(), null, 0);
        var passo = rito.steps(0).getFirst();
        for (int volta = 0; volta < 40; volta++) passo.run(level, meio, 20L, corrido);

        int grama = 0;
        int plantado = 0;
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                BlockPos chão = helper.absolutePos(new BlockPos(x, 1, z));
                if (level.getBlockState(chão).is(Blocks.GRASS_BLOCK)) grama++;
                if (!level.getBlockState(chão.above()).isAir()) plantado++;
            }
        }
        if (grama == 0) helper.fail("em quarenta voltas alguma pedra devia ter virado grama");
        if (plantado == 0) helper.fail("e alguma coisa devia ter nascido em cima");
        helper.succeed();
    }

    /** E ele não planta debaixo de folha: onde há copa, só o chão muda. */
    @GameTest(maxTicks = 40)
    public void itDoesNotPlantUnderLeaves(GameTestHelper helper) {
        piso(helper, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        BlockPos chão = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlockAndUpdate(chão.above(), Blocks.OAK_LEAVES.defaultBlockState());

        var rito = new Rites.NaturesPower(1, 4, 150, 0);
        var corrido = new ActiveRite("tc.rite.naturespower", rito, List.of(), null, 0);
        var passo = rito.steps(0).getFirst();
        for (int volta = 0; volta < 40; volta++) {
            passo.run(level, helper.absolutePos(new BlockPos(4, 2, 4)), 20L, corrido);
        }

        if (!level.getBlockState(chão.above()).is(Blocks.OAK_LEAVES)) {
            helper.fail("a folha fica onde estava: ele não planta por baixo de copa");
        }
        helper.succeed();
    }

    /**
     * <b>Sem o gato, o rito das bonecas recusa.</b>
     *
     * <p>É o único deste porte que pede um familiar para correr, e por isso esta prova existe.
     */
    @GameTest(maxTicks = 40)
    public void withoutTheCatItRefuses(GameTestHelper helper) {
        piso(helper, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(4.5, 2, 4.5)));

        var rito = new Rites.CursePoppets(1);
        var corrido = new ActiveRite("tc.rite.corruptvoodooprotection", rito, List.of(),
                quem.getUUID(), 0);
        RiteStep.Result saiu = rito.steps(0).getFirst().run(level, meio, 20L, corrido);
        if (saiu != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem a maestria da maldição ele desiste e devolve, e deu " + saiu);
        }
        helper.succeed();
    }

    /** Com o gato e com o vínculo, ele quebra as bonecas de proteção de quem o vínculo prende. */
    @GameTest(maxTicks = 60)
    public void withTheCatItBreaksTheProtectionPoppets(GameTestHelper helper) {
        piso(helper, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        var bruxa = helper.makeMockServerPlayerInLevel();
        bruxa.setGameMode(GameType.SURVIVAL);
        bruxa.snapTo(helper.absoluteVec(new Vec3(4.5, 2, 4.5)));

        // o gato que dá a maestria da maldição
        var gato = helper.spawn(net.minecraft.world.entity.EntityTypes.CAT, new BlockPos(5, 2, 5));
        gato.tame(bruxa);
        if (!net.thaumcraft.occulta.familiar.Familiars.vincula(bruxa, gato)) {
            helper.fail("o gato devia vincular-se");
            return;
        }

        // e a vítima, com duas bonecas de proteção presas a ela
        var vítima = helper.makeMockServerPlayerInLevel();
        vítima.setGameMode(GameType.SURVIVAL);
        vítima.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));
        for (int i = 0; i < 2; i++) {
            var boneca = new ItemStack(OccultaItems.VOODOO_PROTECTION_POPPET);
            TaglockItem.bind(boneca, vítima);
            vítima.getInventory().add(boneca);
        }

        var vínculo = new ItemStack(OccultaItems.TAGLOCK);
        TaglockItem.bind(vínculo, vítima);
        var corrido = new ActiveRite("tc.rite.corruptvoodooprotection", new Rites.CursePoppets(1),
                List.of(), bruxa.getUUID(), 0);
        corrido.offer(vínculo, meio);

        RiteStep.Result saiu = new Rites.CursePoppets(1).steps(0).getFirst()
                .run(level, meio, 20L, corrido);
        if (saiu != RiteStep.Result.COMPLETED) {
            helper.fail("com gato e vínculo ele corre, e deu " + saiu);
        }

        int sobraram = 0;
        for (int i = 0; i < vítima.getInventory().getContainerSize(); i++) {
            var stack = vítima.getInventory().getItem(i);
            if (stack.getItem() instanceof PoppetItem boneca
                    && boneca.kind() == PoppetItem.Kind.VOODOO_PROTECTION) {
                sobraram += stack.getCount();
            }
        }
        if (sobraram >= 2) helper.fail("e alguma boneca tem de se ter ido, sobraram " + sobraram);

        gato.discard();
        helper.succeed();
    }
}
