package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.leonard.LeonardEntity;

/**
 * O <b>Leonard</b>, que é o chefe do ramo e a luta mais escrita do mod.
 *
 * <p>A prova que carrega a fatia é a dos <b>tetos</b>: só golpe de gente lhe faz mal, e o teto piora
 * quanto mais perto do fim ele estiver. Errando essa conta, ou ele morre num golpe ou não morre nunca.
 */
public class OccultaLeonardGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>Os números dele são os do original.</b> */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (LeonardEntity.VIDA != 600.0) helper.fail("seiscentos de vida");
        if (LeonardEntity.ENTRADA != 150) helper.fail("e cento e cinquenta batidas de entrada");
        if (LeonardEntity.HEXA_A != 40) helper.fail("o Enrolamento alcança quarenta");
        if (LeonardEntity.TETO != 12.0f || LeonardEntity.TETO_NO_FIM != 4.0f
                || LeonardEntity.TETO_CRESCIDO != 1.0f) {
            helper.fail("e os tetos são doze, quatro e um");
        }
        if (LeonardEntity.FRAQUEZA != 15.0f || LeonardEntity.FRAQUEZA_CRESCIDO != 8.0f) {
            helper.fail("e a porta da fraqueza, quinze e oito");
        }
        helper.succeed();
    }

    /**
     * <b>A entrada:</b> ele aparece com um quarto da vida e invulnerável, e enche-se à frente de quem o
     * veio buscar.
     */
    @GameTest(maxTicks = 200)
    public void heArrivesWeakAndFillsUp(GameTestHelper helper) {
        piso(helper);
        var ele = helper.spawn(OccultaEntities.LEONARD, new BlockPos(3, 2, 3));
        ele.setHealth(ele.getMaxHealth());
        if (ele.entrando() != 0) helper.fail("posto à mão, ele não entra: entra chamado");

        ele.começaAEntrada();
        if (ele.entrando() != LeonardEntity.ENTRADA) helper.fail("a entrada dura cento e cinquenta");
        if (ele.getHealth() != ele.getMaxHealth() / 4.0f) {
            helper.fail("e começa com um quarto da vida: tem " + ele.getHealth());
        }

        /*
         * E a entrada <b>enche-o</b>: quinze goles de um vigésimo da vida, de dez em dez batidas, que
         * somam os três quartos que lhe faltam. A prova dá-lhe as cento e cinquenta batidas de uma vez.
         */
        helper.runAfterDelay(LeonardEntity.ENTRADA + 5, () -> {
            if (ele.entrando() != 0) helper.fail("passadas as cento e cinquenta, a entrada acabou");
            if (ele.getHealth() < ele.getMaxHealth() * 0.9f) {
                helper.fail("e ele está cheio: tem " + ele.getHealth());
            }
            ele.discard();
            helper.succeed();
        });
    }

    /**
     * <b>Os tetos</b>, que são o que faz a luta.
     *
     * <p>Acima de um quarto da vida, doze; abaixo, quatro; crescido, um. E só <b>golpe de gente</b>: a
     * flecha, o feitiço e o fogo passam por ele.
     */
    @GameTest(maxTicks = 40)
    public void onlyAPersonsBlowHurtsHimAndNeverTooMuch(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ele = helper.spawn(OccultaEntities.LEONARD, new BlockPos(3, 2, 3));
        ele.setHealth(ele.getMaxHealth());
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(5.5, 2, 5.5)));
        var golpe = level.damageSources().playerAttack(quem);

        float tinha = ele.getHealth();
        ele.hurtServer(level, golpe, 1000.0f);
        float tirou = tinha - ele.getHealth();
        if (Math.abs(tirou - LeonardEntity.TETO) > 0.01f) {
            helper.fail("mil de golpe tiram doze: tiraram " + tirou);
            ele.discard();
            return;
        }

        // o fogo não lhe toca
        tinha = ele.getHealth();
        ele.hurtServer(level, level.damageSources().inFire(), 1000.0f);
        if (ele.getHealth() != tinha) {
            helper.fail("e o fogo não lhe toca");
            ele.discard();
            return;
        }

        // abaixo de um quarto, quatro
        ele.setHealth(ele.getMaxHealth() * 0.2f);
        ele.invulnerableTime = 0;
        tinha = ele.getHealth();
        ele.hurtServer(level, golpe, 1000.0f);
        tirou = tinha - ele.getHealth();
        if (Math.abs(tirou - LeonardEntity.TETO_NO_FIM) > 0.01f) {
            helper.fail("no fim, quatro: tiraram " + tirou);
            ele.discard();
            return;
        }

        // e crescido, um
        ele.addEffect(new MobEffectInstance(OccultaEffects.RESIZING, 200, LeonardEntity.CRESCE));
        if (!ele.crescido()) helper.fail("o Redimensionar de grau três conta como crescido");
        ele.invulnerableTime = 0;
        tinha = ele.getHealth();
        ele.hurtServer(level, golpe, 1000.0f);
        tirou = tinha - ele.getHealth();
        if (Math.abs(tirou - LeonardEntity.TETO_CRESCIDO) > 0.01f) {
            helper.fail("crescido, um: tiraram " + tirou);
        }

        ele.discard();
        helper.succeed();
    }

    /**
     * <b>A porta da fraqueza</b>, que só abre abaixo de quatro décimos da vida.
     *
     * <p>É por ela que o Cozimento de Ferir Demônios entra, e é a única coisa que lhe tira quinze de uma
     * vez. Acima dos quatro décimos ela não abre.
     */
    @GameTest(maxTicks = 40)
    public void theWeaknessDoorOnlyOpensNearTheEnd(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var ele = helper.spawn(OccultaEntities.LEONARD, new BlockPos(3, 2, 3));
        ele.setHealth(ele.getMaxHealth());

        float tinha = ele.getHealth();
        ele.feriDeFraqueza(level, 100);
        if (ele.getHealth() != tinha) {
            helper.fail("cheio, a porta não abre");
            ele.discard();
            return;
        }

        ele.setHealth(ele.getMaxHealth() * 0.3f);
        ele.invulnerableTime = 0;
        tinha = ele.getHealth();
        ele.feriDeFraqueza(level, 100);
        float tirou = tinha - ele.getHealth();
        if (Math.abs(tirou - LeonardEntity.FRAQUEZA) > 0.01f) {
            helper.fail("abaixo de quatro décimos, quinze: tiraram " + tirou);
        }

        ele.discard();
        helper.succeed();
    }

    /**
     * <b>As quatro coisas que ele faz a quem escolhe</b>, e o Enrolamento que ele tira ao morrer.
     *
     * <p>Três em dez limpam as poções boas, três afundam, três enlouquecem e uma aquece. A prova chama
     * cada uma pelo número, que de outro jeito teria de sortear até cair nelas.
     */
    @GameTest(maxTicks = 40)
    public void thePunishmentsAreFourAndTheCoilComesOff(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        // as poções boas somem, e as ruins ficam
        quem.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.SPEED, 200));
        quem.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 200));
        LeonardEntity.castiga(level, quem, 0);
        if (quem.hasEffect(net.minecraft.world.effect.MobEffects.SPEED)) helper.fail("ele limpa as boas");
        if (!quem.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) helper.fail("e deixa as ruins");

        LeonardEntity.castiga(level, quem, 3);
        if (!quem.hasEffect(OccultaEffects.SINKING)) helper.fail("três em dez afundam");
        LeonardEntity.castiga(level, quem, 6);
        if (!quem.hasEffect(OccultaEffects.INSANITY)) helper.fail("três enlouquecem");
        LeonardEntity.castiga(level, quem, 9);
        if (!quem.hasEffect(OccultaEffects.OVERHEATING)) helper.fail("e uma aquece");

        // e o Enrolamento sai quando ele morre
        var ele = helper.spawn(OccultaEntities.LEONARD, new BlockPos(4, 2, 4));
        net.thaumcraft.research.Incurable.add(quem,
                new MobEffectInstance(OccultaEffects.MORTAL_COIL, LeonardEntity.HEXA_DURA));
        if (!quem.hasEffect(OccultaEffects.MORTAL_COIL)) helper.fail("posto, ele está lá");
        ele.tiraOEnrolamento(level, LeonardEntity.HEXA_A, LeonardEntity.HEXA_A);
        if (quem.hasEffect(OccultaEffects.MORTAL_COIL)) helper.fail("e sai quando ele morre");

        ele.discard();
        helper.succeed();
    }

    /** <b>O cozimento pega fogo</b> a quatro blocos dele: é a resposta dele a quem o envenena. */
    @GameTest(maxTicks = 40)
    public void theBrewBurnsAroundHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        // em cima do chão de pedra, porque fogo sem apoio apaga-se na mesma batida
        BlockPos nuvem = new BlockPos(4, 2, 3);
        helper.setBlock(nuvem, net.thaumcraft.occulta.OccultaBlocks.BREW_GAS);
        if (!helper.getBlockState(nuvem).is(net.thaumcraft.occulta.OccultaBlocks.BREW_GAS)) {
            helper.fail("a nuvem devia estar posta");
            return;
        }

        LeonardEntity.queimaOCozimento(level, helper.absolutePos(new BlockPos(3, 2, 3)));
        if (!helper.getBlockState(nuvem).is(Blocks.FIRE)) {
            helper.fail("e devia ter virado fogo");
            return;
        }
        helper.setBlock(nuvem, Blocks.AIR);
        helper.succeed();
    }

    /** <b>Os cinco feitiços dele</b> existem, e o sorteio sai sempre num deles. */
    @GameTest(maxTicks = 20)
    public void hisFiveSpellsAreThere(GameTestHelper helper) {
        for (int id : LeonardEntity.FEITIÇOS) {
            if (net.thaumcraft.occulta.symbol.Symbols.daquele(id) == null) {
                helper.fail("falta o símbolo " + id);
                return;
            }
        }
        int soma = 0;
        for (int peso : LeonardEntity.PESOS) soma += peso;
        if (soma != 21) helper.fail("os pesos somam vinte e um; somam " + soma);

        var sorte = helper.getLevel().getRandom();
        for (int volta = 0; volta < 200; volta++) {
            var qual = LeonardEntity.sorteia(sorte);
            if (qual == null) {
                helper.fail("o sorteio devia dar sempre um dos cinco");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>E o caldeirão chama-o.</b>
     *
     * <p>Chapéu de Bruxa por chave, cinco coisas dentro — e o que sai do caldeirão não é uma coisa, é um
     * bicho. É a única receita do original que faz isso.
     */
    @GameTest(maxTicks = 120)
    public void theCauldronCallsHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        // a panela sobe um, para a fogueira ter o chão de pedra por baixo
        BlockPos onde = new BlockPos(3, 3, 3);
        helper.setBlock(onde.below(), Blocks.FIRE);
        helper.setBlock(onde, net.thaumcraft.occulta.OccultaBlocks.WITCHES_CAULDRON);
        var panela = helper.getBlockEntity(onde,
                net.thaumcraft.occulta.WitchesCauldronBlockEntity.class);
        panela.fill(net.thaumcraft.occulta.WitchesCauldronBlockEntity.FULL);

        // ferve
        for (int volta = 0; volta < net.thaumcraft.occulta.WitchesCauldronBlockEntity.TICKS_TO_BOIL; volta++) {
            net.thaumcraft.occulta.WitchesCauldronBlockEntity.tick(level, helper.absolutePos(onde),
                    level.getBlockState(helper.absolutePos(onde)), panela);
        }
        if (!panela.isBoiling()) {
            helper.fail("a panela devia estar fervendo");
            return;
        }

        for (var coisa : new net.minecraft.world.item.Item[]{Items.NETHER_WART,
                OccultaItems.TEAR_OF_THE_GODDESS, OccultaItems.DIAMOND_VAPOUR, Items.DIAMOND,
                Items.NETHER_STAR}) {
            if (!panela.addItem(new ItemStack(coisa))) {
                helper.fail("a panela devia ter aceitado " + coisa);
                return;
            }
        }
        if (!panela.addItem(new ItemStack(OccultaItems.WITCH_HAT))) {
            helper.fail("e o chapéu devia disparar");
            return;
        }

        for (int volta = 0; volta <= net.thaumcraft.occulta.WitchesCauldronBlockEntity.RITUAL_TICKS; volta++) {
            net.thaumcraft.occulta.WitchesCauldronBlockEntity.tick(level, helper.absolutePos(onde),
                    level.getBlockState(helper.absolutePos(onde)), panela);
        }

        var vieram = level.getEntitiesOfClass(LeonardEntity.class,
                helper.getBounds().inflate(6.0), net.minecraft.world.entity.Entity::isAlive);
        if (vieram.isEmpty()) {
            helper.fail("e dali devia sair um Leonard");
            return;
        }
        var ele = vieram.getFirst();
        if (ele.entrando() <= 0) helper.fail("e sair entrando, invulnerável");
        if (ele.getHealth() > ele.getMaxHealth() / 2.0f) helper.fail("e fraco");
        for (var cada : vieram) cada.discard();
        helper.setBlock(onde, Blocks.AIR);
        helper.setBlock(onde.below(), Blocks.AIR);
        helper.succeed();
    }
}
