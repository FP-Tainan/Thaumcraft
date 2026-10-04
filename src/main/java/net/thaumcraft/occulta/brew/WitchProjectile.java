package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

/**
 * Os quatro efeitos que o <b>projétil de bruxa</b> deixa onde cai: o {@code EntityWitchProjectile} do
 * Witchery, na parte dele que mexe no mundo.
 *
 * <p>São quatro, e os quatro são usados por mais de uma coisa — pelas <b>Minas de Planta</b>, pelos
 * <b>cozimentos</b> atirados e pelos frascos. Ficam aqui, juntos, porque no original ficavam juntos.
 *
 * <ul>
 *   <li>a <b>teia</b>, que enche de teia a cruz à volta de onde bateu;</li>
 *   <li>a <b>tinta</b>, que cega quem está perto, com a conta da distância;</li>
 *   <li>os <b>espinhos</b>, que plantam um cato no chão;</li>
 *   <li>o <b>brotar</b>, que faz crescer um galho na direção em que o tiro ia — e <b>levanta</b> quem estava
 *       por cima.</li>
 * </ul>
 *
 * <p>Todos eles passam pelo mesmo crivo: <b>só põem bloco onde não há bloco sólido</b>. A única exceção é a
 * teia, que passa por cima de uma <b>camada de neve</b> — e é uma exceção escrita de propósito no original.
 */
