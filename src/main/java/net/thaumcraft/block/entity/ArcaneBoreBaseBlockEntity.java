package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.block.ArcaneBoreBaseBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * A base da broca arcana: o {@code TileArcaneBoreBase} da 4.2.3.5. Puxa Perditio pelos canos (força 128, por todo
 * lado menos o bico), um de cada vez, quando a broca pede para acelerar; a varinha vira o bico para a face batida.
 */
public class ArcaneBoreBaseBlockEntity extends BlockEntity implements EssentiaTransport, Wandable {
    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_BORE_BASE, pos, state);
    }

    public Direction orientation() {
        return this.getBlockState().getValue(ArcaneBoreBaseBlock.FACING);
    }

    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (!level.isClientSide()) {
            level.setBlock(pos, this.getBlockState().setValue(ArcaneBoreBaseBlock.FACING, face), Block.UPDATE_ALL);
            level.playSound(null, pos, TCSounds.TOOL.value(), SoundSource.BLOCKS, 0.3f, 1.9f + level.getRandom().nextFloat() * 0.2f);
        }
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        return true;
    }

    /** O {@code drawEssentia}: um de Perditio de um cano ligado que o solte, se houver. */
    public boolean drawEssentia() {
        if (this.level == null) return false;
        for (Direction facing : Direction.values()) {
            if (!(this.level.getBlockEntity(this.worldPosition.relative(facing)) instanceof EssentiaTransport ic)
                    || !ic.isConnectable(facing.getOpposite())) {
                continue;
            }
            if (!ic.canOutputTo(facing.getOpposite())) return false;
            if (ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing)
                    && ic.takeEssentia(Aspects.ENTROPY, 1, facing.getOpposite()) == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(@Nullable Direction face) {
        return Aspects.ENTROPY;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return face != this.orientation() ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }
}
