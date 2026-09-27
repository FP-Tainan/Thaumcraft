package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.occulta.MutandisItem;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaRituals;
import net.thaumcraft.occulta.WitchesCauldronBlockEntity;

import java.util.List;

/**
 * O Caldeirão da Bruxa: como se faz, quando ferve, o que engole e o que larga.
 */
public class OccultaCauldronGameTest {
    /** A Pasta de Unção faz o caldeirão de um caldeirão comum, e a água que lá estava não se perde. */
    @GameTest
    public void theAnointingPasteMakesTheCauldron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(onde, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack pasta = new ItemStack(OccultaItems.ANOINTING_PASTE, 2);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pasta);
        OccultaItems.ANOINTING_PASTE.useOn(new net.minecraft.world.item.context.UseOnContext(level, player,
                net.minecraft.world.InteractionHand.MAIN_HAND, pasta,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(onde),
                        net.minecraft.core.Direction.UP, onde, false)));

        if (!level.getBlockState(onde).is(OccultaBlocks.WITCHES_CAULDRON)) {
            helper.fail("a pasta devia fazer o Caldeirão da Bruxa; ficou " + level.getBlockState(onde));
            return;
        }
        if (!(level.getBlockEntity(onde) instanceof WitchesCauldronBlockEntity caldeirão)) {
            helper.fail("e ele devia ter miolo");
            return;
        }
        if (!caldeirão.isFull()) helper.fail("a água do caldeirão comum passa para ele; ficou " + caldeirão.water());
        if (pasta.getCount() != 1) helper.fail("e a pasta gasta-se");

        // e num caldeirão que não é caldeirão a pasta não faz nada
        BlockPos outro = helper.absolutePos(new BlockPos(3, 2, 1));
        level.setBlockAndUpdate(outro, Blocks.STONE.defaultBlockState());
        OccultaItems.ANOINTING_PASTE.useOn(new net.minecraft.world.item.context.UseOnContext(level, player,
                net.minecraft.world.InteractionHand.MAIN_HAND, pasta,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(outro),
                        net.minecraft.core.Direction.UP, outro, false)));
        if (!level.getBlockState(outro).is(Blocks.STONE)) helper.fail("pedra não vira caldeirão");
        helper.succeed();
    }

    /** Cheio e com fogo embaixo, ele ferve em cinco segundos — e só então engole o que cai. */
    @GameTest(maxTicks = 200)
    public void itBoilsOverFire(GameTestHelper helper) {
        WitchesCauldronBlockEntity caldeirão = place(helper, new BlockPos(1, 2, 1), true, true);
        if (caldeirão.isBoiling()) helper.fail("ainda não pode estar fervendo");
        helper.succeedWhen(() -> {
            if (!caldeirão.isBoiling()) throw helper.assertionException("o caldeirão ainda não ferveu");
        });
    }

    /** Sem água, ou sem fogo, não ferve. */
    @GameTest(maxTicks = 200)
    public void itNeedsBothWaterAndFire(GameTestHelper helper) {
        WitchesCauldronBlockEntity semÁgua = place(helper, new BlockPos(1, 2, 1), false, true);
        WitchesCauldronBlockEntity semFogo = place(helper, new BlockPos(3, 2, 1), true, false);
        helper.runAfterDelay(150, () -> {
            if (semÁgua.isBoiling()) helper.fail("caldeirão vazio não ferve");
            if (semFogo.isBoiling()) helper.fail("caldeirão sem fogo não ferve");
            helper.succeed();
        });
    }

    /** A receita do Mutandis: raiz e exalação dentro, e o ovo por último. */
    @GameTest(maxTicks = 400)
    public void theMutandisRitualRuns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relativo = new BlockPos(1, 2, 1);
        WitchesCauldronBlockEntity caldeirão = place(helper, relativo, true, true);
        BlockPos onde = helper.absolutePos(relativo);

        BlockPos fogo = onde.below();
        if (caldeirão.water() == 0) helper.fail("o caldeirão devia estar cheio logo depois de posto");
        helper.succeedWhen(() -> {
            // primeiro o que se espera ver: os seis Mutandis no chão, que é o fim da receita
            List<ItemEntity> caiu = level.getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(onde).inflate(2.0));
            boolean mutandis = caiu.stream().anyMatch(item -> item.getItem().is(OccultaItems.MUTANDIS)
                    && item.getItem().getCount() == 6);
            if (mutandis) {
                // e o caldeirão esvazia quando a receita sai, que é o do original
                if (caldeirão.water() != 0) helper.fail("o caldeirão esvazia quando a receita sai");
                if (!caldeirão.inside().isEmpty()) helper.fail("e não guarda o que foi jogado nele");
                return;
            }
            if (caldeirão.isRitualInProgress()) throw helper.assertionException("a receita está a mexer");
            // o fogo às vezes se apaga sozinho no mundo de prova; aqui ele se mantém aceso
            if (!level.getBlockState(fogo).is(Blocks.FIRE)) {
                level.setBlockAndUpdate(fogo, Blocks.FIRE.defaultBlockState());
            }
            if (!caldeirão.isBoiling()) throw helper.assertionException("o caldeirão ainda não ferveu");
            if (caldeirão.inside().isEmpty()) {
                // fervendo: joga-se o que a receita pede, e o ovo por último
                if (!caldeirão.addItem(new ItemStack(OccultaItems.MANDRAKE_ROOT))) {
                    helper.fail("a raiz de mandrágora devia entrar na panela");
                }
                if (!caldeirão.addItem(new ItemStack(OccultaItems.EXHALE_OF_THE_HORNED_ONE))) {
                    helper.fail("a exalação também");
                }
                // o diamante entraria: desde os cozimentos ele é ingrediente de porte. Quem não serve a nada
                // é o pedregulho, e é esse que o caldeirão recusa.
                if (caldeirão.addItem(new ItemStack(Items.COBBLESTONE))) {
                    helper.fail("mas pedregulho não serve a receita nenhuma e não entra");
                }
                if (!caldeirão.addItem(new ItemStack(Items.EGG))) helper.fail("e o ovo dispara a receita");
            }
            throw helper.assertionException("ainda não saíram os seis Mutandis");
        });
    }

    /** O caldeirão também cozinha carne, que é receita sem ingrediente nenhum. */
    @GameTest
    public void itCooksMeat(GameTestHelper helper) {
        ItemStack sai = OccultaRituals.result(Items.PORKCHOP, List.of());
        if (!sai.is(Items.COOKED_PORKCHOP)) helper.fail("o porco cru sai assado; saiu " + sai);
        if (!OccultaRituals.result(Items.BEEF, List.of()).is(Items.COOKED_BEEF)) helper.fail("e a carne também");
        if (!OccultaRituals.result(Items.EGG, List.of()).isEmpty()) {
            helper.fail("mas o ovo sozinho não faz nada: a receita dele pede raiz e exalação");
        }
        helper.succeed();
    }

    /** O Mutandis troca uma planta por outra da lista, e é por aí que se chega às mudas do ofício. */
    @GameTest
    public void theMutandisChangesOnePlantForAnother(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlockAndUpdate(onde.below(), Blocks.DIRT.defaultBlockState());
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);

        boolean virouMuda = false;
        for (int volta = 0; volta < 200 && !virouMuda; volta++) {
            level.setBlockAndUpdate(onde, Blocks.OAK_SAPLING.defaultBlockState());
            ItemStack mutandis = new ItemStack(OccultaItems.MUTANDIS, 64);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, mutandis);
            OccultaItems.MUTANDIS.useOn(new net.minecraft.world.item.context.UseOnContext(level, player,
                    net.minecraft.world.InteractionHand.MAIN_HAND, mutandis,
                    new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(onde),
                            net.minecraft.core.Direction.UP, onde, false)));
            var virou = level.getBlockState(onde);
            if (virou.is(Blocks.OAK_SAPLING)) helper.fail("a muda tinha de virar outra coisa");
            if (virou.is(OccultaBlocks.ROWAN_SAPLING) || virou.is(OccultaBlocks.ALDER_SAPLING)
                    || virou.is(OccultaBlocks.HAWTHORN_SAPLING)) {
                virouMuda = true;
            }
        }
        if (!virouMuda) helper.fail("das duzentas voltas, alguma tinha de dar muda do ofício");

        // o comum não alcança as plantações; o Extremis alcança
        if (((MutandisItem) OccultaItems.MUTANDIS).list().contains(Blocks.WHEAT)) {
            helper.fail("o Mutandis comum não mexe em trigo");
        }
        if (!((MutandisItem) OccultaItems.MUTANDIS_EXTREMIS).list().contains(Blocks.WHEAT)) {
            helper.fail("o Extremis mexe");
        }
        helper.succeed();
    }

    /** E o thaumômetro lê no caldeirão e no Mutandis o que o mod diz. */
    @GameTest
    public void theCauldronAndTheMutandisHaveTheirAspects(GameTestHelper helper) {
        var caldeirão = ObjectAspects.of(OccultaItems.WITCHES_CAULDRON);
        if (caldeirão.getAmount(Aspects.CRAFT) != 8) helper.fail("o caldeirão é fabrico 8; veio " + caldeirão);
        var mutandis = ObjectAspects.of(OccultaItems.MUTANDIS);
        if (mutandis.getAmount(Aspects.EXCHANGE) != 4) helper.fail("o Mutandis é permutatio 4; veio " + mutandis);
        var extremis = ObjectAspects.of(OccultaItems.MUTANDIS_EXTREMIS);
        if (extremis.getAmount(Aspects.EXCHANGE) != 8) helper.fail("e o Extremis, oito; veio " + extremis);
        helper.succeed();
    }

    /** Põe um caldeirão, com ou sem água e com ou sem fogo embaixo. */
    private static WitchesCauldronBlockEntity place(GameTestHelper helper, BlockPos onde, boolean água,
                                                    boolean fogo) {
        ServerLevel level = helper.getLevel();
        BlockPos absoluto = helper.absolutePos(onde);
        level.setBlockAndUpdate(absoluto, OccultaBlocks.WITCHES_CAULDRON.defaultBlockState());
        if (fogo) {
            level.setBlockAndUpdate(absoluto.below(2), Blocks.NETHERRACK.defaultBlockState());
            level.setBlockAndUpdate(absoluto.below(), Blocks.FIRE.defaultBlockState());
        }
        if (!(level.getBlockEntity(absoluto) instanceof WitchesCauldronBlockEntity caldeirão)) {
            helper.fail("o caldeirão devia ter miolo");
            throw new IllegalStateException();
        }
        if (água) caldeirão.fill(WitchesCauldronBlockEntity.FULL);
        return caldeirão;
    }
}
