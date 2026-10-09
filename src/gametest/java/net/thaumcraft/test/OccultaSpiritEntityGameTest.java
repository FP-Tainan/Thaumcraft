package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.spirit.FlyerGoals;
import net.thaumcraft.occulta.spirit.SpiritEntity;
import net.thaumcraft.occulta.spirit.SubduedSpiritItem;
import net.thaumcraft.occulta.waystone.Waystones;

/**
 * O <b>Espírito</b>: a lanterna que deriva, e a moeda com que se pagam os fetiches.
 *
 * <p>A prova que carrega a fatia é a do <b>Espírito Dominado</b>: o comum solta um que fica, o da Aldeia
 * solta um que vive dez segundos e <b>devolve o item</b> ao acabar. É uma bússola que se gasta ao apontar, e
 * que por isso não se gasta nunca — se quem a soltou for atrás dela.
 */
public class OccultaSpiritEntityGameTest {
    /** Os oito do anel miúdo, como o {@code Waystones} os conta. */
    private static final int[][] MIÚDO = {
            {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1},
    };

    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    // ------------------------------------------------------------------ os números

    /** Os números dele são os do original. */
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (SpiritEntity.VIDA != 4.0 || SpiritEntity.VELOCIDADE != 0.4 || SpiritEntity.MURRO != 4.0) {
            helper.fail("quatro de vida, quatro décimos de passo e quatro de murro");
        }
        if (SpiritEntity.PRAZO != 200) helper.fail("e dez segundos de prazo");
        if (SpiritEntity.PESO != 1 || SpiritEntity.DE_DOIS != 2 || SpiritEntity.A_CINCO != 5) {
            helper.fail("peso um, de dois a cinco de cada vez");
        }
        if (SpiritEntity.ACIMA_DE != 60 || SpiritEntity.LUZ != 8 || SpiritEntity.UMA_EM_DEZ != 10) {
            helper.fail("acima de sessenta, com mais de oito de luz, uma vez em dez");
        }
        if (SpiritEntity.FICA != 0 || SpiritEntity.DA_ALDEIA != 1 || SpiritEntity.SEM_DESPOJO != 2) {
            helper.fail("e os três feitios são zero, um e dois");
        }
        helper.succeed();
    }

    /** <b>O que o tenta é a Vontade Concentrada, e só ela.</b> */
    @GameTest
    public void onlyFocusedWillTemptsHim(GameTestHelper helper) {
        if (!SpiritEntity.tenta(new ItemStack(OccultaItems.FOCUSED_WILL))) {
            helper.fail("a Vontade Concentrada o tenta");
        }
        if (SpiritEntity.tenta(new ItemStack(OccultaItems.CONDENSED_FEAR))) {
            helper.fail("e o medo não");
        }
        if (SpiritEntity.tenta(new ItemStack(Items.BONE))) helper.fail("nem o osso, que ele come");
        if (SpiritEntity.tenta(ItemStack.EMPTY)) helper.fail("nem a mão vazia");
        helper.succeed();
    }

    /** <b>Clicar nele não faz nada.</b> */
    @GameTest
    public void heCannotBeTamed(GameTestHelper helper) {
        piso(helper);
        var ele = helper.spawn(OccultaEntities.SPIRIT, new BlockPos(3, 3, 3));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(OccultaItems.FOCUSED_WILL));

        if (ele.mobInteract(quem, net.minecraft.world.InteractionHand.MAIN_HAND)
                != InteractionResult.PASS) {
            helper.fail("o toque não faz nada");
        }
        if (ele.isTame()) helper.fail("e ele não se doma");

        ele.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ onde ele nasce

    /**
     * <b>Ele não nasce fora do mundo dos sonhos.</b>
     *
     * <p>É a primeira linha do {@code getCanSpawnHere} do original, e é ela que faz o Espírito valer o que
     * vale: está na lista de nascimentos de todos os biomas de terra, e mesmo assim não há um só no mundo
     * de cima.
     */
    @GameTest
    public void heNeverSpawnsOutsideTheDream(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(onde.below(), Blocks.GRASS_BLOCK.defaultBlockState());

        for (int volta = 0; volta < 40; volta++) {
            if (SpiritEntity.podeNascer(OccultaEntities.SPIRIT, level,
                    net.minecraft.world.entity.EntitySpawnReason.NATURAL, onde.above(60),
                    level.getRandom())) {
                helper.fail("aqui não é o outro lado");
                return;
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ o prazo e o despojo

    /** <b>O Espírito Dominado solta um espírito que fica.</b> */
    @GameTest
    public void thePlainSubduedSpiritLetsOneOutToStay(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 1, 3));

        var quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5);
        ItemStack naMão = new ItemStack(OccultaItems.SUBDUED_SPIRIT, 2);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, naMão);

        var resultado = naMão.useOn(new net.minecraft.world.item.context.UseOnContext(
                level, quem, net.minecraft.world.InteractionHand.MAIN_HAND, naMão,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde), net.minecraft.core.Direction.UP,
                        onde, false)));
        if (resultado != InteractionResult.SUCCESS) helper.fail("usá-lo num bloco solta o espírito");

        var apareceram = level.getEntitiesOfClass(SpiritEntity.class, new AABB(onde).inflate(4.0));
        if (apareceram.size() != 1) {
            helper.fail("devia haver um espírito; há " + apareceram.size());
            return;
        }
        var ele = apareceram.getFirst();
        if (ele.deEmpréstimo()) helper.fail("e esse fica: não tem prazo");
        if (ele.feitio() != SpiritEntity.FICA) helper.fail("e é do feitio que larga o item comum");
        if (!ele.isPersistenceRequired()) helper.fail("e não some sozinho");

        ele.discard();
        helper.succeed();
    }

    /**
     * <b>E o da Aldeia solta um que tem dez segundos e devolve o item.</b>
     *
     * <p>Esta é a prova que carrega a fatia. A bússola não se gasta: ela <b>vai à frente</b>, e quem a
     * seguir apanha-a outra vez no sítio onde ela parou.
     */
    @GameTest(maxTicks = 80)
    public void theVillageOneComesBack(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 1, 3));

        var ele = helper.spawn(OccultaEntities.SPIRIT, new BlockPos(3, 3, 3));
        ele.vaiParaAAldeia(level, SpiritEntity.DA_ALDEIA);
        if (!ele.deEmpréstimo()) helper.fail("com rumo, ele tem prazo");
        if (ele.feitio() != SpiritEntity.DA_ALDEIA) helper.fail("e é o da aldeia");

        // o prazo acaba: ele some e deixa o item
        for (int volta = 0; volta <= SpiritEntity.PRAZO; volta++) {
            if (ele.isRemoved()) break;
            ele.oPrazo(level);
        }
        if (!ele.isRemoved()) helper.fail("ao fim de dez segundos, ele some");

        var caiu = level.getEntitiesOfClass(ItemEntity.class, new AABB(onde).inflate(8.0));
        boolean devolveu = caiu.stream()
                .anyMatch(item -> item.getItem().is(OccultaItems.SUBDUED_SPIRIT_VILLAGE));
        if (!devolveu) helper.fail("e devolve o Espírito Dominado da Aldeia");
        for (var item : caiu) item.discard();
        helper.succeed();
    }

    /** <b>E o feitio que a Pedra de Caminho usa não larga nada.</b> */
    @GameTest(maxTicks = 80)
    public void theWaystoneOneLeavesNothing(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 1, 3));

        var ele = helper.spawn(OccultaEntities.SPIRIT, new BlockPos(3, 3, 3));
        ele.vaiParaAAldeia(level, SpiritEntity.SEM_DESPOJO);
        for (int volta = 0; volta <= SpiritEntity.PRAZO; volta++) {
            if (ele.isRemoved()) break;
            ele.oPrazo(level);
        }
        if (!ele.isRemoved()) helper.fail("ele some na mesma");

        var caiu = level.getEntitiesOfClass(ItemEntity.class, new AABB(onde).inflate(8.0));
        if (!caiu.isEmpty()) {
            helper.fail("mas não larga nada; largou " + caiu.getFirst().getItem());
            for (var item : caiu) item.discard();
            return;
        }
        helper.succeed();
    }

    /** Os dois itens sabem qual deles é qual. */
    @GameTest
    public void theTwoItemsKnowWhichTheyAre(GameTestHelper helper) {
        if (!(OccultaItems.SUBDUED_SPIRIT instanceof SubduedSpiritItem comum)
                || !(OccultaItems.SUBDUED_SPIRIT_VILLAGE instanceof SubduedSpiritItem aldeia)) {
            helper.fail("os dois são Espíritos Dominados");
            return;
        }
        if (comum.feitio() != SpiritEntity.FICA) helper.fail("o comum solta um que fica");
        if (aldeia.feitio() != SpiritEntity.DA_ALDEIA) helper.fail("e o da aldeia um que aponta");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o anel de giz de Ritual

    /**
     * <b>A Pedra Sintonizada largada num anel miúdo de giz de Ritual chama um espírito.</b>
     *
     * <p>Era a única coisa que ficava de fora da fatia da Pedra de Caminho, declarada lá, e cai com esta.
     */
    @GameTest
    public void theRitualRingCallsASpirit(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 4));
        for (int[] casa : MIÚDO) {
            level.setBlockAndUpdate(meio.offset(casa[0], 0, casa[1]),
                    OccultaBlocks.RITUAL_GLYPH.defaultBlockState());
        }
        if (!Waystones.anelMiúdoDeRitual(level, meio)) {
            helper.fail("oito glifos de Ritual fazem o anel miúdo dele");
            return;
        }

        var largada = new ItemEntity(level, meio.getX() + 0.5, meio.getY() + 0.1, meio.getZ() + 0.5,
                new ItemStack(OccultaItems.ATTUNED_STONE, 3));
        largada.setNoGravity(true);
        level.addFreshEntity(largada);

        if (!Waystones.tentaChamarOEspírito(level, largada)) {
            helper.fail("a pedra no meio do anel chama o espírito");
            return;
        }
        if (!largada.isRemoved()) helper.fail("e a pedra largada some");

        var apareceram = level.getEntitiesOfClass(SpiritEntity.class, new AABB(meio).inflate(4.0));
        if (apareceram.size() != 1) {
            helper.fail("devia haver um espírito; há " + apareceram.size());
            return;
        }
        if (apareceram.getFirst().feitio() != SpiritEntity.SEM_DESPOJO) {
            helper.fail("e é do feitio que não devolve nada");
        }
        if (Waystones.anelMiúdoDeRitual(level, meio)) helper.fail("e os oito glifos estouram");

        // as duas pedras que sobram caem de volta
        boolean sobrou = level.getEntitiesOfClass(ItemEntity.class, new AABB(meio).inflate(4.0)).stream()
                .anyMatch(item -> item.getItem().is(OccultaItems.ATTUNED_STONE)
                        && item.getItem().getCount() == 2);
        if (!sobrou) helper.fail("e as duas que sobram caem de volta no chão");

        for (var item : level.getEntitiesOfClass(ItemEntity.class, new AABB(meio).inflate(4.0))) {
            item.discard();
        }
        apareceram.getFirst().discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ a deriva

    /**
     * <b>A linha reta é conferida antes de empurrar.</b>
     *
     * <p>É a conta que todas as quatro metas de voo partilham, e é ela que faz um espírito parecer
     * indeciso: ele não contorna nada — ele desiste e escolhe outro rumo.
     */
    @GameTest
    public void theStraightLineIsCheckedBeforeDrifting(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ele = helper.spawn(OccultaEntities.SPIRIT, new BlockPos(1, 3, 3));
        BlockPos daqui = ele.blockPosition();

        double x = daqui.getX() + 6.0;
        double y = ele.getY();
        double z = daqui.getZ();
        if (!FlyerGoals.rumoLivre(ele, x, y, z, 6.0)) helper.fail("por ar, o rumo está livre");

        // uma parede no meio, do chão ao teto da arena
        for (int alto = 0; alto < 4; alto++) {
            for (int lado = -2; lado <= 2; lado++) {
                level.setBlockAndUpdate(new BlockPos(daqui.getX() + 3, daqui.getY() + alto - 1,
                        daqui.getZ() + lado), Blocks.STONE.defaultBlockState());
            }
        }
        if (FlyerGoals.rumoLivre(ele, x, y, z, 6.0)) helper.fail("com parede, não está");

        ele.discard();
        helper.succeed();
    }
}
