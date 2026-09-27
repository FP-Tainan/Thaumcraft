package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.kettle.KettleRecipes;
import net.thaumcraft.occulta.spirit.DreamCatcherBlock;
import net.thaumcraft.occulta.spirit.DreamCatcherBlockEntity;
import net.thaumcraft.occulta.spirit.DreamWeaveItem;
import net.thaumcraft.occulta.spirit.DreamWeaveRecipe;
import net.thaumcraft.occulta.spirit.SpiritPlants;
import net.thaumcraft.occulta.spirit.SpiritWalk;
import net.thaumcraft.occulta.spirit.SpiritWorld;

import java.util.List;

/**
 * O outro lado: o Mundo dos Espíritos, o sono, o apanhador e as teias.
 */
public class OccultaSpiritGameTest {
    // ------------------------------------------------------------------ o mundo

    /** O mundo dos espíritos abre-se quando se pede, e abre-se com o chão do mundo de cima. */
    @GameTest
    public void theSpiritWorldOpens(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        ServerLevel lá = SpiritWorld.level(server);
        if (lá == null) {
            helper.fail("o mundo dos espíritos devia abrir");
            return;
        }
        if (!SpiritWorld.is(lá)) helper.fail("e devia reconhecer-se");
        if (SpiritWorld.is(server.overworld())) helper.fail("e o mundo de cima não é ele");
        if (lá.getChunkSource().getGenerator().getClass()
                != server.overworld().getChunkSource().getGenerator().getClass()) {
            helper.fail("o chão de lá é feito pelo mesmo gerador do de cá");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ a conta do pesadelo

    /** Sem apanhador de pesadelos por perto, os arredores não contam: a chance é a que veio. */
    @GameTest
    public void withoutACatcherNothingAroundCounts(GameTestHelper helper) {
        BlockPos meio = new BlockPos(1, 2, 1);
        helper.setBlock(meio, OccultaBlocks.WISPY_COTTON.defaultBlockState());
        double conta = SpiritWorld.nightmareChance(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 1)),
                0.998);
        if (Math.abs(conta - 0.998) > 1.0e-6) {
            helper.fail("sem apanhador a conta não muda, e deu " + conta);
        }
        helper.succeed();
    }

