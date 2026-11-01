package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.occulta.AltarBlockEntity;
import net.thaumcraft.occulta.PowerSources;

import java.util.ArrayList;
import java.util.List;

/**
 * O que um rito pede antes de acontecer: o {@code Sacrifice} do Witchery.
 *
 * <p>São três feitios: as <b>coisas no chão</b> dentro do círculo, o <b>poder do altar</b>, e a junta dos dois.
 * O que se pede em coisas some ao começar o rito — e volta, se o rito desistir pedindo devolução.
 */
public sealed interface Sacrifice {
    /** Se o que está no chão dá para este rito começar. */
    boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão);

    /** Os passos que este pedido põe na fila, antes dos do rito. */
    void steps(List<RiteStep> fila);

    /** O que ele pede, para o livro. */
    List<ItemStack> shown();

    /**
     * As coisas no chão: o {@code SacrificeItem}.
     *
     * <p>Guarda os <b>itens</b>, e não pilhas prontas: a lista dos ritos se monta quando o mod acorda, e nessa
     * hora ainda não há pilha que se possa fazer. Cada pedido é de <b>um</b> — que é como estão todos no
     * original; para pedir dois, põe-se o mesmo item duas vezes.
     */
    record Items(List<Item> wanted) implements Sacrifice {
        public Items(Item... quais) {
            this(List.of(quais));
        }

        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            return find(noChão, this.wanted) != null;
        }

        @Override
        public void steps(List<RiteStep> fila) {
            fila.add(new TakeItems(this.wanted));
        }

        @Override
        public List<ItemStack> shown() {
            List<ItemStack> mostra = new ArrayList<>();
            for (Item qual : this.wanted) mostra.add(new ItemStack(qual));
            return List.copyOf(mostra);
        }
    }

    /**
     * Uma <b>poção certa</b> no chão: o pedido que olha <b>dentro</b> do frasco.
     *
     * <p>Todos os outros pedidos do ofício se bastam com o <b>item</b>, porque em 2014 cada coisa era um
     * item e um número. A poção não: ela é o mesmo item para todas as quarenta, e o que a distingue está
     * guardado dentro dela.
     *
     * <p>Um só rito a pede — o de <b>recarregar a infusão</b>, que no original quer uma Poção de
     * Regeneração e mais quatro mil de altar. O porte pedia «uma poção», qualquer uma, e uma de dano
     * servia. Não serve: o rito chama-se recarregar.
     */
    record Brewed(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> wanted)
            implements Sacrifice {
        /** Se esta pilha é a poção pedida. */
        public boolean é(ItemStack coisa) {
            var dentro = coisa.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            return dentro != null && dentro.potion().filter(this.wanted::equals).isPresent();
        }

        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            return noChão.stream().anyMatch(largado -> this.é(largado.getItem()));
        }

        @Override
        public void steps(List<RiteStep> fila) {
            fila.add(new TakeBrewed(this.wanted));
        }

        @Override
        public List<ItemStack> shown() {
            return List.of(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.POTION, this.wanted));
        }
    }

    /** E o passo que a toma. */
    record TakeBrewed(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> wanted)
            implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            var pede = new Brewed(this.wanted);
            for (ItemEntity largado : onTheGround(level, onde)) {
                if (!pede.é(largado.getItem())) continue;
                ItemStack tirado = largado.getItem().copyWithCount(1);
                rito.offer(tirado, largado.blockPosition());
                largado.getItem().shrink(1);
                if (largado.getItem().isEmpty()) largado.discard();
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                        largado.getX(), largado.getY() + 0.5, largado.getZ(), 8, 0.2, 0.2, 0.2, 0.0);
                level.playSound(null, onde, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                        1.0f, 1.0f);
                return Result.COMPLETED;
            }
            return Result.ABORTED_REFUND;
        }
    }

    /** O poder do altar: o {@code SacrificePower}. */
    record Power(float amount, int everyTicks) implements Sacrifice {
        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            return true;
        }

        @Override
        public void steps(List<RiteStep> fila) {
            fila.add(new TakePower(this.amount, this.everyTicks));
        }

        @Override
        public List<ItemStack> shown() {
            return List.of();
        }
    }

    /** Os dois juntos: o {@code SacrificeMultiple}. */
    record Both(List<Sacrifice> all) implements Sacrifice {
        public Both(Sacrifice... quais) {
            this(List.of(quais));
        }

        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            for (Sacrifice qual : this.all) {
                if (!qual.matches(level, meio, noChão)) return false;
            }
            return true;
        }

        @Override
        public void steps(List<RiteStep> fila) {
            for (Sacrifice qual : this.all) qual.steps(fila);
        }

        @Override
        public List<ItemStack> shown() {
            List<ItemStack> tudo = new ArrayList<>();
            for (Sacrifice qual : this.all) tudo.addAll(qual.shown());
            return List.copyOf(tudo);
        }
    }

    /**
     * Uma coisa <b>que não é obrigatória</b>: o {@code SacrificeOptionalItem} do Witchery.
     *
     * <p>Ela não impede o rito de começar nem de acabar. Estando lá, é <b>tomada</b> — e, sendo uma
     * <b>Pedra de Caminho presa</b>, ela muda o <b>alvo</b> do rito: o que ele fosse fazer à volta do
     * círculo passa a fazer onde a pedra aponta.
     *
     * <p>Duas receitas a usam, e as duas são de obra: <b>erguer a terra</b> e o <b>vulcão</b>. Com a
     * pedra no chão, levanta-se uma coluna — ou abre-se um vulcão — a quilómetros dali.
     */
    record Optional(Item wanted) implements Sacrifice {
        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            return true;
        }

        @Override
        public void steps(List<RiteStep> fila) {
            fila.add(new TakeOptional(this.wanted));
        }

        @Override
        public List<ItemStack> shown() {
            return List.of(new ItemStack(this.wanted));
        }
    }

    /** E o passo que a toma, se ela estiver lá. */
    record TakeOptional(Item wanted) implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            for (ItemEntity largado : onTheGround(level, onde)) {
                if (!largado.getItem().is(this.wanted)) continue;

                /*
                 * E se o que estiver lá for uma Pedra de Caminho presa, o rito muda de sítio. Uma pedra
                 * de outro mundo não serve: o rito faz o que faz <b>aqui</b>.
                 */
                var lugar = net.thaumcraft.occulta.waystone.Waystones.lugar(largado.getItem());
                if (lugar != null && lugar.mundo().equals(level.dimension())) {
                    rito.setTarget(lugar.onde());
                }

                ItemStack tirado = largado.getItem().copyWithCount(1);
                rito.offer(tirado, largado.blockPosition());
                largado.getItem().shrink(1);
                if (largado.getItem().isEmpty()) largado.discard();
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                        largado.getX(), largado.getY() + 0.5, largado.getZ(), 8, 0.2, 0.2, 0.2, 0.0);
                break;
            }
            return Result.COMPLETED;
        }
    }

    /** Nada: há ritos que só pedem o círculo. */
    record None() implements Sacrifice {
        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            return true;
        }

        @Override
        public void steps(List<RiteStep> fila) {
        }

        @Override
        public List<ItemStack> shown() {
            return List.of();
        }
    }

    /** Onde o rito procura o que está no chão: os oito blocos em volta do glifo do meio. */
    int RADIUS = 8;

    /** As coisas largadas dentro do círculo. */
    static List<ItemEntity> onTheGround(ServerLevel level, BlockPos meio) {
        AABB dentro = new AABB(meio).inflate(RADIUS, 1.0, RADIUS);
        return level.getEntitiesOfClass(ItemEntity.class, dentro);
    }

    /**
     * Acha, entre o que está no chão, um de cada coisa pedida — ou nada, se faltar alguma.
     *
     * <p>Uma coisa largada só serve a um pedido: dois pedidos iguais precisam de dois montes.
     */
    static List<ItemEntity> find(List<ItemEntity> noChão, List<Item> pedidos) {
        List<ItemEntity> achados = new ArrayList<>();
        for (Item pede : pedidos) {
            ItemEntity achado = null;
            for (ItemEntity largado : noChão) {
                if (achados.contains(largado)) continue;
                if (!largado.getItem().is(pede)) continue;
                achado = largado;
                break;
            }
            if (achado == null) return null;
            achados.add(achado);
        }
        return achados;
    }

    /** O passo que toma o que está no chão. */
    record TakeItems(List<Item> wanted) implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            List<ItemEntity> achados = find(onTheGround(level, onde), this.wanted);
            if (achados == null) return Result.ABORTED_REFUND;
            for (ItemEntity largado : achados) {
                ItemStack tirado = largado.getItem().copyWithCount(1);
                rito.offer(tirado, largado.blockPosition());
                largado.getItem().shrink(1);
                if (largado.getItem().isEmpty()) largado.discard();
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                        largado.getX(), largado.getY() + 0.5, largado.getZ(), 8, 0.2, 0.2, 0.2, 0.0);
            }
            level.playSound(null, onde, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
            return Result.COMPLETED;
        }
    }

    /**
     * Um <b>vivo</b> dentro do círculo: o {@code SacrificeLiving} do Witchery.
     *
     * <p>Não é um item que se larga: é um bicho que tem de <b>estar lá</b> quando o rito corre, e que
     * <b>some</b> quando ele o toma. Dois ritos deste porte o pedem, e os dois pedem um <b>aldeão</b> — que é
     * o que lhes dá o peso que têm.
     *
     * <p><b>Ele não entra na conta de antes.</b> O {@code isMatch} do original devolve sempre que sim, e por
     * isso o círculo aceita começar sem o bicho lá: quem descobre que falta é o <b>passo</b>, e aí o rito
     * desiste e devolve o resto. É de propósito, e é o que faz um rito começar e morrer à vista de quem o fez.
     */
    record Living(EntityType<?> what) implements Sacrifice {
        @Override
        public boolean matches(ServerLevel level, BlockPos meio, List<ItemEntity> noChão) {
            return true;
        }

        @Override
        public void steps(List<RiteStep> fila) {
            fila.add(new TakeLiving(this.what));
        }

        @Override
        public List<ItemStack> shown() {
            return List.of();
        }
    }

    /** E o passo que o toma. */
    record TakeLiving(EntityType<?> what) implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            if (ticks % 20L != 0L) return Result.STARTING;

            AABB dentro = new AABB(onde).inflate(RADIUS + 1.0, RADIUS + 1.0, RADIUS + 1.0);
            for (var bicho : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, dentro)) {
                if (bicho.getType() != this.what) continue;
                if (bicho.distanceToSqr(onde.getX(), onde.getY(), onde.getZ())
                        > (RADIUS + 1.0) * (RADIUS + 1.0)) {
                    continue;
                }
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        bicho.getX(), bicho.getY() + 1.0, bicho.getZ(), 32, 0.5, 1.0, 0.5, 0.1);
                level.playSound(null, bicho.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                bicho.discard();
                return Result.COMPLETED;
            }

            Player gente = rito.starter(level);
            if (gente != null) {
                gente.sendSystemMessage(net.minecraft.network.chat.Component
                        .translatable("message.thaumcraft.missing_living_sacrifice")
                        .withStyle(net.minecraft.ChatFormatting.RED));
            }
            level.playSound(null, onde, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return Result.ABORTED_REFUND;
        }
    }

    /** E o passo que toma o poder do altar. */
    record TakePower(float amount, int everyTicks) implements RiteStep {
        @Override
        public Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito) {
            if (this.everyTicks > 0 && ticks % this.everyTicks != 0) return Result.STARTING;
            AltarBlockEntity altar = PowerSources.closest(level, onde);
            if (altar == null) return Result.ABORTED_REFUND;
            if (!altar.consume(this.amount)) return Result.STARTING;
            return Result.COMPLETED;
        }
    }
}
