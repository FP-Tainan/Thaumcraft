package net.thaumcraft.occulta.waystone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.PowerSources;
import net.thaumcraft.occulta.TaglockItem;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A <b>Pedra de Caminho</b>: o {@code EntityItemWaystone} do Witchery, com os três itens dele.
 *
 * <p>Uma pedra solta não é nada. O que faz dela uma Pedra de Caminho é <b>largá-la no chão</b>, e o resto é
 * geometria de giz:
 *
 * <ul>
 *   <li>Uma <b>pedra lisa</b> largada no meio de um <b>anel miúdo</b> — as oito casas em volta, riscadas com
 *       giz do Alhures — pega o lugar onde está e vira <b>pedra presa a um lugar</b>. Até oito de uma vez, e os
 *       oito glifos <b>estouram</b>: o anel se gasta.</li>
 *   <li>Mas se houver <b>alguém de pé dentro do anel</b>, a até dois blocos do meio, ela prende-se <b>a essa
 *       pessoa</b> em vez do lugar — e isso custa <b>quatro mil de poder</b> ao altar mais perto. É só uma, e
 *       sem altar ou sem poder não se faz: sai fumo e a pedra fica lisa.</li>
 *   <li>Uma <b>pedra presa</b> largada no meio de um <b>anel pequeno</b> — os doze glifos do anel de raio dois
 *       — gasta-se e <b>leva tudo o que estiver a quatro blocos</b> do meio para onde ela aponta. Bicho,
 *       gente, e item largado.</li>
 * </ul>
 *
 * <p><b>É o menor ofício que há no mod.</b> Oito riscos de giz à volta de um buraco e um lugar fica marcado
 * para sempre; doze riscos e ele vira porta. Sem altar, sem ritual, sem bruxa — e é de propósito, porque é a
 * primeira coisa que alguém faz com giz do Alhures antes de saber para que ele serve.
 *
 * <p>A pedra presa é o que os <b>ritos de teleporte</b> comem, e é por isso que ela veio antes deles.
 *
 * <p>E há um quarto risco, que não é de Alhures: a <b>Pedra Sintonizada</b> ou o <b>Espírito Dominado</b>
 * largados num <b>anel miúdo de giz de Ritual</b> gastam uma peça e soltam um <b>Espírito</b> que aponta a
 * aldeia mais perto e some em dez segundos — e não devolve nada. É a bússola mais cara do mod.
 *
 * <p><b>Fica de fora, declarado:</b>
 * <ul>
 *   <li>A pedra presa, segurada na mão, mostra o lugar dela por uma <b>câmara remota</b> — um pacote de rede e
 *       uma tela próprios.</li>
 *   <li>O original escolhe o alvo do anel por {@code EntityPlayer} primeiro e {@code EntityLiving} depois, em
 *       duas varreduras. Aqui é uma só, com a gente a valer mais: dá o mesmo, porque a gente sempre ganha de
 *       qualquer bicho, por mais perto que ele esteja.</li>
 * </ul>
 */
public final class Waystones {
    /** Quantas batidas a pedra tem de estar no chão antes de olhar o giz: os dois segundos do original. */
    public static final int ESPERA = 40;

    /** E de quanto em quanto ela olha, depois disso. */
    public static final int OLHA_DE = 40;

    /** Quantas pedras lisas um anel miúdo prende de uma vez, quando prende a um lugar. */
    public static final int PRENDE_ATÉ = 8;

    /** O que custa prender uma pedra a uma <b>pessoa</b>: os quatro mil de poder do original. */
    public static final float CUSTA = 4000.0f;

    /** A que distância do meio alguém tem de estar para a pedra se prender a ele. */
    public static final double ALCANCE_DO_ALVO = 2.0;

    /** E a que distância do meio uma pedra presa leva o que houver. */
    public static final double ALCANCE_DA_PORTA = 4.0;

    /** As oito casas que cercam a pedra: o anel miúdo, o menor que há. */
    private static final int[][] MIÚDO = {
            {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1},
    };

    /** E os doze do anel pequeno, de raio dois: o {@code CircleUtil.isSmallCircle}. */
    private static final int[][] PEQUENO = {
            {0, -2}, {1, -2}, {2, -1}, {2, 0}, {2, 1}, {1, 2},
            {0, 2}, {-1, 2}, {-2, 1}, {-2, 0}, {-2, -1}, {-1, -2},
    };

