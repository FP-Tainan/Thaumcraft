package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaHurt;
import net.thaumcraft.occulta.brew.BrewRegistry;

/**
 * A segunda leva de poções do ofício: as vinte e três que faltavam.
 *
 * <p>A prova que carrega a fatia é a do <b>fogo puxado para os dois lados</b>: o Enregelado tira e o Enrolado
 * em Vinha multiplica, e as duas se perguntam no mesmo instante. Se a ordem entre elas se perder, as duas
 * param de fazer sentido.
 */
public class OccultaPotionsGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números delas são os do original. */
    @GameTest(maxTicks = 20)
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (OccultaHurt.ABSORVE != 0.2f) helper.fail("Absorver Magia come um quinto por grau");
        if (OccultaHurt.REFLETE != 0.1f) helper.fail("e Refletir Dano manda um décimo");
        if (OccultaHurt.ALCANCE_DO_EMPURRÃO != 9.0) helper.fail("e o empurrão alcança três blocos");
        helper.succeed();
    }

    /** <b>O gelo tira do fogo e a vinha multiplica</b> — e as duas no mesmo golpe. */
    @GameTest(maxTicks = 40)
    public void iceBluntsFireAndVineSharpensIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var fogo = level.damageSources().inFire();

        var gelado = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        gelado.addEffect(new MobEffectInstance(OccultaEffects.CHILLED, 200, 0));
        float comGelo = OccultaHurt.hurt(gelado, fogo, 6.0f);
        if (comGelo != 5.0f) helper.fail("o primeiro grau do gelo tira um: deu " + comGelo);

        /*
         * O terceiro grau tira três — e o que muda nele não é o quanto, é o <b>piso</b>: até ao segundo grau
         * o fogo sempre deixa um ponto, e do terceiro em diante pode chegar a zero.
         */
        var muitoGelado = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 2));
        muitoGelado.addEffect(new MobEffectInstance(OccultaEffects.CHILLED, 200, 2));
        if (OccultaHurt.hurt(muitoGelado, fogo, 6.0f) != 3.0f) {
            helper.fail("o terceiro grau tira três: deu " + OccultaHurt.hurt(muitoGelado, fogo, 6.0f));
        }
        if (OccultaHurt.hurt(muitoGelado, fogo, 2.0f) != 0.0f) {
            helper.fail("e um fogo fraco não passa de todo");
        }
        if (OccultaHurt.hurt(gelado, fogo, 2.0f) != 1.0f) {
            helper.fail("mas no primeiro grau sobra sempre um ponto");
        }

        var enrolado = helper.spawn(EntityTypes.PIG, new BlockPos(6, 2, 2));
        enrolado.addEffect(new MobEffectInstance(OccultaEffects.WRAPPED_IN_VINE, 200, 1));
        float comVinha = OccultaHurt.hurt(enrolado, fogo, 3.0f);
        if (comVinha != 6.0f) helper.fail("a vinha do segundo grau dobra: deu " + comVinha);

        // e o que não é fogo não muda
        if (OccultaHurt.hurt(gelado, level.damageSources().generic(), 6.0f) != 6.0f) {
            helper.fail("mas nenhuma das duas mexe no que não é fogo");
        }

        gelado.discard();
        muitoGelado.discard();
        enrolado.discard();
        helper.succeed();
    }

    /** <b>Absorver Magia</b> come um quinto por grau, e só do que é magia. */
    @GameTest(maxTicks = 40)
    public void absorbMagicEatsOnlyMagic(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.addEffect(new MobEffectInstance(OccultaEffects.ABSORB_MAGIC, 200, 1));

        float sobrou = OccultaHurt.hurt(porco, level.damageSources().magic(), 10.0f);
        if (Math.abs(sobrou - 6.0f) > 0.001f) {
            helper.fail("o segundo grau come dois quintos: sobraram " + sobrou);
        }
        if (OccultaHurt.hurt(porco, level.damageSources().generic(), 10.0f) != 10.0f) {
            helper.fail("mas não come o que não é magia");
        }

        porco.discard();
        helper.succeed();
    }

    /** <b>Refletir Dano</b> manda parte de volta — e o que volta sai do que chegou. */
    @GameTest(maxTicks = 40)
    public void reflectDamageSendsPartBack(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quemLeva = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        var quemBate = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 5));
        quemLeva.addEffect(new MobEffectInstance(OccultaEffects.REFLECT_DAMAGE, 200, 0));
        float tinha = quemBate.getHealth();

        float sobrou = OccultaHurt.hurt(quemLeva, level.damageSources().mobAttack(quemBate), 10.0f);
        if (sobrou >= 10.0f) helper.fail("o que volta sai do que chega: sobraram " + sobrou);
        if (quemBate.getHealth() >= tinha) helper.fail("e quem bateu leva de volta");

        quemLeva.discard();
        quemBate.discard();
        helper.succeed();
    }

    /** <b>Repelir Agressor</b> empurra quem bate de perto. */
    @GameTest(maxTicks = 40)
    public void repellAttackerPushes(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quemLeva = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        var quemBate = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 3));
        quemLeva.addEffect(new MobEffectInstance(OccultaEffects.REPELL_ATTACKER, 200, 1));
        quemBate.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        OccultaHurt.hurt(quemLeva, level.damageSources().mobAttack(quemBate), 4.0f);
        if (quemBate.getDeltaMovement().lengthSqr() <= 0.0) {
            helper.fail("quem bate de perto sai de perto");
        }

        quemLeva.discard();
        quemBate.discard();
        helper.succeed();
    }

    /** <b>Não Sentir Dor</b> paga com fome antes de pagar com vida. */
    @GameTest(maxTicks = 40)
    public void feelNoPainPaysWithHunger(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.getFoodData().setFoodLevel(20);
        quem.addEffect(new MobEffectInstance(OccultaEffects.FEEL_NO_PAIN, 200, 0));

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        float sobrou = OccultaHurt.hurt(quem, level.damageSources().mobAttack(zumbi), 6.0f);
        if (sobrou != 0.0f) helper.fail("com fome cheia o golpe não chega à vida: sobraram " + sobrou);
        if (quem.getFoodData().getFoodLevel() != 14) {
            helper.fail("e a fome paga por ele: ficou " + quem.getFoodData().getFoodLevel());
        }

        // e o que não vem de bicho nem de gente dói na mesma
        if (OccultaHurt.hurt(quem, level.damageSources().fall(), 6.0f) != 6.0f) {
            helper.fail("mas a queda dói na mesma");
        }

        zumbi.discard();
        helper.succeed();
    }

    /** <b>A Corda Mortal</b> não faz nada, nada, nada — e então mata. */
    @GameTest(maxTicks = 40)
    public void theMortalCoilKillsWhenItEnds(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var corda = OccultaEffects.MORTAL_COIL.value();

        if (corda.shouldApplyEffectTickThisTick(200, 0)) helper.fail("ela não faz nada no meio");
        if (!corda.shouldApplyEffectTickThisTick(1, 0)) helper.fail("e faz tudo no fim");

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        corda.applyEffectTick(level, porco, 0);
        if (porco.isAlive()) helper.fail("e o que ela faz no fim é matar");
        helper.succeed();
    }

    /** <b>A Aura Infernal</b> queima quem está perto, e não quem a tem. */
    @GameTest(maxTicks = 40)
    public void theHellishAuraBurnsWhoeverIsNear(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quemTem = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        var aoLado = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 4));
        var longe = helper.spawn(EntityTypes.PIG, new BlockPos(7, 2, 7));

        quemTem.setHealth(quemTem.getMaxHealth());
        aoLado.setHealth(aoLado.getMaxHealth());
        longe.setHealth(longe.getMaxHealth());
        float tinha = quemTem.getHealth();
        float tinhaOLado = aoLado.getHealth();
        float tinhaLonge = longe.getHealth();

        OccultaEffects.HELLISH_AURA.value().applyEffectTick(level, quemTem, 1);

        if (quemTem.getHealth() != tinha) helper.fail("quem a tem não arde");
        if (aoLado.getHealth() >= tinhaOLado) helper.fail("quem está ao lado, sim");
        if (longe.getHealth() != tinhaLonge) helper.fail("e quem está longe não");

        quemTem.discard();
        aoLado.discard();
        longe.discard();
        helper.succeed();
    }

    /** <b>A Mal Ajustada</b> despe quem a tem — e só perto do fim. */
    @GameTest(maxTicks = 40)
    public void theIllFittingStripsArmour(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var má = OccultaEffects.ILL_FITTING.value();

        if (má.shouldApplyEffectTickThisTick(600, 0)) helper.fail("ela espera pelo fim");
        if (!má.shouldApplyEffectTickThisTick(15, 0)) helper.fail("e então começa");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 3));
        for (var casa : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            zumbi.setItemSlot(casa, new ItemStack(Items.IRON_HELMET));
        }

        // em quatro voltas ela tira pelo menos uma peça
        for (int volta = 0; volta < 16; volta++) má.applyEffectTick(level, zumbi, 0);

        int aindaTem = 0;
        for (var casa : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!zumbi.getItemBySlot(casa).isEmpty()) aindaTem++;
        }
        if (aindaTem == 4) helper.fail("em dezesseis voltas alguma peça devia ter caído");

        zumbi.discard();
        helper.succeed();
    }

    /** <b>Redimensionar</b> mexe na escala, que é o que o original fazia por reflexão. */
    @GameTest(maxTicks = 40)
    public void resizingChangesTheScale(GameTestHelper helper) {
        piso(helper);
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        double era = porco.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);

        porco.addEffect(new MobEffectInstance(OccultaEffects.RESIZING, 200, 0));
        double encolheu = porco.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (encolheu >= era) helper.fail("o primeiro grau encolhe: era " + era + ", ficou " + encolheu);

        porco.removeEffect(OccultaEffects.RESIZING);
        porco.addEffect(new MobEffectInstance(OccultaEffects.RESIZING, 200, 1));
        double cresceu = porco.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (cresceu <= era) helper.fail("e o segundo aumenta: ficou " + cresceu);

        porco.removeEffect(OccultaEffects.RESIZING);
        if (porco.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.SCALE) != era) {
            helper.fail("e acabando ela, o bicho volta ao que era");
        }

        porco.discard();
        helper.succeed();
    }

    /** <b>A Reencarnação</b> levanta outra coisa do corpo. */
    @GameTest(maxTicks = 60)
    public void reincarnateRaisesSomethingElse(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        porco.addEffect(new MobEffectInstance(OccultaEffects.REINCARNATE, 200, 0));

        var volta = new AABB(helper.absolutePos(new BlockPos(3, 2, 3))).inflate(3.0);
        int aranhasAntes = level.getEntitiesOfClass(
                net.minecraft.world.entity.monster.spider.Spider.class, volta).size();

        porco.hurtServer(level, level.damageSources().generic(), 1000.0f);

        helper.succeedWhen(() -> {
            int agora = level.getEntitiesOfClass(
                    net.minecraft.world.entity.monster.spider.Spider.class, volta).size();
            if (agora <= aranhasAntes) {
                throw helper.assertionException("de um bicho sai uma aranha");
            }
            for (var aranha : level.getEntitiesOfClass(
                    net.minecraft.world.entity.monster.spider.Spider.class, volta)) {
                aranha.discard();
            }
        });
    }

    /** <b>E o leite não tira estas</b>, como no original. */
    @GameTest(maxTicks = 20)
    public void theMilkDoesNotTakeThese(GameTestHelper helper) {
        for (var qual : new net.minecraft.core.Holder[]{
                OccultaEffects.COLORFUL, OccultaEffects.DISEASED, OccultaEffects.FEEL_NO_PAIN,
                OccultaEffects.ILL_FITTING, OccultaEffects.MORTAL_COIL, OccultaEffects.PARALYSIS,
                OccultaEffects.QUEASY, OccultaEffects.WRAPPED_IN_VINE}) {
            @SuppressWarnings("unchecked")
            var efeito = (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>) qual;
            if (!OccultaEffects.incurable(efeito)) {
                helper.fail("o leite não tira " + efeito.getRegisteredName());
                return;
            }
        }
        // mas tira as que são só boas
        if (OccultaEffects.incurable(OccultaEffects.FORTUNE)) helper.fail("mas tira a Fortuna");
        if (OccultaEffects.incurable(OccultaEffects.CHILLED)) helper.fail("e o Enregelado");
        helper.succeed();
    }

    /** <b>E todas elas se cozem</b>: cada uma tem o seu ingrediente no caldeirão. */
    @GameTest(maxTicks = 20)
    public void everyNewBrewIsReachable(GameTestHelper helper) {
        var novas = new net.minecraft.core.Holder[]{
                OccultaEffects.COLORFUL, OccultaEffects.FEEL_NO_PAIN, OccultaEffects.LOVE,
                OccultaEffects.DISEASED, OccultaEffects.ABSORB_MAGIC, OccultaEffects.REFLECT_DAMAGE,
                OccultaEffects.FORTUNE, OccultaEffects.REINCARNATE, OccultaEffects.RESIZING,
                OccultaEffects.BREWING_EXPERTISE, OccultaEffects.KEEP_INVENTORY,
                OccultaEffects.KEEP_EFFECTS_ON_DEATH};

        for (var qual : novas) {
            boolean achou = false;
            for (var ação : BrewRegistry.all().values()) {
                if (!(ação instanceof net.thaumcraft.occulta.brew.BrewActions.Potion poção)) continue;
                if (poção.effect() == qual) {
                    achou = true;
                    break;
                }
            }
            if (!achou) {
                helper.fail("falta o cozimento de " + qual.getRegisteredName());
                return;
            }
        }
        helper.succeed();
    }

    /** E o que não se coze é porque vem de outro lugar, e não por esquecimento. */
    @GameTest(maxTicks = 20)
    public void whatIsNotBrewedComesFromElsewhere(GameTestHelper helper) {
        // estas quatro não têm ingrediente: vêm de rito, de cozimento de lugar ou de bicho
        for (var qual : new net.minecraft.core.Holder[]{
                OccultaEffects.MORTAL_COIL, OccultaEffects.PARALYSIS,
                OccultaEffects.WORSHIP, OccultaEffects.QUEASY}) {
            if (qual == null) {
                helper.fail("mas existem");
                return;
            }
        }
        helper.succeed();
    }

    /** O empurrão e o reflexo não mexem em quem bate de longe — menos o reflexo bem forte. */
    @GameTest(maxTicks = 40)
    public void fromAfarMostOfThemDoNothing(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quemLeva = helper.spawn(EntityTypes.PIG, new BlockPos(3, 2, 3));
        var quemBate = helper.spawn(EntityTypes.SKELETON, new BlockPos(6, 2, 6));
        var flecha = new net.minecraft.world.entity.projectile.arrow.Arrow(level, quemBate,
                new ItemStack(Items.ARROW), null);
        LivingEntity atirador = quemBate;
        var deLonge = level.damageSources().arrow(flecha, atirador);

        quemLeva.addEffect(new MobEffectInstance(OccultaEffects.REFLECT_DAMAGE, 200, 0));
        if (OccultaHurt.hurt(quemLeva, deLonge, 10.0f) != 10.0f) {
            helper.fail("o reflexo fraco não devolve flecha");
        }

        quemLeva.removeEffect(OccultaEffects.REFLECT_DAMAGE);
        quemLeva.addEffect(new MobEffectInstance(OccultaEffects.REFLECT_DAMAGE, 200, 2));
        if (OccultaHurt.hurt(quemLeva, deLonge, 10.0f) >= 10.0f) {
            helper.fail("mas o forte, sim");
        }

        flecha.discard();
        quemLeva.discard();
        quemBate.discard();
        helper.succeed();
    }
}
