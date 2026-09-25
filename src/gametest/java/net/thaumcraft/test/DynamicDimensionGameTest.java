package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.thaumcraft.world.DynamicDimensions;

/**
 * Abrir e fechar mundos com o jogo andando — o que os bolsos das Portas Dimensionais vão precisar.
 *
 * <p>É a prova que faltava antes de começar aquele porte: o jogo de hoje monta os mundos uma vez, na abertura, e
 * não tem porta para se pedir mais um depois. Aqui pede-se, escreve-se dentro, lê-se de volta e fecha-se.
 */
public class DynamicDimensionGameTest {
    /** Um mundo novo, aberto com o jogo andando, que aceita blocos e volta a fechar. */
    @GameTest
    public void aWorldCanBeOpenedWhileTheGameRuns(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        ResourceKey<Level> chave = DynamicDimensions.key("test_pocket");
        if (server.getLevel(chave) != null) {
            DynamicDimensions.remove(server, chave);
        }

        var gerador = new net.minecraft.world.level.levelgen.FlatLevelSource(
                net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings.getDefault(
                        server.registryAccess().lookupOrThrow(Registries.BIOME),
                        server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                        server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)));

        var mundo = DynamicDimensions.create(server, chave, BuiltinDimensionTypes.OVERWORLD, gerador);
        if (mundo == null) {
            helper.fail("o mundo novo devia abrir");
            return;
        }
        if (server.getLevel(chave) != mundo) helper.fail("e entrar na lista do servidor");

        // escreve-se lá dentro e lê-se de volta
        BlockPos onde = new BlockPos(0, 70, 0);
        mundo.setBlockAndUpdate(onde, Blocks.DIAMOND_BLOCK.defaultBlockState());
        if (!mundo.getBlockState(onde).is(Blocks.DIAMOND_BLOCK)) {
            helper.fail("o que se escreve no mundo novo devia ficar lá");
        }
        if (mundo.dimension() != chave) helper.fail("e o mundo devia saber o nome dele");

        if (!DynamicDimensions.remove(server, chave)) helper.fail("e o mundo devia fechar");
        if (server.getLevel(chave) != null) helper.fail("e sair da lista do servidor");
        helper.succeed();
    }

    /** Dois bolsos são dois mundos, cada um com o seu número e o seu conteúdo. */
    @GameTest
    public void twoPocketsAreTwoWorlds(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var gerador = new net.minecraft.world.level.levelgen.FlatLevelSource(
                net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings.getDefault(
                        server.registryAccess().lookupOrThrow(Registries.BIOME),
                        server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                        server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)));

        ResourceKey<Level> um = DynamicDimensions.key("test_pocket", 1);
        ResourceKey<Level> dois = DynamicDimensions.key("test_pocket", 2);
        for (var chave : java.util.List.of(um, dois)) {
            if (server.getLevel(chave) != null) DynamicDimensions.remove(server, chave);
        }

        var primeiro = DynamicDimensions.create(server, um, BuiltinDimensionTypes.OVERWORLD, gerador);
        var segundo = DynamicDimensions.create(server, dois, BuiltinDimensionTypes.OVERWORLD, gerador);
        if (primeiro == null || segundo == null) {
            helper.fail("os dois bolsos deviam abrir");
            return;
        }
        if (primeiro == segundo) helper.fail("e serem dois mundos, não um");

        BlockPos onde = new BlockPos(0, 70, 0);
        primeiro.setBlockAndUpdate(onde, Blocks.GOLD_BLOCK.defaultBlockState());
        segundo.setBlockAndUpdate(onde, Blocks.IRON_BLOCK.defaultBlockState());
        if (!primeiro.getBlockState(onde).is(Blocks.GOLD_BLOCK)) helper.fail("o que está num não é o do outro");
        if (!segundo.getBlockState(onde).is(Blocks.IRON_BLOCK)) helper.fail("nem o do outro o deste");

        DynamicDimensions.remove(server, um);
        DynamicDimensions.remove(server, dois);
        helper.succeed();
    }

    /** O mundo de cima não se fecha nem por engano. */
    @GameTest
    public void theOverworldCannotBeClosed(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        if (DynamicDimensions.remove(server, Level.OVERWORLD)) {
            helper.fail("o mundo de cima não se fecha");
        }
        helper.succeed();
    }
}
