package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.NoDrops;
import net.thaumcraft.occulta.village.VillageGuardEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>reserva de sangue</b>: o {@code bloodReserve} do {@code ExtendedPlayer}.
 *
 * <p>É um <b>cantil</b>, e não um poder. Quem mata um <b>aldeão</b>, um <b>guarda</b> ou uma <b>pessoa</b>
 * leva o sangue que a vítima ainda tinha, até <b>duzentos e cinquenta</b> — e esse sangue fica guardado,
 * fora do corpo, até alguém o beber.
 *
 * <p>Quem o bebe é a <b>Bengala-Espada</b>, e é a única coisa no ramo que o sabe fazer. Por isso a reserva
 * existe mesmo em quem nunca viu uma bengala: ela enche-se sozinha, e espera.
 *
 * <p>E <b>só um vampiro a vê</b>: o {@code getBloodReserve} do original devolve zero a quem não for, embora
 * o número continue lá. É uma crueldade pequena e deliberada — a bengala de quem não é vampiro é só uma
 * espada.
 *
 * <p>De quem <b>não larga nada</b> não se tira sangue: é o {@code isNoDrops} do original, e vale aqui pela
 * mesma razão — um aldeão que o mod fez para morrer não é comida.
 */
public final class BloodReserve {
    /** O teto do cantil. */
    public static final int TETO = 250;

    /** O que fica guardado, fora do corpo. Vai para o cliente, que é quem o mostra na dica da bengala. */
    public static final AttachmentType<Integer> DATA = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast(),
                    net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .initializer(() -> 0)
            .buildAndRegister(Thaumcraft.id("blood_reserve"));

    /**
     * <b>Quem pergunta o cantil na tela.</b>
     *
     * <p>A dica da Bengala-Espada mostra o número, e quem a desenha é o cliente — que tem um jogador só e
     * não o passa a quem escreve a dica. O cliente põe aqui a pergunta ao arrancar; no servidor isto
     * devolve zero e ninguém o lê.
     */
    public static java.util.function.IntSupplier naTela = () -> 0;

    private BloodReserve() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** O que há no cantil — e <b>zero</b>, a quem não for vampiro. */
    public static int de(@Nullable Player quem) {
        if (quem == null || !Vampire.é(quem)) return 0;
        Integer tem = quem.getAttached(DATA);
        return tem == null ? 0 : tem;
    }

    /** O que há no cantil, vampiro ou não: é o que o enche precisa de saber. */
    public static int guardado(Player quem) {
        Integer tem = quem.getAttached(DATA);
        return tem == null ? 0 : tem;
    }

    /** Enche, até o teto: o {@code fillBloodReserve}. */
    public static void enche(Player quem, int quanto) {
        if (quanto <= 0) return;
        quem.setAttached(DATA, Math.min(guardado(quem) + quanto, TETO));
    }

    /** Se há o que beber. */
    public static boolean pronta(Player quem) {
        return de(quem) > 0;
    }

    /**
     * <b>Bebe o cantil inteiro.</b>
     *
     * <p>E só <b>estando com fome</b>: o original não o esvazia a quem já está cheio de poder, de modo que
     * um gole nunca se perde. É o {@code useBloodReserve}.
     */
    public static boolean bebe(Player quem) {
        int tem = de(quem);
        if (tem <= 0 || Vampire.sangueDe(quem) >= Vampire.tetoDoSangue(quem)) return false;
        quem.setAttached(DATA, 0);
        Vampire.bebe(quem, tem);
        return true;
    }

    /**
     * O que o morto dá ao cantil de quem o matou.
     *
     * <p>Aldeão, guarda e pessoa dão o sangue que ainda tinham; tudo o mais não dá nada. É a conta do
     * {@code onLivingDeath} do original.
     */
    public static void levou(Player quemMatou, LivingEntity quemMorreu) {
        if (NoDrops.marcado(quemMorreu)) return;
        boolean humano = quemMorreu instanceof Villager
                || quemMorreu instanceof VillageGuardEntity
                || quemMorreu instanceof Player;
        if (!humano) return;
        enche(quemMatou, Blood.de(quemMorreu));
    }
}
