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
                net.thaumcraft.registry.TCEntities.ASPECT_ORB, net.thaumcraft.client.render.AspectOrbRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.PRIMAL_ORB, net.thaumcraft.client.render.PrimalOrbRenderer::new);
        // o Alumentum voando: o RenderAlumentum não desenha nada, só o rastro aparece
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.ALUMENTUM, net.minecraft.client.renderer.entity.NoopRenderer::new);
        // os raios, fachos e faíscas dos focos
        net.thaumcraft.client.fx.ThaumFx.init();
        net.thaumcraft.client.fx.FocusEffects.init();

        // a armadura de fortaleza, com o modelo do ModelFortressArmor
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.FortressArmorRenderer.LAYER,
                net.thaumcraft.client.render.model.FortressArmorModel::createLayer);
        net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer.register(net.thaumcraft.client.render.FortressArmorRenderer::new,
                net.thaumcraft.registry.TCItems.FORTRESS_HELMET, net.thaumcraft.registry.TCItems.FORTRESS_CHESTPLATE,
                net.thaumcraft.registry.TCItems.FORTRESS_LEGGINGS);
        // o arreio taumostático, com o ModelHoverHarness
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.HoverHarnessRenderer.LAYER,
                net.thaumcraft.client.render.HoverHarnessRenderer::createLayer);
        net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer.register(net.thaumcraft.client.render.HoverHarnessRenderer::new,
                net.thaumcraft.registry.TCItems.HOVER_HARNESS);
        // o golem, com a pele da matéria de que ele é feito
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.GolemRenderer.LAYER,
                net.thaumcraft.client.render.GolemModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.GOLEM,
                net.thaumcraft.client.render.GolemRenderer::new);
        // as criaturas: os zumbis, o fogo-fátuo e o morcego de fogo
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.BRAINY_ZOMBIE, net.thaumcraft.client.render.BrainyZombieRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.GIANT_BRAINY_ZOMBIE, net.thaumcraft.client.render.BrainyZombieRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.WISP, net.thaumcraft.client.render.WispRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.FireBatRenderer.LAYER, net.thaumcraft.client.render.FireBatModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.FIREBAT, net.thaumcraft.client.render.FireBatRenderer::new);
        // o pech, a rajada dele (que é só os fogos-fátuos) e a tela de troca
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.PechRenderer.LAYER, net.thaumcraft.client.render.PechModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.PECH, net.thaumcraft.client.render.PechRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.PECH_BLAST, net.minecraft.client.renderer.entity.NoopRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.PECH,
                net.thaumcraft.client.gui.PechScreen::new);
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
                net.thaumcraft.registry.TCBlockEntities.TUBE_FILTER, net.thaumcraft.client.render.TubeFilterRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE_ONEWAY, net.thaumcraft.client.render.TubeVariantRenderers.Oneway::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.TUBE_BUFFER, net.thaumcraft.client.render.TubeVariantRenderers.Buffer::new);
        // o buraco do Buraco Portátil: as paredes de estrelas e as faíscas das quinas
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.HOLE, net.thaumcraft.client.render.HoleRenderer::new);
        // o vidro dos espelhos: prateado sem par, o céu de estrelas com par
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.MIRROR, net.thaumcraft.client.render.MirrorRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ESSENTIA_MIRROR, net.thaumcraft.client.render.MirrorRenderer::new);
        // a tabela de aspectos que o servidor montou; no jogo de um jogador só, o servidor é esta mesma máquina
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.ObjectAspectsSync.TYPE, (payload, context) -> context.client().execute(() -> {
                    if (!context.client().hasSingleplayerServer()) {
                        net.thaumcraft.api.aspects.ObjectAspects.accept(payload.table());
                    }
                }));
        // as pedras de pavimento: a faísca verde da de Viagem e as runas da de Proteção
        net.thaumcraft.block.PavingStoneBlock.clientEffects = new net.thaumcraft.block.PavingStoneBlock.ClientEffects() {
            @Override
            public void sparkle(net.minecraft.core.BlockPos pos, int colour, int count) {
                net.thaumcraft.client.fx.GenericFx.blockSparkle(pos.getX(), pos.getY(), pos.getZ(), colour, count);
            }

            @Override
            public void runes(net.minecraft.core.BlockPos pos, double y, float r, float g, float b, int duration, float gravity) {
                net.thaumcraft.client.fx.BlockRunes.spawn(pos.getX(), y, pos.getZ(), r, g, b, duration, gravity);
            }
        };
        // as mesas do ModelArcaneWorkbench: a bancada arcana (com a varinha deitada) e o item de cada mesa
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ARCANE_WORKBENCH, net.thaumcraft.client.render.ArcaneWorkbenchRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("workbench"), net.thaumcraft.client.render.WorkbenchModel.Unbaked.CODEC);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.DECONSTRUCTION_TABLE, net.thaumcraft.client.render.DeconstructionTableRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ETHEREAL_BLOOM, net.thaumcraft.client.render.EtherealBloomRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.DECONSTRUCTION_TABLE,
                net.thaumcraft.client.gui.DeconstructionTableScreen::new);
        // o fole, com o modelo do original, no chão e na mão
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.BELLOWS, net.thaumcraft.client.render.BellowsRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("bellows"), net.thaumcraft.client.render.BellowsRenderer.Unbaked.CODEC);
        // os minérios e cristais: a veia da pedra infundida na cor do aspecto, o aglomerado de cristal e a faísca dele
        int[] veins = {16777086, 16727041, 37119, 40960, 15650047, 5592439};
        int vein = 0;
        for (var stone : net.thaumcraft.registry.TCBlocks.INFUSED_STONE.values()) {
            net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                    java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xFF000000 | veins[vein++])), stone);
        }
        // as velas: o corpo e os pingos na cor de cada uma (o colorMultiplier do BlockCandle); o pavio sem tinta
        for (int i = 0; i < net.thaumcraft.registry.TCBlocks.CANDLE_COLOURS.length; i++) {
            net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                    java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xFF000000 | net.thaumcraft.registry.TCBlocks.CANDLE_TINTS[i])),
                    net.thaumcraft.registry.TCBlocks.TALLOW_CANDLES.get(net.thaumcraft.registry.TCBlocks.CANDLE_COLOURS[i]));
        }
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.CRYSTAL_CLUSTER, net.thaumcraft.client.render.CrystalClusterRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("crystal_cluster"), net.thaumcraft.client.render.CrystalClusterRenderer.Unbaked.CODEC);
        net.thaumcraft.block.CrystalClusterBlock.clientEffects = (level, pos, colour, random) ->
                net.thaumcraft.client.fx.Spark.spawn(new net.minecraft.world.phys.Vec3(pos.getX() + 0.3 + random.nextFloat() * 0.4,
                        pos.getY() + 0.3 + random.nextFloat() * 0.4, pos.getZ() + 0.3 + random.nextFloat() * 0.4),
                        0.2f + random.nextFloat() * 0.1f, 0xCC000000 | colour, random);
        // a rede de vis: estabilizadores, transdutor, nó energizado, relés e carregador
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.NODE_STABILIZER, net.thaumcraft.client.render.NodeStabilizerRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.NODE_CONVERTER, net.thaumcraft.client.render.NodeConverterRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ENERGIZED_NODE, net.thaumcraft.client.render.EnergizedNodeRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.VIS_RELAY, context -> new net.thaumcraft.client.render.VisRelayRenderer<>(context));
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.WORKBENCH_CHARGER, context -> new net.thaumcraft.client.render.VisRelayRenderer<>(context));
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("node_stabilizer"), net.thaumcraft.client.render.NodeStabilizerRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("node_converter"), net.thaumcraft.client.render.NodeConverterRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("vis_relay"), net.thaumcraft.client.render.VisRelayRenderer.Unbaked.CODEC);
        // a alquimia: a centrífuga, o cristalizador e a cor da essência cristalizada
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.CENTRIFUGE, net.thaumcraft.client.render.CentrifugeRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("centrifuge"), net.thaumcraft.client.render.CentrifugeRenderer.Unbaked.CODEC);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ESSENTIA_CRYSTALIZER, net.thaumcraft.client.render.EssentiaCrystalizerRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("essentia_crystalizer"), net.thaumcraft.client.render.EssentiaCrystalizerRenderer.Unbaked.CODEC);
        net.thaumcraft.client.render.AspectTint.register();
        // a tecla de trocar foco, com o menu radial, e a tela da bolsa de focos
        net.thaumcraft.client.FocusRadial.init();
        // as casas de amuleto, anel e cinto do Baubles
        net.thaumcraft.client.BaublesClient.init();
        net.thaumcraft.client.RunicHud.init();
        net.thaumcraft.client.HoverClient.init();
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.HOVER_HARNESS,
                net.thaumcraft.client.gui.HoverHarnessScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.BAUBLES,
                net.thaumcraft.client.gui.BaublesScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.FOCUS_POUCH,
                net.thaumcraft.client.gui.FocusPouchScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.HAND_MIRROR,
                net.thaumcraft.client.gui.HandMirrorScreen::new);
        // as botas do viajante: o empurrão de quem anda é do lado de quem joga
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            var player = client.player;
            if (player != null && !client.isPaused()
                    && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof net.thaumcraft.item.TravellerBootsItem) {
                net.thaumcraft.item.TravellerBootsItem.tickWorn(player);
            }
        });
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.HUNGRY_CHEST, net.thaumcraft.client.render.HungryChestRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("hungry_chest"), net.thaumcraft.client.render.HungryChestRenderer.Unbaked.CODEC);
        // o levitador: o brilho de dentro, verde da terra em cima e roxo dos lados, e a faísca que sobe dele
        net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xFF00A000),
                        net.minecraft.client.color.block.BlockTintSources.constant(0xFFDD11FF)),
                net.thaumcraft.registry.TCBlocks.LEVITATOR);
        net.thaumcraft.block.LevitatorBlock.clientEffects = (x, y, z, random) ->
                net.thaumcraft.client.fx.Sparkle.spawn(random, x, y, z, 1.0f, 3, -0.3f);
        net.thaumcraft.block.entity.LevitatorBlockEntity.sneaking = player -> player.isShiftKeyDown();
        // as lâmpadas: o corpo é bloco comum, e o bocal que as prende vem do desenhista
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ARCANE_LAMP, net.thaumcraft.client.render.ArcaneLampRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.GROWTH_LAMP, net.thaumcraft.client.render.ArcaneLampRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.FERTILITY_LAMP, net.thaumcraft.client.render.ArcaneLampRenderer::new);
        net.thaumcraft.item.CrystalEssenceItem.known = aspect -> {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            return player == null || net.thaumcraft.research.Knowledges.of(player).hasDiscovered(aspect);
        };
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.INFUSION_MATRIX, net.thaumcraft.client.render.InfusionMatrixRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("infusion_matrix"), net.thaumcraft.client.render.InfusionMatrixRenderer.Unbaked.CODEC);
        // os pilares do altar de infusão, com o modelo do original
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.INFUSION_PILLAR, net.thaumcraft.client.render.InfusionPillarRenderer::new);
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
        // o vidro protegido emendado
        net.thaumcraft.client.render.WardedGlassModel.init();
        // a fornalha infernal: as paredes que formam o desenho grande de cada face, as faíscas de quando ela se forma
        // e a lava que espirra pela boca
        net.thaumcraft.client.render.InfernalFurnaceModel.init();
        net.thaumcraft.block.InfernalFurnaceBlock.clientEffects = (level, pos) ->
                net.thaumcraft.client.fx.GenericFx.blockSparkle(pos.getX(), pos.getY(), pos.getZ(), 0xFF6600, 5);
        net.thaumcraft.block.entity.InfernalFurnaceBlockEntity.clientEffects = (level, pos, fx, fz) -> {
            var random = level.getRandom();
            level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                    pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3 + fx,
                    pos.getY() + 0.3,
                    pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3 + fz, 0.0, 0.0, 0.0);
        };
        net.thaumcraft.block.ShimmerleafBlock.clientEffects = net.thaumcraft.client.fx.Wisp::colored;
        // o cogumelo-vis: a chaminha roxa que encolhe e cai
        net.thaumcraft.block.VishroomBlock.clientEffects = (x, y, z) ->
                net.thaumcraft.client.fx.Wisp.coloredFalling(x, y, z, 0.1f, 0.5f, 0.3f, 0.8f, true, 0.015f);
        // a vagem de mana: a casca e o miolo que brilha
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.MANA_POD, net.thaumcraft.client.render.ManaPodRenderer::new);
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
        // o choque do fogo-fátuo: o bolt de uma criatura na outra (do pé de quem dá até o peito de quem leva)
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.EntityZap.TYPE, (payload, context) -> context.client().execute(() -> {
                    var level = context.client().level;
                    if (level == null) return;
                    var source = level.getEntity(payload.source());
                    var target = level.getEntity(payload.target());
                    if (source == null || target == null) return;
                    net.thaumcraft.client.fx.LightningBolt bolt = new net.thaumcraft.client.fx.LightningBolt(source.getX(), source.getY(), source.getZ(),
                            target.getX(), target.getY() + target.getEyeHeight() - 0.7, target.getZ(), level.getRandom().nextLong(), 3, 0.4f, 4);
                    bolt.defaultFractal();
                    bolt.setType(0);
                    bolt.finalizeBolt();
                }));
        // o raio de um nó a outro: o nodeBolt de tipo 0 e o estalo baixinho
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.BlockZap.TYPE, (payload, context) -> context.client().execute(() -> {
                    var level = context.client().level;
                    if (level == null) return;
                    net.thaumcraft.client.fx.LightningBolt bolt = new net.thaumcraft.client.fx.LightningBolt(payload.from().x, payload.from().y,
                            payload.from().z, payload.to().x, payload.to().y, payload.to().z, level.getRandom().nextLong(), 10, 4.0f, 5);
                    bolt.defaultFractal();
                    bolt.setType(0);
                    bolt.finalizeBolt();
                    level.playLocalSound(payload.from().x, payload.from().y, payload.from().z, net.thaumcraft.registry.TCSounds.ZAP.value(),
                            net.minecraft.sounds.SoundSource.BLOCKS, 0.1f, 1.0f + level.getRandom().nextFloat() * 0.2f, false);
                }));
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
