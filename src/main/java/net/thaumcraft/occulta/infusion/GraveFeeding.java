package net.thaumcraft.occulta.infusion;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * <b>Matar cura</b>, com o Cozimento da Cova: o pedaço do {@code LivingDeathEvent} do Witchery.
 *
 * <p>Quem bebeu o {@linkplain InfusedBrews Cozimento da Cova} não come como gente: come do que mata. A
 * conta do original é por <b>feitio do morto</b>, e a diferença entre os três diz tudo sobre o que o
 * cozimento é —
 *
 * <ul>
 *   <li>um <b>bicho</b> dá oito de comida e um décimo da vida inteira;</li>
 *   <li>um <b>aldeão</b> dá a barriga cheia e quase metade da vida;</li>
 *   <li>e <b>outra pessoa</b> dá a barriga cheia e <b>três quintos</b> dela.</li>
 * </ul>
 *
 * <p>Repare que a conta é da <b>vida inteira</b> e não de pontos: quem tiver mais vida máxima cura mais.
 * E repare em quem dá mais. O cozimento não diz em palavra nenhuma o que quer de quem o bebeu; diz-o nesta
 * tabela.
 */
public final class GraveFeeding {
    /** A comida que cada um dá, e quanto dela se guarda. */
    public static final int COMIDA_DE_GENTE = 20;
    public static final float SABOR_DE_GENTE = 0.9f;
    public static final int COMIDA_DE_BICHO = 8;
    public static final float SABOR_DE_BICHO = 0.8f;

    private GraveFeeding() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((morto, porquê) -> {
            if (!(porquê.getEntity() instanceof Player quem)) return;
            if (!InfusedBrews.tem(quem, InfusedBrews.COVA)) return;
            come(quem, morto);
        });
    }

    /** O que aquele morto deu a quem o matou. */
    public static void come(Player quem, LivingEntity morto) {
        float vida = quem.getMaxHealth();
        if (morto instanceof Player) {
            quem.getFoodData().eat(COMIDA_DE_GENTE, SABOR_DE_GENTE);
            quem.heal(vida * InfusedBrews.DE_GENTE);
            return;
        }
        if (morto instanceof Villager) {
            quem.getFoodData().eat(COMIDA_DE_GENTE, SABOR_DE_GENTE);
            quem.heal(vida * InfusedBrews.DE_ALDEÃO);
            return;
        }
        if (morto instanceof Animal) {
            quem.getFoodData().eat(COMIDA_DE_BICHO, SABOR_DE_BICHO);
            quem.heal(vida * InfusedBrews.DE_BICHO);
        }
    }
}
