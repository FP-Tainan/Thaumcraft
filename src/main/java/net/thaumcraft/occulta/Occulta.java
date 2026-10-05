package net.thaumcraft.occulta;

import net.thaumcraft.Thaumcraft;

/**
 * O Ars Occulta — o Witchery 0.24.1, de Emoniph, com o nome que a lore de quem joga lhe dá.
 *
 * <p>É o ofício das bruxas: as plantas que se criam em terra e em água, o caldeirão, o altar que junta poder da
 * natureza em volta, os rituais desenhados no chão e os espíritos com que se fala. Como os outros ramos, mora no
 * mesmo jar do Thaumcraft, com figuras e textos no espaço de nome {@code thaumcraft}, aba própria no criativo e
 * chaves de pesquisa com o prefixo {@code AO_}.
 *
 * <p><b>Por onde vai:</b> estão feitas as plantas, o forno, as árvores, o caldeirão e o altar. As poções, os
 * rituais de círculo, os bichos e o resto vêm depois, na ordem que o {@code docs/PORTE.md} marca.
 */
public final class Occulta {
    /** A aba do ramo no Thaumonomicon. */
    public static final String CATEGORY = "OCCULTA";

    private Occulta() {
    }

    public static void init() {
        OccultaComponents.init();
        OccultaSounds.init();
        net.thaumcraft.occulta.vampire.BloodedWool.init();
        WickerBundleBlock.init();
        net.thaumcraft.occulta.spirit.SpiritFluids.init();
        OccultaEffects.init();
        OccultaBlocks.init();
        OccultaEntities.init();
        net.thaumcraft.occulta.village.VillagePieces.init();
        net.thaumcraft.occulta.familiar.FamiliarEvents.init();
        net.thaumcraft.occulta.broom.Brooms.init();
        net.thaumcraft.occulta.curse.Curse.init();
        net.thaumcraft.occulta.curse.Grotesque.init();
        net.thaumcraft.occulta.enslave.Enslavement.init();
        NoDrops.init();
        ExtraDrops.init();
        net.thaumcraft.occulta.divine.Predictions.init();
        net.thaumcraft.occulta.infusion.Infusions.init();
        net.thaumcraft.occulta.infusion.beast.CreaturePowers.init();
        net.thaumcraft.occulta.symbol.Symbols.init();
        net.thaumcraft.occulta.symbol.Spells.init();
        net.thaumcraft.occulta.symbol.SpellNetwork.init();
        net.thaumcraft.occulta.door.KeyringRecipe.init();
        net.thaumcraft.occulta.waystone.Waystones.init();
        net.thaumcraft.occulta.hunter.WitchHunters.init();
        net.thaumcraft.occulta.wolf.Werewolf.init();
        net.thaumcraft.occulta.wolf.WerewolfQuest.init();
        net.thaumcraft.occulta.wolf.WerewolfPowers.init();
        net.thaumcraft.occulta.wolf.WerewolfTick.init();
        net.thaumcraft.occulta.wolf.WerewolfHooks.init();
        net.thaumcraft.occulta.vampire.Vampire.init();
        net.thaumcraft.occulta.vampire.Blood.init();
        net.thaumcraft.occulta.vampire.VampirePowers.init();
        net.thaumcraft.occulta.vampire.VampireLadder.init();
        net.thaumcraft.occulta.vampire.VampireBookRecipe.init();
        net.thaumcraft.occulta.vampire.VampireTick.init();
        net.thaumcraft.occulta.vampire.VampireHooks.init();
        OccultaItems.init();
        OccultaAspects.init();
        OccultaGrassSeeds.init();
        OccultaDrops.init();
        OccultaEvents.init();
        Poppets.init();
        net.thaumcraft.occulta.mirror.MirrorTravel.init();
        net.thaumcraft.occulta.rite.Rites.register();
        net.thaumcraft.occulta.kettle.KettleTable.register();
        net.thaumcraft.occulta.spinning.SpinningRecipes.register();
        net.thaumcraft.occulta.brazier.BrazierRecipes.register();
        net.thaumcraft.occulta.spirit.SpiritPlants.init();
        net.thaumcraft.occulta.spirit.SpiritManifest.init();
        net.thaumcraft.occulta.spirit.SpiritDeath.init();
        // a aba do ramo no livro
        net.thaumcraft.api.ThaumcraftApi.category(CATEGORY,
                Thaumcraft.id("textures/item/mandrake_root.png"),
                Thaumcraft.id("textures/gui/gui_occulta_researchback.png"));
        OccultaTable.research();
        // as receitas do livro pedem itens prontos, e por isso esperam a montagem acabar
        net.thaumcraft.api.ThaumcraftApi.onSetup(OccultaTable::recipes);
        Thaumcraft.LOGGER.info("Ars Occulta: {} coisas", OccultaItems.count());
    }
}
