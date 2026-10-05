package net.thaumcraft.occulta.goblin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * A <b>adoração</b> do goblin: a {@code EntityAIWorship} do Witchery.
 *
 * <p>Mandado adorar por uma {@link net.thaumcraft.occulta.StatueOfWorshipBlockEntity Estátua de Adoração},
 * o goblin larga tudo, anda devagar até ela e <b>fica lá</b> meio minuto — e, passado esse meio minuto,
 * ainda fica, com dois terços de chance por batida, até lhe dar na gana sair.
 *
 * <p>E ele nem sempre obedece: a estátua manda, e ele atende <b>duas vezes em três</b>. Com isso, uma
 * estátua com vinte goblins à volta nunca tem vinte adoradores — tem catorze ou quinze, e é por isso que
 * os quinze do terceiro degrau custam o que custam.
 */
public class GoblinWorshipGoal extends Goal {
    /** Quanto tempo ele fica, antes de começar a poder sair: os trinta segundos do original. */
    public static final int FICA = 600;

    /** E o bocado a mais, sorteado, que faz com que eles não saiam todos ao mesmo tempo. */
    public static final int MAIS = 10;

    /** A que passo ele vai até lá. */
    public static final double PASSO = 0.4;

    /** De quantas em quantas vezes ele atende o chamado, e de quantas em quantas ele sai depois. */
    public static final int DE_TRÊS = 3;

    private final GoblinEntity goblin;
    private final int quanto;
    private int batidas;
    private boolean chamado;
    private @Nullable BlockPos onde;

    public GoblinWorshipGoal(GoblinEntity goblin) {
        this.goblin = goblin;
        this.quanto = FICA + goblin.getRandom().nextInt(MAIS);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    /** O chamado da estátua, que ele atende duas vezes em três. */
    public void chama(BlockPos daqui) {
        if (this.goblin.getRandom().nextInt(DE_TRÊS) == 0) return;
        this.chamado = true;
        this.onde = daqui;
    }

    @Override
    public boolean canUse() {
        return this.chamado || this.goblin.adorando();
    }

    @Override
    public void start() {
        this.batidas = 0;
        this.chamado = false;
        this.goblin.adorando(true);
        if (this.onde != null) {
            this.goblin.getNavigation().moveTo(this.onde.getX() + 0.5, this.onde.getY(),
                    this.onde.getZ() + 0.5, PASSO);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.batidas <= this.quanto || this.goblin.getRandom().nextInt(DE_TRÊS) == 0;
    }

    @Override
    public void stop() {
        this.goblin.adorando(false);
    }

    @Override
    public void tick() {
        this.batidas++;
    }
}
