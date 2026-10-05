package net.thaumcraft.occulta.infusion;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * O que a <b>Infusão do Mundo</b> considera <b>metal</b>: a {@code EarthItems} do Witchery.
 *
 * <p>São as vinte e cinco coisas que o original lista à mão — as ferramentas e armaduras de <b>ferro</b> e
 * de <b>ouro</b>, mais os lingotes e a pepita dos dois. Nada de diamante, nada de pedra, nada de couro.
 *
 * <p>Repare no que isso quer dizer para quem leva com a infusão: quem anda de <b>ferro</b> é empurrado e
 * desarmado por ela, e quem anda de <b>diamante</b> ou de <b>couro</b> não. O original fez da armadura boa
 * uma desvantagem, e é a única vez em que ele faz isso.
 *
 * <p>Aqui a lista é um <b>rótulo</b> — {@code thaumcraft:earth_metal} —, de modo que quem jogar pode mexer
 * nela.
 */
public final class EarthMetal {
    /** O rótulo do que é metal para a Infusão do Mundo. */
    public static final TagKey<Item> RÓTULO =
            TagKey.create(Registries.ITEM, Thaumcraft.id("earth_metal"));

    private EarthMetal() {
    }

    /** Se aquilo é metal. */
    public static boolean é(@Nullable ItemStack oquê) {
        return oquê != null && !oquê.isEmpty() && oquê.is(RÓTULO);
    }

    /**
     * E em que lingote aquele minério se funde, ou nada.
     *
     * <p>O original olha o ferro e o ouro à mão e, para o resto, pergunta ao dicionário de minérios do
     * mundo de 2014. Esse dicionário não existe mais: hoje a pergunta é feita aos <b>rótulos</b> que o jogo
     * já tem para os minérios de ferro e de ouro, que é o que ele queria saber.
     */
    public static @Nullable Item lingoteDe(BlockState feitio) {
        if (feitio.is(net.minecraft.tags.BlockTags.IRON_ORES)) return Items.IRON_INGOT;
        if (feitio.is(net.minecraft.tags.BlockTags.GOLD_ORES)) return Items.GOLD_INGOT;
        if (feitio.is(Blocks.COPPER_ORE) || feitio.is(Blocks.DEEPSLATE_COPPER_ORE)) {
            return Items.COPPER_INGOT;
        }
        return null;
    }

    /** E o que fica no lugar do minério fundido: pedra, ou ardósia se o minério era de ardósia. */
    public static Block oQueFica(BlockState feitio) {
        return éFundo(feitio) ? Blocks.DEEPSLATE : Blocks.STONE;
    }

    /** Se o minério é dos de ardósia. */
    private static boolean éFundo(BlockState feitio) {
        return feitio.is(Blocks.DEEPSLATE_IRON_ORE) || feitio.is(Blocks.DEEPSLATE_GOLD_ORE)
                || feitio.is(Blocks.DEEPSLATE_COPPER_ORE);
    }
}
