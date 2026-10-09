package net.thaumcraft.occulta;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

/**
 * O <b>Talismã de Círculo</b>: a {@code ItemCircleTalisman} do Witchery.
 *
 * <p>É um <b>carimbo de círculo</b>. Um rito o enche com o desenho do círculo em que se está — qual giz
 * riscou cada um dos três anéis —, e depois ele desenha esse círculo inteiro noutro lugar, com um clique.
 *
 * <p>Quem faz rituais sabe por que isto existe: um círculo de três anéis são <b>oitenta e quatro</b>
 * glifos riscados um a um, de joelhos, e o giz gasta-se. O talismã é o que torna um ritual uma coisa que
 * se repete em vez de uma coisa que se constrói.
 *
 * <h2>O que ele guarda</h2>
 *
 * <p>Três números, um por anel, e cada um diz <b>qual giz</b> o riscou inteiro: nada, Ritual, Alhures ou
 * Infernal. Um anel de dois gizes misturados não entra no talismã — ele guarda círculos <b>puros</b>, que
 * são os que os ritos pedem.
 *
 * <p>O original os empacota nos três bits de cada do dano do item; aqui são o mesmo número num componente,
 * para que a conta e as dez figuras continuem a ser as dele.
 */
public class CircleTalismanItem extends Item {
    /** Quantos bits cada anel ocupa, e a máscara deles. */
    public static final int BITS = 3;
    public static final int MÁSCARA = 7;

    /** Os três gizes, pela ordem do original: um, dois e três. */
    public static final int NENHUM = 0;
    public static final int RITUAL = 1;
    public static final int ALHURES = 2;
    public static final int INFERNAL = 3;

    /** O lado do desenho, que é o dos três anéis. */
    public static final int LADO = 17;

    public CircleTalismanItem(Properties properties) {
        super(properties);
    }

    // ------------------------------------------------------------------ o que ele guarda

    /** O número empacotado deste talismã. */
    public static int guardado(ItemStack talismã) {
        return talismã.getOrDefault(OccultaComponents.CIRCLE_RINGS, 0);
    }

    /** E os três anéis dele, de dentro para fora. */
    public static int anel(int guardado, int qual) {
        return guardado >>> (BITS * qual) & MÁSCARA;
    }

    /** Empacota três anéis num número. */
    public static int empacota(int dentro, int meio, int fora) {
        return fora << (BITS * 2) | meio << BITS | dentro;
    }

