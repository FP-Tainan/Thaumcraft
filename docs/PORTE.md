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
| 3 | Thaumômetro e pesquisa: escanear, pontos, o caderno, o tabuleiro | **pronta** — com a mesa de pesquisa e o tabuleiro de hexágonos |
| 4 | Varinhas, nós e vis | **prontos**, com sete focos (fogo, escavação, gelo, raio, buraco portátil, troca e primordial); proteção, morcego e pech a fazer |
| 5 | Alquimia: crisol, essência, frascos, jarros, alambique | **pronta** — crisol, frascos, forno alquímico, alambique, tubos e jarros |
| 6 | Infusão: matriz, pedestais, instabilidade | **pronta** |
| 7 | Golens | **os oito golens e os doze núcleos prontos**; dois núcleos já trabalham (juntar e colher) |
| 8 | O resto: mácula, criaturas, eldritch, artifícios | **a mácula pronta**; criaturas, eldritch e artifícios a fazer |

Fora das fatias, entraram no caminho as peças sem as quais nada disso se joga: o minério infundido (de
onde saem os fragmentos), a matéria-prima do mod, as ferramentas e armaduras de táumio e de metal do
vazio, os blocos de construção, a bancada arcana e os Óculos da Revelação.

## Para testar sem jogar tudo de novo

Em criativo os itens aparecem na aba, mas isso não basta: o crisol, a bancada arcana e a infusão
conferem a pesquisa antes de deixar sair qualquer coisa. O comando `/thaumcraft` é o atalho:

| comando | o que faz |
|---|---|
| `/thaumcraft tudo` | descobre os 48 aspectos, enche o bolso de pontos e destranca as 201 pesquisas |
| `/thaumcraft pesquisa tudo` | só as pesquisas |
| `/thaumcraft pesquisa dar <chave>` | uma pesquisa só, com a lista completando o nome |
| `/thaumcraft pesquisa limpar` | apaga o caderno, para começar do zero |
| `/thaumcraft pontos [quanto] [aspecto]` | pontos de aspecto; sem dizer nada, 64 de cada um |
| `/thaumcraft varinha` | enche de vis a varinha que estiver na mão |

Todos aceitam um alvo no fim (`/thaumcraft tudo @a`), e pedem nível dois de permissão — em mundo de um
jogador só basta ter os truques ligados.

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

12. **Erguer o altar de infusão** — pedestal arcano no chão, tijolos de pedra arcana nos quatro cantos dele,
    pedra arcana em cima dos tijolos e a matriz rúnica dois blocos acima do pedestal. A varinha (vinte e cinco
    de cada primário) transforma os cantos em pilares de infusão e acorda a matriz; com a coisa certa no pedestal do meio e
    os ingredientes nos pedestais em volta, o toque seguinte começa a infusão. É assim que saem as hastes
    de varinha melhores — obsidiana, gelo, quartzo, junco, blaze e osso.

13. **Fazer um golem** — um fardo de feno no crisol com *humanus*, *motus* e *spiritus* vira um golem de
    palha; os outros sete saem do mesmo jeito, cada um da sua matéria. Na bancada arcana saem o sino e o
    núcleo em branco, e no crisol o núcleo em branco vira o núcleo do serviço que se quiser.
14. **Pôr o golem para trabalhar** — o golem é posto na face do baú que vai ser a casa dele; encaixa-se o núcleo
    com um toque; toca-se o sino no golem (para ligá-lo) e depois nas faces dos baús ou blocos onde ele deve buscar ou
    levar. O toque sem nada na mão abre a tela dele, onde se diz o que buscar e quanto.

15. **Errar uma infusão** — construção sem simetria (pedestais e estabilizadores sem par do outro lado) faz a
    instabilidade subir; a cada ciclo, com ela em quinhentos, um dos vinte e um azares do original: ingrediente cuspido
    ou destruído (com gosma ou gás de fluxo, ou uma explosão), raios, mácula do fluxo ou cansaço de vis em quem estiver
    perto, a explosão na matriz, ou distorção num jogador.

O que ainda não tem caminho: as criaturas, o lado eldritch e os artifícios de vestir.

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
- A receita do tubo pede a *gota de mercúrio*, como no original (chegou com os aglomerados nativos).
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
- ~~A pedra arcana no lugar do pilar.~~ Resolvido com o jar: o pilar de infusão não tem receita porque é a
  **varinha** que o faz. O `createInfusionAltar` do `WandManager` confere o esqueleto (tijolos de pedra
  arcana nos cantos, pedra arcana em cima deles), cobra vinte e cinco de cada primário e troca os cantos pelos
  pilares (`InfusionPillarBlock`, desenhados com o `pillar.obj` do mod); tirando uma metade de um pilar, a
  outra cai, e cada uma devolve o que era. Os tijolos (4 pedras arcanas numa grade 2×2), as escadas e a laje de
  pedra arcana chegaram junto.
- **Correção de textura**: no original o *bloco* de pedra arcana usa a textura `pedestal_top` e os *tijolos*
  é que usam a `arcane_stone`; o porte tinha trocado. O bloco de sebo ganhou o topo próprio.
- Falta da fatia: a infusão que encanta (o original também encanta itens na matriz), as melhorias rúnicas
  e as cinquenta e oito receitas que esperam peças das fatias seguintes.

## Fatia 7 — golens (refeitos fiéis)

Porte do `EntityGolemBase`, do `GolemHelper`, do `InventoryMob`, do `InventoryUtils`, de todas as tarefas de
`thaumcraft.common.entities.ai.{inventory,interact,fluid,combat,misc}` que os golens usam, do `ContainerGolem` com o
`ContainerGhostSlots`, do `GuiGolem`, do `RenderGolemBase` com o `ModelGolem` e o `ModelGolemAccessories`, do
`RenderEventHandler.renderMarkedBlocks`, do `EntityGolemBobber`/`RenderGolemBobber`, do `EntityDart`/`RenderDart` e
dos itens `ItemGolemPlacer`, `ItemGolemCore`, `ItemGolemUpgrade`, `ItemGolemDecoration` e `ItemGolemBell` (tudo
descompilado do jar). O golem de dois núcleos com o sino de um baú só saiu inteiro.

- **O golem** (`entity/GolemEntity`): a matéria (`GolemTypes`, gerada do `EnumGolemType`) dá vida, couro (mais
  visor 1 e blindagem 4, até 20), passo (o do tipo: gravata ×1,1, blindagem ×0,88, ar +15% cada, avançado ×1,1, os
  pesados ×2 debaixo d'água), carga (mais `min(16, max(4, carga))` por terra), força, fogo e remendo (a cada
  `regenDelay` tiques, ⅔ com o barrete). Casa = onde foi posto; o baú da casa é o bloco atrás da face tocada. Alcance
  16 (+4 por água, +10% com óculos, +20% avançado). Longe demais da casa (48) ou preso num bloco, volta para perto dela.
  Parado com a tela aberta ou em cima da algema acesa, a IA desliga.
- **Núcleos** (as tarefas de cada um na prioridade do original): 0 encher (busca nos baús marcados até a casa ter a
  quantidade pedida, ou "qualquer quantidade"), 1 esvaziar, 2 juntar, 3 colher (com ordem, replanta — sementes, cacau
  no tronco, vagem de mana), 4 guardar (com ordem: chaves de monstros, bichos, jogadores e creepers), 5 decantar
  (fontes e tanques marcados para o tanque da casa; com entropia, bombeia o lago todo), 6 alquimia (dos alambiques ou
  do jarro da casa para os jarros marcados, na ordem de preferência do original), 7 lenhar (o tronco inteiro, do
  bloco mais longe), 8 usar (clica o que carrega nos blocos marcados, direito ou esquerdo, agachado ou não),
  9 açougue (o bicho mais velho, só se sobrarem dois), 10 separar, 11 pescar (a boia, as tabelas de pesca do jogo de
  então, o fogo assa o peixe). Todos fogem do creeper inchando, abrem portas e porteiras e voltam para casa.
- **Melhorias** (até duas de cada; 1 casa, 2 no táumio/sebo/carne, +1 avançado): ar, terra, fogo (mais casas, fogo
  no golpe), água, ordem (cores nas marcas e nas casas, chaves de guarda), entropia (espinhos, dicionário de "minérios",
  ignorar dano e componentes). **Acessórios**: cartola (+5 de vida), óculos, gravata, barrete, lança-dardos (o
  `EntityDart`), visor (o golpe conta como de jogador, para o que só cai assim), blindagem, maça (+2 de dano).
- **O sino**: tocado no golem, liga-se a ele e copia as marcas; tocado num bloco (vê a água também), marca e desmarca a
  face — com a ordem no golem, gira as dezesseis cores (agachado, tira). Batido no golem, recolhe-o como item com tudo
  (núcleo, melhorias, acessórios, marcas, casas); agachado, larga o núcleo e, por sorte, as melhorias. O golem guardado
  e o sino agem antes de o baú abrir (o `onItemUseFirst`).
- **Casas fantasmas** (`inventory/GolemMenu`): a cópia do que se põe, a quantidade mudando no clique (256 no de encher),
  seis de cada vez com rolagem; a de líquido aceita recipientes. **A tela** (`client/gui/GolemScreen`): a fala do golem
  (ou, no avançado, de vez em quando uma ameaça), as abas de cor, as chaves de cada núcleo (em inglês, como no
  original), o golem em pé.
- **Desenho**: o corpo encolhido a quatro décimos dentro do modelo, as poses do original (cabeça baixa sem núcleo ou
  algemado, o despertar com o tique-taque, os braços no passo, carregando, batendo, abertos no alquimista), o balanço
  do passo, o verde do remendo, o núcleo no peito e as plaquinhas das melhorias abaixo dele, os acessórios, as
  rachaduras conforme a vida, o que carrega nos braços, o balde (`bucket.obj`) com o líquido dentro, o jarro do
  alquimista, a vara do pescador. As marcas no mundo: a runa colorida em cada face, o bloco de ar aceso, a casa e a
  linha de escrita saindo da cabeça do golem.
- **O baú itinerante** (`entity/TravelingTrunkEntity`, o `EntityTravelingTrunk` com o `InventoryTrunk`, o `ContainerTravelingTrunk`, o `GuiTravelingTrunk`, o `ModelTrunk`/`RenderTravelingTrunk` e o `ItemTrunkSpawner` com o desenho de baú): pula atrás do dono (parado e sem nada a fazer, fica), some e reaparece num anel em volta dele se fica a mais de vinte blocos, vai atrás dele para outros mundos, se remenda (de cinquenta em cinquenta tiques) e come comida; não sente fogo nem queda. Uma melhoria: ar (pula mais depressa), terra (quatro fileiras), fogo (morde quem feriu o dono, ficando zangado), água (nada o fere e só o dono abre e recolhe), ordem (o sino o recolhe com o que tem dentro) e entropia (suga o que está perto). A tela tem o botão de ficar e a vida; a receita é a infusão do original sobre o baú faminto. Testes: `TrunkGameTest` e `TrunkClientTest`.
- **A algema** (`block/GolemFetterBlock`, o 9/10 do `BlockCosmeticSolid`): acende com redstone e desliga o golem em cima.
- **Receitas**: as melhorias, os acessórios e a algema na bancada arcana (o gerador aprendeu a lã de cada cor, que
  saía branca), e o golem avançado na infusão (qualquer golem com o cérebro no jarro). O núcleo de lenhar espera o
  machado elemental.
- **Diferenças que ficam**: o "dicionário de minérios" são as etiquetas `c:` de hoje; a navegação acha caminho de
  outro jeito, mas vai até o ponto exato como a de então; o golem pensa em trabalho novo a cada dez tiques (o
  original, a cada quinze) porque o jogo de hoje só avalia as tarefas de dois em dois tiques; as mensagens do sino
  saem na barra de ação (o original tinha o `PlayerNotifications`, ainda não portado).
- **Testes**: `GolemGameTest` (a tabela, o corpo, o item e o sino guardando tudo, e cada núcleo de ponta a ponta:
  juntar, encher na quantidade exata, esvaziar, separar, colher e replantar, lenhar, guardar, açougue com casal,
  decantar, alquimia; a algema; as cores do sino; salvar e carregar) e `GolemClientTest` (fila, costas, tela, marcas).

## Fatia 8 — a mácula

A mácula é a conta que o Thaumcraft cobra de quem foi apressado. Ela não nasce sozinha no porte: ela
chega quando uma infusão dá errado, e daí em diante come a terra por conta própria.

- `block/TaintBlock` — a crosta e o solo maculado, do `BlockTaint` do original, com as regras dele:
  madeira e folha caem com **dois** vizinhos maculados, terra e pedra com **três**. Sozinha ela seca e a
  terra volta ao que era.
- `block/TaintFibreBlock` — as fibras que crescem por cima, do `BlockTaintFibres`: nascem no ar colado
  a coisa firme, e não nascem se só houver mácula em volta.
- `block/EtherealBloomBlock` — a Flor Etérea, a resposta do original: plantada, ela desfaz a mácula num
  raio de oito blocos, um pedaço de cada vez. A crosta volta a ser terra, o solo volta a ser grama, e as
  fibras somem.
- `block/ShimmerleafBlock` — a folha-cintilante, que é o que a Flor Etérea pede no crisol.
- A mácula é **cinza na textura**, como no original; quem a pinta é o jogo, com a cor do capim do bioma
  maculado do próprio mod — 7160201, que é 0x6D40C9.
- **A infusão e a mácula se encontram.** O azar da infusão ganhou um quinto caso: a magia que escapa
  apodrece um punhado de terra a seis ou dez blocos do altar. Nunca num bloco do altar — a construção não
  pode se desmanchar sozinha — e nunca um bloco solto, porque a regra do original pede vizinhos já
  maculados para a mácula avançar. É assim que ela chega no original também: em quantidade.

### As diferenças desta fatia

- **O bioma maculado não existe.** No original a mácula pinta o bioma, e é o bioma que decide se ela
  continua ou míngua. O Minecraft de hoje guarda bioma de quatro em quatro blocos e não deixa um mod
  repintá-lo bloco a bloco. Sem essa camada, quem segura a mácula aqui é a companhia: uma mancha viva se
  mantém e avança, um bloco solto se apaga.
- ~~A folha-cintilante nasce sozinha.~~ Resolvido com as árvores mágicas: ela agora nasce só em volta do pé
  dos pinheiros-de-prata, como no original.
- Falta da fatia: as criaturas da mácula (a aranha, o tentáculo, o enxame de esporos), o fluxo — a gosma
  e o gás que a mácula vira —, o lado eldritch inteiro e os artifícios de vestir.

## As árvores mágicas

A grande-madeira e o pinheiro-de-prata, com toras, folhas, mudas, tábuas, escadas e lajes.

- `world/GreatwoodTree` e `world/SilverwoodTree` — o `WorldGenGreatwoodTrees` e o
  `WorldGenSilverwoodTrees` traduzidos conta por conta, inclusive a toca de aranhas-das-cavernas de uma em
  oito grandes-madeiras e o nó de aura puro que nasce dentro do tronco do pinheiro (`SilverwoodKnotBlock`,
  com um quarto da aura da terra).
- `world/MagicalTreeFeature` — uma chance em vinte e cinco por pedaço de mundo para a grande-madeira e uma
  em sessenta para o pinheiro, na altura do que estiver mais alto ali, como no original.
- `block/MagicalSaplingBlock` — com luz nove, a muda cresce uma vez em 25 (grande-madeira) ou 50 (pinheiro)
  tiques ao acaso. Farinha de osso não adianta, como no original.
- As folhas soltam a muda uma vez em 200 (grande-madeira) ou 250 (pinheiro); as do pinheiro brilham com luz
  sete e soltam faísca, e são pintadas do cinza-azulado 8952234 do original.
- A folha-cintilante: luz oito, a caixa do `BlockCustomPlant` e o fogo-fátuo ciano do original.
- Com as peças, quatro receitas do original destravaram sozinhas nos geradores: a haste de Greatwood, o
  filtro, o golem de madeira e a haste de Silverwood.

### As diferenças desta parte

- **O dicionário de biomas.** A chance da grande-madeira vem das marcas de convenção do Fabric, que copiam
  o dicionário do Forge: certa nas florestas, uma em cinco em taigas, pântanos, savanas e planícies, meio a
  meio em terras viçosas. O pinheiro nascia nos "morros de floresta" e "morros de bétula", que o jogo de hoje
  fundiu na floresta e na floresta de bétulas — é lá que ele nasce agora, além de terra mágica.
- **A distância das folhas.** No original a folha apodrecia a mais de quatro passos do tronco; no jogo de
  hoje ela guarda a distância no próprio bloco e o limite é sete. Os geradores medem essa distância ao fim de
  cada árvore, para a copa não apodrecer no primeiro tique.
- **O carvão.** A tora vira carvão vegetal pela receita do próprio jogo (experiência 0,15); a do original dava
  0,5.
- **O nó do tronco** ainda não solta a essência de fogo-fátuo ao ser quebrado: o item não existe por aqui.

## Correções desta rodada

- O gerador das receitas arcanas não lia linhas da grade com `#` (`"Q#Q"`): os focos de fogo, gelo, raio,
  troca e escavação saíam com a grade errada. Agora saem como no original, e o foco primordial também.
- O foco Primordial (`EntityPrimalOrb`, `RenderPrimalOrb`, `FXWisp`): custo sorteado de 50 a 250 de cada
  primário, meio segundo entre tiros; uma em cem explosões deixa mácula ou um nó de aura.

- A pérola de cinzas: a flor do deserto (luz oito, fumaça e chaminha), uma chance em trinta por pedaço de
  mundo nos desertos e terras áridas. Na mesa, a folha-cintilante vira mercúrio e a pérola vira pó de blaze.
- **Cuidado com a fonte legível.** A versão do GitHub que serve de planta diz que a pérola de cinzas vira
  *açúcar* na mesa; o jar original descompilado diz *pó de blaze*. Vale o jar. Quando uma conta ou receita
  parecer estranha, a conferência é sempre contra o jar em `Mod Base/`.
- Os aspectos das coisas do próprio mod (fragmentos, pedra infundida, plantas, toras, recursos, peças de
  táumio) entraram na tabela, gerados do `ConfigAspects` (`scratchpad/aspectos-mod.js`).

## Os aspectos das coisas, refeitos do jar

A auditoria contra o jar mostrou que a tabela de aspectos das coisas do jogo tinha vindo de uma parte da fonte
do GitHub que **não existe no jar** — números inventados pelo re-porte para o 1.12. Foi refeita:

- `api/aspects/ConfigAspectsTable` — gerado pelo `scratchpad/aspectos-jar.js` direto do `ConfigAspects` do jar
  descompilado. Os nomes ofuscados (`Blocks.field_150348_b`) viram nomes de registro pelo mapa SRG montado com o
  `deobfuscation_data` do Forge 1.7.10 e o `Blocks`/`Items` do jar do 1.7.10 que estão na máquina
  (`scratchpad/srg/`). O dicionário de minérios virou as marcas de convenção (`c:ores/iron`, `c:dyes`…).
- `api/aspects/ObjectAspects` — o `generateTags` do `ThaumcraftCraftingManager`: o que não tem anotação é
  deduzido das receitas (crisol, bancada arcana, infusão e mesa, nessa ordem): três quartos da soma dos
  ingredientes pelo que a receita rende, mais a raiz do custo mágico, teto 64. O servidor monta a tabela ao
  abrir e ao recarregar os dados, e a manda para quem entra.
- `api/aspects/ObjectBonus` — o `getBonusTags`: armadura, arma, ferramenta (pelo nível do material),
  encantamentos, poções, a varinha e a essência que a coisa carrega; o `cullTags` deixa no máximo seis.
- **Diferença**: as receitas de hoje não são as de 2014, então o que é deduzido pode sair diferente do que se
  via no jogo antigo — a conta é a mesma, as receitas é que mudaram. O original também guardava o resultado
  da primeira receita achada; a ordem das receitas de hoje é outra.
- O `fatia3-tabela.js` (a tabela antiga) ficou obsoleto.

### As receitas conferidas contra o jar

A mesma auditoria achou, nas receitas geradas:

- **Alumentum**: o gerador do crisol atravessava o laço dos fragmentos equilibrados e fazia "fragmento
  equilibrado a partir de alumentum". Voltou a ser o do jar: pesquisa ALUMENTUM, carvão no crisol.
- As quantidades que o gerador descartava: pólvora, limo, argila e pó de pedra-luminosa saem dois; o pó de osso
  sai quatro; a transmutação do ouro sai três pepitas.
- **Erratas da fonte do GitHub** (o jar manda): o gelo alquímico pede **bloco de neve** (não gelo compactado); o
  golem de argila pede **tijolos** (não argila); a haste de gelo nasce do **gelo comum**; o núcleo de pesca
  leva bacalhau, baiacu e salmão. Quando chegarem, o *Liquid Death* pede **balde** e a *Void Seed* pede
  **sementes de trigo**.
- As 199 pesquisas bateram todas com o jar.
- As pedras de pavimento fazem o que faziam: a de **Viagem** dá dois segundos de velocidade II e de salto a
  quem pisa (faísca verde); a de **Proteção**, sem redstone, levanta uma barreira invisível de dois blocos que é
  parede para bicho e ar para gente, empurra o bicho que estiver no ar sobre ela, e mostra as runas do
  `FXBlockRunes` (azuis com redstone, vermelhas com a barreira tapada, lilases com bicho perto).
  **Diferença**: o caminho dos bichos sempre conta a barreira como parede; o original a abria também para o
  caminho quando havia redstone.
- As folhas da grande-madeira longe demais do tronco para as contas de hoje ficam permanentes: no original a
  folha nascida da árvore só apodrecia quando algo mudava perto dela.
- **As mesas do `ModelArcaneWorkbench`**: a bancada arcana deixou o modelo JSON aproximado e passou a ser o
  modelo do original (tampo, base e quatro pés, textura `worktable.png`), com a varinha deitada sobre o tampo
  quando está na casa dela. A **mesa de desconstrução** chegou com o mesmo modelo (`decontable.png`), o
  thaumômetro em cima, a coisa sendo desfeita girando acima e o primário que sobra girando rente ao tampo; a
  tela é a do original e o primário recolhido vira um ponto de pesquisa. **Diferença**: o original desenha a
  coisa sendo desfeita somando luz, meio fantasma; aqui ela é desenhada normal, acesa.
- O **fole arcano**: `TileBellows` e `ModelBellows`; cada fole apontando para o forno alquímico corta um oitavo
  do tempo de fogo, e no forno comum empurra o cozimento (o campo do jogo é alcançado por reflexão, já que o
  26.x não é ofuscado). O forno queimando alumentum destila o dobro de vezes, como no original.
- O **tubo filtro** (`TileTubeFilter`): com um rótulo marcado preso, só puxa aquele aspecto; agachado, o rótulo
  sai. A caixa de latão é do modelo do bloco; o miolo, pintado da cor do aspecto, é do desenhista (a cor muda
  com o rótulo). O **rótulo de jarro** ganhou a receita do original (corante preto, limo e quatro papéis) e a
  marcação: rótulo + frasco cheio na mesa dá o rótulo marcado (o frasco volta vazio), e o marcado sozinho volta
  a ser em branco — as `JarLabel0..47` e `JarLabelNull` do jar, feitas como uma receita especial.

## Correções da primeira rodada de validação (2026-09-18)

- **Ícones dos focos Primordial e do Buraco Portátil:** no inventário o original desenha só o `getIcon`; a
  textura de profundidade (`getFocusDepthLayerIcon`) é do foco montado na varinha. Ela saiu das camadas do
  modelo (`scratchpad/focos-novos.js`).
- **Flor Etérea:** o bloco não tem desenho (o original devolve `blank` para a face da cruz); quem desenha é o
  `EtherealBloomRenderer`, porte do `TileEtherealBloomRenderer`: caule, folhas de baixo, folhas de cima e o
  cristal crescendo com o `growthCounter`, e o brilho azul da sexta fileira da folha dos nós. Luz 15, som
  `roots` ao brotar, pega em qualquer chão firme (planta de caverna do Forge). O item usa a `purifier_seed`.
- **Matriz rúnica:** os oito cubos do `TileRunicMatrixRenderer`, com o `startUp` (se ergue, inclina e gira ao
  ligar), o tremor da instabilidade, o brilho roxo somado e o halo de raios durante a infusão; sons
  `infuserstart` e `infuser` e as runas subindo do pedestal (`doEffects`). Bloco cheio, luz 10.
- **Alumentum:** o clique direito arremessa o `EntityAlumentum` (invisível, rastro de fogos-fátuos e faíscas,
  explosão de 1,66). O Nitor, como no original, só vira luz colocada.
- **Fragmento de Conhecimento:** usado, dá um ou dois de cada primário ao estoque de pesquisa.
- **Farinha de osso nas mudas mágicas (diferença pedida):** o original não aceitava; aqui vale como numa muda
  comum, 45% de chance de tentar a árvore a cada uso.

## Alquimia, parte A: centrífuga, cristalizador e construto (2026-09-18)

- `block/CentrifugeBlock` + `entity/CentrifugeBlockEntity` — o `TileCentrifuge`: puxa por baixo um ponto composto
  (sucção 128 vazia, 64 ocupada), gira 39 tiques e solta por cima um dos dois componentes, sorteado; redstone
  para. `client/render/CentrifugeRenderer` é o `ModelCentrifuge` (tampas paradas, eixo e pesos girando), com o
  estalo `pump` a cada meia volta.
- `block/EssentiaCrystalizerBlock` + `entity/EssentiaCrystalizerBlockEntity` — o `TileEssentiaCrystalizer`: a
  boca fica para o bloco em que ele foi encostado; 200 passos de 5 tiques e sai uma `CrystalEssenceItem` pelo lado
  de trás (num baú, se houver) com chiado e vapor. `EssentiaCrystalizerRenderer` desenha o `crystalizer.obj` e os
  quatro cristais do `vis_relay.obj`, que giram e tomam a cor do aspecto.
- **Diferença:** a rede de vis (relés) ainda não existe; o original somaria terra drenada da rede para acelerar o
  cristalizador. Aqui ele anda sempre no passo de base, como o original sem relé por perto.
- `ObjModel` lê os `.obj` do original (copiados sem mudança para `models/obj`) em tempo de execução, por grupo.
- `AspectTint` é a tinta de item `thaumcraft:aspect` (o `getColorFromItemStack` do cristal), posta na lista do
  jogo por reflexão, porque o Fabric não a abre.
- O construto alquímico é o metadado 9 do `BlockMetalDevice`: um cubo com `alchemyblock`.
- Os nomes em pt_BR que o original não traduziu (construto, cristalizador, essência cristalizada) foram traduzidos
  aqui (`scratchpad/alquimia.js`).

## Alquimia, parte B: lâmpadas e âmbar (2026-09-18)

- `block/ArcaneLampBlock` (três tipos) com `FACING` (o lado de apoio, ao contrário do clicado, como o
  `BlockMetalDeviceItem`) e `LIT`; cai quando o apoio vira ar. O corpo é modelo de bloco (a caixa W4..W12 × W2..W14
  do `BlockMetalDeviceRenderer`, texturas animadas do original); o `ArcaneLampRenderer` desenha o bocal
  (`renderNozzle` do `ModelBoreBase`, textura `bore.png`).
- `entity/ArcaneLampBlockEntity` — o `TileArcaneLamp`: a cada tique sorteia um ponto a ±15 (no máximo 4 acima do
  chão) e, se for ar com luz < 9, põe ali uma `LampLightBlock` (o `blockAiry` 3: invisível, luz 15). Quebrada, apaga
  as luzes num cubo de 31. A lâmpada do túnel da perfuratriz arcana fica para quando a perfuratriz existir.
- `entity/GrowthLampBlockEntity` — o `TileArcaneLampGrowth`: 1 Herba = 100 cargas, com 1 de reserva; a cada tique
  desce uma coluna sorteada do quadrado de 13 e empurra a primeira planta não madura a < 6 blocos, com faísca verde
  no que cresceu. O `scheduleBlockUpdate` do 1.7 vira `randomTick` (é o tique de crescimento de hoje); o
  `Material` de planta vira as classes de bloco equivalentes; o `CropUtils.isGrownCrop` foi traduzido regra a regra.
- `entity/FertilityLampBlockEntity` — o `TileArcaneLampFertility`: até 4 cargas de Victus (sucção 128 − 10×cargas);
  com 2+, a cada 300 tiques põe no cio um par adulto da mesma espécie a até 7 blocos (se não houver mais de 7).
- `block/AmberBlock` — bloco e tijolos de âmbar (`BlockCosmeticOpaque` 0 e 1): translúcidos (alfa 232 da textura),
  opacidade de luz 3, e as quatro receitas de bancada comum do original (`scratchpad/ambar.js`).
- O mapeador agora traduz os 16 metadados do corante de 2014 (o 15 é a farinha de osso da Lâmpada do Crescimento).
- "Lâmpada da Fertilidade" foi traduzida aqui; o pt_BR do original não a tinha.

