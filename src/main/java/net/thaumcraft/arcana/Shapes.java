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
     * <p>Ela olha para onde o mago está olhando, a dois blocos e meio, e pega no primeiro que achar — bicho ou
     * bloco. Tendo um alvo já dado, usa esse.
     */
    public static final SpellPart.Shape TOUCH = SpellParts.shape(new SpellPart.Shape() {
        /** Até onde a mão chega: as dois blocos e meio do original. */
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
        /** O raio de fábrica dela: as três blocos do original. */
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

    /**
     * <b>Projétil</b>: o {@code Projectile}, e a Forma que define o Ars Magica 2 para quem o joga.
     *
     * <p>Ela não procura alvo nenhum: <b>atira</b>. O feitiço inteiro entra numa entidade que voa e que, ao
     * bater, corre as Essências desta etapa e lança dali o que sobra da frase. É a Forma que faz o projétil
     * seguido de Área explodir no sítio da batida.
     *
     * <p>Ela lê cinco modificadores: <b>Velocidade</b> (multiplica), <b>Gravidade</b> (soma — e o valor dela é
     * negativo, que é o que faz cair), <b>Ricochete</b> e <b>Perfuração</b> (somam) e <b>Alvos Não Sólidos</b>.
     * Sem nenhum deles, o projétil sai a um bloco por batida, a direito, e morre no primeiro que pegar.
     *
     * <p>Ela <b>dá sempre por boa</b>: lançar um projétil custa mana mesmo que ele nunca venha a bater em nada,
     * porque o que pegou foi o atirar. É o que o original faz ao devolver {@code SUCCESS} sem olhar para nada.
     */
    public static final SpellPart.Shape PROJECTILE = SpellParts.shape(new SpellPart.Shape() {
        @Override
        public String name() {
            return "projectile";
        }

        @Override
        public float manaMultiplier() {
            return 1.25f;
        }

        @Override
        public SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem,
                                      @Nullable Entity alvo, Vec3 onde) {
            double velocidade = feitiço.mul(SpellModifierKind.SPEED, SpellModifierKind.SPEED.base);
            var voa = new SpellProjectileEntity(level, quem, feitiço, velocidade);
            voa.setGravity(feitiço.add(SpellModifierKind.GRAVITY, SpellModifierKind.GRAVITY.base));
            voa.setBounces((int) feitiço.add(SpellModifierKind.BOUNCE, SpellModifierKind.BOUNCE.base));
            voa.setPierces((int) feitiço.add(SpellModifierKind.PIERCING, 0.0));
            voa.setTargetNonSolid(feitiço.has(SpellModifierKind.TARGET_NONSOLID_BLOCKS));
            level.addFreshEntity(voa);
            return SpellCast.Result.SUCCESS;
        }
    });

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela as Formas a se registrarem. */
    public static void init() {
    }

    // ------------------------------------------------------------------ a mira

    /**
     * O bicho mais perto no caminho de um ponto a outro: a varredura do {@code EntitySpellProjectile}.
     *
     * <p>Ela infla a caixa de cada bicho em <b>três décimos</b>, que é o número do original, e escolhe o que o
     * segmento corta primeiro. Está escrita à mão de propósito: o ajudante do jogo infla pelo raio de escolha do
     * bicho, que num porco é zero, e assim um projétil fino passava ao lado.
     */
    public static @Nullable EntityHitResult nearest(net.minecraft.world.level.Level level, Entity quem,
                                                   Vec3 daqui, Vec3 até,
                                                   java.util.function.Predicate<Entity> serve) {
        /** O quanto o original engorda a caixa de quem pode levar. */
        final double FOLGA = 0.3;

        var roda = new net.minecraft.world.phys.AABB(daqui, até).inflate(1.0);
        Entity achado = null;
        Vec3 onde = null;
        double perto = 0.0;

        for (Entity outro : level.getEntities(quem, roda, serve)) {
            var caixa = outro.getBoundingBox().inflate(FOLGA);
            var corte = caixa.clip(daqui, até);
            if (corte.isEmpty()) continue;
            double distância = daqui.distanceTo(corte.get());
            if (achado == null || distância < perto) {
                achado = outro;
                onde = corte.get();
                perto = distância;
            }
        }
        return achado == null ? null : new EntityHitResult(achado, onde);
    }

    /**
     * O que o mago está olhando, até àquela distância: o {@code getMovingObjectPosition} do original.
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

        EntityHitResult bicho = nearest(level, quem, olho, até,
                e -> !e.isSpectator() && e.isPickable());

        if (bicho != null) return bicho;
        return bloco.getType() == HitResult.Type.MISS ? null : bloco;
    }
}
