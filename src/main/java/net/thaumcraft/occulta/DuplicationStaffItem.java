package net.thaumcraft.occulta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O <b>Cajado de Duplicação</b>: o {@code ItemDuplicationStaff} do Witchery.
 *
 * <p>Ele copia um pedaço do mundo e o cola noutro lugar. Foi com ele que o autor do original desenhou as
 * casas da aldeia, e por isso ele é o que é: uma ferramenta de quem constrói, não de quem joga.
 *
 * <h2>Como se usa</h2>
 *
 * <ol>
 *   <li>bate-se num bloco: fica o <b>marco</b>, uma quina da caixa;</li>
 *   <li>bate-se noutro: o que estiver <b>entre os dois</b> — sem contar as duas pontas — fica guardado no
 *       cajado, e o marco some;</li>
 *   <li>daí em diante, cada batida <b>cola</b> o que foi guardado, uma casa acima do que se bateu, e
 *       <b>gira noventa graus</b> para a próxima vez;</li>
 *   <li>agachado, cola <b>sem girar</b> e zera o giro;</li>
 *   <li>e agachado <b>no ar</b>, esquece tudo o que tinha guardado.</li>
 * </ol>
 *
 * <p>A caixa é <b>aberta nas duas pontas</b>: marcando dois blocos a cinco de distância, copiam-se os
 * <b>quatro</b> do meio. É assim no original, e é o que torna cômodo marcar as quinas de fora de uma
 * construção.
 *
 * <h2>Dois desvios declarados</h2>
 *
 * <p><b>O giro.</b> O original arruma à mão o metadata de <b>escadas</b>, <b>troncos</b> e <b>portas</b>,
 * com três tabelas de quatro números cada — e erra tudo o resto, porque em 2014 não havia como perguntar a
 * um bloco qual é a cara dele girada. Hoje há: {@code BlockState.rotate} faz isso para <b>todos</b> os
 * blocos. Aqui gira-se com ele, que é mais fiel à intenção do que copiar as três tabelas seria à letra.
 *
 * <p><b>O arquivo de molde.</b> O original escreve também dois arquivos de texto com <b>código Java</b> que
 * reconstrói o que foi copiado — era assim que as peças da aldeia iam parar no código dele. Esse código é
 * da 1.7.10 e não compila contra este porte; a metade que escreve arquivo fica de fora.
 */
public class DuplicationStaffItem extends Item {
    /** Quantos giros há: os quatro do original. */
    public static final int GIROS = 4;

    public DuplicationStaffItem(Properties properties) {
        super(properties);
    }

