package net.thaumcraft.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.Researches;

import java.util.ArrayList;
import java.util.List;

/**
 * O Thaumonomicon: o mapa das pesquisas, como ele é no Thaumcraft 4.2.3.5.
 *
 * <p>Cada aba é um pergaminho que se arrasta, com as pesquisas em casas de vinte e quatro pontos ligadas
 * por linhas — verde quando o caminho está aberto, azul quando ainda falta, escura quando já se passou por
 * ali. As medidas todas são as do original: painel de 256 por 230, mapa de 224 por 196 encostado no canto
 * de cima à esquerda do miolo, e as molduras saem todas da mesma folha, na fileira de baixo.
 */
public class ThaumonomiconScreen extends Screen {
    private static final Identifier BOOK = Thaumcraft.id("textures/gui/gui_research.png");
    private static final Identifier UNKNOWN = Thaumcraft.id("textures/aspects/_unknown.png");
    /** A fonte que o original usa para o que ainda não se entende. */
    private static final Style GALACTIC = Style.EMPTY.withFont(new net.minecraft.network.chat.FontDescription.Resource(Identifier.withDefaultNamespace("alt")));

    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 230;
    private static final int MAP_WIDTH = 224;
    private static final int MAP_HEIGHT = 196;
    private static final int CELL = 24;

    /** Onde o mapa estava quando o livro foi fechado, para reabrir no mesmo lugar. */
    private static String lastCategory = "BASICS";
    private static double lastX = -5 * CELL;
    private static double lastY = -6 * CELL;

    private PlayerKnowledge knowledge;
    private String category = lastCategory;
    private double mapX;
    private double mapY;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private List<Research> shown = List.of();
    private Research hovered;

    public ThaumonomiconScreen() {
        super(Component.translatable("item.thaumcraft.thaumonomicon"));
        this.mapX = lastX;
        this.mapY = lastY;
    }

    @Override
    protected void init() {
        Player player = this.minecraft == null ? null : this.minecraft.player;
        this.knowledge = player == null ? new PlayerKnowledge() : Knowledges.of(player);
        if (ResearchCategories.get(this.category) == null) this.category = "BASICS";
        this.selectCategory(this.category);
    }

