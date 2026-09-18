package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ResearchTableBlock;
import net.thaumcraft.block.entity.ResearchTableBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.Hex;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.ResearchNote;
import net.thaumcraft.research.ResearchNotes;
import net.thaumcraft.research.Researches;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A mesa de pesquisa e as notas têm de seguir o {@code ResearchManager} e o {@code TileResearchTable} da 4.2.3.5.
 */
public class ResearchTableGameTest {
    /** Uma pesquisa de verdade (não de lado), com aspectos, ao alcance de quem começa. */
    private static Research primary(PlayerKnowledge knowledge) {
        for (Research research : Researches.ALL.values()) {
            if (research.tags().isEmpty() || research.is(Research.Mark.SECONDARY)) continue;
            if (!ResearchManager.canUnlock(knowledge, research) || knowledge.hasResearch(research.key())) continue;
            return research;
        }
        return null;
    }

    @GameTest
    public void aNoteHasTheResearchOnItsRim(GameTestHelper helper) {
        for (Research research : Researches.ALL.values()) {
            if (research.tags().isEmpty() || research.primaryTag() == null) continue;
            ItemStack stack = ResearchNotes.create(research.key(), new Random(7));
            ResearchNote note = ResearchNotes.get(stack);
            if (note == null) helper.fail("sem nota para " + research.key());
            int radius = 1 + Math.min(3, research.complexity());
            long rim = note.cells().stream().filter(c -> c.type() == 1).count();
            if (rim != research.tags().size()) helper.fail(research.key() + ": a borda devia ter os " + research.tags().size() + " aspectos");
            for (ResearchNote.Cell cell : note.cells()) {
                int distance = (Math.abs(cell.q()) + Math.abs(cell.r()) + Math.abs(cell.q() + cell.r())) / 2;
                if (distance > radius) helper.fail(research.key() + ": casa fora do tabuleiro");
                if (cell.type() == 1 && distance != radius) helper.fail(research.key() + ": aspecto pedido fora da borda");
            }
            if (note.color() != research.primaryTag().color()) helper.fail("a cor da nota é a do aspecto principal");
        }
        helper.succeed();
    }

    @GameTest
    public void aspectsConnectWhenOneIsMadeOfTheOther(GameTestHelper helper) {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        for (Aspect aspect : List.of(Aspects.AIR, Aspects.FIRE, Aspects.LIGHT, Aspects.WATER)) knowledge.discover(aspect);
        // luz é ar com fogo: ar ao lado de luz ao lado de fogo fecha
        List<ResearchNote.Cell> cells = new ArrayList<>(List.of(
                new ResearchNote.Cell(-1, 0, 1, "aer"), new ResearchNote.Cell(0, 0, 2, "lux"),
                new ResearchNote.Cell(1, 0, 1, "ignis"), new ResearchNote.Cell(0, 1, 0, "")));
        ResearchNote note = new ResearchNote("X", 0, false, 0, cells);
        ResearchNote solved = ResearchNotes.checkCompletion(note, knowledge);
        if (solved == null || !solved.complete()) helper.fail("ar-luz-fogo devia fechar a nota");
        if (solved.byKey().containsKey(new Hex(0, 1).key())) helper.fail("a casa vazia de fora do caminho some");
        // água ao lado de luz não se liga
        List<ResearchNote.Cell> broken = new ArrayList<>(cells);
        broken.set(1, new ResearchNote.Cell(0, 0, 2, "aqua"));
        if (ResearchNotes.checkCompletion(new ResearchNote("X", 0, false, 0, broken), knowledge) != null) {
            helper.fail("água não liga ar e fogo");
        }
        helper.succeed();
    }

