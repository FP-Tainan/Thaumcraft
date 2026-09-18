package net.thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * A varinha do taumaturgo, com as contas da 4.2.3.5.
 *
 * <p>Ela é feita de duas coisas: a haste, que manda em quanto ela guarda, e as pontas, que mandam em
 * quanto cada uso custa. O vis é contado em centésimos, como no original — uma haste de vinte e cinco
 * guarda dois mil e quinhentos, e é por isso que mexer com ela nunca dá números redondos.
 *
 * <p>Para enchê-la, aponta-se para um nó de aura e segura-se o botão: a cada vez ela puxa um ponto de um
 * aspecto que ainda caiba nela, e o nó vai secando. As hastes feitas de coisa primordial — obsidiana,
 * blaze, gelo, quartzo, osso e junco — recolhem sozinhas um pouquinho do aspecto delas, mas só até um
 * décimo do que cabem.
 */
public class WandItem extends Item {
    /** O original guarda vis em centésimos: um ponto de vis são cem no disco. */
    public static final int VIS_UNIT = 100;
    /** Até onde a varinha alcança um nó. */
    private static final double REACH = 5.0;
    /** As hastes primordiais só se enchem sozinhas até um décimo do que cabem. */
    private static final int SELF_FILL_SHARE = 10;

    private final boolean staff;

    public WandItem(Properties properties, boolean staff) {
        super(properties);
        this.staff = staff;
    }

    // ----------------------------------------------------------------- as peças

    public static String rodTag(ItemStack stack) {
        return stack.getOrDefault(TCComponents.WAND_ROD, "wood");
    }

    public static String capTag(ItemStack stack) {
        return stack.getOrDefault(TCComponents.WAND_CAP, "iron");
    }

    public static WandParts.Rod rod(ItemStack stack) {
        WandParts.Rod found = WandParts.rod(rodTag(stack));
        return found != null ? found : WandParts.rod("wood");
    }

    public static WandParts.Cap cap(ItemStack stack) {
        WandParts.Cap found = WandParts.cap(capTag(stack));
        return found != null ? found : WandParts.cap("iron");
    }

    /** Quanto a varinha comporta de cada aspecto, em centésimos. */
    public static int maxVis(ItemStack stack) {
        return rod(stack).capacity() * VIS_UNIT;
    }

    // ----------------------------------------------------------------- o vis

    public static AspectList vis(ItemStack stack) {
        return stack.getOrDefault(TCComponents.WAND_VIS, new AspectList()).copy();
    }

    /** Quanto a varinha tem daquele aspecto, em centésimos. */
    public static int vis(ItemStack stack, Aspect aspect) {
        return vis(stack).getAmount(aspect);
    }

    private static void setVis(ItemStack stack, AspectList list) {
        stack.set(TCComponents.WAND_VIS, list);
    }

    /**
     * Guarda vis na varinha e devolve o que não coube.
     *
     * @param amount quanto, em pontos inteiros — a conta em centésimos é feita aqui
     */
    public static int addVis(ItemStack stack, Aspect aspect, int amount) {
        AspectList list = vis(stack);
        int room = maxVis(stack) - list.getAmount(aspect);
        int given = Math.min(room, amount * VIS_UNIT);
        if (given <= 0) return amount;
        list.add(aspect, given);
        setVis(stack, list);
        return amount - given / VIS_UNIT;
    }

    /**
     * Gasta vis contado em centésimos, sem a conta de pontos inteiros.
     *
     * <p>É o que os focos usam: no original o custo deles já vem nessa moeda miúda, porque eles cobram a
     * cada tique e um ponto inteiro por tique seria um estouro.
     */
    public static boolean consumeRaw(ItemStack stack, AspectList cost, boolean reallyDoIt) {
        AspectList list = vis(stack);
        WandParts.Cap cap = cap(stack);
        AspectList real = new AspectList();
        for (Aspect aspect : cost.getAspects()) {
            // cada ponteira cobra o seu tanto, e as de cobre e prata cobram menos de uns aspectos
            int needed = Math.max(1, (int) (cost.getAmount(aspect) * cap.discount(aspect)));
            if (list.getAmount(aspect) < needed) return false;
            real.add(aspect, needed);
        }
        if (!reallyDoIt) return true;
        for (Aspect aspect : real.getAspects()) list.reduce(aspect, real.getAmount(aspect));
        setVis(stack, list);
        return true;
    }