    @Override
    public void onClose() {
        lastCategory = this.category;
        lastX = this.mapX;
        lastY = this.mapY;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Troca de aba: refaz a lista do que aparece e os limites do arrasto. */
    private void selectCategory(String key) {
        this.category = key;
        List<Research> found = new ArrayList<>();
        int minColumn = 0;
        int minRow = 0;
        int maxColumn = 0;
        int maxRow = 0;
        for (Research research : Researches.of(key)) {
            if (research.is(Research.Mark.VIRTUAL)) continue;
            found.add(research);
            minColumn = Math.min(minColumn, research.column());
            minRow = Math.min(minRow, research.row());
            maxColumn = Math.max(maxColumn, research.column());
            maxRow = Math.max(maxRow, research.row());
        }
        this.shown = found;
        // os mesmos limites do original: o mapa anda um pouco além da última pesquisa, não muito
        this.minX = minColumn * CELL - 85;
        this.maxX = Math.max(this.minX + 1, maxColumn * CELL - 112);
        this.minY = minRow * CELL - 112;
        this.maxY = Math.max(this.minY + 1, maxRow * CELL - 61);
        this.mapX = Mth.clamp(this.mapX, this.minX, this.maxX - 1);
        this.mapY = Mth.clamp(this.mapY, this.minY, this.maxY - 1);
    }

    private int left() {
        return (this.width - PANE_WIDTH) / 2;
    }

    private int top() {
        return (this.height - PANE_HEIGHT) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        int left = this.left();
        int top = this.top();
        int mapLeft = left + 16;
        int mapTop = top + 17;
        if (this.minecraft != null && this.minecraft.player != null) {
            this.knowledge = net.thaumcraft.research.Knowledges.of(this.minecraft.player);
        }
        int scrollX = (int) this.mapX;
        int scrollY = (int) this.mapY;
        this.hovered = null;

        graphics.enableScissor(mapLeft, mapTop, mapLeft + MAP_WIDTH, mapTop + MAP_HEIGHT);
        this.drawParchment(graphics, mapLeft, mapTop, scrollX, scrollY);
        this.drawLinks(graphics, mapLeft, mapTop, scrollX, scrollY);
        this.drawNodes(graphics, mapLeft, mapTop, scrollX, scrollY, mouseX, mouseY);
        graphics.disableScissor();

        this.drawTabs(graphics, left, top);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left, top, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        // o nome da aba vem depois da moldura, senão fica escondido atrás dela
        this.drawTabName(graphics, left, top, mouseX, mouseY);

        this.drawPopup(graphics);
        if (this.hovered != null) this.drawTooltip(graphics, mouseX, mouseY);
    }

    /** O pergaminho do fundo, que desliza junto com o mapa. */
    private void drawParchment(GuiGraphicsExtractor graphics, int mapLeft, int mapTop, int scrollX, int scrollY) {
        ResearchCategories.Category chosen = ResearchCategories.get(this.category);
        if (chosen == null) return;
        int u = (int) ((float) (scrollX - this.minX) / Math.max(1, this.maxX - this.minX) * 288.0f);
        int v = (int) ((float) (scrollY - this.minY) / Math.max(1, this.maxY - this.minY) * 316.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, chosen.background(), mapLeft, mapTop, u, v,
                MAP_WIDTH, MAP_HEIGHT, 512, 512);
    }

    /** As linhas que ligam uma pesquisa às de que ela nasce. */
    private void drawLinks(GuiGraphicsExtractor graphics, int mapLeft, int mapTop, int scrollX, int scrollY) {
        for (Research research : this.shown) {
            if (!ResearchManager.isVisible(this.knowledge, research)) continue;
            boolean done = ResearchManager.isComplete(this.knowledge, research);
            List<String> linked = new ArrayList<>(research.parents());
            linked.addAll(research.siblings());
            for (String key : linked) {
                Research other = Researches.get(key);
                if (other == null || !other.category().equals(this.category)) continue;
                if (other.is(Research.Mark.VIRTUAL)) continue;
                if (!ResearchManager.isVisible(this.knowledge, other)) continue;
                int x1 = research.column() * CELL - scrollX + 11 + mapLeft;
                int y1 = research.row() * CELL - scrollY + 11 + mapTop;
                int x2 = other.column() * CELL - scrollX + 11 + mapLeft;
                int y2 = other.row() * CELL - scrollY + 11 + mapTop;
                if (done) {
                    line(graphics, x1, y1, x2, y2, 0xFF1A1A1A, false);
                } else if (ResearchManager.isComplete(this.knowledge, other)) {
                    line(graphics, x1, y1, x2, y2, 0xFF30C030, true);
                } else {
                    line(graphics, x1, y1, x2, y2, 0xFF3050C0, true);
                }
            }
        }
    }

    /** As pesquisas em si: a moldura, o desenho e o brilho de quem está pronta para ser aberta. */
    private void drawNodes(GuiGraphicsExtractor graphics, int mapLeft, int mapTop, int scrollX, int scrollY,
                           int mouseX, int mouseY) {
        for (Research research : this.shown) {
            if (!ResearchManager.isVisible(this.knowledge, research)) continue;
            int x = research.column() * CELL - scrollX;
            int y = research.row() * CELL - scrollY;
            if (x < -CELL || y < -CELL || x > MAP_WIDTH || y > MAP_HEIGHT) continue;
            int drawX = mapLeft + x;
            int drawY = mapTop + y;

            boolean done = ResearchManager.isComplete(this.knowledge, research);
            boolean open = ResearchManager.canUnlock(this.knowledge, research);
            int tint;
            if (done) {
                tint = 0xFFFFFFFF;
            } else if (open) {
                // o original faz a moldura respirar enquanto a pesquisa espera ser aberta
                float pulse = (float) Math.sin((System.currentTimeMillis() % 600L) / 600.0 * Math.PI * 2.0) * 0.25f + 0.75f;
                int level = (int) (pulse * 255);
                tint = 0xFF000000 | level << 16 | level << 8 | level;
            } else {
                tint = 0xFF4D4D4D;
            }

            int frame = research.is(Research.Mark.ROUND) ? 54
                    : research.is(Research.Mark.HIDDEN) ? 86
                    : research.is(Research.Mark.SECONDARY) ? 110
                    : 0;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, drawX - 2, drawY - 2, frame, 230, 26, 26, 256, 256, tint);
            if (research.is(Research.Mark.SPECIAL)) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, drawX - 2, drawY - 2, 26, 230, 26, 26, 256, 256, tint);
            }
            this.drawIcon(graphics, research, drawX + 3, drawY + 3, done || open ? tint : 0xFF333333);

