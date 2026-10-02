package net.thaumcraft.occulta.spirit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.world.DynamicDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * O Mundo dos Espíritos: o {@code WorldProviderDreamWorld} do Witchery.
 *
 * <p>Não é um lugar novo: é <b>este</b> lugar, visto de outro lado. O chão é o mesmo do mundo de cima, monte por
 * monte — o original pede ao mundo de cima o gerador dele e usa o mesmo —, mas o céu é baço, não chove e nada
 * nasce ali senão o que só nasce em sonho: o <b>Algodão Sonhador</b> e a <b>Erva Cintilante</b>.
 *
 * <p>Quem vai para lá <b>não viaja</b>: deita-se. O corpo fica onde estava, em carne, e o espírito se levanta —
 * com <b>outra</b> mochila, <b>outra</b> vida e <b>outra</b> fome. Da mochila de cá só passam duas coisas, e são
 * as do original: a <b>Agulha de Gelo</b>, que é como se acorda, e o <b>Mutandis</b>. De lá para cá passam o que
 * só existe lá: os dois algodões, a Fome Melíflua, a Agulha de Gelo e o que se apanhou de espírito.
 */
public final class SpiritWorld {
    /** O mundo dos espíritos. */
    public static final ResourceKey<Level> LEVEL = DynamicDimensions.key("spirit");

