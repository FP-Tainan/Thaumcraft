package net.thaumcraft.occulta.vampire;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * <b>O rito que chama</b>: o {@code isRitual} do {@code ItemGlassGoblet}.
 *
 * <p>É a porta de entrada do ramo do vampiro, e é de propósito que ela seja difícil de encontrar por acaso.
 * Desenha-se no chão, com coisas que ninguém põe juntas sem querer:
 *
 * <ul>
 *   <li>um <b>crânio de esqueleto</b> no meio, pousado no chão;</li>
 *   <li>oito <b>pós de redstone</b> à volta dele, nas oito casas vizinhas;</li>
 *   <li>um <b>anel de fio-armadilha</b> de sete por sete, com os cantos cortados;</li>
 *   <li>e quatro <b>tochas</b>, uma em cada canto.</li>
 * </ul>
 *
 * <p>O chão de todo o quadrado tem de ser <b>sólido</b> e os dois andares por cima <b>vazios</b>: é um
 * desenho, e um desenho não se lê com coisas em cima.
 *
 * <p><b>Uma falha do original que fica.</b> O quarto <b>noroeste</b> do anel não é olhado: o autor copiou o
 * arco sudoeste duas vezes e esqueceu o outro. Quem construir o círculo inteiro — que é o que o desenho do
 * livro mostra — passa na mesma; quem deixar cinco fios de fora no noroeste <b>também passa</b>. Fica como
 * está, porque corrigir seria pedir mais do que o original pede.
 *
 * <h2>E o que ele faz</h2>
 *
 * <p>Com um <b>cálice de sangue de galinha</b> na mão, tocar no crânio <b>de noite</b>, a céu aberto e no
 * mundo de cima, chama <b>Elle</b> — e o crânio vai-se num raio. É tudo o que o rito faz: ele não dá
 * vampirice nenhuma. Ele só <b>abre a conversa</b>.
 */
public final class VampireRitual {
    /** O anel de fio-armadilha, pelas casas que o original olha. */
    public static final List<int[]> ANEL = List.of(
            new int[]{0, -3}, new int[]{1, -3}, new int[]{2, -3},
            new int[]{2, -2}, new int[]{3, -2}, new int[]{3, -1},
            new int[]{3, 0}, new int[]{3, 1}, new int[]{3, 2},
            new int[]{2, 2}, new int[]{2, 3}, new int[]{1, 3},
            new int[]{0, 3}, new int[]{-1, 3}, new int[]{-2, 3},
            new int[]{-2, 2}, new int[]{-3, 2}, new int[]{-3, 1},
            new int[]{-3, 0});

    /** As quatro tochas, nos quatro cantos. */
    public static final List<int[]> CANTOS = List.of(
            new int[]{-3, -3}, new int[]{-3, 3}, new int[]{3, -3}, new int[]{3, 3});

    /** E o quadrado inteiro, que é de sete por sete. */
    public static final int RAIO = 3;

    /** A que distância outra Elle impede que se chame mais uma. */
    public static final double SÓ_UMA_A = 32.0;

    private VampireRitual() {
    }

    /** Se o rito está desenhado à volta deste crânio. */
    public static boolean desenhado(ServerLevel level, BlockPos meio) {
        if (!(level.getBlockState(meio).getBlock() instanceof SkullBlock crânio)) return false;
        if (crânio.getType() != SkullBlock.Types.SKELETON) return false;

        for (int[] onde : ANEL) {
            if (!level.getBlockState(meio.offset(onde[0], 0, onde[1])).is(Blocks.TRIPWIRE)) return false;
        }
        for (int[] onde : CANTOS) {
            if (!level.getBlockState(meio.offset(onde[0], 0, onde[1])).is(Blocks.TORCH)) return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (!level.getBlockState(meio.offset(dx, 0, dz)).is(Blocks.REDSTONE_WIRE)) return false;
            }
        }

        // o chão sólido, e os dois andares por cima vazios
        for (int dx = -RAIO; dx <= RAIO; dx++) {
            for (int dz = -RAIO; dz <= RAIO; dz++) {
                BlockPos casa = meio.offset(dx, 0, dz);
                if (!level.getBlockState(casa.below()).isSolidRender()) return false;
                if (!level.isEmptyBlock(casa.above())) return false;
                if (!level.isEmptyBlock(casa.above(2))) return false;
            }
        }
        return true;
    }

    /** Se a noite, o céu e o mundo deixam o rito correr. */
    public static boolean aHoraÉBoa(ServerLevel level, BlockPos meio) {
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return false;
        if (level.isBrightOutside()) return false;
        return level.canSeeSky(meio);
    }

    /** E se já há uma Elle por perto, porque uma de cada vez chega. */
    public static boolean jáHáUma(ServerLevel level, BlockPos meio) {
        return !level.getEntitiesOfClass(FollowerEntity.class,
                new AABB(meio).inflate(SÓ_UMA_A)).isEmpty();
    }

    /**
     * <b>Chama Elle.</b>
     *
     * <p>O crânio vai-se num raio e ela fica no lugar dele. Repare que o raio é <b>de verdade</b> — ele acende
     * o que estiver perto e assusta quem estiver em volta —, e é o original que o quer assim: o rito não é um
     * botão, é um acontecimento.
     *
     * @return se ela veio
     */
    public static boolean chama(ServerLevel level, Player quem, BlockPos meio) {
        if (!desenhado(level, meio) || !aHoraÉBoa(level, meio) || jáHáUma(level, meio)) return false;

        level.removeBlock(meio, false);
        var raio = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (raio != null) {
            raio.snapTo(meio.getX() + 0.5, meio.getY() + 0.05, meio.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(raio);
        }

        var elle = net.thaumcraft.occulta.OccultaEntities.FOLLOWER.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (elle == null) return false;
        elle.snapTo(meio.getX() + 0.5, meio.getY() + 1.05, meio.getZ() + 0.5, 0.0f, 0.0f);
        elle.setPersistenceRequired();
        elle.dono(quem);
        level.addFreshEntity(elle);

        level.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                meio.getX() + 0.5, meio.getY() + 1.05, meio.getZ() + 0.5, 16, 1.0, 2.0, 1.0, 0.0);
        quem.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.lilith.ritual")
                .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        return true;
    }
}
