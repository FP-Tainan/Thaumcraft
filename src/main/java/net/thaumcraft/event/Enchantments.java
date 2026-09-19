package net.thaumcraft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.block.entity.DeconstructionTableBlockEntity;
import net.thaumcraft.item.HoverHarnessItem;
import net.thaumcraft.item.VisAmuletItem;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCEnchantments;

/**
 * O que os encantamentos do Thaumcraft fazem: o {@code updateSpeed} e o {@code doRepair} do {@code EventHandlerEntity}
 * da 4.2.3.5.
 *
 * <p><b>Pressa</b> nas botas: andando para a frente fora do voo, um empurrão de 1,5% por nível (metade no ar, metade
 * na água). <b>Reparo</b>: a cada dois segundos, cada coisa do Thaumcraft gasta no inventário ou vestida conserta um
 * ponto por nível (até dois), pagando em vis — a raiz do dobro de cada primário de que é feita, vezes o nível — de um
 * amuleto de vis vestido ou de uma varinha do inventário. No criativo não conserta.
 */
public final class Enchantments {
    /** As coisas que o original marca como {@code IRepairable}. */
    public static final TagKey<Item> REPAIRABLE = TagKey.create(Registries.ITEM, Thaumcraft.id("repairable"));

    private Enchantments() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.tickCount % 40 != 0 || player.getAbilities().instabuild) continue;
                var inventory = player.getInventory();
                for (int a = 0; a < 36; a++) {
                    ItemStack is = inventory.getItem(a);
                    if (is.getDamageValue() > 0 && is.is(REPAIRABLE) && !(is.getItem() instanceof HoverHarnessItem)) doRepair(is, player);
                }
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                    ItemStack is = player.getItemBySlot(slot);
                    if (is.getDamageValue() > 0 && is.is(REPAIRABLE)) doRepair(is, player);
                }
            }
        });
    }

    /** O custo em vis de consertar esta coisa: a raiz do dobro de cada primário dela, vezes o nível. */
    public static AspectList repairCost(ItemStack is, int level) {
        AspectList cost = ObjectAspects.of(is);
        AspectList out = new AspectList();
        if (cost == null || cost.size() == 0) return out;
        cost = DeconstructionTableBlockEntity.reduceToPrimals(cost);
        for (Aspect a : cost.getAspects()) out.merge(a, (int) Math.sqrt(cost.getAmount(a) * 2) * level);
        return out;
    }

    /** O {@code doRepair}. */
    public static void doRepair(ItemStack is, Player player) {
        int level = Math.min(2, TCEnchantments.level(player.level(), TCEnchantments.REPAIR, is));
        if (level <= 0) return;
        AspectList cost = repairCost(is, level);
        if (cost.size() == 0) return;
        if (consumeVisFromInventory(player, cost)) is.setDamageValue(Math.max(0, is.getDamageValue() - level));
    }

    /** O {@code WandManager.consumeVisFromInventory}: primeiro os amuletos vestidos, depois as varinhas, da última casa para a primeira. */
    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        for (ItemStack worn : net.thaumcraft.baubles.Baubles.of(player).items()) {
            if (worn.getItem() instanceof VisAmuletItem amulet && amulet.consumeAll(worn, player, cost, true)) {
                net.thaumcraft.baubles.Baubles.touch(player);
                return true;
            }
        }
        var inventory = player.getInventory();
        for (int a = 35; a >= 0; a--) {
            ItemStack item = inventory.getItem(a);
            if (item.getItem() instanceof WandItem && WandItem.consume(item, cost, true, player)) return true;
        }
        return false;
    }

    /** A Pressa nas botas, do lado de quem anda (o movimento do jogador é dele). */
    public static void haste(Player player) {
        if (player.getAbilities().flying || player.zza <= 0.0f) return;
        int haste = TCEnchantments.level(player.level(), TCEnchantments.HASTE, player.getItemBySlot(EquipmentSlot.FEET));
        if (haste <= 0) return;
        float bonus = haste * 0.015f;
        if (!player.onGround()) bonus /= 2.0f;
        if (player.isInWater()) bonus /= 2.0f;
        player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
    }
}
