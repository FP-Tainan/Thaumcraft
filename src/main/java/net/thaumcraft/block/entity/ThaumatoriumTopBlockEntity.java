package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileThaumatoriumTop} da 4.2.3.5: a metade de cima do taumatório. Não tem nada seu — canos e funis que
 * encostam nela falam com a de baixo.
 */
public class ThaumatoriumTopBlockEntity extends BlockEntity implements WorldlyContainer, AspectContainer, EssentiaTransport {
    public ThaumatoriumTopBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.THAUMATORIUM_TOP, pos, state);
    }

    @Nullable
    public ThaumatoriumBlockEntity thaumatorium() {
        return this.level != null && this.level.getBlockEntity(this.worldPosition.below()) instanceof ThaumatoriumBlockEntity below ? below : null;
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        var t = this.thaumatorium();
        return t == null ? am : t.addToContainer(tt, am);
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        var t = this.thaumatorium();
        return t != null && t.takeFromContainer(tt, am);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        var t = this.thaumatorium();
        return t != null && t.doesContainerContainAmount(tt, am);
    }

    @Override
    public int containerContains(@Nullable Aspect tt) {
        var t = this.thaumatorium();
        return t == null ? 0 : t.containerContains(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        var t = this.thaumatorium();
        return t == null ? new AspectList() : t.essentia;
    }

    @Override
    public boolean isConnectable(Direction face) {
        var t = this.thaumatorium();
        return t != null && t.isConnectable(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        var t = this.thaumatorium();
        return t != null && t.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        var t = this.thaumatorium();
        if (t != null) t.setSuction(aspect, amount);
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        var t = this.thaumatorium();
        return t == null ? null : t.getSuctionType(face);
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        var t = this.thaumatorium();
        return t == null ? 0 : t.getSuctionAmount(face);
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        var t = this.thaumatorium();
        return t == null ? 0 : t.takeEssentia(aspect, amount, face);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        var t = this.thaumatorium();
        return t == null ? 0 : t.addEssentia(aspect, amount, face);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        var t = this.thaumatorium();
        return t == null || t.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        var t = this.thaumatorium();
        return t == null ? ItemStack.EMPTY : t.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var t = this.thaumatorium();
        return t == null ? ItemStack.EMPTY : t.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var t = this.thaumatorium();
        return t == null ? ItemStack.EMPTY : t.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        var t = this.thaumatorium();
        if (t != null) t.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }
}
