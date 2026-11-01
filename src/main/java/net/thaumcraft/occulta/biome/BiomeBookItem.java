package net.thaumcraft.occulta.biome;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.thaumcraft.occulta.OccultaComponents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * O <b>Livro de Biomas</b>: o {@code ItemBook} do Witchery.
 *
 * <p>É o <b>escolhedor</b> do ofício, e o único livro do mod que se <b>usa</b> em vez de se ler. Ele traz
 * todos os biomas do mundo de cima, uma folha para cada, com o que se sabe de cada um — se chove, quanto
 * calor faz, se neva, se cai raio — e marcadores na beira para saltar de seção em seção.
 *
 * <p>E a folha em que ele está aberto <b>fica</b>: é dela que sai a {@linkplain BiomeNoteItem Nota de
 * Bioma} quando se junta o livro a uma folha de papel na bancada. O livro não se gasta — volta da bancada
 * inteiro, e com a mesma folha aberta.
 *
 * <p>Sem ele não há como pedir um bioma ao caldeirão. O {@linkplain net.thaumcraft.occulta.rite.ClimateChange
 * rito do clima} escolhe por um foco no chão e dá um bioma qualquer daquele clima; o livro escolhe o bioma
 * <b>exato</b>, e é essa a diferença entre os dois caminhos.
 */
public class BiomeBookItem extends Item {
    public BiomeBookItem(Properties properties) {
        super(properties);
    }

    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1);
    }

    /** Em que bioma ele está aberto. */
    public static @Nullable ResourceKey<Biome> bioma(ItemStack stack) {
        return stack.get(OccultaComponents.BIOME);
    }

    /** E vira-se a folha dele para outro. */
    public static void abre(ItemStack stack, ResourceKey<Biome> qual) {
        stack.set(OccultaComponents.BIOME, qual);
    }

    /**
     * Clicar abre-o.
     *
     * <p>Quem o abre é o <b>lado de cá</b>, pelo mesmo gancho de uso dos outros livros do ramo — e é de lá
     * que a folha escolhida vem de volta, pelo {@link BiomeBookChoice}. O lado de lá não conhece folha
     * nenhuma: ele conhece o bioma que o recado nomeia.
     */
    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        return InteractionResult.SUCCESS;
    }

    /**
     * O {@code addInformation}: a folha em que ele está, e o lembrete de que um papel a copia.
     *
     * <p>É a única dica do mod que <b>ensina uma receita</b>, e está aqui porque sem ela ninguém descobre:
     * juntar um livro a um papel não se parece com nada.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        ResourceKey<Biome> qual = bioma(stack);
        linha.accept(Component.translatable("tc.biomebook.currentpage",
                qual == null ? Component.translatable("tc.biomebook.nopage")
                        : Component.translatable(qual.identifier().toLanguageKey("biome")))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        linha.accept(Component.empty());
        linha.accept(Component.translatable("tc.biomebook.tip")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY,
                        net.minecraft.ChatFormatting.ITALIC));
    }
}
