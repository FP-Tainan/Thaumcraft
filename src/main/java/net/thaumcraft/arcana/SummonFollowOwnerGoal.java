package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

/**
 * A invocação anda atrás de quem a chamou: o {@code EntityAISummonFollowOwner} do Ars Magica 2.
 *
 * <p>É a mesma vontade do lobo domado, com uma diferença que importa: aqui o dono não vem de um campo do bicho,
 * vem do <b>apego</b> — e por isso ela serve para qualquer criatura, inclusive um esqueleto, que de dono não
 * sabe nada.
 *
 * <p><b>Ela também teleporta.</b> Se o caminho não deu e o dono está a doze blocos ou mais, a invocação
 * aparece numa casa livre da moldura de cinco por cinco em volta dele. É o original, e é o que impede a
 * invocação de ficar presa atrás de uma parede enquanto quem a chamou segue em frente.
 */
public class SummonFollowOwnerGoal extends Goal {
    private final PathfinderMob invocação;
    private final double passo;
    private final float deLonge;
    private final float dePerto;
    private final PathNavigation caminho;

    private LivingEntity dono;
    private int esperaDoCaminho;
    private boolean nadavaAntes;

    public SummonFollowOwnerGoal(PathfinderMob invocação, double passo, float dePerto, float deLonge) {
        this.invocação = invocação;
        this.passo = passo;
        this.dePerto = dePerto;
        this.deLonge = deLonge;
        this.caminho = invocação.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity quem = this.quemÉODono();
        if (quem == null) return false;
        if (this.invocação.distanceToSqr(quem) < this.dePerto * this.dePerto) return false;
        this.dono = quem;
        return true;
    }

    private LivingEntity quemÉODono() {
        if (!(this.invocação.level() instanceof ServerLevel level)) return null;
        return Summons.donoDe(level, this.invocação);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.caminho.isDone()
                && this.dono != null
                && this.invocação.distanceToSqr(this.dono) > this.deLonge * this.deLonge;
    }

    @Override
    public void start() {
        this.esperaDoCaminho = 0;
        this.nadavaAntes = this.invocação.getNavigation().canFloat();
        this.invocação.getNavigation().setCanFloat(false);
    }

    @Override
    public void stop() {
        this.dono = null;
        this.caminho.stop();
        this.invocação.getNavigation().setCanFloat(this.nadavaAntes);
    }

    @Override
    public void tick() {
        this.invocação.lookAt(this.dono, 10.0f, this.invocação.getMaxHeadXRot());
        if (--this.esperaDoCaminho > 0) return;
        this.esperaDoCaminho = 10;

        // o original triplica o passo aqui, e é o que faz a invocação correr para não ficar atrás
        if (this.caminho.moveTo(this.dono, this.passo * 3.0)) return;
        if (this.invocação.isLeashed()) return;
        if (this.invocação.distanceToSqr(this.dono) < 144.0) return;

        this.teleportaParaOLado();
    }

    /** A moldura de cinco por cinco em volta do dono, com o miolo de três por três de fora. */
    private void teleportaParaOLado() {
        Level level = this.invocação.level();
        int x = net.minecraft.util.Mth.floor(this.dono.getX()) - 2;
        int z = net.minecraft.util.Mth.floor(this.dono.getZ()) - 2;
        int y = net.minecraft.util.Mth.floor(this.dono.getBoundingBox().minY);

        for (int l = 0; l <= 4; l++) {
            for (int c = 0; c <= 4; c++) {
                if (l >= 1 && c >= 1 && l <= 3 && c <= 3) continue;
                BlockPos pé = new BlockPos(x + l, y - 1, z + c);
                if (!level.getBlockState(pé).isSolidRender()) continue;
                if (level.getBlockState(pé.above()).isSolidRender()) continue;
                if (level.getBlockState(pé.above(2)).isSolidRender()) continue;
                this.invocação.snapTo(x + l + 0.5, y, z + c + 0.5,
                        this.invocação.getYRot(), this.invocação.getXRot());
                this.caminho.stop();
                return;
            }
        }
    }
}
