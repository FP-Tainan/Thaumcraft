package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * O <b>Elo de Mana</b>: os {@code manaLinks} do {@code ExtendedProperties} do Ars Magica 2.
 *
 * <p>Um elo põe a mana de outra pessoa ao alcance da sua. Quando a sua acaba no meio de um feitiço, o que
 * falta sai da <b>dela</b> — e só enquanto ela estiver perto.
 *
 * <p><b>Quem ganha o elo é quem leva o feitiço, e não quem o lança.</b> Isto se lê mal e é o que o original
 * faz: {@code For(target).updateManaLink(caster)}. Quer dizer que lançar o Elo em alguém é <b>dar-lhe</b> a
 * sua mana, e não tomar a dele. É um feitiço de quem joga acompanhado.
 *
 * <p>E ele <b>alterna</b>: lançado outra vez no mesmo, desfaz o elo.
 *
 * @param quem os donos das manas que esta pessoa pode puxar
 */
public record ManaLinks(List<UUID> quem) {
    public static final ManaLinks NONE = new ManaLinks(List.of());

    /**
     * A que distância um elo ainda serve: os <b>vinte</b> do original.
     *
     * <p>E são vinte de distância <b>ao quadrado</b>, que é como o original mede — pouco mais de quatro
     * blocos e meio. Um elo não é uma corda comprida.
     */
    public static final double ALCANCE_AO_QUADRADO = 20.0;

    public static final Codec<ManaLinks> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.listOf().optionalFieldOf("quem", List.of()).forGetter(ManaLinks::quem))
            .apply(i, ManaLinks::new));

    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, ManaLinks> STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final AttachmentType<ManaLinks> DATA = AttachmentRegistry.<ManaLinks>builder()
            .initializer(() -> NONE)
            .persistent(CODEC)
            .syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("mana_links"));

    public static ManaLinks of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static void set(Player quem, ManaLinks agora) {
        quem.setAttached(DATA, agora);
    }

    /** Põe ou tira o elo daquela pessoa: o {@code updateManaLink}, que alterna. */
    public ManaLinks alterna(UUID outro) {
        var lista = new ArrayList<>(this.quem);
        if (!lista.remove(outro)) lista.add(outro);
        return new ManaLinks(List.copyOf(lista));
    }

    /** Se há elo com aquela pessoa. */
    public boolean tem(UUID outro) {
        return this.quem.contains(outro);
    }

    // ------------------------------------------------------------------ a mana que vem de fora

    /** A mana que os elos põem ao alcance desta pessoa, agora: o {@code getBonusCurrentMana}. */
    public static float extra(ServerLevel level, Player dono) {
        float soma = 0.0f;
        for (Player outro : perto(level, dono)) soma += Mana.of(outro).mana();
        return soma;
    }

    /**
     * Tira o que falta das manas emprestadas: o {@code deductMana} do original.
     *
     * <p>Ele gasta a sua primeiro e só o que sobrar é que sai das outras, uma a uma, até chegar.
     *
     * @param falta o que ainda falta pagar depois de a mana própria acabar
     * @return o que ficou por pagar, se nem com os elos chegou
     */
    public static float paga(ServerLevel level, Player dono, float falta) {
        for (Player outro : perto(level, dono)) {
            if (falta <= 0.0f) break;
            Mana dele = Mana.of(outro);
            float tira = Math.min(dele.mana(), falta);
            if (tira <= 0.0f) continue;
            Mana.set(outro, dele.withMana(dele.mana() - tira));
            falta -= tira;
        }
        return falta;
    }

    /** Os donos de mana com elo a esta pessoa e dentro do alcance. */
    private static List<Player> perto(ServerLevel level, Player dono) {
        ManaLinks elos = of(dono);
        if (elos.quem().isEmpty()) return List.of();

        var achados = new ArrayList<Player>();
        for (UUID id : elos.quem()) {
            var outro = level.getPlayerByUUID(id);
            if (outro == null) continue;
            if (outro.distanceToSqr(dono) > ALCANCE_AO_QUADRADO) continue;
            achados.add(outro);
        }
        return achados;
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o anexo a se registrar. */
    public static void init() {
    }
}
