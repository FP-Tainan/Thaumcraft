package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

/**
 * O buraco que o foco do Buraco Portátil abre: o {@code TileHole} (com o {@code TileMemory} que ele herda) da
 * 4.2.3.5, descompilado.
 *
 * <p>O buraco guarda o bloco que estava ali e o devolve quando a conta chega ao fim — seis segundos, sem
 * melhorias. No primeiro tique, se ainda há profundidade para cavar, ele abre as oito casas em volta dele (um
 * túnel de três por três) e o bloco seguinte no rumo do túnel, que por sua vez faz o mesmo. Blocos com miolo,
 * rocha-mãe e o que não se quebra ficam de fora.
 */
public class HoleBlockEntity extends BlockEntity {
    /** O quanto um buraco dura sem melhorias: 120 tiques. */
    public static final short DURATION = 120;

    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void tick(Level level, BlockPos pos, HoleBlockEntity hole);
    }

    public static ClientEffects clientEffects = (level, pos, hole) -> {
    };

    private BlockState old = Blocks.AIR.defaultBlockState();
    private short countdown;
    private short countdownMax = DURATION;
    private byte count;
    /** O rumo do túnel, pelo número do {@code Direction}; −1 para as casas de volta, que não cavam. */
    private byte direction = -1;

    public HoleBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.HOLE, pos, state);
    }

    public short countdown() {
        return this.countdown;
    }

    public short countdownMax() {
        return this.countdownMax;
    }

    /**
     * O {@code createHole}: abre um buraco aqui, se o bloco deixar.
     *
     * @param side  o rumo do túnel, ou −1 para uma casa de volta
     * @param count quantos blocos de túnel ainda faltam, contando este
     */
    public static boolean createHole(Level level, BlockPos pos, int side, int count, short max) {
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) != null || state.is(Blocks.BEDROCK) || state.is(TCBlocks.HOLE)
                || state.isAir() || state.canBeReplaced() || state.getDestroySpeed(level, pos) < 0.0f) {
            return false;
        }
        level.setBlock(pos, TCBlocks.HOLE.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
            hole.old = state;
            hole.count = (byte) count;
            hole.countdownMax = max;
            hole.direction = (byte) side;
            hole.setChanged();
            level.sendBlockUpdated(pos, state, hole.getBlockState(), 3);
        }
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HoleBlockEntity hole) {
        if (level.isClientSide()) clientEffects.tick(level, pos, hole);

        if (!level.isClientSide() && hole.countdown == 0 && hole.count > 1 && hole.direction != -1) {
            Direction dir = Direction.from3DDataValue(hole.direction);
            // as oito casas em volta, no plano de través do túnel
            for (int a = 0; a < 9; a++) {
                if (a / 3 == 1 && a % 3 == 1) continue;
                int u = a / 3 - 1, v = a % 3 - 1;
                BlockPos ring = switch (dir.getAxis()) {
                    case Y -> pos.offset(u, 0, v);
                    case Z -> pos.offset(u, v, 0);
                    case X -> pos.offset(0, u, v);
                };
                createHole(level, ring, -1, 1, hole.countdownMax);
            }
            // e o próximo bloco do túnel: o original anda para dentro, contra a face clicada
            if (!createHole(level, pos.relative(dir.getOpposite()), hole.direction, hole.count - 1, hole.countdownMax)) {
                hole.count = 0;
            }
        }

        hole.countdown++;
        if (hole.countdown >= hole.countdownMax && !level.isClientSide()) {
            level.setBlock(pos, hole.old, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.old = input.read("old", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        this.countdown = (short) input.getIntOr("countdown", 0);
        this.countdownMax = (short) input.getIntOr("countdownmax", DURATION);
        this.count = (byte) input.getIntOr("count", 0);
        this.direction = (byte) input.getIntOr("direction", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("old", BlockState.CODEC, this.old);
        output.putInt("countdown", this.countdown);
        output.putInt("countdownmax", this.countdownMax);
        output.putInt("count", this.count);
        output.putInt("direction", this.direction);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
