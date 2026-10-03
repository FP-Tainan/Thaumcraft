package net.thaumcraft.occulta.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>A folha de um livro de marcação</b>: o {@code GuiScreenMarkupBook} do Witchery.
 *
 * <p>Os livros do Witchery não são páginas escritas: são <b>textos com marcas</b> no arquivo de idioma, e a
 * folha monta-os de cada vez. A marcação inteira do original tem modelos, listas de itens e marcadores; este
 * porte traz o <b>pedaço que o Livro do Vampiro usa</b>, que são cinco marcas e nada mais:
 *
 * <ul>
 *   <li>{@code [br]} — quebra de linha;</li>
 *   <li>{@code [h1 ...]} — o título, em letra grande;</li>
 *   <li>{@code [darkred ...]} — um pedaço colorido;</li>
 *   <li>{@code [img=caminho|...|largura|altura]} — uma figura no meio da folha;</li>
 *   <li>{@code [next=capítulo|páginas]} — para onde a seta leva, e <b>quantas páginas ela pede</b>.</li>
 * </ul>
 *
 * <p>É essa última que faz do livro o que ele é. Um capítulo que peça mais páginas do que o exemplar tem
 * <b>não abre</b>: a seta fica apagada, e quem a carrega vê que há mais folha do lado de lá e não sabe o
 * quê. O Witchery nunca escreve "falta-te uma página" — ele <b>mostra a seta apagada</b>.
 *
 * <p>A folha é a do original, de cento e noventa e dois por cento e noventa e dois, e o texto vai na letra
 * miúda, como no livro de pesquisa.
 */
public class MarkupBookScreen extends Screen {
    private static final Identifier FOLHA = Thaumcraft.id("textures/gui/book_single.png");

    /** A folha, e onde o texto cabe dentro dela. */
    public static final int LARGURA = 192;
    public static final int ALTURA = 192;
    public static final int MARGEM_X = 36;
    public static final int MARGEM_Y = 32;
    public static final int CAIXA = 120;

    /** E a altura em que o texto tem de caber: passando dela, a folha inteira encolhe. */
    public static final int CAIXA_ALTA = 132;

    /** A cor da tinta, que é a de tinta velha e não preta. */
    public static final int TINTA = 0xFF3A2E1F;

    /** As três setas: voltar ao princípio, voltar uma, e seguir. */
    public static final int SETA_LARGURA = 18;
    public static final int SETA_ALTURA = 10;

    private final String raiz;
    private final int páginas;
    private final List<String> pilha = new ArrayList<>();

    private final List<Pedaço> pedaços = new ArrayList<>();
    private String seguinte = "";
    private int seguintePede;

    /** Um pedaço montado da folha: uma linha de texto, ou uma figura. */
    private record Pedaço(FormattedCharSequence linha, Identifier figura, int largura, int altura) {
        static Pedaço texto(FormattedCharSequence linha) {
            return new Pedaço(linha, null, 0, 0);
        }

        static Pedaço figura(Identifier qual, int largura, int altura) {
            return new Pedaço(null, qual, largura, altura);
        }
    }

    public MarkupBookScreen(String raiz, int páginas, String capítulo) {
        super(Component.translatable(raiz));
        this.raiz = raiz;
        this.páginas = páginas;
        this.pilha.add(capítulo);
    }

    @Override
    protected void init() {
        this.monta();
    }

    // ------------------------------------------------------------------ a montagem

    /**
     * Lê a marcação do capítulo em que estamos e desmonta-a em pedaços.
     *
     * <p>O original percorre o texto letra a letra e empilha elementos; aqui o trabalho é o mesmo, com o
     * punhado de marcas que este livro usa.
     */
    private void monta() {
        this.pedaços.clear();
        this.seguinte = "";
        this.seguintePede = 0;

        String chave = this.raiz + "." + this.pilha.get(this.pilha.size() - 1);
        String marcação = Language.getInstance().getOrDefault(chave, "");
        if (marcação.equals(chave)) return;

        List<FormattedText> corrido = new ArrayList<>();
        StringBuilder linha = new StringBuilder();
        Style estilo = Style.EMPTY;

        int i = 0;
        while (i < marcação.length()) {
            char c = marcação.charAt(i);
            if (c != '[') {
                linha.append(c);
                i++;
                continue;
            }
            int fim = marcação.indexOf(']', i);
            if (fim < 0) {
                linha.append(c);
                i++;
                continue;
            }

            String marca = marcação.substring(i + 1, fim);
            i = fim + 1;

            if (marca.equals("br")) {
                corrido.add(FormattedText.of(linha.toString(), estilo));
                this.quebra(corrido);
                corrido.clear();
                linha.setLength(0);
                this.pedaços.add(Pedaço.texto(FormattedCharSequence.EMPTY));
                continue;
            }
            if (marca.startsWith("next=")) {
                String[] partes = marca.substring(5).split("\\|");
                this.seguinte = partes[0];
                this.seguintePede = partes.length > 1 ? parse(partes[1]) : 0;
                continue;
            }
            if (marca.startsWith("img=")) {
                corrido.add(FormattedText.of(linha.toString(), estilo));
                this.quebra(corrido);
                corrido.clear();
                linha.setLength(0);
                this.figura(marca.substring(4));
                continue;
            }
            if (marca.startsWith("h1 ")) {
                corrido.add(FormattedText.of(marca.substring(3),
                        estilo.withBold(true).withColor(ChatFormatting.BLACK)));
                continue;
            }
            if (marca.startsWith("darkred ")) {
                corrido.add(FormattedText.of(linha.toString(), estilo));
                linha.setLength(0);
                corrido.add(FormattedText.of(marca.substring(8),
                        estilo.withColor(ChatFormatting.DARK_RED)));
                continue;
            }
            // uma marca que este porte não conhece some, como no original
        }
        corrido.add(FormattedText.of(linha.toString(), estilo));
        this.quebra(corrido);
    }

