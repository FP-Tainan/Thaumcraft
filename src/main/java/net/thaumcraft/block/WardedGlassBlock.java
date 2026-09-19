package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.OwnedBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O vidro protegido: o tipo 2 do {@code BlockCosmeticOpaque} da 4.2.3.5. Vidro com dono (quem pôs), duro (5), que
 * explosão e chefão não quebram, e que se emenda com o vizinho (o {@code WardedGlassModel}); batido, mostra o escudo.
 */
public class WardedGlassBlock extends TransparentBlock implements EntityBlock {
    public static final MapCodec<WardedGlassBlock> CODEC = simpleCodec(WardedGlassBlock::new);

    public WardedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwnedBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof OwnedBlockEntity owned) {
            owned.owner = player.getName().getString();
            owned.setChanged();
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion,
                                  java.util.function.BiConsumer<ItemStack, BlockPos> drops) {
        // o onBlockExploded: explosão não o quebra
    }
}
