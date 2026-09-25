package net.thaumcraft.api.aspects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Um aspecto: o tijolo de que a magia do Thaumcraft é feita.
 *
 * <p>Seis são primários — ar, terra, fogo, água, ordem e perdição — e não vêm de nada. Todos os outros nascem
 * do encontro de dois, e é essa árvore que o jogador vai descobrindo no tabuleiro da pesquisa.
 *
 * <p>Porte fiel do {@code thaumcraft.api.aspects.Aspect} da versão 4.2.3.5: os nomes em latim, as cores e os
 * pares de composição são exatamente os do mod original.
 */
public final class Aspect {
    /** Todos os aspectos, na ordem em que foram declarados. */
    public static final Map<String, Aspect> ASPECTS = new LinkedHashMap<>();

    private final String tag;
    private final int color;
    private final Aspect[] components;
    private final String chatColor;
    /** Como o símbolo se mistura na tela: somando luz ou por cima. Perdição e vazio usam o segundo. */
    private final int blend;
    private final Identifier image;

    private Aspect(String tag, int color, Aspect[] components, String chatColor, int blend) {
        this.tag = tag;
        this.color = color;
        this.components = components;
        this.chatColor = chatColor;
        this.blend = blend;
        this.image = Thaumcraft.id("textures/aspects/" + tag + ".png");
        ASPECTS.put(tag, this);
    }

    /** Um aspecto primário: não se decompõe em nada. */
    static Aspect primal(String tag, int color, String chatColor, int blend) {
        return new Aspect(tag, color, null, chatColor, blend);
    }

    /** Um aspecto composto: nasce do encontro de dois outros. */
    static Aspect compound(String tag, int color, Aspect first, Aspect second) {
        return new Aspect(tag, color, new Aspect[]{first, second}, null, 1);
    }

    /** Um composto que se mistura por cima em vez de somar luz. */
    static Aspect compoundDark(String tag, int color, Aspect first, Aspect second) {
        return new Aspect(tag, color, new Aspect[]{first, second}, null, 771);
    }

    /**
     * Um aspecto de fora do mod, como o {@code new Aspect(tag, cor, componentes, imagem, mistura)} que os addons do
     * original usavam. A imagem sai do mesmo lugar que a dos outros: {@code textures/aspects/<nome>.png}.
     *
     * @param blend 1 soma luz, 771 mistura por cima (é o que perdição e vazio fazem)
     */
    public static Aspect of(String tag, int color, Aspect first, Aspect second, int blend) {
        return new Aspect(tag, color, new Aspect[]{first, second}, null, blend);
    }

    public String tag() {
        return this.tag;
    }

    public int color() {
        return this.color;
    }

    /** Os dois que formam este, ou nulo se ele for primário. */
    public Aspect[] components() {
        return this.components;
    }

    public boolean isPrimal() {
        return this.components == null;
    }

    public int blend() {
        return this.blend;
    }

    public Identifier image() {
        return this.image;
    }

    /** A cor que o nome dele ganha no bate-papo, nos aspectos primários. */
    public String chatColor() {
        return this.chatColor == null ? "f" : this.chatColor;
    }

    /** O nome na língua de quem joga. */
    public Component name() {
        return Component.translatable("tc.aspect." + this.tag);
    }

    public static Aspect of(String tag) {
        return ASPECTS.get(tag);
    }

    @Override
    public String toString() {
        return this.tag;
    }
}
