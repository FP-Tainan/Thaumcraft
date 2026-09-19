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

    /** O baú itinerante: o baú que pula atrás do dono. */
    public static final EntityType<net.thaumcraft.entity.TravelingTrunkEntity> TRAVELING_TRUNK = register("traveling_trunk",
            EntityType.Builder.<net.thaumcraft.entity.TravelingTrunkEntity>of(net.thaumcraft.entity.TravelingTrunkEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .fireImmune()
                    .clientTrackingRange(4)
                    .updateInterval(3));

    /** A boia do golem pescador. */
    public static final EntityType<net.thaumcraft.entity.GolemBobberEntity> GOLEM_BOBBER = register("golem_bobber",
            EntityType.Builder.<net.thaumcraft.entity.GolemBobberEntity>of(net.thaumcraft.entity.GolemBobberEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .noSave()
                    .clientTrackingRange(4)
                    .updateInterval(5));

    /** O dardo do lança-dardos do golem. */
    public static final EntityType<net.thaumcraft.entity.DartEntity> DART = register("dart",
            EntityType.Builder.<net.thaumcraft.entity.DartEntity>of(net.thaumcraft.entity.DartEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20));

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

    /** O slime taumático, que nasce da gosma de fluxo. */
    public static final EntityType<net.thaumcraft.entity.taint.ThaumicSlimeEntity> THAUMIC_SLIME = register("thaumic_slime",
            EntityType.Builder.<net.thaumcraft.entity.taint.ThaumicSlimeEntity>of(net.thaumcraft.entity.taint.ThaumicSlimeEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** A aranha da mácula, que sai do esporo. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintSpiderEntity> TAINT_SPIDER = register("taint_spider",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintSpiderEntity>of(net.thaumcraft.entity.taint.TaintSpiderEntity::new, MobCategory.MONSTER)
                    .sized(0.4f, 0.3f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O Culto Carmesim: o cavaleiro, o clérigo, o pretor e o portal. */
    public static final EntityType<net.thaumcraft.entity.eldritch.CultistKnightEntity> CULTIST_KNIGHT = register("cultist_knight",
            EntityType.Builder.<net.thaumcraft.entity.eldritch.CultistKnightEntity>of(net.thaumcraft.entity.eldritch.CultistKnightEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(4).updateInterval(3));
    public static final EntityType<net.thaumcraft.entity.eldritch.CultistClericEntity> CULTIST_CLERIC = register("cultist_cleric",
            EntityType.Builder.<net.thaumcraft.entity.eldritch.CultistClericEntity>of(net.thaumcraft.entity.eldritch.CultistClericEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(4).updateInterval(3));
    public static final EntityType<net.thaumcraft.entity.eldritch.CultistLeaderEntity> CULTIST_LEADER = register("cultist_leader",
            EntityType.Builder.<net.thaumcraft.entity.eldritch.CultistLeaderEntity>of(net.thaumcraft.entity.eldritch.CultistLeaderEntity::new, MobCategory.MONSTER)
                    .sized(0.75f, 2.25f).eyeHeight(2.025f).clientTrackingRange(4).updateInterval(3));
    public static final EntityType<net.thaumcraft.entity.eldritch.CultistPortalEntity> CULTIST_PORTAL = register("cultist_portal",
            EntityType.Builder.<net.thaumcraft.entity.eldritch.CultistPortalEntity>of(net.thaumcraft.entity.eldritch.CultistPortalEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 3.0f).fireImmune().clientTrackingRange(4).updateInterval(20));
    /** O orbe que persegue o alvo, dos clérigos, do pretor e do golem eldritch. */
    public static final EntityType<net.thaumcraft.entity.GolemOrbEntity> GOLEM_ORB = register("golem_orb",
            EntityType.Builder.<net.thaumcraft.entity.GolemOrbEntity>of(net.thaumcraft.entity.GolemOrbEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(20));

    /** A aranha da mente, o susto da distorção. */
    public static final EntityType<net.thaumcraft.entity.MindSpiderEntity> MIND_SPIDER = register("mind_spider",
            EntityType.Builder.<net.thaumcraft.entity.MindSpiderEntity>of(net.thaumcraft.entity.MindSpiderEntity::new, MobCategory.MONSTER)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O tentáculo da mácula. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintacleEntity> TAINTACLE = register("taintacle",
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType.Builder.createMob(
                            net.thaumcraft.entity.taint.TaintacleEntity::new, MobCategory.MONSTER, mob -> mob
                                    .defaultAttributes(net.thaumcraft.entity.taint.TaintacleEntity::attributes)
                                    .spawnPlacement(net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                            net.thaumcraft.entity.taint.TaintacleEntity::checkSpawn))
                    .sized(0.66f, 3f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O tentáculo pequeno que o grande faz brotar. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintacleSmallEntity> TAINTACLE_SMALL = register("taintacle_small",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintacleSmallEntity>of(net.thaumcraft.entity.taint.TaintacleSmallEntity::new, MobCategory.MONSTER)
                    .sized(0.22f, 1f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O esporo em cima do talo. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintSporeEntity> TAINT_SPORE = register("taint_spore",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintSporeEntity>of(net.thaumcraft.entity.taint.TaintSporeEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    /** O enxameador de esporos que a crosta solta. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintSporeSwarmerEntity> TAINT_SPORE_SWARMER = register("taint_spore_swarmer",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintSporeSwarmerEntity>of(net.thaumcraft.entity.taint.TaintSporeSwarmerEntity::new, MobCategory.MONSTER)
                    .sized(1f, 1f)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    /** O enxame da mácula. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintSwarmEntity> TAINT_SWARM = register("taint_swarm",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintSwarmEntity>of(net.thaumcraft.entity.taint.TaintSwarmEntity::new, MobCategory.MONSTER)
                    .sized(2f, 2f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** A galinha maculada. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintChickenEntity> TAINT_CHICKEN = register("taint_chicken",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintChickenEntity>of(net.thaumcraft.entity.taint.TaintChickenEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.8f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** A vaca maculada. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintCowEntity> TAINT_COW = register("taint_cow",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintCowEntity>of(net.thaumcraft.entity.taint.TaintCowEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 1.3f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O creeper maculado. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintCreeperEntity> TAINT_CREEPER = register("taint_creeper",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintCreeperEntity>of(net.thaumcraft.entity.taint.TaintCreeperEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O porco maculado. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintPigEntity> TAINT_PIG = register("taint_pig",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintPigEntity>of(net.thaumcraft.entity.taint.TaintPigEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 0.9f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** A ovelha maculada. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintSheepEntity> TAINT_SHEEP = register("taint_sheep",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintSheepEntity>of(net.thaumcraft.entity.taint.TaintSheepEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 1.3f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** O aldeão maculado. */
    public static final EntityType<net.thaumcraft.entity.taint.TaintVillagerEntity> TAINT_VILLAGER = register("taint_villager",
            EntityType.Builder.<net.thaumcraft.entity.taint.TaintVillagerEntity>of(net.thaumcraft.entity.taint.TaintVillagerEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(10)
                    .updateInterval(3));

    /** A coisa que sai do crisol, flutuando. */
    public static final EntityType<net.thaumcraft.entity.SpecialItemEntity> SPECIAL_ITEM = register("special_item",
            EntityType.Builder.<net.thaumcraft.entity.SpecialItemEntity>of(net.thaumcraft.entity.SpecialItemEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(6)
                    .updateInterval(20));

    /** A flecha primordial. */
    public static final EntityType<net.thaumcraft.entity.PrimalArrowEntity> PRIMAL_ARROW = register("primal_arrow",
            EntityType.Builder.<net.thaumcraft.entity.PrimalArrowEntity>of(net.thaumcraft.entity.PrimalArrowEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20));

    /** O item que voa até quem o colheu (a pá e o triturador de três por três). */
    public static final EntityType<net.thaumcraft.entity.FollowingItemEntity> FOLLOWING_ITEM = register("following_item",
            EntityType.Builder.<net.thaumcraft.entity.FollowingItemEntity>of(net.thaumcraft.entity.FollowingItemEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(6)
                    .updateInterval(1));

    /** A garrafa de mácula arremessada. */
    public static final EntityType<net.thaumcraft.entity.BottleTaintEntity> BOTTLE_TAINT = register("bottle_taint",
            EntityType.Builder.<net.thaumcraft.entity.BottleTaintEntity>of(net.thaumcraft.entity.BottleTaintEntity::new, MobCategory.MISC)
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
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TRAVELING_TRUNK, net.thaumcraft.entity.TravelingTrunkEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(THAUMIC_SLIME, net.thaumcraft.entity.taint.ThaumicSlimeEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_SPIDER, net.thaumcraft.entity.taint.TaintSpiderEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(MIND_SPIDER, net.thaumcraft.entity.MindSpiderEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINTACLE_SMALL, net.thaumcraft.entity.taint.TaintacleSmallEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_SPORE, net.thaumcraft.entity.taint.TaintSporeEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_SPORE_SWARMER, net.thaumcraft.entity.taint.TaintSporeSwarmerEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_SWARM, net.thaumcraft.entity.taint.TaintSwarmEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_CHICKEN, net.thaumcraft.entity.taint.TaintChickenEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_COW, net.thaumcraft.entity.taint.TaintCowEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_CREEPER, net.thaumcraft.entity.taint.TaintCreeperEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_PIG, net.thaumcraft.entity.taint.TaintPigEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_SHEEP, net.thaumcraft.entity.taint.TaintSheepEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(TAINT_VILLAGER, net.thaumcraft.entity.taint.TaintVillagerEntity.attributes());
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.thaumcraft.world.TCBiomes.TAINTED_LAND),
                MobCategory.MONSTER, TAINTACLE, 1, 1, 1);
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(CULTIST_KNIGHT, net.thaumcraft.entity.eldritch.CultistKnightEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(CULTIST_CLERIC, net.thaumcraft.entity.eldritch.CultistClericEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(CULTIST_LEADER, net.thaumcraft.entity.eldritch.CultistLeaderEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(CULTIST_PORTAL, net.thaumcraft.entity.eldritch.CultistPortalEntity.attributes());
        net.thaumcraft.world.CreatureSpawns.init();
    }
}
