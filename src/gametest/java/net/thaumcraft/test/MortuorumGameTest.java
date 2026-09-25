package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.mortuorum.MortuorumItems;

/**
 * O ramo do Ars Mortuorum: as coisas que se tiram dos mortos.
 */
public class MortuorumGameTest {
    /** A foice tira a alma de quem mata, e gasta uma garrafa vazia para guardá-la. */
    @GameTest
    public void theScytheBottlesTheSoul(GameTestHelper helper) {
        var dono = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        dono.getInventory().add(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
        ItemStack foice = new ItemStack(MortuorumItems.SCYTHE_ITEM);

        var bicho = helper.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        bicho.setHealth(0.0f);
        MortuorumItems.SCYTHE_ITEM.hurtEnemy(foice, bicho, dono);

        boolean temAlma = false;
        boolean temGarrafa = false;
        for (int casa = 0; casa < dono.getInventory().getContainerSize(); casa++) {
            ItemStack pilha = dono.getInventory().getItem(casa);
            if (pilha.is(MortuorumItems.SOUL_IN_A_JAR)) temAlma = true;
            if (pilha.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) temGarrafa = true;
        }
        if (!temAlma) helper.fail("a foice devia deixar uma alma num pote");
        if (temGarrafa) helper.fail("e gastar a garrafa vazia");
        bicho.discard();
        helper.succeed();
    }

    /** Sem garrafa nenhuma, não há onde guardar a alma. */
    @GameTest
    public void theScytheNeedsAnEmptyBottle(GameTestHelper helper) {
        var dono = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack foice = new ItemStack(MortuorumItems.SCYTHE_ITEM);

        var bicho = helper.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        bicho.setHealth(0.0f);
        MortuorumItems.SCYTHE_ITEM.hurtEnemy(foice, bicho, dono);

        for (int casa = 0; casa < dono.getInventory().getContainerSize(); casa++) {
            if (dono.getInventory().getItem(casa).is(MortuorumItems.SOUL_IN_A_JAR)) {
                helper.fail("sem garrafa, não devia sair alma nenhuma");
            }
        }
        bicho.discard();
        helper.succeed();
    }

    /** A de osso corta o dobro da outra. */
    @GameTest
    public void theBoneScytheCutsDeeper(GameTestHelper helper) {
        if (MortuorumItems.SCYTHE.attackDamageBonus() != 2.0f) helper.fail("a foice soma dois de dano");
        if (MortuorumItems.SCYTHE_BONE.attackDamageBonus() != 4.0f) helper.fail("e a de osso, quatro");
        if (MortuorumItems.SCYTHE.durability() != 666) helper.fail("as duas aguentam seiscentos e sessenta e seis golpes");
        helper.succeed();
    }

    /** O balde de sangue põe sangue no chão, e o sangue corre. */
    @GameTest
    public void theBloodFlows(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, net.thaumcraft.mortuorum.MortuorumBlocks.BLOOD);
        var fluido = helper.getLevel().getFluidState(helper.absolutePos(onde));
        if (!fluido.is(net.thaumcraft.mortuorum.MortuorumFluids.BLOOD)) helper.fail("o bloco devia ser sangue");
        if (!fluido.isSource()) helper.fail("e uma fonte");
        if (net.thaumcraft.mortuorum.MortuorumFluids.BLOOD.getBucket() != MortuorumItems.BUCKET_BLOOD) {
            helper.fail("o balde do sangue é o balde de sangue");
        }
        helper.succeed();
    }

    /** Um balde de sangue dá oito frascos, e oito frascos voltam a dar um balde. */
    @GameTest
    public void theBloodGoesBackAndForth(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager();

        var paraFrasco = new java.util.ArrayList<ItemStack>();
        paraFrasco.add(new ItemStack(MortuorumItems.BUCKET_BLOOD));
        for (int i = 0; i < 8; i++) paraFrasco.add(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
        var entradaFrasco = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, paraFrasco);
        var achadoFrasco = recipes.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,
                entradaFrasco, helper.getLevel());
        if (achadoFrasco.isEmpty()) helper.fail("um balde e oito garrafas deviam fechar receita");
        else {
            ItemStack saida = achadoFrasco.get().value().assemble(entradaFrasco);
            if (!saida.is(MortuorumItems.JAR_OF_BLOOD) || saida.getCount() != 8) {
                helper.fail("deviam sair oito frascos de sangue; saiu " + saida);
            }
        }

