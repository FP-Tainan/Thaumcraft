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
import net.minecraft.world.level.block.Blocks;

/**
 * O Fecha-Fendas: o {@code ItemRiftRemover} das Portas Dimensionais.
 *
 * <p>Usado numa fenda solta, fecha-a; usado numa porta dimensional, faz a porta esquecer para onde levava — e a
 * próxima travessia volta a abrir um bolso novo.
 */
public class RiftRemoverItem extends Item {
    public RiftRemoverItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        boolean solta = level.getBlockState(onde).is(ShatteredBlocks.RIFT);
        if (solta) {
            level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        } else {
            fenda.setDestination(null);
        }
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 0.6f);
        if (context.getPlayer() != null) {
            context.getPlayer().sendOverlayMessage(Component.translatable(
                    solta ? "item.thaumcraft.rift_remover.closed" : "item.thaumcraft.rift_remover.cleared"));
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), EquipmentSlot.MAINHAND);
        }
        return InteractionResult.SUCCESS;
    }
}
