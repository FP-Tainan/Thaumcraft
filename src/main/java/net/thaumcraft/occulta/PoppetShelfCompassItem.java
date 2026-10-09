package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A <b>Bússola da Prateleira</b>: o {@code ItemPoppetShelfCompass} do Witchery.
 *
 * <p>Ela não aponta: ela <b>esquenta</b>. Tem <b>seis caras</b>, e a cara muda com a distância à
 * <b>Prateleira de Bonecas</b> mais perto — oito blocos, dezesseis, trinta e dois, sessenta e quatro,
 * cento e vinte e oito, e longe demais.
 *
 * <p>É uma ideia barata e boa, e serve para uma coisa só: quem guarda as suas bonecas numa prateleira
 * escondida precisa de as voltar a achar. É o <b>mapa do tesouro da própria casa</b>.
 *
 * <h2>Desvio declarado</h2>
 *
 * <p>A conta do original corre <b>no cliente</b>, uma vez em vinte batidas de desenho, e escreve o dano do
 * item; aqui corre no <b>servidor</b>, pela mesma razão que a da Bússola de Gente — hoje o desenho de um
 * item vem de um componente. E a cara sai pelo {@code custom_model_data}.
 *
 * <p>O original percorre a <b>lista inteira de blocos com vida do mundo</b> para achar a prateleira mais
 * perto, que em 2014 era uma lista curta. Hoje isso seria percorrer o mundo todo a cada vinte batidas, de
 * modo que a procura se faz num <b>quadrado de cento e vinte e oito blocos</b> à volta de quem a traz —
 * que é exatamente o alcance que a cara mais fraca marca. Além disso o original já dizia «longe demais».
 */
public class PoppetShelfCompassItem extends Item {
    /** As cinco distâncias, da mais perto para a mais longe, em blocos. */
    public static final int[] PERTO = {8, 16, 32, 64, 128};

    /** Uma vez em vinte batidas, como no original. */
    public static final int DE_VINTE_EM_VINTE = 20;

    public PoppetShelfCompassItem(Properties properties) {
        super(properties);
    }

    /** Qual cara este item está mostrando: zero é «longe demais», cinco é «em cima dela». */
    public static int cara(ItemStack bússola) {
        CustomModelData qual = bússola.get(DataComponents.CUSTOM_MODEL_DATA);
        if (qual == null || qual.floats().isEmpty()) return 0;
        return (int) (float) qual.floats().getFirst();
    }

    private static void cara(ItemStack bússola, int qual) {
        if (cara(bússola) == qual) return;
        bússola.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of((float) qual), List.of(), List.of(), List.of()));
    }

    /** Qual cara aquela distância dá: o escadinha do original, do mais perto para o mais longe. */
    public static int caraDe(double distância) {
        for (int n = 0; n < PERTO.length; n++) {
            if (distância < PERTO[n]) return PERTO.length - n;
        }
        return 0;
    }

    @Override
    public void inventoryTick(ItemStack bússola, ServerLevel level, Entity quem, @Nullable
                              net.minecraft.world.entity.EquipmentSlot casa) {
        if (level.getGameTime() % DE_VINTE_EM_VINTE != 7L) return;
        conta(bússola, level, quem);
    }

    /** A conta, feita fora do relógio para se poder pedir. */
    public static void conta(ItemStack bússola, ServerLevel level, Entity quem) {
        cara(bússola, caraDe(maisPerto(level, quem.blockPosition())));
    }

    /**
     * A que distância está a prateleira mais perto, ou infinito.
     *
     * <p>A conta é a do original: a distância conta-se <b>no chão</b>, e não no ar. Uma prateleira no porão
     * e outra ao lado, à mesma distância a pé, valem o mesmo.
     *
     * <p>Só nos pedaços de mundo <b>já carregados</b>: uma prateleira num pedaço que ninguém está vendo não
     * se acha, e é o mesmo que o original fazia sem o dizer — a lista dele era a dos blocos com vida que
     * estavam a bater.
     */
    public static double maisPerto(ServerLevel level, BlockPos onde) {
        int alcance = PERTO[PERTO.length - 1];
        int deX = (onde.getX() - alcance) >> 4;
        int atéX = (onde.getX() + alcance) >> 4;
        int deZ = (onde.getZ() - alcance) >> 4;
        int atéZ = (onde.getZ() + alcance) >> 4;

        double menor = Double.MAX_VALUE;
        for (int cx = deX; cx <= atéX; cx++) {
            for (int cz = deZ; cz <= atéZ; cz++) {
                var pedaço = level.getChunkSource().getChunkNow(cx, cz);
                if (pedaço == null) continue;
                for (var lá : pedaço.getBlockEntities().entrySet()) {
                    if (!(lá.getValue() instanceof PoppetShelfBlockEntity)) continue;
                    double dx = lá.getKey().getX() - onde.getX();
                    double dz = lá.getKey().getZ() - onde.getZ();
                    double quanto = Math.sqrt(dx * dx + dz * dz);
                    if (quanto < menor) menor = quanto;
                }
            }
        }
        return menor;
    }
}
