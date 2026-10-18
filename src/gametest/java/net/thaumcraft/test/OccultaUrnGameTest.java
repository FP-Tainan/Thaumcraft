package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.Brew;
import net.thaumcraft.occulta.brew.BrewItem;
import net.thaumcraft.occulta.leonard.LeonardsUrnItem;
import net.thaumcraft.occulta.leonard.UrnJoiningRecipe;
import net.thaumcraft.occulta.leonard.UrnPouch;
import net.thaumcraft.occulta.symbol.Symbol;
import net.thaumcraft.occulta.symbol.Symbols;

import java.util.List;

/**
 * A <b>Urna do Leonard</b> e os <b>quatro símbolos</b> que bebem dela.
 *
 * <p>A prova que carrega a fatia é a do <b>custo</b>: é o único feitiço do mod cujo preço não está escrito
 * no símbolo — sai do cozimento que está na urna de quem o lança. Errando essa conta, ou o feitiço é de
 * graça ou não se lança nunca.
 */
public class OccultaUrnGameTest {
    /** Um frasco de cozimento atirável, que é o único que entra na urna. */
    private static ItemStack frascoAtirável() {
        // pólvora é o que torna um cozimento arremessável, e o açúcar é o efeito dele
        return BrewItem.of(OccultaItems.BREW,
                List.of(Items.NETHER_WART, Items.SUGAR, Items.GUNPOWDER));
    }

    /** <b>Os quatro tamanhos dela</b>, e as casas que cada um tem. */
    @GameTest(maxTicks = 20)
    public void sheComesInFourSizes(GameTestHelper helper) {
        if (LeonardsUrnItem.TAMANHOS != 4) helper.fail("são quatro tamanhos");
        for (int tamanho = 0; tamanho < LeonardsUrnItem.TAMANHOS; tamanho++) {
            ItemStack urna = LeonardsUrnItem.daquele(tamanho);
            if (LeonardsUrnItem.casas(urna) != tamanho + 1) {
                helper.fail("a de tamanho " + tamanho + " tem " + (tamanho + 1) + " casas");
                return;
            }
        }
        // e a que o Leonard larga é a pequena
        if (LeonardsUrnItem.casas(new ItemStack(OccultaItems.LEONARDS_URN)) != 1) {
            helper.fail("e a urna sem tamanho é a de uma casa");
        }
        helper.succeed();
    }

    /** <b>Só cozimento atirável entra nela.</b> */
    @GameTest(maxTicks = 20)
    public void onlyThrownBrewsFitInside(GameTestHelper helper) {
        ItemStack atirável = frascoAtirável();
        if (!Brew.splash(BrewItem.contents(atirável))) {
            helper.fail("o frasco da prova devia ser de atirar");
            return;
        }
        if (!LeonardsUrnItem.cabe(atirável)) helper.fail("e um frasco de atirar cabe");

        ItemStack debeber = BrewItem.of(OccultaItems.BREW, List.of(Items.NETHER_WART, Items.SUGAR));
        if (LeonardsUrnItem.cabe(debeber)) helper.fail("mas o de beber não cabe");
        if (LeonardsUrnItem.cabe(new ItemStack(Items.DIAMOND))) helper.fail("nem um diamante");
        helper.succeed();
    }

    /**
     * <b>Juntar duas faz a seguinte</b>, e sempre com a pequena por parceira.
     *
     * <p>Duas de uma casa dão a de duas; a de duas com uma de uma dá a de três; duas de duas não dão nada.
     */
    @GameTest(maxTicks = 20)
    public void twoUrnsMakeTheNextOne(GameTestHelper helper) {
        if (junta(LeonardsUrnItem.daquele(0), LeonardsUrnItem.daquele(0)) != 1) {
            helper.fail("duas de uma casa dão a de duas");
            return;
        }
        if (junta(LeonardsUrnItem.daquele(1), LeonardsUrnItem.daquele(0)) != 2) {
            helper.fail("a de duas com uma pequena dá a de três");
            return;
        }
        if (junta(LeonardsUrnItem.daquele(0), LeonardsUrnItem.daquele(2)) != 3) {
            helper.fail("e a ordem na bancada não importa");
            return;
        }
        if (junta(LeonardsUrnItem.daquele(1), LeonardsUrnItem.daquele(1)) != -1) {
            helper.fail("mas duas de duas não dão nada: a parceira é sempre a pequena");
            return;
        }
        if (junta(LeonardsUrnItem.daquele(3), LeonardsUrnItem.daquele(0)) != -1) {
            helper.fail("e a de quatro casas é o fim da linha");
            return;
        }
        if (junta(LeonardsUrnItem.daquele(0), new ItemStack(Items.DIAMOND)) != -1) {
            helper.fail("e um diamante não é urna");
        }
        helper.succeed();
    }

    /** O tamanho que a receita dá para aquelas duas, ou -1 se ela não dá nada. */
    private static int junta(ItemStack uma, ItemStack outra) {
        var entrada = CraftingInput.of(2, 1, List.of(uma, outra));
        ItemStack saiu = UrnJoiningRecipe.INSTANCE.assemble(entrada);
        if (saiu.isEmpty()) return -1;
        return LeonardsUrnItem.casas(saiu) - 1;
    }

