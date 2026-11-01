package net.thaumcraft.occulta.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <b>A folha de um livro de marcação</b>: o {@code GuiScreenMarkupBook} do Witchery.
 *
 * <p>Os livros do Witchery não são páginas escritas: são <b>textos com marcas</b> no arquivo de idioma, e a
 * folha monta-os de cada vez. As marcas que esta folha conhece:
 *
 * <ul>
 *   <li>{@code [br]} — quebra de linha;</li>
 *   <li>{@code [h1 ...]} — o título, em letra grande;</li>
 *   <li>{@code [i ...]} e {@code [darkred ...]} — um pedaço em itálico ou colorido;</li>
 *   <li>{@code [img=caminho|...|largura|altura]} — uma figura;</li>
 *   <li>{@code [stack=item|quantos|lado|altura]} — <b>uma coisa</b>, desenhada no meio do texto;</li>
 *   <li>{@code [url Capítulo]} e {@code [url=capítulo Rótulo]} — um <b>atalho</b> para outro capítulo;</li>
 *   <li>{@code [template=nome 0=... stack|1=...]} — um <b>molde</b>, que é outra marcação com buracos;</li>
 *   <li>{@code [next=capítulo|páginas]} — para onde a seta leva, e <b>quantas páginas ela pede</b>.</li>
 * </ul>
 *
 * <p>É essa última que faz do livro o que ele é. Um capítulo que peça mais páginas do que o exemplar tem
 * <b>não abre</b>: a seta fica apagada, e quem a carrega vê que há mais folha do lado de lá e não sabe o
 * quê. O Witchery nunca escreve "falta-te uma página" — ele <b>mostra a seta apagada</b>.
 *
 * <h2>O texto corre, e as coisas correm com ele</h2>
 *
 * <p>A folha monta-se com um <b>cursor</b> que anda da esquerda para a direita e desce quando não cabe —
 * como o original. É isso que deixa uma receita de bancada ser nove coisas em três linhas em vez de nove
 * linhas, e um atalho ser uma palavra no meio de uma frase em vez de um parágrafo.
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

    /**
     * E a letra vai <b>sem sombra</b>.
     *
     * <p>O {@code drawString} de 2014 não punha sombra nenhuma, e o de hoje põe se ninguém disser o
     * contrário. Uma folha de livro com letra sombreada parece um painel e não uma página: a sombra
     * engrossa cada letra e come o pouco contraste que tinta castanha sobre pergaminho tem.
     */
    public static final boolean SEM_SOMBRA = false;

    /** A cor de um atalho, e a dele debaixo do rato: o azul e o vermelho do original. */
    public static final int ELO = 0xFF0000FF;
    public static final int ELO_NO_RATO = 0xFFFF0000;

    /** O quadrado de uma coisa desenhada no texto: os dezoito do original. */
    public static final int COISA = 18;

    /** E o que a linha de um título mede: uma linha e meia, como no {@code adjustLineHeight} do original. */
    public static final float TÍTULO_ALTO = 1.5f;

    /** As três setas: voltar ao princípio, voltar uma, e seguir. */
    public static final int SETA_LARGURA = 18;
    public static final int SETA_ALTURA = 10;

    private final String raiz;
    private final int páginas;
    private final List<String> pilha = new ArrayList<>();

    private final List<Peça> peças = new ArrayList<>();
    private String seguinte = "";
    private int seguintePede;
    private int alto;

    /** Onde o cursor está, enquanto a folha se monta. */
    private int x;
    private int y;
    private int altoDaLinha;

    /**
     * Uma peça já <b>posta</b> na folha: ela sabe onde está.
     *
     * <p>É a diferença entre esta folha e a de antes: dantes uma peça era uma linha, e agora é uma coisa
     * num lugar. Sem isso não há como pôr uma palavra ao lado de uma panela.
     */
    private record Peça(int x, int y, int largura, int altura,
                        @Nullable FormattedCharSequence linha,
                        @Nullable Identifier figura,
                        @Nullable ItemStack coisa,
                        @Nullable String atalho) {
        static Peça texto(int x, int y, int largura, int altura, FormattedCharSequence linha) {
            return new Peça(x, y, largura, altura, linha, null, null, null);
        }

        static Peça figura(int x, int y, int largura, int altura, Identifier qual) {
            return new Peça(x, y, largura, altura, null, qual, null, null);
        }

        static Peça coisa(int x, int y, ItemStack qual) {
            return new Peça(x, y, COISA, COISA, null, null, qual, null);
        }

        static Peça atalho(int x, int y, int largura, int altura, FormattedCharSequence linha,
                           String para) {
            return new Peça(x, y, largura, altura, linha, null, null, para);
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
     * Lê a marcação do capítulo em que estamos e <b>põe</b> cada pedaço no seu lugar.
     *
     * <p>O original percorre o texto letra a letra e empilha elementos, e depois os desenha com um cursor;
     * aqui as duas coisas acontecem de uma vez, que dá o mesmo e poupa uma lista.
     */
    private void monta() {
        this.peças.clear();
        this.seguinte = "";
        this.seguintePede = 0;
        this.x = 0;
        this.y = 0;
        this.altoDaLinha = this.font.lineHeight;

        String chave = this.raiz + "." + this.pilha.get(this.pilha.size() - 1);
        String marcação = Language.getInstance().getOrDefault(chave, "");
        if (marcação.equals(chave)) {
            this.alto = 0;
            return;
        }
        marcação = this.abreOsMoldes(marcação);

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
                this.escreve(linha.toString(), estilo);
                linha.setLength(0);
                this.novaLinha();
                this.novaLinha();
                continue;
            }
            if (marca.startsWith("next=")) {
                String[] partes = marca.substring(5).split("\\|");
                this.seguinte = partes[0];
                this.seguintePede = partes.length > 1 ? parse(partes[1]) : 0;
                continue;
            }
            this.escreve(linha.toString(), estilo);
            linha.setLength(0);

            if (marca.startsWith("img=")) {
                this.figura(marca.substring(4));
            } else if (marca.startsWith("stack=")) {
                /*
                 * «[stack=coisa rótulo]»: o que vem antes do primeiro espaço é a coisa, e o resto é
                 * <b>texto</b> escrito ao lado dela. É assim que o original faz uma lista de temperos —
                 * cada linha é uma coisa e o que ela faz.
                 */
                String dentro = marca.substring(6);
                int espaço = dentro.indexOf(' ');
                this.coisa(espaço < 0 ? dentro : dentro.substring(0, espaço));
                if (espaço >= 0) this.escreve(dentro.substring(espaço), estilo);
            } else if (marca.startsWith("url")) {
                this.elo(marca, estilo);
            } else if (marca.startsWith("h1 ")) {
                /*
                 * O título: <b>azul-escuro e itálico</b> — o {@code §3§o} do original —, e <b>a linha
                 * acaba nele</b>, com uma linha e meia de altura. Sem essa quebra o parágrafo seguinte
                 * começa colado ao título, e lê-se «Cauldron: RecipeA witch's cauldron...».
                 */
                this.escreve(marca.substring(3),
                        estilo.withItalic(true).withColor(ChatFormatting.DARK_AQUA));
                this.altoDaLinha = (int) Math.ceil(this.altoDaLinha * TÍTULO_ALTO);
                this.novaLinha();
            } else if (marca.startsWith("i ")) {
                this.escreve(marca.substring(2), estilo.withItalic(true));
            } else if (marca.startsWith("darkred ")) {
                this.escreve(marca.substring(8), estilo.withColor(ChatFormatting.DARK_RED));
            }
            // uma marca que este porte não conhece some, como no original
        }
        this.escreve(linha.toString(), estilo);
        this.novaLinha();
        this.alto = this.y;
    }

    /**
     * <b>Abre os moldes</b>: o {@code template} do original.
     *
     * <p>Um molde é outra marcação, guardada numa chave do mesmo livro, com buracos numerados. A marca que
     * o chama diz o que vai em cada buraco — e um buraco pedido como {@code stack|N} recebe uma fila de
     * coisas separadas por vírgula, cada uma virando a sua marca.
     *
     * <p>Faz-se antes de tudo, sobre o texto cru, porque é o que o original faz: o molde aberto volta para
     * a marcação e é lido como se estivesse escrito ali.
     */
    private String abreOsMoldes(String marcação) {
        for (int volta = 0; volta < 8 && marcação.contains("[template="); volta++) {
            int abre = marcação.indexOf("[template=");
            int fecha = marcação.indexOf(']', abre);
            if (fecha < 0) break;
            String dentro = marcação.substring(abre + "[template=".length(), fecha);
            int espaço = dentro.indexOf(' ');
            String nome = espaço < 0 ? dentro : dentro.substring(0, espaço);
            String resto = espaço < 0 ? "" : dentro.substring(espaço + 1);

            String chave = this.raiz + "." + nome;
            String molde = Language.getInstance().getOrDefault(chave, "");
            if (molde.equals(chave)) molde = "";

            String[] pares = resto.isEmpty() ? new String[0] : resto.split("\\s+");
            Object[] buracos = new Object[Math.max(pares.length, 1)];
            java.util.Arrays.fill(buracos, "");
            for (String par : pares) {
                int igual = par.indexOf('=');
                if (igual < 0) continue;
                String onde = par.substring(0, igual);
                String oquê = par.substring(igual + 1);
                if (onde.matches("\\d+")) {
                    buracos[Math.min(parse(onde), buracos.length - 1)] = oquê;
                    continue;
                }
                if (!onde.matches("stack\\|\\d+")) continue;
                StringBuilder fila = new StringBuilder();
                for (String coisa : oquê.split(",")) fila.append("[stack=").append(coisa).append(']');
                buracos[Math.min(parse(onde.substring(onde.indexOf('|') + 1)), buracos.length - 1)] =
                        fila.toString();
            }

            String aberto;
            try {
                aberto = molde.isEmpty() ? "" : String.format(molde, buracos);
            } catch (java.util.IllegalFormatException erro) {
                aberto = "";
            }
            marcação = marcação.substring(0, abre) + aberto + marcação.substring(fecha + 1);
        }
        return marcação;
    }

    /** Escreve um pedaço de texto, palavra a palavra, descendo quando não cabe. */
    private void escreve(String oquê, Style estilo) {
        if (oquê.isEmpty()) return;
        int desde = 0;
        while (desde < oquê.length()) {
            int espaço = oquê.indexOf(' ', desde);
            String palavra = espaço < 0 ? oquê.substring(desde) : oquê.substring(desde, espaço + 1);
            desde = espaço < 0 ? oquê.length() : espaço + 1;
            if (palavra.isEmpty()) continue;

            FormattedText pedaço = FormattedText.of(palavra, estilo);
            int largo = this.font.width(pedaço);
            if (this.x > 0 && this.x + largo > CAIXA) this.novaLinha();
            this.peças.add(Peça.texto(this.x, this.y, largo, this.font.lineHeight,
                    Language.getInstance().getVisualOrder(pedaço)));
            this.x += largo;
        }
    }

    /** Um atalho: {@code [url Capítulo]} ou {@code [url=capítulo Rótulo]}. */
    private void elo(String marca, Style estilo) {
        String resto = marca.substring(3);
        String para = "";
        if (resto.startsWith("=")) {
            int espaço = resto.indexOf(' ');
            para = espaço < 0 ? resto.substring(1) : resto.substring(1, espaço);
            resto = espaço < 0 ? "" : resto.substring(espaço + 1);
        } else if (resto.startsWith(" ")) {
            resto = resto.substring(1);
        }
        if (resto.isEmpty()) return;

        /*
         * Sem alvo escrito, o alvo é o <b>próprio rótulo</b>, em letra pequena e sem espaços — é o que o
         * {@code GuiButtonUrl} do original faz, e é o que torna «[url Brewing]» um atalho para o capítulo
         * «brewing» sem se dizer duas vezes.
         */
        int barra = para.indexOf('|');
        if (barra >= 0) para = para.substring(0, barra);
        String alvo = (para.isEmpty() ? resto : para).toLowerCase(Locale.ROOT).replace(" ", "");

        FormattedText pedaço = FormattedText.of(resto, estilo.withUnderlined(true));
        int largo = this.font.width(pedaço);
        if (this.x > 0 && this.x + largo > CAIXA) this.novaLinha();
        this.peças.add(Peça.atalho(this.x, this.y, largo, this.font.lineHeight,
                Language.getInstance().getVisualOrder(pedaço), alvo));
        this.x += largo;
    }

    /** Uma figura: o caminho vem primeiro e os dois últimos números são o tamanho. */
    private void figura(String oquê) {
        String[] partes = oquê.split("\\|");
        if (partes.length == 0) return;
        int largura = partes.length >= 3 ? parse(partes[partes.length - 2]) : 64;
        int altura = partes.length >= 2 ? parse(partes[partes.length - 1]) : 64;
        if (this.x > 0 && this.x + largura > CAIXA) this.novaLinha();
        this.peças.add(Peça.figura(this.x, this.y, largura, altura, Identifier.parse(partes[0])));
        this.x += largura;
        this.altoDaLinha = Math.max(this.altoDaLinha, altura);
    }

    /**
     * Uma coisa no meio do texto: {@code [stack=item|quantos|lado|altura]}.
     *
     * <p>O original ainda lê um <b>dano</b> antes da quantidade, que era o jeito de 2014 de dizer qual das
     * coisas de um item só se queria. Aqui as coisas são itens por direito próprio, e o que vier em número
     * é a <b>quantidade</b>.
     *
     * <p>Um {@code empty} deixa o <b>buraco</b> e não desenha nada: é como o original escreve uma casa
     * vazia numa receita de bancada.
     */
    private void coisa(String oquê) {
        String[] partes = oquê.split("\\|");
        if (partes.length == 0 || partes[0].isEmpty()) return;
        if (this.x > 0 && this.x + COISA > CAIXA) this.novaLinha();
        this.altoDaLinha = Math.max(this.altoDaLinha, COISA);

        if (!partes[0].equals("empty")) {
            Identifier qual = Identifier.tryParse(partes[0]);
            Item item = qual == null ? net.minecraft.world.item.Items.AIR
                    : BuiltInRegistries.ITEM.getValue(qual);
            if (item != net.minecraft.world.item.Items.AIR) {
                int quantos = 1;
                for (int i = 1; i < partes.length; i++) {
                    if (!partes[i].matches("\\d+")) continue;
                    quantos = Math.max(1, parse(partes[i]));
                    break;
                }
                this.peças.add(Peça.coisa(this.x, this.y, new ItemStack(item, quantos)));
            }
        }
        this.x += COISA;
    }

    private void novaLinha() {
        this.y += this.altoDaLinha;
        this.x = 0;
        this.altoDaLinha = this.font.lineHeight;
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
        float encolhe = this.encolhe();

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + MARGEM_X, y + MARGEM_Y);
        if (encolhe < 1.0f) pose.scale(encolhe, encolhe);

        for (Peça peça : this.peças) {
            if (peça.figura() != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, peça.figura(), peça.x(), peça.y(),
                        0.0f, 0.0f, peça.largura(), peça.altura(), peça.largura(), peça.altura());
                continue;
            }
            if (peça.coisa() != null) {
                graphics.item(peça.coisa(), peça.x(), peça.y());
                graphics.itemDecorations(this.font, peça.coisa(), peça.x(), peça.y());
                continue;
            }
            if (peça.atalho() != null) {
                graphics.text(this.font, peça.linha(), peça.x(), peça.y(),
                        this.noRato(peça, x, y, encolhe, rato, ratoY) ? ELO_NO_RATO : ELO, SEM_SOMBRA);
                continue;
            }
            graphics.text(this.font, peça.linha(), peça.x(), peça.y(), TINTA, SEM_SOMBRA);
        }
        pose.popMatrix();

        this.setas(graphics, x, y);
    }

    private float encolhe() {
        return this.alto > CAIXA_ALTA ? (float) CAIXA_ALTA / this.alto : 1.0f;
    }

    /** Se o rato está em cima daquela peça, contando o encolhimento da folha. */
    private boolean noRato(Peça peça, int folhaX, int folhaY, float encolhe, double rato, double ratoY) {
        double px = folhaX + MARGEM_X + peça.x() * encolhe;
        double py = folhaY + MARGEM_Y + peça.y() * encolhe;
        return rato >= px && rato <= px + peça.largura() * encolhe
                && ratoY >= py && ratoY <= py + peça.altura() * encolhe;
    }

    /** As três setas, e a da frente apagada quando o livro não chega lá. */
    private void setas(GuiGraphicsExtractor graphics, int x, int y) {
        if (this.pilha.size() > 1) {
            graphics.text(this.font, Component.literal("<"), x + 34, y + 16, TINTA, SEM_SOMBRA);
        }
        if (!this.seguinte.isEmpty()) {
            boolean pode = this.páginas >= this.seguintePede;
            graphics.text(this.font, Component.literal(">"), x + 150, y + 16,
                    pode ? TINTA : 0xFFB0A89A, SEM_SOMBRA);
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

        float encolhe = this.encolhe();
        for (Peça peça : this.peças) {
            if (peça.atalho() == null) continue;
            if (!this.noRato(peça, x, 2, encolhe, clique.x(), clique.y())) continue;
            this.vaiA(peça.atalho());
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
        this.vaiA(this.seguinte);
    }

    /** E um atalho, que é a mesma coisa sem seta. */
    private void vaiA(String capítulo) {
        if (capítulo.isEmpty()) return;
        this.pilha.add(capítulo);
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
