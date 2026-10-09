package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.charm.AnimalShop;
import net.thaumcraft.occulta.charm.PolynesiaCharmItem;
import net.thaumcraft.occulta.charm.WolfTokenItem;
import net.thaumcraft.occulta.demon.DemonTrades;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.wolf.Werewolf;

import java.util.List;
import java.util.Set;

/**
 * Os <b>amuletos</b>: o da Polinésia, que faz do bicho um vendedor, a <b>Língua do Diabo</b>, que faz o mesmo
 * e por cima desconta o preço de um demônio, e o <b>Token do Lobo</b>, que passa pelos graus das duas
 * maldições.
 */
public class OccultaCharmGameTest {
    /**
     * <b>A loja fica no bicho.</b>
     *
     * <p>Que é a coisa toda: aberta uma vez, aquela vaca tem aquela loja para sempre — e abri-la outra vez dá
     * exatamente o mesmo que a primeira deu, preço por preço.
     */
    @GameTest
    public void theShopStaysWithTheAnimal(GameTestHelper helper) {
        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 2));
        if (AnimalShop.temLoja(vaca)) helper.fail("uma vaca nasce sem loja");

        MerchantOffers primeira = AnimalShop.loja(vaca);
        if (!AnimalShop.temLoja(vaca)) helper.fail("e passa a ter loja depois de a abrir");
        if (primeira.isEmpty() || primeira.size() > AnimalShop.ATÉ) {
            helper.fail("a loja tem uma ou duas ofertas, e tem " + primeira.size());
            return;
        }

        MerchantOffers segunda = AnimalShop.loja(vaca);
        if (segunda.size() != primeira.size()) helper.fail("e a segunda vez dá a mesma loja");
        for (int n = 0; n < primeira.size(); n++) {
            if (!ItemStack.matches(primeira.get(n).getCostA(), segunda.get(n).getCostA())
                    || !ItemStack.matches(primeira.get(n).getResult(), segunda.get(n).getResult())) {
                helper.fail("oferta " + n + " mudou entre uma abertura e a outra");
            }
        }

        // e cada oferta aguenta uma ou duas trocas, que é o corte do original
        for (MerchantOffer cada : primeira) {
            int quantas = cada.getMaxUses();
            if (quantas < AnimalShop.TROCAS - AnimalShop.DESCONTO
                    || quantas > AnimalShop.TROCAS - AnimalShop.DESCONTO + 1) {
                helper.fail("uma oferta de bicho aguenta uma ou duas trocas, e aguenta " + quantas);
            }
        }
        helper.succeed();
    }

    /**
     * <b>A espécie decide a moeda.</b>
     *
     * <p>O porco aceita cenoura, maçã e batata; a galinha, semente de trigo. E é por isso que não se compra a
     * um porco com trigo: ele não sabe o que é.
     */
    @GameTest
    public void theSpeciesDecidesTheCurrency(GameTestHelper helper) {
        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
        confere(helper, "o porco", AnimalShop.monta(porco),
                Set.of(Items.CARROT, Items.APPLE, Items.POTATO));

        var galinha = helper.spawn(EntityTypes.CHICKEN, new BlockPos(2, 2, 4));
        confere(helper, "a galinha", AnimalShop.monta(galinha), Set.of(Items.WHEAT_SEEDS));

        // e a vaca-de-cogumelo cai no ramo da vaca, que é o acidente do original: ela aceita trigo
        var cogumelo = helper.spawn(EntityTypes.MOOSHROOM, new BlockPos(2, 2, 6));
        confere(helper, "a vaca-de-cogumelo", AnimalShop.monta(cogumelo), Set.of(Items.WHEAT));
        helper.succeed();
    }

    private static void confere(GameTestHelper helper, String quem, MerchantOffers loja,
                                Set<net.minecraft.world.item.Item> moedas) {
        if (loja.isEmpty()) {
            helper.fail(quem + " tem de ter o que vender");
            return;
        }
        for (MerchantOffer cada : loja) {
            if (!moedas.contains(cada.getCostA().getItem())) {
                helper.fail(quem + " aceita " + moedas + ", e pediu " + cada.getCostA());
            }
            if (cada.getCostA().getCount() < 1) helper.fail(quem + " pede ao menos uma peça");
            if (cada.getResult().isEmpty()) helper.fail(quem + " tem de dar alguma coisa");
        }
    }

    /**
     * <b>Em quem o amuleto pega</b>, e em quem não.
     *
     * <p>A vaca sempre; o filhote nunca; o creeper só com o <b>Manto de Bruxa</b>; o morto-vivo só com o de
     * <b>Necromante</b>. É a lista do original, e o manto é a chave dos dois últimos.
     */
    @GameTest
    public void theCharmOnlyTakesWhatTheOriginalTakes(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();

        var vaca = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 2));
        if (!PolynesiaCharmItem.pega(vaca, quem)) helper.fail("o amuleto pega numa vaca");

        var bezerro = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 4));
        bezerro.setBaby(true);
        if (PolynesiaCharmItem.pega(bezerro, quem)) helper.fail("mas não num bezerro");

        var creeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(2, 2, 6));
        if (PolynesiaCharmItem.pega(creeper, quem)) helper.fail("nem num creeper de mãos vazias");
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.WITCH_ROBES));
        if (!PolynesiaCharmItem.pega(creeper, quem)) {
            helper.fail("e num creeper, sim, com o Manto de Bruxa");
        }

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 2));
        if (PolynesiaCharmItem.pega(zumbi, quem)) {
            helper.fail("o Manto de Bruxa não abre um morto-vivo");
        }
        quem.setItemSlot(EquipmentSlot.CHEST, new ItemStack(OccultaItems.NECROMANCERS_ROBES));
        if (!PolynesiaCharmItem.pega(zumbi, quem)) {
            helper.fail("e o de Necromante abre-o");
        }
        if (PolynesiaCharmItem.pega(creeper, quem)) {
            helper.fail("e, com ele, o creeper deixa de ouvir");
        }

        // e um bicho que já tem alvo tem o que fazer
        zumbi.setTarget(vaca);
        if (PolynesiaCharmItem.pega(zumbi, quem)) helper.fail("um bicho com alvo não negocia");
        helper.succeed();
    }

    /**
     * <b>O morcego vendido sai com a loja vazia.</b>
     *
     * <p>É o bit oito do meta do original: apanhar um morcego que já teve loja e soltá-lo não lhe dá uma loja
     * nova. Sem isto, um Apanha-Bicho e um amuleto seriam uma máquina de esmeraldas.
     */
    @GameTest
    public void aSoldBatComesBackEmpty(GameTestHelper helper) {
        var morcego = helper.spawn(EntityTypes.BAT, new BlockPos(2, 3, 2));
        AnimalShop.loja(morcego);
        if (!AnimalShop.temLoja(morcego)) helper.fail("o morcego teve loja");

        AnimalShop.lojaVazia(morcego);
        if (!AnimalShop.temLoja(morcego)) helper.fail("e a loja vazia ainda é uma loja");
        if (!AnimalShop.loja(morcego).isEmpty()) {
            helper.fail("mas não tem nada dentro");
        }
        helper.succeed();
    }

    /**
     * <b>A Língua do Diabo desconta.</b>
     *
     * <p>Cinco no ouro, dois na esmeralda, <b>nada</b> no diamante e um em tudo o mais — e nunca abaixo de
     * um, que é o chão do original.
     */
    @GameTest
    public void theDevilsTongueCutsThePrice(GameTestHelper helper) {
        MerchantOffers caras = new MerchantOffers();
        caras.add(troca(Items.GOLD_INGOT, 30, OccultaItems.DEMON_HEART));
        caras.add(troca(Items.EMERALD, 3, OccultaItems.SPECTRAL_DUST));
        caras.add(troca(Items.DIAMOND, 3, Items.GHAST_TEAR));
        caras.add(troca(Items.BLAZE_ROD, 3, OccultaItems.DOG_TONGUE));
        caras.add(troca(Items.GOLD_INGOT, 2, Items.ENDER_PEARL));

        MerchantOffers baratas = DemonTrades.maisBarato(caras);
        int[] espera = {25, 1, 3, 2, 1};
        for (int n = 0; n < espera.length; n++) {
            int deu = baratas.get(n).getCostA().getCount();
            if (deu != espera[n]) {
                helper.fail("a troca " + n + " havia de custar " + espera[n] + " e custa " + deu);
            }
        }
        // e a lista de origem não se mexeu: o desconto não se guarda no demônio
        if (caras.get(0).getCostA().getCount() != 30) {
            helper.fail("o desconto não mexe na lista do demônio");
        }
        // o que a Língua é, e o que o outro amuleto não é
        if (!PolynesiaCharmItem.éLíngua(new ItemStack(OccultaItems.DEVILS_TONGUE_CHARM))) {
            helper.fail("a Língua do Diabo reconhece-se");
        }
        if (PolynesiaCharmItem.éLíngua(new ItemStack(OccultaItems.POLYNESIA_CHARM))) {
            helper.fail("e o Amuleto da Polinésia não é ela");
        }
        helper.succeed();
    }

    private static MerchantOffer troca(net.minecraft.world.item.Item moeda, int quanto,
                                       net.minecraft.world.item.Item dá) {
        return new MerchantOffer(new ItemCost(moeda, quanto), new ItemStack(dá), 2, 0, 0.0f);
    }

    /**
     * <b>O Token do Lobo passa pelos graus</b>, e volta a zero depois do décimo.
     *
     * <p>Os dois: o de lobisomem de pé, o de vampiro agachado.
     */
    @GameTest
    public void theWolfTokenCyclesBothCurses(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();

        for (int esperado = 1; esperado <= WolfTokenItem.TETO; esperado++) {
            Werewolf.grau(quem, Werewolf.grauDe(quem) + 1);
            if (Werewolf.grauDe(quem) != esperado) {
                helper.fail("o grau de lobisomem havia de ser " + esperado
                        + " e é " + Werewolf.grauDe(quem));
                return;
            }
        }
        // e passando do décimo, zero
        int volta = Werewolf.grauDe(quem) + 1;
        if (volta <= WolfTokenItem.TETO) helper.fail("o décimo é o teto");
        Werewolf.grau(quem, 0);
        if (Werewolf.grauDe(quem) != 0) helper.fail("e depois dele vem o zero");

        Vampire.grau(quem, WolfTokenItem.TETO);
        if (Vampire.grauDe(quem) != WolfTokenItem.TETO) {
            helper.fail("o grau de vampiro sobe até dez, e é " + Vampire.grauDe(quem));
        }
        Vampire.grau(quem, 0);
        if (Vampire.grauDe(quem) != 0) helper.fail("e volta a zero");
        helper.succeed();
    }

    /** E as duas receitas, que são as do original. */
    @GameTest
    public void theTwoRecipesAreTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        ItemStack verme = new ItemStack(Items.NETHER_WART);
        var bancada = CraftingInput.of(3, 3, List.of(
                verme.copy(), new ItemStack(Items.IRON_INGOT), verme.copy(),
                new ItemStack(OccultaItems.ODOUR_OF_PURITY), new ItemStack(Items.COD),
                new ItemStack(OccultaItems.ODOUR_OF_PURITY),
                verme.copy(), new ItemStack(OccultaItems.WHIFF_OF_MAGIC), verme.copy()));
        if (!dá(helper, level, bancada, OccultaItems.POLYNESIA_CHARM)) return;

        ItemStack brasa = new ItemStack(Items.BLAZE_POWDER);
        var outra = CraftingInput.of(3, 3, List.of(
                brasa.copy(), new ItemStack(OccultaItems.POLYNESIA_CHARM), brasa.copy(),
                new ItemStack(OccultaItems.DEMON_HEART), new ItemStack(Items.SKELETON_SKULL),
                new ItemStack(OccultaItems.REFINED_EVIL),
                brasa.copy(), new ItemStack(OccultaItems.DOG_TONGUE), brasa.copy()));
        if (!dá(helper, level, outra, OccultaItems.DEVILS_TONGUE_CHARM)) return;

        // e o Token do Lobo segura-se um segundo, que é o que o original lhe dá
        ItemStack token = new ItemStack(OccultaItems.WOLF_TOKEN);
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (token.getUseDuration(quem) != WolfTokenItem.SEGURAR) {
            helper.fail("o Token do Lobo segura-se um segundo, e segura-se "
                    + token.getUseDuration(quem));
        }
        helper.succeed();
    }

    private static boolean dá(GameTestHelper helper, ServerLevel level, CraftingInput bancada,
                              net.minecraft.world.item.Item oquê) {
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, bancada, level);
        if (achada.isEmpty()) {
            helper.fail("não há receita para " + oquê);
            return false;
        }
        ItemStack feito = achada.get().value().assemble(bancada);
        if (!feito.is(oquê)) {
            helper.fail("a receita havia de dar " + oquê + " e deu " + feito);
            return false;
        }
        return true;
    }
}
