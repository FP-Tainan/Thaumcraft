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
 *
 * <p>Como o Firma-Fendas, procura a fenda <b>na linha de visão</b>: uma fenda solta não tem corpo, e o raio do
 * rato passa através dela.
 */
public class RiftRemoverItem extends Item {
    public RiftRemoverItem(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.InteractionResult use(Level level, net.minecraft.world.entity.player.Player quem,
                                                     net.minecraft.world.InteractionHand mão) {
        BlockPos onde = RiftBladeItem.riftAimedAt(level, quem);
        if (onde == null) return InteractionResult.PASS;
        return close(level, onde, quem, quem.getItemInHand(mão));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        var jogador = context.getPlayer();
        BlockPos apontada = jogador == null ? null : RiftBladeItem.riftAimedAt(level, jogador);
        return close(level, apontada == null ? context.getClickedPos() : apontada, jogador, context.getItemInHand());
    }

    private static InteractionResult close(Level level, BlockPos onde,
                                           @org.jetbrains.annotations.Nullable net.minecraft.world.entity.player.Player quem,
                                           net.minecraft.world.item.ItemStack ferro) {
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        boolean solta = level.getBlockState(onde).is(ShatteredBlocks.RIFT);
        if (solta) {
            level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        } else {
            fenda.setDestination(null);
        }
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 0.6f);
        if (quem != null) {
            quem.sendOverlayMessage(Component.translatable(
                    solta ? "item.thaumcraft.rift_remover.closed" : "item.thaumcraft.rift_remover.cleared"));
            ferro.hurtAndBreak(1, quem, EquipmentSlot.MAINHAND);
        }
        return InteractionResult.SUCCESS;
    }
}
