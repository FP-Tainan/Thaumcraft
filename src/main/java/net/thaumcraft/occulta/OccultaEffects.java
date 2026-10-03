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
     * O <b>Acônito</b>: o {@code Potions.WOLFSBANE} do Witchery.
     *
     * <p>Ele não faz nada por si — nada de velocidade, nada de dano. O que ele faz é <b>segurar a
     * transformação</b>: quem o tem no corpo não vira lobisomem na lua cheia, e um lobisomem que o apanhe não
     * volta a ser aldeão enquanto durar.
     *
     * <p>É um efeito que só existe para ser <b>perguntado</b>, e é a melhor razão que a planta do mato tem
     * para ser plantada ao pé de uma aldeia.
     */
    public static final Holder<MobEffect> WOLFSBANE = register("wolfsbane",
            new MobEffect(MobEffectCategory.NEUTRAL, 0x6B4FA8) {
            });

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

    /**
     * O <b>Escravizado</b>: o {@code Potions.ENSLAVED} do Witchery.
     *
     * <p>Ele não faz nada por si, e é infinito. O que ele é está em
     * {@link net.thaumcraft.occulta.enslave.Enslavement} — este efeito é só a marca que diz que o laço está
     * posto, e a batida que mantém a vontade de brigar as brigas do dono na lista de alvos do bicho.
     */
    public static final Holder<MobEffect> ENSLAVED = register("enslaved",
            new MobEffect(MobEffectCategory.NEUTRAL, 0x4A3728) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    net.thaumcraft.occulta.enslave.Enslavement.tick(level, quem);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return true;
                }
            });

    /**
     * A <b>Inibição do Alhures</b>: o {@code Potions.ENDER_INHIBITION} do Witchery.
     *
     * <p>Quem a tem <b>não vai a lugar nenhum por magia</b>. É o que segura uma Pedra de Caminho largada num
     * círculo: o que estiver inibido fica onde está, e sai fumo dele em vez de portal.
     *
     * <p><b>Fica de fora, declarado:</b> no original ela cancela o {@code EnderTeleportEvent}, e por isso
     * segura também o salto do enderman e a pérola do Alhures. Aqui ela segura o que o porte sabe mandar — o
     * teleporte da Pedra de Caminho. O salto do enderman e a pérola ficam para quando houver um lugar só deles
     * de onde perguntar. Está no {@code PORTE.md}.
     */
    public static final Holder<MobEffect> ENDER_INHIBITION = register("ender_inhibition",
            new MobEffect(MobEffectCategory.HARMFUL, 0x2E1A47) {
            });

    /** Se este bicho está inibido neste grau ou acima: o {@code PotionEnderInhibition.isActive}. */
    public static boolean inibido(net.minecraft.world.entity.Entity quem, int grau) {
        if (!(quem instanceof LivingEntity vivo)) return false;
        var tem = vivo.getEffect(ENDER_INHIBITION);
        return tem != null && tem.getAmplifier() >= grau;
    }

    // ------------------------------------------------------------------ a segunda leva

    /**
     * <b>Enregelado</b>: o {@code PotionChilled}.
     *
     * <p>Anda um décimo mais devagar por grau, e o <b>fogo lhe dói menos</b> — um a menos por grau, e do
     * terceiro grau em diante não dói nada. Em troca, de vinte e cinco em vinte e cinco batidas (e cada vez
     * mais depressa com o grau) ele <b>gela por dentro</b>: um de dano, mas só num <b>blaze</b> — ou em
     * qualquer um, do terceiro grau em diante.
     *
     * <p>É a poção de quem vai ao Nether, e é a única coisa do ofício que torna um blaze um problema de quem
     * o <b>tem</b>, e não de quem o enfrenta.
     */
    public static final Holder<MobEffect> CHILLED = register("chilled",
            new MobEffect(MobEffectCategory.NEUTRAL, 0x9AD5E8) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (quem instanceof net.minecraft.world.entity.monster.Blaze || grau >= 2) {
                        quem.hurtServer(level, level.damageSources().magic(), 1.0f);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    int de = 25 >> grau;
                    return de <= 0 || restante % de == 0;
                }
            }.addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                    Thaumcraft.id("chilled"), -0.1,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /**
     * <b>Aura Infernal</b>: o {@code PotionHellishAura}.
     *
     * <p>Quem a tem não arde — <b>quem está perto dele é que arde</b>. De vinte e cinco em vinte e cinco
     * batidas, tudo o que estiver a um bloco e meio leva um de fogo, e do segundo grau em diante pega fogo de
     * verdade.
     *
     * <p>É a poção que torna estar ao pé de alguém uma coisa perigosa, e por isso é a pior de todas para se
     * dar a um aliado.
     */
    public static final Holder<MobEffect> HELLISH_AURA = register("hellish_aura",
            new MobEffect(MobEffectCategory.NEUTRAL, 0xE2571E) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    for (LivingEntity outro : level.getEntitiesOfClass(LivingEntity.class,
                            quem.getBoundingBox().inflate(1.5, 0.0, 1.5))) {
                        if (outro == quem) continue;
                        outro.hurtServer(level, level.damageSources().inFire(), 1.0f);
                        if (grau > 0) outro.igniteForSeconds(grau);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    int de = 25 >> grau;
                    return de <= 0 || restante % de == 0;
                }
            });

    /**
     * <b>Rasto de Neve</b>: o {@code PotionSnowTrail}.
     *
     * <p>Onde ele pisa fica neve — nas quatro casas debaixo dele, de dez em dez batidas, e só onde o clima
     * deixa. Não faz mal a ninguém: é o jeito do ofício de dizer que passou por ali.
     *
     * <p><b>Fica de fora, declarado:</b> no original, um <b>golem de neve</b> com esta poção <b>estoura</b>,
     * uma vez em vinte, e cobre de neve oito blocos em volta. Fica para quando houver razão: o estouro é uma
     * coisa à parte do rasto, e o rasto é o que a poção é.
     */
    public static final Holder<MobEffect> SNOW_TRAIL = register("snow_trail",
            new MobEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    for (int canto = 0; canto < 4; canto++) {
                        int x = net.minecraft.util.Mth.floor(quem.getX() + (canto % 2 * 2 - 1) * 0.25f);
                        int y = net.minecraft.util.Mth.floor(quem.getY());
                        int z = net.minecraft.util.Mth.floor(quem.getZ() + (canto / 2 % 2 * 2 - 1) * 0.25f);
                        BlockPos onde = new BlockPos(x, y, z);
                        if (!level.getBlockState(onde).isAir()) continue;
                        var neve = net.minecraft.world.level.block.Blocks.SNOW.defaultBlockState();
                        if (!neve.canSurvive(level, onde)) continue;
                        if (level.getBiome(onde).value().coldEnoughToSnow(onde, level.getSeaLevel())) {
                            level.setBlockAndUpdate(onde, neve);
                        }
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return restante % 10 == 0;
                }
            });

    /**
     * <b>Corda Mortal</b>: o {@code PotionMortalCoil}.
     *
     * <p>Ela não faz nada, nada, nada — e então acaba, e quem a tem <b>morre</b>. É um relógio, e é a coisa
     * mais honesta do mod: o que ela faz está no nome e na duração, e mais nada.
     *
     * <p>É o que o <b>Cozimento da Ressurreição ritualizado</b> põe nos mortos que levanta: eles servem dez
     * segundos por grau e depois caem outra vez. Um exército emprestado.
     */
    public static final Holder<MobEffect> MORTAL_COIL = register("mortal_coil",
            new MobEffect(MobEffectCategory.HARMFUL, 0x000000) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    quem.hurtServer(level, level.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                    return false;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return restante == 1;
                }
            });

    /**
     * <b>Enjoado</b>: o {@code PotionQueasy}.
     *
     * <p>Não faz nada por si, e o leite não a tira. É uma <b>marca</b>, e o que ela marca é que o estômago
     * está ocupado: quem a tem não aproveita outra poção bebida.
     */
    public static final Holder<MobEffect> QUEASY = register("queasy",
            new MobEffect(MobEffectCategory.HARMFUL, 0x7E9B5B) {
            });

    /**
     * <b>Perícia de Cozimento</b>: o {@code PotionBrewingExpertise}.
     *
     * <p>Outra marca, e das boas: quem a tem coze melhor. Quem a lê é o caldeirão.
     */
    public static final Holder<MobEffect> BREWING_EXPERTISE = register("brewing_expertise",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x8B5A9E) {
            });

    /**
     * <b>Adoração</b>: o {@code PotionWorship}.
     *
     * <p>E a terceira marca: quem a tem é adorado pelos goblins. Fica posta porque o que a lê virá depois —
     * é a estátua de adoração deles, e os goblins ainda não estão portados.
     */
    public static final Holder<MobEffect> WORSHIP = register("worship",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xD4AF37) {
            });

    /**
     * <b>Mal Ajustada</b>: o {@code PotionIllFitting}.
     *
     * <p>A armadura de quem a tem <b>cai do corpo</b>. Não se gasta, não se quebra — cai, uma peça de cada
     * vez, e <b>só perto do fim</b>: a poção espera quinze batidas por grau e então começa a despir quem a
     * tem.
     *
     * <p>Ela é permanente e o leite não a tira, e é a razão por que um caçador de bruxas com o conjunto
     * inteiro tem medo de uma bruxa com um frasco.
     */
    public static final Holder<MobEffect> ILL_FITTING = register("ill_fitting",
            new MobEffect(MobEffectCategory.HARMFUL, 0x6E5B3A) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    var casas = new net.minecraft.world.entity.EquipmentSlot[]{
                            net.minecraft.world.entity.EquipmentSlot.FEET,
                            net.minecraft.world.entity.EquipmentSlot.LEGS,
                            net.minecraft.world.entity.EquipmentSlot.CHEST,
                            net.minecraft.world.entity.EquipmentSlot.HEAD};
                    var casa = casas[level.getRandom().nextInt(casas.length)];
                    var peça = quem.getItemBySlot(casa);
                    if (peça.isEmpty()) return true;

                    quem.setItemSlot(casa, net.minecraft.world.item.ItemStack.EMPTY);
                    quem.spawnAtLocation(level, peça);
                    level.playSound(null, quem.blockPosition(),
                            net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
                    return true;
                }

                /** O original só começa a despir perto do fim, e o quanto antes quanto maior o grau. */
                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    if (restante % 15 != 0) return false;
                    return switch (grau) {
                        case 1 -> restante <= 30;
                        case 2 -> restante <= 45;
                        case 3 -> restante <= 60;
                        default -> restante <= 15;
                    };
                }
            });

    /**
     * <b>Brotar</b>: o {@code PotionSprouting}.
     *
     * <p>De segundo em segundo, uma vez em quatro, <b>nasce um galho de quem a tem</b> — o mesmo galho do
     * Cozimento de Brotar, e com a mesma conta de tamanho.
     *
     * <p>É das poucas poções do mod que são bonitas de ver e difíceis de aguentar: o galho não fere ninguém,
     * mas tapa a saída.
     */
    public static final Holder<MobEffect> SPROUTING = register("sprouting",
            new MobEffect(MobEffectCategory.HARMFUL, 0x4C7F32) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (level.getRandom().nextInt(4) != 0) return true;
                    net.thaumcraft.occulta.kettle.KettleBrews.brota(level, quem.blockPosition(),
                            net.minecraft.core.Direction.UP, 1 + grau);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return restante % 20 == 9;
                }
            });

    /**
     * <b>Paralisia</b>: o {@code PotionParalysis}.
     *
     * <p>Quarenta a menos de velocidade — que é tanto que <b>ninguém anda</b>. É o que o vampiro faz à vítima
     * antes de beber, e o leite não a tira.
     *
     * <p><b>Fica de fora, declarado:</b> no original, num <b>aldeão</b> ela não paralisa — faz o contrário: o
     * aldeão larga o que estava a fazer e <b>anda até o vampiro</b>. Isso pede a vampirice de jogador, que
     * este porte ainda não tem.
     */
    public static final Holder<MobEffect> PARALYSIS = register("paralysis",
            new MobEffect(MobEffectCategory.HARMFUL, 0x4A4A6A) {
            }.addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                    Thaumcraft.id("paralysis"), -40.0,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /**
     * <b>Redimensionar</b>: o {@code PotionResizing}.
     *
     * <p>Quem a tem <b>muda de tamanho</b>. É a poção mais vistosa do mod, e em 2014 era também a mais cara
     * de escrever: o original mexe no tamanho da caixa por <b>reflexão</b>, método a método, uma vez por
     * feitio de bicho, porque o jogo de então não deixava mudar isso de fora.
     *
     * <p><b>Hoje o jogo tem um atributo para isso</b>, e por isso o que lá eram duzentas linhas aqui é uma:
     * a escala. O original <b>encolhe</b> nos graus pares e <b>aumenta</b> nos ímpares, e é o que se faz.
     */
    public static final Holder<MobEffect> RESIZING = register("resizing",
            new MobEffect(MobEffectCategory.NEUTRAL, 0xC9A0DC) {
                @Override
                public void addAttributeModifiers(net.minecraft.world.entity.ai.attributes.AttributeMap mapa,
                                                  int grau) {
                    super.addAttributeModifiers(mapa, grau);
                    var escala = mapa.getInstance(
                            net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
                    if (escala == null) return;
                    escala.addOrUpdateTransientModifier(
                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                    Thaumcraft.id("resizing"), quanto(grau),
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
                                            .ADD_MULTIPLIED_BASE));
                }

                @Override
                public void removeAttributeModifiers(
                        net.minecraft.world.entity.ai.attributes.AttributeMap mapa) {
                    super.removeAttributeModifiers(mapa);
                    var escala = mapa.getInstance(
                            net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
                    if (escala != null) escala.removeModifier(Thaumcraft.id("resizing"));
                }

                /** Par encolhe, ímpar aumenta, e cada grau vale um quarto. */
                private double quanto(int grau) {
                    double passo = 0.25 * (grau / 2 + 1);
                    return grau % 2 == 0 ? -passo : passo;
                }
            });

    /**
     * <b>Absorver Magia</b>: o {@code PotionAbsorbMagic}.
     *
     * <p>Um quinto por grau do dano <b>mágico</b> não chega — e, em gente, vira <b>mana</b>. É a poção que faz
     * de um duelo de magos uma coisa que o ofício ganha: quem a bebe sai de uma bola de fogo com o poço mais
     * cheio do que entrou.
     *
     * <p>O que ela faz está em {@link OccultaHurt}, junto das outras do golpe.
     */
    public static final Holder<MobEffect> ABSORB_MAGIC = register("absorb_magic",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x5B7FA8) {
            });

    /**
     * <b>Enrolado em Vinha</b>: o {@code PotionWrappedInVine}.
     *
     * <p>Não prende e não fere — <b>só faz o fogo doer até quatro vezes mais</b>. E o leite não a tira.
     *
     * <p>É a melhor armadilha do mod inteiro: ela não faz nada até alguém acender alguma coisa.
     */
    public static final Holder<MobEffect> WRAPPED_IN_VINE = register("wrapped_in_vine",
            new MobEffect(MobEffectCategory.HARMFUL, 0x3C6B2A) {
            });

    /**
     * <b>Refletir Dano</b>: o {@code PotionReflectDamage}.
     *
     * <p>Um décimo por grau volta para quem bateu — e <b>sai do que chegou</b>, que é o detalhe que a torna
     * boa: ela não é espinho, é espelho.
     */
    public static final Holder<MobEffect> REFLECT_DAMAGE = register("reflect_damage",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xB0C4DE) {
            });

    /**
     * <b>Repelir Agressor</b>: o {@code PotionRepellAttacker}.
     *
     * <p>Quem bate de perto <b>sai de perto</b>, e quanto maior o grau mais longe vai. Não tira dano nenhum —
     * compra tempo, que às vezes é mais.
     */
    public static final Holder<MobEffect> REPELL_ATTACKER = register("repell_attacker",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xA8D8E8) {
            });

    /**
     * <b>Não Sentir Dor</b>: o {@code PotionFeelNoPain}.
     *
     * <p>O golpe sai da <b>fome</b> antes de sair da vida. E tem um preço que o original cobra de propósito:
     * do segundo grau em diante, de segundo em segundo, quem a tem pode <b>ficar cego</b> por seis segundos e
     * mais dois por grau — porque não sentir dor não é a mesma coisa que estar bem.
     *
     * <p>O leite não a tira.
     */
    public static final Holder<MobEffect> FEEL_NO_PAIN = register("feel_no_pain",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xC8B9A6) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (grau <= 0) return true;
                    if (quem.hasEffect(MobEffects.BLINDNESS) || quem.hasEffect(STOUT_BELLY)) return true;
                    if (level.getRandom().nextInt(5 - Math.min(grau, 3)) != 0) return true;
                    quem.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            MobEffects.BLINDNESS, (6 + grau * 2) * 20));
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return restante % 20 == 2;
                }
            });

    /**
     * <b>Doente</b>: o {@code PotionDiseased}.
     *
     * <p>Quem a tem bate <b>mais fraco</b> — metade por grau — e vai <b>deixando a doença pelo chão</b>: de
     * dois em dois segundos, uma vez em três, nasce dela um bloco de doença onde ela pisa.
     *
     * <p>O leite não a tira, e é a poção mais cruel do mod: ela não mata, ela <b>espalha</b>.
     *
     * <p><b>Fica de fora, declarado:</b> o bloco de doença do original (o {@code Witchery.Blocks.DISEASE}) não
     * está portado, e por isso o que fica no chão é uma <b>teia</b> — o que há no jogo de hoje que mais se
     * parece com o que ele faz: prende quem passa. Quando o bloco vier, é esta linha que muda.
     */
    public static final Holder<MobEffect> DISEASED = register("diseased",
            new MobEffect(MobEffectCategory.HARMFUL, 0x6B8E3A) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (level.getRandom().nextInt(3) != 0) return true;
                    BlockPos onde = quem.blockPosition();
                    if (!level.getBlockState(onde).isAir()) return true;
                    if (!level.getBlockState(onde.below()).isSolidRender()) return true;
                    level.setBlockAndUpdate(onde,
                            net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState());
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return restante % 40 == 4;
                }
            }.addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    Thaumcraft.id("diseased"), -0.5,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /**
     * <b>Amor</b>: o {@code PotionLove}.
     *
     * <p>De segundo em segundo, quem a tem <b>quer namorar</b>: um bicho entra no cio, e um aldeão ou um
     * zumbi procura par.
     *
     * <p><b>Fica de fora, declarado:</b> o original põe uma vontade própria no zumbi e no aldeão para os
     * fazer procurar par <b>já</b>. Aqui o bicho entra no cio pelo caminho do jogo, e o aldeão e o zumbi ficam
     * com o que o jogo de hoje lhes dá — que para o aldeão já é querer namorar, e para o zumbi não é nada.
     */
    public static final Holder<MobEffect> LOVE = register("love",
            new MobEffect(MobEffectCategory.NEUTRAL, 0xFF6EC7) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity quem, int grau) {
                    if (!(quem instanceof net.minecraft.world.entity.animal.Animal bicho)) return true;
                    if (bicho.getAge() < 0 || bicho.isInLove()) return true;
                    bicho.setAge(0);
                    bicho.setInLove(null);
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int restante, int grau) {
                    return restante % 20 == 7;
                }
            });

    /**
     * <b>Reencarnar</b>: o {@code PotionReincarnate}.
     *
     * <p>Quem a tem <b>não morre sozinho</b>: do corpo levanta-se outra coisa, e o que se levanta depende do
     * que ele era e de quão forte era a poção.
     *
     * <ul>
     *   <li>De um <b>bicho</b> ou de uma <b>aranha</b> sai aranha, aranha-das-cavernas ou creeper;</li>
     *   <li>de tudo o mais sai zumbi, esqueleto ou blaze.</li>
     * </ul>
     *
     * <p>Quem trata disto é o {@link OccultaEvents}, que é quem vê a morte acontecer.
     */
    public static final Holder<MobEffect> REINCARNATE = register("reincarnate",
            new MobEffect(MobEffectCategory.NEUTRAL, 0x8B008B) {
            });

    /**
     * <b>Guardar o Que Se Tem</b>: o {@code PotionKeepInventory}.
     *
     * <p>Quem morre com ela <b>não larga nada</b> — leva tudo consigo. É a poção mais cara do mod a fazer, e
     * a única razão de alguém entrar numa dimensão que não conhece.
     */
    public static final Holder<MobEffect> KEEP_INVENTORY = register("keep_inventory",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFD700) {
            });

    /**
     * <b>Guardar o Que Se Bebeu</b>: o {@code PotionKeepEffectsOnDeath}.
     *
     * <p>E a irmã dela: quem morre com esta <b>acorda com as mesmas poções no corpo</b>. Sem ela, morrer apaga
     * horas de caldeirão.
     */
    public static final Holder<MobEffect> KEEP_EFFECTS_ON_DEATH = register("keep_effects_on_death",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xDAA520) {
            });

    /**
     * <b>Fortuna</b>: o {@code PotionFortune}.
     *
     * <p>O que se cava cai como se a picareta tivesse <b>Fortuna</b> — um nível a mais, ou dois do quarto grau
     * em diante. Não vale para o que tem toque suave, nem para o que guarda coisas dentro.
     *
     * <p>Quem trata disto é o {@link OccultaEvents}, que é quem vê o bloco cair.
     */
    public static final Holder<MobEffect> FORTUNE = register("fortune",
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x50C878) {
            });

    /**
     * <b>Colorido</b>: o {@code PotionColorful}.
     *
     * <p>Não faz <b>nada</b>. Pinta quem a tem da cor do grau — as dezesseis tintas do jogo, pela ordem delas
     * — e é só isso. O leite não a tira, e ela não aparece na lista de efeitos com um nome: aparece com a
     * <b>cor</b>.
     *
     * <p>É a melhor piada do Witchery: a poção mais difícil de fazer sem efeito nenhum.
     */
    public static final Holder<MobEffect> COLORFUL = register("colorful",
            new MobEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF) {
            });

    /**
     * A cor que o <b>Colorido</b> põe em quem o tem, para quem desenha poder vê-la.
     *
     * <p><b>É preciso porque as poções de um bicho não vão para quem joga.</b> O jogo de hoje manda ao cliente
     * as poções do <i>próprio</i> jogador e mais nada — um porco com uma poção no corpo é, do lado de quem
     * olha, um porco qualquer. Em 2014 isso também era assim, e o original escapava porque pintava o bicho
     * <b>dentro</b> do desenho, que lá corria com o bicho do servidor à mão.
     *
     * <p>Por isso a cor viaja num <b>apego sincronizado</b>, posto e tirado na batida de quem a tem. É menos
     * uma cor por bicho do que um <b>fato</b>: este está pintado, e desta cor.
     */
    public static final net.fabricmc.fabric.api.attachment.v1.AttachmentType<Integer> COR =
            net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.<Integer>builder()
                    .syncWith(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast(),
                            net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.all())
                    .buildAndRegister(Thaumcraft.id("colorful"));

    /**
     * A batida que põe e tira a cor.
     *
     * <p>Corre em todo bicho vivo, de segundo em segundo, no mesmo lugar em que as maldições correm. Não custa
     * nada: é uma pergunta por poção, e quase ninguém a tem.
     */
    public static void tickColour(ServerLevel level, LivingEntity quem) {
        if (quem.tickCount % 20 != 11) return;
        var pintado = quem.getEffect(COLORFUL);
        if (pintado == null) {
            if (quem.hasAttached(COR)) quem.removeAttached(COR);
            return;
        }
        int grau = pintado.getAmplifier();
        Integer tinha = quem.getAttached(COR);
        if (tinha == null || tinha != grau) quem.setAttached(COR, grau);
    }

    private static Holder<MobEffect> register(String nome, MobEffect efeito) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thaumcraft.id(nome), efeito);
    }

    /**
     * Os que o leite não tira, como no original: o {@code setIncurable} de cada um.
     *
     * <p>Repare que a lista é quase toda de coisas <b>ruins</b>, e é de propósito: o que o ofício faz a alguém
     * de propósito não se lava com um balde de leite. As duas boas que estão nela — a Máscara de Gás e a
     * Barriga Forte — são as que se bebem <b>antes</b> de uma coisa perigosa, e perdê-las ao beber leite no
     * meio seria uma morte estúpida.
     */
    public static boolean incurable(Holder<MobEffect> qual) {
        return qual == GAS_MASK || qual == STOUT_BELLY || qual == SUN_ALLERGY || qual == DARKNESS_ALLERGY
                || qual == COLORFUL || qual == DISEASED || qual == FEEL_NO_PAIN || qual == ILL_FITTING
                || qual == MORTAL_COIL || qual == PARALYSIS || qual == QUEASY || qual == WRAPPED_IN_VINE
                || qual == ENSLAVED;
    }

    public static void init() {
    }

    /** A luz do lugar, para quem precisar dela sem ter o nível à mão. */
    public static int light(ServerLevel level, BlockPos onde) {
        return level.getBrightness(LightLayer.BLOCK, onde);
    }
}
