package net.thaumcraft.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.ScanManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * O thaumômetro: o {@code ItemThaumometer} da 4.2.3.5. Aponte e segure o botão direito: o uso dura vinte e cinco
 * tiques e o exame fecha faltando cinco. A mira tem de ficar no que estava quando o botão desceu — desviou, o exame
 * morre até o próximo clique. O que já foi examinado não se examina de novo: o aparelho nem começa.
 */
public class ThaumometerItem extends Item {
    private static final int USE_TICKS = 25;
    private static final int FINISH_AT = 5;

    /** O {@code startScan}: o que cada jogador mirava ao apertar. Um mapa por lado, que no mundo local os dois dividem a máquina. */
    private static final Map<UUID, String> AIMING_SERVER = new HashMap<>();
    private static final Map<UUID, String> AIMING_CLIENT = new HashMap<>();

    /** As runas do exame; o desenho mora no cliente. */
    public static Runes runes = (level, target) -> {
    };

    public interface Runes {
        void spawn(Level level, ScanManager.Target target);
    }

    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    private static Map<UUID, String> aiming(Level level) {
        return level.isClientSide() ? AIMING_CLIENT : AIMING_SERVER;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ScanManager.Target target = valid(level, player);
        if (target != null) aiming(level).put(player.getUUID(), target.marker());
        else aiming(level).remove(player.getUUID());
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(entity instanceof Player player)) return;
        Map<UUID, String> aiming = aiming(level);
        String started = aiming.get(player.getUUID());
        if (started == null) return;
        ScanManager.Target target = valid(level, player);
        if (target == null || !target.marker().equals(started)) {
            aiming.remove(player.getUUID());
            return;
        }
        if (level.isClientSide()) {
            // as runas que sobem do alvo, a cada tique, e o tique-taque do aparelho
            runes.spawn(level, target);
            if (remaining % 2 == 0) {
                level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        net.thaumcraft.registry.TCSounds.CAMERA_TICKS.value(), SoundSource.PLAYERS,
                        0.2f, 0.45f + level.getRandom().nextFloat() * 0.1f, false);
            }
        }
        if (remaining > FINISH_AT) return;
        aiming.remove(player.getUUID());
        player.stopUsingItem();
        if (!level.isClientSide()) {
            ScanManager.scan(player, target.key(), target.aspects(), target.name(), target.clue());
        }
    }

    /** O alvo, se ainda não foi examinado (o {@code isValidScanTarget}). */
    private static ScanManager.Target valid(Level level, Player player) {
        ScanManager.Target target = ScanManager.target(level, player);
        if (target == null || Knowledges.of(player).hasScanned(target.key())) return null;
        return target;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof Player player) aiming(level).remove(player.getUUID());
        return false;
    }

}
