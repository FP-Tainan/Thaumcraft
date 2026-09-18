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
import net.thaumcraft.api.aspects.EssentiaTransport;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.block.TubeValveBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * A válvula: o tubo que se fecha. É o {@code TileTubeValve} da 4.2.3.5, descompilado.
 *
 * <p>Ela se abre e se fecha <strong>com a mão</strong> — clicando nela com qualquer coisa que não seja
 * varinha nem cano — e com redstone: o sinal ligando fecha, o sinal desligando abre. Ou seja, uma
 * alavanca do lado manda nela diretamente. As duas coisas guincham ({@code squeek}).
 *
 * <p>A varinha faz outra coisa: gira o manípulo para o próximo lado livre, sem cano encostado. É assim que
 * se ajeita a válvula depois de posta.
 *
 * <p>Fechada, ela continua encaixada nos canos, mas não tem sucção nenhuma e não deixa passar nada. E é aí que a tubulação reclama: a
 * essência que vinha a caminho fica sem para onde ir e o tubo atrás sangra, cuspindo vapor na cor do
 * aspecto. O lado do manípulo nunca conecta.
 */
public class TubeValveBlockEntity extends TubeBlockEntity implements Wandable {
    /** Até onde o manípulo vai: fechado ele está em trezentos e sessenta. */
    public static final float CLOSED = 360.0f;
    /** Quanto ele anda por tique, no original. */
    private static final float TURN_SPEED = 20.0f;

    private boolean allowFlow = true;
    private boolean wasPowered;
    private int count;
    /** Onde o manípulo está agora, de zero a {@link #CLOSED}. Só o lado de quem desenha anda isto. */
    private float rotation;

    public TubeValveBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_VALVE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeValveBlockEntity valve) {
        if (!level.isClientSide() && valve.count++ % 5 == 0) {
            // a redstone manda pelo nível: ligou, fecha; desligou, abre
            boolean powered = level.hasNeighborSignal(pos);
            if (valve.wasPowered && !powered && !valve.allowFlow) valve.setFlow(level, pos, true);
            if (!valve.wasPowered && powered && valve.allowFlow) valve.setFlow(level, pos, false);
            valve.wasPowered = powered;
        }
        if (level.isClientSide()) {
            // o manípulo vai girando até onde devia estar, vinte graus por tique
            if (!valve.allowFlow && valve.rotation < CLOSED) valve.rotation += TURN_SPEED;
            else if (valve.allowFlow && valve.rotation > 0.0f) valve.rotation -= TURN_SPEED;
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

    /** O clique com a mão: abre ou fecha. */
    public void toggleByHand(Level level, BlockPos pos) {
        this.setFlow(level, pos, !this.allowFlow);
    }

    /**
     * A varinha gira o manípulo para o próximo lado livre.
     *
     * <p>O original anda os seis lados a partir do atual, na ordem de baixo, cima, norte, sul, oeste e
     * leste, e para no primeiro que não tem nada de encanar encostado.
     */
    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (level.isClientSide()) return true;
        Direction current = this.facing();
        for (int step = 1; step <= 6; step++) {
            Direction next = Direction.from3DDataValue((current.get3DDataValue() + step) % 6);
            if (level.getBlockEntity(pos.relative(next)) instanceof EssentiaTransport) continue;
            level.setBlock(pos, this.getBlockState().setValue(TubeValveBlock.FACING, next), 3);
            level.playSound(null, pos, TCSounds.TOOL.value(), SoundSource.BLOCKS,
                    0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);
            player.swing(player.getUsedItemHand());
            break;
        }
        return true;
    }

    private void setFlow(Level level, BlockPos pos, boolean allow) {
        if (this.allowFlow == allow) return;
        this.allowFlow = allow;
        // fechada, ela solta o que estava puxando; senão o resto do encanamento continuaria a lhe mandar
        if (!allow) this.setSuction(null, 0);
        level.playSound(null, pos, TCSounds.SQUEEK.value(), SoundSource.BLOCKS,
                0.7f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        this.sync();
    }

    @Override
    public boolean isConnectable(Direction face) {
        // o lado do manípulo nunca conecta. Fechada ela continua encaixada nos canos -- o original não
        // solta os braços, só para a passagem
        return face != this.facing() && super.isConnectable(face);
    }

    @Override
    public int takeEssentia(Aspect wanted, int requested, Direction face) {
        return this.allowFlow ? super.takeEssentia(wanted, requested, face) : 0;
    }

    @Override
    public int addEssentia(Aspect wanted, int requested, Direction face) {
        return this.allowFlow ? super.addEssentia(wanted, requested, face) : 0;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int strength) {
        super.setSuction(this.allowFlow ? aspect : null, this.allowFlow ? strength : 0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.allowFlow = input.getBooleanOr("flow", true);
        // quando o bloco chega do disco o manípulo já nasce no lugar; quando é só um aviso de mudança,
        // ele fica onde está e o tique o leva até lá girando
        if (this.level == null) this.rotation = this.allowFlow ? 0.0f : CLOSED;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("flow", this.allowFlow);
    }
}
