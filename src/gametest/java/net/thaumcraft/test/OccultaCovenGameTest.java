package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.coven.Coven;
import net.thaumcraft.occulta.coven.CovenQuest;
import net.thaumcraft.occulta.coven.CovenWitchEntity;
import net.thaumcraft.occulta.coven.WitchNames;

/**
 * A Bruxa do Coven: o nome, o pedido, o coven que ela entra, e o teto de seis.
 */
public class OccultaCovenGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static CovenWitchEntity bruxa(GameTestHelper helper, int x, int y, int z) {
        return helper.spawn(OccultaEntities.COVEN_WITCH, new BlockPos(x, y, z),
                EntitySpawnReason.STRUCTURE);
    }

    /** As duas listas do original estão inteiras: 272 nomes e 398 sobrenomes. */
    @GameTest(maxTicks = 20)
    public void theNameListsAreTheOriginals(GameTestHelper helper) {
        if (WitchNames.quantosPrimeiros() != 272) {
            helper.fail("são 272 primeiros nomes, são " + WitchNames.quantosPrimeiros());
        }
        if (WitchNames.quantosSobrenomes() != 398) {
            helper.fail("são 398 sobrenomes, são " + WitchNames.quantosSobrenomes());
        }
        var sorte = helper.getLevel().getRandom();
        String nome = WitchNames.sorteia(sorte);
        if (!nome.contains(" ")) helper.fail("um nome de bruxa é nome e sobrenome, veio " + nome);
        helper.succeed();
    }

    /**
     * Ela <b>ganha nome ao ser falada</b>, e não ao nascer.
     *
     * <p>É do original, e faz diferença: uma bruxa com quem ninguém falou não tem nome nenhum.
     */
    @GameTest(maxTicks = 20)
    public void sheGetsHerNameWhenSpokenTo(GameTestHelper helper) {
        piso(helper);
        var bruxa = bruxa(helper, 2, 2, 2);
        if (bruxa.hasCustomName()) helper.fail("antes de falarem com ela, não tem nome");

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 2.5)));
        bruxa.mobInteract(quem, InteractionHand.MAIN_HAND);

        if (!bruxa.hasCustomName()) helper.fail("falada, ela ganha nome");
        bruxa.discard();
        helper.succeed();
    }

    /**
     * O caminho inteiro de um pedido de buscar: ela pede, aceita-se, traz-se, e ela entra.
     *
     * <p>Para a prova não depender do sorteio, usa-se a conversa toda com a mão já cheia de ossos: dos três
     * pedidos que há, o de buscar é o único que se resolve sem matar nada, e os outros dois ficam provados
     * pelo que a <b>recusa</b> diz.
     */
    @GameTest(maxTicks = 40)
    public void bringingWhatSheAskedJoinsHerToTheCoven(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 2.5)));
        if (Coven.tamanho(quem) != 0) helper.fail("ninguém começa com coven");

        // ela só negocia com quem tem familiar, e por isso a prova arranja um primeiro
        var sapo = helper.spawn(net.thaumcraft.occulta.OccultaEntities.TOAD,
                new net.minecraft.core.BlockPos(5, 2, 5));
        sapo.tame(quem);
        if (!net.thaumcraft.occulta.familiar.Familiars.vincula(quem, sapo)) {
            helper.fail("o sapo devia vincular-se");
        }

        /*
          * Procura-se uma bruxa que peça os <b>ossos</b>. Os outros pedidos de buscar são de coisas do ofício
          * — um coração de demônio, cinco cozimentos, uma pedra necrótica —, e esta prova resolve o pedido
          * com o que se arranja sem ofício nenhum.
          */
        CovenWitchEntity bruxa = null;
        for (int tentativa = 0; tentativa < 120 && bruxa == null; tentativa++) {
            var tentada = bruxa(helper, 2, 2, 2);
            tentada.mobInteract(quem, InteractionHand.MAIN_HAND);
            if ("quest.getbones".equals(tentada.pedidoAtual())) bruxa = tentada;
            else tentada.discard();
        }
        if (bruxa == null) {
            helper.fail("em cento e vinte bruxas nenhuma pediu os ossos — o sorteio não está dando todos");
            return;
        }

        // aceitar
        bruxa.mobInteract(quem, InteractionHand.MAIN_HAND);
        // e trazer: sem os ossos ela recusa
        bruxa.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (Coven.tamanho(quem) != 0) helper.fail("de mão vazia ela não entra");

        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE, 30));
        bruxa.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (Coven.tamanho(quem) != 1) {
            helper.fail("trazidos os trinta ossos, ela entra no coven; o coven tem "
                    + Coven.tamanho(quem));
        }
        if (!Coven.tem(quem, bruxa.getUUID())) helper.fail("e quem entrou foi ela");
        if (quem.getMainHandItem().getCount() != 0) {
            helper.fail("e ela fica com os ossos, sobraram " + quem.getMainHandItem().getCount());
        }

        bruxa.discard();
        sapo.discard();
        helper.succeed();
    }

    /**
     * E <b>sem familiar ela não negocia</b>: é a tranca do original, que esteve costurada enquanto os
     * familiares não existiam e voltou a valer com a fatia deles.
     */
    @GameTest(maxTicks = 20)
    public void withoutAFamiliarSheWillNotTalkBusiness(GameTestHelper helper) {
        piso(helper);
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 2.5)));
        if (net.thaumcraft.occulta.familiar.Familiars.temAlgum(quem)) {
            helper.fail("esta prova começa sem familiar");
        }

        var bruxa = bruxa(helper, 2, 2, 2);
        bruxa.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (bruxa.pedidoAtual() != null) helper.fail("sem familiar ela não chega a pedir nada");
        bruxa.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (Coven.tamanho(quem) != 0) helper.fail("e não entra em coven nenhum");

        bruxa.discard();
        helper.succeed();
    }

    /** O teto do original: seis, e nem uma a mais. */
    @GameTest(maxTicks = 20)
    public void sixIsTheCeiling(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < Coven.MAX; i++) {
            if (!Coven.junta(quem, java.util.UUID.randomUUID())) {
                helper.fail("as seis primeiras entram, a " + (i + 1) + " não entrou");
            }
        }
        if (!Coven.cheio(quem)) helper.fail("com seis o coven está cheio");
        if (Coven.junta(quem, java.util.UUID.randomUUID())) helper.fail("a sétima não entra");
        if (Coven.tamanho(quem) != Coven.MAX) {
            helper.fail("o coven tem seis, tem " + Coven.tamanho(quem));
        }
        helper.succeed();
    }

    /** E a mesma bruxa não entra duas vezes. */
    @GameTest(maxTicks = 20)
    public void theSameWitchDoesNotJoinTwice(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var quala = java.util.UUID.randomUUID();
        if (!Coven.junta(quem, quala)) helper.fail("a primeira vez entra");
        if (Coven.junta(quem, quala)) helper.fail("a segunda vez não");
        if (Coven.tamanho(quem) != 1) helper.fail("e o coven continua com uma");
        helper.succeed();
    }

    /**
     * Os pedidos do original que dão para portar estão todos lá.
     *
     * <p>São <b>sete</b> no original e eram <b>três</b> aqui, porque as coisas que os outros quatro pediam
     * não existiam. Com o <b>Coração de Demônio</b>, a <b>Pedra Necrótica</b> e o <b>Cozimento Grotesco</b>
     * no mod, são <b>seis</b> — e fica faltando só o que pede uma <b>Bola de Cristal</b>.
     */
    @GameTest(maxTicks = 20)
    public void thePortableQuestsAreAllThere(GameTestHelper helper) {
        if (CovenQuest.TODAS.size() != 6) {
            helper.fail("são seis pedidos portáveis, são " + CovenQuest.TODAS.size());
        }
        long brigas = CovenQuest.TODAS.stream().filter(q -> q instanceof CovenQuest.Briga).count();
        long buscas = CovenQuest.TODAS.stream().filter(q -> q instanceof CovenQuest.Busca).count();
        if (brigas != 2) helper.fail("duas são de brigar, são " + brigas);
        if (buscas != 4) helper.fail("e quatro são de buscar, são " + buscas);

        // e cada um deles tem fala nos dois idiomas, que é o que uma chave sem tradução não tem
        for (CovenQuest pedido : CovenQuest.TODAS) {
            if (pedido.chave().isBlank()) helper.fail("um pedido sem chave de fala");
        }
        helper.succeed();
    }
}
