package net.thaumcraft.occulta;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Uma boneca: o {@code ItemPoppet} do Witchery.
 *
 * <p>Uma boneca solta não faz nada. <b>Presa a alguém</b> — com um Frasco de Vínculo, na bancada — ela passa a
 * responder por essa pessoa: quando a morte vem por onde a boneca guarda, é a boneca que morre no lugar dela.
 *
 * <p>Ela só precisa <b>existir</b>: vale na mochila de quem ela guarda, ou numa <b>prateleira de bonecas</b> em
 * qualquer canto do mundo. É o que o original faz, e é o que dá jeito a uma casa de bruxa.
 *
 * <p>As de proteção <b>quebram</b> ao valer; as outras gastam-se aos poucos.
 */
public class PoppetItem extends Item {
    /** O que cada boneca guarda. */
    public enum Kind {
        /** A boneca solta, que ainda não é de ninguém. */
        NONE(false),
        /** Da queda. */
        EARTH(true),
        /** Do afogamento. */
        WATER(true),
        /** Do fogo e do estouro. */
        FIRE(true),
        /** Da fome. */
        HUNGER(true),
        /** Da ferramenta gasta, que ela conserta. */
        TOOL(false),
        /** Da morte, venha ela de onde vier. */
        DEATH(true),
        /** Da armadura gasta. */
        ARMOR(true),
        /** Da boneca de vodu de outrem. */
        VOODOO_PROTECTION(false),
        /** E a de vodu, que fere quem ela tem preso. */
        VOODOO(false);

        /** Se ela se desfaz ao valer, em vez de se gastar aos poucos. */
        public final boolean breaks;

        Kind(boolean breaks) {
            this.breaks = breaks;
        }
    }

    private final Kind kind;

    public PoppetItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** Se esta boneca responde por aquela pessoa. */
    public static boolean isFor(ItemStack stack, LivingEntity quem) {
        return stack.getItem() instanceof PoppetItem && TaglockItem.isFor(stack, quem);
    }

    @Override
    public Component getName(ItemStack stack) {
        var preso = TaglockItem.bound(stack);
        if (preso == null) return super.getName(stack);
        return Component.translatable("item.thaumcraft.poppet.bound", super.getName(stack), preso.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        var preso = TaglockItem.bound(stack);
        if (preso == null) {
            linha.accept(Component.translatable("tc.poppet.unbound")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return;
        }
        linha.accept(Component.translatable("tc.taglock.bound", preso.name())
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
