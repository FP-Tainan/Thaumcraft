package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.Thaumcraft;

import java.util.Locale;

/**
 * O feitiço <b>guardado para quando</b>: as cinco {@code Contingency_*} do Ars Magica 2.
 *
 * <p>É a única coisa do ramo que corre <b>sozinha</b>. Lançar uma Contingência não faz nada de visível: ela
 * escreve a frase dentro de quem a levou, e ali fica, calada, até acontecer a coisa que ela espera — e aí
 * dispara por conta própria.
 *
 * <p>São cinco esperas: <b>cair</b>, <b>levar dano</b>, <b>pegar fogo</b>, <b>ficar com pouca vida</b> e
 * <b>morrer</b>. Uma Contingência de Morte com uma Cura é uma segunda vida; uma de Queda com Pena Suave é um
 * paraquedas que ninguém precisa lembrar de abrir.
 *
 * <p><b>Uma de cada vez.</b> Quem já tem uma guardada e lança outra perde a primeira — é o original, e é o que
 * impede alguém de andar por aí com cinco redes de segurança.
 *
 * <p>E elas custam <b>dez vezes</b> uma Forma comum, que é o preço de um feitiço que espera.
 *
 * @param kind  o que ela espera
 * @param spell e o que ela corre quando acontece
 */
public record Contingency(Kind kind, Spell spell) {
    /** O que uma Contingência espera: os {@code ContingencyTypes} do original. */
    public enum Kind {
        /** Nenhuma: quem não tem nada guardado. */
        NONE,
        /** <b>Queda</b>: quando o chão está perto demais para o que falta cair. */
        FALL,
        /** <b>Dano</b>: em qualquer pancada que chegue. */
        DAMAGE_TAKEN,
        /** <b>Fogo</b>: enquanto estiver ardendo. */
        ON_FIRE,
        /** <b>Vida Baixa</b>: quando cair a um terço da vida. */
        HEALTH_LOW,
        /** E <b>Morte</b>: no instante em que morre, antes de morrer de verdade. */
        DEATH;

        public String key() {
            return "tc.spell.shape.contingency_" + this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** A que espera quem chega à queda: os quatro blocos de queda do original. */
    public static final float FALL_MIN = 4.0f;

    /** E o quanto o chão tem de estar perto, medido contra a velocidade de queda. */
    public static final double FALL_LOOKAHEAD = -8.0;

    /** A fração de vida abaixo da qual a de Vida Baixa dispara: o terço do original. */
    public static final float LOW_HEALTH = 1.0f / 3.0f;

    /** Quanto elas custam: as dez vezes do original. */
    public static final float MANA_MULTIPLIER = 10.0f;

    public static final Contingency NONE = new Contingency(Kind.NONE, Spell.EMPTY);

    public static final Codec<Contingency> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(
                            nome -> Kind.valueOf(nome.toUpperCase(Locale.ROOT)),
                            k -> k.name().toLowerCase(Locale.ROOT))
                    .optionalFieldOf("kind", Kind.NONE).forGetter(Contingency::kind),
            Spell.CODEC.optionalFieldOf("spell", Spell.EMPTY).forGetter(Contingency::spell))
            .apply(i, Contingency::new));

    public static final AttachmentType<Contingency> DATA = AttachmentRegistry.<Contingency>builder()
            .initializer(() -> NONE)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("contingency"));

    public static Contingency of(LivingEntity quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static void set(LivingEntity quem, Contingency qual) {
        quem.setAttached(DATA, qual);
    }

    /** Guarda uma nova, apagando a que houvesse. */
    public static void arm(LivingEntity quem, Kind qual, Spell feitiço) {
        set(quem, new Contingency(qual, feitiço));
    }

    /** E limpa. */
    public static void clear(LivingEntity quem) {
        set(quem, NONE);
    }

    public boolean armed() {
        return this.kind != Kind.NONE && !this.spell.isEmpty();
    }

    /**
     * Dispara a Contingência daquela pessoa, se ela for da espera certa.
     *
     * <p>Ela <b>se gasta</b> ao disparar: o original limpa a guardada antes de a correr, e é por isso que uma
     * Contingência de Dano não entra num laço sem fim quando o feitiço dela fere quem a levava.
     *
     * @return se disparou
     */
    public static boolean proc(ServerLevel level, LivingEntity quem, Kind qual) {
        Contingency guardada = of(quem);
        if (!guardada.armed() || guardada.kind() != qual) return false;

        clear(quem);
        SpellCast.cast(level, guardada.spell(), quem, quem, quem.position());
        return true;
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela o dado a se registrar. */
    public static void init() {
    }
}
