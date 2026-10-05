package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A alma da <b>Tina de Prata</b>: a {@code TileEntitySilverVat} do Witchery.
 *
 * <p>Ela guarda <b>uma coisa só</b> — o pó de prata que foi juntando — e faz <b>uma coisa só</b>: olha o
 * que as máquinas ao lado dela estão produzindo, e cada vez que a pilha de <b>lingotes de ouro</b> de uma
 * delas <b>cresce</b>, há <b>uma chance em cinco</b> de aparecer um pó de prata aqui dentro.
 *
 * <h2>O que ela é, de verdade</h2>
 *
 * <p>Não é um forno, não é um caldeirão e não tem receita: é uma <b>bacia encostada à fornalha</b> que
 * apanha o que escorre. O original não explica a física e não precisa — o que ele diz é que <b>fundir ouro
 * suja alguma coisa</b>, e a tina é onde a sujeira assenta.
 *
 * <p>Ela só olha para a <b>casa de saída</b> das máquinas: a prateleira de onde se pode <b>tirar</b> e na
 * qual não se pode <b>pôr</b>. É assim que ela sabe distinguir o ouro que entrou do ouro que saiu.
 */
public class SilverVatBlockEntity extends BlockEntity {
    /** De quantas em quantas batidas ela olha para os lados. */
    public static final int OLHA = 20;

    /** E de quantas em quantas vezes sai pó: a uma em cinco do original. */
    public static final int UMA_EM_CINCO = 5;

    /** Quanto pó cabe numa camada do desenho, e quantas camadas há. */
    public static final int POR_CAMADA = 8;
    public static final int CAMADAS = 8;

    private ItemStack prata = ItemStack.EMPTY;
    private final int[] últimas = new int[Direction.values().length];

    public SilverVatBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.SILVER_VAT_ENTITY, onde, feitio);
    }

    /** O que ela tem dentro. */
    public ItemStack prata() {
        return this.prata;
    }

    public void prata(ItemStack oquê) {
        this.prata = oquê;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    Block.UPDATE_ALL);
        }
    }

    /** Quantas camadas de prata o desenho mostra: a conta do original, no máximo oito. */
    public int camadas() {
        if (this.prata.isEmpty()) return 0;
        return Math.min(Math.max(this.prata.getCount() / POR_CAMADA, 1), CAMADAS);
    }

    /**
     * <b>A batida dela.</b>
     *
     * <p>De segundo em segundo, olha os quatro lados. Numa máquina que tenha uma casa de saída com
     * <b>lingotes de ouro</b>, se a pilha estiver maior do que da última vez que ela olhou, <b>uma vez em
     * cinco</b> aparece um pó de prata aqui.
     *
     * <p><b>Desvio declarado:</b> o original é avisado pelo Forge sempre que a alma de um vizinho muda, e
     * olha só nessa hora. O jogo de hoje não tem esse aviso; aqui ela olha sozinha, uma vez por segundo. O
     * que se vê é o mesmo, só que o relógio é dela e não da fornalha.
     */
    public static void bate(ServerLevel level, BlockPos onde, SilverVatBlockEntity tina) {
        for (Direction rumo : Direction.Plane.HORIZONTAL) {
            BlockPos ali = onde.relative(rumo);
            if (!(level.getBlockEntity(ali) instanceof Container caixa)) continue;

            /*
             * A <b>face do vizinho virada para a tina</b>, que é o lado por onde ele deixa tirar. É o
             * {@code side} que o original calcula a partir da diferença de posição.
             */
            Direction face = rumo.getOpposite();
            for (int casa = 0; casa < caixa.getContainerSize(); casa++) {
                if (!éSaída(caixa, casa, face)) continue;
                ItemStack tem = caixa.getItem(casa);
                if (!tem.is(Items.GOLD_INGOT)) continue;

                if (tem.getCount() > tina.últimas[face.ordinal()]
                        && level.getRandom().nextInt(UMA_EM_CINCO) == 0) {
                    tina.escorre();
                }
                tina.últimas[face.ordinal()] = tem.getCount();
                break;
            }
        }
    }

    /** Se aquela casa é de saída: dá para tirar por ali e não dá para pôr. */
    private static boolean éSaída(Container caixa, int casa, Direction face) {
        if (!(caixa instanceof WorldlyContainer lados)) return true;
        ItemStack ouro = new ItemStack(Items.GOLD_INGOT);
        return lados.canTakeItemThroughFace(casa, ouro, face)
                && !lados.canPlaceItemThroughFace(casa, ouro, face);
    }

    /** E o pó que assenta. */
    private void escorre() {
        if (this.prata.isEmpty()) {
            this.prata(new ItemStack(OccultaItems.SILVER_DUST));
            return;
        }
        if (this.prata.getCount() >= this.prata.getMaxStackSize()) return;
        this.prata.grow(1);
        this.prata(this.prata);
    }

    // ------------------------------------------------------------------ o que fica guardado

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        if (!this.prata.isEmpty()) saída.store("Silver", ItemStack.CODEC, this.prata);
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.prata = entrada.read("Silver", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /** O que ela tem dentro atravessa a rede, porque o desenhista o mostra em camadas. */
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registros) {
        return this.saveCustomOnly(registros);
    }
}
