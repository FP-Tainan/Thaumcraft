package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.item.GolemBellItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Os golens na tela: os oito em fila, cada um com um núcleo e acessórios diferentes (a cartola, o barrete, os óculos, a
 * gravata, a viseira, a blindagem, a maça, o lança-dardos, o cérebro no jarro), carregando coisas (o diamante, o balde
 * de água, o jarro de essência); as costas com o núcleo e as melhorias; a tela do golem; e as marcas do sino.
 */
public class GolemClientTest implements FabricClientGameTest {
    private static GolemEntity golem(ServerLevel level, BlockPos at, String material, int core, String deco, boolean adv, float yaw, int... ups) {
        GolemEntity g = TCEntities.GOLEM.create(level, EntitySpawnReason.COMMAND);
        g.init(GolemEntity.typeIndex(material), adv);
        g.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, yaw, 0.0f);
        g.setYHeadRot(yaw);
        g.setYBodyRot(yaw);
        g.setHome(at);
        g.setCore((byte) core);
        for (int a = 0; a < ups.length; a++) g.setUpgrade(a, (byte) ups[a]);
        g.decoration = deco;
        g.setGolemDecoration(deco);
        g.setup(1);
        g.setNoAi(true);
        level.addFreshEntity(g);
        return g;
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule doMobSpawning false");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            AtomicReference<GolemEntity> fill = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -8; x <= 8; x++) for (int z = 1; z <= 9; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 4; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                String[] mats = {"straw", "wood", "tallow", "clay", "flesh", "stone", "iron", "thaumium"};
                String[] decos = {"", "H", "FGB", "VPM", "RB", "", "", ""};
                int[] cores = {-1, 2, 0, 4, 4, 5, 6, 0};
                for (int i = 0; i < mats.length; i++) {
                    GolemEntity g = golem(level, p.offset(i * 2 - 7, 0, 5), mats[i], cores[i], decos[i], i == 7, 180.0f,
                            i == 7 ? new int[]{4, 5, 2} : new int[0]);
                    g.bootup = 0.0f;
                    if (i == 1) g.setCarried(new ItemStack(Items.DIAMOND));
                    if (i == 5) {
                        g.fluidCarried = FluidVariant.of(Fluids.WATER);
                        g.fluidAmount = 2000;
                        g.updateCarried();
                    }
                    if (i == 6) {
                        g.essentia = Aspects.FIRE;
                        g.essentiaAmount = 20;
                        g.updateCarried();
                    }
                    if (i == 7) {
                        g.setHealth(20.0f);
                        fill.set(g);
                    }
                }
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + p.getY() + " " + (p.getZ() + 0.5) + " 0 12");
            context.waitTicks(40);
            context.takeScreenshot("golens_em_fila");

            // de perto, os da direita (o de táumio avançado, o alquimista, o de balde)
            server.runCommand("tp @p " + (p.getX() + 5.5) + " " + p.getY() + " " + (p.getZ() + 2.0) + " 0 20");
            context.waitTicks(20);
            context.takeScreenshot("golens_de_perto");

            // pelas costas: o núcleo preso e as plaquinhas das melhorias
            server.runCommand("tp @p " + (p.getX() + 7.5) + " " + p.getY() + " " + (p.getZ() + 7.2) + " 180 25");
            context.waitTicks(20);
            context.takeScreenshot("golem_costas");

            // a tela do golem de encher com ordem, entropia e fogo
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                GolemEntity g = fill.get();
                g.inventory.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
                g.inventory.setItem(1, new ItemStack(Items.TORCH, 4));
                g.setColors(0, 14);
                net.thaumcraft.inventory.GolemMenu.open(player, g);
            });
            context.waitTicks(20);
            context.takeScreenshot("golem_tela");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // as marcas do sino: um baú marcado em duas faces, um bloco de ar marcado, e a casa do golem
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                ServerLevel level = player.level();
                BlockPos chest = p.offset(2, 0, 3);
                level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
                GolemEntity g = golem(level, p.offset(-1, 0, 3), "wood", 1, "", false, 90.0f);
                List<Marker> markers = new ArrayList<>();
                markers.add(new Marker(chest, level, 1, -1));
                markers.add(new Marker(chest, level, 4, 14));
                markers.add(new Marker(p.offset(0, 1, 6), level, 1, 3));
                g.setMarkers(markers);
                ItemStack bell = new ItemStack(TCItems.GOLEM_BELL);
                bell.set(TCComponents.GOLEM_MARKERS, List.copyOf(markers));
                bell.set(TCComponents.GOLEM_LINK, new GolemBellItem.Link(g.getId(), g.home(), 5));
                player.getInventory().setItem(0, bell);
                player.getInventory().setSelectedSlot(0);
            });
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + (p.getY() + 1) + " " + (p.getZ() - 1.5) + " 0 25");
            context.waitTicks(30);
            context.takeScreenshot("golem_marcas");
        }
    }
}
