package net.thaumcraft.occulta.demon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.occulta.TaglockItem;

import java.util.function.Consumer;

/**
 * Um <b>contrato que o Diabrete lança</b>: a {@code ItemGeneralContract} do Witchery.
 *
 * <p>São cinco, e os cinco funcionam da mesma maneira: escrevem-se num <b>Contrato</b> em branco com uma
 * coisa do inferno, prendem-se a alguém com um <b>Frasco de Vínculo</b>, e então se dão <b>ao Diabrete</b> —
 * que os lê e faz o que está escrito <b>na pessoa a quem o papel está preso</b>, esteja ela onde estiver.
 *
 * <p>É a parte do mod que faz do Diabrete o que ele é: ele não é um bicho de estimação com poderes, é um
 * <b>intermediário</b>. O poder não é dele — está no papel —, e o que ele cobra para o usar é afeição e
 * paciência.
 *
 * <p>Ele recusa por quatro motivos, e os quatro têm recado: se não gostar bastante de quem lho dá, se tiver
 * sido há pouco, se não achar a pessoa do outro lado — e se estiver <b>ligado</b>, porque aí há poder demais
 * para pensar.
 */
public class ContractItem extends Item {
    /** O que o papel faz em quem está preso a ele. */
    @FunctionalInterface
    public interface Feitio {
        /**
         * <b>Faz.</b>
         *
         * @return se pegou — e um contrato que não pega <b>não se gasta</b>
         */
        boolean faz(ServerLevel level, LivingEntity emQuem);
    }

    private final Feitio oquê;

    /** A dica dele, que é a linha que o original escreve na mão. */
    private final String dica;

    public ContractItem(Properties properties, Feitio oquê, String dica) {
        super(properties);
        this.oquê = oquê;
        this.dica = dica;
    }

    /** Faz o que está escrito. */
    public boolean faz(ServerLevel level, LivingEntity emQuem) {
        return this.oquê.faz(level, emQuem);
    }

    /** Se aquele papel é um contrato de lançar <b>e está preso a alguém</b>: o {@code isBoundContract}. */
    public static boolean preso(ItemStack papel) {
        return papel.getItem() instanceof ContractItem && TaglockItem.isBound(papel);
    }

    @Override
    public void appendHoverText(ItemStack papel, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        linha.accept(Component.translatable(this.dica).withStyle(ChatFormatting.DARK_RED));
    }
}
