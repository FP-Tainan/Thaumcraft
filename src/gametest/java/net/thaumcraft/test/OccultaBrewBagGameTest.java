package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.BrewBagChoice;
import net.thaumcraft.occulta.brew.BrewBagItem;
import net.thaumcraft.occulta.brew.BrewItem;

import java.util.List;

/**
 * A <b>Bolsa de Cozimentos</b>: oito casas e um gesto.
 *
 * <p>A prova que carrega a fatia é a dos <b>oito rumos</b>. Ela é o item todo: errando a ordem das
 * perguntas, as quatro diagonais somem e a bolsa fica com quatro casas; errando a tabela que leva o traço
 * à casa, sai sempre o frasco errado — e nada no jogo diria qual.
 */
public class OccultaBrewBagGameTest {
    /** Um frasco de cozimento de atirar: a pólvora é o que o torna arremessável. */
    private static ItemStack frascoAtirável() {
        return BrewItem.of(OccultaItems.BREW, List.of(Items.NETHER_WART, Items.SUGAR, Items.GUNPOWDER));
    }

    /** Uma bolsa com um frasco em cada casa que a lista disser. */
    private static ItemStack bolsaCom(int... casas) {
        ItemStack bolsa = new ItemStack(OccultaItems.BREW_BAG);
        NonNullList<ItemStack> dentro = NonNullList.withSize(BrewBagItem.CASAS, ItemStack.EMPTY);
        for (int casa : casas) dentro.set(casa, frascoAtirável());
        BrewBagItem.guarda(bolsa, dentro);
        return bolsa;
    }

    /** <b>São oito casas</b>, e não nove: os rumos da cabeça são oito. */
    @GameTest(maxTicks = 20)
    public void sheHasEightSlotsBecauseTheHeadHasEightWays(GameTestHelper helper) {
        if (BrewBagItem.CASAS != 8) {
            helper.fail("são oito casas: são " + BrewBagItem.CASAS);
            return;
        }
        ItemStack bolsa = bolsaCom(0, 7);
        if (BrewBagItem.naCasa(bolsa, 0).isEmpty()) helper.fail("a primeira casa tem frasco");
        if (BrewBagItem.naCasa(bolsa, 7).isEmpty()) helper.fail("e a oitava também");
        if (!BrewBagItem.naCasa(bolsa, 3).isEmpty()) helper.fail("e as do meio estão vazias");
        if (!BrewBagItem.naCasa(bolsa, 8).isEmpty()) helper.fail("e a nona não existe");
        if (!bolsa.has(DataComponents.CONTAINER)) helper.fail("o que está dentro mora no próprio item");
        helper.succeed();
    }

    /** <b>Só cozimento de atirar entra.</b> */
    @GameTest(maxTicks = 20)
    public void onlyThrownBrewsFitInside(GameTestHelper helper) {
        if (!BrewBagItem.cabe(frascoAtirável())) {
            helper.fail("um frasco de atirar cabe");
            return;
        }
        // os frascos do Caldeirão de Pote são todos de atirar
        if (!BrewBagItem.cabe(new ItemStack(OccultaItems.BREW_OF_INK))) {
            helper.fail("e um frasco de pote também");
            return;
        }
        ItemStack debeber = BrewItem.of(OccultaItems.BREW, List.of(Items.NETHER_WART, Items.SUGAR));
        if (BrewBagItem.cabe(debeber)) helper.fail("mas o de beber não cabe");
        if (BrewBagItem.cabe(new ItemStack(Items.DIAMOND))) helper.fail("nem um diamante");
        helper.succeed();
    }

    /**
     * <b>Os oito rumos</b>, e a ordem das perguntas que os torna oito.
     *
     * <p>Uma diagonal pede três graus e meio em cada eixo; um rumo reto pede sete. E as diagonais
     * perguntam-se primeiro — <b>invertida a ordem, elas nunca sairiam</b>, porque um giro de dez graus
     * para cima e dez para a direita passa dos sete nos dois eixos e o primeiro teste a responder ganha.
     */
    @GameTest(maxTicks = 20)
    public void eightWaysAndTheDiagonalsComeFirst(GameTestHelper helper) {
        // os quatro retos: muito num eixo, nada no outro
        if (BrewBagItem.qualTraço(10.0f, 0.0f) != 0) {
            helper.fail("para cima é o traço zero");
            return;
        }
        if (BrewBagItem.qualTraço(-10.0f, 0.0f) != 1) {
            helper.fail("para baixo é o um");
            return;
        }
        if (BrewBagItem.qualTraço(0.0f, -10.0f) != 2) {
            helper.fail("para a direita é o dois");
            return;
        }
        if (BrewBagItem.qualTraço(0.0f, 10.0f) != 3) {
            helper.fail("e para a esquerda é o três");
            return;
        }

        // as quatro diagonais, e são elas que a ordem salva
        if (BrewBagItem.qualTraço(10.0f, -10.0f) != 4) {
            helper.fail("cima e direita é a diagonal quatro, e não o traço de cima");
            return;
        }
        if (BrewBagItem.qualTraço(10.0f, 10.0f) != 6) {
            helper.fail("cima e esquerda é a seis");
            return;
        }
        if (BrewBagItem.qualTraço(-10.0f, -10.0f) != 7) {
            helper.fail("baixo e direita é a sete");
            return;
        }
        if (BrewBagItem.qualTraço(-10.0f, 10.0f) != 5) {
            helper.fail("e baixo e esquerda é a cinco");
            return;
        }

        /*
         * E os dois limiares: quatro graus em cada eixo já é diagonal, mas quatro num eixo só ainda não é
         * nada — é o que separa um gesto de um tremor de rato.
         */
        if (BrewBagItem.qualTraço(4.0f, -4.0f) != 4) {
            helper.fail("quatro graus em cada eixo já é diagonal");
            return;
        }
        if (BrewBagItem.qualTraço(4.0f, 0.0f) != -1) {
            helper.fail("mas quatro num eixo só ainda não é traço nenhum");
            return;
        }
        if (BrewBagItem.qualTraço(0.0f, 0.0f) != -1) helper.fail("e cabeça parada não é traço nenhum");
        helper.succeed();
    }

