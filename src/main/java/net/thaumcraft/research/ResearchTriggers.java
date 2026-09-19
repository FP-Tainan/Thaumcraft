package net.thaumcraft.research;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * O que desperta cada pesquisa escondida ou perdida: os {@code setItemTriggers}, {@code setEntityTriggers} e
 * {@code setAspectTriggers} do {@code ConfigResearch} da 4.2.3.5. <strong>Gerada</strong> pelo {@code scratchpad/gatilhos.js};
 * não editar à mão.
 */
public final class ResearchTriggers {
    public record Triggers(List<Predicate<ItemStack>> items, List<String> entities, List<Aspect> aspects) {
    }

    private static final Map<String, Triggers> ALL = new HashMap<>();

    private ResearchTriggers() {
    }

    public static Triggers of(String key) {
        if (ALL.isEmpty()) fill();
        return ALL.get(key);
    }

    private static void put(String key, List<Predicate<ItemStack>> items, List<String> entities, List<Aspect> aspects) {
        ALL.put(key, new Triggers(items, entities, aspects));
    }

    private static void fill() {
        put("FOCUSHELLBAT", List.of(), List.of("thaumcraft:firebat"), List.of(Aspects.FIRE));
        put("BANNERS", List.of(s -> s.is(TCItems.BANNER)), List.of(), List.of());
        put("HOVERGIRDLE", List.of(), List.of(), List.of(Aspects.FLIGHT));
        // ainda sem par por aqui: new ItemStack(Blocks.field_150427_aO, 1, 32767), new ItemStack(Blocks.field_150384_bq, 1, 32767)
        put("MIRROR", List.of(s -> s.is(net.minecraft.world.item.Items.ENDER_PEARL), s -> s.is(net.minecraft.world.item.Items.END_PORTAL_FRAME)), List.of("minecraft:enderman"), List.of());
        put("LAMPGROWTH", List.of(), List.of(), List.of(Aspects.LIGHT, Aspects.CROP));
        put("LAMPFERTILITY", List.of(), List.of(), List.of(Aspects.LIGHT, Aspects.LIFE));
        put("BONEBOW", List.of(s -> s.is(net.minecraft.world.item.Items.BOW), s -> s.is(net.minecraft.world.item.Items.BONE)), List.of(), List.of());
        put("JARBRAIN", List.of(s -> s.is(TCResources.get("quicksilver"))), List.of("thaumcraft:brainy_zombie", "thaumcraft:giant_brainy_zombie"), List.of());
        put("ARMORFORTRESS", List.of(), List.of(), List.of(Aspects.ARMOR));
        put("LIQUIDDEATH", List.of(), List.of(), List.of(Aspects.DEATH, Aspects.POISON));
        put("BOTTLETAINT", List.of(), List.of(), List.of(Aspects.TAINT));
        put("THAUMIUM", List.of(), List.of(), List.of(Aspects.METAL));
        put("ETHEREALBLOOM", List.of(), List.of(), List.of(Aspects.TAINT));
        put("TINYHAT", List.of(s -> s.is(net.minecraft.tags.ItemTags.WOOL)), List.of(), List.of(Aspects.CLOTH));
        put("TINYGLASSES", List.of(s -> s.is(net.minecraft.tags.ItemTags.WOOL)), List.of(), List.of(Aspects.CLOTH));
        put("TINYBOWTIE", List.of(s -> s.is(net.minecraft.tags.ItemTags.WOOL)), List.of(), List.of(Aspects.CLOTH));
        put("TINYFEZ", List.of(s -> s.is(net.minecraft.tags.ItemTags.WOOL)), List.of(), List.of(Aspects.CLOTH));
        put("TINYDART", List.of(), List.of(), List.of(Aspects.WEAPON));
        put("TINYVISOR", List.of(), List.of(), List.of(Aspects.ARMOR));
        put("TINYARMOR", List.of(), List.of(), List.of(Aspects.ARMOR));
        put("TINYHAMMER", List.of(), List.of(), List.of(Aspects.WEAPON));
        put("OUTERREV", List.of(s -> s.is(TCItems.GLYPHED_STONE), s -> s.is(TCItems.RUNED_STONE)), List.of(), List.of());
        put("PRIMPEARL", List.of(s -> s.is(TCItems.PRIMORDIAL_PEARL)), List.of(), List.of());
        put("ROD_primal_staff", List.of(s -> s.is(TCItems.FOCI.get("primal"))), List.of("thaumcraft:primal_orb"), List.of());
    }
}
