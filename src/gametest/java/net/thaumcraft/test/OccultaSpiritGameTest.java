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
import net.thaumcraft.occulta.kettle.KettleBrews;
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

    /** O mundo dos espíritos se abre quando se pede, e se abre com o chão do mundo de cima. */
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
        double teto = Math.min(Math.max(0.998 + SpiritWorld.CATCHER, 0.0), 1.0);
        // a suíte corre num mundo só, e o que as provas ao lado puserem também entra na conta dos oito: o que
        // esta prova exige é que o apanhador tenha feito a conta cair, e nunca subir
        if (só > teto + 1.0e-6) helper.fail("com apanhador a conta cai a " + teto + " ou menos, e deu " + só);
        if (só < 0.0) helper.fail("e não passa do chão");

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
        if (sono.level() != null) helper.fail("e se coze em qualquer mundo");

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

        // o que já havia de algodão em volta não é desta prova: a suíte corre num mundo só
        java.util.Set<BlockPos> antes = new java.util.HashSet<>();
        var volta = BlockPos.betweenClosed(
                new BlockPos(minX - 16, level.getMinY(), minZ - 16),
                new BlockPos(minX + 31, level.getMaxY(), minZ + 31));
        for (BlockPos casa : volta) {
            if (level.getBlockState(casa).is(OccultaBlocks.WISPY_COTTON)) antes.add(casa.immutable());
        }

        int postas = 0;
        for (long semente = 0L; semente < 30L; semente++) {
            postas += SpiritPlants.patch(chunk, RandomSource.create(semente));
        }
        if (postas == 0) helper.fail("a moita devia ter posto alguma coisa");

        // e nada do que ela pôs está fora das dezesseis casas deste pedaço
        for (BlockPos casa : BlockPos.betweenClosed(
                new BlockPos(minX - 16, level.getMinY(), minZ - 16),
                new BlockPos(minX + 31, level.getMaxY(), minZ + 31))) {
            if (!level.getBlockState(casa).is(OccultaBlocks.WISPY_COTTON)) continue;
            if (antes.contains(casa)) continue;
            if (casa.getX() < minX || casa.getX() > minX + 15
                    || casa.getZ() < minZ || casa.getZ() > minZ + 15) {
                helper.fail("a moita saiu do pedaço, em " + casa);
                return;
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ o espírito fluente

    /** A destilação que abre o fim da linha está na tabela, e dá o que o original dá. */
    @GameTest
    public void theDistilleryBreaksTheSpiritApart(GameTestHelper helper) {
        var receita = net.thaumcraft.occulta.DistilleryRecipes.find(
                new ItemStack(OccultaItems.BREW_OF_FLOWING_SPIRIT),
                new ItemStack(OccultaItems.OIL_OF_VITRIOL),
                new ItemStack(OccultaItems.CLAY_JAR, 64));
        if (receita == null) {
            helper.fail("o Espírito Corrente com óleo de vitríolo devia destilar");
            return;
        }
        if (receita.jars() != 2) helper.fail("e gastar dois potes, e gasta " + receita.jars());

        int lágrimas = 0;
        boolean vontade = false;
        boolean medo = false;
        for (ItemStack sai : receita.outputs()) {
            if (sai.is(OccultaItems.FOCUSED_WILL)) vontade = true;
            if (sai.is(OccultaItems.CONDENSED_FEAR)) medo = true;
            if (sai.is(OccultaItems.BREW_OF_HOLLOW_TEARS)) lágrimas += sai.getCount();
        }
        if (!vontade) helper.fail("dela sai a Vontade Focada");
        if (!medo) helper.fail("e o Medo Condensado");
        if (lágrimas != 8) helper.fail("e oito frascos de Lágrimas Ocas, e saíram " + lágrimas);

        // e a ordem das duas não importa
        if (net.thaumcraft.occulta.DistilleryRecipes.find(new ItemStack(OccultaItems.OIL_OF_VITRIOL),
                new ItemStack(OccultaItems.BREW_OF_FLOWING_SPIRIT),
                new ItemStack(OccultaItems.CLAY_JAR, 64)) == null) {
            helper.fail("e a ordem das duas coisas não importa");
        }
        helper.succeed();
    }

    /** Os cinco Cozimentos Sólidos estão no pote, e todos pedem os dois mil de poder do original. */
    @GameTest
    public void theKettleKnowsTheFiveSolids(GameTestHelper helper) {
        net.minecraft.world.item.Item[] cinco = {
                OccultaItems.BREW_OF_SOLID_ROCK, OccultaItems.BREW_OF_SOLID_DIRT,
                OccultaItems.BREW_OF_SOLID_SAND, OccultaItems.BREW_OF_SOLID_SANDSTONE,
                OccultaItems.BREW_OF_SOLID_EROSION};
        for (var qual : cinco) {
            var receita = KettleRecipes.of(qual);
            if (receita == null) {
                helper.fail("falta no pote: " + qual);
                return;
            }
            if (receita.power() != 2000.0f) {
                helper.fail(qual + " pede dois mil de poder, e pede " + receita.power());
            }
            if (!receita.inputs().contains(OccultaItems.SPANISH_MOSS)) {
                helper.fail(qual + " leva Musgo Espanhol");
            }
        }
        helper.succeed();
    }

    /** O frasco do Espírito Corrente faz poça onde bate, e o das Lágrimas Ocas também. */
    @GameTest
    public void thrownBrewsMakePools(GameTestHelper helper) {
        BlockPos chão = new BlockPos(1, 1, 1);
        helper.setBlock(chão, Blocks.STONE.defaultBlockState());
        BlockPos acima = chão.above();

        var bateu = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(chão)),
                Direction.UP, helper.absolutePos(chão), false);
        if (!KettleBrews.Kind.FLOWING_SPIRIT.impact(helper.getLevel(), bateu, null)) {
            helper.fail("o frasco devia fazer poça");
            return;
        }
        if (!helper.getBlockState(acima).is(OccultaBlocks.FLOWING_SPIRIT)) {
            helper.fail("e a poça é de Espírito Fluente");
        }
        helper.succeed();
    }

    /** E o Sólido endurece a poça inteira de Lágrimas Ocas, e só ela. */
    @GameTest
    public void theSolidBrewHardensTheWholePool(GameTestHelper helper) {
        // uma poça de cinco casas em linha, cada uma sobre pedra
        for (int i = 0; i < 5; i++) {
            helper.setBlock(new BlockPos(1 + i, 1, 1), Blocks.STONE.defaultBlockState());
            helper.setBlock(new BlockPos(1 + i, 2, 1), OccultaBlocks.HOLLOW_TEARS.defaultBlockState());
        }
        // e uma de Espírito Fluente ao lado, que não é para mexer
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 3), OccultaBlocks.FLOWING_SPIRIT.defaultBlockState());

        BlockPos alvo = helper.absolutePos(new BlockPos(1, 2, 1));
        var bateu = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(alvo), Direction.UP, alvo, false);
        if (!KettleBrews.Kind.SOLID_ROCK.impact(helper.getLevel(), bateu, null)) {
            helper.fail("o frasco devia pegar na poça");
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (!helper.getBlockState(new BlockPos(1 + i, 2, 1)).is(Blocks.STONE)) {
                helper.fail("a poça inteira devia virar pedra, e a casa " + i + " não virou");
                return;
            }
        }
        if (!helper.getBlockState(new BlockPos(1, 2, 3)).is(OccultaBlocks.FLOWING_SPIRIT)) {
            helper.fail("e o Espírito Fluente ao lado não se mexe");
        }
        helper.succeed();
    }

    /** Batendo onde não há Lágrimas Ocas, o Sólido não faz nada — e o frasco volta ao chão. */
    @GameTest
    public void theSolidBrewNeedsAPool(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        BlockPos alvo = helper.absolutePos(new BlockPos(1, 1, 1));
        var bateu = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(alvo), Direction.UP, alvo, false);
        if (KettleBrews.Kind.SOLID_DIRT.impact(helper.getLevel(), bateu, null)) {
            helper.fail("sem poça, o frasco não pega");
        }
        helper.succeed();
    }

    /** O da Erosão tira a poça e a casa debaixo dela. */
    @GameTest
    public void theErosionSolidTakesTheGroundToo(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIRT.defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), OccultaBlocks.HOLLOW_TEARS.defaultBlockState());
        BlockPos alvo = helper.absolutePos(new BlockPos(1, 2, 1));
        var bateu = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(alvo), Direction.UP, alvo, false);
        if (!KettleBrews.Kind.SOLID_EROSION.impact(helper.getLevel(), bateu, null)) {
            helper.fail("o frasco devia pegar");
            return;
        }
        if (!helper.getBlockState(new BlockPos(1, 2, 1)).isAir()) helper.fail("a poça sai");
        if (!helper.getBlockState(new BlockPos(1, 1, 1)).isAir()) helper.fail("e o chão debaixo dela também");
        helper.succeed();
    }

    /** O Espírito Fluente cura quem é gente e enfraquece quem não é. */
    @GameTest(maxTicks = 60)
    public void theSpiritKnowsWhoStepsIn(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(onde, OccultaBlocks.FLOWING_SPIRIT.defaultBlockState());

        var porco = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, onde);
        var zumbi = helper.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE, onde);
        helper.runAfterDelay(20, () -> {
            if (!porco.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
                helper.fail("o porco sai curado");
                return;
            }
            if (!zumbi.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)) {
                helper.fail("e o zumbi, fraco");
                return;
            }
            helper.succeed();
        });
    }

    /** E o Algodão Perturbado largado nele volta a ser Algodão Sonhador. */
    @GameTest(maxTicks = 80)
    public void theSpiritUndoesTheNightmareInTheCotton(GameTestHelper helper) {
        BlockPos onde = new BlockPos(1, 2, 1);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE.defaultBlockState());
        helper.setBlock(onde, OccultaBlocks.FLOWING_SPIRIT.defaultBlockState());

        var certo = helper.absolutePos(onde);
        var largado = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(),
                certo.getX() + 0.5, certo.getY() + 0.3, certo.getZ() + 0.5,
                new ItemStack(OccultaItems.DISTURBED_COTTON, 3));
        helper.getLevel().addFreshEntity(largado);

        helper.runAfterDelay(30, () -> {
            if (!largado.getItem().is(OccultaItems.WISPY_COTTON)) {
                helper.fail("o algodão devia perder o pesadelo, e ficou " + largado.getItem());
                return;
            }
            if (largado.getItem().getCount() != 3) helper.fail("e sem perder nenhum");
            helper.succeed();
        });
    }

    /** As poças de Espírito Fluente também derrubam a conta do pesadelo. */
    @GameTest
    public void poolsBringTheReckoningDown(GameTestHelper helper) {
        BlockPos parede = new BlockPos(1, 2, 1);
        BlockPos apanhador = new BlockPos(1, 2, 2);
        helper.setBlock(parede, Blocks.STONE.defaultBlockState());
        helper.setBlock(apanhador, OccultaBlocks.DREAM_CATCHER.defaultBlockState()
                .setValue(DreamCatcherBlock.FACING, Direction.SOUTH));
        helper.getBlockEntity(apanhador, DreamCatcherBlockEntity.class)
                .setWeave(DreamWeaveItem.Weave.NIGHTMARE);

        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 4));
        double sem = SpiritWorld.nightmareChance(helper.getLevel(), onde, 0.998);

        helper.setBlock(new BlockPos(1, 2, 5), OccultaBlocks.FLOWING_SPIRIT.defaultBlockState());
        double com = SpiritWorld.nightmareChance(helper.getLevel(), onde, 0.998);
        if (com >= sem - 1.0e-9) helper.fail("a poça devia derrubar a conta, e deu " + com + " contra " + sem);
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
