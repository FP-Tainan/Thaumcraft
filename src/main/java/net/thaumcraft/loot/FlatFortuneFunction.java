package net.thaumcraft.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.thaumcraft.Thaumcraft;

import java.util.List;
import java.util.Set;

/**
 * A <b>Fortuna que soma, e não multiplica</b>.
 *
 * <p>Os minérios do Ars Magica 2 contam a Fortuna assim: {@code nextInt(4) + 1 + fortune}. O grau do encanto
 * entra como uma <b>parcela</b>, uma pedra por grau, e não como as voltas de dado que o jogo faz nos minérios
 * dele.
 *
 * <p>Nenhuma das três fórmulas do {@code apply_bonus} do jogo faz isso: a {@code ore_drops} multiplica, a
 * {@code uniform_bonus_count} sorteia de zero até o grau, e a {@code binomial} joga moedas. Por isso esta,
 * que é a conta do original e mais nada.
 *
 * <p>A diferença importa: com Fortuna III, um minério do jogo pode render o quádruplo, e um destes rende
 * sempre três a mais. É menos, e é de propósito — no Ars Magica 2 a Fortuna nos minérios dele vale pouco.
 */
public class FlatFortuneFunction extends LootItemConditionalFunction {
    public static final MapCodec<FlatFortuneFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> commonFields(i)
                    .and(Enchantment.CODEC.fieldOf("enchantment").forGetter(f -> f.enchantment))
                    .apply(i, FlatFortuneFunction::new));

    private final Holder<Enchantment> enchantment;

    private FlatFortuneFunction(List<LootItemCondition> predicates, Holder<Enchantment> enchantment) {
        super(predicates);
        this.enchantment = enchantment;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    protected ItemStack run(ItemStack pilha, LootContext contexto) {
        ItemInstance ferramenta = contexto.getOptionalParameter(LootContextParams.TOOL);
        if (ferramenta == null) return pilha;
        int grau = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, ferramenta);
        if (grau > 0) pilha.grow(grau);
        return pilha;
    }

    public static void init() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Thaumcraft.id("flat_fortune"), MAP_CODEC);
    }
}
