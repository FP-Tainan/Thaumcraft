package net.thaumcraft.mortuorum;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;

/** As criaturas do Ars Mortuorum. */
public final class MortuorumEntities {
    /** O Lacaio, que não nasce do mundo: é costurado e acordado no altar. */
    public static final EntityType<MinionEntity> MINION = register("minion",
            FabricEntityType.Builder.createMob(MinionEntity::new, MobCategory.CREATURE,
                            mob -> mob.defaultAttributes(MinionEntity::attributes))
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(8));

    /** O Rastejador da Noite, que nasce no lugar de um zumbi. */
    public static final EntityType<NightCrawlerEntity> NIGHT_CRAWLER = register("night_crawler",
            FabricEntityType.Builder.createMob(NightCrawlerEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(NightCrawlerEntity::attributes))
                    .sized(0.6f, 1.0f).eyeHeight(0.9f).clientTrackingRange(8));

    /** O Ursinho Animado, que se costura e afugenta o que mete medo. */
    public static final EntityType<TeddyEntity> TEDDY = register("teddy",
            FabricEntityType.Builder.createMob(TeddyEntity::new, MobCategory.CREATURE,
                            mob -> mob.defaultAttributes(TeddyEntity::attributes))
                    .sized(0.6f, 0.8f).eyeHeight(0.7f).clientTrackingRange(8));

    /** Os quatro Isaac, que se desfazem uns nos outros. */
    public static final EntityType<IsaacEntity> ISAAC_BODY = isaac("isaac_body", IsaacEntity.Kind.BODY);
    public static final EntityType<IsaacEntity> ISAAC_NORMAL = isaac("isaac_normal", IsaacEntity.Kind.NORMAL);
    public static final EntityType<IsaacEntity> ISAAC_BLOOD = isaac("isaac_blood", IsaacEntity.Kind.BLOOD);
    public static final EntityType<IsaacEntity> ISAAC_HEAD = isaac("isaac_head", IsaacEntity.Kind.HEAD);

    /** As duas lágrimas. */
    public static final EntityType<TearEntity> TEAR = register("tear",
            EntityType.Builder.<TearEntity>of(TearEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<TearEntity> TEAR_BLOOD = register("tear_blood",
            EntityType.Builder.<TearEntity>of(TearEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    private MortuorumEntities() {
    }

    private static EntityType<IsaacEntity> isaac(String name, IsaacEntity.Kind kind) {
        EntityType.EntityFactory<IsaacEntity> fábrica = IsaacEntity::new;
        EntityType<IsaacEntity> tipo = register(name, FabricEntityType.Builder.<IsaacEntity>createMob(
                        fábrica, MobCategory.MONSTER, mob -> mob.defaultAttributes(() -> IsaacEntity.attributes(kind)))
                .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(8));
        IsaacEntity.bind(tipo, kind);
        return tipo;
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(MINION, MinionEntity.attributes());
        FabricDefaultAttributeRegistry.register(NIGHT_CRAWLER, NightCrawlerEntity.attributes());
        FabricDefaultAttributeRegistry.register(TEDDY, TeddyEntity.attributes());
        FabricDefaultAttributeRegistry.register(ISAAC_BODY, IsaacEntity.attributes(IsaacEntity.Kind.BODY));
        FabricDefaultAttributeRegistry.register(ISAAC_NORMAL, IsaacEntity.attributes(IsaacEntity.Kind.NORMAL));
        FabricDefaultAttributeRegistry.register(ISAAC_BLOOD, IsaacEntity.attributes(IsaacEntity.Kind.BLOOD));
        FabricDefaultAttributeRegistry.register(ISAAC_HEAD, IsaacEntity.attributes(IsaacEntity.Kind.HEAD));
    }
}
