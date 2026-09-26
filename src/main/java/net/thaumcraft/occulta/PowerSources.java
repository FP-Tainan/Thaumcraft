package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * A lista dos altares que estão de pé, para quem precisa de poder saber onde há: o {@code PowerSources} do
 * Witchery.
 *
 * <p>Cada altar inteiro entra nela ao nascer e sai ao ser desfeito. Quem quer poder pergunta pelo mais perto
 * dentro do alcance dele — dezesseis blocos, ou mais se ele tiver a Arthana posta em cima.
 *
 * <p>No original a lista é uma por lado (uma no servidor, outra no cliente); aqui é só a do servidor, porque é lá
 * que o poder se gasta.
 */
public final class PowerSources {
    private static final List<AltarBlockEntity> SOURCES = new ArrayList<>();

    private PowerSources() {
    }

    public static void register(AltarBlockEntity altar) {
        clean();
        if (!SOURCES.contains(altar)) SOURCES.add(altar);
    }

    public static void remove(AltarBlockEntity altar) {
        SOURCES.remove(altar);
        clean();
    }

    /** Tira da lista o que já não está de pé. */
    private static void clean() {
        SOURCES.removeIf(altar -> altar.isRemoved() || altar.getLevel() == null || !altar.isCore());
    }

    public static int count() {
        clean();
        return SOURCES.size();
    }

    /** O altar mais perto daquele lugar que ainda o alcança, ou nada. */
    public static AltarBlockEntity closest(ServerLevel level, BlockPos onde) {
        clean();
        AltarBlockEntity achado = null;
        double perto = Double.MAX_VALUE;
        for (AltarBlockEntity altar : SOURCES) {
            if (altar.getLevel() != level) continue;
            double distância = altar.getBlockPos().distSqr(onde);
            double alcance = altar.range() * altar.range();
            if (distância > alcance || distância >= perto) continue;
            perto = distância;
            achado = altar;
        }
        return achado;
    }

    /** Tira poder do altar mais perto; devolve se havia. */
    public static boolean consume(ServerLevel level, BlockPos onde, float quanto) {
        AltarBlockEntity altar = closest(level, onde);
        return altar != null && altar.consume(quanto);
    }

    /** Esvazia a lista: serve ao mundo de prova, que abre e fecha muitos mundos. */
    public static void clear() {
        SOURCES.clear();
    }
}
