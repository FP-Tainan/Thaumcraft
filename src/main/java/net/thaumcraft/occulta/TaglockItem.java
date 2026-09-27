package net.thaumcraft.occulta;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * O Frasco de Vínculo: o {@code ItemTaglockKit} do Witchery.
 *
 * <p>Um frasco vazio que se enche com um <b>fio de alguém</b> — um cabelo, uma gota de sangue, o que der. Basta
 * tocar quem se quer com ele na mão. Cheio, ele guarda o nome e a marca daquela pessoa, e é isso que prende uma
 * boneca a ela.
 *
 * <p>Ninguém se vincula a si mesmo por engano: tocar-se com o frasco enche-o do próprio dono, que é o que quem
 * quer uma boneca de proteção precisa.
 */
public class TaglockItem extends Item {
    public TaglockItem(Properties properties) {
        super(properties);
    }

    /** A quem este frasco está preso, ou nada. */
    @Nullable
    public static Taglock bound(ItemStack stack) {
        return stack.get(OccultaComponents.TAGLOCK);
    }

    public static boolean isBound(ItemStack stack) {
        return stack.has(OccultaComponents.TAGLOCK);
    }

    /** Prende aquela pessoa a este frasco (ou a esta boneca). */
    public static void bind(ItemStack stack, LivingEntity quem) {
        stack.set(OccultaComponents.TAGLOCK, new Taglock(quem.getUUID(), quem.getName().getString()));
    }

    /** Se aquele vínculo é daquela pessoa. */
    public static boolean isFor(ItemStack stack, LivingEntity quem) {
        Taglock preso = bound(stack);
        return preso != null && preso.owner().equals(quem.getUUID());
    }

    /** O {@code onLeftClickEntity}: tocar alguém enche o frasco com ele. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player quem, LivingEntity alvo,
                                                  InteractionHand mão) {
        if (isBound(stack)) return InteractionResult.PASS;
        if (quem.level().isClientSide()) return InteractionResult.SUCCESS;

        ItemStack cheio = stack.copyWithCount(1);
        bind(cheio, alvo);
        stack.shrink(1);
        if (!quem.getInventory().add(cheio)) quem.drop(cheio, false);
        quem.level().playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.BOTTLE_FILL,
                SoundSource.PLAYERS, 1.0f, 1.2f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        Taglock preso = bound(stack);
        if (preso == null) return super.getName(stack);
        return Component.translatable("item.thaumcraft.taglock.bound", preso.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        Taglock preso = bound(stack);
        if (preso == null) return;
        linha.accept(Component.translatable("tc.taglock.bound", preso.name())
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    /**
     * O que um frasco cheio guarda: quem é, e como se chama.
     *
     * @param owner a marca de quem foi preso
     * @param name  o nome dele, para se ler no frasco
     */
    public record Taglock(UUID owner, String name) {
        public static final com.mojang.serialization.Codec<Taglock> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
                        net.minecraft.core.UUIDUtil.CODEC.fieldOf("owner").forGetter(Taglock::owner),
                        com.mojang.serialization.Codec.STRING.fieldOf("name").forGetter(Taglock::name)
                ).apply(i, Taglock::new));

        public static final net.minecraft.network.codec.StreamCodec<
                net.minecraft.network.RegistryFriendlyByteBuf, Taglock> STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.composite(
                        net.minecraft.core.UUIDUtil.STREAM_CODEC, Taglock::owner,
                        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, Taglock::name,
                        Taglock::new);
    }
}
