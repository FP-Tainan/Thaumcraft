package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.block.entity.EssentiaMirrorBlockEntity;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.block.entity.LinkedMirrorBlockEntity;
import net.thaumcraft.block.entity.MirrorBlockEntity;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.item.HandMirrorItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** Os espelhos da 4.2.3.5: o mágico, o de essência e o de mão. */
public class MirrorGameTest {
    /** Um espelho colado na face sul de uma pedra, olhando para o sul. */
    private static void mirror(GameTestHelper helper, BlockPos at, Block block) {
        helper.setBlock(at.north(), Blocks.STONE.defaultBlockState());
        helper.setBlock(at, block.defaultBlockState().setValue(MirrorBlock.FACING, Direction.SOUTH));
    }

    /** Liga dois espelhos já postos, como o restoreLink deixaria. */
    private static void pair(GameTestHelper helper, BlockPos a, BlockPos b) {
        LinkedMirrorBlockEntity ma = helper.getBlockEntity(a, LinkedMirrorBlockEntity.class);
        LinkedMirrorBlockEntity mb = helper.getBlockEntity(b, LinkedMirrorBlockEntity.class);
        ma.setLink(helper.absolutePos(b), helper.getLevel().dimension());
        mb.setLink(helper.absolutePos(a), helper.getLevel().dimension());
        ma.linked = true;
        mb.linked = true;
    }

