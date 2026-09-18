package net.thaumcraft.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.thaumcraft.Thaumcraft;

/**
 * Os sons do mod, os mesmos arquivos do original.
 *
 * <p>Arquivo gerado por {@code scratchpad/sons.js} a partir de
 * {@code Mod Base/Thaumcraft-1.7.10-4.2.3.5/assets/thaumcraft/sounds}. Nao mexer na mao.
 */
public final class TCSounds {
    /** A varinha bebendo vis de um no. */
    public static final Holder<SoundEvent> WAND = register("wand");
    /** A varinha sem vis para o que se pediu. */
    public static final Holder<SoundEvent> WAND_FAIL = register("wand_fail");
    /** A pagina do Thaumonomicon virando. */
    public static final Holder<SoundEvent> PAGE = register("page");
    /** A pena anotando a pesquisa. */
    public static final Holder<SoundEvent> WRITE = register("write");
    /** A pesquisa se abrindo na cabeca de quem estuda. */
    public static final Holder<SoundEvent> LEARN = register("learn");
    /** A bancada arcana comecando a montar. */
    public static final Holder<SoundEvent> CRAFT_START = register("craft_start");
    /** A bancada arcana falhando. */
    public static final Holder<SoundEvent> CRAFT_FAIL = register("craft_fail");
    /** O crisol fervendo. */
    public static final Holder<SoundEvent> BUBBLE = register("bubble");
    /** O crisol entornando a mistura errada. */
    public static final Holder<SoundEvent> SPILL = register("spill");
    /** A lasca de gelo saindo da varinha. */
    public static final Holder<SoundEvent> ICE = register("ice");
    /** O raio do foco de choque. */
    public static final Holder<SoundEvent> SHOCK = register("shock");
    /** O estalo do raio ao acertar. */
    public static final Holder<SoundEvent> ZAP = register("zap");
    /** O thaumometro medindo. */
    public static final Holder<SoundEvent> CAMERA_TICKS = register("camera_ticks");
    /** O thaumometro travando a medida. */
    public static final Holder<SoundEvent> CAMERA_CLACK = register("camera_clack");
    /** O cristal do no de aura. */
    public static final Holder<SoundEvent> CRYSTAL = register("crystal");
    /** O frasco de essencia. */
    public static final Holder<SoundEvent> JAR = register("jar");
    /** A varinha girando uma peca de encanamento. */
    public static final Holder<SoundEvent> TOOL = register("tool");
    /** A valvula do cano abrindo e fechando. */
    public static final Holder<SoundEvent> SQUEEK = register("squeek");

    private TCSounds() {
    }

    private static Holder<SoundEvent> register(String name) {
        Identifier id = Thaumcraft.id(name);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id,
                SoundEvent.createVariableRangeEvent(id));
    }

    /** Chamado na abertura do mod so para as constantes acima sairem do papel. */
    public static void init() {
    }
}
