package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.occulta.FumeFunnelBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaFumes;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.WitchesOvenBlock;
import net.thaumcraft.occulta.WitchesOvenBlockEntity;

/**
 * O Forno das Bruxas: o que ele aceita cozinhar, o cheiro que guarda nos potes e o que os funis lhe fazem.
 */
public class OccultaOvenGameTest {
    /** O forno cozinha o que vira carvão, comida ou cinza — e mais nada. */
    @GameTest
    public void theOvenOnlyCooksCoalFoodAndAsh(GameTestHelper helper) {
        WitchesOvenBlockEntity forno = place(helper, new BlockPos(1, 2, 1), Direction.NORTH);

        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.OAK_LOG));
        if (!forno.canSmelt()) helper.fail("tora vira carvão: o forno aceita");

        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.BEEF));
        if (!forno.canSmelt()) helper.fail("carne é comida: o forno aceita");

        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.OAK_SAPLING));
        if (!forno.canSmelt()) helper.fail("muda vira cinza de madeira: o forno aceita");
        if (!forno.result(helper.getLevel()).is(OccultaItems.WOOD_ASH)) {
            helper.fail("a muda devia dar cinza de madeira; deu " + forno.result(helper.getLevel()));
        }

        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.IRON_ORE));
        if (forno.canSmelt()) helper.fail("o forno das bruxas não é fundição: minério não entra");

        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.STONE));
        if (forno.canSmelt()) helper.fail("pedra vira pedra lisa, que não é carvão nem comida nem cinza");
        helper.succeed();
    }

    /** Cozinhando com pote dentro, o cheiro do que queimou fica guardado. */
    @GameTest(maxTicks = 1200)
    public void theOvenCatchesTheSmellInAJar(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        WitchesOvenBlockEntity forno = place(helper, onde, Direction.NORTH);
        // dois funis com filtro, um de cada lado: a sorte do cheiro sobe para nove décimos e a cozedura apressa
        for (BlockPos lado : new BlockPos[]{onde.east(), onde.west()}) {
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(lado),
                    OccultaBlocks.FILTERED_FUME_FUNNEL.defaultBlockState()
                            .setValue(FumeFunnelBlock.FACING, Direction.NORTH));
        }
        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.BIRCH_SAPLING, 64));
        forno.setItem(WitchesOvenBlockEntity.FUEL, new ItemStack(Items.COAL, 64));
        forno.setItem(WitchesOvenBlockEntity.JARS, new ItemStack(OccultaItems.CLAY_JAR, 64));

        helper.succeedWhen(() -> {
            ItemStack fumo = forno.getItem(WitchesOvenBlockEntity.BYPRODUCT);
            if (fumo.isEmpty()) throw helper.assertionException("o forno ainda não guardou cheiro nenhum");
            if (!fumo.is(OccultaItems.BREATH_OF_THE_GODDESS)) {
                helper.fail("a muda de bétula dá o Sopro da Deusa; deu " + fumo);
            }
            if (forno.getItem(WitchesOvenBlockEntity.OUTPUT).isEmpty()) {
                helper.fail("e a cinza de madeira devia estar na casa dela");
            }
            if (forno.getItem(WitchesOvenBlockEntity.JARS).getCount() == 64) {
                helper.fail("o pote gasta-se ao guardar o cheiro");
            }
        });
    }

    /** Sem pote, o forno cozinha na mesma — mas o cheiro se perde. */
    @GameTest(maxTicks = 400)
    public void withoutAJarTheSmellIsLost(GameTestHelper helper) {
        WitchesOvenBlockEntity forno = place(helper, new BlockPos(1, 2, 1), Direction.NORTH);
        forno.setItem(WitchesOvenBlockEntity.INPUT, new ItemStack(Items.BIRCH_SAPLING, 64));
        forno.setItem(WitchesOvenBlockEntity.FUEL, new ItemStack(Items.COAL, 64));

        helper.succeedWhen(() -> {
            if (forno.getItem(WitchesOvenBlockEntity.OUTPUT).isEmpty()) {
                throw helper.assertionException("o forno ainda não cozinhou");
            }
            if (!forno.getItem(WitchesOvenBlockEntity.BYPRODUCT).isEmpty()) {
                helper.fail("sem pote não há onde guardar o cheiro");
            }
        });
    }

    /** Cada muda deixa o seu cheiro; o resto deixa Fumo Fétido, e a muda de selva não deixa nada. */
    @GameTest
    public void eachSaplingHasItsOwnSmell(GameTestHelper helper) {
        confere(helper, Items.OAK_SAPLING, OccultaItems.EXHALE_OF_THE_HORNED_ONE);
        confere(helper, Items.SPRUCE_SAPLING, OccultaItems.HINT_OF_REBIRTH);
        confere(helper, Items.BIRCH_SAPLING, OccultaItems.BREATH_OF_THE_GODDESS);
        confere(helper, Items.BEEF, OccultaItems.FOUL_FUME);
        if (!OccultaFumes.of(new ItemStack(Items.JUNGLE_SAPLING)).isEmpty()) {
            helper.fail("a muda de selva não deixa cheiro nenhum, como no original");
        }
        helper.succeed();
    }

    /** Os funis apressam o forno: vinte tiques a menos por funil, dos cento e oitenta. */
    @GameTest
    public void funnelsHurryTheOven(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(1, 2, 1);
        WitchesOvenBlockEntity forno = place(helper, onde, Direction.NORTH);
        BlockPos absoluto = helper.absolutePos(onde);
        var estado = level.getBlockState(absoluto);

        if (forno.cookTime(level, absoluto, estado) != WitchesOvenBlockEntity.COOK_TIME) {
            helper.fail("sem funil, a cozedura leva o tempo inteiro");
        }

        // um funil em cima, virado para onde o forno olha
        level.setBlockAndUpdate(absoluto.above(), OccultaBlocks.FUME_FUNNEL.defaultBlockState()
                .setValue(FumeFunnelBlock.FACING, Direction.NORTH));
        int comUm = forno.cookTime(level, absoluto, estado);
        if (comUm != WitchesOvenBlockEntity.COOK_TIME - WitchesOvenBlockEntity.FUNNEL_HASTE) {
            helper.fail("um funil tira vinte tiques; ficou em " + comUm);
        }
        // e o de cima não melhora a sorte do cheiro: só os dos lados
        if (forno.funnelChance(level, absoluto, estado) != 0.0) {
            helper.fail("o funil de cima apressa, mas não melhora a sorte");
        }

        // um funil de cada lado
        level.setBlockAndUpdate(absoluto.east(), OccultaBlocks.FUME_FUNNEL.defaultBlockState()
                .setValue(FumeFunnelBlock.FACING, Direction.NORTH));
        level.setBlockAndUpdate(absoluto.west(), OccultaBlocks.FILTERED_FUME_FUNNEL.defaultBlockState()
                .setValue(FumeFunnelBlock.FACING, Direction.NORTH));
        int comTrês = forno.cookTime(level, absoluto, estado);
        if (comTrês != WitchesOvenBlockEntity.COOK_TIME - 3 * WitchesOvenBlockEntity.FUNNEL_HASTE) {
            helper.fail("três funis tiram sessenta tiques; ficou em " + comTrês);
        }
        double sorte = forno.funnelChance(level, absoluto, estado);
        double esperada = WitchesOvenBlockEntity.FUNNEL_CHANCE + WitchesOvenBlockEntity.FILTERED_FUNNEL_CHANCE;
        if (Math.abs(sorte - esperada) > 0.0001) {
            helper.fail("o funil comum e o com filtro somam " + esperada + "; somaram " + sorte);
        }

        // e um funil virado para outro lado não conta para nada
        level.setBlockAndUpdate(absoluto.east(), OccultaBlocks.FUME_FUNNEL.defaultBlockState()
                .setValue(FumeFunnelBlock.FACING, Direction.SOUTH));
        if (forno.funnels(level, absoluto, estado) != 2) {
            helper.fail("funil virado para outro lado não serve ao forno");
        }
        helper.succeed();
    }

    /** E o clique no funil abre o forno a que ele serve. */
    @GameTest
    public void theFunnelOpensTheOvenItServes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = new BlockPos(1, 2, 1);
        place(helper, onde, Direction.NORTH);
        BlockPos absoluto = helper.absolutePos(onde);
        BlockPos funil = absoluto.above();
        var estado = OccultaBlocks.FUME_FUNNEL.defaultBlockState().setValue(FumeFunnelBlock.FACING, Direction.NORTH);
        level.setBlockAndUpdate(funil, estado);
        if (!absoluto.equals(FumeFunnelBlock.oven(level, funil, estado))) {
            helper.fail("o funil de cima serve o forno de baixo");
        }
        // e um funil sozinho não serve forno nenhum
        BlockPos sozinho = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(sozinho, estado);
        if (FumeFunnelBlock.oven(level, sozinho, estado) != null) {
            helper.fail("funil sem forno não serve nada");
        }
        helper.succeed();
    }

    /** E o thaumômetro lê o forno e os fumos com os números do próprio Witchery. */
    @GameTest
    public void theOvenAndTheFumesHaveTheirAspects(GameTestHelper helper) {
        var forno = ObjectAspects.of(OccultaItems.WITCHES_OVEN);
        if (forno.getAmount(Aspects.METAL) != 14 || forno.getAmount(Aspects.MECHANISM) != 3) {
            helper.fail("o forno é metallum 14 e machina 3; veio " + forno);
        }
        var sopro = ObjectAspects.of(OccultaItems.BREATH_OF_THE_GODDESS);
        if (sopro.getAmount(Aspects.AIR) != 3 || sopro.getAmount(Aspects.SOUL) != 1) {
            helper.fail("o Sopro da Deusa é aer 3 e spiritus 1; veio " + sopro);
        }
        var pote = ObjectAspects.of(OccultaItems.CLAY_JAR);
        if (pote.getAmount(Aspects.VOID) != 1) helper.fail("o pote de barro é vacuos 1; veio " + pote);
        helper.succeed();
    }

    /** O cheiro que aquilo deixa. */
    private static void confere(GameTestHelper helper, net.minecraft.world.item.Item queimado,
                                net.minecraft.world.item.Item cheiro) {
        ItemStack saiu = OccultaFumes.of(new ItemStack(queimado));
        if (!saiu.is(cheiro)) helper.fail(queimado + " devia deixar " + cheiro + "; deixou " + saiu);
    }

    /** Põe um forno virado para um lado e devolve o miolo dele. */
    private static WitchesOvenBlockEntity place(GameTestHelper helper, BlockPos onde, Direction para) {
        BlockPos absoluto = helper.absolutePos(onde);
        helper.getLevel().setBlockAndUpdate(absoluto, OccultaBlocks.WITCHES_OVEN.defaultBlockState()
                .setValue(WitchesOvenBlock.FACING, para));
        if (helper.getLevel().getBlockEntity(absoluto) instanceof WitchesOvenBlockEntity forno) return forno;
        helper.fail("o forno devia ter miolo");
        throw new IllegalStateException();
    }
}
