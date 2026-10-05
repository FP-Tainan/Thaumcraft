package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.Infusion;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>esqueleto</b>: a {@code CreaturePowerSkeleton} do Witchery.
 *
 * <p>Uma <b>flecha</b>, com a força do tempo que se segurou a Mão — a mesma conta do arco do jogo: o
 * quadrado do tempo mais duas vezes o tempo, tudo a dividir por três. Ao segundo cheio ela sai <b>crítica</b>.
 *
 * <p>É o único poder da lista que recompensa segurar, e o único que dá a quem o tem um arco que nunca gasta
 * flechas.
 */
public class SkeletonPower extends CreaturePower {
    public SkeletonPower(int id) {
        super(id, EntityTypes.SKELETON);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        float força = força(segurou);
        AbstractArrow flecha = ((net.minecraft.world.item.ArrowItem) Items.ARROW).createArrow(
                level, new ItemStack(Items.ARROW), quem, ItemStack.EMPTY);
        flecha.shootFromRotation(quem, quem.getXRot(), quem.getYRot(), 0.0f, força * 3.0f, 1.0f);
        if (força >= 1.0f) flecha.setCritArrow(true);
        level.addFreshEntity(flecha);
        Infusion.toca(level, quem, SoundEvents.ARROW_SHOOT);
    }

    /** A força do arco do jogo, à letra. */
    public static float força(int segurou) {
        float quanto = segurou / 20.0f;
        quanto = (quanto * quanto + quanto * 2.0f) / 3.0f;
        return Math.min(quanto, 1.0f);
    }
}
