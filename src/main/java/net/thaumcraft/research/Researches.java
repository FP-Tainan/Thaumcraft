package net.thaumcraft.research;

import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A árvore inteira do Thaumonomicon, como ela é no Thaumcraft 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/livro-pesquisas.js} a partir do {@code ConfigResearch} do jar
 * original: posições, ligações, aspectos, marcas, ícones e páginas (as de texto e as de receita, pelo nome da receita).
 * Para mudar, mude o gerador.
 */
public final class Researches {
    /** As pesquisas na ordem em que o mod original as registra. */
    public static final Map<String, Research> ALL = new LinkedHashMap<>();

    private Researches() {
    }

    private static void add(Research research) {
        ALL.put(research.key(), research);
    }

    /**
     * Põe no livro uma pesquisa que não é do Thaumcraft: é por aqui que um mod de fora entra na árvore, como os
     * addons entravam pelo {@code ResearchItem.registerResearchItem} do original. A tabela do mod já está montada
     * quando isto é chamado, então a pesquisa nova entra no fim, sem mexer na ordem do original.
     */
    public static void register(Research research) {
        add(research);
    }

    /**
     * Dá uma irmã nova a uma pesquisa que já está no livro: é o {@code addSiblingToOriginal} do
     * {@code ResearchItemProxy}, que faz a sombra do ramo abrir junto com a pesquisa de que ela é sombra.
     */
    public static void addSibling(String key, String sibling) {
        Research research = ALL.get(key);
        if (research == null) return;
        if (research.siblings().contains(sibling)) return;
        List<String> irmas = new ArrayList<>(research.siblings());
        irmas.add(sibling);
        add(new Research(research.key(), research.category(), research.tags(), research.column(), research.row(),
                research.complexity(), research.iconTexture(), research.iconStack(), research.marks(),
                research.parents(), research.parentsHidden(), List.copyOf(irmas), research.pages(),
                research.warp(), research.requires()));
    }

    /** Só para garantir que a tabela do mod já esteja montada antes de um mod de fora mexer nela. */
    public static void init() {
    }

    /** Tudo o que está numa categoria, na ordem do original. */
    public static List<Research> of(String category) {
        List<Research> found = new ArrayList<>();
        for (Research research : ALL.values()) {
            if (research.category().equals(category)) found.add(research);
        }
        return found;
    }

    public static Research get(String key) {
        return ALL.get(key);
    }

    static {
        thaumaturgy();
        artifice();
        alchemy();
        golemancy();
        basics();
        eldritch();
    }

