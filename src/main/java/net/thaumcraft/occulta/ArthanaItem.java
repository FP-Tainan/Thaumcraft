package net.thaumcraft.occulta;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * A Arthana: o {@code ItemArthana} do Witchery.
 *
 * <p>É a faca do ofício, e não é arma de guerra — <b>ouro, com a vida do ferro</b>. O que ela faz não é cortar
 * melhor: é <b>abrir o que os bichos guardam</b>. Quem mata com ela na mão tira do esqueleto, do zumbi e do
 * creeper a <b>caveira</b> deles, tira <b>Pó Espectral</b> dos mortos-vivos, e tira o que o pote pede — a
 * Língua de Cão, o Dedo de Sapo, a Lã de Morcego e o Coração de Creeper — muito mais vezes.
 *
 * <p>O ouro é escolha do original, e faz sentido nela: é o metal que não serve para lutar.
 *
 * <p><b>Desvio declarado:</b> no original ela também se <b>pousa no Altar</b>, com um {@code BlockPlacedItem} que
 * a deixa à vista em cima da pedra. Esse bloco não está portado — nem para ela, nem para as outras coisas que o
 * original pousa lá —, e entra quando ele entrar.
 */
public class ArthanaItem extends Item {
    /**
     * De que ela é feita: o ouro do jogo com a vida do ferro, que é o que o
     * {@code func_77656_e(ToolMaterial.IRON.func_77997_a())} do original faz.
     */
    public static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_GOLD_TOOL, ToolMaterial.IRON.durability(), 12.0f, 0.0f, 22,
            ItemTags.GOLD_TOOL_MATERIALS);

    public ArthanaItem(Properties properties) {
        super(properties);
    }
}
