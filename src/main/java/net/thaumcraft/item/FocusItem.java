package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.FocusUpgradeTable.Type;
import net.thaumcraft.registry.TCComponents;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Um foco de varinha: o {@code ItemFocusBasic} e os {@code ItemFocus*} da 4.2.3.5 no que eles dizem de si — o custo, a
 * espera entre usos, se é jato contínuo e as melhorias que cabem em cada um dos cinco postos. O que cada foco faz
 * quando a varinha aponta fica em {@link Focuses}.
 *
 * <p>Como no original, o foco entra e sai da varinha pela tecla de trocar foco (F): segurando, abre o menu
 * radial com os focos do inventário e das bolsas; agachado, a tecla tira o foco preso. As melhorias vão junto.
 *
 * @param type o que este foco faz, pelo nome que o original dá a ele
 */
public class FocusItem extends Item {
    private final String type;
    private final AspectList cost;

    public FocusItem(Properties properties, String type, AspectList cost, boolean continuous) {
        super(properties);
        this.type = type;
        this.cost = cost;
    }

    public String type() {
        return this.type;
    }

    // ----------------------------------------------------------------- as melhorias

    /** O {@code getAppliedUpgrades}: os cinco postos, com o número do tipo ou −1. */
    public static short[] upgrades(ItemStack focusStack) {
        short[] out = {-1, -1, -1, -1, -1};
        List<Short> saved = focusStack.get(TCComponents.FOCUS_UPGRADES);
        if (saved != null) {
            for (int j = 0; j < saved.size() && j < 5; j++) out[j] = saved.get(j);
        }
        return out;
    }

    /** O {@code getUpgradeLevel}: quantas vezes este tipo aparece nos postos. */
    public static int level(ItemStack focusStack, Type type) {
        int level = 0;
        for (short id : upgrades(focusStack)) if (id == type.id()) level++;
        return level;
    }

    public static boolean isUpgradedWith(ItemStack focusStack, Type type) {
        return level(focusStack, type) > 0;
    }

    /** O {@code getPossibleUpgradesByRank}. */
    public List<Type> possibleByRank(ItemStack focusStack, int rank) {
        var ranks = FocusUpgradeTable.RANKS.get(this.type);
        return ranks == null || rank < 1 || rank > 5 ? List.of() : ranks.get(rank - 1);
    }

    /** O {@code canApplyUpgrade}: as restrições que cada foco do original põe. */
    public boolean canApply(ItemStack focusStack, @Nullable Player player, Type upgrade, int rank) {
        return switch (this.type) {
            case "fire" -> upgrade != FocusUpgradeTable.ALCHEMISTSFIRE || !isUpgradedWith(focusStack, FocusUpgradeTable.FIREBALL)
                    || !isUpgradedWith(focusStack, FocusUpgradeTable.ALCHEMISTSFIRE);
            case "shock" -> upgrade != FocusUpgradeTable.ENLARGE || isUpgradedWith(focusStack, FocusUpgradeTable.CHAINLIGHTNING)
                    || isUpgradedWith(focusStack, FocusUpgradeTable.EARTHSHOCK);
            case "warding" -> upgrade != FocusUpgradeTable.ENLARGE || isUpgradedWith(focusStack, FocusUpgradeTable.ARCHITECT);
            case "hellbat" -> upgrade != FocusUpgradeTable.VAMPIREBATS
                    || player != null && net.thaumcraft.research.ResearchManager.knows(player, "VAMPBAT");
            default -> true;
        };
    }

    /** O {@code applyUpgrade}: só num posto ainda vazio. */
    public static boolean apply(ItemStack focusStack, Type upgrade, int rank) {
        short[] upgrades = upgrades(focusStack);
        if (rank < 1 || rank > 5 || upgrades[rank - 1] != -1) return false;
        upgrades[rank - 1] = upgrade.id();
        List<Short> list = new ArrayList<>();
        for (short s : upgrades) list.add(s);
        focusStack.set(TCComponents.FOCUS_UPGRADES, List.copyOf(list));
        return true;
    }

    // ----------------------------------------------------------------- o que o foco diz de si

