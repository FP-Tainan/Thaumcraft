package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileArcaneFurnaceNozzle} da 4.2.3.5: os blocos da fornalha infernal encostados no centro (os do meio das
 * paredes e o de baixo) aceitam um cano por fora. Quando a fornalha está quase sem pressa, eles puxam Ignis com
 * força 128, e cada unidade dá à fornalha 600 tiques acelerada.
 */
public class InfernalFurnaceNozzleBlockEntity extends BlockEntity implements EssentiaTransport {
    /** Para fora: o lado oposto ao do centro. Nulo se este bico não encosta no centro. */
    @Nullable
    private Direction facing;
    private boolean searched;
    @Nullable
    private InfernalFurnaceBlockEntity furnace;
    private int drawDelay;

    public InfernalFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFERNAL_FURNACE_NOZZLE, pos, state);
    }

    private void search() {
        if (this.searched || this.level == null) return;
        this.searched = true;
        for (Direction dir : Direction.values()) {
            if (this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof InfernalFurnaceBlockEntity found) {
                this.facing = dir.getOpposite();
                this.furnace = found;
                break;
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceNozzleBlockEntity nozzle) {
        nozzle.search();
        if (nozzle.furnace != null && nozzle.furnace.isRemoved()) nozzle.furnace = null;
        if (nozzle.furnace != null && nozzle.furnace.speedyTime < 60 && nozzle.drawEssentia()) nozzle.furnace.speedyTime += 600;
    }

    /** O {@code drawEssentia}: a cada cinco tiques, uma unidade de Ignis do cano de fora, se ele puxar menos que este. */
    boolean drawEssentia() {
        if (++this.drawDelay % 5 != 0 || this.facing == null) return false;
        if (!(this.level.getBlockEntity(this.worldPosition.relative(this.facing)) instanceof EssentiaTransport ic)
                || !ic.isConnectable(this.facing.getOpposite())) {
            return false;
        }
        if (!ic.canOutputTo(this.facing.getOpposite())) return false;
        return ic.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing)
                && ic.takeEssentia(Aspects.FIRE, 1, this.facing.getOpposite()) == 1;
    }

    @Override
    public boolean isConnectable(Direction face) {
        this.search();
        return this.facing != null;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        this.search();
        return this.facing != null;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return Aspects.FIRE;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        this.search();
        return this.furnace != null && this.furnace.speedyTime < 40 ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
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
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
