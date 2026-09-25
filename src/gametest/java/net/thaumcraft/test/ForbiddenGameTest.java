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
import net.thaumcraft.forbidden.ForbiddenItems;
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

    /** Os oito fragmentos existem, com o aspecto do pecado de cada um. */
    @GameTest
    public void theEightShardsExist(GameTestHelper helper) {
        if (net.thaumcraft.forbidden.ForbiddenItems.SHARDS.size() != 7) {
            helper.fail("o original tem sete vícios; há " + net.thaumcraft.forbidden.ForbiddenItems.SHARDS.size());
        }
        String[][] pares = {{"wrath", "ira"}, {"envy", "invidia"}, {"pride", "superbia"},
                {"lust", "luxuria"}, {"sloth", "desidia"}};
        for (String[] par : pares) {
            var item = net.thaumcraft.forbidden.ForbiddenItems.SHARDS.get(par[0]);
            var aspectos = ObjectAspects.of(new ItemStack(item));
            Aspect pecado = ForbiddenAspects.ASPECTS.get(par[1]);
            if (aspectos.getAmount(pecado) != 2) {
                helper.fail("o fragmento da " + par[0] + " tem dois de " + par[1] + "; tem " + aspectos.getAmount(pecado));
            }
            if (aspectos.getAmount(Aspects.CRYSTAL) != 1) helper.fail("e um de cristal");
        }
        // o da mácula não é pecado: ele leva mácula
        var macula = ObjectAspects.of(new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.SHARDS.get("taint")));
        if (macula.getAmount(Aspects.TAINT) != 3) helper.fail("o fragmento da mácula tem três de mácula");
        // e o da gula se come
        var gula = new ItemStack(net.thaumcraft.forbidden.ForbiddenItems.GLUTTONY_SHARD);
        if (gula.get(net.minecraft.core.component.DataComponents.FOOD) == null) {
            helper.fail("o fragmento da gula é comida, como no original");
        }
        helper.succeed();
    }

    /** A Preguiça cai de quem morre sozinho no Nether, e nada cai fora dele. */
    @GameTest
    public void theSlothShardFallsFromTheLonelyDead(GameTestHelper helper) {
        // fora do Nether, o ramo não mexe em nada
        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(1, 2, 1));
        if (net.thaumcraft.forbidden.ForbiddenDrops.inTheNether(helper.getLevel())) {
            helper.fail("o mundo do teste não é o Nether");
        }
        net.thaumcraft.forbidden.ForbiddenDrops.onDeath(porco, helper.getLevel().damageSources().generic());
        var caidos = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(1, 2, 1))).inflate(4.0));
        if (!caidos.isEmpty()) helper.fail("fora do Nether não cai fragmento nenhum");
        helper.succeed();
    }
}
