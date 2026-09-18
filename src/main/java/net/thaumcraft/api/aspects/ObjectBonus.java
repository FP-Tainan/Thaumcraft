package net.thaumcraft.api.aspects;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.thaumcraft.item.JarContents;
import net.thaumcraft.item.PhialItem;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;

import java.util.Map;

/**
 * O que a coisa ganha por cima do que a tabela diz: o fim do {@code getObjectTags} (a varinha e a poção) e o
 * {@code getBonusTags} do {@code ThaumcraftCraftingManager} da 4.2.3.5.
 */
final class ObjectBonus {
    private ObjectBonus() {
    }

    /** Os aspectos de cada encantamento, pelos números do original. */
    private static final Map<ResourceKey<Enchantment>, Aspect> ENCHANTMENTS = Map.ofEntries(
            Map.entry(Enchantments.AQUA_AFFINITY, Aspects.WATER),
            Map.entry(Enchantments.BANE_OF_ARTHROPODS, Aspects.BEAST),
            Map.entry(Enchantments.BLAST_PROTECTION, Aspects.ARMOR),
            Map.entry(Enchantments.EFFICIENCY, Aspects.TOOL),
            Map.entry(Enchantments.FEATHER_FALLING, Aspects.FLIGHT),
            Map.entry(Enchantments.FIRE_ASPECT, Aspects.FIRE),
            Map.entry(Enchantments.FIRE_PROTECTION, Aspects.ARMOR),
            Map.entry(Enchantments.FLAME, Aspects.FIRE),
            Map.entry(Enchantments.FORTUNE, Aspects.GREED),
            Map.entry(Enchantments.INFINITY, Aspects.CRAFT),
            Map.entry(Enchantments.KNOCKBACK, Aspects.AIR),
            Map.entry(Enchantments.LOOTING, Aspects.GREED),
            Map.entry(Enchantments.POWER, Aspects.WEAPON),
            Map.entry(Enchantments.PROJECTILE_PROTECTION, Aspects.ARMOR),
            Map.entry(Enchantments.PROTECTION, Aspects.ARMOR),
            Map.entry(Enchantments.PUNCH, Aspects.AIR),
            Map.entry(Enchantments.RESPIRATION, Aspects.AIR),
            Map.entry(Enchantments.SHARPNESS, Aspects.WEAPON),
            Map.entry(Enchantments.SILK_TOUCH, Aspects.EXCHANGE),
            Map.entry(Enchantments.THORNS, Aspects.WEAPON),
            Map.entry(Enchantments.SMITE, Aspects.ENTROPY),
            Map.entry(Enchantments.UNBREAKING, Aspects.EARTH),
            Map.entry(Enchantments.LUCK_OF_THE_SEA, Aspects.GREED),
            Map.entry(Enchantments.LURE, Aspects.BEAST));

    /** Os efeitos de poção e o aspecto que cada um dá, vezes três por nível. */
    private static final Map<Holder<MobEffect>, Aspect> EFFECTS = Map.ofEntries(
            Map.entry(MobEffects.BLINDNESS, Aspects.DARKNESS),
            Map.entry(MobEffects.NAUSEA, Aspects.ELDRITCH),
            Map.entry(MobEffects.STRENGTH, Aspects.WEAPON),
            Map.entry(MobEffects.MINING_FATIGUE, Aspects.TRAP),
            Map.entry(MobEffects.HASTE, Aspects.TOOL),
            Map.entry(MobEffects.INSTANT_DAMAGE, Aspects.DEATH),
            Map.entry(MobEffects.INSTANT_HEALTH, Aspects.HEAL),
            Map.entry(MobEffects.HUNGER, Aspects.DEATH),
            Map.entry(MobEffects.INVISIBILITY, Aspects.SENSES),
            Map.entry(MobEffects.JUMP_BOOST, Aspects.FLIGHT),
            Map.entry(MobEffects.SLOWNESS, Aspects.TRAP),
            Map.entry(MobEffects.SPEED, Aspects.MOTION),
            Map.entry(MobEffects.NIGHT_VISION, Aspects.SENSES),
            Map.entry(MobEffects.POISON, Aspects.POISON),
            Map.entry(MobEffects.REGENERATION, Aspects.HEAL),
            Map.entry(MobEffects.RESISTANCE, Aspects.ARMOR),
            Map.entry(MobEffects.WATER_BREATHING, Aspects.AIR),
            Map.entry(MobEffects.WITHER, Aspects.DEATH));

