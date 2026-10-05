package net.thaumcraft.occulta.divine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * <b>Vais cair</b>: a {@code PredictionFall} do Witchery.
 *
 * <p>É a profecia que melhor mostra o que este ramo faz. Enquanto está em dia, ela só espera: qualquer queda
 * a cumpre. Passado o prazo, ela <b>abre o chão</b>.
 *
 * <p>E abre-o com cuidado: só se os <b>nove blocos</b> debaixo de quem foi avisado forem terra ou grama, e só
 * se ele estiver acima da altura oito. Então põe <b>cascalho</b> nos nove de cima — que é o aviso, e quem o
 * vir tem meio segundo para sair — e <b>esvazia seis blocos</b> por baixo deles.
 *
 * <p>O cascalho é o detalhe de quem sabia o que estava fazendo: ele cai sozinho, e por isso a armadilha
 * funciona mesmo que quem a leve esteja parado.
 */
public class FallPrediction extends Prediction {
    /** Quanto fundo é o buraco. */
    public static final int FUNDO = 6;

    /**
     * E a <b>altura acima do fundo do mundo</b> abaixo da qual ela não se atreve.
     *
     * <p>O original escreve {@code y > 8} à letra, porque o mundo dele acabava no <b>zero</b> e oito blocos
     * de pedra eram a margem que ele queria deixar por baixo do buraco. O mundo de hoje acaba em menos
     * sessenta e quatro, e oito à letra proibiria a profecia em quase toda a parte — por isso o que se mede
     * aqui é a <b>distância ao fundo</b>, que é o que o oito queria dizer.
     */
    public static final int CHÃO = 8;

    /**
     * O chão que ela sabe abrir: terra e grama, que eram os dois materiais que o original aceitava.
     *
     * <p>É um rótulo próprio e não o {@code #minecraft:dirt}, que na versão de hoje são <b>três blocos</b> e
     * não inclui a grama. O rótulo está em {@code data/thaumcraft/tags/block/soft_ground.json}.
     */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> MOLE =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    net.thaumcraft.Thaumcraft.id("soft_ground"));

    public FallPrediction(int id, int peso, double forçaPorBatida, String recado) {
        super(id, peso, forçaPorBatida, recado);
    }

    @Override
    public boolean força(ServerLevel level, ServerPlayer quem) {
        return abre(level, quem.blockPosition().below(), FUNDO, CHÃO, Blocks.AIR);
    }

    /**
     * Põe cascalho nos nove e esvazia — ou enche — o que está por baixo.
     *
     * <p>A mesma conta serve à queda e à água: muda só o que vai no fundo, quão fundo ele é e a partir de
     * que altura ela se atreve.
     */
    static boolean abre(ServerLevel level, BlockPos chão, int fundo, int altura, Block oquê) {
        if (chão.getY() - level.getMinY() <= altura) return false;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!level.getBlockState(chão.offset(x, 0, z)).is(MOLE)) return false;
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y > -fundo; y--) {
                    BlockPos ali = chão.offset(x, y, z);
                    level.setBlockAndUpdate(ali, y == 0
                            ? Blocks.GRAVEL.defaultBlockState() : oquê.defaultBlockState());
                }
            }
        }
        return true;
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, DamageSource fonte, boolean atrasada,
                            boolean velha) {
        return fonte.is(DamageTypes.FALL);
    }
}
