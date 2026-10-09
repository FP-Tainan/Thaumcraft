package net.thaumcraft.occulta.clothes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>calçado do ofício</b>: os três pares de sapatos do {@code ItemWitchesClothes} do Witchery.
 *
 * <p>São três, e os três são a mesma ideia levada a sítios diferentes: um par de sapatos que <b>muda o
 * chão por onde se passa</b>.
 *
 * <ul>
 *   <li>as <b>Chinelas de Gelo</b> congelam a água e viram a lava em obsidiana — e a lava gasta-as;</li>
 *   <li>os <b>Sapatos Escorridos</b> tiram o veneno de quem os traz e o <b>despejam no chão</b>, que
 *       cresce com ele;</li>
 *   <li>e as <b>Chinelas de Rubi</b>, que não mudam nada: dizem-se três palavras e elas levam quem as
 *       traz para casa.</li>
 * </ul>
 */
public final class WitchFootwear {
    // ------------------------------------------------------------------ as Chinelas de Gelo

    /** As quatro casas em volta dos pés que elas tocam: o {@code for (i = 0; i < 4; i++)} do original. */
    public static final int CANTOS = 4;

    /** E de quantas em quantas vezes a lava as gasta: uma em dez. */
    public static final int LAVA_GASTA_UMA_EM = 10;

    // ------------------------------------------------------------------ os Sapatos Escorridos

    /** O que o veneno aduba em volta: três de raio, e um acima e um abaixo dos pés. */
    public static final int ADUBA_RAIO = 3;
    public static final double ADUBA_RAIO_AO_QUADRADO = 9.0;
    public static final int ADUBA_ALTURA = 1;

    private WitchFootwear() {
    }

    /** Se aquela pessoa traz aquele calçado. */
    public static boolean calça(ServerPlayer quem, net.minecraft.world.item.Item qual) {
        return quem.getItemBySlot(EquipmentSlot.FEET).is(qual);
    }

    /**
     * <b>Chinelas de Gelo</b>: o {@code handleIcySlippersEffect}.
     *
     * <p>As quatro casas de chão em volta dos pés: a <b>água</b> vira gelo e a <b>lava</b> vira obsidiana.
     * E é a lava que lhes custa — uma vez em dez, o par apanha um ponto de desgaste.
     *
     * <p>Repare na diferença para o Caminhar no Gelo do jogo: este não põe gelo frágil que derrete, põe
     * <b>gelo</b>, e a obsidiana que faz da lava fica. Quem anda com elas deixa um rasto que não se
     * desfaz.
     */
    public static void gelo(ServerLevel level, ServerPlayer quem) {
        if (!calça(quem, OccultaItems.ICY_SLIPPERS)) return;
        ItemStack sapatos = quem.getItemBySlot(EquipmentSlot.FEET);
        int y = net.minecraft.util.Mth.floor(quem.getY() - 1.0);

        for (int canto = 0; canto < CANTOS; canto++) {
            int x = net.minecraft.util.Mth.floor(quem.getX() + (canto % 2 * 2 - 1) * 0.5f);
            int z = net.minecraft.util.Mth.floor(quem.getZ() + (canto / 2 % 2 * 2 - 1) * 0.5f);
            BlockPos casa = new BlockPos(x, y, z);
            BlockState feitio = level.getBlockState(casa);

            if (feitio.is(Blocks.WATER)) {
                level.setBlockAndUpdate(casa, Blocks.ICE.defaultBlockState());
            } else if (feitio.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(casa, Blocks.OBSIDIAN.defaultBlockState());
                if (level.getRandom().nextInt(LAVA_GASTA_UMA_EM) == 0) {
                    sapatos.hurtAndBreak(1, quem, EquipmentSlot.FEET);
                }
            }
        }
    }

    /**
     * <b>Sapatos Escorridos</b>: o {@code handleSeepingShoesEffect}.
     *
     * <p>Com os pés no chão, eles tiram o <b>veneno</b> e o <b>definhar</b> de quem os traz — e o que
     * tiraram não se perde: <b>escorre</b> para o chão à volta, três de raio, e aduba tudo o que lá
     * estiver como se fosse farinha de osso.
     *
     * <p>É a melhor ideia do calçado do mod: o veneno não se cura, muda de dono. Quem os traz anda a
     * fazer um jardim do que o envenenou.
     */
    public static void escorre(ServerLevel level, ServerPlayer quem) {
        if (!quem.onGround()) return;
        if (!calça(quem, OccultaItems.SEEPING_SHOES)) return;

        boolean tirou = false;
        if (quem.hasEffect(MobEffects.POISON)) {
            quem.removeEffect(MobEffects.POISON);
            tirou = true;
        }
        if (quem.hasEffect(MobEffects.WITHER)) {
            quem.removeEffect(MobEffects.WITHER);
            tirou = true;
        }
        if (!tirou) return;

        BlockPos pés = quem.blockPosition();
        for (int x = pés.getX() - ADUBA_RAIO; x <= pés.getX() + ADUBA_RAIO; x++) {
            for (int z = pés.getZ() - ADUBA_RAIO; z <= pés.getZ() + ADUBA_RAIO; z++) {
                for (int y = pés.getY() - ADUBA_ALTURA; y <= pés.getY() + ADUBA_ALTURA; y++) {
                    double dx = x - pés.getX();
                    double dz = z - pés.getZ();
                    if (dx * dx + dz * dz > ADUBA_RAIO_AO_QUADRADO) continue;
                    BlockPos casa = new BlockPos(x, y, z);
                    if (level.getBlockState(casa).isAir()) continue;
                    if (!level.getBlockState(casa.above()).isAir()) continue;
                    BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, casa);
                }
            }
        }
        level.playSound(null, pés, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.5f, 1.0f);
    }
}
