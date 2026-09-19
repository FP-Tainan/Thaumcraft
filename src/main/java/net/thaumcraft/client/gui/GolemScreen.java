package net.thaumcraft.client.gui;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.GolemHelper;
import net.thaumcraft.inventory.GolemMenu;
import net.thaumcraft.item.GolemBellItem;
import net.thaumcraft.item.GolemCoreItem;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A tela do golem: o {@code GuiGolem} da 4.2.3.5 — a fala do golem (ou, no avançado, de vez em quando, uma ameaça), as
 * casas fantasmas com o fundo da matéria dele, as abas de cor (com a ordem), as chaves de cada núcleo e as de
 * comparação (com a entropia), e o golem em pé à esquerda. Os textos das chaves são os do original, em inglês.
 */
public class GolemScreen extends AbstractContainerScreen<GolemMenu> {
    private static final Identifier GUI = Thaumcraft.id("textures/gui/guigolem.png");
    private int threat = -1;

    public GolemScreen(GolemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
        GolemEntity golem = menu.golem;
        if (golem != null && golem.advanced && golem.level().getRandom().nextInt(4) == 0) this.threat = golem.level().getRandom().nextInt(9);
    }

    private void blit(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h) {
        g.blit(RenderPipelines.GUI_TEXTURED, GUI, x, y, u, v, w, h, 256, 256);
    }

