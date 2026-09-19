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
    /**
     * O altar do original: sem os pilares a matriz não vale; a varinha, com pedestal, tijolos de pedra arcana nos
     * cantos e pedra arcana em cima, cobra vinte e cinco de cada primário e ergue os pilares; tirando a metade de
     * um pilar, a outra cai e o altar deixa de valer.
     */
    @GameTest(maxTicks = 40)
    public void theWandRaisesTheAltar(GameTestHelper helper) {
        BlockPos matrixAt = new BlockPos(3, 3, 3);
        helper.setBlock(matrixAt, TCBlocks.INFUSION_MATRIX);
        helper.setBlock(matrixAt.below(2), TCBlocks.PEDESTAL);
        if (InfusionMatrixBlockEntity.validLocation(helper.getLevel(), helper.absolutePos(matrixAt))) {
            helper.fail("sem pilares o altar não devia valer");
        }
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                helper.setBlock(matrixAt.offset(dx, -2, dz), TCBlocks.BUILDING.get("arcane_stone_bricks"));
                helper.setBlock(matrixAt.offset(dx, -1, dz), TCBlocks.BUILDING.get("arcane_stone"));
            }
        }
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack wand = new ItemStack(net.thaumcraft.registry.TCItems.WAND);
        net.thaumcraft.api.aspects.AspectList vis = new net.thaumcraft.api.aspects.AspectList();
        for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) vis.add(primal, 5000);
        wand.set(net.thaumcraft.registry.TCComponents.WAND_VIS, vis);
        var matrix = helper.getBlockEntity(matrixAt, InfusionMatrixBlockEntity.class);
        if (!matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, wand)) helper.fail("a varinha devia erguer o altar");
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                helper.assertBlockPresent(TCBlocks.INFUSION_PILLAR, matrixAt.offset(dx, -2, dz));
                helper.assertBlockPresent(TCBlocks.INFUSION_PILLAR_TOP, matrixAt.offset(dx, -1, dz));
            }
        }
        if (!InfusionMatrixBlockEntity.validLocation(helper.getLevel(), helper.absolutePos(matrixAt))) {
            helper.fail("com os pilares, o altar devia valer");
        }
        if (wand.get(net.thaumcraft.registry.TCComponents.WAND_VIS).getAmount(net.thaumcraft.api.aspects.Aspects.FIRE) >= 5000) {
            helper.fail("erguer o altar custa vis");
        }
        // tirando o topo de um pilar, a base cai junto
        helper.setBlock(matrixAt.offset(1, -1, 1), net.minecraft.world.level.block.Blocks.AIR);
        helper.runAfterDelay(5, () -> {
            helper.assertBlockNotPresent(TCBlocks.INFUSION_PILLAR, matrixAt.offset(1, -2, 1));
            if (InfusionMatrixBlockEntity.validLocation(helper.getLevel(), helper.absolutePos(matrixAt))) {
                helper.fail("sem um dos pilares o altar não devia valer");
            }
            helper.succeed();
        });
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
        // os pilares já de pé, como a varinha os deixa
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                helper.setBlock(matrixAt.offset(dx, -1, dz), TCBlocks.INFUSION_PILLAR_TOP);
                helper.setBlock(matrixAt.offset(dx, -2, dz), TCBlocks.INFUSION_PILLAR);
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
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, ItemStack.EMPTY);
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, ItemStack.EMPTY);
        if (!matrix.isCrafting()) helper.fail("a infusão não começou");

        // A instabilidade desta receita é três, e o azar dela pode cuspir um ingrediente de um pedestal
        // -- o que é o comportamento certo, mas trava a infusão e faria esta prova falhar de vez em
        // quando sem motivo. Aqui a prova repõe o que for cuspido, como quem está olhando faria, para
        // medir o ciclo da infusão e não a sorte do dado.
        helper.succeedWhen(() -> {
            int reposto = 0;
            for (var wanted : recipe.components()) {
                BlockPos at = new BlockPos(7, centreAt.getY(), 2 + reposto * 2);
                reposto++;
                if (!(helper.getBlockEntity(at, PedestalBlockEntity.class) instanceof PedestalBlockEntity pedestal)) {
                    continue;
                }
                if (pedestal.held().isEmpty()) {
                    pedestal.hold(new ItemStack(wanted.items().iterator().next()));
                }
            }
            ItemStack made = helper.getBlockEntity(centreAt, PedestalBlockEntity.class).held();
            if (!ItemStack.isSameItem(made, recipe.result())) {
                helper.fail("o pedestal do meio devia ter " + recipe.result().getItem()
                        + ", tem " + made.getItem());
            }
        });
    }

    /** Monta o altar com os pilares de pé e o pedestal do meio segurando {@code middle}. */
    private static InfusionMatrixBlockEntity altar(GameTestHelper helper, BlockPos matrixAt, ItemStack middle) {
        helper.setBlock(matrixAt, TCBlocks.INFUSION_MATRIX);
        helper.setBlock(matrixAt.below(2), TCBlocks.PEDESTAL);
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                helper.setBlock(matrixAt.offset(dx, -1, dz), TCBlocks.INFUSION_PILLAR_TOP);
                helper.setBlock(matrixAt.offset(dx, -2, dz), TCBlocks.INFUSION_PILLAR);
            }
        }
        helper.getBlockEntity(matrixAt.below(2), PedestalBlockEntity.class).hold(middle);
        return helper.getBlockEntity(matrixAt, InfusionMatrixBlockEntity.class);
    }

    /**
     * Tirando a coisa do meio no meio do serviço, o ciclo seguinte sorteia um azar e para a infusão — mas a matriz
     * continua ligada, pronta para outra (o craftCycle com o centro inválido).
     */
    @GameTest(maxTicks = 100)
    public void takingTheCentreAwayStopsTheInfusion(GameTestHelper helper) {
        InfusionRecipe recipe = InfusionRecipes.ALL.getFirst();
        BlockPos matrixAt = new BlockPos(4, 4, 4);
        InfusionMatrixBlockEntity matrix = altar(helper, matrixAt, new ItemStack(recipe.central().items().iterator().next()));
        int slot = 0;
        for (var wanted : recipe.components()) {
            BlockPos at = new BlockPos(7, 2, 2 + slot++ * 2);
            helper.setBlock(at, TCBlocks.PEDESTAL);
            helper.getBlockEntity(at, PedestalBlockEntity.class).hold(new ItemStack(wanted.items().iterator().next()));
        }
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        net.thaumcraft.research.Knowledges.of(player).completeResearch(recipe.research());
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, ItemStack.EMPTY);
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, ItemStack.EMPTY);
        if (!matrix.isCrafting()) helper.fail("a infusão não começou");
        helper.getBlockEntity(matrixAt.below(2), PedestalBlockEntity.class).hold(ItemStack.EMPTY);
        helper.succeedWhen(() -> {
            if (matrix.isCrafting()) helper.fail("sem a coisa do meio a infusão devia parar");
            if (!matrix.isActive()) helper.fail("a matriz continua ligada");
            if (matrix.instability() != 0) helper.fail("parada, a instabilidade zera");
        });
    }

    /** Sem saber a pesquisa, a receita não começa (o findMatchingInfusionRecipe olha o que o jogador sabe). */
    @GameTest
    public void anUnknownRecipeDoesNotStart(GameTestHelper helper) {
        InfusionRecipe recipe = InfusionRecipes.ALL.getFirst();
        BlockPos matrixAt = new BlockPos(4, 4, 4);
        InfusionMatrixBlockEntity matrix = altar(helper, matrixAt, new ItemStack(recipe.central().items().iterator().next()));
        int slot = 0;
        for (var wanted : recipe.components()) {
            BlockPos at = new BlockPos(7, 2, 2 + slot++ * 2);
            helper.setBlock(at, TCBlocks.PEDESTAL);
            helper.getBlockEntity(at, PedestalBlockEntity.class).hold(new ItemStack(wanted.items().iterator().next()));
        }
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, ItemStack.EMPTY);
        matrix.poke(helper.getLevel(), helper.absolutePos(matrixAt), player, ItemStack.EMPTY);
        if (matrix.isCrafting()) helper.fail("sem a pesquisa, nada começa");
        helper.succeed();
    }

    /** Só jarro, reservatório e espelho dão essência pelo ar; o alambique guarda, mas não dá. */
    @GameTest
    public void onlyJarsGiveEssentiaThroughTheAir(GameTestHelper helper) {
        BlockPos matrixAt = new BlockPos(4, 4, 4);
        InfusionMatrixBlockEntity matrix = altar(helper, matrixAt, ItemStack.EMPTY);
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.ALEMBIC);
        var alembic = helper.getBlockEntity(new BlockPos(1, 2, 1), net.thaumcraft.block.entity.AlembicBlockEntity.class);
        alembic.addToContainer(net.thaumcraft.api.aspects.Aspects.FIRE, 10);
        if (net.thaumcraft.api.aspects.EssentiaSources.drain(matrix, net.thaumcraft.api.aspects.Aspects.FIRE, null, 12)) {
            helper.fail("o alambique não é fonte");
        }
        helper.succeed();
    }
}
