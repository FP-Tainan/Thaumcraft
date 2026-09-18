package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * O {@code TileEtherealBloom} da 4.2.3.5: dois contadores que só o desenho usa. O {@code growthCounter} conta
 * desde que a flor apareceu (ou desde que o mundo a carregou, já que o original não o guarda) e faz caule,
 * folhas e cristal crescerem; o {@code counter} começa num ponto sorteado e escolhe o quadro do brilho.
 */
public class EtherealBloomBlockEntity extends BlockEntity {
    public int counter;
    public int growthCounter;

    public EtherealBloomBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ETHEREAL_BLOOM, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EtherealBloomBlockEntity bloom) {
        if (bloom.counter == 0) bloom.counter = level.getRandom().nextInt(100);
        bloom.counter++;
        // o som das raízes brotando, só de quem vê, no primeiro tique
        if (level.isClientSide() && bloom.growthCounter == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.ROOTS.value(),
                    SoundSource.BLOCKS, 1.0f, 0.6f, false);
        }
        bloom.growthCounter++;
    }
}
