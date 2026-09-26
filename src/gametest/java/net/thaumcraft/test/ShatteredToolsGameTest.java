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
    /** Uma varinha com o foco pedido preso e vis a mais do que o preciso. */
    private static ItemStack varinha(String foco) {
        ItemStack varinha = new ItemStack(net.thaumcraft.registry.TCItems.WAND);
        varinha.set(net.thaumcraft.registry.TCComponents.WAND_FOCUS, foco);
        var melhorias = new java.util.ArrayList<Short>();
        while (melhorias.size() < 5) melhorias.add((short) -1);
        varinha.set(net.thaumcraft.registry.TCComponents.FOCUS_UPGRADES, melhorias);
        var vis = new net.thaumcraft.api.aspects.AspectList();
        for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) vis.add(primal, 2500);
        varinha.set(net.thaumcraft.registry.TCComponents.WAND_VIS, vis);
        return varinha;
    }

    /** Aponta a varinha à casa dada e dispara o foco dela. */
    private static boolean aponta(GameTestHelper helper, net.minecraft.server.level.ServerPlayer quem,
                                  ItemStack varinha, BlockPos alvo) {
        var olho = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
        quem.snapTo(olho.x, olho.y, olho.z, 0.0f, 0.0f);
        quem.getInventory().setItem(0, varinha);
        quem.getInventory().setSelectedSlot(0);
        return net.thaumcraft.item.Focuses.tick(helper.getLevel(), quem, varinha,
                net.thaumcraft.item.Focuses.on(varinha));
    }

    /** O Foco de Firmar Fenda prende a fenda apontada, e na segunda vez não faz nada. */
    @GameTest
    public void theHoldFocusHoldsARift(GameTestHelper helper) {
        // o olho de quem joga fica a um bloco e meio do chão: a fenda tem de estar à altura dele
        BlockPos onde = new BlockPos(1, 3, 4);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        var fenda = helper.getBlockEntity(onde, RiftBlockEntity.class);
        if (fenda.stabilized()) helper.fail("a fenda nasce solta");

        var quem = helper.makeMockServerPlayerInLevel();
        ItemStack varinha = varinha("rift_hold");
        if (!aponta(helper, quem, varinha, onde)) helper.fail("o foco devia pegar na fenda apontada");
        if (!fenda.stabilized()) helper.fail("e prendê-la");

        if (aponta(helper, quem, varinha, onde)) helper.fail("e a segunda vez não faz nada");
        helper.succeed();
    }

    /** O Foco de Abrir Fenda rasga uma onde a varinha aponta, e ela sai à vista de quem a rasgou. */
    @GameTest
    public void theOpenFocusTearsARift(GameTestHelper helper) {
        // uma parede à altura dos olhos, para o foco ter onde bater
        helper.setBlock(new BlockPos(1, 3, 5), net.minecraft.world.level.block.Blocks.STONE);
        var quem = helper.makeMockServerPlayerInLevel();
        ItemStack varinha = varinha("rift_open");
        if (!aponta(helper, quem, varinha, BlockPos.ZERO)) helper.fail("o foco devia rasgar a fenda");

        BlockPos onde = new BlockPos(1, 3, 4);
        helper.assertBlockPresent(ShatteredBlocks.RIFT, onde);
        if (helper.getBlockEntity(onde, RiftBlockEntity.class).natural()) {
            helper.fail("quem a rasgou sabe onde a rasgou: esta não pede os óculos");
        }
        helper.succeed();
    }

    /** E o Foco de Fechar Fenda fecha-a. */
    @GameTest
    public void theCloseFocusClosesARift(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 3, 4);
        helper.setBlock(onde, ShatteredBlocks.RIFT);
        var quem = helper.makeMockServerPlayerInLevel();
        ItemStack varinha = varinha("rift_close");
        if (!aponta(helper, quem, varinha, onde)) helper.fail("o foco devia pegar na fenda apontada");
        helper.assertBlockNotPresent(ShatteredBlocks.RIFT, onde);
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
