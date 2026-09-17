package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;

/**
 * O sino do golem: é com ele que se diz ao golem para onde levar o que junta.
 *
 * <p>No original o sino marca as faces dos baús com marcas coloridas, e o golem lê essas marcas. Aqui ele
 * é mais simples: toca-se no baú para guardá-lo no sino, e depois toca-se no golem para dizer que aquela
 * é a casa dele. **Diferença deliberada**, anotada em {@code docs/PORTE.md} — as marcas coloridas pedem
 * uma camada de desenho e de rede que ainda não existe por aqui.
 */
public class GolemBellItem extends Item {
    public GolemBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos at = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(level.getBlockEntity(at) instanceof Container)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            context.getItemInHand().set(TCComponents.GOLEM_HOME, at);
            player.sendSystemMessage(Component.translatable("tc.golem.marked",
                    at.getX(), at.getY(), at.getZ()));
            level.playSound(null, at, TCSounds.WAND.value(), SoundSource.PLAYERS, 0.7f, 1.6f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof GolemEntity golem)) return InteractionResult.PASS;

        BlockPos home = stack.get(TCComponents.GOLEM_HOME);
        if (home == null) {
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("tc.golem.nohome"));
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.level().isClientSide()) {
            golem.setHome(home);
            player.sendSystemMessage(Component.translatable("tc.golem.sent",
                    home.getX(), home.getY(), home.getZ()));
            player.level().playSound(null, golem.blockPosition(), TCSounds.WAND.value(),
                    SoundSource.PLAYERS, 0.7f, 1.2f);
        }
        return InteractionResult.SUCCESS;
    }
}