public final class WitchProjectile {
    /**
     * Em que chão um cato do ofício pega.
     *
     * <p>O original pergunta pelo <b>material</b> do bloco e aceita nove deles. Os materiais sumiram do jogo,
     * e os rótulos de hoje <b>não os substituem</b>: o {@code #minecraft:dirt} da 26.2 são três blocos e nem
     * sequer inclui a grama. Por isso o rol vai escrito à mão, em
     * {@code data/thaumcraft/tags/block/cactus_ground.json}, e quem jogar pode mexer nele.
     */
    public static final TagKey<Block> CHÃO_DE_CATO =
            TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Thaumcraft.id("cactus_ground"));

    /** O raio da tinta, simples e reforçada, e quanto tempo ela cega. */
    public static final double TINTA = 4.0;
    public static final double TINTA_FORTE = 5.0;
    public static final int CEGO = 400;

    /** O quanto o galho sobe por tiro, simples e reforçado, e até onde o cato cresce. */
    public static final int GALHO = 15;
    public static final int GALHO_FORTE = 20;
    public static final int CATO = 4;

    private WitchProjectile() {
    }

    /**
     * <b>A teia.</b>
     *
     * <p>Ela enche a <b>cruz</b> à volta do lugar onde o tiro bateu: o meio, os quatro lados, acima e abaixo
     * — e, reforçada, as quatro quinas também. Onze blocos de teia, quinze reforçada.
     *
     * <p>Batendo no <b>topo</b> de um bloco, ela desce uma casa se o lugar acima dele estiver vazio: é o
     * original a evitar que a teia fique a flutuar um bloco acima do chão.
     */
    public static void teia(ServerLevel level, BlockPos onde, @Nullable Direction face, boolean forte) {
        BlockPos meio = face == null ? onde : onde.relative(face);
        if (face == Direction.UP && !level.getBlockState(onde).isSolid()) meio = meio.below();

        põeSeCouber(level, meio, Blocks.COBWEB);
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            põeSeCouber(level, meio.relative(lado), Blocks.COBWEB);
        }
        if (forte) {
            for (Direction lado : Direction.Plane.HORIZONTAL) {
                põeSeCouber(level, meio.relative(lado).relative(lado.getClockWise()), Blocks.COBWEB);
            }
        }
        põeSeCouber(level, meio.above(), Blocks.COBWEB);
        põeSeCouber(level, meio.below(), Blocks.COBWEB);
    }

    /**
     * <b>A tinta.</b>
     *
     * <p>Ela cega todo o mundo num raio de quatro blocos — cinco reforçada —, e <b>quanto mais perto, mais
     * tempo</b>: vinte segundos no meio, nada na borda. A conta é a do original, e é linear na distância.
     *
     * <p>Quem foi acertado em cheio leva os vinte segundos cheios, esteja onde estiver. E todo bicho que
     * estava a perseguir alguém <b>perde o alvo</b>, que é o que a tinta faz de melhor.
     */
    public static void tinta(ServerLevel level, double x, double y, double z, @Nullable Entity emCheio,
                             AABB daí, boolean forte) {
        double raio = forte ? TINTA_FORTE : TINTA;
        for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, daí.inflate(raio, 2.0, raio))) {
            double longe = vivo.distanceToSqr(x, y, z);
            if (longe >= raio * raio) continue;
            double quanto = vivo == emCheio ? 1.0 : 1.0 - Math.sqrt(longe) / raio;
            vivo.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (quanto * CEGO + 0.5), 0));
            if (vivo instanceof Mob bicho) bicho.setTarget(null);
        }
    }

    /**
     * <b>O cato.</b>
     *
     * <p>Ele precisa de <b>chão</b>: um dos blocos que o original aceitava pelo material deles. Não sendo
     * chão, não acontece nada — e é por isso que atirar espinhos numa parede de madeira é desperdício.
     *
     * <p>Havendo chão que não seja cato, ele <b>vira areia</b> primeiro: o original faz isso, e é o que dá ao
     * cato do ofício o seu jeito de deixar uma mancha de deserto onde passou. Havendo cato, ele <b>sobe até o
     * topo da coluna</b> e cresce dali — de modo que acertar duas vezes no mesmo lugar faz uma coluna mais
     * alta e não duas colunas.
     *
     * @return se pegou
     */
    public static boolean cato(ServerLevel level, BlockPos onde, int alto) {
        BlockPos pé = level.getBlockState(onde).isSolid() ? onde : onde.below();
        BlockState chão = level.getBlockState(pé);
        if (!chão.is(CHÃO_DE_CATO)) return false;

        if (!chão.is(Blocks.CACTUS)) {
            level.setBlock(pé, Blocks.SAND.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            while (level.getBlockState(pé).is(Blocks.CACTUS)) pé = pé.above();
            pé = pé.below();
        }

        for (int i = 1; i <= alto; i++) {
            if (!põeSeCouber(level, pé.above(i), Blocks.CACTUS)) break;
        }
        return true;
    }

    /**
     * <b>O galho.</b>
     *
     * <p>Ele cresce <b>para fora da face em que o tiro bateu</b>: acertando o <b>topo</b> do chão, sobe;
     * acertando o lado de baixo de um teto, desce; acertando uma parede, sai dela para quem atirou. Vai até
     * quinze blocos, vinte reforçado, e para no primeiro bloco sólido que encontrar.
     *
     * <p>E ele <b>larga folhas pelo caminho</b>: uma vez em quatro, uma folha num lugar sorteado ao lado.
     *
     * <p>A madeira é a <b>do lugar</b>: batendo em madeira do jogo, dá madeira do jogo; batendo em madeira do
     * ofício, dá madeira do ofício; batendo em qualquer outra coisa, <b>sorteia</b> entre as duas.
     *
     * <p>E a parte que faz dele uma arma: crescendo <b>para cima</b>, tudo o que estiver vivo até dois
     * blocos acima de onde ele bateu é <b>levantado para o topo do galho</b>. Quem atirar aos pés de alguém
     * o manda para o céu numa árvore, sem lhe tocar.
     */
    public static void galho(ServerLevel level, BlockPos onde, Direction face, int quanto, AABB daí) {
        BlockState bateu = level.getBlockState(onde);
        Block tronco = madeira(level, bateu);
        Block folha = folhaDe(tronco);

        boolean sólidoOndeBateu = bateu.isSolid();
        int i = face == Direction.UP && !sólidoOndeBateu ? 0 : 1;
        for (; i < quanto; i++) {
            BlockPos ali = onde.relative(face, i);
            if (ali.getY() >= level.getMaxY() || !põeSeCouber(level, ali, deitada(tronco, face))) break;

            int lx = face.getStepX() == 0 && level.getRandom().nextInt(4) == 0
                    ? level.getRandom().nextInt(3) - 1 : 0;
            int ly = face.getStepY() == 0 && lx == 0 && level.getRandom().nextInt(4) == 0
                    ? level.getRandom().nextInt(3) - 1 : 0;
            int lz = face.getStepZ() == 0 && lx == 0 && ly == 0 && level.getRandom().nextInt(4) == 0
                    ? level.getRandom().nextInt(3) - 1 : 0;
            if (lx != 0 || ly != 0 || lz != 0) {
                põeSeCouber(level, ali.offset(lx, ly, lz), folha);
            }
        }

        if (face != Direction.UP) return;
        BlockPos topo = onde.relative(face, i);
        for (LivingEntity vivo : level.getEntitiesOfClass(LivingEntity.class, daí.inflate(0.0, 2.0, 0.0))) {
            if (level.getBlockState(topo.above()).isSolid()) continue;
            if (level.getBlockState(topo.above(2)).isSolid()) continue;
            vivo.teleportTo(topo.getX() + 0.5, topo.getY() + 1, topo.getZ() + 0.5);
        }
    }

    /**
     * Qual madeira o galho é, pelo que ele encontrou.
     *
     * <p>A regra do original: batendo em madeira, o galho é <b>daquela madeira</b>; batendo em qualquer
     * outra coisa, ele <b>sorteia</b> entre a do jogo e a do ofício. Um galho que sai de um carvalho é de
     * carvalho; um que sai de uma pedra é do que calhar.
     *
     * <p><b>Uma volta a menos, declarada:</b> o original guarda o feitio da madeira que encontrou, de modo
     * que bater numas <b>tábuas</b> de bétula dá um galho de bétula. Aqui só o <b>tronco</b> leva o feitio
     * consigo — tábuas, folhas e mudas dão carvalho ou sorveira, conforme a família. O caminho curto custava
     * uma tabela de doze entradas que o original tinha de graça no número.
     */
    private static Block madeira(ServerLevel level, BlockState bateu) {
        if (bateu.is(BlockTags.LOGS)) return bateu.getBlock();
        if (bateu.is(BlockTags.PLANKS) || bateu.is(BlockTags.LEAVES)) {
            return doOfício(bateu) ? net.thaumcraft.occulta.OccultaBlocks.ROWAN_LOG : Blocks.OAK_LOG;
        }
        return level.getRandom().nextBoolean()
                ? Blocks.OAK_LOG : net.thaumcraft.occulta.OccultaBlocks.ROWAN_LOG;
    }

    /** Se a madeira em que ele bateu é das três do ofício. */
    private static boolean doOfício(BlockState bateu) {
        return bateu.is(net.thaumcraft.occulta.OccultaBlocks.ROWAN_PLANKS)
                || bateu.is(net.thaumcraft.occulta.OccultaBlocks.ROWAN_LEAVES)
                || bateu.is(net.thaumcraft.occulta.OccultaBlocks.ALDER_PLANKS)
                || bateu.is(net.thaumcraft.occulta.OccultaBlocks.ALDER_LEAVES)
                || bateu.is(net.thaumcraft.occulta.OccultaBlocks.HAWTHORN_PLANKS)
                || bateu.is(net.thaumcraft.occulta.OccultaBlocks.HAWTHORN_LEAVES);
    }

    /** E a folha que acompanha o tronco. */
    private static Block folhaDe(Block tronco) {
        if (tronco == net.thaumcraft.occulta.OccultaBlocks.ROWAN_LOG) {
            return net.thaumcraft.occulta.OccultaBlocks.ROWAN_LEAVES;
        }
        if (tronco == net.thaumcraft.occulta.OccultaBlocks.ALDER_LOG) {
            return net.thaumcraft.occulta.OccultaBlocks.ALDER_LEAVES;
        }
        if (tronco == net.thaumcraft.occulta.OccultaBlocks.HAWTHORN_LOG) {
            return net.thaumcraft.occulta.OccultaBlocks.HAWTHORN_LEAVES;
        }
        return Blocks.OAK_LEAVES;
    }

    /** O tronco deitado no eixo em que o galho cresce. */
    private static BlockState deitada(Block tronco, Direction eixo) {
        return tronco.defaultBlockState().trySetValue(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,
                eixo.getAxis());
    }

    /**
     * O crivo de todos eles: <b>só põe bloco onde não há bloco sólido</b>, e dá os pós do original ao pôr.
     *
     * <p>A exceção é a <b>teia por cima de neve</b>: uma camada de neve é sólida e a teia passa por cima dela
     * de todo jeito. É uma exceção escrita no original, e não se adivinha.
     */
    public static boolean põeSeCouber(ServerLevel level, BlockPos onde, Block oquê) {
        return põeSeCouber(level, onde, oquê.defaultBlockState());
    }

    public static boolean põeSeCouber(ServerLevel level, BlockPos onde, BlockState oquê) {
        BlockState tem = level.getBlockState(onde);
        if (tem.isSolid() && !(oquê.is(Blocks.COBWEB) && tem.is(Blocks.SNOW))) return false;

        level.setBlock(onde, oquê, Block.UPDATE_ALL);
        level.sendParticles(ParticleTypes.EXPLOSION, onde.getX() + 0.5, onde.getY() + 0.5,
                onde.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
        return true;
    }
}
