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
        EFFECT_FAILED,
        /** Quem ia lançar está <b>calado</b>: tem o Silêncio, e não lança nada. */
        SILENCED,
        /** A frase leva uma essência que quem a lança <b>ainda não descobriu</b>. */
        UNDISCOVERED;

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
        return cast(level, feitiço, quem, alvo, onde, 0);
    }

    /**
     * A mesma coisa, dizendo há quantas batidas se está segurando um feitiço canalizado.
     *
     * <p>Um feitiço comum é lançado com zero e nunca olha para esse número. O Facho olha, e é o que lhe
     * deixa ferir de dez em dez batidas em vez de a cada uma.
     */
    public static Result cast(ServerLevel level, Spell feitiço, LivingEntity quem, @Nullable Entity alvo,
                              Vec3 onde, int batidas) {
        if (feitiço.isEmpty()) return Result.SUCCESS;
        Spell.Stage etapa = feitiço.first();
        if (etapa == null) return Result.MALFORMED;

        // e quem está calado não lança nada, nem paga nada por isso
        if (ArcanaEffects.silenced(quem)) return Result.SILENCED;

        // uma essência de segredo não sai da mão de quem não a descobriu
        if (SpellUnlocks.tranca(feitiço, quem)) return Result.UNDISCOVERED;

        // e uma frase com a combinação certa abre o segredo, pegue ela em alguma coisa ou não
        SpellUnlocks.descobre(feitiço, quem);

        // o Arcano desconta cinco por cento da mana e do desgaste, acima de meio
        float desconto = AffinityEffects.manaDiscount(quem);
        float custo = feitiço.manaCost(quem, alvo) * desconto;
        float desgaste = feitiço.burnout() * desconto;
        if (!affords(quem, custo, desgaste)) {
            return manaOf(quem).canBurn(desgaste) ? Result.NOT_ENOUGH_MANA : Result.BURNED_OUT;
        }

        Result saiu = etapa.shape().begin(level, feitiço, quem, alvo, onde, batidas);
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

            // e o mesmo feitiço que puxa a Afinidade ensina alguma coisa a quem o lançou
            float aprende = XP_PER_ESSENCE * era.falloff();
            if (canalizado) aprende /= 4.0f;
            learn(gente, aprende);
        }

        agora = agora.spent(canalizado);
        if (!agora.equals(era)) AffinityData.set(gente, agora);
    }

    /**
     * Quanta experiência mágica dá cada Essência lançada: os cinco centésimos do original.
     *
     * <p>Como o deslocamento de Afinidade, ela é multiplicada pelo retorno decrescente — quem despeja
     * feitiços seguidos não aprende nada. Subir de nível é coisa de muitos dias, e é de propósito.
     */
    public static final float XP_PER_ESSENCE = 0.05f;

    /**
     * Soma experiência mágica a quem lançou, e dá o ponto se ele subiu de nível.
     *
     * <p>O original dá <b>um ponto a cada dois níveis</b>, e a cor dele depende de onde se está: azul até o
     * vinte, verde até o quarenta, vermelho até o cinquenta. Aqui os pontos não se guardam — eles se
     * <b>contam</b> do nível, e o que se guarda é quantos já se gastaram. Dá no mesmo e não há como os
     * perder.
     */
    public static void learn(Player quem, float quanto) {
        if (quem.hasInfiniteMaterials() || quanto <= 0.0f) return;
        Mana era = Mana.of(quem);
        Mana agora = era.addXp(quanto);
        if (agora.equals(era)) return;
        Mana.set(quem, agora);

        if (agora.level() > era.level() && quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "tc.spell.level_up", agora.level()));
            for (int nível = era.level() + 1; nível <= agora.level(); nível++) {
                SkillTree.Point ponto = SkillTree.pointFor(nível);
                if (ponto == null) continue;
                gente.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "tc.spell.point_gained",
                        net.minecraft.network.chat.Component.translatable(ponto.key()))
                        .withStyle(estilo -> estilo.withColor(ponto.color)));
            }
        }
    }

    /** O {@code × 5.0F} com que o original multiplica todo deslocamento de Afinidade. */
    public static final float AFFINITY_FACTOR = 5.0f;

    private static Mana manaOf(LivingEntity quem) {
        return quem instanceof Player gente ? Mana.of(gente) : Mana.NONE;
    }
}
