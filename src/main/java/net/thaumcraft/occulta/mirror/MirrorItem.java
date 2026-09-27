package net.thaumcraft.occulta.mirror;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;

import java.util.function.Consumer;

/**
 * O Espelho em item: o {@code ItemMirror} do Witchery.
 *
 * <p>Assenta-se numa <b>parede</b>, e ocupa dois blocos: o que se clicou e o de baixo. Ele leva consigo a ligação
 * que já tivesse — por isso um espelho arrancado e posto noutro canto continua a dar para a mesma cela.
 *
 * <p>Na descrição ele diz o que é: <b>habitado</b>, enquanto o Reflexo dele viver, ou <b>vazado</b>, depois.
 */
public class MirrorItem extends Item {
    public MirrorItem(Properties properties) {
        super(properties);
    }

    /** O {@code onItemUse} do original: o espelho vai para o bloco do lado da parede em que se bateu. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction lado = context.getClickedFace();
        if (lado.getAxis().isVertical()) return InteractionResult.FAIL;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        BlockPos alto = context.getClickedPos().relative(lado);
        BlockPos baixo = alto.below();
        if (!level.getBlockState(alto).isAir() || !level.getBlockState(baixo).isAir()) {
            return InteractionResult.FAIL;
        }
        var quem = context.getPlayer();
        if (quem != null && (!quem.mayUseItemAt(alto, lado, context.getItemInHand())
                || !quem.mayUseItemAt(baixo, lado, context.getItemInHand()))) {
            return InteractionResult.FAIL;
        }

        BlockState espelho = OccultaBlocks.WITCH_MIRROR.defaultBlockState()
                .setValue(MirrorBlock.FACING, lado);
        server.setBlock(alto, espelho.setValue(MirrorBlock.HALF, DoubleBlockHalf.UPPER), 3);
        if (!server.getBlockState(alto).is(OccultaBlocks.WITCH_MIRROR)) return InteractionResult.FAIL;
        server.setBlock(baixo, espelho.setValue(MirrorBlock.HALF, DoubleBlockHalf.LOWER), 3);
        if (server.getBlockEntity(alto) instanceof MirrorBlockEntity alma) {
            alma.readFromItem(server, context.getItemInHand());
        }
        context.getItemInHand().consume(1, quem);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack item, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        MirrorLink.Held trazia = item.getOrDefault(OccultaComponents.MIRROR, MirrorLink.Held.EMPTY);
        if (trazia.hollow()) {
            linha.accept(Component.translatable("tc.mirror.hollow").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            linha.accept(Component.translatable("tc.mirror.inhabited").withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
