package net.thaumcraft.occulta.goblin;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>roupa de goblin</b>: o {@code ItemGoblinClothes} do Witchery, que são três peças e uma ideia.
 *
 * <h2>As três peças</h2>
 *
 * <ul>
 *   <li>a <b>Fita Torcida</b>, na cabeça: quem <b>estiver olhando</b> para quem a tem, a dezesseis blocos,
 *       fica <b>enjoado</b> — e, se for gente, é <b>virado ao contrário</b>. Bichos ficam <b>fracos</b>;</li>
 *   <li>a <b>Aljava do Mog</b>, no peito: o arco dispara <b>sem flecha</b>, e a flecha que sai faz <b>três
 *       vezes</b> o dano em quem estiver <b>no ar</b> e deixa <b>Fraqueza</b> por dez segundos;</li>
 *   <li>a <b>Cinta do Gulg</b>, nas pernas: o <b>murro de mão vazia</b> faz <b>cinco</b> de dano e atira o
 *       alvo <b>um bloco para cima</b>.</li>
 * </ul>
 *
 * <h2>E a ideia</h2>
 *
 * <p>Dois jogadores a <b>oito blocos</b> um do outro, um com a Aljava e outro com a Cinta, ganham os dois
 * <b>Resistência II</b>. É a mesma conta de distância que faz os deuses goblins invencíveis — a que o
 * {@link GoblinGodEntity} tem — <b>virada para quem joga</b>: o Mog e o Gulg eram fortes juntos, e as roupas
 * deles continuam sendo.
 *
 * <p>Repare no que isso quer dizer de desenho: é o único par de peças do mod inteiro que <b>só vale a dois</b>.
 * Uma pessoa com as duas não ganha nada com isso.
 */
public final class GoblinClothes {
    /** A que distância os dois donos se sentem, e de quanto em quanto tempo se procuram. */
    public static final double PERTO = 8.0;
    public static final int PROCURA = 100;

    /** Quanto dura a Resistência que o par dá, e de que nível. */
    public static final int RESISTE = 200;
    public static final int RESISTE_NÍVEL = 1;

    /** O alcance da Fita Torcida, e de quantas em quantas batidas ela olha. */
    public static final double OLHAR = 16.0;
    public static final int DE_CINCO = 5;

    /** Quanto dura o que ela faz. */
    public static final int TONTO = 100;

    /** A abertura do cone de quem «está olhando», que é a do original. */
    public static final double CONE = 0.025;

    /** Quanto a flecha da Aljava multiplica em quem está no ar, e quanto de Fraqueza ela deixa. */
    public static final float NO_AR = 3.0f;
    public static final int FRACO = 200;

    /** E o murro de mão vazia da Cinta: cinco de dano e um bloco de voo. */
    public static final float MURRO = 5.0f;
    public static final double VOA = 1.0;

    /** A marca que a Aljava põe na flecha: o {@code WITCMogged} do original. */
    public static final AttachmentType<Boolean> MOGADA =
            AttachmentRegistry.<Boolean>builder()
                    .initializer(() -> Boolean.FALSE)
                    .persistent(com.mojang.serialization.Codec.BOOL)
                    .buildAndRegister(Thaumcraft.id("mogged_arrow"));

    private GoblinClothes() {
    }

    // ------------------------------------------------------------------ quem veste o quê

    public static boolean temFita(@Nullable LivingEntity quem) {
        return quem != null && quem.getItemBySlot(EquipmentSlot.HEAD).is(OccultaItems.KOBOLDITE_HELM);
    }

    public static boolean temAljava(@Nullable LivingEntity quem) {
        return quem != null && quem.getItemBySlot(EquipmentSlot.CHEST).is(OccultaItems.MOGS_QUIVER);
    }

    public static boolean temCinta(@Nullable LivingEntity quem) {
        return quem != null && quem.getItemBySlot(EquipmentSlot.LEGS).is(OccultaItems.GULGS_GURDLE);
    }

    // ------------------------------------------------------------------ o par

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(servidor -> {
            long hora = servidor.overworld().getGameTime();
            for (ServerPlayer quem : servidor.getPlayerList().getPlayers()) {
                if (hora % PROCURA == 0L) oPar(quem);
                if (hora % DE_CINCO == 1L && temFita(quem)) aFita(quem);
            }
        });

