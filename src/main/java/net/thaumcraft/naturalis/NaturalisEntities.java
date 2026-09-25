package net.thaumcraft.naturalis;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.thaumcraft.Thaumcraft;

/** As criaturas do Magia Naturalis 0.5.0. */
public final class NaturalisEntities {
    /** O Revenante Feroz, que o foco levanta: do tamanho de um zumbi pequeno. */
    public static final EntityType<RevenantEntity> REVENANT = register("revenant",
            FabricEntityType.Builder.createMob(RevenantEntity::new, MobCategory.MONSTER,
                            mob -> mob.defaultAttributes(RevenantEntity::attributes))
                    .sized(0.3f, 0.975f).eyeHeight(0.93f).clientTrackingRange(8));

    private NaturalisEntities() {
    }

    public static void init() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name,
            EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Thaumcraft.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
}
