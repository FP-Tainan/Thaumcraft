package net.thaumcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.visnet.VisNodeBlockEntity;
import net.thaumcraft.api.wands.Wandable;
import net.thaumcraft.block.VisRelayBlock;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCSounds;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * O relé de vis: o {@code TileVisRelay} da 4.2.3.5. Alcança oito blocos, pendura-se no ponto da rede mais perto e
 * passa adiante o vis que lhe pedem. O cristal pode ser afinado numa das seis cores primordiais (com a varinha ou com
 * um fragmento): afinado, só se liga a relés da mesma cor ou sem cor. Quem estiver a até cinco blocos fica marcado
 * como perto deste relé — é daí que o amuleto de vis se enche.
 */
public class VisRelayBlockEntity extends VisNodeBlockEntity implements Wandable {
    /** As cores do cristal: ar, fogo, água, terra, ordem e entropia. */
    public static final int[] COLOURS = {16777086, 16727041, 37119, 40960, 15650047, 5592439};
    /** O {@code nearbyPlayers}: o relé mais perto de cada jogador. */
    public static final Map<UUID, WeakReference<VisRelayBlockEntity>> NEARBY_PLAYERS = new HashMap<>();

    public byte colour = -1;
    /** Só do lado de quem joga: o pulso e a cor do fio. */
    public int pulse;
    public float pRed = 0.5f, pGreen = 0.5f, pBlue = 0.5f;
    private int px, py, pz;
    private boolean parentLoaded;

    public VisRelayBlockEntity(BlockPos pos, BlockState state) {
        this(TCBlockEntities.VIS_RELAY, pos, state);
    }

    protected VisRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Para onde o cristal aponta (a face em que foi posto). */
    public Direction orientation() {
        BlockState state = this.getBlockState();
        return state.hasProperty(VisRelayBlock.FACING) ? state.getValue(VisRelayBlock.FACING) : Direction.UP;
    }

