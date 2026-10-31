package net.thaumcraft.occulta.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.brew.BrewBagChoice;
import net.thaumcraft.occulta.brew.BrewBagItem;

/**
 * Quem <b>lê o traço</b> da Bolsa de Cozimentos: o {@code onUsingTick} do {@code ItemBrewBag} do Witchery.
 *
 * <p>É irmão do {@link StrokeReader} da Vara Mística e tem duas diferenças que mudam tudo.
 *
 * <ol>
 *   <li>São <b>oito rumos</b> e não quatro: as quatro diagonais contam, e são elas que dão à bolsa as oito
 *       casas;</li>
 *   <li>é <b>um traço só</b>. A vara desenha um símbolo de vários traços; a bolsa quer apenas saber para
 *       onde se apontou, e no instante em que sabe, para de ler.</li>
 * </ol>
 *
 * <p>E a <b>ordem das perguntas</b> é a do original, com os dois limiares que ela exige: uma diagonal pede
 * <b>três graus e meio</b> em cada eixo e pergunta-se <b>primeiro</b>; um rumo reto pede <b>sete</b>.
 * Invertida a ordem, as diagonais nunca sairiam — um dos dois eixos chega sempre aos sete antes do outro,
 * e o canto viraria lado.
 *
 * <p><b>Agachar desfaz.</b> No meio de um traço, baixar-se limpa o que estava e recomeça a contagem de
 * onde a cabeça está agora — é o jeito de desistir sem largar a bolsa.
 *
 * <p>Um traço só se manda ao servidor se houver <b>frasco naquela casa</b>: apontar para uma casa vazia
 * não escolhe nada, e pode-se tentar outra vez sem largar.
 */
public final class BrewBagReader {
    private static float dePitch;
    private static float deYaw;
    private static boolean lendo;
    private static int escolhida = -1;

    private BrewBagReader() {
    }

    /** A casa que já se escolheu, ou {@code -1}: é o que a roda da tela lê. */
    public static int escolhida() {
        return escolhida;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BrewBagReader::bate);
    }

    private static void bate(Minecraft minecraft) {
        LocalPlayer quem = minecraft.player;
        if (quem == null || !quem.isUsingItem()
                || !(quem.getUseItem().getItem() instanceof BrewBagItem)) {
            if (lendo) para();
            return;
        }

        if (!lendo) {
            lendo = true;
            escolhida = -1;
            marca(quem);
            return;
        }
        // agachado, desfaz-se o que havia e recomeça-se a contagem daqui
        if (quem.isShiftKeyDown()) {
            escolhida = -1;
            marca(quem);
            return;
        }
        if (escolhida >= 0) return;

        float pitch = dePitch - quem.getXRot();
        float yaw = deYaw - quem.getYHeadRot();
        int traço = BrewBagItem.qualTraço(pitch, yaw);
        if (traço < 0) return;

        marca(quem);
        int casa = BrewBagItem.TRAÇO_PARA_CASA[traço];
        ItemStack bolsa = quem.getUseItem();
        if (BrewBagItem.naCasa(bolsa, casa).isEmpty()) return;

        escolhida = casa;
        ClientPlayNetworking.send(new BrewBagChoice.Escolheu(casa));
    }

    private static void marca(LocalPlayer quem) {
        dePitch = quem.getXRot();
        deYaw = quem.getYHeadRot();
    }

    private static void para() {
        lendo = false;
        escolhida = -1;
    }
}
