package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.golems.GolemTypes;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O golem: o servo de argila, palha ou metal que faz o trabalho chato no lugar de quem o fez.
 *
 * <p>De que ele é feito manda em tudo — vida, carga, força, couro e passo saem da tabela
 * {@link GolemTypes}, gerada do {@code EnumGolemType} do original. O que ele <em>faz</em> é o núcleo
 * encaixado nele: sem núcleo ele só perambula, e com um núcleo ele passa a ter serviço.
 *
 * <p>Ele carrega uma coisa só de cada vez — uma pilha, do tamanho que a matéria dele aguenta — e leva
 * tudo para a casa dele, que é o baú marcado com o sino.
 */
public class GolemEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> MATERIAL =
            SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORE =
            SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> CARRIED =
            SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.ITEM_STACK);

    /** O que ele leva na mão: nada é o normal. */
    @Nullable
    private BlockPos home;
    private int mend;

    public GolemEntity(EntityType<? extends GolemEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new net.thaumcraft.entity.ai.GolemWorkGoal(this));
        // sem núcleo ele não tem serviço, e fica parado esperando — como no original. Um golem que
        // perambula antes de alguém lhe dar o que fazer parece bicho solto, e não construto.
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return GolemEntity.this.core() != null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return GolemEntity.this.core() != null && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MATERIAL, 0);
        builder.define(CORE, -1);
        builder.define(CARRIED, ItemStack.EMPTY);
    }

    // ---- de que ele é feito ----

    public String material() {
        int index = this.entityData.get(MATERIAL);
        var names = GolemTypes.ALL.keySet().toArray(new String[0]);
        return names[Math.floorMod(index, names.length)];
    }

    public GolemTypes.Type type() {
        return GolemTypes.of(this.material());
    }

    /** Diz de que ele é feito e acerta o corpo dele com os números daquela matéria. */
    public void setMaterial(String name) {
        var names = GolemTypes.ALL.keySet().toArray(new String[0]);
        for (int index = 0; index < names.length; index++) {
            if (!names[index].equals(name)) continue;
            this.entityData.set(MATERIAL, index);
            this.applyType();
            return;
        }
    }

    /** Passa os números da tabela para os atributos do bicho. */
    private void applyType() {
        GolemTypes.Type type = this.type();
        if (type == null) return;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(type.health());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(type.speed());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(type.strength());
        // a armadura do original vai até quinze; a do jogo de hoje vai até vinte, e a conta é a mesma
        this.getAttribute(Attributes.ARMOR).setBaseValue(type.armor());
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public boolean fireImmune() {
        GolemTypes.Type type = this.type();
        return type != null && type.fireResist();
    }

    // ---- o que ele faz ----

    /** O núcleo encaixado nele, ou nulo se ele ainda não tem serviço. */
    @Nullable
    public String core() {
        int index = this.entityData.get(CORE);
        if (index < 0 || index >= GolemTypes.CORES.length) return null;
        return GolemTypes.CORES[index];
    }

    public void setCore(@Nullable String name) {
        if (name == null) {
            this.entityData.set(CORE, -1);
            return;
        }
        for (int index = 0; index < GolemTypes.CORES.length; index++) {
            if (GolemTypes.CORES[index].equals(name)) {
                this.entityData.set(CORE, index);
                return;
            }
        }
    }

    /** A casa dele: o baú marcado com o sino, para onde ele leva o que junta. */
    @Nullable
    public BlockPos home() {
        return this.home;
    }

    public void setHome(@Nullable BlockPos at) {
        this.home = at;
    }

    // ---- o que ele leva na mão ----

    public ItemStack carried() {
        return this.entityData.get(CARRIED);
    }

    public void setCarried(ItemStack stack) {
        this.entityData.set(CARRIED, stack);
    }

    /** Quanto ainda cabe na mão dele. */
    public int room() {
        GolemTypes.Type type = this.type();
        int limit = type == null ? 1 : type.carry();
        ItemStack held = this.carried();
        if (held.isEmpty()) return limit;
        return Math.max(0, Math.min(limit, held.getMaxStackSize()) - held.getCount());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        // ele se remenda sozinho, no compasso da matéria de que é feito
        GolemTypes.Type type = this.type();
        if (type == null) return;
        if (++this.mend < type.regenDelay()) return;
        this.mend = 0;
        if (this.getHealth() < this.getMaxHealth()) this.heal(1.0f);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // um núcleo na mão dá serviço a ele; agachado, o serviço sai
        if (stack.getItem() instanceof net.thaumcraft.item.GolemCoreItem core) {
            if (!this.level().isClientSide()) {
                String had = this.core();
                this.setCore(core.core());
                if (!player.getAbilities().instabuild) stack.shrink(1);
                if (had != null) {
                    var old = net.thaumcraft.registry.TCItems.GOLEM_CORES.get(had);
                    if (old != null && !player.getInventory().add(new ItemStack(old))) {
                        player.drop(new ItemStack(old), false);
                    }
                }
                this.level().playSound(null, this.blockPosition(), TCSounds.WAND.value(),
                        SoundSource.NEUTRAL, 0.6f, 1.4f);
            }
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && this.core() != null && stack.isEmpty()) {
            if (!this.level().isClientSide()) {
                var old = net.thaumcraft.registry.TCItems.GOLEM_CORES.get(this.core());
                this.setCore(null);
                if (old != null && !player.getInventory().add(new ItemStack(old))) {
                    player.drop(new ItemStack(old), false);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("material", this.material());
        String core = this.core();
        if (core != null) output.putString("core", core);
        if (this.home != null) {
            output.putInt("home_x", this.home.getX());
            output.putInt("home_y", this.home.getY());
            output.putInt("home_z", this.home.getZ());
        }
        if (!this.carried().isEmpty()) output.store("carried", ItemStack.CODEC, this.carried());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setMaterial(input.getStringOr("material", "straw"));
        String core = input.getStringOr("core", "");
        this.setCore(core.isEmpty() ? null : core);
        if (input.getInt("home_y").isPresent()) {
            this.home = new BlockPos(
                    input.getIntOr("home_x", 0), input.getIntOr("home_y", 0), input.getIntOr("home_z", 0));
        }
        this.setCarried(input.read("carried", ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }
}
