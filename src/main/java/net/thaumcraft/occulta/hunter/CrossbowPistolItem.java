package net.thaumcraft.occulta.hunter;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A <b>Besta de Mão</b>: o {@code ItemHandBow} do Witchery.
 *
 * <p>Não é um arco. Um arco se puxa e se solta; esta se <b>carrega</b> — e o que ela tem dentro fica lá,
 * esperando, até alguém soltar o gatilho. Por isso ela tem dois gestos, e os dois começam da mesma forma:
 *
 * <ul>
 *   <li><b>De pé, com ela vazia:</b> segurar carrega. O virote entra, a besta range, e a mão fica com ela
 *       pronta.</li>
 *   <li><b>De pé, com ela carregada:</b> segurar e soltar <b>atira</b>. Meio segundo já bota o virote fora;
 *       um segundo inteiro bota-o <b>crítico</b>.</li>
 *   <li><b>Agachado:</b> segurar <b>troca o virote</b> pelo seguinte que houver na mochila, e devolve o que
 *       estava dentro. É assim que se escolhe entre estaca, prata e o resto — sem menu nenhum, com o
 *       polegar.</li>
 * </ul>
 *
 * <p>É o <b>gesto</b> que faz dela o que ela é: quem caça o que a espada não mata precisa de escolher a
 * munição com o bicho em cima, e agachar-se para trocar é exatamente o tempo que isso devia custar.
 *
 * <p><b>Traduções declaradas:</b>
 * <ol>
 *   <li>O original guarda o virote carregado e o <b>preferido</b> no NBT da besta. Aqui são dois componentes,
 *       que é a mesma coisa dita na língua de hoje.</li>
 *   <li>O original dá <b>três</b> estalos enquanto se segura agachado — aos cinco, dez e quinze tiques —, e a
 *       troca acontece ao soltar, depois dos dez. Fica igual.</li>
 *   <li>O <b>Infinito</b> do original tem uma chance de um em quatro de não gastar o virote; aqui o
 *       encantamento equivalente do jogo não existe para bestas, e por isso quem está em <b>criativo</b> é o
 *       único que não gasta — que é o outro caminho do mesmo {@code if}.</li>
 * </ol>
 */
public class CrossbowPistolItem extends Item {
    /** Quanto tempo de aperto é preciso para a troca valer: as dez batidas do original. */
    public static final int PARA_TROCAR = 10;

    /** E a volta inteira, que é o que faz o tiro crítico. */
    public static final int VOLTA_INTEIRA = 20;

    /** Quanto a besta se gasta por tiro. */
    public static final int GASTA = 2;

    /** O dano-base de um virote, e o do que parte. */
    public static final float DANO = 2.0f;
    public static final float DANO_QUE_PARTE = 1.0f;

    /** Quantos saem do que parte, e o leque deles. */
    public static final int QUANTOS_PARTEM = 3;
    public static final float LEQUE = 20.0f;

    /** Os estalos de quem está escolhendo: aos cinco, dez e quinze. */
    private static final int[] ESTALOS = {5, 10, 15};

    public CrossbowPistolItem(Properties properties) {
        super(properties);
    }

    /** A roda de verdade, que só se pode montar depois de os itens existirem. */
    public static List<Item> roda() {
        return List.of(OccultaItems.STAKE_BOLT, OccultaItems.ANTI_MAGIC_BOLT, OccultaItems.HOLY_BOLT,
                OccultaItems.SPLITTING_BOLT, OccultaItems.SILVER_BOLT);
    }

    /** O virote que está dentro, se há algum. */
    @Nullable
    public static Item carregado(ItemStack besta) {
        return besta.get(OccultaComponents.BOLT_LOADED);
    }

    /** E o que a pessoa escolheu da última vez. */
    @Nullable
    public static Item preferido(ItemStack besta) {
        return besta.get(OccultaComponents.BOLT_PREFERRED);
    }

    private static void põe(ItemStack besta, DataComponentType<Item> qual, @Nullable Item virote) {
        if (virote == null) {
            besta.remove(qual);
        } else {
            besta.set(qual, virote);
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack besta) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack besta, LivingEntity quem) {
        return 72000;
    }

