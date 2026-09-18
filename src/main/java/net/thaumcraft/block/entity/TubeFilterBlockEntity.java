package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCResources;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo filtro: o {@code TileTubeFilter} da 4.2.3.5. Com um rótulo marcado preso nele, só puxa o aspecto do
 * rótulo; sem rótulo, é um tubo como os outros.
 */
public class TubeFilterBlockEntity extends TubeBlockEntity {
    private @Nullable Aspect aspectFilter;

    public TubeFilterBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_FILTER, pos, state);
    }

    public @Nullable Aspect aspectFilter() {
        return this.aspectFilter;
    }

    public void setAspectFilter(@Nullable Aspect aspect) {
        this.aspectFilter = aspect;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    /** O rótulo marcado com o aspecto do filtro, que volta para quem o tira. */
    public static ItemStack label(Aspect aspect) {
        ItemStack label = new ItemStack(TCResources.get("jar_label"));
        label.set(TCComponents.LABEL_ASPECT, aspect.tag());
        return label;
    }

    @Override
    protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(this.aspectFilter, restrict, directional);
    }

    /** Quebrado com o rótulo preso, o rótulo cai junto, como no {@code breakBlock} do original. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.aspectFilter != null && this.level != null) {
            Containers.dropItemStack(this.level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, label(this.aspectFilter));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.aspectFilter = input.getString("AspectFilter").map(Aspect::of).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.aspectFilter != null) output.putString("AspectFilter", this.aspectFilter.tag());
    }
}
