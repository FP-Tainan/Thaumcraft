package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.RitualCircles;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O <b>rito de cozimento</b>: o caldeirão como lançador, e não como panela.
 *
 * <p>É a última peça grande do ofício, e a que mais muda o que ele é. Um caldeirão cheio e a ferver pode
 * fazer um frasco — ou, com a coisa certa dentro, <b>atirar o cozimento ao longe</b>: por cima de uma
 * área, ou em cima de <b>uma pessoa marcada</b>, onde quer que ela esteja no mundo.
 *
 * <p>É assim que se amaldiçoa alguém à distância no Witchery. Não há projétil, não há alcance de arma e
 * não há aviso: há um caldeirão numa cave, um círculo de giz, um coven, e uma marca com o nome de quem
 * vai adoecer.
 *
 * <h2>O que o dispara</h2>
 *
 * <p>Duas coisas, e nenhuma delas é um ingrediente de cozimento:
 *
 * <ul>
 *   <li>a <b>Língua de Cão</b> — duzentos e cinquenta de poder — espalha o cozimento <b>à volta do
 *       caldeirão</b>, e só serve com cozimento que se atire;</li>
 *   <li>a <b>Marca</b> — mil de poder — atira-o em <b>quem ela nomeia</b>.</li>
 * </ul>
 *
 * <h2>O giz paga, e o giz infernal arrisca</h2>
 *
 * <p>Este é o desenho mais bonito do original, e ele nunca o explica. O rito custa <b>uma vez e quatro
 * décimos</b> do poder que o cozimento pediria — e cada anel de giz à volta do caldeirão <b>desconta</b>:
 * dois décimos por anel de ritual, <b>trinta e sete centésimos</b> por anel infernal.
 *
 * <p>Mas o giz infernal <b>cobra de outra maneira</b>: cada anel dele soma um ponto de <b>risco</b>, e
 * quem não tiver anel de ritual nenhum já começa com um. O risco é a chance de o caldeirão <b>se virar
 * contra quem está por perto</b> — lentidão, paralisia, loucura, e, no pior dos casos, raios.
 *
 * <p>Quer dizer: o caminho barato é o perigoso, e quem quiser um rito seguro paga-o em poder.
 */
public final class BrewRitual {
    /** De quantas em quantas batidas o caldeirão olha para o rito, e quantas olhadas até começar. */
    public static final int DE_QUANTO = 10;
    public static final int ESPERA = 20;

    /** A escala do poder, e o que cada anel lhe desconta. */
    public static final double ESCALA = 1.4;
    public static final double DESCONTO_RITUAL = 0.2;
    public static final double DESCONTO_INFERNAL = 0.37;

    /** A que distância do caldeirão se conta o coven. */
    public static final double COVEN_A = 6.0;

    /** E o coven a partir do qual não há limite de distância. */
    public static final int COVEN_SEM_LIMITE = 6;

    /** A que distância a má sorte apanha quem estiver por perto. */
    public static final double MÁ_SORTE_A = 16.0;

    /** Como o rito pode acabar. */
    public enum Estado {
        /** Ainda está a correr. */
        ANDANDO,
        /** Acabou, e o caldeirão esvazia. */
        PRONTO,
        /** O alvo está longe demais para este coven. */
        LONGE,
        /** Não há alvo nenhum: a marca não nomeia ninguém que esteja no mundo. */
        SEM_ALVO
    }

    private BrewRitual() {
    }

    // ------------------------------------------------------------------ o coven

    /**
     * Quantas bruxas acompanham: o {@code witchCount} do original.
     *
     * <p>Conta as <b>bruxas de coven com dono</b> a seis blocos, e os <b>jogadores com coven</b> que
     * estejam ali — <b>menos o primeiro</b>. Esse primeiro é quem faz o rito, e quem faz não se acompanha
     * a si mesmo.
     */
    public static int coven(ServerLevel level, BlockPos onde) {
        AABB roda = new AABB(onde).inflate(COVEN_A);
        int quantas = 0;
        for (var bruxa : level.getEntitiesOfClass(
                net.thaumcraft.occulta.coven.CovenWitchEntity.class, roda)) {
            if (bruxa.dono() != null) quantas++;
        }
        boolean oPrimeiro = false;
        for (Player gente : level.getEntitiesOfClass(Player.class, roda)) {
            if (net.thaumcraft.occulta.coven.Coven.tamanho(gente) <= 0) continue;
            if (oPrimeiro) quantas++;
            else oPrimeiro = true;
        }
        return quantas;
    }

    // ------------------------------------------------------------------ o giz

    /** A escala do poder, pelos anéis que houver. */
    public static double escala(RitualCircles.Circles anéis) {
        double escala = ESCALA;
        if (anéis.inner().allOf(RitualCircles.INNER, 0)) escala -= DESCONTO_RITUAL;
        if (anéis.middle().allOf(RitualCircles.MIDDLE, 0)) escala -= DESCONTO_RITUAL;
        if (anéis.inner().allOf(RitualCircles.INNER, 2)) escala -= DESCONTO_INFERNAL;
        if (anéis.middle().allOf(RitualCircles.MIDDLE, 2)) escala -= DESCONTO_INFERNAL;
        return escala;
    }

    /** E o risco: um se não houver giz de ritual nenhum, mais um por anel infernal. */
    public static int risco(RitualCircles.Circles anéis) {
        boolean pequeno = anéis.inner().allOf(RitualCircles.INNER, 0);
        boolean médio = anéis.middle().allOf(RitualCircles.MIDDLE, 0);
        int risco = pequeno || médio ? 0 : 1;
        if (anéis.inner().allOf(RitualCircles.INNER, 2)) risco++;
        if (anéis.middle().allOf(RitualCircles.MIDDLE, 2)) risco++;
        return risco;
    }

