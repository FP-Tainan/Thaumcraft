package net.thaumcraft.research;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * O que quer dizer cada nome de receita que as páginas do Thaumonomicon citam.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/livro-receitas.js} a partir do {@code ConfigRecipes} do jar
 * original. As da bancada comum e as montagens de estrutura vêm inteiras, como o original as mostra; as outras dizem a
 * pesquisa e o que sai, para o livro achar a receita de verdade nas tabelas do mod.
 */
public final class BookRecipes {
    /** Uma receita da bancada comum: o que sai, a forma (zero para sem forma) e o que vai em cada casa. */
    public record Crafting(Supplier<ItemStack> result, int width, int height, List<List<ItemStack>> grid) {
    }

    /** Uma montagem: o vis que custa, o tamanho, e o que vai em cada casa, camada a camada. */
    public record Compound(net.thaumcraft.api.aspects.AspectList vis, int sizeX, int sizeY, int sizeZ, List<Supplier<ItemStack>> blocks) {
    }

    /** O que acha uma receita nas tabelas: a pesquisa e o que sai ({@code any}: vale qualquer uma dessa pesquisa com essa saída). */
    public record Ref(String research, Supplier<ItemStack> result, boolean any) {
    }

    /** Uma infusão de encantamento: a pesquisa e o encantamento que ela sobe. */
    public record Enchant(String research, String enchantment) {
    }

    public static final Map<String, Object> ALL = new HashMap<>();

    private BookRecipes() {
    }

    private static void crafting(String name, Supplier<ItemStack> result, int w, int h, List<List<ItemStack>> grid) {
        ALL.put(name, new Crafting(result, w, h, grid));
    }

    private static void compound(String name, net.thaumcraft.api.aspects.AspectList vis, int x, int y, int z, List<Supplier<ItemStack>> blocks) {
        ALL.put(name, new Compound(vis, x, y, z, blocks));
    }

    private static void ref(String name, String research, Supplier<ItemStack> result) {
        ALL.put(name, new Ref(research, result, false));
    }

    private static void refAny(String name, String research, Supplier<ItemStack> result) {
        ALL.put(name, new Ref(research, result, true));
    }

    /** Um cetro de exemplo: " TF / RT / T  ", ponta, haste e amuleto primordial, custo um e meio do da varinha. */
    private static void sceptre(String name, String cap, String rod) {
        var capPart = net.thaumcraft.api.wands.WandParts.cap(cap);
        var rodPart = net.thaumcraft.api.wands.WandParts.RODS.get(rod);
        int cost = (int) (capPart.craftCost() * rodPart.craftCost() * 1.5f);
        var vis = new net.thaumcraft.api.aspects.AspectList();
        for (var primal : net.thaumcraft.api.aspects.Aspects.primals()) vis.add(primal, cost);
        var t = net.minecraft.world.item.crafting.Ingredient.of(TCItems.WAND_CAPS.get(cap));
        var r = net.minecraft.world.item.crafting.Ingredient.of(rod.equals("wood") ? net.minecraft.world.item.Items.STICK : TCItems.WAND_RODS.get(rod));
        var f = net.minecraft.world.item.crafting.Ingredient.of(TCResources.get("primal_charm"));
        ALL.put(name, new net.thaumcraft.crafting.ArcaneRecipe("SCEPTRE", net.thaumcraft.item.WandItem.bookStack(cap, rod, true),
                java.util.Arrays.asList(null, t, f, null, r, t, t, null, null), vis));
    }

    private static void enchantment(String name, String research, String enchantment) {
        ALL.put(name, new Enchant(research, enchantment));
    }

    /** Os rótulos marcados, um por aspecto ({@code JarLabel0..47}), e o que apaga a marca ({@code JarLabelNull}). */
    private static void labels() {
        int count = 0;
        for (net.thaumcraft.api.aspects.Aspect aspect : net.thaumcraft.api.aspects.Aspect.ASPECTS.values()) {
            ItemStack phial = new ItemStack(TCItems.PHIAL);
            phial.set(net.thaumcraft.registry.TCComponents.PHIAL_ASPECT, aspect.tag());
            ItemStack marked = new ItemStack(TCResources.get("jar_label"));
            marked.set(net.thaumcraft.registry.TCComponents.LABEL_ASPECT, aspect.tag());
            crafting("JarLabel_" + count++, () -> marked, 0, 0, List.of(List.of(new ItemStack(TCResources.get("jar_label"))), List.of(phial)));
        }
        ItemStack water = new ItemStack(TCResources.get("jar_label"));
        water.set(net.thaumcraft.registry.TCComponents.LABEL_ASPECT, net.thaumcraft.api.aspects.Aspects.WATER.tag());
        crafting("JarLabelNull", () -> new ItemStack(TCResources.get("jar_label")), 0, 0, List.of(List.of(water)));
    }

    private static List<ItemStack> stacks(net.minecraft.world.item.Item[] items) {
        return java.util.Arrays.stream(items).map(ItemStack::new).toList();
    }

