package net.thaumcraft.shattered;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * As salas das Portas Dimensionais, as de verdade.
 *
 * <p>O original é um mod de quebra-cabeças: as salas para lá de uma porta não são quartos lisos, são cento e
 * dezasseis construções à mão — salões, armadilhas, poços, labirintos, minas — cada uma com as portas dela já
 * postas nas paredes, e é por essas portas que se vai para a seguinte. É isso que faz o mod ser o que é, e é isso
 * que quem manda mandou vir para cá.
 *
 * <p>Lá as salas vêm em esquemas {@code .schem} da 1.12, com nomes de bloco que já não existem
 * ({@code minecraft:stonebrick[variant=cracked_stonebrick]}). O {@code scratchpad/dd-salas.js} lê-os, passa cada
 * um dos trezentos e vinte e oito estados pelo {@code dd-mapa.js} — que é a planificação da 1.13 feita à mão para
 * os noventa e seis blocos que estas salas usam — e escreve um arquivo por sala com o que o jogo de hoje entende.
 * As portas do mod viram as nossas, e os tecidos também.
 *
 * <p>O formato é o mais simples que serve: cabeçalho, paleta de estados em texto, e o corpo em pares de
 * <i>quantas casas seguidas, qual entrada da paleta</i>. Estas salas são quase todas ar, e ar seguido comprime-se
 * a nada: os oito milhões de casas das cento e dezasseis salas cabem em trezentos e poucos quilobytes.
 */
public final class DungeonRooms {
    /** Onde moram, e o índice que as lista. */
    private static final String PASTA = "dungeons/";
    private static final Identifier ÍNDICE = Thaumcraft.id(PASTA + "salas.txt");

    /** A marca que abre o arquivo, e a versão do formato. */
    private static final int MAGIC = 0x54434452; // "TCDR"
    private static final int VERSION = 1;

    /** Uma sala lida: a caixa dela, a paleta, e o corpo em pares de repetição. */
    public record Room(String name, int width, int height, int length, BlockState[] palette,
                       int[] counts, int[] states) {
        public int blocks() {
            return this.width * this.height * this.length;
        }
    }

    private static @Nullable List<String> nomes;
    private static final Map<String, Room> LIDAS = new ConcurrentHashMap<>();

    private DungeonRooms() {
    }

