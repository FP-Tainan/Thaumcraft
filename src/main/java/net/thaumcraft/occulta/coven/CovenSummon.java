package net.thaumcraft.occulta.coven;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteStep;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <b>Chamar o coven</b>: o {@code EntityCovenWitch.summonCoven} do Witchery.
 *
 * <p>Uma bruxa recrutada <b>saiu do mundo</b>, e o que ficou dela foi o retrato na lista de quem a
 * recrutou. Chamá-la é refazê-la a partir desse retrato — a menos que ela já esteja por perto, caso em que
 * ela simplesmente <b>vem</b>.
 *
 * <p>Elas vêm <b>uma por segundo</b>, pela ordem em que entraram. É o que faz um coven de seis demorar seis
 * segundos a juntar-se, e é o que o original quis: ver as bruxas chegando uma a uma.
 *
 * <p>E elas <b>não ficam</b>. Uma bruxa chamada tem <b>um minuto</b> e some; um coven é uma visita.
 *
 * <h2>Onde elas aparecem</h2>
 *
 * <p>Se houver um <b>Caldeirão de Bruxa</b> a nove blocos, elas vão para lá — a três casas dele, e nunca
 * em cima dele. É um detalhe pequeno do original e diz muito: um coven chamado procura o caldeirão, porque
 * é ali que elas trabalham. Não havendo caldeirão, elas aparecem à volta de quem as chamou.
 */
public final class CovenSummon {
    /** Até onde se procura uma bruxa que já esteja por perto, antes de refazer o retrato dela. */
    public static final double LONGE_XZ = 64.0;
    public static final double LONGE_Y = 16.0;

    /** Quanto tempo uma bruxa chamada fica: o minuto do original. */
    public static final int PRAZO = 1200;

    /** De quantas em quantas batidas vem a seguinte. */
    public static final int A_CADA = 20;

    /** A que distância um caldeirão as atrai, e a que distância dele elas param. */
    public static final double CALDEIRÃO = 9.0;
    public static final int DO_CALDEIRÃO_MAX = 3;
    public static final int DO_CALDEIRÃO_MIN = 1;

