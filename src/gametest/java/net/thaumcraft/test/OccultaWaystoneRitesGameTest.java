package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;
import net.thaumcraft.occulta.waystone.Waystones;

import java.util.List;

/**
 * As <b>sete Pedras de Caminho do círculo</b>: as cinco que ele faz e as duas que ele usa.
 */
public class OccultaWaystoneRitesGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Acha o primeiro item largado à volta daquele ponto. */
    private static ItemStack largado(ServerLevel level, BlockPos onde,
                                     net.minecraft.world.item.Item qual) {
        for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(onde).inflate(3.0))) {
            if (item.getItem().is(qual)) return item.getItem();
        }
        return ItemStack.EMPTY;
    }

    private static void varre(ServerLevel level, BlockPos onde) {
        for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(onde).inflate(3.0))) {
            item.discard();
        }
    }

    /** <b>A pedra sai do círculo já sabendo voltar para ele.</b> */
    @GameTest(maxTicks = 40)
    public void theRiteBindsTheWaystoneToTheCircleItself(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        varre(level, meio);

        var rito = new Rites.SummonLocationBoundItem(
                () -> new ItemStack(OccultaItems.BOUND_WAYSTONE));
        if (rito.steps(0).getFirst().run(level, meio, 20L, null) != RiteStep.Result.COMPLETED) {
            helper.fail("o rito acaba");
            return;
        }

        var pedra = largado(level, meio, OccultaItems.BOUND_WAYSTONE);
        if (pedra.isEmpty()) {
            helper.fail("e larga uma pedra presa");
            return;
        }
        var lugar = Waystones.lugar(pedra);
        if (lugar == null || !lugar.onde().equals(meio)) {
            helper.fail("presa ao coração do círculo, e está em "
                    + (lugar == null ? "nada" : lugar.onde()));
        }

        varre(level, meio);
        helper.succeed();
    }

    /** <b>E a cópia tira o lugar do que se ofereceu</b>, sem precisar de ir lá. */
    @GameTest(maxTicks = 40)
    public void theCopyRiteTakesTheLocationFromWhatWasOffered(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 4));
        BlockPos lá = helper.absolutePos(new BlockPos(5, 2, 5));
        varre(level, meio);

        ItemStack velha = new ItemStack(OccultaItems.BOUND_WAYSTONE);
        velha.set(OccultaComponents.WAYSTONE, new Waystones.Lugar(level.dimension(), lá));

        var rito = new Rites.SummonCopyBoundItem(
                () -> new ItemStack(OccultaItems.BOUND_WAYSTONE, 2));
        var activo = new ActiveRite("tc.rite.bindwaystonecopy", rito, List.of(), null, 0);
        activo.offer(velha, meio);

        if (rito.steps(0).getFirst().run(level, meio, 20L, activo) != RiteStep.Result.COMPLETED) {
            helper.fail("o rito acaba");
            return;
        }
        var nova = largado(level, meio, OccultaItems.BOUND_WAYSTONE);
        if (nova.getCount() != 2) helper.fail("saem duas, e saíram " + nova.getCount());
        var lugar = Waystones.lugar(nova);
        if (lugar == null || !lugar.onde().equals(lá)) {
            helper.fail("com o lugar da velha, e está em " + (lugar == null ? "nada" : lugar.onde()));
        }

        varre(level, meio);
        helper.succeed();
    }

    /**
     * <b>A Pedra Sangrada prende o bicho, e não a pessoa.</b>
     *
     * <p>É a torção do original: a gente é a <b>condição de entrada</b> — sem ninguém no círculo o rito
     * desiste —, mas quem acaba preso é o <b>último bicho</b> que ele acha no mesmo cubo. Entrar no
     * círculo com um porco ao lado dá uma pedra presa ao porco.
     */
    @GameTest(maxTicks = 40)
    public void thePersonIsOnlyTheDoorAndTheBeastIsWhatGetsBound(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(4, 2, 2));
        varre(level, meio);

        var rito = new Rites.SummonEntityBoundItem(
                () -> new ItemStack(OccultaItems.BLOODED_WAYSTONE));
        var passo = rito.steps(0).getFirst();

        /*
         * Tem de ser um jogador <b>que esteja no mundo</b>: o rito procura gente com uma varredura da
         * caixa, e o jogador de mentira do {@code makeMockPlayer} não entra na lista do nível.
         */
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(new Vec3(meio.getX() + 0.5, meio.getY(), meio.getZ() + 0.5));
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(4, 2, 3));
        porco.setNoAi(true);

        if (passo.run(level, meio, 20L, null) != RiteStep.Result.COMPLETED) {
            helper.fail("com gente no círculo, ele acaba");
            return;
        }
        var pedra = largado(level, meio, OccultaItems.BLOODED_WAYSTONE);
        if (pedra.isEmpty()) {
            helper.fail("e larga uma Pedra Sangrada");
            return;
        }
        if (!TaglockItem.isFor(pedra, porco)) helper.fail("presa ao porco, que é o engano do original");

        varre(level, meio);
        porco.discard();
        helper.succeed();
    }

    /** <b>O rito da pedra leva tudo o que estiver no anel</b>, e não só quem o fez. */
    @GameTest(maxTicks = 40)
    public void theWaystoneRiteSendsEverythingInTheRing(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos lá = helper.absolutePos(new BlockPos(5, 2, 5));
        level.setBlockAndUpdate(lá, Blocks.STONE.defaultBlockState());

        ItemStack pedra = new ItemStack(OccultaItems.BOUND_WAYSTONE);
        pedra.set(OccultaComponents.WAYSTONE, new Waystones.Lugar(level.dimension(), lá));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        porco.setNoAi(true);
        double antes = porco.getZ();

        var rito = new Rites.TeleportToWaystone(3);
        var activo = new ActiveRite("tc.rite.teleporttowaystone", rito, List.of(), null, 0);
        activo.offer(pedra, meio);
        if (rito.steps(0).getFirst().run(level, meio, 20L, activo) != RiteStep.Result.COMPLETED) {
            helper.fail("havendo quem levar, o rito acaba");
            return;
        }
        if (Math.abs(porco.getZ() - antes) < 1.0) helper.fail("e o porco vai com ele");

        porco.discard();
        helper.succeed();
    }

    /**
     * <b>E o que traz alguém é abafado pelo marcador certo, e só por ele.</b>
     *
     * <p>Com a <b>estátua de lobo</b> perto da vaca, o rito passa — o lobo abafa a maldição dirigida e
     * não este. Com a <b>mandrágora</b>, ele devolve o que se ofereceu e não puxa ninguém.
     */
    @GameTest(maxTicks = 40)
    public void theCreatureRiteIsDampenedByTheRightMarkerOnly(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 4));

        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(5, 2, 1));
        vaca.setNoAi(true);
        ItemStack frasco = new ItemStack(OccultaItems.TAGLOCK);
        TaglockItem.bind(frasco, vaca);

        var rito = new Rites.TeleportEntity(3);
        var activo = new ActiveRite("tc.rite.teleportentity", rito, List.of(), null, 0);
        activo.offer(frasco, meio);

        BlockPos marcador = helper.absolutePos(new BlockPos(4, 2, 1));
        level.setBlockAndUpdate(marcador, OccultaBlocks.WOLF_MARKER.defaultBlockState());
        if (rito.steps(0).getFirst().run(level, meio, 20L, activo) != RiteStep.Result.COMPLETED) {
            helper.fail("o lobo não abafa este rito");
        }
        level.setBlockAndUpdate(marcador, Blocks.AIR.defaultBlockState());

        // e com a mandrágora, sim
        vaca.snapTo(new Vec3(marcador.getX() + 0.5, marcador.getY(), marcador.getZ() + 0.5));
        level.setBlockAndUpdate(marcador, OccultaBlocks.MANDRAKE_MARKER.defaultBlockState());
        if (rito.steps(0).getFirst().run(level, meio, 20L, activo) != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("com a mandrágora perto dela, o rito é abafado e devolve");
        }

        level.setBlockAndUpdate(marcador, Blocks.AIR.defaultBlockState());
        vaca.discard();
        helper.succeed();
    }
}
