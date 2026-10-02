package net.thaumcraft.occulta.village;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Defender a aldeia de quem a aldeia odeia: o {@code EntityAIDefendVillageGeneric} do Witchery.
 *
 * <p>O jogo de hoje já tem esta mira — o {@code DefendVillageTargetGoal} —, mas ela está <b>presa ao golem de
 * ferro</b> no tipo: pede um {@code IronGolem} e não um bicho qualquer. O Witchery tinha escrito a dele
 * justamente para a poder dar a outro bicho, e é por isso que esta classe existe: é a mesma conta, aberta a
 * qualquer um. O nome do original diz isso — <i>genérica</i>.
 *
 * <p><b>Como ela escolhe.</b> Olha-se dez por oito por dez em volta; de todo aldeão ali, pergunta-se o que ele
 * acha de cada jogador ali. Quem estiver em <b>cem abaixo de zero</b> com algum deles passa a ser alvo. Em
 * criativo e em espectador não se mexe.
 *
 * <p><b>Desvio declarado.</b> O original fazia duas perguntas à aldeia: <i>quem a atacou</i>
 * ({@code findNearestVillageAggressor}) e <i>com quem ela está mal</i> ({@code getNearestTargetPlayer}, uma em
 * vinte voltas). A primeira pergunta deixou de existir: no jogo de hoje a aldeia não é um objeto que guarda quem
 * a agrediu — não há a quem perguntar. Ela está feita de outro jeito no guarda, que caça monstro por conta
 * própria e caça gente que bate em quem mora ali. O que sobra para aqui é a segunda, que é a que a reputação de
 * hoje sabe responder.
 */
public class DefendVillageGoal extends TargetGoal {
    /** A caixa em que se procura, pelos números do original. */
    private static final double RANGE_XZ = 10.0;
    private static final double RANGE_Y = 8.0;

    /** E o quanto a aldeia tem de odiar alguém para o guarda ir atrás: os cem do jogo de hoje. */
    private static final int HATED = -100;

    private final Mob guarda;
    private final TargetingConditions quemConta = TargetingConditions.forCombat().range(64.0);

    private @Nullable LivingEntity achado;

    public DefendVillageGoal(Mob guarda) {
        super(guarda, false, true);
        this.guarda = guarda;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        AABB roda = this.guarda.getBoundingBox().inflate(RANGE_XZ, RANGE_Y, RANGE_XZ);
        ServerLevel level = getServerLevel(this.guarda);
        List<Villager> aldeões = level.getNearbyEntities(Villager.class, this.quemConta, this.guarda, roda);
        List<Player> gente = level.getNearbyPlayers(this.quemConta, this.guarda, roda);

        this.achado = null;
        for (Villager aldeão : aldeões) {
            for (Player quem : gente) {
                if (aldeão.getPlayerReputation(quem) <= HATED) this.achado = quem;
            }
        }

        if (this.achado == null) return false;
        return !(this.achado instanceof Player quem && (quem.isSpectator() || quem.isCreative()));
    }

    @Override
    public void start() {
        this.guarda.setTarget(this.achado);
        super.start();
    }
}
