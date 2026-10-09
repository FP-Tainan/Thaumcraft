package net.thaumcraft.occulta.imp;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.symbol.SymbolKnowledge;

import java.util.function.Consumer;

/**
 * Um <b>Cozimento de Alma</b>: o {@code BrewSoul} do Witchery.
 *
 * <p>Quatro frascos, cada um com a alma de um demônio dentro, e cada um ensina <b>um feitiço</b>:
 *
 * <ul>
 *   <li>o da <b>Fome</b> ensina o <b>Carnosa Diem</b>;</li>
 *   <li>o do <b>Medo</b> ensina o <b>Morsmordre</b>;</li>
 *   <li>o da <b>Angústia</b> ensina o <b>Ignianima</b>;</li>
 *   <li>e o do <b>Tormento</b> ensina o <b>Tormentum</b>.</li>
 * </ul>
 *
 * <p>Bebe-se, e fica sabido <b>para sempre</b> — morrer não o tira. É a única coisa do mod que se aprende
 * assim: todo o resto se destranca com uma receita ou com um rito, e estes quatro destrancam-se
 * <b>engolindo</b>.
 *
 * <p>Três saem do <b>Diabrete</b>, que os troca por coisas brilhantes, e o quarto do <b>Senhor do
 * Tormento</b>. Não há receita para nenhum.
 *
 * @see SymbolKnowledge o que fica escrito em quem bebeu
 */
public class SoulBrewItem extends Item {
    private final String chave;

    public SoulBrewItem(Properties propriedades, String chave) {
        super(propriedades);
        this.chave = chave;
    }

    /** Que feitiço este frasco ensina. */
    public String chave() {
        return this.chave;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack frasco, Level level, LivingEntity quem) {
        ItemStack sobra = super.finishUsingItem(frasco, level, quem);
        if (quem instanceof ServerPlayer gente) {
            SymbolKnowledge.learn(gente, this.chave);
            gente.sendSystemMessage(Component.translatable("tc.occulta.soulbrew.learned",
                    Component.translatable("tc.occulta.spell." + this.chave))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return sobra;
    }

    @Override
    public void appendHoverText(ItemStack frasco, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        linha.accept(Component.translatable("tc.occulta.soulbrew.teaches",
                        Component.translatable("tc.occulta.spell." + this.chave))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
