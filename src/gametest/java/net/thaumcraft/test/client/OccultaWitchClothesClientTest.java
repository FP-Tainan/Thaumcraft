package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;

/**
 * As <b>roupas de bruxa</b> vestidas, em quatro bonecos.
 *
 * <p>O que importa nesta tela:
 *
 * <ul>
 *   <li>o <b>chapéu de bruxa</b>, que são quatro caixas empilhadas — aba larga, gola, corpo e ponta — e
 *       que tem de se ver como um cone e não como uma torre;</li>
 *   <li>o <b>chapéu da Baba Yaga</b>, que é o mesmo lugar e outra coisa: quatro caixas encaixadas e
 *       tortas, um cone <b>amassado</b> que se dobra sobre si próprio;</li>
 *   <li>o <b>manto</b>, que é uma caixa inchada por cima do peito;</li>
 *   <li>e as <b>ombreiras</b>, que só o Manto de Necromante leva.</li>
 * </ul>
 *
 * <p>E a <b>tinta</b>: o primeiro boneco leva o conjunto sem tinta, que sai do roxo de fábrica; os outros
 * levam o chapéu pintado, para se ver que a camada de cima pega e a de baixo não.
 */
public class OccultaWitchClothesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("difficulty peaceful");
            context.waitTicks(20);

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = 2; dz <= 7; dz++) {
                        level.setBlockAndUpdate(meio.offset(dx, -1, dz),
                                Blocks.SMOOTH_STONE.defaultBlockState());
                    }
                }

                // os quatro: conjunto sem tinta, manto de necromante, chapéu da Baba, e chapéu pintado
                veste(level, meio.offset(-3, 0, 5), OccultaItems.WITCH_HAT, OccultaItems.WITCH_ROBES, -1);
                veste(level, meio.offset(-1, 0, 5), OccultaItems.WITCH_HAT,
                        OccultaItems.NECROMANCERS_ROBES, -1);
                veste(level, meio.offset(1, 0, 5), OccultaItems.BABAS_HAT, OccultaItems.WITCH_ROBES, -1);
                veste(level, meio.offset(3, 0, 5), OccultaItems.WITCH_HAT, OccultaItems.WITCH_ROBES,
                        0x55FF55);

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.4, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("as_roupas_de_bruxa");
        }
    }

    private static void veste(net.minecraft.server.level.ServerLevel level, BlockPos onde,
                              net.minecraft.world.item.Item chapéu,
                              net.minecraft.world.item.Item manto, int tinta) {
        var boneco = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
        if (boneco == null) return;
        boneco.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 180.0f, 0.0f);
        boneco.setYBodyRot(180.0f);
        boneco.setItemSlot(EquipmentSlot.HEAD, pinta(new ItemStack(chapéu), tinta));
        boneco.setItemSlot(EquipmentSlot.CHEST, pinta(new ItemStack(manto), tinta));
        level.addFreshEntity(boneco);
    }

    private static ItemStack pinta(ItemStack peça, int tinta) {
        if (tinta < 0) return peça;
        peça.set(net.minecraft.core.component.DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(tinta));
        return peça;
    }
}
