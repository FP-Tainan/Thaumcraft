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
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
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
                BuiltinDimensionTypes.OVERWORLD, gerador);
    }

    /**
     * Abre um bolso novo e devolve onde quem entra há de aparecer. A porta de volta fica na parede do fundo e
     * aponta para a fenda por onde se entrou.
     */
    public static RiftBlockEntity.@Nullable Destination open(ServerLevel de, BlockPos fenda) {
        MinecraftServer server = de.getServer();
        ServerLevel bolsos = level(server);
        if (bolsos == null) return null;

        Count conta = bolsos.getDataStorage().computeIfAbsent(Count.TYPE);
        int qual = conta.next++;
        conta.setDirty();

        BlockPos canto = new BlockPos(qual * STRIDE, 32, 0);
        carve(bolsos, canto);

        // a porta de volta, no meio da parede do norte, olhando para dentro
        BlockPos porta = canto.offset(ROOM / 2, 1, 1);
        door(bolsos, porta, Direction.SOUTH, de.dimension(), fenda);

        return new RiftBlockEntity.Destination(bolsos.dimension(), porta.south(), 180.0f);
    }

    /** A sala: as paredes de tecido antigo, o chão e o teto de tecido comum, e o vazio no meio. */
    public static void carve(ServerLevel bolsos, BlockPos canto) {
        BlockState parede = FabricBlocks.ANCIENT.get(DyeColor.BLACK).defaultBlockState();
        BlockState chão = FabricBlocks.FABRIC.get(DyeColor.BLACK).defaultBlockState();
        for (int x = -1; x <= ROOM; x++) {
            for (int z = -1; z <= ROOM; z++) {
                for (int y = -1; y <= HEIGHT; y++) {
                    BlockPos onde = canto.offset(x, y, z);
                    boolean borda = x < 0 || x >= ROOM || z < 0 || z >= ROOM;
                    boolean cima = y < 0 || y >= HEIGHT;
                    if (borda) bolsos.setBlock(onde, parede, 2);
                    else if (cima) bolsos.setBlock(onde, chão, 2);
                    else bolsos.setBlock(onde, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    /** Põe uma porta dimensional de madeira de duas metades, com a fenda dela já apontada. */
    public static void door(ServerLevel onde, BlockPos baixo, Direction olhando,
                            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> voltaLevel,
                            BlockPos voltaPos) {
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
            fenda.setDestination(new RiftBlockEntity.Destination(voltaLevel, voltaPos.above(), olhando.getOpposite().toYRot()));
        }
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
