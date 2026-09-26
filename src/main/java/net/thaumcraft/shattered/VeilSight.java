package net.thaumcraft.shattered;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

import java.util.function.Supplier;

/**
 * Quem enxerga o Véu.
 *
 * <p>A lore do ramo diz que as fendas não se abrem: <i>sempre estiveram abertas</i>, e o que muda é quem as
 * consegue ver. Enquanto o thaumaturgo não tiver posto Fio do Mundo nos Óculos da Descoberta, uma fenda que já
 * estava no mundo é ar para ele — não a vê, não lhe vê as fagulhas, e passa-lhe ao lado sem saber.
 *
 * <p>O que ele mesmo fez é outra coisa: uma porta que ele assentou ou uma fenda que ele rasgou com a Assinatura
 * ficam à vista de qualquer um, com óculos ou sem eles. Quem rasgou sabe onde rasgou.
 *
 * <p>É o irmão do {@link net.thaumcraft.item.Revealing} — aquele diz quem vê os nós de aura, este quem vê o que
 * está por trás do mundo. Um elmo de outro mod entra pela etiqueta, como lá.
 *
 * <p><b>Isto é do porte, e não do original:</b> nas Portas Dimensionais toda a fenda se vê desde o primeiro dia.
 * A ideia de as esconder atrás de um óculos é de quem joga.
 */
public final class VeilSight {
    /** A etiqueta por onde um mod de fora diz que o elmo dele também enxerga o Véu. */
    public static final TagKey<Item> SEES_THE_VEIL =
            TagKey.create(Registries.ITEM, Thaumcraft.id("sees_the_veil"));

    /**
     * Quem está a olhar, do lado de quem joga.
     *
     * <p>O código comum não pode pedir a máquina do cliente — no servidor dedicado ela não existe. Então quem
     * corre na máquina de quem joga põe aqui como se acha o jogador da vez, que é o mesmo jeito do
     * {@code clientTrail} dos orbes de foco.
     */
    public static Supplier<Player> localPlayer = () -> null;

    private VeilSight() {
    }

    /** Este jogador enxerga o Véu agora? */
    public static boolean can(Player player) {
        if (player == null) return false;
        ItemStack elmo = player.getItemBySlot(EquipmentSlot.HEAD);
        return elmo.is(ShatteredItems.VEIL_GOGGLES) || elmo.is(SEES_THE_VEIL);
    }

    /**
     * Esta fenda aparece a este jogador?
     *
     * <p>As que nasceram com o mundo pedem os óculos; as que alguém fez estão sempre à vista.
     */
    public static boolean sees(Player player, boolean natural) {
        return !natural || can(player);
    }

    /** O mesmo, para o código comum que corre do lado de quem joga e não tem o jogador à mão. */
    public static boolean seesHere(boolean natural) {
        return sees(localPlayer.get(), natural);
    }
}
