package net.thaumcraft.occulta.door;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * <b>A chave de uma porta</b>: o {@code doorX}/{@code doorY}/{@code doorZ} do {@code BlockWitchDoor}.
 *
 * <p>Uma chave do ofício não abre portas <b>deste tipo</b>: abre <b>aquela porta</b>, a que está naquelas três
 * contas e naquele mundo. Ela nasce quando a porta é posta e não se faz de outro jeito — perdida a chave, a
 * porta fica fechada para sempre e só se abre quebrando-a.
 *
 * <p>E o <b>chaveiro</b> é uma lista de chaves numa argola: duas chaves fazem um chaveiro, e um chaveiro mais
 * uma chave faz um chaveiro maior. Ele guarda cada porta uma vez só.
 *
 * <p>A conta olha a <b>metade de baixo</b> da porta, sempre. Uma porta tem duas casas e a chave sabe só uma;
 * clicando na de cima, é a de baixo que se procura.
 */
public final class DoorKeys {
    private DoorKeys() {
    }

    /** Onde esta porta mora, pela metade de baixo dela. */
    public static GlobalPos onde(Level level, BlockPos casa, BlockState feitio) {
        BlockPos baixo = feitio.hasProperty(DoorBlock.HALF)
                && feitio.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? casa.below() : casa;
        return new GlobalPos(level.dimension(), baixo);
    }

    /** Uma chave nova, feita para esta porta. */
    public static ItemStack chave(GlobalPos porta) {
        ItemStack oquê = new ItemStack(OccultaItems.DOOR_KEY);
        oquê.set(OccultaComponents.DOOR_KEY, porta);
        return oquê;
    }

    /** O que esta chave abre, se abrir alguma coisa. */
    public static @Nullable GlobalPos deQuê(ItemStack oquê) {
        return oquê.get(OccultaComponents.DOOR_KEY);
    }

    /** O que este chaveiro abre. */
    public static List<GlobalPos> doChaveiro(ItemStack oquê) {
        List<GlobalPos> tem = oquê.get(OccultaComponents.KEYRING);
        return tem == null ? List.of() : tem;
    }

    /** Se esta peça — chave ou chaveiro — abre aquela porta. */
    public static boolean abre(ItemStack oquê, GlobalPos porta) {
        if (oquê.isEmpty()) return false;
        if (porta.equals(deQuê(oquê))) return true;
        return doChaveiro(oquê).contains(porta);
    }

    /**
     * Se alguém traz com que abrir esta porta.
     *
     * <p>O original varre o <b>inventário inteiro</b>, e não só a mão: a chave de casa não se leva na mão.
     */
    public static boolean temChave(Player quem, GlobalPos porta) {
        for (int lugar = 0; lugar < quem.getInventory().getContainerSize(); lugar++) {
            if (abre(quem.getInventory().getItem(lugar), porta)) return true;
        }
        return false;
    }

    /** E o chaveiro com mais uma porta, se ela ainda não estiver nele. */
    public static List<GlobalPos> mais(List<GlobalPos> tinha, GlobalPos porta) {
        if (tinha.contains(porta)) return tinha;
        List<GlobalPos> agora = new java.util.ArrayList<>(tinha);
        agora.add(porta);
        return List.copyOf(agora);
    }
}
