package net.thaumcraft.occulta;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.coven.Coven;
import net.thaumcraft.occulta.coven.CovenSummon;
import net.thaumcraft.occulta.curse.Curse;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A <b>Pedra do Vidente</b>: o {@code itemSeerStone} do Witchery.
 *
 * <p>Ela faz duas coisas, e as duas são sobre o <b>coven</b>.
 *
 * <p><b>Agachado</b>, ela <b>lê</b> quem estiver a três casas de distância — ou quem a segura, não havendo
 * ninguém. E o que ela lê é a ficha oculta de uma bruxa: quanto tempo ela consegue andar fora do corpo,
 * que familiar tem, de que tamanho é o coven dela, que símbolos sabe desenhar, e que maldições carrega.
 * É a única maneira do mod de <b>ver outra pessoa por dentro</b>.
 *
 * <p><b>Segurada</b>, ela <b>chama o coven</b>: uma bruxa por segundo, pela ordem em que entraram, até um
 * minuto. Elas vêm, ficam um minuto e vão-se — um coven é uma visita.
 *
 * <p>E há uma terceira coisa, que não está neste arquivo: <b>com ela na mão, um círculo aceso conta o
 * coven</b>. Sem ela o coven existe e não comparece, e o rito corre no mínimo — é o original, e é o que
 * faz desta pedra a chave do ofício em grupo em vez de uma curiosidade.
 */
public class SeerStoneItem extends Item {
    /** Até onde a leitura alcança: as três casas do original. */
    public static final double ALCANCE = 3.0;

    /** Quanto tempo ela se deixa segurar: o minuto do original. */
    public static final int SEGURA = 1200;

    public SeerStoneItem(Properties properties) {
        super(properties);
    }

    /** O feitio dela: uma só, e brilhando, que é o {@code setEnchanted} do original. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1)
                .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack oquê) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack oquê, LivingEntity quem) {
        return SEGURA;
    }

    /** Agachado, lê; de pé, começa a chamar. */
    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        if (!quem.isShiftKeyDown()) {
            quem.startUsingItem(mão);
            return InteractionResult.CONSUME;
        }
        if (mundo instanceof ServerLevel level && quem instanceof ServerPlayer gente) {
            Player alvo = quemEstáOlhando(level, gente);
            lê(gente, alvo == null ? gente : alvo);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>Uma bruxa por segundo</b>, enquanto a pedra estiver segurada.
     *
     * <p>A conta do original é a mesma de um relógio que começa no zero: a primeira vem ao fim do primeiro
     * segundo, e dali em diante uma por segundo até acabar o coven.
     */
    @Override
    public void onUseTick(Level mundo, LivingEntity quem, ItemStack oquê, int faltam) {
        if (!(mundo instanceof ServerLevel level) || !(quem instanceof ServerPlayer gente)) return;
        int passou = SEGURA - faltam;
        if (passou <= 0 || passou % CovenSummon.A_CADA != 0) return;
        CovenSummon.chama(level, gente, gente.position(), passou / CovenSummon.A_CADA - 1);
    }

    /**
     * Quem está a ser olhado a três casas, se for gente.
     *
     * <p>O original usa o traçado de raio dele próprio, que vê bichos e blocos. Aqui basta o do jogo: a
     * reta dos olhos até três blocos, e o que ela encontrar primeiro.
     */
    private static @Nullable Player quemEstáOlhando(ServerLevel level, ServerPlayer quem) {
        Vec3 olhos = quem.getEyePosition();
        Vec3 até = olhos.add(quem.getLookAngle().scale(ALCANCE));
        AABB caixa = quem.getBoundingBox().expandTowards(quem.getLookAngle().scale(ALCANCE)).inflate(1.0);
        EntityHitResult bateu = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                level, quem, olhos, até, caixa,
                bicho -> bicho instanceof Player && bicho.isAlive(), 0.0f);
        if (bateu == null) return null;
        Entity achado = bateu.getEntity();
        return achado instanceof Player gente ? gente : null;
    }

    /**
     * <b>A leitura</b>: o {@code readPlayer} do original, pela mesma ordem e com as mesmas recusas.
     *
     * <p>Cada coisa tem duas falas — a que diz o número e a que diz que não há —, e é isso que faz a
     * leitura soar como uma leitura e não como uma tabela. "Não tem familiar" é uma informação.
     *
     * <p><b>Desvio declarado:</b> o original diz também a <b>perícia de engarrafar</b> de quem é lido.
     * Essa perícia não existe neste porte — está declarada como buraco no Caldeirão de Bruxa desde a fatia
     * dele —, e a linha dela fica de fora até ela existir.
     */
    public static void lê(ServerPlayer quem, Player alvo) {
        List<Component> linhas = new ArrayList<>();

        int manifesta = alvo instanceof ServerPlayer gente
                ? net.thaumcraft.occulta.spirit.SpiritManifest.credit(gente) : 0;
        linhas.add(manifesta > 0
                ? Component.translatable("tc.seerstone.manifestationtime", manifesta)
                : Component.translatable("tc.seerstone.nomanifestationtime"));

        String familiar = net.thaumcraft.occulta.familiar.FamiliarData.of(alvo).nome();
        linhas.add(familiar.isEmpty()
                ? Component.translatable("tc.seerstone.nofamiliar")
                : Component.translatable("tc.seerstone.familiar", familiar));

        int coven = Coven.tamanho(alvo);
        linhas.add(coven > 0
                ? Component.translatable("tc.seerstone.covensize", coven)
                : Component.translatable("tc.seerstone.nocoven"));

        String feitiços = símbolosSabidos(alvo);
        linhas.add(feitiços.isEmpty()
                ? Component.translatable("tc.seerstone.nospells")
                : Component.translatable("tc.seerstone.knownspells", feitiços));

        linhas.addAll(maldições(alvo));

        net.minecraft.network.chat.MutableComponent tudo = Component.empty();
        for (int i = 0; i < linhas.size(); i++) {
            if (i > 0) tudo.append(" ");
            tudo.append(linhas.get(i));
        }
        quem.sendSystemMessage(tudo.withStyle(ChatFormatting.BLUE));
    }

    /** Os nomes dos símbolos que a pessoa sabe desenhar, separados por vírgula. */
    private static String símbolosSabidos(Player alvo) {
        var sabe = net.thaumcraft.occulta.symbol.SymbolKnowledge.all(alvo);
        if (sabe.isEmpty()) return "";
        return String.join(", ", new java.util.TreeSet<>(sabe));
    }

    /** E as maldições que ela carrega, cada uma com o grau — ou a fala de quem não carrega nenhuma. */
    private static List<Component> maldições(Player alvo) {
        List<Component> quais = new ArrayList<>();
        for (Curse qual : Curse.values()) {
            int grau = Curse.level(alvo, qual);
            if (grau > 0) {
                quais.add(Component.translatable("tc.seerstone.curse." + qual.getSerializedName(), grau));
            }
        }
        if (quais.isEmpty()) return List.of(Component.translatable("tc.seerstone.notcursed"));
        return quais;
    }
}
