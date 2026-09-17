package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.InfusionMatrixBlockEntity;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.block.entity.PedestalBlockEntity;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.registry.TCBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * A infusão tem de se comportar como no Thaumcraft 4.2.3.5.
 *
 * <p>O que estas provas guardam: a conferência da construção, o casamento da receita (que não liga para a
 * ordem dos pedestais) e o caminho inteiro, do toque da varinha à coisa nova no pedestal do meio.
 */
public class InfusionGameTest {
    /** Sem a construção certa, a matriz não liga. */
    @GameTest
    public void theMatrixNeedsItsAltar(GameTestHelper helper) {
        BlockPos matrixAt = new BlockPos(3, 3, 3);
        helper.setBlock(matrixAt, TCBlocks.INFUSION_MATRIX);
        if (InfusionMatrixBlockEntity.validLocation(helper.getLevel(), helper.absolutePos(matrixAt))) {
            helper.fail("matriz sozinha no ar não devia valer");
        }

        // o pedestal no lugar, mas ainda sem os cantos de pedra arcana
        helper.setBlock(matrixAt.below(2), TCBlocks.PEDESTAL);
        if (InfusionMatrixBlockEntity.validLocation(helper.getLevel(), helper.absolutePos(matrixAt))) {
            helper.fail("faltam os quatro cantos de pedra arcana");
        }

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                helper.setBlock(matrixAt.offset(dx, -2, dz), TCBlocks.BUILDING.get("arcane_stone"));
            }
        }
        if (!InfusionMatrixBlockEntity.validLocation(helper.getLevel(), helper.absolutePos(matrixAt))) {
            helper.fail("com pedestal e os quatro cantos, o altar devia valer");
        }
        helper.succeed();
    }

    /** A receita casa com o conjunto, não com a ordem — os pedestais podem estar em qualquer lugar. */
    @GameTest
    public void theOrderOfThePedestalsDoesNotMatter(GameTestHelper helper) {
        if (InfusionRecipes.ALL.isEmpty()) helper.fail("nenhuma receita de infusão");

        InfusionRecipe recipe = InfusionRecipes.ALL.getFirst();
        List<ItemStack> parts = new ArrayList<>();
        for (var wanted : recipe.components()) parts.add(new ItemStack(wanted.items().iterator().next()));
        ItemStack middle = new ItemStack(recipe.central().items().iterator().next());

        if (!recipe.matches(middle, parts)) helper.fail("a receita não casou com os próprios ingredientes");
        java.util.Collections.reverse(parts);
        if (!recipe.matches(middle, parts)) helper.fail("a ordem dos pedestais não pode importar");

        // sobrando um pedestal a mais, não casa
        parts.add(new ItemStack(net.minecraft.world.item.Items.DIRT));
        if (recipe.matches(middle, parts)) helper.fail("pedestal a mais devia estragar a receita");
        helper.succeed();
    }

    /**
     * O caminho inteiro da infusão.
     *
     * <p>Monta o altar, põe a receita nos pedestais, abastece um jarro com a essência que ela pede e
     * deixa o jogo andar. A coisa nova tem de aparecer no pedestal do meio.
     */
    @GameTest(maxTicks = 900)
    public void infusionRunsFromStartToFinish(GameTestHelper helper) {
        InfusionRecipe recipe = InfusionRecipes.ALL.getFirst();

        BlockPos matrixAt = new BlockPos(4, 4, 4);
        BlockPos centreAt = matrixAt.below(2);
        helper.setBlock(matrixAt, TCBlocks.INFUSION_MATRIX);
        helper.setBlock(centreAt, TCBlocks.PEDESTAL);
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                helper.setBlock(matrixAt.offset(dx, -2, dz), TCBlocks.BUILDING.get("arcane_stone"));
            }
        }

        // a coisa do meio
        PedestalBlockEntity centre = helper.getBlockEntity(centreAt, PedestalBlockEntity.class);
        centre.hold(new ItemStack(recipe.central().items().iterator().next()));

        // e os ingredientes, cada um no seu pedestal, em volta — tudo dentro do pedaço de mundo do teste
        int slot = 0;
        for (var wanted : recipe.components()) {
            BlockPos at = new BlockPos(7, centreAt.getY(), 2 + slot * 2);
            helper.setBlock(at, TCBlocks.PEDESTAL);
            helper.getBlockEntity(at, PedestalBlockEntity.class)
                    .hold(new ItemStack(wanted.items().iterator().next()));
            slot++;
        }

        // um jarro por aspecto que a receita pede, cheio
        int jar = 0;
        for (Aspect aspect : recipe.essentia().getAspects()) {
            BlockPos at = new BlockPos(1, centreAt.getY(), 1 + jar * 2);
            helper.setBlock(at, TCBlocks.JAR);
            JarBlockEntity held = helper.getBlockEntity(at, JarBlockEntity.class);
            held.addToContainer(aspect, JarBlockEntity.CAPACITY);
            if (held.amount() < JarBlockEntity.CAPACITY) {
                helper.fail("o jarro de " + aspect.tag() + " não encheu; ele caiu fora do mundo de teste?");
            }
            jar++;
        }

        // a varinha de quem manda: aqui o toque é dado direto, sem jogador
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        net.thaumcraft.research.Knowledges.of(player).completeResearch(recipe.research());
        InfusionMatrixBlockEntity matrix = helper.getBlockEntity(matrixAt, InfusionMatrixBlockEntity.class);
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player);
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player);
        if (!matrix.isCrafting()) helper.fail("a infusão não começou");

        helper.succeedWhen(() -> {
            ItemStack made = helper.getBlockEntity(centreAt, PedestalBlockEntity.class).held();
            if (!ItemStack.isSameItem(made, recipe.result())) {
                helper.fail("o pedestal do meio devia ter " + recipe.result().getItem()
                        + ", tem " + made.getItem());
            }
        });
    }
}
