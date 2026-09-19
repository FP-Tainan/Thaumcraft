package net.thaumcraft.entity.golem.ai;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.thaumcraft.entity.GolemEntity;

/**
 * As tarefas de cada núcleo, na prioridade do {@code setupGolem} da 4.2.3.5. Sem núcleo, o golem não tem tarefa
 * nenhuma (fica parado, de cabeça baixa).
 */
public final class GolemGoals {
    private GolemGoals() {
    }

    public static void register(GolemEntity golem, GoalSelector goals, GoalSelector targets) {
        byte core = golem.getCore();
        if (core > -1) goals.addGoal(0, new CombatGoals.AvoidCreeperSwell(golem));
        String deco = golem.getGolemDecoration();
        switch (core) {
            case 0 -> {
                goals.addGoal(0, new InventoryGoals.HomeReplace(golem));
                goals.addGoal(1, new InventoryGoals.HomePlace(golem));
                goals.addGoal(2, new InventoryGoals.HomeDrop(golem));
                goals.addGoal(3, new InventoryGoals.FillTake(golem));
                goals.addGoal(4, new InventoryGoals.FillGoto(golem));
            }
            case 1 -> {
                goals.addGoal(0, new InventoryGoals.HomeReplace(golem));
                goals.addGoal(1, new InventoryGoals.EmptyPlace(golem));
                goals.addGoal(2, new InventoryGoals.EmptyDrop(golem));
                goals.addGoal(3, new InventoryGoals.EmptyGoto(golem));
                goals.addGoal(4, new InventoryGoals.HomeTake(golem));
            }
            case 2 -> {
                goals.addGoal(0, new InventoryGoals.HomeReplace(golem));
                goals.addGoal(1, new InventoryGoals.HomePlace(golem));
                goals.addGoal(2, new InventoryGoals.ItemPickup(golem));
            }
            case 3 -> goals.addGoal(2, new InteractGoals.HarvestCrops(golem));
            case 4 -> {
                if (deco.contains("R")) goals.addGoal(2, new CombatGoals.DartAttack(golem));
                goals.addGoal(3, new CombatGoals.AttackOnCollide(golem));
                targets.addGoal(1, new CombatGoals.HurtByTarget(golem));
                targets.addGoal(2, new CombatGoals.NearestAttackableTarget(golem));
            }
            case 5 -> {
                goals.addGoal(1, new FluidGoals.LiquidEmpty(golem));
                goals.addGoal(2, new FluidGoals.LiquidGather(golem));
                goals.addGoal(3, new FluidGoals.LiquidGoto(golem));
            }
            case 6 -> {
                goals.addGoal(1, new FluidGoals.EssentiaEmpty(golem));
                goals.addGoal(2, new FluidGoals.EssentiaGather(golem));
                goals.addGoal(3, new FluidGoals.EssentiaGoto(golem));
            }
            case 7 -> goals.addGoal(2, new InteractGoals.HarvestLogs(golem));
            case 8 -> {
                goals.addGoal(0, new InventoryGoals.HomeReplace(golem));
                goals.addGoal(0, new InteractGoals.UseItem(golem));
                goals.addGoal(4, new InventoryGoals.HomeTake(golem));
            }
            case 9 -> {
                if (deco.contains("R")) goals.addGoal(2, new CombatGoals.DartAttack(golem));
                goals.addGoal(3, new CombatGoals.AttackOnCollide(golem));
                targets.addGoal(1, new CombatGoals.NearestButcherTarget(golem));
            }
            case 10 -> {
                goals.addGoal(0, new InventoryGoals.HomeReplace(golem));
                goals.addGoal(1, new InventoryGoals.SortingPlace(golem));
                goals.addGoal(3, new InventoryGoals.SortingGoto(golem));
                goals.addGoal(4, new InventoryGoals.HomeTakeSorting(golem));
            }
            case 11 -> goals.addGoal(2, new FishGoal(golem));
            default -> {
            }
        }
        if (core > -1) {
            goals.addGoal(5, new MiscGoals.OpenDoor(golem, true));
            goals.addGoal(6, new MiscGoals.ReturnHome(golem));
        }
    }
}
