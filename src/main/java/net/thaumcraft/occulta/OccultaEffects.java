package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * As poções do ofício: os {@code PotionBase} do Witchery.
 *
 * <p>O original tem quase sessenta delas. Esta é a <b>primeira leva</b> — as que se bastam a si mesmas e não
 * pedem nada que o porte ainda não tenha (lobisomem, vampiro, familiar, círculo de giz). As outras vêm depois.
 *
 * <p>Três delas o leite não tira, como no original: a Máscara de Gás, a Barriga Forte e as duas alergias. Quem
 * faz isso é o {@link net.thaumcraft.research.Incurable}, que este mod já tinha para os efeitos da dobra.
 */
public final class OccultaEffects {
    /**
     * Nadar: o {@code PotionSwimming}.
     *
     * <p>Dentro da água, quem a tem anda mais depressa — quinze por cento mais, e mais três por cento por grau.
     *
     * <p><b>Desvio declarado:</b> no original isto é feito no lado de quem joga, olhando se a tecla de andar está
     * apertada; aqui é do lado do servidor e vale para qualquer um que esteja nadando, porque é o único lugar de
     * onde se pode empurrar um bicho sem depender do teclado de ninguém.
     */
    public static final Holder<MobEffect> SWIMMING = register("swimming",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x2E5FD8) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (!quem.isInWater()) return true;
                    double quanto = 1.15 + 0.03 * grau;
                    Vec3 anda = quem.getDeltaMovement();
                    quem.setDeltaMovement(anda.x * quanto, anda.y, anda.z * quanto);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return true;
                }
            });

    /**
     * Queda de Pena: o {@code PotionFeatherFall}.
     *
     * <p>Passada a distância em que ela acorda, a queda para de acelerar — e o tombo que se leva no chão conta
     * como se fosse de poucos blocos. Quanto maior o grau, mais cedo ela acorda e menos se sente.
     */
    public static final Holder<MobEffect> FEATHER_FALL = register("feather_fall",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xEFEFEF) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    int acorda = grau >= 2 ? 3 : (grau >= 1 ? 4 : 5);
                    int tombo = grau >= 3 ? 3 : (grau >= 2 ? 4 : (grau >= 1 ? 5 : 6));
                    if (quem.fallDistance < acorda || quem.getDeltaMovement().y >= -0.2) return true;
                    Vec3 cai = quem.getDeltaMovement();
                    quem.setDeltaMovement(cai.x, -0.2, cai.z);
                    if (quem.fallDistance > tombo) quem.fallDistance = tombo;
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return true;
                }
            });

    /**
     * Flutuar: o {@code PotionFloating}.
     *
     * <p>Quem a tem não cai: enquanto houver chão a três blocos (mais um por grau) debaixo dele, sobe; passando
     * disso, para no ar e desce de vez em quando. E nunca se machuca de queda.
     */
    public static final Holder<MobEffect> FLOATING = register("floating",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xB0E0E6) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    int altura = 3 + grau;
                    quem.fallDistance = 0.0f;
                    BlockPos onde = quem.blockPosition();
                    Vec3 anda = quem.getDeltaMovement();
                    for (int i = 1; i <= altura; i++) {
                        if (level.isEmptyBlock(onde.below(i))) continue;
                        quem.setDeltaMovement(anda.x, 0.25, anda.z);
                        return true;
                    }
                    quem.setDeltaMovement(anda.x, level.getRandom().nextInt(5) == 0 ? -0.05 : 0.0, anda.z);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return true;
                }
            });

    /**
     * Máscara de Gás: o {@code PotionGasMask}.
     *
     * <p>Não faz nada sozinha — ela só existe para que uma <b>névoa</b> de cozimento ruim não pegue em quem a
     * tem. E o leite não a tira.
     */
    public static final Holder<MobEffect> GAS_MASK = register("gas_mask",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x7C7C7C) {
            });

    /**
     * Barriga Forte: o {@code PotionStoutBelly}.
     *
     * <p>Do segundo grau para cima, tira a fome de quem a tem, de segundo em segundo. O leite não a tira.
     */
    public static final Holder<MobEffect> STOUT_BELLY = register("stout_belly",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xA0724A) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (grau > 0 && quem.hasEffect(MobEffects.HUNGER)) quem.removeEffect(MobEffects.HUNGER);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return duração % 20 == 3;
                }
            });

    /**
     * Alergia ao Sol: o {@code PotionSunAllergy}.
     *
     * <p>De dia, a céu aberto, queima. Quem joga perde vida (um, ou dois do quarto grau em diante); os bichos
     * pegam fogo, como um morto-vivo qualquer. O leite não a tira.
     */
    public static final Holder<MobEffect> SUN_ALLERGY = register("sun_allergy",
            new MobEffect(MobEffectCategory.HARMFUL, 0xFFD24A) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (!level.isBrightOutside() || quem.isInWater()) return true;
                    float luz = quem.getLightLevelDependentMagicValue();
                    if (luz <= 0.5f || level.getRandom().nextFloat() >= luz - 0.45f) return true;
                    if (!level.canSeeSky(quem.blockPosition())) return true;
                    if (quem instanceof Player) {
                        quem.hurtServer(level, level.damageSources().onFire(), grau >= 3 ? 2.0f : 1.0f);
                    } else {
                        quem.igniteForSeconds(8.0f);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return duração % 20 == 0;
                }
            });

    /**
     * Alergia ao Escuro: o {@code PotionDarknessAllergy}.
     *
     * <p>O contrário da outra: no escuro, dói. A conta da luz é a do original — menos de dois, mais dois por
     * grau. O leite não a tira.
     */
    public static final Holder<MobEffect> DARKNESS_ALLERGY = register("darkness_allergy",
            new MobEffect(MobEffectCategory.HARMFUL, 0x2A2438) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (level.getMaxLocalRawBrightness(quem.blockPosition()) < 2 + grau * 2) {
                        quem.hurtServer(level, level.damageSources().magic(), 1.0f);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return duração % 20 == 4;
                }
            });

    /**
     * Espinhos: o {@code PotionSpiked}.
     *
     * <p>Quem a tem fere quem se encostar nele — de cinco em cinco batidas, um de dano mais um por grau. É o
     * cacto passado para a pele.
     */
    public static final Holder<MobEffect> SPIKED = register("spiked",
            new MobEffect(MobEffectCategory.HARMFUL, 0x4C7F32) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    double largura = 0.2 + 0.1 * grau;
                    for (LivingEntity outro : level.getEntitiesOfClass(LivingEntity.class,
                            quem.getBoundingBox().inflate(largura, 0.0, largura))) {
                        if (outro == quem) continue;
                        outro.hurtServer(level, level.damageSources().cactus(), 1 + grau);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return duração % 5 == 3;
                }
            });

    /**
     * Envenenar Armas: o {@code PotionPoisonWeapons}.
     *
     * <p>Não faz nada em quem a tem: faz no que ele <b>acerta</b>. Do primeiro ao terceiro grau o golpe envenena,
     * cada vez mais; do quarto em diante, apodrece. Quem trata disso é o {@link OccultaEvents}.
     */
    public static final Holder<MobEffect> POISON_WEAPONS = register("poison_weapons",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x4E9331) {
            });

    /**
     * Volatilidade: o {@code PotionVolatility}.
     *
     * <p>Quem a tem estoura quando apanha — e o estouro cresce com o grau, até três. De vez em quando a própria
     * volatilidade se gasta no estouro. Não vale para queda, fogo, afogamento e fome: só para pancada.
     */
    public static final Holder<MobEffect> VOLATILITY = register("volatility",
            new MobEffect(MobEffectCategory.HARMFUL, 0xC23B22) {
            });

    /**
     * Refletir Projéteis: o {@code PotionReflectProjectiles}.
     *
     * <p>O que voa perto de quem a tem volta por onde veio, a um quarto da velocidade vezes o grau.
     */
    public static final Holder<MobEffect> REFLECT_PROJECTILES = register("reflect_projectiles",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xD8D8F0) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    for (var voando : level.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,
                            quem.getBoundingBox().inflate(2.0))) {
                        if (voando.getOwner() == quem) continue;
                        Vec3 vai = voando.getDeltaMovement();
                        double quanto = -0.25 * (1.0 + grau);
                        voando.setDeltaMovement(vai.x * quanto,
                                vai.x > 0.0 || vai.z > 0.0 ? vai.y * quanto : vai.y, vai.z * quanto);
                        voando.hurtMarked = true;
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return true;
                }
            });

    /**
     * Atrair Projéteis: o {@code PotionAttractProjectiles}, que é o contrário do de cima.
     *
     * <p>O que voa a três blocos (mais três por grau) se vira para quem a tem. É o que o cozimento invertido dá.
     */
    public static final Holder<MobEffect> ATTRACT_PROJECTILES = register("attract_projectiles",
            new MobEffect(MobEffectCategory.HARMFUL, 0x6B4F8A) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    double alcance = (1.0 + grau) * 3.0;
                    for (var voando : level.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,
                            quem.getBoundingBox().inflate(alcance))) {
                        Vec3 vai = voando.getDeltaMovement();
                        boolean depressa = vai.lengthSqr() > 0.25;
                        if (voando.tickCount < (depressa ? 1 : 10)) continue;
                        double dx = quem.getX() - voando.getX();
                        double dy = quem.getBoundingBox().minY + quem.getBbHeight() * 0.75 - voando.getY();
                        double dz = quem.getZ() - voando.getZ();
                        if (Math.sqrt(dx * dx + dz * dz) < 1.0e-7) continue;
                        voando.shoot(dx, dy, dz, 1.0f, 1.0f);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duração, int grau) {
                    return true;
                }
            });

    private OccultaEffects() {
    }

    /**
     * As Profundezas: o {@code witcheryDepths} do {@code Infusion} do original.
     *
     * <p>Quem bebe o Cozimento das Profundezas <b>respira debaixo da água</b> — e, fora dela, definha. É a troca
     * do peixe: o mar passa a ser casa, e a terra deixa de ser.
     */
    public static final Holder<MobEffect> DEPTHS = register("depths",
            new MobEffect(MobEffectCategory.NEUTRAL, 0x1B4F72) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (!quem.hasEffect(MobEffects.WATER_BREATHING)) {
                        quem.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.WATER_BREATHING, 6000, 0));
                    }
                    if (quem.isInWater()) {
                        quem.removeEffect(MobEffects.WITHER);
                    } else if (!quem.hasEffect(MobEffects.WITHER)) {
                        quem.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.WITHER, 100, 1));
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return true;
                }
            });

    private static Holder<MobEffect> register(String nome, MobEffect efeito) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thaumcraft.id(nome), efeito);
    }

    /** Os que o leite não tira, como no original. */
    public static boolean incurable(Holder<MobEffect> qual) {
        return qual == GAS_MASK || qual == STOUT_BELLY || qual == SUN_ALLERGY || qual == DARKNESS_ALLERGY;
    }

    public static void init() {
    }

    /** A luz do lugar, para quem precisar dela sem ter o nível à mão. */
    public static int light(ServerLevel level, BlockPos onde) {
        return level.getBrightness(LightLayer.BLOCK, onde);
    }
}
