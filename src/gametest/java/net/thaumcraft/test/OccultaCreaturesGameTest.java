package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.occulta.EntEntity;
import net.thaumcraft.occulta.MandrakeEntity;
import net.thaumcraft.occulta.MinedrakeEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.WitchCropBlock;
import net.thaumcraft.occulta.WitchLogBlock;
import net.thaumcraft.research.EntityAspects;

import java.util.List;

/**
 * Os três bichos do ofício: a mandrágora que escapa, a de mina que estoura e o Ent que a tora acorda.
 *
 * <p>A chance de cada coisa acontecer já está conferida em outro lugar — a da fuga no
 * {@link OccultaCropsGameTest}, a da tora aqui mesmo, na conta. O que se prova aqui é o que acontece <b>quando</b>
 * acontece.
 */
public class OccultaCreaturesGameTest {
    /** O grito dela cega quem apanha o golpe — a não ser que a pessoa traga abafadores. */
    @GameTest
    public void theMandrakeScreamBlinds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MandrakeEntity bicho = helper.spawn(OccultaEntities.MANDRAKE, new BlockPos(2, 2, 2));
        if (Math.abs(bicho.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.65) > 0.001) {
            helper.fail("ela corre a 0,65, que é o passo do original");
        }
        if (!bicho.canBreatheUnderwater()) helper.fail("ela é raiz: respira debaixo da água");

