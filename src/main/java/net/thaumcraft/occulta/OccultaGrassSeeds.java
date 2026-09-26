package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * De onde vêm as primeiras sementes do ofício: do mato.
 *
 * <p>É o {@code MinecraftForge.addGrassSeed} do {@code Witchery.load}, que punha seis sementes na lista de que o
 * mato tira a semente que larga, com os pesos que estão aqui. Sem isto não há por onde começar o ramo: as plantas
 * não nascem no mundo e não saem de receita nenhuma.
 *
 * <p>Fica de fora a mindrake, que no original também não vem do mato — o bulbo dela sai da criatura, e vem na
 * fatia dos bichos; e a losna, que se acha crescida.
 *
 * <p><b>Desvio declarado:</b> no 1.7.10 a lista era uma só, e a semente de trigo do jogo disputava com as do mod
 * pelo mesmo sorteio — dez de vinte e seis. Aqui a tabela do trigo é a do jogo e não se mexe nela; o mod põe um
 * sorteio à parte, com o mesmo um oitavo e os mesmos pesos, e uma entrada vazia de peso dez no lugar do trigo.
 * Assim as seis saem com a chance exata do original, e o trigo continua saindo como o jogo de hoje quer.
 */
public final class OccultaGrassSeeds {
    /** Uma vez em oito o mato larga semente, como no jogo antigo. */
    public static final float CHANCE = 0.125f;

    /** O peso que a semente de trigo tinha na lista do Forge, e que aqui é o da entrada vazia. */
    public static final int WHEAT_WEIGHT = 10;

    /** As seis e o peso de cada uma, na ordem do {@code Witchery.load}. */
    public static final Map<String, Integer> WEIGHTS = new LinkedHashMap<>();

    static {
        WEIGHTS.put("water_artichoke_seeds", 3);
        WEIGHTS.put("belladonna_seeds", 4);
        WEIGHTS.put("mandrake_seeds", 5);
        WEIGHTS.put("snowbell_seeds", 2);
        WEIGHTS.put("wolfsbane_seeds", 1);
        WEIGHTS.put("garlic", 1);
    }

    /** As tabelas do mato de que se tira semente. */
    private static final java.util.Set<String> GRASS =
            java.util.Set.of("blocks/short_grass", "blocks/fern");

    private OccultaGrassSeeds() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || !key.identifier().getNamespace().equals("minecraft")) return;
            if (!GRASS.contains(key.identifier().getPath())) return;
            LootPool.Builder sorteio = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0f))
                    .when(LootItemRandomChanceCondition.randomChance(CHANCE))
                    .when(ExplosionCondition.survivesExplosion())
                    // o lugar do trigo na lista do original: quando sai, não sai nada daqui
                    .add(EmptyLootItem.emptyItem().setWeight(WHEAT_WEIGHT));
            for (Map.Entry<String, Integer> semente : WEIGHTS.entrySet()) {
                Supplier<Item> item = () -> net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getValue(net.thaumcraft.Thaumcraft.id(semente.getKey()));
                sorteio.add(LootItem.lootTableItem(item.get()).setWeight(semente.getValue()));
            }
            table.withPool(sorteio);
        });
    }
}
