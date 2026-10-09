package net.thaumcraft.occulta.fetish;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** O que os três fetiches partilham e que não cabe nem no bloco nem na alma dele. */
public final class Fetishes {
    /** Os nomes dos seis modos de alarme, pela ordem em que a Boline os roda. */
    public static final String[] MODE_KEYS = {
            "playerwhitelist", "playerblacklist", "creaturewhitelist",
            "allnotfound", "onenotfound", "off",
    };

    /**
     * As <b>espécies que entram em grupo</b>: o {@code groupables} do original.
     *
     * <p>Prender uma vaca a um Espantalho prende <b>todas as vacas</b>; prender um lobo prende <b>aquele
     * lobo</b>. A diferença é esta lista, e ela é escrita a dedo — são as coisas de que se tem um rebanho.
     *
     * <p>O original compara pelo <b>nome traduzido</b> da espécie, e aqui é o mesmo: é por isso que um
     * bicho a que alguém pôs nome deixa de contar como rebanho e passa a contar como ele próprio.
     */
    private static final List<EntityType<?>> GROUPABLE = List.of(
            EntityTypes.VILLAGER, EntityTypes.SHEEP, EntityTypes.COW, EntityTypes.MOOSHROOM,
            EntityTypes.CHICKEN, EntityTypes.PIG, EntityTypes.HORSE, EntityTypes.BAT,
            EntityTypes.SQUID);

    private static @org.jetbrains.annotations.Nullable Set<String> nomes;

    /**
     * Os fetiches que estão de pé, para não varrer o mundo inteiro atrás deles.
     *
     * <p>O original percorre a <b>lista de almas carregadas</b> do mundo e pergunta a cada uma se é
     * um fetiche. Aqui eles se apontam ao nascer, que é o que este mod já faz com as prateleiras de
     * bonecas.
     */
    private static final List<FetishBlockEntity> DE_PÉ = new java.util.ArrayList<>();

    private Fetishes() {
    }

    public static void register(FetishBlockEntity qual) {
        if (!DE_PÉ.contains(qual)) DE_PÉ.add(qual);
    }

    public static void remove(FetishBlockEntity qual) {
        DE_PÉ.remove(qual);
    }

    public static List<FetishBlockEntity> standing() {
        DE_PÉ.removeIf(net.minecraft.world.level.block.entity.BlockEntity::isRemoved);
        return List.copyOf(DE_PÉ);
    }

    /**
     * <b>Se há um fetiche com este efeito a dezesseis blocos daquela pessoa</b>: o {@code isNearTo}.
     *
     * <p>É assim que a <b>Proteção de Vodu</b> chega às bonecas: elas não sabem do fetiche, e o
     * fetiche não sabe delas — a pergunta se faz no momento em que a boneca ia gastar-se.
     */
    public static boolean nearTo(net.minecraft.world.entity.player.Player quem, SpiritEffects qual) {
        for (FetishBlockEntity fetiche : standing()) {
            if (fetiche.getLevel() != quem.level()) continue;
            if (fetiche.effectType() != qual.id) continue;
            var onde = fetiche.getBlockPos();
            if (quem.distanceToSqr(onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5)
                    > SpiritEffects.RANGE_SQ) {
                continue;
            }
            return true;
        }
        return false;
    }

    /** Se aquele nome de espécie é de uma que entra em grupo. */
    public static boolean groupable(String nome) {
        if (nomes == null) {
            Set<String> feito = new HashSet<>();
            for (EntityType<?> qual : GROUPABLE) feito.add(qual.getDescription().getString());
            feito.add(net.thaumcraft.occulta.OccultaEntities.GOBLIN.getDescription().getString());
            feito.add(net.thaumcraft.occulta.OccultaEntities.COVEN_WITCH.getDescription().getString());
            nomes = feito;
        }
        return nomes.contains(nome);
    }

    /**
     * O que um fetiche <b>nunca</b> vê: cadáveres, ilusões, <b>espíritos</b> e familiares.
     *
     * <p>Um fetiche não se assusta com o que a bruxa pôs lá — e o espírito está na lista porque ele é a
     * moeda com que o próprio fetiche foi pago.
     */
    public static boolean ignorable(net.minecraft.server.level.ServerLevel level, Mob bicho) {
        if (bicho instanceof net.thaumcraft.occulta.spirit.CorpseEntity) return true;
        if (bicho instanceof net.thaumcraft.occulta.curse.IllusionEntity) return true;
        if (bicho instanceof net.thaumcraft.occulta.spirit.SpiritEntity) return true;
        return net.thaumcraft.occulta.familiar.Familiars.éDeAlguém(level, bicho);
    }

    /**
     * O que um fetiche largado leva dentro: o {@code TileData} do original.
     *
     * @param color     a tinta
     * @param mode      o modo de alarme
     * @param players   a gente que ele conhece, por nome
     * @param types     as espécies que ele conhece, por nome
     * @param creatures e os bichos que ele conhece, um a um
     */
    public record Saved(int color, int mode, List<String> players, List<String> types,
                        List<UUID> creatures) {
        public static final Saved EMPTY = new Saved(FetishBlockEntity.DEFAULT_COLOR,
                FetishBlockEntity.OFF, List.of(), List.of(), List.of());

        public static final Codec<Saved> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codec.INT.optionalFieldOf("color", FetishBlockEntity.DEFAULT_COLOR)
                                .forGetter(Saved::color),
                        Codec.INT.optionalFieldOf("mode", FetishBlockEntity.OFF).forGetter(Saved::mode),
                        Codec.STRING.listOf().optionalFieldOf("players", List.of())
                                .forGetter(Saved::players),
                        Codec.STRING.listOf().optionalFieldOf("types", List.of()).forGetter(Saved::types),
                        UUIDUtil.CODEC.listOf().optionalFieldOf("creatures", List.of())
                                .forGetter(Saved::creatures))
                .apply(i, Saved::new));

        public static final net.minecraft.network.codec.StreamCodec<
                net.minecraft.network.RegistryFriendlyByteBuf, Saved> STREAM_CODEC =
                net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC);

        public boolean isEmpty() {
            return this.players.isEmpty() && this.types.isEmpty() && this.creatures.isEmpty()
                    && this.mode == FetishBlockEntity.OFF
                    && this.color == FetishBlockEntity.DEFAULT_COLOR;
        }
    }
}
