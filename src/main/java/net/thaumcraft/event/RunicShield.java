package net.thaumcraft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.RunicArmor;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.item.RunicBaubleItem;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * O escudo rúnico: o {@code EventHandlerRunic} da 4.2.3.5.
 *
 * <p>As peças rúnicas vestidas (armadura e amuletos) somam cargas. Enquanto o escudo não está cheio, a cada dois
 * segundos (meio segundo a menos por anel carregado) ele ganha uma carga, pagando meio ponto de ar e meio de terra do
 * amuleto de vis ou de uma varinha do inventário. Cada carga absorve um de dano, antes da armadura. Quando quebra, o
 * escudo espera quatro segundos para voltar a carregar, e as variantes fazem o delas: o cinto cinético explode, o anel
 * de cura dá regeneração, o amuleto de emergência recarrega de uma vez.
 */
public final class RunicShield {
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> RUNIC_ARMOR = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM, net.thaumcraft.Thaumcraft.id("runic_armor"));
    /** Os números do {@code Config} do original: recarga em ms, espera depois de quebrar em tiques, custo em centésimos. */
    public static final int SHIELD_RECHARGE = 2000, SHIELD_WAIT = 80, SHIELD_COST = 50;

    /** A carga e o máximo, para o mostrador de quem joga: o {@code PacketRunicCharge}. */
    public record Charge(int charge, int max) implements CustomPacketPayload {
        public static final Type<Charge> TYPE = new Type<>(Thaumcraft.id("runic_charge"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Charge> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Charge::charge, ByteBufCodecs.VAR_INT, Charge::max, Charge::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O clarão do escudo em quem apanhou, voltado para quem bateu: o {@code PacketFXShield}. */
    public record Flash(int source, int target) implements CustomPacketPayload {
        public static final Type<Flash> TYPE = new Type<>(Thaumcraft.id("runic_flash"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Flash> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Flash::source, ByteBufCodecs.VAR_INT, Flash::target, Flash::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** O que o original guardava em mapas pelo número do jogador. */
    private static final class State {
        int charge;
        int lastCharge = -1;
        long nextCycle;
        boolean dirty = true;
        int rechargeDelay;
        /** max, carregados, cinéticos, de cura, de emergência: o {@code runicInfo}. */
        int[] info;
        final Map<Integer, Long> cooldown = new HashMap<>();
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private RunicShield() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(Charge.TYPE, Charge.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Flash.TYPE, Flash.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) tick(player);
        });
    }

    /** O {@code PacketFXShield} para quem estiver perto: o clarão do escudo em quem apanhou (também nos monstros com escudo). */
    public static void flash(net.minecraft.world.entity.LivingEntity entity, int target, double range) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        Flash flash = new Flash(entity.getId(), target);
        for (ServerPlayer near : PlayerLookup.around(level, entity.position(), range)) ServerPlayNetworking.send(near, flash);
    }

    /** O {@code getFinalCharge}: as cargas da peça mais o endurecimento da infusão rúnica. */
    public static int finalCharge(ItemStack stack) {
        if (!isRunic(stack)) return 0;
        int base = stack.getItem() instanceof RunicArmor armor ? armor.runicCharge(stack) : 0;
        return base + stack.getOrDefault(TCComponents.RUNIC_HARDEN, 0);
    }

    /** As peças que no original são {@code IRunicArmor}: as que têm carga própria e as da etiqueta {@code runic_armor}. */
    public static boolean isRunic(ItemStack stack) {
        return stack.getItem() instanceof RunicArmor || stack.is(RUNIC_ARMOR);
    }

    private static State state(Player player) {
        return STATES.computeIfAbsent(player.getUUID(), id -> new State());
    }

    /** As cargas que o escudo tem agora. */
    public static int charge(Player player) {
        return state(player).charge;
    }

    /** Marca que as peças mudaram: a conta refaz no próximo tique (o {@code isDirty}). */
    public static void markDirty(Player player) {
        state(player).dirty = true;
    }

    /** O {@code livingTick}. */
    public static void tick(ServerPlayer player) {
        State s = state(player);
        if (s.dirty || player.tickCount % 40 == 0) {
            s.dirty = false;
            int max = 0, charged = 0, kinetic = 0, healing = 0, emergency = 0;
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                max += finalCharge(player.getItemBySlot(slot));
            }
            for (ItemStack worn : Baubles.of(player).items()) {
                if (!isRunic(worn)) continue;
                if (worn.getItem() instanceof RunicBaubleItem runic) {
                    switch (runic.kind()) {
                        case CHARGED -> charged++;
                        case HEALING -> healing++;
                        case EMERGENCY -> emergency++;
                        case KINETIC -> kinetic++;
                        default -> {
                        }
                    }
                }
                max += finalCharge(worn);
            }
            if (max > 0) {
                s.info = new int[]{max, charged, kinetic, healing, emergency};
                if (s.charge > max) {
                    s.charge = max;
                    send(player, s.charge, max);
                }
            } else {
                s.info = null;
                s.charge = 0;
                send(player, 0, 0);
            }
        }

        if (s.rechargeDelay > 0) {
            s.rechargeDelay--;
        } else if (s.info != null) {
            long time = System.currentTimeMillis();
            if (s.charge > s.info[0]) {
                s.charge = s.info[0];
            } else if (s.charge < s.info[0] && s.nextCycle < time
                    && consumeVisFromInventory(player, new AspectList().add(Aspects.AIR, SHIELD_COST).add(Aspects.EARTH, SHIELD_COST))) {
                s.nextCycle = time + SHIELD_RECHARGE - s.info[1] * 500L;
                s.charge++;
            }
            if (s.lastCharge != s.charge) {
                send(player, s.charge, s.info[0]);
                s.lastCharge = s.charge;
            }
        }
    }

    /** O {@code WandManager.consumeVisFromInventory}: primeiro o amuleto de vis vestido, depois as varinhas, de trás para a frente. */
    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        for (ItemStack worn : Baubles.of(player).items()) {
            if (worn.getItem() instanceof net.thaumcraft.item.VisAmuletItem amulet && amulet.consumeAll(worn, player, cost, true)) {
                Baubles.touch(player);
                return true;
            }
        }
        for (int a = player.getInventory().getNonEquipmentItems().size() - 1; a >= 0; a--) {
            ItemStack item = player.getInventory().getItem(a);
            if (item.getItem() instanceof WandItem && WandItem.consumeRaw(item, cost, true, player)) return true;
        }
        return false;
    }

    private static void send(ServerPlayer player, int charge, int max) {
        ServerPlayNetworking.send(player, new Charge(charge, max));
    }

    /**
     * O trecho do escudo no {@code entityHurt}: o dano que sobra depois de o escudo absorver o que pode. Chamado pelo
     * jogador antes da armadura.
     */
    public static float absorb(Player player, DamageSource source, float amount) {
        if (!(player instanceof ServerPlayer serverPlayer) || amount <= 0.0f) return amount;
        // afogamento, Wither, vazio e fome passam direto
        if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER) || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.STARVE)) {
            return amount;
        }
        State s = state(player);
        if (s.info == null || s.charge <= 0) return amount;
        long time = System.currentTimeMillis();
        int target = -1;
        if (source.getEntity() != null) target = source.getEntity().getId();
        if (source.is(DamageTypes.FALL)) target = -2;
        if (source.is(DamageTypes.FALLING_BLOCK)) target = -3;
        ServerLevel level = serverPlayer.level();
        Flash flash = new Flash(player.getId(), target);
        for (ServerPlayer near : PlayerLookup.around(level, player.position(), 64.0)) ServerPlayNetworking.send(near, flash);

        int charge = s.charge;
        if (charge > amount) {
            charge = (int) (charge - amount);
            amount = 0.0f;
        } else {
            amount -= charge;
            charge = 0;
        }
        if (charge <= 0 && s.info[2] > 0 && ready(s, 2, time, 20000L)) {
            level.explode(player, player.getX(), player.getY() + player.getBbHeight() / 2.0f, player.getZ(),
                    1.5f + s.info[2] * 0.5f, false, Level.ExplosionInteraction.NONE);
        }
        if (charge <= 0 && s.info[3] > 0 && ready(s, 3, time, 20000L)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, s.info[3]));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.RUNIC_SHIELD_EFFECT.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (charge <= 0 && s.info[4] > 0 && ready(s, 4, time, 60000L)) {
            charge = Math.min(s.info[0], 8 * s.info[4]);
            s.dirty = true;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.RUNIC_SHIELD_CHARGE.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (charge <= 0) s.rechargeDelay = SHIELD_WAIT;
        s.charge = charge;
        send(serverPlayer, charge, s.info[0]);
        return amount;
    }

    /** As esperas das variantes, cada uma com o seu relógio. */
    private static boolean ready(State s, int kind, long time, long wait) {
        Long until = s.cooldown.get(kind);
        if (until != null && until >= time) return false;
        s.cooldown.put(kind, time + wait);
        return true;
    }
}