    /** Com ele, conta: o apanhador tira metade, e o algodão em volta tira mais um pouco. */
    @GameTest
    public void theCatcherBringsTheReckoningDown(GameTestHelper helper) {
        BlockPos parede = new BlockPos(1, 2, 1);
        BlockPos apanhador = new BlockPos(1, 2, 2);
        helper.setBlock(parede, Blocks.STONE.defaultBlockState());
        helper.setBlock(apanhador, OccultaBlocks.DREAM_CATCHER.defaultBlockState()
                .setValue(DreamCatcherBlock.FACING, Direction.SOUTH));
        helper.getBlockEntity(apanhador, DreamCatcherBlockEntity.class)
                .setWeave(DreamWeaveItem.Weave.NIGHTMARE);

        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 4));
        double só = SpiritWorld.nightmareChance(helper.getLevel(), onde, 0.998);
        double esperado = Math.min(Math.max(0.998 + SpiritWorld.CATCHER, 0.0), 1.0);
        if (Math.abs(só - esperado) > 1.0e-6) {
            helper.fail("com apanhador a conta cai para " + esperado + ", e deu " + só);
        }

        helper.setBlock(new BlockPos(1, 2, 5), OccultaBlocks.WISPY_COTTON.defaultBlockState());
        double comAlgodão = SpiritWorld.nightmareChance(helper.getLevel(), onde, 0.998);
        if (comAlgodão >= só - 1.0e-9) helper.fail("e o algodão em volta ainda a faz cair");
        helper.succeed();
    }

    /** E uma conta já fechada nos dois extremos não se mexe. */
    @GameTest
    public void aClosedReckoningDoesNotMove(GameTestHelper helper) {
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        if (SpiritWorld.nightmareChance(helper.getLevel(), onde, 0.0) != 0.0) helper.fail("zero fica zero");
        if (SpiritWorld.nightmareChance(helper.getLevel(), onde, 1.0) != 1.0) helper.fail("e um fica um");
        helper.succeed();
    }

    // ------------------------------------------------------------------ deitar-se e levantar-se

    /** Adormecer leva o espírito para lá, deixa o corpo cá e troca a mochila; acordar desfaz tudo. */
    @GameTest(maxTicks = 100)
    public void sleepingCrossesOverAndWakingComesBack(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getInventory().add(new ItemStack(Items.DIAMOND, 7));
        quem.getInventory().add(new ItemStack(OccultaItems.ICY_NEEDLE));

        if (!SpiritWorld.fallAsleep(quem, 0.0)) {
            helper.fail("devia adormecer");
            return;
        }
        if (!SpiritWalk.walking(quem)) helper.fail("e passar a andar em espírito");
        if (!SpiritWorld.is(quem.level())) helper.fail("e acordar do outro lado");
        if (quem.getInventory().countItem(Items.DIAMOND) != 0) {
            helper.fail("o diamante fica com o corpo");
        }
        if (quem.getInventory().countItem(OccultaItems.ICY_NEEDLE) != 1) {
            helper.fail("e a agulha atravessa com ele");
        }

        if (!SpiritWorld.wakeUp(quem)) {
            helper.fail("e devia acordar");
            return;
        }
        if (SpiritWalk.walking(quem)) helper.fail("e deixar de andar em espírito");
        if (SpiritWorld.is(quem.level())) helper.fail("e voltar ao mundo de cá");
        if (quem.getInventory().countItem(Items.DIAMOND) != 7) {
            helper.fail("e achar o diamante onde o deixou");
        }
        helper.succeed();
    }

    /** Já do outro lado, não se adormece outra vez. */
    @GameTest(maxTicks = 100)
    public void youDoNotFallAsleepTwice(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (!SpiritWorld.fallAsleep(quem, 0.0)) {
            helper.fail("devia adormecer uma vez");
            return;
        }
        if (SpiritWorld.fallAsleep(quem, 0.0)) helper.fail("e não uma segunda");
        SpiritWorld.wakeUp(quem);
        helper.succeed();
    }

    // ------------------------------------------------------------------ o apanhador

    /** A teia é o apanhador: pregada numa parede, vira o bloco com aquele feitio dentro. */
    @GameTest
    public void theWeaveIsTheCatcher(GameTestHelper helper) {
        BlockPos parede = new BlockPos(1, 2, 1);
        helper.setBlock(parede, Blocks.STONE.defaultBlockState());
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.DREAM_WEAVE_EAT));

        bate(helper, parede, quem, Direction.SOUTH);
        BlockPos casa = parede.south();
        if (!helper.getBlockState(casa).is(OccultaBlocks.DREAM_CATCHER)) {
            helper.fail("a teia devia pregar um apanhador na face batida");
            return;
        }
        var alma = helper.getBlockEntity(casa, DreamCatcherBlockEntity.class);
        if (alma.weave() != DreamWeaveItem.Weave.EAT) helper.fail("e com a teia que era");
        if (!quem.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) helper.fail("e gastar a teia");
        if (!alma.drop().is(OccultaItems.DREAM_WEAVE_EAT)) helper.fail("e devolvê-la ao ser quebrado");
        helper.succeed();
    }

    /** E não se prega no chão nem no teto: só de lado, como no original. */
    @GameTest
    public void theWeaveOnlyGoesOnAWall(GameTestHelper helper) {
        BlockPos chão = new BlockPos(1, 2, 1);
        helper.setBlock(chão, Blocks.STONE.defaultBlockState());
        ServerPlayer quem = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.DREAM_WEAVE_MOVE));

        bate(helper, chão, quem, Direction.UP);
        if (helper.getBlockState(chão.above()).is(OccultaBlocks.DREAM_CATCHER)) {
            helper.fail("no teto do bloco não se prega nada");
        }
        if (quem.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) helper.fail("e a teia não se gasta");
        helper.succeed();
    }

    // ------------------------------------------------------------------ tecer

    /** A bancada das teias: cada par de cantos dá a sua, e um par que não é par não dá nenhuma. */
    @GameTest
    public void theWeaveRecipeReadsTheTwoCorners(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        var move = weave(splash(Potions.LONG_SWIFTNESS), splash(Potions.LONG_SLOWNESS), false);
        if (!DreamWeaveRecipe.INSTANCE.matches(move, level)) {
            helper.fail("rapidez com lentidão devia dar teia");
            return;
        }
        if (!DreamWeaveRecipe.INSTANCE.assemble(move).is(OccultaItems.DREAM_WEAVE_MOVE)) {
            helper.fail("e devia dar a do passo ligeiro");
        }

        // e na ordem trocada também, que é o que o espelho da receita antiga fazia
        var trocada = weave(splash(Potions.LONG_SLOWNESS), splash(Potions.LONG_SWIFTNESS), false);
        if (!DreamWeaveRecipe.INSTANCE.assemble(trocada).is(OccultaItems.DREAM_WEAVE_MOVE)) {
            helper.fail("e a ordem dos dois cantos não importa");
        }

        var dig = weave(splash(Potions.LONG_STRENGTH), splash(Potions.LONG_WEAKNESS), false);
        if (!DreamWeaveRecipe.INSTANCE.assemble(dig).is(OccultaItems.DREAM_WEAVE_DIG)) {
            helper.fail("força com fraqueza dá a da mão rápida");
        }

        var eat = weave(splash(Potions.STRONG_HEALING), new ItemStack(OccultaItems.MELLIFLUOUS_HUNGER), false);
        if (!DreamWeaveRecipe.INSTANCE.assemble(eat).is(OccultaItems.DREAM_WEAVE_EAT)) {
            helper.fail("cura com a Fome Melíflua dá a da fartura");
        }

        var intensity = weave(new ItemStack(OccultaItems.BREW_OF_FLOWING_SPIRIT),
                new ItemStack(OccultaItems.BREW_OF_SLEEPING), false);
        if (!DreamWeaveRecipe.INSTANCE.assemble(intensity).is(OccultaItems.DREAM_WEAVE_INTENSITY)) {
            helper.fail("e os dois cozimentos dão a da intensidade");
        }

        var solta = weave(splash(Potions.LONG_SWIFTNESS), splash(Potions.LONG_POISON), false);
        if (DreamWeaveRecipe.INSTANCE.matches(solta, level)) {
            helper.fail("dois cantos que não casam não dão teia nenhuma");
        }
        helper.succeed();
    }

    /** A do pesadelo é a única toda de cordel: com fio enfeitado no meio, ela não sai. */
    @GameTest
    public void theNightmareWeaveIsAllTwine(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var cordel = weave(splash(Potions.LONG_POISON), splash(Potions.LONG_NIGHT_VISION), true);
        if (!DreamWeaveRecipe.INSTANCE.assemble(cordel).is(OccultaItems.DREAM_WEAVE_NIGHTMARE)) {
            helper.fail("veneno com visão noturna, em cordel, dá a dos pesadelos");
        }
        var fio = weave(splash(Potions.LONG_POISON), splash(Potions.LONG_NIGHT_VISION), false);
        if (DreamWeaveRecipe.INSTANCE.matches(fio, level)) {
            helper.fail("e com fio enfeitado no meio não dá nada");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ o pote

    /** O Cozimento do Sono está na tabela do pote, e o do Espírito Corrente só coze do outro lado. */
    @GameTest
    public void theKettleKnowsTheTwoBrews(GameTestHelper helper) {
        var sono = KettleRecipes.of(OccultaItems.BREW_OF_SLEEPING);
        if (sono == null) {
            helper.fail("o Cozimento do Sono devia estar na tabela do pote");
            return;
        }
        if (sono.level() != null) helper.fail("e coze-se em qualquer mundo");

        var espírito = KettleRecipes.of(OccultaItems.BREW_OF_FLOWING_SPIRIT);
        if (espírito == null) {
            helper.fail("e o do Espírito Corrente também");
            return;
        }
        if (espírito.level() != SpiritWorld.LEVEL) {
            helper.fail("mas só do outro lado, e está preso a " + espírito.level());
        }

        List<ItemStack> dentro = new java.util.ArrayList<>();
        for (var item : espírito.inputs()) dentro.add(new ItemStack(item));
        if (espírito.matches(dentro, false, helper.getLevel())) {
            helper.fail("e um pote do mundo de cá não a dá");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ as plantas

    /** O algodão só se espalha do outro lado: no mundo de cá ele fica onde está. */
    @GameTest(maxTicks = 60)
    public void cottonOnlySpreadsOverThere(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.GRASS_BLOCK.defaultBlockState());
        helper.setBlock(onde, OccultaBlocks.WISPY_COTTON.defaultBlockState());
        for (int i = 0; i < 200; i++) {
            helper.getBlockState(onde).randomTick(helper.getLevel(), helper.absolutePos(onde),
                    helper.getLevel().getRandom());
        }
        int achados = 0;
        for (BlockPos casa : BlockPos.betweenClosed(new BlockPos(0, 2, 0), new BlockPos(4, 2, 4))) {
            if (helper.getBlockState(casa).is(OccultaBlocks.WISPY_COTTON)) achados++;
        }
        if (achados != 1) helper.fail("no mundo de cá o algodão não se espalha, e nasceram " + achados);
        helper.succeed();
    }

    /**
     * A moita do algodão <b>não sai do pedaço</b>, e esta é a prova que impede o travamento de voltar.
     *
     * <p>Ela é semeada durante o carregamento do pedaço. Uma planta posta na casa do pedaço ao lado faz o jogo
     * gerá-lo ali mesmo, de dentro do carregamento do primeiro — e esse dispara o seguinte, e o servidor para.
     */
    @GameTest
    public void theCottonPatchStaysInsideItsChunk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos aqui = helper.absolutePos(new BlockPos(1, 1, 1));
        var chunk = level.getChunkAt(aqui);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        int postas = 0;
        for (long semente = 0L; semente < 30L; semente++) {
            postas += SpiritPlants.patch(chunk, RandomSource.create(semente));
        }
        if (postas == 0) helper.fail("a moita devia ter posto alguma coisa");

        // e agora nada de algodão fora das dezesseis casas deste pedaço
        for (BlockPos casa : BlockPos.betweenClosed(
                new BlockPos(minX - 16, level.getMinY(), minZ - 16),
                new BlockPos(minX + 31, level.getMaxY(), minZ + 31))) {
            if (!level.getBlockState(casa).is(OccultaBlocks.WISPY_COTTON)) continue;
            if (casa.getX() < minX || casa.getX() > minX + 15
                    || casa.getZ() < minZ || casa.getZ() > minZ + 15) {
                helper.fail("a moita saiu do pedaço, em " + casa);
                return;
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ as ferramentas da prova

    /** Bater naquela face daquele bloco com o que a pessoa tem na mão. */
    private static void bate(GameTestHelper helper, BlockPos onde, ServerPlayer quem, Direction face) {
        BlockPos certo = helper.absolutePos(onde);
        helper.useBlock(onde, quem, new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(certo), face, certo, false));
    }

    /** Uma poção de atirar daquele feitio. */
    private static ItemStack splash(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> qual) {
        return PotionContents.createItemStack(Items.SPLASH_POTION, qual);
    }

    /** A bancada de uma teia, montada com a forma do original. */
    private static CraftingInput weave(ItemStack esquerda, ItemStack direita, boolean cordel) {
        ItemStack lado = new ItemStack(cordel ? OccultaItems.TORMENTED_TWINE : OccultaItems.FANCIFUL_THREAD);
        return CraftingInput.of(3, 3, List.of(
                esquerda, new ItemStack(OccultaItems.DIAMOND_VAPOUR), direita,
                lado, new ItemStack(Items.ITEM_FRAME), lado.copy(),
                new ItemStack(Items.FEATHER), new ItemStack(OccultaItems.TORMENTED_TWINE),
                new ItemStack(Items.FEATHER)));
    }
}
