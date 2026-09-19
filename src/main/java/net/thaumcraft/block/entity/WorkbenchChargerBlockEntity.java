package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O carregador da bancada arcana: o {@code TileMagicWorkbenchCharger} da 4.2.3.5. Um relé posto em cima da bancada
 * que, a cada tique, puxa da rede até cinco centésimos de cada primordial que ainda caiba na varinha da bancada.
 */
public class WorkbenchChargerBlockEntity extends VisRelayBlockEntity {
    public WorkbenchChargerBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.WORKBENCH_CHARGER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WorkbenchChargerBlockEntity charger) {
        VisRelayBlockEntity.tick(level, pos, state, charger);
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos.below()) instanceof ArcaneWorkbenchBlockEntity workbench)) return;
        ItemStack wand = workbench.getItem(ArcaneWorkbenchBlockEntity.WAND_SLOT);
        if (!(wand.getItem() instanceof WandItem)) return;
        boolean changed = false;
        for (Aspect aspect : WandItem.aspectsWithRoom(wand)) {
            int drain = Math.min(5, WandItem.maxVis(wand) - WandItem.vis(wand).getAmount(aspect));
            if (drain <= 0) continue;
            int got = charger.consumeVis(aspect, drain);
            if (got <= 0) continue;
            AspectList vis = WandItem.vis(wand);
            vis.add(aspect, got);
            WandItem.setVis(wand, vis);
            changed = true;
        }
        if (changed) workbench.setChanged();
    }
}
