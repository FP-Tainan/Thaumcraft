package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.village.VillageGuardEntity;

/**
 * O Guarda da Aldeia: o que ele veste, com que briga, quem ele poupa e o que custa matá-lo.
 *
 * <p><b>Duas coisas da arena que estas provas aprenderam à força</b>, e que vale deixar escritas. A arena por
 * omissão do Fabric é <b>oito por oito por oito de ar puro</b> — não tem chão. E uma criatura posta à mão com
 * {@code addFreshEntity} <b>é apagada pelo despawn</b> em poucos tiques, porque não há jogador por perto: o
 * {@code helper.spawn} do próprio jogo é que lhe põe a persistência, além de tratar a volta da arena. Sem essas
 * duas coisas o guarda não atira e não anda, e o que se vê é um bicho parado que parece defeituoso e não é.
 */
public class OccultaVillageGuardGameTest {
    /** O piso da arena, que ela não traz. */
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** E a caixa onde se procura o que voou, que não passa das paredes da arena. */
    private static AABB arena(GameTestHelper helper) {
        return new AABB(helper.absoluteVec(new Vec3(0.0, 2.0, 0.0)),
                helper.absoluteVec(new Vec3(8.0, 7.0, 8.0)));
    }

    /** Um guarda na arena, equipado como nasceria e persistente como o original o faz nascer. */
    private static VillageGuardEntity guarda(GameTestHelper helper, int x, int y, int z) {
        return helper.spawn(OccultaEntities.VILLAGE_GUARD, new BlockPos(x, y, z),
                EntitySpawnReason.STRUCTURE);
    }

