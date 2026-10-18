package net.thaumcraft.occulta.death;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * O <b>Cavalo da Morte</b>: o {@code EntityDeathsHorse} do Witchery, que é o <b>Binky</b>.
 *
 * <p>São <b>onze linhas</b> no original, e dez delas são o construtor: ele é um cavalo comum que <b>não dá
 * experiência</b>. Mais nada. Não é mais rápido, não é mais forte, não voa e não arde.
 *
 * <p>O que ele é, é a <b>piada</b>: a Morte larga uma sela e a cabeça do Binky, e quem as juntar numa
 * montaria comum tem o cavalo branco. O valor dele está inteiro no nome e no que ele larga, e o original
 * sabia disso — por isso não lhe escreveu mais nada.
 */
public class DeathsHorseEntity extends Horse {
    public DeathsHorseEntity(EntityType<? extends DeathsHorseEntity> tipo, Level level) {
        super(tipo, level);
        this.xpReward = 0;
    }
}
