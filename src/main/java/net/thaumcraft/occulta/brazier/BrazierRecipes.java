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
 * <p>E há receitas que, em vez de fazerem algo enquanto ardem, fazem algo <b>quando acabam</b>: as três
 * que chamam fantasmas. A fogueira arde meio minuto sem nada acontecer, e no fim é que o bicho aparece —
 * o que é a maneira certa de a coisa se sentir, porque não dá para desistir no meio.
 *
 * <p>São as <b>oito</b> do original.
 */
public final class BrazierRecipes {
    private static final List<Recipe> ALL = new ArrayList<>();

    private BrazierRecipes() {
    }

    /**
     * O que uma receita faz em volta enquanto arde.
     *
     * <p>E quanto ela <b>guarda</b> ao fazê-lo: cada ponto guardado estica a queima em
     * {@link BrazierBlockEntity#EXTENDS} tiques. Só a do Murchar guarda algo, e é o que a faz durar
     * enquanto houver plantação para secar.
     */
    public interface Burning {
        int onBurning(ServerLevel level, BlockPos onde, long ticks);
    }

    /** E o que ela faz <b>quando acaba de arder</b>, que é de onde saem os fantasmas. */
    public interface Burnt {
        void onBurnt(ServerLevel level, BlockPos onde);
    }

    /**
     * Uma receita de braseiro.
     *
     * @param key      a chave do nome dela, para o livro
     * @param power    se ela pede poder de altar
     * @param burn     quantos tiques ela arde
     * @param inputs   as três coisas, sem ordem
     * @param burning  o que ela faz enquanto arde
     * @param burnt    e o que ela faz quando acaba, se fizer algo
     */
    public record Recipe(String key, boolean power, int burn, List<Item> inputs, Burning burning,
                         @Nullable Burnt burnt) {
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
        return add(key, power, burn, burning, null, inputs);
    }

