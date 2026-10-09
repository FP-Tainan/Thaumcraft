package net.thaumcraft.occulta.torment;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Baú de Reabastecimento</b>: a {@code BlockRefillingChest} do Witchery.
 *
 * <p>É um baú do jogo em tudo — mesmo desenho, mesma tampa, mesmos vinte e sete lugares — e indestrutível,
 * porque ele é parte do labirinto e não um móvel que alguém leve para casa. Quem o quiser leva o que ele
 * tem dentro, e deixa o baú onde ele está.
 *
 * <p>O que ele tem de seu está no {@link RefillingChestBlockEntity}: de hora em hora, estando vazio e
 * estando no Tormento, ele se enche outra vez.
 */
public class RefillingChestBlock extends ChestBlock {
    public static final MapCodec<RefillingChestBlock> CODEC = simpleCodec(RefillingChestBlock::new);

    public RefillingChestBlock(Properties properties) {
        super(() -> OccultaBlocks.REFILLING_CHEST_ENTITY, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE,
                properties);
    }

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new RefillingChestBlockEntity(onde, feitio);
    }

    /**
     * A tampa anda no cliente, como em qualquer baú, e no servidor corre a conta da hora.
     */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState feitio,
                                                                            BlockEntityType<T> tipo) {
        if (tipo != OccultaBlocks.REFILLING_CHEST_ENTITY) return null;
        if (level.isClientSide()) {
            return (mundo, onde, qual, baú) ->
                    ChestBlockEntity.lidAnimateTick(mundo, onde, qual, (ChestBlockEntity) baú);
        }
        return (mundo, onde, qual, baú) ->
                RefillingChestBlockEntity.serverTick(mundo, onde, qual, (RefillingChestBlockEntity) baú);
    }
}
