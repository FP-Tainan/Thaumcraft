package net.thaumcraft.occulta.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.thaumcraft.occulta.infusion.beast.CreaturePowers;

/**
 * Os <b>poderes de andar</b> do bicho que se tem no bolso, do lado de cá.
 *
 * <p>Trepar paredes, voar, nadar e correr correm <b>no cliente</b>, a cada batida — e é assim no original,
 * por uma razão boa: mexer na velocidade de quem joga só fica macio se for do lado dele. Feito do lado do
 * servidor, o jogador veria o próprio passo se corrigindo de volta duas vezes por segundo.
 *
 * <p>O que torna isto possível é a carga de bicho atravessar a rede: o lado de cá sabe que poder a pessoa
 * tem sem ter de perguntar.
 */
public final class BeastMotion {
    private BeastMotion() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            var quem = minecraft.player;
            if (quem == null || minecraft.isPaused()) return;
            var poder = CreaturePowers.dele(quem);
            if (poder == null) return;
            poder.batida(quem);
        });
    }
}
