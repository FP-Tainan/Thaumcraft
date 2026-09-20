package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.TaintFibreBlock;
import net.thaumcraft.entity.taint.TaintCowEntity;
import net.thaumcraft.entity.taint.TaintSporeEntity;
import net.thaumcraft.entity.taint.ThaumicSlimeEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.Flux;
import net.thaumcraft.world.TCBiomes;

/**
 * A fauna da mácula da 4.2.3.5: quem morre com o fluxo da mácula volta maculado (a vaca vira vaca maculada; o que não
 * tem versão maculada vira slime taumático); o esporo sem o talo estoura em aranhas; o slime taumático cresce comendo
 * gosma e se divide ao morrer; e a gosma cheia solta slimes.
 */
public class TaintFaunaGameTest {
    @GameTest(maxTicks = 60)
    public void cowDiesTainted(GameTestHelper helper) {
        var cow = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 2));
        cow.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 200, 0));
        cow.kill(helper.getLevel());
        helper.succeedWhen(() -> {
            if (helper.getEntities(TCEntities.TAINT_COW).isEmpty()) helper.fail("a vaca devia ter voltado maculada");
        });
    }

    @GameTest(maxTicks = 60)
    public void zombieDiesAsSlime(GameTestHelper helper) {
        var zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        zombie.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 200, 0));
        // o tamanho sai da vida de quem morreu, e o zumbi nasce com um tanto de vida a mais, sorteado
        int esperado = (int) (1.0f + Math.min(zombie.getMaxHealth() / 10.0f, 6.0f));
        zombie.kill(helper.getLevel());
        helper.succeedWhen(() -> {
            var slimes = helper.getEntities(TCEntities.THAUMIC_SLIME);
            if (slimes.isEmpty()) helper.fail("o zumbi devia ter virado slime taumático");
            if (slimes.getFirst().getSize() != esperado) {
                helper.fail("o slime devia ter tamanho " + esperado + ", tem " + slimes.getFirst().getSize());
            }
        });
    }

    @GameTest(maxTicks = 60)
    public void sporeWithoutStalkBursts(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        TaintSporeEntity spore = helper.spawn(TCEntities.TAINT_SPORE, new BlockPos(2, 2, 2));
        spore.setSporeSize(9);
        helper.succeedWhen(() -> {
            if (spore.isAlive()) helper.fail("o esporo sem talo devia ter estourado");
            if (helper.getEntities(TCEntities.TAINT_SPIDER).isEmpty()) helper.fail("devia ter saído aranha");
        });
    }

    @GameTest(maxTicks = 60)
    public void sporeHoldsOnItsStalk(GameTestHelper helper) {
        BlockPos stalk = new BlockPos(2, 2, 2);
        // o bioma do jogo borra entre colunas vizinhas: pinta-se a vizinhança toda
        for (int dx = -4; dx <= 4; dx += 4) for (int dz = -4; dz <= 4; dz += 4) {
            BiomePainter.paint(helper.getLevel(), helper.absolutePos(stalk.offset(dx, 0, dz)), TCBiomes.TAINTED_LAND);
        }
        helper.setBlock(stalk.below(), Blocks.STONE);
        helper.setBlock(stalk, TCBlocks.TAINT_FIBRES.defaultBlockState().setValue(TaintFibreBlock.KIND, 4));
        TaintSporeEntity spore = helper.spawn(TCEntities.TAINT_SPORE, stalk.above());
        helper.runAfterDelay(40, () -> {
            if (!spore.isAlive()) helper.fail("o esporo em cima do talo devia continuar");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 100)
    public void slimeGrowsInGoo(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) helper.setBlock(at.offset(dx, -1, dz), Blocks.STONE);
        helper.setBlock(at, Flux.goo(8));
        ThaumicSlimeEntity slime = helper.spawn(TCEntities.THAUMIC_SLIME, at);
        slime.setSize(1);
        helper.succeedWhen(() -> {
            if (slime.getSize() < 2) helper.fail("o slime devia ter crescido comendo a gosma");
        });
    }

    @GameTest(maxTicks = 60)
    public void bigSlimeSplits(GameTestHelper helper) {
        ThaumicSlimeEntity slime = helper.spawn(TCEntities.THAUMIC_SLIME, new BlockPos(2, 2, 2));
        slime.setSize(9);
        slime.kill(helper.getLevel());
        helper.succeedWhen(() -> {
            long small = helper.getEntities(TCEntities.THAUMIC_SLIME).stream().filter(e -> e.isAlive() && e.getSize() == 1).count();
            if (small < 3) helper.fail("o slime de 9 devia se dividir em três (" + small + ")");
        });
    }

    @GameTest(maxTicks = 200)
    public void cowConversionKeepsGooDrop(GameTestHelper helper) {
        TaintCowEntity cow = helper.spawn(TCEntities.TAINT_COW, new BlockPos(2, 2, 2));
        cow.kill(helper.getLevel());
        helper.succeedWhen(() -> {
            var items = helper.getEntities(EntityTypes.ITEM);
            boolean found = items.stream().anyMatch(i -> i.getItem().is(net.thaumcraft.registry.TCResources.get("tainted_goo"))
                    || i.getItem().is(net.thaumcraft.registry.TCResources.get("taint_tendril")));
            if (!found) helper.fail("a vaca maculada devia deixar gosma ou ramo");
        });
    }
}
