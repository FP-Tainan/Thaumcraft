package net.thaumcraft.occulta.fetish;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaSounds;
import net.thaumcraft.occulta.Spawn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * O que se pode prender a um fetiche: o {@code InfusedSpiritEffect} do Witchery e os cinco filhos dele.
 *
 * <p>Um fetiche vazio é um espantalho de palha. O que o torna uma coisa é um <b>efeito</b> preso a ele por
 * um rito, e o que o rito pede são <b>espíritos</b> — de quatro espécies, em contas diferentes:
 *
 * <table border="1">
 *   <caption>O preço de cada um</caption>
 *   <tr><th>Efeito</th><th>Espíritos</th><th>Espectros</th><th>Banshees</th><th>Poltergeists</th></tr>
 *   <tr><td>Proteção de Vodu</td><td>3</td><td>1</td><td>1</td><td>1</td></tr>
 *   <tr><td>Sentinela</td><td>3</td><td>3</td><td>0</td><td>0</td></tr>
 *   <tr><td>Grito</td><td>3</td><td>0</td><td>2</td><td>0</td></tr>
 *   <tr><td>Desorientação</td><td>3</td><td>0</td><td>0</td><td>2</td></tr>
 *   <tr><td>Caminhar Fantasma</td><td>3</td><td>1</td><td>1</td><td>0</td></tr>
 * </table>
 *
 * <p>Repare na coluna dos <b>espíritos</b>: ela é três em todas. Não há efeito de fetiche que se consiga
 * sem três idas ao outro lado, e é isso que faz do Espírito a moeda do ramo.
 *
 * <h2>Quem cabe primeiro</h2>
 *
 * <p>O rito não escolhe o efeito — ele <b>pega no primeiro da lista cuja conta couber</b> no que estiver
 * dentro do círculo, e gasta exatamente o que esse pede. Quem quiser a Sentinela e levar três espectros e
 * duas banshees leva, em vez dela, a <b>Proteção de Vodu</b>, que é a primeira e pede menos de cada.
 *
 * <p>Isso faz da ordem da lista uma regra do jogo, e não um detalhe: <b>para ter o que se quer, leva-se o
 * que ele pede e não mais</b>.
 */
public abstract class SpiritEffects {
    /** A lista, por ordem — e a ordem é a regra. */
    private static final java.util.ArrayList<SpiritEffects> ALL = new java.util.ArrayList<>();

    /** A que distância um fetiche conta para quem carrega uma boneca: os dezesseis do original. */
    public static final double RANGE = 16.0;
    public static final double RANGE_SQ = RANGE * RANGE;

    public final int id;
    public final String key;
    public final int spirits;
    public final int spectres;
    public final int banshees;
    public final int poltergeists;
    private final boolean inBook;

    protected SpiritEffects(int id, String key, int spirits, int spectres, int banshees,
                            int poltergeists, boolean inBook) {
        this.id = id;
        this.key = key;
        this.spirits = spirits;
        this.spectres = spectres;
        this.banshees = banshees;
        this.poltergeists = poltergeists;
        this.inBook = inBook;
        while (ALL.size() <= id) ALL.add(null);
        ALL.set(id, this);
    }

    public boolean inBook() {
        return this.inBook;
    }

    /** O nome dele, para o item e para o livro. */
    public Component name() {
        return Component.translatable("tc.fetish." + this.key);
    }

    /** De quantas em quantas batidas ele pode disparar outra vez, ou −1 se não houver descanso. */
    public int cooldown() {
        return -1;
    }

    /** A que distância ele procura, ou zero se não procura nada. */
    public double radius() {
        return 0.0;
    }

    /** Se ele manda sinal de redstone enquanto o alarme está levantado. */
    public boolean redstone() {
        return false;
    }

