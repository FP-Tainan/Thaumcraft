// Gera o molde da Livraria do Witchery.
//
//   node tools/aldeia/livraria.js
//
// O corpo veio do ComponentVillageBookShop traduzido chamada por chamada. O que ela tem de diferente das
// outras peças são os QUATRO QUADROS na parede do fundo, cada um com um livro da loja.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const materiais = require(path.join(__dirname, 'materiais.js'));
const { marca } = nbt;

const LARGURA = 11, ALTURA = 9, FUNDO = 10;   // o (0,0,0 .. 10,8,9) do original
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
  const CERCA = bloco(...mat.cerca);
  const TOCHA = bloco('minecraft:torch');
  const BAU = bloco('minecraft:chest', { facing: 'north', type: 'single', waterlogged: 'false' });
  const ENCAIXE = bloco('minecraft:jigsaw', { orientation: 'north_up' });

  const escada = meta => bloco(mat.escada, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });
  const escada_pedra = meta => bloco(mat.escadaPedra, {
    facing: ESCADA_POR_META[meta], half: 'bottom', shape: 'straight', waterlogged: 'false' });

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

  // ------------------------------------------------------------- a livraria, pela ordem do original
  preenche(1, 0, 1, 8, 0, 6, PEDRA);
  preenche(2, 0, 2, 7, 0, 5, TABUA);
  preenche(1, 1, 0, 8, 7, 6, AR);
  preenche(1, 1, 3, 8, 5, 6, PEDRA);
  preenche(1, 6, 4, 8, 6, 5, PEDRA);
  preenche(1, 4, 1, 8, 4, 2, PEDRA);
  preenche(2, 1, 4, 7, 4, 5, AR);
  preenche(2, 1, 3, 7, 3, 3, TABUA);
  preenche(3, 2, 3, 6, 3, 3, AR);
  ponha(AR, 6, 1, 3);
  ponha(TOCHA, 3, 4, 4);
  ponha(TOCHA, 6, 4, 4);
  preenche(1, 2, 4, 1, 4, 5, TABUA);
  preenche(8, 2, 4, 8, 4, 5, TABUA);
  preenche(2, 2, 6, 7, 4, 6, TABUA);
  preenche(1, 1, 1, 1, 3, 1, CERCA);
  preenche(8, 1, 1, 8, 3, 1, CERCA);
  // n = 3
  // s = 2
  // w = 0
  // e = 1
  ponha(escada_pedra(3), 3, 0, 0);
  ponha(escada_pedra(3), 4, 0, 0);
  ponha(escada_pedra(3), 5, 0, 0);
  ponha(escada_pedra(3), 6, 0, 0);
  for (let i = 1; i <= 8; i++) {
  ponha(escada(3), i, 5, 2);
  ponha(escada(3), i, 6, 3);
  ponha(escada(3), i, 7, 4);
  ponha(escada(2), i, 5, 7);
  ponha(escada(2), i, 6, 6);
  ponha(escada(2), i, 7, 5);
  }

  // o baú da loja, em 2,1,4
  ponha(BAU, 2, 1, 4, { id: 'minecraft:chest',
                        LootTable: 'thaumcraft:chests/village_bookshop' });

  // e o encaixe, na soleira que o original calça de degraus
  ponha(ENCAIXE, 4, 0, 0, {
    id: 'minecraft:jigsaw',
    name: 'minecraft:building_entrance',
    target: 'minecraft:building_entrance',
    pool: 'minecraft:village/' + variante + '/streets',
    joint: 'aligned',
    final_state: mat.escadaPedra + '[facing=north,half=bottom,shape=straight,waterlogged=false]',
  });

  // ---------------------------------------------------------------- os quatro quadros
  // O original pendura quatro quadros em 3..6, 3, 6 e põe em cada um um livro SORTEADO da tabela da loja.
  //
  // E AQUI ELES ANDAM UM BLOCO. No 1.7.10 a posição de um quadro é o bloco em que ele se PRENDE — o 6 do
  // original é a parede —; hoje é o bloco que ele OCUPA, com a parede atrás. Posto em 6 ele nasce dentro da
  // madeira e não se vê. Fica em 5, que é o ar à frente da mesma parede, virado para dentro da loja.
  // Um molde é estático e não sorteia: a escolha fica fixa, e o do meio leva o Thaumonomicon — que é o que o
  // original põe ali quando o Thaumcraft está instalado, e aqui os dois são o mesmo mod. Declarado no PORTE.md.
  const LIVROS = ['minecraft:book', 'thaumcraft:thaumonomicon',
                  'minecraft:book', 'minecraft:writable_book'];
  const quadros = [];
  for (let i = 0; i < 4; i++) {
    const x = 3 + i;
    quadros.push({
      pos: marca.lista(6, [x + 0.5, 3.5, 5.5]),
      blockPos: marca.lista(3, [x, 3, 5]),
      // As chaves são as do jogo de hoje, e não as de 2014: Facing com F grande, escrito pelo
      // Direction.LEGACY_ID_CODEC (2 é o norte), e block_pos como vetor de inteiros — e não o
      // TileX/TileY/TileZ de então. Com as chaves erradas o quadro simplesmente não nasce, calado.
      nbt: {
        id: 'minecraft:item_frame',
        block_pos: marca.ints([x, 3, 5]),
        Facing: marca.byte(2),
        Invisible: marca.byte(0), Fixed: marca.byte(0),
        ItemRotation: marca.byte(0), ItemDropChance: 1.0,
        Item: { id: LIVROS[i], count: marca.int(1) },
      },
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
    entities: marca.lista(10, quadros),
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
  const arquivo = path.join(destino, 'bookshop_' + variante + '.nbt');
  const bytes = nbt.escreveArquivo(arquivo, constrói(mat, variante));
  console.log('bookshop_' + variante + '.nbt: ' + LARGURA + 'x' + ALTURA + 'x' + FUNDO
      + ', ' + bytes + ' bytes');
}