    private static UseOnContext click(GameTestHelper helper, Player player, BlockPos pos, Direction face) {
        return new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(
                helper.absoluteVec(Vec3.atCenterOf(pos)), face, helper.absolutePos(pos), false));
    }

    @GameTest(maxTicks = 120)
    public void aMirrorLinkedByTheItemCarriesThrownItems(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1), b = new BlockPos(4, 1, 1);
        mirror(helper, a, TCBlocks.MIRROR);
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.MIRROR));
        TCItems.MIRROR.useOn(click(helper, player, a, Direction.SOUTH));
        ItemStack linked = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).has(DataComponents.CUSTOM_DATA)) linked = player.getInventory().getItem(i);
        }
        if (linked.isEmpty() || !linked.is(TCItems.MIRROR)) helper.fail("o espelho clicado no outro vira um espelho ligado");
        // posto na face sul de outra pedra, ele refaz a ligação
        helper.setBlock(b.north(), Blocks.STONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, linked);
        TCItems.MIRROR.useOn(click(helper, player, b.north(), Direction.SOUTH));
        MirrorBlockEntity ma = helper.getBlockEntity(a, MirrorBlockEntity.class);
        MirrorBlockEntity mb = helper.getBlockEntity(b, MirrorBlockEntity.class);
        if (!ma.linked || !mb.linked || !ma.isLinkValid()) helper.fail("os dois ficam ligados");
        // o que se joga num sai no outro, um de cada vez
        ItemEntity thrown = new ItemEntity(helper.getLevel(), helper.absoluteVec(Vec3.atCenterOf(a)).x,
                helper.absoluteVec(Vec3.atCenterOf(a)).y, helper.absoluteVec(Vec3.atCenterOf(a)).z, new ItemStack(Items.COBBLESTONE, 3));
        thrown.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(thrown);
        helper.succeedWhen(() -> {
            if (!thrown.isRemoved()) throw helper.assertionException("o espelho engole o item");
            int out = helper.getEntities(net.minecraft.world.entity.EntityTypes.ITEM, b, 3.0).stream()
                    .filter(e -> e.getItem().is(Items.COBBLESTONE)).mapToInt(e -> e.getItem().getCount()).sum();
            if (out != 3) throw helper.assertionException("os três saem pelo outro, saíram " + out);
            // cada item que passa deixa os dois vidros mais instáveis (a instabilidade cai um por segundo)
            if (mb.instability < 1) throw helper.assertionException("o que sai deixa o vidro instável");
        });
    }

    @GameTest
    public void aBrokenMirrorRemembersItsPartner(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1), b = new BlockPos(4, 1, 1);
        mirror(helper, a, TCBlocks.MIRROR);
        mirror(helper, b, TCBlocks.MIRROR);
        pair(helper, a, b);
        helper.getLevel().destroyBlock(helper.absolutePos(a), true);
        MirrorBlockEntity mb = helper.getBlockEntity(b, MirrorBlockEntity.class);
        if (mb.linked) helper.fail("o par deixa de estar ligado");
        helper.succeedWhen(() -> {
            var drops = helper.getEntities(net.minecraft.world.entity.EntityTypes.ITEM, a, 2.0);
            if (drops.size() != 1) throw helper.assertionException("cai um espelho");
            ItemStack drop = drops.getFirst().getItem();
            if (!drop.is(TCItems.MIRROR) || !drop.has(DataComponents.CUSTOM_DATA)) throw helper.assertionException("o espelho cai lembrando o par");
        });
    }

    @GameTest
    public void aMirrorFallsWithoutItsWall(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        mirror(helper, a, TCBlocks.MIRROR);
        helper.setBlock(a.north(), Blocks.AIR.defaultBlockState());
        helper.succeedWhen(() -> helper.assertBlockNotPresent(TCBlocks.MIRROR, a));
    }

    @GameTest
    public void aHopperFeedsThePartner(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1), b = new BlockPos(4, 1, 1);
        mirror(helper, a, TCBlocks.MIRROR);
        mirror(helper, b, TCBlocks.MIRROR);
        pair(helper, a, b);
        MirrorBlockEntity ma = helper.getBlockEntity(a, MirrorBlockEntity.class);
        if (!ma.canPlaceItem(0, new ItemStack(Items.STONE))) helper.fail("com par no lugar, ele aceita");
        ma.setItem(0, new ItemStack(Items.STONE, 5));
        if (helper.getBlockEntity(b, MirrorBlockEntity.class).queued() != 5) helper.fail("o que se põe nele vai para a fila do par");
        if (!ma.getItem(0).isEmpty()) helper.fail("ele mesmo nunca guarda nada");
        helper.succeed();
    }

    @GameTest
    public void theEssentiaMirrorDrinksInFrontOfItsPartner(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1), b = new BlockPos(4, 1, 1);
        mirror(helper, a, TCBlocks.ESSENTIA_MIRROR);
        mirror(helper, b, TCBlocks.ESSENTIA_MIRROR);
        pair(helper, a, b);
        // o jarro atrás do par não conta; o da frente, sim
        BlockPos front = b.south(3);
        helper.setBlock(front, TCBlocks.JAR);
        JarBlockEntity jar = helper.getBlockEntity(front, JarBlockEntity.class);
        jar.addToContainer(Aspects.FIRE, 5);
        EssentiaMirrorBlockEntity ma = helper.getBlockEntity(a, EssentiaMirrorBlockEntity.class);
        if (!ma.takeFromContainer(Aspects.FIRE, 1)) helper.fail("o espelho dá a unidade tirada do jarro do outro lado");
        if (jar.containerContains(Aspects.FIRE) != 4) helper.fail("o jarro do outro lado perde uma");
        if (ma.takeFromContainer(Aspects.WATER, 1)) helper.fail("sem o aspecto do outro lado, nada");
        if (ma.takeFromContainer(Aspects.FIRE, 2)) helper.fail("uma unidade por vez");
        helper.succeed();
    }

    @GameTest
    public void theHandMirrorSendsToItsMirror(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        mirror(helper, a, TCBlocks.MIRROR);
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack hand = new ItemStack(TCItems.HAND_MIRROR);
        player.setItemInHand(InteractionHand.MAIN_HAND, hand);
        TCItems.HAND_MIRROR.useOn(click(helper, player, a, Direction.SOUTH));
        if (!hand.has(DataComponents.CUSTOM_DATA) || !hand.hasFoil()) helper.fail("o espelho de mão guarda o espelho clicado e brilha");
        if (!HandMirrorItem.transport(hand, new ItemStack(Items.DIAMOND, 2), player, helper.getLevel())) helper.fail("manda para o espelho");
        helper.assertItemEntityCountIs(Items.DIAMOND, a, 2.0, 2);
        // sem o espelho, a ligação se desfaz
        helper.setBlock(a, Blocks.AIR.defaultBlockState());
        if (HandMirrorItem.transport(hand, new ItemStack(Items.DIAMOND), player, helper.getLevel())) helper.fail("sem espelho não manda");
        if (hand.has(DataComponents.CUSTOM_DATA)) helper.fail("e esquece a ligação");
        helper.succeed();
    }

    @GameTest
    public void theThreeInfusionsExist(GameTestHelper helper) {
        for (var item : new net.minecraft.world.item.Item[]{TCItems.MIRROR, TCItems.ESSENTIA_MIRROR, TCItems.HAND_MIRROR}) {
            if (InfusionRecipes.ALL.stream().noneMatch(r -> r.result().is(item))) helper.fail("falta a infusão de " + item);
        }
        helper.succeed();
    }
}
