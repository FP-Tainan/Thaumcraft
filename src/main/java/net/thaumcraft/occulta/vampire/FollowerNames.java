package net.thaumcraft.occulta.vampire;

import net.minecraft.util.RandomSource;

/**
 * Os <b>nomes</b> da Mais Bela de Todas: o {@code generateFollowerName} do {@code EntityFollower}.
 *
 * <p>Ela nasce com nome — e o nome é a coisa toda. Uma estranha a cinquenta blocos do espelho não assusta
 * ninguém; <b>a Rose Appleseed</b> que o espelho diz ser mais bela do que você é outra história. São
 * trezentas e oitenta e duas palavras de conto de fadas, e o original guardou-as à mão.
 *
 * <p>Repare nos sobrenomes: Appleseed, Goldhair, Rapunzel, Gooseberry, Winkie, Grimm. Nenhum é um
 * sobrenome a sério — são todos de rima infantil ou de irmão Grimm, e é de propósito. A Mais Bela não é
 * uma pessoa: é uma <b>personagem de conto</b> que apareceu no mundo porque alguém perguntou a um espelho.
 */
public final class FollowerNames {
    /** Os cento e seis nomes de homem do original. */
    private static final String[] ELES = {
            "Abraham", "Adam", "Adrian", "Alex", "Alexander", "Allen", "Ambrose", "Andrew", "Anthony",
            "Arthur", "Avery", "Barnaby", "Bartholomew", "Benedict", "Benjamin", "Bernard", "Billy", "Bobby",
            "Charles", "Charley", "Christopher", "Colin", "Conrad", "Cuthbert", "Daniel", "Danny", "Davey",
            "David", "Edmund", "Edward", "Francis", "Fred", "Freddy", "Geoffrey", "George", "Georgie",
            "Gerard", "Gilbert", "Giles", "Gregory", "Hans", "Hansel", "Heinrich", "Henry", "Hugh", "Humphrey",
            "Isaac", "Jack", "Jacques", "James", "Jamie", "Jerome", "Jim", "Jimmy", "John", "Johnny", "Joseph",
            "Julian", "Lancelot", "Lawrence", "Leonard", "Lou", "Luke", "Mark", "Martin", "Mathias", "Matthew",
            "Merlin", "Michael", "Miles", "Nat", "Nathan", "Nathaniel", "Ned", "Nicholas", "Oliver", "Oswyn",
            "Patrick", "Paul", "Peter", "Philip", "Piers", "Ralph", "Reynold", "Richard", "Ricky", "Robert",
            "Robin", "Roger", "Rowland", "Samuel", "Simon", "Solomon", "Stephen", "Terence", "Thomas", "Tim",
            "Tobias", "Tom", "Tommy", "Valentine", "Walter", "Wendell", "Will", "William", "Willie"};

    /** E os cento e vinte de mulher. */
    private static final String[] ELAS = {
            "Agnes", "Alice", "Amy", "Anna", "Annabella", "Anne", "Arabella", "Audrey", "Avis", "Barbara",
            "Beatrice", "Becky", "Bella", "Belle", "Bertha", "Bessy", "Betty", "Blanche", "Bo", "Bonny",
            "Bridget", "Catalina", "Catherine", "Cecily", "Charity", "Charlotte", "Christina", "Christine",
            "Cinderella", "Cindy", "Clara", "Clarissa", "Clemence", "Clementine", "Constance", "Daisy",
            "Denise", "Dorothy", "Edith", "Elinor", "Elizabeth", "Ella", "Ellen", "Elsa", "Elsie", "Emma",
            "Eve", "Evelyn", "Fawn", "Flora", "Florence", "Floretta", "Fortune", "Frances", "Frideswide",
            "Gertrude", "Gillian", "Ginger", "Goat", "Goatley", "Goldie", "Grace", "Gretel", "Helen", "Hilda",
            "Hazel", "Isabel", "Jane", "Janet", "Jemima", "Jill", "Joan", "Joyce", "Judith", "Julia", "Juliet",
            "Katherine", "Katie", "Kitty", "Lena", "Lily", "Liza", "Lizzie", "Lucy", "Mabel", "Maggie",
            "Margaret", "Margery", "Maria", "Marion", "Marlene", "Martha", "Mary", "Maud", "Mildred",
            "Millicent", "Molly", "Odette", "Pansy", "Pearl", "Petunia", "Philippa", "Polly", "Rachel",
            "Rapunzel", "Rebecca", "Rose", "Rosie", "Ruth", "Sarah", "Shiela", "Snow", "Susanna", "Susie",
            "Sybil", "Talia", "Thomasina", "Trudy", "Ursula", "Winifred"};

    /** E os cento e cinquenta e seis sobrenomes, que são todos de conto de fadas. */
    private static final String[] DE = {
            "Apple", "Applegreen", "Applerose", "Appleseed", "Appleton", "Applewhite", "Baker", "Barnes",
            "Bean", "Beanblossom", "Beanstock", "Beaste", "Beasten", "Bell", "Berry", "Bird", "Blackbird",
            "Blackwood", "Boar", "Botter", "Bowers", "Bremen", "Brockett", "Buckle", "Butcher", "Candle",
            "Castle", "Castleton", "Cherry", "Cherrytree", "Cherrywood", "Cherrywell", "Cottage", "Daw",
            "Deer", "Dilly", "Dove", "Duck", "Duckfield", "Duckling", "Faery", "Fairy", "Fiddle", "Fiddler",
            "Fisher", "Fitcher", "Flinders", "Friday", "Frogg", "Frogley", "Frost", "Gold", "Goldencrown",
            "Goldhair", "Goodfellow", "Goose", "Gooseberry", "Gosling", "Gray", "Green", "Greengrass",
            "Griggs", "Grimm", "Grundy", "Hare", "Hay", "Hazeltree", "Hickory", "Hood", "Horner", "Hubbard",
            "Hunter", "Korbes", "Lamb", "Lampkin", "Lark", "Locket", "Locks", "MacDonald", "Mack", "Malone",
            "Marsh", "McDiddler", "Meadow", "Meadows", "Merrypips", "Miller", "Mills", "Mockingbird", "Monday",
            "Mouse", "Mouseley", "Muffet", "Mulberry", "Nimble", "Nutt", "O'Hare", "O'Lairy", "Pea",
            "Peartree", "Pease", "Peep", "Pie", "Pigeon", "Pinchme", "Piper", "Porgy", "Porridge", "Pott",
            "Pumpkin", "Pumpkinseed", "Reynard", "River", "Rivers", "Roley", "Rooster", "Roseberry",
            "Rosebottom", "Roseleaf", "Shoe", "Shoemaker", "Shorter", "Silver", "Slipper", "Sprat", "Saturday",
            "Sparrow", "Spindle", "Spindler", "Spinner", "Star", "Stone", "Stonebridge", "Sunday", "Swan",
            "Tailor", "Thatcher", "Thumb", "Thursday", "Toad", "Tower", "Towers", "Trot", "Tucker", "Tuesday",
            "Twist", "Wednesday", "White", "Whittington", "Winkie", "Wolf", "Wolfram", "Wolfson", "Wolfwood",
            "Woodcroft", "Woods"};

    private FollowerNames() {
    }

    /**
     * Um nome, pelo tipo: o quarto tipo é <b>homem</b> e os outros três são mulher, como no original.
     */
    public static String sorteia(int tipo, RandomSource sorte) {
        String[] primeiro = tipo == FollowerEntity.O_HOMEM ? ELES : ELAS;
        return primeiro[sorte.nextInt(primeiro.length)] + " " + DE[sorte.nextInt(DE.length)];
    }
}
