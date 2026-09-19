package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.FluxBlock;
import net.thaumcraft.block.TaintBlock;
import net.thaumcraft.block.TaintFibreBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.Flux;
import net.thaumcraft.world.TCBiomes;

/**
 * A mácula do {@code BlockTaint}/{@code BlockTaintFibres} da 4.2.3.5: fora do bioma dela, o solo volta a ser terra e a
 * crosta vira gosma; a crosta sem apoio cai e vira bloco de novo no chão; dentro do bioma, as fibras nascem em volta; e
 * o fluxo derramado escorre (a gosma) e sobe (o gás).
 */
public class TaintGameTest {
    private static void tick(GameTestHelper helper, BlockPos rel) {
        BlockPos at = helper.absolutePos(rel);
        helper.getLevel().getBlockState(at).randomTick(helper.getLevel(), at, helper.getLevel().getRandom());
    }

    private static void paint(GameTestHelper helper, BlockPos rel, net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome) {
        for (int dx = -4; dx <= 4; dx += 4) for (int dz = -4; dz <= 4; dz += 4) {
            BiomePainter.paint(helper.getLevel(), helper.absolutePos(rel.offset(dx, 0, dz)), biome);
        }
    }

    /** Fora da Terra Maculada, o solo maculado volta a ser terra (um em dez por tique ao acaso). */
    @GameTest(maxTicks = 400)
    public void soilWithersOutsideTheBiome(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        paint(helper, at, Biomes.PLAINS);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
            helper.setBlock(at.offset(dx, dy, dz), Blocks.STONE);
        }
        helper.setBlock(at, TCBlocks.TAINT_SOIL);
        helper.succeedWhen(() -> {
            tick(helper, at);
            if (!helper.getBlockState(at).is(Blocks.DIRT)) helper.fail("o solo maculado devia ter voltado a ser terra");
        });
    }

    /** Fora da Terra Maculada, a crosta vira gosma de fluxo cheia (um em vinte). */
    @GameTest(maxTicks = 400)
    public void crustTurnsToGooOutsideTheBiome(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        paint(helper, at, Biomes.PLAINS);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
            helper.setBlock(at.offset(dx, dy, dz), Blocks.STONE);
        }
        helper.setBlock(at, TCBlocks.TAINT_CRUST);
        helper.succeedWhen(() -> {
            if (helper.getBlockState(at).is(TCBlocks.TAINT_CRUST)) tick(helper, at);
            var state = helper.getBlockState(at);
            if (!state.is(TCBlocks.FLUX_GOO)) helper.fail("a crosta devia ter virado gosma");
            if (state.getValue(FluxBlock.LEVEL) != 7) helper.fail("a gosma devia nascer cheia");
        });
    }

    /** A crosta com ar embaixo cai como areia e, no chão, vira crosta de novo. */
    @GameTest(maxTicks = 200)
    public void crustFallsAndLands(GameTestHelper helper) {
        BlockPos floor = new BlockPos(3, 1, 3);
        BlockPos top = new BlockPos(3, 5, 3);
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(top, TCBlocks.TAINT_CRUST);
        tick(helper, top);
        if (helper.getLevel().getEntities(TCEntities.FALLING_TAINT, e -> true).isEmpty()) {
            helper.fail("a crosta devia ter começado a cair");
        }
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(floor.above()).is(TCBlocks.TAINT_CRUST)) helper.fail("a crosta devia ter chegado ao chão");
            if (!helper.getBlockState(top).isAir()) helper.fail("o lugar de onde ela caiu devia ficar vazio");
        });
    }

    /** Colada num tronco, a crosta não cai (o {@code canFallBelow}). */
    @GameTest(maxTicks = 40)
    public void crustHoldsNextToALog(GameTestHelper helper) {
        BlockPos top = new BlockPos(3, 5, 3);
        helper.setBlock(top.east().below(), Blocks.OAK_LOG);
        helper.setBlock(top, TCBlocks.TAINT_CRUST);
        if (TaintBlock.canFallBelow(helper.getLevel(), helper.absolutePos(top.below()))) helper.fail("tronco por perto segura a crosta");
        helper.succeed();
    }

    /** Dentro da Terra Maculada, a mácula põe fibras nas faces em volta. */
    @GameTest(maxTicks = 400)
    public void fibresGrowInsideTheBiome(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 1, 3);
        paint(helper, at, TCBiomes.TAINTED_LAND);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) helper.setBlock(at.offset(dx, 0, dz), Blocks.STONE);
        helper.setBlock(at, TCBlocks.TAINT_SOIL);
        helper.succeedWhen(() -> {
            tick(helper, at);
            boolean found = false;
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                if (helper.getBlockState(at.offset(dx, 1, dz)).is(TCBlocks.TAINT_FIBRES)) found = true;
            }
            if (!found) helper.fail("devia ter nascido fibra em cima da pedra em volta");
        });
    }

    /** Fibra fora do bioma maculado some no tique ao acaso. */
    @GameTest(maxTicks = 40)
    public void fibresDieOutsideTheBiome(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        paint(helper, at, Biomes.PLAINS);
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, TCBlocks.TAINT_FIBRES.defaultBlockState().setValue(TaintFibreBlock.KIND, 1));
        tick(helper, at);
        helper.assertBlockPresent(Blocks.AIR, at);
        helper.succeed();
    }

    /** O derrame do reservatório: gosma abaixo, gás acima, cheios. */
    @GameTest(maxTicks = 20)
    public void spillMakesGooBelowAndGasAbove(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 4, 4);
        Flux.spill(helper.getLevel(), helper.absolutePos(at), 100);
        int goo = 0, gas = 0;
        for (int dx = -4; dx <= 4; dx++) for (int dy = -4; dy <= 4; dy++) for (int dz = -4; dz <= 4; dz++) {
            var state = helper.getLevel().getBlockState(helper.absolutePos(at.offset(dx, dy, dz)));
            if (state.is(TCBlocks.FLUX_GOO)) {
                goo++;
                if (dy >= 0) helper.fail("gosma só abaixo do derrame");
            }
            if (state.is(TCBlocks.FLUX_GAS)) {
                gas++;
                if (dy < 0) helper.fail("gás só na altura do derrame ou acima");
            }
        }
        if (goo == 0 || gas == 0) helper.fail("o derrame devia dar gosma e gás (" + goo + ", " + gas + ")");
        helper.succeed();
    }

    /** A gosma cai inteira até o chão e se reparte com os lados. */
    @GameTest(maxTicks = 300)
    public void gooFallsAndSpreads(GameTestHelper helper) {
        BlockPos floor = new BlockPos(3, 1, 3);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) helper.setBlock(floor.offset(dx, 0, dz), Blocks.STONE);
        helper.setBlock(floor.above(4), Flux.goo(8));
        helper.succeedWhen(() -> {
            int total = 0;
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                var state = helper.getBlockState(floor.offset(dx, 1, dz));
                if (state.is(TCBlocks.FLUX_GOO)) total += FluxBlock.quanta(state);
            }
            if (!helper.getBlockState(floor.above()).is(TCBlocks.FLUX_GOO)) helper.fail("a gosma devia ter chegado ao chão");
            if (!helper.getBlockState(floor.offset(1, 1, 0)).is(TCBlocks.FLUX_GOO)) helper.fail("a gosma devia ter se espalhado");
            if (total > 8) helper.fail("a gosma não se cria andando: " + total);
        });
    }

    /** A Flor Etérea devolve às colunas maculadas o bioma natural delas. */
    @GameTest(maxTicks = 2400)
    public void theBloomRestoresTheBiome(GameTestHelper helper) {
        BlockPos bloomAt = new BlockPos(4, 2, 4);
        paint(helper, bloomAt, TCBiomes.TAINTED_LAND);
        helper.setBlock(bloomAt.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(bloomAt, TCBlocks.ETHEREAL_BLOOM);
        helper.succeedWhen(() -> {
            if (helper.getLevel().getBiome(helper.absolutePos(bloomAt)).is(TCBiomes.TAINTED_LAND)) {
                helper.fail("a coluna da flor ainda é Terra Maculada");
            }
        });
    }
}
