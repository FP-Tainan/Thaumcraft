package net.thaumcraft.occulta.curse;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * As <b>maldições</b> do ofício: as quatro etiquetas que o {@code Infusion.EventHooks} do Witchery põe em quem
 * foi amaldiçoado, e o que elas fazem a cada batida.
 *
 * <p>Uma maldição não é um efeito de poção. Ela é um <b>número guardado em quem a tem</b>, que não acaba
 * sozinho, não sai com leite, e não aparece no canto da tela. Só outro rito a tira — e tirar é uma
 * <b>aposta</b>, que pode deixá-la pior.
 *
 * <p>São quatro, e cada uma morde de um jeito:
 *
 * <table border="1">
 *   <caption>As quatro</caption>
 *   <tr><th>maldição</th><th>o que faz</th></tr>
 *   <tr><td><b>Maldição</b></td><td>azar: pancada, lentidão, fraqueza, cegueira — e <b>largar o que se tem
 *       na mão</b></td></tr>
 *   <tr><td><b>Fervura</b></td><td>pega fogo sozinho, mas <b>só em terra quente</b> e com o céu aberto</td></tr>
 *   <tr><td><b>Pesadelo Acordado</b></td><td>o <b>Pesadelo</b> aparece, acordado, à procura de quem o
 *       tem</td></tr>
 *   <tr><td><b>Loucura</b></td><td>vê bichos que <b>não existem</b>, e ouve coisas que não estão lá</td></tr>
 * </table>
 *
 * <p>O grau importa em tudo: quanto mais fundo, mais vezes, mais forte e mais variado. E <b>só o Pesadelo
 * Acordado e a Loucura pegam em gente</b> — as outras duas pegam em qualquer vivo.
 */
public enum Curse implements StringRepresentable {
    /** O {@code witcheryCursed}: o azar que se cola. */
    CURSED("cursed"),
    /** O {@code witcheryOverheating}: a fervura. */
    OVERHEATING("overheating"),
    /** O {@code witcheryWakingNightmare}: o pesadelo que não espera o sono. */
    WAKING_NIGHTMARE("waking_nightmare"),
    /** O {@code witcheryInsanity}: a loucura. */
    INSANITY("insanity"),
    /** E o {@code witcherySinking}: o afundar. */
    SINKING("sinking");

    private final String nome;

    Curse(String nome) {
        this.nome = nome;
    }

    @Override
    public String getSerializedName() {
        return this.nome;
    }

    public static final Codec<Curse> CODEC = StringRepresentable.fromEnum(Curse::values);

