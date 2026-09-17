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
| 3 | Thaumômetro e pesquisa: escanear, pontos, o caderno, o tabuleiro | **thaumômetro e caderno prontos**; o tabuleiro da mesa de pesquisa a fazer |
| 4 | Varinhas, nós e vis | **prontos**, com quatro focos (fogo, escavação, gelo e raio); os outros seis a fazer |
| 5 | Alquimia: crisol, essência, frascos, jarros, alambique | **pronta** — crisol, frascos, forno alquímico, alambique, tubos e jarros |
| 6 | Infusão: matriz, pedestais, instabilidade | **pronta** |
| 7 | Golens | a fazer |
| 8 | O resto: mácula, criaturas, eldritch, artifícios | a fazer |

Fora das fatias, entraram no caminho as peças sem as quais nada disso se joga: o minério infundido (de
onde saem os fragmentos), a matéria-prima do mod, as ferramentas e armaduras de táumio e de metal do
vazio, os blocos de construção, a bancada arcana e os Óculos da Revelação.

## O caminho de quem começa

Hoje o mod já se joga do começo ao meio, nesta ordem:

1. **Minerar pedra infundida** — ela nasce em veios no subsolo, uma cor por aspecto primordial, e larga
   fragmentos quando quebrada com picareta.
2. **Fazer o thaumômetro** — fragmento, ouro e vidro na bancada comum.
3. **Examinar o mundo** — botão direito com o thaumômetro erguido; cada coisa nova rende pontos de
   aspecto, e só se lê o que se tem cabeça para entender.
4. **Fazer a varinha** — pepitas de ferro viram a ponta; ponta e graveto viram a varinha.
5. **Bater numa estante de livros com a varinha** — ela vira o Thaumonomicon.
6. **Pesquisar** — no livro, clicar numa pesquisa ao alcance cobra os aspectos que ela pede.
7. **Achar um nó de aura** — só aparece com o thaumômetro na mão ou os óculos no rosto; segurar o botão
   da varinha nele enche a varinha de vis.
8. **Bater num caldeirão com a varinha** — ele vira crisol. Com água dentro e fogo por baixo, o que se
   joga nele se desfaz em aspectos; com a mistura certa e o catalisador, sai coisa nova — inclusive
   o táumio, de uma barra de ferro.
9. **Fazer ferramentas e armadura de táumio** — na bancada comum.
10. **Bater numa bancada comum com a varinha** — ela vira bancada arcana, que monta o que precisa de vis:
    os Óculos da Revelação, a ponta de ouro e os focos de fogo, escavação, gelo e raio.

11. **Montar a destilaria** — na bancada arcana saem o forno alquímico, o alambique, o jarro e os tubos.
    O forno desfaz o que se joga nele em essência; o alambique empilhado em cima recolhe; o tubo leva; o
    jarro guarda. É a segunda metade da alquimia, e a que abastece tudo o que vem depois.

12. **Erguer o altar de infusão** — pedestal arcano no chão, pedra arcana nos quatro cantos dele e a
    matriz rúnica dois blocos acima. A varinha acorda a matriz; com a coisa certa no pedestal do meio e
    os ingredientes nos pedestais em volta, o toque seguinte começa a infusão. É assim que saem as hastes
    de varinha melhores — obsidiana, gelo, quartzo, junco, blaze e osso.

O que ainda não tem caminho: os golens e o lado eldritch.

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
- `block/ArcaneWorkbenchBlock`, `inventory/ArcaneWorkbenchMenu` e `client/gui/ArcaneWorkbenchScreen` —
  a bancada arcana, com a folha e as medidas do original: resultado em (160, 64), varinha em (160, 24) e a
  grade de três por três em (40, 40) com passo de vinte e quatro. Os seis círculos em volta acendem na cor
  do aspecto que a receita cobra, e apagam quando a varinha não tem o bastante. Uma bancada comum vira
  bancada arcana com um toque de varinha.
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
- `research/ResearchManager.knows` — **quem não pesquisou não fabrica**, como no original: o crisol e a
  bancada arcana conferem a pesquisa da receita antes de deixar a mistura fechar. É isto que dá sentido ao
  livro. Receita sem pesquisa marcada passa livre, que são as que o mod deixa abertas.
