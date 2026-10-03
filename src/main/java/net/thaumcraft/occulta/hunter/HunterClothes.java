package net.thaumcraft.occulta.hunter;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * As <b>roupas de caçador</b>: o {@code ItemHunterClothes} do Witchery.
 *
 * <p>São quatro peças, e cada uma existe em três feitios: a <b>lisa</b>, a <b>prateada</b> e a <b>da
 * aurora</b> — que é prateada <i>e</i> com alho, e por isso vale contra os dois. A proteção delas é a de
 * couro; o que vale nelas não é o couro.
 *
 * <p><b>O que vale é o conjunto</b>, e só o conjunto inteiro:
 *
 * <ul>
 *   <li><b>De magia</b>, uma vez em quatro: o tormento do vodu não chega.</li>
 *   <li><b>De maldição</b>, nove vezes em dez: a maldição não pega.</li>
 *   <li><b>O virote que drena vira o que drena com força</b>, que é a única forma de o haver.</li>
 *   <li><b>E de lobo</b>, se as quatro forem prateadas.</li>
 * </ul>
 *
 * <p><b>E o conjunto cobra.</b> Quem o veste <b>não pode usar boneca nenhuma</b> — nem a que o salva da morte,
 * nem a que lhe guarda as ferramentas. É o preço inteiro do ofício: quem caça bruxas não anda com a magia das
 * bruxas no bolso.
 *
 * <p>As prateadas e as com alho fazem mais uma coisa, e é a melhor delas: a pancada de um <b>lobisomem</b> numa
 * peça prateada — ou de um <b>vampiro</b> numa com alho — vale <b>duas vezes e meia</b> menos, <b>não gasta a
 * peça</b>, e <b>queima quem bateu</b>. A roupa não é armadura: é uma armadilha vestida.
 *
 * <p><b>Fica de fora, declarado:</b> no original, um jogador que <i>seja</i> lobisomem e vista prata — ou que
 * seja vampiro e vista alho — <b>queima-se a si mesmo</b>, um de dano por segundo. Isso pede a licantropia e o
 * vampirismo <b>de jogador</b>, que este porte ainda não tem; o lugar onde a pergunta se faz está marcado em
 * {@link #quemVisteSeQueima(LivingEntity, ItemStack)}.
 */
public final class HunterClothes {
    /** De quantas em quantas a magia escapa: uma em quatro. */
    public static final double DE_MAGIA = 0.25;

    /** E a maldição: nove em dez. */
    public static final double DE_MALDIÇÃO = 0.9;

    /** O quanto a peça certa vale contra a pancada certa. */
    public static final double VALE_MAIS = 2.5;

    /** E quanto queima quem bate nela. */
    public static final float QUEIMA = 1.0f;

    /** As quatro casas que o conjunto ocupa. */
    private static final EquipmentSlot[] CASAS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
    };

    /** As quatro lisas. */
    private static final Set<Item> LISAS = Set.of(
            OccultaItems.HUNTER_HAT, OccultaItems.HUNTER_COAT,
            OccultaItems.HUNTER_LEGS, OccultaItems.HUNTER_BOOTS);

    /** As quatro prateadas. */
    private static final Set<Item> PRATEADAS = Set.of(
            OccultaItems.SILVERED_HUNTER_HAT, OccultaItems.SILVERED_HUNTER_COAT,
            OccultaItems.SILVERED_HUNTER_LEGS, OccultaItems.SILVERED_HUNTER_BOOTS);

    /** E as quatro da aurora, que são prateadas <b>e</b> com alho. */
    private static final Set<Item> DA_AURORA = Set.of(
            OccultaItems.GARLICKED_HUNTER_HAT, OccultaItems.GARLICKED_HUNTER_COAT,
            OccultaItems.GARLICKED_HUNTER_LEGS, OccultaItems.GARLICKED_HUNTER_BOOTS);

    private HunterClothes() {
    }

    /** Se esta peça é roupa de caçador, de qualquer feitio. */
    public static boolean éRoupa(ItemStack peça) {
        Item qual = peça.getItem();
        return LISAS.contains(qual) || PRATEADAS.contains(qual) || DA_AURORA.contains(qual);
    }

    /** Se esta peça é prateada — e a da aurora também é. */
    public static boolean prateada(ItemStack peça) {
        Item qual = peça.getItem();
        return PRATEADAS.contains(qual) || DA_AURORA.contains(qual);
    }

    /** E se tem alho, que só a da aurora tem. */
    public static boolean comAlho(ItemStack peça) {
        return DA_AURORA.contains(peça.getItem());
    }

    /**
     * Se este tem as quatro peças vestidas: o {@code isFullSetWorn}.
     *
     * @param sóPrateadas se as quatro têm de ser prateadas — e as da aurora contam, porque são
     */
    public static boolean vestidoInteiro(@Nullable LivingEntity quem, boolean sóPrateadas) {
        if (quem == null) return false;
        for (EquipmentSlot casa : CASAS) {
            ItemStack peça = quem.getItemBySlot(casa);
            if (!éRoupa(peça)) return false;
            if (sóPrateadas && !prateada(peça)) return false;
        }
        return true;
    }

    /** Se a magia escapa desta vez: o {@code isMagicalProtectionActive}. */
    public static boolean protegeDeMagia(@Nullable LivingEntity quem) {
        return vestidoInteiro(quem, false) && quem.level().getRandom().nextDouble() < DE_MAGIA;
    }

    /** E se a maldição não pega: o {@code isCurseProtectionActive}. */
    public static boolean protegeDeMaldição(@Nullable LivingEntity quem) {
        return vestidoInteiro(quem, false) && quem.level().getRandom().nextDouble() < DE_MALDIÇÃO;
    }

    /** E se o lobo não morde: o {@code isWolfProtectionActive}. */
    public static boolean protegeDeLobo(@Nullable LivingEntity quem) {
        return vestidoInteiro(quem, true);
    }

    /**
     * <b>E não há boneca nenhuma para quem veste o conjunto</b>: o {@code findBoundPoppetInWorld}, que
     * devolve nada antes de procurar.
     *
     * <p>É o preço do ofício, e é cobrado <b>antes</b> de a boneca ser procurada — por isso não se gasta
     * nenhuma: ela simplesmente não é achada.
     */
    public static boolean semBonecas(@Nullable LivingEntity quem) {
        return vestidoInteiro(quem, false);
    }

    /** Se quem deu esta pancada é lobisomem. */
    public static boolean deLobisomem(@Nullable DamageSource fonte) {
        if (fonte == null) return false;
        Entity quem = fonte.getEntity();
        return quem instanceof LivingEntity vivo && net.thaumcraft.occulta.wolf.Lycanthropy.é(vivo);
    }

    /** E se é vampiro. */
    public static boolean deVampiro(@Nullable DamageSource fonte) {
        if (fonte == null) return false;
        Entity quem = fonte.getEntity();
        return quem instanceof LivingEntity vivo && net.thaumcraft.occulta.vampire.Vampirism.é(vivo);
    }

    /** Se esta peça é a certa contra esta pancada — e, se for, vale duas vezes e meia. */
    public static boolean peçaCerta(ItemStack peça, @Nullable DamageSource fonte) {
        if (prateada(peça) && deLobisomem(fonte)) return true;
        return comAlho(peça) && deVampiro(fonte);
    }

    /**
     * Se alguma peça vestida é a certa contra esta pancada: a que faz o dano valer menos, a peça não se gastar,
     * e quem bateu arder.
     */
    public static boolean algumaPeçaCerta(LivingEntity quem, @Nullable DamageSource fonte) {
        for (EquipmentSlot casa : CASAS) {
            if (peçaCerta(quem.getItemBySlot(casa), fonte)) return true;
        }
        return false;
    }

    /**
     * Se quem veste esta peça se queima nela.
     *
     * <p><b>É a costura da licantropia e do vampirismo de jogador.</b> Hoje devolve sempre falso, porque um
     * jogador ainda não pode ser nenhum dos dois; quando puder, é aqui que se pergunta — e nada mais muda.
     */
    public static boolean quemVisteSeQueima(LivingEntity quem, ItemStack peça) {
        if (prateada(peça) && net.thaumcraft.occulta.wolf.Lycanthropy.é(quem)) return true;
        return comAlho(peça) && net.thaumcraft.occulta.vampire.Vampirism.é(quem);
    }
}
