package net.thaumcraft.arcana.machine;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;

/**
 * A <b>Bateria de Mana</b>: o {@code BlockManaBattery} da 1.4.0.009.
 *
 * <p>Um cubo de ferro. Tocar nele diz <b>quanto tem lá dentro</b>, em percentagem e na cor do tipo que ela
 * guarda — é a única maneira de ver a carga, porque a bateria não tem mostrador nem janela.
 *
 * <p>Com a <b>Chave de Cristal</b> na mão ele não diz nada: a chave está a ligar e não a perguntar.
 */
public class ManaBatteryBlock extends BaseEntityBlock {
    public static final MapCodec<ManaBatteryBlock> CODEC = simpleCodec(ManaBatteryBlock::new);

    public ManaBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState feitio) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new ManaBatteryBlockEntity(onde, feitio);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nível, BlockState feitio, BlockEntityType<T> tipo) {
        if (nível.isClientSide()) return null;
        return createTickerHelper(tipo, ArcanaBlocks.MANA_BATTERY_ENTITY,
                (mundo, onde, estado, bateria) -> bateria.serverTick((net.minecraft.server.level.ServerLevel) mundo));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level nível, BlockPos onde, Player quem, BlockHitResult toque) {
        if (nível.isClientSide()) return InteractionResult.SUCCESS;
        if (!(nível.getBlockEntity(onde) instanceof ManaBatteryBlockEntity bateria)) return InteractionResult.PASS;
        PowerType qual = bateria.powerType();
        float quanto = qual == null ? 0.0f
                : PowerNodes.of((net.minecraft.server.level.ServerLevel) nível).getPower(onde, qual);
        quem.sendSystemMessage(Component.translatable("thaumcraft.power.charge",
                Component.literal(String.format("%.2f", quanto / bateria.capacity() * 100.0f))
                        .withStyle(qual == null ? ChatFormatting.WHITE : qual.color())));
        return InteractionResult.SUCCESS;
    }

    /** Com a chave na mão, o bloco cala-se e deixa a chave trabalhar. */
    @Override
    protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack pilha, BlockState feitio, Level nível,
                                          BlockPos onde, Player quem, net.minecraft.world.InteractionHand mão,
                                          BlockHitResult toque) {
        if (pilha.is(ArcanaItems.CRYSTAL_WRENCH)) return InteractionResult.PASS;
        return super.useItemOn(pilha, feitio, nível, onde, quem, mão, toque);
    }
}