    /** O {@code getVisCost}: em centésimos de vis, conforme as melhorias. */
    public AspectList cost(ItemStack focusStack) {
        return switch (this.type) {
            case "fire" -> isUpgradedWith(focusStack, FocusUpgradeTable.FIREBEAM)
                    ? new AspectList().add(Aspects.FIRE, 10).add(Aspects.ORDER, 3)
                    : isUpgradedWith(focusStack, FocusUpgradeTable.FIREBALL)
                    ? new AspectList().add(Aspects.FIRE, 66).add(Aspects.ENTROPY, 33) : this.cost.copy();
            case "frost" -> isUpgradedWith(focusStack, FocusUpgradeTable.SCATTERSHOT)
                    ? new AspectList().add(Aspects.WATER, 20).add(Aspects.FIRE, 2).add(Aspects.ENTROPY, 2).add(Aspects.AIR, 5)
                    : isUpgradedWith(focusStack, FocusUpgradeTable.ICEBOULDER)
                    ? new AspectList().add(Aspects.WATER, 20).add(Aspects.FIRE, 2).add(Aspects.ENTROPY, 2).add(Aspects.EARTH, 5)
                    : this.cost.copy();
            case "shock" -> isUpgradedWith(focusStack, FocusUpgradeTable.CHAINLIGHTNING)
                    ? new AspectList().add(Aspects.AIR, 40).add(Aspects.WATER, 10)
                    : isUpgradedWith(focusStack, FocusUpgradeTable.EARTHSHOCK)
                    ? new AspectList().add(Aspects.AIR, 75).add(Aspects.EARTH, 25) : this.cost.copy();
            // a escavação e a troca com toque de seda (e a escavação com radiestesia) pagam um de cada primário a mais.
            // O original guarda esse custo numa variável só da classe, e o primeiro que perguntar define o de todos;
            // aqui cada um tem o seu
            case "excavation" -> isUpgradedWith(focusStack, FocusUpgradeTable.SILKTOUCH) ? allPlus(this.cost, 1)
                    : isUpgradedWith(focusStack, FocusUpgradeTable.DOWSING)
                    ? new AspectList().add(Aspects.FIRE, 2).add(Aspects.ORDER, 2).add(this.cost) : this.cost.copy();
            case "trade" -> isUpgradedWith(focusStack, FocusUpgradeTable.SILKTOUCH) ? allPlus(this.cost, 1) : this.cost.copy();
            case "pech" -> isUpgradedWith(focusStack, FocusUpgradeTable.NIGHTSHADE) ? allPlus(new AspectList(), 10) : this.cost.copy();
            case "hellbat" -> isUpgradedWith(focusStack, FocusUpgradeTable.BATBOMBS)
                    ? new AspectList().add(Aspects.FIRE, 100).add(Aspects.ENTROPY, 200).add(Aspects.AIR, 100)
                    : isUpgradedWith(focusStack, FocusUpgradeTable.DEVILBATS)
                    ? new AspectList().add(Aspects.FIRE, 100).add(Aspects.ENTROPY, 100).add(Aspects.AIR, 100).add(Aspects.EARTH, 100)
                    : this.cost.copy();
            case "primal" -> Focuses.primalCost(System.currentTimeMillis());
            default -> this.cost.copy();
        };
    }

    /** O custo sem melhoria nenhuma (o que o gerador de aspectos e os testes olham). */
    public AspectList cost() {
        return this.cost(new ItemStack(this));
    }

    private static AspectList allPlus(AspectList base, int each) {
        AspectList out = new AspectList();
        for (Aspect primal : List.of(Aspects.AIR, Aspects.FIRE, Aspects.EARTH, Aspects.WATER, Aspects.ORDER, Aspects.ENTROPY)) out.add(primal, each);
        out.add(base);
        return out;
    }

