package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * A Porta Dimensional na mochila, e a regra de onde ela se pode assentar.
 *
 * <p><b>Isto é do porte, e não do original.</b> Nas Portas Dimensionais uma porta assenta-se onde se quiser e
 * abre sempre o mesmo bolso. Quem manda pediu outra coisa: <i>o jogador tem que estabilizar a fenda para
 * conseguir abrir uma porta</i>. Então:
 *
 * <ul>
 *   <li>numa fenda <b>solta</b> — que ainda está a crescer e a comer o mundo — a porta não pega: quem a tenta
 *       assentar leva o aviso e fica com a porta na mão;</li>
 *   <li>numa fenda <b>presa</b> pelo Firma-Fendas, a porta toma-lhe o lugar e o que ela sabia, e fica
 *       {@linkplain RiftBlockEntity#wild() brava} — quem a atravessar cai numa das salas com tema, e dessa sala
 *       saem outras portas para outras salas;</li>
 *   <li>e longe de qualquer fenda, a porta assenta-se como sempre e abre o bolso liso do original.</li>
 * </ul>
 */
public class DimensionalDoorItem extends DoubleHighBlockItem {
    public DimensionalDoorItem(Block bloco, Properties properties) {
        super(bloco, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos onde = context.getClickedPos();

        // o que a fenda sabia tem de ser lido ANTES de a porta lhe tomar o lugar: assim que o bloco troca, o
        // miolo dela vai-se, e com ele o destino e a marca de brava
        RiftBlockEntity antes = level.getBlockEntity(onde) instanceof RiftBlockEntity fenda ? fenda : null;
        if (antes != null && !antes.stabilized()) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendOverlayMessage(
                        Component.translatable("block.thaumcraft.dimensional_door.loose_rift"));
            }
            return InteractionResult.FAIL;
        }

        RiftBlockEntity.Destination destino = antes == null ? null : antes.destination();
        RiftBlockEntity.Destination fonte = antes == null ? null : antes.source();
        String sala = antes == null ? null : antes.room();
        boolean brava = antes != null;

        InteractionResult feito = super.place(context);
        if (!feito.consumesAction()) return feito;

        if (level.getBlockEntity(onde) instanceof RiftBlockEntity nova) {
            nova.setNatural(false);
            if (brava) {
                nova.setWild(true);
                nova.setRoom(sala);
                if (destino != null) nova.setDestination(destino);
                if (fonte != null) nova.setSource(fonte);
                level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8f, 0.6f);
            }
        }
        return feito;
    }
}
