package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCSounds;

import java.util.ArrayList;
import java.util.List;

/**
 * O que a distorção faz na tela de quem a tem: a vinheta escura e o coração disparado do susto (o {@code warpVignette}),
 * a névoa que se fecha em volta ({@code fogFiddled}) e os quatro filtros de tela das poções (o {@code checkShaders} do
 * {@code ClientTickEventsFML}): dessaturar no olhar mortal, borrar na vista embaçada, avermelhar na fome estranha e o
 * brilho estourado do desprezo do sol.
 */
public final class WarpClient {
    private static final Identifier VIGNETTE = Thaumcraft.id("textures/misc/vignette.png");
    private static final Identifier DESATURATE = Thaumcraft.id("desaturate");
    private static final Identifier BLUR = Thaumcraft.id("blur");
    private static final Identifier HUNGER = Thaumcraft.id("hunger");
    private static final Identifier SUN_SCORNED = Thaumcraft.id("sun_scorned");

    public static int warpVignette;
    private static float targetBrightness = 1.0f;
    private static float prevVignetteBrightness;
    public static boolean fogFiddled;
    public static float fogTarget;
    public static int fogDuration;

    private WarpClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(TCNetwork.MiscEvent.TYPE, (payload, context) -> context.client().execute(() -> {
            var player = context.client().player;
            if (player == null) return;
            switch (payload.kind()) {
                case 0 -> {
                    warpVignette = 100;
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.HEARTBEAT.value(),
                            net.minecraft.sounds.SoundSource.MASTER, 1.0f, 1.0f, false);
                }
                case 1 -> {
                    fogFiddled = true;
                    fogDuration = 2400;
                }
                case 2 -> {
                    fogFiddled = true;
                    if (fogDuration < 200) fogDuration = 200;
                }
                default -> {
                }
            }
        }));
        ClientTickEvents.START_CLIENT_TICK.register(WarpClient::tick);
        // o tooltip do EventHandlerRunic: quanto aquele equipamento distorce
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            var player = Minecraft.getInstance().player;
            int warp = player == null ? 0 : net.thaumcraft.research.WarpEvents.finalWarp(stack, player);
            if (warp > 0) lines.add(net.minecraft.network.chat.Component.translatable("item.warping").append(" " + warp)
                    .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        });
        // a pista de pesquisa (o PacketResearchComplete com arroba): o aviso verde e o som de aprender
        ClientPlayNetworking.registerGlobalReceiver(TCNetwork.ResearchComplete.TYPE, (payload, context) -> context.client().execute(() -> {
            var player = context.client().player;
            if (player == null || !payload.key().startsWith("@")) return;
            PlayerNotifications.add("§a" + net.minecraft.network.chat.Component.translatable("tc.addclue").getString());
            player.playSound(TCSounds.LEARN.value(), 0.2f, 1.0f + player.getRandom().nextFloat() * 0.1f);
        }));
        // a vinheta vai junto da do jogo, debaixo do resto da tela
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Thaumcraft.id("warp_vignette"),
                (graphics, tracker) -> vignette(graphics));
    }

    /** O pedaço do {@code playerTick} do cliente: os filtros, a vinheta e a névoa. */
    private static void tick(Minecraft minecraft) {
        var player = minecraft.player;
        if (player == null || minecraft.isPaused()) return;
        if (player.hasEffect(TCEffects.DEATH_GAZE)) warpVignette = 10;
        if (warpVignette > 0) {
            warpVignette--;
            targetBrightness = 0.0f;
        } else {
            targetBrightness = 1.0f;
        }
        if (fogFiddled) {
            if (fogDuration < 100) fogTarget = 0.1f * (fogDuration / 100.0f);
            else if (fogTarget < 0.1f) fogTarget += 0.001f;
            fogDuration--;
            if (fogDuration < 0) fogFiddled = false;
        }
    }

    /** Os filtros de tela das poções ativas, na ordem do original. */
    public static List<Identifier> activeShaders() {
        List<Identifier> list = new ArrayList<>();
        var player = Minecraft.getInstance().player;
        if (player == null) return list;
        if (player.hasEffect(TCEffects.DEATH_GAZE)) list.add(DESATURATE);
        if (player.hasEffect(TCEffects.BLURRED_VISION)) list.add(BLUR);
        if (player.hasEffect(TCEffects.UNNATURAL_HUNGER)) list.add(HUNGER);
        if (player.hasEffect(TCEffects.SUN_SCORNED)) list.add(SUN_SCORNED);
        return list;
    }

    /**
     * O {@code fogDensityEvent}: a névoa exponencial do original ({@code GL_EXP}, densidade até 0,1) vira a névoa linear
     * de hoje, fechando em {@code 2 / densidade} blocos.
     */
    public static void fog(FogData fog) {
        if (!fogFiddled || fogTarget <= 0.0f) return;
        float end = 2.0f / fogTarget;
        fog.environmentalStart = Math.min(fog.environmentalStart, 0.0f);
        fog.environmentalEnd = Math.min(fog.environmentalEnd, end);
        fog.skyEnd = Math.min(fog.skyEnd, end);
        fog.cloudEnd = Math.min(fog.cloudEnd, end);
    }

    /** O {@code renderVignette}: escurece as bordas (como a vinheta do jogo), pulsando um pouco. */
    private static void vignette(GuiGraphicsExtractor graphics) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        float brightness = 1.0f - targetBrightness;
        prevVignetteBrightness = (float) (prevVignetteBrightness + (brightness - prevVignetteBrightness) * 0.01);
        if (prevVignetteBrightness <= 0.0f) return;
        float b = Mth.clamp(prevVignetteBrightness * (1.0f + Mth.sin(player.tickCount / 2.0f) * 0.1f), 0.0f, 1.0f);
        int w = graphics.guiWidth(), h = graphics.guiHeight();
        graphics.blit(RenderPipelines.VIGNETTE, VIGNETTE, 0, 0, 0.0f, 0.0f, w, h, w, h, ARGB.colorFromFloat(1.0f, b, b, b));
    }
}