    /**
     * <b>E cada traço cai na sua casa</b>, à roda do relógio a partir do cima.
     *
     * <p>As duas tabelas do original são uma a volta da outra, e é isso que a prova mede: o que a roda da
     * tela desenha na casa <i>n</i> é o que o traço leva à casa <i>n</i>.
     */
    @GameTest(maxTicks = 20)
    public void eachStrokeLandsOnItsOwnSlot(GameTestHelper helper) {
        int[] àRoda = {0, 4, 2, 7, 1, 5, 3, 6};
        for (int casa = 0; casa < BrewBagItem.CASAS; casa++) {
            if (BrewBagItem.CASA_PARA_TRAÇO[casa] != àRoda[casa]) {
                helper.fail("a casa " + casa + " é o traço " + àRoda[casa]);
                return;
            }
            if (BrewBagItem.TRAÇO_PARA_CASA[àRoda[casa]] != casa) {
                helper.fail("e o traço " + àRoda[casa] + " volta à casa " + casa);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Largá-la atira o frasco escolhido</b>, e só ele.
     *
     * <p>A escolha vem do lado de cá e o servidor guardou-a; largar a bolsa gasta <b>um</b> frasco daquela
     * casa e deixa as outras em paz.
     */
    @GameTest(maxTicks = 40)
    public void lettingGoThrowsTheChosenOne(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        ItemStack bolsa = bolsaCom(2, 5);
        quem.getInventory().setItem(0, bolsa);
        quem.getInventory().setSelectedSlot(0);

        // sem escolha nenhuma, largar não faz nada e em silêncio
        bolsa.getItem().releaseUsing(bolsa, quem.level(), quem, 0);
        if (BrewBagItem.naCasa(bolsa, 2).isEmpty()) {
            helper.fail("sem escolha, nada sai");
            return;
        }

        BrewBagChoice.escolhe(quem, 2);
        bolsa.getItem().releaseUsing(bolsa, quem.level(), quem, 0);
        if (!BrewBagItem.naCasa(bolsa, 2).isEmpty()) {
            helper.fail("a casa escolhida gasta o frasco dela");
            return;
        }
        if (BrewBagItem.naCasa(bolsa, 5).isEmpty()) {
            helper.fail("e as outras ficam como estavam");
            return;
        }
        if (BrewBagChoice.escolhida(quem) != null) {
            helper.fail("e a escolha esquece-se depois de largar");
            return;
        }

        // uma casa vazia não atira nada
        BrewBagChoice.escolhe(quem, 2);
        bolsa.getItem().releaseUsing(bolsa, quem.level(), quem, 0);
        if (BrewBagItem.naCasa(bolsa, 5).isEmpty()) helper.fail("e a casa vazia não tira do vizinho");
        helper.succeed();
    }

    /** <b>Ela nasce da cor do cacau</b>, e tinge-se como uma armadura de couro. */
    @GameTest(maxTicks = 20)
    public void sheIsCocoaBrownUntilSomeoneDyesHer(GameTestHelper helper) {
        ItemStack bolsa = new ItemStack(OccultaItems.BREW_BAG);
        if (BrewBagItem.cor(bolsa) != BrewBagItem.CACAU) {
            helper.fail("a bolsa nasce cor de cacau: deu " + Integer.toHexString(BrewBagItem.cor(bolsa)));
            return;
        }
        bolsa.set(DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(0xFF3C44AA));
        if (BrewBagItem.cor(bolsa) != 0x3C44AA) {
            helper.fail("e tingida ela fica da cor da tinta");
            return;
        }
        helper.succeed();
    }

    /** <b>E a receita dela</b>: oito couros curtidos à volta de um fio de ouro. */
    @GameTest(maxTicks = 40)
    public void eightLeathersAroundOneGoldenThread(GameTestHelper helper) {
        ItemStack couro = new ItemStack(OccultaItems.IMPREGNATED_LEATHER);
        ItemStack fio = new ItemStack(OccultaItems.GOLDEN_THREAD);
        var entrada = CraftingInput.of(3, 3, List.of(
                couro, couro, couro,
                couro, fio, couro,
                couro, couro, couro));
        var saiu = helper.getLevel().recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, entrada, helper.getLevel());
        if (saiu.isEmpty()) {
            helper.fail("a receita da bolsa devia estar lá");
            return;
        }
        ItemStack feita = saiu.get().value().assemble(entrada);
        if (!feita.is(OccultaItems.BREW_BAG)) helper.fail("e o que sai dela é a bolsa");
        helper.succeed();
    }
}
