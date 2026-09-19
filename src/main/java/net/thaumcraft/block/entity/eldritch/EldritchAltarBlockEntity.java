package net.thaumcraft.block.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCBlockEntities;

/**
 * O altar eldritch: o {@code TileEldritchAltar} da 4.2.3.5. Guarda quantos olhos eldritch já foram postos nele (até
 * quatro, que se veem em volta do topo) e se o portal já foi aberto. Alguns altares chamam gente: os do tipo 0 trazem
 * quatro clérigos carmesins para o ritual e depois cavaleiros enquanto houver clérigo por perto; os do tipo 1, guardiões
 * eldritch. A cada dois segundos, passados os quatro primeiros.
 */
public class EldritchAltarBlockEntity extends BlockEntity {
    private boolean spawner;
    private boolean open;
    private boolean spawnedClerics;
    private byte spawnType;
    private byte eyes;
    private int counter;

    public EldritchAltarBlockEntity(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_ALTAR, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchAltarBlockEntity te) {
        if (!(level instanceof ServerLevel server)) return;
        if (te.spawner && te.counter++ >= 80 && te.counter % 40 == 0) {
            switch (te.spawnType) {
                case 0 -> {
                    if (!te.spawnedClerics) EldritchAltarSpawns.clerics(server, pos, te);
                    else EldritchAltarSpawns.guards(server, pos, te);
                }
                case 1 -> EldritchAltarSpawns.guardian(server, pos, te);
                default -> {
                }
            }
        }
    }

    public boolean isSpawner() {
        return this.spawner;
    }

    public void setSpawner(boolean spawner) {
        this.spawner = spawner;
    }

    public byte getSpawnType() {
        return this.spawnType;
    }

    public void setSpawnType(byte spawnType) {
        this.spawnType = spawnType;
    }

    public byte getEyes() {
        return this.eyes;
    }

    public void setEyes(byte eyes) {
        this.eyes = eyes;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setSpawnedClerics(boolean spawned) {
        this.spawnedClerics = spawned;
        this.setChanged();
    }

    /**
     * O {@code checkForMaze}: se não houver labirinto das Terras de Fora reservado aqui perto, manda gerar um (de quinze a
     * vinte e nove casas de lado) e responde que ainda não há; havendo, responde que há.
     */
    public boolean checkForMaze() {
        if (this.level == null) return false;
        int w = 15 + this.level.getRandom().nextInt(8) * 2;
        int h = 15 + this.level.getRandom().nextInt(8) * 2;
        return EldritchAltarSpawns.checkForMaze(this.level, this.worldPosition, w, h);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.eyes = (byte) input.getIntOr("eyes", 0);
        this.open = input.getBooleanOr("open", false);
        this.spawnedClerics = input.getBooleanOr("spawnedClerics", false);
        this.spawner = input.getBooleanOr("spawner", false);
        this.spawnType = (byte) input.getIntOr("spawntype", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("eyes", this.eyes);
        output.putBoolean("open", this.open);
        output.putBoolean("spawnedClerics", this.spawnedClerics);
        output.putBoolean("spawner", this.spawner);
        output.putInt("spawntype", this.spawnType);
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
