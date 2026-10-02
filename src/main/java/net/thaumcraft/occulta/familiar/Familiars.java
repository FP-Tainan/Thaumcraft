package net.thaumcraft.occulta.familiar;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * O vínculo com um familiar: o {@code Familiar} do Witchery.
 *
 * <p>Um familiar não é um bicho de estimação. <b>Ele leva pancada por quem o tem</b> — um por cento do golpe de
 * longe, <b>dez por cento</b> se estiver a menos de vinte e quatro blocos —, e <b>ele não morre</b>: se fosse
 * morrer, quem o tem leva o dobro da própria vida e cai no lugar dele. É o vínculo do original, e é o que
 * explica por que ter um custa alguma coisa.
 *
 * <p>Sem dono por perto ele também não morre: fica com um de vida e continua. Um familiar perdido não se perde.
 *
 * <p>E ele <b>não vai com quem morre</b>: a morte desfaz o vínculo e o bicho fica no mundo, solto.
 */
public final class Familiars {
    /** O que ele leva do golpe quando está longe, e quando está perto: os do original. */
    public static final float PARTE_DE_LONGE = 0.01f;
    public static final float PARTE_DE_PERTO = 0.1f;

    /** E o que conta como perto: vinte e quatro blocos, que é o 576 ao quadrado do original. */
    public static final double PERTO_AO_QUADRADO = 576.0;

    private Familiars() {
    }

    // ------------------------------------------------------------------ as três maestrias

    /** Se esta pessoa tem familiar acordado: o {@code hasActiveFamiliar} do original. */
    public static boolean temAlgum(@Nullable Player gente) {
        return gente != null && FamiliarData.of(gente).tem();
    }

    public static Optional<FamiliarKind> qual(@Nullable Player gente) {
        return gente == null ? Optional.empty() : FamiliarData.of(gente).kind();
    }

    /** <b>O gato</b>: o escuro de uma maldição dura mais. */
    public static boolean temMaestriaDeMaldicao(@Nullable Player gente) {
        return qual(gente).orElse(null) == FamiliarKind.CAT;
    }

    /** <b>O sapo</b>: sai um frasco a mais de cada caldeirão. */
    public static boolean temMaestriaDeCozimento(@Nullable Player gente) {
        return qual(gente).orElse(null) == FamiliarKind.TOAD;
    }

    /**
     * <b>A coruja</b>: a maestria da vassoura.
     *
     * <p>Ela não destranca nada ainda, porque <b>a vassoura não está portada</b>. Fica aqui porque o bicho é do
     * original e porque, no dia em que a vassoura vier, é só perguntar.
     */
    public static boolean temMaestriaDeVassoura(@Nullable Player gente) {
        return qual(gente).orElse(null) == FamiliarKind.OWL;
    }

    // ------------------------------------------------------------------ vincular e desfazer

    /** Se este bicho pode virar familiar: domado, e de um dos feitios que o original aceita. */
    public static boolean podeVirar(@Nullable Entity bicho) {
        return bicho instanceof TamableAnimal domado && domado.isTame() && deQueFeitio(bicho) != null;
    }

    /**
     * Que familiar este bicho seria.
     *
     * <p>O original aceita o gato dele, <b>a jaguatirica do próprio jogo</b>, o sapo e a coruja. Aqui o gato do
     * jogo entra no lugar da jaguatirica, que é quem herdou o papel dela.
     */
    public static @Nullable FamiliarKind deQueFeitio(@Nullable Entity bicho) {
        if (bicho instanceof ToadEntity) return FamiliarKind.TOAD;
        if (bicho instanceof OwlEntity) return FamiliarKind.OWL;
        if (bicho instanceof net.minecraft.world.entity.animal.feline.Cat) return FamiliarKind.CAT;
        return null;
    }

