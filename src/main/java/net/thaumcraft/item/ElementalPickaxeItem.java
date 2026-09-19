package net.thaumcraft.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCSounds;

/**
 * A picareta do núcleo de fogo: o {@code ItemElementalPickaxe} da 4.2.3.5. Põe fogo por dois segundos no que acerta;
 * usada num bloco (custa cinco de uso), mostra por cinco segundos os minérios, a água e a lava em volta, através das
 * paredes; e o minério que ela quebra às vezes cai como aglomerado nativo (o {@code harvestEvent}).
 */
public class ElementalPickaxeItem extends Item {
    public ElementalPickaxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!target.level().isClientSide() && (!(target instanceof Player) || ((net.minecraft.server.level.ServerLevel) target.level()).isPvpAllowed())) {
            target.igniteForSeconds(2.0f);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null) stack.hurtAndBreak(5, player, context.getHand());
        var pos = context.getClickedPos();
        if (!level.isClientSide()) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.WAND_FAIL.value(), SoundSource.PLAYERS,
                    0.2f, 0.2f + level.getRandom().nextFloat() * 0.2f);
        } else if (ToolFx.client != null) {
            ToolFx.client.oreScan(level, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
