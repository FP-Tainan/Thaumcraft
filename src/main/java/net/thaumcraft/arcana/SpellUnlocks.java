package net.thaumcraft.arcana;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Os <b>segredos</b> do Ars Arcana: o {@code SpellUnlockManager} do Ars Magica 2.
 *
 * <p>Dez perícias do original não se compram com nível nenhum. Elas estão no quadro, de <b>prateado</b>, e
 * ficam lá trancadas até alguém <b>tropeçar nelas</b> — lançando um feitiço que tenha, <b>numa mesma etapa</b>,
 * a combinação certa de peças. Aí a perícia é aprendida na hora e o ponto prateado aparece para a pagar.
 *
 * <p>Ninguém diz a quem joga que elas existem. Não há dica, não há página no livro, não há receita: há um
 * feitiço que alguém escreveu por outra razão e que, ao ser lançado, abre uma porta. É a coisa mais bonita que
 * o Ars Magica 2 faz, e é a mais fácil de nunca se ver.
 *
 * <p>E há o outro lado: <b>um feitiço que leve uma dessas essências sem que quem o lança a saiba não sai</b>.
 * Não é que falhe — ele nem começa. É o que impede alguém de escrever uma Nevasca numa mesa emprestada e
 * lançá-la sem nunca ter descoberto nada.
 */
public final class SpellUnlocks {
    /**
     * Um segredo: a peça que se abre, e as peças que têm de estar juntas numa etapa para a abrir.
     *
     * @param abre    o que se descobre
     * @param precisa e o que é preciso lançar junto para o descobrir
     */
    public record Segredo(SpellPart abre, List<SpellPart> precisa) {
    }

    /**
     * Os dez do original, todos.
     *
     * <p>As combinações são as dele, tal e qual, e valem ser lidas: a <b>Nevasca</b> sai de uma Tempestade com
     * Dano Gélido, Congelar e Dano; a <b>Chuva de Fogo</b>, da mesma Tempestade com Dano de Fogo e Ignição; a
     * <b>Estrela Cadente</b>, de Dano Arcano com Gravidade e Distorção Astral — uma coisa pesada vinda de
     * longe, que é exatamente o que ela é; e o <b>Poder de Bênção</b>, de um feitiço que junte cinco bênçãos
     * de uma vez. Nenhuma delas é um acaso: são feitiços que alguém escreveria de propósito, se tivesse a
     * ideia.
     */
    private static final List<Segredo> SEGREDOS = List.of(
            new Segredo(Essences.FALLING_STAR, List.of(Essences.MAGIC_DAMAGE, Modifiers.GRAVITY,
                    Essences.ASTRAL_DISTORTION)),
            new Segredo(Essences.MANA_LINK, List.of(Essences.MANA_DRAIN, Essences.ENTANGLE)),
            new Segredo(Essences.BLIZZARD, List.of(Essences.STORM, Essences.FROST_DAMAGE,
                    Essences.FREEZE, Modifiers.DAMAGE)),
            new Segredo(Essences.FIRE_RAIN, List.of(Essences.STORM, Essences.FIRE_DAMAGE,
                    Essences.IGNITION, Modifiers.DAMAGE)),
            new Segredo(Modifiers.DISMEMBERING, List.of(Modifiers.PIERCING, Modifiers.DAMAGE)),
            new Segredo(Essences.MANA_SHIELD, List.of(Essences.SHIELD, Essences.REFLECT,
                    Essences.LIFE_TAP)),
            new Segredo(Modifiers.BUFF_POWER, List.of(Essences.HASTE, Essences.SLOWFALL,
                    Essences.SWIFT_SWIM, Essences.GRAVITY_WELL, Essences.LEAP)),
            new Segredo(Essences.DAYLIGHT, List.of(Essences.TRUE_SIGHT,
                    Essences.DIVINE_INTERVENTION, Essences.LIGHT)),
            new Segredo(Essences.MOONRISE, List.of(Essences.NIGHT_VISION,
                    Essences.ENDER_INTERVENTION, Modifiers.LUNAR)),
            new Segredo(Modifiers.PROSPERITY, List.of(Essences.DIG, Modifiers.FEATHER_TOUCH,
                    Modifiers.MINING_POWER)));

    private SpellUnlocks() {
    }

    /** Todos eles, para quem precisar de contar. */
    public static List<Segredo> all() {
        return SEGREDOS;
    }

    /**
     * Olha o feitiço antes de ele sair e abre o que houver para abrir.
     *
     * <p>É chamado <b>antes</b> de o feitiço correr, como o {@code SpellCastingEvent.Pre} do original, e por
     * isso a descoberta acontece mesmo que o feitiço não pegue em nada.
     */
    public static void descobre(Spell feitiço, LivingEntity quem) {
        if (!(quem instanceof Player gente)) return;

        SkillData sabe = SkillData.of(gente);
        for (Segredo segredo : SEGREDOS) {
            if (sabe.knows(segredo.abre())) continue;
            if (!estáNumaEtapa(feitiço, segredo.precisa())) continue;

            sabe = sabe.withSilver(sabe.silver() + 1);
            SkillData.set(gente, sabe);

            var perícia = SkillTree.of(segredo.abre());
            if (perícia != null) sabe = sabe.learn(perícia, Mana.of(gente).level());
            SkillData.set(gente, sabe);

            var item = ArcanaItems.itemOf(segredo.abre());
            gente.sendSystemMessage(Component.translatable("tc.spell.secret_found",
                    item == null ? Component.literal(segredo.abre().name())
                            : new net.minecraft.world.item.ItemStack(item).getHoverName()));
        }
    }

    /**
     * Se este feitiço leva alguma essência de segredo que quem o lança ainda não descobriu.
     *
     * <p>Só as <b>essências</b> trancam o feitiço; um modificador de segredo não tranca nada, e é o original
     * que faz essa distinção — um modificador não faz nada sozinho, e deixá-lo passar não estraga a surpresa.
     */
    public static boolean tranca(Spell feitiço, LivingEntity quem) {
        if (!(quem instanceof Player gente)) return false;
        SkillData sabe = SkillData.of(gente);

        for (Segredo segredo : SEGREDOS) {
            if (!(segredo.abre() instanceof SpellPart.Essence)) continue;
            if (sabe.knows(segredo.abre())) continue;
            for (Spell.Stage etapa : feitiço.stages()) {
                if (etapa.essences().contains(segredo.abre())) return true;
            }
        }
        return false;
    }

    /** Se todas aquelas peças estão juntas numa mesma etapa. */
    private static boolean estáNumaEtapa(Spell feitiço, List<SpellPart> precisa) {
        for (Spell.Stage etapa : feitiço.stages()) {
            boolean todas = true;
            for (SpellPart peça : precisa) {
                boolean tem = etapa.essences().contains(peça) || etapa.modifiers().contains(peça)
                        || etapa.shape() == peça;
                if (!tem) {
                    todas = false;
                    break;
                }
            }
            if (todas) return true;
        }
        return false;
    }
}
