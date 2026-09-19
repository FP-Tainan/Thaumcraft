package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.item.ManaBeanItem;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * O pech: o {@code EntityPech} da 4.2.3.5. Um bichinho de mochila que mora onde o véu entre os mundos é fino, cata o
 * que é de valor (e o que couber na mochila) e foge de gente. Três tipos: o coletor, de briga; o mago, com uma
 * varinha e o foco dos pechs; e o caçador, de arco. Quem fere um pech arruma briga com todos os de perto.
 *
 * <p>Dado algo de valor (ouro, pérola do fim, diamante, esmeralda, maçã dourada, feijão de mana, ou qualquer coisa
 * com Lucrum), ele pode ficar manso; manso, abre a troca: um item de valor pelos tesouros da mochila e da tabela do
 * tipo dele.
 */
public class PechEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(PechEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(PechEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(PechEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int FORAGER = 0, MAGE = 1, STALKER = 2;

    public final NonNullList<ItemStack> loot = NonNullList.withSize(9, ItemStack.EMPTY);
    public boolean trading;
    public boolean updateAINextTick;
    /** O queixo mexendo quando ele resmunga: pi, ou dois pi, caindo para zero. Só do lado de quem vê. */
    public float mumble;
    private int chargeCount;

    private final RangedAttackGoal arrowAttack = new RangedAttackGoal(this, 0.6, 20, 50, 15.0f);
    private final RangedAttackGoal blastAttack = new RangedAttackGoal(this, 0.6, 20, 30, 15.0f);
    private final MeleeAttackGoal meleeAttack = new MeleeAttackGoal(this, 0.6, false);
    private final AvoidEntityGoal<Player> avoidPlayer = new AvoidEntityGoal<>(this, Player.class, 8.0f, 0.5, 0.6);

    public PechEntity(EntityType<? extends PechEntity> type, Level level) {
        super(type, level);
        if (this.getNavigation() instanceof GroundPathNavigation ground) ground.setCanOpenDoors(true);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.2f);
        if (!level.isClientSide()) this.setCombatTask();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PechTradeGoal(this));
        this.goalSelector.addGoal(3, new PechPickupGoal(this));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.5));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
        builder.define(ANGER, 0);
        builder.define(TAMED, false);
    }

    public int pechType() {
        return this.entityData.get(TYPE);
    }

    public void setPechType(int type) {
        this.entityData.set(TYPE, (byte) type);
    }

    public int anger() {
        return this.entityData.get(ANGER);
    }

    public void setAnger(int anger) {
        this.entityData.set(ANGER, anger);
    }

    public boolean isTamed() {
        return this.entityData.get(TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(TAMED, tamed);
    }

    /** O {@code getCommandSenderName}: o nome pelo tipo. */
    @Override
    protected Component getTypeName() {
        return switch (this.pechType()) {
            case MAGE -> Component.translatable("entity.thaumcraft.pech.mage");
            case STALKER -> Component.translatable("entity.thaumcraft.pech.stalker");
            default -> super.getTypeName();
        };
    }

    /** O {@code setCurrentItemOrArmor}: trocou o que tem na mão, reescolhe a briga. */
    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide() && slot == EquipmentSlot.MAINHAND) this.updateAINextTick = true;
    }

    /** O {@code addRandomArmor}: um em vinte de cada coisa na mão. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        switch (random.nextInt(20)) {
            case 0, 12 -> {
                ItemStack wand = new ItemStack(TCItems.WAND);
                wand.set(net.thaumcraft.registry.TCComponents.WAND_FOCUS, "pech");
                AspectList vis = new AspectList()
                        .add(Aspects.EARTH, (2 + random.nextInt(6)) * 100)
                        .add(Aspects.ENTROPY, (2 + random.nextInt(6)) * 100)
                        .add(Aspects.WATER, (2 + random.nextInt(6)) * 100)
                        .add(Aspects.AIR, random.nextInt(4) * 100)
                        .add(Aspects.FIRE, random.nextInt(4) * 100)
                        .add(Aspects.ORDER, random.nextInt(4) * 100);
                wand.set(net.thaumcraft.registry.TCComponents.WAND_VIS, vis);
                this.setItemSlot(EquipmentSlot.MAINHAND, wand);
            }
            case 1 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            case 2, 4, 10, 11, 13 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            case 3 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
            case 5 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            case 6 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            case 7 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            case 8 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_PICKAXE));
            case 9 -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
            default -> {
            }
        }
    }

    /** O {@code onSpawnWithEgg}: a varinha faz o mago, o arco faz o caçador. */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, difficulty, reason, data);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        ItemStack held = this.getMainHandItem();
        if (held.is(TCItems.WAND)) {
            this.setPechType(MAGE);
            this.setDropChance(EquipmentSlot.MAINHAND, 0.1f);
        } else if (!held.isEmpty()) {
            if (held.is(Items.BOW)) this.setPechType(STALKER);
            this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        }
        this.setCanPickUpLoot(level.getRandom().nextFloat() < 0.75f * difficulty.getSpecialMultiplier());
        this.setCombatTask();
        return data;
    }

    /** O {@code getCanSpawnHere}: em bioma mágico (menos a Terra Maculada), com menos de quatro por perto. */
    public static boolean checkSpawn(EntityType<PechEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
                                     RandomSource random) {
        var biome = level.getBiome(pos);
        boolean magical = biome.is(net.thaumcraft.block.ManaPodBlock.MAGICAL) && !biome.is(net.thaumcraft.world.TCBiomes.TAINTED_LAND);
        if (!magical) return false;
        if (level.getEntitiesOfClass(PechEntity.class, new AABB(pos).inflate(16.0)).size() >= 4) return false;
        return checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("PechType", (byte) this.pechType());
        output.putShort("Anger", (short) this.anger());
        output.putBoolean("Tamed", this.isTamed());
        for (int i = 0; i < this.loot.size(); i++) {
            if (!this.loot.get(i).isEmpty()) output.store("Loot" + i, ItemStack.CODEC, this.loot.get(i));
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPechType(input.getByteOr("PechType", (byte) 0));
        this.setAnger(input.getShortOr("Anger", (short) 0));
        this.setTamed(input.getBooleanOr("Tamed", false));
        for (int i = 0; i < this.loot.size(); i++) this.loot.set(i, input.read("Loot" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        this.updateAINextTick = true;
    }

    /** O {@code canDespawn}: com cinco ou mais coisas na mochila, ele fica. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        int count = 0;
        for (ItemStack stack : this.loot) if (!stack.isEmpty()) count++;
        return count < 5 && super.removeWhenFarAway(distance);
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    /**
     * O {@code dropFewItems} e o {@code dropRareDrop}: quase tudo da mochila, feijões de mana de um primordial, às vezes
     * uma moeda de ouro; e, raro, um fragmento de conhecimento.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = source.getEntity() instanceof LivingEntity killer
                ? net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer.getMainHandItem()) : 0;
        for (ItemStack stack : this.loot) {
            if (!stack.isEmpty() && level.getRandom().nextFloat() < 0.88f) this.spawnAtLocation(level, stack.copy(), 1.5f);
        }
        List<net.thaumcraft.api.aspects.Aspect> primals = Aspects.primals();
        for (int a = 0; a < 1 + looting; a++) {
            if (this.random.nextBoolean()) this.spawnAtLocation(level, ManaBeanItem.of(primals.get(this.random.nextInt(primals.size()))), 1.5f);
        }
        if (level.getRandom().nextInt(10) < 1 + looting) this.spawnAtLocation(level, new ItemStack(TCResources.get("gold_coin")), 1.5f);
        if (recentlyHit && this.random.nextInt(200) - looting < 5) {
            this.spawnAtLocation(level, new ItemStack(TCResources.get("knowledge_fragment")), 1.5f);
        }
    }

    // ---------------------------------------------------------------------------------------------- sons e caras

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            this.mumble = (float) Math.PI;
        } else if (id == 17) {
            this.mumble = (float) (Math.PI * 2);
        } else if (id == 18) {
            this.villagerParticles(ParticleTypes.HAPPY_VILLAGER);
        }
        if (id == 19) {
            this.villagerParticles(ParticleTypes.ANGRY_VILLAGER);
            this.mumble = (float) (Math.PI * 2);
        } else if (id < 16 || id > 18) {
            super.handleEntityEvent(id);
        }
    }

    private void villagerParticles(ParticleOptions particle) {
        for (int i = 0; i < 5; i++) {
            this.level().addParticle(particle, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                    this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                    this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                    this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
        }
    }

    /** O {@code playLivingSound}: uma vez em três, com outro pech perto, ele "negocia"; senão, resmunga. */
    @Override
    public void playAmbientSound() {
        if (this.level() instanceof ServerLevel level) {
            if (this.random.nextInt(3) == 0) {
                for (Entity other : level.getEntities(this, this.getBoundingBox().inflate(4.0, 2.0, 4.0))) {
                    if (other instanceof PechEntity) {
                        level.broadcastEntityEvent(this, (byte) 17);
                        this.playSound(TCSounds.PECH_TRADE.value(), this.getSoundVolume(), this.getVoicePitch());
                        return;
                    }
                }
            }
            level.broadcastEntityEvent(this, (byte) 16);
        }
        super.playAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.PECH_IDLE.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.PECH_HIT.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.PECH_DEATH.value();
    }

    // ---------------------------------------------------------------------------------------------- a briga

    /** O {@code setCombatTask}: arco, varinha ou soco; e, se não é manso, foge de gente. */
    public void setCombatTask() {
        this.goalSelector.removeGoal(this.meleeAttack);
        this.goalSelector.removeGoal(this.arrowAttack);
        this.goalSelector.removeGoal(this.blastAttack);
        ItemStack held = this.getMainHandItem();
        if (held.is(Items.BOW)) this.goalSelector.addGoal(2, this.arrowAttack);
        else if (held.is(TCItems.WAND)) this.goalSelector.addGoal(2, this.blastAttack);
        else this.goalSelector.addGoal(2, this.meleeAttack);
        this.goalSelector.removeGoal(this.avoidPlayer);
        if (!this.isTamed()) this.goalSelector.addGoal(4, this.avoidPlayer);
    }

    /** O {@code attackEntityWithRangedAttack}: a flecha do caçador ou a rajada do mago. */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.pechType() == STALKER) {
            ItemStack weapon = this.getMainHandItem();
            ItemStack arrowStack = new ItemStack(Items.ARROW);
            AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, power, weapon);
            double dx = target.getX() - this.getX();
            double dy = target.getY(0.3333333333333333) - arrow.getY();
            double dz = target.getZ() - this.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            arrow.setBaseDamage(power * 2.0f + this.random.nextGaussian() * 0.25 + level.getDifficulty().getId() * 0.11f);
            arrow.shoot(dx, dy + d * 0.2, dz, 1.6f, 14 - level.getDifficulty().getId() * 4);
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            level.addFreshEntity(arrow);
        } else if (this.pechType() == MAGE) {
            PechBlastEntity blast = new PechBlastEntity(level, this, 1, 0, this.random.nextFloat() < 0.1f);
            double dx = target.getX() + target.getDeltaMovement().x - this.getX();
            double dy = target.getY() + target.getEyeHeight() - 1.5 - this.getY();
            double dz = target.getZ() + target.getDeltaMovement().z - this.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            blast.shoot(dx, dy + d * 0.1f, dz, 1.5f, 4.0f);
            this.playSound(TCSounds.ICE.value(), 0.4f, 1.0f + this.random.nextFloat() * 0.1f);
            level.addFreshEntity(blast);
        }
        this.swing(InteractionHand.MAIN_HAND);
    }

    /** O {@code becomeAngryAt}: bravo de 400 a 800 tiques, e deixa de ser manso. */
    private void becomeAngryAt(Entity attacker) {
        if (this.anger() <= 0 && this.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(this, (byte) 19);
            this.playSound(TCSounds.PECH_CHARGE.value(), this.getSoundVolume(), this.getVoicePitch());
        }
        if (attacker instanceof LivingEntity living) this.setTarget(living);
        this.setAnger(400 + this.random.nextInt(400));
        this.setTamed(false);
        this.updateAINextTick = true;
    }

    /** O {@code getTotalArmorValue}: dois a mais, até vinte. */
    @Override
    public int getArmorValue() {
        return Math.min(20, super.getArmorValue() + 2);
    }

    /** Quem fere um pech arruma briga com todos os de perto (32 para os lados, 16 para cima e para baixo). */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            for (Entity other : level.getEntities(this, this.getBoundingBox().inflate(32.0, 16.0, 32.0))) {
                if (other instanceof PechEntity pech) pech.becomeAngryAt(attacker);
            }
            this.becomeAngryAt(attacker);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        if (this.mumble > 0.0f) this.mumble *= 0.75f;
        if (this.anger() > 0) this.setAnger(this.anger() - 1);
        if (!this.level().isClientSide() && this.anger() > 0 && this.getTarget() == null && this.level() instanceof ServerLevel level) {
            // o findPlayerToAttack do EntityMob: o jogador vulnerável mais perto, a dezesseis
            Player player = level.getNearestPlayer(TargetingConditions.forCombat().range(16.0), this);
            this.setTarget(player);
            if (player != null) {
                if (this.chargeCount > 0) this.chargeCount--;
                if (this.chargeCount == 0) {
                    this.chargeCount = 100;
                    this.playSound(TCSounds.PECH_CHARGE.value(), this.getSoundVolume(), this.getVoicePitch());
                }
                level.broadcastEntityEvent(this, (byte) 17);
            }
        }
        if (this.level().isClientSide()) {
            if (this.random.nextInt(15) == 0 && this.anger() > 0) this.oneVillagerParticle(ParticleTypes.ANGRY_VILLAGER);
            if (this.random.nextInt(25) == 0 && this.isTamed()) this.oneVillagerParticle(ParticleTypes.HAPPY_VILLAGER);
        }
        super.tick();
    }

    private void oneVillagerParticle(ParticleOptions particle) {
        this.level().addParticle(particle, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0f - this.getBbWidth(),
                this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
    }

    /** O {@code updateAITasks}: reescolhe a briga quando precisa, e recupera um de vida a cada dois segundos. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.updateAINextTick) {
            this.updateAINextTick = false;
            this.setCombatTask();
        }
        super.customServerAiStep(level);
        if (this.tickCount % 40 == 0) this.heal(1.0f);
    }

    // ---------------------------------------------------------------------------------------------- o valor das coisas

    /** O {@code valuedItems}: o que o pech acha de valor, e quanto. */
    private static Map<net.minecraft.world.item.Item, Integer> valued() {
        return Map.of(TCItems.MANA_BEAN, 1, Items.GOLD_INGOT, 2, Items.GOLDEN_APPLE, 2, Items.ENDER_PEARL, 3, Items.DIAMOND, 4,
                Items.EMERALD, 5);
    }

    /** O {@code isValued}: da lista, ou qualquer coisa com Lucrum. */
    public boolean isValued(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return valued().containsKey(stack.getItem()) || ObjectAspects.of(stack).getAmount(Aspects.GREED) > 0;
    }

    /** O {@code getValue}: o da lista, ou o Lucrum que a coisa tem (até 32). */
    public int getValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Integer value = valued().get(stack.getItem());
        if (value != null) return value;
        return Math.min(32, ObjectAspects.of(stack).getAmount(Aspects.GREED));
    }

    /** O {@code canPickup}: o de valor (se ainda não é manso) ou o que couber na mochila. */
    public boolean canPickup(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!this.isTamed() && valued().containsKey(stack.getItem())) return true;
        for (int a = 0; a < this.loot.size(); a++) {
            ItemStack slot = this.loot.get(a);
            if (slot.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(stack, slot) && stack.getCount() + slot.getCount() <= slot.getMaxStackSize()) return true;
        }
        return false;
    }

    /**
     * O {@code pickupItem}: o de valor ele come — e, se o valor ganhar do sorteio de dez, fica manso; o resto vai para
     * a mochila. Devolve o que sobrou.
     */
    public ItemStack pickupItem(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        if (!this.isTamed() && this.isValued(stack)) {
            if (this.random.nextInt(10) < this.getValue(stack)) {
                this.setTamed(true);
                this.updateAINextTick = true;
                if (this.level() instanceof ServerLevel level) level.broadcastEntityEvent(this, (byte) 18);
            }
            stack.shrink(1);
            return stack;
        }
        for (int a = 0; a < this.loot.size() && !stack.isEmpty(); a++) {
            ItemStack slot = this.loot.get(a);
            if (!slot.isEmpty() && slot.getCount() < slot.getMaxStackSize() && ItemStack.isSameItemSameComponents(stack, slot)) {
                int moved = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(moved);
                stack.shrink(moved);
            }
        }
        for (int a = 0; a < this.loot.size() && !stack.isEmpty(); a++) {
            if (this.loot.get(a).isEmpty()) {
                this.loot.set(a, stack.copy());
                stack.setCount(0);
            }
        }
        return stack;
    }

    /** O {@code interact}: manso, abre a troca (menos agachado ou com etiqueta na mão). */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() || player.getItemInHand(hand).is(Items.NAME_TAG)) return InteractionResult.PASS;
        if (this.isTamed()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer server) {
                server.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<Integer>() {
                    @Override
                    public Integer getScreenOpeningData(net.minecraft.server.level.ServerPlayer p) {
                        return PechEntity.this.getId();
                    }

                    @Override
                    public Component getDisplayName() {
                        return PechEntity.this.getDisplayName();
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory,
                                                                                          Player p) {
                        return new net.thaumcraft.inventory.PechMenu(id, inventory, PechEntity.this);
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // ---------------------------------------------------------------------------------------------- as metas próprias

    /** O {@code AIPechTradePlayer}: trocando, ele para quieto. */
    static class PechTradeGoal extends Goal {
        private final PechEntity pech;

        PechTradeGoal(PechEntity pech) {
            this.pech = pech;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.pech.isAlive() && !this.pech.isInWater() && this.pech.isTamed() && this.pech.onGround() && !this.pech.hurtMarked
                    && this.pech.trading;
        }

        @Override
        public void start() {
            this.pech.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.pech.trading = false;
        }
    }

    /**
     * O {@code AIPechItemEntityGoto}: a cada cinco tiques, o item mais perto (a dezesseis) que ele pode pegar — menos o
     * que ele mesmo largou na troca; chega a um bloco e meio e pega.
     */
    static class PechPickupGoal extends Goal {
        private final PechEntity pech;
        @Nullable
        private ItemEntity target;
        private int count;
        private int failedPathFindingPenalty;

        PechPickupGoal(PechEntity pech) {
            this.pech = pech;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.pech.tickCount % 5 > 0) return false;
            if (--this.count > 0) return false;
            double range = Double.MAX_VALUE;
            this.target = null;
            for (ItemEntity item : this.pech.level().getEntitiesOfClass(ItemEntity.class, this.pech.getBoundingBox().inflate(16.0))) {
                if (!this.pech.canPickup(item.getItem()) || PechDrops.isPechDrop(item)) continue;
                double distance = item.distanceToSqr(this.pech);
                if (distance < range && distance <= 256.0) {
                    range = distance;
                    this.target = item;
                }
            }
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && !this.pech.getNavigation().isDone()
                    && this.target.distanceToSqr(this.pech) < 256.0;
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public void start() {
            this.pech.getNavigation().moveTo(this.target, 1.5);
            this.count = 0;
        }

        @Override
        public void tick() {
            if (this.target == null) return;
            this.pech.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
            if (this.pech.getSensing().hasLineOfSight(this.target) && --this.count <= 0) {
                this.count = this.failedPathFindingPenalty + 4 + this.pech.getRandom().nextInt(4);
                this.pech.getNavigation().moveTo(this.target, 1.5);
                var path = this.pech.getNavigation().getPath();
                if (path != null && path.getEndNode() != null
                        && this.target.distanceToSqr(path.getEndNode().x, path.getEndNode().y, path.getEndNode().z) < 1.0) {
                    this.failedPathFindingPenalty = 0;
                } else {
                    this.failedPathFindingPenalty += 10;
                }
            }
            if (this.pech.distanceToSqr(this.target.getX(), this.target.getBoundingBox().minY, this.target.getZ()) <= 1.5) {
                this.count = 0;
                ItemStack stack = this.target.getItem().copy();
                int before = stack.getCount();
                ItemStack rest = this.pech.pickupItem(stack);
                if (!rest.isEmpty()) this.target.setItem(rest);
                else this.target.discard();
                if (rest.isEmpty() || rest.getCount() != before) {
                    this.target.playSound(SoundEvents.ITEM_PICKUP, 0.2f,
                            ((this.target.getRandom().nextFloat() - this.target.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                }
            }
        }
    }

    /** O que o pech largou da troca não volta para ele: o original marcava com o dono "PechDrop". */
    public static final class PechDrops {
        private PechDrops() {
        }

        public static void mark(ItemEntity item) {
            item.addTag("PechDrop");
        }

        public static boolean isPechDrop(ItemEntity item) {
            return item.entityTags().contains("PechDrop");
        }
    }

}
