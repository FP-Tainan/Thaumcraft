// Gera o molde da Torre de Vigia do Witchery.
//
//   node tools/aldeia/torre.js
//
// O original constrói a torre bloco a bloco em código (ComponentVillageWatchTower.addComponentParts). A aldeia
// do 26.2 é um salto-de-encaixe e come moldes .nbt, e por isso a tradução é esta: as mesmas chamadas, pela mesma
// ordem, escritas aqui e assadas num molde.
//
// Duas regras que fazem a tradução ser fiel:
//  1. O que o original NÃO toca vira structure_void, e não ar. Um molde cheio de ar arrasaria o terreno e as
//     peças vizinhas; o original só punha os blocos que punha.
//  2. O que o original põe COMO ar continua ar — é o vazio que ele cava de propósito, as frestas e as portas.
//
// E o giro não se traduz: o getMetadataWithOffset do original girava a peça conforme a orientação dela, e aqui
// quem gira o molde é o salto-de-encaixe. O molde sai sempre na orientação base.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const materiais = require(path.join(__dirname, 'materiais.js'));
const { marca } = nbt;

const LARGURA = 9, ALTURA = 24, FUNDO = 9;   // o (0,0,0 .. 8,23,8) do original
const VERSAO = 4903;                          // o DataVersion do 26.2

/** As escadas vêm por número no 1.7.10 — 0 leste, 1 oeste, 2 sul, 3 norte —, e o facing de hoje nasceu dele. */
const ESCADA_POR_META = ['east', 'west', 'south', 'north'];

