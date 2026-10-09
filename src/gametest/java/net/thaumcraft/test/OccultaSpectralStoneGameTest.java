package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.enslave.Enslavement;
import net.thaumcraft.occulta.ghost.SpectralStoneItem;
import net.thaumcraft.occulta.ghost.SummonedUndeadEntity;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;

/**
 * A <b>Pedra Espectral</b>: o número que ela guarda, o rito que a enche com o que estava à volta, e os
 * fantasmas que saem dela já escravizados.
 */
public class OccultaSpectralStoneGameTest {
    /**
     * <b>O bicho e a quantidade moram no mesmo número.</b>
     *
     * <p>Os quatro bits de baixo dizem qual; os três de cima, quantos — e é a conta do
     * {@code metaFromCreature} do original.
     */
    @GameTest
    public void theKindAndTheCountShareOneNumber(GameTestHelper helper) {
        for (int bicho = SpectralStoneItem.ESPECTRO; bicho <= SpectralStoneItem.POLTERGEIST; bicho++) {
            for (int quantos = 1; quantos <= SpectralStoneItem.CABEM; quantos++) {
                int guardado = SpectralStoneItem.empacota(bicho, quantos);
                if (SpectralStoneItem.bicho(guardado) != bicho) {
                    helper.fail("o bicho " + bicho + " sai do número " + guardado);
                    return;
                }
                if (SpectralStoneItem.quantos(guardado) != quantos) {
                    helper.fail("e os " + quantos + " também");
                    return;
                }
            }
        }

        // e a pedra em branco não guarda nada
        ItemStack branca = new ItemStack(OccultaItems.SPECTRAL_STONE);
        if (SpectralStoneItem.bicho(SpectralStoneItem.guardado(branca)) != SpectralStoneItem.NADA) {
            helper.fail("uma pedra nova está em branco");
        }

        // cheia, a cara dela é a do bicho
        ItemStack cheia = SpectralStoneItem.cheia(SpectralStoneItem.BANSHEE, 2);
        var cara = cheia.get(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA);
        if (cara == null || cara.floats().isEmpty()
                || (int) (float) cara.floats().getFirst() != SpectralStoneItem.BANSHEE) {
            helper.fail("a cara da pedra é a do bicho que ela tem");
        }
        helper.succeed();
    }

    /**
     * <b>O rito pega em três do mesmo feitio, e só.</b>
     *
     * <p>Postos quatro espectros e uma banshee à volta, ele leva <b>três espectros</b> — o feitio fixa-se
     * no primeiro que ele acha — e deixa os outros em paz.
     */
    @GameTest
    public void theRiteTakesThreeOfOneKind(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        for (int n = 0; n < 4; n++) põe(level, OccultaEntities.SPECTRE, meio.offset(n - 1, 0, 1));
        põe(level, OccultaEntities.BANSHEE, meio.offset(1, 0, -1));

        var rito = new Rites.BindSpectral(Rites.ESPECTROS_A);
        var passo = rito.steps(0).getFirst();
        var deu = passo.run(level, meio, 20L, null);
        if (deu != RiteStep.Result.COMPLETED) {
            helper.fail("o rito acaba, e deu " + deu);
            return;
        }

        var sobraram = level.getEntitiesOfClass(SummonedUndeadEntity.class,
                new net.minecraft.world.phys.AABB(meio).inflate(Rites.ESPECTROS_A),
                net.minecraft.world.entity.Entity::isAlive);
        int espectros = 0, banshees = 0;
        for (var cada : sobraram) {
            if (cada instanceof net.thaumcraft.occulta.ghost.SpectreEntity) espectros++;
            if (cada instanceof net.thaumcraft.occulta.ghost.BansheeEntity) banshees++;
        }
        varre(level, meio);
        if (espectros != 1) helper.fail("ele leva três dos quatro espectros, e sobrou " + espectros);
        if (banshees != 1) helper.fail("e deixa a banshee em paz, e sobraram " + banshees);
        helper.succeed();
    }

