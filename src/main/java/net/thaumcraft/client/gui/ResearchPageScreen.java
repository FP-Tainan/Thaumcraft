package net.thaumcraft.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.InfusionEnchantmentRecipe;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.BookPages;
import net.thaumcraft.research.BookRecipes;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.Page;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.Researches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pesquisa aberta: o {@code GuiResearchRecipe} do Thaumcraft 4.2.3.5, página por página.
 *
 * <p>A folha dupla é a {@code gui_researchbook} ampliada 1,3 vez; as páginas são desenhadas no tamanho normal por cima,
 * a da direita 152 pontos depois da da esquerda. O texto vai na letra miúda do livro (a fonte unicode de então, hoje a
 * {@code uniform}), com as figuras ({@code <IMG>}) e os filetes ({@code <LINE>}) no meio. As receitas usam a folha
 * {@code gui_researchbook_overlay}: bancada comum e arcana, crisol, infusão, infusão de encantamento, fornalha e as
 * montagens de estrutura em camadas. As listas de receitas trocam uma por segundo; o item de uma receita que outra
 * pesquisa ensina leva até ela com um clique, e o marcador do rodapé volta.
 */
public class ResearchPageScreen extends Screen {
    private static final Identifier TEX1 = Thaumcraft.id("textures/gui/gui_researchbook.png");
    private static final Identifier TEX2 = Thaumcraft.id("textures/gui/gui_researchbook_overlay.png");
    private static final Identifier ASPECT_BACK = Thaumcraft.id("textures/aspects/_back.png");
    /** A letra miúda do {@code TCFontRenderer} em modo unicode. */
    private static final Style SMALL = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("uniform")));
    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 181;
    private static final int FONT_HEIGHT = 9;
    private static final int GREY = 0xFF505050;
    private static final Pattern INSERT = Pattern.compile("@(\\d+)@");

    /** O caminho de volta pelas referências: pesquisa e página. */
    private static final LinkedList<Object[]> HISTORY = new LinkedList<>();

    private final Screen parent;
    private final Research research;
    private final List<Page> pages;
    private final int maxPages;
    private int page;
    private final Map<Aspect, List<ItemStack>> aspectItems = new HashMap<>();
    private final Map<Page, List<Object>> resolved = new IdentityHashMap<>();
    private final Map<String, Text> texts = new HashMap<>();
    private final List<int[]> referenceAt = new ArrayList<>();
    private final List<BookPages.Location> referenceTo = new ArrayList<>();
    private List<Component> tooltip;
    private int tooltipX;
    private int tooltipY;
    private long lastCycle;
    private int cycle = -1;
    private PlayerKnowledge knowledge = new PlayerKnowledge();

    public ResearchPageScreen(Screen parent, Research research) {
        this(parent, research, 0);
    }

    public ResearchPageScreen(Screen parent, Research research, int page) {
        super(research.name());
        this.parent = parent;
        this.research = research;
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) this.knowledge = Knowledges.of(player);
        List<Page> list = new ArrayList<>();
        for (Page each : research.pages()) {
            // o texto escondido só para quem já sabe a outra pesquisa
            if (each instanceof Page.Concealed concealed && !this.knows(concealed.research())) continue;
            list.add(each);
        }
        if (research.key().equals("ASPECTS")) this.addAspectPages(list);
        this.pages = list;
        this.maxPages = list.size();
        if (page % 2 == 1) page--;
        this.page = page;
    }

    private boolean knows(String key) {
        Research other = Researches.get(key);
        return other != null && ResearchManager.isComplete(this.knowledge, other);
    }

    /**
     * A pesquisa "Aspectos" ganha, depois das suas, as páginas dos aspectos que quem lê já descobriu, quatro por página
     * — e cada aspecto lembra as coisas examinadas que o têm.
     */
    private void addAspectPages(List<Page> list) {
        for (String key : this.knowledge.scanned()) {
            ItemStack stack = scannedStack(key);
            if (stack.isEmpty()) continue;
            AspectList tags = ObjectAspects.of(stack);
            for (Aspect aspect : tags.getAspects()) {
                ItemStack shown = stack.copyWithCount(Math.max(1, tags.getAmount(aspect)));
                this.aspectItems.computeIfAbsent(aspect, a -> new ArrayList<>()).add(shown);
            }
        }
        AspectList known = new AspectList();
        for (Aspect aspect : this.knowledge.discovered()) known.add(aspect, this.knowledge.points(aspect));
        AspectList sheet = new AspectList();
        int count = 0;
        for (Aspect aspect : known.getAspectsSorted()) {
            sheet.add(aspect, known.getAmount(aspect));
            if (++count == 4) {
                count = 0;
                list.add(new Page.Aspects(sheet));
                sheet = new AspectList();
            }
        }
        if (count > 0) list.add(new Page.Aspects(sheet));
    }

    private static ItemStack scannedStack(String key) {
        Identifier id;
        if (key.startsWith("item:")) {
            id = Identifier.tryParse(key.substring(5));
            if (id == null) return ItemStack.EMPTY;
            return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
        }
        if (key.startsWith("block:")) {
            id = Identifier.tryParse(key.substring(6));
            if (id == null) return ItemStack.EMPTY;
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(id).map(b -> new ItemStack(b.asItem())).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** O Esc e a tecla do inventário voltam ao mapa e esquecem o caminho das referências. */
    @Override
    public void onClose() {
        HISTORY.clear();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
            return;
        }
        super.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    /** O fundo escurecido de sempre, sem o borrão de hoje. */
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        this.extractTransparentBackground(graphics);
    }

    // ------------------------------------------------------------------------------------------------ desenho

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        if (this.minecraft != null && this.minecraft.player != null) this.knowledge = Knowledges.of(this.minecraft.player);
        int sw = (this.width - PANE_WIDTH) / 2;
        int sh = (this.height - PANE_HEIGHT) / 2;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((this.width - PANE_WIDTH * 1.3f) / 2.0f, (this.height - PANE_HEIGHT * 1.3f) / 2.0f);
        pose.scale(1.3f, 1.3f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX1, 0, 0, 0, 0, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        pose.popMatrix();

        this.referenceAt.clear();
        this.referenceTo.clear();
        this.tooltip = null;
        for (int current = this.page; current <= this.page + 1 && current < this.maxPages; current++) {
            this.drawPage(graphics, this.pages.get(current), current % 2, sw, sh, mouseX, mouseY);
        }

        float ticks = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount : 0;
        float bob = Mth.sin(ticks / 3.0f) * 0.2f + 0.1f;
        if (!HISTORY.isEmpty()) scaledRect(graphics, TEX1, sw + 118, sh + 189, 38, 202, 20, 12, bob);
        if (this.page > 0) scaledRect(graphics, TEX1, sw - 16, sh + 190, 0, 184, 12, 8, bob);
        if (this.page < this.maxPages - 2) scaledRect(graphics, TEX1, sw + 262, sh + 190, 12, 184, 12, 8, bob);

        if (this.tooltip != null) {
            graphics.setComponentTooltipForNextFrame(this.font, this.tooltip, this.tooltipX, this.tooltipY);
        } else if (!HISTORY.isEmpty()) {
            int mx = mouseX - (sw + 118), my = mouseY - (sh + 189);
            if (mx >= 0 && my >= 0 && mx < 20 && my < 12) {
                graphics.text(this.font, Component.translatable("recipe.return"), mouseX, mouseY, 0xFFFFFFFF, true);
            }
        }
    }

    /** O {@code drawTexturedModalRectScaled}: o recorte crescido em volta do próprio centro (as setas que pulsam). */
    private static void scaledRect(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v, int w, int h, float scale) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + w / 2.0f, y + h / 2.0f);
        pose.scale(1.0f + scale, 1.0f + scale);
        pose.translate(-w / 2.0f, -h / 2.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, u, v, w, h, 256, 256);
        pose.popMatrix();
    }

    /** Um recorte da folha de 256, ampliado duas vezes a partir de (x, y), como as receitas desenham os quadros. */
    private static void rect2(GuiGraphicsExtractor graphics, float x, float y, int dx, int dy, int u, int v, int w, int h, int color) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(2.0f, 2.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX2, dx, dy, u, v, w, h, 256, 256, color);
        pose.popMatrix();
    }

    private void setTooltip(List<Component> lines, int x, int y) {
        List<Component> styled = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            // a primeira linha em ciano, as outras em cinza (as cores próprias do item ainda mandam)
            styled.add(Component.empty().withStyle(i == 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY).append(lines.get(i)));
        }
        this.tooltip = styled;
        this.tooltipX = x;
        this.tooltipY = y;
    }

    private void aspectTooltip(Aspect aspect, int x, int y) {
        this.setTooltip(List.of(Component.literal(latin(aspect)), aspect.name()), x, y);
    }

    /** O {@code getName} do aspecto: o nome latino, com maiúscula. */
    private static String latin(Aspect aspect) {
        String tag = aspect.tag();
        return tag.isEmpty() ? tag : Character.toUpperCase(tag.charAt(0)) + tag.substring(1);
    }

    /** O tooltip de um ingrediente, com o "clique para pesquisar" quando outra pesquisa ensina a fazê-lo. */
    private void itemTooltip(ItemStack stack, int mouseX, int mouseY, boolean clickThrough) {
        if (stack.isEmpty() || this.minecraft == null) return;
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(this.minecraft, stack));
        // o nome sem a cor da raridade: a lista crua do getTooltip de então, que a caixa pinta de ciano
        if (!lines.isEmpty()) lines.set(0, Component.literal(lines.getFirst().getString()));
        if (clickThrough) {
            BookPages.Location where = BookPages.whereMade(this.knowledge, stack);
            if (where != null && !where.research().equals(this.research.key())) {
                lines.add(Component.translatable("recipe.clickthrough").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                this.referenceAt.add(new int[]{mouseX, mouseY});
                this.referenceTo.add(where);
            }
        }
        this.setTooltip(lines, mouseX, mouseY);
    }

    private static boolean over(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16;
    }

    /** O {@code InventoryUtils.cycleItemStack}: das várias que servem, uma por segundo. */
    private static ItemStack cycle(List<ItemStack> options) {
        if (options.isEmpty()) return ItemStack.EMPTY;
        return options.get((int) (System.currentTimeMillis() / 1000L % options.size()));
    }

    private static ItemStack cycle(Ingredient ingredient) {
        if (ingredient == null) return ItemStack.EMPTY;
        List<ItemStack> options = ingredient.items().map(ItemStack::new).toList();
        return cycle(options);
    }

    private void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, boolean decorations) {
        if (stack.isEmpty()) return;
        graphics.item(stack, x, y);
        if (decorations) graphics.itemDecorations(this.font, stack, x, y);
    }

    private void drawPage(GuiGraphicsExtractor graphics, Page page, int side, int x, int y, int mx, int my) {
        if (this.lastCycle < System.currentTimeMillis()) {
            this.cycle++;
            this.lastCycle = System.currentTimeMillis() + 1000L;
        }
        if (this.page == 0 && side == 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX1, x + 4, y - 13, 24, 184, 96, 4, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX1, x + 4, y + 4, 24, 184, 96, 4, 256, 256);
            Component name = this.research.name();
            int offset = this.font.width(name);
            if (offset <= 130) {
                graphics.text(this.font, name, x + 52 - offset / 2, y - 6, 0xFF303030, false);
            } else {
                float vv = 130.0f / offset;
                var pose = graphics.pose();
                pose.pushMatrix();
                pose.translate(x + 52 - offset / 2 * vv, y - 6.0f * vv);
                pose.scale(vv, vv);
                graphics.text(this.font, name, 0, 0, 0xFF303030, false);
                pose.popMatrix();
            }
            y += 25;
        }
        switch (page) {
            case Page.Text text -> this.drawText(graphics, side, x, y - 10, text.key());
            case Page.Concealed text -> this.drawText(graphics, side, x, y - 10, text.key());
            case Page.Aspects aspects -> this.drawAspectPage(graphics, side, x - 8, y - 8, mx, my, aspects.aspects());
            case Page.Smelting smelting -> this.drawSmeltingPage(graphics, side, x - 4, y - 8, mx, my, smelting);
            case Page.Recipe recipe -> {
                List<Object> list = this.resolved.computeIfAbsent(recipe, p -> BookPages.resolve((Page.Recipe) p));
                if (list.isEmpty()) return;
                if (this.cycle < 0 || this.cycle >= list.size()) this.cycle = 0;
                Object shown = recipe.kind() == Page.Kind.COMPOUND || recipe.kind() == Page.Kind.ENCHANTMENT ? list.getFirst() : list.get(this.cycle);
                switch (shown) {
                    case BookRecipes.Crafting crafting -> this.drawCraftingPage(graphics, side, x - 4, y - 8, mx, my, crafting);
                    case ArcaneRecipe arcane -> this.drawArcanePage(graphics, side, x - 4, y - 8, mx, my, arcane);
                    case CrucibleRecipe crucible -> this.drawCruciblePage(graphics, side, x - 4, y - 8, mx, my, crucible);
                    case InfusionRecipe infusion -> this.drawInfusionPage(graphics, side, x - 4, y - 8, mx, my, infusion);
                    case InfusionEnchantmentRecipe enchant -> this.drawEnchantPage(graphics, side, x - 4, y - 8, mx, my, enchant);
                    case BookRecipes.Compound compound -> this.drawCompoundPage(graphics, side, x - 4, y - 8, mx, my, compound);
                    default -> {
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ texto

    /** Uma página de texto já quebrada: as linhas e, nas marcadas, o que vai no lugar delas. */
    private record Text(List<FormattedText> lines, List<String> inserts) {
    }

    /** O {@code listFormattedStringToWidth} do {@code TCFontRenderer}: quebra de linha, filetes e figuras. */
    private Text split(String key) {
        String raw = Component.translatable(key).getString();
        List<String> inserts = new ArrayList<>();
        boolean found = true;
        while (found) {
            found = false;
            raw = raw.replace("<BR>", "\n").replace("<BR/>", "\n");
            int line = raw.indexOf("<LINE>");
            int lineAlt = raw.indexOf("<LINE/>");
            if (line >= 0 || lineAlt >= 0) {
                inserts.add("<LINE>");
                String tag = line >= 0 ? "<LINE>" : "<LINE/>";
                int at = raw.indexOf(tag);
                raw = raw.substring(0, at) + "\n@" + (inserts.size() - 1) + "@\n" + raw.substring(at + tag.length());
                found = true;
            }
            int i1 = raw.indexOf("<IMG>");
            if (i1 >= 0) {
                int i2 = raw.indexOf("</IMG>", i1);
                if (i2 < 0) break;
                inserts.add(raw.substring(i1 + 5, i2));
                raw = raw.substring(0, i1) + "\n@" + (inserts.size() - 1) + "@\n" + raw.substring(i2 + 6);
                found = true;
            }
        }
        // o trimStringNewline: as quebras do fim não contam
        while (raw.endsWith("\n")) raw = raw.substring(0, raw.length() - 1);
        List<FormattedText> lines = new ArrayList<>();
        for (String paragraph : raw.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add(FormattedText.EMPTY);
                continue;
            }
            lines.addAll(this.font.getSplitter().splitLines(FormattedText.of(paragraph, SMALL), 139, SMALL));
        }
        return new Text(lines, inserts);
    }

    private void drawText(GuiGraphicsExtractor graphics, int side, int x, int y, String key) {
        Text text = this.texts.computeIfAbsent(key, this::split);
        int px = x - 15 + side * 152;
        int py = y;
        for (FormattedText line : text.lines()) {
            String plain = line.getString();
            Matcher insert = INSERT.matcher(plain);
            if (insert.find()) {
                int index = Integer.parseInt(insert.group(1));
                String what = index < text.inserts().size() ? text.inserts().get(index) : null;
                if ("<LINE>".equals(what)) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEX1, px + 139 / 2 - 48, py + 2, 24, 184, 96, 4, 256, 256);
                } else if (what != null) {
                    py = this.drawImage(graphics, what, px, py);
                }
            } else {
                graphics.text(this.font, Language.getInstance().getVisualOrder(line), px, py, 0xFF000000, false);
            }
            py += FONT_HEIGHT;
        }
    }

    /** Uma figura no meio do texto: {@code domínio:caminho:u:v:largura:altura:escala}, numa folha de 256. */
    private int drawImage(GuiGraphicsExtractor graphics, String spec, int px, int py) {
        String[] part = spec.split(":");
        if (part.length < 7) return py;
        // as figuras dos itens moravam em textures/items; hoje é textures/item
        String path = part[1].replace("textures/items/", "textures/item/");
        Identifier texture = Identifier.fromNamespaceAndPath(part[0], path);
        int u = Integer.parseInt(part[2]), v = Integer.parseInt(part[3]);
        int w = Integer.parseInt(part[4]), h = Integer.parseInt(part[5]);
        float scale = Float.parseFloat(part[6]);
        // o GL de então repetia a folha além da borda; a de hoje prende
        if (u + w > 256) u -= 256;
        if (v + h > 256) v -= 256;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(px - 3 + 139 / 2 - w / 2 * scale, py);
        pose.scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, u, v, w, h, 256, 256);
        pose.popMatrix();
        return (int) (py + (h * scale - FONT_HEIGHT));
    }

    /** A letra miúda sozinha, para as legendas da página dos aspectos. */
    private void small(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.text(this.font, Component.literal(text).withStyle(SMALL), x, y, color, false);
    }

    private int smallWidth(String text) {
        return this.font.width(Component.literal(text).withStyle(SMALL));
    }

    // ------------------------------------------------------------------------------------------------ aspectos

    private void drawAspectPage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, AspectList aspects) {
        if (aspects.isEmpty()) return;
        int start = side * 152;
        int ticks = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.tickCount : 0;
        var pose = graphics.pose();
        int count = 0;
        for (Aspect aspect : aspects.getAspectsSorted()) {
            int tx = x + start;
            int ty = y + count * 50;
            if (mx >= tx && my >= ty && mx < tx + 40 && my < ty + 40) {
                pose.pushMatrix();
                pose.translate(x + start - 5, y + count * 50 - 5);
                pose.scale(2.5f, 2.5f);
                graphics.blit(RenderPipelines.GUI_TEXTURED, ASPECT_BACK, 0, 0, 0, 0, 16, 16, 16, 16);
                pose.popMatrix();
            }
            pose.pushMatrix();
            pose.scale(2.0f, 2.0f);
            AspectTags.draw(graphics, this.font, (x + start) / 2, (y + count * 50) / 2, aspect, aspects.getAmount(aspect), 0, 1.0f, false, ticks);
            pose.popMatrix();
            String name = latin(aspect);
            this.small(graphics, name, x + start + 16 - this.smallWidth(name) / 2, y + 33 + count * 50, GREY);
            Aspect[] parts = aspect.components();
            if (parts != null) {
                pose.pushMatrix();
                pose.scale(1.5f, 1.5f);
                AspectTags.draw(graphics, this.font, (int) ((x + start + 54) / 1.5f), (int) ((y + 4 + count * 50) / 1.5f), parts[0], 0, 0, 1.0f, false, ticks);
                AspectTags.draw(graphics, this.font, (int) ((x + start + 96) / 1.5f), (int) ((y + 4 + count * 50) / 1.5f), parts[1], 0, 0, 1.0f, false, ticks);
                pose.popMatrix();
                String first = latin(parts[0]);
                graphics.text(this.font, Component.literal(first).withStyle(SMALL.withItalic(true)),
                        x + start + 16 - this.smallWidth(first) / 2 + 50, y + 30 + count * 50, GREY, false);
                String second = latin(parts[1]);
                graphics.text(this.font, Component.literal(second).withStyle(SMALL.withItalic(true)),
                        x + start + 16 - this.smallWidth(second) / 2 + 92, y + 30 + count * 50, GREY, false);
                graphics.text(this.font, "=", x + start + 7 + 32, y + 12 + count * 50, 0xFF999999, false);
                graphics.text(this.font, "+", x + start + 4 + 79, y + 12 + count * 50, 0xFF999999, false);
            } else {
                this.small(graphics, Component.translatable("tc.aspect.primal").getString(), x + start + 48, y + 12 + count * 50, 0xFF444444);
            }
            count++;
        }
        count = 0;
        for (Aspect aspect : aspects.getAspectsSorted()) {
            int tx = x + start;
            int ty = y + count * 50;
            if (mx >= tx && my >= ty && mx < tx + 40 && my < ty + 40) {
                List<ItemStack> items = this.aspectItems.get(aspect);
                if (items != null && !items.isEmpty()) {
                    int xcount = 0, ycount = 0;
                    for (ItemStack stack : items) {
                        this.item(graphics, stack, mx + 8 + xcount * 17, 17 * ycount + (my - (4 + items.size() / 8 * 8)), true);
                        if (++xcount >= 8) {
                            xcount = 0;
                            ycount++;
                        }
                    }
                }
            }
            count++;
        }
    }

    /** Os aspectos de custo em fileira embaixo, sobre o círculo apagado (bancada arcana e montagens). */
    private void costRow(GuiGraphicsExtractor graphics, AspectList tags, int x, int y, int mx, int my) {
        int count = 0;
        List<Aspect> sorted = tags.getAspectsSortedAmount();
        for (Aspect tag : sorted) {
            AspectTags.draw(graphics, this.font, x + 14 + 18 * count + (5 - tags.size()) * 8, y, tag, tags.getAmount(tag), 0, 1.0f, false, 0);
            count++;
        }
        count = 0;
        for (Aspect tag : sorted) {
            int tx = x + 14 + 18 * count + (5 - tags.size()) * 8;
            if (over(mx, my, tx, y)) this.aspectTooltip(tag, mx, my - 8);
            count++;
        }
    }

    /** Os aspectos em grade de {@code perRow}, a última fileira centrada (crisol e infusão). */
    private void aspectGrid(GuiGraphicsExtractor graphics, AspectList tags, int perRow, int sx, int sy, int mx, int my, int multiplier) {
        int rows = (tags.size() - 1) / perRow;
        int shift = (perRow - tags.size() % perRow) * 10;
        int total = 0;
        for (Aspect tag : tags.getAspectsSorted()) {
            int m = total / perRow >= rows && (rows > 1 || tags.size() < perRow) ? 1 : 0;
            int vx = sx + total % perRow * 20 + shift * m;
            int vy = sy + total / perRow * 20;
            AspectTags.draw(graphics, this.font, vx, vy, tag, tags.getAmount(tag) * multiplier, 0, 1.0f, false, 0);
            if (over(mx, my, vx, vy)) this.aspectTooltip(tag, mx, my);
            total++;
        }
    }

    // ------------------------------------------------------------------------------------------------ receitas

    private void title(GuiGraphicsExtractor graphics, String key, int x, int y) {
        Component text = Component.translatable(key);
        graphics.text(this.font, text, x + 56 - this.font.width(text) / 2, y, GREY, false);
    }

    private void drawCraftingPage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, BookRecipes.Crafting recipe) {
        int start = side * 152;
        rect2(graphics, x + start, y, 2, 32, 60, 15, 52, 52, -1);
        rect2(graphics, x + start, y, 20, 12, 20, 3, 16, 16, -1);
        ItemStack out = recipe.result().get();
        this.item(graphics, out, x + 48 + start, y + 32, true);
        if (over(mx, my, x + 48 + start, y + 32)) this.itemTooltip(out, mx, my, false);
        boolean shaped = recipe.height() > 0;
        this.title(graphics, shaped ? "recipe.type.workbench" : "recipe.type.workbenchshapeless", x + start, y);
        List<List<ItemStack>> grid = recipe.grid();
        for (int n = 0; n < grid.size(); n++) {
            int i, j;
            if (shaped) {
                i = n % recipe.width();
                j = n / recipe.width();
                if (i >= 3 || j >= 3) continue;
            } else {
                if (n >= 9) break;
                i = n % 3;
                j = n / 3;
            }
            ItemStack stack = cycle(grid.get(n));
            if (stack.isEmpty()) continue;
            int ix = x + start + 16 + i * 32, iy = y + 76 + j * 32;
            this.item(graphics, stack.copyWithCount(1), ix, iy, false);
            if (over(mx, my, ix, iy)) this.itemTooltip(stack, mx, my, true);
        }
    }

    private void drawArcanePage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, ArcaneRecipe recipe) {
        int start = side * 152;
        rect2(graphics, x + start, y, 2, 27, 112, 15, 52, 52, -1);
        rect2(graphics, x + start, y, 20, 7, 20, 3, 16, 16, -1);
        rect2(graphics, x + start, y + 164, 0, 0, 68, 76, 12, 12, 0x66FFFFFF);
        if (!recipe.cost().isEmpty()) this.costRow(graphics, recipe.cost(), x + start, y + 172, mx, my);
        ItemStack out = recipe.result();
        this.item(graphics, out, x + 48 + start, y + 22, true);
        // a caixa do tooltip do original fica cinco pontos abaixo do desenho
        if (mx >= x + 48 + start && my >= y + 27 && mx < x + 48 + start + 16 && my < y + 27 + 16) this.itemTooltip(out, mx, my, false);
        this.title(graphics, "recipe.type.arcane", x + start, y);
        List<Ingredient> pattern = recipe.pattern();
        for (int n = 0; n < pattern.size() && n < 9; n++) {
            Ingredient ingredient = pattern.get(n);
            if (ingredient == null) continue;
            ItemStack stack = cycle(ingredient);
            if (stack.isEmpty()) continue;
            int ix = x + start + 16 + n % 3 * 32, iy = y + 66 + n / 3 * 32;
            this.item(graphics, stack, ix, iy, false);
            if (over(mx, my, ix, iy)) this.itemTooltip(stack, mx, my, true);
        }
    }

    private void drawCruciblePage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, CrucibleRecipe recipe) {
        int start = side * 152;
        this.title(graphics, "recipe.type.crucible", x + start, y);
        rect2(graphics, x + start, y + 28, 0, 0, 0, 3, 56, 17, -1);
        rect2(graphics, x + start, y + 28 + 64, 0, 0, 0, 20, 56, 48, -1);
        rect2(graphics, x + start + 42, y + 28 + 48, 0, 0, 100, 84, 11, 13, -1);
        AspectList cost = recipe.cost();
        int rows = (cost.size() - 1) / 3;
        this.aspectGrid(graphics, cost, 3, x + start + 28, y + 96 + 32 - 10 * rows, mx, my, 1);
        ItemStack out = recipe.result();
        this.item(graphics, out, x + 48 + start, y + 36, true);
        ItemStack catalyst = cycle(recipe.catalystStacks());
        this.item(graphics, catalyst, x + 26 + start, y + 72, false);
        if (over(mx, my, x + 48 + start, y + 36)) this.itemTooltip(out, mx, my, false);
        if (over(mx, my, x + 26 + start, y + 72)) this.itemTooltip(catalyst, mx, my, true);
    }

    private void drawSmeltingPage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, Page.Smelting page) {
        ItemStack in = page.input().get();
        ItemStack out = page.output().get();
        if (in.isEmpty() || out.isEmpty()) return;
        int start = side * 152;
        this.title(graphics, "recipe.type.smelting", x + start, y);
        rect2(graphics, x + start, y + 28, 0, 0, 0, 192, 56, 64, -1);
        this.item(graphics, in, x + 48 + start, y + 64, true);
        this.item(graphics, out, x + 48 + start, y + 144, true);
        if (over(mx, my, x + 48 + start, y + 64)) this.itemTooltip(in, mx, my, true);
        if (over(mx, my, x + 48 + start, y + 144)) this.itemTooltip(out, mx, my, false);
    }

    /** O instável da infusão, embaixo da página. */
    private void instability(GuiGraphicsExtractor graphics, int value, int x, int y) {
        int inst = Math.min(5, value / 2);
        Component text = Component.translatable("tc.inst").append(" ").append(Component.translatable("tc.inst." + inst));
        graphics.text(this.font, text, x + 56 - this.font.width(text) / 2, y, GREY, false);
    }

    /** Os pedestais em volta: o {@code pieSlice} do original, de cima em sentido horário, a quarenta pontos do meio. */
    private void ring(GuiGraphicsExtractor graphics, List<Ingredient> components, int sx, int sy, int mx, int my) {
        int le = components.size();
        if (le == 0) return;
        float pieSlice = 360 / le;
        float currentRot = -90.0f;
        for (Ingredient ingredient : components) {
            int xx = (int) (Mth.cos(currentRot / 180.0f * (float) Math.PI) * 40.0f) - 8;
            int yy = (int) (Mth.sin(currentRot / 180.0f * (float) Math.PI) * 40.0f) - 8;
            currentRot += pieSlice;
            ItemStack stack = cycle(ingredient);
            this.item(graphics, stack.copyWithCount(1), sx + xx, sy + yy, false);
            if (over(mx, my, sx + xx, sy + yy)) this.itemTooltip(stack, mx, my, true);
        }
    }

    private void drawInfusionPage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, InfusionRecipe recipe) {
        int start = side * 152;
        this.title(graphics, "recipe.type.infusion", x + start, y);
        this.instability(graphics, recipe.instability(), x + start, y + 194);
        rect2(graphics, x + start, y + 20, 0, 0, 0, 3, 56, 17, -1);
        rect2(graphics, x + start, y + 20 + 38, 0, 0, 200, 77, 60, 44, -1);
        AspectList essentia = recipe.essentia();
        int rows = (essentia.size() - 1) / 5;
        this.aspectGrid(graphics, essentia, 5, x + start + 8, y + 164 - 10 * rows, mx, my, 1);
        ItemStack central = cycle(recipe.central());
        ItemStack out = recipe.resultFor(central);
        this.item(graphics, out, x + 48 + start, y + 28, true);
        this.item(graphics, central.copyWithCount(1), x + 48 + start, y + 94, false);
        this.ring(graphics, recipe.components(), x + 56 + start, y + 102, mx, my);
        if (over(mx, my, x + 48 + start, y + 28)) this.itemTooltip(out, mx, my, false);
        if (over(mx, my, x + 48 + start, y + 94)) this.itemTooltip(central, mx, my, true);
    }

    private void drawEnchantPage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, InfusionEnchantmentRecipe recipe) {
        if (this.minecraft == null || this.minecraft.level == null) return;
        var holder = this.minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(recipe.enchantment()).orElse(null);
        if (holder == null) return;
        Enchantment enchantment = holder.value();
        int start = side * 152;
        int level = (int) (1L + System.currentTimeMillis() / 1000L % Math.max(1, enchantment.getMaxLevel()));
        this.title(graphics, "recipe.type.infusionenchantment", x + start, y);
        this.instability(graphics, recipe.instability(), x + start, y + 194);
        Component name = enchantment.description().copy().append(" ").append(Component.translatable("enchantment.level." + level));
        graphics.text(this.font, name, x + start + 56 - this.font.width(name) / 2, y + 24, 0xFF705090, false);
        int xp = Math.max(1, enchantment.getMinCost(1) / 3) * level;
        String levels = xp + " levels";
        graphics.text(this.font, levels, x + start + 56 - this.font.width(levels) / 2, y + 40, 0xFF508850, false);
        rect2(graphics, x + start, y + 20 + 38, 0, 0, 200, 77, 60, 44, -1);
        AspectList aspects = recipe.aspects();
        int rows = (aspects.size() - 1) / 5;
        this.aspectGrid(graphics, aspects, 5, x + start + 8, y + 164 - 10 * rows, mx, my, level);
        this.ring(graphics, recipe.components(), x + 56 + start, y + 102, mx, my);
    }

    /** A montagem: o bloco a bloco em perspectiva, camada sobre camada, sobre o quadro apagado do chão. */
    private void drawCompoundPage(GuiGraphicsExtractor graphics, int side, int x, int y, int mx, int my, BookRecipes.Compound recipe) {
        int dx = recipe.sizeX(), dy = recipe.sizeY(), dz = recipe.sizeZ();
        int xoff = 64 - (dx * 16 + dz * 16) / 2;
        int yoff = -dy * 25;
        int start = side * 152;
        this.title(graphics, "recipe.type.construct", x + start, y);
        AspectList vis = recipe.vis();
        if (!vis.isEmpty()) this.costRow(graphics, vis, x + start, y + 182, mx, my);
        if (!vis.isEmpty()) rect2(graphics, x + start, y + 174, 0, 0, 68, 76, 12, 12, 0x66FFFFFF);
        float sz = dy > 3 ? (dy - 3) * 0.2f : 0.0f;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + start + xoff * (1.0f + sz), y + 108 + yoff * (1.0f - sz));
        pose.scale(1.0f - sz, 1.0f - sz);
        pose.pushMatrix();
        pose.translate(-8 - xoff, -119 + Math.max(3 - dx, 3 - dz) * 8 + dx * 4 + dz * 4 + dy * 50);
        pose.scale(2.0f, 2.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 0, 72, 64, 44, 256, 256, 0x80FFFFFF);
        pose.popMatrix();
        List<ItemStack> stacks = new ArrayList<>();
        for (var supplier : recipe.blocks()) stacks.add(supplier.get());
        // a de cima na frente: desenha-se de baixo para cima, e dentro da camada na ordem do original
        for (int j = dy - 1; j >= 0; j--) {
            int count = j * dx * dz;
            for (int k = dz - 1; k >= 0; k--) {
                for (int i = dx - 1; i >= 0; i--) {
                    ItemStack stack = count < stacks.size() ? stacks.get(count) : ItemStack.EMPTY;
                    this.item(graphics, stack.copyWithCount(1), i * 16 + k * 16, -i * 8 + k * 8 + j * 50, false);
                    count++;
                }
            }
        }
        pose.popMatrix();
        int count = 0;
        for (int j = 0; j < dy; j++) {
            for (int k = dz - 1; k >= 0; k--) {
                for (int i = dx - 1; i >= 0; i--) {
                    int px = (int) (x + start + xoff * (1.0f + sz) + i * 16 * (1.0f - sz) + k * 16 * (1.0f - sz));
                    int py = (int) (y + 108 + yoff * (1.0f - sz) - i * 8 * (1.0f - sz) + k * 8 * (1.0f - sz) + j * 50 * (1.0f - sz));
                    ItemStack stack = count < stacks.size() ? stacks.get(count) : ItemStack.EMPTY;
                    if (!stack.isEmpty() && mx >= px && my >= py && mx < px + 16.0f * (1.0f - sz) && my < py + 16.0f * (1.0f - sz)) {
                        this.itemTooltip(stack, mx, my, true);
                    }
                    count++;
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ cliques

    private void playPage() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;
        var player = this.minecraft.player;
        this.minecraft.level.playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.PAGE.value(), SoundSource.PLAYERS, 0.66f, 1.0f, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int sw = (this.width - PANE_WIDTH) / 2;
        int sh = (this.height - PANE_HEIGHT) / 2;
        int x = (int) event.x(), y = (int) event.y();
        int mx = x - (sw + 261), my = y - (sh + 189);
        if (this.page < this.maxPages - 2 && mx >= 0 && my >= 0 && mx < 14 && my < 10) {
            this.page += 2;
            this.lastCycle = 0L;
            this.cycle = -1;
            this.playPage();
        }
        mx = x - (sw - 17);
        if (this.page >= 2 && mx >= 0 && my >= 0 && mx < 14 && my < 10) {
            this.page -= 2;
            this.lastCycle = 0L;
            this.cycle = -1;
            this.playPage();
        }
        if (!HISTORY.isEmpty()) {
            mx = x - (sw + 118);
            if (mx >= 0 && my >= 0 && mx < 20 && my < 12) {
                this.playPage();
                Object[] back = HISTORY.pop();
                Research previous = Researches.get((String) back[0]);
                if (previous != null && this.minecraft != null) {
                    this.minecraft.setScreenAndShow(new ResearchPageScreen(this.parent, previous, (Integer) back[1]));
                    return true;
                }
            }
        }
        for (int r = 0; r < this.referenceAt.size(); r++) {
            int[] at = this.referenceAt.get(r);
            if (x >= at[0] && y >= at[1] && x < at[0] + 16 && y < at[1] + 16) {
                BookPages.Location where = this.referenceTo.get(r);
                Research target = Researches.get(where.research());
                if (target == null || this.minecraft == null) continue;
                this.playPage();
                HISTORY.push(new Object[]{this.research.key(), this.page});
                this.minecraft.setScreenAndShow(new ResearchPageScreen(this.parent, target, where.page()));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    /** Para os testes: as páginas que a pesquisa mostra, já com as escondidas tiradas e as dos aspectos postas. */
    public List<Page> pages() {
        return this.pages;
    }

    /** Para os testes: a página da esquerda aberta agora. */
    public int page() {
        return this.page;
    }

}
