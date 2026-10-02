package net.thaumcraft.occulta.spirit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A Agulha de Gelo: o {@code useIcyNeedle} do Witchery.
 *
 * <p>Ela serve para uma coisa só, e é a mais velha de todas: <b>beliscar-se para acordar</b>. Andando em
 * espírito, espetá-la traz de volta ao corpo e gasta a agulha. Acordado, espetá-la só dói — meio coração, e a
 * agulha se gasta na mesma.
 *
 * <p>É também uma das <b>duas únicas coisas</b> que passam da mochila de cá para a de lá, pela razão óbvia: sem
 * ela, quem adormece fica lá até alguém lhe matar o corpo.
 */
public class IcyNeedleItem extends Item {
    /** O que ela tira de quem se belisca acordado. */
    public static final float PRICK = 1.0f;

    public IcyNeedleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(quem instanceof ServerPlayer gente)) return InteractionResult.PASS;

        ItemStack naMão = quem.getItemInHand(mão);
        boolean acordou = SpiritWorld.wakeUp(gente);
        if (!acordou) {
            gente.hurtServer((net.minecraft.server.level.ServerLevel) level,
                    level.damageSources().generic(), PRICK);
        }
        if (!quem.hasInfiniteMaterials()) naMão.shrink(1);
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(),
                acordou ? SoundEvents.PLAYER_LEVELUP : SoundEvents.PLAYER_HURT,
                SoundSource.PLAYERS, 0.6f, acordou ? 1.6f : 1.0f);
        return InteractionResult.SUCCESS;
    }
}
