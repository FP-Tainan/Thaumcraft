package net.thaumcraft.occulta.brew;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaComponents;

import java.util.List;
import java.util.function.Consumer;

/**
 * O Frasco de Cozimento: o {@code ItemBrew} do Witchery.
 *
 * <p>Ele não tem receita nem nome próprio — o que ele é depende do que estava no caldeirão, e isso vem com ele
 * dentro: a lista dos ingredientes, pela ordem em que caíram. Dela saem o <b>nome</b>, a <b>cor</b>, o que
 * <b>faz</b> em quem bebe e quanto se leva a beber.
 *
 * <p>Dois frascos com os mesmos ingredientes na mesma ordem são a mesma coisa; os mesmos em outra ordem, não.
 */
public class BrewItem extends Item {
    public BrewItem(Properties properties) {
        super(properties);
    }

    /** O que este frasco tem dentro. */
    public static List<Item> contents(ItemStack stack) {
        return stack.getOrDefault(OccultaComponents.BREW, List.of());
    }

    /**
     * Um frasco com aquilo dentro.
     *
     * <p>A cor vai também no componente de tinta do jogo, e é de lá que o desenho do item a tira — é o mesmo
     * caminho que uma armadura de couro tinta faz, e poupa um desenhista só para isto.
     */
    public static ItemStack of(Item frasco, List<Item> dentro) {
        ItemStack stack = new ItemStack(frasco);
        stack.set(OccultaComponents.BREW, List.copyOf(dentro));
        stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(0xFF000000 | Brew.color(dentro)));
        return stack;
    }

    /** A cor do caldo, que o desenhista do item usa para pintar o líquido. */
    public static int color(ItemStack stack) {
        return Brew.color(contents(stack));
    }

    @Override
    public Component getName(ItemStack stack) {
        List<Item> dentro = contents(stack);
        if (dentro.isEmpty()) return super.getName(stack);
        return Brew.name(dentro);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        List<Item> dentro = contents(stack);
        if (dentro.isEmpty()) return;
        Brew.lines(dentro).forEach(linha);
    }

    // ------------------------------------------------------------------ atirar, ou beber

    /**
     * O que a mão faz com o frasco: atira-o, se o cozimento for de atirar; bebe-o, se não for.
     *
     * <p>É a diferença entre a poção e a poção de arremesso do jogo — só que aqui não são dois itens, é o mesmo
     * frasco com uma pólvora a mais dentro.
     */
    @Override
    public net.minecraft.world.InteractionResult use(Level level, Player quem,
                                                     net.minecraft.world.InteractionHand mão) {
        ItemStack frasco = quem.getItemInHand(mão);
        if (!Brew.splash(contents(frasco))) return super.use(level, quem, mão);

        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            var atirado = new BrewProjectile(server, quem, frasco.copyWithCount(1));
            atirado.shootFromRotation(quem, quem.getXRot(), quem.getYRot(), -20.0f, 0.5f, 1.0f);
            server.addFreshEntity(atirado);
        }
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(),
                net.minecraft.sounds.SoundEvents.SPLASH_POTION_THROW, net.minecraft.sounds.SoundSource.PLAYERS,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        quem.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        if (!quem.hasInfiniteMaterials()) frasco.shrink(1);
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ beber

    /**
     * O que o cozimento faz em quem o bebeu.
     *
     * <p>O frasco vazio volta sozinho, porque o item diz que se gasta virando vidro ({@code usingConvertsTo}).
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity quem) {
        List<Item> dentro = contents(stack);
        if (!level.isClientSide()) {
            BrewModifiers temperos = new BrewModifiers();
            // quem bebe é quem fez: um frasco da ressurreição bebido levanta os mortos de quem o bebeu
            if (quem instanceof net.minecraft.world.entity.player.Player gente) temperos.quemFez = gente;
            Brew.apply(level, quem, dentro, temperos);
        }
        return super.finishUsingItem(stack, level, quem);
    }

    /** O {@code getMaxItemUseDuration}: os ingredientes mandam em quanto se leva a beber. */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity quem) {
        return Brew.drinkSpeed(contents(stack));
    }
}