    /**
     * As seis casas à volta do círculo onde elas se põem: as do original.
     *
     * <p>Quatro nas diagonais a duas casas e duas no eixo a três — o que faz uma roda torta, e de propósito:
     * nenhuma delas fica em cima de um glifo.
     */
    public static final int[][] À_VOLTA = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}, {0, 3}, {0, -3}};

    private CovenSummon() {
    }

    /**
     * Chama a bruxa daquela posição da lista, se houver uma.
     *
     * @return falso se o coven não tem tantas bruxas, ou se a daquela posição veio de um mundo velho e não
     *         tem nome por que a refazer
     */
    public static boolean chama(ServerLevel level, Player quem, Vec3 onde, int qual) {
        List<Coven.Membro> bruxas = Coven.of(quem).bruxas();
        if (qual < 0 || qual >= bruxas.size()) return false;
        Coven.Membro membro = bruxas.get(qual);
        if (membro.nome().isEmpty()) return false;

        CovenWitchEntity bruxa = porPerto(level, quem, membro.nome());
        boolean nova = bruxa == null;
        if (nova) {
            bruxa = OccultaEntities.COVEN_WITCH.create(level, EntitySpawnReason.TRIGGERED);
            if (bruxa == null) return false;
            bruxa.vesteRetrato(membro);
            bruxa.põeDono(quem.getUUID());
        }

        BlockPos caldeirão = caldeirãoPerto(level, onde);
        Vec3 ali = caldeirão != null ? aoLadoDo(level, caldeirão) : aoLadoDe(level, onde);
        bruxa.snapTo(ali.x, ali.y, ali.z, 0.0f, 0.0f);
        bruxa.getNavigation().stop();
        bruxa.põePrazo(PRAZO);
        if (nova) level.addFreshEntity(bruxa);

        level.sendParticles(ParticleTypes.PORTAL, bruxa.getX(), bruxa.getY() + 1.0, bruxa.getZ(),
                16, 1.0, 2.0, 1.0, 0.0);
        level.playSound(null, bruxa.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL,
                1.0f, 1.0f);
        return true;
    }

    /** A bruxa daquele nome que já esteja por perto e seja de quem chama. */
    private static @Nullable CovenWitchEntity porPerto(ServerLevel level, Player quem, String nome) {
        AABB caixa = new AABB(quem.getX() - LONGE_XZ, quem.getY() - LONGE_Y, quem.getZ() - LONGE_XZ,
                quem.getX() + LONGE_XZ, quem.getY() + LONGE_Y, quem.getZ() + LONGE_XZ);
        for (CovenWitchEntity bruxa : level.getEntitiesOfClass(CovenWitchEntity.class, caixa)) {
            if (!quem.getUUID().equals(bruxa.dono())) continue;
            if (!nome.equalsIgnoreCase(bruxa.comoSeChama())) continue;
            return bruxa;
        }
        return null;
    }

    /** O Caldeirão de Bruxa mais perto, se houver um a nove blocos. */
    private static @Nullable BlockPos caldeirãoPerto(ServerLevel level, Vec3 onde) {
        BlockPos meio = BlockPos.containing(onde);
        int raio = (int) CALDEIRÃO;
        BlockPos melhor = null;
        double perto = CALDEIRÃO * CALDEIRÃO;
        for (BlockPos casa : BlockPos.betweenClosed(meio.offset(-raio, -raio, -raio),
                meio.offset(raio, raio, raio))) {
            if (!level.getBlockState(casa).is(net.thaumcraft.occulta.OccultaBlocks.WITCHES_CAULDRON)) {
                continue;
            }
            double quanto = casa.distToCenterSqr(onde);
            if (quanto > perto) continue;
            perto = quanto;
            melhor = casa.immutable();
        }
        return melhor;
    }

    /**
     * Uma casa num <b>anel</b> à volta do caldeirão: entre uma e três de distância, nunca em cima dele.
     *
     * <p>A conta é a do original, e é um jeito antigo de sortear num anel sem trigonometria: sorteia-se
     * dentro do quadrado pequeno e, passando do meio, empurra-se para fora o dobro do raio interno.
     */
    private static Vec3 aoLadoDo(ServerLevel level, BlockPos caldeirão) {
        int largo = DO_CALDEIRÃO_MAX - DO_CALDEIRÃO_MIN;
        int dx = level.getRandom().nextInt(largo * 2 + 1);
        if (dx > largo) dx += DO_CALDEIRÃO_MIN * 2;
        int dz = level.getRandom().nextInt(largo * 2 + 1);
        if (dz > largo) dz += DO_CALDEIRÃO_MIN * 2;
        return new Vec3(caldeirão.getX() - DO_CALDEIRÃO_MAX + dx,
                caldeirão.getY() + 0.01, caldeirão.getZ() - DO_CALDEIRÃO_MAX + dz);
    }

    /** E, sem caldeirão, ao lado de onde se chamou: uma casa para cada lado. */
    private static Vec3 aoLadoDe(ServerLevel level, Vec3 onde) {
        return new Vec3(onde.x + level.getRandom().nextInt(3) - 1.0,
                onde.y + 0.01, onde.z + level.getRandom().nextInt(3) - 1.0);
    }

    /**
     * Um passo de rito que chama <b>uma</b> bruxa para uma casa certa à volta do círculo: o
     * {@code StepSummonCovenMemeber} do original.
     *
     * <p>Eles vão <b>à frente</b> dos passos do rito, de modo que o rito começa pelo coven chegando — e
     * só depois faz o que veio fazer. É por isso que um rito feito com a Pedra do Vidente na mão demora
     * mais: ele espera as bruxas.
     */
    public record Chegada(int qual, int dx, int dz) implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            if (ticks % A_CADA != 0L) return Result.STARTING;
            Player quem = rito.starter(level);
            if (quem != null) {
                chama(level, quem, Vec3.atBottomCenterOf(onde.offset(this.dx, 0, this.dz)), this.qual);
            }
            return Result.COMPLETED;
        }
    }

    /** Os passos de chegada de um coven daquele tamanho, pela ordem das seis casas. */
    public static List<RiteStep> chegadas(int quantas) {
        int quantos = Math.min(quantas, À_VOLTA.length);
        java.util.List<RiteStep> passos = new java.util.ArrayList<>(quantos);
        for (int qual = 0; qual < quantos; qual++) {
            passos.add(new Chegada(qual, À_VOLTA[qual][0], À_VOLTA[qual][1]));
        }
        return List.copyOf(passos);
    }
}
