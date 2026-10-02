package net.thaumcraft.occulta.familiar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;

import java.util.Optional;
import java.util.UUID;

/**
 * O familiar de alguém: o {@code WITCFamiliar} do Witchery, guardado em quem o tem.
 *
 * <p>Guarda quatro coisas — <b>qual bicho</b>, <b>quem ele é</b>, <b>como se chama</b> e se está <b>chamado</b>.
 * O bicho em si vive no mundo; isto é a ponta do fio que o liga a uma pessoa.
 *
 * @param kind   que bicho é, ou vazio se não há familiar nenhum
 * @param quem   o identificador do bicho no mundo
 * @param nome   o nome que ele ganhou ao ser vinculado
 * @param aoLado se ele está chamado para junto de quem o tem
 */
public record FamiliarData(Optional<FamiliarKind> kind, Optional<UUID> quem,
                           String nome, boolean aoLado) {
    public static final FamiliarData NENHUM =
            new FamiliarData(Optional.empty(), Optional.empty(), "", false);

    public static final Codec<FamiliarData> CODEC = RecordCodecBuilder.create(i -> i.group(
            FamiliarKind.CODEC.optionalFieldOf("kind").forGetter(FamiliarData::kind),
            UUIDUtil.CODEC.optionalFieldOf("quem").forGetter(FamiliarData::quem),
            Codec.STRING.optionalFieldOf("nome", "").forGetter(FamiliarData::nome),
            Codec.BOOL.optionalFieldOf("aoLado", false).forGetter(FamiliarData::aoLado))
            .apply(i, FamiliarData::new));

    /**
     * <b>Ele não vai com quem morre.</b> O original desfaz o vínculo quando a pessoa cai — o familiar fica no
     * mundo, solto —, e por isso isto <b>não</b> leva {@code copyOnDeath}.
     */
    public static final AttachmentType<FamiliarData> DATA = AttachmentRegistry.<FamiliarData>builder()
            .initializer(() -> NENHUM)
            .persistent(CODEC)
            .buildAndRegister(Thaumcraft.id("familiar"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o registro do apego. */
    public static void init() {
    }

    public static FamiliarData of(Player gente) {
        return gente.getAttachedOrCreate(DATA);
    }

    public static void set(Player gente, FamiliarData dado) {
        gente.setAttached(DATA, dado);
    }

    public boolean tem() {
        return this.kind().isPresent() && this.quem().isPresent();
    }
}
