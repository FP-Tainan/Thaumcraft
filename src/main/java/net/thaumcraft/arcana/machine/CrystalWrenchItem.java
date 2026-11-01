package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.thaumcraft.arcana.ArcanaComponents;
import net.thaumcraft.arcana.power.PowerNode;
import net.thaumcraft.arcana.power.PowerNodes;

/**
 * A <b>Chave de Cristal</b>: o {@code ItemCrystalWrench} da 1.4.0.009.
 *
 * <p>É com ela que a rede de Etherium se desenha, e é a única maneira: dois nós não se ligam por estarem
 * encostados, ligam-se porque alguém foi lá com a chave e disse <b>daqui</b> e depois <b>para ali</b>.
 *
 * <p><b>Toca-se primeiro na fonte e depois na máquina</b>, nessa ordem — a chave guarda o primeiro lugar e
 * usa-o no segundo toque. Tocando na fonte <b>agachado</b>, ela fica com o lugar guardado para sempre, e
 * cada toque seguinte liga mais uma máquina à mesma fonte.
 *
 * <p>Agachado sobre coisa nenhuma, ela <b>troca de modo</b>: no modo de cortar, um toque num nó desfaz
 * <b>todas</b> as ligações que ele tinha. E, estando com um lugar guardado para sempre, o primeiro
 * agachar é o que esquece o lugar — só o seguinte troca o modo.
 */
public class CrystalWrenchItem extends Item {
    public CrystalWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext contexto) {
        Level nível = contexto.getLevel();
        BlockPos onde = contexto.getClickedPos();
        ItemStack chave = contexto.getItemInHand();
        Player quem = contexto.getPlayer();
        if (quem == null) return InteractionResult.PASS;

        BlockEntity bloco = nível.getBlockEntity(onde);
        boolean cortar = chave.has(ArcanaComponents.WRENCH_CUT);

        if (bloco != null && !(bloco instanceof PowerNode) && cortar) {
            if (!nível.isClientSide()) quem.sendSystemMessage(Component.translatable("thaumcraft.power.wrongMode"));
            return InteractionResult.SUCCESS;
        }

        if (bloco instanceof PowerNode nó) {
            if (nível.isClientSide()) return InteractionResult.SUCCESS;
            ServerLevel servidor = (ServerLevel) nível;
            if (cortar) {
                PowerNodes.of(servidor).tryDisconnectAllNodes(onde);
                quem.sendSystemMessage(Component.translatable("thaumcraft.power.disconnected"));
                return InteractionResult.SUCCESS;
            }
            BlockPos guardado = chave.get(ArcanaComponents.WRENCH_PAIR);
            if (guardado == null) {
                chave.set(ArcanaComponents.WRENCH_PAIR, onde);
                if (quem.isShiftKeyDown()) chave.set(ArcanaComponents.WRENCH_KEEP, Unit.INSTANCE);
                quem.sendSystemMessage(Component.translatable("thaumcraft.power.sourceStored"));
                return InteractionResult.SUCCESS;
            }
            if (!(servidor.getBlockEntity(guardado) instanceof PowerNode fonte)) {
                chave.remove(ArcanaComponents.WRENCH_PAIR);
                quem.sendSystemMessage(Component.translatable("thaumcraft.power.sourceGone"));
                return InteractionResult.SUCCESS;
            }
            quem.sendSystemMessage(
                    PowerNodes.of(servidor).tryPairNodes(servidor, guardado, fonte, onde, nó));
            if (!chave.has(ArcanaComponents.WRENCH_KEEP)) chave.remove(ArcanaComponents.WRENCH_PAIR);
            return InteractionResult.SUCCESS;
        }

        if (quem.isShiftKeyDown()) {
            if (!nível.isClientSide()) changeMode(chave, quem);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * O {@code handleModeChanges}: agachar esquece primeiro o lugar guardado, e só depois troca o modo.
     */
    public static void changeMode(ItemStack chave, Player quem) {
        if (chave.has(ArcanaComponents.WRENCH_KEEP)) {
            chave.remove(ArcanaComponents.WRENCH_KEEP);
            chave.remove(ArcanaComponents.WRENCH_PAIR);
            quem.sendSystemMessage(Component.translatable("thaumcraft.power.forgot"));
            return;
        }
        if (chave.has(ArcanaComponents.WRENCH_CUT)) chave.remove(ArcanaComponents.WRENCH_CUT);
        else chave.set(ArcanaComponents.WRENCH_CUT, Unit.INSTANCE);
        quem.sendSystemMessage(Component.translatable(chave.has(ArcanaComponents.WRENCH_CUT)
                ? "thaumcraft.power.modeCut" : "thaumcraft.power.modePair"));
    }
}