    public static Recipe add(String key, boolean power, int burn, Burning burning, @Nullable Burnt burnt,
                             Item... inputs) {
        Recipe receita = new Recipe(key, power, burn, List.of(inputs), burning, burnt);
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

    /** As três dos fantasmas: meio minuto, e a do Poltergeist três quartos de minuto. */
    public static final int THIRTY_SECONDS = 20 * 30;
    public static final int FORTY_FIVE_SECONDS = 20 * 45;

    /** De quantos em quantos segundos elas fazem o pó enquanto ardem. */
    public static final int SPARK_EVERY = 5;

    /** A que distância o fantasma nasce, e a que distância o poltergeist de brinde. */
    public static final int CLOSE_MIN = 1;
    public static final int CLOSE_MAX = 2;
    public static final int FAR_MIN = 6;
    public static final int FAR_MAX = 10;

    /** E de cada vinte, um traz um poltergeist atrás. */
    public static final int ONE_IN_TWENTY = 20;

    /** Os números do Murchar: de cinco em cinco tiques, num cubo de sete, e seis alturas. */
    public static final int WILT_EVERY = 5;
    public static final int WILT_RADIUS = 3;
    public static final int ALTURAS = 6;
    public static final int DESCE = 2;

    /** Quanto de vida ela cura, e quanto ela guarda de cada planta secada. */
    public static final float CURA = 0.1f;
    public static final int GUARDA = 2;

    /** E quanto ela arde sozinha, se não houver nada para secar. */
    public static final int ONE_MINUTE = 20 * 60;

    /** A lista, posta na tabela. */
    public static void register() {
        // a Fumaça: um sinal que se vê de longe, e mais nada
        add("tc.brazier.smoke", true, FIVE_MINUTES,
                (level, onde, ticks) -> {
                    level.sendParticles(ParticleTypes.EXPLOSION,
                            onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 4, 16.0, 4.0, 16.0, 0.0);
                    return 0;
                },
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

        /*
         * E as três dos fantasmas. Repare que a do Poltergeist é a <b>única das oito que não pede poder
         * de altar</b>: ele é o fantasma que qualquer um consegue chamar, e é também o que ninguém quer.
         */
        add("tc.brazier.spectre", true, THIRTY_SECONDS,
                BrazierRecipes::spark,
                (level, onde) -> ghost(level, onde, net.thaumcraft.occulta.OccultaEntities.SPECTRE),
                net.thaumcraft.occulta.OccultaItems.WORMWOOD_SPRIG,
                net.thaumcraft.occulta.OccultaItems.BAT_WOOL,
                net.thaumcraft.occulta.OccultaItems.GRAVEYARD_DUST);

        add("tc.brazier.banshee", true, THIRTY_SECONDS,
                BrazierRecipes::spark,
                (level, onde) -> ghost(level, onde, net.thaumcraft.occulta.OccultaEntities.BANSHEE),
                net.thaumcraft.occulta.OccultaItems.WORMWOOD_SPRIG,
                net.thaumcraft.occulta.OccultaItems.CONDENSED_FEAR,
                net.thaumcraft.occulta.OccultaItems.GRAVEYARD_DUST);

        add("tc.brazier.poltergeist", false, FORTY_FIVE_SECONDS,
                BrazierRecipes::spark,
                (level, onde) -> ghost(level, onde, net.thaumcraft.occulta.OccultaEntities.POLTERGEIST),
                net.thaumcraft.occulta.OccultaItems.WORMWOOD_SPRIG,
                net.thaumcraft.occulta.OccultaItems.REFINED_EVIL,
                net.thaumcraft.occulta.OccultaItems.FOCUSED_WILL);

        /*
         * E a oitava, que também não pede poder: o original escreve {@code true} no construtor dela e
         * depois <b>o desmente</b> num método que devolve {@code false}. Vale o segundo.
         */
        add("tc.brazier.wilting", false, ONE_MINUTE, BrazierRecipes::wilt,
                net.thaumcraft.occulta.OccultaItems.CONDENSED_FEAR,
                net.thaumcraft.occulta.OccultaItems.WORMY_APPLE,
                net.thaumcraft.occulta.OccultaItems.GRAVEYARD_DUST);
    }

    /** O pó das três dos fantasmas, de cinco em cinco segundos, enquanto a fogueira arde. */
    private static int spark(ServerLevel level, BlockPos onde, long ticks) {
        if (ticks % (SPARK_EVERY * 20L) != 0L) return 0;
        level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5, 16, 0.5, 1.0, 0.5, 0.0);
        return 0;
    }

    /**
     * <b>O fantasma.</b> Nasce a um ou dois blocos do braseiro — e, de cada vinte, um <b>traz um
     * poltergeist</b> atrás, a seis ou dez blocos, que é a piada do original: chamar os mortos às vezes
     * chama o que não se pediu, e aquele fica.
     */
    private static void ghost(ServerLevel level, BlockPos onde,
                              net.minecraft.world.entity.EntityType<?> qual) {
        nasce(level, onde, qual, CLOSE_MIN, CLOSE_MAX, true);
        if (level.getRandom().nextInt(ONE_IN_TWENTY) != 0) return;
        nasce(level, onde, net.thaumcraft.occulta.OccultaEntities.POLTERGEIST,
                FAR_MIN, FAR_MAX, false);
    }

    /** Põe um no mundo, com o pó e a nota de harpa do original — ou sem, no caso do de brinde. */
    private static void nasce(ServerLevel level, BlockPos onde,
                              net.minecraft.world.entity.EntityType<?> qual,
                              int perto, int longe, boolean comNota) {
        var bicho = net.thaumcraft.occulta.Spawn.perto(level, qual, onde, perto, longe);
        if (!(bicho instanceof net.minecraft.world.entity.Mob mob)) return;
        net.thaumcraft.occulta.Spawn.comOvo(level, mob);

        var meio = net.thaumcraft.occulta.Spawn.meio(mob);
        var pó = comNota
                ? net.minecraft.core.particles.SpellParticleOption.create(
                        ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f)
                : (net.minecraft.core.particles.ParticleOptions) net.minecraft.core.particles
                        .ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFFFFFFF);
        level.sendParticles(pó,
                meio.x, meio.y, meio.z, 16, 1.0, mob.getBbHeight(), 1.0, 0.0);
        if (comNota) {
            level.playSound(null, mob.blockPosition(),
                    net.minecraft.sounds.SoundEvents.NOTE_BLOCK_HARP.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    /**
     * <b>O Murchar</b>: o {@code WILTING} do original, e a oitava.
     *
     * <p>De cinco em cinco tiques ela aponta um lugar ao acaso num cubo de sete por sete em volta — e a
     * altura dele <b>varre de baixo para cima</b>, de dois abaixo do braseiro a três acima, num ciclo de
     * trinta tiques. Se o que estiver ali for uma <b>plantação crescida</b>, ela <b>desfaz um passo do
     * crescimento dela</b> e, com isso, <b>cura um décimo da vida</b> a cada morto-vivo a três blocos.
     *
     * <p>E então ela <b>guarda dois</b>, que são oitocentos tiques a mais de fogueira. É a única das oito
     * que se alimenta do que faz: enquanto houver trigo em volta, ela não acaba.
     */
    private static int wilt(ServerLevel level, BlockPos onde, long ticks) {
        if (ticks % WILT_EVERY != 0L) return 0;

        int sobe = (int) (ticks % (WILT_EVERY * ALTURAS)) / WILT_EVERY;
        BlockPos ali = new BlockPos(
                onde.getX() - WILT_RADIUS + level.getRandom().nextInt(WILT_RADIUS * 2 + 1),
                onde.getY() - DESCE + sobe,
                onde.getZ() - WILT_RADIUS + level.getRandom().nextInt(WILT_RADIUS * 2 + 1));

        /*
         * O original pergunta por {@code IPlantable} e pelo tipo de planta ser {@code Crop}. Hoje a
         * {@link net.minecraft.world.level.block.CropBlock} é exatamente esse conjunto — o trigo, as
         * cenouras, as batatas, a beterraba — e as plantas do ofício herdam dela.
         */
        var feitio = level.getBlockState(ali);
        if (!(feitio.getBlock() instanceof net.minecraft.world.level.block.CropBlock planta)) return 0;
        int quanto = planta.getAge(feitio);
        if (quanto <= 0) return 0;

        level.setBlock(ali, planta.getStateForAge(quanto - 1),
                net.minecraft.world.level.block.Block.UPDATE_ALL);
        level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(
                        ParticleTypes.ENTITY_EFFECT, 0xFFFFFFFF),
                onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5, 8, 0.3, 0.5, 0.3, 0.0);
        heal(level, onde);
        return GUARDA;
    }

    /** E a cura que ela dá aos mortos-vivos em volta: um décimo da vida de cada um. */
    private static void heal(ServerLevel level, BlockPos onde) {
        AABB roda = new AABB(onde.getX() - WILT_RADIUS, onde.getY() - WILT_RADIUS, onde.getZ() - WILT_RADIUS,
                onde.getX() + WILT_RADIUS, onde.getY() + WILT_RADIUS, onde.getZ() + WILT_RADIUS);
        for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (!vivo.getType().builtInRegistryHolder()
                    .is(net.minecraft.tags.EntityTypeTags.UNDEAD)) continue;
            float cheio = vivo.getMaxHealth();
            if (vivo.getHealth() >= cheio) continue;
            vivo.heal(cheio * CURA);
            level.sendParticles(ParticleTypes.HEART, vivo.getX(), vivo.getY() + 1.0, vivo.getZ(),
                    8, 0.5, 1.0, 0.5, 0.0);
        }
    }

    /** O efeito que se derrama em volta, de três em três segundos. */
    private static int around(ServerLevel level, BlockPos onde, long ticks, double raio,
                               net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> qual) {
        if (ticks % (EVERY * 20L) != 0L) return 0;
        AABB roda = new AABB(onde.getX() + 0.5 - raio, onde.getY() - raio, onde.getZ() + 0.5 - raio,
                onde.getX() + 0.5 + raio, onde.getY() + raio, onde.getZ() + 0.5 + raio);
        for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (vivo.distanceToSqr(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5) > raio * raio) continue;
            vivo.addEffect(new MobEffectInstance(qual, LASTS, 0));
        }
        return 0;
    }
}