    /** Os aspectos primários em que ainda cabe alguma coisa. */
    public static List<Aspect> aspectsWithRoom(ItemStack stack) {
        List<Aspect> room = new ArrayList<>();
        AspectList list = vis(stack);
        for (Aspect aspect : Aspects.primals()) {
            if (list.getAmount(aspect) < maxVis(stack)) room.add(aspect);
        }
        return room;
    }

    /**
     * Gasta vis, se houver.
     *
     * <p>O desconto das pontas entra aqui: a de ferro cobra dez por cento a mais, a de vazio cobra vinte
     * por cento a menos. O custo vem em pontos inteiros e vira centésimos na conta.
     */
    public static boolean consume(ItemStack stack, AspectList cost, boolean reallyDoIt) {
        AspectList list = vis(stack);
        WandParts.Cap cap = cap(stack);
        AspectList real = new AspectList();
        for (Aspect aspect : cost.getAspects()) {
            int needed = (int) (cost.getAmount(aspect) * VIS_UNIT * cap.discount(aspect));
            if (list.getAmount(aspect) < needed) return false;
            real.add(aspect, needed);
        }
        if (!reallyDoIt) return true;
        for (Aspect aspect : real.getAspects()) list.reduce(aspect, real.getAmount(aspect));
        setVis(stack, list);
        return true;
    }

    // ----------------------------------------------------------------- o uso

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // o nó na mira vem antes do foco, como no getObjectInUse do original: com um nó na frente a
        // varinha bebe dele mesmo com foco preso; sem nó, o botão aciona o foco
        if (nodeInSight(level, player) != null) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        FocusItem held = net.thaumcraft.item.Focuses.on(stack);
        if (held != null) {
            if (!held.isContinuous()) {
                // tiro único: sai de uma vez, sem segurar
                if (!level.isClientSide()) net.thaumcraft.item.Focuses.tick(level, player, stack, held);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        // nos tubos, a varinha abre e fecha lados e gira o que tem sentido
        InteractionResult tube = TubeWand.use(context);
        if (tube != null) return tube;
        // o que a varinha faz batendo num bloco: é assim que o original entrega as primeiras peças
        return WandTriggers.use(context.getLevel(), context.getPlayer(), context.getClickedPos(),
                context.getItemInHand());
    }

    /**
     * A varinha não abaixa na mão quando o vis dela muda.
     *
     * <p>Bebendo de um nó, o vis muda a cada cinco tiques; e o jogo, vendo a varinha com outros dados,
     * tomava aquilo por troca de item e tocava a animação de abaixar e levantar o braço — a varinha
     * sumia da mão justamente enquanto bebia. No original ela fica firme, apontada para o nó.
     */
    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack,
                                                  ItemStack newStack) {
        return false;
    }

