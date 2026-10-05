package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>A escada dos dez graus do vampiro</b>: as dez subidas espalhadas pelo {@code GenericEvents}, o
 * {@code EntityGrenade}, a {@code EntityLilith} e o {@code ItemGlassGoblet}.
 *
 * <p>Ela é o contrário da {@linkplain net.thaumcraft.occulta.wolf.WerewolfLadder escada do lobisomem}, e a
 * diferença conta tudo sobre as duas maldições. O lobisomem tem uma <b>estátua</b> que lhe diz o que fazer:
 * ele chega, ela manda, ele volta. O vampiro <b>não tem ninguém</b>. Ninguém lhe diz nada, nada no jogo
 * aponta para o degrau seguinte, e o que ele sobe, sobe por ter reparado.
 *
 * <p>O que ele tem é um <b>livro</b> — e o livro não é um manual: é o diário de um erudito condenado que
 * jantou com um imortal e anotou o que ele contou, em reticências e meias-frases. {@linkplain
 * net.thaumcraft.occulta.vampire.VampireBookItem Ele também não está inteiro}, e só se lê até onde as
 * páginas chegam. O teto do grau <b>é</b> até onde o livro foi lido.
 *
 * <h2>Os dez degraus</h2>
 *
 * <ol>
 *   <li><b>Virar</b> — beber o sangue de Lilith, ou o de outro vampiro, num Cálice.</li>
 *   <li><b>Encher o sangue</b> até o teto, uma vez. <i>"a sede daquela primeira noite era avassaladora,
 *       ele teve de a saciar por inteiro"</i> — e está no {@link Vampire#bebe}, porque é o único degrau que
 *       não se procura.</li>
 *   <li><b>Cinco aldeões</b> mordidos <b>sem os esvaziar</b>: o sangue de cada um tem de ficar entre
 *       duzentos e cinquenta e duzentos e oitenta. Descendo de duzentos e quarenta, a conta <b>volta a
 *       zero</b>. <i>"podia beber como precisasse sem que os outros reparassem, desde que não tirasse mais
 *       do que metade"</i>.</li>
 *   <li><b>Dez minutos de noite</b>, acordado e sem fazer nada. <i>"foi na quarta noite depois de dominar a
 *       bebida que o mundo abrandou"</i>.</li>
 *   <li><b>Queimar-se dez vezes com o sol engarrafado</b> — a Granada Solar na própria cara. <i>"achou
 *       maneira de recolher a luz do sol e queimou-se com ela dez vezes durante a noite"</i>. É o degrau mais
 *       estranho do mod e o mais bonito: para aguentar o sol, ele tem de <b>se queimar de propósito</b>.</li>
 *   <li><b>Vinte Blazes</b>. <i>"precisava de mais força, e extinguir criaturas de fogo puro foi a solução...
 *       vinte morreram"</i>.</li>
 *   <li><b>Lilith outra vez</b>, com uma papoula na mão — a flor da cor do sangue que Ela tanto quer.</li>
 *   <li><b>Quatro aldeias</b> diferentes. <i>"voou de aldeia em aldeia até conhecer a extensão do seu
 *       domínio"</i>. Um pedaço de mundo onde ele já esteve não conta.</li>
 *   <li><b>Cinco aldeões em gaiolas</b>, mordidos com o mesmo cuidado do terceiro degrau. <i>"atraiu cinco
 *       deles a gaiolas de ferro preparadas, cobertas de madeira e com uma fresta à frente"</i>.</li>
 *   <li><b>Fazer outro vampiro</b>: o próprio sangue num Cálice, uma presa presa e vazia, e um Caixão ao
 *       lado.</li>
 * </ol>
 *
 * <p>Repare na forma deles. Três degraus pedem <b>moderação</b> — morder sem matar —, dois pedem que ele
 * <b>ande</b>, um pede que ele se <b>machuque</b>, e o último pede que ele <b>faça o que lhe fizeram</b>. Não
 * há um único degrau que peça matar um chefe ou achar um tesouro. A escada do vampiro é uma escada de
 * <b>hábitos</b>, e é por isso que ela leva tanto tempo.
 */
