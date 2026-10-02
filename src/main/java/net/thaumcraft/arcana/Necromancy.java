package net.thaumcraft.arcana;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * O <b>necromante</b>: o exército, a panóplia e a montaria.
 *
 * <p><b>Isto é acréscimo, e não porte.</b> Nada disto está no Ars Magica 2 — lá a Invocação traz uma criatura,
 * nua, e para trazer outra é preciso o Filactério de Cristal, que precisa do Invocador, que precisa da rede de
 * energia que este porte não trouxe. O que existe aqui é a resposta a uma pergunta que a Invocação deixa no ar:
 * <b>e se o mago se dedicasse a isto?</b>
 *
 * <p>São três coisas, e cada uma responde a uma parte da pergunta:
 *
 * <ol>
 *   <li><b>Quantos.</b> O {@link Modifiers#LEGION} sobe o teto em um por cópia. É a única peça nova que custa
 *       mana, e custa caro: cada cópia <b>dobra</b> a conta da etapa.</li>
 *   <li><b>O quê.</b> O {@link Essences#RAISE_DEAD} traz <b>zumbi com espada</b> em vez de esqueleto com arco.
 *       Nem um é melhor que o outro — um atira de longe, o outro bate de perto, e os dois custam o mesmo.</li>
 *   <li><b>Com o quê.</b> E esta é a parte que não se compra: <b>a panóplia vem da Afinidade</b>.</li>
 * </ol>
 *
 * <h2>Por que a panóplia vem da Afinidade, e não de uma peça</h2>
 *
 * <p>Porque é o que o Ars Magica 2 faria. A Afinidade dele não se compra: ela <b>pega</b>, sozinha, de tanto
 * lançar a mesma coisa — e já é assim que o mod dá a quem nada em água a respiração, e a quem anda no fim a
 * resistência. Um necromante não vira necromante comprando uma perícia; ele vira de tanto chamar mortos.
 *
 * <p>Então o que veste a invocação é a <b>profundidade no Fim</b> de quem a chama, que é a Afinidade da própria
 * Invocação:
 *
 * <table border="1">
 *   <caption>A panóplia</caption>
 *   <tr><th>Fim</th><th>o que vem vestido</th></tr>
 *   <tr><td>abaixo de 0,25</td><td>nada: a invocação vem nua, como no original</td></tr>
 *   <tr><td>0,25</td><td>couro, e a arma de pedra</td></tr>
 *   <tr><td>0,50</td><td>ferro</td></tr>
 *   <tr><td>0,75</td><td>diamante</td></tr>
 *   <tr><td>0,90</td><td>e a <b>montaria</b>: um cavalo esquelético, já selado</td></tr>
 * </table>
 *
 * <p>O arco do esqueleto e a espada do zumbi <b>não</b> são panóplia: eles vêm sempre, porque é a arma que diz
 * o que cada um é. O que a panóplia faz com a arma é subir o <b>grau</b> dela.
 */
public final class Necromancy {
    /** Os quatro degraus do Fim, que é a Afinidade da própria Invocação. */
    public static final float COURO = 0.25f;
    public static final float FERRO = 0.50f;
    public static final float DIAMANTE = 0.75f;
    public static final float MONTARIA = 0.90f;

    /** O que se pode chamar. */
    public enum Qual {
        /** O do original: esqueleto, com arco. */
        ESQUELETO(EntityTypes.SKELETON, Items.BOW),
        /** E o do acréscimo: zumbi, com espada. */
        ZUMBI(EntityTypes.ZOMBIE, Items.WOODEN_SWORD);

        private final EntityType<? extends Mob> tipo;
        private final Item arma;

        Qual(EntityType<? extends Mob> tipo, Item arma) {
            this.tipo = tipo;
            this.arma = arma;
        }

        public EntityType<? extends Mob> tipo() {
            return this.tipo;
        }

        /** A arma nua, antes de a panóplia subir o grau dela. */
        public Item arma() {
            return this.arma;
        }
    }

    private Necromancy() {
    }

    /**
     * O grau da panóplia de quem chama: 0 (nada), 1 (couro), 2 (ferro) ou 3 (diamante).
     *
     * <p>Quem não é gente não tem Afinidade, e por isso não tem panóplia — o que deixa as invocações de um
     * monstro nuas, como no original.
     */
    public static int panóplia(LivingEntity quem) {
        if (!(quem instanceof Player gente)) return 0;
        float fim = AffinityData.of(gente).depth(Affinity.ENDER);
        if (fim >= DIAMANTE) return 3;
        if (fim >= FERRO) return 2;
        if (fim >= COURO) return 1;
        return 0;
    }

    /** E se ela vem montada. */
    public static boolean temMontaria(LivingEntity quem) {
        if (!(quem instanceof Player gente)) return false;
        return AffinityData.of(gente).depth(Affinity.ENDER) >= MONTARIA;
    }

    /** Veste a invocação com o que o Fim de quem a chama lhe dá. */
    public static void veste(Mob bicho, Qual qual, int grau) {
        bicho.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(arma(qual, grau)));
        if (grau <= 0) return;

        bicho.setItemSlot(EquipmentSlot.HEAD, new ItemStack(peça(grau, 0)));
        bicho.setItemSlot(EquipmentSlot.CHEST, new ItemStack(peça(grau, 1)));
        bicho.setItemSlot(EquipmentSlot.LEGS, new ItemStack(peça(grau, 2)));
        bicho.setItemSlot(EquipmentSlot.FEET, new ItemStack(peça(grau, 3)));

        // e o que vem vestido não larga a roupa ao cair: ela some com ela
        for (EquipmentSlot onde : EquipmentSlot.values()) {
            if (onde.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || onde == EquipmentSlot.MAINHAND) {
                bicho.setDropChance(onde, 0.0f);
            }
        }
    }

    /**
     * A arma, no grau que a panóplia der.
     *
     * <p>O <b>arco não sobe</b>: não há arco de ferro. O que o esqueleto ganha com a panóplia é a armadura, e
     * é o que faz dele um atirador que aguenta, em vez de um atirador melhor.
     */
    private static Item arma(Qual qual, int grau) {
        if (qual == Qual.ESQUELETO) return Items.BOW;
        return switch (grau) {
            case 0 -> Items.WOODEN_SWORD;
            case 1 -> Items.STONE_SWORD;
            case 2 -> Items.IRON_SWORD;
            default -> Items.DIAMOND_SWORD;
        };
    }

    /** 0 elmo, 1 peito, 2 calças, 3 botas. */
    private static Item peça(int grau, int onde) {
        return switch (grau) {
            case 1 -> switch (onde) {
                case 0 -> Items.LEATHER_HELMET;
                case 1 -> Items.LEATHER_CHESTPLATE;
                case 2 -> Items.LEATHER_LEGGINGS;
                default -> Items.LEATHER_BOOTS;
            };
            case 2 -> switch (onde) {
                case 0 -> Items.IRON_HELMET;
                case 1 -> Items.IRON_CHESTPLATE;
                case 2 -> Items.IRON_LEGGINGS;
                default -> Items.IRON_BOOTS;
            };
            default -> switch (onde) {
                case 0 -> Items.DIAMOND_HELMET;
                case 1 -> Items.DIAMOND_CHESTPLATE;
                case 2 -> Items.DIAMOND_LEGGINGS;
                default -> Items.DIAMOND_BOOTS;
            };
        };
    }

    /**
     * Põe a invocação a cavalo.
     *
     * <p>O cavalo é <b>esquelético e selado</b>, e é invocação também: ele tem o mesmo prazo e a mesma trela do
     * que o monta. Mas <b>não ocupa vaga</b> — uma montaria não é um soldado, e fazê-la contar seria o mesmo que
     * dizer que um necromante a cavalo tem metade do exército.
     */
    public static void monta(ServerLevel level, Mob quemMonta, LivingEntity quem, int prazo) {
        SkeletonHorse cavalo = EntityTypes.SKELETON_HORSE.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (cavalo == null) return;

        Vec3 onde = quemMonta.position();
        cavalo.snapTo(onde.x, onde.y, onde.z, quemMonta.getYRot(), 0.0f);
        cavalo.finalizeSpawn(level, level.getCurrentDifficultyAt(cavalo.blockPosition()),
                EntitySpawnReason.MOB_SUMMONED, null);
        cavalo.setTamed(true);
        if (quem instanceof Player gente) cavalo.setOwner(gente);

        Summons.marcaMontaria(level, cavalo, quem, prazo);
        level.addFreshEntity(cavalo);
        quemMonta.startRiding(cavalo);
    }
}
