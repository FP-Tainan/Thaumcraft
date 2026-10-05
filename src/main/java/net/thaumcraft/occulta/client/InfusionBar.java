package net.thaumcraft.occulta.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.infusion.Infusions;

/**
 * A <b>barra de poder</b> da infusão: o {@code RenderInfusionEnergyBar} do Witchery.
 *
 * <p>Um <b>tubo de vidro</b> de oito por trinta e dois, encostado à direita da tela e no meio dela, que se
 * enche de baixo para cima com a quantidade de carga que a pessoa tem.
 *
 * <p>E o que o enche <b>muda com a infusão</b>: a do Outro Lugar o enche com a textura do <b>portal</b>, a
 * da Luz com a da <b>neve</b>, a do Mundo com a da <b>terra</b> e a Infernal com a da <b>pedra do
 * Nether</b>. É um detalhe pequeno do original e é o que torna a barra legível de relance —
 * não se precisa de ler um número para saber qual delas se tem.
 *
 * <p>Ela só aparece a quem <b>está infundido</b>. Sem infusão não há tubo nenhum.
 *
 * <p>E há uma <b>segunda</b>, dez pixels mais para dentro: a dos <b>poderes de bicho</b>. Essa não é um
 * tubo que se enche: é uma <b>pilha de riscos</b>, um por carga — porque elas são poucas, no máximo vinte, e
 * contar vinte riscos é mais rápido do que medir um nível.
 */
public final class InfusionBar {
    /** O tamanho do tubo. */
    public static final int LARGO = 8;
    public static final int ALTO = 32;

    /** E onde ele fica: encostado à direita e no meio. */
    public static final int DA_DIREITA = 20;

    /** A segunda barra, a dos bichos, fica dez para dentro. */
    public static final int A_SEGUNDA = 30;

    /** E a argila é o que a enche, como no original. */
    private static final Identifier ARGILA = Identifier.withDefaultNamespace("textures/block/clay.png");

    /** O vidro por cima, que é uma tira de oito por um esticada. */
    public static final Identifier VIDRO = Thaumcraft.id("textures/gui/glass.png");

    /** O que enche o tubo de cada infusão. */
    private static final Identifier PORTAL = Identifier.withDefaultNamespace("textures/block/nether_portal.png");
    private static final Identifier NEVE = Identifier.withDefaultNamespace("textures/block/snow.png");
    private static final Identifier NETHERRACK =
            Identifier.withDefaultNamespace("textures/block/netherrack.png");
    private static final Identifier TÁBUAS = Identifier.withDefaultNamespace("textures/block/oak_planks.png");
    private static final Identifier TERRA = Identifier.withDefaultNamespace("textures/block/dirt.png");

    private InfusionBar() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("infusion_bar"), (graphics, tracker) -> desenha(graphics));
    }

    /** O que enche o tubo daquela infusão. */
    public static Identifier doquê(int id) {
        return switch (id) {
            case 1 -> NEVE;
            case 2 -> TERRA;
            case 3 -> PORTAL;
            case 4 -> NETHERRACK;
            default -> TÁBUAS;
        };
    }

    private static void desenha(GuiGraphicsExtractor graphics) {
        Player quem = Minecraft.getInstance().player;
        if (quem == null) return;
        int teto = Infusions.teto(quem);
        if (teto <= 0) return;

        int tem = Math.min(Infusions.energia(quem), teto);
        int cheio = Math.round((float) ALTO * tem / teto);
        int x = graphics.guiWidth() - DA_DIREITA;
        int y = graphics.guiHeight() / 2 - ALTO / 2;

        /*
         * O tubo se enche de baixo para cima, e a textura que o enche é desenhada <b>em pedaços de oito</b>
         * — o último deles cortado ao que falta —, que é como o original faz para a textura não ficar
         * esticada quando a barra está pela metade.
         */
        Identifier oquê = doquê(Infusions.de(quem).id);
        int falta = cheio;
        int debaixo = y + ALTO;
        while (falta > 0) {
            int pedaço = Math.min(falta, LARGO);
            graphics.blit(RenderPipelines.GUI_TEXTURED, oquê, x, debaixo - pedaço,
                    0.0f, (float) (LARGO - pedaço), LARGO, pedaço, 16, 16);
            debaixo -= pedaço;
            falta -= pedaço;
        }

        // e o vidro por cima, que é a tira de um esticada pelo tubo inteiro
        graphics.blit(RenderPipelines.GUI_TEXTURED, VIDRO, x, y, 0.0f, 0.0f, LARGO, ALTO, LARGO, 1);

        desenhaOBicho(graphics, quem, y);
    }

    /**
     * <b>A segunda barra: a do bicho.</b>
     *
     * <p>Um risco de <b>dois pixels</b> por carga, empilhados de baixo para cima. São no máximo vinte, e
     * por isso cabem no mesmo tubo de trinta e dois sem se tocarem.
     */
    private static void desenhaOBicho(GuiGraphicsExtractor graphics, Player quem, int y) {
        int cargas = net.thaumcraft.occulta.infusion.beast.CreaturePowers.cargas(quem);
        if (cargas <= 0 || net.thaumcraft.occulta.infusion.beast.CreaturePowers.dele(quem) == null) return;
        int x = graphics.guiWidth() - A_SEGUNDA;
        for (int risco = 0; risco < cargas; risco++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ARGILA, x, y + ALTO - 2 - risco * 2,
                    0.0f, 0.0f, LARGO, 1, 16, 16);
        }
    }
}
