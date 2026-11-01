package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.familiar.FindOreGoal;
import net.thaumcraft.occulta.familiar.SpectralFamiliarEntity;
import net.thaumcraft.occulta.rite.RiteRegistry;

/**
 * O <b>Familiar Espectral</b>: o porco azul que acha minério.
 *
 * <p>A prova que carrega a fatia é a da <b>busca</b>. Ele não cava, não aponta e não desenha nada: ele
 * procura o minério num poço à volta de si e <b>vai sentar-se em cima dele</b>. Se a busca não achar o que
 * está lá, o bicho fica a pastar e ninguém percebe que está partido — ele não dá erro nenhum, só nunca
 * acha nada.
 */
public class OccultaSpectralFamiliarGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** <b>O rito está no livro</b>, com o anel de dentro e as quatro coisas. */
    @GameTest(maxTicks = 20)
    public void theRiteIsInTheBook(GameTestHelper helper) {
        RiteRegistry.Entry rito = RiteRegistry.all().stream()
                .filter(qual -> qual.key().equals("tc.rite.summonfamiliar")).findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o rito de chamar o familiar devia estar no livro");
            return;
        }
        if (rito.inner().ritual() != 16 || !rito.middle().empty() || !rito.outer().empty()) {
            helper.fail("ele pede só o anel de dentro, de dezesseis glifos");
            return;
        }
        helper.succeed();
    }

    /** <b>As sete coisas que se lhe pedem</b>, cada uma com dois minérios e a fundura dela. */
    @GameTest(maxTicks = 20)
    public void thereAreSevenThingsToAskFor(GameTestHelper helper) {
        if (SpectralFamiliarEntity.PEDE.size() != 7) {
            helper.fail("são sete pedidos: são " + SpectralFamiliarEntity.PEDE.size());
            return;
        }
        for (var pedido : SpectralFamiliarEntity.PEDE) {
            if (pedido.minérios().size() != 2) {
                helper.fail("cada um tem o minério de pedra e o de ardósia");
                return;
            }
            if (pedido.fundura() <= 0) {
                helper.fail("e uma fundura a que o procurar");
                return;
            }
        }
        if (SpectralFamiliarEntity.qualPedido(new ItemStack(Items.DIAMOND)) != 0) {
            helper.fail("o diamante é o primeiro da lista, como no original");
            return;
        }
        if (SpectralFamiliarEntity.qualPedido(new ItemStack(Items.DIRT)) != -1) {
            helper.fail("e um torrão de terra não é pedido nenhum");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>Dar-lhe um diamante manda-o procurar minério de diamante</b>, e um torrão manda-o sentar.
     */
    @GameTest(maxTicks = 40)
    public void adiamondSetsHimLooking(GameTestHelper helper) {
        piso(helper);
        var dono = helper.makeMockServerPlayerInLevel();
        Vec3 onde = helper.absoluteVec(new Vec3(3.5, 2.0, 3.5));
        dono.snapTo(onde.x, onde.y, onde.z, 0.0f, 0.0f);
        var porco = helper.spawn(OccultaEntities.SPECTRAL_FAMILIAR, new BlockPos(2, 2, 2));
        porco.tame(dono);

        ItemStack torrão = new ItemStack(Items.DIRT);
        dono.setItemInHand(InteractionHand.MAIN_HAND, torrão);
        porco.mobInteract(dono, InteractionHand.MAIN_HAND);
        if (!porco.isOrderedToSit()) {
            helper.fail("mão sem pedido manda-o sentar");
            return;
        }
        if (porco.procura() != -1) {
            helper.fail("e não o manda procurar nada");
            return;
        }

        ItemStack diamante = new ItemStack(Items.DIAMOND);
        dono.setItemInHand(InteractionHand.MAIN_HAND, diamante);
        porco.mobInteract(dono, InteractionHand.MAIN_HAND);
        if (porco.procura() != 0) {
            helper.fail("um diamante manda-o procurar diamante: procura " + porco.procura());
            return;
        }
        if (porco.isOrderedToSit()) {
            helper.fail("e levanta-o para ir procurar");
            return;
        }
        if (porco.pedidos() != 1) {
            helper.fail("e conta o pedido: contou " + porco.pedidos());
            return;
        }
        porco.discard();
        helper.succeed();
    }

    /**
     * <b>E ele acha o minério que está por baixo.</b>
     *
     * <p>É a prova que carrega a fatia: a busca varre um poço do fundo do mundo até à fundura do pedido,
     * e tem de achar tanto o de pedra como o de ardósia.
     */
    @GameTest(maxTicks = 40)
    public void heFindsTheOreBelow(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var dono = helper.makeMockServerPlayerInLevel();
        Vec3 onde = helper.absoluteVec(new Vec3(3.5, 2.0, 3.5));
        dono.snapTo(onde.x, onde.y, onde.z, 0.0f, 0.0f);
        var porco = helper.spawn(OccultaEntities.SPECTRAL_FAMILIAR, new BlockPos(3, 2, 3));
        porco.tame(dono);
        porco.procura(0);

        var meta = new FindOreGoal(porco, 1.0);
        if (meta.procura()) {
            helper.fail("sem minério nenhum por baixo ele não acha nada");
            return;
        }

        // um diamante de ardósia no fundo do mundo, debaixo dele
        BlockPos fundo = new BlockPos(
                net.minecraft.util.Mth.floor(porco.getX()), level.getMinY() + 3,
                net.minecraft.util.Mth.floor(porco.getZ()));
        level.setBlockAndUpdate(fundo, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
        if (!meta.procura()) {
            helper.fail("e com um minério de ardósia lá em baixo, acha-o");
            return;
        }
        if (!fundo.equals(meta.achado())) {
            helper.fail("e acha-o onde ele está: achou " + meta.achado());
            return;
        }

        // e o de pedra também, que é o do original
        level.setBlockAndUpdate(fundo, Blocks.DIAMOND_ORE.defaultBlockState());
        if (!meta.procura()) {
            helper.fail("e o de pedra também");
            return;
        }

        // mas não o de outro minério
        level.setBlockAndUpdate(fundo, Blocks.COAL_ORE.defaultBlockState());
        if (meta.procura()) {
            helper.fail("e carvão não é diamante");
            return;
        }
        level.setBlockAndUpdate(fundo, Blocks.STONE.defaultBlockState());
        porco.discard();
        helper.succeed();
    }

    /** <b>E ele gasta-se</b>: o primeiro pedido nunca o leva, e o sexto leva-o sempre. */
    @GameTest(maxTicks = 20)
    public void heWearsOut(GameTestHelper helper) {
        piso(helper);
        var porco = helper.spawn(OccultaEntities.SPECTRAL_FAMILIAR, new BlockPos(2, 2, 2));

        porco.pedidos(1);
        if (porco.seVaiEmbora()) {
            helper.fail("ao primeiro pedido ele fica sempre");
            return;
        }
        porco.pedidos(SpectralFamiliarEntity.PEDIDOS + 1);
        if (!porco.seVaiEmbora()) {
            helper.fail("e passados cinco ele vai-se sempre embora");
            return;
        }
        if (SpectralFamiliarEntity.VAI_EMBORA.length != 5) {
            helper.fail("são cinco chances, como no original");
            return;
        }
        porco.discard();
        helper.succeed();
    }

    /** E dele cai <b>Pó Espectral</b>, que é do que ele é feito. */
    @GameTest(maxTicks = 40)
    public void spectralDustFallsFromHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var porco = helper.spawn(OccultaEntities.SPECTRAL_FAMILIAR, new BlockPos(2, 2, 2));
        porco.hurtServer(level, level.damageSources().genericKill(), 1000.0f);

        boolean caiu = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2)))
                                .inflate(4.0)).stream()
                .anyMatch(item -> item.getItem()
                        .is(net.thaumcraft.occulta.OccultaItems.SPECTRAL_DUST));
        if (!caiu) helper.fail("dele cai Pó Espectral");
        else helper.succeed();
    }
}
