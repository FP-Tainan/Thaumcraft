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
    /** A esfera de gelo do foco de gelo, do tamanho que o dano três dá a ela no original. */
    public static final EntityType<FrostShardEntity> FROST_SHARD = register("frost_shard",
            EntityType.Builder.<FrostShardEntity>of(FrostShardEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    /** A brasa do foco de fogo. */
    public static final EntityType<net.thaumcraft.entity.EmberEntity> EMBER = register("ember",
            EntityType.Builder.<net.thaumcraft.entity.EmberEntity>of(net.thaumcraft.entity.EmberEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    /** A esfera do foco Primordial. */
    public static final EntityType<net.thaumcraft.entity.PrimalOrbEntity> PRIMAL_ORB = register("primal_orb",
            EntityType.Builder.<net.thaumcraft.entity.PrimalOrbEntity>of(net.thaumcraft.entity.PrimalOrbEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(8)
                    .updateInterval(1));

    /** O Alumentum arremessado, que explode onde cai. */
    public static final EntityType<net.thaumcraft.entity.AlumentumEntity> ALUMENTUM = register("alumentum",
            EntityType.Builder.<net.thaumcraft.entity.AlumentumEntity>of(net.thaumcraft.entity.AlumentumEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    /** O golem: o servo que faz o trabalho chato. */
    public static final EntityType<net.thaumcraft.entity.GolemEntity> GOLEM = register("golem",
            EntityType.Builder.<net.thaumcraft.entity.GolemEntity>of(
                            net.thaumcraft.entity.GolemEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.95f)
                    .eyeHeight(0.8f)
                    .clientTrackingRange(8));

    private TCEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    /** Chamado na abertura do mod só para as constantes acima saírem do papel. */
    public static void init() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
                GOLEM, net.thaumcraft.entity.GolemEntity.attributes());
    }
}
