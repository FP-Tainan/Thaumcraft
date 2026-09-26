package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A Assinatura de Fenda: o {@code ItemRiftSignature} das Portas Dimensionais.
 *
 * <p>Usada uma vez, marca o lugar; usada outra, rasga uma fenda em cada um dos dois lugares e liga um ao outro,
 * nos dois sentidos. A Assinatura Estabilizada faz o mesmo e não se gasta nem esquece o lugar marcado, que é o que
 * a torna útil para ligar muitos sítios ao mesmo.
 */
public class RiftSignatureItem extends Item {
    private final boolean stabilized;

    public RiftSignatureItem(Properties properties, boolean stabilized) {
        super(properties);
        this.stabilized = stabilized;
    }

    /** O lugar marcado, guardado no próprio item. */
    public static RiftBlockEntity.@Nullable Destination source(ItemStack stack) {
        return stack.get(ShatteredComponents.RIFT_SOURCE);
    }

    public static void setSource(ItemStack stack, RiftBlockEntity.@Nullable Destination onde) {
        if (onde == null) stack.remove(ShatteredComponents.RIFT_SOURCE);
        else stack.set(ShatteredComponents.RIFT_SOURCE, onde);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player quem = context.getPlayer();
        if (quem == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();

        // a fenda nasce na casa vazia diante daquela em que se clicou
        BlockPos onde = level.getBlockState(context.getClickedPos()).canBeReplaced()
                ? context.getClickedPos() : context.getClickedPos().relative(context.getClickedFace());
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;

        var marcado = source(stack);
        if (marcado == null) {
            setSource(stack, new RiftBlockEntity.Destination(level.dimension(), onde, quem.getYRot()));
            quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_signature.stored"));
            level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        ServerLevel outro = server.getServer().getLevel(marcado.level());
        if (outro == null) {
            setSource(stack, null);
            quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_signature.lost"));
            return InteractionResult.FAIL;
        }

        // uma fenda de cada lado, apontando uma para a outra
        rift(outro, marcado.pos(), new RiftBlockEntity.Destination(level.dimension(), onde, quem.getYRot()));
        rift(server, onde, marcado);

        if (!this.stabilized) {
            setSource(stack, null);
            stack.hurtAndBreak(1, quem, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        quem.sendOverlayMessage(Component.translatable("item.thaumcraft.rift_signature.created"));
        level.playSound(null, onde, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    /** Rasga a fenda naquela casa e aponta-a. Se já houver porta lá, é a porta que passa a apontar. */
    static void rift(ServerLevel level, BlockPos onde, RiftBlockEntity.Destination para) {
        if (!(level.getBlockEntity(onde) instanceof RiftBlockEntity)) {
            if (!level.getBlockState(onde).canBeReplaced()) return;
            level.setBlockAndUpdate(onde, ShatteredBlocks.RIFT.defaultBlockState());
        }
        if (level.getBlockEntity(onde) instanceof RiftBlockEntity fenda) {
            fenda.setDestination(para);
            // quem rasgou sabe onde rasgou: esta fenda não pede os Óculos do Véu
            fenda.setNatural(false);
        }
    }

    /** A chave do lugar marcado, para o componente. */
    public static ResourceKey<Level> levelKey(String nome) {
        return ResourceKey.create(Registries.DIMENSION, net.minecraft.resources.Identifier.parse(nome));
    }
}
