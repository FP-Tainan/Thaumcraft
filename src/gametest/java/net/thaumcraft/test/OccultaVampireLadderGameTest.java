package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.vampire.Blood;
import net.thaumcraft.occulta.vampire.CoffinBlock;
import net.thaumcraft.occulta.vampire.DaylightCollectorBlock;
import net.thaumcraft.occulta.vampire.TornPage;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampireBookItem;
import net.thaumcraft.occulta.vampire.VampireLadder;
import net.thaumcraft.occulta.vampire.VampirePowers;

/**
 * <b>A escada dos dez graus do vampiro</b>, e o livro que a destranca.
 *
 * <p>A prova que carrega a fatia é a do <b>teto</b>: um vampiro sem livro para no <b>terceiro</b> grau para
 * sempre, por mais aldeões que morda. Tudo o resto da escada é inalcançável até ele achar a primeira página
 * — e nada no jogo lho diz.
 */
public class OccultaVampireLadderGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um vampiro do grau pedido, com o teto levantado e o sangue cheio. */
    private static Player vampiro(GameTestHelper helper, int grau) {
        Player quem = helper.makeMockServerPlayerInLevel();
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        }
        quem.getAbilities().instabuild = false;
        quem.onUpdateAbilities();

        Vampire.levantaOTeto(quem, Vampire.TETO);
        Vampire.grau(quem, grau);
        Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
        Vampire.apagaAConta(quem);
        return quem;
    }

    // ------------------------------------------------------------------ os números

    /** Os dez degraus pedem o que o original pede. */
    @GameTest(maxTicks = 20)
    public void theTenRungsAreTheOriginals(GameTestHelper helper) {
        if (VampireLadder.CINCO != 5) helper.fail("são cinco aldeões, duas vezes");
        if (VampireLadder.DEIXA_AO_MENOS != Blood.METADE) {
            helper.fail("e deixa-se metade do sangue neles");
        }
        if (VampireLadder.NÃO_PASSE_DE != 280) helper.fail("e não mais do que duzentos e oitenta");
        if (VampireLadder.ESTRAGOU_ABAIXO_DE != 240) {
            helper.fail("abaixo de duzentos e quarenta a conta se apaga");
        }
        if (VampireLadder.A_NOITE != 300) helper.fail("a noite parada são trezentas voltas do relógio");
        if (VampireLadder.QUEIMADURAS != 10) helper.fail("são dez queimaduras de sol engarrafado");
        if (VampireLadder.BLAZES != 20) helper.fail("e vinte Blazes");
        if (VampireLadder.ALDEIAS != 4) helper.fail("e quatro aldeias");
        if (VampireLadder.BARRAS != 15 || VampireLadder.TETO_DA_GAIOLA != 9) {
            helper.fail("a gaiola é de quinze barras com teto de nove");
        }
        if (VampireLadder.O_CAIXÃO_A != 4) helper.fail("e o caixão a quatro blocos");
        if (VampireBookItem.PÁGINAS != 9) helper.fail("o livro tem nove páginas");
        helper.succeed();
    }

    /**
     * <b>E esta é a prova que carrega a fatia: sem livro, ele para no terceiro grau.</b>
     *
     * <p>O teto nasce em três e só o livro o levanta. Um vampiro que morda a aldeia inteira e passe a noite
     * acordado sobe até ao terceiro e <b>para ali para sempre</b> — e o jogo não lhe diz uma palavra sobre o
     * porquê. É a coisa mais cruel que este mod faz, e é o que torna o livro o centro do ramo.
     */
    @GameTest(maxTicks = 20)
    public void withoutTheBookHeStopsAtThree(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Vampire.grau(quem, 1);
        if (Vampire.tetoDoGrau(quem) != Vampire.MENOR_TETO) {
            helper.fail("o teto de quem nunca leu nada é três");
        }

        Vampire.grau(quem, 3);
        if (Vampire.podeSubir(quem)) helper.fail("no terceiro grau, sem livro, ele não sobe mais");

        // e cada página lida levanta o teto um degrau
        for (int páginas = 0; páginas <= VampireBookItem.PÁGINAS; páginas++) {
            ItemStack livro = VampireBookItem.com(páginas);
            if (VampireBookItem.páginas(livro) != páginas) {
                helper.fail("um livro de " + páginas + " páginas diz que tem " + páginas);
            }
            Vampire.levantaOTeto(quem, páginas + 1);
        }
        if (Vampire.tetoDoGrau(quem) != VampireBookItem.PÁGINAS + 1) {
            helper.fail("o livro inteiro levanta o teto ao décimo grau");
        }
        if (!Vampire.podeSubir(quem)) helper.fail("e então ele sobe");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o terceiro e o nono

    /**
     * <b>Morder sem esvaziar</b>: cinco aldeões na faixa estreita, e um mal mordido apaga tudo.
     *
     * <p>Quatro aldeões bem mordidos e um mal mordido valem <b>zero</b>. É o mod a ensinar moderação da
     * única maneira que ele sabe, que é tirando.
     */
    @GameTest(maxTicks = 60)
    public void drinkingWithoutDrainingClimbsTheThird(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 2);
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));

        // quatro mordidas boas
        for (int n = 0; n < 4; n++) {
            Blood.põe(aldeão, 260);
            VampireLadder.mordeu(level, quem, aldeão);
        }
        if (Vampire.contaDe(quem) != 4) helper.fail("quatro mordidas boas valem quatro");
        if (Vampire.grauDe(quem) != 2) helper.fail("e ainda não sobem o grau");

        // e uma mordida demais apaga tudo
        Blood.põe(aldeão, 200);
        VampireLadder.mordeu(level, quem, aldeão);
        if (Vampire.contaDe(quem) != 0) helper.fail("uma mordida demais apaga a conta inteira");

        // as cinco boas, agora de seguida
        for (int n = 0; n < 5; n++) {
            Blood.põe(aldeão, 270);
            VampireLadder.mordeu(level, quem, aldeão);
        }
        if (Vampire.grauDe(quem) != 3) helper.fail("cinco mordidas boas sobem ao terceiro grau");

        aldeão.discard();
        helper.succeed();
    }

    /** <b>A gaiola</b>: quinze barras de dezesseis, em dois andares, com teto de nove. */
    @GameTest(maxTicks = 60)
    public void theCageIsFifteenBarsAndARoof(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        if (VampireLadder.engaiolado(level, aldeão)) helper.fail("no campo aberto ninguém está preso");

        // o anel inteiro, dois andares
        for (int andar = 0; andar <= 1; andar++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    level.setBlockAndUpdate(meio.offset(dx, andar, dz),
                            Blocks.IRON_BARS.defaultBlockState());
                }
            }
        }
        if (VampireLadder.engaiolado(level, aldeão)) helper.fail("sem teto ainda não é gaiola");

        // e o teto de nove
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(meio.offset(dx, 2, dz), Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        if (!VampireLadder.engaiolado(level, aldeão)) helper.fail("com barras e teto, é gaiola");

        // uma fresta à frente ainda é gaiola; duas já não
        level.setBlockAndUpdate(meio.offset(0, 0, -1), Blocks.AIR.defaultBlockState());
        if (!VampireLadder.engaiolado(level, aldeão)) helper.fail("uma fresta é o que o original deixa");
        level.setBlockAndUpdate(meio.offset(0, 1, -1), Blocks.AIR.defaultBlockState());
        if (VampireLadder.engaiolado(level, aldeão)) helper.fail("duas frestas já não são gaiola");

        aldeão.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ os outros degraus

    /** <b>A noite parada</b>: o relógio conta, e no criativo conta pouco. */
    @GameTest(maxTicks = 40)
    public void theStillNightClimbsTheFourth(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 3);

        if (level.isBrightOutside()) {
            // de dia o degrau não anda, e é a metade mais importante da regra
            VampireLadder.aNoite(level, quem);
            if (Vampire.contaDe(quem) != 0) helper.fail("de dia a noite não conta");
        }

        /*
         * No criativo bastam dez voltas, que é o original tendo pena de quem está testando. A arena da prova
         * pode estar de dia ou de noite conforme o relógio do mundo, e por isso a conta se faz pelo que o
         * degrau devolve: de dia ela não anda, de noite ela anda.
         */
        quem.getAbilities().instabuild = true;
        quem.onUpdateAbilities();
        boolean dia = level.isBrightOutside();
        for (int n = 0; n <= VampireLadder.A_NOITE_NO_CRIATIVO; n++) VampireLadder.aNoite(level, quem);
        if (dia) {
            if (Vampire.grauDe(quem) != 3) helper.fail("de dia o degrau não anda, por mais voltas que dê");
        } else if (Vampire.grauDe(quem) != 4) {
            helper.fail("dez voltas de noite, no criativo, sobem ao quarto grau");
        }
        helper.succeed();
    }

    /** <b>Vinte Blazes</b>, e nada mais conta. */
    @GameTest(maxTicks = 40)
    public void twentyBlazesClimbTheSixth(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 5);

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(2, 2, 2));
        VampireLadder.matou(quem, ovelha);
        if (Vampire.contaDe(quem) != 0) helper.fail("uma ovelha não é uma criatura de fogo puro");
        ovelha.discard();

        var blaze = helper.spawn(EntityTypes.BLAZE, new BlockPos(3, 2, 3));
        for (int n = 0; n < VampireLadder.BLAZES - 1; n++) VampireLadder.matou(quem, blaze);
        if (Vampire.grauDe(quem) != 5) helper.fail("dezenove ainda não chegam");
        VampireLadder.matou(quem, blaze);
        if (Vampire.grauDe(quem) != 6) helper.fail("e o vigésimo sobe ao sexto grau");

        blaze.discard();
        helper.succeed();
    }

    /** <b>Dez queimaduras de sol engarrafado</b> sobem ao quinto. */
    @GameTest(maxTicks = 40)
    public void tenBurnsClimbTheFifth(GameTestHelper helper) {
        Player quem = vampiro(helper, 4);
        for (int n = 0; n < VampireLadder.QUEIMADURAS - 1; n++) VampireLadder.oSolEngarrafado(quem);
        if (Vampire.grauDe(quem) != 4) helper.fail("nove queimaduras ainda não chegam");
        VampireLadder.oSolEngarrafado(quem);
        if (Vampire.grauDe(quem) != 5) helper.fail("a décima sobe ao quinto grau");
        helper.succeed();
    }

    /** <b>Quatro aldeias diferentes</b>: um pedaço de mundo onde ele já esteve não conta. */
    @GameTest(maxTicks = 40)
    public void fourDifferentVillagesClimbTheEighth(GameTestHelper helper) {
        Player quem = vampiro(helper, 7);
        if (!VampireLadder.guardaLugar(quem, 10, 10)) helper.fail("o primeiro lugar é novo");
        if (VampireLadder.guardaLugar(quem, 10, 10)) helper.fail("e o mesmo lugar não conta duas vezes");
        if (!VampireLadder.guardaLugar(quem, 10, 11)) helper.fail("mas o do lado conta");

        // e a lista se esquece quando o grau muda
        Vampire.grau(quem, 8);
        if (!VampireLadder.guardaLugar(quem, 10, 10)) {
            helper.fail("mudando de grau, os lugares se esquecem");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ o décimo

    /**
     * <b>Fazer outro vampiro</b>: quatro coisas ao mesmo tempo, e nenhuma delas é um golpe.
     *
     * <p>O cálice do próprio sangue, a presa presa, a presa vazia, e um caixão a quatro blocos. Falhando
     * qualquer uma, o mod diz qual — e é o único degrau da escada que diz alguma coisa.
     */
    @GameTest(maxTicks = 60)
    public void makingAnotherVampireClimbsTheTenth(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 9);
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));

        // sem nada, não vira
        if (VampireLadder.fazUmVampiro(level, quem, aldeão, true)) {
            helper.fail("uma presa de pé não vira nada");
        }

        // presa, mas cheia de sangue
        aldeão.addEffect(new MobEffectInstance(OccultaEffects.PARALYSIS, 400,
                VampirePowers.PRENDE_GRAU_ALTO));
        Blood.põe(aldeão, 300);
        if (VampireLadder.fazUmVampiro(level, quem, aldeão, true)) {
            helper.fail("ainda corre sangue nela");
        }

        // vazia, mas sem caixão
        Blood.põe(aldeão, 0);
        if (VampireLadder.fazUmVampiro(level, quem, aldeão, true)) {
            helper.fail("sem caixão não há vampiro novo");
        }

        // e com o caixão ao lado, vira
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(5, 2, 3)),
                OccultaBlocks.COFFIN.defaultBlockState());
        if (!VampireLadder.fazUmVampiro(level, quem, aldeão, true)) {
            helper.fail("com as quatro coisas, ela vira");
        }
        if (Vampire.grauDe(quem) != 10) helper.fail("e quem a fez chega ao décimo grau");
        if (!aldeão.isRemoved()) helper.fail("e o aldeão deixa de ser ele");

        /*
         * E o que nasceu dele vai-se: um Vampiro é persistente, anda à procura de aldeias a cento e vinte e
         * oito blocos e teleporta-se para lá. Deixá-lo solto num mundo partilhado por mil provas é largar um
         * bicho dentro das provas dos outros.
         */
        for (var novo : level.getEntitiesOfClass(
                net.thaumcraft.occulta.vampire.VampireEntity.class,
                quem.getBoundingBox().inflate(32.0))) {
            novo.discard();
        }
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(5, 2, 3)),
                Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    // ------------------------------------------------------------------ o livro e as páginas

    /**
     * <b>As páginas só caem para quem já tem o livro.</b>
     *
     * <p>É a única coisa do mod que funciona assim, e é de propósito: quem nunca achou o primeiro exemplar
     * numa livraria de aldeia nunca verá uma página cair, por mais que mate.
     */
    @GameTest(maxTicks = 40)
    public void pagesOnlyFallForWhoAlreadyCarriesTheBook(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = helper.makeMockServerPlayerInLevel();
        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));

        if (VampireBookItem.traz(quem)) helper.fail("de mãos vazias ele não traz livro nenhum");
        TornPage.doMorto(level, quem, aldeão);
        var roda = aldeão.getBoundingBox().inflate(4.0);
        if (!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, roda).isEmpty()) {
            helper.fail("sem livro, nenhuma página cai");
        }

        // com um livro incompleto na mochila, ele traz
        quem.getInventory().add(VampireBookItem.com(3));
        if (!VampireBookItem.traz(quem)) helper.fail("um livro por acabar na mochila conta");

        // e um livro inteiro já não pede páginas
        quem.getInventory().clearContent();
        quem.getInventory().add(VampireBookItem.com(VampireBookItem.PÁGINAS));
        if (VampireBookItem.traz(quem)) helper.fail("um livro inteiro já não faz cair nada");

        aldeão.discard();
        helper.succeed();
    }

    /** E de quem elas caem: dos chefes sempre, dos vivos comuns nunca. */
    @GameTest(maxTicks = 40)
    public void pagesFallFromTheDeadAndTheBetween(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(2, 2, 2));
        for (int n = 0; n < 50; n++) {
            if (TornPage.cai(level, ovelha)) helper.fail("de uma ovelha não cai página nenhuma");
        }
        ovelha.discard();

        if (TornPage.DE_ALDEÃO != 0.1) helper.fail("de um aldeão, uma em dez");
        if (TornPage.DE_ENTRE_MUNDOS != 0.09) helper.fail("de quem anda entre mundos, nove em cem");
        if (TornPage.DE_MORTO_VIVO != 0.02) helper.fail("e de um morto-vivo qualquer, duas em cem");
        helper.succeed();
    }

    // ------------------------------------------------------------------ o sol engarrafado

    /**
     * <b>O Coletor de Luz enche de um em um</b>, ao ritmo do que o sensor marcar.
     *
     * <p>Não serve pôr a esfera ao meio-dia e esperar: cada degrau precisa do sol num ponto diferente do
     * céu. Encher a esfera é <b>ver um dia inteiro nascer</b>.
     */
    @GameTest(maxTicks = 60)
    public void theCollectorFillsOneAtATime(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos sensor = onde.east();

        level.setBlockAndUpdate(onde, OccultaBlocks.DAYLIGHT_COLLECTOR.defaultBlockState());
        Player quem = helper.makeMockServerPlayerInLevel();
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(OccultaItems.QUARTZ_SPHERE));

        var estado = level.getBlockState(onde);
        estado.useItemOn(quem.getMainHandItem(), level, quem, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde),
                        net.minecraft.core.Direction.UP, onde, false));
        if (level.getBlockState(onde).getValue(DaylightCollectorBlock.SOL) != 1) {
            helper.fail("a esfera entra e o sol começa em um");
        }

        // e sobe de um em um, ao número que o sensor marcar
        for (int sol = 1; sol < DaylightCollectorBlock.CHEIA; sol++) {
            level.setBlockAndUpdate(sensor, Blocks.DAYLIGHT_DETECTOR.defaultBlockState()
                    .setValue(DaylightDetectorBlock.POWER, sol + 1));
            level.updateNeighborsAt(sensor, Blocks.DAYLIGHT_DETECTOR);
            if (level.getBlockState(onde).getValue(DaylightCollectorBlock.SOL) != sol + 1) {
                helper.fail("o sensor a marcar " + (sol + 1) + " devia dar o degrau " + (sol + 1));
            }
        }

        // e cheio, o clique dá a Granada Solar
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        level.getBlockState(onde).useItemOn(ItemStack.EMPTY, level, quem,
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde),
                        net.minecraft.core.Direction.UP, onde, false));
        if (level.getBlockState(onde).getValue(DaylightCollectorBlock.SOL) != 0) {
            helper.fail("e esvazia-se ao dar o que juntou");
        }
        if (!quem.getInventory().contains(new ItemStack(OccultaItems.SUN_GRENADE))) {
            helper.fail("o que ele dá, cheio, é uma Granada Solar");
        }

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(sensor, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    // ------------------------------------------------------------------ e o caixão

    /** <b>A tampa</b>: abre e fecha as duas metades, e não abre debaixo de um bloco. */
    @GameTest(maxTicks = 40)
    public void theCoffinLidOpensBothHalves(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos pés = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos cabeça = pés.north();

        level.setBlockAndUpdate(pés, OccultaBlocks.COFFIN.defaultBlockState()
                .setValue(CoffinBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(CoffinBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT));
        level.setBlockAndUpdate(cabeça, OccultaBlocks.COFFIN.defaultBlockState()
                .setValue(CoffinBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(CoffinBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));

        CoffinBlock.viraATampa(level, pés, level.getBlockState(pés));
        if (!level.getBlockState(pés).getValue(CoffinBlock.ABERTO)) helper.fail("a tampa abre");
        if (!level.getBlockState(cabeça).getValue(CoffinBlock.ABERTO)) {
            helper.fail("e abre as duas metades ao mesmo tempo");
        }

        CoffinBlock.viraATampa(level, pés, level.getBlockState(pés));
        if (level.getBlockState(pés).getValue(CoffinBlock.ABERTO)) helper.fail("e fecha outra vez");

        // debaixo de um bloco ela não levanta
        level.setBlockAndUpdate(pés.above(), Blocks.STONE.defaultBlockState());
        CoffinBlock.viraATampa(level, pés, level.getBlockState(pés));
        if (level.getBlockState(pés).getValue(CoffinBlock.ABERTO)) {
            helper.fail("um caixão enterrado fica enterrado");
        }

        level.setBlockAndUpdate(pés.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pés, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(cabeça, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
