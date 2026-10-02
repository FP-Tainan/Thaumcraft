package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mixin.MobGoalAccessor;
import net.thaumcraft.registry.TCDamageTypes;

import java.util.UUID;

/**
 * O que foi invocado, de quem é, e por quanto tempo: o {@code EntityUtilities.makeSummon_PlayerFaction} e o
 * {@code setSummonDuration} do Ars Magica 2.
 *
 * <p>Uma criatura invocada <b>não é uma criatura qualquer com dono</b>. Ela sabe três coisas: quem a chamou,
 * quando acaba, e que ela é invocada — e essa última é o que a impede de ser invocada outra vez, que é como o
 * original evita que a própria Invocação se chame em cadeia.
 *
 * <p><b>Ela muda de lado.</b> O original limpa a lista de alvos dela e põe outra: bate em quem bater nela, e
 * procura monstro, geleia e ghast. Procura <b>monstro que não seja invocação</b> — é o
 * {@code SummonEntitySelector}, e é o que impede duas invocações do mesmo mago de se matarem. E anda atrás de
 * quem a chamou, que é o {@link SummonFollowOwnerGoal}.
 *
 * <p><b>Ela tem prazo, e tem trela.</b> Quatro mil e oitocentas batidas, que são quatro minutos, multiplicadas
 * pelo modificador de Duração. Acabado o prazo, ela <b>morre</b> — cinco mil de dano do {@code unsummon}, que é
 * mais do que qualquer bicho tem. E morre também se quem a chamou morreu, saiu do mundo, ou está a <b>mais de
 * trinta blocos</b>: é a trela, e é o que faz a invocação ser companhia e não um bicho largado no mapa.
 *
 * <p><b>Mas uma invocação com nome próprio não morre: ela se solta.</b> É o {@code revertAI} do original, com
 * uma diferença declarada no {@code PORTE.md}: lá as vontades antigas voltam de uma cópia guardada, e aqui ela
 * se solta com as vontades que tem.
 *
 * <p><b>E o teto é um.</b> No original ele sobe para dois com a perícia {@code ExtraSummon}; essa perícia não é
 * peça de feitiço nenhuma, e a árvore deste porte só sabe guardar peças. Fica o um, declarado no
 * {@code PORTE.md}.
 */
public record Summons(UUID dono, long acabaEm, boolean montaria) {
    /** O prazo base: as quatro mil e oitocentas batidas do original. */
    public static final int PRAZO = 4800;

    /** E quantas se pode ter de pé ao mesmo tempo. */
    public static final int TETO = 1;

    /** A trela, ao quadrado: os novecentos do original, que são trinta blocos. */
    public static final double LONGE_DEMAIS = 900.0;

    /** O dano que desfaz: os cinco mil do original. */
    public static final float DANO_DO_DESFAZER = 5000.0f;

    /**
     * De quanto em quanto se olha o prazo e a trela.
     *
     * <p>O original conta isso <b>na batida do próprio bicho</b>. Aqui o relógio é do mundo, e varrer todo
     * bicho de todo mundo a cada batida é caro para nada: o prazo tem 4800 batidas, a trela tem trinta blocos,
     * e um segundo de folga em qualquer dos dois não se vê. <b>Declarado no {@code PORTE.md}.</b>
     */
    private static final int DE_QUANTO_EM_QUANTO = 20;

