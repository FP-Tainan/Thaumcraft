# Porte do Thaumcraft 4.2.3.5 para o Minecraft 26.2

## A regra

Uma versão só: a **4.2.3.5**, de Azanor, para Minecraft 1.7.10. Nada da 5 nem da 6 entra aqui.

O mod original está em `Mod Base/`, já aberto (`Thaumcraft-1.7.10-4.2.3.5/`) e em jar. Ele é código
compilado: 936 arquivos `.class`, nenhum `.java`. Para as contas e as tabelas, a planta legível é a mesma
versão publicada em <https://github.com/0FL01/Thaumcraft-4.2-FOREVA>.

O que estiver aqui tem de ser o que estava lá: mesmos nomes, mesmas cores, mesmas contas, mesma arte. O que
muda é só a língua que o jogo fala hoje — bloco virou `BlockState`, item virou componente, textura virou
modelo, tela virou `Screen`.

## As fatias

| # | fatia | estado |
|---|---|---|
| 1 | Aspectos: a tabela dos 48, a lista com quantidade, os símbolos | **pronta** |
| 2 | Tradução para português, do `pt_BR.lang` do próprio mod | **pronta** |
| 3 | Thaumômetro e pesquisa: escanear, pontos, o caderno, o tabuleiro | **escaneamento pronto**; caderno e tabuleiro a fazer |
| 4 | Varinhas, nodes e vis | **nós, vis e varinhas prontos**; focos a fazer |
| 5 | Alquimia: crisol, essência, frascos, jarros, alambique | **crisol pronto**; frascos e alambique a fazer |
| 6 | Infusão: matriz, pedestais, instabilidade | a fazer |
| 7 | Golens | a fazer |
| 8 | O resto: mácula, criaturas, eldritch, artifícios | a fazer |

## Fatia 1 — aspectos

- `api/aspects/Aspect` — um aspecto: nome em latim, cor, o par que o forma, mistura de tela e símbolo.
- `api/aspects/Aspects` — a tabela dos 48, **gerada a partir da fonte do original**, não digitada à mão.
  Seis primários (aer, terra, ignis, aqua, ordo, perditio) e quarenta e dois compostos.
- `api/aspects/AspectList` — guardar, juntar, tirar; `merge` guarda o maior, que é como o thaumômetro anota.
- Os 50 símbolos vieram do `assets/thaumcraft/textures/aspects` do próprio mod.
- Os nomes saíram do `en_US.lang` e do `pt_BR.lang` do mod, convertidos para o formato de hoje.

Os testes em `src/gametest` são a cerca: conferem a contagem, uma amostra de cores e pares contra a fonte
da 4.2.3.5, e a conta de somar e tirar. Se alguém mexer na tabela, o build para.

## Fatia 2 — a tradução

O `pt_BR.lang` que vem no mod original cobre parte das coisas e para por aí — das 514 linhas de pesquisa,
ele traduziu 321. O resto foi feito aqui: 79 nomes de pesquisa, 117 subtítulos e as 175 páginas de texto
do Thaumonomicon.

A escolha de palavra segue a que o próprio mod já tinha feito em português, para não haver duas línguas
dentro do mesmo livro: **vis** fica vis, **taumaturgia**, **táumio**, **essência**, **mácula** para taint,
**nó** para node, **distorção** para warp, **fluxo** para flux.

O que continua em inglês é o que tem de continuar: os nomes em latim dos aspectos (Aer, Terra, Ignis...),
que são iguais nos dois idiomas, e os nomes próprios do mod — Thaumcraft, Thaumonomicon, Alumentum, Nitor,
Greatwood, Silverwood.

## Fatia 3 — thaumômetro e exame

- `api/aspects/ObjectAspects` — de que cada coisa do jogo é feita. **Gerada a partir do `ConfigAspects`
  do mod original**: 99 anotações do jogo base. Os nomes que o Minecraft mudou desde 2014 passam por um
  mapa (`GRASS` virou `grass_block`, `RECORD_FAR` virou `music_disc_far`, água e lava correntes viraram o
  mesmo bloco). Água e lava não são item hoje, então a tabela guarda bloco também.
- `research/PlayerKnowledge` — o que o jogador descobriu, quantos pontos tem, o que já examinou e que
  pesquisas concluiu. As contas de ganho são as do original: descobrir de primeira rende dois a mais, no
  teto (cem) o ganho vira a raiz, e bem acima dele vira um. Anda junto do jogador em vez do arquivo
  `.thaum` por nome, que no original fazia a pesquisa sumir quando a pessoa trocava de nome.
