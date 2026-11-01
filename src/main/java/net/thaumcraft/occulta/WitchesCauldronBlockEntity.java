package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * O miolo do Caldeirão da Bruxa: o {@code TileEntityCauldron} do Witchery.
 *
 * <p>Ele guarda três coisas: a <b>água</b> (três baldes, como o tanque de três mil do original), há quanto tempo
 * está ao <b>fogo</b>, e o que já foi <b>jogado dentro</b>. Cheio e com fogo embaixo, ferve em cinco segundos; a
 * partir daí engole o que cai nele.
 *
 * <p>Quando cai dentro a coisa que <b>dispara</b> uma receita e o que está dentro bate com ela, o caldeirão leva
 * três segundos a remexer e então larga o que a receita faz, e esvazia.
 *
 * <p><b>Do original fica de fora, por agora,</b> tudo o que o ritual dele consulta e que ainda não existe aqui: o
 * poder do altar, os círculos de giz, o coven de bruxas em volta, a distância a que quem joga está e a má sorte
 * que cai sobre quem erra. O que sobra é a espera e o resultado, que é o que se vê de fora.
 */
public class WitchesCauldronBlockEntity extends BlockEntity {
    /** O tanque do original: três baldes. */
    public static final int BUCKET = 1000;
    public static final int FULL = 3000;

    /** Cinco segundos ao fogo até ferver, e três remexendo até a receita sair. */
    public static final int TICKS_TO_BOIL = 100;
    public static final int RITUAL_TICKS = 60;

    /**
     * A cor da água parada.
     *
     * <p>É o azul da água do jogo, que é de onde a mistura parte: no original a água do caldeirão também começa
     * água e só ganha cor com o que se joga dentro.
     */
    public static final int PLAIN_COLOR = 0x3F76E4;

    private int water;
    private int heated;
    private int ritual;
    private final List<Item> inside = new ArrayList<>();
    private ItemStack result = ItemStack.EMPTY;
    private @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EntityType<?> chamando;

    /**
     * E o <b>bioma</b> da última Nota de Bioma que caiu dentro.
     *
     * <p>A lista do que está na panela guarda <b>coisas</b> — não guarda o que uma delas tem escrito dentro.
     * A nota é o único ingrediente do ofício que carrega mais do que a própria identidade, e por isso o que
     * ela diz fica guardado aqui ao lado, e sai daqui para o frasco.
     */
    private @org.jetbrains.annotations.Nullable net.minecraft.resources.ResourceKey<
            net.minecraft.world.level.biome.Biome> bioma;

    /**
     * E a <b>Marca</b> que caiu dentro, se caiu alguma: o alvo do rito de cozimento.
     *
     * <p>É o mesmo arranjo do bioma, e pela mesma razão: a lista do que está na panela guarda coisas, e
     * uma marca é uma coisa que traz um <b>nome</b> dentro.
     */
    private @org.jetbrains.annotations.Nullable TaglockItem.Taglock marcado;

    /**
     * Quantas batidas o <b>rito de cozimento</b> já leva, ou zero.
     *
     * <p>Ele conta de dez em dez batidas do mundo, e só começa a fazer alguma coisa depois de vinte
     * contagens — que são as duzentas batidas do original a aquecer o caldeirão antes de ele lançar.
     */
    private int ritoDeCozimento;
    private boolean powered;