    /** O fim do {@code getObjectTags}: a varinha vale pelas peças, e a poção pelos efeitos. */
    static void objectExtras(ItemStack stack, AspectList tags) {
        if (stack.getItem() instanceof WandItem) {
            int cost = WandItem.rod(stack).craftCost() + WandItem.cap(stack).craftCost();
            tags.merge(Aspects.MAGIC, cost / 2);
            tags.merge(Aspects.TOOL, cost / 3);
        }
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            tags.merge(Aspects.WATER, 1);
            if (!stack.is(Items.POTION)) tags.merge(Aspects.ENTROPY, 2);
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            for (MobEffectInstance effect : contents.getAllEffects()) {
                int level = effect.getAmplifier() + 1;
                tags.merge(Aspects.MAGIC, level * 2);
                if (effect.getEffect().equals(MobEffects.FIRE_RESISTANCE)) {
                    tags.merge(Aspects.ARMOR, level);
                    tags.merge(Aspects.FIRE, level * 2);
                    continue;
                }
                for (var entry : EFFECTS.entrySet()) {
                    if (effect.getEffect().equals(entry.getKey())) tags.merge(entry.getValue(), level * 3);
                }
            }
        }
    }

    /** O {@code getBonusTags}: a essência que a coisa carrega, a serventia dela e os encantamentos. */
    static AspectList apply(ItemStack stack, AspectList source) {
        AspectList tmp = new AspectList();
        // o que a coisa carrega: o frasco cheio e o jarro com essência
        Aspect phial = PhialItem.aspectOf(stack);
        if (phial != null) tmp.add(phial, PhialItem.PORTION);
        JarContents jar = stack.get(TCComponents.JAR_CONTENTS);
        if (jar != null && jar.heldAspect() != null && jar.amount() > 0) tmp.add(jar.heldAspect(), jar.amount());
        if (source != null) tmp.add(source);

        Item item = stack.getItem();
        if (stack.has(DataComponents.EQUIPPABLE) && armor(stack) > 0) {
            tmp.merge(Aspects.ARMOR, armor(stack));
        } else if (stack.is(ItemTags.SWORDS)) {
            // o dano da espada do original é o do material, sem os quatro do punho
            double bonus = attackDamage(stack) - 3.0;
            if (bonus + 1.0 > 0.0) tmp.merge(Aspects.WEAPON, (int) (bonus + 1.0));
        } else if (item == Items.BOW) {
            tmp.merge(Aspects.WEAPON, 3).merge(Aspects.FLIGHT, 1);
        } else if (stack.is(ItemTags.PICKAXES)) {
            tmp.merge(Aspects.MINE, harvestLevel(stack) + 1);
        } else if (stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS)) {
            tmp.merge(Aspects.TOOL, harvestLevel(stack) + 1);
        } else if (item == Items.SHEARS || stack.is(ItemTags.HOES)) {
            int uses = stack.getMaxDamage();
            if (uses <= 59) {
                tmp.merge(Aspects.HARVEST, 1);
            } else if (uses <= 131 || uses <= 32) {
                tmp.merge(Aspects.HARVEST, 2);
            } else if (uses <= 250) {
                tmp.merge(Aspects.HARVEST, 3);
            } else {
                tmp.merge(Aspects.HARVEST, 4);
            }
        }

        ItemEnchantments enchantments = stack.is(Items.ENCHANTED_BOOK)
                ? stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                : stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int levels = 0;
        for (var entry : enchantments.entrySet()) {
            int level = entry.getIntValue();
            for (var known : ENCHANTMENTS.entrySet()) {
                if (entry.getKey().is(known.getKey())) tmp.merge(known.getValue(), level);
            }
            levels += level;
        }
        if (levels > 0) tmp.merge(Aspects.MAGIC, levels);
        return cull(tmp);
    }

    private static int armor(ItemStack stack) {
        double total = 0.0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ARMOR)) total += entry.modifier().amount();
        }
        return (int) total;
    }

    private static double attackDamage(ItemStack stack) {
        double total = 0.0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) total += entry.modifier().amount();
        }
        return total;
    }

    /**
     * O nível de colheita do material, como o {@code ToolMaterial} do 1.7.10 dava: madeira e ouro zero, pedra
     * um, ferro dois, diamante três; o táumio três e o metal do vazio quatro, como no mod; a netherita, que não
     * existia, fica com quatro.
     */
    private static int harvestLevel(ItemStack stack) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.startsWith("wooden_") || path.startsWith("golden_")) return 0;
        if (path.startsWith("stone_")) return 1;
        if (path.startsWith("iron_")) return 2;
        if (path.startsWith("diamond_") || path.startsWith("thaumium_")) return 3;
        if (path.startsWith("netherite_") || path.startsWith("void_")) return 4;
        return 0;
    }

    /** O {@code cullTags}: com mais de seis aspectos, sai o de menor peso, até sobrarem seis. */
    private static AspectList cull(AspectList source) {
        AspectList out = source.copy();
        while (out.size() > 6) {
            Aspect lowest = null;
            float low = 32767.0f;
            for (Aspect aspect : out.getAspects()) {
                float weight = out.getAmount(aspect);
                if (aspect.isPrimal()) {
                    weight *= 0.9f;
                } else {
                    Aspect[] parts = aspect.components();
                    if (!parts[0].isPrimal()) {
                        weight *= 1.1f;
                        if (!parts[0].components()[0].isPrimal()) weight *= 1.05f;
                        if (!parts[0].components()[1].isPrimal()) weight *= 1.05f;
                    }
                    if (!parts[1].isPrimal()) {
                        weight *= 1.1f;
                        if (!parts[1].components()[0].isPrimal()) weight *= 1.05f;
                        if (!parts[1].components()[1].isPrimal()) weight *= 1.05f;
                    }
                }
                if (weight < low) {
                    low = weight;
                    lowest = aspect;
                }
            }
            out.remove(lowest);
        }
        return out;
    }
}
