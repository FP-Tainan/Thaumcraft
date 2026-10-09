package net.thaumcraft.occulta.demon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.Spawn;
import net.thaumcraft.research.Incurable;

/**
 * O que cada um dos cinco contratos faz: os cinco {@code activate} do {@code ItemGeneral} do Witchery.
 *
 * <p>Repare na divisão: um deles põe uma <b>coisa</b> no mundo e os outros quatro põem um <b>estado</b> em
 * alguém. E os três estados que não são do jogo — evaporar, tocar fogo e fundir — valem <b>só em gente</b>,
 * porque um bicho não clica em blocos nem quebra pedra.
 *
 * <p>Repare também no que eles <b>não</b> pedem: nenhum deles pergunta se quem está do outro lado quer.
 * Um Contrato do Blaze preso a outra pessoa põe um Blaze de cinquenta de vida ao pé dela, e é isso.
 */
public final class Contracts {
    /** O que o Blaze do contrato tem de vida e de murro: cinquenta e sete. */
    public static final double BLAZE_VIDA = 50.0;
    public static final double BLAZE_MURRO = 7.0;

    /** E em que anéis ele nasce ao pé de quem o apanha. */
    public static final int PERTO = 1;
    public static final int LONGE = 2;

    /** Quinze minutos de resistência ao fogo. */
    public static final int RESISTE = 20 * 60 * 15;

    /** E dez minutos dos outros três. */
    public static final int DEZ_MINUTOS = 20 * 60 * 10;

    private Contracts() {
    }

    /**
     * <b>Chama Viva</b>: um <b>Blaze</b> ao pé de quem está preso ao papel, com <b>cinquenta de vida</b> e
     * <b>sete de murro</b> — o dobro e meio do que um Blaze normal tem de cada.
     */
    public static boolean blaze(ServerLevel level, LivingEntity emQuem) {
        var bicho = Spawn.perto(level, EntityTypes.BLAZE, emQuem.blockPosition(), PERTO, LONGE);
        if (!(bicho instanceof net.minecraft.world.entity.LivingEntity vivo)) return false;

        var vida = vivo.getAttribute(Attributes.MAX_HEALTH);
        if (vida != null) {
            vida.setBaseValue(BLAZE_VIDA);
            vivo.setHealth((float) BLAZE_VIDA);
        }
        var murro = vivo.getAttribute(Attributes.ATTACK_DAMAGE);
        if (murro != null) murro.setBaseValue(BLAZE_MURRO);
        if (vivo instanceof net.minecraft.world.entity.Mob mob) mob.setPersistenceRequired();
        return true;
    }

    /** <b>Tolerância ao Fogo</b>: quinze minutos de Resistência ao Fogo, e vale em qualquer coisa viva. */
    public static boolean resisteAoFogo(ServerLevel level, LivingEntity emQuem) {
        emQuem.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, RESISTE));
        return true;
    }

    /** <b>Evaporação</b>: dez minutos, e só em gente. */
    public static boolean evapora(ServerLevel level, LivingEntity emQuem) {
        return põe(emQuem, OccultaEffects.IMP_EVAPORATION);
    }

    /** <b>Toque de Fogo</b>: dez minutos, e só em gente. */
    public static boolean toqueDeFogo(ServerLevel level, LivingEntity emQuem) {
        return põe(emQuem, OccultaEffects.IMP_FIRE_TOUCH);
    }

    /** <b>Toque de Fundir</b>: dez minutos, e só em gente. */
    public static boolean funde(ServerLevel level, LivingEntity emQuem) {
        return põe(emQuem, OccultaEffects.IMP_MELTING_TOUCH);
    }

    /**
     * Põe um dos três em quem for gente, e recusa em quem não for.
     *
     * <p>E põe-no <b>sem cura</b>, que é o que o original faz ao não os fazer poções: um balde de leite
     * não tira um contrato de cima de ninguém.
     */
    private static boolean põe(LivingEntity emQuem,
                               net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> qual) {
        if (!(emQuem instanceof Player)) return false;
        Incurable.add(emQuem, new MobEffectInstance(qual, DEZ_MINUTOS));
        return true;
    }

    /** Sem uso fora do porte: serve à prova para saber onde o Blaze pode nascer. */
    public static BlockPos onde(ServerLevel level, BlockPos daqui) {
        return Spawn.lugar(level, daqui, PERTO, LONGE);
    }
}
