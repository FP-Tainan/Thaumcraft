package net.thaumcraft.occulta.brew;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

/**
 * Uma <b>gota de cozimento</b> a cair do céu: o {@code EntityDroplet} do Witchery.
 *
 * <p>Ela é a parte visível do rito de <b>poça</b>, e é a imagem mais bonita do ofício inteiro: um
 * caldeirão numa cave, e cem casas acima dele começa a <b>chover o cozimento</b> sobre um campo. Dezenas
 * de gotas, cada uma com a cor do caldo, a cair de onde ninguém as pôs.
 *
 * <p>Cada gota é um frasco pequeno: onde bate, aplica o cozimento àquele lugar ou àquele bicho, e some.
 * Não estoura, não faz barulho e não deixa poça — é só uma gota.
 *
 * <p><b>Pequena mudança declarada:</b> o original escreve-lhe a física à mão — gravidade de três
 * centésimos, arrasto de noventa e nove, e um rodopio enquanto cai. Aqui ela é um
 * {@link ThrowableItemProjectile}, que é a mesma física escrita pelo jogo, e a gravidade do jogo para um
 * objeto atirado é <b>a mesma</b>: três centésimos.
 */
public class BrewDropletEntity extends ThrowableItemProjectile {
    public BrewDropletEntity(EntityType<? extends BrewDropletEntity> tipo, Level level) {
        super(tipo, level);
    }

    /** Uma gota do cozimento que lhe derem, largada de onde lhe disserem. */
    public BrewDropletEntity(ServerLevel level, double x, double y, double z, List<Item> dentro) {
        super(net.thaumcraft.occulta.OccultaEntities.BREW_DROPLET, x, y, z, level,
                BrewItem.of(net.thaumcraft.occulta.OccultaItems.BREW, dentro));
    }

    @Override
    protected Item getDefaultItem() {
        return net.thaumcraft.occulta.OccultaItems.BREW;
    }

    /** O que ela traz dentro. */
    public List<Item> dentro() {
        return BrewItem.contents(this.getItem());
    }

    /** E a cor dela, que é a do caldo: o desenhista tira-a daqui. */
    public int cor() {
        var tinta = this.getItem().get(DataComponents.DYED_COLOR);
        return tinta == null ? 0xFFFFFF : tinta.rgb() & 0xFFFFFF;
    }

    @Override
    protected void onHitEntity(EntityHitResult acertou) {
        super.onHitEntity(acertou);
        if (!(this.level() instanceof ServerLevel level)) return;
        if (acertou.getEntity() instanceof LivingEntity quem) {
            Brew.apply(level, quem, this.dentro(), new BrewModifiers());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult acertou) {
        super.onHitBlock(acertou);
        if (!(this.level() instanceof ServerLevel level)) return;
        Brew.applyToBlock(level, this.dentro(), acertou.getBlockPos(), acertou.getDirection(), 1,
                new BrewModifiers());
    }

    /** Batendo em alguma coisa, ela some — e deixa um salpico da cor dela. */
    @Override
    protected void onHit(net.minecraft.world.phys.HitResult onde) {
        super.onHit(onde);
        if (!(this.level() instanceof ServerLevel level)) return;
        level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(
                        net.minecraft.core.particles.ParticleTypes.ENTITY_EFFECT,
                        0xFF000000 | this.cor()),
                this.getX(), this.getY(), this.getZ(), 6, 0.1, 0.1, 0.1, 0.0);
        this.discard();
    }

    /** Ela não bate em quem a fez cair, porque ninguém a atirou. */
    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity quem) {
        return quem instanceof LivingEntity && super.canHitEntity(quem);
    }

    /** E não traz pilha nenhuma consigo quando alguém a apanha: ela não é um item. */
    @Override
    public ItemStack getPickResult() {
        return ItemStack.EMPTY;
    }
}
