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

        // a esfera de gelo e a brasa, com os desenhistas do original
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.FROST_SHARD, net.thaumcraft.client.render.FrostShardRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.EMBER, net.thaumcraft.client.render.EmberRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.PRIMAL_ORB, net.thaumcraft.client.render.PrimalOrbRenderer::new);
        // os raios, fachos e faíscas dos focos
        net.thaumcraft.client.fx.ThaumFx.init();
        net.thaumcraft.client.fx.FocusEffects.init();

        // o golem, com a pele da matéria de que ele é feito
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.GolemRenderer.LAYER,
                net.thaumcraft.client.render.GolemModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.GOLEM,
                net.thaumcraft.client.render.GolemRenderer::new);
        // o nó de aura é uma nuvem de bolhas, e quem a pinta é este desenhista
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.NODE, net.thaumcraft.client.render.NodeRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("wand"), net.thaumcraft.client.render.WandRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("alembic"), net.thaumcraft.client.render.AlembicItemRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("tube_valve"), net.thaumcraft.client.render.TubeValveItemRenderer.Unbaked.CODEC);

        // o que os Óculos da Revelação mostram no recipiente na mira
        net.thaumcraft.client.render.GogglesOverlay.init();
        // o vapor do cano que sangra, com os tufos de fumaça do próprio mod
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(
                net.thaumcraft.registry.TCParticles.VENT, net.thaumcraft.client.particle.VentParticle.Provider::new);

        // o jarro mostra o que guarda: a névoa na cor do aspecto e, com rótulo, o símbolo dele na frente
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.JAR, net.thaumcraft.client.render.JarRenderer::new);
        // o tubo mostra a essência correndo por dentro dele
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE, net.thaumcraft.client.render.TubeRenderer::new);
        // e a valvula, cujo manipulo gira e rosqueia conforme abre e fecha
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE_VALVE,
                net.thaumcraft.client.render.TubeValveRenderer::new);
        // e o alambique, que é de metal fechado, só se lê com os Óculos da Revelação
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ALEMBIC,
                net.thaumcraft.client.render.AlembicRenderer::new);

        // o pedestal mostra o que segura: pairando um dedo acima do prato e girando devagar
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.PEDESTAL,
                net.thaumcraft.client.render.PedestalRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.CRUCIBLE, net.thaumcraft.client.render.CrucibleRenderer::new);
        // as variantes do tubo: o estreito se desenha como o tubo; o de mão única e o tampão têm anéis
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE_RESTRICT,
                context -> (net.minecraft.client.renderer.blockentity.BlockEntityRenderer) new net.thaumcraft.client.render.TubeRenderer(context));
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE_ONEWAY, net.thaumcraft.client.render.TubeVariantRenderers.Oneway::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE_BUFFER, net.thaumcraft.client.render.TubeVariantRenderers.Buffer::new);
        // o buraco do Buraco Portátil: as paredes de estrelas e as faíscas das quinas
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.HOLE, net.thaumcraft.client.render.HoleRenderer::new);
        // a tabela de aspectos que o servidor montou; no jogo de um jogador só, o servidor é esta mesma máquina
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.ObjectAspectsSync.TYPE, (payload, context) -> context.client().execute(() -> {
                    if (!context.client().hasSingleplayerServer()) {
                        net.thaumcraft.api.aspects.ObjectAspects.accept(payload.table());
                    }
                }));
        // o bloco protegido, com o bloco guardado e as runas do foco de Proteção; e o escudo que acende na batida
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.WARDED, net.thaumcraft.client.render.WardedRenderer::new);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(
                net.thaumcraft.client.fx.BlockWardFx::clientTick);
        net.thaumcraft.block.entity.HoleBlockEntity.clientEffects = net.thaumcraft.client.render.HoleRenderer::sparkles;
        // a mesa de pesquisa: o desenhista da mesa inteira e a tela do tabuleiro
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.RESEARCH_TABLE, net.thaumcraft.client.render.ResearchTableRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                net.thaumcraft.registry.TCMenus.RESEARCH_TABLE, net.thaumcraft.client.gui.ResearchTableScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                net.thaumcraft.registry.TCMenus.ARCANE_WORKBENCH,
                net.thaumcraft.client.gui.ArcaneWorkbenchScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                net.thaumcraft.registry.TCMenus.ALCHEMICAL_FURNACE,
                net.thaumcraft.client.gui.AlchemicalFurnaceScreen::new);
        // a mácula vem cinza na textura, como no original; quem a pinta é o jogo, com a cor do capim
        // do bioma maculado do mod — 7160201, que é 0x6D40C9
        net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                java.util.List.of(state -> 0xFF6D40C9),
                net.thaumcraft.registry.TCBlocks.TAINT_SOIL,
                net.thaumcraft.registry.TCBlocks.TAINT_FIBRES);
        // as folhas das árvores mágicas: a da grande-madeira pega o verde da folhagem do lugar, a do
        // pinheiro-de-prata tem o cinza-azulado fixo do original, 8952234
        net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                java.util.List.of(net.minecraft.client.color.block.BlockTintSources.foliage()),
                net.thaumcraft.registry.TCBlocks.GREATWOOD_LEAVES);
        net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(
                        0xFF000000 | net.thaumcraft.block.MagicalLeavesBlock.SILVERWOOD_COLOR)),
                net.thaumcraft.registry.TCBlocks.SILVERWOOD_LEAVES);
        // e a faísca que as do pinheiro soltam de vez em quando: o sparkle de tamanho dois e cor sete
        net.thaumcraft.block.MagicalLeavesBlock.clientEffects = (level, pos, random) ->
                net.thaumcraft.client.fx.Sparkle.spawn(random,
                        pos.getX() + 0.5f + random.nextFloat() - random.nextFloat(),
                        pos.getY() + 0.5f + random.nextFloat() - random.nextFloat(),
                        pos.getZ() + 0.5f + random.nextFloat() - random.nextFloat(), 2.0f, 7, 0.0f);
        net.thaumcraft.block.ShimmerleafBlock.clientEffects = net.thaumcraft.client.fx.Wisp::colored;
        // o Nitor é um orbe de luz, e não um desenho chapado
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.NITOR,
                net.thaumcraft.client.render.NitorRenderer::new);
        // as marcas do sino do golem, que só aparecem com o sino na mão
        net.thaumcraft.client.render.MarkerOverlay.init();
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
                net.thaumcraft.net.TCNetwork.BlockSparkle.TYPE, (payload, context) -> context.client().execute(() ->
                        net.thaumcraft.client.fx.GenericFx.blockSparkle(payload.pos().getX(), payload.pos().getY(),
                                payload.pos().getZ(), payload.colour(), 1)));
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
