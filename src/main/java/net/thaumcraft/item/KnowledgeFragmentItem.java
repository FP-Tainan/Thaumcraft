package net.thaumcraft.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.research.Knowledges;

/**
 * O Fragmento de Conhecimento: o metadado nove do {@code ItemResource} da 4.2.3.5. Usado, some e dá a quem
 * o leu um ou dois pontos de pesquisa de cada primário.
 */
public class KnowledgeFragmentItem extends Item {
    public KnowledgeFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.getItemInHand(hand).consume(1, player);
        if (!level.isClientSide()) {
            var knowledge = Knowledges.of(player);
            for (Aspect aspect : net.thaumcraft.api.aspects.Aspects.primals()) {
                int q = level.getRandom().nextInt(2) + 1;
                knowledge.pool().add(aspect, q);
                if (player instanceof net.minecraft.server.level.ServerPlayer server) {
                    net.thaumcraft.net.TCNetwork.aspectPool(server, aspect, q, knowledge.points(aspect));
                }
            }
            Knowledges.save(player, knowledge);
        }
        return InteractionResult.SUCCESS;
    }
}
