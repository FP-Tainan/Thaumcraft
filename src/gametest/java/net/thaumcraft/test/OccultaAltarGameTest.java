package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.occulta.AltarBlock;
import net.thaumcraft.occulta.AltarBlockEntity;
import net.thaumcraft.occulta.AltarPower;
import net.thaumcraft.occulta.ChaliceBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PowerSources;

/**
 * O Altar da Bruxa: quando seis pedras viram altar, o poder que a natureza em volta lhe dá e o que os enfeites
 * somam.
 */
public class OccultaAltarGameTest {
    /** Seis pedras, duas por três, fazem um altar; cinco não fazem. */
    @GameTest
    public void sixStonesMakeAnAltar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));

        // cinco ainda não bastam
        for (int volta = 0; volta < 5; volta++) {
            level.setBlockAndUpdate(canto.offset(volta % 3, 0, volta / 3),
                    OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        }
        if (core(helper, canto) != null) helper.fail("cinco pedras não fazem altar");

        // a sexta fecha o altar
        level.setBlockAndUpdate(canto.offset(2, 0, 1), OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        AltarBlockEntity manda = core(helper, canto);
        if (manda == null) {
            helper.fail("seis pedras, duas por três, fazem altar");
            return;
        }
        if (!level.getBlockState(canto).getValue(AltarBlock.JOINED)) {
            helper.fail("e a pedra muda de cara quando o altar fecha");
        }
        if (manda.pieces(level).size() != AltarBlock.PIECES) {
            helper.fail("o altar é de seis pedras; achou " + manda.pieces(level).size());
        }

        // tirando uma, desfaz-se
        level.setBlockAndUpdate(canto.offset(2, 0, 1), Blocks.AIR.defaultBlockState());
        if (core(helper, canto) != null) helper.fail("tirando uma pedra, o altar se desfaz");
        if (level.getBlockState(canto).getValue(AltarBlock.JOINED)) helper.fail("e a cara volta ao que era");
        helper.succeed();
    }

    /** Uma fila de sete não faz altar: cada pedra tem de ter dois ou três vizinhos. */
    @GameTest
    public void aRowIsNotAnAltar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        for (int volta = 0; volta < 6; volta++) {
            level.setBlockAndUpdate(canto.offset(volta, 0, 0), OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        }
        if (core(helper, canto) != null) helper.fail("uma fila de seis não faz altar");
        helper.succeed();
    }

    /** O altar conta o que há de natureza em volta, e é daí que sai o teto de poder dele. */
    @GameTest(maxTicks = 200)
    public void theAltarDrawsPowerFromNature(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        AltarBlockEntity manda = build(helper, canto);
        if (manda == null) return;

        // o mundo de prova é pedra: o teto começa baixo
        manda.refresh();
        float antes = manda.maxPower();

        // um punhado de folhagem em volta sobe o teto: três por folha
        int folhas = 12;
        for (int volta = 0; volta < folhas; volta++) {
            level.setBlockAndUpdate(canto.offset(volta % 4, 2, volta / 4),
                    OccultaBlocks.ROWAN_LEAVES.defaultBlockState());
        }
        manda.refresh();
        float depois = manda.maxPower();
        if (depois - antes != folhas * 3) {
            helper.fail("doze folhagens valem trinta e seis de teto; subiu " + (depois - antes));
        }

        // e o poder sobe dez por segundo até bater no teto
        helper.succeedWhen(() -> {
            if (manda.power() <= 0.0f) throw helper.assertionException("o altar ainda não juntou poder");
            if (manda.power() > manda.maxPower()) helper.fail("e nunca passa do teto");
        });
    }

    /** A caveira e a tocha em cima do altar somam ao teto e à velocidade, como no original. */
    @GameTest
    public void theArtefactsOnTopCount(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        AltarBlockEntity manda = build(helper, canto);
        if (manda == null) return;

        manda.refresh();
        if (manda.rechargeScale() != 1) helper.fail("um altar pelado recarrega no passo de sempre");

        level.setBlockAndUpdate(canto.above(), Blocks.SKELETON_SKULL.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 2) {
            helper.fail("a caveira de esqueleto soma um; ficou " + manda.rechargeScale());
        }

        level.setBlockAndUpdate(canto.offset(1, 1, 0), Blocks.TORCH.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 3) {
            helper.fail("e a tocha soma outro; ficou " + manda.rechargeScale());
        }

        level.setBlockAndUpdate(canto.above(), Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 4) {
            helper.fail("a do wither soma dois; ficou " + manda.rechargeScale());
        }
        helper.succeed();
    }

    /**
     * <b>O candelabro e o cálice somam mais do que a tocha e a caveira.</b>
     *
     * <p>O candelabro vale <b>dois</b> de velocidade, o dobro de uma tocha — e é o <b>mesmo lugar</b>: o
     * altar conta uma luz só, e quem já tem candelabro não ganha nada por pôr uma tocha ao lado. O cálice
     * vale <b>um</b> de teto vazio e <b>dois</b> cheio, e também se conta um só.
     */
    @GameTest
    public void theCandelabraAndTheChaliceCountToo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        AltarBlockEntity manda = build(helper, canto);
        if (manda == null) return;

        manda.refresh();
        if (manda.rechargeScale() != 1 || manda.powerScale() != 1) {
            helper.fail("um altar pelado vale um de cada");
        }

        level.setBlockAndUpdate(canto.above(), OccultaBlocks.CANDELABRA.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 3) {
            helper.fail("o candelabro soma dois; ficou " + manda.rechargeScale());
        }

        /*
         * E a tocha ao lado não soma por cima dele: o altar conta uma luz só, e a que conta é a
         * <b>primeira que encontrar</b> ao andar pelas seis pedras — candelabro ou tocha, conforme a
         * ordem em que elas lhe aparecem. Os dois juntos nunca valem três de luz.
         */
        level.setBlockAndUpdate(canto.offset(1, 1, 0), Blocks.TORCH.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 2 && manda.rechargeScale() != 3) {
            helper.fail("candelabro e tocha contam por um só; ficou " + manda.rechargeScale());
        }
        level.setBlockAndUpdate(canto.offset(1, 1, 0), Blocks.AIR.defaultBlockState());

        // e o segundo candelabro também não soma
        level.setBlockAndUpdate(canto.offset(1, 1, 0), OccultaBlocks.CANDELABRA.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 3) {
            helper.fail("o segundo candelabro não soma; ficou " + manda.rechargeScale());
        }
        level.setBlockAndUpdate(canto.offset(1, 1, 0), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(canto.offset(2, 1, 0), OccultaBlocks.CHALICE.defaultBlockState());
        manda.refresh();
        if (manda.powerScale() != 2) {
            helper.fail("o cálice vazio soma um ao teto; ficou " + manda.powerScale());
        }

        level.setBlockAndUpdate(canto.offset(2, 1, 0), OccultaBlocks.CHALICE.defaultBlockState()
                .setValue(ChaliceBlock.CHEIO, true));
        manda.refresh();
        if (manda.powerScale() != 3) {
            helper.fail("e cheio soma dois; ficou " + manda.powerScale());
        }

        // o segundo cálice não soma: conta-se um de cada
        level.setBlockAndUpdate(canto.offset(0, 1, 1), OccultaBlocks.CHALICE.defaultBlockState()
                .setValue(ChaliceBlock.CHEIO, true));
        manda.refresh();
        if (manda.powerScale() != 3) {
            helper.fail("o segundo cálice não soma; ficou " + manda.powerScale());
        }
        helper.succeed();
    }

    /** E quem precisa de poder acha o altar mais perto, dentro do alcance dele. */
    @GameTest
    public void theAltarIsFoundByThoseWhoNeedIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        AltarBlockEntity manda = build(helper, canto);
        if (manda == null) return;

        if (PowerSources.closest(level, canto.offset(3, 0, 0)) != manda) {
            helper.fail("o altar devia ser achado de perto");
        }
        if (PowerSources.closest(level, canto.offset(100, 0, 0)) == manda) {
            helper.fail("mas não de cem blocos, que passa do alcance dele");
        }
        if (manda.range() != AltarBlockEntity.RANGE) helper.fail("o alcance de um altar pelado é dezesseis");
        helper.succeed();
    }

    /** A tabela de poder é a do original: a folhagem vale três, a grama dois, a flor quatro. */
    @GameTest
    public void theWorthOfEachThingIsTheOriginals(GameTestHelper helper) {
        if (AltarPower.worth(OccultaBlocks.ROWAN_LEAVES) != 3) helper.fail("a folhagem vale três");
        if (AltarPower.worth(Blocks.GRASS_BLOCK) != 2) helper.fail("a grama vale dois");
        if (AltarPower.worth(Blocks.DANDELION) != 4) helper.fail("a flor vale quatro");
        if (AltarPower.worth(Blocks.OAK_LOG) != 2) helper.fail("a tora vale dois");
        if (AltarPower.worth(OccultaBlocks.ROWAN_SAPLING) != 4) helper.fail("a muda vale quatro");
        if (AltarPower.worth(Blocks.DRAGON_EGG) != 250) helper.fail("e o ovo de dragão vale duzentos e cinquenta");
        if (AltarPower.worth(Blocks.STONE) != 0) helper.fail("pedra não é natureza para o altar");
        helper.succeed();
    }

    /** E o thaumômetro lê no altar o que o mod diz. */
    @GameTest
    public void theAltarHasItsAspects(GameTestHelper helper) {
        var altar = ObjectAspects.of(OccultaItems.WITCH_ALTAR);
        if (altar.getAmount(Aspects.EARTH) != 4 || altar.getAmount(Aspects.MECHANISM) != 3) {
            helper.fail("o altar é terra 4 e machina 3; veio " + altar);
        }
        helper.succeed();
    }

    /** Põe seis pedras de duas por três e devolve a que manda. */
    private static AltarBlockEntity build(GameTestHelper helper, BlockPos canto) {
        ServerLevel level = helper.getLevel();
        for (int volta = 0; volta < AltarBlock.PIECES; volta++) {
            level.setBlockAndUpdate(canto.offset(volta % 3, 0, volta / 3),
                    OccultaBlocks.WITCH_ALTAR.defaultBlockState());
        }
        AltarBlockEntity manda = core(helper, canto);
        if (manda == null) helper.fail("as seis pedras deviam fazer um altar");
        return manda;
    }

    /** O bloco que manda no altar daquela pedra, ou nada. */
    /**
     * <b>O Pentáculo deitado no altar dobra a recarga dele — e com ele o altar fica completo.</b>
     *
     * <p>Era a última peça do altar do original que faltava ao porte, e é a única que não se faz com o
     * que o mundo dá: ela leva <b>koboldite</b>, que só sai de um goblin.
     *
     * <p>E ele <b>não soma, multiplica</b> — como o Ovo do Infinito. Os dois juntos dão <b>vinte vezes</b>
     * a velocidade de um altar pelado.
     */
    @GameTest
    public void thePentacleDoublesTheRecharge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos canto = helper.absolutePos(new BlockPos(1, 2, 1));
        AltarBlockEntity manda = build(helper, canto);
        if (manda == null) return;

        manda.refresh();
        if (manda.rechargeScale() != 1) helper.fail("um altar pelado recarrega no passo de sempre");

        net.thaumcraft.occulta.PlacedItemBlock.põe(level, canto.above(),
                new ItemStack(net.thaumcraft.occulta.OccultaItems.PENTACLE), null);
        manda.refresh();
        if (manda.rechargeScale() != net.thaumcraft.occulta.AltarBlockEntity.PENTÁCULO) {
            helper.fail("o pentáculo dobra a recarga; ficou " + manda.rechargeScale());
        }

        // e com uma tocha ao lado, que soma um antes de ele dobrar, dá quatro
        level.setBlockAndUpdate(canto.offset(1, 1, 0), Blocks.TORCH.defaultBlockState());
        manda.refresh();
        if (manda.rechargeScale() != 4) {
            helper.fail("a tocha soma antes de ele dobrar, e dá quatro; deu " + manda.rechargeScale());
        }

        // e um segundo pentáculo não dobra outra vez
        net.thaumcraft.occulta.PlacedItemBlock.põe(level, canto.offset(2, 1, 0),
                new ItemStack(net.thaumcraft.occulta.OccultaItems.PENTACLE), null);
        manda.refresh();
        if (manda.rechargeScale() != 4) {
            helper.fail("o segundo não dobra outra vez; ficou " + manda.rechargeScale());
        }
        helper.succeed();
    }

    private static AltarBlockEntity core(GameTestHelper helper, BlockPos onde) {
        if (helper.getLevel().getBlockEntity(onde) instanceof AltarBlockEntity altar) return altar.core();
        return null;
    }
}
