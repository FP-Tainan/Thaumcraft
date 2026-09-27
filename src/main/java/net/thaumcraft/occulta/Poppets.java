package net.thaumcraft.occulta;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Onde as bonecas valem: o {@code PoppetEventHooks} e o {@code GenericEvents} do Witchery.
 *
 * <p>Uma boneca só entra em cena quando a pancada <b>mataria</b>: aí ela toma o golpe no lugar de quem ela
 * guarda, e desfaz-se. A de fome é a única que também vale antes do fim, quando restam dois corações.
 *
 * <p>Procura-se a boneca na mochila de quem apanhou e, não a achando, em <b>todas as prateleiras de bonecas</b>
 * do mundo — que é o que o original faz.
 */
public final class Poppets {
    /** As prateleiras que estão de pé, para não varrer o mundo inteiro atrás delas. */
    private static final List<PoppetShelfBlockEntity> SHELVES = new ArrayList<>();

    private Poppets() {
    }

    public static void register(PoppetShelfBlockEntity prateleira) {
        if (!SHELVES.contains(prateleira)) SHELVES.add(prateleira);
    }

    public static void remove(PoppetShelfBlockEntity prateleira) {
        SHELVES.remove(prateleira);
    }

    public static int shelves() {
        SHELVES.removeIf(BlockEntity::isRemoved);
        return SHELVES.size();
    }

    /** De quantas em quantas batidas se olha para a ferramenta e para a armadura gastas. */
    public static final int CHECK_EVERY = 20;

    /** A partir de que gasto a boneca conserta: nove décimos, como no original. */
    public static final float WORN = 0.9f;

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((quem, fonte, quanto) -> {
            if (!(quem instanceof Player gente) || !(quem.level() instanceof ServerLevel level)) return true;
            return !guard(level, gente, fonte, quanto);
        });

        // a da ferramenta e a da armadura consertam o que está quase a partir
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_EVERY != 3) return;
            for (ServerLevel level : server.getAllLevels()) {
                for (Player gente : level.players()) mend(level, gente);
            }
        });
    }

    /**
     * O {@code checkForArmorProtection} e o gancho da ferramenta: o que estiver gasto a nove décimos volta a
     * novo, e a boneca gasta-se um pouco.
     *
     * <p><b>Desvio declarado:</b> no original a ferramenta se conserta no momento em que se usa; aqui é de
     * segundo em segundo, no mesmo lugar em que a armadura se olha. O que se vê é o mesmo.
     */
    public static void mend(ServerLevel level, Player quem) {
        mendOne(level, quem, quem.getMainHandItem(), PoppetItem.Kind.TOOL);
        for (var casa : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (casa.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            mendOne(level, quem, quem.getItemBySlot(casa), PoppetItem.Kind.ARMOR);
        }
    }

    private static void mendOne(ServerLevel level, Player quem, ItemStack coisa, PoppetItem.Kind qual) {
        if (coisa.isEmpty() || !coisa.isDamageableItem()) return;
        if (coisa.getDamageValue() < (int) (coisa.getMaxDamage() * WORN)) return;
        if (!spend(level, quem, qual)) return;
        coisa.setDamageValue(0);
        level.playSound(null, quem.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                0.5f, 1.0f);
    }

    /**
     * A boneca que responde por esta pancada, se houver: devolve verdadeiro se ela a tomou.
     *
     * <p>A conta é a do original: só quando o que sobra de vida não dá para aguentar. Cada feitio de morte tem a
     * sua boneca, e a da <b>morte</b> vale para todas as outras.
     */
    public static boolean guard(ServerLevel level, Player quem, DamageSource fonte, float quanto) {
        float sobra = quem.getHealth() - quanto;
        boolean mataria = sobra <= 0.0f;
        boolean quaseFome = sobra <= 2.0f && fonte.is(DamageTypes.STARVE);
        if (!mataria && !quaseFome) return false;

        PoppetItem.Kind qual = kindFor(fonte);
        if (qual != null && spend(level, quem, qual)) {
            comfort(quem, qual);
            return true;
        }
        if (!mataria) return false;
        if (!spend(level, quem, PoppetItem.Kind.DEATH)) return false;
        comfort(quem, kindFor(fonte));
        return true;
    }

    /** Qual boneca guarda de qual morte. */
    @Nullable
    private static PoppetItem.Kind kindFor(DamageSource fonte) {
        if (fonte.is(DamageTypes.FALL) || fonte.is(DamageTypes.STALAGMITE)) return PoppetItem.Kind.EARTH;
        if (fonte.is(DamageTypeTags.IS_FIRE) || fonte.is(DamageTypeTags.IS_EXPLOSION)) {
            return PoppetItem.Kind.FIRE;
        }
        if (fonte.is(DamageTypes.DROWN)) return PoppetItem.Kind.WATER;
        if (fonte.is(DamageTypes.STARVE)) return PoppetItem.Kind.HUNGER;
        return null;
    }

    /** O alívio que fica depois de a boneca tomar o golpe. */
    private static void comfort(Player quem, @Nullable PoppetItem.Kind qual) {
        if (qual == null) return;
        switch (qual) {
            case FIRE -> {
                quem.clearFire();
                quem.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0));
            }
            case WATER -> quem.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 120, 0));
            case HUNGER -> quem.addEffect(new MobEffectInstance(MobEffects.SATURATION, 120, 0));
            default -> {
            }
        }
    }

    /**
     * Gasta a boneca daquele feitio presa àquela pessoa, onde quer que ela esteja.
     *
     * <p>Primeiro na mochila de quem apanhou; depois nas prateleiras. As de proteção desfazem-se; as outras
     * gastam-se de mil em mil, que é o que o original lhes tira de cada vez.
     */
    public static boolean spend(ServerLevel level, Player quem, PoppetItem.Kind qual) {
        if (spendIn(level, quem, qual, quem.getInventory())) return true;
        SHELVES.removeIf(BlockEntity::isRemoved);
        for (PoppetShelfBlockEntity prateleira : List.copyOf(SHELVES)) {
            if (prateleira.getLevel() == null) continue;
            if (spendIn(level, quem, qual, prateleira)) return true;
        }
        return false;
    }

    private static boolean spendIn(ServerLevel level, Player quem, PoppetItem.Kind qual, Container onde) {
        for (int i = 0; i < onde.getContainerSize(); i++) {
            ItemStack stack = onde.getItem(i);
            if (!(stack.getItem() instanceof PoppetItem boneca) || boneca.kind() != qual) continue;
            if (!TaglockItem.isFor(stack, quem)) continue;

            level.playSound(null, quem.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                    quem.getX(), quem.getY() + 1.0, quem.getZ(), 24, 0.4, 0.6, 0.4, 0.0);
            final int casa = i;
            if (qual.breaks) {
                onde.setItem(casa, ItemStack.EMPTY);
            } else {
                stack.hurtAndBreak(1000, level, null, quebrou -> onde.setItem(casa, ItemStack.EMPTY));
            }
            onde.setChanged();
            return true;
        }
        return false;
    }

    /** Onde uma prateleira está, para quem precisar contar. */
    public static List<BlockPos> shelfPositions() {
        List<BlockPos> onde = new ArrayList<>();
        for (PoppetShelfBlockEntity prateleira : SHELVES) onde.add(prateleira.getBlockPos());
        return onde;
    }
}
