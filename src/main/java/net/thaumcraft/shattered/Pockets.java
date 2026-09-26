package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.thaumcraft.world.DynamicDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Os bolsos: o {@code PocketGenerator} e o {@code PocketRegistry} das Portas Dimensionais.
 *
 * <p>Como no original, os bolsos não são um mundo cada: moram todos no mesmo mundo, lado a lado numa grelha, e o
 * que se abre com o jogo andando é o mundo que os guarda. Cada bolso é uma sala forrada de tecido, com uma porta
 * de volta na parede que aponta para a fenda por onde se entrou.
 *
 * <p><b>Do original fica de fora, por enquanto</b>, o leitor de {@code .schem}: lá as salas vêm de esquemas
 * guardados no jar, com ruínas, prisões e bibliotecas. Aqui a sala é lisa, feita em código. O leitor de esquemas é
 * fatia à parte, e quando chegar troca-se só o que enche a sala.
 */
public final class Pockets {
    /** O lado de dentro de um bolso, em blocos — o {@code size 0} do original. */
    public static final int ROOM = 15;
    /** A altura do lado de dentro. */
    public static final int HEIGHT = 8;
    /**
     * De quanto em quanto os bolsos se afastam uns dos outros na grelha.
     *
     * <p>Eram sessenta e quatro, que chegava para o quarto liso. As salas do original vão até noventa e sete de
     * lado, e por isso passam a duzentos e cinquenta e seis — num mundo que é só vazio, o espaço não custa nada.
     */
    public static final int STRIDE = 256;

    private Pockets() {
    }

    /** A conta de quantos bolsos já se abriram, guardada com o mundo. */
    public static final class Count extends SavedData {
        public int next;

        private static final com.mojang.serialization.Codec<Count> CODEC =
                com.mojang.serialization.Codec.INT.fieldOf("next").codec().xmap(Count::new, conta -> conta.next);

        public static final SavedDataType<Count> TYPE = new SavedDataType<>(
                net.thaumcraft.Thaumcraft.id("pockets"), Count::new, CODEC,
                net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private Count() {
        }

        private Count(int next) {
            this.next = next;
        }
    }

