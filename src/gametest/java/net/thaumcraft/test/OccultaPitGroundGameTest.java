package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.OccultaRituals;

/**
 * O <b>chão falso</b>: parece chão, não é chão, e sai do caldeirão.
 */
public class OccultaPitGroundGameTest {
    /**
     * <b>Nada esbarra nele.</b> Um porco posto em cima de uma casa de chão falso <b>cai através dela</b>,
     * e posto em cima de terra de verdade fica onde está.
     */
    @GameTest(maxTicks = 300)
    public void nothingStandsOnTheFakeGround(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos falso = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos certo = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlockAndUpdate(falso, OccultaBlocks.PIT_DIRT.defaultBlockState());
        level.setBlockAndUpdate(certo, Blocks.DIRT.defaultBlockState());

        var caixa = level.getBlockState(falso).getCollisionShape(level, falso);
        if (!caixa.isEmpty()) helper.fail("o chão falso não tem com que esbarrar");
        if (level.getBlockState(certo).getCollisionShape(level, certo).isEmpty()) {
            helper.fail("e a terra de verdade tem");
        }

        /*
         * Os dois porcos caem de três casas acima. O que tem chão falso por baixo <b>passa através
         * dele</b> e vai ao chão da arena; o que tem terra de verdade <b>para em cima dela</b>. O que se
         * mede é a diferença entre os dois, que é a altura do bloco.
         */
        var cai = helper.spawn(EntityTypes.PIG, new BlockPos(2, 5, 2));
        var fica = helper.spawn(EntityTypes.PIG, new BlockPos(4, 5, 2));

        /*
         * <b>A conta é de altura, e espera-se por ela em vez de a marcar no relógio.</b>
         *
         * <p>Esta prova falhava de vez em quando, e sempre na corrida rápida. Primeiro media o
         * {@code onGround}, que pisca em quem acabou de pousar; passou a medir <b>onde o porco está</b>,
         * que não pisca. E continuava a falhar, por outra razão: perguntava a uma <b>batida marcada</b>,
         * a oitenta, e numa corrida com muitas arenas ao mesmo tempo o pedaço de mundo do porco pode
         * ainda nem estar a correr — o porco não caiu porque <b>não teve batidas</b>, e não porque o chão
         * o segurou.
         *
         * <p>Agora ela <b>espera</b>: pergunta a cada batida até o porco ter pousado, e só desiste ao fim
         * do teto da prova. É a diferença entre perguntar «já?» e dizer «agora».
         */
        double topo = helper.absolutePos(new BlockPos(4, 3, 2)).getY();
        helper.succeedWhen(() -> {
            if (fica.getY() - cai.getY() < 0.9) {
                helper.fail("quem pisa no chão falso cai através dele, e os dois ficaram a "
                        + (fica.getY() - cai.getY()));
            }
            if (fica.getY() < topo - 0.05) {
                helper.fail("e quem pisa na terra fica em cima dela, e está a " + fica.getY()
                        + " com a terra a " + topo);
            }

            cai.discard();
            fica.discard();
            level.setBlockAndUpdate(falso, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(certo, Blocks.AIR.defaultBlockState());
        });
    }

    /** <b>E ele sai do caldeirão</b>, quatro de cada vez, pelas duas receitas do original. */
    @GameTest(maxTicks = 20)
    public void bothPitGroundsComeOutOfTheCauldron(GameTestHelper helper) {
        boolean grama = false;
        boolean terra = false;
        for (var receita : OccultaRituals.all()) {
            if (receita.key() != OccultaItems.TORMENTED_TWINE) continue;
            if (receita.result().is(OccultaItems.PIT_GRASS)) {
                if (receita.result().getCount() != 4) helper.fail("a grama sai quatro de cada vez");
                grama = true;
            }
            if (receita.result().is(OccultaItems.PIT_DIRT)) {
                if (receita.result().getCount() != 4) helper.fail("e a terra também");
                terra = true;
            }
        }
        if (!grama) helper.fail("a Linha Atormentada faz grama de armadilha");
        if (!terra) helper.fail("e terra de armadilha");
        helper.succeed();
    }
}
