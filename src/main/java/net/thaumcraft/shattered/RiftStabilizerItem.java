package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * O Firma-Fendas: o {@code ItemRiftStabilizer} das Portas Dimensionais.
 *
 * <p>Usado numa fenda solta, prende-a: ela deixa de comer o mundo em volta e de se fechar sozinha. Dura seis
 * usos e só serve a fendas soltas — numa porta não faz nada.
 */
public class RiftStabilizerItem extends Item {
    public RiftStabilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();
        if (!level.getBlockState(onde).is(ShatteredBlocks.RIFT)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        var quem = context.getPlayer();
        if (fenda.stabilized()) {
            if (quem != null) {
                quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_stabilizer.already"));
            }
            return InteractionResult.FAIL;
        }

        fenda.setStabilized(true);
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.0f);
        if (quem != null) {
            quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_stabilizer.stabilized"));
            context.getItemInHand().hurtAndBreak(1, quem, EquipmentSlot.MAINHAND);
        }
        return InteractionResult.SUCCESS;
    }
}
