package net.thaumcraft.naturalis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectContainer;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.EssentiaSources;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.world.BiomeAura;
import org.jetbrains.annotations.Nullable;

/**
 * O Geo-Pilone: o {@code GeoPylonBlockEntity} do Magia Naturalis 0.5.0.
 *
 * <p>Em pé sobre um vão e três totens de obsidiana, e alimentado de essência pelos canos, ele reescreve a terra
 * num círculo de oito blocos em volta — um pedaço por vez, cobrando pela terra nova o que ela carrega de aura.
 * A varinha o liga e o desliga; agachado, ela recomeça a cobrança.
 */
public class GeoPylonBlockEntity extends BlockEntity implements AspectContainer, Wandable {
    /** O raio que ele reescreve: o oito que o original passava ao {@code handleBiomeMorphing}. */
    public static final int RADIUS = 8;
    /** Quantos blocos de cano ele alcança para beber. */
    private static final int REACH = 12;

    public int ticks;
    /** A terra que o amostrador lhe apontou. */
    private @Nullable ResourceKey<Biome> target;
    public boolean idle = true;
    private int morphX;
    private int morphZ;
    private @Nullable ResourceKey<Biome> lastTarget;
    private AspectList morphCost = new AspectList();
    private AspectList realCost = new AspectList();

    public GeoPylonBlockEntity(BlockPos pos, BlockState state) {
        super(NaturalisBlocks.GEO_PYLON_ENTITY, pos, state);
    }

    public @Nullable ResourceKey<Biome> target() {
        return this.target;
    }

    public void target(@Nullable ResourceKey<Biome> biome) {
        this.target = biome;
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean idle() {
        return this.idle;
    }

    // ------------------------------------------------------------------ a obra

    public static void tick(Level level, BlockPos pos, BlockState state, GeoPylonBlockEntity pylon) {
        if (pylon.idle || ++pylon.ticks % 5 != 0) return;
        boolean update;
        if (pylon.standing(level, pos)) {
            update = pylon.morph((ServerLevel) level, pos);
        } else {
            // a estrutura caiu: ele pára e volta um passo, como no original
            pylon.idle = true;
            pylon.realCost = new AspectList();
            update = true;
            if (pylon.morphX == 0) {
                pylon.morphZ--;
                pylon.morphX = 1024;
            } else {
                pylon.morphX--;
            }
        }
        if (update) {
            level.sendBlockUpdated(pos, state, state, 3);
            pylon.setChanged();
        }
    }

    /** O {@code validateStructure}: um vão embaixo e três totens de obsidiana abaixo dele. */
    public static boolean standing(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isAir()) return false;
        for (int i = 1; i <= 3; i++) {
            if (!level.getBlockState(pos.below(1 + i)).is(TCBlocks.OBSIDIAN_TOTEM)) return false;
        }
        return true;
    }

    /** O {@code handleBiomeMorphing}: bebe o que falta e, quando a conta fecha, reescreve um pedaço de terra. */
    private boolean morph(ServerLevel level, BlockPos pos) {
        if (this.target == null) return false;
        boolean update = false;
        boolean done;
        if (this.realCost.visSize() > 0) {
            for (Aspect aspect : this.realCost.getAspects()) {
                if (this.realCost.getAmount(aspect) > 0
                        && EssentiaSources.drain(this, aspect, null, REACH)) {
                    this.realCost.reduce(aspect, 1);
                    this.setChanged();
                    break;
                }
            }
            done = this.realCost.visSize() <= 0;
            if (done) {
                update = true;
                this.realCost = new AspectList();
                this.write(level, pos);
            }
        } else {
            done = true;
        }
        if (!done || this.ticks % 20 != 0) return update;
        // o passeio do original: uma coluna por vez, linha por linha, dentro do círculo
        if (this.morphZ >= RADIUS * 2) {
            this.morphZ = 0;
            this.idle = true;
            return true;
        }
        if (this.morphX < RADIUS * 2) {
            this.morphX++;
        } else {
            this.morphX = 0;
            this.morphZ++;
        }
        int x = pos.getX() - RADIUS + this.morphX;
        int z = pos.getZ() - RADIUS + this.morphZ;
        Holder<Biome> old = level.getBiome(new BlockPos(x, pos.getY(), z));
        boolean mesma = this.target != null && old.is(this.target);
        double distance = Math.sqrt((double) (this.morphX - RADIUS) * (this.morphX - RADIUS)
                + (double) (this.morphZ - RADIUS) * (this.morphZ - RADIUS));
        if (!mesma && distance <= RADIUS) {
            if (this.lastTarget != this.target) {
                this.lastTarget = this.target;
                this.morphCost = cost(level, this.target);
            }
            this.realCost = this.morphCost.copy();
            return true;
        }
        return update;
    }

    /** Reescreve a coluna de terra em que o passeio está agora. */
    private void write(ServerLevel level, BlockPos pos) {
        if (this.target == null) return;
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).get(this.target).orElse(null);
        if (biome == null) return;
        int x = pos.getX() - RADIUS + this.morphX;
        int z = pos.getZ() - RADIUS + this.morphZ;
        net.minecraft.server.commands.FillBiomeCommand.fill(level,
                new BlockPos(x, level.getMinY(), z), new BlockPos(x, level.getMaxY(), z), biome);
        net.thaumcraft.net.TCNetwork.blockSparkle(level, pos, biome.value().getFoliageColor());
    }

    /** O {@code calculateMorphCost}: cada marca da terra nova cobra dois por cento da aura dela. */
    public static AspectList cost(ServerLevel level, @Nullable ResourceKey<Biome> key) {
        AspectList list = new AspectList();
        if (key == null) return list;
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).get(key).orElse(null);
        if (biome == null) return list;
        for (BiomeAura.Land land : BiomeAura.LANDS) {
            if (!biome.is(land.tag())) continue;
            if (land.aspect() == null) {
                // a terra mágica não tem aspecto de marca: o original cobra dois de Magia
                list.add(Aspects.MAGIC, 2);
            } else {
                list.add(land.aspect(), Math.round(land.aura() * 2.0f / 100.0f));
            }
        }
        return list;
    }

    // ------------------------------------------------------------------ a varinha

    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, Direction face) {
        if (level.isClientSide()) return true;
        if (player.isShiftKeyDown()) {
            this.realCost = level instanceof ServerLevel server ? cost(server, this.target) : new AspectList();
        } else {
            this.idle = !this.idle;
        }
        level.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 3);
        this.setChanged();
        return true;
    }

    // ------------------------------------------------------------------ o que ele mostra de essência

    @Override
    public AspectList getAspects() {
        return this.realCost;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }

    // ------------------------------------------------------------------ o que fica gravado

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.morphX = input.getIntOr("morphX", 0);
        this.morphZ = input.getIntOr("morphZ", 0);
        this.idle = input.getBooleanOr("idle", true);
        this.realCost = input.read("cost", AspectList.CODEC).orElseGet(AspectList::new);
        this.target = input.getString("biome").map(name ->
                ResourceKey.create(Registries.BIOME, Identifier.parse(name))).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("morphX", this.morphX);
        output.putInt("morphZ", this.morphZ);
        output.putBoolean("idle", this.idle);
        output.store("cost", AspectList.CODEC, this.realCost);
        if (this.target != null) output.putString("biome", this.target.identifier().toString());
    }
}
