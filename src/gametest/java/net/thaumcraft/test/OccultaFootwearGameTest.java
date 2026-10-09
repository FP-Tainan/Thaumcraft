package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.clothes.RubySlippers;
import net.thaumcraft.occulta.clothes.WitchClothesItem;
import net.thaumcraft.occulta.clothes.WitchFootwear;

import java.util.List;

/**
 * O <b>calçado do ofício</b>: os três pares que mudam o chão por onde passam, as cores de fábrica de cada
 * um, a frase das Chinelas de Rubi e as três receitas.
 */
public class OccultaFootwearGameTest {
    /** <b>Chinelas de Gelo</b>: a água vira gelo, a lava vira obsidiana — e a lava gasta-as. */
    @GameTest
    public void icySlippersFreezeAndPave(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ItemStack chinelas = new ItemStack(OccultaItems.ICY_SLIPPERS);
        quem.setItemSlot(EquipmentSlot.FEET, chinelas);
        // o jogador de mentira da prova nasce em criativo, e em criativo nada se gasta
        quem.getAbilities().instabuild = false;

        BlockPos sob = quem.blockPosition().below();
        for (int dx = -1; dx <= 0; dx++) {
            for (int dz = -1; dz <= 0; dz++) {
                level.setBlockAndUpdate(sob.offset(dx, 0, dz), Blocks.WATER.defaultBlockState());
            }
        }
        WitchFootwear.gelo(level, quem);
        if (!level.getBlockState(sob).is(Blocks.ICE)) helper.fail("a água debaixo dos pés vira gelo");

        // e a lava vira obsidiana, insistindo até o desgaste cair
        for (int volta = 0; volta < 200 && !chinelas.isDamaged(); volta++) {
            for (int dx = -1; dx <= 0; dx++) {
                for (int dz = -1; dz <= 0; dz++) {
                    level.setBlockAndUpdate(sob.offset(dx, 0, dz), Blocks.LAVA.defaultBlockState());
                }
            }
            WitchFootwear.gelo(level, quem);
            if (!level.getBlockState(sob).is(Blocks.OBSIDIAN)) {
                helper.fail("a lava debaixo dos pés vira obsidiana");
                return;
            }
        }
        if (!chinelas.isDamaged()) helper.fail("e a lava gasta-as, uma vez em dez");

        // sem as chinelas, nada acontece
        quem.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        level.setBlockAndUpdate(sob, Blocks.WATER.defaultBlockState());
        WitchFootwear.gelo(level, quem);
        if (!level.getBlockState(sob).is(Blocks.WATER)) helper.fail("sem elas, a água fica água");

        level.setBlockAndUpdate(sob, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** <b>Sapatos Escorridos</b>: com os pés no chão, tiram o veneno e o definhar. */
    @GameTest
    public void seepingShoesTakeThePoison(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.SEEPING_SHOES));
        quem.setOnGround(true);

        quem.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        quem.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        WitchFootwear.escorre(level, quem);
        if (quem.hasEffect(MobEffects.POISON)) helper.fail("o veneno sai");
        if (quem.hasEffect(MobEffects.WITHER)) helper.fail("e o definhar também");

        // no ar, não: é preciso ter os pés no chão
        quem.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.SEEPING_SHOES));
        quem.setOnGround(false);
        quem.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        WitchFootwear.escorre(level, quem);
        if (!quem.hasEffect(MobEffects.POISON)) helper.fail("no ar, eles não escorrem nada");
        quem.removeEffect(MobEffects.POISON);

        // e sem eles, também não
        quem.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        quem.setOnGround(true);
        quem.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        WitchFootwear.escorre(level, quem);
        if (!quem.hasEffect(MobEffects.POISON)) helper.fail("sem eles, o veneno fica");
        quem.removeEffect(MobEffects.POISON);
        helper.succeed();
    }

    /**
     * <b>Chinelas de Rubi</b>: a frase só pega quem as calça, e uma frase qualquer não pega ninguém.
     *
     * <p>A prova não anda com a pessoa — teleportar num mundo de prova é confusão —; o que ela guarda é
     * <b>quem a frase pega</b>, que é a conta que decide se a fala vai ou não para o resto da mesa.
     */
    @GameTest
    public void onlyTheShodHearTheWords(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        String frase = net.minecraft.network.chat.Component
                .translatable(RubySlippers.CHAVE).getString();

        // descalço, a frase é uma frase
        if (RubySlippers.falou(quem, frase)) helper.fail("descalço, a frase não faz nada");

        quem.setItemSlot(EquipmentSlot.FEET, new ItemStack(OccultaItems.RUBY_SLIPPERS));
        if (!RubySlippers.falou(quem, frase)) helper.fail("calçado, ela pega");
        // e pega com maiúsculas, com apóstrofo e com o resto da fala atrás
        if (!RubySlippers.falou(quem, frase.toUpperCase(java.util.Locale.ROOT) + ", disse ela")) {
            helper.fail("e pega sem olhar a maiúscula nem o que vem depois");
        }
        if (RubySlippers.falou(quem, "bom dia")) helper.fail("mas uma frase qualquer não pega");

        quem.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        helper.succeed();
    }

    /** <b>As três cores de fábrica</b>, que são as do original — e as de Rubi não se tingem. */
    @GameTest
    public void eachPairHasItsOwnColour(GameTestHelper helper) {
        confere(helper, OccultaItems.ICY_SLIPPERS, 7842303, true);
        confere(helper, OccultaItems.SEEPING_SHOES, 2254387, true);
        confere(helper, OccultaItems.RUBY_SLIPPERS, 14483456, false);

        // e as roupas continuam com o castanho quase preto delas
        if (!(OccultaItems.WITCH_HAT instanceof WitchClothesItem chapéu)) {
            helper.fail("o chapéu é roupa de bruxa");
            return;
        }
        if (chapéu.corDeFábrica() != WitchClothesItem.DEFAULT_COLOR) {
            helper.fail("o chapéu fica com a cor de sempre");
        }
        helper.succeed();
    }

    /** E as três receitas, que são as do original. */
    @GameTest
    public void theThreeRecipesAreTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // as Chinelas de Rubi saem dos Sapatos Escorridos, que é a conta que as põe no fim da fila
        confere(helper, level, OccultaItems.RUBY_SLIPPERS, 3, 3, List.of(
                OccultaItems.ATTUNED_STONE, OccultaItems.INFERNAL_BLOOD, OccultaItems.ATTUNED_STONE,
                OccultaItems.GOLDEN_THREAD, OccultaItems.SEEPING_SHOES, OccultaItems.GOLDEN_THREAD,
                OccultaItems.ATTUNED_STONE, OccultaItems.INFERNAL_BLOOD, OccultaItems.ATTUNED_STONE));
        confere(helper, level, OccultaItems.ICY_SLIPPERS, 3, 3, List.of(
                OccultaItems.IMPREGNATED_LEATHER, OccultaItems.GOLDEN_THREAD, OccultaItems.IMPREGNATED_LEATHER,
                OccultaItems.IMPREGNATED_LEATHER, OccultaItems.FROZEN_HEART, OccultaItems.IMPREGNATED_LEATHER,
                OccultaItems.DIAMOND_VAPOUR, OccultaItems.ODOUR_OF_PURITY, OccultaItems.DIAMOND_VAPOUR));
        helper.succeed();
    }

    // ------------------------------------------------------------------ a mão

    private static void confere(GameTestHelper helper, net.minecraft.world.item.Item qual, int cor,
                                boolean tingível) {
        if (!(qual instanceof WitchClothesItem roupa)) {
            helper.fail(qual + " é roupa de bruxa");
            return;
        }
        if (roupa.corDeFábrica() != cor) {
            helper.fail("a cor de fábrica do " + qual + " é " + cor + ", e é " + roupa.corDeFábrica());
        }
        if (roupa.dyeable() != tingível) helper.fail("e o tingir do " + qual + " está trocado");
    }

    private static void confere(GameTestHelper helper, ServerLevel level,
                                net.minecraft.world.item.Item sai, int largura, int altura,
                                List<net.minecraft.world.item.Item> entram) {
        var bancada = CraftingInput.of(largura, altura, entram.stream().map(ItemStack::new).toList());
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
