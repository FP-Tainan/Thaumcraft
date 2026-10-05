package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.GlowGlobeBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.ShadedGlassBlock;
import net.thaumcraft.occulta.vampire.BloodedWool;
import net.thaumcraft.occulta.vampire.Vampire;

/**
 * O <b>Vidro Sombreado</b>, a <b>Lã Ensanguentada</b> e o <b>Globo de Luz</b>.
 *
 * <p>A prova que carrega a fatia é a do vidro: <b>aberto ele deixa passar a luz, fechado não</b>. É a única
 * coisa que ele faz, e é o que torna possível uma casa de vampiro com janelas.
 */
public class OccultaShadedGlassGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números deles são os do original. */
    @GameTest(maxTicks = 20)
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (ShadedGlassBlock.COME_A_LUZ != 15) helper.fail("fechado, o vidro come a luz toda");
        if (GlowGlobeBlock.ACENDE != 15) helper.fail("e o globo acende quinze");
        if (BloodedWool.CUSTA != 125) helper.fail("a lã custa cento e vinte e cinco de sangue");
        if (BloodedWool.GRAU != 4) helper.fail("e pede o quarto grau de vampiro");
        helper.succeed();
    }

    /**
     * <b>Aberto ele deixa passar a luz; fechado, não.</b>
     *
     * <p>Esta é a prova que carrega a fatia, e ela pergunta ao próprio bloco quanto da luz ele come — que é
     * o {@code setLightOpacity} do original, e a única coisa que separa os dois feitios.
     */
    @GameTest(maxTicks = 40)
    public void openItLetsTheLightThroughAndShutItDoesNot(GameTestHelper helper) {
        var aberto = OccultaBlocks.SHADED_GLASS.defaultBlockState();
        var fechado = aberto.setValue(ShadedGlassBlock.FECHADO, true);

        if (aberto.getLightDampening() != 0) helper.fail("aberto, ele não come luz nenhuma");
        if (fechado.getLightDampening() != ShadedGlassBlock.COME_A_LUZ) {
            helper.fail("fechado, ele come a luz toda");
        }
        if (!aberto.propagatesSkylightDown()) helper.fail("e o sol atravessa o aberto");
        if (fechado.propagatesSkylightDown()) helper.fail("mas não o fechado");
        helper.succeed();
    }

    /**
     * <b>E a redstone é quem o fecha.</b>
     *
     * <p>Posto ao lado de um bloco com corrente, ele nasce fechado; tirando a corrente, ele abre sozinho.
     */
    @GameTest(maxTicks = 40)
    public void redstoneIsWhatShutsIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos vidro = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(vidro, OccultaBlocks.SHADED_GLASS.defaultBlockState());
        if (level.getBlockState(vidro).getValue(ShadedGlassBlock.FECHADO)) {
            helper.fail("sem corrente ele está aberto");
        }

        level.setBlockAndUpdate(vidro.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        if (!level.getBlockState(vidro).getValue(ShadedGlassBlock.FECHADO)) {
            helper.fail("e a corrente fecha-o");
        }

        level.setBlockAndUpdate(vidro.east(), Blocks.AIR.defaultBlockState());
        if (level.getBlockState(vidro).getValue(ShadedGlassBlock.FECHADO)) {
            helper.fail("e tirá-la abre-o outra vez");
        }

        level.setBlockAndUpdate(vidro, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** As dezesseis cores existem, e o item diz a sua. */
    @GameTest(maxTicks = 20)
    public void itComesInSixteenColours(GameTestHelper helper) {
        if (DyeColor.values().length != 16) helper.fail("são dezesseis cores no jogo");
        var vidro = new ItemStack(net.thaumcraft.occulta.OccultaItems.SHADED_GLASS);
        vidro.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                        .with(ShadedGlassBlock.COR, DyeColor.RED));
        if (net.thaumcraft.occulta.ShadedGlassItem.cor(vidro) != DyeColor.RED) {
            helper.fail("e o item leva a sua consigo");
        }
        helper.succeed();
    }

    /**
     * <b>A agulha de osso tinge a lã branca</b>, e só na mão de um vampiro com sangue que chegue.
     *
     * <p>É um caminho curioso e vale prová-lo inteiro: para ter uma capa de bruxa é preciso um vampiro, uma
     * ovelha branca e um forno.
     */
    @GameTest(maxTicks = 40)
    public void theNeedleDyesTheWoolAndOnlyForAVampire(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos lã = helper.absolutePos(new BlockPos(6, 2, 6));
        level.setBlockAndUpdate(lã, Blocks.WOOL.pick(DyeColor.WHITE).defaultBlockState());

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(net.thaumcraft.mortuorum.MortuorumItems.BONE_NEEDLE));
        var acertou = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(lã), net.minecraft.core.Direction.UP, lã, false);

        // quem não é vampiro não tinge nada
        BloodedWool.tinge(quem, level, net.minecraft.world.InteractionHand.MAIN_HAND, acertou);
        if (level.getBlockState(lã).is(OccultaBlocks.BLOODED_WOOL)) {
            helper.fail("quem não é vampiro não tinge lã nenhuma");
        }

        Vampire.levantaOTeto(quem, Vampire.TETO);
        Vampire.grau(quem, BloodedWool.GRAU);
        Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
        BloodedWool.tinge(quem, level, net.minecraft.world.InteractionHand.MAIN_HAND, acertou);
        if (!level.getBlockState(lã).is(OccultaBlocks.BLOODED_WOOL)) {
            helper.fail("e um vampiro do quarto grau tinge");
        }

        level.setBlockAndUpdate(lã, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
