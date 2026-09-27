package net.thaumcraft.occulta.brazier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O que o Braseiro queima: o {@code BrazierRecipes} do Witchery.
 *
 * <p>Uma receita de braseiro são <b>três coisas</b>, sem ordem, e um tempo de queima. Enquanto queima, ela
 * <b>faz</b> alguma coisa de segundo em segundo — e é isso que ela é: não sai nada do braseiro senão cinza; o que
 * sai dele é o que acontece em volta enquanto o fogo dura.
 *
 * <p>São oito no original. Estas são as quatro que o mod de hoje alcança; as outras quatro pedem o Pó de
 * Cemitério, o Medo Condensado e os espíritos — o Espectro, a Banshee e o Poltergeist —, e entram com eles.
 */
public final class BrazierRecipes {
    private static final List<Recipe> ALL = new ArrayList<>();

    private BrazierRecipes() {
    }

    /** O que uma receita faz em volta enquanto arde. */
    public interface Burning {
        void onBurning(ServerLevel level, BlockPos onde, long ticks);
    }

    /**
     * Uma receita de braseiro.
     *
     * @param key      a chave do nome dela, para o livro
     * @param power    se ela pede poder de altar
     * @param burn     quantos tiques ela arde
     * @param inputs   as três coisas, sem ordem
     * @param burning  e o que ela faz enquanto arde
     */
    public record Recipe(String key, boolean power, int burn, List<Item> inputs, Burning burning) {
        /** Se aquelas três coisas são esta receita. */
        public boolean matches(List<ItemStack> postas) {
            List<Item> faltam = new ArrayList<>(this.inputs);
            int quantas = 0;
            for (ItemStack coisa : postas) {
                if (coisa.isEmpty()) continue;
                quantas++;
                if (!faltam.remove(coisa.getItem())) return false;
            }
            return faltam.isEmpty() && quantas == this.inputs.size();
        }
    }

    public static Recipe add(String key, boolean power, int burn, Burning burning, Item... inputs) {
        Recipe receita = new Recipe(key, power, burn, List.of(inputs), burning);
        ALL.add(receita);
        return receita;
    }

    public static @Nullable Recipe find(List<ItemStack> postas) {
        for (Recipe receita : ALL) {
            if (receita.matches(postas)) return receita;
        }
        return null;
    }

    public static @Nullable Recipe of(String key) {
        for (Recipe receita : ALL) {
            if (receita.key.equals(key)) return receita;
        }
        return null;
    }

    public static List<Recipe> all() {
        return List.copyOf(ALL);
    }

    // ------------------------------------------------------------------ os números do original

    /** De quantos em quantos segundos as três que dão efeito o dão. */
    public static final int EVERY = 3;
    /** E quanto tempo o efeito dura. */
    public static final int LASTS = 20 * 6;

    /** A que distância a Força e a Resistência pegam, e a que distância a Invisibilidade. */
    public static final double NEAR = 4.0;
    public static final double FAR = 6.0;

    private static final int FIVE_MINUTES = 20 * 60 * 5;
    private static final int TEN_MINUTES = 20 * 60 * 10;

    /** A lista, posta na tabela. */
    public static void register() {
        // a Fumaça: um sinal que se vê de longe, e mais nada
        add("tc.brazier.smoke", true, FIVE_MINUTES,
                (level, onde, ticks) -> level.sendParticles(ParticleTypes.EXPLOSION,
                        onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 4, 16.0, 4.0, 16.0, 0.0),
                Items.GUNPOWDER, net.thaumcraft.occulta.OccultaItems.QUICKLIME, Items.GLOWSTONE_DUST);

        // a Força, a Resistência e a Invisibilidade: o que arde, arde para quem está em volta
        add("tc.brazier.strong", true, FIVE_MINUTES,
                (level, onde, ticks) -> around(level, onde, ticks, NEAR, MobEffects.STRENGTH),
                net.thaumcraft.occulta.OccultaItems.TEAR_OF_THE_GODDESS, Items.BONE, Items.BLAZE_POWDER);

        add("tc.brazier.tough", true, FIVE_MINUTES,
                (level, onde, ticks) -> around(level, onde, ticks, NEAR, MobEffects.RESISTANCE),
                net.thaumcraft.occulta.OccultaItems.TEAR_OF_THE_GODDESS, Items.ROTTEN_FLESH, Items.BLAZE_POWDER);

        add("tc.brazier.invisible", true, TEN_MINUTES,
                (level, onde, ticks) -> around(level, onde, ticks, FAR, MobEffects.INVISIBILITY),
                Items.ENDER_PEARL, Items.SPIDER_EYE, Items.BLAZE_ROD);
    }

    /** O efeito que se derrama em volta, de três em três segundos. */
    private static void around(ServerLevel level, BlockPos onde, long ticks, double raio,
                               net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> qual) {
        if (ticks % (EVERY * 20L) != 0L) return;
        AABB roda = new AABB(onde.getX() + 0.5 - raio, onde.getY() - raio, onde.getZ() + 0.5 - raio,
                onde.getX() + 0.5 + raio, onde.getY() + raio, onde.getZ() + 0.5 + raio);
        for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (vivo.distanceToSqr(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5) > raio * raio) continue;
            vivo.addEffect(new MobEffectInstance(qual, LASTS, 0));
        }
    }
}
