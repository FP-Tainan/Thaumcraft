package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.thaumcraft.loot.ThaumLoot;
import net.thaumcraft.registry.TCSounds;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * As sacolas de tesouro: o {@code ItemLootBag} da 4.2.3.5, comum, incomum e rara. Abrir uma espalha de oito a doze
 * coisas sorteadas da tabela dela ({@link ThaumLoot}) com o tilintar de moedas.
 */
public class LootBagItem extends Item {
    private final int rarity;

    public LootBagItem(int rarity, Properties properties) {
        super(properties);
        this.rarity = rarity;
    }

    public int rarity() {
        return this.rarity;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tc.lootbag").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int q = 8 + level.getRandom().nextInt(5);
            for (int a = 0; a < q; a++) {
                ItemStack loot = generateLoot(this.rarity, level.getRandom(), level.registryAccess());
                if (!loot.isEmpty()) level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), loot));
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.COINS.value(), SoundSource.PLAYERS, 0.75f, 1.0f);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    /** O {@code Utils.generateLoot}: às vezes uma peça de armadura ou arma, senão uma coisa da tabela da sacola. */
    public static ItemStack generateLoot(int rarity, RandomSource random, RegistryAccess access) {
        ItemStack stack;
        if (rarity > 0 && random.nextFloat() < 0.025f * rarity) {
            stack = genGear(rarity, random, access);
            if (stack.isEmpty()) stack = generateLoot(rarity, random, access);
        } else {
            stack = pick(switch (rarity) {
                case 1 -> ThaumLoot.UNCOMMON;
                case 2 -> ThaumLoot.RARE;
                default -> ThaumLoot.COMMON;
            }, random);
        }
        // o livro sai encantado
        if (stack.is(Items.BOOK)) stack = enchant(stack, (int) (5.0f + rarity * 0.75f * random.nextInt(18)), random, access);
        return stack.copy();
    }

    private static ItemStack pick(List<ThaumLoot.Entry> table, RandomSource random) {
        int total = 0;
        for (ThaumLoot.Entry entry : table) total += entry.weight();
        int at = random.nextInt(total);
        for (ThaumLoot.Entry entry : table) {
            at -= entry.weight();
            if (at < 0) return entry.stack().get();
        }
        return ItemStack.EMPTY;
    }

    /** O {@code genGear}: uma peça de qualidade sorteada, um tanto gasta, às vezes encantada. */
    private static ItemStack genGear(int rarity, RandomSource random, RegistryAccess access) {
        int quality = random.nextInt(2);
        if (random.nextFloat() < 0.2f) quality++;
        if (random.nextFloat() < 0.15f) quality++;
        if (random.nextFloat() < 0.1f) quality++;
        if (random.nextFloat() < 0.095f) quality++;
        Item item = ThaumLoot.gear(random.nextInt(5), quality);
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        if (stack.isDamageableItem()) stack.setDamageValue(random.nextInt(1 + stack.getMaxDamage() / 6));
        if (random.nextInt(4) < rarity) stack = enchant(stack, (int) (5.0f + rarity * 0.75f * random.nextInt(18)), random, access);
        return stack;
    }

    /** O {@code addRandomEnchantment}: o que a mesa de encantamento daria naquele nível. */
    private static ItemStack enchant(ItemStack stack, int level, RandomSource random, RegistryAccess access) {
        var tag = access.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
        return EnchantmentHelper.enchantItem(random, stack, level, access, tag.map(set -> set));
    }
}
