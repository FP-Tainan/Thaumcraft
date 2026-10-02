package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.village.VillagePieces;

import java.util.List;

/**
 * A Torre de Vigia: que ela entrou nas aldeias, e que o molde dela é o do original.
 */
public class OccultaWatchtowerGameTest {
    private static final List<String> VARIANTES =
            List.of("plains", "desert", "savanna", "snowy", "taiga");

    /** O peso do original, da tabela {@code townParts} do Config dele. */
    private static final int PESO = 20;

    /**
     * A torre entrou nas cinco aldeias, com o peso do original.
     *
     * <p>E a prova olha também as <b>casas do jogo</b>: somar à piscina errada, ou somar por cima em vez de ao
     * lado, apagaria as casas e a aldeia nasceria só de torres — e uma prova que olhasse só a torre diria que
     * está tudo bem.
     */
    @GameTest(maxTicks = 20)
    public void theWatchtowerJoinsEveryVillage(GameTestHelper helper) {
        var piscinas = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);

        for (String variante : VARIANTES) {
            var chave = ResourceKey.create(Registries.TEMPLATE_POOL,
                    Identifier.withDefaultNamespace("village/" + variante + "/houses"));
            StructureTemplatePool piscina = piscinas.getValueOrThrow(chave);

            int torres = VillagePieces.quantas(piscina, "village/watchtower");
            if (torres != PESO) {
                helper.fail("a aldeia " + variante + " devia ter a torre com peso " + PESO
                        + ", tem " + torres);
            }

            int casasDoJogo = VillagePieces.quantas(piscina, "village/" + variante + "/houses/");
            if (casasDoJogo == 0) {
                helper.fail("a aldeia " + variante + " perdeu as casas do jogo ao ganhar a torre");
            }
        }
        helper.succeed();
    }

    /**
     * E o molde carrega, com as medidas do original.
     *
     * <p>Nove por vinte e quatro por nove, que é o {@code (0,0,0 .. 8,23,8)} do
     * {@code ComponentVillageWatchTower}. Um molde que não carregue não dá erro nenhum na geração — a peça
     * simplesmente não nasce, e a aldeia sai sem torre sem ninguém dar por isso.
     */
    @GameTest(maxTicks = 20)
    public void theWatchtowerTemplateLoads(GameTestHelper helper) {
        var moldes = helper.getLevel().getServer().getStructureManager();
        var molde = moldes.get(Thaumcraft.id("village/watchtower"));
        if (molde.isEmpty()) helper.fail("o molde da torre devia carregar");

        var tamanho = molde.get().getSize();
        if (tamanho.getX() != 9 || tamanho.getY() != 24 || tamanho.getZ() != 9) {
            helper.fail("a torre é 9x24x9, veio " + tamanho.getX() + "x" + tamanho.getY()
                    + "x" + tamanho.getZ());
        }
        helper.succeed();
    }
}
