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
     * já contados. Aqui se guarda só a lista: o resto se lê dela de cada vez, e assim não há duas verdades sobre
     * o mesmo frasco.
     */
    public static final DataComponentType<List<Item>> BREW = register("brew",
            builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec().listOf())
                    .networkSynchronized(ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM)
                            .apply(ByteBufCodecs.list())));

    /**
     * O que está dentro do <b>Cálice de Vidro</b>: o {@code WITCBloodUUID} do {@code ItemGlassGoblet}.
     *
     * <p>Sem ele o cálice está vazio, e um cálice vazio é só um copo.
     */
    public static final DataComponentType<net.thaumcraft.occulta.vampire.GobletBlood> GOBLET =
            register("goblet", builder -> builder
                    .persistent(net.thaumcraft.occulta.vampire.GobletBlood.CODEC)
                    .networkSynchronized(net.thaumcraft.occulta.vampire.GobletBlood.STREAM_CODEC));

    /**
     * Quantas páginas um <b>Livro do Vampiro</b> tem: o {@code damage} do {@code ItemMarkupBook} do original.
     *
     * <p>Lá o número de páginas vivia no dano do item, que era o jeito de 2014 de dar estados a uma coisa.
     * Aqui é um componente, que é o jeito de hoje — e com isso o livro deixa de parecer uma ferramenta
     * gasta e passa a ser o que é: um livro rasgado.
     */
    public static final DataComponentType<Integer> VAMPIRE_PAGES = register("vampire_pages",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** A quem uma boneca ou um frasco está preso: o vínculo do {@code ItemTaglockKit}. */
    public static final DataComponentType<TaglockItem.Taglock> TAGLOCK = register("taglock",
            builder -> builder.persistent(TaglockItem.Taglock.CODEC)
                    .networkSynchronized(TaglockItem.Taglock.STREAM_CODEC));

    /** O que um Espelho em item leva dentro: a cela dele, e se o Reflexo já morreu. */
    public static final DataComponentType<net.thaumcraft.occulta.mirror.MirrorLink.Held> MIRROR =
            register("mirror", builder -> builder
                    .persistent(net.thaumcraft.occulta.mirror.MirrorLink.Held.CODEC)
                    .networkSynchronized(net.thaumcraft.occulta.mirror.MirrorLink.Held.STREAM_CODEC));

    /**
     * O lugar a que uma Pedra de Caminho está presa: o {@code PosX}/{@code PosY}/{@code PosZ}/{@code PosD} do
     * {@code bindToLocation}.
     *
     * <p>No original o nome do mundo é guardado à parte, num {@code NameD}, porque em 2014 uma dimensão era um
     * número e o número não dizia nada a ninguém. Hoje a dimensão <b>é</b> o nome dela, e guardar os dois seria
     * guardar duas verdades sobre o mesmo lugar.
     */
    public static final DataComponentType<net.thaumcraft.occulta.waystone.Waystones.Lugar> WAYSTONE =
            register("waystone", builder -> builder
                    .persistent(net.thaumcraft.occulta.waystone.Waystones.Lugar.CODEC)
                    .networkSynchronized(net.thaumcraft.occulta.waystone.Waystones.Lugar.STREAM_CODEC));

    /**
     * O virote que está <b>dentro</b> da Besta de Mão: o {@code WITCBoltTypeCurrent} do original.
     *
     * <p>A besta não guarda uma munição qualquer — guarda <b>qual</b>, porque é o tipo que decide o que o tiro
     * faz. Sem este componente ela está vazia, e vazia ela não atira: só se carrega.
     */
    public static final DataComponentType<Item> BOLT_LOADED = register("bolt_loaded",
            builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(
                            net.minecraft.core.registries.Registries.ITEM)));

    /** E o que a pessoa escolheu da última vez: o {@code WITCBoltTypePreferred}, que a besta recarrega sozinha. */
    public static final DataComponentType<Item> BOLT_PREFERRED = register("bolt_preferred",
            builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(
                            net.minecraft.core.registries.Registries.ITEM)));

    /**
     * <b>De quem é este pedido</b>: o {@code WITCQuestOwnerID} do Witchery.
     *
     * <p>Vai dentro da coisa que cai do bicho de estimação de uma bruxa do coven, e é por ela que a bruxa
     * reconhece o que é dela. Sem isto, um olho de aranha qualquer fechava o pedido de qualquer bruxa.
     */
    public static final DataComponentType<java.util.UUID> QUEST_OWNER = register("quest_owner",
            builder -> builder.persistent(net.minecraft.core.UUIDUtil.CODEC)
                    .networkSynchronized(net.minecraft.core.UUIDUtil.STREAM_CODEC));

    /** Que porta esta chave abre: o mundo e as três contas do {@code doorX}/{@code doorY}/{@code doorZ}. */
    public static final DataComponentType<net.minecraft.core.GlobalPos> DOOR_KEY = register("door_key",
            builder -> builder.persistent(net.minecraft.core.GlobalPos.CODEC)
                    .networkSynchronized(net.minecraft.core.GlobalPos.STREAM_CODEC));

    /** E o que um chaveiro traz: o {@code doorKeys} do original. */
    public static final DataComponentType<List<net.minecraft.core.GlobalPos>> KEYRING =
            register("keyring", builder -> builder
                    .persistent(net.minecraft.core.GlobalPos.CODEC.listOf())
                    .networkSynchronized(net.minecraft.core.GlobalPos.STREAM_CODEC
                            .apply(ByteBufCodecs.list())));

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
