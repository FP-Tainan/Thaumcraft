package net.thaumcraft.occulta.death;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.ghost.TouchOfDeath;

import java.util.function.Consumer;

/**
 * A <b>Mão da Morte</b>: o {@code ItemDeathsHand} do Witchery, que é uma <b>foice</b>.
 *
 * <p>Bater com ela é dar o <b>toque da morte</b>: um décimo da vida máxima de quem apanhar, nunca menos de
 * dois e nunca mais de quinze. Não passa pela armadura porque não é um golpe.
 *
 * <h2>E ela abre</h2>
 *
 * <p>Com as <b>roupas da Morte</b> vestidas, clicar <b>abre a foice</b>. Aberta, ela faz três coisas:
 *
 * <ul>
 *   <li>bate em <b>tudo o que estiver a um bloco e meio</b> de quem apanhou, e não só nele;</li>
 *   <li>tira <b>mais</b> — e quanto mais faminto estiver quem a segura, mais: um décimo e meio com a
 *       barriga cheia, <b>metade</b> com ela vazia;</li>
 *   <li>e <b>devolve</b> fome e vida a quem a segura, pelo mesmo degrau.</li>
 * </ul>
 *
 * <p>É uma troca, e é uma troca honesta: aberta, ela <b>come</b> — dois de fome por segundo, ou um se só
 * restar um. E fecha-se sozinha se as roupas saírem.
 *
 * <p>O original chama o estado dela de {@code WITCScytheDeployed}, e é por isso que o nome dela em código
 * é uma foice e não uma mão.
 */
public class DeathsHandItem extends Item {
    /** O que ela tira fechada: um décimo da vida máxima, pelo menos dois. */
    public static final float FECHADA = 0.1f;
    public static final float FECHADA_NO_MÍNIMO = 2.0f;

    /** E o teto, que é o mesmo de tudo o que a Morte faz. */
    public static final float TETO = 15.0f;

    /** A vida máxima que ela conta, por maior que o bicho seja. */
    public static final float CONTA_ATÉ = 20.0f;

    /** Aberta, ela alcança isto à volta de quem apanhou. */
    public static final double ALCANCE = 1.5;

    /** E come isto por segundo. */
    public static final int COME = 2;

    public DeathsHandItem(Properties propriedades) {
        super(propriedades);
    }

    /** Se ela está aberta. */
    public static boolean aberta(ItemStack foice) {
        return foice.getOrDefault(OccultaComponents.SCYTHE_DEPLOYED, false);
    }

    /** O degrau da fome: o quanto ela tira, o mínimo, e o que devolve. */
    public record Degrau(float quanto, float noMínimo, int fome, int vida) {
    }

    /**
     * <b>O degrau em que a fome de quem a segura a põe.</b>
     *
     * <p>Quanto mais vazia a barriga, mais ela tira e mais devolve — o que faz dela uma arma de quem já
     * não tem nada a perder. Fechada, é sempre o primeiro degrau.
     */
    public static Degrau degrau(int fome) {
        if (fome == 0) return new Degrau(0.5f, 4.0f, 10, 3);
        if (fome <= 4) return new Degrau(0.25f, 4.0f, 3, 2);
        if (fome <= 10) return new Degrau(0.2f, 3.0f, 2, 1);
        if (fome <= 20) return new Degrau(0.15f, 3.0f, 1, 0);
        return new Degrau(0.15f, 3.0f, 0, 0);
    }

    /** Quanto ela tira daquele, naquele degrau. */
    public static float quanto(LivingEntity noquê, Degrau degrau) {
        float vida = Math.min(noquê.getMaxHealth(), CONTA_ATÉ);
        return Math.min(Math.max(vida * degrau.quanto(), degrau.noMínimo()), TETO);
    }

