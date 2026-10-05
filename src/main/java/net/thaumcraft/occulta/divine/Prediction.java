package net.thaumcraft.occulta.divine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Uma <b>profecia</b>: a {@code Prediction} do Witchery.
 *
 * <p>É uma coisa que <b>ainda não aconteceu</b> e que a Bola de Cristal diz que vai acontecer. O que a torna
 * diferente de um aviso é que ela <b>se cumpre</b>: passado o prazo sem que o mundo a tenha cumprido por si,
 * o próprio mod começa a <b>forçá-la</b> — abre um buraco debaixo de quem foi avisado que ia cair, faz nascer
 * um zumbi ao lado de quem foi avisado que ia brigar com um.
 *
 * <p>É a ideia mais bonita deste ramo do Witchery e vale dizê-la por extenso: <b>a profecia não prevê o
 * futuro, ela o fabrica</b>. Quem lê a sorte não está vendo o que vai ser; está decidindo o que vai ser, e o
 * mundo vai atrás. Por isso o original chama ao que faz depois do prazo «cumprimento por si próprio»
 * ({@code selfFulfillment}) — o nome é a piada toda.
 *
 * <h2>Os prazos</h2>
 *
 * <ul>
 *   <li>até <b>9600 batidas</b> (oito minutos) ela está <b>em dia</b>: só se cumpre se o mundo a cumprir;</li>
 *   <li>passado isso fica <b>atrasada</b>, e a cada batida há uma chance de ela se forçar;</li>
 *   <li>passadas <b>36000</b> (meia hora) fica <b>muito velha</b>, e algumas afrouxam o que pedem — a do
 *       ferro passa a aceitar qualquer pedra, por exemplo.</li>
 * </ul>
 *
 * <p>Os ganchos são quatro no original. Aqui são <b>três</b>: o de dano levado, o de batida do jogador e o de
 * bloco partido. O quarto — o de uso de item — <b>nenhuma das dezessete usa</b>, e não foi portado.
 */
public abstract class Prediction {
    /** O número dela, que é o que fica guardado no jogador. */
    public final int id;

    /** O peso dela no sorteio. */
    public final int peso;

    /** A chance, por batida, de ela se forçar depois de atrasada. */
    public final double forçaPorBatida;

    /** A chave do recado que a anuncia. */
    public final String recado;

    protected Prediction(int id, int peso, double forçaPorBatida, String recado) {
        this.id = id;
        this.peso = peso;
        this.forçaPorBatida = forçaPorBatida;
        this.recado = recado;
    }

    /** Quanto tempo ela tem antes de ficar atrasada. */
    public long prazo() {
        return Predictions.PRAZO;
    }

    /** Se é hora de tentar forçá-la. */
    public boolean tentaForçar(ServerLevel level) {
        return level.getRandom().nextDouble() < this.forçaPorBatida;
    }

    /** Força-a. Devolve se conseguiu. */
    public boolean força(ServerLevel level, ServerPlayer quem) {
        return false;
    }

    /** Se ela pode sequer ser dita a este jogador. */
    public boolean possível(ServerLevel level, ServerPlayer quem) {
        return true;
    }

    /** O gancho do dano levado. */
    public boolean cumprida(ServerLevel level, ServerPlayer quem, DamageSource fonte, boolean atrasada,
                            boolean velha) {
        return false;
    }

    /** O gancho da batida do jogador. */
    public boolean cumprida(ServerLevel level, ServerPlayer quem, boolean atrasada, boolean velha) {
        return false;
    }

    /** O gancho do bloco partido, que pode pôr mais coisas no chão. */
    public boolean cumprida(ServerLevel level, ServerPlayer quem, BlockState oquê,
                            net.minecraft.core.BlockPos onde, List<ItemStack> cai, boolean atrasada,
                            boolean velha) {
        return false;
    }
}