    /**
     * Ele nasce com arco e de couro, e o peito e a cabeça podem vir de malha.
     *
     * <p>Quarenta de vida e quatro de dano, que são os números do original.
     */
    @GameTest(maxTicks = 20)
    public void theGuardIsBornWithABowAndLeather(GameTestHelper helper) {
        piso(helper);
        var guarda = guarda(helper, 2, 2, 2);
        if (!guarda.getMainHandItem().is(Items.BOW)) helper.fail("o guarda nasce com arco na mão");
        if (!guarda.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS)) {
            helper.fail("as botas do guarda são de couro");
        }
        if (!guarda.getItemBySlot(EquipmentSlot.LEGS).is(Items.LEATHER_LEGGINGS)) {
            helper.fail("as calças do guarda são de couro");
        }
        var peito = guarda.getItemBySlot(EquipmentSlot.CHEST);
        if (!peito.is(Items.LEATHER_CHESTPLATE) && !peito.is(Items.CHAINMAIL_CHESTPLATE)) {
            helper.fail("o peito do guarda é de couro ou de malha, veio " + peito);
        }
        var cabeça = guarda.getItemBySlot(EquipmentSlot.HEAD);
        if (!cabeça.is(Items.LEATHER_HELMET) && !cabeça.is(Items.CHAINMAIL_HELMET)) {
            helper.fail("o elmo do guarda é de couro ou de malha, veio " + cabeça);
        }
        if (guarda.getAttributeBaseValue(Attributes.MAX_HEALTH) != 40.0) {
            helper.fail("o guarda tem quarenta de vida");
        }
        if (guarda.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) != 4.0) {
            helper.fail("o guarda bate por quatro");
        }
        guarda.discard();
        helper.succeed();
    }

    /**
     * Com arco na mão ele <b>atira</b>: posto um zumbi à frente, aparece uma flecha no ar.
     *
     * <p>E se olha <b>a cada tique</b>, porque uma flecha que acerta é removida na hora: olhar só no fim é olhar
     * o único instante em que não há flecha nenhuma.
     */
    @GameTest(maxTicks = 200)
    public void theGuardWithABowShoots(GameTestHelper helper) {
        var level = helper.getLevel();
        piso(helper);
        var guarda = guarda(helper, 1, 2, 3);
        if (!guarda.aimingFromAfar()) helper.fail("com arco na mão, a mira de longe é que entra");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(6, 2, 3));
        guarda.setTarget(zumbi);

        AABB arena = arena(helper);
        helper.succeedWhen(() -> {
            if (level.getEntitiesOfClass(AbstractArrow.class, arena).isEmpty()) {
                helper.fail("o guarda de arco devia atirar");
            }
            guarda.discard();
            zumbi.discard();
            level.getEntitiesOfClass(AbstractArrow.class, arena)
                    .forEach(net.minecraft.world.entity.Entity::discard);
        });
    }

    /**
     * Sem arco a mira de longe <b>sai</b>, e ele avança: o outro lado do mesmo {@code setCombatTask}.
     *
     * <p>Esta prova mostra que a troca é <b>viva</b> e não só feita ao nascer — a mão muda depois de o guarda já
     * existir, e a mira muda ali. E ela julga a <b>decisão</b>, e não o que aparece no ar: contar flechas para
     * provar que <i>não</i> houve flecha é prova fraca, porque no fim nunca há flecha nenhuma.
     */
    @GameTest(maxTicks = 200)
    public void theGuardWithoutABowSwingsInstead(GameTestHelper helper) {
        piso(helper);
        var guarda = guarda(helper, 1, 2, 3);
        if (!guarda.aimingFromAfar()) helper.fail("de arco, nasce a mirar de longe");

        guarda.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (guarda.aimingFromAfar()) helper.fail("tirado o arco, a mira de longe sai");

        guarda.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        if (!guarda.aimingFromAfar()) helper.fail("devolvido o arco, a mira de longe volta");

        guarda.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        if (guarda.aimingFromAfar()) helper.fail("de espada, ele avança e não atira");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(6, 2, 3));
        guarda.setTarget(zumbi);

        // e de espada ele chega perto: a prova de que a briga de perto entrou mesmo
        helper.succeedWhen(() -> {
            if (guarda.distanceTo(zumbi) > 3.0) {
                helper.fail("de espada o guarda devia chegar perto, está a " + guarda.distanceTo(zumbi));
            }
            guarda.discard();
            zumbi.discard();
        });
    }

    /** Dois guardas não se ferem, e nenhum deles mira num creeper: o {@code canAttackClass} do original. */
    @GameTest(maxTicks = 20)
    public void guardsSpareEachOtherAndTheCreeper(GameTestHelper helper) {
        var level = helper.getLevel();
        piso(helper);
        var um = guarda(helper, 2, 2, 2);
        var outro = guarda(helper, 3, 2, 2);
        float antes = outro.getHealth();
        outro.hurtServer(level, level.damageSources().mobAttack(um), 5.0f);
        if (outro.getHealth() != antes) helper.fail("um guarda não fere outro guarda");
        if (um.canAttack(outro)) helper.fail("um guarda não mira noutro guarda");

        var creeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(5, 2, 2));
        if (um.canAttack(creeper)) helper.fail("o guarda não mira num creeper");

        um.discard();
        outro.discard();
        creeper.discard();
        helper.succeed();
    }

    /** O infernal é maior, e o fogo não lhe faz nada. */
    @GameTest(maxTicks = 20)
    public void theInfernalGuardIsBiggerAndFireProof(GameTestHelper helper) {
        piso(helper);
        var guarda = guarda(helper, 2, 2, 2);
        if (guarda.fireImmune()) helper.fail("o guarda comum não é imune ao fogo");
        float altura = guarda.getDefaultDimensions(Pose.STANDING).height();

        guarda.setGuardType(VillageGuardEntity.INFERNAL);
        if (!guarda.fireImmune()) helper.fail("o infernal é imune ao fogo");
        var grande = guarda.getDefaultDimensions(Pose.STANDING);
        if (grande.height() <= altura) helper.fail("o infernal é mais alto que o comum");
        if (grande.height() != 2.34f) helper.fail("o infernal tem 2,34 de altura, tem " + grande.height());
        if (grande.width() != 0.72f) helper.fail("o infernal tem 0,72 de largura, tem " + grande.width());

        guarda.discard();
        helper.succeed();
    }

    /**
     * Matar um guarda custa reputação com os aldeões que viram.
     *
     * <p>No original são cinco pontos da reputação da aldeia; hoje é fuxico de aldeão, e se conta como morte de
     * aldeão — que é o que ele é, por o original o fazer nascer de um.
     */
    @GameTest(maxTicks = 40)
    public void killingAGuardTurnsTheVillageAgainstYou(GameTestHelper helper) {
        var level = helper.getLevel();
        piso(helper);
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 4));

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 3.5)));
        if (aldeão.getPlayerReputation(quem) != 0) helper.fail("o aldeão ainda não tem nada contra ninguém");

        var guarda = guarda(helper, 2, 2, 2);
        guarda.die(level.damageSources().playerAttack(quem));
        if (aldeão.getPlayerReputation(quem) >= 0) {
            helper.fail("matar o guarda devia deixar o aldeão de mal, ficou em "
                    + aldeão.getPlayerReputation(quem));
        }

        guarda.discard();
        aldeão.discard();
        helper.succeed();
    }
}
