package net.thaumcraft.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/** Os efeitos do Thaumcraft 4.2.3.5, com as cores dos {@code Potion} do {@code Config}. */
public final class TCEffects {
    /** A proteção contra a dobra ({@code PotionWarpWard}): o fluido purificante a dá; segura os efeitos da dobra. */
    public static final Holder<MobEffect> WARP_WARD = register("warp_ward", new MobEffect(MobEffectCategory.BENEFICIAL, 14742263) {
    });

    /**
     * O fluxo da mácula ({@code PotionFluxTaint}): a cada 40 tiques (metade por nível) fere em 1 o que não é morto-vivo
     * e cura em 1 o que já é maculado. Quem morre com ele vira a versão maculada de si.
     */
    public static final Holder<MobEffect> FLUX_TAINT = register("flux_taint", new MobEffect(MobEffectCategory.HARMFUL, 6697847) {
        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amplifier) {
            if (target instanceof net.thaumcraft.api.TaintedMob) {
                target.heal(1.0f);
            } else if (!target.isInvertedHealAndHarm()) {
                target.hurtServer(level, TCDamageTypes.taint(level), 1.0f);
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            int k = 40 >> amplifier;
            return k <= 0 || duration % k == 0;
        }
    });

    /** A exaustão de vis ({@code PotionVisExhaust}): cada nível encarece o vis da varinha em 10%. */
    public static final Holder<MobEffect> VIS_EXHAUST = register("vis_exhaust", new MobEffect(MobEffectCategory.HARMFUL, 6702199) {
    });

    /**
     * A exaustão de vis contagiosa ({@code PotionInfectiousVisExhaust}): encarece o vis como a outra e, a cada dois
     * segundos, passa para quem estiver a quatro blocos — um nível abaixo (no nível zero, a exaustão comum).
     */
    public static final Holder<MobEffect> INFECTIOUS_VIS_EXHAUST = register("infectious_vis_exhaust",
            new MobEffect(MobEffectCategory.HARMFUL, 6706551) {
                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amplifier) {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0))) {
                        if (e.hasEffect(TCEffects.INFECTIOUS_VIS_EXHAUST)) continue;
                        if (amplifier > 0) e.addEffect(new MobEffectInstance(TCEffects.INFECTIOUS_VIS_EXHAUST, 6000, amplifier - 1));
                        else e.addEffect(new MobEffectInstance(TCEffects.VIS_EXHAUST, 6000, 0));
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 40 == 0;
                }
            });

    private TCEffects() {
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thaumcraft.id(name), effect);
    }

    /** O acréscimo ao custo de vis da exaustão: 10% por nível da mais forte das duas (o {@code getTotalVisDiscount}). */
    public static int exhaustion(Player player) {
        int level = -1;
        MobEffectInstance a = player.getEffect(VIS_EXHAUST), b = player.getEffect(INFECTIOUS_VIS_EXHAUST);
        if (a != null) level = Math.max(level, a.getAmplifier());
        if (b != null) level = Math.max(level, b.getAmplifier());
        return level < 0 ? 0 : (level + 1) * 10;
    }

    public static void init() {
    }
}
