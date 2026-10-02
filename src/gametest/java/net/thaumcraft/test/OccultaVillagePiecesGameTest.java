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
 * Os prédios que o Witchery põe na aldeia: que entraram nas piscinas, e que os moldes são os do original.
 */
public class OccultaVillagePiecesGameTest {
    private static final List<String> VARIANTES =
            List.of("plains", "desert", "savanna", "snowy", "taiga");

    /** Cada peça com o seu peso e o seu tamanho: nome do molde, peso, largura, altura, fundo. */
    private record Peça(String molde, int peso, int largura, int altura, int fundo) {
    }

    private static final List<Peça> PEÇAS = List.of(
            new Peça("watchtower", 20, 9, 24, 9),
            new Peça("keep", 5, 17, 27, 17),
            new Peça("apothecary", 15, 10, 10, 8),
            new Peça("bookshop", 15, 11, 9, 10),
            new Peça("wall_gen", 12, 3, 8, 3),
            new Peça("witch_hut", 10, 5, 7, 7));

    /**
     * As peças entraram nas cinco aldeias, com o peso de cada uma.
     *
     * <p>E a prova olha também as <b>casas do jogo</b>: somar à piscina errada, ou somar por cima em vez de ao
     * lado, apagaria as casas e a aldeia nasceria só de torres — e uma prova que olhasse só a torre diria que
     * está tudo bem.
     */
    @GameTest(maxTicks = 20)
    public void thePiecesJoinEveryVillage(GameTestHelper helper) {
        var piscinas = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);

        for (String variante : VARIANTES) {
            var chave = ResourceKey.create(Registries.TEMPLATE_POOL,
                    Identifier.withDefaultNamespace("village/" + variante + "/houses"));
            StructureTemplatePool piscina = piscinas.getValueOrThrow(chave);

            for (Peça peça : PEÇAS) {
                int quantas = VillagePieces.quantas(piscina, "village/" + peça.molde() + "_" + variante);
                if (quantas != peça.peso()) {
                    helper.fail("a aldeia " + variante + " devia ter " + peça.molde() + " com peso "
                            + peça.peso() + ", tem " + quantas);
                }
            }

            int casasDoJogo = VillagePieces.quantas(piscina, "village/" + variante + "/houses/");
            if (casasDoJogo == 0) {
                helper.fail("a aldeia " + variante + " perdeu as casas do jogo ao ganhar as peças");
            }
        }
        helper.succeed();
    }

    /**
     * E os moldes carregam, com as medidas do original.
     *
     * <p>Um molde que não carregue <b>não dá erro nenhum</b> na geração — a peça simplesmente não nasce, e a
     * aldeia sai sem ela sem ninguém dar por isso.
     */
    @GameTest(maxTicks = 20)
    public void theTemplatesLoadWithTheRightSize(GameTestHelper helper) {
        var moldes = helper.getLevel().getServer().getStructureManager();

        for (Peça peça : PEÇAS) {
            for (String variante : VARIANTES) {
                String nome = "village/" + peça.molde() + "_" + variante;
                var molde = moldes.get(Thaumcraft.id(nome));
                if (molde.isEmpty()) {
                    helper.fail("o molde " + nome + " devia carregar");
                    continue;
                }
                var t = molde.get().getSize();
                if (t.getX() != peça.largura() || t.getY() != peça.altura() || t.getZ() != peça.fundo()) {
                    helper.fail(nome + " é " + peça.largura() + "x" + peça.altura() + "x" + peça.fundo()
                            + ", veio " + t.getX() + "x" + t.getY() + "x" + t.getZ());
                }
            }
        }
        helper.succeed();
    }

    /**
     * E cada molde tem o seu bloco de encaixe, apontado à rua da <b>sua</b> aldeia.
     *
     * <p>É o encaixe que prende a peça à rua: sem ele a peça nunca é escolhida, e apontado à piscina errada ela
     * tenta crescer para a rua de outra aldeia. <b>Nenhuma das duas coisas dá erro</b> — a aldeia só sai sem a
     * peça —, e foi exatamente o engano que escapou na primeira volta desta fatia, quando os cinco moldes
     * diziam todos {@code plains}.
     */
    @GameTest(maxTicks = 20)
    public void everyTemplateHasItsEntranceToItsOwnStreets(GameTestHelper helper) {
        var moldes = helper.getLevel().getServer().getStructureManager();

        for (Peça peça : PEÇAS) {
            for (String variante : VARIANTES) {
                String nome = "village/" + peça.molde() + "_" + variante;
                var molde = moldes.get(Thaumcraft.id(nome));
                if (molde.isEmpty()) {
                    helper.fail("o molde " + nome + " devia carregar");
                    continue;
                }

                var encaixes = molde.get().getJigsaws(net.minecraft.core.BlockPos.ZERO,
                        net.minecraft.world.level.block.Rotation.NONE);
                if (encaixes.size() != 1) {
                    helper.fail(nome + " devia ter um encaixe, tem " + encaixes.size());
                    continue;
                }

                var encaixe = encaixes.getFirst();
                if (!encaixe.name().equals(Identifier.withDefaultNamespace("building_entrance"))) {
                    helper.fail(nome + " devia encaixar por building_entrance, encaixa por "
                            + encaixe.name());
                }
                var esperada = Identifier.withDefaultNamespace("village/" + variante + "/streets");
                if (!encaixe.pool().identifier().equals(esperada)) {
                    helper.fail(nome + " devia apontar a " + esperada + ", aponta a "
                            + encaixe.pool().identifier());
                }
            }
        }
        helper.succeed();
    }
}
