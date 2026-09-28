package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.AffinityData;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.ManaClock;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;

import java.util.List;

/**
 * A Afinidade: a roda das dez, o que crescer numa custa nas outras, e o tranco que não tem volta.
 */
public class ArcanaAffinityGameTest {
    // ------------------------------------------------------------------ a roda

    /** Cada Afinidade tem uma oposta direta, quatro maiores, duas menores e duas vizinhas. */
    @GameTest
    public void theWheelIsComplete(GameTestHelper helper) {
        for (Affinity qual : Affinity.values()) {
            if (qual == Affinity.NONE) {
                if (qual.opposite() != Affinity.NONE) helper.fail("a Afinidade nenhuma não se opõe a nada");
                if (!qual.major().isEmpty() || !qual.minor().isEmpty() || !qual.adjacent().isEmpty()) {
                    helper.fail("e não tem relação nenhuma");
                }
                continue;
            }
            if (qual.opposite() == Affinity.NONE) helper.fail(qual + " devia ter uma oposta direta");
            if (qual.major().size() != 4) helper.fail(qual + " devia ter quatro opostas maiores");
            if (qual.minor().size() != 2) helper.fail(qual + " devia ter duas menores");
            if (qual.adjacent().size() != 2) helper.fail(qual + " devia ter duas vizinhas");
            if (qual.major().contains(qual) || qual.minor().contains(qual)
                    || qual.adjacent().contains(qual)) {
                helper.fail(qual + " não se opõe a si mesma");
            }
        }
        helper.succeed();
    }

    /**
     * <b>As relações não são simétricas, e é do original.</b>
     *
     * <p>O Arcano tem a Vida como oposta direta, mas a Vida tem o Ender; a Natureza tem o Relâmpago, que já
     * está tomado pelo Gelo. Esta prova existe para fixar isso: se alguém "consertar" a tabela um dia, ela
     * quebra e obriga a pessoa a ler por que está assim.
     */
    @GameTest
    public void theWheelIsNotSymmetric(GameTestHelper helper) {
        if (Affinity.ARCANE.opposite() != Affinity.LIFE) helper.fail("o Arcano se opõe à Vida");
        if (Affinity.LIFE.opposite() != Affinity.ENDER) helper.fail("mas a Vida se opõe ao Ender");
        if (Affinity.FIRE.opposite() != Affinity.WATER) helper.fail("o Fogo e a Água se opõem de verdade");
        if (Affinity.WATER.opposite() != Affinity.FIRE) helper.fail("nos dois sentidos");
        if (Affinity.NATURE.opposite() != Affinity.LIGHTNING) helper.fail("e a Natureza se opõe ao Relâmpago");
        if (Affinity.LIGHTNING.opposite() != Affinity.ICE) helper.fail("que por sua vez se opõe ao Gelo");
        helper.succeed();
    }

    /** Crescer numa Afinidade encolhe todas as outras com que ela se relaciona. */
    @GameTest
    public void growingInOneCostsTheOthers(GameTestHelper helper) {
        // parte-se de todas cheias pela metade, para haver o que perder
        AffinityData conta = AffinityData.NONE;
        for (Affinity qual : Affinity.values()) {
            if (qual != Affinity.NONE) conta = conta.with(qual, 50.0f);
        }

        AffinityData depois = conta.increment(Affinity.FIRE, 10.0f);

        if (Math.abs(depois.raw(Affinity.FIRE) - 60.0f) > 0.01f) {
            helper.fail("o Fogo devia subir dez, e está em " + depois.raw(Affinity.FIRE));
        }
        if (Math.abs(depois.raw(Affinity.WATER) - 40.0f) > 0.01f) {
            helper.fail("a Água, oposta direta, devia descer dez");
        }
        for (Affinity maior : Affinity.FIRE.major()) {
            if (Math.abs(depois.raw(maior) - 42.5f) > 0.01f) {
                helper.fail("a oposta maior " + maior + " devia descer 7,5");
            }
        }
        for (Affinity menor : Affinity.FIRE.minor()) {
            if (Math.abs(depois.raw(menor) - 45.0f) > 0.01f) {
                helper.fail("a menor " + menor + " devia descer cinco");
            }
        }
        for (Affinity vizinha : Affinity.FIRE.adjacent()) {
            if (Math.abs(depois.raw(vizinha) - 47.5f) > 0.01f) {
                helper.fail("e a vizinha " + vizinha + " devia descer 2,5");
            }
        }
        helper.succeed();
    }

