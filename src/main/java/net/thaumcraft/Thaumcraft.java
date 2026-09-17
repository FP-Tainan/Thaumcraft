package net.thaumcraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCFeatures;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.ResearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thaumcraft 4.2.3.5, de Azanor, trazido para o Minecraft de hoje.
 *
 * <p>A ideia é seguir o mod original peça por peça, sem misturar com as versões 5 e 6: o que estiver aqui é
 * o que estava lá, com os mesmos nomes, as mesmas contas e a mesma arte — só falando a língua do jogo novo.
 */
public class Thaumcraft implements ModInitializer {
    public static final String MOD_ID = "thaumcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger("Thaumcraft");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Aspects.init();
        ObjectAspects.init();
        Knowledges.init();
        TCBlocks.init();
        TCBlockEntities.init();
        TCComponents.init();
        TCItems.init();
        TCFeatures.init();
        TCNetwork.init();
        // as pesquisas que o original marca para vir abertas chegam com quem entra no mundo
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> ResearchManager.grantStarters(handler.getPlayer()));
        LOGGER.info("{} aspectos e {} coisas anotadas", Aspects.count(), ObjectAspects.size());
    }
}
