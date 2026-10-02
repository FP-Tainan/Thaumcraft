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
import org.jetbrains.annotations.Nullable;

/**
 * O Crisol de Sangue: a {@code BlockBloodCrucible} do Witchery.
 *
 * <p>Uma bacia de pedra baixa onde o <b>vampiro</b> despeja o que bebeu. Cheia — vinte de sangue, quatro goles —
 * e sendo ele de <b>décimo grau</b>, ela lhe abre a escolha do dom maior: a <b>Tempestade</b>, com uma
 * alcachofra-d'água na mão; o <b>Enxame</b>, com lã de morcego; ou a <b>Colheita</b>, com um osso. Escolhido, o
 * crisol se esvazia.
 *
 * <p><b>Declarado, e é o principal:</b> este bloco <b>não faz nada ainda</b>. Ele está de pé, guarda o sangue,
 * mostra-o e sabe a conta dos três dons — mas o que o enche é o vampiro a alimentar-se, e o que ele destrava é o
 * décimo grau do vampiro, e <b>o vampiro não está portado</b>. Ele vai no jar porque é peça do ofício e porque,
 * quando a fatia do vampiro chegar, só é preciso ligar duas linhas: o {@code feed} ao gole e o {@link #level}
 * abaixo à conta do grau. Até lá, quem clicar nele ouve o mesmo "não" que o original dá a quem não é vampiro.
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

    /**
     * O grau de vampiro de quem clicou.
     *
     * <p>Enquanto o vampiro não estiver portado isto é sempre <b>zero</b>, e por isso o crisol recusa sempre. É
     * o único fio que a fatia do vampiro tem de ligar aqui.
     */
    public static int level(Player quem) {
        return 0;
    }

    /** Qual dom cada coisa na mão escolhe, ou nada. */
    public static @Nullable String gift(ItemStack naMão) {
        if (naMão.is(OccultaItems.WATER_ARTICHOKE_GLOBE)) return "storm";
        if (naMão.is(OccultaItems.BAT_WOOL)) return "swarm";
        if (naMão.is(Items.BONE)) return "farm";
        return null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack naMão, BlockState state, Level level, BlockPos pos,
                                          Player quem, InteractionHand mão, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!(server.getBlockEntity(pos) instanceof BloodCrucibleBlockEntity crisol)) return InteractionResult.PASS;

        boolean pode = level(quem) >= VAMPIRE_LEVEL && (crisol.full() || quem.hasInfiniteMaterials());
        String dom = pode ? gift(naMão) : null;
        if (dom == null) {
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.0);
            server.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        crisol.drain();
        if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
        server.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.3, 0.3, 0.3, 0.0);
        server.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}
