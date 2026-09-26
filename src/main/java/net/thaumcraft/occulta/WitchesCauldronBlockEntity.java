package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * O miolo do Caldeirão da Bruxa: o {@code TileEntityCauldron} do Witchery.
 *
 * <p>Ele guarda três coisas: a <b>água</b> (três baldes, como o tanque de três mil do original), há quanto tempo
 * está ao <b>fogo</b>, e o que já foi <b>jogado dentro</b>. Cheio e com fogo embaixo, ferve em cinco segundos; a
 * partir daí engole o que cai nele.
 *
 * <p>Quando cai dentro a coisa que <b>dispara</b> uma receita e o que está dentro bate com ela, o caldeirão leva
 * três segundos a remexer e então larga o que a receita faz, e esvazia.
 *
 * <p><b>Do original fica de fora, por agora,</b> tudo o que o ritual dele consulta e que ainda não existe aqui: o
 * poder do altar, os círculos de giz, o coven de bruxas em volta, a distância a que quem joga está e a má sorte
 * que cai sobre quem erra. O que sobra é a espera e o resultado, que é o que se vê de fora.
 */
public class WitchesCauldronBlockEntity extends BlockEntity {
    /** O tanque do original: três baldes. */
    public static final int BUCKET = 1000;
    public static final int FULL = 3000;

    /** Cinco segundos ao fogo até ferver, e três remexendo até a receita sair. */
    public static final int TICKS_TO_BOIL = 100;
    public static final int RITUAL_TICKS = 60;

    /** A cor da água parada, que é a do original quando não há nada dentro. */
    public static final int PLAIN_COLOR = 0x345F5A;

    private int water;
    private int heated;
    private int ritual;
    private final List<Item> inside = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;

    public WitchesCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.WITCHES_CAULDRON_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ o que ele tem

    public int water() {
        return this.water;
    }

    public boolean isFull() {
        return this.water >= FULL;
    }

    /** O quanto do caldeirão está cheio, de zero a um. */
    public float filled() {
        return this.water / (float) FULL;
    }

    public boolean isBoiling() {
        return this.heated >= TICKS_TO_BOIL;
    }

    public boolean isRitualInProgress() {
        return this.ritual > 0;
    }

    public List<Item> inside() {
        return List.copyOf(this.inside);
    }

    /**
     * A cor do que está dentro: a mistura das cores das coisas jogadas, como o {@code augmentColor} do original —
     * que mistura meio a meio, uma a uma.
     */
    public int color() {
        if (this.inside.isEmpty()) return PLAIN_COLOR;
        int cor = PLAIN_COLOR;
        for (Item item : this.inside) cor = mix(cor, colorOf(item));
        return cor;
    }

    private static int mix(int a, int b) {
        int r = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF)) / 2;
        int g = (((a >> 8) & 0xFF) + ((b >> 8) & 0xFF)) / 2;
        int azul = ((a & 0xFF) + (b & 0xFF)) / 2;
        return (r << 16) | (g << 8) | azul;
    }

    /** Uma cor para cada coisa, tirada do nome dela: o original guarda uma por item, e aqui vale a mesma ideia. */
    private static int colorOf(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        int hash = id.toString().hashCode();
        return hash & 0xFFFFFF;
    }

    // ------------------------------------------------------------------ o que se faz com ele

    /** Enche um tanto; devolve se coube alguma coisa. */
    public boolean fill(int quanto) {
        if (this.water >= FULL) return false;
        this.water = Math.min(FULL, this.water + quanto);
        this.changed();
        return true;
    }

    /** Esvazia, e perde o que estava dentro. */
    public void empty() {
        this.water = 0;
        this.heated = 0;
        this.ritual = 0;
        this.inside.clear();
        this.result = ItemStack.EMPTY;
        this.changed();
    }

    /**
     * O {@code addItem}: a coisa entra na panela se servir para alguma receita — e, se for a que dispara e o que
     * está dentro bater, começa a mexer.
     */
    public boolean addItem(ItemStack caiu) {
        if (!this.isBoiling() || this.isRitualInProgress()) return false;
        Item item = caiu.getItem();
        ItemStack sai = OccultaRituals.result(item, this.inside);
        if (!sai.isEmpty()) {
            this.result = sai;
            this.ritual = RITUAL_TICKS;
            this.inside.add(item);
            this.changed();
            return true;
        }
        if (!OccultaRituals.isIngredient(item)) return false;
        this.inside.add(item);
        this.changed();
        return true;
    }

    /** Quebrado, larga no chão o que estava dentro. */
    public void spill(ServerLevel level, BlockPos pos) {
        for (Item item : this.inside) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(item)));
        }
        this.inside.clear();
    }

    // ------------------------------------------------------------------ o que ele faz sozinho

    /** O {@code updateEntity}: o fogo embaixo, a fervura e o remexer da receita. */
    public static void tick(Level level, BlockPos pos, BlockState state, WitchesCauldronBlockEntity caldeirão) {
        boolean fogo = level.getBlockState(pos.below()).is(Blocks.FIRE);
        if (fogo && caldeirão.isFull()) {
            if (caldeirão.heated < TICKS_TO_BOIL) {
                caldeirão.heated++;
                if (caldeirão.heated == TICKS_TO_BOIL) caldeirão.changed();
            }
        } else if (caldeirão.heated > 0) {
            caldeirão.heated = 0;
            caldeirão.changed();
        }

        if (caldeirão.ritual <= 0) return;
        caldeirão.ritual--;
        if (caldeirão.ritual > 0) return;

        // acabou de mexer: sai o que a receita faz, e o caldeirão esvazia
        ItemStack sai = caldeirão.result.copy();
        caldeirão.empty();
        if (sai.isEmpty() || !(level instanceof ServerLevel server)) return;
        ItemEntity saiu = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, sai);
        saiu.setDeltaMovement(0.0, 0.2, 0.0);
        server.addFreshEntity(saiu);
        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                12, 0.3, 0.3, 0.3, 0.0);
        server.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void changed() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    // ------------------------------------------------------------------ o que ele guarda

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.water = input.getIntOr("Water", 0);
        this.heated = input.getIntOr("Heated", 0);
        this.ritual = input.getIntOr("Ritual", 0);
        this.inside.clear();
        for (String nome : input.read("Inside", com.mojang.serialization.Codec.STRING.listOf())
                .orElse(List.of())) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(nome));
            if (item != null) this.inside.add(item);
        }
        this.result = input.read("Result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Water", this.water);
        output.putInt("Heated", this.heated);
        output.putInt("Ritual", this.ritual);
        List<String> nomes = new ArrayList<>();
        for (Item item : this.inside) nomes.add(BuiltInRegistries.ITEM.getKey(item).toString());
        output.store("Inside", com.mojang.serialization.Codec.STRING.listOf(), nomes);
        if (!this.result.isEmpty()) output.store("Result", ItemStack.CODEC, this.result);
    }

    /** O que o caldeirão manda ao cliente, para as bolhas saírem na cor certa. */
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
