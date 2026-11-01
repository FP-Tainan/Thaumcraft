package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaItems;
import net.thaumcraft.arcana.machine.ManaBatteryBlockEntity;
import net.thaumcraft.arcana.machine.ObeliskBlockEntity;
import net.thaumcraft.arcana.machine.ObeliskFuel;
import net.thaumcraft.arcana.machine.WizardChalkBlock;
import net.thaumcraft.arcana.power.PowerNode;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;

/**
 * A <b>rede de Etherium</b>: o {@code PowerNodeRegistry} e companhia da 1.4.0.009.
 *
 * <p>Nada aqui se liga por encostar. Dois nós ligam-se porque alguém foi lá com a Chave de Cristal e
 * disse daqui e depois para ali — e é esse caminho guardado que a energia percorre, de nó em nó, de vinte
 * em vinte batidas.
 */
public class ArcanaPowerGameTest {
    /** <b>Os três Etheriums são três</b>, e com os números do original. */
    @GameTest(maxTicks = 20)
    public void thereAreThreePowerTypes(GameTestHelper helper) {
        if (PowerType.ALL.length != 3) {
            helper.fail("o original tem três tipos: contei " + PowerType.ALL.length);
            return;
        }
        if (PowerType.LIGHT.id() != 1 || PowerType.NEUTRAL.id() != 2 || PowerType.DARK.id() != 4) {
            helper.fail("os números são bandeiras de bit: 1, 2 e 4");
            return;
        }
        for (PowerType qual : PowerType.ALL) {
            if (PowerType.byId(qual.id()) != qual) {
                helper.fail("o " + qual + " não se acha pelo número dele");
                return;
            }
        }
        if (PowerType.byId(3) != null) {
            helper.fail("um número que não é de nenhum não devia dar tipo nenhum");
            return;
        }
        helper.succeed();
    }

    /** <b>Quem entra na rede entra ao ser posto, e sai ao ser tirado.</b> */
    @GameTest(maxTicks = 20)
    public void anodeJoinsAndLeavesTheNetwork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relativo = new BlockPos(1, 2, 1);
        BlockPos onde = helper.absolutePos(relativo);
        PowerNodes rede = PowerNodes.of(level);

