package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.block.ArcaneEarBlock;
import net.thaumcraft.registry.TCBlockEntities;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * O {@code TileSensor} da 4.2.3.5: a nota (0 a 24) e o instrumento que o ouvido escuta, e o sinal de dez tiques que ele
 * dá quando ouve. As notas tocadas no mundo chegam por {@link #heard}, pelo gancho no bloco musical.
 */
public class ArcaneEarBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    /** Um bloco musical que tocou neste tique: onde, que instrumento e que nota. */
    public record Note(BlockPos pos, int instrument, int note) {
    }

    /** O {@code noteBlockEvents}: as notas do tique, por mundo. */
    private static final Map<ServerLevel, List<Note>> EVENTS = new WeakHashMap<>();

    public byte note;
    public byte tone;
    public int redstoneSignal;

    public ArcaneEarBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_EAR, pos, state);
    }

    public static void heard(ServerLevel level, BlockPos pos, int instrument, int note) {
        EVENTS.computeIfAbsent(level, l -> new java.util.ArrayList<>()).add(new Note(pos.immutable(), instrument, note));
    }

    /** O fim do tique do mundo apaga as notas dele. */
    public static void endTick(ServerLevel level) {
        List<Note> list = EVENTS.get(level);
        if (list != null) list.clear();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneEarBlockEntity ear) {
        if (!(level instanceof ServerLevel server)) return;
        if (ear.redstoneSignal > 0) {
            ear.redstoneSignal--;
            if (ear.redstoneSignal == 0) ear.setPowered(server, pos, false);
        }
        List<Note> notes = EVENTS.get(server);
        if (notes == null) return;
        for (Note heard : notes) {
            if (heard.instrument() == ear.tone && heard.note() == ear.note && heard.pos().distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 4096.0) {
                ear.triggerNote(server, pos, false);
                ear.redstoneSignal = 10;
                ear.setPowered(server, pos, true);
                break;
            }
        }
    }

    private void setPowered(ServerLevel level, BlockPos pos, boolean powered) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ArcaneEarBlock && state.getValue(ArcaneEarBlock.POWERED) != powered) {
            level.setBlock(pos, state.setValue(ArcaneEarBlock.POWERED, powered), 3);
            level.updateNeighborsAt(pos, state.getBlock());
            level.updateNeighborsAt(pos.below(), state.getBlock());
        }
    }

    /** O {@code updateTone}: o instrumento do bloco de baixo. */
    public void updateTone() {
        if (this.level == null) return;
        byte old = this.tone;
        this.tone = (byte) this.level.getBlockState(this.worldPosition.below()).instrument().ordinal();
        if (old != this.tone) this.setChanged();
    }

    /** O {@code changePitch}: a próxima das 25 notas. */
    public void changePitch() {
        this.note = (byte) ((this.note + 1) % 25);
        this.setChanged();
    }

    /** O {@code triggerNote}: com ar em cima, toca a nota (com o som só quando é a mão que mexe). */
    public void triggerNote(Level level, BlockPos pos, boolean sound) {
        if (!level.getBlockState(pos.above()).isAir()) return;
        int instrument = sound ? this.level.getBlockState(pos.below()).instrument().ordinal() : -1;
        level.blockEvent(pos, level.getBlockState(pos).getBlock(), instrument, this.note);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.note = (byte) Math.max(0, Math.min(24, input.getByteOr("note", (byte) 0)));
        this.tone = input.getByteOr("tone", (byte) 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("note", this.note);
        output.putByte("tone", this.tone);
    }

    public static NoteBlockInstrument instrument(int ordinal) {
        return NoteBlockInstrument.values()[Math.max(0, Math.min(NoteBlockInstrument.values().length - 1, ordinal))];
    }
}