    /** O que cada um carrega: a maldição e o grau dela. */
    public static final AttachmentType<Map<Curse, Integer>> DATA =
            AttachmentRegistry.<Map<Curse, Integer>>builder()
                    .initializer(() -> new EnumMap<>(Curse.class))
                    .persistent(Codec.unboundedMap(CODEC, Codec.INT)
                            .xmap(m -> {
                                Map<Curse, Integer> mapa = new EnumMap<>(Curse.class);
                                mapa.putAll(m);
                                return mapa;
                            }, m -> m))
                    .copyOnDeath()
                    .buildAndRegister(Thaumcraft.id("curses"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** Em que grau esta pessoa tem esta maldição, ou zero. */
    public static int level(@Nullable LivingEntity quem, Curse qual) {
        if (quem == null) return 0;
        return quem.getAttachedOrCreate(DATA).getOrDefault(qual, 0);
    }

    /** Põe a maldição no grau que se pedir — nunca abaixo do que já lá está, que é o original. */
    public static void put(LivingEntity quem, Curse qual, int grau) {
        var mapa = new EnumMap<>(quem.getAttachedOrCreate(DATA));
        mapa.put(qual, Math.max(grau, mapa.getOrDefault(qual, 0)));
        quem.setAttached(DATA, mapa);
    }

    /** E tira-a. */
    public static void remove(LivingEntity quem, Curse qual) {
        var mapa = new EnumMap<>(quem.getAttachedOrCreate(DATA));
        mapa.remove(qual);
        quem.setAttached(DATA, mapa);
    }

    /** Se tem alguma. */
    public static boolean any(@Nullable LivingEntity quem) {
        return quem != null && !quem.getAttachedOrCreate(DATA).isEmpty();
    }

    // ------------------------------------------------------------------ o que elas fazem

    /** Quantas batidas de cada vez o azar tenta morder, por grau. */
    public static final int AZAR = 20;

    /** E os cinco efeitos que ele escolhe — o sexto é largar o que se tem na mão. */
    public static void tick(ServerLevel level, LivingEntity quem) {
        var mapa = quem.getAttachedOrCreate(DATA);
        if (mapa.isEmpty()) return;

        afunda(quem, mapa.getOrDefault(SINKING, 0));
        azar(level, quem, mapa.getOrDefault(CURSED, 0));
        fervura(level, quem, mapa.getOrDefault(OVERHEATING, 0));
        if (quem instanceof Player gente) {
            pesadelo(level, gente, mapa.getOrDefault(WAKING_NIGHTMARE, 0));
            loucura(level, gente, mapa.getOrDefault(INSANITY, 0));
        }
    }

    /**
     * O Afundar: dentro da água, descer é mais rápido e subir é mais devagar.
     *
     * <p>Um décimo por grau, até quatro décimos. No grau quatro ou mais, quem cai na água desce quarenta por
     * cento mais depressa e sobe quarenta por cento mais devagar — e com armadura isso é afogar.
     *
     * <p><b>E ela não pega em gente.</b> Este é um engano do original, e fica: o {@code handleCurseEffects}
     * guarda o trecho inteiro atrás de um {@code !(entity instanceof EntityPlayer)}, e <b>dentro</b> dele há
     * um ramo escrito para jogador que nunca pode correr. O autor quis que pegasse em gente e escreveu o
     * contrário. Os ritos que a põem e a tiram existem na mesma — e também no original.
     */
    private static void afunda(LivingEntity quem, int grau) {
        if (grau <= 0 || quem instanceof Player) return;
        if (!quem.isInWater()) return;

        var anda = quem.getDeltaMovement();
        if (anda.y < 0.0) {
            quem.setDeltaMovement(anda.x, anda.y * (1.0 + Math.min(0.1 * grau, 0.4)), anda.z);
        } else if (anda.y > 0.0) {
            quem.setDeltaMovement(anda.x, anda.y * (1.0 - Math.min(0.1 * grau, 0.4)), anda.z);
        }
    }

    /**
     * O azar: um em vinte, e só quando não há já um dos cinco efeitos em cima.
     *
     * <p><b>Quantas opções ele tem cresce com o grau</b> — duas no grau um, e seis a partir do cinco. A última
     * é a que dói: <b>largar o que se tem na mão</b>, no chão, para quem quiser pegar.
     */
    private static void azar(ServerLevel level, LivingEntity quem, int grau) {
        if (grau <= 0) return;
        if (quem.hasEffect(MobEffects.BLINDNESS) || quem.hasEffect(MobEffects.WEAKNESS)
                || quem.hasEffect(MobEffects.MINING_FATIGUE) || quem.hasEffect(MobEffects.SLOWNESS)
                || quem.hasEffect(MobEffects.POISON)) {
            return;
        }
        if (level.getRandom().nextInt(AZAR) != 0) return;

        int quantas = grau >= 5 ? 6 : (grau >= 4 ? 5 : (grau >= 3 ? 4 : (grau >= 2 ? 3 : 2)));
        switch (level.getRandom().nextInt(quantas)) {
            case 0 -> quem.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 600,
                    Math.min(grau - 1, 4)));
            case 1 -> quem.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600,
                    Math.min(grau - 1, 4)));
            case 2 -> quem.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (13 + 2 * grau) * 20,
                    Math.min(grau - 2, 4)));
            case 3 -> {
                quem.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * grau * 20));
                if (grau > 5) quem.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 5 * grau * 20));
            }
            // o quatro é não fazer nada: é do original, e é o que dá à maldição o seu respiro
            case 5 -> larga(level, quem);
            default -> {
            }
        }
    }

    /** Largar o que se tem na mão, no chão. */
    private static void larga(ServerLevel level, LivingEntity quem) {
        ItemStack naMão = quem.getMainHandItem();
        if (naMão.isEmpty()) return;
        if (quem instanceof Player gente) {
            gente.drop(naMão.copy(), true);
            gente.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            quem.spawnAtLocation(level, naMão.copy());
            quem.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    /**
     * A fervura: pega fogo sozinho — mas <b>só onde é quente</b>.
     *
     * <p>O original pede três coisas ao mesmo tempo: o bioma com temperatura <b>1,5 ou mais</b> (que é
     * deserto, savana, terras áridas e Nether), <b>não estar chovendo</b> ali, e não estar na água. É o que
     * faz desta maldição uma coisa que se resolve <b>andando para o norte</b> — e isso é de propósito.
     */
    private static void fervura(ServerLevel level, LivingEntity quem, int grau) {
        if (grau <= 0 || quem.isOnFire()) return;
        int cada = grau > 2 ? 20 : (grau > 1 ? 25 : 30);
        if (level.getRandom().nextInt(cada) != 0) return;

        BlockPos onde = quem.blockPosition();
        var bioma = level.getBiome(onde);
        if (bioma.value().getBaseTemperature() < 1.5f) return;
        if (level.isRainingAt(onde)) return;
        if (quem.isInWater()) return;

        quem.igniteForSeconds(Math.min(level.getRandom().nextInt(grau < 4 ? 2 : grau - 1) + 1, 4));
    }

    /**
     * O Pesadelo Acordado: ele vem, e não espera o sono.
     *
     * <p>Um Pesadelo de cada vez por pessoa — o original olha dezesseis blocos em volta antes de chamar outro.
     * E <b>não acontece no Mundo dos Sonhos</b>: lá o pesadelo já é a casa.
     */
    private static void pesadelo(ServerLevel level, Player quem, int grau) {
        if (grau <= 0) return;
        if (level.dimension() == net.thaumcraft.occulta.spirit.SpiritWorld.LEVEL) return;
        int cada = grau > 4 ? 30 : (grau > 2 ? 60 : 180);
        if (level.getRandom().nextInt(cada) != 0) return;

        var perto = level.getEntitiesOfClass(net.thaumcraft.occulta.spirit.NightmareEntity.class,
                new AABB(quem.blockPosition()).inflate(16.0, 8.0, 16.0));
        if (!perto.isEmpty()) return;

        nasce(level, net.thaumcraft.occulta.OccultaEntities.NIGHTMARE, quem, 2, 6);
    }

    /**
     * A Loucura: bichos que não existem.
     *
     * <p>Um em cada trinta e cinco batidas no grau um, vinte e cinco a partir do três. E <b>a partir do grau
     * quatro</b> há o outro lado dela: um em cada vinte, um <b>barulho</b> — um estouro, ou um enderman — que
     * só quem a tem ouve. Não há nada lá.
     */
    private static void loucura(ServerLevel level, Player quem, int grau) {
        if (grau <= 0) return;
        int cada = grau > 2 ? 25 : (grau > 1 ? 30 : 35);
        if (level.getRandom().nextInt(cada) == 0) {
            var qual = switch (level.getRandom().nextInt(3)) {
                case 1 -> net.thaumcraft.occulta.OccultaEntities.ILLUSION_SPIDER;
                case 2 -> net.thaumcraft.occulta.OccultaEntities.ILLUSION_ZOMBIE;
                default -> net.thaumcraft.occulta.OccultaEntities.ILLUSION_CREEPER;
            };
            var visão = nasce(level, qual, quem, 4, 9);
            if (visão instanceof IllusionEntity ilusão) ilusão.vitima(quem);
            return;
        }
        if (grau < 4 || level.getRandom().nextInt(20) != 0) return;

        // uns sons do jogo são SoundEvent e outros já vêm em Holder: normaliza-se aqui
        net.minecraft.core.Holder<SoundEvent> barulho = level.getRandom().nextInt(3) == 1
                ? net.minecraft.core.Holder.direct(SoundEvents.ENDERMAN_AMBIENT)
                : SoundEvents.GENERIC_EXPLODE;
        if (quem instanceof net.minecraft.server.level.ServerPlayer gente) {
            gente.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    barulho, SoundSource.HOSTILE,
                    gente.getX(), gente.getY(), gente.getZ(), 1.0f, 1.0f, level.getRandom().nextLong()));
        }
    }

    /**
     * Põe um bicho perto de alguém, entre duas distâncias: o {@code Infusion.spawnCreature} do original.
     *
     * <p>Ele tenta dez vezes achar um lugar com chão e dois blocos de ar, no anel entre o mínimo e o máximo.
     * Não achando, não nasce nada — e é melhor assim do que um creeper dentro da parede.
     */
    @Nullable
    private static Entity nasce(ServerLevel level, net.minecraft.world.entity.EntityType<?> qual,
                                Player quem, int perto, int longe) {
        var sorte = level.getRandom();
        for (int tentativa = 0; tentativa < 10; tentativa++) {
            int dx = Mth.nextInt(sorte, perto, longe) * (sorte.nextBoolean() ? 1 : -1);
            int dz = Mth.nextInt(sorte, perto, longe) * (sorte.nextBoolean() ? 1 : -1);
            BlockPos onde = quem.blockPosition().offset(dx, 0, dz);

            for (int dy = 2; dy >= -2; dy--) {
                BlockPos casa = onde.offset(0, dy, 0);
                if (!level.getBlockState(casa.below()).isSolid()) continue;
                if (!level.isEmptyBlock(casa) || !level.isEmptyBlock(casa.above())) continue;

                var bicho = qual.create(level, EntitySpawnReason.TRIGGERED);
                if (bicho == null) return null;
                bicho.snapTo(casa.getX() + 0.5, casa.getY(), casa.getZ() + 0.5,
                        sorte.nextFloat() * 360.0f, 0.0f);
                if (bicho instanceof net.minecraft.world.entity.Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(casa),
                            EntitySpawnReason.TRIGGERED, null);
                }
                level.addFreshEntity(bicho);
                return bicho;
            }
        }
        return null;
    }
}