    public static final Codec<Summons> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("dono").forGetter(Summons::dono),
            Codec.LONG.fieldOf("acabaEm").forGetter(Summons::acabaEm),
            // sem valor é falso: o que foi guardado antes do necromante eram todos soldados
            Codec.BOOL.optionalFieldOf("montaria", false).forGetter(Summons::montaria))
            .apply(i, Summons::new));

    /** Quem chama de fora sem dizer nada chama um soldado. */
    public Summons(UUID dono, long acabaEm) {
        this(dono, acabaEm, false);
    }

    public static final AttachmentType<Summons> DATA = AttachmentRegistry.<Summons>builder()
            .initializer(() -> null)
            .persistent(CODEC)
            .buildAndRegister(Thaumcraft.id("summon"));

    /** Registra o relógio do prazo e da trela — e, de passagem, faz a classe carregar com o apego dela. */
    public static void init() {
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            if (level.getGameTime() % DE_QUANTO_EM_QUANTO != 0) return;
            for (Entity bicho : level.getAllEntities()) tick(level, bicho);
        });
    }

    /** Se este bicho é invocado: o {@code isSummon} do original. */
    public static boolean éInvocado(Entity quem) {
        return quem != null && quem.hasAttached(DATA);
    }

    /** Quem chamou este bicho, se ainda está no mundo: o {@code getOwner} do original. */
    public static LivingEntity donoDe(ServerLevel level, Entity bicho) {
        Summons dado = bicho.getAttached(DATA);
        if (dado == null) return null;
        return level.getPlayerByUUID(dado.dono());
    }

    /**
     * Marca o bicho como invocado de alguém, com o prazo dele, e <b>o passa para o lado do dono</b>.
     *
     * <p>É aqui que o {@code makeSummon_PlayerFaction} e o {@code setOwner} do original se juntam: o apego, a
     * lista de alvos nova, e a vontade de seguir.
     */
    public static void marca(ServerLevel level, Entity bicho, LivingEntity quem, int prazo) {
        marca(level, bicho, quem, prazo, false);
    }

    /**
     * Uma <b>montaria</b>: tudo igual ao soldado, menos a vaga.
     *
     * <p>É do acréscimo do necromante, e está declarado no {@code PORTE.md}: ela tem o mesmo prazo e a mesma
     * trela, mas não conta no teto — uma montaria não é um soldado, e fazê-la contar seria dizer que um
     * necromante a cavalo tem metade do exército.
     */
    public static void marcaMontaria(ServerLevel level, Entity bicho, LivingEntity quem, int prazo) {
        marca(level, bicho, quem, prazo, true);
    }

    private static void marca(ServerLevel level, Entity bicho, LivingEntity quem, int prazo,
                              boolean montaria) {
        if (!(quem instanceof Player gente)) return;
        bicho.setAttached(DATA, new Summons(gente.getUUID(), level.getGameTime() + prazo, montaria));
        if (!(bicho instanceof Mob mob)) return;

        mob.setPersistenceRequired();
        if (mob instanceof net.minecraft.world.entity.TamableAnimal domado) domado.tame(gente);
        trocaDeLado(mob, gente);
    }

    /**
     * A lista de alvos do {@code makeSummon_PlayerFaction}, e a vontade de seguir do {@code setOwner}.
     *
     * <p>No jogo de 2014 havia também de se trocar o {@code EntityAIAttackOnCollide}, porque lá a vontade de
     * bater trazia o alvo dentro dela. Hoje quem escolhe o alvo é só a lista de alvos, e por isso a troca de
     * lado é essa lista e mais nada: a vontade de bater que o bicho já tem bate em quem a lista der.
     * <b>Declarado no {@code PORTE.md}.</b>
     */
    private static void trocaDeLado(Mob mob, Player gente) {
        var alvos = ((MobGoalAccessor) mob).thaumcraft$targetSelector();
        alvos.removeAllGoals(g -> true);

        if (mob instanceof PathfinderMob andarilho) {
            alvos.addGoal(1, new HurtByTargetGoal(andarilho));
        }
        // monstro, mas nunca outra invocação: é o SummonEntitySelector
        alvos.addGoal(2, new NearestAttackableTargetGoal<>(mob, Monster.class, 0, true, false,
                (alvo, onde) -> !éInvocado(alvo)));
        alvos.addGoal(2, new NearestAttackableTargetGoal<>(mob, Slime.class, 0, true, false,
                (alvo, onde) -> !éInvocado(alvo)));
        alvos.addGoal(2, new NearestAttackableTargetGoal<>(mob, Ghast.class, 0, true, false,
                (alvo, onde) -> !éInvocado(alvo)));

        // e ela deixa de achar que quem a chamou é alvo
        if (mob.getTarget() == gente) mob.setTarget(null);

        if (mob instanceof PathfinderMob andarilho) {
            float passo = andarilho.getSpeed();
            if (passo <= 0.0f) passo = 1.0f;
            ((MobGoalAccessor) mob).thaumcraft$goalSelector()
                    .addGoal(1, new SummonFollowOwnerGoal(andarilho, passo, 10.0f, 20.0f));
        }
    }

    /**
     * Quantas invocações de pé esta pessoa tem.
     *
     * <p>Conta-se olhando o mundo, e não uma lista guardada — o original guarda ids e depois tem de os
     * <b>verificar</b> um a um, porque um bicho pode ter morrido sem avisar ninguém. Olhar o mundo dá a mesma
     * resposta sem a lista para manter.
     */
    public static int quantas(ServerLevel level, Player gente) {
        int conta = 0;
        for (Entity bicho : level.getAllEntities()) {
            Summons dado = bicho.getAttached(DATA);
            // a montaria não ocupa vaga: é do acréscimo do necromante, declarado no PORTE.md
            if (dado != null && !dado.montaria() && dado.dono().equals(gente.getUUID())
                    && bicho.isAlive()) {
                conta++;
            }
        }
        return conta;
    }

    /** Se ainda cabe mais uma, com o teto do original: o {@code getCanHaveMoreSummons}. */
    public static boolean cabeMais(ServerLevel level, LivingEntity quem) {
        return cabeMais(level, quem, TETO);
    }

    /**
     * E a mesma pergunta com <b>outro teto</b>, que é o que a Legião do necromante sobe.
     *
     * <p>No original não há esta pergunta, porque lá o teto só muda com uma perícia passiva. Declarado no
     * {@code PORTE.md}.
     */
    public static boolean cabeMais(ServerLevel level, LivingEntity quem, int teto) {
        if (!(quem instanceof Player gente)) return false;
        return quantas(level, gente) < teto;
    }

    /**
     * O prazo e a trela.
     *
     * <p>Acabado o prazo, ou ido o dono, ela morre do {@code unsummon}. <b>Menos se tiver nome próprio</b>: aí
     * ela se solta e fica no mundo, que é o {@code revertAI} do original.
     */
    public static void tick(ServerLevel level, Entity bicho) {
        Summons dado = bicho.getAttached(DATA);
        if (dado == null) return;

        if (level.getGameTime() >= dado.acabaEm()) {
            desfaz(level, bicho);
            return;
        }

        LivingEntity quem = level.getPlayerByUUID(dado.dono());
        if (quem == null || !quem.isAlive() || quem.distanceToSqr(bicho) > LONGE_DEMAIS) {
            if (bicho.hasCustomName()) solta(bicho);
            else desfaz(level, bicho);
        }
    }

    /** Cinco mil de dano, que é mais do que qualquer bicho tem. */
    private static void desfaz(ServerLevel level, Entity bicho) {
        bicho.hurtServer(level, TCDamageTypes.unsummon(level), DANO_DO_DESFAZER);
        // um bicho que não leva dano nenhum ainda tem de sair, senão o prazo não quer dizer nada
        if (bicho.isAlive()) bicho.discard();
    }

    /** E quem tem nome se solta: perde o dono, perde o prazo, e fica. */
    private static void solta(Entity bicho) {
        bicho.removeAttached(DATA);
    }
}