    /** O {@code getActivationCooldown}, em milissegundos: a espera entre um uso e outro. */
    public int cooldown(ItemStack focusStack) {
        return switch (this.type) {
            case "fire" -> isUpgradedWith(focusStack, FocusUpgradeTable.FIREBALL) ? 1000 : 0;
            case "frost" -> level(focusStack, FocusUpgradeTable.SCATTERSHOT) <= 0 && level(focusStack, FocusUpgradeTable.ICEBOULDER) <= 0 ? 200 : 500;
            case "shock" -> isUpgradedWith(focusStack, FocusUpgradeTable.CHAINLIGHTNING) ? 500
                    : isUpgradedWith(focusStack, FocusUpgradeTable.EARTHSHOCK) ? 1000 : 250;
            case "hellbat" -> 1000;
            case "pech" -> 250;
            case "primal" -> 500;
            default -> 0;
        };
    }

    /**
     * Este foco é jato contínuo ou tiro único? O de fogo (sem a bola de fogo), o de raio (sem o choque de terra) e o de
     * escavação seguram o botão; os outros saem de uma vez, como no {@code onFocusRightClick} de cada um.
     */
    public boolean isContinuous(ItemStack focusStack) {
        return switch (this.type) {
            case "fire" -> !isUpgradedWith(focusStack, FocusUpgradeTable.FIREBALL);
            case "shock" -> !isUpgradedWith(focusStack, FocusUpgradeTable.EARTHSHOCK);
            case "excavation" -> true;
            default -> false;
        };
    }

    public boolean isContinuous() {
        return this.isContinuous(new ItemStack(this));
    }

    /** O {@code isVisCostPerTick}: o fogo e a escavação dizem "por tique" na dica. */
    public boolean isVisCostPerTick() {
        return this.type.equals("fire") || this.type.equals("excavation");
    }

    /** O {@code getMaxAreaSize}: o tamanho máximo da área do arquiteto. */
    public int maxAreaSize(ItemStack focusStack) {
        return switch (this.type) {
            case "trade" -> 3 + level(focusStack, FocusUpgradeTable.ENLARGE) * 2;
            case "warding" -> 3 + level(focusStack, FocusUpgradeTable.ENLARGE);
            default -> 1;
        };
    }

    /**
     * O {@code getSortingHelper}: as letras de cada foco e depois os números dos cinco postos (−1 nos vazios, como o
     * original escreve), que é a ordem do menu radial.
     */
    public String sortKey(ItemStack focusStack) {
        String prefix = switch (this.type) {
            case "fire" -> "AF";
            case "excavation" -> "BE";
            case "frost" -> "BF";
            case "shock" -> "BL";
            case "portable_hole" -> "BPH";
            case "trade" -> "BT";
            case "warding" -> "BWA";
            case "primal" -> "FP";
            case "hellbat" -> "HH";
            case "pech" -> "PP";
            default -> this.type;
        };
        StringBuilder out = new StringBuilder(prefix);
        for (short id : upgrades(focusStack)) out.append(id);
        return out.toString();
    }

    public String sortKey() {
        return this.sortKey(new ItemStack(this));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.focus." + this.type);
    }

    /** O {@code addInformation}: o custo (por uso ou por tique) e as melhorias, com o nível em romano. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        AspectList al = this.cost(stack);
        if (!al.isEmpty()) {
            lines.accept(Component.translatable(this.isVisCostPerTick() ? "item.Focus.cost2" : "item.Focus.cost1"));
            DecimalFormat format = new DecimalFormat("#####.##");
            for (Aspect aspect : al.getAspectsSorted()) {
                lines.accept(Component.literal(" ").append(aspect.name().copy().withColor(aspect.color()))
                        .append(" x " + format.format(al.getAmount(aspect) / 100.0f)));
            }
        }
        addUpgradeLines(stack, lines);
    }

    /** O {@code addFocusInformation}: cada melhoria uma vez, com o nível. */
    public static void addUpgradeLines(ItemStack focusStack, Consumer<Component> lines) {
        Map<Short, Integer> map = new LinkedHashMap<>();
        for (short id : upgrades(focusStack)) if (id >= 0) map.merge(id, 1, Integer::sum);
        for (var entry : map.entrySet()) {
            Type type = FocusUpgradeTable.BY_ID.get(entry.getKey());
            if (type == null) continue;
            Component name = Component.translatable("focus.upgrade." + type.name() + ".name");
            if (entry.getValue() > 1) name = name.copy().append(" ").append(Component.translatable("enchantment.level." + entry.getValue()));
            lines.accept(name.copy().withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
