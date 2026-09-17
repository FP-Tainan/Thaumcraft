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
 * é mais simples: <strong>agachado</strong>, toca-se no baú para guardá-lo no sino, e depois toca-se no
 * golem para dizer que aquela é a casa dele. O agachar não é capricho — um baú abre a tela dele antes de
 * o jogo perguntar ao item o que fazer, e agachar é como o Minecraft deixa o item falar primeiro.
 *
 * <p>A marca fica à vista de quem está com o sino na mão, desenhada por
 * {@link net.thaumcraft.client.render.MarkerOverlay}: uma runa girando sobre o baú marcado e sobre a casa
 * de cada golem por perto.
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

        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            context.getItemInHand().set(TCComponents.GOLEM_HOME, at);
            player.sendSystemMessage(Component.translatable("tc.golem.marked",
                    at.getX(), at.getY(), at.getZ()));
            server.playSound(null, at, TCSounds.WAND.value(), SoundSource.PLAYERS, 0.7f, 1.6f);
            // a marca pega com faísca, para o clique não ser no escuro
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    at.getX() + 0.5, at.getY() + 1.2, at.getZ() + 0.5, 24, 0.4, 0.3, 0.4, 0.4);
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
        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) {
            golem.setHome(home);
            player.sendSystemMessage(Component.translatable("tc.golem.sent",
                    home.getX(), home.getY(), home.getZ()));
            server.playSound(null, golem.blockPosition(), TCSounds.WAND.value(),
                    SoundSource.PLAYERS, 0.7f, 1.2f);
            // um fio de faísca do golem até a casa dele, para se ver a ordem sendo dada
            var de = golem.position().add(0.0, 0.6, 0.0);
            var ate = net.minecraft.world.phys.Vec3.atCenterOf(home).add(0.0, 0.6, 0.0);
            int passos = (int) Math.max(6, de.distanceTo(ate) * 3);
            for (int passo = 0; passo <= passos; passo++) {
                var em = de.lerp(ate, passo / (double) passos);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                        em.x, em.y, em.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
