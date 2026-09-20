package net.thaumcraft.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.entity.FocalManipulatorBlockEntity;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.inventory.FocalManipulatorMenu;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.registry.TCSounds;
import org.joml.Matrix3x2fStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tela do manipulador focal: o {@code GuiFocalManipulator} da 4.2.3.5. Em cima, as melhorias já postas no foco; no
 * meio, a casa do foco, o vis que a escolhida pede (e a barra do quanto falta puxar) e a experiência; embaixo, as que
 * cabem no próximo posto. Clicar numa escolhe (de novo, desescolhe); clicar na barra começa. As estrelinhas correm da
 * barra até o posto que está sendo preenchido.
 */
public class FocalManipulatorScreen extends AbstractContainerScreen<FocalManipulatorMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_wandtable.png");
    private static final DecimalFormat FORMAT = new DecimalFormat("#######.#");

    private int selected = -1;
    private int rank;
    private long time;
    private long nextSparkle;
    private final List<FocusUpgradeTable.Type> possibleUpgrades = new ArrayList<>();
    private final List<FocusUpgradeTable.Type> upgrades = new ArrayList<>();
    private AspectList aspects = new AspectList();
    private final Map<Long, Spark> sparkles = new HashMap<>();

    private static final class Spark {
        float x, y;
        final float mx, my;
        final int colour;
        long nextframe;
        int frame;

        Spark(float x, float y, float mx, float my, float r, float g, float b) {
            this.x = x;
            this.y = y;
            this.mx = mx;
            this.my = my;
            this.colour = 0xE6000000 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
            this.nextframe = System.currentTimeMillis() + 50L;
        }
    }

    public FocalManipulatorScreen(FocalManipulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 192, 233);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
        FocalManipulatorBlockEntity table = menu.table;
        if (table != null && table.size > 0) {
            this.gatherInfo();
            this.selected = table.upgrade;
        }
    }

    private static Identifier icon(FocusUpgradeTable.Type type) {
        return Thaumcraft.id("textures/foci/" + type.icon() + ".png");
    }

    private void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h, int colour) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, u, v, w, h, 256, 256, colour);
    }

    private int playerLevel() {
        return this.minecraft.player.experienceLevel;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        super.extractBackground(graphics, mx, my, partial);
        this.time = System.currentTimeMillis();
        int k = this.leftPos, l = this.topPos;
        this.blit(graphics, k, l, 0, 0, this.imageWidth, this.imageHeight, -1);
        FocalManipulatorBlockEntity table = this.menu.table;
        if (table == null) return;
        var random = table.getLevel().getRandom();
        ItemStack focus = table.getItem(0);
        if (focus.isEmpty() || table.rank < 0 || table.reset) {
            this.rank = 0;
            this.selected = -1;
            this.possibleUpgrades.clear();
            this.upgrades.clear();
            this.aspects = new AspectList();
            table.reset = false;
            table.rank = 0;
        }
        if (this.rank > 0) {
            for (int a = 0; a < this.possibleUpgrades.size(); a++) {
                if (this.selected == this.possibleUpgrades.get(a).id()) this.blit(graphics, k + 48 + a * 16, l + 104, 200, 0, 16, 16, -1);
            }
        }
        if (this.rank > 0 && this.selected >= 0 && !focus.isEmpty()) {
            int xp = this.rank * FocalManipulatorBlockEntity.XP_MULT;
            if (table.size == 0 && xp <= this.playerLevel()) this.blit(graphics, k + 48, l + 88, 8, 240, 96, 8, -1);
            this.blit(graphics, k + 108, l + 59, 200, 16, 16, 16, -1);
            int start = 0;
            if (table.aspects.size() > 0) {
                for (Aspect aspect : table.aspects.getAspectsSorted()) {
                    if (table.aspects.getAmount(aspect) == 0) continue;
                    int size = (int) ((float) table.aspects.getAmount(aspect) / table.size * 96.0f);
                    this.blit(graphics, k + 48 + start, l + 88, 112 + start, 240, size, 8, 0xE6000000 | aspect.color());
                    start += size;
                    if (random.nextInt(66) == 0) {
                        float x = 48 + start, y = 92.0f;
                        int c = aspect.color();
                        this.sparkles.put(this.time, new Spark(x, y, (46 + this.rank * 16 - x) / 9.0f, (38.0f - y) / 9.0f,
                                (c >> 16 & 255) / 255.0f, (c >> 8 & 255) / 255.0f, (c & 255) / 255.0f));
                    }
                }
            }
            graphics.text(this.font, "" + xp, k + 125, l + 64, xp > this.playerLevel() ? 0xFFF67278 : 0xFF99FF8D, true);
            AspectList al = table.size > 0 ? table.aspects : this.aspects;
            Matrix3x2fStack pose = graphics.pose();
            int q = 0;
            for (Aspect a : al.getAspectsSorted()) {
                pose.pushMatrix();
                pose.translate(k + 49, (float) (l + 68 - al.size() * 2.5));
                pose.scale(0.5f, 0.5f);
                String name = a.tag().substring(0, 1).toUpperCase() + a.tag().substring(1);
                graphics.text(this.font, name, 0, q * 10, 0xFF000000 | a.color(), true);
                graphics.text(this.font, FORMAT.format(al.getAmount(a) / 100.0f), 48, q * 10, 0xFF000000 | a.color(), true);
                pose.popMatrix();
                q++;
            }
        }
        if (this.rank > 0) {
            if (this.nextSparkle < this.time) {
                this.nextSparkle = this.time + (table.size > 0 ? 10 : 500) + random.nextInt(200);
                this.sparkles.put(this.time, new Spark(42 + this.rank * 16 + random.nextInt(12), 34 + random.nextInt(12), 0.0f, 0.0f,
                        0.5f + random.nextFloat() * 0.4f, 1.0f - random.nextFloat() * 0.4f, 1.0f - random.nextFloat() * 0.4f));
            }
            for (int a = 0; a < this.possibleUpgrades.size(); a++) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, icon(this.possibleUpgrades.get(a)), k + 48 + a * 16, l + 104, 0, 0, 16, 16, 16, 16);
            }
        } else if (this.rank == 0 && !focus.isEmpty()) {
            this.gatherInfo();
        }
        for (int a = 0; a < this.upgrades.size(); a++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon(this.upgrades.get(a)), k + 56 + a * 16, l + 32, 0, 0, 16, 16, 16, 16);
        }
    }

    private void gatherInfo() {
        this.possibleUpgrades.clear();
        this.upgrades.clear();
        this.aspects = new AspectList();
        FocalManipulatorBlockEntity table = this.menu.table;
        ItemStack stack = table.getItem(0);
        if (!(stack.getItem() instanceof FocusItem focus)) return;
        short[] s = FocusItem.upgrades(stack);
        this.rank = 1;
        int fu = 0;
        while (this.rank <= 5 && s[this.rank - 1] != -1) {
            FocusUpgradeTable.Type type = net.thaumcraft.api.FocusUpgrades.byId(s[this.rank - 1]);
            if (type != null) this.upgrades.add(type);
            fu++;
            this.rank++;
        }
        if (fu == 5) {
            this.rank = -1;
        } else {
            for (FocusUpgradeTable.Type type : focus.possibleByRank(stack, this.rank)) {
                if (focus.canApply(stack, this.minecraft.player, type, this.rank)) this.possibleUpgrades.add(type);
            }
        }
        if (table.size > 0) this.selected = table.upgrade;
    }

    /** As estrelinhas, por cima de tudo, somando luz: nove quadros da linha sete da {@code particles.png}. */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (Long key : this.sparkles.keySet().toArray(new Long[0])) {
            Spark s = this.sparkles.get(key);
            graphics.blit(ResearchTableScreen.GUI_ADDITIVE, Sparkle.PARTICLES, (int) (s.x - 4), (int) (s.y - 4), s.frame * 16.0f, 112.0f,
                    8, 8, 16, 16, 256, 256, s.colour);
            if (s.nextframe < this.time) {
                s.frame++;
                s.nextframe = this.time + 50L;
                s.x += s.mx;
                s.y += s.my;
            }
            if (s.frame == 9) this.sparkles.remove(key);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);
        FocalManipulatorBlockEntity table = this.menu.table;
        if (table == null) return;
        int baseX = this.leftPos, baseY = this.topPos;
        if (this.rank > 0) {
            for (int a = 0; a < this.possibleUpgrades.size(); a++) {
                int x = mouseX - (baseX + 48 + a * 16), y = mouseY - (baseY + 104);
                if (x >= 0 && y >= 0 && x < 16 && y < 16) this.fixedTooltip(graphics, this.possibleUpgrades.get(a));
            }
        }
        if (this.selected >= 0) {
            int x = mouseX - (baseX + 48), y = mouseY - (baseY + 48);
            if (x >= 0 && y >= 0 && x < 36 && y < 36) {
                graphics.setComponentTooltipForNextFrame(this.font, List.of(Component.translatable("wandtable.text1")), mouseX, mouseY);
            }
            x = mouseX - (baseX + 108);
            y = mouseY - (baseY + 58);
            if (x >= 0 && y >= 0 && x < 36 && y < 16) {
                graphics.setComponentTooltipForNextFrame(this.font, List.of(Component.translatable("wandtable.text2")), mouseX, mouseY);
            }
            if (table.size == 0 && this.rank * FocalManipulatorBlockEntity.XP_MULT <= this.playerLevel()) {
                x = mouseX - (baseX + 48);
                y = mouseY - (baseY + 88);
                if (x >= 0 && y >= 0 && x < 96 && y < 8) {
                    graphics.setComponentTooltipForNextFrame(this.font, List.of(Component.translatable("wandtable.text3")), mouseX, mouseY);
                }
            }
        }
        for (int a = 0; a < this.upgrades.size(); a++) {
            int x = mouseX - (baseX + 56 + a * 16), y = mouseY - (baseY + 32);
            if (x >= 0 && y >= 0 && x < 16 && y < 16) this.fixedTooltip(graphics, this.upgrades.get(a));
        }
    }

    /**
     * O {@code drawHoveringTextFixed}: o nome (roxo, sublinhado) e a explicação, quebrada na largura que sobra à direita
     * da tela, num quadro preso ao canto de cima da direita da tela.
     */
    private void fixedTooltip(GuiGraphicsExtractor graphics, FocusUpgradeTable.Type type) {
        int width = this.width - (this.leftPos + this.imageWidth - 16);
        List<FormattedCharSequence> list = new ArrayList<>();
        list.addAll(this.font.split(Component.translatable("focus.upgrade." + type.name() + ".name")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE), width));
        list.addAll(this.font.split(Component.translatable("focus.upgrade." + type.name() + ".text"), width));
        int k = 0;
        for (FormattedCharSequence s : list) k = Math.max(k, this.font.width(s));
        int j2 = this.leftPos + this.imageWidth - 36 + 12, k2 = this.topPos + 24 - 12;
        int i1 = 8;
        if (list.size() > 1) i1 += 2 + (list.size() - 1) * 10;
        int j1 = 0xF0100010;
        graphics.nextStratum();
        graphics.fill(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1);
        graphics.fill(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1);
        graphics.fill(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1);
        graphics.fill(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1);
        graphics.fill(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1);
        int k1 = 0x505000FF, l1 = (k1 & 0xFEFEFE) >> 1 | k1 & 0xFF000000;
        graphics.fillGradient(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
        graphics.fillGradient(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
        graphics.fill(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1);
        graphics.fill(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1);
        for (int i = 0; i < list.size(); i++) {
            graphics.text(this.font, list.get(i), j2, k2, -1, true);
            if (i == 0) k2 += 2;
            k2 += 10;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        FocalManipulatorBlockEntity table = this.menu.table;
        if (table == null) return handled;
        int mx = (int) event.x(), my = (int) event.y();
        int gx = this.leftPos, gy = this.topPos;
        int x = mx - (gx + 48), y = my - (gy + 88);
        if (table.size == 0 && this.selected >= 0 && this.rank * FocalManipulatorBlockEntity.XP_MULT <= this.playerLevel()
                && x >= 0 && y >= 0 && x < 96 && y < 8) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.selected);
            this.click();
            return true;
        }
        if (table.size == 0) {
            for (int a = 0; a < this.possibleUpgrades.size(); a++) {
                FocusUpgradeTable.Type u = this.possibleUpgrades.get(a);
                x = mx - (gx + 48 + a * 16);
                y = my - (gy + 104);
                if (x >= 0 && y >= 0 && x < 16 && y < 16) {
                    this.aspects = new AspectList();
                    if (this.selected == u.id()) {
                        this.selected = -1;
                    } else {
                        this.selected = u.id();
                        this.aspects = FocalManipulatorBlockEntity.costOf(u, this.rank);
                    }
                    this.click();
                    return true;
                }
            }
        }
        return handled;
    }

    private void click() {
        var p = this.minecraft.player;
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), TCSounds.CAMERA_CLACK.value(), SoundSource.PLAYERS, 0.4f, 1.0f, false);
    }
}
