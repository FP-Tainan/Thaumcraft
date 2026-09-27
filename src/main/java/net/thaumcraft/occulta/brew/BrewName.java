package net.thaumcraft.occulta.brew;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Como um cozimento se chama: o {@code BrewNameBuilder} e o {@code BrewNamePart} do Witchery.
 *
 * <p>O nome não está escrito em lugar nenhum — ele é <b>montado</b> do que está dentro do caldeirão, na ordem em
 * que as coisas caíram. Cada efeito põe o seu pedaço; cada tempero antes dele muda o pedaço seguinte, com a força
 * ("II", "III") e com a duração; e os ingredientes de espalhamento põem prefixos e sufixos.
 *
 * <p>São duas leituras da mesma lista: a <b>curta</b>, que vira o nome do frasco ("Cozimento de Veneno e
 * Velocidade II"), e a <b>longa</b>, que vira a descrição — uma linha por efeito, com o tempo de cada um.
 */
public final class BrewName {
    /** Onde o pedaço entra no nome. */
    public enum Position {
        NONE, PREFIX, POSTFIX
    }

    /** Um pedaço do nome. */
    public interface Part {
        void applyTo(Builder builder);
    }

    /**
     * O pedaço de um efeito: o nome dele, o nome que ele tem quando o cozimento está invertido, e quanto tempo
     * cada um dos dois dura.
     */
    public record Text(String key, String invertedKey, Position position, int duration, int invertedDuration)
            implements Part {
        public Text(String key) {
            this(key, key, Position.NONE, 0, 0);
        }

        public Text(String key, Position position) {
            this(key, key, position, 0, 0);
        }

        public Text(String key, String invertedKey) {
            this(key, invertedKey, Position.NONE, 0, 0);
        }

        /** O {@code setBaseDuration}: quanto o efeito dura, e quanto dura o contrário dele. */
        public Text lasting(int ticks, int invertedTicks) {
            return new Text(this.key, this.invertedKey, this.position, ticks, invertedTicks);
        }

        public Text lasting(int ticks) {
            return this.lasting(ticks, ticks);
        }

        @Override
        public void applyTo(Builder builder) {
            switch (this.position) {
                case NONE -> builder.append(this);
                case PREFIX -> builder.appendPrefix(Component.translatable(this.key));
                case POSTFIX -> builder.appendPostfix(Component.translatable(this.key));
            }
        }
    }

    /** O pedaço de um tempero: não escreve nada, muda o pedaço seguinte. */
    public record Tweak(int strength, int duration, boolean inverted, int spreadExtent, int spreadDuration,
                        boolean removeCeiling) implements Part {
        public Tweak(int strength, int duration, boolean inverted, int spreadExtent, int spreadDuration) {
            this(strength, duration, inverted, spreadExtent, spreadDuration, false);
        }

        @Override
        public void applyTo(Builder builder) {
            builder.spreadExtent += this.spreadExtent;
            builder.spreadDuration += this.spreadDuration;
            builder.addStrength(this.strength);
            builder.addDuration(this.duration);
            if (this.inverted) builder.inverted = true;
            if (this.removeCeiling) builder.removeCeiling = true;
        }
    }

    /**
     * O montador.
     *
     * <p>Com {@code curto} verdadeiro sai o nome do frasco; com falso, a descrição de várias linhas — que é a
     * diferença entre o {@code getBrewName} e o {@code getBrewInformation} do original.
     */
    public static class Builder {
        private record Piece(Component base, long duration) {
            Component toComponent(boolean splash, boolean curto) {
                long quanto = splash && this.duration > 0 ? this.duration / 2 : this.duration;
                if (curto || quanto <= 0) return this.base;
                return Component.empty().append(this.base)
                        .append(Component.literal(" [" + clock((int) quanto) + "]"));
            }
        }

        private final boolean curto;
        private final List<Piece> pieces = new ArrayList<>();
        private final List<Component> prefixes = new ArrayList<>();
        private final List<Component> postfixes = new ArrayList<>();

        int spreadExtent;
        int spreadDuration;
        int strength;
        int durationModifier;
        int totalStrength;
        int totalDuration;
        boolean inverted;
        boolean removeCeiling;

        public Builder(boolean curto) {
            this.curto = curto;
        }

        /** O relógio do original: minutos e segundos, do jeito que ele escreve. */
        private static String clock(int ticks) {
            int segundos = ticks / 20;
            int minutos = segundos / 60;
            segundos %= 60;
            return segundos < 10 ? minutos + ":0" + segundos : minutos + ":" + segundos;
        }

        void append(Text pedaço) {
            MutableComponent texto = Component.translatable(this.inverted ? pedaço.invertedKey() : pedaço.key());
            long dura = this.inverted ? pedaço.invertedDuration() : pedaço.duration();
            this.inverted = false;
            if (!this.curto && this.strength > 0) {
                texto = texto.append(Component.literal(" "))
                        .append(Component.translatable("tc.brew.potency." + this.strength));
            }
            this.strength = 0;
            this.pieces.add(new Piece(texto, dura * (this.durationModifier + 1)));
            this.durationModifier = 0;
        }

        void appendPrefix(Component texto) {
            this.prefixes.add(texto);
            if (!this.curto && this.spreadExtent > 0) {
                this.prefixes.add(Component.translatable("tc.brew.potency." + this.spreadExtent));
            }
            this.spreadExtent = 0;
            if (!this.curto && this.spreadDuration > 0) {
                this.prefixes.add(Component.translatable("tc.brew.lifetime",
                        Component.translatable("tc.brew.potency." + this.spreadDuration)));
            }
            this.spreadDuration = 0;
        }

        void appendPostfix(Component texto) {
            this.postfixes.add(texto);
        }

        void addStrength(int quanto) {
            if (this.totalStrength < BrewModifiers.CEILING || this.removeCeiling) {
                this.strength += quanto;
                this.totalStrength += quanto;
            }
        }

        void addDuration(int quanto) {
            if (this.totalDuration < BrewModifiers.CEILING || this.removeCeiling) {
                this.durationModifier += quanto;
                this.totalDuration += quanto;
            }
        }

        /** O nome do frasco, de uma linha só. */
        public Component build() {
            MutableComponent saída = Component.empty();
            boolean splash = !this.prefixes.isEmpty();
            for (Component prefixo : this.prefixes) saída.append(prefixo).append(" ");
            saída.append(Component.translatable("tc.brew.of")).append(" ");
            if (this.pieces.isEmpty()) {
                saída.append(Component.translatable("tc.brew.water"));
            } else {
                for (int i = 0; i < this.pieces.size(); i++) {
                    saída.append(this.pieces.get(i).toComponent(splash, true));
                    if (i < this.pieces.size() - 2) saída.append(", ");
                    else if (i < this.pieces.size() - 1) saída.append(" ")
                            .append(Component.translatable("tc.brew.and")).append(" ");
                }
            }
            for (Component sufixo : this.postfixes) saída.append(" ").append(sufixo);
            return saída;
        }

        /** A descrição: uma linha por efeito, com o tempo de cada um. */
        public List<Component> lines() {
            List<Component> saída = new ArrayList<>();
            boolean splash = !this.prefixes.isEmpty();
            for (Component prefixo : this.prefixes) {
                saída.add(prefixo.copy().withStyle(ChatFormatting.GRAY));
            }
            if (this.pieces.isEmpty()) {
                saída.add(Component.translatable("tc.brew.water").withStyle(ChatFormatting.GRAY));
            } else {
                for (Piece pedaço : this.pieces) {
                    saída.add(pedaço.toComponent(splash, false).copy().withStyle(ChatFormatting.GRAY));
                }
            }
            for (Component sufixo : this.postfixes) {
                saída.add(sufixo.copy().withStyle(ChatFormatting.GRAY));
            }
            return saída;
        }
    }

    private BrewName() {
    }
}
