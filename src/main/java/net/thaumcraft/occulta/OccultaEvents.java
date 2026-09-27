package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * O que acontece quando alguém apanha, e que vem de uma poção do ofício: os {@code IHandleLivingHurt} do
 * Witchery.
 *
 * <p>São duas: a que <b>envenena o que a pessoa acerta</b> e a que <b>estoura quem apanha</b>. Nenhuma delas faz
 * coisa alguma sozinha — as duas esperam uma pancada.
 */
public final class OccultaEvents {
    /** O estouro da volatilidade cresce com o grau, e para nos três. */
    public static final float BLAST_BASE = 2.0f;
    public static final float BLAST_PER_LEVEL = 0.5f;
    public static final float BLAST_MAX = 3.0f;

    private OccultaEvents() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((quemLevou, fonte, dano, levou, aparado) -> {
            if (!(quemLevou.level() instanceof ServerLevel level)) return;
            poisonWeapons(level, quemLevou, fonte);
            volatility(level, quemLevou, fonte);
        });
    }

    /**
     * O {@code PotionPoisonWeapons}: quem bate com a poção na veia envenena quem apanhou.
     *
     * <p>Do primeiro ao terceiro grau o veneno vai subindo; do quarto em diante, não é mais veneno — é
     * apodrecimento. Só vale para golpe de gente ou de bicho, não para flecha nem fogo.
     */
    private static void poisonWeapons(ServerLevel level, LivingEntity quemLevou, DamageSource fonte) {
        if (!(fonte.getEntity() instanceof LivingEntity quemBateu)) return;
        if (!isMelee(fonte)) return;
        var arma = quemBateu.getEffect(OccultaEffects.POISON_WEAPONS);
        if (arma == null) return;
        switch (arma.getAmplifier()) {
            case 0 -> quemLevou.addEffect(new MobEffectInstance(MobEffects.POISON, secs(5), 0));
            case 1 -> quemLevou.addEffect(new MobEffectInstance(MobEffects.POISON, secs(5), 1));
            case 2 -> quemLevou.addEffect(new MobEffectInstance(MobEffects.POISON, secs(15), 1));
            default -> quemLevou.addEffect(new MobEffectInstance(MobEffects.WITHER, secs(20), 0));
        }
    }

    /**
     * O {@code PotionVolatility}: quem a tem estoura ao apanhar.
     *
     * <p>Vindo de outro estouro, é certo; das outras pancadas, uma em cinco (menos com o grau). E de vez em
     * quando a própria volatilidade se gasta nisso.
     */
    private static void volatility(ServerLevel level, LivingEntity quemLevou, DamageSource fonte) {
        var carga = quemLevou.getEffect(OccultaEffects.VOLATILITY);
        if (carga == null || !isExplodable(fonte)) return;
        int grau = carga.getAmplifier();
        boolean deEstouro = fonte.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
        if (!deEstouro && level.getRandom().nextInt(Math.max(5 - Math.min(grau, 3), 1)) != 0) return;
        if (level.getRandom().nextInt(grau + 3) == 0) quemLevou.removeEffect(OccultaEffects.VOLATILITY);
        level.explode(deEstouro ? quemLevou : null, quemLevou.getX(), quemLevou.getY(), quemLevou.getZ(),
                Math.min(BLAST_BASE + BLAST_PER_LEVEL * grau, BLAST_MAX), Level.ExplosionInteraction.MOB);
    }

    /** O golpe de perto: é o que o original chama de "mob" e "player". */
    private static boolean isMelee(DamageSource fonte) {
        return fonte.getDirectEntity() == fonte.getEntity() && fonte.getEntity() instanceof LivingEntity;
    }

    /** O que não estoura: queda, fogo, afogamento, fome e sufocamento — o resto, sim. */
    private static boolean isExplodable(DamageSource fonte) {
        return !fonte.is(DamageTypes.FALL) && !fonte.is(DamageTypes.IN_FIRE)
                && !fonte.is(DamageTypes.ON_FIRE) && !fonte.is(DamageTypes.DROWN)
                && !fonte.is(DamageTypes.STARVE) && !fonte.is(DamageTypes.IN_WALL);
    }

    private static int secs(int quanto) {
        return quanto * 20;
    }
}