## Alquimia, parte C: baú faminto (2026-09-18)

- `block/HungryChestBlock` + `entity/HungryChestBlockEntity` — o `BlockChestHungry`/`TileChestHungry`: 27 casas, a
  frente para quem coloca, sem baú duplo. O item que encosta é engolido (som de comer e a tampa dá uma mordida de
  dois décimos); o que não couber fica por cima. Comparador lê o conteúdo.
- `HungryChestRenderer` desenha o `ModelChest` **do 1.7.10** (conferido no jar do jogo: tampa 14×5×14, fecho 2×4×1,
  base 14×10×14, UV 0,0 e 0,19 em 64×64), porque o baú do jogo de hoje mudou de modelo e de folha de textura.
- **Diferença:** o original deixava o baú olhar para cima ou para baixo quando o jogador mirava muito inclinado
  (`BlockPistonBase.determineOrientation`), mas o desenhista só girava para os quatro lados; aqui ele sempre fica
  de pé, virado para quem o pôs.
- O mapeador traduz o alçapão de 2014 como o alçapão de carvalho.

## Alquimia, parte D: levitador arcano (2026-09-18)

- `block/LevitatorBlock` + `entity/LevitatorBlockEntity` — o `BlockLifter`/`TileLifter`: empurra para cima itens,
  o que pode ser empurrado e cavalos até 10 blocos (+10 por levitador ligado empilhado embaixo), parando no
  primeiro bloco cheio; zera a queda; agachado, o jogador desce devagar. Redstone nele ou no bloco de cima desliga.
  Roda dos dois lados como o original: o servidor move itens e bichos, o cliente move o próprio jogador.
- O desenho é o do `BlockLifterRenderer`: o cubo com frestas e, um centésimo para dentro, o `animatedglow` tingido
  (0x00A000 em cima, 0xDD11FF dos lados) — no modelo de bloco, com `light_emission` 11 quando ligado (o brilho 180
  do original). No inventário os lados vão com 0xEECCFF, como o `renderInventoryBlock`.
- A redstone no bloco de cima não avisa o levitador; ele confere a cada 100 tiques, junto com a conta do alcance
  (o original perguntava a cada tique).

## Equipamentos, parte 1: desconto de vis, mantos e botas do viajante (2026-09-18)

- `api/wands/VisDiscountGear` + `WandItem.modifier/totalVisDiscount` — o `IVisDiscountGear` e o
  `getConsumptionModifier` do original: o multiplicador da ponteira menos a soma dos descontos do que se veste,
  com piso de 0,1. Vale para focos, bancada arcana (inclusive o custo mostrado) e o altar. Os óculos dão 5%.
- O material `armorMatSpecial` (1/3/2/1, durabilidade 25) agora é o de óculos, mantos e botas — os óculos estavam
  com 2 de proteção por engano.
- `RobeItem` — peito e calça 2%, botas 1%; tingíveis como o couro (receita `crafting_dye`, lavam no caldeirão pela
  tag `cauldron_can_remove_dye`), cor sem tinta 0x6A3880, com a camada "over" sem cor; consertam com tecido
  encantado. Desenho `robes_1`/`robes_2` do original como `equipment/robes`.
- `TravellerBootsItem` — empurrão de 0,055 no chão (um quarto na água), 0,05 de controle no ar, −0,25 de queda por
  tique, 350 de durabilidade. **Diferença:** o degrau de um bloco é um atributo do item (sempre ativo com a bota
  no pé); o original só o ligava andando para a frente e sem agachar.

## Equipamentos, parte 2: armadura de fortaleza de táumio (2026-09-18)

- `client/render/model/FortressArmorModel` é **gerado** (`scratchpad/fortaleza-modelo.js`) do `ModelFortressArmor`
  descompilado: 66 caixas presas às partes do corpo, sem as caixas do `ModelBiped` (o original as apaga).
- `FortressArmorRenderer` (pelo `ArmorRenderer` do Fabric): cada peça mostra a sua parte; a couraça leva o cinto
  largo, o peitoral e as costas, a calça o cinto estreito; os enfeites crescem com o conjunto (duas ou três peças),
  como no `render` do original; o elmo mostra a máscara e os óculos e é 1% maior.
- `FortressArmorItem` + componentes `fortress_mask` e `fortress_goggles`: o elmo com óculos revela como os óculos
  (sem o desconto de vis, como diz o próprio livro).
- `InfusionRecipe.onCentral`: a saída `Object[]{"etiqueta", valor}` do original (grava a marca no item do meio) —
  óculos (HELMGOGGLES) e máscaras (MASKANGRYGHOST, MASKSIPPINGFIEND; a MASKGRINNINGDEVIL espera o cérebro de zumbi).
- `event/FortressMasks`: o demônio que bebe cura 1 com chance dano/12, o fantasma irado dá 4 s de Wither com chance
  dano/10. O diabo sorridente atenua a Distorção, que ainda não existe.
- `mixin/LivingEntityArmorMixin` — o `ISpecialArmor` com o `ArmorProperties.ApplyArmor` do Forge, só para jogadores com
  alguma peça de fortaleza: magia dano/35 e fogo/explosão dano/20 com prioridade (valem mesmo no dano que atravessa
  armadura, como no 1.7.10), o resto dano/25, tudo vezes 0,875 + 0,125 por peça + 0,05 por máscara; as outras peças
  vestidas entram com armadura/25. Conjunto completo sem máscara: 80% de um golpe comum.
- O gerador de infusão agora separa argumentos respeitando chaves, e o mapeador traduz a caveira de 2014 pelo
  metadado e os corantes de cor pela coleção `Items.DYE` do 26.2.
- "Elmo/Couraça/Coxotes de Fortaleza de Táumio" e os nomes das máscaras foram traduzidos aqui; o pt_BR do original
  não os tinha.

## Equipamentos, parte 3: tecla de trocar foco, menu radial e bolsa de focos (2026-09-18)

- **Mudança de comportamento:** o foco não se encaixa mais clicando com ele na mão (isso tinha sido inventado).
  Agora é como no original: a tecla F (`key.thaumcraft.focus`) com a varinha na mão abre o **menu radial**; agachado,
  F tira o foco preso. `client/FocusRadial` é o `KeyHandler` + `REHWandHandler.handleFociRadial`: as duas rodas
  (`radial.png`/`radial2.png`) girando em sentidos opostos com meia opacidade, o foco preso no centro, os outros em
  círculo pela chave de ordenação (`FocusItem.sortKey`, as letras do `getSortingHelper` de cada foco), o que o mouse
  toca crescendo até 1,3; soltar a tecla ou clicar escolhe.
- **Diferença:** no 1.7 o menu só soltava o mouse; no 26.2 um clique com o mouse solto e sem tela o prende de novo,
  então o menu vive numa tela transparente enquanto F está apertada (o encolher final continua pelo mostrador).
- `item/FocusSwap` — o `WandManager.changeFocus` + `PacketFocusChangeToServer`: junta os focos do inventário e das
  bolsas, pega o pedido (ou o próximo, ou o primeiro), devolve o antigo para a primeira bolsa com espaço ou para o
  inventário, com o som `camera_ticks`. Já tem o gancho para as bolsas vestidas (`extraPouches`).
- `FocusPouchItem` + `FocusPouchMenu` + `FocusPouchScreen` — 18 casas só de foco (6 por fileira), a casa da bolsa
  travada, o conteúdo salvo ao fechar, a tela `gui_focuspouch.png` sem rótulos.
- O mostrador da varinha agora mostra o custo do foco com o desconto dos equipamentos.

## Equipamentos, parte 4: as casas do Baubles e as peças comuns (2026-09-18)

- O Thaumcraft 4.2.3.5 dependia do **Baubles 1.0.1.10**, que não existe no Fabric 26.2. As casas dele vieram para
  dentro do porte, do mesmo jeito (fonte em `Mod Base/Baubles-1.7.10-1.0.1.10`):
  - `api/baubles/BaubleItem` + `BaubleType` — o `IBauble` (vestir, tirar, tique vestido, pode vestir/tirar).
  - `baubles/Baubles` — as 4 casas (amuleto, anel, anel, cinto) num anexo do jogador, salvas com ele e mandadas
    para a máquina de quem joga; o tique das peças dos dois lados; a queda na morte sem `keepInventory`.
  - `inventory/BaublesMenu` + `client/gui/BaublesScreen` — o `ContainerPlayerExpanded`/`GuiPlayerExpanded` com o
    fundo `expanded_inventory.png` do Baubles: armadura à esquerda, o jogador olhando o mouse, as 4 casas, craft 2×2.
  - `client/BaublesClient` — a tecla B e o botãozinho no inventário de sempre (o `GuiBaublesButton`), que troca
    entre os dois inventários.
- O desconto de vis soma as peças vestidas antes da armadura, como o `getTotalVisDiscount` do original.
- A **bolsa de focos** também se veste no cinto (`ItemFocusPouchBauble`) e a tecla de trocar foco procura nela.
- `BaubleBlankItem` — amuleto, anel e cinto comuns (receitas de bancada do original) e os anéis de aprendiz dos seis
  primários (1% de desconto no aspecto deles; no original vêm de baús de masmorra, que ainda não foram portados).
- Os nomes das peças comuns e a palavra "desconto" foram traduzidos aqui; o pt_BR do original não os tinha.

## Equipamentos, parte 5: escudo rúnico, pedra e amuleto de vis (2026-09-18)

- `event/RunicShield` — o `EventHandlerRunic` da 4.2.3.5. A cada 40 tiques (ou quando as peças mudam) soma as
  cargas da armadura e das peças vestidas; recarrega uma runa a cada 2 s (menos 0,5 s por anel carregado) gastando
  50 centésimos de ar e de terra, primeiro do amuleto de vis vestido e depois das varinhas do inventário (com o
  fator da ponteira, como o `consumeAllVisCrafting`). Cada runa segura um ponto de dano, antes da armadura
  (`mixin/PlayerRunicMixin`); afogamento, Wither, vazio e fome passam direto. Ao zerar: o cinturão cinético explode
  (20 s de espera), o anel revigorante dá Regeneração (20 s) e o amuleto de emergência devolve até 8 runas (60 s).
- **Diferença:** a espera para voltar a carregar depois de zerar era um campo só, de todos os jogadores, no
  original; aqui cada jogador tem a sua.
- `item/RunicBaubleItem` — amuleto (8) e amuleto de emergência (7), anel menor (1), anel (5), carregado (4) e
  revigorante (4), cinturão (10) e cinético (9). `client/RunicHud` desenha a barra dourada sobre os corações (onde o
  original a punha), o clarão `client/fx/ShieldRunesFx` (o `hemis.obj` com os 15 quadros, somando luz e sem descartar
  as faces de trás) e a linha "Escudo rúnico +N" nas dicas.
- **Reforço rúnico** (`crafting/RunicAugmentRecipe`, o `InfusionRunicAugmentRecipe`): qualquer peça que aceite
  escudo vai no meio, diamante + sal mundus + um sal por carga em volta; sai com uma carga a mais. Essência
  32 × 2^cargas, instabilidade 5 + cargas/2. As peças que aceitam são a etiqueta `thaumcraft:runic_armor`, gerada
  por `scratchpad/runico-tag.js` a partir das classes do jar que implementam `IRunicArmor` (faltam as dos cultistas,
  do cinto e do arreio de voo e do manto do vazio, que ainda não foram portados).
- `item/VisAmuletItem` — pedra de vis (25) e amuleto de vis (250, só se veste com a pesquisa): vestidos, passam
  até 5 centésimos por aspecto a cada 5 tiques para a varinha na mão. **Pendente:** encher o amuleto pelos relés de
  vis chega com a rede de vis; a receita da pedra, com os baús de masmorra; a do amuleto espera os cristais de vis.
- Receitas de infusão: as poções do jar (metas 8233, 8226, 8257, 16428, 24620) viram ingredientes pelo componente
  da poção (`DefaultCustomIngredients.components`), então só a poção certa serve. As duas receitas do cinturão
  cinético do jar diferem só na meta da poção de arremesso, que aqui é a mesma: fica uma.
- Os nomes das peças rúnicas e de vis foram traduzidos aqui; o pt_BR do original não os tinha.

## Equipamentos, parte 6: arreio e cinturão taumostáticos (2026-09-18)

- `item/HoverHarnessItem` — o `ItemHoverHarness`: couraça do `armorMatSpecial` com 400 de durabilidade, conserta com
  ouro, 5% de desconto de vis no ar e 2% no resto. Com ele na mão, clicar abre a casa do jarro
  (`inventory/HoverHarnessMenu` + `client/gui/HoverHarnessScreen`, a `guihoverharness.png`), que só aceita jarro com
  Potentia; o jarro volta para o arreio ao fechar.
- `event/Hover` + `client/HoverClient` — o `Hover`, o `PacketFlyToServer` e a tecla H (`key.thaumcraft.hover`): voa
  como no criativo, a 70% da velocidade (91% com o cinturão), um ponto de Potentia a cada 360 tiques no ar (288 com o
  cinturão); sem Potentia desliga sozinho, sem o arreio no peito também. Sons `hhon`, `hhoff` e o zumbido `jacobs`.
  Pairando, quebra blocos no ar sem o castigo de 5× (`mixin/PlayerHoverMixin`, o `breakSpeedEvent`).
- **Diferença:** a conta dos tiques até gastar o próximo ponto fica no servidor, e não no arreio (no original ela
  ia no próprio item, o que no 26.2 mandaria o peito do jogador pela rede a cada tique).
- `client/render/HoverHarnessRenderer` — o `ModelHoverHarness`: a caixa do tronco com a `hoverharness.png`, o
  `hoverharness.obj` das costas com a `hoverharness2.png` sem sombreamento (`UnlitCutout`, o `glDisable(GL_LIGHTING)`)
  e, pairando, os dois anéis de raio (`lightningring.png`, 16 quadros) e as faíscas até os blocos em volta (raio de
  tipo 6: agora o `LightningBolt` tem os tipos 5 e 6, que misturam como vidro).
- O mostrador à esquerda (`renderHoverHUD`): o tubo com a Potentia do jarro, o ícone do arreio e o brilho girando.
- `item/HoverGirdleItem` — o `ItemGirdleHover`: vai no cinto, tira um terço de bloco da queda por tique.
- Os dois entram na etiqueta `runic_armor` e as receitas de infusão saem do gerador.
- A tecla era um texto fixo no original; o nome do cinturão e a frase do voo interrompido foram traduzidos aqui.

## Baús de masmorra e sacolas de tesouro (2026-09-18)

- `item/LootBagItem` — o `ItemLootBag`: tesouro comum, incomum e raro (16 por pilha). Abrir espalha de 8 a 12 coisas
  com o som `coins`; o `Utils.generateLoot` e o `genGear` vieram juntos: às vezes uma peça de armadura ou arma
  (couro a vazio, conforme a sorte), um pouco gasta e às vezes encantada; o livro sai encantado.
- `loot/ThaumLoot` é **gerado** por `scratchpad/saque.js` do `Config.initLoot` e do `Utils` descompilados: as três
  tabelas com os pesos do original (moedas, diamante, esmeralda, ouro, pérola do End, fragmentos, peças comuns,
  anéis de aprendiz, pedra de vis, anel rúnico menor, garrafas de experiência, maçãs douradas, livros, poções e a
  estrela do Nether na rara), a tabela de peças do `genGear` e o que vai nos baús do mundo.
- `loot/ChestLoot` — o `ChestGenHooks`: masmorra, templo da selva, pirâmide do deserto, mina abandonada, corredor,
  cruzamento e biblioteca da fortaleza (esta com os fragmentos de conhecimento, 3 a 6, peso 20) e o ferreiro da vila
  (táumio).
- **Diferenças:**
  - no 1.7.10 cada baú sorteava de uma lista só; aqui as coisas do Thaumcraft entram no primeiro sorteio de cada
    tabela, com o mesmo peso e quantidade. O "villageBlacksmith" virou o armeiro da vila (`village_weaponsmith`).
  - as poções do 1.7 eram metas (normal, forte, longa); no 26.2 nem toda poção tem a forte ou a longa — sem ela, fica
    a comum.
  - a pérola primordial (`itemEldritchObject` 3) da sacola rara fica para quando o Eldritch chegar.
  - as sacolas também caíam dos monstros campeões, que ainda não existem aqui.
- Os nomes das sacolas foram traduzidos aqui; o pt_BR do original não os tinha.

## A rede de vis e o comportamento completo dos nós (2026-09-18)

- `block/entity/NodeBlockEntity` agora é o `TileNode` inteiro: além de refazer (600/400/900 tiques), os nós
  vizinhos (até quatro blocos) disputam vis — o mais cheio suga um ponto do mais vazio, às vezes crescendo, com um
  raio entre os dois (`TCNetwork.BlockZap`, o `PacketFXBlockZap`); um aspecto que fica em zero vai perdendo o teto a
  cada 1200 tiques até morrer, às vezes deixando o nó mais pálido, e o nó sem aspecto nenhum some (o do tronco vira
  tora); o tempo em que o pedaço de mundo ficou descarregado é recuperado ao voltar (`lastActive`). O instável solta
  orbes de vis; o faminto puxa e fere quem chega a quinze blocos, alimenta-se do que morre nele, come os blocos em
  volta e mostra as migalhas voando (`client/fx/BoreParticle`, o `FXBoreParticles`); o maculado espalha fibras.
- `entity/AspectOrbEntity` + `client/render/AspectOrbRenderer` — o `EntityAspectOrb`: sai do nó instável, dos
  monstros mortos por alguém (`event/AspectOrbs`: metade das vezes, cada primordial de que eram feitos) e do amuleto
  primordial carregado (`item/PrimalCharmItem`); voa para a varinha da barra que tenha lugar e a enche.
- **Estabilizador de nó** (comum e avançado, `NodeStabilizerBlock` + `NodeStabilizerRenderer` com o
  `node_stabilizer.obj`): embaixo do nó e sem redstone, trava — o comum dobra o tempo de refazer e não deixa o nó
  sugar os vizinhos, o avançado multiplica por vinte; nenhum travado é sugado. Travado, o instável às vezes se acalma
  e o esmaecido volta a pálido. Os pistões abrem e a bolha envolve o nó.
- **Transdutor de nó** (`NodeConverterBlock` + renderer): em cima do nó estabilizado, com redstone, drena o nó em mil
  tiques e o transforma no **nó energizado** (`EnergizedNodeBlock`), a fonte da rede: gera por tique a raiz quadrada
  de cada primordial do nó. Tirando o sinal, volta a nó, vazio. Sem o estabilizador ou o transdutor, o energizado
  estoura. Raios e o estouro (`client/fx/Burst`, o `FXBurst`) como no original.
- `api/visnet/VisNodeBlockEntity` + `VisNet` — o `TileVisNode` e o `VisNetHandler`: a árvore de fontes e relés, cada
  relé pendurado no ponto mais perto à vista e da mesma cor.
- **Relé de vis** (`VisRelayBlock`, preso na face de um bloco): alcance de oito, cristal afinável com a varinha ou
  com um fragmento, fio de luz até o pai (`client/fx/BeamPower`, o `FXBeamPower`: quase invisível sem os óculos) que
  pisca na cor do aspecto que passa. O **amuleto de vis** e a **pedra de vis** agora se enchem perto de um relé.
- **Relé carregador** (`WorkbenchChargerBlock`): em cima da bancada arcana, enche a varinha dela com o vis da rede.
- Receitas (arcana e infusão) saem dos geradores; nomes traduzidos aqui (o pt_BR do original não os tinha).
- **Diferenças e pendências:**
  - o bioma não se pinta aqui: o nó maculado não macula o bioma, o sombrio e o puro não mudam o bioma, e um nó não
    vira maculado por estar em bioma maculado. O zumbi cerebral gigante do nó sombrio chega com as criaturas.
  - o estouro do nó energizado não espalha gosma e gás de fluxo (o fluxo ainda não existe).
  - o aspecto que morre num nó sai também do teto dele (no original ele ficava no teto sem voltar nunca); o efeito
    é o mesmo, muda só a média usada na disputa entre vizinhos.
  - a dica "@FOCUSPRIMAL" do amuleto primordial fica para o Eldritch.

## Minérios e cristais (2026-09-19)

- **Cinábrio** e **âmbar preso em pedra** (o `BlockCustomOre` 0 e 7): o cinábrio dá a si mesmo e fundido vira
  mercúrio; o âmbar dá de um a um mais a fortuna, com um a quatro de experiência, e também funde em âmbar.
- `world/ThaumOresFeature` — o `generateOres` inteiro, uma vez por pedaço de mundo: 18 blocos soltos de cinábrio da
  altura 0 à 51, 20 de âmbar até 24 abaixo da superfície e 8 veios de seis de pedra infundida (uma vez em três do
  aspecto do bioma). As alturas são as do mundo de 1.7.10, que começava no zero.
- **Correção da pedra infundida**, que tinha entrado aproximada numa fatia antiga: agora é como o
  `BlockCustomOreRenderer` — a pedra de base e, por cima, a veia animada tingida na cor do aspecto e brilhando
  (brilho 10, o 160 do original) em vez de luz de bloco; dureza 1,5 (era 3), de um a dois mais a fortuna
  fragmentos (era um só), zero a três de experiência, e a geração nos números do original (era um veio de cinco,
  três vezes por pedaço).
- **Aglomerados de cristal** (`CrystalClusterBlock` + `CrystalClusterRenderer`, o `BlockCrystal` e o
  `TileCrystalRenderer`): um por primordial e o misto, feitos de seis fragmentos. Crescem da face em que foram
  postos e caem sem apoio; luz sete, faíscas na cor deles (`client/fx/Spark`, o `FXSpark` com a
  `particles2.png`), seis fragmentos ao quebrar. As lascas saem do mesmo sorteio do original, então cada aglomerado
  tem o formato que teria lá.
- **Estabilizadores da infusão:** a matriz agora conta as cabeças e os aglomerados de cristal em volta (o
  `IInfusionStabiliser`), como no original: cada um tira um décimo da instabilidade, e o par espelhado mais.
- Com os cristais, a receita do **amuleto de vis** entrou.
- **Diferença:** a conta de fragmentos da pedra infundida é "um ou dois, mais de zero à fortuna" (o original sorteava
  de 1 a 2 + fortuna de uma vez; o intervalo é o mesmo). Os aglomerados só existem por receita, como no mundo normal
  do original (lá eles nascem na dimensão Eldritch).

## Criaturas básicas (2026-09-19)

- **Zumbi Raivoso** (`BrainyZombieEntity`, o `EntityBrainyZombie`): zumbi de 25 de vida, 5 de dano, três de armadura
  a mais, sem reforços, que revida quem o fere. Nasce onde nasce zumbi na superfície (peso 10). Larga três sorteios
  de meio a meio de carne podre, o cérebro de zumbi em (5 + pilhagem) de 10, e o raro do zumbi (ferro, cenoura,
  batata) só morto por jogador.
- **Zumbi Furioso** (`GiantBrainyZombieEntity`): 60 de vida, pula no alvo; cada golpe que leva soma um décimo de
  raiva (até dois), que o faz crescer e bater mais forte e passa devagar. Doze sorteios de duas carnes podres, o
  cérebro, e o raro dele (taumio, cenoura, batata, âmbar). Ainda só por ovo: quem o solta é o nó sombrio, que não
  pinta bioma aqui.
- A pele `bzombie.png` do original era de 64×32; foi convertida para o formato de hoje (braço e perna esquerdos
  copiados dos direitos, como o jogo faz com pele antiga).
- **Fogo-fátuo** (`WispEntity`, o `EntityWisp`): bola de vis de um aspecto (nove em dez primordiais), que vagueia
  sem gravidade e dá choques de longe (`TCNetwork.EntityZap`, o raio do `PacketFXWispZap`). Nasce no Nether (peso
  5), no escuro, até oito por perto. Morto, larga a **essência etérea** do aspecto dele.
- **Essência etérea** (`WispEssenceItem`): dois de aura mais dois do aspecto que carrega, pintada da cor dele; na
  aba do criativo vem uma de cada aspecto.
- **Morcego Infernal** (`FireBatEntity` + `FireBatModel`/`FireBatRenderer`, o `EntityFireBat`, o `ModelFireBat` e o
  `RenderFireBat`): o modelo é o do morcego do jogo de 1.7 (o de hoje mudou), a 35% do tamanho, sempre aceso,
  soltando fumaça e chama. Dorme pendurado até alguém chegar a quatro blocos, persegue quem vê a doze e, encostando,
  põe fogo, morde sem empurrar ou — uma vez em dez — explode sem quebrar bloco. Imune a fogo e explosão; água e
  chuva o afogam. Nasce no Nether (peso 10, de um a dois) e, no Dia das Bruxas, em todo lugar (peso 5). Larga de
  zero a dois de pólvora, mais a pilhagem.
- **Foco dos Nove Infernos** (`Focuses.hellbat`, o `ItemFocusHellbat`): ignis 2, perditio 1 e aer 1 por morcego,
  um por segundo; o morcego invocado sai da mão e vai atrás da criatura na mira a até 32 blocos. Invocado, morde
  com dois, não larga nada e, sem alvo, se desfaz. A receita de infusão entrou.
- **Cérebro de zumbi**: comida de 4 com 0,2 de saturação, 80% de chance de fome por 30 segundos, carne de lobo.
  Com ele entraram os aspectos dele e as receitas de infusão que só esperavam por ele: o núcleo de triagem do golem
  e a máscara do diabo sorridente.
- **Sons:** `wisp_live`, `wisp_dead` e, já para o pech, os seis dele.
- **Diferenças:**
  - comer o cérebro não dá distorção (a distorção ainda não existe).
  - as variantes bomba, diabo e vampiro do morcego estão prontas na criatura, mas só o manipulador focal as liga.
  - o morcego invocado conta o dono como quem feriu o alvo (o original só marcava "ferido recentemente", sem jogador);
    o jogo de hoje precisa do jogador para dar a experiência, então o efeito é o mesmo.
  - o zumbi raivoso nasce nos biomas onde nasce zumbi (o original: todo bioma da superfície com monstros).

## Aglomerados nativos (2026-09-20)

- **Aglomerados nativos** de ferro, cobre, ouro e cinábrio e a **gota de mercúrio** (o `ItemNugget` 5, 16, 17, 21 e 31).
  Cada aglomerado funde em dois lingotes (o de cinábrio em dois mercúrios); nove gotas fazem um mercúrio e um mercúrio
  desfaz em nove.
- No crisol, **metallum e ordo** com o minério viram o aglomerado (PUREIRON, PUREGOLD, PURECOPPER), e **metallum** com
  a pepita faz três (TRANSIRON, TRANSCOPPER). O minério de hoje tem duas caras (pedra e ardósia); as duas valem.
- **Correção:** o mapeador de itens aproximava duas pepitas — a de taumio virava o lingote e a gota de mercúrio virava
  o mercúrio. A receita do tubo volta a pedir a gota, como no original.
- **Diferença:** estanho, prata e chumbo (os aglomerados 18 a 20 e as pepitas 2 a 4) só existiam no original quando
  outro mod trazia o lingote; sem mods, ficam de fora, como lá. As pepitas de ferro e de cobre são as do próprio jogo.
  O cobre, que no original dependia de outro mod, hoje é do jogo e entra.

## Velas, biomas, vagem de mana e cogumelo-vis (2026-09-20)

- **Velas de sebo** (`TallowCandleBlock`, o `BlockCandle` e o `BlockCandleRenderer`): dezesseis cores, luz 14 (o
  0,95 do original), sem colisão, só em chão firme, fumaça e chama no pavio; estabilizam a infusão. Os pingos de sebo
  no pé saem do mesmo sorteio do renderizador original e viram variantes do modelo. Três velas de um barbante e dois
  sebos; o corante pinta a branca e a farinha de osso branqueia qualquer uma.
- **Biomas** (`TCBiomes`, dados em `data/thaumcraft/worldgen/biome`): **Floresta Mágica**, **Terra Maculada** e
  **Sinistro**, com as cores, as criaturas e as decorações do `BiomeGenMagicalForest`, do `BiomeGenTaint` e do
  `BiomeGenEerie` (árvores, flores, mato, nenúfares, cogumelos, pedras com musgo, cogumelos gigantes, vagens de mana,
  cogumelos-vis, fibras e manchas de mácula, nos números do `BiomeDecorator` de cada um). A árvore grande da floresta
  é o `WorldGenBigMagicTree` (o carvalho grande de 2014, de 11 a 22 de altura).
- **Onde nascem** (`mixin/OverworldBiomeBuilderMixin`): o mundo de hoje escolhe bioma por clima, e não por peso. A
  Floresta Mágica fica com metade das florestas frias e temperadas; a Terra Maculada, com metade das planícies frias
  — perto da proporção de pesos do original (5 e 2). O Sinistro só aparece onde um nó sombrio pinta.
