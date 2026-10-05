package net.thaumcraft.occulta.goblin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * <b>O goblin larga o que carrega num baú</b>: o {@code EntityAIDropOffBlocks} do Witchery.
 *
 * <p>Só <b>na corda</b>, só com alguma coisa na mão, e só se o que ele tem na mão <b>não for ferramenta</b> —
 * um goblin não guarda a picareta, guarda o que cavou com ela.
 *
 * <p>Ele procura um <b>baú grande</b>, de vinte e sete casas para cima, que é como o original os distingue de
 * fornalhas e funis: o que ele quer é onde caiba o dia inteiro de trabalho.
 *
 * <p>Fechado o ciclo — apanhar, cavar, largar —, um goblin na corda ao pé de um baú trabalha sozinho até
 * alguém o desprender.
 *
 * <p><b>Tradução declarada:</b> o original varre a <b>lista inteira de blocos com alma do mundo</b> para achar
 * o baú, e apanha um {@code Throwable} em volta disso porque a lista muda enquanto ele a lê. Aqui se varre o
 * que está <b>à volta dele</b>, casa a casa, no mesmo alcance — é a mesma resposta sem ler o mundo inteiro, e
 * sem apanhar erros que não deviam acontecer.
 */
public class GoblinDropOffGoal extends Goal {
    /** Até onde ele procura o baú: os vinte e quatro blocos do original. */
    public static final double ALCANCE = 24.0;

    /** De quantas casas para cima um baú serve: as vinte e sete do original. */
    public static final int BAÚ_GRANDE = 27;

    /** A que distância ele consegue largar. */
    public static final double À_MÃO = 2.5;

    /** O passo com que ele vai lá. */
    public static final double PASSO = 0.6;

    /** E de quantas em quantas batidas ele se lembra de procurar: uma em sessenta, como no original. */
    public static final int LEMBRA_SE = 60;

    private final GoblinEntity goblin;
    private final int alcance;

    @Nullable
    private BlockPos baú;

    public GoblinDropOffGoal(GoblinEntity goblin, double alcance) {
        this.goblin = goblin;
        this.alcance = (int) alcance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.podeLargar()) return false;
        if (this.baú != null && this.ainda(this.baú)) return true;

        this.baú = null;
        if (this.goblin.getRandom().nextInt(LEMBRA_SE) != 0) return false;
        this.baú = this.procura();
        return this.baú != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.podeLargar() && this.baú != null;
    }

    @Override
    public void stop() {
        this.baú = null;
    }

    /** Na corda, com alguma coisa na mão, e essa coisa não é ferramenta. */
    private boolean podeLargar() {
        if (!this.goblin.isLeashed() || this.goblin.adorando()) return false;
        ItemStack naMão = this.goblin.getMainHandItem();
        if (naMão.isEmpty()) return false;
        return !naMão.is(net.minecraft.tags.ItemTags.PICKAXES)
                && !naMão.is(net.minecraft.tags.ItemTags.SHOVELS)
                && !naMão.is(net.minecraft.tags.ItemTags.AXES);
    }

    @Override
    public void tick() {
        if (this.baú == null) return;

        if (this.goblin.getNavigation().isDone()) {
            this.goblin.getNavigation().moveTo(this.baú.getX() + 0.5, this.baú.getY(),
                    this.baú.getZ() + 0.5, PASSO);
        }
        if (this.goblin.distanceToSqr(this.baú.getX() + 0.5, this.baú.getY() + 0.5,
                this.baú.getZ() + 0.5) > À_MÃO * À_MÃO) {
            return;
        }

        if (!(this.goblin.level().getBlockEntity(this.baú) instanceof Container dentro)) {
            this.baú = null;
            return;
        }
        ItemStack naMão = this.goblin.getMainHandItem();
        if (this.guarda(naMão, dentro)) {
            this.goblin.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        this.baú = null;
    }

    /** Se aquele lugar ainda é um baú que lhe serve. */
    private boolean ainda(BlockPos onde) {
        return this.goblin.level().getBlockEntity(onde) instanceof Container dentro
                && dentro.getContainerSize() >= BAÚ_GRANDE;
    }

    /** O baú grande mais perto a que ele consiga chegar. */
    @Nullable
    private BlockPos procura() {
        BlockPos dele = this.goblin.blockPosition();
        BlockPos melhor = null;
        double maisPerto = Double.MAX_VALUE;

        for (BlockPos onde : BlockPos.betweenClosed(dele.offset(-this.alcance, -this.alcance, -this.alcance),
                dele.offset(this.alcance, this.alcance, this.alcance))) {
            if (!(this.goblin.level().getBlockEntity(onde) instanceof Container dentro)) continue;
            if (dentro.getContainerSize() < BAÚ_GRANDE) continue;

            double longe = this.goblin.distanceToSqr(onde.getX() + 0.5, onde.getY() + 0.5,
                    onde.getZ() + 0.5);
            if (longe >= maisPerto) continue;
            // o que já está à mão não precisa de caminho nenhum
            if (longe > À_MÃO * À_MÃO && this.goblin.getNavigation().createPath(onde, 1) == null) continue;
            maisPerto = longe;
            melhor = onde.immutable();
        }
        return melhor;
    }

    /** Põe o que ele traz onde couber, juntando ao que já está ali primeiro. */
    private boolean guarda(ItemStack oQueTraz, Container dentro) {
        for (int casa = 0; casa < dentro.getContainerSize(); casa++) {
            ItemStack lá = dentro.getItem(casa);
            if (lá.isEmpty() || !ItemStack.isSameItemSameComponents(lá, oQueTraz)) continue;
            int cabe = Math.min(lá.getMaxStackSize() - lá.getCount(), oQueTraz.getCount());
            if (cabe <= 0) continue;
            lá.grow(cabe);
            oQueTraz.shrink(cabe);
            dentro.setChanged();
            if (oQueTraz.isEmpty()) return true;
        }
        for (int casa = 0; casa < dentro.getContainerSize(); casa++) {
            if (!dentro.getItem(casa).isEmpty()) continue;
            dentro.setItem(casa, oQueTraz.copy());
            oQueTraz.setCount(0);
            dentro.setChanged();
            return true;
        }
        return false;
    }
}
