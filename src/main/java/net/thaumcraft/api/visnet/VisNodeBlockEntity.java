package net.thaumcraft.api.visnet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Um ponto da rede de vis: o {@code TileVisNode} da 4.2.3.5.
 *
 * <p>A rede é uma árvore. Na raiz ficam as fontes (os nós energizados); cada relé se pendura no ponto mais perto que
 * esteja ao alcance e à vista, e quem pede vis pede ao pai, que pede ao pai dele, até a fonte. A cada 40 tiques cada
 * ponto confere se os filhos ainda estão lá e à vista; se não, a árvore se refaz dali para baixo.
 */
public abstract class VisNodeBlockEntity extends BlockEntity {
    @Nullable
    WeakReference<VisNodeBlockEntity> parent;
    final List<WeakReference<VisNodeBlockEntity>> children = new ArrayList<>();
    protected int nodeCounter;
    private boolean nodeRegged;
    public boolean nodeRefresh;

    protected VisNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Até onde este ponto alcança. */
    public abstract int getRange();

    /** Se é fonte (raiz da árvore). */
    public abstract boolean isSource();

    /** A cor do cristal: só se liga a quem tem a mesma, ou a quem não tem cor ({@code -1}). */
    public byte getAttunement() {
        return -1;
    }

    /** Pede vis subindo pela árvore; devolve quanto conseguiu. */
    public int consumeVis(Aspect aspect, int vis) {
        VisNodeBlockEntity parent = VisNet.valid(this.parent);
        if (parent == null) return 0;
        int out = parent.consumeVis(aspect, vis);
        if (out > 0) this.triggerConsumeEffect(aspect);
        return out;
    }

    public void triggerConsumeEffect(Aspect aspect) {
    }

    /** O pai, se ainda estiver valendo. */
    @Nullable
    public VisNodeBlockEntity parent() {
        return VisNet.valid(this.parent);
    }

    public void setParent(@Nullable VisNodeBlockEntity parent) {
        this.parent = parent == null ? null : new WeakReference<>(parent);
    }

    public List<WeakReference<VisNodeBlockEntity>> children() {
        return this.children;
    }

    /** O {@code updateEntity}: a conferência da árvore, só no servidor. */
    public void visTick() {
        if (this.level == null || this.level.isClientSide()) return;
        if (this.nodeCounter++ % 40 != 0 && !this.nodeRefresh) return;
        if (!this.nodeRefresh && !this.children.isEmpty()) {
            for (WeakReference<VisNodeBlockEntity> ref : this.children) {
                VisNodeBlockEntity child = ref == null ? null : ref.get();
                if (child == null || !VisNet.canNodeBeSeen(this, child)) {
                    this.nodeRefresh = true;
                    break;
                }
            }
        }
        if (this.nodeRefresh) {
            for (WeakReference<VisNodeBlockEntity> ref : this.children) {
                VisNodeBlockEntity child = ref.get();
                if (child != null) child.nodeRefresh = true;
            }
            this.children.clear();
            this.parent = null;
        }
        if (this.isSource() && !this.nodeRegged) {
            VisNet.addSource(this.level, this);
            this.nodeRegged = true;
        } else if (!this.isSource() && VisNet.valid(this.parent) == null) {
            this.setParent(VisNet.addNode(this.level, this));
            this.nodeRefresh = true;
        }
        if (this.nodeRefresh) {
            this.syncNode();
            this.parentChanged();
        }
        this.nodeRefresh = false;
    }

    /** O {@code removeThisNode}: desliga a subárvore toda e avisa o pai. */
    public void removeThisNode() {
        for (WeakReference<VisNodeBlockEntity> ref : new ArrayList<>(this.children)) {
            VisNodeBlockEntity child = ref == null ? null : ref.get();
            if (child != null) child.removeThisNode();
        }
        this.children.clear();
        VisNodeBlockEntity parent = VisNet.valid(this.parent);
        if (parent != null) parent.nodeRefresh = true;
        this.parent = null;
        this.parentChanged();
        if (this.isSource() && this.level != null) VisNet.removeSource(this.level, this);
        this.nodeRegged = false;
        this.syncNode();
    }

    @Override
    public void setRemoved() {
        this.removeThisNode();
        super.setRemoved();
    }

    public void parentChanged() {
    }

    /** Manda o estado para quem joga. */
    protected void syncNode() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