        var paraBalde = new java.util.ArrayList<ItemStack>();
        paraBalde.add(new ItemStack(net.minecraft.world.item.Items.BUCKET));
        for (int i = 0; i < 8; i++) paraBalde.add(new ItemStack(MortuorumItems.JAR_OF_BLOOD));
        var entradaBalde = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, paraBalde);
        var achadoBalde = recipes.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,
                entradaBalde, helper.getLevel());
        if (achadoBalde.isEmpty()) helper.fail("um balde e oito frascos deviam fechar receita");
        else if (!achadoBalde.get().value().assemble(entradaBalde).is(MortuorumItems.BUCKET_BLOOD)) {
            helper.fail("e devia sair o balde de sangue");
        }
        helper.succeed();
    }

    /** As cinquenta e quatro peças de corpo do original existem, e cada uma tem item. */
    @GameTest
    public void theBodyPartsAreAllThere(GameTestHelper helper) {
        if (MortuorumItems.PARTS.size() != 54) {
            helper.fail("o original tem cinquenta e quatro peças; achei " + MortuorumItems.PARTS.size());
        }
        for (var nome : MortuorumItems.PARTS.keySet()) {
            if (!MortuorumItems.PART_ITEMS.containsKey(nome)) helper.fail("falta o item de " + nome);
        }
        var cabeca = MortuorumItems.PARTS.get("wolf_head");
        if (cabeca == null || !cabeca.mob().equals("Wolf") || !cabeca.piece().equals("Head")) {
            helper.fail("do lobo só se tira a cabeça");
        }
        helper.succeed();
    }

    /** Os cinco órgãos são comida, e comê-los dá fome — como no original. */
    @GameTest
    public void theOrgansAreFood(GameTestHelper helper) {
        if (MortuorumItems.ORGANS.size() != 5) helper.fail("são cinco órgãos");
        for (var orgao : MortuorumItems.ORGANS.values()) {
            var comida = new ItemStack(orgao).get(DataComponents.FOOD);
            if (comida == null) helper.fail("órgão é comida");
            else if (comida.nutrition() != 2) helper.fail("cada um enche dois de fome");
        }
        helper.succeed();
    }

    /** A Máquina de Costura sabe fazer as cinquenta e quatro peças, e mais a pele do couro. */
    @GameTest
    public void theSewingMachineKnowsThePatterns(GameTestHelper helper) {
        if (net.thaumcraft.mortuorum.SewingRecipes.ALL.size() != 55) {
            helper.fail("são cinquenta e quatro peças mais a pele; achei "
                    + net.thaumcraft.mortuorum.SewingRecipes.ALL.size());
        }

        // o desenho da cabeça de vaca, com a carne dela no meio
        var grade = new net.minecraft.world.SimpleContainer(16);
        String[] desenho = {"SSSS", "SBFS", "SEES"};
        for (int linha = 0; linha < desenho.length; linha++) {
            for (int coluna = 0; coluna < 4; coluna++) {
                char c = desenho[linha].charAt(coluna);
                ItemStack peca = switch (c) {
                    case 'S' -> new ItemStack(MortuorumItems.ORGANS.get("skin"));
                    case 'B' -> new ItemStack(MortuorumItems.ORGANS.get("brains"));
                    default -> new ItemStack(net.minecraft.world.item.Items.BEEF);
                };
                grade.setItem(coluna + linha * 4, peca);
            }
        }
        var receita = net.thaumcraft.mortuorum.SewingRecipe.find(grade);
        if (receita == null) helper.fail("o desenho da cabeça de vaca devia fechar");
        else if (!receita.result().is(MortuorumItems.PART_ITEMS.get("cow_head"))) {
            helper.fail("e dar a cabeça de vaca; deu " + receita.result());
        }

        // e o couro, sem forma, dá oito peles
        var couro = new net.minecraft.world.SimpleContainer(16);
        couro.setItem(5, new ItemStack(net.minecraft.world.item.Items.LEATHER));
        var pele = net.thaumcraft.mortuorum.SewingRecipe.find(couro);
        if (pele == null || pele.result().getCount() != 8) helper.fail("um couro dá oito peles");
        helper.succeed();
    }

    /** Sem agulha e sem linha, a costura não sai. */
    @GameTest
    public void theSewingMachineNeedsNeedleAndThread(GameTestHelper helper) {
        BlockPos onde = new BlockPos(2, 2, 2);
        helper.setBlock(onde, net.thaumcraft.mortuorum.MortuorumBlocks.SEWING_MACHINE);
        var maquina = helper.getBlockEntity(onde, net.thaumcraft.mortuorum.SewingMachineBlockEntity.class);
        if (maquina == null) helper.fail("a máquina devia ter entidade de bloco");
        if (maquina.ready()) helper.fail("vazia, ela não costura");

        maquina.setItem(net.thaumcraft.mortuorum.SewingMachineBlockEntity.NEEDLE,
                new ItemStack(MortuorumItems.BONE_NEEDLE));
        if (maquina.ready()) helper.fail("só com a agulha, ainda não");
        maquina.setItem(net.thaumcraft.mortuorum.SewingMachineBlockEntity.THREAD,
                new ItemStack(net.minecraft.world.item.Items.STRING));
        if (!maquina.ready()) helper.fail("com agulha e linha, sim");

        maquina.spend();
        if (!maquina.getItem(net.thaumcraft.mortuorum.SewingMachineBlockEntity.NEEDLE).isEmpty()) {
            helper.fail("a costura gasta a agulha");
        }
        helper.succeed();
    }
}
