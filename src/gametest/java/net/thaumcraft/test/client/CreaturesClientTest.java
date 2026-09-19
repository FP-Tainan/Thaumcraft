package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WispEssenceItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/** O zumbi zangado, o furioso, dois fogos-fátuos e os morcegos de fogo (voando e pendurado), à noite. */
public class CreaturesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("difficulty easy");
            server.runCommand("time set midnight");
            server.runCommand("tp @p ~ ~ ~ 180 5");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos base = player.blockPosition().north(6);
                java.util.function.BiFunction<net.minecraft.world.entity.EntityType<? extends Mob>, BlockPos, Mob> put = (type, at) -> {
                    Mob mob = type.create(level, EntitySpawnReason.COMMAND);
                    mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0f, 0.0f);
                    mob.setNoAi(true);
                    mob.setYHeadRot(0.0f);
                    mob.yBodyRot = 0.0f;
                    level.addFreshEntity(mob);
                    return mob;
                };
                put.apply(TCEntities.BRAINY_ZOMBIE, base.west(3));
                var giant = (net.thaumcraft.entity.GiantBrainyZombieEntity) put.apply(TCEntities.GIANT_BRAINY_ZOMBIE, base.west(1).north(2));
                giant.setAnger(1.5f);
                var fire = (net.thaumcraft.entity.WispEntity) put.apply(TCEntities.WISP, base.east(2).above(1));
                fire.setAspect(Aspects.FIRE);
                fire.setNoGravity(true);
                var water = (net.thaumcraft.entity.WispEntity) put.apply(TCEntities.WISP, base.east(4).above(2));
                water.setAspect(Aspects.WATER);
                water.setNoGravity(true);
                var bat = (net.thaumcraft.entity.FireBatEntity) put.apply(TCEntities.FIREBAT, base.east(1).south(2).above(1));
                bat.setIsBatHanging(false);
                bat.setNoGravity(true);
                // o pendurado, embaixo de uma pedra
                level.setBlockAndUpdate(base.east(3).south(2).above(3), Blocks.STONE.defaultBlockState());
                var hanging = (net.thaumcraft.entity.FireBatEntity) put.apply(TCEntities.FIREBAT, base.east(3).south(2).above(2));
                hanging.setIsBatHanging(true);
                var inv = player.getInventory();
                ItemStack wand = new ItemStack(TCItems.WAND);
                wand.set(TCComponents.WAND_FOCUS, "hellbat");
                inv.setItem(0, wand);
                inv.setItem(1, new ItemStack(TCItems.FOCI.get("hellbat")));
                inv.setItem(2, new ItemStack(TCItems.ZOMBIE_BRAIN));
                inv.setItem(3, new ItemStack(TCItems.BRAINY_ZOMBIE_SPAWN_EGG));
                inv.setItem(4, new ItemStack(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG));
                inv.setItem(5, new ItemStack(TCItems.WISP_SPAWN_EGG));
                inv.setItem(6, new ItemStack(TCItems.FIREBAT_SPAWN_EGG));
                int slot = 9;
                for (ItemStack essence : WispEssenceItem.variants()) {
                    if (slot >= 36) break;
                    inv.setItem(slot++, essence);
                }
                inv.setSelectedSlot(0);
            });
            context.waitTicks(40);
            context.takeScreenshot("criaturas");
            server.runCommand("gamemode survival");
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("criaturas_inventario");
        }
    }
}
