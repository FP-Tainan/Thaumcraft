package net.thaumcraft.arcana.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.OcculusMenu;
import net.thaumcraft.arcana.SkillData;
import net.thaumcraft.arcana.SkillTree;

import java.util.ArrayList;
import java.util.List;

/**
 * A árvore de perícias vista: a {@code GuiSkillTrees} do Ars Magica 2.
 *
 * <p>Três ramos, um por aba — <b>Ofensa</b>, <b>Defesa</b> e <b>Utilidade</b> —, e em cada um as perícias nos
 * lugares onde o original as desenhou, ligadas por linhas ao que elas pedem.
 *
 * <p>Uma perícia se lê pela cor: <b>acesa</b> é sabida, <b>clara</b> é comprável agora, <b>escura</b> é
 * trancada. A cor da moldura é a do ponto que ela custa — azul, verde ou vermelho —, e é ela que diz quando
 * ela vai estar ao alcance: o azul até o nível vinte, o verde até o quarenta, o vermelho até o cinquenta.
 *
 * <p><b>Desvio declarado:</b> a do original rola livremente e tem seis abas, com as perícias que não são peças
 * de feitiço (talentos, familiares, afinidade). Aqui são três abas e o quadro é encolhido para caber inteiro
 * na tela, porque as 32 peças deste porte cabem — as 120 do original não caberiam.
 */
public class SkillTreeScreen extends AbstractContainerScreen<OcculusMenu> {
    /** O quanto o quadro do original é encolhido para caber. */
    private static final float ESCALA = 0.42f;

    /** O tamanho de cada perícia desenhada. */
    private static final int PEÇA = 16;

    private static final int FUNDO = 0xFF1A1A22;
    private static final int LINHA_SABIDA = 0xFFDDDDDD;
    private static final int LINHA_TRANCADA = 0xFF4A4A55;
    private static final int ABA_ATIVA = 0xFF3A3A48;
    private static final int ABA_PARADA = 0xFF22222C;

    private SkillTree.Branch aba = SkillTree.Branch.OFFENSE;

    /** Onde cada perícia ficou desenhada, para o clique saber em qual se clicou. */
    private final List<Posta> postas = new ArrayList<>();

    private record Posta(int index, SkillTree.Entry perícia, int x, int y) {
    }

    public SkillTreeScreen(OcculusMenu menu, Inventory mochila, Component título) {
        super(menu, mochila, título, 320, 200);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    // ------------------------------------------------------------------ o quadro

    /** Onde uma perícia fica na tela, do lugar que ela tem no quadro do original. */
    private int telaX(SkillTree.Entry qual) {
        return this.leftPos + 10 + Math.round(qual.x() * ESCALA);
    }

    private int telaY(SkillTree.Entry qual) {
        return this.topPos + 30 + Math.round(qual.y() * ESCALA);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractBackground(g, mouseX, mouseY, partial);
        this.postas.clear();
        if (this.minecraft == null || this.minecraft.player == null) return;

        SkillData sabe = SkillData.of(this.minecraft.player);
        int nível = Mana.of(this.minecraft.player).level();

        int x = this.leftPos;
        int y = this.topPos;
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000);
        g.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, FUNDO);

        abas(g, x, y);
        pontos(g, x, y, sabe, nível);

        List<SkillTree.Entry> quadro = SkillTree.entries();
        List<SkillTree.Entry> doRamo = SkillTree.of(this.aba);

        // primeiro as linhas, para ficarem por baixo
        for (SkillTree.Entry perícia : doRamo) {
            for (var precisa : perícia.needs()) {
                SkillTree.Entry antes = SkillTree.of(precisa);
                if (antes == null || antes.branch() != this.aba) continue;
                linha(g, telaX(antes) + PEÇA / 2, telaY(antes) + PEÇA / 2,
                        telaX(perícia) + PEÇA / 2, telaY(perícia) + PEÇA / 2,
                        sabe.knows(precisa) ? LINHA_SABIDA : LINHA_TRANCADA);
            }
        }

