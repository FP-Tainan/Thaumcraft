package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.StatueOfWorshipBlockEntity;
import net.thaumcraft.occulta.TaglockItem;

/**
 * A <b>Estátua de Adoração</b>, com goblins ajoelhados à volta.
 *
 * <p>O que importa nesta tela é que ela é um <b>boneco de criança</b> — cabeça a três quartos, corpo a
 * metade — e que a <b>pedra é translúcida</b>: por baixo dela vê-se a pele de quem a prendeu a si. É a
 * diferença entre uma estátua com uma cara pintada e uma pessoa <b>dentro</b> de pedra, e é toda a graça
 * da coisa.
 *
 * <p>A segunda tela é ela <b>na mão</b>, desenhada com a mesma malha, para se ver que o item e o bloco são
 * a mesma estátua.
 */
public class OccultaStatueClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("difficulty peaceful");
            context.waitTicks(20);

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                // o chão, para a estátua e os goblins terem onde estar
                for (int x = -4; x <= 4; x++) {
                    for (int z = 0; z <= 8; z++) {
                        level.setBlockAndUpdate(meio.offset(x, -1, z),
                                Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }

                // a estátua, virada para quem olha e presa a quem olha
                BlockPos onde = meio.offset(0, 0, 5);
                level.setBlockAndUpdate(onde, OccultaBlocks.STATUE_OF_WORSHIP.defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
                if (level.getBlockEntity(onde) instanceof StatueOfWorshipBlockEntity estátua) {
                    estátua.dono(new TaglockItem.Taglock(player.getUUID(),
                            player.getName().getString()));
                }

                // e quatro goblins a adorá-la
                for (int volta = 0; volta < 4; volta++) {
                    var goblin = OccultaEntities.GOBLIN.create(level, EntitySpawnReason.COMMAND);
                    if (goblin == null) continue;
                    goblin.snapTo(meio.getX() + 0.5 + (volta - 1.5) * 1.6, meio.getY(),
                            meio.getZ() + 3.5, 0.0f, 0.0f);
                    goblin.setYBodyRot(0.0f);
                    goblin.setYHeadRot(0.0f);
                    goblin.ofício(volta);
                    goblin.adorando(true);
                    goblin.setPersistenceRequired();
                    goblin.setNoAi(true);
                    level.addFreshEntity(goblin);
                }

                player.getInventory().add(new ItemStack(OccultaItems.STATUE_OF_WORSHIP));
                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("a_estatua_e_os_goblins");

            /*
             * A segunda tela é no <b>modo de sobrevivência</b>, de propósito: no criativo o inventário é
             * a lista de blocos do jogo, e o que se quer ver aqui é a estátua na grade de quem a tem.
             */
            server.runCommand("gamemode survival @p");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("a_estatua_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(10);
        }
    }
}
