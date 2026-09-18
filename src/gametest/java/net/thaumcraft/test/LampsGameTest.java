package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ArcaneLampBlock;
import net.thaumcraft.block.entity.JarBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/** As lâmpadas têm de seguir o {@code TileArcaneLamp}, o {@code TileArcaneLampGrowth} e o {@code TileArcaneLampFertility}. */
public class LampsGameTest {
    @GameTest(maxTicks = 200)
    public void theArcaneLampSpreadsLightInTheDark(GameTestHelper helper) {
        // uma caixa fechada, escura por dentro
        for (int x = 0; x < 5; x++) {
            for (int y = 1; y < 6; y++) {
                for (int z = 0; z < 5; z++) {
                    boolean shell = x == 0 || x == 4 || y == 1 || y == 5 || z == 0 || z == 4;
                    helper.setBlock(new BlockPos(x, y, z), shell ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        helper.setBlock(new BlockPos(2, 2, 2), TCBlocks.ARCANE_LAMP.defaultBlockState().setValue(ArcaneLampBlock.FACING, Direction.DOWN));
        helper.succeedWhen(() -> {
            // a luz da própria lâmpada já passa de nove perto dela; o teste só confere que a lâmpada brilha quinze
            if (helper.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, helper.absolutePos(new BlockPos(2, 3, 2))) < 14) {
                helper.fail("a lâmpada arcana brilha quinze");
            }
        });
    }

    @GameTest
    public void breakingTheArcaneLampPutsOutItsLights(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.ARCANE_LAMP.defaultBlockState().setValue(ArcaneLampBlock.FACING, Direction.DOWN));
        helper.setBlock(new BlockPos(3, 2, 3), TCBlocks.LAMP_LIGHT.defaultBlockState());
        helper.destroyBlock(new BlockPos(1, 2, 1));
        if (helper.getBlockState(new BlockPos(3, 2, 3)).is(TCBlocks.LAMP_LIGHT)) helper.fail("a luz some com a lâmpada");
        helper.succeed();
    }

    @GameTest
    public void theLampFallsWithoutItsSupport(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.ARCANE_LAMP.defaultBlockState().setValue(ArcaneLampBlock.FACING, Direction.DOWN));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR.defaultBlockState());
        helper.succeedWhen(() -> {
            if (helper.getBlockState(new BlockPos(1, 2, 1)).is(TCBlocks.ARCANE_LAMP)) helper.fail("a lâmpada tem de cair");
        });
    }

    @GameTest(maxTicks = 40)
    public void theGrowthLampDrinksHerbaAndLightsUp(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.JAR.defaultBlockState());
        JarBlockEntity jar = helper.getBlockEntity(new BlockPos(1, 1, 1), JarBlockEntity.class);
        jar.addToContainer(Aspects.PLANT, 10);
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.GROWTH_LAMP.defaultBlockState().setValue(ArcaneLampBlock.FACING, Direction.DOWN));
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(new BlockPos(1, 2, 1)).getValue(ArcaneLampBlock.LIT)) helper.fail("com Herba ela acende");
            // um ponto para as cem cargas e outro de reserva
            if (jar.amount() != 8) helper.fail("bebe dois pontos: um em uso e um de reserva; o jarro ficou com " + jar.amount());
        });
    }

    @GameTest(maxTicks = 400)
    public void theFertilityLampPutsAPairInLove(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TCBlocks.JAR.defaultBlockState());
        JarBlockEntity jar = helper.getBlockEntity(new BlockPos(1, 1, 1), JarBlockEntity.class);
        jar.addToContainer(Aspects.LIFE, 10);
        helper.setBlock(new BlockPos(1, 2, 1), TCBlocks.FERTILITY_LAMP.defaultBlockState().setValue(ArcaneLampBlock.FACING, Direction.DOWN));
        Animal first = helper.spawn(EntityTypes.COW, new BlockPos(3, 1, 3));
        Animal second = helper.spawn(EntityTypes.COW, new BlockPos(3, 1, 1));
        first.setNoAi(true);
        second.setNoAi(true);
        helper.succeedWhen(() -> {
            if (!first.isInLove() || !second.isInLove()) helper.fail("as duas vacas entram no cio");
        });
    }

    @GameTest(maxTicks = 60)
    public void theHungryChestEatsWhatFallsOnIt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, TCBlocks.HUNGRY_CHEST.defaultBlockState());
        helper.spawnItem(net.minecraft.world.item.Items.DIAMOND, 1.5f, 2.5f, 1.5f);
        helper.succeedWhen(() -> {
            var chest = helper.getBlockEntity(pos, net.thaumcraft.block.entity.HungryChestBlockEntity.class);
            if (!chest.getItem(0).is(net.minecraft.world.item.Items.DIAMOND)) helper.fail("o baú tem de engolir o diamante");
        });
    }
}
