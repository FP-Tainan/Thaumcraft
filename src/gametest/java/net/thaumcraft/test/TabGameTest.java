package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.thaumcraft.Thaumcraft;

/**
 * A aba do criativo tem de mostrar tudo o que o mod registra — nada pode ficar escondido lá.
 */
public class TabGameTest {
    @GameTest
    public void theTabShowsEverythingTheModRegisters(GameTestHelper helper) {
        var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(net.thaumcraft.registry.TCItems.TAB_KEY);
        if (tab == null) helper.fail("faltou a aba do mod");

        // tudo o que o mod registrou como item de mão; o que é peça de dentro do maquinário não conta
        int ours = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !id.getNamespace().equals(Thaumcraft.MOD_ID)) continue;
            if (net.thaumcraft.registry.TCItems.HIDDEN.contains(item)) continue;
            ours++;
        }
        if (ours < 60) helper.fail("o mod devia ter mais itens que isso: " + ours);

        // e o que está escondido tem de estar escondido de verdade
        var shelf = net.thaumcraft.registry.TCItems.displayOrder();
        for (Item item : net.thaumcraft.registry.TCItems.HIDDEN) {
            if (shelf.contains(item)) {
                helper.fail("peça de dentro na aba do criativo: "
                        + BuiltInRegistries.ITEM.getKey(item));
            }
        }

        // a lista da aba, montada à parte: o jogo só a constrói de fato quando abre a tela
        var shown = net.thaumcraft.registry.TCItems.displayOrder();
        if (shown.size() < ours) {
            helper.fail("a aba mostra " + shown.size() + " de " + ours + " itens do mod; algum ficou de fora");
        }
        // e a prateleira não pode citar nada que não exista
        for (Item item : shown) {
            if (BuiltInRegistries.ITEM.getKey(item) == null) helper.fail("item sem nome na aba");
        }
        helper.succeed();
    }
}
