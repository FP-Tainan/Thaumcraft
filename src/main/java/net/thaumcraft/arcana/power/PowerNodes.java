package net.thaumcraft.arcana.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A <b>rede de Etherium</b>: o {@code PowerNodeRegistry} da 1.4.0.009.
 *
 * <p>Ela não é uma rede de canos. Não há fio nenhum entre dois nós: há um <b>caminho guardado</b>, que
 * alguém desenhou uma vez com a Chave de Cristal e que a rede refaz a pé, de nó em nó, cada vez que
 * precisa. Um nó só vê os vizinhos a <b>dez casas</b>, e é saltando de vizinho em vizinho que a energia
 * atravessa uma sala inteira.
 *
 * <p>Quem pede é quem puxa. De <b>vinte em vinte batidas</b> cada máquina percorre os caminhos que tem
 * guardados e tira das fontes o que couber nela. A fonte não empurra para lado nenhum.
 *
 * <p><b>Desvio declarado — onde isto se guarda.</b> O original guarda os nós <b>no pedaço de mundo</b>, no
 * NBT do chunk, e esquece-os quando o pedaço descarrega. Aqui guarda-se tudo num {@code SavedData} do
 * nível: é o lugar que o jogo de hoje dá a este tipo de coisa, e não há no Fabric um sítio onde pendurar
 * dados no NBT de um pedaço. A diferença que se vê é nenhuma — a conferência de caminho já pergunta se o
 * pedaço está carregado antes de olhar para o que lá está.
 */
public class PowerNodes extends SavedData {
    /** A que distância um nó enxerga o vizinho seguinte: <b>dez casas</b>. */
    public static final int SEARCH_RADIUS = 10;
    /** A mesma, ao quadrado, que é como o original a compara. */
    public static final double SEARCH_RADIUS_SQ = 100.0;
    /** E a distância máxima entre as duas pontas de um par: <b>cem casas</b> ({@code 10000} ao quadrado). */
    public static final double MAX_PAIR_DISTANCE_SQ = 10000.0;

    private final Map<BlockPos, PowerNodeEntry> nodes = new HashMap<>();

