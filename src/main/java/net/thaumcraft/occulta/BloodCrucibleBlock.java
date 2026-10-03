package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampirePowers;
import net.thaumcraft.occulta.vampire.VampirePowers.Supremo;
import org.jetbrains.annotations.Nullable;

/**
 * O Crisol de Sangue: a {@code BlockBloodCrucible} do Witchery.
 *
 * <p>Uma bacia de pedra baixa onde o <b>vampiro</b> despeja o que bebeu. Cheia — vinte de sangue, quatro goles —
 * e sendo ele de <b>décimo grau</b>, ela lhe abre a escolha do dom maior: a <b>Tempestade</b>, com uma
 * alcachofra-d'água na mão; o <b>Enxame</b>, com lã de morcego; ou o <b>Caminho de Casa</b>, com um osso. Escolhido, o
 * crisol se esvazia.
 *
 * <p>E o dom <b>vem com cinco usos</b>, nada mais: gastos os cinco, é preciso enchê-lo outra vez e escolher
 * outra vez. Escolher um dom diferente não acumula — ele <b>troca</b> o que havia, e os usos que sobravam do
 * anterior somem com ele. Um vampiro de décimo grau tem um Supremo, e só um.
 *
 * <p>Quem clicar nele não sendo vampiro de décimo grau, ou com o crisol vazio, ou sem nada que sirva na mão,
 * ouve um <b>toque de caixa</b> e não recebe explicação nenhuma. É o original inteiro: ele nunca diz o que
 * falta.
 */
public class BloodCrucibleBlock extends BaseEntityBlock {
    public static final MapCodec<BloodCrucibleBlock> CODEC = simpleCodec(BloodCrucibleBlock::new);

    /** O grau de vampiro que o crisol pede: o décimo, como no original. */
    public static final int VAMPIRE_LEVEL = 10;

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 5.0, 12.0);

    public BloodCrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodCrucibleBlockEntity(pos, state);
    }

    /** O grau de vampiro de quem clicou. */
    public static int level(Player quem) {
        return Vampire.grauDe(quem);
    }

    /**
     * Qual dom cada coisa na mão escolhe, ou nada.
     *
     * <p>As três coisas <b>dizem</b> o que dão, e é o melhor desenho calado do mod: a
     * <b>alcachofra-d'água</b>, que é planta de lago, chama a <b>tempestade</b>; a <b>lã de morcego</b> chama o
     * <b>enxame</b> de morcegos; e o <b>osso</b> — o que resta de um morto — chama o <b>caminho de casa</b>.
     * Ninguém precisa de ler isso em lugar nenhum.
     */
    public static @Nullable Supremo gift(ItemStack naMão) {
        if (naMão.is(OccultaItems.WATER_ARTICHOKE_GLOBE)) return Supremo.TEMPESTADE;
        if (naMão.is(OccultaItems.BAT_WOOL)) return Supremo.ENXAME;
        if (naMão.is(Items.BONE)) return Supremo.CASA;
        return null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState state, Level level, BlockPos pos,
                                          Player quem, InteractionHand mão, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!(server.getBlockEntity(pos) instanceof BloodCrucibleBlockEntity crisol)) return InteractionResult.PASS;

        boolean pode = level(quem) >= VAMPIRE_LEVEL && (crisol.full() || quem.hasInfiniteMaterials());
        Supremo dom = pode ? gift(naMão) : null;
        if (dom == null) {
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.0);
            server.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        VampirePowers.dáOSupremo(quem, dom);
        crisol.drain();
        if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
        server.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.3, 0.3, 0.3, 0.0);
        server.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}
