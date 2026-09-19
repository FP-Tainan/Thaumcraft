package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.ArcaneEarBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O ouvido arcano: o tipo 1 do {@code BlockWoodenDevice} da 4.2.3.5. Afinado como um bloco musical (a mão sobe a nota,
 * e ele toca), escuta a até 64 blocos: quando um bloco musical do mesmo instrumento toca a mesma nota, ele dá meio
 * segundo de sinal forte. O instrumento é o do bloco de baixo, como no bloco musical.
 */
public class ArcaneEarBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneEarBlock> CODEC = simpleCodec(ArcaneEarBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ArcaneEarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneEarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TCBlockEntities.ARCANE_EAR, ArcaneEarBlockEntity::serverTick);
    }

    /** O {@code onBlockActivated}: sobe a nota e toca. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
            ear.changePitch();
            ear.triggerNote(level, pos, true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        if (level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) ear.updateTone();
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) ear.updateTone();
    }

    /** O {@code onBlockEventReceived}: o som do instrumento, alto (três), e a nota subindo. */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int instrument, int note) {
        if (instrument >= 0 && instrument < NoteBlockInstrument.values().length) {
            NoteBlockInstrument tone = NoteBlockInstrument.values()[instrument];
            if (tone.hasCustomSound()) tone = NoteBlockInstrument.HARP;
            level.playSeededSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tone.getSoundEvent(), SoundSource.RECORDS,
                    3.0f, NoteBlock.getPitchFromNote(note), level.getRandom().nextLong());
        }
        level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, note / 24.0, 0.0, 0.0);
        return true;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

}