        helper.setBlock(relativo, ArcanaBlocks.MANA_BATTERY);
        if (!rede.knows(onde)) {
            helper.fail("a bateria posta devia estar na rede");
            return;
        }
        helper.setBlock(relativo, Blocks.AIR);
        if (rede.knows(onde)) {
            helper.fail("a bateria tirada devia sair da rede");
            return;
        }
        helper.succeed();
    }

    /** <b>O Obelisco só queima o que o original manda queimar</b>: pó de vinteum, duzentas batidas. */
    @GameTest(maxTicks = 20)
    public void theObeliskBurnsVinteumDustAndNothingElse(GameTestHelper helper) {
        if (ObeliskFuel.burnTime(new ItemStack(ArcanaItems.MATERIALS.get("vinteum_dust"))) != ObeliskFuel.VINTEUM_DUST) {
            helper.fail("o pó de vinteum queima duzentas batidas");
            return;
        }
        if (ObeliskFuel.burnTime(new ItemStack(net.minecraft.world.item.Items.COAL)) != 0) {
            helper.fail("carvão não é combustível de obelisco");
            return;
        }
        if (ObeliskFuel.burnTime(ItemStack.EMPTY) != 0) {
            helper.fail("nada não queima");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O Obelisco queima e enche-se de Neutro.</b>
     *
     * <p>Cinco por batida, sem nada em volta. Ele não pede a ninguém e não dá a ninguém sem que lhe
     * peçam: o que se vê aqui é só ele a encher-se.
     */
    @GameTest(maxTicks = 80)
    public void theObeliskFillsItselfWithNeutral(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos relativo = new BlockPos(2, 2, 2);
        BlockPos onde = helper.absolutePos(relativo);
        helper.setBlock(relativo, ArcanaBlocks.OBELISK);
        ObeliskBlockEntity obelisco = helper.getBlockEntity(relativo, ObeliskBlockEntity.class);
        obelisco.setItem(0, new ItemStack(ArcanaItems.MATERIALS.get("vinteum_dust"), 1));

        helper.succeedWhen(() -> {
            float tem = PowerNodes.of(level).getPower(onde, PowerType.NEUTRAL);
            helper.assertTrue(tem >= ObeliskBlockEntity.POWER_BASE * 20.0f,
                    "o obelisco devia estar a encher-se: tem " + tem);
            helper.assertTrue(PowerNodes.of(level).getPower(onde, PowerType.DARK) == 0.0f,
                    "e não devia fazer Trevas");
        });
    }

    /**
     * <b>Ligada com a chave, a Bateria bebe do Obelisco.</b>
     *
     * <p>É a prova de que a rede inteira funciona: a chave desenha o caminho, o pedido corre de vinte em
     * vinte batidas, e o que sai de um aparece no outro.
     */
    @GameTest(maxTicks = 120)
    public void pairedTheBatteryDrinksFromTheObelisk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos fonteRel = new BlockPos(1, 2, 1);
        BlockPos destinoRel = new BlockPos(7, 2, 1);
        BlockPos fonte = helper.absolutePos(fonteRel);
        BlockPos destino = helper.absolutePos(destinoRel);

        helper.setBlock(fonteRel, ArcanaBlocks.OBELISK);
        helper.setBlock(destinoRel, ArcanaBlocks.MANA_BATTERY);
        ObeliskBlockEntity obelisco = helper.getBlockEntity(fonteRel, ObeliskBlockEntity.class);
        obelisco.setItem(0, new ItemStack(ArcanaItems.MATERIALS.get("vinteum_dust"), 8));

        PowerNodes rede = PowerNodes.of(level);
        var recado = rede.tryPairNodes(level, fonte, (PowerNode) level.getBlockEntity(fonte),
                destino, (PowerNode) level.getBlockEntity(destino));
        if (!recado.getString().contains("thaumcraft.power.paired")
                && !recado.toString().contains("thaumcraft.power.paired")) {
            helper.fail("a chave devia ter ligado os dois, disse: " + recado);
            return;
        }

        helper.succeedWhen(() -> {
            float tem = rede.getPower(destino, PowerType.NEUTRAL);
            helper.assertTrue(tem > 0.0f, "a bateria devia ter bebido alguma coisa, tem " + tem);
            ManaBatteryBlockEntity bateria = helper.getBlockEntity(destinoRel, ManaBatteryBlockEntity.class);
            helper.assertTrue(bateria.powerType() == PowerType.NEUTRAL,
                    "e devia ter ficado com o tipo Neutro, ficou com " + bateria.powerType());
        });
    }

    /**
     * <b>Um Conduto passa a energia adiante</b>, e é isso que faz a rede chegar longe.
     *
     * <p>A bateria pede por um caminho que o conduto desenhou, e o conduto não é fonte de nada: o que
     * chega à bateria saiu do obelisco, dois saltos atrás.
     */
    @GameTest(maxTicks = 120)
    public void aConduitPassesItAlong(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos fonteRel = new BlockPos(1, 2, 1);
        BlockPos meioRel = new BlockPos(5, 2, 1);
        BlockPos destinoRel = new BlockPos(9, 2, 1);
        BlockPos fonte = helper.absolutePos(fonteRel);
        BlockPos destino = helper.absolutePos(destinoRel);

        helper.setBlock(fonteRel, ArcanaBlocks.OBELISK);
        helper.setBlock(meioRel, ArcanaBlocks.ESSENCE_CONDUIT);
        helper.setBlock(destinoRel, ArcanaBlocks.MANA_BATTERY);
        ObeliskBlockEntity obelisco = helper.getBlockEntity(fonteRel, ObeliskBlockEntity.class);
        obelisco.setItem(0, new ItemStack(ArcanaItems.MATERIALS.get("vinteum_dust"), 8));

        PowerNodes rede = PowerNodes.of(level);
        rede.tryPairNodes(level, fonte, (PowerNode) level.getBlockEntity(fonte),
                destino, (PowerNode) level.getBlockEntity(destino));
        if (rede.entry(destino).pathCount(PowerType.NEUTRAL) == 0) {
            helper.fail("devia haver um caminho guardado até à bateria");
            return;
        }

        helper.succeedWhen(() -> helper.assertTrue(rede.getPower(destino, PowerType.NEUTRAL) > 0.0f,
                "a bateria devia ter bebido pelo conduto"));
    }

    /** <b>Ninguém se liga a si mesmo</b>, e um conduto não é fonte de coisa nenhuma. */
    @GameTest(maxTicks = 20)
    public void theWrenchRefusesWhatItShould(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos umRel = new BlockPos(1, 2, 1);
        BlockPos outroRel = new BlockPos(4, 2, 1);
        BlockPos um = helper.absolutePos(umRel);
        BlockPos outro = helper.absolutePos(outroRel);
        helper.setBlock(umRel, ArcanaBlocks.ESSENCE_CONDUIT);
        helper.setBlock(outroRel, ArcanaBlocks.MANA_BATTERY);
        PowerNodes rede = PowerNodes.of(level);

        var aSiMesmo = rede.tryPairNodes(level, um, (PowerNode) level.getBlockEntity(um),
                um, (PowerNode) level.getBlockEntity(um));
        if (!aSiMesmo.toString().contains("pairToSelf")) {
            helper.fail("ligar um nó a ele mesmo devia ser recusado, disse: " + aSiMesmo);
            return;
        }
        var semTipo = rede.tryPairNodes(level, um, (PowerNode) level.getBlockEntity(um),
                outro, (PowerNode) level.getBlockEntity(outro));
        if (!semTipo.toString().contains("noSupportedTypes")) {
            helper.fail("um conduto não dá energia a ninguém, disse: " + semTipo);
            return;
        }
        helper.succeed();
    }

    /** <b>E a chave corta</b>: desligar apaga todos os caminhos daquele nó. */
    @GameTest(maxTicks = 20)
    public void cuttingForgetsEveryPath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos fonteRel = new BlockPos(1, 2, 1);
        BlockPos destinoRel = new BlockPos(5, 2, 1);
        BlockPos fonte = helper.absolutePos(fonteRel);
        BlockPos destino = helper.absolutePos(destinoRel);
        helper.setBlock(fonteRel, ArcanaBlocks.OBELISK);
        helper.setBlock(destinoRel, ArcanaBlocks.MANA_BATTERY);

        PowerNodes rede = PowerNodes.of(level);
        rede.tryPairNodes(level, fonte, (PowerNode) level.getBlockEntity(fonte),
                destino, (PowerNode) level.getBlockEntity(destino));
        if (rede.entry(destino).pathCount(PowerType.NEUTRAL) != 1) {
            helper.fail("devia haver um caminho");
            return;
        }
        rede.tryDisconnectAllNodes(destino);
        if (rede.entry(destino).pathCount(PowerType.NEUTRAL) != 0) {
            helper.fail("depois de cortar não devia sobrar caminho nenhum");
            return;
        }
        helper.succeed();
    }

    /**
     * <b>O que está em volta do Obelisco conta.</b>
     *
     * <p>Sem nada, um; com o anel de giz, <b>1,25</b>; com os quatro pilares e os topos cinzelados,
     * vezes <b>1,35</b> — oito e pouco por batida em vez de cinco.
     */
    @GameTest(maxTicks = 20)
    public void whatIsAroundTheObeliskMultipliesIt(GameTestHelper helper) {
        BlockPos meio = new BlockPos(3, 2, 3);
        helper.setBlock(meio, ArcanaBlocks.OBELISK);
        ObeliskBlockEntity obelisco = helper.getBlockEntity(meio, ObeliskBlockEntity.class);

        obelisco.checkNearbyBlockState();
        if (obelisco.powerMultiplier() != 1.0f) {
            helper.fail("pelado, o obelisco multiplica por um: deu " + obelisco.powerMultiplier());
            return;
        }

        // o anel de nove quadrados de giz, menos o do meio, que é o próprio obelisco
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                helper.setBlock(meio.offset(i, 0, j), ArcanaBlocks.WIZARD_CHALK.defaultBlockState()
                        .setValue(WizardChalkBlock.VARIANT, 0));
            }
        }
        obelisco.checkNearbyBlockState();
        if (Math.abs(obelisco.powerMultiplier() - ObeliskBlockEntity.CHALK_BONUS) > 1.0e-5f) {
            helper.fail("com o giz devia dar 1,25: deu " + obelisco.powerMultiplier());
            return;
        }

        // e os quatro pilares de dois, com o tijolo cinzelado por cima
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                helper.setBlock(meio.offset(x, 0, z), Blocks.STONE_BRICKS);
                helper.setBlock(meio.offset(x, 1, z), Blocks.STONE_BRICKS);
                helper.setBlock(meio.offset(x, 2, z), Blocks.CHISELED_STONE_BRICKS);
            }
        }
        obelisco.checkNearbyBlockState();
        float esperado = ObeliskBlockEntity.CHALK_BONUS * ObeliskBlockEntity.CHISELED_CAPS;
        if (Math.abs(obelisco.powerMultiplier() - esperado) > 1.0e-5f) {
            helper.fail("com giz e pilares devia dar " + esperado + ": deu " + obelisco.powerMultiplier());
            return;
        }
        helper.succeed();
    }

    /**
     * <b>Um caminho partido deixa marca.</b>
     *
     * <p>Tirando o conduto do meio, o pedido seguinte encontra o buraco e põe lá um Elo Partido — que é
     * como quem joga descobre onde a rede se rompeu.
     */
    @GameTest(maxTicks = 120)
    public void abrokenPathLeavesAMark(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos fonteRel = new BlockPos(1, 2, 1);
        BlockPos meioRel = new BlockPos(5, 2, 1);
        BlockPos destinoRel = new BlockPos(9, 2, 1);
        BlockPos fonte = helper.absolutePos(fonteRel);
        BlockPos destino = helper.absolutePos(destinoRel);

        helper.setBlock(fonteRel, ArcanaBlocks.OBELISK);
        helper.setBlock(meioRel, ArcanaBlocks.ESSENCE_CONDUIT);
        helper.setBlock(destinoRel, ArcanaBlocks.MANA_BATTERY);
        ObeliskBlockEntity obelisco = helper.getBlockEntity(fonteRel, ObeliskBlockEntity.class);
        obelisco.setItem(0, new ItemStack(ArcanaItems.MATERIALS.get("vinteum_dust"), 8));

        PowerNodes rede = PowerNodes.of(level);
        rede.tryPairNodes(level, fonte, (PowerNode) level.getBlockEntity(fonte),
                destino, (PowerNode) level.getBlockEntity(destino));
        helper.setBlock(meioRel, Blocks.AIR);

        helper.succeedWhen(() -> helper.assertBlockPresent(ArcanaBlocks.BROKEN_POWER_LINK, meioRel));
    }
}
