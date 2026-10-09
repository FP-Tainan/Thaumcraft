package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.DivinerItem;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.hunter.CaneSwordItem;
import net.thaumcraft.occulta.vampire.Blood;
import net.thaumcraft.occulta.vampire.BloodReserve;
import net.thaumcraft.occulta.vampire.Vampire;

import java.util.List;

/**
 * A <b>Bengala-Espada</b>, o <b>cantil de sangue</b> que ela abre, e as duas <b>Varas de Rabdomante</b>.
 */
public class OccultaCaneSwordGameTest {
    /**
     * <b>Sacar muda o dano.</b>
     *
     * <p>Guardada ela soma um; sacada soma seis, que é o que uma espada de diamante soma. E ela nasce
     * guardada, que é o estado em que uma bengala deve estar.
     */
    @GameTest
    public void drawingTheBladeChangesTheDamage(GameTestHelper helper) {
        ItemStack bengala = new ItemStack(OccultaItems.CANE_SWORD);
        if (CaneSwordItem.sacada(bengala)) helper.fail("uma bengala nova está guardada");
        if (soma(bengala) != CaneSwordItem.GUARDADA) {
            helper.fail("guardada ela soma um, e soma " + soma(bengala));
        }

        CaneSwordItem.saca(bengala, true);
        if (!CaneSwordItem.sacada(bengala)) helper.fail("sacada, está sacada");
        if (soma(bengala) != CaneSwordItem.SACADA) {
            helper.fail("e soma seis, e soma " + soma(bengala));
        }

        CaneSwordItem.saca(bengala, false);
        if (soma(bengala) != CaneSwordItem.GUARDADA) helper.fail("e guardada volta a somar um");
        helper.succeed();
    }

    /** O que os modificadores da pilha somam ao golpe de quem a tem na mão. */
    private static double soma(ItemStack bengala) {
        var quais = bengala.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (quais == null) return 0.0;
        for (var cada : quais.modifiers()) {
            if (cada.attribute() == Attributes.ATTACK_DAMAGE) return cada.modifier().amount();
        }
        return 0.0;
    }

