package net.thaumcraft.arcana.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.arcana.ArcanaEffects;
import net.thaumcraft.arcana.power.PowerNodes;
import net.thaumcraft.arcana.power.PoweredBlockEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * O que as <b>três fontes</b> do Ars Magica 2 têm em comum: o {@code TileEntityObelisk} visto como
 * pai, que é o que o Aurem Negro e o Prisma Celeste herdam na 1.4.0.009.
 *
 * <p>São três coisas. <b>Nunca pedem</b> energia a ninguém. <b>Olham para o que têm em volta</b> de cem
 * em cem batidas e refazem o multiplicador. E, tendo um décimo do teto, <b>abençoam</b> quem estiver a
 * duas casas com a Regeneração de Mana.
 *
 * <p>A figura é sempre a mesma de forma: um <b>anel de giz</b> à volta (vezes 1,25) e <b>quatro
 * pilares</b> nos cantos (vezes o que o topo deles valer). O que muda de fonte para fonte é de que são os
 * pilares e o que os topos valem — e é só isso.
 */
public abstract class EssenceGeneratorBlockEntity extends PoweredBlockEntity {
    /** De quantas em quantas batidas ela olha para o que tem em volta: <b>cem</b>. */
    public static final int SURROUNDING_CHECK = 100;
    /** Quanto do teto ela precisa de ter para abençoar: <b>um décimo</b>. */
    public static final float BLESSING_THRESHOLD = 0.1f;
    /** E por quanto tempo: <b>600 batidas</b>. */
    public static final int BLESSING_TICKS = 600;
    /** A que distância. */
    public static final int BLESSING_RANGE = 2;
    /** O que o anel de giz multiplica, igual nas três. */
    public static final float CHALK_BONUS = 1.25f;

    /** A máscara com que o original pergunta por todos os grupos de uma vez. */
    protected static final int ALL_MUTEXES = 7;
    /** O número de exclusão dos pilares e o dos topos. */
    protected static final int MUTEX_PILLARS = 2;
    protected static final int MUTEX_CAPS = 4;

    protected int surroundingCheckTicks;
    protected float powerMultiplier = 1.0f;

    protected EssenceGeneratorBlockEntity(BlockEntityType<?> tipo, BlockPos onde, BlockState feitio, int capacity) {
        super(tipo, onde, feitio, capacity);
    }

    /** A figura desta fonte. */
    protected abstract Multiblock figura();

    /** O grupo dos pilares dela. */
    protected abstract Multiblock.Group pilares();

    /** O grupo do anel de giz. */
    protected abstract Multiblock.Group giz();

    /** E os topos que ela aceita, cada um com o que vale. */
    protected abstract Map<Multiblock.Group, Float> topos();

    /** Qual dos três Etheriums ela faz. */
    protected abstract net.thaumcraft.arcana.power.PowerType type();

    /** Com que grau ela abençoa: o Obelisco com 2, o Aurem com 3, o Prisma com 1. */
    protected abstract int blessingAmplifier();

    public float powerMultiplier() {
        return this.powerMultiplier;
    }

    /**
     * O {@code checkNearbyBlockState}: olha para o giz e para os pilares e refaz o multiplicador.
     *
     * <p>O anel de giz põe o multiplicador em 1,25; os pilares multiplicam-no pelo que o topo deles valer.
     * <b>Pilares sem topo não valem nada</b>: o valor de um cap que não esteja montado nunca entra.
     */
    public void checkNearbyBlockState() {
        var montados = this.figura().matchedGroups(ALL_MUTEXES, this.level, this.getBlockPos());
        Map<Multiblock.Group, Float> topos = this.topos();
        float topo = 1.0f;
        boolean temPilares = false;
        boolean temGiz = false;
        for (Multiblock.Group grupo : montados) {
            if (grupo == this.pilares()) temPilares = true;
            else if (grupo == this.giz()) temGiz = true;
            Float vale = topos.get(grupo);
            if (vale != null) topo = vale;
        }
        this.powerMultiplier = temGiz ? CHALK_BONUS : 1.0f;
        if (temPilares) this.powerMultiplier *= topo;
    }

    /** A Regeneração de Mana a quem estiver a duas casas, e só a quem ainda não a tiver. */
    protected void bless(ServerLevel level) {
        BlockPos onde = this.getBlockPos();
        AABB roda = new AABB(onde.getX() - BLESSING_RANGE, onde.getY(), onde.getZ() - BLESSING_RANGE,
                onde.getX() + BLESSING_RANGE + 1, onde.getY() + 3, onde.getZ() + BLESSING_RANGE + 1);
        for (Player quem : level.getEntitiesOfClass(Player.class, roda)) {
            if (quem.hasEffect(ArcanaEffects.MANA_REGEN)) continue;
            quem.addEffect(new MobEffectInstance(ArcanaEffects.MANA_REGEN, BLESSING_TICKS, this.blessingAmplifier()));
        }
    }

    /** O relógio de cem batidas: refaz o multiplicador e abençoa quem estiver perto. */
    protected void surroundings(ServerLevel level) {
        this.checkNearbyBlockState();
        this.surroundingCheckTicks = 1;
        if (PowerNodes.of(level).checkPower(this.getBlockPos(), this.capacity * BLESSING_THRESHOLD)) {
            this.bless(level);
        }
    }

    // ---------------------------------------------------------------- o que toda fonte é na rede

    @Override
    public int chargeRate() {
        return 0;
    }

    @Override
    public boolean canProvidePower(net.thaumcraft.arcana.power.PowerType type) {
        return type == this.type();
    }

    @Override
    public net.thaumcraft.arcana.power.PowerType[] validPowerTypes() {
        return new net.thaumcraft.arcana.power.PowerType[]{this.type()};
    }

    @Override
    public boolean canRequestPower() {
        return false;
    }

    @Override
    public boolean canRelayPower(net.thaumcraft.arcana.power.PowerType type) {
        return false;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    /** Uma lista de topos, na ordem em que foi escrita. */
    protected static Map<Multiblock.Group, Float> caps(Object... pares) {
        Map<Multiblock.Group, Float> feito = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            feito.put((Multiblock.Group) pares[i], (Float) pares[i + 1]);
        }
        return feito;
    }
}
