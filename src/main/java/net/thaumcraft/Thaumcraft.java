package net.thaumcraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCFeatures;
import net.thaumcraft.registry.TCMenus;
import net.thaumcraft.registry.TCSounds;
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
        Knowledges.init();
        net.thaumcraft.registry.TCFluids.init();
        net.thaumcraft.registry.TCEffects.init();
        TCBlocks.init();
        TCBlockEntities.init();
        TCComponents.init();
        TCItems.init();
        net.thaumcraft.api.golems.GolemTypes.init();
        TCEntities.init();
        TCSounds.init();
        net.thaumcraft.registry.TCParticles.init();
        TCFeatures.init();
        TCMenus.init();
        TCNetwork.init();
        // o ouvido arcano: as notas de cada tique somem no fim dele
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_LEVEL_TICK.register(
                net.thaumcraft.block.entity.ArcaneEarBlockEntity::endTick);
        net.thaumcraft.crafting.LabelMarkingRecipe.init();
        net.thaumcraft.crafting.MutationRecipe.init();
        net.thaumcraft.crafting.TagSmeltingRecipe.init();
        net.thaumcraft.crafting.MetalIngotRecipe.init();
        // a fila de trocas do foco de Troca Equivalente, e o golpe da varinha que troca um bloco só
        net.thaumcraft.item.Swapper.init();
        net.thaumcraft.event.FortressMasks.init();
        net.thaumcraft.item.FocusSwap.init();
        net.thaumcraft.item.Architect.init();
        net.thaumcraft.baubles.BaublesEvents.init();
        // o Maleficium: o Tainted Magic, que no original era um mod à parte
        net.thaumcraft.maleficium.Maleficium.init();
        net.thaumcraft.naturalis.Naturalis.init();
        net.thaumcraft.forbidden.Forbidden.init();
        net.thaumcraft.mortuorum.Mortuorum.init();
        net.thaumcraft.event.RunicShield.init();
        net.thaumcraft.event.Hover.init();
        net.thaumcraft.loot.ChestLoot.init();
        net.thaumcraft.event.AspectOrbs.init();
        // a morte líquida: o que a criatura dissolvida deixa, e o tanque do spa aberto aos canos (menos por cima)
        net.thaumcraft.event.Dissolve.init();
        net.thaumcraft.event.Champions.init();
        net.thaumcraft.entity.eldritch.BossSpawns.init();
        net.thaumcraft.world.outer.Labyrinth.init();
        net.thaumcraft.research.Incurable.init();
        net.thaumcraft.event.MobDrops.init();
        net.thaumcraft.research.WarpEvents.guardianSpawner = net.thaumcraft.entity.eldritch.EldritchGuardianEntity::spawnForWarp;
        // os encantamentos: o Reparo com vis
        net.thaumcraft.event.Enchantments.init();
        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                (spa, side) -> side == net.minecraft.core.Direction.UP ? null : spa.tank, net.thaumcraft.registry.TCBlockEntities.ARCANE_SPA);
        // o crisol aceita água de qualquer lado (o IFluidHandler do TileCrucible)
        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                (crucible, side) -> crucible.tank, net.thaumcraft.registry.TCBlockEntities.CRUCIBLE);
        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register(net.thaumcraft.item.Focuses::tradeSwing);
        // o onItemUseFirst dos golens: o golem nasce e o sino marca antes de o baú se abrir
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(net.thaumcraft.item.GolemPlacerItem::placeFirst);
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(net.thaumcraft.item.GolemBellItem::markFirst);
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(net.thaumcraft.item.ResonatorItem::useFirst);
        // o baú itinerante vai atrás do dono de um mundo a outro
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(
                (player, origin, destination) -> net.thaumcraft.entity.TravelingTrunkEntity.followOwner(player));
        // e o onLeftClickEntity do sino: recolhe o golem
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(
                (player, level, hand, entity, hit) -> net.thaumcraft.item.GolemBellItem.pickUp(player, level, hand, entity));

        // a distorção: os eventos de cem em cem segundos e o olhar mortal; e o fluido purificante pesa pela permanente
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) net.thaumcraft.research.WarpEvents.tick(player);
        });
        net.thaumcraft.fluid.PurifyingFluid.permanentWarp = player -> net.thaumcraft.research.Knowledges.of(player).warpPerm();

        // as ferramentas elementais: o machado derruba a árvore e a espada acerta as criaturas em volta do alvo
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, be) -> !net.thaumcraft.item.ElementalAxeItem.fell(level, player, pos, state, be));
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(net.thaumcraft.item.ElementalSwordItem::sweep);
        // e as chaves do Magia Naturalis, que se ligam a quem elas batem
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(net.thaumcraft.naturalis.ArcaneKeyItem::bind);
        // e o sino de golem recolhe o Baú Maligno de volta em item
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register(
                net.thaumcraft.naturalis.EvilTrunkEntity::pickUp);

        // o comando de teste, para destrancar a pesquisa sem ter de jogar tudo de novo
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, registry, environment) ->
                        net.thaumcraft.command.ThaumcraftCommand.register(dispatcher));
        // as pesquisas que o original marca para vir abertas chegam com quem entra no mundo
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> ResearchManager.grantStarters(handler.getPlayer()));
        // de que cada coisa é feita: a tabela se monta com as receitas do servidor, e vai para quem entra
        // o que os mods de fora deixaram para montar tarde (receitas, que carregam itens) entra antes da dedução
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(
                server -> net.thaumcraft.api.ThaumcraftApi.runSetup());
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(ObjectAspects::rebuild);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            ObjectAspects.rebuild(server);
            for (var player : server.getPlayerList().getPlayers()) TCNetwork.syncAspects(player);
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> TCNetwork.syncAspects(handler.getPlayer()));
        LOGGER.info("{} aspectos", Aspects.count());
    }
}
