package net.thaumcraft.occulta.coven;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * O que uma bruxa do coven pede antes de entrar: os {@code Quest} do {@code EntityCovenWitch}.
 *
 * <p>São de duas naturezas. Ou ela <b>solta um bicho</b> e manda matá-lo, ou ela <b>pede uma coisa</b> e manda
 * buscá-la. Nos dois casos se volta a falar com ela, e quando o que ela pediu está feito ela entra no coven.
 *
 * <p><b>Três das sete do original, e as outras quatro ficam de fora declaradas.</b> O original pede, além do
 * que está aqui, um <b>Coração de Demônio</b>, uma <b>Bola de Cristal</b>, cinco <b>Cozimentos Grotescos</b> e
 * uma <b>Pedra Necro</b> — e nenhuma dessas quatro coisas está portada; o Coração de Demônio e o Grotesco já
 * estavam escritos como buraco no {@code PORTE.md} antes desta fatia. Elas voltam quando os itens vierem.
 */
public sealed interface CovenQuest {
    /** A chave da fala com que ela pede. */
    String chave();

    /** Quantos é preciso trazer. Zero quer dizer que não é de trazer, é de matar. */
    default int quantos() {
        return 0;
    }

    /** O que ela faz na hora em que se aceita. */
    default void aceita(ServerLevel level, LivingEntity bruxa, Player quem) {
    }

    /** Se esta coisa na mão serve ao que ela pediu. */
    default boolean serve(ItemStack coisa) {
        return false;
    }

    /** As três que dão para pedir. */
    List<CovenQuest> TODAS = List.of(
            new Briga("quest.fightspider", EntityTypes.SPIDER),
            new Briga("quest.fightzombie", EntityTypes.ZOMBIE),
            new Busca("quest.getbones", Items.BONE, 30));

    /**
     * A de brigar: ela <b>solta o bicho ali</b> e manda resolver.
     *
     * <p>No original o bicho é dela — nasce já a atacar quem aceitou —, e é por isso que a prova dela é ela
     * mesma: quem aceita tem de matar o que ela soltou.
     */
    record Briga(String chave, net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> bicho)
            implements CovenQuest {
        @Override
        public void aceita(ServerLevel level, LivingEntity bruxa, Player quem) {
            var solto = this.bicho().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (solto == null) return;
            solto.snapTo(bruxa.getX(), bruxa.getY(), bruxa.getZ(), bruxa.getYRot(), 0.0f);
            solto.finalizeSpawn(level, level.getCurrentDifficultyAt(solto.blockPosition()),
                    EntitySpawnReason.MOB_SUMMONED, null);
            solto.setTarget(quem);
            level.addFreshEntity(solto);
        }
    }

    /** E a de buscar: ela pede um tanto de alguma coisa, e se conta na mão de quem volta. */
    record Busca(String chave, net.minecraft.world.item.Item coisa, int quantos) implements CovenQuest {
        @Override
        public boolean serve(ItemStack naMao) {
            return naMao.is(this.coisa());
        }
    }
}
