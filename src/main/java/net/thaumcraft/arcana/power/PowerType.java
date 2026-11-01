package net.thaumcraft.arcana.power;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

/**
 * Os <b>três Etheriums</b>: o {@code PowerTypes} da 1.4.0.009.
 *
 * <p>A energia do Ars Magica 2 não é uma só. Ela tem <b>tinta</b>, e a tinta importa: uma máquina pede um
 * tipo e não aceita outro, e uma fonte só dá o tipo que é dela. O Obelisco queima e dá <b>Neutro</b>; o
 * Aurem Negro mata e dá <b>Trevas</b>; o Prisma Celeste bebe do céu e dá <b>Luz</b>.
 *
 * <p>Os números são os do original, e são <b>bandeiras de bit</b> — 1, 2 e 4 — porque lá eles se somavam
 * numa máscara. Aqui não se somam em lado nenhum, mas guardam-se com esses números, que é o que faz um
 * mundo velho continuar legível.
 *
 * <p><b>Desvio declarado:</b> o original deixa qualquer mod registrar um quarto tipo com
 * {@code PowerTypes.RegisterPowerType}. <b>Nada no Ars Magica 2 chama esse método</b>, e aqui os tipos são
 * um enumerado — o que dá a um tipo de energia o mesmo peso que uma Afinidade tem neste porte: uma coisa
 * fechada, que se pode percorrer e guardar sem medo.
 */
public enum PowerType implements StringRepresentable {
    /** O que o Prisma Celeste tira do céu. */
    LIGHT(1, "light", ChatFormatting.AQUA),
    /** O que o Obelisco tira do que queima. */
    NEUTRAL(2, "neutral", ChatFormatting.DARK_BLUE),
    /** O que o Aurem Negro tira de quem morre nele. */
    DARK(4, "dark", ChatFormatting.DARK_RED);

    /** Os três, na ordem em que o {@code PowerTypes.all()} os devolve. */
    public static final PowerType[] ALL = values();

    private final int id;
    private final String nome;
    private final ChatFormatting cor;

    PowerType(int id, String nome, ChatFormatting cor) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
    }

    /** A bandeira de bit do original, que é como o tipo se guarda. */
    public int id() {
        return this.id;
    }

    /** A cor com que o original escreve o nome dele numa dica. */
    public ChatFormatting color() {
        return this.cor;
    }

    /** A chave de texto do nome dele. */
    public String key() {
        return "thaumcraft.power." + this.nome;
    }

    @Override
    public String getSerializedName() {
        return this.nome;
    }

    /** O {@code getByID}: um número que não é de nenhum deles não dá tipo nenhum. */
    public static PowerType byId(int id) {
        for (PowerType qual : ALL) {
            if (qual.id == id) return qual;
        }
        return null;
    }

    public static final com.mojang.serialization.Codec<PowerType> CODEC =
            StringRepresentable.fromEnum(PowerType::values);
}
