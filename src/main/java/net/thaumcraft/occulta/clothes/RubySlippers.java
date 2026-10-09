package net.thaumcraft.occulta.clothes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.infusion.Infusions;
import net.thaumcraft.occulta.waystone.Waystones;

/**
 * As <b>Chinelas de Rubi</b>: o {@code trySayTheresNoPlaceLikeHome} do Witchery.
 *
 * <p>São a única peça de equipamento do mod inteiro que se usa <b>falando</b>. Calça-se o par e diz-se
 * <b>«não há lugar como o nosso lar»</b> no chat — e quem as traz vai para casa.
 *
 * <p>Casa é a <b>cama</b> de quem é, ou o nascimento do mundo se não houver cama, e custa <b>oitenta</b>
 * de infusão (<b>cento e vinte</b> se for noutro mundo). A espera é de <b>meia hora</b>, que é a mais
 * comprida do ofício depois da do Tormentum.
 *
 * <p>Mas há um atalho, e é ele que faz delas o que são: havendo uma <b>Pedra de Caminho presa</b>
 * largada no chão a três blocos, elas vão <b>para onde ela aponta</b> em vez de para casa — por
 * <b>quarenta</b>, com uma espera de <b>um minuto</b>, e <b>gastando a pedra</b>. Quem traz um punhado de
 * pedras presas no bolso anda pelo mundo a uma frase por minuto.
 *
 * <p>E uma vez em cem a coisa sai torta: em vez de ir para onde queria, a pessoa é <b>atirada ao acaso</b>
 * até quinhentos blocos, que é o salto da Sarça do Vazio. É o original lembrando de quem são os sapatos.
 */
public final class RubySlippers {
    /** A frase. O original compara em minúsculas e sem apóstrofos, e começa por ela. */
    public static final String CHAVE = "tc.rite.noplacelikehome";

    /** A que distância uma pedra largada conta. */
    public static final double PERTO = 3.0;

    /** O que custa ir pela pedra, no mesmo mundo e noutro. */
    public static final int PEDRA_CUSTA = 40;
    public static final int PEDRA_CUSTA_LONGE = 80;

    /** E o que custa ir para a cama. */
    public static final int CAMA_CUSTA = 80;
    public static final int CAMA_CUSTA_LONGE = 120;

    /** As duas esperas: um minuto pela pedra, meia hora pela cama. */
    public static final long ESPERA_DA_PEDRA = 20L * 60L;
    public static final long ESPERA_DA_CAMA = 20L * 60L * 30L;

    /**
     * E a vez em cem em que ela sai torta.
     *
     * <p>O original manda um salto de <b>quinhentos</b>; aqui corre o mesmo {@code teleportAway} da
     * Sarça do Vazio, que já é o salto do original com o alcance dele. Fica o alcance da sarça.
     */
    public static final int TORTO_UMA_EM = 100;

    /** Quando cada uma das duas esperas acaba. */
    public static final net.fabricmc.fabric.api.attachment.v1.AttachmentType<Long> ESPEROU_PELA_PEDRA =
            net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.<Long>builder()
                    .initializer(() -> 0L)
                    .persistent(com.mojang.serialization.Codec.LONG)
                    .buildAndRegister(Thaumcraft.id("ruby_slippers_waystone"));

    public static final net.fabricmc.fabric.api.attachment.v1.AttachmentType<Long> ESPEROU_PELA_CAMA =
            net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.<Long>builder()
                    .initializer(() -> 0L)
                    .persistent(com.mojang.serialization.Codec.LONG)
                    .buildAndRegister(Thaumcraft.id("ruby_slippers_bed"));

    private RubySlippers() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar. */
    public static void init() {
    }

    /**
     * A frase dita no chat.
     *
     * @return se ela pegou, e por isso não vai para o resto da mesa
     */
    public static boolean falou(ServerPlayer quem, String oquê) {
        if (!(quem.level() instanceof ServerLevel level)) return false;
        if (!WitchFootwear.calça(quem, OccultaItems.RUBY_SLIPPERS)) return false;
        if (!começaPor(oquê, Component.translatable(CHAVE).getString())) return false;

        ItemEntity pedra = pedraPerto(level, quem);
        return pedra != null ? pelaPedra(level, quem, pedra) : paraCasa(level, quem);
    }

