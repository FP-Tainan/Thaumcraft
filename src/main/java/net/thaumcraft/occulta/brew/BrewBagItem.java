package net.thaumcraft.occulta.brew;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.kettle.KettleBrewItem;

/**
 * A <b>Bolsa de Cozimentos</b>: o {@code ItemBrewBag} do Witchery.
 *
 * <p>São <b>oito casas</b>, e nenhuma delas se escolhe com o rato. Segura-se o botão e <b>mexe-se a
 * cabeça</b> para um dos oito rumos — cima, cima-direita, direita, e assim à roda —, e o frasco daquele
 * rumo é o que sai quando se larga. É a mesma mão da <b>Vara Mística</b>, e é de propósito: o original deu
 * à bolsa a mesma interface dos símbolos, para que quem já sabe desenhar já saiba atirar.
 *
 * <p>Por isso a bolsa tem <b>duas caras</b>. Segurando-a de pé, ela é uma <b>roda de oito</b> desenhada na
 * tela, com os frascos à volta; <b>agachado</b>, ela abre como uma arca de oito casas, para se arrumar o
 * que está dentro.
 *
 * <p>E <b>só cozimento de atirar entra</b>: o frasco do caldeirão que leva pólvora, e os frascos do
 * Caldeirão de Pote, que são todos de atirar. Um cozimento de beber não cabe — a bolsa não é mochila.
 *
 * <h2>Os oito rumos</h2>
 *
 * <p>O que torna isto jogável é que os oito rumos <b>não se medem todos igual</b>. Uma diagonal pede
 * <b>três graus e meio</b> em cada eixo; um rumo reto pede <b>sete</b> num e quase nada no outro. E as
 * diagonais perguntam-se <b>primeiro</b>: sem isso, qualquer tentativa de ir ao canto sairia como o lado,
 * porque um dos dois eixos chega sempre aos sete antes do outro.
 *
 * <p>As casas correm <b>à roda do relógio a partir do cima</b>, e é essa a ordem em que elas aparecem na
 * tela e na bolsa aberta.
 */
public class BrewBagItem extends Item {
    /** As oito casas dela. */
    public static final int CASAS = 8;

    /** Quanto tempo ela se deixa segurar: as trinta e seis mil batidas do original. */
    public static final int SEGURA = 36000;

    /** Quanto giro pede um rumo reto, e quanto pede uma diagonal. */
    public static final float GIRO_RETO = 7.0f;
    public static final float GIRO_DIAGONAL = 3.5f;

    /**
     * A cor em que ela nasce: o <b>cacau</b> do original ({@code Dye.COCOA_BEANS.rgb}).
     *
     * <p>E ela tinge-se, como uma armadura de couro: o original escreve <b>uma receita por tinta</b>, e
     * aqui basta a receita de tingir do jogo, que já junta as dezesseis — e as mistura, o que o original
     * também fazia.
     */
    public static final int CACAU = 0x4D2B15;

    /**
     * Os oito <b>traços</b> do original, pelos números dele, e a casa de cada um.
     *
     * <p>Os números dos traços são os da Vara Mística — {@code 0} cima, {@code 1} baixo, {@code 2} direita,
     * {@code 3} esquerda, e as quatro diagonais de {@code 4} a {@code 7} —, e a tabela
     * {@code STROKE_TO_INDEX} diz em que casa cada um cai. Posta por extenso, ela é a <b>roda do
     * relógio</b>: cima é a casa zero, cima-direita a um, direita a dois, e assim por diante.
     */
    public static final int[] TRAÇO_PARA_CASA = {0, 4, 2, 6, 1, 5, 7, 3};

    /** E o caminho de volta, que é o que a roda da tela lê. */
    public static final int[] CASA_PARA_TRAÇO = {0, 4, 2, 7, 1, 5, 3, 6};

    public BrewBagItem(Properties properties) {
        super(properties);
    }

