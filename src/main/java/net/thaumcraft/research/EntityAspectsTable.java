package net.thaumcraft.research;

import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

/**
 * Os aspectos das criaturas: o {@code registerEntityAspects} do {@code ConfigAspects} da 4.2.3.5, na ordem do original.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/entidades-aspectos.js} a partir do jar descompilado. Os nomes
 * de então viraram ids de hoje; as condições de NBT ({@code powered}, {@code PechType}, {@code Type}) são conferidas
 * pelo {@link EntityAspects}. A criatura que não está aqui não se examina (como no original).
 */
final class EntityAspectsTable {
    private EntityAspectsTable() {
    }

    static void register(EntityAspects.Registrar r) {
        r.entity("minecraft:zombie", null, null, new AspectList().add(Aspects.UNDEAD, 2).add(Aspects.MAN, 1).add(Aspects.EARTH, 1));
        r.entity("minecraft:giant", null, null, new AspectList().add(Aspects.UNDEAD, 4).add(Aspects.MAN, 3).add(Aspects.EARTH, 3));
        r.entity("minecraft:skeleton", null, null, new AspectList().add(Aspects.UNDEAD, 3).add(Aspects.MAN, 1).add(Aspects.EARTH, 1));
        r.entity("minecraft:wither_skeleton", null, null, new AspectList().add(Aspects.UNDEAD, 4).add(Aspects.MAN, 1).add(Aspects.FIRE, 2));
        r.entity("minecraft:creeper", null, null, new AspectList().add(Aspects.PLANT, 2).add(Aspects.FIRE, 2));
        r.entity("minecraft:creeper", "powered", "1", new AspectList().add(Aspects.PLANT, 3).add(Aspects.FIRE, 3).add(Aspects.ENERGY, 3));
        r.entity("minecraft:horse", null, null, new AspectList().add(Aspects.BEAST, 4).add(Aspects.EARTH, 1).add(Aspects.AIR, 1));
        r.entity("minecraft:donkey", null, null, new AspectList().add(Aspects.BEAST, 4).add(Aspects.EARTH, 1).add(Aspects.AIR, 1));
        r.entity("minecraft:mule", null, null, new AspectList().add(Aspects.BEAST, 4).add(Aspects.EARTH, 1).add(Aspects.AIR, 1));
        r.entity("minecraft:skeleton_horse", null, null, new AspectList().add(Aspects.BEAST, 4).add(Aspects.EARTH, 1).add(Aspects.AIR, 1));
        r.entity("minecraft:zombie_horse", null, null, new AspectList().add(Aspects.BEAST, 4).add(Aspects.EARTH, 1).add(Aspects.AIR, 1));
        r.entity("minecraft:pig", null, null, new AspectList().add(Aspects.BEAST, 2).add(Aspects.EARTH, 2));
        r.entity("minecraft:experience_orb", null, null, new AspectList().add(Aspects.MIND, 5));
        r.entity("minecraft:sheep", null, null, new AspectList().add(Aspects.BEAST, 2).add(Aspects.EARTH, 2));
        r.entity("minecraft:cow", null, null, new AspectList().add(Aspects.BEAST, 3).add(Aspects.EARTH, 3));
        r.entity("minecraft:mooshroom", null, null, new AspectList().add(Aspects.BEAST, 3).add(Aspects.PLANT, 1).add(Aspects.EARTH, 2));
        r.entity("minecraft:snow_golem", null, null, new AspectList().add(Aspects.COLD, 3).add(Aspects.WATER, 1));
        r.entity("minecraft:ocelot", null, null, new AspectList().add(Aspects.BEAST, 3).add(Aspects.ENTROPY, 3));
        r.entity("minecraft:cat", null, null, new AspectList().add(Aspects.BEAST, 3).add(Aspects.ENTROPY, 3));
        r.entity("minecraft:chicken", null, null, new AspectList().add(Aspects.BEAST, 2).add(Aspects.FLIGHT, 2).add(Aspects.AIR, 1));
        r.entity("minecraft:squid", null, null, new AspectList().add(Aspects.BEAST, 2).add(Aspects.WATER, 2));
        r.entity("minecraft:wolf", null, null, new AspectList().add(Aspects.BEAST, 3).add(Aspects.EARTH, 3));
        r.entity("minecraft:bat", null, null, new AspectList().add(Aspects.BEAST, 1).add(Aspects.FLIGHT, 1).add(Aspects.AIR, 1));
        r.entity("#boat", null, null, new AspectList().add(Aspects.MECHANISM, 2).add(Aspects.WATER, 2));
        r.entity("minecraft:spider", null, null, new AspectList().add(Aspects.BEAST, 3).add(Aspects.ENTROPY, 2));
        r.entity("minecraft:slime", null, null, new AspectList().add(Aspects.SLIME, 2).add(Aspects.WATER, 2));
        r.entity("minecraft:ghast", null, null, new AspectList().add(Aspects.UNDEAD, 3).add(Aspects.FIRE, 2));
        r.entity("minecraft:zombified_piglin", null, null, new AspectList().add(Aspects.UNDEAD, 4).add(Aspects.FIRE, 2));
        r.entity("minecraft:enderman", null, null, new AspectList().add(Aspects.ELDRITCH, 4).add(Aspects.TRAVEL, 2).add(Aspects.AIR, 2));
        r.entity("minecraft:cave_spider", null, null, new AspectList().add(Aspects.BEAST, 2).add(Aspects.POISON, 2).add(Aspects.EARTH, 1));
        r.entity("minecraft:silverfish", null, null, new AspectList().add(Aspects.BEAST, 1).add(Aspects.EARTH, 1));
        r.entity("minecraft:blaze", null, null, new AspectList().add(Aspects.ELDRITCH, 4).add(Aspects.FIRE, 1));
        r.entity("minecraft:magma_cube", null, null, new AspectList().add(Aspects.SLIME, 3).add(Aspects.FIRE, 2));
        r.entity("minecraft:ender_dragon", null, null, new AspectList().add(Aspects.ELDRITCH, 20).add(Aspects.BEAST, 20).add(Aspects.ENTROPY, 20));
        r.entity("minecraft:wither", null, null, new AspectList().add(Aspects.UNDEAD, 20).add(Aspects.ENTROPY, 20).add(Aspects.FIRE, 15));
        r.entity("minecraft:witch", null, null, new AspectList().add(Aspects.MAN, 3).add(Aspects.MAGIC, 2).add(Aspects.FIRE, 1));
        r.entity("minecraft:villager", null, null, new AspectList().add(Aspects.MAN, 3).add(Aspects.AIR, 2));
        r.entity("minecraft:iron_golem", null, null, new AspectList().add(Aspects.METAL, 4).add(Aspects.EARTH, 3));
        r.entity("minecraft:minecart", null, null, new AspectList().add(Aspects.MECHANISM, 3).add(Aspects.AIR, 2));
        r.entity("minecraft:chest_minecart", null, null, new AspectList().add(Aspects.MECHANISM, 3).add(Aspects.AIR, 1).add(Aspects.VOID, 1));
        r.entity("minecraft:furnace_minecart", null, null, new AspectList().add(Aspects.MECHANISM, 3).add(Aspects.AIR, 1).add(Aspects.FIRE, 1));
        r.entity("minecraft:tnt_minecart", null, null, new AspectList().add(Aspects.MECHANISM, 3).add(Aspects.AIR, 1).add(Aspects.FIRE, 1));
        r.entity("minecraft:hopper_minecart", null, null, new AspectList().add(Aspects.MECHANISM, 3).add(Aspects.AIR, 1).add(Aspects.EXCHANGE, 1));
        r.entity("minecraft:spawner_minecart", null, null, new AspectList().add(Aspects.MECHANISM, 3).add(Aspects.AIR, 1).add(Aspects.MAGIC, 1));
        r.entity("minecraft:end_crystal", null, null, new AspectList().add(Aspects.ELDRITCH, 3).add(Aspects.MAGIC, 3).add(Aspects.HEAL, 3));
        r.entity("minecraft:item_frame", null, null, new AspectList().add(Aspects.SENSES, 3).add(Aspects.CLOTH, 1));
        r.entity("minecraft:painting", null, null, new AspectList().add(Aspects.SENSES, 5).add(Aspects.CLOTH, 3));
        r.entity("thaumcraft:primal_orb", null, null, new AspectList().add(Aspects.AIR, 5).add(Aspects.ENTROPY, 10).add(Aspects.MAGIC, 10).add(Aspects.ENERGY, 10));
        r.entity("thaumcraft:firebat", null, null, new AspectList().add(Aspects.BEAST, 2).add(Aspects.FLIGHT, 1).add(Aspects.FIRE, 2));
        r.entity("thaumcraft:pech", "PechType", "0", new AspectList().add(Aspects.MAN, 2).add(Aspects.MAGIC, 2).add(Aspects.EXCHANGE, 2).add(Aspects.GREED, 2));
        r.entity("thaumcraft:pech", "PechType", "1", new AspectList().add(Aspects.MAN, 2).add(Aspects.MAGIC, 2).add(Aspects.EXCHANGE, 2).add(Aspects.WEAPON, 2));
        r.entity("thaumcraft:pech", "PechType", "2", new AspectList().add(Aspects.MAN, 2).add(Aspects.MAGIC, 4).add(Aspects.EXCHANGE, 2));
        r.entity("thaumcraft:thaumic_slime", null, null, new AspectList().add(Aspects.SLIME, 2).add(Aspects.MAGIC, 1).add(Aspects.WATER, 1));
        r.entity("thaumcraft:brainy_zombie", null, null, new AspectList().add(Aspects.UNDEAD, 3).add(Aspects.MAN, 1).add(Aspects.MIND, 1).add(Aspects.EARTH, 1));
        r.entity("thaumcraft:giant_brainy_zombie", null, null, new AspectList().add(Aspects.UNDEAD, 4).add(Aspects.MAN, 2).add(Aspects.MIND, 1).add(Aspects.EARTH, 2));
        r.entity("thaumcraft:taintacle", null, null, new AspectList().add(Aspects.TAINT, 3).add(Aspects.WATER, 2));
        r.entity("thaumcraft:taintacle_small", null, null, new AspectList().add(Aspects.TAINT, 1).add(Aspects.WATER, 1));
        r.entity("thaumcraft:taint_spider", null, null, new AspectList().add(Aspects.TAINT, 1).add(Aspects.EARTH, 1));
        r.entity("thaumcraft:taint_spore", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.AIR, 2));
        r.entity("thaumcraft:taint_spore_swarmer", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.AIR, 2));
        r.entity("thaumcraft:taint_swarm", null, null, new AspectList().add(Aspects.TAINT, 3).add(Aspects.AIR, 3));
        r.entity("thaumcraft:taint_pig", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.EARTH, 2));
        r.entity("thaumcraft:taint_sheep", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.EARTH, 2));
        r.entity("thaumcraft:taint_cow", null, null, new AspectList().add(Aspects.TAINT, 3).add(Aspects.EARTH, 3));
        r.entity("thaumcraft:taint_chicken", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.FLIGHT, 2).add(Aspects.AIR, 1));
        r.entity("thaumcraft:taint_villager", null, null, new AspectList().add(Aspects.TAINT, 3).add(Aspects.AIR, 2));
        r.entity("thaumcraft:taint_creeper", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.FIRE, 2));
        r.entity("thaumcraft:mind_spider", null, null, new AspectList().add(Aspects.TAINT, 2).add(Aspects.FIRE, 2));
        r.entity("thaumcraft:eldritch_guardian", null, null, new AspectList().add(Aspects.ELDRITCH, 4).add(Aspects.DEATH, 2).add(Aspects.UNDEAD, 4));
        r.entity("thaumcraft:eldritch_orb", null, null, new AspectList().add(Aspects.ELDRITCH, 2).add(Aspects.DEATH, 2));
        r.entity("thaumcraft:cultist_knight", null, null, new AspectList().add(Aspects.ELDRITCH, 1).add(Aspects.MAN, 2).add(Aspects.ENTROPY, 1));
        r.entity("thaumcraft:cultist_cleric", null, null, new AspectList().add(Aspects.ELDRITCH, 1).add(Aspects.MAN, 2).add(Aspects.ENTROPY, 1));
        for (Aspect aspect : Aspects.all()) r.entity("thaumcraft:wisp", "Type", aspect.tag(), new AspectList().add(aspect, 2).add(Aspects.MAGIC, 1).add(Aspects.AIR, 1));
        r.entity("thaumcraft:golem", null, null, new AspectList().add(Aspects.AIR, 2).add(Aspects.EARTH, 2).add(Aspects.MAGIC, 2));
    }
}
