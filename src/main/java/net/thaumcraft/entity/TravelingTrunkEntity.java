package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.entity.golem.InventoryUtils;
import net.thaumcraft.item.GolemUpgradeItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O baú itinerante: o {@code EntityTravelingTrunk} da 4.2.3.5 — um baú que pula atrás do dono, se teleporta para
 * perto dele quando fica para trás (e o segue até por outros mundos), se remenda devagar e come comida para se remendar
 * depressa. Aceita uma melhoria: ar (pula mais depressa), terra (uma fileira a mais), fogo (morde quem atacar o dono),
 * água (só o dono abre e recolhe; nada o fere), ordem (guarda o que tem quando o sino o recolhe) e entropia (suga o
 * que estiver no chão perto dele).
 */
public class TravelingTrunkEntity extends Mob {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAY = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> UPGRADE = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> ROWS = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.INT);
    /** O {@code linkedEntities} do {@code EventHandlerEntity}: os baús de cada dono, para irem com ele de um mundo a outro. */
    public static final Map<String, List<WeakReference<TravelingTrunkEntity>>> LINKED = new HashMap<>();

    public final Inventory inventory = new Inventory(this);
    public float lidrot;
    private int jumpDelay;
    private int eatDelay;
    private float flyingSpeed = 0.02f;
    /** O {@code attackTime} do jogo de então: a espera entre duas mordidas. */
    private int biteTime;

    /** O {@code InventoryTrunk}: trinta e seis casas, das quais a tela mostra 27 (ou 36, com a terra). */
    public static class Inventory implements Container {
        private final TravelingTrunkEntity trunk;
        private final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);

        Inventory(TravelingTrunkEntity trunk) {
            this.trunk = trunk;
        }

        @Override
        public int getContainerSize() {
            return this.trunk.getRows() * 9;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack s : this.items) if (!s.isEmpty()) return false;
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < 36 ? this.items.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack in = this.getItem(slot);
            if (in.isEmpty()) return ItemStack.EMPTY;
            ItemStack out = in.split(amount);
            if (in.isEmpty()) this.items.set(slot, ItemStack.EMPTY);
            return out;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack in = this.getItem(slot);
            this.items.set(slot, ItemStack.EMPTY);
            return in;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < 36) this.items.set(slot, stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int a = 0; a < 36; a++) this.items.set(a, ItemStack.EMPTY);
        }

        @Override
        public void startOpen(net.minecraft.world.entity.ContainerUser user) {
            this.trunk.setOpen(true);
        }

        @Override
        public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
            this.trunk.setOpen(false);
        }

        public void dropAll() {
            if (!(this.trunk.level() instanceof ServerLevel server)) return;
            for (int a = 0; a < 36; a++) {
                if (!this.items.get(a).isEmpty()) {
                    this.trunk.spawnAtLocation(server, this.items.get(a), 0.0f);
                    this.items.set(a, ItemStack.EMPTY);
                }
            }
        }

        public List<ItemStack> all() {
            return this.items;
        }
    }

    public TravelingTrunkEntity(EntityType<? extends TravelingTrunkEntity> type, Level level) {
        super(type, level);
        this.jumpDelay = this.random.nextInt(20) + 10;
        this.setPersistenceRequired();
        // ele se move sozinho, pulando: os controles de passo e pulo do jogo não podem desfazer isso
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
            }
        };
        this.jumpControl = new JumpControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OPEN, false);
        builder.define(STAY, false);
        builder.define(OWNER, "");
        builder.define(UPGRADE, (byte) -1);
        builder.define(ROWS, (byte) 3);
        builder.define(ANGER, 0);
    }

    // ---- os dados

    public int getUpgrade() {
        return this.entityData.get(UPGRADE);
    }

    public void setUpgrade(int upgrade) {
        this.entityData.set(UPGRADE, (byte) upgrade);
    }

    public int getRows() {
        return this.entityData.get(ROWS);
    }

    public void setInvSize() {
        this.entityData.set(ROWS, (byte) (this.getUpgrade() == 1 ? 4 : 3));
    }

    public int getAnger() {
        return this.entityData.get(ANGER);
    }

    public void setAnger(int anger) {
        this.entityData.set(ANGER, anger);
    }

    public boolean isOpen() {
        return this.entityData.get(OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(OPEN, open);
    }

    public boolean getStay() {
        return this.entityData.get(STAY);
    }

    public void setStay(boolean stay) {
        this.entityData.set(STAY, stay);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER);
    }

    public void setOwner(String name) {
        this.entityData.set(OWNER, name);
    }

    @Nullable
    public Player getOwnerEntity() {
        String name = this.getOwnerName();
        if (name.isEmpty()) return null;
        // o getPlayerEntityByName de então; havendo homônimos (só nos testes), o mais perto
        Player best = null;
        for (Player p : this.level().players()) {
            if (p.getName().getString().equals(name) && (best == null || p.distanceToSqr(this) < best.distanceToSqr(this))) best = p;
        }
        return best;
    }

    // ---- a vida

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.CACTUS)) return false;
        return this.getUpgrade() != 3 && super.hurtServer(level, source, amount);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getUpgrade() == 5) this.pullItems();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.033f, 0.0));
        if (this.level().isClientSide()) {
            if (!this.onGround() && this.getDeltaMovement().y < 0.0 && !this.isInWater()) this.lidrot += 0.015f;
            if ((this.onGround() || this.isInWater()) && !this.isOpen()) {
                this.lidrot -= 0.1f;
                if (this.lidrot < 0.0f) this.lidrot = 0.0f;
            }
            if (this.isOpen()) this.lidrot += 0.035f;
            float max = this.isOpen() ? 0.5f : 0.2f;
            if (this.lidrot > max) this.lidrot = max;
        } else if (this.getHealth() < this.getMaxHealth() && (this.getUpgrade() == 3 || this.tickCount % 50 == 0)) {
            this.heal(1.0f);
        }
    }

    @Override
    protected float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    /** O {@code updateEntityActionState}: segue o dono aos pulos, teleporta-se para perto dele e morde quem o atacar. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.biteTime > 0) this.biteTime--;
        if (this.getAnger() > 0) this.setAnger(this.getAnger() - 1);
        if (this.eatDelay > 0) this.eatDelay--;
        this.fallDistance = 0.0;
        Player owner = this.getOwnerEntity();
        if (owner == null) return;
        List<WeakReference<TravelingTrunkEntity>> ll = LINKED.computeIfAbsent(owner.getName().getString(), k -> new ArrayList<>());
        boolean add = true;
        for (WeakReference<TravelingTrunkEntity> trunk : ll) {
            if (trunk.get() != null && trunk.get().getId() == this.getId()) {
                add = false;
                break;
            }
        }
        if (add) ll.add(new WeakReference<>(this));
        if (!this.getStay() && this.distanceTo(owner) > 20.0f) {
            int i = net.minecraft.util.Mth.floor(owner.getX()) - 2;
            int j = net.minecraft.util.Mth.floor(owner.getZ()) - 2;
            int k = net.minecraft.util.Mth.floor(owner.getBoundingBox().minY);
            for (int l = 0; l <= 4; l++) {
                for (int i1 = 0; i1 <= 4; i1++) {
                    if (l >= 1 && i1 >= 1 && l <= 3 && i1 <= 3) continue;
                    BlockPos below = new BlockPos(i + l, k - 1, j + i1);
                    BlockPos at = below.above();
                    boolean ground = level.getBlockState(below).isRedstoneConductor(level, below)
                            || level.getFluidState(below).is(net.minecraft.tags.FluidTags.WATER);
                    if (ground && !level.getBlockState(at).isRedstoneConductor(level, at)
                            && !level.getBlockState(at.above()).isRedstoneConductor(level, at.above())) {
                        level.playSound(null, i + l + 0.5, k, j + i1 + 0.5, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 0.5f, 1.0f);
                        this.snapTo(i + l + 0.5, k, j + i1 + 0.5, this.getYRot(), this.getXRot());
                        this.setTarget(null);
                        return;
                    }
                }
            }
        }
        if (this.getTarget() != null && !this.getTarget().isAlive()) {
            this.setTarget(null);
            this.setAnger(5);
        }
        LivingEntity attacker = owner.getLastHurtByMob();
        if (!this.getStay() && this.getUpgrade() == 2 && this.getAnger() == 0 && this.getTarget() == null && attacker != null
                && attacker.isAlive() && this.hasLineOfSight(attacker)) {
            this.setAnger(600);
            this.setTarget(attacker);
        }
        boolean move = false;
        LivingEntity target = this.getTarget();
        if (this.getAnger() > 0 && target != null && target.isAlive() && target != owner) {
            this.lookAt(target, 10.0f, 20.0f);
            move = true;
            if (this.biteTime <= 0 && this.distanceTo(target) < 1.5 && target.getBoundingBox().maxY > this.getBoundingBox().minY
                    && target.getBoundingBox().minY < this.getBoundingBox().maxY) {
                this.biteTime = 10 + this.random.nextInt(5);
                target.hurtServer(level, this.damageSources().mobAttack(this), 4.0f);
                level.broadcastEntityEvent(this, (byte) 17);
                this.playSound(SoundEvents.BLAZE_HURT, 0.5f, this.random.nextFloat() * 0.1f + 0.9f);
            }
        }
        if (this.distanceTo(owner) > 5.0f && this.getAnger() == 0 && !this.getStay()) {
            this.lookAt(owner, 10.0f, 20.0f);
            move = true;
        }
        if ((this.onGround() || this.isInWater()) && this.jumpDelay-- <= 0 && move) {
            boolean fast = this.getUpgrade() == 0;
            this.jumpDelay = (this.random.nextInt(10) + 5) / 3;
            this.setJumping(true);
            this.xxa = 1.0f - this.random.nextFloat() * 2.0f;
            this.zza = fast ? 8.0f : 6.0f;
            if (this.isInWater()) this.zza *= 0.75f;
            this.flyingSpeed = fast ? 0.04f : 0.03f;
            this.playSound(SoundEvents.CHEST_CLOSE, 0.1f, this.random.nextFloat() * 0.1f + 0.9f);
        } else {
            this.setJumping(false);
            if (this.onGround()) this.xxa = this.zza = 0.0f;
        }
    }

    /** O {@code pullItems} da entropia: engole o que encosta nele e puxa o que está a três blocos. */
    private void pullItems() {
        if (this.isRemoved() || this.getHealth() <= 0.0f) return;
        if (!this.level().isClientSide()) {
            AABB near = new AABB(this.getX() - 0.5, this.getY() - 0.5, this.getZ() - 0.5, this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5);
            for (ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, near)) {
                ItemStack stack = item.getItem().copy();
                ItemStack out = InventoryUtils.placeItemStackIntoInventory(stack, this.inventory, 0, true);
                if (out.isEmpty() || out.getCount() != stack.getCount()) {
                    this.playSound(SoundEvents.GENERIC_EAT.value(), 0.5f, this.random.nextFloat() * 0.5f + 0.5f);
                    this.level().broadcastEntityEvent(this, (byte) 17);
                    if (!out.isEmpty()) item.setItem(out);
                    else item.discard();
                }
            }
        }
        AABB around = new AABB(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 3.0, this.getZ() + 3.0);
        for (ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, around)) {
            double d6 = item.getX() - this.getX();
            double d8 = item.getY() - this.getY() + this.getBbHeight() * 0.8f;
            double d10 = item.getZ() - this.getZ();
            double d11 = Math.sqrt(d6 * d6 + d8 * d8 + d10 * d10);
            double d13 = 0.075;
            item.setDeltaMovement(item.getDeltaMovement().subtract(d6 / d11 * d13, d8 / d11 * d13, d10 / d11 * d13));
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide()) this.inventory.dropAll();
        super.die(source);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOD_STEP;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ITEM_BREAK.value();
    }

    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    // ---- as mãos do jogador

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(TCItems.GOLEM_BELL)) {
            return this.getUpgrade() == 3 && !this.getOwnerName().equals(player.getName().getString()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (this.getUpgrade() == -1 && stack.getItem() instanceof GolemUpgradeItem upgrade) {
            if (!this.level().isClientSide()) {
                this.setUpgrade(upgrade.index());
                this.setInvSize();
                stack.shrink(1);
                this.playSound(TCSounds.UPGRADE.value(), 0.5f, 1.0f);
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food != null && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide()) {
                stack.shrink(1);
                this.heal(food.nutrition());
                this.playSound(this.getHealth() == this.getMaxHealth() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT.value(),
                        0.5f, this.random.nextFloat() * 0.5f + 0.5f);
                this.level().broadcastEntityEvent(this, (byte) 18);
            }
            this.lidrot = 0.15f;
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide()) {
            if (this.getUpgrade() == 3 && !this.getOwnerName().equals(player.getName().getString())) return InteractionResult.SUCCESS;
            if (player instanceof ServerPlayer sp) net.thaumcraft.inventory.TrunkMenu.open(sp, this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 17) {
            this.lidrot = 0.15f;
        } else if (id == 18) {
            this.lidrot = 0.15f;
            double d = this.random.nextGaussian() * 0.02, d1 = this.random.nextGaussian() * 0.02, d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.HEART, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                    this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                    this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(), d, d1, d2);
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ---- salvar

    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Stay", this.getStay());
        nbt.putByte("upgrade", (byte) this.getUpgrade());
        nbt.putString("Owner", this.getOwnerName());
        ValueOutput.ValueOutputList list = nbt.childrenList("Inventory");
        for (int a = 0; a < 36; a++) {
            ItemStack in = this.inventory.getItem(a);
            if (in.isEmpty()) continue;
            ValueOutput e = list.addChild();
            e.putByte("Slot", (byte) a);
            e.store("Item", ItemStack.CODEC, in);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        this.setStay(nbt.getBooleanOr("Stay", false));
        this.setUpgrade(nbt.getByteOr("upgrade", (byte) -1));
        this.setOwner(nbt.getStringOr("Owner", ""));
        this.inventory.clearContent();
        for (ValueInput e : nbt.childrenListOrEmpty("Inventory")) {
            int slot = e.getByteOr("Slot", (byte) 0) & 255;
            if (slot < 36) this.inventory.setItem(slot, e.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
        this.setInvSize();
    }

    /** O {@code travelToDimension} de quem vai atrás do dono: o baú aparece do lado dele no mundo novo. */
    public static void followOwner(ServerPlayer player) {
        List<WeakReference<TravelingTrunkEntity>> dudes = LINKED.get(player.getName().getString());
        if (dudes == null) return;
        for (WeakReference<TravelingTrunkEntity> dude : dudes) {
            TravelingTrunkEntity trunk = dude.get();
            if (trunk == null || trunk.isRemoved() || trunk.getStay() || trunk.level() == player.level()) continue;
            trunk.teleport(new net.minecraft.world.level.portal.TeleportTransition(player.level(),
                    player.position().add(0.0, 0.25, 0.0), net.minecraft.world.phys.Vec3.ZERO, trunk.getYRot(), trunk.getXRot(),
                    net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
        }
    }
}
