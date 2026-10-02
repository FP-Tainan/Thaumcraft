package net.thaumcraft.occulta.coven;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * As bruxas que entraram no coven de alguém: o {@code WITCCoven} do Witchery.
 *
 * <p>Um coven não se compra nem se fabrica — <b>ganha-se</b>. Fala-se com uma bruxa, ela pede uma coisa, e
 * quando se traz o que ela pediu ela entra. <b>Seis é o teto</b>, como no original.
 *
 * <p>E ele <b>pesa nos ritos</b>: há ritos do ofício que fazem mais com mais bruxas em volta, e é esse número
 * que eles perguntam. Até esta fatia o {@code CircleHeartBlockEntity} passava <b>zero</b> na mão — o
 * {@code Rite.steps(int coven)} estava escrito e à espera desde que os círculos entraram, e não havia quem lhe
 * respondesse.
 *
 * <p>Guarda-se quem entrou, e não só quantas: assim uma bruxa não entra duas vezes, e um dia dá para saber
 * quem é quem.
 */
public record Coven(List<UUID> bruxas) {
    /** O teto do original. */
    public static final int MAX = 6;

    public static final Coven VAZIO = new Coven(List.of());

    public static final Codec<Coven> CODEC = UUIDUtil.CODEC.listOf()
            .xmap(Coven::new, Coven::bruxas).fieldOf("bruxas").codec();

    public static final AttachmentType<Coven> DATA = AttachmentRegistry.<Coven>builder()
            .initializer(() -> VAZIO)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("coven"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o registro do apego. */
    public static void init() {
    }

    public static Coven of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static int tamanho(Player quem) {
        return of(quem).bruxas().size();
    }

    public static boolean cheio(Player quem) {
        return tamanho(quem) >= MAX;
    }

    public static boolean tem(Player quem, UUID bruxa) {
        return of(quem).bruxas().contains(bruxa);
    }

    /**
     * Põe mais uma no coven.
     *
     * @return falso se já estava cheio ou se aquela bruxa já lá estava — que é quando o original a faz
     *         sentir-se enganada e virar contra quem falou com ela
     */
    public static boolean junta(Player quem, UUID bruxa) {
        Coven agora = of(quem);
        if (agora.bruxas().size() >= MAX || agora.bruxas().contains(bruxa)) return false;
        List<UUID> novas = new ArrayList<>(agora.bruxas());
        novas.add(bruxa);
        quem.setAttached(DATA, new Coven(List.copyOf(novas)));
        return true;
    }
}
