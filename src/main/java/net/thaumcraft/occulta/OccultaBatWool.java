package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * A Lã de Morcego que um morcego morto deixa: o {@code GenericEvents} do Witchery.
 *
 * <p>Uma vez em três, e só para quem o matou com as próprias mãos — o original olha o jogador que deu o golpe, e
 * é o mesmo que a condição do jogo de hoje faz.
 *
 * <p><b>Do original fica de fora, declarado:</b> a <b>Arthana</b> sobe essa chance para três em quatro, e para
 * uma em uma com o encantamento de saque. A Arthana é a faca do ofício, e ainda não está portada.
 */
public final class OccultaBatWool {
    /** Uma em três, que é a do original sem a faca. */
    public static final float CHANCE = 0.33f;

    private OccultaBatWool() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || !key.identifier().getNamespace().equals("minecraft")) return;
            if (!key.identifier().getPath().equals("entities/bat")) return;
            table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0f))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                    .when(LootItemRandomChanceCondition.randomChance(CHANCE))
                    .add(LootItem.lootTableItem(OccultaItems.BAT_WOOL)));
        });
    }
}
