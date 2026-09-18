package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCItems;

/** Os descontos de vis do que se veste têm de somar como o {@code getTotalVisDiscount} do original. */
public class EquipmentGameTest {
    @GameTest
    public void gogglesAndRobesTakeTenPercentOffTheWand(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        float bare = WandItem.modifier(wand, player, Aspects.FIRE);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.GOGGLES));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.ROBE_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.ROBE_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.ROBE_BOOTS));
        float dressed = WandItem.modifier(wand, player, Aspects.FIRE);
        if (Math.abs(bare - dressed - 0.10f) > 0.001f) helper.fail("óculos 5 + manto 2 + calça 2 + botas 1 = 10%; deu " + (bare - dressed));
        helper.succeed();
    }

    @GameTest
    public void theFullFortressSetTakesEightyPercentOff(GameTestHelper helper) {
        var zombie = helper.makeMockServerPlayerInLevel();
        zombie.setGameMode(GameType.SURVIVAL);
        var attacker = helper.makeMockPlayer(GameType.SURVIVAL);
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.FORTRESS_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.FORTRESS_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.FORTRESS_LEGGINGS));
        float taken;
        try {
            var absorb = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("getDamageAfterArmorAbsorb",
                    net.minecraft.world.damagesource.DamageSource.class, float.class);
            absorb.setAccessible(true);
            taken = (float) absorb.invoke(zombie, helper.getLevel().damageSources().playerAttack(attacker), 10.0f);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        // o golpe de outro jogador (que não muda com a dificuldade): (3 + 7 + 6) / 25 × (0,875 + 3 × 0,125) = 0,8: dos 10, passam 2
        if (Math.abs(taken - 2.0f) > 0.01f) helper.fail("a fortaleza completa deixa passar 2 de 10; passou " + taken);
        helper.succeed();
    }

    @GameTest
    public void theFocusKeySwapsThroughInventoryAndPouch(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        player.getInventory().setItem(0, wand);
        player.getInventory().setSelectedSlot(0);
        player.getInventory().setItem(5, new ItemStack(TCItems.FOCI.get("fire")));
        ItemStack pouch = new ItemStack(TCItems.FOCUS_POUCH);
        var inside = net.minecraft.core.NonNullList.withSize(18, ItemStack.EMPTY);
        inside.set(0, new ItemStack(TCItems.FOCI.get("frost")));
        net.thaumcraft.item.FocusPouchItem.setContents(pouch, inside);
        player.getInventory().setItem(6, pouch);

        // pedir qualquer coisa antes de "AF" pega o primeiro: o fogo, do inventário
        net.thaumcraft.item.FocusSwap.change(wand, player, "");
        if (!"fire".equals(wand.get(net.thaumcraft.registry.TCComponents.WAND_FOCUS))) helper.fail("o primeiro foco é o de fogo");
        if (!player.getInventory().getItem(5).isEmpty()) helper.fail("o foco de fogo sai do inventário");
        // pedir o de gelo tira da bolsa, e o de fogo vai para a casa vazia da bolsa
        net.thaumcraft.item.FocusSwap.change(wand, player, "BF");
        if (!"frost".equals(wand.get(net.thaumcraft.registry.TCComponents.WAND_FOCUS))) helper.fail("agora o de gelo");
        var after = net.thaumcraft.item.FocusPouchItem.contents(player.getInventory().getItem(6));
        if (!after.get(0).is(TCItems.FOCI.get("fire"))) helper.fail("o de fogo volta para a bolsa");
        // agachado: tira o foco
        net.thaumcraft.item.FocusSwap.change(wand, player, net.thaumcraft.item.FocusSwap.REMOVE);
        if (wand.has(net.thaumcraft.registry.TCComponents.WAND_FOCUS)) helper.fail("REMOVE tira o foco");
        helper.succeed();
    }

    @GameTest
    public void wornBaublesCount(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = new ItemStack(TCItems.WAND);
        float fire = WandItem.modifier(wand, player, Aspects.FIRE), water = WandItem.modifier(wand, player, Aspects.WATER);
        var worn = net.thaumcraft.baubles.Baubles.container(player);
        worn.setItem(net.thaumcraft.baubles.Baubles.RING_1, new ItemStack(TCItems.APPRENTICE_RINGS.get("fire")));
        if (Math.abs(fire - WandItem.modifier(wand, player, Aspects.FIRE) - 0.01f) > 0.001f) helper.fail("o anel de aprendiz de fogo dá 1% no fogo");
        if (Math.abs(water - WandItem.modifier(wand, player, Aspects.WATER)) > 0.001f) helper.fail("e nada na água");

        // a bolsa vestida no cinto também vale para a tecla de trocar foco
        ItemStack pouch = new ItemStack(TCItems.FOCUS_POUCH);
        var inside = net.minecraft.core.NonNullList.withSize(18, ItemStack.EMPTY);
        inside.set(3, new ItemStack(TCItems.FOCI.get("shock")));
        net.thaumcraft.item.FocusPouchItem.setContents(pouch, inside);
        worn.setItem(net.thaumcraft.baubles.Baubles.BELT, pouch);
        net.thaumcraft.item.FocusSwap.change(wand, player, "BL");
        if (!"shock".equals(wand.get(net.thaumcraft.registry.TCComponents.WAND_FOCUS))) helper.fail("o foco de raio sai da bolsa do cinto");
        helper.succeed();
    }

    @GameTest
    public void theRunicShieldChargesFromTheWandAndTakesTheHit(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        net.thaumcraft.baubles.Baubles.container(player).setItem(net.thaumcraft.baubles.Baubles.RING_1, new ItemStack(TCItems.RUNIC_RING_LESSER));
        ItemStack wand = new ItemStack(TCItems.WAND);
        WandItem.setVis(wand, new net.thaumcraft.api.aspects.AspectList().add(Aspects.AIR, 1000).add(Aspects.EARTH, 1000));
        player.getInventory().setItem(20, wand);
        net.thaumcraft.event.RunicShield.tick(player);
        if (net.thaumcraft.event.RunicShield.charge(player) != 1) helper.fail("o anel menor carrega uma runa; deu " + net.thaumcraft.event.RunicShield.charge(player));
        int paid = (int) (50 * WandItem.modifier(wand, player, Aspects.AIR));
        if (WandItem.vis(wand, Aspects.AIR) != 1000 - paid || WandItem.vis(wand, Aspects.EARTH) != 1000 - paid) {
            helper.fail("cada runa custa meio de ar e meio de terra da varinha, com o fator da ponteira");
        }
        float left = net.thaumcraft.event.RunicShield.absorb(player, player.damageSources().generic(), 3.0f);
        if (Math.abs(left - 2.0f) > 0.001f) helper.fail("uma runa segura um ponto de dano; sobrou " + left);
        if (net.thaumcraft.event.RunicShield.charge(player) != 0) helper.fail("e se gasta");
        float drown = net.thaumcraft.event.RunicShield.absorb(player, player.damageSources().drown(), 3.0f);
        if (drown != 3.0f) helper.fail("afogamento passa direto");
        helper.succeed();
    }

    @GameTest
    public void runicAugmentationHardensAndGrowsDearer(GameTestHelper helper) {
        if (net.thaumcraft.crafting.RunicAugmentRecipe.forCentral(new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET)) != null) {
            helper.fail("só peças que aceitam escudo rúnico");
        }
        ItemStack goggles = new ItemStack(TCItems.GOGGLES);
        var first = net.thaumcraft.crafting.RunicAugmentRecipe.forCentral(goggles);
        if (first == null || first.components().size() != 2 || first.essentia().getAmount(Aspects.ENERGY) != 32 || first.instability() != 5) {
            helper.fail("sem carga: diamante e sal, 32 de Potentia");
        }
        ItemStack hardened = first.resultFor(goggles);
        if (net.thaumcraft.event.RunicShield.finalCharge(hardened) != 1) helper.fail("sai com uma carga");
        var second = net.thaumcraft.crafting.RunicAugmentRecipe.forCentral(hardened);
        if (second.components().size() != 3 || second.essentia().getAmount(Aspects.ENERGY) != 64) helper.fail("com uma carga: mais um sal, o dobro da essência");
        if (net.thaumcraft.event.RunicShield.finalCharge(new ItemStack(TCItems.RUNIC_GIRDLE)) != 10) helper.fail("o cinturão tem dez cargas");
        helper.succeed();
    }

    @GameTest
    public void theKineticGirdleWantsStrongHarmingSplash(GameTestHelper helper) {
        var recipe = net.thaumcraft.crafting.InfusionRecipes.ALL.stream()
                .filter(r -> r.result().is(TCItems.RUNIC_GIRDLE_KINETIC)).toList();
        if (recipe.size() != 1) helper.fail("uma receita do cinturão cinético (as duas do jar viram a mesma aqui); achou " + recipe.size());
        ItemStack strong = net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION,
                net.minecraft.world.item.alchemy.Potions.STRONG_HARMING);
        ItemStack weak = net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION,
                net.minecraft.world.item.alchemy.Potions.HARMING);
        boolean takesStrong = recipe.get(0).components().stream().anyMatch(i -> i.test(strong));
        boolean takesWeak = recipe.get(0).components().stream().anyMatch(i -> i.test(weak));
        if (!takesStrong || takesWeak) helper.fail("só a de dano II de arremesso");
        helper.succeed();
    }

    @GameTest
    public void theVisStoneTopsUpTheWandInHand(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stone = new ItemStack(TCItems.VIS_STONE);
        stone.set(net.thaumcraft.registry.TCComponents.WAND_VIS, new net.thaumcraft.api.aspects.AspectList().add(Aspects.FIRE, 12));
        ItemStack wand = new ItemStack(TCItems.WAND);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, wand);
        ((net.thaumcraft.item.VisAmuletItem) TCItems.VIS_STONE).onWornTick(stone, player);
        if (WandItem.vis(player.getMainHandItem(), Aspects.FIRE) != 5) helper.fail("passa cinco centésimos por vez");
        if (net.thaumcraft.item.VisAmuletItem.vis(stone).getAmount(Aspects.FIRE) != 7) helper.fail("e a pedra perde o mesmo");
        helper.succeed();
    }

    private static ItemStack potentiaJar(int amount) {
        ItemStack jar = new ItemStack(net.thaumcraft.registry.TCBlocks.JAR);
        jar.set(net.thaumcraft.registry.TCComponents.JAR_CONTENTS, net.thaumcraft.item.JarContents.of(Aspects.ENERGY, amount, null));
        return jar;
    }

    @GameTest
    public void theHarnessFliesOnPotentia(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack harness = new ItemStack(TCItems.HOVER_HARNESS);
        harness.set(net.thaumcraft.registry.TCComponents.HARNESS_JAR, potentiaJar(2));
        player.setItemSlot(EquipmentSlot.CHEST, harness);
        net.thaumcraft.event.Hover.setHover(player, true);
        net.thaumcraft.event.Hover.serverTick(player, harness);
        if (!player.getAbilities().flying) helper.fail("pairando, voa");
        if (!Boolean.TRUE.equals(harness.get(net.thaumcraft.registry.TCComponents.HOVER))) helper.fail("e o arreio guarda que está pairando");
        for (int t = 0; t < net.thaumcraft.event.Hover.EFFICIENCY; t++) net.thaumcraft.event.Hover.serverTick(player, harness);
        if (net.thaumcraft.event.Hover.fuel(harness) != 1) helper.fail("360 tiques gastam um ponto de Potentia; sobrou " + net.thaumcraft.event.Hover.fuel(harness));
        for (int t = 0; t < net.thaumcraft.event.Hover.EFFICIENCY + 1; t++) net.thaumcraft.event.Hover.serverTick(player, harness);
        net.thaumcraft.event.Hover.serverTick(player, harness);
        if (net.thaumcraft.event.Hover.getHover(player) || player.getAbilities().flying) helper.fail("sem Potentia, o voo desliga");
        if (Boolean.TRUE.equals(harness.get(net.thaumcraft.registry.TCComponents.HOVER))) helper.fail("e o arreio também");
        if (net.thaumcraft.event.Hover.toggleHover(player, harness)) helper.fail("não liga com o jarro vazio");
        helper.succeed();
    }

    @GameTest
    public void theGirdleStretchesThePotentiaAndTheSpeed(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack harness = new ItemStack(TCItems.HOVER_HARNESS);
        harness.set(net.thaumcraft.registry.TCComponents.HARNESS_JAR, potentiaJar(5));
        player.setItemSlot(EquipmentSlot.CHEST, harness);
        if (Math.abs(net.thaumcraft.event.Hover.speed(player) - 0.7f) > 0.001f) helper.fail("sem o cinturão, 70% da velocidade");
        net.thaumcraft.baubles.Baubles.container(player).setItem(net.thaumcraft.baubles.Baubles.BELT, new ItemStack(TCItems.HOVER_GIRDLE));
        if (Math.abs(net.thaumcraft.event.Hover.speed(player) - 0.91f) > 0.001f) helper.fail("com ele, 91%");
        net.thaumcraft.event.Hover.setHover(player, true);
        for (int t = 0; t < 289; t++) net.thaumcraft.event.Hover.serverTick(player, harness);
        if (net.thaumcraft.event.Hover.fuel(harness) != 4) helper.fail("com o cinturão, 288 tiques por ponto");
        player.fallDistance = 1.0;
        ((net.thaumcraft.item.HoverGirdleItem) TCItems.HOVER_GIRDLE).onWornTick(new ItemStack(TCItems.HOVER_GIRDLE), player);
        if (Math.abs(player.fallDistance - 0.67) > 0.001) helper.fail("o cinturão tira um terço de bloco da queda por tique");
        // tirando o arreio do peito, o voo acaba
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        net.thaumcraft.event.Hover.checkWorn(player);
        if (net.thaumcraft.event.Hover.getHover(player) || player.getAbilities().flying) helper.fail("sem o arreio no peito não se paira");
        if (WandItem.modifier(new ItemStack(TCItems.WAND), player, Aspects.AIR) != WandItem.modifier(new ItemStack(TCItems.WAND), player, Aspects.FIRE)) {
            helper.fail("sem arreio, nada de desconto diferente no ar");
        }
        helper.succeed();
    }

    @GameTest
    public void theHarnessKeepsItsJar(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack harness = new ItemStack(TCItems.HOVER_HARNESS);
        player.getInventory().setSelectedSlot(0);
        player.getInventory().setItem(0, harness);
        var menu = new net.thaumcraft.inventory.HoverHarnessMenu(1, player.getInventory(), harness);
        if (menu.getSlot(0).mayPlace(new ItemStack(net.thaumcraft.registry.TCBlocks.JAR))) helper.fail("jarro vazio não serve");
        if (!menu.getSlot(0).mayPlace(potentiaJar(8))) helper.fail("jarro de Potentia serve");
        menu.getSlot(0).set(potentiaJar(8));
        menu.removed(player);
        if (net.thaumcraft.event.Hover.fuel(player.getInventory().getItem(0)) != 8) helper.fail("fechando, o jarro fica no arreio");
        player.setItemSlot(EquipmentSlot.CHEST, player.getInventory().getItem(0));
        float air = WandItem.modifier(new ItemStack(TCItems.WAND), player, Aspects.AIR);
        float fire = WandItem.modifier(new ItemStack(TCItems.WAND), player, Aspects.FIRE);
        if (Math.abs(fire - air - 0.03f) > 0.001f) helper.fail("5% de desconto no ar, 2% no resto");
        helper.succeed();
    }
}
