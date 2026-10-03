package net.thaumcraft.occulta.wolf;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.thaumcraft.Thaumcraft;

/**
 * O que conta como <b>prata</b>: o {@code CreatureUtil.isSilverDamage} do Witchery.
 *
 * <p>No original a pergunta é feita ao <b>material da espada</b> — se ele se chama {@code SILVER}, é prata. O
 * jogo de hoje não guarda o nome do material numa espada, e por isso aqui a pergunta é feita a uma
 * <b>etiqueta</b>: {@code thaumcraft:silver_weapons}.
 *
 * <p>Isso é mais do que uma tradução — é melhor, e de propósito: qualquer mod que traga prata pode pôr a
 * espada dele na etiqueta e ela passa a ferir lobisomem, sem este porte saber nada sobre esse mod. O
 * {@code PORTE.md} tem isto declarado.
 *
 * <p><b>De perto, só conta a mão.</b> Uma flecha prateada não é dano de prata — é a pergunta
 * {@code !source.isProjectile()} do original, e o que ela guarda é que prata se crava, não se atira.
 *
 * <p><b>Menos uma.</b> O <b>virote de prata</b> do Caçador de Bruxas tem o seu próprio caminho no original, e
 * é a única coisa de longe que fere um lobisomem. Está portado, e é por ele que se pergunta aqui.
 */
public final class Silver {
    /** A etiqueta das armas que ferem um lobisomem. */
    public static final TagKey<Item> ARMAS = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Thaumcraft.id("silver_weapons"));

    private Silver() {
    }

    /** Se esta pancada veio de prata: a mão com a arma certa, ou o virote de prata do Caçador. */
    public static boolean éDePrata(DamageSource fonte) {
        if (fonte.getDirectEntity() instanceof net.thaumcraft.occulta.hunter.BoltEntity virote) {
            return virote.éDePrata();
        }
        if (!fonte.isDirect()) return false;
        if (!(fonte.getEntity() instanceof LivingEntity quem)) return false;
        return quem.getMainHandItem().is(ARMAS);
    }

    /** Sem uso fora do porte: a etiqueta mora nos dados. */
    public static TagKey<Item> armas() {
        return ARMAS;
    }

    /** E as espadas do jogo, para quem precisar de saber se uma coisa é espada de todo. */
    public static TagKey<Item> espadas() {
        return ItemTags.SWORDS;
    }
}
