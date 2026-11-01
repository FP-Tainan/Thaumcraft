package net.thaumcraft.occulta;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A <b>Granada de Duplicação</b> em voo: o {@code EntityGrenade} do Witchery, no <b>modo um</b>.
 *
 * <p>Ela é a irmã simples da Granada Solar: não alumia, não queima nada e não fica parada no ar. Voa, bate,
 * e onde bateu <b>larga um {@link DecoyEntity sósia}</b> de quem a atirou — mais a <b>Esfera de Quartzo</b>
 * que a fez, que volta sempre, nos dois modos.
 *
 * <p>De quem ela é cópia <b>viaja com ela</b>, e não se pergunta a quem a atirou: ela pode bater depois de
 * quem a atirou ter morrido, e o sósia tem de sair com a cara certa na mesma.
 */
public class DupGrenadeEntity extends ThrowableItemProjectile {
    @Nullable
    private UUID dequem;

    public DupGrenadeEntity(EntityType<? extends DupGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public DupGrenadeEntity(Level level, LivingEntity quem, ItemStack oquê) {
        super(OccultaEntities.DUP_GRENADE, quem, level, oquê);
        this.dequem = DupGrenadeItem.deQuem(oquê);
    }

    @Override
    protected Item getDefaultItem() {
        return OccultaItems.DUP_GRENADE;
    }

    /** De quem ela é a cópia, se alguém a carimbou. */
    public @Nullable UUID deQuem() {
        return this.dequem;
    }

    public void põeDeQuem(@Nullable UUID quem) {
        this.dequem = quem;
    }

    /**
     * <b>Bateu, acabou.</b>
     *
     * <p>A esfera volta — é dela que a granada é feita, e o original devolve-a nos dois modos — e no lugar
     * nasce o sósia, que logo rouba a atenção de quem estivesse atrás do dono.
     */
    @Override
    protected void onHit(HitResult onde) {
        super.onHit(onde);
        if (!(this.level() instanceof ServerLevel mundo)) return;

        Block.popResource(mundo, this.blockPosition(), new ItemStack(OccultaItems.QUARTZ_SPHERE));
        Player dono = this.dequem == null ? null : mundo.getPlayerByUUID(this.dequem);
        if (dono != null) {
            DecoyEntity sósia = OccultaEntities.DECOY.create(mundo, EntitySpawnReason.TRIGGERED);
            if (sósia != null) {
                sósia.snapTo(this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f);
                sósia.veste(dono);
                mundo.addFreshEntity(sósia);
                sósia.rouba(mundo);
            }
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput saída) {
        super.addAdditionalSaveData(saída);
        if (this.dequem != null) saída.store("DeQuem", UUIDUtil.CODEC, this.dequem);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput entrada) {
        super.readAdditionalSaveData(entrada);
        this.dequem = entrada.read("DeQuem", UUIDUtil.CODEC).orElse(null);
    }
}
