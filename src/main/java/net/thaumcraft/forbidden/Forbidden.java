package net.thaumcraft.forbidden;

import net.thaumcraft.Thaumcraft;

/**
 * O Forbidden Magic 0.575, de SpitefulFox — o terceiro ramo de fora.
 *
 * <p>Na lore de quem joga ele fica com o próprio nome: magia humana proibida, e não coisa dos Ancestrais. São os
 * sete pecados, o Nether e o que os thaumaturgos inventaram de pior — técnicas que funcionam dentro das regras da
 * magia, mas por caminhos cruéis.
 *
 * <p>Como o Maleficium e o Magia Naturalis, ele mora no mesmo jar do Thaumcraft, com figuras e textos no espaço de
 * nome {@code thaumcraft} e aba própria no criativo e no Thaumonomicon. As chaves de pesquisa levam o prefixo
 * {@code FM_}, que é a tradução do espaço de nome do original.
 */
public final class Forbidden {
    /** A aba do ramo no Thaumonomicon, com o nome que o original lhe dava. */
    public static final String CATEGORY = "FORBIDDEN";

    private Forbidden() {
    }

    public static void init() {
        ForbiddenAspects.init();
        ForbiddenItems.init();
        events();
        Thaumcraft.LOGGER.info("Forbidden Magic: {} aspectos sombrios, {} coisas",
                ForbiddenAspects.ASPECTS.size(), ForbiddenItems.count());
    }

    /** O {@code FMEventHandler}: de onde vêm os fragmentos dos pecados. */
    private static void events() {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(ForbiddenDrops::onDeath);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof net.minecraft.world.entity.LivingEntity living) ForbiddenDrops.onSpawn(living, level);
        });
        // quem come é o LivingEntityEatMixin, porque o jogo não anuncia isso por evento
    }
}
