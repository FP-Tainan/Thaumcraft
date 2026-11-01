package net.thaumcraft.occulta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A <b>Granada de Duplicação</b>: o {@code ItemSunGrenade} do Witchery, no <b>modo um</b>.
 *
 * <p>Uma Esfera de Quartzo <b>carimbada com a cara de alguém</b>. Atirada, ela larga um
 * {@link DecoyEntity sósia} dessa pessoa onde bate — e o sósia rouba a atenção de tudo o que estivesse
 * atrás dela, por dez segundos. É a única coisa do ofício que serve para <b>fugir</b>.
 *
 * <p>Ela não se fabrica em bancada nenhuma: faz-se no <b>Espelho da Bruxa</b>, com uma Esfera de Quartzo na
 * mão e <b>dois mil</b> de poder de altar. O espelho é que sabe com que cara carimbá-la, porque é ele que
 * está olhando para você.
 *
 * <p>Na dica dela está escrito de quem ela é — e tinha de estar: duas granadas na mesma caixa, com caras
 * diferentes, seriam a mesma coisa a olho nu.
 */
public class DupGrenadeItem extends Item {
    /**
     * O carimbo: de quem é a cara.
     *
     * <p>O <b>número</b> é a identidade e o <b>nome</b> é o que se lê. Os dois são precisos: o número
     * porque um nome muda, e o nome porque a dica se lê do lado de cá, onde ninguém sabe a quem um número
     * pertence.
     */
    public record Carimbo(UUID id, String nome) {
        public static final Codec<Carimbo> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Carimbo::id),
                Codec.STRING.fieldOf("nome").forGetter(Carimbo::nome)
        ).apply(campo, Carimbo::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Carimbo> STREAM_CODEC =
                StreamCodec.composite(
                        UUIDUtil.STREAM_CODEC, Carimbo::id,
                        ByteBufCodecs.STRING_UTF8, Carimbo::nome,
                        Carimbo::new);
    }

    public DupGrenadeItem(Properties properties) {
        super(properties);
    }

    /** O feitio dela: dezesseis por casa e fora do comum, como o original. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(16).rarity(Rarity.UNCOMMON)
                .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    /** O carimbo dela, ou nada se ninguém a carimbou. */
    public static @Nullable Carimbo carimbo(ItemStack granada) {
        return granada.get(OccultaComponents.GRENADE_STAMP);
    }

    /** De quem é a cara desta granada. */
    public static @Nullable UUID deQuem(ItemStack granada) {
        Carimbo qual = carimbo(granada);
        return qual == null ? null : qual.id();
    }

    /** E o nome dessa pessoa, que é o que se lê na dica. */
    public static String nomeDeQuem(ItemStack granada) {
        Carimbo qual = carimbo(granada);
        return qual == null ? "" : qual.nome();
    }

    /** Uma granada carimbada com aquela pessoa: é o que o Espelho da Bruxa faz. */
    public static ItemStack de(Player quem) {
        ItemStack granada = new ItemStack(OccultaItems.DUP_GRENADE);
        granada.set(OccultaComponents.GRENADE_STAMP,
                new Carimbo(quem.getUUID(), quem.getName().getString()));
        return granada;
    }

    @Override
    public void appendHoverText(ItemStack granada, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        String nome = nomeDeQuem(granada);
        if (nome.isEmpty()) return;
        linha.accept(Component.translatable("tc.dupgrenade.tip", nome)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack oquê) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        ItemStack naMão = quem.getItemInHand(mão);
        mundo.playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 0.5f, 0.4f / (mundo.getRandom().nextFloat() * 0.4f + 0.8f));
        if (mundo instanceof ServerLevel level) {
            DupGrenadeEntity atirada = new DupGrenadeEntity(level, quem, naMão.copyWithCount(1));
            atirada.shootFromRotation(quem, quem.getXRot(), quem.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(atirada);
        }
        quem.awardStat(Stats.ITEM_USED.get(this));
        naMão.consume(1, quem);
        return InteractionResult.SUCCESS;
    }
}
