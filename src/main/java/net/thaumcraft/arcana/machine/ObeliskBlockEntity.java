package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.ArcanaEffects;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PowerType;
import net.thaumcraft.arcana.power.PoweredBlockEntity;

/**
 * O <b>Obelisco</b>: o {@code TileEntityObelisk} da 1.4.0.009.
 *
 * <p>A primeira fonte de Etherium, e a mais simples de todas: <b>queima uma coisa e vira energia</b>. Ele
 * só dá <b>Neutro</b>, e só dá enquanto há o que queimar.
 *
 * <p><b>Ele nunca pede nada.</b> Fica cheio à espera de que alguém venha buscar — é o que faz com que
 * ligar a Chave de Cristal se faça sempre dele para a máquina.
 *
 * <p><b>E o que está em volta dele conta.</b> Um anel de nove quadrados de Giz de Mago no chão multiplica
 * o rendimento por <b>1,25</b>; quatro pilares de tijolo de pedra nos cantos, de dois blocos, multiplicam
 * por <b>1,35</b> se tiverem o tijolo cinzelado por cima. As duas coisas somam-se: um obelisco inteiro dá
 * <b>8,4375</b> por batida em vez de 5.
 *
 * <p>E quem trabalhar ao pé de um obelisco a arder, estando ele com <b>um décimo</b> do teto, ganha a
 * <b>Regeneração de Mana</b> por dez minutos.
 */
public class ObeliskBlockEntity extends EssenceGeneratorBlockEntity implements Container, MenuProvider {
    /** Quanto ele guarda: <b>5000</b>. */
    public static final int CAPACITY = 5000;
    /** Quanto ele faz por batida, sem nada em volta: <b>cinco</b>. */
    public static final float POWER_BASE = 5.0f;
    /** O que os pilares com topo cinzelado multiplicam. */
    public static final float CHISELED_CAPS = 1.35f;
    /** Acima de quantas batidas de queima restantes o desenho dele muda para o de alta potência. */
    public static final int HIGH_POWER = 200;
    /** O teto a partir do qual ele deixa de acender combustível novo: três quartos. */
    public static final float CHARGE_CEILING = 0.75f;

    private static final Multiblock FIGURA = new Multiblock("obelisk_structure");
    private static final Multiblock.Group PILARES = FIGURA.createGroup("pillars", MUTEX_PILLARS);
    private static final Multiblock.Group TOPOS = FIGURA.createGroup("caps_chiseled_stone", MUTEX_CAPS);
    private static final Multiblock.Group GIZ = FIGURA.createGroup("wizardChalkCircle", Multiblock.MAIN_MUTEX);

