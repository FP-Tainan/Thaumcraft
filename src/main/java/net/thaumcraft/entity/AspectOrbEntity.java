package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O orbe de aspecto: o {@code EntityAspectOrb} da 4.2.3.5. Uma bolinha de vis primordial que salta do nó instável,
 * do monstro morto e do amuleto primordial, cai e quica, e voa para quem estiver a até oito blocos com uma varinha
 * na barra que ainda tenha lugar para aquele aspecto. Encostando, enche a varinha e some; sozinha, some em 150 tiques.
 */
public class AspectOrbEntity extends Entity {
    private static final EntityDataAccessor<String> ASPECT = SynchedEntityData.defineId(AspectOrbEntity.class, EntityDataSerializers.STRING);
    public static final int MAX_AGE = 150;

    public int orbAge;
    private int orbHealth = 5;
    private int aspectValue;
    @Nullable
    private Player closestPlayer;

    public AspectOrbEntity(EntityType<? extends AspectOrbEntity> type, Level level) {
        super(type, level);
    }

    public AspectOrbEntity(Level level, double x, double y, double z, Aspect aspect, int value) {
        this(TCEntities.ASPECT_ORB, level);
        this.setPos(x, y, z);
        this.setYRot((float) (Math.random() * 360.0));
        this.setDeltaMovement((Math.random() * 0.2 - 0.1) * 2.0, Math.random() * 0.2 * 2.0, (Math.random() * 0.2 - 0.1) * 2.0);
        this.aspectValue = value;
        this.setAspect(aspect);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ASPECT, "");
    }

    @Nullable
    public Aspect aspect() {
        return Aspect.of(this.entityData.get(ASPECT));
    }

    public void setAspect(Aspect aspect) {
        this.entityData.set(ASPECT, aspect == null ? "" : aspect.tag());
    }

    public int value() {
        return this.aspectValue;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement().add(0.0, -0.03, 0.0);
        FluidState fluid = this.level().getFluidState(this.blockPosition());
        if (fluid.is(net.minecraft.tags.FluidTags.LAVA)) {
            motion = new Vec3((this.random.nextFloat() - this.random.nextFloat()) * 0.2, 0.2, (this.random.nextFloat() - this.random.nextFloat()) * 0.2);
            this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.4f, 2.0f + this.random.nextFloat() * 0.4f);
        }
        this.setDeltaMovement(motion);
        this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());

        double range = 8.0;
        if (this.tickCount % 5 == 0 && this.closestPlayer == null) {
            List<Player> targets = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(range));
            double best = Double.MAX_VALUE;
            for (Player player : targets) {
                double d = player.distanceToSqr(this);
                if (d < best && wandSlotWithRoom(player) >= 0) {
                    best = d;
                    this.closestPlayer = player;
                }
            }
        }
        if (this.closestPlayer != null) {
            double dx = (this.closestPlayer.getX() - this.getX()) / range;
            double dy = (this.closestPlayer.getEyeY() - this.getY()) / range;
            double dz = (this.closestPlayer.getZ() - this.getZ()) / range;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double pull = 1.0 - d;
            if (pull > 0.0) {
                pull *= pull;
                this.setDeltaMovement(this.getDeltaMovement().add(dx / d * pull * 0.1, dy / d * pull * 0.1, dz / d * pull * 0.1));
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = 0.98f;
        if (this.onGround()) {
            BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1.0, this.getZ());
            friction = this.level().getBlockState(below).isAir() ? 0.58800006f : this.level().getBlockState(below).getBlock().getFriction() * 0.98f;
        }
        motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x * friction, motion.y * 0.98, motion.z * friction);
        if (this.onGround()) this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));

        if (++this.orbAge >= MAX_AGE) this.discard();
    }

    /** O {@code isWandInHotbarWithRoom}: a primeira varinha da barra onde cabe ao menos um ponto daquele aspecto. */
    private int wandSlotWithRoom(Player player) {
        Aspect aspect = this.aspect();
        if (aspect == null) return -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof WandItem && WandItem.maxVis(stack) - WandItem.vis(stack).getAmount(aspect) >= WandItem.VIS_UNIT) {
                return slot;
            }
        }
        return -1;
    }

    /** O {@code onCollideWithPlayer}: enche a varinha e some. */
    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide()) return;
        Aspect aspect = this.aspect();
        int slot = wandSlotWithRoom(player);
        if (aspect != null && aspect.isPrimal() && slot >= 0 && player.takeXpDelay == 0) {
            WandItem.addVis(player.getInventory().getItem(slot), aspect, this.aspectValue);
            player.takeXpDelay = 2;
            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1f, 0.5f * ((this.random.nextFloat() - this.random.nextFloat()) * 0.7f + 1.8f));
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableToBase(source)) return false;
        this.markHurt();
        this.orbHealth = (int) (this.orbHealth - amount);
        if (this.orbHealth <= 0) this.discard();
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putShort("Health", (short) this.orbHealth);
        output.putShort("Age", (short) this.orbAge);
        output.putShort("Value", (short) this.aspectValue);
        output.putString("Aspect", this.entityData.get(ASPECT));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.orbHealth = input.getShortOr("Health", (short) 5) & 255;
        this.orbAge = input.getShortOr("Age", (short) 0);
        this.aspectValue = input.getShortOr("Value", (short) 1);
        this.entityData.set(ASPECT, input.getStringOr("Aspect", ""));
    }

    /** Que fração da vida já passou: o desenho encolhe com ela. */
    public float life(float partial) {
        return Mth.clamp((this.orbAge + partial) / MAX_AGE, 0.0f, 1.0f);
    }
}
