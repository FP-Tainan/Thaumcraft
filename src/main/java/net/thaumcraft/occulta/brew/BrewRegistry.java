package net.thaumcraft.occulta.brew;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O que cada coisa faz dentro do caldeirão: o {@code WitcheryBrewRegistry} da 0.24.1.
 *
 * <p>A tabela do original tem três mil e quatrocentas linhas e usa quase tudo o que o Witchery tem. Esta é a
 * <b>primeira parte</b> dela, e traz o que o mod de hoje já alcança:
 *
 * <ul>
 *   <li>os <b>ingredientes de porte</b>, que abrem espaço no caldeirão;</li>
 *   <li>os <b>temperos</b>, que dão força, duração, inversão e mais;</li>
 *   <li>os <b>efeitos</b> que são poções do próprio jogo;</li>
 *   <li>e as <b>lãs tintas</b>, que pintam o caldo.</li>
 * </ul>
 *
 * <p><b>Fica declarado o que ainda não está aqui:</b> os efeitos que são poções próprias do Witchery (o nadar, o
 * não sentir dor, a acônito, a máscara de gás, a queda de pena e as outras cinquenta e tantas), o espalhamento —
 * o frasco que se atira, o gás, o líquido e o gatilho —, os efeitos que mexem no mundo e os rituais de círculo de
 * giz. Cada um desses é uma fatia sua, e virá.
 *
 * <p><b>E o que não virá tal e qual</b>, por não existir no mod: a Lágrima da Deusa e o Vapor de Diamante abrem
 * espaço no original e ainda não estão feitos; o Pentáculo de Koboldite, que abre o maior de todos, é de uma parte
 * do Witchery que este porte não traz.
 */
public final class BrewRegistry {
    private static final Map<Item, BrewAction> TABELA = new LinkedHashMap<>();
    private static boolean pronta;

    private BrewRegistry() {
    }

    public static BrewAction of(Item item) {
        build();
        return TABELA.get(item);
    }

    public static boolean knows(Item item) {
        return of(item) != null;
    }

    public static Map<Item, BrewAction> all() {
        build();
        return java.util.Collections.unmodifiableMap(TABELA);
    }

    private static void register(BrewAction ação) {
        TABELA.put(ação.key, ação);
    }

    private static int secs(int quanto) {
        return quanto * 20;
    }

    private static int mins(int quanto) {
        return quanto * 1200;
    }

    /** Os pesos de efeito do original: os leves gastam um, os comuns dois, os graves quatro, seis ou mais. */
    private static final int LEVE = 1;
    private static final int COMUM = 2;
    private static final int GRAVE = 4;

