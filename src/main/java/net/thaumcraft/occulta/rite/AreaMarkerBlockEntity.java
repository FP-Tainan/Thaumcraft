package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.occulta.OccultaBlocks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A alma de um marcador de área: a {@code TileEntityAreaMarker} do Witchery.
 *
 * <p>Ela guarda <b>uma coisa só</b> — a lista de quem matou alguém perto dela — e <b>apresenta-se ao
 * registo</b> ao carregar, porque é por essa lista que os ritos perguntam. Um marcador que ninguém
 * carregou não protege nada, e isso também é do original.
 */
public class AreaMarkerBlockEntity extends BlockEntity {
    private final Set<UUID> matou = new HashSet<>();
    private AreaMarkers.Marcador registado;

    public AreaMarkerBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.AREA_MARKER_ENTITY, onde, feitio);
    }

    /** De que feitio é este marcador, que é o que o bloco por baixo dele diz. */
    public AreaMarkers.Kind feitio() {
        return this.getBlockState().getBlock() instanceof AreaMarkerBlock bloco
                ? bloco.feitio().contra() : AreaMarkers.Kind.CURSE;
    }

    /** Quem já matou alguém perto dela. */
    public Set<UUID> matou() {
        return this.matou;
    }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        if (level.isClientSide()) return;
        this.registado = new AreaMarkers.Marcador(level.dimension(), this.getBlockPos(),
                this.feitio(), this.matou);
        AreaMarkers.register(this.registado);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.registado != null) AreaMarkers.remove(this.registado);
    }

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        if (!this.matou.isEmpty()) {
            saída.store("Killers", UUIDUtil.CODEC.listOf(), java.util.List.copyOf(this.matou));
        }
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.matou.clear();
        entrada.read("Killers", UUIDUtil.CODEC.listOf()).ifPresent(this.matou::addAll);
    }
}