- **Pintar bioma** (`world/BiomePainter`, o `Utils.setBiomeAt`): troca o bioma de uma coluna e manda para quem está
  vendo, como o `/fillbiome`. Com isso os nós voltaram a fazer o que o original faz: o **maculado** pinta Terra
  Maculada em volta, o **sombrio** pinta Sinistro e chama zumbis furiosos (até três por perto, com alguém a 24 blocos),
  o **puro** devolve a Floresta Mágica à Terra Maculada (e o do pinheiro-de-prata pinta Floresta Mágica em volta).
  Um nó comum na Terra Maculada vira maculado uma vez em quinhentas; e nasce maculado metade das vezes, com mais aura.
- **Correção da tabela de aura** (`world/BiomeAura`): tinha só 12 dos 31 tipos do original. Agora os 31 vêm do jar,
  com os tipos do dicionário de biomas do Forge nas marcas de convenção do Fabric (quente, frio, úmido, seco, mágico,
  assustador...). Muda a aura e o aspecto dos nós e dos veios de pedra infundida conforme o bioma.
- **Vagem de mana** (`ManaPodBlock` + `ManaPodRenderer`) e **feijão de mana** (`ManaBeanItem`): a vagem pendura
  embaixo de tora em bioma mágico, cresce até sete (uma vez em trinta), brilha do tamanho que tem e, no três, escolhe
  o aspecto das vizinhas e das misturas delas; dá feijões. O feijão (meio segundo de comer, mesmo sem fome) dá um
  efeito ao acaso da lista de poções de 1.7 e, uma vez em quatro, um ponto de pesquisa do aspecto; plantado embaixo de
  uma tora em bioma mágico, vira vagem.
- **Cogumelo-vis** (`VishroomBlock`): luz oito, chaminha roxa, e quem encosta fica tonto por dez segundos.
- **Diferenças:**
  - o bioma se pinta de quatro em quatro blocos (é como o jogo de hoje guarda bioma); no original era coluna a coluna.
  - os nomes em português dos biomas são tradução do porte (o original não tinha).
  - a mácula ainda se alastra pela regra antiga do porte; a de verdade, que depende do bioma, chega com a fauna da
    mácula e o fluxo.

## Pech (2026-09-20)

- **Pech** (`PechEntity` + `PechModel`/`PechRenderer`, o `EntityPech`, o `ModelPech` e o `RenderPech`): 30 de vida, 6
  de dano, rápido, dois de armadura a mais; foge de gente enquanto não é manso, abre portas, cata do chão o que é de
  valor e o que couber na mochila de nove casas (menos o que ele mesmo largou numa troca). Três tipos pelo que nasce
  na mão: o **coletor** (briga de perto), o **mago** (varinha com o foco dos pechs, rajadas) e o **caçador** (arco).
  Quem fere um pech arruma briga com todos a 32 blocos (bravos de 400 a 800 tiques); ele resmunga mexendo o queixão.
  Nasce na Floresta Mágica e no Sinistro (menos de quatro por perto). Morto, larga quase tudo da mochila, feijões de
  mana, às vezes uma moeda e, raro, um fragmento de conhecimento.
- **Troca** (`PechMenu` + `PechScreen`, o `ContainerPech` e o `GuiPech`): o item de valor (ouro, pérola, diamante,
  esmeralda, maçã dourada, feijão de mana, ou qualquer coisa com Lucrum) come o pech e pode deixá-lo manso; manso, o
  clique abre a troca, e o botão dos dados vira o valor do item em coisas da mochila, da tabela do tipo dele
  (`PechTrades`, com os números do jar) e, nos valores altos, tesouros de masmorra. De vez em quando a amizade acaba.
- **Rajada** (`PechBlastEntity`, o `EntityPechBlast`): cai de leve e, onde bate, fere tudo a dois blocos (menos pechs)
  com veneno, lentidão ou fraqueza.
- **Foco dos pechs** (o `ItemFocusPech`): terra, perditio e aqua 10 por rajada, quatro por segundo.
- **Diferenças:**
  - os livros de Pressa e de Reparo da tabela do mago chegam com os encantamentos do Thaumcraft; as pepitas de estanho,
    prata e chumbo não existem sem mods.
  - o tesouro de masmorra é a lista do baú de 2014 com peso até cinco e uma unidade (maçã dourada, dois discos, as três
    armaduras de cavalo, livro encantado), sorteada por igual.

## Porta arcana, chaves, placa de pressão arcana, ouvido arcano e vidro protegido (2026-09-20)

- **Porta arcana** (`ArcaneDoorBlock`, o `BlockArcaneDoor`): porta com dono (quem pôs); só o dono e quem tem chave
  abrem com a mão, os outros levam "a porta se recusa a abrir" e o som dela emperrada. A redstone não a mexe; a placa
  de pressão arcana ao lado, de alguém que a porta conhece, abre quando liga e fecha quando desliga. Dura (15),
  explosão, wither e dragão não a quebram.
- **Chaves** (`KeyItem`, o `ItemKey`): de ferro e de ouro. Em branco, o dono grava a chave para aquela porta ou placa
  (a de ouro de outro também grava as de ferro); gravada, brilha e põe quem a usa na lista (a de ferro só abre; a de
  ouro também dá acesso a outros e mexe na placa). As mensagens são as do original.
- **Placa de pressão arcana** (`ArcanePressurePlateBlock`): dispara com tudo, com tudo menos o dono e as chaves, ou só
  com eles (o dono troca com a mão; a cara muda, applate1 a 3). Sinal forte embaixo; imune a explosão e chefão.
- **Ouvido arcano** (`ArcaneEarBlock`, o `TileSensor`): afinado como bloco musical (a mão sobe a nota e ele toca, com o
  instrumento do bloco de baixo); ouve a até 64 blocos um bloco musical do mesmo instrumento tocando a mesma nota e dá
  meio segundo de sinal. As notas chegam por um gancho no bloco musical (`mixin/NoteBlockMixin`), como o
  `NoteBlockEvent` do original.
- **Vidro protegido** (`WardedGlassBlock` + `WardedGlassModel`): com dono, duro (5), imune a explosão e chefão; a textura
  emenda com o vizinho pela tabela de 47 quadros do `UtilsFX` (a mesma conta de vizinhos do original, trocando a
  textura de cada face na hora de desenhar); batido, mostra o escudo.
- **Diferenças:**
  - o ouvido usa os instrumentos do bloco musical de hoje (o original tinha cinco: harpa, bumbo, caixa, chimbal e baixo;
    os mesmos blocos de baixo dão os mesmos cinco).

## Espelhos (mágico, de essência e de mão)

Fonte: `BlockMirror`, `BlockMirrorItem`, `TileMirror`, `TileMirrorEssentia`, `TileMirrorRenderer`, `ItemHandMirror`,
`ContainerHandMirror`, `GuiHandMirror`, `InventoryHandMirror` e o `EssentiaHandler` (descompilados do jar).

- **Bloco** (`MirrorBlock`): os números 0–5 e 6–11 do original viraram dois blocos, `mirror` e `essentia_mirror`, com
  `facing` nos seis lados (a face em que se clicou). Placa de 1/16 colada no apoio, sem colisão, dureza 1, resistência
  10, som do jarro a 0,5 de volume e tom 2. Cai quando o bloco de trás sai. Ao quebrar (até no criativo, como o
  `onBlockHarvested` do original), o espelho ligado cai lembrando o par e o par deixa de estar ligado.
- **Ligação** (`LinkedMirrorBlockEntity`): o código que o original repete nas duas entidades ficou numa só —
  `restoreLink`, `invalidateLink`, `isLinkValid`, `isLinkValidSimple`, `isDestinationValid` e a tentativa de religar a
  cada 40 tiques, espaçando 20 a mais por falha até 600. O `linkDim` numérico virou o nome do mundo.
- **Espelho mágico** (`MirrorBlockEntity`): item que encosta na casa (a casa inteira, como no original) vai para a fila
  do par; o par cospe um por vez depois do primeiro segundo, no ritmo `(instabilidade / 50)²`, do vidro para a frente a
  0,15, e o item só volta a entrar num espelho 20 tiques depois (o `timeUntilPortal` do original é o
  `portalCooldown`). Cada item soma um de instabilidade; ela cai um por segundo e com Ordo da rede de vis. É um
  inventário de uma casa que nunca guarda: funil que põe nele manda direto para o par. Evento 1: a fumaça escura.
- **Espelho de essência** (`EssentiaMirrorBlockEntity`): fonte de essência para quem chama (a matriz de infusão), uma
  unidade por vez, tirada dos recipientes na caixa à frente do par (`EssentiaSources.drainFacing`, o `getSources` com
  direção: 17 × 17 de largura e 8 de fundo), sem contar outros espelhos de essência.
- **Espelho de mão** (`HandMirrorItem`, `HandMirrorMenu`, `HandMirrorScreen`): clicado num espelho mágico guarda onde
  ele está (e brilha); com ele na mão, abre a casa do meio da `guihandmirror.png` — o que se põe nela sai pelo
  espelho ligado, com o som do enderman a 0,1. Sem o espelho no lugar, a ligação se desfaz com o zap.
- **Visual**: a moldura é o `renderItemIn2D` do original (a textura extrudada 1/16) feito modelo de bloco — frente e
  verso inteiros e uma faixa por borda de pixel, geradas da textura pelo `Espelho.java` do scratchpad. O vidro
  (`MirrorRenderer`) fica a 0,02 da parede: prateado sem par; com par, o céu de estrelas do buraco portátil recuado
  3/16 de cada lado e o vidro quase transparente por cima; no mágico instável ele treme para fora
  (`instabilidade / 10000`). O item é a moldura com o vidro (`mirrorpaneopen` quando ligado).
- **Receitas**: as três infusões saem do gerador (`mapa-itens.js` agora conhece `blockMirror` e `itemHandMirror`).
- **Testes**: `MirrorGameTest` (ligar pelo item e atravessar três itens, quebrar lembrando o par, cair sem parede,
  funil, essência do outro lado, espelho de mão, as três infusões) e `MirrorClientTest` (parede com espelho sem par,
  par ligado e de essência; espelho no chão).
- Diferença conhecida: o fio de essência ainda é o de partículas que a matriz já usava, não o `EssentiaSourceFX`.

## Fornalha infernal

Fonte: `BlockArcaneFurnace`, `BlockArcaneFurnaceRenderer`, `TileArcaneFurnace`, `TileArcaneFurnaceNozzle`,
`WandManager.createArcaneFurnace/fitArcaneFurnace/replaceArcaneFurnace`, `BlockUtils.isBlockTouchingOnSide` e o
`addSmeltingBonus` do `ConfigRecipes` (descompilados do jar).

- **Formação** (`InfernalFurnaceStructure`): a varinha numa obsidiana, tijolo do Nether ou grade de ferro, com a pesquisa
  INFERNALFURNACE, procura o cubo 3 × 3 × 3 (cantos de tijolo, meio das bordas de obsidiana, lava no centro, o alto do
  centro vazio e exatamente uma grade no meio de uma parede da camada do meio) e gasta 50 de Ignis e 50 de Terra. Cada
  bloco vira a parte da posição dele (`PART` 1 a 9, linha a linha do noroeste, em cada camada; 0 o centro; 10 a boca,
  que guarda para que lado fica o centro). Os blocos são postos sem avisar os vizinhos, senão o centro se desfaria ao
  ver o cubo pela metade.
- **Bloco** (`InfernalFurnaceBlock`): dureza 10, resistência 300 (os 500 do original na conta de hoje), luz 3 (13 no
  centro e na boca), picareta. Colisão: o centro tem um quarto de altura e a boca, a metade do lado do centro. Itens que
  pousam na lava do centro entram na fornalha; bichos que não aguentam fogo tomam 3 de lava e pegam fogo. Fumaça grossa
  sai pelo alto aberto. Faltando um bloco em volta do centro, tudo volta a ser o que era (o laço do original pula os
  blocos ao sul do centro e do meio de cima, e aqui também). Do centro quebrado sai um blaze com Regeneração III e
  Resistência. Cada parte deixa o bloco que era.
- **Fornalha** (`InfernalFurnaceBlockEntity`): 32 casas; funde uma unidade por vez, 140 tiques (80 acelerada), 20 a
  menos por fole a dois blocos do centro, virado para ele e sem sinal (até três). Acelera com Ignis da rede de vis (5 de
  cada vez) ou pelos bicos. O que não funde se desfaz com um chiado. O fundido sai pela boca a 0,13, com a experiência
  da receita e o bônus de fundição (sem fole, 1 em 4 de um; com foles, 44% por fole).
- **Bicos** (`InfernalFurnaceNozzleBlockEntity`): os blocos do meio das paredes e o de baixo, encostados no centro,
  aceitam cano por fora e puxam Ignis com força 128 quando a pressa acaba; cada unidade dá 600 tiques.
- **Bônus de fundição** (`SmeltingBonus`, gerado por `scratchpad/bonus-fundicao.js`): os minérios de ouro, ferro,
  cobre e cinábrio e os aglomerados nativos. O minério de hoje cai bruto, e o bruto entra junto com o bloco. Estanho,
  prata e chumbo não existem no jogo de hoje; as pepitas de carne entram quando as pepitas chegarem.
- **Visual**: as 25 texturas `furnaceN` do jar; as paredes usam o `calculateTexture` do original, portado inteiro
  (`InfernalFurnaceModel`, como o vidro protegido): cada face mostra o seu pedaço do desenho grande, e a face da boca
  ganha a moldura (o "tocando no lado" do original olha os oito vizinhos no plano da face). A boca desenha, na própria casa e
  virados para fora, a grade a 0,625 da borda de fora, os olhos a 0,8 e o fogo a 0,9 com 1,5 de altura; atrás deles, o
  cubo de lava do centro. (No MCP do 1.7.10, `func_147764_f` é a face X+ e `func_147798_e` a X−: lidas trocadas, as
  faces caíam dentro do centro.) Conferido contra a imagem do bloco na wiki do FTB.
- **Testes**: `InfernalFurnaceGameTest` (o cubo precisa de uma grade só, a numeração, o que cai na lava sai fundido
  pela boca, o que não funde some, quebrar desfaz, a tabela de bônus) e `InfernalFurnaceClientTest`.
- Armadilha: o original soma posição inteira com `float`; nas coordenadas enormes dos testes isso arredondava a saída
  para dentro do centro, e o lingote voltava para a lava. Aqui as contas de posição são em `double`.
- Diferença: a gota de lava que espirra pela boca é a partícula de lava do jogo, que não aceita o empurrão do original.

## Fornalha alquímica avançada, construção alquímica avançada e reservatório de essência

Fonte: `BlockAlchemyFurnace`, `TileAlchemyFurnaceAdvanced`, `TileAlchemyFurnaceAdvancedNozzle`,
`TileAlchemyFurnaceAdvancedRenderer`, `WandManager.createAdvancedAlchemicalFurnace`, o aparelho de metal 3,
`BlockEssentiaReservoir(Item/Renderer)`, `TileEssentiaReservoir(Renderer)` e `FXSlimyBubble` (descompilados do jar).

- **Construção alquímica avançada**: o aparelho de metal 3, um cubo com a `alchemyblockadv`. A receita arcana pede a
  pérola primordial, que chega com o Eldritch; o gerador a pega sozinho quando ela existir.
- **Formação** (`AdvancedAlchemicalFurnaceStructure`): a varinha numa construção alquímica (comum ou avançada), com a
  pesquisa ADVALCHEMYFURNACE, procura uma fornalha alquímica a até um bloco; embaixo ela tem de estar cercada das oito
  construções avançadas e em cima haver alambiques nos cantos e construções nos lados. Gasta 50 de Ignis, Aqua e Ordo,
  e cada bloco faísca numa cor qualquer (a cor -9999 do original, que as faíscas agora entendem).
- **Bloco** (`AdvancedAlchemicalFurnaceBlock`): as partes com o número do original (0 o meio, 1 os bicos, 4 os cantos
  de baixo, 3 os lados e 2 os cantos de cima); nada se desenha sozinho. O meio tem 0,7 de altura para o que não é bicho
  (os itens caem nele) e se acende com o calor. Tirar uma parte faz o meio desmontar tudo no tique seguinte; cada parte
  volta a ser a peça que era. O comparador nos bicos dá só 0 ou 1: a conta do original,
  `floor(r * 14) + vis > 0 ? 1 : 0`, pela precedência, é isso.
- **Fornalha** (`AdvancedAlchemicalFurnaceBlockEntity`): a cada cinco tiques bebe da rede de vis até 50 de Ignis
  (calor), Perditio e Aqua, até 500 de cada. O item que cai é desfeito se houver o dobro do tamanho dele em calor e o
  tamanho em cada força; depois ela descansa `5 + (1 - calor/500) × 100` tiques. Guarda até 500 de essência.
- **Bicos** (`AdvancedAlchemicalFurnaceNozzleBlockEntity`): soltam para o cano de fora a essência do meio (o primeiro
  aspecto guardado).
- **Visual** (`AdvancedAlchemicalFurnaceRenderer`): o `adv_alch_furnace.obj` do jar pelo `ObjModel` — a base (acesa
  acima de 100 de calor) e os quatro tanques (acesos com essência); a gosma de fluxo na boca e nas janelas dos tanques
  na altura do que está cheio; o fogo nas quatro grelhas subindo com o calor. As bolhas roxas saem pelo `SlimyBubble`
  (o `FXSlimyBubble`, quadros 144 a 150 da `particles.png`).
- **Reservatório** (`EssentiaReservoirBlock`/`EssentiaReservoirBlockEntity`): 256 de qualquer mistura; puxa uma unidade
  a cada cinco tiques do cano do bocal com força 24 e solta pelo mesmo bocal. Posto, o bocal vira para o bloco clicado;
  a varinha o vira para longe da face batida (agachado, para ela) — para isso a varinha agora diz aos blocos em que
  face bateu. Comparador `floor(r × 14) + (tem essência ? 1 : 0)`. Range de vez em quando (o som `creak`), mais quanto
  mais cheio. O líquido muda de cor passando pelos aspectos guardados. Quebrado com essência, estoura sem quebrar blocos.
  A infusão sai do gerador.
- **Pendente**: o fluxo que o reservatório quebrado derrama (a gosma e o gás) chega com a fatia do fluxo; o lugar já
  chama `Flux.spill`.
- **Testes**: `AdvancedAlchemyGameTest` (o molde, a numeração, o item desfeito e o bico, desmontar, o reservatório, a
  varinha no bocal, a infusão) e `AdvancedAlchemyClientTest`.

## Taumatório, matriz mnemônica e grade de itens

Fonte: `TileThaumatorium`, `TileThaumatoriumTop`, `ContainerThaumatorium`, `GuiThaumatorium`,
`TileThaumatoriumRenderer`, `TileBrainbox`, `TileGrate`, `EntityItemGrate`, os números 5, 6, 10, 11 e 12 do
`BlockMetalDevice`/`BlockMetalDeviceItem`/`BlockMetalDeviceRenderer` e o `WandManager.createThaumatorium`
(descompilados do jar).

- **Formação** (`ThaumatoriumStructure`): a varinha numa de duas construções alquímicas empilhadas sobre um crisol,
  com a pesquisa THAUMATORIUM, gasta 15 de Ignis, 30 de Ordo e 30 de Aqua; o taumatório fica virado para a face batida.
  Na mesma peça, esse gatilho vem antes do da fornalha avançada, como no original.
- **Bloco** (`ThaumatoriumBlock`): as duas metades (`TOP`), invisíveis — o `thaumatorium.obj` inteiro sai da de baixo.
  A mão (sem agachar) abre a tela. Sem o crisol embaixo ou sem a outra metade, cada uma volta a ser construção
  alquímica (e o catalisador cai).
- **Taumatório** (`ThaumatoriumBlockEntity`): funciona com fogo, lava ou nitor debaixo do crisol e sem redstone; a cada
  cinco tiques escolhe, entre as receitas marcadas, a que o catalisador fecha, puxa com força 128 o aspecto que falta
  (dos canos dos lados e de cima, nas duas metades, menos pela frente) e, completo, gasta um catalisador e solta o
  resultado pela frente — num inventário encostado (se ele não tem lugar, espera) ou no chão, com o vapor. A metade
  de cima (`ThaumatoriumTopBlockEntity`) só repassa canos e funis. As receitas guardadas são as de crisol, pelo número
  de cada uma (`CrucibleRecipe.hash`, feito do que a receita é, não da posição na tabela).
- **Tela** (`ThaumatoriumMenu`/`ThaumatoriumScreen`): a `gui_thaumatorium.png`; as receitas que o jogador já pesquisou e
  que aceitam o catalisador, mais as marcadas; setas para passar, clique no resultado para marcar/desmarcar (som
  `hhon`), barrinhas do quanto já entrou de cada aspecto e o "marcadas/cabem" com matrizes.
- **Matriz mnemônica** (`MnemonicMatrixBlock`): a caixa de 3/16 a 13/16 com o pino para o bloco em que foi posta; cai
  sem ele. Cada uma encostada no taumatório (dos lados ou em cima, nas duas metades) com o pino nele dá duas receitas a
  mais. A receita arcana sai do gerador.
- **Grade de itens** (`ItemGrateBlock`/`ItemGrateBlockEntity`): a chapa de 13/16 a 16/16; aberta, os itens atravessam e
  um funil em cima a usa como inventário (o item sai logo abaixo, descendo); fechada, é chão para tudo. A mão ou a
  redstone abre/fecha, com o som da porta. Receita de bancada do jar: grade de ferro sobre alçapão. As nervuras de
  metal por baixo da chapa são as faces que o renderizador do original desenha dentro do bloco.
- O `EntityItemGrate` (o item que não é empurrado para fora de dentro de uma grade) virou item comum: aberta, a grade
  não segura item nenhum, e a diferença só aparece com a grade fechada em cima de um item.
- **Testes**: `ThaumatoriumGameTest` (os números das receitas, a formação, puxar do reservatório e fazer alumentum, a
  matriz, a metade de cima, desmontar, a grade) e `ThaumatoriumClientTest` (o bloco e a tela).

## Melhorias de foco

Porte do `FocusUpgradeType`, do `ItemFocusBasic` (postos, `getPossibleUpgradesByRank`, `canApplyUpgrade`,
`applyUpgrade`, `getVisCost`, `getActivationCooldown`), das melhorias usadas por cada foco (`ItemFocusFire`, `Frost`,
`Shock`, `Excavation`, `PortableHole`, `Trade`, `Pech`, `HellBat`, `Primal`), do `WandManager.setCooldown`, do
`EntityExplosiveOrb`, `EntityShockOrb`, `RenderExplosiveOrb`, `RenderElectricOrb` e do número 10 do `BlockAiry`
(descompilados do jar).

- **Tabela** (`FocusUpgradeTable`, gerada por `scratchpad/melhorias-foco.js`): as 21 melhorias com número, ícone
  (`textures/foci/`) e aspectos, e o que cabe em cada um dos cinco postos de cada foco. No foco, os cinco postos ficam
  em `thaumcraft:focus_upgrades` (-1 = vazio); preso na varinha, a cópia vai junto e volta com ele.
- **Regras** (`FocusItem.canApply`): fogo alquímico só uma vez na bola de fogo; o raio só amplia com relâmpago em cadeia
  ou choque de terra; a proteção só amplia com arquiteto; morcegos vampiros pedem a pesquisa VAMPBAT.
- **Custo, espera e jeito de disparar** mudam como no original (bola de fogo 66 Ignis + 33 Perditio, 1 s, tiro único;
  jato de fogo; estilhaços e rocha de gelo; relâmpago em cadeia; choque de terra 75 Aer + 25 Terra, 1 s; toque suave e
  radiestesia na escavação; beladona no Pech; bombas e diabos nos morcegos). Frugal tira 10% por nível; potência,
  tesouro, ampliar e prolongar entram em cada foco como no original (a haste com runas dá +1 de potência).
- **Espera**: a do original, por criatura e em milissegundos (`WandItem.isOnCooldown/setCooldown/cast`); o jato zera a
  espera ao começar.
- **Radiestesia** (`SpecialMining`, gerada por `scratchpad/mineracao-especial.js` do `Config`): o minério (e o bruto de
  hoje) às vezes sai como aglomerado nativo.
- **Bola de fogo** (`ExplosiveOrbEntity`) e **choque de terra** (`ShockOrbEntity`), com os desenhistas do original
  (`FocusOrbRenderers`); o choque deixa **campos estáticos** (`SparkFieldBlock`): invisíveis, sem colisão, 1–2 de dano
  mágico e lentidão para quem passa, somem sozinhos.
- O custo do original para toque suave/radiestesia fica numa variável da classe, dividida entre todos os focos do mesmo
  tipo (o primeiro que pergunta define o de todos); aqui cada foco tem o seu.
- **Testes**: `FocusUpgradeGameTest` (tabela, postos, regras, custos e esperas, frugal, a melhoria viajando com o foco,
  o choque de terra, o campo estático, a radiestesia) e `FocusUpgradeClientTest` (os orbes e o campo).

## Manipulador focal

Porte do `TileFocalManipulator`, `ContainerFocalManipulator`, `GuiFocalManipulator`, `TileFocalManipulatorRenderer` e do
número 13 do `BlockStoneDevice` (descompilados do jar).

- **Bloco** (`FocalManipulatorBlock`): a mesa do `ModelArcaneWorkbench` com a `wandtable.png` (a mesma peça especial da
  bancada, no chão e na mão), pedra 3/25. Só abre para quem tem FOCALMANIPULATION ("Pesquisa requerida em falta!").
  Receita arcana do jar (a laje de pedra arcana entrou no mapeador de itens).
- **Mesa** (`FocalManipulatorBlockEntity`): a melhoria vai no primeiro posto vazio; custa `posto × 8` níveis de
  experiência (mesmo no criativo é preciso tê-los; só não são cobrados) e 200 centésimos de cada aspecto da melhoria,
  dobrando por posto, reduzidos a primários (`costOf`). A cada cinco tiques puxa até 100 de cada da rede de vis; no fim
  aplica a melhoria (som `wand`). Tirar o foco no meio perde tudo (som `craftfail`). O foco gira em cima da mesa.
- **Tela** (`FocalManipulatorScreen`): a `gui_wandtable.png` de 192×233; as melhorias postas em cima, as que cabem no
  próximo posto embaixo (clicar escolhe/desescolhe), o custo em primários, a experiência (vermelha se falta), a barra
  colorida do quanto falta puxar e as estrelinhas que correm da barra ao posto. Os textos de ajuda saem no quadro preso
  à direita, como o `drawHoveringTextFixed`.
- **Testes**: `FocalManipulatorGameTest` (custo por posto, experiência e postos, puxar da rede até a melhoria entrar) e
  `FocalManipulatorClientTest` (a mesa trabalhando e a tela).

## Arquiteto

Porte do `IArchitect`, do `getArchitectBlocks`/`showAxis` dos focos de troca e de proteção, do `WandManager.toggleMisc`
e `getAreaX/Y/Z/Dim`, da tecla G do `KeyHandler` (`PacketItemKeyToServer`, número 1) e do `handleArchitectOverlay` do
`REHWandHandler` (descompilados do jar).

- **Área** (`Architect`, componente `thaumcraft:wand_area`): x, y, z e a dimensão escolhida, guardados na varinha; sem
  mexer (ou acima do máximo do foco), valem o máximo — 3 + 2 por ampliação na troca, 3 + 1 na proteção.
- **Tecla G** (`ArchitectKey`, "Alternância da varinha"): de pé, cresce a dimensão escolhida (todas, ou uma) e volta a
  zero depois do máximo; agachado, troca a dimensão (a troca não tem a terceira).
- **Troca**: com arquiteto, cada bloco igual à mostra no plano da face, dentro da área, vira um trocador que não se
  espalha. **Proteção**: sempre pela lista do arquiteto (sem a melhoria, a área é zero: só o bloco da mira); cada bloco
  paga o seu vis e para quando acaba; desfazer pega os protegidos do mesmo dono.
- **Prévia** (`ArchitectOverlay` + `shaders/core/architect`): cada bloco da área ganha a casca do vidro protegido,
  ligada entre os blocos, azulada, piscando e somando luz — com o `GL_ADD` do original (a cor soma com a textura) —, vista
  através de tudo; no bloco da mira, as setas (`architect_arrows.png`) das dimensões que a tecla muda. O contorno comum
  some enquanto a prévia aparece.
- **Testes**: `ArchitectGameTest` (a tecla, os blocos da troca, a proteção da parede inteira) e `ArchitectClientTest`
  (a prévia no chão e na parede).

## Broca arcana

Porte do `TileArcaneBore`, `TileArcaneBoreBase`, `ContainerArcaneBore`, `GuiArcaneBore`, `TileArcaneBoreRenderer`,
`TileArcaneBoreBaseRenderer`, `ModelBore`, `ModelBoreBase`, `ModelBoreEmit`, `FXBeamBore`, `FXBoreSparkle`,
`PacketBoreDig` e dos números 4 e 5 do `BlockWoodenDevice`/`BlockWoodenDeviceItem` (descompilados do jar).

- **Base** (`ArcaneBoreBaseBlock`): o bico nasce do lado oposto ao que o jogador olha; a varinha o vira para a face
  batida. Puxa Perditio pelos canos (força 128, menos pelo bico).