    /** O feitio dela: uma só, e da cor do cacau enquanto ninguém a tingir. */
    public static Properties feitio(Properties properties) {
        return properties.stacksTo(1)
                .component(DataComponents.DYED_COLOR,
                        new net.minecraft.world.item.component.DyedItemColor(0xFF000000 | CACAU));
    }

    // ------------------------------------------------------------------ o que ela leva dentro

    /** O que está dentro dela, uma pilha por casa. */
    public static NonNullList<ItemStack> dentro(ItemStack bolsa) {
        NonNullList<ItemStack> lista = NonNullList.withSize(CASAS, ItemStack.EMPTY);
        bolsa.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(lista);
        return lista;
    }

    /** E põe de volta. */
    public static void guarda(ItemStack bolsa, NonNullList<ItemStack> lista) {
        bolsa.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(lista));
    }

    /** O frasco que está naquela casa, ou nada. */
    public static ItemStack naCasa(ItemStack bolsa, int qual) {
        if (qual < 0 || qual >= CASAS) return ItemStack.EMPTY;
        return dentro(bolsa).get(qual);
    }

    /**
     * <b>Só cozimento de atirar entra</b>: o {@code SlotBrewBag.isBrew} do original.
     *
     * <p>São dois: o frasco do <b>Caldeirão de Bruxa</b> que leva pólvora dentro, e qualquer frasco do
     * <b>Caldeirão de Pote</b>, que são todos de atirar.
     */
    public static boolean cabe(ItemStack oquê) {
        if (oquê.getItem() instanceof KettleBrewItem) return true;
        return oquê.getItem() instanceof BrewItem && Brew.splash(BrewItem.contents(oquê));
    }

    /**
     * Que <b>traço</b> é este giro, ou {@code -1} se ainda não é nenhum.
     *
     * <p>A ordem das perguntas é a do original e ela <b>faz</b> os oito rumos: as quatro diagonais
     * primeiro, pelo limiar menor, e só depois os quatro rumos retos. Invertida, um giro que passasse dos
     * sete nos dois eixos sairia como o eixo que fosse perguntado antes, e o canto nunca sairia.
     *
     * <p>Vive aqui, e não com quem o chama, porque é a <b>conta</b> da bolsa e não o desenho dela.
     */
    public static int qualTraço(float pitch, float yaw) {
        if (pitch >= GIRO_DIAGONAL && yaw <= -GIRO_DIAGONAL) return 4;
        if (pitch >= GIRO_DIAGONAL && yaw >= GIRO_DIAGONAL) return 6;
        if (pitch <= -GIRO_DIAGONAL && yaw <= -GIRO_DIAGONAL) return 7;
        if (pitch <= -GIRO_DIAGONAL && yaw >= GIRO_DIAGONAL) return 5;

        if (pitch >= GIRO_RETO) return 0;
        if (pitch <= -GIRO_RETO) return 1;
        if (yaw <= -GIRO_RETO) return 2;
        if (yaw >= GIRO_RETO) return 3;
        return -1;
    }

    // ------------------------------------------------------------------ a mão

    /**
     * Ela não se come nem se bebe: a mão fica <b>parada</b>, como no original, que não escreve
     * {@code getItemUseAction} nenhum.
     */
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack oquê) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack oquê, LivingEntity quem) {
        return SEGURA;
    }

    /**
     * De pé, <b>começa o traço</b>; agachado, <b>abre</b>.
     *
     * <p>São as duas caras dela, e é o agachar que escolhe entre elas — o mesmo agachar que, no meio de um
     * traço, o <b>desfaz</b>.
     */
    @Override
    public InteractionResult use(Level mundo, Player quem, InteractionHand mão) {
        // o original não conhece a mão esquerda, e a bolsa aberta escreve-se de volta na casa escolhida
        if (mão != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (quem.isShiftKeyDown()) {
            if (quem instanceof ServerPlayer gente) BrewBagMenu.abre(gente, gente.getItemInHand(mão));
            return InteractionResult.SUCCESS;
        }
        if (quem instanceof ServerPlayer gente) BrewBagChoice.esquece(gente);
        quem.startUsingItem(mão);
        return InteractionResult.CONSUME;
    }

    /**
     * <b>Largá-la atira o frasco escolhido.</b>
     *
     * <p>Quem escolheu foi o lado de cá, que é onde a cabeça se mexe; o servidor guardou o número da casa e
     * só agora olha para ele. Não havendo escolha nenhuma, não acontece <b>nada</b>, e em silêncio: largar a
     * bolsa sem ter mexido a cabeça não é um erro, é não ter pedido nada.
     *
     * <p>E <b>agachado não sai nada</b>, porque agachar é o jeito de desistir.
     */
    @Override
    public boolean releaseUsing(ItemStack bolsa, Level mundo, LivingEntity quem, int faltam) {
        if (!(mundo instanceof ServerLevel level) || !(quem instanceof ServerPlayer gente)) return false;
        Integer casa = BrewBagChoice.escolhida(gente);
        BrewBagChoice.esquece(gente);
        if (casa == null || gente.isShiftKeyDown()) return false;

        /*
         * O original ainda se defende de uma casa negativa aqui, com o recado de «não desenhou nada que
         * conheça». Ela não chega a acontecer — o recado da escolha só se manda quando há frasco —, mas a
         * defesa fica, porque o dia em que alguém mandar um recado torto é o dia em que ela serve.
         */
        if (casa < 0 || casa >= CASAS) {
            gente.sendSystemMessage(Component.translatable("tc.occulta.infuse.branch.unknownsymbol")
                    .withStyle(ChatFormatting.RED));
            level.playSound(null, gente.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            return false;
        }

        NonNullList<ItemStack> tem = dentro(bolsa);
        ItemStack frasco = tem.get(casa);
        if (frasco.isEmpty()) return false;

        atira(level, gente, frasco.copyWithCount(1));
        if (!gente.hasInfiniteMaterials()) {
            frasco.shrink(1);
            tem.set(casa, frasco);
            guarda(bolsa, tem);
        }
        return false;
    }

    /**
     * <b>Atira um frasco</b> — o do caldeirão ou o do pote, cada um com o seu arremesso.
     *
     * <p>O som é o mesmo dos dois, e é o do <b>arco</b>: é assim no original, e é o que faz a bolsa soar
     * como uma arma e não como uma poção.
     */
    private static void atira(ServerLevel level, ServerPlayer quem, ItemStack frasco) {
        level.playSound(null, quem.getX(), quem.getY(), quem.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (frasco.getItem() instanceof KettleBrewItem) {
            net.minecraft.world.entity.projectile.Projectile.spawnProjectileFromRotation(
                    net.thaumcraft.occulta.kettle.KettleBrewProjectile::new, level, frasco, quem,
                    0.0f, 0.5f, 1.0f);
            return;
        }
        var atirado = new BrewProjectile(level, quem, frasco);
        atirado.shootFromRotation(quem, quem.getXRot(), quem.getYRot(), -20.0f, 0.5f, 1.0f);
        level.addFreshEntity(atirado);
    }

    /** A bolsa que esta pessoa tem na mão direita, ou nada. */
    public static ItemStack naMão(Player quem) {
        ItemStack principal = quem.getMainHandItem();
        return principal.getItem() instanceof BrewBagItem ? principal : ItemStack.EMPTY;
    }

    /** A cor dela: a que lhe tingiram, ou o cacau. */
    public static int cor(ItemStack bolsa) {
        var tinta = bolsa.get(DataComponents.DYED_COLOR);
        return tinta == null ? CACAU : tinta.rgb() & 0xFFFFFF;
    }

    /** Uma bolsa vazia, para quem precisar de uma. */
    public static ItemStack nova() {
        return new ItemStack(OccultaItems.BREW_BAG);
    }
}
