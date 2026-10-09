package net.thaumcraft.occulta.imp;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** O que o Diabrete gosta, o que ele dá e como ele se chama. */
public final class Imps {
    /**
     * As <b>coisas brilhantes</b>, e quanto cada uma lhe agrada: o {@code shinies} do original.
     *
     * <p>Repare na conta, porque ela diz o que ele é: um <b>bloco de diamante</b> vale setenta e dois, e
     * nove diamantes soltos valem setenta e dois também. Ele não conta valor — conta <b>brilho</b>, e um
     * bloco brilha tanto quanto o que o faz. Já uma <b>esmeralda</b> vale três e um <b>bloco de
     * esmeralda</b> vinte e sete, que é nove vezes: ele é coerente.
     *
     * <p>E a ferramenta vale mais do que o metal: uma picareta de diamante vale <b>vinte e quatro</b>, que
     * são três diamantes a oito — ele paga o feitio.
     */
    private static final Map<Item, Integer> BRILHO = Map.ofEntries(
            Map.entry(Items.DIAMOND, 8),
            Map.entry(Items.DIAMOND_AXE, 24),
            Map.entry(Items.DIAMOND_PICKAXE, 24),
            Map.entry(Items.DIAMOND_HOE, 16),
            Map.entry(Items.DIAMOND_SWORD, 16),
            Map.entry(Items.DIAMOND_SHOVEL, 8),
            Map.entry(Items.EMERALD, 3),
            Map.entry(Items.GOLD_INGOT, 1),
            Map.entry(Items.NETHER_STAR, 16),
            Map.entry(Items.BLAZE_ROD, 1),
            Map.entry(Items.GHAST_TEAR, 4),
            Map.entry(Items.GOLDEN_AXE, 3),
            Map.entry(Items.GOLDEN_PICKAXE, 3),
            Map.entry(Items.GOLDEN_SWORD, 2),
            Map.entry(Items.GOLDEN_HOE, 2),
            Map.entry(Items.GOLDEN_SHOVEL, 1),
            Map.entry(Blocks.GOLD_BLOCK.asItem(), 9),
            Map.entry(Blocks.EMERALD_BLOCK.asItem(), 27),
            Map.entry(Blocks.DIAMOND_BLOCK.asItem(), 72),
            Map.entry(Blocks.LAPIS_BLOCK.asItem(), 7),
            Map.entry(Blocks.REDSTONE_BLOCK.asItem(), 5));

    /**
     * Os quatro <b>segredos</b>, pela ordem em que ele os conta.
     *
     * <p>Três deles são a única maneira que há de aprender o Carnosa Diem, o Morsmordre e o Ignianima.
     */
    public static final Item[] SEGREDOS = {
            OccultaItems.BREW_SOUL_HUNGER,
            OccultaItems.BREW_SOUL_FEAR,
            OccultaItems.BREW_SOUL_ANGUISH,
            OccultaItems.CONTRACT_TORMENT,
    };

    /** E o que ele dá depois dos quatro: um ao acaso de sete. */
    public static final Item[] SOBRAS = {
            OccultaItems.BAT_WOOL,
            OccultaItems.DOG_TONGUE,
            OccultaItems.TOE_OF_FROG,
            OccultaItems.OWLETS_WING,
            OccultaItems.ENT_BRANCH,
            OccultaItems.INFERNAL_BLOOD,
            OccultaItems.CREEPER_HEART,
    };

    /** Quantos de cada, que não é um de cada: as contas do original. */
    private static final Map<Item, Integer> QUANTOS = Map.of(
            OccultaItems.BAT_WOOL, 5,
            OccultaItems.DOG_TONGUE, 5,
            OccultaItems.TOE_OF_FROG, 2,
            OccultaItems.OWLETS_WING, 2,
            OccultaItems.ENT_BRANCH, 1,
            OccultaItems.INFERNAL_BLOOD, 2,
            OccultaItems.CREEPER_HEART, 2);

    private Imps() {
    }

    /** Quanto aquela coisa lhe agrada, ou nada se não lhe agrada. */
    public static @Nullable Integer brilho(Item oquê) {
        return BRILHO.get(oquê);
    }

    /** Quantos de um presente ele dá. */
    public static int quantos(Item oquê) {
        return QUANTOS.getOrDefault(oquê, 1);
    }

    // ------------------------------------------------------------------ o nome dele

