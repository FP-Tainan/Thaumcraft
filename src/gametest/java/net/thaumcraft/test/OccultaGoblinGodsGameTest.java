package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.StatueOfWorshipBlockEntity;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.goblin.GoblinGodEntity;
import net.thaumcraft.occulta.goblin.GulgEntity;
import net.thaumcraft.occulta.goblin.MogEntity;

/**
 * Os dois <b>deuses goblins</b>: o <b>Mog</b> e o <b>Gulg</b>.
 *
 * <p>A prova que carrega a fatia é a da <b>distância</b>. Juntos, os dois são <b>invencíveis</b> e o murro
 * do Gulg é o dobro; separados, são dois bichos grandes que se matam. Não há neles um só poder novo: só uma
 * conta de distância, escrita duas vezes com o sinal trocado — e é o melhor desenho de chefe que o Witchery
 * tem.
 */
public class OccultaGoblinGodsGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os números deles são os do original. */
    @GameTest
    public void theirNumbersAreTheOriginals(GameTestHelper helper) {
        if (GoblinGodEntity.INVENCÍVEL != 9.0 || GoblinGodEntity.UM_QUINTO != 36.0
                || GoblinGodEntity.METADE != 81.0 || GoblinGodEntity.QUATRO_QUINTOS != 256.0) {
            helper.fail("três, seis, nove e dezesseis blocos, ao quadrado");
        }
        if (GoblinGodEntity.TETO != 15.0f) helper.fail("e nunca mais de quinze por golpe");
        if (GoblinGodEntity.DESPERTAR != 150) helper.fail("o despertar são cento e cinquenta batidas");
        if (MogEntity.ARMADURA != 5.0 || GulgEntity.ARMADURA != 8.0) {
            helper.fail("cinco de armadura no Mog, oito no Gulg");
        }
        if (MogEntity.NO_AR != 2.5 || MogEntity.NO_CHÃO != 1.5) {
            helper.fail("e a flecha do Mog sai mais depressa contra quem está no ar");
        }
        helper.succeed();
    }

    /**
     * <b>Juntos, nada os fere.</b>
     *
     * <p>Esta é a prova que carrega a fatia. A três blocos um do outro, nenhum dano passa — nem o dos
     * deuses, nem o do fogo, nem o de um golpe de diamante. A luta é <b>separá-los</b>, e tudo o mais é
     * detalhe.
     */
    @GameTest
    public void theDistanceBetweenThemIsEverything(GameTestHelper helper) {
        if (GoblinGodEntity.quantoPassa(4.0) != 0.0) helper.fail("a dois blocos, nada passa");
        if (GoblinGodEntity.quantoPassa(GoblinGodEntity.INVENCÍVEL) != 0.0) {
            helper.fail("e a três também não");
        }
        if (GoblinGodEntity.quantoPassa(16.0) != 0.2) helper.fail("a quatro, um quinto");
        if (GoblinGodEntity.quantoPassa(64.0) != 0.5) helper.fail("a oito, metade");
        if (GoblinGodEntity.quantoPassa(100.0) != 0.8) helper.fail("a dez, quatro quintos");
        if (GoblinGodEntity.quantoPassa(400.0) != 1.0) helper.fail("e a vinte, tudo");
        if (GoblinGodEntity.quantoPassa(Double.MAX_VALUE) != 1.0) {
            helper.fail("e sozinho, um deus goblin é um bicho comum");
        }
        helper.succeed();
    }

    /**
     * <b>E o murro do Gulg é o espelho disso: quanto mais perto do Mog, mais forte.</b>
     *
     * <p>Seis mais vinte e um bloco e meio de voo, colado ao par; seis mais quatro e meio bloco, sozinho.
     * O mesmo número que o torna invencível torna-o um martelo.
     */
    @GameTest
    public void theGulgPunchesHarderNearTheMog(GameTestHelper helper) {
        if (GulgEntity.força(4.0) != 20 || GulgEntity.levanta(4.0) != 1.0) {
            helper.fail("colado ao Mog, vinte de força e um bloco de voo");
        }
        if (GulgEntity.força(16.0) != 15 || GulgEntity.levanta(16.0) != 0.8) helper.fail("a quatro");
        if (GulgEntity.força(64.0) != 10 || GulgEntity.levanta(64.0) != 0.5) helper.fail("a oito");
        if (GulgEntity.força(100.0) != 6 || GulgEntity.levanta(100.0) != 0.2) helper.fail("a dez");
        if (GulgEntity.força(400.0) != 4 || GulgEntity.levanta(400.0) != 0.0) {
            helper.fail("e sozinho, um murro de zumbi");
        }
        helper.succeed();
    }

    /**
     * <b>No mundo: colados, não se magoam; afastados, sim — e nunca mais de quinze.</b>
     */
    @GameTest(maxTicks = 60)
    public void togetherTheyAreUntouchable(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var mog = helper.spawn(OccultaEntities.MOG, new BlockPos(2, 2, 2));
        var gulg = helper.spawn(OccultaEntities.GULG, new BlockPos(3, 2, 2));
        mog.setHealth(mog.getMaxHealth());
        gulg.setHealth(gulg.getMaxHealth());

        if (mog.getAttribute(Attributes.MAX_HEALTH).getBaseValue() != 400.0) {
            helper.fail("quatrocentos de vida, cada um");
        }

        // colados: nenhum dano passa
        mog.hurtServer(level, level.damageSources().magic(), 100.0f);
        if (mog.getHealth() != mog.getMaxHealth()) {
            helper.fail("colado ao par, nada o fere; perdeu "
                    + (mog.getMaxHealth() - mog.getHealth()));
        }

        // e afastados, passa — mas nunca mais de quinze
        BlockPos longe = helper.absolutePos(new BlockPos(2, 2, 2));
        gulg.setPos(longe.getX() + 0.5, longe.getY(), longe.getZ() + 40.5);
        float tinha = mog.getHealth();
        mog.hurtServer(level, level.damageSources().magic(), 100.0f);
        float perdeu = tinha - mog.getHealth();
        if (perdeu <= 0.0f) helper.fail("longe do par, ele é ferido");
        if (perdeu > GoblinGodEntity.TETO) {
            helper.fail("mas nunca mais de quinze por golpe; perdeu " + perdeu);
        }

        mog.discard();
        gulg.discard();
        helper.succeed();
    }

    /**
     * <b>E eles não se mordem — nem aos goblins comuns.</b>
     *
     * <p>Um deus goblin que caçasse goblins não seria um deus goblin.
     */
    @GameTest(maxTicks = 40)
    public void theGodsDoNotHuntTheirOwn(GameTestHelper helper) {
        piso(helper);
        var mog = helper.spawn(OccultaEntities.MOG, new BlockPos(2, 2, 2));
        var gulg = helper.spawn(OccultaEntities.GULG, new BlockPos(5, 2, 5));
        var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(4, 2, 4));

        mog.setTarget(gulg);
        if (mog.getTargetUnchecked() != null) helper.fail("o Mog não caça o Gulg");
        mog.setTarget(goblin);
        if (mog.getTargetUnchecked() != null) helper.fail("nem um goblin comum");

        mog.discard();
        gulg.discard();
        goblin.discard();
        helper.succeed();
    }

    /**
     * <b>A Estrela do Nether chama-os — e come cinco adoradores.</b>
     *
     * <p>É a única vez em todo o mod em que uma coisa boa se paga com a vida de quem a adorava: você não
     * pede os deuses, você <b>os compra</b>.
     */
    @GameTest(maxTicks = 60)
    public void theNetherStarCallsThemAndEatsFive(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos onde = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(onde, OccultaBlocks.STATUE_OF_WORSHIP.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof StatueOfWorshipBlockEntity estátua)) {
            helper.fail("a estátua devia ter alma");
            return;
        }

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        estátua.dono(new TaglockItem.Taglock(quem.getUUID(), quem.getName().getString()));

        var goblins = new java.util.ArrayList<net.thaumcraft.occulta.goblin.GoblinEntity>();
        for (int volta = 0; volta < StatueOfWorshipBlockEntity.ENCHE; volta++) {
            var goblin = helper.spawn(OccultaEntities.GOBLIN, new BlockPos(1 + volta, 2, 6));
            goblin.adorando(true);
            goblins.add(goblin);
        }
        if (estátua.conta(level) != StatueOfWorshipBlockEntity.ENCHE) {
            helper.fail("deviam ser cinco adoradores; são " + estátua.conta(level));
        }

        if (!StatueOfWorshipBlockEntity.chamaOsDeuses(level, onde, quem,
                StatueOfWorshipBlockEntity.PROCURA_COM_A_ESTRELA,
                StatueOfWorshipBlockEntity.PÕE_COM_A_ESTRELA)) {
            helper.fail("a estrela devia chamá-los");
        }

        var caixa = new net.minecraft.world.phys.AABB(onde).inflate(64.0);
        boolean temMog = !level.getEntitiesOfClass(MogEntity.class, caixa).isEmpty();
        boolean temGulg = !level.getEntitiesOfClass(GulgEntity.class, caixa).isEmpty();
        if (!temMog || !temGulg) helper.fail("e eles vêm aos pares: Mog " + temMog + ", Gulg " + temGulg);

        // e um segundo chamado não traz um segundo par
        if (StatueOfWorshipBlockEntity.chamaOsDeuses(level, onde, quem,
                StatueOfWorshipBlockEntity.PROCURA_COM_A_ESTRELA,
                StatueOfWorshipBlockEntity.PÕE_COM_A_ESTRELA)) {
            helper.fail("dois pares de deuses goblins não é um desafio, é um engano");
        }

        // e a estrela come cinco adoradores
        estátua.comeOsAdoradores(level);
        int vivos = 0;
        for (var goblin : goblins) if (goblin.isAlive()) vivos++;
        if (vivos != 0) helper.fail("os cinco deviam ter morrido; vivos " + vivos);

        for (var deus : level.getEntitiesOfClass(GoblinGodEntity.class, caixa)) deus.discard();
        for (var goblin : goblins) goblin.discard();
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
