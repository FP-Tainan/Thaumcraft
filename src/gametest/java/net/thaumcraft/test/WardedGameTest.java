package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.block.ArcaneEarBlock;
import net.thaumcraft.block.ArcanePressurePlateBlock;
import net.thaumcraft.block.entity.ArcaneEarBlockEntity;
import net.thaumcraft.block.entity.OwnedBlockEntity;
import net.thaumcraft.item.KeyItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A porta arcana, as chaves, a placa de pressão arcana e o ouvido arcano da 4.2.3.5. */
public class WardedGameTest {
    private static void door(GameTestHelper helper, BlockPos lower, String owner) {
        helper.setBlock(lower.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(lower, TCBlocks.ARCANE_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(lower.above(), TCBlocks.ARCANE_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        for (BlockPos half : new BlockPos[]{lower, lower.above()}) {
            OwnedBlockEntity owned = helper.getBlockEntity(half, OwnedBlockEntity.class);
            owned.owner = owner;
        }
    }

    @GameTest
    public void onlyTheOwnerOrAKeyOpensTheDoor(GameTestHelper helper) {
        BlockPos lower = new BlockPos(1, 1, 1);
        var player = helper.makeMockServerPlayerInLevel();
        door(helper, lower, "alguem");
        // quem não é dono não abre
        helper.getBlockState(lower).useWithoutItem(helper.getLevel(), player, new net.minecraft.world.phys.BlockHitResult(
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1.5, 1.5)), Direction.NORTH, helper.absolutePos(lower), false));
        if (helper.getBlockState(lower).getValue(DoorBlock.OPEN)) helper.fail("a porta não abre para estranho");
        // a chave gravada, usada por ele, dá a entrada
        OwnedBlockEntity owned = helper.getBlockEntity(lower, OwnedBlockEntity.class);
        owned.owner = player.getName().getString();
        ItemStack blank = new ItemStack(TCItems.IRON_KEY, 2);
        ((KeyItem) TCItems.IRON_KEY).useOnWarded(blank, helper.getLevel(), helper.absolutePos(lower), player);
        ItemStack written = player.getInventory().getItem(0);
        if (written.getCount() != 1 || !written.is(TCItems.IRON_KEY) || !written.isEnchanted() && !written.hasFoil()) {
            helper.fail("o dono grava uma chave para a porta");
        }
        owned.owner = "alguem";
        ((KeyItem) TCItems.IRON_KEY).useOnWarded(written, helper.getLevel(), helper.absolutePos(lower.above()), player);
        if (!owned.accessList.contains("0" + player.getName().getString())) helper.fail("a chave de ferro põe o nome na lista da porta");
        helper.getBlockState(lower).useWithoutItem(helper.getLevel(), player, new net.minecraft.world.phys.BlockHitResult(
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1.5, 1.5)), Direction.NORTH, helper.absolutePos(lower), false));
        if (!helper.getBlockState(lower).getValue(DoorBlock.OPEN)) helper.fail("com a chave, a porta abre");
        helper.succeed();
    }

    @GameTest
    public void redstoneDoesNotOpenIt(GameTestHelper helper) {
        BlockPos lower = new BlockPos(1, 1, 1);
        door(helper, lower, "alguem");
        helper.setBlock(lower.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.succeedWhen(() -> {
            if (helper.getBlockState(lower).getValue(DoorBlock.OPEN)) throw helper.assertionException("a redstone não mexe na porta arcana");
        });
    }

    @GameTest(maxTicks = 60)
    public void theOwnersPlateOpensTheDoor(GameTestHelper helper) {
        BlockPos lower = new BlockPos(1, 1, 1);
        door(helper, lower, "alguem");
        BlockPos plate = lower.east();
        helper.setBlock(plate.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(plate, TCBlocks.ARCANE_PRESSURE_PLATE.defaultBlockState());
        helper.getBlockEntity(plate, OwnedBlockEntity.class).owner = "alguem";
        // um porco (qualquer coisa: a placa vem disparando com tudo) pisa nela
        helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, plate);
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(plate).getValue(ArcanePressurePlateBlock.POWERED)) throw helper.assertionException("a placa liga");
            if (!helper.getBlockState(lower).getValue(DoorBlock.OPEN)) throw helper.assertionException("e abre a porta do mesmo dono");
        });
    }

    @GameTest(maxTicks = 40)
    public void theEarHearsItsNote(GameTestHelper helper) {
        BlockPos ear = new BlockPos(1, 1, 1);
        BlockPos note = new BlockPos(3, 1, 1);
        helper.setBlock(ear.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(note.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(ear, TCBlocks.ARCANE_EAR.defaultBlockState());
        helper.setBlock(note, Blocks.NOTE_BLOCK.defaultBlockState());
        ArcaneEarBlockEntity sensor = helper.getBlockEntity(ear, ArcaneEarBlockEntity.class);
        sensor.updateTone();
        sensor.note = 5;
        var noteState = helper.getBlockState(note);
        // em cima de pedra, o bloco musical é bumbo; o ouvido também, pela pedra dele
        helper.setBlock(note, noteState.setValue(NoteBlock.NOTE, 5).setValue(NoteBlock.INSTRUMENT, net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM));
        helper.getLevel().blockEvent(helper.absolutePos(note), Blocks.NOTE_BLOCK, 0, 0);
        helper.succeedWhen(() -> {
            if (!helper.getBlockState(ear).getValue(ArcaneEarBlock.POWERED)) throw helper.assertionException("o ouvido ouve a nota dele e liga");
        });
    }
}
