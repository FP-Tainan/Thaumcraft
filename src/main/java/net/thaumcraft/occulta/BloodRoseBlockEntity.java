package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * O que a Rosa de Sangue guarda: a {@code TileEntityBloodRose} do Witchery.
 *
 * <p>Um nome, e só um. Pisando nela outra pessoa, o nome de dentro é <b>trocado</b> pelo novo — a rosa não
 * acumula, ela fica com o <b>último</b> que passou. É a diferença entre ela e o Baú de Sanguessugas do
 * original, que guarda uma lista; a flor é um bocado de memória e não um arquivo.
 *
 * <p>E ela guarda o <b>nome e a marca</b>, que é o que um {@linkplain TaglockItem Frasco de Vínculo} precisa.
 * Guardar só o nome serviria no original, que prendia bonecas por nome; aqui o vínculo é pela marca, e a
 * marca é o que se guarda.
 */
public class BloodRoseBlockEntity extends BlockEntity {
    @Nullable
    private TaglockItem.Taglock quem;

    public BloodRoseBlockEntity(BlockPos onde, BlockState state) {
        super(OccultaBlocks.BLOOD_ROSE_ENTITY, onde, state);
    }

    /**
     * Guarda quem pisou.
     *
     * <p>Quem já está lá dentro e pisa outra vez não a muda: ela já o tem, e trocar o mesmo pelo mesmo faria
     * a flor piscar a cada passo.
     *
     * @return se alguma coisa mudou
     */
    public boolean guarda(Player gente) {
        if (this.quem != null && this.quem.owner().equals(gente.getUUID())) return false;
        this.quem = new TaglockItem.Taglock(gente.getUUID(), gente.getGameProfile().name());
        this.setChanged();
        return true;
    }

    /** Põe lá dentro quem já vinha com ela: o que a Boline colheu. */
    public void põe(TaglockItem.Taglock quem) {
        this.quem = quem;
        this.setChanged();
    }

    /** O que ela tem, sem o tirar. */
    public @Nullable TaglockItem.Taglock vê() {
        return this.quem;
    }

    /** E o que ela tem, tirando: a flor fica vazia outra vez. */
    public @Nullable TaglockItem.Taglock tira() {
        TaglockItem.Taglock tinha = this.quem;
        if (tinha == null) return null;
        this.quem = null;
        this.setChanged();
        return tinha;
    }

    @Override
    protected void saveAdditional(ValueOutput dados) {
        super.saveAdditional(dados);
        if (this.quem != null) dados.store("Quem", TaglockItem.Taglock.CODEC, this.quem);
    }

    @Override
    protected void loadAdditional(ValueInput dados) {
        super.loadAdditional(dados);
        this.quem = dados.read("Quem", TaglockItem.Taglock.CODEC).orElse(null);
    }
}
