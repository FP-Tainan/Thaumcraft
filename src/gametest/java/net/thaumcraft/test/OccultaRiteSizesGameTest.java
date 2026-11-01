package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.Rites;
import net.thaumcraft.occulta.rite.Sacrifice;

import java.util.ArrayList;
import java.util.List;

/**
 * Os ritos que o original escreve <b>mais de uma vez</b>.
 *
 * <p>São sete nomes com duas ou três receitas cada, e isso não é repetição: é a <b>escada</b> do ofício.
 * Erguer a Terra levanta quatro, seis ou nove casas conforme o anel e a coisa que se queima; o vulcão
 * faz-se com pedra e Pedra Sintonizada ou com pedregulho e altar; cozer custa altar ou queima o dobro.
 *
 * <p>Nada disto está escrito em parte nenhuma do jogo, e um porte que registre só a primeira receita de
 * cada nome <b>não acusa nada</b> — os ritos que lá estão funcionam. O que se perde é metade do caminho.
 */
public class OccultaRiteSizesGameTest {
    /** Todas as receitas com aquele nome. */
    private static List<RiteRegistry.Entry> todas(String chave) {
        return RiteRegistry.all().stream().filter(qual -> qual.key().equals(chave)).toList();
    }

    /** O que uma receita pede de coisas no chão, de qualquer profundidade de {@code Both}. */
    private static List<Item> coisas(Sacrifice qual) {
        List<Item> tudo = new ArrayList<>();
        if (qual instanceof Sacrifice.Items itens) tudo.addAll(itens.wanted());
        if (qual instanceof Sacrifice.Both ambos) {
            for (Sacrifice cada : ambos.all()) tudo.addAll(coisas(cada));
        }
        return tudo;
    }

    /** Quanto de altar ela pede, ou zero. */
    private static float poder(Sacrifice qual) {
        if (qual instanceof Sacrifice.Power poder) return poder.amount();
        if (qual instanceof Sacrifice.Both ambos) {
            float soma = 0.0f;
            for (Sacrifice cada : ambos.all()) soma += poder(cada);
            return soma;
        }
        return 0.0f;
    }

    /** <b>Erguer a Terra vem em três</b>: quatro com pólvora, seis com redstone, nove com glowstone. */
    @GameTest(maxTicks = 20)
    public void raisingEarthComesInThree(GameTestHelper helper) {
        var todas = todas("tc.rite.raiseearth");
        if (todas.size() != 3) {
            helper.fail("são três receitas de erguer a terra: são " + todas.size());
            return;
        }
        /*
         * As três alturas, e <b>o anel de cada uma</b>: no original o tamanho diz qual é o anel — o de
         * dezesseis é o de dentro, o de vinte e oito o do meio, o de quarenta o de fora. Pôr o de vinte e
         * oito no lugar do de dentro dá um rito que não se consegue fazer, e é o que esta conta tranca.
         */
        int[] altura = {4, 6, 9};
        int[] glifos = {16, 28, 40};
        Item[] queima = {Items.GUNPOWDER, Items.REDSTONE, Items.GLOWSTONE_DUST};
        for (int i = 0; i < 3; i++) {
            var qual = todas.get(i);
            if (!(qual.rite() instanceof Rites.RaiseEarth erguer)) {
                helper.fail("a receita " + i + " devia erguer terra");
                return;
            }
            if (erguer.radius() != altura[i]) {
                helper.fail("a coluna " + i + " tem " + altura[i] + " casas: tem " + erguer.radius());
                return;
            }
            var anel = switch (i) {
                case 0 -> qual.inner();
                case 1 -> qual.middle();
                default -> qual.outer();
            };
            if (anel.ritual() != glifos[i]) {
                helper.fail("e pede um anel de " + glifos[i] + " glifos, no lugar dele");
                return;
            }
            if (!coisas(qual.sacrifice()).contains(queima[i])) {
                helper.fail("e queima " + queima[i] + ": queima " + coisas(qual.sacrifice()));
                return;
            }
        }
        helper.succeed();
    }

    /** <b>O vulcão vem em dois</b>: um paga com Pedra Sintonizada, o outro com altar. */
    @GameTest(maxTicks = 20)
    public void theVolcanoComesInTwo(GameTestHelper helper) {
        var todas = todas("tc.rite.volcano");
        if (todas.size() != 2) {
            helper.fail("são duas receitas de vulcão: são " + todas.size());
            return;
        }
        var comPedra = todas.stream()
                .filter(qual -> coisas(qual.sacrifice()).contains(OccultaItems.ATTUNED_STONE_CHARGED))
                .findFirst().orElse(null);
        var comAltar = todas.stream()
                .filter(qual -> coisas(qual.sacrifice()).contains(Items.COBBLESTONE))
                .findFirst().orElse(null);
        if (comPedra == null || comAltar == null) {
            helper.fail("uma com a Pedra Sintonizada carregada, outra com pedregulho");
            return;
        }
        if (poder(comPedra.sacrifice()) != 0.0f) {
            helper.fail("a da pedra não pede altar nenhum");
            return;
        }
        if (poder(comAltar.sacrifice()) != 2000.0f) {
            helper.fail("e a do pedregulho pede dois mil: pede " + poder(comAltar.sacrifice()));
            return;
        }
        helper.succeed();
    }

