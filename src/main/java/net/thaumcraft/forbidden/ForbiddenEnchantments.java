package net.thaumcraft.forbidden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.crafting.SpecialMining;
import net.thaumcraft.registry.TCItems;

import java.util.List;

/**
 * Os oito encantamentos sombrios do Forbidden Magic 0.575 — o {@code DarkEnchantments}.
 *
 * <p>Só a <b>Ira</b> aparece na mesa de encantamento, como no original; as outras sete se põem por livro. O que
 * cada uma faz:
 *
 * <ul>
 * <li><b>Aglomerante</b>: sem Fortuna, o minério quebrado às vezes sai como aglomerado nativo — vinte por cento
 * mais sete e meio por nível;</li>
 * <li><b>Ira</b>: um e um quarto de dano a mais por nível, e cabe também em machado;</li>
 * <li><b>Capitalista</b>: sem Pilhagem, o aldeão que morre larga uma esmeralda, e o monstro, três vezes em trinta
 * e cinco, uma pepita;</li>
 * <li><b>Consumidora</b>: come o lixo que cai — terra, areia, cascalho, pedregulho e pedra do Nether;</li>
 * <li><b>Educativa</b>: sem Pilhagem, quem morre dá três vezes mais experiência por nível;</li>
 * <li><b>Corruptora</b>: uma vez em três, os fragmentos de cristal saem como fragmentos de pecado;</li>
 * <li><b>Tocada pelo Vazio</b>: a ferramenta camaleão se conserta sozinha, um ponto a cada dez tiques, e custa um
 * ponto de distorção a quem a carrega;</li>
 * <li><b>Impacto</b>: a picareta e a pá camaleão quebram três por três de uma vez.</li>
 * </ul>
 */
public final class ForbiddenEnchantments {
    public static final ResourceKey<Enchantment> CLUSTER = key("cluster");
    public static final ResourceKey<Enchantment> WRATH = key("wrath");
    public static final ResourceKey<Enchantment> GREEDY = key("greedy");
    public static final ResourceKey<Enchantment> CONSUMING = key("consuming");
    public static final ResourceKey<Enchantment> EDUCATIONAL = key("educational");
    public static final ResourceKey<Enchantment> CORRUPTING = key("corrupting");
    public static final ResourceKey<Enchantment> VOIDTOUCHED = key("voidtouched");
    public static final ResourceKey<Enchantment> IMPACT = key("impact");

    /** O lixo que a Consumidora come: a lista {@code Garbage Blocks} do original. */
    private static final List<net.minecraft.world.item.Item> TRASH = List.of(
            Items.DIRT, Items.SAND, Items.RED_SAND, Items.GRAVEL, Items.COBBLESTONE,
            Items.COBBLED_DEEPSLATE, Items.NETHERRACK);

    private ForbiddenEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Thaumcraft.id(name));
    }

    /** Que nível deste encantamento a peça tem. */
    public static int level(ResourceKey<Enchantment> which, ItemStack stack, Level level) {
        if (stack.isEmpty()) return 0;
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(which)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }

    /** O mesmo, para os encantamentos do próprio jogo. */
    private static int vanilla(ResourceKey<Enchantment> which, ItemStack stack, Level level) {
        return level(which, stack, level);
    }

    /** O que os três encantamentos de cava fazem com o que o bloco larga: o {@code onHarvest} do original. */
    public static void onBlockDrops(List<ItemStack> drops, ServerLevel level, BlockPos pos, ItemStack tool) {
        if (tool.isEmpty() || drops.isEmpty()) return;

        int aglomerante = level(CLUSTER, tool, level);
        if (aglomerante > 0 && vanilla(Enchantments.FORTUNE, tool, level) == 0) {
            float chance = 0.2f + aglomerante * 0.075f;
            for (int a = 0; a < drops.size(); a++) {
                ItemStack antes = drops.get(a);
                ItemStack depois = SpecialMining.refine(antes, chance, level.getRandom());
                if (ItemStack.isSameItem(antes, depois)) continue;
                drops.set(a, depois);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2f,
                        0.7f + level.getRandom().nextFloat() * 0.2f);
            }
        }

        if (level(CONSUMING, tool, level) > 0) {
            drops.removeIf(drop -> TRASH.contains(drop.getItem()));
        }

        if (level(CORRUPTING, tool, level) > 0 && level.getRandom().nextInt(3) == 1) {
            List<ItemStack> pecados = new java.util.ArrayList<>();
            for (int a = 0; a < drops.size(); a++) {
                ItemStack drop = drops.get(a);
                if (!isShard(drop)) continue;
                for (int i = 0; i < drop.getCount(); i++) pecados.add(new ItemStack(sinShard(level)));
                drops.set(a, ItemStack.EMPTY);
            }
            drops.removeIf(ItemStack::isEmpty);
            drops.addAll(pecados);
        }
    }

    /** Os fragmentos de cristal do Thaumcraft, que a Corruptora torce. */
    private static boolean isShard(ItemStack stack) {
        return TCItems.SHARDS.values().stream().anyMatch(stack::is);
    }

    /** O {@code getSinShard}: um dos sete fragmentos de pecado, ou o da gula. */
    private static net.minecraft.world.item.Item sinShard(Level level) {
        int sorte = level.getRandom().nextInt(7);
        // o dois e o quatro do original caem no fragmento da gula, que é comida
        return sorte == 2 || sorte == 4 ? ForbiddenItems.GLUTTONY_SHARD
                : ForbiddenItems.SHARDS.get(ForbiddenItems.VICES.get(sorte));
    }

    /** O que os dois encantamentos de arma fazem com quem morre: o {@code onDrops} do original. */
    public static void onKill(LivingEntity dead, Player killer) {
        ItemStack arma = killer.getMainHandItem();
        if (arma.isEmpty() || !(dead.level() instanceof ServerLevel level)) return;
        if (vanilla(Enchantments.LOOTING, arma, level) > 0) return;

        if (level(GREEDY, arma, level) > 0) {
            if (dead instanceof net.minecraft.world.entity.npc.villager.Villager) {
                dead.spawnAtLocation(level, new ItemStack(Items.EMERALD), 1.0f);
            } else if (dead instanceof Enemy && level.getRandom().nextInt(35) < 3) {
                dead.spawnAtLocation(level, new ItemStack(ForbiddenItems.EMERALD_NUGGET), 1.0f);
            }
        }

        int educativa = level(EDUCATIONAL, arma, level);
        if (educativa > 0 && dead instanceof Mob bicho) {
            int aprendizado = 3 * educativa * bicho.getExperienceReward(level, killer);
            // o jogo já parte a experiência em bolas do tamanho certo
            net.minecraft.world.entity.ExperienceOrb.award(level, dead.position(), aprendizado);
        }
    }
}
