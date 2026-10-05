package net.thaumcraft.occulta.symbol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * O <b>feitiço preparado</b> e as <b>travas</b>: o pedaço do {@code ItemMysticBranch} que guarda estado.
 *
 * <p>Desenhar um símbolo não o lança: <b>o prepara</b>. O lado de cá, que é quem lê o desenho, manda para o
 * servidor <b>qual</b> símbolo e de que <b>grau</b>, e o servidor os guarda até a vara ser largada.
 *
 * <p>Isso é o que torna o gesto confiável: o desenho pode ser lido com o atraso da rede sem que o feitiço
 * saia torto, porque quem o lança é sempre o servidor, com o que ele guardou.
 */
public final class Spells {
    /** O que ele tem preparado: que símbolo, e de que grau. */
    public record Preparado(int id, int grau) {
        public static final Codec<Preparado> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                Codec.INT.fieldOf("id").forGetter(Preparado::id),
                Codec.INT.fieldOf("grau").forGetter(Preparado::grau)
        ).apply(campo, Preparado::new));
    }

    /**
     * O feitiço preparado.
     *
     * <p><b>Não atravessa a morte</b>, e não se guarda em disco: um feitiço preparado dura o tempo de se
     * largar a vara, e desligar o mundo no meio disso é desistir dele.
     */
    public static final AttachmentType<Preparado> PREPARADO = AttachmentRegistry.<Preparado>builder()
            .buildAndRegister(Thaumcraft.id("spell_prepared"));

    /** As travas de cada símbolo, por jogador. */
    public static final AttachmentType<Map<String, Long>> TRAVAS =
            AttachmentRegistry.<Map<String, Long>>builder()
                    .initializer(HashMap::new)
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.LONG))
                    .copyOnDeath()
                    .buildAndRegister(Thaumcraft.id("spell_cooldowns"));

    private Spells() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela os apegos. */
    public static void init() {
    }

    public static @Nullable Preparado preparado(ServerPlayer quem) {
        return quem.getAttached(PREPARADO);
    }

    public static void prepara(ServerPlayer quem, int id, int grau) {
        quem.setAttached(PREPARADO, new Preparado(id, grau));
    }

    public static void esquece(ServerPlayer quem) {
        quem.removeAttached(PREPARADO);
    }

    // ------------------------------------------------------------------ as travas

    /** Quanto falta da trava daquele símbolo, em batidas. */
    public static long travaQueFalta(ServerPlayer quem, Symbol qual, ServerLevel level) {
        if (qual.trava <= 0) return 0L;
        Map<String, Long> tem = quem.getAttachedOrCreate(TRAVAS);
        Long quando = tem.get(String.valueOf(qual.id));
        if (quando == null) return 0L;
        long passou = level.getGameTime() - quando;
        return Math.max(0L, qual.trava - passou);
    }

    /** E a põe, depois de lançá-lo. */
    public static void põeTrava(ServerPlayer quem, Symbol qual, ServerLevel level) {
        if (qual.trava <= 0) return;
        Map<String, Long> tem = new HashMap<>(quem.getAttachedOrCreate(TRAVAS));
        tem.put(String.valueOf(qual.id), level.getGameTime());
        quem.setAttached(TRAVAS, Map.copyOf(tem));
    }

    // ------------------------------------------------------------------ o grau que vale

    /**
     * <b>O grau que o desenho vale a quem o fez.</b>
     *
     * <p>Um desenho de grau dois ou três só vale o grau dele a quem tiver <b>Adoração</b> bastante: o
     * original pede que o grau não passe do nível da poção <b>mais dois</b>. Sem ela, o desenho comprido é
     * lançado como <b>grau um</b> e o esforço foi para nada.
     *
     * <p>É o que liga os símbolos à <b>Estátua de Adoração</b> — e, enquanto ela não estiver portada, os
     * graus dois e três só se alcançam no criativo. Fica declarado.
     */
    public static int grauQueVale(ServerPlayer quem, int grau) {
        if (grau <= 1) return grau;
        var adoração = quem.getEffect(net.thaumcraft.occulta.OccultaEffects.WORSHIP);
        if (adoração == null) return 1;
        return grau <= adoração.getAmplifier() + 2 ? grau : 1;
    }
}
