package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.wolf.Lycanthropy;
import net.thaumcraft.occulta.wolf.MoonCharmItem;
import net.thaumcraft.occulta.wolf.Werewolf;
import net.thaumcraft.occulta.wolf.WerewolfStats;
import net.thaumcraft.occulta.wolf.WerewolfTick;

/**
 * A licantropia do jogador: o grau, a forma, a lua e o preço.
 *
 * <p>A prova que carrega a fatia é a do <b>preço</b>. Os poderes de um lobisomem não são o que o define — o
 * que o define é que, ao virar bicho, ele <b>larga tudo o que veste</b>, e o único jeito de escolher quando
 * isso acontece é um amuleto que ele tem de ter ganho antes.
 */
public class OccultaWerewolfPlayerGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest(maxTicks = 20)
    public void herNumbersAreTheOriginals(GameTestHelper helper) {
        if (Werewolf.TETO != 10) helper.fail("são dez graus");
        if (Werewolf.MANDA_NA_MUDANÇA != 2) helper.fail("do segundo em diante ele manda na mudança");
        if (Werewolf.LOBISOMEM_AOS != 5) helper.fail("e do quinto pode ser lobisomem");
        if (WerewolfTick.DE_QUANTO_EM_QUANTO != 40) helper.fail("a lua se olha de dois em dois segundos");
        if (MoonCharmItem.AGUENTA != 50) helper.fail("e o amuleto aguenta cinquenta mudanças");
        helper.succeed();
    }

    /** <b>O grau começa em zero e para em dez.</b> */
    @GameTest(maxTicks = 40)
    public void theLevelStartsAtZeroAndStopsAtTen(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        if (Werewolf.grauDe(quem) != 0) helper.fail("ninguém nasce lobisomem");
        if (Lycanthropy.éMesmoDeGente(quem)) helper.fail("e quem tem grau zero não é lobisomem");

        Werewolf.grau(quem, 1);
        if (!Lycanthropy.éMesmoDeGente(quem)) helper.fail("com um grau, é");
        if (Lycanthropy.é(quem)) helper.fail("mas de gente não conta como bicho");

        Werewolf.grau(quem, 99);
        if (Werewolf.grauDe(quem) != Werewolf.TETO) {
            helper.fail("o grau para em dez; ficou " + Werewolf.grauDe(quem));
        }

        // e baixar a zero devolve a forma de gente
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        Werewolf.grau(quem, 0);
        if (Werewolf.formaDe(quem) != Werewolf.Forma.GENTE) {
            helper.fail("sem grau nenhum, ele volta a ser gente");
        }
        helper.succeed();
    }

    /** <b>Em forma de bicho ele conta como lobisomem</b>; de gente, não. */
    @GameTest(maxTicks = 40)
    public void inBeastFormHeCountsAsAWerewolf(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        Werewolf.grau(quem, 5);

        if (Lycanthropy.é(quem)) helper.fail("de gente, a prata não lhe pega");
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        if (!Lycanthropy.é(quem)) helper.fail("de lobo, sim");
        Werewolf.forma(quem, Werewolf.Forma.LOBISOMEM);
        if (!Lycanthropy.é(quem)) helper.fail("e de lobisomem também");

        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /** <b>Virar bicho custa tudo o que ele veste</b> — menos o amuleto. */
    @GameTest(maxTicks = 40)
    public void turningCostsEverythingHeWears(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 2, 3.5)));
        Werewolf.grau(quem, 3);

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        quem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));

        Werewolf.vira(level, quem, Werewolf.Forma.LOBO);
        if (!quem.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) helper.fail("a armadura cai");
        if (!quem.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) helper.fail("toda ela");
        if (!quem.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            helper.fail("e o lobo não tem mãos: o que estava nelas cai também");
        }
        if (!quem.hasEffect(MobEffects.NIGHT_VISION)) helper.fail("mas ele passa a ver no escuro");

        // e o lobisomem guarda as mãos
        quem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        Werewolf.vira(level, quem, Werewolf.Forma.LOBISOMEM);
        if (quem.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            helper.fail("o lobisomem tem mãos, e guarda o que está nelas");
        }
        if (!quem.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) helper.fail("mas a armadura cai na mesma");

        quem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /** <b>E o Amuleto da Lua é a única coisa que fica.</b> */
    @GameTest(maxTicks = 40)
    public void theMoonCharmIsTheOneThingThatStays(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 2, 3.5)));
        Werewolf.grau(quem, 5);

        quem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(OccultaItems.MOON_CHARM));
        Werewolf.vira(level, quem, Werewolf.Forma.LOBO);
        if (!quem.getItemBySlot(EquipmentSlot.MAINHAND).is(OccultaItems.MOON_CHARM)) {
            helper.fail("o amuleto não cai: largá-lo seria perder o que desfaz a transformação");
        }

        quem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /** <b>A lua cheia manda</b>, e o amuleto e o acônito não a deixam mandar. */
    @GameTest(maxTicks = 60)
    public void theFullMoonCommandsAndTwoThingsStopIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 2, 3.5)));
        Werewolf.grau(quem, 1);

        // fora da lua cheia, um que esteja de bicho volta a ser gente
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        if (!net.thaumcraft.occulta.wolf.Moon.cheia(level) || level.isBrightOutside()) {
            WerewolfTick.olha(level, quem);
            if (Werewolf.formaDe(quem) != Werewolf.Forma.GENTE) {
                helper.fail("passada a lua, ele volta a ser gente");
            }
        }

        // mas com o amuleto na mochila a forma fica onde está
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        quem.getInventory().add(new ItemStack(OccultaItems.MOON_CHARM));
        if (!Werewolf.temAmuleto(quem)) {
            helper.fail("o amuleto na mochila conta, não precisa de estar na mão");
            return;
        }
        WerewolfTick.olha(level, quem);
        if (Werewolf.formaDe(quem) != Werewolf.Forma.LOBO) {
            helper.fail("com o amuleto, a lua passa e não lhe toca");
        }

        // e o acônito também segura
        quem.getInventory().clearContent();
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        quem.addEffect(new MobEffectInstance(OccultaEffects.WOLFSBANE, 200));
        if (!Werewolf.aLuaNãoLhePega(quem)) helper.fail("o acônito segura do mesmo jeito");
        WerewolfTick.olha(level, quem);
        if (Werewolf.formaDe(quem) != Werewolf.Forma.LOBO) {
            helper.fail("com acônito, ele fica como está");
        }

        quem.removeEffect(OccultaEffects.WOLFSBANE);
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /** <b>O que cada forma dá</b>, pelas duas tabelas do original. */
    @GameTest(maxTicks = 40)
    public void whatEachShapeGives(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        double velocidadeDeGente = quem.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double vidaDeGente = quem.getMaxHealth();

        // o lobo é depressa desde o primeiro grau
        Werewolf.grau(quem, 1);
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        if (quem.getAttributeValue(Attributes.MOVEMENT_SPEED) <= velocidadeDeGente) {
            helper.fail("um lobo de grau um já é mais depressa que gente");
        }

        // o lobisomem não vale nada até o quinto
        Werewolf.forma(quem, Werewolf.Forma.GENTE);
        Werewolf.grau(quem, 4);
        Werewolf.forma(quem, Werewolf.Forma.LOBISOMEM);
        if (quem.getMaxHealth() != vidaDeGente) {
            helper.fail("um lobisomem de grau quatro não tem vida a mais");
        }

        // e ao quinto ganha vinte de vida de uma vez
        Werewolf.grau(quem, 5);
        if (quem.getMaxHealth() != vidaDeGente + 20.0) {
            helper.fail("ao quinto ele ganha vinte de vida; tem " + quem.getMaxHealth());
        }

        // e voltando a gente, tudo sai
        Werewolf.forma(quem, Werewolf.Forma.GENTE);
        if (quem.getMaxHealth() != vidaDeGente) helper.fail("de gente, a vida volta ao que era");
        if (quem.getAttributeValue(Attributes.MOVEMENT_SPEED) != velocidadeDeGente) {
            helper.fail("e a velocidade também");
        }

        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /** As duas tabelas são as do original, linha por linha. */
    @GameTest(maxTicks = 20)
    public void thetwoTablesAreTheOriginals(GameTestHelper helper) {
        if (WerewolfStats.LOBO.length != 11) helper.fail("a tabela do lobo tem onze linhas");
        if (WerewolfStats.LOBISOMEM.length != 11) helper.fail("e a do lobisomem também");

        if (WerewolfStats.LOBO[1].velocidade() != 0.5f) helper.fail("o lobo de grau um anda meio mais depressa");
        if (WerewolfStats.LOBO[10].velocidade() != 1.75f) helper.fail("e o de grau dez, uma vez e três quartos");
        if (WerewolfStats.LOBISOMEM[4].vida() != 0) helper.fail("o lobisomem de grau quatro não tem vida a mais");
        if (WerewolfStats.LOBISOMEM[5].vida() != 20) helper.fail("e o de grau cinco tem vinte");
        if (WerewolfStats.LOBISOMEM[10].vida() != 40) helper.fail("e o de dez, quarenta");

        // o teto da pancada é o único número que melhora baixando
        if (WerewolfStats.LOBO[1].tetoDaPancada() != 4.0f) helper.fail("o teto começa em quatro");
        if (WerewolfStats.LOBO[10].tetoDaPancada() != 2.0f) helper.fail("e acaba em dois");
        helper.succeed();
    }

    /** <b>Do segundo grau ele manda na mudança; do quinto, pode ser lobisomem.</b> */
    @GameTest(maxTicks = 40)
    public void controlComesAtTwoAndTheWolfmanAtFive(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        Werewolf.grau(quem, 1);
        if (Werewolf.mandaNaMudança(quem)) helper.fail("de grau um, a lua é que manda");

        Werewolf.grau(quem, 2);
        if (!Werewolf.mandaNaMudança(quem)) helper.fail("de grau dois, ele manda");
        if (Werewolf.podeSerLobisomem(quem)) helper.fail("mas ainda não pode ser lobisomem");

        Werewolf.grau(quem, 5);
        if (!Werewolf.podeSerLobisomem(quem)) helper.fail("de grau cinco, pode");

        // e o amuleto muda mais depressa quanto maior o grau
        if (MoonCharmItem.quandoMuda(10) <= MoonCharmItem.quandoMuda(2)) {
            helper.fail("quanto maior o grau, mais depressa o amuleto muda a forma");
        }

        Werewolf.grau(quem, 0);
        helper.succeed();
    }
    /** <b>E um lobo cabe onde uma pessoa não cabe.</b> */
    @GameTest(maxTicks = 40)
    public void aWolfFitsWhereAPersonDoesNot(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        float deGente = quem.getBbHeight();

        Werewolf.grau(quem, 3);
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        quem.refreshDimensions();
        if (quem.getBbHeight() >= deGente) {
            helper.fail("de lobo ele é mais baixo: era " + deGente + ", é " + quem.getBbHeight());
        }
        if (quem.getBbHeight() >= 1.0f) helper.fail("e cabe debaixo de um bloco");

        // o lobisomem é do tamanho de gente
        Werewolf.forma(quem, Werewolf.Forma.LOBISOMEM);
        quem.refreshDimensions();
        if (quem.getBbHeight() != deGente) helper.fail("mas o lobisomem é do tamanho de gente");

        // e as duas formas sobem um degrau mais alto
        double passadaDeBicho = quem.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT);
        Werewolf.grau(quem, 0);
        quem.refreshDimensions();
        double passadaDeGente = quem.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT);
        if (passadaDeBicho <= passadaDeGente) helper.fail("um bicho sobe um degrau mais alto que gente");

        helper.succeed();
    }
}
