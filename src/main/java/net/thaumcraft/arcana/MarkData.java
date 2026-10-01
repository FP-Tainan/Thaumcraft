package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;

/**
 * O lugar que um arcanista marcou: o {@code markLocation} do {@code ExtendedProperties} do Ars Magica 2.
 *
 * <p>Duas essências vivem disto e não valem nada uma sem a outra. A <b>Marca</b> guarda onde se está; o
 * <b>Chamado</b> traz de volta a esse lugar. É o par mais antigo da magia de qualquer jogo, e o Ars Magica 2
 * põe-lhe duas regras que valem ser ditas:
 *
 * <ol>
 *   <li><b>Marca-se um lugar só.</b> Marcar outra vez apaga o anterior — não há lista de marcas.</li>
 *   <li><b>O Chamado não atravessa mundos.</b> Uma marca feita na superfície não traz ninguém do Nether: o
 *       original recusa e diz porquê.</li>
 * </ol>
 *
 * @param onde  o lugar marcado
 * @param mundo em que mundo ele fica
 */
public record MarkData(Vec3 onde, ResourceKey<Level> mundo) {
    /** Quem nunca marcou nada: o {@code markSet} do original a falso. */
    public static final MarkData NONE = new MarkData(Vec3.ZERO, Level.OVERWORLD);

    public static final Codec<MarkData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3.CODEC.optionalFieldOf("onde", Vec3.ZERO).forGetter(MarkData::onde),
            ResourceKey.codec(Registries.DIMENSION)
                    .optionalFieldOf("mundo", Level.OVERWORLD).forGetter(MarkData::mundo))
            .apply(i, MarkData::new));

    /** O mesmo, para ir pela rede: o livro e a tela podem querer dizer onde a marca está. */
    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, MarkData> STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final AttachmentType<MarkData> DATA = AttachmentRegistry.<MarkData>builder()
            .initializer(() -> NONE)
            .persistent(CODEC)
            .syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("mark"));

    public static MarkData of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static void set(Player quem, MarkData agora) {
        quem.setAttached(DATA, agora);
    }

    /** Se há alguma marca posta: o {@code getMarkSet}. */
    public boolean posta() {
        return !this.onde.equals(Vec3.ZERO);
    }

    /**
     * Sem uso fora do porte: obriga a classe a carregar, e com ela o anexo a se registrar.
     *
     * <p>Como a mana e as afinidades, e pela mesma razão: um anexo registrado tarde não chega à máquina de
     * quem joga.
     */
    public static void init() {
    }
}
