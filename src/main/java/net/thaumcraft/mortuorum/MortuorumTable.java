package net.thaumcraft.mortuorum;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Page;

import java.util.List;

/**
 * A aba do Ars Mortuorum no Thaumonomicon.
 *
 * <p>O Necromancy não é um addon de Thaumcraft: lá não há pesquisa nenhuma, as coisas fazem-se na bancada e
 * pronto. A árvore daqui é <b>do porte</b>, e não do original — é o que põe o ramo dentro do livro, do jeito que
 * os outros ramos entraram. O que cada pesquisa ensina, porém, é o que o original faz: as receitas são as dele.
 */
public final class MortuorumTable {
    private MortuorumTable() {
    }

    public static void research() {
        ThaumcraftApi.research("AM_INTRO", Mortuorum.CATEGORY)
                .at(0, 0)
                .icon(() -> new ItemStack(MortuorumItems.NECRONOMICON))
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.AM_INTRO.1"))
                .register();

        ThaumcraftApi.research("AM_ORGANS", Mortuorum.CATEGORY)
                .at(-2, 0)
                .icon(() -> new ItemStack(MortuorumItems.ORGANS.get("heart")))
                .parents("AM_INTRO")
                .round()
                .auto()
                .pages(Page.text("tc.research_page.AM_ORGANS.1"))
                .register();

        ThaumcraftApi.research("AM_BONE_NEEDLE", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DEATH, 2).add(Aspects.CRAFT, 2))
                .at(-2, -2)
                .icon(() -> new ItemStack(MortuorumItems.BONE_NEEDLE))
                .parents("AM_ORGANS")
                .round()
                .pages(Page.text("tc.research_page.AM_BONE_NEEDLE.1"), Page.crafting("AMBoneNeedle"))
                .register();

