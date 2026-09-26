package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * O Firma-Fendas: o {@code ItemRiftStabilizer} das Portas Dimensionais.
 *
 * <p>Usado numa fenda solta, prende-a: ela deixa de comer o mundo em volta e de se fechar sozinha, e passa a
 * poder receber uma porta. Dura seis usos e só serve a fendas soltas — numa porta não faz nada.
 *
 * <p><b>Uma coisa que o porte teve de resolver:</b> a fenda não tem corpo — não se lhe pode carregar em cima,
 * porque o raio do rato passa através dela. Lá isso não fazia diferença, porque a fenda era só de enfeite; aqui é
 * dela que sai a porta. Então o Firma-Fendas procura a fenda <b>na linha de visão</b>, como a Lâmina de Fenda já
 * fazia: aponta-se-lhe e carrega-se, com ou sem bloco por trás.
 */
public class RiftStabilizerItem extends Item {
    public RiftStabilizerItem(Properties properties) {
        super(properties);
    }

    /** Carregar no ar: procura-se a fenda apontada. */
    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        BlockPos onde = RiftBladeItem.riftAimedAt(level, quem);
        if (onde == null) return InteractionResult.PASS;
        return hold(level, onde, quem, quem.getItemInHand(mão));
    }

    /** E carregar num bloco: se houver fenda na linha, é essa; senão, a casa em que se carregou. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player quem = context.getPlayer();
        BlockPos onde = quem == null ? null : RiftBladeItem.riftAimedAt(level, quem);
        if (onde == null) onde = context.getClickedPos();
        return hold(level, onde, quem, context.getItemInHand());
    }

    /** Prende aquela fenda, se for fenda e ainda estiver solta. */
    private static InteractionResult hold(Level level, BlockPos onde, @Nullable Player quem, ItemStack ferro) {
        if (!level.getBlockState(onde).is(ShatteredBlocks.RIFT)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity fenda)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

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
            ferro.hurtAndBreak(1, quem, EquipmentSlot.MAINHAND);
        }
        return InteractionResult.SUCCESS;
    }
}
