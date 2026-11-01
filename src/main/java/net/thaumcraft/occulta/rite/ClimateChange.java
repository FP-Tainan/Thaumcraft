package net.thaumcraft.occulta.rite;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A <b>Mudança de Clima</b>: o {@code RiteClimateChange} do Witchery.
 *
 * <p>É o maior rito do ofício, e o único que <b>muda o mundo e não o que está nele</b>: ele troca o bioma
 * de tudo à volta do círculo. Uma pradaria vira selva; um deserto vira geleira.
 *
 * <p>E ele é caro de propósito. Pede o <b>anel de fora inteiro</b>, sete coisas, um <b>coven de quatro</b>
 * bruxas — e o raio do que muda sai de quantas bruxas vieram: <b>dezesseis blocos por bruxa acima de
 * três</b>. Com quatro mudam-se dezesseis blocos à volta; com sete, quase cinquenta.
 *
 * <h2>O foco diz em que bioma</h2>
 *
 * <p>O rito não tem um alvo escrito: ele <b>olha o chão</b>. Largando-se uma coisa a oito blocos do
 * círculo, ela diz que clima se quer — uma muda de carvalho pede floresta, um cato pede deserto, uma
 * Agulha de Gelo pede gelo. Sem nenhuma dessas coisas no chão, o rito desiste.
 *
 * <p>E a <b>poeira de glowstone</b> que estiver junto escolhe <b>qual</b> dos biomas daquele clima: uma
 * pedra, o primeiro; duas, o segundo. Sem ela, sorteia-se entre os três primeiros. É um detalhe miúdo do
 * original, e é o que separa «quero floresta» de «quero floresta de bétulas».
 *
 * <p>Dois dos climas mexem também no <b>tempo</b>: o deserto limpa o céu, o gelo faz chover.
 *
 * <h2>Dois desvios declarados</h2>
 *
 * <p><b>Os climas são rótulos escritos à mão.</b> O original pergunta ao dicionário de biomas da Forge,
 * que hoje não existe, e os rótulos do jogo cobrem menos da metade dos treze climas — não há
 * {@code is_swamp} nem {@code is_desert}. Os treze vão escritos em
 * {@code data/thaumcraft/tags/worldgen/biome/climate_*.json}, e quem jogar pode mexer neles.
 *
 * <p><b>A área é um quadrado e não um círculo.</b> O original escreve os biomas à mão, pedaço a pedaço, e
 * manda os pedaços de volta para o lado de cá. Hoje quem faz isso é o próprio jogo, pelo caminho do
 * {@code /fillbiome} — e esse caminho enche uma <b>caixa</b>. Usá-lo é mais são do que refazer as tripas
 * do jogo para arredondar um canto.
 */
public final class ClimateChange {
    /** Quantas bruxas o rito pede, e quantos blocos de raio cada uma acima disso vale. */
    public static final int COVEN_MÍNIMO = 4;
    public static final int POR_BRUXA = 16;

    /** A que distância do círculo o foco tem de estar largado. */
    public static final double DO_FOCO = 8.0;

    /** Quantas batidas ele leva a acender, e entre quantos biomas ele sorteia sem glowstone. */
    public static final int FASES = 5;
    public static final int SORTEIA_ENTRE = 3;

    private ClimateChange() {
    }

    /** O que um clima faz ao tempo. */
    public enum Tempo {
        NADA, SOL, CHUVA
    }

    /** Um clima: o rótulo dos biomas dele e o que ele faz ao tempo. */
    public record Clima(String nome, Tempo tempo) {
        public TagKey<Biome> rótulo() {
            return TagKey.create(Registries.BIOME, Thaumcraft.id("climate_" + this.nome));
        }
    }

    /**
     * O que cada coisa largada no chão pede, pela lista do original.
     *
     * <p>A ordem importa pouco, mas as coisas importam muito: são elas o <b>vocabulário</b> do rito, e
     * quem joga as descobre uma a uma. Uma muda de carvalho é floresta; a de selva é selva; o cato é
     * deserto. Não há menu nenhum que o diga.
     */
    public static Map<Item, Clima> focos() {
        Map<Item, Clima> quais = new LinkedHashMap<>();
        quais.put(Items.OAK_SAPLING, new Clima("forest", Tempo.NADA));
        quais.put(Items.SHORT_GRASS, new Clima("plains", Tempo.NADA));
        quais.put(Items.OBSIDIAN, new Clima("mountain", Tempo.NADA));
        quais.put(Items.STONE, new Clima("hills", Tempo.NADA));
        quais.put(Items.SLIME_BALL, new Clima("swamp", Tempo.NADA));
        quais.put(Items.WATER_BUCKET, new Clima("water", Tempo.NADA));
        quais.put(Items.CACTUS, new Clima("desert", Tempo.SOL));
        quais.put(OccultaItems.ICY_NEEDLE, new Clima("frozen", Tempo.CHUVA));
        quais.put(Items.JUNGLE_SAPLING, new Clima("jungle", Tempo.NADA));
        quais.put(Items.NETHERRACK, new Clima("wasteland", Tempo.NADA));
        quais.put(Items.SAND, new Clima("beach", Tempo.NADA));
        quais.put(Items.RED_MUSHROOM, new Clima("mushroom", Tempo.NADA));
        quais.put(Items.SKELETON_SKULL, new Clima("magical", Tempo.NADA));
        return quais;
    }

    /** O que se achou no chão: o clima pedido e quanta glowstone o acompanhava. */
    public record NoChão(@Nullable Clima clima, int glowstone, List<ItemEntity> gastos) {
    }

