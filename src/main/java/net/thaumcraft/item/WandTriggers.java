package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCItems;

/**
 * O que acontece quando se bate com a varinha em certas coisas do mundo.
 *
 * <p>É como o Thaumcraft 4.2.3.5 entrega as primeiras peças: não há receita de bancada para elas. Bate-se
 * com a varinha numa estante de livros e ela vira o Thaumonomicon; o resto das peças de taumaturgia sai
 * do mesmo jeito, cada uma do seu bloco, e vai entrando conforme as fatias chegarem.
 */
public final class WandTriggers {
    private WandTriggers() {
    }

    /**
     * Tenta o que a varinha faria neste bloco.
     *
     * @return o que dizer ao jogo, ou {@code PASS} se aquele bloco não responde à varinha
     */
    public static InteractionResult use(Level level, Player player, BlockPos pos, ItemStack wand) {
        return use(level, player, pos, wand, net.minecraft.core.Direction.UP);
    }

    /** O mesmo, sabendo em que face a varinha bateu (o reservatório de essência vira para ela). */
    public static InteractionResult use(Level level, Player player, BlockPos pos, ItemStack wand, net.minecraft.core.Direction face) {
        if (!(wand.getItem() instanceof WandItem)) return InteractionResult.PASS;
        BlockState state = level.getBlockState(pos);

        // primeiro quem sabe responder por si: válvula, tubo, matriz. É o IWandable do original, e é o
        // que faz da varinha a chave de fenda da taumaturgia
        if (level.getBlockEntity(pos) instanceof net.thaumcraft.api.wands.Wandable wandable
                && wandable.onWand(level, wand, player, pos, face)) {
            return InteractionResult.SUCCESS;
        }

        // obsidiana, tijolo do Nether ou grade de um cubo certo: a fornalha infernal (evento 2 do original)
        if (net.thaumcraft.block.InfernalFurnaceStructure.isTrigger(state) && player != null
                && net.thaumcraft.research.ResearchManager.knows(player, "INFERNALFURNACE")) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (net.thaumcraft.block.InfernalFurnaceStructure.create(wand, player, level, pos)) return InteractionResult.SUCCESS;
        }
        // duas construções alquímicas sobre um crisol: o taumatório (evento 5, que vem antes do 7 na mesma peça)
        if (state.is(net.thaumcraft.registry.TCBlocks.ALCHEMICAL_CONSTRUCT) && player != null
                && net.thaumcraft.research.ResearchManager.knows(player, "THAUMATORIUM")) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (net.thaumcraft.block.ThaumatoriumStructure.create(wand, player, level, pos, face)) return InteractionResult.SUCCESS;
        }
        // uma construção alquímica em volta de uma fornalha alquímica montada certo: a fornalha avançada (evento 7)
        if (net.thaumcraft.block.AdvancedAlchemicalFurnaceStructure.isTrigger(state) && player != null
                && net.thaumcraft.research.ResearchManager.knows(player, "ADVALCHEMYFURNACE")) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (net.thaumcraft.block.AdvancedAlchemicalFurnaceStructure.create(wand, player, level, pos)) return InteractionResult.SUCCESS;
        }
        // uma caixa de vidro com tampa de lajes em volta de um nó: o jarro para nós (evento 4)
        if (net.thaumcraft.block.NodeJarStructure.isTrigger(state) && player != null
                && net.thaumcraft.research.ResearchManager.knows(player, "NODEJAR")) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (net.thaumcraft.block.NodeJarStructure.create(wand, player, level, pos)) return InteractionResult.SUCCESS;
        }
        // o altar do anel com os quatro olhos e um nó sombrio em cima: o óculo, o portal para as Terras de Fora (evento 6)
        if (state.is(net.thaumcraft.registry.TCBlocks.ELDRITCH_ALTAR) && player != null
                && net.thaumcraft.research.ResearchManager.knows(player, "OCULUS")) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (createOculus(wand, player, level, pos)) return InteractionResult.SUCCESS;
        }
        // a estante de livros vira o caderno de pesquisa
        if (state.is(Blocks.BOOKSHELF)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.removeBlock(pos, false);
            net.minecraft.world.entity.item.ItemEntity book = new net.minecraft.world.entity.item.ItemEntity(
                    level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(TCItems.THAUMONOMICON));
            level.addFreshEntity(book);
            level.playSound(null, pos, net.thaumcraft.registry.TCSounds.WAND.value(), SoundSource.BLOCKS, 0.8f, 1.0f);
            net.thaumcraft.research.ResearchManager.grantStarters(player);
            return InteractionResult.SUCCESS;
        }
        // o caldeirão comum vira crisol, como no original
        if (state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            boolean full = state.is(Blocks.WATER_CAULDRON);
            level.setBlockAndUpdate(pos, net.thaumcraft.registry.TCBlocks.CRUCIBLE.defaultBlockState());
            if (full && level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.CrucibleBlockEntity crucible) {
                crucible.setWater(true);
            }
            level.playSound(null, pos, net.thaumcraft.registry.TCSounds.WAND.value(), SoundSource.BLOCKS, 0.8f, 0.9f);
            return InteractionResult.SUCCESS;
        }
        // a matriz rúnica: o primeiro toque ergue o altar (os cantos viram pilares), o seguinte começa a infusão
        if (level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.InfusionMatrixBlockEntity matrix) {
            if (!level.isClientSide()) matrix.poke(level, pos, player, wand);
            return InteractionResult.SUCCESS;
        }
        // a mesa do mod vira bancada arcana, e a varinha fica nela, pronta para pagar: o onWandRightClick do BlockTable
        if (state.is(net.thaumcraft.registry.TCBlocks.TABLE)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.setBlockAndUpdate(pos, net.thaumcraft.registry.TCBlocks.ARCANE_WORKBENCH.defaultBlockState());
            if (level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity bench
                    && wand.getItem() instanceof WandItem held && !held.isStaff()) {
                bench.setItem(net.thaumcraft.block.entity.ArcaneWorkbenchBlockEntity.WAND_SLOT, wand.copy());
                wand.setCount(0);
            }
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.15f, 0.5f);
            return InteractionResult.SUCCESS;
        }
        // a bancada comum também vira bancada arcana: atalho deste porte, anotado em docs/PORTE.md
        if (state.is(Blocks.CRAFTING_TABLE)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            level.setBlockAndUpdate(pos, net.thaumcraft.registry.TCBlocks.ARCANE_WORKBENCH.defaultBlockState());
            level.playSound(null, pos, net.thaumcraft.registry.TCSounds.WAND.value(), SoundSource.BLOCKS, 0.8f, 1.1f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * O {@code createOculus}: altar com os quatro olhos, ainda fechado, nó sombrio logo acima e labirinto já traçado — cem
     * de cada primordial da varinha, e o nó vira o portal eldritch.
     */
    private static boolean createOculus(ItemStack wand, Player player, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity altar)) return false;
        if (!(level.getBlockEntity(pos.above()) instanceof net.thaumcraft.block.entity.NodeBlockEntity node)) return false;
        if (altar.getEyes() != 4 || altar.isOpen() || node.type() != net.thaumcraft.api.nodes.NodeType.DARK || !altar.checkForMaze()) return false;
        var cost = new net.thaumcraft.api.aspects.AspectList();
        for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) cost.add(primal, 100);
        if (!WandItem.consume(wand, cost, true, player)) return false;
        level.playSound(null, pos, net.thaumcraft.registry.TCSounds.WAND.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        altar.setOpen(true);
        level.removeBlockEntity(pos.above());
        level.setBlockAndUpdate(pos.above(), net.thaumcraft.registry.TCBlocks.ELDRITCH_PORTAL.defaultBlockState());
        altar.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        return true;
    }
}
