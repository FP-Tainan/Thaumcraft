package net.thaumcraft.occulta.client;

import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.thaumcraft.occulta.curse.IllusionEntity;

/**
 * As três visões, desenhadas <b>exatamente como os bichos que elas fingem ser</b>.
 *
 * <p>Isto é o ponto delas, e é por isso que os desenhistas usam o modelo e a pele <b>do próprio jogo</b>, sem
 * um pixel de diferença: quem está amaldiçoado tem de ver um creeper e acreditar que é um creeper. Uma visão
 * que se distinguisse de longe não seria visão nenhuma.
 *
 * <p>No original são três classes de bicho, cada uma com o seu desenhista apontado ao modelo do jogo. Aqui são
 * as mesmas três, pelo mesmo caminho.
 */
public final class IllusionRenderers {
    private static final Identifier PELE_CREEPER =
            Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png");
    private static final Identifier PELE_ARANHA =
            Identifier.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final Identifier PELE_ZUMBI =
            Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

    private IllusionRenderers() {
    }

    /** A visão de creeper: o modelo do jogo, sem o piscar de quem vai estourar — porque ela não estoura. */
    public static class Creeper
            extends MobRenderer<IllusionEntity, CreeperRenderState, CreeperModel> {
        public Creeper(EntityRendererProvider.Context contexto) {
            super(contexto, new CreeperModel(contexto.bakeLayer(ModelLayers.CREEPER)), 0.5f);
        }

        @Override
        public CreeperRenderState createRenderState() {
            return new CreeperRenderState();
        }

        @Override
        public Identifier getTextureLocation(CreeperRenderState estado) {
            return PELE_CREEPER;
        }
    }

    /** A de aranha. */
    public static class Spider
            extends MobRenderer<IllusionEntity, LivingEntityRenderState, SpiderModel> {
        public Spider(EntityRendererProvider.Context contexto) {
            super(contexto, new SpiderModel(contexto.bakeLayer(ModelLayers.SPIDER)), 0.8f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState estado) {
            return PELE_ARANHA;
        }
    }

    /** E a de zumbi. */
    public static class Zombie
            extends MobRenderer<IllusionEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {
        public Zombie(EntityRendererProvider.Context contexto) {
            super(contexto, new ZombieModel<>(contexto.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        }

        @Override
        public ZombieRenderState createRenderState() {
            return new ZombieRenderState();
        }

        @Override
        public Identifier getTextureLocation(ZombieRenderState estado) {
            return PELE_ZUMBI;
        }
    }
}
