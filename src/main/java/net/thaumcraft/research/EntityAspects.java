package net.thaumcraft.research;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.entity.PechEntity;
import net.thaumcraft.entity.WispEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Do que as criaturas são feitas: o {@code ScanManager.generateEntityAspects} da 4.2.3.5 com a tabela do
 * {@code ConfigAspects} ({@link EntityAspectsTable}). Vale a última anotação da criatura cujas condições batem; o
 * jogador é sempre Humanus 4 e mais três aspectos tirados do nome dele (o de Azanor, Direwolf20 e Pahimar são outros).
 * O que não está na tabela não se examina.
 */
public final class EntityAspects {
    private record Entry(String key, String value, AspectList aspects) {
    }

    private static final Map<String, List<Entry>> TABLE = new HashMap<>();

    static {
        EntityAspectsTable.register(new Registrar());
    }

    private EntityAspects() {
    }

    public static final class Registrar {
        private Registrar() {
        }

        public void entity(String id, String key, String value, AspectList aspects) {
            TABLE.computeIfAbsent(id, k -> new ArrayList<>()).add(new Entry(key, value, aspects));
        }
    }

    /** O nome com que o original guardava a criatura examinada: o do jogador vai com o nome dele. */
    public static String key(Entity entity) {
        if (entity instanceof Player player) return "entity:player_" + player.getName().getString();
        return "entity:" + BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }

    public static AspectList of(Entity entity) {
        if (entity instanceof Player player) return player(player.getName().getString());
        List<Entry> entries = new ArrayList<>();
        List<Entry> own = TABLE.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        if (own != null) entries.addAll(own);
        // o barco de então é o barco de hoje de qualquer madeira (não o de baú)
        if (entity instanceof AbstractBoat && !(entity instanceof AbstractChestBoat)) {
            List<Entry> boat = TABLE.get("#boat");
            if (boat != null) entries.addAll(boat);
        }
        AspectList found = null;
        for (Entry entry : entries) {
            if (entry.key() == null || matches(entity, entry.key(), entry.value())) found = entry.aspects();
        }
        return found == null ? null : found.copy();
    }

    /** As condições de NBT do original, pelo que a criatura é hoje. */
    private static boolean matches(Entity entity, String key, String value) {
        return switch (key) {
            case "powered" -> entity instanceof Creeper creeper && creeper.isPowered() && value.equals("1");
            case "PechType" -> entity instanceof PechEntity pech && String.valueOf(pech.pechType()).equals(value);
            case "Type" -> entity instanceof WispEntity wisp && wisp.aspect() != null && wisp.aspect().tag().equals(value);
            default -> false;
        };
    }

    private static AspectList player(String name) {
        AspectList tags = new AspectList().add(Aspects.MAN, 4);
        if (name.equalsIgnoreCase("azanor")) return tags.add(Aspects.ELDRITCH, 20);
        if (name.equalsIgnoreCase("direwolf20")) return tags.add(Aspects.BEAST, 20);
        if (name.equalsIgnoreCase("pahimar")) return tags.add(Aspects.EXCHANGE, 20);
        Random rand = new Random(("player_" + name).hashCode());
        Aspect[] all = Aspects.all().toArray(new Aspect[0]);
        tags.add(all[rand.nextInt(all.length)], 4);
        tags.add(all[rand.nextInt(all.length)], 4);
        tags.add(all[rand.nextInt(all.length)], 4);
        return tags;
    }
}
