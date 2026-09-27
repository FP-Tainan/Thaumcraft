package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Uma tora do ofício: o {@code BlockWitchLog} do Witchery.
 *
 * <p>É madeira como qualquer outra, com uma diferença: quem a corta pode <b>acordar um Ent</b>. A chance é de uma
 * em cem, mais uma por tora encostada nela, até cinco em cem — um bosque cerrado se defende melhor que uma árvore
 * sozinha.
 *
 * <p>O Ent não nasce em cima de quem corta: o original procura um lugar num raio de dezesseis blocos com três de
 * céu livre, e é o que se faz aqui.
 */
public class WitchLogBlock extends RotatedPillarBlock {
    public static final MapCodec<WitchLogBlock> CODEC = simpleCodec(WitchLogBlock::new);

    /** Uma em cem, mais uma por tora ao lado, até cinco. */
    public static final double ENT_CHANCE = 0.01;
    public static final double ENT_MAX = 0.05;

    /** Onde o Ent cabe: até dezesseis blocos daqui, com três de céu. */
    public static final int ENT_RANGE = 16;
    public static final int ENT_HEADROOM = 3;

    public WitchLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    /** O {@code dropBlockAsItemWithChance} do original: cortada a tora, o bosque às vezes responde. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server) this.maybeWakeEnt(server, pos, state);
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** A conta da chance: uma em cem pela tora, mais uma por tora encostada nela, até cinco em cem. */
    public double entChance(net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        double chance = ENT_CHANCE;
        for (Direction lado : Direction.values()) {
            if (level.getBlockState(pos.relative(lado)).is(this)) chance += ENT_CHANCE;
        }
        return Math.min(chance, ENT_MAX);
    }

    private void maybeWakeEnt(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getRandom().nextDouble() >= this.entChance(level, pos)) return;

        BlockPos onde = this.room(level, pos);
        if (onde == null) return;
        EntEntity.spawn(level, onde);
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 1.0f, 0.6f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                onde.getX() + 0.5, onde.getY() + 1.5, onde.getZ() + 0.5, 24, 1.0, 1.5, 1.0, 0.0);
    }

    /** Um lugar por perto onde o Ent caiba de pé. */
    private BlockPos room(ServerLevel level, BlockPos pos) {
        for (int tentativa = 0; tentativa < 16; tentativa++) {
            int x = pos.getX() - ENT_RANGE + level.getRandom().nextInt(ENT_RANGE * 2 + 1);
            int z = pos.getZ() - ENT_RANGE + level.getRandom().nextInt(ENT_RANGE * 2 + 1);
            BlockPos chão = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos(x, 0, z));
            int céu = 0;
            while (céu < 6 && level.isEmptyBlock(chão.above(céu + 1))) céu++;
            if (céu >= ENT_HEADROOM) return chão;
        }
        return null;
    }
}
