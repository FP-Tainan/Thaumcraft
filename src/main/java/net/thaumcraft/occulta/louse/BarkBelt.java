package net.thaumcraft.occulta.louse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.clothes.WitchClothesItem;

/**
 * O <b>Cinto de Casca</b>: o ramo {@code BARK_BELT} do {@code ItemWitchesClothes} do Witchery.
 *
 * <p>É o melhor cinto do ofício e o mais estranho de explicar: ele <b>junta madeira do chão</b> e a gasta
 * para <b>aparar golpes</b>.
 *
 * <ul>
 *   <li>de cem em cem batidas, estando quem o traz de pé em <b>grama ou micélio</b>, ele ganha uma carga
 *       — e rangue ao ganhá-la;</li>
 *   <li>o teto é <b>duas por peça de roupa de bruxa vestida</b>, de modo que um conjunto inteiro vale
 *       oito e um cinto sozinho vale duas;</li>
 *   <li>e levando uma pancada que <b>não seja de madeira</b>, ele gasta uma carga — ou <b>duas</b>, uma
 *       vez em quatro, se tiver mais de uma — e <b>cancela o golpe inteiro</b>, largando um <b>pau</b> por
 *       carga gasta, que fica três segundos no chão e some.</li>
 * </ul>
 *
 * <p>Repare na conta do teto: ela não é do cinto, é do <b>conjunto</b>. O Cinto de Casca é a única peça do
 * mod que paga por se vestir o resto — e é por isso que ele é o fim da linha das roupas de bruxa.
 */
public final class BarkBelt {
    /** De quanto em quanto ele junta: cem batidas. */
    public static final int JUNTA_DE = 100;

    /** Quanto cada peça de roupa de bruxa vestida vale de teto: duas. */
    public static final int POR_PEÇA = 2;

    /** A chance de uma pancada gastar duas cargas em vez de uma. */
    public static final double DUAS_DE_UMA_VEZ = 0.25;

    /** E quanto tempo o pau largado fica no chão: três segundos. */
    public static final int O_PAU_DURA = 60;

    /** Quanto uma coisa largada vive, no jogo de hoje. */
    public static final int UMA_COISA_VIVE = 6000;

    private BarkBelt() {
    }

    /** Quantas cargas este cinto tem. */
    public static int carga(ItemStack cinto) {
        return cinto.getOrDefault(DataComponents.DAMAGE, 0) > 0 ? 0 : cargaCrua(cinto);
    }

    private static int cargaCrua(ItemStack cinto) {
        return cinto.getOrDefault(net.thaumcraft.occulta.OccultaComponents.BARK_PIECES, 0);
    }

    /** Põe-lhe tantas. */
    public static void carga(ItemStack cinto, int quanta) {
        if (quanta <= 0) {
            cinto.remove(net.thaumcraft.occulta.OccultaComponents.BARK_PIECES);
            return;
        }
        cinto.set(net.thaumcraft.occulta.OccultaComponents.BARK_PIECES, quanta);
    }

    /**
     * O <b>teto</b>: duas por peça de roupa de bruxa vestida.
     *
     * <p>O original conta as quatro casas de armadura e soma duas por cada uma que for roupa de bruxa — e
     * o próprio cinto conta, de modo que o mínimo é dois.
     */
    public static int teto(net.minecraft.world.entity.LivingEntity quem) {
        int quanto = 0;
        for (EquipmentSlot casa : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (quem.getItemBySlot(casa).getItem() instanceof WitchClothesItem) quanto += POR_PEÇA;
        }
        return quanto;
    }

    /**
     * A batida: de cem em cem, em grama ou micélio, ele junta uma.
     */
    public static void junta(ServerLevel level, ServerPlayer quem) {
        if (level.getGameTime() % JUNTA_DE != 0) return;
        ItemStack cinto = quem.getItemBySlot(EquipmentSlot.LEGS);
        if (!cinto.is(OccultaItems.BARK_BELT)) return;

        BlockPos sob = quem.blockPosition().below();
        var chão = level.getBlockState(sob);
        if (!chão.is(Blocks.GRASS_BLOCK) && !chão.is(Blocks.MYCELIUM)) return;

        int tem = carga(cinto);
        int teto = teto(quem);
        if (tem >= teto) return;

        carga(cinto, Math.min(tem + 1, teto));
        level.playSound(null, quem.blockPosition(), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS,
                0.5f, 0.8f + level.getRandom().nextFloat() * 0.4f);
    }

    /**
     * O golpe: gasta carga e <b>apara tudo</b>, largando um pau por carga gasta.
     *
     * <p>A guarda do original é uma só e é boa: <b>golpe de madeira não se apara com madeira</b>. Uma
     * espada de pau atravessa o cinto como se ele não estivesse lá.
     *
     * @return se o golpe foi aparado
     */
    public static boolean apara(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        if (fonte.getEntity() == null) return false;
        if (deMadeira(fonte)) return false;
        ItemStack cinto = quem.getItemBySlot(EquipmentSlot.LEGS);
        if (!cinto.is(OccultaItems.BARK_BELT)) return false;

        int tem = Math.min(carga(cinto), teto(quem));
        if (tem <= 0) return false;

        int gasta = tem > 1 && level.getRandom().nextDouble() < DUAS_DE_UMA_VEZ ? 2 : 1;
        carga(cinto, Math.max(tem - gasta, 0));

        for (int volta = 0; volta < gasta; volta++) {
            double dx = level.getRandom().nextBoolean() ? -1.0 : 1.0;
            double dz = level.getRandom().nextBoolean() ? -1.0 : 1.0;
            ItemEntity pau = new ItemEntity(level, quem.getX() + dx, quem.getY() + 1.5,
                    quem.getZ() + dz, new ItemStack(Items.STICK));
            pau.setPickUpDelay(O_PAU_DURA);
            // e ele nasce velho, para sumir nos três segundos do original
            ((net.thaumcraft.mixin.ItemEntityAccessor) pau).thaumcraft$age(UMA_COISA_VIVE - O_PAU_DURA);
            level.addFreshEntity(pau);
        }
        return true;
    }

    /**
     * Se este golpe é <b>de madeira</b>: o {@code CreatureUtil.isWoodenDamage} do original.
     *
     * <p>São duas coisas e só duas: uma <b>espada de pau</b> na mão de quem bateu — e <b>só espada</b>, que
     * um machado de pau não conta —, e o <b>murro do Caçador Cornudo</b>, que é de madeira porque ele é.
     *
     * <p>O «material de madeira» de 2014 é hoje o rótulo do que <b>conserta</b> a ferramenta, que é por
     * onde se lhe pergunta de que ela é feita.
     */
    public static boolean deMadeira(DamageSource fonte) {
        if (!(fonte.getEntity() instanceof net.minecraft.world.entity.LivingEntity quemBateu)) {
            return false;
        }
        if (quemBateu instanceof net.thaumcraft.occulta.wolf.HornedHuntsmanEntity
                && !fonte.is(DamageTypeTags.IS_PROJECTILE)) {
            return true;
        }

        ItemStack arma = quemBateu.getMainHandItem();
        if (!arma.is(net.minecraft.tags.ItemTags.SWORDS)) return false;
        var conserta = arma.get(DataComponents.REPAIRABLE);
        return conserta != null && conserta.items().unwrapKey()
                .map(qual -> qual == net.minecraft.tags.ItemTags.WOODEN_TOOL_MATERIALS)
                .orElse(false);
    }
}
