package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.PlantMineBlock;
import net.thaumcraft.occulta.brew.WitchProjectile;

/**
 * A <b>Mina de Planta</b> e os <b>quatro efeitos</b> do projétil de bruxa.
 *
 * <p>A prova que carrega a fatia é a de que a <b>cara não diz o efeito</b>. Uma papoula de teias e uma
 * papoula de espinhos são a mesma papoula, e é disso que a mina vive: quem a planta sabe o que ela é, quem
 * passa por cima não tem como saber.
 *
 * <p>As outras quatro são os efeitos, um por prova, porque cada um deixa uma coisa diferente no chão.
 */
public class OccultaPlantMineGameTest {
    /**
     * O chão da arena, com <b>duas camadas</b>.
     *
     * <p>A de baixo é de pedra e existe por uma razão: a mina de espinhos <b>troca o chão por areia</b> antes
     * de plantar o cato, e areia sem nada por baixo <b>cai</b>. Com uma camada só, a prova falharia dizendo
     * que o chão não virou areia quando ele virou e foi-se embora.
     */
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 0, z)),
                        Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (PlantMineBlock.Look.values().length != 3) helper.fail("são três caras");
        if (PlantMineBlock.Effect.values().length != 4) helper.fail("e quatro efeitos, que dão doze");
        if (WitchProjectile.CATO != 4) helper.fail("o cato dela cresce quatro");
        if (WitchProjectile.CEGO != 400) helper.fail("e a tinta cega vinte segundos no meio");
        if (WitchProjectile.TINTA != 4.0) helper.fail("num raio de quatro blocos");
        /*
         * E a grama é chão em que um cato pega. Esta linha parece boba e não é: o rol do chão vai escrito à
         * mão porque os rótulos de hoje <b>não</b> traduzem os materiais de 2014 — o {@code #minecraft:dirt}
         * da 26.2 são três blocos e a grama não está entre eles. A conta diz também quantas entradas o rol
         * tem, para separar "o rol não carregou" de "o rol carregou e falta este bloco".
         */
        int quantos = helper.getLevel().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK)
                .get(WitchProjectile.CHÃO_DE_CATO).map(h -> h.size()).orElse(-1);
        if (!Blocks.GRASS_BLOCK.defaultBlockState().is(WitchProjectile.CHÃO_DE_CATO)) {
            helper.fail("a grama é chão em que um cato pega; o rol tem " + quantos + " entradas");
        }
        helper.succeed();
    }

    /**
     * <b>A cara não diz o efeito.</b>
     *
     * <p>Esta é a prova que carrega a fatia, e ela é sobre o <b>desenho</b>: as doze minas são três desenhos,
     * não doze. Quatro papoulas com quatro efeitos diferentes apontam todas para o mesmo modelo.
     */
    @GameTest(maxTicks = 20)
    public void theFaceSaysNothingAboutTheEffect(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        int qual = 0;
        for (PlantMineBlock.Effect efeito : PlantMineBlock.Effect.values()) {
            BlockPos onde = helper.absolutePos(new BlockPos(1 + qual++, 2, 1));
            level.setBlockAndUpdate(onde, OccultaBlocks.PLANT_MINE.defaultBlockState()
                    .setValue(PlantMineBlock.CARA, PlantMineBlock.Look.ROSE)
                    .setValue(PlantMineBlock.EFEITO, efeito));
            if (level.getBlockState(onde).getValue(PlantMineBlock.CARA) != PlantMineBlock.Look.ROSE) {
                helper.fail("as quatro são papoulas");
            }
        }
        for (int i = 0; i < 4; i++) {
            level.setBlockAndUpdate(helper.absolutePos(new BlockPos(1 + i, 2, 1)),
                    Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    /** <b>A de teias</b> enche a cruz à volta dela de teia, e some. */
    @GameTest(maxTicks = 60)
    public void theWebOneFillsTheCrossWithWeb(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(2, 2, 3);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde, mina(PlantMineBlock.Effect.WEBS));

        var ovelha = helper.spawn(EntityTypes.SHEEP, aqui);
        helper.runAfterDelay(10, () -> {
            if (level.getBlockState(onde).is(OccultaBlocks.PLANT_MINE)) helper.fail("a mina some ao pisar");
            int teias = 0;
            for (Direction lado : Direction.values()) {
                if (level.getBlockState(onde.relative(lado)).is(Blocks.COBWEB)) teias++;
            }
            if (teias < 4) helper.fail("e deixa teia à volta, e deixou " + teias);
            ovelha.discard();
            limpa(helper, level, onde);
            helper.succeed();
        });
    }

    /** <b>A de tinta</b> cega quem está perto, e o tempo depende da distância. */
    @GameTest(maxTicks = 60)
    public void theInkOneBlindsWhoeverIsNear(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(5, 2, 3);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde, mina(PlantMineBlock.Effect.INK));

        var ovelha = helper.spawn(EntityTypes.SHEEP, aqui);
        helper.runAfterDelay(10, () -> {
            if (!ovelha.hasEffect(MobEffects.BLINDNESS)) helper.fail("a tinta cega quem pisa");
            ovelha.discard();
            limpa(helper, level, onde);
            helper.succeed();
        });
    }

    /** <b>A de espinhos</b> planta um cato, e o chão vira areia debaixo dele. */
    @GameTest(maxTicks = 60)
    public void theThornOnePlantsACactus(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(2, 2, 6);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde, mina(PlantMineBlock.Effect.THORNS));

        var ovelha = helper.spawn(EntityTypes.SHEEP, aqui);
        helper.runAfterDelay(10, () -> {
            if (!level.getBlockState(onde.below()).is(Blocks.SAND)) {
                helper.fail("o chão vira areia debaixo do cato, e ficou "
                        + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(
                                level.getBlockState(onde.below()).getBlock()));
            }
            if (!level.getBlockState(onde).is(Blocks.CACTUS)) {
                helper.fail("e o cato cresce, e ali está "
                        + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(
                                level.getBlockState(onde).getBlock()));
            }
            ovelha.discard();
            level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(onde.above(), Blocks.AIR.defaultBlockState());
            limpa(helper, level, onde);
            helper.succeed();
        });
    }

    /**
     * <b>A de brotos</b> faz nascer um galho para cima — e <b>levanta quem estava em cima dela</b>.
     *
     * <p>É o jeito do original de mandar alguém para o céu sem lhe tocar: o galho cresce debaixo dos pés e
     * quem estava ali sobe com ele.
     */
    @GameTest(maxTicks = 60)
    public void theSproutingOneGrowsABranchAndLiftsYou(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos aqui = new BlockPos(5, 2, 6);
        BlockPos onde = helper.absolutePos(aqui);
        level.setBlockAndUpdate(onde, mina(PlantMineBlock.Effect.SPROUTING));

        var ovelha = helper.spawn(EntityTypes.SHEEP, aqui);
        double antes = ovelha.getY();
        helper.runAfterDelay(10, () -> {
            int troncos = 0;
            for (int i = 0; i < 10; i++) {
                if (level.getBlockState(onde.above(i)).is(net.minecraft.tags.BlockTags.LOGS)) troncos++;
            }
            if (troncos < 3) helper.fail("o galho sobe, e subiu " + troncos);
            if (ovelha.getY() <= antes + 1.0) helper.fail("e leva consigo quem estava em cima");

            ovelha.discard();
            for (int i = 0; i < 12; i++) {
                level.setBlockAndUpdate(onde.above(i), Blocks.AIR.defaultBlockState());
                for (Direction lado : Direction.Plane.HORIZONTAL) {
                    level.setBlockAndUpdate(onde.above(i).relative(lado), Blocks.AIR.defaultBlockState());
                }
            }
            limpa(helper, level, onde);
            helper.succeed();
        });
    }

    private static net.minecraft.world.level.block.state.BlockState mina(PlantMineBlock.Effect qual) {
        return OccultaBlocks.PLANT_MINE.defaultBlockState().setValue(PlantMineBlock.EFEITO, qual);
    }

    /** Tira o que o efeito deixou, para a arena do lado não o encontrar. */
    private static void limpa(GameTestHelper helper, ServerLevel level, BlockPos onde) {
        AABB caixa = new AABB(onde).inflate(2.0, 2.0, 2.0);
        for (BlockPos ali : BlockPos.betweenClosed(
                BlockPos.containing(caixa.minX, caixa.minY, caixa.minZ),
                BlockPos.containing(caixa.maxX, caixa.maxY, caixa.maxZ))) {
            if (level.getBlockState(ali).is(Blocks.COBWEB) || level.getBlockState(ali).is(Blocks.CACTUS)) {
                level.setBlockAndUpdate(ali, Blocks.AIR.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(onde.below(), Blocks.GRASS_BLOCK.defaultBlockState());
    }
}