    /**
     * Pegar nela: só se segura se houver o que atirar, ou se a pessoa estiver agachada para escolher.
     */
    @Override
    public net.minecraft.world.InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack besta = quem.getItemInHand(mão);
        if (carregado(besta) == null && !quem.isShiftKeyDown()) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        quem.startUsingItem(mão);
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    /** Os três estalos de quem está escolhendo. */
    @Override
    public void onUseTick(Level level, LivingEntity quem, ItemStack besta, int falta) {
        if (!(quem instanceof Player gente) || !gente.isShiftKeyDown()) return;
        int passou = this.getUseDuration(besta, quem) - falta;
        for (int quando : ESTALOS) {
            if (passou != quando) continue;
            if (quantosTipos(gente, carregado(besta)) <= 0) return;
            level.playSound(null, gente.getX(), gente.getY(), gente.getZ(),
                    SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    /** Soltar: ou troca o virote, ou atira. */
    @Override
    public boolean releaseUsing(ItemStack besta, Level level, LivingEntity quem, int falta) {
        if (!(quem instanceof Player gente)) return false;
        int passou = this.getUseDuration(besta, quem) - falta;
        Item dentro = carregado(besta);

        if (dentro == null || (gente.isShiftKeyDown() && passou >= PARA_TROCAR)) {
            if (level.isClientSide()) return false;
            this.escolhe(besta, gente, dentro, passou);
            return false;
        }

        if (level.isClientSide()) return false;
        if (this.atira(besta, level, gente, passou >= VOLTA_INTEIRA ? VOLTA_INTEIRA : VOLTA_INTEIRA - 1)) {
            põe(besta, OccultaComponents.BOLT_LOADED, null);
        }
        return false;
    }

    /**
     * Carregar, ou trocar o que está dentro pelo seguinte que houver.
     *
     * <p>Com a besta <b>vazia</b> ela busca o <b>preferido</b> primeiro — e, não o havendo, o seguinte da roda
     * que exista na mochila. Com ela <b>carregada</b> e a pessoa agachada, anda uma casa na roda e <b>devolve
     * o virote que estava dentro</b>, que é o que faz a troca não custar munição.
     */
    private void escolhe(ItemStack besta, Player gente, @Nullable Item dentro, int passou) {
        int tipos = quantosTipos(gente, dentro);
        if (tipos <= 0) return;

        if (dentro != null) {
            if (!gente.isShiftKeyDown() || passou < PARA_TROCAR) return;
            Item seguinte = this.seguinteQueHá(gente, dentro, dentro);
            if (seguinte == null) return;

            põe(besta, OccultaComponents.BOLT_LOADED, seguinte);
            põe(besta, OccultaComponents.BOLT_PREFERRED, seguinte);
            range(gente);
            if (gente.getAbilities().instabuild) return;

            come(gente, seguinte);
            ItemStack devolve = new ItemStack(dentro);
            if (!gente.getInventory().add(devolve)) gente.drop(devolve, false);
            return;
        }

        Item quer = preferido(besta);
        if (quer == null) quer = OccultaItems.STAKE_BOLT;
        Item usa = tem(gente, quer) ? quer : this.seguinteQueHá(gente, quer, quer);
        if (usa == null) return;

        põe(besta, OccultaComponents.BOLT_LOADED, usa);
        range(gente);
        if (!gente.getAbilities().instabuild) come(gente, usa);
    }

    /** O tiro: o {@code launchBolt}. */
    private boolean atira(ItemStack besta, Level level, Player gente, int batidas) {
        Item virote = carregado(besta);
        if (virote == null) return true;

        float força = batidas / 20.0f;
        força = (força * força + força * 2.0f) / 3.0f;
        if (força < 0.1f) return true;
        if (força > 1.0f) força = 1.0f;

        int tipo = BoltEntity.ESTACA;
        int quantos = 1;
        float começa = 0.0f;
        float passo = 0.0f;
        float dano = DANO;

        if (virote == OccultaItems.SILVER_BOLT) {
            tipo = BoltEntity.PRATA;
        } else if (virote == OccultaItems.HOLY_BOLT) {
            tipo = BoltEntity.SAGRADO;
        } else if (virote == OccultaItems.ANTI_MAGIC_BOLT) {
            if (HunterClothes.vestidoInteiro(gente, false)) {
                tipo = BoltEntity.DRENAGEM_FORTE;
                Drain.derruba(gente, 1.0f);
            } else {
                tipo = BoltEntity.DRENAGEM;
            }
        } else if (virote == OccultaItems.SPLITTING_BOLT) {
            quantos = QUANTOS_PARTEM;
            começa = -LEQUE;
            passo = LEQUE;
            dano = DANO_QUE_PARTE;
        }

        for (int volta = 0; volta < quantos; volta++) {
            BoltEntity saiu = new BoltEntity(level, gente, new ItemStack(virote), besta, tipo);
            saiu.setBaseDamage(dano);
            saiu.pickup = gente.getAbilities().instabuild
                    ? net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.DISALLOWED
                    : net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.ALLOWED;
            if (força == 1.0f) saiu.setCritArrow(true);
            saiu.shootFromRotation(gente, gente.getXRot(), gente.getYRot() + começa + volta * passo,
                    0.0f, força * 2.0f, 1.0f);
            level.addFreshEntity(saiu);
        }

        besta.hurtAndBreak(GASTA, gente, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        level.playSound(null, gente.getX(), gente.getY(), gente.getZ(), SoundEvents.CROSSBOW_SHOOT,
                SoundSource.PLAYERS, 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + força * 0.5f);
        return true;
    }

    /** O seguinte da roda que a pessoa tenha na mochila, dando a volta inteira. */
    @Nullable
    private Item seguinteQueHá(Player gente, Item deOnde, Item pára) {
        List<Item> roda = roda();
        int onde = roda.indexOf(deOnde);
        if (onde < 0) onde = 0;
        for (int passo = 1; passo <= roda.size(); passo++) {
            Item tenta = roda.get((onde + passo) % roda.size());
            if (tenta == pára && passo == roda.size()) break;
            if (tem(gente, tenta)) return tenta;
        }
        return null;
    }

    /** Quantos tipos de virote a pessoa tem, fora o que já está dentro. */
    private static int quantosTipos(Player gente, @Nullable Item dentro) {
        List<Item> achados = new ArrayList<>();
        for (Item qual : roda()) {
            if (qual == dentro) continue;
            if (tem(gente, qual)) achados.add(qual);
        }
        return achados.size();
    }

    private static boolean tem(Player gente, Item qual) {
        for (int casa = 0; casa < gente.getInventory().getContainerSize(); casa++) {
            if (gente.getInventory().getItem(casa).is(qual)) return true;
        }
        return false;
    }

    private static void come(Player gente, Item qual) {
        for (int casa = 0; casa < gente.getInventory().getContainerSize(); casa++) {
            ItemStack oQueTem = gente.getInventory().getItem(casa);
            if (!oQueTem.is(qual)) continue;
            oQueTem.shrink(1);
            return;
        }
    }

    private static void range(Player gente) {
        gente.level().playSound(null, gente.getX(), gente.getY(), gente.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