    @GameTest
    public void scribingToolsJoinTwoTables(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos a = new BlockPos(1, 2, 1), b = new BlockPos(2, 2, 1);
        helper.setBlock(a, TCBlocks.TABLE.defaultBlockState());
        helper.setBlock(b, TCBlocks.TABLE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.SCRIBING_TOOLS));
        BlockPos abs = helper.absolutePos(a);
        var result = new ItemStack(TCItems.SCRIBING_TOOLS).useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
        if (!result.consumesAction()) helper.fail("as ferramentas deviam juntar as mesas");
        var main = helper.getLevel().getBlockState(abs);
        if (!main.is(TCBlocks.RESEARCH_TABLE) || main.getValue(ResearchTableBlock.PART) != ResearchTableBlock.Part.MAIN) {
            helper.fail("onde se clicou fica a metade principal");
        }
        if (!(helper.getLevel().getBlockEntity(abs) instanceof ResearchTableBlockEntity table)
                || !table.getItem(ResearchTableBlockEntity.INK).is(TCItems.SCRIBING_TOOLS)) {
            helper.fail("as ferramentas ficam na mesa");
        }
        // quebrando a outra metade, a principal volta a ser mesa
        helper.setBlock(b, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        if (!helper.getLevel().getBlockState(abs).is(TCBlocks.TABLE)) helper.fail("sem a outra metade, volta a ser mesa");
        helper.succeed();
    }

    @GameTest
    public void writingOnTheTableCostsAPointAndInk(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos a = new BlockPos(1, 2, 1);
        helper.setBlock(a, TCBlocks.TABLE.defaultBlockState());
        helper.setBlock(a.east(), TCBlocks.TABLE.defaultBlockState());
        ResearchTableBlock.form(helper.getLevel(), helper.absolutePos(a), Direction.EAST);
        ResearchTableBlockEntity table = (ResearchTableBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(a));

        PlayerKnowledge knowledge = Knowledges.of(player);
        knowledge.discover(Aspects.AIR);
        knowledge.award(Aspects.AIR, 10);
        Knowledges.save(player, knowledge);
        int before = Knowledges.of(player).points(Aspects.AIR);

        ItemStack note = new ItemStack(TCItems.RESEARCH_NOTES);
        ResearchNotes.set(note, new ResearchNote("X", 0, false, 0, List.of(
                new ResearchNote.Cell(0, 0, 0, ""), new ResearchNote.Cell(1, 0, 1, "ignis"),
                new ResearchNote.Cell(-1, 0, 1, "aqua"))));
        table.setItem(ResearchTableBlockEntity.INK, new ItemStack(TCItems.SCRIBING_TOOLS));
        table.setItem(ResearchTableBlockEntity.NOTE, note);
        table.placeAspect(0, 0, Aspects.AIR, player);

        ResearchNote written = ResearchNotes.get(table.getItem(ResearchTableBlockEntity.NOTE));
        ResearchNote.Cell cell = written.byKey().get(new Hex(0, 0).key());
        if (cell.type() != 2 || !cell.aspect().equals("aer")) helper.fail("o ar devia ficar escrito na casa");
        if (Knowledges.of(player).points(Aspects.AIR) != before - 1) helper.fail("escrever cobra um ponto");
        if (table.getItem(ResearchTableBlockEntity.INK).getDamageValue() != 1) helper.fail("escrever gasta tinta");
        // e apagar devolve a casa vazia, gastando mais tinta
        table.placeAspect(0, 0, null, player);
        if (ResearchNotes.get(table.getItem(ResearchTableBlockEntity.NOTE)).byKey().get("0:0").type() != 0) {
            helper.fail("apagar esvazia a casa");
        }
        if (table.getItem(ResearchTableBlockEntity.INK).getDamageValue() != 2) helper.fail("apagar também gasta tinta");
        helper.succeed();
    }

    @GameTest
    public void theBookWritesANoteForARealResearch(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        PlayerKnowledge knowledge = Knowledges.of(player);
        ResearchManager.grantStarters(knowledge);
        Knowledges.save(player, knowledge);
        Research research = primary(knowledge);
        if (research == null) helper.fail("nenhuma pesquisa de verdade ao alcance de quem começa");
        // sem papel e tinta, nada
        if (ResearchManager.request(player, research.key())) helper.fail("sem papel e tinta não sai nota");
        player.getInventory().add(new ItemStack(Items.PAPER));
        player.getInventory().add(new ItemStack(TCItems.SCRIBING_TOOLS));
        if (!ResearchManager.request(player, research.key())) helper.fail("com papel e tinta sai a nota");
        if (ResearchNotes.slotOf(player, research.key()) < 0) helper.fail("a nota vai para o inventário");
        if (Knowledges.of(player).hasResearch(research.key())) helper.fail("a nota não ensina sozinha");
        if (player.getInventory().contains(new ItemStack(Items.PAPER))) helper.fail("o papel é gasto");
        helper.succeed();
    }
}
