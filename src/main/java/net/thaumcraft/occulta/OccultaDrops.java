package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Optional;

/**
 * O que os bichos do mundo deixam para quem mexe com o ofício: o {@code GenericEvents} do Witchery.
 *
 * <p>São duas contas, e é a <b>Arthana na mão</b> que diz qual vale. Sem ela, o lobo dá a Língua de Cão uma vez
 * em três, o creeper dá o Coração duas em cem e o sapo dá o Dedo uma em cinco. <b>Com ela</b>, a língua e a lã
 * sobem para três em quatro, o dedo para uma em duas, o coração para oito em cem — e se abrem coisas que sem
 * faca não se abrem: a <b>caveira</b> do esqueleto, do zumbi e do creeper, e o <b>Pó Espectral</b> dos dois
 * primeiros.
 *
 * <p>As chances são as do original, número por número, e cada uma é uma <b>pilha própria</b>: quem mata com a
 * faca tira a conta grande, e quem mata sem ela tira a pequena.
 *
 * <p><b>Do original fica de fora, declarado:</b> a <b>Asa de Mocho</b>, que lá cai do Mocho — o jogo de hoje não
 * tem mocho, e inventar-lhe um dono seria pior do que esperar. E a <b>caveira de quem se mata</b>, que o original
 * dá a quem derruba outro jogador com a faca: ela pede o nome do morto escrito na caveira, e isso é conversa
 * entre mundos que este porte não quer travar sozinho.
 *
 * <p><b>Uma escolha declarada:</b> o Dedo de Sapo cai do <b>sapo</b> do jogo de hoje. No original ele cai do Toad,
 * que é um bicho do próprio Witchery com o mesmo papel — e o jogo de agora traz o sapo de casa.
 */
public final class OccultaDrops {
    /** A chance da língua de cão sem a faca: uma em três. */
    public static final float TONGUE = 0.33f;
    /** E com ela: três em quatro. */
    public static final float TONGUE_ARTHANA = 0.75f;
    /** A do coração de creeper: duas em cem sem a faca, oito com ela. */
    public static final float HEART = 0.02f;
    public static final float HEART_ARTHANA = 0.08f;
    /** A do dedo de sapo: uma em cinco sem a faca, uma em duas com ela. */
    public static final float TOE = 0.2f;
    public static final float TOE_ARTHANA = 0.5f;
    /** A da lã de morcego: uma em três sem a faca, três em quatro com ela. */
    public static final float BAT_WOOL = 0.33f;
    public static final float BAT_WOOL_ARTHANA = 0.75f;

    /** O Pó Espectral: quatro em cem do esqueleto, três do zumbi — e só com a faca. */
    public static final float DUST_SKELETON = 0.04f;
    public static final float DUST_ZOMBIE = 0.03f;

    /**
     * E o <b>Coração de Demônio</b> do próprio demônio: <b>uma em três</b>, e só com a faca.
     *
     * <p>É a única coisa que se arranca de um demônio em vez de se lhe comprar — e o preço é ter de o matar,
     * o que, com o teto de quinze de dano por pancada que ele tem, leva muito mais tempo do que juntar o
     * ouro de o comprar.
     */
    public static final float DEMON_HEART_ARTHANA = 0.33f;

    /** E as caveiras: cinco em cem do esqueleto, duas do zumbi, uma do creeper. */
    public static final float SKULL_SKELETON = 0.05f;
    public static final float SKULL_ZOMBIE = 0.02f;
    public static final float SKULL_CREEPER = 0.01f;

    /**
     * E a <b>Cabeça de Lobo</b>, que é o troféu: uma em doze, e a Pilhagem soma mais uma em doze por grau.
     *
     * <p>O original escreve isso como {@code sorte(12) <= min(pilhagem, 3)}, que é a mesma coisa dita ao
     * contrário — e é por isso que a conta aqui tem a base e o passo iguais.
     */
    public static final float WOLF_HEAD = 1.0f / 12.0f;
    public static final float WOLF_HEAD_POR_GRAU = 1.0f / 12.0f;

