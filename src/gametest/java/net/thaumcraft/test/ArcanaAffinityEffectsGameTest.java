package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.AffinityData;
import net.thaumcraft.arcana.AffinityEffects;
import net.thaumcraft.arcana.AffinityPools;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellCast;

/**
 * O que ter Afinidade faz de você: a velocidade, a vida, o dano que chega e o dano que sai.
 *
 * <p>E, acima de tudo, o <b>preço</b>: quase toda Afinidade tem um, e há provas para eles também.
 */
public class ArcanaAffinityEffectsGameTest {
    // ------------------------------------------------------------------ a velocidade

    /** A Natureza cria raiz e o Relâmpago acelera: os dois mexem na mesma conta, em sentidos opostos. */
    @GameTest(maxTicks = 60)
    public void natureSlowsAndLightningSpeeds(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        double base = quem.getAttributeValue(Attributes.MOVEMENT_SPEED);

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.NATURE, 60.0f));
        AffinityEffects.tick(quem);
        double comRaiz = quem.getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (comRaiz >= base) helper.fail("meia Natureza devia atrasar, e deu " + comRaiz);

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.LIGHTNING, 70.0f));
        AffinityEffects.tick(quem);
        double comRelâmpago = quem.getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (comRelâmpago <= base) helper.fail("0,7 de Relâmpago devia acelerar, e deu " + comRelâmpago);

        // e tirar a Afinidade devolve a velocidade de sempre
        AffinityData.set(quem, AffinityData.NONE);
        AffinityEffects.tick(quem);
        if (Math.abs(quem.getAttributeValue(Attributes.MOVEMENT_SPEED) - base) > 1.0e-6) {
            helper.fail("sem Afinidade nenhuma, a velocidade volta ao que era");
        }
        limpa(quem);
        helper.succeed();
    }

    /**
     * O <b>sangue frio</b> do Gelo: quem é de Gelo anda devagar <i>fora</i> do gelo, e normal em cima dele.
     *
     * <p>É o preço mais barato da roda — basta um décimo de Gelo para ele valer — e o único que se resolve
     * andando no lugar certo.
     */
    @GameTest(maxTicks = 60)
    public void coldBloodedOnlyBitesOffIce(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        double base = quem.getAttributeValue(Attributes.MOVEMENT_SPEED);
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.ICE, 20.0f));

        AffinityEffects.tick(quem);
        if (quem.getAttributeValue(Attributes.MOVEMENT_SPEED) >= base) {
            helper.fail("fora do gelo, o sangue frio atrasa");
        }

        // e em cima do gelo, não
        BlockPos gelo = new BlockPos(2, 2, 2);
        helper.setBlock(gelo, Blocks.ICE.defaultBlockState());
        var centro = net.minecraft.world.phys.Vec3.atBottomCenterOf(helper.absolutePos(gelo.above()));
        quem.snapTo(centro.x, centro.y, centro.z, 0.0f, 0.0f);
        AffinityEffects.tick(quem);
        if (Math.abs(quem.getAttributeValue(Attributes.MOVEMENT_SPEED) - base) > 1.0e-6) {
            helper.fail("em cima do gelo, não atrasa");
        }

        helper.setBlock(gelo, Blocks.AIR.defaultBlockState());
        limpa(quem);
        helper.succeed();
    }

    // ------------------------------------------------------------------ o preço

    /**
     * As fraquezas <b>param no 0,9</b>, e essa é a parte que o original acerta.
     *
     * <p>Quem é de Água perde um quarto da vida <b>quando arde</b> — mas só de 0,5 a 0,9. Quem chegou ao fim
     * passou da parte que dói, e é isso que faz valer a pena ir até lá em vez de ficar no meio. As outras
     * fraquezas — a da água e a do sol — seguem a mesma janela.
     *
     * <p>A prova usa o fogo e não a água de propósito: estar molhado só vira verdade depois do tique de
     * física, e um jogador posto à mão dentro de uma poça ainda não sabe que está nela. Arder é imediato.
     */
    @GameTest(maxTicks = 60)
    public void theWeaknessStopsAtNineTenths(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        float base = quem.getMaxHealth();

        // ardendo, com Água no meio da escala: perde um quarto
        quem.igniteForSeconds(5.0f);
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.WATER, 70.0f));
        AffinityEffects.tick(quem);
        float noMeio = quem.getMaxHealth();
        if (noMeio >= base) helper.fail("0,7 de Água ardendo devia tirar vida, e deu " + noMeio);
        if (Math.abs(noMeio - base * 0.75f) > 0.01f) {
            helper.fail("e tirar um quarto dela: " + noMeio + " de " + base);
        }

        // e no fim da escala, não
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.WATER, 95.0f));
        AffinityEffects.tick(quem);
        if (Math.abs(quem.getMaxHealth() - base) > 0.01f) {
            helper.fail("0,95 de Água já passou da fraqueza, e ficou em " + quem.getMaxHealth());
        }

        // e sem fogo nenhum, também não
        quem.clearFire();
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.WATER, 70.0f));
        AffinityEffects.tick(quem);
        if (Math.abs(quem.getMaxHealth() - base) > 0.01f) {
            helper.fail("sem arder, a fraqueza não vale");
        }

        limpa(quem);
        AffinityEffects.tick(quem);
        helper.succeed();
    }

    /** O Arcano leva <b>dez por cento a mais</b> de tudo: é o preço de pagar menos mana. */
    @GameTest(maxTicks = 60)
    public void arcaneTakesMoreDamage(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        var fonte = helper.getLevel().damageSources().generic();

        AffinityData.set(quem, AffinityData.NONE);
        float nu = AffinityEffects.hurt(quem, fonte, 10.0f);
        if (Math.abs(nu - 10.0f) > 0.01f) helper.fail("sem Afinidade, dez é dez");

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.ARCANE, 50.0f));
        float comArcano = AffinityEffects.hurt(quem, fonte, 10.0f);
        if (Math.abs(comArcano - 11.0f) > 0.01f) {
            helper.fail("com Arcano devia levar onze, e levou " + comArcano);
        }

        // e a Terra amortece, no sentido contrário
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.EARTH, 100.0f));
        float comTerra = AffinityEffects.hurt(quem, fonte, 10.0f);
        if (comTerra >= 10.0f) helper.fail("com Terra cheia devia levar menos, e levou " + comTerra);

        limpa(quem);
        helper.succeed();
    }

    /** E o Arcano paga menos: cinco por cento de desconto na mana e no desgaste. */
    @GameTest(maxTicks = 60)
    public void arcanePaysLess(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        AffinityData.set(quem, AffinityData.NONE);
        if (AffinityEffects.manaDiscount(quem) != 1.0f) helper.fail("sem Arcano, preço cheio");

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.ARCANE, 40.0f));
        if (AffinityEffects.manaDiscount(quem) != 1.0f) helper.fail("e abaixo de meio, também");

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.ARCANE, 60.0f));
        if (AffinityEffects.manaDiscount(quem) != 0.95f) helper.fail("acima de meio, cinco por cento a menos");

        // e o desconto chega mesmo ao lançamento
        Mana.set(quem, Mana.NONE.withLevel(40));
        Mana.set(quem, Mana.of(quem).withMana(Mana.of(quem).maxMana()));
        float tinha = Mana.of(quem).mana();
        Spell cura = Spell.of(Shapes.SELF, Essences.HEAL);
        quem.setHealth(quem.getMaxHealth() - 6.0f);

        var saiu = SpellCast.cast(helper.getLevel(), cura, quem, null, quem.getEyePosition());
        if (!saiu.ok()) {
            helper.fail("a cura devia pegar, e deu " + saiu);
            limpa(quem);
            return;
        }
        float gastou = tinha - Mana.of(quem).mana();
        float cheio = cura.manaCost(quem, null);
        if (Math.abs(gastou - cheio * 0.95f) > 0.05f) {
            helper.fail("devia ter gasto 95% de " + cheio + ", e gastou " + gastou);
        }

        limpa(quem);
        helper.succeed();
    }

    /** Matar cobra caro a quem é de Vida: cegueira, fome, cansaço e fraqueza. */
    @GameTest(maxTicks = 60)
    public void lifeMakesKillingHurt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.LIFE, 70.0f));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        AffinityEffects.killed(porco, level.damageSources().playerAttack(quem));

        if (!quem.hasEffect(MobEffects.BLINDNESS)) helper.fail("matar cega quem é de Vida");
        if (!quem.hasEffect(MobEffects.WEAKNESS)) helper.fail("e enfraquece");

        // e um morto-vivo não conta, porque já estava morto
        quem.removeAllEffects();
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 2));
        AffinityEffects.killed(zumbi, level.damageSources().playerAttack(quem));
        if (quem.hasEffect(MobEffects.BLINDNESS)) helper.fail("mas um zumbi não cobra nada");

        quem.removeAllEffects();
        limpa(quem);
        helper.succeed();
    }

    // ------------------------------------------------------------------ pular, cair e bater

    /** O Ar cai leve e a Terra cai pesado. */
    @GameTest(maxTicks = 60)
    public void airFallsLightAndEarthFallsHeavy(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.AIR, 100.0f));
        if (Math.abs(AffinityEffects.fall(quem, 5.0f) - 3.0f) > 0.01f) {
            helper.fail("o Ar cheio tira dois blocos de queda");
        }
        // e não passa do chão
        if (AffinityEffects.fall(quem, 1.0f) != 0.0f) helper.fail("e a queda não fica negativa");

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.EARTH, 100.0f));
        if (Math.abs(AffinityEffects.fall(quem, 5.0f) - 6.25f) > 0.01f) {
            helper.fail("a Terra cheia soma 1,25 à queda");
        }

        limpa(quem);
        helper.succeed();
    }

    /** O Ar pula mais alto. */
    @GameTest(maxTicks = 60)
    public void airJumpsHigher(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.AIR, 40.0f));
        AffinityEffects.jump(quem);
        if (quem.getDeltaMovement().y != 0.0) helper.fail("abaixo de meio Ar, o pulo é o de sempre");

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.AIR, 100.0f));
        AffinityEffects.jump(quem);
        if (Math.abs(quem.getDeltaMovement().y - 0.35) > 0.001) {
            helper.fail("o Ar cheio soma 0,35 ao pulo, e somou " + quem.getDeltaMovement().y);
        }

        quem.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        limpa(quem);
        helper.succeed();
    }

    /** Quem é de Natureza devolve espinhos, e quem é de Gelo devolve lentidão. */
    @GameTest(maxTicks = 60)
    public void natureAndIceStrikeBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        float tinha = porco.getHealth();

        AffinityData.set(quem, AffinityData.NONE
                .with(Affinity.NATURE, 100.0f).with(Affinity.ICE, 100.0f));
        AffinityEffects.hurt(quem, level.damageSources().mobAttack(porco), 5.0f);

        if (porco.getHealth() >= tinha) helper.fail("a Natureza cheia devia espetar o porco");
        if (!porco.hasEffect(MobEffects.SLOWNESS)) helper.fail("e o Gelo cheio devia congelá-lo");

        limpa(quem);
        helper.succeed();
    }

    /** O Fogo queima de mão vazia — e só de mão vazia. */
    @GameTest(maxTicks = 60)
    public void fireOnlyPunchesBarehanded(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.FIRE, 90.0f));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        float comMão = AffinityEffects.hurt(porco, level.damageSources().playerAttack(quem), 2.0f);
        if (Math.abs(comMão - 5.0f) > 0.01f) helper.fail("de mão vazia soma três, e deu " + comMão);
        if (!porco.isOnFire()) helper.fail("e põe fogo em quem levou");

        // com uma espada na mão, não
        porco.clearFire();
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
        float comEspada = AffinityEffects.hurt(porco, level.damageSources().playerAttack(quem), 2.0f);
        if (Math.abs(comEspada - 2.0f) > 0.01f) helper.fail("com arma na mão, não soma nada");

        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                net.minecraft.world.item.ItemStack.EMPTY);
        limpa(quem);
        helper.succeed();
    }

    /** O Fogo resiste ao fogo e o Ender resiste à magia. */
    @GameTest(maxTicks = 60)
    public void fireResistsFireAndEnderResistsMagic(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.FIRE, 100.0f));
        float noFogo = AffinityEffects.hurt(quem, level.damageSources().inFire(), 10.0f);
        if (Math.abs(noFogo - 4.0f) > 0.01f) {
            helper.fail("o Fogo cheio tira sessenta por cento do fogo, e deu " + noFogo);
        }

        AffinityData.set(quem, AffinityData.NONE.with(Affinity.ENDER, 100.0f));
        float naMagia = AffinityEffects.hurt(quem, level.damageSources().magic(), 10.0f);
        if (Math.abs(naMagia - 2.5f) > 0.01f) {
            helper.fail("o Ender cheio tira três quartos da magia, e deu " + naMagia);
        }

        limpa(quem);
        helper.succeed();
    }

    // ------------------------------------------------------------------ os baldes

    /**
     * Os pingos se juntam até virar um: o {@code accumulatedLifeRegen}.
     *
     * <p>A Vida devolve 0,025 × profundidade por batida, e meio coração não existe em pedaço menor que um. O
     * balde guarda o resto.
     */
    @GameTest(maxTicks = 60)
    public void thePoolFillsBeforeItGives(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        AffinityPools.LIFE.set(quem, 0.0f);

        int deu = contaAsVezes(quem, 0.3f, 3);
        if (deu != 0) helper.fail("três pingos de 0,3 não enchem o balde");
        if (Math.abs(AffinityPools.LIFE.get(quem) - 0.9f) > 0.001f) {
            helper.fail("mas ficam guardados: " + AffinityPools.LIFE.get(quem));
        }

        deu = contaAsVezes(quem, 0.3f, 1);
        if (deu != 1) helper.fail("o quarto transborda e dá um");
        if (Math.abs(AffinityPools.LIFE.get(quem) - 0.2f) > 0.001f) {
            helper.fail("e sobra 0,2 no balde, e sobrou " + AffinityPools.LIFE.get(quem));
        }

        AffinityPools.LIFE.set(quem, 0.0f);
        limpa(quem);
        helper.succeed();
    }

    private static int contaAsVezes(ServerPlayer quem, float pingo, int quantos) {
        int[] deu = {0};
        for (int i = 0; i < quantos; i++) AffinityPools.LIFE.accumulate(quem, pingo, () -> deu[0]++);
        return deu[0];
    }

    private static void limpa(ServerPlayer quem) {
        AffinityData.set(quem, AffinityData.NONE);
        Mana.set(quem, Mana.NONE);
    }
}
