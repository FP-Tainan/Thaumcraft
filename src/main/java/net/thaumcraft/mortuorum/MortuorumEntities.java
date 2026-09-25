package net.thaumcraft.mortuorum;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

/** As criaturas do Ars Mortuorum. */
public final class MortuorumEntities {
    /** O Lacaio, que não nasce do mundo: é costurado e acordado no altar. */
    public static final EntityType<MinionEntity> MINION = register("minion",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            MinionEntity::new, MobCategory.CREATURE, mob -> mob
                                    .defaultAttributes(MinionEntity::attributes))
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(8));

    private MortuorumEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(MINION, MinionEntity.attributes());
    }
}
