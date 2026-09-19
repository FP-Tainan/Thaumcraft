package net.thaumcraft.entity.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.world.BiomePainter;
import net.thaumcraft.world.TCBiomes;

import java.util.EnumSet;

/**
 * A ovelha maculada: o {@code EntityTaintSheep} da 4.2.3.5. 20 de vida, 3 de dano, 1 de armadura (0,25). Em vez de
 * comer o capim, ela o macula (o {@code AIConvertGrass}: uma vez em 250, o capim ou a grama embaixo vira fibra e a
 * coluna vira Terra Maculada). Tosquiada, dá de uma a três lãs roxas.
 */
public class TaintSheepEntity extends TaintedMonster implements Shearable {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(TaintSheepEntity.class, EntityDataSerializers.BYTE);
    private int sheepTimer;
    private ConvertGrassGoal convert;

    public TaintSheepEntity(EntityType<? extends TaintSheepEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    public static AttributeSupplier.Builder attributes() {
        return TaintedMonster.attributes(20.0, 3.0, 0.25).add(Attributes.ARMOR, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        this.convert = new ConvertGrassGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, this.convert);
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.sheepTimer = this.convert.timer;
        super.customServerAiStep(level);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) this.sheepTimer = Math.max(0, this.sheepTimer - 1);
        super.aiStep();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) this.sheepTimer = 40;
        else super.handleEntityEvent(id);
    }

    /** O quanto a cabeça desceu para pastar (o {@code func_44003_c}). */
    public float headEatPositionScale(float partial) {
        if (this.sheepTimer <= 0) return 0.0f;
        if (this.sheepTimer >= 4 && this.sheepTimer <= 36) return 1.0f;
        return this.sheepTimer < 4 ? (this.sheepTimer - partial) / 4.0f : -(this.sheepTimer - 40 - partial) / 4.0f;
    }

    /** O giro da cabeça pastando (o {@code func_44002_d}). */
    public float headEatAngleScale(float partial) {
        if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
            float f = (this.sheepTimer - 4 - partial) / 32.0f;
            return (float) (Math.PI / 5) + 0.2199115f * Mth.sin(f * 28.7f);
        }
        return this.sheepTimer > 0 ? (float) (Math.PI / 5) : this.getXRot() * Mth.DEG_TO_RAD;
    }

    public boolean isSheared() {
        return (this.entityData.get(FLAGS) & 16) != 0;
    }

    public void setSheared(boolean sheared) {
        byte b = this.entityData.get(FLAGS);
        this.entityData.set(FLAGS, (byte) (sheared ? b | 16 : b & -17));
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared();
    }

    /** O {@code onSheared}: de uma a três lãs roxas (a lã 10 de então). */
    @Override
    public void shear(ServerLevel level, SoundSource source, ItemStack tool) {
        level.playSound(null, this, net.minecraft.sounds.SoundEvents.SHEEP_SHEAR, source, 1.0f, 1.0f);
        this.setSheared(true);
        int count = 1 + this.random.nextInt(3);
        for (int j = 0; j < count; j++) {
            ItemEntity item = this.spawnAtLocation(level, new ItemStack(Items.WOOL.purple()), 1.0f);
            if (item != null) {
                item.setDeltaMovement(item.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1f,
                        this.random.nextFloat() * 0.05f, (this.random.nextFloat() - this.random.nextFloat()) * 0.1f));
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.SHEARS)) {
            if (this.level() instanceof ServerLevel server && this.readyForShearing()) {
                this.shear(server, SoundSource.PLAYERS, held);
                this.gameEvent(GameEvent.SHEAR, player);
                held.hurtAndBreak(1, player, hand.asEquipmentSlot());
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Sheared", this.isSheared());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSheared(input.getBooleanOr("Sheared", false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return net.minecraft.sounds.SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return net.minecraft.sounds.SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.minecraft.sounds.SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(net.minecraft.sounds.SoundEvents.SHEEP_STEP, 0.15f, 1.0f);
    }

    /** O {@code AIConvertGrass}: o pastar que macula. */
    static class ConvertGrassGoal extends Goal {
        private final TaintSheepEntity sheep;
        int timer;

        ConvertGrassGoal(TaintSheepEntity sheep) {
            this.sheep = sheep;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.sheep.getRandom().nextInt(250) != 0) return false;
            BlockPos pos = this.sheep.blockPosition();
            Level level = this.sheep.level();
            return level.getBlockState(pos).is(Blocks.SHORT_GRASS) || level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
        }

        @Override
        public void start() {
            this.timer = 40;
            this.sheep.level().broadcastEntityEvent(this.sheep, (byte) 10);
            this.sheep.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.timer = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.timer > 0;
        }

        @Override
        public void tick() {
            this.timer = Math.max(0, this.timer - 1);
            if (this.timer != 4 || !(this.sheep.level() instanceof ServerLevel level)) return;
            BlockPos pos = this.sheep.blockPosition();
            if (level.getBlockState(pos).is(Blocks.SHORT_GRASS)) {
                level.levelEvent(2001, pos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.removeBlock(pos, false);
                level.setBlockAndUpdate(pos, TCBlocks.TAINT_FIBRES.defaultBlockState());
                BiomePainter.paint(level, pos, TCBiomes.TAINTED_LAND);
            } else if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                level.levelEvent(2001, pos.below(), Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.setBlockAndUpdate(pos, TCBlocks.TAINT_FIBRES.defaultBlockState());
                BiomePainter.paint(level, pos, TCBiomes.TAINTED_LAND);
            }
        }
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (level.getRandom().nextInt(3) == 0) TaintDrops.either(level, this);
    }
}
