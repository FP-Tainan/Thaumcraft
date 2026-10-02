// Gera o molde do COVEN DO PÂNTANO.
//
//   node tools/aldeia/coven.js
//
// ESTA PEÇA NÃO É PORTE. Ela não existe no Witchery: é um acréscimo, e vai marcada como tal no PORTE.md.
//
// O que ela é, e o que ela não é. NÃO é uma aldeia: aldeia no jogo de hoje traz aldeão, sino, troca, cama e
// incursão, e com isso a bruxa viraria vendedora e o Caçador de Bruxas perderia o sentido. É um COVEN —
// quatro cabanas numa clareira fundo no pântano, sem estrada e sem sino, com o terreiro de ritual no meio.
//
// O terreiro leva o coração do círculo e o ANEL DE DENTRO completo, dezesseis glifos de giz de Ritual. É o que
// faz aquilo ler como chão de ritual e ainda serve para os ritos simples; os três anéis seriam oitenta e
// quatro glifos, e isso já é um depósito de giz e não um cenário.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const { marca } = nbt;

const LARGURA = 29, ALTURA = 10, FUNDO = 29;
const VERSAO = 4903;
const MEIO = 14;

// O desenho do original, só o anel de dentro: as linhas 5 a 11 do padrão de dezessete por dezessete.
const ANEL_DE_DENTRO = [
  '..aaa..',
  '.a...a.',
  'a.....a',
  'a.....a',
  'a.....a',
  '.a...a.',
  '..aaa..',
];

function constrói() {
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
  const TERRA = bloco('minecraft:podzol', { snowy: 'false' });
  const PEDRA = bloco('minecraft:mossy_cobblestone');
  const TABUA = bloco('minecraft:spruce_planks');
  const TELHADO = bloco('minecraft:dark_oak_planks');
  const CANTO = bloco('minecraft:spruce_log', { axis: 'y' });
  const VIDRACA = bloco('minecraft:glass_pane', {
    north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' });
  const CALDEIRAO = bloco('minecraft:water_cauldron', { level: '3' });
  const TORA = bloco('thaumcraft:rowan_log', { axis: 'y' });
  const CORACAO = bloco('thaumcraft:circle_heart');
  const GLIFO = bloco('thaumcraft:ritual_glyph');
  const TOCHA = bloco('minecraft:torch');

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

  // ---------------------------------------------------------------- a clareira
  // Chão de podzol e um palmo de ar por cima: é o que abre o mato do pântano sem arrasar o terreno à volta.
  preenche(2, 0, 2, LARGURA - 3, 0, FUNDO - 3, TERRA);
  preenche(2, 1, 2, LARGURA - 3, 3, FUNDO - 3, AR);

  // ---------------------------------------------------------------- o terreiro, no meio
  ponha(CORACAO, MEIO, 1, MEIO);
  for (let l = 0; l < ANEL_DE_DENTRO.length; l++) {
    for (let c = 0; c < ANEL_DE_DENTRO[l].length; c++) {
      if (ANEL_DE_DENTRO[l][c] !== 'a') continue;
      ponha(GLIFO, MEIO - 3 + c, 1, MEIO - 3 + l);
    }
  }

  // ---------------------------------------------------------------- as quatro cabanas
  // A mesma cabana nos quatro cantos da clareira, cada uma virada para o terreiro. É de propósito que sejam
  // iguais: um coven é uma gente só, e quatro casas iguais à volta de um círculo leem como um lugar.
  function cabana(x0, z0) {
    preenche(x0, 1, z0, x0 + 4, 1, z0 + 4, PEDRA);
    preenche(x0, 2, z0, x0 + 4, 4, z0 + 4, TABUA);
    preenche(x0 + 1, 2, z0 + 1, x0 + 3, 4, z0 + 3, AR);
    preenche(x0, 5, z0, x0 + 4, 5, z0 + 4, TELHADO);
    for (const [dx, dz] of [[0, 0], [4, 0], [0, 4], [4, 4]]) {
      preenche(x0 + dx, 2, z0 + dz, x0 + dx, 4, z0 + dz, CANTO);
    }
    ponha(VIDRACA, x0 + 2, 3, z0);
    ponha(VIDRACA, x0 + 2, 3, z0 + 4);
    ponha(VIDRACA, x0, 3, z0 + 2);
    ponha(VIDRACA, x0 + 4, 3, z0 + 2);
    // a porta é um vão: quem mora aqui não tranca nada
    ponha(AR, x0 + 2, 2, z0 + (z0 < MEIO ? 4 : 0));
    ponha(AR, x0 + 2, 3, z0 + (z0 < MEIO ? 4 : 0));
    ponha(CALDEIRAO, x0 + 1, 2, z0 + 1);
    ponha(TORA, x0 + 3, 2, z0 + 3);
    ponha(TOCHA, x0 + 2, 3, z0 + 2);
  }

  const CANTOS = [[3, 3], [21, 3], [3, 21], [21, 21]];
  for (const [x0, z0] of CANTOS) cabana(x0, z0);

  // ---------------------------------------------------------------- quem mora nele
  // Uma bruxa por cabana. O original não tem coven nenhum no mundo — quem dá o número é este acréscimo, e
  // quatro é o que cabe nas quatro casas sem a clareira parecer um acampamento.
  const bruxas = CANTOS.map(([x0, z0]) => ({
    pos: marca.lista(6, [x0 + 2.5, 2.0, z0 + 2.5]),
    blockPos: marca.lista(3, [x0 + 2, 2, z0 + 2]),
    nbt: { id: 'thaumcraft:coven_witch', PersistenceRequired: marca.byte(1) },
  }));

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
    entities: marca.lista(10, bruxas),
    blocks: marca.lista(10, blocos),
    palette: marca.lista(10, paleta),
    DataVersion: marca.int(VERSAO),
  };
}

const destino = path.join(__dirname, '..', '..',
    'src', 'main', 'resources', 'data', 'thaumcraft', 'structure');
const arquivo = path.join(destino, 'swamp_coven.nbt');
const bytes = nbt.escreveArquivo(arquivo, constrói());
console.log('swamp_coven.nbt: ' + LARGURA + 'x' + ALTURA + 'x' + FUNDO + ', ' + bytes + ' bytes');
