package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.RiteRegistry;
import net.thaumcraft.occulta.rite.RiteStep;
import net.thaumcraft.occulta.rite.Rites;
import net.thaumcraft.occulta.rite.Sacrifice;

import java.util.List;

/**
 * Os ritos que chamam: a Bruxa, o Wither, os Bichos e a Chuva de Sapos.
 *
 * <p>O que estas provas medem, além de o bicho vir, são as <b>recusas</b> — que no original são metade do
 * rito. Um teto baixo demais, um coven pequeno demais, um aldeão que não está lá: cada uma delas devolve o que
 * se ofereceu, e cada uma é fácil de portar de forma que nunca recuse.
 */
public class OccultaSummonRitesGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Os quatro estão na lista. */
    @GameTest(maxTicks = 20)
    public void theFourAreRegistered(GameTestHelper helper) {
        for (String chave : List.of("tc.rite.summonwitch", "tc.rite.summonwither",
                "tc.rite.callbeasts", "tc.rite.rainoftoads")) {
            if (RiteRegistry.all().stream().noneMatch(r -> r.key().equals(chave))) {
                helper.fail("falta o rito " + chave);
                return;
            }
        }
        helper.succeed();
    }

    /** Com o céu livre, o rito traz o bicho — e ele vem em cima do glifo. */
    @GameTest(maxTicks = 60)
    public void withClearSkyTheCreatureComes(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var rito = new Rites.SummonCreature(() -> EntityTypes.WITCH, 0);
        var corrido = new ActiveRite("tc.rite.summonwitch", rito, List.of(), null, 0);
        RiteStep.Result saiu = rito.steps(0).getFirst().run(level, meio, 20L, corrido);
        if (saiu != RiteStep.Result.COMPLETED) {
            helper.fail("com o céu livre ele devia chamar, e deu " + saiu);
            return;
        }

        var achadas = level.getEntitiesOfClass(Mob.class,
                new net.minecraft.world.phys.AABB(meio).inflate(3.0),
                m -> m.getType() == EntityTypes.WITCH);
        if (achadas.isEmpty()) {
            helper.fail("e a bruxa devia estar em cima do glifo");
            return;
        }
        achadas.forEach(Mob::discard);
        helper.succeed();
    }

    /**
     * <b>Uma laje em cima do glifo já chega.</b>
     *
     * <p>No desenho do teto o bloco do meio conta por <b>cem</b>, e o rito só aguenta <b>um</b> estorvo. É o
     * que impede alguém de chamar um Wither dentro de uma caixa.
     */
    @GameTest(maxTicks = 60)
    public void oneBlockOverTheGlyphIsEnoughToRefuse(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        if (Rites.SummonCreature.estorvos(level, meio) > Rites.SummonCreature.ESTORVOS) {
            helper.fail("esta prova começa com o céu livre");
            return;
        }

        level.setBlockAndUpdate(meio.above(), Blocks.STONE.defaultBlockState());
        if (Rites.SummonCreature.estorvos(level, meio) < 100) {
            helper.fail("o bloco do meio conta por cem, e contou "
                    + Rites.SummonCreature.estorvos(level, meio));
        }

        var rito = new Rites.SummonCreature(() -> EntityTypes.WITCH, 0);
        var corrido = new ActiveRite("tc.rite.summonwitch", rito, List.of(), null, 0);
        RiteStep.Result saiu = rito.steps(0).getFirst().run(level, meio, 20L, corrido);
        if (saiu != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("com o teto tapado ele desiste e devolve, e deu " + saiu);
        }
        helper.succeed();
    }

    /** E um coven pequeno demais também é recusa. */
    @GameTest(maxTicks = 60)
    public void tooSmallACovenIsAlsoARefusal(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var rito = new Rites.CallCreatures(() -> List.of(EntityTypes.CHICKEN));
        var sozinha = new ActiveRite("tc.rite.callbeasts", rito, List.of(), null, 0);
        RiteStep.Result saiu = rito.steps(0).getFirst().run(level, meio, 60L, sozinha);
        if (saiu != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("o de Chamar os Bichos pede três bruxas, e sem elas deu " + saiu);
        }

        var emGrupo = new ActiveRite("tc.rite.callbeasts", rito, List.of(), null,
                Rites.CallCreatures.COVEN);
        saiu = rito.steps(0).getFirst().run(level, meio, 60L, emGrupo);
        if (saiu != RiteStep.Result.UPKEEP) {
            helper.fail("e com três ele passa a sustentar-se, e deu " + saiu);
        }
        helper.succeed();
    }

    /**
     * O de Chamar os Bichos <b>traz</b> o que está longe, e não cria nada.
     *
     * <p>A prova usa <b>galinha</b>, e não porco, de propósito: o rito varre cento e vinte e oito blocos em
     * volta, a suíte corre num mundo só, e há provas ao lado que enchem o mundo de porcos às centenas. Com
     * porco, o rito trazia os delas e nunca chegava ao desta — foi o que aconteceu quando uma classe de
     * provas nova mudou onde as arenas caem.
     */
    @GameTest(maxTicks = 80)
    public void callingBeastsBringsThemInsteadOfMakingThem(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        // uma galinha longe o bastante para ser chamado, nos oito cantos que o rito varre
        var galinha = EntityTypes.CHICKEN.create(level, EntitySpawnReason.TRIGGERED);
        if (galinha == null) {
            helper.fail("devia haver galinha");
            return;
        }
        galinha.snapTo(meio.getX() + 40.0, meio.getY() - 1.0, meio.getZ() + 40.0, 0.0f, 0.0f);
        galinha.setPersistenceRequired();
        level.addFreshEntity(galinha);

        var rito = new Rites.CallCreatures(() -> List.of(EntityTypes.CHICKEN));
        var corrido = new ActiveRite("tc.rite.callbeasts", rito, List.of(), null,
                Rites.CallCreatures.COVEN);
        // o canto 2 é o do sudeste por baixo, que é onde a galinha está
        var passo = rito.steps(0).getFirst();
        for (int volta = 0; volta < 8; volta++) {
            passo.run(level, meio, 60L, corrido);
            if (galinha.distanceToSqr(meio.getX(), meio.getY(), meio.getZ()) < 64.0) break;
        }

        if (galinha.distanceToSqr(meio.getX(), meio.getY(), meio.getZ()) >= 64.0) {
            helper.fail("em oito voltas ele varre os oito cantos e a galinha devia ter vindo");
        }
        galinha.discard();
        helper.succeed();
    }

    /** O sapo que cai do céu tem hora para acabar. */
    @GameTest(maxTicks = 40)
    public void arainedToadHasADeadline(GameTestHelper helper) {
        piso(helper);
        var sapo = helper.spawn(net.thaumcraft.occulta.OccultaEntities.TOAD, new BlockPos(3, 2, 3));
        sapo.choveu(2);
        if (!sapo.isAlive()) helper.fail("ele começa vivo");

        helper.succeedWhen(() -> helper.assertTrue(!sapo.isAlive(), "e some quando o prazo acaba"));
    }

    /** O vivo que falta é recusa, e o que está lá some. */
    @GameTest(maxTicks = 60)
    public void theLivingSacrificeMustBeThere(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        var passo = new Sacrifice.TakeLiving(EntityTypes.VILLAGER);
        var corrido = new ActiveRite("tc.rite.summonwither", new Rites.Manifest(), List.of(), null, 0);

        RiteStep.Result saiu = passo.run(level, meio, 20L, corrido);
        if (saiu != RiteStep.Result.ABORTED_REFUND) {
            helper.fail("sem o aldeão ele desiste e devolve, e deu " + saiu);
        }

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 2, 4));
        saiu = passo.run(level, meio, 20L, corrido);
        if (saiu != RiteStep.Result.COMPLETED) {
            helper.fail("com o aldeão lá ele toma-o, e deu " + saiu);
        }
        if (aldeão.isAlive()) helper.fail("e o aldeão some");
        helper.succeed();
    }
}