    /** <b>E cozer também</b>: um custa altar, o outro queima o dobro. */
    @GameTest(maxTicks = 20)
    public void cookingComesInTwo(GameTestHelper helper) {
        var todas = todas("tc.rite.cookfood");
        if (todas.size() != 2) {
            helper.fail("são duas receitas de cozer: são " + todas.size());
            return;
        }
        double menos = -1.0;
        double mais = -1.0;
        for (var qual : todas) {
            if (!(qual.rite() instanceof Rites.CookFood coze)) {
                helper.fail("as duas cozem comida");
                return;
            }
            if (poder(qual.sacrifice()) > 0.0f) menos = coze.burnChance();
            else mais = coze.burnChance();
        }
        if (menos != 0.08 || mais != 0.16) {
            helper.fail("a de altar queima oito por cento e a sem altar dezesseis: "
                    + menos + " e " + mais);
            return;
        }
        // e o rito repetido com outro nome já não está lá
        if (RiteRegistry.all().stream().anyMatch(qual -> qual.key().equals("tc.rite.cook"))) {
            helper.fail("o cozer estava registrado duas vezes, com dois nomes");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E as duas pedras de caminho</b>: a de lugar e a de gente, cada uma com os seus dois caminhos.
     *
     * <p>E a de gente pede uma <b>bola de gosma</b> — que estava escrita como balde de leite, por o
     * {@code field_151123_aH} do original se parecer com o do balde e não ser.
     */
    @GameTest(maxTicks = 20)
    public void bothWaystonesComeInTwo(GameTestHelper helper) {
        var fazer = todas("tc.rite.bindwaystonetoplayer");
        if (fazer.size() != 2) {
            helper.fail("são duas receitas da Pedra Sangrada: são " + fazer.size());
            return;
        }
        for (var qual : fazer) {
            if (!coisas(qual.sacrifice()).contains(Items.SLIME_BALL)) {
                helper.fail("as duas pedem uma bola de gosma: " + coisas(qual.sacrifice()));
                return;
            }
        }
        boolean semAltar = fazer.stream().anyMatch(qual -> poder(qual.sacrifice()) == 0.0f
                && coisas(qual.sacrifice()).contains(OccultaItems.ICY_NEEDLE));
        if (!semAltar) {
            helper.fail("e uma delas faz-se sem altar, com a Agulha de Gelo");
            return;
        }

        var ir = todas("tc.rite.teleporttowaystone");
        if (ir.size() != 2) {
            helper.fail("e são dois os ritos de ir ter com a pedra: são " + ir.size());
            return;
        }
        boolean aDeLugar = ir.stream()
                .anyMatch(qual -> coisas(qual.sacrifice()).contains(OccultaItems.BOUND_WAYSTONE));
        boolean aDeGente = ir.stream()
                .anyMatch(qual -> coisas(qual.sacrifice()).contains(OccultaItems.BLOODED_WAYSTONE));
        if (!aDeLugar || !aDeGente) {
            helper.fail("um leva à pedra do lugar, o outro à de gente");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>E o espelho pede um demônio vivo.</b>
     *
     * <p>O rito chama-se «prender um demônio num espelho», e sem o sacrifício o nome era uma mentira.
     */
    @GameTest(maxTicks = 20)
    public void theMirrorTrapsALiveDemon(GameTestHelper helper) {
        var espelho = RiteRegistry.get("tc.rite.mirror");
        if (espelho == null) {
            helper.fail("o rito do espelho devia estar no livro");
            return;
        }
        boolean vivo = vivo(espelho.sacrifice(), net.thaumcraft.occulta.OccultaEntities.DEMON);
        if (!vivo) helper.fail("ele pede um demônio vivo");
        else helper.succeed();
    }

    private static boolean vivo(Sacrifice qual, net.minecraft.world.entity.EntityType<?> quem) {
        if (qual instanceof Sacrifice.Living vida) return vida.what() == quem;
        if (qual instanceof Sacrifice.Both ambos) {
            for (Sacrifice cada : ambos.all()) {
                if (vivo(cada, quem)) return true;
            }
        }
        return false;
    }

    /** E os três bonecos de fetiche continuam a ser três. */
    @GameTest(maxTicks = 20)
    public void thereAreThreeFetishes(GameTestHelper helper) {
        if (todas("tc.rite.bindfetish").size() != 3) {
            helper.fail("são três bonecos: o espantalho, o ídolo e a escada");
            return;
        }
        helper.succeed();
    }
}