public final class VampireLadder {
    /** O que o terceiro e o nono degraus pedem: o sangue da presa entre estes dois números. */
    public static final int DEIXA_AO_MENOS = Blood.METADE;
    public static final int NÃO_PASSE_DE = 280;
    public static final int ESTRAGOU_ABAIXO_DE = 240;

    /** E quantas presas: cinco, nos dois degraus. */
    public static final int CINCO = 5;

    /** A noite do quarto degrau, em voltas do relógio — e a do criativo, que é curta. */
    public static final int A_NOITE = 300;
    public static final int A_NOITE_NO_CRIATIVO = 10;

    /** As queimaduras do quinto degrau, e os Blazes do sexto. */
    public static final int QUEIMADURAS = 10;
    public static final int BLAZES = 20;

    /** As aldeias do oitavo degrau, e a que distância uma aldeia conta. */
    public static final int ALDEIAS = 4;
    public static final int VÊ_A_ALDEIA_A = 32;

    /** A gaiola do nono degrau: quantas barras de dezesseis, e o teto por cima. */
    public static final int BARRAS = 15;
    public static final int TETO_DA_GAIOLA = 9;

    /** E a que distância da presa o Caixão do décimo degrau tem de estar. */
    public static final int O_CAIXÃO_A = 4;

    /**
     * <b>As aldeias onde ele já esteve</b>: os {@code visitedChunks} do original, para o oitavo degrau.
     *
     * <p>É a mesma ideia dos lugares onde o lobisomem uivou, e pela mesma razão: sem ela, bastava ficar
     * parado numa aldeia a contar. Com ela, ele tem de <b>andar</b> — e um vampiro que anda de aldeia em
     * aldeia é um vampiro que está aprendendo o tamanho do mundo dele.
     *
     * <p>Não vai sincronizada: quem a lê é sempre o servidor.
     */
    public static final AttachmentType<List<Long>> LUGARES = AttachmentRegistry.<List<Long>>builder()
            .initializer(List::of)
            .persistent(com.mojang.serialization.Codec.LONG.listOf())
            .buildAndRegister(net.thaumcraft.Thaumcraft.id("vampire_places"));

    private VampireLadder() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o terceiro e o nono

    /**
     * <b>Morder sem esvaziar</b>: o ramo do {@code onEntityInteract} que conta os dois degraus de aldeões.
     *
     * <p>Os dois pedem a mesma coisa — cinco aldeões cujo sangue fique na faixa estreita — e a diferença é
     * só <b>onde</b>: o terceiro em qualquer aldeão, o nono só em aldeões <b>engaiolados</b>.
     *
     * <p>E os dois têm a mesma crueldade de desenho: beber <b>demais</b> não falha a mordida, <b>apaga a
     * conta inteira</b>. Quatro aldeões bem mordidos e um mal mordido valem zero. É o mod a ensinar
     * moderação da única maneira que ele sabe, que é tirando.
     *
     * <p>A faixa é estreita de propósito: de quinhentos, parar entre duzentos e cinquenta e duzentos e
     * oitenta é <b>três goles e meio</b>. Quem morder de olhos fechados passa da conta.
     */
    public static void mordeu(ServerLevel level, Player quem, LivingEntity presa) {
        if (!(presa instanceof net.minecraft.world.entity.npc.villager.Villager aldeão)) return;

        int grau = Vampire.grauDe(quem);
        boolean terceiro = grau == 2;
        boolean nono = grau == 8 && Vampire.podeSubir(quem) && engaiolado(level, aldeão);
        if (!terceiro && !nono) return;

        int tem = Blood.de(aldeão);
        if (tem >= DEIXA_AO_MENOS && tem <= NÃO_PASSE_DE) {
            if (Vampire.contaDe(quem) >= CINCO - 1) {
                Vampire.sobeUmGrau(quem);
                Vampire.apagaAConta(quem);
            } else {
                Vampire.conta(quem);
                pling(level, quem);
            }
            return;
        }
        if (tem < ESTRAGOU_ABAIXO_DE) Vampire.apagaAConta(quem);
    }

