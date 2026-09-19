package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code TileManaPod} da 4.2.3.5: o aspecto que a vagem vai dar. Ele se decide quando ela chega ao tamanho três —
 * das vagens vizinhas saem os aspectos delas e as misturas dos pares (as misturas valendo em dobro no sorteio) — e,
 * sem ninguém em volta, um em oito é Herba e o resto um primordial qualquer.
 */
public class ManaPodBlockEntity extends BlockEntity {
    @Nullable
    public Aspect aspect;

    public ManaPodBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MANA_POD, pos, state);
    }

    /** O {@code checkGrowth}: cresce um e, no tamanho três, escolhe o aspecto. */
    public void checkGrowth() {
        if (this.level == null) return;
        BlockState state = this.getBlockState();
        int age = state.getValue(ManaPodBlock.AGE);
        if (age < 7) {
            age++;
            this.level.setBlock(this.worldPosition, state.setValue(ManaPodBlock.AGE, age), 3);
        }
        if (age <= 2) return;
        if (age == 3) {
            AspectList around = new AspectList();
            if (this.aspect != null) around.add(this.aspect, 1);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof ManaPodBlockEntity pod && pod.aspect != null) {
                    around.add(pod.aspect, 1);
                }
            }
            if (around.size() > 1) {
                List<Aspect> all = around.getAspects();
                List<Aspect> out = new ArrayList<>();
                for (int i = 0; i < all.size(); i++) {
                    out.add(all.get(i));
                    for (int j = 0; j < all.size(); j++) {
                        if (i == j) continue;
                        Aspect combo = Aspects.combination(all.get(i), all.get(j));
                        if (combo != null) {
                            out.add(combo);
                            out.add(combo);
                        }
                    }
                }
                if (!out.isEmpty()) {
                    this.aspect = out.get(this.level.getRandom().nextInt(out.size()));
                    this.setChanged();
                }
            }
            if (around.size() >= 1 && this.aspect == null) {
                this.aspect = around.getAspectsSortedAmount().get(0);
                this.setChanged();
            }
        }
        if (this.aspect == null) {
            if (this.level.getRandom().nextInt(8) == 0) {
                this.aspect = Aspects.PLANT;
            } else {
                List<Aspect> primals = Aspects.primals();
                this.aspect = primals.get(this.level.getRandom().nextInt(primals.size()));
            }
            this.setChanged();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.aspect = Aspect.of(input.getStringOr("aspect", ""));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.aspect != null) output.putString("aspect", this.aspect.tag());
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
