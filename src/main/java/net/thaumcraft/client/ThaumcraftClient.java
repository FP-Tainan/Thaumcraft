package net.thaumcraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.ScannerRenderer;

/** A parte do mod que só existe na máquina de quem joga: as peças desenhadas e as telas. */
public class ThaumcraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // o thaumômetro é peça de três dimensões, como no original: entra na lista do jogo junto do baú
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("scanner"), ScannerRenderer.Unbaked.CODEC);

        // a lasca de gelo se desenha como o item dela, igual a uma bola de neve
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.FROST_SHARD,
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        // o nó de aura é uma nuvem de bolhas, e quem a pinta é este desenhista
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.NODE, net.thaumcraft.client.render.NodeRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("wand"), net.thaumcraft.client.render.WandRenderer.Unbaked.CODEC);

        // o jarro mostra o que guarda: a névoa na cor do aspecto e o símbolo dele no vidro
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.JAR, net.thaumcraft.client.render.JarRenderer::new);

        // o pedestal mostra o que segura: pairando um dedo acima do prato e girando devagar
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.PEDESTAL,
                net.thaumcraft.client.render.PedestalRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.CRUCIBLE, net.thaumcraft.client.render.CrucibleRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                net.thaumcraft.registry.TCMenus.ARCANE_WORKBENCH,
                net.thaumcraft.client.gui.ArcaneWorkbenchScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                net.thaumcraft.registry.TCMenus.ALCHEMICAL_FURNACE,
                net.thaumcraft.client.gui.AlchemicalFurnaceScreen::new);
        ThaumometerHud.init();
        AspectTooltip.init();
        WandHud.init();
        // o Thaumonomicon abre o mapa das pesquisas, e nada disso encosta no lado do servidor
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() || !player.getItemInHand(hand).is(net.thaumcraft.registry.TCItems.THAUMONOMICON)) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            net.minecraft.client.Minecraft.getInstance().setScreenAndShow(
                    new net.thaumcraft.client.gui.ThaumonomiconScreen());
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        // o resumo do exame chega do servidor e vai para o canto da tela
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.ScanSummary.TYPE, (payload, context) -> context.client().execute(() -> {
                    java.util.List<net.minecraft.network.chat.Component> linhas = new java.util.ArrayList<>();
                    for (int index = 0; index < payload.tags().size(); index++) {
                        var aspect = net.thaumcraft.api.aspects.Aspect.of(payload.tags().get(index));
                        if (aspect == null) continue;
                        linhas.add(net.minecraft.network.chat.Component.translatable("tc.scan.gain",
                                aspect.name(), payload.gained().get(index), payload.totals().get(index)));
                    }
                    ThaumometerHud.showSummary(
                            net.minecraft.network.chat.Component.literal(payload.name()), linhas);
                }));
    }
}
