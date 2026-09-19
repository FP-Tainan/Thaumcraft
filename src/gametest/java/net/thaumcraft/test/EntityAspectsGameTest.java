package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.research.EntityAspects;

/** Os aspectos das criaturas vêm da tabela do {@code ConfigAspects} (o creeper carregado muda; o que não está lá, nada). */
public class EntityAspectsGameTest {
    @GameTest
    public void creaturesFromTheTable(GameTestHelper helper) {
        var cow = EntityAspects.of(helper.spawn(EntityTypes.COW, new BlockPos(1, 2, 1)));
        if (cow == null || cow.getAmount(Aspects.BEAST) != 3 || cow.getAmount(Aspects.EARTH) != 3) helper.fail("vaca: Bestia 3, Terra 3");
        var wither = EntityAspects.of(helper.spawn(EntityTypes.WITHER_SKELETON, new BlockPos(2, 2, 1)));
        if (wither == null || wither.getAmount(Aspects.FIRE) != 2) helper.fail("esqueleto do Wither: Ignis 2");
        var creeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(3, 2, 1));
        if (EntityAspects.of(creeper).getAmount(Aspects.ENERGY) != 0) helper.fail("creeper comum sem Potentia");
        var taint = EntityAspects.of(helper.spawn(TCEntities.TAINT_COW, new BlockPos(1, 2, 3)));
        if (taint == null || taint.getAmount(Aspects.TAINT) != 3) helper.fail("vaca maculada: Vitium 3");
        if (EntityAspects.of(helper.spawn(EntityTypes.ARMADILLO, new BlockPos(3, 2, 3))) != null) helper.fail("o tatu não está na tabela");
        helper.succeed();
    }
}
