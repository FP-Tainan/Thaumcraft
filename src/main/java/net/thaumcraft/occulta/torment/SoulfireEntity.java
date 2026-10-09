package net.thaumcraft.occulta.torment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A <b>bola de fogo de alma</b>: a {@code EntitySoulfire} do Witchery.
 *
 * <p>É a bola de fogo pequena do jogo com duas diferenças: é um pouco <b>maior</b> — cinco dezesseis avos
 * em vez de três — e, além de pegar fogo em quem acerta, ainda lhe bate <b>seis de dano demoníaco</b>, que passa
 * pela armadura.
 *
 * <p>O Senhor do Tormento atira <b>três</b> de cada vez, e uma vez em dez atira <b>nove</b>.
 */
public class SoulfireEntity extends SmallFireball {
    /** O lado dela: os {@code 0.3125} do original. */
    public static final float LADO = 0.3125f;

    /** E o que ela bate, além do fogo. */
    public static final float QUANTO = 6.0f;

    public SoulfireEntity(EntityType<? extends SoulfireEntity> tipo, Level mundo) {
        super(tipo, mundo);
    }

    public SoulfireEntity(Level mundo, LivingEntity quem, Vec3 rumo) {
        super(mundo, quem, rumo);
    }

    @Override
    protected void onHitEntity(EntityHitResult acertou) {
        super.onHitEntity(acertou);
        if (!(this.level() instanceof ServerLevel level)) return;
        if (!(acertou.getEntity() instanceof LivingEntity vivo)) return;
        Demonic.bate(level, this.getOwner(), vivo, QUANTO);
    }
}
