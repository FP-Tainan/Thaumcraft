package net.thaumcraft.occulta.love;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.EnumSet;

/**
 * <b>Cortejar aquele jogador</b>: o {@code EntityAIMateWithPlayer} do Witchery.
 *
 * <p>É a tarefa que a {@linkplain net.thaumcraft.occulta.divine.FallInLovePrediction profecia do amor} enfia
 * num aldeão. Ele anda atrás do jogador, olha para ele, solta corações de vez em quando e, quando chega a
 * menos de um bloco e meio, <b>faz um filho</b> — um aldeão bebê que aparece ali mesmo.
 *
 * <p>O original é descarado quanto a isto e não vale esconder: a profecia do amor faz nascer um aldeão para
 * o jogador e o aldeão trata do resto sozinho. O jogador não é consultado, não há cortejo nenhum, e o que
 * sai dali é um bebê aldeão que vai chamá-lo de pai. É uma piada de 2014 e está portada como estava.
 *
 * <h2>O que mudou</h2>
 *
 * <p>O aldeão de hoje pensa por <b>cérebro</b> e não por tarefas, e o cérebro dele quer levá-lo para a cama,
 * para o posto de trabalho e para o sino. Esta tarefa entra na lista de tarefas, que ainda roda, e <b>ganha
 * sempre que o cérebro não tiver para onde ir</b> — de modo que o aldeão apaixonado às vezes para no meio do
 * caminho para ir dormir. O original não tinha esse problema porque o aldeão dele não tinha cérebro.
 *
 * <p>E o {@code setMating} do original não existe mais: ele só fazia sair corações, que aqui saem pelo
 * recado de bicho número doze, que é o mesmo que o original mandava de vez em quando.
 */
public class MateWithPlayerGoal extends Goal {
    /** Quanto tempo ele persegue antes de desistir. */
    public static final int PACIÊNCIA = 1000;

    /** A que distância, ao quadrado, o cortejo acaba. */
    public static final double PERTO = 2.25;

    /** De quantas em quantas batidas, em média, ele solta corações. */
    public static final int CORAÇÕES = 20;

    private final Villager aldeão;
    private ServerPlayer quem;
    private int conta;

    public MateWithPlayerGoal(Villager aldeão) {
        this.aldeão = aldeão;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** Põe a tarefa num aldeão, já apontada a quem. */
    public static void põe(Villager aldeão, ServerPlayer quem) {
        MateWithPlayerGoal tarefa = new MateWithPlayerGoal(aldeão);
        tarefa.quem = quem;
        aldeão.getGoalSelector().addGoal(1, tarefa);
    }

    @Override
    public boolean canUse() {
        return this.quem != null && this.quem.isAlive() && this.aldeão.getAge() == 0;
    }

    @Override
    public void start() {
        this.conta = PACIÊNCIA;
    }

    @Override
    public void stop() {
        this.quem = null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.conta >= 0 && this.quem != null && this.quem.isAlive()
                && this.aldeão.getAge() == 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.quem == null) return;
        if (this.conta > 0) this.conta--;

        this.aldeão.getLookControl().setLookAt(this.quem, 10.0f, 30.0f);
        if (this.aldeão.distanceToSqr(this.quem) > PERTO) {
            this.aldeão.getNavigation().moveTo(this.quem, 0.3);
        } else if (this.conta > 0) {
            this.conta = 0;
            if (this.aldeão.level() instanceof ServerLevel level) nasce(level);
        }

        if (this.aldeão.getRandom().nextInt(CORAÇÕES) == 0) {
            this.aldeão.level().broadcastEntityEvent(this.aldeão, (byte) 12);
        }
    }

    /**
     * E dali sai um bebê.
     *
     * <p>Os números são os do original: o pai fica <b>seis mil batidas</b> sem poder repetir, e o filho nasce
     * com <b>menos vinte e quatro mil</b>, que é um bebê de verdade.
     */
    private void nasce(ServerLevel level) {
        AgeableMob filho = this.aldeão.getBreedOffspring(level, this.aldeão);
        if (filho == null) return;
        this.aldeão.setAge(6000);
        filho.setAge(-24000);
        filho.snapTo(this.aldeão.getX(), this.aldeão.getY(), this.aldeão.getZ(), 0.0f, 0.0f);
        level.addFreshEntity(filho);
        level.broadcastEntityEvent(filho, (byte) 12);
    }
}
