package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;

/**
 * O que os bichos do mundo deixam para quem mexe com o ofício: o {@code GenericEvents} do Witchery.
 *
 * <p>Três coisas que o Caldeirão de Pote pede e que não se plantam: a <b>Língua de Cão</b> do lobo, o
 * <b>Coração de Creeper</b> do creeper e o <b>Dedo de Sapo</b> do sapo. As contas são as do original, sem a faca.
 *
 * <p><b>Do original fica de fora, declarado:</b> a <b>Arthana</b>, a faca do ofício, que sobe cada uma destas
 * chances — três em quatro na língua, oito em cem no coração — e que ainda não está portada. E a <b>Asa de
 * Mocho</b>, que lá cai do Mocho: o jogo de hoje não tem mocho, e inventar-lhe um dono seria pior do que esperar.
 *
 * <p><b>Uma escolha declarada:</b> o Dedo de Sapo cai do <b>sapo</b> do jogo de hoje. No original ele cai do Toad,
 * que é um bicho do próprio Witchery com o mesmo papel — e o jogo de agora traz o sapo de casa.
 */
public final class OccultaDrops {
    /** A chance da língua de cão: uma em três, a do original sem a faca. */
    public static final float TONGUE = 0.33f;
    /** A do coração de creeper: duas em cem. */
    public static final float HEART = 0.02f;
    /** E a do dedo de sapo: uma em cinco. */
    public static final float TOE = 0.2f;

    private OccultaDrops() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || !key.identifier().getNamespace().equals("minecraft")) return;
            for (var queda : QUEDAS) {
                if (!key.identifier().getPath().equals("entities/" + queda.bicho())) continue;
                table.withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(LootItemRandomChanceCondition.randomChance(queda.chance()))
                        .add(LootItem.lootTableItem(queda.deixa())));
            }
        });
    }

    /** Um bicho, o que ele deixa e quantas vezes em cem. */
    private record Queda(String bicho, Item deixa, float chance) {
    }

    private static final List<Queda> QUEDAS = List.of(
            new Queda("wolf", OccultaItems.DOG_TONGUE, TONGUE),
            new Queda("creeper", OccultaItems.CREEPER_HEART, HEART),
            new Queda("frog", OccultaItems.TOE_OF_FROG, TOE));
}
