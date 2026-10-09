package net.thaumcraft.occulta.clothes;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * As <b>roupas de bruxa</b>: o que elas valem, que não é a proteção.
 *
 * <p>O chapéu e o manto protegem como couro, e isso é o menos importante que há neles. O que eles fazem é
 * <b>dobrar a produção de uma bruxa</b>, e a conta não está no item: está na Chaleira e no Caldeirão.
 *
 * <h2>Na Chaleira: a chance de um segundo frasco</h2>
 *
 * <table border="1">
 *   <caption>O que cada peça soma</caption>
 *   <tr><th>Quem</th><th>Um <b>segundo</b> frasco</th><th>E um <b>terceiro</b></th></tr>
 *   <tr><td>Chapéu de Bruxa</td><td>+35%</td><td>—</td></tr>
 *   <tr><td>Chapéu da Baba Yaga</td><td>+25%</td><td>+25%</td></tr>
 *   <tr><td>Manto de Bruxa, num cozimento que <b>não</b> é de Erguer</td><td>+35%</td><td>—</td></tr>
 *   <tr><td>Manto de Necromante, num cozimento <b>de Erguer</b></td><td>+35%</td><td>—</td></tr>
 *   <tr><td>Familiar com maestria de cozimento</td><td>+5%</td><td>+5% (só com o chapéu da Baba)</td></tr>
 * </table>
 *
 * <p>O chapéu e o manto <b>não se excluem</b>: com os dois, a chance de um segundo frasco é de <b>setenta
 * por cento</b>. E os dois mantos se excluem entre si <b>por tipo de cozimento</b> — o de Necromante só
 * ajuda no de Erguer, e o de Bruxa em todos os outros.
 *
 * <h2>No Caldeirão: o nível de equipamento</h2>
 *
 * <p>Ali não é chance, é <b>contagem</b>: o Chapéu de Bruxa vale um, o Manto vale um, o de Necromante vale
 * um, e o <b>Chapéu da Baba vale dois</b>. Com ele e um manto chega-se ao três, que é o teto.
 *
 * <p>É por isso que elas não são enfeite: <b>duas peças quase dobram o que uma bruxa produz</b>.
 */
public final class WitchClothes {
    /** O que o chapéu e o manto somam à chance de um frasco a mais. */
    public static final double HAT = 0.35;
    public static final double ROBE = 0.35;

    /** E o que o chapéu da Baba soma — menos no segundo, mas também no terceiro. */
    public static final double BABA = 0.25;

    /** O que o familiar com maestria de cozimento soma. */
    public static final double FAMILIAR = 0.05;

    /** Quanto cada peça vale no nível de equipamento do Caldeirão. */
    public static final int LEVEL_HAT = 1;
    public static final int LEVEL_BABA = 2;
    public static final int LEVEL_ROBE = 1;

    private WitchClothes() {
    }

    /** Se aquela peça é roupa de bruxa. */
    public static boolean is(ItemStack peça) {
        return peça.getItem() instanceof WitchClothesItem;
    }

    private static ItemStack na(@Nullable LivingEntity quem, EquipmentSlot casa) {
        return quem == null ? ItemStack.EMPTY : quem.getItemBySlot(casa);
    }

    public static boolean wearingHat(@Nullable LivingEntity quem) {
        return na(quem, EquipmentSlot.HEAD).is(OccultaItems.WITCH_HAT);
    }

    public static boolean wearingBabasHat(@Nullable LivingEntity quem) {
        return na(quem, EquipmentSlot.HEAD).is(OccultaItems.BABAS_HAT);
    }

    public static boolean wearingRobes(@Nullable LivingEntity quem) {
        return na(quem, EquipmentSlot.CHEST).is(OccultaItems.WITCH_ROBES);
    }

    public static boolean wearingNecroRobes(@Nullable LivingEntity quem) {
        return na(quem, EquipmentSlot.CHEST).is(OccultaItems.NECROMANCERS_ROBES);
    }

    // ------------------------------------------------------------------ a Chaleira

    /**
     * A chance de um <b>segundo</b> frasco: o pedaço do {@code BlockKettle} que soma as roupas.
     *
     * @param erguer se o que saiu é um <b>Cozimento de Erguer</b>, que é o que o Manto de Necromante serve
     */
    public static double secondBottle(@Nullable Player quem, boolean erguer) {
        if (quem == null) return 0.0;
        double chance = 0.0;
        if (wearingHat(quem)) chance += HAT;
        else if (wearingBabasHat(quem)) chance += BABA;

        if (!erguer && wearingRobes(quem)) chance += ROBE;
        else if (erguer && wearingNecroRobes(quem)) chance += ROBE;

        if (net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeCozimento(quem)) chance += FAMILIAR;
        return chance;
    }

    /**
     * E a de um <b>terceiro</b>, que só o <b>Chapéu da Baba Yaga</b> dá.
     *
     * <p>É o único jeito no mod inteiro de tirar três frascos de uma chaleira, e é por isso que o chapéu
     * dela é épico.
     */
    public static double thirdBottle(@Nullable Player quem) {
        if (quem == null || !wearingBabasHat(quem)) return 0.0;
        double chance = BABA;
        if (net.thaumcraft.occulta.familiar.Familiars.temMaestriaDeCozimento(quem)) chance += FAMILIAR;
        return chance;
    }

    // ------------------------------------------------------------------ o Caldeirão

    /**
     * O <b>nível de equipamento</b> do Caldeirão: o {@code gearLevel} do {@code BlockCauldron}.
     *
     * <p>Vai de zero a três, e o Chapéu da Baba sozinho já dá dois.
     */
    public static int gearLevel(@Nullable Player quem) {
        if (quem == null) return 0;
        int nível = 0;
        if (wearingHat(quem)) nível += LEVEL_HAT;
        if (wearingBabasHat(quem)) nível += LEVEL_BABA;
        if (wearingRobes(quem)) nível += LEVEL_ROBE;
        if (wearingNecroRobes(quem)) nível += LEVEL_ROBE;
        return nível;
    }
}
