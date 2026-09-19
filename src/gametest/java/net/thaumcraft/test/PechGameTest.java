package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.entity.PechEntity;
import net.thaumcraft.entity.PechTrades;
import net.thaumcraft.inventory.PechMenu;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

/** O pech tem de seguir o {@code EntityPech}, o {@code ContainerPech} e o {@code ItemFocusPech}. */
public class PechGameTest {
    @GameTest
    public void thePechKnowsWhatIsValuable(GameTestHelper helper) {
        PechEntity pech = helper.spawn(TCEntities.PECH, new BlockPos(1, 1, 1));
        if (pech.getMaxHealth() != 30.0f) helper.fail("o pech tem 30 de vida");
        if (pech.getValue(new ItemStack(Items.EMERALD)) != 5) helper.fail("a esmeralda vale cinco");
        if (pech.getValue(new ItemStack(Items.DIAMOND)) != 4) helper.fail("o diamante vale quatro");
        if (pech.getValue(new ItemStack(TCItems.MANA_BEAN)) != 1) helper.fail("o feijão de mana vale um");
        if (!pech.isValued(new ItemStack(TCResources.get("gold_coin")))) helper.fail("a moeda de ouro tem Lucrum e vale");
        if (pech.isValued(new ItemStack(Items.COBBLESTONE))) helper.fail("pedra não vale nada");
        // o que não vale vai para a mochila
        ItemStack rest = pech.pickupItem(new ItemStack(Items.COBBLESTONE, 10));
        if (!rest.isEmpty() || pech.loot.get(0).getCount() != 10) helper.fail("a pedra vai para a mochila");
        // o de valor ele come, e uma hora fica manso
        for (int i = 0; i < 200 && !pech.isTamed(); i++) pech.pickupItem(new ItemStack(Items.EMERALD));
        if (!pech.isTamed()) helper.fail("com esmeraldas o pech fica manso");
        helper.succeed();
    }

    @GameTest
    public void aTamedPechTrades(GameTestHelper helper) {
        PechEntity pech = helper.spawn(TCEntities.PECH, new BlockPos(1, 1, 1));
        pech.setTamed(true);
        var player = helper.makeMockServerPlayerInLevel();
        player.snapTo(pech.getX() + 1.0, pech.getY(), pech.getZ(), 0.0f, 0.0f);
        PechMenu menu = new PechMenu(1, player.getInventory(), pech);
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND, 3));
        if (!menu.canTrade()) helper.fail("com um diamante na casa, o botão acende");
        menu.clickMenuButton(player, 0);
        int out = 0;
        for (int i = 1; i < 5; i++) out += menu.getSlot(i).getItem().isEmpty() ? 0 : 1;
        if (out == 0) helper.fail("a troca dá alguma coisa");
        if (menu.getSlot(0).getItem().getCount() != 2) helper.fail("e come um diamante");
        if (menu.canTrade()) helper.fail("com as saídas cheias, o botão apaga");
        for (int type = 0; type < 3; type++) {
            for (int value = 1; value <= 5; value++) {
                int v = value;
                if (PechTrades.of(type).stream().noneMatch(t -> t.value() == v)) helper.fail("a tabela " + type + " tem coisa de valor " + v);
            }
        }
        helper.succeed();
    }

    @GameTest
    public void hurtingAPechAngersTheOthers(GameTestHelper helper) {
        PechEntity a = helper.spawn(TCEntities.PECH, new BlockPos(1, 1, 1));
        PechEntity b = helper.spawn(TCEntities.PECH, new BlockPos(3, 1, 3));
        b.setTamed(true);
        var player = helper.makeMockServerPlayerInLevel();
        a.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0f);
        if (a.anger() < 400 || b.anger() < 400) helper.fail("os dois ficam bravos, de 400 a 800 tiques");
        if (b.isTamed()) helper.fail("e o manso deixa de ser");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void thePechFocusShootsABlast(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        Vec3 spot = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.snapTo(spot.x, spot.y, spot.z, 0.0f, 0.0f);
        ItemStack wand = new ItemStack(TCItems.WAND);
        wand.set(TCComponents.WAND_FOCUS, "pech");
        AspectList vis = new AspectList();
        for (Aspect primal : Aspects.primals()) vis.add(primal, 2500);
        wand.set(TCComponents.WAND_VIS, vis);
        FocusItem focus = Focuses.on(wand);
        if (focus == null) throw helper.assertionException("o foco dos pechs devia estar registrado");
        if (!net.thaumcraft.item.WandItem.cast(helper.getLevel(), player, wand, focus)) helper.fail("o foco atira");
        if (net.thaumcraft.item.WandItem.cast(helper.getLevel(), player, wand, focus)) helper.fail("quatro por segundo, não mais");
        AspectList left = wand.get(TCComponents.WAND_VIS);
        // terra, perditio e aqua dez cada (onze com a ponteira de ferro)
        if (2500 - left.getAmount(Aspects.EARTH) != 11) helper.fail("terra: " + (2500 - left.getAmount(Aspects.EARTH)));
        var blasts = helper.getLevel().getEntities(TCEntities.PECH_BLAST, player.getBoundingBox().inflate(3.0), e -> true);
        if (blasts.size() != 1) helper.fail("sai uma rajada; saíram " + blasts.size());
        helper.succeed();
    }
}
