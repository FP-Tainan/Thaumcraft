package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O que uma Runa guarda: a {@code TileEntityGroundRuneSpell} do Ars Magica 2.
 *
 * <p>O feitiço que ela vai correr, <b>quantas vezes</b> ainda aguenta, e <b>quem a pôs</b> — que é quem ela não
 * dispara.
 *
 * <p>Um número de vezes <b>negativo</b> quer dizer <b>para sempre</b>: é o {@code setPermanent} do original, e
 * é o que separa uma armadilha de uma porta encantada.
 */
public class SpellRuneBlockEntity extends BlockEntity {
    private Spell spell = Spell.EMPTY;
    private int triggers = 1;
    private @Nullable UUID placedBy;

    public SpellRuneBlockEntity(BlockPos onde, BlockState state) {
        super(ArcanaBlocks.SPELL_RUNE_ENTITY, onde, state);
    }

    public Spell spell() {
        return this.spell;
    }

    public void setSpell(Spell feitiço) {
        this.spell = feitiço;
        this.setChanged();
    }

    public int triggers() {
        return this.triggers;
    }

    /** Quantas vezes ela ainda aguenta; um número negativo quer dizer para sempre. */
    public void setTriggers(int quantas) {
        this.triggers = quantas;
        this.setChanged();
    }

    public boolean permanent() {
        return this.triggers < 0;
    }

    public @Nullable UUID placedBy() {
        return this.placedBy;
    }

    public void setPlacedBy(@Nullable LivingEntity quem) {
        this.placedBy = quem == null ? null : quem.getUUID();
        this.setChanged();
    }

    /**
     * Acende a runa em quem pisou nela.
     *
     * <p>Quem lança não é quem a pôs: é a própria runa, e por isso o feitiço <b>não custa mana a ninguém</b> —
     * ela já foi paga quando foi desenhada. No original isso é feito com um jogador de mentira de nível 99;
     * aqui quem lança é quem pisou, e como o gasto só sai de quem lança em modo de sobrevivência, o efeito é o
     * mesmo sem inventar um jogador.
     *
     * @return se a frase pegou
     */
    public boolean trigger(ServerLevel level, LivingEntity quem) {
        if (this.spell.isEmpty()) return false;
        return SpellCast.cast(level, this.spell, quem, quem, quem.position()).ok();
    }

    /**
     * Gasta uma das vezes que ela tinha.
     *
     * @return se ela acabou e deve sumir do chão
     */
    public boolean spend() {
        if (this.permanent()) return false;
        this.triggers--;
        this.setChanged();
        return this.triggers <= 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("spell", Spell.CODEC, this.spell);
        output.putInt("triggers", this.triggers);
        if (this.placedBy != null) output.store("placed_by", net.minecraft.core.UUIDUtil.CODEC, this.placedBy);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.spell = input.read("spell", Spell.CODEC).orElse(Spell.EMPTY);
        this.triggers = input.getIntOr("triggers", 1);
        this.placedBy = input.read("placed_by", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }
}