    /**
     * O que ele faz.
     *
     * @param tile    o fetiche
     * @param alarme  se o alarme está levantado agora
     * @param achados quem o alarme achou
     * @return se ele fez alguma coisa — e, fazendo, o descanso começa
     */
    public abstract boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados);

    // ------------------------------------------------------------------ a lista

    public static List<SpiritEffects> all() {
        return java.util.Collections.unmodifiableList(ALL);
    }

    public static @Nullable SpiritEffects byId(int id) {
        return id > 0 && id < ALL.size() ? ALL.get(id) : null;
    }

    /** O efeito preso a esta alma, ou nada. */
    public static @Nullable SpiritEffects of(FetishBlockEntity tile) {
        return byId(tile.effectType());
    }

    /** O efeito escrito nesta peça, ou zero. */
    public static int idOf(ItemStack oquê) {
        return oquê.getOrDefault(OccultaComponents.FETISH_EFFECT, 0);
    }

    public static ItemStack withId(ItemStack oquê, int id) {
        oquê.set(OccultaComponents.FETISH_EFFECT, id);
        return oquê;
    }

    public static ItemStack with(ItemStack oquê, SpiritEffects qual) {
        return withId(oquê, qual.id);
    }

    /**
     * <b>Prende o primeiro que couber.</b> O {@code tryBindFetish} do original.
     *
     * @return o que se prendeu, ou nada se nenhum coube
     */
    public static @Nullable SpiritEffects bind(ServerLevel level, ItemStack fetiche,
                                               List<? extends Mob> espíritos,
                                               List<? extends Mob> espectros,
                                               List<? extends Mob> banshees,
                                               List<? extends Mob> poltergeists) {
        for (SpiritEffects qual : ALL) {
            if (qual == null) continue;
            if (qual.spirits > espíritos.size() || qual.spectres > espectros.size()
                    || qual.banshees > banshees.size() || qual.poltergeists > poltergeists.size()) {
                continue;
            }
            withId(fetiche, qual.id);
            gasta(level, qual.spirits, espíritos);
            gasta(level, qual.spectres, espectros);
            gasta(level, qual.banshees, banshees);
            gasta(level, qual.poltergeists, poltergeists);
            return qual;
        }
        return null;
    }

    /** E os bichos que ele gasta somem — cada um com o seu pó de portal e o seu estalo. */
    private static void gasta(ServerLevel level, int quantos, List<? extends Mob> quais) {
        for (int i = 0; i < quantos; i++) {
            Mob quem = quais.get(i);
            level.sendParticles(ParticleTypes.PORTAL, quem.getX(),
                    quem.getY() + quem.getBbHeight() / 2.0, quem.getZ(), 16, 1.0, 2.0, 1.0, 0.0);
            level.playSound(null, quem.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f,
                    level.getRandom().nextFloat() * 0.4f + 0.8f);
            quem.discard();
        }
    }

    // ------------------------------------------------------------------ os cinco

    /**
     * <b>Proteção de Vodu</b>, o primeiro e o mais barato de cada espécie.
     *
     * <p>Ele <b>não faz nada por si</b>: não procura ninguém, não dispara, não manda redstone. Quem o lê
     * são as <b>bonecas</b>, que ficam mais fortes a dezesseis blocos de um fetiche que o tenha. É o único
     * dos cinco que funciona sem o dono saber que está funcionando.
     */
    public static final SpiritEffects ENHANCED_POPPETS =
            new SpiritEffects(1, "enhancedpoppets", 3, 1, 1, 1, true) {
                @Override
                public boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados) {
                    return false;
                }
            };

    /**
     * <b>Sentinela</b>: por cada um que o alarme ache, nasce um <b>Espectro</b> a um bloco dele, já com ele
     * marcado e com <b>trinta segundos de vida</b>.
     *
     * <p>E são <b>dois</b> se houver um só — o espantalho que acha um intruso manda dois contra ele, e o
     * que acha cinco manda um a cada. É a conta de um bicho que não quer gastar o que tem.
     */
    public static final SpiritEffects SENTINEL =
            new SpiritEffects(2, "sentinal", 3, 3, 0, 0, true) {
                /** Quanto descansa, quanto vê, e quanto dura cada espectro. */
                static final int DESCANSA = 20 * 30;
                static final int DURA = 20 * 30;
                static final int SOZINHO = 2;
                static final int ACOMPANHADO = 1;

                @Override
                public int cooldown() {
                    return DESCANSA;
                }

                @Override
                public double radius() {
                    return 8.0;
                }

                @Override
                public boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados) {
                    if (!alarme) return false;
                    if (!(tile.getLevel() instanceof ServerLevel level)) return false;

                    int quantos = achados.size() > 1 ? ACOMPANHADO : SOZINHO;
                    for (LivingEntity quem : achados) {
                        for (int volta = 0; volta < quantos; volta++) {
                            var bicho = Spawn.perto(level, OccultaEntities.SPECTRE,
                                    quem.blockPosition(), 1, 1);
                            if (!(bicho instanceof net.thaumcraft.occulta.ghost.SpectreEntity espectro)) {
                                continue;
                            }
                            Spawn.comOvo(level, espectro);
                            espectro.setTarget(quem);
                            espectro.prazo(DURA);
                            var meio = Spawn.meio(espectro);
                            level.sendParticles(SpellParticleOption.create(
                                            ParticleTypes.INSTANT_EFFECT, 1.0f, 1.0f, 1.0f, 1.0f),
                                    meio.x, meio.y, meio.z, 16, 1.0, espectro.getBbHeight(), 1.0, 0.0);
                            level.playSound(null, espectro.blockPosition(),
                                    OccultaSounds.SPECTRE_SAY.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
                        }
                    }
                    return true;
                }
            };

    /**
     * <b>Grito</b>: o fetiche <b>grita e manda redstone</b> quando o alarme se levanta, e cala-se quando ele
     * baixa. É o único dos cinco que serve de peça de máquina.
     *
     * <p>E o <b>mais longe de todos</b>: dezesseis blocos, o dobro dos outros. Um espantalho que grita é um
     * alarme de verdade.
     *
     * <p>Na Escada de Bruxa ele grita <b>calado</b>: só o pó e a redstone. O original escreve a exceção com
     * o nome do bloco, e faz sentido — uma escada de penas não tem boca.
     */
    public static final SpiritEffects SCREAMER =
            new SpiritEffects(3, "screamer", 3, 0, 2, 0, true) {
                @Override
                public double radius() {
                    return 16.0;
                }

                @Override
                public boolean redstone() {
                    return true;
                }

                @Override
                public boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados) {
                    if (!alarme) return false;
                    if (!(tile.getLevel() instanceof ServerLevel level)) return false;
                    BlockPos onde = tile.getBlockPos();

                    level.sendParticles(DustParticleOptions.REDSTONE, onde.getX() + 0.5,
                            onde.getY() + 0.3, onde.getZ() + 0.5, 16, 0.2, 0.5, 0.2, 0.0);
                    if (!tile.getBlockState().is(net.thaumcraft.occulta.OccultaBlocks.WITCHS_LADDER)) {
                        level.playSound(null, onde, OccultaSounds.SPECTRE_HIT.value(),
                                SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    return true;
                }
            };

    /**
     * <b>Desorientação</b>: quem se aproximar <b>armado ou vestido</b> e olhando para o fetiche é
     * <b>virado ao contrário</b>; e os bichos de menos de cinquenta de vida <b>largam o alvo</b> e apanham
     * outro dos que estão ali.
     *
     * <p>A conta de quem é virado é bonita: mede-se o ângulo de quem está para o fetiche e compara-se com o
     * rumo da cabeça dele. Dentro de <b>quarenta e cinco graus</b> — ou seja, <b>olhando para ele</b> —, o
     * fetiche vira-o. Quem passar de lado não é tocado; quem vier ver o que é, perde-se.
     *
     * <p>E só pega em quem está <b>vestido ou armado</b>, de dez em dez batidas. É o espantalho que deixa
     * passar o lavrador e tonteia o soldado.
     */
    public static final SpiritEffects TWISTER =
            new SpiritEffects(4, "twister", 3, 0, 0, 2, true) {
                /** O arco de quem está olhando, e quanto um bicho tem de ter para não se perder. */
                static final double ARCO = 45.0;
                static final float BICHO_GRANDE = 50.0f;
                static final int DESCANSA = 10;

                @Override
                public int cooldown() {
                    return DESCANSA;
                }

                @Override
                public double radius() {
                    return 8.0;
                }

                @Override
                public boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados) {
                    if (!alarme) return false;
                    BlockPos onde = tile.getBlockPos();

                    for (LivingEntity quem : achados) {
                        if (quem instanceof Player gente) {
                            if (!vestidoOuArmado(gente)) continue;
                            double rumo = Math.toDegrees(Math.atan2(
                                    gente.getZ() - (onde.getZ() + 0.5),
                                    gente.getX() - (onde.getX() + 0.5))) + 180.0;
                            double dele = (gente.getYRot() + 90.0f) % 360.0f;
                            if (dele < 0.0) dele += 360.0;
                            double entre = Math.abs(rumo - dele) % 360.0;
                            if (entre >= ARCO && 360.0 - entre >= ARCO) continue;
                            gente.snapTo(gente.getX(), gente.getY(), gente.getZ(),
                                    (float) ((rumo + 90.0) % 360.0), gente.getXRot());
                        } else if (quem instanceof Mob bicho) {
                            if (bicho.getMaxHealth() >= BICHO_GRANDE) continue;
                            bicho.setTarget(null);
                            if (achados.size() <= 1) continue;
                            LivingEntity outro = achados.get(
                                    bicho.getRandom().nextInt(achados.size()));
                            if (outro != bicho) bicho.setTarget(outro);
                        }
                    }
                    return true;
                }

                private static boolean vestidoOuArmado(Player gente) {
                    for (var casa : net.minecraft.world.entity.EquipmentSlot.values()) {
                        if (casa.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) {
                            continue;
                        }
                        if (!gente.getItemBySlot(casa).isEmpty()) return true;
                    }
                    return !gente.getMainHandItem().isEmpty();
                }
            };

    /**
     * <b>Caminhar Fantasma</b>: a quem andar em espírito por perto, o fetiche <b>salta a próxima perda de
     * manifestação</b>.
     *
     * <p>É o mais quieto dos cinco e o mais útil de todos: um fantasma no mundo dos sonhos perde-se de si
     * próprio com o tempo, e um destes à porta de casa faz com que ele não se perca.
     */
    public static final SpiritEffects GHOST_WALKER =
            new SpiritEffects(5, "ghostwalker", 3, 1, 1, 0, true) {
                @Override
                public double radius() {
                    return 8.0;
                }

                @Override
                public boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados) {
                    if (!alarme) return false;
                    boolean fez = false;
                    for (LivingEntity quem : achados) {
                        if (!(quem instanceof net.minecraft.server.level.ServerPlayer gente)) continue;
                        if (!net.thaumcraft.occulta.spirit.SpiritManifest.ghost(gente)) continue;
                        net.thaumcraft.occulta.spirit.SpiritManifest.skipNext(gente);
                        fez = true;
                    }
                    return fez;
                }
            };

    /**
     * <b>A Morte</b>: o sexto, que não entra no livro e que ninguém pede de propósito.
     *
     * <p>Ele custa <b>cinco de cada</b> dos três fantasmas e <b>nenhum espírito</b>, e a conta é um aviso:
     * quem puser quinze fantasmas dentro de um círculo de giz não vai ficar com um espantalho.
     *
     * <p><b>Fica declarado:</b> este porte põe o efeito na lista para a conta ficar certa — ele é o último,
     * e por isso só cabe quando nenhum dos cinco cabe —, mas <b>o que ele chama ainda não existe</b>. A
     * Morte é outra fatia. Até lá, prendê-lo é prender um fetiche que não faz nada.
     */
    public static final SpiritEffects DEATH =
            new SpiritEffects(6, "death", 0, 5, 5, 5, false) {
                @Override
                public boolean run(FetishBlockEntity tile, boolean alarme, List<LivingEntity> achados) {
                    return true;
                }
            };

    /** Para a lista nascer carregada antes de alguém perguntar por ela. */
    public static void init() {
        // os seis campos estáticos bastam; este método é o pretexto para a aula carregar
    }
}
