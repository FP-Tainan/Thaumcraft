package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.ClimateChange;
import net.thaumcraft.occulta.rite.RiteRegistry;

/**
 * A <b>Mudança de Clima</b>: o maior rito do ofício, e o único que muda o mundo.
 *
 * <p>A prova que carrega a fatia é a do <b>foco no chão</b>. O rito não tem alvo escrito em lado nenhum —
 * ele olha o que está largado à volta do círculo, e é essa lista de treze coisas que é o vocabulário
 * dele. Faltando uma, o clima correspondente deixa de existir e ninguém o descobre.
 */
public class OccultaClimateChangeGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>O rito está no livro</b>, com o anel de fora inteiro e as sete coisas. */
    @GameTest(maxTicks = 20)
    public void theRiteIsInTheBook(GameTestHelper helper) {
        RiteRegistry.Entry rito = RiteRegistry.all().stream()
                .filter(qual -> qual.key().equals("tc.rite.climatechange")).findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o rito devia estar no livro");
            return;
        }
        if (rito.outer().ritual() != 40) {
            helper.fail("ele pede o anel de fora inteiro, quarenta glifos de ritual");
            return;
        }
        if (!rito.inner().empty() || !rito.middle().empty()) {
            helper.fail("e só o de fora");
        }
        helper.succeed();
    }

    /**
     * <b>Os treze climas existem, e cada um tem biomas.</b>
     *
     * <p>Os climas do original vinham do dicionário de biomas da Forge, que já não existe; aqui são
     * rótulos escritos à mão. Um rótulo vazio faz o rito acender e não mudar nada.
     */
    @GameTest(maxTicks = 20)
    public void everyClimateHasBiomes(GameTestHelper helper) {
        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        var focos = ClimateChange.focos();
        if (focos.size() != 13) {
            helper.fail("são treze focos: são " + focos.size());
            return;
        }
        for (var entrada : focos.entrySet()) {
            var rol = biomas.get(entrada.getValue().rótulo()).orElse(null);
            if (rol == null || rol.size() == 0) {
                helper.fail("o clima " + entrada.getValue().nome() + " não tem bioma nenhum");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>O chão diz o clima</b>, e a glowstone diz qual bioma dele.
     *
     * <p>Sem glowstone sorteia-se entre os três primeiros; com duas pedras sai o <b>segundo</b>. É um
     * detalhe miúdo do original e é o que separa «quero floresta» de «quero floresta de bétulas».
     */
    @GameTest(maxTicks = 40)
    public void theGroundNamesTheClimate(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        if (ClimateChange.lêOChão(level, meio).clima() != null) {
            helper.fail("com o chão limpo não se pede clima nenhum");
            return;
        }

        level.addFreshEntity(new ItemEntity(level, meio.getX() + 1.5, meio.getY() + 0.5,
                meio.getZ() + 0.5, new ItemStack(Items.CACTUS)));
        level.addFreshEntity(new ItemEntity(level, meio.getX() - 1.5, meio.getY() + 0.5,
                meio.getZ() + 0.5, new ItemStack(Items.GLOWSTONE_DUST, 2)));

        var chão = ClimateChange.lêOChão(level, meio);
        if (chão.clima() == null || !chão.clima().nome().equals("desert")) {
            helper.fail("um cato no chão pede deserto");
            return;
        }
        if (chão.clima().tempo() != ClimateChange.Tempo.SOL) {
            helper.fail("e o deserto limpa o céu");
            return;
        }
        if (chão.glowstone() != 2) {
            helper.fail("e a glowstone conta: contou " + chão.glowstone());
            return;
        }
        if (chão.gastos().size() != 2) {
            helper.fail("e as duas coisas gastam-se");
            return;
        }

        // com duas pedras sai o segundo bioma do rótulo — e o do gelo tem quatro
        var gelo = new ClimateChange.Clima("frozen", ClimateChange.Tempo.CHUVA);
        var segundo = ClimateChange.qualBioma(level, gelo, 2);
        var rol = level.registryAccess().lookupOrThrow(Registries.BIOME).get(gelo.rótulo()).orElseThrow();
        if (segundo == null || !segundo.equals(rol.get(1))) {
            helper.fail("com duas pedras sai o segundo bioma do rótulo");
            return;
        }
        helper.succeed();
    }

    /** <b>E o raio sai do coven</b>: dezesseis blocos por bruxa acima de três. */
    @GameTest(maxTicks = 20)
    public void theRadiusComesFromTheCoven(GameTestHelper helper) {
        if (ClimateChange.raio(4) != 16) {
            helper.fail("com quatro bruxas, dezesseis blocos: deu " + ClimateChange.raio(4));
            return;
        }
        if (ClimateChange.raio(7) != 64) {
            helper.fail("e com sete, sessenta e quatro: deu " + ClimateChange.raio(7));
            return;
        }
        if (ClimateChange.COVEN_MÍNIMO != 4) helper.fail("e abaixo de quatro ele nem corre");
        helper.succeed();
    }

    /**
     * <b>E ele muda mesmo o bioma</b>, pelo caminho do próprio jogo.
     *
     * <p>É a única parte que não se pode medir por dentro: ou o bioma do chão mudou, ou não mudou.
     */
    @GameTest(maxTicks = 60)
    public void itReallyChangesTheBiome(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));
        var biomas = level.registryAccess().lookupOrThrow(Registries.BIOME);
        var deserto = biomas.get(net.minecraft.world.level.biome.Biomes.DESERT).orElseThrow();

        var antes = level.getBiome(meio);
        if (antes.is(net.minecraft.world.level.biome.Biomes.DESERT)) {
            helper.fail("a prova precisa de começar fora do deserto");
            return;
        }

        ClimateChange.muda(level, meio, 2, deserto);
        if (!level.getBiome(meio).is(net.minecraft.world.level.biome.Biomes.DESERT)) {
            helper.fail("depois do rito o chão é deserto: é " + level.getBiome(meio));
            return;
        }
        // e devolve o que mudou, para a prova não mexer na arena do lado mais do que o pedido
        ClimateChange.muda(level, meio, 2, antes);
        helper.succeed();
    }

    /** <b>A Agulha de Gelo é um dos treze</b> — a única coisa do ofício entre eles. */
    @GameTest(maxTicks = 20)
    public void theIcyNeedleIsOneOfTheThirteen(GameTestHelper helper) {
        var qual = ClimateChange.focos().get(OccultaItems.ICY_NEEDLE);
        if (qual == null || !qual.nome().equals("frozen")) {
            helper.fail("a Agulha de Gelo pede gelo");
            return;
        }
        if (qual.tempo() != ClimateChange.Tempo.CHUVA) helper.fail("e o gelo faz chover");
        helper.succeed();
    }
}
