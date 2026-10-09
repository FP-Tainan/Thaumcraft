package net.thaumcraft.occulta.charm;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.thaumcraft.mixin.MobAmbientSoundInvoker;
import org.jetbrains.annotations.Nullable;

/**
 * <b>O bicho como vendedor</b>: o {@code AnimalMerchant} do {@code ItemPolynesiaCharm}.
 *
 * <p>Um embrulho, e nada mais: a vaca continua sendo uma vaca, e isto é o que fica entre ela e a tela de
 * trocas enquanto ela estiver aberta. O estoque não vive aqui — vive <b>no bicho</b>, no apego do
 * {@link AnimalShop} —, e por isso este embrulho se faz e se desfaz a cada clique sem que a loja se perca.
 *
 * <p>O <b>cumprimento</b> é o detalhe que o original não precisava de ter e teve: ao abrir a tela, o bicho
 * faz o barulho dele <b>três vezes</b> seguidas. É o que torna claro que a vaca entendeu.
 *
 * <p>E <b>cada troca fechada</b> faz o bicho responder, uma vez. Uma galinha que acaba de vender cinco ovos
 * cacareja.
 */
public class AnimalMerchant implements Merchant {
    /** Quantas vezes o bicho se faz ouvir ao abrir a loja. */
    public static final int CUMPRIMENTO = 3;

    /** A oito blocos a tela se fecha, como a de qualquer mercador. */
    public static final double ALCANCE = 8.0;

    private final LivingEntity bicho;
    private @Nullable Player quemNegocia;
    private @Nullable MerchantOffers loja;

    public AnimalMerchant(LivingEntity bicho) {
        this.bicho = bicho;
    }

    /** De quem é a loja. */
    public LivingEntity bicho() {
        return this.bicho;
    }

    /**
     * Abre a loja para quem clicou: o cumprimento, e depois a tela.
     *
     * <p>O nome da tela é o do bicho, <b>cortado em trinta letras</b> — que é o corte do original, e vale a
     * pena porque um bicho com nome na plaquinha pode ter o nome que quem o batizou quiser.
     */
    public void abre(Player quem) {
        this.cumprimenta();
        this.setTradingPlayer(quem);

        String nome = this.bicho.getDisplayName() == null
                ? "" : this.bicho.getDisplayName().getString();
        this.openTradingScreen(quem,
                Component.literal(nome.substring(0, Math.min(30, nome.length()))), 1);
    }

    /** O barulho do bicho, três vezes: o {@code playGreeting} do original. */
    private void cumprimenta() {
        if (!(this.bicho instanceof Mob qual)) return;
        for (int i = 0; i < CUMPRIMENTO; i++) {
            ((MobAmbientSoundInvoker) qual).thaumcraft$barulho();
        }
    }

    // ------------------------------------------------------------------ o que o mercador do jogo pede

    @Override
    public void setTradingPlayer(@Nullable Player quem) {
        this.quemNegocia = quem;
    }

    @Override
    public @Nullable Player getTradingPlayer() {
        return this.quemNegocia;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.loja == null) this.loja = AnimalShop.loja(this.bicho);
        return this.loja;
    }

    @Override
    public void overrideOffers(MerchantOffers quais) {
        this.loja = quais;
    }

    /**
     * Fechada a troca, ela fica gasta <b>no bicho</b>.
     *
     * <p>É o {@code useRecipe} do original: conta mais um uso e escreve a lista de volta. Sem isto, uma vaca
     * com uma oferta de uma troca seria uma vaca com uma oferta para sempre.
     */
    @Override
    public void notifyTrade(MerchantOffer qual) {
        qual.increaseUses();
        if (this.bicho.isAlive() && !this.bicho.level().isClientSide() && this.loja != null) {
            AnimalShop.guarda(this.bicho, this.loja);
        }
        this.responde();
    }

    @Override
    public void notifyTradeUpdated(ItemStack oquê) {
        this.responde();
    }

    private void responde() {
        if (this.bicho instanceof Mob qual) {
            ((MobAmbientSoundInvoker) qual).thaumcraft$barulho();
        }
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int quanto) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public boolean canRestock() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.bicho.level().isClientSide();
    }

    @Override
    public boolean stillValid(Player quem) {
        return this.quemNegocia == quem && this.bicho.isAlive() && quem.closerThan(this.bicho, ALCANCE);
    }
}
