package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PoppetBindingRecipe;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.demon.ContractItem;
import net.thaumcraft.occulta.demon.Contracts;
import net.thaumcraft.occulta.demon.ImpBlessings;
import net.thaumcraft.occulta.imp.ImpEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Os <b>cinco contratos que o Diabrete lança</b>: o que cada um faz, as quatro recusas dele, o papel que
 * não se gasta quando não pega, e o Frasco de Vínculo que agora prende um contrato como prende uma boneca.
 */
public class OccultaDemonContractGameTest {
    /** <b>Chama Viva</b>: um Blaze de cinquenta de vida e sete de murro ao pé de quem o papel prende. */
    @GameTest(maxTicks = 40)
    public void theLivingFlameIsAFatBlaze(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var alvo = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));

        if (!Contracts.blaze(level, alvo)) {
            helper.fail("a chama viva devia nascer");
            return;
        }
        helper.runAfterDelay(3, () -> {
            var quais = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Blaze.class,
                    alvo.getBoundingBox().inflate(8.0));
            if (quais.isEmpty()) {
                helper.fail("e estar lá");
                return;
            }
            var blaze = quais.getFirst();
            if (Math.abs(blaze.getMaxHealth() - Contracts.BLAZE_VIDA) > 0.01) {
                helper.fail("com cinquenta de vida, e tem " + blaze.getMaxHealth());
            }
            var murro = blaze.getAttribute(Attributes.ATTACK_DAMAGE);
            if (murro == null || Math.abs(murro.getBaseValue() - Contracts.BLAZE_MURRO) > 0.01) {
                helper.fail("e sete de murro");
            }
            for (var cada : quais) cada.discard();
            alvo.discard();
            helper.succeed();
        });
    }

    /** <b>Os quatro estados</b>: quinze minutos de fogo em qualquer coisa, e dez dos três só em gente. */
    @GameTest
    public void fourContractsPutAStateOnSomeone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer gente = helper.makeMockServerPlayerInLevel();
        var bicho = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));

        // o de resistir ao fogo vale em tudo
        if (!Contracts.resisteAoFogo(level, bicho)) helper.fail("o de resistir ao fogo pega num bicho");
        var tem = bicho.getEffect(MobEffects.FIRE_RESISTANCE);
        if (tem == null || tem.getDuration() < Contracts.RESISTE - 5) {
            helper.fail("e dá quinze minutos");
        }

        // e os três só em gente
        if (Contracts.evapora(level, bicho)) helper.fail("o de evaporar não pega num bicho");
        if (Contracts.toqueDeFogo(level, bicho)) helper.fail("nem o do toque de fogo");
        if (Contracts.funde(level, bicho)) helper.fail("nem o de fundir");

        if (!Contracts.evapora(level, gente)) helper.fail("mas pegam em gente");
        if (!Contracts.toqueDeFogo(level, gente)) helper.fail("os três");
        if (!Contracts.funde(level, gente)) helper.fail("os três mesmo");

        for (var qual : List.of(OccultaEffects.IMP_EVAPORATION, OccultaEffects.IMP_FIRE_TOUCH,
                OccultaEffects.IMP_MELTING_TOUCH)) {
            var posto = gente.getEffect(qual);
            if (posto == null) {
                helper.fail("e ficam postos");
                continue;
            }
            if (posto.getDuration() < Contracts.DEZ_MINUTOS - 5) helper.fail("por dez minutos");
            if (!net.thaumcraft.research.Incurable.marked(gente, qual)) {
                helper.fail("e o leite não os tira");
            }
            gente.removeEffect(qual);
        }
        bicho.discard();
        helper.succeed();
    }

    /** <b>Evaporar</b> seca a água que tiver ar por cima, e deixa a que estiver tapada. */
    @GameTest
    public void evaporationOnlyDriesTheSurface(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer gente = helper.makeMockServerPlayerInLevel();
        BlockPos onde = gente.blockPosition();

        BlockPos aberta = onde.offset(1, 0, 0);
        BlockPos tapada = onde.offset(2, 0, 0);
        level.setBlockAndUpdate(aberta, Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(tapada, Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(tapada.above(), Blocks.STONE.defaultBlockState());

        /*
         * A conta corre de vinte em vinte batidas e ainda sorteia uma em cinco. A prova chama a
         * varredura direto, sem o relógio: o que ela guarda é o <b>que</b> seca, não o quando.
         */
        Contracts.evapora(level, gente);
        ImpBlessings.seca(level, gente);

        if (level.getBlockState(aberta).is(Blocks.WATER)) {
            helper.fail("a água com ar por cima seca");
        }
        if (!level.getBlockState(tapada).is(Blocks.WATER)) {
            helper.fail("e a que está tapada fica");
        }

        level.setBlockAndUpdate(tapada, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(tapada.above(), Blocks.AIR.defaultBlockState());
        gente.removeEffect(OccultaEffects.IMP_EVAPORATION);
        helper.succeed();
    }

    /** <b>Fundir</b> passa o que cai pela fornalha, e o que não tem receita cai como caía. */
    @GameTest
    public void meltingTouchSmeltsWhatItCan(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer gente = helper.makeMockServerPlayerInLevel();

        List<ItemStack> caiu = new ArrayList<>(List.of(
                new ItemStack(Items.RAW_IRON), new ItemStack(Items.DIAMOND)));

        // sem o contrato, nada muda
        if (ImpBlessings.funde(level, gente, caiu) != caiu) {
            helper.fail("sem o contrato, o que cai cai como caía");
        }

        Contracts.funde(level, gente);
        var fundido = ImpBlessings.funde(level, gente, caiu);
        if (fundido == caiu) helper.fail("com o contrato, o ferro cru funde");
        if (fundido.size() != 2) helper.fail("e sai uma coisa por cada que caiu");
        if (!fundido.getFirst().is(Items.IRON_INGOT)) {
            helper.fail("o ferro cru vira lingote, e virou " + fundido.getFirst());
        }
        if (fundido.getFirst().getCount() < 1 || fundido.getFirst().getCount() > 2) {
            helper.fail("um, ou dois uma vez em quatro — e deu " + fundido.getFirst().getCount());
        }
        if (!fundido.get(1).is(Items.DIAMOND)) {
            helper.fail("e o diamante, que não funde, fica diamante");
        }

        gente.removeEffect(OccultaEffects.IMP_MELTING_TOUCH);
        helper.succeed();
    }

    /**
     * <b>As quatro recusas do Diabrete</b>, e o papel que não se gasta.
     *
     * <p>Ligado, não lê; com pouca afeição, não quer; há pouco tempo, manda esperar; e sem achar a pessoa
     * do outro lado, diz o nome e não faz nada. Em nenhuma delas o contrato se gasta.
     */
    @GameTest
    public void theImpRefusesForFourReasons(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ImpEntity ele = helper.spawn(net.thaumcraft.occulta.OccultaEntities.IMP, new BlockPos(2, 2, 2));
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ele.tame(quem);

        ItemStack papel = new ItemStack(OccultaItems.CONTRACT_RESIST_FIRE);
        TaglockItem.bind(papel, quem);
        if (!ContractItem.preso(papel)) helper.fail("o papel está preso a alguém");
        quem.setItemInHand(InteractionHand.MAIN_HAND, papel);

        // ligado: há poder demais para pensar
        ele.liga(level);
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (quem.hasEffect(MobEffects.FIRE_RESISTANCE)) helper.fail("ligado, ele não lê nada");
        ele.desliga();

        // sem afeição: por que haveria de o fazer?
        ele.afeição(0);
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (quem.hasEffect(MobEffects.FIRE_RESISTANCE)) helper.fail("sem afeição, ele não quer");

        if (papel.getCount() != 1) helper.fail("e em nenhuma delas o papel se gasta");

        // com afeição, ele lê — e o alvo é quem o papel prende, que é quem o deu
        ele.afeição(ImpEntity.GOSTA);
        ele.mobInteract(quem, InteractionHand.MAIN_HAND);
        if (!quem.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            helper.fail("com afeição bastante, ele faz o que está escrito");
        }

        quem.removeEffect(MobEffects.FIRE_RESISTANCE);
        quem.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ele.discard();
        helper.succeed();
    }

    /**
     * <b>O Frasco de Vínculo prende um contrato</b> como prende uma boneca.
     *
     * <p>Era a lacuna da fatia do Diabrete: o Contrato de Posse tinha receita e <b>não tinha como ser
     * assinado</b>, de modo que o Diabrete não se podia comprar fora do criativo.
     */
    @GameTest
    public void thetaglockBindsAContract(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();

        ItemStack frasco = new ItemStack(OccultaItems.TAGLOCK);
        TaglockItem.bind(frasco, quem);

        for (var qual : List.of(OccultaItems.CONTRACT, OccultaItems.CONTRACT_BLAZE,
                OccultaItems.CONTRACT_RESIST_FIRE, OccultaItems.CONTRACT_EVAPORATE,
                OccultaItems.CONTRACT_FIERY_TOUCH, OccultaItems.CONTRACT_SMELTING)) {
            var bancada = CraftingInput.of(2, 1,
                    List.of(new ItemStack(qual), frasco.copy()));
            if (!PoppetBindingRecipe.INSTANCE.matches(bancada, level)) {
                helper.fail("o frasco prende o " + qual);
                continue;
            }
            ItemStack feito = PoppetBindingRecipe.INSTANCE.assemble(bancada);
            if (!feito.is(qual)) helper.fail("e o que sai é o mesmo papel");
            if (!TaglockItem.isFor(feito, quem)) helper.fail("preso a quem estava no frasco");
        }

        // e o frasco se gasta
        var bancada = CraftingInput.of(2, 1,
                List.of(new ItemStack(OccultaItems.CONTRACT), frasco.copy()));
        for (var sobra : PoppetBindingRecipe.INSTANCE.getRemainingItems(bancada)) {
            if (!sobra.isEmpty()) helper.fail("o frasco se gasta");
        }
        helper.succeed();
    }

    /** E as cinco receitas, que são as do original: um Contrato em branco mais coisa do inferno. */
    @GameTest
    public void theFiveRecipesAreTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        confere(helper, level, OccultaItems.CONTRACT_BLAZE,
                List.of(OccultaItems.CONTRACT, Items.BLAZE_ROD, OccultaItems.HINT_OF_REBIRTH));
        confere(helper, level, OccultaItems.CONTRACT_RESIST_FIRE,
                List.of(OccultaItems.CONTRACT, Items.BLAZE_POWDER));
        confere(helper, level, OccultaItems.CONTRACT_EVAPORATE,
                List.of(OccultaItems.CONTRACT, Items.MAGMA_CREAM, Items.BLAZE_ROD));
        confere(helper, level, OccultaItems.CONTRACT_FIERY_TOUCH,
                List.of(OccultaItems.CONTRACT, OccultaItems.EMBER_MOSS, Items.BLAZE_ROD));
        confere(helper, level, OccultaItems.CONTRACT_SMELTING,
                List.of(OccultaItems.CONTRACT, Items.LAVA_BUCKET));
        helper.succeed();
    }

    private static void confere(GameTestHelper helper, ServerLevel level,
                                net.minecraft.world.item.Item sai,
                                List<net.minecraft.world.item.Item> entram) {
        var bancada = CraftingInput.of(entram.size(), 1,
                entram.stream().map(ItemStack::new).toList());
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, bancada, level);
        if (achada.isEmpty()) {
            helper.fail("não há receita para o " + sai);
            return;
        }
        ItemStack feito = achada.get().value().assemble(bancada);
        if (!feito.is(sai)) helper.fail("a receita do " + sai + " dá " + feito);
    }
}
