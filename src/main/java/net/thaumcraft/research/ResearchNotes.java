package net.thaumcraft.research;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.item.ScribingToolsItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * As notas de pesquisa: a parte do {@code ResearchManager} da 4.2.3.5 que cuida delas, descompilada.
 *
 * <p>Uma pesquisa de verdade — das que não são "de lado" — não se compra com pontos: clicando nela no livro,
 * com papel e tinta no inventário, sai uma nota. A nota tem um tabuleiro de hexágonos com os aspectos da
 * pesquisa espalhados pela borda, e quem pesquisa tem de ligá-los uns aos outros, na mesa de pesquisa,
 * escrevendo aspectos nas casas do meio: dois aspectos se ligam quando um é feito do outro. Quando todos os
 * da borda ficam ligados, a nota vira uma descoberta, e lida ela ensina a pesquisa.
 */
public final class ResearchNotes {
    /** A cor da nota sem pesquisa, o 10066329 do original. */
    public static final int DEFAULT_COLOR = 0x999999;

    private ResearchNotes() {
    }

    public static ResearchNote get(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(TCComponents.RESEARCH_NOTE);
    }

    /** Põe a nota no item, e com ela a cor da fita e se é descoberta, para o desenho do item. */
    public static void set(ItemStack stack, ResearchNote note) {
        stack.set(TCComponents.RESEARCH_NOTE, note);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(note.complete()),
                List.of(), List.of(note.color())));
    }

    /** O {@code createNote}: o tabuleiro de uma pesquisa, sorteado. */
    public static ItemStack create(String key, Random random) {
        Research research = Researches.get(key);
        if (research == null) return ItemStack.EMPTY;
        Aspect primary = research.primaryTag();
        if (primary == null) return ItemStack.EMPTY;

        int radius = 1 + Math.min(3, research.complexity());
        Map<String, Hex> hexes = new java.util.LinkedHashMap<>(Hex.generate(radius));
        List<Hex> outer = Hex.distributeRingRandomly(radius, research.tags().size(), random);
        Map<String, ResearchNote.Cell> cells = new java.util.LinkedHashMap<>();
        for (Hex hex : hexes.values()) cells.put(hex.key(), new ResearchNote.Cell(hex.q(), hex.r(), 0, ""));
        List<Aspect> aspects = research.tags().getAspects();
        for (int count = 0; count < outer.size(); count++) {
            Hex hex = outer.get(count);
            hexes.put(hex.key(), hex);
            cells.put(hex.key(), new ResearchNote.Cell(hex.q(), hex.r(), 1, aspects.get(count).tag()));
        }

        // nas pesquisas mais difíceis, umas casas do meio somem, sem nunca isolar um aspecto da borda
        if (research.complexity() > 1) {
            int blanks = research.complexity() * 2;
            Hex[] temp = hexes.values().toArray(new Hex[0]);
            while (blanks > 0) {
                Hex pick = temp[random.nextInt(temp.length)];
                ResearchNote.Cell cell = cells.get(pick.key());
                if (cell == null || cell.type() != 0) continue;
                boolean ok = true;
                for (int n = 0; n < 6; n++) {
                    Hex neighbour = pick.neighbour(n);
                    ResearchNote.Cell around = cells.get(neighbour.key());
                    if (hexes.containsKey(neighbour.key()) && around != null && around.type() == 1) {
                        int count = 0;
                        for (int q = 0; q < 6; q++) {
                            if (hexes.containsKey(hexes.get(neighbour.key()).neighbour(q).key())) count++;
                            if (count >= 2) break;
                        }
                        if (count < 2) {
                            ok = false;
                            break;
                        }
                    }
                }
                if (ok) {
                    hexes.remove(pick.key());
                    cells.remove(pick.key());
                    temp = hexes.values().toArray(new Hex[0]);
                    blanks--;
                }
            }
        }

        ItemStack stack = new ItemStack(TCItems.RESEARCH_NOTES);
        set(stack, new ResearchNote(key, primary.color(), false, 0, new ArrayList<>(cells.values())));
        return stack;
    }

    /** Os dois aspectos se ligam? Um tem de ser feito do outro, e quem pesquisa tem de conhecer os dois. */
    public static boolean connects(PlayerKnowledge knowledge, Aspect a, Aspect b) {
        if (a == null || b == null) return false;
        if (!knowledge.hasDiscovered(a) || !knowledge.hasDiscovered(b)) return false;
        return !a.isPrimal() && (a.components()[0] == b || a.components()[1] == b)
                || !b.isPrimal() && (b.components()[0] == a || b.components()[1] == a);
    }

    /**
     * O {@code checkResearchCompletion}: partindo do primeiro aspecto da borda, segue as ligações; se todos
     * os da borda forem alcançados, a nota fica resolvida e as casas que não fazem parte do caminho somem.
     *
     * @return a nota resolvida, ou nulo se ainda falta ligar alguma coisa
     */
    public static ResearchNote checkCompletion(ResearchNote note, PlayerKnowledge knowledge) {
        Map<String, ResearchNote.Cell> cells = note.byKey();
        List<String> checked = new ArrayList<>();
        List<String> main = new ArrayList<>();
        List<String> remains = new ArrayList<>();
        for (ResearchNote.Cell cell : cells.values()) {
            if (cell.type() == 1) main.add(cell.hex().key());
        }
        for (ResearchNote.Cell cell : cells.values()) {
            if (cell.type() == 1) {
                main.remove(cell.hex().key());
                follow(cells, cell.hex(), checked, main, remains, knowledge);
                break;
            }
        }
        if (!main.isEmpty()) return null;
        cells.values().removeIf(cell -> cell.type() != 1 && !remains.contains(cell.hex().key()));
        return note.with(cells, true, note.copies());
    }

    private static void follow(Map<String, ResearchNote.Cell> cells, Hex hex, List<String> checked, List<String> main,
                               List<String> remains, PlayerKnowledge knowledge) {
        checked.add(hex.key());
        for (int a = 0; a < 6; a++) {
            Hex target = hex.neighbour(a);
            ResearchNote.Cell there = cells.get(target.key());
            if (checked.contains(target.key()) || there == null || there.type() < 1) continue;
            if (!connects(knowledge, cells.get(hex.key()).aspectOrNull(), there.aspectOrNull())) continue;
            remains.add(target.key());
            if (there.type() == 1) main.remove(target.key());
            follow(cells, target, checked, main, remains, knowledge);
        }
    }

    // ----------------------------------------------------------------- papel e tinta

    /** O {@code consumeInkFromPlayer}: a primeira ferramenta de escrita com tinta no inventário. */
    public static boolean consumeInkFromPlayer(Player player, boolean doit) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ScribingToolsItem && stack.getDamageValue() < stack.getMaxDamage()) {
                if (doit) stack.setDamageValue(stack.getDamageValue() + 1);
                return true;
            }
        }
        return false;
    }

    /** O {@code consumeInkFromTable}. */
    public static boolean consumeInkFromTable(ItemStack stack, boolean doit) {
        if (!(stack.getItem() instanceof ScribingToolsItem) || stack.getDamageValue() >= stack.getMaxDamage()) {
            return false;
        }
        if (doit) stack.setDamageValue(stack.getDamageValue() + 1);
        return true;
    }

    /** Onde está, no inventário, uma nota desta pesquisa. */
    public static int slotOf(Player player, String key) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ResearchNote note = get(player.getInventory().getItem(slot));
            if (note != null && note.key().equals(key)) return slot;
        }
        return -1;
    }

    /** O jogador tem papel e tinta para escrever uma nota? */
    public static boolean hasScribeStuff(Player player) {
        return consumeInkFromPlayer(player, false) && player.getInventory().contains(new ItemStack(Items.PAPER));
    }

    /** O {@code createResearchNoteForPlayer}: gasta papel e tinta e entrega a nota. */
    public static boolean giveNote(ServerPlayer player, String key) {
        if (slotOf(player, key) >= 0) return false;
        if (!hasScribeStuff(player)) return false;
        ItemStack note = create(key, new Random(player.getRandom().nextLong()));
        if (note.isEmpty()) return false;
        consumeInkFromPlayer(player, true);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                break;
            }
        }
        if (!player.getInventory().add(note)) player.drop(note, false);
        player.inventoryMenu.broadcastChanges();
        return true;
    }
}
