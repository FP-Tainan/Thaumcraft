package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.SeerStoneItem;
import net.thaumcraft.occulta.coven.Coven;
import net.thaumcraft.occulta.coven.CovenSummon;
import net.thaumcraft.occulta.coven.CovenWitchEntity;
import net.thaumcraft.occulta.rite.RiteRegistry;

import java.util.UUID;

/**
 * A <b>Pedra do Vidente</b>: a pedra que lê gente e chama o coven.
 *
 * <p>A prova que carrega a fatia é a do <b>coven que não comparece</b>. Até agora um coven contava sempre
 * nos ritos, e a pedra não teria o que fazer; no original ele só conta <b>com ela na mão</b>, e é isso que
 * transforma seis nomes numa lista em seis bruxas em volta de um círculo.
 */
public class OccultaSeerStoneGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um retrato de bruxa com aquele nome. */
    private static Coven.Membro retrato(String nome) {
        return new Coven.Membro(UUID.randomUUID(), nome, 0, -1);
    }

    /** <b>A pedra existe</b>, é uma só, e sai do rito de noite com o anel de dentro. */
    @GameTest(maxTicks = 20)
    public void theStoneAndTheRiteThatMakesIt(GameTestHelper helper) {
        ItemStack pedra = new ItemStack(OccultaItems.SEER_STONE);
        if (!(pedra.getItem() instanceof SeerStoneItem)) {
            helper.fail("a Pedra do Vidente devia estar registada");
            return;
        }
        if (pedra.getMaxStackSize() != 1) helper.fail("e é uma por casa");

        RiteRegistry.Entry rito = RiteRegistry.all().stream()
                .filter(qual -> qual.key().equals("tc.rite.infusionseerstone")).findFirst().orElse(null);
        if (rito == null) {
            helper.fail("o rito da pedra devia estar no livro");
            return;
        }
        if (!rito.when().contains(RiteRegistry.When.NIGHT)) helper.fail("e ele é de noite");
        if (rito.inner().ritual() != 16) helper.fail("e o anel de dentro tem dezesseis glifos de ritual");
        if (!rito.middle().empty() || !rito.outer().empty()) {
            helper.fail("e os outros dois anéis não entram");
        }
        helper.succeed();
    }

    /**
     * <b>O coven só conta com a pedra na mão.</b>
     *
     * <p>É a correção que esta fatia traz: ter bruxas no bolso não fazia nada por um círculo no original, e
     * neste porte fazia. Agora a conta é a de lá.
     */
    @GameTest(maxTicks = 20)
    public void theCovenOnlyCountsWithTheStoneInHand(GameTestHelper helper) {
        var quem = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 3; i++) Coven.junta(quem, retrato("Bruxa " + i));
        if (Coven.tamanho(quem) != 3) {
            helper.fail("a prova começa com três no coven");
            return;
        }

        // de mão vazia não há chegadas nenhumas
        if (!CovenSummon.chegadas(0).isEmpty()) helper.fail("sem a pedra, ninguém chega");

        // e com a pedra há uma chegada por bruxa, até as seis casas que há
        if (CovenSummon.chegadas(3).size() != 3) {
            helper.fail("com três no coven, três chegadas");
            return;
        }
        if (CovenSummon.chegadas(Coven.MAX).size() != CovenSummon.À_VOLTA.length) {
            helper.fail("e com seis, as seis casas");
            return;
        }
        if (CovenSummon.chegadas(20).size() != CovenSummon.À_VOLTA.length) {
            helper.fail("e nunca mais do que as seis casas que há");
        }
        helper.succeed();
    }

    /**
     * <b>Chamar refaz a bruxa a partir do retrato</b>, e ela vem com prazo.
     *
     * <p>Uma bruxa recrutada saiu do mundo; o que ficou dela foram três coisas. A prova mede que essas três
     * bastam: a que chega tem o <b>nome certo</b>, é <b>de quem a chamou</b>, e tem <b>um minuto</b>.
     */
    @GameTest(maxTicks = 40)
    public void callingRebuildsHerFromThePortrait(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));
        Coven.junta(quem, retrato("Agnes Nutter"));

        if (CovenSummon.chama(level, quem, quem.position(), 1)) {
            helper.fail("não há uma segunda bruxa para chamar");
            return;
        }
        if (!CovenSummon.chama(level, quem, quem.position(), 0)) {
            helper.fail("a primeira chama-se");
            return;
        }

        var achadas = level.getEntitiesOfClass(CovenWitchEntity.class,
                new net.minecraft.world.phys.AABB(quem.blockPosition()).inflate(4.0));
        if (achadas.size() != 1) {
            helper.fail("e chega uma: chegaram " + achadas.size());
            return;
        }
        CovenWitchEntity veio = achadas.getFirst();
        if (!"Agnes Nutter".equals(veio.comoSeChama())) {
            helper.fail("com o nome dela: veio " + veio.comoSeChama());
            return;
        }
        if (!quem.getUUID().equals(veio.dono())) helper.fail("e é de quem a chamou");
        if (veio.prazo() != CovenSummon.PRAZO) {
            helper.fail("e fica um minuto: fica " + veio.prazo());
            return;
        }

        // chamá-la outra vez não faz uma segunda: é a mesma que vem
        CovenSummon.chama(level, quem, quem.position(), 0);
        int quantas = level.getEntitiesOfClass(CovenWitchEntity.class,
                new net.minecraft.world.phys.AABB(quem.blockPosition()).inflate(8.0)).size();
        if (quantas != 1) helper.fail("chamada duas vezes, continua a ser uma só: são " + quantas);

        veio.discard();
        helper.succeed();
    }

    /**
     * <b>Morrer sai do coven</b>, e o que a identifica é o <b>nome</b>.
     *
     * <p>A bruxa que morre foi chamada, e portanto é um bicho novo com outro número — procurar pelo número
     * não acharia nada. É por isso que o original guarda o nome.
     */
    @GameTest(maxTicks = 40)
    public void dyingLeavesTheCoven(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));
        Coven.junta(quem, retrato("Agnes Nutter"));
        Coven.junta(quem, retrato("Esme Weatherwax"));

        CovenSummon.chama(level, quem, quem.position(), 0);
        var achadas = level.getEntitiesOfClass(CovenWitchEntity.class,
                new net.minecraft.world.phys.AABB(quem.blockPosition()).inflate(4.0));
        if (achadas.size() != 1) {
            helper.fail("a chamada devia ter vindo");
            return;
        }
        CovenWitchEntity veio = achadas.getFirst();
        if (veio.getUUID().equals(Coven.of(quem).bruxas().getFirst().id())) {
            helper.fail("a bruxa refeita é um bicho novo, com outro número");
            return;
        }

        veio.die(level.damageSources().generic());
        if (Coven.temNome(quem, "Agnes Nutter")) helper.fail("morta, ela sai do coven");
        if (!Coven.temNome(quem, "Esme Weatherwax")) helper.fail("e a outra fica");
        if (Coven.tamanho(quem) != 1) {
            helper.fail("o coven fica com uma: tem " + Coven.tamanho(quem));
        }
        helper.succeed();
    }

    /**
     * <b>Entrar no coven é sair do mundo</b>, e o prazo traz de volta.
     *
     * <p>A prova mede o fim da visita: uma bruxa com prazo de uma batida some na batida seguinte, <b>sem
     * morrer</b> — e por isso continua no coven.
     */
    @GameTest(maxTicks = 40)
    public void theVisitEnds(GameTestHelper helper) {
        piso(helper);
        var level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));
        Coven.junta(quem, retrato("Agnes Nutter"));
        CovenSummon.chama(level, quem, quem.position(), 0);

        var veio = level.getEntitiesOfClass(CovenWitchEntity.class,
                new net.minecraft.world.phys.AABB(quem.blockPosition()).inflate(4.0)).getFirst();
        veio.põePrazo(1);
        helper.runAfterDelay(3, () -> {
            if (veio.isAlive()) {
                helper.fail("acabado o prazo, ela some");
                return;
            }
            if (!Coven.temNome(quem, "Agnes Nutter")) {
                helper.fail("e some sem morrer: continua no coven");
                return;
            }
            helper.succeed();
        });
    }

    /** E a <b>leitura</b> não rebenta com quem não tem nada: é a fala de «não há» em cada linha. */
    @GameTest(maxTicks = 20)
    public void readingSomeoneWithNothingSaysSo(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(helper.absoluteVec(new Vec3(3.5, 2, 3.5)));
        quem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(OccultaItems.SEER_STONE));
        SeerStoneItem.lê(quem, quem);
        helper.succeed();
    }
}
