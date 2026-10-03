package net.thaumcraft.occulta.goblin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * <b>O goblin apanha o que está no chão</b>: o {@code EntityAIPickUpBlocks} do Witchery.
 *
 * <p>Só <b>na corda</b>, e só de <b>mãos vazias</b> — um goblin com uma picareta na mão está a cavar, e não
 * apanha nada. Ele vai até o item e o <b>põe na mão</b>, e fica com ele até alguém lho tirar.
 *
 * <p>É o que faz dele um ajudante e não um bicho: ninguém lhe manda apanhar, ele apanha porque está preso e
 * tem as mãos livres.
 */
public class GoblinPickUpGoal extends Goal {
    /** Até onde ele vê o que está no chão: os vinte e quatro blocos do original. */
    public static final double ALCANCE = 24.0;

    /** E a que distância ele consegue pegar. */
    public static final double À_MÃO = 1.5;

    /** O passo com que ele vai buscar. */
    public static final double PASSO = 0.6;

    private final GoblinEntity goblin;
    private final double alcance;

    public GoblinPickUpGoal(GoblinEntity goblin, double alcance) {
        this.goblin = goblin;
        this.alcance = alcance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.podeTrabalhar() && this.háAlgoAoAlcance();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    /** Na corda, de mãos vazias, e sem estar a adorar. */
    private boolean podeTrabalhar() {
        return this.goblin.isLeashed()
                && !this.goblin.adorando()
                && this.goblin.getMainHandItem().isEmpty();
    }

    @Override
    public void start() {
        this.vaiBuscar();
    }

    @Override
    public void tick() {
        if (!this.goblin.getNavigation().isDone()) {
            this.vaiBuscar();
            return;
        }
        // chegou: o que estiver à mão vai para a mão
        ItemEntity perto = this.primeiro(À_MÃO);
        if (perto == null) return;
        this.goblin.setItemSlot(EquipmentSlot.MAINHAND, perto.getItem().copy());
        this.goblin.setDropChance(EquipmentSlot.MAINHAND, 2.0f);
        perto.discard();
    }

    private void vaiBuscar() {
        ItemEntity qual = this.primeiro(this.alcance);
        if (qual == null) return;
        this.goblin.getNavigation().moveTo(qual, PASSO);
    }

    /** Se há alguma coisa no chão a que ele consiga chegar. */
    private boolean háAlgoAoAlcance() {
        ItemEntity qual = this.primeiro(this.alcance);
        if (qual == null) return false;
        // o que já está à mão não precisa de caminho nenhum
        if (this.goblin.distanceToSqr(qual) <= À_MÃO * À_MÃO) return true;
        return this.goblin.getNavigation().createPath(qual, 1) != null;
    }

    @Nullable
    private ItemEntity primeiro(double quão) {
        AABB volta = this.goblin.getBoundingBox().inflate(quão);
        List<ItemEntity> largados = this.goblin.level().getEntitiesOfClass(ItemEntity.class, volta);
        for (ItemEntity qual : largados) {
            if (qual.isRemoved() || qual.getItem().isEmpty()) continue;
            return qual;
        }
        return null;
    }
}
