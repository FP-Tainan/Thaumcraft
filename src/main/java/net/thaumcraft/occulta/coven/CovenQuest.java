package net.thaumcraft.occulta.coven;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.ExtraDrops;
import net.thaumcraft.occulta.OccultaComponents;

/**
 * O que uma bruxa do coven pede antes de entrar: os {@code Quest} do {@code EntityCovenWitch}.
 *
 * <p>São de duas naturezas, e as duas acabam da mesma maneira: <b>trazendo-lhe uma coisa na mão</b>. Ou ela
 * <b>solta um bicho de estimação</b> e pede o olho dele, ou ela <b>pede uma coisa</b> e manda buscá-la.
 *
 * <p>São <b>sete</b> no original e são <b>seis</b> aqui; falta a que pede uma <b>Bola de Cristal</b>.
 */
public sealed interface CovenQuest {
    /** A chave da fala com que ela pede. */
    String chave();

    /** Quantos é preciso trazer. */
    default int quantos() {
        return 1;
    }

    /** O que ela faz na hora em que se aceita. */
    default void aceita(ServerLevel level, LivingEntity bruxa, Player quem) {
    }

    /** Se esta coisa na mão serve ao que <b>esta</b> bruxa pediu. */
    default boolean serve(ItemStack coisa, LivingEntity bruxa) {
        return false;
    }

    /**
     * As que dão para pedir.
     *
     * <p>São <b>sete</b> no original, e eram três aqui porque <b>as coisas que as outras pediam ainda não
     * existiam</b>. Com o Coração de Demônio, a Pedra Necrótica e o Cozimento Grotesco no mod, entram mais
     * três.
     *
     * <p><b>Declarado:</b> falta a sétima, que pede uma <b>Bola de Cristal</b>. Ela entra quando a bola vier.
     */
    List<CovenQuest> TODAS = List.of(
            new Briga("quest.fightspider", EntityTypes.SPIDER, Items.SPIDER_EYE, "peteye"),
            new Briga("quest.fightzombie", EntityTypes.ZOMBIE, Items.ROTTEN_FLESH, "petflesh"),
            new Busca("quest.getbones", Items.BONE, 30),
            new Busca("quest.getdemonheart", net.thaumcraft.occulta.OccultaItems.DEMON_HEART, 1),
            new Busca("quest.makegrotesquebrew", net.thaumcraft.occulta.OccultaItems.BREW_GROTESQUE, 5),
            new Busca("quest.makenecrostone", net.thaumcraft.occulta.OccultaItems.NECROTIC_STONE, 1));

    /**
     * A de brigar: ela <b>solta o bicho de estimação dela ali</b> e pede <b>o olho dele</b>.
     *
     * <p>E é esse pedido, e não a morte, que fecha a conta. O bicho nasce com <b>cem de vida</b> e <b>cinco
     * de dano</b> — cinco vezes o que um bicho daqueles tem —, com o <b>nome da bruxa</b> em cima, já virado
     * para quem aceitou; e nele vem <b>pendurada</b> uma coisa que não é dele: um olho de aranha, ou uma
     * carne podre, com o nome dela e a <b>marca dela</b> por dentro.
     *
     * <p>Morto o bicho, essa coisa cai. Levada de volta, ela a reconhece pela marca — e é por isso que
     * ninguém resolve o pedido de uma bruxa com o olho de aranha que tinha no bolso.
     *
     * <p>O zumbi dela leva ainda um <b>crânio</b> na cabeça, que é como se sabe de longe que aquele zumbi é
     * de alguém.
     *
     * <p><b>Fiel ao original:</b> o bicho <b>não passa pelo nascimento comum</b> — nada de armadura sorteada
     * nem de ajuste por dificuldade. Ele é feito à mão, posto no mundo e mandado atacar, e é só isso.
     */
    record Briga(String chave, net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> bicho,
                 net.minecraft.world.item.Item prova, String chaveDaProva) implements CovenQuest {
        /** O que o bicho dela tem a mais do que um bicho qualquer. */
        public static final double VIDA = 100.0;
        public static final double SOCO = 5.0;

        @Override
        public void aceita(ServerLevel level, LivingEntity bruxa, Player quem) {
            var solto = this.bicho().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (solto == null) return;
            solto.snapTo(bruxa.getX(), bruxa.getY(), bruxa.getZ(), bruxa.getYRot(), 0.0f);

            var vida = solto.getAttribute(Attributes.MAX_HEALTH);
            if (vida != null) vida.setBaseValue(VIDA);
            var soco = solto.getAttribute(Attributes.ATTACK_DAMAGE);
            if (soco != null) soco.setBaseValue(SOCO);
            solto.setHealth(solto.getMaxHealth());

            solto.setCustomName(Component.translatable("witch.thaumcraft.pet", bruxa.getName()));
            if (solto instanceof net.minecraft.world.entity.monster.zombie.Zombie) {
                solto.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
            }
            solto.setTarget(quem);
            solto.setLastHurtByMob(quem);

            ExtraDrops.pendura(solto, this.oOlho(bruxa));
            level.addFreshEntity(solto);
            level.sendParticles(ParticleTypes.WITCH, solto.getX(), solto.getY() + 1.0, solto.getZ(),
                    16, 0.5, 1.0, 0.5, 0.0);
        }

        /** A coisa que vem pendurada no bicho dela: o nome dela em cima, e a marca dela por dentro. */
        public ItemStack oOlho(LivingEntity bruxa) {
            ItemStack coisa = new ItemStack(this.prova());
            coisa.set(DataComponents.CUSTOM_NAME,
                    Component.translatable("witch.thaumcraft." + this.chaveDaProva(), bruxa.getName()));
            coisa.set(OccultaComponents.QUEST_OWNER, bruxa.getUUID());
            return coisa;
        }

        @Override
        public boolean serve(ItemStack naMao, LivingEntity bruxa) {
            return bruxa.getUUID().equals(naMao.get(OccultaComponents.QUEST_OWNER));
        }
    }

    /** E a de buscar: ela pede um tanto de alguma coisa, e se conta na mão de quem volta. */
    record Busca(String chave, net.minecraft.world.item.Item coisa, int quantos) implements CovenQuest {
        @Override
        public boolean serve(ItemStack naMao, LivingEntity bruxa) {
            return naMao.is(this.coisa());
        }
    }
}
