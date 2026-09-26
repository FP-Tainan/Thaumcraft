package net.thaumcraft.occulta;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

/**
 * O que cai de cada planta quando se colhe: o {@code getDrops} do {@code BlockWitchCrop}.
 *
 * <p>A conta do original, que não é a do trigo:
 *
 * <ul>
 *   <li>planta ainda verde: cai uma semente e mais nada;</li>
 *   <li>planta feita: <b>três tentativas de semente</b> (mais uma por nível de fortuna), cada uma com oito
 *       chances em quinze, e depois uma colheita;</li>
 *   <li>a <b>mindrake</b> foge da regra: um bulbo sempre, e outro uma vez em quatro — o bulbo é ao mesmo tempo o
 *       que se planta e o que se colhe, como no original;</li>
 *   <li>a <b>campainha-de-neve</b> dá ainda uma Agulha de Gelo uma vez em cinco;</li>
 *   <li>e a <b>mandrágora</b> é outra história — está no {@link #escapa}.</li>
 * </ul>
 *
 * <p><b>Por que isto não vai numa tabela de saque:</b> uma tabela de dados não sabe contar "três tentativas de
 * oito em quinze", nem olhar a hora do dia. O original conta isto em código, e é o que se faz aqui.
 */
public final class OccultaCrops {
    /** Quantas tentativas de semente uma planta feita dá, e a chance de cada uma, em quinze. */
    public static final int SEED_TRIES = 3;
    public static final int SEED_CHANCE = 8;

    /** A mindrake dá o segundo bulbo uma vez em quatro. */
    public static final int MINDRAKE_AGAIN = 4;

    /** E a campainha-de-neve dá a Agulha de Gelo em uma colheita de cada cinco. */
    public static final double ICY_NEEDLE = 0.2;

    /**
     * De dia a mandrágora quase sempre escapa de quem a colhe; de noite, quase nunca.
     *
     * <p>É o que obriga quem joga a colhê-la à noite, e são os números do original: lá a conta diz quando ela
     * <i>não</i> escapa — nove décimos de dia e um décimo de noite.
     */
    public static final double ESCAPE_BY_DAY = 0.9;
    public static final double ESCAPE_BY_NIGHT = 0.1;

    private OccultaCrops() {
    }

    /** O que aquela planta larga naquele estado, lendo a fortuna da ferramenta de quem a colheu. */
    public static List<ItemStack> drops(Block planta, BlockState estado, LootParams.Builder params) {
        ServerLevel level = params.getLevel();
        int fortuna = 0;
        if (params.getOptionalParameter(LootContextParams.TOOL) instanceof ItemStack ferramenta) {
            fortuna = EnchantmentHelper.getItemEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getOrThrow(Enchantments.FORTUNE), ferramenta);
        }
        return drops(planta, estado, level, level.getRandom(), fortuna);
    }

    /**
     * A conta em si, à parte do saque para os testes a poderem chamar.
     *
     * @param level   de onde saem a dificuldade e a hora do dia, que a mandrágora consulta
     * @param sorte   o acaso
     * @param fortuna quantos níveis de Fortuna a ferramenta tem
     */
    public static List<ItemStack> drops(Block planta, BlockState estado, ServerLevel level, RandomSource sorte,
                                        int fortuna) {
        List<ItemStack> saída = new ArrayList<>();
        if (!(planta instanceof WitchCropBlock crop)) return saída;
        Item semente = crop.getBaseSeedId().asItem();

        if (crop.getAge(estado) < crop.getMaxAge()) {
            saída.add(new ItemStack(semente));
            return saída;
        }

        if (escapa(planta, level, sorte)) return saída;

        if (planta == OccultaBlocks.MINDRAKE) {
            saída.add(new ItemStack(semente));
            if (sorte.nextInt(MINDRAKE_AGAIN) == 0) saída.add(new ItemStack(semente));
            return saída;
        }

        for (int volta = 0; volta < SEED_TRIES + fortuna; volta++) {
            if (sorte.nextInt(15) <= SEED_CHANCE - 1) saída.add(new ItemStack(semente));
        }
        saída.add(new ItemStack(harvest(planta)));
        if (planta == OccultaBlocks.SNOWBELL && sorte.nextDouble() <= ICY_NEEDLE) {
            saída.add(new ItemStack(OccultaItems.ICY_NEEDLE));
        }
        return saída;
    }

    /**
     * O que cada planta feita dá, além das sementes.
     *
     * <p>A mindrake e o alho dão a própria semente — no original o item de colheita delas é nulo, e o mod copia o
     * de semente.
     */
    public static Item harvest(Block planta) {
        if (planta == OccultaBlocks.BELLADONNA) return OccultaItems.BELLADONNA_FLOWER;
        if (planta == OccultaBlocks.MANDRAKE) return OccultaItems.MANDRAKE_ROOT;
        if (planta == OccultaBlocks.WATER_ARTICHOKE) return OccultaItems.WATER_ARTICHOKE_GLOBE;
        if (planta == OccultaBlocks.SNOWBELL) return Items.SNOWBALL;
        if (planta == OccultaBlocks.WORMWOOD) return OccultaItems.WORMWOOD_SPRIG;
        if (planta == OccultaBlocks.WOLFSBANE) return OccultaItems.WOLFSBANE_SPRIG;
        if (planta == OccultaBlocks.MINDRAKE) return OccultaItems.MINDRAKE_BULB;
        return OccultaItems.GARLIC;
    }

    /**
     * A mandrágora escapou de quem a colheu?
     *
     * <p>No original, uma mandrágora arrancada fora de hora não se deixa apanhar: ela sai do chão e vai embora
     * gritando. De dia isso é quase certo; de noite, raro. Em paz nunca acontece, porque o que escapa é bicho.
     *
     * <p><b>Do original fica de fora, por enquanto,</b> a mandrágora que anda e grita — é criatura, e vem em
     * fatia própria, com o estouro de partículas que o mod manda a quem está perto. Enquanto ela não chega, a que
     * escapa apenas não deixa nada no chão.
     */
    public static boolean escapa(Block planta, ServerLevel level, RandomSource sorte) {
        if (planta != OccultaBlocks.MANDRAKE) return false;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        return sorte.nextDouble() <= (level.isBrightOutside() ? ESCAPE_BY_DAY : ESCAPE_BY_NIGHT);
    }
}
