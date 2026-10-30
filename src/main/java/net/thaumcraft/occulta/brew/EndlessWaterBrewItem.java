package net.thaumcraft.occulta.brew;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Cozimento de Água sem Fim</b>: o {@code ItemBrewEndlessWater} do Witchery.
 *
 * <p>Um frasco que dá <b>água</b> e não se esvazia — até se esvaziar. Ele traz um número de goles, e cada
 * gole é uma casa de água posta no mundo ou um caldeirão enchido. Quando os goles acabam, o frasco quebra.
 *
 * <p>Quantos goles ele traz depende de <b>como foi feito</b>: a receita mais rica dá cem, e a mais pobre dá
 * cinco. É a única coisa do mod em que o esforço de fazer se paga em quantidade e não em força.
 *
 * <p>Ele serve a quatro coisas, nesta ordem: enche o <b>caldeirão do ofício</b>, enche o <b>pote</b>, enche
 * a <b>caldeira do jogo</b> até acima — e, não havendo nenhum dos três, <b>põe uma casa de água</b> na face
 * em que se clicou.
 *
 * <p>É o que faz de uma bruxa alguém que não precisa de rio: com um destes no cinto, ela planta onde quiser.
 */
public class EndlessWaterBrewItem extends Item {
    /** Quantos goles o frasco inteiro tem. */
    public static final int GOLES = 100;

    /** O que ele põe no caldeirão do ofício, que é três baldes. */
    public static final int CALDEIRÃO = 3 * net.thaumcraft.occulta.WitchesCauldronBlockEntity.BUCKET;

    public EndlessWaterBrewItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext contexto) {
        Level level = contexto.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return InteractionResult.PASS;

        BlockPos onde = contexto.getClickedPos();
        ItemStack naMão = contexto.getItemInHand();
        Player quem = contexto.getPlayer();
        BlockState oquê = level.getBlockState(onde);

        // o caldeirão do ofício
        if (level.getBlockEntity(onde)
                instanceof net.thaumcraft.occulta.WitchesCauldronBlockEntity caldeirão) {
            if (!caldeirão.fill(CALDEIRÃO)) return InteractionResult.CONSUME;
            return gasta(server, onde, naMão, quem);
        }

        // o pote
        if (level.getBlockEntity(onde) instanceof net.thaumcraft.occulta.kettle.KettleBlockEntity pote) {
            if (!pote.fill()) return InteractionResult.CONSUME;
            return gasta(server, onde, naMão, quem);
        }

        // a caldeira do jogo, que ele enche de uma vez e não aos poucos
        if (oquê.is(Blocks.CAULDRON) || (oquê.is(Blocks.WATER_CAULDRON)
                && oquê.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL)) {
            level.setBlockAndUpdate(onde, Blocks.WATER_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL));
            return gasta(server, onde, naMão, quem);
        }

        // e, não havendo nada disso, uma casa de água na face em que se clicou
        BlockPos lado = onde.relative(contexto.getClickedFace());
        if (!level.getBlockState(lado).canBeReplaced()) return InteractionResult.PASS;
        level.setBlockAndUpdate(lado, Blocks.WATER.defaultBlockState());
        return gasta(server, lado, naMão, quem);
    }

    /** Um gole a menos, e o barulho da água. */
    private static InteractionResult gasta(net.minecraft.server.level.ServerLevel level, BlockPos onde,
                                           ItemStack naMão, @Nullable Player quem) {
        level.playSound(null, onde, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.0f);
        if (quem == null || !quem.hasInfiniteMaterials()) {
            naMão.hurtAndBreak(1, level,
                    quem instanceof net.minecraft.server.level.ServerPlayer dono ? dono : null, item -> {
                    });
        }
        return InteractionResult.SUCCESS;
    }
}
