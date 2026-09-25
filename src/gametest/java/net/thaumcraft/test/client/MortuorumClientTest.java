package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.mortuorum.MinionEntity;
import net.thaumcraft.mortuorum.MinionParts;
import net.thaumcraft.mortuorum.MortuorumBlocks;
import net.thaumcraft.mortuorum.MortuorumEntities;
import net.thaumcraft.mortuorum.MortuorumItems;
import net.thaumcraft.mortuorum.SummoningAltarBlock;
import net.thaumcraft.mortuorum.SummoningAltarBlockEntity;

/** O Altar de Invocação de pé e cinco lacaios remendados de bichos diferentes, para se ver o desenho deles. */
public class MortuorumClientTest implements FabricClientGameTest {
    /** Cinco misturas, das mais mansas às mais esquisitas. */
    private static final MinionParts[] FEITIOS = {
            new MinionParts("zombie_torso", "zombie_torso", "zombie_arm", "zombie_arm", "zombie_legs"),
            new MinionParts("cow_head", "skeleton_torso", "skeleton_arm", "skeleton_arm", "enderman_legs"),
            new MinionParts("chicken_head", "cow_torso", "cow_arm", "chicken_arm", "spider_legs"),
            new MinionParts("villager_head", "iron_golem_torso", "iron_golem_arm", "cow_arm", "pig_legs"),
            new MinionParts("spider_head", "witch_torso", "sheep_arm", "wolf_head", "squid_legs"),
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 180 15");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos base = player.blockPosition().north(7);

                // os cinco lacaios, lado a lado e de frente
                for (int i = 0; i < FEITIOS.length; i++) {
                    MinionEntity lacaio = MortuorumEntities.MINION.create(level, EntitySpawnReason.COMMAND);
                    if (lacaio == null) continue;
                    BlockPos onde = base.east(i * 2 - 4);
                    lacaio.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 0.0f, 0.0f);
                    lacaio.setNoAi(true);
                    lacaio.setYHeadRot(0.0f);
                    lacaio.yBodyRot = 0.0f;
                    lacaio.setParts(FEITIOS[i]);
                    level.addFreshEntity(lacaio);
                }

                // e o altar de lado, montado e com as peças nas casas
                BlockPos altarEm = base.south(3).west(6);
                var estado = MortuorumBlocks.SUMMONING_ALTAR.defaultBlockState()
                        .setValue(SummoningAltarBlock.FACING, Direction.EAST);
                level.setBlockAndUpdate(altarEm, estado);
                MortuorumBlocks.SUMMONING_ALTAR.setPlacedBy(level, altarEm, estado, null, ItemStack.EMPTY);
                if (level.getBlockEntity(altarEm) instanceof SummoningAltarBlockEntity altar) {
                    altar.setItem(SummoningAltarBlockEntity.BLOOD, new ItemStack(MortuorumItems.JAR_OF_BLOOD));
                    altar.setItem(SummoningAltarBlockEntity.SOUL, new ItemStack(MortuorumItems.SOUL_IN_A_JAR));
                    altar.setItem(SummoningAltarBlockEntity.TORSO,
                            new ItemStack(MortuorumItems.PART_ITEMS.get("skeleton_torso")));
                }

                // e uma poça de sangue, para se ver a cor dele
                BlockPos poca = base.south(2).east(5);
                var pedra = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
                for (int dx = -1; dx <= 3; dx++) {
                    for (int dz = -1; dz <= 3; dz++) {
                        boolean borda = dx < 0 || dx > 2 || dz < 0 || dz > 2;
                        level.setBlockAndUpdate(poca.offset(dx, -1, dz), pedra);
                        level.setBlockAndUpdate(poca.offset(dx, 0, dz), borda ? pedra
                                : net.thaumcraft.mortuorum.MortuorumBlocks.BLOOD.defaultBlockState());
                    }
                }

                var inv = player.getInventory();
                inv.setItem(0, new ItemStack(MortuorumItems.SCYTHE_ITEM));
                inv.setItem(6, new ItemStack(MortuorumItems.BUCKET_BLOOD));
                inv.setItem(7, new ItemStack(MortuorumItems.SUMMONING_ALTAR));
                inv.setItem(8, new ItemStack(MortuorumItems.SCYTHE_BONE_ITEM));
                inv.setItem(1, new ItemStack(MortuorumItems.SEWING_MACHINE));
                inv.setItem(2, new ItemStack(MortuorumItems.BONE_NEEDLE));
                inv.setItem(3, new ItemStack(MortuorumItems.JAR_OF_BLOOD));
                inv.setItem(4, new ItemStack(MortuorumItems.SOUL_IN_A_JAR));
                inv.setItem(5, new ItemStack(MortuorumItems.BRAIN_ON_A_STICK));
                inv.setSelectedSlot(0);
            });
            context.waitTicks(40);
            context.takeScreenshot("mortuorum_lacaios");
            server.runCommand("tp @p ~-1 ~ ~-4 90 10");
            context.waitTicks(20);
            context.takeScreenshot("mortuorum_altar");
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("mortuorum_inventario");
        }
    }
}
