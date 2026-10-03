package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.wolf.Werewolf;
import net.thaumcraft.occulta.wolf.WerewolfPowers;
import net.thaumcraft.occulta.wolf.WerewolfStats;
import net.thaumcraft.occulta.wolf.WolfmanEntity;

import java.util.List;

/**
 * O que um lobisomem <b>faz</b>: o salto, a queda, o que lhe tiram, a armadura que ele rasga, a fome que a
 * caça lhe mata, o osso que sai da terra e o contágio.
 *
 * <p>São os poderes que a {@linkplain net.thaumcraft.occulta.wolf.WerewolfLadder escada} destranca, e o que
 * eles têm em comum é que <b>nenhum deles se escolhe</b>: todos vêm do grau e da forma, e todos param
 * sozinhos quando ele volta a ser gente. A prova de cada um tem esse par — o que ele faz, e o que ele deixa
 * de fazer.
 */
public class OccultaWerewolfPowersGameTest {
    /** Sete blocos: o que dói a quem é gente e o que um lobo de grau dez não sente. */
    private static final double SETE_BLOCOS = 7.0;

    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um lobo de grau dez, parado no chão e sem nada na mão. */
    private static Player lobo(GameTestHelper helper, int grau) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, grau);
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        return quem;
    }

    private static Player lobisomem(GameTestHelper helper, int grau) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(quem, grau);
        Werewolf.forma(quem, Werewolf.Forma.LOBISOMEM);
        return quem;
    }

    private static DamageSource golpe(GameTestHelper helper, net.minecraft.resources.ResourceKey<
            net.minecraft.world.damagesource.DamageType> qual) {
        return helper.getLevel().damageSources().source(qual);
    }

    // ------------------------------------------------------------------ o salto

    /**
     * <b>Um bicho pula mais alto</b>, e correndo o pulo também o atira para a frente.
     *
     * <p>E as duas coisas são <b>uma batida só</b>: o original soma ao impulso do pulo e larga.
     */
    @GameTest(maxTicks = 40)
    public void aBeastJumpsHigherAndFartherWhenSprinting(GameTestHelper helper) {
        Player quem = lobo(helper, 10);
        quem.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        WerewolfPowers.pula(quem);

        double sobe = quem.getDeltaMovement().y;
        if (Math.abs(sobe - WerewolfStats.LOBO[10].salto()) > 0.0001) {
            helper.fail("o pulo dele devia subir " + WerewolfStats.LOBO[10].salto() + " e subiu " + sobe);
        }
        if (quem.getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
            helper.fail("parado, ele só sobe");
        }

        // e correndo, ele também vai para a frente
        quem.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        quem.setSprinting(true);
        WerewolfPowers.pula(quem);
        if (quem.getDeltaMovement().horizontalDistance() < WerewolfStats.LOBO[10].arranco() - 0.0001) {
            helper.fail("correndo, o pulo tinha de o atirar para a frente");
        }

        // e de gente não há salto nenhum
        Werewolf.grau(quem, 0);
        quem.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        WerewolfPowers.pula(quem);
        if (quem.getDeltaMovement().lengthSqr() > 0.0001) helper.fail("gente pula como gente");
        helper.succeed();
    }

    // ------------------------------------------------------------------ a queda

    /** <b>A queda perdoa</b> o que o grau perdoa, e não mais do que isso. */
    @GameTest(maxTicks = 40)
    public void theFallForgivesWhatTheGradeForgives(GameTestHelper helper) {
        Player quem = lobo(helper, 10);
        int perdoa = WerewolfStats.LOBO[10].queda();
        if (perdoa <= 0) helper.fail("um lobo de grau dez devia perdoar queda");

        double sobrou = WerewolfPowers.queda(quem, 20.0);
        if (Math.abs(sobrou - (20.0 - perdoa)) > 0.0001) {
            helper.fail("devia ter perdoado " + perdoa + " e sobraram " + sobrou);
        }
        if (WerewolfPowers.queda(quem, 1.0) != 0.0) helper.fail("uma queda pequena não dói de todo");

        Werewolf.grau(quem, 0);
        if (WerewolfPowers.queda(quem, 20.0) != 20.0) helper.fail("gente cai o que caiu");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o que lhe tiram

    /**
     * <b>O teto da pancada</b>: nenhuma pancada tira mais do que o grau deixa — e é o único número da tabela
     * que melhora baixando.
     */
    @GameTest(maxTicks = 40)
    public void noBlowTakesMoreThanTheCap(GameTestHelper helper) {
        Player quem = lobo(helper, 10);
        var dá = WerewolfStats.LOBO[10];
        DamageSource genérico = golpe(helper, DamageTypes.GENERIC);

        float levou = WerewolfPowers.apanha(quem, genérico, 100.0f);
        if (levou > dá.tetoDaPancada() + 0.001f) {
            helper.fail("o teto dele é " + dá.tetoDaPancada() + " e a pancada valeu " + levou);
        }
        if (levou < WerewolfPowers.NUNCA_MENOS_DE) helper.fail("e nunca menos de meio");

        // a resistência subtrai, e não vale para fogo
        float pequena = WerewolfPowers.apanha(quem, genérico, dá.resistência() + 1.0f);
        if (Math.abs(pequena - 1.0f) > 0.001f) {
            helper.fail("a resistência devia ter tirado " + dá.resistência() + " e sobrou " + pequena);
        }
        float arde = WerewolfPowers.apanha(quem, golpe(helper, DamageTypes.IN_FIRE), 2.0f);
        if (arde < 2.0f - 0.001f) helper.fail("fogo não lhe dá resistência nenhuma");

        // e quatro danos passam inteiros
        for (var qual : List.of(DamageTypes.FALL, DamageTypes.DROWN, DamageTypes.IN_WALL,
                DamageTypes.FELL_OUT_OF_WORLD)) {
            if (WerewolfPowers.apanha(quem, golpe(helper, qual), 100.0f) != 100.0f) {
                helper.fail("a queda, o afogamento, a parede e o vazio não olham o grau dele");
            }
        }

        // e de gente o teto não existe
        Werewolf.grau(quem, 0);
        if (WerewolfPowers.apanha(quem, genérico, 100.0f) != 100.0f) helper.fail("gente apanha o que apanha");
        helper.succeed();
    }

    /** <b>A prata soma cinco</b> em vez de ser cortada, e é o que a faz valer a pena. */
    @GameTest(maxTicks = 40)
    public void silverAddsInsteadOfBeingCapped(GameTestHelper helper) {
        piso(helper);
        Player quem = lobo(helper, 10);
        Player outro = helper.makeMockServerPlayerInLevel();
        outro.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(OccultaItems.SILVER_SWORD));

        DamageSource prata = helper.getLevel().damageSources().playerAttack(
                (net.minecraft.server.level.ServerPlayer) outro);
        float levou = WerewolfPowers.apanha(quem, prata, 10.0f);
        if (levou <= WerewolfStats.LOBO[10].tetoDaPancada()) {
            helper.fail("a prata não devia ter sido cortada pelo teto, e valeu " + levou);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ a armadura rasgada

    /** <b>Do nono grau, de lobisomem</b>, cada golpe rasga uma peça da armadura de quem apanha. */
    @GameTest(maxTicks = 60)
    public void theArmourIsRentAtTheNinth(GameTestHelper helper) {
        piso(helper);
        Player quem = lobisomem(helper, 9);
        Player vítima = helper.makeMockServerPlayerInLevel();
        vestiu(vítima);

        // vinte golpes chegam de sobra para uma das quatro peças ter sido rasgada
        for (int n = 0; n < 20; n++) WerewolfPowers.rasga(helper.getLevel(), quem, vítima);
        if (!rasgada(vítima)) helper.fail("um lobisomem de grau nove devia ter rasgado a armadura dela");

        // e de grau oito, ou de lobo, não rasga nada
        Player fraco = lobisomem(helper, 8);
        Player outra = helper.makeMockServerPlayerInLevel();
        vestiu(outra);
        for (int n = 0; n < 20; n++) WerewolfPowers.rasga(helper.getLevel(), fraco, outra);
        if (rasgada(outra)) helper.fail("o nono grau é que rasga, e não o oitavo");

        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        Player terceira = helper.makeMockServerPlayerInLevel();
        vestiu(terceira);
        for (int n = 0; n < 20; n++) WerewolfPowers.rasga(helper.getLevel(), quem, terceira);
        if (rasgada(terceira)) helper.fail("um lobo não tem mãos para rasgar armadura");
        helper.succeed();
    }

    private static void vestiu(Player quem) {
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        quem.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        quem.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
    }

    private static boolean rasgada(Player quem) {
        for (var casa : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET)) {
            var peça = quem.getItemBySlot(casa);
            if (peça.isEmpty() || peça.getDamageValue() > 0) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ a fome que a caça mata

    /** <b>Do quarto grau</b>, cada coisa viva que ele mata o alimenta — e morto-vivo não alimenta. */
    @GameTest(maxTicks = 60)
    public void theHuntFeedsFromTheFourth(GameTestHelper helper) {
        piso(helper);
        Player quem = lobo(helper, 4);
        quem.getFoodData().setFoodLevel(2);

        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(3, 2, 3));
        WerewolfPowers.come(helper.getLevel(), quem, vaca);
        if (quem.getFoodData().getFoodLevel() <= 2) helper.fail("a caça devia tê-lo alimentado");
        vaca.discard();

        // morto-vivo não alimenta
        quem.getFoodData().setFoodLevel(2);
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 4));
        WerewolfPowers.come(helper.getLevel(), quem, zumbi);
        if (quem.getFoodData().getFoodLevel() != 2) helper.fail("carne podre não sustenta ninguém");
        zumbi.discard();

        // e abaixo do quarto grau, nem a caça
        Player cedo = lobo(helper, 3);
        cedo.getFoodData().setFoodLevel(2);
        var outra = helper.spawn(EntityTypes.COW, new BlockPos(5, 2, 5));
        WerewolfPowers.come(helper.getLevel(), cedo, outra);
        if (cedo.getFoodData().getFoodLevel() != 2) helper.fail("a fome só se mata do quarto grau");
        outra.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ o osso e o cavar

    /**
     * <b>O osso que sai da terra</b>: uma vez em vinte, e nunca mais por um minuto.
     *
     * <p>O relógio é o do mundo de fora, e por isso o segundo osso <b>não existe</b> dentro de uma prova — e
     * é exatamente isso que se prova aqui.
     */
    @GameTest(maxTicks = 60)
    public void theBoneComesOutOfTheDirtOncePerMinute(GameTestHelper helper) {
        piso(helper);
        Player quem = lobo(helper, 3);
        List<ItemStack> terra = List.of(new ItemStack(Items.DIRT));

        int achou = 0;
        for (int n = 0; n < 400; n++) {
            var mais = WerewolfPowers.osso(helper.getLevel(), quem, terra);
            if (mais == null) continue;
            achou++;
            if (mais.stream().noneMatch(c -> c.is(Items.BONE))) helper.fail("o que sai da terra é osso");
        }
        if (achou == 0) helper.fail("quatrocentas cavadas sem um osso é sorte demais");
        if (achou > 1) helper.fail("o minuto devia ter travado o segundo, e saíram " + achou);

        // e nada disto vale a quem não é lobo, nem à queda que não é só terra
        Player gente = helper.makeMockServerPlayerInLevel();
        for (int n = 0; n < 100; n++) {
            if (WerewolfPowers.osso(helper.getLevel(), gente, terra) != null) {
                helper.fail("gente não acha osso na terra");
            }
        }
        Player outro = lobo(helper, 3);
        List<ItemStack> pedra = List.of(new ItemStack(Items.COBBLESTONE));
        for (int n = 0; n < 100; n++) {
            if (WerewolfPowers.osso(helper.getLevel(), outro, pedra) != null) {
                helper.fail("só a terra dá osso");
            }
        }
        helper.succeed();
    }

    /** <b>Cavar com as patas</b>: agachado, de lobo e do terceiro grau, a terra sai de uma vez. */
    @GameTest(maxTicks = 60)
    public void aWolfDigsWithItsPaws(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = lobo(helper, 3);
        // o jogador de mentira nasce no criativo, e no criativo um lobo não cava: é o original
        ((net.minecraft.server.level.ServerPlayer) quem).setGameMode(GameType.SURVIVAL);
        quem.setShiftKeyDown(true);

        BlockPos terra = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(terra, Blocks.DIRT.defaultBlockState());
        if (!WerewolfPowers.cavaComAsPatas(level, quem, terra)) helper.fail("um lobo cava terra");
        if (!level.getBlockState(terra).isAir()) helper.fail("e o buraco devia ter ficado");

        // pedra não
        BlockPos pedra = helper.absolutePos(new BlockPos(3, 2, 2));
        level.setBlockAndUpdate(pedra, Blocks.STONE.defaultBlockState());
        if (WerewolfPowers.cavaComAsPatas(level, quem, pedra)) helper.fail("pedra não se cava com as patas");

        // de pé não
        quem.setShiftKeyDown(false);
        BlockPos outra = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(outra, Blocks.DIRT.defaultBlockState());
        if (WerewolfPowers.cavaComAsPatas(level, quem, outra)) helper.fail("é agachado que ele cava");

        // e de gente também não
        quem.setShiftKeyDown(true);
        Werewolf.grau(quem, 0);
        if (WerewolfPowers.cavaComAsPatas(level, quem, outra)) helper.fail("gente cava com ferramenta");
        level.setBlockAndUpdate(outra, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pedra, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    // ------------------------------------------------------------------ o contágio

    /**
     * <b>O contágio do décimo grau</b>, que é o que faz da escada uma coisa que se espalha.
     *
     * <p>Quem ele derrubar abaixo de um quarto da vida apanha a licantropia, uma vez em quatro — e as
     * guardas são as mesmas da mordida do bicho.
     */
    @GameTest(maxTicks = 60)
    public void theTenthGradeSpreadsIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = lobo(helper, Werewolf.TETO);

        Player vítima = helper.makeMockServerPlayerInLevel();
        vítima.setHealth(vítima.getMaxHealth() * WolfmanEntity.QUASE_MORTO / 2.0f);
        for (int n = 0; n < 60 && Werewolf.grauDe(vítima) == 0; n++) {
            WerewolfPowers.contagia(level, quem, vítima);
        }
        if (Werewolf.grauDe(vítima) != 1) helper.fail("sessenta golpes quase mortais deviam ter pegado");

        // quem está bem não apanha
        Player inteira = helper.makeMockServerPlayerInLevel();
        for (int n = 0; n < 60; n++) WerewolfPowers.contagia(level, quem, inteira);
        if (Werewolf.grauDe(inteira) != 0) helper.fail("não é a mordida que pega, é a que quase mata");

        // e abaixo do décimo não pega de todo
        Player nono = lobo(helper, 9);
        Player outra = helper.makeMockServerPlayerInLevel();
        outra.setHealth(outra.getMaxHealth() * WolfmanEntity.QUASE_MORTO / 2.0f);
        for (int n = 0; n < 60; n++) WerewolfPowers.contagia(level, nono, outra);
        if (Werewolf.grauDe(outra) != 0) helper.fail("o contágio é do décimo grau, e de mais nenhum");
        helper.succeed();
    }
    /**
     * Um jogador de mentira a quem a queda conte.
     *
     * <p>Ele nasce <b>podendo voar</b>, e a quem pode voar o jogo nem pergunta pela queda — é a primeira
     * linha do {@code causeFallDamage} dele. Tirada a asa, a conta da queda volta a correr.
     */
    private static Player podeCair(Player quem) {
        quem.getAbilities().mayfly = false;
        quem.getAbilities().flying = false;
        quem.onUpdateAbilities();
        return quem;
    }

    // ------------------------------------------------------------------ e as três costuras

    /**
     * <b>O jogo pergunta por tudo isto</b>: as três costuras que não são chamadas por ninguém deste porte.
     *
     * <p>As contas dos poderes se provam uma a uma acima, mas três deles só existem se o <b>jogo</b> passar
     * por eles — o pulo, a queda e a queda de um bloco. São três remendos no jogo, e um remendo que se aplica
     * mas não acerta no lugar certo é indistinguível de um que não existe. Esta prova chama o jogo e não o
     * porte, e é a única que repara se algum deles se soltar.
     */
    @GameTest(maxTicks = 60)
    public void theGameAsksForAllOfIt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        // o pulo: o jogo chama jumpFromGround, e o bicho tem de subir mais
        Player gente = helper.makeMockServerPlayerInLevel();
        gente.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        gente.setOnGround(true);
        gente.jumpFromGround();
        double pulaGente = gente.getDeltaMovement().y;

        Player bicho = lobo(helper, 10);
        bicho.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        bicho.setOnGround(true);
        bicho.jumpFromGround();
        double pulaBicho = bicho.getDeltaMovement().y;
        if (pulaBicho <= pulaGente + 0.0001) {
            helper.fail("o jogo não passou pelo salto: gente pulou " + pulaGente
                    + " e o bicho " + pulaBicho);
        }

        /*
         * A queda: o jogo chama causeFallDamage, e o que se olha é o que ele DEVOLVE — e não a vida, porque
         * um jogador de mentira está travado no criativo e nada lhe dói.
         *
         * Sete blocos doem a quem é gente: o jogo tira três e sobram quatro. Ao lobo de grau dez o remendo
         * perdoa cinco, sobram dois, o jogo tira três e não sobra nada — e então ele devolve FALSO. A
         * diferença entre os dois é a prova de que o remendo está no caminho.
         */
        Player cai = podeCair(helper.makeMockServerPlayerInLevel());
        if (!cai.causeFallDamage(SETE_BLOCOS, 1.0f, golpe(helper, DamageTypes.FALL))) {
            helper.fail("sete blocos deviam contar como queda a quem é gente");
        }
        Player caiBicho = podeCair(lobo(helper, 10));
        if (caiBicho.causeFallDamage(SETE_BLOCOS, 1.0f, golpe(helper, DamageTypes.FALL))) {
            helper.fail("o jogo não passou pela queda: sete blocos ainda doem a um lobo de grau dez");
        }

        // e a queda de um bloco: o jogo chama Block.getDrops, e dali tem de poder sair osso
        Player cava = lobo(helper, 3);
        var terra = Blocks.DIRT.defaultBlockState();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        boolean osso = false;
        for (int n = 0; n < 400 && !osso; n++) {
            var caiu = net.minecraft.world.level.block.Block.getDrops(terra, level, onde, null, cava,
                    new ItemStack(Items.AIR));
            osso = caiu.stream().anyMatch(c -> c.is(Items.BONE));
        }
        if (!osso) helper.fail("o jogo não passou pelo osso: quatrocentas cavadas e nenhum");
        helper.succeed();
    }
}
