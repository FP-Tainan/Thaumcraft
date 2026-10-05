package net.thaumcraft.occulta.infusion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O <b>registro das infusões</b> e o que cada jogador carrega delas: o {@code Infusion.Registry} do Witchery.
 *
 * <p>Guarda, por jogador, <b>três números</b>: qual infusão ele tem, quanta carga lhe resta e qual era o
 * teto. No original são três etiquetas no {@code PlayerPersisted} dele; aqui é um apego que atravessa a
 * morte, porque a infusão não se perde ao morrer — foi feita <b>no corpo</b>, e o corpo volta.
 *
 * <h2>A carga não volta sozinha</h2>
 *
 * <p>Vale dizê-lo porque é a decisão de desenho mais importante deste ramo: <b>não há recarga passiva</b>.
 * O que o rito der é o que há, e cada poder gasta. Para encher outra vez é preciso ou <b>refazer o rito</b>
 * — que custa quatro mil de altar e quase mata — ou ir a uma <b>Estátua de Adoração</b>, que dá trinta de
 * cada vez e ainda não está portada.
 *
 * <p>Quer dizer que a infusão não é uma barra de mana: é um <b>cantil</b>. Quem se infunde anda a contar as
 * goladas.
 */
public final class Infusions {
    /** Quanto tempo a Mão de Bruxa se deixa segurar. */
    public static final int SEGURA = 400;

    /** Quanta carga o rito dá. */
    public static final int CARGAS = 200;

    /** E a que distância do círculo ele apanha quem está lá. */
    public static final int ALCANCE = 4;

    /** Quanto dano o rito faz a quem se infunde. */
    public static final float DANO = 100.0f;

    /** O que um jogador carrega da infusão dele. */
    public record Carga(int id, int tem, int teto) {
        public static final Carga NENHUMA = new Carga(0, 0, 0);

        public static final Codec<Carga> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                Codec.INT.fieldOf("id").forGetter(Carga::id),
                Codec.INT.fieldOf("tem").forGetter(Carga::tem),
                Codec.INT.fieldOf("teto").forGetter(Carga::teto)
        ).apply(campo, Carga::new));
    }

    /** O que ele carrega — e que o lado de cá também vê, para a barra de poder. */
    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, Carga> CARGA_PELA_REDE =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(Carga.CODEC);

    public static final AttachmentType<Carga> CARGA = AttachmentRegistry.<Carga>builder()
            .initializer(() -> Carga.NENHUMA)
            .persistent(Carga.CODEC)
            .syncWith(CARGA_PELA_REDE,
                    net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("infusion"));

    private static final List<Infusion> TODAS = new ArrayList<>();

    /** A infusão de quem não tem nenhuma, que não sabe fazer nada. */
    public static final Infusion NENHUMA = new Infusion(0) {
    };

    static {
        põe(NENHUMA);
        põe(new LightInfusion(1));
        põe(new OverworldInfusion(2));
        põe(new OtherwhereInfusion(3));
        põe(new InfernalInfusion(4));
    }

    private Infusions() {
    }

    private static void põe(Infusion qual) {
        TODAS.add(qual);
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego e a lista. */
    public static void init() {
        Shockwave.init();
    }

    /** A infusão daquele número, ou a de ninguém. */
    public static Infusion daquele(int id) {
        for (Infusion cada : TODAS) {
            if (cada.id == id) return cada;
        }
        return NENHUMA;
    }

    /** Quantas há. */
    public static int quantas() {
        return TODAS.size();
    }

    /** A que este jogador carrega. */
    public static Infusion de(Player quem) {
        return daquele(quem.getAttachedOrCreate(CARGA).id());
    }

    // ------------------------------------------------------------------ a carga

    public static int energia(Player quem) {
        return quem.getAttachedOrCreate(CARGA).tem();
    }

    public static int teto(Player quem) {
        return quem.getAttachedOrCreate(CARGA).teto();
    }

    /** Põe a carga dele, deixando a infusão e o teto como estão. */
    public static void põeEnergia(ServerPlayer quem, int quanta) {
        Carga tinha = quem.getAttachedOrCreate(CARGA);
        quem.setAttached(CARGA, new Carga(tinha.id(), quanta, tinha.teto()));
    }

    /** Enche até o teto, sem passar dele: o que a Estátua de Adoração faz. */
    public static void enche(ServerPlayer quem, int quanta) {
        Carga tinha = quem.getAttachedOrCreate(CARGA);
        quem.setAttached(CARGA, new Carga(tinha.id(), Math.min(tinha.tem() + quanta, tinha.teto()),
                tinha.teto()));
    }

    /** <b>Apaga a carga</b> e deixa a infusão: o {@code clearInfusion} do original. */
    public static void apaga(ServerPlayer quem) {
        Carga tinha = quem.getAttachedOrCreate(CARGA);
        quem.setAttached(CARGA, new Carga(tinha.id(), 0, tinha.teto()));
    }

    /**
     * <b>Infunde.</b>
     *
     * <p>Troca a infusão que ele tivesse e o enche até o teto novo. É o que o rito faz a quem sobreviver.
     */
    public static void infunde(ServerPlayer quem, Infusion qual, int cargas) {
        quem.setAttached(CARGA, new Carga(qual.id, cargas, cargas));
    }

    /**
     * <b>Tira carga para um poder</b>, com recado se não houver.
     *
     * <p>É o {@code aquireEnergy} do original: o que o equipamento de fora chama. A diferença para o
     * {@code gasta} de dentro da infusão é que este <b>não apaga o que sobrava</b> — só recusa.
     */
    public static boolean tira(ServerLevel level, ServerPlayer quem, int quanto, boolean recado) {
        Carga tem = quem.getAttachedOrCreate(CARGA);
        if (tem.id() == 0) {
            if (recado) {
                quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.infusionrequired")
                        .withStyle(ChatFormatting.RED));
                Infusion.falha(level, quem);
            }
            return false;
        }
        if (!quem.getAbilities().instabuild && tem.tem() < quanto) {
            if (recado) {
                quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.nocharges")
                        .withStyle(ChatFormatting.RED));
                Infusion.falha(level, quem);
            }
            return false;
        }
        if (!quem.getAbilities().instabuild) põeEnergia(quem, tem.tem() - quanto);
        return true;
    }

    // ------------------------------------------------------------------ o lugar de voltar

    /** Onde ele guardou o lugar de voltar: o {@code WITCRecall} do original. */
    public record Volta(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> onde,
                        net.minecraft.core.BlockPos lugar) {
        public static final Codec<Volta> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                net.minecraft.resources.ResourceKey.codec(
                        net.minecraft.core.registries.Registries.DIMENSION)
                        .fieldOf("onde").forGetter(Volta::onde),
                net.minecraft.core.BlockPos.CODEC.fieldOf("lugar").forGetter(Volta::lugar)
        ).apply(campo, Volta::new));
    }

    public static final AttachmentType<Volta> VOLTA = AttachmentRegistry.<Volta>builder()
            .persistent(Volta.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("recall"));

    public static @Nullable Volta volta(Player quem) {
        return quem.getAttached(VOLTA);
    }

    public static void guardaVolta(ServerPlayer quem) {
        quem.setAttached(VOLTA, new Volta(quem.level().dimension(), quem.blockPosition()));
    }
}
