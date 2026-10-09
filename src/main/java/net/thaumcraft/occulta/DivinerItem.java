package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.infusion.OverworldInfusion;

/**
 * As <b>Varas de Rabdomante</b>: o {@code ItemDiviner} do Witchery, a mesma classe duas vezes — uma que
 * procura <b>água</b> e outra que procura <b>lava</b>.
 *
 * <p>É uma ideia simples e boa. Aponta-se para o <b>chão</b> — a face de cima de um bloco, a seis de
 * distância — e <b>segura-se</b>. A cada batida a vara olha <b>um bloco mais fundo</b> debaixo daquele
 * ponto, e desce; achando o que procura, faz <b>faísca mágica</b> e toca o orbe. Batendo na rocha-mãe ou
 * no fundo do mundo, faz <b>fumaça</b> e toca a caixa.
 *
 * <p>Nos dois casos ela <b>para</b> e gasta um dos <b>cinquenta</b> usos. Não há como segurar uma vara e
 * não saber o que há debaixo dos pés: ela sempre responde.
 *
 * <p>Vinte segundos de segurar são quatrocentos blocos de fundura, que é mais do que o mundo tem — de modo
 * que o tempo nunca acaba antes da resposta. E <b>deixando de apontar para o chão</b>, ela desiste na hora:
 * uma vara apontada para o ar é uma vara que não sabe por onde descer.
 */
public class DivinerItem extends Item {
    /** Cinquenta respostas. */
    public static final int USOS = 50;

    /** Vinte segundos de segurar: quatrocentas batidas, e com elas quatrocentos blocos. */
    public static final int SEGURAR = 400;

    /** A seis blocos de distância, que é o alcance do olhar dela. */
    public static final double ALCANCE = 6.0;

    /**
     * O que ela faz ao chegar ao <b>fundo do mundo</b>: desiste.
     *
     * <p>O original escreve {@code posY <= 1}, que em 2014 era o fundo; hoje o fundo é o que o mundo disser,
     * e pode ser qualquer número. A pergunta passa a ser feita ao mundo.
     */
    public static int fundoDe(Level level) {
        return level.getMinY();
    }

    /** O que ela procura. */
    private final Block oQueProcura;

    public DivinerItem(Block oQueProcura, Properties properties) {
        super(properties);
        this.oQueProcura = oQueProcura;
    }

    /** O bloco que esta vara procura. */
    public Block oQueProcura() {
        return this.oQueProcura;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack vara, LivingEntity quem) {
        return SEGURAR;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack vara) {
        return ItemUseAnimation.BOW;
    }

    /**
     * Cada batida desce um bloco.
     *
     * <p>A conta é a do original: a <b>fundura</b> é o tanto de batidas que já se segurou, contado a partir
     * do bloco para onde se aponta. Primeira batida, o bloco de cima; centésima, cem abaixo dele.
     */
    @Override
    public void onUseTick(Level level, LivingEntity quemUsa, ItemStack vara, int falta) {
        if (!(level instanceof ServerLevel mundo) || !(quemUsa instanceof ServerPlayer quem)) return;

        int fundura = SEGURAR - falta;
        HitResult onde = OverworldInfusion.olha(mundo, quem, ALCANCE);
        if (!(onde instanceof BlockHitResult bateu) || bateu.getType() != HitResult.Type.BLOCK
                || bateu.getDirection() != Direction.UP) {
            mundo.playSound(null, quem.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.PLAYERS, 0.5f, 1.0f);
            quem.stopUsingItem();
            return;
        }

        BlockPos chão = bateu.getBlockPos();
        BlockPos olhando = new BlockPos(chão.getX(), chão.getY() - fundura, chão.getZ());
        Block lá = mundo.getBlockState(olhando).getBlock();

        boolean achou = lá == this.oQueProcura;
        boolean acabou = achou || lá == Blocks.BEDROCK;
        if (!acabou && olhando.getY() > fundoDe(mundo)) return;

        double x = chão.getX() + 0.5;
        double y = chão.getY() + 1.0;
        double z = chão.getZ() + 0.5;
        if (achou) {
            mundo.sendParticles(ParticleTypes.ENCHANTED_HIT, x, y, z, 8, 0.5, 0.5, 0.5, 0.0);
            mundo.playSound(null, chão, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    0.5f, 1.0f);
        } else {
            mundo.sendParticles(ParticleTypes.SMOKE, x, y, z, 8, 0.5, 0.5, 0.5, 0.0);
            mundo.playSound(null, chão, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS,
                    0.5f, 1.0f);
        }

        quem.stopUsingItem();
        vara.hurtAndBreak(1, quem, EquipmentSlot.MAINHAND);
    }
}
