package net.thaumcraft.mortuorum;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * As coisas do Ars Mortuorum — o Necromancy 1.7.10, de sirolf2009.
 *
 * <p>Como os outros ramos, ele mora no mesmo jar do Thaumcraft, com as figuras e os textos no espaço de nome
 * {@code thaumcraft} e aba própria no criativo. Os números de item do original — que lá eram um item só com
 * muitos valores — viram um item por coisa, que é como o jogo de hoje faz.
 */
public final class MortuorumItems {
    /** A ordem em que as coisas entram na aba do criativo. */
    private static final List<Item> ORDER = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.id("mortuorum"));

    /** Os quatro avulsos do {@code ItemGeneric}. */
    public static final Item BONE_NEEDLE = register("bone_needle", properties -> new Item(properties));
    public static final Item SOUL_IN_A_JAR = register("soul_in_a_jar", properties -> new Item(properties));
    public static final Item JAR_OF_BLOOD = register("jar_of_blood", properties -> new Item(properties));
    public static final Item BRAIN_ON_A_STICK = register("brain_on_a_stick", properties -> new Item(properties));

    /**
     * Os cinco órgãos do {@code ItemOrgans}: comem-se, enchem dois de fome com pouca saturação e dão meio
     * minuto de fome a quem os come, como no original.
     */
    public static final Map<String, Item> ORGANS = new LinkedHashMap<>();

    static {
        for (String nome : List.of("brains", "heart", "muscle", "lungs", "skin")) {
            ORGANS.put(nome, register(nome, properties -> new Item(properties.food(
                    new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build(),
                    net.minecraft.world.item.component.Consumables.defaultFood().onConsume(
                            new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(
                                    new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8f)).build()))));
        }
    }

    /** As peças de corpo do {@code ItemBodyPart}: de que bicho e que pedaço. */
    public record Part(String mob, String piece) {
    }

    /** Cada peça, pelo nome do item. */
    public static final Map<String, Part> PARTS = new LinkedHashMap<>();
    public static final Map<String, Item> PART_ITEMS = new LinkedHashMap<>();

    static {
        parts("Cow", "Torso", "Head", "Arm", "Legs");
        parts("Creeper", "Torso", "Legs");
        parts("Enderman", "Head", "Torso", "Arm", "Legs");
        parts("Pig", "Head", "Torso", "Arm", "Legs");
        parts("Pigzombie", "Head", "Torso", "Arm", "Legs");
        parts("Skeleton", "Torso", "Arm", "Legs");
        parts("Spider", "Head", "Torso", "Legs");
        parts("Zombie", "Torso", "Arm", "Legs");
        parts("Chicken", "Head", "Torso", "Arm", "Legs");
        parts("Villager", "Head", "Torso", "Arm", "Legs");
        parts("Witch", "Head", "Torso", "Arm", "Legs");
        parts("Squid", "Head", "Torso", "Legs");
        parts("CaveSpider", "Head", "Torso", "Legs");
        parts("Sheep", "Head", "Torso", "Arm", "Legs");
        parts("IronGolem", "Head", "Torso", "Arm", "Legs");
        parts("Wolf", "Head");
    }

    /** E o item da Máquina de Costura. */
    public static final Item SEWING_MACHINE = register("sewing_machine", properties ->
            new net.minecraft.world.item.BlockItem(MortuorumBlocks.SEWING_MACHINE,
                    properties.useBlockDescriptionPrefix()));

    /** O balde de sangue. */
    public static final Item BUCKET_BLOOD = register("bucket_blood", properties ->
            new net.minecraft.world.item.BucketItem(MortuorumFluids.BLOOD, properties
                    .craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)));

    /** E o item do Altar de Invocação. */
    public static final Item SUMMONING_ALTAR = register("summoning_altar", properties ->
            new net.minecraft.world.item.BlockItem(MortuorumBlocks.SUMMONING_ALTAR,
                    properties.useBlockDescriptionPrefix()));

    private MortuorumItems() {
    }

    /** Um bicho e os pedaços que se tiram dele. */
    private static void parts(String mob, String... pieces) {
        for (String piece : pieces) {
            String name = mob.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)
                    + "_" + piece.toLowerCase(java.util.Locale.ROOT);
            PARTS.put(name, new Part(mob, piece));
            PART_ITEMS.put(name, register(name, properties -> new Item(properties)));
        }
    }

    public static int count() {
        return ORDER.size();
    }

    /** O que a aba do ramo mostra, na ordem. */
    public static List<Item> shown() {
        return List.copyOf(ORDER);
    }

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        Identifier id = Thaumcraft.id(name);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ORDER.add(item);
        return item;
    }

    /** A aba do criativo do ramo, à parte da do Thaumcraft. */
    public static void init() {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.thaumcraft.mortuorum"))
                .icon(() -> new ItemStack(BONE_NEEDLE))
                .displayItems((parameters, output) -> ORDER.forEach(output::accept))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, tab);
    }
}
