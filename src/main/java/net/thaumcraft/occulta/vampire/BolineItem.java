package net.thaumcraft.occulta.vampire;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * A <b>Boline</b>: o {@code ItemBoline} do Witchery.
 *
 * <p>É a faca de <b>colher</b> do ofício, e não a de cortar: bate como uma faca de madeira e dura como uma de
 * ferro. O que ela tem de seu é não se <b>gastar</b> no que uma faca de colher corta — folha, teia, grama,
 * trepadeira e fio-armadilha.
 *
 * <p>E faz uma coisa que nenhuma outra faca faz: <b>sacrificar uma galinha sobre o rito do vampiro</b>, para
 * encher o Cálice do sangue que chama Lilith. É um detalhe que o mod nunca explica em lado nenhum, e é por
 * isso que ele é um segredo.
 */
public class BolineItem extends Item {
    public BolineItem(Properties properties) {
        super(properties);
    }

    /** O que ela corta de graça: o que uma faca de colher corta. */
    /**
     * <b>E só ela colhe a Rosa de Sangue.</b>
     *
     * <p>Quebrada de qualquer outro jeito, a rosa não deixa nada. Colhida com a Boline, ela cai inteira — e
     * <b>com quem tem dentro</b>, de modo que se pode arrancar a flor que apanhou alguém e levá-la para
     * casa. É o {@code ItemBoline.onBlockStartBreak} do original.
     *
     * @return se ela era uma rosa e já foi colhida
     */
    public static boolean colheARosa(net.minecraft.server.level.ServerLevel level,
                                     net.minecraft.core.BlockPos onde,
                                     net.minecraft.world.entity.player.Player quem) {
        BlockState oquê = level.getBlockState(onde);
        if (!oquê.is(net.thaumcraft.occulta.OccultaBlocks.BLOOD_ROSE)) return false;

        var rosa = level.getBlockEntity(onde) instanceof net.thaumcraft.occulta.BloodRoseBlockEntity alma
                ? alma.vê() : null;
        net.minecraft.world.item.ItemStack caiu =
                new net.minecraft.world.item.ItemStack(net.thaumcraft.occulta.OccultaItems.BLOOD_ROSE);
        if (rosa != null) {
            caiu.set(net.thaumcraft.occulta.OccultaComponents.TAGLOCK, rosa);
        }

        level.removeBlock(onde, false);
        net.minecraft.world.level.block.Block.popResource(level, onde, caiu);
        return true;
    }

    public static boolean deGraça(BlockState oquê) {
        return oquê.is(net.minecraft.tags.BlockTags.LEAVES)
                || oquê.is(Blocks.COBWEB)
                || oquê.is(Blocks.SHORT_GRASS)
                || oquê.is(Blocks.TALL_GRASS)
                || oquê.is(Blocks.VINE)
                || oquê.is(Blocks.TRIPWIRE);
    }

    @Override
    public boolean mineBlock(ItemStack faca, Level level, BlockState oquê, BlockPos onde,
                             LivingEntity quem) {
        if (deGraça(oquê)) return true;
        return super.mineBlock(faca, level, oquê, onde, quem);
    }

    @Override
    public void appendHoverText(ItemStack faca, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        linha.accept(Component.translatable("tc.boline.tip").withStyle(ChatFormatting.DARK_GREEN));
    }
}