    static {
        FIGURA.add(0, 0, 0, ArcanaBlocks.OBELISK);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                FIGURA.add(PILARES, x, 0, z, Blocks.STONE_BRICKS);
                FIGURA.add(PILARES, x, 1, z, Blocks.STONE_BRICKS);
                FIGURA.add(TOPOS, x, 2, z, Blocks.CHISELED_STONE_BRICKS);
            }
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                FIGURA.add(GIZ, i, 0, j, ArcanaBlocks.WIZARD_CHALK);
            }
        }
    }

    /** O único topo que o Obelisco conhece: o tijolo cinzelado, que vale 1,35. */
    private static final java.util.Map<Multiblock.Group, Float> TOPOS_VALEM = caps(TOPOS, CHISELED_CAPS);

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    public int burnTimeRemaining;
    public int maxBurnTime = 1;

    public ObeliskBlockEntity(BlockPos onde, BlockState feitio) {
        super(ArcanaBlocks.OBELISK_ENTITY, onde, feitio, CAPACITY);
    }

    /** A figura do obelisco, para quem a queira desenhar num livro. */
    public static Multiblock structure() {
        return FIGURA;
    }

    /** Está a arder, ou tem o que arder. */
    public boolean isActive() {
        return this.burnTimeRemaining > 0 || !this.items.getFirst().isEmpty();
    }

    /** Está a arder forte: é o que faz o desenho dele ficar branco. */
    public boolean isHighPowerActive() {
        return this.burnTimeRemaining > HIGH_POWER && !this.items.getFirst().isEmpty();
    }

    @Override
    protected Multiblock figura() {
        return FIGURA;
    }

    @Override
    protected Multiblock.Group pilares() {
        return PILARES;
    }

    @Override
    protected Multiblock.Group giz() {
        return GIZ;
    }

    @Override
    protected java.util.Map<Multiblock.Group, Float> topos() {
        return TOPOS_VALEM;
    }

    @Override
    protected PowerType type() {
        return PowerType.NEUTRAL;
    }

    /** O Obelisco abençoa com grau dois. */
    @Override
    protected int blessingAmplifier() {
        return 2;
    }

    @Override
    public void serverTick(ServerLevel level) {
        this.surroundingCheckTicks++;
        if (this.isActive()) {
            if (this.surroundingCheckTicks % SURROUNDING_CHECK == 0) this.surroundings(level);

            float tem = PowerNodes.of(level).getPower(this.getBlockPos(), PowerType.NEUTRAL);
            float faria = ObeliskFuel.burnTime(this.items.getFirst()) * (POWER_BASE * this.powerMultiplier);
            float limite = Math.max(this.capacity - faria, this.capacity * CHARGE_CEILING);
            if (this.burnTimeRemaining <= 0 && tem < limite) {
                int queima = ObeliskFuel.burnTime(this.items.getFirst());
                if (queima > 0) {
                    this.burnTimeRemaining = queima;
                    this.maxBurnTime = Math.max(1, queima);
                    // um balde deixa o balde para trás; tudo o resto se gasta
                    var resto = this.items.getFirst().getItem().getCraftingRemainder();
                    this.items.getFirst().shrink(1);
                    if (this.items.getFirst().isEmpty() && resto != null) this.items.set(0, resto.create());
                    this.sync(level);
                }
            }

            if (this.burnTimeRemaining > 0) {
                this.burnTimeRemaining--;
                PowerNodes.of(level).insertPower(this.getBlockPos(), this, PowerType.NEUTRAL,
                        POWER_BASE * this.powerMultiplier);
                if (this.burnTimeRemaining % 20 == 0) this.sync(level);
            }
        } else {
            this.surroundingCheckTicks = 1;
        }

        super.serverTick(level);
    }

    private void sync(ServerLevel level) {
        this.setChanged();
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    // ---------------------------------------------------------------- o que ele é na rede

    // ---------------------------------------------------------------- a casa do combustível

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.items.getFirst().isEmpty();
    }

    @Override
    public ItemStack getItem(int qual) {
        return qual == 0 ? this.items.getFirst() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int qual, int quantos) {
        return ContainerHelper.removeItem(this.items, qual, quantos);
    }

    @Override
    public ItemStack removeItemNoUpdate(int qual) {
        return ContainerHelper.takeItem(this.items, qual);
    }

    @Override
    public void setItem(int qual, ItemStack pilha) {
        this.items.set(qual, pilha);
        pilha.limitSize(this.getMaxStackSize());
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player quem) {
        return Container.stillValidBlockEntity(this, quem);
    }

    @Override
    public boolean canPlaceItem(int qual, ItemStack pilha) {
        return ObeliskFuel.burnTime(pilha) > 0;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    /** Os dois números que a tela precisa: quanto falta queimar e quanto era ao todo. */
    private final net.minecraft.world.inventory.ContainerData dados = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int qual) {
            return qual == ObeliskMenu.DATA_BURN ? ObeliskBlockEntity.this.burnTimeRemaining
                    : ObeliskBlockEntity.this.maxBurnTime;
        }

        @Override
        public void set(int qual, int quanto) {
            if (qual == ObeliskMenu.DATA_BURN) ObeliskBlockEntity.this.burnTimeRemaining = quanto;
            else ObeliskBlockEntity.this.maxBurnTime = quanto;
        }

        @Override
        public int getCount() {
            return ObeliskMenu.DATA_SIZE;
        }
    };

    public net.minecraft.world.inventory.ContainerData data() {
        return this.dados;
    }

    /** O que o mostrador desenha: quanto falta da queima, em tantas partes. */
    public int cookProgressScaled(int quantas) {
        return this.burnTimeRemaining * quantas / Math.max(1, this.maxBurnTime);
    }

    // ---------------------------------------------------------------- a tela

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.obelisk");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory mochila, Player quem) {
        return new ObeliskMenu(id, mochila, this);
    }

    // ---------------------------------------------------------------- o que se guarda

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        saída.putInt("burnTimeRemaining", this.burnTimeRemaining);
        saída.putInt("maxBurnTime", this.maxBurnTime);
        ContainerHelper.saveAllItems(saída, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.burnTimeRemaining = entrada.getIntOr("burnTimeRemaining", 0);
        this.maxBurnTime = Math.max(1, entrada.getIntOr("maxBurnTime", 1));
        ContainerHelper.loadAllItems(entrada, this.items);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registos) {
        return this.saveWithoutMetadata(registos);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** O que o desenhista do cliente usa para fazer o obelisco subir e descer. */
    public static void clientTick(ObeliskBlockEntity obelisco) {
        if (obelisco.burnTimeRemaining > 0) obelisco.burnTimeRemaining--;
    }

    /** Dá a quem está de fora o que largar quando o bloco se parte. */
    public NonNullList<ItemStack> contents() {
        return this.items;
    }
}
