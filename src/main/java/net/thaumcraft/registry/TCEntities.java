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

    /** A crosta da mácula caindo, do tamanho da areia caindo. */
    public static final EntityType<net.thaumcraft.entity.FallingTaintEntity> FALLING_TAINT = register("falling_taint",
            EntityType.Builder.<net.thaumcraft.entity.FallingTaintEntity>of(net.thaumcraft.entity.FallingTaintEntity::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    /** A brasa do foco de fogo. */
    public static final EntityType<net.thaumcraft.entity.EmberEntity> EMBER = register("ember",
            EntityType.Builder.<net.thaumcraft.entity.EmberEntity>of(net.thaumcraft.entity.EmberEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    /** A bola de fogo do foco de fogo com a melhoria de mesmo nome. */
    public static final EntityType<net.thaumcraft.entity.ExplosiveOrbEntity> EXPLOSIVE_ORB = register("explosive_orb",
            EntityType.Builder.<net.thaumcraft.entity.ExplosiveOrbEntity>of(net.thaumcraft.entity.ExplosiveOrbEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    /** O orbe do choque de terra, do foco de raio. */
    public static final EntityType<net.thaumcraft.entity.ShockOrbEntity> SHOCK_ORB = register("shock_orb",
            EntityType.Builder.<net.thaumcraft.entity.ShockOrbEntity>of(net.thaumcraft.entity.ShockOrbEntity::new, MobCategory.MISC)
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

    /** O orbe de aspecto: a bolinha de vis que vai sozinha para a varinha. */
    public static final EntityType<net.thaumcraft.entity.AspectOrbEntity> ASPECT_ORB = register("aspect_orb",
            EntityType.Builder.<net.thaumcraft.entity.AspectOrbEntity>of(net.thaumcraft.entity.AspectOrbEntity::new, MobCategory.MISC)
                    .sized(0.125f, 0.125f)
                    .clientTrackingRange(6)
                    .updateInterval(20));

    /** O golem: o servo que faz o trabalho chato. */
    public static final EntityType<net.thaumcraft.entity.GolemEntity> GOLEM = register("golem",
            EntityType.Builder.<net.thaumcraft.entity.GolemEntity>of(
                            net.thaumcraft.entity.GolemEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.95f)
                    .eyeHeight(0.8f)
                    .clientTrackingRange(8));

    /** O zumbi zangado: nasce onde nascem monstros na superfície, peso dez. */
    public static final EntityType<net.thaumcraft.entity.BrainyZombieEntity> BRAINY_ZOMBIE = register("brainy_zombie",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            net.thaumcraft.entity.BrainyZombieEntity::new, MobCategory.MONSTER, mob -> mob
                                    .defaultAttributes(net.thaumcraft.entity.BrainyZombieEntity::attributes)
                                    .spawnPlacement(net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                            net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules))
                    .sized(0.6f, 1.95f).eyeHeight(1.74f).clientTrackingRange(8));

    /** O zumbi furioso: o dos nós sombrios; cresce com a raiva. */
    public static final EntityType<net.thaumcraft.entity.GiantBrainyZombieEntity> GIANT_BRAINY_ZOMBIE = register("giant_brainy_zombie",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            net.thaumcraft.entity.GiantBrainyZombieEntity::new, MobCategory.MONSTER, mob -> mob
                                    .defaultAttributes(net.thaumcraft.entity.GiantBrainyZombieEntity::attributes))
                    .sized(1.32f, 3.96f).clientTrackingRange(8));

    /** O fogo-fátuo: do tamanho de 0,9, sem gravidade. */
    public static final EntityType<net.thaumcraft.entity.WispEntity> WISP = register("wisp",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            net.thaumcraft.entity.WispEntity::new, MobCategory.MONSTER, mob -> mob
                                    .defaultAttributes(net.thaumcraft.entity.WispEntity::attributes)
                                    .spawnPlacement(net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                            net.thaumcraft.entity.WispEntity::checkSpawn))
                    .sized(0.9f, 0.9f).clientTrackingRange(8));

    /** O morcego de fogo: meio por 0,9, imune ao fogo. */
    public static final EntityType<net.thaumcraft.entity.FireBatEntity> FIREBAT = register("firebat",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            net.thaumcraft.entity.FireBatEntity::new, MobCategory.MONSTER, mob -> mob
                                    .defaultAttributes(net.thaumcraft.entity.FireBatEntity::attributes)
                                    .spawnPlacement(net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                            net.thaumcraft.entity.FireBatEntity::checkSpawn))
                    .sized(0.5f, 0.9f).fireImmune().clientTrackingRange(5));

    /** O pech: seis décimos por um e oito, os olhos a dois terços da altura. */
    public static final EntityType<net.thaumcraft.entity.PechEntity> PECH = register("pech",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            net.thaumcraft.entity.PechEntity::new, MobCategory.MONSTER, mob -> mob
                                    .defaultAttributes(net.thaumcraft.entity.PechEntity::attributes)
                                    .spawnPlacement(net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                            net.thaumcraft.entity.PechEntity::checkSpawn))
                    .sized(0.6f, 1.8f).eyeHeight(1.8f * 0.66f).clientTrackingRange(8));

    /** A rajada do pech mago e do foco dos pechs. */
    public static final EntityType<net.thaumcraft.entity.PechBlastEntity> PECH_BLAST = register("pech_blast",
            EntityType.Builder.<net.thaumcraft.entity.PechBlastEntity>of(net.thaumcraft.entity.PechBlastEntity::new, MobCategory.MISC)
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
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
                GOLEM, net.thaumcraft.entity.GolemEntity.attributes());
        net.thaumcraft.world.CreatureSpawns.init();
    }
}
