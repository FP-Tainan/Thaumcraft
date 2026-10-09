package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.CircleTalismanItem;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.RitualCircles;

import java.util.List;

/**
 * O <b>Talismã de Círculo</b>: a conta dos três anéis, a figura que ele mostra, o desenho que ele risca
 * e o que ele recusa.
 */
public class OccultaCircleTalismanGameTest {
    /** Os três anéis empacotam-se e desempacotam-se, como no dano do item do original. */
    @GameTest
    public void threeRingsInOneNumber(GameTestHelper helper) {
        int guardado = CircleTalismanItem.empacota(CircleTalismanItem.RITUAL,
                CircleTalismanItem.ALHURES, CircleTalismanItem.INFERNAL);
        if (CircleTalismanItem.anel(guardado, 0) != CircleTalismanItem.RITUAL) {
            helper.fail("o anel de dentro é o de Ritual");
        }
        if (CircleTalismanItem.anel(guardado, 1) != CircleTalismanItem.ALHURES) {
            helper.fail("o do meio é o do Alhures");
        }
        if (CircleTalismanItem.anel(guardado, 2) != CircleTalismanItem.INFERNAL) {
            helper.fail("e o de fora é o Infernal");
        }

        // e o número é o do original: c<<6 | b<<3 | a
        if (guardado != (3 << 6 | 2 << 3 | 1)) helper.fail("o empacotamento é o do original");
        if (CircleTalismanItem.empacota(0, 0, 0) != 0) helper.fail("e um talismã branco é zero");
        helper.succeed();
    }

    /**
     * <b>A figura é a do maior anel riscado</b>: havendo o de fora, é a dele; senão a do meio; senão a de
     * dentro.
     */
    @GameTest
    public void theIconIsTheOutermostRing(GameTestHelper helper) {
        if (CircleTalismanItem.figura(CircleTalismanItem.empacota(1, 0, 0)) != 1) {
            helper.fail("só o de dentro: a primeira figura");
        }
        if (CircleTalismanItem.figura(CircleTalismanItem.empacota(1, 2, 0)) != 5) {
            helper.fail("com o do meio, a quinta");
        }
        if (CircleTalismanItem.figura(CircleTalismanItem.empacota(1, 2, 3)) != 9) {
            helper.fail("e com o de fora, a nona");
        }
        if (CircleTalismanItem.figura(0) != 0) helper.fail("e um talismã branco não tem nenhuma");

        // e o talismã escrito leva a figura consigo
        ItemStack escrito = CircleTalismanItem.escrito(CircleTalismanItem.empacota(0, 0, 2));
        var figura = escrito.get(DataComponents.CUSTOM_MODEL_DATA);
        if (figura == null || figura.strings().isEmpty() || !figura.strings().getFirst().equals("8")) {
            helper.fail("um anel de fora do Alhures mostra a oitava figura");
        }
        if (CircleTalismanItem.guardado(escrito) == 0) helper.fail("e guarda o desenho");
        helper.succeed();
    }

    /**
     * <b>O carimbo</b>: ele risca o círculo inteiro de uma vez, e volta a ser branco.
     *
     * <p>A prova conta os glifos riscados e compara com o desenho do original — dezesseis no anel de
     * dentro, vinte e oito no do meio e quarenta no de fora.
     */
    @GameTest
    public void theStampDrawsTheWholeCircle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));

        // chão para o círculo assentar, e o desenho inteiro em volta
        int raio = (RitualCircles.side() - 1) / 2;
        for (int x = -raio; x <= raio; x++) {
            for (int z = -raio; z <= raio; z++) {
                level.setBlockAndUpdate(meio.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(x, 0, z), Blocks.AIR.defaultBlockState());
            }
        }

        int guardado = CircleTalismanItem.empacota(CircleTalismanItem.RITUAL,
                CircleTalismanItem.ALHURES, CircleTalismanItem.INFERNAL);
        level.setBlockAndUpdate(meio, OccultaBlocks.CIRCLE_HEART.defaultBlockState());

        ItemStack talismã = CircleTalismanItem.escrito(guardado);
        carimba(helper, level, meio, talismã);

        var lido = RitualCircles.read(level, meio);
        if (lido.inner().ritual() != RitualCircles.INNER) {
            helper.fail("o anel de dentro sai inteiro de giz de Ritual, e saiu com "
                    + lido.inner().ritual());
        }
        if (lido.middle().otherwhere() != RitualCircles.MIDDLE) {
            helper.fail("o do meio, do Alhures");
        }
        if (lido.outer().infernal() != RitualCircles.OUTER) helper.fail("e o de fora, Infernal");
        if (CircleTalismanItem.guardado(talismã) != 0) helper.fail("e o talismã fica branco");
        helper.succeed();
    }

    /** E a receita do talismã em branco, que é a do original. */
    @GameTest
    public void theRecipeIsTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var bancada = CraftingInput.of(3, 3, List.of(
                new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_NUGGET),
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_NUGGET)));
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, bancada, level);
        if (achada.isEmpty()) {
            helper.fail("não há receita para o talismã");
            return;
        }
        ItemStack feito = achada.get().value().assemble(bancada);
        if (!feito.is(OccultaItems.CIRCLE_TALISMAN)) helper.fail("a receita dá o talismã, e deu " + feito);
        helper.succeed();
    }

    /** Carimba com aquele talismã naquele ponto. */
    private static void carimba(GameTestHelper helper, ServerLevel level, BlockPos meio,
                                            ItemStack talismã) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, talismã);
        var onde = new net.minecraft.world.item.context.UseOnContext(level, quem,
                net.minecraft.world.InteractionHand.MAIN_HAND, talismã,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(meio),
                        net.minecraft.core.Direction.UP, meio, false));
        talismã.getItem().useOn(onde);
    }
}
