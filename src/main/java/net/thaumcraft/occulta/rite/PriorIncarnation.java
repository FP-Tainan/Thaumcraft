package net.thaumcraft.occulta.rite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaComponents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A <b>Encarnação Anterior</b>: o {@code RitePriorIncarnation} do Witchery.
 *
 * <p>É o rito que devolve o que se perdeu. Não o que caiu no chão — esse se vai buscar —, mas o que
 * <b>sumiu</b>: as coisas que ficaram largadas onde se morreu até o mundo as apagar.
 *
 * <h2>Como ele sabe o que era seu</h2>
 *
 * <p>Em três passos, e nenhum deles é o rito:
 *
 * <ol>
 *   <li><b>Ao morrer</b>, tudo o que cai fica <b>carimbado</b> com o número de quem morreu, e o lugar da
 *       morte fica guardado;</li>
 *   <li><b>ao sumir</b> — e só ao sumir de velho, não ao arder nem ao cair no vazio —, a coisa carimbada
 *       entra numa <b>lista</b> que quem morreu carrega;</li>
 *   <li><b>ao ser apanhada</b>, o carimbo sai: o que se recuperou com as mãos não se recupera outra vez.</li>
 * </ol>
 *
 * <p>Só então o rito tem o que devolver — e <b>só perto de onde se morreu</b>: dezesseis blocos. Quem morrer
 * no Nether não recupera nada num círculo de casa.
 *
 * <p>E ele deixa um <b>esqueleto com o seu nome</b> em pé no círculo. O original não explica; não precisa.
 */
public final class PriorIncarnation {
    /** A que distância do círculo a pessoa tem de estar, e a que distância dele ela morreu. */
    public static final int PERTO = 5;
    public static final int DA_MORTE = 16;

    /** Quanto tempo uma coisa largada dura antes de sumir: os cinco minutos do jogo. */
    public static final int SOME_AOS = 6000;

    /**
     * O que uma pessoa tem para recuperar, e onde ela o perdeu.
     *
     * <p>Guarda-se o <b>mundo</b> junto com o lugar, que o original não guarda — ele compara só as três
     * contas, de modo que morrer no Nether e fazer o rito nas mesmas contas do Mundo devolveria tudo. Aqui
     * não devolve. <b>Declarado.</b>
     */
    public record Perdido(List<ItemStack> coisas, Optional<GlobalPos> onde) {
        public static final Perdido NADA = new Perdido(List.of(), Optional.empty());

        public static final Codec<Perdido> CODEC = RecordCodecBuilder.create(campo -> campo.group(
                ItemStack.CODEC.listOf().fieldOf("coisas").forGetter(Perdido::coisas),
                GlobalPos.CODEC.optionalFieldOf("onde").forGetter(Perdido::onde)
        ).apply(campo, Perdido::new));
    }

    public static final AttachmentType<Perdido> DATA = AttachmentRegistry.<Perdido>builder()
            .initializer(() -> Perdido.NADA)
            .persistent(Perdido.CODEC)
            .copyOnDeath()
            .buildAndRegister(Thaumcraft.id("prior_incarnation"));

    private PriorIncarnation() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o registro do apego. */
    public static void init() {
    }

    public static Perdido de(Player quem) {
        return quem.getAttachedOrCreate(DATA);
    }

    // ------------------------------------------------------------------ os três passos

