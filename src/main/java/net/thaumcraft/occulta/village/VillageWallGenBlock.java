package net.thaumcraft.occulta.village;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * O marcador que vira muralha: o {@code BlockVillageWallGen} do Witchery.
 *
 * <p>Não se vê, não se pega e não se fabrica — ele só nasce com a aldeia, espera os trechos vizinhos ficarem
 * prontos, manda levantar a muralha e some. Quem faz o trabalho é o {@link VillageWallGenBlockEntity}.
 */
public class VillageWallGenBlock extends BaseEntityBlock {
    public static final MapCodec<VillageWallGenBlock> CODEC = simpleCodec(VillageWallGenBlock::new);

    public VillageWallGenBlock(Properties propriedades) {
        super(propriedades);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** Invisível: ele é um recado, não um bloco. */
    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new VillageWallGenBlockEntity(onde, feitio);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState feitio, BlockEntityType<T> tipo) {
        return level.isClientSide() ? null
                : createTickerHelper(tipo, OccultaBlocks.VILLAGE_WALL_GEN_ENTITY,
                        VillageWallGenBlockEntity::tick);
    }
}
