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
import net.thaumcraft.Thaumcraft;

import java.util.List;

/**
 * O <b>Coven do Pântano</b>: a estrutura, o molde e onde ela nasce.
 *
 * <p><b>Este não é porte.</b> O Witchery não tem coven nenhum no mundo — ele dá a Bruxa do Coven e deixa-a
 * numa cabana dentro da aldeia. O coven do pântano é acréscimo, e vai marcado como tal aqui e no
 * {@code PORTE.md}.
 */
public class OccultaSwampCovenGameTest {
    private static final ResourceKey<Structure> COVEN =
            ResourceKey.create(Registries.STRUCTURE, Thaumcraft.id("swamp_coven"));

    /** A estrutura existe, e é uma só peça — não cresce como aldeia. */
    @GameTest(maxTicks = 20)
    public void theCovenIsOnePieceOnly(GameTestHelper helper) {
        var registos = helper.getLevel().registryAccess();
        var estruturas = registos.lookupOrThrow(Registries.STRUCTURE);
        Structure coven = estruturas.getValueOrThrow(COVEN);

        var ops = RegistryOps.create(JsonOps.INSTANCE, registos);
        var escrito = Structure.DIRECT_CODEC.encodeStart(ops, coven)
                .getOrThrow(erro -> new AssertionError("não deu para escrever o coven: " + erro));
        int tamanho = escrito.getAsJsonObject().get("size").getAsInt();
        if (tamanho != 1) {
            helper.fail("o coven é uma clareira e nada mais: tamanho devia ser 1, é " + tamanho);
        }
        helper.succeed();
    }

    /**
     * Ele nasce <b>no pântano, e só</b>.
     *
     * <p>E a prova olha os dois lados: que o pântano tem, e que a planície <b>não</b> — um coven numa planície
     * seria um acampamento à vista de todos, que é o contrário do que ele é.
     */
    @GameTest(maxTicks = 20)
    public void itBelongsToTheSwampAndNowhereElse(GameTestHelper helper) {
        var biomas = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        TagKey<Biome> etiqueta = TagKey.create(Registries.BIOME,
                Thaumcraft.id("has_structure/swamp_coven"));

        for (String bioma : List.of("swamp", "mangrove_swamp")) {
            if (!tem(biomas, etiqueta, bioma)) helper.fail("o coven nasce em " + bioma);
        }
        for (String bioma : List.of("plains", "desert", "forest", "taiga", "jungle")) {
            if (tem(biomas, etiqueta, bioma)) helper.fail("o coven não nasce em " + bioma);
        }
        helper.succeed();
    }

    /** E o molde carrega, com a clareira inteira: vinte e nove por dez por vinte e nove. */
    @GameTest(maxTicks = 20)
    public void theClearingTemplateLoads(GameTestHelper helper) {
        var moldes = helper.getLevel().getServer().getStructureManager();
        var molde = moldes.get(Thaumcraft.id("swamp_coven"));
        if (molde.isEmpty()) {
            helper.fail("o molde do coven devia carregar");
            return;
        }
        var t = molde.get().getSize();
        if (t.getX() != 29 || t.getY() != 10 || t.getZ() != 29) {
            helper.fail("a clareira é 29x10x29, veio " + t.getX() + "x" + t.getY() + "x" + t.getZ());
        }
        helper.succeed();
    }

    private static boolean tem(net.minecraft.core.HolderLookup.RegistryLookup<Biome> biomas,
                               TagKey<Biome> etiqueta, String bioma) {
        var chave = ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace(bioma));
        return biomas.get(etiqueta)
                .map(set -> set.stream().anyMatch(dono -> dono.is(chave)))
                .orElse(false);
    }
}
