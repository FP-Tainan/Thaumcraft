package net.thaumcraft.occulta.infusion;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Um <b>Cozimento Infundido</b> na mão: o {@code InfusedBrew} do Witchery.
 *
 * <p>Bebe-se, e o que fica não é uma poção — é uma {@linkplain InfusedBrews condição} que dura duas horas.
 * O frasco não dá efeito nenhum que se veja na lista; o que ele faz vê-se no mundo.
 *
 * <p>Beber um <b>apaga o outro</b>: não se anda com os dois ao mesmo tempo, e é assim no original — o
 * estado é um número só.
 */
public class InfusedBrewItem extends Item {
    private final int qual;

    public InfusedBrewItem(Properties properties, int qual) {
        super(properties);
        this.qual = qual;
    }

    /** Qual dos dois este frasco é. */
    public int qual() {
        return this.qual;
    }

    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1)
                .food(new net.minecraft.world.food.FoodProperties.Builder()
                        .nutrition(0).saturationModifier(0.0f).alwaysEdible().build())
                .usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack coisa, Level level, LivingEntity quem) {
        if (!level.isClientSide() && quem instanceof Player gente) {
            InfusedBrews.bebe(gente, this.qual);
        }
        return super.finishUsingItem(coisa, level, quem);
    }

    /** E a dica diz quanto dura, que é o que o separa de qualquer outro frasco. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        linha.accept(Component.translatable("tc.infusedbrew.lasts", InfusedBrews.DURA / 1200L)
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY,
                        net.minecraft.ChatFormatting.ITALIC));
    }
}
