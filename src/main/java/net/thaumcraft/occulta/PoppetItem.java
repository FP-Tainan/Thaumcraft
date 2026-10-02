package net.thaumcraft.occulta;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Uma boneca: o {@code ItemPoppet} do Witchery.
 *
 * <p>Uma boneca solta não faz nada. <b>Presa a alguém</b> — com um Frasco de Vínculo, na bancada — ela passa a
 * responder por essa pessoa: quando a morte vem por onde a boneca guarda, é a boneca que morre no lugar dela.
 *
 * <p>Ela só precisa <b>existir</b>: vale na mochila de quem ela guarda, ou numa <b>prateleira de bonecas</b> em
 * qualquer canto do mundo. É o que o original faz, e é o que dá jeito a uma casa de bruxa.
 *
 * <p>As de proteção <b>quebram</b> ao valer; as outras se gastam aos poucos.
 */
public class PoppetItem extends Item {
    /** O que cada boneca guarda. */
    public enum Kind {
        /** A boneca solta, que ainda não é de ninguém. */
        NONE(false),
        /** Da queda. */
        EARTH(true),
        /** Do afogamento. */
        WATER(true),
        /** Do fogo e do estouro. */
        FIRE(true),
        /** Da fome. */
        HUNGER(true),
        /** Da ferramenta gasta, que ela conserta. */
        TOOL(false),
        /** Da morte, venha ela de onde vier. */
        DEATH(true),
        /** Da armadura gasta. */
        ARMOR(true),
        /** Da boneca de vodu de outrem. */
        VOODOO_PROTECTION(false),
        /** E a de vodu, que fere quem ela tem preso. */
        VOODOO(false);

        /** Se ela se desfaz ao valer, em vez de se gastar aos poucos. */
        public final boolean breaks;

        Kind(boolean breaks) {
            this.breaks = breaks;
        }
    }

    private final Kind kind;

    public PoppetItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** Se esta boneca responde por aquela pessoa. */
    public static boolean isFor(ItemStack stack, LivingEntity quem) {
        return stack.getItem() instanceof PoppetItem && TaglockItem.isFor(stack, quem);
    }

    // ------------------------------------------------------------------ a boneca de vodu

    /** Quanto tempo se segura a boneca antes de largar, e quanto ela se gasta a cada vez. */
    public static final int DRAW_TICKS = 80;
    public static final int VOODOO_COST = 10;

    /** Quanto tempo a pessoa arde, quando se aponta a boneca para lava. */
    public static final float LAVA_SECONDS = 10.0f;

    @Override
    public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack stack) {
        return this.kind == Kind.VOODOO ? net.minecraft.world.item.ItemUseAnimation.BOW
                : net.minecraft.world.item.ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity quem) {
        return DRAW_TICKS;
    }

    @Override
    public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level,
                                                     net.minecraft.world.entity.player.Player quem,
                                                     net.minecraft.world.InteractionHand mão) {
        if (this.kind != Kind.VOODOO || !TaglockItem.isBound(quem.getItemInHand(mão))) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        quem.startUsingItem(mão);
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    /**
     * O {@code onPlayerStoppedUsing}: ao largar a boneca, o que se lhe fez acontece a quem ela tem preso.
     *
     * <p>Apontada para <b>lava</b>, a pessoa pega fogo — e a boneca se desfaz. <b>De pé</b>, empurra-a para onde
     * se olha, com a força do tempo que se segurou. <b>Agachado</b>, com uma agulha de osso na mochila, espeta:
     * meio coração, e a agulha se gasta.
     */
    @Override
    public boolean releaseUsing(ItemStack stack, net.minecraft.world.level.Level level, LivingEntity quemUsa,
                                int aindaFalta) {
        if (this.kind != Kind.VOODOO) return false;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)
                || !(quemUsa instanceof net.minecraft.world.entity.player.Player quem)) {
            return false;
        }
        LivingEntity alvo = Voodoo.bound(server, stack);
        if (alvo == null) return false;
        if (Voodoo.guarded(server, alvo)) {
            Voodoo.backfire(server, quem);
            return true;
        }

        var olhando = quem.pick(5.0, 0.0f, true);
        boolean naLava = olhando instanceof net.minecraft.world.phys.BlockHitResult bateu
                && server.getBlockState(bateu.getBlockPos()).is(net.minecraft.world.level.block.Blocks.LAVA);
        if (naLava) {
            alvo.igniteForSeconds(LAVA_SECONDS);
            stack.shrink(1);
            return true;
        }

        if (!quem.isShiftKeyDown()) {
            float força = (DRAW_TICKS - aindaFalta) / 20.0f;
            var olhar = quem.getLookAngle();
            alvo.push(olhar.x * 0.9 * força, olhar.y * 0.3 * força, olhar.z * 0.9 * força);
            alvo.hurtMarked = true;
            wear(stack, server);
            return true;
        }

        if (quem.hasInfiniteMaterials() || Voodoo.takeNeedle(quem)) {
            alvo.hurtServer(server, server.damageSources().magic(), 0.5f);
            if (!quem.hasInfiniteMaterials()) wear(stack, server);
        }
        return true;
    }

    private static void wear(ItemStack stack, net.minecraft.server.level.ServerLevel level) {
        stack.hurtAndBreak(VOODOO_COST, level, null, quebrou -> {
        });
    }

    @Override
    public Component getName(ItemStack stack) {
        var preso = TaglockItem.bound(stack);
        if (preso == null) return super.getName(stack);
        return Component.translatable("item.thaumcraft.poppet.bound", super.getName(stack), preso.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        var preso = TaglockItem.bound(stack);
        if (preso == null) {
            linha.accept(Component.translatable("tc.poppet.unbound")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return;
        }
        linha.accept(Component.translatable("tc.taglock.bound", preso.name())
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