    /** E o feitio dele: o mesmo chão, o céu baço e a hora parada. */
    public static final ResourceKey<DimensionType> TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, Thaumcraft.id("spirit"));

    // ------------------------------------------------------------------ a conta do pesadelo

    /** A que distância se olha em volta de quem adormece: os oito do original. */
    public static final int LOOK = 8;

    /** O que cada coisa em volta tira ou põe na chance de pesadelo, e quantas de cada contam. */
    public static final double CATCHER = -0.5;
    public static final double COTTON = -0.1;
    public static final int COTTONS = 2;
    public static final double FIRE = 0.1;
    public static final int FIRES = 3;
    /** E a poça de Espírito Fluente, que tira um décimo cada, até três. */
    public static final double POOL = -0.1;
    public static final int POOLS = 3;

    private SpiritWorld() {
    }

    public static boolean is(Level level) {
        return level.dimension() == LEVEL;
    }

    /**
     * O mundo dos espíritos, abrindo-o se ainda não houver.
     *
     * <p>Ele nasce com o <b>gerador do mundo de cima</b>, que é o que o original faz: assim o chão de lá é o
     * mesmo de cá, monte por monte, e quem anda em espírito reconhece o caminho de casa.
     */
    public static @Nullable ServerLevel level(MinecraftServer server) {
        var gerador = server.overworld().getChunkSource().getGenerator();
        return DynamicDimensions.getOrCreate(server, LEVEL, TYPE, gerador);
    }

    /**
     * A chance de o sonho ser dos maus, olhando o que há em volta de quem adormece: o pedaço do
     * {@code sendPlayerToSpiritWorld} que conta as coisas.
     *
     * <p>A regra do original é a que surpreende: os arredores <b>só contam</b> se houver um <b>Apanhador de
     * Sonhos com a teia dos pesadelos</b> por perto. Sem ele, a chance é a que veio, e ela é quase um. É isso que
     * faz da primeira noite uma noite feia, e do quarto de sonho uma coisa que se constrói.
     *
     * <p>As <b>poças de Espírito Fluente</b> tiram dez por cento cada, até três — e só contam as <b>fontes</b>,
     * que é o que o original mede ao exigir metadado zero.
     *
     * <p><b>Do original fica de fora, declarado:</b> o <b>Coração de Demônio</b>, que lá <i>sobe</i> a chance em
     * trinta e cinco por cento cada e é o que torna o pesadelo <b>demoníaco</b>. Ele é bloco de demônio, e o
     * demônio não está portado; sem ele, não há pesadelo demoníaco neste porte.
     */
    public static double nightmareChance(ServerLevel level, BlockPos onde, double base) {
        if (base <= 0.0 || base >= 1.0) return base;

        double chance = base;
        boolean apanhador = false;
        int algodões = 0;
        int fogos = 0;
        int poças = 0;

        for (BlockPos casa : BlockPos.betweenClosed(onde.offset(-LOOK, -LOOK, -LOOK),
                onde.offset(LOOK, LOOK, LOOK))) {
            var feitio = level.getBlockState(casa);
            if (!apanhador && feitio.is(OccultaBlocks.DREAM_CATCHER)
                    && DreamCatcherBlockEntity.catchesNightmares(level, casa)) {
                chance += CATCHER;
                apanhador = true;
            }
            if (algodões < COTTONS && feitio.is(OccultaBlocks.WISPY_COTTON)) {
                algodões++;
                chance += COTTON;
            }
            if (fogos < FIRES && feitio.is(Blocks.FIRE)) {
                fogos++;
                chance += FIRE;
            }
            if (poças < POOLS && feitio.is(OccultaBlocks.FLOWING_SPIRIT)
                    && feitio.getFluidState().isSource()) {
                poças++;
                chance += POOL;
            }
        }
        return apanhador ? Math.min(Math.max(chance, 0.0), 1.0) : base;
    }

    // ------------------------------------------------------------------ deitar-se e levantar-se

    /** O que passa da mochila de cá para a de lá. */
    private static List<net.minecraft.world.item.Item> CARRIED_IN() {
        return List.of(OccultaItems.ICY_NEEDLE, OccultaItems.MUTANDIS);
    }

    /** E o que passa da de lá para a de cá. */
    private static List<net.minecraft.world.item.Item> CARRIED_OUT() {
        return List.of(OccultaItems.WISPY_COTTON, OccultaItems.DISTURBED_COTTON,
                OccultaItems.ICY_NEEDLE, OccultaItems.MELLIFLUOUS_HUNGER);
    }

    /**
     * Manda o espírito daquela pessoa para o outro lado: o {@code sendPlayerToSpiritWorld}.
     *
     * @param base a chance de pesadelo antes de se olhar em volta
     * @return se foi
     */
    public static boolean fallAsleep(ServerPlayer quem, double base) {
        if (!(quem.level() instanceof ServerLevel aqui) || is(aqui)) return false;
        if (SpiritWalk.walking(quem)) return false;
        ServerLevel lá = level(aqui.getServer());
        if (lá == null) return false;

        boolean pesadelo = false;
        double chance = nightmareChance(aqui, quem.blockPosition(), base);
        if (chance > 0.0) pesadelo = chance >= 1.0 || aqui.getRandom().nextDouble() < chance;

        // o corpo fica onde estava, em carne
        BlockPos corpo = quem.blockPosition();
        CorpseEntity.lay(aqui, quem);

        // o que passa daqui para lá, e o resto fica com o corpo
        List<ItemStack> passam = take(quem, CARRIED_IN());
        SpiritWalk agora = SpiritWalk.of(quem)
                .withWalking(true)
                .withDream(pesadelo, false)
                .withBody(corpo);
        agora = swap(quem, agora);
        SpiritWalk.set(quem, agora);
        for (ItemStack coisa : passam) quem.getInventory().add(coisa);

        int alto = lá.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, corpo.getX(), corpo.getZ());
        quem.teleportTo(lá, corpo.getX() + 0.5, alto + 1.0, corpo.getZ() + 0.5, Set.of(),
                quem.getYRot(), quem.getXRot(), false);
        return true;
    }

    /**
     * Acorda: o {@code returnPlayerToOverworld}.
     *
     * <p>O espírito volta ao corpo, a mochila de cá volta ao lugar, e o que se apanhou do outro lado vem com ele
     * — só o que o original deixa vir.
     */
    public static boolean wakeUp(ServerPlayer quem) {
        SpiritWalk era = SpiritWalk.of(quem);
        if (!era.walking()) return false;
        MinecraftServer server = quem.level().getServer();
        if (server == null) return false;

        List<ItemStack> trazem = is(quem.level()) ? take(quem, CARRIED_OUT()) : new ArrayList<>();
        SpiritWalk agora = swap(quem, era).withWalking(false).withDream(false, false);
        SpiritWalk.set(quem, agora);
        for (ItemStack coisa : trazem) quem.getInventory().add(coisa);

        ServerLevel casa = server.overworld();
        BlockPos onde = era.body().orElse(casa.getRespawnData().pos());
        int alto = casa.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, onde.getX(), onde.getZ());
        double y = Math.max(onde.getY(), alto);
        quem.teleportTo(casa, onde.getX() + 0.5, y, onde.getZ() + 0.5, Set.of(),
                quem.getYRot(), quem.getXRot(), false);
        quem.clearFire();
        CorpseEntity.rise(casa, quem);
        return true;
    }

    /**
     * Troca a metade de cá pela de lá: mochila, vida e fome.
     *
     * @return o guardado novo, com a metade que ficou lá dentro
     */
    private static SpiritWalk swap(ServerPlayer quem, SpiritWalk era) {
        List<ItemStack> minha = new ArrayList<>();
        var mochila = quem.getInventory();
        for (int i = 0; i < mochila.getContainerSize(); i++) minha.add(mochila.getItem(i).copy());
        float vida = quem.getHealth();
        int fome = quem.getFoodData().getFoodLevel();
        float gordura = quem.getFoodData().getSaturationLevel();

        mochila.clearContent();
        List<ItemStack> outra = era.inventory();
        for (int i = 0; i < outra.size() && i < mochila.getContainerSize(); i++) {
            mochila.setItem(i, outra.get(i).copy());
        }
        // a primeira vez do outro lado começa com a vida cheia e a fome do original
        quem.setHealth(outra.isEmpty() && era.health() <= 0.0f ? quem.getMaxHealth()
                : Math.max(era.health(), 1.0f));
        quem.getFoodData().setFoodLevel(outra.isEmpty() && era.food() <= 0 ? 20 : era.food());
        quem.getFoodData().setSaturation(era.saturation());
        quem.containerMenu.broadcastChanges();
        return era.withOther(minha, vida, fome, gordura);
    }

    /** Tira da mochila tudo o que for daquelas coisas, e devolve o que tirou. */
    private static List<ItemStack> take(ServerPlayer quem, List<net.minecraft.world.item.Item> quais) {
        List<ItemStack> tirados = new ArrayList<>();
        var mochila = quem.getInventory();
        for (int i = 0; i < mochila.getContainerSize(); i++) {
            ItemStack coisa = mochila.getItem(i);
            if (coisa.isEmpty() || !quais.contains(coisa.getItem())) continue;
            tirados.add(coisa.copy());
            mochila.setItem(i, ItemStack.EMPTY);
        }
        return tirados;
    }

    // ------------------------------------------------------------------ o fantasma

    /**
     * Manifesta o espírito no mundo de cá: o {@code manifestPlayerInOverworldAsGhost}.
     *
     * <p>Ele deixa do outro lado <b>tudo o que trazia menos as Agulhas de Gelo</b>, e aparece no chão alto do
     * mundo de cima, no mesmo ponto do mapa. Só passa quem tiver crédito do Rito da Manifestação; sem ele, o
     * portal deixa passar e não faz nada.
     *
     * @return se foi
     */
    public static boolean manifest(ServerPlayer quem) {
        if (!(quem.level() instanceof ServerLevel aqui) || !is(aqui)) return false;
        SpiritWalk era = SpiritWalk.of(quem);
        if (!era.walking() || era.ghost()) return false;
        if (!SpiritManifest.canManifest(quem)) return false;
        MinecraftServer server = aqui.getServer();
        if (server == null) return false;

        // as agulhas atravessam; o resto fica
        List<ItemStack> agulhas = take(quem, List.of(OccultaItems.ICY_NEEDLE));
        List<ItemStack> ficam = new ArrayList<>();
        var mochila = quem.getInventory();
        for (int i = 0; i < mochila.getContainerSize(); i++) ficam.add(mochila.getItem(i).copy());
        mochila.clearContent();
        for (ItemStack coisa : agulhas) mochila.add(coisa);
        quem.containerMenu.broadcastChanges();

        SpiritWalk.set(quem, era.withGhost(true, ficam, Math.max(quem.getHealth(), 1.0f)));

        ServerLevel casa = server.overworld();
        BlockPos onde = quem.blockPosition();
        int alto = casa.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, onde.getX(), onde.getZ());
        quem.teleportTo(casa, onde.getX() + 0.5, alto, onde.getZ() + 0.5, Set.of(),
                quem.getYRot(), quem.getXRot(), false);
        quem.clearFire();
        return true;
    }

    /**
     * E o contrário: o {@code returnGhostPlayerToSpiritWorld}.
     *
     * <p>As Agulhas que ele tiver na mão voltam com ele; o resto do que apanhou no mundo de cá <b>fica lá</b>,
     * porque fantasma não carrega coisa de gente. A mochila do outro lado volta ao lugar.
     */
    public static boolean unmanifest(ServerPlayer quem) {
        SpiritWalk era = SpiritWalk.of(quem);
        if (!era.ghost()) return false;
        MinecraftServer server = quem.level().getServer();
        if (server == null) return false;
        ServerLevel lá = level(server);
        if (lá == null) return false;

        List<ItemStack> agulhas = take(quem, List.of(OccultaItems.ICY_NEEDLE));
        var mochila = quem.getInventory();
        mochila.clearContent();
        List<ItemStack> voltam = era.ghostInventory();
        for (int i = 0; i < voltam.size() && i < mochila.getContainerSize(); i++) {
            mochila.setItem(i, voltam.get(i).copy());
        }
        for (ItemStack coisa : agulhas) mochila.add(coisa);
        if (era.ghostHealth() > 0.0f) quem.setHealth(Math.min(era.ghostHealth(), quem.getMaxHealth()));
        quem.containerMenu.broadcastChanges();

        SpiritWalk.set(quem, SpiritWalk.of(quem).withGhost(false, List.of(), 0.0f));

        BlockPos onde = quem.blockPosition();
        int alto = lá.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, onde.getX(), onde.getZ());
        quem.teleportTo(lá, onde.getX() + 0.5, alto + 1.0, onde.getZ() + 0.5, Set.of(),
                quem.getYRot(), quem.getXRot(), false);
        quem.clearFire();
        return true;
    }

    /** O corpo daquela pessoa, se ele estiver deitado por aí. */
    public static @Nullable CorpseEntity corpse(ServerLevel level, ServerPlayer quem) {
        return CorpseEntity.of(level, quem);
    }

    /** Sem uso fora do porte: serve à prova para pôr um corpo no chão. */
    public static EntitySpawnReason reason() {
        return EntitySpawnReason.TRIGGERED;
    }
}