- `item/PhialItem` — o frasco de essência, que guarda oito pontos de um aspecto. **Diferença deliberada**:
  no mod ele se enche no alambique, que chega com o resto da alquimia; até lá ele se enche direto do crisol
  fervendo, tirando dele o aspecto mais abundante.

### A essência encanada

Esta é a metade da alquimia que faz a essência sair do lugar. O crisol desfaz as coisas e a essência se
perde na água; daqui em diante ela é recolhida, levada e guardada.

- `api/aspects/EssentiaTransport` e `api/aspects/AspectContainer` — as duas interfaces do original
  (`IEssentiaTransport` e `IAspectContainer`), com os mesmos nomes e a mesma ideia. A regra de ouro da
  fatia está aqui: **a essência não é empurrada, é puxada**. Cada peça anuncia uma sucção — um aspecto
  que quer e uma força com que quer — e a essência corre de onde a sucção é fraca para onde ela é forte,
  uma unidade de cada vez.
- `block/entity/AlchemicalFurnaceBlockEntity` — o forno, com os números do `TileAlchemyFurnace`:
  cinquenta de essência guardada, dez tiques de fogo por ponto de aspecto e um empurrão para os
  alambiques a cada quarenta tiques. O empurrão tem as duas passadas do original — primeiro completa
  quem já começou um aspecto, depois dá um aspecto novo a quem estiver vazio —, que é o que faz uma
  pilha de alambiques **separar** a essência em vez de todos brigarem pela mesma.
- `block/entity/AlembicBlockEntity` — trinta e dois de um aspecto só, do `TileAlembic`. Ele só deixa
  sair: a sucção dele é zero, porque quem o enche é o forno de baixo, empurrando. É essa diferença que
  faz a tubulação andar num sentido só.
- `block/entity/TubeBlockEntity` — o coração da fatia, e a tradução linha a linha do `TileTube`. De
  dois em dois tiques ele refaz a conta da sucção: olha os vizinhos, acha o que puxa mais forte e passa
  a puxar com **um a menos** do que ele. É assim que a fome do jarro lá no fim da linha viaja tubo a
  tubo até a fonte, perdendo força a cada peça — e é por isso que a tubulação tem alcance, em vez de ser
  infinita. De cinco em cinco tiques ele tira uma unidade do vizinho que puxa menos. E quando dois lados
  puxam igual querendo aspectos diferentes, ele **vaza** por quarenta tiques e para tudo, que é o jeito
  do original de avisar que a tubulação foi mal pensada.
- `block/entity/JarBlockEntity` — sessenta e quatro de um aspecto, do `TileJarFillable`. Só se liga
  pelo alto, e é a fome dele que faz tudo andar: puxa com trinta e dois sem rótulo e com sessenta e
  quatro com rótulo — que é como o original faz um jarro rotulado ganhar de um sem rótulo na disputa
  pela mesma essência.
- `client/render/JarRenderer` — a névoa dentro do vidro, na cor do aspecto, subindo conforme o jarro
  enche e respirando devagar; e o símbolo do aspecto desenhado nos quatro lados, para se ler o jarro de
  qualquer ângulo. **Diferença**: no original a névoa gira dentro do pote; aqui ela sobe e respira, mas
  não gira.
- `client/gui/AlchemicalFurnaceScreen` — a tela do forno com a folha do original, mais a fila do que
  ele já tem guardado por dentro. Essa lista não viaja pela tela: ela vem do próprio bloco, que o
  servidor já mantém acertado em quem está por perto — assim ela pode ter os quarenta e oito aspectos
  sem precisar de um número de tela para cada um.
- As texturas são as do original: `pipe_1` e `pipe_2` no tubo, `metalbase` e `goldbase` no alambique,
  `jar_side`/`jar_top`/`jar_bottom` no jarro e `al_furnace_*` no forno.
