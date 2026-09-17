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

        // tudo o que o mod registrou como item
        int ours = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && id.getNamespace().equals(Thaumcraft.MOD_ID)) ours++;
        }
        if (ours < 60) helper.fail("o mod devia ter mais itens que isso: " + ours);

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
