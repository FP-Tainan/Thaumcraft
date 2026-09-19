package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.thaumcraft.client.fx.SwarmFx;
import net.thaumcraft.client.fx.TaintFx;
import net.thaumcraft.client.render.taint.TaintModels;
import net.thaumcraft.client.render.taint.TaintOddModels;
import net.thaumcraft.client.render.taint.TaintRenderers;
import net.thaumcraft.client.render.taint.TaintSpiderRenderer;
import net.thaumcraft.entity.taint.TaintSplosion;
import net.thaumcraft.entity.taint.TaintSporeEntity;
import net.thaumcraft.entity.taint.TaintSporeSwarmerEntity;
import net.thaumcraft.entity.taint.TaintSwarmEntity;
import net.thaumcraft.entity.taint.TaintacleEntity;
import net.thaumcraft.entity.taint.TaintedMonster;
import net.thaumcraft.entity.taint.ThaumicSlimeEntity;
import net.thaumcraft.registry.TCEntities;

/** O lado de quem vê da fauna da mácula: os modelos, os desenhistas e os efeitos que as criaturas pedem. */
public final class TaintClient {
    private TaintClient() {
    }

    public static void init() {
        ModelLayerRegistry.registerModelLayer(TaintRenderers.CHICKEN, TaintModels::chicken);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.COW, TaintModels::cow);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.PIG, TaintModels::pig);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SHEEP, TaintModels::sheep);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SHEEP_WOOL, TaintModels::sheepWool);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.CREEPER, TaintModels::creeper);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.VILLAGER, TaintModels::villager);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SLIME_INNER, TaintOddModels::slimeInner);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SLIME_OUTER, TaintOddModels::slimeOuter);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SPORE, TaintOddModels::spore);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SWARMER_INNER, TaintOddModels::swarmerInner);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.SWARMER_OUTER, TaintOddModels::swarmerOuter);
        ModelLayerRegistry.registerModelLayer(TaintRenderers.TAINTACLE, () -> TaintOddModels.taintacle(10));
        ModelLayerRegistry.registerModelLayer(TaintRenderers.TAINTACLE_SMALL, () -> TaintOddModels.taintacle(6));

        EntityRendererRegistry.register(TCEntities.TAINT_CHICKEN, TaintRenderers::chicken);
        EntityRendererRegistry.register(TCEntities.TAINT_COW, TaintRenderers::cow);
        EntityRendererRegistry.register(TCEntities.TAINT_PIG, TaintRenderers::pig);
        EntityRendererRegistry.register(TCEntities.TAINT_SHEEP, TaintRenderers.Sheep::new);
        EntityRendererRegistry.register(TCEntities.TAINT_CREEPER, TaintRenderers.Creeper::new);
        EntityRendererRegistry.register(TCEntities.TAINT_VILLAGER, TaintRenderers.Villager::new);
        EntityRendererRegistry.register(TCEntities.TAINT_SPIDER, TaintSpiderRenderer::new);
        EntityRendererRegistry.register(TCEntities.THAUMIC_SLIME, TaintRenderers.Slime::new);
        EntityRendererRegistry.register(TCEntities.TAINT_SPORE, TaintRenderers.Spore::new);
        EntityRendererRegistry.register(TCEntities.TAINT_SPORE_SWARMER, TaintRenderers.Swarmer::new);
        EntityRendererRegistry.register(TCEntities.TAINT_SWARM, TaintRenderers.Nothing::new);
        EntityRendererRegistry.register(TCEntities.TAINTACLE,
                context -> new TaintRenderers.Taintacle(context, TaintRenderers.TAINTACLE, 10, 0.6f));
        EntityRendererRegistry.register(TCEntities.TAINTACLE_SMALL,
                context -> new TaintRenderers.Taintacle(context, TaintRenderers.TAINTACLE_SMALL, 6, 0.2f));
        EntityRendererRegistry.register(TCEntities.BOTTLE_TAINT, ThrownItemRenderer::new);

        TaintedMonster.sploosh = TaintFx::sploosh;
        TaintSplosion.effect = TaintFx::taintsplosion;
        ThaumicSlimeEntity.jumpEffect = TaintFx::slimeJump;
        TaintSporeEntity.splooshEffect = TaintFx::sploosh;
        TaintSporeEntity.swarmEffect = e -> SwarmFx.spawn(e, 0.1f, 10.0f, 0.0f);
        TaintSporeEntity.swarmDead = fx -> !(fx instanceof SwarmFx swarm) || !swarm.alive;
        TaintSporeSwarmerEntity.killSwarm = fx -> {
            if (fx instanceof SwarmFx swarm) swarm.alive = false;
        };
        TaintSwarmEntity.swarmEffect = e -> SwarmFx.spawn(e, 0.22f, 15.0f, 0.08f);
        TaintacleEntity.ariseEffect = TaintFx::tentacleArise;
    }
}
