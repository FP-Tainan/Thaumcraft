package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * O poder do <b>zumbi</b>: a {@code CreaturePowerZombie} do Witchery.
 *
 * <p><b>Resistência II e Força I</b>, trinta segundos. É o poder mais sem graça da lista, e o mais fácil de
 * manter cheio — zumbis há todas as noites.
 */
public class ZombiePower extends CreaturePower {
    /** Quanto tempo as duas duram. */
    public static final int DURA = 600;

    public ZombiePower(int id) {
        super(id, EntityTypes.ZOMBIE);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        quem.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURA, 1));
        quem.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURA, 0));
    }
}
