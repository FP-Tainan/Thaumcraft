package net.thaumcraft.entity.golem.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.thaumcraft.entity.GolemBobberEntity;
import net.thaumcraft.entity.GolemEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * O {@code AIFish} da 4.2.3.5: o golem pescador acha água perto de casa, lança a boia e, com a sorte da água em
 * volta e da força dele, puxa um peixe (ou lixo, ou tesouro) que voa até ele. As tabelas são as do original (as da
 * vara de pescar do jogo de então), e a melhoria de fogo já tira o peixe assado.
 */
public class FishGoal extends GolemGoal {
    /** O {@code WeightedRandomFishable}: a coisa, o peso, quanto já vem gasta e se vem encantada. */
    private record Fishable(Supplier<ItemStack> stack, int weight, float damage, boolean enchant) {
        Fishable(Supplier<ItemStack> stack, int weight) {
            this(stack, weight, 0.0f, false);
        }

        ItemStack make(ServerLevel level, RandomSource rand) {
            ItemStack out = this.stack.get();
            if (this.damage > 0.0f && out.isDamageableItem()) {
                int i = (int) (this.damage * out.getMaxDamage());
                int j = out.getMaxDamage() - rand.nextInt(rand.nextInt(Math.max(1, i)) + 1);
                if (j > i) j = i;
                if (j < 1) j = 1;
                out.setDamageValue(j);
            }
            if (this.enchant) {
                out = EnchantmentHelper.enchantItem(rand, out, 30, level.registryAccess(), Optional.empty());
            }
            return out;
        }
    }

    private static final List<Fishable> LOOTCRAP = List.of(
            new Fishable(() -> new ItemStack(Items.LEATHER_BOOTS), 10, 0.9f, false),
            new Fishable(() -> new ItemStack(Items.LEATHER), 10),
            new Fishable(() -> new ItemStack(Items.BONE), 10),
            new Fishable(() -> PotionContents.createItemStack(Items.POTION, Potions.WATER), 10),
            new Fishable(() -> new ItemStack(Items.STRING), 5),
            new Fishable(() -> new ItemStack(Items.FISHING_ROD), 2, 0.9f, false),
            new Fishable(() -> new ItemStack(Items.BOWL), 10),
            new Fishable(() -> new ItemStack(Items.STICK), 5),
            new Fishable(() -> new ItemStack(Items.INK_SAC, 10), 5),
            new Fishable(() -> new ItemStack(Items.TRIPWIRE_HOOK), 10),
            new Fishable(() -> new ItemStack(Items.ROTTEN_FLESH), 10));
    private static final List<Fishable> LOOTRARE = List.of(
            new Fishable(() -> new ItemStack(Items.LILY_PAD), 1),
            new Fishable(() -> new ItemStack(Items.NAME_TAG), 1),
            new Fishable(() -> new ItemStack(Items.SADDLE), 1),
            new Fishable(() -> new ItemStack(Items.BOW), 1, 0.25f, true),
            new Fishable(() -> new ItemStack(Items.FISHING_ROD), 1, 0.25f, true),
            new Fishable(() -> new ItemStack(Items.BOOK), 1, 0.0f, true));
    private static final List<Fishable> LOOTFISH = List.of(
            new Fishable(() -> new ItemStack(Items.COD), 60),
            new Fishable(() -> new ItemStack(Items.SALMON), 25),
            new Fishable(() -> new ItemStack(Items.TROPICAL_FISH), 2),
            new Fishable(() -> new ItemStack(Items.PUFFERFISH), 13));

    private float quality;
    private final float distance;
    private int count;
    private BlockPos target;
    private GolemBobberEntity bobber;

    public FishGoal(GolemEntity golem) {
        super(golem);
        this.distance = Mth.ceil(golem.getRange() / 2.0f);
    }

    private static boolean water(Level level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getBlockState(pos).liquid();
    }

