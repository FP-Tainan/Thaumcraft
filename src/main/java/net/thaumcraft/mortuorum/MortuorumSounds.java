package net.thaumcraft.mortuorum;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.thaumcraft.Thaumcraft;

/** Os sons do Ars Mortuorum, os mesmos arquivos do Necromancy. */
public final class MortuorumSounds {
    /** O uivo e o grito do Rastejador da Noite. */
    public static final SoundEvent NIGHT_CRAWLER_HOWL = register("nightcrawler_howl");
    public static final SoundEvent NIGHT_CRAWLER_SCREAM = register("nightcrawler_scream");
    /** O lacaio acordando no altar. */
    public static final SoundEvent MINION_SPAWN = register("minion_spawn");
    /** A lágrima de Isaac saindo. */
    public static final SoundEvent TEAR = register("tear");

    private MortuorumSounds() {
    }

    private static SoundEvent register(String nome) {
        Identifier id = Thaumcraft.id(nome);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}
