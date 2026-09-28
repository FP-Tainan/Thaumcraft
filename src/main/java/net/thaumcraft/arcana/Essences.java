package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * As Essências: as {@code am2.spell.components} do Ars Magica 2.
 *
 * <p>São elas que fazem alguma coisa acontecer. Os números são os do original, e a conta do desgaste também:
 * <b>38 por cento do que o feitiço custa de mana</b>, que é o {@code getBurnoutFromMana}.
 */
public final class Essences {
    /** O desgaste sai do custo: o {@code getBurnoutFromMana} do original. */
    public static final float BURNOUT_RATIO = 0.38f;

    private Essences() {
    }

    public static float burnoutFromMana(float mana) {
        return mana * BURNOUT_RATIO;
    }

    /**
     * <b>Dano de Fogo</b>: o {@code FireDamage}.
     *
     * <p>Seis de dano de base, e pega fogo em quem leva. Coisa do Nether não sente — o que faz sentido e é o
     * que o original faz.
     */
    public static final SpellPart.Essence FIRE_DAMAGE = SpellParts.essence(new SpellPart.Essence() {
        /** O dano de fábrica: os seis do original. */
        public static final float BASE = 6.0f;
        public static final float MANA = 120.0f;

        @Override
        public String name() {
            return "fire_damage";
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return 20.0f;
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof LivingEntity vivo)) return false;
            if (vivo.fireImmune()) return true;

            float dano = (float) feitiço.add(SpellModifierKind.DAMAGE, BASE);
            vivo.igniteForSeconds(2.0f);
            boolean pegou = vivo.hurtServer(level, level.damageSources().indirectMagic(quem, quem), dano);
            if (pegou) {
                level.sendParticles(ParticleTypes.FLAME, vivo.getX(), vivo.getY() + 1.0, vivo.getZ(),
                        12, 0.3, 0.4, 0.3, 0.02);
            }
            return pegou;
        }
    });

    /**
     * <b>Cura</b>: o {@code Heal}.
     *
     * <p>Dois de vida — e <b>dez de dano sagrado</b> em morto-vivo, que ainda por cima pega fogo. É a mesma
     * essência: o que muda é quem a leva.
     */
    public static final SpellPart.Essence HEAL = SpellParts.essence(new SpellPart.Essence() {
        public static final int BASE = 2;
        public static final int UNDEAD = 10;
        public static final float MANA = 225.0f;

        @Override
        public String name() {
            return "heal";
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(MANA);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof LivingEntity vivo)) return false;

            if (vivo.isInvertedHealAndHarm()) {
                int quanto = (int) feitiço.mul(SpellModifierKind.HEALING, UNDEAD);
                vivo.igniteForSeconds(2.0f);
                return vivo.hurtServer(level, level.damageSources().indirectMagic(quem, quem), quanto);
            }

            int quanto = (int) feitiço.mul(SpellModifierKind.HEALING, BASE);
            if (vivo.getHealth() >= vivo.getMaxHealth()) return false;
            vivo.heal(quanto);
            level.sendParticles(ParticleTypes.HEART, vivo.getX(), vivo.getY() + 1.2, vivo.getZ(),
                    4, 0.3, 0.3, 0.3, 0.0);
            return true;
        }
    });

    /**
     * <b>Luz</b>: o {@code Light}.
     *
     * <p>Em quem leva, trinta segundos de enxergar no escuro. Num bloco, uma tocha em cima dele — que é o que
     * a Luz do original acaba por fazer a quem a atira ao chão.
     */
    public static final SpellPart.Essence LIGHT = SpellParts.essence(new SpellPart.Essence() {
        /** Os seiscentos tiques do original. */
        public static final int BASE_TICKS = 600;
        public static final float MANA = 50.0f;

        @Override
        public String name() {
            return "light";
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(MANA);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof LivingEntity vivo)) return false;
            int tempo = (int) feitiço.mul(SpellModifierKind.DURATION, BASE_TICKS);
            vivo.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, tempo, 0));
            return true;
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            BlockPos casa = onde.relative(face);
            if (!level.getBlockState(casa).canBeReplaced()) return false;
            var tocha = Blocks.TORCH.defaultBlockState();
            if (!tocha.canSurvive(level, casa)) return false;
            level.setBlockAndUpdate(casa, tocha);
            return true;
        }
    });

    /**
     * <b>Escavar</b>: o {@code Dig}.
     *
     * <p>Quebra o bloco em que bate, se ele for quebrável. A força com que cava é o que decide se ele leva
     * pedra, e é aí que o modificador de mineração entra.
     */
    public static final SpellPart.Essence DIG = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 85.0f;

        @Override
        public String name() {
            return "dig";
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(MANA);
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            var feitio = level.getBlockState(onde);
            if (feitio.isAir()) return false;
            float dureza = feitio.getDestroySpeed(level, onde);
            if (dureza < 0.0f) return false;

            double força = feitiço.mul(SpellModifierKind.MINING_POWER, SpellModifierKind.MINING_POWER.base);
            if (dureza > força * 3.0) return false;

            level.destroyBlock(onde, true, quem);
            level.playSound(null, onde, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);
            return true;
        }
    });

    /**
     * <b>Toque Gélido</b>: o {@code FrostDamage}.
     *
     * <p>Cinco de dano e um bom tempo de lentidão. O original chama-lhe Frost Damage, e é a irmã do fogo.
     */
    public static final SpellPart.Essence FROST_DAMAGE = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 5.0f;
        public static final int SLOW_TICKS = 100;
        public static final float MANA = 110.0f;

        @Override
        public String name() {
            return "frost_damage";
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(MANA);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof LivingEntity vivo)) return false;
            float dano = (float) feitiço.add(SpellModifierKind.DAMAGE, BASE);
            boolean pegou = vivo.hurtServer(level, level.damageSources().freeze(), dano);
            if (pegou) {
                int tempo = (int) feitiço.mul(SpellModifierKind.DURATION, SLOW_TICKS);
                vivo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, tempo, 1));
                level.sendParticles(ParticleTypes.SNOWFLAKE, vivo.getX(), vivo.getY() + 1.0, vivo.getZ(),
                        12, 0.3, 0.4, 0.3, 0.02);
            }
            return pegou;
        }
    });

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela as Essências a se registrarem. */
    public static void init() {
    }

    /** As que existem, para quem precisar de contar. */
    public static Set<SpellPart.Essence> all() {
        return Set.copyOf(SpellParts.essences());
    }
}
