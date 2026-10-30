package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.RiteRegistry;

/**
 * As <b>duas pequenas que faltavam</b>: o <b>Guisado de Carne</b> e o rito que chama o <b>Reflexo</b>.
 */
public class OccultaStewGameTest {
    /** <b>Cru ele quase não alimenta; cozido, enche a barra.</b> E os dois devolvem a tigela. */
    @GameTest(maxTicks = 20)
    public void theStewIsWorthlessRawAndWorthEverythingCooked(GameTestHelper helper) {
        var cru = new ItemStack(OccultaItems.STEW_RAW).get(DataComponents.FOOD);
        var feito = new ItemStack(OccultaItems.STEW).get(DataComponents.FOOD);
        if (cru == null || feito == null) {
            helper.fail("os dois guisados são comida");
            return;
        }
        if (cru.nutrition() != 1) helper.fail("cru ele alimenta um; alimenta " + cru.nutrition());
        if (feito.nutrition() != 12) helper.fail("e cozido alimenta doze; alimenta " + feito.nutrition());
        if (feito.nutrition() <= cru.nutrition()) helper.fail("cozinhar vale a pena");

        for (var qual : java.util.List.of(OccultaItems.STEW_RAW, OccultaItems.STEW)) {
            var sobra = new ItemStack(qual).get(DataComponents.USE_REMAINDER);
            if (sobra == null || !sobra.convertInto().is(Items.BOWL)) {
                helper.fail("e os dois devolvem a tigela");
                return;
            }
        }
        helper.succeed();
    }

    /** <b>O rito do Reflexo está lá</b>, e com o anel que o original lhe dá: o de fora, todo infernal. */
    @GameTest(maxTicks = 20)
    public void theReflectionRiteIsThereWithItsInfernalRing(GameTestHelper helper) {
        RiteRegistry.Entry qual = null;
        for (var rito : RiteRegistry.all()) {
            if (rito.key().equals("tc.rite.summonreflection")) qual = rito;
        }
        if (qual == null) {
            helper.fail("o rito que chama o Reflexo está no livro");
            return;
        }
        if (qual.inner() != RiteRegistry.Ring.NONE || qual.middle() != RiteRegistry.Ring.NONE) {
            helper.fail("ele não pede anel de dentro nem do meio");
        }
        if (qual.outer().infernal() != 40 || qual.outer().ritual() != 0
                || qual.outer().otherwhere() != 0) {
            helper.fail("e o de fora é quarenta glifos de giz infernal, e mais nada");
        }
        helper.succeed();
    }
}
