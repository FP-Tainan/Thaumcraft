package net.thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.crafting.SpecialMining;
import net.thaumcraft.item.ElementalPickaxeItem;
import net.thaumcraft.item.PrimalCrusherItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * O {@code harvestEvent} do {@code EventHandlerWorld}: com a picareta elemental ou o triturador primordial, o minério às
 * vezes cai como aglomerado nativo (vinte por cento, mais sete e meio por nível de fortuna), com o tinido de experiência.
 */
@Mixin(Block.class)
public abstract class BlockDropsMiningMixin {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
            at = @At("RETURN"))
    private static void thaumcraft$specialMining(BlockState state, ServerLevel level, BlockPos pos, BlockEntity be, Entity entity, ItemInstance tool,
                                                 CallbackInfoReturnable<List<ItemStack>> info) {
        if (!(tool instanceof ItemStack held)) return;
        // os encantamentos sombrios do Forbidden Magic mexem no que cai, e não dependem da ferramenta
        net.thaumcraft.forbidden.ForbiddenEnchantments.onBlockDrops(info.getReturnValue(), level, pos, held);
        if (!(held.getItem() instanceof ElementalPickaxeItem || held.getItem() instanceof PrimalCrusherItem)) return;
        if (entity == null) return;
        var fortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE);
        int level_ = fortune.map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, held)).orElse(0);
        float chance = 0.2f + level_ * 0.075f;
        List<ItemStack> drops = info.getReturnValue();
        for (int a = 0; a < drops.size(); a++) {
            ItemStack is = drops.get(a);
            ItemStack smr = SpecialMining.refine(is, chance, level.getRandom());
            if (!ItemStack.isSameItem(is, smr)) {
                drops.set(a, smr);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS,
                        0.2f, 0.7f + level.getRandom().nextFloat() * 0.2f);
            }
        }
    }
}