    private OccultaDrops() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin() || !key.identifier().getNamespace().equals("minecraft")) return;
            // a Cabeça de Lobo, que é a única queda que a Pilhagem melhora
            if (key.identifier().getPath().equals("entities/wolf")) {
                table.withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(net.minecraft.world.level.storage.loot.predicates
                                .LootItemRandomChanceWithEnchantedBonusCondition
                                .randomChanceAndLootingBoost(registries, WOLF_HEAD, WOLF_HEAD_POR_GRAU))
                        .add(LootItem.lootTableItem(OccultaItems.WOLF_HEAD)));
            }

            for (var queda : QUEDAS) {
                if (!key.identifier().getPath().equals("entities/" + queda.bicho())) continue;
                var pilha = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(LootItemRandomChanceCondition.randomChance(queda.chance()))
                        .add(LootItem.lootTableItem(queda.deixa()));
                if (queda.arthana() != null) pilha.when(queda.arthana() ? comFaca() : semFaca());
                table.withPool(pilha);
            }
        });
    }

    /** A prova de que quem matou trazia a Arthana na mão. */
    private static LootItemCondition.Builder comFaca() {
        return LootItemEntityPropertyCondition.hasProperties(net.minecraft.world.level.storage.loot.LootContext.EntityTarget.ATTACKING_PLAYER,
                EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
                        .mainhand(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM,
                                OccultaItems.ARTHANA))
                        .build()));
    }

    /** E a de que não trazia. */
    private static LootItemCondition.Builder semFaca() {
        return comFaca().invert();
    }

    /**
     * Um bicho, o que ele deixa e com que chance.
     *
     * @param arthana {@code true} se esta queda só vale com a faca na mão, {@code false} se só sem ela, e nada
     *                se vale das duas maneiras
     */
    private record Queda(String bicho, Item deixa, float chance, Boolean arthana) {
    }

    private static final List<Queda> QUEDAS = List.of(
            // o que o pote pede, nas duas contas
            new Queda("wolf", OccultaItems.DOG_TONGUE, TONGUE, false),
            new Queda("wolf", OccultaItems.DOG_TONGUE, TONGUE_ARTHANA, true),
            new Queda("creeper", OccultaItems.CREEPER_HEART, HEART, false),
            new Queda("creeper", OccultaItems.CREEPER_HEART, HEART_ARTHANA, true),
            new Queda("frog", OccultaItems.TOE_OF_FROG, TOE, false),
            new Queda("frog", OccultaItems.TOE_OF_FROG, TOE_ARTHANA, true),
            new Queda("bat", OccultaItems.BAT_WOOL, BAT_WOOL, false),
            new Queda("bat", OccultaItems.BAT_WOOL, BAT_WOOL_ARTHANA, true),

            // e o que só a faca abre
            new Queda("skeleton", OccultaItems.SPECTRAL_DUST, DUST_SKELETON, true),
            new Queda("zombie", OccultaItems.SPECTRAL_DUST, DUST_ZOMBIE, true),
            new Queda("skeleton", net.minecraft.world.item.Items.SKELETON_SKULL, SKULL_SKELETON, true),
            new Queda("zombie", net.minecraft.world.item.Items.ZOMBIE_HEAD, SKULL_ZOMBIE, true),
            new Queda("creeper", net.minecraft.world.item.Items.CREEPER_HEAD, SKULL_CREEPER, true),
            new Queda("demon", OccultaItems.DEMON_HEART, DEMON_HEART_ARTHANA, true));

    /** Sem uso fora do porte: serve à prova para contar as quedas. */
    public static int count() {
        return QUEDAS.size();
    }

    /** E para saber se aquela queda existe, e com que chance. */
    public static Optional<Float> chance(String bicho, Item deixa, boolean comFaca) {
        return QUEDAS.stream()
                .filter(q -> q.bicho().equals(bicho) && q.deixa() == deixa
                        && (q.arthana() == null || q.arthana() == comFaca))
                .map(Queda::chance)
                .findFirst();
    }
}
