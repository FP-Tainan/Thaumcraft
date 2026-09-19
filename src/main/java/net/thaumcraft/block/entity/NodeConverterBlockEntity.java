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
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.NodeStabilizerBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;

/**
 * O transdutor de nó: o {@code TileNodeConverter} da 4.2.3.5. Fica em cima de um nó estabilizado; com sinal de
 * redstone, vai drenando o nó ponto a ponto (mil tiques) e o transforma num nó energizado, a fonte da rede de vis.
 * Com o nó já energizado, tirar o sinal desfaz: em cinquenta tiques ele volta a ser nó, vazio. Sem o estabilizador
 * embaixo do nó energizado, tudo estoura.
 *
 * <p>{@code status}: 0 parado, 1 energizando, 2 segurando um nó energizado.
 */
public class NodeConverterBlockEntity extends BlockEntity {
    public int count = -1;
    public int status;

    public NodeConverterBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_CONVERTER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeConverterBlockEntity converter) {
        converter.update(level, pos);
    }

    private void update(Level level, BlockPos pos) {
        if (this.count == -1) this.checkStatus(level, pos);
        BlockPos below = pos.below();
        boolean server = !level.isClientSide();

        if (this.status == 1 && server && this.count >= 1000 && level.getBlockEntity(below) instanceof NodeBlockEntity node
                && !(node instanceof NodeJarBlockEntity)) {
            AspectList base = node.baseAspects().copy();
            var type = node.type();
            var modifier = node.modifier();
            level.setBlock(below, TCBlocks.ENERGIZED_NODE.defaultBlockState(), 3);
            if (level.getBlockEntity(below) instanceof EnergizedNodeBlockEntity energized) energized.setup(base, type, modifier);
            this.checkStatus(level, pos);
            level.blockEvent(pos, this.getBlockState().getBlock(), 10, 10);
            this.sync();
        }

        if (this.status == 2 && server && this.count <= 50 && level.getBlockEntity(below) instanceof EnergizedNodeBlockEntity energized) {
            AspectList aura = energized.auraBase().copy();
            var type = energized.type();
            var modifier = energized.modifier();
            level.setBlock(below, TCBlocks.NODE.defaultBlockState(), 3);
            // volta nó, com o teto de antes e nada dentro
            if (level.getBlockEntity(below) instanceof NodeBlockEntity node) node.setup(aura, new AspectList(), type, modifier);
            level.blockEvent(pos, this.getBlockState().getBlock(), 10, 10);
            this.status = 0;
            this.sync();
        }

        if (this.status != 0 && level.hasNeighborSignal(pos)) {
            if (this.count < 1000) {
                this.count++;
                if (server && level.getBlockEntity(below) instanceof NodeBlockEntity node) {
                    List<Aspect> have = node.aspects().getAspects();
                    if (!have.isEmpty()) {
                        node.take(have.get(level.getRandom().nextInt(have.size())), 1);
                    }
                }
                if (this.count > 50 && !server) this.bolts(level, pos);
            }
        } else if (this.count > 0) {
            this.count--;
            if (this.count > 50 && !server) this.bolts(level, pos);
        }
        if (this.count > 1000) this.count = 1000;
    }

    /** Os raios do transdutor até o nó e, com o estabilizador, do estabilizador até o nó. */
    private void bolts(Level level, BlockPos pos) {
        var random = level.getRandom();
        Vec3 node = new Vec3(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5);
        if (random.nextBoolean()) {
            net.thaumcraft.client.NodeClient.nodeBolt(new Vec3(pos.getX() + 0.25 + random.nextFloat() * 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.25 + random.nextFloat() * 0.5), node);
        }
        if (random.nextBoolean() && this.hasStabilizer(level, pos)) {
            net.thaumcraft.client.NodeClient.nodeBolt(new Vec3(pos.getX() + 0.25 + random.nextFloat() * 0.5, pos.getY() - 1.5,
                    pos.getZ() + 0.25 + random.nextFloat() * 0.5), node);
        }
    }

    private boolean hasStabilizer(Level level, BlockPos pos) {
        BlockPos two = pos.below(2);
        return !level.hasNeighborSignal(two) && level.getBlockState(two).getBlock() instanceof NodeStabilizerBlock;
    }

    /** O {@code checkStatus}: decide o que fazer, e estoura o nó energizado que ficou sem estabilizador. */
    public void checkStatus(Level level, BlockPos pos) {
        if (this.count == -1) this.count = 0;
        BlockPos below = pos.below();
        boolean energized = level.getBlockState(below).is(TCBlocks.ENERGIZED_NODE);
        if (this.status == 2 && this.count > 50 && !(this.hasStabilizer(level, pos) && energized)) {
            if (!level.isClientSide()) net.thaumcraft.block.EnergizedNodeBlock.explodify(level, below);
            this.status = 0;
            this.count = 50;
            this.sync();
            return;
        }
        if (level.hasNeighborSignal(pos) && level.getBlockState(below).is(TCBlocks.NODE) && this.hasStabilizer(level, pos)) {
            this.status = 1;
        } else if (energized) {
            this.status = 2;
            this.count = 1000;
        } else {
            this.status = 0;
        }
        this.sync();
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 10 && param == 10) {
            if (this.level != null && this.level.isClientSide()) {
                BlockPos pos = this.getBlockPos();
                net.thaumcraft.client.NodeClient.burst(this.level, new Vec3(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5));
            }
            return true;
        }
        return super.triggerEvent(id, param);
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
        this.status = input.getIntOr("status", 0);
        this.count = input.getIntOr("count", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("status", this.status);
        output.putInt("count", this.count);
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
