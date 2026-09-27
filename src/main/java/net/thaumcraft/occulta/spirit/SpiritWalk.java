package net.thaumcraft.occulta.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

import java.util.List;
import java.util.Optional;

/**
 * O que fica guardado em quem anda em espírito: o {@code WITCSpiritWorld} do {@code Infusion} do original.
 *
 * <p>Andar em espírito não é viajar: é <b>deitar-se</b>. O corpo fica onde estava e o espírito levanta-se — e por
 * isso quem anda tem <b>duas de cada coisa</b>: dois inventários, duas vidas, duas fomes. Esta é a metade que
 * fica dormindo enquanto a outra anda.
 *
 * @param walking   se o espírito está fora do corpo
 * @param nightmare se o sonho é dos maus
 * @param demonic   e se é dos piores
 * @param body      onde o corpo ficou
 * @param inventory o inventário do outro lado, guardado em cru
 * @param health    a vida do outro lado
 * @param food      a fome do outro lado
 * @param saturation e a gordura dela
 */
public record SpiritWalk(boolean walking, boolean nightmare, boolean demonic, Optional<BlockPos> body,
                         List<ItemStack> inventory, float health, int food, float saturation) {
    public static final SpiritWalk AWAKE =
            new SpiritWalk(false, false, false, Optional.empty(), List.of(), 0.0f, 0, 0.0f);

    public static final Codec<SpiritWalk> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("walking", false).forGetter(SpiritWalk::walking),
            Codec.BOOL.optionalFieldOf("nightmare", false).forGetter(SpiritWalk::nightmare),
            Codec.BOOL.optionalFieldOf("demonic", false).forGetter(SpiritWalk::demonic),
            BlockPos.CODEC.optionalFieldOf("body").forGetter(SpiritWalk::body),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("inventory", List.of()).forGetter(SpiritWalk::inventory),
            Codec.FLOAT.optionalFieldOf("health", 0.0f).forGetter(SpiritWalk::health),
            Codec.INT.optionalFieldOf("food", 0).forGetter(SpiritWalk::food),
            Codec.FLOAT.optionalFieldOf("saturation", 0.0f).forGetter(SpiritWalk::saturation))
            .apply(i, SpiritWalk::new));

    public static final AttachmentType<SpiritWalk> DATA = AttachmentRegistry.<SpiritWalk>builder()
            .initializer(() -> AWAKE)
            .persistent(CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("spirit_walk"));

    public static SpiritWalk of(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    public static void set(Player quem, SpiritWalk agora) {
        quem.setAttached(DATA, agora);
    }

    /** Se aquela pessoa está com o espírito fora do corpo. */
    public static boolean walking(Player quem) {
        return of(quem).walking();
    }

    /** E se o sonho dela é dos maus. */
    public static boolean nightmare(Player quem) {
        return of(quem).nightmare();
    }

    public SpiritWalk withWalking(boolean walking) {
        return new SpiritWalk(walking, this.nightmare, this.demonic, this.body, this.inventory,
                this.health, this.food, this.saturation);
    }

    public SpiritWalk withDream(boolean nightmare, boolean demonic) {
        return new SpiritWalk(this.walking, nightmare, demonic, this.body, this.inventory,
                this.health, this.food, this.saturation);
    }

    public SpiritWalk withBody(BlockPos onde) {
        return new SpiritWalk(this.walking, this.nightmare, this.demonic, Optional.of(onde), this.inventory,
                this.health, this.food, this.saturation);
    }

    /** Guarda a metade que fica: o que ela tinha na mochila, a vida e a fome. */
    public SpiritWalk withOther(List<ItemStack> inventory, float health, int food, float saturation) {
        return new SpiritWalk(this.walking, this.nightmare, this.demonic, this.body, inventory,
                health, food, saturation);
    }
}