- **Broca** (`ArcaneBoreBlock`): só em cima ou embaixo de uma base (o `canPlaceItemBlockOnSide`); cava para onde o
  jogador estava (como o pistão); a varinha a vira; a mão abre a tela; sem a base, cai com o que tem dentro. A caixa
  vai um bloco além, para onde cava, como no original.
- **Trabalho** (`ArcaneBoreBlockEntity`): com redstone (nela ou na base), foco de escavação e picareta que não esteja
  por quebrar, percorre a espiral de raio 2 + ampliar em volta do eixo e cava o primeiro bloco sólido de cada ponto,
  até 64 de fundo; o tempo de cada bloco é `max(10 − velocidade, dureza × 2 − velocidade × 2)`, quatro vezes mais sem
  Perditio (da rede de vis ou de canos na base). Colhe com a sorte (tesouro do foco ou fortuna da picareta) ou a seda,
  junta os itens soltos em volta, refina com a radiestesia, manda tudo para um inventário encostado no bico da base
  (ou cospe pelo bico) e gasta 1 da picareta. Com uma lâmpada arcana encostada na base, deixa luzes pelo túnel.
- **Visual**: os modelos do original (`bore.png`, `jar.png`, `vortex.png`), o corpo girando e inclinando para o bloco
  da vez, os dois fachos (`beam1` verde e `beam2` laranja), as migalhas do bloco e as faíscas verdes voando até o bico
  (o bloco da vez e o som do que saiu chegam pelo `TCNetwork.BoreDig`, o `PacketBoreDig`).
- **Tela** (`ArcaneBoreScreen`): as duas casas, o aviso de picareta por quebrar e a largura, a velocidade e as outras
  propriedades (os textos, que o original escreve em inglês fixo, foram para as línguas).
- **Falta aqui**: a
  picareta do núcleo elemental dando radiestesia (espera as ferramentas elementais).
- **Receitas** do jar: a base na bancada arcana, a broca na infusão (as peças 4 e 5 entraram no mapeador de itens).
- **Testes**: `ArcaneBoreGameTest` (cavar e encher o baú, parada sem redstone/picareta, o que o foco dá, cair sem a
  base) e `ArcaneBoreClientTest` (trabalhando, parada e a tela).

## Pedestal de recarga e foco composto

Porte do `TileWandPedestal`, `TileWandPedestalRenderer` e dos números 5 e 8 do `BlockStoneDevice` e do
`BlockStoneDeviceRenderer` (descompilados do jar).

- **Pedestal** (`WandPedestalBlock`/`WandPedestalBlockEntity`): os três degraus de pedra do renderizador do original
  (modelo de bloco com as caixas e as texturas por lado); a mão põe a varinha ou o amuleto de vis e tira o que está em
  cima (jogado aos pés de quem tocou, com o estalo). A cada cinco tiques, bebe um ponto de um nó a até oito blocos,
  do primeiro aspecto em que ainda cabe; deixa sempre um no nó, menos com a ponta de ferro ou a haste de madeira. O
  comparador lê de 1 a 15 o quanto a varinha está cheia.
- **Foco composto** (`RechargeFocusBlock`): a cruz de pedra de sete dezesseis avos; posto sobre o pedestal, ele também
  quebra os aspectos compostos do nó nos primários (um ponto do composto vira um ponto de um primário dele). Tocá-lo é
  tocar o pedestal.
- **Visual** (`WandPedestalRenderer`): a varinha girando sobre a coluna, subindo e descendo, e a linha ondulante até o
  nó, na cor do aspecto que está bebendo.
- **Receitas** de infusão do jar (as peças 5 e 8 entraram no mapeador de itens).
- **Testes**: `WandPedestalGameTest` (bebe do nó, comparador; compostos só com o foco) e `WandPedestalClientTest`.

## Spa arcano, sais de banho e fluidos

Porte do `TileSpa`, `ContainerSpa`, `GuiSpa`, do número 12 do `BlockStoneDevice`, do `BlockFluidPure`, do
`BlockFluidDeath`, do `ItemBathSalts`, dos baldes, do `PotionWarpWard`, do `DamageSourceThaumcraft.dissolve` e dos
trechos `itemExpire` e `livingDrops` do `EventHandlerEntity` (descompilados do jar).

- **Fluidos** (`fluid/ThaumFluid`, `PurifyingFluid`, `LiquidDeathFluid`, `TCFluids`): o fluido que corre do jogo de
  hoje, com as texturas animadas do original. O **purificante** (luz 10, anda a cada 5 tiques, 8 níveis) dá ao jogador
  que entra numa fonte a **proteção contra a dobra** (`TCEffects.WARP_WARD`, o ícone recortado da `potions.png`) por
  `min(32000, 200000 / √dobra)` tiques e a fonte some; borbulha branco. A **morte líquida** (luz 8) dissolve o que vive
  — 1 de dano por nível, até 4 — e quem morre dissolvido solta cristais de essência dos aspectos dele; borbulha roxo.
  O fluido finito do Forge não existe hoje: a morte líquida corre perdendo dois níveis por bloco. Nenhum forma fonte.
- **Baldes** dos dois e **sais de banho**: soltos, os sais duram dez segundos; se acabam numa fonte de água, ela vira
  purificante (no jogo de hoje o item boia, então vale também a fonte logo abaixo dele).
- **Spa** (`ArcaneSpaBlock`/`ArcaneSpaBlockEntity`): tanque de cinco baldes (aberto aos canos de fluido do Fabric, menos
  por cima) e a casa dos sais; um recipiente na mão despeja no tanque; a mão vazia abre a tela. A cada 40 tiques, sem
  redstone, verte um balde por cima — misturando, água + sal vira purificante; sem misturar, o fluido do tanque — e,
  com o bloco de cima já cheio, numa casa vizinha encostada (até dois blocos). A água não sai onde evapora.
- **Tela** (`ArcaneSpaScreen`): a casa, o botão de misturar e o tanque com o fluido desenhado.
- **Receitas** do jar: o spa (arcana), os sais e o balde de morte líquida (crisol).
- **Testes**: `SpaGameTest` (purificante pelo spa, só o fluido, a proteção contra a dobra, a morte líquida, os sais na
  água) e `SpaClientTest`.

## Encantamentos do Thaumcraft e infusão de encantamento

Porte do `EnchantmentHaste`, `EnchantmentRepair`, do `updateSpeed`/`doRepair` do `EventHandlerEntity`, do
`WandManager.consumeVisFromInventory`, do `InfusionEnchantmentRecipe` e do caminho de encantamento da
`TileInfusionMatrix` (descompilados do jar).

- **Dados** (`data/thaumcraft/enchantment`): Pressa (peso 3, até III, custo 15+9(n−1), botas e o arreio) e Reparo (peso
  2, até II, custo 20+10(n−1), as coisas que o original marca `IRepairable` — a marca `thaumcraft:repairable` —, não
  convive com Inquebrável). Os dois entram na mesa de encantamento, nos livros e nas trocas. As ferramentas e armaduras
  do mod entraram nas marcas do jogo (`pickaxes`, `foot_armor`...), para aceitar encantamentos e servirem na broca.
- **Pressa** (`event/Enchantments.haste`): andando para a frente, fora do voo, um empurrão de 1,5% por nível (metade no
  ar, metade na água), do lado de quem anda. No arreio, cada nível dá 0,075 à velocidade do pairar.
- **Reparo** (`event/Enchantments`): a cada dois segundos, cada coisa marcada, gasta, no inventário ou vestida (menos o
  arreio no inventário), conserta um ponto por nível pagando em vis — a raiz do dobro de cada primário dela, vezes o
  nível — de um amuleto de vis vestido ou de uma varinha (da última casa para a primeira). A broca arcana conserta a
  picareta pela rede de vis, do mesmo jeito que o original.
- **Infusão de encantamento** (`InfusionEnchantmentRecipe`, tabela `InfusionEnchantments` gerada por
  `scratchpad/infusao-encantamentos.js` com as 24 receitas do jar): quando nenhuma receita de infusão fecha, a matriz
  tenta subir um nível de encantamento da coisa do meio (que precisa aceitá-lo, não estar no máximo e ter só
  encantamentos compatíveis). A essência cresce com o nível atual e um décimo por nível de outros encantamentos; a
  instabilidade soma metade dos níveis; antes da essência, a matriz tira experiência (um terço do custo mínimo, vezes
  1 + o nível atual) de quem estiver a dez blocos, um nível por vez, com um arranhão mágico; sem ninguém com
  experiência, a essência às vezes aumenta.
- **Pech**: os livros de Pressa e de Reparo voltaram à troca do pech mago.
- **Testes**: `EnchantmentGameTest` (os dados, o Reparo pagando com a varinha, a infusão subindo Afiada).

## Jarro de cérebro e nó no jarro

Porte do `TileJarBrain`, `TileJarNode`, `ItemJarNode`, `ModelBrain`, dos números 1 e 2 do `BlockJar`, do
`renderBrain` do `TileJarRenderer`, do `ItemJarNodeRenderer` e do `createNodeJar`/`fitNodeJar`/`replaceNodeJar` do
`WandManager` (descompilados do jar).

- **Jarro de cérebro** (`BrainJarBlock`/`BrainJarBlockEntity`): o vidro do jarro com a salmoura (`jarbrine.png`) e o
  `ModelBrain` (`brain2.png`), que vira devagar para a bolinha ou o jogador mais perto e sobe e desce. Puxa as bolinhas
  de experiência a até seis blocos e as come (até 2000); o toque devolve até 64 ao acaso e o faz esperar dois segundos;
  quebrado, devolve tudo. Suspira de vez em quando (`brain`), solta faíscas de feitiço quando cheio e o comparador lê o
  quanto tem. Conta como estante para a mesa de encantar (o original valia 2; a marca do jogo de hoje vale 1).
  Receita de infusão do jar.
- **Nó no jarro** (`NodeJarBlock`/`NodeJarBlockEntity`, `NodeJarStructure`, gatilho quatro da varinha): uma caixa de
  vidro de 3×3×3 com o nó no meio e tampa de lajes de madeira; a varinha no vidro (de uma das duas fileiras de cima),
  com NODEJAR e 70 de cada primário, encolhe tudo num jarro com o nó — que três vezes em quatro enfraquece um passo.
  Preso, o nó não se refaz nem faz nada (e o pedestal de recarga e o transdutor não o usam); luz 11; o desenhista do
  nó o mostra um tanto mais baixo. A varinha quebra o vidro e o solta como estava. Quebrado, sai o item com o nó
  (`thaumcraft:jarred_node`), que põe o jarro de volta e lista os aspectos na dica; na mão, o nó aparece em três planos.
- **Testes**: `SpecialJarGameTest` (o cérebro come; a caixa vira jarro e a varinha solta) e `SpecialJarClientTest`.

## Mácula e fluxo (blocos)

Porte do `BlockTaint`, `BlockTaintFibres`, `BlockTaintFibreRenderer`, `EntityFallingTaint`, `BlockFluxGoo`,
`BlockFluxGas`, `BlockGasRenderer`, `PotionFluxTaint`, `PotionVisExhaust`, `PotionInfectiousVisExhaust`,
`TileEtherealBloom` e dos derrames do `BlockEssentiaReservoir`, `BlockAiry.explodify` e `TileCrucible.spill`
(descompilados do jar). A regra antiga, inventada (a mácula secando sozinha, a flor limpando bloco a bloco), saiu.

- **Crosta, solo e carne** (`TaintBlock`, números 0, 1 e 2): dureza 1,75/1,5/0,2, resistência 10, som de carne
  (`gore`). No tique ao acaso, com dois vizinhos maculados, uma vez em mil pinta de Terra Maculada uma coluna vizinha
  (`BiomePainter`) com o som de raízes. A crosta cai como areia (`FallingTaintEntity`) se embaixo há ar, fogo, fibra,
  coisa substituível ou fluido — menos com tronco a um bloco — e escorrega de lado de uma coluna de crosta. Um bloco
  sorteado perto, dentro do bioma, ganha fibra; ali a crosta com ar em cima vira, uma vez em duzentas, um enxameador
  de esporos (gancho da fauna), e a cercada de crosta vira gosma cheia. Fora do bioma, a crosta vira gosma (1 em 20)
  e o solo, terra (1 em 10). Quem pisa pega o fluxo da mácula (jogador 1 em 100 por 4 s; bichos 1 em 20 por 8 s). A
  crosta pinga (a gota do `FXDrop`) com ar embaixo. Caem: nada, terra e nove carnes podres (com toque de seda, o
  próprio bloco). O solo pega a cor do capim do lugar. Bloco de carne: nove carnes podres na bancada; é a base do
  golem de carne no crisol.
- **Fibras** (`TaintFibreBlock`, `KIND` 0 a 4): a película, o capim, o capim que brilha (luz 8), o talo de esporos e o
  talo com esporo (luz 10). Só vivem no bioma (fora dele somem; a película também some cercada só de mácula ou ar).
  O `spreadFibres`: colado a bloco firme, não cercado só de mácula, em ar, coisa substituível, flor ou folha — nove
  vezes em dez a película; na outra (com chão firme e ar em cima), capim (9/10), o que brilha ou o talo. Sem fibra no
  lugar sorteado, com dois vizinhos maculados, tronco/abóbora/melancia/cacto viram crosta; com três, terra, areia,
  cascalho e argila viram solo. O talo solta um esporo (gancho da fauna). Desenho (`TaintFibreModel`): toda forma forra
  cada face firme em volta que não seja do bloco da mácula (a 0,005, na cor do capim); a película acende um brilho
  `taint_over` em 5% das faces; o capim é uma cruz deslocada pelo mesmo hash do original; os talos, os quatro planos
  de uma plantação, sem cor.
- **Gosma e gás de fluxo** (`FluxBlock`/`FluxGooBlock`/`FluxGasBlock`): o fluido finito do Forge, oito quanta, luz 7,
  qualquer bloco posto em cima o substitui. A gosma desce a cada 30 tiques, prende quem anda nela (quanto mais cheia
  mais) e dá exaustão de vis; parada, vira slime taumático (gancho), pinta o bioma e vira fibra, ou evapora um quantum
  (às vezes subindo como gás); bolhas cor-de-rosa. O gás sobe a cada 12 tiques e, respirado, dá exaustão de vis ou
  náusea. Desenho (`FluxModel`): a altura dos quanta (7/8 cheia, inteira com mais do mesmo do lado de onde vem); o
  gás sem teto firme é um cubo inteiro.
- **Derrames** (`Flux`): o reservatório quebrado (50 sorteios a até 4 blocos, gosma abaixo e gás acima, até o tanto de
  essência/16), o nó energizado explodindo (50 sorteios a até 7, sem limite) e o `spill` do crisol (pronto para a
  fatia do crisol).
- **Efeitos**: fluxo da mácula (fere 1 a cada 40 tiques, metade por nível; cura o que é maculado; dano `taint`),
  exaustão de vis (+10% de custo por nível) e a contagiosa (passa para quem está a quatro blocos, um nível abaixo).
- **Flor Etérea**: a cada segundo devolve a uma coluna a até sete blocos (num raio de nove) que seja de Terra
  Maculada, Mata Assombrada ou Floresta Mágica o bioma natural do gerador (a Terra Maculada natural vira planície). É
  só isso: a mácula fora do bioma é que definha.
- **Bancada**: bloco de carne, de taumium e de sebo (e de volta, menos a carne).
- **Testes**: `TaintGameTest` (solo e crosta fora do bioma, a crosta caindo e presa por tronco, fibras nascendo e
  morrendo, o derrame, a gosma caindo sem se multiplicar, a flor devolvendo o bioma) e `TaintClientTest`.

## Fauna da mácula

Porte do `EntityTaintChicken`, `Cow`, `Pig`, `Sheep` (com o `AIConvertGrass`), `Creeper` (com o `AICreeperSwell`),
`Villager`, `EntityTaintSpider`, `EntityThaumicSlime`, `EntityTaintSpore`, `EntityTaintSporeSwarmer`,
`EntityTaintSwarm`, `EntityTaintacle`, `EntityTaintacleSmall`, `EntityBottleTaint`/`ItemBottleTaint`, dos
desenhistas e modelos (`RenderTaint*`, `ModelTaintSheep1/2`, `ModelTaintSpore`, `ModelTaintSporeSwarmer`,
`ModelTaintacle`/`ModelRendererTaintacle`), do `FXSwarm` e dos efeitos `splooshFX`, `taintsplosionFX`,
`slimeJumpFX` e `tentacleAriseFX` (descompilados do jar; os modelos de bicho vêm do jar do 1.7.10, porque as peles do
mod são desenhadas para eles).

- **Bichos maculados** (`entity.taint`): vida, dano, armadura e velocidade do original; caçam jogador, aldeão e
  (os de quatro patas e a galinha) bichos; voz grossa (tom 0,7) e o respingo roxo nos primeiros tiques. A galinha pula
  e cai devagar; a ovelha macula o capim que come (fibra e bioma) e dá lã roxa na tosquia; o creeper maculado estoura
  com força 1,5, dá fluxo da mácula a seis blocos e macula o chão; o aldeão abre portas e pode deixar moeda.
- **Conversão** (`TaintConversion`, no começo do `LivingDeathEvent`): quem morre com o fluxo da mácula volta como a
  versão maculada (creeper, ovelha, vaca/cogumelo, porco, galinha, aldeão) ou como slime taumático de 1 a 7; nesse caso
  não há orbes de aspecto.
- **Slime taumático**: tamanho até cem (vida = tamanho), pula atrás do jogador (três vezes mais depressa), cospe um
  slime pequeno de longe e encolhe, junta-se a outro slime sem jogador por perto, cresce comendo a gosma de fluxo e se
  divide ao morrer. Nasce da gosma (pequeno com 3 a 6 quanta, maior cheia).
- **Esporo e enxameador**: o talo de esporos solta o esporo (que cresce até dez e estoura em aranhas da mácula ao
  toque, ferido ou sem o talo); a crosta com ar em cima solta o enxameador (um por 16 blocos), que solta enxames a cada
  25 s com jogador perto. Fora da Terra Maculada, murcham. As mosquinhas (`SwarmFx`) voam em volta, zumbindo.
- **Enxame**: voa como morcego, pica sem empurrar (fraqueza), vagueia pela Terra Maculada.
- **Tentáculos**: brotam só na película de fibra ou no solo maculado da Terra Maculada (peso 1, nenhum outro a 24
  blocos), não saem do lugar, apertam de perto (dano de tentáculo) e fazem brotar o pequeno aos pés de quem está longe
  ou os fere de longe. Desenho: gomos 12% menores a cada um, a bolinha e a cabeça acesas, brotando do chão.
- **Garrafa de mácula** (crisol: frasco cheio + 8 Vitium + 8 Praecantatio): arremessada, dá fluxo da mácula a cinco
  blocos e macula o chão.
- **Matérias**: gosma maculada e ramo de mácula (o que a fauna deixa).
- **Testes**: `TaintFaunaGameTest` (conversão, slime do zumbi, esporo estourando e segurando no talo, slime crescendo e
  se dividindo, o que a vaca deixa) e `TaintFaunaClientTest`.

## Aspectos das criaturas

Porte do `registerEntityAspects` do `ConfigAspects` e do `ScanManager.generateEntityAspects` (do jar): a tabela
`EntityAspectsTable` é gerada pelo `scratchpad/entidades-aspectos.js` e troca a conta inventada de antes. Vale a
última anotação cujas condições batem (o creeper carregado, o tipo do pech, o aspecto do fogo-fátuo); o esqueleto do
Wither, que era um tipo de esqueleto, virou criatura própria; o cavalo de então vale para cavalo, burro, mula e os dois
mortos-vivos, e o barco para os barcos (não os de baú). O jogador é Humanus 4 e mais três aspectos tirados do nome.
O que não está na tabela não se examina (nem solta orbes). Teste: `EntityAspectsGameTest`.

## Crisol fiel

Porte do `TileCrucible`, da parte do crisol do `BlockMetalDevice`, do `TileCrucibleRenderer`, do `EntitySpecialItem`
e dos efeitos `crucibleBoil`/`Froth`/`FrothDown`/`Bubble`, do `ItemEssence` (descompilados do jar). A regra antiga
(água sim/não, cor misturada) saiu.

- **Tanque** de 1000 mB de água (canos do jogo enchem e esvaziam por qualquer lado); balde ou garrafa d'água enchem o
  tanque inteiro e voltam vazios.
- **Calor**: só com água; fogo, lava ou nitor embaixo (o bloco de magma e a fogueira não valiam); +1 por tique e +2
  por fole em qualquer dos quatro lados, até 200; ferve acima de 150.
- **O que cai dentro** (com água e fervendo): cada item da pilha fecha uma receita (de quem jogou, com a pesquisa —
  item de funil não fabrica, como o nome vazio do original) ou vira aspectos; a conta do original processa só uma
  parte da pilha por toque. A receita bebe 50 mB e o resultado sai flutuando (`SpecialItemEntity`, imune a explosão).
  O que não tem aspecto pula para fora. Quem entra fervendo se queima de dez em dez toques.
- **Transbordo**: com mais de cem de essência, a cada cinco tiques um ponto sorteado some e sai um quantum de fluxo.
- **Decomposição**: fervendo e sossegado cinco segundos, um aspecto (sorteado de novo se deu primordial) perde um
  ponto e vira um dos seus componentes (o primordial sai como fluxo), bebendo 2 mB.
- **Despejo**: quebrado, ou com a varinha agachado, a água some e cada dois de essência viram um derrame de fluxo.
- **Desenho**: a água parada do jogo na altura do tanque e da essência, puxando para o roxo com a essência; espuma,
  espuma escorrendo pela borda (mais de cem), bolhas da cor dos aspectos, a fervura de quando algo cai dentro e o
  estalo da lava. Comparador pela essência.
- **Frasco**: como no original, enche-se no alambique e nos jarros (oito) e se despeja nos jarros — não no crisol.
- **Testes**: `CrucibleGameTest` (o frasco no jarro, a pilha dissolvendo em partes, receita com e sem quem jogou, o
  transbordo e a decomposição, o fluxo ao quebrar).

## Infusão fiel, distorção e avisos

Refeito a partir do `TileInfusionMatrix`, do `EssentiaHandler`, do `FXEssentiaTrail`, dos `drawInfusionParticles`
do `ClientProxy`, do `PlayerNotifications`/`REHNotifyHandler` e dos `addWarpToPlayer` (descompilados do jar).

- **Ciclo** (`craftCycle`) de dez em dez tiques (vinte depois de beber um nível de experiência): azar, experiência (no
  encantamento), essência (uma unidade por ciclo; faltando, uma chance em `100 - 3×instabilidade da receita` de subir a
  instabilidade, e os pedestais são recontados), ingredientes (cinco ciclos puxando as migalhas de cada pedestal; o que
  sobra no pedestal é o `getCraftingRemainder` do item; ingrediente em falta faz a essência crescer) e o fim, com as
  faíscas de toda cor no pedestal do meio (evento de bloco 12). A receita só começa se o jogador conhece a pesquisa, e
  a matriz não diz nada quando não acha receita (as mensagens de antes eram invenção).
- **Os vinte e um azares** na proporção do original (`nextInt(21)`): 0/2/10/13 cospe um ingrediente; 1/11 cospe com gás
  de fluxo; 6/17 com gosma; 19 some com gosma; 7 some com gás; 4/15 cospe com explosão; 3/8/14 raio numa criatura, 12
  em todas (4 a 7 de dano mágico); 5/16 mácula do fluxo (seis segundos) ou cansaço de vis (dois minutos) numa criatura,
  18 em todas; 9 explosão na matriz; 20 distorção num jogador perto (um em quatro de um ponto que gruda, senão 1 a 5
  temporários). Tirar a coisa do meio sorteia um azar e para a infusão com o som de falha, mas a matriz segue ligada.
- **Essência pelo ar**: só jarro, reservatório e espelho (`AspectSource`, o `IAspectSource`) dão; a lista de fontes fica
  guardada por bloco que bebe e, sem nenhuma que dê, só se procura de novo cinco segundos depois. O fio é o
  `FXEssentiaTrail` (a bolinha da cor do aspecto, subindo em espiral e batendo nos blocos) que o cliente solta por
  quinze tiques a cada unidade. O espelho de essência passou a usar o mesmo caminho.
- **Partículas**: migalhas do item (ou do bloco) voando do pedestal para a matriz, uma em três vira faísca roxa; a
  faísca verde de quem paga com experiência; raios em volta da matriz com instabilidade.
- **Distorção** guardada no jogador (permanente, que gruda, temporária e o contador), com os avisos e os sussurros. Os
  eventos da distorção (poções, aranhas, névoa) chegam na fatia seguinte.
- **Avisos do canto** (`client/PlayerNotifications`): as linhas em meia escala com o símbolo do aspecto, a faísca que
  entra com a mais nova, os pontos de pesquisa voando até o livro. O exame e a mesa de pesquisa mandam o
  `PacketAspectPool`/`PacketAspectDiscovery` como no original; o resumo inventado do visor do thaumômetro saiu.
- **Testes**: `InfusionGameTest` (tirar o meio para a infusão, receita sem pesquisa, alambique não é fonte),
  `WarpGameTest`; tela: `InfusionRunClientTest`.

## Distorção: eventos, poções e sabão

Porte do `WarpEvents`, das poções (`PotionUnnaturalHunger`, `PotionDeathGaze`, `PotionBlurredVision`,
`PotionSunScorned`, `PotionThaumarhia`), do `EntityMindSpider`/`RenderMindSpider`, do `ItemSanitySoap`, do
`PacketMiscEvent`, do `checkShaders`/`renderVignette`/`fogDensityEvent` e das fontes de distorção (descompilados do jar).

- **Eventos** de cem em cem segundos (sem a proteção contra a dobra): a chance, o sorteio e a lista inteira do original
  (pontos de pesquisa, cansaço de vis, taumarria, fome estranha, névoa, vista embaçada, desprezo do sol, fadiga, fago do
  fluxo, visão noturna, olhar mortal, aranhas da mente falsas e de verdade, cegueira, um ponto que gruda indo embora), com
  a máscara do diabo sorridente descontando; acima de 10/25/50 de distorção de verdade, a pista dos sais de banho e as
  pesquisas ELDRITCHMINOR/ELDRITCHMAJOR. O guardião eldritch da névoa espera o Eldritch (`WarpEvents.guardianSpawner`).
- **Fontes**: pesquisa proibida (metade permanente, metade que gruda; o livro e as notas avisam o nível), fabricar as
  coisas do `addWarpToItem` (tabela gerada pelo `scratchpad/dobra-itens.js`; falta a pedra sinistra), a infusão e o
  equipamento que distorce (`WarpEvents.WarpingGear`, com o tooltip).
- **Poções**: ícones recortados da `potions.png`; o olhar mortal vira contra o jogador o que ele encara e o faz murchar;
  a fome estranha cansa a cada tique e só a carne podre e o cérebro de zumbi aliviam.
- **Tela**: os quatro filtros do original (`post_effect/desaturate|blur|hunger|sun_scorned`, com o bloom do Thaumcraft
  traduzido para o GLSL de hoje), a vinheta com o coração disparado no susto, a névoa (a exponencial de então vira a
  linear de hoje, fechando em 2/densidade blocos) e as aranhas da mente quase transparentes, que só quem as chamou vê.
- **Sabão higienizante** (crisol: bloco de sebo + Mens/Alienis/Ordo/Sano 16): dez segundos esfregando, leva toda a
  temporária e às vezes um ponto da que gruda.
- **Diferença**: no original as poções da distorção não se curavam com leite; aqui o leite ainda tira.
- **Testes**: `WarpGameTest` (pesquisa proibida, fabricar, sabão, evento abrindo as pesquisas); tela: `WarpClientTest`.

## Itens soltos: comida, ferramentas elementais, arco de osso, relíquias

Descompilados do jar: `ItemNuggetEdible`, `ItemTripleMeatTreat`, `ItemElemental*`, `ItemPrimalCrusher`,
`ItemCrimsonSword`, `ItemVoid*`, `ItemBowBone`, `ItemPrimalArrow`/`EntityPrimalArrow`/`RenderPrimalArrow`,
`ItemResonator`, `ItemSanityChecker`, `ItemCompassStone`, `EntityFollowingItem`, `FXSmokeSpiral`, `startScan`.

- **Comida**: as quatro pepitas de carne (bônus de fundir carne na fornalha infernal, tabela regerada; o peixe vale para
  bacalhau e salmão) e o petisco de três carnes (quatro receitas sem forma, geradas pelo `scratchpad/comida.js`).
- **Ferramentas elementais** (infusão, receitas regeradas): a picareta põe fogo, varre os minérios/água/lava através das
  paredes por cinco segundos e às vezes solta aglomerado nativo; o machado puxa os itens e derruba a árvore de fora para
  dentro; a pá cava 3×3 e põe nove blocos (tecla G troca a orientação, com a prévia do arquiteto); a enxada ara 3×3, faz de
  farinha de osso e faz crescer as mudas mágicas; a espada ergue quem a segura num redemoinho de fumaça e acerta em volta
  do alvo. O que a pá e o triturador cavam voa até quem cavou (`FollowingItemEntity`).
