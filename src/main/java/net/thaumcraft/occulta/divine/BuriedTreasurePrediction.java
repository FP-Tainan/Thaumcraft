package net.thaumcraft.occulta.divine;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

/**
 * <b>Vais achar um tesouro</b>: a {@code PredictionBuriedTreasure} do Witchery.
 *
 * <p>Quem foi avisado cava grama, terra, areia ou cascalho e, por baixo, <b>aparece um baú</b> — cheio do que
 * um corredor de mina velha costuma ter.
 *
 * <p>Ela <b>nunca se força</b>: o original lhe desliga as duas portas do cumprimento por si próprio, e com
 * razão — um baú que aparecesse debaixo de quem estivesse parado não seria um tesouro enterrado, seria um
 * baú aparecendo. Esta só acontece enquanto alguém cava.
 *
 * <p>E ela é exigente com o lugar: pede que os <b>quatro lados</b> e o bloco <b>dois abaixo</b> não sejam ar,
 * para que o baú não fique pendurado num buraco. É o que a faz parecer enterrada.
 */
public class BuriedTreasurePrediction extends AlwaysForcedPrediction {
    /** O que vai dentro dele. */
    private final ResourceKey<LootTable> doquê;

    /** A altura abaixo da qual não há tesouro nenhum. */
    public static final int FUNDO = 6;

    public BuriedTreasurePrediction(int id, int peso, double forçaPorBatida, String recado, int prazo,
                                    double emDia, ResourceKey<LootTable> doquê) {
        super(id, peso, forçaPorBatida, recado, prazo, emDia);
        this.doquê = doquê;
    }

    /** Esta nunca se força: ela só acontece a quem cava. */
    @Override
    public boolean tentaForçar(ServerLevel level) {
        return false;
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, BlockState feitio, BlockPos onde,
                            List<ItemStack> cai, boolean atrasada, boolean velha) {
        if (!feitio.is(net.minecraft.tags.BlockTags.DIRT) && !feitio.is(Blocks.GRASS_BLOCK)
                && !feitio.is(net.minecraft.tags.BlockTags.SAND) && !feitio.is(Blocks.GRAVEL)) {
            return false;
        }
        if (onde.getY() <= FUNDO || !this.évez(level, atrasada)) return false;
        for (BlockPos lado : new BlockPos[] {
            onde.below().east(), onde.below().west(), onde.below().south(), onde.below().north(),
            onde.below(2),
        }) {
            if (level.getBlockState(lado).isAir()) return false;
        }

        BlockPos baú = onde.below();
        level.setBlockAndUpdate(baú, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(baú) instanceof RandomizableContainerBlockEntity dentro) {
            dentro.setLootTable(this.doquê, level.getRandom().nextLong());
        }
        return true;
    }
}
