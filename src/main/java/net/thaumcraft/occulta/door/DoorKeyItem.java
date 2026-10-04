package net.thaumcraft.occulta.door;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A <b>Chave</b> de uma porta de sorveira.
 *
 * <p>Ela diz, por baixo do nome, <b>onde</b> fica a porta que abre: o mundo e as três contas. Sem isso ela
 * seria uma chave sem fechadura, e um baú cheio delas não diria nada a ninguém.
 *
 * <p>Ela não se usa: basta <b>tê-la</b>. O original varre o inventário inteiro de quem toca a porta, e é por
 * isso que a chave de casa não precisa de ir para a mão.
 */
public class DoorKeyItem extends Item {
    public DoorKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack oquê, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag flag) {
        GlobalPos porta = DoorKeys.deQuê(oquê);
        if (porta == null) return;
        linha.accept(diz(porta).withStyle(ChatFormatting.GRAY));
    }

    /** Onde a porta fica, pelo nome do mundo e pelas três contas. */
    public static net.minecraft.network.chat.MutableComponent diz(GlobalPos porta) {
        return Component.literal(porta.dimension().identifier().getPath() + ": "
                + porta.pos().getX() + ", " + porta.pos().getY() + ", " + porta.pos().getZ());
    }
}
