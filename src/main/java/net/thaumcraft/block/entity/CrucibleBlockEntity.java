package net.thaumcraft.block.entity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.entity.SpecialItemEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.Flux;

import java.util.List;

/**
 * O crisol: o {@code TileCrucible} da 4.2.3.5.
 *
 * <p>Um tanque de 1000 mB de água. Com água e fogo, lava ou nitor embaixo, esquenta um grau por tique (mais dois por
 * fole em volta) até 200; sem, esfria. Passando de 150 ferve: o que cai dentro se desfaz nos aspectos (a pilha
 * inteira) ou, se fecha uma receita com o catalisador, sai a coisa nova, flutuando, e a receita bebe 50 mB. Com mais
 * de cem de essência dissolvida, a cada cinco tiques um ponto escapa como fluxo; e fervendo, a cada cinco segundos
 * sossegados um aspecto composto se desfaz num dos seus componentes (um primordial escapa como fluxo), bebendo 2 mB.
 * Quem entra fervendo se queima. A varinha, agachado, despeja tudo — como quebrar o crisol.
 */
public class CrucibleBlockEntity extends BlockEntity implements Wandable, net.thaumcraft.api.aspects.AspectContainer {
    /** O quanto ele chega a esquentar. */
    public static final int MAX_HEAT = 200;
    /** Acima disto a água ferve. */
    public static final int BOILING = 150;
    /** A capacidade do tanque, em mB. */
    public static final int CAPACITY = 1000;

    private int heat;
    private AspectList aspects = new AspectList();
    private int bellows = -1;
    private long counter = -100L;
    /** O dano de quem entra: o {@code delay} do bloco no original. */
    private int touchDelay;

