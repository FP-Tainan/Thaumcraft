package net.thaumcraft.occulta.vampire;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaComponents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * O <b>Cálice de Vidro</b>: o {@code ItemGlassGoblet} do Witchery.
 *
 * <p>É o item mais importante do ramo do vampiro, porque é a <b>porta</b>: vazio, ele é um copo; cheio do
 * sangue certo, é a única coisa no mod que faz de alguém um vampiro.
 *
 * <p>Ele enche de três maneiras, e as três são degraus diferentes da história:
 *
 * <ol>
 *   <li>com <b>sangue de galinha</b>, sacrificada com a {@linkplain net.thaumcraft.occulta.OccultaItems#BOLINE
 *       Boline} sobre o rito — e esse serve para <b>chamar</b>, não para beber;</li>
 *   <li>com o <b>sangue de Lilith</b>, que ela dá a quem a vencer: bebê-lo é <b>virar vampiro</b>;</li>
 *   <li>e com o <b>próprio sangue</b> de um vampiro do nono grau, que gasta cento e vinte e cinco de poder
 *       para o encher.</li>
 * </ol>
 *
 * <p>Beber um cálice cheio de sangue que <b>não</b> é de galinha, não sendo já vampiro, vira. É a entrada do
 * mod inteiro, e é de propósito que ela seja tão estreita: quem não procurou Lilith não entra por acaso.
 *
 * <p>Quem já é vampiro e bebe não vira nada — só arrota, e o cálice se esvazia.
 */
public class GobletItem extends Item {
    /** Quanto tempo se leva a beber, e o que um vampiro do nono grau gasta para o encher. */
    public static final int DEMORA = 32;
    public static final int CUSTA = 125;

    /** Do nono grau em diante ele pode encher o cálice com o próprio sangue. */
    public static final int ENCHE_AOS = 9;

    public GobletItem(Properties properties) {
        super(properties);
    }

    /** O que está dentro, ou nada. */
    @Nullable
    public static GobletBlood dentro(ItemStack cálice) {
        return cálice.get(OccultaComponents.GOBLET);
    }

    public static boolean cheio(ItemStack cálice) {
        return dentro(cálice) != null;
    }

    /** Enche-o. */
    public static void enche(ItemStack cálice, GobletBlood oquê) {
        cálice.set(OccultaComponents.GOBLET, oquê);
    }

