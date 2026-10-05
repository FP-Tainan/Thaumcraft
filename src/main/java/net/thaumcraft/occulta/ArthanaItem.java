package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
 * <h2>E ela se deita no altar</h2>
 *
 * <p>Clicando no <b>topo de uma pedra de altar</b> com ar por cima, ela sai do inventário e fica
 * <b>deitada</b> ali, num {@link PlacedItemBlock}. O altar passa a contá-la e <b>dobra o alcance</b> dele:
 * dezesseis blocos viram trinta e dois.
 *
 * <p>É um gesto, e vale ver o que ele diz: a faca que abre o que os bichos guardam, pousada na pedra, faz o
 * altar <b>alcançar mais longe</b>. O original não explica, e não precisa.
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

    /**
     * <b>Deitada no altar.</b>
     *
     * <p>Só no <b>topo</b> de uma pedra de altar, e só se houver <b>ar</b> por cima dela. O original tira a
     * faca do lugar em que ela estava, e não da pilha — de modo que ela sai inteira, com o estrago e o nome
     * que tivesse.
     */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        Level mundo = onde.getLevel();
        BlockPos pedra = onde.getClickedPos();
        if (onde.getClickedFace() != Direction.UP) return super.useOn(onde);
        if (!mundo.getBlockState(pedra).is(OccultaBlocks.WITCH_ALTAR)) return super.useOn(onde);
        if (!mundo.getBlockState(pedra.above()).isAir()) return super.useOn(onde);

        if (!mundo.isClientSide()) {
            ItemStack faca = onde.getItemInHand();
            PlacedItemBlock.põe(mundo, pedra.above(), faca.copyWithCount(1), onde.getPlayer());
            faca.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
