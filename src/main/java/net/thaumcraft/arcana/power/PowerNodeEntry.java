package net.thaumcraft.arcana.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * O que a rede guarda de <b>um</b> nó: o {@code PowerNodeEntry} da 1.4.0.009.
 *
 * <p>Duas coisas. <b>Quanto</b> de cada tipo ele tem dentro, e <b>por onde</b> ele busca: uma lista de
 * caminhos por tipo, cada caminho uma fila de posições que acaba numa fonte.
 *
 * <p>O caminho guarda-se inteiro, e não só a ponta, porque é ele que se confere antes de cada pedido: se
 * um bloco do meio desapareceu, o caminho morre e no lugar dele fica um <b>Elo Partido</b>, que é como
 * quem joga descobre onde a rede se rompeu.
 */
public class PowerNodeEntry {
    private final Map<PowerType, Float> amounts = new EnumMap<>(PowerType.class);
    private final Map<PowerType, List<LinkedList<BlockPos>>> paths = new EnumMap<>(PowerType.class);

    public void clearNodePaths() {
        this.paths.values().forEach(List::clear);
    }

    /**
     * Guarda um caminho até uma fonte.
     *
     * <p>Um caminho por fonte: se já havia outro que acabava na mesma ponta, ele sai e este entra. É assim
     * que religar dois nós <b>refaz</b> a ligação em vez de a duplicar.
     */
    public void registerNodePath(PowerType type, LinkedList<BlockPos> caminho) {
        List<LinkedList<BlockPos>> lista = this.paths.computeIfAbsent(type, qual -> new ArrayList<>());
        Iterator<LinkedList<BlockPos>> it = lista.iterator();
        while (it.hasNext()) {
            if (it.next().getLast().equals(caminho.getLast())) {
                it.remove();
                break;
            }
        }
        lista.add(caminho);
    }

    /** Quantos caminhos deste tipo este nó conhece. */
    public int pathCount(PowerType type) {
        List<LinkedList<BlockPos>> lista = this.paths.get(type);
        return lista == null ? 0 : lista.size();
    }

    /**
     * O pedido: tenta trazer até {@code quanto} deste tipo, por quantos caminhos forem precisos.
     *
     * <p>Quem já está cheio não pede, e quem pediria mais do que cabe pede só o que cabe.
     */
    public float requestPower(ServerLevel level, PowerType type, float quanto, float teto) {
        if (this.getPower(type) >= teto) return 0.0f;
        List<LinkedList<BlockPos>> lista = this.paths.get(type);
        if (lista == null || lista.isEmpty()) return 0.0f;

        if (this.amounts.containsKey(type) && this.amounts.get(type) + quanto > teto) {
            quanto = teto - this.amounts.get(type);
        }

        float trazido = 0.0f;
        for (LinkedList<BlockPos> caminho : lista) {
            trazido += requestPowerFrom(level, caminho, type, quanto - trazido);
            if (trazido >= quanto) break;
        }
        return trazido;
    }

    /**
     * Confere o caminho inteiro, bloco a bloco.
     *
     * <p>Se um deles deixou de ser nó, o caminho morre — e se o lugar ficou vazio, fica lá um Elo Partido
     * a dizer onde foi.
     *
     * <p><b>Desvio declarado:</b> o original chama {@code getChunkFromBlockCoords}, que <b>carrega</b> o
     * pedaço de mundo para o conferir. Aqui pergunta-se antes se ele está carregado, e um caminho que
     * passa por pedaço descarregado apenas não serve <b>desta vez</b> — não morre e não deixa elo partido.
     * Uma rede não arranca pedaços de mundo do disco de vinte em vinte batidas.
     */
    private static boolean validatePath(ServerLevel level, LinkedList<BlockPos> caminho) {
        for (BlockPos onde : caminho) {
            if (!level.hasChunkAt(onde)) return false;
            BlockEntity bloco = level.getBlockEntity(onde);
            if (bloco instanceof PowerNode) continue;
            if (level.getBlockState(onde).isAir()) {
                level.setBlockAndUpdate(onde, net.thaumcraft.arcana.ArcanaBlocks.BROKEN_POWER_LINK.defaultBlockState());
            }
            return false;
        }
        return true;
    }