    /**
     * A conta não fecha em zero, e é de propósito.
     *
     * <p>Quem soma <b>um</b> numa Afinidade tira <b>5,5</b> das outras — 1 da oposta direta, 3 das quatro
     * maiores, 1 das duas menores e 0,5 das duas vizinhas. O saldo é <b>menos 4,5</b> por ponto ganho.
     *
     * <p>Não é para render; é para doer escolher. Esta prova guarda o número.
     */
    @GameTest
    public void theWheelLosesMoreThanItGains(GameTestHelper helper) {
        AffinityData cheia = AffinityData.NONE;
        for (Affinity qual : Affinity.values()) {
            if (qual != Affinity.NONE) cheia = cheia.with(qual, 50.0f);
        }
        float antes = soma(cheia);
        float depois = soma(cheia.increment(Affinity.EARTH, 4.0f));

        // ganhou 4 e tirou 4 × 5,5 = 22 das outras: o saldo é −18
        if (Math.abs((depois - antes) + 18.0f) > 0.01f) {
            helper.fail("o saldo devia ser menos dezoito, e foi " + (depois - antes));
        }
        helper.succeed();
    }

    private static float soma(AffinityData conta) {
        float total = 0.0f;
        for (Affinity qual : Affinity.values()) total += conta.raw(qual);
        return total;
    }

    /** Nada passa do teto nem do chão. */
    @GameTest
    public void nothingLeavesTheBounds(GameTestHelper helper) {
        AffinityData zerada = AffinityData.NONE.increment(Affinity.AIR, 5.0f);
        for (Affinity qual : Affinity.values()) {
            if (zerada.raw(qual) < 0.0f) helper.fail(qual + " não fica negativa");
            if (zerada.raw(qual) > Affinity.MAX_DEPTH) helper.fail(qual + " não passa de cem");
        }
        if (Math.abs(zerada.raw(Affinity.AIR) - 5.0f) > 0.01f) helper.fail("o Ar subiu cinco");
        helper.succeed();
    }

    /** Quem chega aos cem tranca ali, e não muda mais: o {@code isLocked}. */
    @GameTest
    public void reachingTheEndLocksForever(GameTestHelper helper) {
        AffinityData conta = AffinityData.NONE.increment(Affinity.ENDER, Affinity.MAX_DEPTH);
        if (!conta.locked()) helper.fail("chegar aos cem tranca");

        AffinityData depois = conta.increment(Affinity.FIRE, 50.0f);
        if (depois.raw(Affinity.FIRE) > 0.0f) helper.fail("e trancado não se muda mais");
        if (depois != conta) helper.fail("nem se cria conta nova para isso");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o retorno decrescente

    /** Lançar gasta o retorno decrescente, e o tempo devolve. */
    @GameTest(maxTicks = 60)
    public void castingSpendsTheFalloff(GameTestHelper helper) {
        AffinityData conta = AffinityData.NONE;
        if (conta.falloff() != AffinityData.MAX_FALLOFF) helper.fail("ele começa cheio, em 1,2");

        AffinityData gasta = conta.spent(false);
        if (Math.abs(gasta.falloff() - 0.9f) > 0.001f) {
            helper.fail("um feitiço custa 0,3, e ficou em " + gasta.falloff());
        }
        AffinityData canalizada = conta.spent(true);
        if (Math.abs(canalizada.falloff() - 1.1f) > 0.001f) {
            helper.fail("e um canalizado custa 0,1");
        }

        // quatro feitiços seguidos zeram, e zerado não se ganha Afinidade nenhuma
        AffinityData seca = conta.spent(false).spent(false).spent(false).spent(false);
        if (seca.falloff() != 0.0f) helper.fail("quatro feitiços seguidos zeram o ganho");

        // e o tempo devolve
        AffinityData volta = seca.recovered().recovered();
        if (Math.abs(volta.falloff() - 0.01f) > 0.001f) helper.fail("e cada batida devolve 0,005");
        helper.succeed();
    }

    /** O relógio devolve o retorno decrescente junto com a mana. */
    @GameTest(maxTicks = 60)
    public void theClockGivesTheFalloffBack(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        AffinityData.set(quem, AffinityData.NONE.spent(false));
        Mana.set(quem, Mana.NONE);

        float antes = AffinityData.of(quem).falloff();
        ManaClock.tick(quem);
        float depois = AffinityData.of(quem).falloff();

        // vinte batidas de uma vez: 20 × 0,005 = 0,1
        if (Math.abs(depois - (antes + 0.1f)) > 0.001f) {
            helper.fail("uma batida do relógio devolve 0,1, e devolveu " + (depois - antes));
        }

        AffinityData.set(quem, AffinityData.NONE);
        Mana.set(quem, Mana.NONE);
        helper.succeed();
    }

    // ------------------------------------------------------------------ lançar puxa

    /** A Afinidade de um feitiço se conta das Essências dele: o {@code mainAffinityFor}. */
    @GameTest
    public void aSpellKnowsItsOwnAffinity(GameTestHelper helper) {
        if (Spell.of(Shapes.TOUCH, Essences.FIRE_DAMAGE).mainAffinity() != Affinity.FIRE) {
            helper.fail("um feitiço de fogo é de Fogo");
        }
        if (Spell.of(Shapes.TOUCH, Essences.HEAL).mainAffinity() != Affinity.LIFE) {
            helper.fail("e um de cura é de Vida");
        }
        // a Luz não puxa para lado nenhum
        if (Spell.of(Shapes.TOUCH, Essences.LIGHT).mainAffinity() != Affinity.NONE) {
            helper.fail("e a Luz não é de Afinidade nenhuma");
        }
        // e quem aparece mais vezes ganha
        Spell dobrado = new Spell(List.of(
                new Spell.Stage(Shapes.TOUCH, List.of(Essences.FIRE_DAMAGE, Essences.FROST_DAMAGE),
                        List.of()),
                new Spell.Stage(Shapes.AOE, List.of(Essences.FIRE_DAMAGE), List.of())));
        if (dobrado.mainAffinity() != Affinity.FIRE) {
            helper.fail("dois fogos contra um gelo dá Fogo");
        }
        helper.succeed();
    }

    /** E lançar um feitiço que pegou puxa quem o lançou. */
    @GameTest(maxTicks = 60)
    public void castingPullsTheCaster(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        AffinityData.set(quem, AffinityData.NONE);
        Mana.set(quem, Mana.NONE.withLevel(40));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));