    /** Olha o chão à volta do círculo e lê o que se pediu. */
    public static NoChão lêOChão(ServerLevel level, BlockPos meio) {
        Map<Item, Clima> quais = focos();
        Clima achado = null;
        int glowstone = 0;
        List<ItemEntity> gastos = new ArrayList<>();

        AABB caixa = new AABB(meio.getX() - DO_FOCO, meio.getY() - 2, meio.getZ() - DO_FOCO,
                meio.getX() + DO_FOCO, meio.getY() + 2, meio.getZ() + DO_FOCO);
        for (ItemEntity largado : level.getEntitiesOfClass(ItemEntity.class, caixa)) {
            Item oquê = largado.getItem().getItem();
            if (oquê == Items.GLOWSTONE_DUST) {
                glowstone += largado.getItem().getCount();
                gastos.add(largado);
                continue;
            }
            Clima qual = quais.get(oquê);
            if (qual == null) continue;
            achado = qual;
            gastos.add(largado);
        }
        return new NoChão(achado, glowstone, gastos);
    }

    /**
     * Qual bioma daquele clima sai.
     *
     * <p>Com glowstone, o <b>n-ésimo</b> do rótulo; sem ela, um dos três primeiros ao acaso. É a conta do
     * original, com a lista do dicionário trocada pela do rótulo.
     */
    public static @Nullable Holder<Biome> qualBioma(ServerLevel level, Clima clima, int glowstone) {
        HolderSet.Named<Biome> rol = level.registryAccess()
                .lookupOrThrow(Registries.BIOME).get(clima.rótulo()).orElse(null);
        if (rol == null || rol.size() == 0) return null;
        int qual = glowstone > 0
                ? Math.min(glowstone, rol.size()) - 1
                : level.getRandom().nextInt(Math.min(SORTEIA_ENTRE, rol.size()));
        return rol.get(qual);
    }

    /** O raio que um coven daquele tamanho alcança. */
    public static int raio(int coven) {
        return POR_BRUXA * (coven - (COVEN_MÍNIMO - 1));
    }

    /** E o tempo que o clima pede, se pedir algum. */
    public static void mexeNoTempo(ServerLevel level, Tempo qual) {
        if (qual == Tempo.NADA) return;
        // o tempo do mundo de hoje mora num guardado à parte, e não no ServerLevel
        int quanto = (300 + level.getRandom().nextInt(600)) * 20;
        var tempo = level.getWeatherData();
        if (qual == Tempo.SOL) {
            if (!tempo.isRaining() && !tempo.isThundering()) return;
            tempo.setClearWeatherTime(quanto);
            tempo.setRainTime(0);
            tempo.setThunderTime(0);
            tempo.setRaining(false);
            tempo.setThundering(false);
            return;
        }
        if (tempo.isRaining() || tempo.isThundering()) return;
        tempo.setClearWeatherTime(0);
        tempo.setRainTime(quanto);
        tempo.setThunderTime(quanto);
        tempo.setRaining(true);
        tempo.setThundering(false);
    }

    /**
     * O passo do rito: cinco segundos de faíscas e, no quinto, o mundo muda.
     *
     * <p>As recusas são as do original e pela mesma ordem: o <b>mundo</b> (nem o Nether nem o Fim), o
     * <b>coven</b> e o <b>foco</b>. Cada uma devolve o que se ofereceu.
     */
    public static final class Passo implements RiteStep {
        private int fase;

        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            Player quem = rito.starter(level);
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                return recusa(level, onde, quem, "wrongdimension");
            }
            if (rito.coven() < COVEN_MÍNIMO) {
                return recusa(level, onde, quem, "coventoosmall");
            }
            if (ticks % 20L != 0L) return Result.STARTING;

            this.fase++;
            if (this.fase < FASES) {
                level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                                ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                        onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5,
                        16, this.fase * 1.5, this.fase * 1.1, this.fase * 1.5, 0.0);
                return Result.STARTING;
            }

            NoChão chão = lêOChão(level, onde);
            if (chão.clima() == null) return recusa(level, onde, quem, "missingbiomefoci");
            Holder<Biome> bioma = qualBioma(level, chão.clima(), chão.glowstone());
            if (bioma == null) return recusa(level, onde, quem, "missingbiomefoci");

            for (ItemEntity gasto : chão.gastos()) {
                level.sendParticles(ParticleTypes.POOF, gasto.getX(), gasto.getY() + 0.5, gasto.getZ(),
                        8, 0.3, 0.3, 0.3, 0.0);
                gasto.discard();
            }

            muda(level, onde, raio(rito.coven()), bioma);
            mexeNoTempo(level, chão.clima().tempo());
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, onde.getX() + 0.5, onde.getY() + 1.0,
                    onde.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
            return Result.COMPLETED;
        }
    }

    /** Troca o bioma de tudo à volta, pelo caminho que o próprio jogo usa. */
    public static int muda(ServerLevel level, BlockPos meio, int raio, Holder<Biome> bioma) {
        BlockPos de = new BlockPos(meio.getX() - raio, level.getMinY(), meio.getZ() - raio);
        BlockPos até = new BlockPos(meio.getX() + raio, level.getMaxY(), meio.getZ() + raio);
        return net.minecraft.server.commands.FillBiomeCommand.fill(level, de, até, bioma)
                .left().orElse(0);
    }

    private static RiteStep.Result recusa(ServerLevel level, BlockPos onde, @Nullable Player quem,
                                          String porquê) {
        level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        if (quem != null) {
            quem.sendSystemMessage(Component.translatable("tc.rite." + porquê)
                    .withStyle(ChatFormatting.RED));
        }
        return RiteStep.Result.ABORTED_REFUND;
    }

    /** E o rito, que é um passo só. */
    public record Rito() implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of(new Passo());
        }
    }
}
