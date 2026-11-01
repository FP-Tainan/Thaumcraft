package net.thaumcraft.occulta.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.biome.BiomeBookChoice;
import net.thaumcraft.occulta.biome.BiomeBookItem;
import net.thaumcraft.occulta.biome.BiomeCatalogue;

import java.util.List;

/**
 * A folha do <b>Livro de Biomas</b>: o {@code GuiScreenBiomeBook} do Witchery.
 *
 * <p>Uma folha por bioma, com o nome dele em cima e quatro linhas do que se sabe dele abaixo; a conta de
 * folhas no alto à direita; as duas setas embaixo; e, encostados na beira direita, os <b>marcadores</b> —
 * um por seção, que saltam para o primeiro bioma dela.
 *
 * <p>A folha em que ele fica <b>vai para o servidor</b>, porque é lá que o item vive: cada virada manda o
 * recado do {@link BiomeBookChoice}. É o {@code sendBookToServer} do original, só que o recado nomeia o
 * bioma e não a folha.
 *
 * <h2>Os dois desvios do que se mostra</h2>
 *
 * <p>O original mostra <b>cinco</b> coisas de cada bioma: o nome, a <b>chuva</b> (um número de zero a um),
 * a <b>temperatura</b> (outro número, com «(úmido)» atrás quando a chuva passava de 0,85), se <b>neva</b> e
 * se cai <b>raio</b>.
 *
 * <p><b>Desvio declarado:</b> o número da chuva <b>não existe mais</b>. Em 2014 cada bioma tinha um
 * {@code rainfall} aberto a quem perguntasse; hoje o jogo guarda só um «chove ou não chove», e o resto está
 * trancado. A linha da chuva passa a ser um <b>sim ou não</b>, e com ela cai o «(úmido)», que saía do número.
 *
 * <p><b>E o que neva e o que troveja saem do lugar onde se lê.</b> O original tinha dois interruptores no
 * bioma; hoje quem decide é a <b>altura</b> — o mesmo bioma neva no alto da serra e chove na praia dele. O
 * livro responde <b>onde quem o lê está</b>, que é a resposta honesta e a mais útil: é a altura em que essa
 * pessoa vai cozinhar.
 */
@Environment(EnvType.CLIENT)
public class BiomeBookScreen extends Screen {
    /** A folha, que é a mesma dos outros livros do ramo. */
    private static final Identifier FOLHA = Thaumcraft.id("textures/gui/book_single.png");

    public static final int LARGURA = 192;
    public static final int ALTURA = 192;
    public static final int MARGEM_X = 36;
    public static final int MARGEM_Y = 32;
    public static final int CAIXA = 116;

    /** A cor da tinta, a dos atalhos e a de um marcador. */
    public static final int TINTA = 0xFF3A2E1F;
    public static final int APAGADO = 0xFFB0A89A;

    /** E a letra vai sem sombra, como a do outro livro: ver o {@link MarkupBookScreen#SEM_SOMBRA}. */
    public static final boolean SEM_SOMBRA = MarkupBookScreen.SEM_SOMBRA;

    /**
     * A <b>aba</b> de um marcador: o pedaço {@code (26, 220)} da folha dupla do original, com a versão
     * acesa logo abaixo dela.
     *
     * <p>Sem ela os quinze nomes ficavam escritos <b>no ar</b>, à direita do livro, por cima do mundo — que
     * é o que acontece quando se porta um botão sem portar o desenho dele.
     */
    private static final Identifier ABA = Thaumcraft.id("textures/gui/book_bookmark.png");

    /** Onde os marcadores ficam, e de quanto em quanto: o {@code mid + 160} e o {@code 12 * i + 10}. */
    public static final int MARCADOR_X = 160;
    public static final int MARCADOR_Y = 10;
    public static final int MARCADOR_ALTO = 12;

    /** E o tamanho de cada aba: os sessenta por onze do original. */
    public static final int ABA_LARGA = 60;
    public static final int ABA_ALTA = 11;

    private final ItemStack livro;
    private List<BiomeCatalogue.Página> páginas = List.of();
    private List<Integer> marcadores = List.of();
    private int folha;

    public BiomeBookScreen(ItemStack livro) {
        super(livro.getHoverName());
        this.livro = livro;
    }