    private static float requestPowerFrom(ServerLevel level, LinkedList<BlockPos> caminho, PowerType type, float quanto) {
        if (!validatePath(level, caminho)) return 0.0f;
        BlockPos ponta = caminho.getLast();
        BlockEntity bloco = level.getBlockEntity(ponta);
        if (!(bloco instanceof PowerNode fonte) || !fonte.canProvidePower(type)) return 0.0f;
        return PowerNodes.of(level).consumePower(ponta, fonte, type, quanto);
    }

    /** De que tipo este nó tem mais. */
    public PowerType getHighestPowerType() {
        PowerType qual = null;
        float maior = 0.0f;
        for (var entrada : this.amounts.entrySet()) {
            if (entrada.getValue() > maior) {
                maior = entrada.getValue();
                qual = entrada.getKey();
            }
        }
        return qual;
    }

    /** E quanto é esse mais. */
    public float getHighestPower() {
        float maior = 0.0f;
        for (float quanto : this.amounts.values()) maior = Math.max(maior, quanto);
        return maior;
    }

    public float getPower(PowerType type) {
        return this.amounts.getOrDefault(type, 0.0f);
    }

    public void setPower(PowerType type, float quanto) {
        if (type != null) this.amounts.put(type, quanto);
    }

    public Map<PowerType, List<LinkedList<BlockPos>>> nodePaths() {
        return this.paths;
    }

    // ---------------------------------------------------------------- o que se guarda em disco

    private record Guardado(PowerType type, float amount) {
        static final Codec<Guardado> CODEC = RecordCodecBuilder.create(i -> i.group(
                PowerType.CODEC.fieldOf("type").forGetter(Guardado::type),
                Codec.FLOAT.fieldOf("amount").forGetter(Guardado::amount)
        ).apply(i, Guardado::new));
    }

    private record Caminhos(PowerType type, List<List<BlockPos>> paths) {
        static final Codec<Caminhos> CODEC = RecordCodecBuilder.create(i -> i.group(
                PowerType.CODEC.fieldOf("type").forGetter(Caminhos::type),
                BlockPos.CODEC.listOf().listOf().fieldOf("paths").forGetter(Caminhos::paths)
        ).apply(i, Caminhos::new));
    }

    public static final Codec<PowerNodeEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Guardado.CODEC.listOf().optionalFieldOf("amounts", List.of()).forGetter(PowerNodeEntry::guardados),
            Caminhos.CODEC.listOf().optionalFieldOf("paths", List.of()).forGetter(PowerNodeEntry::caminhos)
    ).apply(i, PowerNodeEntry::new));

    public PowerNodeEntry() {
    }

    private PowerNodeEntry(List<Guardado> quantos, List<Caminhos> porOnde) {
        for (Guardado qual : quantos) this.amounts.put(qual.type(), qual.amount());
        for (Caminhos qual : porOnde) {
            List<LinkedList<BlockPos>> lista = new ArrayList<>();
            for (List<BlockPos> caminho : qual.paths()) lista.add(new LinkedList<>(caminho));
            this.paths.put(qual.type(), lista);
        }
    }

    private List<Guardado> guardados() {
        List<Guardado> feito = new ArrayList<>();
        this.amounts.forEach((qual, quanto) -> feito.add(new Guardado(qual, quanto)));
        return feito;
    }

    private List<Caminhos> caminhos() {
        List<Caminhos> feito = new ArrayList<>();
        this.paths.forEach((qual, lista) -> {
            if (lista.isEmpty()) return;
            feito.add(new Caminhos(qual, lista.stream().map(caminho -> (List<BlockPos>) new ArrayList<>(caminho)).toList()));
        });
        return feito;
    }
}