            if (mouseX >= drawX && mouseX <= drawX + 22 && mouseY >= drawY && mouseY <= drawY + 22) {
                this.hovered = research;
            }
        }
    }

    /**
     * O desenho de uma pesquisa.
     *
     * <p>Quando o original usa um item que ainda não existe por aqui, entra o símbolo do aspecto de que
     * ela mais precisa — que é o que o próprio mod chama de marca principal da pesquisa. Assim nenhuma
     * casa fica vazia enquanto as outras fatias não chegam.
     */
    private void drawIcon(GuiGraphicsExtractor graphics, Research research, int x, int y, int tint) {
        Identifier own = research.icon();
        if (own != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, own, x, y, 0, 0, 16, 16, 16, 16, tint);
            return;
        }
        Aspect aspect = research.primaryTag();
        if (aspect != null) {
            int color = tint == 0xFFFFFFFF ? 0xFF000000 | aspect.color() : tint;
            graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(), x, y, 0, 0, 16, 16, 16, 16, color);
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN, x, y, 0, 0, 16, 16, 16, 16, tint);
    }

    /** As abas, encostadas na lombada à esquerda. */
    private void drawTabs(GuiGraphicsExtractor graphics, int left, int top) {
        int index = 0;
        for (ResearchCategories.Category chosen : ResearchCategories.visible(this.knowledge)) {
            boolean selected = chosen.key().equals(this.category);
            int x = left - 24;
            int y = top + index * CELL;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, y, selected ? 152 : 176, 232, 24, 24, 256, 256);
            int inset = selected ? 0 : 8;
            graphics.blit(RenderPipelines.GUI_TEXTURED, chosen.icon(), left - 19 + inset, top + 4 + index * CELL,
                    0, 0, 16, 16, 16, 16);
            if (!selected) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, y, 200, 232, 24, 24, 256, 256);
            }
            index++;
        }
    }

    /** O nome da aba sob o cursor. Desenhado por último, para ficar na frente da moldura. */
    private void drawTabName(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        int index = 0;
        for (ResearchCategories.Category chosen : ResearchCategories.visible(this.knowledge)) {
            int x = left - 24;
            int y = top + index * CELL;
            if (mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24) {
                graphics.setComponentTooltipForNextFrame(this.font, java.util.List.of(chosen.name()), mouseX, mouseY);
                return;
            }
            index++;
        }
    }

    /** O nome da pesquisa sob o cursor, e o que ela custa. */
    /** O aviso que aparece no meio do livro por três segundos, como o popupmessage do original. */
    private long popupTime;
    private Component popupMessage = Component.empty();

    /** De lado: se compra com pontos, direto do livro. */
    private static boolean secondary(Research research) {
        return research.is(Research.Mark.SECONDARY);
    }

    private void drawPopup(GuiGraphicsExtractor graphics) {
        if (this.popupTime <= System.currentTimeMillis()) return;
        int xq = this.width / 2, yq = this.height / 2;
        var lines = this.font.split(this.popupMessage, 150);
        int half = lines.size() * this.font.lineHeight / 2;
        graphics.fill(xq - 78, yq - half - 3, xq + 78, yq + half + 3, 0xC0000000);
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(this.font, lines.get(i), xq - 75, yq - half + i * this.font.lineHeight, -7302913, false);
        }
    }

    private void drawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean done = ResearchManager.isComplete(this.knowledge, this.hovered);
        boolean open = ResearchManager.canUnlock(this.knowledge, this.hovered);
        List<Component> lines = new ArrayList<>();
        // o que não se pode nem abrir vem em garranchos, como no original
        lines.add(done || open
                ? this.hovered.name()
                : this.hovered.name().copy().withStyle(GALACTIC));
        if (!done && open && !this.hovered.tags().isEmpty() && !secondary(this.hovered)) {
            // as pesquisas de verdade saem de uma nota: os três avisos do original
            var player = this.minecraft.player;
            if (net.thaumcraft.research.ResearchNotes.slotOf(player, this.hovered.key()) >= 0) {
                lines.add(Component.translatable("tc.research.hasnote").withColor(16753920));
            } else if (net.thaumcraft.research.ResearchNotes.hasScribeStuff(player)) {
                lines.add(Component.translatable("tc.research.getprim").withColor(8900331));
            } else {
                lines.add(Component.translatable("tc.research.shortprim").withColor(14423100));
            }
        } else if (!done && open && !this.hovered.tags().isEmpty()) {
            // o preço, com o que já se tem de cada aspecto em verde e o que falta em vermelho
            for (Aspect aspect : this.hovered.tags().getAspects()) {
                int wanted = this.hovered.tags().getAmount(aspect);
                int have = this.knowledge.points(aspect);
                lines.add(Component.literal(aspect.name().getString() + " " + have + "/" + wanted)
                        .withStyle(have >= wanted ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
            lines.add(ResearchManager.canAfford(this.knowledge, this.hovered)
                    ? Component.translatable("tc.research.unlock").withStyle(ChatFormatting.YELLOW)
                    : Component.translatable("tc.research.missing").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (this.hovered.warp() > 0) {
            lines.add(Component.translatable("tc.research.warp", this.hovered.warp())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
    }

    /** Uma linha entre duas pesquisas, com as faíscas andando quando o caminho ainda está por andar. */
    private static void line(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, boolean moving) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) return;
        long tick = System.currentTimeMillis() / 50L;
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            int shade = color;
            if (moving && Math.floorMod(i - tick, 14L) < 4) shade = 0xFFFFFFFF;
            graphics.fill(x - 1, y - 1, x + 1, y + 1, shade);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int left = this.left();
        int top = this.top();
        double mouseX = event.x();
        double mouseY = event.y();
        // as abas
        int index = 0;
        for (ResearchCategories.Category chosen : ResearchCategories.visible(this.knowledge)) {
            int x = left - 24;
            int y = top + index * CELL;
            if (mouseX >= x && mouseX < x + 24 && mouseY >= y && mouseY < y + 24) {
                if (!chosen.key().equals(this.category)) this.selectCategory(chosen.key());
                return true;
            }
            index++;
        }
        if (this.hovered != null && this.minecraft != null) {
            if (ResearchManager.isComplete(this.knowledge, this.hovered)) {
                if (!this.hovered.pages().isEmpty()) {
                    this.minecraft.setScreenAndShow(new ResearchPageScreen(this, this.hovered));
                    return true;
                }
            } else if (ResearchManager.canUnlock(this.knowledge, this.hovered) && !this.hovered.tags().isEmpty()
                    && !secondary(this.hovered)) {
                // a pesquisa de verdade: com papel e tinta, o clique escreve a nota
                var player = this.minecraft.player;
                if (net.thaumcraft.research.ResearchNotes.hasScribeStuff(player)
                        && net.thaumcraft.research.ResearchNotes.slotOf(player, this.hovered.key()) < 0) {
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                            new net.thaumcraft.net.ResearchRequest(this.hovered.key()));
                    this.popupTime = System.currentTimeMillis() + 3000L;
                    this.popupMessage = Component.translatable("tc.research.popup", this.hovered.name());
                }
                return true;
            } else if (ResearchManager.canUnlock(this.knowledge, this.hovered)
                    && ResearchManager.canAfford(this.knowledge, this.hovered)) {
                // quem decide é o servidor; daqui só sai o pedido
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new net.thaumcraft.net.ResearchRequest(this.hovered.key()));
                this.knowledge = net.thaumcraft.research.Knowledges.of(this.minecraft.player);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int left = this.left();
        int top = this.top();
        if (event.x() >= left + 8 && event.x() < left + 8 + MAP_WIDTH
                && event.y() >= top + 17 && event.y() < top + 17 + MAP_HEIGHT) {
            this.mapX = Mth.clamp(this.mapX - dragX, this.minX, this.maxX - 1);
            this.mapY = Mth.clamp(this.mapY - dragY, this.minY, this.maxY - 1);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }
}
