package net.thaumcraft.occulta.infusion.beast;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.brew.WitchProjectile;
import net.thaumcraft.occulta.infusion.Infusion;
import org.jetbrains.annotations.Nullable;

/**
 * O poder da <b>aranha</b>: a {@code CreaturePowerSpider} do Witchery.
 *
 * <p>Usado, <b>enche de teia</b> o lugar para onde se está olhando — a mesma cruz de onze blocos que o
 * frasco de teia faz.
 *
 * <p>E, sem se usar, ele dá a quem o tem o <b>andar da aranha</b>:
 *
 * <ul>
 *   <li>com um bloco cheio <b>por cima da cabeça</b>, a queda é <b>travada a quase metade</b>;</li>
 *   <li>encostado a uma parede, ele <b>sobe</b>;</li>
 *   <li>e <b>agachado</b> contra a parede, ele <b>fica parado nela</b>.</li>
 * </ul>
 *
 * <p>É o poder mais útil para quem constrói e o mais barato de manter: aranhas há em qualquer caverna.
 *
 * <p><b>Desvio declarado:</b> o original <b>atira</b> o frasco de teia como projétil e a teia nasce onde ele
 * bate. O porte não tem ainda a entidade que atira frascos de ingrediente, e por isso a teia nasce <b>onde o
 * olhar bate</b>, até dezesseis blocos. O resultado no chão é o mesmo; o que se perde é o arco da coisa no
 * ar.
 */
public class SpiderPower extends CreaturePower {
    /** Até onde o olhar dela chega. */
    public static final double OLHAR = 16.0;

    /** O quanto o teto trava a queda, e o quanto a parede empurra para cima. */
    public static final double TRAVA = 0.6;
    public static final double SOBE = 0.3;

    public SpiderPower(int id, EntityType<?> dequê) {
        super(id, dequê);
    }

    @Override
    public void usa(ServerLevel level, ServerPlayer quem, int segurou, @Nullable HitResult onde) {
        Vec3 olhos = quem.getEyePosition();
        Vec3 rumo = olhos.add(quem.getLookAngle().scale(OLHAR));
        BlockHitResult bateu = level.clip(new ClipContext(olhos, rumo, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, quem));
        if (bateu.getType() == HitResult.Type.MISS) {
            Infusion.falha(level, quem);
            return;
        }
        Infusion.toca(level, quem, SoundEvents.ARROW_SHOOT);
        WitchProjectile.teia(level, bateu.getBlockPos().relative(bateu.getDirection()),
                bateu.getDirection(), false);
    }

    /** O andar da aranha, do lado de cá. */
    @Override
    public void batida(Player quem) {
        BlockPos acima = BlockPos.containing(quem.getX(), quem.getY() + 1.0, quem.getZ());
        if (quem.level().getBlockState(acima).isSolidRender()) {
            quem.setDeltaMovement(quem.getDeltaMovement().multiply(1.0, TRAVA, 1.0));
        }
        if (!quem.horizontalCollision) return;
        Vec3 anda = quem.getDeltaMovement();
        quem.setDeltaMovement(anda.x, quem.isShiftKeyDown() ? 0.0 : SOBE, anda.z);
    }
}
