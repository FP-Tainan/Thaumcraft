package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeModifier;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.api.visnet.VisNodeBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O nó energizado: o {@code TileNodeEnergized} da 4.2.3.5. É a fonte da rede de vis: não guarda vis, gera. A cada tique
 * tem, de cada primordial de que o nó era feito, a raiz quadrada do que ele tinha (mais com o brilhante, menos com o
 * pálido e o esmaecido), e é isso que os relés podem puxar dele naquele tique. O instável às vezes se desarranja e
 * refaz a conta.
 */
public class EnergizedNodeBlockEntity extends VisNodeBlockEntity {
    private AspectList auraBase = new AspectList().add(Aspects.AIR, 20).add(Aspects.FIRE, 20).add(Aspects.EARTH, 20)
            .add(Aspects.WATER, 20).add(Aspects.ORDER, 20).add(Aspects.ENTROPY, 20);
    private AspectList visBase = new AspectList();
    private AspectList vis = new AspectList();
    private NodeType type = NodeType.NORMAL;
    private NodeModifier modifier;

    public EnergizedNodeBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ENERGIZED_NODE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergizedNodeBlockEntity node) {
        node.visTick();
        if (level.isClientSide()) return;
        if (node.type == NodeType.UNSTABLE && level.getRandom().nextInt(500) == 1) node.visBase = new AspectList();
        if (node.visBase.size() == 0 && node.auraBase.size() > 0) node.setupNode();
        node.vis = node.visBase.copy();
    }

    /** O {@code setupNode}: a raiz quadrada de cada primordial de que a aura era feita. */
    public void setupNode() {
        this.visBase = new AspectList();
        AspectList primals = reduceToPrimals(this.auraBase);
        for (Aspect aspect : primals.getAspects()) {
            int amount = primals.getAmount(aspect);
            if (this.modifier == NodeModifier.BRIGHT) amount = (int) (amount * 1.2f);
            if (this.modifier == NodeModifier.PALE) amount = (int) (amount * 0.8f);
            if (this.modifier == NodeModifier.FADING) amount = (int) (amount * 0.5f);
            amount = Mth.floor(Mth.sqrt(amount));
            if (this.type == NodeType.UNSTABLE && this.level != null) amount += this.level.getRandom().nextInt(5) - 2;
            if (amount >= 1) this.visBase.merge(aspect, amount);
        }
        this.syncNode();
    }

    /** O {@code reduceToPrimals} com mescla: de cada primordial fica o maior tanto, não a soma. */
    private static AspectList reduceToPrimals(AspectList list) {
        AspectList out = new AspectList();
        for (Aspect aspect : list.getAspects()) {
            if (aspect.isPrimal()) {
                out.merge(aspect, list.getAmount(aspect));
            } else {
                AspectList parts = new AspectList();
                parts.add(aspect.components()[0], list.getAmount(aspect));
                parts.add(aspect.components()[1], list.getAmount(aspect));
                AspectList reduced = reduceToPrimals(parts);
                for (Aspect part : reduced.getAspects()) out.merge(part, reduced.getAmount(part));
            }
        }
        return out;
    }

    /** O transdutor energizou um nó: guarda de que era feito e refaz a conta. */
    public void setup(AspectList aura, NodeType type, NodeModifier modifier) {
        this.auraBase = aura.copy();
        this.type = type;
        this.modifier = modifier;
        this.setupNode();
    }

    public AspectList auraBase() {
        return this.auraBase;
    }

    /** O que ele gera por tique, de cada primordial. */
    public AspectList visBase() {
        return this.visBase;
    }

    public NodeType type() {
        return this.type;
    }

    public NodeModifier modifier() {
        return this.modifier;
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int consumeVis(Aspect aspect, int amount) {
        int drain = Math.min(this.vis.getAmount(aspect), amount);
        if (drain > 0) this.vis.reduce(aspect, drain);
        return drain;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.auraBase = input.read("aura", AspectList.CODEC).orElseGet(AspectList::new);
        this.visBase = input.read("vis", AspectList.CODEC).orElseGet(AspectList::new);
        this.type = input.read("type", NodeType.CODEC).orElse(NodeType.NORMAL);
        this.modifier = input.read("modifier", NodeModifier.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aura", AspectList.CODEC, this.auraBase);
        output.store("vis", AspectList.CODEC, this.visBase);
        output.store("type", NodeType.CODEC, this.type);
        if (this.modifier != null) output.store("modifier", NodeModifier.CODEC, this.modifier);
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
