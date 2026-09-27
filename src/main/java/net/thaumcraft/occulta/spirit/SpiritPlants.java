package net.thaumcraft.occulta.spirit;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaBlocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * O que nasce sozinho no Mundo dos Espíritos: o {@code generateDreamworld} do {@code WitcheryWorldGenerator}.
 *
 * <p>O original planta <b>uma moita de Algodão Sonhador por pedaço</b>, em três de cada quatro, e é essa a única
 * maneira de o haver: ele não se semeia, encontra-se.
 *
 * <p><b>Desvio declarado:</b> lá isso corre no gerador do mundo, no momento em que o pedaço nasce. Aqui o Mundo
 * dos Espíritos usa o gerador <b>do mundo de cima</b> — é o que o faz ser o mesmo chão —, e meter uma planta
 * nesse gerador plantaria algodão também no mundo de cima. Então a moita entra <b>ao carregar o pedaço pela
 * primeira vez</b>, e o mundo guarda quais já receberam a sua. O que se vê é o mesmo: um mundo com moitas de
 * algodão espalhadas, uma por pedaço.
 */
public final class SpiritPlants {
    /** Em quantos pedaços, de quatro, nasce uma moita. */
    public static final int CHANCE = 4;
    /** E quantas plantas tem uma moita, como a flor do jogo. */
    public static final int PATCH = 12;
    /** A que distância da primeira as outras da moita nascem. */
    public static final int SPREAD = 4;

    private SpiritPlants() {
    }

    /** Os pedaços do Mundo dos Espíritos que já receberam o que tinham a receber. */
    public static final class Sown extends SavedData {
        private final Set<Long> chunks = new HashSet<>();

        private static final com.mojang.serialization.Codec<Sown> CODEC =
                com.mojang.serialization.Codec.LONG.listOf().fieldOf("chunks").codec()
                        .xmap(Sown::of, semeados -> List.copyOf(semeados.chunks));

        public static final SavedDataType<Sown> TYPE = new SavedDataType<>(
                Thaumcraft.id("spirit_plants"), Sown::new, CODEC,
                net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private Sown() {
        }

        private static Sown of(List<Long> quais) {
            Sown feito = new Sown();
            feito.chunks.addAll(quais);
            return feito;
        }

        /** Marca aquele pedaço; devolve se ele ainda não estava marcado. */
        public boolean mark(ChunkPos onde) {
            if (!this.chunks.add(onde.pack())) return false;
            this.setDirty();
            return true;
        }

        public boolean has(ChunkPos onde) {
            return this.chunks.contains(onde.pack());
        }
    }

    public static void init() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, novo) -> {
            if (!SpiritWorld.is(level)) return;
            sow(level, chunk);
        });
    }

    /** Planta a moita daquele pedaço, se ele ainda não tiver a sua. */
    public static void sow(ServerLevel level, LevelChunk chunk) {
        Sown semeados = level.getDataStorage().computeIfAbsent(Sown.TYPE);
        if (!semeados.mark(chunk.getPos())) return;

        RandomSource sorte = RandomSource.create(chunk.getPos().pack());
        if (sorte.nextInt(CHANCE) == 0) return;

        patch(chunk, sorte);
    }

    /**
     * Uma moita de algodão, como a {@code WorldGenFlowers} do jogo antigo.
     *
     * <p><b>Ela não sai do pedaço, e não fala com o mundo.</b> Isto não é feitio: é obrigação. A moita entra
     * <b>durante o carregamento do pedaço</b>, e ali o pedaço ainda não entrou na lista do mundo — quem lhe pedir
     * um bloco pelo mundo fica à espera de si mesmo, e o servidor para. Pedir uma casa do pedaço ao lado é pior
     * ainda: faz o jogo gerá-lo na hora, de dentro do carregamento do primeiro, e esse dispara o seguinte. Por
     * isso tudo aqui — a altura, o que está lá e o que se põe — passa pelo <b>pedaço</b>, e nunca pelo mundo.
     */
    public static int patch(LevelChunk chunk, RandomSource sorte) {
        var algodão = OccultaBlocks.WISPY_COTTON.defaultBlockState();
        int meioX = SPREAD + sorte.nextInt(16 - SPREAD * 2);
        int meioZ = SPREAD + sorte.nextInt(16 - SPREAD * 2);
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();

        int postas = 0;
        for (int i = 0; i < PATCH; i++) {
            int dx = meioX + sorte.nextInt(SPREAD * 2 + 1) - SPREAD;
            int dz = meioZ + sorte.nextInt(SPREAD * 2 + 1) - SPREAD;
            int x = baseX + Math.clamp(dx, 0, 15);
            int z = baseZ + Math.clamp(dz, 0, 15);
            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
            BlockPos onde = new BlockPos(x, y, z);
            if (!chunk.getBlockState(onde).isAir()) continue;
            if (!DreamPlantBlock.ground(chunk.getBlockState(onde.below()))) continue;
            chunk.setBlockState(onde, algodão, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            chunk.markUnsaved();
            postas++;
        }
        return postas;
    }
}