    /** O original compara em minúsculas, sem apóstrofos, e só exige que a fala <b>comece</b> pela frase. */
    private static boolean começaPor(String oquê, String frase) {
        String limpo = oquê.toLowerCase(java.util.Locale.ROOT).replace("'", "").replace("’", "").trim();
        String alvo = frase.toLowerCase(java.util.Locale.ROOT).replace("'", "").replace("’", "").trim();
        return !alvo.isEmpty() && limpo.startsWith(alvo);
    }

    /** A primeira Pedra de Caminho presa largada a três blocos. */
    private static @org.jetbrains.annotations.Nullable ItemEntity pedraPerto(ServerLevel level,
                                                                             ServerPlayer quem) {
        AABB volta = new AABB(quem.getX() - PERTO, quem.getY() - PERTO, quem.getZ() - PERTO,
                quem.getX() + PERTO, quem.getY() + PERTO, quem.getZ() + PERTO);
        for (ItemEntity largada : level.getEntitiesOfClass(ItemEntity.class, volta)) {
            var coisa = largada.getItem();
            if (coisa.is(OccultaItems.BOUND_WAYSTONE) && Waystones.presa(coisa)) return largada;
        }
        return null;
    }

    /** Pela pedra: quarenta, um minuto de espera, e a pedra gasta-se. */
    private static boolean pelaPedra(ServerLevel level, ServerPlayer quem, ItemEntity pedra) {
        long falta = quanto(quem, ESPEROU_PELA_PEDRA, ESPERA_DA_PEDRA, level);
        if (falta > 0L) {
            espera(quem, falta);
            return true;
        }

        var lugar = Waystones.lugar(pedra.getItem());
        boolean noutroMundo = lugar != null && lugar.mundo() != level.dimension();
        int custa = noutroMundo ? PEDRA_CUSTA_LONGE : PEDRA_CUSTA;
        if (!Infusions.tira(level, quem, custa, true)) return true;

        quem.setAttached(ESPEROU_PELA_PEDRA, level.getGameTime());
        if (level.getRandom().nextInt(TORTO_UMA_EM) == 0) {
            net.thaumcraft.occulta.BrambleBlock.atiraLonge(level, quem.blockPosition(), quem);
            pedra.discard();
            return true;
        }
        Waystones.leva(level, pedra.getItem(), quem);
        pedra.discard();
        return true;
    }

    /** E para casa: oitenta, meia hora de espera, e a cama de quem é. */
    private static boolean paraCasa(ServerLevel level, ServerPlayer quem) {
        long falta = quanto(quem, ESPEROU_PELA_CAMA, ESPERA_DA_CAMA, level);
        if (falta > 0L && !quem.getAbilities().instabuild) {
            espera(quem, falta);
            return true;
        }

        var dormiu = quem.getRespawnConfig();
        ServerLevel casa = dormiu == null ? level.getServer().overworld()
                : level.getServer().getLevel(dormiu.respawnData().dimension());
        if (casa == null) casa = level.getServer().overworld();
        BlockPos onde = dormiu == null ? casa.getRespawnData().pos() : dormiu.respawnData().pos();

        int custa = casa.dimension() == level.dimension() ? CAMA_CUSTA : CAMA_CUSTA_LONGE;
        if (!Infusions.tira(level, quem, custa, true)) return true;

        quem.setAttached(ESPEROU_PELA_CAMA, level.getGameTime());
        int alto = casa.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                onde.getX(), onde.getZ());
        quem.teleportTo(casa, onde.getX() + 0.5, Math.max(onde.getY() + 1, alto),
                onde.getZ() + 0.5, java.util.Set.of(), quem.getYRot(), quem.getXRot(), false);
        return true;
    }

    /** Quanto falta de uma das esperas, em batidas. */
    private static long quanto(ServerPlayer quem,
                               net.fabricmc.fabric.api.attachment.v1.AttachmentType<Long> qual,
                               long quanta, ServerLevel level) {
        Long quando = quem.getAttachedOrCreate(qual);
        if (quando == null || quando <= 0L) return 0L;
        long passou = level.getGameTime() - quando;
        return Math.max(0L, quanta - passou);
    }

    private static void espera(ServerPlayer quem, long falta) {
        quem.sendSystemMessage(Component.translatable("tc.rite.slippersoncooldown",
                Math.max(1L, falta / 20L / 60L)).withStyle(ChatFormatting.RED));
    }
}
