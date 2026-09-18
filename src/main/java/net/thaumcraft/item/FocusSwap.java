package net.thaumcraft.item;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * A troca de foco: o {@code WandManager.changeFocus} e o {@code PacketFocusChangeToServer} da 4.2.3.5.
 *
 * <p>Junta, pela chave de ordenação, os focos do inventário e das bolsas; pega o pedido (ou o próximo dele, ou o
 * primeiro) e o prende na varinha da mão, devolvendo o que estava preso para a primeira bolsa com espaço ou, sem
 * bolsa, para o inventário. {@code REMOVE} só tira o foco preso.
 */
public final class FocusSwap {
    public static final String REMOVE = "REMOVE";

    /** O pedido de quem apertou a tecla ou escolheu no menu radial. */
    public record Change(String key) implements CustomPacketPayload {
        public static final Type<Change> TYPE = new Type<>(Thaumcraft.id("focus_change"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Change> CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Change::key, Change::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** As bolsas vestidas: o cinto do Baubles (o original procura nas quatro casas). */
    public static java.util.function.Function<Player, List<ItemStack>> extraPouches = player -> {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack worn : net.thaumcraft.baubles.Baubles.of(player).items()) {
            if (worn.getItem() instanceof FocusPouchItem) out.add(worn);
        }
        return out;
    };

    private FocusSwap() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(Change.TYPE, Change.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Change.TYPE, (payload, context) -> context.server().execute(() -> {
            Player player = context.player();
            ItemStack wand = player.getMainHandItem();
            if (wand.getItem() instanceof WandItem) change(wand, player, payload.key());
        }));
    }

    /** Onde está um foco: numa casa do inventário, ou numa casa de uma bolsa. */
    private record Place(int slot, @Nullable ItemStack pouch, int pouchSlot) {
    }

    /** As bolsas de quem joga: as vestidas primeiro, como no original, depois as do inventário. */
    public static List<ItemStack> pouches(Player player) {
        List<ItemStack> out = new ArrayList<>(extraPouches.apply(player));
        for (int a = 0; a < 36; a++) {
            ItemStack item = player.getInventory().getItem(a);
            if (item.getItem() instanceof FocusPouchItem) out.add(item);
        }
        return out;
    }

    /** Os focos à mão, pela chave de ordenação: o mesmo mapa que o menu radial mostra. */
    public static TreeMap<String, ItemStack> available(Player player) {
        TreeMap<String, ItemStack> out = new TreeMap<>();
        places(player).forEach((key, place) -> out.put(key, stackAt(player, place)));
        return out;
    }

    private static TreeMap<String, Place> places(Player player) {
        TreeMap<String, Place> foci = new TreeMap<>();
        for (ItemStack pouch : extraPouches.apply(player)) addPouch(foci, pouch);
        for (int a = 0; a < 36; a++) {
            ItemStack item = player.getInventory().getItem(a);
            if (item.getItem() instanceof FocusItem focus) foci.put(focus.sortKey(), new Place(a, null, -1));
            if (item.getItem() instanceof FocusPouchItem) addPouch(foci, item);
        }
        return foci;
    }

    private static void addPouch(TreeMap<String, Place> foci, ItemStack pouch) {
        NonNullList<ItemStack> inside = FocusPouchItem.contents(pouch);
        for (int q = 0; q < inside.size(); q++) {
            if (inside.get(q).getItem() instanceof FocusItem focus) foci.put(focus.sortKey(), new Place(-1, pouch, q));
        }
    }

    private static ItemStack stackAt(Player player, Place place) {
        if (place.pouch == null) return player.getInventory().getItem(place.slot).copy();
        return FocusPouchItem.contents(place.pouch).get(place.pouchSlot).copy();
    }

    public static void change(ItemStack wand, Player player, String key) {
        TreeMap<String, Place> foci = places(player);
        List<ItemStack> pouches = pouches(player);
        String had = wand.get(TCComponents.WAND_FOCUS);

        if (!REMOVE.equals(key) && !foci.isEmpty()) {
            String newKey = key;
            if (!foci.containsKey(newKey)) newKey = foci.higherKey(newKey);
            if (newKey == null || !foci.containsKey(newKey)) newKey = foci.firstKey();
            Place place = foci.get(newKey);
            ItemStack item;
            if (place.pouch == null) {
                item = player.getInventory().getItem(place.slot).copy();
                player.getInventory().setItem(place.slot, ItemStack.EMPTY);
            } else {
                NonNullList<ItemStack> inside = FocusPouchItem.contents(place.pouch);
                item = inside.get(place.pouchSlot).copy();
                inside.set(place.pouchSlot, ItemStack.EMPTY);
                FocusPouchItem.setContents(place.pouch, inside);
            }
            if (!(item.getItem() instanceof FocusItem chosen)) return;
            player.level().playSound(null, player.blockPosition(), TCSounds.CAMERA_TICKS.value(), SoundSource.PLAYERS, 0.3f, 1.0f);
            if (had != null && giveBack(player, had, pouches)) {
                wand.remove(TCComponents.WAND_FOCUS);
                had = null;
            }
            if (had == null) wand.set(TCComponents.WAND_FOCUS, chosen.type());
            else if (!addToPouch(item, pouches)) player.getInventory().add(item);
        } else if (had != null && giveBack(player, had, pouches)) {
            wand.remove(TCComponents.WAND_FOCUS);
            player.level().playSound(null, player.blockPosition(), TCSounds.CAMERA_TICKS.value(), SoundSource.PLAYERS, 0.3f, 0.9f);
        }
        player.getInventory().setChanged();
        net.thaumcraft.baubles.Baubles.touch(player);
    }

    /** O foco que estava preso volta para a primeira bolsa com espaço ou para o inventário. */
    private static boolean giveBack(Player player, String type, List<ItemStack> pouches) {
        Item item = Focuses.byType(type);
        if (item == null) return true;
        ItemStack focus = new ItemStack(item);
        return addToPouch(focus, pouches) || player.getInventory().add(focus);
    }

    private static boolean addToPouch(ItemStack focus, List<ItemStack> pouches) {
        for (ItemStack pouch : pouches) {
            NonNullList<ItemStack> inside = FocusPouchItem.contents(pouch);
            for (int q = 0; q < inside.size(); q++) {
                if (!inside.get(q).isEmpty()) continue;
                inside.set(q, focus.copy());
                FocusPouchItem.setContents(pouch, inside);
                return true;
            }
        }
        return false;
    }
}
