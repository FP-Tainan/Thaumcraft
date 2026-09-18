package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.TubeValveBlock;
import net.thaumcraft.block.entity.TubeBlockEntity;
import net.thaumcraft.block.entity.TubeBufferBlockEntity;
import net.thaumcraft.block.entity.TubeValveBlockEntity;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * A varinha batendo num tubo: o {@code onWandRightClick} do {@code TileTube}, do {@code TileTubeValve} e do
 * {@code TileTubeBuffer} da 4.2.3.5.
 *
 * <p>O original sabe em que pedaço do tubo a varinha bateu. Num braço, aquele lado abre ou fecha — e o
 * vizinho, se for tubo, acompanha do lado dele. No miolo, o tubo gira para o próximo lado que tenha com quem
 * se ligar: é assim que se aponta o tubo de mão única, e que se muda a roda da válvula de lugar. No tampão,
 * agachado, o braço estrangula em vez de fechar.
 */
public final class TubeWand {
    /** O miolo que o original deixa bater: de 0,34 a 0,66 em cada eixo. */
    private static final double CORE = 0.16;

    private TubeWand() {
    }

    /** @return o que aconteceu, ou nulo se não era tubo */
    @Nullable
    public static InteractionResult use(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof TubeBlockEntity) && !(entity instanceof TubeBufferBlockEntity)) return null;
        Player player = context.getPlayer();
        Vec3 local = context.getClickLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        double dx = local.x - 0.5, dy = local.y - 0.5, dz = local.z - 0.5;
        double core = entity instanceof TubeBufferBlockEntity ? 0.25 : CORE;
        boolean inCore = Math.abs(dx) <= core && Math.abs(dy) <= core && Math.abs(dz) <= core;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (inCore) {
            if (entity instanceof TubeValveBlockEntity) {
                turnValve(level, pos);
            } else if (entity instanceof TubeBlockEntity tube) {
                tube.rotate();
            } else {
                return InteractionResult.PASS;
            }
            tool(level, pos);
            return InteractionResult.SUCCESS;
        }

        Direction face;
        if (Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) >= Math.abs(dz)) face = dx > 0 ? Direction.EAST : Direction.WEST;
        else if (Math.abs(dy) >= Math.abs(dz)) face = dy > 0 ? Direction.UP : Direction.DOWN;
        else face = dz > 0 ? Direction.SOUTH : Direction.NORTH;

        if (entity instanceof TubeBufferBlockEntity buffer && player != null && player.isShiftKeyDown()) {
            buffer.cycleChoke(face);
            level.playSound(null, pos, TCSounds.SQUEEK.value(), SoundSource.BLOCKS, 0.6f,
                    1.1f + level.getRandom().nextFloat() * 0.2f);
            return InteractionResult.SUCCESS;
        }
        boolean open;
        if (entity instanceof TubeBufferBlockEntity buffer) {
            buffer.toggle(face);
            open = buffer.isOpen(face);
        } else {
            TubeBlockEntity tube = (TubeBlockEntity) entity;
            tube.toggle(face);
            open = tube.isOpen(face);
        }
        // o vizinho, se for tubo ou tampão, acompanha do lado dele
        BlockEntity beside = level.getBlockEntity(pos.relative(face));
        Direction back = face.getOpposite();
        if (beside instanceof TubeBlockEntity other && other.isOpen(back) != open) other.toggle(back);
        if (beside instanceof TubeBufferBlockEntity other && other.isOpen(back) != open) other.toggle(back);
        tool(level, pos);
        return InteractionResult.SUCCESS;
    }

    /** A roda da válvula vai para o próximo lado em que não haja cano. */
    private static void turnValve(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(TubeValveBlock.FACING)) return;
        int a = state.getValue(TubeValveBlock.FACING).get3DDataValue();
        for (int step = 1; step <= 6; step++) {
            Direction candidate = Direction.from3DDataValue((a + step) % 6);
            if (!state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(candidate))) {
                level.setBlock(pos, state.setValue(TubeValveBlock.FACING, candidate), 3);
                return;
            }
        }
    }

    private static void tool(Level level, BlockPos pos) {
        level.playSound(null, pos, TCSounds.TOOL.value(), SoundSource.BLOCKS, 0.5f,
                0.9f + level.getRandom().nextFloat() * 0.2f);
    }
}
