package net.thaumcraft.forbidden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.EssentiaTransport;
import org.jetbrains.annotations.Nullable;

/**
 * A Gaiola da Ira: o {@code TileEntityWrathCage} do Forbidden Magic 0.575.
 *
 * <p>Um gerador de monstros que não nasce do mundo: ele é afinado com um cristal marcado e <b>come essência</b>
 * para trabalhar. Guarda até sessenta e quatro de três coisas — a essência do próprio bicho, Ira e Desídia — e
 * cada cinco delas rendem quatro bichos. O modo, que o Garfo do Diabolista troca, diz qual das três ele puxa.
 */
public class WrathCageBlockEntity extends BlockEntity implements EssentiaTransport {
    /** O {@code wrathCost} e o {@code wrathEff} do original: cinco de essência por quatro bichos. */
    public static final int COST = 5;
    public static final int EFFICIENCY = 4;
    public static final int CAPACITY = 64;

    /** Os três modos do original, na ordem em que o garfo os troca. */
    public static final int MODE_SPECIAL = 0, MODE_WRATH = 1, MODE_SLOTH = 2;

    private @Nullable Identifier mob;
    private int special;
    private int wrath;
    private int sloth;
    private int mode = MODE_SPECIAL;
    private int fuel;
    private int spawnDelay = 20;
    /** Comendo Desídia ele trabalha devagar, como no original. */
    private boolean slothful;

    public WrathCageBlockEntity(BlockPos pos, BlockState state) {
        super(ForbiddenBlocks.WRATH_CAGE_ENTITY, pos, state);
    }

    // ----------------------------------------------------------------- o bicho

    public @Nullable Identifier mob() {
        return this.mob;
    }

    public boolean isSet() {
        return this.mob != null;
    }

    /** Afina a gaiola com um bicho e devolve o que ela tinha, para voltar ao cristal de quem a afinou. */
    public @Nullable Identifier attune(Identifier novo) {
        Identifier antes = this.mob;
        this.mob = novo;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        return antes;
    }

    /** A essência que este bicho pede. */
    public Aspect aspect() {
        return WrathMobs.aspectOf(this.mob);
    }

    public int mode() {
        return this.mode;
    }

    /** O garfo troca o modo, que é qual das três essências ela puxa. */
    public void cycleMode() {
        this.mode = (this.mode + 1) % 3;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public int stored(int which) {
        return switch (which) {
            case MODE_WRATH -> this.wrath;
            case MODE_SLOTH -> this.sloth;
            default -> this.special;
        };
    }

    // ----------------------------------------------------------------- o trabalho

    public static void tick(Level level, BlockPos pos, BlockState state, WrathCageBlockEntity cage) {
        if (!(level instanceof ServerLevel server)) return;
        if (level.hasNeighborSignal(pos)) return;
        if (!cage.isSet()) return;

        if (cage.fuel <= 0) {
            if (!cage.burn()) return;
        }
        if (cage.spawnDelay > 0) {
            cage.spawnDelay--;
            return;
        }
        cage.spawn(server, pos);
    }

    /** O {@code updateSpawner}: cinco de essência viram quatro bichos, do que houver — o especial primeiro. */
    private boolean burn() {
        if (this.special >= COST) {
            this.special -= COST;
            this.slothful = false;
        } else if (this.wrath >= COST) {
            this.wrath -= COST;
            this.slothful = false;
        } else if (this.sloth >= COST) {
            this.sloth -= COST;
            this.slothful = true;
        } else {
            return false;
        }
        this.fuel = EFFICIENCY;
        this.setChanged();
        return true;
    }

    /** Três bichos por vez, num raio de quatro, e nunca mais de seis por perto. */
    private void spawn(ServerLevel level, BlockPos pos) {
        EntityType<?> type = this.mob == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(this.mob);
        if (type == null) return;
        for (int quantos = 0; quantos < 3 && this.fuel > 0; quantos++) {
            AABB perto = new AABB(pos).inflate(8.0, 4.0, 8.0);
            if (level.getEntities(type, perto, bicho -> true).size() >= 6) break;

            double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * 4.0;
            double y = pos.getY() + level.getRandom().nextInt(3) - 1;
            double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * 4.0;
            Entity bicho = type.create(level, EntitySpawnReason.SPAWNER);
            if (bicho == null) return;
            bicho.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0f, 0.0f);
            if (!level.noCollision(bicho)) continue;
            if (bicho instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.SPAWNER, null);
            }
            level.addFreshEntity(bicho);
            level.levelEvent(2004, pos, 0);
            this.fuel--;
        }
        this.spawnDelay = 200 + level.getRandom().nextInt(101) + (this.slothful ? 200 : 0);
        this.setChanged();
    }

    // ----------------------------------------------------------------- a essência

    /** O {@code drawEssentia}: ela puxa dos canos ligados o que o modo pede. */
    public void drawEssentia() {
        if (this.level == null || !this.isSet()) return;
        Aspect quer = switch (this.mode) {
            case MODE_WRATH -> ForbiddenAspects.ASPECTS.get("ira");
            case MODE_SLOTH -> ForbiddenAspects.ASPECTS.get("desidia");
            default -> this.aspect();
        };
        if (quer == null || this.stored(this.mode) >= CAPACITY) return;

        for (Direction face : Direction.values()) {
            if (!(this.level.getBlockEntity(this.worldPosition.relative(face)) instanceof EssentiaTransport ic)) continue;
            if (!ic.isConnectable(face.getOpposite()) || !ic.canOutputTo(face.getOpposite())) continue;
            if (ic.getEssentiaType(face.getOpposite()) != quer || ic.getEssentiaAmount(face.getOpposite()) <= 0) continue;
            if (ic.takeEssentia(quer, 1, face.getOpposite()) != 1) continue;
            switch (this.mode) {
                case MODE_WRATH -> this.wrath++;
                case MODE_SLOTH -> this.sloth++;
                default -> this.special++;
            }
            this.setChanged();
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            return;
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(@Nullable Direction face) {
        return switch (this.mode) {
            case MODE_WRATH -> ForbiddenAspects.ASPECTS.get("ira");
            case MODE_SLOTH -> ForbiddenAspects.ASPECTS.get("desidia");
            default -> this.aspect();
        };
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return this.isSet() && this.stored(this.mode) < CAPACITY ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        Aspect quer = this.getSuctionType(face);
        if (quer != aspect || this.stored(this.mode) >= CAPACITY) return 0;
        switch (this.mode) {
            case MODE_WRATH -> this.wrath++;
            case MODE_SLOTH -> this.sloth++;
            default -> this.special++;
        }
        this.setChanged();
        return 1;
    }

    @Override
    public @Nullable Aspect getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }

    // ----------------------------------------------------------------- o que se guarda

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mob = input.getString("Mob").map(Identifier::parse).orElse(null);
        this.special = input.getIntOr("Special", 0);
        this.wrath = input.getIntOr("Wrath", 0);
        this.sloth = input.getIntOr("Sloth", 0);
        this.mode = input.getIntOr("Mode", MODE_SPECIAL);
        this.fuel = input.getIntOr("Fuel", 0);
        this.spawnDelay = input.getIntOr("Delay", 20);
        this.slothful = input.getBooleanOr("Slothful", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.mob != null) output.putString("Mob", this.mob.toString());
        output.putInt("Special", this.special);
        output.putInt("Wrath", this.wrath);
        output.putInt("Sloth", this.sloth);
        output.putInt("Mode", this.mode);
        output.putInt("Fuel", this.fuel);
        output.putInt("Delay", this.spawnDelay);
        output.putBoolean("Slothful", this.slothful);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