- `research/ScanManager` — as três regras do exame: só se lê o que se tem cabeça para entender (de todo
  aspecto composto é preciso conhecer os dois de que ele nasce), cada coisa rende ponto uma vez só, e o
  aparelho diz qual aspecto está faltando quando recusa.
- `item/ThaumometerItem` — o botão apertado, com o exame terminando faltando cinco tiques e a mira tendo
  de ficar na mesma coisa o tempo todo. **Única diferença de propósito em relação ao original**: lá o exame
  leva um segundo (vinte e cinco tiques), aqui leva dois (quarenta e cinco), a pedido — um segundo passava
  rápido demais para o peso que o aparelho tem. A mira enxerga líquido e coisa caída no chão, que valem
  pelo item que são, como na 4.2.3.5.
- `client/render/ScannerRenderer` — o aparelho é a peça de três dimensões do mod (`scanner.obj` mais
  `scanner.png` e o vidro `scanscreen.png`). Na 4.2.3.5 ele **nunca teve desenho chapado**.
- `mixin/ItemInHandRendererMixin` — os braços. O jogo de hoje só desenha braço para quem está de mão
  vazia; com item na mão desenha só o item. No original o thaumômetro erguido aparece entre as duas mãos,
  que sobem pelos cantos e seguram a moldura (o `renderFirstPersonArms` do `ItemThaumometerRenderer`), e
  abaixado tem o braço que o segura. É o que este remendo devolve.

- `research/Researches` — a árvore inteira do Thaumonomicon: 201 pesquisas em seis abas. **Gerada a
  partir dos `ConfigResearch*.java` do mod original** pelo `scratchpad/fatia3-pesquisas.js`; cada posição,
  ligação, aspecto e marca é cópia do que o mod registra. Os gametests seguram os números.
- `research/ResearchCategories` e `research/ResearchManager` — as seis abas e as três perguntas do mapa:
  já sei isto, posso abrir agora, isto sequer aparece. A aba dos Eldritch só se abre depois do
  `ELDRITCHMINOR`, como no original.
- `client/gui/ThaumonomiconScreen` — o mapa: painel de 256 por 230, miolo de 224 por 196, casas de 24 e as
  molduras todas da mesma folha (`gui_research.png`, fileira de baixo). O pergaminho do fundo desliza junto
  com o arrasto, e as linhas ficam verdes quando o caminho está aberto e azuis quando ainda falta.
- `client/gui/ResearchPageScreen` — a folha aberta, de 256 por 181, com o texto vindo dos arquivos de
  idioma do próprio mod e as marcas dele (`<BR>`, `<LINE>`).
- `item/ThaumonomiconItem` — o livro.

## Fatia 6 — matéria-prima, ferramentas e armaduras

- `registry/TCResources` — **gerada** pelo `scratchpad/fatia6-recursos.js`: as treze matérias-primas do
  mod que só existem para entrar em receita (táumio, mercúrio, sebo, âmbar, tecido encantado, salis mundus
  e o resto). No original tudo isso é um item só com números diferentes; aqui cada um é um item, que é como
  o jogo de hoje faz.
- `item/TCMaterials` — de que são feitas as ferramentas, com os números do `ThaumcraftApi` original:
  táumio com 400 de uso, 7 de velocidade e 22 de encantabilidade; metal do vazio cortando mais e durando
  menos. As armaduras seguem os mesmos 2/5/6/2 e 3/6/7/3 do original.
- `registry/TCGear` — **gerada** pelo `scratchpad/fatia6-ferramentas.js`: as dezoito peças de táumio e de
  metal do vazio, com as texturas do mod e as receitas de bancada do `ConfigRecipesSpecialSlice`.
- **O minério infundido** — a pedra de onde os fragmentos saem, uma por aspecto primordial. As texturas são
  montadas pelo `scratchpad/Infuso.java` a partir das duas do original (a pedra de fundo e a veia animada),
  tingindo a veia com a cor do aspecto, que é o que o mod faz na hora de desenhar. Nasce em veios pelo
  subsolo e larga o fragmento quando se quebra com picareta.
- `crafting/ArcaneRecipe` e `crafting/ArcaneRecipes` — a receita de bancada arcana, que cobra vis da
  varinha. **Gerada** pelo `scratchpad/fatia6-arcanas.js`; por enquanto só uma fecha, porque as outras
  sessenta e sete usam blocos do mod que ainda não existem. A bancada em si chega junto com eles.

## Fatia 5 — alquimia