    /** As pilhas de uma etiqueta de itens (o dicionário de minério de então). */
    private static List<ItemStack> tag(String id) {
        var key = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.parse(id));
        List<ItemStack> out = new java.util.ArrayList<>();
        for (var holder : net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(key)) out.add(new ItemStack(holder));
        return out;
    }

    /** Montado na primeira vez que o livro pede (as etiquetas só existem com o mundo aberto). */
    private static boolean built;

    public static synchronized Object get(String name) {
        if (!built) {
            built = true;
            part1();
            part2();
            part3();
        }
        return ALL.get(name);
    }

    private static void part1() {
        compound("Thaumonomicon", new net.thaumcraft.api.aspects.AspectList(), 1, 2, 1, List.of(() -> net.thaumcraft.item.WandItem.bookStack("iron", "wood", false), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.BOOKSHELF.asItem())));
        compound("ArcTable", new net.thaumcraft.api.aspects.AspectList(), 1, 2, 1, List.of(() -> net.thaumcraft.item.WandItem.bookStack("iron", "wood", false), () -> new net.minecraft.world.item.ItemStack(TCBlocks.TABLE.asItem())));
        compound("ResTable", new net.thaumcraft.api.aspects.AspectList(), 1, 2, 2, List.of(() -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCItems.SCRIBING_TOOLS), () -> new net.minecraft.world.item.ItemStack(TCBlocks.TABLE.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.TABLE.asItem())));
        compound("Crucible", new net.thaumcraft.api.aspects.AspectList(), 1, 2, 1, List.of(() -> net.thaumcraft.item.WandItem.bookStack("iron", "wood", false), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CAULDRON)));
        compound("InfernalFurnace", new net.thaumcraft.api.aspects.AspectList().add(net.thaumcraft.api.aspects.Aspects.FIRE, 50).add(net.thaumcraft.api.aspects.Aspects.EARTH, 50), 3, 3, 3, List.of(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.LAVA.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.IRON_BARS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem())));
        compound("InfusionAltar", new net.thaumcraft.api.aspects.AspectList().add(net.thaumcraft.api.aspects.Aspects.FIRE, 25).add(net.thaumcraft.api.aspects.Aspects.EARTH, 25).add(net.thaumcraft.api.aspects.Aspects.ORDER, 25).add(net.thaumcraft.api.aspects.Aspects.AIR, 25).add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 25).add(net.thaumcraft.api.aspects.Aspects.WATER, 25), 3, 3, 3, List.of(() -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.INFUSION_MATRIX.asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.PEDESTAL.asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())));
        compound("NodeJar", new net.thaumcraft.api.aspects.AspectList().add(net.thaumcraft.api.aspects.Aspects.FIRE, 70).add(net.thaumcraft.api.aspects.Aspects.EARTH, 70).add(net.thaumcraft.api.aspects.Aspects.AIR, 70).add(net.thaumcraft.api.aspects.Aspects.WATER, 70).add(net.thaumcraft.api.aspects.Aspects.ORDER, 70).add(net.thaumcraft.api.aspects.Aspects.ENTROPY, 70), 3, 4, 3, List.of(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem()), () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem())));
        compound("Thaumatorium", new net.thaumcraft.api.aspects.AspectList().add(net.thaumcraft.api.aspects.Aspects.FIRE, 15).add(net.thaumcraft.api.aspects.Aspects.ORDER, 30).add(net.thaumcraft.api.aspects.Aspects.WATER, 30), 1, 3, 1, List.of(() -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRUCIBLE.asItem())));
        compound("AdvAlchemyFurnace", new net.thaumcraft.api.aspects.AspectList().add(net.thaumcraft.api.aspects.Aspects.FIRE, 50).add(net.thaumcraft.api.aspects.Aspects.WATER, 50).add(net.thaumcraft.api.aspects.Aspects.ORDER, 50), 3, 2, 3, List.of(() -> new net.minecraft.world.item.ItemStack(TCBlocks.ALEMBIC.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALEMBIC.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), () -> net.minecraft.world.item.ItemStack.EMPTY, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALEMBIC.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALEMBIC.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_FURNACE.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem())));
        ref("Alumentum", "ALUMENTUM", () -> new net.minecraft.world.item.ItemStack(TCItems.ALUMENTUM));
        ref("Nitor", "NITOR", () -> new net.minecraft.world.item.ItemStack(TCItems.NITOR));
        ref("Thaumium", "THAUMIUM", () -> new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")));
        ref("VoidMetal", "VOIDMETAL", () -> new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot")));
        ref("VoidSeed", "VOIDMETAL", () -> new net.minecraft.world.item.ItemStack(TCResources.get("void_seed")));
        ref("Tallow", "TALLOW", () -> new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow")));
        ref("AltGunpowder", "ALCHEMICALDUPLICATION", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GUNPOWDER, 2));
        ref("AltSlime", "ALCHEMICALDUPLICATION", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SLIME_BALL, 2));
        ref("AltClay", "ALCHEMICALDUPLICATION", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CLAY_BALL, 2));
        ref("AltGlowstone", "ALCHEMICALDUPLICATION", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GLOWSTONE_DUST, 2));
        ref("AltInk", "ALCHEMICALDUPLICATION", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.INK_SAC, 2));
        ref("AltWeb", "ALCHEMICALMANUFACTURE", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.COBWEB.asItem()));
        ref("AltMossyCobble", "ALCHEMICALMANUFACTURE", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE.asItem()));
        ref("AltIce", "ALCHEMICALMANUFACTURE", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.ICE.asItem()));
        ref("AltCrackedBrick", "ENTROPICPROCESSING", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.CRACKED_STONE_BRICKS.asItem()));
        ref("AltBonemeal", "ENTROPICPROCESSING", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BONE_MEAL, 4));
        ref("PureIron", "PUREIRON", () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_iron_cluster")));
        ref("PureGold", "PUREGOLD", () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_gold_cluster")));
        ref("PureCopper", "PURECOPPER", () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_copper_cluster")));
        ref("PureTin", "PURETIN", () -> net.minecraft.world.item.ItemStack.EMPTY);
        ref("PureSilver", "PURESILVER", () -> net.minecraft.world.item.ItemStack.EMPTY);
        ref("PureLead", "PURELEAD", () -> net.minecraft.world.item.ItemStack.EMPTY);
        ref("TransIron", "TRANSIRON", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 3));
        ref("TransGold", "TRANSGOLD", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET, 3));
        ref("TransCopper", "TRANSCOPPER", () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COPPER_NUGGET, 3));
        ref("TransTin", "TRANSTIN", () -> net.minecraft.world.item.ItemStack.EMPTY);
        ref("TransSilver", "TRANSSILVER", () -> net.minecraft.world.item.ItemStack.EMPTY);
        ref("TransLead", "TRANSLEAD", () -> net.minecraft.world.item.ItemStack.EMPTY);
        ref("EtherealBloom", "ETHEREALBLOOM", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ETHEREAL_BLOOM.asItem()));
        ref("LiquidDeath", "LIQUIDDEATH", () -> new net.minecraft.world.item.ItemStack(TCItems.BUCKET_DEATH));
        ref("BottleTaint", "BOTTLETAINT", () -> new net.minecraft.world.item.ItemStack(TCItems.BOTTLE_TAINT));
        ref("GolemStraw", "GOLEMSTRAW", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("straw")));
        ref("GolemWood", "GOLEMWOOD", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("wood")));
        ref("GolemTallow", "GOLEMTALLOW", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("tallow")));
        ref("GolemClay", "GOLEMCLAY", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("clay")));
        ref("GolemFlesh", "GOLEMFLESH", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("flesh")));
        ref("GolemStone", "GOLEMSTONE", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("stone")));
        ref("GolemIron", "GOLEMIRON", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("iron")));
        ref("GolemThaumium", "GOLEMTHAUMIUM", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("thaumium")));
        ref("CoreGather", "COREGATHER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("gather")));
        ref("CoreFill", "COREFILL", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("fill")));
        ref("CoreEmpty", "COREEMPTY", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("empty")));
        ref("CoreHarvest", "COREHARVEST", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("harvest")));
        ref("CoreGuard", "COREGUARD", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("guard")));
        ref("CoreButcher", "COREBUTCHER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("butcher")));
        ref("CoreLiquid", "CORELIQUID", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("decanting")));
        ref("BathSalts", "BATHSALTS", () -> new net.minecraft.world.item.ItemStack(TCItems.BATH_SALTS));
        ref("SaneSoap", "SANESOAP", () -> new net.minecraft.world.item.ItemStack(TCItems.SANITY_SOAP));
        ref("PrimalCharm", "BASICARTIFACE", () -> new net.minecraft.world.item.ItemStack(TCResources.get("primal_charm")));
        ref("ArcaneDoor", "WARDEDARCANA", () -> new net.minecraft.world.item.ItemStack(TCItems.ARCANE_DOOR));
        ref("WardedGlass", "WARDEDARCANA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.WARDED_GLASS.asItem(), 8));
        ref("IronKey", "WARDEDARCANA", () -> new net.minecraft.world.item.ItemStack(TCItems.IRON_KEY, 2));
        ref("FluxScrubber", "FLUXSCRUB", () -> new net.minecraft.world.item.ItemStack(TCBlocks.FLUX_SCRUBBER.asItem()));
        ref("GoldKey", "WARDEDARCANA", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLD_KEY, 2));
        ref("ArcanePressurePlate", "WARDEDARCANA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_PRESSURE_PLATE.asItem()));
        ref("NodeStabilizer", "NODESTABILIZER", () -> new net.minecraft.world.item.ItemStack(TCBlocks.NODE_STABILIZER.asItem()));
        ref("NodeTransducer", "VISPOWER", () -> new net.minecraft.world.item.ItemStack(TCBlocks.NODE_CONVERTER.asItem()));
        ref("NodeRelay", "VISPOWER", () -> new net.minecraft.world.item.ItemStack(TCBlocks.VIS_RELAY.asItem(), 2));
        ref("NodeChargeRelay", "VISCHARGERELAY", () -> new net.minecraft.world.item.ItemStack(TCBlocks.WORKBENCH_CHARGER.asItem()));
        ref("FocalManipulator", "FOCALMANIPULATION", () -> new net.minecraft.world.item.ItemStack(TCBlocks.FOCAL_MANIPULATOR.asItem()));
        ref("GolemFetter", "GOLEMFETTER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_FETTER));
        ref("ArcaneStone1", "ARCANESTONE", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem(), 9));
        crafting("ArcaneStone2", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem(), 4), 2, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem()))));
        crafting("ArcaneStone3", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_STONE_STAIRS, 4), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()))));
        crafting("ArcaneStone4", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_STONE_SLAB.asItem(), 6), 3, 1, List.of(List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem())), List.of(new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()))));
        ref("PaveTravel", "PAVETRAVEL", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("paving_stone_travel").asItem(), 4));
        ref("ArcaneLamp", "ARCANELAMP", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_LAMP.asItem()));
        ref("ArcaneSpa", "ARCANESPA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_SPA.asItem()));
        ref("PaveWard", "PAVEWARD", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("paving_stone_warding").asItem(), 4));
        ref("Levitator", "LEVITATOR", () -> new net.minecraft.world.item.ItemStack(TCBlocks.LEVITATOR.asItem()));
        ref("ArcaneEar", "ARCANEEAR", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_EAR.asItem()));
        ref("MirrorGlass", "BASICARTIFACE", () -> new net.minecraft.world.item.ItemStack(TCResources.get("mirrored_glass")));
        ref("BoneBow", "BONEBOW", () -> new net.minecraft.world.item.ItemStack(TCItems.BONE_BOW));
        ref("InfusionMatrix", "INFUSION", () -> new net.minecraft.world.item.ItemStack(TCBlocks.INFUSION_MATRIX.asItem()));
        ref("ArcanePedestal", "INFUSION", () -> new net.minecraft.world.item.ItemStack(TCBlocks.PEDESTAL.asItem(), 2));
        ref("WardedJar", "DISTILESSENTIA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.JAR.asItem()));
        ref("JarVoid", "JARVOID", () -> new net.minecraft.world.item.ItemStack(TCBlocks.JAR_VOID.asItem()));
        ref("WandCapGold", "CAP_gold", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("gold")));
        ref("WandCapCopper", "CAP_copper", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("copper")));
        ref("WandCapSilverInert", "CAP_silver", () -> new net.minecraft.world.item.ItemStack(TCItems.INERT_CAPS.get("silver")));
        ref("WandCapThaumiumInert", "CAP_thaumium", () -> new net.minecraft.world.item.ItemStack(TCItems.INERT_CAPS.get("thaumium")));
        ref("WandCapVoidInert", "CAP_void", () -> new net.minecraft.world.item.ItemStack(TCItems.INERT_CAPS.get("void")));
        ref("WandRodGreatwood", "ROD_greatwood", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("greatwood")));
        ref("WandRodGreatwoodStaff", "ROD_greatwood_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("greatwood")));
        ref("WandRodObsidianStaff", "ROD_obsidian_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("obsidian")));
        ref("WandRodSilverwoodStaff", "ROD_silverwood_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("silverwood")));
        ref("WandRodIceStaff", "ROD_ice_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("ice")));
        ref("WandRodQuartzStaff", "ROD_quartz_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("quartz")));
        ref("WandRodReedStaff", "ROD_reed_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("reed")));
        ref("WandRodBlazeStaff", "ROD_blaze_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("blaze")));
        ref("WandRodBoneStaff", "ROD_bone_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("bone")));
        ref("FocusFire", "FOCUSFIRE", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("fire")));
        ref("FocusFrost", "FOCUSFROST", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("frost")));
        ref("FocusShock", "FOCUSSHOCK", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("shock")));
        ref("FocusTrade", "FOCUSTRADE", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("trade")));
        ref("FocusExcavation", "FOCUSEXCAVATION", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("excavation")));
    }

    private static void part2() {
        ref("FocusPrimal", "FOCUSPRIMAL", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("primal")));
        ref("FocusPouch", "FOCUSPOUCH", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCUS_POUCH));
        ref("Deconstructor", "DECONSTRUCTOR", () -> new net.minecraft.world.item.ItemStack(TCBlocks.DECONSTRUCTION_TABLE.asItem()));
        ref("ArcaneBoreBase", "ARCANEBORE", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_BORE_BASE.asItem()));
        ref("EnchantedFabric", "ENCHFABRIC", () -> new net.minecraft.world.item.ItemStack(TCResources.get("enchanted_fabric")));
        ref("RobeChest", "ENCHFABRIC", () -> new net.minecraft.world.item.ItemStack(TCItems.ROBE_CHESTPLATE));
        ref("RobeLegs", "ENCHFABRIC", () -> new net.minecraft.world.item.ItemStack(TCItems.ROBE_LEGGINGS));
        ref("RobeBoots", "ENCHFABRIC", () -> new net.minecraft.world.item.ItemStack(TCItems.ROBE_BOOTS));
        ref("Goggles", "GOGGLES", () -> new net.minecraft.world.item.ItemStack(TCItems.GOGGLES));
        ref("HungryChest", "HUNGRYCHEST", () -> new net.minecraft.world.item.ItemStack(TCBlocks.HUNGRY_CHEST.asItem()));
        ref("GolemBell", "GOLEMBELL", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_BELL));
        ref("CoreBlank", "COREGATHER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORE_BLANK));
        ref("UpgradeAir", "UPGRADEAIR", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(0)));
        ref("UpgradeEarth", "UPGRADEEARTH", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(1)));
        ref("UpgradeFire", "UPGRADEFIRE", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(2)));
        ref("UpgradeWater", "UPGRADEWATER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(3)));
        ref("UpgradeOrder", "UPGRADEORDER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(4)));
        ref("UpgradeEntropy", "UPGRADEENTROPY", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(5)));
        ref("TinyHat", "TINYHAT", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(0)));
        ref("TinyFez", "TINYFEZ", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(3)));
        ref("TinyBowtie", "TINYBOWTIE", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(2)));
        ref("TinyGlasses", "TINYGLASSES", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(1)));
        ref("TinyDart", "TINYDART", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(4)));
        ref("TinyVisor", "TINYVISOR", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(5)));
        ref("TinyArmor", "TINYARMOR", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(6)));
        ref("TinyHammer", "TINYHAMMER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(7)));
        ref("Filter", "DISTILESSENTIA", () -> new net.minecraft.world.item.ItemStack(TCResources.get("vis_filter"), 2));
        ref("AlchemyFurnace", "DISTILESSENTIA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_FURNACE.asItem()));
        ref("Alembic", "DISTILESSENTIA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALEMBIC.asItem()));
        ref("Bellows", "BELLOWS", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BELLOWS.asItem()));
        ref("Tube", "TUBES", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE.asItem(), 8));
        ref("Resonator", "TUBES", () -> new net.minecraft.world.item.ItemStack(TCItems.RESONATOR));
        ref("TubeValve", "TUBES", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE_VALVE.asItem()));
        ref("TubeFilter", "TUBEFILTER", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE_FILTER.asItem()));
        ref("TubeRestrict", "TUBEFILTER", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE_RESTRICT.asItem()));
        ref("TubeOneway", "TUBEFILTER", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE_ONEWAY.asItem()));
        ref("TubeBuffer", "CENTRIFUGE", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE_BUFFER.asItem()));
        ref("AlchemicalConstruct", "DISTILESSENTIA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()));
        ref("AdvAlchemyConstruct", "ADVALCHEMYFURNACE", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem(), 4));
        ref("Centrifuge", "CENTRIFUGE", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CENTRIFUGE.asItem()));
        ref("EssentiaCrystalizer", "ESSENTIACRYSTAL", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ESSENTIA_CRYSTALIZER.asItem()));
        ref("MnemonicMatrix", "THAUMATORIUM", () -> new net.minecraft.world.item.ItemStack(TCBlocks.MNEMONIC_MATRIX.asItem()));
        enchantment("InfEnchRepair", "INFUSIONENCHANTMENT", "thaumcraft:repair");
        enchantment("InfEnchHaste", "INFUSIONENCHANTMENT", "thaumcraft:haste");
        enchantment("InfEnch0", "INFUSIONENCHANTMENT", "minecraft:protection");
        enchantment("InfEnch1", "INFUSIONENCHANTMENT", "minecraft:fire_protection");
        enchantment("InfEnch2", "INFUSIONENCHANTMENT", "minecraft:blast_protection");
        enchantment("InfEnch3", "INFUSIONENCHANTMENT", "minecraft:projectile_protection");
        enchantment("InfEnch4", "INFUSIONENCHANTMENT", "minecraft:feather_falling");
        enchantment("InfEnch5", "INFUSIONENCHANTMENT", "minecraft:respiration");
        enchantment("InfEnch6", "INFUSIONENCHANTMENT", "minecraft:aqua_affinity");
        enchantment("InfEnch7", "INFUSIONENCHANTMENT", "minecraft:thorns");
        enchantment("InfEnch8", "INFUSIONENCHANTMENT", "minecraft:sharpness");
        enchantment("InfEnch9", "INFUSIONENCHANTMENT", "minecraft:smite");
        enchantment("InfEnch10", "INFUSIONENCHANTMENT", "minecraft:bane_of_arthropods");
        enchantment("InfEnch11", "INFUSIONENCHANTMENT", "minecraft:knockback");
        enchantment("InfEnch12", "INFUSIONENCHANTMENT", "minecraft:fire_aspect");
        enchantment("InfEnch13", "INFUSIONENCHANTMENT", "minecraft:looting");
        enchantment("InfEnch14", "INFUSIONENCHANTMENT", "minecraft:efficiency");
        enchantment("InfEnch15", "INFUSIONENCHANTMENT", "minecraft:silk_touch");
        enchantment("InfEnch16", "INFUSIONENCHANTMENT", "minecraft:unbreaking");
        enchantment("InfEnch17", "INFUSIONENCHANTMENT", "minecraft:fortune");
        enchantment("InfEnch18", "INFUSIONENCHANTMENT", "minecraft:power");
        enchantment("InfEnch19", "INFUSIONENCHANTMENT", "minecraft:punch");
        enchantment("InfEnch20", "INFUSIONENCHANTMENT", "minecraft:flame");
        enchantment("InfEnch21", "INFUSIONENCHANTMENT", "minecraft:infinity");
        ref("WandCapSilver", "CAP_silver", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("silver")));
        ref("WandCapThaumium", "CAP_thaumium", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("thaumium")));
        ref("WandCapVoid", "CAP_void", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("void")));
        ref("WandRodObsidian", "ROD_obsidian", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("obsidian")));
        ref("WandRodIce", "ROD_ice", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("ice")));
        ref("WandRodQuartz", "ROD_quartz", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("quartz")));
        ref("WandRodReed", "ROD_reed", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("reed")));
        ref("WandRodBlaze", "ROD_blaze", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("blaze")));
        ref("WandRodBone", "ROD_bone", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("bone")));
        ref("WandRodSilverwood", "ROD_silverwood", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("silverwood")));
        ref("WandRodPrimalStaff", "ROD_primal_staff", () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("primal")));
        ref("FocusHellbat", "FOCUSHELLBAT", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("hellbat")));
        ref("FocusPortableHole", "FOCUSPORTABLEHOLE", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("portable_hole")));
        ref("FocusWarding", "FOCUSWARDING", () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("warding")));
        ref("WandPed", "WANDPED", () -> new net.minecraft.world.item.ItemStack(TCBlocks.WAND_PEDESTAL.asItem()));
        ref("WandPedFocus", "WANDPEDFOC", () -> new net.minecraft.world.item.ItemStack(TCBlocks.RECHARGE_FOCUS.asItem()));
        ref("NodeStabilizerAdv", "NODESTABILIZERADV", () -> new net.minecraft.world.item.ItemStack(TCBlocks.NODE_STABILIZER_ADVANCED.asItem()));
        ref("JarBrain", "JARBRAIN", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BRAIN_JAR.asItem()));
        refAny("AdvancedGolem", "ADVANCEDGOLEM", () -> new net.minecraft.world.item.ItemStack((TCItems.GOLEM_PLACERS.values().toArray(new net.minecraft.world.item.Item[0]))[0]));
        ref("HoverHarness", "HOVERHARNESS", () -> new net.minecraft.world.item.ItemStack(TCItems.HOVER_HARNESS));
        ref("HoverGirdle", "HOVERGIRDLE", () -> new net.minecraft.world.item.ItemStack(TCItems.HOVER_GIRDLE));
        ref("VisAmulet", "VISAMULET", () -> new net.minecraft.world.item.ItemStack(TCItems.VIS_AMULET));
        ref("RunicAmulet", "RUNICARMOR", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_AMULET));
        ref("RunicAmuletEmergency", "RUNICEMERGENCY", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_AMULET_EMERGENCY));
        ref("RunicRing", "RUNICARMOR", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_RING));
        ref("RunicRingCharged", "RUNICCHARGED", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_RING_CHARGED));
        ref("RunicRingHealing", "RUNICHEALING", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_RING_REGEN));
        ref("RunicGirdle", "RUNICARMOR", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_GIRDLE));
        ref("RunicGirdleKinetic", "RUNICKINETIC", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_GIRDLE_KINETIC));
        ref("RunicGirdleKinetic_2", "RUNICKINETIC", () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_GIRDLE_KINETIC));
        ref("Mirror", "MIRROR", () -> new net.minecraft.world.item.ItemStack(TCBlocks.MIRROR.asItem()));
        ref("MirrorHand", "MIRRORHAND", () -> new net.minecraft.world.item.ItemStack(TCItems.HAND_MIRROR));
        ref("MirrorEssentia", "MIRRORESSENTIA", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ESSENTIA_MIRROR.asItem()));
        ref("ElementalAxe", "ELEMENTALAXE", () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_AXE));
        ref("ElementalPick", "ELEMENTALPICK", () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_PICKAXE));
        ref("ElementalSword", "ELEMENTALSWORD", () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_SWORD));
        ref("ElementalShovel", "ELEMENTALSHOVEL", () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_SHOVEL));
        ref("ElementalHoe", "ELEMENTALHOE", () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_HOE));
        ref("BootsTraveller", "BOOTSTRAVELLER", () -> new net.minecraft.world.item.ItemStack(TCItems.TRAVELLER_BOOTS));
    }

    private static void part3() {
        ref("CoreAlchemy", "COREALCHEMY", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("alchemy")));
        ref("CoreSorting", "CORESORTING", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("sorting")));
        ref("CoreLumber", "CORELUMBER", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("chop")));
        ref("CoreFishing", "COREFISHING", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("fishing")));
        ref("CoreUse", "COREUSE", () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("use")));
        ref("ArcaneBore", "ARCANEBORE", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_BORE.asItem()));
        ref("TravelTrunk", "TRAVELTRUNK", () -> new net.minecraft.world.item.ItemStack(TCItems.TRUNK_SPAWNER));
        ref("LampGrowth", "LAMPGROWTH", () -> new net.minecraft.world.item.ItemStack(TCBlocks.GROWTH_LAMP.asItem()));
        ref("LampFertility", "LAMPFERTILITY", () -> new net.minecraft.world.item.ItemStack(TCBlocks.FERTILITY_LAMP.asItem()));
        ref("ThaumiumFortressHelm", "ARMORFORTRESS", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_HELMET));
        ref("ThaumiumFortressChest", "ARMORFORTRESS", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_CHESTPLATE));
        ref("ThaumiumFortressLegs", "ARMORFORTRESS", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_LEGGINGS));
        ref("VoidRobeHelm", "ARMORVOIDFORTRESS", () -> new net.minecraft.world.item.ItemStack(TCItems.VOID_ROBE_HELMET));
        ref("VoidRobeChest", "ARMORVOIDFORTRESS", () -> new net.minecraft.world.item.ItemStack(TCItems.VOID_ROBE_CHESTPLATE));
        ref("VoidRobeLegs", "ARMORVOIDFORTRESS", () -> new net.minecraft.world.item.ItemStack(TCItems.VOID_ROBE_LEGGINGS));
        refAny("HelmGoggles", "HELMGOGGLES", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_HELMET));
        refAny("MaskGrinningDevil", "MASKGRINNINGDEVIL", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_HELMET));
        refAny("MaskAngryGhost", "MASKANGRYGHOST", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_HELMET));
        refAny("MaskSippingFiend", "MASKSIPPINGFIEND", () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_HELMET));
        ref("SanityCheck", "SANITYCHECK", () -> new net.minecraft.world.item.ItemStack(TCItems.SANITY_CHECKER));
        ref("EssentiaReservoir", "ESSENTIARESERVOIR", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ESSENTIA_RESERVOIR.asItem()));
        ref("SinStone", "SINSTONE", () -> new net.minecraft.world.item.ItemStack(TCItems.SINISTER_STONE));
        ref("PrimalCrusher", "PRIMALCRUSHER", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_CRUSHER));
        ref("EldritchEye", "OCULUS", () -> new net.minecraft.world.item.ItemStack(TCItems.ELDRITCH_EYE));
        crafting("MundaneAmulet", () -> new net.minecraft.world.item.ItemStack(TCItems.MUNDANE_AMULET), 3, 3, List.of(List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)), List.of()));
        crafting("MundaneRing", () -> new net.minecraft.world.item.ItemStack(TCItems.MUNDANE_RING), 3, 3, List.of(List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), List.of()));
        crafting("MundaneBelt", () -> new net.minecraft.world.item.ItemStack(TCItems.MUNDANE_BELT), 3, 3, List.of(List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)), List.of()));
        crafting("JarLabel", () -> new net.minecraft.world.item.ItemStack(TCResources.get("jar_label"), 4), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DYE.black())), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SLIME_BALL)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER))));
        crafting("JarLabelNull", () -> new net.minecraft.world.item.ItemStack(TCResources.get("jar_label")), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("jar_label")))));
        crafting("WandBasic", () -> net.thaumcraft.item.WandItem.bookStack("iron", "wood", false), 3, 3, List.of(List.of(), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("iron"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("iron"))), List.of(), List.of()));
        crafting("WandCapIron", () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("iron")), 3, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET))));
        crafting("KnowFrag", () -> BookStacks.unknownNotes(), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment")))));
        crafting("PlankGreatwood", () -> new net.minecraft.world.item.ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem(), 4), 1, 1, List.of(List.of(new net.minecraft.world.item.ItemStack(TCBlocks.GREATWOOD_LOG.asItem()))));
        crafting("PlankSilverwood", () -> new net.minecraft.world.item.ItemStack(TCBlocks.SILVERWOOD_PLANKS.asItem(), 4), 1, 1, List.of(List.of(new net.minecraft.world.item.ItemStack(TCBlocks.SILVERWOOD_LOG.asItem()))));
        crafting("BlockFlesh", () -> new net.minecraft.world.item.ItemStack(TCBlocks.FLESH_BLOCK.asItem()), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH))));
        crafting("BlockThaumium", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("thaumium_block").asItem()), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")))));
        crafting("BlockTallow", () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("tallow_block").asItem()), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow")))));
        crafting("Clusters6", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("balanced").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy")))));
        crafting("Grate", () -> new net.minecraft.world.item.ItemStack(TCBlocks.ITEM_GRATE.asItem()), 1, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.IRON_BARS.asItem())), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.OAK_TRAPDOOR.asItem()))));
        crafting("Phial", () -> new net.minecraft.world.item.ItemStack(TCItems.PHIAL, 8), 3, 3, List.of(List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CLAY_BALL)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem())), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem())), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem())), List.of()));
        crafting("Table", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TABLE.asItem()), 3, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SLAB)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_PLANKS)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_PLANKS))));
        crafting("Scribe1", () -> new net.minecraft.world.item.ItemStack(TCItems.SCRIBING_TOOLS), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.PHIAL)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DYE.black()))));
        crafting("Scribe2", () -> new net.minecraft.world.item.ItemStack(TCItems.SCRIBING_TOOLS), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DYE.black()))));
        crafting("Scribe3", () -> new net.minecraft.world.item.ItemStack(TCItems.SCRIBING_TOOLS), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SCRIBING_TOOLS)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DYE.black()))));
        crafting("Thaumometer", () -> new net.minecraft.world.item.ItemStack(TCItems.THAUMOMETER), 3, 3, List.of(List.of(), stacks(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GLASS.asItem())), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)), List.of(), stacks(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), List.of()));
        crafting("ThaumiumHelm", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_helmet")), 3, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")))));
        crafting("ThaumiumChest", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_chestplate")), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")))));
        crafting("ThaumiumLegs", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_leggings")), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")))));
        crafting("ThaumiumBoots", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_boots")), 3, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")))));
        crafting("ThaumiumShovel", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_shovel")), 1, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK))));
        crafting("ThaumiumPick", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_pickaxe")), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of()));
        crafting("ThaumiumAxe", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_axe")), 2, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of()));
        crafting("ThaumiumHoe", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_hoe")), 2, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of()));
        crafting("ThaumiumSword", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("thaumium_sword")), 1, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK))));
        crafting("VoidHelm", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_helmet")), 3, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot")))));
        crafting("VoidChest", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_chestplate")), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot")))));
        crafting("VoidLegs", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_leggings")), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot")))));
        crafting("VoidBoots", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_boots")), 3, 2, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot")))));
        crafting("VoidShovel", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_shovel")), 1, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK))));
        crafting("VoidPick", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_pickaxe")), 3, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of()));
        crafting("VoidAxe", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_axe")), 2, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of()));
        crafting("VoidHoe", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_hoe")), 2, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)), List.of()));
        crafting("VoidSword", () -> new net.minecraft.world.item.ItemStack(TCItems.GEAR.get("void_sword")), 1, 3, List.of(List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot"))), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK))));
        crafting("TallowCandle", () -> new net.minecraft.world.item.ItemStack(TCBlocks.TALLOW_CANDLES.get("white").asItem(), 3), 3, 3, List.of(List.of(), List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING)), List.of(), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of(), List.of(), List.of(new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow"))), List.of()));
        // os feitos em laço no original
        refAny("BalancedShard_0", "CRUCIBLE", () -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED));
        refAny("BalancedShard_1", "CRUCIBLE", () -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED));
        refAny("BalancedShard_2", "CRUCIBLE", () -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED));
        refAny("BalancedShard_3", "CRUCIBLE", () -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED));
        refAny("BalancedShard_4", "CRUCIBLE", () -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED));
        refAny("BalancedShard_5", "CRUCIBLE", () -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED));
        ref("Banner_0", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(0));
        ref("Banner_1", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(1));
        ref("Banner_2", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(2));
        ref("Banner_3", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(3));
        ref("Banner_4", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(4));
        ref("Banner_5", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(5));
        ref("Banner_6", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(6));
        ref("Banner_7", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(7));
        ref("Banner_8", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(8));
        ref("Banner_9", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(9));
        ref("Banner_10", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(10));
        ref("Banner_11", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(11));
        ref("Banner_12", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(12));
        ref("Banner_13", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(13));
        ref("Banner_14", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(14));
        ref("Banner_15", "BANNERS", () -> net.thaumcraft.block.BannerBlock.stack(15));
        ref("PrimalArrow_0", "PRIMALARROW", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("air")));
        ref("PrimalArrow_1", "PRIMALARROW", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("fire")));
        ref("PrimalArrow_2", "PRIMALARROW", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("water")));
        ref("PrimalArrow_3", "PRIMALARROW", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("earth")));
        ref("PrimalArrow_4", "PRIMALARROW", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("order")));
        ref("PrimalArrow_5", "PRIMALARROW", () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("entropy")));
        labels();
        crafting("Clusters0", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("air").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("air")))));
        crafting("Clusters1", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("fire").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("fire")))));
        crafting("Clusters2", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("water").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("water")))));
        crafting("Clusters3", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("earth").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("earth")))));
        crafting("Clusters4", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("order").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("order")))));
        crafting("Clusters5", () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRYSTAL_CLUSTERS.get("entropy").asItem()), 0, 0, List.of(List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy"))), List.of(new net.minecraft.world.item.ItemStack(TCItems.SHARDS.get("entropy")))));
        sceptre("Sceptre_1", "iron", "wood");
        sceptre("Sceptre_2", "gold", "greatwood");
        sceptre("Sceptre_3", "thaumium", "silverwood");
    }

}
