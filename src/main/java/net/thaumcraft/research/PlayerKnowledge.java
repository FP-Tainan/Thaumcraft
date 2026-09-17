package net.thaumcraft.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * O que um jogador sabe: que aspectos já descobriu, quantos pontos tem de cada um, o que já examinou e que
 * pesquisas concluiu.
 *
 * <p>Porte do {@code PlayerKnowledge} da 4.2.3.5. As contas de ganhar ponto são as do original: descobrir um
 * aspecto pela primeira vez rende dois a mais, e quem já tem muito de um aspecto passa a ganhar cada vez
 * menos dele.
 */
public final class PlayerKnowledge {
    /** A partir daqui o ganho encolhe; é o {@code aspect_total_cap} do mod original. */
    public static final int ASPECT_CAP = 100;

    private final Set<String> discovered = new LinkedHashSet<>();
    private final AspectList pool = new AspectList();
    private final Set<String> scanned = new LinkedHashSet<>();
    private final Set<String> research = new LinkedHashSet<>();

    public PlayerKnowledge() {
    }

    /**
     * Uma cópia deste caderno.
     *
     * <p>Serve para o anexo do jogo enxergar que alguma coisa mudou: guardando de volta o mesmo objeto,
     * ele não vê mudança nenhuma e não manda nada para a máquina de quem joga.
     */
    public PlayerKnowledge copy() {
        PlayerKnowledge copia = new PlayerKnowledge();
        copia.discovered.addAll(this.discovered);
        copia.pool.add(this.pool);
        copia.scanned.addAll(this.scanned);
        copia.research.addAll(this.research);
        return copia;
    }

    private PlayerKnowledge(List<String> discovered, AspectList pool, List<String> scanned, List<String> research) {
        this.discovered.addAll(discovered);
        this.pool.add(pool);
        this.scanned.addAll(scanned);
        this.research.addAll(research);
    }

    // ------------------------------------------------------------ aspectos descobertos

    public boolean hasDiscovered(Aspect aspect) {
        return aspect != null && this.discovered.contains(aspect.tag());
    }

    /** Descobriu os dois de que aquele aspecto é feito? É o que decide se dá para examinar a coisa. */
    public boolean hasDiscoveredParents(Aspect aspect) {
        if (aspect == null) return false;
        if (aspect.isPrimal()) return true;
        for (Aspect parent : aspect.components()) {
            if (!hasDiscovered(parent)) return false;
        }
        return true;
    }

    /** Anota o aspecto como descoberto. Devolve se era novidade. */
    public boolean discover(Aspect aspect) {
        return aspect != null && this.discovered.add(aspect.tag());
    }

    public List<Aspect> discovered() {
        List<Aspect> list = new ArrayList<>();
        for (String tag : this.discovered) {
            Aspect aspect = Aspect.of(tag);
            if (aspect != null) list.add(aspect);
        }
        return list;
    }

    // ------------------------------------------------------------ pontos de pesquisa

    public int points(Aspect aspect) {
        return this.pool.getAmount(aspect);
    }

    public AspectList pool() {
        return this.pool;
    }

    /**
     * Dá pontos daquele aspecto, com as contas do original: quem passa do limite ganha só a raiz do que
     * ganharia, e quem passa de um quarto acima disso ganha um só. Devolve quanto entrou de fato.
     */
    public int award(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return 0;
        // descobrir de primeira vale dois a mais
        if (discover(aspect)) amount += 2;
        int have = points(aspect);
        if (have >= ASPECT_CAP) amount = (int) Math.sqrt(Math.max(0, amount));
        if (amount > 1 && have >= ASPECT_CAP * 1.25f) amount = 1;
        if (amount <= 0) return 0;
        this.pool.add(aspect, amount);
        return amount;
    }

    public boolean spend(Aspect aspect, int amount) {
        return this.pool.reduce(aspect, amount);
    }

    // ------------------------------------------------------------ o que já foi examinado

    public boolean hasScanned(String key) {
        return this.scanned.contains(key);
    }

    /** Anota que já examinou aquilo. Devolve se era a primeira vez. */
    public boolean markScanned(String key) {
        return this.scanned.add(key);
    }

    public int scannedCount() {
        return this.scanned.size();
    }

    // ------------------------------------------------------------ pesquisas

    public boolean hasResearch(String key) {
        return this.research.contains(key);
    }

    public boolean completeResearch(String key) {
        return this.research.add(key);
    }

    public List<String> research() {
        return new ArrayList<>(this.research);
    }

    // ------------------------------------------------------------ guardar e mandar

    public static final Codec<PlayerKnowledge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("discovered", List.of()).forGetter(k -> new ArrayList<>(k.discovered)),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("pool", java.util.Map.of())
                    .forGetter(PlayerKnowledge::poolAsMap),
            Codec.STRING.listOf().optionalFieldOf("scanned", List.of()).forGetter(k -> new ArrayList<>(k.scanned)),
            Codec.STRING.listOf().optionalFieldOf("research", List.of()).forGetter(k -> new ArrayList<>(k.research))
    ).apply(instance, (discovered, pool, scanned, research) -> {
        AspectList list = new AspectList();
        pool.forEach((tag, amount) -> list.add(Aspect.of(tag), amount));
        return new PlayerKnowledge(discovered, list, scanned, research);
    }));

    private java.util.Map<String, Integer> poolAsMap() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        for (var entry : this.pool.entries()) map.put(entry.getKey().tag(), entry.getValue());
        return map;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC = StreamCodec.of(
            (buffer, knowledge) -> {
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buffer, new ArrayList<>(knowledge.discovered));
                AspectList.STREAM_CODEC.encode(buffer, knowledge.pool);
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buffer, new ArrayList<>(knowledge.scanned));
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buffer, new ArrayList<>(knowledge.research));
            },
            buffer -> new PlayerKnowledge(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buffer),
                    AspectList.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buffer),
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buffer)));
}
