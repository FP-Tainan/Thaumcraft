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
