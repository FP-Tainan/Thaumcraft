// Os materiais com que um prédio de aldeia é feito, por variante.
//
// O Witchery re-veste a aldeia conforme o bioma — é o que o WorldHandlerVillageDistrict$EventHooks faz, pelos
// eventos GetVillageBlockID e GetVillageBlockMeta do Forge. Ele tem dois ramos, areia e neve; nos outros biomas
// a aldeia fica como o jogo a faz.
//
// No deserto: pedregulho e tora viram ARENITO, e a madeira vira BÉTULA — a tábua é trocada por meta 2, que é a
// de bétula, e por isso as escadas e a laje também são de bétula. O cercado não é trocado, e fica carvalho.
//
// Na neve o original troca tudo por neve e gelo, mas usando blocos DELE — SNOW_STAIRS, SNOW_SLAB_SINGLE,
// PERPETUAL_ICE_FENCE e SNOW_PRESSURE_PLATE —, e nenhum deles está portado. A aldeia de neve fica com o
// material comum, e isso está declarado no PORTE.md.

/** O comum: o que o original usa onde não re-veste nada. */
const COMUM = {
  pedra: ['minecraft:cobblestone', null],
  tabua: ['minecraft:oak_planks', null],
  cerca: ['minecraft:oak_fence',
          { north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' }],
  laje: ['minecraft:oak_slab', { type: 'bottom', waterlogged: 'false' }],
  escada: 'minecraft:oak_stairs',
  escadaPedra: 'minecraft:cobblestone_stairs',
  tora: 'minecraft:oak_log',
  lajePedra: 'minecraft:cobblestone_slab',
};

/** E o do deserto: arenito e bétula. */
const DESERTO = {
  pedra: ['minecraft:sandstone', null],
  tabua: ['minecraft:birch_planks', null],
  cerca: COMUM.cerca,
  laje: ['minecraft:birch_slab', { type: 'bottom', waterlogged: 'false' }],
  escada: 'minecraft:birch_stairs',
  escadaPedra: 'minecraft:sandstone_stairs',
  tora: 'minecraft:sandstone',
  lajePedra: 'minecraft:sandstone_slab',
};

module.exports = { COMUM, DESERTO };