    /** Parte o corrido em linhas que caibam na caixa. */
    private void quebra(List<FormattedText> corrido) {
        if (corrido.isEmpty()) return;
        FormattedText junto = FormattedText.composite(List.copyOf(corrido));
        for (FormattedCharSequence linha : this.font.split(junto, CAIXA)) {
            this.pedaços.add(Pedaço.texto(linha));
        }
    }

    /** Uma figura: o caminho vem primeiro e os dois últimos números são o tamanho. */
    private void figura(String oquê) {
        String[] partes = oquê.split("\\|");
        if (partes.length == 0) return;
        int largura = partes.length >= 3 ? parse(partes[partes.length - 2]) : 64;
        int altura = partes.length >= 2 ? parse(partes[partes.length - 1]) : 64;
        this.pedaços.add(Pedaço.figura(Identifier.parse(partes[0]), largura, altura));
    }

    private static int parse(String oquê) {
        try {
            return Integer.parseInt(oquê.trim());
        } catch (NumberFormatException erro) {
            return 0;
        }
    }

    // ------------------------------------------------------------------ e o desenho

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int rato, int ratoY, float parcial) {
        super.extractRenderState(graphics, rato, ratoY, parcial);

        int x = (this.width - LARGURA) / 2;
        int y = 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, FOLHA, x, y, 0.0f, 0.0f, LARGURA, ALTURA, 256, 256);

        /*
         * O capítulo mais cheio deste livro é o que traz uma figura no meio, e nem sempre cabe na folha. O
         * original não se preocupa com isso — a letra dele era a miúda, e o que sobrasse sobrava. Aqui a
         * folha <b>encolhe o bastante para caber</b>, que é a mesma folha e sem nada cortado.
         */
        int alto = this.altura();
        float encolhe = alto > CAIXA_ALTA ? (float) CAIXA_ALTA / alto : 1.0f;

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + MARGEM_X, y + MARGEM_Y);
        if (encolhe < 1.0f) pose.scale(encolhe, encolhe);

        int linha = 0;
        for (Pedaço pedaço : this.pedaços) {
            if (pedaço.figura() != null) {
                int meio = (CAIXA - pedaço.largura()) / 2;
                graphics.blit(RenderPipelines.GUI_TEXTURED, pedaço.figura(), meio, linha,
                        0.0f, 0.0f, pedaço.largura(), pedaço.altura(),
                        pedaço.largura(), pedaço.altura());
                linha += pedaço.altura() + 2;
                continue;
            }
            graphics.text(this.font, pedaço.linha(), 0, linha, TINTA);
            linha += this.font.lineHeight;
        }
        pose.popMatrix();

        this.setas(graphics, x, y);
    }

    /** Quanto a folha pede de altura, com as figuras contadas. */
    private int altura() {
        int alto = 0;
        for (Pedaço pedaço : this.pedaços) {
            alto += pedaço.figura() != null ? pedaço.altura() + 2 : this.font.lineHeight;
        }
        return alto;
    }

    /** As três setas, e a da frente apagada quando o livro não chega lá. */
    private void setas(GuiGraphicsExtractor graphics, int x, int y) {
        if (this.pilha.size() > 1) {
            graphics.text(this.font, Component.literal("<"), x + 34, y + 16, TINTA);
        }
        if (!this.seguinte.isEmpty()) {
            boolean pode = this.páginas >= this.seguintePede;
            graphics.text(this.font, Component.literal(">"), x + 150, y + 16,
                    pode ? TINTA : 0xFFB0A89A);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent clique, boolean duplo) {
        int x = (this.width - LARGURA) / 2;
        double rx = clique.x() - x;
        double ry = clique.y() - 2;

        if (rx >= 148 && rx <= 148 + SETA_LARGURA && ry >= 12 && ry <= 12 + SETA_ALTURA) {
            this.vaiAoSeguinte();
            return true;
        }
        if (rx >= 32 && rx <= 32 + SETA_LARGURA && ry >= 12 && ry <= 12 + SETA_ALTURA) {
            this.volta();
            return true;
        }
        return super.mouseClicked(clique, duplo);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent tecla) {
        if (tecla.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            this.vaiAoSeguinte();
            return true;
        }
        if (tecla.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            this.volta();
            return true;
        }
        return super.keyPressed(tecla);
    }

    /** A seta da frente, que <b>não anda</b> se o livro não tiver as páginas. */
    private void vaiAoSeguinte() {
        if (this.seguinte.isEmpty() || this.páginas < this.seguintePede) return;
        this.pilha.add(this.seguinte);
        this.monta();
    }

    private void volta() {
        if (this.pilha.size() <= 1) return;
        this.pilha.remove(this.pilha.size() - 1);
        this.monta();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
