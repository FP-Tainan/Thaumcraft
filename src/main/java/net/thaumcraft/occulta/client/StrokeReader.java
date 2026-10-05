package net.thaumcraft.occulta.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.symbol.SpellNetwork;
import net.thaumcraft.occulta.symbol.Symbol;
import net.thaumcraft.occulta.symbol.Symbols;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Quem <b>lê os traços</b> da Vara Mística: o {@code onUsingTick} do {@code ItemMysticBranch} do Witchery.
 *
 * <p>Enquanto a vara estiver segurada, a cada batida se mede quanto a cabeça girou desde o último traço. Se
 * ela girou <b>sete graus ou mais</b> para um lado, esse lado vira um traço — e o ponto de partida passa a
 * ser onde a cabeça está agora.
 *
 * <p>A ordem das perguntas é a do original e ela importa: pergunta-se primeiro o <b>cima</b> e o
 * <b>baixo</b>, e só depois a <b>esquerda</b> e a <b>direita</b>. Girando a cabeça na diagonal, o que sai é
 * sempre o traço vertical.
 *
 * <p>Quando o que foi desenhado bate com um símbolo, se manda dizer ao servidor e <b>se para de ler</b> —
 * de modo que continuar a mexer a cabeça depois disso não estraga o que já se acertou.
 */
public final class StrokeReader {
    private static final List<Byte> TRAÇOS = new ArrayList<>();
    private static float dePitch;
    private static float deYaw;
    private static boolean lendo;
    private static @Nullable Symbol achado;

    private StrokeReader() {
    }

    /** O que está desenhado agora, para quem o quiser mostrar. */
    public static List<Byte> traços() {
        return List.copyOf(TRAÇOS);
    }

    /** E o símbolo que já se acertou, se algum. */
    public static @Nullable Symbol achado() {
        return achado;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> bate(minecraft));
    }

    private static void bate(Minecraft minecraft) {
        LocalPlayer quem = minecraft.player;
        if (quem == null || !quem.isUsingItem()
                || !quem.getUseItem().is(OccultaItems.MYSTIC_BRANCH)) {
            if (lendo) para();
            return;
        }

        if (!lendo) {
            lendo = true;
            TRAÇOS.clear();
            achado = null;
            dePitch = quem.getXRot();
            deYaw = quem.getYHeadRot();
            return;
        }
        if (achado != null || TRAÇOS.size() >= Symbols.MÁXIMO) return;

        float pitch = dePitch - quem.getXRot();
        float yaw = deYaw - quem.getYHeadRot();
        Byte traço = null;
        if (pitch >= Symbols.GIRO) traço = Symbols.CIMA;
        else if (pitch <= -Symbols.GIRO) traço = Symbols.BAIXO;
        else if (yaw <= -Symbols.GIRO) traço = Symbols.DIREITA;
        else if (yaw >= Symbols.GIRO) traço = Symbols.ESQUERDA;
        if (traço == null) return;

        TRAÇOS.add(traço);
        dePitch = quem.getXRot();
        deYaw = quem.getYHeadRot();

        byte[] feito = new byte[TRAÇOS.size()];
        for (int volta = 0; volta < feito.length; volta++) feito[volta] = TRAÇOS.get(volta);
        Symbol qual = Symbols.doDesenho(feito);
        if (qual == null) return;

        achado = qual;
        ClientPlayNetworking.send(new SpellNetwork.Preparou(qual.id, Symbols.grauDoDesenho(feito)));
    }

    private static void para() {
        lendo = false;
        TRAÇOS.clear();
        achado = null;
    }
}