    /** <b>Os quatro símbolos</b> existem, com os desenhos e os números do original. */
    @GameTest(maxTicks = 20)
    public void theFourSymbolsAreThere(GameTestHelper helper) {
        String[] nomes = {"leonard1", "leonard2", "leonard3", "leonard4"};
        for (int qual = 0; qual < 4; qual++) {
            Symbol símbolo = Symbols.daquele(43 + qual);
            if (símbolo == null) {
                helper.fail("falta o símbolo " + (43 + qual));
                return;
            }
            if (!símbolo.nome.equals(nomes[qual])) {
                helper.fail("o " + (43 + qual) + " chama-se " + nomes[qual] + "; chama-se " + símbolo.nome);
                return;
            }
        }
        /*
         * E os desenhos são os do original: <b>quatro traços</b>, e de <b>grau dois</b> — o primeiro
         * número de cada conjunto do original é o grau, e não um traço. São os únicos quatro símbolos do
         * mod que não têm desenho de grau um.
         */
        byte[] caelum = {0, 3, 3, 1};
        if (Symbols.doDesenho(caelum) != Symbols.daquele(43)) helper.fail("esse desenho é o Caelum");
        if (Symbols.grauDoDesenho(caelum) != 2) helper.fail("e é de grau dois");
        helper.succeed();
    }

    /**
     * <b>O custo deles sai do cozimento</b>, e é o único do mod que não está escrito no símbolo.
     *
     * <p>Sem urna, cinco. Com urna e frasco, o espaço gasto do cozimento <b>dobrado</b> — e nunca menos de
     * quatro. Com a <b>Adoração</b> de grau zero, metade a mais em vez do dobro; com ela de grau um ou
     * mais, o espaço <b>cru</b>, que é o engano do original e fica.
     */
    @GameTest(maxTicks = 40)
    public void theirCostComesFromTheBrewInside(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));
        Symbol caelum = Symbols.daquele(43);
        if (caelum == null) {
            helper.fail("o Caelum devia estar lá");
            return;
        }

        // sem urna nenhuma: cinco
        if (caelum.custo(quem, 1) != 5) {
            helper.fail("sem urna, cinco: deu " + caelum.custo(quem, 1));
            return;
        }

        ItemStack frasco = frascoAtirável();
        int espaço = Brew.capacity(BrewItem.contents(frasco)).used();
        ItemStack urna = LeonardsUrnItem.daquele(0);
        NonNullList<ItemStack> dentro = NonNullList.withSize(1, frasco);
        LeonardsUrnItem.guarda(urna, dentro);
        quem.getInventory().setItem(0, urna);

        int esperado = Math.max(espaço * 2, 4);
        if (caelum.custo(quem, 1) != esperado) {
            helper.fail("com frasco, o dobro do espaço: esperava " + esperado
                    + " e deu " + caelum.custo(quem, 1));
            return;
        }

        // e com Adoração de grau zero, metade a mais
        quem.addEffect(new net.minecraft.world.effect.MobEffectInstance(OccultaEffects.WORSHIP, 200, 0));
        int comAdoração = Math.max(espaço + (int) Math.ceil(espaço * 0.5), 4);
        if (caelum.custo(quem, 1) != comAdoração) {
            helper.fail("com Adoração, metade a mais: esperava " + comAdoração
                    + " e deu " + caelum.custo(quem, 1));
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E o símbolo tira o frasco da urna.</b>
     *
     * <p>É um desvio declarado: o original lê o frasco e deixa-o lá, de modo que a urna atira para sempre.
     */
    @GameTest(maxTicks = 20)
    public void castingTakesTheBrewOut(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));

        ItemStack urna = LeonardsUrnItem.daquele(1);
        NonNullList<ItemStack> dentro = NonNullList.withSize(2, ItemStack.EMPTY);
        dentro.set(1, frascoAtirável());
        LeonardsUrnItem.guarda(urna, dentro);
        quem.getInventory().setItem(3, urna);

        int onde = UrnPouch.naMochila(quem);
        if (onde != 3) {
            helper.fail("a urna está na quarta casa: achou " + onde);
            return;
        }
        if (UrnPouch.espreita(quem, onde, 0).isEmpty() != true) helper.fail("a casa de cima está vazia");
        if (UrnPouch.espreita(quem, onde, 1).isEmpty()) {
            helper.fail("e a de baixo tem o frasco");
            return;
        }
        if (!UrnPouch.espreita(quem, onde, 2).isEmpty()) {
            helper.fail("e a terceira não existe nesta urna");
            return;
        }

        ItemStack saiu = UrnPouch.tira(quem, onde, 1);
        if (saiu.isEmpty()) {
            helper.fail("tirar devia dar o frasco");
            return;
        }
        if (!UrnPouch.espreita(quem, onde, 1).isEmpty()) helper.fail("e a casa fica vazia");
        helper.succeed();
    }

    /** <b>E o que se põe dentro fica lá</b>, no próprio item, e não num baú escondido. */
    @GameTest(maxTicks = 20)
    public void whatGoesInStaysInTheItem(GameTestHelper helper) {
        ItemStack urna = LeonardsUrnItem.daquele(3);
        NonNullList<ItemStack> dentro = NonNullList.withSize(4, ItemStack.EMPTY);
        dentro.set(2, frascoAtirável());
        LeonardsUrnItem.guarda(urna, dentro);

        if (!urna.has(DataComponents.CONTAINER)) helper.fail("o que está dentro mora no próprio item");
        ItemStack cópia = urna.copy();
        if (LeonardsUrnItem.naCasa(cópia, 2).isEmpty()) helper.fail("e a cópia leva o que estava dentro");
        if (!LeonardsUrnItem.naCasa(cópia, 0).isEmpty()) helper.fail("e as outras casas ficam vazias");
        helper.succeed();
    }
}
