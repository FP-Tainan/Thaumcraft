package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.mortuorum.MortuorumItems;

/**
 * O ramo do Ars Mortuorum: as coisas que se tiram dos mortos.
 */
public class MortuorumGameTest {
    /** As cinquenta e quatro peças de corpo do original existem, e cada uma tem item. */
    @GameTest
    public void theBodyPartsAreAllThere(GameTestHelper helper) {
        if (MortuorumItems.PARTS.size() != 54) {
            helper.fail("o original tem cinquenta e quatro peças; achei " + MortuorumItems.PARTS.size());
        }
        for (var nome : MortuorumItems.PARTS.keySet()) {
            if (!MortuorumItems.PART_ITEMS.containsKey(nome)) helper.fail("falta o item de " + nome);
        }
        var cabeca = MortuorumItems.PARTS.get("wolf_head");
        if (cabeca == null || !cabeca.mob().equals("Wolf") || !cabeca.piece().equals("Head")) {
            helper.fail("do lobo só se tira a cabeça");
        }
        helper.succeed();
    }

    /** Os cinco órgãos são comida, e comê-los dá fome — como no original. */
    @GameTest
    public void theOrgansAreFood(GameTestHelper helper) {
        if (MortuorumItems.ORGANS.size() != 5) helper.fail("são cinco órgãos");
        for (var orgao : MortuorumItems.ORGANS.values()) {
            var comida = new ItemStack(orgao).get(DataComponents.FOOD);
            if (comida == null) helper.fail("órgão é comida");
            else if (comida.nutrition() != 2) helper.fail("cada um enche dois de fome");
        }
        helper.succeed();
    }
}
