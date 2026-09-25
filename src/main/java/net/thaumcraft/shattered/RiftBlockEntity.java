package net.thaumcraft.shattered;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A fenda que mora numa porta: o {@code TileEntityEntranceRift} das Portas Dimensionais.
 *
 * <p>Ela guarda para onde leva — um mundo, um lugar e para que lado se sai — e passa quem a atravessa para lá.
 * Sem destino, a primeira travessia manda abrir um bolso e aponta-se para ele.
 *
 * <p><b>Do original fica de fora, por enquanto</b>, o registro de fendas: lá as fendas formam um grafo, acham-se
 * umas às outras e remendam-se quando uma morre. Aqui cada fenda sabe só de si. O que ela guarda cabe no que o
 * grafo há de querer depois, então isto não se joga fora quando ele chegar.
 */
public class RiftBlockEntity extends BlockEntity {
    /** Para onde a fenda leva. */
    public record Destination(ResourceKey<Level> level, BlockPos pos, float yaw) {
        public static final Codec<Destination> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("level").forGetter(Destination::level),
                BlockPos.CODEC.fieldOf("pos").forGetter(Destination::pos),
                Codec.FLOAT.optionalFieldOf("yaw", 0.0f).forGetter(Destination::yaw)
        ).apply(instance, Destination::new));

        public GlobalPos global() {
            return new GlobalPos(this.level, this.pos);
        }
    }

    private @Nullable Destination destination;
    /** De onde a fenda veio, para a porta de volta saber para onde apontar. */
    private @Nullable Destination source;

    public RiftBlockEntity(BlockPos pos, BlockState state) {
        super(ShatteredBlocks.RIFT_ENTITY, pos, state);
    }

    public @Nullable Destination destination() {
        return this.destination;
    }

    public void setDestination(@Nullable Destination destination) {
        this.destination = destination;
        this.setChanged();
    }

    public @Nullable Destination source() {
        return this.source;
    }

    public void setSource(@Nullable Destination source) {
        this.source = source;
        this.setChanged();
    }

    /**
     * Passa quem atravessa para o outro lado. Sem destino, abre-se um bolso e a fenda passa a apontar para ele —
     * que é o que a porta de madeira do original faz na primeira vez que alguém a atravessa.
     */
    public boolean teleport(Entity quem) {
        if (!(this.level instanceof ServerLevel aqui)) return false;
        if (this.destination == null) {
            Destination feito = Pockets.open(aqui, this.worldPosition);
            if (feito == null) return false;
            this.setDestination(feito);
        }
        ServerLevel destino = aqui.getServer().getLevel(this.destination.level());
        if (destino == null) return false;

        BlockPos onde = this.destination.pos();
        aqui.playSound(null, this.worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (quem instanceof ServerPlayer jogador) {
            jogador.teleportTo(destino, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, Set.of(),
                    this.destination.yaw(), 0.0f, false);
        } else {
            quem.teleportTo(destino, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, Set.of(),
                    this.destination.yaw(), 0.0f, false);
        }
        destino.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.destination = input.read("destination", Destination.CODEC).orElse(null);
        this.source = input.read("source", Destination.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.destination != null) output.store("destination", Destination.CODEC, this.destination);
        if (this.source != null) output.store("source", Destination.CODEC, this.source);
    }
}
