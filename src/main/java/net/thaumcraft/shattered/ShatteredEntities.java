package net.thaumcraft.shattered;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

/** As criaturas dos Reinos Fragmentados. */
public final class ShatteredEntities {
    /** O Monólito: a lousa que olha. */
    public static final EntityType<MonolithEntity> MONOLITH = register("monolith",
            FabricEntityType.Builder.createMob(MonolithEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(MonolithEntity::attributes))
                    .sized(3.0f, 3.0f).eyeHeight(1.5f).clientTrackingRange(10).fireImmune());

    private ShatteredEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(MONOLITH, MonolithEntity.attributes());
    }
}
