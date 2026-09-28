package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * As Formas: as {@code am2.spell.shapes} do Ars Magica 2.
 *
 * <p>Cada uma acha o que a etapa atinge e, tendo achado, <b>passa a frase adiante</b>: tira a etapa da frente e
 * lança o resto. É por isso que um Projétil seguido de um Toque funciona — o projétil que bate acorda o toque.
 */
public final class Shapes {
    private Shapes() {
    }

    /**
     * <b>Autoconjuração</b>: o {@code Self}.
     *
     * <p>A mais simples e a mais barata — metade do custo. O alvo é quem lançou, e mais ninguém.
     */
    public static final SpellPart.Shape SELF = SpellParts.shape(new SpellPart.Shape() {
        @Override
        public String name() {
            return "self";
        }

        @Override
        public float manaMultiplier() {
            return 0.5f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, quem);
            if (!saiu.ok()) return saiu;
            return SpellCast.cast(level, feitiço.pop(), quem, alvo, onde);
        }
    });

    /**
     * <b>Toque</b>: o {@code Touch}.
     *
     * <p>Ela olha para onde o mago está a olhar, a duas casas e meia, e pega no primeiro que achar — bicho ou
     * bloco. Tendo um alvo já dado, usa esse.
     */
    public static final SpellPart.Shape TOUCH = SpellParts.shape(new SpellPart.Shape() {
        /** Até onde a mão chega: as duas casas e meia do original. */
        public static final double REACH = 2.5;

        @Override
        public String name() {
            return "touch";
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            if (alvo != null) {
                SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, alvo);
                if (!saiu.ok()) return saiu;
                return SpellCast.cast(level, feitiço.pop(), quem, alvo, onde);
            }

            HitResult bateu = look(level, quem, REACH, feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));
            if (bateu == null) return SpellCast.Result.EFFECT_FAILED;

            if (bateu instanceof EntityHitResult nele) {
                SpellCast.Result saiu = SpellCast.onEntity(level, feitiço, quem, nele.getEntity());
                if (!saiu.ok()) return saiu;
                return SpellCast.cast(level, feitiço.pop(), quem, nele.getEntity(), bateu.getLocation());
            }

            BlockHitResult nisso = (BlockHitResult) bateu;
            SpellCast.Result saiu = SpellCast.onBlock(level, feitiço, quem, nisso.getBlockPos(),
                    nisso.getDirection(), nisso.getLocation());
            if (!saiu.ok()) return saiu;
            return SpellCast.cast(level, feitiço.pop(), quem, null, bateu.getLocation());
        }
    });

    /**
     * <b>Área</b>: o {@code AoE}.
     *
     * <p>Ela não procura nada: pega em <b>tudo o que estiver em roda</b> do ponto, a um raio que os
     * modificadores mexem. É a Forma que se usa quando o que importa é o lugar e não o alvo.
     */
    public static final SpellPart.Shape AOE = SpellParts.shape(new SpellPart.Shape() {
        /** O raio de fábrica dela: as três casas do original. */
        public static final double BASE_RADIUS = 3.0;

        @Override
        public String name() {
            return "aoe";
        }

        @Override
        public float manaMultiplier() {
            return 1.5f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            double raio = feitiço.mul(SpellModifierKind.RADIUS, BASE_RADIUS);
            var roda = new net.minecraft.world.phys.AABB(onde, onde).inflate(raio);

            boolean pegou = false;
            for (Entity quem2 : level.getEntities(quem, roda, e -> e.distanceToSqr(onde) <= raio * raio)) {
                if (SpellCast.onEntity(level, feitiço, quem, quem2).ok()) pegou = true;
            }

            BlockPos casa = BlockPos.containing(onde);
            if (SpellCast.onBlock(level, feitiço, quem, casa,
                    net.minecraft.core.Direction.UP, onde).ok()) {
                pegou = true;
            }
            if (!pegou) return SpellCast.Result.EFFECT_FAILED;
            return SpellCast.cast(level, feitiço.pop(), quem, alvo, onde);
        }
    });

    // ------------------------------------------------------------------ a mira

    /**
     * O que o mago está a olhar, até àquela distância: o {@code getMovingObjectPosition} do original.
     *
     * <p>Ele olha bicho <b>e</b> bloco e devolve o que estiver mais perto, que é o que faz um feitiço de toque
     * pegar no bicho à frente da parede e não na parede atrás dele.
     */
    public static @Nullable HitResult look(ServerLevel level, LivingEntity quem, double alcance,
                                           boolean líquidos) {
        Vec3 olho = quem.getEyePosition();
        Vec3 fim = olho.add(quem.getLookAngle().scale(alcance));

        BlockHitResult bloco = level.clip(new ClipContext(olho, fim,
                ClipContext.Block.OUTLINE,
                líquidos ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, quem));
        Vec3 até = bloco.getType() == HitResult.Type.MISS ? fim : bloco.getLocation();

        var roda = quem.getBoundingBox().expandTowards(quem.getLookAngle().scale(alcance)).inflate(1.0);
        EntityHitResult bicho = net.minecraft.world.entity.projectile.ProjectileUtil
                .getEntityHitResult(quem, olho, até, roda,
                        e -> !e.isSpectator() && e.isPickable(), olho.distanceToSqr(até));

        if (bicho != null) return bicho;
        return bloco.getType() == HitResult.Type.MISS ? null : bloco;
    }
}
