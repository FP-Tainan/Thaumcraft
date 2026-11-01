package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaItems;

import java.util.List;

/**
 * Os <b>materiais do Ars Arcana</b>: os cinco minérios, os cinco blocos de guardar e as nove pedras e pós.
 *
 * <p>É o {@code BlockAMOre} e o {@code ItemOre} da 1.4.0.009, que eram um bloco e um item com metadados.
 */
public class ArcanaMaterialsGameTest {
    /** Os cinco materiais que têm minério, bloco e pedra. */
    private static final String[] CINCO = {"vinteum", "chimerite", "blue_topaz", "moonstone", "sunstone"};

    /** <b>Tudo está registrado</b>: nove materiais, cinco minérios e cinco blocos de guardar. */
    @GameTest(maxTicks = 20)
    public void everyMaterialIsRegistered(GameTestHelper helper) {
        if (ArcanaItems.MATERIALS.size() != 9) {
            helper.fail("o ItemOre tem nove metadados: contei " + ArcanaItems.MATERIALS.size());
            return;
        }
        if (ArcanaBlocks.ORES.size() != 5 || ArcanaBlocks.MATERIAL_BLOCKS.size() != 5) {
            helper.fail("são cinco minérios e cinco blocos de guardar");
            return;
        }
        for (String qual : CINCO) {
            if (ArcanaBlocks.ORES.get(qual) == null) {
                helper.fail("falta o minério de " + qual);
                return;
            }
            if (ArcanaBlocks.MATERIAL_BLOCKS.get(qual) == null) {
                helper.fail("falta o bloco de " + qual);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Os minérios pedem picareta de ferro.</b>
     *
     * <p>O {@code getHarvestLevel} do original devolve 2 para todos os metadados, sem olhar para qual é.
     */
    @GameTest(maxTicks = 20)
    public void theOresWantAnIronPickaxe(GameTestHelper helper) {
        for (String qual : CINCO) {
            for (var bloco : List.of(ArcanaBlocks.ORES.get(qual), ArcanaBlocks.MATERIAL_BLOCKS.get(qual))) {
                BlockState feitio = bloco.defaultBlockState();
                if (!feitio.requiresCorrectToolForDrops()) {
                    helper.fail(BuiltInRegistries.BLOCK.getKey(bloco) + " cai a mão");
                    return;
                }
                if (!feitio.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                    helper.fail(BuiltInRegistries.BLOCK.getKey(bloco) + " não é de picareta");
                    return;
                }
                if (!feitio.is(BlockTags.NEEDS_IRON_TOOL)) {
                    helper.fail(BuiltInRegistries.BLOCK.getKey(bloco) + " não pede ferro");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * <b>E cai o que o {@code getDrops} do original manda cair.</b>
     *
     * <p>O vinteum cai a si mesmo — é o único que se funde depois. Os outros quatro caem em pedras, e cada um
     * tem a sua conta: quimerita e topázio <b>1 a 4</b>, pedra-da-lua <b>1 a 2</b>, pedra-do-sol <b>2 a 4</b>.
     */
    @GameTest(maxTicks = 20)
    public void eachOreDropsItsOwnCount(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));

        // o vinteum cai como bloco, e é por isso que há uma receita de fundição para ele
        List<ItemStack> vinteum = Block.getDrops(ArcanaBlocks.ORES.get("vinteum").defaultBlockState(),
                level, onde, null, null, new ItemStack(Items.IRON_PICKAXE));
        if (vinteum.size() != 1 || !vinteum.getFirst().is(ArcanaBlocks.ORES.get("vinteum").asItem())) {
            helper.fail("o minério de vinteum devia cair a si mesmo");
            return;
        }

        if (!conta(helper, level, onde, "chimerite", "chimerite", 1, 4)) return;
        if (!conta(helper, level, onde, "blue_topaz", "blue_topaz", 1, 4)) return;
        if (!conta(helper, level, onde, "moonstone", "moonstone", 1, 2)) return;
        if (!conta(helper, level, onde, "sunstone", "sunstone", 2, 4)) return;
        helper.succeed();
    }

    /** Duzentas quebras: tem de sair sempre o mesmo item, sempre entre o mínimo e o máximo, e ver os dois. */
    private static boolean conta(GameTestHelper helper, ServerLevel level, BlockPos onde,
                                 String minério, String pedra, int mínimo, int máximo) {
        BlockState feitio = ArcanaBlocks.ORES.get(minério).defaultBlockState();
        boolean viuMínimo = false, viuMáximo = false;
        for (int volta = 0; volta < 200; volta++) {
            List<ItemStack> caiu = Block.getDrops(feitio, level, onde, null, null, new ItemStack(Items.IRON_PICKAXE));
            if (caiu.size() != 1 || !caiu.getFirst().is(ArcanaItems.MATERIALS.get(pedra))) {
                helper.fail("o minério de " + minério + " devia cair " + pedra + ", caiu " + caiu);
                return false;
            }
            int quantos = caiu.getFirst().getCount();
            if (quantos < mínimo || quantos > máximo) {
                helper.fail("o minério de " + minério + " caiu " + quantos + ", e devia ser de " + mínimo + " a " + máximo);
                return false;
            }
            if (quantos == mínimo) viuMínimo = true;
            if (quantos == máximo) viuMáximo = true;
        }
        if (!viuMínimo || !viuMáximo) {
            helper.fail("em duzentas quebras o minério de " + minério + " nunca deu " + (viuMínimo ? máximo : mínimo));
            return false;
        }
        return true;
    }

    /**
     * <b>O Toque Suave leva o minério inteiro</b>, e a Fortuna soma um por grau.
     *
     * <p>A Fortuna do original é uma soma e não uma multiplicação: {@code nextInt(4) + 1 + fortune}. É por isso
     * que ela vale pouco nestes minérios comparada com a dos minérios do jogo.
     */
    @GameTest(maxTicks = 20)
    public void silkTakesTheOreAndFortuneAddsOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        Block minério = ArcanaBlocks.ORES.get("chimerite");

        ItemStack seda = new ItemStack(Items.IRON_PICKAXE);
        encanta(level, seda, Enchantments.SILK_TOUCH, 1);
        List<ItemStack> comSeda = Block.getDrops(minério.defaultBlockState(), level, onde, null, null, seda);
        if (comSeda.size() != 1 || !comSeda.getFirst().is(minério.asItem())) {
            helper.fail("com Toque Suave devia vir o minério inteiro, veio " + comSeda);
            return;
        }

        ItemStack sorte = new ItemStack(Items.IRON_PICKAXE);
        encanta(level, sorte, Enchantments.FORTUNE, 3);
        int maior = 0;
        for (int volta = 0; volta < 200; volta++) {
            maior = Math.max(maior, Block.getDrops(minério.defaultBlockState(), level, onde, null, null, sorte)
                    .getFirst().getCount());
        }
        // quatro de base mais três de Fortuna
        if (maior != 7) {
            helper.fail("com Fortuna 3 o máximo devia ser sete, foi " + maior);
            return;
        }
        helper.succeed();
    }

    private static void encanta(ServerLevel level, ItemStack ferramenta,
                                net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> qual,
                                int grau) {
        var registo = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        ferramenta.enchant(registo.getOrThrow(qual), grau);
    }

    /**
     * <b>Os blocos de guardar caem a si mesmos.</b>
     *
     * <p>Nove pedras fazem um e um desfaz-se em nove: nada se perde a guardar.
     */
    @GameTest(maxTicks = 20)
    public void theStorageBlocksDropThemselves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(1, 2, 1));
        for (String qual : CINCO) {
            Block bloco = ArcanaBlocks.MATERIAL_BLOCKS.get(qual);
            List<ItemStack> caiu = Block.getDrops(bloco.defaultBlockState(), level, onde, null, null,
                    new ItemStack(Items.IRON_PICKAXE));
            if (caiu.size() != 1 || !caiu.getFirst().is(bloco.asItem())) {
                helper.fail("o bloco de " + qual + " devia cair a si mesmo, caiu " + caiu);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>Os nove materiais empilham até 64 e têm nome.</b>
     *
     * <p>Guarda contra o descuido mais fácil de todos: registrar um item e esquecer o nome dele, que então sai
     * como a chave crua.
     */
    @GameTest(maxTicks = 20)
    public void theNineMaterialsAreOrdinaryItems(GameTestHelper helper) {
        for (var entrada : ArcanaItems.MATERIALS.entrySet()) {
            ItemStack pilha = new ItemStack(entrada.getValue());
            if (pilha.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) != 64) {
                helper.fail(entrada.getKey() + " não empilha até 64");
                return;
            }
            if (!BuiltInRegistries.ITEM.getKey(entrada.getValue()).getPath().equals(entrada.getKey())) {
                helper.fail(entrada.getKey() + " registrou-se com outro nome");
                return;
            }
        }
        helper.succeed();
    }
}