    public final SingleVariantStorage<FluidVariant> tank = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET;
        }

        @Override
        protected boolean canInsert(FluidVariant variant) {
            return variant.isOf(Fluids.WATER);
        }

        @Override
        protected void onFinalCommit() {
            CrucibleBlockEntity.this.sync();
        }
    };

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CRUCIBLE, pos, state);
    }

    // ------------------------------------------------------------------------------------------------ a água

    /** A água no tanque, em mB. */
    public int water() {
        return (int) (this.tank.amount * CAPACITY / FluidConstants.BUCKET);
    }

    public boolean hasWater() {
        return this.tank.amount > 0;
    }

    /** Enche (ou esvazia) o tanque de uma vez. */
    public void setWater(boolean full) {
        if (full) {
            this.tank.variant = FluidVariant.of(Fluids.WATER);
            this.tank.amount = FluidConstants.BUCKET;
        } else {
            this.tank.variant = FluidVariant.blank();
            this.tank.amount = 0;
        }
        this.sync();
    }

    /** O {@code tank.drain}: tira tantos mB. */
    private void drain(int mb) {
        this.tank.amount = Math.max(0, this.tank.amount - mb * FluidConstants.BUCKET / CAPACITY);
        if (this.tank.amount == 0) this.tank.variant = FluidVariant.blank();
    }

    /** O {@code fill} com balde ou garrafa: um balde inteiro, se não está cheio. */
    public boolean fillBucket() {
        if (this.water() >= CAPACITY) return false;
        this.setWater(true);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ o tique

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        crucible.counter++;
        int prevheat = crucible.heat;
        if (level instanceof ServerLevel server) {
            if (crucible.bellows < 0) crucible.getBellows();
            if (!crucible.hasWater()) {
                if (crucible.heat > 0) crucible.heat--;
            } else if (heatSource(level, pos.below())) {
                if (crucible.heat < MAX_HEAT) {
                    crucible.heat += 1 + crucible.bellows * 2;
                    if (prevheat < 151 && crucible.heat >= 151) crucible.sync();
                }
            } else if (crucible.heat > 0) {
                crucible.heat--;
                if (crucible.heat == 149) crucible.sync();
            }
            if (crucible.tagAmount() > 100 && crucible.counter % 5L == 0L) {
                crucible.takeRandomFromSource();
                Flux.crucibleSpill(server, pos);
            }
            if (crucible.counter > 100L && crucible.heat > BOILING) {
                crucible.counter = 0L;
                if (crucible.tagAmount() > 0) crucible.decompose(server, pos);
                crucible.sync();
            }
        } else if (crucible.hasWater()) {
            clientEffects.accept(crucible);
        }
        // o cliente adianta um grau quando cruza a fervura, como o original
        if (level.isClientSide() && prevheat < 151 && crucible.heat >= 151) crucible.heat++;
    }

    /** O que esquenta: fogo, lava, ou o nitor. */
    private static boolean heatSource(Level level, BlockPos below) {
        BlockState state = level.getBlockState(below);
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || level.getFluidState(below).is(FluidTags.LAVA)
                || state.is(TCBlocks.NITOR);
    }

    /** Uma rodada da decomposição: um aspecto (sorteado de novo, uma vez, se deu primordial) perde um ponto. */
    private void decompose(ServerLevel level, BlockPos pos) {
        List<Aspect> list = this.aspects.getAspects();
        int s = list.size();
        Aspect a = list.get(level.getRandom().nextInt(s));
        if (a.isPrimal()) a = list.get(level.getRandom().nextInt(s));
        this.drain(2);
        this.aspects.remove(a, 1);
        if (!a.isPrimal()) {
            Aspect[] parts = a.components();
            this.aspects.add(level.getRandom().nextBoolean() ? parts[0] : parts[1], 1);
        } else {
            Flux.crucibleSpill(level, pos);
        }
    }

    /** O {@code takeRandomFromSource}: um ponto de um aspecto sorteado some. */
    private void takeRandomFromSource() {
        if (this.aspects.size() > 0) {
            List<Aspect> list = this.aspects.getAspects();
            this.aspects.remove(list.get(this.level.getRandom().nextInt(list.size())), 1);
        }
        this.sync();
    }

    /** O {@code getBellows}: os foles dos quatro lados (para onde quer que apontem). */
    public void getBellows() {
        this.bellows = 0;
        if (this.level == null) return;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (this.level.getBlockState(this.worldPosition.relative(d)).is(TCBlocks.BELLOWS)) this.bellows++;
        }
    }

    /** O total de essência dissolvida. */
    public int tagAmount() {
        return this.aspects.visSize();
    }

    /** A altura da água (e do que está nela), de 0,3 a quase 1. */
    public float fluidHeight() {
        float base = 0.3f + 0.5f * ((float) this.water() / CAPACITY);
        float out = base + this.tagAmount() / 100.0f * (1.0f - base);
        if (out > 1.0f) out = 1.001f;
        if (out == 1.0f) out = 0.9999f;
        return out;
    }

    // ------------------------------------------------------------------------------------------------ o que cai dentro

    /** O {@code onEntityCollidedWithBlock}: fervendo, o item se desfaz; quem vive se queima de dez em dez toques. */
    public void touched(Level level, net.minecraft.world.entity.Entity entity) {
        if (level.isClientSide()) return;
        if (entity instanceof ItemEntity item && !(entity instanceof SpecialItemEntity) && this.heat > BOILING && this.hasWater()) {
            this.attemptSmelt(item);
            return;
        }
        if (++this.touchDelay < 10) return;
        this.touchDelay = 0;
        if (entity instanceof net.minecraft.world.entity.LivingEntity living && this.heat > BOILING && this.hasWater()
                && level instanceof ServerLevel server) {
            living.hurtServer(server, level.damageSources().inFire(), 1.0f);
            level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 2.0f + level.getRandom().nextFloat() * 0.4f);
        }
    }

    /** O {@code attemptSmelt}: cada item da pilha fecha uma receita ou se desfaz nos aspectos. */
    public void attemptSmelt(ItemEntity entity) {
        Level level = this.level;
        if (level == null) return;
        boolean bubble = false, event = false;
        ItemStack item = entity.getItem();
        Player thrower = entity.getOwner() instanceof Player p ? p : null;
        int stacksize = item.getCount();
        for (int a = 0; a < stacksize; a++) {
            CrucibleRecipe rc = CrucibleRecipes.find(this.aspects, item);
            // sem quem jogou (um funil, por exemplo) não há pesquisa que valha, como o nome vazio do original
            if (rc != null && (thrower == null || !net.thaumcraft.research.ResearchManager.knows(thrower, rc.research()))) rc = null;
            if (rc != null && this.hasWater()) {
                ItemStack out = rc.result().copy();
                if (thrower != null) out.onCraftedBy(thrower, out.getCount());
                this.aspects = rc.removeFrom(this.aspects);
                this.drain(50);
                this.ejectItem(out);
                event = true;
                stacksize--;
                this.counter = -250L;
            } else {
                AspectList ot = ObjectAspects.of(item);
                if (ot == null || ot.isEmpty()) {
                    entity.setDeltaMovement((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f, 0.35f,
                            (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f);
                    level.playSound(null, entity, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                            (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7f + 1.0f);
                    return;
                }
                for (Aspect tag : ot.getAspects()) this.aspects.add(tag, ot.getAmount(tag));
                bubble = true;
                stacksize--;
                this.counter = -150L;
            }
        }
        if (bubble) {
            level.playSound(null, entity, TCSounds.BUBBLE.value(), SoundSource.BLOCKS, 0.2f, 1.0f + level.getRandom().nextFloat() * 0.4f);
            this.sync();
            level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 2, 1);
        }
        if (event) {
            this.sync();
            level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 2, 5);
        }
        if (stacksize <= 0) {
            entity.discard();
        } else {
            item.setCount(stacksize);
            entity.setItem(item);
        }
        this.setChanged();
    }

    /** O {@code ejectItem}: a coisa nova sobe flutuando, de pilha cheia em pilha cheia. */
    private void ejectItem(ItemStack items) {
        boolean first = true;
        do {
            ItemStack spitout = items.copy();
            if (spitout.getCount() > spitout.getMaxStackSize()) spitout.setCount(spitout.getMaxStackSize());
            items.shrink(spitout.getCount());
            SpecialItemEntity entity = new SpecialItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.71,
                    this.worldPosition.getZ() + 0.5, spitout);
            var random = this.level.getRandom();
            entity.setDeltaMovement(first ? 0.0 : (random.nextFloat() - random.nextFloat()) * 0.01f, 0.1f,
                    first ? 0.0 : (random.nextFloat() - random.nextFloat()) * 0.01f);
            this.level.addFreshEntity(entity);
            first = false;
        } while (items.getCount() > 0);
    }

    /** O {@code spillRemnants}: a água vai embora e cada dois de essência viram um derrame de fluxo. */
    public void spillRemnants() {
        if (!(this.level instanceof ServerLevel level)) return;
        if (!this.hasWater() && this.aspects.visSize() <= 0) return;
        this.tank.variant = FluidVariant.blank();
        this.tank.amount = 0;
        for (int a = 0; a < this.aspects.visSize() / 2; a++) Flux.crucibleSpill(level, this.worldPosition);
        this.aspects = new AspectList();
        this.sync();
        level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 2, 5);
    }

    /** O {@code breakBlock}: quebrado, despeja. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.spillRemnants();
        super.preRemoveSideEffects(pos, state);
    }

    /** A varinha, agachado, despeja o crisol. */
    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (!player.isShiftKeyDown()) return false;
        if (!level.isClientSide()) this.spillRemnants();
        return true;
    }

    /** O {@code getComparatorInputOverride}. */
    public int comparator() {
        float r = this.aspects.visSize() / 100.0f;
        return (int) Math.floor(r * 14.0f) + (this.aspects.visSize() > 0 ? 1 : 0);
    }

    // ------------------------------------------------------------------------------------------------ do lado de quem vê

    /** Os efeitos da água fervendo (o {@code drawEffects}; o cliente liga isto). */
    public static java.util.function.Consumer<CrucibleBlockEntity> clientEffects = crucible -> {
    };

    /** O evento 1 (faíscas) e o 2 (a fervura, com a força dada) (o cliente liga isto). */
    public interface ClientEvents {
        void sparkle(CrucibleBlockEntity crucible);

        void boil(CrucibleBlockEntity crucible, int strength);
    }

    public static ClientEvents clientEvents = new ClientEvents() {
        @Override
        public void sparkle(CrucibleBlockEntity crucible) {
        }

        @Override
        public void boil(CrucibleBlockEntity crucible, int strength) {
        }
    };

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            if (this.level != null && this.level.isClientSide()) clientEvents.sparkle(this);
            return true;
        }
        if (id == 2) {
            if (this.level != null && this.level.isClientSide()) clientEvents.boil(this, param);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public boolean boiling() {
        return this.heat > BOILING && this.hasWater();
    }

    public int heat() {
        return this.heat;
    }

    // ------------------------------------------------------------------------------------------------ o que ele guarda

    /**
     * O {@code IAspectContainer} do {@code TileCrucible}: o caldeirão diz o que tem dentro — é assim que os Óculos da
     * Revelação mostram a essência dissolvida —, mas não deixa ninguém pôr nem tirar por cano, como no original.
     */
    @Override
    public AspectList getAspects() {
        return this.aspects;
    }

    @Override
    public boolean doesContainerAccept(net.thaumcraft.api.aspects.Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(net.thaumcraft.api.aspects.Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(net.thaumcraft.api.aspects.Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(net.thaumcraft.api.aspects.Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(net.thaumcraft.api.aspects.Aspect aspect) {
        return 0;
    }

    public AspectList aspects() {
        return this.aspects;
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
        this.heat = input.getShortOr("Heat", (short) 0);
        SingleVariantStorage.readValue(this.tank, FluidVariant.CODEC, FluidVariant::blank, input);
        // o crisol de antes desta versão guardava só "água ou não"
        if (input.getBooleanOr("water", false) && this.tank.amount == 0) {
            this.tank.variant = FluidVariant.of(Fluids.WATER);
            this.tank.amount = FluidConstants.BUCKET;
        }
        this.aspects = input.read("Aspects", AspectList.CODEC).or(() -> input.read("aspects", AspectList.CODEC)).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("Heat", (short) this.heat);
        SingleVariantStorage.writeValue(this.tank, FluidVariant.CODEC, output);
        output.store("Aspects", AspectList.CODEC, this.aspects);
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