    @Override
    protected void init() {
        var biomas = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BIOME);
        this.páginas = BiomeCatalogue.páginas(biomas);
        this.marcadores = BiomeCatalogue.marcadores(this.páginas);
        this.folha = BiomeCatalogue.folhaDe(this.páginas, BiomeBookItem.bioma(this.livro));
    }

    // ------------------------------------------------------------------ virar a folha

    /**
     * O {@code storeCurrentPage} e o {@code sendBookToServer} juntos: a folha escreve-se no livro de cá e o
     * recado vai para lá.
     *
     * <p>No original são dois momentos — escrever a cada virada, mandar ao fechar. Aqui manda-se a cada
     * virada, que é um recado de dezenas de bytes e poupa o caso de o livro sair da mão sem fechar a folha.
     */
    private void vira(int para) {
        if (para < 0 || para >= this.páginas.size() || para == this.folha) return;
        this.folha = para;
        var qual = this.páginas.get(this.folha).qual();
        BiomeBookItem.abre(this.livro, qual);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
                .send(new BiomeBookChoice.Abriu(qual));
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent clique, boolean duplo) {
        int x = (this.width - LARGURA) / 2;
        double rx = clique.x() - x;
        double ry = clique.y() - 2;

        if (rx >= 118 && rx <= 136 && ry >= 154 && ry <= 164) {
            this.vira(this.folha + 1);
            return true;
        }
        if (rx >= 36 && rx <= 54 && ry >= 154 && ry <= 164) {
            this.vira(this.folha - 1);
            return true;
        }
        for (int i = 0; i < this.marcadores.size(); i++) {
            int topo = MARCADOR_ALTO * i + MARCADOR_Y;
            if (rx < MARCADOR_X || rx > MARCADOR_X + ABA_LARGA) continue;
            if (ry < topo || ry > topo + ABA_ALTA) continue;
            this.vira(this.marcadores.get(i));
            return true;
        }
        return super.mouseClicked(clique, duplo);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent tecla) {
        if (tecla.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            this.vira(this.folha + 1);
            return true;
        }
        if (tecla.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            this.vira(this.folha - 1);
            return true;
        }
        return super.keyPressed(tecla);
    }

    // ------------------------------------------------------------------ e o desenho

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int rato, int ratoY, float parcial) {
        super.extractRenderState(graphics, rato, ratoY, parcial);

        int x = (this.width - LARGURA) / 2;
        int y = 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, FOLHA, x, y, 0.0f, 0.0f, LARGURA, ALTURA, 256, 256);

        if (this.páginas.isEmpty()) return;

        // a conta de folhas, encostada à direita como no original
        Component conta = Component.translatable("book.pageIndicator",
                this.folha + 1, this.páginas.size());
        graphics.text(this.font, conta,
                x + LARGURA - 44 - this.font.width(conta), y + 16, TINTA, SEM_SOMBRA);

        var página = this.páginas.get(this.folha);
        int linha = y + MARGEM_Y;
        graphics.text(this.font, página.nome(), x + MARGEM_X, linha, TINTA, SEM_SOMBRA);
        linha += this.font.lineHeight * 2;

        for (Component conta2 : this.oQueSeSabe(página)) {
            graphics.text(this.font, conta2, x + MARGEM_X, linha, TINTA, SEM_SOMBRA);
            linha += this.font.lineHeight;
        }

        /*
         * Os marcadores, cada um na sua aba: a aba acende debaixo do rato, como no original, e o nome da
         * seção em que estamos vai em tinta cheia enquanto os outros ficam desbotados.
         */
        for (int i = 0; i < this.marcadores.size(); i++) {
            var qual = this.páginas.get(this.marcadores.get(i)).seção();
            int aba = x + MARCADOR_X;
            int topo = y + MARCADOR_ALTO * i + MARCADOR_Y;
            boolean noRato = rato >= aba && rato <= aba + ABA_LARGA
                    && ratoY >= topo && ratoY <= topo + ABA_ALTA;
            graphics.blit(RenderPipelines.GUI_TEXTURED, ABA, aba, topo,
                    0.0f, noRato ? ABA_ALTA : 0.0f, ABA_LARGA, ABA_ALTA, ABA_LARGA, ABA_ALTA * 2);
            graphics.text(this.font, qual.rótuloEscrito(), aba + 2, topo + 2,
                    qual.equals(página.seção()) ? TINTA : APAGADO, SEM_SOMBRA);
        }

        // e as duas setas, apagadas nas pontas do livro
        graphics.text(this.font, Component.literal("<"), x + 38, y + 154,
                this.folha > 0 ? TINTA : APAGADO, SEM_SOMBRA);
        graphics.text(this.font, Component.literal(">"), x + 120, y + 154,
                this.folha < this.páginas.size() - 1 ? TINTA : APAGADO, SEM_SOMBRA);
    }

    /**
     * As quatro linhas do que se sabe de um bioma: chuva, calor, neve e raio.
     *
     * <p>A neve e o raio saem da <b>altura em que quem lê está</b> — ver o cabeçalho da classe.
     */
    private List<Component> oQueSeSabe(BiomeCatalogue.Página página) {
        var biomas = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BIOME);
        var bioma = biomas.getOrThrow(página.qual()).value();
        var onde = this.minecraft.player.blockPosition();
        int mar = this.minecraft.level.getSeaLevel();

        boolean chove = bioma.hasPrecipitation();
        boolean neva = chove && bioma.coldEnoughToSnow(onde, mar);
        return List.of(
                Component.translatable("tc.biomebook.rainfall", this.simOuNão(chove)),
                Component.translatable("tc.biomebook.temperature",
                        String.format(java.util.Locale.ROOT, "%.2f", bioma.getBaseTemperature())),
                Component.translatable("tc.biomebook.snows", this.simOuNão(neva)),
                Component.translatable("tc.biomebook.lightning", this.simOuNão(chove && !neva)));
    }

    private Component simOuNão(boolean qual) {
        return Component.translatable(qual ? "tc.yes" : "tc.no");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
