package net.thaumcraft.baubles;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.baubles.BaubleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * As quatro casas do Baubles 1.0.1.10 — amuleto, anel, anel e cinto —, presas ao jogador e salvas com ele (o
 * {@code InventoryBaubles} + {@code PlayerHandler}). Vão junto para a máquina de quem joga, como o
 * {@code PacketSyncBauble}.
 */
public final class Baubles {
    public static final int SIZE = 4;
    public static final int AMULET = 0, RING_1 = 1, RING_2 = 2, BELT = 3;

    /** O que está vestido, casa por casa. */
    public record Worn(List<ItemStack> items) {
        public static final Codec<Worn> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(Worn::new, Worn::items);
        public static final StreamCodec<RegistryFriendlyByteBuf, Worn> STREAM_CODEC =
                ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()).map(Worn::new, Worn::items);

        public static Worn empty() {
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < SIZE; i++) list.add(ItemStack.EMPTY);
            return new Worn(list);
        }

        public ItemStack get(int slot) {
            return slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
        }
    }

    public static final AttachmentType<Worn> WORN = AttachmentRegistry.<Worn>builder()
            .initializer(Worn::empty)
            .persistent(Worn.CODEC)
            .syncWith(Worn.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("baubles"));

    private Baubles() {
    }

    public static void init() {
    }

    public static Worn of(Player player) {
        return player.getAttachedOrCreate(WORN);
    }

    public static ItemStack get(Player player, int slot) {
        return of(player).get(slot);
    }

    /** Guarda uma casa (uma lista nova, para o anexo perceber a mudança e mandar para a máquina de quem joga). */
    static void put(Player player, int slot, ItemStack stack) {
        List<ItemStack> list = new ArrayList<>(of(player).items());
        while (list.size() < SIZE) list.add(ItemStack.EMPTY);
        list.set(slot, stack);
        player.setAttached(WORN, new Worn(list));
    }

    /** Manda de novo as casas para a máquina de quem joga, depois de mexer por dentro de uma peça vestida. */
    public static void touch(Player player) {
        player.setAttached(WORN, new Worn(new ArrayList<>(of(player).items())));
    }

    /** As casas vistas como um inventário, para as telas: o {@code InventoryBaubles}, com os avisos de vestir e tirar. */
    public static Container container(Player player) {
        return new Container() {
            @Override
            public int getContainerSize() {
                return SIZE;
            }

            @Override
            public boolean isEmpty() {
                for (ItemStack stack : of(player).items()) if (!stack.isEmpty()) return false;
                return true;
            }

            @Override
            public ItemStack getItem(int slot) {
                return Baubles.get(player, slot);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack had = Baubles.get(player, slot);
                if (had.isEmpty() || amount <= 0) return ItemStack.EMPTY;
                ItemStack left = had.copy();
                ItemStack taken = left.split(amount);
                if (taken.getItem() instanceof BaubleItem bauble) bauble.onUnequipped(taken, player);
                put(player, slot, left.isEmpty() ? ItemStack.EMPTY : left);
                return taken;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack had = Baubles.get(player, slot);
                put(player, slot, ItemStack.EMPTY);
                return had;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                ItemStack had = Baubles.get(player, slot);
                if (!had.isEmpty() && had.getItem() instanceof BaubleItem bauble) bauble.onUnequipped(had, player);
                put(player, slot, stack);
                if (!stack.isEmpty() && stack.getItem() instanceof BaubleItem bauble) bauble.onEquipped(stack, player);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player who) {
                return true;
            }

            @Override
            public void clearContent() {
                player.setAttached(WORN, Worn.empty());
            }
        };
    }

    /** O tique das peças vestidas, o {@code EventHandlerEntity.playerTick} do Baubles, dos dois lados. */
    public static void tick(Player player) {
        List<ItemStack> items = of(player).items();
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() instanceof BaubleItem bauble) bauble.onWornTick(stack, player);
        }
    }

    /** O {@code playerDeath}: sem a regra de guardar o inventário, as peças caem com o resto. */
    public static void dropOnDeath(Player player) {
        List<ItemStack> items = of(player).items();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) player.drop(stack.copy(), true, false);
        }
        player.setAttached(WORN, Worn.empty());
    }
}
