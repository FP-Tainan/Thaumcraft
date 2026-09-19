package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.visnet.VisNet;
import net.thaumcraft.block.BellowsBlock;
import net.thaumcraft.block.InfernalFurnaceBlock;
import net.thaumcraft.crafting.SmeltingBonus;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * O {@code TileArcaneFurnace} da 4.2.3.5: o coração da fornalha infernal. Guarda até 32 pilhas do que caiu na lava e
 * funde uma unidade por vez — 140 tiques, 80 com a fornalha acelerada (Ignis da rede de vis, um pouco, ou pelos bicos,
 * muito), 20 a menos por fole virado para ela. O que sai, sai pela boca, com a experiência e às vezes um bônus.
 */
public class InfernalFurnaceBlockEntity extends BlockEntity {
    private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(32, ItemStack.EMPTY);
    public int furnaceCookTime;
    public int furnaceMaxCookTime;
    public int speedyTime;
    public int facingX = -5;
    public int facingZ = -5;

    public InfernalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFERNAL_FURNACE, pos, state);
    }

    public int getSizeInventory() {
        return this.furnaceItemStacks.size();
    }

    public ItemStack getStackInSlot(int i) {
        return this.furnaceItemStacks.get(i);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceBlockEntity furnace) {
        furnace.tick((ServerLevel) level);
    }

    private void tick(ServerLevel level) {
        if (this.facingX == -5) this.getFacing();
        boolean cookedflag = false;
        if (this.furnaceCookTime > 0) {
            this.furnaceCookTime--;
            cookedflag = true;
        }
        if (cookedflag && this.speedyTime > 0) this.speedyTime--;
        if (this.speedyTime <= 0) this.speedyTime = VisNet.drainVis(level, this.worldPosition, Aspects.FIRE, 5);
        if (this.furnaceMaxCookTime == 0) this.furnaceMaxCookTime = this.calcCookTime();
        if (this.furnaceCookTime > this.furnaceMaxCookTime) this.furnaceCookTime = this.furnaceMaxCookTime;
        if (this.furnaceCookTime == 0 && cookedflag) {
            for (int a = 0; a < this.getSizeInventory(); a++) {
                ItemStack stack = this.furnaceItemStacks.get(a);
                if (stack.isEmpty()) continue;
                Optional<RecipeHolder<SmeltingRecipe>> recipe = recipe(level, stack);
                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(stack));
                    this.ejectItem(level, result.copy(), stack, recipe.get().value().experience());
                    level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 3, 0);
                    stack.shrink(1);
                    if (stack.isEmpty()) this.furnaceItemStacks.set(a, ItemStack.EMPTY);
                    this.setChanged();
                    break;
                }
            }
        }
        if (this.furnaceCookTime == 0 && !cookedflag) {
            for (int a = 0; a < this.getSizeInventory(); a++) {
                if (!this.furnaceItemStacks.get(a).isEmpty() && this.canSmelt(a)) {
                    this.furnaceMaxCookTime = this.calcCookTime();
                    this.furnaceCookTime = this.furnaceMaxCookTime;
                    break;
                }
            }
        }
    }

    private static Optional<RecipeHolder<SmeltingRecipe>> recipe(ServerLevel level, ItemStack stack) {
        return level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
    }

    /** O {@code getBellows}: foles a dois blocos do centro (menos em cima), virados para ele e sem sinal; no máximo três. */
    private int getBellows() {
        int bellows = 0;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos at = this.worldPosition.relative(dir, 2);
            BlockState state = this.level.getBlockState(at);
            if (this.level.getBlockEntity(at) instanceof BellowsBlockEntity && state.getBlock() instanceof BellowsBlock
                    && state.getValue(BellowsBlock.FACING) == dir.getOpposite() && !this.level.hasNeighborSignal(at)) {
                bellows++;
            }
        }
        return Math.min(3, bellows);
    }

    private int calcCookTime() {
        return (this.speedyTime > 0 ? 80 : 140) - 20 * this.getBellows();
    }

    /** O {@code addItemsToInventory}: junta numa pilha igual ou ocupa uma casa vazia; o que não funde se desfaz. */
    public boolean addItemsToInventory(ItemStack items) {
        for (int a = 0; a < this.getSizeInventory(); a++) {
            ItemStack here = this.furnaceItemStacks.get(a);
            if (!here.isEmpty() && ItemStack.isSameItemSameComponents(here, items) && here.getCount() + items.getCount() <= items.getMaxStackSize()) {
                here.grow(items.getCount());
                if (!this.canSmelt(a)) this.destroyItem(a);
                this.setChanged();
                return true;
            }
            if (here.isEmpty()) {
                this.furnaceItemStacks.set(a, items.getCount() > 64 ? items.copyWithCount(64) : items);
                if (!this.canSmelt(a)) this.destroyItem(a);
                this.setChanged();
                return true;
            }
        }
        return false;
    }

    /** O {@code destroyItem}: o chiado e uma gota de lava. */
    private void destroyItem(int slot) {
        this.furnaceItemStacks.set(slot, ItemStack.EMPTY);
        var random = this.level.getRandom();
        this.level.playSound(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f);
        if (this.level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LAVA, this.worldPosition.getX() + (double) random.nextFloat(), this.worldPosition.getY() + 1.0,
                    this.worldPosition.getZ() + (double) random.nextFloat(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** O {@code getFacing}: de que lado está a boca. */
    private void getFacing() {
        this.facingX = 0;
        this.facingZ = 0;
        if (mouth(this.worldPosition.west())) {
            this.facingX = -1;
        } else if (mouth(this.worldPosition.east())) {
            this.facingX = 1;
        } else if (mouth(this.worldPosition.north())) {
            this.facingZ = -1;
        } else {
            this.facingZ = 1;
        }
    }

    private boolean mouth(BlockPos at) {
        return InfernalFurnaceBlock.part(this.level.getBlockState(at)) == 10;
    }

    /** O {@code ejectItem}: o fundido sai pela boca, e junto o bônus (se houver) e a experiência. */
    public void ejectItem(ServerLevel level, ItemStack items, ItemStack furnaceItemStack, float experience) {
        if (items.isEmpty()) return;
        var random = level.getRandom();
        int bellows = this.getBellows();
        float lx = 0.5f + this.facingX * 1.2f;
        float lz = 0.5f + this.facingZ * 1.2f;
        // em double: somado a um inteiro grande, o float do original perde a casa decimal
        double x = this.worldPosition.getX() + (double) lx, y = this.worldPosition.getY() + 0.4, z = this.worldPosition.getZ() + (double) lz;
        this.spawn(level, new ItemEntity(level, x, y, z, items), 0.03f);
        Item bonusItem = SmeltingBonus.of(furnaceItemStack);
        if (bonusItem != null) {
            int count = 0;
            if (bellows == 0) {
                if (random.nextInt(4) == 0) count++;
            } else {
                for (int a = 0; a < bellows; a++) {
                    if (random.nextFloat() < 0.44f) count++;
                }
            }
            if (count > 0) this.spawn(level, new ItemEntity(level, x, y, z, new ItemStack(bonusItem, count)), 0.03f);
        }
        int xp = items.getCount();
        if (experience == 0.0f) {
            xp = 0;
        } else if (experience < 1.0f) {
            int whole = Mth.floor(xp * experience);
            if (whole < Mth.ceil(xp * experience) && (float) Math.random() < xp * experience - whole) whole++;
            xp = whole;
        }
        while (xp > 0) {
            int part = ExperienceOrb.getExperienceValue(xp);
            xp -= part;
            this.spawn(level, new ExperienceOrb(level, x, y, z, part), 0.025f);
        }
    }

    /** Empurra para fora pela boca (ou treme de lado, no eixo que não é o dela), sem subir. */
    private void spawn(ServerLevel level, net.minecraft.world.entity.Entity entity, float wobble) {
        var random = level.getRandom();
        float mx = this.facingX == 0 ? (random.nextFloat() - random.nextFloat()) * wobble : this.facingX * 0.13f;
        float mz = this.facingZ == 0 ? (random.nextFloat() - random.nextFloat()) * wobble : this.facingZ * 0.13f;
        entity.setDeltaMovement(mx, 0.0, mz);
        level.addFreshEntity(entity);
    }

    private boolean canSmelt(int slot) {
        ItemStack stack = this.furnaceItemStacks.get(slot);
        return !stack.isEmpty() && this.level instanceof ServerLevel server && recipe(server, stack).isPresent();
    }

    /** O evento 3: o que sai da boca espirra lava e borbulha. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != 3) return super.triggerEvent(id, param);
        if (this.level != null && this.level.isClientSide()) {
            if (this.facingX == -5) this.getFacing();
            var random = this.level.getRandom();
            for (int a = 0; a < 5; a++) {
                clientEffects.lava(this.level, this.worldPosition, this.facingX, this.facingZ);
                this.level.playLocalSound(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                        SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1f + random.nextFloat() * 0.1f, 0.9f + random.nextFloat() * 0.15f, false);
            }
        }
        return true;
    }

    /** O {@code furnaceLavaFx}, do lado de quem vê. */
    public interface ClientEffects {
        void lava(Level level, BlockPos pos, int facingX, int facingZ);
    }

    public static ClientEffects clientEffects = (level, pos, fx, fz) -> {
    };

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.furnaceItemStacks = NonNullList.withSize(32, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.furnaceItemStacks);
        this.furnaceCookTime = input.getIntOr("CookTime", 0);
        this.speedyTime = input.getIntOr("SpeedyTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("CookTime", (short) this.furnaceCookTime);
        output.putInt("SpeedyTime", (short) this.speedyTime);
        ContainerHelper.saveAllItems(output, this.furnaceItemStacks);
    }

    @Nullable
    public ItemStack firstStack() {
        for (ItemStack s : this.furnaceItemStacks) if (!s.isEmpty()) return s;
        return null;
    }
}
