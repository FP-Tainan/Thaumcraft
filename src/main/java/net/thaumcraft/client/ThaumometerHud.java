package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.ScanManager;
import net.thaumcraft.registry.TCItems;

import java.util.List;
import java.util.Map;

/**
 * O visor do thaumômetro: com o aparelho erguido, o que está na mira aparece escrito por dentro da lente —
 * o nome em cima e os símbolos do que aquilo é feito embaixo, com a quantidade de cada um.
 *
 * <p>No canto de baixo, à direita, fica o resumo do último exame: o que foi aprendido e quantos pontos
 * entraram em cada aspecto, do jeito que o mod original mostra.
 */
public final class ThaumometerHud {
    private static final net.minecraft.resources.Identifier UNKNOWN = Thaumcraft.id("textures/aspects/_unknown.png");

    /** Até onde a mira alcança: o mesmo do aparelho. */
    private static final double REACH = 16.0;

    /** O resumo do último exame, que fica um pouco na tela e some. */
    private static Component learned;
    private static List<Component> gains = List.of();
    private static int fade;

    private ThaumometerHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("thaumometer"), (graphics, tracker) -> draw(graphics));
    }

    /** Chamado quando o servidor avisa que um exame terminou. */
    public static void showSummary(Component title, List<Component> lines) {
        learned = title;
        gains = lines;
        fade = 140;
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        if (fade > 0) {
            summary(graphics, minecraft);
            fade--;
        }
        if (!raised(player)) return;

        // o visor olha para o mesmo alvo que o exame vai pegar, e na mesma ordem: bicho, depois bloco
        Component name;
        AspectList aspects;
        Entity creature = ScanManager.entityInSight(minecraft.level, player, REACH);
        if (creature != null) {
            name = ScanManager.nameOf(creature);
            aspects = ScanManager.aspectsOf(creature);
        } else {
            net.minecraft.core.BlockPos pos = ScanManager.blockInSight(player, REACH);
            if (pos == null) return;
            BlockState state = minecraft.level.getBlockState(pos);
            if (state.isAir()) return;
            name = ScanManager.nameOf(state);
            aspects = ScanManager.aspectsOf(state);
        }

        PlayerKnowledge knowledge = Knowledges.of(player);
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        // o vazio do aro: é aqui dentro que tudo tem de caber
        int lens = 86;

        graphics.centeredText(minecraft.font, name, centerX, centerY - 46, 0xFFF3E4B4);

        if (aspects.isEmpty()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN, centerX - 10, centerY - 10, 0, 0, 20, 20, 20, 20);
            return;
        }
        if (!ScanManager.canUnderstand(knowledge, aspects)) {
            // não dá para ler: o aparelho mostra o que falta entender antes
            Aspect missing = ScanManager.missingParent(knowledge, aspects);
            graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN, centerX - 10, centerY - 16, 0, 0, 20, 20, 20, 20);
            if (missing != null) {
                graphics.centeredText(minecraft.font, missing.name(), centerX, centerY + 10, 0xFFB9A0D8);
            }
            return;
        }

        // os símbolos, em fileiras que cabem no vazio da lente
        int icon = 18;
        int step = 26;
        int perRow = Math.max(1, Math.min(3, lens / step));
        int count = aspects.size();
        int rows = (count + perRow - 1) / perRow;
        int top = centerY - (rows * step) / 2 + 6;
        int index = 0;
        for (Map.Entry<Aspect, Integer> entry : aspects.entries()) {
            int column = index % perRow;
            int row = index / perRow;
            int columns = Math.min(perRow, count - row * perRow);
            int x = centerX - (columns * step) / 2 + column * step + (step - icon) / 2;
            int y = top + row * step;
            Aspect aspect = entry.getKey();
            graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(), x, y, 0, 0, icon, icon,
                    icon, icon, icon, icon, 0xFF000000 | aspect.color());
            String amount = String.valueOf(entry.getValue());
            graphics.text(minecraft.font, amount, x + icon - minecraft.font.width(amount) + 2, y + icon - 4, 0xFFFFFFFF);
            index++;
        }
    }

    /** O resumo do último exame, encostado no canto de baixo à direita. */
    private static void summary(GuiGraphicsExtractor graphics, Minecraft minecraft) {
        if (learned == null) return;
        int alpha = Math.min(255, fade * 4) << 24;
        int right = graphics.guiWidth() - 6;
        int y = graphics.guiHeight() - 16 - gains.size() * 11;
        graphics.text(minecraft.font, learned,
                right - minecraft.font.width(learned), y, alpha | 0xE8D9A8);
        for (Component line : gains) {
            y += 11;
            graphics.text(minecraft.font, line, right - minecraft.font.width(line), y, alpha | 0xC8F0C8);
        }
    }

    /** O aparelho está erguido ao olho? */
    private static boolean raised(Player player) {
        return player.isUsingItem() && player.getUseItem().is(TCItems.THAUMOMETER);
    }
}
