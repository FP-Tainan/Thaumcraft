package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PoppetItem;
import net.thaumcraft.occulta.PoppetShelfBlockEntity;
import net.thaumcraft.occulta.Poppets;
import net.thaumcraft.occulta.TaglockItem;

/**
 * As bonecas: o vínculo que as prende, o que elas guardam e a prateleira que as faz valer de longe.
 */
public class OccultaPoppetGameTest {
    /** O frasco enche-se de quem se toca, e diz de quem é. */
    @GameTest
    public void theTaglockTakesAName(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack frasco = new ItemStack(OccultaItems.TAGLOCK);
        if (TaglockItem.isBound(frasco)) helper.fail("um frasco novo está vazio");

        TaglockItem.bind(frasco, quem);
        if (!TaglockItem.isBound(frasco)) helper.fail("depois de tocar alguém, ele está cheio");
        if (!TaglockItem.isFor(frasco, quem)) helper.fail("e é daquela pessoa");

        var outro = helper.makeMockPlayer(GameType.SURVIVAL);
        if (TaglockItem.isFor(frasco, outro)) helper.fail("e não de outra qualquer");
        helper.succeed();
    }

    /** A boneca da terra toma a queda que mataria, e desfaz-se. */
    @GameTest
    public void theEarthPoppetTakesTheFall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setHealth(2.0f);

        ItemStack boneca = new ItemStack(OccultaItems.EARTH_POPPET);
        TaglockItem.bind(boneca, quem);
        quem.getInventory().add(boneca);

        var queda = level.damageSources().fall();
        if (!Poppets.guard(level, quem, queda, 10.0f)) {
            helper.fail("a queda que mataria devia ser tomada pela boneca");
        }
        boolean aindaTem = false;
        for (int i = 0; i < quem.getInventory().getContainerSize(); i++) {
            if (quem.getInventory().getItem(i).getItem() instanceof PoppetItem) aindaTem = true;
        }
        if (aindaTem) helper.fail("e a boneca desfaz-se ao valer");
        helper.succeed();
    }

    /** Uma boneca que não é sua não guarda você. */
    @GameTest
    public void aPoppetOfAnotherDoesNothing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        var outro = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setHealth(2.0f);

        ItemStack boneca = new ItemStack(OccultaItems.EARTH_POPPET);
        TaglockItem.bind(boneca, outro);
        quem.getInventory().add(boneca);

        if (Poppets.guard(level, quem, level.damageSources().fall(), 10.0f)) {
            helper.fail("a boneca de outra pessoa não guarda quem a carrega");
        }
        helper.succeed();
    }

    /** E cada boneca guarda do que é dela: a da terra não guarda do fogo. */
    @GameTest
    public void eachPoppetGuardsItsOwnDeath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setHealth(2.0f);
        ItemStack boneca = new ItemStack(OccultaItems.EARTH_POPPET);
        TaglockItem.bind(boneca, quem);
        quem.getInventory().add(boneca);

        if (Poppets.guard(level, quem, level.damageSources().onFire(), 10.0f)) {
            helper.fail("a boneca da terra não guarda de fogo");
        }
        // já a da morte guarda de tudo
        quem.getInventory().clearContent();
        ItemStack morte = new ItemStack(OccultaItems.DEATH_POPPET);
        TaglockItem.bind(morte, quem);
        quem.getInventory().add(morte);
        if (!Poppets.guard(level, quem, level.damageSources().onFire(), 10.0f)) {
            helper.fail("a da morte guarda de qualquer coisa");
        }
        helper.succeed();
    }

    /** A boneca vale da prateleira, sem estar na mochila de ninguém. */
    @GameTest
    public void aPoppetWorksFromTheShelf(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.POPPET_SHELF.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof PoppetShelfBlockEntity prateleira)) {
            helper.fail("a prateleira devia ter alma");
            return;
        }

        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        quem.setHealth(2.0f);
        ItemStack boneca = new ItemStack(OccultaItems.WATER_POPPET);
        TaglockItem.bind(boneca, quem);
        prateleira.setItem(0, boneca);

        if (!Poppets.guard(level, quem, level.damageSources().drown(), 10.0f)) {
            helper.fail("a boneca na prateleira guarda o dono, esteja ele onde estiver");
        }
        if (!prateleira.getItem(0).isEmpty()) helper.fail("e desfaz-se ali mesmo");

        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** Na prateleira só vão bonecas. */
    @GameTest
    public void onlyPoppetsGoOnTheShelf(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(onde, OccultaBlocks.POPPET_SHELF.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof PoppetShelfBlockEntity prateleira)) {
            helper.fail("a prateleira devia ter alma");
            return;
        }
        if (!prateleira.canPlaceItem(0, new ItemStack(OccultaItems.POPPET))) {
            helper.fail("uma boneca entra");
        }
        if (prateleira.canPlaceItem(0, new ItemStack(net.minecraft.world.item.Items.STONE))) {
            helper.fail("mas pedra não");
        }
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** A da ferramenta conserta o que está quase a partir. */
    @GameTest
    public void theToolPoppetMendsWhatIsWorn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);

        ItemStack picareta = new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
        picareta.setDamageValue((int) (picareta.getMaxDamage() * 0.95f));
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, picareta);

        // sem boneca, ela fica como está
        Poppets.mend(level, quem);
        if (picareta.getDamageValue() == 0) helper.fail("sem boneca, nada se conserta");

        ItemStack boneca = new ItemStack(OccultaItems.TOOL_POPPET);
        TaglockItem.bind(boneca, quem);
        quem.getInventory().add(boneca);
        Poppets.mend(level, quem);
        if (picareta.getDamageValue() != 0) helper.fail("com a boneca, a picareta volta a nova");

        // e a boneca gasta-se, mas não se desfaz
        boolean aindaTem = false;
        for (int i = 0; i < quem.getInventory().getContainerSize(); i++) {
            if (quem.getInventory().getItem(i).getItem() instanceof PoppetItem) aindaTem = true;
        }
        if (!aindaTem) helper.fail("a da ferramenta gasta-se aos poucos, não se desfaz de uma vez");
        helper.succeed();
    }
}
