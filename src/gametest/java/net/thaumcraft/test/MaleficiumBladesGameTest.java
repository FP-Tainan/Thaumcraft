package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.maleficium.FortressBladeItem;
import net.thaumcraft.maleficium.MaleficiumItems;
import net.thaumcraft.maleficium.ThaumicDisassemblerItem;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.WarpEvents;

import java.util.Arrays;

/**
 * As últimas coisas do Maleficium: o sangue do vazio, o cogumelo, a chave do portão, o desmontador, a lâmina
 * primordial e as três lâminas de fortaleza com as suas inscrições.
 */
public class MaleficiumBladesGameTest {
    /** O frasco de sangue e uma peça de armadura devolvem a peça tocada pelo vazio. */
    @GameTest
    public void voidBloodTouchesArmour(GameTestHelper helper) {
        ItemStack armour = new ItemStack(TCItems.FORTRESS_CHESTPLATE);
        var input = CraftingInput.of(2, 1, Arrays.asList(new ItemStack(MaleficiumItems.VOID_BLOOD), armour));
        var found = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (found.isEmpty()) helper.fail("o sangue do vazio com uma armadura devia fechar receita");
        ItemStack out = found.get().value().assemble(input);
        if (!out.is(TCItems.FORTRESS_CHESTPLATE)) helper.fail("devia sair a mesma peça, saiu " + out);
        if (!Boolean.TRUE.equals(out.get(TCComponents.VOID_TOUCHED))) helper.fail("a peça devia sair tocada pelo vazio");
        helper.succeed();
    }

    /** E a peça já tocada não aceita outro frasco. */
    @GameTest
    public void voidBloodOnlyWorksOnce(GameTestHelper helper) {
        ItemStack armour = new ItemStack(TCItems.FORTRESS_CHESTPLATE);
        armour.set(TCComponents.VOID_TOUCHED, true);
        var input = CraftingInput.of(2, 1, Arrays.asList(new ItemStack(MaleficiumItems.VOID_BLOOD), armour));
        var found = helper.getLevel().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (found.isPresent()) helper.fail("a peça já tocada não devia aceitar outro frasco");
        helper.succeed();
    }

    /** A armadura tocada pelo vazio se conserta um ponto por segundo. */
    @GameTest
    public void voidTouchedArmourRepairsItself(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack armour = new ItemStack(TCItems.FORTRESS_CHESTPLATE);
        armour.set(TCComponents.VOID_TOUCHED, true);
        armour.setDamageValue(10);
        player.getInventory().setItem(0, armour);
        for (int tick = 0; tick < 60; tick++) {
            player.tickCount++;
            net.thaumcraft.maleficium.MaleficiumEvents.tickForTest(player);
        }
        if (armour.getDamageValue() >= 10) helper.fail("a peça tocada pelo vazio devia ter-se consertado; está em " + armour.getDamageValue());
        helper.succeed();
    }

    /** O desmontador passa de modo em modo e cava conforme o modo. */
    @GameTest
    public void theDisassemblerCyclesModes(GameTestHelper helper) {
        ItemStack tool = new ItemStack(MaleficiumItems.THAUMIC_DISASSEMBLER);
        int[] esperado = {20, 8, 128, 0};
        for (int mode = 0; mode < 4; mode++) {
            tool.set(TCComponents.DISASSEMBLER_MODE, mode);
            if (ThaumicDisassemblerItem.efficiency(tool) != esperado[mode]) {
                helper.fail("o modo " + mode + " devia cavar a " + esperado[mode]);
            }
        }
        tool.set(TCComponents.DISASSEMBLER_CHARGE, 0);
        if (tool.getItem().getDestroySpeed(tool, Blocks.STONE.defaultBlockState()) != 1.0f) {
            helper.fail("sem carga o desmontador cava como a mão");
        }
        tool.set(TCComponents.DISASSEMBLER_MODE, 0);
        tool.set(TCComponents.DISASSEMBLER_CHARGE, 1000);
        if (tool.getItem().getDestroySpeed(tool, Blocks.STONE.defaultBlockState()) != 20.0f) {
            helper.fail("com carga, no modo normal, ele cava a vinte");
        }
        helper.succeed();
    }

