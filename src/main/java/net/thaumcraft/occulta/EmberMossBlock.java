package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O Musgo de Brasa: o {@code BlockEmberMoss} do Witchery.
 *
 * <p>Cresce no Nether e <b>queima quem lhe pisa</b> — três segundos de fogo, que é o que o original dá. Quem
 * está no criativo ou já é à prova de fogo passa por cima sem sentir.
 */
public class EmberMossBlock extends BushBlock {
    /** Quantos segundos de fogo ele põe em quem passa. */
    public static final float BURN_SECONDS = 3.0f;

    public EmberMossBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        if (level.isClientSide() || !(quem instanceof LivingEntity vivo)) return;
        if (vivo.isOnFire() || vivo.fireImmune()) return;
        if (vivo instanceof Player gente && gente.getAbilities().instabuild) return;
        vivo.igniteForSeconds(BURN_SECONDS);
    }
}