    /**
     * Os cem nomes de demônio do original, com as repetições que ele tem.
     *
     * <p>Elas <b>não</b> são engano: o Krakus aparece três vezes e o Larhepeis também, de modo que esses
     * saem mais vezes. Uma lista de nomes com repetições é uma lista de nomes com peso, e é assim que ela
     * fica.
     */
    private static final List<String> NOMES = List.of(
            "Ppaironael", "Aethon", "Tyrnak", "Beelzebuth", "Botis", "Moloch", "Taet", "Epnanaet",
            "Unonom", "Hexpemsazon", "Thayax", "Ethahoat", "Pruslas", "Ahtuxies", "Laripael", "Elxar",
            "Tarihimal", "Sapanolr", "Sahaminapiel", "Honed", "Oghmus", "Zedeson", "Halmaneop", "Nopoz",
            "Ekarnahox", "Sacuhatakael", "Ticos", "Arametheus", "Azmodaeus", "Larhepeis", "Topriraiz",
            "Rarahaimzah", "Tedrahamael", "Osaselael", "Phlegon", "Nelokhiel", "Haristum", "Zul",
            "Larhepeis", "Aamon", "Tramater", "Ehhbes", "Kra`an", "Quarax", "Hotesiatrem", "Surgat",
            "Nu`uhn", "Litedabh", "Unonom", "Bolenoz", "Hilopael", "Haristum", "Uhn", "Hiepacth",
            "Pemcapso", "Ankou", "Pundohien", "Koit", "Montobulus", "Amsaset", "Aropet", "Isnal",
            "Solael", "Exroh", "Sidragrosam", "Pnecamob", "Malashim", "Beelzebuth", "Ehohit", "Izatap",
            "Olon", "Assoaz", "Agalierept", "Krakus", "Umlaboor", "Aknrar", "Damaz", "Rhysus",
            "Pundohien", "Ba`al", "Rasuniolpas", "Anhoor", "Nyarlathotep", "Krakus", "Larhepeis",
            "Itakup", "Erdok", "Umlaboor", "Ezon", "Krakus", "Glassyalabolas", "Kra`an", "Ehnnat",
            "Terxor", "Asramel", "Tadal", "Arpzih", "Azmodaeus", "Henbolaron", "Rhysus");

    /** Em quantas ele tem um nome só: uma em cinco. */
    public static final int UM_NOME_SÓ = 5;

    /**
     * Um <b>nome de demônio</b>.
     *
     * <p>Em quatro de cada cinco vezes são <b>dois</b> juntos — «Krakus Ehnnat» —, e na quinta é um só.
     * Com cem nomes e dois de cada vez, ele quase nunca se repete.
     */
    public static String nome(RandomSource sorte) {
        String um = NOMES.get(sorte.nextInt(NOMES.size()));
        if (sorte.nextInt(UM_NOME_SÓ) == 0) return um;
        return um + " " + NOMES.get(sorte.nextInt(NOMES.size()));
    }

    public static int quantosNomes() {
        return NOMES.size();
    }

    // ------------------------------------------------------------------ a casa

    /** Quem tem casa e um raio em volta dela. */
    public interface HasHome {
        BlockPos home();

        double homeRange();
    }

    /**
     * <b>Vaguear perto de casa</b>: o {@code EntityAIWanderWithRestriction} do original.
     *
     * <p>É a meta de vaguear de sempre com uma linha a mais: o ponto sorteado só serve se estiver
     * <b>dentro do raio de casa</b>. Não havendo, ele não anda — e é isso que prende um Diabrete comprado
     * ao lugar onde o negócio se fechou.
     */
    public static class WanderNearHome extends Goal {
        /** Uma em cento e vinte por batida, e com ele parado há menos de cem. */
        public static final int UMA_EM = 120;
        public static final int PARADO_HÁ = 100;

        /** E o tamanho do sorteio: dez de lado, sete de altura. */
        public static final int LADO = 10;
        public static final int ALTO = 7;

        private final PathfinderMob quem;
        private final double velocidade;
        private final HasHome casa;
        private double x;
        private double y;
        private double z;

        public WanderNearHome(PathfinderMob quem, double velocidade, HasHome casa) {
            this.quem = quem;
            this.velocidade = velocidade;
            this.casa = casa;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.quem.getNoActionTime() >= PARADO_HÁ) return false;
            if (this.quem.getRandom().nextInt(UMA_EM) != 0) return false;

            var onde = DefaultRandomPos.getPos(this.quem, LADO, ALTO);
            if (onde == null) return false;

            BlockPos lar = this.casa.home();
            double longe = onde.distanceToSqr(lar.getX(), lar.getY(), lar.getZ());
            if (longe > this.casa.homeRange() * this.casa.homeRange()) return false;

            this.x = onde.x;
            this.y = onde.y;
            this.z = onde.z;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.quem.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.quem.getNavigation().moveTo(this.x, this.y, this.z, this.velocidade);
        }
    }
}
