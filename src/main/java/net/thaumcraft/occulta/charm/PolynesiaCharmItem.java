package net.thaumcraft.occulta.charm;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.NoDrops;
import net.thaumcraft.occulta.clothes.WitchClothes;
import net.thaumcraft.occulta.coven.CovenWitchEntity;
import net.thaumcraft.occulta.familiar.Familiars;
import net.thaumcraft.occulta.imp.ImpEntity;
import net.thaumcraft.occulta.infusion.OverworldInfusion;

import java.util.function.Consumer;

/**
 * O <b>Amuleto da Polinésia</b> e a <b>Língua do Diabo</b>: o {@code ItemPolynesiaCharm} do Witchery, a mesma
 * classe duas vezes.
 *
 * <p>É a ideia mais divertida que o ramo tem no equipamento: <b>o bicho vira vendedor</b>. Clica-se num bicho
 * a cinco blocos e abre-se uma tela de trocas com ele, com o que ele por acaso tenha — e ele guarda essa loja
 * para sempre. Veja o {@link AnimalShop}.
 *
 * <h2>Em quem pega</h2>
 *
 * <p>Em <b>bicho de criação</b>, em <b>bicho do ar</b>, em <b>aranha</b> e em <b>bicho de água</b>, sempre. Em
 * <b>creeper</b>, só trazendo o <b>Manto de Bruxa</b> — e é a única coisa no ramo que faz um creeper valer a
 * pena vivo. Em <b>morto-vivo</b>, só trazendo o <b>Manto de Necromante</b>.
 *
 * <p>E não pega em <b>familiar</b>, em <b>bruxa de coven</b> nem em <b>diabrete</b>; nem em <b>filhote</b>; nem
 * em bicho que <b>já tenha alvo</b>, porque um bicho com alvo tem o que fazer. Nem em morcego que <b>não
 * largue nada</b>, que é o morcego que já esteve num Apanha-Bicho.
 *
 * <h2>As duas caras</h2>
 *
 * <p>São o mesmo amuleto, de <b>cinquenta usos</b> cada, e gasta um por loja aberta. O que a <b>Língua do
 * Diabo</b> tem a mais não está aqui: está no <b>Demônio</b>, que desconta o preço a quem a traz na mão,
 * cobra-lhe cinco usos por troca e <b>quase deixa de lhe atirar bolas de fogo</b>. Veja o {@code DemonEntity}.
 *
 * <h2>Desvios declarados</h2>
 *
 * <p><b>A lula entra pelos dois lados.</b> Na 1.7.10 lulas e peixes eram da mesma classe, e o original pede
 * essa; hoje a lula vai por um lado e o peixe por outro, de modo que a pergunta se faz às duas. Sem isto a
 * lula não teria loja — e ela tem uma, que vende <b>sacos de tinta</b>.
 *
 * <p><b>O vínculo exclui os três familiares, e não só o gato.</b> No original o gato ligado vira um bicho
 * de outra classe, e é essa classe que o amuleto recusa — a coruja e o sapo ligados continuam a ser coruja
 * e sapo, e passam por descuido. Aqui o vínculo mora em quem o tem e não no bicho, de modo que a pergunta é
 * pelo vínculo: os três ficam de fora. É o que o original quis dizer, escrito de um jeito que o diz.
 *
 * <p>E no original o amuleto tem um ramo para o Demônio que <b>nunca corre</b>: ele só abriria loja a quem
 * estivesse na lista acima, e o Demônio não é bicho de criação, nem do ar, nem aranha, nem de água, nem
 * morto-vivo. Quem negocia com um demônio é o próprio demônio, clicando nele sem amuleto nenhum. O ramo não
 * veio, e o que ele guardava — <b>a loja de graça</b> — não existe.
 */
public class PolynesiaCharmItem extends Item {
    /** A cinco blocos: o {@code MAX_TARGET_RANGE} do original. */
    public static final double ALCANCE = 5.0;

    /** Cinquenta lojas. */
    public static final int USOS = 50;

    /** O que cada loja aberta gasta. */
    public static final int CUSTA = 1;

    private final boolean encantaDemônios;
    private final String dica;

    public PolynesiaCharmItem(boolean encantaDemônios, String dica, Properties properties) {
        super(properties);
        this.encantaDemônios = encantaDemônios;
        this.dica = dica;
    }

    /** Se este é a <b>Língua do Diabo</b>. */
    public boolean encantaDemônios() {
        return this.encantaDemônios;
    }

    /** Se o que está nesta mão é a Língua do Diabo. */
    public static boolean éLíngua(ItemStack oquê) {
        return oquê.getItem() instanceof PolynesiaCharmItem amuleto && amuleto.encantaDemônios();
    }

    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack amuleto = quem.getItemInHand(mão);
        if (!(level instanceof ServerLevel mundo) || !(quem instanceof ServerPlayer gente)) {
            return InteractionResult.SUCCESS;
        }

        LivingEntity bicho = oQueEstáOlhando(mundo, gente);
        if (bicho == null || !pega(bicho, quem)) {
            level.playSound(null, quem.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.PLAYERS, 0.5f, 1.0f);
            return InteractionResult.CONSUME;
        }

        new AnimalMerchant(bicho).abre(quem);
        amuleto.hurtAndBreak(CUSTA, gente, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        return InteractionResult.SUCCESS;
    }

    /** O bicho que ele está olhando, a cinco blocos — e nada se o que está lá é um bloco. */
    private static @org.jetbrains.annotations.Nullable LivingEntity oQueEstáOlhando(
            ServerLevel level, ServerPlayer quem) {
        HitResult onde = OverworldInfusion.olha(level, quem, ALCANCE);
        if (onde instanceof EntityHitResult bateu && bateu.getEntity() instanceof LivingEntity vivo) {
            return vivo;
        }
        return null;
    }

    /**
     * <b>Se o amuleto pega neste bicho</b>: a lista do original, na ordem dele.
     *
     * <p>O manto é a chave dos dois casos que não são de criação: o de bruxa abre o creeper, o de necromante
     * abre o morto-vivo. Sem o manto, nenhum dos dois ouve.
     */
    public static boolean pega(LivingEntity bicho, Player quem) {
        boolean dalista = bicho instanceof Animal
                || bicho instanceof AmbientCreature
                || bicho instanceof Spider
                || bicho instanceof WaterAnimal
                || bicho instanceof net.minecraft.world.entity.animal.AgeableWaterCreature
                || (bicho instanceof Creeper && WitchClothes.wearingRobes(quem))
                || (AnimalShop.mortoVivo(bicho) && WitchClothes.wearingNecroRobes(quem));
        if (!dalista) return false;

        if (bicho instanceof CovenWitchEntity || bicho instanceof ImpEntity) return false;
        if (!bicho.isAlive() || bicho.isBaby()) return false;
        if (bicho instanceof Mob qual && qual.getTarget() != null) return false;
        if (bicho instanceof Bat && NoDrops.marcado(bicho)) return false;

        return !(bicho.level() instanceof ServerLevel mundo) || !Familiars.éDeAlguém(mundo, bicho);
    }

    @Override
    public void appendHoverText(ItemStack amuleto, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha,
                                net.minecraft.world.item.TooltipFlag bandeira) {
        for (String parte : this.dica.split(";")) {
            linha.accept(Component.translatable(parte).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /** O que as duas caras levam de propriedades: cinquenta usos, uma por casa, e o tom de incomum. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1)
                .durability(USOS)
                .rarity(net.minecraft.world.item.Rarity.UNCOMMON);
    }
}
