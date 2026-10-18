package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.curse.Curse;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Estátua da Deusa</b>: a {@code BlockStatueGoddess} do Witchery.
 *
 * <p>Uma senhora de vestido, de braços abertos, num bloco de <b>dois de altura</b>. E é a <b>única coisa
 * do mod inteiro que desfaz uma maldição sem pedir nada em troca</b>: quem a toca perde, de uma vez, as
 * <b>cinco</b> maldições do ofício e as <b>cinco</b> poções ruins do jogo.
 *
 * <h2>Ou tudo, ou nada</h2>
 *
 * <p>O original lê as cinco maldições numa condição só: <b>havendo uma</b>, ele limpa todas e ainda tira o
 * veneno, a fraqueza, a cegueira, o cansaço de minerar e a lentidão. <b>Não havendo nenhuma</b>, as poções
 * ficam — e a estátua responde com uma <b>nota de caixa</b>, que é o jeito dela de dizer que não.
 *
 * <p>Vale ler isso com atenção, porque é o avesso do que parece: quem estiver só envenenado <b>não</b> se
 * cura aqui. A estátua trata de maldições, e as poções vão de carona.
 *
 * <h2>Desvios declarados</h2>
 *
 * <p><b>A receita vem ligada.</b> No original ela mora atrás de um ajuste que vem <b>desligado</b>
 * ({@code AllowGoddessStatueRecipe}), e sem ele a estátua só existe no criativo. Este porte não tem
 * arquivo de ajustes, e uma estátua que ninguém pode fazer é uma estátua que não existe — por isso a
 * receita está aqui, com as <b>três estrelas do Nether</b> e o <b>Coração de Demônio</b> do original.
 *
 * <p><b>Ela não guarda quem a assentou.</b> No original a alma dela guarda o nome de quem a pôs, e esse
 * nome é lido num lugar só: o soco que <b>tira a coluna inteira de uma vez</b>, porque lá a estátua são
 * dois blocos empilhados. Aqui ela é <b>um bloco</b> com a caixa de dois, como as outras estátuas deste
 * porte, e quebrá-la já a tira inteira — de modo que não sobrou nada para o nome fazer.
 */
public class GoddessStatueBlock extends BaseEntityBlock {
    public static final MapCodec<GoddessStatueBlock> CODEC = simpleCodec(GoddessStatueBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Dois blocos de altura, e um dedo mais estreita à frente e atrás: a caixa do original. */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 1.6, 16.0, 32.0, 14.4);

    public GoddessStatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        return this.defaultBlockState()
                .setValue(FACING, contexto.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext contexto) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new GoddessStatueBlockEntity(onde, feitio);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde,
                                               Player quem, BlockHitResult onde2) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        cura(server, onde, quem);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack mão, BlockState feitio,
                                          Level level, BlockPos onde, Player quem,
                                          net.minecraft.world.InteractionHand qualMão,
                                          BlockHitResult onde2) {
        return this.useWithoutItem(feitio, level, onde, quem, onde2);
    }

    /**
     * <b>O toque.</b> Havendo <b>uma</b> maldição, caem as cinco e caem as cinco poções; não havendo
     * nenhuma, cai a nota de caixa e mais nada.
     *
     * @return se ela curou alguma coisa
     */
    public static boolean cura(ServerLevel level, BlockPos onde, Player quem) {
        /*
         * O original pergunta se a <b>etiqueta existe</b>, e não em que grau está. Aqui a etiqueta é o
         * grau, e grau zero é não ter — de modo que a pergunta é a mesma.
         */
        boolean alguma = false;
        for (Curse qual : Curse.values()) {
            if (Curse.level(quem, qual) > 0) alguma = true;
        }
        if (!alguma) {
            level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS,
                    1.0f, 1.0f);
            return false;
        }

        for (Curse qual : Curse.values()) {
            if (Curse.level(quem, qual) <= 0) continue;
            Curse.remove(quem, qual);
            quem.sendSystemMessage(Component.translatable("tc.occulta.goddess." + qual.getSerializedName())
                    .withStyle(net.minecraft.ChatFormatting.BLUE));
        }

        quem.removeEffect(MobEffects.POISON);
        quem.removeEffect(MobEffects.WEAKNESS);
        quem.removeEffect(MobEffects.BLINDNESS);
        quem.removeEffect(MobEffects.MINING_FATIGUE);
        quem.removeEffect(MobEffects.SLOWNESS);

        level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        net.minecraft.core.particles.ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                quem.getX(), quem.getY() + 1.0, quem.getZ(), 16, 1.0, 2.0, 1.0, 0.0);
        level.playSound(null, onde, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f);
        return true;
    }
}
