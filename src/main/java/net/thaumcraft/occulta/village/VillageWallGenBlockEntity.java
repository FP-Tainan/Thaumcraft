package net.thaumcraft.occulta.village;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * O que levanta a muralha, e depois se apaga.
 *
 * <p>É o {@code TileEntityVillageWallGen} do Witchery. A muralha não é um prédio: ela precisa saber <b>onde a
 * aldeia acaba</b>, e isso só se sabe depois de a aldeia estar desenhada. Por isso a peça da muralha é só um
 * marcador — um bloco invisível que espera, olha em volta, desenha e some.
 *
 * <p><b>Quarenta tiques de espera</b>, que são os do original: tempo de os trechos vizinhos acabarem de nascer.
 * E, como lá, se ao fim de mil tiques ainda não houver aldeia à volta, ele desiste e some na mesma — um
 * marcador esquecido num mundo é pior do que uma aldeia sem muralha.
 *
 * <p><b>Onde isto difere do original, e para melhor:</b> lá o bloco recebia a lista de peças pela mão, de quem
 * o criou. Aqui ele <b>pergunta ao mundo</b> — {@code getStructureWithPieceAt} dá a aldeia inteira a partir da
 * posição dele. Não depende de ninguém lhe entregar nada, e por isso funciona mesmo que o trecho seja
 * carregado de novo mais tarde.
 */
public class VillageWallGenBlockEntity extends BlockEntity {
    /** Quanto espera antes de desenhar, e quanto espera antes de desistir: os do original. */
    private static final int ESPERA = 40;
    private static final int DESISTE = 1000;

    private int tiques;

    public VillageWallGenBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.VILLAGE_WALL_GEN_ENTITY, onde, feitio);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos onde, BlockState feitio,
                            VillageWallGenBlockEntity eu) {
        if (!(level instanceof ServerLevel servidor)) return;
        eu.tiques++;
        if (eu.tiques < ESPERA) return;

        if (eu.tiques <= DESISTE) {
            var ruas = ruasDaAldeia(servidor, onde);
            if (!ruas.isEmpty()) {
                boolean deserto = servidor.getBiome(onde)
                        .is(net.minecraft.world.level.biome.Biomes.DESERT);
                VillageWall.desenha(servidor, ruas, onde.getY(),
                        VillageWall.base(deserto), VillageWall.cerca(deserto),
                        VillageWall.escada(deserto));
            } else if (eu.tiques < DESISTE) {
                return;   // a aldeia ainda pode estar a nascer
            }
        }

        servidor.removeBlock(onde, false);
    }

    /**
     * As ruas da aldeia que está à volta deste bloco.
     *
     * <p><b>Só as ruas</b>, como no original: é delas que a mancha da aldeia sai, e as casas ficam todas
     * dentro. Lá a peça de rua era a classe {@code Path}; hoje a aldeia é de encaixe e o que a distingue é o
     * molde dela viver em {@code village/<variante>/streets/}. <b>Desvio declarado:</b> reconhecer uma rua pelo
     * nome do molde é mais frágil do que por tipo, e é o que o jogo de hoje dá.
     */
    private static List<VillageWall.Faixa> ruasDaAldeia(ServerLevel level, BlockPos onde) {
        List<VillageWall.Faixa> ruas = new ArrayList<>();
        var aldeia = level.structureManager().getStructureWithPieceAt(onde, StructureTags.VILLAGE);
        if (aldeia == null || !aldeia.isValid()) return ruas;

        for (var peça : aldeia.getPieces()) {
            if (peça instanceof PoolElementStructurePiece encaixada
                    && encaixada.getElement().toString().contains("/streets/")) {
                ruas.add(VillageWall.Faixa.de(peça.getBoundingBox()));
            }
        }
        return ruas;
    }

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        saída.putInt("Tiques", this.tiques);
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.tiques = entrada.getIntOr("Tiques", 0);
    }
}
