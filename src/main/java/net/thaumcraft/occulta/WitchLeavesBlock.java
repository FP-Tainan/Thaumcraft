package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A folhagem de uma das três árvores do ofício: o {@code BlockWitchLeaves} do Witchery.
 *
 * <p>O que cai dela é o do original, que não é o do carvalho:
 *
 * <ul>
 *   <li>a <b>muda</b> sai uma vez em vinte, e a Fortuna melhora essa conta;</li>
 *   <li>e a da <b>sorveira</b> larga ainda <b>Bagas de Sorveira</b>, uma vez em duzentas.</li>
 * </ul>
 *
 * <p>Com tesoura ou com Toque Suave sai a própria folhagem, como em qualquer folha do jogo — é o que o jogo de
 * hoje faz pela tabela de saque, e aqui se faz à mão, porque a conta das mudas não cabe numa tabela.
 */
public class WitchLeavesBlock extends LeavesBlock {
    /** Uma vez em vinte sai muda, e uma vez em duzentas saem bagas. */
    public static final int SAPLING_CHANCE = 20;
    public static final int BERRY_CHANCE = 200;

    private final Supplier<net.minecraft.world.level.ItemLike> sapling;
    private final boolean berries;

    public WitchLeavesBlock(Properties properties, Supplier<net.minecraft.world.level.ItemLike> sapling,
                            boolean berries) {
        super(0.01f, properties);
        this.sapling = sapling;
        this.berries = berries;
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        throw new UnsupportedOperationException("a folhagem do ofício não vai em estrutura nem em comando");
    }

    /** O original não tinha folha caindo da árvore. */
    @Override
    protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level level,
                                              net.minecraft.core.BlockPos pos, RandomSource random) {
    }

    /** O {@code dropBlockAsItemWithChance} do original, com a Fortuna a melhorar as duas contas. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> saída = new ArrayList<>();
        ServerLevel level = params.getLevel();
        RandomSource sorte = level.getRandom();
        ItemStack ferramenta = params.getOptionalParameter(LootContextParams.TOOL) instanceof ItemStack held
                ? held : ItemStack.EMPTY;

        var encantamentos = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if (EnchantmentHelper.getItemEnchantmentLevel(encantamentos.getOrThrow(Enchantments.SILK_TOUCH), ferramenta) > 0
                || ferramenta.is(net.minecraft.world.item.Items.SHEARS)) {
            saída.add(new ItemStack(this));
            return saída;
        }

        int fortuna = EnchantmentHelper.getItemEnchantmentLevel(
                encantamentos.getOrThrow(Enchantments.FORTUNE), ferramenta);

        int mudas = SAPLING_CHANCE;
        if (fortuna > 0) mudas = Math.max(10, mudas - (2 << fortuna));
        if (sorte.nextInt(mudas) == 0) saída.add(new ItemStack(this.sapling.get()));

        if (this.berries) {
            int bagas = BERRY_CHANCE;
            if (fortuna > 0) bagas = Math.max(40, bagas - (10 << fortuna));
            if (sorte.nextInt(bagas) == 0) saída.add(new ItemStack(OccultaItems.ROWAN_BERRIES));
        }
        return saída;
    }
}