    /**
     * As nove casas em que o meio do anel pequeno pode estar, visto de onde a pedra caiu.
     *
     * <p>O original varre esta mesma lista e devolve {@code coord - co} quando acha o anel em {@code coord +
     * co} — o que é <b>o avesso</b> do meio que ele achou. <b>Está corrigido aqui</b>: o meio é onde o anel
     * está. Com a pedra bem no meio, o primeiro da lista é {@code (0,0)} e o engano não se vê; é só quando ela
     * cai de lado que o original manda a porta para o lugar errado. Está no {@code PORTE.md}.
     */
    private static final int[][] ONDE_O_MEIO_PODE_ESTAR = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
    };

    /** O lugar que uma pedra presa guarda. */
    public record Lugar(ResourceKey<Level> mundo, BlockPos onde) {
        public static final Codec<Lugar> CODEC = RecordCodecBuilder.create(i -> i.group(
                        ResourceKey.codec(Registries.DIMENSION).fieldOf("mundo").forGetter(Lugar::mundo),
                        BlockPos.CODEC.fieldOf("onde").forGetter(Lugar::onde))
                .apply(i, Lugar::new));

        public static final net.minecraft.network.codec.StreamCodec<
                net.minecraft.network.RegistryFriendlyByteBuf, Lugar> STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.composite(
                        ResourceKey.streamCodec(Registries.DIMENSION), Lugar::mundo,
                        BlockPos.STREAM_CODEC, Lugar::onde,
                        Lugar::new);
    }

    private Waystones() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar. */
    public static void init() {
    }

    /** O lugar desta pedra, se ela tiver algum. */
    @Nullable
    public static Lugar lugar(ItemStack pedra) {
        return pedra.get(OccultaComponents.WAYSTONE);
    }

    /** Se esta pedra sabe ir a algum lugar — por lugar ou por pessoa. */
    public static boolean presa(ItemStack pedra) {
        return pedra.has(OccultaComponents.WAYSTONE) || TaglockItem.isBound(pedra);
    }

    // ------------------------------------------------------------------ prender

    /**
     * Uma pedra lisa largada no chão: se estiver no meio de um anel miúdo, prende-se.
     *
     * <p>Devolve <b>verdadeiro</b> se gastou o anel — e nesse caso o item largado já se foi.
     */
    public static boolean tentaPrender(ServerLevel level, ItemEntity largada) {
        ItemStack pedra = largada.getItem();
        if (!pedra.is(OccultaItems.WAYSTONE)) return false;

        BlockPos meio = largada.blockPosition();
        if (!anelMiúdo(level, meio)) return false;

        int tinha = pedra.getCount();
        ItemStack feita;
        int sobra;

        LivingEntity alvo = alvoNoAnel(level, meio);
        if (alvo != null) {
            // presa a uma pessoa: custa poder, e é só uma
            var altar = PowerSources.closest(level, meio);
            if (altar == null || !PowerSources.consume(level, meio, CUSTA)) {
                fumo(level, meio);
                return false;
            }
            sobra = tinha - 1;
            feita = sangrada(alvo);
        } else {
            int quantas = Math.min(tinha, PRENDE_ATÉ);
            sobra = tinha - quantas;
            feita = new ItemStack(OccultaItems.BOUND_WAYSTONE, quantas);
            feita.set(OccultaComponents.WAYSTONE, new Lugar(level.dimension(), meio));
        }

        level.addFreshEntity(new ItemEntity(level, largada.getX(), largada.getY(), largada.getZ(), feita));
        if (sobra > 0) {
            level.addFreshEntity(new ItemEntity(level, largada.getX(), largada.getY(), largada.getZ(),
                    new ItemStack(OccultaItems.WAYSTONE, sobra)));
        }

        level.sendParticles(ParticleTypes.EXPLOSION, largada.getX(), largada.getY(), largada.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, meio, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.6f, 1.4f);
        gastaOAnel(level, meio);
        largada.discard();
        return true;
    }

    /**
     * A Pedra Sangrada deste bicho: a pedra presa a <b>quem</b>, e não a onde.
     *
     * <p>É o {@code setTaglockForEntity} do original, e é o mesmo vínculo do Frasco de Sangue — a pedra e o
     * frasco guardam a mesma coisa, e é de propósito: quem sabe encher um frasco sabe prender uma pedra.
     */
    public static ItemStack sangrada(LivingEntity alvo) {
        ItemStack feita = new ItemStack(OccultaItems.BLOODED_WAYSTONE);
        TaglockItem.bind(feita, alvo);
        return feita;
    }

    /** Quem está de pé dentro do anel, a até dois blocos do meio — a gente antes do bicho. */
    @Nullable
    public static LivingEntity alvoNoAnel(ServerLevel level, BlockPos meio) {
        double x = meio.getX() + 0.5;
        double z = meio.getZ() + 0.5;
        AABB volta = new AABB(meio).inflate(ALCANCE_DO_ALVO);

        LivingEntity achado = null;
        double maisPerto = -1.0;
        for (LivingEntity quem : level.getEntitiesOfClass(LivingEntity.class, volta)) {
            if (!(quem instanceof Player) && !(quem instanceof Mob)) continue;
            double longe = quem.distanceToSqr(x, quem.getY(), z);
            if (longe > ALCANCE_DO_ALVO * ALCANCE_DO_ALVO) continue;
            // a gente ganha sempre do bicho, como nas duas varreduras do original
            boolean melhor = achado == null
                    || (quem instanceof Player && !(achado instanceof Player))
                    || ((quem instanceof Player) == (achado instanceof Player) && longe < maisPerto);
            if (melhor) {
                achado = quem;
                maisPerto = longe;
            }
        }
        return achado;
    }

    // ------------------------------------------------------------------ levar

    /**
     * Uma pedra presa largada no chão: se estiver num anel pequeno, gasta-se e leva o que houver em volta.
     *
     * <p>Devolve <b>verdadeiro</b> se a porta abriu.
     */
    public static boolean tentaLevar(ServerLevel level, ItemEntity largada) {
        ItemStack pedra = largada.getItem();
        if (!pedra.is(OccultaItems.BOUND_WAYSTONE) && !pedra.is(OccultaItems.BLOODED_WAYSTONE)) {
            return false;
        }

        BlockPos meio = meioDoAnelPequeno(level, largada.blockPosition());
        if (meio == null) return false;

        // uma pedra se gasta; o resto do monte cai de volta no chão
        ItemStack gasta = pedra.split(1);
        if (!pedra.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, largada.getX(), largada.getY(), largada.getZ(),
                    pedra.copy()));
        }
        largada.discard();

        double x = meio.getX() + 0.5;
        double z = meio.getZ() + 0.5;
        AABB volta = new AABB(meio).inflate(ALCANCE_DA_PORTA);
        for (Entity quem : level.getEntities((Entity) null, volta, bicho ->
                bicho instanceof LivingEntity || bicho instanceof ItemEntity)) {
            if (quem.isRemoved()) continue;
            if (quem.distanceToSqr(x, quem.getY(), z) > ALCANCE_DA_PORTA * ALCANCE_DA_PORTA) continue;
            if (OccultaEffects.inibido(quem, 0)) {
                fumo(level, quem.blockPosition());
                continue;
            }
            if (!leva(level, gasta, quem)) fumo(level, quem.blockPosition());
        }
        return true;
    }

    /**
     * Leva este bicho para onde a pedra aponta: o {@code teleportToLocation} do {@code ItemGeneral}.
     *
     * <p>Primeiro o lugar; não havendo lugar, a pessoa a que ela está presa — e aí o destino é onde essa
     * pessoa estiver <b>agora</b>, que é o que torna a pedra presa a alguém diferente da pedra presa a um
     * lugar.
     */
    public static boolean leva(ServerLevel level, ItemStack pedra, Entity quem) {
        Lugar onde = lugar(pedra);
        if (onde != null) {
            ServerLevel destino = level.getServer().getLevel(onde.mundo());
            if (destino == null) return false;
            return põe(level, destino, onde.onde().getX() + 0.5, onde.onde().getY(),
                    onde.onde().getZ() + 0.5, quem);
        }

        var vínculo = TaglockItem.bound(pedra);
        if (vínculo == null) return false;
        Player dono = level.getServer().getPlayerList().getPlayer(vínculo.owner());
        if (dono == null) return false;
        if (!(dono.level() instanceof ServerLevel ondeEle)) return false;
        return põe(level, ondeEle, dono.getX(), dono.getY(), dono.getZ(), quem);
    }

    /** O portal dos dois lados: o fumo antes e depois, como no original. */
    private static boolean põe(ServerLevel daqui, ServerLevel para, double x, double y, double z,
                               Entity quem) {
        portal(daqui, quem.getX(), quem.getY(), quem.getZ());
        boolean foi = quem.teleportTo(para, x, y, z, Set.of(Relative.X_ROT, Relative.Y_ROT),
                quem.getYRot(), quem.getXRot(), true);
        if (!foi) return false;
        portal(para, x, y, z);
        return true;
    }

    private static void portal(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 32, 0.5, 1.0, 0.5, 0.2);
        level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void fumo(ServerLevel level, BlockPos onde) {
        level.sendParticles(ParticleTypes.SMOKE, onde.getX() + 0.5, onde.getY() + 0.5, onde.getZ() + 0.5,
                16, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------------ chamar o espírito

    /**
     * A <b>Pedra Sintonizada</b> ou o <b>Espírito Dominado</b> largados num anel miúdo de <b>giz de
     * Ritual</b>: o terceiro ramo do {@code onUpdate} do {@code EntityItemWaystone}.
     *
     * <p>Uma peça do monte se gasta, o resto cai de volta no chão, o anel estoura e nasce um
     * <b>Espírito com dez segundos de vida e o rumo da aldeia mais perto</b> — do feitio que não
     * devolve nada. É uma bússola que se queima ao apontar, e é cara: uma Pedra Sintonizada.
     *
     * <p>Repare no giz: os outros três ramos pedem o <b>do Alhures</b>, e este pede o <b>de Ritual</b>.
     * Não é um descuido do original — é o que separa a geometria que move coisas da geometria que
     * chama coisas.
     *
     * @return se o anel se gastou
     */
    public static boolean tentaChamarOEspírito(ServerLevel level, ItemEntity largada) {
        ItemStack oquê = largada.getItem();
        if (!oquê.is(OccultaItems.ATTUNED_STONE) && !oquê.is(OccultaItems.SUBDUED_SPIRIT)) return false;

        BlockPos meio = largada.blockPosition();
        if (!anelMiúdoDeRitual(level, meio)) return false;

        oquê.shrink(1);
        if (!oquê.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, largada.getX(), largada.getY(), largada.getZ(),
                    oquê.copy()));
        }
        largada.discard();

        var bicho = net.thaumcraft.occulta.OccultaEntities.SPIRIT.create(level,
                net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (bicho != null) {
            bicho.snapTo(largada.getX(), largada.getY(), largada.getZ(), 0.0f, 0.0f);
            bicho.setPersistenceRequired();
            level.addFreshEntity(bicho);
            bicho.vaiParaAAldeia(level,
                    net.thaumcraft.occulta.spirit.SpiritEntity.SEM_DESPOJO);
            level.sendParticles(net.minecraft.core.particles.SpellParticleOption.create(
                            ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                    bicho.getX(), bicho.getY() + bicho.getBbHeight() / 2.0, bicho.getZ(),
                    16, 1.0, bicho.getBbHeight(), 1.0, 0.0);
        }

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, largada.getX(), largada.getY(),
                largada.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, meio, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f,
                level.getRandom().nextFloat() * 0.4f + 0.8f);
        gastaOAnelDeRitual(level, meio);
        return true;
    }

    // ------------------------------------------------------------------ o giz

    /** Se as oito casas em volta são glifos do Alhures: o {@code isInnerTinyBlockCircle}. */
    public static boolean anelMiúdo(Level level, BlockPos meio) {
        for (int[] casa : MIÚDO) {
            if (!level.getBlockState(meio.offset(casa[0], 0, casa[1])).is(OccultaBlocks.OTHERWHERE_GLYPH)) {
                return false;
            }
        }
        return true;
    }

    /** Se os doze glifos do anel pequeno estão riscados em volta deste meio. */
    public static boolean anelPequeno(Level level, BlockPos meio) {
        for (int[] casa : PEQUENO) {
            if (!level.getBlockState(meio.offset(casa[0], 0, casa[1])).is(OccultaBlocks.OTHERWHERE_GLYPH)) {
                return false;
            }
        }
        return true;
    }

    /** O meio do anel pequeno, visto de onde a pedra caiu — ou nada, se não há anel. */
    @Nullable
    public static BlockPos meioDoAnelPequeno(Level level, BlockPos daPedra) {
        for (int[] casa : ONDE_O_MEIO_PODE_ESTAR) {
            BlockPos tenta = daPedra.offset(casa[0], 0, casa[1]);
            if (anelPequeno(level, tenta)) return tenta;
        }
        return null;
    }

    /** O mesmo anel miúdo, mas riscado a <b>giz de Ritual</b>. */
    public static boolean anelMiúdoDeRitual(Level level, BlockPos meio) {
        for (int[] casa : MIÚDO) {
            if (!level.getBlockState(meio.offset(casa[0], 0, casa[1])).is(OccultaBlocks.RITUAL_GLYPH)) {
                return false;
            }
        }
        return true;
    }

    /** E esse também se gasta, do mesmo jeito. */
    private static void gastaOAnelDeRitual(ServerLevel level, BlockPos meio) {
        gastaOAnel(level, meio);
    }

    /** O anel miúdo se gasta: os oito glifos somem, cada um com o seu estouro. */
    private static void gastaOAnel(ServerLevel level, BlockPos meio) {
        for (int[] casa : MIÚDO) {
            BlockPos onde = meio.offset(casa[0], 0, casa[1]);
            level.removeBlock(onde, false);
            level.sendParticles(ParticleTypes.EXPLOSION, onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
