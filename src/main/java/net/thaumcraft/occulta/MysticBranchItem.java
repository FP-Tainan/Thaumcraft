package net.thaumcraft.occulta;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.infusion.Infusion;
import net.thaumcraft.occulta.infusion.Infusions;
import net.thaumcraft.occulta.symbol.Spells;
import net.thaumcraft.occulta.symbol.Symbol;
import net.thaumcraft.occulta.symbol.Symbols;

/**
 * A <b>Vara Mística</b>: o {@code ItemMysticBranch} do Witchery.
 *
 * <p>É com ela que se <b>desenham os símbolos</b>. Segura-se o botão, move-se a cabeça — cada sete graus de
 * giro conta um traço —, e quando o que foi desenhado bate com um dos desenhos da tabela, o nome do feitiço
 * aparece. Largando a vara, ele sai.
 *
 * <p>É a única coisa deste mod em que <b>o gesto é a interface</b>. Não há menu, não há lista, não há botão:
 * há um desenho que se sabe ou não se sabe fazer.
 *
 * <h2>O que ela pede</h2>
 *
 * <ul>
 *   <li><b>uma infusão</b>, qualquer uma — sem ela a vara não lança nada;</li>
 *   <li><b>a Infusão Infernal</b>, se o símbolo for dos <b>imperdoáveis</b>;</li>
 *   <li>e <b>carga</b> bastante, que é o custo do símbolo dobrado por grau.</li>
 * </ul>
 *
 * <p>E um desenho de <b>grau dois ou três</b> só vale o grau dele a quem tiver <b>Adoração</b> bastante —
 * sem ela, o desenho comprido é lançado como grau um e o esforço foi para nada. É o que liga os símbolos à
 * Estátua de Adoração, que ainda não está portada.
 *
 * <p>Ela também se <b>deita no altar</b>, como a Arthana — e aí o altar ganha <b>poder de encanto</b>.
 */
public class MysticBranchItem extends Item {
    /** Quanto tempo ela se deixa segurar: as trinta e seis mil batidas do original. */
    public static final int SEGURA = 36000;

    public MysticBranchItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack oquê) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack oquê, LivingEntity quem) {
        return SEGURA;
    }

    /** Pegar nela <b>apaga o feitiço preparado</b> e começa um desenho novo. */
    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        if (quem instanceof ServerPlayer gente) Spells.esquece(gente);
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /**
     * <b>Deitada no altar</b>, como a Arthana.
     *
     * <p>Só no topo de uma pedra de altar com ar por cima.
     */
    @Override
    public InteractionResult useOn(UseOnContext onde) {
        Level mundo = onde.getLevel();
        BlockPos pedra = onde.getClickedPos();
        if (onde.getClickedFace() != Direction.UP) return super.useOn(onde);
        if (!mundo.getBlockState(pedra).is(OccultaBlocks.WITCH_ALTAR)) return super.useOn(onde);
        if (!mundo.getBlockState(pedra.above()).isAir()) return super.useOn(onde);

        if (!mundo.isClientSide()) {
            ItemStack vara = onde.getItemInHand();
            PlacedItemBlock.põe(mundo, pedra.above(), vara.copyWithCount(1), onde.getPlayer());
            vara.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>E largá-la lança o que estiver preparado.</b>
     *
     * <p>A ordem das recusas é a do original, e ela conta uma história: primeiro se pergunta se há feitiço,
     * depois se há infusão, depois se a infusão serve, depois se o feitiço está de molho, e só no fim se há
     * carga. Cada recusa tem o seu recado.
     */
    @Override
    public boolean releaseUsing(ItemStack oquê, Level mundo, LivingEntity quem, int faltam) {
        if (!(mundo instanceof ServerLevel level) || !(quem instanceof ServerPlayer gente)) return false;
        Spells.Preparado preparado = Spells.preparado(gente);
        Spells.esquece(gente);
        if (preparado == null) {
            recusa(level, gente, "unknownsymbol");
            return false;
        }

        Symbol qual = Symbols.daquele(preparado.id());
        if (qual == null) {
            recusa(level, gente, "unknownsymbol");
            return false;
        }

        var carga = gente.getAttachedOrCreate(Infusions.CARGA);
        if (!gente.getAbilities().instabuild && carga.id() == 0) {
            recusa(level, gente, "infusionrequired");
            return false;
        }
        if (!qual.serveAInfusão(gente, carga.id())) {
            recusa(level, gente, "infernalrequired");
            return false;
        }

        long falta = Spells.travaQueFalta(gente, qual, level);
        if (falta > 0L && !gente.getAbilities().instabuild) {
            gente.sendSystemMessage(Component.translatable("tc.occulta.infuse.branch.effectoncooldown",
                    falta / 20L).withStyle(ChatFormatting.RED));
            Infusion.falha(level, gente);
            return false;
        }

        int grau = Spells.grauQueVale(gente, preparado.grau());
        int custa = qual.custo(grau);
        if (!gente.getAbilities().instabuild && Infusions.energia(gente) < custa) {
            recusa(level, gente, "nocharges");
            return false;
        }

        qual.lança(level, gente, grau);
        Spells.põeTrava(gente, qual, level);
        if (!gente.getAbilities().instabuild) {
            Infusions.põeEnergia(gente, Infusions.energia(gente) - custa);
        }
        return false;
    }

    private static void recusa(ServerLevel level, ServerPlayer quem, String porquê) {
        quem.sendSystemMessage(Component.translatable("tc.occulta.infuse.branch." + porquê)
                .withStyle(ChatFormatting.RED));
        Infusion.falha(level, quem);
    }
}
