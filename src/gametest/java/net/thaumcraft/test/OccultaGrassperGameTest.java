package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.GrassperBlock;
import net.thaumcraft.occulta.GrassperBlockEntity;
import net.thaumcraft.occulta.MutandisItem;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * O <b>Apanha-Erva</b>: a planta que segura o que lhe dão.
 *
 * <p>A prova que carrega a fatia é a da <b>ordem</b>: cheio, ele <b>sempre</b> larga; vazio, ele pega uma.
 * Não há como trocar o que ele segura sem primeiro o esvaziar — e é essa regra que faz dele uma peça de
 * receita em que se pode confiar.
 */
public class OccultaGrassperGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static void toca(GameTestHelper helper, Player quem, BlockPos casa) {
        helper.getLevel().getBlockState(casa).useWithoutItem(helper.getLevel(), quem,
                new BlockHitResult(Vec3.atCenterOf(casa), Direction.UP, casa, false));
    }

    /**
     * <b>Dá e tira, nesta ordem.</b>
     *
     * <p>De mão cheia e boca vazia, ele pega <b>uma</b>. De boca cheia, larga — mesmo que quem o toque traga
     * outra coisa na mão.
     */
    @GameTest(maxTicks = 40)
    public void itTakesOneAndThenAlwaysGivesBack(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(casa, OccultaBlocks.GRASSPER.defaultBlockState());

        Player quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2.0, 2.5)));
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(Items.ENDER_PEARL, 5));

        toca(helper, quem, casa);
        if (!GrassperBlock.segura(level, casa, Items.ENDER_PEARL)) {
            helper.fail("de boca vazia, ele pega o que lhe dão: bloco "
                    + level.getBlockState(casa).getBlock() + ", alma " + level.getBlockEntity(casa)
                    + ", boca " + GrassperBlock.oQueSegura(level, casa));
        }
        if (quem.getMainHandItem().getCount() != 4) {
            helper.fail("e pega uma só, sobrando " + quem.getMainHandItem().getCount());
        }

        // de boca cheia, larga — mesmo com outra coisa na mão
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(Items.DIAMOND, 3));
        toca(helper, quem, casa);
        if (!GrassperBlock.oQueSegura(level, casa).isEmpty()) {
            helper.fail("de boca cheia, ele larga sempre");
        }
        if (quem.getMainHandItem().getCount() != 3) {
            helper.fail("e não pega o que a mão trazia na mesma vez");
        }

        var caiu = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(casa).inflate(3.0));
        boolean achou = caiu.stream().anyMatch(item -> item.getItem().is(Items.ENDER_PEARL));
        if (!achou) helper.fail("e o que ele largou cai no chão");
        caiu.forEach(net.minecraft.world.entity.Entity::discard);

        level.setBlockAndUpdate(casa, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** E quebrado, ele larga o que estava segurando. */
    @GameTest(maxTicks = 40)
    public void brokenItDropsWhatItHeld(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos casa = helper.absolutePos(new BlockPos(6, 2, 6));
        level.setBlockAndUpdate(casa, OccultaBlocks.GRASSPER.defaultBlockState());
        if (level.getBlockEntity(casa) instanceof GrassperBlockEntity alma) {
            alma.põe(new ItemStack(Items.BLAZE_POWDER));
        }

        var caixa = new net.minecraft.world.phys.AABB(casa).inflate(3.0);
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caixa)
                .forEach(net.minecraft.world.entity.Entity::discard);

        level.destroyBlock(casa, false);
        boolean achou = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caixa)
                .stream().anyMatch(item -> item.getItem().is(Items.BLAZE_POWDER));
        if (!achou) helper.fail("quebrado, ele larga o que segurava");
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, caixa)
                .forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /**
     * <b>De onde ele vem</b>: um baú vazio, com quatro tufos de grama à volta e água por baixo.
     *
     * <p>É a conta que o {@code PORTE.md} já esperava desde a fatia da Rosa de Sangue, onde ficou escrito
     * que o baú comum vira um Apanha-Erva. Agora vira.
     */
    @GameTest(maxTicks = 40)
    public void anEmptyChestInGrassBecomesFourGrasspers(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos baú = helper.absolutePos(new BlockPos(4, 2, 4));

        level.setBlockAndUpdate(baú.below(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(baú, Blocks.CHEST.defaultBlockState());
        if (MutandisItem.éBaúDeApanhaErva(level, baú)) helper.fail("sem a grama à volta, não é");

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(baú.relative(lado), Blocks.SHORT_GRASS.defaultBlockState());
        }
        if (!MutandisItem.éBaúDeApanhaErva(level, baú)) helper.fail("com a grama e a água, é");

        // e um baú com alguma coisa dentro não serve: o que lá estiver não se perde por um descuido
        if (level.getBlockEntity(baú)
                instanceof net.minecraft.world.level.block.entity.ChestBlockEntity caixa) {
            caixa.setItem(0, new ItemStack(Items.DIAMOND));
            if (MutandisItem.éBaúDeApanhaErva(level, baú)) helper.fail("um baú com coisa dentro não serve");
            caixa.setItem(0, ItemStack.EMPTY);
        }

        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(baú.relative(lado), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(baú, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(baú.below(), Blocks.STONE.defaultBlockState());
        helper.succeed();
    }
}