    @Override
    public byte getAttunement() {
        return this.colour;
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isSource() {
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VisRelayBlockEntity relay) {
        relay.drawEffect();
        relay.visTick();
        if (!level.isClientSide() && relay.nodeCounter % 20 == 0) {
            for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(5.0))) {
                WeakReference<VisRelayBlockEntity> old = NEARBY_PLAYERS.get(player.getUUID());
                VisRelayBlockEntity near = old == null ? null : old.get();
                if (near == null || !(near.getBlockPos().distToCenterSqr(player.position()) < pos.distToCenterSqr(player.position()))) {
                    NEARBY_PLAYERS.put(player.getUUID(), new WeakReference<>(relay));
                }
            }
        }
    }

    /** O {@code drawEffect}: do lado de quem joga, refaz o pai pelo que veio do servidor e estica o fio até ele. */
    protected void drawEffect() {
        if (this.level == null) return;
        if (this.level.isClientSide()) {
            if (this.parentLoaded) {
                if (this.px == 0 && this.py == 0 && this.pz == 0) {
                    this.setParent(null);
                } else if (this.level.getBlockEntity(this.getBlockPos().offset(-this.px, -this.py, -this.pz)) instanceof VisNodeBlockEntity node) {
                    this.setParent(node);
                }
                this.parentLoaded = false;
            }
            VisNodeBlockEntity parent = this.parent();
            if (parent != null) {
                Direction d1 = parent instanceof VisRelayBlockEntity relay && !(relay instanceof WorkbenchChargerBlockEntity) ? relay.orientation() : null;
                Direction d2 = this instanceof WorkbenchChargerBlockEntity ? null : this.orientation();
                Vec3 from = Vec3.atCenterOf(parent.getBlockPos());
                if (d1 != null) from = from.subtract(d1.getStepX() * 0.05, d1.getStepY() * 0.05, d1.getStepZ() * 0.05);
                Vec3 to = Vec3.atCenterOf(this.getBlockPos());
                if (d2 != null) to = to.subtract(d2.getStepX() * 0.05, d2.getStepY() * 0.05, d2.getStepZ() * 0.05);
                net.thaumcraft.client.NodeClient.beam(this.getBlockPos(), from, to, this.pRed, this.pGreen, this.pBlue, this.pulse > 0);
            }
            this.pRed = Math.min(1.0f, this.pRed + 0.025f);
            this.pGreen = Math.min(1.0f, this.pGreen + 0.025f);
            this.pBlue = Math.min(1.0f, this.pBlue + 0.025f);
        }
        if (this.pulse > 0) this.pulse--;
    }

    @Override
    public void triggerConsumeEffect(Aspect aspect) {
        int c = -1;
        if (aspect == Aspects.AIR) c = 0;
        else if (aspect == Aspects.FIRE) c = 1;
        else if (aspect == Aspects.WATER) c = 2;
        else if (aspect == Aspects.EARTH) c = 3;
        else if (aspect == Aspects.ORDER) c = 4;
        else if (aspect == Aspects.ENTROPY) c = 5;
        if (c >= 0 && this.pulse == 0 && this.level != null) {
            this.pulse = 5;
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 0, c);
        }
    }

    /** O pulso chega a quem joga: acende o fio na cor do aspecto, e sobe a corrente de relés até a fonte. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != 0) return super.triggerEvent(id, param);
        if (this.level != null && this.level.isClientSide() && param >= 0 && param < COLOURS.length) {
            int c = COLOURS[param];
            this.pulse = 5;
            this.pRed = (c >> 16 & 255) / 255.0f;
            this.pGreen = (c >> 8 & 255) / 255.0f;
            this.pBlue = (c & 255) / 255.0f;
            for (VisNodeBlockEntity up = this.parent(); up instanceof VisRelayBlockEntity relay && relay.pulse == 0; up = up.parent()) {
                relay.pRed = this.pRed;
                relay.pGreen = this.pGreen;
                relay.pBlue = this.pBlue;
                relay.pulse = 5;
            }
        }
        return true;
    }

    @Override
    public void parentChanged() {
        if (this.level != null && !this.level.isClientSide() && this.getBlockState().hasProperty(VisRelayBlock.LIT)) {
            boolean lit = this.parent() != null;
            if (this.getBlockState().getValue(VisRelayBlock.LIT) != lit) {
                this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(VisRelayBlock.LIT, lit), 3);
            }
        }
    }

    /** A varinha gira a cor do cristal: sem cor, e as seis primordiais. */
    @Override
    public boolean onWand(Level level, ItemStack wand, Player player, BlockPos pos, net.minecraft.core.Direction face) {
        if (!level.isClientSide()) {
            this.colour++;
            if (this.colour > 5) this.colour = -1;
            this.retune();
        }
        return true;
    }

    /** O fragmento afina o cristal na cor dele; o mesmo fragmento, ou o balanceado, tira a cor. */
    public void attune(byte shard) {
        this.colour = shard != this.colour && shard != 6 ? shard : -1;
        this.retune();
    }

    private void retune() {
        this.removeThisNode();
        this.nodeRefresh = true;
        this.syncNode();
        if (this.level != null) {
            this.level.playSound(null, this.getBlockPos(), TCSounds.CRYSTAL.value(), SoundSource.BLOCKS, 0.2f, 1.0f);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.colour = (byte) input.getIntOr("color", -1);
        this.px = input.getIntOr("px", 0);
        this.py = input.getIntOr("py", 0);
        this.pz = input.getIntOr("pz", 0);
        this.parentLoaded = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("color", this.colour);
        VisNodeBlockEntity parent = this.parent();
        BlockPos pos = this.getBlockPos();
        output.putInt("px", parent == null ? 0 : pos.getX() - parent.getBlockPos().getX());
        output.putInt("py", parent == null ? 0 : pos.getY() - parent.getBlockPos().getY());
        output.putInt("pz", parent == null ? 0 : pos.getZ() - parent.getBlockPos().getZ());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
