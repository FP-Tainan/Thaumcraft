package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewActions;
import net.thaumcraft.occulta.brew.BrewDispersal;
import net.thaumcraft.occulta.brew.BrewDropletEntity;
import net.thaumcraft.occulta.brew.BrewRegistry;
import net.thaumcraft.occulta.brew.BrewRitual;

import java.util.List;

/**
 * O <b>rito de cozimento</b>: o caldeirão como lançador.
 *
 * <p>É a última peça grande do ofício, e a que mais muda o que ele é — um caldeirão numa cave, um círculo
 * de giz, um coven, e uma marca com o nome de quem vai adoecer do outro lado do mapa.
 *
 * <p>A prova que carrega a fatia é a do <b>giz</b>: cada anel desconta no poder que o rito custa, e o giz
 * infernal desconta mais e <b>arrisca</b>. É o desenho mais bonito do original e o que ele nunca explica,
 * e um desconto trocado não acusa nada — o rito corre na mesma, mais caro ou mais barato do que devia.
 */
public class OccultaBrewRitualGameTest {
    /** Um cozimento de atirar que cabe: porte, efeito e pólvora. */
    private static final List<Item> ATIRADO = List.of(OccultaItems.MANDRAKE_ROOT,
            Items.NETHER_WART, Items.SPIDER_EYE, Items.GUNPOWDER);

    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>Os dois gatilhos estão na tabela</b>, e nenhum deles é um efeito. */
    @GameTest(maxTicks = 20)
    public void bothTriggersAreInTheTable(GameTestHelper helper) {
        var língua = BrewRegistry.of(OccultaItems.DOG_TONGUE);
        var marca = BrewRegistry.of(OccultaItems.TAGLOCK);
        if (!(língua instanceof BrewActions.Ritual aVolta)
                || !(marca instanceof BrewActions.Ritual emAlguém)) {
            helper.fail("a Língua de Cão e a Marca fazem do caldeirão um rito");
            return;
        }
        if (!aVolta.sóAtirado() || emAlguém.sóAtirado()) {
            helper.fail("a Língua pede cozimento de atirar, e a Marca não");
            return;
        }
        if (língua.power() != 250 || marca.power() != 1000) {
            helper.fail("duzentos e cinquenta e mil, como no original");
            return;
        }
        if (língua.isEffect() || marca.isEffect()) {
            helper.fail("e nenhum deles é um efeito: são gatilhos");
            return;
        }
        helper.succeed();
    }

