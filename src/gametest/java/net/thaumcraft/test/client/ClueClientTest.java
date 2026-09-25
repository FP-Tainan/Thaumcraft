package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

import java.util.concurrent.atomic.AtomicReference;

/** O thaumômetro lendo um osso caído: as runas subindo, os avisos de ponto e a pista do arco de osso; e a nota desconhecida. */
public class ClueClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set noon");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -3; x <= 3; x++) for (int z = 1; z <= 5; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 4; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                // um osso caído no chão, que se examina como o item
                var bone = new net.minecraft.world.entity.item.ItemEntity(level, p.getX() + 0.5, p.getY(), p.getZ() + 2.5,
                        new ItemStack(net.minecraft.world.item.Items.BONE));
                bone.setDeltaMovement(0, 0, 0);
                bone.setNeverPickUp();
                bone.setUnlimitedLifetime();
                level.addFreshEntity(bone);
                player.getInventory().setItem(0, new ItemStack(TCItems.THAUMOMETER));
                ItemStack note = new ItemStack(TCItems.RESEARCH_NOTES);
                note.set(TCComponents.UNKNOWN_NOTE, net.minecraft.util.Unit.INSTANCE);
                player.getInventory().setItem(1, note);
                player.getInventory().setSelectedSlot(0);
                // quem examina já conhece os primários e o que nasce deles, todos
                var knowledge = net.thaumcraft.research.Knowledges.of(player);
                for (var aspect : net.thaumcraft.api.aspects.Aspect.ASPECTS.values()) {
                    knowledge.discover(aspect);
                }
                net.thaumcraft.research.Knowledges.save(player, knowledge);
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 45");
            context.waitTicks(20);
            context.getInput().holdKey(options -> options.keyUse);
            context.waitTicks(10);
            context.takeScreenshot("exame-runas");
            context.waitTicks(15);
            context.getInput().releaseKey(options -> options.keyUse);
            context.waitTicks(20);
            context.takeScreenshot("exame-avisos");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var conhecimento = net.thaumcraft.research.Knowledges.of(player);
                // o createClue sorteia uma entre as que o osso desperta, e não é sempre a mesma; o que se
                // cobra aqui é que o exame deu alguma pista, e que a do arco de osso está entre as que pode dar
                boolean alguma = net.thaumcraft.research.Researches.ALL.keySet().stream()
                        .anyMatch(chave -> conhecimento.hasResearch("@" + chave));
                if (!alguma) throw new AssertionError("o osso examinado devia dar alguma pista");
                var gatilhos = net.thaumcraft.research.ResearchTriggers.of("BONEBOW");
                if (gatilhos == null || gatilhos.items().stream().noneMatch(
                        t -> t.test(new ItemStack(net.minecraft.world.item.Items.BONE)))) {
                    throw new AssertionError("e a do arco de osso devia estar entre elas");
                }
            });
            context.waitTicks(60);
            context.takeScreenshot("exame-pista");
            context.getInput().pressKey(options -> options.keyHotbarSlots[1]);
            context.waitTicks(5);
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("nota-desconhecida-inventario");
        }
    }
}
