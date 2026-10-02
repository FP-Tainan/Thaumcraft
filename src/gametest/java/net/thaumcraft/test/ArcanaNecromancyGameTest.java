package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Affinity;
import net.thaumcraft.arcana.AffinityData;
import net.thaumcraft.arcana.Essences;
import net.thaumcraft.arcana.Modifiers;
import net.thaumcraft.arcana.Necromancy;
import net.thaumcraft.arcana.Shapes;
import net.thaumcraft.arcana.Spell;
import net.thaumcraft.arcana.SpellModifierKind;
import net.thaumcraft.arcana.Summons;

import java.util.List;

/**
 * O necromante: o exército, a escolha de quem vem, e a panóplia que a Afinidade dá.
 *
 * <p><b>Nada disto é porte</b>, e por isso estas provas são o único lugar onde os números do acréscimo estão
 * escritos. Elas valem mais do que as de uma fatia portada: lá há um original para conferir, aqui não há.
 */
public class ArcanaNecromancyGameTest {
    private static void piso(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    private static ServerPlayer mago(GameTestHelper helper, float fim) {
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.CREATIVE);
        quem.snapTo(helper.absoluteVec(new Vec3(2.5, 2, 2.5)));
        AffinityData.set(quem, AffinityData.NONE.with(Affinity.ENDER, fim * Affinity.MAX_DEPTH));
        return quem;
    }

    /** Lança a peça num ponto do chão. */
    private static boolean chama(GameTestHelper helper, Spell feitiço, net.thaumcraft.arcana.SpellPart.Essence
            qual, ServerPlayer quem, Vec3 onde) {
        return qual.onBlock(helper.getLevel(), feitiço, quem, BlockPos.containing(onde), Direction.UP, onde);
    }

    /** As invocações <b>desta</b> pessoa — as provas correm todas no mesmo mundo. */
    private static List<Mob> as(GameTestHelper helper, ServerPlayer quem) {
        var achadas = new java.util.ArrayList<Mob>();
        for (var bicho : helper.getLevel().getAllEntities()) {
            if (!(bicho instanceof Mob mob)) continue;
            var dado = mob.getAttached(Summons.DATA);
            if (dado != null && dado.dono().equals(quem.getUUID())) achadas.add(mob);
        }
        return achadas;
    }

    private static void limpa(List<Mob> quais) {
        for (Mob qual : quais) qual.discard();
    }

