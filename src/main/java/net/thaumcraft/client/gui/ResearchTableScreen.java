package net.thaumcraft.client.gui;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.inventory.ResearchTableMenu;
import net.thaumcraft.net.ResearchTablePayloads;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Hex;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchNote;
import net.thaumcraft.research.ResearchNotes;
import net.thaumcraft.research.Researches;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tela da mesa de pesquisa: o {@code GuiResearchTable} da 4.2.3.5, descompilado.
 *
 * <p>À esquerda, os aspectos que quem pesquisa conhece, cinco por coluna, com quantos pontos tem de cada e o
 * brilho dos que a mesa dá de bônus; embaixo deles, as duas casas de combinar e o botão que junta. À direita,
 * o pergaminho com o tabuleiro de hexágonos da nota, e runas fantasmas que aparecem e somem no papel.
 *
 * <p>Arrasta-se um aspecto da lista para uma casa vazia do tabuleiro para escrevê-lo, ou para as casas de
 * combinar. Clicando num aspecto escrito, ele se apaga. Quando dois vizinhos se ligam — um feito do outro —,
 * uma linha azul pulsa entre eles.
 */
public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/guiresearchtable2.png");
    private static final Identifier PARCHMENT = Thaumcraft.id("textures/misc/parchment3.png");
    private static final Identifier HEX1 = Thaumcraft.id("textures/gui/hex1.png");
    private static final Identifier HEX2 = Thaumcraft.id("textures/gui/hex2.png");
    private static final Identifier SCRIPT = Thaumcraft.id("textures/misc/script.png");
    private static final Identifier UNKNOWN = Thaumcraft.id("textures/aspects/_unknown.png");
    private static final Identifier BACK = Thaumcraft.id("textures/aspects/_back.png");
    private static final Identifier PARTICLES = Thaumcraft.id("textures/misc/particles.png");
    private static final int HEX_SIZE = 9;

    /** A luz que soma, textura e cor: o {@code glBlendFunc(SRC_ALPHA, ONE)} do destaque do hexágono. */
    private static final RenderPipeline GUI_ADDITIVE = RenderPipelines.register(RenderPipeline.builder()
            .withLocation(Thaumcraft.id("pipeline/gui_additive"))
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build());
    /** E a mesma soma sem textura, para as linhas das ligações. */
    private static final RenderPipeline GUI_ADDITIVE_FILL = RenderPipelines.register(RenderPipeline.builder()
            .withLocation(Thaumcraft.id("pipeline/gui_additive_fill"))
            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withVertexShader("core/gui")
            .withFragmentShader("core/gui")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build());

    private record Rune(int q, int r, long start, long decay, int rune) {
    }

    private final Map<String, Rune> runes = new HashMap<>();
    private final Map<String, Hex[]> lines = new HashMap<>();
    private final List<String> checked = new ArrayList<>();
    private final List<String> highlight = new ArrayList<>();
    private long lastRuneCheck;
    private long butcount2;
    private int page;
    private int lastPage;
    private Aspect select1;
    private Aspect select2;
    private Aspect dragged;
    private ResearchNote note;

    public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 255, 255);
    }

    private PlayerKnowledge knowledge() {
        return Knowledges.of(this.minecraft.player);
    }

    private AspectList bonus() {
        ResearchTableBlockEntity table = this.menu.entity();
        return table == null ? new AspectList() : table.bonus();
    }

    private boolean knows(String key) {
        return this.knowledge().hasResearch(key);
    }

    /** O que se conhece, com os pontos de cada: o {@code getAspectsDiscovered}. */
    private List<Aspect> discovered() {
        AspectList list = new AspectList();
        for (Aspect aspect : this.knowledge().discovered()) list.add(aspect, 1);
        return list.getAspectsSorted();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    // ----------------------------------------------------------------- o fundo

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        int x = this.leftPos, y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, 255, 167, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 40, y + 167, 0, 166, 184, 88, 256, 256);
        if (this.page < this.lastPage) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 51, y + 121, 208, 208, 24, 8, 256, 256);
        }
        if (this.page > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 27, y + 121, 184, 208, 24, 8, 256, 256);
        }
        long now = System.nanoTime();
        if (this.select1 != null && this.select2 != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 35, y + 139, 184, 184, 32, 16, 256, 256);
            if (this.butcount2 < now) this.drawOrb(graphics, x + 43, y + 139);
            else graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 35, y + 139, 184, 168, 32, 16, 256, 256);
        }
        this.note = ResearchNotes.get(this.menu.getSlot(ResearchTableBlockEntity.NOTE).getItem());
        if (this.knows("RESEARCHDUPE") && this.note != null && this.note.complete()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 37, y + 5, 232, 200, 24, 24, 256, 256);
        }
        this.drawAspects(graphics, x + 10, y + 40);
        this.drawSheet(graphics, x, y, mouseX, mouseY);
    }

    private void drawAspects(GuiGraphicsExtractor graphics, int x, int y) {
        List<Aspect> aspects = this.discovered();
        PlayerKnowledge knowledge = this.knowledge();
        AspectList bonus = this.bonus();
        this.lastPage = (aspects.size() - 20) / 5;
        int count = 0, drawn = 0;
        for (Aspect aspect : aspects) {
            if (++count - 1 >= this.page * 5 && drawn < 25) {
                boolean faded = knowledge.points(aspect) <= 0 && bonus.getAmount(aspect) <= 0;
                AspectTags.draw(graphics, this.font, x + drawn / 5 * 16, y + drawn % 5 * 16, aspect,
                        knowledge.points(aspect), bonus.getAmount(aspect), faded ? 0.33f : 1.0f, false,
                        this.ticks());
                drawn++;
            }
        }
        if (this.select1 != null && knowledge.points(this.select1) <= 0 && bonus.getAmount(this.select1) <= 0) {
            this.select1 = null;
        }
        if (this.select2 != null && knowledge.points(this.select2) <= 0 && bonus.getAmount(this.select2) <= 0) {
            this.select2 = null;
        }
        if (this.select1 != null) AspectTags.draw(graphics, this.font, x + 3, y + 99, this.select1, 0, 0, 1.0f, false, 0);
        if (this.select2 != null) AspectTags.draw(graphics, this.font, x + 61, y + 99, this.select2, 0, 0, 1.0f, false, 0);
    }

    private int ticks() {
        return this.minecraft.player == null ? 0 : this.minecraft.player.tickCount;
    }

    // ----------------------------------------------------------------- o pergaminho

    private void drawSheet(GuiGraphicsExtractor graphics, int x, int y, int mx, int my) {
        if (this.note == null || this.note.key().isEmpty()) {
            this.runes.clear();
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, PARCHMENT, x + 94, y + 8, 0, 0, 150, 150, 256, 256);
        Map<String, ResearchNote.Cell> cells = this.note.byKey();
        long time = System.currentTimeMillis();
        var random = this.minecraft.level.getRandom();
        // as runas fantasmas: de quatro em quatro de segundo, uma nova numa casa fora do tabuleiro
        if (this.lastRuneCheck < time) {
            this.lastRuneCheck = time + 250L;
            int k = random.nextInt(120) - 60;
            int l = random.nextInt(120) - 60;
            Hex hp = Hex.fromPixel(k, l, HEX_SIZE);
            if (!this.runes.containsKey(hp.key()) && !cells.containsKey(hp.key())) {
                this.runes.put(hp.key(), new Rune(hp.q(), hp.r(), time,
                        this.lastRuneCheck + 15000L + random.nextInt(10000), random.nextInt(16)));
            }
        }
        for (Rune rune : List.copyOf(this.runes.values())) {
            if (rune.decay < time) {
                this.runes.remove(rune.q + ":" + rune.r);
                continue;
            }
            Hex hex = new Hex(rune.q, rune.r);
            float progress = (float) (time - rune.start) / (float) (rune.decay - rune.start);
            float alpha = 0.5f;
            if (progress < 0.25f) alpha = progress * 2.0f;
            else if (progress > 0.5f) alpha = 1.0f - progress;
            this.drawRune(graphics, x + 169 + hex.pixelX(HEX_SIZE), y + 83 + hex.pixelY(HEX_SIZE), rune.rune,
                    alpha * 0.66f);
        }

        Hex hp = Hex.fromPixel(mx - (x + 169), my - (y + 83), HEX_SIZE);
        this.lines.clear();
        this.checked.clear();
        this.highlight.clear();
        PlayerKnowledge knowledge = this.knowledge();
        for (ResearchNote.Cell cell : cells.values()) {
            if (cell.type() == 1 && cell.aspectOrNull() != null && knowledge.hasDiscovered(cell.aspectOrNull())) {
                this.checkConnections(cells, cell.hex(), knowledge);
            }
        }
        for (Hex[] con : this.lines.values()) {
            this.drawLine(graphics, x + 169 + con[0].pixelX(HEX_SIZE), y + 83 + con[0].pixelY(HEX_SIZE),
                    x + 169 + con[1].pixelX(HEX_SIZE), y + 83 + con[1].pixelY(HEX_SIZE));
        }
        if (!this.note.complete()) {
            for (ResearchNote.Cell cell : cells.values()) {
                Hex hex = cell.hex();
                if (cell.type() != 1) {
                    if (hex.equals(hp)) this.drawHex(graphics, HEX2, GUI_ADDITIVE, hex, x + 169, y + 83, 0xFFFFFFFF);
                    this.drawHex(graphics, HEX1, RenderPipelines.GUI_TEXTURED, hex, x + 169, y + 83, 0x40FFFFFF);
                } else {
                    this.drawOrb(graphics, x + 161 + hex.pixelX(HEX_SIZE), y + 75 + hex.pixelY(HEX_SIZE));
                }
            }
        }
        for (ResearchNote.Cell cell : cells.values()) {
            Aspect aspect = cell.aspectOrNull();
            if (aspect == null) continue;
            Hex hex = cell.hex();
            double px = x + 161 + hex.pixelX(HEX_SIZE), py = y + 75 + hex.pixelY(HEX_SIZE);
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate((float) (px - Math.floor(px)), (float) (py - Math.floor(py)));
            int ix = (int) Math.floor(px), iy = (int) Math.floor(py);
            if (!knowledge.hasDiscovered(aspect)) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN, ix, iy, 0, 0, 16, 16, 16, 16, 0x80000000);
            } else if (cell.type() == 1 || this.highlight.contains(hex.key())) {
                AspectTags.draw(graphics, this.font, ix, iy, aspect, 0, 0, 1.0f, false, 0);
            } else if (cell.type() == 2) {
                AspectTags.draw(graphics, this.font, ix, iy, aspect, 0, 0, 0.66f, true, 0);
            }
            pose.popMatrix();
        }
    }

    private void checkConnections(Map<String, ResearchNote.Cell> cells, Hex hex, PlayerKnowledge knowledge) {
        this.checked.add(hex.key());
        for (int a = 0; a < 6; a++) {
            Hex target = hex.neighbour(a);
            ResearchNote.Cell there = cells.get(target.key());
            if (this.checked.contains(target.key()) || there == null || there.type() < 1) continue;
            if (!ResearchNotes.connects(knowledge, cells.get(hex.key()).aspectOrNull(), there.aspectOrNull())) continue;
            String k1 = hex.key() + ":" + target.key();
            String k2 = target.key() + ":" + hex.key();
            if (!this.lines.containsKey(k1) && !this.lines.containsKey(k2)) {
                this.lines.put(k1, new Hex[]{hex, target});
                this.highlight.add(target.key());
            }
            this.checkConnections(cells, target, knowledge);
        }
    }

    private void drawHex(GuiGraphicsExtractor graphics, Identifier texture, RenderPipeline pipeline, Hex hex, int x,
                         int y, int colour) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) (x + hex.pixelX(HEX_SIZE)), (float) (y + hex.pixelY(HEX_SIZE)));
        graphics.blit(pipeline, texture, -8, -8, 0, 0, 16, 16, 16, 16, colour);
        pose.popMatrix();
    }

    /** A linha azul que pulsa entre dois aspectos ligados: três pontos de largura, somando luz. */
    private void drawLine(GuiGraphicsExtractor graphics, double x, double y, double x2, double y2) {
        float alpha = 0.3f + Mth.sin((float) (this.ticks() + x)) * 0.3f + 0.3f;
        int colour = Mth.clamp((int) (alpha * 255.0f), 0, 255) << 24 | 0x0099CC;
        double dx = x2 - x, dy = y2 - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) x, (float) y);
        pose.rotate((float) Math.atan2(dy, dx));
        pose.scale(length / 64.0f, 1.0f);
        graphics.fill(GUI_ADDITIVE_FILL, 0, -1, 64, 2, colour);
        pose.popMatrix();
    }

    /** Uma runa da tira de dezesseis, preta e meio apagada, deitada. */
    private void drawRune(GuiGraphicsExtractor graphics, double x, double y, int rune, float alpha) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) x, (float) y);
        pose.rotate((float) Math.toRadians(-90.0));
        int colour = Mth.clamp((int) (alpha * 255.0f), 0, 255) << 24;
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCRIPT, -5, -5, 16 * rune, 0, 10, 10, 16, 16, 256, 16, colour);
        pose.popMatrix();
    }

    /** O orbe que marca os aspectos que a pesquisa pede, mudando de cor devagar. */
    private void drawOrb(GuiGraphicsExtractor graphics, double x, double y) {
        int count = this.ticks();
        float red = 0.7f + Mth.sin((float) ((count + x) / 10.0)) * 0.15f + 0.15f;
        float green = 0.7f + Mth.sin((float) ((count + x + y) / 11.0)) * 0.15f + 0.15f;
        float blue = 0.7f + Mth.sin((float) ((count + y) / 12.0)) * 0.15f + 0.15f;
        int colour = 0xFF000000 | (int) (Mth.clamp(red, 0, 1) * 255) << 16 | (int) (Mth.clamp(green, 0, 1) * 255) << 8
                | (int) (Mth.clamp(blue, 0, 1) * 255);
        this.drawOrb(graphics, x, y, colour);
    }

    private void drawOrb(GuiGraphicsExtractor graphics, double x, double y, int colour) {
        int part = this.ticks() % 8;
        // o original anda meio quadro por vez e dá a volta na folha: são as colunas pares da linha nove
        float u = (0.5f + part / 8.0f) % 1.0f;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) x, (float) y);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PARTICLES, 0, 0, u * 256.0f, 128.0f, 16, 16, 16, 16, 256, 256,
                0xFF000000 | colour);
        pose.popMatrix();
    }

    // ----------------------------------------------------------------- por cima

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);
        int gx = this.leftPos, gy = this.topPos;
        if (this.note != null && this.knows("RESEARCHDUPE") && this.note.complete()) {
            int dx = mouseX - (gx + 37), dy = mouseY - (gy + 5);
            if (dx >= 0 && dy >= 0 && dx < 24 && dy < 24) {
                Research research = Researches.get(this.note.key());
                if (research != null) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, gx + 100, gy + 21, 184, 224, 48, 16, 256, 256);
                    AspectList cost = research.tags().copy();
                    for (Aspect aspect : cost.getAspects()) cost.add(aspect, this.note.copies());
                    int count = 0;
                    for (Aspect aspect : cost.getAspectsSorted()) {
                        AspectTags.draw(graphics, this.font, gx + 148 + count * 16, gy + 21, aspect,
                                cost.getAmount(aspect), 0, 1.0f, false, 0);
                        count++;
                    }
                    graphics.text(this.font, Component.translatable("tc.research.copy"), gx + 100, gy + 12, 0xFFFFFFFF, true);
                }
            }
        }
        if (this.dragged != null) this.drawOrb(graphics, mouseX - 8, mouseY - 8, this.dragged.color());
        this.drawAspectText(graphics, gx + 10, gy + 40, mouseX, mouseY);

        ItemStack ink = this.menu.getSlot(ResearchTableBlockEntity.INK).getItem();
        if (this.note != null && (ink.isEmpty() || ink.getDamageValue() >= ink.getMaxDamage())) {
            List<Component> lines = List.of(Component.translatable("tc.researchtable.noink.0"),
                    Component.translatable("tc.researchtable.noink.1"));
            int sx = Math.max(this.font.width(lines.get(0)), this.font.width(lines.get(1))) / 2;
            graphics.setComponentTooltipForNextFrame(this.font, lines, gx + 157 - sx, gy + 84);
        }
    }

    private void drawAspectText(GuiGraphicsExtractor graphics, int x, int y, int mx, int my) {
        int count = 0, drawn = 0;
        for (Aspect aspect : this.discovered()) {
            if (++count - 1 >= this.page * 5 && drawn < 25) {
                int dx = mx - (x + drawn / 5 * 16), dy = my - (y + drawn % 5 * 16);
                if (dx >= 0 && dy >= 0 && dx < 16 && dy < 16) {
                    this.aspectTooltip(graphics, aspect, mx, my);
                    // o pesquisador vê de que o aspecto é feito
                    if (this.knows("RESEARCHER1") && !aspect.isPrimal()) {
                        Matrix3x2fStack pose = graphics.pose();
                        for (int[] at : new int[][]{{mx + 6, my + 6}, {mx + 24, my + 6}}) {
                            pose.pushMatrix();
                            pose.translate(at[0], at[1]);
                            pose.scale(1.25f, 1.25f);
                            graphics.blit(RenderPipelines.GUI_TEXTURED, BACK, 0, 0, 0, 0, 16, 16, 16, 16);
                            pose.popMatrix();
                        }
                        AspectTags.draw(graphics, this.font, mx + 26, my + 8, aspect.components()[1], 0, 0, 1.0f, false, 0);
                        AspectTags.draw(graphics, this.font, mx + 8, my + 8, aspect.components()[0], 0, 0, 1.0f, false, 0);
                    }
                    return;
                }
                drawn++;
            }
        }
        if (this.select1 != null && mx - (x + 3) >= 0 && my - (y + 99) >= 0 && mx - (x + 3) < 16 && my - (y + 99) < 16) {
            this.aspectTooltip(graphics, this.select1, mx, my);
            return;
        }
        if (this.select2 != null && mx - (x + 61) >= 0 && my - (y + 99) >= 0 && mx - (x + 61) < 16 && my - (y + 99) < 16) {
            this.aspectTooltip(graphics, this.select2, mx, my);
        }
    }

    private void aspectTooltip(GuiGraphicsExtractor graphics, Aspect aspect, int mx, int my) {
        graphics.setComponentTooltipForNextFrame(this.font, List.of(aspect.name(),
                Component.translatable("tc.aspect.help." + aspect.tag())), mx, my - 8);
    }

    // ----------------------------------------------------------------- mouse

    private Aspect clickedAspect(double mx, double my, boolean ignoreZero) {
        PlayerKnowledge knowledge = this.knowledge();
        AspectList bonus = this.bonus();
        int count = 0, drawn = 0;
        for (Aspect aspect : this.discovered()) {
            if (++count - 1 >= this.page * 5 && drawn < 25) {
                double dx = mx - (this.leftPos + drawn / 5 * 16 + 10), dy = my - (this.topPos + drawn % 5 * 16 + 40);
                if ((ignoreZero || knowledge.points(aspect) > 0 || bonus.getAmount(aspect) > 0)
                        && dx >= 0 && dy >= 0 && dx < 16 && dy < 16) {
                    return aspect;
                }
                drawn++;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int gx = this.leftPos, gy = this.topPos;
        if (event.button() == 0 && mx >= gx + 10 && mx < gx + 90 && my >= gy + 40 && my < gy + 120) {
            Aspect aspect = this.clickedAspect(mx, my, false);
            if (aspect != null) {
                this.sound(TCSounds.HHOFF, 0.2f, 1.0f + this.minecraft.level.getRandom().nextFloat() * 0.1f);
                this.dragged = aspect;
                return true;
            }
        }
        boolean handled = super.mouseClicked(event, doubleClick);
        long now = System.nanoTime();
        if (this.butcount2 > now) return handled;
        if (mx - (gx + 35) >= 0 && my - (gy + 139) >= 0 && mx - (gx + 35) < 32 && my - (gy + 139) < 16
                && this.select1 != null && this.select2 != null) {
            this.butcount2 = now + 200_000_000L;
            this.sound(TCSounds.CAMERA_CLACK, 0.4f, 1.0f);
            this.sound(TCSounds.HHON, 0.3f, 1.0f);
            this.combine(this.select1, this.select2);
            return true;
        }
        if (this.page > 0 && mx - (gx + 27) >= 0 && my - (gy + 121) >= 0 && mx - (gx + 27) < 24 && my - (gy + 121) < 8) {
            this.page--;
            this.sound(TCSounds.KEY, 0.3f, 1.0f);
            return true;
        }
        if (this.page < this.lastPage && mx - (gx + 51) >= 0 && my - (gy + 121) >= 0 && mx - (gx + 51) < 24
                && my - (gy + 121) < 8) {
            this.page++;
            this.sound(TCSounds.KEY, 0.3f, 1.0f);
            return true;
        }
        if (this.select1 != null && mx - (gx + 11) >= 0 && my - (gy + 137) >= 0 && mx - (gx + 11) < 16
                && my - (gy + 137) < 16) {
            this.select1 = null;
            this.sound(TCSounds.HHOFF, 0.2f, 1.0f);
            return true;
        }
        if (this.select2 != null && mx - (gx + 71) >= 0 && my - (gy + 137) >= 0 && mx - (gx + 71) < 16
                && my - (gy + 137) < 16) {
            this.select2 = null;
            this.sound(TCSounds.HHOFF, 0.2f, 1.0f);
            return true;
        }
        if (this.note != null) {
            // clicando num aspecto escrito, ele se apaga
            Hex hp = Hex.fromPixel(mx - (gx + 169), my - (gy + 83), HEX_SIZE);
            ResearchNote.Cell cell = this.note.byKey().get(hp.key());
            if (cell != null && cell.type() == 2) {
                this.sound(TCSounds.HHON, 0.3f, 1.0f);
                this.sound(TCSounds.ERASE, 0.2f, 1.0f + this.minecraft.level.getRandom().nextFloat() * 0.1f);
                this.place(hp, null);
                return true;
            }
            if (this.knows("RESEARCHDUPE") && this.note.complete() && mx - (gx + 37) >= 0 && my - (gy + 5) >= 0
                    && mx - (gx + 37) < 24 && my - (gy + 5) < 24) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ResearchTableMenu.DUPLICATE);
                this.sound(TCSounds.CAMERA_CLACK, 0.4f, 1.0f);
                return true;
            }
        }
        // o pesquisador mestre junta os dois de que um aspecto é feito com um clique agachado
        if (event.hasShiftDown() && this.knows("RESEARCHER2")) {
            Aspect aspect = this.clickedAspect(mx, my, true);
            if (aspect != null && !aspect.isPrimal()) {
                Aspect a = aspect.components()[0], b = aspect.components()[1];
                PlayerKnowledge knowledge = this.knowledge();
                AspectList bonus = this.bonus();
                if ((knowledge.points(a) > 0 || bonus.getAmount(a) > 0) && (knowledge.points(b) > 0 || bonus.getAmount(b) > 0)) {
                    this.dragged = null;
                    this.sound(TCSounds.HHON, 0.3f, 1.0f);
                    this.combine(a, b);
                    return true;
                }
            }
        }
        return handled;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragged == null) return super.mouseReleased(event);
        double mx = event.x(), my = event.y();
        int gx = this.leftPos, gy = this.topPos;
        Aspect aspect = this.dragged;
        this.dragged = null;
        if (this.note != null) {
            Hex hp = Hex.fromPixel(mx - (gx + 169), my - (gy + 83), HEX_SIZE);
            ResearchNote.Cell cell = this.note.byKey().get(hp.key());
            if (cell != null && cell.type() == 0) {
                this.sound(TCSounds.HHON, 0.3f, 1.0f);
                this.sound(TCSounds.WRITE, 0.2f, 1.0f);
                this.place(hp, aspect);
                return true;
            }
        }
        if (mx - (gx + 20) >= -16 && my - (gy + 146) >= -16 && mx - (gx + 20) < 16 && my - (gy + 146) < 16) {
            this.sound(TCSounds.HHOFF, 0.2f, 1.0f);
            this.select1 = aspect;
            return true;
        }
        if (mx - (gx + 79) >= -16 && my - (gy + 146) >= -16 && mx - (gx + 79) < 16 && my - (gy + 146) < 16) {
            this.sound(TCSounds.HHOFF, 0.2f, 1.0f);
            this.select2 = aspect;
            return true;
        }
        // soltou em cima dele mesmo: é um clique, e ele vai para a primeira casa de combinar livre
        if (this.clickedAspect(mx, my, false) == aspect) {
            if (this.select1 == null) this.select1 = aspect;
            else if (this.select2 == null) this.select2 = aspect;
        }
        return true;
    }

    private void place(Hex hex, Aspect aspect) {
        ResearchTableBlockEntity table = this.menu.entity();
        if (table == null) return;
        ClientPlayNetworking.send(new ResearchTablePayloads.Place(table.getBlockPos(), hex.q(), hex.r(),
                aspect == null ? "" : aspect.tag()));
    }

    private void combine(Aspect first, Aspect second) {
        ResearchTableBlockEntity table = this.menu.entity();
        if (table == null) return;
        ClientPlayNetworking.send(new ResearchTablePayloads.Combine(table.getBlockPos(), first.tag(), second.tag()));
    }

    private void sound(Holder<SoundEvent> sound, float volume, float pitch) {
        var player = this.minecraft.player;
        if (player == null) return;
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), sound.value(), SoundSource.PLAYERS,
                volume, pitch, false);
    }
}
