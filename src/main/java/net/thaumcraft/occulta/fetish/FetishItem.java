package net.thaumcraft.occulta.fetish;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Um fetiche no baú: o {@code ClassItemBlock} do {@code BlockFetish}.
 *
 * <p>Um espantalho vazio e um espantalho com a <b>Sentinela</b> presa são o mesmo item com a mesma folha.
 * Sem o nome dizer o que ele leva, não havia como os distinguir — e perder um fetiche ligado num baú cheio
 * de fetiches vazios custaria três idas ao outro lado.
 */
public class FetishItem extends BlockItem {
    public FetishItem(Block qual, Properties propriedades) {
        super(qual, propriedades);
    }

    @Override
    public Component getName(ItemStack oquê) {
        SpiritEffects qual = SpiritEffects.byId(SpiritEffects.idOf(oquê));
        if (qual == null) return super.getName(oquê);
        return Component.translatable("tc.fetish.named", super.getName(oquê), qual.name());
    }

    @Override
    public void appendHoverText(ItemStack oquê, Item.TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        var guardado = oquê.get(net.thaumcraft.occulta.OccultaComponents.FETISH_DATA);
        if (guardado == null || guardado.isEmpty()) return;
        int quantos = guardado.players().size() + guardado.types().size() + guardado.creatures().size();
        if (quantos == 0) return;
        linha.accept(Component.translatable("tc.fetish.knows", quantos)
                .withStyle(ChatFormatting.GRAY));
    }
}
