package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaEffects;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;

import java.util.List;

/**
 * Os efeitos do Ars Arcana e as essências que os põem.
 *
 * <p>Vinte e tal essências do original são a mesma coisa escrita vinte e tal vezes: contam a duração, contam
 * os Poderes de Bênção, e põem um efeito. O que estas provas medem é essa conta — e o que cada efeito faz de
 * verdade nos quatro lugares por onde ele mexe em quem o tem.
 */
public class ArcanaEffectsGameTest {
    /** Uma essência de bênção põe o efeito, com meio minuto de base. */
    @GameTest(maxTicks = 80)
    public void aBlessingPutsItsEffectOn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);

        if (!Essences.HASTE.onEntity(level, Spell.of(Shapes.TOUCH, Essences.HASTE), quem, porco)) {
            helper.fail("a Pressa devia pegar");
            return;
        }
        var tem = porco.getEffect(ArcanaEffects.HASTE);
        if (tem == null) {
            helper.fail("e o porco devia ficar com ela");
            return;
        }
        if (tem.getDuration() != 600) helper.fail("meio minuto de base, e deu " + tem.getDuration());
        if (tem.getAmplifier() != 0) helper.fail("e grau zero sem Poder de Bênção nenhum");

        porco.discard();
        helper.succeed();
    }

    /** A Duração multiplica o tempo, e o Poder de Bênção sobe o grau. */
    @GameTest(maxTicks = 80)
    public void durationAndBuffPowerChangeIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);

        Spell forte = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.HASTE),
                List.of(Modifiers.DURATION, Modifiers.BUFF_POWER, Modifiers.BUFF_POWER))));
        Essences.HASTE.onEntity(level, forte, quem, porco);

        var tem = porco.getEffect(ArcanaEffects.HASTE);
        if (tem == null) {
            helper.fail("devia pegar");
            return;
        }
        // 600 × 2,2 = 1320
        if (tem.getDuration() != 1320) {
            helper.fail("a Duração multiplica por 2,2: esperava 1320 e deu " + tem.getDuration());
        }
        if (tem.getAmplifier() != 2) helper.fail("e dois Poderes de Bênção fazem grau dois");

        porco.discard();
        helper.succeed();
    }

    /** Os degraus da Pressa não são uma escada de passos iguais: 0,2, 0,45 e 0,9. */
    @GameTest(maxTicks = 80)
    public void hasteHasThreeUnevenSteps(GameTestHelper helper) {
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        double parado = porco.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

        porco.addEffect(new MobEffectInstance(ArcanaEffects.HASTE, 200, 0));
        double um = porco.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        porco.removeEffect(ArcanaEffects.HASTE);

        porco.addEffect(new MobEffectInstance(ArcanaEffects.HASTE, 200, 2));
        double três = porco.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

        if (Math.abs(um - parado * 1.2) > 1.0e-6) helper.fail("o primeiro degrau é 0,2");
        if (Math.abs(três - parado * 1.9) > 1.0e-6) helper.fail("e o terceiro é 0,9");

        porco.discard();
        helper.succeed();
    }

    /** O Gelado é o contrário, com os degraus dele. */
    @GameTest(maxTicks = 80)
    public void frostSlowsTheOtherWay(GameTestHelper helper) {
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        double parado = porco.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

        porco.addEffect(new MobEffectInstance(ArcanaEffects.FROST_SLOW, 200, 1));
        double meio = porco.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (Math.abs(meio - parado * 0.5) > 1.0e-6) helper.fail("o segundo degrau do gelo é 0,5 para trás");

        porco.discard();
        helper.succeed();
    }

    /** Quem está calado não lança nada — e não paga nada por isso. */
    @GameTest(maxTicks = 80)
    public void silenceStopsEverything(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // um jogador de mentira do servidor não aguenta receber um efeito: ele tenta avisar a ligação dele,
        // e não há nenhuma. O de mentira simples serve para tudo o que esta prova mede.
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        Mana.set(quem, new Mana(30, 5000.0f, 0.0f));
        float tinha = Mana.of(quem).mana();

        quem.addEffect(new MobEffectInstance(ArcanaEffects.SILENCE, 200, 0));
        var saiu = SpellCast.cast(level, Spell.of(Shapes.SELF, Essences.HEAL), quem, quem, quem.position());

        if (saiu != SpellCast.Result.SILENCED) helper.fail("calado não lança, e deu " + saiu);
        if (Mana.of(quem).mana() != tinha) helper.fail("e não paga nada por não lançar");

        quem.removeEffect(ArcanaEffects.SILENCE);
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** O Escudo Arcano deixa o dano a um quarto. */
    @GameTest(maxTicks = 80)
    public void theShieldQuartersTheDamage(GameTestHelper helper) {
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        if (ArcanaEffects.hurt(porco, porco.damageSources().generic(), 8.0f) != 8.0f) {
            helper.fail("sem escudo, o dano passa inteiro");
        }

        porco.addEffect(new MobEffectInstance(ArcanaEffects.MAGIC_SHIELD, 200, 0));
        if (ArcanaEffects.hurt(porco, porco.damageSources().generic(), 8.0f) != 2.0f) {
            helper.fail("com escudo, fica um quarto");
        }

        porco.discard();
        helper.succeed();
    }

    /** E o Escudo de Mana come a mana em vez do corpo: 250 por ponto. */
    @GameTest(maxTicks = 80)
    public void theManaShieldEatsMana(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        Mana.set(quem, new Mana(50, 5000.0f, 0.0f));
        quem.addEffect(new MobEffectInstance(ArcanaEffects.MANA_SHIELD, 200, 0));

        // com mana de sobra, bloqueia tudo — e custa cem por ponto
        float fica = ArcanaEffects.hurt(quem, quem.damageSources().generic(), 4.0f);
        if (fica != 0.0f) helper.fail("com mana de sobra, não dói nada");
        if (Math.abs(Mana.of(quem).mana() - 4600.0f) > 0.01f) {
            helper.fail("e come cem por ponto: esperava 4600 e ficou " + Mana.of(quem).mana());
        }

        // com pouca mana, come o que há e o resto dói
        Mana.set(quem, new Mana(50, 250.0f, 0.0f));
        fica = ArcanaEffects.hurt(quem, quem.damageSources().generic(), 4.0f);
        if (Math.abs(fica - 3.0f) > 0.01f) helper.fail("250 de mana aparam um ponto, e deu " + fica);
        if (Mana.of(quem).mana() != 0.0f) helper.fail("e a mana acaba");

        quem.removeEffect(ArcanaEffects.MANA_SHIELD);
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    /** A Queda de Pena apaga a queda, e o Salto perdoa uns blocos dela. */
    @GameTest(maxTicks = 80)
    public void fallingIsChangedByThem(GameTestHelper helper) {
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);

        if (ArcanaEffects.fall(porco, 20.0f) != 20.0f) helper.fail("sem nada, a queda é o que é");

        porco.addEffect(new MobEffectInstance(ArcanaEffects.SLOWFALL, 200, 0));
        if (ArcanaEffects.fall(porco, 20.0f) != 0.0f) helper.fail("com a Queda de Pena, não dói");
        porco.removeEffect(ArcanaEffects.SLOWFALL);

        porco.addEffect(new MobEffectInstance(ArcanaEffects.LEAP, 200, 0));
        if (ArcanaEffects.fall(porco, 20.0f) != 12.0f) helper.fail("o Salto perdoa oito no primeiro grau");
        porco.removeEffect(ArcanaEffects.LEAP);

        porco.addEffect(new MobEffectInstance(ArcanaEffects.GRAVITY_WELL, 200, 0));
        if (ArcanaEffects.fall(porco, 20.0f) != 30.0f) helper.fail("e o Poço faz doer uma vez e meia");

        porco.discard();
        helper.succeed();
    }

    /** A Fúria soma quatro a cada golpe, depois do fator do nível. */
    @GameTest(maxTicks = 80)
    public void furyAddsFour(GameTestHelper helper) {
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        if (ArcanaEffects.extraDamage(porco) != 0.0f) helper.fail("sem Fúria, nada se soma");

        porco.addEffect(new MobEffectInstance(ArcanaEffects.FURY, 200, 0));
        if (ArcanaEffects.extraDamage(porco) != 4.0f) helper.fail("com Fúria, somam-se quatro");

        porco.discard();
        helper.succeed();
    }

    /** O Enredar prende quem leva: nem para cima, nem para o lado. */
    @GameTest(maxTicks = 80)
    public void entangleStopsEverything(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        porco.addEffect(new MobEffectInstance(ArcanaEffects.ENTANGLED, 200, 0));
        porco.setDeltaMovement(0.5, 0.5, 0.5);

        ArcanaEffects.tick(level, porco);
        if (porco.getDeltaMovement().lengthSqr() > 1.0e-9) helper.fail("enredado não se mexe");

        porco.discard();
        helper.succeed();
    }

    /**
     * O Dissipar tira o que couber num orçamento de seis, contado pelo grau.
     *
     * <p>Um efeito de grau zero é de graça; um de grau três come metade. É o que faz dele uma coisa de usar
     * com jeito, e não um botão de apagar tudo.
     */
    @GameTest(maxTicks = 80)
    public void dispelHasABudgetOfSix(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);

        porco.addEffect(new MobEffectInstance(ArcanaEffects.HASTE, 200, 0));
        porco.addEffect(new MobEffectInstance(ArcanaEffects.FURY, 200, 2));
        porco.addEffect(new MobEffectInstance(ArcanaEffects.SHRINK, 200, 9));

        Essences.DISPEL.onEntity(level, Spell.of(Shapes.TOUCH, Essences.DISPEL), quem, porco);

        if (porco.hasEffect(ArcanaEffects.HASTE)) helper.fail("o de grau zero é de graça e sai");
        if (porco.hasEffect(ArcanaEffects.FURY)) helper.fail("o de grau dois cabe nos seis e sai");
        if (!porco.hasEffect(ArcanaEffects.SHRINK)) helper.fail("e o de grau nove não cabe e fica");

        porco.discard();
        helper.succeed();
    }

    /** O Encantar põe um bicho de criação no cio, e não o encanta. */
    @GameTest(maxTicks = 80)
    public void charmingAnAnimalPutsItInLove(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);

        if (!Essences.CHARM.onEntity(level, Spell.of(Shapes.TOUCH, Essences.CHARM), quem, porco)) {
            helper.fail("o Encantar devia pegar num porco");
        }
        if (!porco.isInLove()) helper.fail("e pô-lo no cio");
        if (porco.hasEffect(ArcanaEffects.CHARMED)) helper.fail("e não o encantar");

        porco.discard();
        helper.succeed();
    }

    /** E encanta o que não é de criação. */
    @GameTest(maxTicks = 80)
    public void charmingAMonsterCharmsIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        zumbi.setNoAi(true);

        Essences.CHARM.onEntity(level, Spell.of(Shapes.TOUCH, Essences.CHARM), quem, zumbi);
        if (!zumbi.hasEffect(ArcanaEffects.CHARMED)) helper.fail("um zumbi se encanta");
        // e não se encanta duas vezes
        if (Essences.CHARM.onEntity(level, Spell.of(Shapes.TOUCH, Essences.CHARM), quem, zumbi)) {
            helper.fail("e não se encanta quem já está encantado");
        }

        zumbi.discard();
        helper.succeed();
    }

    /** O Túmulo de Água puxa para o fundo — e só dentro d'água. */
    @GameTest(maxTicks = 80)
    public void theWateryGraveOnlyPullsInWater(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        porco.addEffect(new MobEffectInstance(ArcanaEffects.WATERY_GRAVE, 200, 0));
        porco.setDeltaMovement(0.0, 0.0, 0.0);

        ArcanaEffects.tick(level, porco);
        if (porco.getDeltaMovement().y < 0.0) helper.fail("fora d'água não puxa nada");

        porco.discard();
        helper.succeed();
    }

    /** Todas as essências de bênção têm efeito, item e nome. */
    @GameTest
    public void everyBlessingIsComplete(GameTestHelper helper) {
        for (var peça : net.thaumcraft.arcana.SpellParts.essences()) {
            if (net.thaumcraft.arcana.ArcanaItems.itemOf(peça) == null) {
                helper.fail("sem item: " + peça.name());
            }
        }
        helper.succeed();
    }

    /** E o Silêncio e a Distorção Astral são perguntas que os outros fazem. */
    @GameTest(maxTicks = 80)
    public void theTwoQuestionsAnswerRight(GameTestHelper helper) {
        LivingEntity porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        if (ArcanaEffects.silenced(porco)) helper.fail("sem Silêncio, fala-se");
        if (ArcanaEffects.blocksTeleport(porco)) helper.fail("sem Distorção, se anda");

        porco.addEffect(new MobEffectInstance(ArcanaEffects.SILENCE, 200, 0));
        porco.addEffect(new MobEffectInstance(ArcanaEffects.ASTRAL_DISTORTION, 200, 0));
        if (!ArcanaEffects.silenced(porco)) helper.fail("com Silêncio, não");
        if (!ArcanaEffects.blocksTeleport(porco)) helper.fail("e com Distorção, também não");

        porco.discard();
        helper.succeed();
    }
}
