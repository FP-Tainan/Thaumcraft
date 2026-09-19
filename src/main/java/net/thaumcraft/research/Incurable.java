package net.thaumcraft.research;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.Thaumcraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Os efeitos que o leite não tira: o {@code getCurativeItems().clear()} da 4.2.3.5, que a distorção, o gás e a gosma de
 * fluxo, a infusão instável e a fome estranha usam. Hoje o leite limpa tudo; o que foi posto por aqui volta logo depois
 * (o {@code LivingEntityIncurableMixin}).
 */
public final class Incurable {
    /** Os efeitos marcados neste ser. */
    public static final AttachmentType<List<Identifier>> MARKED = AttachmentRegistry.<List<Identifier>>builder()
            .persistent(Identifier.CODEC.listOf())
            .initializer(ArrayList::new)
            .buildAndRegister(Thaumcraft.id("incurable"));

    private Incurable() {
    }

    public static void init() {
    }

    /** Põe o efeito sem cura. */
    public static void add(LivingEntity entity, MobEffectInstance effect) {
        if (!entity.addEffect(effect)) return;
        Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        List<Identifier> marked = new ArrayList<>(entity.getAttachedOrCreate(MARKED));
        if (!marked.contains(id)) marked.add(id);
        entity.setAttached(MARKED, marked);
    }

    public static boolean marked(LivingEntity entity, Holder<MobEffect> effect) {
        List<Identifier> marked = entity.getAttached(MARKED);
        return marked != null && marked.contains(BuiltInRegistries.MOB_EFFECT.getKey(effect.value()));
    }
}
