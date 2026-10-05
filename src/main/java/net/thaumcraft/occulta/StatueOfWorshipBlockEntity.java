package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.goblin.GoblinEntity;
import net.thaumcraft.occulta.infusion.Infusions;
import org.jetbrains.annotations.Nullable;

/**
 * A alma da <b>Estátua de Adoração</b>: a {@code TileEntityStatueOfWorship} do Witchery.
 *
 * <p>Ela guarda uma coisa só — <b>quem é o dono</b> — e faz uma coisa só, de cinco em cinco segundos:
 * conta os goblins que a adoram e paga ao dono conforme o número.
 *
 * <h2>Os três degraus</h2>
 *
 * <p>Cinco adoradores enchem a infusão; dez dão <b>Adoração</b>; quinze dão <b>Adoração II</b>. Repare
 * que são <b>quinze goblins</b> num cubo de oito blocos — isso é uma aldeia inteira de goblins junta —, e
 * que é o que custa lançar um símbolo do terceiro grau. O ofício não é gentil, e nesta ponta dele ele
 * também não é discreto.
 */
public class StatueOfWorshipBlockEntity extends BlockEntity {
    /** De quanto em quanto tempo ela conta: os cinco segundos do original. */
    public static final int PULSO = 100;

    /** A que distância ela conta goblins. */
    public static final double CUBO = 8.0;

    /** Os três degraus de adoração. */
    public static final int ENCHE = 5;
    public static final int ADORA = 10;
    public static final int ADORA_MAIS = 15;

    /** Quanta carga de infusão ela dá por pulso, e a que distância o dono tem de estar. */
    public static final int CARGA = 30;
    public static final double ALCANCE = 64.0;

    /** Quanto dura a Adoração que ela dá: o minuto do original. */
    public static final int DURA = 1200;

    private @Nullable TaglockItem.Taglock dono;
    private int quantos;

    public StatueOfWorshipBlockEntity(BlockPos onde, BlockState feitio) {
        super(OccultaBlocks.STATUE_OF_WORSHIP_ENTITY, onde, feitio);
    }

    public @Nullable TaglockItem.Taglock dono() {
        return this.dono;
    }

    public void dono(TaglockItem.Taglock quem) {
        this.dono = quem;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    /** Quantos goblins a adoravam da última vez que ela contou. */
    public int quantos() {
        return this.quantos;
    }

    /**
     * <b>A batida dela.</b>
     *
     * <p>De cinco em cinco segundos: conta, e paga. Sem dono não conta nada — uma estátua de bancada não
     * é estátua de ninguém.
     */
    public static void bate(ServerLevel level, BlockPos onde, StatueOfWorshipBlockEntity estátua) {
        if (estátua.dono == null) return;
        int adoram = estátua.conta(level);
        ServerPlayer dono = level.getServer().getPlayerList().getPlayer(estátua.dono.owner());
        if (dono == null) return;
        paga(level, onde, dono, adoram);
    }

    /** O que ela paga ao dono, conforme o número de adoradores. */
    public static void paga(ServerLevel level, BlockPos onde, ServerPlayer dono, int adoram) {

        /*
         * <b>A carga só chega a quem está por perto.</b> Sessenta e quatro blocos é longe de se ver a
         * estátua, mas não é longe de a ter em casa — e é de propósito: o original quer que você volte.
         */
        if (adoram >= ENCHE && dono.distanceToSqr(onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5)
                <= ALCANCE * ALCANCE && Infusions.energia(dono) < Infusions.teto(dono)) {
            Infusions.enche(dono, CARGA);
            level.playSound(null, dono.getX(), dono.getY(), dono.getZ(),
                    net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 2.0f);
            level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                            net.minecraft.core.particles.ParticleTypes.INSTANT_EFFECT,
                            1.0f, 1.0f, 1.0f, 1.0f),
                    dono.getX(), dono.getY() + 1.0, dono.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        }

        /*
         * E a <b>Adoração</b>, que não tem nada que ver com a infusão: é o que os símbolos do segundo e do
         * terceiro grau pedem. Dez goblins dão o segundo grau; quinze, o terceiro.
         */
        if (adoram >= ADORA) {
            dono.addEffect(new MobEffectInstance(OccultaEffects.WORSHIP, DURA,
                    adoram >= ADORA_MAIS ? 1 : 0, true, true));
        }
    }

    /**
     * Conta os que adoram e <b>manda adorar</b> os que ainda não adoram.
     *
     * <p>Repare na ordem: ela conta <b>antes</b> de mandar. Quem acabou de ser mandado só entra na conta do
     * pulso seguinte, e é por isso que uma estátua recém-posta leva uns segundos a pagar.
     */
    public int conta(ServerLevel level) {
        AABB cubo = new AABB(this.worldPosition.getX() + 0.5 - CUBO,
                this.worldPosition.getY() + 0.5 - CUBO, this.worldPosition.getZ() + 0.5 - CUBO,
                this.worldPosition.getX() + 0.5 + CUBO, this.worldPosition.getY() + 0.5 + CUBO,
                this.worldPosition.getZ() + 0.5 + CUBO);
        int adoram = 0;
        for (GoblinEntity goblin : level.getEntitiesOfClass(GoblinEntity.class, cubo)) {
            if (goblin.adorando()) adoram++;
            else goblin.começaAAdorar(this.worldPosition);
        }
        this.quantos = adoram;
        return adoram;
    }

    // ------------------------------------------------------------------ o que fica guardado

    @Override
    protected void saveAdditional(ValueOutput saída) {
        super.saveAdditional(saída);
        if (this.dono != null) saída.store("Owner", TaglockItem.Taglock.CODEC, this.dono);
    }

    @Override
    protected void loadAdditional(ValueInput entrada) {
        super.loadAdditional(entrada);
        this.dono = entrada.read("Owner", TaglockItem.Taglock.CODEC).orElse(null);
    }

    /**
     * <b>O dono atravessa o item.</b>
     *
     * <p>É o que faz a estátua ser de alguém de verdade: posta, ela toma o dono que vinha no item;
     * partida, ela o leva consigo na tabela de despojos. Repare no que isso quer dizer — quem põe a
     * estátua <b>não é</b> necessariamente o dono dela. Uma estátua presa a alguém, roubada e posta
     * por outro, continua enchendo a infusão do primeiro.
     */
    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder peças) {
        super.collectImplicitComponents(peças);
        if (this.dono != null) peças.set(OccultaComponents.TAGLOCK, this.dono);
    }

    @Override
    protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter peças) {
        super.applyImplicitComponents(peças);
        var preso = peças.get(OccultaComponents.TAGLOCK);
        if (preso != null) this.dono = preso;
    }

    /** O dono atravessa a rede, porque o desenhista precisa da cara dele. */
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registros) {
        return this.saveCustomOnly(registros);
    }
}
