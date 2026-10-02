package net.thaumcraft.occulta.coven;

import net.minecraft.util.RandomSource;

/**
 * Os nomes das bruxas: as duas listas do {@code EntityCovenWitch} do Witchery, tal e qual.
 *
 * <p>Duzentos e setenta e dois primeiros nomes e trezentos e noventa e oito sobrenomes — cento e oito mil
 * bruxas diferentes. <b>Uma bruxa do coven ganha nome quando alguém fala com ela</b>, e é esse nome que ela
 * leva para o resto da vida.
 *
 * <p>As listas são do original e não foram mexidas: são nomes de bruxa de história, de ópera e de folclore, e
 * trocá-los por outros seria trocar o sotaque do mod.
 */
public final class WitchNames {
    private static final String[] PRIMEIROS = {
        "Abigail", "Agatha", "Agony", "Alcina", "Alcyone", "Alexandra", "Alexandria", "Alvira", "Amanita",
        "Amaranth", "Amarantha", "Ambrosia", "Amelia", "Amethyst", "Anastasia", "Andromeda", "Angel", "Angela",
        "Angelica", "Angelique", "Anna", "Arachne", "Aradia", "Aria", "Arianna", "Ariadne", "Ariel", "Artemis",
        "Artemisia", "Astrea", "Astrid", "Astoria", "Autumn", "Aurora", "Beatrix", "Bella", "Belladonna", "Belle",
        "Bernadette", "Beryl", "Bianca", "Blanche", "Bliss", "Calliope", "Callypso", "Calpurnia", "Camilla",
        "Carlotta", "Carmilla", "Caroline", "Carrie", "Cassandra", "Cassiopeia", "Catherine", "Cathy", "Cecelia",
        "Celeste", "Celia", "Charlotte", "Christine", "Circe", "Clara", "Claudia", "Cleopatra", "Columbia",
        "Coraline", "Cordelia", "Cornelia", "Crystal", "Daphne", "Daria", "Darla", "Delia", "Delilah", "Della",
        "Demetria", "Demonica", "Desdemona", "Desire", "Dolores", "Dora", "Dove", "Drusilla", "Dusk", "Ebony",
        "Echo", "Eden", "Elanore", "Electra", "Eldora", "Elena", "Eliza", "Eloise", "Elphaba", "Elspeth",
        "Elsinore", "Elvira", "Ember", "Emilie", "Ephemera", "Eranthe", "Eris", "Esmerelda", "Estrella", "Esther",
        "Eterna", "Eternity", "Eudora", "Euphemia", "Eva", "Evalina", "Evangeline", "Eve", "Granny", "Gabriella",
        "Gabrielle", "Garnet", "Genevieve", "Godiva", "Hathor", "Hecate", "Hecuba", "Helena", "Hepzibah",
        "Hesperia", "Hippolita", "Ianthe", "Icie", "Icy", "Inez", "Infinity", "Ione", "Iris", "Isabeau",
        "Isabella", "Isabelle", "Isadora", "Isis", "Isolde", "Istar", "Ivy", "Izora", "Jane", "Jeanette", "Jinx",
        "Jocasta", "Juliet", "Katrina", "Lavinia", "Layla", "Leona", "Lenora", "Lenore", "Leona", "Libitina",
        "Ligeia", "Lilah", "Lilith", "Lillian", "Lily", "Lolita", "Lorraine", "Lucinda", "Lucretia", "Luna",
        "Lydia", "Lyra", "Madeline", "Magdalena", "Magenta", "Mara", "Marcella", "Margaret", "Marguerita", "Maria",
        "Marie", "Marissa", "Martha", "Matilda", "Medea", "Medusa", "Melanie", "Melantha", "Melanthe", "Melinda",
        "Mercedes", "Merula", "Mildred", "Mina", "Minerva", "Miranda", "Miriam", "Moira", "Mordea", "Morgan",
        "Morgana", "Morticia", "Nadia", "Nadine", "Nerezza", "Nora", "Nyx", "Obsidia", "Octavia", "Odessa",
        "Olivia", "Opal", "Ophelia", "Pandora", "Patience", "Pearl", "Penelope", "Perenelle", "Permelia",
        "Persephone", "Pixie", "Phoenix", "Poppy", "Priscilla", "Prudence", "Rachel", "Rain", "Raven", "Regina",
        "Rita", "Rosa", "Rose", "Rosemary", "Rowena", "Ruby", "Sabrina", "Salem", "Samantha", "Sangria", "Scarlet",
        "Selena", "Selene", "Sephora", "Seraphina", "Serena", "Serenity", "Shannon", "Silver", "Simone", "Sophia",
        "Sybella", "Sybil", "Sylvia", "Tabitha", "Tempest", "Theda", "Theresa", "Thora", "Threnody", "Trinity",
        "Twilight", "Umbra", "Vaitiare", "Valerie", "Vanessa", "Verna", "Verona", "Veronica", "Vesta", "Victoria",
        "Violet", "Whisper", "Willow", "Winter", "Xenobia", "Zillah", "Zinnia"
    };