    private void blitTinted(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h, int argb) {
        g.blit(RenderPipelines.GUI_TEXTURED, GUI, x, y, u, v, w, h, 256, 256, argb);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        GolemEntity golem = this.menu.golem;
        if (golem == null) return;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(0.5f, 0.5f);
        Component text = this.threat >= 0 ? Component.translatable("golemthreat." + this.threat + ".text")
                : Component.translatable("golemblurb." + golem.getCore() + ".text");
        g.textWithWordWrap(this.font, text, 80, 22, 110, 0xFFDDDDDD, false);
        if (this.menu.maxScroll > 0) {
            g.text(this.font, (this.menu.currentScroll + 1) + "/" + (this.menu.maxScroll + 1), 323, 140, 0xFFDDDDDD, false);
        }
        pose.popMatrix();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractBackground(g, mouseX, mouseY, partial);
        GolemEntity golem = this.menu.golem;
        int baseX = this.leftPos, baseY = this.topPos;
        this.blit(g, baseX, baseY, 0, 0, this.imageWidth, this.imageHeight);
        if (golem == null) return;
        int core = golem.getCore();
        int slots = golem.inventory.slotCount;
        int typeLoc = golem.getGolemTypeIndex() * 24;
        if (core > -1 && GolemCoreItem.hasInventory(core)) {
            for (int a = 0; a < Math.min(6, slots); a++) {
                this.blit(g, baseX + 96 + a / 2 * 28, baseY + 12 + a % 2 * 31, 184, typeLoc, 24, 24);
                if (golem.getUpgradeAmount(4) > 0) {
                    this.blit(g, baseX + 96 + a / 2 * 28, baseY + 4 + a % 2 * 31, 72, 168, 24, 12);
                    int color = golem.getColors(a + this.menu.currentScroll * 6);
                    if (color > -1) {
                        this.blitTinted(g, baseX + 105 + a / 2 * 28, baseY + 7 + a % 2 * 31, 0, 176, 6, 6, 0xFF000000 | GolemBellItem.COLORS[color]);
                    }
                }
                if (core == 5) {
                    Fluid fluid = GolemHelper.fluidInItem(golem.inventory.getItem(a + this.menu.currentScroll * 6));
                    if (fluid != null) {
                        TextureAtlasSprite sprite = net.thaumcraft.client.render.FluidSprites.still(fluid);
                        if (sprite != null) {
                            int tint = net.thaumcraft.client.render.FluidSprites.tint(fluid);
                            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, baseX + 100 + a / 2 * 28, baseY + 16 + a % 2 * 31, 16, 16, tint);
                        }
                    }
                }
            }
            if (slots > 6) {
                this.blit(g, baseX + 111, baseY + 68, 0, this.menu.currentScroll > 0 ? 200 : 208, 24, 8);
                this.blit(g, baseX + 135, baseY + 68, 24, this.menu.currentScroll < this.menu.maxScroll ? 200 : 208, 24, 8);
            }
        }
        if (core == 4 && golem.getUpgradeAmount(4) > 0) {
            boolean[] on = {golem.canAttackHostiles(), golem.canAttackAnimals(), golem.canAttackPlayers(), golem.canAttackCreepers()};
            for (int a = 0; a < 4; a++) {
                this.blit(g, baseX + 104, baseY + 5 + 16 * a, 8, 168, 8, 8);
                if (on[a]) this.blit(g, baseX + 104, baseY + 5 + 16 * a, 8, 176, 8, 8);
            }
            g.text(this.font, "Monsters", baseX + 122, baseY + 6, 0xFFFFCCCC, false);
            g.text(this.font, "Animals", baseX + 122, baseY + 22, 0xFFFFFFCC, false);
            g.text(this.font, "Players", baseX + 122, baseY + 38, 0xFFCCCCFF, false);
            g.text(this.font, "Creepers", baseX + 122, baseY + 54, 0xFFCCFFCC, false);
        }
        Matrix3x2fStack pose = g.pose();
        if (core == 0) {
            this.blit(g, baseX + 62, baseY + 54, 8, 168, 8, 8);
            String text = "Precise amount";
            if (!golem.getToggles()[0]) this.blit(g, baseX + 62, baseY + 54, 8, 176, 8, 8);
            else text = "Any amount";
            pose.pushMatrix();
            pose.translate(baseX + 66, baseY + 48);
            pose.scale(0.5f, 0.5f);
            g.text(this.font, text, -this.font.width(text) / 2, 0, 0xFFFDFDFD, false);
            pose.popMatrix();
        }
        if (core == 8) {
            boolean[] t = golem.getToggles();
            String[] texts = {t[0] ? "Empty space" : "Block", t[1] ? "Left click" : "Right click", t[2] ? "Sneaking" : "Not sneaking"};
            for (int i = 0; i < 3; i++) {
                this.blit(g, baseX + 42, baseY + 40 + i * 10, 8, 168, 8, 8);
                if (!t[i]) this.blit(g, baseX + 42, baseY + 40 + i * 10, 8, 176, 8, 8);
            }
            pose.pushMatrix();
            pose.translate(baseX + 53, baseY + 42);
            pose.scale(0.5f, 0.5f);
            for (int i = 0; i < 3; i++) g.text(this.font, texts[i], 0, i * 20, 0xFFFDFDFD, false);
            pose.popMatrix();
        }
        if (golem.getUpgradeAmount(5) > 0 && GolemCoreItem.canSort(core)) {
            int shiftx = core == 10 ? 66 : 180;
            int shifty = core == 10 ? 12 : 0;
            boolean[] on = {golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT()};
            String[] texts = {"Use Ore dictionary", "Ignore item damage", "Ignore NBT values"};
            for (int i = 0; i < 3; i++) {
                this.blit(g, baseX + shiftx, baseY + 24 + shifty + i * 10, 8, 168, 8, 8);
                if (on[i]) this.blit(g, baseX + shiftx, baseY + 24 + shifty + i * 10, 8, 176, 8, 8);
            }
            pose.pushMatrix();
            pose.translate(baseX + shiftx + 10, baseY + 26 + shifty);
            pose.scale(0.5f, 0.5f);
            for (int i = 0; i < 3; i++) g.text(this.font, texts[i], 0, i * 20, on[i] ? 0xFFFDFDFD : 0xFF666666, false);
            pose.popMatrix();
        }
        this.drawGolem(g, golem, baseX, baseY);
    }

    /** O {@code drawGolem}: o golem de pé, virado vinte graus, com a cabeça um pouco para o lado. */
    private void drawGolem(GuiGraphicsExtractor g, GolemEntity golem, int baseX, int baseY) {
        var renderer = this.minecraft.getEntityRenderDispatcher().getRenderer(golem);
        var state = renderer.createRenderState(golem, 1.0f);
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 160.0f;
            living.yRot = 15.0f;
            living.xRot = 0.0f;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0f;
            living.walkAnimationSpeed = 0.0f;
        }
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf();
        // a perspectiva do original (noventa graus, a doze de distância, cinco vezes maior, deslocada para a esquerda) põe
        // os pés do golem em (19, 65) da tela e a cabeça em (19, 18): cerca de cinquenta pontos por bloco
        Vector3f translation = new Vector3f(0.0f, state.boundingBoxHeight / 2.0f, 0.0f);
        g.entity(state, 50, translation, rotation, camera, baseX - 6, baseY + 10, baseX + 44, baseY + 73);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractRenderState(g, mouseX, mouseY, partial);
        GolemEntity golem = this.menu.golem;
        if (golem == null) return;
        int baseX = this.leftPos, baseY = this.topPos;
        int slots = golem.inventory.slotCount;
        // o nome da cor, em cima da tela, ao passar sobre a aba (o original só mostra com a entropia)
        if (golem.getCore() > -1 && GolemCoreItem.hasInventory(golem.getCore()) && golem.getUpgradeAmount(5) > 0) {
            for (int a = 0; a < Math.min(6, slots); a++) {
                int mx = mouseX - (baseX + 96 + a / 2 * 28), my = mouseY - (baseY + 4 + a % 2 * 31);
                if (mx >= 0 && my >= 0 && mx < 24 && my < 12) {
                    int c = golem.getColors(a + this.menu.currentScroll * 6);
                    String text = c >= 0 ? GolemBellItem.COLOR_NAMES[c] : "Any color";
                    g.text(this.font, text, baseX + 133 - this.font.width(text) / 2, baseY - 6, 0xFFFDFDFD, false);
                }
            }
        }
    }

    private void button(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        GolemEntity golem = this.menu.golem;
        if (golem == null) return handled;
        int px = (int) event.x(), py = (int) event.y();
        int baseX = this.leftPos, baseY = this.topPos;
        int core = golem.getCore();
        int slots = golem.inventory.slotCount;
        if (core > -1 && GolemCoreItem.hasInventory(core)) {
            for (int a = 0; a < Math.min(6, slots); a++) {
                if (golem.getUpgradeAmount(4) > 0) {
                    int x7 = px - (baseX + 96 + a / 2 * 28), y8 = py - (baseY + 4 + a % 2 * 31);
                    if (x7 >= 0 && y8 >= 0 && x7 < 8 && y8 < 12) {
                        this.button(a + this.menu.currentScroll * 6);
                        return true;
                    }
                    x7 = px - (baseX + 96 + 16 + a / 2 * 28);
                    if (x7 >= 0 && y8 >= 0 && x7 < 8 && y8 < 12) {
                        this.button(a + slots + this.menu.currentScroll * 6);
                        return true;
                    }
                }
            }
            if (slots > 6) {
                int x7 = px - (baseX + 111), y8 = py - (baseY + 68);
                if (x7 >= 0 && y8 >= 0 && x7 < 24 && y8 < 8 && this.menu.currentScroll > 0) {
                    this.button(66);
                    this.menu.currentScroll--;
                    return true;
                }
                x7 = px - (baseX + 135);
                if (x7 >= 0 && y8 >= 0 && x7 < 24 && y8 < 8 && this.menu.currentScroll < this.menu.maxScroll) {
                    this.button(67);
                    this.menu.currentScroll++;
                    return true;
                }
            }
        }
        if (core == 4) {
            for (int a = 0; a < 4; a++) {
                int x7 = px - (baseX + 104), y8 = py - (baseY + 5 + 16 * a);
                if (x7 >= 0 && y8 >= 0 && x7 < 8 && y8 < 8) {
                    this.button(51 + a);
                    return true;
                }
            }
        }
        if (core == 0) {
            int x7 = px - (baseX + 62), y8 = py - (baseY + 54);
            if (x7 >= 0 && y8 >= 0 && x7 < 8 && y8 < 8) {
                this.button(50);
                return true;
            }
        }
        if (core == 8) {
            for (int a = 0; a < 3; a++) {
                int x7 = px - (baseX + 42), y8 = py - (baseY + 40 + a * 10);
                if (x7 >= 0 && y8 >= 0 && x7 < 8 && y8 < 8) {
                    this.button(50 + a);
                    return true;
                }
            }
        }
        if (golem.getUpgradeAmount(5) > 0 && GolemCoreItem.canSort(core)) {
            int shiftx = core == 10 ? 66 : 180;
            int shifty = core == 10 ? 12 : 0;
            for (int a = 0; a < 3; a++) {
                int x7 = px - (baseX + shiftx), y8 = py - (baseY + 24 + a * 10 + shifty);
                if (x7 >= 0 && y8 >= 0 && x7 < 64 && y8 < 8) {
                    this.button(55 + a);
                    return true;
                }
            }
        }
        return handled;
    }

    /** Na casa de líquido, a dica é o nome do líquido (o {@code renderToolTip} do original). */
    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (this.hoveredSlot instanceof GolemMenu.Ghost ghost && ghost.isFluid() && ghost.hasItem()) {
            Fluid fluid = GolemHelper.fluidInItem(ghost.getItem());
            if (fluid != null) {
                g.setTooltipForNextFrame(this.font, FluidVariantAttributes.getName(FluidVariant.of(fluid)), mouseX, mouseY);
            }
            return;
        }
        super.extractTooltip(g, mouseX, mouseY);
    }
}
