package net.thaumcraft.entity.eldritch;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.thaumcraft.registry.TCItems;

/**
 * O cavaleiro carmesim: o {@code EntityCultistKnight} da 4.2.3.5. Trinta e seis de vida, a armadura de placas dos
 * cultistas e uma espada de ferro — raramente (um em cem; cinco em cem no difícil) uma de táumio, ou uma do vazio com o
 * capuz de robe no lugar do elmo.
 */
public class CultistKnightEntity extends CultistEntity {
    public CultistKnightEntity(EntityType<? extends CultistKnightEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return CultistEntity.attributes().add(Attributes.MAX_HEALTH, 36.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.0, false));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CultistGoals.HurtByTarget(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CULTIST_PLATE_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CULTIST_PLATE_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CULTIST_PLATE_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CULTIST_BOOTS));
        if (random.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.05f : 0.01f)) {
            int i = random.nextInt(5);
            if (i == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TCItems.GEAR.get("void_sword")));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CULTIST_ROBE_HELMET));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TCItems.GEAR.get("thaumium_sword")));
                if (random.nextBoolean()) this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        this.enchantHeld(level, random, difficulty, 0.25f, 5, 18);
    }
}
