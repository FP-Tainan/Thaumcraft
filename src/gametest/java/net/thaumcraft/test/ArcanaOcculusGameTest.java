package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.OcculusMenu;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.SkillData;
import net.thaumcraft.arcana.SkillTree;
import net.thaumcraft.arcana.SpellPartItem;

/**
 * O caminho de comprar uma perícia, de ponta a ponta.
 *
 * <p>Esta prova existe porque o resto do ramo podia estar inteiro e <b>a progressão não funcionar</b>: o botão
 * da tela manda um número ao servidor, o servidor prova tudo de novo, e é aí que a perícia é sabida e a peça
 * entregue. Nada disso era exercitado — as provas da árvore conferiam a <i>conta</i>, e não o <i>caminho</i>.
 */
public class ArcanaOcculusGameTest {
    /** Comprar a primeira Forma: o menu aprende e entrega a peça. */
    @GameTest(maxTicks = 60)
    public void buyingASkillLearnsItAndGivesThePart(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);
        quem.getInventory().clearContent();

        var menu = new OcculusMenu(1, quem.getInventory());
        int qual = SkillTree.entries().indexOf(SkillTree.of(Shapes.SELF));
        if (qual < 0) {
            helper.fail("a Autoconjuração devia estar no quadro");
            return;
        }

        if (!menu.clickMenuButton(quem, qual)) {
            helper.fail("comprar a Autoconjuração devia dar certo");
            limpa(quem);
            return;
        }

        if (!SkillData.of(quem).knows(Shapes.SELF)) helper.fail("e ela passa a ser sabida");
        if (SkillData.of(quem).used(SkillTree.Point.BLUE) != 1) helper.fail("e gasta um ponto azul");

        // e a peça chega à mochila
        boolean tem = false;
        for (int i = 0; i < quem.getInventory().getContainerSize(); i++) {
            if (SpellPartItem.of(quem.getInventory().getItem(i)) == Shapes.SELF) tem = true;
        }
        if (!tem) helper.fail("e a peça chega à mão de quem aprendeu");

        limpa(quem);
        helper.succeed();
    }

    /** O servidor prova tudo de novo: um número fora do quadro não compra nada. */
    @GameTest(maxTicks = 60)
    public void aBadButtonBuysNothing(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        SkillData.set(quem, SkillData.NONE);
        var menu = new OcculusMenu(1, quem.getInventory());

        if (menu.clickMenuButton(quem, -1)) helper.fail("um número negativo não compra");
        if (menu.clickMenuButton(quem, 9999)) helper.fail("nem um fora do quadro");
        if (!SkillData.of(quem).known().isEmpty()) helper.fail("e nada é aprendido");

        limpa(quem);
        helper.succeed();
    }

    /** E uma perícia trancada também não: sem o que ela pede, o botão não vale. */
    @GameTest(maxTicks = 60)
    public void alockedSkillCannotBeBought(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);
        var menu = new OcculusMenu(1, quem.getInventory());

        // o Dano de Fogo precisa do Projétil antes
        int fogo = SkillTree.entries().indexOf(
                SkillTree.of(net.thaumcraft.arcana.Essences.FIRE_DAMAGE));
        if (menu.clickMenuButton(quem, fogo)) helper.fail("o Dano de Fogo precisa do Projétil antes");

        // comprando o Projétil, ele abre
        int projetil = SkillTree.entries().indexOf(SkillTree.of(Shapes.PROJECTILE));
        if (!menu.clickMenuButton(quem, projetil)) helper.fail("o Projétil é raiz e compra-se");
        if (!menu.clickMenuButton(quem, fogo)) helper.fail("e então o Dano de Fogo abre");

        limpa(quem);
        helper.succeed();
    }

    /** Gastos os três pontos de começo, não se compra mais nada até subir de nível. */
    @GameTest(maxTicks = 60)
    public void threePointsBuyThreeThings(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);
        var menu = new OcculusMenu(1, quem.getInventory());

        for (var forma : java.util.List.of(Shapes.SELF, Shapes.TOUCH, Shapes.PROJECTILE)) {
            int qual = SkillTree.entries().indexOf(SkillTree.of(forma));
            if (!menu.clickMenuButton(quem, qual)) helper.fail("a " + forma.name() + " cabe nos três");
        }

        // a quarta raiz azul já não cabe
        int quarta = SkillTree.entries().indexOf(
                SkillTree.of(net.thaumcraft.arcana.Modifiers.TARGET_NONSOLID_BLOCKS));
        if (menu.clickMenuButton(quem, quarta)) {
            helper.fail("sem ponto nenhum, não se compra mais");
        }

        limpa(quem);
        helper.succeed();
    }

    /** E há um item para cada peça que se pode comprar. */
    @GameTest
    public void everyBuyableSkillHasAnItem(GameTestHelper helper) {
        for (var perícia : SkillTree.entries()) {
            if (ArcanaItems.itemOf(perícia.part()) == null) {
                helper.fail("sem item, comprar " + perícia.part().name() + " não entregaria nada");
            }
        }
        helper.succeed();
    }

    private static void limpa(ServerPlayer quem) {
        SkillData.set(quem, SkillData.NONE);
        Mana.set(quem, Mana.NONE);
        quem.getInventory().clearContent();
    }
}
