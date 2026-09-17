package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.registry.TCItems;

/**
 * Os focos de varinha cobram o que o Thaumcraft 4.2.3.5 cobra.
 *
 * <p>Cada custo aqui foi lido no {@code getVisCost} da classe do foco no original, sem melhoria nenhuma
 * aplicada — é o {@code COST_BASE} de cada um. Se alguém mexer num custo sem mexer no original junto,
 * esta prova quebra a compilação.
 */
public class FocusGameTest {
    @GameTest
    public void eachFocusChargesWhatTheOriginalCharges(GameTestHelper helper) {
        // FocusFire: new AspectList().add(Aspect.FIRE, 10)
        check(helper, "fire", true, new AspectList().add(Aspects.FIRE, 10));
        // FocusExcavation: new AspectList().add(Aspect.EARTH, 15)
        check(helper, "excavation", true, new AspectList().add(Aspects.EARTH, 15));
        // FocusFrost: new AspectList().add(Aspect.WATER, 5).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2)
        check(helper, "frost", false, new AspectList()
                .add(Aspects.WATER, 5).add(Aspects.FIRE, 2).add(Aspects.ENTROPY, 2));
        // FocusShock: new AspectList().add(Aspect.AIR, 25)
        check(helper, "shock", true, new AspectList().add(Aspects.AIR, 25));
        helper.succeed();
    }

    /** Todo foco registrado tem nome, cara e custo — nenhum pode entrar pela metade. */
    @GameTest
    public void noFocusIsHalfDone(GameTestHelper helper) {
        if (TCItems.FOCI.isEmpty()) helper.fail("nenhum foco registrado");
        for (var entry : TCItems.FOCI.entrySet()) {
            Item item = entry.getValue();
            if (!(item instanceof FocusItem focus)) {
                helper.fail("o foco " + entry.getKey() + " não é um foco");
                continue;
            }
            if (!focus.type().equals(entry.getKey())) {
                helper.fail("o foco " + entry.getKey() + " se chama " + focus.type());
            }
            if (focus.cost().isEmpty()) helper.fail("o foco " + entry.getKey() + " não cobra nada");
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, String type, boolean continuous, AspectList expected) {
        Item item = TCItems.FOCI.get(type);
        if (!(item instanceof FocusItem focus)) {
            helper.fail("falta o foco de " + type);
            return;
        }
        AspectList cost = focus.cost();
        for (Aspect aspect : expected.getAspects()) {
            if (cost.getAmount(aspect) != expected.getAmount(aspect)) {
                helper.fail("o foco de " + type + " devia cobrar " + expected.getAmount(aspect)
                        + " de " + aspect.tag() + ", cobra " + cost.getAmount(aspect));
            }
        }
        if (cost.getAspects().size() != expected.getAspects().size()) {
            helper.fail("o foco de " + type + " cobra aspectos a mais ou a menos que o original");
        }
        if (focus.isContinuous() != continuous) {
            helper.fail("o foco de " + type + (continuous ? " é jato contínuo" : " é tiro único"));
        }
    }
}
