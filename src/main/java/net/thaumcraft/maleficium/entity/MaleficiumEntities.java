package net.thaumcraft.maleficium.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

/** As criaturas do Maleficium: as três que saem dos focos. */
public final class MaleficiumEntities {
    public static final EntityType<DarkMatterEntity> DARK_MATTER = register("dark_matter",
            EntityType.Builder.<DarkMatterEntity>of(DarkMatterEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    public static final EntityType<DiffusionEntity> DIFFUSION = register("diffusion",
            EntityType.Builder.<DiffusionEntity>of(DiffusionEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    public static final EntityType<HomingShardEntity> HOMING_SHARD = register("homing_shard",
            EntityType.Builder.<HomingShardEntity>of(HomingShardEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(2));

    private MaleficiumEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = Thaumcraft.id(name);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id,
                builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }

    public static void init() {
    }
}
