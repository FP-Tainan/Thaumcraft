package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellModifierKind;

import java.util.List;

/**
 * As essências que <b>empurram</b>, e a que puxa.
 *
 * <p>Nenhuma delas fere ninguém. O que elas fazem é mexer em quem já está ali — e num jogo em que se cai de
 * alturas e se morre disso, empurrar é uma arma tão boa como qualquer outra.
 */
public class ArcanaPushGameTest {
    /** O Arremesso atira para cima, e a Velocidade Acrescentada atira mais. */
    @GameTest(maxTicks = 80)
    public void flingThrowsUpward(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);
        porco.setDeltaMovement(0.0, 0.0, 0.0);
        Essences.FLING.onEntity(level, Spell.of(Shapes.TOUCH, Essences.FLING), quem, porco);
        double simples = porco.getDeltaMovement().y;

        porco.setDeltaMovement(0.0, 0.0, 0.0);
        Spell forte = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.FLING), List.of(Modifiers.VELOCITY_ADDED))));
        Essences.FLING.onEntity(level, forte, quem, porco);
        double comMais = porco.getDeltaMovement().y;

        if (simples < 1.0) helper.fail("o Arremesso atira para cima, e deu " + simples);
        if (comMais <= simples) helper.fail("e com a Velocidade Acrescentada atira mais");
        if (Math.abs(comMais - simples - 0.5) > 1.0e-6) helper.fail("meio a mais, e deu " + (comMais - simples));

        porco.discard();
        helper.succeed();
    }

    /** O Empurrão atira para longe de quem lançou, e não para cima. */
    @GameTest(maxTicks = 80)
    public void knockbackPushesAway(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.0, 1.5)), 0.0f, 0.0f);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 1));
        porco.setNoAi(true);
        porco.setDeltaMovement(0.0, 0.0, 0.0);

        Essences.KNOCKBACK.onEntity(level, Spell.of(Shapes.TOUCH, Essences.KNOCKBACK), quem, porco);
        var anda = porco.getDeltaMovement();

        // o porco está a leste de quem lançou, e é para leste que ele vai
        var paraLá = porco.position().subtract(quem.position());
        if (anda.horizontalDistance() < 1.0) helper.fail("o Empurrão empurra de verdade");
        if (anda.x * paraLá.x + anda.z * paraLá.z <= 0.0) {
            helper.fail("e empurra para longe de quem lançou, não para perto");
        }
        if (Math.abs(anda.y - 0.325) > 1.0e-6) helper.fail("e levanta pouco: 0,325");

        porco.discard();
        helper.succeed();
    }

    /**
     * O Repelir apontado a quem o lança afasta <b>tudo o que está em roda</b>.
     *
     * <p>E afasta todos com a <b>mesma força</b>. A conta do original parece depender da distância e não
     * depende: dividir a linha entre os dois pelo comprimento dela dá uma linha de comprimento um. O que muda
     * de um para o outro é o rumo, e mais nada — tirando o décimo que o original soma à distância, que faz
     * quem está colado ser empurrado um bocadinho menos.
     */
    @GameTest(maxTicks = 80)
    public void repelPushesEverythingAround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2.0, 2.5)), 0.0f, 0.0f);

        var perto = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 2));
        var longe = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 2));
        perto.setNoAi(true);
        longe.setNoAi(true);
        perto.setDeltaMovement(0.0, 0.0, 0.0);
        longe.setDeltaMovement(0.0, 0.0, 0.0);

        Essences.REPEL.onEntity(level, Spell.of(Shapes.SELF, Essences.REPEL), quem, quem);

        double doPerto = perto.getDeltaMovement().length();
        double doLonge = longe.getDeltaMovement().length();
        if (doPerto <= 0.0) helper.fail("quem está em roda é afastado");
        if (Math.abs(doLonge - 0.4) > 0.05) {
            helper.fail("e a força é sempre a mesma, 0,4: deu " + doLonge);
        }
        if (doPerto > doLonge) {
            helper.fail("e o décimo somado à distância faz quem está colado voar um bocadinho menos: "
                    + doPerto + " e " + doLonge);
        }

        perto.discard();
        longe.discard();
        helper.succeed();
    }

    /** A Telecinese chama a si o que está caído, e não o que acabou de cair. */
    @GameTest(maxTicks = 80)
    public void telekinesisPullsWhatHasSettled(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var caído = new ItemEntity(level, helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2.0, 5.5)).x,
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2.0, 5.5)).y,
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2.0, 5.5)).z,
                new ItemStack(Items.DIAMOND));
        level.addFreshEntity(caído);
        caído.tickCount = 40;
        caído.setDeltaMovement(0.0, 0.0, 0.0);

        var agora = new ItemEntity(level, helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2.0, 4.5)).x,
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2.0, 4.5)).y,
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2.0, 4.5)).z,
                new ItemStack(Items.EMERALD));
        level.addFreshEntity(agora);
        agora.tickCount = 0;
        agora.setDeltaMovement(0.0, 0.0, 0.0);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        porco.setNoAi(true);
        Essences.TELEKINESIS.onEntity(level, Spell.of(Shapes.TOUCH, Essences.TELEKINESIS), quem, porco);

        if (caído.getDeltaMovement().lengthSqr() <= 0.0) helper.fail("o que já estava no chão vem");
        if (agora.getDeltaMovement().lengthSqr() > 0.0) helper.fail("e o que acabou de cair fica");
        if (caído.getDeltaMovement().y > 0.0) helper.fail("e nada sobe");

        caído.discard();
        agora.discard();
        porco.discard();
        helper.succeed();
    }

    /** O Desarmar faz cair a arma de um bicho — e ela cai gasta. */
    @GameTest(maxTicks = 80)
    public void disarmDropsAWornWeapon(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);

        var esqueleto = helper.spawn(EntityTypes.SKELETON, new BlockPos(3, 2, 3));
        esqueleto.setNoAi(true);
        esqueleto.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));

        if (!Essences.DISARM.onEntity(level, Spell.of(Shapes.TOUCH, Essences.DISARM), quem, esqueleto)) {
            helper.fail("devia desarmar");
            return;
        }
        if (!esqueleto.getMainHandItem().isEmpty()) helper.fail("e a mão fica vazia");

        boolean achou = false;
        for (var e : level.getEntitiesOfClass(ItemEntity.class, esqueleto.getBoundingBox().inflate(3.0))) {
            if (e.getItem().is(Items.BOW)) {
                achou = true;
                if (e.getItem().getDamageValue() < e.getItem().getMaxDamage() * 0.75) {
                    helper.fail("e o arco cai gasto");
                }
            }
        }
        if (!achou) helper.fail("e o arco cai no chão");

        esqueleto.discard();
        helper.succeed();
    }

    /** E não se desarma quem não tem nada na mão. */
    @GameTest(maxTicks = 80)
    public void thereIsNothingToDisarm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.CREATIVE);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.setNoAi(true);

        if (Essences.DISARM.onEntity(level, Spell.of(Shapes.TOUCH, Essences.DISARM), quem, porco)) {
            helper.fail("um porco não tem o que largar");
        }

        porco.discard();
        helper.succeed();
    }

    /** A Velocidade Acrescentada soma meio, e se conta. */
    @GameTest
    public void velocityAddedAddsHalf(GameTestHelper helper) {
        Spell duas = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.FLING),
                List.of(Modifiers.VELOCITY_ADDED, Modifiers.VELOCITY_ADDED))));
        double soma = duas.add(SpellModifierKind.VELOCITY_ADDED, 0.0);
        if (Math.abs(soma - 1.0) > 1.0e-6) helper.fail("duas somam um, e deu " + soma);
        helper.succeed();
    }
}
