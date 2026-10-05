package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.goblin.GoblinClothes;

/**
 * A <b>roupa de goblin</b>: a Fita Torcida, a Aljava do Mog e a Cinta do Gulg.
 *
 * <p>A prova que carrega a fatia é a do <b>par</b>: dois jogadores a oito blocos, um com a Aljava e outro
 * com a Cinta, ficam os dois <b>mais duros</b>. É a mesma conta de distância que faz os deuses goblins
 * invencíveis, virada para quem joga — e é o único par de peças do mod inteiro que <b>só vale a dois</b>.
 */
public class OccultaGoblinClothesGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números dela são os do original. */
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (GoblinClothes.PERTO != 8.0 || GoblinClothes.PROCURA != 100) {
            helper.fail("o par se sente a oito blocos, de cinco em cinco segundos");
        }
        if (GoblinClothes.RESISTE != 200 || GoblinClothes.RESISTE_NÍVEL != 1) {
            helper.fail("e dá Resistência II por dez segundos");
        }
        if (GoblinClothes.OLHAR != 16.0 || GoblinClothes.DE_CINCO != 5
                || GoblinClothes.TONTO != 100) {
            helper.fail("a Fita olha a dezesseis blocos, de cinco em cinco batidas, e dá cinco segundos");
        }
        if (GoblinClothes.NO_AR != 3.0f || GoblinClothes.FRACO != 200) {
            helper.fail("a flecha da Aljava vale o triplo no ar e dá dez segundos de Fraqueza");
        }
        if (GoblinClothes.MURRO != 5.0f || GoblinClothes.VOA != 1.0) {
            helper.fail("e o murro da Cinta vale cinco e um bloco de voo");
        }
        helper.succeed();
    }

    /**
     * <b>O par.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Um sozinho não ganha nada, mesmo com as duas peças vestidas;
     * dois a oito blocos, cada um com a sua, ficam os dois mais duros.
     */
    @GameTest(maxTicks = 40)
    public void theTwoPiecesOnlyPayInPairs(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));

        ServerPlayer um = helper.makeMockServerPlayerInLevel();
        um.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        um.removeEffect(MobEffects.RESISTANCE);

        // ele sozinho, com as duas peças, não ganha nada
        um.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.MOGS_QUIVER));
        um.setItemSlot(EquipmentSlot.LEGS, new ItemStack(OccultaItems.GULGS_GURDLE));
        GoblinClothes.oPar(um);
        if (um.hasEffect(MobEffects.RESISTANCE)) {
            helper.fail("uma pessoa com as duas peças não ganha nada com isso");
        }

        // e com outro ao lado, com a peça que falta, ganham os dois
        ServerPlayer outro = helper.makeMockServerPlayerInLevel();
        outro.setPos(onde.getX() + 2.5, onde.getY(), onde.getZ() + 0.5);
        um.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        outro.setItemSlot(EquipmentSlot.LEGS, new ItemStack(OccultaItems.GULGS_GURDLE));

        GoblinClothes.oPar(um);
        var dura = um.getEffect(MobEffects.RESISTANCE);
        if (dura == null || dura.getAmplifier() != GoblinClothes.RESISTE_NÍVEL) {
            helper.fail("os dois juntos ficam mais duros; deu " + dura);
        }

        um.removeEffect(MobEffects.RESISTANCE);
        um.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        outro.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        helper.succeed();
    }

    /**
     * <b>A Fita Torcida desnorteia quem está olhando — e só quem está olhando.</b>
     *
     * <p>O cone é o do enderman, e <b>aperta com a distância</b>: de longe é preciso olhar bem certo, de
     * perto basta ter a pessoa à frente.
     */
    @GameTest(maxTicks = 40)
    public void theBandOnlyCatchesWhoIsLooking(GameTestHelper helper) {
        piso(helper);
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.KOBOLDITE_HELM));
        if (!GoblinClothes.temFita(quem)) helper.fail("a Fita devia estar na cabeça dele");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 6));
        zumbi.removeEffect(MobEffects.WEAKNESS);

        // de costas, não pega
        zumbi.setYRot(0.0f);
        zumbi.setYHeadRot(0.0f);
        zumbi.setXRot(0.0f);
        if (GoblinClothes.olhaPara(quem, zumbi)) helper.fail("de costas ele não está olhando");

        // e uma abóbora na cabeça não se desnorteia
        zumbi.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
        zumbi.setYRot(180.0f);
        zumbi.setYHeadRot(180.0f);
        if (GoblinClothes.olhaPara(quem, zumbi)) helper.fail("quem não vê não se desnorteia");

        zumbi.discard();
        quem.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        helper.succeed();
    }

    /**
     * <b>O murro de mão vazia da Cinta vale cinco — e só de mão vazia.</b>
     *
     * <p>Com qualquer coisa na mão, a Cinta cala-se: ela é para quem anda sem nada.
     */
    @GameTest(maxTicks = 40)
    public void theGurdlePunchesOnlyBareHanded(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5);
        quem.setItemSlot(EquipmentSlot.LEGS, new ItemStack(OccultaItems.GULGS_GURDLE));
        quem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        var fonte = level.damageSources().playerAttack(quem);
        if (!GoblinClothes.daCinta(fonte)) helper.fail("de mão vazia, a Cinta fala");

        quem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        if (GoblinClothes.daCinta(fonte)) helper.fail("e com uma espada na mão, cala-se");

        // e o que ela faz: cinco de dano e um bloco de voo
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 4));
        zumbi.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        float quanto = GoblinClothes.aCinta(zumbi);
        if (quanto != GoblinClothes.MURRO) helper.fail("cinco de dano; deu " + quanto);
        if (Math.abs(zumbi.getDeltaMovement().y - GoblinClothes.VOA) > 1.0E-6) {
            helper.fail("e um bloco de voo; voou " + zumbi.getDeltaMovement().y);
        }

        zumbi.discard();
        quem.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        quem.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        helper.succeed();
    }

    /**
     * <b>A flecha da Aljava vale o triplo em quem está no ar — e deixa Fraqueza em qualquer caso.</b>
     */
    @GameTest(maxTicks = 40)
    public void theQuiverArrowCrushesTheAirborne(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        zumbi.removeEffect(MobEffects.WEAKNESS);

        // no chão, vale o que vale
        zumbi.setOnGround(true);
        float noChão = GoblinClothes.aFlecha(level, zumbi, 4.0f);
        if (noChão != 4.0f) helper.fail("no chão ela vale o que vale; valeu " + noChão);
        if (!zumbi.hasEffect(MobEffects.WEAKNESS)) helper.fail("mas a Fraqueza fica sempre");

        // no ar, o triplo
        zumbi.setOnGround(false);
        float noAr = GoblinClothes.aFlecha(level, zumbi, 4.0f);
        if (noAr != 12.0f) helper.fail("no ar vale o triplo; valeu " + noAr);

        zumbi.discard();
        helper.succeed();
    }
}