    /** E bebe entropia da varinha que estiver no inventário. */
    @GameTest
    public void theDisassemblerDrinksEntropy(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack wand = new ItemStack(TCItems.WAND);
        net.thaumcraft.item.WandItem.setVis(wand, new AspectList().add(Aspects.ENTROPY, 2500));
        player.getInventory().setItem(1, wand);
        ItemStack tool = new ItemStack(MaleficiumItems.THAUMIC_DISASSEMBLER);
        player.getInventory().setItem(0, tool);
        player.tickCount = 20;
        tool.getItem().inventoryTick(tool, helper.getLevel(), player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        if (ThaumicDisassemblerItem.charge(tool) != 100) {
            helper.fail("o desmontador devia beber cem de entropia por segundo; bebeu " + ThaumicDisassemblerItem.charge(tool));
        }
        helper.succeed();
    }

    /** As três lâminas ferem com os números do original e distorcem conforme o metal. */
    @GameTest
    public void theThreeBladesKeepTheirNumbers(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        record Lamina(net.minecraft.world.item.Item item, float damage, int warp) {
        }
        for (Lamina lamina : new Lamina[]{
                new Lamina(MaleficiumItems.THAUMIUM_FORTRESS_BLADE, 14.25f, 0),
                new Lamina(MaleficiumItems.VOIDMETAL_FORTRESS_BLADE, 17.5f, 3),
                new Lamina(MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE, 20.75f, 7)}) {
            ItemStack stack = new ItemStack(lamina.item());
            float damage = ((FortressBladeItem) lamina.item()).attackDamage();
            if (damage != lamina.damage()) helper.fail(lamina.item() + " fere " + damage + ", devia ferir " + lamina.damage());
            int warp = lamina.item() instanceof WarpEvents.WarpingGear gear ? gear.getWarp(stack, player) : -1;
            if (warp != lamina.warp()) helper.fail(lamina.item() + " distorce " + warp + ", devia distorcer " + lamina.warp());
            var mods = stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            double total = mods.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 1.0,
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            if (Math.abs(total - (1.0 + lamina.damage())) > 0.001) {
                helper.fail(lamina.item() + " devia somar " + lamina.damage() + " ao braço; soma " + (total - 1.0));
            }
        }
        helper.succeed();
    }

    /** O golpe da lâmina inscrita põe o inimigo a arder, a andar devagar ou a definhar. */
    @GameTest
    public void theInscriptionsChangeTheBlow(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        record Caso(int inscription, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, boolean fire) {
        }
        for (Caso caso : new Caso[]{
                new Caso(FortressBladeItem.INSCRIPTION_FIRE, null, true),
                new Caso(FortressBladeItem.INSCRIPTION_THUNDER, MobEffects.SLOWNESS, false),
                new Caso(FortressBladeItem.INSCRIPTION_HEAL, MobEffects.WITHER, false)}) {
            LivingEntity alvo = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 2, 1));
            ItemStack blade = new ItemStack(MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE);
            blade.set(TCComponents.KATANA_INSCRIPTION, caso.inscription());
            blade.getItem().hurtEnemy(blade, alvo, player);
            if (caso.fire() && alvo.getRemainingFireTicks() <= 0) helper.fail("a inscrição do demônio devia pôr fogo");
            if (caso.effect() != null && !alvo.hasEffect(caso.effect())) {
                helper.fail("a inscrição " + caso.inscription() + " devia deixar " + caso.effect().getRegisteredName());
            }
            alvo.discard();
        }
        helper.succeed();
    }

    /** A inscrição é uma infusão que muda a própria lâmina do meio. */
    @GameTest
    public void theInscriptionIsAnInfusionOnTheBlade(GameTestHelper helper) {
        var recipe = net.thaumcraft.crafting.InfusionRecipes.ALL.stream()
                .filter(r -> "INSCRIPTIONFIRE".equals(r.research()))
                .filter(r -> r.central().test(new ItemStack(MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE)))
                .findFirst();
        if (recipe.isEmpty()) helper.fail("a lâmina de metal das sombras devia aceitar a inscrição do demônio");
        ItemStack blade = new ItemStack(MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE);
        ItemStack out = recipe.get().resultFor(blade);
        Integer mark = out.get(TCComponents.KATANA_INSCRIPTION);
        if (mark == null || mark != 0) helper.fail("a infusão devia gravar a inscrição do demônio; gravou " + mark);
        helper.succeed();
    }

    /** A chave do portão só se prende uma vez, e guarda o mundo e o lugar. */
    @GameTest
    public void theGateKeyBindsOnce(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack key = new ItemStack(MaleficiumItems.GATE_KEY);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, key);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
                net.minecraft.core.Direction.UP, pos, false);
        key.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        GlobalPos alvo = key.get(TCComponents.GATE_KEY_TARGET);
        if (alvo == null) helper.fail("a chave devia ter-se prendido ao lugar");
        else if (!alvo.pos().equals(pos.above())) helper.fail("a chave devia prender-se ao bloco de cima; prendeu-se a " + alvo.pos());
        helper.succeed();
    }

    /** A lâmina primordial se conserta sozinha e enfraquece quem ela acerta. */
    @GameTest
    public void thePrimalBladeSapsAndMends(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        LivingEntity alvo = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 2, 1));
        ItemStack blade = new ItemStack(MaleficiumItems.PRIMAL_BLADE);
        blade.setDamageValue(5);
        blade.getItem().hurtEnemy(blade, alvo, player);
        if (!alvo.hasEffect(MobEffects.WITHER) || !alvo.hasEffect(MobEffects.WEAKNESS)) {
            helper.fail("a lâmina primordial devia pôr a definhar e a enfraquecer");
        }
        player.tickCount = 20;
        blade.getItem().inventoryTick(blade, helper.getLevel(), player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        if (blade.getDamageValue() != 4) helper.fail("a lâmina devia consertar-se um ponto por segundo; está em " + blade.getDamageValue());
        alvo.discard();
        helper.succeed();
    }
}
