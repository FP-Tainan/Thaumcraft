package net.thaumcraft.occulta.divine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * <b>Vais achar muito {@code X}</b>: a {@code PredictionMultiMine} do Witchery.
 *
 * <p>Quem foi avisado parte o bloco certo e dele cai <b>um punhado a mais</b> — oito a vinte de ferro, um
 * diamante, uma esmeralda, dez a vinte de carvão.
 *
 * <p><b>O que cai se guarda como item e não como pilha</b>, porque a lista das dezessete é montada quando
 * a classe carrega, e nessa altura uma pilha ainda não se pode fazer: os componentes dos itens só se ligam
 * depois. A pilha se faz na hora de cair.
 *
 * <p>E há um detalhe que só se percebe depois de meia hora: passada a <b>muito velha</b>, ela deixa de pedir
 * o bloco certo e passa a aceitar <b>pedra qualquer</b>. Quer dizer que a profecia do diamante, se ninguém a
 * cumprir, acaba por dar um diamante a quem estiver cavando um túnel — e quanto mais ele demora a achar um,
 * mais perto está de lhe cair um na mão. É a profecia dizendo: eu avisei.
 */
public class MultiMinePrediction extends AlwaysForcedPrediction {
    private final Block qual;
    private final Item oquê;
    private final int mínimo;
    private final int máximo;

    public MultiMinePrediction(int id, int peso, double forçaPorBatida, String recado, int prazo,
                               double emDia, Block qual, Item oquê, int mínimo, int máximo) {
        super(id, peso, forçaPorBatida, recado, prazo, emDia);
        this.qual = qual;
        this.oquê = oquê;
        this.mínimo = mínimo;
        this.máximo = máximo;
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, BlockState feitio, BlockPos onde,
                            List<ItemStack> cai, boolean atrasada, boolean velha) {
        boolean éoque = feitio.is(this.qual) || (velha && feitio.is(Blocks.STONE));
        if (!éoque || !this.évez(level, atrasada)) return false;

        int escolha = this.máximo - this.mínimo;
        int quantos = this.mínimo
                + (escolha > 1 ? level.getRandom().nextInt(escolha) + 1 : escolha);
        for (int volta = 0; volta < quantos; volta++) {
            cai.add(new ItemStack(this.oquê));
        }
        return true;
    }
}
