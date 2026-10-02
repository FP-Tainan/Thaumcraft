package net.thaumcraft.occulta.village;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mixin.StructureTemplatePoolAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Os prédios que o Witchery põe dentro da aldeia do próprio jogo.
 *
 * <p>No jogo de 2014 isto era uma chamada — o {@code registerVillageCreationHandler} —, e a peça entrava na
 * tabela de pesos do gerador de aldeia. Hoje a aldeia é um <b>salto-de-encaixe</b>: as peças vivem em piscinas
 * carregadas do disco, e cada uma é um molde {@code .nbt} com um bloco de encaixe que diz por onde ela se
 * prende à rua.
 *
 * <p><b>Os pesos são os do original</b>, da tabela {@code townParts} do Config dele.
 *
 * <p><b>Quando isto corre.</b> Ao servidor arrancar, antes de se gerar qualquer trecho — as piscinas já estão
 * carregadas do disco e ninguém ainda lhes perguntou nada. Mais cedo não há piscina; mais tarde a aldeia já
 * estaria desenhada.
 */
public final class VillagePieces {
    /**
     * A Torre de Vigia, com peso 20 — o do original.
     *
     * <p>Lá ela entra em <b>quatro grupos</b> de zero a um, que no gerador de então era o jeito de dizer "até
     * quatro torres, e talvez nenhuma". Aqui o peso faz o mesmo trabalho: a profundidade do salto é que decide
     * quantas peças cabem, e o peso decide quantas vezes esta é sorteada entre as candidatas.
     */
    private static final int PESO_TORRE = 20;

    /**
     * O Forte, com peso 5.
     *
     * <p><b>Desvio declarado, e é o maior desta fatia.</b> No original ele tem <b>peso 100 e no máximo um por
     * aldeia</b> — o gerador de 2014 sabia limitar quantidade, e o salto-de-encaixe de hoje não sabe: peso aqui
     * só diz quantas vezes a peça é sorteada entre as candidatas, e nada impede que saia duas vezes.
     *
     * <p>Peso 100 contra os 87 que a piscina de casas do jogo soma faria <b>quase toda</b> construção da aldeia
     * ser um forte de dezessete por vinte e sete. O número escolhido é o que faz a conta dar <b>cerca de um</b>
     * forte por aldeia, que é o que o original entrega. Não é o número dele; é o efeito dele.
     */
    private static final int PESO_FORTE = 5;

    /** As cinco variantes de aldeia, que são as cinco piscinas de casas a mexer. */
    private static final List<String> VARIANTES =
            List.of("plains", "desert", "savanna", "snowy", "taiga");

    private VillagePieces() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(VillagePieces::junta);
    }

    private static void junta(MinecraftServer servidor) {
        var registos = servidor.registryAccess();
        var piscinas = registos.lookupOrThrow(Registries.TEMPLATE_POOL);
        Holder<StructureProcessorList> semRetoque =
                registos.lookupOrThrow(Registries.PROCESSOR_LIST).getOrThrow(ProcessorLists.EMPTY);

        // um molde por variante: o material muda no deserto, e o encaixe aponta à rua da sua aldeia
        for (String variante : VARIANTES) {
            acrescenta(piscinas, variante, Thaumcraft.id("village/watchtower_" + variante),
                    PESO_TORRE, semRetoque);
            acrescenta(piscinas, variante, Thaumcraft.id("village/keep_" + variante),
                    PESO_FORTE, semRetoque);
        }
    }

    /**
     * Soma uma peça à piscina de casas de uma variante.
     *
     * <p><b>As duas listas mudam.</b> A esticada pelo peso é de onde se sorteia; a de pares é a que o
     * {@code getMaxSize} lê para saber de quanto espaço a aldeia precisa. Mexer só na primeira faz a peça nascer
     * e ficar cortada ao meio.
     */
    private static void acrescenta(Registry<StructureTemplatePool> piscinas,
                                   String variante, Identifier molde, int peso,
                                   Holder<StructureProcessorList> processadores) {
        var chave = ResourceKey.create(Registries.TEMPLATE_POOL,
                Identifier.withDefaultNamespace("village/" + variante + "/houses"));
        StructureTemplatePool piscina = piscinas.getValueOrThrow(chave);

        StructurePoolElement peça = StructurePoolElement.single(molde.toString(), processadores)
                .apply(StructureTemplatePool.Projection.RIGID);

        var acesso = (StructureTemplatePoolAccessor) piscina;
        for (int i = 0; i < peso; i++) acesso.thaumcraft$templates().add(peça);

        List<Pair<StructurePoolElement, Integer>> pares =
                new ArrayList<>(acesso.thaumcraft$rawTemplates());
        pares.add(Pair.of(peça, peso));
        acesso.thaumcraft$setRawTemplates(pares);
    }

    /** Quantas vezes uma peça está numa piscina: serve às provas. */
    public static int quantas(StructureTemplatePool piscina, String molde) {
        return (int) ((StructureTemplatePoolAccessor) piscina).thaumcraft$templates().stream()
                .filter(peça -> peça.toString().contains(molde))
                .count();
    }
}
