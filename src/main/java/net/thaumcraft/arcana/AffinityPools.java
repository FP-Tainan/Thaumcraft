package net.thaumcraft.arcana;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;
import net.thaumcraft.Thaumcraft;

/**
 * Os pingos que se juntam até virar um: os {@code accumulatedLifeRegen} e {@code accumulatedHungerRegen} do
 * {@code AffinityData} do Ars Magica 2.
 *
 * <p>A Vida devolve <b>0,025 × profundidade</b> por batida e a Natureza devolve <b>0,02</b> de comida — e nem
 * uma coisa nem outra existe em pedaço menor que um. Então o original guarda o resto num balde e, quando o
 * balde passa de um, dá <b>um</b> e tira <b>um</b> do balde. É o que faz a regeneração ser lenta e contínua em
 * vez de pular de meio em meio coração.
 */
public enum AffinityPools {
    /** O balde da vida que volta sozinha. */
    LIFE(AttachmentRegistry.<Float>builder()
            .initializer(() -> 0.0f)
            .persistent(com.mojang.serialization.Codec.FLOAT)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("affinity_life_pool"))),

    /** E o da comida que o sol dá a quem é de Natureza por inteiro. */
    HUNGER(AttachmentRegistry.<Float>builder()
            .initializer(() -> 0.0f)
            .persistent(com.mojang.serialization.Codec.FLOAT)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("affinity_hunger_pool")));

    private final AttachmentType<Float> tipo;

    AffinityPools(AttachmentType<Float> tipo) {
        this.tipo = tipo;
    }

    /**
     * Junta mais um pingo e, se o balde transbordar, corre o que tinha de correr.
     *
     * @param quanto o pingo desta batida
     * @param dá     o que fazer quando o balde passa de um
     */
    public void accumulate(ServerPlayer quem, float quanto, Runnable dá) {
        if (quanto <= 0.0f) return;
        float agora = quem.getAttachedOrCreate(this.tipo) + quanto;
        while (agora > 1.0f) {
            agora -= 1.0f;
            dá.run();
        }
        quem.setAttached(this.tipo, agora);
    }

    /** O que há no balde, para as provas. */
    public float get(ServerPlayer quem) {
        return quem.getAttachedOrCreate(this.tipo);
    }

    public void set(ServerPlayer quem, float quanto) {
        quem.setAttached(this.tipo, quanto);
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela os baldes a se registrarem. */
    public static void init() {
    }
}