    /** E esvazia-o. */
    public static void esvazia(ItemStack cálice) {
        cálice.remove(OccultaComponents.GOBLET);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack cálice) {
        return cheio(cálice) ? ItemUseAnimation.DRINK : ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack cálice, LivingEntity quem) {
        return DEMORA;
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack cálice = quem.getItemInHand(mão);
        if (!cheio(cálice) && Vampire.grauDe(quem) < ENCHE_AOS) {
            // um cálice vazio na mão de quem não tem sangue para o encher não faz nada
            if (level instanceof ServerLevel) diz(quem, Vampire.é(quem) ? "lowlevel" : "nothing");
            return InteractionResult.CONSUME;
        }
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /**
     * Beber, ou encher com o próprio sangue.
     *
     * <p>São os dois lados do mesmo gole: um cálice <b>vazio</b> na mão de um vampiro do nono grau se enche do
     * sangue dele; um cálice <b>cheio</b> se esvazia na boca de quem o levou.
     */
    @Override
    public ItemStack finishUsingItem(ItemStack cálice, Level level, LivingEntity quemBebeu) {
        if (!(level instanceof ServerLevel mundo) || !(quemBebeu instanceof Player quem)) return cálice;

        if (!cheio(cálice)) {
            enche_com_o_proprio(mundo, quem, cálice);
            return cálice;
        }

        GobletBlood oquê = dentro(cálice);
        mundo.playSound(null, quem.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS,
                0.5f, mundo.getRandom().nextFloat() * 0.1f + 0.9f);

        if (oquê != null && oquê.fonte() != GobletBlood.Fonte.GALINHA && !Vampire.é(quem)) {
            vira(mundo, quem);
        }
        esvazia(cálice);
        return cálice;
    }

    /** O nono degrau: o vampiro enche o cálice do próprio sangue, e isso lhe custa. */
    private static void enche_com_o_proprio(ServerLevel level, Player quem, ItemStack cálice) {
        if (Vampire.grauDe(quem) < ENCHE_AOS) return;
        if (!Vampire.gasta(quem, CUSTA, true)) {
            diz(quem, "notenoughblood");
            return;
        }
        enche(cálice, GobletBlood.de(quem));
        level.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                quem.getX(), quem.getY() + quem.getBbHeight() * 0.85, quem.getZ(), 16, 0.8, 0.3, 0.8, 0.0);
        level.playSound(null, quem.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /**
     * <b>E então ele vira.</b>
     *
     * <p>O sangue dela entra e o dele sai: o poder de sangue começa no cento e vinte e cinco do primeiro
     * grau, e o <b>sangue de gente</b> que ele tinha vai a zero — não há mais nada de vivo nele para um outro
     * vampiro vir buscar.
     */
    private static void vira(ServerLevel level, Player quem) {
        Vampire.grau(quem, 1);
        Blood.põe(quem, 0);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                quem.getX(), quem.getY() + 1.0, quem.getZ(), 32, 0.8, 1.5, 0.8, 0.0);
        level.playSound(null, quem.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        quem.sendSystemMessage(Component.translatable("tc.goblet.turned")
                .withStyle(ChatFormatting.DARK_RED));
    }

    /**
     * <b>O cálice no crânio</b>: com sangue de galinha dentro, de noite e a céu aberto, ele chama Elle.
     *
     * <p>É o único uso do sangue de galinha, e é por isso que ele não serve para beber: ele não é alimento,
     * é <b>chamada</b>.
     */
    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext onde) {
        Player quem = onde.getPlayer();
        if (quem == null) return InteractionResult.PASS;
        if (!(onde.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        ItemStack cálice = onde.getItemInHand();
        GobletBlood oquê = dentro(cálice);
        if (oquê == null || oquê.fonte() != GobletBlood.Fonte.GALINHA) return InteractionResult.PASS;
        if (!(level.getBlockState(onde.getClickedPos()).getBlock()
                instanceof net.minecraft.world.level.block.SkullBlock)) {
            return InteractionResult.PASS;
        }

        if (!VampireRitual.chama(level, quem, onde.getClickedPos())) {
            diz(quem, "seemswrong");
            return InteractionResult.SUCCESS;
        }
        esvazia(cálice);
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>A galinha sacrificada</b>: o {@code handleCreatureDeath} do original.
     *
     * <p>Quem matar uma galinha <b>com a Boline na mão</b>, em cima ou ao lado do rito, enche o primeiro
     * cálice vazio que tiver na barra de itens. É o passo que o mod nunca explica, e é de propósito: ele é
     * para quem leu o livro certo.
     */
    public static void aGalinha(ServerLevel level, Player quem, net.minecraft.world.entity.LivingEntity morta) {
        if (!(morta instanceof net.minecraft.world.entity.animal.chicken.Chicken)) return;
        if (!quem.getMainHandItem().is(net.thaumcraft.occulta.OccultaItems.BOLINE)) return;

        BlockPos meio = acharORito(level, morta.blockPosition());
        if (meio == null) return;

        for (int casa = 0; casa < 9; casa++) {
            ItemStack cálice = quem.getInventory().getItem(casa);
            if (!(cálice.getItem() instanceof GobletItem) || cheio(cálice)) continue;

            enche(cálice, GobletBlood.GALINHA);
            level.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                    morta.getX(), morta.getY() + morta.getBbHeight() * 0.85, morta.getZ(),
                    16, 0.5, 0.5, 0.5, 0.0);
            level.playSound(null, morta.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            return;
        }
    }

    /** O rito, se houver um a um bloco de onde a galinha caiu. */
    @Nullable
    private static BlockPos acharORito(ServerLevel level, BlockPos perto) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos onde = perto.offset(dx, dy, dz);
                    if (VampireRitual.desenhado(level, onde)) return onde;
                }
            }
        }
        return null;
    }

    private static void diz(Player quem, String oquê) {
        quem.sendSystemMessage(Component.translatable("tc.goblet." + oquê).withStyle(ChatFormatting.RED));
    }

    @Override
    public Component getName(ItemStack cálice) {
        return cheio(cálice)
                ? Component.translatable("item.thaumcraft.goblet.full")
                : super.getName(cálice);
    }

    @Override
    public void appendHoverText(ItemStack cálice, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> linha, TooltipFlag flag) {
        GobletBlood oquê = dentro(cálice);
        if (oquê == null) return;
        linha.accept(Component.translatable("tc.goblet.tip", oquê.diz()).withStyle(ChatFormatting.DARK_RED));
    }
}
