// Gera o molde do Boticário do Witchery.
//
//   node tools/aldeia/boticario.js
//
// O corpo veio do ComponentVillageApothecary traduzido chamada por chamada.
//
// UM DESLOCAMENTO EM Z, E A RAZÃO DELE. O original declara a caixa dele como (0,0,0 .. 9,9,6) e depois põe a
// placa e o degrau da porta em z = -1, um bloco FORA dessa caixa — no gerador de 2014 isso funcionava, porque
// a conta era feita contra o pedaço de mundo a gerar e não contra a caixa da peça. Um molde não tem
// coordenada negativa, e por isso o molde é um bloco mais fundo e tudo anda um em z. O corpo fica literal
// como o original o escreveu, e quem desloca é o ponha.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const materiais = require(path.join(__dirname, 'materiais.js'));
const { marca } = nbt;

const LARGURA = 10, ALTURA = 10, FUNDO = 8;   // o (0,0,0 .. 9,9,6) do original, mais um em z
const DZ = 1;                                  // e o deslocamento que põe o z = -1 dele no zero daqui
const VERSAO = 4903;

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
  const LAJE = bloco(...mat.laje);
  const TOCHA = bloco('minecraft:torch');
  const VASO = bloco('minecraft:flower_pot');
  const CALDEIRAO = bloco('minecraft:cauldron');
  const BAU = bloco('minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' });
  const ENCAIXE = bloco('minecraft:jigsaw', { orientation: 'north_up' });
  const VIDRACA = bloco('minecraft:glass_pane', {
    north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' });
  const PLACA = bloco('minecraft:oak_wall_sign', { facing: 'north', waterlogged: 'false' });

  const escada = meta => bloco(mat.escada, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });
  const escada_pedra = meta => bloco(mat.escadaPedra, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });
  // A escada do original pode vir de cabeça para baixo, pelo bit 4 da metadata.
  const escada_betula = (meta, deCabecaParaBaixo) => bloco(mat.escadaBetula, {
    facing: ESCADA_POR_META[meta], half: deCabecaParaBaixo ? 'top' : 'bottom',
    shape: 'straight', waterlogged: 'false' });

  const pano = new Int32Array(LARGURA * ALTURA * FUNDO).fill(VAZIO);
  const extras = new Map();
  const ind = (x, y, z) => (y * FUNDO + z) * LARGURA + x;

  function ponha(b, x, y, z, dado) {
    z += DZ;
    if (x < 0 || x >= LARGURA || y < 0 || y >= ALTURA || z < 0 || z >= FUNDO) return;
    pano[ind(x, y, z)] = b;
    if (dado) extras.set(ind(x, y, z), dado);
  }

  function preenche(x1, y1, z1, x2, y2, z2, b) {
    for (let y = y1; y <= y2; y++)
      for (let z = z1; z <= z2; z++)
        for (let x = x1; x <= x2; x++) ponha(b, x, y, z);
  }

  // ------------------------------------------------------------- o boticário, pela ordem do original
  preenche(1, 1, 1, 7, 5, 4, AR);
  preenche(0, 0, 0, 8, 0, 5, PEDRA);
  preenche(0, 5, 0, 8, 5, 5, PEDRA);
  preenche(0, 6, 1, 8, 6, 4, PEDRA);
  preenche(0, 7, 2, 8, 7, 3, PEDRA);
  // i = 3
  // j = 2
  for (let k = -1; k <= 2; k++) {
  for (let l = 0; l <= 8; l++) {
  ponha(escada(3), l, 6 + k, k);
  ponha(escada(2), l, 6 + k, 5 - k);
  }
  }
  preenche(0, 1, 0, 0, 1, 5, PEDRA);
  preenche(1, 1, 5, 8, 1, 5, PEDRA);
  preenche(8, 1, 0, 8, 1, 4, PEDRA);
  preenche(2, 1, 0, 7, 1, 0, PEDRA);
  preenche(0, 2, 0, 0, 4, 0, PEDRA);
  preenche(0, 2, 5, 0, 4, 5, PEDRA);
  preenche(8, 2, 5, 8, 4, 5, PEDRA);
  preenche(8, 2, 0, 8, 4, 0, PEDRA);
  preenche(0, 2, 1, 0, 4, 4, TABUA);
  preenche(1, 2, 5, 7, 4, 5, TABUA);
  preenche(8, 2, 1, 8, 4, 4, TABUA);
  preenche(1, 2, 0, 7, 4, 0, TABUA);
  ponha(VIDRACA, 4, 2, 0);
  ponha(VIDRACA, 5, 2, 0);
  ponha(VIDRACA, 6, 2, 0);
  ponha(VIDRACA, 4, 3, 0);
  ponha(VIDRACA, 5, 3, 0);
  ponha(VIDRACA, 6, 3, 0);
  ponha(VIDRACA, 0, 2, 2);
  ponha(VIDRACA, 0, 2, 3);
  ponha(VIDRACA, 0, 3, 2);
  ponha(VIDRACA, 0, 3, 3);
  ponha(VIDRACA, 8, 2, 2);
  ponha(VIDRACA, 8, 2, 3);
  ponha(VIDRACA, 8, 3, 2);
  ponha(VIDRACA, 8, 3, 3);
  ponha(VIDRACA, 2, 2, 5);
  ponha(VIDRACA, 3, 2, 5);
  ponha(VIDRACA, 5, 2, 5);
  ponha(VIDRACA, 6, 2, 5);
  ponha(VIDRACA, 2, 3, 5);
  ponha(VIDRACA, 3, 3, 5);
  ponha(VIDRACA, 5, 3, 5);
  ponha(VIDRACA, 6, 3, 5);
  preenche(1, 4, 1, 7, 4, 1, TABUA);
  preenche(1, 4, 4, 7, 4, 4, TABUA);
  ponha(CALDEIRAO, 7, 1, 1);
  ponha(escada_betula(3, true), 5, 1, 3);
  ponha(escada_betula(2, true), 5, 1, 1);
  ponha(LAJE, 5, 1, 2);
  ponha(TOCHA, 5, 2, 3);
  ponha(VASO, 5, 2, 1);
  ponha(AR, 1, 1, 0);
  ponha(AR, 1, 2, 0);
  ponha(escada_pedra(3), 1, 0, -1);

  // a porta: o original põe-na em 1,1,0 com a metadata 1, que é o virado ao norte
  const PORTA_BAIXO = bloco('minecraft:oak_door', {
    facing: 'north', half: 'lower', hinge: 'left', open: 'false', powered: 'false' });
  const PORTA_CIMA = bloco('minecraft:oak_door', {
    facing: 'north', half: 'upper', hinge: 'left', open: 'false', powered: 'false' });
  ponha(PORTA_BAIXO, 1, 1, 0);
  ponha(PORTA_CIMA, 1, 2, 0);

  // a placa com o nome da casa, do lado de fora
  ponha(PLACA, 1, 3, -1, { id: 'minecraft:sign', is_waxed: marca.byte(0),
    front_text: { messages: marca.lista(8, ['""', '{"translate":"structure.thaumcraft.apothecary"}',
                                            '""', '""']),
                  color: 'black', has_glowing_text: marca.byte(0) },
    back_text: { messages: marca.lista(8, ['""', '""', '""', '""']),
                 color: 'black', has_glowing_text: marca.byte(0) } });

  // o baú do original, em 7,0,1
  ponha(BAU, 7, 0, 1, { id: 'minecraft:chest',
                        LootTable: 'thaumcraft:chests/village_apothecary' });

  // e o encaixe, à frente da porta: o degrau que o original põe ali é o que fica no lugar dele
  ponha(ENCAIXE, 1, 0, -1, {
    id: 'minecraft:jigsaw',
    name: 'minecraft:building_entrance',
    target: 'minecraft:building_entrance',
    pool: 'minecraft:village/' + variante + '/streets',
    joint: 'aligned',
    final_state: mat.escadaPedra + '[facing=north,half=bottom,shape=straight,waterlogged=false]',
  });

  // ---------------------------------------------------------------- quem mora nela
  // O original chama spawnVillagers(..., 2,1,2, 1) e dá-lhe a profissão dele, o ApothecaryVillagerID 2435.
  // Essa profissão é do Witchery e não está portada — fica um clérigo, que é a do jogo mais perto de quem
  // vende poções. Declarado no PORTE.md.
  const moradores = [{
    pos: marca.lista(6, [2.5, 1.0, 2.5 + DZ]),
    blockPos: marca.lista(3, [2, 1, 2 + DZ]),
    nbt: { id: 'minecraft:villager', PersistenceRequired: marca.byte(1),
           VillagerData: { profession: 'minecraft:cleric', level: marca.int(1),
                           type: 'minecraft:plains' } },
  }];

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
    entities: marca.lista(10, moradores),
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
  const arquivo = path.join(destino, 'apothecary_' + variante + '.nbt');
  const bytes = nbt.escreveArquivo(arquivo, constrói(mat, variante));
  console.log('apothecary_' + variante + '.nbt: ' + LARGURA + 'x' + ALTURA + 'x' + FUNDO
      + ', ' + bytes + ' bytes');
}
