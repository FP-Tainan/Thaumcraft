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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
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

    // ------------------------------------------------------------------ o dano

    /**
     * Fere alguém com um feitiço: o {@code attackTargetSpecial} do original, com o {@code modifyDamage} junto.
     *
     * <p><b>Todo dano de feitiço passa por aqui</b>, e há duas razões para isso.
     *
     * <p>A primeira é o <b>fator do nível</b>, que é do original e muda tudo: um arcanista de nível zero faz
     * <b>metade</b> do dano escrito, e só ao chegar ao vinte é que faz o que está na folha. Daí para cima
     * cresce de novo, até <b>o dobro</b> no 99. Quer dizer que a mesma frase de feitiço fere de maneira
     * diferente em duas mãos, e é isso que faz subir de nível valer a pena para quem já tem todas as peças.
     *
     * <p>A segunda é o <b>Desmembramento</b>, que só faz sentido no instante em que alguém morre.
     *
     * @param base o dano escrito da essência, antes de os modificadores mexerem nele
     */
    public static boolean fere(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo,
                               net.minecraft.world.damagesource.DamageSource fonte, float base) {
        if (!(alvo instanceof LivingEntity vivo)) return false;
        // a Fúria soma DEPOIS do fator do nível, que é a ordem do original: ele multiplica no lugar da
        // chamada e só dentro do attackTargetSpecial é que soma os quatro
        float dano = (float) feitiço.add(level, SpellModifierKind.DAMAGE, base) * fatorDoNível(quem)
                + ArcanaEffects.extraDamage(quem);
        boolean pegou = vivo.hurtServer(level, fonte, dano);
        if (pegou) desmembra(level, feitiço, quem, vivo);
        return pegou;
    }

    /**
     * O quanto o nível de quem lança multiplica o dano: o {@code modifyDamage} do original.
     *
     * <p>Até o vinte ele vai de <b>meio</b> a <b>um</b>; do vinte ao 99 vai de <b>um</b> a <b>dois</b>. Quem
     * não é gente — um bicho que lança — conta como nível zero, e é o que o original faz ao ler a mana de quem
     * não tem nenhuma.
     */
    public static float fatorDoNível(LivingEntity quem) {
        int nível = quem instanceof net.minecraft.world.entity.player.Player gente
                ? Mana.of(gente).level() : 0;
        return nível < 20
                ? (float) (0.5 + 0.5 * nível / 19.0)
                : (float) (1.0 + 1.0 * (nível - 20) / 79.0);
    }

    /**
     * A cabeça que às vezes cai: o <b>Desmembramento</b> do original.
     *
     * <p>Cada Desmembramento posto na etapa dá <b>cinco por cento</b> de chance, e só a quem tem cabeça para
     * cair — esqueleto, esqueleto do Nether, zumbi, creeper e gente. O original conta pela classe exata do
     * bicho, e não pelo que ele herda: um zumbi afogado não deixa cabeça de zumbi.
     */
    private static void desmembra(ServerLevel level, Spell feitiço, LivingEntity quem, LivingEntity morto) {
        if (morto.getHealth() > 0.0f) return;
        double chance = feitiço.add(level, SpellModifierKind.DISMEMBERING_LEVEL, 0.0);
        if (chance <= 0.0 || level.getRandom().nextDouble() > chance) return;

        var tipo = morto.getType();
        net.minecraft.world.item.Item cabeça = null;
        if (tipo == net.minecraft.world.entity.EntityTypes.SKELETON) {
            cabeça = net.minecraft.world.item.Items.SKELETON_SKULL;
        } else if (tipo == net.minecraft.world.entity.EntityTypes.WITHER_SKELETON) {
            cabeça = net.minecraft.world.item.Items.WITHER_SKELETON_SKULL;
        } else if (tipo == net.minecraft.world.entity.EntityTypes.ZOMBIE) {
            cabeça = net.minecraft.world.item.Items.ZOMBIE_HEAD;
        } else if (tipo == net.minecraft.world.entity.EntityTypes.CREEPER) {
            cabeça = net.minecraft.world.item.Items.CREEPER_HEAD;
        } else if (morto instanceof net.minecraft.world.entity.player.Player) {
            cabeça = net.minecraft.world.item.Items.PLAYER_HEAD;
        }
        if (cabeça == null) return;

        net.minecraft.world.level.block.Block.popResource(level, morto.blockPosition(),
                new net.minecraft.world.item.ItemStack(cabeça));
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

        /** Ela puxa para o Fogo. */
        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.FIRE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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

            vivo.igniteForSeconds(2.0f);
            boolean pegou = fere(level, feitiço, quem, alvo, ArcanaDamage.fire(level, quem), BASE);
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

        /** Ela puxa para a Vida — e ela puxa cinco vezes mais que os danos. */
        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIFE);
        }

        @Override
        public float affinityShift() {
            return 0.05f;
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
                int quanto = (int) feitiço.mul(level, SpellModifierKind.HEALING, UNDEAD);
                vivo.igniteForSeconds(2.0f);
                return vivo.hurtServer(level, level.damageSources().indirectMagic(quem, quem), quanto);
            }

            int quanto = (int) feitiço.mul(level, SpellModifierKind.HEALING, BASE);
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

        /** Ela puxa para lado nenhum: a Luz é a Afinidade nenhuma no original. */
        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.NONE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            int tempo = (int) feitiço.mul(level, SpellModifierKind.DURATION, BASE_TICKS);
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

        /** Ela puxa para a Terra, e de leve: um milésimo por vez. */
        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.EARTH);
        }

        @Override
        public float affinityShift() {
            return 0.001f;
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

            double força = feitiço.mul(level, SpellModifierKind.MINING_POWER, SpellModifierKind.MINING_POWER.base);
            if (dureza > força * 3.0) return false;

            // a Prosperidade e o Toque de Pena não mexem no feitiço: mexem no que cai. O original encanta com
            // elas a ferramenta invisível com que colhe o bloco, e é o que se faz aqui.
            int sorte = feitiço.count(SpellModifierKind.FORTUNE_LEVEL);
            boolean seda = feitiço.has(SpellModifierKind.SILKTOUCH_LEVEL);
            if (sorte > 0 || seda) {
                var ferramenta = net.thaumcraft.item.Focuses.harvestTool(level, seda ? 0 : sorte, seda);
                for (var caiu : net.minecraft.world.level.block.Block.getDrops(
                        feitio, level, onde, level.getBlockEntity(onde), quem, ferramenta)) {
                    net.minecraft.world.level.block.Block.popResource(level, onde, caiu);
                }
                level.removeBlock(onde, false);
            } else {
                level.destroyBlock(onde, true, quem);
            }
            level.playSound(null, onde, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);
            return true;
        }
    });

    /**
     * <b>Toque Gélido</b>: o {@code FrostDamage}.
     *
     * <p>Cinco de dano e um bom tempo de lentidão. O original lhe chama Frost Damage, e é a irmã do fogo.
     */
    public static final SpellPart.Essence FROST_DAMAGE = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 5.0f;
        public static final int SLOW_TICKS = 100;
        public static final float MANA = 110.0f;

        @Override
        public String name() {
            return "frost_damage";
        }

        /** Ela puxa para o Gelo. */
        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ICE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            boolean pegou = fere(level, feitiço, quem, alvo, ArcanaDamage.frost(level, quem), BASE);
            if (pegou) {
                int tempo = (int) feitiço.mul(level, SpellModifierKind.DURATION, SLOW_TICKS);
                vivo.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, tempo, 1));
                level.sendParticles(ParticleTypes.SNOWFLAKE, vivo.getX(), vivo.getY() + 1.0, vivo.getZ(),
                        12, 0.3, 0.4, 0.3, 0.02);
            }
            return pegou;
        }
    });

    // ------------------------------------------------------------------ os outros danos

    /**
     * <b>Dano Físico</b>: o {@code PhysicalDamage}.
     *
     * <p>Oito de dano, que é o mais alto de todos os danos de base — e é de propósito: ele é <b>barato</b>,
     * quarenta de mana, contra os 120 do fogo e os 180 do raio. O que ele não tem é o resto: não queima, não
     * lentifica, não atravessa nada. É uma paulada.
     */
    public static final SpellPart.Essence PHYSICAL_DAMAGE = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 8.0f;
        public static final float MANA = 40.0f;

        @Override
        public String name() {
            return "physical_damage";
        }

        /** Ele puxa para a Terra. */
        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.EARTH);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            return fere(level, feitiço, quem, alvo, level.damageSources().mobAttack(quem), BASE);
        }
    });

    /**
     * <b>Dano Arcano</b>: o {@code MagicDamage}.
     *
     * <p>Seis de dano, e o dano que menos coisas resistem. Ele é a porta da Afinidade Arcana e da do Fim ao
     * mesmo tempo — é a única essência do ramo que puxa para <b>duas</b> Afinidades e nenhuma delas é a óbvia.
     */
    public static final SpellPart.Essence MAGIC_DAMAGE = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 6.0f;
        public static final float MANA = 80.0f;

        @Override
        public String name() {
            return "magic_damage";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ARCANE, Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            return fere(level, feitiço, quem, alvo,
                    level.damageSources().indirectMagic(quem, quem), BASE);
        }
    });

    /**
     * <b>Dano de Raio</b>: o {@code LightningDamage}.
     *
     * <p><b>Doze</b> de dano — o mais alto de todos —, por 180 de mana, que também é o mais caro dos danos
     * simples. Ele não faz nada além de ferir, e fere mais do que qualquer outro.
     */
    public static final SpellPart.Essence LIGHTNING_DAMAGE = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 12.0f;
        public static final float MANA = 180.0f;

        @Override
        public String name() {
            return "lightning_damage";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIGHTNING);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            boolean pegou = fere(level, feitiço, quem, alvo, ArcanaDamage.lightning(level, quem), BASE);
            if (pegou) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, alvo.getX(), alvo.getY() + 1.0, alvo.getZ(),
                        16, 0.3, 0.4, 0.3, 0.1);
            }
            return pegou;
        }
    });

    /**
     * <b>Afogar</b>: o {@code Drown}.
     *
     * <p>Doze de dano a quem respira, <b>fora d'água</b>. Morto-vivo não se afoga, e golem de ferro também
     * não — é o que o original diz, nesta ordem, e vale para os dois.
     */
    public static final SpellPart.Essence DROWN = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 12.0f;
        public static final float MANA = 80.0f;

        @Override
        public String name() {
            return "drown";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.WATER);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (alvo.getType() == net.minecraft.world.entity.EntityTypes.IRON_GOLEM) return false;
            if (vivo.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                return false;
            }
            boolean pegou = fere(level, feitiço, quem, alvo, ArcanaDamage.drown(level, quem), BASE);
            if (pegou) {
                level.sendParticles(ParticleTypes.BUBBLE, vivo.getX(), vivo.getEyeY(), vivo.getZ(),
                        20, 0.3, 0.3, 0.3, 0.02);
            }
            return pegou;
        }
    });

    /**
     * <b>Drenar Vida</b>: o {@code LifeDrain}.
     *
     * <p>Quatro de dano, e <b>um quarto do que feriu volta como vida</b> para quem lançou. Morto-vivo não
     * sangra vida nenhuma — e repare que o original devolve {@code true} nesse caso, o que quer dizer que o
     * feitiço <b>é cobrado na mesma</b>. É mantido: quem atira em esqueleto paga para não fazer nada.
     *
     * <p>A conta do que se cura é do original e é uma divisão de inteiros: <b>quatro de dano cura um</b>, e
     * oito curam dois. Com modificadores de dano cura mais, mas sempre de quatro em quatro.
     */
    public static final SpellPart.Essence LIFE_DRAIN = SpellParts.essence(new SpellPart.Essence() {
        public static final int BASE = 4;
        public static final float MANA = 300.0f;

        @Override
        public String name() {
            return "life_drain";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIFE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (vivo.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                // o original cobra o feitiço mesmo assim
                return true;
            }

            int quanto = (int) feitiço.add(level, SpellModifierKind.DAMAGE, BASE);
            boolean pegou = fere(level, feitiço, quem, alvo,
                    level.damageSources().indirectMagic(quem, quem), BASE);
            if (!pegou) return false;

            quem.heal(quanto / 4);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, vivo.getX(), vivo.getY() + 1.0, vivo.getZ(),
                    8, 0.3, 0.3, 0.3, 0.0);
            return true;
        }
    });

    /**
     * <b>Vida por Mana</b>: o {@code LifeTap}.
     *
     * <p>Ele não fere o alvo — fere <b>quem o lança</b>, e troca essa vida por mana. Dois de dano em si
     * próprio, e de volta <b>um por cento do teto de mana por ponto de dano</b>: quem tem muito teto ganha
     * muito, e quem não tem, não.
     *
     * <p>Não custa mana nenhuma, o que faz sentido: ele é a saída de quem ficou sem.
     */
    public static final SpellPart.Essence LIFE_TAP = SpellParts.essence(new SpellPart.Essence() {
        public static final float BASE = 2.0f;

        @Override
        public String name() {
            return "life_tap";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIFE, Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
        }

        @Override
        public float manaCost() {
            return 0.0f;
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof LivingEntity)) return false;
            if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return false;

            // aqui o original multiplica em vez de somar, ao contrário de todos os outros danos
            double dano = feitiço.mul(level, SpellModifierKind.DAMAGE, BASE);
            Mana conta = Mana.of(gente);
            float devolve = (float) (dano * 0.01 * conta.maxMana());
            if (!quem.hurtServer(level, level.damageSources().magic(), (int) Math.floor(dano))) {
                return false;
            }

            Mana.set(gente, Mana.of(gente).withMana(Mana.of(gente).mana() + devolve));
            return true;
        }
    });

    /**
     * <b>Drenar Mana</b>: o {@code ManaDrain}.
     *
     * <p>Rouba <b>250 de mana</b> de quem tiver mana, e dá a quem lançou. Não fere ninguém: é a essência para
     * usar contra outro arcanista, e contra mais ninguém.
     */
    public static final SpellPart.Essence MANA_DRAIN = SpellParts.essence(new SpellPart.Essence() {
        /** O que se rouba de uma vez: os 250 do original. */
        public static final float ROUBA = 250.0f;
        public static final float MANA = 20.0f;

        @Override
        public String name() {
            return "mana_drain";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ARCANE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (!(alvo instanceof net.minecraft.world.entity.player.Player vítima)) return false;
            if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return false;

            Mana dele = Mana.of(vítima);
            float rouba = Math.min(ROUBA, dele.mana());
            if (rouba <= 0.0f) return false;

            Mana.set(vítima, dele.withMana(dele.mana() - rouba));
            Mana.set(gente, Mana.of(gente).withMana(Mana.of(gente).mana() + rouba));
            return true;
        }
    });

    /**
     * <b>Ignição</b>: o {@code Ignition}.
     *
     * <p>Põe fogo — em quem não está ardendo já, ou no chão onde bater. <b>Três segundos</b> de base, que a
     * Duração multiplica. É a essência mais barata que faz dano de verdade, porque o dano vem do fogo e não
     * dela.
     */
    public static final SpellPart.Essence IGNITION = SpellParts.essence(new SpellPart.Essence() {
        /** Os três segundos do original. */
        public static final int BASE_SECONDS = 3;
        public static final float MANA = 35.0f;

        @Override
        public String name() {
            return "ignition";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.FIRE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (alvo.isOnFire()) return false;
            alvo.igniteForSeconds((float) feitiço.mul(level, SpellModifierKind.DURATION, BASE_SECONDS));
            return true;
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            // o original anda um bloco para o lado da face em que bateu, e se não couber tenta o de cima
            BlockPos lugar = onde.relative(face);
            if (!podeArder(level, lugar)) {
                lugar = lugar.above();
                if (!podeArder(level, lugar)) return false;
            }
            level.setBlockAndUpdate(lugar, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
            return true;
        }

        /** Onde o fogo cabe: no ar, na neve e na flor, que é o que o original deixa queimar. */
        private boolean podeArder(ServerLevel level, BlockPos onde) {
            var feitio = level.getBlockState(onde);
            if (feitio.isAir()) return true;
            if (feitio.is(net.minecraft.world.level.block.Blocks.SNOW)) return true;
            return feitio.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock;
        }
    });

    /**
     * <b>Derreter Armadura</b>: o {@code MeltArmor}.
     *
     * <p>Come <b>um quarto da durabilidade inteira</b> de cada peça de armadura de quem levar. Não fere, não
     * queima: desfaz o que protege, e o resto do feitiço trata do resto.
     *
     * <p>No original isto só vale contra <b>gente</b>, e é mantido — um esqueleto de armadura não a perde.
     */
    public static final SpellPart.Essence MELT_ARMOR = SpellParts.essence(new SpellPart.Essence() {
        /** O quarto da durabilidade: o {@code 0.25F} do original. */
        public static final float PARTE = 0.25f;
        public static final float MANA = 15.0f;

        @Override
        public String name() {
            return "melt_armor";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.FIRE);
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
            if (!(alvo instanceof net.minecraft.server.level.ServerPlayer vítima)) return false;

            boolean pegou = false;
            for (var onde : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (onde.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) continue;
                var peça = vítima.getItemBySlot(onde);
                if (peça.isEmpty() || !peça.isDamageableItem()) continue;
                int come = (int) Math.ceil(peça.getMaxDamage() * PARTE);
                peça.hurtAndBreak(come, level, vítima, item -> {
                });
                pegou = true;
            }
            if (pegou) {
                level.sendParticles(ParticleTypes.LAVA, vítima.getX(), vítima.getY() + 1.0, vítima.getZ(),
                        8, 0.3, 0.5, 0.3, 0.0);
            }
            return pegou;
        }
    });

    // ------------------------------------------------------------------ as que põem um efeito

    /**
     * Uma essência que <b>põe um efeito</b> em quem ela pega.
     *
     * <p>Vinte e tal das essências do original são esta mesma coisa escrita vinte e tal vezes: contam a
     * duração, contam quantos <b>Poderes de Bênção</b> há na frase, e põem o efeito com essa duração e esse
     * grau. <b>Seiscentas batidas</b> de base — meio minuto — e a Duração as multiplica.
     *
     * <p>O original faz aqui mais uma coisa que este porte não faz: se a pessoa estiver dentro de um
     * <b>círculo de ritual</b> desenhado no chão, a duração salta uma hora por grau. Os rituais são da outra
     * metade do Ars Magica 2 — a das máquinas e do Obelisco —, e estão declarados fora deste ramo.
     *
     * @param qual  o efeito que ela põe
     * @param mana  o que ela custa
     * @param puxa  e o quanto ela puxa a Afinidade de quem a lança
     */
    private record Bênção(String name, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> qual,
                          float mana, java.util.Set<Affinity> afinidades, float puxa)
            implements SpellPart.Essence {
        /** As seiscentas batidas do original. */
        public static final int BASE = 600;

        @Override
        public java.util.Set<Affinity> affinities() {
            return this.afinidades;
        }

        @Override
        public float affinityShift() {
            return this.puxa;
        }

        @Override
        public float manaCost() {
            return this.mana;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(this.mana);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof LivingEntity vivo)) return false;
            vivo.addEffect(new MobEffectInstance(this.qual, duração(level, feitiço), grau(feitiço)));
            return true;
        }

        /** Quanto tempo ele dura: as seiscentas batidas multiplicadas pela Duração. */
        static int duração(ServerLevel level, Spell feitiço) {
            return (int) feitiço.mul(level, SpellModifierKind.DURATION, BASE);
        }

        /** E com que força: um grau por Poder de Bênção posto na frase. */
        static int grau(Spell feitiço) {
            return feitiço.count(SpellModifierKind.BUFF_POWER);
        }
    }

    /**
     * <b>Pressa</b>: se anda mais depressa. Os degraus do original não são uma escada de passos iguais —
     * 0,2, depois 0,45, depois 0,9 —, e é o que faz valer a pena pôr mais um Poder de Bênção.
     */
    public static final SpellPart.Essence HASTE = SpellParts.essence(
            new Bênção("haste", ArcanaEffects.HASTE, 80.0f, java.util.Set.of(Affinity.LIGHTNING), 0.05f));

    /**
     * <b>Regeneração</b>: um de vida de oitenta em oitenta batidas, e o dobro da pressa por cada grau.
     *
     * <p>Custa <b>540</b> de mana, que é muito para o que faz de uma vez — e pouco para o que faz num minuto.
     */
    public static final SpellPart.Essence REGENERATION = SpellParts.essence(
            new Bênção("regeneration", ArcanaEffects.REGENERATION, 540.0f, java.util.Set.of(Affinity.LIFE, Affinity.NATURE), 0.05f));

    /**
     * <b>Visão Noturna</b>: a do jogo, tal e qual — o original também usa a poção de sempre.
     */
    public static final SpellPart.Essence NIGHT_VISION = SpellParts.essence(
            new Bênção("night_vision", net.minecraft.world.effect.MobEffects.NIGHT_VISION, 80.0f, java.util.Set.of(Affinity.ENDER), 0.05f));

    /**
     * <b>Respirar na Água</b>: o ar não desce. O original tem efeito próprio para isto, e não usa o do jogo,
     * porque o dele guarda o ar que havia em vez de o encher.
     */
    public static final SpellPart.Essence WATER_BREATHING = SpellParts.essence(
            new Bênção("water_breathing", ArcanaEffects.WATER_BREATHING, 80.0f, java.util.Set.of(Affinity.WATER), 0.05f));

    /**
     * <b>Nado Rápido</b>: dentro d'água cada braçada vale <b>1,133</b> vezes mais, e mais três centésimos
     * por grau.
     */
    public static final SpellPart.Essence SWIFT_SWIM = SpellParts.essence(
            new Bênção("swift_swim", ArcanaEffects.SWIFT_SWIM, 80.0f, java.util.Set.of(Affinity.WATER), 0.05f));

    /**
     * <b>Escudo</b>: tudo o que dói passa a doer <b>um quarto</b>. É o efeito mais forte do ramo pelo que
     * custa, e o original não lhe põe teto nenhum.
     */
    public static final SpellPart.Essence SHIELD = SpellParts.essence(
            new Bênção("shield", ArcanaEffects.MAGIC_SHIELD, 80.0f, java.util.Set.of(Affinity.ARCANE), 0.05f));

    /**
     * <b>Escudo de Mana</b>: a mana leva a pancada em vez do corpo, a <b>250 por ponto de dano</b>.
     *
     * <p>É uma das dez perícias <b>prateadas</b> do original: não se compra com nível, se descobre.
     */
    public static final SpellPart.Essence MANA_SHIELD = SpellParts.essence(
            new Bênção("mana_shield", ArcanaEffects.MANA_SHIELD, 380.0f, java.util.Set.of(Affinity.LIFE, Affinity.EARTH, Affinity.WATER), 0.01f));

    /**
     * <b>Visão Verdadeira</b>: se vê o que se esconde.
     */
    public static final SpellPart.Essence TRUE_SIGHT = SpellParts.essence(
            new Bênção("true_sight", ArcanaEffects.TRUE_SIGHT, 80.0f, java.util.Set.of(Affinity.NONE), 0.05f));

    /**
     * <b>Invisibilidade</b>: a do jogo, como no original.
     */
    public static final SpellPart.Essence INVISIBILITY = SpellParts.essence(
            new Bênção("invisibility", net.minecraft.world.effect.MobEffects.INVISIBILITY, 80.0f, java.util.Set.of(Affinity.ARCANE), 0.05f));

    /**
     * <b>Voo</b>: se voa. Oitenta de mana por meio minuto de asas, e o relógio da mana não para enquanto se
     * está no ar — é isto que faz do Ar uma Afinidade perigosa de se ter.
     */
    public static final SpellPart.Essence FLIGHT = SpellParts.essence(
            new Bênção("flight", ArcanaEffects.FLIGHT, 80.0f, java.util.Set.of(Affinity.AIR), 0.05f));

    /**
     * <b>Levitação</b>: se voa devagar e mal. Quem a tem anda a dois quintos do que andava e quase não sobe
     * — é o voo de quem não sabe voar.
     */
    public static final SpellPart.Essence LEVITATION = SpellParts.essence(
            new Bênção("levitation", ArcanaEffects.LEVITATION, 80.0f, java.util.Set.of(Affinity.AIR), 0.05f));

    /**
     * <b>Queda de Pena</b>: desce-se a quatro quintos, e chegar ao chão não dói.
     */
    public static final SpellPart.Essence SLOWFALL = SpellParts.essence(
            new Bênção("slowfall", ArcanaEffects.SLOWFALL, 80.0f, java.util.Set.of(Affinity.AIR), 0.05f));

    /**
     * <b>Refletir</b>: o próximo projétil de feitiço volta para trás.
     *
     * <p><b>1440</b> de mana, o segundo mais caro de todo o ramo. E se gasta de uma vez: o efeito sai no
     * instante em que bloqueia alguma coisa.
     */
    public static final SpellPart.Essence REFLECT = SpellParts.essence(
            new Bênção("reflect", ArcanaEffects.SPELL_REFLECT, 1440.0f, java.util.Set.of(Affinity.ARCANE), 0.05f));

    /**
     * <b>Congelar</b>: prende quem leva. Vinte e nove de mana — a essência de efeito mais barata do ramo —,
     * e os degraus do gelo são 0,2, 0,5 e 0,8 para trás.
     */
    public static final SpellPart.Essence FREEZE = SpellParts.essence(
            new Bênção("freeze", ArcanaEffects.FROST_SLOW, 29.0f, java.util.Set.of(Affinity.ICE), 0.02f));

    /**
     * <b>Lentidão</b>: o mesmo efeito do Congelar por quase três vezes o preço.
     *
     * <p><b>É assim no original</b>, e não é engano de leitura: o {@code Slow} e o {@code Freeze} põem os dois
     * o {@code BuffEffectFrostSlowed}, com a mesma duração e o mesmo grau. O que muda é o preço — 80 contra
     * 29 — e a Afinidade que cada um puxa. Fica como está.
     */
    public static final SpellPart.Essence SLOW = SpellParts.essence(
            new Bênção("slow", ArcanaEffects.FROST_SLOW, 80.0f, java.util.Set.of(Affinity.ICE), 0.05f));

    /**
     * <b>Túmulo de Água</b>: dentro d'água, puxa para o fundo — meio bloco por batida, e mais meio por grau.
     */
    public static final SpellPart.Essence WATERY_GRAVE = SpellParts.essence(
            new Bênção("watery_grave", ArcanaEffects.WATERY_GRAVE, 80.0f, java.util.Set.of(Affinity.WATER), 0.05f));

    /**
     * <b>Fúria</b>: cada golpe de quem a tem fere <b>quatro</b> a mais.
     *
     * <p>Os 261 de mana são do original, e é o único número redondo-por-acaso do ramo inteiro.
     */
    public static final SpellPart.Essence FURY = SpellParts.essence(
            new Bênção("fury", ArcanaEffects.FURY, 261.0f, java.util.Set.of(Affinity.FIRE, Affinity.LIGHTNING), 0.01f));

    /**
     * <b>Cegueira</b>: a do jogo.
     */
    public static final SpellPart.Essence BLIND = SpellParts.essence(
            new Bênção("blind", net.minecraft.world.effect.MobEffects.BLINDNESS, 80.0f, java.util.Set.of(Affinity.ENDER), 0.05f));

    /**
     * <b>Náusea</b>: a do jogo. Duzentos de mana para enjoar alguém.
     */
    public static final SpellPart.Essence NAUSEATE = SpellParts.essence(
            new Bênção("nauseate", net.minecraft.world.effect.MobEffects.NAUSEA, 200.0f, java.util.Set.of(Affinity.LIFE), 0.05f));

    /**
     * <b>Silêncio</b>: quem o leva não lança feitiço nenhum.
     *
     * <p>É a pior coisa que se pode fazer a um arcanista, e custa <b>800</b> de mana. Contra quem não lança
     * feitiços não faz nada — e o original cobra na mesma.
     */
    public static final SpellPart.Essence SILENCE = SpellParts.essence(
            new Bênção("silence", ArcanaEffects.SILENCE, 800.0f, java.util.Set.of(Affinity.WATER, Affinity.ENDER), 0.01f));

    /**
     * <b>Enredar</b>: não se mexe. Nem para cima, nem para o lado.
     */
    public static final SpellPart.Essence ENTANGLE = SpellParts.essence(
            new Bênção("entangle", ArcanaEffects.ENTANGLED, 80.0f, java.util.Set.of(Affinity.NATURE), 0.05f));

    /**
     * <b>Embaralhar Sinapses</b>: a mão vai para onde não se mandou.
     *
     * <p><b>Sete mil</b> de mana: a essência mais cara do ramo inteiro, de longe. No original quem a lança é
     * o Guardião do Raio, e quem a compra tem de a descobrir primeiro.
     */
    public static final SpellPart.Essence SCRAMBLE_SYNAPSES = SpellParts.essence(
            new Bênção("scramble_synapses", ArcanaEffects.SCRAMBLE_SYNAPSES, 7000.0f, java.util.Set.of(Affinity.LIGHTNING), 0.05f));

    /**
     * <b>Encolher</b>: fica-se pequeno — e quem é pequeno não se machuca ao cair.
     */
    public static final SpellPart.Essence SHRINK = SpellParts.essence(
            new Bênção("shrink", ArcanaEffects.SHRINK, 120.0f, java.util.Set.of(Affinity.NONE), 0.05f));

    /**
     * <b>Distorção Astral</b>: ninguém sai dali por magia, nem chega. É o contra-feitiço do Piscar.
     */
    public static final SpellPart.Essence ASTRAL_DISTORTION = SpellParts.essence(
            new Bênção("astral_distortion", ArcanaEffects.ASTRAL_DISTORTION, 80.0f, java.util.Set.of(Affinity.ENDER), 0.05f));

    /**
     * <b>Âncora do Tempo</b>: guarda onde se estava e como se estava, e devolve tudo quando o efeito acaba.
     *
     * <p>Puxa <b>quinze centésimos</b> de Afinidade Arcana por lançamento, que é quinze vezes o que um dano
     * puxa — é a essência que mais depressa faz de alguém um arcanista.
     */
    public static final SpellPart.Essence CHRONO_ANCHOR = SpellParts.essence(
            new Bênção("chrono_anchor", ArcanaEffects.CHRONO_ANCHOR, 80.0f, java.util.Set.of(Affinity.ARCANE), 0.15f));

    /**
     * <b>Poço de Gravidade</b>: cai-se mais depressa, e a queda dói uma vez e meia.
     */
    public static final SpellPart.Essence GRAVITY_WELL = SpellParts.essence(
            new Bênção("gravity_well", ArcanaEffects.GRAVITY_WELL, 80.0f, java.util.Set.of(Affinity.EARTH, Affinity.ENDER), 0.01f));

    /**
     * <b>Salto</b>: se pula alto e para onde se olha — e não se morre disso, porque o Salto traz a almofada
     * junto: oito, vinte ou 45 blocos de queda perdoados, pelo grau.
     */
    public static final SpellPart.Essence LEAP = SpellParts.essence(
            new Bênção("leap", ArcanaEffects.LEAP, 70.0f, java.util.Set.of(Affinity.AIR), 0.01f));


    /**
     * <b>Dissipar</b>: tira os efeitos de quem os tem.
     *
     * <p>E não tira todos: tira o que couber num <b>orçamento de seis</b>. Cada efeito custa o <b>grau</b> em
     * que está — um efeito de grau zero é de graça, um de grau três come metade do orçamento —, e o que não
     * couber fica. É a conta do original, e é ela que faz do Dissipar uma coisa que se usa com jeito e não um
     * botão de apagar tudo.
     */
    public static final SpellPart.Essence DISPEL = SpellParts.essence(new SpellPart.Essence() {
        /** O orçamento do original. */
        public static final int ORÇAMENTO = 6;
        public static final float MANA = 200.0f;

        @Override
        public String name() {
            return "dispel";
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

            int sobra = ORÇAMENTO;
            var tirar = new java.util.ArrayList<net.minecraft.core.Holder<
                    net.minecraft.world.effect.MobEffect>>();
            for (var efeito : vivo.getActiveEffects()) {
                int custa = efeito.getAmplifier();
                if (custa > sobra) continue;
                sobra -= custa;
                tirar.add(efeito.getEffect());
            }
            for (var qual : tirar) vivo.removeEffect(qual);
            return !tirar.isEmpty();
        }
    });

    /**
     * <b>Encantar</b>: quem leva passa a lutar do lado de quem lançou.
     *
     * <p>Num bicho de criação ele faz outra coisa, e é o original que o diz: em vez de o encantar, <b>põe-no
     * no cio</b>. É a única essência do ramo com duas caras conforme quem leva.
     */
    public static final SpellPart.Essence CHARM = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 300.0f;

        @Override
        public String name() {
            return "charm";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIFE);
        }

        @Override
        public float affinityShift() {
            return 0.1f;
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
            if (!(alvo instanceof net.minecraft.world.entity.PathfinderMob bicho)) return false;
            if (bicho.hasEffect(ArcanaEffects.CHARMED)) return false;

            if (bicho instanceof net.minecraft.world.entity.animal.Animal criação) {
                criação.setInLove(quem instanceof net.minecraft.world.entity.player.Player gente ? gente : null);
                return true;
            }

            bicho.addEffect(new MobEffectInstance(ArcanaEffects.CHARMED,
                    Bênção.duração(level, feitiço), Bênção.grau(feitiço)));
            // e deixa de querer mal a quem o encantou
            bicho.setTarget(null);
            bicho.setLastHurtByMob(null);
            return true;
        }
    });

    // ------------------------------------------------------------------ as que empurram

    /**
     * <b>Arremesso</b>: atira para cima.
     *
     * <p><b>1,05</b> de velocidade para o alto, e é tudo. Vinte de mana. A Velocidade Acrescentada lhe soma
     * meio por vez, e três delas atiram alguém alto o bastante para a queda o matar — que é o feitiço inteiro.
     */
    public static final SpellPart.Essence FLING = SpellParts.essence(new SpellPart.Essence() {
        public static final double BASE = 1.05;
        public static final float MANA = 20.0f;

        @Override
        public String name() {
            return "fling";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.AIR);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            double sobe = feitiço.add(level, SpellModifierKind.VELOCITY_ADDED, BASE);
            alvo.push(0.0, sobe, 0.0);
            alvo.hurtMarked = true;
            return true;
        }
    });

    /**
     * <b>Empurrão</b>: atira para longe de quem lançou.
     *
     * <p><b>1,5</b> na horizontal e <b>0,325</b> para cima, na linha que sai de quem lança para quem leva. Não
     * fere: só afasta — e afastar alguém de uma beirada é o mesmo que o empurrar dela.
     */
    public static final SpellPart.Essence KNOCKBACK = SpellParts.essence(new SpellPart.Essence() {
        public static final double BASE = 1.5;
        /** O que ele levanta, que é pouco e é de propósito: é um empurrão e não um arremesso. */
        public static final double SOBE = 0.325;
        public static final float MANA = 60.0f;

        @Override
        public String name() {
            return "knockback";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.AIR, Affinity.WATER, Affinity.EARTH);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (!(alvo instanceof LivingEntity)) return false;
            double força = feitiço.add(level, SpellModifierKind.VELOCITY_ADDED, BASE);
            double ângulo = Math.atan2(alvo.getZ() - quem.getZ(), alvo.getX() - quem.getX());
            alvo.push(força * Math.cos(ângulo), SOBE, força * Math.sin(ângulo));
            alvo.hurtMarked = true;
            return true;
        }
    });

    /**
     * <b>Repelir</b>: afasta tudo o que estiver em roda.
     *
     * <p>Se apontado a outra coisa, afasta só essa. Se apontado a quem o lança, afasta <b>tudo o que estiver a
     * dois blocos</b> — e é assim que ele se usa: quando já há gente demais perto.
     *
     * <p>A conta do original <b>parece</b> depender da distância e não depende: ele divide a linha entre os
     * dois por <b>2,5</b> <i>e pela distância</i>, e dividir uma linha pelo seu próprio comprimento dá uma
     * linha de comprimento um. O empurrão sai sempre com a mesma força — <b>0,4</b> —, e o que muda de um
     * para o outro é só o <b>rumo</b>.
     *
     * <p>E há um detalhe que não é engano: o original soma <b>um décimo</b> à distância antes de dividir. Por
     * causa disso quem está <i>colado</i> é empurrado um pouco <b>menos</b> do que quem está a dois blocos —
     * o contrário do que a intuição diz. Fica.
     */
    public static final SpellPart.Essence REPEL = SpellParts.essence(new SpellPart.Essence() {
        /** A que distância ele alcança, quando apontado a quem o lança. */
        public static final double RODA = 2.0;
        public static final float MANA = 5.0f;

        @Override
        public String name() {
            return "repel";
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
            if (alvo != quem) {
                afasta(quem, alvo);
                return true;
            }
            for (Entity e : level.getEntities(quem, quem.getBoundingBox().inflate(RODA))) {
                afasta(quem, e);
            }
            return true;
        }

        private void afasta(LivingEntity quem, Entity alvo) {
            Vec3 daqui = quem.position();
            Vec3 ali = alvo.position();
            double quanto = daqui.distanceTo(ali) + 0.1;
            Vec3 linha = ali.subtract(daqui);
            alvo.push(linha.x / 2.5 / quanto, linha.y / 2.5 / quanto, linha.z / 2.5 / quanto);
            alvo.hurtMarked = true;
        }
    });

    /**
     * <b>Telecinese</b>: chama a si o que está caído no chão.
     *
     * <p>Itens e bolas de experiência a <b>dezesseis blocos</b> de distância e <b>três</b> de altura vêm ter
     * com o ponto em que o feitiço bateu, a <b>quinze centésimos</b> de velocidade por batida. E não sobem:
     * o original corta a parte para cima do movimento, para nada ficar a saltitar.
     *
     * <p>Só chama o que já está no chão há <b>um segundo</b> — sem isso, ela roubaria o que acabou de cair da
     * mão de quem a lançou.
     */
    public static final SpellPart.Essence TELEKINESIS = SpellParts.essence(new Puxa("telekinesis", 6.0f,
            java.util.Set.of(Affinity.ARCANE), 0.001f));

    /**
     * <b>Atrair</b>: a mesma coisa que a Telecinese, e o original as escreve duas vezes.
     *
     * <p>Elas chamam a <i>mesma</i> conta — o {@code doTK_Extrapolated} —, com os mesmos números. O que muda é
     * o preço, que é metade, e a Afinidade, que aqui não é nenhuma. Fica como está.
     */
    public static final SpellPart.Essence ATTRACT = SpellParts.essence(new Puxa("attract", 2.6f,
            java.util.Set.of(Affinity.NONE), 0.0f));

    /** A conta que a Telecinese e o Atrair partilham. */
    private record Puxa(String name, float mana, java.util.Set<Affinity> afinidades, float puxa)
            implements SpellPart.Essence {
        /** Até onde ela alcança, e a que altura. */
        public static final double LONGE = 16.0;
        public static final double ALTO = 3.0;
        /** E a que velocidade traz o que pegou. */
        public static final double VEM = 0.15;
        /** O que caiu agora não vem: o original espera vinte batidas. */
        public static final int ESPERA = 20;

        @Override
        public java.util.Set<Affinity> affinities() {
            return this.afinidades;
        }

        @Override
        public float affinityShift() {
            return this.puxa;
        }

        @Override
        public float manaCost() {
            return this.mana;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(this.mana);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            return puxaPara(level, alvo.position());
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return puxaPara(level, batida);
        }

        static boolean puxaPara(ServerLevel level, Vec3 para) {
            var caixa = new net.minecraft.world.phys.AABB(
                    para.x - LONGE, para.y - ALTO, para.z - LONGE,
                    para.x + LONGE, para.y + ALTO, para.z + LONGE);
            boolean pegou = false;
            for (Entity e : level.getEntities((Entity) null, caixa,
                    e -> e instanceof net.minecraft.world.entity.item.ItemEntity
                            || e instanceof net.minecraft.world.entity.ExperienceOrb)) {
                if (e.tickCount < ESPERA) continue;
                Vec3 linha = para.subtract(e.position()).normalize().scale(VEM);
                // nada sobe: o original corta a parte de cima para o que ele chama não ficar saltitando
                e.setDeltaMovement(linha.x, Math.min(0.0, linha.y), linha.z);
                pegou = true;
            }
            return pegou;
        }
    }

    /**
     * <b>Acelerar</b>: apressa o passo de quem o lança.
     *
     * <p>Seis de mana, a essência mais barata do ramo inteiro. E o que ela faz é <b>quase nada</b>: o original
     * multiplica por 1,6 a velocidade de passo da inteligência do bicho — o número que o jogo recalcula a cada
     * batida. <b>Num bicho, ele salta uma vez; numa pessoa, não faz nada de nada.</b>
     *
     * <p>Fica como está, e fica declarado: consertá-lo seria inventar um feitiço que o Ars Magica 2 não tem.
     */
    public static final SpellPart.Essence ACCELERATE = SpellParts.essence(new SpellPart.Essence() {
        public static final float VEZES = 1.6f;
        public static final float MANA = 6.0f;

        @Override
        public String name() {
            return "accelerate";
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
            if (quem instanceof net.minecraft.world.entity.Mob bicho) {
                bicho.setSpeed(bicho.getSpeed() * VEZES);
            }
            return true;
        }
    });

    /**
     * <b>Desarmar</b>: tira das mãos o que estiver nelas.
     *
     * <p>Numa pessoa, larga o que ela segura. Num bicho, a arma cai no chão — e cai <b>gasta</b>: o original
     * tira-lhe entre 80 e 99 por cento da durabilidade, para desarmar um esqueleto não ser uma maneira de
     * ganhar arcos.
     */
    public static final SpellPart.Essence DISARM = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 130.0f;

        @Override
        public String name() {
            return "disarm";
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
            if (alvo instanceof net.minecraft.world.entity.player.Player gente) {
                if (gente.getMainHandItem().isEmpty()) return false;
                gente.drop(gente.getMainHandItem().copy(), false);
                gente.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                return true;
            }
            if (!(alvo instanceof net.minecraft.world.entity.Mob bicho)) return false;

            ItemStack arma = bicho.getMainHandItem();
            if (arma.isEmpty()) return false;

            ItemStack caiu = arma.copy();
            if (caiu.isDamageableItem()) {
                caiu.setDamageValue((int) Math.floor(
                        caiu.getMaxDamage() * (0.8f + level.getRandom().nextFloat() * 0.19f)));
            }
            bicho.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            net.minecraft.world.level.block.Block.popResource(level, bicho.blockPosition(), caiu);
            bicho.setTarget(quem);
            return true;
        }
    });

    // ------------------------------------------------------------------ as que deslocam

    /**
     * <b>Piscar</b>: passa para a frente, doze blocos, sem atravessar nada.
     *
     * <p>O original não atira ninguém: ele <b>procura</b>. Parte da distância cheia e vai <b>descendo um
     * bloco de cada vez</b> até achar um lugar onde caibam duas casas de ar — e em cada distância prova doze
     * lugares: os quatro cantos em roda do ponto, e os mesmos um acima e um abaixo. Por isso um Piscar contra
     * uma parede põe a pessoa <b>encostada à parede</b>, e não dentro dela.
     */
    public static final SpellPart.Essence BLINK = SpellParts.essence(new SpellPart.Essence() {
        /** Os doze blocos do original, que o Alcance soma. */
        public static final double BASE = 12.0;
        public static final float MANA = 160.0f;

        @Override
        public String name() {
            return "blink";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.05f;
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
            if (ArcanaEffects.blocksTeleport(vivo)) return true;

            double distância = feitiço.add(level, SpellModifierKind.RANGE, BASE);
            Vec3 rumo = vivo.getLookAngle().normalize();
            while (distância > 0.0) {
                Vec3 tenta = vivo.position().add(rumo.scale(distância));
                Vec3 achou = cabe(level, tenta);
                if (achou != null) {
                    vivo.teleportTo(achou.x, achou.y, achou.z);
                    level.sendParticles(ParticleTypes.PORTAL, achou.x, achou.y + 1.0, achou.z,
                            24, 0.4, 0.8, 0.4, 0.1);
                    return true;
                }
                distância -= 1.0;
            }
            return false;
        }

        /**
         * Um lugar onde caibam duas casas de ar, dos doze que o original prova.
         *
         * <p>Os quatro cantos em roda do ponto — o de baixo e o de cima de cada coordenada —, e os mesmos
         * um bloco abaixo e um bloco acima.
         */
        private Vec3 cabe(ServerLevel level, Vec3 onde) {
            int[] xs = {(int) Math.floor(onde.x), (int) Math.ceil(onde.x)};
            int[] zs = {(int) Math.floor(onde.z), (int) Math.ceil(onde.z)};
            for (int dy : new int[]{0, -1, 1}) {
                int y = (int) onde.y + dy;
                for (int x : xs) {
                    for (int z : zs) {
                        if (livre(level, x, y, z)) return new Vec3(x + 0.5, y, z + 0.5);
                    }
                }
            }
            return null;
        }

        /** Duas casas de ar, uma sobre a outra: o {@code CheckCoords} do original. */
        private boolean livre(ServerLevel level, int x, int y, int z) {
            if (y < level.getMinY()) return false;
            var baixo = new BlockPos(x, y, z);
            var cima = baixo.above();
            return level.getBlockState(baixo).getCollisionShape(level, baixo).isEmpty()
                    && level.getBlockState(cima).getCollisionShape(level, cima).isEmpty();
        }
    });

    /**
     * <b>Teleporte Aleatório</b>: atira para um lugar qualquer ali perto.
     *
     * <p><b>Nove blocos</b> de lado, e o Alcance os multiplica. O original não procura chão nenhum — ele
     * sorteia e manda, e quem cair dentro de pedra que se desenrasque. Fica assim.
     */
    public static final SpellPart.Essence RANDOM_TELEPORT = SpellParts.essence(new SpellPart.Essence() {
        /** Os nove blocos do original, que o Alcance multiplica. */
        public static final double BASE = 9.0;
        public static final float MANA = 52.5f;

        @Override
        public String name() {
            return "random_teleport";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (alvo instanceof LivingEntity vivo && ArcanaEffects.blocksTeleport(vivo)) return true;
            double lado = feitiço.mul(level, SpellModifierKind.RANGE, BASE);
            var sorte = level.getRandom();
            alvo.teleportTo(
                    alvo.getX() + (sorte.nextDouble() - 0.5) * lado,
                    alvo.getY() + (sorte.nextDouble() - 0.5) * lado,
                    alvo.getZ() + (sorte.nextDouble() - 0.5) * lado);
            level.sendParticles(ParticleTypes.PORTAL, alvo.getX(), alvo.getY() + 1.0, alvo.getZ(),
                    24, 0.4, 0.8, 0.4, 0.1);
            return true;
        }
    });

    /**
     * <b>Marca</b>: guarda este lugar.
     *
     * <p>Cinco de mana, e não faz mais nada — o que ela vale está no <b>Chamado</b>, que traz de volta aqui.
     * Marca-se <b>um lugar só</b>: marcar outra vez apaga o de antes.
     */
    public static final SpellPart.Essence MARK = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 5.0f;

        @Override
        public String name() {
            return "mark";
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
            return marca(level, quem, alvo.position());
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return marca(level, quem, batida);
        }

        private boolean marca(ServerLevel level, LivingEntity quem, Vec3 onde) {
            if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return false;
            MarkData.set(gente, new MarkData(onde, level.dimension()));
            return true;
        }
    });

    /**
     * <b>Chamado</b>: traz de volta ao lugar marcado.
     *
     * <p>Quinhentos de mana, e duas recusas do original: sem marca posta não acontece nada, e uma marca de
     * outro mundo também não serve. As duas dizem porquê, como no original.
     */
    public static final SpellPart.Essence RECALL = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 500.0f;

        @Override
        public String name() {
            return "recall";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ARCANE);
        }

        @Override
        public float affinityShift() {
            return 0.1f;
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
            if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return false;
            if (!(alvo instanceof LivingEntity vivo)) return false;
            if (ArcanaEffects.blocksTeleport(quem) || ArcanaEffects.blocksTeleport(vivo)) return true;

            MarkData marca = MarkData.of(gente);
            if (!marca.posta()) {
                gente.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.spell.no_mark"));
                return false;
            }
            if (!marca.mundo().equals(level.dimension())) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("tc.spell.mark_elsewhere"));
                return false;
            }

            vivo.teleportTo(marca.onde().x, marca.onde().y, marca.onde().z);
            level.sendParticles(ParticleTypes.PORTAL, marca.onde().x, marca.onde().y + 1.0,
                    marca.onde().z, 32, 0.4, 0.8, 0.4, 0.1);
            return true;
        }
    });

    /**
     * <b>Trocar de Lugar</b>: quem lança vai para onde o outro está, e o outro para onde quem lançou estava.
     *
     * <p>Cem de mana, e é o feitiço mais perigoso de levar: quem o leva pode ser atirado para onde quem o
     * lançou não queria estar.
     */
    public static final SpellPart.Essence TRANSPLACE = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 100.0f;

        @Override
        public String name() {
            return "transplace";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.02f;
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
            if (alvo.isRemoved()) return false;
            if (ArcanaEffects.blocksTeleport(quem)) return true;

            Vec3 dele = alvo.position();
            Vec3 meu = quem.position();
            quem.teleportTo(dele.x, dele.y, dele.z);
            alvo.teleportTo(meu.x, meu.y, meu.z);
            level.sendParticles(ParticleTypes.PORTAL, dele.x, dele.y + 1.0, dele.z, 16, 0.4, 0.8, 0.4, 0.1);
            level.sendParticles(ParticleTypes.PORTAL, meu.x, meu.y + 1.0, meu.z, 16, 0.4, 0.8, 0.4, 0.1);
            return true;
        }
    });

    /**
     * <b>Intervenção Divina</b>: leva a casa.
     *
     * <p>À cama de quem a lança, ou ao ponto de partida do mundo se não houver cama. No Fim não faz nada — o
     * original diz <i>Nothing happens...</i> e devolve verdadeiro, quer dizer que <b>cobra na mesma</b>.
     */
    public static final SpellPart.Essence DIVINE_INTERVENTION = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 400.0f;

        @Override
        public String name() {
            return "divine_intervention";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.4f;
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
            if (!(alvo instanceof net.minecraft.server.level.ServerPlayer gente)) return false;
            if (ArcanaEffects.blocksTeleport(gente)) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("tc.spell.distortion_blocks"));
                return true;
            }
            if (level.dimension().equals(net.minecraft.world.level.Level.END)) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("tc.spell.nothing_happens"));
                return true;
            }

            ServerLevel casa = level.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
            if (casa == null) return false;

            // a cama, se houver uma na superfície; senão, o lugar por onde o mundo começou
            var cama = gente.getRespawnConfig();
            Vec3 onde = cama != null
                    && cama.respawnData().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)
                    ? Vec3.atBottomCenterOf(cama.respawnData().globalPos().pos())
                    : Vec3.atBottomCenterOf(casa.getRespawnData().globalPos().pos());

            gente.teleportTo(casa, onde.x, onde.y, onde.z, java.util.Set.of(),
                    gente.getYRot(), gente.getXRot(), false);
            return true;
        }
    });

    /**
     * <b>Intervenção do Fim</b>: leva ao Nether.
     *
     * <p>A irmã da outra, e o original a escreve quase igual: no Fim não faz nada, e a quem já está no Nether
     * responde que já lá está — e essa recusa, ao contrário das outras, devolve <b>falso</b> e sai de graça.
     */
    public static final SpellPart.Essence ENDER_INTERVENTION = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 400.0f;

        @Override
        public String name() {
            return "ender_intervention";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.4f;
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
            if (!(alvo instanceof net.minecraft.server.level.ServerPlayer gente)) return false;
            if (ArcanaEffects.blocksTeleport(gente)) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("tc.spell.distortion_blocks"));
                return true;
            }
            if (level.dimension().equals(net.minecraft.world.level.Level.END)) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("tc.spell.nothing_happens"));
                return true;
            }
            if (level.dimension().equals(net.minecraft.world.level.Level.NETHER)) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("tc.spell.already_nether"));
                return false;
            }

            ServerLevel nether = level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            if (nether == null) return false;
            gente.teleportTo(nether, gente.getX() / 8.0, gente.getY(), gente.getZ() / 8.0,
                    java.util.Set.of(), gente.getYRot(), gente.getXRot(), false);
            return true;
        }
    });

    // ------------------------------------------------------------------ as que mexem no mundo

    /**
     * <b>Criar Água</b>: põe água onde bateu — ou enche o caldeirão, se for num caldeirão.
     *
     * <p>Vinte e cinco de mana. O original põe água <b>na face em que bateu</b>, e não dentro do bloco: é a
     * mesma conta de quem põe um balde.
     */
    public static final SpellPart.Essence CREATE_WATER = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 25.0f;

        @Override
        public String name() {
            return "create_water";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.WATER);
        }

        @Override
        public float affinityShift() {
            return 0.001f;
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
            if (feitio.is(net.minecraft.world.level.block.Blocks.CAULDRON)
                    || feitio.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON)) {
                level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.WATER_CAULDRON
                        .defaultBlockState().setValue(
                                net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
                return true;
            }

            BlockPos lugar = onde.relative(face);
            if (!level.getBlockState(lugar).canBeReplaced()) return false;
            level.setBlockAndUpdate(lugar,
                    net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
            return true;
        }
    });

    /** <b>Arar</b>: terra e grama viram terra arada. E mais nada vira. */
    public static final SpellPart.Essence PLOW = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 75.0f;

        @Override
        public String name() {
            return "plow";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.EARTH);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (!feitio.is(net.minecraft.world.level.block.Blocks.DIRT)
                    && !feitio.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                return false;
            }
            level.setBlockAndUpdate(onde,
                    net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState());
            return true;
        }
    });

    /**
     * <b>Plantar</b>: planta a primeira semente que houver na mochila de quem lança.
     *
     * <p>A <b>primeira</b>, e não a que se escolher: o original varre o inventário e usa a que achar. E gasta
     * uma, como quem planta à mão.
     */
    public static final SpellPart.Essence PLANT = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 80.0f;

        @Override
        public String name() {
            return "plant";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.NATURE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return false;
            if (!level.getBlockState(onde.above()).canBeReplaced()) return false;

            var mochila = gente.getInventory();
            for (int i = 0; i < mochila.getContainerSize(); i++) {
                ItemStack coisa = mochila.getItem(i);
                if (!(coisa.getItem() instanceof net.minecraft.world.item.BlockItem semente)) continue;
                var planta = semente.getBlock().defaultBlockState();
                if (!planta.canSurvive(level, onde.above())) continue;
                // a planta do jogo de hoje é um VegetationBlock: a lavoura, a flor e a erva vêm dele
                if (!(semente.getBlock() instanceof net.minecraft.world.level.block.VegetationBlock)) {
                    continue;
                }

                level.setBlockAndUpdate(onde.above(), planta);
                coisa.shrink(1);
                return true;
            }
            return false;
        }
    });

    /**
     * <b>Colher</b>: tira a planta e deixa cair o que ela daria.
     *
     * <p>E colhe <b>o que estiver lá</b>, crescido ou não — o original não olha para a idade da planta.
     */
    public static final SpellPart.Essence HARVEST_PLANTS = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 60.0f;

        @Override
        public String name() {
            return "harvest_plants";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.NATURE);
        }

        @Override
        public float affinityShift() {
            return 0.02f;
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
            if (!(feitio.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock)) {
                return false;
            }
            level.destroyBlock(onde, true, quem);
            return true;
        }
    });

    /** <b>Crescer</b>: o mesmo que o pó de osso, e pelo mesmo preço de quase nada. */
    public static final SpellPart.Essence GROW = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 17.4f;

        @Override
        public String name() {
            return "grow";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.NATURE);
        }

        @Override
        public float affinityShift() {
            return 0.02f;
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
            if (!(feitio.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock planta)) {
                return false;
            }
            if (!planta.isValidBonemealTarget(level, onde, feitio)) return false;
            if (!planta.isBonemealSuccess(level, level.getRandom(), onde, feitio)) return true;
            planta.performBonemeal(level, level.getRandom(), onde, feitio);
            level.levelEvent(2005, onde, 0);
            return true;
        }
    });

    /**
     * <b>Forja</b>: cozinha o bloco onde bateu, como se ele estivesse num forno.
     *
     * <p>Areia vira vidro, minério vira lingote, e <b>gelo vira água</b> — esse último é caso à parte no
     * original, porque gelo não tem receita de forno. Se o que sai não for bloco, ele cai no chão como item.
     */
    public static final SpellPart.Essence FORGE = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 55.0f;

        @Override
        public String name() {
            return "forge";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.FIRE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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

            if (feitio.is(net.minecraft.world.level.block.Blocks.ICE)) {
                level.setBlockAndUpdate(onde, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
                return true;
            }

            var entrada = new ItemStack(feitio.getBlock());
            if (entrada.isEmpty()) return false;
            var receita = level.recipeAccess().getRecipeFor(
                    net.minecraft.world.item.crafting.RecipeType.SMELTING,
                    new net.minecraft.world.item.crafting.SingleRecipeInput(entrada), level);
            if (receita.isEmpty()) return false;

            ItemStack saiu = receita.get().value().assemble(
                    new net.minecraft.world.item.crafting.SingleRecipeInput(entrada));
            if (saiu.isEmpty()) return false;

            if (saiu.getItem() instanceof net.minecraft.world.item.BlockItem virado) {
                level.setBlockAndUpdate(onde, virado.getBlock().defaultBlockState());
            } else {
                level.removeBlock(onde, false);
                net.minecraft.world.level.block.Block.popResource(level, onde, saiu);
            }
            level.playSound(null, onde, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 2.0f);
            return true;
        }
    });

    /**
     * <b>Outono do Mago</b>: derruba as folhas em roda, e deixa cair o que elas dariam.
     *
     * <p><b>Dois blocos</b> de raio, que o Raio soma. Quinze de mana. É a maneira do arcanista de pegar
     * mudas e maçãs sem trepar a árvore.
     */
    public static final SpellPart.Essence WIZARDS_AUTUMN = SpellParts.essence(new SpellPart.Essence() {
        /** Os dois blocos do original, que o Raio soma. */
        public static final int BASE_RAIO = 2;
        public static final float MANA = 15.0f;

        @Override
        public String name() {
            return "wizards_autumn";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.NATURE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            int raio = (int) feitiço.add(level, SpellModifierKind.RADIUS, BASE_RAIO);
            boolean pegou = false;
            for (int i = -raio; i <= raio; i++) {
                for (int j = -raio; j <= raio; j++) {
                    for (int k = -raio; k <= raio; k++) {
                        BlockPos aqui = onde.offset(i, j, k);
                        if (!level.getBlockState(aqui).is(net.minecraft.tags.BlockTags.LEAVES)) continue;
                        level.destroyBlock(aqui, true, quem);
                        pegou = true;
                    }
                }
            }
            return pegou;
        }
    });

    /**
     * <b>Seca</b>: tira a água do que ela tocar.
     *
     * <p>A flor e a erva alta viram erva morta; a grama, o micélio, o arenito e a terra viram areia; a pedra
     * vira pedregulho; o tijolo de pedra racha; e a água <b>desaparece</b>. É a lista do original, nesta
     * ordem.
     */
    public static final SpellPart.Essence DROUGHT = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 60.0f;

        @Override
        public String name() {
            return "drought";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.FIRE, Affinity.AIR);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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

            if (feitio.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock
                    || feitio.is(Blocks.SHORT_GRASS) || feitio.is(Blocks.TALL_GRASS)
                    || feitio.is(Blocks.FERN)) {
                level.setBlockAndUpdate(onde, Blocks.DEAD_BUSH.defaultBlockState());
                return true;
            }
            if (feitio.is(Blocks.GRASS_BLOCK) || feitio.is(Blocks.MYCELIUM)
                    || feitio.is(Blocks.SANDSTONE) || feitio.is(Blocks.DIRT)) {
                level.setBlockAndUpdate(onde, Blocks.SAND.defaultBlockState());
                return true;
            }
            if (feitio.is(Blocks.STONE)) {
                level.setBlockAndUpdate(onde, Blocks.COBBLESTONE.defaultBlockState());
                return true;
            }
            if (feitio.is(Blocks.STONE_BRICKS)) {
                level.setBlockAndUpdate(onde, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                return true;
            }
            if (feitio.is(Blocks.WATER)) {
                level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
                return true;
            }

            // e se não for nada disso, a água do lado em que bateu também some
            BlockPos lado = onde.relative(face);
            if (level.getBlockState(lado).is(Blocks.WATER)) {
                level.setBlockAndUpdate(lado, Blocks.AIR.defaultBlockState());
                return true;
            }
            return false;
        }
    });

    // ------------------------------------------------------------------ as que mexem no céu

    /**
     * <b>Afastar a Chuva</b>: acaba com ela, e garante um dia inteiro de sol.
     *
     * <p><b>750</b> de mana, e <b>três décimos</b> de Afinidade da Água por lançamento — trinta vezes o que um
     * dano puxa. É a essência que mais depressa faz de alguém um filho da água, e nem é preciso querer.
     *
     * <p>Se não estiver chovendo, ela não faz nada e não cobra.
     */
    public static final SpellPart.Essence BANISH_RAIN = SpellParts.essence(new SpellPart.Essence() {
        /** O dia inteiro de sol que ela deixa. */
        public static final int SOL = 24000;
        public static final float MANA = 750.0f;

        @Override
        public String name() {
            return "banish_rain";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.WATER);
        }

        @Override
        public float affinityShift() {
            return 0.3f;
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
            return afasta(level);
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return afasta(level);
        }

        private boolean afasta(ServerLevel level) {
            if (!level.isRaining()) return false;
            // no jogo de hoje o tempo mora num guardado à parte, e não no mundo
            var tempo = level.getWeatherData();
            tempo.setClearWeatherTime(SOL);
            tempo.setRainTime(0);
            tempo.setRaining(false);
            tempo.setThundering(false);
            return true;
        }
    });

    /**
     * <b>Tempestade</b>: chama a chuva — e, se ela já estiver forte, chama o raio.
     *
     * <p>Quinze de mana, que é quase nada para o que ela faz. E o que ela faz depende do céu: com tempo bom,
     * <b>começa a chover</b>; com a chuva já a bater com força, há <b>uma em cinco</b> de cair um raio num
     * monstro a cinquenta blocos. Lançá-la duas vezes não é lançá-la duas vezes.
     */
    public static final SpellPart.Essence STORM = SpellParts.essence(new SpellPart.Essence() {
        /** A cinquenta blocos, que é onde ela procura em quem cair o raio. */
        public static final int LONGE = 50;
        /** Uma em cinco: o {@code nextInt(100) < 20} do original. */
        public static final int CHANCE = 20;
        public static final float MANA = 15.0f;

        @Override
        public String name() {
            return "storm";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIGHTNING, Affinity.NATURE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
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
            return tempo(level, quem);
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return tempo(level, quem);
        }

        private boolean tempo(ServerLevel level, LivingEntity quem) {
            if (level.getRainLevel(1.0f) <= 0.9f) {
                var tempo = level.getWeatherData();
                tempo.setClearWeatherTime(0);
                tempo.setRainTime(12000);
                tempo.setRaining(true);
                return true;
            }
            if (level.getRandom().nextInt(100) >= CHANCE) return true;

            var monstros = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
                    quem.getBoundingBox().inflate(LONGE, 10.0, LONGE));
            if (monstros.isEmpty()) return true;

            var coitado = monstros.get(level.getRandom().nextInt(monstros.size()));
            if (!level.canSeeSky(coitado.blockPosition())) return true;

            var raio = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(
                    level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (raio == null) return true;
            raio.snapTo(coitado.position());
            level.addFreshEntity(raio);
            return true;
        }
    });

    /**
     * <b>Luz do Dia</b>: faz o sol nascer.
     *
     * <p><b>Vinte e cinco mil</b> de mana — mais do que qualquer arcanista tem antes do nível alto, e mais do
     * que qualquer outra essência do ramo custa por larga margem. Mexer no céu é caro.
     *
     * <p>Se já for dia, não faz nada e não cobra. E é uma das dez perícias <b>prateadas</b>: se descobre.
     */
    public static final SpellPart.Essence DAYLIGHT = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 25000.0f;

        @Override
        public String name() {
            return "daylight";
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
            return amanhece(level);
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return amanhece(level);
        }

        /**
         * Faz o sol nascer.
         *
         * <p><b>Desvio declarado.</b> O original escreve a batida à mão: o dia em curso arredondado para
         * cima, vezes 24000. O jogo de hoje já não deixa mexer no relógio assim — ele tem <b>marcos</b>, e
         * quem quer o amanhecer pede o marco do amanhecer. Dá no mesmo lugar do céu e é o que o jogo entende.
         */
        private boolean amanhece(ServerLevel level) {
            if (level.isBrightOutside()) return false;
            return level.dimensionType().defaultClock().map(relógio -> {
                level.clockManager().moveToTimeMarker(relógio,
                        net.minecraft.world.clock.ClockTimeMarkers.DAY);
                return true;
            }).orElse(false);
        }
    });

    /**
     * <b>Anoitecer</b>: faz a noite cair.
     *
     * <p>A irmã do outro, pelo mesmo preço de <b>vinte e cinco mil</b>. No original a conta é o dia em curso,
     * arredondado para baixo, mais <b>13250</b> — a batida em que o sol se põe; aqui é o marco da noite, pela
     * mesma razão que o outro.
     */
    public static final SpellPart.Essence MOONRISE = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 25000.0f;

        @Override
        public String name() {
            return "moonrise";
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
            return anoitece(level);
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return anoitece(level);
        }

        /** E a noite cair, pelo marco da noite — o mesmo desvio que o do amanhecer. */
        private boolean anoitece(ServerLevel level) {
            if (!level.isBrightOutside()) return false;
            return level.dimensionType().defaultClock().map(relógio -> {
                level.clockManager().moveToTimeMarker(relógio,
                        net.minecraft.world.clock.ClockTimeMarkers.NIGHT);
                return true;
            }).orElse(false);
        }
    });

    // ------------------------------------------------------------------ as duas que são um feitiço inteiro

    /**
     * Uma essência de <b>temporal</b>: a Nevasca e a Chuva de Fogo.
     *
     * <p>Elas não são Formas nem Essências no sentido comum — são <b>um feitiço inteiro numa peça só</b>.
     * Postas numa frase, criam no lugar uma área que fere por si, a cada batida, e vai deixando neve ou fogo
     * no chão. É por isso que as duas são perícias <b>prateadas</b> no original: não se compram, descobrem-se.
     *
     * <p>E as duas recusam se já houver uma igual a <b>dez blocos</b>. Duas nevascas no mesmo lugar seriam o
     * dobro do dano pelo dobro do preço, e o original não quer isso.
     *
     * @param raioBase o raio de partida, que o Raio soma
     * @param metade   se o raio é dividido por dois depois de somado, como a Chuva de Fogo faz
     */
    private record Temporal(String name, SpellEffectEntity.Kind qual, float mana,
                            java.util.Set<Affinity> afinidades, float puxa, int raioBase, boolean metade)
            implements SpellPart.Essence {
        /** A que distância uma igual impede outra. */
        public static final double PERTO = 10.0;

        /** E quanto tempo ela fica, antes da Duração. */
        public static final int BASE_TEMPO = 100;

        @Override
        public java.util.Set<Affinity> affinities() {
            return this.afinidades;
        }

        @Override
        public float affinityShift() {
            return this.puxa;
        }

        @Override
        public float manaCost() {
            return this.mana;
        }

        @Override
        public float burnout() {
            return burnoutFromMana(this.mana);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            return nasce(level, feitiço, quem, alvo.position());
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return nasce(level, feitiço, quem, batida);
        }

        private boolean nasce(ServerLevel level, Spell feitiço, LivingEntity quem, Vec3 onde) {
            var perto = new net.minecraft.world.phys.AABB(
                    onde.x - PERTO, onde.y - PERTO, onde.z - PERTO,
                    onde.x + PERTO, onde.y + PERTO, onde.z + PERTO);
            for (var outra : level.getEntities(ArcanaEntities.SPELL_EFFECT, perto, e -> true)) {
                if (outra.kind() == this.qual) return false;
            }

            int raio = (int) feitiço.add(level, SpellModifierKind.RADIUS, this.raioBase);
            if (this.metade) raio = raio / 2 + 1;

            // ela não leva feitiço nenhum adiante: o que ela faz, faz por si
            var área = new SpellEffectEntity(level, quem, Spell.of(Shapes.SELF), this.qual);
            área.setWeather(this.qual);
            área.setRadius(raio);
            área.setDamageBonus((float) feitiço.mul(level, SpellModifierKind.DAMAGE, 1.0));
            área.setLife((int) feitiço.mul(level, SpellModifierKind.DURATION, BASE_TEMPO));
            área.snapTo(onde);
            level.addFreshEntity(área);
            return true;
        }
    }

    /**
     * <b>Nevasca</b>: um temporal de gelo parado num lugar.
     *
     * <p>Fere <b>um</b> de gelo por batida e prende quem estiver dentro com o Gelado no terceiro grau — que é
     * o mais forte que ele tem. E vai deixando <b>neve no chão</b>, duas batidas em cada dez.
     *
     * <p>1200 de mana, e é uma das dez perícias prateadas.
     */
    public static final SpellPart.Essence BLIZZARD = SpellParts.essence(new Temporal("blizzard",
            SpellEffectEntity.Kind.BLIZZARD, 1200.0f, java.util.Set.of(Affinity.ICE), 0.1f, 2, false));

    /**
     * <b>Chuva de Fogo</b>: a irmã da Nevasca.
     *
     * <p>Fere <b>três quartos</b> por batida em vez de um, e vai pondo <b>fogo no chão</b> em vez de neve. E o
     * raio dela é outro: o original soma o Raio ao dois e depois <b>divide por dois e soma um</b> — uma Chuva
     * de Fogo é sempre mais apertada do que uma Nevasca com os mesmos modificadores.
     *
     * <p><b>Três mil</b> de mana, e também é prateada.
     */
    public static final SpellPart.Essence FIRE_RAIN = SpellParts.essence(new Temporal("fire_rain",
            SpellEffectEntity.Kind.FIRE_RAIN, 3000.0f, java.util.Set.of(Affinity.FIRE), 0.1f, 2, true));

    /**
     * <b>Estrela Cadente</b>: chama uma do teto do mundo.
     *
     * <p>Ela cai no lugar que o feitiço marcou e, ao chegar, fere <b>tudo o que estiver a cinco blocos</b> e
     * tiver linha de vista para ela. O dano é <b>dois vezes quinze</b> — trinta —, e o modificador de Dano
     * multiplica os quinze antes de os dobrar, que é a conta do original e é por isso que ela cresce depressa.
     *
     * <p>E recusa se já houver uma a dez blocos: uma estrela de cada vez.
     *
     * <p>É uma das dez perícias <b>prateadas</b> do original.
     */
    public static final SpellPart.Essence FALLING_STAR = SpellParts.essence(new SpellPart.Essence() {
        /** O quinze do original, que o Dano multiplica — e que depois é dobrado. */
        public static final int BASE = 15;
        public static final double PERTO = 10.0;

        /** E a cinquenta blocos acima do ponto: o {@code impactY + 50.0} do original. */
        public static final double ACIMA = 50.0;
        public static final float MANA = 400.0f;

        @Override
        public String name() {
            return "falling_star";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ARCANE);
        }

        @Override
        public float affinityShift() {
            return 0.05f;
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
            return chama(level, feitiço, quem, alvo.position());
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return chama(level, feitiço, quem, batida);
        }

        private boolean chama(ServerLevel level, Spell feitiço, LivingEntity quem, Vec3 onde) {
            // o original procura uma estrela já chamada a dez blocos de (x, onde.y + 50, z) — e a estrela
            // nasce no teto do mundo. Quer dizer que a guarda só pega quando se chama uma perto do teto;
            // ao nível do chão ela nunca acha nada, e uma segunda estrela sai. É assim no original.
            var perto = new net.minecraft.world.phys.AABB(
                    onde.x, onde.y + ACIMA, onde.z, onde.x, onde.y + ACIMA, onde.z).inflate(PERTO);
            if (!level.getEntities(ArcanaEntities.SHOOTING_STAR, perto, e -> true).isEmpty()) return false;

            int força = (int) feitiço.mul(level, SpellModifierKind.DAMAGE, BASE);
            var estrela = new ShootingStarEntity(level, quem, 2.0f * força);
            // ela nasce no teto do mundo, por cima do lugar marcado
            estrela.snapTo(onde.x, level.getMaxY(), onde.z);
            level.addFreshEntity(estrela);
            return true;
        }
    });

    /**
     * <b>Elo de Mana</b>: põe a sua mana ao alcance de outra pessoa.
     *
     * <p>Lê-se mal e é o que o original faz: quem ganha o elo é <b>quem leva o feitiço</b>. Lançar o Elo em
     * alguém é <b>dar-lhe</b> a sua mana, e não tomar a dele — e só enquanto estiverem perto.
     *
     * <p>Não custa mana nenhuma, e <b>alterna</b>: lançado outra vez no mesmo, desfaz o elo. E puxa <b>um
     * quarto</b> de Afinidade por vez, repartido por três — o Raio, o Fim e o Arcano —, que é a puxada mais
     * forte de todo o ramo.
     *
     * <p>É uma das dez perícias prateadas.
     */
    public static final SpellPart.Essence MANA_LINK = SpellParts.essence(new SpellPart.Essence() {
        @Override
        public String name() {
            return "mana_link";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.LIGHTNING, Affinity.ENDER, Affinity.ARCANE);
        }

        @Override
        public float affinityShift() {
            return 0.25f;
        }

        @Override
        public float manaCost() {
            return 0.0f;
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (!(alvo instanceof net.minecraft.world.entity.player.Player levou)) return false;
            if (!(quem instanceof net.minecraft.world.entity.player.Player lançou)) return false;

            ManaLinks.set(levou, ManaLinks.of(levou).alterna(lançou.getUUID()));
            return true;
        }
    });

    // ------------------------------------------------------------------ as duas que aprendem no mundo

    /**
     * O feitiço que está na mão de quem lança, para as essências que <b>escrevem nele</b>.
     *
     * <p>Duas delas fazem isso — o Colocar Bloco e a Apropriação —, e é o {@code getOriginalSpellStack} do
     * original: elas não mudam o mundo com o que sabem, elas <b>aprendem</b> e guardam o que aprenderam no
     * próprio item. Um feitiço desses na mochila de outra pessoa continua sabendo o que aprendeu.
     */
    private static @Nullable ItemStack naMão(LivingEntity quem) {
        if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return null;
        for (net.minecraft.world.InteractionHand mão : net.minecraft.world.InteractionHand.values()) {
            ItemStack coisa = gente.getItemInHand(mão);
            if (coisa.getItem() instanceof SpellItem) return coisa;
        }
        return null;
    }

    /**
     * <b>Colocar Bloco</b>: põe no mundo o bloco que o feitiço aprendeu.
     *
     * <p>E ele aprende <b>agachado</b>: lançar o feitiço agachado contra um bloco lhe ensina aquele bloco;
     * lançá-lo de pé põe um igual onde se apontar. É o original inteiro, e é a única peça do ramo que muda de
     * trabalho conforme a pessoa está agachada ou não.
     *
     * <p><b>E ele gasta o bloco da mochila</b> — quem não o tiver não põe nada. Em criativo põe sempre, como
     * tudo o resto.
     */
    public static final SpellPart.Essence PLACE_BLOCK = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 5.0f;

        @Override
        public String name() {
            return "place_block";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.EARTH, Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.05f;
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
            ItemStack coisa = naMão(quem);
            if (coisa == null) return false;
            if (!(quem instanceof net.minecraft.world.entity.player.Player gente)) return false;

            // agachado, ele aprende
            if (quem.isShiftKeyDown()) {
                var feitio = level.getBlockState(onde);
                if (feitio.isAir()) return false;
                coisa.set(ArcanaComponents.PLACE_BLOCK, feitio);
                return true;
            }

            var guardado = coisa.get(ArcanaComponents.PLACE_BLOCK);
            if (guardado == null) return false;

            BlockPos lugar = level.getBlockState(onde).isSolidRender() ? onde.relative(face) : onde;
            if (!level.getBlockState(lugar).canBeReplaced()) return false;

            // e gasta um da mochila, se não for criativo
            ItemStack custa = new ItemStack(guardado.getBlock());
            if (!gente.hasInfiniteMaterials()) {
                int qual = gente.getInventory().findSlotMatchingItem(custa);
                if (qual < 0) return false;
                gente.getInventory().removeItem(qual, 1);
            }

            level.setBlockAndUpdate(lugar, guardado);
            return true;
        }
    });

    /**
     * <b>Apropriação</b>: tira uma coisa do mundo e a leva consigo.
     *
     * <p>Um bloco <b>com o que ele tem dentro</b> — um baú apropriado volta com as coisas lá — ou um bicho
     * inteiro, com a vida e o nome que tinha. Enquanto estiver guardado, aquilo <b>não existe</b> em lugar
     * nenhum senão no feitiço; lançá-lo outra vez põe de volta.
     *
     * <p>Leva <b>uma coisa de cada vez</b>: com alguma coisa dentro, o feitiço só sabe devolvê-la.
     *
     * <p>O original recusa gente e chefes, e isto também — um feitiço que guardasse uma pessoa seria outra
     * coisa, e não esta.
     */
    public static final SpellPart.Essence APPROPRIATION = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 415.0f;

        @Override
        public String name() {
            return "appropriation";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.WATER);
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
            ItemStack coisa = naMão(quem);
            if (coisa == null) return false;

            var guardado = coisa.get(ArcanaComponents.APPROPRIATED);
            if (guardado != null) return devolve(level, coisa, guardado, onde, face, batida);

            var feitio = level.getBlockState(onde);
            if (feitio.isAir()) return false;
            if (feitio.getDestroySpeed(level, onde) < 0.0f) return false;

            var dentro = java.util.Optional.<net.minecraft.nbt.CompoundTag>empty();
            var bloco = level.getBlockEntity(onde);
            if (bloco != null) {
                dentro = java.util.Optional.of(bloco.saveWithoutMetadata(level.registryAccess()));
                level.removeBlockEntity(onde);
            }
            coisa.set(ArcanaComponents.APPROPRIATED, Appropriated.of(feitio, dentro));
            level.removeBlock(onde, false);
            return true;
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            ItemStack coisa = naMão(quem);
            if (coisa == null) return false;

            var guardado = coisa.get(ArcanaComponents.APPROPRIATED);
            if (guardado != null) {
                return devolve(level, coisa, guardado,
                        alvo.blockPosition(), Direction.UP, alvo.position());
            }

            if (alvo instanceof net.minecraft.world.entity.player.Player) return false;
            if (!(alvo instanceof LivingEntity)) return false;

            net.minecraft.nbt.CompoundTag escrito;
            try (var escopo = new net.minecraft.util.ProblemReporter.ScopedCollector(
                    net.thaumcraft.Thaumcraft.LOGGER)) {
                var saída = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                        escopo, level.registryAccess());
                if (!alvo.save(saída)) return false;
                escrito = saída.buildResult();
            }

            coisa.set(ArcanaComponents.APPROPRIATED, Appropriated.of(escrito));
            alvo.discard();
            return true;
        }

        /** E o caminho de volta: o que estava guardado volta ao mundo, e o feitiço esvazia. */
        private boolean devolve(ServerLevel level, ItemStack coisa, Appropriated guardado,
                                BlockPos onde, Direction face, Vec3 batida) {
            if (guardado.bicho().isPresent()) {
                var bicho = net.minecraft.world.entity.EntityType.loadEntityRecursive(
                        guardado.bicho().get(), level,
                        new net.minecraft.world.entity.EntitySpawnRequest(
                                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED, true),
                        e -> {
                            e.snapTo(batida.x, batida.y, batida.z, e.getYRot(), e.getXRot());
                            return e;
                        });
                if (bicho == null) return false;
                level.addFreshEntity(bicho);
                coisa.remove(ArcanaComponents.APPROPRIATED);
                return true;
            }

            if (guardado.bloco().isEmpty()) return false;
            BlockPos lugar = level.getBlockState(onde).isSolidRender() ? onde.relative(face) : onde;
            if (!level.getBlockState(lugar).canBeReplaced()) return false;

            level.setBlockAndUpdate(lugar, guardado.bloco().get());
            if (guardado.dentro().isPresent() && level.getBlockEntity(lugar) != null) {
                level.getBlockEntity(lugar).loadWithComponents(
                        net.minecraft.world.level.storage.TagValueInput.create(
                                net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(),
                                guardado.dentro().get()));
            }
            coisa.remove(ArcanaComponents.APPROPRIATED);
            return true;
        }
    });

    // ------------------------------------------------------------------ a que chama alguém

    /**
     * <b>Invocação</b>: chama uma criatura, e ela briga por quem a chamou.
     *
     * <p><b>O resto da etapa cai nela.</b> É a coisa mais bonita desta peça, e é do original: as outras
     * essências da mesma etapa não vão ao mundo, vão <b>à criatura invocada</b>. Uma Invocação com Pressa
     * chama um esqueleto rápido; com Escudo, um esqueleto duro. É assim que se melhora o que se chama.
     *
     * <p>E por isso ela <b>recusa quem já é invocado</b>: sem essa recusa, a Invocação chamaria a si mesma em
     * cadeia quando o resto da etapa caísse sobre o que ela acabou de chamar. É a mesma guarda do original.
     *
     * <p><b>Esqueleto</b>, e com arco na mão — é o padrão do original, e aqui é o único. No Ars Magica 2 quem
     * escolhe outro bicho é o <b>Filactério de Cristal</b>, que se enche no <b>Invocador</b>: um bloco da rede
     * de energia do mod, que é a metade que este porte não trouxe. Declarado no {@code PORTE.md}.
     *
     * <p>Quatro minutos de prazo, que o modificador de Duração multiplica, e <b>uma de cada vez</b>.
     */
    public static final SpellPart.Essence SUMMON = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 400.0f;
        public static final float DESGASTE = 120.0f;

        @Override
        public String name() {
            return "summon";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER, Affinity.LIFE);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return DESGASTE;
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return chama(level, feitiço, quem, batida);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            // nunca em cima de uma invocação: é o que impede a cadeia
            if (Summons.éInvocado(alvo)) return false;
            return chama(level, feitiço, quem, alvo.position());
        }

        /**
         * Chama uma, se couber.
         *
         * <p><b>Cheio, ela dá certo de qualquer jeito</b> — e avisa. É do original, e é fácil de ler ao avesso:
         * o {@code applyEffectBlock} dele só devolve {@code false} quando a criatura <b>não nasceu</b>; com o
         * teto cheio ele manda a frase e devolve {@code true}, ou seja <b>a mana se gasta</b>. É um castigo por
         * lançar sem olhar, e é de propósito.
         */
        private boolean chama(ServerLevel level, Spell feitiço, LivingEntity quem, Vec3 onde) {
            return Essences.invoca(level, feitiço, quem, onde, Necromancy.Qual.ESQUELETO);
        }
    });

    /**
     * <b>Erguer os Mortos</b>: a mesma chamada, mas quem vem é um <b>zumbi com espada</b>.
     *
     * <p><b>Isto é acréscimo, e não porte.</b> No Ars Magica 2 quem escolhe a criatura é o <b>Filactério de
     * Cristal</b>, e enchê-lo exige o Invocador — um bloco da rede de energia que este porte não trouxe. Sem
     * ele, a Invocação do original é para sempre um esqueleto, e metade da ideia dela fica sem uso.
     *
     * <p>Esta peça devolve a <b>escolha</b>, que é o que se perdeu, sem devolver a máquina. E devolve só a
     * escolha: <b>nem um é melhor que o outro</b>. Custam o mesmo, duram o mesmo, ocupam a mesma vaga; um
     * atira de longe e o outro bate de perto, e é isso.
     *
     * <p>Afinidade só do <b>Fim</b>. A Invocação do original puxa Fim e Vida, e a Vida está lá porque o que ela
     * traz é uma criatura viva. Um morto que se ergue não é, e por isso aqui fica só o Fim. Declarado no
     * {@code PORTE.md}.
     */
    public static final SpellPart.Essence RAISE_DEAD = SpellParts.essence(new SpellPart.Essence() {
        public static final float MANA = 400.0f;
        public static final float DESGASTE = 120.0f;

        @Override
        public String name() {
            return "raise_dead";
        }

        @Override
        public java.util.Set<Affinity> affinities() {
            return java.util.Set.of(Affinity.ENDER);
        }

        @Override
        public float affinityShift() {
            return 0.01f;
        }

        @Override
        public float manaCost() {
            return MANA;
        }

        @Override
        public float burnout() {
            return DESGASTE;
        }

        @Override
        public boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                               Direction face, Vec3 batida) {
            return Essences.invoca(level, feitiço, quem, batida, Necromancy.Qual.ZUMBI);
        }

        @Override
        public boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            if (Summons.éInvocado(alvo)) return false;
            return Essences.invoca(level, feitiço, quem, alvo.position(), Necromancy.Qual.ZUMBI);
        }
    });

    /**
     * O caminho de uma invocação, seja ela qual for.
     *
     * <p>É o {@code summonCreature} do original, com as três coisas do necromante enxertadas e declaradas: o
     * <b>teto</b> que a Legião sobe, a <b>panóplia</b> que a Afinidade dá, e a <b>montaria</b>. Tirando essas
     * três, o que está aqui é o dele, linha por linha.
     */
    private static boolean invoca(ServerLevel level, Spell feitiço, LivingEntity quem, Vec3 onde,
                                  Necromancy.Qual qual) {
        // o teto do original, mais uma vaga por Legião — e o aviso é o dele
        int teto = Summons.TETO + feitiço.count(SpellModifierKind.SUMMON_COUNT);
        if (!Summons.cabeMais(level, quem, teto)) {
            if (quem instanceof net.minecraft.world.entity.player.Player gente) {
                gente.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("message.thaumcraft.no_more_summons"));
            }
            return true;
        }

        var bicho = qual.tipo().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (bicho == null) return false;

        bicho.snapTo(onde.x, onde.y, onde.z, quem.getYRot(), 0.0f);
        bicho.finalizeSpawn(level, level.getCurrentDifficultyAt(bicho.blockPosition()),
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED, null);
        // a arma vem sempre — é ela que diz o que cada um é —, e a armadura vem da Afinidade
        Necromancy.veste(bicho, qual, Necromancy.panóplia(quem));

        int prazo = (int) feitiço.mul(level, SpellModifierKind.DURATION, Summons.PRAZO);
        Summons.marca(level, bicho, quem, prazo);
        level.addFreshEntity(bicho);

        if (Necromancy.temMontaria(quem)) Necromancy.monta(level, bicho, quem, prazo);

        // e o resto da etapa cai NELA, que é o applyStageToEntity do original
        SpellCast.onEntity(level, feitiço, quem, bicho);
        return true;
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela as Essências a se registrarem. */
    public static void init() {
    }

    /** As que existem, para quem precisar de contar. */
    public static Set<SpellPart.Essence> all() {
        return Set.copyOf(SpellParts.essences());
    }
}
