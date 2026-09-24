package net.thaumcraft.naturalis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.thaumcraft.block.entity.OwnedBlockEntity;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * As chaves de táumio do Magia Naturalis 0.5.0: o {@code ArcaneKeyItem}.
 *
 * <p>São duas. A Chave do Desvendar (o zero do original) fica ligada a duas almas — a de quem a forjou e a de quem
 * ela aceita — e, usada num bloco arcano do forjador, dá a entrada a quem ela carrega. A Chave do Endosso (o um)
 * junta uma lista de gente ao bater em cada uma e, usada num bloco a que quem a leva tem entrada, põe todos eles
 * na lista daquele bloco de uma só vez.
 */
public class ArcaneKeyItem extends Item {
    /** Alguém anotado numa chave: quem é, como se chama e com que nível entra. */
    public record Soul(UUID id, String name, byte level) {
        public static final Codec<Soul> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Soul::id),
                Codec.STRING.fieldOf("name").forGetter(Soul::name),
                Codec.BYTE.optionalFieldOf("level", (byte) 0).forGetter(Soul::level)
        ).apply(instance, Soul::new));
    }

    /** O que a chave guarda: o forjador, a alma a que ela obedece e a lista que ela carrega. */
    public record Bond(Optional<Soul> forger, Optional<Soul> owner, List<Soul> endorsed) {
        public static final Bond EMPTY = new Bond(Optional.empty(), Optional.empty(), List.of());
        public static final Codec<Bond> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Soul.CODEC.optionalFieldOf("forger").forGetter(Bond::forger),
                Soul.CODEC.optionalFieldOf("owner").forGetter(Bond::owner),
                Soul.CODEC.listOf().optionalFieldOf("endorsed", List.of()).forGetter(Bond::endorsed)
        ).apply(instance, Bond::new));

        public Bond withForger(Soul soul) {
            return new Bond(Optional.of(soul), this.owner, this.endorsed);
        }

        public Bond withOwner(Soul soul) {
            return new Bond(this.forger, Optional.of(soul), this.endorsed);
        }

        public Bond withEndorsed(Soul soul) {
            List<Soul> lista = new ArrayList<>(this.endorsed);
            lista.removeIf(other -> other.id().equals(soul.id()));
            lista.add(soul);
            return new Bond(this.forger, this.owner, List.copyOf(lista));
        }
    }

    /** Zero é a do Desvendar, um é a do Endosso. */
    private final int type;

    public ArcaneKeyItem(int type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public int type() {
        return this.type;
    }

    public static Bond bond(ItemStack stack) {
        Bond guardado = stack.get(TCComponents.ARCANE_KEY);
        return guardado == null ? Bond.EMPTY : guardado;
    }

    private static Soul soul(Player player, byte level) {
        return new Soul(player.getUUID(), player.getName().getString(), level);
    }

    /** Uma chave forjada ou ligada brilha, como no original. */
    @Override
    public boolean isFoil(ItemStack stack) {
        Bond bond = bond(stack);
        return bond.forger().isPresent() || bond.owner().isPresent();
    }

    /** Quem a tira da mesa vira o forjador dela. */
    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        Bond bond = bond(stack);
        if (bond.forger().isEmpty()) stack.set(TCComponents.ARCANE_KEY, bond.withForger(soul(player, (byte) 0)));
    }

    /** Agachado: a chave sem forjador se forja, e a forjada sem alma se liga a quem a segura. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !player.isShiftKeyDown()) return InteractionResult.PASS;
        Bond bond = bond(stack);
        if (bond.forger().isEmpty()) {
            stack.set(TCComponents.ARCANE_KEY, bond.withForger(soul(player, (byte) 0)));
            player.sendSystemMessage(Component.translatable("chat.thaumcraft.key.owner", player.getName().getString())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else if (bond.owner().isEmpty()) {
            stack.set(TCComponents.ARCANE_KEY, bond.withOwner(soul(player, (byte) 0)));
            player.sendSystemMessage(Component.translatable("chat.thaumcraft.key.boundplayer.1", player.getName().getString())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * O {@code onItemUseFirst}: a do Desvendar dá a entrada a quem ela carrega, num bloco do forjador dela; a do
     * Endosso põe a lista inteira no bloco, se quem a leva já entra lá.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        Bond bond = bond(stack);
        var chest = level.getBlockEntity(context.getClickedPos());
        if (this.type == 0) {
            if (bond.forger().isEmpty() || bond.owner().isEmpty()) return InteractionResult.PASS;
            Soul forger = bond.forger().get();
            Soul owner = bond.owner().get();
            boolean doForjador = chest instanceof ArcaneChestBlockEntity arcane
                    ? forger.id().equals(arcane.owner())
                    : chest instanceof OwnedBlockEntity owned && forger.name().equals(owned.owner);
            if (!doForjador) return InteractionResult.PASS;
            if (!grant(chest, owner)) return InteractionResult.PASS;
            player.sendSystemMessage(Component.translatable(chest instanceof ArcaneChestBlockEntity
                            ? "chat.thaumcraft.key.access.chest" : "chat.thaumcraft.key.access.misc")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            level.playSound(null, context.getClickedPos(), TCSounds.KEY.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f);
            return InteractionResult.SUCCESS;
        }
        if (bond.endorsed().isEmpty()) return InteractionResult.PASS;
        boolean entra = chest instanceof ArcaneChestBlockEntity arcane
                ? arcane.mayOpen(player)
                : chest instanceof OwnedBlockEntity owned && owned.mayUse(player);
        if (!entra) return InteractionResult.PASS;
        boolean algum = false;
        for (Soul soul : bond.endorsed()) algum |= grant(chest, soul);
        if (!algum) return InteractionResult.PASS;
        player.sendSystemMessage(Component.translatable("chat.thaumcraft.key.access.misc")
                .withStyle(ChatFormatting.DARK_PURPLE));
        level.playSound(null, context.getClickedPos(), TCSounds.KEY.value(),
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f);
        return InteractionResult.SUCCESS;
    }

    /** Põe alguém na lista de um baú arcano ou de um bloco com dono do Thaumcraft. */
    private static boolean grant(net.minecraft.world.level.block.entity.BlockEntity block, Soul soul) {
        if (block instanceof ArcaneChestBlockEntity chest) return chest.allow(soul.id(), soul.level());
        if (block instanceof OwnedBlockEntity owned) {
            String marca = soul.level() + soul.name();
            if (soul.name().equals(owned.owner) || owned.accessList.contains(marca)) return false;
            owned.accessList.add(marca);
            owned.setChanged();
            return true;
        }
        return false;
    }

    /**
     * O {@code onLeftClickEntity}: bater numa pessoa com a do Desvendar liga a chave à alma dela; com a do Endosso,
     * põe essa pessoa na lista que a chave carrega.
     */
    public static InteractionResult bind(Player player, Level level, InteractionHand hand, Entity entity,
                                         EntityHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ArcaneKeyItem key) || !(entity instanceof Player alvo)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Bond bond = bond(stack);
        if (key.type == 0) {
            stack.set(TCComponents.ARCANE_KEY, bond.withOwner(soul(alvo, (byte) 0)));
            player.sendSystemMessage(Component.translatable("chat.thaumcraft.key.boundplayer", alvo.getName().getString())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            stack.set(TCComponents.ARCANE_KEY, bond.withEndorsed(soul(alvo, (byte) 0)));
            player.sendSystemMessage(Component.translatable("chat.thaumcraft.key.accesslist", alvo.getName().getString())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("flavor.thaumcraft.key." + this.type).withStyle(ChatFormatting.DARK_PURPLE));
        Bond bond = bond(stack);
        if (bond.owner().isPresent() || bond.forger().isPresent()) lines.accept(Component.empty());
        bond.owner().ifPresent(soul ->
                lines.accept(Component.translatable("hint.thaumcraft.key.bound", soul.name()).withStyle(ChatFormatting.GRAY)));
        bond.forger().ifPresent(soul ->
                lines.accept(Component.translatable("hint.thaumcraft.key.forged", soul.name()).withStyle(ChatFormatting.GRAY)));
        for (Soul soul : bond.endorsed()) {
            lines.accept(Component.literal(" " + soul.name()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
