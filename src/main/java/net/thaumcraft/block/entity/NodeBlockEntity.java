package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.registry.TCBlockEntities;

import java.util.ArrayList;
import java.util.List;

/**
 * Um nó de aura: o poço de vis de que as varinhas bebem.
 *
 * <p>As contas são as da 4.2.3.5. Cada nó tem uma lista de aspectos com um teto — o que ele tem de berço —
 * e o que sobrou agora. De tempos em tempos ele devolve um ponto a um aspecto que esteja faltando, e a
 * pressa disso é o feitio dele: seiscentos tiques no comum, quatrocentos no brilhante, novecentos no
 * pálido, e o esmaecido não se refaz nunca mais.
 */
public class NodeBlockEntity extends BlockEntity {
    /** De quanto em quanto tempo um nó comum devolve um ponto. */
    public static final int REGEN_NORMAL = 600;
    public static final int REGEN_BRIGHT = 400;
    public static final int REGEN_PALE = 900;
    /** O esmaecido não devolve nada: o que se tirou está tirado. */
    public static final int REGEN_FADING = 0;

    private AspectList base = new AspectList();
    private AspectList aspects = new AspectList();
    private NodeType type = NodeType.NORMAL;
    private NodeModifier modifier;
    private int count;

    public NodeBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE, pos, state);
    }

    /** Quantos tiques o nó espera entre um ponto e o próximo. Zero quer dizer que ele não se refaz. */
    public int regenerationInterval() {
        if (this.modifier == NodeModifier.BRIGHT) return REGEN_BRIGHT;
        if (this.modifier == NodeModifier.PALE) return REGEN_PALE;
        if (this.modifier == NodeModifier.FADING) return REGEN_FADING;
        return REGEN_NORMAL;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeBlockEntity node) {
        node.count++;
        if (level.isClientSide()) return;
        if (node.base.isEmpty()) {
            // nó posto na mão não vem com nada dentro: aqui ele ganha o que teria se tivesse nascido sozinho
            // (o do tronco do pinheiro-de-prata, como os que nascem nele)
            net.thaumcraft.world.NodeFeature.setupNode(level, pos, level.getRandom(),
                    state.is(net.thaumcraft.registry.TCBlocks.SILVERWOOD_KNOT));
            return;
        }
        int interval = node.regenerationInterval();
        if (interval <= 0 || node.count % interval != 0) return;
        if (node.rechargeOne(level)) node.sync();
    }

    /**
     * Devolve um ponto a um aspecto que esteja faltando, sorteado entre os que faltam.
     *
     * <p>É assim no original: não é o primeiro da lista nem o mais vazio — é um qualquer entre os que
     * ainda não encheram.
     */
    private boolean rechargeOne(Level level) {
        List<Aspect> missing = new ArrayList<>();
        for (Aspect aspect : this.base.getAspects()) {
            if (this.aspects.getAmount(aspect) < this.base.getAmount(aspect)) missing.add(aspect);
        }
        if (missing.isEmpty()) return false;
        this.aspects.add(missing.get(level.getRandom().nextInt(missing.size())), 1);
        return true;
    }

    /** Tira vis do nó, se houver. */
    public boolean take(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return false;
        if (this.aspects.getAmount(aspect) < amount) return false;
        this.aspects.reduce(aspect, amount);
        this.sync();
        return true;
    }

    /** Devolve vis ao nó, sem passar do que ele comporta. */
    public int give(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return 0;
        int room = this.base.getAmount(aspect) - this.aspects.getAmount(aspect);
        int given = Math.min(room, amount);
        if (given <= 0) return 0;
        this.aspects.add(aspect, given);
        this.sync();
        return given;
    }

    public AspectList aspects() {
        return this.aspects;
    }

    public AspectList baseAspects() {
        return this.base;
    }

    public NodeType type() {
        return this.type;
    }

    public NodeModifier modifier() {
        return this.modifier;
    }

    /** Quanto de vis o nó tem agora, somando tudo. */
    public int visSize() {
        return this.aspects.visSize();
    }

    public void setup(AspectList base, NodeType type, NodeModifier modifier) {
        this.base = base;
        this.aspects = base.copy();
        this.type = type;
        this.modifier = modifier;
        this.sync();
    }

    /**
     * A cor do último aspecto que uma varinha bebeu daqui, o {@code drainColor} do original.
     *
     * <p>É para onde a linha da drenagem vai puxando a cor dela. Só o desenho usa isto.
     */
    private int drainColour = 0xFFFFFF;
    /** A cor que a linha mostra agora, andando um quinto por tique até a {@link #drainColour}. */
    private int shownColour = 0xFFFFFF;
    private long shownAt;

    /** Uma varinha acabou de beber este aspecto daqui. */
    public void drained(Aspect aspect) {
        if (this.drainColour == aspect.color()) return;
        this.drainColour = aspect.color();
        this.sync();
    }

    /**
     * A cor da linha da drenagem neste instante.
     *
     * <p>O original não troca de cor de uma vez: a cada tique ele anda um quinto do caminho da cor que
     * estava até a do aspecto bebido, e é isso que dá à linha o degradê quando a varinha passa de um
     * aspecto a outro.
     */
    public int shownColour(long gameTime) {
        int steps = (int) Math.min(20, gameTime - this.shownAt);
        for (int step = 0; step < steps; step++) {
            int r = ((this.drainColour >> 16 & 255) + (this.shownColour >> 16 & 255) * 4) / 5;
            int g = ((this.drainColour >> 8 & 255) + (this.shownColour >> 8 & 255) * 4) / 5;
            int b = ((this.drainColour & 255) + (this.shownColour & 255) * 4) / 5;
            this.shownColour = r << 16 | g << 8 | b;
        }
        this.shownAt = gameTime;
        return this.shownColour;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.base = input.read("base", AspectList.CODEC).orElseGet(AspectList::new);
        this.aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.type = input.read("type", NodeType.CODEC).orElse(NodeType.NORMAL);
        this.modifier = input.read("modifier", NodeModifier.CODEC).orElse(null);
        this.drainColour = input.getIntOr("drain", 0xFFFFFF);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("base", AspectList.CODEC, this.base);
        output.store("aspects", AspectList.CODEC, this.aspects);
        output.store("type", NodeType.CODEC, this.type);
        if (this.modifier != null) output.store("modifier", NodeModifier.CODEC, this.modifier);
        output.putInt("drain", this.drainColour);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
