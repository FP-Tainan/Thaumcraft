package net.thaumcraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.ScannerRenderer;

/** A parte do mod que só existe na máquina de quem joga: as peças desenhadas e as telas. */
public class ThaumcraftClient implements ClientModInitializer {
    /** O {@code lastSound} do {@code PacketAspectPool}: um tinido a cada cem milissegundos, no máximo. */
    private static long lastPoolSound;

    @Override
    public void onInitializeClient() {
        // o thaumômetro é peça de três dimensões, como no original: entra na lista do jogo junto do baú
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("scanner"), ScannerRenderer.Unbaked.CODEC);

        // a esfera de gelo e a brasa, com os desenhistas do original
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.FROST_SHARD, net.thaumcraft.client.render.FrostShardRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.EMBER, net.thaumcraft.client.render.EmberRenderer::new);
        // os orbes das melhorias de foco: a bola de fogo e o choque de terra, com o rastro de fumaça e o clarão
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.EXPLOSIVE_ORB, net.thaumcraft.client.render.FocusOrbRenderers.Explosive::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.SHOCK_ORB, net.thaumcraft.client.render.FocusOrbRenderers.Electric::new);
        net.thaumcraft.entity.ExplosiveOrbEntity.clientTrail = orb -> {
            var r = orb.getRandom();
            net.thaumcraft.client.fx.ThaumFx.add(new net.thaumcraft.client.fx.GenericFx(
                    orb.xo + (r.nextFloat() - r.nextFloat()) * 0.3f, orb.yo + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    orb.zo + (r.nextFloat() - r.nextFloat()) * 0.3f, 0.0, 0.0, 0.0, 1.0f, 1.0f, 1.0f, 0.8f, false,
                    151, 9, 1, 7 + r.nextInt(5), 0, 2.0f + r.nextFloat()));
        };
        net.thaumcraft.entity.ShockOrbEntity.clientBurst = orb ->
                net.thaumcraft.client.fx.Burst.spawn(orb.position(), 3.0f, orb.getRandom());
        net.thaumcraft.entity.FrostShardEntity.clientSparkle = (shard, frosty) -> {
            var r = shard.getRandom();
            float s = shard.getDamage() / 10.0f;
            for (int a = 0; a < frosty; a++) {
                net.thaumcraft.client.fx.Sparkle.spawn(r, shard.getX() - s + r.nextFloat() * s * 2.0f,
                        shard.getY() - s + r.nextFloat() * s * 2.0f, shard.getZ() - s + r.nextFloat() * s * 2.0f, 0.4f, 6, 0.005f);
            }
        };
        net.thaumcraft.block.SparkFieldBlock.clientEffects = (level, pos, random) -> {
            float h = random.nextFloat() * 0.33f;
            int red = (int) ((0.65f + random.nextFloat() * 0.1f) * 255.0f);
            net.thaumcraft.client.fx.Spark.spawn(new net.minecraft.world.phys.Vec3(pos.getX() + random.nextFloat(),
                    pos.getY() + 0.1515f + h / 2.0f, pos.getZ() + random.nextFloat()), 0.33f + h,
                    0xCC000000 | red << 16 | 0xFFFF, random);
        };
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

        // o Culto Carmesim: o corpo, as armaduras de modelo próprio, o portal e o orbe
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.CultistRenderer.LAYER, net.thaumcraft.client.render.CultistRenderer::createLayer);
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(net.thaumcraft.client.render.CultistArmorRenderer.ROBE_INNER,
                () -> net.thaumcraft.client.render.model.CultistRobeModel.createLayer(true));
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(net.thaumcraft.client.render.CultistArmorRenderer.ROBE_OUTER,
                () -> net.thaumcraft.client.render.model.CultistRobeModel.createLayer(false));
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(net.thaumcraft.client.render.CultistArmorRenderer.PLATE_INNER,
                () -> net.thaumcraft.client.render.model.CultistPlateModel.createLayer(true));
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(net.thaumcraft.client.render.CultistArmorRenderer.PLATE_OUTER,
                () -> net.thaumcraft.client.render.model.CultistPlateModel.createLayer(false));
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(net.thaumcraft.client.render.CultistArmorRenderer.LEADER_INNER,
                () -> net.thaumcraft.client.render.model.CultistLeaderModel.createLayer(true));
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(net.thaumcraft.client.render.CultistArmorRenderer.LEADER_OUTER,
                () -> net.thaumcraft.client.render.model.CultistLeaderModel.createLayer(false));
        net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer.register(context -> new net.thaumcraft.client.render.CultistArmorRenderer(context,
                        Thaumcraft.id("textures/models/cultist_robe_armor.png"), net.thaumcraft.client.render.CultistArmorRenderer.ROBE_INNER,
                        net.thaumcraft.client.render.CultistArmorRenderer.ROBE_OUTER, net.thaumcraft.client.render.CultistArmorRenderer::robeSway),
                net.thaumcraft.registry.TCItems.CULTIST_ROBE_HELMET, net.thaumcraft.registry.TCItems.CULTIST_ROBE_CHESTPLATE,
                net.thaumcraft.registry.TCItems.CULTIST_ROBE_LEGGINGS);
        net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer.register(context -> new net.thaumcraft.client.render.CultistArmorRenderer(context,
                        Thaumcraft.id("textures/models/cultist_plate_armor.png"), net.thaumcraft.client.render.CultistArmorRenderer.PLATE_INNER,
                        net.thaumcraft.client.render.CultistArmorRenderer.PLATE_OUTER, net.thaumcraft.client.render.CultistArmorRenderer::plateSway),
                net.thaumcraft.registry.TCItems.CULTIST_PLATE_HELMET, net.thaumcraft.registry.TCItems.CULTIST_PLATE_CHESTPLATE,
                net.thaumcraft.registry.TCItems.CULTIST_PLATE_LEGGINGS);
        net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer.register(context -> new net.thaumcraft.client.render.CultistArmorRenderer(context,
                        Thaumcraft.id("textures/models/cultist_leader_armor.png"), net.thaumcraft.client.render.CultistArmorRenderer.LEADER_INNER,
                        net.thaumcraft.client.render.CultistArmorRenderer.LEADER_OUTER, net.thaumcraft.client.render.CultistArmorRenderer::leaderSway),
                net.thaumcraft.registry.TCItems.CULTIST_LEADER_HELMET, net.thaumcraft.registry.TCItems.CULTIST_LEADER_CHESTPLATE,
                net.thaumcraft.registry.TCItems.CULTIST_LEADER_LEGGINGS);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.CULTIST_KNIGHT,
                net.thaumcraft.client.render.CultistRenderer::new);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.CULTIST_CLERIC,
                net.thaumcraft.client.render.CultistRenderer::new);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.CULTIST_LEADER,
                net.thaumcraft.client.render.CultistRenderer::new);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.CULTIST_PORTAL,
                net.thaumcraft.client.render.CultistPortalRenderer::new);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.GOLEM_ORB,
                net.thaumcraft.client.render.FocusOrbRenderers.GolemOrb::new);
        net.thaumcraft.entity.GolemOrbEntity.clientBurst = orb -> net.thaumcraft.client.NodeClient.burst(orb.level(), orb.position(), false);
        net.thaumcraft.client.ChampionClient.init();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.BlockArc.TYPE, (payload, ctx) -> ctx.client().execute(() -> {
                    var level = ctx.client().level;
                    if (level == null) return;
                    var source = level.getEntity(payload.source());
                    if (source == null) return;
                    var random = level.getRandom();
                    float r = 0.3f - random.nextFloat() * 0.1f, g = 0.0f, b = 0.5f + random.nextFloat() * 0.2f;
                    if (source instanceof net.thaumcraft.entity.eldritch.CultistPortalEntity) {
                        r = 0.5f + random.nextFloat() * 0.2f;
                        b = 0.0f;
                    }
                    var pos = payload.pos();
                    net.thaumcraft.client.fx.Arc.spawn(random, source.getX(), source.getBoundingBox().minY + source.getBbHeight() / 2.0f,
                            source.getZ(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, r, g, b, 0.5f);
                }));
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
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.GolemRenderer.DAMAGE,
                net.thaumcraft.client.render.GolemModel::createBodyLayer);
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.GolemRenderer.ACCESSORIES,
                net.thaumcraft.client.render.GolemAccessoriesModel::createLayer);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.GOLEM_BOBBER,
                net.thaumcraft.client.render.GolemBobberRenderer::new);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.DART,
                net.thaumcraft.client.render.DartRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.GOLEM,
                net.thaumcraft.client.gui.GolemScreen::new);
        // o baú itinerante
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(
                net.thaumcraft.client.render.TrunkRenderer.LAYER, net.thaumcraft.client.render.TrunkRenderer.Model::createLayer);
        net.minecraft.client.renderer.entity.EntityRenderers.register(net.thaumcraft.registry.TCEntities.TRAVELING_TRUNK,
                net.thaumcraft.client.render.TrunkRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.TRUNK,
                net.thaumcraft.client.gui.TrunkScreen::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("traveling_trunk"), net.thaumcraft.client.render.TrunkRenderer.Unbaked.CODEC);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.FLUX_SCRUBBER, net.thaumcraft.client.render.FluxScrubberRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("flux_scrubber"), net.thaumcraft.client.render.FluxScrubberRenderer.Unbaked.CODEC);
        // o anel eldritch: o altar e o capstone (a mesma peça, texturas diferentes) e o obelisco
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ELDRITCH_ALTAR, net.thaumcraft.client.render.EldritchCapRenderer.altar());
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ELDRITCH_CAP, net.thaumcraft.client.render.EldritchCapRenderer.cap());
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ELDRITCH_OBELISK, net.thaumcraft.client.render.EldritchObeliskRenderer::new);
        net.thaumcraft.block.entity.eldritch.EldritchObeliskBlockEntity.ObeliskFx.client = (level, x, y, z, target) ->
                net.thaumcraft.client.fx.Wisp.fx4(x, y, z, target, 5, true, 1.0f);
        // o estandarte, no mundo e na mão
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.BANNER, net.thaumcraft.client.render.BannerRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("banner"), net.thaumcraft.client.render.BannerRenderer.Unbaked.CODEC);
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
        // a fornalha alquímica avançada (o modelo inteiro sai do meio) e o reservatório de essência
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE, net.thaumcraft.client.render.AdvancedAlchemicalFurnaceRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ESSENTIA_RESERVOIR, net.thaumcraft.client.render.EssentiaReservoirRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.THAUMATORIUM, net.thaumcraft.client.render.ThaumatoriumRenderer::new);
        net.thaumcraft.block.AdvancedAlchemicalFurnaceBlock.clientEffects = (level, x, y, z, size) ->
                net.thaumcraft.client.fx.SlimyBubble.spawn(x, y, z, size, net.thaumcraft.client.fx.SlimyBubble.purple(level.getRandom()), level.getRandom());
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
        // os fluidos (o purificante e a morte líquida), as bolhas deles e a tela do spa arcano
        net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry.register(net.thaumcraft.registry.TCFluids.PURIFYING,
                net.thaumcraft.registry.TCFluids.PURIFYING_FLOWING, new net.minecraft.client.renderer.block.FluidModel.Unbaked(
                        new net.minecraft.client.resources.model.sprite.Material(Thaumcraft.id("block/fluidpure"), true),
                        new net.minecraft.client.resources.model.sprite.Material(Thaumcraft.id("block/fluidpure"), true), null, null));
        net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry.register(net.thaumcraft.registry.TCFluids.DEATH,
                net.thaumcraft.registry.TCFluids.DEATH_FLOWING, new net.minecraft.client.renderer.block.FluidModel.Unbaked(
                        new net.minecraft.client.resources.model.sprite.Material(Thaumcraft.id("block/fluiddeath"), true),
                        new net.minecraft.client.resources.model.sprite.Material(Thaumcraft.id("block/fluiddeath"), true), null, null));
        net.thaumcraft.fluid.ThaumFluid.clientEffects = new net.thaumcraft.fluid.ThaumFluid.ClientEffects() {
            @Override
            public void purifying(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, int meta, net.minecraft.util.RandomSource random) {
                net.thaumcraft.client.fx.Bubble.spawn(pos.getX() + random.nextFloat(), pos.getY() + 0.125f * (8 - meta),
                        pos.getZ() + random.nextFloat(), 1.0f, 1.0f, 1.0f, 0.25f, 0, random);
            }

            @Override
            public void death(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, int meta, net.minecraft.util.RandomSource random) {
                float h = random.nextFloat() * 0.075f;
                int red = (int) ((0.3f - random.nextFloat() * 0.1f) * 255), blue = (int) ((0.4f + random.nextFloat() * 0.1f) * 255);
                net.thaumcraft.client.fx.SlimyBubble.spawn(pos.getX() + random.nextFloat(), pos.getY() + 0.1f + 0.225f * meta,
                        pos.getZ() + random.nextFloat(), 0.075f + h, 0xCC000000 | red << 16 | blue, random);
            }
        };
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.ARCANE_SPA,
                net.thaumcraft.client.gui.ArcaneSpaScreen::new);
        // os jarros especiais: o cérebro na salmoura e o nó preso (o desenhista do nó, um tanto mais baixo)
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.BRAIN_JAR, net.thaumcraft.client.render.SpecialJarRenderers.Brain::new);
        registerNodeJar();
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("special_jar"), net.thaumcraft.client.render.SpecialJarRenderers.Unbaked.CODEC);
        net.thaumcraft.block.NodeJarBlock.clientEffects = pos -> {
            for (int yy = -1; yy < 3; yy++) for (int xx = -1; xx < 2; xx++) for (int zz = -1; zz < 2; zz++) {
                net.thaumcraft.client.fx.GenericFx.blockSparkle(pos.getX() + xx, pos.getY() + yy, pos.getZ() + zz, -9999, 5);
            }
        };
        // o pedestal de recarga: a varinha girando em cima e a linha até o nó de que bebe
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.WAND_PEDESTAL, net.thaumcraft.client.render.WandPedestalRenderer::new);
        // a broca arcana: a broca e a base com os modelos do original, a tela, o facho e as migalhas
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ARCANE_BORE, net.thaumcraft.client.render.ArcaneBoreRenderers.Bore::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.ARCANE_BORE_BASE, net.thaumcraft.client.render.ArcaneBoreRenderers.Base::new);
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("arcane_bore"), net.thaumcraft.client.render.ArcaneBoreRenderers.Unbaked.CODEC);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.ARCANE_BORE,
                net.thaumcraft.client.gui.ArcaneBoreScreen::new);
        net.thaumcraft.block.entity.ArcaneBoreBlockEntity.clientEffects = new net.thaumcraft.block.entity.ArcaneBoreBlockEntity.ClientEffects() {
            @Override
            public Object beam(net.minecraft.world.level.Level level, double px, double py, double pz, double tx, double ty, double tz,
                               int type, int colour, boolean reverse, float endMod, Object old, int impact) {
                return net.thaumcraft.client.fx.BoreFx.beam(level, px, py, pz, tx, ty, tz, type, colour, reverse, endMod, old, impact);
            }

            @Override
            public void digFx(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos from, net.minecraft.core.BlockPos to,
                              net.minecraft.world.level.block.state.BlockState state) {
                net.thaumcraft.client.fx.BoreFx.dig(level, from, to, state);
            }
        };
        // o manipulador focal: a mesa com o foco girando em cima, a tela e as estrelinhas de quando trabalha
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                net.thaumcraft.registry.TCBlockEntities.FOCAL_MANIPULATOR, net.thaumcraft.client.render.FocalManipulatorRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.FOCAL_MANIPULATOR,
                net.thaumcraft.client.gui.FocalManipulatorScreen::new);
        net.thaumcraft.block.entity.FocalManipulatorBlockEntity.clientEffects = (level, pos) -> {
            var r = level.getRandom();
            net.thaumcraft.client.fx.ThaumFx.add(new net.thaumcraft.client.fx.GenericFx(
                    pos.getX() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.3f, pos.getY() + 1.25 + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    pos.getZ() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.3f, 0.0, 0.0, 0.0, 0.5f + r.nextFloat() * 0.4f,
                    1.0f - r.nextFloat() * 0.4f, 1.0f - r.nextFloat() * 0.4f, 0.8f, false, 112, 9, 1, 6 + r.nextInt(5), 0,
                    0.7f + r.nextFloat() * 0.4f));
        };
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
        SpecialModelRenderers.ID_MAPPER.put(Thaumcraft.id("essentia_reservoir"), net.thaumcraft.client.render.EssentiaReservoirRenderer.Unbaked.CODEC);
        net.thaumcraft.client.render.AspectTint.register();
        net.thaumcraft.client.render.SinisterActive.register();
        // a tecla de trocar foco, com o menu radial, e a tela da bolsa de focos
        net.thaumcraft.client.FocusRadial.init();
        // a Pressa nas botas: o empurrão é do lado de quem anda
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player != null && !minecraft.isPaused()) net.thaumcraft.event.Enchantments.haste(minecraft.player);
        });
        // o arquiteto: a tecla G e a prévia da área
        net.thaumcraft.client.ArchitectKey.init();
        net.thaumcraft.client.render.ArchitectOverlay.init();
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
        net.minecraft.client.gui.screens.MenuScreens.register(net.thaumcraft.registry.TCMenus.THAUMATORIUM,
                net.thaumcraft.client.gui.ThaumatoriumScreen::new);
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
        // a mácula vem cinza na textura, como no original: o solo e as fibras pegam a cor do capim do lugar (a média
        // dos nove em volta, que no bioma maculado é 0x6D40C9)
        net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register(
                java.util.List.of(net.minecraft.client.color.block.BlockTintSources.grass()),
                net.thaumcraft.registry.TCBlocks.TAINT_SOIL,
                net.thaumcraft.registry.TCBlocks.TAINT_FIBRES);
        net.thaumcraft.client.render.TaintFibreModel.init();
        TaintClient.init();
        CrucibleClient.init();
        net.thaumcraft.client.render.FluxModel.init();
        // as bolhas da gosma de fluxo: o FXBubble na cor de sempre (rosa), quase transparente, na altura da gosma
        net.thaumcraft.block.FluxGooBlock.clientEffects = (level, pos, meta) -> {
            var random = level.getRandom();
            net.thaumcraft.client.fx.Bubble.spawn(pos.getX() + random.nextFloat(), pos.getY() + 0.125f * meta, pos.getZ() + random.nextFloat(),
                    1.0f, 0.0f, 0.5f, 0.25f, 0, random);
        };
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                net.thaumcraft.registry.TCEntities.FALLING_TAINT, net.thaumcraft.client.render.FallingTaintRenderer::new);
        net.thaumcraft.entity.FallingTaintEntity.landEffect = net.thaumcraft.client.fx.TaintFx::land;
        net.thaumcraft.block.TaintBlock.clientDrip = (level, x, y, z) -> {
            if (level instanceof net.minecraft.client.multiplayer.ClientLevel client) net.thaumcraft.client.fx.TaintFx.droplet(client, x, y, z, 0.3f, 0.1f, 0.8f);
        };
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
        // a broca arcana: o bloco da vez e o som do bloco que saiu
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.BoreDig.TYPE, (payload, context) -> context.client().execute(() -> {
                    var level = context.client().level;
                    if (level != null && level.getBlockEntity(payload.pos()) instanceof net.thaumcraft.block.entity.ArcaneBoreBlockEntity bore) {
                        bore.boreEvent(payload.id(), payload.param());
                    }
                }));
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
        // a matriz de infusão: as runas do pedestal, as migalhas que ela puxa, a experiência e os raios da instabilidade
        net.thaumcraft.block.entity.InfusionMatrixBlockEntity.clientEffects = new net.thaumcraft.block.entity.InfusionMatrixBlockEntity.ClientEffects() {
            @Override
            public void runes(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pedestal, float r, float g, float b) {
                net.thaumcraft.client.fx.BlockRunes.spawn(pedestal.getX(), pedestal.getY(), pedestal.getZ(), r, g, b, 25, -0.03f);
            }

            @Override
            public void pedestal(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pedestal, net.minecraft.core.BlockPos matrix,
                                 net.minecraft.world.item.ItemStack stack) {
                if (level instanceof net.minecraft.client.multiplayer.ClientLevel client) {
                    net.thaumcraft.client.fx.InfusionFx.fromPedestal(client, pedestal, matrix, stack);
                }
            }

            @Override
            public void experience(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity from, net.minecraft.core.BlockPos matrix) {
                var random = level.getRandom();
                for (int a = 0; a < 4; a++) {
                    net.thaumcraft.client.fx.InfusionFx.experience(from.getX() + (random.nextFloat() - random.nextFloat()) * from.getBbWidth(),
                            from.getBoundingBox().minY + random.nextFloat() * from.getBbHeight(),
                            from.getZ() + (random.nextFloat() - random.nextFloat()) * from.getBbWidth(), matrix, random);
                }
            }

            @Override
            public void bolt(net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) {
                NodeClient.nodeBolt(from, to);
            }
        };
        net.thaumcraft.block.PedestalBlock.clientEffects = (pos, colour, count) ->
                net.thaumcraft.client.fx.GenericFx.blockSparkle(pos.getX(), pos.getY(), pos.getZ(), colour, count);
        // os avisos do canto: o PacketAspectDiscovery, o PacketAspectPool e o PacketWarpMessage
        PlayerNotifications.init();
        // as runas do thaumômetro enquanto ele lê (o blockRunes do doScan)
        net.thaumcraft.item.ThaumometerItem.runes = (level, target) -> net.thaumcraft.client.fx.BlockRunes.spawn(
                target.x(), target.y(), target.z(), 0.3f + level.getRandom().nextFloat() * 0.7f, 0.0f,
                0.3f + level.getRandom().nextFloat() * 0.7f, target.runes(), 0.03f);
        // a distorção na tela: a vinheta, a névoa e os filtros das poções; e as bolhas do sabão
        WarpClient.init();
        // as ferramentas mágicas: faíscas, bolhas, a varredura de minérios da picareta e o redemoinho da espada
        net.thaumcraft.item.ToolFx.client = new net.thaumcraft.item.ToolFx.Client() {
            @Override
            public void sparkle(net.minecraft.core.BlockPos pos, int colour, int count) {
                net.thaumcraft.client.fx.GenericFx.blockSparkle(pos.getX(), pos.getY(), pos.getZ(), colour, count);
            }

            @Override
            public void bubble(net.minecraft.world.level.Level level, double x, double y, double z, float r, float g, float b) {
                net.thaumcraft.client.fx.Bubble.spawn(x, y, z, r, g, b, 1.0f, 1, level.getRandom());
            }

            @Override
            public void oreScan(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
                net.thaumcraft.client.fx.OreScan.start(level, pos);
            }

            @Override
            public void smokeSpiral(net.minecraft.world.level.Level level, double x, double y, double z, float radius, int start, int miny, int colour) {
                net.thaumcraft.client.fx.SmokeSpiral.spawn(x, y, z, radius, start, miny, colour, level.getRandom());
            }
        };
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.BlockBubble.TYPE, (payload, context) -> context.client().execute(() -> {
                    var level = context.client().level;
                    if (level == null) return;
                    var random = level.getRandom();
                    float r = (payload.colour() >> 16 & 255) / 255.0f, g = (payload.colour() >> 8 & 255) / 255.0f, b = (payload.colour() & 255) / 255.0f;
                    var p = payload.pos();
                    for (int a = 0; a < 2; a++) {
                        net.thaumcraft.client.fx.Bubble.spawn(p.getX(), p.getY() + random.nextFloat(), p.getZ() + random.nextFloat(), r, g, b, 1.0f, 1, random);
                        net.thaumcraft.client.fx.Bubble.spawn(p.getX() + 1, p.getY() + random.nextFloat(), p.getZ() + random.nextFloat(), r, g, b, 1.0f, 1, random);
                        net.thaumcraft.client.fx.Bubble.spawn(p.getX() + random.nextFloat(), p.getY() + random.nextFloat(), p.getZ(), r, g, b, 1.0f, 1, random);
                        net.thaumcraft.client.fx.Bubble.spawn(p.getX() + random.nextFloat(), p.getY() + random.nextFloat(), p.getZ() + 1, r, g, b, 1.0f, 1, random);
                    }
                }));
        net.thaumcraft.item.SanitySoapItem.clientEffects = (level, x, y, z, r, g, b) ->
                net.thaumcraft.client.fx.Bubble.spawn(x, y, z, r, g, b, 1.0f, 1, level.getRandom());
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.AspectDiscovery.TYPE, (payload, context) -> context.client().execute(() -> {
                    var aspect = net.thaumcraft.api.aspects.Aspect.of(payload.tag());
                    var player = context.client().player;
                    if (aspect == null || player == null) return;
                    String text = net.minecraft.network.chat.Component.translatable("tc.addaspectdiscovery", aspect.name()).getString();
                    PlayerNotifications.add("§6" + text, aspect);
                    player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2f, 0.5f + player.getRandom().nextFloat() * 0.2f);
                }));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.Notice.TYPE, (payload, context) -> context.client().execute(() -> {
                    var text = payload.arg().isEmpty()
                            ? net.minecraft.network.chat.Component.translatable(payload.key())
                            : net.minecraft.network.chat.Component.translatable(payload.key(), net.minecraft.network.chat.Component.translatable(payload.arg()));
                    PlayerNotifications.add(text.getString());
                }));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.AspectPool.TYPE, (payload, context) -> context.client().execute(() -> {
                    var aspect = net.thaumcraft.api.aspects.Aspect.of(payload.tag());
                    var player = context.client().player;
                    if (aspect == null || player == null || payload.amount() <= 0) return;
                    String text = net.minecraft.network.chat.Component.translatable("tc.addaspectpool", payload.amount(), aspect.name()).getString();
                    PlayerNotifications.add(text, aspect);
                    for (int a = 0; a < payload.amount(); a++) PlayerNotifications.addAspect(aspect);
                    if (System.currentTimeMillis() > lastPoolSound) {
                        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1f, 0.9f + player.getRandom().nextFloat() * 0.2f);
                        lastPoolSound = System.currentTimeMillis() + 100L;
                    }
                }));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.WarpMessage.TYPE, (payload, context) -> context.client().execute(() -> {
                    var player = context.client().player;
                    int data = payload.amount();
                    if (player == null || data == 0) return;
                    if (payload.kind() == net.thaumcraft.research.Warp.PERMANENT && data > 0) {
                        player.playSound(net.thaumcraft.registry.TCSounds.WHISPERS.value(), 0.5f, 1.0f);
                        PlayerNotifications.add(net.minecraft.network.chat.Component.translatable("tc.addwarp").getString());
                    } else if (payload.kind() == net.thaumcraft.research.Warp.STICKY) {
                        if (data > 0) player.playSound(net.thaumcraft.registry.TCSounds.WHISPERS.value(), 0.5f, 1.0f);
                        PlayerNotifications.add(net.minecraft.network.chat.Component.translatable(
                                data < 0 ? "tc.removewarpsticky" : "tc.addwarpsticky").getString());
                    } else if (payload.kind() == net.thaumcraft.research.Warp.TEMPORARY && data > 0) {
                        PlayerNotifications.add(net.minecraft.network.chat.Component.translatable("tc.addwarptemp").getString());
                    }
                }));
        // a essência vindo pelo ar (o sourceFX do EssentiaHandler) e o que a matriz de infusão puxa (o dela)
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.EssentiaSource.TYPE, (payload, context) -> context.client().execute(() ->
                        net.thaumcraft.client.fx.EssentiaTrail.source(payload.pos(), payload.source(), payload.colour())));
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK.register(
                net.thaumcraft.client.fx.EssentiaTrail::clientTick);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                net.thaumcraft.net.TCNetwork.InfusionSource.TYPE, (payload, context) -> context.client().execute(() -> {
                    var level = context.client().level;
                    if (level != null && level.getBlockEntity(payload.pos()) instanceof net.thaumcraft.block.entity.InfusionMatrixBlockEntity matrix) {
                        matrix.addSourceFx(payload.dx(), payload.dy(), payload.dz(), payload.entity());
                    }
                }));
    }

    /** O nó no jarro usa o desenhista do nó: a peça dele é um nó de outro tipo. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerNodeJar() {
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                (net.minecraft.world.level.block.entity.BlockEntityType) net.thaumcraft.registry.TCBlockEntities.NODE_JAR,
                (net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider) net.thaumcraft.client.render.NodeRenderer::new);
    }
}
