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
    /** A porta dimensional traz uma fenda na metade de baixo, e só nela. */
    @GameTest
    public void theDoorCarriesARift(GameTestHelper helper) {
        BlockPos baixo = new BlockPos(2, 2, 2);
        var estado = net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR.defaultBlockState();
        helper.setBlock(baixo, estado);
        helper.setBlock(baixo.above(), estado.setValue(
                net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));

        if (!(helper.getBlockEntity(baixo, net.thaumcraft.shattered.RiftBlockEntity.class)
                instanceof net.thaumcraft.shattered.RiftBlockEntity)) {
            helper.fail("a metade de baixo devia ter a fenda");
        }
        if (helper.getLevel().getBlockEntity(helper.absolutePos(baixo.above())) != null) {
            helper.fail("e a de cima, não");
        }
        helper.succeed();
    }

    /** Atravessada, a porta abre um bolso e leva quem passa para lá — e a porta de volta traz de volta. */
    @GameTest
    public void theDoorOpensAPocketAndComesBack(GameTestHelper helper) {
        BlockPos baixo = new BlockPos(2, 2, 2);
        helper.setBlock(baixo, net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR);
        var fenda = helper.getBlockEntity(baixo, net.thaumcraft.shattered.RiftBlockEntity.class);
        if (fenda == null) {
            helper.fail("a porta devia ter fenda");
            return;
        }
        if (fenda.destination() != null) helper.fail("e nascer sem destino");

        var bicho = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(2, 2, 3));
        if (!fenda.teleport(bicho)) {
            helper.fail("a travessia devia dar certo");
            return;
        }
        var destino = fenda.destination();
        if (destino == null) helper.fail("e a fenda devia ficar a apontar para o bolso");
        else if (destino.level() != ShatteredRealms.PUBLIC_POCKETS) helper.fail("que fica no mundo dos bolsos");

        var bolsos = helper.getLevel().getServer().getLevel(ShatteredRealms.PUBLIC_POCKETS);
        if (bolsos == null) {
            helper.fail("o mundo dos bolsos devia estar aberto");
            return;
        }
        // um bicho que muda de mundo é copiado para lá, e não levado; o que se procura é a cópia
        var chegados = bolsos.getEntitiesOfClass(net.minecraft.world.entity.animal.pig.Pig.class,
                new net.minecraft.world.phys.AABB(destino.pos()).inflate(8.0));
        if (chegados.isEmpty()) helper.fail("e quem atravessou devia estar lá");
        for (var chegado : chegados) chegado.discard();

        // a sala: paredes de tecido antigo e ar no meio
        BlockPos dentro = destino.pos();
        if (!bolsos.getBlockState(dentro).isAir()) helper.fail("onde se chega é ar");
        var porta = bolsos.getBlockState(dentro.north());
        if (!porta.is(net.thaumcraft.shattered.ShatteredBlocks.OAK_DIMENSIONAL_DOOR)) {
            helper.fail("e atrás fica a porta de volta; achei " + porta);
        }

        // e a porta de volta aponta para onde se entrou
        if (bolsos.getBlockEntity(dentro.north()) instanceof net.thaumcraft.shattered.RiftBlockEntity volta) {
            var paraCasa = volta.destination();
            if (paraCasa == null) helper.fail("a porta de volta devia ter destino");
            else if (paraCasa.level() != helper.getLevel().dimension()) helper.fail("e ele é o mundo de onde se veio");
        } else {
            helper.fail("a porta de volta devia ter fenda");
        }

        bicho.discard();
        net.thaumcraft.world.DynamicDimensions.remove(helper.getLevel().getServer(), ShatteredRealms.PUBLIC_POCKETS);
        helper.succeed();
    }

    /** São dezesseis cores de tecido comum, dezesseis de antigo, mais o eterno e o desfiado. */
    @GameTest
    public void theFabricsAreAllThere(GameTestHelper helper) {
        if (FabricBlocks.FABRIC.size() != 16) helper.fail("o tecido comum tem dezesseis cores");
        if (FabricBlocks.ANCIENT.size() != 16) helper.fail("e o antigo também");
        if (FabricBlocks.count() != 34) helper.fail("com o eterno e o desfiado, são trinta e quatro");
        if (ShatteredItems.count() != FabricBlocks.count() + net.thaumcraft.shattered.ShatteredBlocks.doors().size()) {
            helper.fail("cada bloco tem o seu item; achei " + ShatteredItems.count());
        }
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
