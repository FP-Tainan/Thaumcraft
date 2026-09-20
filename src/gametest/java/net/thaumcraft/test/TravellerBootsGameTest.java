package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCItems;

/**
 * As botas do viajante: o {@code ItemBootsTraveller} da 4.2.3.5 — correm mais, sobem um bloco sem pular e amortecem a
 * queda. Aqui se conferem os dois números que o item carrega: o degrau de um bloco inteiro (o
 * {@code player.stepHeight = 1.0F} do original) e o pulo de três blocos, que é a mudança pedida por quem joga.
 *
 * <p>Não se calça nada num jogador de mentira: o jogo só junta os atributos da roupa no tique seguinte, e o de mentira
 * não chega a tê-lo. Confere-se o que o item promete, com a conta do próprio jogo.
 */
public class TravellerBootsGameTest {
    /** A queda de um jogador, tique a tique: tira oito centésimos e encolhe dois por cento, como o {@code travel}. */
    private static double alturaDoPulo(double impulso) {
        double y = 0.0, v = impulso, alto = 0.0;
        for (int tique = 0; tique < 200 && y >= 0.0; tique++) {
            y += v;
            alto = Math.max(alto, y);
            v = (v - 0.08) * 0.98;
        }
        return alto;
    }

    @GameTest
    public void theyClimbAFullBlockAndJumpThree(GameTestHelper helper) {
        var botas = new ItemStack(TCItems.TRAVELLER_BOOTS).getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        var base = Player.createAttributes().build();

        double degrau = botas.compute(Attributes.STEP_HEIGHT, base.getBaseValue(Attributes.STEP_HEIGHT), EquipmentSlot.FEET);
        if (degrau < 1.0) helper.fail("calçadas, as botas têm de subir um bloco inteiro; sobem " + degrau);

        double impulso = botas.compute(Attributes.JUMP_STRENGTH, base.getBaseValue(Attributes.JUMP_STRENGTH), EquipmentSlot.FEET);
        double altura = alturaDoPulo(impulso);
        if (altura < 3.0) helper.fail("com elas o pulo tem de passar dos três blocos; pula " + altura + " (impulso " + impulso + ")");
        helper.succeed();
    }
}
