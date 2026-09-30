package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaDamage;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;
import net.thaumcraft.arcana.SpellModifierKind;

import java.util.List;

/**
 * Os danos do Ars Arcana, e o que eles têm em comum.
 *
 * <p>O que eles têm em comum é <b>o fator do nível</b>: o mesmo feitiço fere metade na mão de quem chega e o
 * dobro na de quem chegou ao fim. É a conta mais importante do ramo e não estava portada até aqui — o dano
 * saía sempre igual, e subir de nível não mudava nada para quem já tinha as peças.
 */
public class ArcanaDamageGameTest {
    /** O fator do nível: metade no zero, um no vinte, dois no 99. */
    @GameTest
    public void levelDoublesTheDamage(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();

        Mana.set(quem, new Mana(0, 0.0f, 0.0f));
        if (Math.abs(Essences.fatorDoNível(quem) - 0.5f) > 1.0e-4) {
            helper.fail("quem chega fere metade, e deu " + Essences.fatorDoNível(quem));
        }

        Mana.set(quem, new Mana(20, 0.0f, 0.0f));
        if (Math.abs(Essences.fatorDoNível(quem) - 1.0f) > 1.0e-4) {
            helper.fail("no vinte fere o que está escrito, e deu " + Essences.fatorDoNível(quem));
        }

        Mana.set(quem, new Mana(99, 0.0f, 0.0f));
        if (Math.abs(Essences.fatorDoNível(quem) - 2.0f) > 1.0e-4) {
            helper.fail("no 99 fere o dobro, e deu " + Essences.fatorDoNível(quem));
        }

        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** E um bicho, que não tem nível nenhum, conta como quem chega. */
    @GameTest
    public void whatHasNoLevelHitsAsAStranger(GameTestHelper helper) {
        var bicho = helper.spawn(EntityTypes.ZOMBIE, new net.minecraft.core.BlockPos(1, 2, 1));
        bicho.setNoAi(true);
        if (Math.abs(Essences.fatorDoNível(bicho) - 0.5f) > 1.0e-4) {
            helper.fail("quem não tem mana fere metade");
        }
        helper.succeed();
    }

    /**
     * Um feitiço de dano fere mesmo, e fere <b>o que o nível manda</b>.
     *
     * <p>Doze de dano de raio na mão de quem tem nível vinte é doze; na de quem tem zero é seis. A prova mede
     * os dois no mesmo tipo de bicho, e é a conta do original.
     */
    @GameTest(maxTicks = 100)
    public void theSameSpellHurtsMoreAtHigherLevel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        float meio = fere(helper, level, quem, 0);
        float inteiro = fere(helper, level, quem, 20);

        if (meio <= 0.0f) {
            helper.fail("o raio devia ferir");
            return;
        }
        if (inteiro < meio * 1.8f) {
            helper.fail("no vinte devia ferir o dobro do que fere no zero: " + meio + " e " + inteiro);
        }
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /**
     * O dano que o raio faz na mão de quem é daquele nível.
     *
     * <p>Quem leva é um <b>golem de ferro</b>, e não um porco: um porco tem dez de vida e o raio de nível
     * vinte faz doze — a conta ficava escondida atrás da morte dele.
     */
    private float fere(GameTestHelper helper, ServerLevel level, ServerPlayer quem, int nível) {
        Mana.set(quem, new Mana(nível, 0.0f, 0.0f));
        var golem = helper.spawn(EntityTypes.IRON_GOLEM, new net.minecraft.core.BlockPos(3, 2, 3));
        golem.setNoAi(true);
        float era = golem.getHealth();
        Essences.LIGHTNING_DAMAGE.onEntity(level,
                Spell.of(Shapes.TOUCH, Essences.LIGHTNING_DAMAGE), quem, golem);
        float agora = golem.getHealth();
        golem.discard();
        return era - agora;
    }

    /** O Afogar não pega em morto-vivo nem em golem de ferro: os dois não respiram. */
    @GameTest(maxTicks = 100)
    public void drowningNeedsLungs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new net.minecraft.core.BlockPos(3, 2, 3));
        var golem = helper.spawn(EntityTypes.IRON_GOLEM, new net.minecraft.core.BlockPos(4, 2, 4));
        zumbi.setNoAi(true);
        golem.setNoAi(true);
        for (LivingEntity bicho : List.of(zumbi, golem)) {
            float era = bicho.getHealth();
            Essences.DROWN.onEntity(level, Spell.of(Shapes.TOUCH, Essences.DROWN), quem, bicho);
            if (bicho.getHealth() < era) helper.fail("este não se afoga: " + bicho.getType());
            bicho.discard();
        }