    /** O feitio dele: um só, e épico, como no original. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1).rarity(Rarity.EPIC);
    }

    /**
     * O que o cajado guardou: as medidas e os blocos, de baixo para cima e de x em z.
     *
     * <p>A ordem é a do original — para cada x, para cada z, a coluna inteira de baixo para cima — e tem de
     * ser, porque é ela que o colar lê de volta.
     */
    public record Molde(int largo, int alto, int fundo, List<BlockState> blocos) {
        public static final Codec<Molde> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                Codec.INT.fieldOf("largo").forGetter(Molde::largo),
                Codec.INT.fieldOf("alto").forGetter(Molde::alto),
                Codec.INT.fieldOf("fundo").forGetter(Molde::fundo),
                BlockState.CODEC.listOf().fieldOf("blocos").forGetter(Molde::blocos)
        ).apply(campo, Molde::new));

        public boolean vazio() {
            return this.largo <= 0 || this.alto <= 0 || this.fundo <= 0 || this.blocos.isEmpty();
        }
    }

    // ------------------------------------------------------------------ o que ele carrega

    public static @Nullable BlockPos marco(ItemStack cajado) {
        return cajado.get(OccultaComponents.STAFF_MARK);
    }

    public static @Nullable Molde molde(ItemStack cajado) {
        return cajado.get(OccultaComponents.STAFF_MOULD);
    }

    public static int giro(ItemStack cajado) {
        return cajado.getOrDefault(OccultaComponents.STAFF_TURN, 0);
    }

    // ------------------------------------------------------------------ a mão

    /** <b>Agachado no ar, esquece tudo.</b> */
    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        if (!quem.isShiftKeyDown()) return InteractionResult.PASS;
        if (mundo instanceof ServerLevel level) {
            ItemStack cajado = quem.getItemInHand(mão);
            cajado.remove(OccultaComponents.STAFF_MOULD);
            cajado.remove(OccultaComponents.STAFF_MARK);
            cajado.remove(OccultaComponents.STAFF_TURN);
            level.playSound(null, quem.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                    1.0f, 1.0f);
            quem.sendSystemMessage(Component.translatable("tc.dupstaff.cleared"));
        }
        return InteractionResult.SUCCESS;
    }

    /** E numa batida: marcar, guardar ou colar, nessa ordem de prioridade. */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        Level mundo = onde.getLevel();
        if (!(mundo instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Player quem = onde.getPlayer();
        if (quem == null) return InteractionResult.PASS;
        ItemStack cajado = onde.getItemInHand();
        BlockPos casa = onde.getClickedPos();

        Molde tem = molde(cajado);
        if (tem != null && !tem.vazio()) {
            cola(level, cajado, casa.above(), quem, !quem.isShiftKeyDown());
            return InteractionResult.SUCCESS;
        }
        BlockPos marcado = marco(cajado);
        if (marcado != null) {
            guarda(level, cajado, marcado, casa, quem);
            return InteractionResult.SUCCESS;
        }
        marca(level, cajado, casa, quem);
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ as três coisas que ele faz

    /** <b>O marco</b>: uma quina da caixa. */
    private static void marca(ServerLevel level, ItemStack cajado, BlockPos casa, Player quem) {
        cajado.set(OccultaComponents.STAFF_MARK, casa);
        faísca(level, casa);
        quem.sendSystemMessage(Component.translatable("tc.dupstaff.marked",
                casa.getX(), casa.getY(), casa.getZ()));
    }

    /**
     * <b>Guarda o que está entre as duas quinas</b>, sem contar as pontas.
     *
     * <p>Duas quinas num mesmo plano não encerram nada: o original pede que as <b>três</b> contas sejam
     * diferentes, e aí a caixa de dentro tem pelo menos um bloco. Não sendo, não se guarda nada — e o
     * marco fica, para se poder tentar outra quina.
     */
    private static void guarda(ServerLevel level, ItemStack cajado, BlockPos marco, BlockPos casa,
                               Player quem) {
        int largo = Math.max(marco.getX(), casa.getX()) - Math.min(marco.getX(), casa.getX()) - 1;
        int alto = Math.max(marco.getY(), casa.getY()) - Math.min(marco.getY(), casa.getY()) - 1;
        int fundo = Math.max(marco.getZ(), casa.getZ()) - Math.min(marco.getZ(), casa.getZ()) - 1;

        List<BlockState> blocos = new ArrayList<>();
        if (marco.getX() != casa.getX() && marco.getY() != casa.getY() && marco.getZ() != casa.getZ()) {
            int deX = Math.min(marco.getX(), casa.getX()) + 1;
            int deY = Math.min(marco.getY(), casa.getY()) + 1;
            int deZ = Math.min(marco.getZ(), casa.getZ()) + 1;
            for (int x = deX; x < Math.max(marco.getX(), casa.getX()); x++) {
                for (int z = deZ; z < Math.max(marco.getZ(), casa.getZ()); z++) {
                    for (int y = deY; y < Math.max(marco.getY(), casa.getY()); y++) {
                        blocos.add(level.getBlockState(new BlockPos(x, y, z)));
                    }
                }
            }
        }

        if (blocos.isEmpty()) {
            level.sendParticles(ParticleTypes.SMOKE, casa.getX() + 0.5, casa.getY(), casa.getZ() + 0.5,
                    16, 1.0, 1.0, 1.0, 0.0);
            level.playSound(null, casa, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS,
                    1.0f, 1.0f);
            quem.sendSystemMessage(Component.translatable("tc.dupstaff.nothing"));
            return;
        }

        cajado.set(OccultaComponents.STAFF_MOULD, new Molde(largo, alto, fundo, List.copyOf(blocos)));
        cajado.remove(OccultaComponents.STAFF_MARK);
        faísca(level, casa);
        quem.sendSystemMessage(Component.translatable("tc.dupstaff.saved", blocos.size(),
                largo, alto, fundo));
    }

    /**
     * <b>Cola</b> o que estava guardado, e gira para a próxima.
     *
     * <p>Girando, o molde roda à volta da quina em que se bateu — é o que o original faz, percorrendo a
     * lista ao contrário num eixo ou nos dois conforme o giro.
     */
    private static void cola(ServerLevel level, ItemStack cajado, BlockPos casa, Player quem,
                             boolean girando) {
        Molde molde = molde(cajado);
        if (molde == null || molde.vazio()) return;

        int qual = girando ? (giro(cajado) + 1) % GIROS : 0;
        cajado.set(OccultaComponents.STAFF_TURN, qual);
        desenha(level, casa, molde, daVolta(qual));
        level.playSound(null, casa, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    /** O giro do jogo que corresponde ao número que o cajado guarda. */
    public static Rotation daVolta(int qual) {
        return switch (Math.floorMod(qual, GIROS)) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /**
     * Desenha o molde no mundo, a partir daquela quina.
     *
     * <p>A leitura é sempre a mesma — x, depois z, depois a coluna —, e quem muda é <b>onde</b> cada coluna
     * vai parar. É a mesma conta das quatro voltas do original, escrita uma vez em vez de quatro.
     */
    public static void desenha(ServerLevel level, BlockPos onde, Molde molde, Rotation volta) {
        int i = 0;
        for (int x = 0; x < molde.largo(); x++) {
            for (int z = 0; z < molde.fundo(); z++) {
                for (int y = 0; y < molde.alto(); y++, i++) {
                    if (i >= molde.blocos().size()) return;
                    BlockState qual = molde.blocos().get(i).rotate(volta);
                    BlockPos ali = onde.offset(viraX(x, z, molde, volta), y,
                            viraZ(x, z, molde, volta));
                    level.setBlock(ali, qual, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static int viraX(int x, int z, Molde molde, Rotation volta) {
        return switch (volta) {
            case CLOCKWISE_90 -> molde.fundo() - 1 - z;
            case CLOCKWISE_180 -> molde.largo() - 1 - x;
            case COUNTERCLOCKWISE_90 -> z;
            default -> x;
        };
    }

    private static int viraZ(int x, int z, Molde molde, Rotation volta) {
        return switch (volta) {
            case CLOCKWISE_90 -> x;
            case CLOCKWISE_180 -> molde.fundo() - 1 - z;
            case COUNTERCLOCKWISE_90 -> molde.largo() - 1 - x;
            default -> z;
        };
    }

    private static void faísca(ServerLevel level, BlockPos casa) {
        level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                        ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                casa.getX() + 0.5, casa.getY() + 1.0, casa.getZ() + 0.5, 16, 1.0, 1.0, 1.0, 0.0);
        level.playSound(null, casa, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