    public WitchesCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(OccultaBlocks.WITCHES_CAULDRON_ENTITY, pos, state);
    }

    // ------------------------------------------------------------------ o que ele tem

    public int water() {
        return this.water;
    }

    public boolean isFull() {
        return this.water >= FULL;
    }

    /** O quanto do caldeirão está cheio, de zero a um. */
    public float filled() {
        return this.water / (float) FULL;
    }

    /** Quantas batidas de fogo ele já levou. */
    public int heated() {
        return this.heated;
    }

    public boolean isBoiling() {
        return this.heated >= TICKS_TO_BOIL;
    }

    public boolean isRitualInProgress() {
        return this.ritual > 0;
    }

    public List<Item> inside() {
        return List.copyOf(this.inside);
    }

    /** A Marca que caiu dentro, se caiu alguma. */
    public @org.jetbrains.annotations.Nullable TaglockItem.Taglock marcado() {
        return this.marcado;
    }

    /** Quantas batidas o rito de cozimento já leva. */
    public int ritoDeCozimento() {
        return this.ritoDeCozimento;
    }

    /** Se o que está na panela faz dela um rito. */
    public boolean éRito() {
        return net.thaumcraft.occulta.brew.Brew.ritual(this.inside) != null;
    }

    /** E o bioma da Nota de Bioma que caiu dentro, se caiu alguma. */
    public @org.jetbrains.annotations.Nullable net.minecraft.resources.ResourceKey<
            net.minecraft.world.level.biome.Biome> bioma() {
        return this.bioma;
    }

    /**
     * A cor do que está dentro: a mistura das cores das coisas jogadas, como o {@code augmentColor} do original —
     * que mistura meio a meio, uma a uma.
     */
    public int color() {
        if (this.inside.isEmpty()) return PLAIN_COLOR;
        // o cozimento manda na cor com a conta do original; as coisas de ritual, com o aspecto maior delas
        if (this.isBrewing()) return net.thaumcraft.occulta.brew.Brew.color(this.inside);
        int cor = PLAIN_COLOR;
        for (Item item : this.inside) cor = mix(cor, colorOf(item));
        return cor;
    }

    /** Se o que está dentro é cozimento, e não ingrediente de ritual. */
    public boolean isBrewing() {
        for (Item item : this.inside) {
            if (net.thaumcraft.occulta.brew.BrewRegistry.knows(item)) return true;
        }
        return false;
    }

    /** O poder que o altar tem de ter para este cozimento sair. */
    public int brewPower() {
        return this.isBrewing() ? net.thaumcraft.occulta.brew.Brew.power(this.inside) : 0;
    }

    /** Se há altar por perto com o poder que este cozimento pede: o {@code isPowered} do original. */
    public boolean isPowered() {
        return this.powered;
    }

    private static int mix(int a, int b) {
        int r = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF)) / 2;
        int g = (((a >> 8) & 0xFF) + ((b >> 8) & 0xFF)) / 2;
        int azul = ((a & 0xFF) + (b & 0xFF)) / 2;
        return (r << 16) | (g << 8) | azul;
    }

    /**
     * A cor de cada coisa que se joga dentro.
     *
     * <p><b>Desvio declarado.</b> O original guarda uma cor por item, numa tabela que só existe junto das poções.
     * Enquanto elas não chegam, a cor de cada coisa é a do <b>aspecto maior</b> dela — o que o thaumômetro lê. É
     * mais coisa de Thaumcraft do que de Witchery, e é o que faz a água ficar verde com erva e roxa com magia.
     */
    private static int colorOf(Item item) {
        var aspectos = net.thaumcraft.api.aspects.ObjectAspects.of(item);
        var maior = aspectos.getAspectsSortedAmount();
        if (maior.isEmpty()) return PLAIN_COLOR;
        return maior.get(0).color();
    }

    // ------------------------------------------------------------------ o que se faz com ele

    /** Enche um tanto; devolve se coube alguma coisa. */
    public boolean fill(int quanto) {
        if (this.water >= FULL) return false;
        this.water = Math.min(FULL, this.water + quanto);
        this.changed();
        return true;
    }

    /** Esvazia, e perde o que estava dentro. */
    public void empty() {
        this.water = 0;
        this.heated = 0;
        this.ritual = 0;
        this.inside.clear();
        this.result = ItemStack.EMPTY;
        this.chamando = null;
        this.bioma = null;
        this.marcado = null;
        this.ritoDeCozimento = 0;
        this.changed();
    }

    /**
     * O {@code addItem}: a coisa entra na panela se servir para alguma receita — e, se for a que dispara e o que
     * está dentro bater, começa a mexer.
     */
    public boolean addItem(ItemStack caiu) {
        if (!this.isBoiling() || this.isRitualInProgress()) return false;
        Item item = caiu.getItem();
        ItemStack sai = OccultaRituals.result(item, this.inside);
        var bicho = OccultaRituals.summons(item, this.inside);
        if (!sai.isEmpty() || bicho != null) {
            this.result = sai;
            this.chamando = bicho;
            this.ritual = RITUAL_TICKS;
            this.inside.add(item);
            this.changed();
            return true;
        }
        if (OccultaRituals.isIngredient(item)) {
            this.inside.add(item);
            this.changed();
            return true;
        }
        // e o que não é de receita pode ser de cozimento
        if (!net.thaumcraft.occulta.brew.Brew.canAdd(this.inside, item, this.isFull())) return false;
        List<Item> depois = net.thaumcraft.occulta.brew.Brew.add(this.inside, item);
        this.inside.clear();
        this.inside.addAll(depois);
        /*
         * E se o que caiu foi uma <b>Nota de Bioma</b>, o que ela tem escrito fica guardado: é a única coisa
         * do ofício cujo conteúdo o caldeirão precisa de lembrar. Duas notas, e vale a última — como vale a
         * última de qualquer coisa que se repita na panela.
         */
        if (item instanceof net.thaumcraft.occulta.biome.BiomeNoteItem) {
            this.bioma = net.thaumcraft.occulta.biome.BiomeNoteItem.bioma(caiu);
        }
        // e a Marca traz o nome de quem o rito vai apanhar
        if (item instanceof TaglockItem) this.marcado = TaglockItem.bound(caiu);
        this.changed();
        return true;
    }

    /**
     * Uma batida do <b>rito de cozimento</b>: o laço do {@code updateEntity} do original.
     *
     * <p>Ele só corre com o caldeirão a <b>ferver</b>, com <b>altar</b>, <b>cheio</b>, e com um gatilho de
     * rito lá dentro. E não começa logo: conta <b>vinte</b> olhadas — duzentas batidas — antes de lançar
     * a primeira vez. É o caldeirão a tomar balanço, e é o que dá tempo a quem o acendeu de sair do
     * círculo.
     */
    private static void rito(ServerLevel level, BlockPos pos, WitchesCauldronBlockEntity caldeirão) {
        if (!caldeirão.isBoiling() || !caldeirão.isPowered() || !caldeirão.isFull()
                || !caldeirão.éRito()) {
            if (caldeirão.ritoDeCozimento > 0) {
                caldeirão.ritoDeCozimento = 0;
                caldeirão.changed();
            }
            return;
        }

        caldeirão.ritoDeCozimento++;
        if (caldeirão.ritoDeCozimento <= net.thaumcraft.occulta.brew.BrewRitual.ESPERA) {
            net.thaumcraft.occulta.brew.BrewRitual.fumo(level, pos);
            return;
        }

        var gatilho = net.thaumcraft.occulta.brew.Brew.ritual(caldeirão.inside);
        int poder = gatilho == null ? 0 : gatilho.power();
        var marcado = caldeirão.quemAMarcaNomeia(level);
        net.minecraft.world.phys.Vec3 aqui = net.minecraft.world.phys.Vec3.atCenterOf(pos.above());
        int pulsos = caldeirão.ritoDeCozimento - net.thaumcraft.occulta.brew.BrewRitual.ESPERA;

        // com Marca na panela, o alvo é a pessoa — e se ela não estiver no mundo, não há rito
        if (caldeirão.marcado != null && marcado == null) {
            net.thaumcraft.occulta.brew.BrewRitual.falhou(level, pos);
            caldeirão.ritoDeCozimento = 0;
            caldeirão.changed();
            return;
        }

        var deu = net.thaumcraft.occulta.brew.BrewRitual.corre(level, pos, marcado, aqui,
                caldeirão.inside, pulsos, poder);
        switch (deu) {
            case ANDANDO -> net.thaumcraft.occulta.brew.BrewRitual.fumo(level, pos);
            case PRONTO -> {
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                caldeirão.empty();
            }
            default -> {
                net.thaumcraft.occulta.brew.BrewRitual.falhou(level, pos);
                caldeirão.ritoDeCozimento = 0;
                caldeirão.changed();
            }
        }
    }

    /**
     * <b>Quem a Marca nomeia</b>, se ela estiver no mundo.
     *
     * <p>Sem marca não há pessoa nenhuma, e o rito cai à volta do próprio caldeirão — que é a Língua de
     * Cão a espalhar o cozimento por onde está.
     */
    public @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity quemAMarcaNomeia(
            ServerLevel level) {
        if (this.marcado == null) return null;
        var gente = level.getPlayerByUUID(this.marcado.owner());
        if (gente != null) return gente;
        return level.getEntity(this.marcado.owner())
                instanceof net.minecraft.world.entity.LivingEntity vivo ? vivo : null;
    }

    /**
     * O frasco que se tira do caldeirão: o {@code fillBottleFromCauldron} do original.
     *
     * <p>Pede três coisas: que esteja <b>fervendo</b>, que haja <b>altar</b> com o poder do cozimento e que o
     * caldeirão esteja <b>cheio</b> — o original tira uma garrafa por caldeirão a quem ainda não sabe engarrafar,
     * e é aí que este porte está.
     *
     * <p>E ele <b>não esvazia</b>: cada frasco <b>drena</b> uma parte dos três mil, e quanto se drena sai
     * da {@linkplain net.thaumcraft.occulta.brew.Bottling perícia de engarrafar} de quem o tira. Quem
     * nunca engarrafou leva os três mil de uma vez e fica com um frasco; quem já engarrafou muito, e
     * tiver o chapéu, o manto, o sapo e a Perícia de Cozimento a correr, tira <b>trinta</b> do mesmo
     * caldeirão.
     *
     * <p>É a diferença mais calada que o ofício tem, e a maior.
     *
     * @param quem quem está engarrafando, ou nulo quando não é ninguém (uma prova, um funil)
     */
    public ItemStack bottle(ServerLevel level, BlockPos pos,
                            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.player.Player quem) {
        if (!this.isBoiling() || !this.isBrewing() || !this.isFull()) return ItemStack.EMPTY;
        int poder = this.brewPower();
        if (poder > 0) {
            var altar = PowerSources.closest(level, pos);
            if (altar == null || !altar.consume(poder)) return ItemStack.EMPTY;
        }
        ItemStack frasco = net.thaumcraft.occulta.brew.BrewItem.of(OccultaItems.BREW, this.inside);
        if (this.bioma != null) frasco.set(OccultaComponents.BIOME, this.bioma);

        /*
         * E aprende-se a engarrafar engarrafando — mas só com cozimento à altura, e o que se aprende só
         * se vê no <b>caldeirão seguinte</b>: o dreno deste já foi medido antes de a perícia subir.
         */
        int dreno = net.thaumcraft.occulta.brew.Bottling.dreno(quem, this.inside);
        if (quem != null) {
            net.thaumcraft.occulta.brew.Bottling.aprende(quem,
                    net.thaumcraft.occulta.brew.Brew.capacity(this.inside));
        }

        this.water -= dreno;
        if (this.water <= 0) this.empty();
        else this.changed();
        return frasco;
    }

    /** Quebrado, larga no chão o que estava dentro. */
    public void spill(ServerLevel level, BlockPos pos) {
        for (Item item : this.inside) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(item)));
        }
        this.inside.clear();
    }

    // ------------------------------------------------------------------ o que ele faz sozinho

    /** O {@code updateEntity}: o fogo embaixo, a fervura e o remexer da receita. */
    public static void tick(Level level, BlockPos pos, BlockState state, WitchesCauldronBlockEntity caldeirão) {
        boolean fogo = level.getBlockState(pos.below()).is(Blocks.FIRE);
        if (fogo && caldeirão.isFull()) {
            if (caldeirão.heated < TICKS_TO_BOIL) {
                caldeirão.heated++;
                if (caldeirão.heated == TICKS_TO_BOIL) caldeirão.changed();
            }
        } else if (caldeirão.heated > 0) {
            caldeirão.heated = 0;
            caldeirão.changed();
        }

        // o {@code isPowered} do original: de vinte em vinte batidas se olha se há altar com o poder pedido
        if (level instanceof ServerLevel server && level.getGameTime() % 20 == 7) {
            int poder = caldeirão.brewPower();
            boolean antes = caldeirão.powered;
            if (!caldeirão.isBoiling()) caldeirão.powered = false;
            else if (poder == 0) caldeirão.powered = true;
            else {
                var altar = PowerSources.closest(server, pos);
                caldeirão.powered = altar != null && altar.power() >= poder;
            }
            if (antes != caldeirão.powered) caldeirão.changed();
        }

        // e o rito de cozimento, que é o caldeirão a lançar em vez de a cozinhar
        if (level instanceof ServerLevel server
                && level.getGameTime() % net.thaumcraft.occulta.brew.BrewRitual.DE_QUANTO == 8) {
            rito(server, pos, caldeirão);
        }

        if (caldeirão.ritual <= 0) return;
        caldeirão.ritual--;
        if (caldeirão.ritual > 0) return;

        // acabou de mexer: sai o que a receita faz, e o caldeirão esvazia
        ItemStack sai = caldeirão.result.copy();
        var bicho = caldeirão.chamando;
        caldeirão.empty();
        if (!(level instanceof ServerLevel server)) return;
        if (bicho != null) {
            chama(server, pos, bicho);
            return;
        }
        if (sai.isEmpty()) return;
        ItemEntity saiu = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, sai);
        saiu.setDeltaMovement(0.0, 0.2, 0.0);
        server.addFreshEntity(saiu);
        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                12, 0.3, 0.3, 0.3, 0.0);
        server.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /**
     * <b>O bicho que a receita chama</b>, no lugar da coisa que ela largaria.
     *
     * <p>Ele nasce <b>em cima</b> do caldeirão, com o estouro de pó e o grito do Wither que o original
     * usa, e <b>fica</b>: um chefe chamado não some porque ninguém está olhando. Sendo o <b>Leonard</b>,
     * começa a entrada dele — sete segundos e meio invulnerável, e um quarto da vida.
     */
    private static void chama(ServerLevel level, BlockPos pos, net.minecraft.world.entity.EntityType<?> qual) {
        var nasceu = net.thaumcraft.occulta.Spawn.perto(level, qual, pos.above(), 0, 0);
        if (nasceu == null) return;
        if (nasceu instanceof net.minecraft.world.entity.Mob mob) mob.setPersistenceRequired();
        if (nasceu instanceof net.thaumcraft.occulta.leonard.LeonardEntity leonard) {
            leonard.começaAEntrada();
        }
        level.sendParticles(ParticleTypes.EXPLOSION, nasceu.getX(), nasceu.getY() + 1.0, nasceu.getZ(),
                16, 1.0, 1.0, 1.0, 0.0);
        level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void changed() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    // ------------------------------------------------------------------ o que ele guarda

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.water = input.getIntOr("Water", 0);
        this.heated = input.getIntOr("Heated", 0);
        this.ritual = input.getIntOr("Ritual", 0);
        this.inside.clear();
        for (String nome : input.read("Inside", com.mojang.serialization.Codec.STRING.listOf())
                .orElse(List.of())) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(nome));
            if (item != null) this.inside.add(item);
        }
        this.result = input.read("Result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.bioma = input.read("Biome", net.minecraft.resources.ResourceKey.codec(
                net.minecraft.core.registries.Registries.BIOME)).orElse(null);
        this.marcado = input.read("Marcado", TaglockItem.Taglock.CODEC).orElse(null);
        this.ritoDeCozimento = input.getIntOr("RitoDeCozimento", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Water", this.water);
        output.putInt("Heated", this.heated);
        output.putInt("Ritual", this.ritual);
        List<String> nomes = new ArrayList<>();
        for (Item item : this.inside) nomes.add(BuiltInRegistries.ITEM.getKey(item).toString());
        output.store("Inside", com.mojang.serialization.Codec.STRING.listOf(), nomes);
        if (!this.result.isEmpty()) output.store("Result", ItemStack.CODEC, this.result);
        if (this.bioma != null) {
            output.store("Biome", net.minecraft.resources.ResourceKey.codec(
                    net.minecraft.core.registries.Registries.BIOME), this.bioma);
        }
        if (this.marcado != null) output.store("Marcado", TaglockItem.Taglock.CODEC, this.marcado);
        output.putInt("RitoDeCozimento", this.ritoDeCozimento);
    }

    /** O que o caldeirão manda ao cliente, para as bolhas saírem na cor certa. */
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
