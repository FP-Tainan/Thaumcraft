package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.broom.BroomEntity;
import net.thaumcraft.occulta.familiar.Familiars;

/**
 * A vassoura: o que ela faz, de que cor fica, e o que a coruja lhe soma.
 *
 * <p>A última é a razão de esta fatia existir. A fatia dos familiares entregou a coruja com a <b>maestria da
 * vassoura</b> escrita e sem nada para destrancar — estava dito no javadoc e no {@code PORTE.md}, e era dívida.
 * A prova {@code theOwlMakesHerFasterAndGivesHerBrakes} é onde ela fica paga.
 */
public class OccultaBroomGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static BroomEntity vassoura(GameTestHelper helper, int x, int y, int z) {
        return helper.spawn(OccultaEntities.BROOM, new BlockPos(x, y, z), EntitySpawnReason.MOB_SUMMONED);
    }

    /** Os números da conta de voo são os do original. */
    @GameTest(maxTicks = 20)
    public void theFlightNumbersAreTheOriginals(GameTestHelper helper) {
        if (BroomEntity.EMPURRÃO_PARADO != 0.07) {
            helper.fail("ela parte de 0,07, parte de " + BroomEntity.EMPURRÃO_PARADO);
        }
        if (BroomEntity.EMPURRÃO_MÁXIMO != 0.35) {
            helper.fail("e sobe até 0,35, sobe até " + BroomEntity.EMPURRÃO_MÁXIMO);
        }
        if (BroomEntity.TETO != 0.9) helper.fail("o teto é 0,9, é " + BroomEntity.TETO);
        if (BroomEntity.CORUJA_TETO != 0.3) {
            helper.fail("e a coruja soma 0,3, soma " + BroomEntity.CORUJA_TETO);
        }
        if (BroomEntity.CORUJA_EMPURRÃO != 0.2) {
            helper.fail("e 0,2 de empurrão, soma " + BroomEntity.CORUJA_EMPURRÃO);
        }
        helper.succeed();
    }

    /** Sem tinta ela é castanha, e a tinta na mão pinta as cerdas em vez de montar. */
    @GameTest(maxTicks = 30)
    public void dyePaintsTheBristlesInsteadOfMounting(GameTestHelper helper) {
        piso(helper);
        var vassoura = vassoura(helper, 3, 2, 3);
        if (vassoura.cor() != -1) helper.fail("por pintar, ela não tem cor nenhuma");

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));
        quem.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(Items.DYE.pick(net.minecraft.world.item.DyeColor.PURPLE), 2));

        vassoura.interact(quem, InteractionHand.MAIN_HAND, Vec3.ZERO);
        if (vassoura.cor() != net.minecraft.world.item.DyeColor.PURPLE.getId()) {
            helper.fail("pintada de roxo, a cor é a do roxo, e é " + vassoura.cor());
        }
        if (!quem.getPassengers().isEmpty() || vassoura.getFirstPassenger() != null) {
            helper.fail("e com tinta na mão ninguém monta");
        }
        if (quem.getMainHandItem().getCount() != 1) {
            helper.fail("e gasta uma tinta, sobraram " + quem.getMainHandItem().getCount());
        }

        vassoura.discard();
        helper.succeed();
    }

    /** De mão vazia, monta-se. */
    @GameTest(maxTicks = 30)
    public void emptyHandedYouMountIt(GameTestHelper helper) {
        piso(helper);
        var vassoura = vassoura(helper, 3, 2, 3);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));

        vassoura.interact(quem, InteractionHand.MAIN_HAND, Vec3.ZERO);
        if (quem.getVehicle() != vassoura) helper.fail("de mão vazia, monta-se");

        quem.stopRiding();
        vassoura.discard();
        helper.succeed();
    }

    /**
     * <b>A coruja.</b> Quem tem a maestria da vassoura monta com mais teto, e com freio.
     *
     * <p>A conta é lida ao montar, uma vez só, como no original — e por isso esta prova vincula a coruja
     * <b>antes</b> de montar.
     */
    @GameTest(maxTicks = 40)
    public void theOwlMakesHerFasterAndGivesHerBrakes(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));
        if (Familiars.temMaestriaDeVassoura(quem)) helper.fail("esta prova começa sem coruja");

        var coruja = helper.spawn(OccultaEntities.OWL, new BlockPos(5, 2, 5));
        coruja.tame(quem);
        if (!Familiars.vincula(quem, coruja)) helper.fail("a coruja devia vincular-se");
        if (!Familiars.temMaestriaDeVassoura(quem)) {
            helper.fail("e a coruja é a maestria da vassoura — era isto que não destrancava nada");
        }

        var vassoura = vassoura(helper, 3, 2, 3);
        vassoura.interact(quem, InteractionHand.MAIN_HAND, Vec3.ZERO);
        if (quem.getVehicle() != vassoura) helper.fail("montou");
        if (!vassoura.temCoruja()) helper.fail("e a vassoura sabe que quem a monta tem a coruja");

        quem.stopRiding();
        vassoura.discard();
        coruja.discard();
        helper.succeed();
    }

    /** Quebrada, ela deixa no chão a vassoura encantada — que é o que se montava. */
    @GameTest(maxTicks = 40)
    public void brokenItLeavesTheEnchantedBroom(GameTestHelper helper) {
        piso(helper);
        var vassoura = vassoura(helper, 3, 2, 3);
        var level = helper.getLevel();

        vassoura.hurtServer(level, level.damageSources().generic(), BroomEntity.AGUENTA + 1.0f);
        if (vassoura.isAlive()) helper.fail("quarenta de dano desfazem a vassoura");

        helper.succeedWhen(() -> helper.assertItemEntityPresent(OccultaItems.ENCHANTED_BROOM));
    }

    /** E uma vassoura na mão apaga o giz do chão. */
    @GameTest(maxTicks = 30)
    public void aBroomInHandSweepsTheChalkAway(GameTestHelper helper) {
        piso(helper);
        BlockPos giz = new BlockPos(3, 2, 3);
        helper.setBlock(giz, OccultaBlocks.RITUAL_GLYPH);
        helper.assertBlockPresent(OccultaBlocks.RITUAL_GLYPH, giz);

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));

        // de mão vazia não apaga
        OccultaBlocks.RITUAL_GLYPH.defaultBlockState()
                .attack(helper.getLevel(), helper.absolutePos(giz), quem);
        helper.assertBlockPresent(OccultaBlocks.RITUAL_GLYPH, giz);

        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.BROOM));
        OccultaBlocks.RITUAL_GLYPH.defaultBlockState()
                .attack(helper.getLevel(), helper.absolutePos(giz), quem);
        helper.assertBlockNotPresent(OccultaBlocks.RITUAL_GLYPH, giz);
        helper.succeed();
    }
}
