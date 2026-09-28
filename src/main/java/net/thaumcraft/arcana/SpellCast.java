package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Lançar um feitiço: o {@code SpellHelper} do Ars Magica 2.
 *
 * <p>A conta é sempre a mesma e corre nesta ordem: se vê se dá (mana e desgaste), se corre a <b>Forma</b> da
 * etapa da frente, e é ela que acha o alvo e chama as <b>Essências</b>. Só se cobra <b>depois</b> de a etapa
 * pegar — um feitiço que não achou nada não custa nada, que é o que o original faz ao devolver
 * {@code EFFECT_FAILED} antes de tirar mana.
 */
public final class SpellCast {
    private SpellCast() {
    }

    /** O que saiu de um lançamento: o {@code SpellCastResult}. */
    public enum Result {
        /** Pegou. */
        SUCCESS,
        /** Não havia mana. */
        NOT_ENOUGH_MANA,
        /** O mago está gasto demais. */
        BURNED_OUT,
        /** A frase não faz sentido: sem etapa, ou com uma Forma que não devia estar ali. */
        MALFORMED,
        /** Correu, mas não achou nada em que pegar. */
        EFFECT_FAILED;

        public boolean ok() {
            return this == SUCCESS;
        }
    }

    /**
     * Lança a etapa da frente daquele feitiço.
     *
     * <p>É esta que a Forma volta a chamar com {@link Spell#pop()} quando acaba, e é por isso que uma frase de
     * três etapas corre as três sem ninguém escrever um laço.
     */
    public static Result cast(ServerLevel level, Spell feitiço, LivingEntity quem, @Nullable Entity alvo,
                              Vec3 onde) {
        if (feitiço.isEmpty()) return Result.SUCCESS;
        Spell.Stage etapa = feitiço.first();
        if (etapa == null) return Result.MALFORMED;

        // o Arcano desconta cinco por cento da mana e do desgaste, acima de meio
        float desconto = AffinityEffects.manaDiscount(quem);
        float custo = feitiço.manaCost(quem, alvo) * desconto;
        float desgaste = feitiço.burnout() * desconto;
        if (!affords(quem, custo, desgaste)) {
            return manaOf(quem).canBurn(desgaste) ? Result.NOT_ENOUGH_MANA : Result.BURNED_OUT;
        }

        Result saiu = etapa.shape().begin(level, feitiço, quem, alvo, onde);
        if (saiu.ok()) {
            charge(quem, custo, desgaste);
            shiftAffinity(quem, etapa);
        }
        return saiu;
    }

    /**
     * Manda as essências da etapa da frente num bicho: o {@code applyStageToEntity}.
     *
     * @return {@code SUCCESS} se ao menos uma delas pegou
     */
    public static Result onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
        Spell.Stage etapa = feitiço.first();
        if (etapa == null) return Result.MALFORMED;
        if (alvo instanceof Player gente && gente.hasInfiniteMaterials()) return Result.EFFECT_FAILED;

        boolean pegou = false;
        for (SpellPart.Essence essência : etapa.essences()) {
            if (essência.onEntity(level, feitiço, quem, alvo)) pegou = true;
        }
        return pegou ? Result.SUCCESS : Result.EFFECT_FAILED;
    }

    /** E num bloco: o {@code applyStageToGround}. */
    public static Result onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                                 Direction face, Vec3 batida) {
        Spell.Stage etapa = feitiço.first();
        if (etapa == null) return Result.MALFORMED;

        boolean pegou = false;
        for (SpellPart.Essence essência : etapa.essences()) {
            if (essência.onBlock(level, feitiço, quem, onde, face, batida)) pegou = true;
        }
        return pegou ? Result.SUCCESS : Result.EFFECT_FAILED;
    }

    // ------------------------------------------------------------------ o preço

    /** Se quem lança tem com que pagar. Quem está em criativo paga sempre. */
    public static boolean affords(LivingEntity quem, float mana, float burnout) {
        if (quem instanceof Player gente && gente.hasInfiniteMaterials()) return true;
        if (!(quem instanceof Player gente)) return true;
        Mana conta = Mana.of(gente);
        return conta.has(mana) && conta.canBurn(burnout);
    }

    /** E cobra. */
    public static void charge(LivingEntity quem, float mana, float burnout) {
        if (!(quem instanceof Player gente) || gente.hasInfiniteMaterials()) return;
        Mana.set(gente, Mana.of(gente).spend(mana, burnout));
    }

    // ------------------------------------------------------------------ a Afinidade

    /**
     * O quanto o feitiço que pegou puxa quem o lançou: o trecho do {@code SpellUtils.doAffinityShift}.
     *
     * <p>Cada Essência da etapa puxa para a Afinidade dela, e o quanto ela puxa é multiplicado pelo
     * <b>retorno decrescente</b> de quem lançou e por <b>cinco</b> — o {@code × 5.0F} do original. Feitiço
     * canalizado puxa um quarto disso.
     *
     * <p>E no fim, o retorno decrescente <b>desce</b>: quem despeja feitiços seguidos não ganha Afinidade
     * nenhuma. Ela vem de lançar ao longo de muitos dias, que é o que ela devia significar.
     */
    public static void shiftAffinity(LivingEntity quem, Spell.Stage etapa) {
        if (!(quem instanceof Player gente)) return;

        AffinityData era = AffinityData.of(gente);
        AffinityData agora = era;
        boolean canalizado = etapa.shape().channeled();

        for (SpellPart.Essence essência : etapa.essences()) {
            float puxa = essência.affinityShift() * era.falloff() * AFFINITY_FACTOR;
            if (canalizado) puxa /= 4.0f;
            if (puxa <= 0.0f) continue;
            for (Affinity qual : essência.affinities()) {
                agora = agora.increment(qual, puxa);
            }
        }

        agora = agora.spent(canalizado);
        if (!agora.equals(era)) AffinityData.set(gente, agora);
    }

    /** O {@code × 5.0F} com que o original multiplica todo deslocamento de Afinidade. */
    public static final float AFFINITY_FACTOR = 5.0f;

    private static Mana manaOf(LivingEntity quem) {
        return quem instanceof Player gente ? Mana.of(gente) : Mana.NONE;
    }
}
