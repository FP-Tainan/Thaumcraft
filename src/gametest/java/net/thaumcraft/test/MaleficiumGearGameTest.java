package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.baubles.BaubleType;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.item.Revealing;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.maleficium.FlyteCharmItem;
import net.thaumcraft.maleficium.MaleficiumBaubles;
import net.thaumcraft.maleficium.MaleficiumItems;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.WarpEvents;

/**
 * As roupas do Maleficium: os óculos que revelam, as botas do caminhante, a faixa que liga o empurrão e o amuleto
 * que troca vis por voo.
 */
public class MaleficiumGearGameTest {
    /** Os dois óculos revelam o que está por trás do mundo, como os do Thaumcraft. */
    @GameTest
    public void bothGogglesReveal(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        for (var goggles : new net.minecraft.world.item.Item[]{MaleficiumItems.WARPED_GOGGLES, MaleficiumItems.VOIDMETAL_GOGGLES}) {
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(goggles));
            if (!Revealing.can(player)) helper.fail(goggles + " devia revelar como os óculos do mod");
        }
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        helper.succeed();
    }

    /** O desconto de vis e a distorção de cada peça são os do original. */
    @GameTest
    public void theGearKeepsItsNumbers(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        record Peca(net.minecraft.world.item.Item item, int discount, int warp) {
        }
        for (Peca peca : new Peca[]{
                new Peca(MaleficiumItems.VOIDMETAL_GOGGLES, 12, 5),
                new Peca(MaleficiumItems.WARPED_GOGGLES, 0, 1),
                new Peca(MaleficiumItems.VOIDWALKER_BOOTS, 5, 5),
                new Peca(MaleficiumItems.VOID_FORTRESS_CHESTPLATE, 5, 3),
                new Peca(MaleficiumItems.SHADOW_FORTRESS_CHESTPLATE, 5, 5)}) {
            ItemStack stack = new ItemStack(peca.item());
            int discount = peca.item() instanceof VisDiscountGear gear ? gear.visDiscount(stack, player, null) : -1;
            int warp = peca.item() instanceof WarpEvents.WarpingGear gear ? gear.getWarp(stack, player) : -1;
            if (discount != peca.discount()) helper.fail(peca.item() + " desconta " + discount + ", devia descontar " + peca.discount());
            if (warp != peca.warp()) helper.fail(peca.item() + " distorce " + warp + ", devia distorcer " + peca.warp());
        }
        helper.succeed();
    }

    /** As botas sobem um bloco e pulam um quarto mais alto, como o original. */
    @GameTest
    public void theVoidwalkerBootsClimbAndJump(GameTestHelper helper) {
        var mods = new ItemStack(MaleficiumItems.VOIDWALKER_BOOTS).getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        var base = net.minecraft.world.entity.player.Player.createAttributes().build();
        double degrau = mods.compute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT,
                base.getBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT), EquipmentSlot.FEET);
        if (degrau < 1.0) helper.fail("as botas do caminhante sobem um bloco; sobem " + degrau);
        double pulo = mods.compute(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,
                base.getBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH), EquipmentSlot.FEET);
        if (Math.abs(pulo - 0.42 * 1.25) > 0.001) helper.fail("o pulo devia ser um quarto maior; é " + pulo);
        helper.succeed();
    }

    /** A faixa vai no cinto, dá vinte de escudo rúnico e liga e desliga o empurrão. */
    @GameTest
    public void theSashTogglesTheBoost(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack sash = new ItemStack(MaleficiumItems.VOIDWALKER_SASH);
        if (!(MaleficiumItems.VOIDWALKER_SASH instanceof MaleficiumBaubles.VoidwalkerSashItem item)) {
            helper.fail("a faixa devia ser bijuteria");
            return;
        }
        if (item.baubleType(sash) != BaubleType.BELT) helper.fail("a faixa vai na casa do cinto");
        if (item.runicCharge(sash) != 20) helper.fail("a faixa dá vinte de escudo rúnico");
        if (!MaleficiumBaubles.VoidwalkerSashItem.speedOn(sash)) helper.fail("a faixa vem ligada, como no original");

        Baubles.container(player).setItem(Baubles.BELT, sash);
        if (!MaleficiumBaubles.speedSash(player)) helper.fail("a faixa vestida e ligada devia contar");
        player.setShiftKeyDown(true);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sash);
        sash.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (MaleficiumBaubles.VoidwalkerSashItem.speedOn(sash)) helper.fail("agachar e clicar devia desligar o empurrão");
        helper.succeed();
    }

    /** O amuleto de voo cobra aer de uma varinha do inventário. */
    @GameTest
    public void theFlyteCharmSpendsVis(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        // o jogador de mentira vem no criativo, e no criativo o voo é de graça
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(MaleficiumItems.FLYTE_CHARM));
        if (!FlyteCharmItem.carried(player)) helper.fail("o amuleto devia contar no inventário");
        if (FlyteCharmItem.consume(player, FlyteCharmItem.FLIGHT, false)) {
            helper.fail("sem varinha com vis, não há voo");
        }
        player.getInventory().add(new ItemStack(TCItems.WAND));
        // a varinha que conta é a que ficou no inventário, não a cópia que entrou nele
        ItemStack wand = ItemStack.EMPTY;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(TCItems.WAND)) wand = player.getInventory().getItem(slot);
        }
        WandItem.setVis(wand, new AspectList().add(Aspects.AIR, 100));
        if (!FlyteCharmItem.consume(player, FlyteCharmItem.FLIGHT, true)) helper.fail("com vis de aer, o voo se paga");
        // quinze de aer, mais o que a ponta cobra a mais (a de ferro cobra dez por cento)
        int sobrou = WandItem.vis(wand, Aspects.AIR);
        if (sobrou < 83 || sobrou > 85) helper.fail("o voo cobra quinze de aer; sobrou " + sobrou);
        helper.succeed();
    }
}
