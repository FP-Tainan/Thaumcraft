package net.thaumcraft.occulta;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.thaumcraft.Thaumcraft;

import java.util.List;
import java.util.function.UnaryOperator;

/** O que os itens do Ars Occulta carregam por dentro. */
public final class OccultaComponents {
    /**
     * O que está no frasco de cozimento: a lista dos ingredientes, pela ordem em que caíram no caldeirão.
     *
     * <p>No Witchery isto é o NBT do líquido, com a lista {@code Items} e, ao lado dela, a cor, o poder e o nome
     * já contados. Aqui guarda-se só a lista: o resto lê-se dela de cada vez, e assim não há duas verdades sobre
     * o mesmo frasco.
     */
    public static final DataComponentType<List<Item>> BREW = register("brew",
            builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec().listOf())
                    .networkSynchronized(ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM)
                            .apply(ByteBufCodecs.list())));

    /** A quem uma boneca ou um frasco está preso: o vínculo do {@code ItemTaglockKit}. */
    public static final DataComponentType<TaglockItem.Taglock> TAGLOCK = register("taglock",
            builder -> builder.persistent(TaglockItem.Taglock.CODEC)
                    .networkSynchronized(TaglockItem.Taglock.STREAM_CODEC));

    private OccultaComponents() {
    }

    private static <T> DataComponentType<T> register(String nome,
                                                     UnaryOperator<DataComponentType.Builder<T>> feitio) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Thaumcraft.id(nome),
                feitio.apply(DataComponentType.builder()).build());
    }

    /** Chamado ao carregar o ramo, para as classes acordarem na ordem certa. */
    public static void init() {
    }
}
