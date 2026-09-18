package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.block.TubeValveBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * A válvula: o tubo que se fecha.
 *
 * <p>É um tubo comum com um manípulo de lado. Bater nele com a varinha abre ou fecha a passagem, e um
 * sinal de redstone faz o mesmo — o original vira a chave a cada vez que o sinal <em>liga</em>, não
 * enquanto ele está ligado, de modo que um botão serve de interruptor.
 *
 * <p>Fechada, ela não tem sucção nenhuma e não deixa passar nada. E é aí que a tubulação começa a
 * reclamar: a essência que estava a caminho fica sem para onde ir e o tubo atrás dela sangra, cuspindo
 * fumaça na cor do aspecto. Isso não é defeito, é o aviso do original de que a instalação foi mal
 * pensada.
 *
 * <p>O lado onde o manípulo fica nunca conecta — é o {@code facing} do original, e é por isso que a
 * válvula se põe apontando para fora do encanamento.
 */
public class TubeValveBlockEntity extends TubeBlockEntity implements Wandable {
    /** Quantos graus o manípulo gira entre aberto e fechado. */
    public static final float TURN = 270.0f;
    /** O quanto ele rosqueia para dentro ao fechar, em blocos. */
    public static final float SCREW = 0.12f;
    /** O quanto do giro ele vence por tique. */
    private static final float TURN_SPEED = 22.5f;

    private boolean allowFlow = true;
    private boolean wasPowered;
    /** Onde o manípulo está agora, entre zero e {@link #TURN}. Só serve para desenhar. */
    private float rotation;

    public TubeValveBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_VALVE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeValveBlockEntity valve) {
        // o manípulo caminha até onde devia estar, em vez de saltar
        float wanted = valve.allowFlow ? 0.0f : TURN;
        if (valve.rotation != wanted) {
            float step = Math.signum(wanted - valve.rotation) * TURN_SPEED;
            valve.rotation = Math.abs(wanted - valve.rotation) <= TURN_SPEED ? wanted : valve.rotation + step;
        }
        if (!level.isClientSide()) {
            // a chave vira quando o sinal liga, não enquanto ele fica ligado: assim um botão serve
            boolean powered = level.hasNeighborSignal(pos);
            if (powered && !valve.wasPowered) valve.setFlow(level, pos, !valve.allowFlow);
            valve.wasPowered = powered;
        }
        TubeBlockEntity.tick(level, pos, state, valve);
    }

    /** O lado para onde o manípulo aponta. Nele não se encaixa cano nenhum. */
    @Override
    protected Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(TubeValveBlock.FACING)
                ? state.getValue(TubeValveBlock.FACING) : Direction.UP;
    }

    public boolean allowsFlow() {
        return this.allowFlow;
    }

    /** Onde o manípulo está, para o desenhista. */
    public float rotation() {
        return this.rotation;
    }

    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (level.isClientSide()) return true;
        this.setFlow(level, pos, !this.allowFlow);
        player.swing(player.getUsedItemHand());
        return true;
    }

    private void setFlow(Level level, BlockPos pos, boolean allow) {
        if (this.allowFlow == allow) return;
        this.allowFlow = allow;
        // fechada, ela solta o que estava puxando; senão o resto do encanamento continuaria a lhe mandar
        if (!allow) this.setSuction(null, 0);
        level.playSound(null, pos, TCSounds.TOOL.value(), SoundSource.BLOCKS,
                0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        this.sync();
    }

    @Override
    public boolean isConnectable(Direction face) {
        // o lado do manípulo nunca conecta, e fechada ela não conecta com ninguém
        return this.allowFlow && face != this.facing() && super.isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int strength) {
        super.setSuction(this.allowFlow ? aspect : null, this.allowFlow ? strength : 0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.allowFlow = input.getBooleanOr("flow", true);
        this.rotation = this.allowFlow ? 0.0f : TURN;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("flow", this.allowFlow);
    }
}
