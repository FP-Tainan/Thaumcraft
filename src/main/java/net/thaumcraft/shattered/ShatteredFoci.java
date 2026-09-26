package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;

import java.util.List;

/**
 * Os três focos de fenda.
 *
 * <p><b>Isto é do porte, e não do original.</b> Nas Portas Dimensionais o abrir, o prender e o fechar de uma fenda
 * são ferramentas de mão — a Assinatura, o Firma-Fendas, o Fecha-Fendas. Quem manda disse que num mod de
 * Thaumcraft isso é trabalho de varinha, e tem razão: é a varinha que gasta vis, é dela que sai tudo o mais que
 * se faz ao mundo, e um thaumaturgo que já tem varinha não anda com três ferros no cinto para isto.
 *
 * <p>Ficam, então, três focos:
 *
 * <ul>
 *   <li><b>Abrir Fenda</b> — rasga uma fenda nova onde a varinha aponta. Ela nasce pequena, e cresce sozinha como
 *       as do mundo;</li>
 *   <li><b>Firmar Fenda</b> — prende a fenda apontada: ela deixa de comer o mundo em volta, e passa a poder
 *       receber uma porta;</li>
 *   <li><b>Fechar Fenda</b> — fecha a fenda apontada; numa porta, faz a porta esquecer para onde levava.</li>
 * </ul>
 *
 * <p>O Firma-Fendas e o Fecha-Fendas de mão saíram, que era o que quem manda pediu. A Assinatura de Fenda fica:
 * ela não abre nem fecha nada, o que ela faz é <i>ligar dois lugares</i>, e isso é outro ofício.
 */
public final class ShatteredFoci {
    /**
     * O que cada um custa de vis. O de abrir é o caro: pôr um buraco no mundo não é pouco.
     *
     * <p><b>Só primordiais:</b> uma varinha não guarda outra coisa. O Vazio, que seria o aspecto certo para isto,
     * é composto e não cabe numa varinha — fica para a essência da infusão que faz o foco.
     */
    public static final AspectList COST_OPEN =
            new AspectList().add(Aspects.ENTROPY, 400).add(Aspects.AIR, 200);
    public static final AspectList COST_HOLD =
            new AspectList().add(Aspects.ORDER, 300).add(Aspects.EARTH, 150);
    public static final AspectList COST_CLOSE =
            new AspectList().add(Aspects.ORDER, 200).add(Aspects.ENTROPY, 100);

    /** Até onde a varinha alcança uma fenda. */
    public static final double REACH = 16.0;

    private ShatteredFoci() {
    }

    public static void init() {
        List<FocusUpgradeTable.Type> só = List.of(FocusUpgradeTable.FRUGAL);
        for (String qual : new String[]{"rift_open", "rift_hold", "rift_close"}) {
            net.thaumcraft.api.FocusUpgrades.ranks(qual, List.of(só, só, só, só, só));
        }
        Focuses.register("rift_open", ShatteredFoci::open);
        Focuses.register("rift_hold", ShatteredFoci::hold);
        Focuses.register("rift_close", ShatteredFoci::close);
    }

    /** Rasga uma fenda onde a varinha aponta, se houver ar para ela. */
    private static boolean open(Level level, Player quem, ItemStack varinha, FocusItem foco) {
        if (!(Focuses.targetBlock(level, quem) instanceof BlockHitResult mira)
                || mira.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos onde = mira.getBlockPos().relative(mira.getDirection());
        if (!level.getBlockState(onde).canBeReplaced()) return false;
        if (level.getBlockState(onde).is(ShatteredBlocks.RIFT)) return false;
        if (!WandItem.consumeFocus(varinha, COST_OPEN, true, quem)) return false;

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(onde, ShatteredBlocks.RIFT.defaultBlockState());
            // quem a rasgou sabe onde a rasgou: esta não pede os Óculos do Véu
            if (level.getBlockEntity(onde) instanceof RiftBlockEntity fenda) fenda.setNatural(false);
        }
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 0.5f);
        return true;
    }

    /** Prende a fenda apontada. */
    private static boolean hold(Level level, Player quem, ItemStack varinha, FocusItem foco) {
        BlockPos onde = RiftAim.riftAimedAt(level, quem);
        if (onde == null) return false;
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) return false;
        if (fenda.stabilized()) {
            quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_stabilizer.already"));
            return false;
        }
        if (!WandItem.consumeFocus(varinha, COST_HOLD, true, quem)) return false;

        if (!level.isClientSide()) fenda.setStabilized(true);
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.0f);
        quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_stabilizer.stabilized"));
        return true;
    }

    /** Fecha a fenda apontada; numa porta, faz a porta esquecer para onde levava. */
    private static boolean close(Level level, Player quem, ItemStack varinha, FocusItem foco) {
        BlockPos onde = RiftAim.riftAimedAt(level, quem);
        if (onde == null) {
            // numa porta não há fenda solta que apanhar: vale o bloco que estiver na mira
            if (!(Focuses.targetBlock(level, quem) instanceof BlockHitResult mira)
                    || mira.getType() != HitResult.Type.BLOCK) {
                return false;
            }
            onde = mira.getBlockPos();
        }
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) return false;
        boolean solta = level.getBlockState(onde).is(ShatteredBlocks.RIFT);
        if (!solta && fenda.destination() == null) return false;
        if (!WandItem.consumeFocus(varinha, COST_CLOSE, true, quem)) return false;

        if (!level.isClientSide()) {
            if (solta) level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
            else fenda.setDestination(null);
        }
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 0.6f);
        quem.sendOverlayMessage(Component.translatable(
                solta ? "item.thaumcraft.rift_remover.closed" : "item.thaumcraft.rift_remover.cleared"));
        return true;
    }
}
