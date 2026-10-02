package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;

import java.util.ArrayList;
import java.util.List;

/**
 * O coração de um círculo de giz: o {@code TileEntityCircle} do Witchery.
 *
 * <p>Ele guarda os ritos que estão rodando e corre <b>um passo por batida</b> do primeiro da fila. Os que se
 * sustentam sozinhos vão para outra fila, e essa corre sempre o mesmo passo até desistir.
 *
 * <p>Quem bate no glifo com um rito a correr <b>desiste</b> dele, e o que se ofereceu volta para o chão.
 *
 * <p><b>Os ritos se guardam em disco</b>, como no original: desligado o mundo, o rito continua de onde estava.
 * O que se guarda não são os passos — é o <b>nome do rito</b> e <b>quantos passos faltam</b>, e a fila é
 * remontada da lista de ritos ao voltar. Um rito cujo nome já não exista é largado, e o que se ofereceu volta
 * para o chão.
 */
public class CircleHeartBlockEntity extends BlockEntity {
    private final List<ActiveRite> running = new ArrayList<>();
    private final List<ActiveRite> upkeep = new ArrayList<>();
    private boolean abortNext;
    private long ticks;

    public CircleHeartBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.CIRCLE_HEART_ENTITY, pos, state);
    }

    public boolean busy() {
        return !this.running.isEmpty() || !this.upkeep.isEmpty();
    }

    /** O {@code activateBlock}: bate-se no glifo e o círculo procura o que ele pode fazer. */
    public void toggle(ServerLevel level, Player quem) {
        if (this.busy()) {
            this.abortNext = true;
            this.upkeep.clear();
            return;
        }

        var círculos = RitualCircles.read(level, this.worldPosition);
        var noChão = net.thaumcraft.occulta.rite.Sacrifice.onTheGround(level, this.worldPosition);
        List<RiteRegistry.Entry> achados = RiteRegistry.find(level, this.worldPosition, círculos, noChão);

        if (achados.isEmpty()) {
            if (quem != null) quem.sendSystemMessage(Component.translatable("tc.rite.unknown"));
            level.playSound(null, this.worldPosition, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS,
                    1.0f, 1.0f);
            return;
        }

        // O tamanho do coven de quem começou. Até a fatia da Bruxa do Coven isto era um zero escrito na mão,
        // porque não havia quem respondesse: o Rite.steps(int coven) estava aqui à espera desde que os
        // círculos entraram, e todo rito que cresce com bruxas em volta corria no mínimo. Agora há.
        int coven = quem == null ? 0 : net.thaumcraft.occulta.coven.Coven.tamanho(quem);

        for (RiteRegistry.Entry rito : achados) {
            this.running.add(new ActiveRite(rito.key(), rito.rite(), rito.steps(coven),
                    quem == null ? null : quem.getUUID(), coven));
        }
        level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
        this.setChanged();
    }

    /** O {@code updateEntity}: um passo por batida, e os de sustento à parte. */
    public static void tick(Level level, BlockPos pos, BlockState state, CircleHeartBlockEntity coração) {
        if (!(level instanceof ServerLevel server)) return;
        coração.ticks++;

        // os que se sustentam: correm sempre o mesmo passo
        for (int i = coração.upkeep.size() - 1; i >= 0; i--) {
            ActiveRite rito = coração.upkeep.get(i);
            RiteStep.Result saiu = rito.steps().getFirst().run(server, pos, coração.ticks, rito);
            if (saiu == RiteStep.Result.UPKEEP || saiu == RiteStep.Result.STARTING) continue;
            coração.upkeep.remove(i);
            if (saiu == RiteStep.Result.ABORTED_REFUND) rito.refund(server);
        }

        if (coração.running.isEmpty()) return;
        ActiveRite rito = coração.running.getFirst();
        RiteStep.Result saiu = rito.steps().getFirst().run(server, pos, coração.ticks, rito);

        if (coração.abortNext) {
            coração.abortNext = false;
            saiu = RiteStep.Result.ABORTED_REFUND;
            coração.running.clear();
            rito.refund(server);
            server.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 0.7f);
            return;
        }

        switch (saiu) {
            case STARTING -> {
            }
            case COMPLETED -> {
                rito.steps().removeFirst();
                if (rito.steps().isEmpty()) coração.running.removeFirst();
            }
            case UPKEEP -> {
                coração.running.removeFirst();
                coração.upkeep.add(rito);
            }
            case ABORTED, ABORTED_REFUND -> {
                coração.running.removeFirst();
                if (saiu == RiteStep.Result.ABORTED_REFUND) rito.refund(server);
                server.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 0.7f);
            }
        }
        coração.setChanged();
    }

    // ------------------------------------------------------------------ o que fica em disco

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.store("running", ActiveRite.Saved.CODEC.listOf(),
                this.running.stream().map(ActiveRite::save).toList());
        output.store("upkeep", ActiveRite.Saved.CODEC.listOf(),
                this.upkeep.stream().map(ActiveRite::save).toList());
        output.putLong("ticks", this.ticks);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.running.clear();
        this.upkeep.clear();
        read(input, "running", this.running);
        read(input, "upkeep", this.upkeep);
        this.ticks = input.getLongOr("ticks", 0L);
        this.abortNext = false;
    }

    /** Lê uma das duas filas; o rito cujo nome já não exista fica de fora. */
    private static void read(net.minecraft.world.level.storage.ValueInput input, String nome,
                             List<ActiveRite> onde) {
        for (ActiveRite.Saved guardado : input.read(nome, ActiveRite.Saved.CODEC.listOf())
                .orElseGet(List::of)) {
            ActiveRite rito = ActiveRite.load(guardado);
            if (rito != null) onde.add(rito);
        }
    }
}
