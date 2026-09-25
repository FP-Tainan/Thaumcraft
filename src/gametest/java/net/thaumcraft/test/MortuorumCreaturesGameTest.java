package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.mortuorum.IsaacEntity;
import net.thaumcraft.mortuorum.MortuorumEntities;
import net.thaumcraft.mortuorum.MortuorumEvents;
import net.thaumcraft.mortuorum.MortuorumItems;
import net.thaumcraft.mortuorum.NightCrawlerEntity;
import net.thaumcraft.mortuorum.TeddyEntity;

/** As criaturas do Ars Mortuorum: o Rastejador da Noite, o Ursinho Animado e os quatro Isaac. */
public class MortuorumCreaturesGameTest {
    /** A dentada do Rastejador da Noite apodrece quem a leva. */
    @GameTest
    public void theNightCrawlerBiteWithers(GameTestHelper helper) {
        NightCrawlerEntity bicho = helper.spawn(MortuorumEntities.NIGHT_CRAWLER, new BlockPos(2, 2, 2));
        if (Math.abs(bicho.getAttributeValue(Attributes.MAX_HEALTH) - 35.0) > 0.001) {
            helper.fail("ele tem trinta e cinco de vida");
        }
        var alvo = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(2, 2, 3));
        bicho.doHurtTarget(helper.getLevel(), alvo);
        if (!alvo.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
            helper.fail("quem leva a dentada devia ficar a definhar");
        }
        bicho.discard();
        alvo.discard();
        helper.succeed();
    }

    /** O clique roda os três feitios do ursinho, e o terceiro volta ao primeiro. */
    @GameTest
    public void theTeddyCyclesItsThreeStates(GameTestHelper helper) {
        TeddyEntity ursinho = helper.spawn(MortuorumEntities.TEDDY, new BlockPos(2, 2, 2));
        var dono = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        if (ursinho.state() != TeddyEntity.State.WALKING) helper.fail("ele começa andando");
        ursinho.mobInteract(dono, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (ursinho.state() != TeddyEntity.State.DEFENDING) helper.fail("o primeiro clique põe-no de guarda");
        ursinho.mobInteract(dono, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (ursinho.state() != TeddyEntity.State.SITTING) helper.fail("o segundo senta-o");
        if (!ursinho.isOrderedToSit()) helper.fail("e sentado ele fica mesmo sentado");
        ursinho.mobInteract(dono, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (ursinho.state() != TeddyEntity.State.WALKING) helper.fail("e o terceiro põe-no a andar outra vez");
        ursinho.discard();
        helper.succeed();
    }

    /** Isaac não morre de uma vez: cada um deixa o seguinte no lugar dele. */
    @GameTest
    public void isaacFallsApartInPieces(GameTestHelper helper) {
        if (IsaacEntity.Kind.BLOOD.health != 75.0) helper.fail("o de sangue tem setenta e cinco de vida");
        if (IsaacEntity.Kind.HEAD.health != 40.0) helper.fail("e a cabeça, quarenta");
        if (IsaacEntity.Kind.BODY.cries) helper.fail("o corpo sem cabeça não chora");

        IsaacEntity inteiro = helper.spawn(MortuorumEntities.ISAAC_NORMAL, new BlockPos(2, 2, 2));
        if (inteiro.kind() != IsaacEntity.Kind.NORMAL) helper.fail("este é o inteiro");
        inteiro.die(inteiro.damageSources().generic());
        var deSangue = helper.getLevel().getEntitiesOfClass(IsaacEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(3.0),
                bicho -> bicho.kind() == IsaacEntity.Kind.BLOOD);
        if (deSangue.size() != 1) helper.fail("do inteiro devia levantar-se o de sangue; achei " + deSangue.size());

        // e do de sangue, a cabeça e o corpo
        if (!deSangue.isEmpty()) {
            IsaacEntity sangue = deSangue.getFirst();
            sangue.die(sangue.damageSources().generic());
            var restos = helper.getLevel().getEntitiesOfClass(IsaacEntity.class,
                    new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(4.0),
                    bicho -> bicho.kind() == IsaacEntity.Kind.HEAD || bicho.kind() == IsaacEntity.Kind.BODY);
            if (restos.size() != 2) helper.fail("do de sangue ficam de pé a cabeça e o corpo; achei " + restos.size());
        }

        for (IsaacEntity bicho : helper.getLevel().getEntitiesOfClass(IsaacEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(6.0))) {
            bicho.discard();
        }
        helper.succeed();
    }

    /** A lágrima de sangue corta o dobro da de água. */
    @GameTest
    public void theBloodTearHurtsMore(GameTestHelper helper) {
        var água = MortuorumEntities.TEAR.create(helper.getLevel(),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        var sangue = MortuorumEntities.TEAR_BLOOD.create(helper.getLevel(),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (água == null || sangue == null) helper.fail("as duas lágrimas deviam existir");
        else {
            if (água.damage() != 3.0f) helper.fail("a de água tira três");
            if (sangue.damage() != 6.0f) helper.fail("e a de sangue, seis");
            if (água.isBlood()) helper.fail("a de água não é de sangue");
            if (!sangue.isBlood()) helper.fail("e a de sangue é");
            água.discard();
            sangue.discard();
        }
        helper.succeed();
    }

    /** A tabela dos órgãos que caem de quem morre é a do original: sete em cem, e quatro delas de músculo. */
    @GameTest
    public void theOrganDropTableMatches(GameTestHelper helper) {
        int quantos = 0;
        int músculo = 0;
        for (int sorte = 0; sorte < 100; sorte++) {
            String órgão = MortuorumEvents.organOf(sorte);
            if (órgão == null) continue;
            quantos++;
            if (órgão.equals("muscle")) músculo++;
            if (!MortuorumItems.ORGANS.containsKey(órgão)) helper.fail("não há órgão chamado " + órgão);
        }
        if (quantos != 7) helper.fail("são sete em cem; achei " + quantos);
        if (músculo != 4) helper.fail("e quatro delas são músculo; achei " + músculo);
        helper.succeed();
    }

    /** O Coração de Alma chama um dos três, e se gasta. */
    @GameTest
    public void theSoulHeartCallsOneOfThree(GameTestHelper helper) {
        BlockPos chão = new BlockPos(2, 1, 2);
        helper.setBlock(chão, net.minecraft.world.level.block.Blocks.STONE);
        var quemChama = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack coração = new ItemStack(MortuorumItems.SOUL_HEART);
        var alvo = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(chão)),
                net.minecraft.core.Direction.UP, helper.absolutePos(chão), false);
        MortuorumItems.SOUL_HEART.useOn(new net.minecraft.world.item.context.UseOnContext(
                helper.getLevel(), quemChama, net.minecraft.world.InteractionHand.MAIN_HAND, coração, alvo));

        var nascidos = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(chão)).inflate(3.0),
                bicho -> bicho.getType() == MortuorumEntities.ISAAC_NORMAL
                        || bicho.getType() == MortuorumEntities.TEDDY
                        || bicho.getType() == MortuorumEntities.NIGHT_CRAWLER);
        if (nascidos.size() != 1) helper.fail("devia nascer um dos três; achei " + nascidos.size());
        if (!coração.isEmpty()) helper.fail("e o coração devia gastar-se");
        for (var bicho : nascidos) bicho.discard();
        helper.succeed();
    }
}
