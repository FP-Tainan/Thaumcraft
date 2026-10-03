package net.thaumcraft.occulta.wolf;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaEffects;

/**
 * O <b>aldeão que vira</b>: o {@code EntityVillagerWere} do Witchery.
 *
 * <p>Por fora é um aldeão como outro qualquer — troca, trabalha, dorme, e ninguém o distingue. Por dentro
 * tem a doença, e na <b>primeira lua cheia</b> ele vira {@link WolfmanEntity}, com a profissão e as trocas
 * guardadas para quando voltar.
 *
 * <p><b>Criança não vira.</b> É do original, e é a única misericórdia que ele tem.
 *
 * <p>E o <b>acônito</b> no corpo segura a transformação: é o que faz daquela planta do mato uma coisa que se
 * planta de propósito ao pé de uma aldeia.
 */
public class WereVillagerEntity extends Villager {
    /** De quanto em quanto ele olha o céu: as cem batidas do original. */
    public static final int OLHA_A_LUA = 100;

    public WereVillagerEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % OLHA_A_LUA != 3) return;
        if (this.isBaby()) return;
        if (!Moon.cheia(level)) return;
        if (this.hasEffect(OccultaEffects.WOLFSBANE)) return;

        WolfmanEntity.doAldeão(level, this);
    }
}
