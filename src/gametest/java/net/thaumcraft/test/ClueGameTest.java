package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.ResearchNotes;
import net.thaumcraft.research.ResearchTriggers;
import net.thaumcraft.research.Researches;
import net.thaumcraft.research.ScanManager;

import java.util.Collections;

/**
 * As pesquisas escondidas da 4.2.3.5: o que as desperta ({@code createClue}), quando elas aparecem no livro, e a nota de
 * conhecimento desconhecido feita de nove fragmentos.
 */
public class ClueGameTest {
    /** Os gatilhos vieram do ConfigResearch do jar: vinte e sete pesquisas, com item, criatura ou aspecto. */
    @GameTest
    public void triggersCameFromTheJar(GameTestHelper helper) {
        var bow = ResearchTriggers.of("BONEBOW");
        if (bow == null || bow.items().stream().noneMatch(t -> t.test(new ItemStack(Items.BONE)))) helper.fail("o arco de osso desperta com osso");
        var bat = ResearchTriggers.of("FOCUSHELLBAT");
        if (bat == null || !bat.entities().contains("thaumcraft:firebat") || !bat.aspects().contains(Aspects.FIRE)) {
            helper.fail("o foco do morcego desperta com o morcego de fogo e com ignis");
        }
        var hat = ResearchTriggers.of("TINYHAT");
        if (hat == null || hat.items().stream().noneMatch(t -> t.test(new ItemStack(Items.WOOL.red())))) helper.fail("qualquer lã desperta a cartola");
        helper.succeed();
    }

    /** Pista por item: o osso examinado dá a pista do arco de osso, e a pesquisa passa a aparecer no livro. */
    @GameTest
    public void boneGivesTheBoneBowClue(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var research = Researches.get("BONEBOW");
        if (ResearchManager.isVisible(Knowledges.of(player), research)) helper.fail("escondida aparecendo antes da pista");
        if (!ResearchManager.createClue(player, new ItemStack(Items.BONE), new AspectList())) helper.fail("o osso não deu pista");
        var knowledge = Knowledges.of(player);
        if (!knowledge.hasResearch("@BONEBOW")) helper.fail("a pista devia ser a do arco de osso");
        if (!ResearchManager.isVisible(knowledge, research)) helper.fail("com a pista a pesquisa devia aparecer");
        // a mesma pista não sai duas vezes
        if (ResearchManager.createClue(player, new ItemStack(Items.BONE), new AspectList())) helper.fail("pista repetida");
        helper.succeed();
    }

    /** Pista por criatura e por aspecto, como no original. */
    @GameTest
    public void creaturesAndAspectsGiveClues(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ResearchManager.createClue(player, "thaumcraft:firebat", new AspectList());
        if (!Knowledges.of(player).hasResearch("@FOCUSHELLBAT")) helper.fail("o morcego de fogo devia dar a pista do foco");
        ResearchManager.createClue(player, "minecraft:pig", new AspectList().add(Aspects.METAL, 2));
        if (!Knowledges.of(player).hasResearch("@THAUMIUM")) helper.fail("metallum ganho devia dar a pista do táumio");
        helper.succeed();
    }

    /** O exame de verdade chama a pista, e o que já foi examinado não se examina de novo. */
    @GameTest
    public void scanningCallsTheClue(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack bone = new ItemStack(Items.BONE);
        AspectList aspects = ObjectAspects.of(bone);
        var knowledge = Knowledges.of(player);
        for (var aspect : aspects.getAspects()) {
            knowledge.discover(aspect);
            for (var parent : aspect.isPrimal() ? new net.thaumcraft.api.aspects.Aspect[0] : aspect.components()) knowledge.discover(parent);
        }
        Knowledges.save(player, knowledge);
        String key = ScanManager.keyOf(bone);
        if (!ScanManager.scan(player, key, aspects, bone.getHoverName(), bone).scanned()) helper.fail("o osso devia ser lido");
        if (!Knowledges.of(player).hasResearch("@BONEBOW")) helper.fail("examinar o osso devia dar a pista do arco");
        if (ScanManager.scan(player, key, aspects, bone.getHoverName(), bone).scanned()) helper.fail("examinou duas vezes a mesma coisa");
        helper.succeed();
    }