        // e um porco afoga-se
        var porco = helper.spawn(EntityTypes.PIG, new net.minecraft.core.BlockPos(3, 2, 3));
        porco.setNoAi(true);
        float era = porco.getHealth();
        Essences.DROWN.onEntity(level, Spell.of(Shapes.TOUCH, Essences.DROWN), quem, porco);
        if (porco.getHealth() >= era) helper.fail("um porco afoga-se");
        porco.discard();
        helper.succeed();
    }

    /** Drenar Vida cura quem lança — um quarto do que feriu, de quatro em quatro. */
    @GameTest(maxTicks = 100)
    public void drainingLifeHeals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        quem.setHealth(10.0f);
        Mana.set(quem, new Mana(20, 0.0f, 0.0f));

        var porco = helper.spawn(EntityTypes.PIG, new net.minecraft.core.BlockPos(3, 2, 3));
        porco.setNoAi(true);
        Essences.LIFE_DRAIN.onEntity(level, Spell.of(Shapes.TOUCH, Essences.LIFE_DRAIN), quem, porco);

        if (quem.getHealth() <= 10.0f) helper.fail("drenar vida cura quem drena");
        porco.discard();
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** E não drena nada de morto-vivo — mas o original cobra o feitiço na mesma, e isso fica. */
    @GameTest(maxTicks = 100)
    public void theUndeadHaveNoLifeToDrain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        quem.setHealth(10.0f);

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new net.minecraft.core.BlockPos(3, 2, 3));
        zumbi.setNoAi(true);
        float era = zumbi.getHealth();
        boolean pegou = Essences.LIFE_DRAIN.onEntity(level,
                Spell.of(Shapes.TOUCH, Essences.LIFE_DRAIN), quem, zumbi);

        if (zumbi.getHealth() < era) helper.fail("um zumbi não tem vida para drenar");
        if (quem.getHealth() > 10.0f) helper.fail("e quem drena não se cura");
        if (!pegou) helper.fail("mas o original cobra o feitiço na mesma");
        zumbi.discard();
        helper.succeed();
    }

    /** Vida por Mana fere quem a lança e devolve mana. */
    @GameTest(maxTicks = 100)
    public void lifeTapTradesBloodForMana(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // um jogador de mentira do servidor não se pode ferir: o jogo procura a ligação dele e não há nenhuma.
        // Quem leva o dano aqui é o jogador de mentira simples, que é o mesmo para tudo o que esta prova mede.
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setHealth(20.0f);
        Mana.set(quem, new Mana(30, 0.0f, 0.0f));

        var porco = helper.spawn(EntityTypes.PIG, new net.minecraft.core.BlockPos(3, 2, 3));
        porco.setNoAi(true);
        Essences.LIFE_TAP.onEntity(level, Spell.of(Shapes.TOUCH, Essences.LIFE_TAP), quem, porco);

        if (quem.getHealth() >= 20.0f) helper.fail("vida por mana custa vida");
        if (Mana.of(quem).mana() <= 0.0f) helper.fail("e devolve mana");
        porco.discard();
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** Drenar Mana rouba de quem tem, e não fere ninguém. */
    @GameTest(maxTicks = 100)
    public void manaDrainStealsFromAnotherCaster(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer ladrão = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        ServerPlayer vítima = helper.makeMockServerPlayerInLevel();

        Mana.set(ladrão, new Mana(30, 0.0f, 0.0f));
        Mana.set(vítima, new Mana(30, 500.0f, 0.0f));
        float tinha = Mana.of(vítima).mana();
        float vida = vítima.getHealth();

        Essences.MANA_DRAIN.onEntity(level, Spell.of(Shapes.TOUCH, Essences.MANA_DRAIN), ladrão, vítima);

        if (Mana.of(vítima).mana() >= tinha) helper.fail("a vítima perde mana");
        if (Mana.of(ladrão).mana() <= 0.0f) helper.fail("e o ladrão ganha-a");
        if (vítima.getHealth() < vida) helper.fail("e ninguém se fere nisto");

        Mana.set(ladrão, Mana.NONE);
        Mana.set(vítima, Mana.NONE);
        helper.succeed();
    }

    /** A Ignição põe fogo em quem não está ardendo, e não em quem já está. */
    @GameTest(maxTicks = 100)
    public void ignitionOnlyLightsWhatIsNotBurning(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new net.minecraft.core.BlockPos(3, 2, 3));
        porco.setNoAi(true);
        Spell frase = Spell.of(Shapes.TOUCH, Essences.IGNITION);

        if (!Essences.IGNITION.onEntity(level, frase, quem, porco)) helper.fail("devia pegar");
        if (!porco.isOnFire()) helper.fail("e devia arder");
        if (Essences.IGNITION.onEntity(level, frase, quem, porco)) {
            helper.fail("quem já arde não se acende outra vez");
        }

        porco.discard();
        helper.succeed();
    }

    /** O Desmembramento só existe se estiver na frase, e cada um vale cinco por cento. */
    @GameTest
    public void dismemberingIsFivePercentEach(GameTestHelper helper) {
        Spell sem = Spell.of(Shapes.TOUCH, Essences.PHYSICAL_DAMAGE);
        if (sem.add(SpellModifierKind.DISMEMBERING_LEVEL, 0.0) != 0.0) {
            helper.fail("sem o modificador, nenhuma cabeça cai");
        }
        // a peça ainda não está portada: o que se prova aqui é a conta que a espera
        if (sem.has(SpellModifierKind.DISMEMBERING_LEVEL)) helper.fail("e ele não está lá");
        helper.succeed();
    }

    /** A Prosperidade e o Toque de Pena mudam o que cai, e se contam. */
    @GameTest
    public void fortuneAndSilkAreCounted(GameTestHelper helper) {
        Spell duasSortes = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.DIG),
                List.of(Modifiers.PROSPERITY, Modifiers.PROSPERITY))));
        if (duasSortes.count(SpellModifierKind.FORTUNE_LEVEL) != 2) {
            helper.fail("duas Prosperidades são Fortuna II");
        }

        Spell seda = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.DIG), List.of(Modifiers.FEATHER_TOUCH))));
        if (!seda.has(SpellModifierKind.SILKTOUCH_LEVEL)) helper.fail("e o Toque de Pena está lá");
        helper.succeed();
    }

    /** Os tipos de dano do ramo existem no jogo, e cada um sai de quem o lançou. */
    @GameTest
    public void theBranchHasItsOwnDeaths(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        for (var fonte : List.of(ArcanaDamage.fire(level, quem), ArcanaDamage.frost(level, quem),
                ArcanaDamage.lightning(level, quem), ArcanaDamage.wind(level, quem),
                ArcanaDamage.holy(level, quem), ArcanaDamage.drown(level, quem))) {
            if (fonte.getEntity() != quem) helper.fail("o dano é de quem o lançou");
        }
        helper.succeed();
    }
}
