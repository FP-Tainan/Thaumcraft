package net.thaumcraft.shattered;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.item.GogglesItem;

import java.util.function.Consumer;

/**
 * Os Óculos do Véu: os Óculos da Descoberta com Fio do Mundo enrolado no aro, e as lentes trocadas pelo vazio.
 *
 * <p>Fazem tudo o que os outros faziam — os nós de aura, o que guardam os recipientes, os cinco por cento de
 * desconto de vis — e mais uma coisa: com eles no rosto, as fendas que já estavam no mundo aparecem.
 *
 * <p><b>Isto é do porte, e não do original:</b> nem o Thaumcraft nem as Portas Dimensionais têm estes óculos. A
 * ideia é de quem joga, e o que ela resolve é um buraco que os dois mods juntos abriam — nas Portas Dimensionais
 * as fendas veem-se desde o primeiro dia, e então nada há para descobrir; aqui elas estão lá desde o primeiro
 * dia mas só se veem depois de se aprender a ver, que é o que o Thaumcraft faz com tudo o mais.
 */
public class VeilGogglesItem extends GogglesItem {
    public VeilGogglesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.translatable("tc.veil_goggles.hint").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