- **Triturador primordial** e **lâmina carmesim**: consertam-se sozinhos e distorcem dois; o triturador cava 3×3 (a
  receita espera a pérola primordial do Eldritch). O **metal do vazio** também se conserta, distorce um e enfraquece.
- **Arco de osso** (arma em dez tiques, atira mais longe, meio ponto a mais) e as **seis flechas primordiais** (qualquer
  arco as atira; ar e ordem furam armadura, fogo queima, água deixa lento, terra empurra, entropia murcha), com o
  fogo-fátuo da cor do primário. **Diferença**: o arco de hoje pega a flecha que achar primeiro; no original a primordial
  tinha preferência.
- **Ressonador** (o que há e o que puxa num cano), **verificador de sanidade** (o tubo da distorção no canto da tela) e
  **pedra sinistra** (acende com um nó sombrio à frente).
- **Testes**: `ToolsGameTest`; tela: `ToolsClientTest`.

## Estandartes e purificador de fluxo

- **Estandartes** (`TileBanner`, `TileBannerRenderer`/`ModelBanner`, o aparelho de madeira 8): o dos cultistas e os
  dezesseis coloridos (bancada arcana, receitas geradas desenrolando o laço do original), de pé (dezesseis rumos) ou na
  parede, com o pano balançando; o frasco de essência pinta o aspecto (agachado apaga); quebrado, leva cor e aspecto no
  item, que se desenha como o estandarte.
- **Purificador de fluxo** (`TileFluxScrubber`, o aparelho de pedra 14): bebe Aer da rede de vis e desfaz a gosma e o
  gás de fluxo a até dezesseis blocos, juntando Praecantatio que sai por cano; o topo de obelisco com a ponta balançando.
- A **caixa mágica** (`BlockMagicBox`) não entra: no original ela não tem receita nem aparece no criativo.
- **Testes**: `ToolsGameTest` (estandarte, purificador); tela: `BannerClientTest`.

## Pistas, pesquisas escondidas e o exame fiel

Descompilados do jar: `ResearchManager.createClue`/`findHiddenResearch`, `ScanManager.completeScan`/`isValidScanTarget`/
`generateNodeAspects`, `ItemThaumometer.doScan`/`onUsingTick`, `ItemResearchNotes` (metadado 42), `ItemResource` (9) e a
conta de visibilidade do `GuiResearchBrowser`.

- **Gatilhos**: `research/ResearchTriggers.java`, gerado pelo `scratchpad/gatilhos.js` a partir dos `setItemTriggers`,
  `setEntityTriggers` e `setAspectTriggers` do `ConfigResearch` do jar (27 pesquisas). O portal e o portal do End não têm
  item hoje; os do Eldritch entram com a fatia dele.
- **Pista** (`createClue`): o primeiro exame de uma coisa pode acordar uma pesquisa escondida ou perdida cujo gatilho
  bata (o item, a criatura, ou um aspecto ganho); marca `@CHAVE` e avisa no canto.
- **Livro**: a pesquisa aparece se sabida, se tem a pista, ou se não é perdida/escondida (e a encoberta com os pais
  feitos). Saiu a regra inventada do `hint:`.
- **Thaumômetro**: vinte e cinco tiques, fecha faltando cinco; criatura até dez blocos, bloco no alcance do braço; o que
  já foi examinado não começa; desviou a mira, o exame morre até o próximo clique; as runas sobem do alvo e o tique-taque
  toca baixo, só para quem examina. Nodos se examinam pelo `generateNodeAspects`. Os avisos são os do original
  (`tc.unknownobject`, `tc.discoveryerror` com o aspecto que falta) no canto da tela; as mensagens `tc.scan.*` eram
  invenção e saíram, junto do estalo de câmera.
- **Fragmento de conhecimento**: um ou dois pontos de cada primário, com os avisos. **Nove fragmentos** fazem a nota de
  conhecimento desconhecido; lida, vira a nota de uma pesquisa escondida (sorteada pela hora do mundo), ou, sem nenhuma,
  some e devolve de sete a nove fragmentos. As notas de pesquisa voltam a não empilhar, como no original.
- **Testes**: `ClueGameTest`; tela: `ClueClientTest`.

## Eldritch 6.1: ruínas do mundo, anel eldritch e blocos antigos

Descompilados do jar: `BlockEldritch` (+`BlockEldritchRenderer`, `BlockEldritchItem`), `BlockCosmeticSolid` (0, 1, 8, 11–15),
`BlockLoot` (+ renderers), `ItemEldritchObject`, `TileEldritchAltar/Obelisk/Cap` (+ `TileEldritchCapRenderer`,
`TileEldritchObeliskRenderer`), `WorldGenEldritchRing`, `WorldGenMound`, `WorldGenHilltopStones`, `generateTotem` e o
pedaço de estruturas do `generateSurface`, `createRandomNodeAt` (o `eerie`), `CustomStepSound`.

- **Blocos**: totem de obsidiana (lados pela coluna: base, base sombreada, entalhes pela soma dos restos), totem carregado
  (nó sombrio dentro; quebrado estoura e solta essências), ladrilho de obsidiana (4 de 4 obsidianas), pedra antiga (as
  quatro figuras por face; aqui dezesseis combinações fixas sorteadas por bloco), rocha antiga (ladrilho 2×2 pela
  paridade), pedra incrustada, pedestal, escada e laje; do `BlockEldritch`: altar, obelisco (pé e topo), capitel (só os
  desenhistas; quebrado um, somem as peças vizinhas), pedra incrustada luminosa, pedra de glifos (deixa fragmento) e o
  enfeite — estas três dois pixels para dentro nas faces soltas. Urnas e caixotes nas três raridades (derramam 1+r a
  3+r coisas da tabela das sacolas).
- **Itens**: olho eldritch (vai no altar; do terceiro em diante o altar chama guardiões), ritos carmesins (ensinam o
  CRIMSON), tábua rúnica, pérola primordial (num nó: mexe na base, melhora o feitio, explode e cospe fluxo) e o colocador
  de obelisco. As receitas que esperavam a pérola e o olho entraram (construto alquímico avançado, triturador, olho).
- **Desenho**: o capitel e o altar com a peça `Cap` do `obelisk_cap.obj` (e os olhos em volta); o obelisco boiando com a
  casca rendada, as pontas e o céu de estrelas por dentro (o shader do buraco portátil); de longe, o campo parado.
- **Mundo** (`RuinsFeature`): uma tentativa por pedaço — túmulo 1/150 (os ~2450 blocos do original numa tabela gerada
  pelo `scratchpad/tumulo.js`, urnas, baú às vezes com armadilha de TNT, geradores de esqueleto e zumbi), anel eldritch
  1/66 (às vezes com estandartes e altar chamador), pedras do topo 1/40 (acima de 85, baú e gerador de fogo-fátuo),
  cada um com nó sombrio; senão, totem 1/360. **Diferenças**: o canto do túmulo sorteia até a casa 13 do pedaço (a
  geração de hoje não deixa escrever mais longe); o nó solto do mundo é outra etapa e não segura o totem.
- **Faltam** (fatias seguintes): quem o altar chama (clérigos, cavaleiros, guardiões), o labirinto que o anel reserva e o
  portal (Terras de Fora).
- **Testes**: `RuinsGameTest`; tela: `RuinsClientTest`.

## Eldritch 6.2: o Culto Carmesim e os campeões

Descompilados do jar: `EntityCultist`, `EntityCultistKnight`, `EntityCultistCleric`, `EntityCultistLeader`,
`EntityCultistPortal`, `EntityThaumcraftBoss`, `EntityGolemOrb`, `AICultistHurtByTarget`, `AIAltarFocus`,
`AILongRangeAttack`, `AIAttackOnCollide`, `RenderCultist`, `RenderCultistPortal`, `RenderElectricOrb`, `FXArc`,
`PacketFXBlockArc`, `ItemCultistRobeArmor/PlateArmor/LeaderArmor/Boots`, `ModelRobe`, `ModelKnightArmor`,
`ModelLeaderArmor`, `ChampionModifier` e os treze `ChampionMod*`, `EntityUtils.makeChampion` e os ganchos de campeão do
`EventHandlerEntity`, do `EventHandlerRunic` e do `RenderEventHandler`.

- **Campeões** (`event/Champions`): monstro da lista (zumbi, aranha, blaze, enderman, esqueleto, bruxa, tentáculo,
  fogo-fátuo, pech, cultistas; chefes sempre) tem a chance do original de nascer campeão de um dos treze tipos: +30 de
  vida, dano triplo, o nome do tipo, e o efeito (a cada tique, no golpe dado ou no levado); faíscas de cada tipo; morto
  por alguém, experiência e sacola. O tipo mora num anexo sincronizado (no original, num atributo).
- **Cultistas**: cavaleiro (placa, espada — raramente de táumio ou do vazio), clérigo (robe; orbe vermelho que persegue
  ou três bolas de fogo; o ritual em volta do altar, boiando e ligado a ele por um fio), aliados entre si, chamando
  ajuda; derrubam fragmento, semente do vazio, moeda e, raramente, os ritos carmesins. O altar do anel agora chama os
  quatro clérigos e depois cavaleiros.
- **Pretor** (chefe): barra de chefe, sempre campeão com o título ("Pretor Fertus, o Audaz"), cura, raiva com golpe
  forte, troca de alvo pela raiva e reforço por jogador; lâmina carmesim.
- **Portal carmesim**: finca os estandartes, espalha caixotes com arcos de faísca, solta levas de cultistas e o pretor;
  morto, deixa a pérola primordial. Quem o abre é a fechadura das Terras de Fora (6.4).
- **Armaduras**: robe (1% de desconto, 1 de distorção), placa, pretor e botas, com os modelos do original gerados pelo
  `scratchpad/armadura-cultista.js` e as abas balançando com o passo.
- **Testes**: `CultistGameTest`; tela: `CultistClientTest`.

## Eldritch 6.3: o caranguejo, o zumbi habitado e o guardião

Descompilados do jar: `EntityEldritchCrab`, `EntityInhabitedZombie`, `EntityEldritchGuardian`, `EntityEldritchOrb`,
`TileEldritchCrabSpawner` (e o `BlockEldritch` número 9), `RenderEldritchCrab`, `RenderInhabitedZombie`,
`RenderEldritchGuardian`, `RenderEldritchOrb`, `TileEldritchCrabSpawnerRenderer`, `ModelEldritchCrab`,
`ModelEldritchGuardian`, `FXSonic`, `FXWispEG`, `FXVent`, `PacketFXSonic`, e os trechos do `ConfigEntities`, do
`TileEldritchAltar.spawnGuardian` e do `WarpEvents.spawnGuardian`.

- **Caranguejo eldritch**: vinte de vida, pula no alvo e monta na cabeça dele mordendo; de elmo (cinco de armadura, mais
  lento) quebra o elmo na metade da vida; imune a veneno; artrópode; pérola do fim. Modelo gerado pelo
  `scratchpad/modelo-entidade.js`, com a `craboverlay.png` acesa por cima.
- **Zumbi habitado** ("Casca Cambaleante"): placa dos cavaleiros, trinta de vida, caça cultistas, não converte aldeão;
  morto, estoura e solta um caranguejo de elmo. Só nasce sem outro a trinta e dois blocos.
- **Guardião eldritch**: cinquenta de vida, orbe eldritch (fraqueza e dois terços do dano em volta) de um braço e do
  outro, ou o grito (cone de ondulação, murchar e distorção); névoa escura dos pés; fora das Terras de Fora é um vulto
  translúcido que some com a distância e traz a névoa aos que estão perto; nas Terras de Fora ganha um escudo que se
  refaz. O altar do anel (tipo 1) e a névoa da distorção agora o chamam.
- **Abertura incrustada** (`crusted_opening`): com alguém a dezesseis blocos e menos de seis caranguejos por perto, chia,
  solta vapor e cospe um caranguejo sem elmo pela face virada; o respiradouro é o `crabvent.obj`. Quem a põe no mundo são
  as salas das Terras de Fora (6.4).
- Campeões: caranguejo (0) e zumbi habitado (3) entram na lista, como no original.
- **Testes**: `EldritchCreatureGameTest`; tela: `EldritchCreatureClientTest`.

## Eldritch 6.4: as Terras de Fora

Descompilados do jar: `WorldProviderOuter`, `ChunkProviderOuter`, `BiomeGenEldritch`, `TeleporterThaumcraft`,
`MazeHandler`, `MazeThread`, `MazeGenerator`, `Cell`, `CellLoc`, `GenCommon`, `GenPassage`, `Gen2x2`, `GenBossRoom`,
`GenKeyRoom`, `GenNestRoom`, `GenLibraryRoom`, `GenPortal`, `MapBossData`, `BlockEldritchPortal`, `BlockEldritchNothing`, o
`BlockEldritch` inteiro (7 porta, 8 fechadura, 10 pedra rúnica), o 12 do `BlockAiry`, o 7 do `BlockCrystal`,
`TileEldritchPortal`, `TileEldritchLock`, `TileEldritchTrap`, `TileEldritchNothing`, `TileEldritchCrystal` e os
desenhistas deles, `EntityPermanentItem`, o `createOculus` do `WandManager`.

- **A dimensão** (`thaumcraft:outer`): sem céu, sem tempo, névoa grossa da cor do original, sem chuva, sem cama; o bioma
  eldritch com o zumbi habitado e o guardião. Os chunks nascem vazios (`OuterChunkGenerator`, o `ChunkProviderOuter`) e o
  labirinto é o recurso do bioma. **Diferença:** o original guardava o labirinto num `labyrinth.dat` à parte; aqui é um
  dado salvo do mundo (`Labyrinth`), com a mesma tabela de chunk para casa.
- **O labirinto** (`world/outer`): o anel do mundo de cima e o olho no altar reservam o labirinto (traçado em outra linha
  de execução, como o `MazeThread`); cada chunk das Terras de Fora que é casa vira a sala dela no andar cinquenta — o
  portal no meio, as quatro partes da sala do chefe, a sala da chave (a tábua rúnica boiando, dois a quatro guardiões),
  ninhos, bibliotecas, corredores (rúnicos, incrustados, maculados, de aranhas da mente) — com os enfeites no fim (pedras
  incrustadas luminosas, cristais estranhos, aberturas de caranguejo, urnas). Os geradores recebem os números do 1.7 e
  os traduzem num lugar só (`MazeBlocks`). **Diferença:** o nada que dá para fora era marcado pelo aviso de vizinho do
  1.7; aqui é conferido no fim da construção do chunk e da beirada dos vizinhos.
- **O óculo**: altar com os quatro olhos, nó sombrio em cima e o labirinto traçado — a varinha com cem de cada primordial
  o transforma no portal. **O portal** leva às Terras de Fora (ensinando "Entrar nas Terras de Fora") e de volta, para
  uma quina ao lado do portal mais perto do outro lado. **Diferença:** o original varria cada bloco de um quadrado de 257
  por 257; aqui, lá dentro o portal é achado pelo labirinto, e aqui fora pelas entidades de bloco, do chunk mais perto
  para o mais longe.
- **Blocos**: o nada (céu de estrelas nas faces abertas, oito de dano do vazio), o intransponível, a porta antiga, a
  fechadura (a tábua rúnica abre; cinco segundos depois a porta some e a sala ganha o chefe da vez — golem, guardião-mor,
  culto ou mácula; o golem, o guardião-mor e o tentáculo gigante chegam na 6.5), a pedra rúnica (choque e distorção a
  três blocos; as runas de cada face sorteadas entre 24 modelos), os cristais estranhos (`vcrystal.obj`).
- **Testes**: `OuterLandsGameTest`; tela: `OuterLandsClientTest` (entra pelo portal e fotografa a sala e um corredor).

## Eldritch 6.5: os chefes

Descompilados do jar: `EntityEldritchGolem`, `EntityEldritchWarden`, `EntityTaintacleGiant`, `RenderEldritchGolem`,
`ModelEldritchGolem`, o ramo do guardião-mor do `RenderEldritchGuardian`/`ModelEldritchGuardian`, o `RenderTaintacle` de
catorze gomos, os números 10 e 11 do `BlockAiry`, e o fim das salas de chefe do `TileEldritchLock`.

- **Construto eldritch**: 250 de vida, seis de armadura, imune a fogo; esmaga urnas e caixotes e derruba o que é mole. O
  golpe que o mataria arranca a cabeça numa explosão e não passa; sem cabeça, o pescoço solta vapor, faíscas e arcos até
  o chão, o golpe empurra, e ele atira os orbes que perseguem em rajadas, recarregando por 7,5 s. Modelo gerado a 2,15×.
- **Guardião-mor**: nome antigo sorteado ("Aphoom-Zhah, o Audaz"), 200 de vida e mais 132 de escudo que se refaz; sobe do
  chão ao nascer; deixa o campo sugador por onde anda; sem escudo, volta para o meio da sala e, invulnerável, espalha
  anéis de campo sugador; orbe eldritch ou grito (empurra, murcha, enfraquece, distorce). O olho do capuz aceso.
- **Tentáculo gigante**: 125 de vida, nove de dano, nasce campeão, barra de chefe, raiva; o último a cair por perto deixa
  a pérola primordial.
- **Campo sugador** (`sapping_field`): quem não é eldritch anda devagar, cansa, enfraquece e às vezes murcha.
- A fechadura agora chama os três (`BossSpawns`), cada chefe nascendo virado para ela.
- **Testes**: `BossGameTest`; tela: `BossClientTest`.

## Eldritch: manto do vazio e as pistas que faltavam

- **Armadura de manto do vazio** (`ItemVoidRobeArmor`): capuz, manto e calças com a proteção do metal do vazio, cinco por
  cento de desconto de vis e dois de distorção por peça, conserto sozinho, o capuz revelando como os óculos. No corpo, o
  `ModelRobe` dos cultistas; a cor do tingimento vai no pano (a `void_robe_armor_overlay.png`, que o `getArmorTexture` do
  original devolve no passe que o Forge tinge) e os enfeites por cima. As três receitas de infusão entram pelo gerador.
- **Tingir**: os mantos do taumaturgo e do vazio entram na etiqueta `minecraft:dyeable` — é a receita de tingir do jogo
  fazendo o papel do `RecipesRobeArmorDyes` e do `RecipesVoidRobeArmorDyes`.
- **Pistas**: "Revelações das Terras de Fora" agora desperta examinando a pedra de glifos ou a pedra rúnica (que viram
  item, como no original).

## Os efeitos que o leite não tira

O original limpava os `getCurativeItems` dos efeitos que a distorção, o gás e a gosma de fluxo, a infusão instável e a
fome estranha põem — o leite não os tira. Hoje o leite limpa tudo de uma vez (`removeAllEffects`); o `Incurable` marca
esses efeitos e o `LivingEntityIncurableMixin` os devolve logo depois. Teste: `WarpGameTest.milkDoesNotCureWarp`.


## O Thaumonomicon fiel (2026-09-19)

Descompilados do jar: `GuiResearchRecipe`, `GuiResearchBrowser`, `GuiResearchPopup`, `TCFontRenderer`, `ResearchPage`,
`ResearchItem` e o `ConfigResearch`/`ConfigRecipes` inteiros.

- **Páginas** (`ResearchPageScreen`): a folha dupla ampliada 1,3×, o texto na letra miúda do livro (a fonte unicode de
  então, hoje a `uniform`) com as figuras `<IMG>` (as `research1..5` e `eldritchajor1..2` copiadas do jar) e os
  filetes `<LINE>`; bancada comum (com e sem forma), bancada arcana (com o vis), crisol (a água, a seta e o
  catalisador), fornalha, infusão (os pedestais em volta, a instabilidade), infusão de encantamento (o nível em rodízio,
  a experiência), aumento rúnico, e as **montagens de estrutura** em camadas sobre o chão quadriculado. As listas de
  receitas trocam uma por segundo; o ingrediente que outra pesquisa ensina leva até ela com um clique
  (`recipe.clickthrough`) e o marcador do rodapé volta. "Aspectos" ganha as páginas dos aspectos descobertos, quatro por
  página, com as coisas examinadas sob o cursor.
- **O que cada página cita** é gerado do jar (`scratchpad/livro-pesquisas.js` → `Researches`, `scratchpad/livro-receitas.js`
  → `BookRecipes`) e achado nas tabelas do mod (`BookPages`). Teste: `ResearchGameTest.everyBookRecipeResolves` (toda
  página de receita acha tantas receitas quantos nomes cita).
- **Mapa** (`ThaumonomiconScreen`): o `GuiResearchBrowser` — ícones de item e de desenho, apagados enquanto não se pode
  abrir (o item escurecido pelo `TintedItems`, que leva a cor do `glColor` de então até a colagem do item na tela); as
  linhas curvas, ondulantes e desbotando do original; a aura roxa do conhecimento proibido; a faísca da recém-aprendida e
  da aba dela; a caixa do cursor com o subtítulo, o aviso proibido e o que falta; o arrasto que volta macio para os
  limites; o som de câmera nas abas.
- **Faixa "Research Completed!"** (`ResearchPopup`): três segundos no canto, uma de cada vez, por cima de tudo.
- Testes de tela: `BookClientTest`, `BookPagesClientTest` (uma página de cada tipo, o tooltip e a faixa).

## O thaumômetro em primeira pessoa

`ThaumometerFirstPerson`: o `ItemThaumometerRenderer` com as transformações do `ItemRenderer` de então — o aparelho
erguido, as duas mãos (o braço direito do modelo nas duas, como no original), o vidro que tremula, e na lente o nome, os
aspectos do que já foi examinado e o tipo do nodo. Saíram o visor inventado no meio da tela e a pose erguida inventada.

## Cetros

`ArcaneSceptreRecipe` na bancada (três pontas em volta do amuleto primordial no canto de cima à direita, a haste no
meio; uma vez e meia o custo), vis uma vez e meia, um décimo a menos de gasto, sem troca de foco, a ponta maior com a
achatada embaixo e as dez runas girando. As três receitas de exemplo do livro vêm do `ConfigResearch`. Teste:
`WandAssemblyGameTest.sceptreFollowsTheOriginal`; tela: `SceptreClientTest`.

## Estanho, prata e chumbo

Os metais que o original aproveitava quando outro mod os trazia, agora pelas etiquetas `c:` (`OtherMetals`): pepitas e
aglomerados nativos, crisol `PureX`/`TransX` com catalisador por etiqueta, mineração especial, bônus da fornalha
infernal, fundição do aglomerado no primeiro lingote da etiqueta (`thaumcraft:tag_smelting`), lingote ↔ nove pepitas, a
ponta de prata inerte. Sem mod que traga o lingote, as receitas nem carregam e as pesquisas não aparecem. Teste:
`CrucibleGameTest.otherMetalsGoByTags`.

## Os aspectos do jogo de hoje e o gesto do agachar (2026-09-19)

O Thaumcraft original só conhecia o Minecraft de 2014: tudo o que veio depois — pedra-profunda, cobre que envelhece,
ametista, corais, sculk, as plantas do Nether novo, as câmaras de provação, o enxofre e o cinábrio da 26.2 — não tinha
aspecto nenhum, e por isso nem o thaumômetro nem a alquimia enxergavam essas coisas.

- **`NewItemsAspectsTable`** (gerada por `scratchpad/aspectos-novos.js`): 373 anotações, só das **bases** — o que sai de
  receita herda do que entra, como o próprio mod deduz. Os valores seguem o tom do original (matéria-prima de um a três,
  coisa rara de quatro a seis). Onde a conta do original zera por render muito (três pedras dão seis lajes), as famílias
  de construção ganham um mínimo pela marca, que é o mesmo remendo que o original fazia com o dicionário de minérios.
- **Sem aspecto, de propósito**: as peças de criador (blocos de comando, barreira, luz, estrutura, bastão de depuração) e
  os ovos de nascimento, como no original. Teste: `VanillaAspectsGameTest` passa por todo item do jogo.
- **O gesto do agachar** (`AspectTooltip`): o `renderAspectsInGui` do `ClientTickEventsFML` — segurando o agachar sobre
  uma casa de qualquer tela, os símbolos do que aquilo é feito aparecem em fileira acima do cursor, cada um no disco do
  original, com a quantidade; o aspecto que quem joga ainda não descobriu sai como interrogação, e o item que ainda não
  foi examinado não mostra nada. Tela: `AspectHoverClientTest`.

## As bijuterias no inventário de sempre, e o pulo do viajante (2026-09-20)

- **As quatro casas no inventário do jogo** (`InventoryMenuBaublesMixin`, `InventoryScreenBaublesMixin`): amuleto, dois
  anéis e cinto entram no fim da lista de casas do `InventoryMenu`, na fileira ao lado da mão de apoio. O quadro e o
  desenho apagado de cada uma saem da mesma folha do Baubles 1.0.1.10 (`expanded_inventory.png`), recortados de onde
  ficavam no inventário expandido. Agachar com a peça na mão veste; o que não aceita sair não sai.
  - **Fora do original, a pedido de quem joga**: saíram o inventário expandido, a tecla B e o botãozinho que alternava
    as duas telas (`BaublesMenu`, `BaublesScreen`, o pacote `Open`). O botão do livro de receitas, que morava bem no
    meio da fileira nova, subiu para a coluna vazia entre o quadro do jogador e a grade de fabricação.
  - Testes: `BaublesInventoryGameTest`, tela `BaublesClientTest`.
- **O pulo alto das botas do viajante**: também a pedido de quem joga, a força de pulo vai de 0,42 para 0,70 — pouco
  mais de três blocos de altura. O original só dava o degrau de um bloco, que continua. Testes:
  `TravellerBootsGameTest` (o degrau e a conta do pulo), `TravellerBootsClientTest` (anda, sobe e pula no mundo).
- **Nenhum ponto do livro sem desenho**: a mesa de pesquisa ganhou o item que já tinha no original (escondido da aba,
  como lá: nasce da mesa com as ferramentas de escrita), que é o que dá a cara da pesquisa `RESTABLE`. Guarda:
  `ResearchGameTest.everyResearchHasAnIcon`, que confere figura, item e modelo de todas.
- **Português**: os 130 textos que o pt_BR do original deixara em inglês (a descrição dos aspectos, os sussurros da
  distorção, os avisos de dobra, as peças do vazio, os núcleos de golem) entraram por `scratchpad/traducoes.js`. Ficam
  em inglês só os dezenove nomes próprios e formatos que o original também não traduz.

## O livro em português de verdade, e o inventário sem o guia de receitas (2026-09-20)

- **As sessenta e uma páginas que faltavam**: o pt_BR que veio com o 4.2.3.5 era um trabalho pela metade — havia
  pesquisa com a primeira página em português e a segunda em inglês, cortando no meio da frase (a do Pech). Todas
  entraram por `scratchpad/traducoes-livro.js`, com as marcas do livro intactas (`<BR>`, `<LINE>`, `<IMG>`, os `§` de
  cor e grifo). Guarda: `LangGameTest`, que passa palavra por palavra pelo arquivo inteiro.
- **Os pontos sem desenho do Thaumonomicon**: o jarro com nodo e o jarro de cérebro desenhavam só o que ia dentro — o
  vidro ficava de fora, porque um modelo `minecraft:special` não desenha o modelo de base, só chama quem o desenha em
  Java; agora são `minecraft:composite` (o vidro mais o que vai dentro). A mesa de pesquisa ganhou o seu
  `SpecialModelRenderer`, com o mesmo corpo, as mesmas folhas e a mesma pena do bloco, encolhido para caber na casa.
  `ResearchGameTest.everyResearchHasAnIcon` agora também confere se o modelo apontado desenha alguma coisa.
- **Fora do inventário, o botão do livro de receitas** (`InventoryRecipeBookMixin`): não é coisa do mod, é do jogo, mas
  quem joga pediu para tirá-lo — nunca usa o guia, e sem ele o inventário fica limpo. Só sai do inventário do jogador;
  na bancada e nas fornalhas continua onde sempre esteve. Era ele que ficava bem no meio da fileira das bijuterias.

## A porta dos mods de fora (2026-09-20)

O Thaumcraft 4 nunca foi um mod sozinho: Forbidden Magic, Tainted Magic, Magia Naturalis, Necromancy e companhia
entravam todos pelo `ThaumcraftApi`. Para que o mesmo valha aqui, a porta foi aberta:

- **`net.thaumcraft.api.ThaumcraftApi`**: abre aba no Thaumonomicon (`category`), põe pesquisa na árvore (`research`,
  com construtor fluente em `Research.of`), dá nome a uma receita para as páginas a citarem (`bookRecipe`), registra
  receita de crisol, de bancada arcana e de infusão, anota de que as coisas do mod são feitas (`aspects`) e a
  distorção que elas trazem (`warp`).
