package net.thaumcraft.arcana;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * A <b>Banha de Porco</b>: o pedaço do {@code onLivingDrops} do {@code AMEventHandler} que faz cair gordura
 * de quem é porco.
 *
 * <p>Três em dez, e <b>não precisa de ter sido gente a matar</b> — o original olha só para o bicho e para o
 * dado. Um porco que se afoga deixa banha na água.
 */
public final class ArcanaDrops {
    /** Três em dez, que é o {@code nextDouble() < 0.3F} do original. */
    public static final float BANHA = 0.3f;

    private ArcanaDrops() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || !key.identifier().getNamespace().equals("minecraft")) return;
            if (!key.identifier().getPath().equals("entities/pig")) return;
            table.withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0f))
                    .when(LootItemRandomChanceCondition.randomChance(BANHA))
                    .add(LootItem.lootTableItem(ArcanaItems.MATERIALS.get("animal_fat"))));
        });
    }
}
