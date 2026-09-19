package net.thaumcraft.event;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.item.HoverGirdleItem;
import net.thaumcraft.item.HoverHarnessItem;
import net.thaumcraft.item.JarContents;
import net.thaumcraft.registry.TCComponents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * O voo do arreio taumostático: o {@code Hover} e o {@code PacketFlyToServer} da 4.2.3.5.
 *
 * <p>Com o arreio no peito e Potentia no jarro dele, a tecla H liga e desliga o pairar: a pessoa voa como no modo
 * criativo, mais devagar (70% da velocidade, 91% com o cinturão taumostático), e cada 360 tiques no ar gastam um
 * ponto de Potentia (288 com o cinturão). Sem Potentia, o voo desliga sozinho.
 *
 * <p>Como no original, cada lado guarda se a pessoa está pairando: quem joga decide e avisa o servidor. A conta dos
 * tiques até gastar o próximo ponto fica aqui no servidor, e não no arreio; no original ela ia no próprio item.
 */
public final class Hover {
    /** O {@code EFFICIENCY}: tiques de voo por ponto de Potentia. */
    public static final int EFFICIENCY = 360;

    private static final Map<UUID, Boolean> SERVER = new HashMap<>(), CLIENT = new HashMap<>();
    private static final Map<UUID, Integer> CHARGE = new HashMap<>();

    /** O {@code PacketFlyToServer}: quem joga ligou ou desligou o pairar. */
    public record Fly(boolean hover) implements CustomPacketPayload {
        public static final Type<Fly> TYPE = new Type<>(Thaumcraft.id("hover"));
        public static final StreamCodec<ByteBuf, Fly> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, Fly::hover, Fly::new);

        @Override
        public Type<Fly> type() {
            return TYPE;
        }
    }

    private Hover() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Fly.TYPE, Fly.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Fly.TYPE, (payload, context) ->
                context.server().execute(() -> setHover(context.player(), payload.hover())));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) checkWorn(player);
        });
        // no original o estado era pela identidade da entidade, que muda ao entrar de novo: aí vale o do arreio
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            SERVER.remove(handler.player.getUUID());
            CHARGE.remove(handler.player.getUUID());
        });
    }

    private static Map<UUID, Boolean> side(Player player) {
        return player.level().isClientSide() ? CLIENT : SERVER;
    }

    public static void setHover(Player player, boolean hover) {
        side(player).put(player.getUUID(), hover);
    }

    public static boolean getHover(Player player) {
        return side(player).getOrDefault(player.getUUID(), false);
    }

    /** Quanta Potentia o jarro do arreio tem. */
    public static int fuel(ItemStack armor) {
        ItemStack jar = armor.getOrDefault(TCComponents.HARNESS_JAR, ItemStack.EMPTY);
        JarContents contents = jar.get(TCComponents.JAR_CONTENTS);
        return contents != null && contents.heldAspect() == Aspects.ENERGY ? contents.amount() : 0;
    }

    /** Se é um jarro que o arreio aceita: com Potentia dentro. */
    public static boolean isFuel(ItemStack jar) {
        JarContents contents = jar.get(TCComponents.JAR_CONTENTS);
        return contents != null && contents.heldAspect() == Aspects.ENERGY;
    }

    private static boolean girdle(Player player) {
        return Baubles.get(player, Baubles.BELT).getItem() instanceof HoverGirdleItem;
    }

    /**
     * O {@code toggleHover}: liga (só com Potentia) ou desliga. O aviso ao servidor e o som, do lado de quem joga,
     * ficam com o {@code HoverClient}.
     *
     * @return se mudou
     */
    public static boolean toggleHover(Player player, ItemStack armor) {
        boolean hover = getHover(player);
        if (!hover && fuel(armor) <= 0) return false;
        setHover(player, !hover);
        return true;
    }

    /** O {@code handleHoverArmor} do lado do servidor: gasta a Potentia, zera a queda e guarda o estado no arreio. */
    public static void serverTick(ServerPlayer player, ItemStack armor) {
        if (!SERVER.containsKey(player.getUUID()) && armor.has(TCComponents.HOVER)) {
            SERVER.put(player.getUUID(), Boolean.TRUE.equals(armor.get(TCComponents.HOVER)));
        }
        boolean hover = getHover(player);
        player.getAbilities().flying = hover;
        if (hover && expendCharge(player, armor)) {
            player.connection.resetFlyingTicks();
            player.fallDistance = 0.0;
            if (!Boolean.TRUE.equals(armor.get(TCComponents.HOVER))) armor.set(TCComponents.HOVER, true);
        } else {
            if (hover) toggleHover(player, armor);
            player.fallDistance *= 0.75;
            if (Boolean.TRUE.equals(armor.get(TCComponents.HOVER))) armor.set(TCComponents.HOVER, false);
        }
    }

    /** O {@code expendCharge}: um tique de voo; a cada 360 (288 com o cinturão), um ponto de Potentia. */
    public static boolean expendCharge(Player player, ItemStack armor) {
        int fuel = fuel(armor);
        if (fuel <= 0) return false;
        float mod = girdle(player) ? 0.8f : 1.0f;
        int charge = CHARGE.getOrDefault(player.getUUID(), 0);
        if (charge < EFFICIENCY * mod) {
            CHARGE.put(player.getUUID(), charge + 1);
            return true;
        }
        CHARGE.put(player.getUUID(), 0);
        fuel--;
        ItemStack jar = armor.getOrDefault(TCComponents.HARNESS_JAR, ItemStack.EMPTY).copy();
        JarContents old = jar.get(TCComponents.JAR_CONTENTS);
        JarContents now = JarContents.of(fuel > 0 ? Aspects.ENERGY : null, fuel, old == null ? null : old.labelAspect());
        if (now.worthKeeping()) jar.set(TCComponents.JAR_CONTENTS, now);
        else jar.remove(TCComponents.JAR_CONTENTS);
        armor.set(TCComponents.HARNESS_JAR, jar);
        return fuel > 0;
    }

    /** O pedaço do {@code livingTick}: sem o arreio no peito, o pairar acaba. */
    public static void checkWorn(Player player) {
        if (getHover(player) && !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof HoverHarnessItem)) {
            setHover(player, false);
            player.getAbilities().flying = false;
        }
    }

    /** O quanto o voo freia: 0,7, mais 0,075 por nível de Pressa no arreio, mais 0,21 com o cinturão, nunca acima de 1. */
    public static float speed(Player player) {
        int haste = net.thaumcraft.registry.TCEnchantments.level(player.level(), net.thaumcraft.registry.TCEnchantments.HASTE,
                player.getItemBySlot(EquipmentSlot.CHEST));
        float mod = 0.7f + 0.075f * haste;
        if (girdle(player)) mod += 0.21f;
        return Math.min(mod, 1.0f);
    }
}
