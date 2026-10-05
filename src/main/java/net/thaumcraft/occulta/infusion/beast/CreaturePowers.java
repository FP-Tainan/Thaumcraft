package net.thaumcraft.occulta.infusion.beast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O <b>registro dos poderes de bicho</b> e o que cada jogador carrega deles: o
 * {@code CreaturePower.Registry} do Witchery.
 *
 * <p>São <b>vinte e cinco</b> poderes de <b>treze famílias</b>, e a lista é a do original, número por
 * número — porque o número é o que fica guardado no jogador e o que a barra da tela lê.
 *
 * <p>Repare no que a lista diz sobre o mod: metade dela são bichos de capoeira que só sabem <b>curar</b>, e
 * esses dão <b>uma carga</b> em vez de dez. Matar uma ovelha para se curar um coração é um péssimo negócio,
 * e é de propósito — o poder de bicho é para os bichos que custam a apanhar.
 */
public final class CreaturePowers {
    /** O teto de cargas de bicho. */
    public static final int TETO = 20;

    /** O que ele carrega: que poder, e quantas cargas dele. */
    public record Bicho(int id, int cargas) {
        public static final Bicho NENHUM = new Bicho(0, 0);

        public static final Codec<Bicho> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                Codec.INT.fieldOf("id").forGetter(Bicho::id),
                Codec.INT.fieldOf("cargas").forGetter(Bicho::cargas)
        ).apply(campo, Bicho::new));
    }

    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, Bicho> PELA_REDE =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(Bicho.CODEC);

    /** O que ele carrega — e que o lado de cá também vê, para a segunda barra e para os poderes de andar. */
    public static final AttachmentType<Bicho> BICHO = AttachmentRegistry.<Bicho>builder()
            .initializer(() -> Bicho.NENHUM)
            .persistent(Bicho.CODEC)
            .syncWith(PELA_REDE,
                    net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("beast_power"));

    private static final List<CreaturePower> TODOS = new ArrayList<>();

    static {
        // os números são os do original, um a um
        põe(new SpiderPower(1, EntityTypes.CAVE_SPIDER));
        põe(new SpiderPower(2, EntityTypes.SPIDER));
        põe(new CreeperPower(3));
        põe(new BatPower(4, EntityTypes.BAT));
        põe(new SquidPower(5));
        põe(new GhastPower(6));
        põe(new BlazePower(7));
        põe(new PigManPower(8));
        põe(new ZombiePower(9));
        põe(new SkeletonPower(10));
        põe(new JumpPower(11, EntityTypes.MAGMA_CUBE));
        põe(new JumpPower(12, EntityTypes.SLIME));
        põe(new SpeedPower(13, EntityTypes.SILVERFISH));
        põe(new SpeedPower(14, EntityTypes.OCELOT));
        põe(new SpeedPower(15, EntityTypes.WOLF));
        põe(new SpeedPower(16, EntityTypes.HORSE));
        põe(new EndermanPower(17));
        põe(new HealPower(18, EntityTypes.SHEEP, 1));
        põe(new HealPower(19, EntityTypes.COW, 1));
        põe(new HealPower(20, EntityTypes.CHICKEN, 1));
        põe(new HealPower(21, EntityTypes.PIG, 1));
        põe(new HealPower(22, EntityTypes.VILLAGER, 2));
        põe(new HealPower(23, EntityTypes.MOOSHROOM, 2));
        põe(new BatPower(24, OccultaEntities.OWL));
        põe(new JumpPower(25, OccultaEntities.TOAD));
    }

    private CreaturePowers() {
    }

    private static void põe(CreaturePower qual) {
        TODOS.add(qual);
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego e a lista. */
    public static void init() {
    }

    /** O poder daquele número, ou nada. */
    public static @Nullable CreaturePower daquele(int id) {
        for (CreaturePower cada : TODOS) {
            if (cada.id == id) return cada;
        }
        return null;
    }

    /** E o que se tira daquele bicho, ou nada. */
    public static @Nullable CreaturePower de(Entity bicho) {
        EntityType<?> qual = bicho.getType();
        for (CreaturePower cada : TODOS) {
            if (cada.dequê == qual) return cada;
        }
        return null;
    }

    /** Quantos há. */
    public static int quantos() {
        return TODOS.size();
    }

    // ------------------------------------------------------------------ o que ele carrega

    /** O poder que ele carrega, ou nada. */
    public static @Nullable CreaturePower dele(Player quem) {
        return daquele(quem.getAttachedOrCreate(BICHO).id());
    }

    public static int cargas(Player quem) {
        return quem.getAttachedOrCreate(BICHO).cargas();
    }

    /**
     * <b>Toma o poder daquele bicho.</b>
     *
     * <p>Se já for o mesmo, <b>soma</b> as cargas até o teto. Se for outro, <b>troca</b> — e o que se tinha
     * se perde inteiro.
     */
    public static void toma(ServerPlayer quem, CreaturePower qual) {
        Bicho tinha = quem.getAttachedOrCreate(BICHO);
        if (tinha.id() == qual.id) {
            quem.setAttached(BICHO, new Bicho(qual.id,
                    Math.min(tinha.cargas() + qual.porBicho(), TETO)));
            return;
        }
        quem.setAttached(BICHO, new Bicho(qual.id, qual.porBicho()));
    }

    /** Tira cargas de bicho. */
    public static void põeCargas(ServerPlayer quem, int quantas) {
        Bicho tinha = quem.getAttachedOrCreate(BICHO);
        quem.setAttached(BICHO, new Bicho(tinha.id(), quantas));
    }
}
