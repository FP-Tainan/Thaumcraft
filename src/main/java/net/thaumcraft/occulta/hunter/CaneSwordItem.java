package net.thaumcraft.occulta.hunter;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.vampire.BloodReserve;

import java.util.function.Consumer;

/**
 * A <b>Bengala-Espada</b>: o {@code ItemCaneSword} do Witchery.
 *
 * <p>Uma bengala com uma <b>espada de diamante dentro</b>. Guardada, ela faz <b>dois</b> de dano — é um
 * pau; <b>sacada</b>, faz os sete de uma espada de diamante inteira. Agachar e clicar saca ou guarda, com
 * um som para cada.
 *
 * <p>É a única arma do ramo que <b>escolhe</b> ser arma. Quem anda com ela guardada anda desarmado de
 * propósito, e quem a saca o diz a quem está à frente.
 *
 * <h2>O cantil</h2>
 *
 * <p>Guardada, clicando sem agachar, ela <b>bebe a reserva de sangue</b> de quem a traz — e só se quem a
 * traz for <b>vampiro</b>, tiver o cantil cheio e estiver com fome. Veja o {@link BloodReserve}: o cantil
 * enche-se sozinho a cada aldeão, guarda ou pessoa que morre pela mão de quem o tem, e a bengala é a única
 * coisa que o sabe abrir.
 *
 * <p>A dica dela diz quanto há lá dentro, e é por isso que ela é a única coisa no ramo com um número que
 * muda na mão.
 *
 * <h2>Desvio declarado</h2>
 *
 * <p>O original escreve o dano num {@code getAttributeModifiers} que lê o NBT da pilha a cada pergunta.
 * Hoje os modificadores de uma pilha são um <b>componente</b>, de modo que sacar e guardar <b>escrevem</b>
 * o componente novo na pilha. Dá no mesmo na mão de quem a usa, e tem uma vantagem que o original não
 * tinha: a dica do item mostra o dano certo sem que ninguém lhe peça.
 */
public class CaneSwordItem extends Item {
    /**
     * O dano da lâmina <b>sacada</b>: o que uma espada de diamante soma, que é o que o original lhe dá.
     *
     * <p>O original escreve {@code 4 + o dano do material}, que na 1.7.10 era a conta de toda espada; hoje
     * a mesma espada soma seis, e sete é o que quem bate vê.
     */
    public static final double SACADA = 6.0;

    /** E o da bengala <b>guardada</b>, que é um pau: um a somar, dois no total. */
    public static final double GUARDADA = 1.0;

    /**
     * A velocidade do golpe: a de uma espada.
     *
     * <p><b>Declarado:</b> o original não a escreve porque o jogo de 2014 não a tinha. Sem ela a bengala
     * bateria na velocidade de mão vazia, que é quase o dobro da de uma espada — e isso faria dela, sacada,
     * a melhor arma do mod por acidente.
     */
    public static final double VELOCIDADE = -2.4;

    public CaneSwordItem(Properties properties) {
        super(properties);
    }

    /** Se a lâmina está de fora: o {@code WITCBladeDeployed} do original. */
    public static boolean sacada(ItemStack bengala) {
        return Boolean.TRUE.equals(bengala.get(OccultaComponents.BLADE_DRAWN));
    }

    /** Saca ou guarda, e com isso muda o dano que ela faz. */
    public static void saca(ItemStack bengala, boolean fora) {
        bengala.set(OccultaComponents.BLADE_DRAWN, fora);
        bengala.set(DataComponents.ATTRIBUTE_MODIFIERS, modificadores(fora));
    }

    /** O dano de cada feitio, escrito como o jogo de hoje o escreve. */
    public static ItemAttributeModifiers modificadores(boolean fora) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Thaumcraft.id("cane_sword_damage"),
                                fora ? SACADA : GUARDADA, AttributeModifier.Operation.ADD_VALUE),
                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Thaumcraft.id("cane_sword_speed"),
                                VELOCIDADE, AttributeModifier.Operation.ADD_VALUE),
                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .build();
    }

    /**
     * <b>Agachado, saca ou guarda. De pé e guardada, bebe o cantil.</b>
     *
     * <p>De pé e sacada, não faz nada: uma espada sacada na mão de quem clica no ar é uma espada, e o
     * original a deixa em paz.
     */
    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack bengala = quem.getItemInHand(mão);
        if (!(level instanceof ServerLevel mundo)) return InteractionResult.SUCCESS;

        boolean fora = sacada(bengala);
        if (quem.isShiftKeyDown()) {
            saca(bengala, !fora);
            mundo.playSound(null, quem.blockPosition(),
                    (fora ? OccultaSounds.SWORD_SHEATHE : OccultaSounds.SWORD_DRAW).value(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (fora) return InteractionResult.PASS;

        if (BloodReserve.bebe(quem)) {
            mundo.sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE,
                    quem.getX(), quem.getY() + quem.getBbHeight() * 0.85, quem.getZ(),
                    16, 0.5, 0.5, 0.5, 0.0);
            mundo.playSound(null, quem.blockPosition(), OccultaSounds.DRINK.value(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        mundo.playSound(null, quem.blockPosition(),
                net.minecraft.sounds.SoundEvents.NOTE_BLOCK_SNARE.value(),
                SoundSource.PLAYERS, 0.5f, 1.0f);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack bengala, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, net.minecraft.world.item.TooltipFlag bandeira) {
        linha.accept(Component.translatable("tc.canesword.tip").withStyle(ChatFormatting.DARK_GRAY));
        linha.accept(Component.translatable("tc.canesword.tip2").withStyle(ChatFormatting.DARK_GRAY));
        linha.accept(Component.translatable("tc.canesword.tip3").withStyle(ChatFormatting.DARK_GRAY));
        linha.accept(Component.translatable("tc.canesword.reserve", BloodReserve.naTela.getAsInt())
                .withStyle(ChatFormatting.DARK_RED));
    }
}