- **`onSetup`**: ao carregar um mod o jogo ainda não terminou de montar os itens (`Components not bound yet`), então
  toda receita — que carrega `ItemStack` — vai para uma fila que roda quando o mundo abre, logo antes de o Thaumcraft
  deduzir os aspectos das receitas. É o mesmo lugar em que as tabelas do próprio mod se montam.
- **Páginas**: `Page.text`, `Page.arcane`, `Page.crucible`, `Page.infusion`, `Page.compound` e companhia; uma página
  pode citar a própria receita que o mod registrou, e não só a pesquisa mais a saída como a tabela gerada faz.
- **Abas demais**: o original punha as abas numa fileira só descendo a lombada, e com cinco addons elas cairiam fora
  do livro. A nona começa outra coluna, mais para fora.
- **Guarda**: `net.thaumcraft.test.addon.TestAddon` é um mod de mentira que carrega junto com os testes e entra por
  essa porta; `AddonApiGameTest` confere que tudo o que ele registrou chegou onde devia.

## Maleficium — o Tainted Magic dentro do Thaumcraft (2026-09-20)

O Tainted Magic 8.1.1, de Yulife, era um mod à parte que entrava pelo `ThaumcraftApi`. Aqui ele vai **no mesmo jar**,
a pedido de quem joga, mas continua entrando pela mesma porta — o ramo é uma aba própria no Thaumonomicon, e o nome
dela é o da lore de quem joga: **Maleficium**, onde o original dizia *Obscura*. O código fica em
`net.thaumcraft.maleficium`, as figuras e os idiomas no espaço de nome do Thaumcraft, e a aba do criativo é separada
da do mod, como o original a tinha.

### Fatia 1 — a matéria-prima

- **Os doze subtipos do `ItemMaterial` viraram doze itens**, com os mesmos nomes e as mesmas figuras: o Minecraft de
  hoje não tem subtipo. O mesmo vale para os dois sais do `ItemSalis`.
- **Os sais** (`SalisItem` + `ItemEntitySalisMixin`): largados no chão duram cem tiques e se gastam, virando o tempo
  (Tempestas) ou o dia (Aevum). O original fazia isso no `onEntityItemUpdate`, que o jogo de hoje não tem mais — daí
  o mixin. As faíscas são as do original, pela faísca do próprio Thaumcraft (`Sparkle.custom`).
- **A aba e as três primeiras pesquisas** (`MALEFICIUM`, `SHADOWMETAL`, `UNBALANCEDSHARDS`), com as três receitas de
  crisol que elas mostram: o ferro que vira metal das sombras e os dois fragmentos desequilibrados.
- **Aspectos**: o Tainted Magic não anota nenhum, nem no original — quem os deduz das receitas é o Thaumcraft.
- Testes: `MaleficiumGameTest`; tela: `MaleficiumClientTest`.

### Fatia 2 — a madeira distorcida, a beladona e o Lumos

- **A árvore distorcida inteira**: tora, tábuas, folhas e muda, mais o **nó** do tronco — que no original era a mesma
  tora com outro número e aqui é bloco à parte, duro como obsidiana e cheio de sementes do vazio (de uma a cinco).
  Quebrá-lo solta fogos-fátuos e o estalo de receita falhada, como no original.
- **A árvore** (`WarpwoodTree`): o `WorldGenWarpwoodTree` conta por conta — tronco em cruz de cinco por cinco, entre
  sete e onze de altura, copa em bola achatada, nós aqui e ali (nunca dois seguidos) e um pé de beladona ao pé dela.
  Ela não nasce sozinha no mundo, nem no original: vem da muda.
- **O adubo da distorção** (`WarpFertilizerItem`): posto numa muda de madeira-prata, torce-a em muda distorcida — é
  por aí que a árvore entra no mundo.
- **A beladona**: fere e envenena quem passa por dentro dela, dá de uma a três bagas e sai inteira com tesoura. As
  bagas matam quem as come, como no original (o dano `nightshade`, que agora é um tipo de dano do mod).
- **O Lumos**: a luzinha que o foco e o anel deixam pelo caminho — ilumina como tocha, não atrapalha, solta faíscas de
  vez em quando e estala como gelo ao se quebrar. O `TileLumos` do original só existia para as faíscas; aqui elas
  saem do próprio bloco, sem entidade de bloco.
- **Diferença do jogo de hoje**: o chão que segura a árvore é perguntado pela etiqueta de vegetação
  (`minecraft:supports_vegetation`), e não pela de terra — no 26.2 a grama não está nesta última, e o original
  perguntava pelo `canSustainPlant`.
- Testes: `MaleficiumGameTest` (blocos, árvore, adubo, Lumos); tela: `MaleficiumTreeClientTest`.

### Fatia 3 — o metal das sombras e o punhal oco

- **As cinco ferramentas de metal das sombras**, com os números do `TMMaterials`: dois mil e quinhentos usos,
  velocidade dezessete, trinta de encantabilidade. A lâmina dá os mesmos dez de dano do original (quatro da espada
  mais seis do metal), e todas se consertam com o lingote, pela etiqueta `c:ingots/shadowmetal`.
- **A enxada vira a terra dos dois lados** (`ShadowmetalHoeItem`): terra e grama viram terra arada, e terra arada
  volta a ser terra. O original desliga a aração de sempre para fazer isso.
- **O punhal oco** (`HollowDaggerItem`): feito de haste de osso na bancada arcana, quase não fere — mas, ao golpear,
  enche de sangue carmesim o primeiro frasco vazio do inventário de quem bate.
- **As páginas do livro**: a pesquisa do metal das sombras passou a mostrar, depois do crisol, as cinco receitas de
  mesa; e o punhal ganhou a pesquisa dele, com a receita arcana.
- Testes: as ferramentas (uso, dano e conserto), a enxada dos dois lados e o punhal que tira sangue.

### Fatia 4 — as peças de varinha, e a tabela passa a ser gerada

- **A haste de madeira distorcida e o núcleo de bastão dela**: guardam duzentos e cinquenta e quinhentos de cada vis
  e se enchem sozinhos conforme a **distorção permanente** de quem os carrega — dez mil tiques divididos pela
  distorção, como o `WandHandler` do original. Quem está protegido da distorção não ganha nada.
- **As quatro pontas**: metal das sombras (desconta trinta e cinco por cento do vis), pano encantado, pano carmesim e
  pano de sombra.
- **A porta das varinhas** no Thaumcraft: `WandParts.registerRod`, `registerCap` e `onRodTick` — este último é o
  `IWandRodOnUpdate` do original, e é o que deixa uma haste de fora fazer o que a de madeira distorcida faz.
- **A tabela do ramo passou a ser gerada** (`scratchpad/mal-tabela.js`), lendo o `ResearchRegistry` e o
  `RecipeRegistry` do jar: vinte pesquisas e trinta e quatro receitas, com as posições, os aspectos, as marcas, os
  pais e a distorção do original. O que ainda não tem item por aqui fica no relatório do gerador, para as fatias
  seguintes. Os textos do livro saem do idioma do jar (`scratchpad/mal-lang.js`) e o português é escrito à mão.
- Entre elas entrou a **Criação**, as seis páginas em que o Tainted Magic conta de onde veio tudo — os Eldritch
  partindo o universo em dois, o Vazio de um lado e o Mundo de Cima do outro, as Pérolas Primordiais seladas nas
  Terras de Fora. É o texto que mais conversa com a lore de quem joga.

### Fatia 5 — as roupas e as bijuterias

- **Os dois óculos** (distorcidos e de metal do vazio): revelam o que está por trás do mundo, como os do Thaumcraft.
  Para isso o `Revealing` do mod ganhou a etiqueta `thaumcraft:revealing`, por onde um ramo de fora diz que o elmo
  dele também revela.
- **As botas do caminhante do vazio**: empurrão de doze centésimos por tique (o dobro com a faixa ligada), degrau de
  um bloco, pulo um quarto mais alto, queda amortecida acima de três blocos, cinco por cento de desconto de vis,
  conserto sozinho e cinco de distorção.
- **A faixa do caminhante**: bijuteria de cinto, vinte de escudo rúnico, dois de distorção, e o empurrão que se liga
  e desliga agachando com ela na mão — ligada, ainda dá cinco por cento de pulo.
- **As duas armaduras de fortaleza** (vazio e sombras), **o anel de Lumos** (visão noturna enquanto vestido) e **o
  amuleto de voo**, que troca aer de uma varinha do inventário por voo, e plana quando se agacha caindo.
- Tudo com os números do original, conferidos por teste: desconto de vis, distorção, degrau e pulo.



### Fatia 6 — os focos, as melhorias e as três criaturas

- **Seis focos**: enxame de mácula (chama um enxame do Thaumcraft atrás de quem a varinha aponta), matéria escura
  (esfera destrutiva, ou névoa contínua com a difusão), onda de choque (empurra tudo num raio de quinze blocos e
  machuca quem está a menos de sete), lasca de vis (persegue o alvo, quica nas paredes), Lumos (põe a luzinha) e a
  maça do mago (pesa na varinha e vira arma de perto).
- **Cinco melhorias** que só esses focos aceitam: sanidade, anticorpo, corrosiva, persistente e difusão — com os
  aspectos, os ícones e os postos do original.
- **Três criaturas**: a bola de matéria escura, a névoa e a lasca que persegue.
- **Duas portas novas no Thaumcraft**, porque o ramo precisava delas: `Focuses.register` (o que um foco de fora faz
  quando a varinha aponta) e `net.thaumcraft.api.FocusUpgrades` (melhorias e postos de fora, que a mesa de foco e o
  livro passam a consultar junto com os do mod).
- Os dois sons do original (a onda de choque e a lasca) entraram no `sounds.json` do Thaumcraft.

### Fatia 7 — as lâminas de fortaleza e o resto

Esta fecha o Maleficium: o que restava do Tainted Magic 8.1.1 entrou todo.

- **As três Lâminas de Fortaleza** (táumio, metal do vazio e metal das sombras): no original eram três subtipos de um
  item só, e aqui são três itens, com os danos do original (14,25, 17,5 e 20,75) e a distorção de cada metal (zero,
  três e sete). Não gastam uso. Segurando o clique direito por um segundo e soltando, sai o golpe carregado — uma vez
  e meia o dano, e de dez em dez vezes duas e meia. A de metal do vazio enfraquece quem ela acerta; a de metal das
  sombras enfraquece e dá fome.
- **As três inscrições** (Demônio Furioso, Espírito Vingativo e Deusa Benevolente): infusões que gravam a marca na
  própria lâmina do meio, e que servem para as três. Com uma delas, o golpe carregado vira bola de fogo, onda de
  choque ou cura — e a lâmina descansa sete segundos. Agachado, sai o golpe carregado de sempre.
- **O desenho das lâminas** (`FortressBladeRenderer`): o `ModelKatana` e o `ModelSaya` do original, caixa por caixa,
  com a bainha ao lado da lâmina e, quando ela é inscrita, as vinte e oito runas do `script.png` correndo pelo fio.
  A bainha aparece também na cintura de quem a carrega (`HipSheathLayer`), como o `IRenderInventoryItem` fazia.
- **O Medidor Rúnico** (`MaleficiumHud`): as dezesseis runas por cima da barra, que enchem enquanto o golpe carrega e
  esvaziam ao contrário enquanto a lâmina descansa.
- **O Desmontador Táumico**: não gasta uso — bebe cem centésimos de entropia por segundo das varinhas do inventário,
  até cinquenta mil, e queima essa carga para cavar (vinte, oito ou cento e vinte e oito, conforme o modo), para
  lavrar a terra em volta e para bater (vinte de dano). Agachado, o clique direito passa de modo em modo.
- **A Lâmina Primordial**: fere como nada mais, põe a definhar e a enfraquecer, conserta-se sozinha e, com o clique
  direito seguro, abre o redemoinho que puxa tudo num raio de quinze blocos.
- **A Chave do Portão Celeste**: prende-se a um lugar (uma vez só, e ganha uma cor sua) e leva de volta a ele depois
  de dois segundos de clique direito, desde que seja no mesmo mundo e o lugar esteja desimpedido.
- **O Frasco de Sangue Infundido com o Vazio**: na mesa comum, com uma peça de armadura qualquer, devolve a peça
  `tocada pelo vazio` — que dali em diante se conserta sozinha, esteja onde estiver no inventário.
- **O Cogumelo Mágico**: come-se depressa, cura, apressa e ensina um ponto de pesquisa de um primário sorteado.
- **A Forja Carmesim Avançada**, que faltava por causa do ícone: as três peças do Pretor Carmesim, que o ramo faz a
  partir das do Cavaleiro.
- **As armaduras de fortaleza do ramo passaram a usar o modelo de fortaleza do Thaumcraft**, com a folha de textura
  de cada uma, como no original — e por isso aceitam também as máscaras e os óculos por infusão.
- A tabela gerada fechou em **quarenta e oito pesquisas e setenta e nove receitas**, sem nada adiado: o gerador passou
  a entender o ícone pedido pelo nome (`ItemApi.getItem`), os subtipos da katana, o coringa de metadado das coisas do
  próprio Tainted Magic e as infusões que mudam a coisa do meio.
- Testes: o sangue do vazio (receita, conserto e uma vez só), os modos e a bebida de entropia do desmontador, os
  números das três lâminas, o que cada inscrição faz no golpe, a infusão que grava a inscrição, a chave que se prende
  e a lâmina primordial. Trezentos e quarenta e um ao todo, todos passando.


## O Magia Naturalis

O segundo ramo de fora: o **Magia Naturalis 0.5.0**, de elenterius (111 classes), que a lore de quem joga chama de
Thaumaturgia Aplicada — o caminho que amplia a thaumaturgia de sempre sem apodrecer. A lore mantém o nome do
original, então o ramo se chama assim mesmo.

Como o Maleficium, ele mora no mesmo jar, com figuras e textos no espaço de nome `thaumcraft`, aba própria no
criativo e no Thaumonomicon, e entra pelo `ThaumcraftApi`. As chaves de pesquisa dele levam o prefixo `MN_`, que
é a tradução do espaço de nome que o original usa.

A tabela também é gerada (`scratchpad/mn-tabela.js`, com `mn-itens.js` e `mn-lang.js`), lendo o `MNResearch` e o
`MNRecipes` do jar. O gerador aprendeu três coisas que o do Maleficium não precisava: acompanhar as variáveis
`aspects` e `recipe` que o original reaproveita entre receitas, resolver os *proxies* (pesquisas do Thaumcraft
copiadas para dentro da aba do ramo, que aqui viram a própria pesquisa do Thaumcraft) e escrever as receitas de mesa
como dados do jogo, e não só como página de livro.

### Fatia 1 — o ramo e as três foices

- A aba do ramo, o ícone e o fundo da aba do livro, tudo do original.
- **As três foices** — a de táumio, a do vazio e a da Abundância — com a ceifa em área: elas levam junto tudo o que
  for igual e estiver encostado, até onde cada uma alcança (duas, quatro e nove). A da Abundância colhe para o
  inventário e faz cair três vezes; a do vazio enfraquece quem ela acerta, conserta-se sozinha e distorce um.
  Agachado, a foice corta um bloco só.

### Fatia 2 — a madeira arcana

- **Os sete feitios** da madeira arcana, que no original eram um bloco só com o feitio no metadado: tábua deitada de
  madeira-grande e de prateada, tábua em pé de prateada, dois ornamentos, os de ouro e a cercadura, que só tem o
  desenho nos lados. Contam como tábua, como o dicionário de minérios do original dizia.

### Fatia 3 — os dois óculos

- **Os Óculos** e os **Óculos de Cristal Escuro**: revelam os nós e descontam vis — seis por cento os primeiros,
  cinco os segundos, e sete de Perditio, ou nove enquanto é dia.
- Os de cristal escuro não deixam ficar cego e mostram o contorno de quem está invisível (o `IRevealInvisible`); os
  Óculos escrevem no meio da tela o que é o nó para o qual se olha (o `renderSpectaclesHUD`).

### Fatia 4 — o Diário de Pesquisa

- Agachado numa Mesa de Decomposição, ele anota um ponto do aspecto que ela está tirando; o clique comum despeja
  tudo o que estiver anotado no caderno de quem o carrega, e o diário volta a ficar em branco.

### Fatia 5 — as duas pedras alquímicas

- **A Pedra do Catalisador Fenomorfo** troca um bloco pelo próximo da família dele — a lã muda de cor, o tijolo de
  pedra fica musgoso, a tábua vira a de outra madeira — e, agachado, deita ou levanta o que tiver eixo.
- **A Pedra do Alquimista de Mercúrio** sobe um degrau em cada efeito de quem a usa, cobrando um pó de brilho por
  efeito e cortando o tempo deles; sem pó, ou no terceiro degrau, ela castiga. Quem não descobriu Permutatio não
  consegue usá-la.

### Fatia 6 — o Foco de Construção

- Levanta uma forma de blocos a partir da face mirada — cubo, plano, plano estendido ou esfera —, cobrando cinco de
  Ordo por bloco e tirando os blocos do inventário; o alcance cresce três por posto de Ampliação.
- As teclas são as do original (`MNKeyBindings` mais o `PacketKeyInput`): **N** aumenta a área, **J** diminui,
  **B** passa à forma seguinte — e, com o Ctrl, ao jeito seguinte — e o botão do meio do mouse marca o bloco da mira.
  O foco tem os dois jeitos do original: constrói com o bloco marcado ou com o que estiver na mira.
- O canto de cima da tela mostra o bloco que ele vai pôr, quantos ainda há na mochila, a forma e o tamanho — é o
  `renderBuildFocusHUD`. (Por um tempo isso se fez agachado, enquanto as teclas não existiam; saiu quando elas
  chegaram, porque o original não tinha esse atalho.)

### Fatia 7 — a Bolsa de Focos do Fim

- Bijuteria de cinto que não guarda nada por si: o que ela mostra é o baú do fim de quem a carrega, e os focos que
  estiverem lá ficam ao alcance da varinha.

### Fatia 8 — o Bicho num Jarro

- O jarro guarda uma criatura viva inteira: clicado nela, ela entra do jeito que estava, e a dica do jarro diz quem
  está lá dentro. Posto no mundo, ele leva o bicho junto; quebrado, o bicho volta para o item; e uma batida de
  varinha quebra o vidro e solta quem estava preso. Gente e os dois chefes não cabem em jarro, como no original.
- O desenho é o do original: a criatura flutua no meio do vidro, encolhida para caber, girando devagar.

### Fatia 9 — o estandarte do ramo

- O estandarte do Thaumcraft com a figura do Magia Naturalis, que a folha do estandarte passa a aceitar por bloco.

### Fatia 10 — o Baú Arcano e as chaves de táumio

- **O Baú Arcano**, de madeira-grande (nove por seis) ou de prateada (onze por sete): quem o põe vira dono, e só o
  dono — e quem ele deixar entrar — o abre. Não liga para explosão, e para quem não entra ele nem se desgasta.
- A tampa abre e fecha como a do baú comum; as folhas entram no atlas dos baús do jogo, como as de lá.
- A varinha o encolhe de volta em item, levando o que havia dentro e a lista de quem entrava; posto de novo, tudo
  volta ao lugar.
- **As duas chaves de táumio**: a **Chave do Desvendar** liga uma alma à outra e, num bloco arcano de quem a forjou,
  dá a entrada a quem ela carrega; a **Chave do Endosso** junta uma lista de gente ao bater em cada uma e despeja
  todos de uma vez num bloco a que quem a leva já entra. A chave arcana do próprio Thaumcraft também abre caminho no
  baú, batendo nele.

### Fatia 11 — a Mesa de Transcrição

- A mesa arcana com a pintura do ramo, o diário deitado no tampo e a pena girando por cima.
- De dois em dois segundos ela sorteia uma das quatro mesas de decomposição postas em cruz a dois blocos dali e leva
  para o Diário de Pesquisa o primário que aquela mesa acabou de tirar. Cheio o diário — sessenta e quatro de cada
  primário —, ele desce pronto para a casa de baixo.
- O diário ganhou o teto de sessenta e quatro do original, e a mesa de decomposição passou a deixar levar o primário.

### Fatia 12 — o Geo-Pilone, o amostrador e as trocas da pedra

- **O Geo-Pilone**, em cima de um vão e de três totens de obsidiana, reescreve a terra num círculo de oito blocos —
  um pedaço por vez, cobrando de essência o que a terra nova carrega de aura (a conta do `BiomeHandler`, dois por
  cento da aura de cada marca). A varinha o liga e o desliga; agachada, refaz a cobrança.
- **O Relatório de Bioma** guarda a terra de um lugar — o nome, a cor da folhagem e o que ela cobraria — e afina o
  pilone com ela. A figura dele ganha a cor da terra guardada, como no original.
- **As trocas da Pedra do Catalisador** viraram receita própria (`MutationRecipe`), que devolve a pedra à bancada
  como o `getContainerItem` do original: as seis da madeira arcana e as trinta e duas da lã e da argila de cor.

### Fatia 13 — o Revenante Feroz

- O **Foco do Revenante** levanta do chão um zumbi pequeno contra a criatura da mira, a até trinta e dois blocos.
- Ele é de quem o levantou e não o ataca, arremete em cima do alvo, transforma em outro revenante metade dos aldeões
  que mata e desmancha quando o alvo morre ou passados quinze segundos. Cada posto de Potência engrossa o braço dele.

### Fatia 14 — a Criadora de Mácula e o Baú Maligno

- **A Criadora de Mácula**: a aranha grande da terra maculada (42 de vida, ligeira, imune ao veneno da mácula) que,
  ferida e com alguém para caçar, põe no mundo uma ou duas aranhas de mácula de um segundo em um segundo, com pressa
  e força de mais. Nasce na terra maculada e pode ser campeã, como o original pedia.
- **O Baú Maligno**, nos quatro feitios: pula atrás de quem o chamou, morde quem está caçando, come comida para se
  remendar e guarda trinta e seis coisas numa tela de quatro fileiras. O sino de golem o recolhe de volta em item —
  levando o que tinha dentro, ou derramando tudo se quem o recolhe estiver agachado. Creeper foge dele.
- Os quatro modelos saem do jar por `scratchpad/mn-baus.js`, e o item que chama o baú é desenhado com o modelo do
  feitio dele (o `RenderItemEvilTrunkSpawner`).

### Fatia 15 — as sombras das pesquisas do Thaumcraft

- O `ResearchItemProxy` do original: seis cópias ocas de pesquisas do Thaumcraft (óculos, arcanas protegidas, foco
  de troca, crisol, bolsa de focos e baú itinerante) entram na aba do ramo, escondidas e só de leitura, com o desenho
  e as páginas da pesquisa de que são sombra — e como irmãs dela, de modo que abrem junto. É por elas que as
  pesquisas do ramo nascem de um pé que está na aba delas.
- Com isso a tabela do ramo não adia nada: dezoito pesquisas e setenta receitas.

**Desvios declarados**: a Foice da Abundância pede um livro encantado qualquer, porque hoje um ingrediente não sabe
olhar encantamento; e a troca de terra usa o `fillbiome` do jogo, que é o que existe hoje no lugar do
`setBiomeAt` de então.

## O Forbidden Magic (2026-09-24)

O terceiro ramo de fora: o **Forbidden Magic 0.575**, de SpitefulFox (92 classes), que na lore de quem joga fica
com o próprio nome — magia humana proibida, e não coisa dos Ancestrais: os sete pecados, o Nether e o que os
thaumaturgos inventaram de pior. O código mora em `net.thaumcraft.forbidden`, as figuras e os textos no espaço de
nome `thaumcraft`, e as chaves de pesquisa levam o prefixo `FM_`.

O que depende de outros mods — Blood Magic, Botania, Ars Magica, EE3, Twilight Forest, Thaumic Tinkerer — fica de
fora, como o próprio original o punha atrás de `Loader.isModLoaded`.

### A porta dos aspectos

Para um ramo de fora criar aspecto novo e somar aspecto ao que já existe, a porta cresceu:

- `ThaumcraftApi.aspect(nome, cor, pai, mãe, mistura)`, que é o construtor público do `Aspect` que os addons do
  original usavam;
- `ObjectAspects.Registrar.add` e `.blockAdd`, que somam ao que a coisa já tem — o `getObjectAspects`, somar, e
  registrar de volta;
- `EntityAspects.onRegister`, com `add(criatura, variante, aspectos)`, que é o `scanEntities` que os addons
  percorriam.

O teste da tabela de aspectos passou a contar os quarenta e oito que o **próprio Thaumcraft** declara (os campos da
classe `Aspects`), e não tudo o que estiver na tabela, que agora recebe os dos ramos.

### Fatia 1 — os sete aspectos sombrios

- **infernus** (fogo + magia), **ira** (arma + fogo), **gula** (fome + vazio), **invidia** (sentidos + fome),
  **superbia** (voo + vazio), **desidia** (armadilha + alma) e **luxuria** (carne + fome), com a cor e a mistura do
  original.
- E o que eles somam: dezenove coisas do jogo (a pedra do Nether, a estrela, o bolo, a dinamite, a cama...), seis
  anotações inteiras e quinze criaturas — o creeper carregado levando mais ira que o comum, como lá.
- Tudo sai de `scratchpad/fm-aspectos.js`, que lê o `DarkAspects` do jar.

### Fatia 2 — os oito fragmentos dos pecados

- Os sete do `ItemDeadlyShard` — ira, inveja, mácula, soberba, luxúria, preguiça e avareza — e o da gula, que se
  come, com a aba do ramo no criativo.
- E de onde eles vêm, do `FMEventHandler`, tudo no Nether: a **Preguiça** de quem morre sozinho; a **Ira** de quem
  morre por arma forte (a chance cresce com o dano, com os encantamentos de briga e com a Potência do foco da
  varinha); a **Soberba** dos chefes; a **Avareza** de quem caça com pilhagem ou foco de tesouro; a **Inveja** do
  porco-zumbi que nasceu com um na mão (um em cada cento e setenta e cinco); e a **Gula** de quem come no Nether.
- Quem come é o `LivingEntityEatMixin`, porque o jogo não anuncia isso por evento.

### Fatia 3 — a árvore maculada

- O tronco, as tábuas, as folhas, a muda, a pedra maculada e os tijolos dela, com a árvore do
  `WorldGenTaintedTree` — o carvalho de então, feito de madeira maculada, que só cresce onde couber inteira.
- As folhas caem a muda e, uma vez em vinte ao apodrecer, um **Fruto Maculado**: ele enche a barriga, mas gruda um
  ponto de distorção, meio minuto de mácula e de fome e, quatro vezes em dez, a taumarreia.
- E o **carvão maculado**, que sai do tronco na fornalha.

**Falta**: as ferramentas (pás, picaretas, machados, o garfo, as morfas), as peças de varinha (hastes e coifas
sombrias), o foco do piscar, o bolo arcano, a flor de tinta, a gaiola da ira, as bijuterias, os oito encantamentos
sombrios, as duas poções e a tabela de pesquisas e receitas.

## Quatro consertos do que quem joga viu (2026-09-25)

### Os focos sem nome

O `FocusItem` montava o nome na hora, `item.thaumcraft.focus.<tipo>`, e só os dez focos do Thaumcraft tinham essa
chave: os seis do Maleficium e os dois do Magia Naturalis apareciam com a chave na cara. Agora o nome de um foco é
o nome do **próprio item** — um foco de ramo de fora ganha nome amigável só de se registrar, sem chave à parte —, e
as dez chaves repetidas saíram dos dois idiomas.

O `NamesGameTest` passou a conferir o **nome que a dica mostra**, e não só a chave crua do item, andando pelos
pedaços do texto: é assim que um `getName()` que monta chave na hora deixa de passar despercebido.

### O Baú Maligno no ar

O `CorruptedTrunkModel` do original faz um `glTranslatef(-0.5F, 0.5F, -0.5F)` antes de desenhar, porque as peças
dele são medidas a partir do canto do bloco e não em volta dos pés do bicho. Sem isso o baú ficava meio bloco no ar
e meio bloco de lado — era o que parecia pele quebrada. O `scale` do desenhista faz esse mesmo empurrão, que em
26.2 cai no mesmo lugar do `preRenderCallback` de então.

Os baús arcanos, esses, estavam certos: postos lado a lado com um baú do jogo, saem iguais, com a folha de
madeira-grande e a de prateada no lugar.

### O bicho grande demais no jarro

Eram dois males. O primeiro derrubava o jogo: o bicho do jarro nunca entrou no mundo, e o desenhista do jogo pede
o **id** dele para escolher o modelo do que ele tem na mão — sem id, estoura. O gerador de monstros do jogo passa
por isso dando um id de mentira (`-1`) ao mostruário dele, e o jarro passou a fazer o mesmo.

O segundo era o tamanho: o `TileJarPrisonRenderer` encolhe o bicho a um valor fixo, `0,21875`, e o vira para quem
chega a menos de quatro blocos e meio; de longe ele roda devagar. A porta daqui encolhia conforme o tamanho do
bicho, e um porco saía mais que o dobro do que cabia. Agora é o número do original.

O `SkinClientTest` põe um porco dentro de um jarro, e é ele que segura os dois: se o id voltar a faltar, o teste
de tela derruba o cliente.

