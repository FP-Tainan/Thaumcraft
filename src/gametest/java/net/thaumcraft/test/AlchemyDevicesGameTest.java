package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.EssentiaCrystalizerBlock;
import net.thaumcraft.block.entity.CentrifugeBlockEntity;
import net.thaumcraft.block.entity.EssentiaCrystalizerBlockEntity;
import net.thaumcraft.item.CrystalEssenceItem;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;

/** A centrífuga e o cristalizador têm de seguir o {@code TileCentrifuge} e o {@code TileEssentiaCrystalizer}. */
public class AlchemyDevicesGameTest {
    @GameTest(maxTicks = 80)
    public void theCentrifugeSplitsACompoundIntoOneOfItsParts(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, TCBlocks.CENTRIFUGE.defaultBlockState());
        CentrifugeBlockEntity centrifuge = helper.getBlockEntity(pos, CentrifugeBlockEntity.class);
        if (centrifuge.addEssentia(Aspects.FIRE, 1, Direction.DOWN) != 0) helper.fail("primário não entra na centrífuga");
        if (centrifuge.addEssentia(Aspects.METAL, 1, Direction.DOWN) != 1) helper.fail("o composto entra por baixo");
        helper.succeedWhen(() -> {
            Aspect out = centrifuge.aspectOut();
            if (out == null) helper.fail("ainda girando");
            if (!List.of(Aspects.METAL.components()).contains(out)) helper.fail("saiu " + out.tag() + ", que não compõe o metal");
            if (centrifuge.getSuctionAmount(Direction.UP) != 0) helper.fail("por cima ela não puxa nada");
        });
    }

    @GameTest(maxTicks = 1100)
    public void theCrystalizerDropsACrystalIntoTheChestBehind(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), Blocks.CHEST.defaultBlockState());
        helper.setBlock(pos, TCBlocks.ESSENTIA_CRYSTALIZER.defaultBlockState().setValue(EssentiaCrystalizerBlock.FACING, Direction.UP));
        EssentiaCrystalizerBlockEntity crystalizer = helper.getBlockEntity(pos, EssentiaCrystalizerBlockEntity.class);
        if (crystalizer.addEssentia(Aspects.FIRE, 1, Direction.UP) != 1) helper.fail("a essência entra pela boca");
        if (crystalizer.addEssentia(Aspects.FIRE, 1, Direction.DOWN) != 0) helper.fail("por trás não entra nada");
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(pos.below(), ChestBlockEntity.class);
            if (chest.getItem(0).isEmpty()) helper.fail("ainda cristalizando");
            if (CrystalEssenceItem.aspectOf(chest.getItem(0)) != Aspects.FIRE) helper.fail("o cristal tem de ser de fogo");
        });
    }
}
