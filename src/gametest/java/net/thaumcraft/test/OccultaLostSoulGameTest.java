package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.spirit.FlyerGoals;
import net.thaumcraft.occulta.spirit.LostSoulEntity;

/**
 * A <b>Alma Perdida</b>, que é um Espírito que briga.
 *
 * <p>A prova que carrega a fatia é a da <b>tabela dos três feitios</b>: o que a torna um bicho e não um
 * saco de pancada é que cada feitio só apanha de <b>uma</b> coisa, e a cor dela diz qual. Errando a
 * tabela, bate-se nela a tarde toda sem lhe tirar nada — ou mata-se com qualquer coisa, que é pior.
 */
public class OccultaLostSoulGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>Os números dela são os do original.</b> */
    @GameTest(maxTicks = 20)
    public void herNumbersAreTheOriginals(GameTestHelper helper) {
        if (LostSoulEntity.VIDA != 20.0) helper.fail("vinte de vida");
        if (LostSoulEntity.VELOCIDADE != 0.4) helper.fail("quatro décimos de passo");
        if (LostSoulEntity.MURRO != 2.0) helper.fail("e dois de murro");
        if (LostSoulEntity.TETO != 15.0f) helper.fail("e nada lhe tira mais de quinze");
        if (LostSoulEntity.UMA_EM != 4) helper.fail("e o golpe do feitio sai uma em quatro");
        if (LostSoulEntity.FEITIOS != 3) helper.fail("e os feitios são três");
        helper.succeed();
    }

    /**
     * <b>Cada feitio tem a sua cor</b>, e é por ela que se sabe, de longe, qual arma serve.
     *
     * <p>Vermelho é fogo, verde é golpe, azul é magia — as três cores puras do original.
     */
    @GameTest(maxTicks = 20)
    public void eachKindWearsItsColour(GameTestHelper helper) {
        piso(helper);
        var alma = helper.spawn(OccultaEntities.LOST_SOUL, new BlockPos(3, 3, 3));

        alma.feitioDaAlma(LostSoulEntity.FOGO);
        if (alma.cor() != LostSoulEntity.VERMELHO) helper.fail("a do fogo é vermelha");
        alma.feitioDaAlma(LostSoulEntity.GOLPE);
        if (alma.cor() != LostSoulEntity.VERDE) helper.fail("a do golpe é verde");
        alma.feitioDaAlma(LostSoulEntity.MAGIA);
        if (alma.cor() != LostSoulEntity.AZUL) helper.fail("e a da magia é azul");

        alma.discard();
        helper.succeed();
    }

    /**
     * <b>A tabela dos três feitios</b>, que é o que faz dela um bicho e não um saco de pancada.
     *
     * <p>A do fogo só apanha de fogo e estouro; a da magia, só de magia; e a do golpe apanha de tudo
     * <b>menos</b> as oito coisas que o original lista. Repare no primeiro: o que ela atira é o que a mata.
     */
    @GameTest(maxTicks = 20)
    public void eachKindOnlyTakesItsOwnHurt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var fontes = level.damageSources();

        DamageSource fogo = fontes.inFire();
        DamageSource estouro = fontes.explosion(null, null);
        DamageSource magia = fontes.magic();
        var esqueleto = helper.spawn(EntityTypes.SKELETON, new BlockPos(6, 2, 6));
        DamageSource flecha = fontes.arrow(
                new net.minecraft.world.entity.projectile.arrow.Arrow(level, esqueleto,
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW),
                        null), esqueleto);
        DamageSource murro = fontes.generic();
        DamageSource murcha = fontes.wither();
        DamageSource cacto = fontes.cactus();

        // a do fogo: só fogo e estouro
        if (!LostSoulEntity.apanhaDe(fogo, LostSoulEntity.FOGO)) helper.fail("a do fogo apanha de fogo");
        if (!LostSoulEntity.apanhaDe(estouro, LostSoulEntity.FOGO)) helper.fail("e de estouro");
        if (LostSoulEntity.apanhaDe(murro, LostSoulEntity.FOGO)) helper.fail("mas não de murro");
        if (LostSoulEntity.apanhaDe(magia, LostSoulEntity.FOGO)) helper.fail("nem de magia");

        // a da magia: só magia
        if (!LostSoulEntity.apanhaDe(magia, LostSoulEntity.MAGIA)) helper.fail("a da magia apanha de magia");
        if (LostSoulEntity.apanhaDe(fogo, LostSoulEntity.MAGIA)) helper.fail("e de mais nada: nem fogo");
        if (LostSoulEntity.apanhaDe(murro, LostSoulEntity.MAGIA)) helper.fail("nem murro");

        // e a do golpe: tudo menos as oito
        if (!LostSoulEntity.apanhaDe(murro, LostSoulEntity.GOLPE)) helper.fail("a do golpe apanha de murro");
        for (var nãoPega : new DamageSource[]{flecha, magia, fogo, estouro, murcha, cacto}) {
            if (LostSoulEntity.apanhaDe(nãoPega, LostSoulEntity.GOLPE)) {
                helper.fail("mas não de " + nãoPega.type().msgId());
                return;
            }
        }
        esqueleto.discard();
        helper.succeed();
    }

    /** <b>E nada lhe tira mais de quinze de uma vez</b>, por muito que se bata. */
    @GameTest(maxTicks = 40)
    public void nothingTakesMoreThanFifteenAtOnce(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var alma = helper.spawn(OccultaEntities.LOST_SOUL, new BlockPos(3, 3, 3));
        alma.feitioDaAlma(LostSoulEntity.MAGIA);
        alma.setHealth(alma.getMaxHealth());

        alma.hurtServer(level, level.damageSources().magic(), 1000.0f);
        float sobrou = alma.getHealth();
        if (sobrou != LostSoulEntity.VIDA - LostSoulEntity.TETO) {
            helper.fail("mil de magia tiram quinze e deixam cinco: ficou " + sobrou);
            alma.discard();
            return;
        }

        // e o que o feitio não deixa não lhe tira nada
        alma.hurtServer(level, level.damageSources().inFire(), 1000.0f);
        if (alma.getHealth() != sobrou) helper.fail("e o fogo não lhe toca");

        alma.discard();
        helper.succeed();
    }

    /**
     * <b>O prazo manda-a embora calada.</b>
     *
     * <p>É o que o Leonard usa, e é o que a torna possível de tirar do mundo: ela some sem largar nada e
     * sem o estouro de pó com que o Espírito de quem herda se despede.
     */
    @GameTest(maxTicks = 60)
    public void theDeadlineSendsHerAwayQuietly(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var alma = helper.spawn(OccultaEntities.LOST_SOUL, new BlockPos(3, 3, 3));

        if (alma.deEmpréstimo()) helper.fail("ela não nasce com prazo");
        alma.prazo(1);
        if (!alma.oPrazo(level)) helper.fail("e com um, acaba na primeira volta");
        if (alma.isAlive()) helper.fail("e some");

        // e não deixa Espírito Dominado nenhum atrás dela
        var largou = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                helper.getBounds().inflate(4.0));
        for (var coisa : largou) {
            if (coisa.getItem().is(net.thaumcraft.occulta.OccultaItems.SUBDUED_SPIRIT)) {
                helper.fail("e não larga nada: o Espírito larga, ela não");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Ela caça gente, e nenhum outro bicho.</b>
     *
     * <p>O original esvazia as listas que herda do Espírito e põe uma só mira: o jogador. Um porco ao lado
     * dela é um porco, e fica.
     */
    @GameTest(maxTicks = 60)
    public void sheHuntsPeopleAndNothingElse(GameTestHelper helper) {
        piso(helper);
        var alma = helper.spawn(OccultaEntities.LOST_SOUL, new BlockPos(3, 3, 3));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));

        helper.runAfterDelay(30, () -> {
            if (alma.getTargetUnchecked() == porco) helper.fail("um porco não é presa dela");
            alma.discard();
            porco.discard();
            helper.succeed();
        });
    }

    /**
     * <b>Investir é encostar.</b>
     *
     * <p>O alcance da meta de murro é {@code (largura × 2)² + largura do alvo} — para um bicho de um quarto
     * de bloco contra gente, menos de um bloco ao quadrado. É uma meta de <b>colisão</b>, e o nome dela no
     * original diz isso.
     */
    @GameTest(maxTicks = 20)
    public void chargingMeansTouching(GameTestHelper helper) {
        piso(helper);
        var alma = helper.spawn(OccultaEntities.LOST_SOUL, new BlockPos(3, 3, 3));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 5));

        double esperado = 0.5 * 0.5 + porco.getBbWidth();
        double deu = FlyerGoals.Investe.encosta(alma, porco);
        if (Math.abs(deu - esperado) > 1.0e-6) {
            helper.fail("o alcance do murro é " + esperado + " e deu " + deu);
        }
        if (FlyerGoals.Investe.RECARGA != 20) helper.fail("e a recarga é de um segundo");

        alma.discard();
        porco.discard();
        helper.succeed();
    }
}
