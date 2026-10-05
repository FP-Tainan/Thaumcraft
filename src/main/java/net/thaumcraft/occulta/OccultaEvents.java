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
            if (quemLevou instanceof net.minecraft.server.level.ServerPlayer quem) {
                net.thaumcraft.occulta.divine.Predictions.levouDano(level, quem, fonte);
            }
        });

        /*
         * As <b>profecias</b>, que precisam de três portas: a do golpe levado, a da batida — que é onde
         * elas se forçam quando o prazo passa — e a do bloco partido, que é por onde o ferro e o diamante
         * caem a mais.
         */
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(servidor -> {
            for (var quem : servidor.getPlayerList().getPlayers()) {
                if (quem.level() instanceof ServerLevel level) {
                    net.thaumcraft.occulta.divine.Predictions.batida(level, quem);
                }
            }
        });

        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register(
                (level, quem, onde, oquê, alma) -> {
                    if (!(level instanceof ServerLevel mundo)) return;
                    if (!(quem instanceof net.minecraft.server.level.ServerPlayer gente)) return;
                    net.thaumcraft.occulta.divine.Predictions.partiu(mundo, gente, oquê, onde);
                });

        /*
         * A Boline colhe a Rosa de Sangue antes de o jogo lhe tocar: a rosa não deixa nada quando se quebra,
         * e só a faca de colher do ofício a tira do chão inteira, com o que ela guarda dentro.
         */
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register(
                (level, quem, onde, oquê, alma) -> {
                    if (!(level instanceof ServerLevel mundo)) return true;
                    if (!oquê.is(OccultaBlocks.BLOOD_ROSE)) return true;
                    if (!quem.getMainHandItem().is(OccultaItems.BOLINE)) return true;
                    return !net.thaumcraft.occulta.vampire.BolineItem.colheARosa(mundo, onde, quem);
                });

        /*
         * O soco com a <b>Mão de Bruxa</b>, que não magoa ninguém: ele é só o jeito de a infusão de quem
         * a tem chegar ao mundo.
         */
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(
                net.thaumcraft.occulta.WitchHandItem::soco);

        ServerLivingEntityEvents.AFTER_DEATH.register((quemMorreu, fonte) -> {
            if (!(quemMorreu.level() instanceof ServerLevel level)) return;
            onDeath(level, quemMorreu, fonte);
            net.thaumcraft.occulta.WitchHandItem.deUmaBruxaMorta(level, quemMorreu, fonte);
        });

        // e o jogador novo recebe o que o velho guardou: as poções que atravessaram a morte
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.COPY_FROM.register(
                (velho, novo, manteveTudo) -> {
                    var levou = velho.getAttachedOrCreate(GUARDADAS);
                    if (levou.isEmpty()) return;
                    for (var tem : levou) {
                        novo.addEffect(new net.minecraft.world.effect.MobEffectInstance(tem));
                    }
                    velho.removeAttached(GUARDADAS);
                });
    }

    /**
     * O que cada poção faz quando quem a tem <b>morre</b>: os {@code IHandleLivingDeath} do Witchery.
     *
     * <p>São três, e as três são o avesso uma da outra: a <b>Reencarnação</b> troca o morto por outra coisa, e
     * as duas de <b>guardar</b> fazem a morte custar menos.
     */
    private static void onDeath(ServerLevel level, LivingEntity quemMorreu, DamageSource fonte) {
        ExtraDrops.larga(level, quemMorreu);
        reincarnate(level, quemMorreu, fonte);
        keepEffects(quemMorreu);
    }

    /**
     * A <b>Reencarnação</b>: o {@code PotionReincarnate}.
     *
     * <p>Do corpo levanta-se outra coisa, e o que se levanta diz o que o morto era: de bicho ou de aranha sai
     * bicho de teia; de tudo o mais, morto-vivo. E quanto mais forte a poção, pior o que sai.
     *
     * <p>O que nasce <b>já odeia quem matou</b> — é o {@code attacker} que o original passa ao
     * {@code spawnCreature}, e é o que torna a poção uma vingança e não um truque.
     */
    private static void reincarnate(ServerLevel level, LivingEntity quemMorreu, DamageSource fonte) {
        var volta = quemMorreu.getEffect(OccultaEffects.REINCARNATE);
        if (volta == null) return;
        int grau = volta.getAmplifier();

        boolean deTeia = quemMorreu instanceof net.minecraft.world.entity.animal.Animal
                || quemMorreu instanceof net.minecraft.world.entity.monster.spider.Spider;
        net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> qual;
        if (deTeia) {
            qual = grau > 2 ? net.minecraft.world.entity.EntityTypes.CREEPER
                    : grau > 1 ? net.minecraft.world.entity.EntityTypes.CAVE_SPIDER
                    : net.minecraft.world.entity.EntityTypes.SPIDER;
        } else {
            qual = grau > 2 ? net.minecraft.world.entity.EntityTypes.BLAZE
                    : grau > 1 ? net.minecraft.world.entity.EntityTypes.SKELETON
                    : net.minecraft.world.entity.EntityTypes.ZOMBIE;
        }

        var nasceu = qual.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (nasceu == null) return;
        nasceu.snapTo(quemMorreu.getX(), quemMorreu.getY(), quemMorreu.getZ(),
                quemMorreu.getYRot(), quemMorreu.getXRot());
        nasceu.finalizeSpawn(level, level.getCurrentDifficultyAt(nasceu.blockPosition()),
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED, null);
        if (fonte.getEntity() instanceof LivingEntity quemMatou) nasceu.setTarget(quemMatou);
        level.addFreshEntity(nasceu);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                quemMorreu.getX(), quemMorreu.getY() + 0.5, quemMorreu.getZ(), 24, 0.4, 0.6, 0.4, 0.0);
    }

    /**
     * <b>Guardar o Que Se Bebeu</b>: o {@code PotionKeepEffectsOnDeath}.
     *
     * <p>Ela se guarda a si mesma junto com as outras, e é a única coisa boa de morrer com o caldeirão cheio.
     *
     * <p>Quem a tem é marcado com o apego {@link #GUARDADAS}; quem o lê é o {@code COPY_FROM}, que corre
     * quando o jogador novo toma o lugar do velho.
     */
    private static void keepEffects(LivingEntity quemMorreu) {
        if (!(quemMorreu instanceof net.minecraft.world.entity.player.Player gente)) return;
        if (!gente.hasEffect(OccultaEffects.KEEP_EFFECTS_ON_DEATH)) return;

        java.util.List<net.minecraft.world.effect.MobEffectInstance> levou = new java.util.ArrayList<>();
        for (var tem : gente.getActiveEffects()) levou.add(new net.minecraft.world.effect.MobEffectInstance(tem));
        gente.setAttached(GUARDADAS, levou);
    }

    /**
     * As poções que atravessaram a morte com quem as tinha.
     *
     * <p>Fica num apego e não numa lista do servidor porque o jogador que morre e o que acorda são, para o
     * jogo, <b>dois objetos diferentes</b>; o apego é o que passa de um para o outro.
     */
    public static final net.fabricmc.fabric.api.attachment.v1.AttachmentType<
            java.util.List<net.minecraft.world.effect.MobEffectInstance>> GUARDADAS =
            net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
                    .<java.util.List<net.minecraft.world.effect.MobEffectInstance>>builder()
                    .initializer(java.util.List::of)
                    .buildAndRegister(net.thaumcraft.Thaumcraft.id("kept_effects"));

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
