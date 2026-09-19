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
}
