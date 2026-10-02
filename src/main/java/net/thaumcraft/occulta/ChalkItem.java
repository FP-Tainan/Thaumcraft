package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Um giz de círculo: o {@code ItemChalk} do Witchery.
 *
 * <p>Risca-se o chão com ele. O <b>giz dourado</b> risca o glifo do meio, que é onde o ritual acontece; os outros
 * três riscam os glifos dos anéis, cada risco com um dos doze desenhos, sorteado.
 *
 * <p>Riscar por cima de um glifo já riscado <b>o troca</b> pelo deste giz — é assim que se muda um anel de giz
 * sem o apagar primeiro. E riscar por cima de um do mesmo giz só troca o desenho.
 *
 * <p>Cada risco gasta um ponto do giz, que dura sessenta e quatro.
 */
public class ChalkItem extends Item {
    private final Supplier<Block> glyph;

    public ChalkItem(Supplier<Block> glyph, Properties properties) {
        super(properties);
        this.glyph = glyph;
    }

    public Block glyph() {
        return this.glyph.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return InteractionResult.SUCCESS;

        Block risca = this.glyph.get();
        BlockState oQueTem = level.getBlockState(onde);
        BlockPos alvo = oQueTem.is(risca) || isGlyph(oQueTem) ? onde : onde.above();

        if (!drawOn(server, alvo, risca)) return InteractionResult.PASS;

        level.playSound(null, alvo, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        Player quem = context.getPlayer();
        ItemStack giz = context.getItemInHand();
        if (quem != null && !quem.hasInfiniteMaterials()) {
            giz.hurtAndBreak(1, quem, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code drawGlyph}: risca ali, se ali der para riscar. */
    public static boolean drawOn(net.minecraft.server.level.ServerLevel level, BlockPos onde, Block risca) {
        BlockState oQueTem = level.getBlockState(onde);
        BlockState novo = risca.defaultBlockState();
        if (novo.hasProperty(GlyphBlock.SHAPE)) {
            novo = novo.setValue(GlyphBlock.SHAPE, level.getRandom().nextInt(GlyphBlock.SHAPES));
        }
        if (!oQueTem.isAir() && !isGlyph(oQueTem)) return false;
        if (!novo.canSurvive(level, onde)) return false;
        level.setBlockAndUpdate(onde, novo);
        return true;
    }

    /** Se aquilo é risco de giz, de qualquer um dos quatro. */
    public static boolean isGlyph(BlockState state) {
        return state.getBlock() instanceof GlyphBlock || state.is(OccultaBlocks.CIRCLE_HEART);
    }
}
