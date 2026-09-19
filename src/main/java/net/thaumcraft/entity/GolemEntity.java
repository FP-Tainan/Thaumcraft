package net.thaumcraft.entity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.golems.GolemTypes;
import net.thaumcraft.entity.golem.GolemInventory;
import net.thaumcraft.entity.golem.InventoryUtils;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.entity.golem.ai.GolemGoals;
import net.thaumcraft.item.GolemCoreItem;
import net.thaumcraft.item.GolemDecorationItem;
import net.thaumcraft.item.GolemUpgradeItem;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O golem: o {@code EntityGolemBase} da 4.2.3.5.
 *
 * <p>De que ele é feito ({@link GolemTypes}, o {@code EnumGolemType}) manda na vida, no couro, no passo, na força, em
 * quanto carrega e em quantas melhorias cabem. O que ele <em>faz</em> é o núcleo (0 encher, 1 esvaziar, 2 juntar,
 * 3 colher, 4 guardar, 5 decantar líquidos, 6 alquimia, 7 lenhar, 8 usar, 9 açougue, 10 separar, 11 pescar) — cada um
 * monta as tarefas do original em {@link #setupGolem()}. As melhorias (0 ar, 1 terra, 2 fogo, 3 água, 4 ordem,
 * 5 entropia, até duas de cada) e os enfeites (H cartola, G óculos, B gravata, F fez, R dardos, V viseira, P couraça,
 * M maça) mudam números por toda parte, como no original.
 *
 * <p>A casa dele é onde foi posto, e a face do bloco em que foi posto: o baú da casa é o bloco atrás dessa face. As
 * marcas do sino dizem aonde ir buscar e levar.
 */
