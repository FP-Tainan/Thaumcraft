package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.NodeJarStructure;
import net.thaumcraft.block.entity.BrainJarBlockEntity;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.block.entity.NodeJarBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** O jarro de cérebro (come experiência, devolve ao toque) e o nó no jarro (a caixa de vidro, a varinha que solta). */
public class SpecialJarGameTest {
    @GameTest(maxTicks = 100)
    public void theBrainEatsExperience(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.BRAIN_JAR.defaultBlockState());
        BrainJarBlockEntity jar = helper.getBlockEntity(new BlockPos(1, 1, 1), BrainJarBlockEntity.class);
        Vec3 at = helper.absoluteVec(new Vec3(3.5, 1.5, 1.5));
        helper.getLevel().addFreshEntity(new ExperienceOrb(helper.getLevel(), at.x, at.y, at.z, 7));
        helper.succeedWhen(() -> {
            if (jar.xp != 7) helper.fail("o cérebro puxa e come a bolinha, tem " + jar.xp);
            if (jar.comparator() != 1) helper.fail("o comparador acende");
        });
    }

    @GameTest
    public void theNodeGoesIntoTheJar(GameTestHelper helper) {
        // a caixa: lajes em cima, vidro em volta, o nó no meio (o canto em (0,1,0), tampa em y 4)
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) {
            helper.setBlock(new BlockPos(x, 4, z), Blocks.OAK_SLAB.defaultBlockState());
            for (int y = 1; y < 4; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.GLASS.defaultBlockState());
        }
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.NODE.defaultBlockState());
        NodeBlockEntity node = helper.getBlockEntity(new BlockPos(1, 2, 1), NodeBlockEntity.class);
        node.setup(new AspectList().add(Aspects.AIR, 20).add(Aspects.FIRE, 10), NodeType.NORMAL, null);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 25000);
        wand.set(TCComponents.WAND_VIS, vis);
        wand.set(TCComponents.WAND_ROD, "silverwood");
        if (!NodeJarStructure.create(wand, player, helper.getLevel(), helper.absolutePos(new BlockPos(0, 3, 0)))) {
            throw helper.assertionException("a caixa certa vira o jarro");
        }
        helper.assertBlockPresent(TCBlocks.NODE_JAR, new BlockPos(1, 2, 1));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(0, 4, 0));
        NodeJarBlockEntity jar = helper.getBlockEntity(new BlockPos(1, 2, 1), NodeJarBlockEntity.class);
        if (jar.aspects().getAmount(Aspects.AIR) != 20) helper.fail("o nó entra inteiro");
        // a varinha quebra o vidro e o nó volta
        jar.onWand(helper.getLevel(), wand, player, helper.absolutePos(new BlockPos(1, 2, 1)), Direction.UP);
        helper.assertBlockPresent(TCBlocks.NODE, new BlockPos(1, 2, 1));
        NodeBlockEntity back = helper.getBlockEntity(new BlockPos(1, 2, 1), NodeBlockEntity.class);
        if (back.aspects().getAmount(Aspects.FIRE) != 10) helper.fail("e sai como estava");
        helper.succeed();
    }
}
