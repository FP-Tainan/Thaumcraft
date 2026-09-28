package net.thaumcraft.arcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Uma peça de feitiço: o {@code ISpellPart} do Ars Magica 2.
 *
 * <p>A gramática do Ars Arcana tem três classes de palavra, e são estas: a <b>Forma</b> diz <i>como</i> o efeito
 * entra no mundo, a <b>Essência</b> diz <i>o que</i> ele faz, e o <b>Modificador</b> muda os números de um e de
 * outro. Um feitiço é uma frase escrita com elas.
 *
 * <p>Toda peça tem um <b>nome</b>, e é por ele que um feitiço guardado num item volta a ser um feitiço: o
 * original guarda números de registo, que mudam de instalação para instalação; aqui se guarda o nome, que não
 * muda. <b>Desvio declarado, e é de propósito:</b> um feitiço escrito num mundo continua legível noutro.
 */
public interface SpellPart {
    /** O nome desta peça, dentro do ramo. */
    String name();

    /**
     * Uma <b>Forma</b>: o {@code ISpellShape}.
     *
     * <p>Ela é quem começa a etapa: acha o que a etapa tem de atingir e manda as essências nele. Quando acaba,
     * é ela que passa a frase adiante — <b>tirando a primeira etapa</b> e recomeçando com o que sobrou. É por
     * isso que um feitiço de duas etapas encadeia: o projétil que bate chama o toque que vem a seguir.
     */
    interface Shape extends SpellPart {
        /**
         * Começa esta etapa.
         *
         * @param feitiço o que falta do feitiço, com esta etapa à frente
         * @param quem    quem o lançou
         * @param alvo    a quem ele já estava apontado, ou nada
         * @param onde    de onde ele parte
         * @return o que saiu
         */
        SpellCast.Result begin(ServerLevel level, Spell feitiço, LivingEntity quem, @Nullable Entity alvo,
                               Vec3 onde);

        /** O quanto esta forma multiplica o que o feitiço custa. */
        default float manaMultiplier() {
            return 1.0f;
        }

        /** Se ela se sustenta enquanto se segura o botão. */
        default boolean channeled() {
            return false;
        }

        /** Se ela só pode ser a <b>última</b> da frase, por não passar nada adiante. */
        default boolean terminus() {
            return false;
        }
    }

    /**
     * Uma <b>Essência</b>: o {@code ISpellComponent}.
     *
     * <p>É o que o feitiço faz de verdade. Ela não sabe achar ninguém — quem acha é a Forma; ela só sabe o que
     * fazer com quem lhe trouxerem.
     */
    interface Essence extends SpellPart {
        /** O que ela faz a um bicho ou a uma pessoa; devolve se pegou. */
        default boolean onEntity(ServerLevel level, Spell feitiço, LivingEntity quem, Entity alvo) {
            return false;
        }

        /** E o que ela faz a um bloco; devolve se pegou. */
        default boolean onBlock(ServerLevel level, Spell feitiço, LivingEntity quem, BlockPos onde,
                                Direction face, Vec3 batida) {
            return false;
        }

        /** Quanto ela custa de mana. */
        float manaCost();

        /** E quanto de desgaste ela deixa em quem a lançou. */
        default float burnout() {
            return 0.0f;
        }
    }

    /**
     * Um <b>Modificador</b>: o {@code ISpellModifier}.
     *
     * <p>Ele não faz nada sozinho: muda um número de quem faz. Cada um diz <b>que números mexe</b> e <b>por
     * quanto</b>, e um feitiço pode levar o mesmo modificador mais de uma vez — aí ele vale duas vezes.
     */
    interface Modifier extends SpellPart {
        /** Que feitios de número este modificador mexe. */
        java.util.Set<SpellModifierKind> modifies();

        /** Por quanto ele mexe naquele feitio. */
        float value(SpellModifierKind qual);

        /** E o quanto ele multiplica o custo, por vez que aparece. */
        default float manaMultiplier(int quantas) {
            return 1.0f;
        }
    }
}
