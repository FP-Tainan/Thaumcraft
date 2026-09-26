package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A Lâmina de Fenda: o {@code ItemRiftBlade} das Portas Dimensionais.
 *
 * <p>Corta como uma espada de ferro e, com o botão de usar, salta. Se houver uma fenda na linha de visão, ela
 * atravessa-a; se houver um bicho, ela leva quem a empunha para junto dele — e quanto mais gasta a lâmina
 * estiver, mais longe e mais torto o salto sai, que é a única coisa que o gasto dela muda.
 *
 * <p>Conserta-se com Tecido Estável e brilha sempre, como no original.
 */
public class RiftBladeItem extends Item {
    /** Até onde a lâmina procura, em blocos. */
    public static final double RANGE = 16.0;
    /** E quantos tiques de espera depois de cada salto. */
    public static final int COOLDOWN = 20;

    public RiftBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack stack = quem.getItemInHand(mão);

        LivingEntity bicho = aimedAt(level, quem);
        BlockPos fenda = bicho == null ? riftAimedAt(level, quem) : null;

        if (level.isClientSide()) {
            if (bicho == null && fenda == null) {
                quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_blade.miss"));
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }

        if (fenda != null && level.getBlockEntity(fenda) instanceof RiftBlockEntity fendaBE) {
            level.playSound(null, quem.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.0f);
            fendaBE.teleport(quem);
            saltou(quem, stack);
            return InteractionResult.SUCCESS;
        }

        if (bicho != null) {
            blink(level, quem, bicho, stack);
            saltou(quem, stack);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    /** A espera e o desgaste que todo salto cobra. */
    private void saltou(Player quem, ItemStack stack) {
        quem.getCooldowns().addCooldown(stack, COOLDOWN);
        stack.hurtAndBreak(1, quem, EquipmentSlot.MAINHAND);
    }

    /**
     * O salto para junto do bicho: o original tira a direção de quem empunha para o bicho, torce-a por um ângulo
     * que cresce com o gasto da lâmina e põe quem saltou a essa distância, subindo até achar ar.
     */
    private static void blink(Level level, Player quem, LivingEntity bicho, ItemStack stack) {
        double gasto = (double) stack.getDamageValue() / Math.max(1, stack.getMaxDamage());
        double distância = Math.random() * gasto * 7.0 + 2.0;
        double torção = (Math.random() - 0.5) * gasto * 360.0;

        Vec3 dele = bicho.position();
        Vec3 direção = quem.position().subtract(dele).normalize().yRot((float) Math.toRadians(torção));
        BlockPos onde = BlockPos.containing(dele.add(direção.scale(distância)));
        while (onde.getY() < level.getMaxY() && !level.getBlockState(onde).isAir()) onde = onde.above();

        float olhar = (quem.getYRot() - (float) torção) % 360.0f;
        if (level instanceof ServerLevel servidor && quem instanceof net.minecraft.server.level.ServerPlayer jogador) {
            jogador.teleportTo(servidor, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                    java.util.Set.of(), olhar, quem.getXRot(), false);
        } else {
            quem.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, olhar, quem.getXRot());
        }
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    /** O bicho vivo mais perto na linha de visão, ou nada. */
    public static @Nullable LivingEntity aimedAt(Level level, Player quem) {
        Vec3 olho = quem.getEyePosition();
        Vec3 até = olho.add(quem.getLookAngle().scale(RANGE));
        AABB caixa = quem.getBoundingBox().expandTowards(quem.getLookAngle().scale(RANGE)).inflate(1.0);

        LivingEntity achado = null;
        double perto = Double.MAX_VALUE;
        for (LivingEntity candidato : level.getEntitiesOfClass(LivingEntity.class, caixa, e -> e != quem && e.isAlive())) {
            var bateu = candidato.getBoundingBox().inflate(0.3).clip(olho, até);
            if (bateu.isEmpty()) continue;
            double quanto = olho.distanceToSqr(bateu.get());
            if (quanto < perto) {
                perto = quanto;
                achado = candidato;
            }
        }
        return achado;
    }

    /** E a fenda na linha de visão, ou nada. */
    public static @Nullable BlockPos riftAimedAt(Level level, Player quem) {
        Vec3 olho = quem.getEyePosition();
        Vec3 até = olho.add(quem.getLookAngle().scale(RANGE));
        BlockHitResult bateu = level.clip(new ClipContext(olho, até,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, quem));

        // a fenda não tem corpo, então o raio passa por ela: percorre-se a linha à procura de uma
        Vec3 fim = bateu.getType() == HitResult.Type.MISS ? até : bateu.getLocation();
        double quanto = olho.distanceTo(fim);
        for (double passo = 0.0; passo <= quanto; passo += 0.25) {
            BlockPos onde = BlockPos.containing(olho.add(quem.getLookAngle().scale(passo)));
            if (level.getBlockState(onde).is(ShatteredBlocks.RIFT)) return onde;
        }
        return null;
    }
}
