package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.vampire.Blood;
import net.thaumcraft.occulta.vampire.FollowerEntity;
import net.thaumcraft.occulta.vampire.GobletBlood;
import net.thaumcraft.occulta.vampire.GobletItem;
import net.thaumcraft.occulta.vampire.LilithEntity;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampireRitual;

/**
 * <b>A porta de entrada</b>: o rito, Elle, Lilith e o sangue que faz um vampiro.
 *
 * <p>A prova que carrega a fatia é a de que <b>Lilith não morre</b>. Um chefe que se mata é um saco de vida
 * com um prémio dentro; um chefe que <b>não</b> se mata, e que no fim do combate <b>oferece</b> alguma coisa,
 * é outra espécie de coisa — e é a melhor ideia do mod inteiro.
 */
public class OccultaLilithGameTest {
    private static void piso(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 12; x++) {
            for (int z = 0; z < 12; z++) {
                level.setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 1, z)),
                        Blocks.STONE.defaultBlockState());
            }
        }
    }

    /** Um jogador de mentira a quem as coisas custem. */
    private static Player mortal(Player quem) {
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        }
        quem.getAbilities().instabuild = false;
        quem.onUpdateAbilities();
        return quem;
    }

    /** Desenha o rito inteiro à volta deste lugar. */
    private static void desenhaORito(GameTestHelper helper, BlockPos meio) {
        ServerLevel level = helper.getLevel();
        for (int dx = -VampireRitual.RAIO; dx <= VampireRitual.RAIO; dx++) {
            for (int dz = -VampireRitual.RAIO; dz <= VampireRitual.RAIO; dz++) {
                level.setBlockAndUpdate(meio.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(meio.offset(dx, 2, dz), Blocks.AIR.defaultBlockState());
            }
        }
        for (int[] onde : VampireRitual.ANEL) {
            level.setBlockAndUpdate(meio.offset(onde[0], 0, onde[1]),
                    Blocks.TRIPWIRE.defaultBlockState());
        }
        for (int[] onde : VampireRitual.CANTOS) {
            level.setBlockAndUpdate(meio.offset(onde[0], 0, onde[1]), Blocks.TORCH.defaultBlockState());
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                level.setBlockAndUpdate(meio.offset(dx, 0, dz),
                        Blocks.REDSTONE_WIRE.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(meio, Blocks.SKELETON_SKULL.defaultBlockState());
    }

    /** O rito se lê quando está desenhado, e não se lê quando falta uma coisa. */
    @GameTest(maxTicks = 60)
    public void theRiteIsReadWhenItIsDrawn(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(5, 2, 5));

        if (VampireRitual.desenhado(level, meio)) helper.fail("um chão vazio não é um rito");
        desenhaORito(helper, meio);
        if (!VampireRitual.desenhado(level, meio)) helper.fail("o rito inteiro devia ler-se");

        // tirando uma tocha, deixa de se ler
        level.setBlockAndUpdate(meio.offset(3, 0, 3), Blocks.AIR.defaultBlockState());
        if (VampireRitual.desenhado(level, meio)) helper.fail("sem as quatro tochas não há rito");
        level.setBlockAndUpdate(meio.offset(3, 0, 3), Blocks.TORCH.defaultBlockState());

        // e com uma coisa por cima, também não
        level.setBlockAndUpdate(meio.offset(2, 1, 2), Blocks.STONE.defaultBlockState());
        if (VampireRitual.desenhado(level, meio)) helper.fail("um desenho não se lê com coisas em cima");
        level.setBlockAndUpdate(meio.offset(2, 1, 2), Blocks.AIR.defaultBlockState());

        // e um crânio que não é de esqueleto não serve
        level.setBlockAndUpdate(meio, Blocks.ZOMBIE_HEAD.defaultBlockState());
        if (VampireRitual.desenhado(level, meio)) helper.fail("é um crânio de esqueleto, e não outro");
        helper.succeed();
    }

    /**
     * <b>O cálice enche com a galinha</b>, e só com a Boline na mão e o rito debaixo.
     */
    @GameTest(maxTicks = 60)
    public void theChickenFillsTheGoblet(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(5, 2, 5));
        desenhaORito(helper, meio);

        Player quem = mortal(helper.makeMockServerPlayerInLevel());
        // a mão de um jogador é a casa escolhida da barra: o cálice vai noutra casa
        ItemStack cálice = new ItemStack(OccultaItems.GOBLET);
        quem.getInventory().setItem(1, cálice);

        var galinha = helper.spawn(net.minecraft.world.entity.EntityTypes.CHICKEN,
                new BlockPos(5, 3, 5));

        // sem a Boline, nada acontece
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        GobletItem.aGalinha(level, quem, galinha);
        if (GobletItem.cheio(quem.getInventory().getItem(1))) {
            helper.fail("sem a Boline a galinha não enche nada");
        }

        // com ela, enche
        quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                new ItemStack(OccultaItems.BOLINE));
        GobletItem.aGalinha(level, quem, galinha);
        var dentro = GobletItem.dentro(quem.getInventory().getItem(1));
        if (dentro == null || dentro.fonte() != GobletBlood.Fonte.GALINHA) {
            helper.fail("o cálice devia ter enchido de sangue de galinha");
        }

        galinha.discard();
        helper.succeed();
    }

    /**
     * <b>Beber o sangue de Lilith faz um vampiro</b> — e o de galinha não faz nada.
     */
    @GameTest(maxTicks = 60)
    public void onlyHerBloodTurnsYou(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        Player comGalinha = mortal(helper.makeMockServerPlayerInLevel());
        ItemStack galinha = new ItemStack(OccultaItems.GOBLET);
        GobletItem.enche(galinha, GobletBlood.GALINHA);
        ((GobletItem) galinha.getItem()).finishUsingItem(galinha, level, comGalinha);
        if (Vampire.é(comGalinha)) helper.fail("sangue de galinha não faz vampiro nenhum");
        if (GobletItem.cheio(galinha)) helper.fail("mas o cálice esvazia-se na mesma");

        Player quem = mortal(helper.makeMockServerPlayerInLevel());
        ItemStack dela = new ItemStack(OccultaItems.GOBLET);
        GobletItem.enche(dela, GobletBlood.LILITH);
        ((GobletItem) dela.getItem()).finishUsingItem(dela, level, quem);
        if (Vampire.grauDe(quem) != 1) helper.fail("o sangue dela devia tê-lo virado");
        if (Blood.de(quem) != 0) helper.fail("e o sangue de gente devia ter secado");
        helper.succeed();
    }

    /**
     * <b>Lilith não morre.</b>
     *
     * <p>É a prova que carrega a fatia: o golpe que a mataria devolve-lhe a vida cheia e fá-la amiga.
     */
    @GameTest(maxTicks = 60)
    public void she_cannot_be_killed(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        var lilith = helper.spawn(net.thaumcraft.occulta.OccultaEntities.LILITH,
                new BlockPos(5, 2, 5));

        if (lilith.getMaxHealth() != LilithEntity.VIDA) helper.fail("duzentos de vida");
        if (lilith.amiga()) helper.fail("ela não começa amiga");

        // nenhuma pancada lhe tira mais de doze
        float antes = lilith.getHealth();
        lilith.hurtServer(level, level.damageSources().magic(), 1000.0f);
        float tirou = antes - lilith.getHealth();
        if (tirou > LilithEntity.TETO_DA_PANCADA + 0.01f) {
            helper.fail("o teto dela é doze, e esta pancada tirou " + tirou);
        }

        // e matá-la não a mata
        /*
         * Ela tem as batidas de graça de todo vivo, e numa prova não há tempo de as esperar: põe-se a vida a
         * um e dá-se o golpe que a mataria.
         */
        Player quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(lilith.getX() + 1.0, lilith.getY(), lilith.getZ(), 0.0f, 0.0f);
        lilith.invulnerableTime = 0;
        lilith.setHealth(1.0f);
        lilith.hurtServer(level, level.damageSources().playerAttack(
                (net.minecraft.server.level.ServerPlayer) quem), 1000.0f);
        if (!lilith.amiga()) helper.fail("ela devia ter ficado amiga em vez de morrer");
        if (!lilith.isAlive()) helper.fail("e viva");
        if (lilith.getHealth() != lilith.getMaxHealth()) helper.fail("e com a vida cheia");

        // e amiga, ela já não apanha
        float cheia = lilith.getHealth();
        lilith.hurtServer(level, level.damageSources().magic(), 10.0f);
        if (lilith.getHealth() != cheia) helper.fail("amiga, ela não se deixa ferir mais");

        lilith.discard();
        helper.succeed();
    }

    /**
     * <b>E o que ela dá</b>: o cálice cheio do sangue dela, a cura com alho, e o grau com a papoula.
     */
    @GameTest(maxTicks = 80)
    public void what_she_gives(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        // a quem não é vampiro, o sangue dela
        var lilith = amiga(helper, new BlockPos(5, 2, 5));
        Player quem = mortal(helper.makeMockServerPlayerInLevel());
        quem.snapTo(lilith.getX(), lilith.getY(), lilith.getZ(), 0.0f, 0.0f);
        quem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(OccultaItems.GOBLET));
        lilith.interact(quem, net.minecraft.world.InteractionHand.MAIN_HAND,
                net.minecraft.world.phys.Vec3.ZERO);

        boolean deu = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        quem.getBoundingBox().inflate(4.0)).stream()
                .anyMatch(coisa -> {
                    var dentro = GobletItem.dentro(coisa.getItem());
                    return dentro != null && dentro.fonte() == GobletBlood.Fonte.LILITH;
                });
        if (!deu) helper.fail("ela devia ter-lhe dado o cálice cheio do sangue dela");
        if (lilith.isAlive()) helper.fail("e ido-se embora depois");

        // e a quem é vampiro, a cura
        var outra = amiga(helper, new BlockPos(8, 2, 8));
        Player vamp = mortal(helper.makeMockServerPlayerInLevel());
        Vampire.levantaOTeto(vamp, Vampire.TETO);
        Vampire.grau(vamp, 4);
        vamp.snapTo(outra.getX(), outra.getY(), outra.getZ(), 0.0f, 0.0f);
        vamp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(OccultaItems.GARLIC));
        outra.interact(vamp, net.minecraft.world.InteractionHand.MAIN_HAND,
                net.minecraft.world.phys.Vec3.ZERO);
        if (Vampire.é(vamp)) helper.fail("o alho devia tê-lo curado");

        helper.succeed();
    }

    /** Uma Lilith já vencida, que é como ela fica depois do combate. */
    private static LilithEntity amiga(GameTestHelper helper, BlockPos onde) {
        ServerLevel level = helper.getLevel();
        var lilith = helper.spawn(net.thaumcraft.occulta.OccultaEntities.LILITH, onde);
        Player quem = helper.makeMockServerPlayerInLevel();
        quem.snapTo(lilith.getX() + 1.0, lilith.getY(), lilith.getZ(), 0.0f, 0.0f);
        lilith.invulnerableTime = 0;
        lilith.setHealth(1.0f);
        lilith.hurtServer(level, level.damageSources().playerAttack(
                (net.minecraft.server.level.ServerPlayer) quem), 1000.0f);
        return lilith;
    }

    /**
     * <b>Elle procura lava</b>, e achando-a faz dela a casa — e esquece quem a chamou.
     */
    @GameTest(maxTicks = 80)
    public void elle_looks_for_lava(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();

        var elle = helper.spawn(net.thaumcraft.occulta.OccultaEntities.FOLLOWER,
                new BlockPos(5, 2, 5));
        if (!elle.fireImmune()) helper.fail("a casa dela é um lago de lava: ela não arde");
        if (elle.casa() != null) helper.fail("ela não nasce com casa");

        // sem lava, não acha nada
        for (int n = 0; n < 40; n++) elle.tick();
        if (elle.casa() != null) helper.fail("num campo seco não há casa para ela");

        elle.discard();
        helper.succeed();
    }

    /** E um lago de lava lê-se quando é grande o bastante, e não quando é uma poça. */
    @GameTest(maxTicks = 60)
    public void a_lava_pool_is_a_big_thing(GameTestHelper helper) {
        piso(helper);
        ServerLevel level = helper.getLevel();
        BlockPos meio = helper.absolutePos(new BlockPos(6, 2, 6));

        level.setBlockAndUpdate(meio, Blocks.LAVA.defaultBlockState());
        if (FollowerEntity.éLago(level, meio)) helper.fail("uma poça de um bloco não é um lago");

        for (int dx = -FollowerEntity.LAGO; dx <= FollowerEntity.LAGO; dx++) {
            for (int dz = -FollowerEntity.LAGO; dz <= FollowerEntity.LAGO; dz++) {
                if (dx * dx + dz * dz > FollowerEntity.LAGO * FollowerEntity.LAGO) continue;
                BlockPos onde = meio.offset(dx, 0, dz);
                level.setBlockAndUpdate(onde, Blocks.LAVA.defaultBlockState());
                level.setBlockAndUpdate(onde.above(), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(onde.above(2), Blocks.AIR.defaultBlockState());
            }
        }
        if (!FollowerEntity.éLago(level, meio)) helper.fail("treze blocos de boca já são um lago");

        // com um teto por cima, já não é
        level.setBlockAndUpdate(meio.above(), Blocks.STONE.defaultBlockState());
        if (FollowerEntity.éLago(level, meio)) helper.fail("um lago precisa de céu por cima");
        helper.succeed();
    }

    /** O Cálice, a Boline, Elle e Lilith estão todos no jogo. */
    @GameTest(maxTicks = 20)
    public void the_door_is_all_there(GameTestHelper helper) {
        for (var qual : java.util.List.of(OccultaItems.BOLINE, OccultaItems.GOBLET)) {
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(qual))) {
                helper.fail("falta um item da porta");
            }
        }
        for (var qual : java.util.List.of("follower", "lilith", "lilith_spell")) {
            if (!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.containsKey(
                    net.thaumcraft.Thaumcraft.id(qual))) {
                helper.fail("falta " + qual);
            }
        }
        helper.succeed();
    }
}
