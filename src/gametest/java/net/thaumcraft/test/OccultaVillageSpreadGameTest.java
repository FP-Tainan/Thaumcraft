package net.thaumcraft.test;

import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/**
 * As aldeias do Witchery: <b>maiores</b>, e em <b>mais lugares</b>.
 *
 * <p>Esta fatia não tem bicho nem bloco — ela mexe só no que o jogo lê do disco. Por isso as provas são sobre o
 * que ficou <b>carregado no registro</b>, que é a única coisa que importa: se a etiqueta do bioma somou, e se o
 * tamanho da aldeia é o novo.
 */
public class OccultaVillageSpreadGameTest {
    /** As cinco variantes de aldeia do jogo. */
    private static final List<String> VARIANTES =
            List.of("plains", "desert", "savanna", "snowy", "taiga");

    /** O tamanho novo: o salto-de-encaixe vai mais fundo, e a aldeia se espalha mais. */
    private static final int TAMANHO = 8;

    /**
     * As aldeias ficaram maiores.
     *
     * <p>O {@code size} de uma estrutura de encaixe não tem acessor público, e por isso se pergunta-lhe pelo
     * próprio codec: escreve-se a estrutura <b>como ela foi carregada</b> e se lê o número de volta. Prova o que
     * o jogo tem na mão, e não o que está escrito num arquivo que ele podia nem ter lido.
     */
    @GameTest(maxTicks = 20)
    public void villagesAreBigger(GameTestHelper helper) {
        var registros = helper.getLevel().registryAccess();
        var ops = RegistryOps.create(JsonOps.INSTANCE, registros);
        var estruturas = registros.lookupOrThrow(Registries.STRUCTURE);

        for (String qual : VARIANTES) {
            var chave = ResourceKey.create(Registries.STRUCTURE,
                    Identifier.withDefaultNamespace("village_" + qual));
            Structure aldeia = estruturas.getValueOrThrow(chave);
            var escrito = Structure.DIRECT_CODEC.encodeStart(ops, aldeia)
                    .getOrThrow(erro -> new AssertionError("não deu para escrever village_" + qual + ": " + erro));
            int tamanho = escrito.getAsJsonObject().get("size").getAsInt();
            if (tamanho != TAMANHO) {
                helper.fail("a aldeia " + qual + " devia ter tamanho " + TAMANHO + ", tem " + tamanho);
            }
        }
        helper.succeed();
    }

    /**
     * E nascem em mais biomas, somando-se aos do jogo.
     *
     * <p>A prova olha os dois lados: que os acréscimos entraram, e que <b>os do jogo continuam lá</b> — uma
     * etiqueta escrita com {@code replace} por engano apagaria as planícies, e tudo continuaria a parecer bem.
     */
    @GameTest(maxTicks = 20)
    public void villagesReachTheDryBiomes(GameTestHelper helper) {
        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);

        record Caso(String variante, String bioma) {
        }
        List<Caso> deviamTer = List.of(
                // os do jogo, que não podem ter sido apagados pela soma
                new Caso("plains", "plains"),
                new Caso("desert", "desert"),
                new Caso("savanna", "savanna"),
                new Caso("snowy", "snowy_plains"),
                new Caso("taiga", "taiga"),
                // e os que o Witchery acrescenta
                new Caso("plains", "forest"),
                new Caso("plains", "dark_forest"),
                new Caso("plains", "windswept_hills"),
                new Caso("plains", "cherry_grove"),
                new Caso("desert", "badlands"),
                new Caso("desert", "wooded_badlands"),
                new Caso("savanna", "savanna_plateau"),
                new Caso("snowy", "grove"),
                new Caso("snowy", "ice_spikes"),
                new Caso("taiga", "snowy_taiga"),
                new Caso("taiga", "old_growth_pine_taiga"));

        for (Caso caso : deviamTer) {
            if (!temBioma(biomas, caso.variante(), caso.bioma())) {
                helper.fail("a aldeia " + caso.variante() + " devia nascer em " + caso.bioma());
            }
        }
        helper.succeed();
    }

    /**
     * E o molhado continua sem aldeia, como no original.
     *
     * <p>O {@code WorldHandlerVillageDistrict} recusa <b>molhado, oceano, praia, rio, selva, Nether e End</b>, e
     * a selva mesmo estando na lista dele vem desligada de fábrica. Sem esta prova, um acréscimo distraído punha
     * aldeia no pântano — que é justamente onde o coven vai morar, e onde ela não pode estar.
     */
    @GameTest(maxTicks = 20)
    public void theWetAndTheJungleStayWithoutVillages(GameTestHelper helper) {
        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        List<String> proibidos = List.of("swamp", "mangrove_swamp", "jungle", "bamboo_jungle",
                "sparse_jungle", "river", "beach", "ocean", "mushroom_fields");

        for (String bioma : proibidos) {
            for (String variante : VARIANTES) {
                if (temBioma(biomas, variante, bioma)) {
                    helper.fail("não devia nascer aldeia " + variante + " em " + bioma);
                }
            }
        }
        helper.succeed();
    }

    /** Se a etiqueta da variante tem aquele bioma. */
    private static boolean temBioma(net.minecraft.core.HolderLookup.RegistryLookup<Biome> biomas,
                                    String variante, String bioma) {
        TagKey<Biome> etiqueta = TagKey.create(Registries.BIOME,
                Identifier.withDefaultNamespace("has_structure/village_" + variante));
        var chave = ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(bioma));
        return biomas.get(etiqueta)
                .map(set -> set.stream().anyMatch(dono -> dono.is(chave)))
                .orElse(false);
    }
}
