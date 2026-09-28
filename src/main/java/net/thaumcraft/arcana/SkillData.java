package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * O que um arcanista <b>sabe</b> e o que ele tem para gastar: o {@code SkillData} do Ars Magica 2.
 *
 * <p>Duas coisas. As <b>perícias sabidas</b> — e saber uma é saber para sempre, não há desaprender — e os
 * <b>pontos</b> de cada cor, que se ganham subindo de nível e se gastam comprando.
 *
 * <p>Quem começa já sabe <b>três pontos azuis</b>, que é o suficiente para as três Formas de começo: a
 * Autoconjuração, o Toque e o Projétil. É o {@code new int[]{3, 0, 0, 0}} do original, e é a mão que ele dá a
 * quem chega.
 *
 * @param known as perícias sabidas, pelo nome da peça
 * @param spent quantos pontos de cada cor já se gastaram
 */
public record SkillData(Set<String> known, Map<SkillTree.Point, Integer> spent) {
    public static final SkillData NONE = new SkillData(Set.of(), Map.of());

    public static final Codec<SkillData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.listOf().optionalFieldOf("known", List.of())
                    .xmap(LinkedHashSet::new, List::copyOf)
                    .forGetter(d -> new LinkedHashSet<>(d.known())),
            Codec.unboundedMap(
                            Codec.STRING.xmap(
                                    n -> SkillTree.Point.valueOf(n.toUpperCase(Locale.ROOT)),
                                    p -> p.name().toLowerCase(Locale.ROOT)),
                            Codec.INT)
                    .optionalFieldOf("spent", Map.of()).forGetter(SkillData::spent))
            .apply(i, (sabidas, gastos) -> new SkillData(Set.copyOf(sabidas), gastos)));

    public static final AttachmentType<SkillData> DATA = AttachmentRegistry.<SkillData>builder()
            .initializer(() -> NONE)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("skills"));

    public static SkillData of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static void set(Player quem, SkillData agora) {
        quem.setAttached(DATA, agora);
    }

    // ------------------------------------------------------------------ saber

    /** Se aquela peça já é sabida. */
    public boolean knows(SpellPart qual) {
        return this.known.contains(qual.name());
    }

    /** E se tudo o que aquela perícia pede já é sabido. */
    public boolean canLearn(SkillTree.Entry qual) {
        if (this.knows(qual.part())) return false;
        for (SpellPart precisa : qual.needs()) {
            if (!this.knows(precisa)) return false;
        }
        return this.free(qual.point()) > 0;
    }

    /** Aprende, gastando o ponto. Quem não pode aprender fica como estava. */
    public SkillData learn(SkillTree.Entry qual, int level) {
        if (!this.canLearn(qual, level)) return this;
        var sabidas = new LinkedHashSet<>(this.known);
        sabidas.add(qual.part().name());
        var gastos = new EnumMap<SkillTree.Point, Integer>(SkillTree.Point.class);
        gastos.putAll(this.spent);
        gastos.merge(qual.point(), 1, Integer::sum);
        return new SkillData(Set.copyOf(sabidas), Map.copyOf(gastos));
    }

    /** A mesma prova, sabendo de que nível é quem quer aprender. */
    public boolean canLearn(SkillTree.Entry qual, int level) {
        if (this.knows(qual.part())) return false;
        for (SpellPart precisa : qual.needs()) {
            if (!this.knows(precisa)) return false;
        }
        return this.free(qual.point(), level) > 0;
    }

    // ------------------------------------------------------------------ os pontos

    /** Quantos pontos daquela cor já se gastaram. */
    public int used(SkillTree.Point qual) {
        return this.spent.getOrDefault(qual, 0);
    }

    /** Quantos sobram, para quem já chegou ao teto de cada cor. */
    public int free(SkillTree.Point qual) {
        return this.free(qual, SkillTree.RED_UNTIL);
    }

    /** E quantos sobram a quem é daquele nível. */
    public int free(SkillTree.Point qual, int level) {
        return SkillTree.pointsUpTo(qual, level) - this.used(qual);
    }

    /** Tudo o que se sabe, como peças. */
    public List<SpellPart> parts() {
        return this.known.stream().map(SpellParts::byName).filter(java.util.Objects::nonNull).toList();
    }

    /** Sem uso fora do porte: obriga a classe a ser carregada, e com ela o dado a se registrar. */
    public static void init() {
    }
}