    private static final String[] SOBRENOMES = {
        "Adams", "Addams", "Argent", "Ashwood", "Balfour", "Barker", "Batby", "Bathory", "Batsford", "Batson",
        "Batstone", "Batt", "Baudelaire", "Black", "Blackbird", "Blackburn", "Blackcat", "Blacklock", "Blackmoore",
        "Blackstone", "Blackthorn", "Blackwell", "Blackwing", "Blackwolf", "Blackwood", "Blair", "Blood",
        "Bloodgood", "Bloodhart", "Bloodmoore", "Bloodsaw", "Bloodstone", "Bloodsworth", "Bloodwine", "Bloodworth",
        "Boggart", "Boggarty", "Bonebrake", "Bonehart", "Bonehill", "Bonella", "Boneman", "Bones", "Bonesmith",
        "Bonewits", "Borden", "Broom", "Broomwood", "Burton", "Byron", "Cackler", "Cain", "Calamity", "Castle",
        "Castleton", "Cemetary", "Chill", "Chillingwood", "Cobweb", "Coffin", "Coffinberry", "Coffins", "Cold",
        "Coldbridge", "Coldeman", "Coldstone", "Coldwell", "Cole", "Collins", "Constantine", "Corbett", "Corbin",
        "Corpse", "Corpseley", "Creak", "Creakey", "Creep", "Creeper", "Creeps", "Crepsley", "Crimson", "Cross",
        "Crossway", "Crosswicks", "Crow", "Crowden", "Crowe", "Crowley", "Darcy", "Dark", "Darke", "Darken",
        "Darkenwald", "Darkes", "Darkmoore", "Darkwell", "Darkwood", "Deadman", "Deadmond", "Deadmore", "Deadrick",
        "Deadwood", "DeAngelus", "Dearborn", "Death", "Deathridge", "Deathrow", "December", "Delambre",
        "DeLioncourt", "Demond", "Demonde", "Demonte", "DeMort", "DeRavin", "Devall", "Devane", "DeVille",
        "DeWinter", "Dracul", "Drago", "Drake", "Dread", "Drear", "Dreary", "Drelincourt", "DuLac", "Dumaine",
        "Dunsany", "Eldritch", "Fang", "Fanger", "Fate", "Faust", "February", "Fear", "Fearfield", "Fears",
        "Frankenstein", "Frost", "Fury", "Gautier", "Ghoul", "Ghoulson", "Ghost", "Ghosten", "Ghostley", "Giger",
        "Goblin", "Goth", "Gotham", "Gothard", "Gothberg", "Gravedigger", "Gravemaker", "Graves", "Gravesen",
        "Gravesgard", "Grey", "Greyson", "Greystone", "Grimmauld", "Grimm", "Grimmer", "Grimmes", "Grimmins",
        "Grimsbro", "Grimsby", "Grimsman", "Grimwood", "Harker", "Hart", "Haunt", "Haunter", "Haunton", "Haunty",
        "Hawk", "Hawke", "Havelock", "Heart", "Heartstrom", "Hemlock", "Hex", "Hexem", "Hexter", "Hexwood",
        "Hollow", "Holmes", "Holmwood", "Hugo", "Hunter", "Hyde", "January", "Jekyll", "Kenrick", "Kilgore",
        "Killar", "Killewich", "Killings", "LaCroix", "Lapidus", "LaRue", "LeFay", "LeStrange", "LeStrange",
        "Locke", "London", "Loveless", "Lovelock", "Lovett", "Lycan", "MacBeth", "Mandrake", "Marrow", "Masters",
        "Mist", "Misteri", "Moan", "Moon", "Moones", "Moonie", "Moonly", "Monet", "Monster", "Monstery",
        "Montague", "Montresor", "Morgan", "Morgue", "Moriarty", "Murdoc", "Murray", "Morrow", "Mort", "Mortella",
        "Munster", "Mysterios", "Night", "Nightchase", "Nightengale", "Nightingdale", "Nightman", "Nightwalker",
        "Nightwine", "Nocton", "Nox", "October", "Odd", "Odder", "Oddman", "Oddson", "Owl", "Owley", "Owlford",
        "Owlsey", "Pale", "Pale", "Paine", "Pains", "Payne", "Plague", "Poe", "Poe", "Poe", "Pyre", "Pyre", "Pyre",
        "Radcliffe", "Rain", "Raven", "Ravencraft", "Ravendale", "Ravenhorst", "Ravensloft", "Ravenway", "Rayne",
        "Reaper", "Redbone", "Redcross", "Redd", "Redfern", "Redgrave", "Redmond", "Redwine", "Redwolf",
        "Renfield", "Riven", "Rookwood", "Roth", "Ripley", "Ripper", "Salvatore", "Scar", "Scare", "Scarebrook",
        "Scares", "Scarey", "Scarlati", "Setzer", "Seward", "Shade", "Shademoore", "Shadow", "Shadows",
        "Shadowton", "Shelley", "Skeleton", "Skelinen", "Skellington", "Skelton", "Skull", "Skullman", "Specter",
        "Spectre", "Spellman", "Spider", "Spinner", "Spirite", "Spook", "Spook", "Spook", "Song", "Snow",
        "St. Claire", "St. Germaine", "Steele", "Sterling", "Stoker", "Storm", "Storme", "Stormfelt", "Stormwind",
        "Stormyr", "Stone", "Stonewall", "Strange", "Strangeman", "Strangeway", "Striker", "Swan", "Swann",
        "Teeth", "Tombs", "Tombstone", "Towers", "Trick", "Valancourt", "Valdemar", "Valentine", "Valentino",
        "Vamper", "Vamplers", "Vampouille", "Vamprine", "Vampyr", "Van Allen", "Van Gogh", "Van Halen",
        "Van Helgen", "Van Helsing", "Voorhees", "Webb", "Weird", "Weird", "West", "Westenra", "White",
        "Whitebone", "Whitemoon", "Whitewing", "Widdowes", "Wild", "Wildblood", "Wilde", "Winchester", "Windgate",
        "Windholm", "Windward", "Wing", "Wingblade", "Wingfield", "Winter", "Winterford", "Winterrose",
        "Winterwood", "Winters", "Witche", "Witcher", "Witchery", "Witchey", "Witchman", "Wither", "Wolf",
        "Wolfen", "Wolfmann", "Wolfram", "Wolfstone", "Wolftooth"
    };

    private WitchNames() {
    }

    /** Um nome sorteado, como o {@code generateWitchName} do original. */
    public static String sorteia(RandomSource sorte) {
        return PRIMEIROS[sorte.nextInt(PRIMEIROS.length)] + " "
                + SOBRENOMES[sorte.nextInt(SOBRENOMES.length)];
    }

    /** Quantos de cada, para as provas. */
    public static int quantosPrimeiros() {
        return PRIMEIROS.length;
    }

    public static int quantosSobrenomes() {
        return SOBRENOMES.length;
    }
}
