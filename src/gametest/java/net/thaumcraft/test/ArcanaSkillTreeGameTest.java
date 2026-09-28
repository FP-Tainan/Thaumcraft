package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.SkillData;
import net.thaumcraft.arcana.SkillTree;
import net.thaumcraft.arcana.SpellPart;
import net.thaumcraft.arcana.SpellParts;

import java.util.HashSet;

/**
 * A árvore de perícias: o que dá curva ao ramo.
 *
 * <p>Sem ela um arcanista nasce sabendo tudo. Com ela, começa sabendo três Formas e compra o resto com o que
 * aprende lançando — e <b>não dá para ter tudo</b>.
 */
public class ArcanaSkillTreeGameTest {
    // ------------------------------------------------------------------ o quadro

    /** Toda peça da gramática está na árvore, e toda perícia da árvore é uma peça. */
    @GameTest
    public void everyPartIsInTheTree(GameTestHelper helper) {
        for (SpellPart peça : SpellParts.shapes()) esperada(helper, peça);
        for (SpellPart peça : SpellParts.essences()) esperada(helper, peça);
        for (SpellPart peça : SpellParts.modifiers()) esperada(helper, peça);

        if (SkillTree.entries().size() != SpellParts.count()) {
            helper.fail("há " + SpellParts.count() + " peças e " + SkillTree.entries().size() + " perícias");
        }
        helper.succeed();
    }

    private static void esperada(GameTestHelper helper, SpellPart peça) {
        if (SkillTree.of(peça) == null) helper.fail(peça.name() + " devia estar na árvore");
    }

    /**
     * A árvore é <b>alcançável</b>: toda perícia tem caminho até uma raiz.
     *
     * <p>É a prova que mais vale aqui. Os pré-requisitos do original passam por 89 perícias que este porte não
     * tem, e eles foram refeitos subindo o grafo até os ancestrais portados — se essa conta estivesse errada,
     * haveria peças que nunca se poderiam comprar, e ninguém daria por isso jogando.
     */
    @GameTest
    public void everySkillIsReachable(GameTestHelper helper) {
        var alcançáveis = new HashSet<SpellPart>();
        for (var raiz : SkillTree.roots()) alcançáveis.add(raiz.part());
        if (alcançáveis.isEmpty()) {
            helper.fail("devia haver raízes");
            return;
        }

        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (var perícia : SkillTree.entries()) {
                if (alcançáveis.contains(perícia.part())) continue;
                if (perícia.needs().stream().allMatch(alcançáveis::contains)) {
                    alcançáveis.add(perícia.part());
                    mudou = true;
                }
            }
        }

