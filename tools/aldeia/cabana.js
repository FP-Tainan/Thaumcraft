// Gera o molde da Cabana da Bruxa.
//
//   node tools/aldeia/cabana.js
//
// O corpo veio do ComponentVillageWitchHut traduzido chamada por chamada. É a menor peça da aldeia e a que diz
// mais: uma casa de aldeia comum por fora, e dentro um caldeirão, uma tora de sorveira, um vaso — e uma bruxa.
//
// AS TRÊS MADEIRAS SÃO DE PROPÓSITO. O original usa tábua comum nas paredes, tábua de metadata 1 no telhado e
// de metadata 2 nos cantos: carvalho, abeto e bétula. É o que faz a cabana destoar de leve das casas à volta
// sem gritar, que é exatamente o que uma casa de bruxa numa aldeia devia fazer.
//
// E O Z ANDA UM, como no Boticário: o original põe o degrau da porta em z = -1, fora da caixa que ele próprio
// declara. Molde não tem coordenada negativa.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const materiais = require(path.join(__dirname, 'materiais.js'));
const { marca } = nbt;

const LARGURA = 5, ALTURA = 7, FUNDO = 7;   // o (0,0,0 .. 4,6,5) do original, mais um em z
const DZ = 1;
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
  // as duas outras madeiras do original: metadata 1 é abeto, metadata 2 é bétula
  const TELHADO = bloco('minecraft:spruce_planks');
  const CANTO = bloco('minecraft:birch_planks');
  const VIDRACA = bloco('minecraft:glass_pane', {
    north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' });
  // o caldeirão do original vem com metadata 3: cheio de água
  const CALDEIRAO = bloco('minecraft:water_cauldron', { level: '3' });
  const TORA = bloco('thaumcraft:rowan_log', { axis: 'y' });
  const VASO = bloco('minecraft:flower_pot');
  const ENCAIXE = bloco('minecraft:jigsaw', { orientation: 'north_up' });
  const PORTA_BAIXO = bloco('minecraft:oak_door', {
    facing: 'north', half: 'lower', hinge: 'left', open: 'false', powered: 'false' });
  const PORTA_CIMA = bloco('minecraft:oak_door', {
    facing: 'north', half: 'upper', hinge: 'left', open: 'false', powered: 'false' });
  const escada_pedra = meta => bloco(mat.escadaPedra, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });

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

  // ---------------------------------------------------------------- a cabana, pela ordem do original

  preenche(1, 1, 1, 3, 5, 4, AR);
  preenche(0, 0, 0, 3, 0, 4, PEDRA);
  preenche(1, 0, 1, 2, 0, 3, PEDRA);

  // o telhado: a casa é sempre a alta, porque o original põe isTallHouse no construtor e nunca mais mexe
  preenche(1, 5, 1, 2, 5, 3, TELHADO);

  for (const [x, z] of [[1, 0], [2, 0], [1, 4], [2, 4]]) ponha(TABUA, x, 4, z);
  for (const z of [1, 2, 3]) { ponha(TABUA, 0, 4, z); ponha(TABUA, 3, 4, z); }

  // os quatro cantos, de bétula
  for (const [x, z] of [[0, 0], [3, 0], [0, 4], [3, 4]]) preenche(x, 1, z, x, 3, z, CANTO);
  for (const [x, z] of [[0, 0], [3, 0], [0, 4], [3, 4]]) ponha(TABUA, x, 3, z);

  preenche(0, 1, 1, 0, 3, 3, TABUA);
  preenche(3, 1, 1, 3, 3, 3, TABUA);
  preenche(1, 1, 0, 2, 3, 0, TABUA);
  preenche(1, 1, 4, 2, 3, 4, TABUA);

  ponha(VIDRACA, 0, 2, 2);
  ponha(VIDRACA, 3, 2, 2);

  // o que faz dela cabana de bruxa
  ponha(CALDEIRAO, 1, 1, 3);
  ponha(TORA, 2, 1, 3);
  ponha(VASO, 2, 2, 3);

  // a porta
  ponha(AR, 1, 1, 0);
  ponha(AR, 1, 2, 0);
  ponha(PORTA_BAIXO, 1, 1, 0);
  ponha(PORTA_CIMA, 1, 2, 0);

  // e o encaixe, no degrau à frente da porta
  ponha(ENCAIXE, 1, 0, -1, {
    id: 'minecraft:jigsaw',
    name: 'minecraft:building_entrance',
    target: 'minecraft:building_entrance',
    pool: 'minecraft:village/' + variante + '/streets',
    joint: 'aligned',
    final_state: mat.escadaPedra + '[facing=north,half=bottom,shape=straight,waterlogged=false]',
  });

  // ---------------------------------------------------------------- quem mora nela
  // O original chama spawnWitches(..., 1, 1, 2, 1): uma bruxa do coven, persistente.
  const moradoras = [{
    pos: marca.lista(6, [1.5, 1.0, 2.5 + DZ]),
    blockPos: marca.lista(3, [1, 1, 2 + DZ]),
    nbt: { id: 'thaumcraft:coven_witch', PersistenceRequired: marca.byte(1) },
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
    entities: marca.lista(10, moradoras),
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
  const arquivo = path.join(destino, 'witch_hut_' + variante + '.nbt');
  const bytes = nbt.escreveArquivo(arquivo, constrói(mat, variante));
  console.log('witch_hut_' + variante + '.nbt: ' + LARGURA + 'x' + ALTURA + 'x' + FUNDO
      + ', ' + bytes + ' bytes');
}
