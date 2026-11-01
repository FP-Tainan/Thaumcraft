package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.arcana.ArcanaBlocks;

/**
 * O <b>Giz de Mago</b> na mão: o {@code ItemChalk} da 1.4.0.009.
 *
 * <p>Risca-se <b>por cima</b> de um bloco, e só por cima. Cada risco gasta um ponto dos <b>cinquenta</b>
 * que o giz tem, e sai com um dos dezesseis desenhos, sorteado.
 *
 * <p>Não se risca por cima de outro risco: é o que impede alguém de gastar o giz todo no mesmo lugar sem
 * dar por isso.
 */
public class ChalkItem extends Item {
    /** Quantos riscos dá um giz. */
    public static final int USES = 50;

    /** O mesmo sinalizador do original: põe o bloco e avisa os vizinhos. */
    private static final int AVISA_VIZINHOS = 2;

    public ChalkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext contexto) {
        if (contexto.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        Level nível = contexto.getLevel();
        BlockPos onde = contexto.getClickedPos();
        if (nível.getBlockState(onde).is(ArcanaBlocks.WIZARD_CHALK)) return InteractionResult.PASS;
        BlockPos acima = onde.above();
        if (!nível.getBlockState(acima).isAir()) return InteractionResult.PASS;

        var risco = ArcanaBlocks.WIZARD_CHALK.defaultBlockState()
                .setValue(WizardChalkBlock.VARIANT, nível.getRandom().nextInt(WizardChalkBlock.VARIANTS));
        if (!risco.canSurvive(nível, acima)) return InteractionResult.PASS;

        if (!nível.isClientSide()) {
            nível.setBlock(acima, risco, AVISA_VIZINHOS);
            var pilha = contexto.getItemInHand();
            var quem = contexto.getPlayer();
            if (quem != null) {
                pilha.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel) nível,
                        quem instanceof net.minecraft.server.level.ServerPlayer gente ? gente : null, item -> {
                        });
            }
        }
        return InteractionResult.SUCCESS;
    }
}
