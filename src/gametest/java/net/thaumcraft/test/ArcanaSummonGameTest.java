package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.Summons;

import java.util.List;

/**
 * A Invocação: quem vem, de que lado ela fica, quanto dura, e até onde a trela chega.
 *
 * <p>O que estas provas medem é o que o {@code Summon} do Ars Magica 2 faz de verdade — e o que é fácil de
 * portar errado. <b>Uma invocação que bate em quem a chamou não é uma invocação</b>, é um esqueleto nascido na
 * cara do mago; e <b>uma invocação sem prazo</b> é um bicho largado no mapa. As duas coisas já estiveram
 * erradas neste arquivo antes de haver prova.
 */
public class ArcanaSummonGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Uma Invocação simples, sem modificador nenhum. */
    private static Spell simples() {
        return Spell.of(Shapes.TOUCH, Essences.SUMMON);
    }

    /** Lança a Invocação num ponto do chão, que é o {@code applyEffectBlock} do original. */
    private static boolean chama(GameTestHelper helper, Spell feitiço, ServerPlayer quem, Vec3 onde) {
        return Essences.SUMMON.onBlock(helper.getLevel(), feitiço, quem,
                BlockPos.containing(onde), Direction.UP, onde);
    }

    /**
     * O esqueleto <b>desta</b> pessoa.
     *
     * <p>O de quem lançou, e não um esqueleto invocado qualquer: as provas correm todas no mesmo mundo, e
     * {@code getAllEntities} vê as arenas das vizinhas. Procurar sem o dono acha a invocação de outra prova e
     * mede a coisa errada — o que já aconteceu aqui.
     *
     * <p>E tem de estar <b>viva</b>: um esqueleto que acabou de cair ainda aparece no {@code getAllEntities}
     * da mesma batida, e a prova da trela chama duas vezes seguidas. Sem este cuidado, a segunda chamada
     * achava a primeira, já morta, e a prova falhava a dizer que a com nome tinha morrido.
     */
    /**
     * O esqueleto <b>desta</b> prova.
     *
     * <p>A procura é à volta de quem chamou, e não no mundo todo: o jogador de mentira das provas tem sempre
     * a mesma marca, de modo que um esqueleto chamado na arena do lado é dele também. Procurando longe,
     * acha-se o da vizinha e prova-se outra coisa.
     */
    private static Skeleton oEsqueleto(GameTestHelper helper, ServerPlayer quem) {
        var roda = net.minecraft.world.phys.AABB.ofSize(quem.position(), 24.0, 24.0, 24.0);
        for (var osso : helper.getLevel().getEntitiesOfClass(Skeleton.class, roda)) {
            if (!osso.isAlive()) continue;
            var dado = osso.getAttached(Summons.DATA);
            if (dado != null && dado.dono().equals(quem.getUUID())) return osso;
        }
        return null;
    }

    /**
     * Ela traz um esqueleto, com arco, marcado, e <b>do lado de quem a chamou</b>.
     *
     * <p>A última parte é a que importa: no original o {@code makeSummon_PlayerFaction} limpa a lista de alvos
     * do bicho, e é isso que faz ele não mirar o mago. A prova mede a lista, e não o humor dele.
     */
    @GameTest(maxTicks = 40)
    public void itBringsASkeletonWithABowOnYourSide(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        Vec3 onde = helper.absoluteVec(new Vec3(4.5, 2, 4.5));
        if (!chama(helper, simples(), quem, onde)) {
            helper.fail("a Invocação devia pegar");
            return;
        }

        var osso = oEsqueleto(helper, quem);
        if (osso == null) {
            helper.fail("e devia ter vindo um esqueleto marcado");
            return;
        }
        if (!osso.getMainHandItem().is(Items.BOW)) {
            helper.fail("o esqueleto do original vem com arco, veio com " + osso.getMainHandItem());
        }
        if (Summons.quantas(level, quem) != 1) {
            helper.fail("e conta uma, conta " + Summons.quantas(level, quem));
        }
        if (osso.getTarget() == quem) helper.fail("ela não mira quem a chamou");

        osso.discard();
        helper.succeed();
    }

    /**
     * Ela procura <b>monstro</b>, e o alvo dela é o zumbi ao lado — nunca o mago.
     *
     * <p>É a outra metade da troca de lado: tirar o mago da lista não serve de nada se nada entrar no lugar.
     */
    @GameTest(maxTicks = 60)
    public void itHuntsMonstersAndNeverTheCaster(GameTestHelper helper) {
        piso(helper);
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(1.5, 2, 1.5)));

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(6, 2, 6));
        zumbi.setNoAi(true);

        if (!chama(helper, simples(), quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)))) {
            helper.fail("a Invocação devia pegar");
            return;
        }
        var osso = oEsqueleto(helper, quem);
        if (osso == null) {
            helper.fail("devia ter vindo um esqueleto");
            return;
        }

        helper.succeedWhen(() -> {
            if (osso.getTarget() == quem) helper.fail("ela nunca mira quem a chamou");
            helper.assertTrue(osso.getTarget() == zumbi, "ela devia estar mirando o zumbi");
        });
    }

    /**
     * O teto é um — e a segunda, <b>cheia, ainda dá certo</b>.
     *
     * <p>É o original lido ao pé da letra: ele só devolve {@code false} quando a criatura não nasceu; com o teto
     * cheio ele manda a frase e devolve {@code true}, e por isso a mana se gasta. É castigo, e é de propósito.
     */
    @GameTest(maxTicks = 40)
    public void theCapIsOneAndTheSecondOneOnlyWarns(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        if (Summons.TETO != 1) helper.fail("o teto deste porte é um, é " + Summons.TETO);

        chama(helper, simples(), quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var primeiro = oEsqueleto(helper, quem);
        if (primeiro == null) {
            helper.fail("a primeira vem");
            return;
        }

        if (!chama(helper, simples(), quem, helper.absoluteVec(new Vec3(5.5, 2, 5.5)))) {
            helper.fail("cheia, ela ainda devolve certo — a mana se gasta");
        }
        if (Summons.quantas(level, quem) != 1) {
            helper.fail("mas continua uma, e são " + Summons.quantas(level, quem));
        }

        primeiro.discard();
        helper.succeed();
    }

    /** E não se invoca <b>em cima de uma invocação</b>: é o que impede a cadeia. */
    @GameTest(maxTicks = 40)
    public void youCannotSummonOntoASummon(GameTestHelper helper) {
        piso(helper);
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        chama(helper, simples(), quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var osso = oEsqueleto(helper, quem);
        if (osso == null) {
            helper.fail("devia ter vindo um esqueleto");
            return;
        }

        if (Essences.SUMMON.onEntity(helper.getLevel(), simples(), quem, osso)) {
            helper.fail("em cima de uma invocação ela recusa");
        }

        osso.discard();
        helper.succeed();
    }

    /** O prazo é o do original, e a Duração o multiplica. */
    @GameTest(maxTicks = 40)
    public void theDurationModifierStretchesTheDeadline(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        if (Summons.PRAZO != 4800) helper.fail("o prazo do original é 4800, é " + Summons.PRAZO);

        Spell longa = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.SUMMON), List.of(Modifiers.DURATION))));
        chama(helper, longa, quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));

        var osso = oEsqueleto(helper, quem);
        if (osso == null) {
            helper.fail("devia ter vindo um esqueleto");
            return;
        }
        // 4800 × 2,2 = 10560
        long falta = osso.getAttached(Summons.DATA).acabaEm() - level.getGameTime();
        if (falta != 10560) helper.fail("um modificador de Duração dá 10560 batidas, e deu " + falta);

        osso.discard();
        helper.succeed();
    }

    /** Acabado o prazo, ela <b>morre</b> — e morre do dano que desfaz, não desaparece calada. */
    @GameTest(maxTicks = 60)
    public void whenTheDeadlinePassesItDies(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        chama(helper, simples(), quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var osso = oEsqueleto(helper, quem);
        if (osso == null) {
            helper.fail("devia ter vindo um esqueleto");
            return;
        }

        // o prazo acabou agorinha
        osso.setAttached(Summons.DATA, new Summons(quem.getUUID(), level.getGameTime() - 1));
        Summons.tick(level, osso);

        if (osso.isAlive()) helper.fail("acabado o prazo ela cai");
        helper.succeed();
    }

    /**
     * A trela: longe demais de quem a chamou, ela se desfaz — <b>menos se tiver nome próprio</b>, e aí só se
     * solta e fica.
     */
    @GameTest(maxTicks = 60)
    public void tooFarItGoesAwayUnlessItHasAName(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        if (Summons.LONGE_DEMAIS != 900.0) {
            helper.fail("a trela do original é 900 ao quadrado, é " + Summons.LONGE_DEMAIS);
        }

        // uma sem nome, levada para longe
        chama(helper, simples(), quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var anônima = oEsqueleto(helper, quem);
        if (anônima == null) {
            helper.fail("devia ter vindo um esqueleto");
            return;
        }
        anônima.snapTo(quem.getX() + 100.0, quem.getY(), quem.getZ());
        Summons.tick(level, anônima);
        if (anônima.isAlive()) helper.fail("longe demais, a sem nome se desfaz");

        // e uma com nome, no mesmo lugar
        chama(helper, simples(), quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var batizada = oEsqueleto(helper, quem);
        if (batizada == null) {
            helper.fail("devia ter vindo a segunda");
            return;
        }
        batizada.setCustomName(net.minecraft.network.chat.Component.literal("Osvaldo"));
        batizada.snapTo(quem.getX() + 100.0, quem.getY(), quem.getZ());
        Summons.tick(level, batizada);

        if (!batizada.isAlive()) helper.fail("a com nome não morre: ela se solta");
        if (Summons.éInvocado(batizada)) helper.fail("e solta quer dizer sem dono e sem prazo");

        batizada.discard();
        helper.succeed();
    }
    /**
     * E ela está na árvore <b>no lugar que o original lhe deu</b>: ramo da Defesa, ponto verde, em (267, 135),
     * pendurada no Vida por Mana.
     *
     * <p>Os números saem do {@code SkillTreeManager} do Ars Magica 2, lidos lá. Uma peça que funciona mas que
     * ninguém pode comprar no lugar certo não está portada, e só uma prova assim diz isso.
     */
    @GameTest(maxTicks = 20)
    public void itSitsWhereTheOriginalPutIt(GameTestHelper helper) {
        var posto = net.thaumcraft.arcana.SkillTree.of(Essences.SUMMON);
        if (posto == null) {
            helper.fail("a Invocação tem de estar na árvore");
            return;
        }
        if (posto.branch() != net.thaumcraft.arcana.SkillTree.Branch.DEFENSE) {
            helper.fail("o original a põe na Defesa, e está em " + posto.branch());
        }
        if (posto.point() != net.thaumcraft.arcana.SkillTree.Point.GREEN) {
            helper.fail("e em ponto verde, e está em " + posto.point());
        }
        if (posto.x() != 267 || posto.y() != 135) {
            helper.fail("em (267, 135), e está em (" + posto.x() + ", " + posto.y() + ")");
        }
        if (!posto.needs().contains(Essences.LIFE_TAP)) {
            helper.fail("e pende do Vida por Mana, e pende de " + posto.needs());
        }
        helper.succeed();
    }
}
