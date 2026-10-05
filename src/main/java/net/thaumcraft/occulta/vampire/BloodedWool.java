package net.thaumcraft.occulta.vampire;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * A <b>Lã Ensanguentada</b>: o ramo do {@code itemBoneNeedle} do Witchery que faz pano escuro.
 *
 * <p>Um <b>vampiro do quarto grau</b> com uma <b>Agulha de Osso</b> na mão fura a si mesmo sobre um bloco de
 * <b>lã branca</b> e tinge-a com o próprio sangue: <b>cento e vinte e cinco</b> de poder, que é o mesmo que
 * um primeiro gole lhe dá.
 *
 * <p>E a lã tinta, <b>no forno</b>, vira <b>Pano Escuro</b> — que é o tecido de que se fazem as roupas do
 * ofício. É um caminho curioso e vale dizê-lo inteiro: para ter uma capa de bruxa é preciso um vampiro, uma
 * ovelha branca e um forno.
 *
 * <p>Só a <b>branca</b> serve. O original pergunta pelo número zero da lã, que é a que não foi tingida de
 * nada — faz sentido: é a única que ainda tem lugar para outra cor.
 */
public final class BloodedWool {
    /** O que o furo custa ao vampiro, e de que grau ele tem de ser. */
    public static final int CUSTA = 125;
    public static final int GRAU = 4;

    private BloodedWool() {
    }

    public static void init() {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(BloodedWool::tinge);
    }

    /** A agulha na lã branca, de quem tem sangue que chegue. */
    public static InteractionResult tinge(Player quem, Level level, InteractionHand mão,
                                          BlockHitResult acertou) {
        var naMão = quem.getItemInHand(mão);
        if (!naMão.is(net.thaumcraft.mortuorum.MortuorumItems.BONE_NEEDLE)) {
            return InteractionResult.PASS;
        }
        if (!level.getBlockState(acertou.getBlockPos()).is(Blocks.WHITE_WOOL)) {
            return InteractionResult.PASS;
        }
        if (!Vampire.é(quem) || Vampire.grauDe(quem) < GRAU) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;

        if (!Vampire.gasta(quem, CUSTA, true)) {
            mundo.playSound(null, acertou.getBlockPos(),
                    net.minecraft.sounds.SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.BLOCKS, 1.0f, 0.5f);
            return InteractionResult.SUCCESS;
        }

        var onde = acertou.getBlockPos();
        mundo.setBlock(onde, OccultaBlocks.BLOODED_WOOL.defaultBlockState(), Block.UPDATE_ALL);
        mundo.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5, 20, 1.0, 1.0, 1.0, 0.0);
        mundo.playSound(null, onde, OccultaSounds.DRINK.value(), SoundSource.BLOCKS, 0.5f,
                0.4f / (mundo.getRandom().nextFloat() * 0.4f + 0.8f));
        return InteractionResult.SUCCESS;
    }
}
