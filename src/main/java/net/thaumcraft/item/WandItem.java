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

    /** O {@code getFocusItem}: o foco preso, como item, com as melhorias dele; vazio se não houver. */
    public static ItemStack focusStack(ItemStack wand) {
        FocusItem focus = Focuses.on(wand);
        if (focus == null) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(focus);
        var upgrades = wand.get(TCComponents.FOCUS_UPGRADES);
        if (upgrades != null) out.set(TCComponents.FOCUS_UPGRADES, upgrades);
        return out;
    }

    /** O {@code getFocusPotency}: a potência do foco, mais um no bastão primordial (o das runas). */
    public static int focusPotency(ItemStack wand) {
        ItemStack focus = focusStack(wand);
        return focus.isEmpty() ? 0 : FocusItem.level(focus, FocusUpgradeTable.POTENCY) + (rod(wand).runes() ? 1 : 0);
    }

    public static int focusTreasure(ItemStack wand) {
        return FocusItem.level(focusStack(wand), FocusUpgradeTable.TREASURE);
    }

    public static int focusFrugal(ItemStack wand) {
        return FocusItem.level(focusStack(wand), FocusUpgradeTable.FRUGAL);
    }

    public static int focusEnlarge(ItemStack wand) {
        return FocusItem.level(focusStack(wand), FocusUpgradeTable.ENLARGE);
    }

    public static int focusExtend(ItemStack wand) {
        return FocusItem.level(focusStack(wand), FocusUpgradeTable.EXTEND);
    }

    // o WandManager.setCooldown do original: a espera entre usos do foco, por criatura e por lado
    private static final java.util.Map<Integer, Long> COOLDOWN_CLIENT = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, Long> COOLDOWN_SERVER = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean isOnCooldown(net.minecraft.world.entity.LivingEntity entity) {
        var map = entity.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER;
        Long until = map.get(entity.getId());
        return until != null && until > System.currentTimeMillis();
    }

    public static void setCooldown(net.minecraft.world.entity.LivingEntity entity, int millis) {
        if (millis == 0) {
            COOLDOWN_CLIENT.remove(entity.getId());
            COOLDOWN_SERVER.remove(entity.getId());
        } else {
            (entity.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER).put(entity.getId(), System.currentTimeMillis() + millis);
        }
    }

    /**
     * O tiro único do {@code onItemRightClick}: só fora da espera, que começa já ao disparar; o servidor age.
     * Diz se o foco saiu.
     */
    public static boolean cast(Level level, Player player, ItemStack wand, FocusItem focus) {
        if (isOnCooldown(player)) return false;
        setCooldown(player, focus.cooldown(focusStack(wand)));
        return level.isClientSide() || net.thaumcraft.item.Focuses.tick(level, player, wand, focus);
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

    public static void setVis(ItemStack stack, AspectList list) {
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
        return consumeRaw(stack, cost, reallyDoIt, null);
    }

    public static boolean consumeRaw(ItemStack stack, AspectList cost, boolean reallyDoIt, @org.jetbrains.annotations.Nullable Player player) {
        return consumeRaw(stack, cost, reallyDoIt, player, 0.0f);
    }

    /**
     * O que o foco gasta: o mesmo, com o desconto das melhorias frugais do foco preso (um décimo por nível), que o
     * original só dá fora da fabricação.
     */
    public static boolean consumeFocus(ItemStack stack, AspectList cost, boolean reallyDoIt, @org.jetbrains.annotations.Nullable Player player) {
        return consumeRaw(stack, cost, reallyDoIt, player, focusFrugal(stack) / 10.0f);
    }

    private static boolean consumeRaw(ItemStack stack, AspectList cost, boolean reallyDoIt, @org.jetbrains.annotations.Nullable Player player,
                                      float discount) {
        AspectList list = vis(stack);
        WandParts.Cap cap = cap(stack);
        AspectList real = new AspectList();
        for (Aspect aspect : cost.getAspects()) {
            // cada ponteira cobra o seu tanto, e as de cobre e prata cobram menos de uns aspectos
            int needed = Math.max(1, (int) (cost.getAmount(aspect) * Math.max(0.1f, modifier(stack, player, aspect) - discount)));
            if (list.getAmount(aspect) < needed) return false;
            real.add(aspect, needed);
        }
        if (!reallyDoIt) return true;
        for (Aspect aspect : real.getAspects()) list.reduce(aspect, real.getAmount(aspect));
        setVis(stack, list);
        return true;
    }

    /**
     * O {@code getConsumptionModifier} do original: o multiplicador da ponteira menos o desconto do que o jogador
     * veste, nunca abaixo de um décimo.
     */
    public static float modifier(ItemStack stack, @org.jetbrains.annotations.Nullable Player player, Aspect aspect) {
        float modifier = cap(stack).discount(aspect);
        if (player != null) modifier -= totalVisDiscount(player, aspect);
        return Math.max(modifier, 0.1f);
    }

    /** O {@code getConsumptionModifier} fora da fabricação: o mesmo, menos um décimo por melhoria frugal do foco. */
    public static float focusModifier(ItemStack stack, @org.jetbrains.annotations.Nullable Player player, Aspect aspect) {
        return Math.max(0.1f, modifier(stack, player, aspect) - focusFrugal(stack) / 10.0f);
    }

    /**
     * O {@code WandManager.getTotalVisDiscount}: a soma dos descontos das peças vestidas, em pontos percentuais.
     * Primeiro os amuletos, anéis e cinto vestidos, depois a armadura, como no original.
     */
    public static float totalVisDiscount(Player player, @org.jetbrains.annotations.Nullable Aspect aspect) {
        int total = 0;
        for (ItemStack worn : net.thaumcraft.baubles.Baubles.of(player).items()) {
            if (worn.getItem() instanceof net.thaumcraft.api.wands.VisDiscountGear gear) total += gear.visDiscount(worn, player, aspect);
        }
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            ItemStack worn = player.getItemBySlot(slot);
            if (worn.getItem() instanceof net.thaumcraft.api.wands.VisDiscountGear gear) total += gear.visDiscount(worn, player, aspect);
        }
        // a exaustão de vis tira 10% do desconto por nível
        total -= net.thaumcraft.registry.TCEffects.exhaustion(player);
        return total / 100.0f;
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
        return consume(stack, cost, reallyDoIt, null);
    }

    public static boolean consume(ItemStack stack, AspectList cost, boolean reallyDoIt, @org.jetbrains.annotations.Nullable Player player) {
        AspectList list = vis(stack);
        WandParts.Cap cap = cap(stack);
        AspectList real = new AspectList();
        for (Aspect aspect : cost.getAspects()) {
            int needed = (int) (cost.getAmount(aspect) * VIS_UNIT * modifier(stack, player, aspect));
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
            ItemStack focusStack = focusStack(stack);
            if (!held.isContinuous(focusStack)) {
                // tiro único: sai de uma vez, sem segurar, e só depois da espera do foco (o setCooldown do original)
                cast(level, player, stack, held);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
            // o jato: o original zera a espera ao começar, e o primeiro tique já sai
            setCooldown(player, -1);
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
                context.getItemInHand(), context.getClickedFace());
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
            if (remaining % 5 == 0) drain(stack, node, level, player);
            return;
        }
        FocusItem focus = net.thaumcraft.item.Focuses.on(stack);
        if (focus != null) {
            // dos dois lados, como o onUsingFocusTick do original: o servidor age, quem vê desenha — um golpe por espera
            // do foco (o raio, a cada 250 ms)
            if (isOnCooldown(player)) return;
            setCooldown(player, focus.cooldown(focusStack(stack)));
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
     * Um gole no nó: o {@code onUsingWandTick} da {@code TileNode} da 4.2.3.5.
     *
     * <p>Um aspecto sorteado entre os que o nó tem e que ainda cabem na varinha; o gole é de um ponto, mais um
     * com cada pesquisa de Sangria de Nós. Com a Preservação de Nós, e fora das varinhas de madeira ou de ponta
     * de ferro, a varinha nunca leva o último ponto de um aspecto — a não ser agachado.
     */
    private static void drain(ItemStack stack, NodeBlockEntity node, Level level, Player player) {
        var knowledge = net.thaumcraft.research.Knowledges.of(player);
        int tap = 1;
        if (knowledge.hasResearch("NODETAPPER1")) tap++;
        if (knowledge.hasResearch("NODETAPPER2")) tap++;
        boolean preserve = preserves(stack, player);
        Aspect chosen = drainable(stack, node, preserve, level.getRandom());
        if (chosen == null) return;
        int amount = node.aspects().getAmount(chosen);
        if (tap > amount) tap = amount;
        if (preserve && tap == amount) tap--;
        if (tap <= 0) return;
        int left = addVis(stack, chosen, tap);
        if (left >= tap) return;
        node.take(chosen, tap - left);
        level.playSound(null, node.getBlockPos(), net.thaumcraft.registry.TCSounds.WAND.value(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.3f, 1.2f + level.getRandom().nextFloat() * 0.3f);
        node.drained(chosen);
    }

    /** Se a varinha deixa o último ponto de cada aspecto no nó: a Preservação de Nós, fora da madeira e do ferro. */
    public static boolean preserves(ItemStack stack, Player player) {
        return !player.isShiftKeyDown() && net.thaumcraft.research.Knowledges.of(player).hasResearch("NODEPRESERVE")
                && !rod(stack).tag().equals("wood") && !cap(stack).tag().equals("iron");
    }

    /**
     * O {@code chooseRandomFilteredFromSource} do nó: um aspecto dele, sorteado, que ainda caiba na varinha.
     * Nenhum, e a varinha para de beber — e o feixe some, como no original.
     */
    @org.jetbrains.annotations.Nullable
    public static Aspect drainable(ItemStack stack, NodeBlockEntity node, boolean preserve,
                                   net.minecraft.util.RandomSource random) {
        int min = preserve ? 1 : 0;
        List<Aspect> room = aspectsWithRoom(stack);
        List<Aspect> possible = new ArrayList<>();
        for (Aspect aspect : node.aspects().getAspects()) {
            if (room.contains(aspect) && node.aspects().getAmount(aspect) > min) possible.add(aspect);
        }
        return possible.isEmpty() ? null : possible.get(random.nextInt(possible.size()));
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