    private record Guardado(BlockPos at, PowerNodeEntry data) {
        static final Codec<Guardado> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("at").forGetter(Guardado::at),
                PowerNodeEntry.CODEC.fieldOf("data").forGetter(Guardado::data)
        ).apply(i, Guardado::new));
    }

    private static final Codec<PowerNodes> CODEC = Guardado.CODEC.listOf()
            .fieldOf("nodes").codec().xmap(PowerNodes::of, PowerNodes::guardados);

    public static final SavedDataType<PowerNodes> TYPE = new SavedDataType<>(
            Thaumcraft.id("arcana_power"), PowerNodes::new, CODEC,
            net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private PowerNodes() {
    }

    private static PowerNodes of(List<Guardado> quais) {
        PowerNodes feito = new PowerNodes();
        for (Guardado qual : quais) feito.nodes.put(qual.at(), qual.data());
        return feito;
    }

    private List<Guardado> guardados() {
        List<Guardado> feito = new ArrayList<>();
        this.nodes.forEach((onde, dados) -> feito.add(new Guardado(onde, dados)));
        return feito;
    }

    /** A rede daquele nível. O {@code PowerNodeRegistry.For(world)} do original. */
    public static PowerNodes of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /** Se o nível for de cliente não há rede: quem decide quanta energia há é sempre o servidor. */
    public static PowerNodes of(Level level) {
        return level instanceof ServerLevel servidor ? of(servidor) : null;
    }

    // ---------------------------------------------------------------- entrar e sair da rede

    public PowerNodeEntry entry(BlockPos onde) {
        return this.nodes.computeIfAbsent(onde.immutable(), qual -> {
            this.setDirty();
            return new PowerNodeEntry();
        });
    }

    public void register(BlockPos onde) {
        this.entry(onde);
    }

    public void remove(BlockPos onde) {
        if (this.nodes.remove(onde.immutable()) != null) this.setDirty();
    }

    public boolean knows(BlockPos onde) {
        return this.nodes.containsKey(onde.immutable());
    }

    public int size() {
        return this.nodes.size();
    }

    // ---------------------------------------------------------------- ligar e desligar

    /**
     * O <b>emparelhar</b> da Chave de Cristal: desenha, para cada tipo que a fonte dá e o destino usa, um
     * caminho a pé de um ao outro.
     *
     * <p>Devolve o que o original diz a quem está com a chave na mão — e as razões para não dar são as
     * dele: ligar um nó a si mesmo, não haver tipo em comum, estarem a mais de cem casas, ou não haver por
     * onde ir.
     */
    public Component tryPairNodes(ServerLevel level, BlockPos deOnde, PowerNode fonte, BlockPos paraOnde, PowerNode destino) {
        if (deOnde.equals(paraOnde)) return Component.translatable("thaumcraft.power.pairToSelf");

        List<PowerType> comuns = new ArrayList<>();
        for (PowerType qual : destino.validPowerTypes()) {
            if (fonte.canProvidePower(qual)) comuns.add(qual);
        }
        if (comuns.isEmpty()) return Component.translatable("thaumcraft.power.noSupportedTypes");
        if (deOnde.distSqr(paraOnde) > MAX_PAIR_DISTANCE_SQ) return Component.translatable("thaumcraft.power.tooFar");

        int conseguidos = 0;
        for (PowerType qual : comuns) {
            List<BlockPos> achado = new PowerPathfinder(level, paraOnde, qual).compute(deOnde);
            if (achado == null) continue;
            // o original junta o caminho pela frente, de modo que a fonte fica na ponta
            LinkedList<BlockPos> caminho = new LinkedList<>();
            for (BlockPos passo : achado) caminho.addFirst(passo);
            this.entry(paraOnde).registerNodePath(qual, caminho);
            conseguidos++;
        }
        this.setDirty();

        if (conseguidos == 0) return Component.translatable("thaumcraft.power.noPathFound");
        return conseguidos == comuns.size()
                ? Component.translatable("thaumcraft.power.paired")
                : Component.translatable("thaumcraft.power.partlyPaired");
    }

    public void tryDisconnectAllNodes(BlockPos onde) {
        this.entry(onde).clearNodePaths();
        this.setDirty();
    }

    // ---------------------------------------------------------------- a energia em si

    public float requestPower(ServerLevel level, BlockPos onde, PowerNode quem, PowerType type, float quanto) {
        return this.entry(onde).requestPower(level, type, quanto, quem.capacity());
    }

    public float consumePower(BlockPos onde, PowerNode quem, PowerType type, float quanto) {
        PowerNodeEntry dados = this.entry(onde);
        float tem = dados.getPower(type);
        if (tem < quanto) quanto = tem;
        dados.setPower(type, tem - quanto);
        this.setDirty();
        return quanto;
    }

    public float insertPower(BlockPos onde, PowerNode quem, PowerType type, float quanto) {
        PowerNodeEntry dados = this.entry(onde);
        float tem = dados.getPower(type);
        if (tem + quanto > quem.capacity()) quanto = quem.capacity() - tem;
        dados.setPower(type, tem + quanto);
        this.setDirty();
        return quanto;
    }

    public void setPower(BlockPos onde, PowerNode quem, PowerType type, float quanto) {
        if (quanto > quem.capacity()) quanto = quem.capacity();
        this.entry(onde).setPower(type, quanto);
        this.setDirty();
    }

    public boolean checkPower(BlockPos onde, PowerType type, float quanto) {
        return this.entry(onde).getPower(type) >= quanto;
    }

    /** Se <b>algum</b> dos três chega àquela quantidade. */
    public boolean checkPower(BlockPos onde, float quanto) {
        for (PowerType qual : PowerType.ALL) {
            if (this.checkPower(onde, qual, quanto)) return true;
        }
        return false;
    }

    public boolean checkPower(BlockPos onde) {
        return this.getHighestPower(onde) > 0.0f;
    }

    public float getPower(BlockPos onde, PowerType type) {
        return this.entry(onde).getPower(type);
    }

    public float getHighestPower(BlockPos onde) {
        return this.entry(onde).getHighestPower();
    }

    public PowerType getHighestPowerType(BlockPos onde) {
        return this.entry(onde).getHighestPowerType();
    }

    // ---------------------------------------------------------------- quem está perto

    /**
     * Os nós a <b>dez casas</b> deste, e vivos.
     *
     * <p>Quem a rede conhece mas já não é nó é <b>esquecido aqui</b>: é esta a limpeza que o original faz,
     * e é por isso que arrancar uma máquina não deixa lixo na rede para sempre.
     */
    public List<BlockPos> nearbyNodes(ServerLevel level, BlockPos onde) {
        List<BlockPos> vivos = new ArrayList<>();
        List<BlockPos> mortos = new ArrayList<>();
        for (BlockPos outro : this.nodes.keySet()) {
            if (outro.equals(onde) || outro.distSqr(onde) > SEARCH_RADIUS_SQ) continue;
            if (!level.hasChunkAt(outro)) continue;
            BlockEntity bloco = level.getBlockEntity(outro);
            if (bloco instanceof PowerNode) vivos.add(outro);
            else mortos.add(outro);
        }
        if (!mortos.isEmpty()) {
            mortos.forEach(this.nodes::remove);
            this.setDirty();
        }
        return vivos;
    }
}