        for (var perícia : SkillTree.entries()) {
            if (!alcançáveis.contains(perícia.part())) {
                helper.fail(perícia.part().name() + " não se alcança de raiz nenhuma");
            }
        }
        helper.succeed();
    }

    /** Nenhuma perícia pede a si mesma, e nenhuma pede algo que não está na árvore. */
    @GameTest
    public void thePrerequisitesAreSane(GameTestHelper helper) {
        for (var perícia : SkillTree.entries()) {
            for (var precisa : perícia.needs()) {
                if (precisa == perícia.part()) helper.fail(perícia.part().name() + " pede a si mesma");
                if (SkillTree.of(precisa) == null) {
                    helper.fail(perícia.part().name() + " pede " + precisa.name() + ", que não está na árvore");
                }
            }
        }
        helper.succeed();
    }

    /** As raízes são as três Formas de começo, mais os Alvos Não Sólidos. */
    @GameTest
    public void theRootsAreTheStartingShapes(GameTestHelper helper) {
        var raízes = SkillTree.roots().stream().map(SkillTree.Entry::part).collect(java.util.stream.Collectors.toSet());
        for (var qual : java.util.List.of(Shapes.SELF, Shapes.TOUCH, Shapes.PROJECTILE)) {
            if (!raízes.contains(qual)) helper.fail("a " + qual.name() + " devia ser raiz");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ os pontos

    /** Um ponto a cada dois níveis, e a cor muda com o tempo. */
    @GameTest
    public void pointsComeEveryTwoLevels(GameTestHelper helper) {
        if (SkillTree.pointFor(1) != null) helper.fail("nível ímpar não dá ponto");
        if (SkillTree.pointFor(2) != SkillTree.Point.BLUE) helper.fail("o nível dois dá azul");
        if (SkillTree.pointFor(20) != SkillTree.Point.BLUE) helper.fail("o vinte ainda é azul");
        if (SkillTree.pointFor(22) != SkillTree.Point.GREEN) helper.fail("o vinte e dois já é verde");
        if (SkillTree.pointFor(40) != SkillTree.Point.GREEN) helper.fail("o quarenta ainda é verde");
        if (SkillTree.pointFor(42) != SkillTree.Point.RED) helper.fail("o quarenta e dois é vermelho");
        if (SkillTree.pointFor(50) != SkillTree.Point.RED) helper.fail("o cinquenta ainda é vermelho");
        if (SkillTree.pointFor(52) != null) helper.fail("e depois do cinquenta não dá mais nada");
        helper.succeed();
    }

    /**
     * <b>Não dá para ter tudo</b>, e é de propósito.
     *
     * <p>São 25 pontos ao todo — três azuis de começo, mais um a cada dois níveis até o cinquenta — para 32
     * perícias. Quem quer o ramo inteiro tem de escolher o que deixar de lado.
     */
    @GameTest
    public void youCannotHaveEverything(GameTestHelper helper) {
        int todos = 0;
        for (var cor : SkillTree.Point.values()) todos += SkillTree.pointsUpTo(cor, SkillTree.RED_UNTIL);
        if (todos >= SkillTree.entries().size()) {
            helper.fail("devia haver menos pontos (" + todos + ") que perícias ("
                    + SkillTree.entries().size() + ")");
        }
        // três de começo, mais dez azuis, dez verdes e cinco vermelhos
        if (SkillTree.pointsUpTo(SkillTree.Point.BLUE, 50) != 13) helper.fail("treze azuis ao todo");
        if (SkillTree.pointsUpTo(SkillTree.Point.GREEN, 50) != 10) helper.fail("dez verdes");
        if (SkillTree.pointsUpTo(SkillTree.Point.RED, 50) != 5) helper.fail("cinco vermelhos");
        helper.succeed();
    }

    /** Quem começa tem três pontos azuis, que é o que dá para as três Formas de começo. */
    @GameTest(maxTicks = 60)
    public void everyoneStartsWithThreeBluePoints(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);

        SkillData sabe = SkillData.of(quem);
        if (sabe.free(SkillTree.Point.BLUE, 0) != SkillTree.STARTING_BLUE) {
            helper.fail("três azuis de começo, e há " + sabe.free(SkillTree.Point.BLUE, 0));
        }
        if (sabe.free(SkillTree.Point.GREEN, 0) != 0) helper.fail("e nenhum verde");

        // e dão para as três Formas de começo
        for (var forma : java.util.List.of(Shapes.SELF, Shapes.TOUCH, Shapes.PROJECTILE)) {
            var perícia = SkillTree.of(forma);
            if (!sabe.canLearn(perícia, 0)) helper.fail("a " + forma.name() + " devia estar ao alcance");
            sabe = sabe.learn(perícia, 0);
        }
        if (sabe.free(SkillTree.Point.BLUE, 0) != 0) helper.fail("e gastam os três");

        limpa(quem);
        helper.succeed();
    }

    // ------------------------------------------------------------------ aprender

    /** Não se aprende o que se não pode: sem ponto, sem pré-requisito, ou já sabido. */
    @GameTest(maxTicks = 60)
    public void youCannotLearnWhatYouCannot(GameTestHelper helper) {
        SkillData sabe = SkillData.NONE;

        // o Dano de Fogo precisa do Projétil
        var fogo = SkillTree.of(Essences.FIRE_DAMAGE);
        if (sabe.canLearn(fogo, 10)) helper.fail("o Dano de Fogo precisa do Projétil antes");

        sabe = sabe.learn(SkillTree.of(Shapes.PROJECTILE), 10);
        if (!sabe.knows(Shapes.PROJECTILE)) helper.fail("o Projétil aprende-se");
        if (!sabe.canLearn(fogo, 10)) helper.fail("e agora o Dano de Fogo está ao alcance");

        // e não se aprende duas vezes
        sabe = sabe.learn(SkillTree.of(Shapes.PROJECTILE), 10);
        if (sabe.used(SkillTree.Point.BLUE) != 1) helper.fail("aprender duas vezes não gasta dois pontos");

        // uma vermelha não se compra cedo por mais azuis que se tenha
        var dano = SkillTree.of(net.thaumcraft.arcana.Modifiers.DAMAGE);
        if (dano.point() != SkillTree.Point.RED) helper.fail("o Dano custa vermelho");
        if (sabe.free(SkillTree.Point.RED, 10) != 0) helper.fail("e no nível dez não há vermelho nenhum");
        helper.succeed();
    }

    /** Aprender gasta o ponto da cor certa, e só dessa. */
    @GameTest
    public void learningSpendsTheRightColour(GameTestHelper helper) {
        SkillData sabe = SkillData.NONE.learn(SkillTree.of(Shapes.SELF), 50);
        if (sabe.used(SkillTree.Point.BLUE) != 1) helper.fail("a Autoconjuração custa um azul");
        if (sabe.used(SkillTree.Point.GREEN) != 0) helper.fail("e nenhum verde");
        if (sabe.used(SkillTree.Point.RED) != 0) helper.fail("e nenhum vermelho");
        helper.succeed();
    }

    /** E há um item para cada perícia, que é o que se recebe ao aprender. */
    @GameTest
    public void everySkillHasItsItem(GameTestHelper helper) {
        for (var perícia : SkillTree.entries()) {
            if (ArcanaItems.itemOf(perícia.part()) == null) {
                helper.fail("a perícia " + perícia.part().name() + " devia ter o seu item");
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ a experiência

    /** Lançar dá experiência, e a experiência dá nível. */
    @GameTest
    public void castingTeaches(GameTestHelper helper) {
        Mana conta = new Mana(1, 0.0f, 0.0f, 0.0f);
        float falta = conta.xpToNextLevel();
        if (falta <= 0.0f) helper.fail("do nível um para o dois falta alguma coisa");

        Mana subiu = conta.addXp(falta);
        if (subiu.level() != 2) helper.fail("e com ela sobe-se, e ficou no " + subiu.level());
        if (subiu.xp() != 0.0f) helper.fail("e a experiência volta a zero");

        // a conta cresce: do noventa e oito para o noventa e nove custa muito mais
        Mana quase = new Mana(98, 0.0f, 0.0f, 0.0f);
        if (quase.xpToNextLevel() <= falta * 100) helper.fail("e cresce muito com o nível");

        // e no noventa e nove para
        Mana cheio = new Mana(Mana.MAX_LEVEL, 0.0f, 0.0f, 0.0f);
        if (!cheio.addXp(1000.0f).equals(cheio)) helper.fail("no noventa e nove não se sobe mais");
        helper.succeed();
    }

    /**
     * Uma pilha de experiência de uma vez sobe <b>um</b> nível só, e o resto <b>perde-se</b>.
     *
     * <p>É o original: o {@code addMagicXP} zera a experiência ao subir, sem guardar o excesso. Esta prova
     * existe para fixar isso — parece um erro e não é, e quem vier "consertar" tem de ler antes.
     */
    @GameTest
    public void extraXpIsThrownAway(GameTestHelper helper) {
        Mana conta = new Mana(1, 0.0f, 0.0f, 0.0f);
        Mana depois = conta.addXp(50.0f);
        if (depois.level() != 2) helper.fail("cinquenta de uma vez sobe um nível só, e subiu para " + depois.level());
        if (depois.xp() != 0.0f) helper.fail("e o que sobrou perde-se, e ficaram " + depois.xp());

        // e subir de verdade é lançar muitas vezes: cada Essência dá cinco centésimos
        Mana devagar = new Mana(1, 0.0f, 0.0f, 0.0f);
        int lançou = 0;
        while (devagar.level() == 1 && lançou < 1000) {
            devagar = devagar.addXp(0.05f);
            lançou++;
        }
        if (devagar.level() != 2) helper.fail("mil feitiços deviam subir um nível");
        if (lançou < 2) helper.fail("e não basta um: foram " + lançou);
        helper.succeed();
    }

    private static void limpa(ServerPlayer quem) {
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);
    }
}
