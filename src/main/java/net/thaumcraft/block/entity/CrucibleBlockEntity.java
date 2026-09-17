package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.registry.TCBlockEntities;

import java.util.List;

/**
 * O crisol: um caldeirão de ferro em que as coisas se desfazem no que elas são.
 *
 * <p>As contas são as da 4.2.3.5. Ele esquenta enquanto houver fogo por baixo — um grau por tique até
 * duzentos — e só ferve passando de cento e cinquenta; abaixo disso nada acontece. Fervendo, o que se joga
 * dentro se desfaz nos aspectos que tem e some na água; se o que está dentro casar com uma receita e o
 * jogado for o catalisador dela, sai a coisa nova e os aspectos gastos vão embora.
 */
public class CrucibleBlockEntity extends BlockEntity {
    /** O quanto ele chega a esquentar. */
    public static final int MAX_HEAT = 200;
    /** Abaixo disto a água não ferve e nada se desfaz. */
    public static final int BOILING = 150;

    private int heat;
    private boolean water;
    private AspectList aspects = new AspectList();

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CRUCIBLE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (level.isClientSide()) return;
        boolean fire = isFireBelow(level, pos);
        int before = crucible.heat;
        if (fire && crucible.water) {
            if (crucible.heat < MAX_HEAT) crucible.heat++;
        } else if (crucible.heat > 0) {
            crucible.heat--;
        }
        if (before <= BOILING && crucible.heat > BOILING) {
            level.playSound(null, pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.4f, 1.2f);
        }
        if (crucible.heat != before && (crucible.heat % 10 == 0 || crucible.boiling() != before > BOILING)) {
            crucible.sync();
        }
        if (!crucible.boiling()) return;

        // o que cair dentro enquanto ferve se desfaz
        List<ItemEntity> inside = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(pos).inflate(-0.1, 0.0, -0.1).expandTowards(0.0, 0.4, 0.0));
        for (ItemEntity item : inside) crucible.swallow(item, level);
    }

    /** Há chama por baixo? No original basta fogo ou lava; aqui vale qualquer coisa que queime. */
    private static boolean isFireBelow(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(net.minecraft.world.level.block.Blocks.FIRE)
                || below.is(net.minecraft.world.level.block.Blocks.SOUL_FIRE)
                || below.is(net.minecraft.world.level.block.Blocks.LAVA)
                || below.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
                || below.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)
                        && below.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)
                || below.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)
                        && below.getValue(net.minecraft.world.level.block.CampfireBlock.LIT);
    }

    /** Engole uma coisa que caiu dentro: ou ela vira receita, ou ela vira aspecto. */
    private void swallow(ItemEntity entity, Level level) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) return;

        CrucibleRecipe recipe = CrucibleRecipes.find(this.aspects, stack);
        // sem a pesquisa, a mistura não fecha: o que cai dentro só se desfaz em aspectos
        if (recipe != null && !net.thaumcraft.research.ResearchManager.knows(thrower(entity, level),
                recipe.research())) {
            recipe = null;
        }
        if (recipe != null) {
            this.aspects = recipe.removeFrom(this.aspects);
            stack.shrink(1);
            if (stack.isEmpty()) entity.discard();
            else entity.setItem(stack);
            ItemEntity result = new ItemEntity(level, entity.getX(), entity.getY() + 0.25, entity.getZ(),
                    recipe.result().copy());
            result.setDeltaMovement(0.0, 0.25, 0.0);
            level.addFreshEntity(result);
            level.playSound(null, this.getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6f, 1.0f);
            this.sync();
            return;
        }

        AspectList found = ObjectAspects.of(stack);
        if (found.isEmpty()) {
            // o que não é feito de nada o crisol cospe de volta
            entity.setDeltaMovement((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2,
                    0.35, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2);
            level.playSound(null, this.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 0.8f);
            return;
        }
        for (Aspect aspect : found.getAspects()) this.aspects.add(aspect, found.getAmount(aspect));
        stack.shrink(1);
        if (stack.isEmpty()) entity.discard();
        else entity.setItem(stack);
        level.playSound(null, this.getBlockPos(), SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.3f,
                1.0f + level.getRandom().nextFloat() * 0.4f);
        this.sync();
    }

    /** Quem jogou aquilo dentro, se ainda dá para saber. */
    private static net.minecraft.world.entity.player.Player thrower(ItemEntity entity, Level level) {
        var owner = entity.getOwner();
        return owner instanceof net.minecraft.world.entity.player.Player player ? player : null;
    }

    public boolean boiling() {
        return this.heat > BOILING && this.water;
    }

    public int heat() {
        return this.heat;
    }

    public boolean hasWater() {
        return this.water;
    }

    public void setWater(boolean full) {
        this.water = full;
        this.sync();
    }

    public AspectList aspects() {
        return this.aspects;
    }

    /** A cor da água, que é a mistura do que está dissolvido nela. */
    public int brew() {
        if (this.aspects.isEmpty()) return 0x3F76E4;
        long red = 0;
        long green = 0;
        long blue = 0;
        int total = 0;
        for (Aspect aspect : this.aspects.getAspects()) {
            int amount = this.aspects.getAmount(aspect);
            red += (aspect.color() >> 16 & 255) * amount;
            green += (aspect.color() >> 8 & 255) * amount;
            blue += (aspect.color() & 255) * amount;
            total += amount;
        }
        if (total == 0) return 0x3F76E4;
        return (int) (red / total) << 16 | (int) (green / total) << 8 | (int) (blue / total);
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
        this.heat = input.getIntOr("heat", 0);
        this.water = input.getBooleanOr("water", false);
        this.aspects = input.read("aspects", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("heat", this.heat);
        output.putBoolean("water", this.water);
        output.store("aspects", AspectList.CODEC, this.aspects);
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
