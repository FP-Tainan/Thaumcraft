package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.entity.TubeFilterBlockEntity;
import net.thaumcraft.crafting.LabelMarkingRecipe;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.List;

/** O rótulo marcado e o tubo filtro têm de seguir o {@code ConfigRecipes} e o {@code TileTubeFilter}. */
public class TubeFilterGameTest {
    @GameTest
    public void aPhialMarksTheLabel(GameTestHelper helper) {
        ItemStack label = new ItemStack(TCResources.get("jar_label"));
        ItemStack phial = new ItemStack(TCItems.PHIAL);
        phial.set(TCComponents.PHIAL_ASPECT, Aspects.FIRE.tag());
        CraftingInput input = CraftingInput.of(2, 1, List.of(label, phial));
        if (!LabelMarkingRecipe.INSTANCE.matches(input, helper.getLevel())) helper.fail("rótulo com frasco cheio marca");
        ItemStack marked = LabelMarkingRecipe.INSTANCE.assemble(input);
        if (!Aspects.FIRE.tag().equals(marked.get(TCComponents.LABEL_ASPECT))) helper.fail("o rótulo leva o aspecto do frasco");
        if (!LabelMarkingRecipe.INSTANCE.getRemainingItems(input).get(1).is(TCItems.PHIAL)) helper.fail("o frasco volta vazio");
        CraftingInput alone = CraftingInput.of(1, 1, List.of(marked));
        if (!LabelMarkingRecipe.INSTANCE.matches(alone, helper.getLevel())) helper.fail("o rótulo marcado sozinho se apaga");
        if (LabelMarkingRecipe.INSTANCE.assemble(alone).has(TCComponents.LABEL_ASPECT)) helper.fail("e sai em branco");
        if (LabelMarkingRecipe.INSTANCE.matches(CraftingInput.of(1, 1, List.of(label)), helper.getLevel())) {
            helper.fail("rótulo em branco sozinho não é receita");
        }
        helper.succeed();
    }

    @GameTest
    public void theFilterPullsOnlyItsAspect(GameTestHelper helper) {
        BlockPos jarPos = new BlockPos(1, 2, 1);
        BlockPos tubePos = new BlockPos(1, 3, 1);
        helper.setBlock(jarPos, TCBlocks.JAR.defaultBlockState());
        helper.setBlock(tubePos, TCBlocks.TUBE_FILTER.defaultBlockState());
        var tube = helper.getBlockEntity(tubePos, TubeFilterBlockEntity.class);
        tube.setAspectFilter(Aspects.WATER);
        helper.runAfterDelay(10, () -> {
            // o jarro vazio puxa qualquer coisa; o filtro só puxa aqua
            if (tube.getSuctionType(null) != Aspects.WATER) helper.fail("o filtro devia puxar aqua, puxa " + tube.getSuctionType(null));
            if (tube.getSuctionAmount(null) <= 0) helper.fail("e com força");
            helper.succeed();
        });
    }
}
