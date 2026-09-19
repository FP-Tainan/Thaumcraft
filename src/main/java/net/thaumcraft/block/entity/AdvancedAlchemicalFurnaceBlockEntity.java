package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.api.visnet.VisNet;
import net.thaumcraft.block.AdvancedAlchemicalFurnaceBlock;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O {@code TileAlchemyFurnaceAdvanced} da 4.2.3.5: a fornalha alquímica avançada. Não queima combustível: a cada cinco
 * tiques bebe da rede de vis até 50 de Ignis (o calor), 50 de Perditio e 50 de Aqua (as duas forças), até 500 de cada.
 * O item que cai nela é desfeito na hora, custando o dobro do tamanho dele em calor e o tamanho em cada força, e ela
 * descansa um pouco (mais quanto mais fria). Guarda até 500 de essência, que os bicos soltam.
 */
public class AdvancedAlchemicalFurnaceBlockEntity extends BlockEntity {
    public AspectList aspects = new AspectList();
    public int vis;
    public final int maxVis = 500;
    public int heat;
    public int power1;
    public int power2;
    public final int maxPower = 500;
    public boolean destroy;
    private int count;
    private int processed;

    public AdvancedAlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedAlchemicalFurnaceBlockEntity tile) {
        tile.count++;
        if (level.isClientSide()) return;
        if (tile.destroy) {
            // uma parte saiu: tudo volta a ser as peças, o meio por último
            AdvancedAlchemicalFurnaceBlock.restoreAround(level, pos);
            level.setBlock(pos, AdvancedAlchemicalFurnaceBlock.original(0).defaultBlockState(), 3);
            return;
        }
        if (tile.processed > 0) tile.processed--;
        if (tile.count % 5 == 0) {
            int pt = tile.heat--;
            if (tile.heat <= tile.maxPower) tile.heat += VisNet.drainVis(level, pos, Aspects.FIRE, 50);
            if (tile.power1 <= tile.maxPower) tile.power1 += VisNet.drainVis(level, pos, Aspects.ENTROPY, 50);
            if (tile.power2 <= tile.maxPower) tile.power2 += VisNet.drainVis(level, pos, Aspects.WATER, 50);
            if (pt / 50 != tile.heat / 50) tile.sync();
        }
    }

    /** O {@code process}: desfaz o item se houver calor e força para o tamanho dele. */
    public boolean process(ItemStack stack) {
        if (this.processed != 0 || !this.canSmelt(stack)) return false;
        AspectList al = ObjectAspects.of(stack);
        int aa = al.visSize();
        if (aa * 2 > this.heat || aa > this.power1 || aa > this.power2) return false;
        this.heat -= aa * 2;
        this.power1 -= aa;
        this.power2 -= aa;
        this.processed = (int) (this.processed + (5.0f + Math.max(0.0f, (1.0f - (float) this.heat / this.maxPower) * 100.0f)));
        this.aspects.add(al);
        this.vis = this.aspects.visSize();
        this.sync();
        return true;
    }

    private boolean canSmelt(ItemStack stack) {
        if (stack.isEmpty()) return false;
        AspectList al = ObjectAspects.of(stack);
        return !al.isEmpty() && al.visSize() + this.aspects.visSize() <= this.maxVis;
    }

    /** O {@code markBlockForUpdate}: manda para quem vê e acerta a luz do meio. */
    public void sync() {
        this.setChanged();
        if (this.level == null) return;
        BlockState state = this.getBlockState();
        int light = this.heat > 100 ? Math.min(12, (int) ((float) this.heat / this.maxPower * 12.0f)) : 0;
        if (state.hasProperty(AdvancedAlchemicalFurnaceBlock.LIGHT) && state.getValue(AdvancedAlchemicalFurnaceBlock.LIGHT) != light) {
            this.level.setBlock(this.worldPosition, state.setValue(AdvancedAlchemicalFurnaceBlock.LIGHT, light), 3);
        } else {
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.vis = input.getIntOr("vis", 0);
        this.heat = input.getIntOr("heat", 0);
        this.aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
        this.power1 = input.getIntOr("power1", 0);
        this.power2 = input.getIntOr("power2", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("vis", (short) this.vis);
        output.putInt("heat", (short) this.heat);
        output.store("aspects", AspectList.CODEC, this.aspects);
        output.putInt("power1", (short) this.power1);
        output.putInt("power2", (short) this.power2);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
