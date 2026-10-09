package net.thaumcraft.test;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.AltarPower;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.demon.DemonEntity;
import net.thaumcraft.occulta.demon.DemonHeartItem;
import net.thaumcraft.occulta.demon.DemonTrades;
import net.thaumcraft.occulta.rite.Rites;

/**
 * O <b>Demônio</b> e o <b>Coração</b> dele.
 *
 * <p>A prova que carrega a fatia é a do <b>coração comido</b>: ele dá mais poder do que qualquer outra coisa
 * deste mod — e põe fogo em quem o come por <b>doze segundos a mais</b> do que a Resistência ao Fogo dura. É
 * nesses doze segundos, cego, que o negócio se cobra, e nenhuma linha do jogo o diz.
 *
 * <p>E a segunda é a da <b>moeda</b>: quem paga com a matéria do inferno leva o estouro, e a matéria do
 * inferno é <b>vara de blaze e creme de magma</b>. O pó de blaze, que se parece com as duas, é justamente o
 * que o <b>manda embora</b> no rito de banir. Trocar um pelo outro arruinaria a armadilha, e é um erro de uma
 * letra num nome de campo.
 */
public class OccultaDemonGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** E o fogo que o estouro deixa não fica para a prova do lado. */
    private static void apaga(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = -2; x < 10; x++) {
            for (int z = -2; z < 10; z++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos onde = helper.absolutePos(new BlockPos(x, y, z));
                    if (level.getBlockState(onde).is(Blocks.FIRE)) {
                        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    /** Os números dele são os do original. */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (DemonEntity.VIDA != 100.0f) helper.fail("cem de vida");
        if (DemonEntity.TETO_DE_DANO != 15.0f) helper.fail("e nada lhe tira mais do que quinze de uma vez");
        if (DemonEntity.SOCO != 7.0f || DemonEntity.SOCO_A_MAIS != 15) {
            helper.fail("de perto ele bate sete mais até quinze");
        }
        if (DemonEntity.ESTOURO != 3.0f || DemonEntity.ESPERA_DO_ESTOURO != 50) {
            helper.fail("e o estouro é de três, cinquenta batidas depois");
        }
        if (DemonEntity.VÊ_A != 32.0) helper.fail("ele vê a trinta e dois, que era a omissão de 2014");
        if (Rites.HellOnEarth.DEMÔNIO != 0.02) helper.fail("dois em cem do Inferno na Terra é um demônio");
        helper.succeed();
    }

    /**
     * <b>Nada lhe tira mais do que quinze de uma vez.</b>
     *
     * <p>É o que o torna uma coisa que se negocia em vez de se matar: uma espada encantada leva o mesmo tempo
     * que uma de madeira, e o que muda é só a paciência de quem bate.
     */
    @GameTest(maxTicks = 40)
    public void nothingTakesMoreThanFifteenFromHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var demônio = helper.spawn(OccultaEntities.DEMON, new BlockPos(3, 2, 3));

        demônio.setHealth(DemonEntity.VIDA);
        demônio.invulnerableTime = 0;
        demônio.hurtServer(level, level.damageSources().generic(), 1000.0f);
        float sobrou = demônio.getHealth();
        if (sobrou < DemonEntity.VIDA - DemonEntity.TETO_DE_DANO - 0.01f) {
            helper.fail("mil de dano tiram quinze, e tiraram " + (DemonEntity.VIDA - sobrou));
        }
        if (sobrou > DemonEntity.VIDA - DemonEntity.TETO_DE_DANO + 0.01f) {
            helper.fail("mas tiram os quinze inteiros");
        }

        // e o fogo não lhe faz nada, nem a água
        if (!demônio.fireImmune()) helper.fail("um demônio não arde");
        if (!demônio.canBreatheUnderwater()) helper.fail("nem se afoga");

        demônio.discard();
        helper.succeed();
    }

    /**
     * <b>Pagar a um demônio com o fogo dele é uma armadilha — e o fogo dele tem nome.</b>
     *
     * <p>Vara de blaze e <b>creme de magma</b> são a matéria do inferno. O <b>pó</b> de blaze não é: é o que
     * entra no sacrifício do rito de banir, ou seja, é exatamente o que o <b>manda embora</b>.
     *
     * <p>E quem lhe paga com a matéria do inferno faz o negócio — e o demônio estoura cinquenta batidas
     * depois, que é o tempo de quem fez o negócio se virar e começar a andar. <b>Ele não morre no estouro</b>:
     * no original o campo chama-se {@code tryEscape}, e o estouro é a fuga dele. Quem fica no buraco é quem
     * pagou.
     */
    @GameTest(maxTicks = 100)
    public void payingHimInFireIsATrap(GameTestHelper helper) {
        piso(helper);
        if (!DemonTrades.éFogo(Items.BLAZE_ROD)) helper.fail("a vara de blaze é o fogo dele");
        if (!DemonTrades.éFogo(Items.MAGMA_CREAM)) helper.fail("e o creme de magma também");
        if (DemonTrades.éFogo(Items.BLAZE_POWDER)) {
            helper.fail("mas o pó de blaze não: esse é o que o manda embora no rito de banir");
        }
        if (DemonTrades.éFogo(Items.GOLD_INGOT) || DemonTrades.éFogo(Items.EMERALD)
                || DemonTrades.éFogo(Items.DIAMOND)) {
            helper.fail("e nenhuma das moedas honestas o solta");
        }

        var demônio = helper.spawn(OccultaEntities.DEMON, new BlockPos(3, 3, 3));
        demônio.setHealth(DemonEntity.VIDA);

        /*
         * Quem prova o estouro é uma ovelha ao lado dele, e não um jogador: o jogador de mentira das provas
         * está travado em criativo, e em criativo não se leva estouro nenhum.
         */
        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(4, 2, 3));
        float tinha = ovelha.getHealth();

        demônio.pagaramComFogo();
        helper.startSequence()
                .thenExecuteAfter(DemonEntity.ESPERA_DO_ESTOURO + 10, () -> {
                    if (demônio.isRemoved()) helper.fail("o demônio não morre no próprio estouro");
                    /*
                     * E a pergunta é só se ela <b>levou</b>. O estouro reparte o dano por raios sorteados, e
                     * a mesma ovelha no mesmo lugar ora cai, ora fica com um fio de vida, ora leva um
                     * arranhão — <b>mudar de arena basta para virar a moeda</b>, e acrescentar uma prova
                     * nova a outro lugar do mod muda as arenas de todas. Pedir um número é pedir que ela
                     * falhe um dia; o que a prova quer saber é se o estouro <b>chega</b> a quem está ao
                     * lado.
                     */
                    float agora = ovelha.isAlive() ? ovelha.getHealth() : 0.0f;
                    if (agora >= tinha) {
                        helper.fail("mas quem está ao lado dele leva, e a ovelha ficou com " + agora
                                + " de " + tinha);
                    }
                    demônio.discard();
                    ovelha.discard();
                    apaga(helper);
                })
                .thenSucceed();
    }

    /**
     * <b>A lista dele tem sempre um coração, e tem sempre seis a nove trocas.</b>
     *
     * <p>A ordem em que o original a monta é o que faz isso ser verdade: os livros entram primeiro e tantos
     * quantas as trocas couberem, as coisas do ofício entram depois, tudo se <b>embaralha</b>, o Coração é
     * enfiado num dos <b>três primeiros lugares</b> e só então se <b>corta</b>. O Coração entra depois de
     * embaralhar, e por isso nunca é cortado.
     *
     * <p>E cada troca se faz <b>duas vezes</b>, que é o que sobra das sete de um pedido novo depois do
     * desconto de cinco do original. Um demônio vende dois corações, e nunca mais.
     */
    @GameTest(maxTicks = 20)
    public void hisListAlwaysHasAHeartInTheFirstThree(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        if (DemonTrades.ESTOQUE != 2) helper.fail("cada troca se faz duas vezes");

        for (int volta = 0; volta < 40; volta++) {
            var lista = DemonTrades.monta(level);
            if (lista.size() < DemonEntity.TROCAS_DE
                    || lista.size() > DemonEntity.TROCAS_DE + DemonEntity.TROCAS_A_MAIS - 1) {
                helper.fail("de seis a nove trocas, e deu " + lista.size());
                return;
            }
            int onde = -1;
            int quantos = 0;
            for (int n = 0; n < lista.size(); n++) {
                if (!lista.get(n).getResult().is(OccultaItems.DEMON_HEART)) continue;
                quantos++;
                onde = n;
            }
            if (quantos != 1) {
                helper.fail("um coração, e só um, e deu " + quantos);
                return;
            }
            if (onde > 2) {
                helper.fail("e num dos três primeiros lugares, e deu o " + onde);
                return;
            }
            for (var troca : lista) {
                if (troca.getMaxUses() != DemonTrades.ESTOQUE) {
                    helper.fail("cada troca se faz duas vezes, e esta se faz " + troca.getMaxUses());
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * <b>E esta é a prova que carrega a fatia: o fogo passa da proteção por doze segundos.</b>
     *
     * <p>Comer o coração dá Vida Extra V, Regeneração II, Força III, Rapidez III e Resistência ao Fogo III
     * por <b>dois minutos</b> — mais poder do que qualquer outra coisa deste mod dá de uma vez. E põe fogo em
     * quem come por <b>dois minutos e doze segundos</b>.
     *
     * <p>Doze segundos, cego, sem proteção, ardendo. É o preço, e ele vem no fim.
     */
    @GameTest(maxTicks = 40)
    public void eatingTheHeartBurnsLongerThanItProtects(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        Player quem = helper.makeMockServerPlayerInLevel();
        quem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(OccultaItems.DEMON_HEART));
        quem.clearFire();

        quem.getMainHandItem().use(level, quem, InteractionHand.MAIN_HAND);

        List<Holder<MobEffect>> devia = List.of(
                MobEffects.HEALTH_BOOST, MobEffects.REGENERATION, MobEffects.STRENGTH,
                MobEffects.SPEED, MobEffects.FIRE_RESISTANCE, MobEffects.BLINDNESS, MobEffects.HUNGER);
        for (Holder<MobEffect> qual : devia) {
            if (quem.getEffect(qual) == null) {
                helper.fail("comer o coração devia ter dado " + qual.getRegisteredName());
            }
        }

        var fogo = quem.getEffect(MobEffects.FIRE_RESISTANCE);
        if (fogo != null && fogo.getAmplifier() != 2) helper.fail("a resistência ao fogo é do terceiro grau");
        if (fogo != null && fogo.getDuration() != DemonHeartItem.QUANTO) {
            helper.fail("e dura dois minutos");
        }

        if (quem.getRemainingFireTicks() <= 0) helper.fail("e comê-lo põe fogo em quem come");
        if (DemonHeartItem.O_FOGO <= DemonHeartItem.QUANTO) {
            helper.fail("o fogo tem de durar mais do que a proteção contra ele");
        }
        if (DemonHeartItem.O_FOGO - DemonHeartItem.QUANTO != 240) {
            helper.fail("e passa dela por doze segundos exatos");
        }
        quem.clearFire();
        helper.succeed();
    }

    /** O Coração posto no chão é a fonte de poder mais forte que um Altar pode ter. */
    @GameTest(maxTicks = 20)
    public void theHeartIsTheStrongestAltarPower(GameTestHelper helper) {
        boolean achou = false;
        int maior = 0;
        for (AltarPower.Source fonte : AltarPower.sources()) {
            if (fonte.what().test(OccultaBlocks.DEMON_HEART.defaultBlockState())
                    && fonte.factor() == 40 && fonte.limit() == 2) {
                achou = true;
            }
            // o Ovo do Dragão vale mais por peça, mas só conta um
            if (fonte.limit() >= 2 && fonte.factor() > maior) maior = fonte.factor();
        }
        if (!achou) helper.fail("o Coração vale quarenta, contando até dois");
        if (maior != 40) helper.fail("e é o maior de tudo o que conta mais do que um, e deu " + maior);
        helper.succeed();
    }

    /**
     * <b>Só o demônio chamado por alguém fica.</b>
     *
     * <p>O do rito de chamar é marcado e persiste; o que o Inferno na Terra cospe não é, e por isso o rito
     * mais caro do mod dá demônios que vão embora se ninguém estiver olhando.
     */
    @GameTest(maxTicks = 20)
    public void onlyASummonedDemonStays(GameTestHelper helper) {
        piso(helper);
        /*
         * A arena põe persistência em tudo o que nasce nela, e por isso o que se prova aqui é a <b>marca</b>:
         * um demônio qualquer não a tem, o do rito tem, e tê-la é o que o faz ficar.
         */
        var qualquer = helper.spawn(OccultaEntities.DEMON, new BlockPos(2, 2, 2));
        if (qualquer.foiChamado()) helper.fail("um demônio qualquer não foi chamado por ninguém");

        var chamado = helper.spawn(OccultaEntities.DEMON, new BlockPos(5, 2, 5));
        chamado.marcaComoChamado();
        if (!chamado.foiChamado()) helper.fail("o do rito foi chamado");
        if (!chamado.isPersistenceRequired()) helper.fail("e por isso fica");
        if (chamado.removeWhenFarAway(4096.0)) helper.fail("e nunca se apaga por distância");

        qualquer.discard();
        chamado.discard();
        helper.succeed();
    }

    /**
     * <b>Banir</b> apaga o que é de lá, e só o que é de lá.
     *
     * <p>É o botão de desfazer de quem chamou mais do que devia — e por isso ele é barato: pó de blaze e uma
     * pedra.
     */
    @GameTest(maxTicks = 40)
    public void banishingOnlyTakesWhatComesFromThere(GameTestHelper helper) {
        piso(helper);
        var demônio = helper.spawn(OccultaEntities.DEMON, new BlockPos(3, 2, 3));
        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(5, 2, 5));

        if (!Rites.BanishDemon.deLá(demônio)) helper.fail("um demônio é de lá");
        if (Rites.BanishDemon.deLá(ovelha)) helper.fail("uma ovelha não é");

        demônio.discard();
        ovelha.discard();
        helper.succeed();
    }

    /** E o Inferno na Terra cospe o Nether pela porta que abriu, com dois por cento de demônio. */
    @GameTest(maxTicks = 20)
    public void hellOnEarthSpitsTheNetherOut(GameTestHelper helper) {
        if (Rites.HellOnEarth.sorteia(0.01) != OccultaEntities.DEMON) {
            helper.fail("abaixo de dois em cem sai um demônio");
        }
        if (Rites.HellOnEarth.sorteia(0.05) != EntityTypes.GHAST) helper.fail("depois dele, o ghast");
        if (Rites.HellOnEarth.sorteia(0.2) != EntityTypes.BLAZE) helper.fail("depois, o blaze");
        if (Rites.HellOnEarth.sorteia(0.5) != EntityTypes.MAGMA_CUBE) helper.fail("depois, o cubo de magma");
        if (Rites.HellOnEarth.sorteia(0.9) != EntityTypes.ZOMBIFIED_PIGLIN) {
            helper.fail("e o resto são zumbis-porcos");
        }
        helper.succeed();
    }

    /**
     * E ele <b>estraga o chão</b> enquanto cresce: o que é vivo vira pedra do Nether.
     *
     * <p>Mais perto do meio, mais fundo — uma casa em duas no terço de dentro, uma em quatro na metade, uma
     * em seis no resto. É isso que deixa no fim uma mancha densa no centro e esfarrapada nas pontas, em vez
     * de um disco de pedra do Nether.
     */
    @GameTest(maxTicks = 40)
    public void hellOnEarthSpoilsTheGroundItCrosses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var rito = new Rites.HellOnEarth(20, 15, 200.0f);

        // terra, com ar em cima, é o que ele come
        BlockPos chão = helper.absolutePos(new BlockPos(3, 1, 3));
        int virou = 0;
        for (int volta = 0; volta < 200; volta++) {
            level.setBlockAndUpdate(chão, Blocks.GRASS_BLOCK.defaultBlockState());
            rito.onBlock(level, chão, 1, null, false);
            if (level.getBlockState(chão).is(Blocks.NETHERRACK)) virou++;
        }
        if (virou == 0 || virou == 200) {
            helper.fail("no terço de dentro ele estraga uma casa em duas, e estragou " + virou + " de 200");
        }

        // e a pedra não: o inferno na terra come o que é vivo e deixa o que é construído
        level.setBlockAndUpdate(chão, Blocks.STONE_BRICKS.defaultBlockState());
        for (int volta = 0; volta < 50; volta++) rito.onBlock(level, chão, 1, null, false);
        if (!level.getBlockState(chão).is(Blocks.STONE_BRICKS)) {
            helper.fail("mas o que é construído fica");
        }

        level.setBlockAndUpdate(chão, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }
}
