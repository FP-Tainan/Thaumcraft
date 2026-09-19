package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileAlchemyFurnaceAdvancedNozzle} da 4.2.3.5: os lados de baixo da fornalha avançada. Cada um solta, para o
 * cano de fora, a essência guardada no meio; não recebe nada.
 */
public class AdvancedAlchemicalFurnaceNozzleBlockEntity extends BlockEntity implements AspectContainer, EssentiaTransport {
    @Nullable
    private Direction facing;
    private boolean searched;
    @Nullable
    private AdvancedAlchemicalFurnaceBlockEntity furnace;

    public AdvancedAlchemicalFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE, pos, state);
    }

    private void search() {
        if (this.furnace != null && this.furnace.isRemoved()) {
            this.furnace = null;
            this.searched = false;
        }
        if (this.searched || this.level == null) return;
        this.searched = true;
        this.facing = null;
        for (Direction dir : Direction.values()) {
            if (this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof AdvancedAlchemicalFurnaceBlockEntity found) {
                this.facing = dir.getOpposite();
                this.furnace = found;
                break;
            }
        }
    }

    @Nullable
    public AdvancedAlchemicalFurnaceBlockEntity furnace() {
        this.search();
        return this.furnace;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedAlchemicalFurnaceNozzleBlockEntity nozzle) {
        nozzle.search();
    }

    @Override
    public AspectList getAspects() {
        this.search();
        return this.furnace != null ? this.furnace.aspects : new AspectList();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        this.search();
        if (this.furnace == null || this.furnace.aspects.getAmount(aspect) < amount) return false;
        this.furnace.aspects.remove(aspect, amount);
        this.furnace.vis = this.furnace.aspects.visSize();
        this.furnace.sync();
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        this.search();
        return this.furnace != null && this.furnace.aspects.getAmount(aspect) >= amount;
    }

    @Override
    public int containerContains(@Nullable Aspect aspect) {
        this.search();
        return this.furnace == null || aspect == null ? 0 : this.furnace.aspects.getAmount(aspect);
    }

    @Override
    public boolean isConnectable(Direction face) {
        this.search();
        return face == this.facing;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        this.search();
        return face == this.facing;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    /** O primeiro aspecto guardado, como no original. */
    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction face) {
        this.search();
        return this.furnace == null || this.furnace.aspects.isEmpty() ? null : this.furnace.aspects.getAspects().getFirst();
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        Aspect first = this.getEssentiaType(face);
        return first == null ? 0 : this.furnace.aspects.getAmount(first);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