        ThaumcraftApi.research("AM_SEWING", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.CRAFT, 3).add(Aspects.MECHANISM, 3).add(Aspects.DEATH, 2))
                .at(-4, -2)
                .icon(() -> new ItemStack(MortuorumItems.SEWING_MACHINE))
                .parents("AM_BONE_NEEDLE")
                .pages(Page.text("tc.research_page.AM_SEWING.1"), Page.crafting("AMSewingMachine"))
                .register();

        ThaumcraftApi.research("AM_BODY_PARTS", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.FLESH, 4).add(Aspects.DEATH, 3).add(Aspects.CRAFT, 2))
                .at(-6, -2)
                .icon(() -> new ItemStack(MortuorumItems.PART_ITEMS.get("zombie_torso")))
                .parents("AM_SEWING")
                .pages(Page.text("tc.research_page.AM_BODY_PARTS.1"))
                .register();

        ThaumcraftApi.research("AM_BLOOD", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.LIFE, 3).add(Aspects.WATER, 2))
                .at(2, 0)
                .icon(() -> new ItemStack(MortuorumItems.JAR_OF_BLOOD))
                .parents("AM_INTRO")
                .round()
                .pages(Page.text("tc.research_page.AM_BLOOD.1"), Page.crafting("AMJarOfBlood"), Page.crafting("AMBucketBlood"))
                .register();

        ThaumcraftApi.research("AM_SCYTHE", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DEATH, 4).add(Aspects.TOOL, 3).add(Aspects.SOUL, 2))
                .at(2, 2)
                .icon(() -> new ItemStack(MortuorumItems.SCYTHE_ITEM))
                .parents("AM_BLOOD")
                .pages(Page.text("tc.research_page.AM_SCYTHE.1"), Page.crafting("AMScythe"))
                .register();

        ThaumcraftApi.research("AM_SCYTHE_BONE", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DEATH, 5).add(Aspects.TOOL, 4).add(Aspects.SOUL, 3))
                .at(4, 2)
                .icon(() -> new ItemStack(MortuorumItems.SCYTHE_BONE_ITEM))
                .parents("AM_SCYTHE")
                .pages(Page.text("tc.research_page.AM_SCYTHE_BONE.1"), Page.crafting("AMScytheBone"))
                .register();

        ThaumcraftApi.research("AM_SOUL", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.SOUL, 4).add(Aspects.VOID, 2))
                .at(4, 0)
                .icon(() -> new ItemStack(MortuorumItems.SOUL_IN_A_JAR))
                .parents("AM_SCYTHE")
                .round()
                .pages(Page.text("tc.research_page.AM_SOUL.1"))
                .register();

        ThaumcraftApi.research("AM_NECRONOMICON", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.MIND, 4).add(Aspects.DEATH, 3).add(Aspects.MAGIC, 3))
                .at(0, -2)
                .icon(() -> new ItemStack(MortuorumItems.NECRONOMICON))
                .parents("AM_BLOOD")
                .pages(Page.text("tc.research_page.AM_NECRONOMICON.1"), Page.crafting("AMNecronomicon"))
                .register();

        ThaumcraftApi.research("AM_ALTAR", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DEATH, 5).add(Aspects.SOUL, 4).add(Aspects.EXCHANGE, 3))
                .at(0, -4)
                .icon(() -> new ItemStack(MortuorumItems.SUMMONING_ALTAR))
                .parents("AM_NECRONOMICON")
                .pages(Page.text("tc.research_page.AM_ALTAR.1"))
                .register();

        ThaumcraftApi.research("AM_MINION", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.SOUL, 5).add(Aspects.FLESH, 5).add(Aspects.MAN, 3))
                .at(-3, -4)
                .icon(() -> new ItemStack(MortuorumItems.BRAIN_ON_A_STICK))
                .parents("AM_ALTAR", "AM_BODY_PARTS")
                .pages(Page.text("tc.research_page.AM_MINION.1"), Page.crafting("AMBrainOnAStick"))
                .register();

        ThaumcraftApi.research("AM_TEDDY", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.CLOTH, 4).add(Aspects.SOUL, 3).add(Aspects.BEAST, 2))
                .at(-5, -4)
                .icon(() -> new ItemStack(Items.LEATHER))
                .parents("AM_MINION")
                .round()
                .pages(Page.text("tc.research_page.AM_TEDDY.1"))
                .register();

        ThaumcraftApi.research("AM_SOUL_HEART", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.SOUL, 5).add(Aspects.LIFE, 4).add(Aspects.EXCHANGE, 3))
                .at(2, -4)
                .icon(() -> new ItemStack(MortuorumItems.SOUL_HEART))
                .parents("AM_ALTAR", "AM_SOUL")
                .pages(Page.text("tc.research_page.AM_SOUL_HEART.1"))
                .register();

        ThaumcraftApi.research("AM_ISAAC", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DEATH, 4).add(Aspects.MAN, 3).add(Aspects.WATER, 2))
                .at(4, -4)
                .icon(() -> new ItemStack(MortuorumItems.ISAACS_HEAD))
                .parents("AM_SOUL_HEART")
                .round()
                .pages(Page.text("tc.research_page.AM_ISAAC.1"))
                .register();

        ThaumcraftApi.research("AM_NIGHT_CRAWLER", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DARKNESS, 4).add(Aspects.BEAST, 3).add(Aspects.DEATH, 3))
                .at(6, -4)
                .icon(() -> new ItemStack(Items.ENDER_PEARL))
                .parents("AM_ISAAC")
                .round()
                .pages(Page.text("tc.research_page.AM_NIGHT_CRAWLER.1"))
                .register();

        ThaumcraftApi.research("AM_SKULL_WALL", Mortuorum.CATEGORY)
                .aspects(new AspectList().add(Aspects.DEATH, 3).add(Aspects.EARTH, 3))
                .at(-4, 2)
                .icon(() -> new ItemStack(MortuorumItems.SKULL_WALL))
                .parents("AM_INTRO")
                .round()
                .pages(Page.text("tc.research_page.AM_SKULL_WALL.1"))
                .register();
    }

    /** As receitas que o livro mostra: as mesmas da bancada, montadas depois de os itens existirem. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("AMBoneNeedle", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.BONE_NEEDLE), 1, 1,
                List.of(List.of(new ItemStack(Items.BONE_MEAL)))));

        ThaumcraftApi.bookRecipe("AMSewingMachine", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.SEWING_MACHINE), 3, 3, List.of(
                        List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.STRING)), List.of(new ItemStack(MortuorumItems.BONE_NEEDLE)),
                        List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_INGOT)))));

        ThaumcraftApi.bookRecipe("AMBrainOnAStick", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.BRAIN_ON_A_STICK), 2, 2, List.of(
                        List.of(new ItemStack(Items.FISHING_ROD)), List.<ItemStack>of(),
                        List.<ItemStack>of(), List.of(new ItemStack(MortuorumItems.ORGANS.get("brains"))))));

        ThaumcraftApi.bookRecipe("AMNecronomicon", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.NECRONOMICON), 3, 3, List.of(
                        List.of(new ItemStack(Items.LEATHER)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)), List.of(new ItemStack(Items.LEATHER)),
                        List.of(new ItemStack(Items.INK_SAC)), List.of(new ItemStack(Items.BOOK)), List.of(new ItemStack(Items.FEATHER)),
                        List.of(new ItemStack(Items.LEATHER)), List.of(new ItemStack(Items.NETHER_WART)), List.of(new ItemStack(Items.LEATHER)))));

        ThaumcraftApi.bookRecipe("AMScythe", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.SCYTHE_ITEM), 2, 3, List.of(
                        List.of(new ItemStack(Items.OBSIDIAN)), List.of(new ItemStack(Items.SHEARS)),
                        List.<ItemStack>of(), List.of(new ItemStack(Items.STICK)),
                        List.<ItemStack>of(), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)))));

        ThaumcraftApi.bookRecipe("AMScytheBone", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.SCYTHE_BONE_ITEM), 2, 3, List.of(
                        List.of(new ItemStack(Items.OBSIDIAN)), List.of(new ItemStack(MortuorumItems.SCYTHE_ITEM)),
                        List.<ItemStack>of(), List.of(new ItemStack(Items.BONE)),
                        List.<ItemStack>of(), List.of(new ItemStack(Items.DIAMOND)))));

        ThaumcraftApi.bookRecipe("AMJarOfBlood", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.JAR_OF_BLOOD, 8), 3, 3, List.of(
                        List.of(new ItemStack(MortuorumItems.BUCKET_BLOOD)), List.of(new ItemStack(Items.GLASS_BOTTLE)), List.of(new ItemStack(Items.GLASS_BOTTLE)),
                        List.of(new ItemStack(Items.GLASS_BOTTLE)), List.of(new ItemStack(Items.GLASS_BOTTLE)), List.of(new ItemStack(Items.GLASS_BOTTLE)),
                        List.of(new ItemStack(Items.GLASS_BOTTLE)), List.of(new ItemStack(Items.GLASS_BOTTLE)), List.of(new ItemStack(Items.GLASS_BOTTLE)))));

        ThaumcraftApi.bookRecipe("AMBucketBlood", ThaumcraftApi.crafting(
                () -> new ItemStack(MortuorumItems.BUCKET_BLOOD), 3, 3, List.of(
                        List.of(new ItemStack(Items.BUCKET)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)),
                        List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)),
                        List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)), List.of(new ItemStack(MortuorumItems.JAR_OF_BLOOD)))));
    }
}