public class GolemEntity extends PathfinderMob implements net.minecraft.world.entity.ContainerUser {
    private static final EntityDataAccessor<ItemStack> CARRIED = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> TOGGLES = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> DECORATION = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> CORE = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> COLORS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> UPGRADES = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> ADVANCED = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> FLUID = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FLUID_AMOUNT = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SLOTS = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOME_FACING = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);

    public GolemInventory inventory = new GolemInventory(this, 1);
    public ItemStack itemCarried = ItemStack.EMPTY;
    /** O líquido que carrega (núcleo 5), em mB. */
    public FluidVariant fluidCarried = FluidVariant.blank();
    public int fluidAmount;
    public ItemStack itemWatched = ItemStack.EMPTY;
    @Nullable
    public Aspect essentia;
    public int essentiaAmount;
    public boolean advanced;
    public int homeFacing;
    public boolean paused;
    public boolean inactive;
    public byte[] colors = {-1};
    public byte[] upgrades = {-1};
    public String decoration = "";
    public float bootup = -1.0f;
    public int golemType = 1;
    public int regenTimer;
    protected List<Marker> markers = new ArrayList<>();
    public int action;
    public int leftArm;
    public int rightArm;
    public int healing;

    public GolemEntity(EntityType<? extends GolemEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        if (this.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
            ground.setCanFloat(true);
        }
    }

    public static AttributeSupplier.Builder attributes() {
        // o passo vem do tipo, e não do atributo: no original o getAIMoveSpeed devolve o passo do tipo
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    /** Monta um golem novo daquele tipo, avançado ou não, com as casas de melhoria vazias. */
    public void init(int type, boolean adv) {
        this.golemType = type;
        this.entityData.set(TYPE, (byte) type);
        this.advanced = adv;
        this.entityData.set(ADVANCED, adv);
        this.upgrades = new byte[this.type().upgrades() + (adv ? 1 : 0)];
        java.util.Arrays.fill(this.upgrades, (byte) -1);
        this.syncUpgrades();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CARRIED, ItemStack.EMPTY);
        builder.define(OWNER, "");
        builder.define(TOGGLES, (byte) 0);
        builder.define(TYPE, (byte) 1);
        builder.define(DECORATION, "");
        builder.define(CORE, (byte) -1);
        builder.define(COLORS, "");
        builder.define(UPGRADES, "");
        builder.define(ADVANCED, false);
        builder.define(FLUID, "");
        builder.define(FLUID_AMOUNT, 0);
        builder.define(SLOTS, 1);
        builder.define(HOME_FACING, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (!this.level().isClientSide()) return;
        // o readSpawnData: quem já nasce com núcleo não faz a animação de despertar
        if (CORE.equals(key) && this.getCore() >= 0 && this.bootup < 0.0f) this.bootup = 0.0f;
        if (ADVANCED.equals(key)) this.advanced = this.entityData.get(ADVANCED);
        if (TYPE.equals(key)) this.golemType = this.entityData.get(TYPE);
        if (DECORATION.equals(key)) this.decoration = this.entityData.get(DECORATION);
        if (SLOTS.equals(key) && this.inventory.slotCount != this.entityData.get(SLOTS)) {
            this.inventory = new GolemInventory(this, this.entityData.get(SLOTS));
        }
    }

    // ---- o tipo

    public GolemTypes.Type type() {
        var names = GolemTypes.ALL.values().toArray(new GolemTypes.Type[0]);
        return names[Math.floorMod(this.golemType, names.length)];
    }

    public int getGolemTypeIndex() {
        return this.golemType;
    }

    public String material() {
        return this.type().name();
    }

    public static int typeIndex(String material) {
        int i = 0;
        for (String name : GolemTypes.ALL.keySet()) {
            if (name.equals(material)) return i;
            i++;
        }
        return 1;
    }

    /** Pedra, ferro e táumio: pesados, andam pelo fundo da água, e depressa. */
    public boolean isHeavy() {
        int t = this.golemType;
        return t == 5 || t == 6 || t == 7;
    }

    // ---- montagem

    /** O {@code setupGolemInventory}: o tamanho da lista conforme o núcleo e a melhoria de fogo. */
    public boolean setupGolemInventory() {
        if (!GolemCoreItem.hasInventory(this.getCore())) return false;
        if (this.getCore() > -1) {
            int invSize = switch (this.getCore()) {
                case 3, 4, 6 -> 0;
                case 5 -> 1 + this.getUpgradeAmount(2);
                default -> 6 + this.getUpgradeAmount(2) * 6;
            };
            GolemInventory inventory2 = new GolemInventory(this, invSize);
            inventory2.copyFrom(this.inventory);
            this.inventory = inventory2;
            this.entityData.set(SLOTS, invSize);
        }
        byte[] old = this.colors;
        this.colors = new byte[this.inventory.slotCount];
        for (int a = 0; a < this.inventory.slotCount; a++) this.colors[a] = a < old.length ? old[a] : -1;
        this.syncColors();
        return true;
    }

    /** O {@code setupGolem}: vida, dano e as tarefas de cada núcleo, na prioridade do original. */
    public boolean setupGolem() {
        if (!this.level().isClientSide()) this.entityData.set(TYPE, (byte) this.golemType);
        // os leves fogem da água; os pesados andam por ela
        this.setPathfindingMalus(PathType.WATER, this.isHeavy() ? 0.0f : -1.0f);
        int bonus = this.getGolemDecoration().contains("H") ? 5 : 0;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.type().health() + bonus);
        int damage = 2 + this.getGolemStrength() + this.getUpgradeAmount(1);
        if (this.getGolemDecoration().contains("M")) damage += 2;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        this.goalSelector.removeAllGoals(g -> true);
        this.targetSelector.removeAllGoals(g -> true);
        GolemGoals.register(this, this.goalSelector, this.targetSelector);
        if (this.getCore() > -1) {
            this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
            this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        }
        return true;
    }

    public void setup(int side) {
        this.homeFacing = side;
        this.entityData.set(HOME_FACING, side);
        this.setupGolem();
        this.setupGolemInventory();
    }

    @Override
    protected void registerGoals() {
    }

    /** As tarefas que estão rodando agora (para as provas saberem onde o golem está no serviço). */
    public List<String> runningGoals() {
        return this.goalSelector.getAvailableGoals().stream().filter(w -> w.isRunning())
                .map(w -> w.getGoal().getClass().getSimpleName()).toList();
    }

    // ---- a casa

    public BlockPos home() {
        return this.getHomePosition();
    }

    public Direction homeFacing() {
        return Direction.from3DDataValue(this.level().isClientSide() ? this.entityData.get(HOME_FACING) : this.homeFacing);
    }

    /** O baú da casa: o bloco atrás da face em que o golem foi posto. */
    public BlockPos homeContainer() {
        return this.home().relative(this.homeFacing().getOpposite());
    }

    public void setHome(BlockPos pos) {
        this.setHomeTo(pos, 32);
    }

    // ---- números

    public int getCarryLimit() {
        int base = this.type().carry();
        return base + Math.min(16, Math.max(4, base)) * this.getUpgradeAmount(1);
    }

    public int getFluidCarryLimit() {
        return (int) Math.floor(Math.sqrt(this.getCarryLimit())) * 1000;
    }

    /** O {@code getAIMoveSpeed}: o passo do tipo, com os enfeites e as melhorias. Parado ou algemado, zero. */
    public float golemSpeed() {
        if (this.paused || this.inactive) return 0.0f;
        String deco = this.getGolemDecoration();
        float speed = (float) this.type().speed() * (deco.contains("B") ? 1.1f : 1.0f);
        if (deco.contains("P")) speed *= 0.88f;
        speed *= 1.0f + this.getUpgradeAmount(0) * 0.15f;
        if (this.advanced) speed *= 1.1f;
        if (this.isInWater() && this.isHeavy()) speed *= 2.0f;
        return speed;
    }

    public float getRange() {
        float dmod = 16 + this.getUpgradeAmount(3) * 4;
        if (this.getGolemDecoration().contains("G")) dmod += Math.max(dmod * 0.1f, 1.0f);
        if (this.advanced) dmod += Math.max(dmod * 0.2f, 2.0f);
        return dmod;
    }

    @Override
    public boolean isWithinHome(BlockPos pos) {
        float dmod = this.getRange();
        return this.home().distSqr(pos) < dmod * dmod;
    }

    public int getGolemStrength() {
        return this.type().strength() + this.getUpgradeAmount(1);
    }

    public int getAttackSpeed() {
        return 20 - (this.advanced ? 2 : 0);
    }

    @Override
    public int getArmorValue() {
        int armor = super.getArmorValue() + this.type().armor();
        String deco = this.getGolemDecoration();
        if (deco.contains("V")) armor++;
        if (deco.contains("P")) armor += 4;
        return Math.min(armor, 20);
    }

    // ---- o que carrega

    public ItemStack getCarried() {
        if (!this.itemCarried.isEmpty() && this.itemCarried.getCount() <= 0) this.setCarried(ItemStack.EMPTY);
        return this.itemCarried;
    }

    public void setCarried(ItemStack stack) {
        this.itemCarried = stack;
        this.updateCarried();
    }

    public int getCarrySpace() {
        return this.itemCarried.isEmpty() ? this.getCarryLimit()
                : Math.min(this.getCarryLimit() - this.itemCarried.getCount(), this.itemCarried.getMaxStackSize() - this.itemCarried.getCount());
    }

    public boolean hasSomething() {
        return this.inventory.hasSomething();
    }

    public ItemStack getCarriedForDisplay() {
        return this.entityData.get(CARRIED);
    }

    @Nullable
    public Fluid displayFluid() {
        String id = this.entityData.get(FLUID);
        if (id.isEmpty()) return null;
        return net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(net.minecraft.resources.Identifier.parse(id));
    }

    public int displayFluidAmount() {
        return this.entityData.get(FLUID_AMOUNT);
    }

    /** O {@code updateCarried}: o que os outros veem na mão dele (ou no balde, ou no jarro). */
    public void updateCarried() {
        if (this.level().isClientSide()) return;
        if (!this.itemCarried.isEmpty()) {
            this.entityData.set(CARRIED, this.itemCarried.copy());
        } else if (this.getCore() == 6) {
            this.entityData.set(CARRIED, this.essentiaDisplay());
        } else {
            this.entityData.set(CARRIED, ItemStack.EMPTY);
        }
        if (this.getCore() == 5 && this.fluidAmount > 0 && !this.fluidCarried.isBlank()) {
            this.entityData.set(FLUID, net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(this.fluidCarried.getFluid()).toString());
            this.entityData.set(FLUID_AMOUNT, this.fluidAmount);
        } else {
            this.entityData.set(FLUID, "");
            this.entityData.set(FLUID_AMOUNT, 0);
        }
    }

    /** O jarro cheio que o golem alquimista leva nos braços: 64 é cheio, conforme a carga. */
    private ItemStack essentiaDisplay() {
        ItemStack disp = new ItemStack(TCBlocks.JAR.asItem());
        int amt = (int) (64.0f * ((float) this.essentiaAmount / this.getCarryLimit()));
        if (this.essentia != null && this.essentiaAmount > 0) {
            disp.set(net.thaumcraft.registry.TCComponents.JAR_CONTENTS, net.thaumcraft.item.JarContents.of(this.essentia, Math.max(1, amt), null));
        }
        return disp;
    }

    public void dropStuff() {
        if (!this.itemCarried.isEmpty() && this.level() instanceof ServerLevel server) {
            this.spawnAtLocation(server, this.itemCarried, 0.5f);
        }
    }

    // ---- chaves, cores, melhorias, enfeites

    public boolean[] getToggles() {
        byte b = this.entityData.get(TOGGLES);
        boolean[] out = new boolean[8];
        for (int a = 0; a < 8; a++) out[a] = (b & (1 << a)) != 0;
        return out;
    }

    public byte getTogglesValue() {
        return this.entityData.get(TOGGLES);
    }

    public void setToggle(int index, boolean tog) {
        byte b = this.entityData.get(TOGGLES);
        b = (byte) (tog ? b | (1 << index) : b & ~(1 << index));
        this.entityData.set(TOGGLES, b);
    }

    public void setTogglesValue(byte tog) {
        this.entityData.set(TOGGLES, tog);
    }

    public boolean canAttackHostiles() {
        return !this.getToggles()[1];
    }

    public boolean canAttackAnimals() {
        return !this.getToggles()[2];
    }

    public boolean canAttackPlayers() {
        return !this.getToggles()[3];
    }

    public boolean canAttackCreepers() {
        return !this.getToggles()[4];
    }

    public boolean checkOreDict() {
        return this.getToggles()[5];
    }

    public boolean ignoreDamage() {
        return this.getToggles()[6];
    }

    public boolean ignoreNBT() {
        return this.getToggles()[7];
    }

    public short getColors(int slot) {
        String s = this.entityData.get(COLORS);
        if (slot < 0 || slot >= s.length()) return -1;
        char c = s.charAt(slot);
        return c == 'h' ? -1 : Short.parseShort(String.valueOf(c), 16);
    }

    public void setColors(int slot, int color) {
        if (slot < this.colors.length) this.colors[slot] = (byte) color;
        this.syncColors();
    }

    private void syncColors() {
        StringBuilder s = new StringBuilder();
        for (byte c : this.colors) s.append(c == -1 ? "h" : Integer.toHexString(c));
        this.entityData.set(COLORS, s.toString());
    }

    public byte getUpgrade(int slot) {
        String s = this.entityData.get(UPGRADES);
        if (slot < 0 || slot >= s.length()) return -1;
        byte t = Byte.parseByte(String.valueOf(s.charAt(slot)), 16);
        return t == 15 ? -1 : t;
    }

    public int upgradeSlots() {
        return this.entityData.get(UPGRADES).length();
    }

    public int getUpgradeAmount(int type) {
        int a = 0;
        for (int i = 0; i < this.upgradeSlots(); i++) if (this.getUpgrade(i) == type) a++;
        return a;
    }

    public void setUpgrade(int slot, byte upgrade) {
        this.upgrades[slot] = upgrade;
        this.syncUpgrades();
    }

    private void syncUpgrades() {
        StringBuilder s = new StringBuilder();
        // o original escreve o −1 como "ffffffff"; aqui um f só, que o getUpgrade lê como vazio do mesmo jeito
        for (byte c : this.upgrades) s.append(c < 0 ? "f" : Integer.toHexString(c));
        this.entityData.set(UPGRADES, s.toString());
    }

    /** As cores das casas que pedem aquela coisa (todas, se nenhuma casa pede nada). */
    public List<Byte> getColorsMatching(ItemStack match) {
        List<Byte> l = new ArrayList<>();
        if (this.inventory.slotCount > 0) {
            boolean allNull = true;
            for (int a = 0; a < this.inventory.slotCount; a++) {
                if (!this.inventory.getItem(a).isEmpty()) allNull = false;
                if (InventoryUtils.areItemStacksEqual(this.inventory.getItem(a), match, this.checkOreDict(), this.ignoreDamage(), this.ignoreNBT())) {
                    l.add(this.colors[a]);
                }
            }
            if (allNull) for (int a = 0; a < this.inventory.slotCount; a++) l.add(this.colors[a]);
        }
        return l;
    }

    public byte getCore() {
        return this.entityData.get(CORE);
    }

    public void setCore(byte core) {
        this.entityData.set(CORE, core);
    }

    public String getGolemDecoration() {
        return this.entityData.get(DECORATION);
    }

    public void setGolemDecoration(String deco) {
        this.decoration = deco;
        this.entityData.set(DECORATION, deco);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER);
    }

    public void setOwner(String name) {
        this.entityData.set(OWNER, name);
    }

    public List<Marker> getMarkers() {
        List<Marker> valid = new ArrayList<>();
        for (Marker m : this.markers) if (m.in(this.level())) valid.add(m);
        this.markers = valid;
        return this.markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = new ArrayList<>(markers);
    }

    public float getHealthPercentage() {
        return this.getHealth() / this.getMaxHealth();
    }

    // ---- a tampa do baú (o golemChestInteract do original, ligado de fábrica)

    @Nullable
    private BlockPos openedChest;

    /** O {@code openInventory}: a tampa sobe enquanto o golem mexe no baú. */
    public void openChest(net.minecraft.world.Container container) {
        if (!(container instanceof net.minecraft.world.level.block.entity.BlockEntity be)) return;
        this.closeChest(null);
        this.openedChest = be.getBlockPos();
        container.startOpen(this);
    }

    /** O {@code closeInventory}. */
    public void closeChest(@Nullable net.minecraft.world.Container container) {
        if (this.openedChest == null) return;
        var target = container != null ? container : InventoryUtils.containerAt(this.level(), this.openedChest);
        BlockPos was = this.openedChest;
        this.openedChest = null;
        if (target != null && (!(target instanceof net.minecraft.world.level.block.entity.BlockEntity be) || be.getBlockPos().equals(was))) {
            target.stopOpen(this);
        }
    }

    @Override
    public boolean hasContainerOpen(net.minecraft.world.level.block.entity.ContainerOpenersCounter counter, BlockPos pos) {
        return this.openedChest != null && this.openedChest.equals(pos);
    }

    @Override
    public double getContainerInteractionRange() {
        return 3.0;
    }

    // ---- as animações

    public int getActionTimer() {
        return 3 - Math.abs(this.action - 3);
    }

    public void startActionTimer() {
        if (this.action == 0) {
            this.action = 6;
            this.level().broadcastEntityEvent(this, (byte) 4);
        }
    }

    public void startLeftArmTimer() {
        if (this.leftArm == 0) {
            this.leftArm = 5;
            this.level().broadcastEntityEvent(this, (byte) 6);
        }
    }

    public void startRightArmTimer() {
        if (this.rightArm == 0) {
            this.rightArm = 5;
            this.level().broadcastEntityEvent(this, (byte) 8);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 4 -> this.action = 6;
            case 5 -> {
                this.healing = 5;
                int bonus = this.getGolemDecoration().contains("H") ? 5 : 0;
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.type().health() + bonus);
            }
            case 6 -> this.leftArm = 5;
            case 8 -> this.rightArm = 5;
            case 7 -> this.bootup = 33.0f;
            default -> super.handleEntityEvent(id);
        }
    }

    // ---- a vida

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(0.4f, 0.95f);
    }

    /** Parado (com a tela aberta) ou algemado, a cabeça não pensa — no original a IA fica desligada. */
    @Override
    protected boolean isImmobile() {
        return this.paused || this.inactive || super.isImmobile();
    }

    @Override
    public float getSpeed() {
        return this.paused || this.inactive ? 0.0f : super.getSpeed();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.action > 0) this.action--;
        if (this.leftArm > 0) this.leftArm--;
        if (this.rightArm > 0) this.rightArm--;
        if (this.healing > 0) this.healing--;
        BlockPos below = this.blockPosition().below();
        var belowState = this.level().getBlockState(below);
        this.inactive = this.blockPosition().getY() > this.level().getMinY() && belowState.is(TCBlocks.GOLEM_FETTER)
                && belowState.getValue(net.thaumcraft.block.GolemFetterBlock.POWERED);
        if (!this.level().isClientSide()) {
            if (this.regenTimer > 0) {
                this.regenTimer--;
            } else {
                this.regenTimer = this.type().regenDelay();
                if (this.getGolemDecoration().contains("F")) this.regenTimer = (int) (this.regenTimer * 0.66f);
                if (this.getHealth() < this.getMaxHealth()) {
                    this.level().broadcastEntityEvent(this, (byte) 5);
                    this.heal(1.0f);
                }
            }
            BlockPos home = this.home();
            if (this.distanceToSqr(home.getX(), home.getY(), home.getZ()) >= 2304.0 || this.isInWall()) {
                // longe demais de casa (ou preso num bloco): volta para perto dela
                for (int dy = 1; dy >= -1; dy--) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos at = home.offset(dx, dy, dz);
                            if (this.level().getBlockState(at.below()).isFaceSturdy(this.level(), at.below(), Direction.UP)
                                    && !this.level().getBlockState(at).isCollisionShapeFullBlock(this.level(), at)) {
                                this.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, this.getYRot(), this.getXRot());
                                this.getNavigation().stop();
                                return;
                            }
                        }
                    }
                }
            }
        } else if (this.bootup > 0.0f && this.getCore() > -1) {
            this.bootup = this.bootup * (this.bootup / 33.1f);
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), TCSounds.CAMERA_TICKS.value(),
                    this.getSoundSource(), this.bootup * 0.2f, this.bootup, false);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return this.type().fireResist();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.paused = false;
        if (source.is(DamageTypes.CACTUS)) return false;
        if (source.is(DamageTypeTags.IS_FIRE) && this.type().fireResist()) return false;
        if (this.golemType == 7 && source.is(DamageTypes.MAGIC)) amount *= 0.5f;
        Entity attacker = source.getEntity();
        int entropy = this.getUpgradeAmount(5);
        if (attacker != null && entropy > 0 && attacker.getId() != this.getId()) {
            attacker.hurtServer(level, this.damageSources().thorns(this), entropy * 2 + this.random.nextInt(2 * entropy));
            attacker.playSound(SoundEvents.THORNS_HIT, 0.5f, 1.0f);
        }
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            BlockPos home = this.home();
            this.snapTo(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5, 0.0f, 0.0f);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide()) {
            net.thaumcraft.Thaumcraft.LOGGER.info("[Thaumcraft] {} was killed by {} ({})", this, source.getEntity(), source.getMsgId());
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        this.dropStuff();
    }

    /** O {@code attackEntityAsMob}: o dano do golem, fogo pela melhoria de fogo e o "golpe de jogador" da viseira. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        float f = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean flag = target.hurtServer(level, this.damageSources().mobAttack(this), f);
        if (flag) {
            if (this.getGolemDecoration().contains("V") && target instanceof LivingEntity living) {
                living.setLastHurtByPlayer(net.fabricmc.fabric.api.entity.FakePlayer.get(level), 100);
            }
            int j = this.getUpgradeAmount(2);
            if (j > 0) target.igniteForSeconds(j * 4);
        }
        return flag;
    }

    /** O {@code canAttackClass}: nunca aldeão, golem ou morcego. */
    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof net.minecraft.world.entity.npc.villager.Villager) && !(target instanceof GolemEntity)
                && !(target instanceof Bat) && super.canAttack(target);
    }

    /** O {@code isValidTarget}: quem o guarda ataca (conforme as chaves e a ordem), e quem o açougueiro abate. */
    public boolean isValidTarget(Entity target) {
        if (!target.isAlive()) return false;
        if (target instanceof Player p && p.getName().getString().equals(this.getOwnerName())) return false;
        if (!this.isWithinHome(target.blockPosition())) return false;
        if (this.getCore() == 9) {
            if (isAnimal(target)) return !(target instanceof Animal a && a.isBaby());
            return false;
        }
        if (this.canAttackCreepers() && this.getUpgradeAmount(4) > 0 && target instanceof Creeper) return true;
        if (this.canAttackHostiles() && target instanceof Enemy && !(target instanceof Creeper)) return true;
        if (this.canAttackAnimals() && this.getUpgradeAmount(4) > 0 && isAnimal(target)) return true;
        return this.canAttackPlayers() && this.getUpgradeAmount(4) > 0 && target instanceof Player;
    }

    /** Bicho: animal (ou outra criatura mansa), que não seja monstro, nem manso de alguém, nem golem. */
    private static boolean isAnimal(Entity target) {
        boolean animal = target instanceof Animal || target instanceof net.minecraft.world.entity.animal.AgeableWaterCreature
                || target instanceof net.minecraft.world.entity.animal.fish.WaterAnimal || target instanceof Bat;
        return animal && !(target instanceof Enemy) && !(target instanceof TamableAnimal t && t.isTame())
                && !(target instanceof AbstractGolem) && !(target instanceof GolemEntity) && !(target instanceof AbstractVillager);
    }

    /** O dardo: sai do golem com o dano da força, e o braço esquerdo dá o tranco. */
    public void attackEntityWithRangedAttack(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) return;
        DartEntity dart = new DartEntity(server, this, target, 1.6f, 7.0f - this.getUpgradeAmount(3) * 1.75f);
        dart.setBaseDamage(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f);
        this.playSound(TCSounds.GOLEM_IRON_SHOOT.value(), 0.5f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.6f));
        server.addFreshEntity(dart);
        this.startLeftArmTimer();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CAMERA_CLACK.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.CAMERA_CLACK.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.CAMERA_CLACK.value();
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("item.thaumcraft.golem_" + this.material());
    }

    // ---- as mãos do jogador

    private boolean addDecoration(String type, ItemStack stack) {
        String deco = this.decoration;
        if (deco.contains(type)) return false;
        if ((type.equals("F") || type.equals("H")) && (deco.contains("F") || deco.contains("H"))) return false;
        if ((type.equals("G") || type.equals("V")) && (deco.contains("G") || deco.contains("V"))) return false;
        if ((type.equals("B") || type.equals("P")) && (deco.contains("P") || deco.contains("B"))) return false;
        this.decoration = deco + type;
        if (!this.level().isClientSide()) {
            this.setGolemDecoration(this.decoration);
            stack.shrink(1);
            this.playSound(TCSounds.CAMERA_CLACK.value(), 1.0f, 1.0f);
            this.setupGolem();
        }
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(TCItems.GOLEM_BELL)) return InteractionResult.PASS;
        if (this.getCore() == -1 && stack.getItem() instanceof GolemCoreItem core) {
            if (!this.level().isClientSide()) {
                this.setCore((byte) core.index());
                this.setupGolem();
                this.setupGolemInventory();
                stack.shrink(1);
                this.playSound(TCSounds.UPGRADE.value(), 0.5f, 1.0f);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof GolemUpgradeItem upgrade) {
            for (int a = 0; a < this.upgradeSlots(); a++) {
                if (this.getUpgrade(a) == -1 && this.getUpgradeAmount(upgrade.index()) < 2) {
                    if (!this.level().isClientSide()) {
                        this.setUpgrade(a, (byte) upgrade.index());
                        this.setupGolem();
                        this.setupGolemInventory();
                        stack.shrink(1);
                        this.playSound(TCSounds.UPGRADE.value(), 0.5f, 1.0f);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
        if (stack.getItem() instanceof GolemDecorationItem deco) {
            this.addDecoration(deco.decoChar(), stack);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.COOKIE)) {
            stack.shrink(1);
            player.swing(hand);
            for (int i = 0; i < 3; i++) {
                double vx = this.random.nextGaussian() * 0.02, vy = this.random.nextGaussian() * 0.02, vz = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.HEART,
                        this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                        this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(), vx, vy, vz);
                this.playSound(SoundEvents.GENERIC_EAT.value(), 0.3f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                if (this.level().isClientSide()) {
                    // o original só dá a pressa do lado de quem vê: é enfeite, não passo
                    int duration = 600;
                    var speed = this.getEffect(MobEffects.SPEED);
                    if (speed != null && speed.getDuration() < 2400) duration += speed.getDuration();
                    this.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0));
                }
            }
            this.heal(5.0f);
            return InteractionResult.SUCCESS;
        }
        if (this.getCore() > -1 && GolemCoreItem.hasGUI(this.getCore()) && !(stack.getItem() instanceof net.thaumcraft.item.WandItem)) {
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) net.thaumcraft.inventory.GolemMenu.open(sp, this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // ---- salvar

    private static final com.mojang.serialization.Codec<List<Byte>> BYTES = com.mojang.serialization.Codec.BYTE.listOf();

    private static List<Byte> toList(byte[] in) {
        List<Byte> out = new ArrayList<>();
        for (byte b : in) out.add(b);
        return out;
    }

    private static byte[] toArray(List<Byte> in) {
        byte[] out = new byte[in.size()];
        for (int a = 0; a < out.length; a++) out[a] = in.get(a);
        return out;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        BlockPos home = this.home();
        nbt.putInt("HomeX", home.getX());
        nbt.putInt("HomeY", home.getY());
        nbt.putInt("HomeZ", home.getZ());
        nbt.putByte("HomeFacing", (byte) this.homeFacing);
        nbt.putByte("GolemType", (byte) this.golemType);
        nbt.putByte("Core", this.getCore());
        nbt.putString("Decoration", this.decoration);
        nbt.putByte("toggles", this.getTogglesValue());
        nbt.putBoolean("advanced", this.advanced);
        nbt.store("colors", BYTES, toList(this.colors));
        nbt.store("upgrades", BYTES, toList(this.upgrades));
        if (this.getCore() == 5 && this.fluidAmount > 0 && !this.fluidCarried.isBlank()) {
            nbt.store("Fluid", FluidVariant.CODEC, this.fluidCarried);
            nbt.putInt("FluidAmount", this.fluidAmount);
        }
        if (this.getCore() == 6 && this.essentia != null && this.essentiaAmount > 0) {
            nbt.putString("essentia", this.essentia.tag());
            nbt.putByte("essentiaAmount", (byte) this.essentiaAmount);
        }
        if (!this.itemCarried.isEmpty()) nbt.store("ItemCarried", ItemStack.CODEC, this.itemCarried);
        nbt.putString("Owner", this.getOwnerName());
        nbt.store("Markers", Marker.CODEC.listOf(), this.markers);
        this.inventory.save(nbt, "Inventory");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        this.homeFacing = nbt.getByteOr("HomeFacing", (byte) 0);
        this.entityData.set(HOME_FACING, this.homeFacing);
        this.setHome(new BlockPos(nbt.getIntOr("HomeX", 0), nbt.getIntOr("HomeY", 0), nbt.getIntOr("HomeZ", 0)));
        this.advanced = nbt.getBooleanOr("advanced", false);
        this.entityData.set(ADVANCED, this.advanced);
        this.golemType = nbt.getByteOr("GolemType", (byte) 1);
        this.setCore(nbt.getByteOr("Core", (byte) -1));
        if (this.getCore() == 5) {
            this.fluidCarried = nbt.read("Fluid", FluidVariant.CODEC).orElse(FluidVariant.blank());
            this.fluidAmount = nbt.getIntOr("FluidAmount", 0);
        }
        if (this.getCore() == 6) {
            this.essentia = Aspect.of(nbt.getStringOr("essentia", ""));
            if (this.essentia != null) this.essentiaAmount = nbt.getByteOr("essentiaAmount", (byte) 0);
        }
        this.setTogglesValue(nbt.getByteOr("toggles", (byte) 0));
        this.itemCarried = nbt.read("ItemCarried", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.setGolemDecoration(nbt.getStringOr("Decoration", ""));
        this.setOwner(nbt.getStringOr("Owner", ""));
        this.markers = new ArrayList<>(nbt.read("Markers", Marker.CODEC.listOf()).orElse(List.of()));
        int ul = this.type().upgrades() + (this.advanced ? 1 : 0);
        byte[] saved = toArray(nbt.read("upgrades", BYTES).orElse(List.of()));
        this.upgrades = new byte[ul];
        for (int a = 0; a < ul; a++) this.upgrades[a] = a < saved.length ? saved[a] : -1;
        this.syncUpgrades();
        this.setupGolem();
        this.setupGolemInventory();
        this.inventory.load(nbt, "Inventory");
        byte[] oldcolors = toArray(nbt.read("colors", BYTES).orElse(List.of()));
        this.colors = new byte[this.inventory.slotCount];
        for (int a = 0; a < this.inventory.slotCount; a++) this.colors[a] = a < oldcolors.length ? oldcolors[a] : -1;
        this.syncColors();
        this.updateCarried();
    }
}
