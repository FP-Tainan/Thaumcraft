package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Põe um bicho perto de alguém, num anel entre duas distâncias: o {@code Infusion.spawnCreature} do Witchery.
 *
 * <p>É de onde saem o pesadelo de uma maldição, as visões do Espelho, os lobos de um uivo e o <b>Caçador
 * Cornudo</b> que o Chifre da Caça chama. Uma conta só, usada por todos eles.
 *
 * <p>O jeito do original é curioso e vale copiar letra por letra: ele sorteia um número no <b>dobro</b> do
 * vão entre o perto e o longe e, passando da metade, <b>o empurra para fora</b> somando duas vezes o perto. O
 * resultado é um anel — nunca em cima de quem chamou, nunca longe demais — e com o viés que o original tem,
 * que não é um anel perfeito.
 *
 * <p>Depois ele procura o chão: <b>sobe</b> enquanto estiver entupido, no máximo oito blocos, <b>desce</b>
 * enquanto estiver vazio, e mede o <b>vão de ar</b> por cima. Dois blocos bastam — e bastam até para um bicho
 * de três blocos de altura, que é o Caçador. Fica entalado, e é o que o original faz.
 */
public final class Spawn {
    /** O vão de ar que o original exige: dois blocos, e nem mais um. */
    public static final int VÃO = 2;

    /** Quanto ele sobe à procura de ar, e quanto mede de vão. */
    private static final int SOBE_ATÉ = 8;
    private static final int MEDE_ATÉ = 6;

    private Spawn() {
    }

    /**
     * Onde um bicho caberia perto de aqui, ou {@code null} se não há lugar.
     *
     * @param perto a distância mínima, em blocos
     * @param longe a máxima
     */
    @Nullable
    public static BlockPos lugar(ServerLevel level, BlockPos daqui, int perto, int longe) {
        var sorte = level.getRandom();
        int vão = longe - perto;

        int ax = sorte.nextInt(vão * 2 + 1);
        if (ax > vão) ax += perto * 2;
        int nx = daqui.getX() - longe + ax;

        int az = sorte.nextInt(vão * 2 + 1);
        if (az > vão) az += perto * 2;
        int nz = daqui.getZ() - longe + az;

        int ny = daqui.getY();
        while (!level.isEmptyBlock(new BlockPos(nx, ny, nz)) && ny < daqui.getY() + SOBE_ATÉ) ny++;
        while (level.isEmptyBlock(new BlockPos(nx, ny, nz)) && ny > level.getMinY()) ny--;

        int alto = 0;
        while (level.isEmptyBlock(new BlockPos(nx, ny + alto + 1, nz)) && alto < MEDE_ATÉ) alto++;
        return alto >= VÃO ? new BlockPos(nx, ny, nz) : null;
    }

    /**
     * Põe o bicho nesse lugar, se houver um.
     *
     * <p>Ele nasce <b>olhando para o norte</b> e sem o acerto de dificuldade que um bicho do mundo leva: é o
     * que o original faz, porque na 1.7.10 nada disso passava por aqui.
     */
    @Nullable
    public static Entity perto(ServerLevel level, EntityType<?> qual, BlockPos daqui, int perto, int longe) {
        BlockPos onde = lugar(level, daqui, perto, longe);
        if (onde == null) return null;
        Entity bicho = qual.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return null;
        bicho.snapTo(onde.getX() + 0.5, onde.getY() + 1.05, onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(bicho);
        return bicho;
    }

    /** O meio do bicho que acabou de nascer, para os pós e o barulho irem onde ele está. */
    public static Vec3 meio(Entity bicho) {
        return new Vec3(bicho.getX(), bicho.getY() + bicho.getBbHeight() / 2.0, bicho.getZ());
    }
}
