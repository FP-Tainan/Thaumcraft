package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.vampire.Blood;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampirePowers;
import net.thaumcraft.occulta.vampire.VampireStats;
import net.thaumcraft.occulta.vampire.VampireTick;
import net.thaumcraft.occulta.wolf.Werewolf;

/**
 * O <b>corpo do vampiro</b>: o sangue, a sede, o sol e a mordida.
 *
 * <p>A prova que carrega a fatia é a do <b>sangue</b>, porque ele é três coisas ao mesmo tempo — a comida, o
 * combustível e o guarda-sol. Tirar o sangue dele é tirar as três de uma vez, e é isso que faz de um
 * vampiro uma coisa que se joga com pressa.
 */
public class OccultaVampirePlayerGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um vampiro do grau pedido, com o sangue cheio. */
    private static Player vampiro(GameTestHelper helper, int grau) {
        Player quem = mortal(helper.makeMockServerPlayerInLevel());
        Vampire.levantaOTeto(quem, Vampire.TETO);
        Vampire.grau(quem, grau);
        Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
        return quem;
    }

    /**
     * Um jogador de mentira a quem a vampirice custe alguma coisa.
     *
     * <p>Ele nasce no <b>criativo</b>, e no criativo um vampiro <b>não gasta sangue</b> — é o original, e é
     * de propósito. Para provar o que ser vampiro custa, é preciso tirar dele primeiro a mão que não paga.
     */
    private static Player mortal(Player quem) {
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        }
        quem.getAbilities().instabuild = false;
        quem.getAbilities().invulnerable = false;
        quem.onUpdateAbilities();
        return quem;
    }

    /** Os números dele são os do original. */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (Vampire.TETO != 10) helper.fail("são dez graus, como no lobisomem");
        if (Vampire.SANGUE_DO_PRIMEIRO != 125) helper.fail("um vampiro novo nasce com cento e vinte e cinco");
        if (Vampire.SANGUE_BASE != 500 || Vampire.SANGUE_POR_GRAU != 250) {
            helper.fail("o teto é quinhentos mais duzentos e cinquenta por grau");
        }
        if (Blood.TETO != 500) helper.fail("todo vivo tem quinhentos de sangue");
        if (VampireTick.DE_QUANTO_EM_QUANTO != 40) helper.fail("o relógio é de dois em dois segundos");
        if (VampireTick.AGUENTA_O_SOL_AOS != 5) helper.fail("do quinto grau ele aguenta o sol");
        if (VampireStats.DANO[10] != 3.0f) helper.fail("e o dano dele para em três");
        helper.succeed();
    }

    /**
     * <b>O teto do sangue cresce com o grau</b> — e pela metade em quem também é lobisomem.
     *
     * <p>É a única linha do mod em que as duas maldições se olham, e ela diz uma coisa clara: ser as duas
     * custa.
     */
    @GameTest(maxTicks = 40)
    public void theBloodCeilingGrowsAndHybridsPayForIt(GameTestHelper helper) {
        Player quem = vampiro(helper, 10);
        int sozinho = Vampire.tetoDoSangue(quem);
        if (sozinho != Vampire.SANGUE_BASE + 10 * Vampire.SANGUE_POR_GRAU) {
            helper.fail("um vampiro de grau dez devia ter três mil de teto, e tem " + sozinho);
        }

        Werewolf.grau(quem, Vampire.HÍBRIDO_AOS);
        int híbrido = Vampire.tetoDoSangue(quem);
        if (híbrido >= sozinho) helper.fail("ser lobisomem devia custar-lhe teto, e não custou");
        if (híbrido != Vampire.SANGUE_BASE + 5 * Vampire.SANGUE_POR_GRAU) {
            helper.fail("devia crescer pela metade, e deu " + híbrido);
        }

        // e um lobisomem de grau um ainda não conta
        Werewolf.grau(quem, 1);
        if (Vampire.tetoDoSangue(quem) != sozinho) helper.fail("o híbrido começa no segundo grau de lobo");
        Werewolf.grau(quem, 0);
        helper.succeed();
    }

    /**
     * <b>Encher o sangue no primeiro grau sobe ao segundo</b>: o único degrau que não se procura.
     *
     * <p><b>Corrigido nesta fatia:</b> esta prova dizia antes que, <i>sem ter lido nada</i>, nem esse degrau
     * vinha — e estava errada. No original os dois primeiros degraus <b>não perguntam pelo teto</b>; só do
     * terceiro em diante é que alguém o olha. Um vampiro recém-nascido sobe ao segundo e ao terceiro sem
     * livro nenhum, e é aí que ele para. A conta está no {@link Vampire#tetoDoGrau}.
     */
    @GameTest(maxTicks = 40)
    public void fillingTheBloodAtTheFirstGradeClimbs(GameTestHelper helper) {
        Player semLer = helper.makeMockServerPlayerInLevel();
        Vampire.grau(semLer, 1);
        Vampire.bebe(semLer, 9999);
        if (Vampire.grauDe(semLer) != 2) helper.fail("sem livro nenhum, o sangue cheio ainda sobe o grau");

        Player quem = helper.makeMockServerPlayerInLevel();
        Vampire.levantaOTeto(quem, Vampire.TETO);
        Vampire.grau(quem, 1);
        if (Vampire.sangueDe(quem) != Vampire.SANGUE_DO_PRIMEIRO) {
            helper.fail("ele devia nascer com cento e vinte e cinco");
        }
        Vampire.bebe(quem, 9999);
        if (Vampire.grauDe(quem) != 2) helper.fail("o sangue cheio devia tê-lo levado ao segundo grau");
        helper.succeed();
    }

    /** O teto do grau nunca fica abaixo de três, e nunca desce. */
    @GameTest(maxTicks = 20)
    public void theGradeCeilingOnlyGoesUp(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Vampire.levantaOTeto(quem, 1);
        if (Vampire.de(quem).teto() != Vampire.MENOR_TETO) helper.fail("o menor teto é três");
        Vampire.levantaOTeto(quem, 7);
        if (Vampire.de(quem).teto() != 7) helper.fail("e ele sobe quando se lê mais");
        Vampire.levantaOTeto(quem, 4);
        if (Vampire.de(quem).teto() != 7) helper.fail("mas nunca desce");
        helper.succeed();
    }

    /** <b>Beber de quem está acordado dá dois terços</b>, e beber demais mata. */
    @GameTest(maxTicks = 60)
    public void drinkingFromTheAwakeGivesLessAndHurts(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 1);

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 3));
        int saiu = Blood.tira(level, aldeão, 10, quem);
        if (saiu != (int) Math.ceil(10 * Blood.ACORDADO)) {
            helper.fail("de quem está acordado saem dois terços, e saíram " + saiu);
        }

        // e de quem está desacordado sai tudo
        aldeão.addEffect(new MobEffectInstance(OccultaEffects.PARALYSIS, 200,
                Blood.PARALISIA_QUE_DESACORDA));
        int tudo = Blood.tira(level, aldeão, 10, quem);
        if (tudo != 10) helper.fail("de quem não se debate sai tudo, e saíram " + tudo);

        // abaixo da metade, cada gole fere
        Blood.põe(aldeão, Blood.METADE - 1);
        float antes = aldeão.getHealth();
        Blood.tira(level, aldeão, 10, quem);
        if (aldeão.getHealth() >= antes) helper.fail("abaixo da metade a mordida devia ter ferido");

        aldeão.discard();
        helper.succeed();
    }

    /**
     * <b>Sangue de bicho mantém vivo e não faz forte</b>: nunca passa de um quarto do teto.
     *
     * <p>É a regra mais elegante do vampiro, porque não proíbe nada — deixa quem não quiser morder gente
     * sobreviver, e prende-o no primeiro grau para sempre.
     */
    @GameTest(maxTicks = 40)
    public void animalBloodKeepsYouAliveAndWeak(GameTestHelper helper) {
        Player quem = helper.makeMockServerPlayerInLevel();
        Vampire.levantaOTeto(quem, Vampire.TETO);
        Vampire.grau(quem, 1);
        Vampire.sangue(quem, 0);

        int quarto = (int) Math.ceil(Vampire.tetoDoSangue(quem) * 0.25f);
        for (int n = 0; n < 1000; n++) Vampire.bebeDeBicho(quem, VampirePowers.GOLE_DE_BICHO);
        if (Vampire.sangueDe(quem) != quarto) {
            helper.fail("sangue de bicho para num quarto do teto, e deu " + Vampire.sangueDe(quem));
        }
        if (Vampire.grauDe(quem) != 1) helper.fail("e por isso ele nunca passa do primeiro grau assim");

        // e sangue de gente passa disso
        Vampire.bebe(quem, 100);
        if (Vampire.sangueDe(quem) <= quarto) helper.fail("sangue de gente não tem esse limite");
        helper.succeed();
    }

    /** <b>Comida não alimenta um vampiro</b>, e o sangue alimenta. */
    @GameTest(maxTicks = 60)
    public void foodDoesNotFeedHimButBloodDoes(GameTestHelper helper) {
        piso(helper);
        Player quem = vampiro(helper, 3);
        quem.getFoodData().setFoodLevel(0);

        var pão = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD);
        var oquê = pão.get(net.minecraft.core.component.DataComponents.FOOD);
        if (oquê == null) {
            helper.fail("um pão devia ser comida");
            return;
        }
        oquê.onConsume(helper.getLevel(), quem, pão,
                pão.get(net.minecraft.core.component.DataComponents.CONSUMABLE));
        if (quem.getFoodData().getFoodLevel() != 0) helper.fail("pão não alimenta um vampiro");

        // e o relógio, que converte sangue em comida
        int tinha = Vampire.sangueDe(quem);
        VampireTick.cobra(helper.getLevel(), quem);
        if (quem.getFoodData().getFoodLevel() <= 0) helper.fail("o sangue dele devia ter virado comida");
        if (Vampire.sangueDe(quem) >= tinha) helper.fail("e devia ter custado sangue");

        // a quem é gente, o pão alimenta
        Player gente = helper.makeMockServerPlayerInLevel();
        gente.getFoodData().setFoodLevel(0);
        oquê.onConsume(helper.getLevel(), gente, pão,
                pão.get(net.minecraft.core.component.DataComponents.CONSUMABLE));
        if (gente.getFoodData().getFoodLevel() <= 0) helper.fail("e gente come pão");
        helper.succeed();
    }

    /** <b>Sem sangue nem comida vem a maldição da sede</b>: Fraqueza IX, Lentidão II e Fadiga II. */
    @GameTest(maxTicks = 60)
    public void thirstCursesTheEmpty(GameTestHelper helper) {
        piso(helper);
        Player quem = vampiro(helper, 3);
        Vampire.sangue(quem, 0);
        quem.getFoodData().setFoodLevel(0);

        VampireTick.cobra(helper.getLevel(), quem);
        var fraco = quem.getEffect(MobEffects.WEAKNESS);
        if (fraco == null || fraco.getAmplifier() != VampireTick.FRAQUEZA_DA_SEDE) {
            helper.fail("a sede devia tê-lo deixado fraco de grau nove");
        }
        if (!quem.hasEffect(MobEffects.SLOWNESS) || !quem.hasEffect(MobEffects.MINING_FATIGUE)) {
            helper.fail("e lento, e sem força para cavar");
        }
        helper.succeed();
    }

    /**
     * <b>O sol, nos seus quatro degraus.</b>
     *
     * <p>A prova não tem céu, e por isso ela chama a conta do sol e não o relógio: o que importa é o
     * <b>grau</b>. Um vampiro novo perde tudo de uma vez; um velho paga sessenta e aguenta.
     */
    @GameTest(maxTicks = 60)
    public void theSunHasFourDegrees(GameTestHelper helper) {
        piso(helper);

        Player novo = vampiro(helper, 1);
        int tinha = Vampire.sangueDe(novo);
        if (tinha <= 0) helper.fail("ele tinha de ter sangue para perder");
        aoSol(novo);
        if (Vampire.sangueDe(novo) != 0) {
            helper.fail("abaixo do quinto grau o sol zera o sangue, e sobraram " + Vampire.sangueDe(novo));
        }
        if (!novo.isOnFire()) helper.fail("e zerado o sangue, ele pega fogo");

        Player velho = vampiro(helper, VampireTick.AGUENTA_O_SOL_AOS);
        int cheio = Vampire.sangueDe(velho);
        aoSol(velho);
        if (Vampire.sangueDe(velho) <= 0) helper.fail("do quinto grau ele aguenta o sol");
        if (Vampire.sangueDe(velho) != cheio - VampireTick.O_SOL_TIRA) {
            helper.fail("e o sol devia ter-lhe tirado sessenta");
        }
        if (!velho.hasEffect(MobEffects.WEAKNESS)) helper.fail("e deixá-lo fraco");
        helper.succeed();
    }

    /** O castigo do sol, que é a parte da conta que não depende do céu. */
    private static void aoSol(Player quem) {
        if (Vampire.grauDe(quem) >= VampireTick.AGUENTA_O_SOL_AOS) {
            Vampire.gasta(quem, VampireTick.O_SOL_TIRA, false);
            quem.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, VampireTick.CASTIGO,
                    VampireTick.FRAQUEZA_DO_SOL, false, false));
        } else {
            Vampire.sangue(quem, 0);
        }
        if (Vampire.sangueDe(quem) == 0) quem.igniteForSeconds(VampireTick.PEGA_FOGO);
    }

    /**
     * <b>O sangue de um lobisomem é veneno</b>: morder um custa quatro de dor e não dá sangue nenhum.
     *
     * <p>É a única regra do mod em que as duas maldições se encontram, e ela diz que elas não se misturam.
     */
    @GameTest(maxTicks = 60)
    public void werewolfBloodIsPoison(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = vampiro(helper, 3);
        Vampire.sangue(quem, 0);
        VampirePowers.escolhe(quem, VampirePowers.Poder.BEBER);

        Player lobo = helper.makeMockServerPlayerInLevel();
        Werewolf.grau(lobo, 5);
        lobo.snapTo(quem.getX(), quem.getY(), quem.getZ(), 0.0f, 0.0f);

        if (!VampirePowers.bebe(level, quem, lobo)) helper.fail("ele devia ter tentado morder");
        if (Vampire.sangueDe(quem) != 0) helper.fail("sangue de lobisomem não alimenta ninguém");

        Werewolf.grau(lobo, 0);
        helper.succeed();
    }

    /**
     * E o poder escolhido roda, e só roda para quem é vampiro.
     *
     * <p>A roda inteira é de quem tem os <b>dez graus</b>: ela para no último poder que o grau dá, e a conta
     * disso está no {@link OccultaVampirePowersGameTest}.
     */
    @GameTest(maxTicks = 40)
    public void theChosenPowerCycles(GameTestHelper helper) {
        Player gente = helper.makeMockServerPlayerInLevel();
        VampirePowers.seguinte(gente);
        if (VampirePowers.escolhido(gente) != VampirePowers.Poder.NENHUM) {
            helper.fail("quem não é vampiro não escolhe poder nenhum");
        }

        Player quem = vampiro(helper, Vampire.TETO);
        var todos = VampirePowers.Poder.values();
        for (int n = 1; n < todos.length; n++) {
            VampirePowers.seguinte(quem);
            if (VampirePowers.escolhido(quem) != todos[n]) {
                helper.fail("a roda devia ter passado ao seguinte");
            }
        }
        VampirePowers.seguinte(quem);
        if (VampirePowers.escolhido(quem) != VampirePowers.Poder.NENHUM) {
            helper.fail("e depois do último vem nenhum outra vez");
        }

        if (VampirePowers.vêNoEscuro(quem)) helper.fail("a visão começa desligada");
        VampirePowers.viraAVisão(quem);
        if (!VampirePowers.vêNoEscuro(quem)) helper.fail("e liga-se");
        VampirePowers.viraAVisão(quem);
        if (VampirePowers.vêNoEscuro(quem)) helper.fail("e desliga-se");
        helper.succeed();
    }

    /** <b>Deixar de ser vampiro apaga tudo</b> — e devolve à pessoa o sangue de gente. */
    @GameTest(maxTicks = 40)
    public void theCureGivesBackTheBloodOfTheLiving(GameTestHelper helper) {
        Player quem = vampiro(helper, 7);
        if (VampireStats.de(quem) <= 0.0f) helper.fail("um vampiro de grau sete bate mais forte");
        Blood.põe(quem, 0);

        Vampire.grau(quem, 0);
        if (Vampire.é(quem)) helper.fail("ele devia ter deixado de ser vampiro");
        if (Vampire.sangueDe(quem) != 0) helper.fail("e o poder de sangue devia ter ido com ele");
        if (VampireStats.de(quem) != 0.0f) helper.fail("e o dano do grau também");
        if (Blood.de(quem) <= 0) helper.fail("e o sangue de gente devia ter voltado");
        helper.succeed();
    }

    /** E quem não é vampiro repõe o sangue sozinho, que é o que um vampiro vem buscar. */
    @GameTest(maxTicks = 40)
    public void the_living_make_blood_again(GameTestHelper helper) {
        piso(helper);
        Player gente = helper.makeMockServerPlayerInLevel();
        Blood.põe(gente, 100);
        VampireTick.cobra(helper.getLevel(), gente);
        if (Blood.de(gente) != 100 + Blood.RECUPERA) {
            helper.fail("o corpo devia ter reposto dois, e tem " + Blood.de(gente));
        }

        // e um vampiro não repõe: ele bebe
        Player vamp = vampiro(helper, 3);
        Blood.põe(vamp, 100);
        VampireTick.cobra(helper.getLevel(), vamp);
        if (Blood.de(vamp) != 100) helper.fail("um vampiro não faz sangue, toma o dos outros");
        helper.succeed();
    }
}
