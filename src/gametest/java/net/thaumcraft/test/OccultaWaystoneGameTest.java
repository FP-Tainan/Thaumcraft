package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.waystone.Waystones;

/**
 * A Pedra de Caminho: o anel miúdo que prende, o anel pequeno que leva, e o giz que se gasta.
 *
 * <p>A prova que carrega a fatia é a do <b>meio do anel</b>. O original acha o anel numa casa e devolve o
 * avesso dela — e com a pedra bem no meio ninguém vê o engano, porque o avesso de nada é nada. Esta prova larga
 * a pedra <b>de lado</b>, que é onde ela sempre cai quando alguém a atira para dentro do círculo.
 */
public class OccultaWaystoneGameTest {
    /** As oito casas do anel miúdo, pela ordem do original. */
    private static final int[][] MIÚDO = {
            {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1},
    };

    /** E os doze do anel pequeno. */
    private static final int[][] PEQUENO = {
            {0, -2}, {1, -2}, {2, -1}, {2, 0}, {2, 1}, {1, 2},
            {0, 2}, {-1, 2}, {-2, 1}, {-2, 0}, {-2, -1}, {-1, -2},
    };

    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 2, z)),
                        Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void risca(GameTestHelper helper, BlockPos meio, int[][] anel) {
        ServerLevel level = helper.getLevel();
        for (int[] casa : anel) {
            level.setBlockAndUpdate(meio.offset(casa[0], 0, casa[1]),
                    OccultaBlocks.OTHERWHERE_GLYPH.defaultBlockState());
        }
    }

    /** Larga uma pedra no chão, já velha o bastante para olhar o giz. */
    private static ItemEntity larga(GameTestHelper helper, BlockPos onde, ItemStack pedra) {
        ServerLevel level = helper.getLevel();
        ItemEntity item = new ItemEntity(level, onde.getX() + 0.5, onde.getY() + 0.1, onde.getZ() + 0.5,
                pedra);
        item.setNoGravity(true);
        level.addFreshEntity(item);
        return item;
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void herNumbersAreTheOriginals(GameTestHelper helper) {
        if (Waystones.ESPERA != 40) helper.fail("ela espera dois segundos no chão");
        if (Waystones.OLHA_DE != 40) helper.fail("e olha o giz de dois em dois segundos");
        if (Waystones.PRENDE_ATÉ != 8) helper.fail("um anel prende oito de uma vez");
        if (Waystones.CUSTA != 4000.0f) helper.fail("e a pedra sangrada custa quatro mil de poder");
        if (Waystones.ALCANCE_DO_ALVO != 2.0) helper.fail("o alvo está a dois blocos do meio");
        if (Waystones.ALCANCE_DA_PORTA != 4.0) helper.fail("e a porta leva o que está a quatro");
        helper.succeed();
    }

    /** <b>O anel miúdo prende a pedra ao lugar</b> — e gasta-se ao fazê-lo. */
    @GameTest(maxTicks = 40)
    public void theTinyRingBindsAStoneToTheSpot(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));
        risca(helper, meio, MIÚDO);
        if (!Waystones.anelMiúdo(level, meio)) {
            helper.fail("oito glifos do Alhures fazem o anel miúdo");
            return;
        }

        ItemEntity largada = larga(helper, meio, new ItemStack(OccultaItems.WAYSTONE));
        if (!Waystones.tentaPrender(level, largada)) {
            helper.fail("uma pedra lisa no meio do anel prende-se");
            return;
        }
        if (!largada.isRemoved()) helper.fail("e a pedra lisa some");

        ItemEntity feita = achaPedra(helper, meio, OccultaItems.BOUND_WAYSTONE);
        if (feita == null) {
            helper.fail("no lugar dela fica uma pedra presa");
            return;
        }
        Waystones.Lugar lugar = Waystones.lugar(feita.getItem());
        if (lugar == null) {
            helper.fail("e a pedra presa sabe o lugar");
            return;
        }
        if (!lugar.onde().equals(meio)) {
            helper.fail("o lugar é o meio do anel: esperava " + meio + ", guardou " + lugar.onde());
        }
        if (!lugar.mundo().equals(level.dimension())) helper.fail("e o mundo é este");

        // o giz se gasta
        if (Waystones.anelMiúdo(level, meio)) helper.fail("os oito glifos estouram");

        feita.discard();
        helper.succeed();
    }

    /** <b>Oito de uma vez, e o resto cai de volta.</b> */
    @GameTest(maxTicks = 40)
    public void eightAtOnceAndNoMore(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));
        risca(helper, meio, MIÚDO);

        ItemEntity largada = larga(helper, meio, new ItemStack(OccultaItems.WAYSTONE, 12));
        if (!Waystones.tentaPrender(level, largada)) {
            helper.fail("doze pedras lisas no anel prendem-se");
            return;
        }

        ItemEntity presas = achaPedra(helper, meio, OccultaItems.BOUND_WAYSTONE);
        ItemEntity lisas = achaPedra(helper, meio, OccultaItems.WAYSTONE);
        if (presas == null || lisas == null) {
            helper.fail("ficam oito presas e quatro lisas");
            return;
        }
        if (presas.getItem().getCount() != 8) {
            helper.fail("oito prendem-se; prenderam " + presas.getItem().getCount());
        }
        if (lisas.getItem().getCount() != 4) {
            helper.fail("e quatro sobram; sobraram " + lisas.getItem().getCount());
        }

        presas.discard();
        lisas.discard();
        helper.succeed();
    }

    /**
     * <b>Com alguém no anel, a pedra quer prender-se a ele — e sem altar não se faz.</b>
     *
     * <p>Os quatro mil de poder do original são muito de propósito: um altar nu não os tem, e por isso a Pedra
     * Sangrada é coisa de quem já criou um altar de verdade. Sem poder, o anel <b>fica de pé</b> e a pedra
     * fica lisa: nada se gasta, e dá para tentar outra vez.
     */
    @GameTest(maxTicks = 40)
    public void withSomeoneInTheRingAndNoAltarNothingHappens(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));
        risca(helper, meio, MIÚDO);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));
        if (Waystones.alvoNoAnel(level, meio) != porco) {
            helper.fail("o porco de pé no meio é o alvo do anel");
            porco.discard();
            return;
        }

        ItemEntity largada = larga(helper, meio, new ItemStack(OccultaItems.WAYSTONE));
        if (Waystones.tentaPrender(level, largada)) {
            helper.fail("sem altar não se prende a ninguém");
        }
        if (largada.isRemoved()) helper.fail("e a pedra fica onde estava");
        if (!Waystones.anelMiúdo(level, meio)) helper.fail("e o giz não se gasta");

        largada.discard();
        porco.discard();
        helper.succeed();
    }

    /** E a gente ganha do bicho, por mais perto que ele esteja. */
    @GameTest(maxTicks = 40)
    public void aPersonBeatsABeastHoweverCloseItStands(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2, 5.5)));

        if (Waystones.alvoNoAnel(level, meio) != quem) {
            helper.fail("a gente ganha do bicho, mesmo de mais longe");
        }

        porco.discard();
        helper.succeed();
    }

    /** A Pedra Sangrada guarda <b>quem</b>, e não onde. */
    @GameTest(maxTicks = 40)
    public void theBloodedStoneHoldsAWhoNotAWhere(GameTestHelper helper) {
        piso(helper);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 4));

        ItemStack pedra = Waystones.sangrada(porco);
        if (!pedra.is(OccultaItems.BLOODED_WAYSTONE)) helper.fail("é uma pedra sangrada");
        if (Waystones.lugar(pedra) != null) helper.fail("e não guarda lugar nenhum");
        if (!TaglockItem.isFor(pedra, porco)) helper.fail("guarda o porco");
        if (!Waystones.presa(pedra)) helper.fail("e ainda assim sabe ir a algum lugar");

        porco.discard();
        helper.succeed();
    }

    /**
     * <b>O meio do anel é onde o anel está</b>, e não o avesso dele.
     *
     * <p>Esta é a prova do engano do original: a pedra cai <b>de lado</b>, uma casa a leste do meio, e o anel
     * tem de ser achado no meio de verdade.
     */
    @GameTest(maxTicks = 40)
    public void theRingCentreIsWhereTheRingIs(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(5, 2, 5));
        risca(helper, meio, PEQUENO);

        if (!Waystones.anelPequeno(level, meio)) {
            helper.fail("doze glifos fazem o anel pequeno");
            return;
        }

        BlockPos deLado = meio.offset(1, 0, 0);
        BlockPos achado = Waystones.meioDoAnelPequeno(level, deLado);
        if (achado == null) {
            helper.fail("a pedra de lado ainda acha o anel");
            return;
        }
        if (!achado.equals(meio)) {
            helper.fail("e o meio é " + meio + ", não " + achado);
        }
        helper.succeed();
    }

    /** <b>O anel pequeno leva o que estiver nele</b>, e a pedra gasta-se ao abrir a porta. */
    @GameTest(maxTicks = 60)
    public void theSmallRingCarriesWhateverStandsInIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(5, 2, 5));
        risca(helper, meio, PEQUENO);

        /*
         * O destino: um chão de pedra longe do anel, e <b>por cima</b> dele. A suíte corre num mundo só e as
         * arenas ficam lado a lado no mesmo andar; quarenta blocos para o lado caem dentro da arena de outra
         * prova, que varre o que lá estiver quando se arruma. Para cima não há arena nenhuma.
         */
        BlockPos destino = helper.absolutePos(new BlockPos(5, 2, 5)).offset(0, 40, 0);
        level.setBlockAndUpdate(destino.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(destino, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(destino.above(), Blocks.AIR.defaultBlockState());

        ItemStack presa = new ItemStack(OccultaItems.BOUND_WAYSTONE, 2);
        presa.set(net.thaumcraft.occulta.OccultaComponents.WAYSTONE,
                new Waystones.Lugar(level.dimension(), destino));

        var galinha = helper.spawn(EntityTypes.CHICKEN, new BlockPos(5, 2, 5));
        double daqui = galinha.getY();

        ItemEntity largada = larga(helper, meio, presa);
        if (!Waystones.tentaLevar(level, largada)) {
            helper.fail("uma pedra presa no anel pequeno abre a porta");
            galinha.discard();
            return;
        }
        if (!largada.isRemoved()) helper.fail("e a pedra largada some");

        if (Math.abs(galinha.getY() - daqui) < 20.0) {
            helper.fail("a galinha vai para onde a pedra aponta; ficou em " + galinha.getY());
        }

        // <b>e a pedra que sobrou vai pela porta também</b>, como no original: ela cai no meio do anel, e o
        // anel leva tudo o que está nele, item largado incluído
        ItemEntity sobrou = achaPedra(helper, meio, OccultaItems.BOUND_WAYSTONE);
        if (sobrou != null) {
            helper.fail("a pedra que sobrou cai no anel, e o anel a leva junto");
            sobrou.discard();
            galinha.discard();
            return;
        }
        ItemEntity láLonge = achaPedra(helper, destino, OccultaItems.BOUND_WAYSTONE);
        if (láLonge == null) {
            helper.fail("ela tem de estar do outro lado");
        } else {
            if (láLonge.getItem().getCount() != 1) {
                helper.fail("e é uma só; são " + láLonge.getItem().getCount());
            }
            láLonge.discard();
        }

        galinha.discard();
        helper.succeed();
    }

    /** <b>Mas quem está inibido fica onde está.</b> */
    @GameTest(maxTicks = 60)
    public void inhibitionKeepsYouWhereYouAre(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(5, 2, 5));
        risca(helper, meio, PEQUENO);

        BlockPos destino = helper.absolutePos(new BlockPos(5, 2, 5)).offset(0, 0, 60);
        level.setBlockAndUpdate(destino.below(), Blocks.STONE.defaultBlockState());

        ItemStack presa = new ItemStack(OccultaItems.BOUND_WAYSTONE);
        presa.set(net.thaumcraft.occulta.OccultaComponents.WAYSTONE,
                new Waystones.Lugar(level.dimension(), destino));

        var galinha = helper.spawn(EntityTypes.CHICKEN, new BlockPos(5, 2, 5));
        galinha.addEffect(new MobEffectInstance(OccultaEffects.ENDER_INHIBITION, 200));
        if (!OccultaEffects.inibido(galinha, 0)) {
            helper.fail("a galinha está inibida");
            galinha.discard();
            return;
        }
        double daqui = galinha.getZ();

        Waystones.tentaLevar(level, larga(helper, meio, presa));
        if (Math.abs(galinha.getZ() - daqui) > 2.0) {
            helper.fail("quem está inibido não vai a lugar nenhum");
        }

        galinha.discard();
        helper.succeed();
    }

    /** A pedra largada daquele monte, se houver. */
    private static ItemEntity achaPedra(GameTestHelper helper, BlockPos meio,
                                        net.minecraft.world.item.Item qual) {
        var volta = new net.minecraft.world.phys.AABB(meio).inflate(3.0);
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, volta)) {
            if (item.isRemoved() || !item.getItem().is(qual)) continue;
            return item;
        }
        return null;
    }
}
