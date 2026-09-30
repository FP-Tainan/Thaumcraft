package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.OcculusBlock;
import net.thaumcraft.arcana.SkillData;
import net.thaumcraft.arcana.SkillTree;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Essences;

/**
 * A árvore de perícias vista.
 *
 * <p>Esta foto existe pela mesma razão que a das partículas: a tela da árvore desenha à mão — abas, linhas
 * entre as perícias, molduras da cor do ponto, figuras dos itens — e <b>nenhuma prova de servidor desenha</b>.
 * Se alguma dessas contas estiver errada, o jogo quebra ao abrir o Óculus e só uma foto diria.
 *
 * <p>São três estados: quem acaba de chegar (três pontos azuis, só as raízes ao alcance), quem já sabe alguma
 * coisa (as linhas acesas e o que se abriu), e a aba de Ofensa, que é a mais cheia.
 */
public class ArcanaSkillTreeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            // 1) quem acaba de chegar
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                SkillData.set(jogador, SkillData.NONE);
                Mana.set(jogador, Mana.NONE);
                jogador.openMenu(OcculusBlock.provider());
            });
            context.waitTicks(20);
            context.takeScreenshot("aa_arvore_novo");

            // 2) quem já sabe alguma coisa e tem pontos de sobra
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                Mana.set(jogador, new Mana(50, 0.0f, 0.0f, 0.0f));
                SkillData sabe = SkillData.NONE;
                // a corrente do original: o Projétil, o Dano Físico e os dois que pendem dele
                for (var qual : java.util.List.of(Shapes.PROJECTILE, Essences.PHYSICAL_DAMAGE,
                        Essences.FIRE_DAMAGE, Essences.LIGHTNING_DAMAGE)) {
                    sabe = sabe.learn(SkillTree.of(qual), 50);
                }
                SkillData.set(jogador, sabe);
                jogador.openMenu(OcculusBlock.provider());
            });
            context.waitTicks(20);
            // e o que o servidor sabe tem de chegar aqui: o Óculus desenha do lado de cá, e já mostrou
            // nível zero a quem tinha cinquenta porque o anexo da mana se registrava tarde demais
            context.runOnClient(minecraft -> {
                if (Mana.of(minecraft.player).level() != 50) {
                    throw new AssertionError("o nível não chegou ao cliente: "
                            + Mana.of(minecraft.player).level());
                }
                if (SkillData.of(minecraft.player).known().size() != 4) {
                    throw new AssertionError("as perícias não chegaram ao cliente");
                }
            });
            context.takeScreenshot("aa_arvore_ofensa");

            // 3) e a aba de Utilidade, que é a que tem o Vínculo
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof net.thaumcraft.arcana.client.SkillTreeScreen tela) {
                    tela.showBranch(SkillTree.Branch.UTILITY);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("aa_arvore_utilidade");
        }
    }
}
