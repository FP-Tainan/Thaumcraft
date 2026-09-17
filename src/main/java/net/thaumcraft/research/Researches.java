package net.thaumcraft.research;

import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A árvore inteira do Thaumonomicon, como ela é no Thaumcraft 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia3-pesquisas.js} a partir dos
 * {@code ConfigResearch*.java} do mod original. Nenhuma posição, ligação ou aspecto daqui foi
 * digitado de memória — é tudo cópia do que o mod registra. Para mudar, mude o gerador.
 */
public final class Researches {
    /** As pesquisas na ordem em que o mod original as registra. */
    public static final Map<String, Research> ALL = new LinkedHashMap<>();

    private Researches() {
    }

    private static void add(Research research) {
        ALL.put(research.key(), research);
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
        add(new Research("ASPECTS", "BASICS", new AspectList(), 0, 0, 0,
                "textures/misc/r_aspects.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ASPECTS.1", "tc.research_page.ASPECTS.2", "tc.research_page.ASPECTS.3"), 0));
        add(new Research("PECH", "BASICS", new AspectList(), -4, -4, 0,
                "textures/misc/r_pech.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.PECH.1", "tc.research_page.PECH.2"), 0));
        add(new Research("NODES", "BASICS", new AspectList(), -2, 0, 0,
                "textures/misc/r_nodes.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.NODES.1", "tc.research_page.NODES.2", "tc.research_page.NODES.3"), 0));
        add(new Research("WARP", "BASICS", new AspectList(), 0, 2, 0,
                "textures/misc/r_warp.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.WARP.1", "tc.research_page.WARP.2", "tc.research_page.WARP.3"), 0));
        add(new Research("RESEARCH", "BASICS", new AspectList(), 2, 0, 0,
                null, "itemInkwell", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.RESEARCH.1", "tc.research_page.RESEARCH.2", "tc.research_page.RESEARCH.3", "tc.research_page.RESEARCH.4", "tc.research_page.RESEARCH.5", "tc.research_page.RESEARCH.6", "tc.research_page.RESEARCH.7", "tc.research_page.RESEARCH.8", "tc.research_page.RESEARCH.9", "tc.research_page.RESEARCH.10", "tc.research_page.RESEARCH.11", "tc.research_page.RESEARCH.12"), 0));
        add(new Research("KNOWFRAG", "BASICS", new AspectList(), 3, -2, 0,
                null, "itemResource", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("RESEARCH"), List.of(), List.of(), List.of("tc.research_page.KNOWFRAG.1"), 0));
        add(new Research("THAUMONOMICON", "BASICS", new AspectList(), 1, -2, 0,
                null, "itemThaumonomicon", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("RESEARCH"), List.of(), List.of(), List.of("tc.research_page.THAUMONOMICON.1"), 0));
        add(new Research("ORE", "BASICS", new AspectList(), -2, -2, 0,
                null, "blockCustomOre", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ORE.1", "tc.research_page.ORE.2", "tc.research_page.ORE.3", "tc.research_page.ORE.4"), 0));
        add(new Research("PLANTS", "BASICS", new AspectList(), -2, -4, 0,
                null, "blockCustomPlant", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.PLANTS.1", "tc.research_page.PLANTS.2", "tc.research_page.PLANTS.3", "tc.research_page.PLANTS.4", "tc.research_page.PLANTS.5", "tc.research_page.PLANTS.6"), 0));
        add(new Research("RESEARCHER1", "BASICS", new AspectList().add(Aspects.MIND, 3).add(Aspects.SENSES, 3).add(Aspects.ORDER, 3), 4, 1, 1,
                "textures/misc/r_researcher1.png", null, List.of(Research.Mark.ROUND),
                List.of("RESEARCH"), List.of(), List.of(), List.of("tc.research_page.RESEARCHER1.1"), 0));
        add(new Research("DECONSTRUCTOR", "BASICS", new AspectList().add(Aspects.MIND, 3).add(Aspects.CRAFT, 3).add(Aspects.ENTROPY, 3), 6, 2, 1,
                null, "blockTable", List.of(Research.Mark.ROUND),
                List.of("RESEARCHER1"), List.of(), List.of(), List.of("tc.research_page.DECONSTRUCTOR.1", "tc.research_page.DECONSTRUCTOR.2"), 0));
        add(new Research("RESEARCHER2", "BASICS", new AspectList().add(Aspects.MIND, 6).add(Aspects.ORDER, 3).add(Aspects.SENSES, 3).add(Aspects.MAGIC, 3), 3, 3, 2,
                "textures/misc/r_researcher2.png", null, List.of(Research.Mark.ROUND, Research.Mark.SPECIAL),
                List.of("RESEARCHER1"), List.of(), List.of(), List.of("tc.research_page.RESEARCHER2.1"), 1));
        add(new Research("RESEARCHDUPE", "BASICS", new AspectList().add(Aspects.MIND, 6).add(Aspects.EXCHANGE, 3).add(Aspects.SENSES, 3).add(Aspects.GREED, 3).add(Aspects.CRAFT, 3), 4, 5, 3,
                "textures/misc/r_resdupe.png", null, List.of(Research.Mark.ROUND),
                List.of("RESEARCHER2"), List.of(), List.of(), List.of("tc.research_page.RESEARCHDUPE.1"), 0));
        add(new Research("ENCHANT", "BASICS", new AspectList(), -4, -2, 0,
                "textures/misc/r_enchant.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ENCHANT.1", "tc.research_page.ENCHANT.2"), 0));
        add(new Research("NODETAPPER1", "BASICS", new AspectList().add(Aspects.AURA, 3).add(Aspects.MAGIC, 3).add(Aspects.MOTION, 3).add(Aspects.EXCHANGE, 3), -4, 1, 2,
                "textures/misc/r_nodetap1.png", null, List.of(Research.Mark.ROUND),
                List.of("NODES"), List.of(), List.of(), List.of("tc.research_page.NODETAPPER1.1"), 0));
        add(new Research("NODEPRESERVE", "BASICS", new AspectList().add(Aspects.AURA, 3).add(Aspects.GREED, 3).add(Aspects.SENSES, 3), -6, 2, 2,
                "textures/misc/r_nodepreserve.png", null, List.of(Research.Mark.ROUND),
                List.of("NODETAPPER1"), List.of(), List.of(), List.of("tc.research_page.NODEPRESERVE"), 0));
        add(new Research("NODETAPPER2", "BASICS", new AspectList().add(Aspects.AURA, 6).add(Aspects.MAGIC, 3).add(Aspects.MOTION, 3).add(Aspects.EXCHANGE, 3), -3, 3, 2,
                "textures/misc/r_nodetap2.png", null, List.of(Research.Mark.ROUND, Research.Mark.SPECIAL),
                List.of("NODETAPPER1"), List.of(), List.of(), List.of("tc.research_page.NODETAPPER2.1"), 0));
        add(new Research("NODEJAR", "BASICS", new AspectList().add(Aspects.AURA, 6).add(Aspects.GREED, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MOTION, 3), -7, 4, 3,
                null, null, List.of(Research.Mark.CONCEALED),
                List.of("NODEPRESERVE"), List.of(), List.of(), List.of("tc.research_page.NODEJAR.1", "tc.research_page.NODEJAR.2"), 0));
        add(new Research("CRIMSON", "BASICS", new AspectList(), 0, 4, 0,
                null, "itemEldritchObject", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.CRIMSON.1"), 3));
        add(new Research("BASICTHAUMATURGY", "THAUMATURGY", new AspectList(), 0, 0, 0,
                null, "itemWandCasting", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.BASICTHAUMATURGY.1", "tc.research_page.BASICTHAUMATURGY.2"), 0));
        add(new Research("FOCUSFIRE", "THAUMATURGY", new AspectList().add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), 2, -2, 1,
                null, "focusFire", List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(), List.of("tc.research_page.FOCUSFIRE.1", "tc.research_page.FOCUSFIRE.2"), 0));
        add(new Research("FOCUSFROST", "THAUMATURGY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 3).add(Aspects.COLD, 6), 1, -5, 1,
                null, "focusFrost", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(), List.of("tc.research_page.FOCUSFROST.1"), 0));
        add(new Research("FOCUSHELLBAT", "THAUMATURGY", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.BEAST, 6).add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), 3, -7, 2,
                null, "focusHellbat", List.of(Research.Mark.HIDDEN),
                List.of(), List.of("FOCUSFIRE", "INFUSION"), List.of(), List.of("tc.research_page.FOCUSHELLBAT.1"), 2));
        add(new Research("FOCUSEXCAVATION", "THAUMATURGY", new AspectList().add(Aspects.EARTH, 3).add(Aspects.ENTROPY, 3).add(Aspects.MAGIC, 3), 0, -3, 2,
                null, "focusExcavation", List.of(Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(), List.of("tc.research_page.FOCUSEXCAVATION.1"), 0));
        add(new Research("FOCUSWARDING", "THAUMATURGY", new AspectList().add(Aspects.EARTH, 6).add(Aspects.ARMOR, 3).add(Aspects.ORDER, 3).add(Aspects.MIND, 3), -2, -4, 3,
                null, "focusWarding", List.of(Research.Mark.CONCEALED),
                List.of("FOCUSEXCAVATION", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.FOCUSWARDING.1"), 0));
        add(new Research("FOCUSSHOCK", "THAUMATURGY", new AspectList().add(Aspects.AIR, 3).add(Aspects.ENERGY, 6).add(Aspects.MAGIC, 3), 3, -5, 1,
                null, "focusShock", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(), List.of("tc.research_page.FOCUSSHOCK.1"), 0));
        add(new Research("FOCUSTRADE", "THAUMATURGY", new AspectList().add(Aspects.EARTH, 3).add(Aspects.EXCHANGE, 6).add(Aspects.MAGIC, 3), 4, -3, 2,
                null, "focusTrade", List.of(Research.Mark.CONCEALED),
                List.of("FOCUSFIRE"), List.of(), List.of(), List.of("tc.research_page.FOCUSTRADE.1"), 0));
        add(new Research("FOCUSPORTABLEHOLE", "THAUMATURGY", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.ENTROPY, 3).add(Aspects.ELDRITCH, 6).add(Aspects.AIR, 3), 7, -2, 2,
                null, "focusPortableHole", List.of(Research.Mark.CONCEALED),
                List.of("FOCUSTRADE", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.FOCUSPORTABLEHOLE.1"), 0));
        add(new Research("FOCUSPOUCH", "THAUMATURGY", new AspectList().add(Aspects.VOID, 6).add(Aspects.MAGIC, 3).add(Aspects.TOOL, 3), 4, -1, 1,
                null, "itemFocusPouch", List.of(Research.Mark.SECONDARY),
                List.of("FOCUSFIRE"), List.of(), List.of(), List.of("tc.research_page.FOCUSPOUCH.1"), 0));
        add(new Research("CAP_gold", "THAUMATURGY", new AspectList().add(Aspects.METAL, 3).add(Aspects.GREED, 3).add(Aspects.TOOL, 3), 3, 2, 1,
                null, "itemWandCap", List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(), List.of("tc.research_page.CAP_gold.1"), 0));
        add(new Research("CAP_copper", "THAUMATURGY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3).add(Aspects.TOOL, 3), 2, 0, 1,
                null, "itemWandCap", List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(), List.of("tc.research_page.CAP_copper.1"), 0));
        add(new Research("CAP_thaumium", "THAUMATURGY", new AspectList().add(Aspects.METAL, 6).add(Aspects.MAGIC, 6).add(Aspects.TOOL, 3).add(Aspects.AURA, 3), 5, 4, 2,
                null, "itemWandCap", List.of(),
                List.of("CAP_gold", "THAUMIUM", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.CAP_thaumium.1"), 0));
        add(new Research("CAP_silver", "THAUMATURGY", new AspectList().add(Aspects.METAL, 3).add(Aspects.GREED, 3).add(Aspects.TOOL, 3).add(Aspects.AURA, 3), 5, 1, 1,
                null, "itemWandCap", List.of(Research.Mark.CONCEALED),
                List.of("CAP_gold", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.CAP_silver.1"), 0));
        add(new Research("ROD_greatwood", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.TREE, 6).add(Aspects.MAGIC, 3), -5, 2, 1,
                null, "itemWandRod", List.of(),
                List.of("BASICTHAUMATURGY"), List.of(), List.of(), List.of("tc.research_page.ROD_greatwood.1"), 0));
        add(new Research("ROD_reed", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.AIR, 6).add(Aspects.PLANT, 3).add(Aspects.MAGIC, 3), -5, -1, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_reed.1"), 0));
        add(new Research("ROD_blaze", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.FIRE, 6).add(Aspects.ENERGY, 3).add(Aspects.MAGIC, 3), -7, 0, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_blaze.1"), 0));
        add(new Research("ROD_obsidian", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EARTH, 6).add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), -8, 2, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_obsidian.1"), 0));
        add(new Research("ROD_ice", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.COLD, 6).add(Aspects.WATER, 3).add(Aspects.MAGIC, 3), -7, 4, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_ice.1"), 0));
        add(new Research("ROD_quartz", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ORDER, 6).add(Aspects.CRYSTAL, 3).add(Aspects.MAGIC, 3), -5, 5, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_quartz.1"), 0));
        add(new Research("ROD_bone", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ENTROPY, 6).add(Aspects.UNDEAD, 3).add(Aspects.MAGIC, 3), -3, 0, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_bone.1"), 1));
        add(new Research("ROD_silverwood", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 6).add(Aspects.TREE, 6).add(Aspects.MAGIC, 9), -2, 5, 3,
                null, "itemWandRod", List.of(),
                List.of("ROD_greatwood", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ROD_silverwood.1"), 0));
        add(new Research("SCEPTRE", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 6).add(Aspects.CRAFT, 6).add(Aspects.TREE, 6).add(Aspects.MAGIC, 9), 0, 4, 3,
                null, null, List.of(Research.Mark.CONCEALED),
                List.of("ROD_silverwood"), List.of(), List.of(), List.of("tc.research_page.SCEPTRE.1"), 0));
        add(new Research("ROD_greatwood_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.TREE, 6).add(Aspects.MAGIC, 3), -1, 7, 1,
                null, "itemWandRod", List.of(),
                List.of("ROD_silverwood"), List.of(), List.of(), List.of("tc.research_page.ROD_greatwood_staff.1", "tc.research_page.ROD_greatwood_staff.2"), 0));
        add(new Research("ROD_reed_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.AIR, 6).add(Aspects.PLANT, 3).add(Aspects.MAGIC, 3), -5, -2, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_reed"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_reed_staff.1"), 0));
        add(new Research("ROD_blaze_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.FIRE, 6).add(Aspects.ENERGY, 3).add(Aspects.MAGIC, 3), -8, -1, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_blaze"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_blaze_staff.1"), 0));
        add(new Research("ROD_obsidian_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EARTH, 6).add(Aspects.FIRE, 3).add(Aspects.MAGIC, 3), -9, 2, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_obsidian"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_obsidian_staff.1"), 0));
        add(new Research("ROD_ice_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.COLD, 6).add(Aspects.WATER, 3).add(Aspects.MAGIC, 3), -8, 5, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_ice"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_ice_staff.1"), 0));
        add(new Research("ROD_quartz_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ORDER, 6).add(Aspects.CRYSTAL, 3).add(Aspects.MAGIC, 3), -4, 6, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_quartz"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_quartz_staff.1"), 0));
        add(new Research("ROD_bone_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.ENTROPY, 6).add(Aspects.UNDEAD, 3).add(Aspects.MAGIC, 3), -2, -1, 2,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_bone"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_bone_staff.1"), 1));
        add(new Research("ROD_silverwood_staff", "THAUMATURGY", new AspectList().add(Aspects.TOOL, 6).add(Aspects.TREE, 6).add(Aspects.MAGIC, 9), -1, 5, 3,
                null, "itemWandRod", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ROD_silverwood"), List.of("ROD_greatwood_staff"), List.of(), List.of("tc.research_page.ROD_silverwood_staff.1"), 0));
        add(new Research("NODESTABILIZER", "THAUMATURGY", new AspectList().add(Aspects.AURA, 4).add(Aspects.ORDER, 4).add(Aspects.ENERGY, 4), -7, -4, 1,
                null, "blockStoneDevice", List.of(),
                List.of("NODEPRESERVE"), List.of(), List.of(), List.of("tc.research_page.NODESTABILIZER.1", "tc.research_page.NODESTABILIZER.2"), 0));
        add(new Research("NODESTABILIZERADV", "THAUMATURGY", new AspectList().add(Aspects.AURA, 9).add(Aspects.MAGIC, 6).add(Aspects.ORDER, 6).add(Aspects.ENERGY, 6), -8, -3, 2,
                null, "blockStoneDevice", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("NODESTABILIZER"), List.of(), List.of(), List.of("tc.research_page.NODESTABILIZERADV.1"), 0));
        add(new Research("VISPOWER", "THAUMATURGY", new AspectList().add(Aspects.AURA, 3).add(Aspects.MECHANISM, 3).add(Aspects.ENERGY, 6), -5, -6, 2,
                null, "blockStoneDevice", List.of(Research.Mark.SPECIAL),
                List.of("NODESTABILIZER"), List.of(), List.of(), List.of("tc.research_page.VISPOWER.1", "tc.research_page.VISPOWER.2", "tc.research_page.VISPOWER.3", "tc.research_page.VISPOWER.4", "tc.research_page.VISPOWER.5"), 0));
        add(new Research("FOCALMANIPULATION", "THAUMATURGY", new AspectList().add(Aspects.MAGIC, 8).add(Aspects.TOOL, 8).add(Aspects.CRAFT, 5).add(Aspects.CRYSTAL, 5).add(Aspects.ENERGY, 5), -3, -8, 2,
                null, "blockStoneDevice", List.of(),
                List.of("VISPOWER"), List.of("INFUSION", "FOCUSFIRE"), List.of(), List.of("tc.research_page.FOCALMANIPULATION.1", "tc.research_page.FOCALMANIPULATION.2"), 0));
        add(new Research("VAMPBAT", "THAUMATURGY", new AspectList().add(Aspects.HUNGER, 5).add(Aspects.LIFE, 5).add(Aspects.MAGIC, 5), 4, -8, 1,
                "textures/foci/vampirebats.png", null, List.of(Research.Mark.SECONDARY),
                List.of("FOCUSHELLBAT"), List.of("FOCALMANIPULATION"), List.of(), List.of(), 0));
        add(new Research("WANDPED", "THAUMATURGY", new AspectList().add(Aspects.AURA, 6).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.ENERGY, 3), -9, -6, 2,
                null, "blockStoneDevice", List.of(Research.Mark.CONCEALED),
                List.of("INFUSION", "NODEPRESERVE", "NODESTABILIZER"), List.of(), List.of(), List.of("tc.research_page.WANDPED.1"), 0));
        add(new Research("VISAMULET", "THAUMATURGY", new AspectList().add(Aspects.AURA, 3).add(Aspects.MAGIC, 6).add(Aspects.ENERGY, 3).add(Aspects.VOID, 3), -9, -8, 2,
                null, "itemAmuletVis", List.of(Research.Mark.CONCEALED),
                List.of("WANDPED"), List.of(), List.of(), List.of("tc.research_page.VISAMULET.1", "tc.research_page.VISAMULET.2"), 0));
        add(new Research("WANDPEDFOC", "THAUMATURGY", new AspectList().add(Aspects.AURA, 6).add(Aspects.MAGIC, 6).add(Aspects.EXCHANGE, 6).add(Aspects.ENERGY, 3).add(Aspects.TOOL, 3), -10, -7, 3,
                null, "blockStoneDevice", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("WANDPED"), List.of(), List.of(), List.of("tc.research_page.WANDPEDFOC.1"), 0));
        add(new Research("VISCHARGERELAY", "THAUMATURGY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.AURA, 3).add(Aspects.MECHANISM, 3).add(Aspects.ENERGY, 6), -7, -6, 2,
                null, "blockMetalDevice", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("VISPOWER", "WANDPED"), List.of("ROD_greatwood"), List.of(), List.of("tc.research_page.VISCHARGERELAY.1"), 0));
        add(new Research("CAP_iron", "THAUMATURGY", new AspectList(), 0, 0, 1,
                null, null, List.of(Research.Mark.AUTO, Research.Mark.VIRTUAL),
                List.of(), List.of(), List.of(), List.of(), 0));
        add(new Research("ROD_wood", "THAUMATURGY", new AspectList(), 0, 0, 1,
                null, null, List.of(Research.Mark.AUTO, Research.Mark.VIRTUAL),
                List.of(), List.of(), List.of(), List.of(), 0));
        add(new Research("PHIAL", "ALCHEMY", new AspectList(), 0, -2, 0,
                null, "itemEssence", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.PHIAL.1"), 0));
        add(new Research("CRUCIBLE", "ALCHEMY", new AspectList(), 0, 0, 0,
                null, "blockMetalDevice", List.of(Research.Mark.STUB, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.CRUCIBLE.1", "tc.research_page.CRUCIBLE.2", "tc.research_page.CRUCIBLE.3", "tc.research_page.CRUCIBLE.4", "tc.research_page.CRUCIBLE.5"), 0));
        add(new Research("NITOR", "ALCHEMY", new AspectList().add(Aspects.LIGHT, 3).add(Aspects.FIRE, 1), 2, -1, 1,
                null, "itemResource", List.of(),
                List.of("CRUCIBLE"), List.of(), List.of(), List.of("tc.research_page.NITOR.1"), 0));
        add(new Research("ALUMENTUM", "ALCHEMY", new AspectList().add(Aspects.ENERGY, 3).add(Aspects.FIRE, 1), 2, 1, 1,
                null, "itemResource", List.of(),
                List.of("CRUCIBLE"), List.of(), List.of(), List.of("tc.research_page.ALUMENTUM.1"), 0));
        add(new Research("DISTILESSENTIA", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.WATER, 3).add(Aspects.SLIME, 3), 5, -1, 1,
                null, "blockMetalDevice", List.of(),
                List.of("NITOR", "ALUMENTUM"), List.of(), List.of("JARLABEL"), List.of("tc.research_page.DISTILESSENTIA.1", "tc.research_page.DISTILESSENTIA.2"), 0));
        add(new Research("THAUMIUM", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.MAGIC, 3), -1, 3, 1,
                null, "itemResource", List.of(Research.Mark.HIDDEN),
                List.of("CRUCIBLE"), List.of(), List.of(), List.of("tc.research_page.THAUMIUM.1"), 0));
        add(new Research("TUBES", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3), 7, 0, 1,
                null, "blockTube", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("DISTILESSENTIA"), List.of(), List.of(), List.of("tc.research_page.TUBES.1", "tc.research_page.TUBES.2", "tc.research_page.TUBES.3", "tc.research_page.TUBES.4"), 0));
        add(new Research("TUBEFILTER", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.ORDER, 3), 9, 1, 2,
                null, "blockTube", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TUBES"), List.of(), List.of(), List.of("tc.research_page.TUBEFILTER.1", "tc.research_page.TUBEFILTER.2"), 0));
        add(new Research("ESSENTIACRYSTAL", "ALCHEMY", new AspectList().add(Aspects.WATER, 5).add(Aspects.CRYSTAL, 5).add(Aspects.EXCHANGE, 3).add(Aspects.MAGIC, 5), 8, -2, 1,
                null, "blockTube", List.of(Research.Mark.CONCEALED),
                List.of("TUBES"), List.of(), List.of(), List.of("tc.research_page.ESSENTIACRYSTAL.1"), 0));
        add(new Research("CENTRIFUGE", "ALCHEMY", new AspectList().add(Aspects.ENTROPY, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.CRAFT, 3), 10, 0, 2,
                null, "blockTube", List.of(Research.Mark.CONCEALED),
                List.of("TUBEFILTER"), List.of(), List.of(), List.of("tc.research_page.CENTRIFUGE.1", "tc.research_page.CENTRIFUGE.2", "tc.research_page.CENTRIFUGE.3"), 0));
        add(new Research("THAUMATORIUM", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MAGIC, 6).add(Aspects.EXCHANGE, 3).add(Aspects.CRAFT, 3), 10, -2, 3,
                "textures/blocks/alchemyblock.png", null, List.of(Research.Mark.CONCEALED),
                List.of("CENTRIFUGE"), List.of(), List.of(), List.of("tc.research_page.THAUMATORIUM.1", "tc.research_page.THAUMATORIUM.2", "tc.research_page.THAUMATORIUM.3"), 0));
        add(new Research("TALLOW", "ALCHEMY", new AspectList().add(Aspects.FLESH, 3).add(Aspects.MAGIC, 1), -2, 0, 1,
                null, "itemResource", List.of(),
                List.of("CRUCIBLE"), List.of(), List.of(), List.of("tc.research_page.TALLOW.1"), 0));
        add(new Research("ALCHEMICALDUPLICATION", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.GREED, 3).add(Aspects.CRAFT, 3), -4, 0, 1,
                "textures/misc/r_alchmult.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TALLOW"), List.of(), List.of(), List.of("tc.research_page.ALCHEMICALDUPLICATION.1"), 0));
        add(new Research("ALCHEMICALMANUFACTURE", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3).add(Aspects.CRAFT, 3), -5, -2, 1,
                "textures/misc/r_alchman.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ALCHEMICALDUPLICATION"), List.of(), List.of(), List.of("tc.research_page.ALCHEMICALMANUFACTURE.1"), 0));
        add(new Research("ENTROPICPROCESSING", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 1).add(Aspects.ENTROPY, 3).add(Aspects.CRAFT, 1), -6, 1, 1,
                "textures/misc/r_alchent.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ALCHEMICALDUPLICATION"), List.of(), List.of(), List.of("tc.research_page.ENTROPICPROCESSING.1"), 0));
        add(new Research("LIQUIDDEATH", "ALCHEMY", new AspectList().add(Aspects.DEATH, 3).add(Aspects.POISON, 3).add(Aspects.ENTROPY, 1).add(Aspects.WATER, 1), -7, 3, 2,
                null, "itemBucketDeath", List.of(Research.Mark.HIDDEN),
                List.of("ENTROPICPROCESSING"), List.of(), List.of(), List.of("tc.research_page.LIQUIDDEATH.1"), 3));
        add(new Research("BOTTLETAINT", "ALCHEMY", new AspectList().add(Aspects.TAINT, 5).add(Aspects.MAGIC, 3).add(Aspects.ENTROPY, 1).add(Aspects.WATER, 1), -8, 1, 2,
                null, "itemBottleTaint", List.of(Research.Mark.HIDDEN),
                List.of("ENTROPICPROCESSING"), List.of(), List.of(), List.of("tc.research_page.BOTTLETAINT.1"), 2));
        add(new Research("PUREIRON", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 3), -2, 5, 1,
                null, "itemNugget", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM"), List.of(), List.of(), List.of("tc.research_page.PUREIRON.1"), 0));
        add(new Research("PUREGOLD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.GREED, 1), -4, 3, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(), List.of("tc.research_page.PUREGOLD.1"), 0));
        add(new Research("PURECOPPER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.EXCHANGE, 1), -4, 5, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(), List.of("tc.research_page.PURECOPPER.1"), 0));
        add(new Research("PURETIN", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.CRYSTAL, 1), -4, 7, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(), List.of("tc.research_page.PURETIN.1"), 0));
        add(new Research("PURESILVER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 2).add(Aspects.GREED, 1), -3, 8, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(), List.of("tc.research_page.PURESILVER.1"), 0));
        add(new Research("PURELEAD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.ORDER, 3), -2, 9, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("PUREIRON"), List.of(), List.of(), List.of("tc.research_page.PURELEAD.1"), 0));
        add(new Research("TRANSIRON", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 0, 5, 1,
                null, "itemNugget", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM"), List.of(), List.of(), List.of("tc.research_page.TRANSIRON.1"), 0));
        add(new Research("TRANSGOLD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 2, 3, 1,
                null, "Items", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(), List.of("tc.research_page.TRANSGOLD.1"), 0));
        add(new Research("TRANSCOPPER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 2, 5, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(), List.of("tc.research_page.TRANSCOPPER.1"), 0));
        add(new Research("TRANSTIN", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 2).add(Aspects.CRYSTAL, 1), 2, 7, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(), List.of("tc.research_page.TRANSTIN.1"), 0));
        add(new Research("TRANSSILVER", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 2).add(Aspects.GREED, 1), 1, 8, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(), List.of("tc.research_page.TRANSSILVER.1"), 0));
        add(new Research("TRANSLEAD", "ALCHEMY", new AspectList().add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 2).add(Aspects.ORDER, 1), 0, 9, 1,
                null, "itemNugget", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("TRANSIRON"), List.of(), List.of(), List.of("tc.research_page.TRANSLEAD.1"), 0));
        add(new Research("ETHEREALBLOOM", "ALCHEMY", new AspectList().add(Aspects.MAGIC, 1).add(Aspects.PLANT, 6).add(Aspects.HEAL, 3).add(Aspects.TAINT, 6), -2, -3, 2,
                null, "blockCustomPlant", List.of(Research.Mark.CONCEALED, Research.Mark.HIDDEN),
                List.of("CRUCIBLE"), List.of(), List.of(), List.of("tc.research_page.ETHEREALBLOOM.1", "tc.research_page.ETHEREALBLOOM.2"), 0));
        add(new Research("BATHSALTS", "ALCHEMY", new AspectList().add(Aspects.MIND, 3).add(Aspects.AURA, 3).add(Aspects.ORDER, 3).add(Aspects.HEAL, 3), -4, -4, 2,
                null, "itemBathSalts", List.of(Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.BATHSALTS.1"), 0));
        add(new Research("SANESOAP", "ALCHEMY", new AspectList().add(Aspects.MIND, 5).add(Aspects.ORDER, 5).add(Aspects.HEAL, 5).add(Aspects.ELDRITCH, 5), -3, -6, 1,
                null, "itemSanitySoap", List.of(),
                List.of("BATHSALTS"), List.of(), List.of(), List.of("tc.research_page.SANESOAP.1"), 0));
        add(new Research("ARCANESPA", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.MECHANISM, 3).add(Aspects.ORDER, 3), -6, -5, 1,
                null, "blockStoneDevice", List.of(Research.Mark.SECONDARY),
                List.of("BATHSALTS"), List.of(), List.of(), List.of("tc.research_page.ARCANESPA.1"), 0));
        add(new Research("JARLABEL", "ALCHEMY", new AspectList(), 4, -3, 0,
                null, "blockJar", List.of(Research.Mark.STUB, Research.Mark.ROUND),
                List.of("DISTILESSENTIA"), List.of(), List.of(), List.of("tc.research_page.JARLABEL.1", "tc.research_page.JARLABEL.2", "tc.research_page.JARLABEL.3"), 0));
        add(new Research("JARVOID", "ALCHEMY", new AspectList().add(Aspects.WATER, 3).add(Aspects.ENTROPY, 3).add(Aspects.VOID, 6), 5, -5, 1,
                null, "blockJar", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("JARLABEL"), List.of(), List.of(), List.of("tc.research_page.JARVOID.1"), 0));
        add(new Research("ARCANESTONE", "ARTIFICE", new AspectList(), 5, -2, 0,
                null, "blockCosmeticSolid", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ARCANESTONE.1"), 0));
        add(new Research("GRATE", "ARTIFICE", new AspectList(), 2, -1, 0,
                null, "blockMetalDevice", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.GRATE.1"), 0));
        add(new Research("TABLE", "ARTIFICE", new AspectList(), 0, -1, 0,
                null, "blockTable", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TABLE.1"), 0));
        add(new Research("ARCTABLE", "ARTIFICE", new AspectList(), -1, -3, 0,
                null, "blockTable", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("TABLE"), List.of(), List.of(), List.of("tc.research_page.ARCTABLE.1"), 0));
        add(new Research("RESTABLE", "ARTIFICE", new AspectList(), 1, -3, 0,
                null, "blockTable", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of("TABLE"), List.of(), List.of(), List.of("tc.research_page.RESTABLE.1"), 0));
        add(new Research("THAUMOMETER", "ARTIFICE", new AspectList(), 2, 1, 0,
                null, "itemThaumometer", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.THAUMOMETER.1"), 0));
        add(new Research("PAVETRAVEL", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.EARTH, 3).add(Aspects.FLIGHT, 3), 4, -4, 1,
                null, "blockCosmeticSolid", List.of(Research.Mark.SECONDARY),
                List.of("ARCANESTONE"), List.of(), List.of(), List.of("tc.research_page.PAVETRAVEL.1"), 0));
        add(new Research("PAVEWARD", "ARTIFICE", new AspectList().add(Aspects.MOTION, 3).add(Aspects.TRAP, 3).add(Aspects.BEAST, 3), 6, -4, 1,
                null, "blockCosmeticSolid", List.of(Research.Mark.SECONDARY),
                List.of("ARCANESTONE"), List.of(), List.of(), List.of("tc.research_page.PAVEWARD.1", "tc.research_page.PAVEWARD.2"), 0));
        add(new Research("GOGGLES", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.AURA, 3).add(Aspects.MAGIC, 3), 4, 1, 1,
                null, "itemGoggles", List.of(Research.Mark.CONCEALED),
                List.of("THAUMOMETER"), List.of(), List.of(), List.of("tc.research_page.GOGGLES.1"), 0));
        add(new Research("ARCANEEAR", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.ENERGY, 3).add(Aspects.AIR, 3), 6, 0, 1,
                null, "blockWoodenDevice", List.of(Research.Mark.CONCEALED),
                List.of("GOGGLES"), List.of(), List.of(), List.of("tc.research_page.ARCANEEAR.1"), 0));
        add(new Research("SINSTONE", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.DARKNESS, 3).add(Aspects.ELDRITCH, 3).add(Aspects.AURA, 3), 6, 2, 1,
                null, "itemCompassStone", List.of(Research.Mark.CONCEALED),
                List.of("GOGGLES"), List.of(), List.of(), List.of("tc.research_page.SINSTONE.1"), 2));
        add(new Research("LEVITATOR", "ARTIFICE", new AspectList().add(Aspects.MOTION, 3).add(Aspects.FLIGHT, 3).add(Aspects.AIR, 3), -3, -3, 1,
                null, "blockLifter", List.of(Research.Mark.CONCEALED),
                List.of("NITOR"), List.of(), List.of(), List.of("tc.research_page.LEVITATOR.1"), 0));
        add(new Research("INFERNALFURNACE", "ARTIFICE", new AspectList().add(Aspects.FIRE, 6).add(Aspects.METAL, 3).add(Aspects.CRAFT, 3).add(Aspects.AURA, 3), -4, -1, 2,
                "textures/misc/r_infernalfurnace.png", null, List.of(Research.Mark.CONCEALED),
                List.of("NITOR", "ALUMENTUM"), List.of(), List.of(), List.of("tc.research_page.INFERNALFURNACE.1", "tc.research_page.INFERNALFURNACE.2"), 2));
        add(new Research("BELLOWS", "ARTIFICE", new AspectList().add(Aspects.AIR, 6).add(Aspects.MECHANISM, 3).add(Aspects.MOTION, 3), -6, -2, 1,
                null, "blockWoodenDevice", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("INFERNALFURNACE"), List.of(), List.of(), List.of("tc.research_page.BELLOWS.1", "tc.research_page.BELLOWS.2"), 0));
        add(new Research("ARCANEBORE", "ARTIFICE", new AspectList().add(Aspects.MINE, 6).add(Aspects.MOTION, 3).add(Aspects.MECHANISM, 3).add(Aspects.TOOL, 3), -3, 8, 2,
                null, "blockWoodenDevice", List.of(Research.Mark.CONCEALED),
                List.of("FOCUSEXCAVATION", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ARCANEBORE.1", "tc.research_page.ARCANEBORE.2", "tc.research_page.ARCANEBORE.3"), 0));
        add(new Research("ARCANELAMP", "ARTIFICE", new AspectList().add(Aspects.LIGHT, 3).add(Aspects.SENSES, 3).add(Aspects.DARKNESS, 3), -3, 1, 1,
                null, "blockMetalDevice", List.of(Research.Mark.SECONDARY),
                List.of("NITOR"), List.of(), List.of(), List.of("tc.research_page.ARCANELAMP.1"), 0));
        add(new Research("ENCHFABRIC", "ARTIFICE", new AspectList().add(Aspects.CLOTH, 3).add(Aspects.MAGIC, 3), 0, 3, 1,
                null, "itemResource", List.of(Research.Mark.SECONDARY),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ENCHFABRIC.1", "tc.research_page.ENCHFABRIC.2"), 0));
        add(new Research("RUNICARMOR", "ARTIFICE", new AspectList().add(Aspects.ARMOR, 6).add(Aspects.AIR, 3).add(Aspects.MAGIC, 3).add(Aspects.ENERGY, 3).add(Aspects.MIND, 3), 3, 4, 3,
                null, "itemRingRunic", List.of(Research.Mark.CONCEALED),
                List.of("ENCHFABRIC"), List.of("INFUSION"), List.of(), List.of("tc.research_page.RUNICARMOR.1", "tc.research_page.RUNICARMOR.2"), 0));
        add(new Research("RUNICCHARGED", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.ENERGY, 6), 2, 3, 2,
                null, "itemRingRunic", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(), List.of("tc.research_page.RUNICCHARGED.1"), 0));
        add(new Research("RUNICHEALING", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.HEAL, 4).add(Aspects.WATER, 4), 4, 3, 2,
                null, "itemRingRunic", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(), List.of("tc.research_page.RUNICHEALING.1"), 0));
        add(new Research("RUNICKINETIC", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.AIR, 6), 2, 5, 2,
                null, "itemGirdleRunic", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(), List.of("tc.research_page.RUNICKINETIC.1"), 0));
        add(new Research("RUNICEMERGENCY", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.EARTH, 4).add(Aspects.VOID, 4), 4, 5, 2,
                null, "itemAmuletRunic", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(), List.of("tc.research_page.RUNICEMERGENCY.1"), 0));
        add(new Research("RUNICAUGMENTATION", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.ARMOR, 3).add(Aspects.EXCHANGE, 4).add(Aspects.GREED, 4), 6, 4, 1,
                "textures/misc/r_runicupg.png", null, List.of(Research.Mark.CONCEALED),
                List.of("RUNICARMOR"), List.of(), List.of(), List.of("tc.research_page.RUNICAUGMENTATION.1", "tc.research_page.RUNICAUGMENTATION.2"), 0));
        add(new Research("BANNERS", "ARTIFICE", new AspectList().add(Aspects.SENSES, 3).add(Aspects.CLOTH, 3).add(Aspects.MAGIC, 1), 4, 8, 1,
                null, null, List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.BANNERS.1"), 0));
        add(new Research("ELEMENTALAXE", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.WATER, 3).add(Aspects.MOTION, 3), -7, 4, 2,
                null, "itemAxeElemental", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ELEMENTALAXE.1", "tc.research_page.ELEMENTALAXE.2"), 0));
        add(new Research("ELEMENTALPICK", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.FIRE, 3).add(Aspects.SENSES, 3), -7, 3, 2,
                null, "itemPickElemental", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ELEMENTALPICK.1", "tc.research_page.ELEMENTALPICK.2"), 0));
        add(new Research("ELEMENTALSWORD", "ARTIFICE", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.AIR, 3).add(Aspects.ENERGY, 3), -7, 5, 2,
                null, "itemSwordElemental", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ELEMENTALSWORD.1"), 0));
        add(new Research("ELEMENTALSHOVEL", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EARTH, 3).add(Aspects.CRAFT, 3), -7, 6, 2,
                null, "itemShovelElemental", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ELEMENTALSHOVEL.1", "tc.research_page.ELEMENTALSHOVEL.2"), 0));
        add(new Research("ELEMENTALHOE", "ARTIFICE", new AspectList().add(Aspects.TOOL, 3).add(Aspects.LIFE, 3).add(Aspects.CROP, 3), -7, 7, 2,
                null, "itemHoeElemental", List.of(Research.Mark.CONCEALED),
                List.of("THAUMIUM", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ELEMENTALHOE.1"), 0));
        add(new Research("WARDEDARCANA", "ARTIFICE", new AspectList().add(Aspects.TOOL, 6).add(Aspects.MIND, 3).add(Aspects.MECHANISM, 3).add(Aspects.ARMOR, 3), -5, -4, 2,
                null, "itemArcaneDoor", List.of(),
                List.of("THAUMIUM"), List.of(), List.of(), List.of("tc.research_page.WARDEDARCANA.1", "tc.research_page.WARDEDARCANA.2", "tc.research_page.WARDEDARCANA.3", "tc.research_page.WARDEDARCANA.4"), 0));
        add(new Research("BONEBOW", "ARTIFICE", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.AIR, 3).add(Aspects.MOTION, 3), -7, 1, 1,
                null, "itemBowBone", List.of(Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.BONEBOW.1"), 0));
        add(new Research("PRIMALARROW", "ARTIFICE", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.AIR, 3).add(Aspects.FIRE, 3).add(Aspects.WATER, 3).add(Aspects.EARTH, 3).add(Aspects.ORDER, 3).add(Aspects.ENTROPY, 3), -9, 0, 2,
                null, "itemPrimalArrow", List.of(Research.Mark.CONCEALED),
                List.of("BONEBOW"), List.of(), List.of(), List.of("tc.research_page.PRIMALARROW.1", "tc.research_page.PRIMALARROW.2", "tc.research_page.PRIMALARROW.3"), 0));
        add(new Research("INFUSION", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 6).add(Aspects.MECHANISM, 3).add(Aspects.CRAFT, 6), -4, 5, 2,
                null, "blockStoneDevice", List.of(Research.Mark.CONCEALED),
                List.of("DISTILESSENTIA"), List.of(), List.of(), List.of("tc.research_page.INFUSION.1", "tc.research_page.INFUSION.2", "tc.research_page.INFUSION.3", "tc.research_page.INFUSION.4", "tc.research_page.INFUSION.5"), 0));
        add(new Research("LAMPGROWTH", "ARTIFICE", new AspectList().add(Aspects.LIGHT, 3).add(Aspects.PLANT, 6).add(Aspects.LIFE, 3).add(Aspects.CROP, 3), -4, 3, 2,
                null, "blockMetalDevice", List.of(Research.Mark.HIDDEN),
                List.of("ARCANELAMP", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.LAMPGROWTH.1"), 0));
        add(new Research("LAMPFERTILITY", "ARTIFICE", new AspectList().add(Aspects.BEAST, 6).add(Aspects.LIFE, 6).add(Aspects.LIGHT, 3), -2, 3, 2,
                null, "blockMetalDevice", List.of(Research.Mark.HIDDEN),
                List.of("ARCANELAMP", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.LAMPFERTILITY.1"), 0));
        add(new Research("MIRROR", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 6).add(Aspects.ELDRITCH, 3).add(Aspects.DARKNESS, 3).add(Aspects.CRYSTAL, 3), -1, 8, 2,
                null, "blockMirror", List.of(Research.Mark.HIDDEN),
                List.of("INFUSION"), List.of(), List.of(), List.of("tc.research_page.MIRROR.1", "tc.research_page.MIRROR.2", "tc.research_page.MIRROR.3"), 0));
        add(new Research("MIRRORHAND", "ARTIFICE", new AspectList().add(Aspects.TOOL, 6).add(Aspects.ELDRITCH, 3).add(Aspects.CRYSTAL, 3).add(Aspects.TRAVEL, 3), 1, 9, 2,
                null, "itemHandMirror", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("MIRROR"), List.of(), List.of(), List.of("tc.research_page.MIRRORHAND.1"), 0));
        add(new Research("MIRRORESSENTIA", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 6).add(Aspects.ELDRITCH, 3).add(Aspects.WATER, 3).add(Aspects.MAGIC, 3), -1, 10, 2,
                null, "blockMirror", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("MIRROR"), List.of(), List.of(), List.of("tc.research_page.MIRRORESSENTIA.1", "tc.research_page.MIRRORESSENTIA.2"), 0));
        add(new Research("JARBRAIN", "ARTIFICE", new AspectList().add(Aspects.HUNGER, 3).add(Aspects.MIND, 3).add(Aspects.UNDEAD, 3).add(Aspects.GREED, 3), -5, 9, 2,
                null, "blockJar", List.of(Research.Mark.HIDDEN),
                List.of("INFUSION"), List.of(), List.of(), List.of("tc.research_page.JARBRAIN.1"), 3));
        add(new Research("INFUSIONENCHANTMENT", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 6).add(Aspects.MIND, 3).add(Aspects.WEAPON, 3).add(Aspects.ARMOR, 3).add(Aspects.TOOL, 3), -6, 11, 3,
                "textures/misc/r_enchant.png", null, List.of(Research.Mark.CONCEALED),
                List.of("JARBRAIN"), List.of(), List.of(), List.of("tc.research_page.INFUSIONENCHANTMENT.1", "tc.research_page.INFUSIONENCHANTMENT.2", "tc.research_page.INFUSIONENCHANTMENT.3"), 0));
        add(new Research("ARMORFORTRESS", "ARTIFICE", new AspectList().add(Aspects.METAL, 3).add(Aspects.ARMOR, 5).add(Aspects.CRAFT, 5), -8, 9, 2,
                null, "itemHelmFortress", List.of(Research.Mark.HIDDEN),
                List.of("THAUMIUM", "INFUSIONENCHANTMENT"), List.of(), List.of(), List.of("tc.research_page.ARMORFORTRESS.1", "tc.research_page.ARMORFORTRESS.2"), 0));
        add(new Research("HELMGOGGLES", "ARTIFICE", new AspectList().add(Aspects.SENSES, 5).add(Aspects.AURA, 3).add(Aspects.ARMOR, 3), -9, 7, 2,
                null, "itemGoggles", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of("GOGGLES"), List.of(), List.of("tc.research_page.HELMGOGGLES.1"), 0));
        add(new Research("MASKGRINNINGDEVIL", "ARTIFICE", new AspectList().add(Aspects.HEAL, 5).add(Aspects.MIND, 5).add(Aspects.ARMOR, 3), -10, 8, 2,
                "textures/misc/r_mask0.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of(), List.of(), List.of("tc.research_page.MASKGRINNINGDEVIL.1"), 0));
        add(new Research("MASKANGRYGHOST", "ARTIFICE", new AspectList().add(Aspects.ENTROPY, 5).add(Aspects.DEATH, 5).add(Aspects.ARMOR, 3), -10, 9, 2,
                "textures/misc/r_mask1.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of(), List.of(), List.of("tc.research_page.MASKANGRYGHOST.1"), 1));
        add(new Research("MASKSIPPINGFIEND", "ARTIFICE", new AspectList().add(Aspects.UNDEAD, 5).add(Aspects.LIFE, 5).add(Aspects.ARMOR, 3), -10, 10, 2,
                "textures/misc/r_mask2.png", null, List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("ARMORFORTRESS"), List.of(), List.of(), List.of("tc.research_page.MASKSIPPINGFIEND.1"), 1));
        add(new Research("BOOTSTRAVELLER", "ARTIFICE", new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.EARTH, 3).add(Aspects.FLIGHT, 3).add(Aspects.WATER, 3), -1, 5, 2,
                null, "itemBootsTraveller", List.of(Research.Mark.CONCEALED),
                List.of("ENCHFABRIC", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.BOOTSTRAVELLER.1"), 0));
        add(new Research("HOVERHARNESS", "ARTIFICE", new AspectList().add(Aspects.FLIGHT, 6).add(Aspects.TRAVEL, 6).add(Aspects.AIR, 6).add(Aspects.MECHANISM, 3), 1, 7, 3,
                null, "itemHoverHarness", List.of(Research.Mark.CONCEALED),
                List.of("BOOTSTRAVELLER"), List.of(), List.of(), List.of("tc.research_page.HOVERHARNESS.1", "tc.research_page.HOVERHARNESS.2"), 0));
        add(new Research("HOVERGIRDLE", "ARTIFICE", new AspectList().add(Aspects.FLIGHT, 6).add(Aspects.TRAVEL, 3).add(Aspects.AIR, 3).add(Aspects.MOTION, 6), 2, 7, 3,
                null, "itemGirdleHover", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of("HOVERHARNESS"), List.of(), List.of(), List.of("tc.research_page.HOVERGIRDLE.1"), 0));
        add(new Research("BASICARTIFACE", "ARTIFICE", new AspectList(), 0, 1, 0,
                null, "itemResource", List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.AUTO),
                List.of(), List.of(), List.of(), List.of("tc.research_page.BASICARTIFACE.1"), 0));
        add(new Research("FLUXSCRUB", "ARTIFICE", new AspectList().add(Aspects.MAGIC, 3).add(Aspects.TRAP, 3).add(Aspects.AIR, 3).add(Aspects.WATER, 3), -8, -3, 1,
                null, "blockStoneDevice", List.of(Research.Mark.SECONDARY),
                List.of("VISPOWER", "BELLOWS", "TUBES"), List.of("INFUSION"), List.of(), List.of("tc.research_page.FLUXSCRUB.1"), 0));
        add(new Research("HUNGRYCHEST", "GOLEMANCY", new AspectList().add(Aspects.HUNGER, 3).add(Aspects.VOID, 3), -1, 0, 1,
                null, "blockChestHungry", List.of(Research.Mark.SECONDARY),
                List.of(), List.of(), List.of(), List.of("tc.research_page.HUNGRYCHEST.1"), 0));
        add(new Research("TINYHAT", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.LIFE, 1).add(Aspects.GREED, 1), 5, 10, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYHAT.1"), 0));
        add(new Research("TINYGLASSES", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.SENSES, 1).add(Aspects.GREED, 1), 6, 10, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYGLASSES.1"), 0));
        add(new Research("TINYBOWTIE", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.TRAVEL, 1).add(Aspects.GREED, 1), 7, 10, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYBOWTIE.1"), 0));
        add(new Research("TINYFEZ", "GOLEMANCY", new AspectList().add(Aspects.CLOTH, 2).add(Aspects.ENERGY, 1).add(Aspects.GREED, 1), 8, 10, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYFEZ.1"), 0));
        add(new Research("TINYDART", "GOLEMANCY", new AspectList().add(Aspects.FLIGHT, 1).add(Aspects.WEAPON, 2).add(Aspects.GREED, 1), 5, 11, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYDART.1"), 0));
        add(new Research("TINYVISOR", "GOLEMANCY", new AspectList().add(Aspects.SENSES, 1).add(Aspects.ARMOR, 2).add(Aspects.GREED, 1), 6, 11, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYVISOR.1"), 0));
        add(new Research("TINYARMOR", "GOLEMANCY", new AspectList().add(Aspects.METAL, 1).add(Aspects.ARMOR, 2).add(Aspects.GREED, 1), 7, 11, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYARMOR.1"), 0));
        add(new Research("TINYHAMMER", "GOLEMANCY", new AspectList().add(Aspects.METAL, 1).add(Aspects.WEAPON, 2).add(Aspects.GREED, 1), 8, 11, 1,
                null, "itemGolemDecoration", List.of(Research.Mark.SECONDARY, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.TINYHAMMER.1"), 0));
        add(new Research("GOLEMSTRAW", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 3).add(Aspects.MOTION, 3).add(Aspects.CROP, 3).add(Aspects.EXCHANGE, 3), 0, 2, 2,
                null, "itemGolemPlacer", List.of(),
                List.of("HUNGRYCHEST"), List.of(), List.of("COREGATHER", "GOLEMBELL"), List.of("tc.research_page.GOLEMSTRAW.1", "tc.research_page.GOLEMSTRAW.2", "tc.research_page.GOLEMSTRAW.3"), 0));
        add(new Research("GOLEMBELL", "GOLEMANCY", new AspectList(), 3, 0, 0,
                null, "itemGolemBell", List.of(Research.Mark.STUB),
                List.of("GOLEMSTRAW"), List.of(), List.of(), List.of("tc.research_page.GOLEMBELL.1", "tc.research_page.GOLEMBELL.2"), 0));
        add(new Research("GOLEMWOOD", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 4).add(Aspects.MOTION, 4).add(Aspects.TREE, 3).add(Aspects.EXCHANGE, 3), 2, 4, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.SECONDARY),
                List.of("GOLEMSTRAW"), List.of(), List.of(), List.of("tc.research_page.GOLEMWOOD.1"), 0));
        add(new Research("GOLEMCLAY", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 6).add(Aspects.MOTION, 6).add(Aspects.EARTH, 3).add(Aspects.EXCHANGE, 3), 2, 6, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMWOOD"), List.of(), List.of(), List.of("tc.research_page.GOLEMCLAY.1"), 0));
        add(new Research("GOLEMSTONE", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 6).add(Aspects.MOTION, 6).add(Aspects.EARTH, 3).add(Aspects.EXCHANGE, 3), 2, 8, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMCLAY"), List.of(), List.of(), List.of("tc.research_page.GOLEMSTONE.1"), 0));
        add(new Research("GOLEMIRON", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 9).add(Aspects.MOTION, 9).add(Aspects.METAL, 3).add(Aspects.EXCHANGE, 3), 0, 10, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMSTONE"), List.of(), List.of(), List.of("tc.research_page.GOLEMIRON.1"), 0));
        add(new Research("GOLEMTHAUMIUM", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 10).add(Aspects.MOTION, 10).add(Aspects.METAL, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3), 2, 10, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.CONCEALED),
                List.of("GOLEMIRON", "THAUMIUM"), List.of(), List.of(), List.of("tc.research_page.GOLEMTHAUMIUM.1"), 0));
        add(new Research("GOLEMFLESH", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 7).add(Aspects.MOTION, 7).add(Aspects.FLESH, 6).add(Aspects.EXCHANGE, 3), 4, 4, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.CONCEALED),
                List.of("GOLEMWOOD"), List.of(), List.of(), List.of("tc.research_page.GOLEMFLESH.1"), 3));
        add(new Research("GOLEMTALLOW", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 3).add(Aspects.MOTION, 3).add(Aspects.FLESH, 3).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 3), 4, 6, 2,
                null, "itemGolemPlacer", List.of(Research.Mark.CONCEALED),
                List.of("GOLEMCLAY", "TALLOW"), List.of(), List.of(), List.of("tc.research_page.GOLEMTALLOW.1"), 0));
        add(new Research("GOLEMFETTER", "GOLEMANCY", new AspectList().add(Aspects.TRAP, 3).add(Aspects.MECHANISM, 3), 4, 8, 1,
                null, "blockCosmeticSolid", List.of(Research.Mark.SECONDARY),
                List.of("GOLEMSTONE"), List.of(), List.of(), List.of("tc.research_page.GOLEMFETTER.1"), 0));
        add(new Research("UPGRADEAIR", "GOLEMANCY", new AspectList().add(Aspects.AIR, 6).add(Aspects.MOTION, 3), 7, -3, 1,
                null, "itemGolemUpgrade", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(), List.of("tc.research_page.UPGRADEAIR.1"), 0));
        add(new Research("UPGRADEEARTH", "GOLEMANCY", new AspectList().add(Aspects.EARTH, 6).add(Aspects.LIFE, 3), 6, -2, 1,
                null, "itemGolemUpgrade", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(), List.of("tc.research_page.UPGRADEEARTH.1"), 0));
        add(new Research("UPGRADEFIRE", "GOLEMANCY", new AspectList().add(Aspects.FIRE, 6).add(Aspects.ENERGY, 3), 5, -1, 1,
                null, "itemGolemUpgrade", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(), List.of("tc.research_page.UPGRADEFIRE.1"), 0));
        add(new Research("UPGRADEWATER", "GOLEMANCY", new AspectList().add(Aspects.WATER, 6).add(Aspects.SENSES, 3), 5, 1, 1,
                null, "itemGolemUpgrade", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(), List.of("tc.research_page.UPGRADEWATER.1"), 0));
        add(new Research("UPGRADEORDER", "GOLEMANCY", new AspectList().add(Aspects.ORDER, 6).add(Aspects.MIND, 3), 6, 2, 1,
                null, "itemGolemUpgrade", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(), List.of("tc.research_page.UPGRADEORDER.1"), 0));
        add(new Research("UPGRADEENTROPY", "GOLEMANCY", new AspectList().add(Aspects.ENTROPY, 6).add(Aspects.MIND, 3), 7, 3, 1,
                null, "itemGolemUpgrade", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("GOLEMBELL"), List.of(), List.of(), List.of("tc.research_page.UPGRADEENTROPY.1"), 0));
        add(new Research("TRAVELTRUNK", "GOLEMANCY", new AspectList().add(Aspects.SOUL, 3).add(Aspects.TRAVEL, 3).add(Aspects.TREE, 3).add(Aspects.VOID, 3), 0, 4, 2,
                null, "itemTrunkSpawner", List.of(Research.Mark.CONCEALED),
                List.of("INFUSION", "GOLEMWOOD"), List.of(), List.of(), List.of("tc.research_page.TRAVELTRUNK.1", "tc.research_page.TRAVELTRUNK.2"), 0));
        add(new Research("COREGATHER", "GOLEMANCY", new AspectList(), -3, 3, 1,
                null, "itemGolemCore", List.of(Research.Mark.STUB, Research.Mark.CONCEALED),
                List.of("GOLEMSTRAW"), List.of(), List.of(), List.of("tc.research_page.COREGATHER.1", "tc.research_page.COREGATHER.2"), 0));
        add(new Research("COREFILL", "GOLEMANCY", new AspectList().add(Aspects.HUNGER, 3).add(Aspects.EXCHANGE, 3).add(Aspects.VOID, 3), -5, 3, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(), List.of("tc.research_page.COREFILL.1"), 0));
        add(new Research("COREEMPTY", "GOLEMANCY", new AspectList().add(Aspects.VOID, 3).add(Aspects.EXCHANGE, 3).add(Aspects.GREED, 3), -5, 1, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(), List.of("tc.research_page.COREEMPTY.1"), 0));
        add(new Research("CORESORTING", "GOLEMANCY", new AspectList().add(Aspects.VOID, 3).add(Aspects.EXCHANGE, 3).add(Aspects.GREED, 3).add(Aspects.HUNGER, 3), -7, 2, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREEMPTY", "COREFILL", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.CORESORTING.1"), 0));
        add(new Research("COREUSE", "GOLEMANCY", new AspectList().add(Aspects.TOOL, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MECHANISM, 3).add(Aspects.MAN, 3), -7, 0, 3,
                null, "itemGolemCore", List.of(Research.Mark.CONCEALED),
                List.of("COREEMPTY", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.COREUSE.1", "tc.research_page.COREUSE.2"), 0));
        add(new Research("COREHARVEST", "GOLEMANCY", new AspectList().add(Aspects.HARVEST, 6).add(Aspects.CROP, 3).add(Aspects.TRAVEL, 3), -2, 5, 2,
                null, "itemGolemCore", List.of(Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(), List.of("tc.research_page.COREHARVEST.1"), 0));
        add(new Research("COREFISHING", "GOLEMANCY", new AspectList().add(Aspects.WATER, 3).add(Aspects.HARVEST, 3).add(Aspects.BEAST, 3).add(Aspects.HUNGER, 3), -2, 7, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREHARVEST", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.COREFISHING.1"), 0));
        add(new Research("CORELUMBER", "GOLEMANCY", new AspectList().add(Aspects.TREE, 6).add(Aspects.HARVEST, 3).add(Aspects.TOOL, 3).add(Aspects.ENERGY, 3), -1, 7, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREHARVEST", "ELEMENTALAXE"), List.of(), List.of(), List.of("tc.research_page.CORELUMBER.1"), 0));
        add(new Research("COREGUARD", "GOLEMANCY", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.TRAP, 3).add(Aspects.SENSES, 3), -4, 5, 2,
                null, "itemGolemCore", List.of(Research.Mark.CONCEALED),
                List.of("COREGATHER"), List.of(), List.of(), List.of("tc.research_page.COREGUARD.1"), 0));
        add(new Research("COREBUTCHER", "GOLEMANCY", new AspectList().add(Aspects.WEAPON, 3).add(Aspects.BEAST, 3).add(Aspects.SENSES, 3).add(Aspects.HARVEST, 3), -3, 7, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("COREGUARD", "COREHARVEST"), List.of(), List.of(), List.of("tc.research_page.COREBUTCHER.1"), 1));
        add(new Research("CORELIQUID", "GOLEMANCY", new AspectList().add(Aspects.WATER, 3).add(Aspects.EXCHANGE, 3).add(Aspects.TRAVEL, 3), -7, 4, 2,
                null, "itemGolemCore", List.of(Research.Mark.CONCEALED),
                List.of("COREFILL"), List.of(), List.of(), List.of("tc.research_page.CORELIQUID.1"), 0));
        add(new Research("COREALCHEMY", "GOLEMANCY", new AspectList().add(Aspects.WATER, 3).add(Aspects.TRAVEL, 3).add(Aspects.MAGIC, 3).add(Aspects.ENERGY, 3), -9, 3, 2,
                null, "itemGolemCore", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("CORELIQUID", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.COREALCHEMY.1", "tc.research_page.COREALCHEMY.2"), 0));
        add(new Research("ADVANCEDGOLEM", "GOLEMANCY", new AspectList().add(Aspects.LIFE, 3).add(Aspects.ENERGY, 3).add(Aspects.MIND, 6).add(Aspects.SENSES, 3), 8, 0, 2,
                null, null, List.of(Research.Mark.CONCEALED),
                List.of("INFUSION", "UPGRADEAIR", "UPGRADEEARTH", "UPGRADEFIRE", "UPGRADEWATER", "UPGRADEORDER", "UPGRADEENTROPY"), List.of(), List.of(), List.of("tc.research_page.ADVANCEDGOLEM.1"), 5));
        add(new Research("ELDRITCHMINOR", "ELDRITCH", new AspectList(), 1, 0, 0,
                "textures/misc/r_eldritchminor.png", null, List.of(Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ELDRITCHMINOR.1"), 0));
        add(new Research("OCULUS", "ELDRITCH", new AspectList().add(Aspects.MIND, 3).add(Aspects.DARKNESS, 3).add(Aspects.EXCHANGE, 3).add(Aspects.TRAVEL, 6).add(Aspects.ELDRITCH, 6), -2, 2, 1,
                null, "itemEldritchObject", List.of(Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.CONCEALED),
                List.of("CRIMSON", "ELDRITCHMAJOR"), List.of(), List.of(), List.of("tc.research_page.OCULUS.1", "tc.research_page.OCULUS.2"), 6));
        add(new Research("ENTEROUTER", "ELDRITCH", new AspectList(), -3, 4, 1,
                "textures/misc/r_outer.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.HIDDEN),
                List.of("OCULUS"), List.of(), List.of(), List.of("tc.research_page.ENTEROUTER.1"), 0));
        add(new Research("OUTERREV", "ELDRITCH", new AspectList().add(Aspects.ELDRITCH, 4).add(Aspects.MIND, 4), -5, 3, 1,
                "textures/misc/r_outerrev.png", null, List.of(Research.Mark.SPECIAL, Research.Mark.SECONDARY, Research.Mark.LOST),
                List.of("ENTEROUTER"), List.of(), List.of(), List.of("tc.research_page.OUTERREV.1"), 0));
        add(new Research("PRIMPEARL", "ELDRITCH", new AspectList().add(Aspects.AIR, 8).add(Aspects.EARTH, 8).add(Aspects.FIRE, 8).add(Aspects.WATER, 8).add(Aspects.ORDER, 8).add(Aspects.ENTROPY, 8), 0, 4, 1,
                null, "itemEldritchObject", List.of(Research.Mark.SPECIAL, Research.Mark.SECONDARY, Research.Mark.LOST),
                List.of("ELDRITCHMINOR"), List.of(), List.of(), List.of("tc.research_page.PRIMPEARL.1", "tc.research_page.PRIMPEARL.2"), 0));
        add(new Research("PRIMNODE", "ELDRITCH", new AspectList().add(Aspects.AURA, 1).add(Aspects.MAGIC, 1).add(Aspects.ORDER, 1).add(Aspects.ENTROPY, 1), 0, 6, 1,
                "textures/misc/r_nodes_2.png", null, List.of(Research.Mark.SECONDARY),
                List.of("PRIMPEARL"), List.of(), List.of(), List.of("tc.research_page.PRIMNODE.1"), 1));
        add(new Research("ADVALCHEMYFURNACE", "ELDRITCH", new AspectList().add(Aspects.AURA, 1).add(Aspects.MAGIC, 1).add(Aspects.ORDER, 1).add(Aspects.ENTROPY, 1), -2, 6, 1,
                null, "blockMetalDevice", List.of(Research.Mark.SECONDARY),
                List.of("PRIMPEARL", "DISTILESSENTIA", "VISPOWER"), List.of(), List.of(), List.of("tc.research_page.ADVALCHEMYFURNACE.1", "tc.research_page.ADVALCHEMYFURNACE.2"), 0));
        add(new Research("PRIMALCRUSHER", "ELDRITCH", new AspectList().add(Aspects.MINE, 6).add(Aspects.TOOL, 6).add(Aspects.ENTROPY, 6).add(Aspects.VOID, 6).add(Aspects.WEAPON, 6).add(Aspects.ELDRITCH, 6).add(Aspects.GREED, 6), 2, 5, 2,
                null, "itemPrimalCrusher", List.of(Research.Mark.CONCEALED),
                List.of("PRIMPEARL"), List.of("VOIDMETAL", "ELEMENTALPICK", "ELEMENTALSHOVEL"), List.of(), List.of("tc.research_page.PRIMALCRUSHER.1", "tc.research_page.PRIMALCRUSHER.2"), 0));
        add(new Research("VOIDMETAL", "ELDRITCH", new AspectList().add(Aspects.METAL, 3).add(Aspects.ELDRITCH, 3).add(Aspects.DARKNESS, 3).add(Aspects.VOID, 5), 2, -2, 2,
                null, "itemResource", List.of(),
                List.of("THAUMIUM", "ELDRITCHMINOR"), List.of(), List.of(), List.of("tc.research_page.VOIDMETAL.1", "tc.research_page.VOIDMETAL.2"), 0));
        add(new Research("ESSENTIARESERVOIR", "ELDRITCH", new AspectList().add(Aspects.WATER, 5).add(Aspects.VOID, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MAGIC, 5).add(Aspects.VOID, 5), 4, -3, 2,
                null, "blockEssentiaReservoir", List.of(),
                List.of("VOIDMETAL", "CENTRIFUGE", "INFUSION"), List.of(), List.of(), List.of("tc.research_page.ESSENTIARESERVOIR.1", "tc.research_page.ESSENTIARESERVOIR.2"), 0));
        add(new Research("CAP_void", "ELDRITCH", new AspectList().add(Aspects.VOID, 5).add(Aspects.ELDRITCH, 5).add(Aspects.TOOL, 3).add(Aspects.MAGIC, 3).add(Aspects.AURA, 3), 5, -1, 3,
                null, "itemWandCap", List.of(Research.Mark.CONCEALED),
                List.of("CAP_thaumium", "VOIDMETAL"), List.of(), List.of(), List.of("tc.research_page.CAP_void.1"), 1));
        add(new Research("ARMORVOIDFORTRESS", "ELDRITCH", new AspectList().add(Aspects.ARMOR, 5).add(Aspects.ELDRITCH, 3).add(Aspects.CLOTH, 3).add(Aspects.DARKNESS, 3).add(Aspects.VOID, 5), 0, -3, 3,
                null, "itemHelmVoidRobe", List.of(Research.Mark.SECONDARY, Research.Mark.CONCEALED),
                List.of("VOIDMETAL", "ENCHFABRIC", "ELDRITCHMAJOR"), List.of(), List.of(), List.of("tc.research_page.ARMORVOIDFORTRESS.1"), 0));
        add(new Research("FOCUSPRIMAL", "ELDRITCH", new AspectList().add(Aspects.AIR, 6).add(Aspects.WATER, 6).add(Aspects.FIRE, 6).add(Aspects.EARTH, 6).add(Aspects.ORDER, 6).add(Aspects.ENTROPY, 6).add(Aspects.MAGIC, 6), 4, 1, 2,
                null, "focusPrimal", List.of(Research.Mark.CONCEALED),
                List.of("ELDRITCHMINOR"), List.of(), List.of(), List.of("tc.research_page.FOCUSPRIMAL.1"), 2));
        add(new Research("SANITYCHECK", "ELDRITCH", new AspectList().add(Aspects.MIND, 5).add(Aspects.ELDRITCH, 3).add(Aspects.SENSES, 5), 2, 2, 1,
                null, "itemSanityChecker", List.of(),
                List.of("ELDRITCHMINOR"), List.of(), List.of(), List.of("tc.research_page.SANITYCHECK.1"), 0));
        add(new Research("ROD_primal_staff", "ELDRITCH", new AspectList().add(Aspects.AIR, 9).add(Aspects.EARTH, 9).add(Aspects.FIRE, 9).add(Aspects.WATER, 9).add(Aspects.ORDER, 9).add(Aspects.ENTROPY, 9).add(Aspects.TOOL, 9).add(Aspects.MAGIC, 12), 6, 2, 3,
                null, "itemWandRod", List.of(Research.Mark.HIDDEN),
                List.of("FOCUSPRIMAL"), List.of("ROD_silverwood_staff", "ROD_bone_staff", "ROD_greatwood_staff", "ROD_blaze_staff", "ROD_reed_staff", "ROD_obsidian_staff", "ROD_quartz_staff", "ROD_ice_staff"), List.of(), List.of("tc.research_page.ROD_primal_staff.1"), 3));
        add(new Research("ELDRITCHMAJOR", "ELDRITCH", new AspectList(), -1, 0, 0,
                "textures/misc/r_eldritchmajor.png", null, List.of(Research.Mark.STUB, Research.Mark.ROUND, Research.Mark.SPECIAL, Research.Mark.HIDDEN),
                List.of(), List.of(), List.of(), List.of("tc.research_page.ELDRITCHMAJOR.1", "tc.research_page.ELDRITCHMAJOR.2"), 0));
    }
}
