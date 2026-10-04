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
 * O <b>Chaveiro</b>: a argola em que as chaves do ofício se juntam.
 *
 * <p>Duas chaves fazem um chaveiro; um chaveiro mais uma chave faz um chaveiro maior. Cada porta entra <b>uma
 * vez só</b> — pôr duas chaves da mesma porta na bancada não dobra nada.
 *
 * <p>Ele vale por todas as chaves que tem, e é por isso que existe: quem tem três casas trancadas já não
 * carrega três chaves, carrega uma argola. E quem perde a argola perde as três.
 */
public class KeyringItem extends Item {
    public KeyringItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack oquê, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag flag) {
        for (GlobalPos porta : DoorKeys.doChaveiro(oquê)) {
            linha.accept(DoorKeyItem.diz(porta).withStyle(ChatFormatting.GRAY));
        }
    }
}