    /** <b>E a Língua de Cão só entra com cozimento de atirar.</b> */
    @GameTest(maxTicks = 20)
    public void theDogTongueNeedsASplash(GameTestHelper helper) {
        List<Item> bebido = List.of(OccultaItems.MANDRAKE_ROOT, Items.NETHER_WART, Items.SPIDER_EYE);
        if (Brew.canAdd(bebido, OccultaItems.DOG_TONGUE, true)) {
            helper.fail("um cozimento de beber não se espalha, e a Língua recusa-o");
            return;
        }
        if (!Brew.canAdd(ATIRADO, OccultaItems.DOG_TONGUE, true)) {
            helper.fail("e com pólvora na panela ela entra");
            return;
        }
        // e a Marca entra nos dois
        if (!Brew.canAdd(bebido, OccultaItems.TAGLOCK, true)) {
            helper.fail("a Marca entra em qualquer um");
            return;
        }
        // mas nenhum entra numa panela que não esteja cheia
        if (Brew.canAdd(ATIRADO, OccultaItems.DOG_TONGUE, false)) {
            helper.fail("e nenhum entra numa panela que não esteja cheia");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O giz paga o rito, e o giz infernal arrisca.</b>
     *
     * <p>Sem anel nenhum custa uma vez e quatro décimos e arrisca sempre; com os dois anéis de ritual
     * custa um e não arrisca nada; com os dois infernais custa menos de um terço — e arrisca dois.
     */
    @GameTest(maxTicks = 20)
    public void chalkPaysAndInfernalChalkRisks(GameTestHelper helper) {
        var nenhum = anéis(0, 0);
        if (Math.abs(BrewRitual.escala(nenhum) - 1.4) > 0.001) {
            helper.fail("sem anel nenhum, uma vez e quatro décimos");
            return;
        }
        if (BrewRitual.risco(nenhum) != 1) {
            helper.fail("e arrisca sempre um");
            return;
        }

        var ritual = anéis(1, 0);
        if (Math.abs(BrewRitual.escala(ritual) - 1.0) > 0.001) {
            helper.fail("com os dois anéis de ritual, uma vez: deu " + BrewRitual.escala(ritual));
            return;
        }
        if (BrewRitual.risco(ritual) != 0) {
            helper.fail("e não arrisca nada");
            return;
        }

        var infernal = anéis(0, 1);
        if (Math.abs(BrewRitual.escala(infernal) - (1.4 - 0.74)) > 0.001) {
            helper.fail("com os dois infernais, menos de um terço: deu " + BrewRitual.escala(infernal));
            return;
        }
        if (BrewRitual.risco(infernal) != 3) {
            helper.fail("e arrisca três: o de não ter giz de ritual, mais um por anel infernal");
            return;
        }
        helper.succeed();
    }

    /** Dois anéis cheios do giz que se pedir: zero é ritual, dois é infernal. */
    private static net.thaumcraft.occulta.RitualCircles.Circles anéis(int ritual, int infernal) {
        return new net.thaumcraft.occulta.RitualCircles.Circles(
                new net.thaumcraft.occulta.RitualCircles.Ring(
                        ritual * net.thaumcraft.occulta.RitualCircles.INNER, 0,
                        infernal * net.thaumcraft.occulta.RitualCircles.INNER,
                        net.thaumcraft.occulta.RitualCircles.INNER),
                new net.thaumcraft.occulta.RitualCircles.Ring(
                        ritual * net.thaumcraft.occulta.RitualCircles.MIDDLE, 0,
                        infernal * net.thaumcraft.occulta.RitualCircles.MIDDLE,
                        net.thaumcraft.occulta.RitualCircles.MIDDLE),
                new net.thaumcraft.occulta.RitualCircles.Ring(0, 0, 0,
                        net.thaumcraft.occulta.RitualCircles.OUTER));
    }

    /**
     * <b>E o alcance sai do coven</b>: sozinho são trinta e dois blocos, com seis não há limite.
     */
    @GameTest(maxTicks = 20)
    public void theCovenSetsTheRange(GameTestHelper helper) {
        BlockPos caldeirão = BlockPos.ZERO;
        if (!BrewRitual.alcança(caldeirão, new Vec3(30.0, 0.0, 0.0), 0)) {
            helper.fail("sozinho ele alcança trinta blocos");
            return;
        }
        if (BrewRitual.alcança(caldeirão, new Vec3(40.0, 0.0, 0.0), 0)) {
            helper.fail("e não alcança quarenta");
            return;
        }
        if (!BrewRitual.alcança(caldeirão, new Vec3(200.0, 0.0, 0.0), 3)) {
            helper.fail("com três bruxas alcança duzentos");
            return;
        }
        if (!BrewRitual.alcança(caldeirão, new Vec3(1000000.0, 0.0, 0.0),
                BrewRitual.COVEN_SEM_LIMITE)) {
            helper.fail("e com seis não há limite nenhum");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O rito de poça faz chover.</b>
     *
     * <p>É a dívida mais antiga do ramo — a chuva de gotas —, e a imagem mais bonita dele: cem casas
     * acima do alvo começam a cair gotas do cozimento, dezenas por batida.
     */
    @GameTest(maxTicks = 40)
    public void theLiquidRiteRains(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        var poça = new BrewDispersal.Liquid();
        var espalha = new net.thaumcraft.occulta.brew.BrewImpact(null);
        var deu = poça.noRito(level, meio, ATIRADO, 0, 1, espalha);
        if (deu != BrewRitual.Estado.ANDANDO) {
            helper.fail("à primeira batida a chuva ainda está a começar");
            return;
        }

        var gotas = level.getEntitiesOfClass(BrewDropletEntity.class,
                new net.minecraft.world.phys.AABB(meio).inflate(64.0, 256.0, 64.0));
        if (gotas.isEmpty()) {
            helper.fail("e caem gotas");
            return;
        }
        var uma = gotas.getFirst();
        if (uma.getY() < meio.getY() + BrewDispersal.Liquid.ALTURA) {
            helper.fail("de cem casas acima: nasceu a " + (uma.getY() - meio.getY()));
            return;
        }
        if (!uma.dentro().contains(Items.SPIDER_EYE)) {
            helper.fail("e cada gota leva o cozimento consigo");
            return;
        }
        gotas.forEach(net.minecraft.world.entity.Entity::discard);

        // e ela acaba: dez batidas sem duração nenhuma
        if (poça.noRito(level, meio, ATIRADO, 0, BrewDispersal.Liquid.DURA, espalha)
                != BrewRitual.Estado.PRONTO) {
            helper.fail("e às dez batidas ela acaba");
            return;
        }
        level.getEntitiesOfClass(BrewDropletEntity.class,
                        new net.minecraft.world.phys.AABB(meio).inflate(64.0, 256.0, 64.0))
                .forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /** <b>E o rito de uma vez só cai em cima do alvo</b>, e acaba na primeira batida. */
    @GameTest(maxTicks = 40)
    public void theInstantRiteFallsAtOnce(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));

        var deuma = new BrewDispersal.Instant();
        var deu = deuma.noRito(level, meio, ATIRADO, 0, 1,
                new net.thaumcraft.occulta.brew.BrewImpact(null));
        if (deu != BrewRitual.Estado.PRONTO) {
            helper.fail("ele acaba na primeira batida");
            return;
        }
        if (!porco.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
            helper.fail("e o que estiver no alvo apanha o cozimento");
            return;
        }
        porco.discard();
        helper.succeed();
    }

    /**
     * <b>E quem está guardado não apanha nada.</b>
     *
     * <p>São três guardas, e o rito corre na mesma: gasta-se o caldeirão e não acontece nada a ninguém.
     * É o original, e é o que faz valer a pena vestir as roupas de caçador.
     */
    @GameTest(maxTicks = 40)
    public void theWardedTakeNothing(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));
        if (BrewRitual.guardado(level, porco)) {
            helper.fail("um porco qualquer não está guardado de nada");
            return;
        }

        /*
         * E um marcador de lobo guarda-o: é a mesma guarda que trava o rito da maldição dirigida, e o
         * original manda-a valer também para o caldeirão.
         */
        net.thaumcraft.occulta.rite.AreaMarkers.esqueceTudo();
        var marcador = new net.thaumcraft.occulta.rite.AreaMarkers.Marcador(level.dimension(),
                helper.absolutePos(new BlockPos(4, 2, 4)),
                net.thaumcraft.occulta.rite.AreaMarkers.Kind.CURSE, java.util.Set.of());
        net.thaumcraft.occulta.rite.AreaMarkers.register(marcador);
        boolean guardado = BrewRitual.guardado(level, porco);
        net.thaumcraft.occulta.rite.AreaMarkers.esqueceTudo();
        porco.discard();
        if (!guardado) {
            helper.fail("com um marcador de lobo perto, ele está guardado");
            return;
        }
        helper.succeed();
    }

    /** E o rito de nuvem é um <b>anel que cresce</b>, até oito blocos. */
    @GameTest(maxTicks = 20)
    public void theGasRiteGrows(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));
        var nuvem = new BrewDispersal.Gas();
        var espalha = new net.thaumcraft.occulta.brew.BrewImpact(null);

        if (nuvem.noRito(level, meio, ATIRADO, 0, 1, espalha) != BrewRitual.Estado.ANDANDO) {
            helper.fail("à primeira batida o anel ainda é pequeno");
            return;
        }
        if (nuvem.noRito(level, meio, ATIRADO, 0, BrewDispersal.Gas.CRESCE_ATÉ, espalha)
                != BrewRitual.Estado.PRONTO) {
            helper.fail("e às oito ele acaba");
            return;
        }
        helper.succeed();
    }
}