    @Override
    public boolean canUse() {
        if (this.target != null || this.count > 0 || !this.onBeat() || !this.pathDone()) return false;
        Level level = this.golem.level();
        if (this.bobber != null) this.bobber.discard();
        BlockPos vv = this.findWater();
        if (vv == null) return false;
        this.target = vv;
        this.quality = 0.0f;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos n = vv.relative(dir);
            if (water(level, n) && level.isEmptyBlock(n.above())) {
                this.quality += 3.0e-5f;
                if (level.canSeeSky(n.above())) this.quality += 3.0e-5f;
                for (int depth = 1; depth <= 3; depth++) if (water(level, n.below(depth))) this.quality += 1.5e-5f;
            }
        }
        level.playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(), SoundEvents.ARROW_SHOOT, this.golem.getSoundSource(), 0.5f,
                0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        this.bobber = new GolemBobberEntity(level, this.golem, vv.getX(), vv.getY(), vv.getZ());
        return level.addFreshEntity(this.bobber);
    }

    @Override
    public boolean canContinueToUse() {
        return this.bobber != null && !this.bobber.isRemoved() && this.target != null && this.count-- > 0;
    }

    @Override
    public void tick() {
        if (this.target == null || !(this.golem.level() instanceof ServerLevel level)) return;
        this.golem.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 1.0, this.target.getZ() + 0.5, 30.0f, 30.0f);
        float chance = this.quality + this.golem.getGolemStrength() * 1.5e-4f;
        if (level.getRandom().nextFloat() >= chance) return;
        this.golem.startRightArmTimer();
        int qq = 1;
        if (this.golem.getUpgradeAmount(0) > 0 && level.getRandom().nextInt(10) < this.golem.getUpgradeAmount(0)) qq++;
        for (int a = 0; a < qq; a++) {
            ItemStack fs = this.getFishingResult(level);
            if (this.golem.getUpgradeAmount(2) > 0) {
                var recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(fs), level);
                if (recipe.isPresent()) fs = recipe.get().value().assemble(new SingleRecipeInput(fs)).copy();
            }
            ItemEntity entityitem = new ItemEntity(level, this.target.getX() + 0.5, this.target.getY() + 1.0, this.target.getZ() + 0.5, fs);
            if (this.golem.getUpgradeAmount(2) > 0) entityitem.igniteForSeconds(2);
            entityitem.setPickUpDelay(20);
            RandomSource r = level.getRandom();
            double d1 = this.golem.getX() + r.nextFloat() - r.nextFloat() - this.target.getX() + 0.5;
            double d3 = this.golem.getY() - this.target.getY() + 1.0;
            double d5 = this.golem.getZ() + r.nextFloat() - r.nextFloat() - this.target.getZ() + 0.5;
            double d7 = Math.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
            double d9 = 0.1;
            entityitem.setDeltaMovement(d1 * d9, d3 * d9 + Math.sqrt(d7) * 0.08, d5 * d9);
            level.addFreshEntity(entityitem);
        }
        if (this.bobber != null) {
            this.bobber.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.15f, 1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.4f);
            level.sendParticles(ParticleTypes.SPLASH, this.bobber.getX(), this.bobber.getY() + 0.5, this.bobber.getZ(),
                    20 + level.getRandom().nextInt(20), 0.1, 0.0, 0.1, 0.0);
            this.bobber.discard();
        }
        this.target = null;
    }

    @Override
    public void stop() {
        if (this.bobber != null) this.bobber.discard();
        this.target = null;
        this.count = -1;
    }

    @Override
    public void start() {
        this.count = 300 + this.golem.level().getRandom().nextInt(200);
        this.golem.startRightArmTimer();
    }

    private BlockPos findWater() {
        var rand = this.golem.getRandom();
        BlockPos home = this.golem.home();
        Level level = this.golem.level();
        for (int i = 0; i < this.distance * 2.0f; i++) {
            int x = (int) (home.getX() + rand.nextInt((int) (1.0f + this.distance * 2.0f)) - this.distance);
            int y = (int) (home.getY() + rand.nextInt((int) (1.0f + this.distance)) - this.distance / 2.0f);
            int z = (int) (home.getZ() + rand.nextInt((int) (1.0f + this.distance * 2.0f)) - this.distance);
            BlockPos p = new BlockPos(x, y, z);
            if (water(level, p) && level.isEmptyBlock(p.above())) return p;
        }
        return null;
    }

    private ItemStack getFishingResult(ServerLevel level) {
        RandomSource rand = level.getRandom();
        float f = rand.nextFloat();
        float f1 = 0.1f - this.golem.getUpgradeAmount(5) * 0.025f;
        float f2 = 0.05f + this.golem.getUpgradeAmount(4) * 0.0125f;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos n = this.target.relative(dir);
            if (water(level, n) && level.isEmptyBlock(n.above())) {
                f1 -= 0.005f;
                f2 += 0.00125f;
                if (level.canSeeSky(n.above())) {
                    f1 -= 0.005f;
                    f2 += 0.00125f;
                }
                for (int depth = 1; depth <= 3; depth++) if (water(level, n.below(depth))) f2 += 0.001f;
            }
        }
        f1 = Mth.clamp(f1, 0.0f, 1.0f);
        f2 = Mth.clamp(f2, 0.0f, 1.0f);
        if (f < f1) return pick(LOOTCRAP, rand).make(level, rand);
        f -= f1;
        if (f < f2) return pick(LOOTRARE, rand).make(level, rand);
        return pick(LOOTFISH, rand).make(level, rand);
    }

    private static Fishable pick(List<Fishable> list, RandomSource rand) {
        int total = 0;
        for (Fishable f : list) total += f.weight();
        int r = rand.nextInt(total);
        for (Fishable f : list) {
            r -= f.weight();
            if (r < 0) return f;
        }
        return list.getLast();
    }
}
