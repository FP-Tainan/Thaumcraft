package net.thaumcraft.arcana;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.thaumcraft.Thaumcraft;

import java.util.function.UnaryOperator;

/** O que os itens do Ars Arcana carregam por dentro. */
public final class ArcanaComponents {
    /**
     * O feitiço escrito num item.
     *
     * <p>No Ars Magica 2 ele mora espalhado pelo NBT — {@code NumStages}, {@code ShapeOrdinal_0} e companhia.
     * Aqui é uma coisa só, e é a frase inteira.
     */
    public static final DataComponentType<Spell> SPELL = register("spell",
            builder -> builder.persistent(Spell.CODEC).networkSynchronized(Spell.STREAM_CODEC));

    /**
     * O bloco que o <b>Colocar Bloco</b> aprendeu a pôr.
     *
     * <p>No original é um par de números no NBT da varinha ({@code PlaceBlockID} e {@code PlaceMeta}); aqui é
     * o feitio do bloco inteiro, que é o que o jogo de hoje tem no lugar da metadata.
     */
    public static final DataComponentType<net.minecraft.world.level.block.state.BlockState> PLACE_BLOCK =
            register("place_block", builder -> builder
                    .persistent(net.minecraft.world.level.block.state.BlockState.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(
                            net.minecraft.world.level.block.state.BlockState.CODEC)));

    /** E o que a <b>Apropriação</b> levou consigo: o {@code stored_data}. */
    public static final DataComponentType<Appropriated> APPROPRIATED = register("appropriated",
            builder -> builder.persistent(Appropriated.CODEC)
                    .networkSynchronized(Appropriated.STREAM_CODEC));

    private ArcanaComponents() {
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
