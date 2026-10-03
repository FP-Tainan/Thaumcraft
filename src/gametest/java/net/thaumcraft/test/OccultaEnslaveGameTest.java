package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.mixin.MobGoalAccessor;
import net.thaumcraft.occulta.NoDrops;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.brew.BrewWorldActions;
import net.thaumcraft.occulta.enslave.EnslaverHurtByTargetGoal;
import net.thaumcraft.occulta.enslave.Enslavement;

/**
 * O Escravizado: o laço que o Cozimento da Ressurreição põe nos mortos que levanta.
 *
 * <p>A prova que carrega a fatia é a segunda. Um zumbi levantado ao lado de quem o levantou e que mordesse
 * essa pessoa não seria um exército — seria um acidente. O que faz dele soldado é <b>não mirar no dono</b>.
 */
public class OccultaEnslaveGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Quem não se escraviza são os do original. */
    @GameTest(maxTicks = 40)
    public void whoCannotBeEnslavedAreTheOriginalsOwn(GameTestHelper helper) {
        piso(helper);
        if (Enslavement.DE_QUANTO_EM_QUANTO != 20) helper.fail("a vontade se confere a cada vinte batidas");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        if (!Enslavement.podeSerEscravizado(zumbi)) helper.fail("um zumbi se escraviza");

        var golem = helper.spawn(EntityTypes.IRON_GOLEM, new BlockPos(5, 2, 2));
        if (Enslavement.podeSerEscravizado(golem)) helper.fail("mas um golem não");

        var bruxa = helper.spawn(EntityTypes.WITCH, new BlockPos(2, 2, 5));
        if (Enslavement.podeSerEscravizado(bruxa)) helper.fail("nem uma bruxa");

        var ente = helper.spawn(net.thaumcraft.occulta.OccultaEntities.ENT, new BlockPos(5, 2, 5));
        if (Enslavement.podeSerEscravizado(ente)) helper.fail("nem um ente");

        zumbi.discard();
        golem.discard();
        bruxa.discard();
        ente.discard();
        helper.succeed();
    }

    /**
     * <b>Um escravo nunca mira em quem o escravizou.</b>
     *
     * <p>A prova mexe na <b>dificuldade</b> e a põe de volta no fim: em paz, nenhum bicho mira em gente
     * nenhuma, e a prova passaria por engano — o alvo seria nulo por paz, e não por laço.
     */
    @GameTest(maxTicks = 40)
    public void anEnslavedMobNeverTargetsItsEnslaver(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var antes = level.getDifficulty();
        level.getServer().setDifficulty(net.minecraft.world.Difficulty.EASY, true);
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        var dono = sobrevivente(helper);
        try {
            // antes do laço, ele mira em qualquer um
            zumbi.setTarget(dono);
            if (zumbi.getTarget() != dono) {
                helper.fail("antes do laço, o zumbi mira em quem quiser");
                return;
            }

            if (!Enslavement.escraviza(zumbi, dono)) helper.fail("e o laço se põe");
            if (zumbi.getTargetUnchecked() != null) {
                helper.fail("pôr o laço faz ele largar o que tinha em mira");
            }
            if (!Enslavement.escravoDe(zumbi, dono)) helper.fail("e ele passa a ser dele");
            if (!zumbi.hasEffect(OccultaEffects.ENSLAVED)) helper.fail("com o efeito posto, e infinito");

            // e dali em diante não mira mais nele, por mais que se mande
            zumbi.setTarget(dono);
            if (zumbi.getTargetUnchecked() != null) {
                helper.fail("um escravo não mira em quem o escravizou");
            }
            helper.succeed();
        } finally {
            level.getServer().setDifficulty(antes, true);
            zumbi.discard();
        }
    }

    /** Mas mira em tudo o mais: o laço é com uma pessoa, e não com o mundo. */
    @GameTest(maxTicks = 40)
    public void butItStillTargetsEverythingElse(GameTestHelper helper) {
        piso(helper);
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 3));
        var dono = helper.makeMockServerPlayerInLevel();
        dono.setGameMode(GameType.SURVIVAL);

        Enslavement.escraviza(zumbi, dono);
        zumbi.setTarget(porco);
        if (zumbi.getTarget() != porco) helper.fail("ele continua a mirar no que não é o dono");

        zumbi.discard();
        porco.discard();
        helper.succeed();
    }

    /** Escravizar o que já é seu não é laço novo. */
    @GameTest(maxTicks = 40)
    public void enslavingWhatIsAlreadyYoursChangesNothing(GameTestHelper helper) {
        piso(helper);
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        var dono = helper.makeMockServerPlayerInLevel();
        dono.setGameMode(GameType.SURVIVAL);

        if (!Enslavement.escraviza(zumbi, dono)) helper.fail("o primeiro laço se põe");
        if (Enslavement.escraviza(zumbi, dono)) helper.fail("e o segundo não: já era dele");

        zumbi.discard();
        helper.succeed();
    }

    /** <b>E ele briga as brigas do dono</b>: a vontade entra na lista de alvos pela batida do efeito. */
    @GameTest(maxTicks = 60)
    public void itFightsItsEnslaversFights(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        var dono = helper.makeMockServerPlayerInLevel();
        dono.setGameMode(GameType.SURVIVAL);
        Enslavement.escraviza(zumbi, dono);

        if (temAVontade(zumbi)) {
            helper.fail("a vontade não vem junto com o laço: vem na batida");
            zumbi.discard();
            return;
        }

        Enslavement.tick(level, zumbi);
        if (!temAVontade(zumbi)) {
            helper.fail("uma batida do efeito põe a vontade de brigar as brigas do dono");
            zumbi.discard();
            return;
        }

        // e não a põe duas vezes
        int quantas = quantasVontades(zumbi);
        Enslavement.tick(level, zumbi);
        if (quantasVontades(zumbi) != quantas) {
            helper.fail("e não a põe duas vezes; ficaram " + quantasVontades(zumbi));
        }

        zumbi.discard();
        helper.succeed();
    }

    /**
     * <b>O Cozimento da Ressurreição levanta mortos que são de quem o atirou</b> — e deles não cai nada.
     *
     * <p>É a razão de ser desta fatia: sem o laço, o frasco era uma arma que mordia quem a atirava; sem a
     * marca, era uma fábrica de carne podre.
     */
    @GameTest(maxTicks = 60)
    public void theRaisingBrewMakesThemYours(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var dono = helper.makeMockServerPlayerInLevel();
        dono.setGameMode(GameType.SURVIVAL);

        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 4));
        BrewWorldActions.Raising.raise(level, onde, dono);

        Mob morto = null;
        for (Mob quem : level.getEntitiesOfClass(Mob.class, new AABB(onde).inflate(2.0))) {
            if (Enslavement.escravoDe(quem, dono)) {
                morto = quem;
                break;
            }
        }
        if (morto == null) {
            helper.fail("o morto levantado é de quem o levantou");
            return;
        }
        if (!NoDrops.marcado(morto)) helper.fail("e dele não cai nada");
        if (!morto.isPersistenceRequired()) helper.fail("e ele não some sozinho");

        morto.discard();
        helper.succeed();
    }

    /** E um morto levantado por ninguém é um morto solto. */
    @GameTest(maxTicks = 60)
    public void raisedByNoOneIsLoose(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(6, 2, 6));
        BrewWorldActions.Raising.raise(level, onde);

        Mob morto = null;
        for (Mob quem : level.getEntitiesOfClass(Mob.class, new AABB(onde).inflate(1.5))) {
            if (NoDrops.marcado(quem)) {
                morto = quem;
                break;
            }
        }
        if (morto == null) {
            helper.fail("ainda assim ele nasce marcado");
            return;
        }
        if (Enslavement.escravizado(morto)) helper.fail("mas sem dono");

        morto.discard();
        helper.succeed();
    }

    /** <b>E de gente cai sempre</b>, por mais que a marca esteja lá. */
    @GameTest(maxTicks = 40)
    public void fromAPersonSomethingAlwaysDrops(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockServerPlayerInLevel();
        NoDrops.marca(quem);
        if (NoDrops.marcado(quem)) {
            helper.fail("nenhum feitiço faz um jogador morrer com as mãos vazias");
        }

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        NoDrops.marca(porco);
        if (!NoDrops.marcado(porco)) helper.fail("mas de um porco, sim");

        porco.discard();
        helper.succeed();
    }

    /**
     * Um jogador de prova <b>que um bicho possa mirar</b>.
     *
     * <p>O {@code makeMockServerPlayerInLevel} entrega um jogador que está <b>sempre em criativo</b>: a classe
     * de mentira do jogo sobrescreve o {@code gameMode()} com a palavra {@code CREATIVE} escrita à mão, e o
     * {@code setGameMode} não a tira de lá. E nenhum bicho mira em alguém em criativo — é a primeira coisa que
     * o {@code asValidTarget} do {@code Mob} olha.
     *
     * <p>Quem serve é o <b>{@code makeMockServerPlayer(GameType)}</b>, que diz o modo que se pediu. Ele não
     * entra no mundo, e para esta prova não precisa: a mira não pergunta onde o alvo está.
     */
    private static net.minecraft.world.entity.player.Player sobrevivente(GameTestHelper helper) {
        return helper.makeMockServerPlayer(GameType.SURVIVAL);
    }

    private static boolean temAVontade(Mob quem) {
        return quantasVontades(quem) > 0;
    }

    private static int quantasVontades(Mob quem) {
        return (int) ((MobGoalAccessor) quem).thaumcraft$targetSelector().getAvailableGoals().stream()
                .filter(g -> g.getGoal() instanceof EnslaverHurtByTargetGoal)
                .count();
    }
}
