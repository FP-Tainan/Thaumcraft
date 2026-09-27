package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * O que uma nuvem de cozimento carrega: o {@code TileEntityBrewFluid} do Witchery.
 *
 * <p>Ela guarda o cozimento inteiro — a lista do que estava no caldeirão —, a cor, quanto ainda se espalha e
 * quanto tempo dura. Quando a nuvem cresce para o lado, a nova leva uma cópia de tudo isto.
 *
 * <p>A <b>duração</b> e o <b>alcance</b> saem do que se jogou na panela: a flor de beladona e o lápis esticam uma,
 * a cinza de madeira e o cacau alargam o outro.
 */
public class BrewFluidBlockEntity extends BlockEntity {
    /** Depois de tantas batidas a nuvem some, custe o que custar: o {@code runTicks > 120} do original. */
    public static final int MAX_RUN_TICKS = 120;

    private List<Item> dentro = List.of();
    private int color = WitchesCauldronColor.PLAIN;
    private int duration = 100;
    private int expansion = 4;
    private int runTicks;

    public BrewFluidBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.BREW_GAS_ENTITY, pos, state);
    }

    /** O {@code initalise}: a nuvem nasce com o cozimento e com as contas do espalhamento. */
    public void start(List<Item> cozimento, BrewImpact espalha) {
        this.dentro = List.copyOf(cozimento);
        this.color = Brew.color(cozimento);
        this.duration = espalha.lifetime >= 0 ? 5 + espalha.lifetime * espalha.lifetime * 5 : 100;
        this.expansion = Math.min(4 + espalha.extent, 10);
        this.setChanged();
    }

    /** E a nuvem que nasceu de outra leva o mesmo que ela. */
    public void copyFrom(BrewFluidBlockEntity outra) {
        this.dentro = outra.dentro;
        this.color = outra.color;
        this.duration = outra.duration;
        this.expansion = outra.expansion;
        this.setChanged();
    }

    public List<Item> contents() {
        return this.dentro;
    }

    public int color() {
        return this.color;
    }

    public int duration() {
        return this.duration;
    }

    public int expansion() {
        return this.expansion;
    }

    public int tickRun() {
        return ++this.runTicks;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        List<Item> lida = new ArrayList<>();
        for (String nome : input.read("Brew", com.mojang.serialization.Codec.STRING.listOf())
                .orElse(List.of())) {
            net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getOptional(net.minecraft.resources.Identifier.parse(nome)).ifPresent(lida::add);
        }
        this.dentro = List.copyOf(lida);
        this.color = input.getIntOr("Color", WitchesCauldronColor.PLAIN);
        this.duration = input.getIntOr("Duration", 100);
        this.expansion = input.getIntOr("Expansion", 4);
        this.runTicks = input.getIntOr("Run", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<String> nomes = new ArrayList<>();
        for (Item item : this.dentro) {
            nomes.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        }
        output.store("Brew", com.mojang.serialization.Codec.STRING.listOf(), nomes);
        output.putInt("Color", this.color);
        output.putInt("Duration", this.duration);
        output.putInt("Expansion", this.expansion);
        output.putInt("Run", this.runTicks);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>
            getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    /** A cor da água antes de virar cozimento, que é a que a nuvem tem quando não tem cor nenhuma. */
    public static final class WitchesCauldronColor {
        public static final int PLAIN = 0x33A3B3;

        private WitchesCauldronColor() {
        }
    }
}
