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
     * Chamar os bichos traz os que já existem, em vez de fazer bichos novos.
     *
     * <p><b>A prova usa um camelo</b>, e não um bicho comum. O rito varre uma caixa de <b>cento e vinte e
     * oito blocos</b> por canto e traz <b>dois</b> de cada vez; as provas correm todas no mesmo mundo, e um
     * canto desses apanha as arenas das vizinhas. Com uma galinha, o rito trazia as galinhas das outras
     * provas e nunca chegava à desta. Com um camelo — que nenhuma outra prova usa — o único que há no canto é
     * o dela.
     */
    @GameTest(maxTicks = 60)
    public void callingBeastsBringsThemInsteadOfMakingThem(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(3, 2, 3));

        /*
         * E antes de tudo, <b>mandar os camelos do mundo para longe</b>: o chamado alcança cento e vinte e
         * oito blocos e traz <b>dois</b> por volta, de modo que um deserto com uma caravana lá dentro enche a
         * cota antes de chegar ao nosso. Nenhuma outra prova usa camelos, e por isso os que houver são do
         * mundo.
         *
         * <p>E não basta descartá-los: um bicho descartado <b>continua na lista</b> até o fim da batida, e o
         * rito, que corre dentro desta mesma batida, ainda o vê e gasta a cota com ele. Mudá-los de lugar
         * tira-os da caixa na hora, porque a caixa se pergunta ao corpo do bicho e não ao índice.
         */
        for (var qualquer : level.getEntitiesOfClass(net.minecraft.world.entity.animal.camel.Camel.class,
                new net.minecraft.world.phys.AABB(meio).inflate(Rites.CallCreatures.ALCANCE))) {
            qualquer.snapTo(meio.getX() + 10000.0, meio.getY(), meio.getZ() + 10000.0, 0.0f, 0.0f);
            qualquer.discard();
        }

        var bicho = EntityTypes.CAMEL.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) {
            helper.fail("devia haver camelo");
            return;
        }
        /*
         * Longe do círculo, mas <b>dentro da arena desta prova</b>, e é por isso que ele vai ao alto: a
         * arena tem oito por oito de chão, e a quina dela está a <b>trinta e dois</b> do meio ao quadrado —
         * que é exatamente o {@link Rites.CallCreatures#PERTO_DEMAIS}, e perto demais não se chama. Três
         * blocos acima, a conta dá quarenta e um, com folga, e o bicho cai num dos quatro cantos <b>de
         * cima</b> do rito. Posto fora da arena, ele cairia na de outra prova — ou num pedaço de mundo que
         * ninguém carregou.
         */
        bicho.snapTo(meio.getX() + 4.0, meio.getY() + 3.0, meio.getZ() + 4.0, 0.0f, 0.0f);
        bicho.setPersistenceRequired();
        level.addFreshEntity(bicho);

        var rito = new Rites.CallCreatures(() -> List.of(EntityTypes.CAMEL));
        var corrido = new ActiveRite("tc.rite.callbeasts", rito, List.of(), null,
                Rites.CallCreatures.COVEN);
        /*
         * Oito voltas varrem os oito cantos — mas cada volta traz só <b>dois</b>, e a suíte corre num mundo
         * só: um camelo de uma prova ao lado, dentro dos cento e vinte e oito blocos, pode vir à frente
         * deste. Vindo, ele fica perto e deixa de contar, e por isso bastam voltas que cheguem.
         */
        var passo = rito.steps(0).getFirst();
        for (int volta = 0; volta < 32; volta++) {
            passo.run(level, meio, 60L, corrido);
            if (bicho.distanceToSqr(meio.getX(), meio.getY(), meio.getZ()) < 64.0) break;
        }

        if (bicho.distanceToSqr(meio.getX(), meio.getY(), meio.getZ()) >= 64.0) {
            helper.fail("varridos os oito cantos, o camelo devia ter vindo");
        }
        if (Rites.CallCreatures.ALCANCE != 128.0) helper.fail("o chamado alcança cento e vinte e oito");
        if (Rites.CallCreatures.DE_CADA_VEZ != 2) helper.fail("e traz dois de cada vez");
        bicho.discard();
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
