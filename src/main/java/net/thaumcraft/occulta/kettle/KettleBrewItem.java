package net.thaumcraft.occulta.kettle;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Um frasco do Caldeirão de Pote: o {@code ItemGeneral.Brew} do Witchery.
 *
 * <p>Atira-se como uma poção de arremesso, e o que ele faz onde bate é o de cada {@link KettleBrews.Kind}.
 */
public class KettleBrewItem extends Item {
    private final KettleBrews.Kind kind;

    public KettleBrewItem(KettleBrews.Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public KettleBrews.Kind kind() {
        return this.kind;
    }

    /** O que um frasco daqueles carrega, ou nada se não for frasco de pote. */
    public static KettleBrews.Kind kindOf(ItemStack frasco) {
        return frasco.getItem() instanceof KettleBrewItem brew ? brew.kind() : null;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.SPLASH_POTION_THROW,
                SoundSource.PLAYERS, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            net.minecraft.world.entity.projectile.Projectile.spawnProjectileFromRotation(
                    KettleBrewProjectile::new, server, naMão, quem, 0.0f, 0.5f, 1.0f);
        }
        quem.awardStat(Stats.ITEM_USED.get(this));
        naMão.consume(1, quem);
        return InteractionResult.SUCCESS;
    }
}