        /*
         * E a flecha que sai de quem tem a Aljava nasce <b>marcada</b> e <b>sem volta</b>: ela não se apanha
         * do chão. O original diz o mesmo pondo o tipo de apanha em dois, que é o que o jogo faz com uma
         * flecha de arco encantado com Infinidade.
         */
        ServerEntityEvents.ENTITY_LOAD.register((bicho, level) -> {
            if (!(bicho instanceof AbstractArrow seta)) return;
            if (!(seta.getOwner() instanceof Player dono) || !temAljava(dono)) return;
            seta.setAttached(MOGADA, Boolean.TRUE);
            seta.pickup = AbstractArrow.Pickup.DISALLOWED;
        });
    }

    /**
     * <b>Os dois donos juntos ficam mais duros.</b>
     *
     * <p>A oito blocos um do outro, um com a Aljava e outro com a Cinta, os dois ganham Resistência II por
     * dez segundos. É a conta dos deuses virada para quem joga.
     */
    public static void oPar(ServerPlayer quem) {
        if (!temAljava(quem) && !temCinta(quem)) return;
        AABB caixa = quem.getBoundingBox().inflate(PERTO);
        for (Player outro : quem.level().getEntitiesOfClass(Player.class, caixa)) {
            if (outro == quem) continue;
            boolean casa = (temAljava(quem) && temCinta(outro))
                    || (temAljava(outro) && temCinta(quem));
            if (!casa) continue;
            quem.removeEffect(MobEffects.RESISTANCE);
            quem.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, RESISTE, RESISTE_NÍVEL));
            return;
        }
    }

    /**
     * <b>A Fita Torcida desnorteia quem olha.</b>
     *
     * <p>Quem estiver <b>olhando</b> para quem a tem, a dezesseis blocos e com linha de vista, fica
     * <b>enjoado</b> cinco segundos — e, se for gente, é <b>virado ao contrário</b> na hora. Um bicho fica
     * <b>fraco</b> em vez disso: não tem tela para lhe virar.
     *
     * <p>E ela não pega em quem traz uma <b>abóbora na cabeça</b>, que é o original dizendo que quem não vê
     * não se desnorteia.
     */
    public static void aFita(ServerPlayer quem) {
        AABB caixa = quem.getBoundingBox().inflate(OLHAR);
        for (LivingEntity outro : quem.level().getEntitiesOfClass(LivingEntity.class, caixa)) {
            if (outro == quem || !olhaPara(quem, outro)) continue;
            if (outro instanceof ServerPlayer gente) {
                vira(gente, quem);
                if (!gente.hasEffect(MobEffects.NAUSEA)) {
                    gente.addEffect(new MobEffectInstance(MobEffects.NAUSEA, TONTO, 0));
                }
            } else if (!outro.hasEffect(MobEffects.WEAKNESS)) {
                outro.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TONTO, 0));
            }
        }
    }

    /**
     * Se aquele está olhando para este: o {@code shouldAffectTarget} do original.
     *
     * <p>É o cone do enderman, com a mesma folga — e a abertura <b>aperta com a distância</b>, de modo que
     * de longe é preciso olhar bem certo e de perto basta ter a pessoa à frente.
     */
    public static boolean olhaPara(LivingEntity este, LivingEntity aquele) {
        if (aquele.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN)) return false;

        Vec3 rumo = aquele.getViewVector(1.0f).normalize();
        Vec3 para = new Vec3(este.getX() - aquele.getX(),
                este.getBoundingBox().minY + este.getBbHeight() / 2.0
                        - (aquele.getY() + aquele.getEyeHeight()),
                este.getZ() - aquele.getZ());
        double longe = para.length();
        if (longe <= 0.0) return false;
        double quanto = rumo.dot(para.normalize());
        return quanto > 1.0 - CONE / longe && aquele.hasLineOfSight(este);
    }

    /** E o virar: a pessoa fica de costas para quem a desnorteou. */
    private static void vira(ServerPlayer quem, LivingEntity paraQuem) {
        double rumo = Math.atan2(quem.getZ() - paraQuem.getZ(), quem.getX() - paraQuem.getX());
        float giro = (float) ((Math.toDegrees(rumo) + 180.0 + 90.0) % 360.0);
        quem.connection.teleport(quem.getX(), quem.getY(), quem.getZ(), giro, quem.getXRot());
    }

    // ------------------------------------------------------------------ o que o mixin pergunta

    /** Se aquele golpe veio de uma flecha da Aljava. */
    public static boolean daAljava(DamageSource fonte) {
        return fonte.getDirectEntity() instanceof AbstractArrow seta
                && Boolean.TRUE.equals(seta.getAttached(MOGADA));
    }

    /**
     * <b>O que uma flecha da Aljava faz:</b> três vezes o dano em quem está no ar, e Fraqueza em qualquer
     * caso.
     *
     * @return quanto o golpe passa a valer
     */
    public static float aFlecha(ServerLevel level, LivingEntity noquê, float quanto) {
        noquê.removeEffect(MobEffects.WEAKNESS);
        noquê.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, FRACO, 0));
        return noquê.onGround() ? quanto : quanto * NO_AR;
    }

    /** Se aquele golpe é um murro de mão vazia de quem tem a Cinta. */
    public static boolean daCinta(DamageSource fonte) {
        return fonte.getEntity() instanceof Player quem && temCinta(quem)
                && quem.getMainHandItem().isEmpty()
                && fonte.getDirectEntity() == quem;
    }

    /** E o que ele faz: cinco de dano fixo e um bloco de voo. */
    public static float aCinta(LivingEntity noquê) {
        noquê.setDeltaMovement(noquê.getDeltaMovement().x, VOA, noquê.getDeltaMovement().z);
        noquê.hurtMarked = true;
        return MURRO;
    }

}