    private static synchronized void build() {
        if (pronta) return;
        pronta = true;

        // ------------------------------------------------------------ as lãs, que pintam o caldo
        for (DyeColor cor : DyeColor.values()) {
            register(new BrewActions.SetColor(Items.WOOL.pick(cor), 0, cor.getTextureDiffuseColor() & 0xFFFFFF));
        }

        // ------------------------------------------------------------ os que abrem espaço
        register(BrewActions.Modifier.room(OccultaItems.MANDRAKE_ROOT, 0, 1, 1));
        register(BrewActions.Modifier.room(Items.NETHER_WART, 50, 2, 2));
        register(BrewActions.Modifier.room(Items.DIAMOND, 150, 2, 8).yield(-2));
        register(new BrewActions.Modifier(Items.NETHER_STAR, new BrewName.Tweak(0, 0, false, 0, 0, true), 150,
                espaço -> espaço.openIf(4, 10), temperos -> temperos.powerCeilingDisabled = true, 0));

        // ------------------------------------------------------------ o jeito de se espalhar
        // A pólvora e o globo de alcachofra fazem o cozimento se atirar. Um espalhamento desfaz o outro: dois
        // não convivem na mesma panela, e é por isso que cada um apaga todos (inclusive outro igual).
        List<Item> jeitos = List.of(Items.GUNPOWDER, OccultaItems.WATER_ARTICHOKE_GLOBE, OccultaItems.BAT_WOOL,
                OccultaItems.WORMWOOD_SPRIG);
        for (Item qual : List.of(Items.GUNPOWDER, OccultaItems.WATER_ARTICHOKE_GLOBE, OccultaItems.BAT_WOOL)) {
            BrewDispersal jeito = qual == OccultaItems.BAT_WOOL
                    ? new BrewDispersal.Gas() : new BrewDispersal.Instant();
            BrewAction ação = new BrewActions.Dispersal(qual, 0, jeito);
            for (Item outro : jeitos) ação.nullifies(outro, false);
            register(ação);
        }

        // ------------------------------------------------------------ o alcance e a duração do que se espalha
        // o alcance alarga o estouro; a duração é para o gás e o líquido, que ainda não chegaram
        register(BrewActions.Modifier.impact(OccultaItems.WOOD_ASH, new BrewName.Tweak(0, 0, false, 1, 0), 50,
                espalha -> {
                    if (espalha.extent < 1) espalha.extent++;
                }));
        register(BrewActions.Modifier.impact(Items.COCOA_BEANS, new BrewName.Tweak(0, 0, false, 1, 0), 100,
                espalha -> {
                    if (espalha.extent < 2) espalha.extent++;
                }));
        register(BrewActions.Modifier.impact(OccultaItems.BELLADONNA_FLOWER, new BrewName.Tweak(0, 0, false, 0, 1),
                50, espalha -> {
                    if (espalha.lifetime < 1) espalha.lifetime++;
                }));
        register(BrewActions.Modifier.impact(Items.LAPIS_LAZULI, new BrewName.Tweak(0, 0, false, 0, 1), 100,
                espalha -> {
                    if (espalha.lifetime < 2) espalha.lifetime++;
                }));
        register(BrewActions.Modifier.impact(Items.END_STONE, new BrewName.Tweak(0, 0, false, 0, 1), 150,
                espalha -> {
                    if (espalha.lifetime < 3) espalha.lifetime++;
                }));

        // ------------------------------------------------------------ os temperos
        // a pepita de ouro tira as fagulhas do efeito
        register(BrewActions.Modifier.tempering(Items.GOLD_NUGGET, null, 50,
                temperos -> temperos.noParticles = true));
        // três que se bebem mais depressa
        register(BrewActions.Modifier.drink(OccultaItems.ROWAN_BERRIES, 50, -8));
        register(BrewActions.Modifier.drink(OccultaItems.EXHALE_OF_THE_HORNED_ONE, 0, -4));
        // força: cada um sobe um grau, e só enquanto o efeito ainda estiver abaixo do grau dele
        register(BrewActions.Modifier.tempering(Items.GLOWSTONE_DUST, new BrewName.Tweak(1, 0, false, 0, 0), 50,
                temperos -> {
                    if (temperos.strength < 1) temperos.increaseStrength(1);
                }));
        register(BrewActions.Modifier.tempering(Items.BLAZE_ROD, new BrewName.Tweak(1, 0, false, 0, 0), 100,
                temperos -> {
                    if (temperos.strength < 2) temperos.increaseStrength(1);
                }));
        // duração: o mesmo, de outro lado
        register(BrewActions.Modifier.tempering(Items.REDSTONE, new BrewName.Tweak(0, 1, false, 0, 0), 50,
                temperos -> {
                    if (temperos.duration < 1) temperos.increaseDuration(1);
                }));
        register(BrewActions.Modifier.tempering(Items.OBSIDIAN, new BrewName.Tweak(0, 1, false, 0, 0), 100,
                temperos -> {
                    if (temperos.duration < 2) temperos.increaseDuration(1);
                }));
        register(BrewActions.Modifier.tempering(OccultaItems.MINDRAKE_BULB, new BrewName.Tweak(0, 1, false, 0, 0),
                150, temperos -> {
                    if (temperos.duration < 3) temperos.increaseDuration(1);
                }));
        // o olho de aranha fermentado inverte o efeito seguinte
        register(BrewActions.Modifier.tempering(Items.FERMENTED_SPIDER_EYE, new BrewName.Tweak(0, 0, true, 0, 0),
                25, temperos -> temperos.inverted = true));
        // o tijolo do Nether tira o alvo do chão; o tijolo comum, o alvo de quem passa
        register(BrewActions.Modifier.tempering(Items.NETHER_BRICK, null, 50,
                temperos -> temperos.disableBlockTarget = true));
        register(BrewActions.Modifier.tempering(Items.BRICK, null, 50,
                temperos -> temperos.disableEntityTarget = true));
        // e o peixe-palhaço tira o teto da força
        register(BrewActions.Modifier.tempering(Items.TROPICAL_FISH, null, 200,
                temperos -> temperos.strengthCeilingDisabled = true));

        // ------------------------------------------------------------ as poções do próprio ofício
        register(new BrewActions.Potion(Items.COD, new BrewName.Text("tc.brew.swimming"), 0,
                net.thaumcraft.occulta.OccultaEffects.SWIMMING, mins(3), LEVE));
        register(new BrewActions.Potion(Items.FEATHER, new BrewName.Text("tc.brew.featherfall"), 100,
                net.thaumcraft.occulta.OccultaEffects.FEATHER_FALL, mins(1), COMUM));
        register(new BrewActions.Potion(Items.SUGAR_CANE, new BrewName.Text("tc.brew.floating"), 250,
                net.thaumcraft.occulta.OccultaEffects.FLOATING, secs(90), COMUM));
        register(new BrewActions.Potion(Items.GRAVEL, new BrewName.Text("tc.brew.gasmask"), 100,
                net.thaumcraft.occulta.OccultaEffects.GAS_MASK, secs(90), COMUM));
        register(new BrewActions.Potion(OccultaItems.FOUL_FUME, new BrewName.Text("tc.brew.stoutbelly"), 1000,
                net.thaumcraft.occulta.OccultaEffects.STOUT_BELLY, secs(90), GRAVE));
        register(new BrewActions.Potion(Items.SALMON, new BrewName.Text("tc.brew.allergysun"), 1000,
                net.thaumcraft.occulta.OccultaEffects.SUN_ALLERGY, secs(60), 6));
        register(new BrewActions.Potion(Items.SOUL_SAND, new BrewName.Text("tc.brew.allergydark"), 4000,
                net.thaumcraft.occulta.OccultaEffects.DARKNESS_ALLERGY, mins(2), GRAVE));

        // ------------------------------------------------------------ os efeitos que mexem no lugar
        // só acontecem no cozimento atirado, porque é aí que há um lugar onde ele bateu
        register(new BrewWorldActions.Felling(Items.STRING, 0, 0, LEVE));
        register(new BrewWorldActions.Pruning(Items.BROWN_MUSHROOM, 0, LEVE));
        register(new BrewWorldActions.Pulverisation(Items.FLINT, 250, LEVE));
        register(new BrewWorldActions.Lilify(Items.LILY_PAD, 200, LEVE));
        register(new BrewWorldActions.Planting(Items.WHEAT_SEEDS, 0, LEVE));
        register(new BrewWorldActions.Blight(Items.POISONOUS_POTATO, 2000, GRAVE));

        // ------------------------------------------------------------ os efeitos que são poções do jogo
        register(new BrewActions.Potion(Items.SPIDER_EYE, new BrewName.Text("tc.brew.poison"), 0,
                MobEffects.POISON, secs(45), COMUM));
        register(new BrewActions.Potion(Items.SUGAR,
                new BrewName.Text("tc.brew.movespeed", "tc.brew.moveslow"), 100,
                MobEffects.SPEED, mins(3), MobEffects.SLOWNESS, secs(90), COMUM));
        register(new BrewActions.Potion(Items.PUFFERFISH, new BrewName.Text("tc.brew.waterbreathing"), 100,
                MobEffects.WATER_BREATHING, mins(3), COMUM));
        register(new BrewActions.Potion(Items.MAGMA_CREAM, new BrewName.Text("tc.brew.resistfire"), 100,
                MobEffects.FIRE_RESISTANCE, mins(3), COMUM));
        register(new BrewActions.Potion(Items.GOLDEN_CARROT,
                new BrewName.Text("tc.brew.nightvision", "tc.brew.invisibility"), 200,
                MobEffects.NIGHT_VISION, mins(3), MobEffects.INVISIBILITY, mins(3), COMUM));
        register(new BrewActions.Potion(Items.GHAST_TEAR,
                new BrewName.Text("tc.brew.regeneration", "tc.brew.poison"), 200,
                MobEffects.REGENERATION, secs(45), MobEffects.POISON, secs(45), COMUM));
        register(new BrewActions.Potion(Items.BLAZE_POWDER,
                new BrewName.Text("tc.brew.damageboost", "tc.brew.weakness"), 200,
                MobEffects.STRENGTH, mins(3), MobEffects.WEAKNESS, secs(90), COMUM));
        register(new BrewActions.Potion(Items.GLISTERING_MELON_SLICE,
                new BrewName.Text("tc.brew.healing", "tc.brew.harming"), 200,
                MobEffects.INSTANT_HEALTH, 0, MobEffects.INSTANT_DAMAGE, 0, COMUM));
        register(new BrewActions.Potion(Items.LEATHER, new BrewName.Text("tc.brew.jump"), 200,
                MobEffects.JUMP_BOOST, mins(3), COMUM));
        register(new BrewActions.Potion(Items.WITHER_SKELETON_SKULL, new BrewName.Text("tc.brew.wither"), 200,
                MobEffects.WITHER, secs(15), GRAVE));
        register(new BrewActions.Potion(Items.INK_SAC, new BrewName.Text("tc.brew.blindness"), 1000,
                MobEffects.BLINDNESS, secs(15), GRAVE));
        register(new BrewActions.Potion(Items.GOLDEN_APPLE, new BrewName.Text("tc.brew.absorbsion"), 1000,
                MobEffects.ABSORPTION, secs(30), GRAVE));
        register(new BrewActions.Potion(Items.ENCHANTED_GOLDEN_APPLE, new BrewName.Text("tc.brew.healthboost"),
                1000, MobEffects.HEALTH_BOOST, mins(2), GRAVE));
    }

}
