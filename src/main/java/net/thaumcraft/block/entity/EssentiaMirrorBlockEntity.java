package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.EssentiaSources;
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileMirrorEssentia} da 4.2.3.5: o espelho de essência. Não guarda nada e não aceita cano; é uma fonte de
 * essência para quem chama (a matriz de infusão e companhia): quando lhe pedem uma unidade, ele a tira dos
 * recipientes que estiverem à frente do par, a até oito blocos.
 */
public class EssentiaMirrorBlockEntity extends LinkedMirrorBlockEntity implements AspectContainer {
    /** O {@code linkedFacing}: para onde o par olha; nulo até alguém precisar saber. */
    @Nullable
    public Direction linkedFacing;

    public EssentiaMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ESSENTIA_MIRROR, pos, state);
    }

    @Override
    protected void onRestored(ServerLevel targetWorld) {
        BlockState there = targetWorld.getBlockState(this.linkPos());
        this.linkedFacing = there.hasProperty(MirrorBlock.FACING) ? there.getValue(MirrorBlock.FACING) : null;
    }

    @Override
    protected void onUnlinked() {
        this.linkedFacing = null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaMirrorBlockEntity mirror) {
        mirror.relinkTick();
    }

    @Override
    public AspectList getAspects() {
        return new AspectList();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return amount;
    }

    /** O {@code takeFromContainer}: uma unidade por vez, bebida do lado do par. */
    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (!this.isLinkValid() || amount > 1) return false;
        ServerLevel targetWorld = this.targetWorld();
        if (targetWorld == null) return false;
        if (this.linkedFacing == null) this.onRestored(targetWorld);
        if (this.linkedFacing == null || !(targetWorld.getBlockEntity(this.linkPos()) instanceof EssentiaMirrorBlockEntity)) return false;
        BlockPos from = EssentiaSources.drainFacing(targetWorld, this.linkPos(), aspect, this.linkedFacing, 8);
        if (from == null) return false;
        EssentiaSources.thread(targetWorld, this.linkPos(), from, aspect.color());
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(@Nullable Aspect aspect) {
        return 0;
    }
}
