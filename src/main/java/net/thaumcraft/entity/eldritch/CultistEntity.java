package net.thaumcraft.entity.eldritch;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import org.jetbrains.annotations.Nullable;

/**
 * O cultista carmesim: o {@code EntityCultist} da 4.2.3.5, a base do cavaleiro e do clérigo. Abre portas, não
 * desaparece à toa, e é aliado dos outros cultistas e do pretor. Morto, às vezes deixa um fragmento de conhecimento, uma
 * semente do vazio ou uma moeda; raramente, os ritos carmesins.
 */
public abstract class CultistEntity extends Monster {
    protected CultistEntity(EntityType<? extends CultistEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        if (this.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    /** As moedas, as sementes e os fragmentos do {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        int looting = 0;
        if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity killer) {
            var enchantments = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer.getMainHandItem());
        }
        int r = this.random.nextInt(10);
        if (r == 0) this.spawnAtLocation(level, new ItemStack(TCResources.get("knowledge_fragment")), 1.5f);
        else if (r <= 1) this.spawnAtLocation(level, new ItemStack(TCResources.get("void_seed")), 1.5f);
        else if (r <= 3 + looting) this.spawnAtLocation(level, new ItemStack(TCResources.get("gold_coin")), 1.5f);
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // o dropRareDrop: os ritos carmesins, na conta de raro do jogo de então (nextInt(200) menos a pilhagem abaixo de cinco)
        if (recentlyHit && this.random.nextInt(200) - looting < 5) this.spawnAtLocation(level, new ItemStack(TCItems.CRIMSON_RITES), 1.0f);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    }

    /** O {@code enchantEquipment} dos cultistas: com a chance e a força dadas, encanta a arma na mão. */
    protected void enchantHeld(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty, float chance, int base, int spread) {
        float f = difficulty.getSpecialMultiplier();
        ItemStack held = this.getMainHandItem();
        if (!held.isEmpty() && random.nextFloat() < chance * f) {
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                    random, held, (int) (base + f * random.nextInt(spread)), level.registryAccess(), java.util.Optional.empty()));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    /** Aliado de cultista e de pretor ({@code isOnSameTeam}). */
    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return other instanceof CultistEntity || other instanceof CultistLeaderEntity || super.considersEntityAsAlly(other);
    }
}
