package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.entity.TravelingTrunkEntity;
import net.thaumcraft.item.GolemBellItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/** O baú itinerante do Thaumcraft 4.2.3.5: segue o dono aos pulos, some e reaparece perto dele, e as melhorias. */
public class TrunkGameTest {
    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
    }

    /** Um jogador de mentira com nome próprio (o do jogo de teste é o mesmo para todos, e as provas correm juntas). */
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper helper, String name) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), name), false);
        var p = new net.minecraft.server.level.ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, p, cookie);
        return p;
    }

    private static TravelingTrunkEntity trunk(GameTestHelper helper, BlockPos at, String owner, int upgrade) {
        TravelingTrunkEntity t = TCEntities.TRAVELING_TRUNK.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        BlockPos abs = helper.absolutePos(at);
        t.snapTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0f, 0.0f);
        t.setOwner(owner);
        t.setUpgrade(upgrade);
        t.setInvSize();
        helper.getLevel().addFreshEntity(t);
        return t;
    }

    /** Pula atrás do dono até ficar a menos de cinco blocos dele. */
    @GameTest(maxTicks = 400)
    public void theTrunkHopsAfterItsOwner(GameTestHelper helper) {
        floor(helper);
        var owner = helper.makeMockServerPlayerInLevel();
        BlockPos o = helper.absolutePos(new BlockPos(6, 2, 6));
        owner.snapTo(o.getX() + 0.5, o.getY(), o.getZ() + 0.5);
        TravelingTrunkEntity t = trunk(helper, new BlockPos(1, 2, 1), owner.getName().getString(), -1);
        helper.succeedWhen(() -> {
            if (t.distanceTo(owner) > 5.5f) helper.fail("o baú devia ter chegado perto do dono, está a " + t.distanceTo(owner)
                    + " (dono achado: " + t.getOwnerEntity() + ", nome " + owner.getName().getString() + ", no chão " + t.onGround()
                    + ", movimento " + t.getDeltaMovement() + ")");
        });
    }

    /** Longe demais (mais de vinte), some e aparece num anel em volta do dono. */
    @GameTest(maxTicks = 100)
    public void theTrunkTeleportsToAFarOwner(GameTestHelper helper) {
        floor(helper);
        var owner = player(helper, "dono-longe");
        BlockPos far = helper.absolutePos(new BlockPos(3, 2, 3)).offset(40, 0, 0);
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) helper.getLevel().setBlockAndUpdate(far.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        owner.snapTo(far.getX() + 0.5, far.getY(), far.getZ() + 0.5);
        TravelingTrunkEntity t = trunk(helper, new BlockPos(1, 2, 1), owner.getName().getString(), -1);
        helper.succeedWhen(() -> {
            if (t.distanceTo(owner) > 4.0f) helper.fail("o baú devia ter aparecido perto do dono");
        });
    }

    /** Com a entropia, engole o que está no chão perto dele. */
    @GameTest(maxTicks = 200)
    public void anEntropyTrunkSwallowsItems(GameTestHelper helper) {
        floor(helper);
        TravelingTrunkEntity t = trunk(helper, new BlockPos(3, 2, 3), "", 5);
        ItemEntity item = helper.spawnItem(Items.DIAMOND, 4.5f, 2.5f, 3.5f);
        helper.succeedWhen(() -> {
            boolean found = false;
            for (int a = 0; a < 27; a++) if (t.inventory.getItem(a).is(Items.DIAMOND)) found = true;
            if (!found || item.isAlive()) helper.fail("o diamante devia ter entrado no baú");
        });
    }

    /** Com a água, nada o fere; com a terra, tem quatro fileiras. */
    @GameTest
    public void upgradesChangeTheTrunk(GameTestHelper helper) {
        floor(helper);
        TravelingTrunkEntity water = trunk(helper, new BlockPos(2, 2, 2), "", 3);
        water.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 50.0f);
        if (water.getHealth() != water.getMaxHealth()) helper.fail("com a água, o baú não se fere");
        TravelingTrunkEntity earth = trunk(helper, new BlockPos(5, 2, 5), "", 1);
        if (earth.inventory.getContainerSize() != 36) helper.fail("com a terra, o baú tem trinta e seis casas");
        helper.succeed();
    }

    /** O sino recolhe o baú: com a ordem, o item leva o que tinha dentro; sem ela, o que tinha cai no chão. */
    @GameTest
    public void theBellPicksUpTheTrunk(GameTestHelper helper) {
        floor(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.GOLEM_BELL));
        TravelingTrunkEntity order = trunk(helper, new BlockPos(2, 2, 2), player.getName().getString(), 4);
        order.inventory.setItem(0, new ItemStack(Items.EMERALD, 7));
        GolemBellItem.pickUp(player, helper.getLevel(), InteractionHand.MAIN_HAND, order);
        if (order.isAlive()) helper.fail("o baú devia ter sido recolhido");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, order.getBoundingBox().inflate(2));
        var spawner = drops.stream().filter(e -> e.getItem().is(TCItems.TRUNK_SPAWNER)).findFirst();
        if (spawner.isEmpty()) helper.fail("devia ter caído o baú guardado");
        var inv = spawner.get().getItem().get(TCComponents.TRUNK_INVENTORY);
        if (inv == null || !inv.getFirst().is(Items.EMERALD) || inv.getFirst().getCount() != 7) helper.fail("com a ordem, o baú guardado leva as esmeraldas");
        if (drops.stream().anyMatch(e -> e.getItem().is(Items.EMERALD))) helper.fail("com a ordem, nada cai no chão");
        helper.succeed();
    }
}
