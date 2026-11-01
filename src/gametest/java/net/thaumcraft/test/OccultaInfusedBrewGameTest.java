package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.Undead;
import net.thaumcraft.occulta.enslave.Enslavement;
import net.thaumcraft.occulta.infusion.GraveFeeding;
import net.thaumcraft.occulta.infusion.InfusedBrews;
import net.thaumcraft.occulta.rite.RiteRegistry;

import java.util.List;

/**
 * Os <b>Cozimentos Infundidos</b>: os dois que não são poções.
 *
 * <p>A prova que carrega a fatia é a do <b>Cozimento da Cova</b>, porque ele é três coisas ao mesmo tempo
 * e nenhuma delas se vê numa lista de efeitos: quem o bebeu <b>conta como morto-vivo</b>, <b>cura-se
 * matando</b>, e com a Pedra Necrótica na mão <b>escraviza com o olhar</b>. Qualquer uma das três partida
 * deixa um cozimento que parece funcionar e não funciona.
 */
public class OccultaInfusedBrewGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um jogador posto <b>dentro da arena</b>, que é onde as coisas dele têm de acontecer. */
    private static net.minecraft.server.level.ServerPlayer naArena(GameTestHelper helper,
                                                                   double x, double z, float rumo) {
        var quem = helper.makeMockServerPlayerInLevel();
        Vec3 onde = helper.absoluteVec(new Vec3(x, 2.0, z));
        quem.snapTo(onde.x, onde.y, onde.z, rumo, 0.0f);
        return quem;
    }

    /** <b>Os dois ritos estão no livro</b>, com os dois anéis e o bicho vivo de cada um. */
    @GameTest(maxTicks = 20)
    public void bothRitesAreInTheBook(GameTestHelper helper) {
        for (String qual : List.of("tc.rite.infusebrewsoaring", "tc.rite.infusebrewgrave")) {
            RiteRegistry.Entry rito = RiteRegistry.all().stream()
                    .filter(cada -> cada.key().equals(qual)).findFirst().orElse(null);
            if (rito == null) {
                helper.fail("o rito " + qual + " devia estar no livro");
                return;
            }
            if (rito.inner().ritual() != 16 || rito.middle().ritual() != 28) {
                helper.fail(qual + " pede o anel de dentro de dezesseis e o do meio de vinte e oito");
                return;
            }
            if (!rito.outer().empty()) {
                helper.fail(qual + " não pede o anel de fora");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * <b>A base não se bebe.</b> Bebida, dá Definhamento IV — que é o original a dizer «isto é um
     * ingrediente».
     */
    @GameTest(maxTicks = 40)
    public void theBaseIsNotADrink(GameTestHelper helper) {
        piso(helper);
        var quem = naArena(helper, 2.5, 2.5, 0.0f);
        ItemStack frasco = new ItemStack(OccultaItems.INFUSION_BASE);
        frasco.getItem().finishUsingItem(frasco, helper.getLevel(), quem);

        var tem = quem.getEffect(MobEffects.WITHER);
        if (tem == null) {
            helper.fail("quem a bebe apanha Definhamento");
            return;
        }
        if (tem.getAmplifier() != 3) {
            helper.fail("e de grau quatro: é " + (tem.getAmplifier() + 1));
            return;
        }
        if (InfusedBrews.qual(quem) != InfusedBrews.NENHUM) {
            helper.fail("e ela não infunde nada: é só a base");
            return;
        }
        helper.succeed();
    }

    /** <b>Beber um infunde por duas horas</b>, e beber o outro apaga o primeiro. */
    @GameTest(maxTicks = 40)
    public void drinkingOneReplacesTheOther(GameTestHelper helper) {
        piso(helper);
        var quem = naArena(helper, 2.5, 2.5, 0.0f);
        ServerLevel level = helper.getLevel();

        ItemStack voar = new ItemStack(OccultaItems.BREW_OF_SOARING);
        voar.getItem().finishUsingItem(voar, level, quem);
        if (!InfusedBrews.tem(quem, InfusedBrews.VOAR)) {
            helper.fail("beber o de Voar infunde o de Voar");
            return;
        }
        if (InfusedBrews.falta(quem) != InfusedBrews.DURA) {
            helper.fail("e ele começa inteiro: faltam " + InfusedBrews.falta(quem));
            return;
        }
        if (InfusedBrews.DURA != 144000L) {
            helper.fail("duas horas de jogo, que são cento e quarenta e quatro mil batidas");
            return;
        }

        ItemStack cova = new ItemStack(OccultaItems.BREW_OF_THE_GRAVE);
        cova.getItem().finishUsingItem(cova, level, quem);
        if (!InfusedBrews.tem(quem, InfusedBrews.COVA)) {
            helper.fail("e beber o da Cova troca um pelo outro");
            return;
        }
        if (InfusedBrews.tem(quem, InfusedBrews.VOAR)) {
            helper.fail("não se anda com os dois: o estado é um número só");
            return;
        }
        helper.succeed();
    }

    /** <b>E quem bebeu o da Cova conta como morto-vivo.</b> */
    @GameTest(maxTicks = 40)
    public void theGraveMakesYouUndead(GameTestHelper helper) {
        piso(helper);
        var quem = naArena(helper, 2.5, 2.5, 0.0f);
        if (Undead.é(quem)) {
            helper.fail("gente não é morto-vivo");
            return;
        }
        InfusedBrews.bebe(quem, InfusedBrews.COVA);
        if (!Undead.é(quem)) {
            helper.fail("e com o da Cova passa a ser");
            return;
        }
        InfusedBrews.bebe(quem, InfusedBrews.VOAR);
        if (Undead.é(quem)) {
            helper.fail("mas só com o da Cova: o de Voar não faz isso");
            return;
        }
        // e um zumbi é morto-vivo sem beber nada
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 2, 4));
        if (!Undead.é(zumbi)) helper.fail("e o zumbi continua a ser o que sempre foi");
        else helper.succeed();
    }

    /**
     * <b>E matar cura</b>, por feitio do morto.
     *
     * <p>A conta é da <b>vida inteira</b> e não de pontos, e a diferença entre um bicho e um aldeão é o
     * que o cozimento quer dizer sem o dizer.
     */
    @GameTest(maxTicks = 40)
    public void killingFeedsTheGrave(GameTestHelper helper) {
        piso(helper);
        var quem = naArena(helper, 2.5, 2.5, 0.0f);
        InfusedBrews.bebe(quem, InfusedBrews.COVA);
        float cheio = quem.getMaxHealth();

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
        quem.setHealth(1.0f);
        GraveFeeding.come(quem, porco);
        float doBicho = quem.getHealth() - 1.0f;
        if (Math.abs(doBicho - cheio * InfusedBrews.DE_BICHO) > 0.01f) {
            helper.fail("um bicho dá um décimo da vida inteira: deu " + doBicho);
            return;
        }

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(4, 2, 1));
        quem.setHealth(1.0f);
        GraveFeeding.come(quem, aldeão);
        float doAldeão = quem.getHealth() - 1.0f;
        if (doAldeão <= doBicho) {
            helper.fail("e um aldeão dá mais do que um bicho");
            return;
        }
        if (Math.abs(doAldeão - cheio * InfusedBrews.DE_ALDEÃO) > 0.01f) {
            helper.fail("quase metade da vida inteira: deu " + doAldeão);
            return;
        }
        porco.discard();
        aldeão.discard();
        helper.succeed();
    }

    /**
     * <b>E a Pedra Necrótica escraviza com o olhar</b> — mas só com o cozimento, e só de dez em dez
     * segundos.
     */
    @GameTest(maxTicks = 60)
    public void theStoneEnslavesWhatYouLookAt(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        // o jogador olha para o norte (rumo 180), e o zumbi fica a norte dele
        var quem = naArena(helper, 2.5, 4.5, 180.0f);
        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 1));
        ItemStack pedra = new ItemStack(OccultaItems.NECROTIC_STONE);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pedra);

        // sem o cozimento, nada acontece
        pedra.getItem().use(level, quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (Enslavement.escravizado(zumbi)) {
            helper.fail("sem o cozimento a pedra não escraviza ninguém");
            return;
        }

        InfusedBrews.bebe(quem, InfusedBrews.COVA);
        pedra.getItem().use(level, quem, net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!Enslavement.escravoDe(zumbi, quem)) {
            helper.fail("com ele, o zumbi que ela vir passa a ser de quem olhou");
            return;
        }
        if (InfusedBrews.aPedraPode(quem)) {
            helper.fail("e ela espera dez segundos antes de outra vez");
            return;
        }
        zumbi.discard();
        helper.succeed();
    }

    /** E a vassoura ganha o empurrão do de Voar, que soma com o da coruja. */
    @GameTest(maxTicks = 20)
    public void theBroomGainsFromSoaring(GameTestHelper helper) {
        if (InfusedBrews.VOAR_EMPURRÃO != 0.1) {
            helper.fail("o de Voar soma um décimo ao empurrão: soma "
                    + InfusedBrews.VOAR_EMPURRÃO);
            return;
        }
        if (net.thaumcraft.occulta.broom.BroomEntity.VOAR_EMPURRÃO
                != InfusedBrews.VOAR_EMPURRÃO) {
            helper.fail("e é o mesmo número dos dois lados");
            return;
        }
        if (net.thaumcraft.occulta.broom.BroomEntity.CORUJA_EMPURRÃO <= 0.0) {
            helper.fail("e a coruja continua a somar o dela");
            return;
        }
        helper.succeed();
    }

    /** E o relógio apaga-o: passadas as duas horas, não há cozimento nenhum. */
    @GameTest(maxTicks = 40)
    public void theClockRunsOut(GameTestHelper helper) {
        piso(helper);
        Player quem = naArena(helper, 2.5, 2.5, 0.0f);
        long agora = helper.getLevel().getGameTime();
        // posto como se tivesse sido bebido há duas horas e uma batida
        quem.setAttached(InfusedBrews.INFUNDIDO, new InfusedBrews.Infundido(
                InfusedBrews.COVA, agora - InfusedBrews.DURA - 1L, 0L));
        if (InfusedBrews.qual(quem) != InfusedBrews.NENHUM) {
            helper.fail("passadas as duas horas ele não vale mais");
            return;
        }
        if (Undead.é(quem)) {
            helper.fail("e quem o bebeu volta a ser gente");
            return;
        }
        InfusedBrews.tique(helper.getLevel());
        if (InfusedBrews.de(quem).qual() != InfusedBrews.NENHUM) {
            helper.fail("e o tique limpa-o de vez");
            return;
        }
        helper.succeed();
    }
}
