package net.thaumcraft.occulta.louse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.thaumcraft.occulta.OccultaItems;

import java.util.List;

/**
 * O que um <b>piolho na mochila</b> e um <b>Cinto Mordedor</b> fazem quando quem os traz apanha: o pedaço
 * do {@code GenericEvents} do Witchery que corre no golpe levado.
 *
 * <p>Os dois fazem a mesma coisa e a ordem entre eles importa, que é por estarem aqui juntos: <b>primeiro
 * o piolho</b>, e só se não houver nenhum é que o <b>cinto</b> fala. Uma pancada gasta um dos dois, nunca
 * os dois.
 *
 * <h2>Para onde vai a poção</h2>
 *
 * <p>Esta é a ideia, e é boa: a poção vai para um dos dois lados conforme <b>o que ela é</b>. Sendo das
 * <b>agressivas</b> — lentidão, fraqueza, veneno, definhar, dano e fome —, vai para <b>quem bateu</b>;
 * sendo qualquer outra, fica em <b>quem a trazia</b>. Quer dizer que o mesmo cinto é armadura ou arma
 * conforme o que se lhe puser dentro, e quem o enche escolhe o quê.
 *
 * <p>A <b>Regeneração</b> é a exceção escrita à mão no original: ela fica sempre em quem traz o cinto,
 * mesmo não sendo a sua vez.
 */
public final class Lice {
    /** O que o piolho da mochila custa a quem o traz: um de dano. */
    public static final float O_PIOLHO_DÓI = 1.0f;

    private Lice() {
    }

    /**
     * As <b>seis poções agressivas</b> do {@code isPotionAggressive}.
     *
     * <p>Repare no que <b>não</b> está na lista: a cegueira, a náusea, a lentidão de mineração. Elas não
     * são agressivas aos olhos do original — e por isso um cinto cheio de cegueira cega <b>quem o traz</b>.
     */
    public static boolean agressiva(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> qual) {
        return qual == MobEffects.WEAKNESS || qual == MobEffects.SLOWNESS || qual == MobEffects.POISON
                || qual == MobEffects.WITHER || qual == MobEffects.INSTANT_DAMAGE
                || qual == MobEffects.HUNGER;
    }

    /**
     * O golpe levado: primeiro o piolho da mochila, depois o cinto.
     *
     * @return se alguma coisa foi gasta
     */
    public static boolean levouGolpe(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        return oPiolho(level, quem, fonte) || oCinto(level, quem, fonte);
    }

    /**
     * <b>O piolho da mochila</b>: o primeiro cheio que houver morde, e o piolho fica vazio.
     *
     * <p>E ele <b>custa</b>: quem o traz leva um de dano do bicho, que é o original lembrando de que um
     * piolho na mochila é um piolho.
     */
    private static boolean oPiolho(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        var mochila = quem.getInventory();
        for (int lugar = 0; lugar < mochila.getContainerSize(); lugar++) {
            ItemStack coisa = mochila.getItem(lugar);
            if (!coisa.is(OccultaItems.LOUSE) || !LouseItem.cheio(coisa)) continue;

            PotionContents tem = LouseItem.poção(coisa);
            dá(level, quem, fonte, tem);
            coisa.remove(DataComponents.POTION_CONTENTS);
            quem.hurtServer(level, level.damageSources().generic(), O_PIOLHO_DÓI);
            return true;
        }
        return false;
    }

    /** <b>O Cinto Mordedor</b>: as duas poções que ele guarda, uma por pancada. */
    private static boolean oCinto(ServerLevel level, ServerPlayer quem, DamageSource fonte) {
        ItemStack cinto = quem.getItemBySlot(EquipmentSlot.LEGS);
        if (!cinto.is(OccultaItems.BITING_BELT)) return false;

        List<PotionContents> tem = BitingBelt.poções(cinto);
        if (tem.isEmpty()) return false;

        dá(level, quem, fonte, tem.getFirst());
        BitingBelt.gasta(cinto);
        return true;
    }

    /** E para onde cada efeito vai. */
    private static void dá(ServerLevel level, ServerPlayer quem, DamageSource fonte,
                           PotionContents oquê) {
        LivingEntity quemBateu = fonte.getEntity() instanceof LivingEntity vivo ? vivo : null;
        for (MobEffectInstance cada : oquê.getAllEffects()) {
            if (quem.hasEffect(cada.getEffect())) continue;
            boolean paraOOutro = agressiva(cada.getEffect())
                    && cada.getEffect() != MobEffects.REGENERATION && quemBateu != null;
            (paraOOutro ? quemBateu : quem).addEffect(new MobEffectInstance(cada));
        }
    }
}
