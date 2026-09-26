package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Achar a fenda para onde alguém está apontando.
 *
 * <p>Uma fenda solta <b>não tem corpo</b>: o raio do mouse passa direto por ela e acerta o bloco de trás. Quem
 * quiser pegar uma fenda tem que percorrer a linha de visão de um quarto de bloco em quarto de bloco e ver se em
 * alguma dessas posições tem uma.
 *
 * <p>Isso começou dentro da Lâmina de Fenda, que foi o primeiro item a precisar. A lâmina saiu — quem manda não
 * quis espada nem armadura neste ramo —, mas a conta ficou: é dela que os três focos e o efeito deles dependem.
 */
public final class RiftAim {
    /** Até onde se procura. */
    public static final double RANGE = 16.0;

    private RiftAim() {
    }

    /** A fenda na linha de visão, ou nada. */
    public static @Nullable BlockPos riftAimedAt(Level level, Player quem) {
        Vec3 olho = quem.getEyePosition();
        Vec3 até = olho.add(quem.getLookAngle().scale(RANGE));
        BlockHitResult bateu = level.clip(new ClipContext(olho, até,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, quem));

        // a fenda não tem corpo, então o raio passa por ela: se percorre a linha procurando uma
        Vec3 fim = bateu.getType() == HitResult.Type.MISS ? até : bateu.getLocation();
        double quanto = olho.distanceTo(fim);
        for (double passo = 0.0; passo <= quanto; passo += 0.25) {
            BlockPos onde = BlockPos.containing(olho.add(quem.getLookAngle().scale(passo)));
            if (level.getBlockState(onde).is(ShatteredBlocks.RIFT)) return onde;
        }
        return null;
    }
}
