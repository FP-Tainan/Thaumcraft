package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.SilverVatBlockEntity;

/**
 * A <b>Tina de Prata</b>: a bacia que se encosta a uma fornalha e apanha o que escorre.
 *
 * <p>A prova que carrega a fatia é a do <b>ouro que cresce</b>: ela não olha para o que há numa máquina ao
 * lado, olha para o que <b>apareceu</b> nela desde a última vez. Uma fornalha cheia de ouro parada não lhe
 * dá nada; uma fornalha que acabou de fundir mais um lingote, sim — uma vez em cinco.
 */
public class OccultaSilverVatGameTest {
    /** Põe a tina e uma fornalha ao lado dela, e devolve a alma da tina. */
    private static SilverVatBlockEntity monta(GameTestHelper helper, BlockPos tina, BlockPos forno) {
        ServerLevel level = helper.getLevel();
        level.setBlockAndUpdate(tina, OccultaBlocks.SILVER_VAT.defaultBlockState());
        level.setBlockAndUpdate(forno, Blocks.FURNACE.defaultBlockState());
        return level.getBlockEntity(tina) instanceof SilverVatBlockEntity alma ? alma : null;
    }

    /** Os números dela são os do original. */
    @GameTest
    public void itsNumbersAreTheOriginals(GameTestHelper helper) {
        if (SilverVatBlockEntity.UMA_EM_CINCO != 5) helper.fail("uma vez em cinco");
        if (SilverVatBlockEntity.POR_CAMADA != 8 || SilverVatBlockEntity.CAMADAS != 8) {
            helper.fail("e oito camadas de oito pós cada");
        }
        helper.succeed();
    }

    /**
     * <b>Ela nasce vazia, e o que se lhe põe dentro sai clicando.</b>
     */
    @GameTest(maxTicks = 40)
    public void sheStartsEmptyAndGivesBackWhatSheHas(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.SILVER_VAT.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof SilverVatBlockEntity tina)) {
            helper.fail("a tina devia ter alma");
            return;
        }
        if (!tina.prata().isEmpty()) helper.fail("ela nasce vazia");
        if (tina.camadas() != 0) helper.fail("e sem camada nenhuma");

        tina.prata(new ItemStack(OccultaItems.SILVER_DUST, 16));
        if (tina.camadas() != 2) {
            helper.fail("dezesseis pós são duas camadas; deu " + tina.camadas());
        }
        tina.prata(new ItemStack(OccultaItems.SILVER_DUST, 1));
        if (tina.camadas() != 1) helper.fail("e um pó já é uma camada");
        tina.prata(new ItemStack(OccultaItems.SILVER_DUST, 64));
        if (tina.camadas() != SilverVatBlockEntity.CAMADAS) {
            helper.fail("e sessenta e quatro são as oito; deu " + tina.camadas());
        }

        // e clicar nela devolve o que lá está
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setPos(onde.getX() + 0.5, onde.getY(), onde.getZ() + 1.5);
        level.getBlockState(onde).useWithoutItem(level, quem, new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(onde), net.minecraft.core.Direction.UP,
                onde, false));
        if (!tina.prata().isEmpty()) helper.fail("clicando nela, ela esvazia");

        boolean caiu = !level.getEntitiesOfClass(ItemEntity.class,
                new net.minecraft.world.phys.AABB(onde).inflate(6.0),
                largado -> largado.getItem().is(OccultaItems.SILVER_DUST)).isEmpty();
        if (!caiu) helper.fail("e o pó fica no chão");

        for (var largado : level.getEntitiesOfClass(ItemEntity.class,
                new net.minecraft.world.phys.AABB(onde).inflate(6.0))) {
            largado.discard();
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /**
     * <b>Ela olha para o ouro que cresce, e não para o ouro que está lá.</b>
     *
     * <p>Esta é a prova que carrega a fatia. Uma fornalha com ouro parado não lhe dá nada — ela já contou
     * esse ouro. É preciso que a pilha <b>aumente</b>, que é o que acontece quando a fornalha funde mais
     * um lingote.
     */
    @GameTest(maxTicks = 100)
    public void sheWatchesTheGoldGrowAndNotTheGoldThatIsThere(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos forno = onde.east();
        SilverVatBlockEntity tina = monta(helper, onde, forno);
        if (tina == null) {
            helper.fail("a tina devia ter alma");
            return;
        }
        if (!(level.getBlockEntity(forno) instanceof FurnaceBlockEntity fornalha)) {
            helper.fail("e a fornalha também");
            return;
        }

        /*
         * Vinte voltas com a pilha de ouro <b>parada</b>: a primeira conta-a e as outras dezenove não
         * mexem em nada. Com uma chance em cinco, vinte voltas dariam quatro pós se ela olhasse para o
         * que está lá.
         */
        fornalha.setItem(2, new ItemStack(Items.GOLD_INGOT, 8));
        for (int volta = 0; volta < 20; volta++) {
            SilverVatBlockEntity.bate(level, onde, tina);
        }
        if (!tina.prata().isEmpty()) {
            helper.fail("ouro parado não lhe dá nada; deu " + tina.prata());
        }

        /*
         * E agora com a pilha a crescer, uma vez por volta: com uma chance em cinco, em sessenta voltas é
         * quase impossível não sair nenhum.
         */
        for (int volta = 0; volta < 60; volta++) {
            fornalha.setItem(2, new ItemStack(Items.GOLD_INGOT, 1 + volta % 60));
            SilverVatBlockEntity.bate(level, onde, tina);
        }
        if (tina.prata().isEmpty()) {
            helper.fail("e o ouro que cresce dá pó de prata");
        }

        level.setBlockAndUpdate(forno, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
