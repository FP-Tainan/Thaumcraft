package net.thaumcraft.occulta.hunter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.arcana.Mana;
import net.thaumcraft.item.WandItem;

/**
 * <b>Chupar o poder de alguém</b>: o {@code ModHookManager.reducePowerLevels} do Witchery.
 *
 * <p>É o que o virote de drenagem forte faz, e é a coisa mais contra-mago que o ofício tem. Não tira vida: tira
 * <b>com que fazer magia</b>. Quem apanha um continua inteiro e fica sem nada para lançar.
 *
 * <p>No original isto toca em três poços — o do próprio Witchery, o do Ars Magica e o do Thaumcraft — porque lá
 * eram três mods. Aqui são <b>dois</b>, e estão no mesmo jar:
 *
 * <ul>
 *   <li>a <b>mana</b> do Ars Arcana, que é o mesmo poço que o original chamava de energia de infusão e de mana
 *       do Ars Magica (eram dois lá porque eram dois mods; aqui a conta é uma só);</li>
 *   <li>e o <b>vis</b> das varinhas que a pessoa carrega, que é o que o gancho do Thaumcraft chupava.</li>
 * </ul>
 *
 * <p><b>Tradução declarada:</b> o gancho do Thaumcraft chama o {@code consumeVisFromInventory} em volta, cem de
 * cada vez, até a conta acabar — e esse método aplica o desconto da ponteira da varinha. Aqui o vis se tira
 * <b>cru</b>, sem desconto nenhum: uma ponteira boa faz uma varinha <b>gastar</b> menos, e não a protege de
 * quem a está esvaziando. Está no {@code PORTE.md}.
 */
public final class Drain {
    /** Quantas voltas de cem o original dá por aspecto, quando a fração é inteira. */
    public static final int TUDO = 1000;

    /** E quantas dá por fração: cento e cinquenta vezes ela, nunca menos de uma. */
    public static final int POR_FRAÇÃO = 150;

    /** Cada volta tira um ponto de vis, que são cem centésimos. */
    public static final int POR_VOLTA = 100;

    /** Os seis primordiais, que é o que uma varinha guarda. */
    private static final Aspect[] PRIMORDIAIS = {
            Aspects.AIR, Aspects.EARTH, Aspects.FIRE, Aspects.WATER, Aspects.ORDER, Aspects.ENTROPY,
    };

    private Drain() {
    }

    /**
     * Chupa aquela fração do poder desta pessoa.
     *
     * <p>Devolve <b>verdadeiro</b> se tirou alguma coisa — que é o que diz se o virote valeu a pena.
     */
    public static boolean derruba(LivingEntity quem, float fração) {
        if (!(quem instanceof Player gente)) return false;
        if (gente.level().isClientSide()) return false;

        boolean tirou = mana(gente, fração);
        return varinhas(gente, fração) || tirou;
    }

    /** A mana do Ars Arcana: a fração do teto, nunca menos de um. */
    private static boolean mana(Player gente, float fração) {
        Mana conta = Mana.of(gente);
        float teto = conta.maxMana();
        if (teto <= 0.0f || conta.mana() <= 0.0f) return false;

        float quanto = Math.max(teto * fração, 1.0f);
        Mana.set(gente, conta.withMana(Math.max(conta.mana() - quanto, 0.0f)));
        return true;
    }

    /** E o vis de toda varinha que a pessoa carregue. */
    private static boolean varinhas(Player gente, float fração) {
        int voltas = fração >= 1.0f ? TUDO : Math.max((int) (POR_FRAÇÃO * fração), 1);
        int teto = voltas * POR_VOLTA;

        boolean tirou = false;
        for (int casa = 0; casa < gente.getInventory().getContainerSize(); casa++) {
            ItemStack oQueTem = gente.getInventory().getItem(casa);
            if (!(oQueTem.getItem() instanceof WandItem)) continue;

            AspectList tem = WandItem.vis(oQueTem);
            boolean mexeu = false;
            for (Aspect qual : PRIMORDIAIS) {
                int há = tem.getAmount(qual);
                if (há <= 0) continue;
                tem.reduce(qual, Math.min(há, teto));
                mexeu = true;
            }
            if (!mexeu) continue;
            WandItem.setVis(oQueTem, tem);
            tirou = true;
        }
        return tirou;
    }
}