        for (SkillTree.Entry perícia : doRamo) {
            int px = telaX(perícia);
            int py = telaY(perícia);
            this.postas.add(new Posta(quadro.indexOf(perícia), perícia, px, py));

            boolean sabida = sabe.knows(perícia.part());
            boolean dá = sabe.canLearn(perícia, nível);

            // a moldura é da cor do ponto que ela custa
            int moldura = perícia.point().color | 0xFF000000;
            g.fill(px - 1, py - 1, px + PEÇA + 1, py + PEÇA + 1,
                    sabida || dá ? moldura : escurecer(moldura));
            g.fill(px, py, px + PEÇA, py + PEÇA, sabida ? 0xFF000000 : 0xCC000000);
        }
    }

    /** Uma linha entre duas perícias, desenhada a pontos porque a tela só sabe encher retângulos. */
    private static void linha(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int cor) {
        int passos = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (passos == 0) return;
        for (int i = 0; i <= passos; i++) {
            int x = x1 + (x2 - x1) * i / passos;
            int y = y1 + (y2 - y1) * i / passos;
            g.fill(x, y, x + 1, y + 1, cor);
        }
    }

    /** A mesma cor, mais escura: o que marca uma perícia fora de alcance. */
    private static int escurecer(int cor) {
        int r = (cor >> 16 & 0xFF) / 4;
        int gr = (cor >> 8 & 0xFF) / 4;
        int b = (cor & 0xFF) / 4;
        return 0xFF000000 | r << 16 | gr << 8 | b;
    }

    private void abas(GuiGraphicsExtractor g, int x, int y) {
        int i = 0;
        for (SkillTree.Branch ramo : SkillTree.Branch.values()) {
            int ax = x + 8 + i * 84;
            g.fill(ax, y + 6, ax + 80, y + 22, ramo == this.aba ? ABA_ATIVA : ABA_PARADA);
            Component nome = Component.translatable(ramo.key());
            g.text(this.font, nome, ax + 40 - this.font.width(nome) / 2 - this.leftPos, y + 11 - this.topPos,
                    ramo == this.aba ? 0xFFFFFFFF : 0xFF888888, false);
            i++;
        }
    }

    /** Os pontos que sobram de cada cor, e o nível de quem olha. */
    private void pontos(GuiGraphicsExtractor g, int x, int y, SkillData sabe, int nível) {
        Component fala = Component.translatable("tc.spell.level", nível);
        g.text(this.font, fala, this.imageWidth - this.font.width(fala) - 8, 11, 0xFFAAAAAA, false);

        int px = 8;
        for (SkillTree.Point cor : SkillTree.Point.values()) {
            Component quantos = Component.literal(String.valueOf(sabe.free(cor, nível)));
            g.fill(x + px, y + this.imageHeight - 16, x + px + 8, y + this.imageHeight - 8,
                    cor.color | 0xFF000000);
            g.text(this.font, quantos, px + 11, this.imageHeight - 16, 0xFFFFFFFF, false);
            px += 11 + this.font.width(quantos) + 8;
        }
    }

    // ------------------------------------------------------------------ as figuras e os cliques

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // as figuras das peças, por cima das molduras
        for (Posta posta : this.postas) {
            var item = ArcanaItems.itemOf(posta.perícia().part());
            if (item == null) continue;
            g.item(new ItemStack(item), posta.x() - this.leftPos, posta.y() - this.topPos);
        }
        dica(g, mouseX, mouseY);
    }

    /** A dica da perícia sob o cursor: o nome, o que ela pede e o que ela custa. */
    private void dica(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        SkillData sabe = SkillData.of(this.minecraft.player);
        int nível = Mana.of(this.minecraft.player).level();

        for (Posta posta : this.postas) {
            if (mouseX < posta.x() || mouseX >= posta.x() + PEÇA) continue;
            if (mouseY < posta.y() || mouseY >= posta.y() + PEÇA) continue;

            var linhas = new ArrayList<Component>();
            var item = ArcanaItems.itemOf(posta.perícia().part());
            if (item != null) linhas.add(new ItemStack(item).getHoverName());

            if (sabe.knows(posta.perícia().part())) {
                linhas.add(Component.translatable("tc.spell.skill.known")
                        .withStyle(net.minecraft.ChatFormatting.GREEN));
            } else {
                linhas.add(Component.translatable("tc.spell.skill.costs",
                                Component.translatable(posta.perícia().point().key()))
                        .withStyle(estilo -> estilo.withColor(posta.perícia().point().color)));
                for (var precisa : posta.perícia().needs()) {
                    if (sabe.knows(precisa)) continue;
                    var antes = ArcanaItems.itemOf(precisa);
                    linhas.add(Component.translatable("tc.spell.skill.needs",
                                    antes == null ? Component.literal(precisa.name())
                                            : new ItemStack(antes).getHoverName())
                            .withStyle(net.minecraft.ChatFormatting.RED));
                }
                if (sabe.free(posta.perícia().point(), nível) <= 0) {
                    linhas.add(Component.translatable("tc.spell.skill.no_points")
                            .withStyle(net.minecraft.ChatFormatting.RED));
                }
            }
            g.setComponentTooltipForNextFrame(this.font, linhas, mouseX, mouseY);
            return;
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent evento, boolean duplo) {
        double mouseX = evento.x();
        double mouseY = evento.y();
        // as abas
        for (int i = 0; i < SkillTree.Branch.values().length; i++) {
            int ax = this.leftPos + 8 + i * 84;
            if (mouseX >= ax && mouseX < ax + 80
                    && mouseY >= this.topPos + 6 && mouseY < this.topPos + 22) {
                this.aba = SkillTree.Branch.values()[i];
                return true;
            }
        }

        // e as perícias
        for (Posta posta : this.postas) {
            if (mouseX < posta.x() || mouseX >= posta.x() + PEÇA) continue;
            if (mouseY < posta.y() || mouseY >= posta.y() + PEÇA) continue;
            if (this.minecraft == null || this.minecraft.gameMode == null) return true;
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, posta.index());
            return true;
        }
        return super.mouseClicked(evento, duplo);
    }

}
