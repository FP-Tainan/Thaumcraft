package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.ResearchManager;

import java.util.function.Consumer;

/**
 * Os objetos eldritch: o {@code ItemEldritchObject} da 4.2.3.5, um item por número — o olho eldritch (0), os ritos
 * carmesins (1), a tabuleta rúnica (2), a pérola primordial (3) e o colocador de obelisco do modo criativo (4).
 */
public class EldritchObjectItem extends Item {
    public enum Kind { EYE, CRIMSON_RITES, RUNED_TABLET, PRIMORDIAL_PEARL, OBELISK_PLACER }

    public final Kind kind;

    public EldritchObjectItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    /** Os ritos carmesins, lidos, ensinam a pesquisa do Culto Carmesim. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (this.kind == Kind.CRIMSON_RITES && !level.isClientSide()
                && !ResearchManager.knows(player, "CRIMSON") && ResearchManager.complete(player, "CRIMSON")) {
            level.playSound(null, player.blockPosition(), TCSounds.LEARN.value(), SoundSource.PLAYERS, 0.75f, 1.0f);
        }
        return this.kind == Kind.CRIMSON_RITES ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /** O {@code onItemUseFirst}: a pérola num nó de aura, e o colocador de obelisco em cima de um bloco. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (this.kind == Kind.OBELISK_PLACER && context.getClickedFace() == net.minecraft.core.Direction.UP) {
            if (player != null) player.swing(context.getHand());
            for (int a = 1; a <= 6; a++) {
                if (!level.isEmptyBlock(pos.above(a))) return InteractionResult.FAIL;
            }
            level.setBlock(pos.above(1), TCBlocks.ELDRITCH_ALTAR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos.above(3), TCBlocks.ELDRITCH_OBELISK.defaultBlockState(), Block.UPDATE_ALL);
            for (int a = 4; a <= 7; a++) level.setBlock(pos.above(a), TCBlocks.ELDRITCH_OBELISK_UPPER.defaultBlockState(), Block.UPDATE_ALL);
            return InteractionResult.SUCCESS;
        }
        if (this.kind == Kind.PRIMORDIAL_PEARL && level.getBlockEntity(pos) instanceof NodeBlockEntity node) {
            if (player != null) player.swing(context.getHand());
            if (!level.isClientSide()) pearl(level, pos, node, context.getItemInHand(), player);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    /**
     * A pérola primordial num nó: os compostos do nó perdem um ponto de base (metade das vezes), os primários variam de
     * menos dois a mais três (seis, com a pesquisa do nó primordial), primário ausente pode nascer, o feitio melhora
     * (esmaecido para pálido, pálido para comum, comum às vezes brilhante), e o nó cospe fluxo em volta numa explosão.
     */
    private static void pearl(Level level, BlockPos pos, NodeBlockEntity node, ItemStack stack, Player player) {
        stack.shrink(1);
        var random = level.getRandom();
        boolean research = player != null && ResearchManager.knows(player, "PRIMNODE");
        var base = node.baseAspects();
        for (Aspect a : base.getAspects()) {
            int m = base.getAmount(a);
            if (!a.isPrimal()) {
                if (random.nextBoolean()) node.setBase(a, m - 1);
            } else {
                node.setBase(a, m - 2 + random.nextInt(research ? 9 : 6));
            }
        }
        for (Aspect a : Aspects.primals()) {
            int m = node.baseAspects().getAmount(a);
            int r = random.nextInt(research ? 4 : 3);
            if (r > 0 && r > m) {
                node.setBase(a, r);
                node.addToContainer(a, 1);
            }
        }
        NodeModifier modifier = node.modifier();
        if (modifier == NodeModifier.FADING && random.nextBoolean()) node.setModifier(NodeModifier.PALE);
        else if (modifier == NodeModifier.PALE && random.nextBoolean()) node.setModifier(null);
        else if (modifier == null && random.nextInt(5) == 0) node.setModifier(NodeModifier.BRIGHT);
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        node.setChanged();
        level.explode(null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                3.0f + random.nextFloat() * (research ? 3 : 5), true, Level.ExplosionInteraction.BLOCK);
        for (int a = 0; a < 33; a++) {
            BlockPos at = pos.offset(random.nextInt(6) - random.nextInt(6), random.nextInt(6) - random.nextInt(6),
                    random.nextInt(6) - random.nextInt(6));
            if (!level.isEmptyBlock(at)) continue;
            if (at.getY() < pos.getY()) {
                level.setBlock(at, TCBlocks.FLUX_GOO.defaultBlockState().setValue(net.thaumcraft.block.FluxBlock.LEVEL, 7), Block.UPDATE_ALL);
            } else {
                level.setBlock(at, TCBlocks.FLUX_GAS.defaultBlockState().setValue(net.thaumcraft.block.FluxBlock.LEVEL, 7), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        switch (this.kind) {
            case EYE -> tooltip.accept(Component.translatable("item.ItemEldritchObject.text.1").withStyle(ChatFormatting.DARK_PURPLE));
            case CRIMSON_RITES -> {
                tooltip.accept(Component.translatable("item.ItemEldritchObject.text.2").withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.accept(Component.translatable("item.ItemEldritchObject.text.3").withStyle(ChatFormatting.DARK_BLUE));
            }
            case RUNED_TABLET -> tooltip.accept(Component.translatable("item.ItemEldritchObject.text.4").withStyle(ChatFormatting.DARK_PURPLE));
            case PRIMORDIAL_PEARL -> {
                tooltip.accept(Component.translatable("item.ItemEldritchObject.text.5").withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.accept(Component.translatable("item.ItemEldritchObject.text.6").withStyle(ChatFormatting.DARK_PURPLE));
            }
            case OBELISK_PLACER -> tooltip.accept(Component.literal("Creative Mode Only").withStyle(ChatFormatting.ITALIC));
        }
    }
}
