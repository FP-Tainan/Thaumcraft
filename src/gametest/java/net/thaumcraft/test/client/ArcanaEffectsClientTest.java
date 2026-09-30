package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.thaumcraft.arcana.ArcanaEffects;

import java.util.List;

/**
 * Os ícones dos efeitos do Ars Arcana, vistos.
 *
 * <p>Esta foto existe pela lição que já custou caro duas vezes neste porte: <b>nenhuma prova de servidor
 * desenha</b>. Os 24 ícones foram recortados à mão das duas folhas do original, com uma conta de 1.7.10 que
 * ninguém pode conferir sem olhar — se a conta estivesse errada por um quadrado, todos eles sairiam trocados
 * e as 808 provas de servidor continuariam verdes.
 *
 * <p>São duas fotos dos <b>emblemas do canto</b>, que é onde o jogo mostra os efeitos quando não há tela
 * aberta: doze de cada vez, cada um no seu quadrado inteiro. Na tela da mochila o jogo aperta a lista quando
 * ela é comprida e corta os ícones ao meio — bom para jogar, mau para conferir uma figura.
 */
public class ArcanaEffectsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            põe(server, List.of("HASTE", "FROST_SLOW", "LEAP", "GRAVITY_WELL", "SLOWFALL", "FLIGHT",
                    "LEVITATION", "ENTANGLED", "SWIFT_SWIM", "WATERY_GRAVE", "SHRINK", "REGENERATION"));
            context.waitTicks(10);
            context.takeScreenshot("aa_efeitos_primeira_dúzia");

            limpa(server);
            põe(server, List.of("WATER_BREATHING", "FURY", "MAGIC_SHIELD", "MANA_SHIELD", "SPELL_REFLECT",
                    "SILENCE", "ASTRAL_DISTORTION", "CHARMED", "TRUE_SIGHT", "SCRAMBLE_SYNAPSES",
                    "CHRONO_ANCHOR", "ILLUMINATION"));
            context.waitTicks(10);
            context.takeScreenshot("aa_efeitos_segunda_dúzia");

        }
    }

    /** Põe aqueles efeitos em quem joga, todos de uma vez e todos longos. */
    private static void põe(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                            List<String> quais) {
        server.runOnServer(s -> {
            var jogador = s.getPlayerList().getPlayers().getFirst();
            for (String nome : quais) {
                var qual = efeito(nome);
                if (qual != null) jogador.addEffect(new MobEffectInstance(qual, 6000, 0));
            }
        });
    }

    private static void limpa(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().removeAllEffects());
    }

    /** O efeito com aquele nome, lido do próprio {@link ArcanaEffects} — para a lista ser curta de ler. */
    private static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> efeito(String nome) {
        try {
            @SuppressWarnings("unchecked")
            var qual = (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>)
                    ArcanaEffects.class.getField(nome).get(null);
            return qual;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("não há efeito chamado " + nome, e);
        }
    }
}