- **Diferença**: a receita do tubo pede uma *gota de mercúrio* no original, que é o mercúrio miúdo que
  sai dos minérios nativos. Os minérios nativos são de uma fatia que ainda não chegou, então aqui a
  receita pede o próprio mercúrio.
- Falta da fatia: as variações de tubo (válvula, filtro, estreito, de mão única, tampão), o jarro do
  vazio, o fole que acelera o forno e o forno arcano.

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
- `item/Revealing` e os **Óculos da Revelação** — no original um nó de aura não fica à vista de qualquer
  um: é preciso o thaumômetro na mão ou os óculos no rosto. Sem isso, um nó é só ar. É o que voltou a
  valer aqui.
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
- `item/FocusItem` e `item/Focuses` — os focos de varinha, que é o que dá magia à varinha. Por ora são
  quatro, com os custos lidos no `getVisCost` de cada classe do original, sem melhoria nenhuma:
  - **fogo**, jato contínuo, ignis 10 por tique: um sopro de chamas que incendeia o que alcança;
  - **escavação**, jato contínuo, terra 15 por bloco: quebra o bloco na mira a doze blocos;
  - **gelo**, tiro único, aqua 5 + ignis 2 + perditio 2: atira uma lasca que tira três de vida, congela a
    água em que bate e deixa neve onde cai;
  - **raio**, jato contínuo e o mais caro de todos, aer 25 por tique: fulmina a criatura na mira a vinte
    blocos, tirando quatro de vida por tique.

  **Diferença**: no mod o foco entra numa casa da própria varinha, alcançada por uma tecla; aqui ele se
  encaixa com um clique, que procura a varinha no inventário. **Diferença**: a lentidão que a lasca de
  gelo deixa é acréscimo daqui — o original entrega esse efeito pela melhoria do gelo alquímico, e as
  melhorias de foco ainda não existem neste porte. **Diferença**: o raio do original é uma linha traçada
  à mão pelo mod; aqui é um rastro de faíscas do jogo.
- `entity/FrostShardEntity` e `registry/TCEntities` — a primeira criatura do porte, e a base para as
  das fatias seguintes. A lasca voa quase reta (o original lhe dá uma queda de leve), some sozinha em
  cinco segundos e se desenha como o item dela, igual a uma bola de neve. O item `frost_shard` existe só
  para dar cara ao projétil e fica fora da aba do criativo — é o primeiro item registrado assim, e o
  teste da aba passou a saber a diferença.
- `registry/TCSounds` — **gerado** pelo `scratchpad/sons.js` a partir dos `.ogg` do próprio mod:
  dezesseis sons, trinta e dois arquivos. Com eles, a varinha, o thaumômetro, o crisol, o livro e a
  pesquisa deixaram de tomar som emprestado do Minecraft e passaram a soar como o original soa. Os que
  ainda não têm dono (jarro, cristal) entraram junto porque as fatias seguintes vão querê-los.
- `client/WandHud` — os seis primários com as barrinhas no canto de baixo, como no original.

Falta da fatia: os seis focos restantes (buraco portátil, proteção, primordial, morcego, troca e pech),
as melhorias de foco e as varinhas de bastão em si (a peça existe, a receita não).

- `research/ResearchManager.unlock` — **onde os pontos do thaumômetro viram alguma coisa**: clicar numa
  pesquisa ao alcance, no livro, cobra os aspectos que ela pede e a destranca. Os preços são os do
  original. **Diferença deliberada**: no mod isto acontece na mesa de pesquisa, com papel, tinta e o
  tabuleiro de hexágonos; o tabuleiro é a última peça da fatia e ainda não chegou, e até lá seria pior
  deixar os pontos sem serventia nenhuma. Quem decide é sempre o servidor — do livro só sai o pedido.