    // ------------------------------------------------------------------ a distância

    /**
     * Se o alvo está ao alcance deste coven: o {@code isDistanceAllowed}.
     *
     * <p>Com <b>seis bruxas</b> não há limite nenhum. Abaixo disso o alcance cresce com o coven — e
     * cresce depressa: sozinho são trinta e dois blocos, com cinco são quase trezentos.
     */
    public static boolean alcança(BlockPos caldeirão, Vec3 alvo, int coven) {
        if (coven >= COVEN_SEM_LIMITE) return true;
        double quanto = (1 + coven) * 4.0;
        double teto = 2.0 * quanto * quanto;
        return caldeirão.distToCenterSqr(alvo.x, alvo.y, alvo.z) <= teto * teto;
    }

    // ------------------------------------------------------------------ a má sorte

    /**
     * O caldeirão virado contra quem está por perto: o {@code checkForMisfortune}.
     *
     * <p>Metade das vezes não acontece nada. Do resto, quase tudo é lentidão; uma vez em dez é
     * <b>loucura</b>, e uma em cinquenta são <b>raios</b>.
     */
    public static void máSorte(ServerLevel level, BlockPos onde, int risco, int poder) {
        if (risco <= 0 || poder <= 0) return;
        double sorte = level.getRandom().nextDouble() * (1.0 + (risco - 1) * 0.2);
        if (sorte < 0.5) return;

        if (sorte < 0.75) {
            emVolta(level, onde, new MobEffectInstance(MobEffects.SLOWNESS, 60 * 20, 1));
        } else if (sorte < 0.9) {
            emVolta(level, onde, new MobEffectInstance(
                    net.thaumcraft.occulta.OccultaEffects.PARALYSIS, 20 * 20, 2));
        } else if (sorte < 0.98) {
            emVolta(level, onde, new MobEffectInstance(
                    net.thaumcraft.occulta.OccultaEffects.INSANITY, 3 * 1200, 2));
        } else {
            emVolta(level, onde, new MobEffectInstance(
                    net.thaumcraft.occulta.OccultaEffects.PARALYSIS, 10 * 20, 2));
            int quantos = level.getRandom().nextInt(3) + 2;
            for (int n = 0; n < quantos; n++) raio(level, onde);
        }
    }

    /** O efeito em tudo o que estiver a dezasseis blocos. */
    private static void emVolta(ServerLevel level, BlockPos onde, MobEffectInstance qual) {
        AABB roda = new AABB(onde).inflate(MÁ_SORTE_A);
        for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, roda)) {
            if (quem.distanceToSqr(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5)
                    > MÁ_SORTE_A * MÁ_SORTE_A) {
                continue;
            }
            quem.addEffect(new MobEffectInstance(qual));
        }
    }

    /** E um raio perto, que é o pior que ela faz. */
    private static void raio(ServerLevel level, BlockPos onde) {
        int dx = level.getRandom().nextInt(9) - 4;
        int dz = level.getRandom().nextInt(9) - 4;
        BlockPos casa = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                onde.offset(dx, 0, dz));
        var raio = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (raio == null) return;
        raio.snapTo(Vec3.atBottomCenterOf(casa));
        level.addFreshEntity(raio);
    }

    // ------------------------------------------------------------------ e o rito em si

    /**
     * Uma batida do rito: gasta o poder, espalha o cozimento e tira a sorte da má sorte.
     *
     * @param alvo   onde o cozimento cai — o próprio caldeirão, ou quem a marca nomeia
     * @param pulsos quantas batidas o rito já leva
     */
    public static Estado corre(ServerLevel level, BlockPos caldeirão, @Nullable Vec3 alvo,
                               List<Item> dentro, int pulsos, int poder) {
        if (alvo == null) return Estado.SEM_ALVO;

        int coven = coven(level, caldeirão);
        if (!alcança(caldeirão, alvo, coven)) return Estado.LONGE;

        var anéis = RitualCircles.read(level, caldeirão);
        int gasto = (int) Math.floor(poder * escala(anéis));
        if (gasto > 0) {
            var altar = net.thaumcraft.occulta.PowerSources.closest(level, caldeirão);
            if (altar == null || !altar.consume(gasto)) return Estado.ANDANDO;
        }

        BrewImpact espalha = Brew.impact(dentro, null);
        BrewDispersal jeito = espalha.dispersal();
        Estado deu = jeito == null ? Estado.PRONTO
                : jeito.noRito(level, BlockPos.containing(alvo), dentro, coven, pulsos, espalha);

        máSorte(level, caldeirão, risco(anéis), poder);
        return deu;
    }

    /** Os temperos de um rito: o coven entra como força, e não há quem atirou. */
    public static BrewModifiers temperos(int coven) {
        BrewModifiers temperos = new BrewModifiers();
        temperos.coven = coven;
        return temperos;
    }

    /** O estalo e o pó de cada batida do rito. */
    public static void fumo(ServerLevel level, BlockPos onde) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.02);
    }

    /** E o que se ouve quando ele falha. */
    public static void falhou(ServerLevel level, BlockPos onde) {
        level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 0.6f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                onde.getX() + 0.5, onde.getY() + 1.0, onde.getZ() + 0.5, 24, 0.4, 0.4, 0.4, 0.02);
    }
}
