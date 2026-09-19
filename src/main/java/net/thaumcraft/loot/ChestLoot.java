package net.thaumcraft.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.thaumcraft.registry.TCComponents;

/**
 * O {@code ChestGenHooks.addItem} do {@code Config.initLoot}: sacolas, táumio, âmbar, peças comuns, anéis de aprendiz
 * e o resto de {@link ThaumLoot#CHESTS} entram nos baús de masmorra, templos, minas, fortalezas e no ferreiro da vila.
 *
 * <p>No 1.7.10 cada baú sorteava de uma lista só; no 26.2 as tabelas têm mais de um sorteio, e as coisas do Thaumcraft
 * entram no primeiro deles, com o mesmo peso e a mesma quantidade do original. A pedra de vis sai com o vis sorteado
 * uma vez ao carregar o mundo, como o original sorteava uma vez ao iniciar o jogo.
 */
public final class ChestLoot {
    private ChestLoot() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || !key.identifier().getNamespace().equals("minecraft")) return;
            String path = key.identifier().getPath();
            var mine = ThaumLoot.CHESTS.stream().filter(chest -> chest.table().equals(path)).toList();
            if (mine.isEmpty()) return;
            int[] pool = {0};
            table.modifyPools(builder -> {
                if (pool[0]++ != 0) return;
                for (ThaumLoot.Chest chest : mine) builder.add(entry(chest));
            });
        });
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ThaumLoot.Chest chest) {
        var item = chest.item().get();
        var entry = LootItem.lootTableItem(item).setWeight(chest.weight());
        if (chest.min() != 1 || chest.max() != 1) entry.apply(SetItemCountFunction.setCount(UniformGenerator.between(chest.min(), chest.max())));
        if (item == net.thaumcraft.registry.TCItems.VIS_STONE) entry.apply(SetComponentsFunction.setComponent(TCComponents.WAND_VIS, ThaumLoot.visStoneVis()));
        return entry;
    }
}
