package net.thaumcraft.occulta.symbol;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Um <b>símbolo</b>: a {@code SymbolEffect} do Witchery.
 *
 * <p>É a parte do mod que mais se parece com magia de livro, e a que ele copiou de onde toda gente sabe:
 * cada símbolo tem um <b>nome latino</b> e um <b>desenho</b> que se faz no ar com a Vara Mística, movendo a
 * cabeça. Acertando o desenho, o nome aparece; largando a vara, o feitiço sai.
 *
 * <h2>O desenho</h2>
 *
 * <p>Um símbolo é uma <b>sequência de traços</b>, e um traço é uma das quatro direções em que se pode virar
 * a cabeça: <b>cima</b>, <b>baixo</b>, <b>direita</b>, <b>esquerda</b>. Cada sete graus de giro contam um
 * traço, e quinze traços é o máximo.
 *
 * <p>E cada símbolo tem <b>três desenhos</b>, um por grau: o de grau um é curto, e os de dois e três
 * repetem traços para ficarem mais longos. Quanto mais comprido o desenho, mais forte o feitiço — e mais
 * fácil de errar.
 *
 * <h2>O que ele custa</h2>
 *
 * <p>O custo é <b>dobrado por grau</b>: um feitiço de custo um gasta uma carga no grau um, duas no dois e
 * quatro no três. E o grau só vale se quem o lança tiver <b>Adoração</b> bastante — sem ela, um desenho de
 * grau três é lançado como grau um, e o esforço foi para nada.
 */
public abstract class Symbol {
    /** O número dele, que é o que atravessa a rede. */
    public final int id;

    /** O nome dele no idioma. */
    public final String nome;

    /** O que ele custa no grau um. */
    public final int custo;

    /** Se ele é uma <b>maldição</b>, e se é das que não se perdoam. */
    public final boolean maldição;
    public final boolean imperdoável;

    /** E quanto tempo até se poder repetir. */
    public final int trava;

    /**
     * A <b>chave de saber</b> deste símbolo, ou nada se ele não pede nenhuma.
     *
     * <p>Quatro dos trinta e um pedem uma, e ela não vem de livro: vem de um <b>gole</b>. Veja o
     * {@link SymbolKnowledge}.
     */
    public final @org.jetbrains.annotations.Nullable String chave;

    protected Symbol(int id, String nome, int custo, boolean maldição, boolean imperdoável, int trava) {
        this(id, nome, custo, maldição, imperdoável, trava, null);
    }

    protected Symbol(int id, String nome, int custo, boolean maldição, boolean imperdoável, int trava,
                     @org.jetbrains.annotations.Nullable String chave) {
        this.id = id;
        this.nome = nome;
        this.custo = custo;
        this.maldição = maldição;
        this.imperdoável = imperdoável;
        this.trava = trava;
        this.chave = chave;
    }

    protected Symbol(int id, String nome) {
        this(id, nome, 1, false, false, 0);
    }

    /** <b>O feitiço.</b> */
    public abstract void lança(ServerLevel level, ServerPlayer quem, int grau);

    /**
     * O que ele custa naquele grau: o custo dobrado por grau, como no original.
     */
    public int custo(int grau) {
        return (int) Math.floor(Math.pow(2.0, grau - 1) * this.custo);
    }

    /**
     * Se a infusão que ele tem serve para este símbolo.
     *
     * <p>Os <b>imperdoáveis</b> — e o original lhes chama isso — só se lançam com a <b>Infusão
     * Infernal</b>. Os outros se servem de qualquer uma. No criativo, tudo serve.
     */
    public boolean serveAInfusão(ServerPlayer quem, int infusão) {
        if (quem.getAbilities().instabuild) return true;
        if (infusão <= 0) return false;
        return !this.imperdoável || infusão == Symbols.A_INFERNAL;
    }

    /**
     * E se aquela pessoa <b>sabe</b> este símbolo.
     *
     * <p>Quem não pede chave é sabido por todos. Quem pede, só por quem bebeu o Cozimento de Alma que
     * a dá.
     */
    public boolean oSabe(ServerPlayer quem) {
        return this.chave == null || SymbolKnowledge.knows(quem, this.chave);
    }
}
