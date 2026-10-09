package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.torment.ContractTormentItem;
import net.thaumcraft.occulta.torment.Demonic;
import net.thaumcraft.occulta.torment.LordOfTormentEntity;
import net.thaumcraft.occulta.torment.Torment;
import net.thaumcraft.occulta.torment.TormentMaze;

import java.util.ArrayList;
import java.util.List;

/**
 * O Senhor do Tormento: o teto de dano, a fuga que leva gente consigo, o que ele larga, o golpe demoníaco
 * que passa pela armadura, o rótulo de quem é do inferno e o círculo de pedras que o chama.
 */
public class OccultaTormentLordGameTest {
    /** <b>Nenhum golpe lhe tira mais de cinco</b> — ou oito, se for demoníaco. */
    @GameTest
    public void fiveFromAnythingAndEightFromHell(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LordOfTormentEntity ele = põe(helper, 2, 2, 2);
        if (ele == null) return;

        float cheio = ele.getMaxHealth();
        ele.setHealth(cheio);
        ele.invulnerableTime = 0;
        ele.hurtServer(level, level.damageSources().magic(), 100.0f);
        float tirou = cheio - ele.getHealth();
        if (tirou > LordOfTormentEntity.TETO + 0.001f) {
            helper.fail("cem de dano comum tiram no máximo cinco, e tiraram " + tirou);
        }

        ele.setHealth(cheio);
        ele.invulnerableTime = 0;
        ele.hurtServer(level, Demonic.fonte(level, null), 100.0f);
        float demoníaco = cheio - ele.getHealth();
        if (demoníaco <= tirou) helper.fail("e o demoníaco tira mais do que o comum");
        if (demoníaco > LordOfTormentEntity.TETO_DEMONÍACO + 0.001f) {
            helper.fail("mas nunca mais de oito, e tirou " + demoníaco);
        }

        // e estouro nenhum lhe faz cócegas
        ele.setHealth(cheio);
        ele.invulnerableTime = 0;
        ele.hurtServer(level, level.damageSources().explosion(null, null), 100.0f);
        if (ele.getHealth() < cheio) helper.fail("estouro nenhum lhe tira nada");

        ele.discard();
        helper.succeed();
    }

    /**
     * <b>A fuga.</b> Chegado a metade da vida, fora do Tormento, ele some — e escreve o mandado de «começa
     * com o chefe» em quem o feriu, com o <b>mesmo andar</b> para todos.
     */
    @GameTest
    public void heFleesAtHalfAndTakesHisAttackersAlong(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LordOfTormentEntity ele = põe(helper, 2, 2, 2);
        if (ele == null) return;

        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        ele.setHealth(ele.getMaxHealth() * LordOfTormentEntity.METADE);
        ele.invulnerableTime = 0;
        ele.hurtServer(level, level.damageSources().playerAttack(quem), 1.0f);

        if (!ele.isRemoved()) helper.fail("a metade da vida é onde ele foge");
        if (!ele.oQueAnotou().contains(quem.getUUID())) helper.fail("e ele anota quem lhe bateu");

        var mandado = Torment.order(quem);
        if (mandado.oquê() != Torment.COM_O_CHEFE) {
            helper.fail("quem lhe bateu vai atrás dele, e com o chefe à espera");
        }
        if (mandado.andar() < 0 || mandado.andar() >= TormentMaze.LEVELS) {
            helper.fail("e para um dos seis andares, e deu " + mandado.andar());
        }
        Torment.order(quem, Torment.NADA, -1);
        helper.succeed();
    }

