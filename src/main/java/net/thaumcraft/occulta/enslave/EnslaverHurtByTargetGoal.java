package net.thaumcraft.occulta.enslave;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.OccultaEffects;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * A vontade de brigar as brigas do dono: o {@code EntityAIEnslaverHurtByTarget} do Witchery.
 *
 * <p>É o {@code HurtByTargetGoal} do jogo <b>com os olhos virados</b>: em vez de olhar quem bateu <i>nele</i>,
 * olha quem bateu <b>em quem o escravizou</b> — e põe-se em cima dessa pessoa.
 *
 * <p>O miolo dela é o <b>relógio de vingança</b>. Todo bicho e toda gente guarda a batida em que foi ferido pela
 * última vez; a vontade guarda o número que viu, e só se acende quando o número <b>muda</b>. Sem isso, um
 * escravo cuja gente levasse uma pancada ficaria a reacender-se para sempre contra o mesmo agressor, mesmo
 * depois de este já ter fugido ou morrido.
 *
 * <p>É também o que faz um exército de mortos levantados pelo Cozimento da Ressurreição andar junto: ninguém
 * lhes diz quem é o inimigo — eles descobrem, porque viram o dono levar uma pancada.
 */
public class EnslaverHurtByTargetGoal extends TargetGoal {
    private final PathfinderMob escravo;

    @Nullable
    private LivingEntity quemBateuNoDono;

    /** O número que esta vontade já viu: só se acende quando ele muda. */
    private int relógioDaVingança;

    public EnslaverHurtByTargetGoal(PathfinderMob escravo) {
        super(escravo, false);
        this.escravo = escravo;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.escravo.hasEffect(OccultaEffects.ENSLAVED)) return false;

        Player dono = this.escravo.level() instanceof net.minecraft.server.level.ServerLevel level
                ? Enslavement.donoDe(level, this.escravo)
                : null;
        if (dono == null) return false;

        this.quemBateuNoDono = dono.getLastHurtByMob();
        int agora = dono.getLastHurtByMobTimestamp();
        if (agora == this.relógioDaVingança) return false;
        if (this.quemBateuNoDono == null) return false;

        // e nunca o próprio dono, por mais voltas que a pancada dê
        if (this.quemBateuNoDono == dono) return false;

        return this.canAttack(this.quemBateuNoDono,
                net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.escravo.setTarget(this.quemBateuNoDono);
        this.targetMob = this.quemBateuNoDono;

        if (this.escravo.level() instanceof net.minecraft.server.level.ServerLevel level) {
            Player dono = Enslavement.donoDe(level, this.escravo);
            if (dono != null) this.relógioDaVingança = dono.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
