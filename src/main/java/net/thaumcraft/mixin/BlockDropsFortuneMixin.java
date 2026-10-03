package net.thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A poção da <b>Fortuna</b>: o {@code PotionFortune}, que era um gancho no {@code HarvestDropsEvent} do Forge.
 *
 * <p>O original <b>recalcula</b> o que cai, com um nível de fortuna a mais — ou dois, do quarto grau em
 * diante. Aqui se faz a mesma coisa pelo outro lado: em vez de refazer a queda depois, a <b>ferramenta
 * chega mais encantada</b> ao lugar onde a queda se calcula. Dá o mesmo e não precisa de refazer nada.
 *
 * <p>Como no original, não vale para <b>toque suave</b> nem para o que guarda coisas dentro: um baú com
 * fortuna não dá dois baús.
 */
@Mixin(Block.class)
public abstract class BlockDropsFortuneMixin {
    @ModifyVariable(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;",
            at = @At("HEAD"), argsOnly = true)
    private static ItemInstance thaumcraft$fortune(ItemInstance ferramenta, BlockState qual, ServerLevel level,
                                                   BlockPos onde, BlockEntity guarda, Entity quem,
                                                   ItemInstance mesma) {
        if (guarda != null) return ferramenta;
        if (!(quem instanceof LivingEntity vivo)) return ferramenta;
        if (!(ferramenta instanceof ItemStack naMão) || naMão.isEmpty()) return ferramenta;

        var sorte = vivo.getEffect(OccultaEffects.FORTUNE);
        if (sorte == null) return ferramenta;

        var registro = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var toqueSuave = registro.get(Enchantments.SILK_TOUCH);
        if (toqueSuave.isPresent()
                && EnchantmentHelper.getItemEnchantmentLevel(toqueSuave.get(), naMão) > 0) {
            return ferramenta;
        }

        var fortuna = registro.get(Enchantments.FORTUNE);
        if (fortuna.isEmpty()) return ferramenta;

        int tem = EnchantmentHelper.getItemEnchantmentLevel(fortuna.get(), naMão);
        int soma = sorte.getAmplifier() > 2 ? 2 : 1;

        ItemStack cópia = naMão.copy();
        cópia.enchant(fortuna.get(), tem + soma);
        return cópia;
    }
}
