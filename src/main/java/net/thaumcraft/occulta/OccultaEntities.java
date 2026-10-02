package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

/**
 * Os bichos do Ars Occulta.
 *
 * <p>Nenhum deles nasce do mundo: a mandrágora sai de uma raiz arrancada fora de hora, a de mina sai de um bulbo
 * largado no chão, e o Ent sai de uma tora do ofício quebrada. É assim no original, e é o que os põe no caminho
 * de quem mexe com o ofício sem cuidado.
 */
public final class OccultaEntities {
    /** A Mandrágora que anda e grita. */
    public static final EntityType<MandrakeEntity> MANDRAKE = register("mandrake",
            FabricEntityType.Builder.createMob(MandrakeEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(MandrakeEntity::attributes))
                    .sized(0.6f, 0.9f).eyeHeight(0.8f).clientTrackingRange(8));

    /** A Mandrágora-de-Mina, que estoura. */
    public static final EntityType<MinedrakeEntity> MINEDRAKE = register("minedrake",
            FabricEntityType.Builder.createMob(MinedrakeEntity::new, MobCategory.CREATURE,
                            mob -> mob.defaultAttributes(MinedrakeEntity::attributes))
                    .sized(0.6f, 0.8f).eyeHeight(0.7f).clientTrackingRange(8));

    /** E o Ent, que é a árvore de pé. */
    public static final EntityType<EntEntity> ENT = register("ent",
            FabricEntityType.Builder.createMob(EntEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(EntEntity::attributes))
                    .sized(1.2f, 3.0f).eyeHeight(2.7f).clientTrackingRange(10));

    /**
     * O frasco de cozimento atirado.
     *
     * <p>Não é bicho: é o {@code EntityBrew}, que voa como uma poção de arremesso e arrebenta onde bate.
     */
    public static final EntityType<net.thaumcraft.occulta.brew.BrewProjectile> BREW = register("brew",
            EntityType.Builder.<net.thaumcraft.occulta.brew.BrewProjectile>of(
                            net.thaumcraft.occulta.brew.BrewProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    /** A cara que aparece no vidro de um espelho a quem lhe pergunta. */
    public static final EntityType<net.thaumcraft.occulta.mirror.MirrorFaceEntity> MIRROR_FACE =
            register("mirror_face", FabricEntityType.Builder.createMob(
                            net.thaumcraft.occulta.mirror.MirrorFaceEntity::new, MobCategory.MISC,
                            mob -> mob.defaultAttributes(net.thaumcraft.occulta.mirror.MirrorFaceEntity::attributes))
                    .sized(0.5f, 0.5f).eyeHeight(0.25f).clientTrackingRange(8).fireImmune());

    /** E o Reflexo, o demônio que guarda a cela de um espelho. */
    public static final EntityType<net.thaumcraft.occulta.mirror.ReflectionEntity> REFLECTION =
            register("reflection", FabricEntityType.Builder.createMob(
                            net.thaumcraft.occulta.mirror.ReflectionEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(net.thaumcraft.occulta.mirror.ReflectionEntity::attributes))
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(10).fireImmune());

    /** O frasco do Caldeirão de Pote atirado. */
    public static final EntityType<net.thaumcraft.occulta.kettle.KettleBrewProjectile> KETTLE_BREW =
            register("kettle_brew", EntityType.Builder
                    .<net.thaumcraft.occulta.kettle.KettleBrewProjectile>of(
                            net.thaumcraft.occulta.kettle.KettleBrewProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));

    /** O corpo que fica deitado enquanto o espírito anda. */
    public static final EntityType<net.thaumcraft.occulta.spirit.CorpseEntity> CORPSE =
            register("corpse", FabricEntityType.Builder.createMob(
                            net.thaumcraft.occulta.spirit.CorpseEntity::new, MobCategory.MISC,
                            mob -> mob.defaultAttributes(net.thaumcraft.occulta.spirit.CorpseEntity::attributes))
                    .sized(0.6f, 0.6f).eyeHeight(0.4f).clientTrackingRange(10));

    /** E o Pesadelo, que mora do outro lado quando a noite corre mal. */
    public static final EntityType<net.thaumcraft.occulta.spirit.NightmareEntity> NIGHTMARE =
            register("nightmare", FabricEntityType.Builder.createMob(
                            net.thaumcraft.occulta.spirit.NightmareEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(net.thaumcraft.occulta.spirit.NightmareEntity::attributes))
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(10));

    /**
     * O <b>Guarda da Aldeia</b>.
     *
     * <p>Este não nasce de descuido do ofício, nasce com a aldeia: vem com o Forte e com a Torre de Vigia que o
     * Witchery põe nela. Por isso entra como {@code CREATURE} e não como monstro — ele mora ali. O tamanho que se
     * declara é o do guarda comum; o infernal estica-se a si mesmo quando lhe dizem o tipo.
     */
    public static final EntityType<net.thaumcraft.occulta.village.VillageGuardEntity> VILLAGE_GUARD =
            register("village_guard", FabricEntityType.Builder.createMob(
                            net.thaumcraft.occulta.village.VillageGuardEntity::new, MobCategory.CREATURE,
                            mob -> mob.defaultAttributes(
                                    net.thaumcraft.occulta.village.VillageGuardEntity::attributes))
                    .sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(10));

    private OccultaEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(MANDRAKE, MandrakeEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(MINEDRAKE, MinedrakeEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(ENT, EntEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(MIRROR_FACE, net.thaumcraft.occulta.mirror.MirrorFaceEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(REFLECTION, net.thaumcraft.occulta.mirror.ReflectionEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(CORPSE, net.thaumcraft.occulta.spirit.CorpseEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(NIGHTMARE, net.thaumcraft.occulta.spirit.NightmareEntity.attributes());
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
                .register(VILLAGE_GUARD, net.thaumcraft.occulta.village.VillageGuardEntity.attributes());
    }
}
