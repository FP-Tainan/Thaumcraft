package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.research.ScanManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * O thaumômetro: aponte para alguma coisa e segure o botão direito para examinar.
 *
 * <p>As contas são as da 4.2.3.5: o exame leva vinte tiques e a mira tem de continuar na mesma coisa o tempo
 * todo — olhou para o lado, começa de novo. Cada coisa só rende ponto na primeira vez; depois o aparelho só
 * mostra do que ela é feita.
 */
public class ThaumometerItem extends Item {
    /**
     * Quanto tempo o botão fica apertado, e quando o exame fecha.
     *
     * <p>No original são vinte e cinco tiques com o exame fechando faltando cinco — um segundo redondo.
     * Aqui ele demora um pouco mais, dois segundos, que dá mais peso ao aparelho; o resto da conta é igual.
     */
    private static final int USE_TICKS = 45;
    private static final int FINISH_AT = 5;
    /** Até onde a mira alcança. */
    private static final double REACH = 16.0;

    /** O que cada jogador estava mirando quando começou; se mudar, o exame recomeça. */
    private static final Map<UUID, String> AIMING = new HashMap<>();

    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) AIMING.put(player.getUUID(), target(level, player));
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
        if (level.isClientSide()) return;

        String now = target(level, player);
        String started = AIMING.get(player.getUUID());
        if (now == null || !now.equals(started)) {
            // mirou noutra coisa: o exame recomeça do zero
            AIMING.put(player.getUUID(), now);
            player.startUsingItem(player.getUsedItemHand());
            return;
        }
        // o tique-taque do aparelho enquanto ele lê
        if (remaining % 2 == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, 0.45f + level.getRandom().nextFloat() * 0.1f);
        }
        if (remaining > FINISH_AT) return;

        player.stopUsingItem();
        AIMING.remove(player.getUUID());
        finish(level, player);
    }

    /** Termina o exame do que estiver na mira. */
    private static void finish(Level level, Player player) {
        Entity creature = ScanManager.entityInSight(level, player, REACH);
        if (creature != null) {
            ScanManager.Result result = ScanManager.scan(player, ScanManager.keyOf(creature),
                    ScanManager.aspectsOf(creature), ScanManager.nameOf(creature));
            tell(player, result);
            return;
        }
        BlockPos pos = ScanManager.blockInSight(player, REACH);
        if (pos == null) return;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        AspectList aspects = ScanManager.aspectsOf(state);
        ScanManager.Result result = ScanManager.scan(player, ScanManager.keyOf(state), aspects,
                ScanManager.nameOf(state));
        tell(player, result);
    }

    private static void tell(Player player, ScanManager.Result result) {
        if (!result.scanned()) {
            if (result.message() != null) player.sendOverlayMessage(result.message());
            return;
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.4f, 1.4f);
        if (!(player instanceof net.minecraft.server.level.ServerPlayer server)) return;
        if (result.aspects().isEmpty()) {
            player.sendOverlayMessage(result.message());
            return;
        }
        // o resumo do canto da tela: o que entrou agora e quanto já se tem
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (var aspect : result.aspects()) tags.add(aspect.tag());
        net.thaumcraft.net.TCNetwork.send(server, new net.thaumcraft.net.TCNetwork.ScanSummary(
                result.message().getString(), tags, result.gained(), result.totals()));
    }

    /** Uma marca do que está na mira agora, para saber se o jogador desviou o olhar. */
    private static String target(Level level, Player player) {
        Entity creature = ScanManager.entityInSight(level, player, REACH);
        if (creature != null) return "e" + creature.getId();
        BlockPos pos = ScanManager.blockInSight(player, REACH);
        if (pos == null) return null;
        return "b" + pos.asLong();
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof Player player) AIMING.remove(player.getUUID());
        return false;
    }

    /** O nome que o aparelho mostra quando não há nada para ler. */
    public static Component nothing() {
        return Component.translatable("tc.scan.nothing");
    }
}
