package net.thaumcraft.shattered.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.client.fx.FocusEffects;
import net.thaumcraft.client.fx.LightningBolt;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.shattered.RiftBladeItem;

/**
 * O que os três focos de fenda desenham do lado de quem joga.
 *
 * <p>Quem manda pediu: <i>o mesmo do foco de choque, mas em raios da cor da fenda para abrir e fechar, e branco
 * para firmar, que remete ao Ordo</i>. É isso — o mesmo raio do {@code FXLightningBolt} da 4.2.3.5, com o mesmo
 * jeito de sair da mão e o mesmo esfarelar de fagulhas no alvo, só que pintado de outra cor.
 *
 * <p>Os sete tipos de raio do original ficaram como estavam; a cor daqui entra pelo {@code setColours}.
 */
public final class ShatteredFx {
    /** O roxo-azulado da fenda: a passada larga e fraca, e a fina e forte. */
    private static final float[] FENDA_LARGA = {0.20f, 0.10f, 0.55f};
    private static final float[] FENDA_FINA = {0.58f, 0.45f, 1.0f};

    /** E o branco do Ordo, para firmar. */
    private static final float[] ORDO_LARGA = {0.72f, 0.72f, 0.78f};
    private static final float[] ORDO_FINA = {1.0f, 1.0f, 1.0f};

    /** Até onde o raio procura o que mirar. */
    private static final double REACH = 20.0;

    private ShatteredFx() {
    }

    public static void init() {
        Focuses.registerClient("rift_open", (level, quem, varinha, foco) -> raio(level, quem, FENDA_LARGA, FENDA_FINA));
        Focuses.registerClient("rift_close", (level, quem, varinha, foco) -> raio(level, quem, FENDA_LARGA, FENDA_FINA));
        Focuses.registerClient("rift_hold", (level, quem, varinha, foco) -> raio(level, quem, ORDO_LARGA, ORDO_FINA));
    }

    /**
     * O raio da mão até onde a varinha aponta, e as fagulhas onde ele bate.
     *
     * <p>A mira é a mesma dos focos: primeiro a fenda na linha de visão — que não tem corpo e o raio do mouse
     * atravessa —, depois o bloco em que se acertou, e, se nada houver, um ponto lá adiante no ar.
     */
    private static boolean raio(Level level, Player quem, float[] larga, float[] fina) {
        Vec3 base = FocusEffects.hand(quem);
        Vec3 alvo = mira(level, quem);
        var sorte = level.getRandom();

        for (int i = 0; i < 5; i++) {
            Sparkle.spawn(sorte,
                    alvo.x + (sorte.nextFloat() - sorte.nextFloat()) * 0.3f,
                    alvo.y + (sorte.nextFloat() - sorte.nextFloat()) * 0.3f,
                    alvo.z + (sorte.nextFloat() - sorte.nextFloat()) * 0.3f,
                    2.0f + sorte.nextFloat(), 2, 0.05f + sorte.nextFloat() * 0.05f);
        }

        // os mesmos números do foco de choque: seis tiques de vida, oito segmentos por tique
        LightningBolt raio = new LightningBolt(base.x, base.y, base.z, alvo.x, alvo.y, alvo.z,
                sorte.nextLong(), 6, 0.5f, 8);
        raio.defaultFractal();
        raio.setWidth(0.125f);
        raio.setColours(larga, fina);
        raio.finalizeBolt();
        return true;
    }

    /** Onde o raio vai bater. */
    private static Vec3 mira(Level level, Player quem) {
        BlockPos fenda = RiftBladeItem.riftAimedAt(level, quem);
        if (fenda != null) return Vec3.atCenterOf(fenda);
        HitResult bateu = Focuses.targetBlock(level, quem);
        if (bateu.getType() != HitResult.Type.MISS) return bateu.getLocation();
        return quem.getEyePosition().add(quem.getViewVector(1.0f).scale(REACH));
    }
}
