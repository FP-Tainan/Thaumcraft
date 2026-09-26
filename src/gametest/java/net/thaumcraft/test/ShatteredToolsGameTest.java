package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.shattered.RiftBlockEntity;
import net.thaumcraft.shattered.ShatteredBlocks;
import net.thaumcraft.shattered.ShatteredItems;
import net.thaumcraft.shattered.ShatteredMaterials;

/** As ferramentas dos Reinos Fragmentados: o Firma-Fendas, a Lâmina de Fenda e a armadura de Fio do Mundo. */
public class ShatteredToolsGameTest {
    @GameTest
    public void theStabilizerHoldsARift(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        var fenda = helper.getBlockEntity(onde, RiftBlockEntity.class);
        if (fenda.stabilized()) helper.fail("a fenda nasce solta");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack ferro = new ItemStack(ShatteredItems.RIFT_STABILIZER);
        player.setItemInHand(InteractionHand.MAIN_HAND, ferro);
        usar(helper, player, ferro, onde);

        if (!fenda.stabilized()) helper.fail("o Firma-Fendas prende a fenda");
        if (ferro.getDamageValue() != 1) helper.fail("e gasta um uso: " + ferro.getDamageValue());

        // a segunda vez não faz nada, e não gasta
        usar(helper, player, ferro, onde);
        if (ferro.getDamageValue() != 1) helper.fail("e não gasta de novo numa fenda já presa");
        helper.succeed();
    }

    /** A fenda presa não come mais o que está em volta. */
    @GameTest
    public void theHeldRiftStopsEating(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        BlockPos comida = new BlockPos(1, 2, 2);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        helper.setBlock(comida, net.minecraft.world.level.block.Blocks.STONE);
        var fenda = helper.getBlockEntity(onde, RiftBlockEntity.class);
        fenda.setStabilized(true);

        var estado = helper.getBlockState(onde);
        var random = helper.getLevel().getRandom();
        for (int volta = 0; volta < 200; volta++) {
            estado.randomTick(helper.getLevel(), helper.absolutePos(onde), random);
        }
        if (!helper.getBlockState(comida).is(net.minecraft.world.level.block.Blocks.STONE)) {
            helper.fail("a fenda presa não come o que está em volta");
        }
        helper.succeed();
    }

    /** A Lâmina de Fenda acha a fenda que está na frente de quem a empunha. */
    @GameTest
    public void theBladeFindsTheRiftAhead(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 onde = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
        player.snapTo(onde.x, onde.y, onde.z, 0.0f, 0.0f);

        if (net.thaumcraft.shattered.RiftBladeItem.riftAimedAt(helper.getLevel(), player) != null) {
            helper.fail("sem fenda na frente, a lâmina não acha nada");
        }
        // o giro zero olha para o sul, que é o +Z
        // o olho de quem joga fica a um bloco e meio do chão, então a fenda tem de estar à altura dele
        helper.setBlock(new BlockPos(1, 3, 4), ShatteredBlocks.RIFT);
        var achada = net.thaumcraft.shattered.RiftBladeItem.riftAimedAt(helper.getLevel(), player);
        if (achada == null) helper.fail("com fenda na frente, ela acha");
        helper.succeed();
    }

    /** Os números da armadura são os do original, e cada peça conserta-se com Fio do Mundo. */
    @GameTest
    public void theArmourCameFromTheOriginal(GameTestHelper helper) {
        var material = ShatteredMaterials.WOVEN_WORLD_THREAD;
        if (material.durability() != 20) helper.fail("vinte de durabilidade: " + material.durability());
        if (material.enchantmentValue() != 20) helper.fail("e vinte de encantabilidade");
        int[] esperado = {5, 4, 3, 2};
        var ordem = new net.minecraft.world.item.equipment.ArmorType[]{
                net.minecraft.world.item.equipment.ArmorType.HELMET,
                net.minecraft.world.item.equipment.ArmorType.CHESTPLATE,
                net.minecraft.world.item.equipment.ArmorType.LEGGINGS,
                net.minecraft.world.item.equipment.ArmorType.BOOTS};
        for (int i = 0; i < ordem.length; i++) {
            int achado = material.defense().getOrDefault(ordem[i], 0);
            if (achado != esperado[i]) {
                helper.fail(ordem[i] + " protege " + esperado[i] + ", e não " + achado);
            }
        }
        if (!new ItemStack(net.thaumcraft.shattered.ShatteredItems.WORLD_THREAD)
                .is(ShatteredMaterials.REPAIRS_ARMOR)) {
            helper.fail("e conserta-se com Fio do Mundo");
        }
        helper.succeed();
    }

    /** Usa o item do jeito que o jogo usa: no bloco, vindo de cima. */
    private static void usar(GameTestHelper helper, net.minecraft.world.entity.player.Player player,
                            ItemStack stack, BlockPos onde) {
        BlockPos absoluto = helper.absolutePos(onde);
        var bateu = new BlockHitResult(Vec3.atCenterOf(absoluto), net.minecraft.core.Direction.UP, absoluto, false);
        stack.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, bateu));
    }
}
