package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.forbidden.ForbiddenAspects;
import net.thaumcraft.research.EntityAspects;

/** O ramo do Forbidden Magic tem de seguir o {@code DarkAspects} do 0.575. */
public class ForbiddenGameTest {
    /** Os sete aspectos sombrios existem, com a cor, os pais e a mistura do original. */
    @GameTest
    public void theSevenDarkAspectsExist(GameTestHelper helper) {
        String[][] esperados = {
                {"infernus", "FIRE", "MAGIC"}, {"ira", "WEAPON", "FIRE"}, {"gula", "HUNGER", "VOID"},
                {"invidia", "SENSES", "HUNGER"}, {"superbia", "FLIGHT", "VOID"}, {"desidia", "TRAP", "SOUL"},
                {"luxuria", "FLESH", "HUNGER"}};
        if (ForbiddenAspects.ASPECTS.size() != esperados.length) {
            helper.fail("o original tem sete aspectos sombrios; há " + ForbiddenAspects.ASPECTS.size());
        }
        for (String[] esperado : esperados) {
            Aspect aspecto = ForbiddenAspects.ASPECTS.get(esperado[0]);
            if (aspecto == null) {
                helper.fail("falta o aspecto " + esperado[0]);
                return;
            }
            if (Aspect.ASPECTS.get(esperado[0]) != aspecto) helper.fail(esperado[0] + " devia estar na tabela do mod");
            if (aspecto.components() == null || aspecto.components().length != 2) {
                helper.fail(esperado[0] + " nasce de dois aspectos");
                return;
            }
            if (aspecto.components()[0] != byName(esperado[1]) || aspecto.components()[1] != byName(esperado[2])) {
                helper.fail(esperado[0] + " nasce de " + esperado[1] + " e " + esperado[2]);
            }
        }
        helper.succeed();
    }

    /** E eles somam ao que as coisas e as criaturas já tinham, sem apagar nada. */
    @GameTest
    public void theDarkAspectsAddToWhatWasThere(GameTestHelper helper) {
        Aspect infernus = ForbiddenAspects.ASPECTS.get("infernus");
        Aspect ira = ForbiddenAspects.ASPECTS.get("ira");
        Aspect gula = ForbiddenAspects.ASPECTS.get("gula");

        var pedra = ObjectAspects.of(new ItemStack(Items.NETHERRACK));
        if (pedra.getAmount(infernus) != 1) helper.fail("a pedra do Nether ganha um de infernus; tem " + pedra.getAmount(infernus));
        if (pedra.getAmount(Aspects.FIRE) < 1) helper.fail("e não perde o fogo que já tinha");

        var bolo = ObjectAspects.of(new ItemStack(Items.CAKE));
        if (bolo.getAmount(gula) != 7) helper.fail("o bolo ganha sete de gula; tem " + bolo.getAmount(gula));

        var tnt = ObjectAspects.of(new ItemStack(Items.TNT));
        if (tnt.getAmount(ira) != 2) helper.fail("a dinamite ganha dois de ira; tem " + tnt.getAmount(ira));

        // as criaturas: o creeper comum e o carregado levam contas diferentes, como no original
        var creeper = helper.spawn(net.minecraft.world.entity.EntityTypes.CREEPER, new BlockPos(1, 2, 1));
        var lista = EntityAspects.of(creeper);
        if (lista == null || lista.getAmount(ira) != 2) {
            helper.fail("o creeper ganha dois de ira; tem " + (lista == null ? "nada" : lista.getAmount(ira)));
        }
        if (lista != null && lista.getAmount(Aspects.PLANT) < 2) helper.fail("e não perde a planta que já tinha");
        helper.succeed();
    }

    private static Aspect byName(String name) {
        return switch (name) {
            case "FIRE" -> Aspects.FIRE;
            case "MAGIC" -> Aspects.MAGIC;
            case "WEAPON" -> Aspects.WEAPON;
            case "HUNGER" -> Aspects.HUNGER;
            case "VOID" -> Aspects.VOID;
            case "SENSES" -> Aspects.SENSES;
            case "FLIGHT" -> Aspects.FLIGHT;
            case "TRAP" -> Aspects.TRAP;
            case "SOUL" -> Aspects.SOUL;
            case "FLESH" -> Aspects.FLESH;
            default -> null;
        };
    }
}