    /**
     * Escreve o desenho no talismã — e põe junto a figura que lhe toca.
     *
     * <p>A figura é a do <b>maior anel riscado</b>: havendo o de fora, é a dele; senão a do meio; senão a
     * de dentro. É a conta do {@code getIconFromDamage} do original, e é por isso que um talismã de três
     * anéis e um de um anel de fora têm a mesma cara.
     */
    public static ItemStack escrito(int guardado) {
        ItemStack feito = new ItemStack(OccultaItems.CIRCLE_TALISMAN);
        if (guardado <= 0) return feito;
        feito.set(OccultaComponents.CIRCLE_RINGS, guardado);
        feito.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(),
                List.of(String.valueOf(figura(guardado))), List.of()));
        return feito;
    }

    /** Qual das dez figuras este talismã mostra. */
    public static int figura(int guardado) {
        int dentro = anel(guardado, 0);
        int meio = anel(guardado, 1);
        int fora = anel(guardado, 2);
        if (fora > 0) return fora + 6;
        if (meio > 0) return meio + 3;
        return dentro;
    }

    // ------------------------------------------------------------------ o carimbo

    /**
     * O clique: confere o lugar inteiro e só então risca.
     *
     * <p>São <b>duas voltas</b> pelo desenho, como no original: a primeira pergunta se cabe tudo, a
     * segunda risca. Faltando uma casa, nada se risca e o talismã não se gasta — que é o que torna o
     * carimbo seguro de usar num terreno acidentado.
     */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        if (!(onde.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Player quem = onde.getPlayer();
        ItemStack talismã = onde.getItemInHand();
        int guardado = guardado(talismã);

        BlockPos batido = onde.getClickedPos();
        boolean noCoração = level.getBlockState(batido).is(OccultaBlocks.CIRCLE_HEART);
        BlockPos meio = noCoração ? batido : batido.above();
        if (!noCoração && !OccultaBlocks.CIRCLE_HEART.defaultBlockState().canSurvive(level, meio)) {
            recusa(level, quem);
            return InteractionResult.SUCCESS;
        }
        if (guardado <= 0) {
            recusa(level, quem);
            return InteractionResult.SUCCESS;
        }

        if (!risca(level, meio, guardado, false)) {
            recusa(level, quem);
            return InteractionResult.SUCCESS;
        }
        risca(level, meio, guardado, true);
        if (!noCoração) {
            level.setBlockAndUpdate(meio, OccultaBlocks.CIRCLE_HEART.defaultBlockState());
        }

        gasta(talismã, quem);
        return InteractionResult.SUCCESS;
    }

    /**
     * Uma volta pelo desenho: confere, ou risca.
     *
     * @return se coube
     */
    private static boolean risca(ServerLevel level, BlockPos meio, int guardado, boolean valendo) {
        int raio = (LADO - 1) / 2;
        for (int z = 0; z < LADO; z++) {
            String linha = RitualCircles.linha(z);
            for (int x = 0; x < LADO; x++) {
                char qual = linha.charAt(x);
                if (qual == '.') continue;
                int giz = anel(guardado, qual == 'a' ? 0 : qual == 'b' ? 1 : 2);
                if (giz == NENHUM) continue;

                BlockPos casa = meio.offset(x - raio, 0, z - raio);
                Block glifo = switch (giz) {
                    case RITUAL -> OccultaBlocks.RITUAL_GLYPH;
                    case ALHURES -> OccultaBlocks.OTHERWHERE_GLYPH;
                    default -> OccultaBlocks.INFERNAL_GLYPH;
                };
                BlockState feitio = glifo.defaultBlockState();
                if (!level.getBlockState(casa).canBeReplaced()) return false;
                if (!feitio.canSurvive(level, casa)) return false;
                if (valendo) level.setBlockAndUpdate(casa, feitio);
            }
        }
        return true;
    }

    /**
     * O talismã gasto volta a ser branco.
     *
     * <p>No original, uma pilha de talismãs gasta <b>um</b> e devolve um branco à mochila; um talismã
     * sozinho simplesmente se apaga. Aqui é a mesma conta.
     */
    private static void gasta(ItemStack talismã, Player quem) {
        if (quem != null && quem.hasInfiniteMaterials()) return;
        if (talismã.getCount() > 1) {
            talismã.shrink(1);
            ItemStack branco = new ItemStack(OccultaItems.CIRCLE_TALISMAN);
            if (quem != null && !quem.getInventory().add(branco)) quem.drop(branco, false);
            return;
        }
        talismã.remove(OccultaComponents.CIRCLE_RINGS);
        talismã.remove(DataComponents.CUSTOM_MODEL_DATA);
    }

    private static void recusa(ServerLevel level, @org.jetbrains.annotations.Nullable Player quem) {
        if (quem == null) return;
        level.playSound(null, quem.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    // ------------------------------------------------------------------ o que ele diz

    @Override
    public Component getName(ItemStack talismã) {
        int guardado = guardado(talismã);
        if (guardado <= 0) return super.getName(talismã);
        return Component.translatable("item.thaumcraft.circle_talisman.written",
                super.getName(talismã), diz(guardado));
    }

    /** Os anéis por extenso, de dentro para fora, como o original os escreve. */
    public static Component diz(int guardado) {
        var feito = Component.empty();
        boolean primeiro = true;
        String[] tamanhos = {"small", "medium", "large"};
        for (int qual = 0; qual < 3; qual++) {
            int giz = anel(guardado, qual);
            if (giz == NENHUM) continue;
            if (!primeiro) feito.append(", ");
            feito.append(Component.translatable(
                    "tc.circletalisman." + tamanhos[qual] + "." + giz));
            primeiro = false;
        }
        return feito;
    }

    @Override
    public void appendHoverText(ItemStack talismã, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        linha.accept(Component.translatable("tc.circletalisman.tip").withStyle(ChatFormatting.BLUE));
    }
}