    /** <b>Erguer os Mortos</b> traz zumbi com espada, e a Invocação continua trazendo esqueleto com arco. */
    @GameTest(maxTicks = 40)
    public void raisingTheDeadBringsAZombieWithASword(GameTestHelper helper) {
        piso(helper);
        var quem = mago(helper, 0.0f);
        var feitiço = Spell.of(Shapes.TOUCH, Essences.RAISE_DEAD);

        if (!chama(helper, feitiço, Essences.RAISE_DEAD, quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)))) {
            helper.fail("Erguer os Mortos devia pegar");
            return;
        }
        var minhas = as(helper, quem);
        if (minhas.size() != 1) {
            helper.fail("devia vir uma, vieram " + minhas.size());
            return;
        }
        var veio = minhas.getFirst();
        if (!(veio instanceof Zombie)) helper.fail("quem se ergue é zumbi, veio " + veio.getType());
        if (!veio.getMainHandItem().is(Items.WOODEN_SWORD)) {
            helper.fail("e vem com espada, veio com " + veio.getMainHandItem());
        }
        // e ela troca de lado como a outra: a lista de alvos é a mesma
        if (veio.getTarget() == quem) helper.fail("um morto erguido não mira quem o ergueu");

        limpa(minhas);
        helper.succeed();
    }

    /**
     * <b>Legião</b>: cada cópia sobe o teto em uma.
     *
     * <p>Sem ela o teto é o do original, que é um — e é por isso que esta prova começa provando que a segunda
     * sem Legião não vem.
     */
    @GameTest(maxTicks = 60)
    public void legionRaisesTheCeilingOnePerCopy(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = mago(helper, 0.0f);

        var sozinha = Spell.of(Shapes.TOUCH, Essences.SUMMON);
        chama(helper, sozinha, Essences.SUMMON, quem, helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        chama(helper, sozinha, Essences.SUMMON, quem, helper.absoluteVec(new Vec3(5.5, 2, 4.5)));
        if (Summons.quantas(level, quem) != 1) {
            helper.fail("sem Legião o teto é um, e estão " + Summons.quantas(level, quem));
        }

        // com duas Legiões o teto é três, e as duas que faltavam cabem
        var exército = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.SUMMON), List.of(Modifiers.LEGION, Modifiers.LEGION))));
        if (exército.count(SpellModifierKind.SUMMON_COUNT) != 2) {
            helper.fail("duas Legiões contam duas, contaram "
                    + exército.count(SpellModifierKind.SUMMON_COUNT));
        }
        chama(helper, exército, Essences.SUMMON, quem, helper.absoluteVec(new Vec3(5.5, 2, 4.5)));
        chama(helper, exército, Essences.SUMMON, quem, helper.absoluteVec(new Vec3(6.5, 2, 4.5)));
        if (Summons.quantas(level, quem) != 3) {
            helper.fail("com duas Legiões cabem três, e estão " + Summons.quantas(level, quem));
        }
        // e a quarta não
        chama(helper, exército, Essences.SUMMON, quem, helper.absoluteVec(new Vec3(6.5, 2, 5.5)));
        if (Summons.quantas(level, quem) != 3) {
            helper.fail("a quarta não entra, e estão " + Summons.quantas(level, quem));
        }

        limpa(as(helper, quem));
        helper.succeed();
    }

    /** E ela custa o que custa: cada cópia <b>dobra</b> a conta da etapa. */
    @GameTest(maxTicks = 20)
    public void legionDoublesThePriceEachTime(GameTestHelper helper) {
        var nua = Spell.of(Shapes.TOUCH, Essences.SUMMON);
        var uma = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.SUMMON), List.of(Modifiers.LEGION))));
        var duas = new Spell(List.of(new Spell.Stage(Shapes.TOUCH,
                List.of(Essences.SUMMON), List.of(Modifiers.LEGION, Modifiers.LEGION))));

        float semNada = nua.manaCost(null, null);
        float comUma = uma.manaCost(null, null);
        float comDuas = duas.manaCost(null, null);

        if (comUma < semNada * 1.9f) helper.fail("uma Legião dobra: " + semNada + " para " + comUma);
        if (comDuas < comUma * 1.9f) helper.fail("e a segunda dobra outra vez: " + comUma
                + " para " + comDuas);
        helper.succeed();
    }

    /** Os quatro degraus da panóplia, que saem da profundidade no Fim de quem chama. */
    @GameTest(maxTicks = 20)
    public void thePanoplyHasFourSteps(GameTestHelper helper) {
        var nenhum = mago(helper, 0.1f);
        if (Necromancy.panóplia(nenhum) != 0) helper.fail("abaixo de um quarto não vem nada vestido");
        AffinityData.set(nenhum, AffinityData.NONE.with(Affinity.ENDER,
                Necromancy.COURO * Affinity.MAX_DEPTH));
        if (Necromancy.panóplia(nenhum) != 1) helper.fail("a um quarto, couro");
        AffinityData.set(nenhum, AffinityData.NONE.with(Affinity.ENDER,
                Necromancy.FERRO * Affinity.MAX_DEPTH));
        if (Necromancy.panóplia(nenhum) != 2) helper.fail("a meio, ferro");
        AffinityData.set(nenhum, AffinityData.NONE.with(Affinity.ENDER,
                Necromancy.DIAMANTE * Affinity.MAX_DEPTH));
        if (Necromancy.panóplia(nenhum) != 3) helper.fail("a três quartos, diamante");
        if (Necromancy.temMontaria(nenhum)) helper.fail("mas a três quartos ainda não há montaria");
        AffinityData.set(nenhum, AffinityData.NONE.with(Affinity.ENDER,
                Necromancy.MONTARIA * Affinity.MAX_DEPTH));
        if (!Necromancy.temMontaria(nenhum)) helper.fail("a nove décimos, montaria");
        helper.succeed();
    }

    /**
     * Um necromante de meio caminho chama gente <b>vestida de ferro</b> — e nada do que ela veste cai.
     *
     * <p>A roupa some com quem a usava: uma invocação que largasse diamante ao fim do prazo seria uma fábrica
     * de diamante, e o prazo é de quatro minutos.
     */
    @GameTest(maxTicks = 40)
    public void aHalfwayNecromancerArmsWhatHeCalls(GameTestHelper helper) {
        piso(helper);
        var quem = mago(helper, Necromancy.FERRO);

        chama(helper, Spell.of(Shapes.TOUCH, Essences.RAISE_DEAD), Essences.RAISE_DEAD, quem,
                helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var minhas = as(helper, quem);
        if (minhas.size() != 1) {
            helper.fail("devia vir uma, vieram " + minhas.size());
            return;
        }
        var zumbi = minhas.getFirst();
        if (!zumbi.getMainHandItem().is(Items.IRON_SWORD)) {
            helper.fail("a meio caminho a espada é de ferro, é " + zumbi.getMainHandItem());
        }
        if (!zumbi.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE)) {
            helper.fail("e o peito de ferro, é " + zumbi.getItemBySlot(EquipmentSlot.CHEST));
        }
        if (zumbi.getItemBySlot(EquipmentSlot.FEET).isEmpty()) helper.fail("e as botas também");

        limpa(minhas);
        helper.succeed();
    }

    /** <b>O arco não sobe de grau</b>: o esqueleto ganha armadura, e não um arco melhor. */
    @GameTest(maxTicks = 40)
    public void theBowNeverUpgradesOnlyTheArmour(GameTestHelper helper) {
        piso(helper);
        var quem = mago(helper, Necromancy.DIAMANTE);

        chama(helper, Spell.of(Shapes.TOUCH, Essences.SUMMON), Essences.SUMMON, quem,
                helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var minhas = as(helper, quem);
        if (minhas.isEmpty()) {
            helper.fail("devia vir uma");
            return;
        }
        var osso = minhas.stream().filter(m -> m instanceof Skeleton).findFirst().orElse(null);
        if (osso == null) {
            helper.fail("devia vir um esqueleto");
            return;
        }
        if (!osso.getMainHandItem().is(Items.BOW)) {
            helper.fail("o arco é sempre o arco, é " + osso.getMainHandItem());
        }
        if (!osso.getItemBySlot(EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET)) {
            helper.fail("mas o elmo é de diamante, é " + osso.getItemBySlot(EquipmentSlot.HEAD));
        }

        limpa(minhas);
        helper.succeed();
    }

    /**
     * No fundo do Fim, ela vem <b>a cavalo</b> — e o cavalo <b>não ocupa vaga</b>.
     *
     * <p>É a decisão que esta prova guarda: um necromante a cavalo tem o mesmo exército de um a pé.
     */
    @GameTest(maxTicks = 40)
    public void atTheDeepEndSheComesMountedAndTheHorseTakesNoSlot(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = mago(helper, 1.0f);

        chama(helper, Spell.of(Shapes.TOUCH, Essences.SUMMON), Essences.SUMMON, quem,
                helper.absoluteVec(new Vec3(4.5, 2, 4.5)));
        var minhas = as(helper, quem);

        var cavalo = minhas.stream().filter(m -> m instanceof SkeletonHorse).findFirst().orElse(null);
        if (cavalo == null) {
            helper.fail("no fundo do Fim vem cavalo, e vieram " + minhas.size() + " bichos");
            return;
        }
        var osso = minhas.stream().filter(m -> m instanceof Skeleton).findFirst().orElse(null);
        if (osso == null) {
            helper.fail("e o esqueleto que o monta");
            return;
        }
        if (osso.getVehicle() != cavalo) helper.fail("e ele tem de estar em cima dele");

        // dois bichos no mundo, uma vaga só ocupada
        if (Summons.quantas(level, quem) != 1) {
            helper.fail("o cavalo não ocupa vaga: devia contar uma, contou "
                    + Summons.quantas(level, quem));
        }
        if (!cavalo.getAttached(Summons.DATA).montaria()) helper.fail("e ele está marcado como montaria");

        limpa(minhas);
        helper.succeed();
    }

    /** E as duas peças novas estão na árvore, penduradas na Invocação. */
    @GameTest(maxTicks = 20)
    public void bothNewPartsHangOffTheSummon(GameTestHelper helper) {
        for (var qual : List.<net.thaumcraft.arcana.SpellPart>of(Essences.RAISE_DEAD, Modifiers.LEGION)) {
            var posto = net.thaumcraft.arcana.SkillTree.of(qual);
            if (posto == null) {
                helper.fail(qual.name() + " tem de estar na árvore");
                return;
            }
            if (posto.branch() != net.thaumcraft.arcana.SkillTree.Branch.DEFENSE) {
                helper.fail(qual.name() + " fica na Defesa, com a Invocação, e está em " + posto.branch());
            }
            if (!posto.needs().contains(Essences.SUMMON)) {
                helper.fail(qual.name() + " pende da Invocação, e pende de " + posto.needs());
            }
        }
        helper.succeed();
    }
}