    /**
     * <b>Dentro do Tormento ele não foge</b> — e é por isso que lá, e só lá, ele morre e larga o
     * <b>Cozimento de Alma do Tormento</b>, que é a única fonte dele no mod.
     *
     * <p>O Tormento <b>fica guardado</b> como qualquer mundo, de modo que o que uma corrida anterior
     * deixou lá ainda lá está. Por isso a prova tranca o pedaço, <b>espera que ele carregue</b>, limpa o
     * que achar e só então mexe: sem a espera, a limpeza não vê nada e a conta sai com os restos de
     * ontem dentro.
     */
    @GameTest(maxTicks = 120)
    public void onlyInTheTormentDoesHeDieAndLeaveTheBrew(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        ServerLevel lá = Torment.level(server);
        if (lá == null) {
            helper.fail("o Tormento devia abrir");
            return;
        }

        BlockPos longe = new BlockPos(1000, 60, 1000);
        var volta = new net.minecraft.world.phys.AABB(longe).inflate(12.0);
        lá.setChunkForced(longe.getX() >> 4, longe.getZ() >> 4, true);

        helper.runAfterDelay(20, () -> {
            limpa(lá, volta);

            lá.setBlockAndUpdate(longe.below(), Blocks.STONE.defaultBlockState());
            var ele = OccultaEntities.LORD_OF_TORMENT.create(lá, EntitySpawnReason.TRIGGERED);
            if (ele == null) {
                helper.fail("ele devia nascer");
                return;
            }
            ele.snapTo(longe.getX() + 0.5, longe.getY(), longe.getZ() + 0.5, 0.0f, 0.0f);
            lá.addFreshEntity(ele);

            // a metade da vida, que cá fora o faria fugir, não o move
            ele.setHealth(ele.getMaxHealth() * LordOfTormentEntity.METADE);
            ele.invulnerableTime = 0;
            ele.hurtServer(lá, lá.damageSources().magic(), 1.0f);
            if (ele.isRemoved()) helper.fail("no Tormento ele não tem para onde fugir");

            // e aqui ele morre
            ele.setHealth(1.0f);
            ele.invulnerableTime = 0;
            ele.kill(lá);
            if (!ele.isDeadOrDying()) helper.fail("e morre");

            helper.runAfterDelay(20, () -> {
                List<ItemStack> caiu = new ArrayList<>();
                for (var coisa : lá.getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class, volta)) {
                    caiu.add(coisa.getItem());
                }
                if (!tem(caiu, OccultaItems.BREW_SOUL_TORMENT)) {
                    helper.fail("ele larga o Cozimento de Alma do Tormento, e largou " + caiu);
                }
                if (!tem(caiu, OccultaItems.DEMON_HEART)) helper.fail("e um Coração de Demônio");
                int livros = 0;
                for (var coisa : caiu) {
                    if (coisa.is(Items.ENCHANTED_BOOK)) livros++;
                }
                if (livros != LordOfTormentEntity.LIVROS) {
                    helper.fail("e dois livros encantados, e largou " + livros);
                }

                limpa(lá, volta);
                lá.setChunkForced(longe.getX() >> 4, longe.getZ() >> 4, false);
                helper.succeed();
            });
        });
    }

    /**
     * <b>Um por andar.</b> A sala do meio recebe um Senhor com metade da vida, e quem chegar depois
     * <b>não põe outro</b> — que é o que faz um coven inteiro brigar com um só chefe.
     */
    @GameTest(maxTicks = 120)
    public void oneLordPerFloorAndHalfAlive(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        ServerLevel lá = Torment.level(server);
        if (lá == null) {
            helper.fail("o Tormento devia abrir");
            return;
        }
        int andar = TormentMaze.LEVELS - 1;
        var caixa = new net.minecraft.world.phys.AABB(
                TormentMaze.ORIGIN_X, TormentMaze.floorOf(andar) - 4, TormentMaze.ORIGIN_Z,
                TormentMaze.ORIGIN_X + TormentMaze.SPAN_X, TormentMaze.floorOf(andar) + 8,
                TormentMaze.ORIGIN_Z + TormentMaze.SPAN_Z);
        lá.setChunkForced(TormentMaze.LORD_X >> 4, TormentMaze.LORD_Z >> 4, true);

        helper.runAfterDelay(20, () -> {
            limpa(lá, caixa);

            var primeiro = Torment.waitFor(lá, andar);
            if (primeiro == null) {
                helper.fail("um Senhor devia ficar à espera");
                return;
            }
            if (Math.abs(primeiro.getHealth()
                    - primeiro.getMaxHealth() * LordOfTormentEntity.METADE) > 0.5f) {
                helper.fail("e com metade da vida, e tem " + primeiro.getHealth());
            }
            if (Math.abs(primeiro.getX() - (TormentMaze.LORD_X + 0.5)) > 0.001
                    || Math.abs(primeiro.getZ() - (TormentMaze.LORD_Z + 0.5)) > 0.001) {
                helper.fail("e na sala do meio daquele andar");
            }

            helper.runAfterDelay(20, () -> {
                Torment.waitFor(lá, andar);
                var quantos = lá.getEntitiesOfClass(LordOfTormentEntity.class, caixa);
                if (quantos.size() != 1) helper.fail("um Senhor por andar, e há " + quantos.size());
                limpa(lá, caixa);
                lá.setChunkForced(TormentMaze.LORD_X >> 4, TormentMaze.LORD_Z >> 4, false);
                helper.succeed();
            });
        });
    }

    /** Tira dali tudo o que for Senhor do Tormento ou coisa no chão. */
    private static void limpa(ServerLevel onde, net.minecraft.world.phys.AABB caixa) {
        for (var cada : onde.getEntitiesOfClass(LordOfTormentEntity.class, caixa)) cada.discard();
        for (var cada : onde.getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, caixa)) {
            cada.discard();
        }
    }

    /** <b>O golpe demoníaco passa pela armadura</b>, que é o que a bola de fogo de alma leva. */
    @GameTest
    public void theInfernalBlowGoesThroughArmour(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Zombie couraçado = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        couraçado.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
                new ItemStack(Items.DIAMOND_CHESTPLATE));
        couraçado.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                new ItemStack(Items.DIAMOND_HELMET));
        couraçado.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
                new ItemStack(Items.DIAMOND_LEGGINGS));
        couraçado.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,
                new ItemStack(Items.DIAMOND_BOOTS));

        float cheio = couraçado.getMaxHealth();
        couraçado.setHealth(cheio);
        couraçado.invulnerableTime = 0;
        couraçado.hurtServer(level, level.damageSources().mobAttack(couraçado), 6.0f);
        float comArmadura = cheio - couraçado.getHealth();

        couraçado.setHealth(cheio);
        couraçado.invulnerableTime = 0;
        Demonic.bate(level, null, couraçado, 6.0f);
        float demoníaco = cheio - couraçado.getHealth();

        if (demoníaco <= comArmadura) {
            helper.fail("o golpe demoníaco passa pela armadura: deu " + demoníaco
                    + " contra " + comArmadura);
        }
        if (Math.abs(demoníaco - 6.0f) > 0.5f) {
            helper.fail("e passa por ela inteira: seis são seis, e deu " + demoníaco);
        }
        couraçado.discard();
        helper.succeed();
    }

    /**
     * <b>O tiro dele</b>: um <b>Ignianima</b> e <b>três</b> bolas de fogo de alma — nove, uma vez em dez.
     *
     * <p>A prova chama o tiro à mão, porque a meta que o chama precisa de uma briga a andar. O que ela
     * guarda é a conta: um feitiço e três bolas, e nunca menos.
     */
    @GameTest(maxTicks = 40)
    public void heThrowsOneSpellAndThreeFireballs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LordOfTormentEntity ele = põe(helper, 2, 3, 2);
        if (ele == null) return;
        ele.setNoAi(true);
        var alvo = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 6));

        ele.atira(level, alvo, 0.5f);

        helper.runAfterDelay(3, () -> {
            var volta = ele.getBoundingBox().inflate(8.0);
            int bolas = level.getEntitiesOfClass(
                    net.thaumcraft.occulta.torment.SoulfireEntity.class, volta).size();
            int feitiços = level.getEntitiesOfClass(
                    net.thaumcraft.occulta.symbol.SpellEffectEntity.class, volta).size();

            if (bolas < LordOfTormentEntity.BOLAS) {
                helper.fail("ele atira três bolas de fogo de alma de cada vez, e atirou " + bolas);
            }
            if (bolas > LordOfTormentEntity.BOLAS_DEMAIS) {
                helper.fail("e no máximo nove, e atirou " + bolas);
            }
            if (feitiços != 1) {
                helper.fail("e um Ignianima junto, e atirou " + feitiços);
            }

            for (var cada : level.getEntitiesOfClass(
                    net.thaumcraft.occulta.torment.SoulfireEntity.class, volta)) {
                cada.discard();
            }
            for (var cada : level.getEntitiesOfClass(
                    net.thaumcraft.occulta.symbol.SpellEffectEntity.class, volta)) {
                cada.discard();
            }
            alvo.discard();
            ele.discard();
            helper.succeed();
        });
    }

    /** <b>Quem é do inferno</b>: os quatro do jogo e os quatro do mod. */
    @GameTest
    public void whoIsOfHell(GameTestHelper helper) {
        var devem = List.of(EntityTypes.GHAST, EntityTypes.BLAZE, EntityTypes.MAGMA_CUBE, EntityTypes.WITHER,
                OccultaEntities.DEMON, OccultaEntities.IMP, OccultaEntities.LILITH,
                OccultaEntities.LORD_OF_TORMENT);
        for (var qual : devem) {
            if (!qual.builtInRegistryHolder().is(Demonic.MARCA)) {
                helper.fail("é do inferno e não está no rótulo: " + qual);
            }
        }
        // e gente nunca é, mesmo que alguém a ponha no rótulo
        ServerPlayer quem = helper.makeMockServerPlayerInLevel();
        if (Demonic.é(quem)) helper.fail("gente nunca é do inferno");

        var zumbi = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 2, 2));
        if (Demonic.é(zumbi)) helper.fail("nem um zumbi, que é morto-vivo e não demônio");
        zumbi.discard();
        helper.succeed();
    }

    /**
     * <b>O círculo de pedras</b>: o desenho de onze por onze do original, pedra a pedra.
     *
     * <p>Ele é levantado à mão dentro da arena, conferido, e depois se tira <b>uma</b> arquitrave para ver
     * que ele deixa de servir.
     */
    @GameTest(maxTicks = 100)
    public void theStoneCircleIsTheOriginals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(2, 2, 2));

        BlockPos tirar = null;
        for (int z = 0; z < ContractTormentItem.DESENHO.length; z++) {
            for (int x = 0; x < ContractTormentItem.DESENHO[z].length; x++) {
                int quanto = ContractTormentItem.DESENHO[z][x];
                BlockPos casa = meio.offset(x - 5, 0, z - 5);
                level.setBlockAndUpdate(casa.below(), Blocks.STONE.defaultBlockState());
                for (int h = 0; h <= 3; h++) {
                    level.setBlockAndUpdate(casa.above(h), Blocks.AIR.defaultBlockState());
                }
                if (quanto == 2) {
                    level.setBlockAndUpdate(casa, Blocks.STONE.defaultBlockState());
                } else if (quanto == 3) {
                    level.setBlockAndUpdate(casa.above(2), Blocks.STONE.defaultBlockState());
                    if (tirar == null) tirar = casa.above(2);
                } else if (quanto == 4) {
                    for (int h = 0; h <= 2; h++) {
                        level.setBlockAndUpdate(casa.above(h), Blocks.STONE.defaultBlockState());
                    }
                }
            }
        }

        if (!ContractTormentItem.círculo(level, meio)) {
            helper.fail("o círculo levantado pelo desenho do original devia servir");
        }

        // e tirada uma arquitrave, não serve mais
        if (tirar != null) {
            level.setBlockAndUpdate(tirar, Blocks.AIR.defaultBlockState());
            if (ContractTormentItem.círculo(level, meio)) {
                helper.fail("faltando uma arquitrave, ele não serve");
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ a mão

    private static LordOfTormentEntity põe(GameTestHelper helper, int x, int y, int z) {
        var ele = helper.spawn(OccultaEntities.LORD_OF_TORMENT, new BlockPos(x, y, z));
        if (ele == null) helper.fail("o Senhor do Tormento devia nascer");
        return ele;
    }

    private static boolean tem(List<ItemStack> onde, net.minecraft.world.item.Item oquê) {
        for (var coisa : onde) {
            if (coisa.is(oquê)) return true;
        }
        return false;
    }
}