### Os focos que não desenhavam nada

O tiro único da varinha — o `cast` — voltava cedo do lado de quem vê, e por isso os focos de ramo de fora nunca
desenhavam coisa alguma. Agora ele corre dos dois lados, como o `onFocusRightClick` do original, e o `Focuses`
ganhou uma porta (`registerClient`) para cada ramo pendurar a metade `isRemote` do foco dele. O Maleficium
pendurou as três que faltavam:

- a **onda de choque**, com um raio do peito de quem lançou até cada um que ela pega, e cinco faíscas em volta;
- a **lasca de vis**, com dezoito faíscas de onde ela sai;
- e o **Lumos**, com as nove faíscas do lugar em que a luz acendeu.

**Desvio pedido**: no original o Lumos é uma luz invisível que solta uma faísca a cada quinze tiques, e quase não
se acha. A pedido de quem joga ele acende agora também um **Nitor branco** — a mesma chama do Nitor, com os mesmos
jatos, só que branca. O resto do Lumos continua igual: as faíscas, o som de gelo ao quebrar e a luz que ele dá.

## O sinistro, o demoníaco e a folha de 128 (2026-09-25)

Postos os quatro feitios do Baú Maligno de frente, um a um, dois ainda saíam errados — e a culpa não era da
posição, mas da **folha de textura**: o `CorruptedTrunkModel` e o `TaintedTrunkModel` do original dizem
`setTextureSize(64, 64)`, mas o **sinistro** e o **demoníaco** dizem `setTextureSize(128, 64)`. O gerador dos
modelos não lia esse número e escrevia 64x64 para todos, então cada peça desses dois ia buscar a figura no dobro do
lugar certo — as asas do demoníaco saíam de um pedaço vazio da folha, e o frasco do sinistro, de outro.

O gerador passou a ler o tamanho da folha do próprio jar, e também a **mistura**: o sinistro é o único que liga o
`glBlendFunc` para desenhar o vidro do frasco que ele carrega na cabeça. Por aqui isso vira o tipo de desenho do
modelo inteiro (`entityTranslucent`), e as peças opacas, que têm alfa cheio, saem iguais. O frasco voltou a ser
vidro, com o cérebro aparecendo lá dentro.

O `SkinClientTest` agora tira uma foto de cada feitio, sozinho e de frente, que é a única maneira de saber qual é
qual; e o `NaturalisGameTest` ganhou o caminho inteiro do jarro — pegar o bicho da mão, pô-lo no bloco e
devolvê-lo ao item quando o vidro se quebra.

## A troca de pele dos baús e a forma do jarro (2026-09-25)

### Cada baú com a própria pele

Quem joga notou o que uma foto de um baú só nunca mostraria: **os baús trocavam de pele conforme o que houvesse
em volta**. O desenhista guardava o modelo do feitio no `extractRenderState` — e em 26.2 o jogo **lê todos os
bichos do quadro antes de desenhar qualquer um**, de modo que o último lido mandava no modelo de todos, enquanto a
pele continuava a de cada um: um baú demoníaco ao lado de um maculado saía com o corpo de um e a figura do outro.

A escolha passou para o `submit`, que corre por bicho na hora de desenhar. O teste de tela agora põe os quatro
feitios juntos antes de os retratar um a um, que é a única cena em que isso aparece.

### O jarro era um caixote

O `BlockJarRenderer` do original desenha o jarro em duas caixas — o corpo, de três a treze e doze de alto, e a
tampinha, de cinco a onze e mais dois — com a pele do lado do Magia Naturalis e o topo e o fundo do jarro do
Thaumcraft. O modelo daqui era um caixote de doze por catorze, sem as coordenadas de figura e sem vidro. Agora ele
é o mesmo modelo do jarro do Thaumcraft, com a pele do ramo, e o vidro voltou a ser vidro.

E ele cintila: o `randomDisplayTick` do original solta, uma vez em quatro, uma faísca dourada
(`0xFFCC00`) em volta do vidro.

### Fatia 4 — as ferramentas do ramo

- A **Pá do Purificador**, que cava mácula como quem cava terra, limpa a gosma e o gás de fluxo de onze por nove
  por onze com um clique (um ponto de vida por bloco, até quinze) e, cavando mácula, tira dali um Fragmento de
  Mácula — a Fortuna e o Toque Suave melhoram a sorte, como no `onHarvest` do original.
- A **Picareta da Distorção**, a **Espada do Garfo do Diabolista** — que é de táumio e vai ser a chave da Gaiola
  da Ira — e o **Chicote de Montaria**, que apressa o cavalo e o porco que apanham dele, dá pressa, força e mão
  de obra a gente e golem, e no Nether às vezes arranca um fragmento.
- E o **Machado do Tomador de Crânios**, que decepa: o esqueleto uma vez em vinte e seis mais a pilhagem, o zumbi
  e o creeper o dobro dela, e quem joga uma vez em onze — a cabeça sai com o nome do dono.

### Fatia 5 — as ferramentas camaleão

As quatro (picareta, espada, pá e machado) guardam **três caras** cada uma. Agachado, com o botão direito, a
ferramenta troca de cara: os encantamentos e o nome que ela tinha ficam na cara de onde ela saiu, e voltam os da
cara para onde ela foi — cinco de vida por troca, e não se troca com a ferramenta no fim. É o `enchants0..2` e o
`Name0..2` do NBT do original, aqui num pedaço de dado (`MorphSlot`) com o mesmo feitio.

O olho da figura diz em que cara ela está, com as três cores do original — vinho, azul e ouro —, pintado pela cor
que o próprio item carrega.

### Fatia 6 — o Foco do Piscar

O salto até onde a varinha aponta, a até cento e vinte e oito blocos, com as duas melhorias que só ele tem: o
**Fogo do Inferno**, que incendeia (e machuca em três mais três por Potência) quem estiver onde ele chega, e o
**Pandemônio**, que manda os monstros de lá para onde ele saiu. O custo muda com a melhoria posta — entropia,
entropia com fogo, entropia com ordem —, como no `getVisCost` do original.

### Fatia 7 — o bolo, as flores e as estrelas

- O **Bolo Arcano**, de doze fatias, que volta a crescer sozinho: cada batida do acaso repõe uma fatia. Cada
  garfada enche dois de fome com um de saturação.
- A **Flor de Tinta**, que se espalha sozinha, mas só até dez num pedaço de cinco por três por cinco, e a
  **Roseira Umbria**, de dois blocos, que espalha as flores — três vezes em dez, ou de uma vez com farinha de osso.
- O **Bloco de Estrelas do Nether**, que serve de base de farol, e a **Pepita de Esmeralda**.
- A tinta preta do original era um item próprio, porque em 1.7.10 as tintas eram números de um item só; aqui ela é
  a tinta preta do jogo, que é a mesma coisa.

### Fatia 8 — as peças de varinha do ramo

- A **haste maculada**, que em terra maculada repõe um de cada primário a cada cem tiques, até um décimo do que
  cabe.
- A **haste infernal**, que faz o mesmo no Nether (e o fogo em qualquer lugar, até um quinto), apaga o fogo de
  quem a leva e cura o definhamento.
- A **haste profana**, que é um pacto: ela repõe o vis de graça até gastar as vinte e cinco mil que prometeu,
  grudando distorção pelo caminho (uma chance em duas mil e quinhentas por ponto reposto) — e, gasto o pacto, ela
  vira a **haste profanada**, que não faz nada, e cobra mais um ponto de distorção.
- E a **ponta alquímica**, que desconta dez por cento do vis e vinte por cento no de água.

As de brincadeira do original — a haste de neutrônio, feita de bedrock, e a ponta de oricalco, feita de bloco de
comando — ficaram de fora, como o resto do que só existe em modo criativo.

### Fatia 9 — os tinteiros e as duas bijuterias

- O **Tinteiro de Cristal**, que escreve como os outros mas, gasto até o fim, devolve de quatro a sete pontos de
  cada primário ao caderno de quem o usou — e vira pena e tinteiro comuns.
- O **Tinteiro Primordial**, que não seca nunca. As ferramentas de escrita ganharam uma porta (`spendsInk`) para
  isso, que é como o original faz, não deixando o dano subir.
- O **Anel da Nutrição**, que rende dois de fome e dois de saturação a mais em cada garfada.
- E a **Coleira do Pacto**, um amuleto de vis do tamanho do grande que converte a dor de quem a veste em vis —
  três centésimos por ponto de dano, seis se quem bate estiver de chicote.

### Fatia 10 — os oito encantamentos sombrios

Em 26.2 os encantamentos são dados, e não classes: cada um é um arquivo com o que ele encanta, o que custa e
quanto sobe. Só a **Ira** entra na mesa de encantamento (é a única que o original deixa), e ela é inteira de
dados — um e um quarto de dano por nível, pelo efeito `minecraft:damage`. As outras sete se põem por livro e o
que fazem está no `ForbiddenEnchantments`:

- **Aglomerante** (até IV), na picareta camaleão: sem Fortuna, o minério vira aglomerado nativo, vinte por cento
  mais sete e meio por nível;
- **Capitalista**: sem Pilhagem, o aldeão larga uma esmeralda e o monstro, três vezes em trinta e cinco, uma
  pepita;
- **Consumidora**: come o lixo que cai (terra, areia, cascalho, pedregulho e pedra do Nether);
- **Educativa** (até V): sem Pilhagem, quem morre dá três vezes mais experiência por nível;
- **Corruptora**: uma vez em três, os fragmentos de cristal saem como fragmentos de pecado;
- **Tocada pelo Vazio**: a ferramenta camaleão se conserta um ponto a cada dez tiques;
- **Impacto**: a picareta e a pá camaleão quebram três por três, no plano da face de quem cava.

As três que mexem no que o bloco larga entram pelo mesmo mixin que já servia à picareta elemental.

**Fica de fora**: as duas poções do original (o Selo de Sangue e a Praga do Dragão), porque quem as aplica é o
Rapieira de Sangue do Blood Magic e o Matadragões, que só faz o que faz com o Draconic Evolution instalado.

### Fatia 11 — a Gaiola da Ira

O gerador de monstros que não nasce do mundo: afinado com um **Cristal de Marca** (que se marca matando o bicho
com o Garfo do Diabolista), ele come **essência** para trabalhar. Guarda até sessenta e quatro de três coisas — a
essência do próprio bicho, Ira e Desídia —, e cada cinco delas rendem quatro bichos, três por vez, num raio de
quatro e nunca mais de seis por perto. O Garfo troca o modo, que diz qual das três ela puxa dos canos; comendo
Desídia ela trabalha devagar, como no original.

A tabela de que essência cada bicho é feito é a do original (`spawnerMobs`), com os quarenta e dois bichos dele
traduzidos para os nomes de hoje — e quem não está nela pede cobiça.

### Fatia 12 — a árvore, as receitas e o livro

A aba do ramo no Thaumonomicon, com a figura e o fundo do original, e as **trinta pesquisas** dele nas mesmas
casas, com os mesmos custos de aspecto, os mesmos pais e as mesmas marcas (as redondas, as escondidas, as
secundárias). As chaves levam o prefixo `FM_`, e os pais que são do próprio Thaumcraft continuam com o nome que
têm lá.

As **vinte e cinco receitas** que fecham com o que já existe: as ferramentas, as camaleão, o bolo, as flores, o
foco do piscar, as duas hastes, a gaiola, o cristal, as estrelas, as esmeraldas e os oito encantamentos — estes
últimos por infusão na própria peça, que é o `addInfusionEnchantmentRecipe` do original. Quem lê uma página de
receita de encantamento de um ramo de fora agora a acha: o `BookPages` passou a aceitar a receita crua, como já
fazia com as de crisol e de infusão.

E o livro inteiro em **inglês e português**: trinta nomes, trinta subtítulos e trinta e cinco páginas de texto.

**Fica de fora** do ramo, por depender de outros mods: o Rapieira de Sangue, os poços de sangue e vínculo e o Orbe
Divino (Blood Magic), as hastes e pontas de Botania, Ars Magica, Witchery e Equivalent Exchange, o Matadragões
(Draconic Evolution) e as duas poções, que só vêm desses. Também ficam de fora as duas peças de brincadeira feitas
de bedrock e de bloco de comando.

## O Ars Mortuorum (2026-09-25)

O quarto ramo de fora: o **Necromancy 1.7.10**, de sirolf2009 (119 classes), que na lore de quem joga se chama
**Ars Mortuorum** — a arte de costurar os mortos. O código mora em `net.thaumcraft.mortuorum`, as figuras e os
textos no espaço de nome `thaumcraft`, e as chaves de pesquisa levam o prefixo `AM_`.

### Fatia 1 — as coisas que se tiram dos mortos

- Os quatro avulsos do `ItemGeneric`: a **Agulha de Osso**, a **Alma num Pote**, o **Pote de Sangue** e o
  **Cérebro no Espeto**.
- Os cinco **órgãos** do `ItemOrgans` — miolos, coração, músculo, pulmões e pele —, que se comem: dois de fome,
  pouca saturação e meio minuto de fome oito vezes em dez, como no original.
- E as **cinquenta e quatro peças de corpo** do `ItemBodyPart`: dezesseis bichos, cada um com os pedaços que o
  original lhe dá (da vaca saem quatro; do lobo, só a cabeça).

Onde o original guardava tudo isso em três itens com muitos números, aqui é um item por coisa, que é como o jogo
de hoje faz — e cada um leva a figura que tinha lá.

### Fatia 2 — a Máquina de Costura

O `BlockSewing` e o `ContainerSewing` do original: uma grade de **quatro por quatro** — não de três por três —,
mais duas casas com o que a costura gasta (a **agulha de osso** e a **linha**) e a casa de onde sai a peça. Sem
agulha ou sem linha, nada sai, mesmo com o desenho certo; e tirar a peça gasta uma de cada coisa da grade, uma
agulha e uma linha, como no `SlotSewing`.

As **cinquenta e cinco receitas** saem do `initDefaultRecipes`: cada peça de corpo tem o desenho do pedaço —
cabeça, tronco, braço ou pernas — com a pele, o osso e os órgãos nas mesmas casas, e a carne do bicho no lugar das
letras E e F (a vaca pede carne de vaca; o golem de ferro pede abóbora na cabeça e bloco de ferro no resto). Mais a
pele, que sai de um couro qualquer, oito de cada vez, sem forma nenhuma.

A receita com forma anda pela grade e aceita o desenho espelhado, como a da bancada do jogo.

**De passagem**: o teste do frenesi do guardião-mor era instável, porque o escudo dele é absorção e ele às vezes já
nascia com ela; agora o teste zera o escudo antes de bater, que é o que ele queria provar.

## Os Reinos Fragmentados (2026-09-25)

O quinto ramo de fora: as **Dimensional Doors 3.2.3**, de StevenRS11 e da equipa dimdev, que na lore de quem joga
são os **Reinos Fragmentados** — os bolsos que se abrem entre um lugar e outro. O código mora em
`net.thaumcraft.shattered`, as figuras e os textos no espaço de nome `thaumcraft`, e as chaves de pesquisa levam o
prefixo `SR_`.

### O que já lá está

- Os **trinta e quatro tecidos**: dezesseis cores de tecido comum, dezesseis de tecido antigo, o **Tecido Eterno**
  e o **Tecido Desfiado**, que é o chão do Limbo.
- As **quatro portas dimensionais** — carvalho, ferro, ouro e quartzo —, cada uma com uma **fenda** a morar na
  metade de baixo. Quem atravessa uma porta aberta sai num bolso; sem destino marcado, a primeira travessia abre
  um bolso novo e a porta passa a apontar para ele.
- A **fenda solta**, que fica no ar onde uma porta esteve, e que vai comendo o mundo em volta — do que ela come
  sai, de quatro em quatro mordidas, um **Fio do Mundo**, que é de onde vem todo o resto do ramo.
- A **Assinatura de Fenda** e a **Estabilizada**, que ligam dois lugares, e o **Fecha-Fendas**.
- O **Limbo**: um mundo próprio, de terra desfiada sobre chão de tecido eterno, com o gerador dele; e os
  **bolsos**, que moram todos num mundo só, lado a lado numa grelha, cada um uma sala forrada de tecido com a
  porta de volta na parede.
- Os **Monólitos**, que olham quem entra no Limbo.

O mundo dos bolsos e o Limbo abrem-se com o jogo a andar, pelo `DynamicDimensions` — o jogo de hoje não deixa
registar mundos em código como a 1.7.10 deixava, então eles nascem na primeira vez que alguém precisa deles.

### Fatia das ferramentas — a lâmina, o ferro e a armadura

- A **Lâmina de Fenda** (`ItemRiftBlade`): corta como espada de ferro e, com o botão de usar, salta. Havendo uma
  fenda na linha de visão, ela atravessa-a; havendo um bicho, leva quem a empunha para junto dele. Quanto mais
  gasta a lâmina, mais longe e mais torto o salto sai — é a única coisa que o gasto dela muda. Vinte tiques de
  espera entre saltos, e conserta-se com Tecido Estável.
- O **Firma-Fendas** (`ItemRiftStabilizer`): usado numa fenda solta, prende-a — ela deixa de comer o mundo em
  volta. Seis usos, e numa porta não faz nada.
- A **armadura de Fio do Mundo Tecido**: as quatro peças do `ItemWovenWorldThreadArmor`, com os números do
  original — vinte de durabilidade, 5/4/3/2 de proteção e vinte de encantabilidade —, que se consertam com Fio do
  Mundo. Tecem-se do fio, e também se forram por cima de uma peça de couro, como lá.

**Uma diferença declarada:** o original tem uma opção de configuração que troca o Tecido Estável por pérola do fim
em todas as receitas. Aqui vale sempre o Tecido Estável, que é o que o mod faz de fábrica.

**Ainda fica de fora** do ramo: o rosto da fenda solta (no original ela é um rasgão preto que treme no ar, feito
de um sistema-L), o tamanho das fendas e o registo delas, os alçapões dimensionais, a Ferramenta de Ajuste de
Fenda, as portas de ouro e de quartzo comuns e o disco de música.

### Fatia do rosto — o rasgão da fenda solta

A fenda solta era invisível: estava lá, comia o mundo em volta e levava quem a atravessasse, mas não se via. Agora
tem o rosto do original — um **rabisco de dragão pintado de preto**, pendurado no ar, que treme.

O desenho vem de um **sistema-L**: quatro rabiscos (o terdragão, o dragão, o dragão duplo e o vórtice) em várias
gerações, catorze ao todo, e cada fenda escolhe o seu quando nasce, junto com o lado a que fica virada. Três
coisas mexem nele ao mesmo tempo, e são as três do original: o **tremor**, que abana o rasgão inteiro e cresce com
o cubo do tamanho dele; o **esvoaçar**, que mexe cada canto por conta própria com dez ondas a correr; e o
**giro**. O tempo de cada fenda é o dela — a conta leva um número tirado do lugar onde ela está, de modo que duas
fendas lado a lado não tremam juntas.

A fenda também **cresce sozinha**, como no original: dez vezes por tique ela soma `1/(tamanho+1)` ao tamanho, e o
rasgão cresce com ele. Presa pelo Firma-Fendas, para.

**Três diferenças declaradas**, todas de dentro:

- O original monta os sistemas-L ao arrancar e recorta o contorno de cada rabisco com uma biblioteca de Delaunay.
  Aqui as catorze formas vêm prontas num arquivo de dados, feito por `scratchpad/dd-curvas.js` com o mesmo
  sistema-L e a mesma ordem; e em vez de recortar o contorno, pintam-se as casas que o rabisco ocupa, juntas em
  tiras deitadas. A silhueta é a mesma — o rabisco é uma união de quadradinhos, toda da mesma cor — e fica sem os
  buracos que o corte deixa onde o contorno toca em si mesmo.
- O original pinta o rasgão com uma mistura que escurece o que está atrás, e essa mistura já não existe no jogo de
  hoje; aqui ele vai de preto quase opaco, que é o que se via lá.
- O original esvoaça seis décimos, e o tamanho dele nunca para de crescer. Aqui o esvoaçar é de dois décimos e
  meio — com as tiras pequenas, seis décimos esfarelavam o rasgão — e o tamanho para em seiscentos, senão ao fim
  de uma hora o rasgão teria vinte blocos de ponta a ponta.

### Fatia das outras portas — o alçapão e as duas de enfeite

- O **Alçapão Dimensional** (`BlockDimensionalTrapdoor`): é a porta deitada. Tem a fenda a morar nele, e quem cai
  por ele aberto sai num Reino Fragmentado; atrás de quem passa ele fecha-se, a não ser que haja redstone a
  segurá-lo. O original só tem o de madeira, e aqui é o mesmo.
- A **Porta de Ouro** e a **Porta de Quartzo**: as duas portas comuns do ramo, que não levam fenda nenhuma. São
  matéria-prima — a porta dimensional de ouro faz-se de uma delas com Tecido Estável, e a de quartzo da outra.

Com elas o ramo fecha a lista de coisas que se constroem. **Ficam ainda de fora**: o tamanho e o registo das
fendas (lá as fendas falam umas com as outras e chamam endermen), a Ferramenta de Ajuste de Fenda, a Placa de
Marcação, o disco de música e as salas de esquema — no original os bolsos vêm de `.schem` guardados no jar, com
ruínas, prisões e bibliotecas, e aqui a sala ainda é lisa.

### Fatia da placa — e o que no original está por acabar

A **Placa de Marcação** (`BlockMarkingPlate`): um poste alto e fino, de quase dois blocos, que serve para marcar um
lugar. No original não faz mais nada — é enfeite —, e é assim que vem para cá.

Com ela o ramo fica completo no que dá para pôr no mundo. **Duas coisas do original não vieram porque lá também
não funcionam**, e isso só se vê lendo o código:

- O **chamado dos endermen**: a fenda devia chamar um enderman de vez em quando, mas o
  `TileEntityFloatingRift.spawnEndermen` só corre se o `updateNearestRift` disser que sim — e na 3.2.3 esse método
  devolve `false` sempre. Nunca chama ninguém.
- A **Ferramenta de Ajuste de Fenda**: ela abre uma tela, e a tela (`GUIRiftConfigScreen`) desenha o fundo e o
  título e mais nada. Não há um controlo nela.

Se algum dia quiser trazê-las, é escrever o que lá falta, e não portar — e isso é outra conversa.

## Um susto que não era do mod (2026-09-26)

A suíte de tela falhou duas vezes em sete com um erro feio ao criar mundo:

```
Unbound values in registry ResourceKey[minecraft:root / minecraft:worldgen/biome]:
    [thaumcraft:magical_forest, thaumcraft:tainted_land]
```

Não era do mod. Os dois arquivos de bioma estão certos, e o que faltava era **o arquivo em si**, por um instante:
a suíte lê os recursos de `build/resources/main`, e uma compilação correndo ao mesmo tempo reescreve essa pasta
por baixo do jogo. Quando a leitura dos registos calha no meio da reescrita, o bioma não está lá e fica por
ligar.

Prova: com a suíte a correr, forçaram-se oito reescritas seguidas do `magical_forest.json`, e o erro apareceu,
nomeando esse mesmo bioma e o vizinho dele na pasta. Sem nada a compilar em paralelo, três voltas seguidas
passaram limpas.

**A regra que fica:** não compilar enquanto a suíte de tela corre. Se o erro voltar sem nada em paralelo, então aí
sim é do mod, e o que se procura é quem refere esses dois biomas antes de os dados carregarem.

## As duas foices, por fim (2026-09-26)

Depois de umas quantas voltas, o que quem joga queria era simples: **as duas foices no feitio das sete caixas do
`ModelScytheBone`**, e a folha a separá-las — a de sangue com a do original, de cabo de madeira, e a de osso com
a mesma folha passada a osso, de cabo e tudo.

Saíram, então, as sete caixas do `ModelScythe` (a foice de sangue de origem) e o modelo de Blender do
`scythe.obj`, que no original só aparecia a quem estivesse numa lista de nomes que o mod ia buscar à rede. Ficam
no histórico.

**De passagem, uma armadilha do original que vale guardar:** o `ModelScytheSpecial` **não segue o `.mtl`**. O
arquivo de materiais manda o cabo usar a `cloth.jpg`, e o desenhista liga-o à `guntex.jpg` antes de o desenhar. É
a folha do desenhista que vale — quem for ler um `.obj` de mod da 1.7.10 que olhe primeiro para quem o desenha.

## Os Óculos do Véu (2026-09-26)

Ideia de quem joga, e **do porte, não do original**: nas Portas Dimensionais toda a fenda se vê desde o primeiro
dia, e então não há nada para descobrir. Aqui as fendas continuam a estar no mundo desde sempre — o `RiftFeature`
já as espalhava —, mas **só aparecem a quem aprendeu a vê-las**, que é o que o Thaumcraft faz com tudo o mais.

O que quem manda decidiu, quando lhe perguntei:

* **Óculos novos do ramo**, e não uma melhoria dos que já havia: os **Óculos do Véu**, feitos dos Óculos da
  Descoberta com Fio do Mundo em volta, com pesquisa própria na aba dos Reinos Fragmentados.
* **Só as fendas que já estavam no mundo** pedem os óculos. A porta que o thaumaturgo assentou e a fenda que ele
  rasgou com a Assinatura ficam à vista de qualquer um — quem rasgou sabe onde rasgou.

### Como se sabe de quem é cada fenda

O `RiftBlockEntity` ganhou um `natural`, que **vem ligado**. Quem põe fendas sem passar por mãos de ninguém é a
geração do mundo, que chama `setBlock` e nada mais; quem as faz de propósito passa por um destes dois sítios, e
os dois desligam-no:

* `DimensionalDoorBlock.setPlacedBy` e `DimensionalTrapdoorBlock.setPlacedBy` — quem assenta passa por aqui, a
  geração do mundo não. É essa a linha que separa as duas.
* `RiftSignatureItem.rift` — a fenda que a Assinatura rasga é de quem a rasgou.

### Quem enxerga

O `VeilSight` é o irmão do `Revealing`: aquele diz quem vê os nós de aura, este quem vê o que está por trás do
mundo. Tem a etiqueta `thaumcraft:sees_the_veil`, por onde um mod de fora mete o elmo dele, e os Óculos do Véu
entram também na `thaumcraft:revealing` — são os da Descoberta melhorados, e não perdem nada do que eles faziam.

**Uma manha que vale guardar:** o `animateTick` do `FloatingRiftBlock` também tinha de saber quem está a olhar, e
é código comum — no servidor dedicado a classe `Minecraft` não existe. Em vez de lhe tocar, o `VeilSight` tem um
`localPlayer` que quem corre do lado de quem joga preenche no arranque, que é o mesmo jeito do `clientTrail` dos
orbes de foco. Sem isso, as fagulhas denunciavam a fenda a quem não a devia ver.

### A folha

A dos Óculos da Descoberta, com as lentes de ametista trocadas pelo vazio: uma rampa de preto-azulado com umas
poucas fagulhas brancas dentro. Quem é lente reconhece-se pela cor e não pelo lugar — um pixel em que o azul manda
sobre o vermelho e o verde —, e por isso a mesma conta serviu à folha do item e à da armadura. Gerador em
`Veu.java`, no rascunho.

**Guardas:** o `VeilSightGameTest` cobre as quatro coisas — a fenda do mundo nasce natural, a porta assentada e a
fenda da Assinatura não, os óculos abrem o olho (e os da Descoberta sozinhos não), e os do Véu continuam a
revelar os nós. O `RiftWorldClientTest` tira três retratos da mesma fenda: sem óculos (nada), com óculos (o
rasgão e as fagulhas) e a nossa sem óculos (à vista).

## O feitio da fenda, segunda volta (2026-09-26)

Também a pedido: as fendas deixam de ser o rabisco de dragão do original e passam a ser **um talho alto e preto**,
de beiras roídas, afilado nas duas pontas, com uma gavinha ou outra a sair-lhe do lado e fagulhas de estrela a
piscar em volta — brancas na maior parte, e uma em cada três puxada para o roxo do vazio.

O `RiftTear` monta-o do número que a fenda sorteou quando nasceu: dele saem o torcer da espinha, o inchar da
barriga, as gavinhas e o lugar de cada fagulha. Duas fendas do mesmo número são iguais; de números diferentes,
não. O que mexe continua a ser do original — o tremor, o esvoaçar das dez ondas e o giro —, e as fagulhas vão
levadas pelo mesmo tremor, senão descolavam-se do talho quando a fenda abana.

**Uma coisa que mudou por baixo:** o rasgão do original é largo e quadrado, e a medida dele saía da largura; o
talho é alto e estreito, e por isso quem manda na medida passa a ser o lado maior. Com a largura, um talho de
quatro blocos de altura ficava do tamanho de um dedo.

Saíram daqui o `RiftCurves`, o `rift_curves.mesh` e o `scratchpad/dd-curvas.js` que o fazia, e com eles o ouvinte
de recarga que só existia para os esquecer. Ficam no histórico, que é onde hão de estar se alguém quiser o feitio
do original de volta.

## Da fenda presa à sala (2026-09-26)

