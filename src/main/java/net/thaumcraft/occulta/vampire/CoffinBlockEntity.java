package net.thaumcraft.occulta.vampire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A alma do Caixão, que só serve para uma coisa: fazer a tampa <b>andar</b>.
 *
 * <p>No original é o {@code lidAngle}/{@code prevLidAngle} de um baú — dois números entre zero e um, que
 * correm um atrás do outro —, e o desenhista interpola entre eles com a mesma curva cúbica dos baús do jogo.
 * Sem isso a tampa <b>saltava</b> de fechada a aberta num quadro, e um caixão que salta não assusta ninguém.
 *
 * <p>Ela não guarda nada e não vai para o disco: o que está guardado é a {@linkplain CoffinBlock#ABERTO
 * tampa aberta ou fechada}, que é estado de bloco. Isto é só a animação a caminho de lá.
 */
public class CoffinBlockEntity extends BlockEntity {
    /** Quanto a tampa anda por batida. */
    public static final float PASSO = 0.1f;

    private float tampa;
    private float tampaAntes;

    public CoffinBlockEntity(BlockPos onde, BlockState state) {
        super(net.thaumcraft.occulta.OccultaBlocks.COFFIN_ENTITY, onde, state);
    }

    /** O passo da tampa, uma vez por batida e só do lado de cá. */
    public void anda() {
        this.tampaAntes = this.tampa;
        boolean aberto = this.getBlockState().hasProperty(CoffinBlock.ABERTO)
                && this.getBlockState().getValue(CoffinBlock.ABERTO);
        this.tampa = Math.clamp(this.tampa + (aberto ? PASSO : -PASSO), 0.0f, 1.0f);
    }

    /**
     * O quanto a tampa está aberta neste instante, entre zero e um.
     *
     * <p>A curva é a do baú: {@code 1 - (1-x)³}, que abre depressa e <b>assenta devagar</b> no fim. É o
     * detalhe que faz a tampa parecer pesada.
     */
    public float tampa(float parcial) {
        float anda = this.tampaAntes + (this.tampa - this.tampaAntes) * parcial;
        float um = 1.0f - anda;
        return 1.0f - um * um * um;
    }
}
