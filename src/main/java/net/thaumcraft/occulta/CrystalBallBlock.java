package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.divine.Predictions;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Bola de Cristal</b>: a {@code BlockCrystalBall} do Witchery.
 *
 * <p>Uma esfera de vidro num pé de três degraus, que <b>lê a sorte</b> de quem estiver perto. É a porta de
 * entrada de todo o ramo das {@linkplain net.thaumcraft.occulta.divine.Prediction profecias}, e é um objeto
 * muito mais estranho do que parece.
 *
 * <h2>Ela não lê a sorte de quem bate nela</h2>
 *
 * <p>Ao ser batida, ela procura <b>outro jogador</b> num retângulo de cinco por dois por cinco à volta de si,
 * e lê a sorte <b>dele</b>. Só se não houver mais ninguém ali é que lê a de quem bateu.
 *
 * <p>Quer dizer: a Bola de Cristal é feita para duas pessoas. Quem a tem em casa não lê o próprio futuro —
 * ele lê o dos outros, e são os outros que carregam a profecia. É a única coisa deste mod que <b>só serve
 * em companhia</b>.
 *
 * <h2>E ela custa poder</h2>
 *
 * <p><b>Quinhentos</b> de um altar a dezesseis blocos, por leitura. Sem altar, ou sem poder nele, ela diz que
 * não vê nada e toca um tambor. E tem uma <b>recarga de cem batidas</b> entre uma leitura e outra.
 *
 * <p>Para ler a sorte de outro é preciso ser <b>vidente</b>, e vidente só fica quem passou pelo rito que faz
 * aparecer a própria bola. No <b>criativo</b> qualquer um lê.
 */
public class CrystalBallBlock extends Block implements EntityBlock {
    public static final MapCodec<CrystalBallBlock> CODEC = simpleCodec(CrystalBallBlock::new);

    /** O que ela ocupa, nos números do original. */
    private static final VoxelShape FORMA = Block.box(4.8, 0.0, 4.8, 11.2, 9.6, 11.2);

    /** O retângulo em que ela procura a vítima: cinco para os lados, dois para cima e para baixo. */
    public static final double PERTO = 5.0;
    public static final double ACIMA = 2.0;

    public CrystalBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Ela se desenha sozinha, pelo {@link net.thaumcraft.occulta.client.CrystalBallRenderer}. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter mundo, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /** Ela pousa em chão firme, e só nele. */
    @Override
    protected boolean canSurvive(BlockState feitio, LevelReader mundo, BlockPos onde) {
        BlockPos chão = onde.below();
        return mundo.getBlockState(chão).isFaceSturdy(mundo, chão, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState feitio, LevelReader mundo, ScheduledTickAccess relógio,
                                     BlockPos onde, Direction lado, BlockPos vizinho, BlockState doVizinho,
                                     RandomSource sorte) {
        if (lado == Direction.DOWN && !feitio.canSurvive(mundo, onde)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(feitio, mundo, relógio, onde, lado, vizinho, doVizinho, sorte);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new CrystalBallBlockEntity(onde, feitio);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level mundo, BlockPos onde, Player quem,
                                               BlockHitResult bateu) {
        if (!(mundo instanceof ServerLevel level) || !(quem instanceof ServerPlayer vidente)) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(onde) instanceof CrystalBallBlockEntity bola)) {
            return InteractionResult.SUCCESS;
        }

        if (!bola.podeSerUsada(level)) {
            recusa(level, vidente, onde, "recharging");
            return InteractionResult.SUCCESS;
        }
        if (!paga(level, vidente, onde)) return InteractionResult.SUCCESS;

        Predictions.lê(level, vítima(level, onde, vidente), vidente, true);
        bola.usada(level);
        level.sendParticles(ParticleTypes.EXPLOSION, onde.getX() + 0.5, onde.getY() + 0.2,
                onde.getZ() + 0.5, 16, 0.2, 0.2, 0.2, 0.0);
        level.playSound(null, onde, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>Quem vai levar a profecia.</b>
     *
     * <p>O jogador mais perto que não seja quem bateu — e, se não houver nenhum, quem bateu.
     *
     * <p><b>Há um engano no original e está portado:</b> ao medir qual é o mais perto, ele mede sempre a
     * distância de <b>quem bateu</b> à bola, e não a do jogador que está olhando. A conta dá o mesmo número
     * em todas as voltas, de modo que o que ele realmente escolhe é o <b>último da lista</b>, e não o mais
     * perto. Com um só a assistir — que é o caso de quase sempre — não faz diferença nenhuma.
     */
    private static ServerPlayer vítima(ServerLevel level, BlockPos onde, ServerPlayer quemBateu) {
        AABB ali = new AABB(onde.getX() + 0.5 - PERTO, onde.getY() + 0.5 - ACIMA,
                onde.getZ() + 0.5 - PERTO, onde.getX() + 0.5 + PERTO, onde.getY() + 0.5 + ACIMA,
                onde.getZ() + 0.5 + PERTO);
        ServerPlayer escolhido = quemBateu;
        for (Player cada : level.getEntitiesOfClass(Player.class, ali)) {
            if (cada != quemBateu && cada instanceof ServerPlayer outro) escolhido = outro;
        }
        return escolhido;
    }

    /** Tira os quinhentos de um altar perto. Devolve se conseguiu. */
    private static boolean paga(ServerLevel level, ServerPlayer quem, BlockPos onde) {
        AltarBlockEntity altar = PowerSources.closest(level, onde);
        if (altar != null && altar.consume(Predictions.CUSTO)) return true;
        recusa(level, quem, onde, "nopower");
        return false;
    }

    private static void recusa(ServerLevel level, ServerPlayer quem, BlockPos onde, String porquê) {
        quem.sendSystemMessage(Component.translatable("tc.occulta.prediction." + porquê)
                .withStyle(ChatFormatting.GRAY));
        level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
