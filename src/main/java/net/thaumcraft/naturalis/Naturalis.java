package net.thaumcraft.naturalis;

import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.ThaumcraftApi;

/**
 * O <i>Magia Naturalis</i>: o mod de elenterius, versão 0.5.0, dentro do Thaumcraft.
 *
 * <p>Na lore de quem joga ele é a Thaumaturgia Aplicada — o caminho que amplia a thaumaturgia de sempre sem
 * apodrecer: construção, ambiente, guarda de coisas, ferramentas e transcrição. O nome é o que a lore lhe dá, e é
 * o mesmo do original.
 *
 * <p>Como o Maleficium, entra pela porta do {@link ThaumcraftApi} e mora no mesmo jar, com aba própria no criativo
 * e no Thaumonomicon.
 */
public final class Naturalis {
    /** A aba do ramo no livro. */
    public static final String CATEGORY = "NATURALIS";

    private Naturalis() {
    }

    public static void init() {
        NaturalisBlocks.init();
        NaturalisEntities.init();
        NaturalisItems.init();
        BuilderFocus.init();
        RevenantFocus.init();
        NaturalisKeys.init();
        // o WorldEventHandler do original: creeper foge do Baú Maligno
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                ((net.thaumcraft.mixin.MobGoalAccessor) creeper).thaumcraft$goalSelector().addGoal(3,
                        new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(
                                creeper, EvilTrunkEntity.class, 6.0f, 1.0, 1.2));
            }
        });
        // o "championWhiteList" que o original mandava ao Thaumcraft: taint_breeder com bônus um
        net.thaumcraft.event.Champions.whitelist(e -> e instanceof TaintBreederEntity, 1);
        // o Criador de Mácula nasce na terra maculada, como o addSpawn do original o punha
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.thaumcraft.world.TCBiomes.TAINTED_LAND),
                net.minecraft.world.entity.MobCategory.MONSTER, NaturalisEntities.TAINT_BREEDER, 30, 1, 1);
        research();
        Thaumcraft.LOGGER.info("Magia Naturalis: {} coisas", NaturalisItems.count());
    }

    private static void research() {
        ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/item/research_log.png"),
                Thaumcraft.id("textures/gui/gui_naturalis_researchback.png"));

        NaturalisTable.research();
        // as receitas carregam itens, então só se montam quando o mundo abre
        ThaumcraftApi.onSetup(NaturalisTable::recipes);
        NaturalisTable.mutations();
    }
}