    /** O mundo dos bolsos, abrindo-o se ainda não houver. */
    public static @Nullable ServerLevel level(MinecraftServer server) {
        var registos = server.registryAccess();
        var gerador = new FlatLevelSource(new FlatLevelGeneratorSettings(
                Optional.empty(),
                registos.lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.THE_VOID),
                List.of()).withBiomeAndLayers(List.of(new FlatLayerInfo(1, Blocks.AIR)), Optional.empty(),
                registos.lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.THE_VOID)));
        return DynamicDimensions.getOrCreate(server, ShatteredRealms.PUBLIC_POCKETS,
                ShatteredRealms.POCKET_TYPE, gerador);
    }

    /**
     * Abre um bolso novo e devolve onde quem entra há de aparecer.
     *
     * <p>Se a fenda que abriu este bolso era <b>brava</b> — uma das que já estavam no mundo, presa pelo
     * Firma-Fendas e tomada por uma porta —, o que se abre é uma das {@linkplain DungeonRooms salas do original}:
     * um salão, uma armadilha, um poço, um labirinto, com as portas dele já postas nas paredes. Uma dessas portas
     * passa a ser a de volta; as outras ficam por apontar, e quem as atravessar abre outra sala. É assim que o
     * quebra-cabeças se vai ligando.
     *
     * <p>Sem fenda brava — uma porta comum, feita na bancada e assentada onde calhou — o bolso é o quarto liso de
     * tecido preto do original, com a porta de volta e mais nada.
     */
    public static RiftBlockEntity.@Nullable Destination open(ServerLevel de, BlockPos fenda, boolean bravo,
                                                             @Nullable String veioDe) {
        MinecraftServer server = de.getServer();
        ServerLevel bolsos = level(server);
        if (bolsos == null) return null;

        Count conta = bolsos.getDataStorage().computeIfAbsent(Count.TYPE);
        int qual = conta.next++;
        conta.setDirty();

        BlockPos canto = new BlockPos(qual * STRIDE, 32, 0);
        if (bravo) {
            var feito = dungeon(bolsos, canto, de, fenda, veioDe);
            if (feito != null) return feito;
            // sem salas para dar, cai-se no quarto liso, que é melhor do que não abrir nada
        }

        carve(bolsos, canto);

        // a porta de volta, no meio da parede do norte, olhando para dentro
        BlockPos porta = canto.offset(ROOM / 2, 0, 1);
        limpa(bolsos, porta);
        door(bolsos, porta, Direction.SOUTH, de.dimension(), fenda, false, null);

        // quem chega sai da porta e olha para dentro da sala, que é o que se faz ao atravessar uma porta
        return new RiftBlockEntity.Destination(bolsos.dimension(), porta.south(), 0.0f);
    }

    /** O mesmo, para uma porta comum: bolso liso e nada mais. */
    public static RiftBlockEntity.@Nullable Destination open(ServerLevel de, BlockPos fenda) {
        return open(de, fenda, false, null);
    }

    /**
     * Põe uma das salas do original naquele canto e liga-lhe as portas.
     *
     * <p>A sala vem com as portas dela desenhadas nas paredes. A <b>primeira</b> passa a ser a de volta — aponta
     * para a fenda de onde se veio, e é à frente dela que quem chega aparece. As outras ficam por apontar e
     * bravas: quem as atravessar abre outra sala, e é assim que o quebra-cabeças se vai ligando.
     *
     * <p>Se a sala não tiver porta nenhuma — há-as assim no original, que são becos —, abre-se uma na parede do
     * norte para não se ficar lá fechado.
     */
    private static RiftBlockEntity.@Nullable Destination dungeon(ServerLevel bolsos, BlockPos canto, ServerLevel de,
                                                                 BlockPos fenda, @Nullable String veioDe) {
        MinecraftServer server = bolsos.getServer();
        String nome = DungeonRooms.roll(server, bolsos.getRandom(), veioDe);
        if (nome == null) return null;
        DungeonRooms.Room sala = DungeonRooms.read(server, nome);
        if (sala == null) return null;

        List<BlockPos> portas = DungeonRooms.place(bolsos, canto, sala);
        if (portas.isEmpty()) {
            // um beco: abre-se uma saída no meio da parede do norte, à altura do chão da sala
            BlockPos saída = canto.offset(sala.width() / 2, 1, 1);
            limpa(bolsos, saída);
            door(bolsos, saída, Direction.SOUTH, de.dimension(), fenda, false, nome);
            return new RiftBlockEntity.Destination(bolsos.dimension(), saída.south(), 0.0f);
        }

        BlockPos volta = portas.getFirst();
        Direction olhar = bolsos.getBlockState(volta).getValue(DoorBlock.FACING);
        for (BlockPos porta : portas) {
            if (!(bolsos.getBlockEntity(porta) instanceof RiftBlockEntity miolo)) continue;
            miolo.setNatural(false);
            miolo.setRoom(nome);
            if (porta.equals(volta)) {
                miolo.setWild(false);
                miolo.setDestination(new RiftBlockEntity.Destination(
                        de.dimension(), fenda.above(), olhar.getOpposite().toYRot()));
            } else {
                miolo.setWild(true);
            }
        }

        // e quem lá mora: os que atravessam as fendas há mais tempo do que nós
        RiftWalkers.populate(bolsos, canto, sala.width(), sala.length());

        // quem chega sai da porta de volta e olha para dentro da sala
        return new RiftBlockEntity.Destination(bolsos.dimension(), volta.relative(olhar), olhar.toYRot());
    }

    /** O quarto liso do original: paredes de tecido antigo, chão e teto de tecido comum, e o vazio no meio. */
    public static void carve(ServerLevel bolsos, BlockPos canto) {
        BlockState parede = FabricBlocks.ANCIENT.get(net.minecraft.world.item.DyeColor.BLACK).defaultBlockState();
        BlockState chão = FabricBlocks.FABRIC.get(net.minecraft.world.item.DyeColor.BLACK).defaultBlockState();
        BlockState teto = chão;
        for (int x = -1; x <= ROOM; x++) {
            for (int z = -1; z <= ROOM; z++) {
                for (int y = -1; y <= HEIGHT; y++) {
                    BlockPos onde = canto.offset(x, y, z);
                    boolean borda = x < 0 || x >= ROOM || z < 0 || z >= ROOM;
                    boolean cima = y >= HEIGHT;
                    boolean baixo = y < 0;
                    if (borda) bolsos.setBlock(onde, parede, 2);
                    else if (baixo) bolsos.setBlock(onde, chão, 2);
                    else if (cima) bolsos.setBlock(onde, teto, 2);
                    else bolsos.setBlock(onde, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    /** Abre espaço para uma porta. */
    private static void limpa(ServerLevel bolsos, BlockPos baixo) {
        bolsos.setBlock(baixo, Blocks.AIR.defaultBlockState(), 2);
        bolsos.setBlock(baixo.above(), Blocks.AIR.defaultBlockState(), 2);
    }

    /**
     * Põe uma porta dimensional de madeira de duas metades.
     *
     * <p>Com um lugar de volta, ela já sai apontada para lá; sem ele, fica por apontar, e a primeira travessia é
     * que lhe abre o bolso do outro lado. As portas que a sala traz de nascença estão sempre à vista — não são
     * fendas do mundo à espera de quem as veja, são o caminho por onde se há de andar.
     */
    public static void door(ServerLevel onde, BlockPos baixo, Direction olhando,
                            @Nullable net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> voltaLevel,
                            @Nullable BlockPos voltaPos, boolean bravo, @Nullable String sala) {
        DoorBlock bloco = (DoorBlock) ShatteredBlocks.OAK_DIMENSIONAL_DOOR;
        BlockState debaixo = bloco.defaultBlockState()
                .setValue(DoorBlock.FACING, olhando)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false);
        onde.setBlock(baixo, debaixo, 3);
        onde.setBlock(baixo.above(), debaixo.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);

        if (onde.getBlockEntity(baixo) instanceof RiftBlockEntity fenda) {
            fenda.setNatural(false);
            fenda.setWild(bravo);
            fenda.setRoom(sala);
            if (voltaLevel != null && voltaPos != null) {
                fenda.setDestination(new RiftBlockEntity.Destination(
                        voltaLevel, voltaPos.above(), olhando.getOpposite().toYRot()));
            }
        }
    }

    /** O mesmo, para quem só quer a porta de volta de um bolso liso. */
    public static void door(ServerLevel onde, BlockPos baixo, Direction olhando,
                            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> voltaLevel,
                            BlockPos voltaPos) {
        door(onde, baixo, olhando, voltaLevel, voltaPos, false, null);
    }

    /** Quantos bolsos já se abriram naquele mundo. */
    public static int count(ServerLevel bolsos) {
        Count conta = bolsos.getDataStorage().get(Count.TYPE);
        return conta == null ? 0 : conta.next;
    }

    /** O feitio cru da conta, para os testes. */
    static CompoundTag debug(ServerLevel bolsos) {
        return new CompoundTag();
    }
}