    private static void thaumaturgy() {
        add(new Research("BASICTHAUMATURGY", "THAUMATURGY", new AspectList(), 0, 0, 0,
                null, () -> net.thaumcraft.item.WandItem.bookStack("iron", "wood", false), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BASICTHAUMATURGY.1"),
                        new Page.Text("tc.research_page.BASICTHAUMATURGY.2"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("WandCapIron")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("WandBasic"))), 0, null));
        add(new Research("FOCUSFIRE", "THAUMATURGY", new AspectList().add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), 2, -2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("fire")), List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSFIRE.1"),
                        new Page.Text("tc.research_page.FOCUSFIRE.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusFire"))), 0, null));
        add(new Research("FOCUSFROST", "THAUMATURGY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 3).add(Aspects.COLD, 6), 1, -5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("frost")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSFROST.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusFrost"))), 0, null));
        add(new Research("FOCUSHELLBAT", "THAUMATURGY", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.BEAST, 6).add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), 3, -7, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("hellbat")), List.of(Research.Mark.HIDDEN),
                List.of(), List.of("FOCUSFIRE", "INFUSION"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSHELLBAT.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("FocusHellbat"))), 2, null));
        add(new Research("FOCUSEXCAVATION", "THAUMATURGY", new AspectList().add(Aspects.EARTH, 3).add(Aspects.ENTROPY, 3).add(Aspects.MAGIC, 3), 0, -3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("excavation")), List.of(Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSEXCAVATION.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusExcavation"))), 0, null));
        add(new Research("FOCUSWARDING", "THAUMATURGY", new AspectList().add(Aspects.EARTH, 6).add(Aspects.ARMOR, 3).add(Aspects.ORDER, 3).add(Aspects.MIND, 3), -2, -4, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("warding")), List.of(Research.Mark.CONCEALED),
                List.of("FOCUSEXCAVATION", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSWARDING.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("FocusWarding"))), 0, null));
        add(new Research("FOCUSSHOCK", "THAUMATURGY", new AspectList().add(Aspects.AIR, 3).add(Aspects.ENERGY, 6).add(Aspects.MAGIC, 3), 3, -5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("shock")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSSHOCK.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusShock"))), 0, null));
        add(new Research("FOCUSTRADE", "THAUMATURGY", new AspectList().add(Aspects.EARTH, 3).add(Aspects.EXCHANGE, 6).add(Aspects.MAGIC, 3), 4, -3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("trade")), List.of(Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSTRADE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusTrade"))), 0, null));
        add(new Research("FOCUSPORTABLEHOLE", "THAUMATURGY", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.ENTROPY, 3).add(Aspects.ELDRITCH, 6).add(Aspects.AIR, 3), 7, -2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("portable_hole")), List.of(Research.Mark.CONCEALED),
                List.of("FOCUSTRADE", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSPORTABLEHOLE.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("FocusPortableHole"))), 0, null));
        add(new Research("FOCUSPOUCH", "THAUMATURGY", new AspectList().add(Aspects.VOID, 6).add(Aspects.MAGIC, 3).add(Aspects.TOOL, 3), 4, -1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCUS_POUCH), List.of(Research.Mark.SECONDARY),
                List.of("FOCUSFIRE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSPOUCH.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusPouch"))), 0, null));
        add(new Research("CAP_iron", "THAUMATURGY", new AspectList(), 0, 0, 1,
                null, null, List.of(Research.Mark.AUTO, Research.Mark.VIRTUAL),
                List.of(), List.of(), List.of(),
                List.of(), 0, null));
        add(new Research("CAP_gold", "THAUMATURGY", new AspectList().add(Aspects.METAL, 3).add(Aspects.GREED, 3).add(Aspects.TOOL, 3), 3, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("gold")), List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CAP_gold.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandCapGold"))), 0, null));
        add(new Research("CAP_thaumium", "THAUMATURGY", new AspectList().add(Aspects.METAL, 6).add(Aspects.MAGIC, 6).add(Aspects.TOOL, 3).add(Aspects.AURA, 3), 5, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("thaumium")), List.of(),
                List.of("CAP_gold", "THAUMIUM", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CAP_thaumium.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandCapThaumiumInert")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandCapThaumium"))), 0, null));
        add(new Research("CAP_copper", "THAUMATURGY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3).add(Aspects.TOOL, 3), 2, 0, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("copper")), List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CAP_copper.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandCapCopper"))), 0, null));
        add(new Research("CAP_silver", "THAUMATURGY", new AspectList().add(Aspects.METAL, 3).add(Aspects.GREED, 3).add(Aspects.TOOL, 3).add(Aspects.AURA, 3), 5, 1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("silver")), List.of(Research.Mark.CONCEALED),
                List.of("CAP_gold", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CAP_silver.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandCapSilverInert")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandCapSilver"))), 0, "c:ingots/silver"));
        add(new Research("ROD_wood", "THAUMATURGY", new AspectList(), 0, 0, 1,
                null, null, List.of(Research.Mark.AUTO, Research.Mark.VIRTUAL),
                List.of(), List.of(), List.of(),
                List.of(), 0, null));
        add(new Research("ROD_greatwood", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.TREE, 6).add(Aspects.MAGIC, 3), -5, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("greatwood")), List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_greatwood.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodGreatwood"))), 0, null));
        add(new Research("ROD_reed", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.AIR, 6).add(Aspects.PLANT, 3).add(Aspects.MAGIC, 3), -5, -1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("reed")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_reed.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodReed"))), 0, null));
        add(new Research("ROD_blaze", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.FIRE, 6).add(Aspects.ENERGY, 3).add(Aspects.MAGIC, 3), -7, 0, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("blaze")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_blaze.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodBlaze"))), 0, null));
        add(new Research("ROD_obsidian", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EARTH, 6).add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), -8, 2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("obsidian")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_obsidian.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodObsidian"))), 0, null));
        add(new Research("ROD_ice", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.COLD, 6).add(Aspects.WATER, 3).add(Aspects.MAGIC, 3), -7, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("ice")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_ice.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodIce"))), 0, null));
        add(new Research("ROD_quartz", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ORDER, 6).add(Aspects.CRYSTAL, 3).add(Aspects.MAGIC, 3), -5, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("quartz")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_quartz.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodQuartz"))), 0, null));
        add(new Research("ROD_bone", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ENTROPY, 6).add(Aspects.UNDEAD, 3).add(Aspects.MAGIC, 3), -3, 0, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("bone")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_bone.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodBone"))), 1, null));
        add(new Research("ROD_silverwood", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 6).add(Aspects.TREE, 6).add(Aspects.MAGIC, 9), -2, 5, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_RODS.get("silverwood")), List.of(),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_silverwood.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodSilverwood"))), 0, null));
        add(new Research("SCEPTRE", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 6).add(Aspects.CRAFT, 6).add(Aspects.TREE, 6).add(Aspects.MAGIC, 9), 0, 4, 3,
                null, () -> net.thaumcraft.item.WandItem.bookStack("thaumium", "silverwood", true), List.of(Research.Mark.CONCEALED),
                List.of("ROD_silverwood"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.SCEPTRE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Sceptre_1", "Sceptre_2", "Sceptre_3"))), 0, null));
        add(new Research("ROD_greatwood_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.TREE, 6).add(Aspects.MAGIC, 3), -1, 7, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("greatwood")), List.of(),
                List.of("ROD_silverwood"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_greatwood_staff.1"),
                        new Page.Text("tc.research_page.ROD_greatwood_staff.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodGreatwoodStaff"))), 0, null));
        add(new Research("ROD_reed_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.AIR, 6).add(Aspects.PLANT, 3).add(Aspects.MAGIC, 3), -5, -2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("reed")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_reed"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_reed_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodReedStaff"))), 0, null));
        add(new Research("ROD_blaze_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.FIRE, 6).add(Aspects.ENERGY, 3).add(Aspects.MAGIC, 3), -8, -1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("blaze")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_blaze"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_blaze_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodBlazeStaff"))), 0, null));
        add(new Research("ROD_obsidian_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EARTH, 6).add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), -9, 2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("obsidian")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_obsidian"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_obsidian_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodObsidianStaff"))), 0, null));
        add(new Research("ROD_ice_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.COLD, 6).add(Aspects.WATER, 3).add(Aspects.MAGIC, 3), -8, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("ice")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_ice"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_ice_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodIceStaff"))), 0, null));
        add(new Research("ROD_quartz_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ORDER, 6).add(Aspects.CRYSTAL, 3).add(Aspects.MAGIC, 3), -4, 6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("quartz")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_quartz"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_quartz_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodQuartzStaff"))), 0, null));
        add(new Research("ROD_bone_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ENTROPY, 6).add(Aspects.UNDEAD, 3).add(Aspects.MAGIC, 3), -2, -1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("bone")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_bone"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_bone_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodBoneStaff"))), 1, null));
        add(new Research("ROD_silverwood_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 6).add(Aspects.TREE, 6).add(Aspects.MAGIC, 9), -1, 5, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("silverwood")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_silverwood"), List.of("ROD_greatwood_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_silverwood_staff.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandRodSilverwoodStaff"))), 0, null));
        add(new Research("WANDPED", "THAUMATURGY", new AspectList().add(Aspects.AURA, 6).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.ENERGY, 3), -9, -6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.WAND_PEDESTAL.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("INFUSION", "NODEPRESERVE", "NODESTABILIZER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.WANDPED.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandPed"))), 0, null));
        add(new Research("VISAMULET", "THAUMATURGY", new AspectList().add(Aspects.AURA, 3).add(Aspects.MAGIC, 6).add(Aspects.ENERGY, 3).add(Aspects.VOID, 3), -9, -8, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.VIS_AMULET), List.of(Research.Mark.CONCEALED),
                List.of("WANDPED"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.VISAMULET.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("VisAmulet")),
                        new Page.Text("tc.research_page.VISAMULET.2")), 0, null));
        add(new Research("WANDPEDFOC", "THAUMATURGY", new AspectList().add(Aspects.AURA, 6).add(Aspects.MAGIC, 6).add(Aspects.EXCHANGE, 6).add(Aspects.ENERGY, 3).add(Aspects.TOOL, 3), -10, -7, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.RECHARGE_FOCUS.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("WANDPED"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.WANDPEDFOC.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandPedFocus"))), 0, null));
        add(new Research("NODESTABILIZER", "THAUMATURGY", new AspectList().add(Aspects.AURA, 4).add(Aspects.ORDER, 4).add(Aspects.ENERGY, 4), -7, -4, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.NODE_STABILIZER.asItem()), List.of(),
                List.of("NODEPRESERVE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODESTABILIZER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("NodeStabilizer")),
                        new Page.Text("tc.research_page.NODESTABILIZER.2")), 0, null));
        add(new Research("NODESTABILIZERADV", "THAUMATURGY", new AspectList().add(Aspects.AURA, 9).add(Aspects.MAGIC, 6).add(Aspects.ORDER, 6).add(Aspects.ENERGY, 6), -8, -3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.NODE_STABILIZER.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("NODESTABILIZER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODESTABILIZERADV.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("NodeStabilizerAdv"))), 0, null));
        add(new Research("VISPOWER", "THAUMATURGY", new AspectList().add(Aspects.AURA, 3).add(Aspects.MECHANISM, 3).add(Aspects.ENERGY, 6), -5, -6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.NODE_CONVERTER.asItem()), List.of(Research.Mark.SPECIAL),
                List.of("NODESTABILIZER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.VISPOWER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("NodeTransducer")),
                        new Page.Text("tc.research_page.VISPOWER.2"),
                        new Page.Text("tc.research_page.VISPOWER.3"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("NodeRelay")),
                        new Page.Text("tc.research_page.VISPOWER.4"),
                        new Page.Text("tc.research_page.VISPOWER.5")), 0, null));
        add(new Research("VISCHARGERELAY", "THAUMATURGY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.AURA, 3).add(Aspects.MECHANISM, 3).add(Aspects.ENERGY, 6), -7, -6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.WORKBENCH_CHARGER.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("VISPOWER", "WANDPED"), List.of("ROD_greatwood"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.VISCHARGERELAY.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("NodeChargeRelay"))), 0, null));
        add(new Research("FOCALMANIPULATION", "THAUMATURGY", new AspectList().add(Aspects.MAGIC, 8).add(Aspects.TOOL, 8).add(Aspects.CRAFT, 5).add(Aspects.CRYSTAL, 5).add(Aspects.ENERGY, 5), -3, -8, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.FOCAL_MANIPULATOR.asItem()), List.of(),
                List.of("VISPOWER"), List.of("INFUSION", "FOCUSFIRE"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCALMANIPULATION.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocalManipulator")),
                        new Page.Text("tc.research_page.FOCALMANIPULATION.2")), 0, null));
        add(new Research("VAMPBAT", "THAUMATURGY", new AspectList().add(Aspects.HUNGER, 5).add(Aspects.LIFE, 5).add(Aspects.MAGIC, 5), 4, -8, 1,
                "textures/foci/vampirebats.png", null, List.of(Research.Mark.SECONDARY),
                List.of("FOCUSHELLBAT"), List.of("FOCALMANIPULATION"), List.of(),
                List.of(
                        new Page.Text("focus.upgrade.vampirebats.text")), 0, null));
    }

    private static void artifice() {
        add(new Research("BASICARTIFACE", "ARTIFICE", new AspectList(), 0, 1, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("primal_charm")), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BASICARTIFACE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("PrimalCharm")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("MundaneAmulet")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("MundaneRing")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("MundaneBelt")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("MirrorGlass"))), 0, null));
        add(new Research("ARCANESTONE", "ARTIFICE", new AspectList(), 5, -2, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARCANESTONE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcaneStone1")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ArcaneStone2")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ArcaneStone3")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ArcaneStone4"))), 0, null));
        add(new Research("GRATE", "ARTIFICE", new AspectList(), 2, -1, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ITEM_GRATE.asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GRATE.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Grate"))), 0, null));
        add(new Research("TABLE", "ARTIFICE", new AspectList(), 0, -1, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.TABLE.asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TABLE.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Table"))), 0, null));
        add(new Research("ARCTABLE", "ARTIFICE", new AspectList(), -1, -3, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_WORKBENCH.asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("TABLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARCTABLE.1"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("ArcTable"))), 0, null));
        add(new Research("RESTABLE", "ARTIFICE", new AspectList(), 1, -3, 0,
                null, () -> BookStacks.researchTable(), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("TABLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RESTABLE.1"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("ResTable"))), 0, null));
        add(new Research("THAUMOMETER", "ARTIFICE", new AspectList(), 2, 1, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.THAUMOMETER), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.THAUMOMETER.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Thaumometer"))), 0, null));
        add(new Research("PAVETRAVEL", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.EARTH, 3).add(Aspects.FLIGHT, 3), 4, -4, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("paving_stone_travel").asItem()), List.of(Research.Mark.SECONDARY),
                List.of("ARCANESTONE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PAVETRAVEL.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("PaveTravel"))), 0, null));
        add(new Research("PAVEWARD", "ARTIFICE", new AspectList().add(Aspects.MOTION, 3).add(Aspects.TRAP, 3).add(Aspects.BEAST, 3), 6, -4, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BUILDING.get("paving_stone_warding").asItem()), List.of(Research.Mark.SECONDARY),
                List.of("ARCANESTONE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PAVEWARD.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("PaveWard")),
                        new Page.Text("tc.research_page.PAVEWARD.2")), 0, null));
        add(new Research("GOGGLES", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.AURA, 3).add(Aspects.MAGIC, 3), 4, 1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOGGLES), List.of(Research.Mark.CONCEALED),
                List.of("THAUMOMETER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOGGLES.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Goggles"))), 0, null));
        add(new Research("ARCANEEAR", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.ENERGY, 3).add(Aspects.AIR, 3), 6, 0, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_EAR.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("GOGGLES"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARCANEEAR.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcaneEar"))), 0, null));
        add(new Research("SINSTONE", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.DARKNESS, 3).add(Aspects.ELDRITCH, 3).add(Aspects.AURA, 3), 6, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.SINISTER_STONE), List.of(Research.Mark.CONCEALED),
                List.of("GOGGLES"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.SINSTONE.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("SinStone"))), 2, null));
        add(new Research("LEVITATOR", "ARTIFICE", new AspectList().add(Aspects.MOTION, 3).add(Aspects.FLIGHT, 3).add(Aspects.AIR, 3), -3, -3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.LEVITATOR.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("NITOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.LEVITATOR.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Levitator"))), 0, null));
        add(new Research("INFERNALFURNACE", "ARTIFICE", new AspectList().add(Aspects.FIRE, 6).add(Aspects.METAL, 3).add(Aspects.CRAFT, 3).add(Aspects.AURA, 3), -4, -1, 2,
                "textures/misc/r_infernalfurnace.png", null, List.of(Research.Mark.CONCEALED),
                List.of("NITOR", "ALUMENTUM"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.INFERNALFURNACE.1"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("InfernalFurnace")),
                        new Page.Text("tc.research_page.INFERNALFURNACE.2")), 2, null));
        add(new Research("BELLOWS", "ARTIFICE", new AspectList().add(Aspects.AIR, 6).add(Aspects.MECHANISM, 3).add(Aspects.MOTION, 3), -6, -2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BELLOWS.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("INFERNALFURNACE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BELLOWS.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Bellows")),
                        new Page.Text("tc.research_page.BELLOWS.2")), 0, null));
        add(new Research("ENCHFABRIC", "ARTIFICE", new AspectList().add(Aspects.CLOTH, 3).add(Aspects.MAGIC, 3), 0, 3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("enchanted_fabric")), List.of(Research.Mark.SECONDARY),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ENCHFABRIC.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("EnchantedFabric")),
                        new Page.Text("tc.research_page.ENCHFABRIC.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("RobeChest")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("RobeLegs")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("RobeBoots"))), 0, null));
        add(new Research("INFUSION", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 6).add(Aspects.MECHANISM, 3).add(Aspects.CRAFT, 6), -4, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.INFUSION_MATRIX.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("DISTILESSENTIA"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.INFUSION.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("InfusionMatrix")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcanePedestal")),
                        new Page.Text("tc.research_page.INFUSION.2"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("InfusionAltar")),
                        new Page.Text("tc.research_page.INFUSION.3"),
                        new Page.Text("tc.research_page.INFUSION.4"),
                        new Page.Text("tc.research_page.INFUSION.5")), 0, null));
        add(new Research("FLUXSCRUB", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.TRAP, 3).add(Aspects.AIR, 3).add(Aspects.WATER, 3), -8, -3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.FLUX_SCRUBBER.asItem()), List.of(Research.Mark.SECONDARY),
                List.of("VISPOWER", "BELLOWS", "TUBES"), List.of("INFUSION"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FLUXSCRUB.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FluxScrubber"))), 0, null));
        add(new Research("RUNICARMOR", "ARTIFICE", new AspectList().add(Aspects.ARMOR, 6).add(Aspects.AIR, 3).add(Aspects.MAGIC, 3).add(Aspects.ENERGY, 3).add(Aspects.MIND, 3), 3, 4, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_RING), List.of(Research.Mark.CONCEALED),
                List.of("ENCHFABRIC"), List.of("INFUSION"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RUNICARMOR.1"),
                        new Page.Text("tc.research_page.RUNICARMOR.2"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicRing")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicAmulet")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicGirdle"))), 0, null));
        add(new Research("RUNICCHARGED", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.ENERGY, 6), 2, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_RING_CHARGED), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RUNICCHARGED.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicRingCharged"))), 0, null));
        add(new Research("RUNICHEALING", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.HEAL, 4).add(Aspects.WATER, 4), 4, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_RING_REGEN), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RUNICHEALING.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicRingHealing"))), 0, null));
        add(new Research("RUNICKINETIC", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.AIR, 6), 2, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_GIRDLE_KINETIC), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RUNICKINETIC.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicGirdleKinetic"))), 0, null));
        add(new Research("RUNICEMERGENCY", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.EARTH, 4).add(Aspects.VOID, 4), 4, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.RUNIC_AMULET_EMERGENCY), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RUNICEMERGENCY.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("RunicAmuletEmergency"))), 0, null));
        add(new Research("BANNERS", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.CLOTH, 3).add(Aspects.MAGIC, 1), 4, 8, 1,
                null, () -> net.thaumcraft.block.BannerBlock.stack(10), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BANNERS.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Banner_0", "Banner_1", "Banner_2", "Banner_3", "Banner_4", "Banner_5", "Banner_6", "Banner_7", "Banner_8", "Banner_9", "Banner_10", "Banner_11", "Banner_12", "Banner_13", "Banner_14", "Banner_15"))), 0, null));
        add(new Research("RUNICAUGMENTATION", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.EXCHANGE, 4).add(Aspects.GREED, 4), 6, 4, 1,
                "textures/misc/r_runicupg.png", null, List.of(Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RUNICAUGMENTATION.1"),
                        new Page.Recipe(Page.Kind.RUNIC, List.of("RunicAugment_0", "RunicAugment_1", "RunicAugment_2", "RunicAugment_3", "RunicAugment_4")),
                        new Page.Text("tc.research_page.RUNICAUGMENTATION.2")), 0, null));
        add(new Research("BOOTSTRAVELLER", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.EARTH, 3).add(Aspects.FLIGHT, 3).add(Aspects.WATER, 3), -1, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.TRAVELLER_BOOTS), List.of(Research.Mark.CONCEALED),
                List.of("ENCHFABRIC", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BOOTSTRAVELLER.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("BootsTraveller"))), 0, null));
        add(new Research("HOVERHARNESS", "ARTIFICE", new AspectList().add(Aspects.FLIGHT, 6).add(Aspects.TRAVEL, 6).add(Aspects.AIR, 6).add(Aspects.MECHANISM, 3), 1, 7, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.HOVER_HARNESS), List.of(Research.Mark.CONCEALED),
                List.of("BOOTSTRAVELLER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.HOVERHARNESS.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("HoverHarness")),
                        new Page.Text("tc.research_page.HOVERHARNESS.2")), 0, null));
        add(new Research("HOVERGIRDLE", "ARTIFICE", new AspectList().add(Aspects.FLIGHT, 6).add(Aspects.TRAVEL, 3).add(Aspects.AIR, 3).add(Aspects.MOTION, 6), 2, 7, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.HOVER_GIRDLE), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of("HOVERHARNESS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.HOVERGIRDLE.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("HoverGirdle"))), 0, null));
        add(new Research("MIRROR", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 6).add(Aspects.ELDRITCH, 3).add(Aspects.DARKNESS, 3).add(Aspects.CRYSTAL, 3), -1, 8, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.MIRROR.asItem()), List.of(Research.Mark.HIDDEN),
                List.of("INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.MIRROR.1"),
                        new Page.Text("tc.research_page.MIRROR.2"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("Mirror")),
                        new Page.Text("tc.research_page.MIRROR.3")), 0, null));
        add(new Research("MIRRORHAND", "ARTIFICE", new AspectList().add(Aspects.TOOL, 6).add(Aspects.ELDRITCH, 3).add(Aspects.CRYSTAL, 3).add(Aspects.TRAVEL, 3), 1, 9, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.HAND_MIRROR), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("MIRROR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.MIRRORHAND.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("MirrorHand"))), 0, null));
        add(new Research("MIRRORESSENTIA", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 6).add(Aspects.ELDRITCH, 3).add(Aspects.WATER, 3).add(Aspects.MAGIC, 3), -1, 10, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ESSENTIA_MIRROR.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("MIRROR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.MIRRORESSENTIA.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("MirrorEssentia")),
                        new Page.Text("tc.research_page.MIRRORESSENTIA.2")), 0, null));
        add(new Research("ARCANEBORE", "ARTIFICE", new AspectList().add(Aspects.MINE, 6).add(Aspects.MOTION, 3).add(Aspects.MECHANISM, 3).add(Aspects.TOOL, 3), -3, 8, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_BORE.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("FOCUSEXCAVATION", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARCANEBORE.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ArcaneBore")),
                        new Page.Text("tc.research_page.ARCANEBORE.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcaneBoreBase")),
                        new Page.Text("tc.research_page.ARCANEBORE.3")), 0, null));
        add(new Research("ARCANELAMP", "ARTIFICE", new AspectList().add(Aspects.LIGHT, 3).add(Aspects.SENSES, 3).add(Aspects.DARKNESS, 3), -3, 1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_LAMP.asItem()), List.of(Research.Mark.SECONDARY),
                List.of("NITOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARCANELAMP.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcaneLamp")),
                        new Page.Concealed("ARCANEBORE", "tc.research_page.ARCANELAMP.2")), 0, null));
        add(new Research("LAMPGROWTH", "ARTIFICE", new AspectList().add(Aspects.LIGHT, 3).add(Aspects.PLANT, 6).add(Aspects.LIFE, 3).add(Aspects.CROP, 3), -4, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.GROWTH_LAMP.asItem()), List.of(Research.Mark.HIDDEN),
                List.of("ARCANELAMP", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.LAMPGROWTH.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("LampGrowth"))), 0, null));
        add(new Research("LAMPFERTILITY", "ARTIFICE", new AspectList().add(Aspects.BEAST, 6).add(Aspects.LIFE, 6).add(Aspects.LIGHT, 3), -2, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.FERTILITY_LAMP.asItem()), List.of(Research.Mark.HIDDEN),
                List.of("ARCANELAMP", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.LAMPFERTILITY.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("LampFertility"))), 0, null));
        add(new Research("BONEBOW", "ARTIFICE", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.AIR, 3).add(Aspects.MOTION, 3), -7, 1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.BONE_BOW), List.of(Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BONEBOW.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("BoneBow"))), 0, null));
        add(new Research("PRIMALARROW", "ARTIFICE", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.AIR, 3).add(Aspects.FIRE, 3).add(Aspects.WATER, 3).add(Aspects.EARTH, 3).add(Aspects.ORDER, 3).add(Aspects.ENTROPY, 3), -9, 0, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_ARROWS.get("air")), List.of(Research.Mark.CONCEALED),
                List.of("BONEBOW"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PRIMALARROW.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("PrimalArrow_0", "PrimalArrow_1", "PrimalArrow_2", "PrimalArrow_3", "PrimalArrow_4", "PrimalArrow_5")),
                        new Page.Text("tc.research_page.PRIMALARROW.2"),
                        new Page.Text("tc.research_page.PRIMALARROW.3")), 0, null));
        add(new Research("ELEMENTALAXE", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.WATER, 3).add(Aspects.MOTION, 3), -7, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_AXE), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELEMENTALAXE.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ElementalAxe")),
                        new Page.Text("tc.research_page.ELEMENTALAXE.2")), 0, null));
        add(new Research("ELEMENTALPICK", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.FIRE, 3).add(Aspects.SENSES, 3), -7, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_PICKAXE), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELEMENTALPICK.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ElementalPick")),
                        new Page.Text("tc.research_page.ELEMENTALPICK.2")), 0, null));
        add(new Research("ELEMENTALSWORD", "ARTIFICE", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.AIR, 3).add(Aspects.ENERGY, 3), -7, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_SWORD), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELEMENTALSWORD.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ElementalSword"))), 0, null));
        add(new Research("ELEMENTALSHOVEL", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EARTH, 3).add(Aspects.CRAFT, 3), -7, 6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_SHOVEL), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELEMENTALSHOVEL.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ElementalShovel")),
                        new Page.Text("tc.research_page.ELEMENTALSHOVEL.2")), 0, null));
        add(new Research("ELEMENTALHOE", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.LIFE, 3).add(Aspects.CROP, 3), -7, 7, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ELEMENTAL_HOE), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELEMENTALHOE.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ElementalHoe"))), 0, null));
        add(new Research("WARDEDARCANA", "ARTIFICE", new AspectList().add(Aspects.TOOL, 6).add(Aspects.MIND, 3).add(Aspects.MECHANISM, 3).add(Aspects.ARMOR, 3), -5, -4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ARCANE_DOOR), List.of(),
                List.of("THAUMIUM"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.WARDEDARCANA.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcaneDoor")),
                        new Page.Text("tc.research_page.WARDEDARCANA.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("IronKey")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("GoldKey")),
                        new Page.Text("tc.research_page.WARDEDARCANA.3"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcanePressurePlate")),
                        new Page.Text("tc.research_page.WARDEDARCANA.4"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WardedGlass"))), 0, null));
        add(new Research("JARBRAIN", "ARTIFICE", new AspectList().add(Aspects.HUNGER, 3).add(Aspects.MIND, 3).add(Aspects.UNDEAD, 3).add(Aspects.GREED, 3), -5, 9, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.BRAIN_JAR.asItem()), List.of(Research.Mark.HIDDEN),
                List.of("INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.JARBRAIN.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("JarBrain"))), 3, null));
        add(new Research("INFUSIONENCHANTMENT", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 6).add(Aspects.MIND, 3).add(Aspects.WEAPON, 3).add(Aspects.ARMOR, 3).add(Aspects.TOOL, 3), -6, 11, 3,
                "textures/misc/r_enchant.png", null, List.of(Research.Mark.CONCEALED),
                List.of("JARBRAIN"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.INFUSIONENCHANTMENT.1"),
                        new Page.Text("tc.research_page.INFUSIONENCHANTMENT.2"),
                        new Page.Text("tc.research_page.INFUSIONENCHANTMENT.3"),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnchRepair")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnchHaste")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch0")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch1")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch2")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch3")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch4")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch5")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch6")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch7")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch8")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch9")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch10")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch11")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch12")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch13")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch14")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch15")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch16")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch17")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch18")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch19")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch20")),
                        new Page.Recipe(Page.Kind.ENCHANTMENT, List.of("InfEnch21"))), 0, null));
        add(new Research("ARMORFORTRESS", "ARTIFICE", new AspectList().add(Aspects.METAL, 3).add(Aspects.ARMOR, 5).add(Aspects.CRAFT, 5), -8, 9, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FORTRESS_HELMET), List.of(Research.Mark.HIDDEN),
                List.of("THAUMIUM", "INFUSIONENCHANTMENT"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARMORFORTRESS.1"),
                        new Page.Text("tc.research_page.ARMORFORTRESS.2"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ThaumiumFortressHelm")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ThaumiumFortressChest")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("ThaumiumFortressLegs"))), 0, null));
        add(new Research("HELMGOGGLES", "ARTIFICE", new AspectList().add(Aspects.SENSES, 5).add(Aspects.AURA, 3).add(Aspects.ARMOR, 3), -9, 7, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOGGLES), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of("GOGGLES"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.HELMGOGGLES.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("HelmGoggles"))), 0, null));
        add(new Research("MASKGRINNINGDEVIL", "ARTIFICE", new AspectList().add(Aspects.HEAL, 5).add(Aspects.MIND, 5).add(Aspects.ARMOR, 3), -10, 8, 2,
                "textures/misc/r_mask0.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.MASKGRINNINGDEVIL.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("MaskGrinningDevil"))), 0, null));
        add(new Research("MASKANGRYGHOST", "ARTIFICE", new AspectList().add(Aspects.ENTROPY, 5).add(Aspects.DEATH, 5).add(Aspects.ARMOR, 3), -10, 9, 2,
                "textures/misc/r_mask1.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.MASKANGRYGHOST.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("MaskAngryGhost"))), 1, null));
        add(new Research("MASKSIPPINGFIEND", "ARTIFICE", new AspectList().add(Aspects.UNDEAD, 5).add(Aspects.LIFE, 5).add(Aspects.ARMOR, 3), -10, 10, 2,
                "textures/misc/r_mask2.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.MASKSIPPINGFIEND.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("MaskSippingFiend"))), 1, null));
    }

    private static void alchemy() {
        add(new Research("PHIAL", "ALCHEMY", new AspectList(), 0, -2, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.PHIAL), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PHIAL.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Phial"))), 0, null));
        add(new Research("CRUCIBLE", "ALCHEMY", new AspectList(), 0, 0, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.CRUCIBLE.asItem()), List.of(Research.Mark.STUB, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CRUCIBLE.1"),
                        new Page.Text("tc.research_page.CRUCIBLE.2"),
                        new Page.Text("tc.research_page.CRUCIBLE.3"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("Crucible")),
                        new Page.Text("tc.research_page.CRUCIBLE.4"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("BalancedShard_0", "BalancedShard_1", "BalancedShard_2", "BalancedShard_3", "BalancedShard_4", "BalancedShard_5")),
                        new Page.Text("tc.research_page.CRUCIBLE.5"),
                        new Page.Smelting(() -> new net.minecraft.world.item.ItemStack(TCItems.SHARD_BALANCED), () -> new net.minecraft.world.item.ItemStack(TCResources.get("salis_mundus")))), 0, null));
        add(new Research("NITOR", "ALCHEMY", new AspectList().add(Aspects.LIGHT, 3).add(Aspects.FIRE, 1), 2, -1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.NITOR), List.of(),
                List.of("CRUCIBLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NITOR.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("Nitor"))), 0, null));
        add(new Research("ALUMENTUM", "ALCHEMY", new AspectList().add(Aspects.ENERGY, 3).add(Aspects.FIRE, 1), 2, 1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ALUMENTUM), List.of(),
                List.of("CRUCIBLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ALUMENTUM.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("Alumentum"))), 0, null));
        add(new Research("ALCHEMICALDUPLICATION", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.GREED, 3).add(Aspects.CRAFT, 3), -4, 0, 1,
                "textures/misc/r_alchmult.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TALLOW"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ALCHEMICALDUPLICATION.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltGunpowder")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltSlime")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltClay")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltGlowstone")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltInk"))), 0, null));
        add(new Research("ALCHEMICALMANUFACTURE", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.CRAFT, 3), -5, -2, 1,
                "textures/misc/r_alchman.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ALCHEMICALDUPLICATION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ALCHEMICALMANUFACTURE.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltWeb")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltMossyCobble")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltIce"))), 0, null));
        add(new Research("ENTROPICPROCESSING", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 1).add(Aspects.ENTROPY, 3).add(Aspects.CRAFT, 1), -6, 1, 1,
                "textures/misc/r_alchent.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ALCHEMICALDUPLICATION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ENTROPICPROCESSING.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltCrackedBrick")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("AltBonemeal"))), 0, null));
        add(new Research("LIQUIDDEATH", "ALCHEMY", new AspectList().add(Aspects.DEATH, 3).add(Aspects.POISON, 3).add(Aspects.ENTROPY, 1).add(Aspects.WATER, 1), -7, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.BUCKET_DEATH), List.of(Research.Mark.HIDDEN),
                List.of("ENTROPICPROCESSING"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.LIQUIDDEATH.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("LiquidDeath"))), 3, null));
        add(new Research("BOTTLETAINT", "ALCHEMY", new AspectList().add(Aspects.TAINT, 5).add(Aspects.MAGIC, 3).add(Aspects.ENTROPY, 1).add(Aspects.WATER, 1), -8, 1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.BOTTLE_TAINT), List.of(Research.Mark.HIDDEN),
                List.of("ENTROPICPROCESSING"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BOTTLETAINT.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("BottleTaint"))), 2, null));
        add(new Research("THAUMIUM", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.MAGIC, 3), -1, 3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("thaumium_ingot")), List.of(Research.Mark.HIDDEN),
                List.of("CRUCIBLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.THAUMIUM.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("Thaumium")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumAxe")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumSword")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumPick")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumShovel")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumHoe")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumHelm")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumChest")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumLegs")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("ThaumiumBoots"))), 0, null));
        add(new Research("PUREIRON", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 3), -2, 5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_iron_cluster")), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PUREIRON.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("PureIron"))), 0, null));
        add(new Research("PUREGOLD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.GREED, 1), -4, 3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_gold_cluster")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PUREGOLD.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("PureGold"))), 0, null));
        add(new Research("PURECOPPER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.EXCHANGE, 1), -4, 5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_copper_cluster")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PURECOPPER.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("PureCopper"))), 0, null));
        add(new Research("PURETIN", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.CRYSTAL, 1), -4, 7, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_tin_cluster")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PURETIN.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("PureTin"))), 0, "c:ingots/tin"));
        add(new Research("PURESILVER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.GREED, 1), -3, 8, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_silver_cluster")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PURESILVER.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("PureSilver"))), 0, "c:ingots/silver"));
        add(new Research("PURELEAD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 3), -2, 9, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("native_lead_cluster")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PURELEAD.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("PureLead"))), 0, "c:ingots/lead"));
        add(new Research("TRANSIRON", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 0, 5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_NUGGET), List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRANSIRON.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("TransIron"))), 0, null));
        add(new Research("TRANSGOLD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 2, 3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRANSGOLD.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("TransGold"))), 0, null));
        add(new Research("TRANSCOPPER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 2, 5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COPPER_NUGGET), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRANSCOPPER.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("TransCopper"))), 0, null));
        add(new Research("TRANSTIN", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 2).add(Aspects.CRYSTAL, 1), 2, 7, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("tin_nugget")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRANSTIN.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("TransTin"))), 0, "c:ingots/tin"));
        add(new Research("TRANSSILVER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 2).add(Aspects.GREED, 1), 1, 8, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("silver_nugget")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRANSSILVER.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("TransSilver"))), 0, "c:ingots/silver"));
        add(new Research("TRANSLEAD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 2).add(Aspects.ORDER, 1), 0, 9, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("lead_nugget")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRANSLEAD.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("TransLead"))), 0, "c:ingots/lead"));
        add(new Research("TALLOW", "ALCHEMY", new AspectList().add(Aspects.FLESH, 3).add(Aspects.MAGIC, 1), -2, 0, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("magic_tallow")), List.of(),
                List.of("CRUCIBLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TALLOW.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("Tallow")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("TallowCandle"))), 0, null));
        add(new Research("ETHEREALBLOOM", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 1).add(Aspects.PLANT, 6).add(Aspects.HEAL, 3).add(Aspects.TAINT, 6), -2, -3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ETHEREAL_BLOOM.asItem()), List.of(Research.Mark.CONCEALED, Research.Mark.HIDDEN),
                List.of("CRUCIBLE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ETHEREALBLOOM.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("EtherealBloom")),
                        new Page.Text("tc.research_page.ETHEREALBLOOM.2")), 0, null));
        add(new Research("BATHSALTS", "ALCHEMY", new AspectList().add(Aspects.MIND, 3).add(Aspects.AURA, 3).add(Aspects.ORDER, 3).add(Aspects.HEAL, 3), -4, -4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.BATH_SALTS), List.of(Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.BATHSALTS.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("BathSalts"))), 0, null));
        add(new Research("SANESOAP", "ALCHEMY", new AspectList().add(Aspects.MIND, 5).add(Aspects.ORDER, 5).add(Aspects.HEAL, 5).add(Aspects.ELDRITCH, 5), -3, -6, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.SANITY_SOAP), List.of(),
                List.of("BATHSALTS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.SANESOAP.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("SaneSoap"))), 0, null));
        add(new Research("ARCANESPA", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MECHANISM, 3).add(Aspects.ORDER, 3), -6, -5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ARCANE_SPA.asItem()), List.of(Research.Mark.SECONDARY),
                List.of("BATHSALTS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARCANESPA.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("ArcaneSpa"))), 0, null));
        add(new Research("DISTILESSENTIA", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.WATER, 3).add(Aspects.SLIME, 3), 5, -1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ALEMBIC.asItem()), List.of(),
                List.of("NITOR", "ALUMENTUM"), List.of(), List.of("JARLABEL"),
                List.of(
                        new Page.Text("tc.research_page.DISTILESSENTIA.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("AlchemyFurnace")),
                        new Page.Text("tc.research_page.DISTILESSENTIA.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Filter")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Alembic")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("AlchemicalConstruct"))), 0, null));
        add(new Research("JARLABEL", "ALCHEMY", new AspectList(), 4, -3, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.JAR.asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND),
                List.of("DISTILESSENTIA"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.JARLABEL.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WardedJar")),
                        new Page.Text("tc.research_page.JARLABEL.2"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("JarLabel")),
                        new Page.Text("tc.research_page.JARLABEL.3"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("JarLabel*")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("JarLabelNull"))), 0, null));
        add(new Research("JARVOID", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.ENTROPY, 3).add(Aspects.VOID, 6), 5, -5, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.JAR_VOID.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("JARLABEL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.JARVOID.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("JarVoid"))), 0, null));
        add(new Research("TUBES", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3), 7, 0, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("DISTILESSENTIA"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TUBES.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Tube")),
                        new Page.Text("tc.research_page.TUBES.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TubeValve")),
                        new Page.Text("tc.research_page.TUBES.3"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Resonator")),
                        new Page.Text("tc.research_page.TUBES.4")), 0, null));
        add(new Research("TUBEFILTER", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.ORDER, 3), 9, 1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.TUBE_FILTER.asItem()), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TUBES"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TUBEFILTER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TubeFilter")),
                        new Page.Text("tc.research_page.TUBEFILTER.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TubeRestrict")),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TubeOneway"))), 0, null));
        add(new Research("ESSENTIACRYSTAL", "ALCHEMY", new AspectList().add(Aspects.WATER, 5).add(Aspects.CRYSTAL, 5).add(Aspects.EXCHANGE, 3).add(Aspects.MAGIC, 5), 8, -2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ESSENTIA_CRYSTALIZER.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("TUBES"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ESSENTIACRYSTAL.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("EssentiaCrystalizer"))), 0, null));
        add(new Research("CENTRIFUGE", "ALCHEMY", new AspectList().add(Aspects.ENTROPY, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.CRAFT, 3), 10, 0, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.CENTRIFUGE.asItem()), List.of(Research.Mark.CONCEALED),
                List.of("TUBEFILTER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CENTRIFUGE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Centrifuge")),
                        new Page.Text("tc.research_page.CENTRIFUGE.2"),
                        new Page.Text("tc.research_page.CENTRIFUGE.3"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TubeBuffer"))), 0, null));
        add(new Research("THAUMATORIUM", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 6).add(Aspects.EXCHANGE, 3).add(Aspects.CRAFT, 3), 10, -2, 3,
                "textures/block/alchemyblock.png", null, List.of(Research.Mark.CONCEALED),
                List.of("CENTRIFUGE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.THAUMATORIUM.1"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("Thaumatorium")),
                        new Page.Text("tc.research_page.THAUMATORIUM.2"),
                        new Page.Text("tc.research_page.THAUMATORIUM.3"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("MnemonicMatrix"))), 0, null));
    }

    private static void golemancy() {
        add(new Research("HUNGRYCHEST", "GOLEMANCY", new AspectList().add(Aspects.HUNGER, 3).add(Aspects.VOID, 3), -1, 0, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.HUNGRY_CHEST.asItem()), List.of(Research.Mark.SECONDARY),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.HUNGRYCHEST.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("HungryChest"))), 0, null));
        add(new Research("GOLEMFETTER", "GOLEMANCY", new AspectList().add(Aspects.TRAP, 3).add(Aspects.MECHANISM, 3), 4, 8, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_FETTER), List.of(Research.Mark.SECONDARY),
                List.of("GOLEMSTONE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMFETTER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("GolemFetter"))), 0, null));
        add(new Research("TRAVELTRUNK", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 3).add(Aspects.TRAVEL, 3).add(Aspects.TREE, 3).add(Aspects.VOID, 3), 0, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.TRUNK_SPAWNER), List.of(Research.Mark.CONCEALED),
                List.of("INFUSION", "GOLEMWOOD"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TRAVELTRUNK.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("TravelTrunk")),
                        new Page.Text("tc.research_page.TRAVELTRUNK.2"),
                        new Page.Concealed("UPGRADEAIR", "tc.research_page.TRAVELTRUNK.UAI"),
                        new Page.Concealed("UPGRADEEARTH", "tc.research_page.TRAVELTRUNK.UEA"),
                        new Page.Concealed("UPGRADEFIRE", "tc.research_page.TRAVELTRUNK.UFI"),
                        new Page.Concealed("UPGRADEWATER", "tc.research_page.TRAVELTRUNK.UWA"),
                        new Page.Concealed("UPGRADEORDER", "tc.research_page.TRAVELTRUNK.UOR"),
                        new Page.Concealed("UPGRADEENTROPY", "tc.research_page.TRAVELTRUNK.UEN")), 0, null));
        add(new Research("GOLEMSTRAW", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 3).add(Aspects.MOTION, 3).add(Aspects.CROP, 3).add(Aspects.EXCHANGE, 3), 0, 2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("straw")), List.of(),
                List.of("HUNGRYCHEST"), List.of(), List.of("COREGATHER", "GOLEMBELL"),
                List.of(
                        new Page.Text("tc.research_page.GOLEMSTRAW.1"),
                        new Page.Text("tc.research_page.GOLEMSTRAW.2"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemStraw")),
                        new Page.Text("tc.research_page.GOLEMSTRAW.3")), 0, null));
        add(new Research("GOLEMWOOD", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 4).add(Aspects.MOTION, 4).add(Aspects.TREE, 3).add(Aspects.EXCHANGE, 3), 2, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("wood")), List.of(Research.Mark.SECONDARY),
                List.of("GOLEMSTRAW"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMWOOD.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemWood"))), 0, null));
        add(new Research("GOLEMTALLOW", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 3).add(Aspects.MOTION, 3).add(Aspects.FLESH, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3), 4, 6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("tallow")), List.of(Research.Mark.CONCEALED),
                List.of("GOLEMCLAY", "TALLOW"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMTALLOW.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("BlockTallow")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemTallow"))), 0, null));
        add(new Research("GOLEMCLAY", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 6).add(Aspects.MOTION, 6).add(Aspects.EARTH, 3).add(Aspects.EXCHANGE, 3), 2, 6, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("clay")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMWOOD"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMCLAY.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemClay"))), 0, null));
        add(new Research("GOLEMFLESH", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 7).add(Aspects.MOTION, 7).add(Aspects.FLESH, 6).add(Aspects.EXCHANGE, 3), 4, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("flesh")), List.of(Research.Mark.CONCEALED),
                List.of("GOLEMWOOD"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMFLESH.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("BlockFlesh")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemFlesh"))), 3, null));
        add(new Research("GOLEMSTONE", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 6).add(Aspects.MOTION, 6).add(Aspects.EARTH, 3).add(Aspects.EXCHANGE, 3), 2, 8, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("stone")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMCLAY"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMSTONE.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemStone"))), 0, null));
        add(new Research("GOLEMIRON", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 9).add(Aspects.MOTION, 9).add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 0, 10, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("iron")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMSTONE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMIRON.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemIron"))), 0, null));
        add(new Research("GOLEMTHAUMIUM", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 10).add(Aspects.MOTION, 10).add(Aspects.METAL, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3), 2, 10, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_PLACERS.get("thaumium")), List.of(Research.Mark.CONCEALED),
                List.of("GOLEMIRON", "THAUMIUM"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMTHAUMIUM.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("BlockThaumium")),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("GolemThaumium"))), 0, null));
        add(new Research("GOLEMBELL", "GOLEMANCY", new AspectList(), 3, 0, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_BELL), List.of(Research.Mark.STUB),
                List.of("GOLEMSTRAW"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.GOLEMBELL.1"),
                        new Page.Text("tc.research_page.GOLEMBELL.2"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("GolemBell"))), 0, null));
        add(new Research("COREGATHER", "GOLEMANCY", new AspectList(), -3, 3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("gather")), List.of(Research.Mark.STUB, Research.Mark.CONCEALED),
                List.of("GOLEMSTRAW"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREGATHER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("CoreBlank")),
                        new Page.Text("tc.research_page.COREGATHER.2"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreGather"))), 0, null));
        add(new Research("COREFILL", "GOLEMANCY", new AspectList().add(Aspects.HUNGER, 3).add(Aspects.EXCHANGE, 3).add(Aspects.VOID, 3), -5, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("fill")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREFILL.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreFill"))), 0, null));
        add(new Research("COREEMPTY", "GOLEMANCY", new AspectList().add(Aspects.VOID, 3).add(Aspects.EXCHANGE, 3).add(Aspects.GREED, 3), -5, 1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("empty")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREEMPTY.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreEmpty"))), 0, null));
        add(new Research("CORESORTING", "GOLEMANCY", new AspectList().add(Aspects.VOID, 3).add(Aspects.EXCHANGE, 3).add(Aspects.GREED, 3).add(Aspects.HUNGER, 3), -7, 2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("sorting")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREEMPTY", "COREFILL", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CORESORTING.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("CoreSorting"))), 0, null));
        add(new Research("COREUSE", "GOLEMANCY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MECHANISM, 3).add(Aspects.MAN, 3), -7, 0, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("use")), List.of(Research.Mark.CONCEALED),
                List.of("COREEMPTY", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREUSE.1"),
                        new Page.Text("tc.research_page.COREUSE.2"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("CoreUse")),
                        new Page.Concealed("UPGRADEAIR", "tc.research_page.COREUSE.3")), 0, null));
        add(new Research("COREHARVEST", "GOLEMANCY", new AspectList().add(Aspects.HARVEST, 6).add(Aspects.CROP, 3).add(Aspects.TRAVEL, 3), -2, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("harvest")), List.of(Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREHARVEST.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreHarvest")),
                        new Page.Concealed("UPGRADEORDER", "tc.research_page.COREHARVEST.2")), 0, null));
        add(new Research("COREFISHING", "GOLEMANCY", new AspectList().add(Aspects.WATER, 3).add(Aspects.HARVEST, 3).add(Aspects.BEAST, 3).add(Aspects.HUNGER, 3), -2, 7, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("fishing")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREHARVEST", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREFISHING.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("CoreFishing")),
                        new Page.Concealed("UPGRADEAIR", "tc.research_page.COREFISHING.2"),
                        new Page.Concealed("UPGRADEFIRE", "tc.research_page.COREFISHING.3"),
                        new Page.Concealed("UPGRADEORDER", "tc.research_page.COREFISHING.4"),
                        new Page.Concealed("UPGRADEENTROPY", "tc.research_page.COREFISHING.5")), 0, null));
        add(new Research("CORELUMBER", "GOLEMANCY", new AspectList().add(Aspects.TREE, 6).add(Aspects.HARVEST, 3).add(Aspects.TOOL, 3).add(Aspects.ENERGY, 3), -1, 7, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("chop")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREHARVEST", "ELEMENTALAXE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CORELUMBER.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("CoreLumber"))), 0, null));
        add(new Research("COREGUARD", "GOLEMANCY", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.TRAP, 3).add(Aspects.SENSES, 3), -4, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("guard")), List.of(Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREGUARD.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreGuard")),
                        new Page.Concealed("UPGRADEORDER", "tc.research_page.COREGUARD.2")), 0, null));
        add(new Research("COREBUTCHER", "GOLEMANCY", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.BEAST, 3).add(Aspects.SENSES, 3).add(Aspects.HARVEST, 3), -3, 7, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("butcher")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREGUARD", "COREHARVEST"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREBUTCHER.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreButcher"))), 1, null));
        add(new Research("CORELIQUID", "GOLEMANCY", new AspectList().add(Aspects.WATER, 3).add(Aspects.EXCHANGE, 3).add(Aspects.TRAVEL, 3), -7, 4, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("decanting")), List.of(Research.Mark.CONCEALED),
                List.of("COREFILL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CORELIQUID.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("CoreLiquid")),
                        new Page.Concealed("UPGRADEENTROPY", "tc.research_page.CORELIQUID.2")), 0, null));
        add(new Research("COREALCHEMY", "GOLEMANCY", new AspectList().add(Aspects.WATER, 3).add(Aspects.TRAVEL, 3).add(Aspects.MAGIC, 3).add(Aspects.ENERGY, 3), -9, 3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_CORES.get("alchemy")), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("CORELIQUID", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.COREALCHEMY.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("CoreAlchemy")),
                        new Page.Text("tc.research_page.COREALCHEMY.2")), 0, null));
        add(new Research("UPGRADEAIR", "GOLEMANCY", new AspectList().add(Aspects.AIR, 6).add(Aspects.MOTION, 3), 7, -3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(0)), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.UPGRADEAIR.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("UpgradeAir"))), 0, null));
        add(new Research("UPGRADEEARTH", "GOLEMANCY", new AspectList().add(Aspects.EARTH, 6).add(Aspects.LIFE, 3), 6, -2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(1)), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.UPGRADEEARTH.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("UpgradeEarth"))), 0, null));
        add(new Research("UPGRADEFIRE", "GOLEMANCY", new AspectList().add(Aspects.FIRE, 6).add(Aspects.ENERGY, 3), 5, -1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(2)), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.UPGRADEFIRE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("UpgradeFire"))), 0, null));
        add(new Research("UPGRADEWATER", "GOLEMANCY", new AspectList().add(Aspects.WATER, 6).add(Aspects.SENSES, 3), 5, 1, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(3)), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.UPGRADEWATER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("UpgradeWater"))), 0, null));
        add(new Research("UPGRADEORDER", "GOLEMANCY", new AspectList().add(Aspects.ORDER, 6).add(Aspects.MIND, 3), 6, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(4)), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.UPGRADEORDER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("UpgradeOrder"))), 0, null));
        add(new Research("UPGRADEENTROPY", "GOLEMANCY", new AspectList().add(Aspects.ENTROPY, 6).add(Aspects.MIND, 3), 7, 3, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_UPGRADES.get(5)), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.UPGRADEENTROPY.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("UpgradeEntropy"))), 0, null));
        add(new Research("ADVANCEDGOLEM", "GOLEMANCY", new AspectList().add(Aspects.LIFE, 3).add(Aspects.ENERGY, 3).add(Aspects.MIND, 6).add(Aspects.SENSES, 3), 8, 0, 2,
                null, () -> BookStacks.advancedGolem(), List.of(Research.Mark.CONCEALED),
                List.of("INFUSION", "UPGRADEAIR", "UPGRADEEARTH", "UPGRADEFIRE", "UPGRADEWATER", "UPGRADEORDER", "UPGRADEENTROPY"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ADVANCEDGOLEM.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("AdvancedGolem"))), 5, null));
        add(new Research("TINYHAT", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.LIFE, 1).add(Aspects.GREED, 1), 5, 10, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(0)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYHAT.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyHat"))), 0, null));
        add(new Research("TINYGLASSES", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.SENSES, 1).add(Aspects.GREED, 1), 6, 10, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(1)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYGLASSES.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyGlasses"))), 0, null));
        add(new Research("TINYBOWTIE", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.TRAVEL, 1).add(Aspects.GREED, 1), 7, 10, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(2)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYBOWTIE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyBowtie"))), 0, null));
        add(new Research("TINYFEZ", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.ENERGY, 1).add(Aspects.GREED, 1), 8, 10, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(3)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYFEZ.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyFez"))), 0, null));
        add(new Research("TINYDART", "GOLEMANCY", new AspectList().add(Aspects.FLIGHT, 1).add(Aspects.WEAPON, 2).add(Aspects.GREED, 1), 5, 11, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(4)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYDART.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyDart"))), 0, null));
        add(new Research("TINYVISOR", "GOLEMANCY", new AspectList().add(Aspects.SENSES, 1).add(Aspects.ARMOR, 2).add(Aspects.GREED, 1), 6, 11, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(5)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYVISOR.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyVisor"))), 0, null));
        add(new Research("TINYARMOR", "GOLEMANCY", new AspectList().add(Aspects.METAL, 1).add(Aspects.ARMOR, 2).add(Aspects.GREED, 1), 7, 11, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(6)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYARMOR.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyArmor"))), 0, null));
        add(new Research("TINYHAMMER", "GOLEMANCY", new AspectList().add(Aspects.METAL, 1).add(Aspects.WEAPON, 2).add(Aspects.GREED, 1), 8, 11, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.GOLEM_DECORATIONS.get(7)), List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.TINYHAMMER.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("TinyHammer"))), 0, null));
    }

    private static void basics() {
        add(new Research("ASPECTS", "BASICS", new AspectList(), 0, 0, 0,
                "textures/misc/r_aspects.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ASPECTS.1"),
                        new Page.Text("tc.research_page.ASPECTS.2"),
                        new Page.Text("tc.research_page.ASPECTS.3")), 0, null));
        add(new Research("PECH", "BASICS", new AspectList(), -4, -4, 0,
                "textures/misc/r_pech.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PECH.1"),
                        new Page.Text("tc.research_page.PECH.2")), 0, null));
        add(new Research("NODES", "BASICS", new AspectList(), -2, 0, 0,
                "textures/misc/r_nodes.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODES.1"),
                        new Page.Text("tc.research_page.NODES.2"),
                        new Page.Text("tc.research_page.NODES.3")), 0, null));
        add(new Research("WARP", "BASICS", new AspectList(), 0, 2, 0,
                "textures/misc/r_warp.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.WARP.1"),
                        new Page.Text("tc.research_page.WARP.2"),
                        new Page.Text("tc.research_page.WARP.3")), 0, null));
        add(new Research("RESEARCH", "BASICS", new AspectList(), 2, 0, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.SCRIBING_TOOLS), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RESEARCH.1"),
                        new Page.Text("tc.research_page.RESEARCH.2"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Thaumometer")),
                        new Page.Text("tc.research_page.RESEARCH.3"),
                        new Page.Text("tc.research_page.RESEARCH.4"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Scribe1")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Scribe2")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Scribe3")),
                        new Page.Text("tc.research_page.RESEARCH.5"),
                        new Page.Text("tc.research_page.RESEARCH.6"),
                        new Page.Text("tc.research_page.RESEARCH.7"),
                        new Page.Text("tc.research_page.RESEARCH.8"),
                        new Page.Text("tc.research_page.RESEARCH.9"),
                        new Page.Text("tc.research_page.RESEARCH.10"),
                        new Page.Text("tc.research_page.RESEARCH.11"),
                        new Page.Text("tc.research_page.RESEARCH.12")), 0, null));
        add(new Research("KNOWFRAG", "BASICS", new AspectList(), 3, -2, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("knowledge_fragment")), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("RESEARCH"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.KNOWFRAG.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("KnowFrag"))), 0, null));
        add(new Research("THAUMONOMICON", "BASICS", new AspectList(), 1, -2, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.THAUMONOMICON), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("RESEARCH"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.THAUMONOMICON.1"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("Thaumonomicon"))), 0, null));
        add(new Research("ORE", "BASICS", new AspectList(), -2, -2, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.CINNABAR_ORE.asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ORE.1"),
                        new Page.Text("tc.research_page.ORE.2"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("Clusters0", "Clusters1", "Clusters2", "Clusters3", "Clusters4", "Clusters5", "Clusters6")),
                        new Page.Text("tc.research_page.ORE.3"),
                        new Page.Text("tc.research_page.ORE.4")), 0, null));
        add(new Research("PLANTS", "BASICS", new AspectList(), -2, -4, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.GREATWOOD_SAPLING.asItem()), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PLANTS.1"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("PlankGreatwood")),
                        new Page.Text("tc.research_page.PLANTS.2"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("PlankSilverwood")),
                        new Page.Text("tc.research_page.PLANTS.3"),
                        new Page.Text("tc.research_page.PLANTS.4"),
                        new Page.Text("tc.research_page.PLANTS.5"),
                        new Page.Text("tc.research_page.PLANTS.6")), 0, null));
        add(new Research("ENCHANT", "BASICS", new AspectList(), -4, -2, 0,
                "textures/misc/r_enchant.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ENCHANT.1"),
                        new Page.Text("tc.research_page.ENCHANT.2")), 0, null));
        add(new Research("NODETAPPER1", "BASICS", new AspectList().add(Aspects.AURA, 3).add(Aspects.MAGIC, 3).add(Aspects.MOTION, 3).add(Aspects.EXCHANGE, 3), -4, 1, 2,
                "textures/misc/r_nodetap1.png", null, List.of(Research.Mark.ROUND),
                List.of("NODES"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODETAPPER1.1")), 0, null));
        add(new Research("NODEPRESERVE", "BASICS", new AspectList().add(Aspects.AURA, 3).add(Aspects.GREED, 3).add(Aspects.SENSES, 3), -6, 2, 2,
                "textures/misc/r_nodepreserve.png", null, List.of(Research.Mark.ROUND),
                List.of("NODETAPPER1"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODEPRESERVE")), 0, null));
        add(new Research("NODEJAR", "BASICS", new AspectList().add(Aspects.AURA, 6).add(Aspects.GREED, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MOTION, 3), -7, 4, 3,
                null, () -> BookStacks.nodeJar(), List.of(Research.Mark.CONCEALED),
                List.of("NODEPRESERVE"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODEJAR.1"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("NodeJar")),
                        new Page.Text("tc.research_page.NODEJAR.2")), 0, null));
        add(new Research("NODETAPPER2", "BASICS", new AspectList().add(Aspects.AURA, 6).add(Aspects.MAGIC, 3).add(Aspects.MOTION, 3).add(Aspects.EXCHANGE, 3), -3, 3, 2,
                "textures/misc/r_nodetap2.png", null, List.of(Research.Mark.ROUND, Research.Mark.SPECIAL),
                List.of("NODETAPPER1"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.NODETAPPER2.1")), 0, null));
        add(new Research("RESEARCHER1", "BASICS", new AspectList().add(Aspects.MIND, 3).add(Aspects.SENSES, 3).add(Aspects.ORDER, 3), 4, 1, 1,
                "textures/misc/r_researcher1.png", null, List.of(Research.Mark.ROUND),
                List.of("RESEARCH"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RESEARCHER1.1")), 0, null));
        add(new Research("DECONSTRUCTOR", "BASICS", new AspectList().add(Aspects.MIND, 3).add(Aspects.CRAFT, 3).add(Aspects.ENTROPY, 3), 6, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.DECONSTRUCTION_TABLE.asItem()), List.of(Research.Mark.ROUND),
                List.of("RESEARCHER1"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.DECONSTRUCTOR.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("Deconstructor")),
                        new Page.Text("tc.research_page.DECONSTRUCTOR.2")), 0, null));
        add(new Research("RESEARCHER2", "BASICS", new AspectList().add(Aspects.MIND, 6).add(Aspects.ORDER, 3).add(Aspects.SENSES, 3).add(Aspects.MAGIC, 3), 3, 3, 2,
                "textures/misc/r_researcher2.png", null, List.of(Research.Mark.ROUND, Research.Mark.SPECIAL),
                List.of("RESEARCHER1"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RESEARCHER2.1")), 1, null));
        add(new Research("RESEARCHDUPE", "BASICS", new AspectList().add(Aspects.MIND, 6).add(Aspects.EXCHANGE, 3).add(Aspects.SENSES, 3).add(Aspects.GREED, 3).add(Aspects.CRAFT, 3), 4, 5, 3,
                "textures/misc/r_resdupe.png", null, List.of(Research.Mark.ROUND),
                List.of("RESEARCHER2"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.RESEARCHDUPE.1")), 0, null));
        add(new Research("CRIMSON", "BASICS", new AspectList(), 0, 4, 0,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.CRIMSON_RITES), List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CRIMSON.1")), 3, null));
    }

    private static void eldritch() {
        add(new Research("ELDRITCHMINOR", "ELDRITCH", new AspectList(), 1, 0, 0,
                "textures/misc/r_eldritchminor.png", null, List.of(Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELDRITCHMINOR.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("VoidSeed"))), 0, null));
        add(new Research("ELDRITCHMAJOR", "ELDRITCH", new AspectList(), -1, 0, 0,
                "textures/misc/r_eldritchmajor.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ELDRITCHMAJOR.1"),
                        new Page.Text("tc.research_page.ELDRITCHMAJOR.2")), 0, null));
        add(new Research("OCULUS", "ELDRITCH", new AspectList().add(Aspects.MIND, 3).add(Aspects.DARKNESS, 3).add(Aspects.EXCHANGE, 3).add(Aspects.TRAVEL, 6).add(Aspects.ELDRITCH, 6), -2, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.ELDRITCH_EYE), List.of(Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.CONCEALED),
                List.of("CRIMSON", "ELDRITCHMAJOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.OCULUS.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("EldritchEye")),
                        new Page.Text("tc.research_page.OCULUS.2")), 6, null));
        add(new Research("ENTEROUTER", "ELDRITCH", new AspectList(), -3, 4, 1,
                "textures/misc/r_outer.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.HIDDEN),
                List.of("OCULUS"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ENTEROUTER.1")), 0, null));
        add(new Research("OUTERREV", "ELDRITCH", new AspectList().add(Aspects.ELDRITCH, 4).add(Aspects.MIND, 4), -5, 3, 1,
                "textures/misc/r_outerrev.png", null, List.of(Research.Mark.SPECIAL, Research.Mark.SECONDARY, Research.Mark.LOST),
                List.of("ENTEROUTER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.OUTERREV.1")), 0, null));
        add(new Research("PRIMPEARL", "ELDRITCH", new AspectList().add(Aspects.AIR, 8).add(Aspects.EARTH, 8).add(Aspects.FIRE, 8).add(Aspects.WATER, 8).add(Aspects.ORDER, 8).add(Aspects.ENTROPY, 8), 0, 4, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMORDIAL_PEARL), List.of(Research.Mark.SPECIAL, Research.Mark.SECONDARY, Research.Mark.LOST),
                List.of("ELDRITCHMINOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PRIMPEARL.1"),
                        new Page.Text("tc.research_page.PRIMPEARL.2")), 0, null));
        add(new Research("PRIMNODE", "ELDRITCH", new AspectList().add(Aspects.AURA, 1).add(Aspects.MAGIC, 1).add(Aspects.ORDER, 1).add(Aspects.ENTROPY, 1), 0, 6, 1,
                "textures/misc/r_nodes_2.png", null, List.of(Research.Mark.SECONDARY),
                List.of("PRIMPEARL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PRIMNODE.1")), 1, null));
        add(new Research("ADVALCHEMYFURNACE", "ELDRITCH", new AspectList().add(Aspects.AURA, 1).add(Aspects.MAGIC, 1).add(Aspects.ORDER, 1).add(Aspects.ENTROPY, 1), -2, 6, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.asItem()), List.of(Research.Mark.SECONDARY),
                List.of("PRIMPEARL", "DISTILESSENTIA", "VISPOWER"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ADVALCHEMYFURNACE.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("AdvAlchemyConstruct")),
                        new Page.Text("tc.research_page.ADVALCHEMYFURNACE.2"),
                        new Page.Recipe(Page.Kind.COMPOUND, List.of("AdvAlchemyFurnace"))), 0, null));
        add(new Research("PRIMALCRUSHER", "ELDRITCH", new AspectList().add(Aspects.MINE, 6).add(Aspects.TOOL, 6).add(Aspects.ENTROPY, 6).add(Aspects.VOID, 6).add(Aspects.WEAPON, 6).add(Aspects.ELDRITCH, 6).add(Aspects.GREED, 6), 2, 5, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.PRIMAL_CRUSHER), List.of(Research.Mark.CONCEALED),
                List.of("PRIMPEARL"), List.of("VOIDMETAL", "ELEMENTALPICK", "ELEMENTALSHOVEL"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.PRIMALCRUSHER.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("PrimalCrusher")),
                        new Page.Text("tc.research_page.PRIMALCRUSHER.2")), 0, null));
        add(new Research("SANITYCHECK", "ELDRITCH", new AspectList().add(Aspects.MIND, 5).add(Aspects.ELDRITCH, 3).add(Aspects.SENSES, 5), 2, 2, 1,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.SANITY_CHECKER), List.of(),
                List.of("ELDRITCHMINOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.SANITYCHECK.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("SanityCheck"))), 0, null));
        add(new Research("VOIDMETAL", "ELDRITCH", new AspectList().add(Aspects.METAL, 3).add(Aspects.ELDRITCH, 3).add(Aspects.DARKNESS, 3).add(Aspects.VOID, 5), 2, -2, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCResources.get("void_ingot")), List.of(),
                List.of("THAUMIUM", "ELDRITCHMINOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.VOIDMETAL.1"),
                        new Page.Recipe(Page.Kind.CRUCIBLE, List.of("VoidMetal")),
                        new Page.Text("tc.research_page.VOIDMETAL.2"),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidAxe")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidSword")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidPick")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidShovel")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidHoe")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidHelm")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidChest")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidLegs")),
                        new Page.Recipe(Page.Kind.CRAFTING, List.of("VoidBoots"))), 0, null));
        add(new Research("ESSENTIARESERVOIR", "ELDRITCH", new AspectList().add(Aspects.WATER, 5).add(Aspects.VOID, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MAGIC, 5).add(Aspects.VOID, 5), 4, -3, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCBlocks.ESSENTIA_RESERVOIR.asItem()), List.of(),
                List.of("VOIDMETAL", "CENTRIFUGE", "INFUSION"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ESSENTIARESERVOIR.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("EssentiaReservoir")),
                        new Page.Text("tc.research_page.ESSENTIARESERVOIR.2")), 0, null));
        add(new Research("CAP_void", "ELDRITCH", new AspectList().add(Aspects.VOID, 5).add(Aspects.ELDRITCH, 5).add(Aspects.TOOL, 3).add(Aspects.MAGIC, 3).add(Aspects.AURA, 3), 5, -1, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.WAND_CAPS.get("void")), List.of(Research.Mark.CONCEALED),
                List.of("CAP_thaumium", "VOIDMETAL"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.CAP_void.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("WandCapVoidInert")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandCapVoid"))), 1, null));
        add(new Research("ARMORVOIDFORTRESS", "ELDRITCH", new AspectList().add(Aspects.ARMOR, 5).add(Aspects.ELDRITCH, 3).add(Aspects.CLOTH, 3).add(Aspects.DARKNESS, 3).add(Aspects.VOID, 5), 0, -3, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.VOID_ROBE_HELMET), List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("VOIDMETAL", "ENCHFABRIC", "ELDRITCHMAJOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ARMORVOIDFORTRESS.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("VoidRobeHelm")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("VoidRobeChest")),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("VoidRobeLegs"))), 0, null));
        add(new Research("FOCUSPRIMAL", "ELDRITCH", new AspectList().add(Aspects.AIR, 6).add(Aspects.WATER, 6).add(Aspects.FIRE, 6).add(Aspects.EARTH, 6).add(Aspects.ORDER, 6).add(Aspects.ENTROPY, 6).add(Aspects.MAGIC, 6), 4, 1, 2,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.FOCI.get("primal")), List.of(Research.Mark.CONCEALED),
                List.of("ELDRITCHMINOR"), List.of(), List.of(),
                List.of(
                        new Page.Text("tc.research_page.FOCUSPRIMAL.1"),
                        new Page.Recipe(Page.Kind.ARCANE, List.of("FocusPrimal"))), 2, null));
        add(new Research("ROD_primal_staff", "ELDRITCH", new AspectList().add(Aspects.AIR, 9).add(Aspects.EARTH, 9).add(Aspects.FIRE, 9).add(Aspects.WATER, 9).add(Aspects.ORDER, 9).add(Aspects.ENTROPY, 9).add(Aspects.TOOL, 9).add(Aspects.MAGIC, 12), 6, 2, 3,
                null, () -> new net.minecraft.world.item.ItemStack(TCItems.STAFF_RODS.get("primal")), List.of(Research.Mark.HIDDEN),
                List.of("FOCUSPRIMAL"), List.of("ROD_silverwood_staff", "ROD_bone_staff", "ROD_greatwood_staff", "ROD_blaze_staff", "ROD_reed_staff", "ROD_obsidian_staff", "ROD_quartz_staff", "ROD_ice_staff"), List.of(),
                List.of(
                        new Page.Text("tc.research_page.ROD_primal_staff.1"),
                        new Page.Recipe(Page.Kind.INFUSION, List.of("WandRodPrimalStaff"))), 3, null));
    }

}
