package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A <b>Mesa de Inscrição</b>: a {@code TileEntityInscriptionTable} do Ars Magica 2.
 *
 * <p>É onde um feitiço deixa de ser ideia e vira coisa. Põem-se as peças em fila — a Forma primeiro, depois as
 * Essências e os Modificadores dela —, e a mesa lê a frase, diz se ela faz sentido, e escreve o feitiço.
 *
 * <p><b>Ela não gasta as peças.</b> No original elas nem sequer são coisas: são <i>perícias</i>, aprendidas
 * numa árvore, e quem as sabe escreve com elas quantos feitiços quiser. Aqui viraram itens — porque a árvore
 * não está portada — mas o espírito fica: a mesa lê e devolve.
 */
public class InscriptionTableBlockEntity extends BlockEntity implements Container, MenuProvider {
    /** Quantas peças cabem numa frase: as nove casas da mesa. */
    public static final int RECIPE_SIZE = 9;

    /** E a casa de onde sai o feitiço escrito. */
    public static final int RESULT = RECIPE_SIZE;

    public static final int SIZE = RECIPE_SIZE + 1;

    private final NonNullList<ItemStack> itens = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    /** O que a última leitura disse da frase, para a tela mostrar. */
    private SpellValidator.Result leitura = SpellValidator.Result.EMPTY;

    public InscriptionTableBlockEntity(BlockPos onde, BlockState state) {
        super(ArcanaBlocks.INSCRIPTION_TABLE_ENTITY, onde, state);
    }

    /** As peças que estão na mesa, em fila, sem os buracos. */
    public List<SpellPart> recipe() {
        var peças = new ArrayList<SpellPart>();
        for (int i = 0; i < RECIPE_SIZE; i++) {
            SpellPart qual = SpellPartItem.of(this.itens.get(i));
            if (qual != null) peças.add(qual);
        }
        return List.copyOf(peças);
    }

    /** O que a mesa acha da frase que está nela. */
    public SpellValidator.Result reading() {
        return this.leitura;
    }

    /**
     * Relê a frase e escreve — ou apaga — o feitiço da casa de saída.
     *
     * <p>Corre a cada mexida numa casa, que é o que faz a mesa responder enquanto se escreve em vez de só no
     * fim.
     */
    public void reread() {
        List<SpellPart> peças = this.recipe();
        this.leitura = SpellValidator.validate(peças);

        if (!this.leitura.ok()) {
            this.itens.set(RESULT, ItemStack.EMPTY);
        } else {
            Spell feitiço = SpellValidator.build(peças);
            this.itens.set(RESULT,
                    SpellItem.write(new ItemStack(ArcanaItems.SPELL), feitiço));
        }
        this.setChanged();
    }

    // ------------------------------------------------------------------ o baú

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.itens.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int qual) {
        return this.itens.get(qual);
    }

    @Override
    public ItemStack removeItem(int qual, int quantos) {
        ItemStack saiu = ContainerHelper.removeItem(this.itens, qual, quantos);
        if (qual != RESULT) this.reread();
        return saiu;
    }

    @Override
    public ItemStack removeItemNoUpdate(int qual) {
        return ContainerHelper.takeItem(this.itens, qual);
    }

    @Override
    public void setItem(int qual, ItemStack isso) {
        this.itens.set(qual, isso);
        if (qual != RESULT) this.reread();
    }

    @Override
    public boolean stillValid(Player quem) {
        return Container.stillValidBlockEntity(this, quem);
    }

    @Override
    public void clearContent() {
        this.itens.clear();
        this.leitura = SpellValidator.Result.EMPTY;
    }

    /** Só peças de feitiço entram nas casas da frase. */
    @Override
    public boolean canPlaceItem(int qual, ItemStack isso) {
        return qual != RESULT && isso.getItem() instanceof SpellPartItem;
    }

    // ------------------------------------------------------------------ a tela

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.inscription_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory mochila, Player quem) {
        return new InscriptionMenu(id, mochila, this);
    }

    // ------------------------------------------------------------------ guardar

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.itens);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.itens.clear();
        ContainerHelper.loadAllItems(input, this.itens);
        this.leitura = SpellValidator.validate(this.recipe());
    }
}
