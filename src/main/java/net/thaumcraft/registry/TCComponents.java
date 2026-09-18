package net.thaumcraft.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.AspectList;

/** O que os itens do mod carregam por dentro. */
public final class TCComponents {
    /** De que madeira a varinha é feita: manda em quanto ela guarda. */
    public static final DataComponentType<String> WAND_ROD = register("wand_rod",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** De que metal são as pontas: mandam em quanto cada uso custa. */
    public static final DataComponentType<String> WAND_CAP = register("wand_cap",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** O vis guardado, em centésimos — que é como o original conta. */
    public static final DataComponentType<AspectList> WAND_VIS = register("wand_vis",
            builder -> builder.persistent(AspectList.CODEC).networkSynchronized(AspectList.STREAM_CODEC));

    /** O foco preso na varinha, se houver. */
    public static final DataComponentType<String> WAND_FOCUS = register("wand_focus",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** O bloco que o foco de Troca Equivalente escolheu, guardado na varinha pelo id do item dele. */
    public static final DataComponentType<String> WAND_PICKED = register("wand_picked",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** O aspecto que um frasco de essência guarda. */
    public static final DataComponentType<String> PHIAL_ASPECT = register("phial_aspect",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** O aspecto marcado num rótulo de jarro: o rótulo marcado, que o tubo filtro aceita. */
    public static final DataComponentType<String> LABEL_ASPECT = register("label_aspect",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** A casa que o sino do golem está guardando: o baú para onde o golem vai levar o que juntar. */
    public static final DataComponentType<net.minecraft.core.BlockPos> GOLEM_HOME = register("golem_home",
            builder -> builder.persistent(net.minecraft.core.BlockPos.CODEC)
                    .networkSynchronized(net.minecraft.core.BlockPos.STREAM_CODEC));

    /** O que um jarro quebrado leva dentro: a essência, quanto dela e o rótulo. */
    public static final DataComponentType<net.thaumcraft.item.JarContents> JAR_CONTENTS = register("jar_contents",
            builder -> builder.persistent(net.thaumcraft.item.JarContents.CODEC)
                    .networkSynchronized(net.thaumcraft.item.JarContents.STREAM_CODEC));

    /** O que está escrito numa nota de pesquisa: a pesquisa e o tabuleiro de hexágonos. */
    public static final DataComponentType<net.thaumcraft.research.ResearchNote> RESEARCH_NOTE = register("research_note",
            builder -> builder.persistent(net.thaumcraft.research.ResearchNote.CODEC)
                    .networkSynchronized(net.thaumcraft.research.ResearchNote.STREAM_CODEC));

    private TCComponents() {
    }

    private static <T> DataComponentType<T> register(String name,
            java.util.function.UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Thaumcraft.id(name),
                builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {
    }
}
