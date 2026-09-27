package net.thaumcraft.occulta.brew;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Um cozimento: a lista do que caiu no caldeirão, pela ordem, e tudo o que se lê dela.
 *
 * <p>No Witchery isto vive no NBT do líquido do caldeirão — a lista {@code Items} e, ao lado, a cor, o poder, o
 * nome e o espaço gasto, todos <b>recontados</b> de cada vez que alguma coisa cai lá dentro. Aqui é a mesma
 * coisa: quem manda é a lista, e o resto se lê dela. Por isso não há estado a guardar além dos ingredientes.
 *
 * <p>A ordem importa em tudo: o tempero vale para o efeito <b>seguinte</b>, a cor é uma conta encadeada e o que
 * apaga só apaga o que veio <b>antes</b>.
 */
public final class Brew {
    private Brew() {
    }

    /** As ações de cada coisa da lista, na ordem — o que o {@code BrewActionList} do original monta. */
    public static List<BrewAction> actions(List<Item> dentro) {
        List<BrewAction> saída = new ArrayList<>();
        for (Item item : dentro) {
            BrewAction ação = BrewRegistry.of(item);
            if (ação != null) saída.add(ação);
        }
        return saída;
    }

    /**
     * O {@code canAdd} do original: se este ingrediente ainda cabe.
     *
     * <p>São três perguntas. A do próprio ingrediente ({@link BrewAction#canAdd}); a de <b>não repetir</b> — o
     * mesmo ingrediente não entra duas vezes seguidas sem um efeito de permeio; e a do <b>espaço</b>, que é a que
     * recusa o efeito que não cabe mais no caldeirão.
     */
    public static boolean canAdd(List<Item> dentro, Item novo, boolean cheio) {
        BrewAction ação = BrewRegistry.of(novo);
        if (ação == null) return false;
        List<BrewAction> ações = actions(dentro);
        BrewCapacity espaço = new BrewCapacity();
        boolean jáEstá = false;
        for (BrewAction anterior : ações) {
            anterior.augmentCapacity(espaço);
            if (anterior == ação) jáEstá = true;
            else if (anterior.isEffect()) jáEstá = false;
        }
        if (!ação.canAdd(ações, cheio, espaço.hasEffects())) return false;
        return !jáEstá && ação.augmentCapacity(espaço);
    }

    /** O que sobra da lista depois de o ingrediente novo apagar o que tinha de apagar. */
    public static List<Item> add(List<Item> dentro, Item novo) {
        BrewAction ação = BrewRegistry.of(novo);
        if (ação == null) return List.copyOf(dentro);
        List<Item> itens = new ArrayList<>(dentro);
        ação.processNullification(itens);
        itens.add(novo);
        return List.copyOf(itens);
    }

    /** O espaço do caldeirão como está: quanto se abriu, quanto se gastou e quantos efeitos entraram. */
    public static BrewCapacity capacity(List<Item> dentro) {
        BrewCapacity espaço = new BrewCapacity();
        for (BrewAction ação : actions(dentro)) ação.augmentCapacity(espaço);
        return espaço;
    }

    /** O poder que o altar tem de dar para este cozimento sair: a soma do que cada coisa custa. */
    public static int power(List<Item> dentro) {
        int total = 0;
        for (BrewAction ação : actions(dentro)) total += ação.power();
        return total;
    }

    /** A cor do caldo: a conta encadeada do original, ou a cor que a lã mandou. */
    public static int color(List<Item> dentro) {
        int cor = 0;
        for (BrewAction ação : actions(dentro)) cor = ação.augmentColor(cor);
        return cor & 0xFFFFFF;
    }

    /** Se este cozimento se atira em vez de se beber. */
    public static boolean splash(List<Item> dentro) {
        for (BrewAction ação : actions(dentro)) {
            if (ação.createsSplash()) return true;
        }
        return false;
    }

    /** Quanto se leva a beber: as trinta e duas batidas do jogo, mudadas pelos ingredientes, nunca menos de duas. */
    public static int drinkSpeed(List<Item> dentro) {
        int quanto = 32;
        for (BrewAction ação : actions(dentro)) quanto += ação.drinkSpeedModifier();
        return Math.max(quanto, 2);
    }

    /** O nome do frasco. */
    public static Component name(List<Item> dentro) {
        BrewName.Builder montador = new BrewName.Builder(true);
        for (BrewAction ação : actions(dentro)) {
            if (ação.namePart() != null) ação.namePart().applyTo(montador);
        }
        return montador.build();
    }

    /** E a descrição, uma linha por efeito. */
    public static List<Component> lines(List<Item> dentro) {
        BrewName.Builder montador = new BrewName.Builder(false);
        for (BrewAction ação : actions(dentro)) {
            if (ação.namePart() != null) ação.namePart().applyTo(montador);
        }
        return montador.lines();
    }

    /**
     * O que o cozimento faz em quem o bebe.
     *
     * <p>Percorre a lista pela ordem: cada ingrediente que <b>cabe no espaço</b> tempera e aplica; o que não
     * cabe é como se não estivesse lá. É o {@code applyToEntity} do {@code BrewActionList}.
     */
    public static void apply(Level level, LivingEntity quem, List<Item> dentro, BrewModifiers temperos) {
        for (BrewAction ação : actions(dentro)) {
            if (!ação.augmentCapacity(temperos.capacity())) continue;
            ação.augmentModifiers(temperos);
            ação.applyToEntity(level, quem, temperos);
        }
    }
}
