package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    /** Que rito é, pelo nome com que ele está na lista: é por ele que um rito guardado se remonta. */
    private final String key;
    private final List<RiteStep> steps;
    private final List<Offered> offered = new ArrayList<>();
    private final int coven;

    @Nullable
    private UUID starter;

    @Nullable
    private BlockPos target;

    /**
     * Em que fase o passo de agora vai.
     *
     * <p>Há ritos que correm o mesmo passo muitas vezes e precisam de saber quantas já correram — a tempestade,
     * a terra que sobe, a terra que se parte. Isso <b>mora aqui</b>, e não dentro do passo, para que um mundo
     * desligado no meio de um rito volte no ponto em que estava.
     */
    private int stage;

    public ActiveRite(String key, Rite rite, List<RiteStep> steps, @Nullable UUID starter, int coven) {
        this.key = key;
        this.rite = rite;
        this.steps = new ArrayList<>(steps);
        this.starter = starter;
        this.coven = coven;
    }

    public String key() {
        return this.key;
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

    public int stage() {
        return this.stage;
    }

    /** Passa à fase seguinte e devolve a nova. */
    public int advance() {
        return ++this.stage;
    }

    /** O que se ofereceu volta para o chão, onde estava. */
    public void refund(ServerLevel level) {
        for (Offered coisa : this.offered) {
            net.minecraft.world.level.block.Block.popResource(level, coisa.where(), coisa.stack());
        }
        this.offered.clear();
    }

    // ------------------------------------------------------------------ escrever-se e voltar

    /**
     * Um rito a correr, em disco.
     *
     * <p>Ele <b>não guarda os passos</b>: guarda o nome do rito e <b>quantos passos faltam</b>. Ao voltar, a
     * fila de passos é remontada da lista de ritos e cortada no ponto em que estava. É o que permite guardar um
     * rito sem pedir a cada passo que saiba escrever-se — e o que se perde com isso são contas que um passo
     * tenha só para si, que nenhum dos ritos deste porte tem.
     */
    public record Saved(String key, int remaining, java.util.Optional<UUID> starter, int coven,
                        List<Offered> offered, java.util.Optional<BlockPos> target, int stage) {
        public static final com.mojang.serialization.Codec<Offered> OFFERED_CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
                        ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(Offered::stack),
                        BlockPos.CODEC.fieldOf("where").forGetter(Offered::where))
                        .apply(i, Offered::new));

        public static final com.mojang.serialization.Codec<Saved> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
                        com.mojang.serialization.Codec.STRING.fieldOf("key").forGetter(Saved::key),
                        com.mojang.serialization.Codec.INT.fieldOf("remaining").forGetter(Saved::remaining),
                        net.minecraft.core.UUIDUtil.CODEC.optionalFieldOf("starter").forGetter(Saved::starter),
                        com.mojang.serialization.Codec.INT.optionalFieldOf("coven", 0).forGetter(Saved::coven),
                        OFFERED_CODEC.listOf().optionalFieldOf("offered", List.of()).forGetter(Saved::offered),
                        BlockPos.CODEC.optionalFieldOf("target").forGetter(Saved::target),
                        com.mojang.serialization.Codec.INT.optionalFieldOf("stage", 0).forGetter(Saved::stage))
                        .apply(i, Saved::new));
    }

    /** O que este rito é, para guardar. */
    public Saved save() {
        return new Saved(this.key, this.steps.size(), java.util.Optional.ofNullable(this.starter), this.coven,
                List.copyOf(this.offered), java.util.Optional.ofNullable(this.target), this.stage);
    }

    /** E o contrário: o rito de volta, ou nada se já não houver rito com aquele nome. */
    @Nullable
    public static ActiveRite load(Saved guardado) {
        var entrada = RiteRegistry.all().stream()
                .filter(r -> r.key().equals(guardado.key()))
                .findFirst().orElse(null);
        if (entrada == null) return null;

        List<RiteStep> todos = entrada.steps(guardado.coven());
        if (guardado.remaining() <= 0 || guardado.remaining() > todos.size()) return null;
        List<RiteStep> faltam = new ArrayList<>(todos.subList(todos.size() - guardado.remaining(), todos.size()));

        ActiveRite rito = new ActiveRite(guardado.key(), entrada.rite(), faltam,
                guardado.starter().orElse(null), guardado.coven());
        rito.offered.addAll(guardado.offered());
        rito.target = guardado.target().orElse(null);
        rito.stage = guardado.stage();
        return rito;
    }
}
