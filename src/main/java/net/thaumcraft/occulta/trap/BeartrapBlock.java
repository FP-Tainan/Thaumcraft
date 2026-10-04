package net.thaumcraft.occulta.trap;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.wolf.Lycanthropy;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>Armadilha de Urso</b> e a <b>Armadilha de Lobo</b>: o {@code BlockBeartrap} do Witchery, que é um
 * bloco só com uma chave virada.
 *
 * <p>Ela é rasa — um pedaço de pixel de alto — e <b>não estorva a passagem</b>: ninguém tropeça nela, ninguém
 * a vê de longe, e pisar nela é a coisa mais natural do mundo. Quem pisa leva <b>quatro de dano de bigorna</b>
 * e fica <b>trinta segundos preso</b> no lugar, com a paralisia no terceiro grau.
 *
 * <p>Ela <b>nasce disparada</b>. Quem a põe no chão tem de a armar com um clique, e um clique volta a
 * desarmá-la — que é o jeito do original de dizer que uma armadilha não se arma sozinha. Armada, ela leva
 * <b>vinte batidas</b> para ficar sensível: o tempo de quem a armou sair de cima dela.
 *
 * <p>E ela <b>se esconde de quem não a pôs</b>. Veja {@link BeartrapBlockEntity#àVistaDe}.
 *
 * <p>A <b>de prata</b> é outra coisa inteiramente, e a diferença é uma chave: ela <b>só apanha lobisomens</b>,
 * e dentre eles só <b>o que ela mesma chamou</b>. Em troca, ela <b>não volta para a mão</b> — quebrá-la não
 * devolve nada, porque a prata se gastou no que ela fez. Veja {@link BeartrapBlockEntity}.
 */
public class BeartrapBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BeartrapBlock> CODEC = simpleCodec(BeartrapBlock::new);

    /**
     * A forma dela, que é a conta do original: de três a treze em largura e <b>pouco mais de um pixel</b> de
     * alto. É pequena de propósito — ninguém precisa pisar no meio dela para a disparar, mas também não se vê
     * um relevo no chão.
     */
    private static final VoxelShape FORMA = Block.box(3.2, 0.16, 3.2, 12.8, 1.6, 12.8);

    /** O que ela faz a quem pisa: o dano de bigorna e a paralisia de trinta segundos no terceiro grau. */
    public static final float DANO = 4.0f;
    public static final int PRESO = 600;
    public static final int GRAU = 2;

    /** Os pós do estalo, e o quanto eles se espalham. */
    public static final int PÓS = 20;
    public static final double ESPALHA = 0.25;
    public static final double SOBE = 0.5;

    /** A meia volume, que é como o original toca tudo o que toca assim. */
    public static final float MEIO = 0.5f;

    /** E o tom dele: entre um terço e meio. */
    public static float tom(Level level) {
        return 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f);
    }

    public BeartrapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Ela olha para <b>quem a pôs</b>: o original guarda o lado oposto ao que a pessoa estava olhando.
     *
     * <p>Faz sentido para o desenho — os dois arcos dela são iguais, mas a placa e as molas não são, e ela
     * fica de frente para quem se abaixou para a pôr.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext onde) {
        return this.defaultBlockState().setValue(FACING, onde.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext quem) {
        return FORMA;
    }

    /** Nada de tropeçar nela: o original devolve caixa de colisão nenhuma. */
    @Override
    protected VoxelShape getCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                           CollisionContext quem) {
        return Shapes.empty();
    }

    /**
     * E o pé só a encontra <b>dentro da forma dela</b>.
     *
     * <p>No original isto é uma conta à mão: ele monta a caixa da armadilha e pergunta se ela cruza a caixa
     * de quem passou. Aqui é o jogo que pergunta, e o que se lhe dá é a mesma caixa — e não a de colisão, que
     * é vazia de propósito.
     */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                                       Entity quem) {
        return FORMA;
    }

    /** Quem a desenha é a alma dela, com vinte caixas e dois arcos que se levantam. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new BeartrapBlockEntity(onde, feitio);
    }

    /**
     * <b>Quem a põe fica dono dela</b>, e é isso que a esconde dos outros.
     *
     * <p>No original ela também <b>já vem disparada</b> daqui, que é o mesmo que a alma dela faz ao nascer.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos onde, BlockState feitio, @Nullable LivingEntity quem,
                            ItemStack oquê) {
        super.setPlacedBy(level, onde, feitio, quem, oquê);
        if (level.isClientSide() || !(quem instanceof Player gente)) return;
        if (!(level.getBlockEntity(onde) instanceof BeartrapBlockEntity armadilha)) return;
        armadilha.dono(gente);
        armadilha.dispara();
    }

    /**
     * Um clique <b>arma ou desarma</b>, com o estalo seco do original.
     *
     * <p>É também como se desarma uma que já disparou: ela não se recarrega sozinha, e quem a quiser armada
     * outra vez tem de se abaixar e armá-la — pisando nela mesma, se não tiver cuidado.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState feitio, Level level, BlockPos onde, Player quem,
                                               BlockHitResult acertou) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(onde) instanceof BeartrapBlockEntity armadilha)) {
            return InteractionResult.PASS;
        }
        armadilha.vira(level);
        /*
         * O clique vai <b>a quem clicou</b>, e vai baixo: meia volume e o tom entre um terço e meio, que é a
         * conta de tom do original para tudo o que ele toca assim. Quem está ao lado quase não ouve.
         */
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), OccultaSounds.CLICK.value(),
                SoundSource.BLOCKS, MEIO, tom(level));
        return InteractionResult.SUCCESS;
    }

    /**
     * E o pé que a encontra.
     *
     * <p>A <b>de ferro</b> apanha qualquer coisa viva. A <b>de prata</b> pede duas coisas: que seja um
     * <b>lobisomem</b> e que seja <b>o dela</b>.
     *
     * <p>Quem está no <b>criativo</b> leva o dano e não leva a paralisia — é o que o original faz, e é a única
     * misericórdia que ela tem.
     */
    @Override
    protected void entityInside(BlockState feitio, Level level, BlockPos onde, Entity quem,
                                InsideBlockEffectApplier efeitos, boolean emCima) {
        if (!(level instanceof ServerLevel mundo) || !(quem instanceof LivingEntity vivo)) return;
        if (!(level.getBlockEntity(onde) instanceof BeartrapBlockEntity armadilha)) return;
        if (!armadilha.sensível(level)) return;

        if (armadilha.dePrata()) {
            if (!Lycanthropy.é(vivo) || !armadilha.apanhaOLobo(vivo)) return;
        }

        boolean criativo = quem instanceof Player gente && gente.getAbilities().instabuild;
        if (!criativo) {
            vivo.addEffect(new MobEffectInstance(OccultaEffects.PARALYSIS, PRESO, GRAU, true, true));
        }
        vivo.hurtServer(mundo, mundo.damageSources().anvil(null), DANO);

        /*
         * Os pós e o estalo, pelas contas do original: <b>vinte</b> pós de redstone, espalhados um quarto de
         * bloco para cada lado e meio bloco para cima, e o estalo a meia volume com o tom baixo.
         *
         * <p>Os vinte saem de uma conta curiosa que vale copiar: o original faz
         * {@code min(ceil(max(largura, 1) * 20), 300)}, de modo que toda largura abaixo de um bloco dá
         * exatamente vinte. A armadilha pede um quarto, e leva vinte.
         */
        for (int volta = 0; volta < PÓS; volta++) {
            mundo.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                    onde.getX() + 0.5 + mundo.getRandom().nextDouble() * ESPALHA * 2.0 - ESPALHA,
                    onde.getY() + 0.5 + mundo.getRandom().nextDouble() * SOBE,
                    onde.getZ() + 0.5 + mundo.getRandom().nextDouble() * ESPALHA * 2.0 - ESPALHA,
                    1, 0.02, 0.02, 0.02, 0.0);
        }
        mundo.playSound(null, onde, OccultaSounds.MANTRAP.value(), SoundSource.BLOCKS, MEIO, tom(mundo));
        armadilha.dispara();
    }

    /**
     * O relógio só anda na <b>de prata</b>: é ela que chama o lobisomem, e o original devolve
     * {@code canUpdate() == silvered} justamente para a de ferro não custar nada.
     */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState feitio,
                                                                            BlockEntityType<T> tipo) {
        if (level.isClientSide() || !feitio.is(OccultaBlocks.WOLFTRAP)) return null;
        return (mundo, onde, qual, alma) -> {
            if (alma instanceof BeartrapBlockEntity armadilha && mundo instanceof ServerLevel servidor) {
                armadilha.bate(servidor);
            }
        };
    }
}