Falta da fatia: o tabuleiro hexagonal da mesa de pesquisa; e, no livro, as páginas de receita e os
ícones de item — 168 pesquisas apontam para itens que só chegam nas fatias seguintes, e até lá elas
aparecem com o símbolo do aspecto de que mais precisam.

## Fatia 6 — infusão

A infusão é o terceiro jeito de fabricar do mod, e o mais perigoso. A bancada arcana cobra vis da
varinha; o crisol cobra aspectos dissolvidos na água; a infusão cobra **essência guardada em jarros**, e
cobra também paciência — ela leva tempo, e pode dar errado no meio.

- `crafting/InfusionRecipe` — a receita: o que vai no pedestal do meio, o que vai nos de fora, a
  essência que a matriz vai sugar e a instabilidade natural daquela receita. Diferente da bancada, a
  forma não importa: os pedestais de fora podem estar em qualquer lugar ao alcance e em qualquer ordem.
  O que importa é o conjunto.
- `crafting/InfusionRecipes` — **gerada** pelo `scratchpad/fatia7-infusao.js` a partir das três fatias
  de receita do original (`ConfigRecipesInfusionSlice`, `...DeviceSlice` e `...EquipmentSlice`). Seis
  receitas fecham hoje, e são justamente as **hastes de varinha** — que até esta fatia não tinham como
  ser feitas.
- `block/entity/PedestalBlockEntity` e `block/PedestalBlock` — o pedestal arcano, do `TilePedestal`:
  segura uma coisa só. Um toque põe, outro tira.
- `block/entity/InfusionMatrixBlockEntity` — a matriz rúnica, e o coração da fatia. A construção é a do
  diagrama do altar do próprio original (o `InfusionAltar` do `ConfigRecipes`): pedestal no chão, pedra
  arcana nos quatro cantos dele, matriz dois blocos acima. De dez em dez tiques ela dá um passo — puxa um
  ponto de essência de algum jarro a doze blocos, ou consome um ingrediente de um pedestal —, e no fim a
  coisa nova toma o lugar da velha no pedestal do meio.
- **A instabilidade e a simetria.** É o que faz a infusão do Thaumcraft ser o que é. Cada receita traz a
  sua instabilidade, e a ela se soma a falta de simetria da construção: cada pedestal conta dois pontos,
  e mais um se tiver coisa em cima; o pedestal espelhado do outro lado da matriz desconta o mesmo. Uma
  sala perfeitamente simétrica zera a conta — e é por isso que as salas de infusão do mod são desenhadas
  como mandalas. A cada passo, com um em quinhentos de chance por ponto de instabilidade, alguma coisa
  dá errado.
- `api/aspects/EssentiaSources` — o `EssentiaHandler` do original, reduzido ao que a infusão usa: a
  matriz não tem cano nenhum ligado a ela, ela chama a essência dos jarros por perto e a essência vem
  pelo ar, com um fio de luz na cor do aspecto. **Diferença**: o original guarda uma lista dos jarros
  achados para não vasculhar o mundo toda vez; aqui a vasculhada é feita na hora, porque o alcance é
  curto e ela só acontece a cada dez tiques.
- `client/render/PedestalRenderer` — o que está no pedestal paira um dedo acima do prato e gira devagar,
  como no original.
- **Diferença**: o original sorteia entre vinte e um azares quando a infusão escapa; aqui são os quatro
  que dá para fazer sem as peças das fatias seguintes — cuspir um ingrediente, um raio, um susto em quem
  estiver perto e a explosão. Os outros (mácula, criaturas do vazio, distorção) chegam com a fatia oito.
- **Diferença**: o `validLocation` da versão que serve de planta pede um bloco de "pilar de infusão" nos
  quatro cantos. Esse bloco não tem receita em lugar nenhum das fontes, e o diagrama do altar que o
  próprio mod desenha no livro põe **pedra arcana** ali. Ficou a pedra arcana, que já existe e já se faz.
- Falta da fatia: a infusão que encanta (o original também encanta itens na matriz), as melhorias rúnicas
  e as cinquenta e oito receitas que esperam peças das fatias seguintes.
