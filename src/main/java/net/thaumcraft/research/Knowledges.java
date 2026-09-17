package net.thaumcraft.research;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

/**
 * Onde o que cada jogador sabe fica guardado, colado nele e salvo com o mundo.
 *
 * <p>No mod original isso era um arquivo {@code .thaum} por nome de jogador, o que fazia a pesquisa se
 * perder quando a pessoa trocava de nome. Aqui anda junto do jogador, então isso não acontece.
 */
public final class Knowledges {
    public static final AttachmentType<PlayerKnowledge> KNOWLEDGE = AttachmentRegistry
            .<PlayerKnowledge>builder()
            .initializer(PlayerKnowledge::new)
            .persistent(PlayerKnowledge.CODEC)
            // vai junto para a máquina de quem joga: é o visor do aparelho que precisa disto
            .syncWith(PlayerKnowledge.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("knowledge"));

    private Knowledges() {
    }

    public static void init() {
    }

    public static PlayerKnowledge of(Player player) {
        return player.getAttachedOrCreate(KNOWLEDGE);
    }

    public static void save(Player player, PlayerKnowledge knowledge) {
        player.setAttached(KNOWLEDGE, knowledge);
    }
}
