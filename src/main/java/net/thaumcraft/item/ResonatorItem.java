package net.thaumcraft.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.block.entity.TubeBufferBlockEntity;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;

/**
 * O ressonador de essência: o {@code ItemResonator} da 4.2.3.5. Tocado num cano (ou em qualquer peça que passa essência)
 * diz no chat quanto de que essência há ali daquele lado e com que força e de que aspecto ele puxa.
 */
public class ResonatorItem extends Item {
    public ResonatorItem(Properties properties) {
        super(properties);
    }

    /** O {@code onItemUseFirst}: antes do bloco abrir qualquer coisa. */
    public static InteractionResult useFirst(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(TCItems.RESONATOR)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(hit.getBlockPos()) instanceof EssentiaTransport et)) return InteractionResult.PASS;
        if (level.isClientSide()) {
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        Direction face = hit.getDirection();
        if (!(et instanceof TubeBufferBlockEntity) && et.getEssentiaType(face) != null) {
            player.sendSystemMessage(Component.translatable("tc.resonator1", "" + et.getEssentiaAmount(face), et.getEssentiaType(face).name()));
        } else if (et instanceof TubeBufferBlockEntity buffer && buffer.aspects().size() > 0) {
            for (Aspect aspect : buffer.aspects().getAspectsSorted()) {
                player.sendSystemMessage(Component.translatable("tc.resonator1", "" + buffer.aspects().getAmount(aspect), aspect.name()));
            }
        }
        Component suction = et.getSuctionType(face) != null ? et.getSuctionType(face).name() : Component.translatable("tc.resonator3");
        player.sendSystemMessage(Component.translatable("tc.resonator2", "" + et.getSuctionAmount(face), suction));
        level.playSound(null, hit.getBlockPos(), TCSounds.ALEMBIC_KNOCK.value(), SoundSource.BLOCKS, 0.5f, 1.9f + level.getRandom().nextFloat() * 0.1f);
        return InteractionResult.SUCCESS;
    }
}
