package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

/**
 * A Porta Dimensional: o {@code BlockDimensionalDoor} das Portas Dimensionais.
 *
 * <p>É uma porta como as outras, com uma fenda morando na metade de baixo. Quem a atravessa aberta sai do outro
 * lado dela — e o outro lado não é a sala seguinte, é um Reino Fragmentado. Atrás de quem passa a porta fecha-se,
 * como no original.
 */
public class DimensionalDoorBlock extends DoorBlock implements EntityBlock {
    public static final MapCodec<DimensionalDoorBlock> CODEC = RecordCodecBuilder();

    private static MapCodec<DimensionalDoorBlock> RecordCodecBuilder() {
        return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockSetType.CODEC.fieldOf("block_set_type").forGetter(bloco -> bloco.type()),
                propertiesCodec()
        ).apply(instance, DimensionalDoorBlock::new));
    }

    public DimensionalDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    /**
     * A porta que alguém assentou fica à vista de todos: só as que nasceram com o mundo pedem os Óculos do Véu.
     *
     * <p>Quem assenta passa por aqui; a geração do mundo não, e é isso que separa as duas.
     */
    
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                                net.minecraft.world.entity.LivingEntity quem,
                               net.minecraft.world.item.ItemStack ferramenta) {
        super.setPlacedBy(level, pos, state, quem, ferramenta);
        BlockPos baixo = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (level.getBlockEntity(baixo) instanceof RiftBlockEntity fenda) fenda.setNatural(false);
    }

    /** A fenda mora na metade de baixo, como no original. */
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new RiftBlockEntity(pos, state) : null;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean past) {
        if (!(level instanceof ServerLevel server)) return;
        if (!state.getValue(OPEN)) return;
        if (entity.isOnPortalCooldown()) return;

        BlockPos baixo = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (!(server.getBlockEntity(baixo) instanceof RiftBlockEntity fenda)) return;

        entity.setPortalCooldown(50);
        if (!fenda.teleport(entity)) return;
        entity.setPortalCooldown(0);
        // a porta fecha-se atrás de quem passa, se quem passou não estava agachado
        if (entity instanceof Player quem && !quem.isShiftKeyDown()) {
            this.setOpen(quem, server, server.getBlockState(baixo), baixo, false);
        }
    }
}
