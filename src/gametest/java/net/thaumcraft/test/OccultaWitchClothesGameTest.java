package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.ArmorType;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaMaterials;
import net.thaumcraft.occulta.clothes.WitchClothes;
import net.thaumcraft.occulta.clothes.WitchClothesItem;

import java.util.Arrays;
import java.util.List;

/**
 * As <b>roupas de bruxa</b>: o Chapéu, o Manto, o Manto de Necromante e o Chapéu da Baba Yaga.
 *
 * <p>A prova que carrega a fatia é a do <b>frasco a mais</b>. A proteção delas é a do couro e não importa
 * a ninguém; o que importa é que o chapéu e o manto somam <b>setenta por cento</b> de chance de um segundo
 * frasco na Chaleira, e <b>dois frascos certos</b> no Caldeirão. Duas peças quase dobram a produção de uma
 * bruxa.
 */
public class OccultaWitchClothesGameTest {
    /** Os números delas são os do original. */
    @GameTest
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (WitchClothes.HAT != 0.35 || WitchClothes.ROBE != 0.35) {
            helper.fail("o chapéu e o manto somam trinta e cinco por cento cada");
        }
        if (WitchClothes.BABA != 0.25) helper.fail("e o da Baba vinte e cinco, duas vezes");
        if (WitchClothes.FAMILIAR != 0.05) helper.fail("e o familiar cinco");
        if (WitchClothes.LEVEL_HAT != 1 || WitchClothes.LEVEL_ROBE != 1) {
            helper.fail("no caldeirão, o chapéu e o manto valem um cada");
        }
        if (WitchClothes.LEVEL_BABA != 2) helper.fail("e o da Baba vale dois sozinho");
        if (WitchClothesItem.DEFAULT_COLOR != 2628115) helper.fail("e a cor de fábrica é a do original");
        helper.succeed();
    }

    /** E são couro: protegem pouco e gastam-se depressa. */
    @GameTest
    public void theyAreLeather(GameTestHelper helper) {
        if (OccultaMaterials.WITCH.durability() != OccultaMaterials.LEATHER_WEAR) {
            helper.fail("elas duram como couro");
        }
        if (OccultaMaterials.WITCH.defense().get(ArmorType.HELMET) != 1
                || OccultaMaterials.WITCH.defense().get(ArmorType.CHESTPLATE) != 3) {
            helper.fail("e protegem como couro: um no chapéu, três no manto");
        }
        helper.succeed();
    }

    /** O Chapéu da Baba é o único que não se tinge, e o único que é épico. */
    @GameTest
    public void onlyBabasHatRefusesDye(GameTestHelper helper) {
        if (!(OccultaItems.WITCH_HAT instanceof WitchClothesItem chapéu)
                || !(OccultaItems.BABAS_HAT instanceof WitchClothesItem baba)
                || !(OccultaItems.NECROMANCERS_ROBES instanceof WitchClothesItem necro)) {
            helper.fail("as três são roupa de bruxa");
            return;
        }
        if (!chapéu.dyeable()) helper.fail("o chapéu de bruxa se tinge");
        if (baba.dyeable()) helper.fail("e o da Baba não");
        if (!necro.necro()) helper.fail("e só o de Necromante leva ombreiras");
        helper.succeed();
    }

    /**
     * <b>Setenta por cento com as duas peças.</b>
     *
     * <p>Esta é a prova que carrega a fatia. O chapéu e o manto <b>não se excluem</b>: eles somam. E os
     * dois mantos excluem-se entre si <b>por tipo de cozimento</b>, que é a parte que ninguém adivinha.
     */
    @GameTest
    public void hatAndRobeStack(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (WitchClothes.secondBottle(quem, false) != 0.0) helper.fail("sem roupa, nada");

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.WITCH_HAT));
        if (Math.abs(WitchClothes.secondBottle(quem, false) - 0.35) > 0.0001) {
            helper.fail("só com o chapéu, trinta e cinco");
        }

        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.WITCH_ROBES));
        if (Math.abs(WitchClothes.secondBottle(quem, false) - 0.70) > 0.0001) {
            helper.fail("com os dois, setenta: " + WitchClothes.secondBottle(quem, false));
        }

        // e o manto de bruxa não vale num cozimento de erguer
        if (Math.abs(WitchClothes.secondBottle(quem, true) - 0.35) > 0.0001) {
            helper.fail("num cozimento de erguer, o manto de bruxa não conta");
        }
        // mas o de necromante vale, e só nesse
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.NECROMANCERS_ROBES));
        if (Math.abs(WitchClothes.secondBottle(quem, true) - 0.70) > 0.0001) {
            helper.fail("e o de necromante conta num de erguer");
        }
        if (Math.abs(WitchClothes.secondBottle(quem, false) - 0.35) > 0.0001) {
            helper.fail("e não conta nos outros");
        }

        quem.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        quem.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        helper.succeed();
    }

    /** <b>E só o Chapéu da Baba dá chance de um terceiro.</b> */
    @GameTest
    public void onlyBabasHatGivesAThird(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.WITCH_HAT));
        if (WitchClothes.thirdBottle(quem) != 0.0) helper.fail("o chapéu de bruxa não dá um terceiro");

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.BABAS_HAT));
        if (Math.abs(WitchClothes.thirdBottle(quem) - 0.25) > 0.0001) {
            helper.fail("e o da Baba dá vinte e cinco");
        }
        if (Math.abs(WitchClothes.secondBottle(quem, false) - 0.25) > 0.0001) {
            helper.fail("e vinte e cinco no segundo, e não trinta e cinco");
        }

        quem.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        helper.succeed();
    }

    /** <b>E no Caldeirão não é chance: é contagem.</b> */
    @GameTest
    public void theCauldronCountsInstead(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (WitchClothes.gearLevel(quem) != 0) helper.fail("sem roupa, zero");

        quem.setItemSlot(EquipmentSlot.HEAD, new ItemStack(OccultaItems.BABAS_HAT));
        if (WitchClothes.gearLevel(quem) != 2) helper.fail("o chapéu da Baba vale dois sozinho");

        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.WITCH_ROBES));
        if (WitchClothes.gearLevel(quem) != 3) helper.fail("e com um manto se chega ao três");

        quem.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        quem.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        helper.succeed();
    }

    /** As quatro receitas, e a do Couro Impregnado que as paga. */
    @GameTest
    public void theFiveAreCraftable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack couro = new ItemStack(OccultaItems.IMPREGNATED_LEATHER);
        ItemStack fio = new ItemStack(OccultaItems.GOLDEN_THREAD);
        ItemStack nada = ItemStack.EMPTY;

        confere(helper, level, "o Couro Impregnado", OccultaItems.IMPREGNATED_LEATHER, Arrays.asList(
                new ItemStack(OccultaItems.WHIFF_OF_MAGIC), new ItemStack(Items.LEATHER),
                new ItemStack(OccultaItems.WHIFF_OF_MAGIC),
                new ItemStack(Items.LEATHER), new ItemStack(OccultaItems.DIAMOND_VAPOUR),
                new ItemStack(Items.LEATHER),
                new ItemStack(OccultaItems.WHIFF_OF_MAGIC), new ItemStack(Items.LEATHER),
                new ItemStack(OccultaItems.WHIFF_OF_MAGIC)));

        confere(helper, level, "o Chapéu de Bruxa", OccultaItems.WITCH_HAT, Arrays.asList(
                nada, couro, nada,
                fio, couro, fio,
                couro, new ItemStack(Items.GLOWSTONE_DUST), couro));

        confere(helper, level, "o Manto de Bruxa", OccultaItems.WITCH_ROBES, Arrays.asList(
                couro, fio, couro,
                couro, new ItemStack(OccultaItems.CREEPER_HEART), couro,
                couro, couro, couro));

        confere(helper, level, "o Manto de Necromante", OccultaItems.NECROMANCERS_ROBES, Arrays.asList(
                couro, fio, couro,
                couro, new ItemStack(OccultaItems.NECROTIC_STONE), couro,
                couro, couro, couro));

        helper.succeed();
    }

    /** E o Couro Impregnado conserta as quatro. */
    @GameTest
    public void theLeatherMendsThem(GameTestHelper helper) {
        var rótulo = OccultaMaterials.WITCH.repairIngredient();
        if (!new ItemStack(OccultaItems.IMPREGNATED_LEATHER).is(rótulo)) {
            helper.fail("o Couro Impregnado é o que as conserta");
        }
        helper.succeed();
    }

    private static void confere(GameTestHelper helper, ServerLevel level, String nome,
                                net.minecraft.world.item.Item sai, List<ItemStack> posto) {
        var mesa = CraftingInput.of(3, 3, posto);
        var achou = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, mesa, level);
        if (achou.isEmpty()) {
            helper.fail(nome + " devia fechar receita");
            return;
        }
        ItemStack saiu = achou.get().value().assemble(mesa);
        if (!saiu.is(sai)) helper.fail("de " + nome + " devia sair ele próprio; saiu " + saiu);
    }
}
