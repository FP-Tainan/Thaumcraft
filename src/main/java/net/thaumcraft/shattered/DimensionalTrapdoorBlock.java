package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;

/**
 * O Alçapão Dimensional: o {@code BlockDimensionalTrapdoor} das Portas Dimensionais.
 *
 * <p>É a mesma coisa que a porta, deitada: um alçapão com uma fenda a morar nele, e quem cai por ele aberto sai
 * num Reino Fragmentado. Depois de alguém passar, ele fecha-se atrás — a não ser que haja redstone a segurá-lo
 * aberto, que é o que o original faz.
 */
public class DimensionalTrapdoorBlock extends TrapDoorBlock implements EntityBlock {
    public static final MapCodec<DimensionalTrapdoorBlock> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BlockSetType.CODEC.fieldOf("block_set_type").forGetter(bloco -> bloco.getType()),
                    propertiesCodec()
            ).apply(instance, DimensionalTrapdoorBlock::new));

    public DimensionalTrapdoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public MapCodec<? extends TrapDoorBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiftBlockEntity(pos, state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean past) {
        if (!(level instanceof ServerLevel server)) return;
        if (!state.getValue(OPEN)) return;
        if (entity.isOnPortalCooldown()) return;
        if (!(server.getBlockEntity(pos) instanceof RiftBlockEntity fenda)) return;

        entity.setPortalCooldown(50);
        if (!fenda.teleport(entity)) return;
        entity.setPortalCooldown(0);

        // atrás de quem passa o alçapão fecha-se, se não houver redstone a segurá-lo
        if (entity instanceof net.minecraft.world.entity.player.Player && !server.hasNeighborSignal(pos)) {
            server.setBlock(pos, state.setValue(OPEN, false), 2);
        }
    }
}
