package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * O <b>Feixe de Vime</b>: o {@code BlockWickerBundle} do Witchery.
 *
 * <p>Por fora, um fardo de galhos amarrados — um tronco com casca de vime. Por dentro, a peça de que se
 * constrói o <b>Homem de Vime</b>: uma figura de <b>oito blocos de altura</b> que, acesa com isqueiro,
 * <b>arde</b> e de dentro dela sai o <b>Caçador Cornudo</b>.
 *
 * <p>São <b>dois</b>: o <b>simples</b>, que é nove mudas atadas, e o <b>ensanguentado</b>, que é o simples
 * passado por <b>Sangue Infernal</b>. E só o ensanguentado serve para a figura — o simples é madeira, o
 * ensanguentado é oferenda.
 *
 * <p>É o segundo caminho para o Caçador, e o mais antigo: o <b>Chifre da Caça</b> chama-o de qualquer lugar,
 * mas o Homem de Vime pede que alguém o <b>construa</b>, o encha de sangue e lhe ponha fogo. O primeiro é um
 * pedido; o segundo é um sacrifício.
 */
public class WickerBundleBlock extends RotatedPillarBlock {
    public static final MapCodec<WickerBundleBlock> CODEC = simpleCodec(WickerBundleBlock::new);

    /** Se o feixe foi passado por Sangue Infernal. */
    public static final BooleanProperty SANGUE = BooleanProperty.create("bloodied");

    /**
     * A figura, coluna por coluna.
     *
     * <p>A primeira conta é o <b>desvio ao longo</b> do eixo em que ela foi construída; a segunda, a
     * <b>altura</b> a contar dos pés. Assim como no original, a figura é de duas colunas de largura, com os
     * braços abertos no meio.
     */
    private static final int[][] FIGURA = {
        {0, 6}, {1, 6},
        {0, 5}, {1, 5},
        {-1, 4}, {0, 4}, {1, 4}, {2, 4},
        {-1, 3}, {0, 3}, {1, 3}, {2, 3},
        {-1, 2}, {0, 2}, {1, 2}, {2, 2},
        {0, 1},
        {0, 0}, {1, 0},
    };

    /*
     * E falta um: o <b>(+1, +1)</b>, que o molde do original <b>não pergunta</b>. Ele checa ali o bloco de
     * baixo outra vez — é um descuido de quem o escreveu, e o jogo de 2014 aceita desde então uma figura com
     * aquele lugar cheio ou vazio. Fica assim.
     */

    /** E o que <b>não</b> pode haver, que é o que lhe dá contorno. */
    private static final int[][] VAZIO = {
        {0, 7}, {1, 7},
        {-1, 6}, {2, 6},
        {-1, 5}, {2, 5},
        {-2, 4}, {3, 4},
        {-2, 3}, {3, 3},
        {-2, 2}, {3, 2},
        {-1, 1}, {2, 1},
        {-1, 0}, {2, 0},
    };

    /** Onde o fogo pega. */
    private static final int[][] FOGO = {
        {0, 6}, {1, 6},
        {0, 3}, {1, 3},
        {0, 2}, {1, 2},
        {0, 1}, {1, 1},
        {0, 0}, {1, 0},
        {-1, 4}, {2, 4},
    };

    /** Quantos pós a chegada dele levanta. */
    public static final int PÓS = 120;

