package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.ObjectAspects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Toda coisa do jogo de hoje tem de ter aspecto: sem isso não dá para examinar no thaumômetro nem usar na alquimia.
 *
 * <p>O Thaumcraft original só conhecia o jogo de 2014; o que veio depois está anotado em {@code NewItemsAspectsTable}
 * (gerado por {@code scratchpad/aspectos-novos.js}). Ficam de fora, como no original, as peças que só existem no modo
 * criativo — os blocos de comando, a barreira, a luz, os de estrutura, o bastão de depuração — e os ovos de nascimento.
 */
public class VanillaAspectsGameTest {
    /** O que não é coisa do mundo: peça de criador, não de jogador. */
    private static final Set<String> CRIATIVO = Set.of(
            "command_block", "repeating_command_block", "chain_command_block", "command_block_minecart",
            "barrier", "light", "structure_void", "structure_block", "jigsaw", "test_block", "test_instance_block",
            "knowledge_book", "debug_stick");

    @GameTest
    public void everyVanillaItemHasAspects(GameTestHelper helper) {
        List<String> sem = new ArrayList<>();
        for (var item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals("minecraft") || item == Items.AIR) continue;
            if (CRIATIVO.contains(id.getPath()) || id.getPath().endsWith("_spawn_egg")) continue;
            if (ObjectAspects.of(new ItemStack(item)).isEmpty()) sem.add(id.getPath());
        }
        if (!sem.isEmpty()) helper.fail(sem.size() + " coisas do jogo sem aspecto: " + String.join(" ", sem));
        helper.succeed();
    }
}
