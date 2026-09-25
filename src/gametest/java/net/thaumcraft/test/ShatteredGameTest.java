package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.thaumcraft.shattered.FabricBlocks;
import net.thaumcraft.shattered.ShatteredItems;
import net.thaumcraft.shattered.ShatteredRealms;
import net.thaumcraft.world.DynamicDimensions;

/** Os Reinos Fragmentados: os tecidos e o que eles fazem. */
public class ShatteredGameTest {
    /** São dezesseis cores de tecido comum, dezesseis de antigo, mais o eterno e o desfiado. */
    @GameTest
    public void theFabricsAreAllThere(GameTestHelper helper) {
        if (FabricBlocks.FABRIC.size() != 16) helper.fail("o tecido comum tem dezesseis cores");
        if (FabricBlocks.ANCIENT.size() != 16) helper.fail("e o antigo também");
        if (FabricBlocks.count() != 34) helper.fail("com o eterno e o desfiado, são trinta e quatro");
        if (ShatteredItems.count() != FabricBlocks.count()) helper.fail("cada bloco tem o seu item");
        if (!FabricBlocks.isFabric(FabricBlocks.FABRIC.get(DyeColor.BLACK))) helper.fail("o preto é tecido");
        if (FabricBlocks.isFabric(FabricBlocks.ETERNAL)) helper.fail("o eterno não é tecido de bolso");
        helper.succeed();
    }

    /** Fora de um bolso, o tecido não troca de bloco. */
    @GameTest
    public void theFabricOnlyGivesWayInsideAPocket(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, FabricBlocks.FABRIC.get(DyeColor.BLACK));
        var quemTenta = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack pedra = new ItemStack(Items.STONE);
        FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState().useItemOn(pedra, helper.getLevel(), quemTenta,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(onde)),
                        net.minecraft.core.Direction.UP, helper.absolutePos(onde), false));
        helper.assertBlockPresent(FabricBlocks.FABRIC.get(DyeColor.BLACK), onde);
        if (pedra.getCount() != 1) helper.fail("e nem gasta o bloco de quem tentou");
        helper.succeed();
    }

    /** E dentro dele, troca. */
    @GameTest
    public void theFabricGivesWayInsideAPocket(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var chave = ShatteredRealms.PUBLIC_POCKETS;
        var gerador = new net.minecraft.world.level.levelgen.FlatLevelSource(
                net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings.getDefault(
                        server.registryAccess().lookupOrThrow(Registries.BIOME),
                        server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                        server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)));
        var bolso = DynamicDimensions.getOrCreate(server, chave, BuiltinDimensionTypes.OVERWORLD, gerador);
        if (bolso == null) {
            helper.fail("o bolso devia abrir");
            return;
        }
        if (!ShatteredRealms.isPocket(bolso)) helper.fail("e saber que é bolso");

        BlockPos onde = new BlockPos(0, 70, 0);
        bolso.setBlockAndUpdate(onde, FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState());
        var quemTroca = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack pedra = new ItemStack(Items.STONE, 2);
        FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState().useItemOn(pedra, bolso, quemTroca,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde),
                        net.minecraft.core.Direction.UP, onde, false));
        if (!bolso.getBlockState(onde).is(Blocks.STONE)) {
            helper.fail("dentro do bolso o tecido cede o lugar; ficou " + bolso.getBlockState(onde));
        }
        if (pedra.getCount() != 1) helper.fail("e gasta o bloco de quem o pôs");

        DynamicDimensions.remove(server, chave);
        helper.succeed();
    }

    /** O tecido antigo não se quebra. */
    @GameTest
    public void theAncientFabricDoesNotBreak(GameTestHelper helper) {
        var antigo = FabricBlocks.ANCIENT.get(DyeColor.BLACK).defaultBlockState();
        if (antigo.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) >= 0.0f) {
            helper.fail("o tecido antigo não se quebra a pico");
        }
        var comum = FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState();
        if (comum.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) < 0.0f) {
            helper.fail("mas o comum sim");
        }
        helper.succeed();
    }
}
