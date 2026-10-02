// Gera o molde do Forte do Witchery.
//
//   node tools/aldeia/forte.js
//
// O corpo veio do ComponentVillageKeep.addComponentParts traduzido chamada por chamada. Duas coisas do
// original que valem ser ditas aqui:
//
//  - O ajudante dele, fill(x,y,z, largura,altura,fundo), NÃO é o fillWithBlocks do jogo, que vai de canto a
//    canto. Confundir os dois faz um prédio quase certo, e é o tipo de erro que só a foto apanha.
//  - O drawTower(offsetX, flipX) desenha a mesma torre duas vezes, em 0,0 e em 8,4. O segundo número espelha
//    a janela e a viga para o outro lado.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const materiais = require(path.join(__dirname, 'materiais.js'));
const { marca } = nbt;

const LARGURA = 17, ALTURA = 27, FUNDO = 17;   // o (0,0,0 .. 16,26,16) do original
const VERSAO = 4903;

const ESCADA_POR_META = ['east', 'west', 'south', 'north'];
const ESCADA_MAO_POR_META = { 2: 'north', 3: 'south', 4: 'west', 5: 'east' };

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
  const CERCA = bloco(...mat.cerca);
  const TOCHA = bloco('minecraft:torch');
  const TABUA = bloco(...mat.tabua);
  const LAJE = bloco(...mat.laje);
  const VIDRACA = bloco('minecraft:glass_pane', {
    north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' });
  const GRADE = bloco('minecraft:iron_bars', {
    north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' });
  const BAU = bloco('minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' });
  const ENCAIXE = bloco('minecraft:jigsaw', { orientation: 'north_up' });

  // A tora do original leva o eixo na metadata: 0-3 em pé, 4-7 no eixo X, 8-11 no eixo Z.
  const tora = meta => bloco(mat.tora, { axis: meta >= 8 ? 'z' : (meta >= 4 ? 'x' : 'y') });
  const TORA = tora(0);

  const escada = meta => bloco(mat.escada, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });
  const escada_pedra = meta => bloco(mat.escadaPedra, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });
  const escada_mao = meta => bloco('minecraft:ladder', {
    facing: ESCADA_MAO_POR_META[meta], waterlogged: 'false' });

  // A laje de pedra do original: meta 8-15 é a de cima, e o resto do número diz o material.
  const laje_pedra = meta => bloco(mat.lajePedra, {
    type: meta >= 8 ? 'top' : 'bottom', waterlogged: 'false' });

  const pano = new Int32Array(LARGURA * ALTURA * FUNDO).fill(VAZIO);
  const extras = new Map();
  const ind = (x, y, z) => (y * FUNDO + z) * LARGURA + x;

  function ponha(b, x, y, z, dado) {
    if (x < 0 || x >= LARGURA || y < 0 || y >= ALTURA || z < 0 || z >= FUNDO) return;
    pano[ind(x, y, z)] = b;
    if (dado) extras.set(ind(x, y, z), dado);
  }

  function preenche(x1, y1, z1, x2, y2, z2, b) {
    for (let y = y1; y <= y2; y++)
      for (let z = z1; z <= z2; z++)
        for (let x = x1; x <= x2; x++) ponha(b, x, y, z);
  }

  /** O drawTower do original: a mesma torre duas vezes, espelhada pelo segundo número. */
  function desenhaTorre(offsetX, flipX) {
    preenche(3 + offsetX, 0, 1, (3 + offsetX) + (3) - 1, 10, 1, PEDRA);
    preenche(3 + offsetX, 0, 5, (3 + offsetX) + (3) - 1, 10, 5, PEDRA);
    preenche(2 + offsetX, 0, 2, (2 + offsetX) + (1) - 1, 10, 4, PEDRA);
    preenche(6 + offsetX, 0, 2, (6 + offsetX) + (1) - 1, 10, 4, PEDRA);
    preenche(3 + offsetX, 0, 2, (3 + offsetX) + (3) - 1, 0, 4, PEDRA);
    preenche(2 + offsetX, 4, 1, (2 + offsetX) + (5) - 1, 4, 5, PEDRA);
    preenche(2 + offsetX, 9, 1, (2 + offsetX) + (5) - 1, 9, 5, PEDRA);
    ponha(PEDRA, 4 + offsetX, 11, 1);
    ponha(PEDRA, 4 + offsetX, 11, 5);
    ponha(PEDRA, 2 + offsetX, 11, 3);
    ponha(PEDRA, 6 + offsetX, 11, 3);
    preenche(4 + offsetX, 1, 1, (4 + offsetX) + (1) - 1, 3, 1, TORA);
    preenche(2 + offsetX + flipX, 1, 3, (2 + offsetX + flipX) + (1) - 1, 3, 3, TORA);
    preenche(4 + offsetX, 6, 1, (4 + offsetX) + (1) - 1, 7, 1, GRADE);
    preenche(2 + offsetX + flipX, 6, 3, (2 + offsetX + flipX) + (1) - 1, 7, 3, GRADE);
    ponha(tora(8), 4 + offsetX, 7, 5);
    ponha(tora(4), 6 + offsetX - flipX, 7, 3);
    preenche(4 + offsetX, 5, 5, (4 + offsetX) + (1) - 1, 6, 5, AR);
    preenche(4 + offsetX, 1, 5, (4 + offsetX) + (1) - 1, 2, 5, AR);
    preenche(6 + offsetX - flipX, 5, 3, (6 + offsetX - flipX) + (1) - 1, 6, 3, AR);
    // meta = 2
    for (let h = 1; h <= 9; h++) {
    ponha(escada_mao(2), 3 + offsetX, h, 2);
    }
    ponha(TOCHA, 3 + offsetX, 2, 4);
    ponha(TOCHA, 3 + offsetX, 6, 4);
  }

  // ------------------------------------------------------------- o forte, pela ordem do original
  preenche(1, 1, 1, 14, 26, 14, AR);
  desenhaTorre(0, 0);
  desenhaTorre(8, 4);
  preenche(7, 0, 2, 9, 0, 4, PEDRA);
  preenche(7, 4, 3, 9, 4, 4, PEDRA);
  preenche(7, 5, 2, 9, 5, 2, PEDRA);
  ponha(PEDRA, 8, 6, 2);
  // meta = 8
  for (let x = 7; x <= 9; x++) {
  ponha(tora(8), x, 4, 2);
  }
  preenche(7, 3, 3, 9, 3, 3, CERCA);
  ponha(laje_pedra(11), 7, 3, 2);
  ponha(laje_pedra(11), 7, 3, 4);
  ponha(laje_pedra(11), 9, 3, 2);
  ponha(laje_pedra(11), 9, 3, 4);
  // meta = 3
  // meta2 = 2
  for (let var17 = 7; var17 <= 9; var17++) {
  ponha(escada_pedra(3), var17, 0, 1);
  ponha(escada_pedra(2), var17, 0, 4);
  }
  preenche(2, 0, 9, 5, 15, 9, PEDRA);
  preenche(2, 0, 14, 5, 15, 14, PEDRA);
  preenche(1, 0, 10, 1, 15, 13, PEDRA);
  preenche(6, 0, 10, 6, 15, 13, PEDRA);
  preenche(2, 0, 10, 5, 0, 13, PEDRA);
  preenche(1, 4, 9, 6, 4, 14, PEDRA);
  preenche(1, 9, 9, 6, 9, 14, PEDRA);
  preenche(1, 14, 9, 6, 14, 14, PEDRA);
  preenche(3, 16, 9, 4, 16, 9, PEDRA);
  preenche(3, 16, 14, 4, 16, 14, PEDRA);
  preenche(1, 16, 11, 1, 16, 12, PEDRA);
  preenche(6, 16, 11, 6, 16, 12, PEDRA);
  preenche(3, 1, 14, 4, 3, 14, TORA);
  preenche(1, 1, 11, 1, 3, 12, TORA);
  preenche(3, 11, 9, 4, 12, 9, GRADE);
  preenche(3, 6, 14, 4, 7, 14, GRADE);
  preenche(3, 11, 14, 4, 12, 14, GRADE);
  preenche(1, 6, 11, 1, 7, 12, GRADE);
  preenche(1, 11, 11, 1, 12, 12, GRADE);
  preenche(6, 11, 11, 6, 12, 12, GRADE);
  preenche(4, 1, 9, 4, 2, 9, AR);
  preenche(4, 5, 9, 4, 6, 9, AR);
  preenche(6, 1, 11, 6, 2, 11, AR);
  preenche(6, 5, 11, 6, 6, 11, AR);
  ponha(tora(8), 4, 7, 9);
  ponha(tora(4), 6, 7, 11);
  // meta = 2
  for (let h = 1; h <= 14; h++) {
  ponha(escada_mao(2), 2, h, 10);
  }
  ponha(TOCHA, 2, 2, 13);
  ponha(TOCHA, 2, 6, 13);
  ponha(TOCHA, 2, 11, 13);
  preenche(11, 0, 9, 13, 18, 9, PEDRA);
  preenche(11, 0, 13, 13, 18, 13, PEDRA);
  preenche(10, 0, 10, 10, 18, 12, PEDRA);
  preenche(14, 0, 10, 14, 18, 12, PEDRA);
  preenche(11, 0, 10, 13, 0, 12, PEDRA);
  preenche(10, 4, 9, 14, 4, 13, PEDRA);
  preenche(10, 9, 9, 14, 9, 13, PEDRA);
  preenche(10, 14, 9, 14, 14, 13, PEDRA);
  preenche(10, 19, 9, 14, 19, 13, PEDRA);
  preenche(12, 1, 13, 12, 3, 13, TORA);
  preenche(14, 1, 11, 14, 3, 11, TORA);
  preenche(12, 6, 13, 12, 7, 13, GRADE);
  preenche(12, 11, 9, 12, 12, 9, GRADE);
  preenche(12, 16, 9, 12, 17, 9, GRADE);
  preenche(12, 11, 13, 12, 12, 13, GRADE);
  preenche(12, 16, 13, 12, 17, 13, GRADE);
  preenche(14, 6, 11, 14, 7, 11, GRADE);
  preenche(14, 11, 11, 14, 12, 11, GRADE);
  preenche(14, 16, 11, 14, 17, 11, GRADE);
  preenche(10, 11, 11, 10, 12, 11, GRADE);
  preenche(10, 16, 11, 10, 17, 11, GRADE);
  preenche(12, 5, 9, 12, 6, 9, AR);
  preenche(12, 1, 9, 12, 2, 9, AR);
  preenche(10, 5, 11, 10, 6, 11, AR);
  preenche(10, 1, 11, 10, 2, 11, AR);
  ponha(tora(8), 12, 7, 9);
  ponha(tora(4), 10, 7, 11);
  // meta = 2
  for (let h = 1; h <= 14; h++) {
  ponha(escada_mao(2), 11, h, 10);
  }
  ponha(TOCHA, 11, 2, 12);
  ponha(TOCHA, 11, 6, 12);
  ponha(TOCHA, 11, 11, 12);
  ponha(TOCHA, 11, 16, 12);
  ponha(tora(0), 11, 19, 10);
  preenche(10, 20, 9, 14, 21, 13, TABUA);
  preenche(11, 22, 10, 13, 23, 12, TABUA);
  preenche(12, 24, 11, 12, 25, 11, TABUA);
  preenche(11, 20, 10, 13, 21, 12, AR);
  // n = 3
  // s = 2
  // w = 0
  // e = 1
  for (let var18 = 9; var18 <= 15; var18++) {
  ponha(escada(3), var18, 20, 8);
  ponha(escada(2), var18, 20, 14);
  }
  for (let var19 = 10; var19 <= 14; var19++) {
  ponha(escada(3), var19, 22, 9);
  ponha(escada(2), var19, 22, 13);
  }
  for (let var20 = 11; var20 <= 13; var20++) {
  ponha(escada(3), var20, 24, 10);
  ponha(escada(2), var20, 24, 12);
  }
  for (let z = 9; z <= 13; z++) {
  ponha(escada(0), 9, 20, z);
  ponha(escada(1), 15, 20, z);
  }
  for (let var31 = 10; var31 <= 12; var31++) {
  ponha(escada(0), 10, 22, var31);
  ponha(escada(1), 14, 22, var31);
  }
  ponha(escada(0), 11, 24, 11);
  ponha(escada(1), 13, 24, 11);
  preenche(7, 0, 11, 9, 0, 12, PEDRA);
  preenche(7, 4, 11, 9, 4, 11, PEDRA);
  preenche(7, 1, 12, 9, 5, 12, PEDRA);
  ponha(PEDRA, 8, 6, 12);
  preenche(7, 1, 12, 7, 4, 12, TORA);
  ponha(TOCHA, 8, 2, 11);
  preenche(9, 1, 12, 9, 4, 12, TORA);
  // meta = 3
  for (let var21 = 7; var21 <= 9; var21++) {
  ponha(escada_pedra(3), var21, 0, 10);
  }
  ponha(laje_pedra(11), 7, 3, 11);
  ponha(laje_pedra(11), 9, 3, 11);
  preenche(3, 0, 6, 4, 0, 8, PEDRA);
  preenche(4, 4, 6, 4, 4, 8, PEDRA);
  preenche(3, 1, 6, 3, 5, 8, PEDRA);
  ponha(PEDRA, 3, 6, 7);
  preenche(3, 1, 6, 3, 4, 6, TORA);
  ponha(TOCHA, 4, 2, 7);
  preenche(3, 1, 8, 3, 4, 8, TORA);
  // meta = 1
  for (let var32 = 6; var32 <= 8; var32++) {
  ponha(escada_pedra(1), 5, 0, var32);
  }
  ponha(laje_pedra(11), 4, 3, 6);
  ponha(laje_pedra(11), 4, 3, 8);
  preenche(12, 0, 6, 13, 0, 8, PEDRA);
  preenche(12, 4, 6, 12, 4, 8, PEDRA);
  preenche(13, 1, 6, 13, 5, 8, PEDRA);
  ponha(PEDRA, 13, 6, 7);
  preenche(13, 1, 6, 13, 4, 6, TORA);
  ponha(TOCHA, 12, 2, 7);
  preenche(13, 1, 8, 13, 4, 8, TORA);
  // meta = 0
  for (let var33 = 6; var33 <= 8; var33++) {
  ponha(escada_pedra(0), 11, 0, var33);
  }
  ponha(laje_pedra(11), 12, 3, 6);
  ponha(laje_pedra(11), 12, 3, 8);
  // o baú do original, em 13,20,12, com três a oito sorteios
  ponha(BAU, 13, 20, 12, { id: 'minecraft:chest',
                           LootTable: 'thaumcraft:chests/village_keep' });

  // e o encaixe, no portão entre as duas torres
  ponha(ENCAIXE, 8, 0, 0, {
    id: 'minecraft:jigsaw',
    name: 'minecraft:building_entrance',
    target: 'minecraft:building_entrance',
    pool: 'minecraft:village/' + variante + '/streets',
    joint: 'aligned',
    final_state: 'minecraft:air',
  });

  // ---------------------------------------------------------------- os guardas
  // O original chama spawnGuards três vezes — (7,1,7,3), (5,10,4,4) e (13,10,4,5) — e o laço dele é
  // "n <= conta", uma volta a mais. São quatro, cinco e seis: quinze guardas. É o descuido do original,
  // o mesmo da torre de vigia, e vai assim.
  const guardas = [];
  for (const [x, y, z, conta] of [[7, 1, 7, 3], [5, 10, 4, 4], [13, 10, 4, 5]]) {
    for (let i = 0; i <= conta; i++) {
      guardas.push({
        pos: marca.lista(6, [x + 0.5, y, z + 0.5]),
        blockPos: marca.lista(3, [x, y, z]),
        nbt: { id: 'thaumcraft:village_guard', PersistenceRequired: marca.byte(1) },
      });
    }
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

const destino = path.join(__dirname, '..', '..',
    'src', 'main', 'resources', 'data', 'thaumcraft', 'structure', 'village');

const VARIANTES = {
  plains: materiais.COMUM,
  desert: materiais.DESERTO,
  savanna: materiais.COMUM,
  snowy: materiais.COMUM,
  taiga: materiais.COMUM,
};

for (const [variante, mat] of Object.entries(VARIANTES)) {
  const arquivo = path.join(destino, 'keep_' + variante + '.nbt');
  const bytes = nbt.escreveArquivo(arquivo, constrói(mat, variante));
  console.log('keep_' + variante + '.nbt: ' + LARGURA + 'x' + ALTURA + 'x' + FUNDO + ', ' + bytes + ' bytes');
}
