package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A <b>Sarça do Vazio</b>: o {@code BlockVoidBramble} do Witchery.
 *
 * <p>Ela faz as duas coisas que as outras sarças fazem metade de cada: <b>atira para longe</b> quem lhe
 * toca, como a do Fim, e <b>apaga a magia</b> à volta dela.
 *
 * <p>E é a segunda que importa. <b>Nenhum círculo acende a trinta e dois blocos de uma Sarça do Vazio.</b>
 * Não falha, não aborta, não avisa: o glifo do meio simplesmente não responde. É a única coisa neste mod
 * que <b>desliga o ofício</b>, e a única defesa possível contra um coven que já sabe o que está fazendo.
 *
 * <p>Plantá-la à volta de uma casa é dizer: aqui não se faz nada. E ela brilha de leve, para quem a plantou
 * saber onde o seu próprio silêncio começa.
 */
public class VoidBrambleBlock extends VegetationBlock {
    public static final com.mojang.serialization.MapCodec<VoidBrambleBlock> CODEC =
            simpleCodec(VoidBrambleBlock::new);

    /** Até onde o silêncio dela chega. */
    public static final double ALCANCE = 32.0;

    private static final VoxelShape FORMA = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public VoidBrambleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, net.minecraft.world.level.BlockGetter level,
                                  BlockPos onde, CollisionContext quem) {
        return FORMA;
    }

    @Override
    protected boolean mayPlaceOn(BlockState oquê, net.minecraft.world.level.BlockGetter level, BlockPos onde) {
        return !oquê.isAir();
    }

    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader level, BlockPos onde) {
        return !level.getBlockState(onde.below()).isAir();
    }

    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean dentroMesmo) {
        if (!(level instanceof ServerLevel mundo) || !(quem instanceof LivingEntity vivo)) return;
        BrambleBlock.atiraLonge(mundo, onde, vivo);
    }

    /**
     * <b>Se a magia está apagada aqui.</b>
     *
     * <p><b>Diferença declarada de feitio:</b> o original guarda uma <b>lista</b> de sarças do vazio no
     * mundo, que cada uma preenche ao carregar e esvazia ao sumir, e pergunta a essa lista. Aqui se olha a
     * bola na hora.
     *
     * <p>A razão é que a pergunta só se faz quando um <b>círculo acende</b>, que é uma coisa rara e lenta
     * por natureza — e uma lista que se mantém sozinha tem de acertar o carregar, o descarregar, o quebrar e
     * o gravar, e errando um deles fica um silêncio onde não há sarça nenhuma. Olhar na hora não erra.
     */
    public static boolean apagado(ServerLevel level, BlockPos onde) {
        int alcance = (int) ALCANCE;
        double limite = ALCANCE * ALCANCE;
        BlockPos.MutableBlockPos casa = new BlockPos.MutableBlockPos();
        for (int dx = -alcance; dx <= alcance; dx++) {
            for (int dz = -alcance; dz <= alcance; dz++) {
                for (int dy = -alcance; dy <= alcance; dy++) {
                    if (dx * dx + dy * dy + dz * dz > limite) continue;
                    casa.set(onde.getX() + dx, onde.getY() + dy, onde.getZ() + dz);
                    if (!level.isLoaded(casa)) continue;
                    if (level.getBlockState(casa).is(OccultaBlocks.VOID_BRAMBLE)) return true;
                }
            }
        }
        return false;
    }
}
