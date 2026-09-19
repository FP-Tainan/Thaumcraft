package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.visnet.VisNet;
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code TileMirror} da 4.2.3.5: o espelho mágico. O que entra por um sai pelo par, um item por vez; cada item que
 * passa deixa o vidro mais instável, e quanto mais instável, mais devagar ele cospe. A instabilidade cai sozinha (um
 * ponto por segundo) ou com Ordo da rede de vis.
 *
 * <p>Como no original, ele é um inventário de uma casa só que nunca guarda nada: o que um funil põe nele vai direto
 * para o par.
 */
public class MirrorBlockEntity extends LinkedMirrorBlockEntity implements Container {
    public int instability;
    private List<ItemStack> outputStacks = new ArrayList<>();

    public MirrorBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MIRROR, pos, state);
    }

    private Direction face() {
        return this.getBlockState().getValue(MirrorBlock.FACING);
    }

    /** O {@code transport}: o item jogado no espelho vai para a fila do par. */
    public boolean transport(ItemEntity ie) {
        ItemStack items = ie.getItem();
        if (this.linked && this.isLinkValid()) {
            ServerLevel world = this.targetWorld();
            BlockEntity target = world == null ? null : world.getBlockEntity(this.linkPos());
            if (target instanceof MirrorBlockEntity mirror) {
                mirror.addStack(items.copy());
                this.addInstability(null, items.getCount());
                ie.discard();
                this.setChanged();
                target.setChanged();
                this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 0);
                return true;
            }
        }
        return false;
    }

    /** O {@code eject}: depois do primeiro segundo, cospe um item de uma pilha qualquer da fila. */
    public void eject() {
        if (!this.outputStacks.isEmpty() && this.count > 20) {
            int i = this.level.getRandom().nextInt(this.outputStacks.size());
            ItemStack stack = this.outputStacks.get(i);
            if (!stack.isEmpty()) {
                if (this.spawnItem(stack.copyWithCount(1))) {
                    stack.shrink(1);
                    this.addInstability(null, 1);
                    this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 0);
                    if (stack.isEmpty()) this.outputStacks.remove(i);
                    this.setChanged();
                }
            }
        }
    }

    /** O {@code spawnItem}: o item sai do vidro para a frente, devagar, e só volta a entrar num espelho um segundo depois. */
    public boolean spawnItem(ItemStack stack) {
        Direction face = this.face();
        ItemEntity ie2 = new ItemEntity(this.level,
                this.worldPosition.getX() + 0.5 - face.getStepX() * 0.3,
                this.worldPosition.getY() + 0.5 - face.getStepY() * 0.3,
                this.worldPosition.getZ() + 0.5 - face.getStepZ() * 0.3, stack);
        ie2.setDeltaMovement(face.getStepX() * 0.15f, face.getStepY() * 0.15f, face.getStepZ() * 0.15f);
        ie2.setPortalCooldown(20);
        return this.level.addFreshEntity(ie2);
    }

    /** O {@code addInstability}: neste espelho e, se vier o mundo, no par também. */
    protected void addInstability(@Nullable Level targetWorld, int amt) {
        this.instability += amt;
        if (targetWorld != null && targetWorld.getBlockEntity(this.linkPos()) instanceof MirrorBlockEntity te) {
            te.instability += amt;
            if (te.instability < 0) te.instability = 0;
            te.setChanged();
        }
    }

    /** Quantos itens esperam na fila para sair. */
    public int queued() {
        return this.outputStacks.stream().mapToInt(ItemStack::getCount).sum();
    }

    public void addStack(ItemStack stack) {
        this.outputStacks.add(stack);
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MirrorBlockEntity mirror) {
        int tickrate = mirror.instability / 50;
        if (tickrate == 0 || mirror.count % (tickrate * tickrate) == 0) mirror.eject();
        mirror.checkInstability();
        mirror.relinkTick();
    }

    /** O {@code checkInstability}: um ponto a menos por segundo, e o Ordo que a rede de vis mandar. */
    public void checkInstability() {
        if (this.instability > 0 && this.count % 20 == 0) {
            this.instability--;
            this.sync();
        }
        if (this.instability > 0) {
            int amt = VisNet.drainVis(this.level, this.worldPosition, Aspects.ORDER, Math.min(this.instability, 1));
            if (amt > 0) this.addInstability(this.targetWorld(), -amt);
        }
    }

    /** O evento 1: a fumacinha escura saindo do vidro. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != 1) return super.triggerEvent(id, param);
        if (this.level != null && this.level.isClientSide()) {
            Direction face = this.face();
            var random = this.level.getRandom();
            for (int q = 0; q < 2; q++) {
                double xx = this.worldPosition.getX() + 0.33 + random.nextFloat() * 0.33f - face.getStepX() / 2.0;
                double yy = this.worldPosition.getY() + 0.33 + random.nextFloat() * 0.33f - face.getStepY() / 2.0;
                double zz = this.worldPosition.getZ() + 0.33 + random.nextFloat() * 0.33f - face.getStepZ() / 2.0;
                this.level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x80000000),
                        xx, yy, zz, face.getStepX() * 0.05, face.getStepY() * 0.05, face.getStepZ() * 0.05);
            }
        }
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.instability = input.getIntOr("instability", 0);
        this.outputStacks = new ArrayList<>(input.read("Items", ItemStack.CODEC.listOf()).orElseGet(List::of));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("instability", this.instability);
        List<ItemStack> keep = this.outputStacks.stream().filter(s -> !s.isEmpty()).toList();
        output.store("Items", ItemStack.CODEC.listOf(), keep);
    }

    // o inventário de uma casa que nunca guarda nada

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    /** O {@code setInventorySlotContents}: vai para a fila do par; sem par, cai de volta para fora. */
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (stack.isEmpty() || this.level == null || this.level.isClientSide()) return;
        ServerLevel world = this.targetWorld();
        if (world != null && world.getBlockEntity(this.linkPos()) instanceof MirrorBlockEntity target) {
            target.addStack(stack.copy());
            this.addInstability(null, stack.getCount());
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 0);
        } else {
            this.spawnItem(stack.copy());
        }
    }

    /** O {@code isItemValidForSlot}: só aceita se houver espelho no lugar do par. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        ServerLevel world = this.targetWorld();
        return world != null && world.getBlockEntity(this.linkPos()) instanceof MirrorBlockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
    }
}
