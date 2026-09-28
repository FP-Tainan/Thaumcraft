package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellItem;
import net.thaumcraft.arcana.SpellProjectileEntity;

import java.util.List;

/**
 * O feitiço voando visto: o quadrado de luz da {@code lens_flare} do original, atravessando o ar.
 *
 * <p>Os projéteis ficam <b>parados no ar</b> de propósito — velocidade zero —, porque um projétil a 2,6 blocos
 * por batida sai do quadro antes de a foto ser tirada. Ficam três à altura dos olhos e lado a lado, para se ver a
 * figura, o tamanho e o modo como a luz soma sobre o que está atrás.
 */
public class ArcanaProjectileClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p 0 -59 0 -90 0");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = jogador.level();

                // uma parede de pedra atrás, para se ver a luz somar sobre alguma coisa
                for (int y = -60; y <= -56; y++) {
                    for (int z = -2; z <= 2; z++) {
                        level.setBlockAndUpdate(new net.minecraft.core.BlockPos(8, y, z),
                                net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }

                Spell tiro = Spell.of(Shapes.PROJECTILE, Essences.FIRE_DAMAGE);
                for (int i = 0; i < 3; i++) {
                    var voa = new SpellProjectileEntity((net.minecraft.server.level.ServerLevel) level,
                            jogador, tiro, 1.0);
                    voa.snapTo(4.0, -58.0, 0.5 + (i - 1) * 1.6, 90.0f, 0.0f);
                    voa.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(voa);
                }

                // e a frase de um feitiço de projétil na mão, com os modificadores dele
                jogador.getInventory().setItem(0, SpellItem.write(new ItemStack(ArcanaItems.SPELL),
                        new Spell(List.of(
                                new Spell.Stage(Shapes.PROJECTILE, List.of(Essences.FIRE_DAMAGE),
                                        List.of(Modifiers.SPEED, Modifiers.GRAVITY, Modifiers.PIERCING)),
                                new Spell.Stage(Shapes.AOE, List.of(Essences.LIGHT), List.of())))));
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(30);
            context.takeScreenshot("aa_projetil");

            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);

            // o cursor sobre a primeira posição da barra, que é onde o feitiço está: a frase é o tooltip
            double[] onde = context.computeOnClient(minecraft -> {
                double escala = minecraft.getWindow().getGuiScale();
                int esquerda = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
                int topo = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
                return new double[]{(esquerda + 8 + 8) * escala, (topo + 142 + 8) * escala};
            });
            context.getInput().setCursorPos(onde[0], onde[1]);
            context.waitTicks(5);
            context.takeScreenshot("aa_projetil_frase");
        }
    }
}
