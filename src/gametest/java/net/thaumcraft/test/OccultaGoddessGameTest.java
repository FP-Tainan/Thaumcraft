package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.GoddessStatueBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.curse.Curse;

/**
 * A <b>Estátua da Deusa</b>: a única coisa do mod que desfaz uma maldição sem pedir nada em troca.
 */
public class OccultaGoddessGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** As cinco poções que ela tira <b>de carona</b>, quando tira alguma maldição. */
    private static final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>[] DE_CARONA =
            new net.minecraft.core.Holder[]{MobEffects.POISON, MobEffects.WEAKNESS, MobEffects.BLINDNESS,
                    MobEffects.MINING_FATIGUE, MobEffects.SLOWNESS};

    /**
     * <b>Ou tudo, ou nada.</b> Havendo <b>uma</b> maldição, caem as cinco e caem as cinco poções —
     * mesmo as quatro maldições que a pessoa não tinha.
     */
    @GameTest(maxTicks = 40)
    public void sheLiftsEveryCurseAndTheFivePotionsWithThem(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos onde = new BlockPos(2, 2, 2);
        level.setBlockAndUpdate(helper.absolutePos(onde),
                OccultaBlocks.GODDESS_STATUE.defaultBlockState());

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 2.5)));
        for (Curse qual : Curse.values()) Curse.put(quem, qual, 2);
        for (var poção : DE_CARONA) quem.addEffect(new MobEffectInstance(poção, 200));

        if (!GoddessStatueBlock.cura(level, helper.absolutePos(onde), quem)) {
            helper.fail("com maldição, ela cura");
        }
        for (Curse qual : Curse.values()) {
            if (Curse.level(quem, qual) != 0) helper.fail("a maldição " + qual + " cai");
        }
        for (var poção : DE_CARONA) {
            if (quem.hasEffect(poção)) helper.fail("e a poção vai de carona");
        }

        level.setBlockAndUpdate(helper.absolutePos(onde), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>E quem só está envenenado não se cura aqui.</b> É o avesso do que parece: a condição do original
     * lê as <b>cinco maldições</b> e mais nada — as poções vão de carona ou não vão.
     */
    @GameTest(maxTicks = 40)
    public void withoutACurseShePullsNothingOffYou(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos onde = new BlockPos(2, 2, 4);
        level.setBlockAndUpdate(helper.absolutePos(onde),
                OccultaBlocks.GODDESS_STATUE.defaultBlockState());

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 4.5)));
        quem.addEffect(new MobEffectInstance(MobEffects.POISON, 200));

        if (GoddessStatueBlock.cura(level, helper.absolutePos(onde), quem)) {
            helper.fail("sem maldição, ela não cura nada");
        }
        if (!quem.hasEffect(MobEffects.POISON)) helper.fail("e o veneno fica");

        // e uma maldição só já abre a porta para as duas coisas
        Curse.put(quem, Curse.SINKING, 1);
        if (!GoddessStatueBlock.cura(level, helper.absolutePos(onde), quem)) {
            helper.fail("uma maldição basta");
        }
        if (quem.hasEffect(MobEffects.POISON)) helper.fail("e aí o veneno vai junto");

        level.setBlockAndUpdate(helper.absolutePos(onde), Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Ela mede <b>dois blocos</b> de alto, como no original, e olha para quem a assenta. */
    @GameTest(maxTicks = 20)
    public void sheIsTwoBlocksTall(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.GODDESS_STATUE.defaultBlockState());

        var feitio = level.getBlockState(onde);
        if (!feitio.is(OccultaBlocks.GODDESS_STATUE)) helper.fail("a estátua fica de pé");
        var caixa = feitio.getShape(level, onde).bounds();
        if (Math.abs(caixa.maxY - 2.0) > 0.001) helper.fail("e mede dois de alto, mede " + caixa.maxY);
        if (!feitio.hasProperty(GoddessStatueBlock.FACING)) helper.fail("e sabe para onde olha");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
