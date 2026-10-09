package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.TaglockItem;
import net.thaumcraft.occulta.treefyd.TreefydEntity;

import java.util.List;

/**
 * O <b>Treefyd</b>: o que ele ataca, quem ele aprende a poupar, e o que o faz crescer.
 */
public class OccultaTreefydGameTest {
    /**
     * <b>Ele ataca o estranho e poupa o dono.</b>
     *
     * <p>E poupa também as famílias que o original lhe tira da frente: o Ent, o bicho do ar, o de água.
     */
    @GameTest
    public void heAttacksTheStrangerAndSparesTheOwner(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        TreefydEntity treefyd = helper.spawn(OccultaEntities.TREEFYD, new BlockPos(2, 2, 2));

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 4));
        if (!treefyd.estranho(porco)) helper.fail("um porco que ele não conhece é estranho");

        var morcego = helper.spawn(EntityTypes.BAT, new BlockPos(2, 3, 4));
        if (treefyd.estranho(morcego)) helper.fail("mas um bicho do ar não");

        var lula = helper.spawn(EntityTypes.SQUID, new BlockPos(4, 2, 4));
        if (treefyd.estranho(lula)) helper.fail("nem um bicho de água");

        var outro = helper.spawn(OccultaEntities.TREEFYD, new BlockPos(4, 2, 2));
        if (treefyd.estranho(outro)) helper.fail("nem outro Treefyd");

        if (!treefyd.estranho(quem)) helper.fail("e quem não é dono dele é estranho");
        treefyd.dono(quem.getUUID());
        if (treefyd.estranho(quem)) helper.fail("mas o dono não");

        varre(helper);
        helper.succeed();
    }

    /**
     * <b>Os de criação conhecem-se por espécie; os outros, um a um.</b>
     *
     * <p>Apresentada uma ovelha, ele poupa <b>todas</b>; apresentado um creeper, poupa <b>aquele</b>.
     */
    @GameTest
    public void herdAnimalsAreKnownByKind(GameTestHelper helper) {
        TreefydEntity treefyd = helper.spawn(OccultaEntities.TREEFYD, new BlockPos(2, 2, 2));

        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(2, 2, 4));
        var outraOvelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(4, 2, 4));
        if (!TreefydEntity.porEspécie(ovelha)) helper.fail("uma ovelha conhece-se por espécie");
        treefyd.apresenta(ovelha, false);
        if (treefyd.estranho(ovelha)) helper.fail("e apresentada, ele poupa-a");
        if (treefyd.estranho(outraOvelha)) helper.fail("e poupa todas as outras");

        var creeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(2, 2, 6));
        var outroCreeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(4, 2, 6));
        if (TreefydEntity.porEspécie(creeper)) helper.fail("um creeper conhece-se um a um");
        treefyd.apresenta(creeper, false);
        if (treefyd.estranho(creeper)) helper.fail("e apresentado, ele poupa-o");
        if (!treefyd.estranho(outroCreeper)) helper.fail("mas o do lado continua estranho");

        // e agachado esquece-se
        treefyd.apresenta(ovelha, true);
        if (!treefyd.estranho(ovelha)) helper.fail("esquecida, a ovelha volta a ser estranha");
        treefyd.apresenta(creeper, true);
        if (!treefyd.estranho(creeper)) helper.fail("e o creeper também");

        varre(helper);
        helper.succeed();
    }

    /**
     * <b>O dono fá-lo crescer, e ele faz filhos.</b>
     *
     * <p>O Coração de Creeper leva-o a cem; o de Demônio, a cento e cinquenta. A Boline liga o modo
     * sentinela. E uma semente faz um filho que já conhece o que o pai conhece.
     */
    @GameTest
    public void theOwnerFeedsHimAndHeMakesChildren(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;
        TreefydEntity treefyd = helper.spawn(OccultaEntities.TREEFYD, new BlockPos(2, 2, 2));
        treefyd.dono(quem.getUUID());

        if (treefyd.getMaxHealth() != TreefydEntity.VIDA) helper.fail("ele nasce com cinquenta de vida");

        dá(quem, new ItemStack(OccultaItems.CREEPER_HEART));
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        if (treefyd.getMaxHealth() != TreefydEntity.CREEPER_VIDA) {
            helper.fail("o Coração de Creeper leva-o a cem, e está em " + treefyd.getMaxHealth());
        }

        dá(quem, new ItemStack(OccultaItems.DEMON_HEART));
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        if (treefyd.getMaxHealth() != TreefydEntity.DEMÔNIO_VIDA) {
            helper.fail("e o de Demônio a cento e cinquenta, e está em " + treefyd.getMaxHealth());
        }

        if (treefyd.sentinela()) helper.fail("ele nasce andando");
        dá(quem, new ItemStack(OccultaItems.BOLINE));
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        if (!treefyd.sentinela()) helper.fail("e a Boline faz dele uma sentinela");

        // o filho herda o que o pai conhece
        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(5, 2, 5));
        treefyd.apresenta(ovelha, false);
        dá(quem, new ItemStack(OccultaItems.TREEFYD_SEEDS));
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);

        var todos = level.getEntitiesOfClass(TreefydEntity.class,
                treefyd.getBoundingBox().inflate(4.0),
                net.minecraft.world.entity.Entity::isAlive);
        TreefydEntity filho = null;
        for (var cada : todos) if (cada != treefyd) filho = cada;
        if (filho == null) {
            helper.fail("uma semente na mão do dono faz um filho");
            varre(helper);
            return;
        }
        if (filho.estranho(ovelha)) helper.fail("e o filho já conhece o que o pai conhecia");
        if (!quem.getUUID().equals(filho.dono())) helper.fail("e o dono é o mesmo");

        varre(helper);
        helper.succeed();
    }

    /** <b>E só o dono mexe com ele.</b> */
    @GameTest
    public void onlyTheOwnerTeachesHim(GameTestHelper helper) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;
        TreefydEntity treefyd = helper.spawn(OccultaEntities.TREEFYD, new BlockPos(2, 2, 2));

        dá(quem, new ItemStack(OccultaItems.BOLINE));
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        if (treefyd.sentinela()) helper.fail("quem não é dono não lhe toca");

        treefyd.dono(quem.getUUID());
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        if (!treefyd.sentinela()) helper.fail("e o dono toca");

        // e o frasco de vínculo apresenta de verdade
        var ovelha = helper.spawn(EntityTypes.SHEEP, new BlockPos(2, 2, 4));
        ItemStack frasco = new ItemStack(OccultaItems.TAGLOCK, 2);
        TaglockItem.bind(frasco, ovelha);
        dá(quem, frasco);
        treefyd.interact(quem, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        if (treefyd.estranho(ovelha)) helper.fail("um frasco de vínculo apresenta-lhe alguém");
        if (quem.getMainHandItem().getCount() != 1) helper.fail("e gasta o frasco");

        varre(helper);
        helper.succeed();
    }

    /**
     * <b>A semente planta-se no chão, e o que nasce não é uma planta.</b>
     *
     * <p>E não se planta em lugar coberto: tem de haver ar por cima.
     */
    @GameTest
    public void theSeedPlantsHim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.getAbilities().instabuild = false;

        BlockPos chão = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(chão, Blocks.GRASS_BLOCK.defaultBlockState());

        ItemStack semente = new ItemStack(OccultaItems.TREEFYD_SEEDS, 2);
        dá(quem, semente);
        var onde = new net.minecraft.world.item.context.UseOnContext(level, quem,
                InteractionHand.MAIN_HAND, semente,
                new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(chão), Direction.UP, chão, false));
        semente.getItem().useOn(onde);

        var nasceram = level.getEntitiesOfClass(TreefydEntity.class,
                new net.minecraft.world.phys.AABB(chão).inflate(3.0),
                net.minecraft.world.entity.Entity::isAlive);
        if (nasceram.size() != 1) {
            helper.fail("a semente planta um Treefyd, e plantou " + nasceram.size());
            varre(helper);
            return;
        }
        if (!quem.getUUID().equals(nasceram.getFirst().dono())) {
            helper.fail("e o dono é quem a plantou");
        }
        if (semente.getCount() != 1) helper.fail("e gasta uma semente");
        if (!level.getBlockState(chão.above()).is(Blocks.SHORT_GRASS)) {
            helper.fail("e a erva alta fica no lugar");
        }

        varre(helper);
        helper.succeed();
    }

    /** E a receita, que é a do original: duas sementes. */
    @GameTest
    public void theRecipeIsTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack mutandis = new ItemStack(OccultaItems.MUTANDIS_EXTREMIS);
        var bancada = CraftingInput.of(3, 3, List.of(
                mutandis.copy(), new ItemStack(OccultaItems.REEK_OF_MISFORTUNE), mutandis.copy(),
                new ItemStack(OccultaItems.EMBER_MOSS),
                new ItemStack(OccultaItems.WATER_ARTICHOKE_GLOBE),
                new ItemStack(OccultaItems.MANDRAKE_ROOT),
                mutandis.copy(), new ItemStack(OccultaItems.TEAR_OF_THE_GODDESS), mutandis.copy()));
        var achada = level.recipeAccess().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING, bancada, level);
        if (achada.isEmpty()) {
            helper.fail("não há receita para a semente");
            return;
        }
        ItemStack feito = achada.get().value().assemble(bancada);
        if (!feito.is(OccultaItems.TREEFYD_SEEDS)) helper.fail("a receita dá a semente, e deu " + feito);
        if (feito.getCount() != 2) helper.fail("e dá duas, e deu " + feito.getCount());
        helper.succeed();
    }

    /** Põe aquilo na mão de quem joga. */
    private static void dá(ServerPlayer quem, ItemStack oquê) {
        quem.setItemInHand(InteractionHand.MAIN_HAND, oquê);
    }

    /** Varre os Treefyds que sobraram: um deles na arena do lado estraga a prova de outro. */
    private static void varre(GameTestHelper helper) {
        for (var cada : helper.getLevel().getEntitiesOfClass(TreefydEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(0, 0, 0)))
                        .inflate(24.0))) {
            cada.discard();
        }
    }
}
