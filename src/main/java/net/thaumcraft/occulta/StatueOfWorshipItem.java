package net.thaumcraft.occulta;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * A <b>Estátua de Adoração</b> em item: o {@code ClassItemBlock} do {@code BlockStatueOfWorship}.
 *
 * <p>Ele faz uma coisa só, e é uma ideia boa: põe o <b>nome do dono</b> atrás do nome da estátua. Uma
 * estátua pelada e uma presa a alguém são o mesmo item com a mesma folha, e sem isso não havia como as
 * distinguir num baú — nem como saber, de quatro estátuas guardadas, qual é a sua.
 */
public class StatueOfWorshipItem extends BlockItem {
    public StatueOfWorshipItem(Block qual, Properties properties) {
        super(qual, properties);
    }

    @Override
    public Component getName(ItemStack oquê) {
        Component nome = super.getName(oquê);
        TaglockItem.Taglock dono = TaglockItem.bound(oquê);
        if (dono == null || dono.name().isEmpty()) return nome;
        return Component.empty().append(nome)
                .append(Component.literal(" (" + dono.name() + ")").withStyle(ChatFormatting.GRAY));
    }
}