    /**
     * <b>E não havendo nenhum, ele devolve.</b>
     *
     * <p>Um círculo preparado e vazio não custa nada a quem o preparou — que é a regra do original para
     * este rito e para mais nenhum dos que dão um objeto.
     */
    @GameTest
    public void withNoGhostsTheRiteRefunds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));
        var passo = new Rites.BindSpectral(Rites.ESPECTROS_A).steps(0).getFirst();
        var deu = passo.run(level, meio, 20L, null);
        if (deu != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem fantasma nenhum ele devolve, e deu " + deu);
        }
        helper.succeed();
    }

    /**
     * <b>Largando-a, os fantasmas saem escravizados.</b>
     *
     * <p>E a pedra fica em branco. Antes dos dois segundos não sai nada, que é o que o <i>pling</i> do
     * segundo segundo está dizendo.
     */
    @GameTest
    public void releasingFreesThemEnslaved(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;

        BlockPos chão = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(chão, Blocks.STONE.defaultBlockState());
        quem.snapTo(chão.getX() + 0.5, chão.getY() + 3.0, chão.getZ() + 0.5, 0.0f, 90.0f);

        ItemStack pedra = SpectralStoneItem.cheia(SpectralStoneItem.POLTERGEIST, 2);
        quem.setItemInHand(InteractionHand.MAIN_HAND, pedra);

        // cedo demais: nada sai
        pedra.getItem().releaseUsing(pedra, level, quem, SpectralStoneItem.SEGURAR - 1);
        if (SpectralStoneItem.guardado(pedra) == 0) helper.fail("cedo demais, nada sai");

        // e depois dos dois segundos, saem os dois
        pedra.getItem().releaseUsing(pedra, level, quem,
                SpectralStoneItem.SEGURAR - SpectralStoneItem.PRONTA_AOS);
        if (SpectralStoneItem.guardado(pedra) != 0) {
            helper.fail("largada, a pedra fica em branco");
            return;
        }

        var saíram = level.getEntitiesOfClass(net.thaumcraft.occulta.ghost.PoltergeistEntity.class,
                new net.minecraft.world.phys.AABB(chão).inflate(6.0),
                net.minecraft.world.entity.Entity::isAlive);
        if (saíram.size() != 2) {
            helper.fail("saem os dois que ela tinha, e saíram " + saíram.size());
            return;
        }
        boolean todosEscravos = true;
        for (var cada : saíram) {
            if (!Enslavement.escravoDe(cada, quem)) todosEscravos = false;
        }
        varre(level, chão);
        if (!todosEscravos) helper.fail("e saem escravizados por quem a largou");
        helper.succeed();
    }

    /** E os dois ritos estão na lista, com os anéis e a hora do original. */
    @GameTest
    public void theTwoRitesAreRegistered(GameTestHelper helper) {
        var necromancia = RiteRegistry.get("tc.rite.spectralstone");
        if (necromancia == null) {
            helper.fail("o Rito da Necromancia está na lista");
            return;
        }
        if (necromancia.inner().ritual() != 16) helper.fail("num anel de dezesseis de giz de ritual");
        if (!necromancia.when().contains(RiteRegistry.When.NIGHT)) helper.fail("e só de noite");

        var prender = RiteRegistry.get("tc.rite.bindspectral");
        if (prender == null) {
            helper.fail("e o Rito de Prender também");
            return;
        }
        if (prender.inner().ritual() != 28) helper.fail("num anel de vinte e oito");
        if (!(prender.rite() instanceof Rites.BindSpectral)) helper.fail("e é o que prende espectros");
        helper.succeed();
    }

    /**
     * <b>Varre o que sobrou.</b>
     *
     * <p>Um fantasma esquecido numa arena anda, e andando entra na do lado e estraga a prova de outro. É o
     * que o {@code gametest-mundo-partilhado} ensina, e é barato de evitar.
     */
    private static void varre(ServerLevel level, BlockPos meio) {
        for (var cada : level.getEntitiesOfClass(SummonedUndeadEntity.class,
                new net.minecraft.world.phys.AABB(meio).inflate(16.0))) {
            cada.discard();
        }
    }

    /** Põe um daqueles ali. */
    private static void põe(ServerLevel level,
                            net.minecraft.world.entity.EntityType<? extends SummonedUndeadEntity> qual,
                            BlockPos onde) {
        var bicho = qual.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;
        bicho.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(bicho);
    }
}
