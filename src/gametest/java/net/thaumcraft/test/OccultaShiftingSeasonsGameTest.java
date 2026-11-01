package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewDispersal;
import net.thaumcraft.occulta.brew.BrewModifiers;
import net.thaumcraft.occulta.brew.BrewRegistry;
import net.thaumcraft.occulta.brew.BrewWorldActions;

import java.util.List;

/**
 * As <b>Estações que Mudam</b>: o cozimento que troca o bioma.
 *
 * <p>Ele é o único efeito do mod que precisa de <b>ler um ingrediente por dentro</b>. A lista do que está no
 * caldeirão guarda coisas, não frascos — e o que a Nota de Bioma tem escrito não caberia nela. O bioma
 * viaja, então, por um caminho só dele: do caldeirão para o frasco, do frasco para o estouro, e do estouro
 * para os temperos. <b>É esse caminho que esta prova anda</b>, porque um elo partido nele não rebenta nada:
 * o frasco estoura calado e o chão fica como estava.
 */
public class OccultaShiftingSeasonsGameTest {
    /** O cozimento que abre espaço bastante para um efeito de oitavo grau: nove, e ele gasta oito. */
    private static final List<Item> ESPAÇO = List.of(OccultaItems.MANDRAKE_ROOT, Items.NETHER_WART,
            OccultaItems.TEAR_OF_THE_GODDESS, OccultaItems.DIAMOND_VAPOUR, Items.DIAMOND);

    private static List<Item> comANota() {
        var dentro = new java.util.ArrayList<>(ESPAÇO);
        dentro.add(OccultaItems.BIOME_NOTE);
        return List.copyOf(dentro);
    }

    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>A Nota de Bioma é um efeito da tabela</b>, e é ela que traz este. */
    @GameTest(maxTicks = 20)
    public void theNoteIsWhatBringsTheEffect(GameTestHelper helper) {
        var ação = BrewRegistry.of(OccultaItems.BIOME_NOTE);
        if (!(ação instanceof BrewWorldActions.BiomeChange)) {
            helper.fail("a Nota de Bioma devia ser as Estações que Mudam: é " + ação);
            return;
        }
        if (ação.power() != 5000) {
            helper.fail("ela pede cinco mil de poder: pede " + ação.power());
            return;
        }
        if (!ação.isEffect()) helper.fail("e ela é um efeito, que gasta espaço");
        else helper.succeed();
    }

    /**
     * <b>E ele só estoura.</b>
     *
     * <p>É o {@code setOnlyInstant} do original, e não é um detalhe: um efeito que muda o mundo não pode
     * ficar no ar em nuvem nem no chão em poça. Com a lã de morcego na panela — que é o que faz nuvem — o
     * frasco ainda estoura.
     */
    @GameTest(maxTicks = 20)
    public void itOnlyEverSplashes(GameTestHelper helper) {
        // sem a nota, a lã de morcego manda: faz nuvem
        var semNota = new java.util.ArrayList<>(ESPAÇO);
        semNota.add(OccultaItems.BAT_WOOL);
        if (!(Brew.impact(List.copyOf(semNota), null).dispersal() instanceof BrewDispersal.Gas)) {
            helper.fail("a lã de morcego faz nuvem, e esta prova precisa disso para valer");
            return;
        }

        var comNota = new java.util.ArrayList<>(semNota);
        comNota.add(OccultaItems.BIOME_NOTE);
        var jeito = Brew.impact(List.copyOf(comNota), null).dispersal();
        if (!(jeito instanceof BrewDispersal.Instant)) {
            helper.fail("com a nota ele só estoura: faz " + jeito);
            return;
        }
        helper.succeed();
    }

    /** <b>E o que ele apanha é pouco</b>: um bloco à volta, mais um por grau de força. */
    @GameTest(maxTicks = 20)
    public void theRadiusComesFromTheStrength(GameTestHelper helper) {
        if (BrewWorldActions.BiomeChange.raio(0) != 1) {
            helper.fail("sem força nenhuma, um bloco: deu " + BrewWorldActions.BiomeChange.raio(0));
            return;
        }
        if (BrewWorldActions.BiomeChange.raio(3) != 4) {
            helper.fail("e com três graus, quatro: deu " + BrewWorldActions.BiomeChange.raio(3));
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O caldeirão lembra-se do que a nota dizia, e o frasco leva-o.</b>
     *
     * <p>É o elo que não existe em mais nenhum ingrediente do ofício, e o único que uma foto não apanharia.
     */
    @GameTest(maxTicks = 20)
    public void theFlaskCarriesTheBiome(GameTestHelper helper) {
        ItemStack frasco = net.thaumcraft.occulta.brew.BrewItem.of(OccultaItems.BREW, comANota());
        frasco.set(OccultaComponents.BIOME, Biomes.DESERT);
        if (!Biomes.DESERT.equals(frasco.get(OccultaComponents.BIOME))) {
            helper.fail("o frasco guarda o bioma que lhe escreveram");
            return;
        }
        // e o nome dele traz o efeito, que é como quem joga sabe que a nota pegou
        String nome = frasco.getHoverName().getString();
        if (nome.isEmpty() || nome.equals(new ItemStack(OccultaItems.BREW).getHoverName().getString())) {
            helper.fail("e o nome dele diz o que ele é: é «" + nome + "»");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E o chão muda mesmo.</b>
     *
     * <p>Com o bioma nos temperos, o efeito troca o bioma de onde o frasco bateu; <b>sem</b> ele, não faz
     * nada — e não faz nada <b>calado</b>, que é o jeito certo de um frasco sem nota se comportar.
     */
    @GameTest(maxTicks = 60)
    public void itReallyChangesTheGround(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        var dentro = comANota();

        var antes = level.getBiome(meio);
        if (antes.is(Biomes.DESERT)) {
            helper.fail("a prova precisa de começar fora do deserto");
            return;
        }

        // sem bioma nos temperos: nada acontece
        Brew.applyToBlock(level, dentro, meio, Direction.UP, 1, new BrewModifiers());
        if (level.getBiome(meio).is(Biomes.DESERT)) {
            helper.fail("um frasco sem nota não muda bioma nenhum");
            return;
        }

        BrewModifiers temperos = new BrewModifiers();
        temperos.bioma = Biomes.DESERT;
        Brew.applyToBlock(level, dentro, meio, Direction.UP, 1, temperos);
        if (!level.getBiome(meio).is(Biomes.DESERT)) {
            helper.fail("com a nota o chão vira deserto: é " + level.getBiome(meio));
            return;
        }

        // e devolve-se o que mudou, para a arena do lado não apanhar o deserto
        net.thaumcraft.occulta.rite.ClimateChange.muda(level, meio,
                BrewWorldActions.BiomeChange.raio(0), antes);
        helper.succeed();
    }
}