    /**
     * <b>Ao morrer</b>: esquece o que havia e guarda o lugar.
     *
     * <p>Uma morte apaga a lista da anterior — é do original, e é o que impede que se junte o espólio de
     * dez mortes para um rito só.
     *
     * @return se há o que carimbar: com o {@code keepInventory} ligado, não cai nada e não há rito
     */
    public static boolean morreu(Player quem) {
        if (!(quem.level() instanceof ServerLevel level)) return false;
        if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) return false;
        quem.setAttached(DATA, new Perdido(List.of(),
                Optional.of(GlobalPos.of(level.dimension(), quem.blockPosition()))));
        return true;
    }

    /** E carimba o que cai com o número de quem caiu. */
    public static void carimba(ItemStack oquê, Player quem) {
        if (oquê.isEmpty()) return;
        oquê.set(OccultaComponents.LOST_BY, quem.getUUID());
    }

    /** <b>Ao ser apanhada</b>, a coisa deixa de ser de um morto. */
    public static void apanhada(ItemStack oquê) {
        oquê.remove(OccultaComponents.LOST_BY);
    }

    /**
     * <b>Ao sumir de velha</b>, ela entra na lista de quem a perdeu.
     *
     * <p>O carimbo sai dela antes de entrar: o que o rito devolve é uma coisa limpa, que se pode perder
     * outra vez.
     */
    public static void sumiu(ServerLevel level, ItemEntity largada) {
        ItemStack oquê = largada.getItem();
        UUID dequem = oquê.get(OccultaComponents.LOST_BY);
        if (dequem == null) return;
        Player dono = quem(level, dequem);
        if (dono == null) return;

        oquê.remove(OccultaComponents.LOST_BY);
        Perdido tinha = de(dono);
        List<ItemStack> agora = new ArrayList<>(tinha.coisas());
        agora.add(oquê.copy());
        dono.setAttached(DATA, new Perdido(List.copyOf(agora), tinha.onde()));
    }

    /**
     * A pessoa daquele número.
     *
     * <p>Pergunta-se primeiro à <b>lista do servidor</b>, que conhece todos os mundos — uma coisa pode
     * sumir no Nether enquanto o dono está no Mundo. Não estando lá, pergunta-se ao <b>nível</b>: num mundo
     * de prova há gente carregada que a lista do servidor não conhece.
     */
    private static @Nullable Player quem(ServerLevel level, UUID qual) {
        Player naLista = level.getServer().getPlayerList().getPlayer(qual);
        return naLista != null ? naLista : level.getPlayerByUUID(qual);
    }

    // ------------------------------------------------------------------ e o rito

    /**
     * Devolve a quem estiver perto do círculo o que ele perdeu — se o perdeu <b>perto dali</b>.
     *
     * @return se devolveu alguma coisa a alguém
     */
    public static boolean devolve(ServerLevel level, BlockPos meio) {
        boolean deu = false;
        AABB caixa = new AABB(meio.getX() - PERTO, meio.getY(), meio.getZ() - PERTO,
                meio.getX() + PERTO, meio.getY() + 1, meio.getZ() + PERTO);
        for (Player quem : level.getEntitiesOfClass(Player.class, caixa)) {
            if (quem.distanceToSqr(meio.getX(), meio.getY(), meio.getZ()) > (double) PERTO * PERTO) {
                continue;
            }
            if (devolveA(level, meio, quem)) deu = true;
        }
        return deu;
    }

    private static boolean devolveA(ServerLevel level, BlockPos meio, Player quem) {
        Perdido tinha = de(quem);
        if (tinha.coisas().isEmpty() || tinha.onde().isEmpty()) return false;

        GlobalPos onde = tinha.onde().get();
        if (!onde.dimension().equals(level.dimension())) return false;
        if (onde.pos().distSqr(meio) > (double) DA_MORTE * DA_MORTE) return false;

        for (ItemStack coisa : tinha.coisas()) {
            Block.popResource(level, meio, coisa.copy());
        }
        osso(level, meio, quem);
        quem.setAttached(DATA, Perdido.NADA);
        return true;
    }

    /** <b>E o esqueleto com o seu nome</b>, que é o original sem explicação nenhuma. */
    private static void osso(ServerLevel level, BlockPos meio, Player quem) {
        var esqueleto = net.minecraft.world.entity.EntityTypes.SKELETON.create(level,
                EntitySpawnReason.TRIGGERED);
        if (esqueleto == null) return;
        esqueleto.snapTo(meio.getX() + 0.5, meio.getY(), meio.getZ() + 0.5, 0.0f, 0.0f);
        esqueleto.setCustomName(quem.getName().copy());
        level.addFreshEntity(esqueleto);
    }

    /** O estouro de quem conseguiu, e a fumaça de quem não tinha nada. */
    public static void mostra(ServerLevel level, BlockPos meio, boolean deu) {
        if (deu) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, meio.getX() + 0.5, meio.getY() + 1.0,
                    meio.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, meio, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }
        level.sendParticles(ParticleTypes.SMOKE, meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ() + 0.5,
                16, 1.0, 2.0, 1.0, 0.0);
        level.playSound(null, meio, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /**
     * O passo do rito: espera um segundo e devolve.
     *
     * <p>Com o <b>{@code keepInventory} ligado</b> ele desiste e devolve o que se ofereceu — não há nada
     * que recuperar num mundo em que não se perde nada.
     */
    public record Passo() implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
                Player quem = rito.starter(level);
                if (quem != null) {
                    quem.sendSystemMessage(net.minecraft.network.chat.Component
                            .translatable("tc.rite.disabled")
                            .withStyle(net.minecraft.ChatFormatting.RED));
                }
                return Result.ABORTED_REFUND;
            }
            if (ticks % 20L != 0L) return Result.STARTING;
            mostra(level, onde, devolve(level, onde));
            return Result.COMPLETED;
        }
    }

    /** E o rito em si, que é um passo só. */
    public record Rito() implements Rite {
        @Override
        public List<RiteStep> steps(int coven) {
            return List.of(new Passo());
        }
    }

    /** O que uma coisa largada traz carimbado, para quem o quiser ver. */
    public static @Nullable UUID dequem(ItemStack oquê) {
        return oquê.get(OccultaComponents.LOST_BY);
    }
}
