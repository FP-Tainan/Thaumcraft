package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.Warp;

/** A distorção do Thaumcraft 4.2.3.5: as três que se guardam no jogador e o contador que puxa os eventos. */
public class WarpGameTest {
    /**
     * A temporária entra e sai; a permanente só entra por aqui; a que gruda sai até zero e não passa disso; e cada mudança
     * põe o contador no total.
     */
    @GameTest
    public void warpIsKeptOnThePlayer(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        Warp.add(player, 3, true);
        Warp.add(player, 2, false);
        Warp.addSticky(player, 1);
        var k = Knowledges.of(player);
        if (k.warpTemp() != 3 || k.warpPerm() != 2 || k.warpSticky() != 1) helper.fail("devia ter 3 temporária, 2 permanente e 1 que gruda");
        if (k.warpTotal() != 6 || k.warpCounter() != 6) helper.fail("o total e o contador são seis");
        Warp.add(player, -5, false);
        if (Knowledges.of(player).warpPerm() != 2) helper.fail("a permanente não sai por aqui");
        Warp.addSticky(player, -4);
        if (Knowledges.of(player).warpSticky() != 0) helper.fail("a que gruda para no zero");
        Warp.add(player, -1, true);
        if (Knowledges.of(player).warpTemp() != 2) helper.fail("a temporária sai");
        helper.succeed();
    }

    /** Pesquisa proibida dá distorção: a dos Óculos (seis) vira três permanentes e três que grudam. */
    @GameTest
    public void forbiddenResearchWarps(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        int warp = net.thaumcraft.research.Researches.get("OCULUS").warp();
        net.thaumcraft.research.ResearchManager.complete(player, "OCULUS");
        var k = Knowledges.of(player);
        if (k.warpPerm() != warp - warp / 2 || k.warpSticky() != warp / 2) helper.fail("os Óculos deviam dar " + warp + " de distorção, deu " + k.warpPerm() + "+" + k.warpSticky());
        helper.succeed();
    }

    /** Fabricar a morte líquida gruda um ponto (o addWarpToItem). */
    @GameTest
    public void craftingWarpedThingsSticks(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.BUCKET_DEATH).onCraftedBy(player, 1);
        if (Knowledges.of(player).warpSticky() != 1) helper.fail("o balde de morte líquida devia grudar um ponto");
        helper.succeed();
    }

    /** O sabão leva toda a temporária e se gasta. */
    @GameTest
    public void soapWashesTemporaryWarp(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        Warp.add(player, 5, true);
        var soap = new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.SANITY_SOAP, 2);
        soap.getItem().releaseUsing(soap, helper.getLevel(), player, 0);
        if (Knowledges.of(player).warpTemp() != 0) helper.fail("o sabão leva a temporária");
        if (soap.getCount() != 1) helper.fail("o sabão se gasta");
        helper.succeed();
    }

    /**
     * Com o contador alto o evento sempre sai; com distorção de verdade acima de cinquenta, as pesquisas proibidas se
     * abrem (e a pista dos sais de banho); e a temporária perde um ponto.
     */
    @GameTest
    public void warpEventsOpenForbiddenResearch(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.thaumcraft.registry.TCEffects.WARP_WARD, 100));
        Warp.add(player, 60, false);
        Warp.add(player, 3, true);
        var k = Knowledges.of(player);
        k.setWarpCounter(10000);
        Knowledges.save(player, k);
        net.thaumcraft.research.WarpEvents.checkWarpEvent(player);
        var after = Knowledges.of(player);
        if (!after.hasResearch("ELDRITCHMINOR") || !after.hasResearch("ELDRITCHMAJOR")) helper.fail("as pesquisas eldritch deviam se abrir");
        if (!after.hasResearch("@BATHSALTS")) helper.fail("a pista dos sais de banho devia aparecer");
        if (after.warpCounter() >= 10000) helper.fail("o contador desce");
        if (after.warpTemp() != 2) helper.fail("a temporária perde um ponto, está " + after.warpTemp());
        helper.succeed();
    }

    /** O leite tira os efeitos comuns, mas não os da distorção (o getCurativeItems vazio do original). */
    @net.fabricmc.fabric.api.gametest.v1.GameTest(maxTicks = 20)
    public void milkDoesNotCureWarp(net.minecraft.gametest.framework.GameTestHelper helper) {
        var cow = helper.spawn(net.minecraft.world.entity.EntityTypes.COW, new net.minecraft.core.BlockPos(1, 2, 1));
        cow.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SPEED, 600));
        net.thaumcraft.research.Incurable.add(cow, new net.minecraft.world.effect.MobEffectInstance(net.thaumcraft.registry.TCEffects.SUN_SCORNED, 600));
        cow.removeAllEffects();
        if (cow.hasEffect(net.minecraft.world.effect.MobEffects.SPEED)) helper.fail("o leite tira a velocidade");
        if (!cow.hasEffect(net.thaumcraft.registry.TCEffects.SUN_SCORNED)) helper.fail("o leite não tira o desprezo do sol");
        helper.succeed();
    }
}
