package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.hunter.WitchHunterEntity;
import net.thaumcraft.occulta.hunter.WitchHunters;

/**
 * O Caçador de Bruxas: o teto da pancada, a lista curta do que ele caça, e o relógio que o chama.
 *
 * <p>A prova que carrega a fatia é a do <b>relógio</b>. Um caçador que aparecesse de graça seria mais um
 * monstro; o que faz dele a resposta do mundo ao ofício é que ele <b>vem porque alguém fez alguma coisa</b>,
 * e vem com atraso.
 */
public class OccultaWitchHunterGameTest {
    /**
     * O chão da arena, <b>largo o bastante para o anel deles</b>.
     *
     * <p>Os caçadores nascem num anel de três a oito blocos de quem os chamou, e quem os põe <b>desce até
     * achar chão</b>. Num chão de dez por dez, metade do anel cai fora dele — e aí a conta desce até o fundo
     * do mundo e o caçador nasce a setenta blocos de distância, vivo e longe da vista.
     *
     * <p>A prova passava por sorte. Agora o chão cobre o anel inteiro: de menos quatro a catorze.
     */
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = -4; x < 15; x++) {
            for (int z = -4; z < 15; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 2, z)),
                        Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 3, z)),
                        Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Os números dele são os do original. */
    @GameTest(maxTicks = 20)
    public void hisNumbersAreTheOriginals(GameTestHelper helper) {
        if (WitchHunterEntity.VIDA != 30.0) helper.fail("ele tem trinta de vida");
        if (WitchHunterEntity.TETO_DA_PANCADA != 9.0f) helper.fail("e nenhuma pancada lhe tira mais de nove");
        if (WitchHunterEntity.LIMPA_O_VENENO != 20) helper.fail("limpa-se do veneno de segundo em segundo");
        if (WitchHunterEntity.QUANTAS_PELES != 3) helper.fail("e tem três caras");
        if (WitchHunters.CHANCE_DE_SER_NOTADO != 0.1) helper.fail("a magia negra é notada uma vez em dez");
        if (WitchHunters.DEMORA != 2400) helper.fail("e eles só podem vir dois minutos depois");
        if (WitchHunters.CHANCE_DE_VIREM != 0.01) helper.fail("com uma chance em cem por volta");
        if (WitchHunters.QUANTOS != 2) helper.fail("e vêm dois");
        helper.succeed();
    }

    /** <b>Nenhuma pancada lhe tira mais de nove</b>, por maior que seja. */
    @GameTest(maxTicks = 40)
    public void noBlowTakesMoreThanNine(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var caçador = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(3, 2, 3));
        float tinha = caçador.getHealth();

        caçador.hurtServer(level, level.damageSources().generic(), 1000.0f);
        float tirou = tinha - caçador.getHealth();
        if (tirou > WitchHunterEntity.TETO_DA_PANCADA + 0.001f) {
            helper.fail("mil de dano tiram nove, e tiraram " + tirou);
        }
        if (tirou <= 0.0f) helper.fail("mas tiram alguma coisa");

        caçador.discard();
        helper.succeed();
    }

    /** E a mão de um guarda ou de outro caçador não lhe tira nada: são do mesmo lado. */
    @GameTest(maxTicks = 40)
    public void hisOwnSideCannotHurtHim(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var um = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(2, 2, 2));
        var outro = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(5, 2, 5));
        var guarda = helper.spawn(OccultaEntities.VILLAGE_GUARD, new BlockPos(2, 2, 5));
        float tinha = um.getHealth();

        um.hurtServer(level, level.damageSources().mobAttack(outro), 5.0f);
        if (um.getHealth() != tinha) helper.fail("outro caçador não o fere");

        um.hurtServer(level, level.damageSources().mobAttack(guarda), 5.0f);
        if (um.getHealth() != tinha) helper.fail("nem um guarda de aldeia");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(5, 2, 2));
        um.hurtServer(level, level.damageSources().mobAttack(zumbi), 5.0f);
        if (um.getHealth() >= tinha) helper.fail("mas um zumbi, sim");

        um.discard();
        outro.discard();
        guarda.discard();
        zumbi.discard();
        helper.succeed();
    }

    /** <b>A lista do que ele caça é curta</b>, e é de propósito: ele não é um monstro. */
    @GameTest(maxTicks = 40)
    public void heHuntsAShortList(GameTestHelper helper) {
        piso(helper);
        var caçador = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(1, 2, 1));

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(3, 2, 1));
        if (!caçador.éCaça(zumbi)) helper.fail("ele caça morto-vivo");

        var bruxa = helper.spawn(EntityTypes.WITCH, new BlockPos(5, 2, 1));
        if (!caçador.éCaça(bruxa)) helper.fail("e bruxa");

        var lobo = helper.spawn(OccultaEntities.WOLFMAN, new BlockPos(7, 2, 1));
        if (!caçador.éCaça(lobo)) helper.fail("e lobisomem");

        var vampiro = helper.spawn(OccultaEntities.VAMPIRE, new BlockPos(1, 2, 4));
        if (!caçador.éCaça(vampiro)) helper.fail("e vampiro");

        var aldeão = helper.spawn(EntityTypes.VILLAGER, new BlockPos(3, 2, 4));
        if (caçador.éCaça(aldeão)) helper.fail("mas não aldeão");

        var porco = helper.spawn(EntityTypes.PIG, new BlockPos(5, 2, 4));
        if (caçador.éCaça(porco)) helper.fail("nem porco");

        var creeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(7, 2, 4));
        if (caçador.éCaça(creeper)) helper.fail("nem creeper: ele não é um monstro, é um homem com um trabalho");

        for (var quem : new net.minecraft.world.entity.Entity[]{
                caçador, zumbi, bruxa, lobo, vampiro, aldeão, porco, creeper}) {
            quem.discard();
        }
        helper.succeed();
    }

    /** <b>E gente, só a que ele veio buscar.</b> */
    @GameTest(maxTicks = 40)
    public void andOfPeopleOnlyTheOneHeCameFor(GameTestHelper helper) {
        piso(helper);
        var caçador = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(3, 2, 3));
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);

        if (caçador.éCaça(quem)) helper.fail("sem o ter vindo buscar, não");
        if (caçador.quemEleQuer() != null) helper.fail("e ele não começa a querer ninguém");

        caçador.vemBuscar(quem);
        if (!caçador.éCaça(quem)) helper.fail("vindo buscá-lo, sim");
        if (!quem.getUUID().equals(caçador.quemEleQuer())) helper.fail("e sabe de quem se trata");

        caçador.discard();
        helper.succeed();
    }

    /** O veneno não pega nele. */
    @GameTest(maxTicks = 60)
    public void poisonDoesNotStick(GameTestHelper helper) {
        piso(helper);
        var caçador = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(3, 2, 3));
        caçador.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
        caçador.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600));

        helper.succeedWhen(() -> {
            if (caçador.hasEffect(MobEffects.POISON)) {
                throw helper.assertionException("o veneno não pega num caçador");
            }
            if (!caçador.hasEffect(MobEffects.SLOWNESS)) {
                throw helper.assertionException("mas o resto pega: é só do veneno que ele se limpa");
            }
            caçador.discard();
        });
    }

    /** Com besta ele atira; sem besta, avança. */
    @GameTest(maxTicks = 40)
    public void withACrossbowHeShootsFromAfar(GameTestHelper helper) {
        piso(helper);
        var caçador = helper.spawn(OccultaEntities.WITCH_HUNTER, new BlockPos(3, 2, 3));
        caçador.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(OccultaItems.CROSSBOW_PISTOL));
        if (!caçador.aimingFromAfar()) helper.fail("com a besta na mão ele atira");

        caçador.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (caçador.aimingFromAfar()) helper.fail("e desarmado ele avança");

        caçador.discard();
        helper.succeed();
    }

    /** <b>O relógio</b>: a magia negra é notada, e a partir daí há tempo. */
    @GameTest(maxTicks = 60)
    public void blackMagicStartsAClock(GameTestHelper helper) {
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        WitchHunters.esquece(quem);
        if (WitchHunters.notado(quem)) helper.fail("ninguém começa notado");

        // uma vez em dez é pouco; em duzentas voltas, ou foi notado ou a conta está errada
        for (int volta = 0; volta < 200 && !WitchHunters.notado(quem); volta++) {
            WitchHunters.magiaNegra(quem);
        }
        if (!WitchHunters.notado(quem)) {
            helper.fail("em duzentas magias negras alguém devia ter reparado");
            return;
        }

        WitchHunters.esquece(quem);
        if (WitchHunters.notado(quem)) helper.fail("e o relógio se apaga quando eles vêm");
        helper.succeed();
    }

    /** <b>E quando eles vêm, vêm dois</b> — e já sabem de quem se trata. */
    @GameTest(maxTicks = 60)
    public void whenTheyComeTheyComeInTwos(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var quem = helper.makeMockServerPlayerInLevel();
        quem.setGameMode(GameType.SURVIVAL);
        quem.snapTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2, 5.5)));

        int vieram = WitchHunters.manda(level, quem);
        if (vieram <= 0) {
            helper.fail("num chão de dez por dez eles acham onde pôr os pés");
            return;
        }
        if (vieram > WitchHunters.QUANTOS) helper.fail("nunca mais de dois; vieram " + vieram);

        var volta = new AABB(quem.blockPosition()).inflate(WitchHunters.LONGE + 2);
        int acharam = 0;
        for (WitchHunterEntity caçador
                : level.getEntitiesOfClass(WitchHunterEntity.class, volta)) {
            if (!quem.getUUID().equals(caçador.quemEleQuer())) continue;
            acharam++;
            if (!caçador.isPersistenceRequired()) helper.fail("e não somem sozinhos");
            caçador.discard();
        }
        if (acharam != vieram) {
            helper.fail("todos sabem de quem vieram buscar: vieram " + vieram + ", sabem " + acharam);
        }
        helper.succeed();
    }
}
