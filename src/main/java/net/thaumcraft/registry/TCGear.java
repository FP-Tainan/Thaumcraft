package net.thaumcraft.registry;

/**
 * As ferramentas e armaduras do mod, na ordem em que entram na aba do criativo.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia6-ferramentas.js}.
 */
public final class TCGear {
    /** nome, material (thaumium ou void) e tipo de peça. */
    public record Piece(String name, String material, String kind) {
    }

    public static final Piece[] PIECES = {
            new Piece("thaumium_pickaxe", "thaumium", "pickaxe"),
            new Piece("thaumium_axe", "thaumium", "axe"),
            new Piece("thaumium_shovel", "thaumium", "shovel"),
            new Piece("thaumium_hoe", "thaumium", "hoe"),
            new Piece("thaumium_sword", "thaumium", "sword"),
            new Piece("thaumium_helmet", "thaumium", "helmet"),
            new Piece("thaumium_chestplate", "thaumium", "chestplate"),
            new Piece("thaumium_leggings", "thaumium", "leggings"),
            new Piece("thaumium_boots", "thaumium", "boots"),
            new Piece("void_pickaxe", "void", "pickaxe"),
            new Piece("void_axe", "void", "axe"),
            new Piece("void_shovel", "void", "shovel"),
            new Piece("void_hoe", "void", "hoe"),
            new Piece("void_sword", "void", "sword"),
            new Piece("void_helmet", "void", "helmet"),
            new Piece("void_chestplate", "void", "chestplate"),
            new Piece("void_leggings", "void", "leggings"),
            new Piece("void_boots", "void", "boots"),
    };

    private TCGear() {
    }
}