    /**
     * Vincula o bicho a quem o domou.
     *
     * <p>Ele <b>ganha nome aqui</b> — um da lista do feitio dele —, e é o nome que leva daí em diante.
     *
     * @return falso se já havia familiar, ou se o bicho não serve
     */
    public static boolean vincula(Player gente, Entity bicho) {
        if (FamiliarData.of(gente).tem()) return false;
        FamiliarKind feitio = deQueFeitio(bicho);
        if (feitio == null || !podeVirar(bicho)) return false;

        String nome = bicho.hasCustomName()
                ? bicho.getName().getString()
                : feitio.sorteiaNome(gente.getRandom());
        bicho.setCustomName(Component.literal(nome));
        FamiliarData.set(gente, new FamiliarData(Optional.of(feitio),
                Optional.of(bicho.getUUID()), nome, true));
        return true;
    }

    /** E desfaz: o bicho fica no mundo, solto. */
    public static void desfaz(Player gente) {
        FamiliarData.set(gente, FamiliarData.NENHUM);
    }

    /** O bicho vinculado, se estiver carregado por perto. */
    public static @Nullable LivingEntity acha(ServerLevel level, Player gente) {
        FamiliarData dado = FamiliarData.of(gente);
        if (dado.quem().isEmpty()) return null;
        UUID qual = dado.quem().get();
        Entity achado = level.getEntity(qual);
        return achado instanceof LivingEntity vivo ? vivo : null;
    }

    // ------------------------------------------------------------------ a pancada que ele leva

    /**
     * O quanto do golpe o familiar leva por quem o tem.
     *
     * <p>Um por cento de longe, dez por cento de perto — e <b>só se der um de dano inteiro</b>, que é o corte
     * do original: um familiar não sangra por arranhão.
     *
     * @return o que sobra do golpe para quem o levou
     */
    public static float desvia(ServerLevel level, Player gente,
                               net.minecraft.world.damagesource.DamageSource fonte, float dano) {
        LivingEntity familiar = acha(level, gente);
        if (familiar == null || !familiar.isAlive()) return dano;

        float parte = familiar.distanceToSqr(gente) <= PERTO_AO_QUADRADO
                ? PARTE_DE_PERTO : PARTE_DE_LONGE;
        float levado = dano * parte;
        if (levado >= 1.0f) familiar.hurtServer(level, fonte, levado);
        return dano - levado;
    }

    /**
     * O familiar ia morrer.
     *
     * <p><b>Ele não morre.</b> Se o dono estiver no mesmo mundo, é o dono que cai — o dobro da vida dele —, e o
     * vínculo desfaz-se. Sem dono por perto, o bicho fica com um de vida e continua.
     *
     * @return verdadeiro se a morte foi impedida
     */
    public static boolean morreriaAgora(ServerLevel level, LivingEntity familiar) {
        Player dono = donoDe(level, familiar);
        if (dono == null) {
            familiar.setHealth(1.0f);
            return true;
        }
        dono.hurtServer(level, level.damageSources().magic(), dono.getMaxHealth() * 2.0f);
        desfaz(dono);
        familiar.setHealth(1.0f);
        return true;
    }

    /** De quem este bicho é familiar, se for de alguém que esteja no mundo. */
    public static @Nullable Player donoDe(ServerLevel level, LivingEntity bicho) {
        for (Player gente : level.players()) {
            FamiliarData dado = FamiliarData.of(gente);
            if (dado.quem().filter(q -> q.equals(bicho.getUUID())).isPresent()) return gente;
        }
        return null;
    }

    /** E quando quem o tem cai, o vínculo desfaz-se — o bicho fica. */
    public static void doneMorreu(Player gente) {
        desfaz(gente);
    }

    /** Para as provas: se há algum familiar vinculado num raio. */
    public static boolean algumPorPerto(ServerLevel level, Player gente, double raio) {
        LivingEntity familiar = acha(level, gente);
        if (familiar == null) return false;
        AABB roda = gente.getBoundingBox().inflate(raio);
        return roda.contains(familiar.position());
    }
}