    /** Os nomes de todas as salas, na ordem do índice. */
    public static List<String> names(MinecraftServer server) {
        if (nomes != null) return nomes;
        List<String> achados = new ArrayList<>();
        try (InputStream entrada = server.getResourceManager().open(ÍNDICE);
             BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String nome = linha.trim();
                if (!nome.isEmpty()) achados.add(nome);
            }
        } catch (IOException erro) {
            Thaumcraft.LOGGER.error("não consegui ler o índice das salas em {}", ÍNDICE, erro);
        }
        nomes = List.copyOf(achados);
        return nomes;
    }

    /** Uma sala qualquer, que não seja aquela de onde se veio. */
    public static @Nullable String roll(MinecraftServer server, RandomSource sorte, @Nullable String anterior) {
        List<String> todas = names(server);
        if (todas.isEmpty()) return null;
        if (todas.size() == 1 || anterior == null) return todas.get(sorte.nextInt(todas.size()));
        String escolhida;
        do {
            escolhida = todas.get(sorte.nextInt(todas.size()));
        } while (escolhida.equals(anterior));
        return escolhida;
    }

    /** A sala de nome tal, lida uma vez e guardada. */
    public static @Nullable Room read(MinecraftServer server, String nome) {
        Room guardada = LIDAS.get(nome);
        if (guardada != null) return guardada;
        Identifier onde = Thaumcraft.id(PASTA + nome + ".room");
        try (InputStream cru = server.getResourceManager().open(onde);
             DataInputStream dados = new DataInputStream(new GZIPInputStream(cru))) {
            if (dados.readInt() != MAGIC) throw new IOException("não é uma sala");
            int versão = dados.readUnsignedByte();
            if (versão != VERSION) throw new IOException("versão " + versão + ", esperava " + VERSION);

            int largura = dados.readInt(), altura = dados.readInt(), comprimento = dados.readInt();
            int quantas = dados.readUnsignedShort();
            HolderLookup.Provider registos = server.registryAccess();
            var blocos = registos.lookupOrThrow(Registries.BLOCK);
            BlockState[] paleta = new BlockState[quantas];
            for (int i = 0; i < quantas; i++) paleta[i] = estado(blocos, dados.readUTF());

            int pares = dados.readInt();
            int[] contas = new int[pares];
            int[] quais = new int[pares];
            for (int i = 0; i < pares; i++) {
                contas[i] = dados.readInt();
                quais[i] = dados.readUnsignedShort();
            }
            Room sala = new Room(nome, largura, altura, comprimento, paleta, contas, quais);
            LIDAS.put(nome, sala);
            return sala;
        } catch (IOException erro) {
            Thaumcraft.LOGGER.error("não consegui ler a sala {}", onde, erro);
            return null;
        }
    }

    /** Um estado de bloco a partir do texto dele; o que não se entender vira ar, e fica dito no registo. */
    private static BlockState estado(HolderLookup<net.minecraft.world.level.block.Block> blocos, String texto) {
        try {
            return net.minecraft.commands.arguments.blocks.BlockStateParser
                    .parseForBlock(blocos, texto, false).blockState();
        } catch (CommandSyntaxException erro) {
            Thaumcraft.LOGGER.warn("estado de bloco que não entendi numa sala: {}", texto);
            return Blocks.AIR.defaultBlockState();
        }
    }

    /**
     * Põe a sala no mundo, com o canto de menor coordenada naquela casa.
     *
     * <p>Devolve onde ficaram as metades de baixo das portas dimensionais que a sala traz — são as saídas dela, e
     * é quem chama que decide qual é a de volta e quais são as que ainda não levam a lado nenhum.
     */
    public static List<BlockPos> place(ServerLevel level, BlockPos canto, Room sala) {
        return place(level, canto, sala, true);
    }

    /**
     * O mesmo, dizendo se o ar se pode saltar.
     *
     * <p>Num bolso acabado de abrir o mundo já é vazio, e mais de dois terços do que uma sala tem é ar: saltá-lo
     * poupa a maior parte do trabalho. A maior das salas do original tem seiscentas mil casas, e pô-las uma a uma
     * enquanto alguém atravessa uma porta faz-se sentir.
     */
    public static List<BlockPos> place(ServerLevel level, BlockPos canto, Room sala, boolean pulaAr) {
        List<BlockPos> portas = new ArrayList<>();
        int i = 0;
        for (int par = 0; par < sala.counts().length; par++) {
            BlockState estado = sala.palette()[sala.states()[par]];
            int quantas = sala.counts()[par];
            if (pulaAr && estado.isAir()) {
                i += quantas;
                continue;
            }
            boolean éPorta = estado.getBlock() instanceof DimensionalDoorBlock
                    && estado.getValue(net.minecraft.world.level.block.DoorBlock.HALF)
                    == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER;
            for (int passo = 0; passo < quantas; passo++, i++) {
                // a ordem do esquema: o x anda primeiro, depois o z, e o y é o que anda mais devagar
                int x = i % sala.width();
                int z = (i / sala.width()) % sala.length();
                int y = i / (sala.width() * sala.length());
                BlockPos onde = canto.offset(x, y, z);
                // sem avisar os vizinhos: uma sala inteira a avisar-se a si mesma bloco a bloco custa caro, e o
                // que ela quer é ficar exatamente como foi desenhada
                level.setBlock(onde, estado, 2);
                if (éPorta) portas.add(onde);
            }
        }
        return portas;
    }

    /** Esquece o que leu — serve aos testes e a quem troque o pacote de dados. */
    public static void forget() {
        nomes = null;
        LIDAS.clear();
    }

    /** Quantas salas há, para quem quiser contar. */
    public static int count(MinecraftServer server) {
        return names(server).size();
    }
}