function constrói(mat, variante) {
  const paleta = [];
  const porChave = new Map();
  function bloco(nome, props) {
    const chave = nome + (props ? JSON.stringify(props) : '');
    if (porChave.has(chave)) return porChave.get(chave);
    paleta.push(props ? { Name: nome, Properties: props } : { Name: nome });
    porChave.set(chave, paleta.length - 1);
    return paleta.length - 1;
  }

  const VAZIO = bloco('minecraft:structure_void');
  const AR = bloco('minecraft:air');
  const PEDRA = bloco(...mat.pedra);
  const TABUA = bloco(...mat.tabua);
  const CERCA = bloco(...mat.cerca);
  const LAJE = bloco(...mat.laje);
  const TOCHA = bloco('minecraft:torch');
  const ESCADA_MAO = bloco('minecraft:ladder', { facing: 'south', waterlogged: 'false' });
  const BAU = bloco('minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' });
  const ENCAIXE = bloco('minecraft:jigsaw', { orientation: 'north_up' });
  const escada = meta => bloco(mat.escada, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });

  const pano = new Int32Array(LARGURA * ALTURA * FUNDO).fill(VAZIO);
  const extras = new Map();
  const ind = (x, y, z) => (y * FUNDO + z) * LARGURA + x;

  function ponha(b, x, y, z, dado) {
    if (x < 0 || x >= LARGURA || y < 0 || y >= ALTURA || z < 0 || z >= FUNDO) return;
    pano[ind(x, y, z)] = b;
    if (dado) extras.set(ind(x, y, z), dado);
  }

  /** O fillWithBlocks do original: caixa cheia, extremos inclusive. */
  function preenche(x1, y1, z1, x2, y2, z2, b) {
    for (let y = y1; y <= y2; y++)
      for (let z = z1; z <= z2; z++)
        for (let x = x1; x <= x2; x++) ponha(b, x, y, z);
  }

  // ---------------------------------------------------------------- a torre, pela ordem do original

  preenche(2, 0, 2, 6, 17, 6, PEDRA);
  preenche(3, 13, 3, 5, 14, 5, AR);
  preenche(2, 16, 3, 6, 17, 5, AR);
  preenche(3, 16, 2, 5, 17, 6, AR);
  preenche(3, 15, 1, 5, 16, 1, PEDRA);
  preenche(4, 14, 1, 4, 17, 1, PEDRA);
  preenche(3, 15, 7, 5, 16, 7, PEDRA);
  preenche(4, 14, 7, 4, 17, 7, PEDRA);
  preenche(1, 15, 3, 1, 16, 5, PEDRA);
  preenche(1, 14, 4, 1, 17, 4, PEDRA);
  preenche(7, 15, 3, 7, 16, 5, PEDRA);
  preenche(7, 14, 4, 7, 17, 4, PEDRA);

  for (const [x, z] of [[2, 2], [2, 6], [6, 2], [6, 6]]) ponha(CERCA, x, 18, z);

  preenche(2, 19, 2, 6, 19, 6, TABUA);
  preenche(3, 20, 3, 5, 20, 5, TABUA);
  ponha(TABUA, 4, 19, 4);

  const N = escada(3), S = escada(2), O = escada(0), L = escada(1);
  for (const x of [2, 3, 4, 5, 6]) { ponha(N, x, 19, 1); ponha(N, x, 20, 2); }
  for (const x of [3, 4, 5]) ponha(N, x, 21, 3);
  for (const x of [2, 3, 4, 5, 6]) { ponha(S, x, 19, 7); ponha(S, x, 20, 6); }
  for (const x of [3, 4, 5]) ponha(S, x, 21, 5);
  for (const z of [2, 3, 4, 5, 6]) { ponha(O, 1, 19, z); ponha(O, 2, 20, z); }
  for (const z of [3, 4, 5]) ponha(O, 3, 21, z);
  for (const z of [2, 3, 4, 5, 6]) { ponha(L, 7, 19, z); ponha(L, 6, 20, z); }
  for (const z of [3, 4, 5]) ponha(L, 5, 21, z);

  ponha(LAJE, 4, 22, 4);

  preenche(4, 1, 2, 4, 2, 3, AR);
  ponha(TOCHA, 3, 2, 4); ponha(TOCHA, 5, 2, 4);
  ponha(TOCHA, 4, 14, 3); ponha(TOCHA, 4, 16, 4);

  preenche(2, 6, 2, 2, 14, 2, AR);
  preenche(6, 6, 2, 6, 14, 2, AR);
  preenche(6, 6, 6, 6, 14, 6, AR);
  preenche(2, 6, 6, 2, 14, 6, AR);
  preenche(4, 6, 2, 4, 12, 2, AR);
  preenche(4, 6, 6, 4, 12, 6, AR);
  preenche(6, 6, 4, 6, 12, 4, AR);
  preenche(2, 6, 4, 2, 12, 4, AR);
  preenche(2, 9, 2, 6, 9, 6, PEDRA);

  preenche(3, 0, 1, 5, 4, 1, PEDRA);
  preenche(4, 1, 1, 4, 3, 1, AR);
  preenche(3, 0, 7, 5, 4, 7, PEDRA);
  preenche(4, 1, 7, 4, 3, 7, AR);
  preenche(1, 0, 3, 1, 4, 5, PEDRA);
  preenche(1, 1, 4, 1, 3, 4, AR);
  preenche(7, 0, 3, 7, 4, 5, PEDRA);
  preenche(7, 1, 4, 7, 3, 4, AR);

  for (let i = 1; i <= 12; i++) ponha(ESCADA_MAO, 4, i, 4);
  for (let i = 13; i <= 15; i++) ponha(ESCADA_MAO, 3, i, 5);

  ponha(BAU, 5, 13, 5, { id: 'minecraft:chest',
                         LootTable: 'thaumcraft:chests/village_watchtower' });

  // e o encaixe, que é o que prende a torre à rua
  ponha(ENCAIXE, 4, 0, 0, {
    id: 'minecraft:jigsaw',
    name: 'minecraft:building_entrance',
    target: 'minecraft:building_entrance',
    pool: 'minecraft:village/' + variante + '/streets',
    joint: 'aligned',
    final_state: 'minecraft:air',
  });

  // ---------------------------------------------------------------- os guardas
  // O original chama spawnGuards(..., 4, 16, 4, 3) e o laço dele é "for (n = 0; n <= 3; n++)" — quatro
  // guardas, e não três. É um descuido do original, e vai assim.
  const guardas = [];
  for (let i = 0; i < 4; i++) {
    guardas.push({
      pos: marca.lista(6, [4.5, 16.0, 4.5]),
      blockPos: marca.lista(3, [4, 16, 4]),
      nbt: { id: 'thaumcraft:village_guard', PersistenceRequired: marca.byte(1) },
    });
  }

  const blocos = [];
  for (let y = 0; y < ALTURA; y++)
    for (let z = 0; z < FUNDO; z++)
      for (let x = 0; x < LARGURA; x++) {
        const i = ind(x, y, z);
        const e = { pos: marca.lista(3, [x, y, z]), state: marca.int(pano[i]) };
        const dado = extras.get(i);
        if (dado) e.nbt = dado;
        blocos.push(e);
      }

  return {
    size: marca.lista(3, [LARGURA, ALTURA, FUNDO]),
    entities: marca.lista(10, guardas),
    blocks: marca.lista(10, blocos),
    palette: marca.lista(10, paleta),
    DataVersion: marca.int(VERSAO),
  };
}

const destino = path.join(__dirname, "..", "..",
    "src", "main", "resources", "data", "thaumcraft", "structure", "village");

// Um molde por variante: o material muda no deserto, e o encaixe aponta sempre à rua da sua aldeia.
const VARIANTES = {
  plains: materiais.COMUM,
  desert: materiais.DESERTO,
  savanna: materiais.COMUM,
  snowy: materiais.COMUM,
  taiga: materiais.COMUM,
};

for (const [variante, mat] of Object.entries(VARIANTES)) {
  const arquivo = path.join(destino, "watchtower_" + variante + ".nbt");
  const bytes = nbt.escreveArquivo(arquivo, constrói(mat, variante));
  console.log("watchtower_" + variante + ".nbt: " + LARGURA + "x" + ALTURA + "x" + FUNDO + ", " + bytes + " bytes");
}