        // primeiro de abafadores, que é como se anda perto dela: o grito não alcança quem os traz
        var quemLeva = helper.makeMockPlayer(GameType.SURVIVAL);
        quemLeva.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.EARMUFFS));
        if (!MandrakeEntity.wearsEarmuffs(quemLeva)) helper.fail("os abafadores se contam na cabeça");
        bicho.doHurtTarget(level, quemLeva);
        if (quemLeva.hasEffect(MobEffects.BLINDNESS)) helper.fail("com abafadores o grito não a alcança");
        if (!MandrakeEntity.wearsEarmuffs(quemLeva)) helper.fail("e os abafadores não se gastam com a pancada");

        // e sem eles, cega
        quemLeva.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        bicho.doHurtTarget(level, quemLeva);
        if (!quemLeva.hasEffect(MobEffects.BLINDNESS)) helper.fail("o grito dela cega quem apanha o golpe");
        var cegueira = quemLeva.getEffect(MobEffects.BLINDNESS);
        if (cegueira.getDuration() > MandrakeEntity.BLIND_TICKS) {
            helper.fail("a cegueira é de quinze segundos; veio de " + cegueira.getDuration() + " batidas");
        }
        if (cegueira.getAmplifier() != MandrakeEntity.BLIND_LEVEL) helper.fail("e é do segundo grau");
        quemLeva.removeEffect(MobEffects.BLINDNESS);

        bicho.discard();
        helper.succeed();
    }

    /** Arrancada fora de hora, a mandrágora sai do chão em vez de se deixar colher. */
    @GameTest
    public void theMandrakeGetsUpAndLeaves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        AABB perto = new AABB(onde).inflate(3.0);
        level.setBlockAndUpdate(onde.below(), Blocks.FARMLAND.defaultBlockState());
        WitchCropBlock mandrágora = (WitchCropBlock) OccultaBlocks.MANDRAKE;

        // colhe-se a planta feita até uma delas escapar; de noite é uma em dez, e mesmo assim é quase certo
        boolean escapou = false;
        for (int volta = 0; volta < 300 && !escapou; volta++) {
            level.setBlockAndUpdate(onde, mandrágora.getStateForAge(mandrágora.getMaxAge()));
            level.destroyBlock(onde, true);
            escapou = !level.getEntitiesOfClass(MandrakeEntity.class, perto).isEmpty();
        }
        if (!escapou) helper.fail("em trezentas colheitas, alguma mandrágora tinha de escapar");

        level.getEntitiesOfClass(MandrakeEntity.class, perto).forEach(Entity::discard);
        level.getEntitiesOfClass(ItemEntity.class, perto).forEach(Entity::discard);
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** O bulbo largado no chão vira bicho ao fim de três segundos. */
    @GameTest(maxTicks = 200)
    public void theBulbSproutsIntoAMinedrake(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 3, 2));
        AABB perto = new AABB(onde).inflate(3.0);
        ItemEntity bulbo = new ItemEntity(level, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                new ItemStack(OccultaItems.MINDRAKE_BULB));
        level.addFreshEntity(bulbo);

        helper.succeedWhen(() -> {
            List<MinedrakeEntity> bichos = level.getEntitiesOfClass(MinedrakeEntity.class, perto);
            if (bichos.isEmpty()) throw helper.assertionException("o bulbo ainda não virou bicho");
            if (!bulbo.isRemoved()) helper.fail("e o bulbo some quando ela nasce");
            bichos.forEach(Entity::discard);
        });
    }

    /** Ela não morde: estoura em quem alcança — e do chão queimado nasce uma flor. */
    @GameTest
    public void theMinedrakeBlowsUp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        MinedrakeEntity bicho = helper.spawn(OccultaEntities.MINEDRAKE, new BlockPos(2, 2, 2));
        if (Math.abs(bicho.getAttributeValue(Attributes.MAX_HEALTH) - 4.0) > 0.001) {
            helper.fail("ela tem quatro de vida; tem " + bicho.getAttributeValue(Attributes.MAX_HEALTH));
        }
        if (bicho.isTame()) helper.fail("nascida sem ninguém, ela não tem dono");

        // a flor que fica onde ela estourou (o estouro em si o jogo já sabe fazer)
        level.setBlockAndUpdate(onde.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        bicho.flower(level);
        var virou = level.getBlockState(onde);
        if (!virou.is(Blocks.POPPY) && !virou.is(Blocks.DANDELION)) {
            helper.fail("sobre terra nasce papoula ou dente-de-leão; ficou " + virou);
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());

        // e sobre pedra não nasce nada
        level.setBlockAndUpdate(onde.below(), Blocks.STONE.defaultBlockState());
        bicho.flower(level);
        if (!level.getBlockState(onde).isAir()) helper.fail("sobre pedra não nasce flor nenhuma");

        // o golpe dela é o estouro, e ela morre nele
        bicho.doHurtTarget(level, helper.makeMockPlayer(GameType.SURVIVAL));
        if (!bicho.isRemoved()) helper.fail("ela morre no estouro que dá");
        helper.succeed();
    }

    /** Onde o Ent pisa, a terra melhora. */
    @GameTest
    public void theEntFeedsTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde.below(), Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.WHEAT.defaultBlockState());

        EntEntity bicho = helper.spawn(OccultaEntities.ENT, new BlockPos(2, 2, 4));
        if (Math.abs(bicho.getAttributeValue(Attributes.MAX_HEALTH) - 200.0) > 0.001) {
            helper.fail("o Ent tem duzentos de vida; tem " + bicho.getAttributeValue(Attributes.MAX_HEALTH));
        }
        if (Math.abs(bicho.getAttributeValue(Attributes.ATTACK_DAMAGE) - 4.0) > 0.001) {
            helper.fail("e bate por quatro");
        }
        if (bicho.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) < 1.0) {
            helper.fail("e nada o empurra");
        }
        if (EntEntity.BONEMEAL_CHANCE != 300) helper.fail("ele aduba uma vez em trezentas batidas");
        bicho.discard();

        int antes = level.getBlockState(onde).getValue(CropBlock.AGE);
        EntEntity.feedGround(level, onde);
        int depois = level.getBlockState(onde).getValue(CropBlock.AGE);
        if (depois <= antes) helper.fail("o trigo debaixo do Ent devia crescer; ficou na idade " + depois);

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** A tora cortada acorda um Ent de vez em quando: uma em cem, mais uma por tora encostada, até cinco. */
    @GameTest
    public void theLogWakesAnEnt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        WitchLogBlock tora = (WitchLogBlock) OccultaBlocks.ROWAN_LOG;
        level.setBlockAndUpdate(onde, tora.defaultBlockState());

        if (Math.abs(tora.entChance(level, onde) - 0.01) > 1.0e-9) {
            helper.fail("uma tora sozinha é uma em cem; veio " + tora.entChance(level, onde));
        }
        level.setBlockAndUpdate(onde.north(), tora.defaultBlockState());
        level.setBlockAndUpdate(onde.south(), tora.defaultBlockState());
        if (Math.abs(tora.entChance(level, onde) - 0.03) > 1.0e-9) {
            helper.fail("com duas encostadas são três em cem; veio " + tora.entChance(level, onde));
        }
        for (var lado : net.minecraft.core.Direction.values()) {
            level.setBlockAndUpdate(onde.relative(lado), tora.defaultBlockState());
        }
        if (Math.abs(tora.entChance(level, onde) - WitchLogBlock.ENT_MAX) > 1.0e-9) {
            helper.fail("num bosque cerrado a conta para em cinco em cem; veio " + tora.entChance(level, onde));
        }

        // e o Ent que dali sai fica de pé no lugar que se lhe der
        EntEntity.spawn(level, onde.above(2));
        var acordados = level.getEntitiesOfClass(EntEntity.class, new AABB(onde).inflate(6.0));
        if (acordados.size() != 1) helper.fail("devia ter-se levantado um Ent; achei " + acordados.size());
        acordados.forEach(Entity::discard);

        for (var lado : net.minecraft.core.Direction.values()) {
            level.setBlockAndUpdate(onde.relative(lado), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Os três têm nome e o thaumômetro tem o que ler neles. */
    @GameTest
    public void theCreaturesAreKnown(GameTestHelper helper) {
        var mandrágora = EntityAspects.of(helper.spawn(OccultaEntities.MANDRAKE, new BlockPos(1, 2, 1)));
        if (mandrágora == null || mandrágora.getAmount(Aspects.PLANT) != 4
                || mandrágora.getAmount(Aspects.MAN) != 2) {
            helper.fail("a mandrágora é herba 4 e humanus 2; veio " + mandrágora);
        }
        var deMina = EntityAspects.of(helper.spawn(OccultaEntities.MINEDRAKE, new BlockPos(2, 2, 1)));
        if (deMina == null || deMina.getAmount(Aspects.FIRE) != 2 || deMina.getAmount(Aspects.ENTROPY) != 2) {
            helper.fail("a de mina leva ignis 2 e perditio 2 do estouro dela; veio " + deMina);
        }
        var ent = EntityAspects.of(helper.spawn(OccultaEntities.ENT, new BlockPos(3, 2, 1)));
        if (ent == null || ent.getAmount(Aspects.TREE) != 8) {
            helper.fail("o Ent é arbor 8; veio " + ent);
        }
        helper.getLevel().getEntitiesOfClass(Entity.class,
                new AABB(helper.absolutePos(new BlockPos(2, 2, 1))).inflate(4.0),
                bicho -> bicho instanceof MandrakeEntity || bicho instanceof MinedrakeEntity
                        || bicho instanceof EntEntity).forEach(Entity::discard);
        helper.succeed();
    }
}
