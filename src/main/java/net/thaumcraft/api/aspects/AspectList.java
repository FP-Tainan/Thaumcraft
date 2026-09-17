package net.thaumcraft.api.aspects;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Um punhado de aspectos com quantidade: o que uma coisa tem dentro, o que um jarro guarda, o que uma
 * receita cobra.
 *
 * <p>Porte fiel do {@code thaumcraft.api.aspects.AspectList} da 4.2.3.5 — os mesmos nomes de método, para
 * quem conhece o original não se perder.
 */
public final class AspectList {
    private final Map<Aspect, Integer> aspects = new LinkedHashMap<>();

    public AspectList() {
    }

    public AspectList(AspectList other) {
        this.aspects.putAll(other.aspects);
    }

    /** Quantos aspectos diferentes há aqui. */
    public int size() {
        return this.aspects.size();
    }

    /** A soma de tudo. */
    public int visSize() {
        int total = 0;
        for (int amount : this.aspects.values()) total += amount;
        return total;
    }

    public boolean isEmpty() {
        return this.aspects.isEmpty();
    }

    public int getAmount(Aspect aspect) {
        return this.aspects.getOrDefault(aspect, 0);
    }

    /** Põe mais daquele aspecto. */
    public AspectList add(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return this;
        this.aspects.merge(aspect, amount, Integer::sum);
        return this;
    }

    /** Junta tudo o que a outra lista tem. */
    public AspectList add(AspectList other) {
        for (Map.Entry<Aspect, Integer> entry : other.aspects.entrySet()) add(entry.getKey(), entry.getValue());
        return this;
    }

    /**
     * Guarda só se for mais do que já havia. É como o thaumômetro anota o que aprendeu: dois caminhos para a
     * mesma coisa não somam, vale o maior.
     */
    public AspectList merge(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return this;
        if (getAmount(aspect) < amount) this.aspects.put(aspect, amount);
        return this;
    }

    public AspectList merge(AspectList other) {
        for (Map.Entry<Aspect, Integer> entry : other.aspects.entrySet()) merge(entry.getKey(), entry.getValue());
        return this;
    }

    /** Tira daquele aspecto, se houver o bastante. Devolve se deu. */
    public boolean reduce(Aspect aspect, int amount) {
        int have = getAmount(aspect);
        if (have < amount) return false;
        if (have == amount) this.aspects.remove(aspect);
        else this.aspects.put(aspect, have - amount);
        return true;
    }

    /** Tira o que puder, mesmo que não chegue; devolve quanto saiu. */
    public int remove(Aspect aspect, int amount) {
        int taken = Math.min(getAmount(aspect), amount);
        reduce(aspect, taken);
        return taken;
    }

    public AspectList remove(Aspect aspect) {
        this.aspects.remove(aspect);
        return this;
    }

    public AspectList copy() {
        return new AspectList(this);
    }

    public List<Aspect> getAspects() {
        return new ArrayList<>(this.aspects.keySet());
    }

    /** Só os primários que estão aqui. */
    public List<Aspect> getPrimalAspects() {
        return this.aspects.keySet().stream().filter(Aspect::isPrimal).toList();
    }

    /** Em ordem alfabética do nome em latim, como o original mostra. */
    public List<Aspect> getAspectsSorted() {
        List<Aspect> list = getAspects();
        list.sort(Comparator.comparing(Aspect::tag));
        return list;
    }

    /** Do que tem mais para o que tem menos. */
    public List<Aspect> getAspectsSortedAmount() {
        List<Aspect> list = getAspects();
        list.sort(Comparator.comparingInt(this::getAmount).reversed());
        return list;
    }

    public Iterable<Map.Entry<Aspect, Integer>> entries() {
        return this.aspects.entrySet();
    }

    /**
     * Como a lista fica guardada em disco: um número por nome em latim, que é como o original guardava.
     *
     * <p>Aspecto que o jogo de hoje não conheça é simplesmente deixado de lado, em vez de estourar a
     * leitura do mundo inteiro.
     */
    public static final com.mojang.serialization.Codec<AspectList> CODEC =
            com.mojang.serialization.Codec.unboundedMap(
                            com.mojang.serialization.Codec.STRING, com.mojang.serialization.Codec.INT)
                    .xmap(map -> {
                        AspectList list = new AspectList();
                        map.forEach((tag, amount) -> {
                            Aspect aspect = Aspect.of(tag);
                            if (aspect != null) list.add(aspect, amount);
                        });
                        return list;
                    }, list -> {
                        Map<String, Integer> map = new java.util.LinkedHashMap<>();
                        for (Map.Entry<Aspect, Integer> entry : list.aspects.entrySet()) {
                            map.put(entry.getKey().tag(), entry.getValue());
                        }
                        return map;
                    });

    /** Como a lista viaja do servidor para o cliente. */
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectList> STREAM_CODEC = StreamCodec.of(
            (buffer, list) -> {
                buffer.writeVarInt(list.aspects.size());
                for (Map.Entry<Aspect, Integer> entry : list.aspects.entrySet()) {
                    ByteBufCodecs.STRING_UTF8.encode(buffer, entry.getKey().tag());
                    buffer.writeVarInt(entry.getValue());
                }
            },
            buffer -> {
                AspectList list = new AspectList();
                int size = buffer.readVarInt();
                for (int index = 0; index < size; index++) {
                    Aspect aspect = Aspect.of(ByteBufCodecs.STRING_UTF8.decode(buffer));
                    int amount = buffer.readVarInt();
                    list.add(aspect, amount);
                }
                return list;
            });

    @Override
    public String toString() {
        return this.aspects.toString();
    }
}
