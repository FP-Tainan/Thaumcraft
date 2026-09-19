package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * A pedra e o amuleto de vis: o {@code ItemAmuletVis} da 4.2.3.5. Guardam vis dos seis primários — 25 pontos a
 * pedra, 250 o amuleto — e, vestidos, passam aos poucos para a varinha na mão (até cinco centésimos por aspecto a
 * cada cinco tiques). O amuleto só se veste com a pesquisa dele. Perto de um relé de vis, os dois se enchem com o vis
 * da rede.
 */
public class VisAmuletItem extends Item implements BaubleItem {
    private final boolean greater;

    public VisAmuletItem(boolean greater, Properties properties) {
        super(properties);
        this.greater = greater;
    }

    public int maxVis(ItemStack stack) {
        return this.greater ? 25000 : 2500;
    }

    public static AspectList vis(ItemStack stack) {
        return new AspectList(stack.getOrDefault(TCComponents.WAND_VIS, new AspectList()));
    }

    @Override
    public BaubleType baubleType(ItemStack stack) {
        return BaubleType.AMULET;
    }

    @Override
    public boolean canEquip(ItemStack stack, LivingEntity wearer) {
        return !this.greater || wearer instanceof Player player && net.thaumcraft.research.Knowledges.of(player).hasResearch("VISAMULET");
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer.level().isClientSide() || wearer.tickCount % 5 != 0) return;
        this.feedWand(stack, wearer);
        this.chargeFromRelay(stack, wearer);
    }

    /** Passa até cinco centésimos de cada aspecto para a varinha na mão. */
    private void feedWand(ItemStack stack, LivingEntity wearer) {
        ItemStack wand = wearer.getMainHandItem();
        if (!(wand.getItem() instanceof WandItem)) return;
        AspectList mine = vis(stack);
        boolean moved = false;
        for (Aspect aspect : WandItem.aspectsWithRoom(wand)) {
            int have = mine.getAmount(aspect);
            if (have <= 0) continue;
            int amount = Math.min(5, WandItem.maxVis(wand) - WandItem.vis(wand).getAmount(aspect));
            amount = Math.min(amount, have);
            if (amount <= 0) continue;
            mine.reduce(aspect, amount);
            AspectList wandVis = WandItem.vis(wand);
            wandVis.add(aspect, amount);
            WandItem.setVis(wand, wandVis);
            moved = true;
        }
        if (moved) {
            stack.set(TCComponents.WAND_VIS, mine);
            if (wearer instanceof Player player) net.thaumcraft.baubles.Baubles.touch(player);
        }
    }

    /** Perto de um relé da rede de vis (a menos de cinco blocos), o amuleto puxa dele até cinco centésimos de cada. */
    private void chargeFromRelay(ItemStack stack, LivingEntity wearer) {
        var ref = net.thaumcraft.block.entity.VisRelayBlockEntity.NEARBY_PLAYERS.get(wearer.getUUID());
        if (ref == null) return;
        var relay = ref.get();
        if (relay == null || relay.isRemoved() || relay.getBlockPos().distToCenterSqr(wearer.position()) >= 26.0) {
            net.thaumcraft.block.entity.VisRelayBlockEntity.NEARBY_PLAYERS.remove(wearer.getUUID());
            return;
        }
        AspectList mine = vis(stack);
        boolean charged = false;
        for (Aspect aspect : Aspects.primals()) {
            int room = this.maxVis(stack) - mine.getAmount(aspect);
            if (room <= 0) continue;
            int got = relay.consumeVis(aspect, Math.min(5, room));
            if (got <= 0) continue;
            mine.add(aspect, got);
            relay.triggerConsumeEffect(aspect);
            charged = true;
        }
        if (charged) {
            stack.set(TCComponents.WAND_VIS, mine);
            if (wearer instanceof Player player) net.thaumcraft.baubles.Baubles.touch(player);
        }
    }

    /** O {@code consumeAllVis}: gasta o custo em centésimos, se houver de tudo. */
    public boolean consumeAll(ItemStack stack, Player player, AspectList cost, boolean doIt) {
        if (cost.size() == 0) return false;
        AspectList mine = vis(stack);
        for (Aspect aspect : cost.getAspects()) {
            if (mine.getAmount(aspect) < cost.getAmount(aspect)) return false;
        }
        if (doIt) {
            for (Aspect aspect : cost.getAspects()) mine.reduce(aspect, cost.getAmount(aspect));
            stack.set(TCComponents.WAND_VIS, mine);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        if (!this.greater) lines.accept(Component.translatable("item.thaumcraft.vis_stone.text").withStyle(ChatFormatting.AQUA));
        lines.accept(Component.translatable("item.capacity.text").append(" " + this.maxVis(stack) / 100).withStyle(ChatFormatting.GOLD));
        AspectList mine = vis(stack);
        for (Aspect aspect : Aspects.primals()) {
            if (mine.getAmount(aspect) <= 0) continue;
            String amount = new java.text.DecimalFormat("#######.##").format(mine.getAmount(aspect) / 100.0f);
            lines.accept(Component.literal(" ").append(aspect.name().copy().withStyle(net.minecraft.network.chat.Style.EMPTY
                    .withColor(aspect.color()))).append(" x " + amount));
        }
    }
}
