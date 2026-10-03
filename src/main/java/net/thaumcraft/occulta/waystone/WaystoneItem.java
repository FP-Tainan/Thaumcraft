package net.thaumcraft.occulta.waystone;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.occulta.TaglockItem;

import java.util.function.Consumer;

/**
 * A Pedra de Caminho presa, que diz na mão para onde aponta: o {@code getBoundDisplayName} do
 * {@code ItemGeneral}.
 *
 * <p>Uma pedra presa a um lugar diz o mundo e as três contas dele. Uma pedra presa a alguém diz o nome dessa
 * pessoa — e não diz onde ela está, porque nem ela sabe: a pedra vai dar onde a pessoa estiver na hora.
 */
public class WaystoneItem extends Item {
    public WaystoneItem(Properties properties) {
        super(properties);
    }

    /**
     * O nome do mundo, para se ler na pedra: o {@code provider.getDimensionName()} do original.
     *
     * <p>Em 2014 cada mundo tinha um nome em código — "Overworld", "Nether" — e era esse que a pedra
     * mostrava. Hoje um mundo é uma marca, e a marca crua não se lê bem numa dica; por isso se pergunta por um
     * texto em {@code dimension.<espaço>.<nome>}, e se não houver, se mostra a marca. <b>Declarado no
     * {@code PORTE.md}.</b>
     */
    private static Component nomeDoMundo(Waystones.Lugar onde) {
        var marca = onde.mundo().identifier();
        return Component.translatableWithFallback(
                "dimension." + marca.getNamespace() + "." + marca.getPath(), marca.toString());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        Waystones.Lugar onde = Waystones.lugar(stack);
        if (onde != null) {
            linha.accept(Component.translatable("tc.waystone.bound", nomeDoMundo(onde),
                            onde.onde().getX(), onde.onde().getY(), onde.onde().getZ())
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        var vínculo = TaglockItem.bound(stack);
        if (vínculo != null) {
            linha.accept(Component.translatable("tc.waystone.bound_player", vínculo.name())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
