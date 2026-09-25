package net.thaumcraft.forbidden;

import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Os sete aspectos sombrios do Forbidden Magic 0.575 — os pecados e o Nether — e o que eles somam às coisas e
 * às criaturas que já existiam.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/fm-aspectos.js} a partir do {@code DarkAspects} do jar — não
 * se escreve à mão.
 */
public final class ForbiddenAspects {
    /** Os sete, pelo nome em latim. */
    public static final Map<String, Aspect> ASPECTS = new LinkedHashMap<>();

    private ForbiddenAspects() {
    }

    /** Cria os aspectos. Roda antes de tudo, porque as pesquisas e as receitas do ramo os citam. */
    public static void init() {
        ASPECTS.put("luxuria", ThaumcraftApi.aspect("luxuria", 0xFFC1CE, Aspects.FLESH, Aspects.HUNGER, 1));
        ASPECTS.put("infernus", ThaumcraftApi.aspect("infernus", 0xFF0000, Aspects.FIRE, Aspects.MAGIC, 771));
        ASPECTS.put("superbia", ThaumcraftApi.aspect("superbia", 0x9639FF, Aspects.FLIGHT, Aspects.VOID, 1));
        ASPECTS.put("gula", ThaumcraftApi.aspect("gula", 0xD59C46, Aspects.HUNGER, Aspects.VOID, 1));
        ASPECTS.put("invidia", ThaumcraftApi.aspect("invidia", 0x00BA00, Aspects.SENSES, Aspects.HUNGER, 1));
        ASPECTS.put("desidia", ThaumcraftApi.aspect("desidia", 0x6E6E6E, Aspects.TRAP, Aspects.SOUL, 771));
        ASPECTS.put("ira", ThaumcraftApi.aspect("ira", 0x870404, Aspects.WEAPON, Aspects.FIRE, 771));

        // o que eles somam ao que as coisas já tinham
        ThaumcraftApi.aspects(r -> {
            r.add("minecraft:netherrack", new AspectList().add(ASPECTS.get("infernus"), 1));
            r.blockAdd("minecraft:nether_portal", new AspectList().add(ASPECTS.get("infernus"), 4));
            r.add("minecraft:nether_star", new AspectList().add(ASPECTS.get("infernus"), 8).add(ASPECTS.get("superbia"), 8));
            r.add("minecraft:nether_wart", new AspectList().add(ASPECTS.get("infernus"), 1));
            r.add("minecraft:nether_brick", new AspectList().add(ASPECTS.get("infernus"), 1));
            r.add("minecraft:nether_bricks", new AspectList().add(ASPECTS.get("infernus"), 2));
            r.add("minecraft:ghast_tear", new AspectList().add(ASPECTS.get("ira"), 4));
            r.add("minecraft:wither_skeleton_skull", new AspectList().add(ASPECTS.get("infernus"), 4));
            r.add("minecraft:creeper_head", new AspectList().add(ASPECTS.get("ira"), 2));
            r.add("minecraft:tnt", new AspectList().add(ASPECTS.get("ira"), 2));
            r.add("minecraft:golden_helmet", new AspectList().add(ASPECTS.get("superbia"), 2));
            r.add("minecraft:golden_chestplate", new AspectList().add(ASPECTS.get("superbia"), 1));
            r.add("minecraft:golden_leggings", new AspectList().add(ASPECTS.get("superbia"), 1));
            r.add("minecraft:golden_boots", new AspectList().add(ASPECTS.get("superbia"), 1));
            r.add("minecraft:golden_sword", new AspectList().add(ASPECTS.get("superbia"), 2));
            r.add("minecraft:cake", new AspectList().add(ASPECTS.get("gula"), 7));
            r.add("minecraft:cookie", new AspectList().add(ASPECTS.get("gula"), 1));
            r.add("minecraft:fire_charge", new AspectList().add(ASPECTS.get("ira"), 1));
            r.add("minecraft:saddle", new AspectList().add(ASPECTS.get("luxuria"), 2));
            r.tag("c:ores/quartz", new AspectList().add(Aspects.CRYSTAL, 3).add(ASPECTS.get("infernus"), 2));
            r.tag("minecraft:beds", new AspectList().add(Aspects.CRAFT, 3).add(Aspects.CLOTH, 6).add(ASPECTS.get("desidia"), 4));
            r.item("minecraft:ender_eye", new AspectList().add(Aspects.SENSES, 4).add(Aspects.ELDRITCH, 4).add(Aspects.MAGIC, 3).add(ASPECTS.get("invidia"), 4));
            r.item("minecraft:comparator", new AspectList().add(Aspects.MECHANISM, 2).add(Aspects.ORDER, 2).add(ASPECTS.get("invidia"), 2));
            r.tag("c:foods/cooked_meat", new AspectList().add(Aspects.FLESH, 6).add(Aspects.LIFE, 6).add(Aspects.ENERGY, 6).add(Aspects.BEAST, 4).add(ASPECTS.get("gula"), 6));
            r.item("minecraft:lead", new AspectList().add(Aspects.BEAST, 2).add(Aspects.CLOTH, 2).add(Aspects.SLIME, 1).add(ASPECTS.get("luxuria"), 2));
            r.item("thaumcraft:wrath_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(ASPECTS.get("ira"), 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:envy_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(ASPECTS.get("invidia"), 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:taint_shard", new AspectList().add(Aspects.TAINT, 3).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:pride_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(ASPECTS.get("superbia"), 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:lust_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(ASPECTS.get("luxuria"), 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:gluttony_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(ASPECTS.get("gula"), 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:sloth_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(ASPECTS.get("desidia"), 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:greed_shard", new AspectList().add(ASPECTS.get("infernus"), 1).add(Aspects.GREED, 2).add(Aspects.CRYSTAL, 1));
            r.item("thaumcraft:taint_fruit", new AspectList().add(Aspects.TAINT, 2).add(Aspects.POISON, 1));
            r.item("thaumcraft:taint_coal", new AspectList().add(Aspects.TAINT, 2).add(Aspects.ENTROPY, 2));
            r.item("thaumcraft:taint_leaves", new AspectList().add(Aspects.TAINT, 1).add(Aspects.PLANT, 1));
            r.item("thaumcraft:taint_log", new AspectList().add(Aspects.TAINT, 1).add(Aspects.TREE, 3));
            r.item("thaumcraft:taint_planks", new AspectList().add(Aspects.TREE, 1));
            r.item("thaumcraft:taint_stone_bricks", new AspectList().add(Aspects.EARTH, 1));
        });

        // e às criaturas
        net.thaumcraft.research.EntityAspects.onRegister(r -> {
            r.add("thaumcraft:brainy_zombie", false, new AspectList().add(ASPECTS.get("ira"), 3));
            r.add("thaumcraft:giant_brainy_zombie", false, new AspectList().add(ASPECTS.get("ira"), 4));
            r.add("thaumcraft:firebat", false, new AspectList().add(ASPECTS.get("ira"), 1).add(ASPECTS.get("infernus"), 2));
            r.add("minecraft:wither_skeleton", false, new AspectList().add(ASPECTS.get("infernus"), 3));
            r.add("minecraft:creeper", true, new AspectList().add(ASPECTS.get("ira"), 4));
            r.add("minecraft:creeper", false, new AspectList().add(ASPECTS.get("ira"), 2));
            r.add("minecraft:wither", false, new AspectList().add(ASPECTS.get("infernus"), 7).add(ASPECTS.get("ira"), 7));
            r.add("minecraft:ocelot", false, new AspectList().add(ASPECTS.get("desidia"), 3));
            r.add("minecraft:enderman", false, new AspectList().add(ASPECTS.get("superbia"), 4).add(ASPECTS.get("invidia"), 4));
            r.add("minecraft:ghast", false, new AspectList().add(ASPECTS.get("infernus"), 3).add(ASPECTS.get("ira"), 3));
            r.add("minecraft:zombified_piglin", false, new AspectList().add(ASPECTS.get("ira"), 6));
            r.add("minecraft:ender_dragon", false, new AspectList().add(ASPECTS.get("superbia"), 10));
            r.add("thaumcraft:taintacle", false, new AspectList().add(ASPECTS.get("luxuria"), 3));
            r.add("minecraft:pig", false, new AspectList().add(ASPECTS.get("gula"), 3));
            r.add("minecraft:magma_cube", false, new AspectList().add(ASPECTS.get("infernus"), 1));
        });
    }
}
