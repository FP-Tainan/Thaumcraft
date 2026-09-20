package net.thaumcraft.maleficium;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * O que o Maleficium faz a cada tique: o voo do amuleto e o empurrão da faixa do caminhante do vazio.
 *
 * <p>No original isso morava em ouvintes de evento do Forge ({@code LivingUpdateEvent}, {@code LivingJumpEvent});
 * aqui mora no tique do servidor, que é onde o jogo de hoje deixa mexer nisto.
 */
public final class MaleficiumEvents {
    /** O pulo a mais da faixa ligada: cinco por cento, como no original. */
    private static final AttributeModifier SASH_JUMP = new AttributeModifier(
            Thaumcraft.id("sash_jump"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private MaleficiumEvents() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) tick(player);
        });
    }

    private static void tick(ServerPlayer player) {
        flight(player);
        sash(player);
        voidTouched(player);
    }

    /**
     * O {@code repairItems} do original: tudo o que o frasco de sangue do vazio tocou se conserta sozinho, um ponto
     * por segundo, esteja onde estiver no inventário.
     */
    private static void voidTouched(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isDamaged()) continue;
            if (!Boolean.TRUE.equals(stack.get(net.thaumcraft.registry.TCComponents.VOID_TOUCHED))) continue;
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    /**
     * O {@code updateFlight} do amuleto: com ele no inventário e vis numa varinha, quem o carrega pode voar; o voo
     * cobra quinze de aer de tempos em tempos, e sem vis ele acaba.
     */
    private static void flight(ServerPlayer player) {
        if (player.getAbilities().instabuild || player.isSpectator()) return;
        boolean charm = FlyteCharmItem.carried(player);
        boolean canFly = charm && FlyteCharmItem.consume(player, FlyteCharmItem.FLIGHT, false);
        if (canFly) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            if (player.getAbilities().flying && player.tickCount % 20 == 0) {
                FlyteCharmItem.consume(player, FlyteCharmItem.FLIGHT, true);
            }
            // agachando no ar, ele plana em vez de cair
            if (!player.getAbilities().flying && player.isShiftKeyDown() && !player.onGround()
                    && player.fallDistance > 0.5f && FlyteCharmItem.consume(player, FlyteCharmItem.GLIDE, true)) {
                Vec3 look = Vec3.directionFromRotation(0.0f, player.getYHeadRot());
                player.setDeltaMovement(player.getDeltaMovement().x + look.x * 0.1, -0.1,
                        player.getDeltaMovement().z + look.z * 0.1);
                player.hurtMarked = true;
            }
            return;
        }
        // sem amuleto ou sem vis, o voo é tirado — mas só o que veio dele
        if (charm || !player.getAbilities().mayfly) return;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    /** A faixa ligada dá cinco por cento de pulo, e o modificador some junto com ela. */
    private static void sash(Player player) {
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump == null) return;
        boolean on = MaleficiumBaubles.speedSash(player);
        boolean has = jump.getModifier(SASH_JUMP.id()) != null;
        if (on && !has) jump.addTransientModifier(SASH_JUMP);
        if (!on && has) jump.removeModifier(SASH_JUMP.id());
    }

    /** O mesmo tique, para os testes chamarem sem servidor. */
    public static void tickForTest(ServerPlayer player) {
        tick(player);
    }

    /** O tique das botas, do lado de quem joga: o empurrão para a frente vale no cliente. */
    public static void clientTick(Player player) {
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        if (boots.getItem() instanceof VoidwalkerBootsItem) VoidwalkerBootsItem.tickWorn(player);
    }
}
