package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Um rito a correr: o {@code ActivatedRitual} do Witchery.
 *
 * <p>Guarda a fila de passos que falta, quem o começou, quantas bruxas o acompanham e <b>o que já se ofereceu</b>
 * — porque um rito que desiste pedindo devolução põe tudo isso de volta no chão.
 *
 * <p>Guarda também o <b>lugar</b> que o rito escolheu, quando ele escolhe um: há ritos que acontecem longe do
 * círculo, e é esse ponto que os passos seguintes usam.
 */
public class ActiveRite {
    private final Rite rite;
    private final List<RiteStep> steps;
    private final List<Offered> offered = new ArrayList<>();
    private final int coven;

    @Nullable
    private UUID starter;

    @Nullable
    private BlockPos target;

    public ActiveRite(Rite rite, List<RiteStep> steps, @Nullable UUID starter, int coven) {
        this.rite = rite;
        this.steps = new ArrayList<>(steps);
        this.starter = starter;
        this.coven = coven;
    }

    /** O que se ofereceu, e de onde veio — para poder voltar. */
    public record Offered(ItemStack stack, BlockPos where) {
    }

    public Rite rite() {
        return this.rite;
    }

    public List<RiteStep> steps() {
        return this.steps;
    }

    public int coven() {
        return this.coven;
    }

    public List<Offered> offered() {
        return this.offered;
    }

    public void offer(ItemStack stack, BlockPos onde) {
        this.offered.add(new Offered(stack.copy(), onde));
    }

    @Nullable
    public UUID starter() {
        return this.starter;
    }

    @Nullable
    public Player starter(ServerLevel level) {
        return this.starter == null ? null : level.getPlayerByUUID(this.starter);
    }

    @Nullable
    public BlockPos target() {
        return this.target;
    }

    public void setTarget(@Nullable BlockPos onde) {
        this.target = onde;
    }

    /** O que se ofereceu volta para o chão, onde estava. */
    public void refund(ServerLevel level) {
        for (Offered coisa : this.offered) {
            net.minecraft.world.level.block.Block.popResource(level, coisa.where(), coisa.stack());
        }
        this.offered.clear();
    }
}