    /**
     * <b>A gaiola</b>: o {@code villagerIsInCage}, que é uma das coisas mais sinistras que este mod pede.
     *
     * <p>Um anel de <b>barras de ferro</b> à volta do aldeão, de dois andares de altura: das dezesseis
     * posições, ao menos <b>quinze</b> têm de ser barras — uma fresta, e só uma. E por cima, um <b>teto
     * inteiro</b> de nove blocos que não sejam ar.
     *
     * <p>O original olha também as oito casas à volta da que o aldeão pisa, de modo que ele conta como
     * engaiolado mesmo encostado a um canto da gaiola.
     *
     * <p><i>"atraiu cinco deles a gaiolas de ferro preparadas, cobertas de madeira e com uma fresta à
     * frente"</i>. O livro descreve a gaiola antes de o jogador a ter de construir, e não diz para quê.
     */
    public static boolean engaiolado(ServerLevel level, LivingEntity quem) {
        BlockPos onde = quem.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (éGaiola(level, onde.offset(dx, 0, dz))) return true;
            }
        }
        return false;
    }

    /** Uma gaiola em volta deste bloco: quinze barras em dois andares, e um teto de nove. */
    private static boolean éGaiola(ServerLevel level, BlockPos meio) {
        int barras = 0;
        for (int andar = 0; andar <= 1; andar++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    if (level.getBlockState(meio.offset(dx, andar, dz)).is(Blocks.IRON_BARS)) barras++;
                }
            }
        }
        if (barras < BARRAS) return false;

        int teto = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!level.getBlockState(meio.offset(dx, 2, dz)).canBeReplaced()) teto++;
            }
        }
        return teto >= TETO_DA_GAIOLA;
    }

    // ------------------------------------------------------------------ o quarto, o quinto e o sexto

    /**
     * <b>A noite parada</b>: o quarto degrau, que o relógio do vampiro conta.
     *
     * <p>Trezentas voltas de quarenta batidas são <b>dez minutos</b> de noite — mais do que uma noite inteira
     * do jogo cabe —, e por isso ele não se faz numa noite só. No <b>criativo</b> bastam dez voltas, que é o
     * original tendo pena de quem está testando.
     *
     * <p>É o único degrau da escada que não pede <b>nada</b>: pede só que ele <b>esteja acordado de noite</b>,
     * e o tempo passe. É o degrau que ensina o horário.
     */
    public static void aNoite(ServerLevel level, Player quem) {
        if (Vampire.grauDe(quem) != 3) return;
        if (level.isBrightOutside()) return;

        int basta = quem.getAbilities().instabuild ? A_NOITE_NO_CRIATIVO : A_NOITE;
        if (Vampire.contaDe(quem) >= basta) {
            if (!Vampire.podeSubir(quem)) return;
            Vampire.sobeUmGrau(quem);
            Vampire.apagaAConta(quem);
            return;
        }
        Vampire.conta(quem);
    }

    /**
     * <b>Queimar-se de propósito</b>: o quinto degrau, que a Granada Solar conta.
     *
     * <p>Dez vezes com o sol engarrafado na própria cara. É o degrau mais estranho da escada e o melhor:
     * para <b>aguentar</b> o sol, ele tem de aprender a <b>levar</b> o sol — e a única maneira de o levar sem
     * morrer é em doses, de noite, de garrafa em garrafa.
     *
     * <p>E a conta não distingue de quem é a granada: queima dele, de outro, ou de um dispensador.
     */
    public static void oSolEngarrafado(Player quem) {
        if (Vampire.grauDe(quem) != 4 || !Vampire.podeSubir(quem)) return;
        if (Vampire.contaDe(quem) >= QUEIMADURAS - 1) {
            Vampire.sobeUmGrau(quem);
            Vampire.apagaAConta(quem);
            return;
        }
        Vampire.conta(quem);
    }

    /**
     * <b>Vinte Blazes</b>: o sexto degrau.
     *
     * <p><i>"extinguir criaturas de fogo puro foi a solução"</i>. É o único degrau que pede uma viagem ao
     * Nether, e o único que se parece com o que outros mods pedem — e mesmo esse pede <b>vinte</b>, que é
     * muito mais do que um recado de caçada.
     */
    public static void matou(Player quem, LivingEntity quemMorreu) {
        if (Vampire.grauDe(quem) != 5 || !Vampire.podeSubir(quem)) return;
        if (!(quemMorreu instanceof net.minecraft.world.entity.monster.Blaze)) return;

        if (Vampire.contaDe(quem) >= BLAZES - 1) {
            Vampire.sobeUmGrau(quem);
            Vampire.apagaAConta(quem);
            return;
        }
        Vampire.conta(quem);
    }

    // ------------------------------------------------------------------ e o oitavo

    /**
     * <b>Quatro aldeias</b>: o oitavo degrau, que o relógio conta e os lugares guardam.
     *
     * <p>Estar a trinta e dois blocos de uma aldeia conta <b>uma vez por aldeia</b>, e nunca mais. O pedaço
     * de mundo onde ela está fica guardado, e voltar lá não vale nada.
     *
     * <p>Ele vem logo a seguir ao voo de morcego, e isso não é coincidência: o degrau que pede que ele
     * conheça o tamanho do mundo dele chega no momento em que ele ganha asas para o percorrer.
     */
    public static void aAldeia(ServerLevel level, Player quem) {
        if (Vampire.grauDe(quem) != 7 || !Vampire.podeSubir(quem)) return;

        BlockPos aldeia = level.findNearestMapStructure(StructureTags.VILLAGE, quem.blockPosition(),
                VÊ_A_ALDEIA_A / 16 + 1, false);
        if (aldeia == null) return;
        if (aldeia.distToCenterSqr(quem.getX(), aldeia.getY(), quem.getZ())
                > (double) VÊ_A_ALDEIA_A * VÊ_A_ALDEIA_A) {
            return;
        }
        if (!guardaLugar(quem, aldeia.getX() >> 4, aldeia.getZ() >> 4)) return;

        if (Vampire.contaDe(quem) >= ALDEIAS - 1) {
            Vampire.sobeUmGrau(quem);
            Vampire.apagaAConta(quem);
            return;
        }
        Vampire.conta(quem);
        pling(level, quem);
    }

    /** Guarda o pedaço de mundo, e diz se ele é novo. */
    public static boolean guardaLugar(Player quem, int pedaçoX, int pedaçoZ) {
        long chave = (long) pedaçoX << 32 | (long) pedaçoZ & 0xFFFFFFFFL;
        List<Long> tinha = quem.getAttachedOrCreate(LUGARES);
        if (tinha.contains(chave)) return false;

        List<Long> agora = new ArrayList<>(tinha);
        agora.add(chave);
        quem.setAttached(LUGARES, List.copyOf(agora));
        return true;
    }

    /** E os lugares se esquecem quando o grau muda, como tudo o resto. */
    public static void limpa(Player quem) {
        quem.removeAttached(LUGARES);
    }

    // ------------------------------------------------------------------ e o décimo

    /**
     * <b>Fazer outro vampiro</b>: o décimo degrau, no {@code tryConvertToVampire} do Cálice.
     *
     * <p>Quatro coisas ao mesmo tempo, e nenhuma delas é um golpe:
     *
     * <ol>
     *   <li>um <b>Cálice do próprio sangue</b> dele — do nono grau em diante ele pode enchê-lo;</li>
     *   <li>uma presa <b>presa</b>, com a paralisia no <b>quinto</b> grau da poção, que é o que o prender de
     *       um vampiro do oitavo grau dá;</li>
     *   <li>a presa <b>vazia</b> — sangue a zero, o que são cinquenta goles;</li>
     *   <li>e um <b>Caixão</b> a quatro blocos.</li>
     * </ol>
     *
     * <p>É o fim da escada e ele fecha o círculo: o que o fez vampiro foi um cálice de sangue dado por
     * alguém, e o que o faz completo é <b>dar o seu</b>. O mod nunca o diz; a última página do livro mostra
     * o erudito a receber o cálice, e acaba aí.
     *
     * @return se a presa virou
     */
    public static boolean fazUmVampiro(ServerLevel level, Player quem, LivingEntity presa,
                                       boolean sangueÉDele) {
        if (!(presa instanceof net.minecraft.world.entity.Mob bicho)) return false;
        if (Vampirism.é(presa) || net.thaumcraft.occulta.wolf.Lycanthropy.éMesmoDeGente(presa)) return false;

        var preso = presa.getEffect(net.thaumcraft.occulta.OccultaEffects.PARALYSIS);
        if (preso == null || preso.getAmplifier() < VampirePowers.PRENDE_GRAU_ALTO) {
            diz(quem, "targetnottransfixed");
            return false;
        }
        if (Blood.de(presa) != 0) {
            diz(quem, "targetnotdrained");
            return false;
        }
        if (!háCaixão(level, presa)) {
            diz(quem, "nocoffinnear");
            return false;
        }

        vira(level, bicho);
        if (sangueÉDele && Vampire.grauDe(quem) == 9 && Vampire.podeSubir(quem)) {
            Vampire.sobeUmGrau(quem);
            Vampire.apagaAConta(quem);
        }
        return true;
    }

    /** Se há um Caixão a quatro blocos da presa, em qualquer direção. */
    public static boolean háCaixão(ServerLevel level, LivingEntity presa) {
        BlockPos onde = presa.blockPosition();
        for (BlockPos olha : BlockPos.betweenClosed(
                onde.offset(-O_CAIXÃO_A, -O_CAIXÃO_A, -O_CAIXÃO_A),
                onde.offset(O_CAIXÃO_A, O_CAIXÃO_A, O_CAIXÃO_A))) {
            if (level.getBlockState(olha).is(net.thaumcraft.occulta.OccultaBlocks.COFFIN)) return true;
        }
        return false;
    }

    /** E a presa deixa de ser ela: no lugar dela fica um Vampiro, com o estouro do original. */
    private static void vira(ServerLevel level, net.minecraft.world.entity.Mob presa) {
        var vampiro = net.thaumcraft.occulta.OccultaEntities.VAMPIRE.create(level,
                net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        if (vampiro == null) return;

        vampiro.snapTo(presa.getX(), presa.getY(), presa.getZ(), presa.getYRot(), presa.getXRot());
        vampiro.setPersistenceRequired();
        vampiro.finalizeSpawn(level, level.getCurrentDifficultyAt(presa.blockPosition()),
                net.minecraft.world.entity.EntitySpawnReason.CONVERSION, null);
        presa.discard();
        level.addFreshEntity(vampiro);
        level.levelEvent(null, net.minecraft.world.level.block.LevelEvent.SOUND_ZOMBIE_INFECTED,
                vampiro.blockPosition(), 0);
    }

    // ------------------------------------------------------------------ e os dois recados

    /** O toque de sino de quem acabou de dar um passo e não sabe para onde. */
    private static void pling(ServerLevel level, Player quem) {
        if (!(quem instanceof net.minecraft.server.level.ServerPlayer gente)) return;
        if (gente.connection == null) return;
        gente.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS,
                gente.getX(), gente.getY(), gente.getZ(), 1.0f, 1.0f, level.getRandom().nextLong()));
    }

    private static void diz(Player quem, String oquê) {
        quem.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.goblet." + oquê)
                .withStyle(net.minecraft.ChatFormatting.RED));
    }
}
