package net.thaumcraft.occulta.brew;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>poça de cozimento</b>: o {@code BlockBrewLiquidEffect} do Witchery.
 *
 * <p>É o que um frasco de <b>losna</b> deixa onde bateu. Ao contrário da {@linkplain BrewGasBlock nuvem},
 * que sobe e se abre em bola, a poça faz o que líquido faz: <b>escorre</b>. Procura o buraco mais perto,
 * desce por ele, e no chão plano se abre em todas as direções ao mesmo tempo — gastando uma casa de altura
 * a cada passo, até não ter mais o que gastar.
 *
 * <h2>As seis casas, e as oito do desenho</h2>
 *
 * <p>Uma poça inteira vale <b>seis</b> casas de altura ({@code quantaPerBlock}), e a nascente é a única que
 * as tem todas. Cada passo para o lado tira uma, e por isso ela nunca vai mais longe que <b>cinco casas</b>
 * da nascente: na sexta não sobra altura para dar.
 *
 * <p>E ao desenhar o original divide por <b>oito</b> ({@code quantaPerBlockFloat}), não por seis. Não é
 * descuido do porte: é dele, e o efeito é que a poça <b>nunca enche o bloco</b> — a mais cheia chega a
 * catorze pixels, e a mais fina a quatro. Fica assim.
 *
 * <h2>Lá ela é um fluido do Forge, aqui não</h2>
 *
 * <p>Lá ela é um {@code IFluidBlock} registrado como fluido, e dele tira a densidade (mil, logo <b>desce</b>),
 * a temperatura, a luz (nenhuma) e o compasso — a viscosidade dividida por duzentos, que dá uma batida a
 * cada <b>cinco</b>. Aqui não há fluido nenhum registrado: é um bloco com um nível, e os quatro números estão
 * escritos à mão, com os valores que o Forge lhe dava.
 *
 * <p><b>Declarado:</b> o original também <b>empurra</b> quem está dentro dela, no sentido do escorrimento,
 * pelo gancho de fluido do Forge ({@code velocityToAddToEntity}). Esse gancho não tem par no jogo de hoje
 * sem registrar um fluido de verdade, e um empurrão mal medido atira gente pelo mapa. A poça vive poucos
 * segundos e o empurrão dela é quase nada: ficou de fora, e está dito.
 *
 * <p><b>Declarado:</b> o original guarda as contas do escorrimento em dois vetores <b>do próprio bloco</b> —
 * um bloco é um só no jogo todo, e duas poças correndo ao mesmo tempo escrevem uma na conta da outra. Aqui
 * as contas são locais de cada batida. É uma correção, não um desvio de aparência.
 *
 * <p><b>Declarado:</b> a chuva de gotas que o rito de círculo faz cair ({@code onUpdateRitual}, com o
 * {@code EntityDroplet}) não é desta fatia — nem a do gás, que tem a mesma dívida.
 */
public class BrewLiquidBlock extends BaseEntityBlock {
    public static final MapCodec<BrewLiquidBlock> CODEC = simpleCodec(BrewLiquidBlock::new);

    /** Quantas casas de altura uma poça inteira vale: o {@code quantaPerBlock} do original. */
    public static final int QUANTA = 6;

    /** E por quantas ele divide ao desenhar, que são <b>oito</b>. Vem dele; ver o cabeçalho. */
    public static final float QUANTA_DRAWN = 8.0f;

    /** De quantas em quantas batidas ela corre: a viscosidade do fluido dividida por duzentos. */
    public static final int TICK = 5;

    /** A densidade dela, que sendo maior que zero manda a poça para <b>baixo</b>. */
    public static final int DENSITY = 1000;

