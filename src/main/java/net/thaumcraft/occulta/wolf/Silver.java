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
 * <p><b>E só vale corpo a corpo.</b> Dano de longe não é dano de prata, por mais prateada que seja a flecha —
 * é a pergunta {@code !source.isProjectile()} do original. O virote de prata do Witchery tinha o seu próprio
 * caminho, e esse ainda não está portado.
 */
public final class Silver {
    /** A etiqueta das armas que ferem um lobisomem. */
    public static final TagKey<Item> ARMAS = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            Thaumcraft.id("silver_weapons"));

    private Silver() {
    }

    /** Se esta pancada veio de prata. */
    public static boolean éDePrata(DamageSource fonte) {
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
