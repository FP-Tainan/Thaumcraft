package net.thaumcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * O golem guardado na mão: um toque no chão e ele levanta.
 *
 * <p>É o {@code ItemGolemPlacer} do original — um item por matéria. O que sai do item já vem com os
 * números daquela matéria; o serviço dele é outra história, e vem do núcleo.
 *
 * @param material de que ele é feito, pelo nome que o original dá à matéria
 */
public class GolemPlacerItem extends Item {
    private final String material;

    public GolemPlacerItem(Properties properties, String material) {
        super(properties);
        this.material = material;
    }

    public String material() {
        return this.material;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        var at = context.getClickedPos().relative(context.getClickedFace());
        GolemEntity golem = TCEntities.GOLEM.create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        if (golem == null) return InteractionResult.PASS;

        golem.setMaterial(this.material);
        golem.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5,
                context.getHorizontalDirection().getOpposite().toYRot(), 0.0f);
        golem.finalizeSpawn(level, level.getCurrentDifficultyAt(at),
                net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE, null);
        level.addFreshEntity(golem);
        level.playSound(null, at, TCSounds.CRAFT_START.value(), SoundSource.NEUTRAL, 0.7f, 1.2f);

        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.golem_" + this.material);
    }
}