A terceira coisa que quem manda pediu: *o jogador poderia ter que estabilizar a fenda pra conseguir abrir uma
porta, e aí cai em uma daquelas salas aleatórias que saem se conectando — uma é uma biblioteca, na próxima porta
um pedaço de deserto, a outra o Nether, a outra um pedaço de um reino antigo*.

### A regra da porta

Quem manda nisso é o `DimensionalDoorItem`, e não o bloco: o que a fenda sabia tem de ser lido **antes** de a
porta lhe tomar o lugar, porque assim que o bloco troca o miolo dela vai-se e leva o destino consigo. São três
casos:

* numa fenda **solta**, a porta não pega — o aviso aparece e a porta fica na mão;
* numa fenda **presa** pelo Firma-Fendas, a porta toma-lhe o lugar e tudo o que ela sabia, e fica **brava**;
* **longe de qualquer fenda**, a porta assenta-se como sempre e abre o bolso liso do original.

### As salas

Um bolso bravo sai com um dos quatro temas do `PocketThemes` e com **três portas**: a de volta, no meio da parede
do norte, e mais duas nas paredes de lado, que ainda não apontam para lado nenhum. Quem atravessar uma delas abre
outro bolso bravo, de outro tema — o sorteio nunca repete o tema de onde se veio —, e é assim que as salas se vão
ligando. O bolso liso continua a ser o que uma porta comum abre, com a porta de volta e mais nada.

Lá as salas vêm de esquemas `.schem` guardados no jar. O porte ainda não os lê, e estas são feitas em código;
quando o leitor chegar, troca-se o que enche a sala e não o resto.

### Três coisas que custaram a achar, e que valem para o que vier

1. **Areia num bolso cai para o vazio.** O deserto tinha chão de areia, e um bolso não tem nada por baixo dele: à
   primeira sacudidela o chão esvaziava-se e via-se o céu do vazio por baixo. O chão passou a arenito, e as dunas
   de areia por cima têm-no a segurá-las.
2. **Um bolso cavado num pedaço de mundo que ninguém segura pode ir-se embora.** Nos retratos, cavar a sala e só
   depois levar lá quem joga dava sala nenhuma e queda no vazio. Leva-se primeiro, cava-se depois.
3. **Da consola, `gamemode creative` sem `@p` não faz nada** — queixa-se de que falta um jogador, e o resto do
   teste corre com quem joga a pé, a cair e a morrer. Nos retratos de bolso é melhor `gamemode spectator @p`, que
   além disso não cai enquanto a sala não nasce.

**Guardas:** o `WildPocketGameTest` cobre as seis — a porta não pega na fenda solta e não se gasta, pega na presa
e fica brava, a porta comum não fica brava, o bolso liso tem uma porta só, o bravo tem três com duas por apontar
e um tema, e dois temas seguidos nunca são o mesmo. O `WildPocketClientTest` tira um retrato de cada sala.

## Os que já andavam nas fendas (2026-09-26)

A quarta coisa que quem manda pediu, e a que amarra as outras três: *a gente pode amarrar isso com os endermans,
assim isso explicaria como eles vagam entre as dimensões e como eles teleportam*. E de facto explica — o
Thaumcraft e as Portas Dimensionais já contavam a mesma história por dois lados, e faltava alguém a atravessar de
um para o outro.

O `RiftWalkers` diz três coisas sem uma linha de texto:

* uma fenda **já crescida** põe cá fora um enderman de vez em quando — ele não apareceu, chegou;
* um enderman morto **ao pé de uma fenda**, ou dentro de um Reino Fragmentado, deixa **Fio do Mundo**: é o que
  traz agarrado de tanto andar por onde o Véu está roto;
* e nas salas para lá de uma fenda presa há sempre um ou outro, porque é ali que eles moram.

E, no livro, a página que o thaumaturgo escreveu ao fim de onze dias de olho num rasgão: *não se teleportam;
passam, e saem noutro sítio onde o Véu esteja fino*.

**Guardas:** o `RiftWalkersGameTest` cobre quem conta como andarilho do Véu (ao pé de uma fenda sim, longe dela
não), que uma fenda pequena não põe ninguém cá fora por mais voltas que se dê, e que uma crescida põe — e pára
quando a vizinhança enche. O fio que eles deixam ao morrer depende de terem morrido às mãos de alguém, e isso não
dá para forjar num gametest sem armar uma morte inteira; fica por cobrir, e está dito.

### E a fenda que não se pode apontar

Depois de a porta passar a depender da fenda presa, veio à vista um buraco que até aí não fazia diferença: **uma
fenda solta não tem corpo**, e o raio do rato passa através dela. Carregar-lhe em cima acertava no bloco por trás,
e o Firma-Fendas nunca via fenda nenhuma — a fatia inteira não tinha como começar.

A Lâmina de Fenda já resolvia isto à mão, percorrendo a linha de visão de um quarto de bloco em quarto de bloco
(`RiftBladeItem.riftAimedAt`). O Firma-Fendas e o Fecha-Fendas passam a fazer o mesmo, e ganham um `use` para se
poderem apontar ao ar, sem bloco por trás. Guarda: `theHandToolsFindTheBodilessRift`.

Lá isto não aparecia porque a fenda era só de enfeite; aqui é dela que sai a porta.

## A fenda como quem manda a quis: uma gavinha (2026-09-26)

O talho chato não era o que ele tinha pedido. A foto que mostrou é outra coisa: **uma gavinha comprida de três
dimensões**, redonda, gorda em baixo e a afinar até a ponta se perder, com um S ao meio, preta de céu sem lua e
com estrelas presas na pele.

O `RiftTendril` monta-a assim: uma espinha que sobe virando devagar e a meio caminho dobra o virar para o outro
lado — é isso que lhe dá o S em vez de um gancho —, anéis de oito lados à volta dela, e a grossura a cair de uma
raiz no pé até zero na ponta. Cada fenda tem a sua, do número que sorteou ao nascer.

**Duas coisas que o desenho pedia e não havia:**

* **Luz.** O desenho é de cor só, sem folha e sem normais, e por isso um tubo lia-se como uma fita chata. A
  gavinha traz agora um número de luz por canto, feito quando ela se monta: quem olha para a luz fica claro,
  quem lhe dá as costas fica escuro. É o que lhe dá o redondo.
* **Um esvoaçar que respeite o pé.** O original esvoaça seis décimos por igual em todo o rabisco; numa gavinha
  isso não serve, porque o pé dela está preso ao mundo. Aqui o esvoaçar cresce com o quadrado da altura do canto:
  o pé fica quieto e a ponta ondula.

O `RiftTear` chato ficou no histórico, ao lado do `RiftCurves` que já lá estava.

## As salas do original, todas as cento e dezesseis (2026-09-26)

As salas com tema feitas em código eram um remendo enquanto não havia leitor de esquemas. Quem manda disse o que
elas eram: *o Dimensional Doors original era um mod de dungeon puzzle, onde cada sala aleatória era grande e tinha
espaço e uma construção única — veja nos arquivos originais e copie de lá as salas*. Tem razão, e agora estão cá.

### O caminho

Os esquemas são Sponge v1 da 1.12: `Width`, `Height`, `Length`, uma paleta de nome para número, e o corpo num
vetor de varints. Os nomes são os de antes da planificação da 1.13 —
`minecraft:stonebrick[variant=cracked_stonebrick]`, `minecraft:stone_slab[half=top]`,
`minecraft:stone_stairs` (que era a escada de pedregulho, e não de pedra).

Contam-se **cento e cinquenta e seis esquemas, noventa e seis blocos distintos e trezentos e vinte e oito
estados** — pouco o bastante para a planificação se fazer à mão e por inteiro, sem adivinhar nada. É o
`scratchpad/dd-mapa.js`, e o `dd-salas.js` passa-lhe cada esquema e escreve um arquivo por sala.

O formato de saída é o mais simples que serve: cabeçalho, paleta de estados em texto — que o `BlockStateParser` lê
—, e o corpo em pares de *quantas casas seguidas, qual entrada da paleta*. Estas salas são quase todas ar, e ar
seguido comprime-se a nada: as **oito milhões de casas das cento e dezasseis salas cabem em 311 KiB**.

As portas do mod viraram as nossas na tradução, e os tecidos também. Quer dizer que **as salas já vêm com as
saídas desenhadas nas paredes**: a primeira passa a ser a de volta, as outras ficam por apontar, e quem as
atravessar abre outra sala. O quebra-cabeças liga-se sozinho.

### Três coisas que isto obrigou a mexer

* Os bolsos afastavam-se **sessenta e quatro** uns dos outros; as salas vão até noventa e sete de lado. Passam a
  duzentos e cinquenta e seis — num mundo que é só vazio, o espaço não custa nada.
* Pôr uma sala é pôr até seiscentas mil casas enquanto alguém atravessa uma porta. Num bolso acabado de abrir o
  mundo já é vazio, e mais de dois terços do que uma sala tem é ar: **o ar salta-se**, e sobra um terço do
  trabalho.
* As salas do original têm portas de ferro e de quartzo, e não só de madeira. O teste que as contava só sabia da
  de madeira e dizia que não havia porta nenhuma.

O `PocketThemes` — a biblioteca, o deserto, o Nether e o reino antigo feitos à mão — saiu, e fica no histórico.
Serviu para saber o que se queria; as de verdade servem melhor.

## A porta do Thaumcraft, rachada (2026-09-26)

Quem manda: *ao invés de várias portas no padrão do Minecraft, deveríamos usar a porta do Thaumcraft mas com umas
rachaduras nela que dê pra ver o portalzinho lá*.

As quatro portas dimensionais passam a ser a **porta arcana** do Thaumcraft com uma racha a atravessá-la de alto
a baixo. A racha é um buraco de verdade — alfa zero —, e porque as portas se desenham de recorte, o que se vê por
ela é o vão que já estava desenhado por trás da folha. Não foi preciso mexer no vão; ele sempre esteve ali, era a
folha que o tapava toda.

A racha corre pelas duas metades sem dar um salto no meio, e por isso monta-se numa folha de dezasseis por trinta
e dois e corta-se depois (`scratchpad/Rachar.java`). **Desvio declarado:** as quatro continuam a distinguir-se,
mas só pelo metal do aro — ferro, ouro, quartzo e o escuro da arcana —, porque as salas do original usam as
quatro e seria pena ficarem todas iguais.

## As portas que já estavam lá (2026-09-26)

E: *precisa colocar também umas portas antigas pelo mundo que só dê pra ver de óculos*, com uma foto — uma
ombreira de pedra de pé num descampado, vazia; e, com os óculos, uma porta dentro dela.

A **Porta Antiga** é a única do ramo que não se desenha como bloco. O `AncientDoorBlock` diz que não tem desenho
nenhum, e quem a põe de pé é o desenhista do vão, que já sabia quem está a olhar: uma caixa de três dedos com a
folha rachada nas duas caras, de recorte, e o vão a brilhar pela racha. A ombreira nasce no mundo de cima, uma em
cada quatrocentos e vinte pedaços, com o lajedo já comido pelo tempo.

**Duas coisas que uma porta invisível obrigou a pensar:**

* **Não pode ter corpo.** Uma parede invisível no meio de uma ombreira é uma armadilha, e não um segredo: quem
  não tem os óculos atravessa a ombreira e não dá por nada.
* **Nem contorno.** A caixa de ver ainda aparecia quando o rato lhe passava por cima, e a porta denunciava-se. A
  forma de um bloco não costuma saber quem a pediu, mas a conta traz quem pediu: sem os óculos, ela devolve
  forma nenhuma. É a única parte disto que olha para quem está do outro lado do ecrã.

**Guarda:** `theAncientDoorIsOnlyThereForWhoSeesIt` — sem desenho, sem corpo, e fechada a quem não a vê.

## Três focos, e menos três ferros no cinto (2026-09-26)

Quem manda: *acho que estabilizar a fenda, fechar a fenda, abrir fenda deveriam ser focus de varinha e não
itens*. Tem razão, e a razão é do próprio Thaumcraft: tudo o mais que um thaumaturgo faz ao mundo faz-se com a
varinha, o vis está na varinha, e um rasgão no Véu não é trabalho de ferro como um nó também não é.

Saíram o **Firma-Fendas** e o **Fecha-Fendas** de mão. Entraram três focos: **Rasgar**, que abre uma fenda onde a
varinha aponta; **Firmar**, que prende a apontada; e **Remendar**, que a fecha — e que numa porta faz a porta
esquecer para onde levava. Entram pelo `Focuses.register`, que é a porta que o mod já tinha para os focos de um
ramo de fora.

A **Assinatura de Fenda** fica. Ela não abre nem fecha nada: o que ela faz é *ligar dois lugares*, e isso é outro
ofício.

**Uma coisa que custou uma volta de suíte:** uma varinha só guarda **vis primordial**. O Vazio, que seria o
aspecto certo para tudo isto, é composto — uma varinha nunca tem nenhum, e os três focos não disparavam. Os custos
passam a ser em primordiais; o Vazio fica para a essência da infusão que faz cada foco, onde ele cabe.

**Guardas:** `theOpenFocusTearsARift`, `theHoldFocusHoldsARift` (e a segunda vez não faz nada) e
`theCloseFocusClosesARift`, os três com varinha de verdade e o foco preso nela.

## Cinco pedidos de quem manda (2026-09-26)

### Português do Brasil

O primeiro, e o que vale para tudo o que vier: *eu quero as coisas em português do Brasil, não nesse português de
Portugal*. O texto de jogo do ramo foi reescrito — as páginas do livro, os nomes, as mensagens —, e o resto do
`pt_br.json` foi varrido nas construções que denunciavam o europeu: o gerúndio com "estar a", o "há de", e o
pronome grudado depois do verbo onde o Brasil põe antes ("o sangue guarda-se" virou "o sangue se guarda").

**Fica dito:** os comentários do código e este documento ainda estão em português europeu de ponta a ponta, de
muitas sessões atrás. Do que é novo em diante vai tudo em brasileiro; varrer o que já existe é um serviço à parte,
e fica para quando ele pedir.

### As rachas da porta, uma por uma

*As rachaduras ficam de um tamanho e forma fixas, eu queria algo mais aleatório e como um fio caído no chão.*

São oito agora, e o jogo sorteia uma por posição — o arquivo de estados lista oito desenhos para cada variante, e
o resto é do próprio Minecraft. Cada racha é um risco que atravessa a folha de um lado ao outro por uma curva de
quatro pontos, com o meio dela saindo do caminho reto para cada lado: dá um fio contínuo que ondula e nunca se
enrola nem faz bico.

**Uma coisa que a solução obrigou:** o jogo sorteia a metade de cima e a de baixo **em separado**, porque são dois
blocos. Então a racha de cada metade tem de caber inteira dentro dela e nunca encostar na beirada — se ela
atravessasse a folha toda, metade das portas sairia com a racha partida no meio.

A Porta Antiga não tem desenho de bloco nenhum, e por isso o sorteio dela é feito à mão, pela posição, dentro do
desenhista do vão. São as mesmas oito rachas.

### A porta do mundo leva às salas

*A porta que nasce no mundo leva pra uma sala quadrada pequena, não pras salas de desafios do puzzle.* Era um
defeito: faltava marcar a fenda dela como brava quando a ombreira nasce. Uma porta que ficou de pé num descampado
desde antes de haver quem a visse não dá para um quarto de tecido.

### Uma porta só

*Não precisa ter portas de ferro, ouro, quartzo, nem o alçapão, só a porta de madeira.* Saíram as três
dimensionais, o alçapão e as duas portas comuns de enfeite. As salas do original que traziam porta de ferro ou de
quartzo passam a trazer a de madeira — quem resolve isso é a tradução dos esquemas, não o jogo.

### E as assinaturas também não

*Os itens Assinatura da Fenda e Assinatura da Fenda Estabilizada não precisam existir, porque temos os focos de
varinha.* Saíram, e com eles a pesquisa que as ensinava; os três focos ficam pendurados direto no Fio do Mundo.

## Mais quatro de quem manda (2026-09-26)

### Nascer do lado certo da porta

O defeito que travava o jogo: quem atravessava uma porta do mundo nascia **atrás** da porta da sala e, ao dar o
primeiro passo, atravessava ela de novo e voltava para o mundo de cima. O lugar de chegada vinha do lado para
onde a porta olha, e as salas do original foram desenhadas à mão — tem porta virada para cada lado.

Agora se procura. Primeiro os dois lados da porta, ganhando o que estiver mais para dentro da caixa da sala; e,
se nenhum servir — tem porta do original encostada em escada, em degrau, em poço —, se varre a vizinhança até
achar chão. **Um teste antigo não bastava:** o primeiro que eu escrevi só olhava se a casa era ar, e reprovava
degraus e lajes onde dá para ficar de pé muito bem. O que vale é se a caixa de um jogador cabe ali.

A volta também estava errada: quem voltava nascia **dentro** da porta do mundo, porque o destino era a casa da
fenda, que é a própria porta.

**Guarda:** `theArrivalIsInsideTheRoomAndStandable`, em seis salas seguidas.

### A fenda se ramifica

*Elas vão crescendo mas só vai aumentando de tamanho; eu queria que fossem se ramificando tipo uma raiz de
planta.* A gavinha deixou de ser uma só e passou a ser uma lista de **braços**: o primeiro nasce com a fenda, e
cada um dos outros brota de um ponto de um braço mais velho e só aparece depois que ela passa de um tamanho. Quem
desenha escolhe quantos mostrar — é isso que faz a fenda parecer que está se abrindo, e não inchando.

**Duas coisas que custaram uma volta cada:**

* **O tamanho de cada galho tem de sair do braço de onde ele brotou**, e não do último que se montou. Com uma
  variável só, os galhos iam encolhendo uns em cima dos outros e do quinto em diante nem se viam.
* **O lado para onde o galho sai tem de ser de través ao pai.** Com um lado qualquer sorteado, sobrava nele um
  tanto da direção do pai e o galho corria grudado no corpo dele: a fenda virava um borrão em vez de uma raiz.

### O fio que ela solta

*Conforme ela fosse aumentando de tamanho, fosse dropando, não todas as vezes, um fio do mundo.* Uma fenda que
cresce solta um Fio do Mundo uma vez em mil e duzentas. A presa não solta nada: quem a firmou ganhou uma porta e
perdeu a colheita. **Guarda:** `theGrowingRiftShedsThread`.

### Os raios dos focos

*O mesmo do foco de choque, mas em raios da cor da fenda para abrir e fechar, e branco para firmar, que remete ao
Ordo.* É o mesmo `FXLightningBolt` da 4.2.3.5, com o mesmo jeito de sair da mão e o mesmo esfarelar de fagulhas no
alvo. Os sete tipos de raio do original ficaram como estavam — quem precisa de outra cor passa a dela pelo
`setColours`, e a tabela fica intacta.

## A porta que abre, e o ramo na aba dos Ancestrais (2026-09-26)

### A folha tem de girar

*Portas do mundo: quando clica ela não abre visualmente, mas funciona.* O desenhista do vão guarda a caixa da
porta **fechada**, de propósito — é ali que o vão mora, e é o que faz ele ficar parado no buraco quando a porta
abre. Só que a folha da Porta Antiga estava usando essa mesma caixa, e a folha *é* a porta: ela tem de girar.

Agora são duas caixas no mesmo estado de desenho: a da porta fechada, para o vão, e a de agora, para a folha.

### Sem espada e sem armadura

Saíram a Lâmina de Fenda e as quatro peças de Fio do Mundo Tecido, com o material, as receitas, as folhas e a
etiqueta de conserto delas. O que este ramo faz é abrir caminho, e não brigar.

**Uma coisa ficou:** a conta que acha a fenda para onde alguém está apontando morava dentro da lâmina, e é dela
que os três focos dependem — uma fenda solta não tem corpo, o raio do mouse passa direto por ela, e sem percorrer
a linha de visão não há como pegar nenhuma. Virou o `RiftAim`.

### E o ramo foi para a aba dos Ancestrais

*Acho que podemos passar o progresso dele ali na aba do ancestral, porque combina e diminui as abas.* Combina
mesmo: o Véu roto, o que anda do outro lado dele e o que os Eldritch fizeram ao mundo são a mesma história
contada de lados diferentes.

A aba própria saiu, e as sete pesquisas foram para o lado direito da dos Ancestrais, que estava livre — a do
original ocupa de −5 a 4 em x, e o ramo começa no 6. A primeira delas passa a pendurar direto no
{@code ELDRITCHMINOR}, e o atalho que existia só para isso saiu junto.

## A lâmina da foice (2026-09-26)

Quem manda desenhou de vermelho por cima do retrato o que faltava: uma lâmina de verdade no alto do cabo.

No original a lâmina da foice de osso são **três varetas de um por um por quinze**. De longe elas somem, e o que
se via era um cabo pelado com um toco na ponta. Agora a lâmina são sete chapas finas enfileiradas, cada uma
virada um tanto em relação à de trás: juntas fazem a curva, e a largura vai caindo da base até a ponta. Por fora
corre uma fita mais clara, que é o gume.

**Como a curva se faz sem conta nenhuma:** cada chapa é desenhada dentro do quadro da anterior, então basta andar
para a frente e virar um pouco a cada volta — a curva vai se somando sozinha, sem seno nem cosseno.

**Duas coisas que a folha e o ângulo obrigaram:**

* A folha do original só tem uma **barra fina** de lâmina, que dava para as três varetas e não dá para uma chapa.
  O resto dela é transparente, e é onde entraram as duas manchas novas — a chapa escura e a fita do gume — que o
  `scratchpad/Lamina.java` pinta. A folha de osso sai daí, passando a nova pelo `Osso.java` de sempre.
* A lâmina de uma foice fica **deitada**, de través ao cabo: o plano dela é o mesmo em que ela é balançada. Isso
  quer dizer que olhar a chapa de frente é olhar o cabo de topo, e num quadro de dezesseis por dezesseis não cabe
  mostrar os dois. O ícone do inventário virou de lado até a lâmina se ler como lâmina, e o cabo ficou de esguelha.

**Desvio declarado:** as três varetas do `ModelScytheBone` saíram, e ficam no histórico.

**E o lugar dela custou três voltas.** A lâmina saía por trás de quem segura a foice; quem manda marcou de
vermelho onde ela devia estar — para a frente, atravessando na altura do peito. Meia-volta em torno do cabo não
resolvia (ela só trocava de um lado ruim para o outro): o que resolveu foi um quarto de volta, que tira a lâmina
do eixo em que o cabo é segurado, mais uma caída para o lado do cabo. Os três números ficam juntos no começo da
classe, que é onde se mexe quando ele quiser outra pose.

**E a meia-lua virada para o lado errado, que custou mais quatro voltas.** A lâmina estava no lugar certo mas
com o gancho da ponta subindo, e não caindo. Duas coisas que eu confundi pelo caminho e que vale deixar escritas:
virar a lâmina **não** é trocar o sinal do quanto ela vira a cada pedaço — isso muda para que lado o arco todo
sai, e a lâmina vai parar no outro lado do cabo. E meia-volta em torno do próprio comprimento também não, porque
ela leva a chapa para o outro lado da espinha e a lâmina some atrás do cabo.

O que vira a meia-lua sem tirá-la do lugar é **desenhar a chapa do outro lado da espinha**: o arco continua o
mesmo, e a barriga da lâmina passa de cima dele para baixo. Uma linha.

**Como parei de adivinhar:** em vez de mais uma volta no escuro, os dois números viraram campos que se mexem em
tempo de jogo, e um teste de tela fotografou as quatro combinações de uma vez. Com as quatro lado a lado deu
para ver que nenhuma servia, e que o que faltava era outra coisa — o teste foi embora depois, que era ferramenta
de ajuste e não guarda.

**De passagem, dois testes que piscavam**, nenhum deles do que se mexeu. Os dois pelo mesmo motivo de fundo:
olhavam uma vez, num tique escolhido a dedo, uma coisa que o jogo não promete para quando.

* O `altarCallsTheGuardian` — o altar tenta chamar o guardião a cada quarenta tiques e cada tentativa pode dar
  em nada, porque o lugar sorteado pode não servir. Em quatrocentos tiques eram oito tentativas, e de vez em
  quando as oito falhavam. Passou para mil e duzentos.
* O `keyRoom` — quem nasce junto com a sala só entra na lista do mundo nos tiques seguintes, e não num número
  fixo deles. Passou a olhar a cada tique até aparecerem, em vez de olhar no oitavo.

## O Ars Occulta (2026-09-26)

O sexto ramo de fora: o **Witchery 0.24.1**, de Emoniph (803 classes), que na lore de quem joga se chama
**Ars Occulta** — o ofício das bruxas. O código mora em `net.thaumcraft.occulta`, as figuras e os textos no
espaço de nome `thaumcraft`, e as chaves de pesquisa vão levar o prefixo `AO_`.

É o maior dos ramos de fora, e por isso vai por fatias: as plantas, o caldeirão, o altar, os rituais, os
espíritos e as artes de coven. Esta é a primeira.

### Fatia 1 — as oito plantas do ofício

O `BlockWitchCrop` do original, que cresce como o trigo mas com cinco coisas que são dele. Cada uma virou um
campo do `WitchCropBlock.Traits`, e é o que separa as oito:

| planta | idades | onde | farinha de osso | o que tem de seu |
| --- | --- | --- | --- | --- |
| beladona | 4 | terra | duas ou mais | dá a Flor de Beladona |
| mandrágora | 4 | terra | duas ou mais | escapa de quem a arranca de dia |
| alcachofra-d'água | 4 | **água** | duas ou mais | planta-se sobre a água parada |
| campainha-de-neve | 4 | terra | duas ou mais | dá bola de neve e, uma vez em cinco, Agulha de Gelo |
| losna | 4 | terra | duas ou mais | feita, **sobe outra em cima dela** |
| mandrágora-de-mina | 4 | terra | **uma só** | cresce uma vez e meia mais devagar |
| acônito | **7** | terra | **uma só** | as sete idades |
| alho | **5** | terra | duas ou mais | é semente de si mesmo |

**O chão delas é mais largo que o do trigo:** grama, terra, terra arada, a própria planta e a losna — esta última
porque é sobre losna que a losna sobe. A alcachofra é a única que troca tudo isso por água, e a semente dela é um
`PlaceOnWaterBlockItem`, que é o que o jogo de hoje tem no lugar do `waterPlant` do `ItemWitchSeeds`.

**Nenhuma recusa farinha de osso.** O `canFertilize` do original não fecha a porta: ele diz de quanto o pulo é —
de duas idades até o fim nas que aceitam, e de uma só na mandrágora-de-mina e na acônito. Foi o que ficou.

**O que cai não dava tabela de saque** e está no `OccultaCrops`: planta verde larga uma semente; planta feita faz
**três tentativas de semente** (mais uma por nível de Fortuna), cada uma com oito chances em quinze, e larga a
colheita. Fora dessa conta ficam duas: a mandrágora-de-mina, que dá um bulbo sempre e o segundo uma vez em
quatro, e a mandrágora — de dia ela escapa nove vezes em dez, de noite uma em dez. É o que obriga quem joga a
colhê-la à noite, e é o número do original ao contrário, porque lá a conta diz quando ela *não* escapa.

Uma tabela de dados não sabe contar três tentativas de oito em quinze, e muito menos olhar a hora do dia; por
isso a conta é em código, como no original.

**Na mandrágora-de-mina e no alho a semente e a colheita são o mesmo item.** No original o item de colheita delas
é nulo, e o mod copia o de semente — daí o bulbo ser o que se planta e o que se colhe, e o alho também.

**Uma coisa que é do porte:** os aspectos. O Witchery não era addon de Thaumcraft e não anotava aspecto em nada, e
como nada do ramo sai de receita, nada seria deduzido — as plantas ficariam sem leitura no thaumômetro. O
`OccultaAspects` anota as quatorze coisas no tom do `ConfigAspects` do original: a semente é *herba*, a colheita é
*messis*, e cada uma leva o que a lore do Witchery lhe dá — *venenum* na beladona, *gelum* na campainha-de-neve,
*cognitio* na mandrágora-de-mina.

**Do original fica de fora, por agora,** a mandrágora que anda e grita: é criatura, e vem na fatia dos bichos do
ramo. Enquanto ela não chega, a que escapa apenas não deixa nada no chão.

**Cinco delas usam o modelo de plantação do jogo e três o de flor**, que é o que o `getRenderType` do original diz
(seis para a maioria, um para a campainha-de-neve, a acônito e a losna). As sessenta e uma folhas vieram do jar
pelo `scratchpad/wi-plantas.js`, que também escreve os modelos e os arquivos de estado.

**Uma coisa que o jogo de hoje obrigou:** as três idades — quatro, cinco e sete — são três propriedades diferentes
e têm todas o mesmo nome, `age`. Não dá para declarar as três num bloco só, e o construtor do `CropBlock` pergunta
pela propriedade antes de o campo do filho estar escrito. A idade de cada planta espera num balcão
(`ThreadLocal`) enquanto o bloco nasce, e sai de lá assim que o construtor acaba.
