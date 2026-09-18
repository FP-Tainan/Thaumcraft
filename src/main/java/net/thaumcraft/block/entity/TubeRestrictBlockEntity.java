package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo estreito: o {@code TileTubeRestrict} da 4.2.3.5. Ele passa adiante só metade da força com que é
 * puxado, em vez de um a menos, e assim segura a essência de um lado da tubulação.
 */
public class TubeRestrictBlockEntity extends TubeBlockEntity {
    public TubeRestrictBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_RESTRICT, pos, state);
    }

    @Override
    protected void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(filter, true, directional);
    }
}
