package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.thaumcraft.occulta.village.VillageWall;

import java.util.List;

/**
 * A planta da muralha: ruas engordadas, vãos fechados, miolo apagado e portões nas pontas.
 *
 * <p>Estas provas não escrevem um bloco no mundo. A muralha de verdade cerca uma aldeia inteira — centenas de
 * blocos de lado —, e não cabe numa arena de oito por oito; mas <b>a forma dela sai toda da planta</b>, e a
 * planta é uma conta pura. É nela que estão os erros que importam: um contorno que se feche por dentro, um
 * miolo que não se apague, um portão que não dê para a rua.
 */
public class OccultaVillageWallGameTest {
    /** Uma rua reta, para o caso simples. */
    private static VillageWall.Faixa rua(int x1, int z1, int x2, int z2) {
        return VillageWall.Faixa.de(new BoundingBox(x1, 60, z1, x2, 64, z2));
    }

    /**
     * Uma rua sozinha vira uma mancha com borda e miolo.
     *
     * <p>A rua é engordada sete além das pontas e vinte para os lados; o que fica no meio dessa mancha é miolo
     * e some, e o que fica na beira é a muralha.
     */
    @GameTest(maxTicks = 20)
    public void oneStreetBecomesARingAroundIt(GameTestHelper helper) {
        var planta = VillageWall.planta(List.of(rua(0, 0, 40, 4)));
        if (planta == null) {
            helper.fail("uma rua devia dar planta");
            return;
        }
        byte[][] mapa = planta.mapa();

        int borda = 0, miolo = 0, portão = 0;
        for (byte[] coluna : mapa) {
            for (byte célula : coluna) {
                if (célula == VillageWall.CELULA_BORDA) borda++;
                else if (célula == VillageWall.CELULA_MIOLO) miolo++;
                else if (célula == VillageWall.CELULA_PORTAO) portão++;
            }
        }

        if (miolo == 0) helper.fail("o miolo da mancha devia ser apagado");
        if (borda == 0) helper.fail("devia sobrar borda, que é onde a muralha se levanta");
        if (miolo <= borda) {
            helper.fail("numa mancha grande o miolo é maior que a borda: miolo=" + miolo
                    + " borda=" + borda);
        }
        if (portão != 6) {
            helper.fail("uma rua leste-oeste tem três células de portão em cada ponta, logo seis; tem "
                    + portão);
        }
        helper.succeed();
    }

    /**
     * E o portão fica mesmo <b>na ponta da rua</b>, não em qualquer lugar da borda.
     *
     * <p>É o que faz a muralha abrir-se onde a estrada sai. Um portão no meio de um lado seria um buraco.
     */
    @GameTest(maxTicks = 20)
    public void theGateSitsWhereTheStreetLeaves(GameTestHelper helper) {
        // uma rua leste-oeste de x=0 a x=40, centrada em z=2
        var planta = VillageWall.planta(List.of(rua(0, 0, 40, 4)));
        if (planta == null) {
            helper.fail("uma rua devia dar planta");
            return;
        }
        byte[][] mapa = planta.mapa();

        for (int x = 0; x < mapa.length; x++) {
            for (int z = 0; z < mapa[x].length; z++) {
                if (mapa[x][z] != VillageWall.CELULA_PORTAO) continue;
                int mundoX = planta.minX() + x - 1;
                int mundoZ = planta.minZ() + z - 1;
                // a rua engordada vai de x = -7 a x = 47, e o portão só pode estar nessas duas pontas
                if (mundoX != -7 && mundoX != 47) {
                    helper.fail("o portão devia estar numa ponta da rua, está em x=" + mundoX);
                }
                // e no meio da rua JÁ ENGORDADA, que vai de z = -20 a z = 24.
                //
                // O meio dela seria 2, e o portão fica em 0..2: a conta do original é
                // "altura / 2 + mínimo - 1", e esse -1 com a divisão inteira cai um bloco antes do
                // centro. É o original, e vai assim — foi esta prova que mo mostrou, porque eu tinha
                // escrito aqui a conta que achava certa em vez da que o original faz.
                if (mundoZ < 0 || mundoZ > 2) {
                    helper.fail("o portão devia estar no meio da rua engordada, está em z=" + mundoZ);
                }
            }
        }
        helper.succeed();
    }

    /**
     * Duas ruas que se cruzam dão <b>uma</b> mancha, e não duas.
     *
     * <p>É o fechar de vãos que faz isso, e sem ele a aldeia sairia com duas muralhas soltas por dentro.
     */
    @GameTest(maxTicks = 20)
    public void twoCrossingStreetsBecomeOneBlob(GameTestHelper helper) {
        var planta = VillageWall.planta(List.of(rua(0, 0, 40, 4), rua(18, -20, 22, 24)));
        if (planta == null) {
            helper.fail("duas ruas deviam dar planta");
            return;
        }
        byte[][] mapa = planta.mapa();

        // conta as manchas de células não vazias
        boolean[][] visto = new boolean[mapa.length][mapa[0].length];
        int manchas = 0;
        for (int x = 0; x < mapa.length; x++) {
            for (int z = 0; z < mapa[x].length; z++) {
                if (mapa[x][z] == VillageWall.CELULA_VAZIA || visto[x][z]) continue;
                manchas++;
                var fila = new java.util.ArrayDeque<int[]>();
                fila.add(new int[]{x, z});
                visto[x][z] = true;
                while (!fila.isEmpty()) {
                    int[] onde = fila.poll();
                    for (int[] passo : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        int nx = onde[0] + passo[0], nz = onde[1] + passo[1];
                        if (nx < 0 || nz < 0 || nx >= mapa.length || nz >= mapa[0].length) continue;
                        if (visto[nx][nz] || mapa[nx][nz] == VillageWall.CELULA_VAZIA) continue;
                        visto[nx][nz] = true;
                        fila.add(new int[]{nx, nz});
                    }
                }
            }
        }

        if (manchas != 1) helper.fail("duas ruas que se cruzam dão uma mancha só, deram " + manchas);
        helper.succeed();
    }

    /** E sem rua nenhuma não há planta — uma aldeia sem estrada não ganha muralha. */
    @GameTest(maxTicks = 20)
    public void noStreetsMeansNoWall(GameTestHelper helper) {
        if (VillageWall.planta(List.of()) != null) helper.fail("sem rua não há muralha");
        helper.succeed();
    }
}
