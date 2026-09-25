package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.client.render.TintedItems;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.Researches;

import java.util.ArrayList;
import java.util.List;

/**
 * O Thaumonomicon: o {@code GuiResearchBrowser} do Thaumcraft 4.2.3.5.
 *
 * <p>Cada aba é um pergaminho que se arrasta (e volta macio para dentro dos limites ao soltar), com as pesquisas em
 * casas de vinte e quatro pontos. As ligações são as linhas curvas do original: escuras entre pesquisas já feitas,
 * verdes e ondulantes quando o pai está feito, azuis quando ainda falta. A pesquisa proibida (a que dá distorção) tem a
 * aura roxa por trás; a recém-aprendida, a faísca. O ícone é o do original — item ou desenho —, apagado enquanto não se
 * pode abrir. Sob o cursor sai a caixa do original, com o subtítulo miúdo, o aviso de conhecimento proibido e o que
 * falta para pesquisar.
 */
public class ThaumonomiconScreen extends Screen {
    private static final Identifier BOOK = Thaumcraft.id("textures/gui/gui_research.png");
    private static final Identifier UNKNOWN = Thaumcraft.id("textures/aspects/_unknown.png");
    private static final Identifier PARTICLES = Thaumcraft.id("textures/misc/particles.png");
    private static final Identifier NODES = Thaumcraft.id("textures/misc/nodes.png");
    /** A fonte que o original usa para o que ainda não se entende (o galáctico da mesa de encantamentos). */
    private static final Style GALACTIC = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("alt")));

    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 230;
    private static final int MAP_WIDTH = 224;
    private static final int MAP_HEIGHT = 196;
    private static final int CELL = 24;

    /** A casa em que o mapa estava quando o livro foi fechado (a da última pesquisa aprendida, depois de aprender). */
    public static int lastX = -5;
    public static int lastY = -6;
    private static String selectedCategory;
    /** As pesquisas recém-aprendidas e as suas abas: ganham a faísca até o livro ser fechado. */
    public static final List<String> HIGHLIGHTED = new ArrayList<>();

    private PlayerKnowledge knowledge = new PlayerKnowledge();
    private final List<Research> research = new ArrayList<>();
    private int mapTop, mapLeft, mapBottom, mapRight;
    /** O mapa: onde está, onde estava no tique anterior e para onde vai. */
    private double guiMapX, guiMapY, prevX, prevY, targetX, targetY;
    private Research currentHighlight;
    private boolean hasScribestuff;
    private long popupTime;
    private Component popupMessage = Component.empty();

    public ThaumonomiconScreen() {
        super(Component.translatable("item.thaumcraft.thaumonomicon"));
        this.prevX = this.guiMapX = this.targetX = lastX * CELL - 141 / 2 - 12;
        this.prevY = this.guiMapY = this.targetY = lastY * CELL - 141 / 2;
        this.updateResearch();
    }

    /** O {@code updateResearch}: a lista da aba, a tinta e o papel, e os limites do arrasto. */
    private void updateResearch() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) this.knowledge = Knowledges.of(player);
        if (selectedCategory == null || ResearchCategories.get(selectedCategory) == null) {
            selectedCategory = ResearchCategories.ALL.keySet().iterator().next();
        }
        this.research.clear();
        int minColumn = 0, minRow = 0, maxColumn = 0, maxRow = 0;
        for (Research each : Researches.of(selectedCategory)) {
            if (!each.present()) continue;
            this.research.add(each);
            minColumn = Math.min(minColumn, each.column());
            minRow = Math.min(minRow, each.row());
            maxColumn = Math.max(maxColumn, each.column());
            maxRow = Math.max(maxRow, each.row());
        }
        this.hasScribestuff = player != null && net.thaumcraft.research.ResearchNotes.hasScribeStuff(player);
        this.mapTop = minColumn * CELL - 85;
        this.mapLeft = minRow * CELL - 112;
        this.mapBottom = maxColumn * CELL - 112;
        this.mapRight = maxRow * CELL - 61;
    }

    @Override
    protected void init() {
        this.updateResearch();
    }

    @Override
    public void removed() {
        lastX = (int) ((this.guiMapX + 141 / 2 + 12.0) / CELL);
        lastY = (int) ((this.guiMapY + 141 / 2) / CELL);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
            HIGHLIGHTED.clear();
            this.minecraft.setScreenAndShow(null);
            return true;
        }
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) HIGHLIGHTED.clear();
        return super.keyPressed(event);
    }

    @Override
    public void tick() {
        this.prevX = this.guiMapX;
        this.prevY = this.guiMapY;
        double dx = this.targetX - this.guiMapX;
        double dy = this.targetY - this.guiMapY;
        if (dx * dx + dy * dy < 4.0) {
            this.guiMapX += dx;
            this.guiMapY += dy;
        } else {
            this.guiMapX += dx * 0.85;
            this.guiMapY += dy * 0.85;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        this.extractTransparentBackground(graphics);
    }

    private int left() {
        return (this.width - PANE_WIDTH) / 2;
    }

    private int top() {
        return (this.height - PANE_HEIGHT) / 2;
    }

    private boolean done(Research research) {
        return ResearchManager.isComplete(this.knowledge, research);
    }

    private boolean canUnlock(Research research) {
        return ResearchManager.canUnlock(this.knowledge, research);
    }

    private boolean clued(Research research) {
        return this.knowledge.hasResearch("@" + research.key());
    }

    private float ticks(float partial) {
        return this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount + partial : 0;
    }

    // ------------------------------------------------------------------------------------------------ desenho

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        if (this.minecraft != null && this.minecraft.player != null) this.knowledge = Knowledges.of(this.minecraft.player);
        int var4 = this.left();
        int var5 = this.top();
        this.genResearchBackground(graphics, mx, my, partial);
        if (this.popupTime > System.currentTimeMillis()) {
            int xq = var4 + 128;
            int yq = var5 + 128;
            List<FormattedCharSequence> lines = this.font.split(this.popupMessage, 150);
            int half = lines.size() * this.font.lineHeight / 2;
            graphics.fillGradient(xq - 78, yq - half - 3, xq + 78, yq + half + 3, 0xC0000000, 0xC0000000);
            for (int i = 0; i < lines.size(); i++) {
                graphics.text(this.font, lines.get(i), xq - 75, yq - half + i * this.font.lineHeight, -7302913, false);
            }
        }
        int count = 0;
        for (ResearchCategories.Category category : ResearchCategories.visible(this.knowledge)) {
            int mposx = mx - (var4 + tabColumn(count));
            int mposy = my - (var5 + tabRow(count));
            if (mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
                graphics.text(this.font, category.name(), mx, my - 8, 0xFFFFFFFF, true);
            }
            count++;
        }
    }

    private void genResearchBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        long t = System.nanoTime() / 50000000L;
        int var4 = Mth.floor(this.prevX + (this.guiMapX - this.prevX) * partial);
        int var5 = Mth.floor(this.prevY + (this.guiMapY - this.prevY) * partial);
        if (var4 < this.mapTop) var4 = this.mapTop;
        if (var5 < this.mapLeft) var5 = this.mapLeft;
        if (var4 >= this.mapBottom) var4 = this.mapBottom - 1;
        if (var5 >= this.mapRight) var5 = this.mapRight - 1;
        int var8 = this.left();
        int var9 = this.top();
        int var10 = var8 + 16;
        int var11 = var9 + 17;

        graphics.enableScissor(var10, var11, var10 + MAP_WIDTH, var11 + MAP_HEIGHT);
        // o pergaminho, que desliza mais devagar que o mapa
        ResearchCategories.Category chosen = ResearchCategories.get(selectedCategory);
        int vx = (int) ((float) (var4 - this.mapTop) / Math.abs(this.mapTop - this.mapBottom) * 288.0f);
        int vy = (int) ((float) (var5 - this.mapLeft) / Math.abs(this.mapLeft - this.mapRight) * 316.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, chosen.background(), var10, var11, vx / 2 * 2, vy / 2 * 2, MAP_WIDTH, MAP_HEIGHT, 512, 512);

        float ticks = this.ticks(partial);
        for (Research child : this.research) {
            for (String key : child.parents()) {
                Research parent = Researches.get(key);
                if (parent == null || !parent.category().equals(selectedCategory) || parent.is(Research.Mark.VIRTUAL)) continue;
                int x1 = child.column() * CELL - var4 + 11 + var10;
                int y1 = child.row() * CELL - var5 + 11 + var11;
                int x2 = parent.column() * CELL - var4 + 11 + var10;
                int y2 = parent.row() * CELL - var5 + 11 + var11;
                boolean childDone = this.done(child);
                boolean parentDone = this.done(parent);
                if (childDone) {
                    this.drawLine(graphics, x1, y1, x2, y2, 0.1f, 0.1f, 0.1f, ticks, false);
                } else if (!child.is(Research.Mark.LOST)
                        && (!child.is(Research.Mark.HIDDEN) && !child.is(Research.Mark.LOST) || this.clued(child))
                        && (!child.is(Research.Mark.CONCEALED) || this.canUnlock(child))) {
                    if (parentDone) {
                        this.drawLine(graphics, x1, y1, x2, y2, 0.0f, 1.0f, 0.0f, ticks, true);
                    } else if ((!parent.is(Research.Mark.HIDDEN) && !child.is(Research.Mark.LOST) || this.clued(parent))
                            && (!parent.is(Research.Mark.CONCEALED) || this.canUnlock(parent))) {
                        this.drawLine(graphics, x1, y1, x2, y2, 0.0f, 0.0f, 1.0f, ticks, true);
                    }
                }
            }
            for (String key : child.siblings()) {
                Research sibling = Researches.get(key);
                if (sibling == null || !sibling.category().equals(selectedCategory) || sibling.is(Research.Mark.VIRTUAL)) continue;
                if (sibling.parents().contains(child.key())) continue;
                int x1 = child.column() * CELL - var4 + 11 + var10;
                int y1 = child.row() * CELL - var5 + 11 + var11;
                int x2 = sibling.column() * CELL - var4 + 11 + var10;
                int y2 = sibling.row() * CELL - var5 + 11 + var11;
                if (this.done(child)) {
                    this.drawLine(graphics, x1, y1, x2, y2, 0.1f, 0.1f, 0.2f, ticks, false);
                } else if (!child.is(Research.Mark.LOST)
                        && (!child.is(Research.Mark.HIDDEN) || this.clued(child))
                        && (!child.is(Research.Mark.CONCEALED) || this.canUnlock(child))) {
                    if (this.done(sibling)) {
                        this.drawLine(graphics, x1, y1, x2, y2, 0.0f, 1.0f, 0.0f, ticks, true);
                    } else if ((!sibling.is(Research.Mark.HIDDEN) || this.clued(sibling))
                            && (!sibling.is(Research.Mark.CONCEALED) || this.canUnlock(sibling))) {
                        this.drawLine(graphics, x1, y1, x2, y2, 0.0f, 0.0f, 1.0f, ticks, true);
                    }
                }
            }
        }

        this.currentHighlight = null;
        for (Research node : this.research) {
            int var26 = node.column() * CELL - var4;
            int var27 = node.row() * CELL - var5;
            if (node.is(Research.Mark.VIRTUAL) || var26 < -24 || var27 < -24 || var26 > 224 || var27 > 196) continue;
            int var42 = var10 + var26;
            int var41 = var11 + var27;
            int tint;
            if (this.done(node)) {
                if (node.warp() > 0) this.drawForbidden(graphics, var42 + 11, var41 + 11);
                tint = 0xFFFFFFFF;
            } else {
                if (!ResearchManager.isVisible(this.knowledge, node)) continue;
                if (node.warp() > 0) this.drawForbidden(graphics, var42 + 11, var41 + 11);
                if (this.canUnlock(node)) {
                    float pulse = (float) Math.sin(net.minecraft.util.Util.getMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25f + 0.75f;
                    tint = grey(pulse);
                } else {
                    tint = grey(0.3f);
                }
            }
            boolean secondaryFrame = node.is(Research.Mark.SECONDARY);
            int frame;
            if (node.is(Research.Mark.ROUND)) {
                frame = 54;
            } else if (node.is(Research.Mark.HIDDEN)) {
                frame = secondaryFrame ? 230 : 86;
            } else {
                frame = secondaryFrame ? 110 : 0;
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, var42 - 2, var41 - 2, frame, 230, 26, 26, 256, 256, tint);
            if (node.is(Research.Mark.SPECIAL)) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, var42 - 2, var41 - 2, 26, 230, 26, 26, 256, 256, tint);
            }
            boolean locked = !this.canUnlock(node);
            if (HIGHLIGHTED.contains(node.key())) {
                int px = (int) (t % 16L) * 16;
                graphics.blit(RenderPipelines.GUI_TEXTURED, PARTICLES, var42 - 5, var41 - 5, px, 80, 16, 16, 256, 256);
            }
            if (node.iconStack() != null) {
                ItemStack icon = node.iconStack().get();
                TintedItems.item(graphics, icon, var42 + 3, var41 + 3, locked ? grey(0.1f) : -1);
            } else if (node.icon() != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, node.icon(), var42 + 3, var41 + 3, 0, 0, 16, 16, 16, 16, locked ? grey(0.2f) : tint);
            }
            if (mx >= var10 && my >= var11 && mx < var10 + 224 && my < var11 + 196
                    && mx >= var42 && mx <= var42 + 22 && my >= var41 && my <= var41 + 22) {
                this.currentHighlight = node;
            }
        }
        graphics.disableScissor();

        // as abas: as primeiras encostadas à lombada, à esquerda, e as que sobram na borda direita
        int count = 0;
        for (ResearchCategories.Category category : ResearchCategories.visible(this.knowledge)) {
            boolean selected = category.key().equals(selectedCategory);
            boolean mirrored = tabMirrored(count);
            int s1 = selected ? 0 : 24;
            int s2 = selected ? 0 : 8;
            int tabX = var8 + tabColumn(count);
            int tabY = var9 + tabRow(count);

            // o pergaminho vai espelhado nas da direita, para a ponta dele apontar para fora do livro
            if (mirrored) blitFlipped(graphics, tabX, tabY, 152 + s1, 232, 24, 24);
            else graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, tabX, tabY, 152 + s1, 232, 24, 24, 256, 256);

            // o desenho da aba e a faísca não se espelham: seriam lidos ao contrário
            int iconX = mirrored ? tabX + 3 - s2 : tabX + 5 + s2;
            if (HIGHLIGHTED.contains(category.key())) {
                int px = (int) (16L * (t % 16L));
                graphics.blit(RenderPipelines.GUI_TEXTURED, PARTICLES, iconX - 8, tabY - 4, px, 80, 16, 16, 256, 256);
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, category.icon(), iconX, tabY + 4, 0, 0, 16, 16, 16, 16);
            if (!selected) {
                if (mirrored) blitFlipped(graphics, tabX, tabY, 200, 232, 24, 24);
                else graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, tabX, tabY, 200, 232, 24, 24, 256, 256);
            }
            count++;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, var8, var9, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);

        if (this.currentHighlight != null) this.drawHighlight(graphics, mx, my);
    }

    /**
     * Onde fica a aba de número n. O original punha todas numa fileira só descendo a lombada, e oito era quanto
     * cabia; com os mods de fora elas passam disso, então as que sobram vão para a borda direita do livro, de
     * fora para dentro, e o pergaminho delas é desenhado espelhado, para apontar para fora como o das da esquerda.
     */
    private static final int TABS_PER_COLUMN = PANE_HEIGHT / CELL - 1;

    private static int tabColumn(int index) {
        int coluna = index / TABS_PER_COLUMN;
        return coluna == 0 ? -CELL : PANE_WIDTH + CELL * (coluna - 1);
    }

    /** Se a aba de número n mora na borda direita, e portanto se desenha ao contrário. */
    private static boolean tabMirrored(int index) {
        return index / TABS_PER_COLUMN > 0;
    }

    /**
     * Desenha um pedaço da folha do livro do avesso, da direita para a esquerda: é o que vira o pergaminho das
     * abas da borda direita para a ponta apontar para fora.
     *
     * <p>Espelhar com a matriz do desenho não serve — uma escala negativa vira o quadro do avesso e ele
     * desaparece —, então trocam-se as duas beiras da folha, que dá no mesmo e sempre aparece.
     */
    private static void blitFlipped(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(BOOK, x, y, x + w, y + h,
                (u + w) / 256.0f, u / 256.0f, v / 256.0f, (v + h) / 256.0f);
    }

    private static int tabRow(int index) {
        return index % TABS_PER_COLUMN * CELL;
    }

    private static int grey(float level) {
        int v = Mth.clamp((int) (level * 255.0f), 0, 255);
        return 0xFF000000 | v << 16 | v << 8 | v;
    }

    /** A caixa de quem está sob o cursor, como a do original. */
    private void drawHighlight(GuiGraphicsExtractor graphics, int par1, int par2) {
        Research highlight = this.currentHighlight;
        Component var34 = highlight.name();
        int var26 = par1 + 6;
        int var27 = par2 - 4;
        int var99 = 0;
        boolean done = this.done(highlight);
        boolean unlockable = this.canUnlock(highlight);
        // o que não se pode nem abrir vem em garranchos
        Style style = !done && !unlockable ? GALACTIC : Style.EMPTY;
        Component name = var34.copy().withStyle(style);
        var pose = graphics.pose();
        if (!unlockable) {
            Component missing = Component.translatable("tc.researchmissing");
            int var42 = (int) Math.max(this.font.width(name), this.font.width(missing.copy().withStyle(style)) / 1.5f);
            int var30 = this.font.split(missing.copy().withStyle(style), var42 * 2).size() * this.font.lineHeight;
            graphics.fillGradient(var26 - 3, var27 - 3, var26 + var42 + 3, var27 + var30 + 10, 0xC0000000, 0xC0000000);
            pose.pushMatrix();
            pose.translate(var26, var27 + 12);
            pose.scale(0.5f, 0.5f);
            int line = 0;
            for (FormattedCharSequence seq : this.font.split(missing, var42 * 2)) {
                graphics.text(this.font, seq, 0, line++ * this.font.lineHeight, -9416624, false);
            }
            pose.popMatrix();
        } else {
            boolean secondary = !done && !highlight.tags().isEmpty() && highlight.is(Research.Mark.SECONDARY);
            boolean primary = !secondary && !done;
            Component text = Component.translatable("tc.research_text." + highlight.key());
            int var42 = (int) Math.max(this.font.width(name), this.font.width(text) / 1.9f);
            int var41 = this.font.split(name, Math.max(1, var42)).size() * this.font.lineHeight + 5;
            if (primary) {
                var99 += 9;
                var42 = (int) Math.max(var42, this.font.width(Component.translatable("tc.research.shortprim")) / 1.9f);
            }
            if (secondary) {
                var99 += 29;
                var42 = (int) Math.max(var42, this.font.width(Component.translatable("tc.research.short")) / 1.9f);
            }
            int warp = Math.min(5, highlight.warp());
            Component wte = Component.translatable("tc.forbidden", Component.translatable("tc.forbidden.level." + warp));
            if (highlight.warp() > 0) {
                var99 += 9;
                var42 = (int) Math.max(var42, this.font.width(wte) / 1.9f);
            }
            graphics.fillGradient(var26 - 3, var27 - 3, var26 + var42 + 3, var27 + var41 + 6 + var99, 0xC0000000, 0xC0000000);
            pose.pushMatrix();
            pose.translate(var26, var27 + var41 - 1);
            pose.scale(0.5f, 0.5f);
            graphics.text(this.font, text, 0, 0, -7302913, true);
            pose.popMatrix();
            if (warp > 0) {
                pose.pushMatrix();
                pose.translate(var26, var27 + var41 + 8);
                pose.scale(0.5f, 0.5f);
                graphics.text(this.font, wte, 0, 0, 0xFFFFFFFF, true);
                pose.popMatrix();
                var41 += 9;
            }
            if (primary) {
                pose.pushMatrix();
                pose.translate(var26, var27 + var41 + 8);
                pose.scale(0.5f, 0.5f);
                Player player = this.minecraft.player;
                if (net.thaumcraft.research.ResearchNotes.slotOf(player, highlight.key()) >= 0) {
                    graphics.text(this.font, Component.translatable("tc.research.hasnote"), 0, 0, 0xFF000000 | 16753920, true);
                } else if (this.hasScribestuff) {
                    graphics.text(this.font, Component.translatable("tc.research.getprim"), 0, 0, 0xFF000000 | 8900331, true);
                } else {
                    graphics.text(this.font, Component.translatable("tc.research.shortprim"), 0, 0, 0xFF000000 | 14423100, true);
                }
                pose.popMatrix();
            } else if (secondary) {
                boolean enough = true;
                int cc = 0;
                for (Aspect aspect : highlight.tags().getAspectsSortedAmount()) {
                    if (this.knowledge.hasDiscovered(aspect)) {
                        float alpha = 1.0f;
                        if (this.knowledge.points(aspect) < highlight.tags().getAmount(aspect)) {
                            alpha = (float) Math.sin(net.minecraft.util.Util.getMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25f + 0.75f;
                            enough = false;
                        }
                        AspectTags.draw(graphics, this.font, var26 + cc * 16, var27 + var41 + 8, aspect, highlight.tags().getAmount(aspect), 0, alpha, false, 0);
                    } else {
                        enough = false;
                        graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN, var26 + cc * 16, var27 + var41 + 8, 0, 0, 16, 16, 16, 16, 0x80808080);
                    }
                    cc++;
                }
                pose.pushMatrix();
                pose.translate(var26, var27 + var41 + 27);
                pose.scale(0.5f, 0.5f);
                if (enough) {
                    graphics.text(this.font, Component.translatable("tc.research.purchase"), 0, 0, 0xFF000000 | 8900331, true);
                } else {
                    graphics.text(this.font, Component.translatable("tc.research.short"), 0, 0, 0xFF000000 | 14423100, true);
                }
                pose.popMatrix();
            }
        }
        int color = unlockable ? (highlight.is(Research.Mark.SPECIAL) ? -128 : -1) : (highlight.is(Research.Mark.SPECIAL) ? -8355776 : -8355712);
        graphics.text(this.font, name, var26, var27, color, true);
    }

    /**
     * O {@code drawLine}: uma tira de pontos a cada dois pontos de tela, do filho para o pai, que vai encurvando; a que
     * ondula balança mais perto do filho e vai do transparente ao escuro.
     */
    private void drawLine(GuiGraphicsExtractor graphics, int x, int y, int x2, int y2, float r, float g, float b, float count, boolean wiggle) {
        double d3 = x - x2;
        double d4 = y - y2;
        float dist = (float) Math.sqrt(d3 * d3 + d4 * d4);
        int inc = (int) (dist / 2.0f);
        if (inc <= 0) return;
        float dx = (float) (d3 / inc);
        float dy = (float) (d4 / inc);
        if (Math.abs(d3) > Math.abs(d4)) {
            dx *= 2.0f;
        } else {
            dy *= 2.0f;
        }
        // a linha de três pixels da tela de então, em pontos de interface
        float width = 3.0f / (float) (this.minecraft == null ? 2 : this.minecraft.getWindow().getGuiScale());
        float lastX = 0, lastY = 0;
        int lastColor = 0;
        for (int a = 0; a <= inc; a++) {
            float r2 = r, g2 = g, b2 = b;
            float mx = 0.0f, my = 0.0f;
            float op = 0.6f;
            if (wiggle) {
                float phase = (float) a / inc;
                mx = Mth.sin((count + a) / 7.0f) * 5.0f * (1.0f - phase);
                my = Mth.sin((count + a) / 5.0f) * 5.0f * (1.0f - phase);
                r2 *= 1.0f - phase;
                g2 *= 1.0f - phase;
                b2 *= 1.0f - phase;
                op *= phase;
            }
            float px = x - dx * a + mx;
            float py = y - dy * a + my;
            int color = Mth.clamp((int) (op * 255), 0, 255) << 24 | (int) (r2 * 255) << 16 | (int) (g2 * 255) << 8 | (int) (b2 * 255);
            if (a > 0) segment(graphics, lastX, lastY, px, py, width, lastColor);
            lastX = px;
            lastY = py;
            lastColor = color;
            if (Math.abs(d3) > Math.abs(d4)) {
                dx *= 1.0f - 1.0f / (inc * 3.0f / 2.0f);
            } else {
                dy *= 1.0f - 1.0f / (inc * 3.0f / 2.0f);
            }
        }
    }

    private static void segment(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1, float width, int color) {
        if ((color >>> 24) == 0) return;
        float len = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        if (len <= 0) return;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x0, y0);
        pose.rotate((float) Math.atan2(y1 - y0, x1 - x0));
        pose.translate(0, -width / 2.0f);
        pose.scale(len, width);
        graphics.fill(0, 0, 1, 1, color);
        pose.popMatrix();
    }

    /** A aura do conhecimento proibido: o quadro animado dos nós, roxo, atrás da pesquisa. */
    private void drawForbidden(GuiGraphicsExtractor graphics, int x, int y) {
        int count = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount : 0;
        int frames = 32;
        int part = count % frames;
        int cframe = frames - 1 - part;
        // a folha dos nós é de 2048: cada quadro, 64
        graphics.blit(RenderPipelines.GUI_TEXTURED, NODES, x - 40, y - 40, cframe * 64, 5 * 64, 80, 80, 64, 64, 2048, 2048,
                (int) (0.66f * 255) << 24 | 4456533);
    }

    // ------------------------------------------------------------------------------------------------ cliques

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.popupTime = System.currentTimeMillis() - 1L;
        Research highlight = this.currentHighlight;
        if (highlight != null && !this.done(highlight) && this.canUnlock(highlight) && this.minecraft != null) {
            this.updateResearch();
            boolean secondary = !highlight.tags().isEmpty() && highlight.is(Research.Mark.SECONDARY);
            if (secondary) {
                if (ResearchManager.canAfford(this.knowledge, highlight)) {
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new net.thaumcraft.net.ResearchRequest(highlight.key()));
                }
            } else if (this.hasScribestuff && net.thaumcraft.research.ResearchNotes.slotOf(this.minecraft.player, highlight.key()) == -1) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new net.thaumcraft.net.ResearchRequest(highlight.key()));
                this.popupTime = System.currentTimeMillis() + 3000L;
                this.popupMessage = Component.translatable("tc.research.popup", highlight.name());
            }
        } else if (highlight != null && this.done(highlight) && this.minecraft != null) {
            this.minecraft.setScreenAndShow(new ResearchPageScreen(this, highlight, 0));
        } else {
            int var4 = this.left();
            int var5 = this.top();
            int count = 0;
            for (ResearchCategories.Category category : ResearchCategories.visible(this.knowledge)) {
                int mposx = (int) event.x() - (var4 + tabColumn(count));
                int mposy = (int) event.y() - (var5 + tabRow(count));
                if (mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
                    selectedCategory = category.key();
                    this.updateResearch();
                    this.playButtonClick();
                    break;
                }
                count++;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void playButtonClick() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;
        var player = this.minecraft.player;
        this.minecraft.level.playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.CAMERA_CLACK.value(), SoundSource.PLAYERS, 0.4f, 1.0f, false);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int var6 = this.left() + 8;
        int var7 = this.top() + 17;
        if (event.x() >= var6 && event.x() < var6 + 224 && event.y() >= var7 && event.y() < var7 + 196) {
            this.guiMapX -= dragX;
            this.guiMapY -= dragY;
            this.prevX = this.targetX = this.guiMapX;
            this.prevY = this.targetY = this.guiMapY;
            // o destino fica dentro dos limites; ao soltar, o mapa volta macio para ele
            if (this.targetX < this.mapTop) this.targetX = this.mapTop;
            if (this.targetY < this.mapLeft) this.targetY = this.mapLeft;
            if (this.targetX >= this.mapBottom) this.targetX = this.mapBottom - 1;
            if (this.targetY >= this.mapRight) this.targetY = this.mapRight - 1;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    /** Para os testes: a aba aberta. */
    public static void select(String category) {
        selectedCategory = category;
    }
}