    /** O nível da casa, que no original é o metadata: zero é a nascente, cinco é o fio mais fino. */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, QUANTA - 1);

    /** Uma vez em cem, por batida e por lado, ela encharca o que está ao lado. */
    public static final int BLOCK_CHANCE = 100;

    /** E uma vez em dez quem está dentro apanha — fraco, como na nuvem. */
    public static final int TOUCH_CHANCE = 10;
    public static final double TOUCH_POWER = 0.25;
    public static final double TOUCH_DURATION = 0.5;

    /** As quatro direções do escorrimento, na ordem em que o original as conta. */
    private static final Direction[] WAY = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

    public BrewLiquidBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewFluidBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Não se esbarra nela: o original devolve caixa nenhuma. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }

    /**
     * Mas <b>clica-se através dela</b> quando ela é só fio: o {@code canStopRayTrace} do original só a deixa
     * apanhar o olhar quando é nascente.
     */
    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LEVEL) == 0 ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        level.scheduleTick(pos, this, TICK);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block qual,
                                   @Nullable Orientation lado, boolean mexeu) {
        level.scheduleTick(pos, this, TICK);
    }

    /**
     * O {@code updateTick}, na ordem dele: a nascente seca, a casa se mede pelas vizinhas, a poça desce, se
     * não puder descer se abre, e por fim encharca o que está em volta.
     */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource sorte) {
        BrewFluidBlockEntity poça = level.getBlockEntity(pos) instanceof BrewFluidBlockEntity achou
                ? achou : null;

        // 1. a nascente seca: as três primeiras batidas são de graça, e daí em diante é uma em tantas
        if (poça != null && this.isSource(level, pos)) {
            if (poça.tickUpdate() <= 3 || (poça.duration() != 0 && sorte.nextInt(poça.duration()) != 0)) {
                level.scheduleTick(pos, this, TICK);
            } else {
                level.removeBlock(pos, false);
                return;
            }
        }

        // 2. quanto há aqui, e quanto devia haver pelo que as vizinhas têm
        int quanta = QUANTA - state.getValue(LEVEL);
        if (quanta < QUANTA) {
            BlockPos acima = pos.above();
            boolean vemDeCima = level.getBlockState(acima).is(this)
                    || level.getBlockState(acima.west()).is(this)
                    || level.getBlockState(acima.east()).is(this)
                    || level.getBlockState(acima.north()).is(this)
                    || level.getBlockState(acima.south()).is(this);

            int esperado;
            if (vemDeCima) {
                esperado = QUANTA - 1;
            } else {
                int maior = -100;
                for (Direction lado : WAY) maior = this.larger(level, pos.relative(lado), maior);
                esperado = maior - 1;
            }

            if (esperado != quanta) {
                quanta = esperado;
                if (esperado <= 0) {
                    /*
                     * Secou — e o original <b>não para aqui</b>: a casa some e ele segue para o escorrimento
                     * como se nada fosse, com a poça que já não existe. Fica como está, que é o que ele faz.
                     */
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(pos, state.setValue(LEVEL, QUANTA - esperado), Block.UPDATE_ALL);
                    level.scheduleTick(pos, this, TICK);
                    level.updateNeighborsAt(pos, this, null);
                }
            }
        }

        // 3. ela desce, se houver por onde
        BlockPos abaixo = pos.below();
        if (this.canDisplace(level, abaixo)) {
            this.flowInto(level, abaixo, 1, poça);
            return;
        }

        // 4. e se não houver, ela se abre pelos lados que levam mais perto de um buraco
        int fio = QUANTA - quanta + 1;
        if (fio >= QUANTA) return;

        if (this.isSource(level, pos) || !this.flowsVertically(level, pos)) {
            if (level.getBlockState(pos.above()).is(this)) fio = 1;
            boolean[] para = this.bestWays(level, pos);
            for (int i = 0; i < WAY.length; i++) {
                if (para[i]) this.flowInto(level, pos.relative(WAY[i]), fio, poça);
            }
        }

        // 5. e o que ela toca, ela encharca — de vez em quando, e sempre um grau mais fraco
        if (poça != null && !poça.contents().isEmpty()) {
            for (Direction lado : Direction.values()) {
                if (sorte.nextDouble() >= 1.0 / BLOCK_CHANCE) continue;
                BlockPos vizinho = pos.relative(lado);
                if (!this.isTarget(level, vizinho)) continue;
                BrewModifiers temperos = new BrewModifiers();
                temperos.strengthPenalty++;
                temperos.quemFez = poça.quemFez(level);
                Brew.applyToBlock(level, poça.contents(), vizinho, lado.getOpposite(), 1, temperos);
            }
            level.scheduleTick(pos, this, TICK);
        }
    }

    /** O {@code onEntityCollidedWithBlock}: quem passa dentro apanha, de vez em quando e fraco. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity quem,
                                InsideBlockEffectApplier applier, boolean dentroMesmo) {
        if (!(level instanceof ServerLevel server) || !(quem instanceof LivingEntity vivo)) return;
        if (server.getRandom().nextInt(TOUCH_CHANCE) != 4) return;
        if (!(level.getBlockEntity(pos) instanceof BrewFluidBlockEntity poça)) return;
        if (poça.contents().isEmpty()) return;
        BrewModifiers temperos = new BrewModifiers(TOUCH_POWER, TOUCH_DURATION);
        temperos.quemFez = poça.quemFez(server);
        temperos.protectedFromBadEffects = vivo.hasEffect(net.thaumcraft.occulta.OccultaEffects.GAS_MASK);
        Brew.apply(level, vivo, poça.contents(), temperos);
    }

    // ------------------------------------------------------------------ as contas do escorrimento

    /** Quanto de altura há numa casa: zero no ar, menos um no que não é poça, e o resto na poça. */
    private int quanta(BlockGetter level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        if (oquê.isAir()) return 0;
        return oquê.is(this) ? QUANTA - oquê.getValue(LEVEL) : -1;
    }

    /** O {@code getLargerQuanta}: o maior dos dois, ignorando o que não tem nada. */
    private int larger(BlockGetter level, BlockPos onde, int comparar) {
        int tem = this.quanta(level, onde);
        if (tem <= 0) return comparar;
        return Math.max(tem, comparar);
    }

    /** Nascente é a casa cheia: a que tem nível zero. */
    private boolean isSource(BlockGetter level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        return oquê.is(this) && oquê.getValue(LEVEL) == 0;
    }

    /** Ela já está descendo se há poça abaixo, ou se há por onde descer. */
    private boolean flowsVertically(BlockGetter level, BlockPos onde) {
        BlockPos abaixo = onde.below();
        return level.getBlockState(abaixo).is(this)
                || level.getBlockState(onde).is(this) && this.canFlowInto(level, abaixo);
    }

    /** O que ela não empurra de jeito nenhum: as portas, as placas e a cana. */
    private static boolean keeps(BlockState oquê) {
        return oquê.is(BlockTags.DOORS) || oquê.is(BlockTags.SIGNS) || oquê.is(Blocks.SUGAR_CANE);
    }

    private static boolean isPortal(BlockState oquê) {
        return oquê.is(Blocks.NETHER_PORTAL) || oquê.is(Blocks.END_PORTAL) || oquê.is(Blocks.END_GATEWAY);
    }

    /**
     * O {@code canDisplace}: ela entra no ar e no que não segura ninguém, e não entra em líquido, noutra
     * poça, num portal nem no que ela guarda.
     *
     * <p><b>Declarado:</b> o original compara densidades para decidir entre dois fluidos dele. Aqui só existe
     * uma poça, e por isso a escada de densidades tem um degrau só.
     */
    private boolean canDisplace(BlockGetter level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        if (oquê.isAir()) return true;
        if (!oquê.getFluidState().isEmpty()) return false;
        if (oquê.is(this) || keeps(oquê)) return false;
        return !oquê.blocksMotion() && !isPortal(oquê);
    }

    /** E o {@code displaceIfPossible}, que além de poder, <b>faz</b>: o que estava ali cai no chão. */
    private boolean displaceIfPossible(ServerLevel level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        if (oquê.isAir()) return true;
        if (!this.canDisplace(level, onde)) return false;
        Block.dropResources(oquê, level, onde, level.getBlockEntity(onde));
        return true;
    }

    /** O {@code canFlowInto}, que é o de cima mais o «noutra poça, sim». */
    private boolean canFlowInto(BlockGetter level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        if (oquê.isAir() || oquê.is(this)) return true;
        if (keeps(oquê)) return false;
        return !oquê.blocksMotion() && oquê.getFluidState().isEmpty() && !isPortal(oquê);
    }

    /**
     * O {@code getOptimalFlowDirections}: por cada um dos quatro lados, quantos passos faltam até achar um
     * buraco. Ela só corre para os lados que empatam no menor — e num chão plano empatam todos, que é por
     * isso que ela se abre em cruz.
     */
    private boolean[] bestWays(ServerLevel level, BlockPos pos) {
        int[] custo = new int[WAY.length];
        for (int i = 0; i < WAY.length; i++) {
            custo[i] = 1000;
            BlockPos lado = pos.relative(WAY[i]);
            if (!this.canFlowInto(level, lado) || this.isSource(level, lado)) continue;
            custo[i] = this.canFlowInto(level, lado.below()) ? 0
                    : this.flowCost(level, lado, 1, WAY[i]);
        }

        int menor = custo[0];
        for (int i = 1; i < custo.length; i++) menor = Math.min(menor, custo[i]);

        boolean[] melhor = new boolean[WAY.length];
        for (int i = 0; i < custo.length; i++) melhor[i] = custo[i] == menor;
        return melhor;
    }

    /** O {@code calculateFlowCost}: olha até quatro casas à frente, e nunca para trás. */
    private int flowCost(ServerLevel level, BlockPos pos, int fundura, Direction veioDe) {
        int custo = 1000;
        for (Direction lado : WAY) {
            if (lado == veioDe.getOpposite()) continue;
            BlockPos adiante = pos.relative(lado);
            if (!this.canFlowInto(level, adiante) || this.isSource(level, adiante)) continue;
            if (this.canFlowInto(level, adiante.below())) return fundura;
            if (fundura < 4) custo = Math.min(custo, this.flowCost(level, adiante, fundura + 1, lado));
        }
        return custo;
    }

    /** O {@code flowIntoBlock}: põe a poça na casa e passa-lhe tudo o que a de trás carregava. */
    private void flowInto(ServerLevel level, BlockPos onde, int nível,
                          @Nullable BrewFluidBlockEntity fonte) {
        if (nível < 0 || !this.displaceIfPossible(level, onde)) return;
        level.setBlock(onde, this.defaultBlockState().setValue(LEVEL, Math.min(nível, QUANTA - 1)),
                Block.UPDATE_ALL);
        if (level.getBlockEntity(onde) instanceof BrewFluidBlockEntity nova && fonte != null) {
            nova.copyFrom(fonte);
        }
    }

    /**
     * O {@code isTargetBlock}: o que vale encharcar. Qualquer coisa que não seja ela mesma — e o ar só vale
     * quando há chão de verdade por baixo dele, que é onde a poça assenta.
     */
    private boolean isTarget(BlockGetter level, BlockPos onde) {
        BlockState oquê = level.getBlockState(onde);
        if (oquê.is(this)) return false;
        if (!oquê.isAir()) return true;
        return level.getBlockState(onde.below()).isSolidRender();
    }

    /** Onde a poça nasce, a partir de onde o frasco bateu: o mesmo da nuvem. */
    public static BlockPos where(HitResult onde) {
        return BrewGasBlock.where(onde);
    }

    /** E onde ela cabe. */
    public static boolean fits(Level level, BlockPos onde) {
        return BrewGasBlock.fits(level, onde);
    }
}