- `block/CrucibleBlock` e `block/entity/CrucibleBlockEntity` — o crisol. As contas são as do original:
  esquenta um grau por tique enquanto houver fogo por baixo, até duzentos, e só ferve passando de cento e
  cinquenta. Fervendo, o que cai dentro se desfaz nos aspectos que tem; o que não é feito de nada ele
  cospe de volta.
- `crafting/CrucibleRecipe` e `crafting/CrucibleRecipes` — **gerada** pelo
  `scratchpad/fatia5-crisol.js` a partir do `ConfigRecipesCrucibleSlice` do mod. Dezesseis receitas por
  enquanto: as que fecham com coisas que já existem por aqui. As outras estão anotadas na saída do gerador
  e entram conforme as fatias trouxerem as peças. Uma não tem como voltar: a duplicação de corante, porque
  o corante genérico de 2014 virou dezesseis itens.
- `client/render/CrucibleRenderer` — a água na cor do que está dissolvido, subindo um dedo e tremendo
  quando ferve.
- `item/WandTriggers` — o caldeirão vira crisol com um toque de varinha, como no original.

## Fatia 4 — varinhas, nós e vis

- `api/nodes/NodeType` e `NodeModifier` — os seis tipos e os três feitios do original.
- `block/NodeBlock` e `block/entity/NodeBlockEntity` — o nó é uma bolha de magia parada no ar: sem face
  para desenhar, sem segurar quem passa, com um miolo de meia casa só para a mira pegar. Ele devolve um
  ponto a um aspecto faltante de tempos em tempos, e a pressa é o feitio dele — seiscentos tiques no
  comum, quatrocentos no brilhante, novecentos no pálido, e o esmaecido não se refaz nunca mais.
- `world/NodeFeature` — como um nó nasce: um em dezoito sai de tipo fora do comum, um em nove ganha
  feitio, e o tamanho sai da aura da terra. Um nó a cada trinta e seis pedaços de mundo, como no original.
- `world/BiomeAura` — **gerada** pelo `scratchpad/fatia4-aura.js` a partir da tabela do `Config` do mod.
  Uma coisa não teve como ser igual: o original usava o dicionário de biomas do Forge, com marcas como
  WET, HOT, DENSE e MAGICAL que o Minecraft de hoje não tem. Ficaram as doze que sobreviveram, com os
  números do original; terra sem marca vale cem, que é o que o original devolvia quando não reconhecia.
- `client/render/NodeRenderer` — a nuvem de bolhas: uma por aspecto, na cor dele, saindo da folha de
  trinta e dois quadros do próprio mod (`misc/nodes.png`). Cada bolha respira num compasso próprio e
  cresce com o quanto o nó guarda; o esmaecido pisca como quem está para se apagar.
- `api/wands/WandParts` — **gerada** pelo `scratchpad/fatia4-varinhas.js` a partir do `Thaumcraft.java`
  do mod: quatro pontas, nove hastes de varinha e nove de bastão, com capacidade, desconto e custo de
  feitura de lá.
- `item/WandItem` — o vis é contado em centésimos, como no original: haste de vinte e cinco guarda dois
  mil e quinhentos. Aponta-se para um nó e segura-se o botão para beber dele, um ponto de cada vez. As
  hastes primordiais recolhem sozinhas o aspecto delas, até um décimo do que cabem.
- `client/render/WandRenderer` e `BoxMesh` — a varinha é peça de três dimensões, montada das mesmas três
  caixas do `ModelWand` original e com as texturas dele. O `BoxMesh` refaz o desenrolado de textura que o
  Minecraft antigo usava, sem o qual as texturas do mod sairiam embaralhadas.
- `client/WandHud` — os seis primários com as barrinhas no canto de baixo, como no original.

Falta da fatia: os focos da varinha e as varinhas de bastão em si (a peça existe, a receita não).

- `research/ResearchManager.unlock` — **onde os pontos do thaumômetro viram alguma coisa**: clicar numa
  pesquisa ao alcance, no livro, cobra os aspectos que ela pede e a destranca. Os preços são os do
  original. **Diferença deliberada**: no mod isto acontece na mesa de pesquisa, com papel, tinta e o
  tabuleiro de hexágonos; o tabuleiro é a última peça da fatia e ainda não chegou, e até lá seria pior
  deixar os pontos sem serventia nenhuma. Quem decide é sempre o servidor — do livro só sai o pedido.

Falta da fatia: o tabuleiro hexagonal da mesa de pesquisa; e, no livro, as páginas de receita e os
ícones de item — 168 pesquisas apontam para itens que só chegam nas fatias seguintes, e até lá elas
aparecem com o símbolo do aspecto de que mais precisam.
