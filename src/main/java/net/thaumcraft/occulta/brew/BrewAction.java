package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * O que um ingrediente faz ao cozimento: o {@code BrewAction} do Witchery.
 *
 * <p>Cada coisa que cai no caldeirão é uma destas. Umas <b>abrem espaço</b> (a Raiz de Mandrágora), outras
 * <b>gastam</b> esse espaço com um efeito (o olho de aranha), outras <b>temperam</b> o efeito seguinte (o pó de
 * pedra luminosa), outras <b>pintam</b> o caldo (a lã tinta) e outras dizem <b>como</b> o cozimento se espalha
 * (a pólvora, a lã de morcego). Esta fatia traz as quatro primeiras; o espalhamento vem depois.
 *
 * <p>A <b>cor</b> não é escolhida: sai de uma conta que mistura o que já havia com o nome do ingrediente
 * ({@code 37 * cor + chave}), e por isso cada receita tem a sua e duas receitas parecidas saem parecidas. Só a lã
 * tinta manda na cor à força.
 */
public abstract class BrewAction {
    public final Item key;
    protected final BrewName.Part namePart;
    protected final int power;
    protected final boolean createsSplash;
    protected final int forcedColor;

    private final List<Item> nullifiers = new ArrayList<>();
    private final List<Item> priorNullifiers = new ArrayList<>();
    private int yieldBonus;
    private int yieldPenalty;

    protected BrewAction(Item key, BrewName.Part namePart, int power) {
        this(key, namePart, power, false, -1);
    }

    protected BrewAction(Item key, BrewName.Part namePart, int power, boolean createsSplash, int forcedColor) {
        this.key = key;
        this.namePart = namePart;
        this.power = power;
        this.createsSplash = createsSplash;
        this.forcedColor = forcedColor;
    }

    public final BrewName.Part namePart() {
        return this.namePart;
    }

    public final int power() {
        return this.power;
    }

    public final boolean createsSplash() {
        return this.createsSplash;
    }

    /** O {@code augmentColor}: a cor nova sai da que havia com a chave deste ingrediente. */
    public final int augmentColor(int cor) {
        if (this.forcedColor != -1) return this.forcedColor;
        if (cor == 0) cor = 17;
        return 37 * cor + this.key.hashCode();
    }

    /** Quantos frascos a mais ou a menos este ingrediente dá: o {@code ModifierYield} do original. */
    public final BrewAction yield(int quanto) {
        if (quanto < 0) this.yieldPenalty = Math.abs(quanto);
        else this.yieldBonus = quanto;
        return this;
    }

    public final int yieldBonus() {
        return this.yieldBonus;
    }

    public final int yieldPenalty() {
        return this.yieldPenalty;
    }

    /** O que este ingrediente apaga do que já está no caldeirão. */
    public final BrewAction nullifies(Item outro, boolean sóOAnterior) {
        (sóOAnterior ? this.priorNullifiers : this.nullifiers).add(outro);
        return this;
    }

    /**
     * O {@code processNullifaction}: tira do caldeirão o que este ingrediente desfaz.
     *
     * <p>No original isto anda em duas listas ao mesmo tempo — a das ações e a dos itens —, e é sobre a dos itens
     * que se mexe aqui, porque é ela que manda: cada coisa que está no caldeirão é uma ação, e só entra o que a
     * tabela conhece.
     *
     * <p>O caso do {@code onlyPrior} — desfazer só o que veio logo antes — não é usado por ingrediente nenhum da
     * tabela do original; fica feito do jeito direito, sem o deslocamento de um que o original tem lá.
     */
    public final void processNullification(List<Item> dentro) {
        if (!this.priorNullifiers.isEmpty() && !dentro.isEmpty()
                && this.priorNullifiers.contains(dentro.getLast())) {
            dentro.removeLast();
        }
        if (this.nullifiers.isEmpty()) return;
        for (int i = dentro.size() - 1; i >= 0; i--) {
            if (this.nullifiers.contains(dentro.get(i))) dentro.remove(i);
        }
    }

    /** Se este ingrediente cabe ainda, olhando o caldeirão como está. */
    public boolean canAdd(List<BrewAction> dentro, boolean cheio, boolean temEfeitos) {
        return true;
    }

    /** Quanto tempo se leva a beber: o {@code getDrinkSpeedModifiers}. */
    public int drinkSpeedModifier() {
        return 0;
    }

    /** Se este ingrediente é um efeito — o que conta para a ordem em que se podem repetir as coisas. */
    public boolean isEffect() {
        return false;
    }

    /** O {@code augmentEffectLevels}: abre ou gasta espaço, e diz se coube. */
    public abstract boolean augmentCapacity(BrewCapacity espaço);

    /** O {@code prepareSplashPotion}: quem manda no jeito de espalhar diz aqui qual é. */
    public void prepareImpact(BrewImpact espalha) {
    }

    /** O {@code augmentEffectModifiers}: o tempero que espera pelo efeito seguinte. */
    public void augmentModifiers(BrewModifiers temperos) {
    }

    /** O que ele faz a quem bebe. */
    public void applyToEntity(Level level, LivingEntity quem, BrewModifiers temperos) {
    }

    /** E o que ele faz ao lugar, quando o cozimento se derrama. */
    public void applyToBlock(ServerLevel level, BlockPos onde, Direction lado, int raio, BrewModifiers temperos) {
    }
}
