package net.thaumcraft.arcana.power;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * O caminho de um nó a outro: o {@code AStar} e o {@code PowerNodePathfinder} da 1.4.0.009, juntos.
 *
 * <p>Anda de nó em nó, nunca mais de <b>dez casas</b> por salto, e só passa por quem deixa passar. Começa
 * na <b>fonte</b> e procura o <b>destino</b>.
 *
 * <p><b>Uma particularidade do original que fica tal e qual.</b> Isto chama-se A*, mas o palpite dele não
 * olha para a chegada: tanto o custo ({@code g}) como o palpite ({@code h}) são a distância <b>do nó atual
 * ao próximo</b>. Ou seja, o palpite é o custo outra vez, e o que isto faz na prática é um Dijkstra com o
 * peso de cada salto a dobrar. Dá o mesmo caminho — o mais curto —, só não poupa trabalho nenhum. Como as
 * redes são de poucas dezenas de nós, isso nunca se nota, e mudá-lo seria mudar o original sem motivo.
 */
public class PowerPathfinder {
    private final ServerLevel level;
    private final BlockPos destino;
    private final PowerType tipo;

    public PowerPathfinder(ServerLevel level, BlockPos destino, PowerType tipo) {
        this.level = level;
        this.destino = destino;
        this.tipo = tipo;
    }

    /** Um passo do caminho e o que ele custou até aqui. */
    private static final class Passo implements Comparable<Passo> {
        final BlockPos onde;
        final Passo pai;
        double g;
        double f;

        Passo(BlockPos onde, Passo pai) {
            this.onde = onde;
            this.pai = pai;
        }

        @Override
        public int compareTo(Passo outro) {
            return Double.compare(this.f, outro.f);
        }
    }

    /** O caminho da fonte ao destino, ou nada se não houver. */
    public List<BlockPos> compute(BlockPos fonte) {
        PriorityQueue<Passo> fila = new PriorityQueue<>();
        Map<BlockPos, Double> melhor = new HashMap<>();

        Passo raiz = new Passo(fonte, null);
        custo(raiz, fonte, fonte);
        expande(raiz, fila, melhor);

        while (true) {
            Passo passo = fila.poll();
            if (passo == null) return null;
            if (passo.onde.equals(this.destino)) {
                LinkedList<BlockPos> caminho = new LinkedList<>();
                for (Passo volta = passo; volta != null; volta = volta.pai) caminho.addFirst(volta.onde);
                return caminho;
            }
            expande(passo, fila, melhor);
        }
    }

    private void expande(Passo passo, PriorityQueue<Passo> fila, Map<BlockPos, Double> melhor) {
        Double mínimo = melhor.get(passo.onde);
        if (mínimo != null && mínimo <= passo.f) return;
        melhor.put(passo.onde, passo.f);

        for (BlockPos seguinte : sucessores(passo.onde)) {
            Passo novo = new Passo(seguinte, passo);
            custo(novo, passo.onde, seguinte);
            fila.offer(novo);
        }
    }

    /** O {@code f} do original: o custo do pai mais a distância ao quadrado, duas vezes. */
    private static void custo(Passo passo, BlockPos de, BlockPos para) {
        double salto = de.distSqr(para);
        passo.g = salto + (passo.pai != null ? passo.pai.g : 0.0);
        passo.f = passo.g + salto;
    }

    private List<BlockPos> sucessores(BlockPos onde) {
        if (!(this.level.getBlockEntity(onde) instanceof PowerNode)) return List.of();
        List<BlockPos> bons = new ArrayList<>();
        for (BlockPos perto : PowerNodes.of(this.level).nearbyNodes(this.level, onde)) {
            if (serve(perto)) bons.add(perto);
        }
        return bons;
    }

    /**
     * Quem serve de passo: quem deixa passar — e o <b>destino</b>, que não precisa de deixar passar, basta
     * que se sirva deste tipo.
     */
    private boolean serve(BlockPos onde) {
        BlockEntity bloco = this.level.getBlockEntity(onde);
        if (!(bloco instanceof PowerNode nó)) return false;
        if (onde.equals(this.destino)) {
            for (PowerType qual : nó.validPowerTypes()) {
                if (qual == this.tipo) return true;
            }
        }
        return nó.canRelayPower(this.tipo);
    }
}