    /**
     * <b>Aberta, ela come.</b> Dois de fome por segundo — ou um, se só restar um —, e fecha-se se as
     * roupas saírem.
     */
    @Override
    public void inventoryTick(ItemStack foice, ServerLevel level, Entity quem, EquipmentSlot casa) {
        if (!aberta(foice) || !(quem instanceof Player gente)) return;
        if (level.getGameTime() % 20L != 0L) return;
        if (!DeathsClothesItem.conjuntoVestido(gente)) {
            foice.set(OccultaComponents.SCYTHE_DEPLOYED, false);
            return;
        }
        var barriga = gente.getFoodData();
        int fome = barriga.getFoodLevel();
        if (fome > 0) barriga.setFoodLevel(Math.max(fome - (fome == 1 ? 1 : COME), 0));
    }

    /** Clicar abre e fecha — e só com as roupas vestidas. */
    @Override
    public InteractionResult use(Level level, Player quem, InteractionHand mão) {
        ItemStack foice = quem.getItemInHand(mão);
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!DeathsClothesItem.conjuntoVestido(quem)) return InteractionResult.PASS;

        boolean abriu = !aberta(foice);
        foice.set(OccultaComponents.SCYTHE_DEPLOYED, abriu);
        if (abriu) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                    quem.getX(), quem.getY() + 1.0, quem.getZ(), 16, 0.5, 1.0, 0.5, 0.0);
            server.playSound(null, quem.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * <b>O golpe.</b> Fechada, toca em quem apanhou; aberta, em tudo o que estiver a um bloco e meio
     * dele — e devolve a quem a segura o que o degrau mandar.
     *
     * <p>É um gancho de fora, como o soco da Mão de Bruxa, e <b>engole o golpe</b>: a foice não bate como
     * arma, ela toca. O original devolve verdadeiro no {@code onLeftClickEntity}, que lá quer dizer o
     * mesmo.
     */
    public static InteractionResult soco(Player quem, Level mundo, InteractionHand mão, Entity noquê,
                                         @org.jetbrains.annotations.Nullable
                                         net.minecraft.world.phys.EntityHitResult onde) {
        ItemStack foice = quem.getItemInHand(mão);
        if (!(foice.getItem() instanceof DeathsHandItem)) return InteractionResult.PASS;
        if (mundo instanceof ServerLevel level && noquê instanceof LivingEntity vivo) {
            bate(level, quem, foice, vivo);
        }
        return InteractionResult.SUCCESS;
    }

    private static void bate(ServerLevel level, Player quem, ItemStack foice, LivingEntity noquê) {
        Degrau degrau = aberta(foice) ? degrau(quem.getFoodData().getFoodLevel())
                : new Degrau(FECHADA, FECHADA_NO_MÍNIMO, 0, 0);
        if (!aberta(foice)) {
            TouchOfDeath.toca(level, quem, noquê, quanto(noquê, degrau));
            return;
        }

        var perto = new AABB(noquê.getX() - ALCANCE, noquê.getBoundingBox().minY, noquê.getZ() - ALCANCE,
                noquê.getX() + ALCANCE, noquê.getBoundingBox().maxY, noquê.getZ() + ALCANCE);
        for (LivingEntity cada : level.getEntitiesOfClass(LivingEntity.class, perto, Entity::isAlive)) {
            if (cada == quem) continue;
            if (!TouchOfDeath.toca(level, quem, cada, quanto(cada, degrau))) continue;
            if (degrau.fome() > 0) {
                var barriga = quem.getFoodData();
                barriga.setFoodLevel(Math.min(barriga.getFoodLevel() + degrau.fome(), 20));
            }
            if (degrau.vida() > 0) quem.heal(degrau.vida());
        }
    }

    @Override
    public void appendHoverText(ItemStack foice, TooltipContext contexto, TooltipDisplay mostra,
                                Consumer<Component> linha, TooltipFlag bandeira) {
        linha.accept(Component.translatable("tc.deathshand.tip").withStyle(ChatFormatting.DARK_GRAY));
        linha.accept(Component.translatable("tc.deathshand.tip2").withStyle(ChatFormatting.DARK_GRAY));
        linha.accept(Component.translatable(aberta(foice) ? "tc.deathshand.open" : "tc.deathshand.shut")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
