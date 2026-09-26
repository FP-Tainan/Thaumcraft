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
    /** De quanto em quanto os bolsos se afastam uns dos outros na grelha. */
    public static final int STRIDE = 64;

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
     * Abre um bolso novo e devolve onde quem entra há de aparecer. A porta de volta fica no meio da parede do
     * norte e aponta para a fenda por onde se entrou.
     *
     * <p>Se a fenda que abriu este bolso era <b>brava</b> — uma das que já estavam no mundo, presa pelo
     * Firma-Fendas e tomada por uma porta —, a sala sai com um dos temas do {@link PocketThemes} e com mais duas
     * portas, uma em cada parede de lado, que ainda não apontam para lugar nenhum. Quem as atravessar abre outro
     * bolso bravo, de outro tema, e é assim que as salas se vão ligando umas às outras.
     *
     * <p>Sem fenda brava — uma porta comum, feita na bancada e assentada onde calhou — o bolso é o liso de tecido
     * preto do original, com a porta de volta e mais nada.
     */
    public static RiftBlockEntity.@Nullable Destination open(ServerLevel de, BlockPos fenda, boolean bravo,
                                                             @Nullable PocketThemes veioDe) {
        MinecraftServer server = de.getServer();
        ServerLevel bolsos = level(server);
        if (bolsos == null) return null;

        Count conta = bolsos.getDataStorage().computeIfAbsent(Count.TYPE);
        int qual = conta.next++;
        conta.setDirty();

        BlockPos canto = new BlockPos(qual * STRIDE, 32, 0);
        PocketThemes tema = bravo ? PocketThemes.roll(bolsos.getRandom(), veioDe) : null;
        carve(bolsos, canto, tema);
        if (tema != null) tema.fill(bolsos, canto, bolsos.getRandom());

        // a porta de volta, no meio da parede do norte, olhando para dentro
        BlockPos porta = canto.offset(ROOM / 2, 0, 1);
        limpa(bolsos, porta);
        door(bolsos, porta, Direction.SOUTH, de.dimension(), fenda, false, tema);

        // e, num bolso bravo, as outras duas: uma em cada parede de lado, ainda sem destino, a olhar para dentro
        if (tema != null) {
            // encostada à parede de oeste, olhando para leste; e a de leste, olhando para oeste
            BlockPos oeste = canto.offset(0, 0, ROOM / 2);
            limpa(bolsos, oeste);
            door(bolsos, oeste, Direction.EAST, null, null, true, tema);

            BlockPos leste = canto.offset(ROOM - 1, 0, ROOM / 2);
            limpa(bolsos, leste);
            door(bolsos, leste, Direction.WEST, null, null, true, tema);
        }

        // quem chega sai da porta e olha para dentro da sala, que é o que se faz ao atravessar uma porta
        return new RiftBlockEntity.Destination(bolsos.dimension(), porta.south(), 0.0f);
    }

    /** O mesmo, para uma porta comum: bolso liso e nada mais. */
    public static RiftBlockEntity.@Nullable Destination open(ServerLevel de, BlockPos fenda) {
        return open(de, fenda, false, null);
    }

    /** A sala: as paredes, o chão e o teto do tema, e o vazio no meio. Sem tema, o tecido preto do original. */
    public static void carve(ServerLevel bolsos, BlockPos canto, @Nullable PocketThemes tema) {
        BlockState parede = tema == null ? PocketThemes.plainWall() : tema.wall();
        BlockState chão = tema == null ? PocketThemes.plainFloor() : tema.floor();
        BlockState teto = tema == null ? PocketThemes.plainFloor() : tema.ceiling();
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

    public static void carve(ServerLevel bolsos, BlockPos canto) {
        carve(bolsos, canto, null);
    }

    /** Abre espaço para uma porta, que o tema pode ter enchido. */
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
                            net.minecraft.resources.@Nullable ResourceKey<net.minecraft.world.level.Level> voltaLevel,
                            @Nullable BlockPos voltaPos, boolean bravo, @Nullable PocketThemes tema) {
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
            fenda.setTheme(tema);
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
