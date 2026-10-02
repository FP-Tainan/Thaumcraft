// Gera o molde do marcador da Muralha.
//
//   node tools/aldeia/muralha.js
//
// A Muralha é a única peça da aldeia que NÃO é um prédio. No original a "peça" dela é um marcador de três por
// oito por três que põe um único bloco — o BlockVillageWallGen —, e quem desenha a muralha é o bloco-entidade
// desse bloco, depois, quando a aldeia já está no chão. O molde daqui é esse marcador, e mais nada.

const path = require('path');
const nbt = require(path.join(__dirname, 'nbt.js'));
const { marca } = nbt;

const LARGURA = 3, ALTURA = 8, FUNDO = 3;   // o (0,0,0 .. 2,7,2) do original
const VERSAO = 4903;

function constrói(variante) {
  const paleta = [
    { Name: 'minecraft:structure_void' },
    { Name: 'thaumcraft:village_wall_gen' },
    { Name: 'minecraft:jigsaw', Properties: { orientation: 'north_up' } },
  ];
  const VAZIO = 0, MARCADOR = 1, ENCAIXE = 2;

  const blocos = [];
  for (let y = 0; y < ALTURA; y++)
    for (let z = 0; z < FUNDO; z++)
      for (let x = 0; x < LARGURA; x++) {
        let estado = VAZIO;
        let dado = null;
        if (x === 1 && y === 1 && z === 1) {
          estado = MARCADOR;
          dado = { id: 'thaumcraft:village_wall_gen' };
        } else if (x === 1 && y === 0 && z === 0) {
          estado = ENCAIXE;
          dado = {
            id: 'minecraft:jigsaw',
            name: 'minecraft:building_entrance',
            target: 'minecraft:building_entrance',
            pool: 'minecraft:village/' + variante + '/streets',
            joint: 'aligned',
            final_state: 'minecraft:air',
          };
        }
        const e = { pos: marca.lista(3, [x, y, z]), state: marca.int(estado) };
        if (dado) e.nbt = dado;
        blocos.push(e);
      }

  return {
    size: marca.lista(3, [LARGURA, ALTURA, FUNDO]),
    entities: marca.lista(10, []),
    blocks: marca.lista(10, blocos),
    palette: marca.lista(10, paleta),
    DataVersion: marca.int(VERSAO),
  };
}

const destino = path.join(__dirname, '..', '..',
    'src', 'main', 'resources', 'data', 'thaumcraft', 'structure', 'village');

for (const variante of ['plains', 'desert', 'savanna', 'snowy', 'taiga']) {
  const arquivo = path.join(destino, 'wall_gen_' + variante + '.nbt');
  const bytes = nbt.escreveArquivo(arquivo, constrói(variante));
  console.log('wall_gen_' + variante + '.nbt: ' + LARGURA + 'x' + ALTURA + 'x' + FUNDO
      + ', ' + bytes + ' bytes');
}