    /** Nove fragmentos fazem a nota desconhecida. */
    @GameTest
    public void nineFragmentsMakeAnUnknownNote(GameTestHelper helper) {
        ItemStack fragment = new ItemStack(TCResources.get("knowledge_fragment"));
        var input = CraftingInput.of(3, 3, Collections.nCopies(9, fragment));
        var found = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (found.isEmpty()) helper.fail("nove fragmentos não fecham receita");
        ItemStack out = found.get().value().assemble(input);
        if (!out.is(TCItems.RESEARCH_NOTES) || !out.has(TCComponents.UNKNOWN_NOTE)) helper.fail("devia sair a nota desconhecida, saiu " + out);
        helper.succeed();
    }

    /** Sem pesquisa escondida a achar, a teoria é falsa: a nota some e devolve de sete a nove fragmentos. */
    @GameTest
    public void falseTheoryGivesFragmentsBack(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var knowledge = Knowledges.of(player);
        // tudo sabido: não sobra escondida nenhuma
        for (var research : Researches.ALL.values()) knowledge.completeResearch(research.key());
        Knowledges.save(player, knowledge);
        if (!ResearchManager.findHiddenResearch(player).equals("FAIL")) helper.fail("sabendo tudo, não devia achar nada");
        ItemStack note = new ItemStack(TCItems.RESEARCH_NOTES);
        note.set(TCComponents.UNKNOWN_NOTE, net.minecraft.util.Unit.INSTANCE);
        player.setItemInHand(InteractionHand.MAIN_HAND, note);
        var box = player.getBoundingBox().inflate(3);
        var before = new java.util.HashSet<>(helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box));
        note.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        if (!player.getMainHandItem().isEmpty()) helper.fail("a nota falsa devia sumir");
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box);
        drops.removeAll(before);
        int count = drops.stream().filter(e -> e.getItem().is(TCResources.get("knowledge_fragment"))).mapToInt(e -> e.getItem().getCount()).sum();
        if (count < 7 || count > 9) helper.fail("devia devolver de sete a nove fragmentos, devolveu " + count);
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    /** Com pesquisa a achar, a nota vira a nota daquela pesquisa. */
    @GameTest
    public void unknownNoteRevealsHiddenResearch(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        String key = ResearchManager.findHiddenResearch(player);
        if (key.equals("FAIL")) helper.fail("um jogador novo devia ter alguma escondida a achar");
        if (!Researches.get(key).is(net.thaumcraft.research.Research.Mark.HIDDEN)) helper.fail(key + " não é escondida");
        ItemStack note = new ItemStack(TCItems.RESEARCH_NOTES);
        note.set(TCComponents.UNKNOWN_NOTE, net.minecraft.util.Unit.INSTANCE);
        player.setItemInHand(InteractionHand.MAIN_HAND, note);
        note.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        var written = ResearchNotes.get(player.getMainHandItem());
        if (written == null || !Researches.get(written.key()).is(net.thaumcraft.research.Research.Mark.HIDDEN)) {
            helper.fail("a nota devia virar a de uma pesquisa escondida");
        }
        helper.succeed();
    }

    /** O fragmento lido dá um ou dois pontos de cada primário. */
    @GameTest
    public void fragmentGivesPrimalPoints(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack fragment = new ItemStack(TCResources.get("knowledge_fragment"), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, fragment);
        fragment.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        var knowledge = Knowledges.of(player);
        for (var aspect : Aspects.primals()) {
            int p = knowledge.points(aspect);
            if (p < 1 || p > 2) helper.fail(aspect.tag() + " devia ter um ou dois pontos, tem " + p);
        }
        if (player.getMainHandItem().getCount() != 1) helper.fail("o fragmento devia ser gasto");
        helper.succeed();
    }
}
