package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.research.Research;

import java.util.ArrayList;
import java.util.List;

/**
 * A página aberta de uma pesquisa: o livro de duas folhas do Thaumcraft 4.2.3.5.
 *
 * <p>A folha é a do original — 256 por 181, com as duas páginas lado a lado e o nome da pesquisa entre dois
 * filetes no alto da primeira. O texto vem dos mesmos arquivos de idioma do mod, com as marcas dele:
 * {@code <BR>} quebra linha e {@code <LINE>} abre um vão. As páginas de receita chegam junto com as fatias
 * que trouxerem as receitas; por ora entram as de texto, que são a maioria.
 */
public class ResearchPageScreen extends Screen {
    private static final Identifier BOOK = Thaumcraft.id("textures/gui/gui_researchbook.png");
    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 181;
    private static final int PAGE_WIDTH = 98;
    private static final int PAGE_STRIDE = 126;
    /** O original escreve miúdo para caber mais história em cada folha. */
    private static final float TEXT_SCALE = 0.72f;
    private static final int TEXT_COLOR = 0xFF302010;
    /** Quanto de folha sobra para o texto, do filete de cima até o rodapé. */
    private static final int TEXT_ROOM = 140;

    private final Screen parent;
    private final Research research;
    /** O texto já quebrado em folhas do tamanho que cabe. */
    private final List<List<FormattedCharSequence>> sheets = new ArrayList<>();
    private int page;

    public ResearchPageScreen(Screen parent, Research research) {
        super(research.name());
        this.parent = parent;
        this.research = research;
    }

    @Override
    protected void init() {
        this.sheets.clear();
        int lineHeight = Math.max(1, Math.round(this.font.lineHeight * TEXT_SCALE));
        int wrapWidth = Math.round(PAGE_WIDTH / TEXT_SCALE);
        int fullSheet = TEXT_ROOM / lineHeight;
        // a primeira folha perde algumas linhas para o nome da pesquisa
        int firstSheet = (TEXT_ROOM - 25) / lineHeight;
        boolean first = true;
        for (String key : this.research.pages()) {
            List<FormattedCharSequence> lines = this.font.split(clean(Component.translatable(key)), wrapWidth);
            int index = 0;
            do {
                int room = Math.max(1, first ? firstSheet : fullSheet);
                int end = Math.min(lines.size(), index + room);
                this.sheets.add(new ArrayList<>(lines.subList(index, end)));
                index = end;
                first = false;
            } while (index < lines.size());
        }
    }

    /** As marcas do texto do mod original viram o que o jogo de hoje entende. */
    private static Component clean(Component source) {
        String text = source.getString()
                .replace("<BR>", "\n")
                .replace("<LINE>", "\n\n")
                // as figuras embutidas ficam para quando as páginas de receita chegarem
                .replaceAll("<IMG>.*?</IMG>", "");
        return Component.literal(text);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
            return;
        }
        super.onClose();
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
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left, top, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);

        for (int side = 0; side < 2 && this.page + side < this.sheets.size(); side++) {
            int x = left + 24 + side * PAGE_STRIDE;
            int y = top + 16;
            if (this.page == 0 && side == 0) {
                // o nome da pesquisa entre os dois filetes, como na primeira folha do original
                graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, x - 4, y - 2, 24, 184, 96, 4, 256, 256);
                graphics.centeredText(this.font, this.research.name(), x + PAGE_WIDTH / 2, y + 4, TEXT_COLOR);
                graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, x - 4, y + 15, 24, 184, 96, 4, 256, 256);
                y += 25;
            }
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(TEXT_SCALE, TEXT_SCALE);
            int line = 0;
            for (FormattedCharSequence text : this.sheets.get(this.page + side)) {
                graphics.text(this.font, text, 0, line * this.font.lineHeight, TEXT_COLOR, false);
                line++;
            }
            pose.popMatrix();
        }

        if (this.page > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left + 14, top + 162, 0, 184, 12, 8, 256, 256);
        }
        if (this.page < this.sheets.size() - 2) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left + 230, top + 162, 12, 184, 12, 8, 256, 256);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int left = this.left();
        int top = this.top();
        double x = event.x();
        double y = event.y();
        if (y >= top + 158 && y <= top + 174) {
            if (this.page > 0 && x >= left + 10 && x <= left + 30) {
                this.page -= 2;
                return true;
            }
            if (this.page < this.sheets.size() - 2 && x >= left + 226 && x <= left + 246) {
                this.page += 2;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
