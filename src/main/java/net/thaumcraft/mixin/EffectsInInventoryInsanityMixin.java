package net.thaumcraft.mixin;

import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.thaumcraft.occulta.OccultaEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * O nome falso da <b>Insanidade</b> na lista de efeitos: o {@code renderInventoryEffect} do
 * {@code PotionInsanity}.
 *
 * <p>A poção que faz alguém ver bichos que não existem <b>não diz o próprio nome</b>. Em vez dele, a lista do
 * inventário mostra uma de <b>sete piadas</b>, e troca de piada <b>a cada três segundos</b> — a conta é
 * {@code duração / 60 % 7}, a do original. Quem olha a lista enquanto a tem vê o nome mudar à frente dos
 * olhos, e é essa a melhor parte: a poção mente sobre o que é.
 *
 * <p>O grau <b>não vai</b> no nome, como no original: ele desenha a linha inteira à mão e não escreve o
 * algarismo romano que o jogo poria. A duração continua debaixo, que é do jogo.
 *
 * <p>Isto mexe no desenho e não no efeito, e por isso é um remendo de tela: o nome de verdade da poção existe
 * e aparece em todo lugar onde não é esta lista.
 */
@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryInsanityMixin {
    /** De quantos em quantos tiques a piada troca. */
    private static final int TROCA = 60;

    /** Quantas piadas há. */
    private static final int PIADAS = 7;

    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$insanity(MobEffectInstance qual, CallbackInfoReturnable<Component> ci) {
        if (qual.getEffect() != OccultaEffects.INSANITY) return;
        int piada = qual.getDuration() / TROCA % PIADAS;
        ci.setReturnValue(Component.translatable("tc.potion.insanity." + piada));
    }
}
