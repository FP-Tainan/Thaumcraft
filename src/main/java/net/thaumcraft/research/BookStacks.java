package net.thaumcraft.research;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** As pilhas que o {@code ConfigResearch} do original montava à mão para os ícones de algumas pesquisas. */
public final class BookStacks {
    private BookStacks() {
    }

    /** O golem avançado (o {@code itemGolemPlacer} com a marca "advanced"). */
    public static ItemStack advancedGolem() {
        ItemStack stack = new ItemStack(TCItems.GOLEM_PLACERS.values().iterator().next());
        stack.set(TCComponents.GOLEM_ADVANCED, net.minecraft.util.Unit.INSTANCE);
        return stack;
    }

    /** O pote de nó. */
    public static ItemStack nodeJar() {
        return new ItemStack(TCItems.NODE_JAR);
    }

    /** As notas de pesquisa desconhecidas (o {@code itemResearchNotes} 42). */
    public static ItemStack unknownNotes() {
        ItemStack stack = new ItemStack(TCItems.RESEARCH_NOTES);
        stack.set(TCComponents.UNKNOWN_NOTE, net.minecraft.util.Unit.INSTANCE);
        return stack;
    }

    /** A mesa de pesquisa (o {@code blockTable} 1). */
    public static ItemStack researchTable() {
        return new ItemStack(net.thaumcraft.registry.TCItems.RESEARCH_TABLE);
    }
}
