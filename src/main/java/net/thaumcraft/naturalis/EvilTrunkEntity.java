package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O Baú Maligno: o {@code EntityEvilTrunk} do Magia Naturalis 0.5.0.
 *
 * <p>Um baú de dentes que anda aos pulos atrás de quem o chamou, morde quem atacar o dono, come comida para se
 * remendar e guarda trinta e seis coisas. O sino de golem o recolhe de volta em item — e, se quem o recolhe não
 * estiver agachado, o item leva o que havia dentro. São quatro feitios, cada um com a sua cara.
 */
public class EvilTrunkEntity extends Mob {
    /** Os quatro feitios do original, na ordem dos números dele. */
    public enum Kind {
        CORRUPTED, SINISTER, DEMONIC, TAINTED;

        public static Kind byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : CORRUPTED;
        }
    }

    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(EvilTrunkEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(EvilTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAITING = SynchedEntityData.defineId(EvilTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(EvilTrunkEntity.class, EntityDataSerializers.STRING);

    /** As trinta e seis casas do {@code InventoryEvilTrunk}. */
    public static final int SLOTS = 36;
    /** A tela mostra quatro fileiras. */
    public static final int ROWS = 4;

    public final Inventory inventory = new Inventory();
    /** O quanto a boca já abriu, só no cliente. */
    public float lidRot;
    private int jumpDelay;
    private int biteTime;
    private float flyingSpeed = 0.02f;
    private @Nullable UUID owner;

    /** O inventário dele, como o do baú itinerante: trinta e seis casas soltas. */
    public class Inventory implements Container {
        private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

        @Override
        public int getContainerSize() {
            return SLOTS;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : this.items) if (!stack.isEmpty()) return false;
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < SLOTS ? this.items.get(slot) : ItemStack.EMPTY;
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
            if (slot >= 0 && slot < SLOTS) this.items.set(slot, stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return !EvilTrunkEntity.this.isRemoved();
        }

        @Override
        public void clearContent() {
            this.items.clear();
        }

        public NonNullList<ItemStack> all() {
            return this.items;
        }

        public boolean hasItems() {
            return !this.isEmpty();
        }

        /** Derrama tudo no chão. */
        public void dropAll() {
            for (int slot = 0; slot < SLOTS; slot++) {
                ItemStack stack = this.items.get(slot);
                if (stack.isEmpty()) continue;
                EvilTrunkEntity.this.spawnAtLocation((ServerLevel) EvilTrunkEntity.this.level(), stack);
                this.items.set(slot, ItemStack.EMPTY);
            }
        }
    }

    public EvilTrunkEntity(EntityType<? extends EvilTrunkEntity> type, Level level) {
        super(type, level);
        this.jumpDelay = this.random.nextInt(20) + 10;
        this.setPersistenceRequired();
        // como o baú itinerante, ele anda aos pulos: os controles do jogo não mandam nele
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
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, (byte) 0);
        builder.define(OPEN, false);
        builder.define(WAITING, false);
        builder.define(OWNER_NAME, "");
    }

    // ------------------------------------------------------------------ os dados

    public Kind kind() {
        return Kind.byId(this.entityData.get(KIND));
    }

    public void kind(Kind kind) {
        this.entityData.set(KIND, (byte) kind.ordinal());
    }

    public boolean isOpen() {
        return this.entityData.get(OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(OPEN, open);
    }

    public boolean isWaiting() {
        return this.entityData.get(WAITING);
    }

    public void setWaiting(boolean waiting) {
        this.entityData.set(WAITING, waiting);
    }

    public String ownerName() {
        return this.entityData.get(OWNER_NAME);
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    public void owner(Player player) {
        this.owner = player.getUUID();
        this.entityData.set(OWNER_NAME, player.getName().getString());
    }

    public boolean isOwner(Player player) {
        return this.owner != null && this.owner.equals(player.getUUID());
    }

    public @Nullable Player ownerEntity() {
        return this.owner == null ? null : this.level().getPlayerByUUID(this.owner);
    }

    // ------------------------------------------------------------------ a vida

    /** Fogo, lava, cacto e afogamento não o pegam. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            // a boca sobe quando ele cai e quando está aberto, e desce quando ele pousa
            if (!this.onGround() && this.getDeltaMovement().y < 0.0 && !this.isInWater()) this.lidRot += 0.015f;
            if ((this.onGround() || this.isInWater()) && !this.isOpen()) {
                this.lidRot -= 0.1f;
                if (this.lidRot < 0.0f) this.lidRot = 0.0f;
            }
            if (this.isOpen()) this.lidRot += 0.035f;
            float teto = this.isOpen() ? 0.5f : 0.2f;
            if (this.lidRot > teto) this.lidRot = teto;
        } else if (this.getHealth() < this.getMaxHealth() && this.tickCount % 50 == 0) {
            this.heal(1.0f);
        }
    }

    @Override
    protected float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    /** O {@code updateEntityActionState}: pula atrás do dono e morde quem ele está caçando. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.biteTime > 0) this.biteTime--;
        this.fallDistance = 0.0;
        Player dono = this.ownerEntity();
        if (dono == null) return;
        // longe demais, ele se põe do lado do dono
        if (!this.isWaiting() && this.distanceTo(dono) > 20.0f) {
            int i = net.minecraft.util.Mth.floor(dono.getX()) - 2;
            int j = net.minecraft.util.Mth.floor(dono.getZ()) - 2;
            int k = net.minecraft.util.Mth.floor(dono.getBoundingBox().minY);
            for (int l = 0; l <= 4; l++) {
                for (int c = 0; c <= 4; c++) {
                    if (l >= 1 && c >= 1 && l <= 3 && c <= 3) continue;
                    BlockPos abaixo = new BlockPos(i + l, k - 1, j + c);
                    BlockPos onde = abaixo.above();
                    boolean chao = level.getBlockState(abaixo).isRedstoneConductor(level, abaixo);
                    if (chao && !level.getBlockState(onde).isRedstoneConductor(level, onde)
                            && !level.getBlockState(onde.above()).isRedstoneConductor(level, onde.above())) {
                        level.playSound(null, i + l + 0.5, k, j + c + 0.5, SoundEvents.ENDERMAN_TELEPORT,
                                this.getSoundSource(), 0.5f, 1.0f);
                        this.snapTo(i + l + 0.5, k, j + c + 0.5, this.getYRot(), this.getXRot());
                        this.setTarget(null);
                        return;
                    }
                }
            }
        }
        boolean andar = false;
        LivingEntity alvo = this.getTarget();
        if (alvo != null && !alvo.isAlive()) {
            this.setTarget(null);
            alvo = null;
        }
        if (alvo != null && alvo != dono) {
            this.lookAt(alvo, 10.0f, 20.0f);
            andar = true;
            if (this.biteTime <= 0 && this.distanceTo(alvo) < 1.5
                    && alvo.getBoundingBox().maxY > this.getBoundingBox().minY
                    && alvo.getBoundingBox().minY < this.getBoundingBox().maxY) {
                this.biteTime = 10 + this.random.nextInt(5);
                float dano = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                alvo.hurtServer(level, this.damageSources().mobAttack(this), dano);
                level.broadcastEntityEvent(this, (byte) 10);
                this.playSound(SoundEvents.BLAZE_HURT, 0.5f, this.random.nextFloat() * 0.1f + 0.9f);
            }
        }
        if (!this.isWaiting() && alvo == null && this.distanceTo(dono) > 5.0f) {
            this.lookAt(dono, 10.0f, 20.0f);
            andar = true;
        }
        if ((this.onGround() || this.isInWater()) && this.jumpDelay-- <= 0 && andar) {
            this.jumpDelay = (this.random.nextInt(10) + 5) / 3;
            this.setJumping(true);
            this.xxa = 1.0f - this.random.nextFloat() * 2.0f;
            this.zza = 6.0f;
            if (this.isInWater()) this.zza *= 0.75f;
            this.flyingSpeed = 0.03f;
            this.playSound(SoundEvents.CHEST_CLOSE, 0.1f, this.random.nextFloat() * 0.1f + 0.9f);
        } else {
            this.setJumping(false);
            if (this.onGround()) this.xxa = this.zza = 0.0f;
        }
    }

    // ------------------------------------------------------------------ as mãos de quem chega

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var comida = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (comida != null && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide()) {
                stack.shrink(1);
                this.heal(comida.nutrition());
                this.playSound(this.getHealth() == this.getMaxHealth()
                        ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT.value(),
                        0.5f, this.random.nextFloat() * 0.5f + 0.5f);
                this.level().broadcastEntityEvent(this, (byte) 11);
            }
            this.lidRot = 0.15f;
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() || !this.isOwner(player)) return InteractionResult.PASS;
        if (!this.level().isClientSide() && player instanceof ServerPlayer dono) {
            net.thaumcraft.inventory.EvilTrunkMenu.open(dono, this);
        }
        return InteractionResult.SUCCESS;
    }

    /** O {@code handleLeftClick}: o sino de golem recolhe o baú de volta em item. */
    public static InteractionResult pickUp(Player player, Level level, InteractionHand hand,
                                           net.minecraft.world.entity.Entity entity,
                                           net.minecraft.world.phys.EntityHitResult hit) {
        if (!(entity instanceof EvilTrunkEntity trunk) || trunk.isRemoved()) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).is(TCItems.GOLEM_BELL) || !trunk.isOwner(player)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack item = new ItemStack(NaturalisItems.trunkSpawner(trunk.kind()));
        // agachado, quem recolhe derrama o que havia dentro; de pé, o item leva tudo junto
        if (player.isShiftKeyDown()) {
            trunk.inventory.dropAll();
        } else if (trunk.inventory.hasItems()) {
            item.set(net.minecraft.core.component.DataComponents.CONTAINER,
                    net.minecraft.world.item.component.ItemContainerContents.fromItems(trunk.inventory.all()));
            trunk.inventory.clearContent();
        }
        trunk.spawnAtLocation((ServerLevel) level, item);
        level.playSound(null, trunk.getX(), trunk.getY(), trunk.getZ(), net.thaumcraft.registry.TCSounds.ZAP.value(),
                trunk.getSoundSource(), 0.5f, 1.0f);
        trunk.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.lidRot = 0.15f;
        } else if (id == 11) {
            this.lidRot = 0.15f;
            for (int a = 0; a < 1; a++) {
                double dx = this.random.nextGaussian() * 0.02, dy = this.random.nextGaussian() * 0.02,
                        dz = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.HEART,
                        this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                        this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(), dx, dy, dz);
            }
        } else {
            super.handleEntityEvent(id);
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

    // ------------------------------------------------------------------ o que fica gravado

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("TrunkType", (byte) this.kind().ordinal());
        output.putBoolean("Waiting", this.isWaiting());
        output.putString("OwnerName", this.ownerName());
        if (this.owner != null) output.store("OwnerUUID", UUIDUtil.CODEC, this.owner);
        ValueOutput.ValueOutputList lista = output.childrenList("Inventory");
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            ValueOutput casa = lista.addChild();
            casa.putByte("Slot", (byte) slot);
            casa.store("Item", ItemStack.CODEC, stack);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.kind(Kind.byId(input.getByteOr("TrunkType", (byte) 0)));
        this.setWaiting(input.getBooleanOr("Waiting", false));
        this.entityData.set(OWNER_NAME, input.getStringOr("OwnerName", ""));
        this.owner = input.read("OwnerUUID", UUIDUtil.CODEC).orElse(null);
        this.inventory.clearContent();
        for (ValueInput casa : input.childrenListOrEmpty("Inventory")) {
            int slot = casa.getByteOr("Slot", (byte) 0) & 255;
            if (slot < SLOTS) this.inventory.setItem(slot, casa.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
    }
}