    public WickerBundleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y).setValue(SANGUE, false));
    }

    @Override
    protected MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        super.createBlockStateDefinition(construtor);
        construtor.add(SANGUE);
    }

    /** E o clique do meio traz o feixe que está ali, com ou sem sangue. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos onde, BlockState feitio,
                                          boolean comAlma) {
        ItemStack qual = super.getCloneItemStack(level, onde, feitio, comAlma);
        if (feitio.getValue(SANGUE)) {
            qual.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                    net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                            .with(SANGUE, true));
        }
        return qual;
    }

    /**
     * <b>O isqueiro no feixe</b>, e nada mais.
     *
     * <p>Com qualquer outra coisa na mão não acontece nada — nem sequer o que o isqueiro costuma fazer, que
     * é pôr fogo no bloco do lado. O original devolve que o clique foi tratado, e com isso o isqueiro nem se
     * gasta.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState feitio, Level level, BlockPos onde,
                                          Player quem, net.minecraft.world.InteractionHand mão,
                                          BlockHitResult acertou) {
        if (!naMão.is(Items.FLINT_AND_STEEL)) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;
        acende(mundo, onde, quem.getYRot());
        return InteractionResult.SUCCESS;
    }

    /**
     * Procura a figura a partir do feixe em que se bateu e, achando-a, põe-lhe fogo.
     *
     * <p>A conta do original é curiosa e vale copiá-la: ele primeiro descobre <b>em que eixo</b> a figura foi
     * construída, olhando os quatro vizinhos; depois <b>desce</b> até aos pés e <b>anda para trás</b> até à
     * coluna mais baixa do eixo. Só então compara o que está ali com o molde.
     *
     * <p>E o andar usa <b>qualquer</b> feixe, enquanto o molde só aceita os <b>ensanguentados</b>: quem
     * construir a figura certa com um feixe simples no meio dela não a acende, mas o caminho até aos pés
     * ainda o atravessa.
     *
     * @return se havia uma figura ali
     */
    public static boolean acende(ServerLevel level, BlockPos daqui, float rumo) {
        boolean oeste = éFeixe(level, daqui.west());
        boolean leste = éFeixe(level, daqui.east());
        boolean norte = éFeixe(level, daqui.north());
        boolean sul = éFeixe(level, daqui.south());
        boolean emX = oeste || leste;
        boolean emZ = norte || sul;
        if (emX == emZ) return false;

        BlockPos pés = daqui;
        Direction aoLongo;
        if (!emX) {
            pés = norte && !sul ? pés.north() : pés.south();
            aoLongo = Direction.SOUTH;
        } else {
            pés = oeste && !leste ? pés.west() : pés.east();
            aoLongo = Direction.EAST;
        }
        while (éFeixe(level, pés.below())) pés = pés.below();
        while (éFeixe(level, pés.relative(aoLongo.getOpposite()))) {
            pés = pés.relative(aoLongo.getOpposite());
        }

        if (!éAFigura(level, pés, aoLongo)) return true;

        for (int[] onde : FOGO) {
            level.setBlock(ali(pés, aoLongo, onde), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        }
        chama(level, pés, aoLongo, rumo);
        return true;
    }

    /** Se o que está ali é o molde: os dezenove cheios e os dezesseis vazios. */
    public static boolean éAFigura(LevelReader level, BlockPos pés, Direction aoLongo) {
        for (int[] onde : FIGURA) {
            if (!ensanguentado(level, ali(pés, aoLongo, onde))) return false;
        }
        for (int[] onde : VAZIO) {
            if (ensanguentado(level, ali(pés, aoLongo, onde))) return false;
        }
        return true;
    }

    /** Põe o Caçador de pé no meio dela, com os pós do original. */
    private static void chama(ServerLevel level, BlockPos pés, Direction aoLongo, float rumo) {
        var bicho = OccultaEntities.HORNED_HUNTSMAN.create(level, EntitySpawnReason.TRIGGERED);
        if (bicho == null) return;

        double meio = 0.5;
        double x = pés.getX() + (aoLongo == Direction.EAST ? 1.0 : meio);
        double z = pés.getZ() + (aoLongo == Direction.SOUTH ? 1.0 : meio);
        bicho.snapTo(x, pés.getY() + 0.1, z, 180.0f + rumo, 0.0f);
        bicho.setYHeadRot(bicho.getYRot());
        bicho.setYBodyRot(bicho.getYRot());
        bicho.setPersistenceRequired();
        bicho.entradaEstoura();
        level.addFreshEntity(bicho);

        for (int volta = 0; volta < PÓS; volta++) {
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    pés.getX() + level.getRandom().nextDouble(),
                    pés.getY() - 2 + level.getRandom().nextDouble() * 3.9,
                    pés.getZ() + 1 + level.getRandom().nextDouble(),
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** O lugar que um par do molde aponta, a contar dos pés. */
    private static BlockPos ali(BlockPos pés, Direction aoLongo, int[] onde) {
        return pés.relative(aoLongo, onde[0]).above(onde[1]);
    }

    /** Se ali há um feixe, de qualquer feitio. */
    public static boolean éFeixe(LevelReader level, BlockPos onde) {
        return level.getBlockState(onde).is(OccultaBlocks.WICKER_BUNDLE);
    }

    /** E se ali há um feixe <b>ensanguentado</b>, que é o único que o molde aceita. */
    public static boolean ensanguentado(LevelReader level, BlockPos onde) {
        BlockState feitio = level.getBlockState(onde);
        return feitio.is(OccultaBlocks.WICKER_BUNDLE) && feitio.getValue(SANGUE);
    }
}