        quem.setHealth(quem.getMaxHealth() - 6.0f);
        var saiu = SpellCast.cast(helper.getLevel(), Spell.of(Shapes.SELF, Essences.HEAL), quem, null,
                quem.getEyePosition());
        if (!saiu.ok()) {
            helper.fail("a cura devia pegar, e deu " + saiu);
            limpa(quem);
            return;
        }

        AffinityData conta = AffinityData.of(quem);
        // 0,05 de deslocamento × 1,2 de retorno × 5 = 0,3
        if (Math.abs(conta.raw(Affinity.LIFE) - 0.3f) > 0.001f) {
            helper.fail("a Vida devia subir 0,3, e subiu " + conta.raw(Affinity.LIFE));
        }
        if (conta.falloff() >= AffinityData.MAX_FALLOFF) {
            helper.fail("e o retorno decrescente devia ter descido");
        }
        if (conta.highest() != Affinity.LIFE) helper.fail("e a maior devia ser a Vida");

        limpa(quem);
        helper.succeed();
    }

    /** Um feitiço que não pegou não puxa nada: quem falhou não aprendeu. */
    @GameTest(maxTicks = 60)
    public void aSpellThatMissesPullsNothing(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        AffinityData.set(quem, AffinityData.NONE);
        Mana.set(quem, Mana.NONE.withLevel(40));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));

        // curar quem está inteiro não pega
        quem.setHealth(quem.getMaxHealth());
        SpellCast.cast(helper.getLevel(), Spell.of(Shapes.SELF, Essences.HEAL), quem, null,
                quem.getEyePosition());

        AffinityData conta = AffinityData.of(quem);
        if (conta.raw(Affinity.LIFE) != 0.0f) helper.fail("um feitiço que falha não puxa nada");
        if (conta.falloff() != AffinityData.MAX_FALLOFF) {
            helper.fail("nem gasta o retorno decrescente");
        }

        limpa(quem);
        helper.succeed();
    }

    private static void limpa(ServerPlayer quem) {
        AffinityData.set(quem, AffinityData.NONE);
        Mana.set(quem, Mana.NONE);
    }
}