    /**
     * <b>O cantil enche-se com quem morre.</b>
     *
     * <p>Aldeão e pessoa dão o sangue que ainda tinham; um porco não dá nada. E o cantil tem teto: duzentos
     * e cinquenta, e nem mais um.
     */
    @GameTest
    public void theReserveFillsFromTheDead(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        Vampire.grau(quem, 1);
        if (BloodReserve.guardado(quem) != 0) helper.fail("o cantil começa vazio");

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
        Blood.põe(aldeão, 100);
        BloodReserve.levou(quem, aldeão);
        if (BloodReserve.guardado(quem) != 100) {
            helper.fail("o aldeão dá o sangue que tinha, e deu " + BloodReserve.guardado(quem));
        }

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 4));
        BloodReserve.levou(quem, porco);
        if (BloodReserve.guardado(quem) != 100) helper.fail("e um porco não dá nada");

        // e o teto é duzentos e cinquenta
        BloodReserve.enche(quem, 1000);
        if (BloodReserve.guardado(quem) != BloodReserve.TETO) {
            helper.fail("o cantil cabe duzentos e cinquenta, e cabe "
                    + BloodReserve.guardado(quem));
        }
        helper.succeed();
    }

    /**
     * <b>Só um vampiro o vê, e só a bengala guardada o abre.</b>
     *
     * <p>O cantil enche-se em qualquer um — mas quem não é vampiro pergunta e ouve zero, que é a crueldade
     * pequena do original.
     */
    @GameTest
    public void onlyAVampireDrinksTheReserve(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        BloodReserve.enche(quem, 200);
        if (BloodReserve.de(quem) != 0) helper.fail("quem não é vampiro não vê o cantil");
        if (BloodReserve.bebe(quem)) helper.fail("e não o bebe");

        Vampire.grau(quem, 1);
        Vampire.sangue(quem, 0);
        if (BloodReserve.de(quem) != 200) helper.fail("e o vampiro vê os duzentos que lá estavam");
        if (!BloodReserve.bebe(quem)) helper.fail("e bebe-os");
        if (BloodReserve.guardado(quem) != 0) helper.fail("e o cantil fica vazio");
        if (Vampire.sangueDe(quem) != 200) {
            helper.fail("e o sangue dele sobe para duzentos, e está em " + Vampire.sangueDe(quem));
        }

        // cheio, não se bebe: um gole nunca se perde
        BloodReserve.enche(quem, 100);
        Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
        if (BloodReserve.bebe(quem)) helper.fail("cheio de sangue, ele não abre o cantil");
        if (BloodReserve.guardado(quem) != 100) helper.fail("e o que lá estava fica lá");
        helper.succeed();
    }

    /**
     * <b>A vara desce um bloco por batida</b>, e para ao achar o que procura.
     *
     * <p>A prova põe água <b>sete</b> blocos abaixo de um chão de pedra, põe quem a segura <b>em cima
     * dele, olhando para baixo</b>, e conta as batidas até a vara parar. Sete, e um uso gasto.
     */
    @GameTest
    public void theRodGoesDownOneBlockPerTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;

        BlockPos chão = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());
        for (int fundo = 1; fundo <= 10; fundo++) {
            level.setBlockAndUpdate(chão.below(fundo), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(chão.below(ONDE), Blocks.WATER.defaultBlockState());

        // de pé em cima dele, olhando a direito para baixo
        quem.snapTo(chão.getX() + 0.5, chão.getY() + 3.0, chão.getZ() + 0.5, 0.0f, 90.0f);

        ItemStack vara = new ItemStack(OccultaItems.DIVINER_WATER);
        quem.setItemInHand(InteractionHand.MAIN_HAND, vara);
        quem.startUsingItem(InteractionHand.MAIN_HAND);

        int parou = -1;
        for (int batida = 1; batida <= ONDE + 4; batida++) {
            vara.getItem().onUseTick(level, quem, vara, DivinerItem.SEGURAR - batida);
            if (vara.getDamageValue() > 0) {
                parou = batida;
                break;
            }
        }
        if (parou != ONDE) {
            helper.fail("a vara acha a água na sétima batida, e achou na " + parou);
            return;
        }
        if (quem.isUsingItem()) helper.fail("e para de ser segurada");

        // e as duas procuram coisas diferentes
        if (((DivinerItem) OccultaItems.DIVINER_LAVA).oQueProcura() != Blocks.LAVA) {
            helper.fail("a vara de lava procura lava");
        }
        if (((DivinerItem) OccultaItems.DIVINER_WATER).oQueProcura() != Blocks.WATER) {
            helper.fail("e a d'água, água");
        }
        helper.succeed();
    }

    /** A que fundura a prova põe a água. */
    private static final int ONDE = 7;

    /**
     * <b>Apontando para o ar, ela desiste.</b>
     *
     * <p>Uma vara que não vê chão não sabe por onde descer, e o original a larga na hora — sem gastar uso
     * nenhum, que é o que a torna justa.
     */
    @GameTest
    public void theRodGivesUpWhenItSeesNoGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;

        BlockPos onde = helper.absolutePos(new BlockPos(2, 5, 2));
        quem.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 0.0f, -90.0f);

        ItemStack vara = new ItemStack(OccultaItems.DIVINER_WATER);
        quem.setItemInHand(InteractionHand.MAIN_HAND, vara);
        quem.startUsingItem(InteractionHand.MAIN_HAND);
        vara.getItem().onUseTick(level, quem, vara, DivinerItem.SEGURAR - 1);

        if (vara.getDamageValue() != 0) helper.fail("olhando para o céu ela não gasta nada");
        if (quem.isUsingItem()) helper.fail("e larga-se logo");
        helper.succeed();
    }

    /** E as três receitas, que são as do original. */
    @GameTest
    public void theThreeRecipesAreTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        ItemStack pano = new ItemStack(OccultaItems.DARK_CLOTH);
        var bancada = CraftingInput.of(3, 3, List.of(
                ItemStack.EMPTY, pano.copy(), new ItemStack(Items.GOLD_INGOT),
                pano.copy(), new ItemStack(Items.DIAMOND_SWORD), pano.copy(),
                pano.copy(), pano.copy(), ItemStack.EMPTY));
        if (!dá(helper, level, bancada, OccultaItems.CANE_SWORD)) return;

        ItemStack frasco = new ItemStack(Items.POTION);
        ItemStack pau = new ItemStack(Items.STICK);
        var água = CraftingInput.of(3, 3, List.of(
                frasco.copy(), pau.copy(), frasco.copy(),
                frasco.copy(), pau.copy(), frasco.copy(),
                pau.copy(), new ItemStack(OccultaItems.TEAR_OF_THE_GODDESS), pau.copy()));
        if (!dá(helper, level, água, OccultaItems.DIVINER_WATER)) return;

        ItemStack vara = new ItemStack(Items.BLAZE_ROD);
        var lava = CraftingInput.of(3, 3, List.of(
                ItemStack.EMPTY, vara.copy(), ItemStack.EMPTY,
                ItemStack.EMPTY, new ItemStack(OccultaItems.DIVINER_WATER), ItemStack.EMPTY,
                vara.copy(), ItemStack.EMPTY, vara.copy()));
        if (!dá(helper, level, lava, OccultaItems.DIVINER_LAVA)) return;
        helper.succeed();
    }

    private static boolean dá(GameTestHelper helper, ServerLevel level, CraftingInput bancada,
                              net.minecraft.world.item.Item oquê) {
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, bancada, level);
        if (achada.isEmpty()) {
            helper.fail("não há receita para " + oquê);
            return false;
        }
        ItemStack feito = achada.get().value().assemble(bancada);
        if (!feito.is(oquê)) {
            helper.fail("a receita havia de dar " + oquê + " e deu " + feito);
            return false;
        }
        return true;
    }
}