    @Override
    public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack stack) {
        // o original usa a pose do arco (EnumAction.bow): em primeira pessoa a varinha vem para o meio da
        // tela, e de fora os dois braços se erguem apontando para a frente
        return net.minecraft.world.item.ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack, int remaining) {
        if (!(entity instanceof Player player)) return;
        // primeiro o nó na mira, depois o foco — a ordem do onUsingTick do original
        NodeBlockEntity node = nodeInSight(level, player);
        if (node != null) {
            if (level.isClientSide()) return;
            // a cada cinco tiques a varinha dá mais um gole no nó, como no original
            if (remaining % 5 == 0) drain(stack, node, level);
            return;
        }
        FocusItem focus = net.thaumcraft.item.Focuses.on(stack);
        if (focus != null) {
            // dos dois lados, como o onUsingFocusTick do original: o servidor age, quem vê desenha
            if (!net.thaumcraft.item.Focuses.tick(level, player, stack, focus)) player.stopUsingItem();
            return;
        }
        if (!level.isClientSide()) player.stopUsingItem();
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity,
                                int remaining) {
        // soltou o botão: a escavação esquece o bloco que estava roendo
        if (entity instanceof Player player) net.thaumcraft.item.Focuses.stop(player);
        return false;
    }

    /**
     * Um gole no nó: um ponto de um aspecto que ainda caiba na varinha.
     *
     * <p>No original o aspecto é sorteado entre os que têm lugar, e não o mais cheio nem o mais vazio.
     */
    private static void drain(ItemStack stack, NodeBlockEntity node, Level level) {
        List<Aspect> room = aspectsWithRoom(stack);
        List<Aspect> possible = new ArrayList<>();
        for (Aspect aspect : room) {
            if (node.aspects().getAmount(aspect) > 0) possible.add(aspect);
        }
        if (possible.isEmpty()) return;
        Aspect chosen = possible.get(level.getRandom().nextInt(possible.size()));
        if (!node.take(chosen, 1)) return;
        addVis(stack, chosen, 1);
        level.playSound(null, node.getBlockPos(), net.thaumcraft.registry.TCSounds.WAND.value(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.3f, 1.2f + level.getRandom().nextFloat() * 0.3f);
        node.drained(chosen);
    }

    /**
     * O rastro de quem está bebendo de um nó.
     *
     * <p>No original o nó drenado solta faíscas na cor do aspecto que está saindo, e elas se veem mesmo
     * sem os óculos — é o que denuncia um nó que alguém está secando.
     */

    /** O nó na mira de quem segura a varinha. */
    public static NodeBlockEntity nodeInSight(Level level, Player player) {
        net.minecraft.world.phys.HitResult hit = player.pick(REACH, 1.0f, false);
        if (!(hit instanceof net.minecraft.world.phys.BlockHitResult block)) return null;
        BlockPos pos = block.getBlockPos();
        return level.getBlockEntity(pos) instanceof NodeBlockEntity node ? node : null;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity,
                              net.minecraft.world.entity.EquipmentSlot slot) {
        if (!(entity instanceof Player player)) return;
        WandParts.Rod rod = rod(stack);
        int limit = maxVis(stack) / SELF_FILL_SHARE;

        if (rod.primal() != null) {
            // a haste do aspecto recolhe um ponto a cada dez segundos, enquanto estiver por baixo do décimo
            if (player.tickCount % 200 == 0 && vis(stack, rod.primal()) < limit) {
                addVis(stack, rod.primal(), 1);
            }
            return;
        }
        if (!rod.anyPrimal()) return;
        // a haste primordial recolhe qualquer um dos seis, e bem mais depressa
        if (player.tickCount % 50 != 0) return;
        List<Aspect> hungry = new ArrayList<>();
        for (Aspect aspect : Aspects.primals()) {
            if (vis(stack, aspect) < limit) hungry.add(aspect);
        }
        if (hungry.isEmpty()) return;
        addVis(stack, hungry.get(level.getRandom().nextInt(hungry.size())), 1);
    }

    @Override
    public Component getName(ItemStack stack) {
        // "Varinha de X com pontas de Y", que é como o original monta o nome
        Component name = Component.translatable(
                this.staff ? "item.thaumcraft.staff.named" : "item.thaumcraft.wand.named",
                Component.translatable("tc.rod." + rodTag(stack)),
                Component.translatable("tc.cap." + capTag(stack)));
        String focus = stack.get(TCComponents.WAND_FOCUS);
        if (focus == null) return name;
        return Component.translatable("item.thaumcraft.wand.focused", name,
                Component.translatable("item.thaumcraft.focus." + focus));
    }

    public boolean isStaff() {
        return this.staff;
    }
}
