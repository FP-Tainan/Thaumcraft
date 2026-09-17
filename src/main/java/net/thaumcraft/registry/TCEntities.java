package net.thaumcraft.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.FrostShardEntity;

/** As criaturas e projéteis do mod. */
public final class TCEntities {
    /** A lasca de gelo do foco de gelo. */
    public static final EntityType<FrostShardEntity> FROST_SHARD = register("frost_shard",
            EntityType.Builder.<FrostShardEntity>of(FrostShardEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    private TCEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    /** Chamado na abertura do mod só para as constantes acima saírem do papel. */
    public static void init() {
    }
}
