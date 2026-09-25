package net.thaumcraft.forbidden;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * De que essência cada bicho é feito, para a Gaiola da Ira: o {@code spawnerMobs} do Forbidden Magic 0.575.
 *
 * <p>É esta tabela que diz o que a gaiola pede quando está afinada com um bicho — e quem não está nela pede
 * cobiça, como no original.
 */
public final class WrathMobs {
    private static final Map<Identifier, Aspect> ASPECTS = new LinkedHashMap<>();

    /** O que a gaiola pede quando não conhece o bicho. */
    public static final Aspect DEFAULT = Aspects.GREED;

    private WrathMobs() {
    }

    static {
        put("minecraft:zombie", Aspects.FLESH);
        put("minecraft:skeleton", Aspects.DEATH);
        put("minecraft:creeper", Aspects.FIRE);
        put("minecraft:horse", Aspects.BEAST);
        put("minecraft:pig", Aspects.BEAST);
        put("minecraft:sheep", Aspects.CLOTH);
        put("minecraft:cow", Aspects.BEAST);
        put("minecraft:mooshroom", Aspects.PLANT);
        put("minecraft:ocelot", Aspects.BEAST);
        put("minecraft:chicken", Aspects.FLIGHT);
        put("minecraft:squid", Aspects.SENSES);
        put("minecraft:wolf", Aspects.BEAST);
        put("minecraft:bat", Aspects.FLIGHT);
        put("minecraft:spider", Aspects.CLOTH);
        put("minecraft:slime", Aspects.SLIME);
        put("minecraft:ghast", ForbiddenAspects.ASPECTS.get("infernus"));
        put("minecraft:zombified_piglin", Aspects.GREED);
        put("minecraft:enderman", Aspects.ELDRITCH);
        put("minecraft:cave_spider", Aspects.POISON);
        put("minecraft:silverfish", Aspects.BEAST);
        put("minecraft:blaze", Aspects.FIRE);
        put("minecraft:magma_cube", Aspects.FIRE);
        put("minecraft:witch", Aspects.MAGIC);
        put("minecraft:villager", Aspects.GREED);
        put("thaumcraft:firebat", Aspects.FIRE);
        put("thaumcraft:wisp", Aspects.AURA);
        put("thaumcraft:thaumic_slime", Aspects.TAINT);
        put("thaumcraft:brainy_zombie", Aspects.MIND);
        put("thaumcraft:giant_brainy_zombie", Aspects.MIND);
        put("thaumcraft:taint_spider", Aspects.TAINT);
        put("thaumcraft:taint_swarm", Aspects.TAINT);
        put("thaumcraft:tainted_pig", Aspects.TAINT);
        put("thaumcraft:tainted_sheep", Aspects.TAINT);
        put("thaumcraft:tainted_cow", Aspects.TAINT);
        put("thaumcraft:tainted_chicken", Aspects.TAINT);
        put("thaumcraft:tainted_villager", Aspects.TAINT);
        put("thaumcraft:cultist_knight", Aspects.ELDRITCH);
        put("thaumcraft:cultist_cleric", Aspects.ELDRITCH);
        put("thaumcraft:eldritch_crab", Aspects.ELDRITCH);
        put("thaumcraft:inhabited_zombie", Aspects.ELDRITCH);
        put("thaumcraft:pech", Aspects.GREED);
        put("thaumcraft:eldritch_guardian", Aspects.ELDRITCH);
    }

    private static void put(String id, @Nullable Aspect aspect) {
        if (aspect != null) ASPECTS.put(Identifier.parse(id), aspect);
    }

    /** De que essência este bicho é feito. */
    public static Aspect aspectOf(@Nullable Identifier mob) {
        if (mob == null) return DEFAULT;
        return ASPECTS.getOrDefault(mob, DEFAULT);
    }

    /** Se a gaiola sabe fazer este bicho — é o que o cristal precisa para se marcar. */
    public static boolean known(EntityType<?> type) {
        return ASPECTS.containsKey(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static int size() {
        return ASPECTS.size();
    }
}
