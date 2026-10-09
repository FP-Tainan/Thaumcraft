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
 * <p>Ninguém se vincula a si mesmo por engano: tocar-se com o frasco o enche do próprio dono, que é o que quem
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
        stack.set(OccultaComponents.TAGLOCK, new Taglock(quem.getUUID(),
                quem.getName().getString(), !(quem instanceof Player)));
    }

    /** Se aquele vínculo é daquela pessoa. */
    public static boolean isFor(ItemStack stack, LivingEntity quem) {
        Taglock preso = bound(stack);
        return preso != null && preso.owner().equals(quem.getUUID());
    }

    /**
     * <b>Encostar o frasco numa Rosa de Sangue tira dela quem ela apanhou</b>: o ramo do
     * {@code ItemTaglockKit} que clica numa rosa.
     *
     * <p>É a única maneira do ofício inteiro de prender alguém <b>sem lhe chegar perto</b>. Tudo o resto —
     * a boneca, a maldição, o espelho — precisa de um fio de quem se quer, e um fio pede um encontro. A rosa
     * não pede: planta-se no caminho e espera-se.
     */
    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext onde) {
        if (isBound(onde.getItemInHand())) return InteractionResult.PASS;
        if (!(onde.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) {
            return InteractionResult.PASS;
        }
        Player quem = onde.getPlayer();
        if (quem == null) return InteractionResult.PASS;
        /*
         * O <b>Baú de Sanguessugas</b>: ele anota o nome de quem o abre, e o frasco sai com um desses nomes.
         * É a armadilha mais paciente do mod — ninguém se fere, ninguém se prende; alguém só <b>teve
         * curiosidade</b>, e isso bastou.
         */
        if (level.getBlockState(onde.getClickedPos()).is(OccultaBlocks.LEECH_CHEST)) {
            return doBaú(level, onde, quem);
        }

        if (!level.getBlockState(onde.getClickedPos()).is(OccultaBlocks.BLOOD_ROSE)) {
            return InteractionResult.PASS;
        }

        Taglock tinha = BloodRoseBlock.tira(level, onde.getClickedPos());
        if (tinha == null) return InteractionResult.SUCCESS;

        ItemStack cheio = onde.getItemInHand().copyWithCount(1);
        cheio.set(OccultaComponents.TAGLOCK, tinha);
        onde.getItemInHand().shrink(1);
        if (!quem.getInventory().add(cheio)) quem.drop(cheio, false);
        level.playSound(null, onde.getClickedPos(), SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS, 1.0f, 1.2f);
        return InteractionResult.SUCCESS;
    }

    /**
     * O frasco enchido num <b>Baú de Sanguessugas</b>.
     *
     * <p>Ele devolve um nome que não seja o de quem está perguntando, e só de quem está no mundo agora: um
     * nome de alguém que saiu não serve para prender ninguém, e fica guardado para quando ele voltar.
     *
     * <p>Não havendo nome nenhum, o baú range e nada acontece — que é o original dizendo "ainda não caiu
     * ninguém".
     */
    private static InteractionResult doBaú(net.minecraft.server.level.ServerLevel level,
                                           net.minecraft.world.item.context.UseOnContext onde, Player quem) {
        if (!(level.getBlockEntity(onde.getClickedPos())
                instanceof net.thaumcraft.occulta.LeechChestBlockEntity baú)) {
            return InteractionResult.PASS;
        }
        String nome = baú.tiraUmNome(quem);
        if (nome == null) {
            level.playSound(null, onde.getClickedPos(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        var achado = level.getServer().getPlayerList().getPlayerByName(nome);
        if (achado == null) return InteractionResult.SUCCESS;

        ItemStack cheio = onde.getItemInHand().copyWithCount(1);
        cheio.set(OccultaComponents.TAGLOCK, new Taglock(achado.getUUID(), nome));
        onde.getItemInHand().shrink(1);
        if (!quem.getInventory().add(cheio)) quem.drop(cheio, false);
        level.playSound(null, onde.getClickedPos(), SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS, 1.0f, 1.2f);
        return InteractionResult.SUCCESS;
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
    public record Taglock(UUID owner, String name, boolean creature) {
        /**
         * O frasco cheio de alguém que <b>não é gente</b>: o {@code BoundType.CREATURE} do original.
         *
         * <p>Até à fatia dos fetiches isto não fazia diferença nenhuma — todo o ofício prende gente. O
         * <b>Espantalho</b> é a primeira coisa do mod que precisa de saber a diferença, porque ele
         * guarda as vacas de alguém por <b>espécie</b> e a gente por <b>nome</b>.
         *
         * <p>Os frascos antigos leem-se como gente, que é o que quase todos são.
         */
        public Taglock(UUID owner, String name) {
            this(owner, name, false);
        }

        public static final com.mojang.serialization.Codec<Taglock> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
                        net.minecraft.core.UUIDUtil.CODEC.fieldOf("owner").forGetter(Taglock::owner),
                        com.mojang.serialization.Codec.STRING.fieldOf("name").forGetter(Taglock::name),
                        com.mojang.serialization.Codec.BOOL.optionalFieldOf("creature", false)
                                .forGetter(Taglock::creature)
                ).apply(i, Taglock::new));

        public static final net.minecraft.network.codec.StreamCodec<
                net.minecraft.network.RegistryFriendlyByteBuf, Taglock> STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.composite(
                        net.minecraft.core.UUIDUtil.STREAM_CODEC, Taglock::owner,
                        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, Taglock::name,
                        net.minecraft.network.codec.ByteBufCodecs.BOOL, Taglock::creature,
                        Taglock::new);
    }
}
