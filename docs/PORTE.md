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
  mil e quinhentos. Aponta-se para um nó e se segura o botão para beber dele, um ponto de cada vez. As
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
quatorze gomos, os números 10 e 11 do `BlockAiry`, e o fim das salas de chefe do `TileEldritchLock`.

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
- **A Lâmina Primordial**: fere como nada mais, faz definhar e enfraquecer, conserta-se sozinha e, com o clique
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
Thaumcraft. O modelo daqui era um caixote de doze por quatorze, sem as coordenadas de figura e sem vidro. Agora ele
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

### O que já está ali

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

O mundo dos bolsos e o Limbo se abrem com o jogo rodando, pelo `DynamicDimensions` — o jogo de hoje não deixa
registar mundos em código como a 1.7.10 deixava, então eles nascem na primeira vez que alguém precisa deles.

### Fatia das ferramentas — a lâmina, o ferro e a armadura

- A **Lâmina de Fenda** (`ItemRiftBlade`): corta como espada de ferro e, com o botão de usar, salta. Havendo uma
  fenda na linha de visão, ela a atravessa; havendo um bicho, leva quem a empunha para junto dele. Quanto mais
  gasta a lâmina, mais longe e mais torto o salto sai — é a única coisa que o gasto dela muda. Vinte tiques de
  espera entre saltos, e se conserta com Tecido Estável.
- O **Firma-Fendas** (`ItemRiftStabilizer`): usado numa fenda solta, prende-a — ela deixa de comer o mundo em
  volta. Seis usos, e numa porta não faz nada.
- A **armadura de Fio do Mundo Tecido**: as quatro peças do `ItemWovenWorldThreadArmor`, com os números do
  original — vinte de durabilidade, 5/4/3/2 de proteção e vinte de encantabilidade —, que se consertam com Fio do
  Mundo. Tecem-se do fio, e também se forram por cima de uma peça de couro, como lá.

**Uma diferença declarada:** o original tem uma opção de configuração que troca o Tecido Estável por pérola do fim
em todas as receitas. Aqui vale sempre o Tecido Estável, que é o que o mod faz de fábrica.

**Ainda fica de fora** do ramo: o rosto da fenda solta (no original ela é um rasgão preto que treme no ar, feito
de um sistema-L), o tamanho das fendas e o registro delas, os alçapões dimensionais, a Ferramenta de Ajuste de
Fenda, as portas de ouro e de quartzo comuns e o disco de música.

### Fatia do rosto — o rasgão da fenda solta

A fenda solta era invisível: estava lá, comia o mundo em volta e levava quem a atravessasse, mas não se via. Agora
tem o rosto do original — um **rabisco de dragão pintado de preto**, pendurado no ar, que treme.

O desenho vem de um **sistema-L**: quatro rabiscos (o terdragão, o dragão, o dragão duplo e o vórtice) em várias
gerações, quatorze ao todo, e cada fenda escolhe o seu quando nasce, junto com o lado a que fica virada. Três
coisas mexem nele ao mesmo tempo, e são as três do original: o **tremor**, que abana o rasgão inteiro e cresce com
o cubo do tamanho dele; o **esvoaçar**, que mexe cada canto por conta própria com dez ondas a correr; e o
**giro**. O tempo de cada fenda é o dela — a conta leva um número tirado do lugar onde ela está, de modo que duas
fendas lado a lado não tremam juntas.

A fenda também **cresce sozinha**, como no original: dez vezes por tique ela soma `1/(tamanho+1)` ao tamanho, e o
rasgão cresce com ele. Presa pelo Firma-Fendas, para.

**Três diferenças declaradas**, todas de dentro:

- O original monta os sistemas-L ao arrancar e recorta o contorno de cada rabisco com uma biblioteca de Delaunay.
  Aqui as quatorze formas vêm prontas num arquivo de dados, feito por `scratchpad/dd-curvas.js` com o mesmo
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
  por ele aberto sai num Reino Fragmentado; atrás de quem passa ele se fecha, a não ser que haja redstone a
  segurá-lo. O original só tem o de madeira, e aqui é o mesmo.
- A **Porta de Ouro** e a **Porta de Quartzo**: as duas portas comuns do ramo, que não levam fenda nenhuma. São
  matéria-prima — a porta dimensional de ouro se faz de uma delas com Tecido Estável, e a de quartzo da outra.

Com elas o ramo fecha a lista de coisas que se constroem. **Ficam ainda de fora**: o tamanho e o registro das
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
por baixo do jogo. Quando a leitura dos registros calha no meio da reescrita, o bioma não está lá e fica por
ligar.

Prova: com a suíte rodando, forçaram-se oito reescritas seguidas do `magical_forest.json`, e o erro apareceu,
nomeando esse mesmo bioma e o vizinho dele na pasta. Sem nada a compilar em paralelo, três voltas seguidas
passaram limpas.

**A regra que fica:** não compilar enquanto a suíte de tela roda. Se o erro voltar sem nada em paralelo, então aí
sim é do mod, e o que se procura é quem refere esses dois biomas antes de os dados carregarem.

## As duas foices, por fim (2026-09-26)

Depois de umas quantas voltas, o que quem joga queria era simples: **as duas foices no feitio das sete caixas do
`ModelScytheBone`**, e a folha a separá-las — a de sangue com a do original, de cabo de madeira, e a de osso com
a mesma folha passada a osso, de cabo e tudo.

Saíram, então, as sete caixas do `ModelScythe` (a foice de sangue de origem) e o modelo de Blender do
`scythe.obj`, que no original só aparecia a quem estivesse numa lista de nomes que o mod ia buscar à rede. Ficam
no histórico.

**De passagem, uma armadilha do original que vale guardar:** o `ModelScytheSpecial` **não segue o `.mtl`**. O
arquivo de materiais manda o cabo usar a `cloth.jpg`, e o desenhista o liga à `guntex.jpg` antes de o desenhar. É
a folha do desenhista que vale — quem for ler um `.obj` de mod da 1.7.10 que olhe primeiro para quem o desenha.

## Os Óculos do Véu (2026-09-26)

Ideia de quem joga, e **do porte, não do original**: nas Portas Dimensionais toda a fenda se vê desde o primeiro
dia, e então não há nada para descobrir. Aqui as fendas continuam no mundo desde sempre — o `RiftFeature`
já as espalhava —, mas **só aparecem a quem aprendeu a vê-las**, que é o que o Thaumcraft faz com tudo o mais.

O que quem manda decidiu, quando lhe perguntei:

* **Óculos novos do ramo**, e não uma melhoria dos que já havia: os **Óculos do Véu**, feitos dos Óculos da
  Descoberta com Fio do Mundo em volta, com pesquisa própria na aba dos Reinos Fragmentados.
* **Só as fendas que já estavam no mundo** pedem os óculos. A porta que o thaumaturgo assentou e a fenda que ele
  rasgou com a Assinatura ficam à vista de qualquer um — quem rasgou sabe onde rasgou.

### Como se sabe de quem é cada fenda

O `RiftBlockEntity` ganhou um `natural`, que **vem ligado**. Quem põe fendas sem passar por mãos de ninguém é a
geração do mundo, que chama `setBlock` e nada mais; quem as faz de propósito passa por um destes dois lugares, e
os dois desligam-no:

* `DimensionalDoorBlock.setPlacedBy` e `DimensionalTrapdoorBlock.setPlacedBy` — quem assenta passa por aqui, a
  geração do mundo não. É essa a linha que separa as duas.
* `RiftSignatureItem.rift` — a fenda que a Assinatura rasga é de quem a rasgou.

### Quem enxerga

O `VeilSight` é o irmão do `Revealing`: aquele diz quem vê os nós de aura, este quem vê o que está por trás do
mundo. Tem a etiqueta `thaumcraft:sees_the_veil`, por onde um mod de fora mete o elmo dele, e os Óculos do Véu
entram também na `thaumcraft:revealing` — são os da Descoberta melhorados, e não perdem nada do que eles faziam.

**Uma manha que vale guardar:** o `animateTick` do `FloatingRiftBlock` também tinha de saber quem está olhando, e
é código comum — no servidor dedicado a classe `Minecraft` não existe. Em vez de lhe tocar, o `VeilSight` tem um
`localPlayer` que quem corre do lado de quem joga preenche no arranque, que é o mesmo jeito do `clientTrail` dos
orbes de foco. Sem isso, as fagulhas denunciavam a fenda a quem não a devia ver.

### A folha

A dos Óculos da Descoberta, com as lentes de ametista trocadas pelo vazio: uma rampa de preto-azulado com umas
poucas fagulhas brancas dentro. Quem é lente se reconhece pela cor e não pelo lugar — um pixel em que o azul manda
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

O `RiftTear` o monta do número que a fenda sorteou quando nasceu: dele saem o torcer da espinha, o inchar da
barriga, as gavinhas e o lugar de cada fagulha. Duas fendas do mesmo número são iguais; de números diferentes,
não. O que mexe continua sendo do original — o tremor, o esvoaçar das dez ondas e o giro —, e as fagulhas vão
levadas pelo mesmo tremor, senão se descolavam do talho quando a fenda abana.

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
porta lhe tomar o lugar, porque assim que o bloco troca o miolo dela se vai e leva o destino consigo. São três
casos:

* numa fenda **solta**, a porta não pega — o aviso aparece e a porta fica na mão;
* numa fenda **presa** pelo Firma-Fendas, a porta lhe toma o lugar e tudo o que ela sabia, e fica **brava**;
* **longe de qualquer fenda**, a porta se assenta como sempre e abre o bolso liso do original.

### As salas

Um bolso bravo sai com um dos quatro temas do `PocketThemes` e com **três portas**: a de volta, no meio da parede
do norte, e mais duas nas paredes de lado, que ainda não apontam para lado nenhum. Quem atravessar uma delas abre
outro bolso bravo, de outro tema — o sorteio nunca repete o tema de onde se veio —, e é assim que as salas se vão
ligando. O bolso liso continua sendo o que uma porta comum abre, com a porta de volta e mais nada.

Lá as salas vêm de esquemas `.schem` guardados no jar. O porte ainda não os lê, e estas são feitas em código;
quando o leitor chegar, troca-se o que enche a sala e não o resto.

### Três coisas que custaram a achar, e que valem para o que vier

1. **Areia num bolso cai para o vazio.** O deserto tinha chão de areia, e um bolso não tem nada por baixo dele: à
   primeira sacudidela o chão se esvaziava e se via o céu do vazio por baixo. O chão passou a arenito, e as dunas
   de areia por cima têm-no a segurá-las.
2. **Um bolso cavado num pedaço de mundo que ninguém segura pode ir embora.** Nos retratos, cavar a sala e só
   depois levar lá quem joga dava sala nenhuma e queda no vazio. Leva-se primeiro, cava-se depois.
3. **Da consola, `gamemode creative` sem `@p` não faz nada** — queixa-se de que falta um jogador, e o resto do
   teste corre com quem joga a pé, a cair e a morrer. Nos retratos de bolso é melhor `gamemode spectator @p`, que
   além disso não cai enquanto a sala não nasce.

**Guardas:** o `WildPocketGameTest` cobre as seis — a porta não pega na fenda solta e não se gasta, pega na presa
e fica brava, a porta comum não fica brava, o bolso liso tem uma porta só, o bravo tem três com duas por apontar
e um tema, e dois temas seguidos nunca são o mesmo. O `WildPocketClientTest` tira um retrato de cada sala.

## Os que já andavam nas fendas (2026-09-26)

A quarta coisa que quem manda pediu, e a que amarra as outras três: *a gente pode amarrar isso com os endermans,
assim isso explicaria como eles vagam entre as dimensões e como eles teleportam*. E de fato explica — o
Thaumcraft e as Portas Dimensionais já contavam a mesma história por dois lados, e faltava alguém a atravessar de
um para o outro.

O `RiftWalkers` diz três coisas sem uma linha de texto:

* uma fenda **já crescida** põe cá fora um enderman de vez em quando — ele não apareceu, chegou;
* um enderman morto **ao pé de uma fenda**, ou dentro de um Reino Fragmentado, deixa **Fio do Mundo**: é o que
  traz agarrado de tanto andar por onde o Véu está roto;
* e nas salas para lá de uma fenda presa há sempre um ou outro, porque é ali que eles moram.

E, no livro, a página que o thaumaturgo escreveu ao fim de onze dias de olho num rasgão: *não se teleportam;
passam, e saem noutro lugar onde o Véu esteja fino*.

**Guardas:** o `RiftWalkersGameTest` cobre quem conta como andarilho do Véu (ao pé de uma fenda sim, longe dela
não), que uma fenda pequena não põe ninguém cá fora por mais voltas que se dê, e que uma crescida põe — e pára
quando a vizinhança enche. O fio que eles deixam ao morrer depende de terem morrido às mãos de alguém, e isso não
dá para forjar num gametest sem armar uma morte inteira; fica por cobrir, e está dito.

### E a fenda que não se pode apontar

Depois de a porta passar a depender da fenda presa, veio à vista um buraco que até aí não fazia diferença: **uma
fenda solta não tem corpo**, e o raio do mouse passa através dela. Clicar nela acertava no bloco por trás,
e o Firma-Fendas nunca via fenda nenhuma — a fatia inteira não tinha como começar.

A Lâmina de Fenda já resolvia isto à mão, percorrendo a linha de visão de um quarto de bloco em quarto de bloco
(`RiftBladeItem.riftAimedAt`). O Firma-Fendas e o Fecha-Fendas passam a fazer o mesmo, e ganham um `use` para se
poderem apontar ao ar, sem bloco por trás. Guarda: `theHandToolsFindTheBodilessRift`.

Lá isto não aparecia porque a fenda era só de enfeite; aqui é dela que sai a porta.

## A fenda como quem manda a quis: uma gavinha (2026-09-26)

O talho chato não era o que ele tinha pedido. A foto que mostrou é outra coisa: **uma gavinha comprida de três
dimensões**, redonda, gorda em baixo e a afinar até a ponta se perder, com um S ao meio, preta de céu sem lua e
com estrelas presas na pele.

O `RiftTendril` a monta assim: uma espinha que sobe virando devagar e a meio caminho dobra o virar para o outro
lado — é isso que lhe dá o S em vez de um gancho —, anéis de oito lados à volta dela, e a grossura a cair de uma
raiz no pé até zero na ponta. Cada fenda tem a sua, do número que sorteou ao nascer.

**Duas coisas que o desenho pedia e não havia:**

* **Luz.** O desenho é de cor só, sem folha e sem normais, e por isso um tubo se lia como uma fita chata. A
  gavinha traz agora um número de luz por canto, feito quando ela se monta: quem olha para a luz fica claro,
  quem lhe dá as costas fica escuro. É o que lhe dá o redondo.
* **Um esvoaçar que respeite o pé.** O original esvoaça seis décimos por igual em todo o rabisco; numa gavinha
  isso não serve, porque o pé dela está preso ao mundo. Aqui o esvoaçar cresce com o quadrado da altura do canto:
  o pé fica quieto e a ponta ondula.

O `RiftTear` chato ficou no histórico, ao lado do `RiftCurves` que já estava ali.

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
`scratchpad/dd-mapa.js`, e o `dd-salas.js` lhe passa cada esquema e escreve um arquivo por sala.

O formato de saída é o mais simples que serve: cabeçalho, paleta de estados em texto — que o `BlockStateParser` lê
—, e o corpo em pares de *quantas casas seguidas, qual entrada da paleta*. Estas salas são quase todas ar, e ar
seguido se comprime a nada: as **oito milhões de posições das 116 salas cabem em 311 KiB**.

As portas do mod viraram as nossas na tradução, e os tecidos também. Quer dizer que **as salas já vêm com as
saídas desenhadas nas paredes**: a primeira passa a ser a de volta, as outras ficam por apontar, e quem as
atravessar abre outra sala. O quebra-cabeças se liga sozinho.

### Três coisas que isto obrigou a mexer

* Os bolsos se afastavam **sessenta e quatro** uns dos outros; as salas vão até noventa e sete de lado. Passam a
  duzentos e cinquenta e seis — num mundo que é só vazio, o espaço não custa nada.
* Pôr uma sala é pôr até seiscentas mil casas enquanto alguém atravessa uma porta. Num bolso acabado de abrir o
  mundo já é vazio, e mais de dois terços do que uma sala tem é ar: **o ar se salta**, e sobra um terço do
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

A racha corre pelas duas metades sem dar um salto no meio, e por isso se monta numa folha de dezesseis por trinta
e dois e se corta depois (`scratchpad/Rachar.java`). **Desvio declarado:** as quatro continuam se distinguindo,
mas só pelo metal do aro — ferro, ouro, quartzo e o escuro da arcana —, porque as salas do original usam as
quatro e seria pena ficarem todas iguais.

## As portas que já estavam lá (2026-09-26)

E: *precisa colocar também umas portas antigas pelo mundo que só dê pra ver de óculos*, com uma foto — uma
ombreira de pedra de pé num descampado, vazia; e, com os óculos, uma porta dentro dela.

A **Porta Antiga** é a única do ramo que não se desenha como bloco. O `AncientDoorBlock` diz que não tem desenho
nenhum, e quem a põe de pé é o desenhista do vão, que já sabia quem está olhando: uma caixa de três dedos com a
folha rachada nas duas caras, de recorte, e o vão a brilhar pela racha. A ombreira nasce no mundo de cima, uma em
cada quatrocentos e vinte pedaços, com o lajedo já comido pelo tempo.

**Duas coisas que uma porta invisível obrigou a pensar:**

* **Não pode ter corpo.** Uma parede invisível no meio de uma ombreira é uma armadilha, e não um segredo: quem
  não tem os óculos atravessa a ombreira e não dá por nada.
* **Nem contorno.** A caixa de ver ainda aparecia quando o rato lhe passava por cima, e a porta se denunciava. A
  forma de um bloco não costuma saber quem a pediu, mas a conta traz quem pediu: sem os óculos, ela devolve
  forma nenhuma. É a única parte disto que olha para quem está do outro lado da tela.

**Guarda:** `theAncientDoorIsOnlyThereForWhoSeesIt` — sem desenho, sem corpo, e fechada a quem não a vê.

## Três focos, e menos três ferros no cinto (2026-09-26)

Quem manda: *acho que estabilizar a fenda, fechar a fenda, abrir fenda deveriam ser focus de varinha e não
itens*. Tem razão, e a razão é do próprio Thaumcraft: tudo o mais que um thaumaturgo faz ao mundo se faz com a
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
pronome grudado depois do verbo onde o Brasil põe antes ("o sangue se guarda" virou "o sangue se guarda").

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

**As primeiras sementes vêm do mato.** É o `MinecraftForge.addGrassSeed` do `Witchery.load`: seis das oito entram
na lista de que o mato tira a semente que larga, com peso — cinco para a mandrágora, quatro para a beladona, três
para a alcachofra, dois para a campainha-de-neve e um para a acônito e para o alho. Sem isso não há por onde
começar o ramo, porque nenhuma delas nasce no mundo nem sai de receita.

*Desvio declarado:* no 1.7.10 a lista era uma só, e o trigo do jogo disputava o mesmo sorteio (dez de vinte e
seis). Aqui não se mexe na tabela do trigo: o ramo põe um sorteio à parte, com o mesmo um oitavo, os mesmos pesos
e uma entrada vazia de peso dez no lugar do trigo. As seis saem com a chance exata do original; o trigo segue
como o jogo de hoje quer.

**E os aspectos são do próprio Witchery.** O mod trazia um `ModHookThaumcraft4` de mil e seiscentas linhas que
anotava cada coisa dele nos aspectos do Thaumcraft 4 — era assim que os dois se davam em 2014. É de lá que saem
os números do `OccultaAspects`, item por item e planta por planta (a flor de beladona é *venenum* 4 e *mortuus* 4;
a raiz de mandrágora é *herba* 2, *humanus* 1 e *terra* 1; o bulbo da mindrake é *aqua* 1 e *permutatio* 1, que é
o que o original diz mesmo sendo estranho).

*Duas coisas são do porte, e ficam declaradas:* a acônito e o alho, que aquele arquivo não anotava — nem semente,
nem colheita, nem a planta. Vão no tom do resto: veneno e fera na acônito, que é o que ela faz aos lobisomens;
vida e morto-vivo no alho, que é o que ele faz aos vampiros.

**Do original fica de fora, por agora,** a mandrágora que anda e grita: é criatura, e vem na fatia dos bichos do
ramo. Enquanto ela não chega, a que escapa apenas não deixa nada no chão.

**Cinco delas usam o modelo de plantação do jogo e três o de flor**, que é o que o `getRenderType` do original diz
(seis para a maioria, um para a campainha-de-neve, a acônito e a losna). As sessenta e uma folhas vieram do jar
pelo `scratchpad/wi-plantas.js`, que também escreve os modelos e os arquivos de estado.

**Uma coisa que o jogo de hoje obrigou:** as três idades — quatro, cinco e sete — são três propriedades diferentes
e têm todas o mesmo nome, `age`. Não dá para declarar as três num bloco só, e o construtor do `CropBlock` pergunta
pela propriedade antes de o campo do filho estar escrito. A idade de cada planta espera num balcão
(`ThreadLocal`) enquanto o bloco nasce, e sai de lá assim que o construtor acaba.

### Fatia 2 — o Forno das Bruxas

O `BlockWitchesOven` e o `BlockFumeFunnel` do original, que é por onde o ramo começa de verdade: é no forno que se
ganham os **sete fumos**, e sem eles não há Pedra Sintonizada, nem altar, nem quase nada do que vem depois.

**O forno cozinha pouco de propósito.** Ele segue as receitas de fornalha do jogo, mas o `canSmelt` só deixa
passar o que vira **carvão, comida ou cinza de madeira** — não é fundição. E qualquer muda vira Cinza de Madeira,
que é o que o `AddSmeltingForAllSaplingsToWoodAsh` do original faz por padrão; aqui isso é uma receita só, pela
marca `minecraft:saplings`.

**Cada coisa cozida deixa um cheiro**, e com um Pote de Barro na casa dos potes o cheiro fica guardado: três
décimos de chance, mais o que os funis dos lados acrescentam. A muda de carvalho dá a Exalação do Cornífero, a de
pinheiro o Indício de Renascimento, a de bétula o Sopro da Deusa; a de selva não dá nada, e o original não diz por
quê. Tudo o mais dá Fumo Fétido. Os outros três — Lufada de Magia, Fedor de Má Sorte e Odor de Pureza — vêm das
três árvores do ofício, que ainda não chegaram; os itens já estão aqui à espera delas.

**Os funis fazem duas coisas, e não a mesma.** Apressam o cozimento em vinte tiques cada um, dos cento e oitenta —
e aí vale também o que está em cima do forno. Mas a **sorte** do cheiro só melhora com os dois dos lados: um
quarto cada, ou três décimos se tiver filtro. Um funil virado para outro lado não serve para nada, como no
original, onde a marca dele tinha de ser igual à do forno.

**Dois blocos viraram um.** No original há um forno aceso e outro apagado, que é como o jogo de 2014 fazia; aqui é
um só, com a marca `lit`.

**O feitio dos dois é de modelo de Techne**, desenhado por desenhista de bloco, como a Máquina de Costura do Ars
Mortuorum — e o do funil muda com o que ele tem em volta: com forno embaixo vira cano com chapéu, sem forno é o
corpo largo, e com forno ao lado sai de lá a canalização daquele lado.

**Uma pedra no caminho, que já é conhecida da casa.** As duas peças do cano chamam `setTextureSize(64, 128)`
**depois** do `addBox`, onde aquilo já não vale — o mesmo caso do espelho do Techne. Lido ao pé da letra, o cano
ia buscar um pedaço vazio da folha e sumia; a folha é de 64 por 64 como o resto do modelo.

### Fatia 3 — as três árvores do ofício

A sorveira, o amieiro e o espinheiro-alvar: o `BlockWitchLog`, o `BlockWitchLeaves` e o `BlockWitchSapling` do
original, com as duas árvores que os geradores dele fazem. Cada uma dá seis blocos — tora, folhagem, muda,
tábuas, escada e laje —, que no original eram um bloco só com três marcas.

**A sorveira é o carvalho pequeno do jogo antigo**, com a copa um bloco mais larga (`spread = 1`) e de cinco a
sete de altura. O **amieiro** e o **espinheiro-alvar** são o carvalho grande, o mesmo desenho de galhos que a
grande-madeira do Thaumcraft usa, com os números que o `setScale` de cada um manda: o amieiro estreito e ralo, de
galhos mais caídos; o espinheiro largo e cheio.

**O que cai da folhagem** é o do original: a muda uma vez em vinte, e a da sorveira larga ainda **Bagas de
Sorveira** uma vez em duzentas — as duas contas melhoram com Fortuna. Com tesoura ou Toque Suave sai a própria
folhagem, que é o que o jogo de hoje faz com qualquer folha.

**A muda não cresce na primeira batida do acaso:** a primeira marca, a segunda faz a árvore. É o
`markOrGrowMarked` do jogo antigo, e aqui a marca é o `stage`, como nas mudas de hoje.

**E o forno fecha a conta:** as três mudas do ofício dão os três fumos que faltavam — a sorveira a Lufada de
Magia, o amieiro o Fedor de Má Sorte, o espinheiro-alvar o Odor de Pureza.

**Do original fica de fora, por agora,** o Ent que às vezes sai de uma tora quebrada (uma chance em cem, mais uma
por tora encostada, até cinco) — é criatura, e vem na fatia dos bichos. E a Porta de Sorveira, que é porta.

**Uma coisa a lembrar de quem joga:** as mudas do ofício não nascem no mundo nem saem de receita nenhuma. No
original vêm de **mutar uma muda comum com Mutandis**, que é feito no caldeirão — e o caldeirão ainda não chegou.
Até lá elas só existem no criativo. É assim no original também: sem caldeirão, não há árvore do ofício.

### Fatia 4 — o Caldeirão da Bruxa

O `BlockCauldron` e o `TileEntityCauldron` do original, que é onde o ofício começa a valer: enche-se de água,
acende-se fogo embaixo, espera-se ferver — cinco segundos — e daí o que se joga dentro entra na panela.

**Ele não se fabrica.** se Faz untando um caldeirão comum com **Pasta de Unção**, que sai das quatro sementes que
o mato dá (alcachofra, mandrágora, beladona e campainha-de-neve). É o `useAnnointingPaste` do original, e a água
que o caldeirão comum já tinha passa para ele.

**A ordem importa, e é a do original:** primeiro entra o que a receita pede, e por último a coisa que
<b>dispara</b>. Quando ela cai, o caldeirão olha o que tem dentro; batendo, mexe três segundos e larga o que a
receita faz, e esvazia. O que não serve a receita nenhuma nem entra.

Do que o original faz assim, entram aqui as que só pedem coisas que já existem: o **Mutandis** (raiz de mandrágora
e exalação do Cornífero, com um ovo por último; saem seis), o **Mutandis Extremis** (Mutandis com uma verruga do
Nether) e a carne, que o caldeirão cozinha sem mais nada — porco, frango, boi e carneiro.

**E o Mutandis fecha o ramo em si mesmo:** passado numa planta, ele a troca por outra da lista — e é só por aí que
se chega às três mudas do ofício, porque elas não nascem no mundo nem saem de receita. O Extremis alcança também
as plantações, troca grama por micélio e faz barro da terra que está debaixo de água.

**Do original ficam de fora, por agora,** as coisas que o ritual dele consulta e que ainda não existem aqui: o
poder do altar, os círculos de giz, o coven de bruxas em volta e a má sorte que cai sobre quem erra. O que sobra é
a espera e o resultado. Ficam de fora também as **poções** — o caldeirão do Witchery é antes de tudo uma fábrica
de poções, e isso é um sistema inteiro, que vem em fatia própria.

**Uma coisa do porte:** a cor da água. O original guarda uma cor por ingrediente numa tabela que só existe junto
das poções; enquanto elas não chegam, a cor de cada coisa sai do nome dela, e a mistura é meio a meio, como no
`augmentColor` dele.

### Fatia 5 — o Altar

O `BlockAltar` do original: **seis pedras, duas por três**, encostadas de lado. Uma pedra sozinha não é nada; o
bando é que faz o altar, e a primeira pedra dele passa a ser a que manda. A conta que decide isso é a do
original, e é curiosa: cada pedra tem de ter **dois ou três** vizinhos de altar, e o bando todo tem de dar
**exatamente seis** — duas por três é a única forma que fecha as duas coisas ao mesmo tempo.

**O poder vem da natureza em volta.** O altar olha um cubo de vinte e nove de lado e conta o que lá há: cada
coisa vale um tanto e só conta até um tanto. A folhagem vale três e conta até cem; a grama, dois até oitenta; a
flor, quatro mas só até trinta; o ovo de dragão vale duzentos e cinquenta e conta uma vez. Somado, dá o teto. O
que está guardado sobe dez por segundo até esse teto.

Os números são os do original, um por um; o que lá era um bloco por linha passa aqui à marca que reúne os do
mesmo tipo — as mudas, as toras, a folhagem, as flores —, que é como o jogo de hoje agrupa.

**Os enfeites em cima somam:** a caveira de esqueleto soma um ao teto e à velocidade, a do wither dois, a de
gente três; e a tocha soma um à velocidade. Ficam de fora, declarados, o candelabro, o cálice, a Arthana, o Ramo
Místico, o pentáculo e o Ovo do Infinito — que ainda não foram portados.

**Uma armadilha do jogo de hoje, que custou uma volta.** Ao marcar quem manda, o miolo troca a cara da pedra — e
uma troca de estado também passa pelo `onPlace`. A conta do bando recomeçava do meio dela mesma, e cada pedra
acabava a achar que quem manda é outra: o altar ficava de pé, mas sem juntar poder nenhum. Agora o `onPlace` só
conta o bando quando a pedra é **nova**, e a cara só se troca quando muda mesmo.

**E um desvio declarado:** a tela. O original abre uma janela que diz quanto poder o altar tem; aqui o clique
escreve a mesma coisa na conversa, que é o que dá para fazer sem uma tela de bloco inteira só para três números.

### Fatia 6 — a aba no Thaumonomicon

O Witchery não tem pesquisa nenhuma: o que lá se aprende está num livro escrito à parte, fora do sistema do
Thaumcraft. A árvore desta aba é a que a **lore de quem joga** marca — *O Caminho Antigo*, *Bruxaria*,
*Resonantia Naturae* e *O Altar da Bruxa* —, e o que cada pesquisa ensina é o que o original faz.

Das quatro linhas que a lore abre depois do Altar, estão aqui as duas que já têm coisa dentro: as **Plantas de
Ritual** e os **Cozimentos e Infusões**. As outras duas — a Magia Simpática e as Artes do Espírito — esperam as
bonecas e os sonhos.

**Uma escolha declarada:** na lore as quatro linhas saem todas do Altar. Aqui as duas que já existem saem de onde
a mão alcança, que é **antes** dele: sem as plantas não há Pasta de Unção, e sem o caldeirão não há Mutandis nem
as madeiras do ofício. A ordem do que se aprende segue a ordem do que se faz.

**O fundo da aba** é a mesma nebulosa das outras, girada para o verde (`scratchpad/Tingir.java`), que é como as
abas dos ramos se parecem umas com as outras sem serem iguais.

**E uma pedra no caminho:** a quebra de linha das páginas do livro é a da 4.2.3.5 — `<BR>` —, e não o `[nl]` que
eu tinha escrito; até o acerto, as páginas mostravam a marca no meio do texto.

### O caldeirão passa a ser o crisol (2026-09-27)

*Por que o Caldeirão da Bruxa é um modelo novo, se já tudo é Thaumcraft? Usa o Crisol mesmo, e a unção só lhe
pendura umas plantas nas laterais.*

Foi o que se fez. O caldeirão deixa de ser o modelo de Techne do Witchery e passa a ser **o crisol do mod** — o
mesmo modelo, as mesmas folhas — com quatro molhos de ervas encostados por fora, um em cada parede. A Pasta de
Unção não troca a panela: enfeita-a.

Com isso saiu um desenhista de bloco inteiro e o desenhista de item do caldeirão: agora o bloco é modelo comum, e
o que o desenhista de tile põe é só o **líquido**, do mesmo jeito que o crisol põe o dele.

**As ervas** são um retalho montado pelo `scratchpad/Ervas.java` com as plantas do próprio ramo: a losna de um
lado, a beladona do outro e o alho pendurado no meio, num barbante.

**E a cor do que ferve** deixou de sair do nome da coisa: sai do **aspecto maior** dela, o que o thaumômetro lê.
A água começa azul como a do jogo e vai ficando da cor do que se joga dentro — verde com erva, e assim por
diante. É mais coisa de Thaumcraft do que de Witchery, e fica declarado.

**Uma hora perdida com o que não era defeito:** o líquido parecia não aparecer. Aparecia — mas um caldeirão de
paredes altas só mostra o que tem dentro quando se olha de cima, e as fotos estavam todas de esguelha. O que as
voltas de diagnóstico deixaram de bom foi o caminho honesto no teste de tela: a água entra por balde e clique,
como quem joga faz, em vez de ser escrita à força no bloco.

### O Magia Naturalis se muda para a aba do ofício (2026-09-27)

*Une o Naturalis na aba da bruxaria, porque elas se conversam: a natureza, a bruxa, o que é antigo, o natural.*

A aba própria do Magia Naturalis saiu do livro. As dezoito pesquisas dele e as seis sombras das do Thaumcraft
passaram para a aba do **Ars Occulta**, à direita da árvore do ofício — doze colunas adiante, que é onde nenhuma
cai em cima de outra (há um teste que confere isso, pesquisa por pesquisa).

A entrada do ramo, o `MN_INTRO`, pendura-se agora no `AO_OLD_WAYS`: é o fio que liga as duas árvores e mostra de
onde uma olha para a outra. Ele continua se abrindo sozinho, como antes — quem já jogava não perde nada.

O `Naturalis.CATEGORY` deixou de ser um nome seu e passou a apontar para o do ofício, que é o que faz todo o
resto do ramo (receitas, sombras, testes) continuar a funcionar sem mexer em mais nada.

**A aba do criativo do Magia Naturalis continua onde estava** — o que se juntou foi o livro, que é onde as duas
histórias se encontram.

### Os bichos do ofício (2026-09-27)

Três criaturas, e nenhuma delas é caça: são o que as plantas e as árvores do ramo fazem quando alguém mexe com
elas sem cuidado.

**A Mandrágora** (`MandrakeEntity`, o `EntityMandrake`) é a raiz que se arrancou fora de hora. Quem colhe a
planta feita de dia não leva raiz nenhuma: leva a mandrágora de pé, gritando atrás dele. O grito **cega** quem
apanha o golpe — quinze segundos, do segundo grau — e a única defesa são os **Abafadores** na cabeça. Ela não dá
experiência nenhuma, como no original: não é caça, é castigo. A fuga já estava contada nas plantas; o que se
juntou aqui foi o bicho que nasce dela, no lugar em que ela estava.

**A Mandrágora-de-Mina** (`MinedrakeEntity`, o `EntityMindrake`) nasce do **bulbo largado no chão**: três
segundos e ele vira bicho — um por cada bulbo do monte —, e se quem o largou foi alguém, ela nasce dona dessa
pessoa. Ela não morde: **estoura**. Ao alcançar quem persegue, explode e morre, e do chão queimado nasce uma
papoula ou um dente-de-leão. Morta de outro jeito, estoura na mesma, um pouco menos. O que faz o bulbo acabar é
um mixin no `ItemEntity`, que é onde o original punha o `onItemExpireEvent`.

**O Ent** (`EntEntity`, o `EntityEnt`) sai de uma **tora do ofício quebrada**: uma em cem, mais uma por tora
encostada nela, até cinco em cem — um bosque cerrado se defende melhor que uma árvore sozinha. Duzentos de vida,
quatro de dano, e nada o empurra. Onde ele pisa a terra melhora: de trezentas em trezentas batidas, o chão
debaixo dele recebe farinha de osso. Ele não nasce em cima de quem corta — procura um lugar num raio de dezesseis
blocos com três de céu livre, como no original.

**Os Abafadores não são armadura de folha.** Isto custou uma foto vermelha: a folha do original é de 64 por 64 e
tem tinta só em dois cantos, porque o que a lê é o `ModelEarmuffs` — cinco caixas presas à cabeça, as duas
conchas nas orelhas e o arco de três pedaços por cima. Posta como folha de armadura do jogo de hoje, ela pintava
o **corpo inteiro**. Agora vão pelo caminho da armadura de fortaleza: modelo próprio (`EarmuffsRenderer`), corpo
todo escondido, e a folha em `textures/models`. As caixas são as do original, número por número.

**Um defeito que apanhou dois itens.** Os Abafadores e a Cabeça de Isaac tinham durabilidade **zero**, na ideia de
que zero era o mesmo que "não se gasta". Não é: o jogo de hoje olha para a peça que *tem* durabilidade, seja ela
qual for, e a primeira pancada que o dono leva gasta um ponto dela — com conta zero, a peça se desfaz no primeiro
golpe. Na prática, quem levava um golpe da mandrágora perdia os abafadores nesse mesmo golpe e o grito seguinte
já o alcançava. Os dois passaram a ter a durabilidade do original, que é tanta que nunca se gastam. Foi um teste
que o apanhou.

**O balanço.** Os três desenhistas do original mexem o corpo ao andar — seis graus e meio para um lado e para o
outro, no compasso do passo. É o andar de uma planta que não tem pernas, e está portado.

**De fora, declarado:** o dono que o Ent pode ter, no original, vem da poção de escravizar — coisa do caldeirão,
que ainda não chegou. E os **aspectos** dos três são do porte: o original não os anotava, e sem eles o
thaumômetro não teria o que ler numa criatura do mod.

### Semente não se planta em cima de planta (2026-09-27)

*Dá para plantar duas sementes uma sobre a outra — corrige isso.*

**Desvio declarado, pedido.** O `canPlaceBlockOn` do `BlockWitchCrop` aceitava como chão, além de grama, terra e
terra arada, **a própria planta** e a **losna** — e é por isso que dava para semear em cima do que já estava
plantado, e ficava uma planta do ofício boiando um bloco acima da horta.

Apertou-se dos dois lados:

- **o chão**: planta nenhuma serve de chão a outra. A única exceção é a **losna debaixo de losna**, porque é
  disso que ela precisa para se sustentar quando sobe sozinha;
- **a semente**: o `WitchSeedItem` recusa o clique quando o que está debaixo do lugar é planta do ofício — até a
  losna sobre losna, que só se empilha por conta própria, nunca pela mão de quem semeia.

A losna continua se empilhando exatamente como antes: isso é do `randomTick`, não de quem planta.

Há duas provas novas: uma percorre as oito plantas contra as oito e confere que só a losna sobre losna se
sustenta; a outra dá a semente a um jogador de mentira e clica — recusa sobre a planta, planta em terra arada.
A segunda metade existe para a primeira não passar à toa.

### O caldeirão coze: o motor dos cozimentos (2026-09-27)

O coração do Witchery. A tabela do original tem **três mil e quatrocentas linhas** e usa quase tudo o que o mod
tem; esta é a primeira fatia dela — o motor inteiro, e a parte da tabela que o mod de hoje já alcança.

**Como um cozimento é.** Não é um item com receita: é a **lista do que caiu no caldeirão, pela ordem**. Dela sai
tudo o resto — o nome, a cor, o poder que o altar tem de pagar, o que faz em quem bebe e quanto se leva a beber.
Por isso o `Brew` não guarda estado nenhum além da lista: recontar é mais barato que guardar duas verdades sobre
o mesmo frasco.

**O espaço.** Água fervendo não recebe efeito nenhum. Quem abre espaço são os ingredientes de porte — a raiz de
mandrágora (um), a verruga do Nether (dois), o diamante (dois) e a Estrela do Nether (quatro) —, e cada um só
abre enquanto o que já se abriu for menor que o **teto** dele. É por isso que duas verrugas não valem quatro: a
segunda vê que já se passou do teto dela e não faz nada. Cada efeito **gasta** desse espaço conforme o peso, e o
que não couber não entra.

**Os temperos** valem para o efeito **seguinte**, uma vez só, e se apagam depois dele. Força, tempo, inversão,
sem fagulhas, sem alvo de bloco, sem alvo de criatura, sem teto de força. A força e o tempo param de subir aos
sete — a não ser com a Estrela do Nether, que levanta o teto.

**A cor** não se escolhe: é a conta encadeada do original (`37 * cor + chave`), que dá a cada receita a sua e faz
receitas parecidas saírem parecidas. Só a lã tinta manda nela à força. No frasco ela vai também no componente de
tinta do jogo, e é de lá que o desenho do item a tira — o mesmo caminho de uma armadura de couro tinta, e poupa
um desenhista só para isto.

**O poder** é a soma do que cada ingrediente custa, e o altar tem de o **ter** enquanto ferve e **pagá-lo** na
hora de engarrafar. Uma garrafa de vidro na mão e um clique: o caldeirão tem de estar fervendo e cheio.

**Sai um frasco por caldeirão.** No original sai mais para quem tem prática de engarrafar, chapéu de bruxa,
túnica e familiar — nada disso existe aqui ainda, e quem engarrafa neste porte é sempre alguém que está a
aprender. Fica declarado.

**O que esta fatia traz da tabela:** os quatro ingredientes de porte que o mod tem, os onze temperos, as treze
poções que são poções do próprio jogo e as dezesseis lãs.

**E o que fica para as fatias seguintes, declarado:** os efeitos que são poções próprias do Witchery (o nadar, o
não sentir dor, a acônito, a máscara de gás, a queda de pena — cinquenta e tantas); o **espalhamento**, que é o
frasco que se atira, o gás, o líquido e o gatilho; os efeitos que mexem no mundo; e os rituais de círculo de giz.
A Lágrima da Deusa, o Vapor de Diamante e o Pentáculo de Koboldite abrem espaço no original e ainda não existem.

**Uma armadilha que custou meia hora**, e que vale escrever: dentro de um `succeedWhen` de teste, o `helper.fail`
é **engolido** como "ainda não" — o teste tenta outra vez na batida seguinte. Como o corpo já tinha esvaziado o
caldeirão, ele nunca mais fervia, e o que se via no fim era "o caldeirão não ferveu" em vez do defeito de
verdade. A prova passou a esperar uma vez só (`runAfterDelay`), e o defeito apareceu na primeira tentativa.

**E um achado do próprio motor:** a prova antiga do caldeirão dizia que "um diamante não serve a receita nenhuma
e não entra". Desde os cozimentos, entra — ele é ingrediente de porte. Quem não serve a nada é o pedregulho, e é
esse que a prova usa agora.

### As receitas de cozimento entram no livro (2026-09-27)

*Depois de "Os Cozimentos" podia ter as ramificações com as receitas das poções.*

Foi o que se fez. De "Os Cozimentos" saem agora quatro ramos — **do Corpo**, **dos Sentidos**, **que Guardam** e
**que Ferem** —, e cada um traz quatro receitas.

**Uma página nova no livro.** Nenhum dos tipos de página do Thaumonomicon servia: um cozimento não é bancada, nem
crisol, nem infusão — é uma **ordem**. A página de cozimento mostra o caldeirão em cima, o que cai dentro numa
coluna numerada de um a quatro, o frasco que sai e o que o altar paga.

**É acréscimo do porte, declarado.** O Witchery não tem livro de pesquisa nenhum: o que se sabe sobre cozimentos
lá está num livro escrito à mão, fora do jogo. Aqui as receitas entram no Thaumonomicon como as outras, que é o
que faz o ramo se parecer com o resto do mod.

**O frasco e o poder não se escrevem à mão:** saem do próprio motor, do mesmo jeito que sairiam no caldeirão. E
há uma prova que percorre <b>todas</b> as receitas do livro, ingrediente por ingrediente, e confere que o
caldeirão aceita cada uma, que o poder escrito bate com o do motor e que nenhuma delas é uma receita que não faz
nada. O livro não pode ensinar o que a panela recusa.

### O frasco que se atira (2026-09-27)

Uma pitada de pólvora muda o que um cozimento é: ele deixa de se beber e passa a se **atirar**. É o
`BrewActionDispersal` com o `DispersalInstant` do Witchery, e é o primeiro dos quatro jeitos de espalhar.

**Onde o frasco bate, arrebenta.** O que estiver a três blocos (mais o alcance) apanha o cozimento, e apanha
menos quanto mais longe estiver — a conta do original é `1 − distância / raio`. Em quem levou o frasco em cheio,
vale inteiro. Atirado, o cozimento dura **metade** do que duraria na boca de quem o bebesse.

**Dois jeitos de espalhar não convivem** na mesma panela: o que cair depois desfaz o que estava lá. É o
`addNullifier` do original, e cada espalhamento apaga todos os outros — inclusive outro igual.

**O alcance** vem da cinza de madeira (um) e das sementes de cacau (mais um), cada uma com o seu teto. A
**duração** — a flor de beladona, o lápis-lazúli e a pedra do fim — entra na conta desde já, mas só terá o que
fazer quando o gás e o líquido chegarem; fica declarado.

**O nome mudou de feitio, e por causa do português.** No original o prefixo entra antes de "Brew of" e sai
"Splash Brew of Poison". Em português a mesma ordem daria "Arremessável Cozimento de Veneno", que ninguém diz.
Agora há duas chaves: com prefixo, o miolo é outro — em inglês continua "Brew of", e em português é vazio, porque
o próprio prefixo já diz "Cozimento Arremessável de". Sai "Cozimento Arremessável de Veneno".

**No livro** há uma pesquisa nova, *O Frasco que se Atira*, com quatro receitas — e elas passam pela mesma prova
que as outras: o caldeirão tem de aceitar cada uma.

**Do original fica de fora, declarado:** o modo de feitiço do `EntityBrew`, em que o frasco voa reto e sem peso
porque quem o atira é uma varinha do Witchery — as varinhas não estão portadas. E os outros três jeitos de
espalhar: o **gás** (lã de morcego), o **líquido** (losna) e o **gatilho** (cabeça de creeper), que pedem blocos
próprios e vêm na fatia seguinte.

### A névoa de cozimento (2026-09-27)

O segundo dos quatro jeitos de espalhar: com **Lã de Morcego** na panela, o frasco não estoura — ele **abre**.
É o `DispersalGas` com o `BlockBrewGas` e o `TileEntityBrewFluid` do Witchery.

**A nuvem cresce sozinha**, de cinco em cinco batidas, com a chance que o original dá a cada direção: pouca para
cima (duas em dez), mais para baixo (quatro) e bastante para os lados (oito). Cada passo conta um a mais no
estágio dela, e ela para no alcance que o cozimento lhe deu — quatro, mais o que a cinza de madeira e o cacau
alargarem, até dez.

**E depois morre.** Cheia, tem a cada batida a chance de um sobre a duração de sumir; e ao fim de **cento e
vinte** batidas some de qualquer jeito. É aí que a flor de beladona e o lápis-lazúli passam a ter o que fazer: a
duração é `5 + lifetime² × 5`, a conta do original.

**Quem passa dentro apanha fraco**: uma vez em dez, com um quarto da força e metade do tempo. Uma névoa não é um
frasco na cara.

**A Lã de Morcego** sai de um morcego morto por alguém, uma vez em três — e os aspectos dela são os do original
(corpus 1, volatus 1), que estavam escritos no `ModHookThaumcraft4`. <b>Do original fica de fora, declarado:</b>
a Arthana sobe essa chance para três em quatro, e a faca do ofício ainda não está portada.

**A folha da nuvem é a do original**, e a marca de animação dela teve de ser reescrita: na 1.7.10 o
`width: 1, height: 32` era a conta de <b>quadros</b>; hoje são <b>pixels</b>, e a folha é de 32 por 32 em trinta
e dois quadros. A ordem em que eles passam é a do original, de trás para a frente.

**A cor** sai do cozimento, como no caldeirão: é um pintor de bloco que lê a alma da nuvem, que é o
`colorMultiplier` do original.

### Os cozimentos que mexem no lugar (2026-09-27)

Até aqui um cozimento só fazia coisa a quem o bebia ou apanhava. Estes seis fazem coisa ao **chão** — e por isso
só valem atirados: bebidos, não têm onde pegar. São os `BrewAction*` da pasta `action/effect` do Witchery.

- **Derrubada** (um fio): todo tronco na bola cai, largando o que largaria a quem o cortasse.
- **Poda** (cogumelo marrom): folha e mato de roldão.
- **Pulverização** (pederneira): pedra → pedregulho → cascalho → areia, e a areia se solta do chão.
- **Vitória-régia** (uma vitória-régia): sobe até achar água com céu livre e põe folha nela.
- **Plantio** (sementes de trigo): não traz semente nenhuma — planta **o que já estiver largado** em volta, uma
  de cada vez, onde couber.
- **Praga** (batata venenosa, dois mil de poder): o mato some, a flor vira arbusto seco, a terra arada vira
  areia, e o chão apodrece — uma vez em cinco para areia, uma em cinco para terra. Em quem apanha, o aldeão vira
  zumbi e a vaca vira vaca-cogumelo; os outros bichos levam vinte.

**Os ingredientes saíram do original por dedução**, porque o decompilado só tem os nomes ofuscados: o
`field_151170_bI` da praga fica entre a batata cozida e o mapa na ordem de registro da 1.7.10, e os vizinhos dele
(cenoura dourada e crânio) já estavam confirmados por outras receitas. É batata venenosa.

**A geometria é a do original.** O círculo cheio que a praga e o plantio desenham é riscado pelo método de
Bresenham, como no `BlockActionCircle`, e não por conta de distância: a diferença aparece na borda, e um porte
que a mudasse desenharia outra coisa.

**E uma prova apanhou um defeito de verdade:** a nuvem de gás tinha caixa vazia, e por isso <b>ninguém contava
como estando dentro dela</b> — o gás nunca tocaria em quem passasse. A caixa passou a ser a do bloco inteiro,
como a do portal do Nether, que é o que faz o jogo saber que alguém está dentro. A colisão continua vazia: não se
esbarra numa névoa.

### As poções do ofício, primeira leva (2026-09-27)

O Witchery tem quase sessenta poções próprias, e é delas que depende metade da tabela de cozimentos. Esta é a
primeira leva: as sete que se bastam a si mesmas e não pedem nada que o porte ainda não tenha.

- **Nado** (bacalhau cru): dentro da água anda quinze por cento mais depressa, e mais três por grau.
- **Queda de Pena** (pena): passada a distância em que acorda, a queda para de acelerar e o tombo conta pouco.
- **Flutuação** (cana-de-açúcar): enquanto houver chão a três blocos debaixo, sobe; passando disso, fica no ar.
- **Máscara de Gás** (cascalho): não faz nada sozinha — existe para que uma névoa ruim não pegue em quem a tem.
- **Barriga Forte** (Exalação Fétida): do segundo grau para cima, tira a fome.
- **Alergia ao Sol** (salmão cru): a céu aberto, de dia, queima.
- **Alergia ao Escuro** (areia das almas): no escuro, dói — menos de dois de luz, mais dois por grau.

**Quatro delas o leite não tira**, como no original: a máscara, a barriga e as duas alergias. Quem faz isso é o
`Incurable`, que este mod já tinha para os efeitos da dobra — não foi preciso inventar nada.

**A máscara ficou ligada à névoa**: o tempero ganhou a marca de quem está protegido, e um efeito <b>ruim</b> não
pega em quem traz a máscara. É o `protectedFromNegativePotions` do original, que o `BlockBrewGas` já usava lá.

**Um desvio declarado, no Nado:** no original o empurrão é dado do lado de quem joga, olhando se a tecla de andar
está apertada. Aqui é do lado do servidor e vale para qualquer um que esteja nadando — é o único lugar de onde se
pode empurrar um bicho sem depender do teclado de ninguém.

**E duas provas apanharam coisa.** A da alergia ao escuro não doía porque a arena é clara — e a caixa de pedra
que eu pus em volta não fechava as <b>quinas</b>, por onde a luz entra de canto. Fechadas as quinas, e com o
bicho posto no meio depois de a caixa existir (posta com ele dentro, ela o empurra para fora), a prova anda.

A outra foi a que percorre as receitas do livro: eu tinha escrito a da alergia ao sol com verruga e diamante, que
abrem quatro de espaço — e ela pesa <b>seis</b>. O caldeirão recusaria, e o livro estaria ensinando o que a panela
não faz. A receita passou a levar uma Estrela do Nether, que abre os quatro que faltavam.

### As poções do ofício, segunda leva (2026-09-27)

Mais cinco, e estas todas dependem de alguém apanhar ou de alguma coisa voar:

- **Espinhos** (cacto): quem a tem fere quem se encostar nele, de cinco em cinco batidas.
- **Armas Envenenadas** (cogumelo vermelho): não faz nada em quem bebe — faz no que ele <b>acerta</b>. Do
  primeiro ao terceiro grau envenena; do quarto, apodrece.
- **Volatilidade** (mato alto ou arbusto seco): quem a tem estoura ao apanhar. Vindo de outro estouro é certo;
  das outras pancadas, uma em cinco. E de vez em quando ela própria se gasta nisso.
- **Reflexo de Projéteis** (teia): o que voa perto volta por onde veio.
- **Atração de Projéteis** (a mesma teia, invertida): o que voa a três blocos se vira para quem a tem.

**As duas que esperam uma pancada** vivem num gancho à parte, o `OccultaEvents`, no `AFTER_DAMAGE` do Fabric — é
o lugar de hoje para o que o original fazia no `IHandleLivingHurt`.

**Uma prova escorregadia, e o que ela ensinou:** a da alergia ao escuro voltou a falhar porque o buraco de pedra
de <b>um bloco</b> é estreito demais — o bicho tem 0,9 de largura e escorrega para fora dele à primeira sacudida.
Agora a prova constrói uma sala de três por três, com as quinas fechadas, e põe o bicho no meio dela. Rodou duas
vezes seguidas sem falhar.

### Erguer os Mortos (2026-09-27)

Um osso na panela, e o frasco que se atira levanta um morto onde bate: zumbi em seis de cada dez, esqueleto em
quase todas as outras, e um porco-zumbi raro. Com força, levantam-se mais — um a mais por grau, cada um com a sua
chance, nascidos de três blocos em volta, no primeiro chão que houver.

**Do original fica de fora, declarado:** o morto erguido em <b>ritual</b>, que dura pouco e obedece a quem o
ergueu — isso é a Manha Mortal e o escravizar, e nenhum dos dois está portado. Aqui ele se levanta e fica, como
qualquer morto da noite.

**E o Transpor fica para a fatia dos círculos:** ele é efeito de ritual, não de frasco — troca de lugar um pedaço
de mundo entre dois círculos de giz, e sem eles não há o que portar.

### A Destilaria (2026-09-27)

O que o forno junta num cheiro só, a Destilaria separa: **duas coisas entram, potes de barro se gastam, e saem
até quatro** — cada uma no seu pote. É o `BlockDistillery` do Witchery, com os sete números dele: sete casas,
oitocentos tiques por destilação e seis décimos de poder de altar por batida.

**Ela não tem fogo.** Quem a move é o altar, e sem altar por perto ela para onde estava — e diz isso na tela, com
o mesmo quadradinho de aviso do original.

**O que ela traz de novo:** a Cal Virgem, o Gesso, o Óleo de Vitríolo, o Orvalho do Ender, o Mal Refinado e,
sobretudo, a **Lágrima da Deusa** e o **Vapor de Diamante** — que abrem quatro e seis de espaço no caldeirão.
Eram esses dois que faltavam para os cozimentos graves, e estavam declarados como buraco desde a fatia do motor.
O buraco fechou.

**O modelo é o do original, peça por peça:** o alambique de três andares com o cano torto (que é a única peça
inclinada, dois vírgula três radianos) e a armação de quatro. E as **garrafas**: uma por pote de barro que
estiver na casa deles, até quatro — as únicas caixas do modelo que ligam o espelho antes de a caixa entrar, que é
quando ele vale.

**Uma coisa que a foto apanhou:** as garrafas não apareciam. O desenhista lê o número de potes da alma do bloco, e
<b>o que está dentro de uma alma não chega ao cliente sozinho</b>. A alma passou a mandar o bloco de novo quando
esse número muda — e só quando muda, para não falar à toa.

**Duas coisas do original ficam declaradas.** A receita de montagem pede uma <b>Pedra Sintonizada</b>, que este
porte ainda não tem; no lugar dela vai um diamante, e isso volta atrás quando a pedra chegar. *(Voltou: a Pedra
Sintonizada entrou na fatia das máquinas, de 2026-09-27, e a receita passou a pedi-la.)* E a tabela de receitas é
a parte que o mod alcança: faltam as que pedem o Coração de Demônio e o Cozimento de Espírito Fluente.

**E o livro ganhou uma página de destilação**, como a de cozimento — com o que entra, os potes que se gastam e o
que sai. O que ela mostra sai da <b>própria tabela</b>, e não da mão de quem escreve; e sai <b>na hora de
desenhar</b>, porque no momento em que o livro se monta os itens ainda não existem — isso custou um tombo de
arranque com um "Components not bound yet".

### Os círculos de giz e os primeiros ritos (2026-09-27)

O maior pedaço que faltava do Witchery. Um círculo é o contrário de um caldeirão: nada ferve, nada se mistura —
risca-se no chão, larga-se o que se oferece dentro, e bate-se no meio.

**Quatro gizes.** O de ritual sai da bancada (cinza de madeira, gesso e uma Lágrima da Deusa); os outros três
saem dele, no caldeirão a ferver — o dourado, o do alhures e o infernal. Cada risco gasta um ponto dos sessenta e
quatro, e riscar por cima de um glifo o troca pelo do giz que se tem na mão.

**Três anéis.** O desenho é o do original letra por letra, dezessete por dezessete: o de dentro com dezesseis
glifos, o do meio com vinte e oito, o de fora com quarenta. Cada anel pode ser de qualquer giz, e o que um rito
pede é <b>quantos glifos de qual giz em qual anel</b>. Não é círculo por conta de distância: é este risco.

**Cada giz tem doze desenhos**, sorteados a cada risco — trinta e seis figuras ao todo, as do original.

**O motor dos ritos** é a fila de passos do `RitualStep`: um passo por batida, e o que ele devolve diz o que
acontece — fica, passa ao seguinte, desiste, ou passa a <b>sustentar-se</b> (que é como os círculos de proteção
do original ficam de pé). O que se oferece some ao começar e <b>volta para o chão</b> se o rito desistir pedindo
devolução.

**Três ritos, os que o mod já alcança:** o <b>Cozimento</b> (vara de blaze, cinza e carvão, anel de fora
infernal), a <b>Fertilidade</b> (farinha de osso, Sopro de Renascimento, Vapor de Diamante, cal, gesso e
Mutandis, anel de dentro de ritual) e o <b>Eclipse</b> (espada de pedra e cal, só de dia). Os outros noventa e
três da tabela do original pedem coisa que ainda não existe aqui — a Pedra Sintonizada, a Sopa de Redstone, o
Dedo de Sapo —, e entram quando os itens chegarem.

**Dois desvios declarados.** ~~Os ritos <b>não se guardam em disco</b>: um rito morre ao desligar o mundo, onde
no original ele continuaria de onde estava.~~ *Resolvido: eles se guardam, e o que se guarda é o nome do rito e
quantos passos faltam.* E a Fertilidade não cura o aldeão zumbi como lá: no jogo de hoje isso é maçã dourada e fraqueza, que é
outra coisa, não o mesmo rito.

**E uma armadilha que já tinha aparecido**, agora com nome: o que se registra no arranque do mod <b>não pode
montar pilhas de item</b> — os componentes ainda não estão presos. A lista de ritos guarda os <b>itens</b> que
cada oferenda pede, e monta a pilha só quando o livro vai desenhar.

### As bonecas: a Magia Simpática (2026-09-27)

A primeira das duas linhas da lore que estavam vazias. Uma boneca de pano, musgo e fio; solta, não é nada. Presa
a alguém, passa a **responder por essa pessoa**: quando a morte vem por onde a boneca guarda, é a boneca que se
desfaz.

**O Frasco de Vínculo** é o que prende: um frasco de vidro com uma agulha de osso, que se enche <b>tocando</b>
quem se quer. Cheio, guarda o nome e a marca daquela pessoa — e na bancada, com uma boneca, faz dela a boneca
dela. É o `RecipeShapelessPoppet` do original, aqui uma receita própria que passa o vínculo de um para o outro.

**Nove bonecas.** Terra (queda), Água (afogamento), Fogo (fogo e estouro), Fome, Morte (todas), Ferramenta e
Armadura (que consertam o que está gasto a nove décimos), Contra o Vodu, e a de Vodu.

**Elas só precisam existir.** Valem na mochila de quem guardam ou numa <b>Prateleira de Bonecas</b>, em qualquer
canto do mundo — e é por isso que uma casa de bruxa tem uma parede delas. A prateleira entra numa lista ao nascer
e sai ao ser desfeita, para não haver de varrer o mundo à procura.

**Com elas vieram** a Agulha de Osso (que o Ars Mortuorum já tinha, e é a mesma — não se registra duas), o Musgo
Espanhol, o Musgo de Brasa (que queima quem lhe pisa) e a **Gota de Sorte**, que sai do caldeirão com Mutandis
Extremis por chave.

**A boneca de vodu** faz o que se lhe fizer: apontada para <b>lava</b>, a pessoa arde e a boneca se desfaz; <b>de
pé</b>, empurra-a para onde se olha, com a força do tempo que se segurou; <b>agachado</b>, com uma agulha de osso
na mochila, espeta — meio coração, e a agulha se gasta. Quem traz a <b>Contra o Vodu</b> não sente nada disso, e
uma vez em quatro cai um raio em cima de quem tentou.

**Dois desvios declarados.** O giz: no original uma receita dá <b>duas</b> varas de sessenta e quatro riscos que
se empilham — na 1.7.10 uma coisa gasta ainda empilhava, hoje não. A receita dá <b>uma</b> de cento e vinte e
oito, que é o mesmo giz na mesma conta. E a boneca da ferramenta: no original ela conserta no momento em que se
usa a ferramenta; aqui é de segundo em segundo, no mesmo lugar em que a armadura se olha. O que se vê é o mesmo.

### Os espelhos e o Mundo do Espelho (2026-09-27)

A outra metade da Magia Simpática, e o pedaço do Witchery que tem mundo próprio.

**Um espelho não sai de bancada.** Sai do **Rito de Infusão**: o anel do meio riscado a giz de ritual, uma
Lágrima da Deusa, uma barra de ouro e uma vidraça, e dois mil de poder. O rito é o `RiteSummonItem` do original,
que é o mais simples que há — o que se ofereceu some e no meio do círculo fica aquilo.

**São dois blocos**, pregados numa parede, e a **alma mora nos dois** — como no original. A de cima é que guarda
a ligação; a de baixo conta quem lhe fica diante e, sobretudo, **tem desenhista**: a figura é a mesma nas duas
metades, a de baixo de cabeça para baixo, e é assim que a moldura oval fecha. Sem alma na de baixo o oval ficava
pela metade, e foi a foto que apanhou isso.

**Quem lhe fica diante, olhando para ele, atravessa.** São os quatro caminhos do `onEntityWalking`, na ordem do
original: dois espelhos vazados **de costas** furam a parede entre eles (até trinta e duas casas); dentro do
Mundo do Espelho um espelho posto por quem joga passa **de cela em cela** (paga três mil de poder de altar);
dois vazados **em prumo** furam o chão (até dezesseis); e, não havendo nada disso, atravessa-se **de mundo**.

**O Mundo do Espelho é uma colmeia.** Cada pedaço de dezesseis é uma cela de nove de lado forrada de superfície
de espelho, e nenhuma se liga à outra. O desenho é o do `WorldChunkManagerMirror` casa por casa — as duas tabelas
dele dizem quais casas são parede. Cada espelho ganha **a sua** cela pela caracol do `getDimCoords`, e nela nasce
um espelho **selado** que aponta de volta. O mundo se abre com o jogo andando, pelo mesmo `DynamicDimensions` dos
bolsos das Portas Dimensionais.

**O Reflexo** guarda a cela: cem de vida, **nenhuma pancada lhe tira mais de seis**, e ele veste a armadura, a
melhor arma e os efeitos de quem entrou. Morto ele, o espelho de cá fica **vazado**, e passa a ser ponte.

**A cara do espelho** aparece ao clique, e responde quem é o mais belo — com o rumo de quem for, e a lista de
quem mais lhe ficou diante desde a última vez. Some em dez segundos.

**As duas cantigas** tiram do Mundo do Espelho: *espelho espelho meu me manda para casa* leva à cela por onde se
entrou (espera de cinco minutos) e *espelho espelho meu eu desisto* leva à cama (espera de uma hora). Elas se ouvem
no `ALLOW_CHAT_MESSAGE` do Fabric, que é o lugar de hoje para o que o original fazia no gancho de fala.

**Desvios declarados.** (1) O Reflexo **não lança os feitiços da Vara Mística**, que é fatia à parte, nem toma a
forma de lobisomem — briga de perto ou de arco. (2) A pele dele é a de reserva do próprio Witchery, e não a pele
de quem entrou baixada do servidor de peles. (3) A **mais bela** nunca é uma Seguidora nascida na hora, por a
Seguidora não estar portada: é sempre alguém que joga. (4) Os outros dois caminhos do clique — vestir a pele de
outrem com um Frasco de Vínculo, e a Granada Duplicadora com a Esfera de Quartzo — pedem a Dobra e a Esfera, que
não existem aqui. (5) O **Rito de Convocação**, que chama o Reflexo para fora do espelho, pede a Esfera de
Quartzo e fica para quando ela chegar. (6) ~~O Rito de Infusão pede uma Lágrima da Deusa no lugar do Cozimento de
Lágrimas Ocas~~ — *resolvido: a fatia do Espírito Fluente trouxe o cozimento, e o rito voltou a pedi-lo* — e ele
não pede o demônio vivo em sacrifício, que continua fora. (7) O mundo do espelho é sempre da altura inteira: o ajuste de encolher do original
morava no arquivo de ajustes, que este mod não tem. (8) A cantiga vale **nas duas línguas**, a do original e a
de cá, porque o servidor não sabe em que língua está quem escreveu.

**Uma coisa do original que aqui vai certa:** lá a caixa que dispara a travessia era escrita à mão para cada
lado, e a do lado leste ficou com o número trocado — `maxZ` onde devia ser `maxX`. Aqui a caixa sai de uma conta
só, e os quatro lados ficam iguais.

### O espelho de textura das paredes, corrigido para todos os modelos (2026-09-27)

A foto do espelho apanhou uma coisa **que vinha de trás**, e que valia para todos os modelos de Techne já
portados: o `BoxMesh` emparelhava a figura das **quatro paredes** de cada caixa ao contrário — girada de
meia-volta em relação ao `ModelBox` de 2014.

A conta do jogo daquele tempo é esta, canto por canto: numa parede, o **menor** y da caixa fica no alto da figura
e o **maior** x fica à direita dela. O `BoxMesh` fazia o contrário nos dois eixos. Como estes modelos se desenham
**de cabeça para baixo** — é o giro de meia-volta que todo desenhista de Techne faz antes de começar —, o engano
punha a figura das paredes de pernas para o ar e trocada de lado. O fundo e o topo já estavam certos.

Em peça simétrica não se via. No espelho se viu de uma vez: o vidro saía com a ponta redonda voltada para a
emenda dos dois blocos e a faixa lavrada no meio, em vez do oval fechado que o original tem. Corrigidas as quatro
paredes, o oval fecha.

A correção vale para tudo o que usa o `BoxMesh` — a Destilaria, o Forno das Bruxas, o Caldeirão, os funis, a
Máquina de Costura, o Altar de Convocação —, e a suíte de tela inteira correu depois dela.

### O Caldeirão de Pote (2026-09-27)

A segunda panela do ofício, e a que faz quase tudo o que o Witchery tem de beber e de atirar. Ela é o contrário
do Caldeirão da Bruxa: ali a <b>ordem</b> das coisas é tudo; aqui não há ordem nenhuma — são <b>seis coisas</b>
que ou fazem uma receita, ou estragam o pote.

**O que se faz com ele:** se assenta, acende-se lume por baixo, enche-se com um balde de água, atira-se o que
entra lá para dentro e se atiram frascos de vidro. Fechada a receita, o líquido toma a cor dela e fica pronto; aí
chega-se com um frasco na mão e se tira. Cada frasco que sai gasta um dos que estão no pote.

**Errar tem preço**, e é o do original: coisa a mais, coisa que não casa com receita nenhuma, ou o lume que se
apaga — e o pote estraga. Estragado, esvazia-se com um balde e se começa de novo.

**O casamento tem dois feitios**, como lá: <b>inteiro</b>, com as seis casas cheias, e <b>pela metade</b>, enquanto
se enche — é o segundo que dá a cor ao líquido antes de a última coisa entrar, e é por ele que se sabe, olhando,
que se está no caminho certo.

**Com ele vieram três coisas que caem dos bichos** — a Língua de Cão do lobo (uma em três), o Coração de Creeper
do creeper (duas em cem) e o Dedo de Sapo do sapo (uma em cinco) — e a **Sopa de Redstone**, que é a primeira
receita do pote e a base dos óleos do ofício.

**O modelo é o do original peça por peça:** as quatro paredes, o fundo, a barra de cima de onde ele pende, as
quatro correntes e a tampa de líquido com as quatro figuras que se revezam de segundo em segundo, pintada da cor
da receita — meia-luz enquanto cozinha, cor cheia quando fica pronta, e alaranjada quando estraga. A barra só se
desenha quando não há bloco por cima, como lá.

**Uma armadilha que a foto apanhou:** as correntes saíam para o lado errado, uma delas atravessando meio céu. O
`ModelRenderer` de 2014 gira as peças na ordem <b>Z, depois Y, depois X</b>, e eu girava Y antes de Z. Com a ordem
certa elas pendem como devem.

**Desvios declarados.** (1) A água é <b>sim ou não</b>: um balde enche, um balde vazio esvazia. No original é um
tanque de mil medidas que outros mods podem encher aos poucos; sem esses mods à volta, o que se vê é o mesmo. (2)
Os dois acréscimos de frasco — o do <b>chapéu de bruxa</b> e o do <b>familiar de cozimento</b> — ficam de fora,
porque nem o chapéu nem os familiares estão portados. (3) A tabela do original tem <b>trinta e sete</b> receitas;
esta leva traz a que o mod já consegue dar. As outras esperam a Teia do ofício, a Asa de Mocho, o Leite
Purificado, a Fome Melíflua, o Fio Enfeitado, o Espírito Subjugado, a Pedra Sintonizada, o Coração de Demônio —
e, sobretudo, os <b>frascos que elas fazem</b>, que são fatia à parte: cada um tem o seu efeito ao bater, e isso é
o `EntityWitchProjectile` inteiro. (4) A <b>Asa de Mocho</b> não entra porque o jogo de hoje não tem mocho, e o
<b>Dedo de Sapo</b> cai do <b>sapo</b> do jogo de agora, que faz o mesmo papel do Toad do original. (5) A
<b>Arthana</b>, que sobe todas estas chances de queda, ainda não está portada.

### Os frascos do Caldeirão de Pote (2026-09-27)

O pote sem frascos era uma panela sem receita. Esta fatia traz os **sete primeiros** — e com eles o ofício passa a
ser uma coisa de andar com frascos no cinto.

**Cada um faz uma coisa só**, escrita à mão no `EntityWitchProjectile` do original, e é isso que os separa dos
cozimentos do Caldeirão da Bruxa: ali a mistura é que manda; aqui cada frasco é um efeito.

- **Vinhas**: batendo numa **parede**, a vinha nasce nela e desce até onde a parede for, seguindo-a de degrau em
  degrau; depois sobe do mesmo jeito. No chão e no teto não pega.
- **Espinhos**: cacto. A terra vira areia debaixo dele, e o cacto sobe três. Em quem apanha, nascem quatro à volta.
- **Tinta**: cegueira a quatro de raio, tanto mais longa quanto mais perto — e os bichos perdem o alvo.
- **Brotação**: um galho de tronco cresce quinze casas na direção em que o frasco bateu, com folha aqui e ali. Se
  cresce para cima, leva consigo quem estava em cima dele.
- **Erosão**: come uma bola de dois de raio e devolve em **obsidiana** o que havia dela. Em quem apanha, ácido —
  oito de dano, e a armadura se gasta cem.
- **Amor**: os bichos em roda se apaixonam, e os filhotes crescem de uma vez.
- **Erguer os Mortos**: um morto se levanta onde ele bate. É o mesmo levantar do cozimento de caldeirão, que já
  estava portado — não se escreveu duas vezes.

**Um frasco que não pega volta ao chão em item**, como no original: um de espinhos atirado contra pedra do nether
não se perde.

**As oito receitas são as do original**, ingrediente por ingrediente e cor por cor — inclusive o Erguer os Mortos,
que pede quinhentos de poder de altar, e a Sopa de Redstone, que pede mil.

**O livro ganhou a página do pote:** as seis coisas em roda, o poder que ela pede e o que sai, tirados da
**própria tabela** e montados na hora de desenhar.

**E uma coisa que a foto do livro apanhou**, e que vinha das fatias das bonecas e dos espelhos: as páginas deste
porte marcam o negrito com `<b>`, e o livro não conhecia essa marca — mostrava-a por escrito no meio da frase. O
quebrador de linha do livro passou a trocá-la pelo código de negrito do jogo, que é o que o original usava.

**Desvios declarados.** (1) O frasco **reforçado** fica de fora: lá ele estende o alcance de cada efeito e depende
de se ter um **familiar de cozimento** acordado, e os familiares não estão portados. (2) O Cozimento de Amor não
junta o par de **aldeões** à força nem o de **zumbis escravizados** — o primeiro seria outra coisa no jogo de
hoje, e o segundo pede a Poção de Escravizar. (3) Ficam para as próximas levas os outros vinte e nove frascos da
tabela, e com eles a Teia do ofício, a Asa de Mocho, o Leite Purificado, a Fome Melíflua, o Fio Enfeitado, o
Espírito Subjugado, a Pedra Sintonizada e o Coração de Demônio.

### Os frascos do pote, segunda leva (2026-09-27)

Mais cinco, e com eles três coisas que o pote pedia e não havia.

- **Teias**: teia de aranha na casa em que bate e nas seis em volta.
- **Gelo**: havendo **água** encostada, ela congela de casa em casa até três de distância; batendo em chão ou
  parede seca, sobem **três colunas de gelo** à frente de quem atirou; e em quem apanha, uma **gaiola** de quatro
  de alto. Os que o gelo não segura — o blaze, o wither, o golem de ferro, o dragão e o Ent — só recebem água, e
  o creeper estoura ali mesmo, como no original.
- **Infecção**: a pedra, o pedregulho e o tijolo de pedra apodrecem em **pedra-de-bicho**; o aldeão vira zumbi; e
  quem mais apanhar leva um golpe e fica lerdo por cinco segundos.
- **Troca**: o que estiver **largado no chão** em roda toma o lugar do bloco em que o frasco bateu, casa por
  casa, do mais perto para o mais longe, até acabarem os itens.
- **Profundezas**: este não se atira — **se bebe**. Quinze segundos em que se respira debaixo da água e, fora
  dela, se definha. É a troca do peixe: o mar passa a ser casa, e a terra deixa de ser.

**As três coisas que vieram com eles**, todas de bancada, como no original: a **Teia do Ofício** (linha em cruz
sobre uma teia de aranha), a **Maçã Bichada** (maçã, carne podre e açúcar) e o **Leite Purificado** (um balde de
leite passado pelo Odor de Pureza, em três potes de barro) — que, bebido, tira **um** efeito qualquer de quem o
bebeu, uma vez em duas.

**E o Coração de Creeper passou a estourar ao ser comido**, que é o que ele faz no original e ficara por fazer na
fatia de ontem. Fica também o instante de resistência ao fogo que o original declara — e que não serve de grande
consolo.

**Desvios declarados.** (1) A **Troca** corre do meio para fora pela distância; no original ela corre numa
**espiral** desenhada pelo `EffectSpiral`, que é um relógio de animação, e não uma regra do que se troca.
(2) O Coração de Creeper estoura **um e sem fogo**: é o manso dos dois que o original oferece, e o outro depende
de um arquivo de ajustes que este mod não tem. (3) Ficam fora, por dependerem do que não existe aqui: o
**Solidificar** (os quatro frascos que endurecem a poça de Lágrimas Ocas, que vem do Espírito Fluente), o
**Grotesco** e o ~~**Sono**~~ (que pedem a Dobra e o Mundo dos Sonhos — *o do Sono entrou com a fatia do
Mundo dos Espíritos*), o de **Morcegos** (que pede o Laço de Bicho),
o **Revelar** e os **óleos** (que pedem casar poção com poção, e não item com item), e os três de **familiar** —
o Salto Amaldiçoado, a Língua de Sapo e o Hitchcock —, que no original só se conseguem fazer com um familiar
acordado.

### A Pedra Sintonizada, a Roca, o Braseiro e o Crisol de Sangue (2026-09-27)

**A peça que faltava era pequena e estava à vista.** A **Pedra Sintonizada** — o `itemAttunedStone` — sai de uma
receita de bancada simples: um **Sopro de Magia** sobre um **diamante** sobre um **balde de lava**. Os três já
existiam aqui. Com ela:

- a **Destilaria** deixa de pedir um diamante no lugar dela, e **o desvio declarado na fatia dela cai**;
- o **Caldeirão de Pote**, que estava sem receita de montagem nenhuma, ganha a do original (varas, linha, um
  caldeirão e a pedra);
- e a **Roca** e o **Braseiro** passam a ser montáveis.

**A Roca** fia o que não se fia à mão: cinco casas — a fibra, três temperos e o que sai —, trezentos tiques por
fio e seis décimos de poder de altar por batida. Sem altar por perto ela para, e diz isso na tela com o mesmo
quadradinho da Destilaria. A roda gira enquanto ela fia, e o novelo gira com ela no outro sentido, que é o que o
original faz.

**Das quatro receitas dela, duas.** A **teia**, que é oito de linha e mais nada, e o **Fio Dourado**, que sai de um
fardo de feno com um Sopro de Magia. As outras duas fiam o **algodão do sonho** — o Sonhador e o Perturbado —, que
só nasce no Mundo dos Sonhos. *Resolvido na fatia seguinte: o Algodão Sonhador e o Perturbado nascem lá, e as
quatro receitas da Roca estão todas de pé.*

**O Braseiro** é o contrário de todas as outras máquinas do ofício: <b>não sai nada dele</b>. Põem-se três coisas,
acende-se — com isqueiro ou com redstone — e o que ele faz é o que acontece <b>em volta</b> enquanto o fogo dura.
Apaga-se com um balde de água, que volta vazio, ou com um frasco; quebrado aceso, larga cinza e mais nada, porque
o que estava dentro ardeu.

**Das oito receitas dele, quatro:** o **Sinal de Fumaça** (pólvora, cal virgem e pó de pedra luminosa), que faz uma
coluna que se vê de longe por cinco minutos; o **Fogo da Força** e o do **Couro Duro** (uma Lágrima da Deusa com
osso ou carne podre e pó de blaze), que derramam Força e Resistência a quatro de distância; e o **Fogo que Some**
(pérola do end, olho de aranha e vara de blaze), que dá Invisibilidade a seis, por dez minutos. As outras quatro
chamam <b>espíritos</b> — o Espectro, a Banshee e o Poltergeist — e pedem o Pó de Cemitério e o Medo Condensado.

**O Crisol de Sangue está de pé, e é honesto dizer que ele ainda não faz nada.** Ele é peça de <b>vampiro</b>: o
vampiro despeja nele o que bebeu, cinco de cada vez até vinte, e o crisol cheio lhe abre a escolha do dom maior —
a Tempestade com uma alcachofra-d'água na mão, o Enxame com lã de morcego, a Colheita com um osso. O bloco está
inteiro: monta-se, guarda o sangue, mostra-o subindo dentro dele e sabe a conta dos três dons. O que falta é o
vampiro, e ele se liga por <b>dois fios</b>: o `feed` ao gole e o `BloodCrucibleBlock.level` à conta do grau, que
hoje devolve zero de propósito. Até lá, quem clicar nele ouve o mesmo "não" que o original dá a quem não é
vampiro.

**Os três modelos são os do original, caixa por caixa**, tirados pelo gerador `wi-modelo.js`, que lê um
`ModelX.java` de 2014 e escreve as linhas de `BoxMesh` — e que fica para os modelos que vierem.

~~**Um desvio declarado, e é o único:** a receita de montagem do **Braseiro** pede, no original, uma **Pedra
Necrótica**, que sai de um rito que pede a Pedra Sintonizada e o **Pó Espectral** — e o Pó Espectral só cai de um
bicho morto com a **Arthana**, que não está portada. No lugar dela vai a **Pedra Sintonizada**.~~
*Resolvido na fatia da Arthana: a faca chegou, o pó com ela, e o Braseiro voltou a pedir a Pedra Necrótica.*

### O sono, o Mundo dos Espíritos e as Teias de Sonho (2026-09-27)

**Há um lado de lá, e não se vai a ele: dorme-se para ele.** Esta fatia é o `WorldProviderDreamWorld` do Witchery
inteiro, e é a primeira do porte em que o jogador **muda de mundo com o corpo ficando para trás**.

**Como funciona.** se Bebe o **Cozimento do Sono** — ou se come a **Maçã do Sono** — e três coisas acontecem de
uma vez: um **Corpo Adormecido** fica deitado no chão onde a pessoa estava, em carne e com tudo o que ela levava;
a mochila, a vida e a fome são **trocadas** por um segundo conjunto guardado no próprio jogador; e o espírito
acorda no **Mundo dos Espíritos**, no mesmo ponto do mapa, no chão alto de lá.

**O mundo de lá é o daqui.** Ele se abre com o **gerador do mundo de cima**, que é o que o original faz: o chão é
o mesmo, monte por monte, e quem anda em espírito reconhece o caminho de casa. O que muda é que não há gente,
e que nascem lá duas plantas que não nascem em mais nenhum lugar: o **Algodão Sonhador** e a **Erva Cintilante**.

**Da travessia passa pouco**, e o original diz exatamente o quê: a **Agulha de Gelo** e o **Mutandis** vão; o
Algodão, o Perturbado, a Agulha e a **Fome Melíflua** voltam. O resto fica com o corpo. Para acordar se espeta a
Agulha em si mesmo; sem ela, morre-se para acordar — e morrer do outro lado é acordar de mãos vazias.

**A conta do pesadelo é a parte engenhosa, e é a que faz o quarto valer a pena.** O Cozimento do Sono passa uma
chance de pesadelo de **0,998**, que é quase um. A Maçã passa **um redondo**. Sobre essa chance, o original olha
os arredores de quem adormece — mas **só olha se houver um Apanhador de Sonhos com a teia dos pesadelos a menos de
oito**. Sem ele, a chance é a que veio e não há nada a fazer. Com ele: o apanhador tira **meia**, cada Algodão
Sonhador em volta tira **um décimo** até dois, e cada **fogo** aceso em volta acrescenta **um décimo** até três.
É por isso que a primeira noite é feia e o quarto de sonho é uma coisa que se constrói.

**Em pesadelo, o outro lado tem coisas que andam.** O **Pesadelo** — cem de vida, resistência a empurrão inteira,
quatro de dano e a manha de arrombar porta — só nasce lá, só persegue quem está em pesadelo e desaparece quando o
mundo já não é o dele. Deixa **Fome Melíflua**, duas vezes numa em cinco.

**A teia é o apanhador.** Isto foi uma correção de fidelidade: no original não existe apanhador vazio, porque é a
**Teia de Sonho** que se prega à parede e vira o bloco, com o feitio dela dentro — o `placeDreamCatcher` do
`ItemGeneral`. Prega-se só **de lado**, nunca no chão nem no teto. Quebrado, devolve a teia. O item `dream_catcher`
que esta fatia tinha inventado numa primeira passagem **foi tirado**.

**Cada teia tem duas caras**, e é a mesma teia que dá as duas conforme a noite corra bem ou mal: o **passo ligeiro**
(Rapidez ou Lentidão), a **mão rápida** (Pressa ou Fadiga), a **fartura** (Fartura ou Fome), os **pesadelos**
(Fraqueza ou Cegueira) e a **intensidade** (Visão Noturna ou Cegueira). As duas últimas são as que mandam nas
outras: a dos pesadelos faz o pesadelo, a da intensidade **aperta** o que houver — sobe o grau do efeito bom e
encurta-lhe o tempo. Menos o da fartura, que em vez de subir de grau dura **dois minutos a mais**, porque grau de
fartura não quer dizer nada. É a troca que o original faz.

**As receitas das teias saíram decifradas, e não adivinhadas.** As oito `GameRegistry.addRecipe` do original põem
nos dois cantos de cima **poções do jogo**, escritas em número de dano de 1.7.10. Decifrados: `16450` é Rapidez
longa, `16458` Lentidão longa, `16457` Força longa, `16456` Fraqueza longa, `16421` Cura II, `16452` Veneno longo
e `16454` Visão Noturna longa — todas **de atirar**, que é o que o bit `16384` diz. Cada par é o par de efeitos da
sua teia, e por isso nada aqui foi escolhido por este porte.

**E dois cozimentos novos**, os dois da tabela do pote do original: o **do Sono** (Leite Purificado, biscoito,
Cozimento do Amor, Sopro de Magia, Agulha de Gelo e Globo de Alcachofra) e o **do Espírito Corrente**, que é o
primeiro deste porte **preso a um mundo**: um pote fervido no mundo de cá nunca o dá. A **Maçã do Sono** deixou de
pedir o Cozimento das Profundezas de mentira e passou a pedir o do Sono, que é o que o original pede.

**Três furos, tapados aqui.** As provas de servidor desta fatia **nunca tinham corrido**: a classe delas não
estava na lista de entrada do `fabric.mod.json`, e a suíte passava sem as ver. Registrada, ela põe a conta em
seiscentas e vinte.

Os outros dois são da fatia anterior. As três máquinas — a Roca, o Braseiro e o Crisol — tinham modelo
de bloco mas **nenhum modelo de item**: na mão e no inventário não se via nada. Agora cada uma tem desenhista
próprio, com o modelo do bloco parado e vazio, como o Caldeirão de Pote. E **nenhum dos blocos novos das duas
fatias tinha tabela de despojo**: quebrar a Roca, o Braseiro, o Crisol, o Algodão ou a Erva não devolvia coisa
nenhuma.

**E a Maçã do Sono estava com os números trocados** — quatro de comida e três décimos de fartura, quando o
original diz **três e três**. Corrigida.

**Desvios declarados.**

1. **Não há pesadelo demoníaco.** No original o **Coração de Demônio** *sobe* a chance em trinta e cinco por cento
   cada, e é ele que torna o pesadelo demoníaco. É bloco de demônio, e o demônio não está portado.
2. ~~**O termo das poças de Espírito Fluente fica fora da conta.**~~ *Resolvido na fatia seguinte: o fluido
   entrou, e cada poça tira os seus dez por cento, até três.*
3. **O Algodão Sonhador é semeado por pedaço de mundo, e não por geração.** O Mundo dos Espíritos usa o gerador do
   mundo de cima, que não conhece as plantas de lá; então, quando um pedaço de mundo é carregado pela primeira vez
   do outro lado, três em cada quatro recebem uma mancha de doze algodões. O que se vê é o que o original mostra —
   um mundo coberto deles — por outro caminho.

   Isso traz uma regra que **não é escolha**: a moita não sai do pedaço, e o semeador não fala com o mundo. No
   momento em que ele corre, o pedaço ainda não entrou na lista do mundo — quem lhe pedir um bloco *pelo mundo*
   fica à espera de si mesmo; e pedir uma casa do pedaço ao lado faz o jogo gerá-lo ali, de dentro do carregamento
   do primeiro, que dispara o seguinte. O servidor trava ao entrar no outro lado. Custou uma tarde a encontrar, e
   há uma prova de servidor que impede a volta.
4. **O Corpo Adormecido leva a pele de reserva, e não a de quem o deixou.** No original ela é baixada de um
   servidor de peles. Este porte não baixa a pele de ninguém de fora; o corpo leva a mesma pele de reserva do
   Reflexo.

   E **se deita com uma volta só**, noventa graus em Z, que é a com que o jogo de hoje deita um morto. O original
   põe antes dela um `glTranslatef(0.9, 0.25, 0)` e uma segunda volta em Y: com as contas de agora esses números
   atiram o corpo para o ar e para o lado. O que se vê é o que o original mostra — um corpo caído no chão.
5. **A bancada das teias é uma receita de código, e não de arquivo.** Um ingrediente de receita do Minecraft de
   hoje não sabe olhar os componentes de uma coisa, e portanto não distingue uma poção de Rapidez de uma de
   Veneno. A conta está em `DreamWeaveRecipe`, com as poções exatas do original — e aceita os dois cantos nas duas
   ordens, que é o que a receita moldada antiga fazia ao experimentar-se também espelhada.
6. ~~**O Cozimento do Espírito Corrente é item, e não frasco de fluido.**~~ *Resolvido na fatia seguinte: ele
   atira-se e faz poça, como o `BrewFluid` do original.*

### O Espírito Fluente, a Destilaria e os Cozimentos Sólidos (2026-09-27)

**O Mundo dos Espíritos tinha um líquido, e ele faltava.** Esta fatia é o `BlockFlowingSpirit` do Witchery — que
serve aos dois líquidos do mod — e a destilação que os liga.

**O Espírito Fluente** vem de um cozimento que só ferve do outro lado. Atira-se o frasco e onde ele bate fica uma
poça. Ela **conhece quem entra nela**: gente comum sai **curada** por cinco segundos; morto-vivo, coisa do
inferno e Pesadelo saem **fracos** por quinze. E ela desfaz o pesadelo de dentro das coisas — **Algodão
Perturbado largado nela volta a ser Algodão Sonhador**, que é o `nightmareBane` do original.

**Isso fecha a conta do pesadelo.** O termo das poças estava declarado como pendente na fatia dos sonhos: cada
poça tira dez por cento, até três, e só contam as **fontes** — que é o que o original mede ao exigir metadado
zero. O desvio cai.

**A destilação que abre o fim da linha.** Passado pela Destilaria com **óleo de vitríolo**, o Espírito Corrente
parte-se em três coisas que não se conseguem de nenhum outro jeito: a **Vontade Focada**, o **Medo Condensado** e
**oito frascos de Lágrimas Ocas**, por dois potes de barro. É a receita mais importante da máquina, e a única do
mod que gasta um cozimento para fazer outro.

**As Lágrimas Ocas são o avesso dele**: nelas o morto e o demônio é que saram, e a gente comum é que definha. E
servem para uma coisa só — a que faz delas o fim da linha.

**Os cinco Cozimentos Sólidos.** Atirado numa poça de Lágrimas Ocas, cada um **endurece a poça inteira**: ele
anda por ela de casa em casa, pelas seis faces, até sessenta e quatro do ponto em que bateu, e troca tudo de uma
vez. Pedra, terra, areia, arenito — e o da **Erosão**, que não endurece nada: tira a poça **e a casa debaixo
dela**. Os cinco pedem a mesma coisa no pote — Exalação Fétida, Odor de Pureza, Mutandis, Cinza de Madeira e
Musgo Espanhol — e o que muda é a primeira casa, que diz no que a poça vira. **Dois mil de poder cada**: é o mais
caro que o Caldeirão de Pote faz.

**E o Rito de Infusão volta a pedir o que o original pede.** Ele fazia o Espelho da Bruxa com uma **Lágrima da
Deusa** no lugar do **Cozimento das Lágrimas Ocas**, porque este não existia. Existe; o desvio cai.

**Duas armadilhas do jogo de hoje, achadas aqui.**

A primeira: **`Level.removeBlock` numa casa de líquido repõe o próprio líquido.** Ele monta a casa nova a partir
do estado de fluido que lá está, e num bloco de líquido esse estado é o líquido. O Cozimento da Erosão tirava a
poça e ela voltava no mesmo instante. Ar tem de ser posto como ar.

A segunda: **andar pela poça por chamada de função dentro de si mesma**, como o `SpreadEffect.spread` do
original, estoura a pilha do jogo numa poça grande o bastante. Aqui a mesma varredura é feita com uma fila, e tem
teto de quatro mil e noventa e seis casas.

**Desvios declarados.**

1. **O líquido conta oito níveis por bloco, e não cinco.** O original põe `quantaPerBlock = 5`, que é coisa do
   fluido do Forge de 2014; o fluido do jogo de hoje conta oito, como a água, e não se lhe muda isso sem
   reescrever o motor do líquido. As poças correm um pouco mais longe do que corriam; nada mais muda.
2. **Do `isDemonic` ficam os quatro do jogo.** O original conta, além do Ghast, do Blaze, do Cubo de Magma e do
   Wither, os bichos do próprio Witchery — o Demônio, o Leonard, o Senhor do Tormento, o Diabrete e a Lilith.
   Nenhum deles está portado.
3. ~~**O Portal do Espírito fica de fora, e é fatia própria.**~~ *Resolvido na fatia seguinte: o portal, o
   fantasma e o Rito da Manifestação entraram.*
4. **As duas destilações do Coração de Demônio continuam fora**, pelo mesmo motivo de sempre: o demônio não está
   portado, e sem ele não há coração. São as únicas que faltam da tabela da máquina.
5. **Os baldes dos dois líquidos são coisa deste porte.** O original usa o balde universal do Forge, que não
   existe aqui; cada líquido ganhou o seu, com a figura do balde de sangue do Ars Mortuorum repintada da cor do
   que carrega.

### A Arthana, o Pó Espectral e a Pedra Necrótica (2026-09-27)

**A faca do ofício estava faltando, e ela é a chave de meia dúzia de coisas.** A **Arthana** — o `ItemArthana` do
Witchery — é de **ouro com a vida do ferro**, e o ouro é escolha do original: é o metal que não serve para lutar.
Ela sai de uma bancada com um lingote de ouro, uma esmeralda, duas pepitas e uma vara. Nada nela é raro.

**O que ela faz não é cortar melhor: é abrir o que os bichos guardam.** Com ela na mão, tudo o que o Caldeirão
de Pote pede vem muito mais vezes — a Língua de Cão e a Lã de Morcego passam de **uma em três para três em
quatro**, o Dedo de Sapo de **uma em cinco para uma em duas**, o Coração de Creeper de **duas em cem para
oito**. E se abre o que sem faca não se abre: a **caveira** do esqueleto, do zumbi e do creeper, e o **Pó
Espectral**, que só sai de morto-vivo aberto por ela.

**Isso é feito na tabela de despojos do jogo, e não num evento.** Cada queda é uma pilha própria com a sua
condição: umas exigem a Arthana na mão de quem matou, outras exigem que ela **não** esteja lá. Há prova de
servidor que mata um esqueleto quatrocentas vezes com a faca e quatrocentas sem, e confirma que o pó só sai de
um dos dois lados.

**E o pó abre uma pedra.** Passado por farinha de osso e Mutandis vira **Pó de Cemitério**; e num círculo de
dezesseis glifos de ritual, **de noite**, com mil de poder, uma Pedra Sintonizada, um osso, carne podre, Cinza de
Madeira, uma espada de ferro e o próprio pó, o **Rito de Necromancia** dá a **Pedra Necrótica**.

**Com ela cai o desvio declarado do Braseiro.** Ele pedia a Pedra Sintonizada no lugar da Necrótica, porque a
Necrótica dependia de um pó que dependia de uma faca que não existia. Agora pede o que o original pede, e é a
única das três máquinas que exige o círculo antes da bancada — o que a pesquisa do livro passa a dizer.

**Desvios declarados.**

1. **A Arthana não se pousa no Altar.** No original ela tem um `BlockPlacedItem` que a deixa à vista em cima da
   pedra. Esse bloco não está portado — nem para ela, nem para as outras coisas que o original pousa lá — e
   entra quando ele entrar.
2. **A caveira de quem se mata fica fora.** O original dá a quem derruba outro jogador com a faca uma caveira com
   o nome do morto escrito nela. É conversa entre mundos que este porte não quer travar sozinho.
3. **O Boline fica para a fatia das plantas dele.** É a outra faca — a de colher — e o que ela tem de próprio é
   arrancar inteiras a Planta-Armadilha e a Rosa de Sangue, que não estão portadas. Sem elas seria só uma espada
   de ferro com outro nome.
4. **A Asa de Mocho continua fora**, pelo mesmo motivo de sempre: o jogo de hoje não tem mocho.
5. **As quatro fumaças de espírito do Braseiro continuam por acender.** Elas agora têm os dois ingredientes que
   lhes faltavam — o **Pó de Cemitério**, que esta fatia traz, e o **Medo Condensado**, que a do Espírito Fluente
   trouxe —, mas o que elas chamam são o Espectro, a Banshee e o Poltergeist, e esses bichos não estão portados.
   O que falta já não é ingrediente: é gente do outro lado.

### O Portal do Espírito e o fantasma (2026-09-27)

**A fatia do Espírito Fluente deixou um gancho declarado, e ele fecha aqui.** O que faltava era o
`BlockSpiritPortal` do Witchery e a manifestação — a parte do `WorldProviderDreamWorld` que traz de volta ao
mundo de cá quem está do outro lado, sem o acordar.

**A cadeia inteira, que só agora existe.** O **Rito da Manifestação** não abre porta nenhuma e não mostra nada:
o que sai dele é **crédito**, cento e cinquenta segundos de corpo neste lado, guardados no jogador até se
precisar deles. Ele pede o que só a Arthana abre — Pó Espectral, Fome Melíflua, Pedra Necrótica, uma picareta de
ouro, a própria faca e pólvora —, cinco mil de poder e um anel de dezesseis glifos de ritual.

**A porta se monta do outro lado.** Um vão de dois por dois com moldura de **neve** em volta, e uma fonte de
**Espírito Fluente** derramada lá dentro. É a única coisa que o Espírito Fluente acende, e só acende no Mundo dos
Espíritos: uma poça no mundo de cá não faz portal nenhum.

**Atravessando-o com crédito**, o espírito volta ao mundo de cá em **fantasma**, no mesmo ponto do mapa. A
mochila fica do outro lado; só as **Agulhas de Gelo** atravessam. O relógio desce de cinco em cinco segundos e
avisa aos sessenta, aos trinta e aos quinze; no zero, o fantasma é puxado de volta, queira ou não — e o que ele
tiver apanhado no mundo de cá fica cá, porque fantasma não carrega coisa de gente.

**E entrou a regra que faltava: quem anda em espírito não morre.** No Mundo dos Espíritos e em fantasma, o golpe
que mataria é **apagado** e em vez dele o espírito volta ao corpo. É o que o `onLivingHurt` do original faz, e é
o que torna o outro lado jogável: o corpo está deitado no mundo de cá, à vista de qualquer um, e morrer *lá*
mataria o que está *aqui*. Quem joga em criativo não entra nesta conta, como no original.

**Um achado sobre o original.** A moldura do portal, no Witchery, é de **camada de neve** — e uma camada de neve
precisa de chão firme por baixo. A fileira de cima da moldura fica sobre o **vão**, que é ar: a moldura do
original **não se consegue montar em jogo**. Ou se monta dentro de uma estrutura sólida que depois se tira, e aí
a neve cai com ela.

**Desvios declarados.**

1. **A moldura aceita a neve nas duas formas**, a camada e o bloco. É a correção do achado acima: com o bloco,
   que se empilha, a moldura passa a ser construível sem deixar de ser de neve. A camada continua valendo, para
   quem conseguir montá-la.
2. **O crédito de manifestação se soma**, e o original guarda um número só. Dois ritos dão o dobro de segundos;
   no original o segundo rito reescreveria o primeiro. Somar é o que a leitura do `RiteSetNBT` sugere e é o que
   não desperdiça o que se ofereceu.
3. **A picareta de ouro do rito é leitura, e não certeza.** O original pede o `Items.field_151005_D`, que é uma
   ferramenta de ouro; qual delas, o nome ofuscado não diz. Pela ordem em que o `EarthItems` do próprio mod
   emparelha as ferramentas de ferro com as de ouro, ela é a **picareta**. Se um dia se provar que é o machado,
   troca-se uma linha.
4. **O fantasma não se vê de fora como fantasma.** No original ele é desenhado translúcido, por um pacote de
   estilo que o servidor manda a todos. Este porte não tem esse pacote; o fantasma anda visível como qualquer
   um. O que ele é continua valendo em tudo o resto — o que carrega, o relógio, e não morrer.

### Os ritos se guardam em disco (2026-09-27)

**Um rito a correr morria ao desligar o mundo.** Estava declarado desde a fatia dos círculos, com a nota de que
guardá-lo pediria que cada passo soubesse escrever-se. Pedia menos do que isso.

**O que se guarda não são os passos: é o nome do rito e quantos passos faltam.** Ao voltar, a fila é remontada
da lista de ritos — sacrifício mais rito, na mesma ordem em que ela se monta ao começar — e cortada no ponto em
que estava. Com ela voltam quem o começou, o tamanho do coven, o lugar que o rito escolheu e **o que já se
ofereceu**, para que um rito que desista pedindo devolução continue a pôr tudo de volta no chão.

**Um rito cujo nome já não exista é largado** em vez de estourar, e o mesmo vale para uma fila maior do que o
rito tem — que é o que acontece a um mundo salvo com uma versão do mod em que o rito era mais comprido.

**O que se perde com isto** é o estado que um passo tenha <b>só para si</b>, fora do {@code ActiveRite}. Nenhum
dos ritos deste porte tem: os passos leem do mundo, do círculo e do que está guardado no rito, e o que eles
precisam de lembrar — o alvo — já mora no rito. O dia em que um passo precisar de memória própria, ele passa a
escrevê-la; até lá, isto é tudo.

### Os ritos do tempo e da terra (2026-09-27)

**A tabela de ritos do original tem noventa e seis entradas, e este porte tinha oito.** Quase todas as que
faltam pedem coisa que ainda não existe aqui; estas quatro não pediam nada.

**O Rito da Tempestade** — o `RiteWeatherCallStorm` — chama o raio. De trinta em trinta batidas cai um num anel
em volta do círculo, nunca em cima dele, e na **quarta** vez o céu se fecha numa trovoada de cinco a quinze
minutos. Depois disso caem raios a esmo até a conta acabar.

**O Rito de Cozer** — o `RiteCookItem` — coze tudo o que for comida e estiver largado a cinco do círculo, e
queima oito por cento em **carvão vegetal**. Não havendo nada que se coza, ele desiste e devolve o que se
ofereceu, que é o que o original faz.

**O Rito de Erguer a Terra** — o `RiteRaiseColumn` — levanta um cilindro de chão uma casa de cada vez, oito
vezes, com quem estiver em cima a subir junto. A borda sai desigual de propósito: um bloco de beira em cada sete
fica para trás.

**E o Rito de Partir a Terra** — o `RitePartEarth` — abre uma vala torta de sessenta passos a partir do círculo,
cavando um buraco fundo em cada um. É o mais barato de todos: pede um Cozimento de Erosão e mais nada.

**A fase de um rito passou a morar no rito, e não no passo.** Estes quatro correm o mesmo passo muitas vezes e
precisam de saber quantas já correram; se essa conta vivesse dentro do passo, um mundo desligado no meio de uma
tempestade voltaria do começo. Ela mora no `ActiveRite`, guarda-se com ele, e há prova disso.

**Desvios declarados.**

1. **O caminho da vala é semeado pelo lugar do círculo**, e no original sai do relógio de sorte do mundo. Com o
   relógio do mundo o caminho seria diferente a cada vez — e perder-se-ia ao desligar. Semeado pelo lugar, ele é
   sempre o mesmo para o mesmo círculo, e é isso que deixa o rito continuar de onde estava. O que se vê é igual;
   o que muda é que a mesma pedra dá sempre a mesma rachadura.
2. **A Pedra de Caminho opcional fica de fora.** A Tempestade e o Erguer a Terra aceitam, no original, uma Pedra
   de Caminho ligada como oferenda opcional, para o rito acontecer **onde ela aponta** em vez de no círculo. A
   Pedra de Caminho não está portada; os dois ritos acontecem no círculo.
3. ~~**Ficam de fora, por dependerem da Pedra Sintonizada Carregada:** o Vulcão, as Barreiras e as versões
   portáteis do Eclipse e da Tempestade.~~ *Resolvido em parte na fatia seguinte: a pedra carregada e o Vulcão
   entraram; as Barreiras esperam o bloco de barreira, que é peça própria.*

### A Pedra Sintonizada Carregada e o Vulcão (2026-09-27)

**Vinte e cinco dos noventa e seis ritos do original não correm sem uma coisa**, e ela não existia aqui: a
**Pedra Sintonizada Carregada**. É a mesma pedra passada pelo **Rito da Carga** — dois anéis, dezesseis glifos
por dentro e vinte e oito por fora, dois mil de poder, e uma Pedra Sintonizada com pó de pedra luminosa,
redstone, Cinza de Madeira e Cal Virgem. Tudo o que ele pede já existia.

**E com ela entrou o maior estrago que o ofício faz.** O **Rito do Vulcão** não se faz em qualquer lugar: o
círculo tem de ter **lava por baixo**, e a conta do original é exigente — uma casa de lava com **duas vizinhas
de lava**, medidas nas seis casas que ele olha, e não um pingo. Não achando, ele desiste, devolve o que se
ofereceu e diz ao dono do círculo por quê.

**Achando, ele levanta um cone** de quinze em quinze batidas, camada a camada, com a beira de baixo salpicada de
grama e quem estiver em cima a subir junto. Erguido o cone, a lava **sobe por dentro** até o alto; no penúltimo
passo ela transborda e o cume **se rompe por um dos quatro lados**, a esmo. No último, a coluna que veio de baixo
é fechada — e o que fica é um monte com uma cratera, e não um cano de lava aberto até o fundo do mundo.

**Os números são os do original**, incluindo os que parecem enganos e não são: o raio da camada `y` se conta
como `raio - (alto - fase - 1 + y) * raio / alto`, e é essa conta torta que faz o cone crescer de dentro para
fora em vez de subir reto. As linhas do círculo encolhem uma casa a cada cinco, a esmo, e é isso que tira a
régua da borda.

**Desvios declarados.**

1. **O rompimento do cume conta oito e usa quatro.** O original sorteia de zero a sete e só os quatro primeiros
   abrem um lado; nos outros quatro não acontece nada. Está portado assim, porque mudar isso mudaria a chance.
2. ~~**As Barreiras continuam de fora.**~~ *Resolvido na fatia seguinte: o bloco de barreira e os três ritos
   entraram.*

### As Barreiras (2026-09-27)

**Há ritos que não acontecem e acabam: sustentam-se.** Os três **Ritos da Barreira** — a `RiteProtectionCircleBarrier`
sobre a `RiteProtectionCircle` do Witchery — são os primeiros deste porte a correr **para sempre**, enquanto
houver com que os pagar.

**A cúpula.** De vinte em vinte batidas o rito desenha chão, parede cilíndrica e teto em volta do círculo, e
cada casa dela é um **bloco de barreira** com trinta batidas de vida. Parado o rito, a parede se desfaz sozinha
em segundo e meio — não há nada a limpar, e não fica entulho de um rito interrompido.

**E ela sabe de quem é.** Uma barreira que trava gente deixa passar **quem a ergueu**, e quem estiver em criativo
agachado. Isso mora na casa, e não no rito: cada bloco guarda o dono, quanto tempo lhe falta e se trava gente.

**Os três.** A **Barreira** pede obsidiana e redstone e trava só o que não é gente; a **Maior** pede obsidiana e
pó de pedra luminosa, é mais alta e mais larga, trava gente também e exige o anel de vinte e oito. As duas
**comem poder a cada batida** e morrem sem Altar por perto. A **Portátil** pede a **Pedra Sintonizada Carregada**,
não come nada e dura um minuto certo — é a que se leva para onde não há altar.

**Desvios declarados.**

1. **A Pedra de Caminho opcional continua de fora**, como na Tempestade e no Erguer a Terra: no original, a
   Barreira e a Maior aceitam uma ligada para a cúpula nascer **onde ela aponta**. Ela não está portada.
2. **A fonte de poder é procurada de novo a cada batida.** O original guarda o altar que achou e só volta a
   procurar uma vez em cinco, para poupar trabalho. Aqui a procura é a mesma que todos os outros ritos já fazem,
   e não valeu a pena duplicar o cache por isso.

### As versões maiores e as portáteis (2026-09-27)

**O original tem o mesmo rito três vezes, e a diferença não está escrita em lado nenhum: está na ferramenta.**
A Tempestade pede uma espada de **pau**; a Maior, uma de **pedra**; a Portátil, uma de **ferro**. O Eclipse pede
um machado de pedra e o Portátil um de ferro. É a escada que diz, sem palavras, qual é qual.

**E a diferença de verdade é como se paga.** As versões normais e maiores comem **poder do Altar** — dois mil
na Tempestade Maior, três mil no Eclipse. As portáteis não comem nada: pagam com a **Pedra Sintonizada
Carregada**, que se gasta ali. São as que se levam para onde não há altar.

**Quatro entraram**: a Tempestade Maior (que alcança sete de raio em vez de três, e corre dezoito fases em vez
de oito), a Tempestade Portátil, o Eclipse Portátil e a Fertilidade Portátil — esta última trocando, como no
original, o Mutandis comum pelo **Extremis**.

**Uma correção de fidelidade no caminho:** o Eclipse pedia uma **espada** de pedra neste porte, e o original pede
um **machado**. O nome ofuscado (`Items.field_151049_t`) não diz qual ferramenta é; o que o diz é a escada — o
Eclipse Portátil pede um machado de ferro, e os dois têm de ser do mesmo feitio. Corrigido.

**Desvio declarado.** A Pedra de Caminho opcional continua de fora nas duas Tempestades, pelo mesmo motivo de
sempre: ela não está portada.

### A Maldição da Cegueira (2026-09-27)

**A primeira das maldições que se abrem em roda.** A `RiteBlindness` do Witchery usa a mesma base que a
Fertilidade — o anel que cresce de cinco em cinco batidas — e por isso ela entrou de graça: não foi preciso
escrever motor nenhum, só dizer o que o anel faz a quem apanha.

**O que ele faz são dois minutos de escuro**, em gente e em bicho, do círculo até **oitenta casas**. E só a quem
está <b>naquele anel</b>: quem já ficou para trás não leva outra vez, e quem ainda vem espera a sua vez.

**E há uma defesa, uma só.** Quem trouxer uma **boneca de proteção contra vodu** presa a si a gasta — e o rito
**morre de vez**. É a única coisa que pára a maldição, e é a que o original dá.

**Desvios declarados.**

1. **O Caçador de Bruxas não é avisado.** No original, fazer magia negra chama um sobre quem a fez. Ele não está
   portado, e a maldição corre sem consequência.
2. **O familiar de maldição não dobra o escuro.** No original, uma bruxa com esse familiar acordado faz o escuro
   durar cinco minutos em vez de dois. Os familiares não estão portados; ficam os dois minutos.

## Ars Arcana — o Ars Magica 2

O quarto ramo de fora: o **Ars Magica 2 1.4.0.009**, de Mithion (919 classes), que a lore de quem joga chama de
**Ars Arcana — a Gramática da Magia**.

**Onde os outros perguntam outra coisa.** A Thaumaturgia pergunta *por que a magia funciona*. O Ars Occulta
pergunta *que vínculo faz o mundo responder*. O Ars Arcana pergunta **como construir exatamente o efeito que se
quer** — e a resposta dele é que um feitiço não é uma receita, é uma **frase**.

### Fatia 1 — a gramática, a mana e o lançar (2026-09-27)

**Três classes de palavra, e o feitiço é a frase.** A **Forma** diz como o efeito entra no mundo; a **Essência**
diz o que ele faz; os **Modificadores** mudam os números de uma e de outra. Trocar uma palavra faz outro
feitiço, e é isso que separa este ramo de tudo o que já está portado: não há lista de feitiços, há gramática.

**Uma frase pode ter mais de uma etapa**, e é a própria Forma que passa adiante: acabada a dela, ela tira a
etapa da frente e lança o que sobra. É por isso que um Toque seguido de uma Área encadeia sem ninguém escrever
um laço — o toque que pega acorda a área que vem depois.

**A Mana não é Vis, e a lore insiste nisso.** Vis é a energia que existe no mundo; Mana é o quanto um corpo
consegue puxar dela de uma vez. Um arcanista seca no meio de uma aura cheia: a energia está lá, o cano é que
acabou. A conta do teto é a do original, tal e qual — `nível^1,5 × (85 × nível/99) + 500` —, lenta no começo e
disparando no fim.

**E há o desgaste**, o `fatigue` do original: cada feitiço deixa **38 por cento** do que custou, e
cheio ele impede de lançar. É o que impede alguém com mana de sobra de despejar feitiços sem parar.

**O que entrou de cada classe.** Três Formas — **Autoconjuração** (metade do preço), **Toque** (duas casas e
meia à frente, bicho ou bloco, o que estiver mais perto) e **Área** (tudo em roda, três casas). Cinco Essências
— **Dano de Fogo**, **Dano Gélido**, **Cura**, **Luz** e **Escavar**. E seis Modificadores — **Dano**,
**Alcance**, **Duração**, **Raio**, **Cura** e **Força de Mineração**.

**Os números são todos do original**, incluindo os que surpreendem: o modificador de **Raio** multiplica por
**0,7**, ou seja **encolhe** — no Ars Magica 2 ele custa duas vezes e meia por vez e serve para *apertar* uma
área, para o feitiço não apanhar quem não devia. E o **Dano** **soma** 2,2 em vez de multiplicar, que é o que
impede um feitiço de dano de crescer sem fim.

**Um feitiço que falha é de graça.** Só se cobra depois de a etapa pegar, que é o que o original faz ao
devolver `EFFECT_FAILED` antes de tirar mana. Curar quem está com a vida cheia não custa nada.

**Desvios declarados.**

1. **As peças se guardam por nome, e não por número.** O original numera cada peça e soma mil às Essências e
   cinco mil aos Modificadores para as separar; um feitiço escrito numa instalação fica ilegível noutra em que
   os números tenham andado. Aqui se guarda o nome, e a separação é o próprio tipo da peça. **É de propósito e é
   melhor:** um feitiço escrito num mundo continua legível noutro.
2. **O feitiço mora num componente, e não espalhado pelo NBT.** O original guarda `NumStages`,
   `ShapeOrdinal_0`, `SpellComponentIDs_0` e companhia em chaves separadas. Aqui é uma coisa só.
3. **A Afinidade entrou na fatia 3.** (Ficava aqui a nota de que ela faltava.)
4. **A Mesa de Inscrição entrou na fatia 7 e a árvore de perícias na fatia 8.** Faltam os Rituais de Obelisco.
5. **As quinze Formas entraram**, a última delas — o Vínculo — na fatia 10.

### Fatia 2 — o relógio da mana e o Projétil (2026-09-27)

**O relógio.** De vinte em vinte batidas a mana sobe um pouco e o desgaste desce outro. Encher por inteiro leva
**1800 batidas** a quem não tem nível, e **1200** a quem chegou ao 99: um
minuto e meio, ou um minuto. É esse tempo que faz a mana valer alguma coisa — quem a gastou espera.

**E o desgaste desce com o nível, e é só isso que o faz.** A conta é `0,01 × nível × batidas`. Com nível zero
ela dá **zero**: um arcanista sem nível que se gastou **fica gasto**. Não é que tenha pouca mana — é que não se
recupera. É o original, e é a razão mais forte que o Ars Magica 2 dá para subir de nível.

**Quem joga em criativo enche na hora**, como no original.

**O Projétil.** A Forma que define o ramo para quem o joga. Ela não procura alvo: **atira**. O feitiço inteiro
entra numa entidade que voa e que, ao bater, corre as Essências daquela etapa e **lança dali o que sobra da
frase**. É o que faz um Projétil seguido de uma Área explodir no lugar da batida e não na mão de quem lançou.

Ele **não tem física do jogo**: se move à mão, sem arrasto, sem gravidade a não ser a que o modificador der. Um
projétil sem modificadores voa a direito a um bloco por batida até bater ou até acabarem as **100 batidas** de
vida. Ele **atravessa** (Perfuração) e **salta** (Ricochete, com 0,8 da velocidade a cada salto), e
cada bicho e cada bloco só contam uma vez — o original guarda a lista do que já apanhou.

**Ela dá sempre por boa**: atirar custa mana mesmo que o projétil nunca venha a bater em nada, porque o que
pegou foi o atirar.

**Cinco Modificadores novos**, com os números do original: **Velocidade** (multiplica por 2,6, 15 por cento
a mais por vez), **Gravidade** (soma **−0,06** — o sinal negativo é o que a faz cair, porque o
projétil lê a gravidade ao contrário do que o nome sugere), **Ricochete** (dois saltos), **Perfuração** (dois a
mais) e **Alvos Não Sólidos** (pega em água e no que não tem caixa).

**A Gravidade e os Alvos Não Sólidos são de graça.** No original os dois devolvem `1.0F` *sem* multiplicar pela
quantidade, ao contrário de todos os outros. Não é descuido: são os dois modificadores que mudam *como* o
feitiço se comporta e não *quanto* ele faz.

**A conta de quem atravessa parte de zero** e não do dois que o feitio traz: um projétil sem Perfuração morre no
primeiro que apanhar. O dois é o que *cada* Perfuração acrescenta.

**Desvios declarados.**

1. **A Duração num Projétil não faz nada — e é um erro do original que este porte mantém.** A Forma lê o
   modificador de Duração para decidir a vida do projétil e **joga o número fora**: o campo que a guardaria é
   final e nasce a menos um, e o tique troca o menos um por 100. Fica como está, porque corrigi-lo mudava o
   alcance de todo feitiço de projétil do jogo.
2. **A divisão do relógio é corrigida.** A conta do original é `2400 × (0,75 − 0,25 × (nível/99))` com `nível` e
   `99` **inteiros**: essa divisão dá zero para todo nível abaixo de 99, e o nível não conta para
   nada. Aqui é feita em vírgula flutuante, que é o que a fórmula claramente queria. **É uma correção e não uma
   escolha de gosto:** sem ela, metade da frase do original (`0,25 × …`) seria código morto.
3. **O perseguir ficou de fora, porque no original é código morto.** A entidade sabe perseguir
   (`setHoming`, busca num raio de 15 blocos, vira 60 graus por batida), mas **não existe nenhum
   modificador** que ligue o `HOMING` — não há `Homing.java` entre os modificadores do jar. Fica de fora até
   haver com que o ligar.
4. **O refletir feitiços ficou de fora**, porque depende da lista de bênçãos (`BuffList.spellReflect`), que não
   está portada. No original, um alvo com essa bênção manda o projétil de volta a quem o lançou.
5. **A figura e a cor por Afinidade entraram na fatia 3.** (Ficava aqui a nota de que faltavam.)
6. **O desenho anda a tira ele mesmo.** A `lens_flare` é uma tira de 13 quadros com um `.mcmeta` de animação,
   e no original ela vive no atlas dos itens, onde o jogo anima ela sozinho. Aqui ela é a textura da entidade, que
   não passa pelo atlas, e por isso o desenho avança um quadro por batida — que é o que o `.mcmeta` sem tempo
   declarado pede, e dá a mesma coisa na tela.

### Fatia 3 — a Afinidade (2026-09-27)

**A ideia mais bonita do ramo, e a que mais o separa de tudo o que já está portado: lançar feitiços muda quem
os lança.** Quem só atira fogo não vira um mago melhor — vira um mago *de fogo*, e vai perdendo o gelo pelo
caminho. A Afinidade não se escolhe em lugar nenhum: ela é o registro do que a pessoa fez.

São **dez**, mais a Afinidade nenhuma: Arcano, Água, Fogo, Terra, Ar, Relâmpago, Gelo, Natureza, Vida e Ender.

**A roda.** Cada uma tem quatro relações com as outras, e é a soma delas que faz a roda girar: a **oposta
direta**, que perde tanto quanto esta ganha; quatro **opostas maiores**, que perdem três quartos; duas
**menores**, que perdem metade; e duas **vizinhas**, que perdem um quarto — porque mesmo o que é parecido se
afasta.

**A conta não fecha em zero, e é de propósito.** Quem soma **um** numa Afinidade tira
1 + 4×0,75 + 2×0,5 + 2×0,25 = **5,5** das outras: o saldo é de **menos 4,5** por ponto ganho. Não é para
render; é para doer escolher. Há uma prova que guarda esse número, porque ele parece erro e não é.

**O tranco.** Quem chega aos 100 numa delas fica preso ali para sempre — o `isLocked` do original. É a única
coisa deste ramo que não tem volta.

**O retorno decrescente** é o que impede alguém de ganhar uma Afinidade numa tarde. Ele começa em **1,2**,
perde **0,3** por feitiço lançado (0,1 se for canalizado) e volta a subir **0,005** por batida. Quatro feitiços
seguidos zeram o ganho: a Afinidade vem de lançar ao longo de muitos dias, que é exatamente o que ela devia
significar.

**O deslocamento por feitiço é miúdo de propósito.** Cada Essência puxa para a Afinidade dela — Fogo e Gelo
0,01, Cura 0,05, Escavar 0,001, Luz nada —, e isso é multiplicado pelo retorno decrescente e por **cinco**. Uma
cura lançada com o retorno cheio move a Vida em **0,3** de 100.

**Um feitiço que falha não puxa nada**, e nem gasta o retorno decrescente: quem errou não aprendeu.

**A Afinidade de um feitiço se conta, não se escreve.** se Passa por todas as etapas somando uma marca para
cada Afinidade que cada Essência puxa, e a que aparecer mais vezes é a do feitiço — o `mainAffinityFor` do
original. É dela que saem a figura e a cor de um projétil, e é isso que faz um feitiço de fogo *parecer* um
feitiço de fogo sem ninguém ter escolhido.

**As onze figuras vieram do jar**, uma por Afinidade, com o número de quadros de cada uma: o estouro do Fogo
tem 24, o relâmpago 20, a pedra da Terra 16, o vento do Ar 10, a `lens_flare` da nenhuma e a planta da Natureza
13, o arcano 8 — e a brasa do Gelo, o brilho da Vida e a bola de Água são quadros soltos, sem animação. As três
cores que o original escreve à mão (Ender, Gelo, Vida) vieram junto; as outras sete saem brancas, porque a
figura delas já vem colorida.

**Desvios declarados.**

1. **As relações não são simétricas, e isso é do original.** O Arcano tem a Vida como oposta direta, mas a Vida
   tem o Ender; o Relâmpago tem o Gelo, e a Natureza também tem o Relâmpago, que já está tomado. Parece erro de
   digitação de 2014 e pode bem ser, mas mexer nisso mudaria a roda inteira. Fica como está, e há uma prova
   que fixa a assimetria para quem vier "consertar" ter de ler antes.
2. **O retorno decrescente sobe de 20 em 20 batidas, e não a cada uma.** No original o `tickDiminishingReturns`
   corre todo tique somando 0,005. Aqui corre junto com o relógio da mana, somando os mesmos 0,005 vinte vezes.
   Dá no mesmo, e poupa um laço por batida em cima de todo mundo que está no servidor.
3. **Os efeitos passivos de Afinidade entraram na fatia 4.** (Ficava aqui a nota de que faltavam.)
4. **O livro da Afinidade e o Orbe de Essência ficaram de fora**, porque são a mesa de escrever feitiços e o
   ritual de obelisco, que não estão portados.

### Fatia 4 — o que ter Afinidade faz de você (2026-09-27)

A fatia anterior trouxe a roda; esta traz a **recompensa** dela — e, o que é mais interessante, o **preço**.

**O que a Afinidade te dá.** O Relâmpago acima de 0,65 mais que dobra a velocidade e acima de 0,5 dá passada de
bloco inteiro. A Água acima de 0,5 nada depressa e acima de 0,4 repõe o fôlego sozinha. O Fogo cheio tira 60%
do dano de fogo, e o Ender tira 75% do dano mágico. A Vida devolve vida sozinha, 0,025 × profundidade por
batida. A Natureza cheia come do sol e **sobe parede**. O Ar acima de 0,5 pula mais alto e cai mais leve. O
Ender acima de 0,75 enxerga no escuro, e cheio faz **enderman não te encarar**. O Gelo agachado **congela a
água à frente dos pés** — e cheio endurece a lava, obsidiana na fonte e pedregulho na corrente.

**E o que ela te cobra.** Quase toda uma tem um preço, e é isso que faz a roda valer:

- quem é de **Natureza** anda 10% mais devagar, porque criou raiz;
- quem é de **Gelo** anda 10% mais devagar **fora do gelo** — sangue frio —, e basta um décimo de Gelo;
- quem é de **Fogo**, **Ender** ou **Relâmpago** perde **um quarto da vida** quando está molhado;
- quem é de **Água** perde um quarto quando arde ou está no Nether;
- quem é de **Ender** perde um quarto **sob o sol**;
- quem é de **Arcano** leva **10% a mais** de todo dano — é o preço de pagar 5% menos de mana;
- quem é de **Terra** afunda na água;
- quem é de **Relâmpago** acima de 0,25 **queima 100 de mana por batida** quando está molhado, e entre 0,5 e
  0,8 faz **dinamite por perto acender sozinha**;
- e quem é de **Vida** acima de 0,6 fica cego, faminto, lento e fraco **cada vez que mata** algo que estava
  vivo. Mortos-vivos não contam.

**A coisa mais bem pensada do original está nos intervalos.** Olhe as fraquezas: elas valem de **0,5 a 0,9**, e
**somem acima disso**. Não é engano — é o original dizendo que quem *chegou ao fim* de uma Afinidade passou da
parte que dói. É o que faz valer a pena ir até lá em vez de ficar no meio, e é o motivo de o tranco em 100
existir. Há uma prova que fixa esse intervalo.

**Os baldes.** A vida que volta (0,025 por batida) e a comida que o sol dá (0,02) não existem em pedaço menor
que um. O original guarda o resto num balde e, quando ele passa de um, dá um e tira um. É o que faz a
regeneração ser lenta e contínua em vez de pular de meio em meio coração.

**Onde isto entra no jogo de hoje.** O Forge de 2014 tinha `LivingUpdateEvent`, `LivingHurtEvent`,
`LivingFallEvent`, `LivingJumpEvent` e `LivingDeathEvent`. O jogo de hoje não tem nenhum, então são quatro
mixins: `actuallyHurt` (em `LivingEntity` e em `Player`, que tem o seu próprio), `jumpFromGround`,
`causeFallDamage` e `die`. O tique corre no `END_SERVER_TICK`, a cada batida — porque quase tudo aqui é sobre
o agora: se a pessoa está molhada, se está ao sol, se está agachada.

**Desvios declarados.**

1. **O nadar depressa usa a Graça do Golfinho do jogo**, no lugar do `BuffEffectSwiftSwim` do original, que é
   um efeito próprio do sistema de bênçãos do Ars Magica 2 — que não está portado. O efeito no jogo é o mesmo:
   nada-se mais depressa.
2. **A visão noturna do Ender está sempre ligada** acima de 0,75. No original ela tem um interruptor
   (`hasActivatedNightVision`) numa tecla própria, e o sistema de teclas do ramo não está portado.
3. **A bênção da Clareza ficou de fora.** No original, quem é de Arcano acima de 0,4 tem 5% de chance de ganhar
   Clareza a cada feitiço, e com ela o feitiço seguinte é **de graça**. Depende da lista de bênçãos.
4. **O movimento reverso na água ficou de fora.** O original reescreve o empurrão da corrente para quem é de
   Água acima de 0,5, o que lá dá para **nadar contra a corrente**. No jogo de hoje a correnteza funciona de
   outra maneira, e a Graça do Golfinho já cobre a intenção.
5. **A ponte de gelo olha um bloco à frente**, e não a lista de blocos do `GetHorizontalBlocksInFrontOfCharacter`
   do original, que varre um leque. O efeito é o mesmo em quem anda: a água à frente dos pés vira gelo.

### Fatia 5 — a área que fica (2026-09-27)

Até aqui todo feitiço acontecia **num instante**: o toque pega, o projétil bate, a área explode. Esta fatia
traz as três Formas que fazem a magia **ficar** — a `EntitySpellEffect` do original, que mora num lugar e
corre a frase vezes sem conta enquanto durar.

**A Zona** é um disco parado: dois blocos de raio, cinco segundos, e de segundo em segundo ela manda as
Essências do que sobrou da frase em quem estiver dentro **e** lança o que sobrou dali. É a Forma de quem quer
segurar um corredor, e é a mais cara das que atingem alguma coisa: **4,5×**. (Só as Contingências da fatia
6, que não atingem nada e só esperam, custam mais.)

**A Parede** é uma linha atravessada no caminho. Três blocos de raio para cada lado, e ela não é uma caixa: é
um **segmento de reta**, e cada bicho é medido contra ele. Só pega quem estiver a menos de 0,75 de bloco da
linha e a menos de 2 de altura — quem passa por cima ou por longe atravessa sem sentir nada. Ela nasce
atravessada ao olhar de quem a lançou, o que a põe *no* caminho e não *ao longo* dele.

**A Onda** é a mesma Parede **andando**: um bloco de raio, um segundo de vida, meio bloco por batida. Curta e
rápida de propósito — o que ela faz não é segurar um lugar, é **varrer** um. E é a única das três que mexe no
mundo: ela corre a frase em cada bloco por onde passa, e é por isso que uma Onda de Escavar abre uma vala e
uma Onda de Luz deixa um rastro aceso.

**As três são *principum*, e isso é a coisa mais interessante desta fatia.** Elas não fazem nada por si:
criam um lugar, e quem faz alguma coisa é a frase que vem a seguir. Elas **tiram a etapa delas** antes de
entregar à entidade, e por isso uma Zona seguida de Toque e Dano de Fogo é uma Zona que, de segundo em
segundo, corre "Toque + Dano de Fogo" no lugar onde está. Uma Zona sozinha no fim de uma frase é uma frase
incompleta.

**Dois números que parecem iguais e não são.** O raio da Zona **soma** e o da Parede **multiplica**. Não é
descuido do original: o modificador de Raio multiplica por 0,7, ou seja *encolhe*, e numa Parede ele aperta de
verdade enquanto numa Zona quase não se sente.

**E dois modificadores que não fazem o que o nome diz, na Onda.** A **Perfuração** não perfura nada: ela faz
a Onda **atravessar paredes**. E cada **Gravidade** posta faz a Onda descer **meio bloco por batida**, o que a
manda escada abaixo — é o `countModifiers` do original, um número que não se soma nem se multiplica, se conta.

**Uma manha do original que vale guardar:** quando a gravidade de uma Zona é **negativa** e não é a primeira
volta, ela lança o feitiço **um bloco abaixo** de si. É o que faz uma Zona que afunda ir deixando efeito no
chão por onde passa em vez de no ar onde está.

**Desvios declarados.**

1. **Uma Forma principum sozinha é recusada como malformada, de graça.** O original deixa lançar e cobra a
   mana de um feitiço que não faz nada, porque quem impediria de escrevê-lo é a Mesa de Inscrição — que não
   está portada. É uma armadilha a menos e nenhuma perda.
2. **Sem o jogador de mentira.** O original guarda um `DummyEntityPlayer` na entidade, para o feitiço ter de
   quem partir depois de quem o lançou já ter ido embora. Aqui, se quem lançou sumiu, a área morre — o que é
   mais simples e não deixa um jogador fantasma no mundo.
3. **As partículas entraram na fatia 9.** (Ficava aqui a nota de que faltavam.)
4. **A Chuva de Fogo e a Nevasca ficaram de fora.** A mesma entidade do original tem mais dois feitios
   (`TYPE_ROF` e `TYPE_BLIZ`), que não são Formas: são efeitos de itens e de rituais que não estão portados.

### Fatia 6 — a Corrente, o Facho, a Runa e as Contingências (2026-09-28)

As Formas que faltavam, menos uma. Com esta fatia o ramo tem **quatorze** das quinze do original.

**A Corrente** salta de um alvo para o seguinte: pega quem o mago está olhando e, dali, procura o vivo mais
perto que ainda não foi pego — e outra vez, até três. Cada salto alcança quatro blocos. Em cada um dos alvos
ela faz duas coisas: manda as Essências desta etapa **e** lança o que sobra da frase dali, o que faz de uma
Corrente seguida de Área uma Área em cada bicho da corrente. **Quem lançou nunca entra na corrente.**

**O Facho** é a única Forma que se **segura** em vez de se lançar. Enquanto o botão estiver preso ela aponta
para onde o mago olha e corre de novo a cada batida — mas só **fere de dez em dez**. É o que separa o facho de
um moedor: ele queima devagar e sem parar. Por isso custa a **décima parte** de uma Forma comum: o preço é por
batida, e ao fim de dez batidas somou o de um feitiço inteiro. Segurar um facho é gastar mana o tempo todo, e
ele para sozinho quando a mana acaba.

Isso obrigou a trazer o **canalizar** para o ramo: o item passou a ter `getUseDuration`, `onUseTick` e uma
Forma passou a poder saber **há quantas batidas** está sendo segurada. É o `useCount` do original, e só o
Facho olha para ele.

**A Runa** é um feitiço que **espera**. Ela fica desenhada no chão e não faz nada até alguém pisar — e então
corre a frase naquela pessoa e se gasta. Quantas vezes aguenta, di-lo o modificador de Repetições (uma só, sem
ele). **Quem a pôs não a dispara**, o que é o que a torna usável para guardar uma porta. Ela guarda a
Afinidade num estado do bloco, e é daí que sai a cor: vieram as **onze figuras** do original, uma por
Afinidade, e uma runa de fogo é vermelha sem ninguém ter escolhido.

**As cinco Contingências** são a única coisa do ramo que corre **sozinha**. Lançar uma não faz nada de
visível: ela escreve a frase dentro de quem a levou e ali fica, calada, até acontecer a coisa que espera —
**cair**, **levar dano**, **pegar fogo**, **ficar com um terço da vida** ou **morrer**. Uma Contingência de
Morte com uma Cura é uma segunda vida; uma de Queda com uma Pena Suave é um paraquedas que ninguém precisa
lembrar de abrir. Elas custam **dez vezes** uma Forma comum, que é o preço de um feitiço que espera.

**Duas coisas bem pensadas do original, nas Contingências.** Primeiro: **uma de cada vez** — quem já tem uma
guardada e lança outra perde a primeira, o que impede alguém de andar com cinco redes de segurança. Segundo:
ela **se gasta antes de correr**, e é isso que impede uma Contingência de Dano de entrar num laço sem fim
quando o feitiço dela fere quem a levava.

**E a de Queda não dispara quando se começa a cair.** Ela espera o **chão estar perto demais** para o que
falta cair: a distância até o chão tem de ser menor que oito vezes a velocidade de queda. É o que faz dela um
paraquedas e não um planador — deixa cair à vontade e só age no fim.

**Onde isto entra no jogo de hoje.** Três das cinco Contingências têm um momento certo (a pancada, a vida a
descer, a morte) e ficam num mixin em `actuallyHurt` e `die`. As outras duas são **estados** — estar ardendo,
estar caindo — e por isso é preciso olhar para elas a cada batida, num mixin em `tick`.

**Desvios declarados.**

1. **O Vínculo entrou na fatia 10.** (Ficava aqui a nota de que faltava.)
2. **A Runa não solta nada ao ser quebrada.** No original ela também não: ou dispara e se gasta, ou fica.
3. **O feitiço de uma Runa é lançado por quem pisa nela**, e não por um jogador de mentira de nível 99 como
   no original. Como o gasto de mana só sai de quem lança em modo de sobrevivência e a runa já foi paga
   quando foi desenhada, o efeito no jogo é o mesmo sem inventar um jogador.
4. **As partículas da Corrente entraram na fatia 9.**

### Fatia 7 — a Mesa de Inscrição (2026-09-28)

Até aqui os feitiços vinham prontos da aba do criativo: a gramática existia, mas não havia onde escrever com
ela. Esta fatia traz a bancada do arcanista — e, com ela, a coisa que faltava para o ramo ser **jogável**.

**As peças viraram itens.** Uma por palavra da gramática: 15 Formas, 5 Essências e 12 Modificadores, 32 ao
todo, cada uma com a figura do original. Elas não fazem nada na mão — não se lançam, não se comem, não se põem
no chão. O que elas servem é para ser escritas numa frase.

**A mesa lê a fila e escreve o feitiço.** Nove casas, da esquerda para a direita, porque é a *ordem* que
separa as etapas: cada Forma começa uma etapa nova, e o que vier depois dela — Essências e Modificadores — é
dessa etapa. É o `splitToStages` do original, e é por isso que escrever um feitiço é escrever uma lista e não
preencher um formulário.

**E ela diz o que está errado.** Esta é a parte que faz a mesa valer a pena. Até agora uma frase malformada só
dava em nada ao ser lançada; agora há quem diga **por quê**, em vermelho, com o nome da peça que a estragou.
É onde a gramática deixa de ser uma regra escondida no código e vira uma coisa que se aprende jogando.

**As quatro regras do `SpellValidator`, todas do original:**

1. toda etapa tem **uma Forma**;
2. a **última** etapa tem ao menos uma Essência — as do meio não precisam, porque quem faz alguma coisa é o
   fim da frase;
3. uma Forma **principum** não pode ser a última: ela cria um lugar e pede quem o use;
4. e uma Forma **terminus** só pode ser a última.

Uma frase **vazia** não é errada: é só uma frase que ainda não se escreveu, e a mesa fica calada.

**Desvios declarados.**

1. **As peças são itens, e no original são perícias.** Lá elas se aprendem numa árvore, e quem as sabe escreve
   com elas quantos feitiços quiser. A árvore não está portada, então elas viraram coisas — mas o espírito
   fica: **a mesa não gasta as peças**. Tirar o feitiço da casa de saída deixa a frase escrita, e a mesa
   escreve outro igual na hora. Até a árvore existir, a aba do criativo dá todas as 32.
2. **A tela é de casas, e não de arrastar e soltar.** A do original é uma tela própria, com as peças numa
   lista de onde se arrastam para a frase, e com *grupos de Forma* — um segundo lugar onde se guardam Formas
   para reusar. Aqui a frase é uma fila de casas de inventário, que é o que faz sentido quando as peças são
   itens, e não há grupos de Forma.
3. **O fundo da tela é desenhado com retângulos**, e não com uma folha de figura, pela mesma razão: a folha do
   original é daquela tela de arrastar e soltar.
4. **Sem escrever o feitiço num livro.** O original tem um `writeRecipeAndDataToBook` que põe a receita num
   livro-e-pena para se passar a outra pessoa — com um livro, papel, pena e tinta nas quatro casas da mesa.
   Como aqui as peças são itens que se podem dar a alguém, a receita já se passa de mão em mão; o livro fica
   para quando a árvore de perícias entrar e as peças deixarem de ser coisas.
5. **A Mesa ganhou o modelo do original na fatia 12.** Continua sendo um bloco só, e não dois.

### Fatia 8 — a árvore de perícias (2026-09-28)

É o que dava **curva** ao ramo, e o que faltava para ele estar fechado. Sem ela um arcanista nascia sabendo
tudo; com ela, começa sabendo três Formas e compra o resto com o que aprende lançando.

**Três ramos e três cores.** Ofensa, Defesa e Utilidade; e o ponto **azul**, que se ganha até o nível vinte, o
**verde**, do vinte ao quarenta, e o **vermelho**, do quarenta ao cinquenta. Uma perícia vermelha não se
compra cedo por mais pontos azuis que se tenha, e é isso que faz o ramo ter começo, meio e fim.

**se Ganha um ponto a cada dois níveis, e só até o cinquenta.** Depois disso o nível ainda sobe — e ainda
enche a mana e apressa o relógio — mas não compra mais nada. São **25 pontos** ao todo (3 azuis de começo,
mais 10 azuis, 10 verdes e 5 vermelhos) para **32 perícias**: **não dá para ter tudo**, e é de propósito. Há
uma prova que guarda esse número.

**A experiência vem de lançar.** Cada Essência lançada dá 0,05 de experiência mágica, multiplicada pelo
retorno decrescente — o mesmo que rege a Afinidade. Quem despeja feitiços seguidos não aprende nada. E a conta
do que falta para o nível seguinte é a do original, `(nível × 0,25)^1,5`: devagar no começo, uma parede no
fim.

**O Óculus** é onde os pontos viram peças. Um pedestal, e a primeira coisa que um arcanista constrói depois da
Mesa de Inscrição.

**As posições, os ramos, as cores e a forma do grafo são as do original**, lidas do jar: 31 das 32 peças deste
porte estão na árvore do Ars Magica 2, com coordenada, ramo, cor e pré-requisitos.

**Desvios declarados.**

1. **Os pré-requisitos foram refeitos, e esta é a deviação que mais importa.** A árvore do original tem **120**
   perícias, e os caminhos entre as minhas 32 passam por **89** que este porte não tem — se eu copiasse os
   pré-requisitos ao pé da letra, quase nada seria comprável. Então cada perícia pede os **ancestrais portados
   mais próximos**, subindo o grafo do original até achá-los. A *forma* da árvore fica: Projétil → Fogo/Gelo →
   Área → Facho → Dano/Onda; Autoconjuração → Cura → Curar; Toque → Escavar → Luz → Corrente/Alcance/Raio.
   **Há uma prova que confere que toda perícia se alcança de uma raiz** — sem ela, uma peça inalcançável
   passaria despercebida a quem jogasse.
2. **Os Alvos Não Sólidos não estão na árvore do original.** Aqui são raiz e custam um azul, porque sem eles o
   Projétil e a Onda não sabem pegar em água.
3. **Saber é receber.** Comprar uma perícia dá a **peça como item**, porque neste porte as peças são itens (ver
   a fatia 7). No original saber já basta, porque lá elas não são coisas.
4. **Os pontos não se guardam: contam-se do nível.** O original guarda quantos pontos a pessoa tem; aqui
   guarda-se quantos já se **gastaram**, e os que sobram saem da conta do nível. Dá no mesmo e não há como os
   perder num mundo que se corrompeu.
5. **A tela tem três abas e não seis**, e o quadro é encolhido para caber inteiro sem rolar. As outras três
   abas do original (talentos, familiares, afinidade) são sistemas que não estão portados, e as 120 perícias
   dele não caberiam numa tela — as 32 destas cabem.
6. **A experiência que sobra ao subir de nível se perde — e é um erro do original que este porte mantém.** O
   `addMagicXP` zera a experiência ao subir, sem guardar o excesso e sem tornar a olhar: quem ganhasse de uma
   vez o bastante para dois níveis só subiria um. Quase nunca se nota, porque a experiência chega de cinco em
   cinco centésimos, mas nos primeiros níveis — em que o que falta é um oitavo de ponto — chega a perder
   metade do que se ganhou. Fica assim porque mexer nisso mudaria o ritmo de todo o começo do ramo, e há uma
   prova que fixa o comportamento.
7. **O Óculus é um cubo com figuras do jogo.** O original tem um modelo próprio com textura de 64×32, que não
   é de face de bloco; usá-la num cubo ficaria torta. É fatia própria, junto com os outros modelos do ramo.

### Fatia 9 — o pó que os feitiços deixam no ar (2026-09-28)

As Formas funcionavam e não se viam. Esta fatia dá cara a elas.

**Um mote, e a cor vem da Afinidade.** É a mesma partícula para tudo — um ponto de luz que sobe devagar e
apaga em vinte batidas —, e a cor viaja nela, como a do efeito de poção do jogo. É o que faz uma Zona de fogo
ser vermelha e uma Parede de gelo ser azul **sem ninguém ter escolhido**: a cor sai da frase.

**Cada Forma põe o pó onde lhe convém**, com os ritmos do original: a **Zona** põe quatro motes de duas em
duas batidas, girando dez graus de cada vez; a **Parede** e a **Onda** põem um a cada meio bloco da linha,
com um bloco de tremor — o `addRandomOffset(1,1,1)`, que é o que faz a parede parecer uma cortina e não um
fio; a **Corrente** põe uma fila bem junta de um elo ao seguinte; e o **Projétil** deixa um rastro.

**Um erro de verdade, achado por esta fatia.** A entidade da área — Zona, Parede e Onda — **não tinha
desenhista registrado**, e o cliente quebrava com um `NullPointerException` assim que uma nascia. O jogo pede
um desenhista a *toda* entidade que entra no mundo, mesmo às que não se veem. Isso estava em pé desde a fatia
5 e passou por três fatias sem ninguém dar por isso, porque **as provas de servidor não desenham**. Foi a
prova de tela nova que pegou. Entrou um `NoopRenderer`.

**Desvios declarados.**

1. **Uma figura de partícula só, e não onze.** O original tem uma por Afinidade — a `lens_flare`, a
   `explosion_2`, a `ember` e por aí. Aqui é o `sparkle` dele para todas, e quem separa uma Afinidade da
   outra é a **cor**. As onze figuras estão no porte (o Projétil as usa), mas como partícula elas dariam onze
   folhas de animação para pouca diferença na tela.
2. **O tamanho do mote foi afinado pelo que se vê.** O original diz `setParticleScale(0.15F)`, mas esse número
   é da escala do motor de partículas *dele*; no do jogo de hoje, o mesmo 0,15 dá um ponto quase invisível.
   Portar um número de aparência entre dois motores diferentes pelo valor escrito seria fidelidade falsa — o
   que se porta é o que aparece.
3. **O facho da Corrente é uma fila de motes.** O original desenha um facho de verdade — uma tira contínua
   entre os dois pontos, e um *raio* se a Afinidade for a do Relâmpago. A fila dá a mesma leitura e não
   precisa de um desenhista próprio.
4. **A Zona não orbita.** No original os quatro motes dela dão voltas em torno do centro enquanto sobem
   (`ParticleOrbitPoint`). Aqui eles nascem já girados e sobem a direito: o anel gira porque o ângulo sai da
   idade, e não porque cada mote ande em volta.

### Fatia 10 — o Vínculo, e as quinze Formas fechadas (2026-09-29)

A última. Com ela o ramo tem **as quinze Formas** do Ars Magica 2.

**O Vínculo não lança nada.** O que ele faz é trocar o feitiço na mão por uma **ferramenta** — e essa
ferramenta custa mana **a cada batida** para se manter, e volta a ser o feitiço quando a mana acaba. É a ideia
mais bonita do ramo depois da Afinidade: uma ferramenta que só existe enquanto se pode pagar por ela.

**Em troca, ela nunca se gasta.** O original a conserta um ponto por batida enquanto a mantém. Uma picareta
vinculada de diamante não quebra nunca — mas come **um ponto de mana por batida**, que são **vinte por
segundo**, e ninguém a carrega sem pensar.

**O preço depende do metal**, e são os três números do `IBoundItem`: **0,1** por batida para a pedra (a
enxada), **0,4** para o ferro (a pá) e **1,0** para o diamante (a picareta, o machado e a espada). É a única
escolha que a ferramenta dá: quanto ela vale contra quanto ela custa.

**O feitiço vai dentro da ferramenta**, e é por isso que desfazer e refazer não perde a frase.

**Desvios declarados.**

1. **Cada ferramenta é a sua própria Forma, e no original há uma só.** Lá o Vínculo é uma perícia única, e
   qual ferramenta ele faz sai de um **número guardado no feitiço**, escolhido na Mesa de Inscrição. Este
   porte não tem números guardados nas peças — cada peça é um item —, então são **cinco Formas**, uma por
   ferramenta. Escolher a peça é escolher a ferramenta, que é como tudo o mais funciona aqui. Na árvore, a
   primeira fica exatamente onde o Vínculo do original ficava (275, 210, azul, depois da Luz) e as outras
   quatro abrem em leque a partir dela.
2. **O arco vinculado entrou na fatia 11.** (Ficava aqui a nota de que faltava.)
3. **A árvore ficou com 37 perícias para 25 pontos.** Aperta mais do que antes, e é o que se queria: escolher
   o que deixar de lado é o ramo inteiro.

### Fatia 11 — a aba do livro, e o arco (2026-09-30)

**O ramo existia e ninguém dava por ele.** Esta foi a lacuna real que sobrou, e não os obeliscos: o Ars Arcana
tinha dez fatias de código, 775 provas, e **zero páginas no Thaumonomicon**. A categoria `ARCANA` estava
declarada numa constante e nunca era registrada. Quem jogasse não tinha caminho nenhum até o ramo — as peças
estavam na aba do criativo e mais nada.

**Sete pesquisas, na ordem do que se faz**, que é a única ordem que ensina alguma coisa:

1. **A Gramática da Magia** — o degrau de entrada: um feitiço é uma frase, não uma receita.
2. **O Óculus** — onde se vê o que se pode aprender, e onde os pontos viram peças.
3. **A Mesa de Inscrição** — onde as peças viram frase, e onde a mesa diz o que está errado.
4. **A Mana e o Desgaste** — o cano e não a água, e por que o nível importa.
5. **A Afinidade** — lançar muda quem lança, e o que isso dá e cobra.
6. **As Formas** — as quinze maneiras de um feitiço entrar no mundo.
7. **O Vínculo** — o fim da estrada.

O texto é o do caderno de quem escreve o Thaumonomicon: alguém que topou com um ofício que não é o dele e
acha a gramática elegante e um pouco incômoda. Ele explica **as contas** — a fórmula da mana, os 38% de
desgaste, a roda de Afinidade que perde 5,5 por ponto ganho, os 25 pontos para 38 perícias — porque este é um
ramo em que os números *são* o desenho.

**E o arco vinculado**, que fecha os seis tipos do original. Ele é de **ferro** — quatro décimos por batida,
como a pá — e não de diamante, o que é uma escolha do original que faz sentido: um arco que nunca se gasta já
vale muito por si. Ele é uma classe à parte das outras cinco porque um arco do jogo tem de herdar o `BowItem`
para saber puxar a corda; o que ele repete das outras é só o relógio da mana.

**Sobre os Rituais de Obelisco, e por que eles não entram.**

Ficou escrito em fatias anteriores que eles faltavam. Lendo o original com cuidado, eles **não são uma fatia
deste ramo**: o Obelisco é uma fonte de energia (`PowerNodeRegistry`, `PowerTypes.NEUTRAL`) para a maquinaria
do Ars Magica 2, e os rituais — que são feitiços lançados sobre um padrão de blocos com reagentes no chão —
produzem justamente essa maquinaria: `blackAurem`, `inertSpawner`, `tarmaRoot`.

Este porte trouxe **o sistema de feitiços** do AM2. A maquinaria e a rede de energia são a outra metade do mod,
e portá-las seria um trabalho do tamanho de um mod inteiro — com o detalhe de que, sozinhos, o Obelisco não
teria o que alimentar e os rituais não teriam o que construir. Fica declarado como **escolha de escopo**, e
não como pendência.

### Fatia 12 — os modelos do original (2026-09-30)

O Óculus e a Mesa de Inscrição eram cubos. Agora são o que o original desenhou.

**As caixas vêm dos modelos Techne do Ars Magica 2**, convertidas uma a uma: **28** no Óculus
(`ModelOcculus` — o pedestal, as garras e o olho) e **15** na Mesa (`ModelInscriptionTableLeft` — a
escrivaninha, o livro e o pano).

**A conta da conversão** é a mesma de todo modelo de bicho montado num bloco, e vale escrever porque ela
volta sempre: o desenhista do original põe o modelo em (0,5; 1,5; 0,5) e gira **180° em Z**, porque o Y do
Techne cresce para baixo. Depois disso, para uma caixa em `ponto + caixa`:

```
X = 8 − (ponto.x + caixa.x)   e ela cresce para −X
Y = 24 − (ponto.y + caixa.y)  e cresce para −Y
Z = 8 + (ponto.z + caixa.z)   e cresce para +Z
```

**Dois erros que só a foto acharia**, e que nenhuma das 775 provas de servidor pegaria:

1. **O modelo do Óculus não carregava**, e o bloco saía como o cubo roxo de textura perdida. A causa é do
   original: a caixa `Stand4` tem um mapa de figura que **passa da borda da folha** — chega a `u=65` numa
   folha de 64. No 1.7.10 a figura dava a volta e mostrava uma lasca da outra ponta; o jogo de hoje recusa o
   modelo inteiro com um `Cannot compute translucency out of bounds`. As coordenadas ficam **presas na
   borda**: perde-se um pixel no fundo do pilar do meio e se ganha um modelo que carrega.
2. **As caixas giradas do livro não cabem num modelo JSON.** Ele só aceita **um eixo** e **cinco ângulos**
   (0, ±22,5, ±45), e o original gira em radianos quaisquer. As duas da Mesa foram encaixadas na mais
   próxima: −13,6° virou −22,5° e 29° virou 22,5°. As páginas do livro ficam um pouco mais abertas do que no
   original.

**Desvios declarados.**

1. **A Mesa continua sendo um bloco só.** No original ela são **dois** — `Left` e `Right`, cada um preenchendo
   o seu bloco. Aqui se usa a metade esquerda, que é a da escrivaninha com o livro e já se lê como uma mesa
   inteira. Fazer dela um bloco duplo mudaria o bloco, a receita e o menu.
2. **Os dois modelos passam da altura do bloco** — o olho do Óculus chega a `y=25` e a Mesa a `y=20`. É o que
   o original faz, e o jogo de hoje aceita até 32.

### Fatia 13 — comprar uma perícia, e ver o quadro (2026-09-30)

Doze fatias e **a compra de perícia nunca tinha sido provada**. As provas da árvore conferiam a *conta* — se
o ponto chega, se o pré-requisito está sabido — e nenhuma delas passava pelo **caminho**: o botão da tela, o
número que ele manda, o servidor que prova tudo outra vez, a peça que chega à mochila. Agora passam cinco.

E a tela da árvore **nunca tinha sido fotografada**. Três fotos depois, três defeitos:

1. **Os textos saíam fora do painel.** O nome das abas, o nível e os pontos ficavam pendurados por cima e à
   esquerda da janela, no vazio. A causa é que no jogo de hoje o `extractBackground` desenha em pixel de
   **tela** e o `extractLabels` em pixel do **painel** — e o código subtraía o canto em quem não devia.
2. **O ramo de Utilidade transbordava por baixo.** O quadro do original desce até `y=524` nesse ramo e até
   `y=360` no de Defesa; com um encolhimento fixo, ou um ficava minúsculo ou o outro saía da tela. Agora
   cada ramo é **medido e encaixado**: encolhe-se até caber, nunca mais do que 0,42, e se centra o que sobra.
3. **E o pior: o nível e a mana nunca chegavam à máquina de quem joga.** O Óculus mostrava *Nível 0* e *três
   pontos azuis* a quem estava no nível cinquenta, e por isso mostrava também **menos um** ponto verde — a
   conta de pontos gastos vinha do servidor e a de pontos ganhos vinha de um nível que era zero.

**A causa do terceiro é uma armadilha que vale escrever**, porque ela não se vê lendo o código: um anexo de
jogador (`AttachmentType`) só se **registra quando a classe carrega**. A `SkillData` carregava no arranque,
porque o `Arcana.init()` a chamava; a `Mana` e a `AffinityData` não — elas só carregavam quando alguém as
usava, e do lado do cliente isso é **depois de entrar no mundo**. O pacote de sincronia chegava a um nome que
o cliente ainda não conhecia e **caía no chão sem um pio**: sem erro, sem aviso, sem nada. As três agora se
registram no `init`, e as três vão pela rede com `syncWith`.

A prova de tela passou a **exigir** o que o servidor sabe: se o nível não chegar ao cliente, ela quebra.

### Fatia 14 — os modificadores do céu, a sorte e a seda (2026-09-30)

Quatro modificadores que faltavam, e uma coisa que o porte não sabia fazer.

**O que ele não sabia fazer** era deixar um modificador **olhar o mundo**. Todos os outros valem sempre o
mesmo — o Alcance soma quatro blocos e é o fim da conversa —, mas o **Solar** e o **Lunar** só existem por
causa da hora e da fase da lua. A conta do feitiço (`Spell.mul` e `Spell.add`) passou a levar o mundo, e os
25 lugares que a chamam passaram a passá-lo.

**As contas do Solar e do Lunar são as do original, com as esquisitices que ele tem**, e duas merecem ser
ditas porque um leitor honesto acharia que são erro do porte:

1. O original converte o ângulo de **radianos para graus** e depois entrega esses graus ao seno, que espera
   radianos. O que sai não é a onda suave que o nome promete: é uma coisa que salta. Fica.
2. O Solar pergunta se a hora está **depois de 23500 e antes de 12500** — o que nenhum número é. A pergunta
   é sempre não, e por isso o alcance e o raio dele valem **sempre dois**. É um engano de 2014 do Ars Magica
   2, e consertá-lo mudaria o feitiço de quem joga. Fica, e fica escrito.

**A Prosperidade e o Toque de Pena** não mexem no feitiço: mexem no que **cai** do que ele quebra. O original
encanta com elas a ferramenta invisível com que o Escavar colhe o bloco — uma Prosperidade é Fortuna I, duas
é Fortuna II, e a seda manda na sorte. O Escavar passou a colher assim.

**E entrou a quarta cor de ponto: a prateada.** Ela não se ganha subindo de nível — se ganha **descobrindo**.
Lançar um feitiço com a combinação certa de peças destranca uma das dez perícias prateadas do original e dá o
ponto para a comprar. A cor, a conta e o lugar delas no quadro entram agora; **quem as destranca é uma fatia
própria**, porque as combinações pedem peças que ainda não estão portadas. Até lá a Prosperidade aparece no
quadro e não se compra, que é exatamente o que ela é no original antes de se descobrir o segredo.

### Fatia 15 — os danos, e o fator do nível (2026-09-30)

Nove essências novas, e **a conta mais importante do ramo, que faltava**.

**O fator do nível.** Todo dano de feitiço do Ars Magica 2 passa pelo `modifyDamage`, e ele multiplica: quem
está no nível **zero fere metade** do que está escrito na peça, quem chegou ao **vinte fere o escrito**, e
quem chegou ao **99 fere o dobro**. Isto não estava portado — o dano saía sempre igual, e subir de nível não
mudava nada para quem já tinha as peças. Agora todo dano passa pelo `Essences.fere`, e é lá que a conta mora.

**Os tipos de dano do ramo.** O original tem os seus: quem morre de um feitiço de gelo não morreu afogado, e
o texto do chat é outro. Entraram seis — fogo, gelo, raio, vento, luz e afogamento — e todos são
**absolutos**, como no original: nem armadura, nem encanto, nem poção os diminuem.

**As nove essências.** Dano Físico (8, e o mais barato), Dano Arcano (6), Dano de Raio (12, o mais alto),
Afogar (12, e não pega em morto-vivo nem em golem de ferro), Drenar Vida, Vida por Mana, Drenar Mana,
Ignição e Derreter Armadura.

**Dois enganos do original mantidos, e declarados:**

1. **Drenar Vida cobra o feitiço a quem atira em morto-vivo.** Ele devolve `true` sem ferir e sem curar, o
   que quer dizer que a mana sai na mesma. Fica.
2. **Derreter Armadura não está no quadro de perícias.** O original registra a peça, dá-lhe figura e nome, e
   **se esquece de a pôr na árvore** — quem joga o original nunca a pode comprar. O porte guarda o engano, e
   a prova `everyPartIsInTheTree` tem agora uma lista de órfãs onde ele fica escrito.

**E a corrente do quadro voltou a ser a do original.** Enquanto faltavam os danos, o Fogo e o Gelo pendiam do
Projétil porque o antepassado verdadeiro deles não existia. Agora existe: o **Dano Físico** é a raiz, o Fogo
e o Arcano vêm dele, e o Raio, o Gelo e o Afogar vêm desses. A Área voltou a pedir **os cinco danos**.

### Fatia 16 — os efeitos, as bênçãos, e o quadro inteiro do original (2026-09-30)

A maior fatia do ramo. Entram **24 efeitos**, **29 essências** que os põem, **dois modificadores**, e o
**quadro de perícias do original, inteiro e gerado**.

**Os 24 efeitos.** No Ars Magica 2 cada um é uma classe `BuffEffect` com um `applyEffect` e um `stopEffect`,
e quase todos são **marcos vazios**: o que eles fazem está escrito no `AMEventHandler`, num punhado de
métodos gigantes que olham para todos de uma vez. Aqui o efeito é o que o jogo de hoje chama de efeito, e o
que ele faz está **junto dele**, nos quatro lugares por onde um efeito pode mexer em alguém — a **batida**, o
**dano**, a **queda** e o **pulo**.

O original tem mais cinco — a Agilidade, a Clareza, a Regeneração de Mana, o Aumento de Mana e a Redução de
Desgaste. Quem os dá são as **máquinas** dele: o Obelisco, o Prisma Celeste, as garrafas. Essa metade não é
deste ramo, e um efeito que ninguém pode ganhar é peso morto: ficam de fora, declarados.

**Os ícones vieram das duas folhas do original**, recortados com a conta do 1.7.10: o jogo daquela época
guardava os ícones de poção a partir de `y=198`, em quadrados de 18 por 18, e o índice sai de
`setIconIndex(coluna, linha)`. Vinte e quatro quadrados, conferidos numa foto — porque se a conta estivesse
errada por um quadrado, **todos** sairiam trocados e as provas de servidor continuariam verdes.

**O ícone do Embaralhar de Sinapses sai em branco**, e é assim no original: o `BuffList` manda buscá-lo à
linha 1, coluna 7 da segunda folha, e essa casa está vazia. Quem joga o Ars Magica 2 vê um quadrado vazio, e
quem jogar este vê o mesmo. Está na foto.

**As bênçãos são uma só peça de código.** Vinte e tal essências do original são a mesma coisa escrita vinte e
tal vezes: contam a duração — **600 batidas**, que a Duração multiplica —, contam quantos **Poderes de
Bênção** há na frase, e põem o efeito com essa duração e esse grau. Aqui é um `record` chamado `Bênção`, e
cada essência é uma linha.

**Dois números do original que valem ser ditos:**

1. **A Lentidão e o Congelar são o mesmo efeito por preços diferentes.** Os dois põem o `BuffEffectFrostSlowed`
   com a mesma duração e o mesmo grau; o que muda é o preço — **80** contra **29** — e a Afinidade que cada um
   puxa. Não é engano de leitura, e fica.
2. **Os degraus da Pressa não são uma escada de passos iguais**: 0,2, depois 0,45, depois 0,9. Por isso eles
   não podem ser um modificador de atributo comum, que o jogo multiplicaria pelo grau — o efeito põe o seu à
   mão, no gancho por onde o jogo aplica os modificadores.

**E o quadro de perícias passou a ser gerado do original.** Até aqui ele era escrito à mão, fatia a fatia, e
as correntes iam sendo remendadas à medida que as peças chegavam. Agora as 79 linhas saem do
`SkillTreeManager` do Ars Magica 2, lidas uma a uma: cada perícia no ramo, na cor e no lugar em que ele a pôs.

O que muda é o que **não está portado**. O original tem 120 perícias em quatro ramos; o quarto — os
**Talentos**, com a regeneração de mana, as faixas de mago e os ganhos de afinidade — não é feito de peças de
feitiço e não é deste ramo. E dos outros três falta o que ainda não foi portado. Quando uma perícia
desaparece assim, os filhos dela passam a pender do **antepassado portado mais próximo**.

**Três peças são órfãs, e são órfãs no original.** O **Derreter Armadura**, a **Náusea** e o **Embaralhar
Sinapses** têm peça, figura, nome e receita, e **não estão em ramo nenhum** da árvore do Ars Magica 2 — quem
o joga nunca as pode comprar. O porte guarda o engano, e a prova `everyPartIsInTheTree` é onde ele fica
escrito.

### Fatia 17 — as que empurram (2026-09-30)

Sete essências e o modificador que faltava. Nenhuma delas fere ninguém: o que elas fazem é **mexer em quem já
lá está** — e num jogo em que se cai de alturas e se morre disso, empurrar é uma arma tão boa como outra.

**Arremesso** (1,05 para cima), **Empurrão** (1,5 na horizontal e 0,325 para cima, na linha que sai de quem
lança), **Repelir**, **Telecinese**, **Atrair**, **Acelerar** e **Desarmar**. E a **Velocidade Acrescentada**,
que soma meio ao empurrão — não ao que voa, que isso é a Velocidade.

**O Repelir tem força fixa, e a conta do original engana.** Ele divide a linha entre os dois por 2,5 **e pela
distância** — e dividir uma linha pelo seu próprio comprimento dá uma linha de comprimento um. O empurrão sai
sempre com **0,4**, e o que muda é só o rumo. Há ainda um décimo somado à distância antes da divisão, e por
causa dele quem está *colado* é empurrado um pouco **menos** do que quem está a dois blocos. Apontado a quem o
lança, ele pega tudo o que estiver a **dois blocos**.

**Três coisas do original mantidas, e declaradas:**

1. **O Atrair e a Telecinese são a mesma conta.** Os dois chamam o `doTK_Extrapolated` com os mesmos números
   — dezesseis blocos de distância, três de altura, 0,15 de velocidade, e nada sobe. O que muda é o preço,
   que é menos de metade, e a Afinidade. Ficam os dois.
2. **O Acelerar quase não faz nada.** O original multiplica por 1,6 a velocidade de passo da inteligência do
   bicho — o número que o jogo recalcula a cada batida. Num bicho ele salta uma vez; **numa pessoa não faz
   nada de nada**. Seis de mana, a essência mais barata do ramo. Fica como está: consertá-lo seria inventar
   um feitiço que o Ars Magica 2 não tem.
3. **A arma que o Desarmar faz cair cai gasta** — entre 80 e 99 por cento da durabilidade —, para desarmar
   esqueletos não ser uma maneira de ganhar arcos.

### Fatia 18 — as que deslocam (2026-09-30)

Sete essências, e um lugar novo onde um arcanista guarda coisa: a **Marca**.

**Piscar** (doze blocos à frente), **Teleporte Aleatório** (nove de lado, que o Alcance multiplica),
**Marca**, **Chamado**, **Trocar de Lugar**, **Intervenção Divina** (leva a casa) e **Intervenção do Fim**
(leva ao Nether).

**O Piscar não atira ninguém: ele procura.** Parte da distância cheia e vai descendo um bloco de cada vez até
achar um lugar onde caibam duas casas de ar — e em cada distância prova **doze** lugares: os quatro cantos em
roda do ponto, e os mesmos um acima e um abaixo. É por isso que um Piscar contra uma parede põe a pessoa
**encostada** à parede, e não dentro dela.

**A Marca e o Chamado são um par e não valem nada um sem o outro.** Se marca **um lugar só** — marcar outra
vez apaga o anterior — e o Chamado **não atravessa mundos**: uma marca feita na superfície não traz ninguém
do Nether, e o original recusa dizendo porquê. O lugar marcado mora num anexo próprio, que vai pela rede como
a mana e as afinidades.

**A Distorção Astral prende tudo isto**: com ela posta, nenhuma das sete desloca ninguém — e todas **cobram
na mesma**, que é o que o original faz ao devolver verdadeiro sem fazer nada.

### Fatia 19 — as que mexem no mundo (2026-09-30)

Oito essências de lavoura e de pedreira: **Criar Água** (e enche o caldeirão, se for num caldeirão),
**Arar**, **Plantar**, **Colher**, **Crescer** (que é o pó de osso), **Forja** (que cozinha o bloco como um
forno, e faz do gelo água), **Outono do Mago** (derruba as folhas a dois blocos de raio) e **Seca**.

**A lista da Seca é a do original, nesta ordem**: a flor e a erva alta viram erva morta; a grama, o micélio,
o arenito e a terra viram areia; a pedra vira pedregulho; o tijolo de pedra racha; e a água desaparece.

**Duas ficam de fora, e é a mesma razão da Cor.** O **Colocar Bloco** e a **Apropriação** guardam um bloco
**dentro do feitiço** — o original o escreve no NBT da varinha na hora de inscrever. Este porte guarda os
feitiços por nome de peça e não tem ainda onde pôr um dado desses. É fatia própria, com a Cor.

### Fatia 20 — as que mexem no céu (2026-09-30)

Quatro, e são as mais caras do ramo inteiro.

**Afastar a Chuva** (750 de mana, e **três décimos** de Afinidade da Água por lançamento — trinta vezes o que
um dano puxa), **Tempestade** (quinze de mana: com tempo bom começa a chover, e com a chuva já forte há uma
em cinco de cair um raio num monstro a cinquenta blocos), **Luz do Dia** e **Anoitecer**.

**As duas do relógio custam vinte e cinco mil de mana cada uma** — mais do que qualquer arcanista tem antes
do nível alto, e mais do que qualquer outra essência por larga margem. Mexer no céu é caro, e é de propósito.

**Desvio declarado: o relógio.** O original escreve a batida à mão — o dia em curso vezes 24000 para o
amanhecer, e mais 13250 para o anoitecer. O jogo de hoje já não deixa mexer no relógio assim: ele tem
**marcos**, e quem quer o amanhecer pede o marco do amanhecer. Dá no mesmo lugar do céu.

**E o tempo mora noutro lugar.** No 1.7.10 a chuva estava no próprio mundo; hoje está num guardado à parte,
o `WeatherData`. A conta é a mesma, o lugar é outro.

### Fatia 21 — a Nevasca e a Chuva de Fogo (2026-09-30)

Duas essências que não são essências: são **um feitiço inteiro numa peça só**. Postas numa frase, criam no
lugar uma área que fere por si, a cada batida, e vai deixando **neve** ou **fogo** no chão. É por isso que as
duas são perícias prateadas no original — não se compram, se descobrem.

A entidade de área ganhou duas famílias novas por causa delas. As três de antes — a Zona, a Parede e a Onda —
acham quem está lá e lhe passam **o resto da frase**; estas duas não passam nada: fazem o que fazem e mais
nada. E fazem uma coisa que nenhuma outra faz: **desfazem o empurrão** que a pancada daria, senão quem
estivesse dentro saltaria para fora na primeira batida e a nevasca não seria nevasca nenhuma.

**Os números são os do original**: a Nevasca fere **um** por batida e prende com o Gelado no terceiro grau —
o mais forte que ele tem —, a Chuva de Fogo fere **três quartos**, e as duas deixam alguma coisa no chão em
**duas batidas de cada dez**. O raio da Chuva de Fogo é somado ao Raio e depois **dividido por dois mais um**:
ela é sempre mais apertada do que uma Nevasca com os mesmos modificadores.

**E as duas recusam se já houver uma igual a dez blocos.** Duas nevascas no mesmo lugar seriam o dobro do
dano pelo dobro do preço, e o original não quer isso.

**O que se vê delas** são vinte flocos por batida numa e dez chamas na outra, caindo de **dez blocos acima**.
A entidade é invisível, como a da Zona — sem as partículas, as duas seriam dois círculos de nada. Está na
foto, que é o único lugar onde isso se podia ver.

### Fatia 22 — os segredos (2026-09-30)

**Dez perícias do original não se compram com nível nenhum.** Elas estão no quadro, de prateado, e ficam lá
trancadas até alguém **tropeçar nelas** — lançando um feitiço que tenha, **numa mesma etapa**, a combinação
certa de peças. Aí a perícia é aprendida na hora e o ponto prateado aparece para a pagar.

Ninguém diz a quem joga que elas existem. Não há dica, não há página no livro, não há receita: há um feitiço
que alguém escreveu por outra razão e que, ao ser lançado, abre uma porta.

**As combinações são as do original**, e valem ser lidas: a **Nevasca** sai de uma Tempestade com Dano Gélido,
Congelar e Dano; a **Chuva de Fogo**, da mesma Tempestade com Dano de Fogo e Ignição; o **Poder de Bênção**,
de um feitiço que junte **cinco bênçãos** de uma vez; a **Prosperidade**, de um Escavar com Toque de Pena e
Força de Mineração. Nenhuma é um acaso: são feitiços que alguém escreveria de propósito, se tivesse a ideia.

**E há o outro lado**: um feitiço que leve uma dessas **essências** sem que quem o lança a tenha descoberto
**não sai** — não falha, nem começa. Um **modificador** de segredo não tranca nada, e é o original que faz
essa distinção: um modificador não faz nada sozinho, e deixá-lo passar não estraga a surpresa.

**Oito dos dez entram agora.** Faltam os dois cuja peça ainda não está portada — a **Estrela Cadente**, que
pede uma entidade própria, e o **Elo de Mana**, que pede mexer no cano da mana.

### Fatia 23 — o Canal (2026-09-30)

A Forma que faltava das dezessete do original, e tinha passado despercebida porque o nome dela parece o de
outra: o **Canal** não é o Facho. Ele é a **Autoconjuração que se segura** — corre a etapa em quem a lança, de
**dez em dez batidas**, enquanto o botão estiver apertado.

**Com a Telecinese ou o Atrair na frase, ele corre a cada batida.** É a exceção do original, e faz sentido:
as duas são essências que puxam coisas devagar, e de dez em dez batidas quase não se notariam.

### Fatia 24 — a Estrela Cadente (2026-09-30)

Ela cai do teto do mundo no lugar que o feitiço marcou e, ao chegar, fere **tudo o que estiver a cinco
blocos** e tiver linha de vista para ela. O dano é **dois vezes quinze** — e o modificador de Dano multiplica
os quinze **antes** de eles serem dobrados, que é a conta do original e é por isso que ela cresce depressa.

**Ela não se vê, e isso é do original.** A mesma entidade, usada pelo Guardião da Terra, é uma pedra de três
caixas com a pele dele — mas o desenhista dela começa por perguntar se é uma estrela cadente e, se for,
**não desenha nada**. O que se vê é só o rastro: brasas azuladas ao longo do caminho, uma a cada décimo de
bloco que ela desce. A cor é a do original, `0.24, 0.58, 0.71` — um azul de madrugada.

**E ela acelera**: um décimo por batida, até o teto de dois blocos por batida. Uma estrela chamada de muito
alto chega a cair mais depressa do que se vê.

### Fatia 25 — o Elo de Mana (2026-09-30)

A última peça do ramo, e a que mexe no cano por onde a mana sai.

**Um elo põe a sua mana ao alcance de outra pessoa.** Quando a dela acaba no meio de um feitiço, o que falta
sai da sua — e só enquanto estiverem perto.

**Quem ganha o elo é quem leva o feitiço, e não quem o lança.** se Lê mal e é o que o original faz
(`For(target).updateManaLink(caster)`): lançar o Elo em alguém é **dar-lhe** a sua mana, e não tomar a dele.
É um feitiço de quem joga acompanhado.

**O alcance é curto**: vinte de distância **ao quadrado**, que é como o original mede — pouco mais de quatro
blocos e meio. Um elo não é uma corda comprida.

**E a ordem importa**: gasta-se a mana própria primeiro, e só o que sobrar é que sai das emprestadas. É o que
faz do Elo uma rede de emergência em vez de uma torneira.

Com ele, **os dez segredos do original estão todos no lugar**.

### Fatia 26 — o livro conta o que o jogo não conta (2026-09-30)

Três páginas novas na aba do ramo: **o que fica depois** (os efeitos e as bênçãos), **mexer no céu** (e o
preço de o fazer) e **o que não se compra**.

A terceira é a que tinha de existir. **Nada no jogo diz a quem joga que há perícias que não se compram** — e
é assim de propósito, porque descobri-las é o que elas valem. Mas um jogador que nunca souber que elas
existem também nunca vai procurar, e aí o segredo deixa de ser segredo e passa a ser conteúdo morto.

A página resolve isso dizendo **que elas existem e como se abrem**, e **não dizendo quais são**: as dez
combinações continuam por descobrir. O que ela dá é a direção — frases que juntam coisas que ninguém junta —
e o aviso de que um feitiço emprestado com uma delas dentro não sai da mão de quem não a descobriu.

### Fatia 27 — as três que pedem uma escolha (2026-09-30)

As três que tinham ficado para trás. Quase toda peça do ramo diz tudo o que é; estas não: a **Cor** não diz
*qual* cor, e o **Colocar Bloco** e a **Apropriação** não dizem *qual* bloco. Cada uma responde à sua maneira,
e são duas maneiras diferentes — porque no original também são.

**A etapa ganhou um dado.** No Ars Magica 2 isto são `byte[]` guardados no NBT da varinha com chaves como
`SpellModifierMeta_14_0_0`; aqui é um **número por nome de peça, por etapa**, que é o alcance que o original
lhes dá. Hoje só a Cor tem um.

**A Cor responde na Mesa.** se Põe uma **tinta** na casa logo a seguir à peça, e é ela que manda — que é o
mesmo lugar que a tinta tem no original, onde ela é ingrediente da receita da Mesa, lido da esquerda para a
direita. Sem tinta **a Mesa recusa a frase e diz porquê**: no original a receita simplesmente não casa, e
aqui achei melhor dizer do que deixar sair um feitiço preto que ninguém pediu.

**As dezesseis cores são as do original**, a tabela `ItemDye.dyeColors` do 1.7.10. O jogo de hoje tem outras
três tabelas de cor de tinta — a da ovelha, a do fogo de artifício, a do texto — e nenhuma delas dá estes
números. Como o que se vê é o ponto da peça, ficam os dele.

**E ela pinta sem trocar a cara.** O projétil leva a cor à parte da Afinidade, como o `DW_COLOR` do original:
a Afinidade continua decidindo a **figura**, e a Cor só a pinta. Está na foto — cinco projéteis de fogo lado
a lado, um sem Cor e quatro com tintas, todos com a mesma figura. O de tinta branca sai igual ao sem Cor, e
é o que tem de ser: o branco do original é `0xF0F0F0`.

**As outras duas perguntam ao mundo.** O **Colocar Bloco** aprende **agachado** — lançado assim contra um
bloco, fica sabendo aquele bloco; de pé, põe um igual e gasta um da mochila. É a única peça do ramo que muda
de trabalho conforme a pessoa está agachada, e é do original.

**A Apropriação tira a coisa do mundo e a leva dentro.** Um bloco **com o que ele tem dentro** — um baú
apropriado volta com as coisas lá — ou um bicho inteiro, com o nome e a vida que tinha. Enquanto estiver
guardado, aquilo **não existe** em lugar nenhum senão no feitiço. Uma coisa de cada vez, e gente não: o
original recusa gente e chefes, e este recusa também.

Com estas três, **todas as peças de feitiço do Ars Magica 2 que não dependem da outra metade do mod estão
portadas**.

## A aldeia do Ars Occulta — Fatia A: o Guarda

Um buraco que passou batido, e que não estava declarado em lugar nenhum: o Witchery **mexe na aldeia**. Faz
umas delas maiores, cerca-as de muralha, põe-lhes forte, torre de vigia, boticário, livraria e cabana de bruxa,
e põe gente armada a andar lá dentro. Nenhuma linha disso tinha entrado no porte, e as fatias do Ars Occulta
nunca tocaram no assunto — ao contrário de tudo o mais que ficou de fora, que está escrito aqui com a razão.
Esta fatia abre a conta, e começa por quem mora lá.

**O Guarda responde a uma pergunta que o Minecraft nunca respondeu: quem defende a aldeia de gente.** O golem
defende de monstro, e de quem a aldeia já odeia. O guarda é outra coisa — é um aldeão que pegou um arco, mora
na aldeia, anda por ela, abre e fecha as portas, e mata o que entrar.

**Quarenta de vida e quatro de dano**, e briga de longe ou de perto **conforme o que tem na mão**: com arco
atira, sem arco avança. E ele volta a decidir isso toda vez que a mão muda, e não só ao nascer — desarmar um
guarda fá-lo avançar. É o `setCombatTask` do original, e há duas provas para os dois lados dele.

**Nasce de couro, e uma em cada cinco vezes o peito e a cabeça vêm de malha.** É o detalhe que faz uma aldeia
guardada parecer guardada de verdade, e não uniformizada.

**Dois tipos.** O comum, do tamanho de gente. E o **infernal**, 0,72 por 2,34, imune ao fogo e com flechas que
queimam cem tiques. O original guarda isso num byte e troca o tamanho ao trocar o byte; aqui é a mesma coisa,
pelo `refreshDimensions`.

**E o infernal é maior só na caixa, não no desenho** — no original também. A foto dele ao lado do comum mostrava
dois guardas iguais, e eu fui conferir antes de chamar aquilo de defeito: o `RenderVillageGuard` do Witchery é
apenas `super(new ModelVillageGuard(), 0.5F)`, **sem escala nenhuma**, e o `ModelVillageGuard` não olha o tipo.
Lá o infernal ocupa mais espaço e tropeça em tetos mais baixos, mas se desenha do tamanho de gente. Fica assim,
porque é assim; e fica escrito para quem vier depois não "consertar" uma fidelidade.

**Dois guardas nunca se batem nem se ferem, e nenhum deles mira num creeper.** É o `canAttackClass` do
original, e a razão do segundo é óbvia para quem já viu um creeper ao lado de uma casa.

**O corpo é uma mistura, e é de propósito.** Tronco e braços de gente, para a armadura assentar; cabeça alta de
aldeão, com nariz; e uma túnica meio ponto mais larga por cima do tronco. As pernas andam **pela metade** da
amplitude e sem volta para os lados — o que se vê é um andar pesado, de quem está de guarda, e não o trote do
aldeão. Tudo isso é o `ModelVillageGuard`, e a túnica entra na raiz do modelo e não no tronco porque é onde o
original a desenha: à parte, depois de tudo, sem receber volta nenhuma.

**A mira genérica.** O jogo de hoje já tem o defender-a-aldeia — o `DefendVillageTargetGoal` —, mas ele está
**preso ao golem de ferro no tipo**: pede um `IronGolem` e não um bicho qualquer. O Witchery tinha escrito a
dele justamente para a poder dar a outro bicho, e o nome diz isso: *genérica*. Aqui ela é a mesma conta, aberta
a qualquer um.

**Desvios declarados.**

1. **O sangue fica de fora.** O guarda do original tem um poço de quinhentos de sangue, que serve de comida a
   vampiro, e o `takeBlood` dele conta com a paralisia do ofício. O vampiro não está portado; um poço de sangue
   sem quem o beba é peso morto. Volta quando o vampiro vier.
2. **A casa do guarda é um raio fixo.** O original pede à aldeia o centro e o tamanho, e prende o guarda a
   `tamanho × 1,5`. O jogo de hoje não tem objeto de aldeia nem tamanho de aldeia — tem lugares de interesse
   espalhados por seções. Aqui se acha a seção de aldeia mais perto e se prende o guarda a **quarenta e oito**,
   que é o que aquele produto dava numa aldeia de tamanho comum. A cura de um de vida por volta, quando ele não
   tem ninguém para matar, é a do original.
3. **A reputação é a de hoje, e se conta como morte de aldeão.** O original tira cinco da reputação da aldeia
   de quem o matou. Hoje reputação é fuxico de aldeão, e o que se conta aos aldeões num raio de dezesseis é o
   `VILLAGER_KILLED` — que além de funcionar é o mais fiel, porque no original **o guarda nasce de um aldeão**
   que pegou um arco (`createFrom(EntityVillager)`), e matá-lo pesa como matar quem mora ali.

   **E não o `GOLEM_KILLED`, que era o que parecia certo.** Ele existe no jogo de hoje, mas aparece **uma única
   vez em todo o código, na própria declaração**: nada o dispara e nada o trata — o `onReputationEventFrom` do
   aldeão só conhece quatro eventos, e esse não está entre eles. É constante morta. Escrevi-o primeiro, e a
   prova o apanhou: a reputação não se movia.
4. **Não há o recolher-se à noite.** A casa oito da mira do original era o `EntityAIRestrictOpenDoor`, que
   mandava o bicho ficar dentro de casa. Esse comportamento deixou de existir e não tem par; fica o abrir e
   fechar portas, que é a casa nove. A casa oito ficou vazia de propósito, para a ordem do original se ler.
5. **Duas perguntas viraram uma.** A mira de defender a aldeia fazia duas: *quem a atacou* e *com quem ela está
   mal*. A primeira deixou de existir — a aldeia não é mais um objeto que guarda quem a agrediu, não há a quem
   perguntar. Ela está refeita no próprio guarda, que caça monstro por conta e caça gente que bate em quem mora
   ali. Para a mira sobra a segunda, que é a que a reputação de hoje sabe responder.
6. **O Caçador de Bruxas e o Goblin não entram na conta de quem é alvo**, por não estarem portados. No original
   o guarda poupa o Caçador (são do mesmo lado) e caça o Goblin mesmo não sendo monstro.

**Guardas:** `OccultaVillageGuardGameTest`, com seis — o que ele veste e os números dele, o de arco que atira,
o de espada que troca de mira e chega perto, os dois que se poupam e o creeper que escapa, o infernal maior e
imune, e a aldeia que fica de mal com quem o matou.

**Três pedras no caminho, que ficam escritas porque nenhuma era defeito do original e todas voltariam a morder.**

1. **O `registerGoals` corre dentro do construtor do `Mob`** — antes de os campos da subclasse serem
   atribuídos. As duas brigas nasciam na declaração do campo e por isso eram `null` na hora em que a mira as
   pedia. Nascem no `registerGoals`, e o `arrumaBriga` sai calado se for chamado antes disso. Quem apanhou foi
   o `MobTickGameTest.everyMobSurvivesItsOwnThinking`, que já estava no projeto e cria cada bicho do mod: achou
   o defeito antes de qualquer prova nova rodar.
2. **A arena de prova é oito por oito por oito de ar puro** — não tem chão. Sem piso o guarda cai, e uma prova
   de briga morre sem nunca ter começado.
3. **Criatura posta à mão com `addFreshEntity` é criatura apagada.** O `helper.spawn` do jogo chama
   `setPersistenceRequired` antes de a pôr no mundo; sem isso um bicho de categoria `CREATURE` sem jogador por
   perto desaparece em poucos tiques. O que se vê é um guarda que não atira e não anda, com a vida cheia e na
   posição exata onde nasceu — e parece defeito dele. Isto é também um **requisito das estruturas**: o original
   chama `enablePersistence()` em todo guarda que nasce do Forte e da Torre, e a fatia C tem de fazer o mesmo.

**A foto.** `OccultaVillageGuardClientTest`, com duas — três guardas em fila, onde o sorteio da armadura se vê
(uma em cinco vezes o peito e a cabeça vêm de malha, e é isso que faz uma aldeia guardada não parecer
uniformizada); e um de frente e de perto, onde a mistura se lê: cabeça alta de aldeão com nariz, túnica por cima
do tronco, braços de gente de fora. O infernal não entra em foto nenhuma, pela razão de cima.

E uma lição de prova, não de porte: **provar que algo não aconteceu olhando o efeito não serve.** A prova do
"sem arco não atira" contava flechas, e flecha que acerta é removida na hora — no fim nunca há nenhuma, e ela
passava sempre. Agora ela julga a **decisão** do `setCombatTask`: posto o arco a mira de longe entra, tirado
sai, devolvido volta, e de espada ele **chega perto**.

## A aldeia do Ars Occulta — Fatia B: maiores, e em mais lugares

Esta fatia não traz bicho nem bloco: mexe só no que o jogo lê do disco. E é a que mais obrigou a **traduzir** em
vez de transcrever, porque o Minecraft de hoje faz aldeia de um jeito que o de 2014 não fazia.

**As aldeias ficaram maiores.** No original, o `WorldHandlerVillageDistrict.preInit` registra cada peça de aldeia
do próprio jogo **outra vez, em grupos**, pela tabela `townParts` do Config: a casa de jardim, a casa, a cabana
de madeira, o salão, a casa 3 e os dois campos entram **três grupos cada**, com peso 20 e três a cinco de cada
vez; o ferreiro entra um grupo, peso 5, zero a um; a torre de vigia quatro grupos; e a **igreja entra zero**, que
é o jeito do original de dizer que não quer mais igrejas do que o jogo já dá. O gerador de 2014 escolhia peças
por peso até esgotar as contagens, e por isso registrar a mesma peça de novo fazia a aldeia crescer.

**Isso não tem equivalente hoje.** A aldeia do 26.2 é um salto-de-encaixe: o número de peças não vem de peso
nem de contagem, vem da **profundidade** do salto. Peso aqui decide *qual* peça entra, não *quantas*. A única
alavanca que o jogo de hoje dá para o tamanho é o `size` da estrutura, e é essa que se usou: **de seis para
oito**, nas cinco variantes.

**E nascem em mais biomas.** O `init` do original percorre todo bioma do jogo e chama `addVillageBiome` em tudo
o que não é molhado, oceano, praia, rio, selva, End nem Nether — com uma chave por tipo para desligar, e a selva
já desligada de fábrica. Isso é a taxonomia do `BiomeDictionary` de 2014, que não existe mais; o que se fez foi
levar a **intenção** dele aos biomas de hoje:

- **planície** ganha floresta, floresta de flores, bétula, bétula antiga, floresta escura, cerejeira, girassóis
  e os três ventosos (montanha, floresta e cascalho) — o FOREST, o PLAINS, o MOUNTAIN e o HILLS do original;
- **deserto** ganha os três *badlands*, que é o MESA que o original permite;
- **savana** ganha o planalto e a savana ventosa;
- **neve** ganha encostas nevadas, bosque e picos de gelo;
- **taiga** ganha a taiga nevada e as duas antigas.

**Desvios declarados.**

1. **O tamanho é outra alavanca.** Não se registram peças repetidas, porque não há onde; muda-se a profundidade.
   O efeito é o mesmo — aldeia maior — mas a conta é outra, e uma aldeia do porte não terá *exatamente* as três
   a cinco casas a mais de cada tipo que o original dava.
2. **Etiqueta soma, estrutura substitui.** As cinco etiquetas de bioma levam só os acréscimos, e o jogo as junta
   às dele — a prova confere os dois lados, que os novos entraram e que **os do jogo continuam lá**, porque uma
   etiqueta escrita com `replace` por engano apagaria as planícies e tudo continuaria a parecer bem. Já os cinco
   arquivos de estrutura **substituem** os do jogo, e por isso são cópia exata deles com um número trocado.
   **Risco declarado:** se a Mojang mexer nesses arquivos, a nossa cópia fica velha.
3. **Pântano, selva e cogumelo continuam sem aldeia**, como no original — e há prova disso, porque é no pântano
   que o coven vai morar.
4. **O ermo e o planalto de pedra ficam de fora.** O original permite WASTELAND, que no jogo de hoje não tem
   correspondente, e MOUNTAIN, que hoje inclui picos nus onde uma aldeia seria absurda. Entraram os ventosos,
   que são a montanha habitável; ficaram de fora os picos.
5. **O Jardim Pálido fica de fora.** Pela taxonomia do original ele seria FOREST e entraria; é um bioma de 2024
   feito para não ter ninguém, e pô-lo aqui seria aplicar a regra contra o sentido dela.

**Guardas:** `OccultaVillageSpreadGameTest`, com três — as cinco aldeias com tamanho oito, os biomas novos mais
os do jogo, e o molhado que continua vazio. A do tamanho pergunta à estrutura **pelo codec dela**: o `size` não
tem acessor público, então se escreve a estrutura como ela foi carregada e se lê o número de volta. Prova o que
o jogo tem na mão, e não o que está num arquivo que ele podia nem ter lido.

**A foto:** `OccultaVillageSpreadClientTest`, uma — a aldeia posta com `/place structure` e vista de viés. Ela
custou quatro tentativas, e as três pedras ficaram escritas no javadoc dela: o jogador tem de ir ao lugar antes,
senão o trecho não está carregado e não se planta nada; a altura se pergunta ao mapa de alturas, senão a aldeia
nasce enterrada; e **`gamemode` pede alvo** — corre a partir do console, que não é jogador nenhum, e sem `@a`
não faz nada (quem for posto no ar cai e morre).

## A aldeia do Ars Occulta — Fatia C1: a Torre de Vigia, e a máquina que põe prédios na aldeia

A fatia C é a maior da conta — muralha, forte, torre, boticário e livraria —, e por isso vai por partes. Esta
primeira traz **uma** peça e, com ela, toda a máquina que as outras vão usar.

**O problema, em uma frase:** o original constrói cada prédio **bloco a bloco em código** e registra a classe no
gerador de aldeia; o jogo de hoje monta aldeia por **salto-de-encaixe**, e as peças dele são moldes `.nbt` que
vivem em piscinas carregadas do disco. Nem a forma nem o registro passam diretos.

### Traduzir código procedural em molde

O `ComponentVillageWatchTower` é uma sequência de `fillWithBlocks` e `placeBlock`. O que se fez foi escrever um
**gerador** que repete as mesmas chamadas, na mesma ordem, e assa um molde. O leitor e o escritor de NBT foram
escritos para isto.

**A regra que faz a tradução ser fiel: o que o original não toca vira `structure_void`, e não ar.** Um molde
nasce cheio de alguma coisa, e se essa coisa for ar o prédio arrasa o terreno à volta e as peças vizinhas. O
original só punha os blocos que punha; o resto ficava como estava. O ar **que ele põe de propósito** — o vão da
escada, as frestas, as portas — continua ar.

**As medidas e a metadata.** Nove por vinte e quatro por nove, que é o `(0,0,0 .. 8,23,8)` do original. As
escadas vêm por número no 1.7.10 — 0 leste, 1 oeste, 2 sul, 3 norte —, e foi desse número que a propriedade
`facing` de hoje nasceu; a tabela é a mesma. O `getMetadataWithOffset` do original, que girava a peça conforme a
orientação dela, **não se traduz**: aqui quem gira o molde é o salto-de-encaixe, e por isso o molde é escrito
sempre na orientação base.

**O que ficou de fora, declarado:** o `fillColumnDown` e o `clearCurrentPositionBlocksUpwards`, que o original
usava para assentar a torre no terreno e limpar o que estivesse por cima. Os dois mexem **fora** da caixa do
molde e não têm como ser escritos nele; hoje quem faz esse trabalho é o `terrain_adaptation: beard_thin` que a
aldeia já traz.

### Somar peças às piscinas do jogo

A piscina de casas de cada variante é um arquivo do jogo, e arquivo de dados **substitui**. Copiar os cinco
arquivos inteiros e colar as nossas peças no fim seria dez mil bytes de dados do jogo duplicados por variante,
velhos no dia em que a Mojang mexer numa casa. Em vez disso há um mixin de acesso, e as peças se somam à
**piscina já carregada**, ao servidor arrancar — depois de o disco ser lido e antes de se gerar qualquer trecho.

**São duas listas, e as duas têm de mudar.** A `templates` é a lista já esticada pelo peso, de onde o sorteio
tira; a `rawTemplates` é a de pares peça-peso, que é a que o `getMaxSize` lê para saber de quanto espaço a
aldeia precisa. **Mexer só na primeira faz a peça nascer e ficar cortada ao meio** — e isso está escrito no
javadoc do mixin, porque é o erro que se comete uma vez.

### A torre

**Peso 20**, o do original. Lá ela entra em quatro grupos de zero a um, que no gerador de então era o jeito de
dizer "até quatro torres, e talvez nenhuma"; aqui a profundidade do salto decide quantas peças cabem e o peso
decide quantas vezes esta é sorteada entre as candidatas. **Desvio declarado:** o número de torres por aldeia
não é mais garantido entre zero e quatro.

**O baú** tem a tabela do original, as dezoito entradas com os pesos dele e os dois a cinco sorteios — pão,
maçã e peixe pesados 15; a malha e o ferro pesados 5; o couro 6; arco e flechas 8; sela 3; e as duas armaduras
de cavalo pesadas 1.

**E são quatro guardas, não três.** O original chama `spawnGuards(..., 3)` e o laço dele é
`for (n = 0; n <= 3; n++)` — quatro voltas. É descuido do original, e vai assim. Eles entram na lista de
entidades do molde, **com a persistência posta**, que é o que o `enablePersistence()` do original fazia e o que
a fatia A já tinha deixado escrito como requisito.

**Guardas:** `OccultaWatchtowerGameTest`, com duas — a torre nas cinco piscinas com peso 20, e o molde que
carrega com nove por vinte e quatro por nove. A primeira confere também que **as casas do jogo continuam lá**:
somar na piscina errada faria aldeias só de torres, e uma prova que olhasse só a torre diria que está tudo bem.
A segunda existe porque um molde que não carrega **não dá erro nenhum** — a peça simplesmente não nasce.

**As fotos:** `OccultaWatchtowerClientTest`, duas — a torre inteira de fora e o mirante de perto, com os
guardas lá dentro. Elas são o que julga o molde de verdade: a prova de servidor passa igual se o telhado
estiver virado do avesso.

## A aldeia do Ars Occulta — C1b: a aldeia re-vestida pelo bioma, e os cinco nomes de bloco

Esta emenda sai de uma pergunta pequena — *que bloco é o `field_150487_bG`?* — que abriu um buraco que eu tinha
aberto na C1 sem dar por ele.

### Cinco nomes, resolvidos com prova

Os prédios que faltam usam cinco blocos cujo nome de 2014 eu não sabia, e adivinhar é o que este porte não faz.
Não há jar desofuscado do 1.7.10 à mão, e a busca não deu a tabela. A prova veio de **dentro do próprio
material**:

- O `ConfigAspects` do **Thaumcraft 4.2.3.5** registra aspectos por metadata, e isso identifica um bloco sem
  margem. O `field_150417_aV` tem meta 0 terra, meta 1 terra+**planta**, meta 2 terra+**entropia**, meta 3
  terra+**ordem** — musgo, rachado e entalhado: é o **tijolo de pedra**. O `field_150322_A` tem meta 1
  terra+**magia** e meta 2 terra+**ordem** — a cara entalhada e o liso: é o **arenito**.
- O `WorldHandlerVillageDistrict$Wall` atribui `blockBase` e `stairsBlock` **em par**, e troca os dois juntos:
  daí `field_150390_bg` ser a **escada de tijolo de pedra** e `field_150372_bz` a **de arenito**.
- E o `field_150487_bG` sai do `EventHooks`: no deserto a tábua é trocada por **meta 2, que é bétula**, e é por
  isso que a escada de carvalho vira a dele — **escada de bétula**.

### O buraco que isso abriu

Lendo o `EventHooks` apareceu uma coisa que eu não sabia que existia: **o Witchery re-veste a aldeia conforme o
bioma**, pelos eventos `GetVillageBlockID` e `GetVillageBlockMeta` do Forge. No deserto, pedregulho e tora viram
arenito e toda a madeira vira bétula. Na neve, tudo vira neve e gelo.

E na C1 eu tinha posto **a mesma torre de pedregulho e carvalho nas cinco variantes**. Passava nas provas e
estava errada.

**O conserto:** um molde por variante. O gerador passou a receber o material, e saem cinco torres — o deserto
em arenito e bétula, as outras quatro no material comum. De quebra isso arrumou outra coisa que estava errada e
não dava erro: o bloco de encaixe de cada molde aponta à **piscina de ruas da sua aldeia**, e antes todos
diziam `plains`.

**Desvio declarado: a aldeia de neve fica com o material comum.** O ramo da neve do original troca tudo por
blocos **dele** — `SNOW_STAIRS`, `SNOW_SLAB_SINGLE`, `PERPETUAL_ICE_FENCE`, `SNOW_PRESSURE_PLATE` —, e nenhum
deles está portado. Volta quando eles vierem.

**E uma observação que poupa trabalho futuro:** o resto do `EventHooks` **não precisa ser portado**. Ele existe
porque a aldeia de 2014 era uma só, de pedregulho e carvalho, em todo bioma; o jogo de hoje já tem cinco
variantes de aldeia com material próprio e resolve isso sozinho. O que sobrou de útil dele foi a tabela de
materiais do deserto, que está no `tools/aldeia/materiais.js`.

### Os geradores entram no repositório

Os moldes `.nbt` são binários, e sem o gerador ninguém os revisa nem os regera. Os scripts passam a morar em
`tools/aldeia/` — `nbt.js` (leitor e escritor de NBT), `materiais.js` (a tabela por variante) e `torre.js`. Para
refazer os moldes: `node tools/aldeia/torre.js`.

**A foto nova:** as duas torres lado a lado, a comum e a do deserto, que é o que mostra a re-vestimenta.

## A aldeia do Ars Occulta — Fatia C2: o Forte

A maior peça da aldeia: **dezessete por vinte e sete por dezessete**, duas torres ligadas por um portão e um
torreão no meio. O corpo saiu do `ComponentVillageKeep` traduzido chamada por chamada, e desta vez com um
tradutor escrito para isso — o Java descompilado do Witchery é regular o bastante para se traduzir por padrão,
e o que não casa sai marcado. Das dezessete marcas que sobraram, **nenhuma era geometria**: eram escrituração,
as duas chamadas da torre, o baú e os guardas.

**Uma armadilha do original, que vale guardar.** O ajudante dele, `fill(x,y,z, largura,altura,fundo)`, **não é**
o `fillWithBlocks` do jogo, que vai de canto a canto. Confundir os dois faz um prédio quase certo — paredes um
bloco mais curtas, telhado um bloco mais baixo —, e é o tipo de erro que nenhuma prova apanha e só a foto mostra.

**E o `drawTower(offsetX, flipX)`** desenha a mesma torre duas vezes, em `0,0` e em `8,4`; o segundo número
espelha a janela e a viga para o outro lado.

**Quinze guardas**, de três chamadas — `(7,1,7,3)`, `(5,10,4,4)` e `(13,10,4,5)` —, e os três números saem do
mesmo descuido do original, cujo laço é `n <= conta` e dá sempre uma volta a mais: quatro, cinco e seis. É o
mesmo descuido da torre de vigia, e vai assim.

**O baú** fica em `13,20,12`, com três a oito sorteios e a tabela do original: ouro em barra e em pepita pesados
10 e 20, as quatro peças de armadura de ouro e as duas ferramentas pesadas 5, e as armaduras de cavalo de
diamante e de ouro pesadas 1.

### O desvio que mais pesa desta fatia

**No original o Forte tem peso 100 e no máximo um por aldeia.** O gerador de 2014 sabia limitar quantidade — o
`PieceWeight` leva peso **e** conta —, e o salto-de-encaixe de hoje **não sabe**: peso aqui só diz quantas vezes
a peça é sorteada entre as candidatas, e nada impede que saia duas.

Peso 100 contra os **87** que a piscina de casas do jogo soma faria quase toda construção da aldeia ser um forte
de dezessete por vinte e sete. O que se fez foi medir: a piscina tem trinta e sete entradas somando oitenta e
sete, e **peso 5** é o que faz a conta dar cerca de um forte por aldeia. **Não é o número do original; é o
efeito dele.**

A torre de vigia não precisou disso: o peso 20 dela dá à volta de um quinto das escolhas, que é o que os "quatro
grupos de zero a um" do original entregam.

**Guardas:** `OccultaVillagePiecesGameTest`, com três — as peças nas cinco piscinas com o peso de cada uma (e as
casas do jogo ainda lá), os moldes com as medidas certas, e **o encaixe de cada molde apontado à rua da sua
aldeia**.

Esta última entrou no lugar de uma que eu ia escrever e não dava: contar os guardas de um molde, que não tem
acessor público. E saiu melhor do que a que eu queria, porque pega exatamente o engano que escapou na C1 —
encaixe faltando, ou apontando à piscina de outra variante. **Nenhuma das duas coisas dá erro**: a aldeia só sai
sem a peça, calada.

## A aldeia do Ars Occulta — Fatia C3: o Boticário e a Livraria

As duas casas da aldeia, e as duas que trouxeram coisa que as peças anteriores não tinham: o Boticário tem
**porta, placa com o nome e um morador**, e a Livraria tem **quatro quadros com livros** na parede do fundo.

**Os pesos não precisaram ser inventados.** Cada uma tem um handler próprio no original — o
`WorldHandlerVillageApothecary` e o `WorldHandlerVillageBookShop` —, e os dois dizem `PieceWeight(classe, 15,
1 + (tamanho > 2 ? sorteio(2) : 0))`: **peso 15**, uma ou duas por aldeia.

### Dois erros que só a foto apanhou

Os dois são do mesmo feitio — coisa que mudou de 2014 para hoje e que **falha calada** —, e nenhuma prova de
servidor os teria mostrado.

**1. Os quadros nasciam dentro da parede.** No 1.7.10 a posição de um quadro é o bloco em que ele se **prende**;
hoje é o bloco que ele **ocupa**, com a parede atrás. O original pendura em `3..6, 3, 6`, e o 6 é a madeira:
traduzido ao pé da letra, o quadro nasce dentro dela e não se vê. Aqui vai em `5`, que é o ar à frente da mesma
parede.

**2. As chaves do NBT de um quadro mudaram.** Hoje é `Facing` com F grande, escrito pelo
`Direction.LEGACY_ID_CODEC`, e `block_pos` como **vetor de inteiros** — e não o `TileX`/`TileY`/`TileZ` de
então. Com as chaves erradas o quadro simplesmente não nasce, sem erro nenhum. (Foi por isto que o escritor de
NBT do `tools/aldeia` aprendeu a escrever vetor de inteiros.)

### Desvios declarados

1. **O morador do Boticário é um clérigo.** O original lhe dá uma profissão sua, o `ApothecaryVillagerID 2435`,
   com as trocas dela; isso é um sistema de aldeão que não está portado. Clérigo é a profissão do jogo mais
   perto de quem vende poções.
2. **A placa fez o molde crescer um bloco.** O original declara a caixa do Boticário como `(0,0,0 .. 9,9,6)` e
   depois põe a placa e o degrau da porta em **z = -1**, fora dela — no gerador de 2014 a conta era feita contra
   o pedaço de mundo a gerar e não contra a caixa da peça, e por isso funcionava. Um molde não tem coordenada
   negativa: o molde é um bloco mais fundo e tudo anda um em z. O corpo fica literal como o original o escreveu,
   e quem desloca é o `ponha`.
3. **A Livraria perdeu o baú vampírico.** No original ele é livro comum mais **três entradas de página de livro
   vampírico** (pesos 3, 2 e 1) e um **Livro Vampírico** garantido. O sistema de vampiro não está portado —
   é o mesmo que já tinha tirado o sangue do Guarda —, e por isso ficam de fora. Sobram o livro, o livro de
   escrever e **o Thaumonomicon**.
4. **E o Thaumonomicon está ali de propósito.** A lista de livros do Config do original inclui
   `Thaumcraft:ItemThaumonomicon`: quando o Thaumcraft estava instalado, a livraria da aldeia o vendia. Aqui os
   dois **são o mesmo mod**, então ele está sempre lá — no baú e no quadro do meio.
5. **Os quadros têm livro fixo.** O original sorteia um da tabela da loja para cada quadro; um molde é estático
   e não sorteia. A escolha ficou: livro, Thaumonomicon, livro, livro de escrever.
6. **O degrau da porta deixou de ser condicional.** O original só o põe se houver um desnível à frente da porta.
   Num molde não há condição; ele é sempre o `final_state` do bloco de encaixe, que é onde a rua encosta.

**Guardas:** as três provas de `OccultaVillagePiecesGameTest` passam a cobrir **as quatro peças** — peso, medidas
e o encaixe apontado à rua da sua aldeia.

**As fotos:** o Boticário de frente, onde se veem a porta, a placa por cima dela e o morador pela janela; e a
Livraria **de cima, sem telhado**. O telhado sai por um `fill` só para a foto: acertar uma câmera entre as
paredes de uma loja fechada custou mais tentativas do que tirar o teto, e o que se quer ver são os quatro
quadros.

## A aldeia do Ars Occulta — Fatia C4: a Muralha

A peça que fecha a conta da aldeia, e a única que **não é um prédio**.

**Uma muralha precisa saber onde a aldeia acaba**, e isso só se sabe depois de a aldeia estar desenhada. Por
isso, no original, a "peça" da muralha é um marcador de três por oito por três que põe **um bloco invisível** —
o `BlockVillageWallGen` —, e quem levanta a muralha é o bloco-entidade dele, quarenta tiques mais tarde. Aqui é
o mesmo: o molde tem o marcador e nada mais.

**O algoritmo, que é o coração dela.** Pega só as **ruas** — as casas ficam todas dentro delas —, engorda cada
uma sete além das pontas e vinte para cada lado, une tudo num mapa de duas dimensões, **fecha os vãos de até
sete** que tenham sobrado entre ruas soltas, e depois **apaga o miolo**: toda célula cujas oito vizinhas estejam
ocupadas deixa de contar. O que sobra é a borda, e é nela que a muralha se levanta.

**E os portões saem de graça**: as três células do meio da **ponta** de cada rua engordada ficam marcadas à
parte. É por ali que a estrada sai da aldeia, e é ali que a muralha se abre.

**A altura é sondada e suavizada.** Para cada pedaço se desce contando blocos sólidos à volta até achar nove —
é o que impede a muralha de nascer sobre uma copa de árvore —, e a altura é depois puxada **um degrau de cada
vez** contra a da vizinha já feita. É isso que a faz acompanhar o relevo em vez de flutuar.

**Onde isto melhora o original, declarado.** Lá o bloco recebia a lista de peças pela mão de quem o criou. Aqui
ele **pergunta ao mundo** — o `getStructureWithPieceAt` dá a aldeia inteira a partir da posição dele. Não
depende de ninguém lhe entregar nada, e por isso funciona mesmo que o trecho seja carregado de novo mais tarde.

**Desvios declarados.**

1. **A muralha não come parede de casa.** O original troca ar, folha, planta **e madeira** — e "madeira" ali
   queria dizer árvore no caminho. Hoje não há "material"; ficaram folha e tronco. Tábua fica de fora: uma
   muralha que abre buraco na casa de alguém é defeito, não fidelidade.
2. **Rua se reconhece pelo nome do molde.** A classe `Path` de 2014 não existe; o que distingue uma rua hoje é
   o molde dela viver em `village/<variante>/streets/`. É mais frágil do que por tipo, e é o que há.
3. **Peso 12, e não os 100 do original.** Mesmo caso do Forte: o salto-de-encaixe não sabe limitar quantidade.
   Mas aqui se pode arriscar um número mais alto, porque **duas muralhas não estragam nada** — desenhar outra
   vez é quase de graça (só se troca o que é trocável, e tijolo não é) e a guarnição não dobra, porque cada
   guarda confere se já há um a oito blocos.

### Duas coisas que esta fatia ensinou sobre as próprias provas

**O `/place structure` não registra a aldeia.** Ele desenha os blocos, mas não deixa o trecho a saber que há ali
uma aldeia — e o marcador, que pergunta exatamente isso, não acha ruas nenhumas e desiste. A primeira tentativa
da foto saiu com a aldeia e **sem muralha**, e o diagnóstico disse porquê: `INVALID_START`. A prova passou a
**procurar uma aldeia de verdade** com o `findNearestMapStructure`.

**E o mundo das provas de cliente é superplano.** O `setConsistentSettings` do Fabric força o preset FLAT — é o
que faz as fotos ficarem iguais entre execuções, e é por isso que o chão de todas as outras é liso. Num
superplano não nasce aldeia nenhuma. Esta foto pede um mundo normal **de semente fixa**, que é o jeito de ter
aldeia sem perder a repetibilidade.

**Guardas:** `OccultaVillageWallGameTest`, com quatro — e nenhuma delas escreve um bloco no mundo. A muralha de
verdade cerca centenas de blocos e não cabe numa arena de oito por oito, mas **a forma dela sai toda da
planta**, e planta é conta pura. Provam-se o miolo apagado, o portão na ponta certa da rua, duas ruas cruzadas
a darem **uma** mancha só, e a aldeia sem rua que não ganha muralha.

Uma delas me apanhou: eu tinha escrito a expectativa do portão pela conta que **me parecia certa** — o meio da
rua — em vez da que o original faz. Ele calcula `altura / 2 + mínimo - 1`, e esse `-1` com a divisão inteira
cai **um bloco antes do centro**. O código estava fiel; a régua é que era minha. Ficou escrito no comentário
dela, porque é o terceiro descuido do original que encontro nesta conta e a tentação de "corrigir" é real.

**A foto:** a aldeia murada vista de cima — a muralha a acompanhar o relevo em degraus, as ameias, o portão por
onde a estrada sai, e a torre de vigia lá dentro.

## Fatia D — a Bruxa do Coven, e o zero que saiu dos círculos

Esta fatia fecha um buraco que estava **escrito no código à espera** desde que os círculos de giz entraram: o
`Rite.steps(int coven)` existia, e o `CircleHeartBlockEntity` lhe passava **zero na mão**, porque não havia quem
respondesse. Todo rito do ofício que faz mais com mais bruxas em volta corria no mínimo.

**A Bruxa do Coven não é monstro nem aldeã — é alguém com quem se negocia.** se Fala com ela e ela pede uma
coisa; aceita-se, e ela espera; trazido o que pediu, **entra no coven de quem trouxe**. Seis é o teto, e a mesma
bruxa não entra duas vezes.

**Ela ganha nome ao ser falada**, e não ao nascer — é do original, e faz diferença: uma bruxa com quem ninguém
falou não tem nome nenhum. São as duas listas do original, **duzentos e setenta e dois primeiros nomes e
trezentos e noventa e oito sobrenomes**: cento e oito mil bruxas diferentes. Não foram mexidas, porque trocá-las
seria trocar o sotaque do mod.

**E tem uma de cinco caras**, sorteada ao nascer. O corpo é o da bruxa do próprio jogo — o original usa o
`ModelWitch` tal e qual —, e é a pele que muda. É o que faz um coven de seis parecer **seis pessoas** e não seis
cópias.

**Trinta de vida, e não ataca quem não a atacou.** A poção dela é a da bruxa do jogo: no original a conta é a
mesma, feita com os números de 2014, e por isso aqui se copiou a de hoje em vez de traduzir metadata de poção.
Quem a enganar — aceitar e voltar com o coven já cheio — faz dela inimiga, que é o `tricked` do original.

### Desvios declarados

1. **Três dos sete pedidos.** O original pede, além do que está aqui, um **Coração de Demônio**, uma **Bola de
   Cristal**, cinco **Cozimentos Grotescos** e uma **Pedra Necro** — e nenhuma dessas quatro coisas está
   portada. O Coração de Demônio e o Grotesco já estavam escritos como buraco neste documento antes desta
   fatia. Ficam a brigar com aranha, brigar com zumbi e trazer trinta ossos; as outras voltam com os itens.
2. **O familiar virou costura, e não desvio.** No original ela só negocia com quem tem um familiar acordado, e
   os familiares são a fatia seguinte. Em vez de tirar a regra, ela está num método — o `temFamiliar` — que
   hoje responde sempre que sim. Quando os familiares chegarem, **é essa a única linha que muda**, e nada
   precisa ser reescrito.

### Um erro meu que vale ficar escrito

**Escrevi as falas dela em português de Portugal.** "O teu coven", "volta quando tiveres", "traz-me trinta
ossos", "tu me enganaste". A regra deste porte é português do Brasil, e eu a furei — reescrevi tudo, e conferi
que o arquivo de língua continua com as mesmas três mil cento e dezenove chaves, para não ter perdido nada no
caminho. Fica escrito porque o deslize é fácil justamente num texto de personagem, onde a tentação de "soar
antigo" puxa para o lado errado.

**Guardas:** `OccultaCovenGameTest`, com seis — as duas listas de nomes inteiras, o nome que só vem quando
falam com ela, o caminho completo de um pedido de buscar (pede, aceita, de mão vazia recusa, com os trinta
ossos entra e fica com eles), o teto de seis, a mesma bruxa que não entra duas vezes, e os três pedidos que há.

**A foto:** as cinco caras lado a lado. Uma sozinha não diria nada.

## Fatia E — a Cabana da Bruxa

A menor peça da aldeia e a que diz mais. Por fora é uma casa de aldeia: pedregulho em baixo, tábua em cima,
porta e duas vidraças. Por dentro tem um **caldeirão cheio**, uma **tora de sorveira**, um vaso — e uma
**bruxa do coven**.

**É a peça que a fatia anterior destrancou.** No original a cabana nasce com uma bruxa dentro, e até a Bruxa do
Coven existir não havia quem pôr lá.

**As três madeiras são de propósito.** O original usa tábua comum nas paredes, tábua de metadata **1** no
telhado e de metadata **2** nos cantos: carvalho, **abeto** e **bétula**. É o que faz a cabana destoar de leve
das casas à volta sem gritar — que é exatamente o que uma casa de bruxa numa aldeia devia fazer.

**Peso 10, e zero ou uma por aldeia** — é o que o handler dela diz:
`PieceWeight(classe, 10, sorteio(2))`. O zero é de propósito, e é o que faz valer a pena procurar.

**Dois descuidos do original que vão como estão.** O `isTallHouse` é posto no construtor e nunca mais mexido —
a cabana é **sempre** a versão alta, e o ramo da baixa é código morto. E o `tablePosition` é sorteado entre um
e dois e depois testado por `> 0`: o caldeirão, a tora e o vaso aparecem **sempre**.

**E o z anda um**, como no Boticário: o original põe o degrau da porta em `z = -1`, fora da caixa que ele
próprio declara.

**As fotos:** a cabana de fora, onde as três madeiras se leem; e por dentro, sem telhado, com o caldeirão e a
bruxa.

## Fatia F — o Coven do Pântano

**Esta fatia não é porte.** Tudo o que veio antes — inclusive o que foi melhorado, traduzido de outro jeito ou
declarado de fora — saiu do código do Witchery. O Coven do Pântano **não existe nele**: o original dá a Bruxa do
Coven e a deixa numa cabana dentro da aldeia, e mais nada. Isto é um acréscimo, pedido por quem joga, e fica
marcado como tal.

**A ideia e o que ela não podia ser.** O pedido era "uma vila de bruxas no pântano". Aldeia no jogo de hoje
traz aldeão, sino, troca, cama e incursão — e com isso a bruxa viraria vendedora e o **Caçador de Bruxas
perderia o sentido**: a graça do ofício é a bruxa estar escondida. O que se fez foi um **coven**: quatro
cabanas numa clareira fundo no pântano, sem estrada e sem sino, com o terreiro de ritual no meio. Raro.

**O terreiro é de verdade.** Leva o coração do círculo e o **anel de dentro** completo — dezesseis glifos de
giz de Ritual, pelo desenho do `RitualCircles`, que é o do original letra por letra. É o que faz aquilo ler
como chão de ritual e ainda serve aos ritos simples. **Os três anéis seriam oitenta e quatro glifos**, e isso
já é um depósito de giz e não um cenário.

**Quatro cabanas iguais, e é de propósito:** um coven é uma gente só, e quatro casas iguais à volta de um
círculo leem como um lugar. Abeto e carvalho-escuro, pedregulho com musgo, caldeirão e tora de sorveira dentro
de cada uma. **Uma bruxa por cabana.**

**Onde ele nasce:** pântano e mangue, e mais nenhum lugar — e há prova dos dois lados, que o pântano tem e que
a planície não. Um coven numa planície seria um acampamento à vista de todos, que é o contrário do que ele é.
Espaçamento de oitenta trechos com separação de vinte e quatro: raro o bastante para valer procurar.

### A foto, e a lição que ela repetiu

A primeira tentativa plantou o coven com `/place template` na altura do mapa de alturas, e ele saiu
**flutuando sobre a copa das árvores**: num pântano fechado, a altura de superfície *depois* das árvores é o
alto delas. A estrutura de verdade usa a altura de **antes** e abre o mato com o `terrain_adaptation` — mas
isso só se vê deixando-a nascer.

É a mesma lição da muralha, e é a segunda vez nesta conta: **o que se põe à mão não prova o caminho que o jogo
usa**. A prova passou a procurar um coven gerado, e na foto ele está assentado no chão, com o mato aberto à
volta.

**Guardas:** `OccultaSwampCovenGameTest`, com três — a estrutura de uma peça só (ela não cresce como aldeia), o
pântano que tem e a planície que não, e o molde com a clareira inteira.

## Fatia G — os Familiares

A fatia que **destranca três coisas que estavam escritas neste documento como buraco** desde muito antes dela.

**Um familiar não é bicho de estimação.** Ele <b>leva pancada por quem o tem</b> — um por cento do golpe de
longe, **dez por cento** a menos de vinte e quatro blocos —, e **ele não morre**: se fosse morrer, quem o tem
leva o dobro da própria vida e cai no lugar dele. Sem dono por perto, o bicho fica com um de vida e continua.
É o vínculo do original, e é o que explica por que ter um custa alguma coisa.

**E ele não vai com quem morre:** a morte desfaz o vínculo, e o bicho fica no mundo, solto.

### Os três, e o que cada um destranca

| bicho | maestria | o que ela faz aqui |
| --- | --- | --- |
| **gato** | maldição | o escuro de uma maldição passa de **dois** minutos para **cinco** |
| **sapo** | cozimento | sai **um frasco a mais** de cada caldeirão |
| **coruja** | vassoura | mais empurrão, mais teto e freio — a vassoura veio na fatia dela |

As duas primeiras fechavam buracos que já estavam escritos: a Maldição da Cegueira dizia *"o familiar de
maldição não dobra o escuro... não estão portados"*, e o engarrafar dizia *"quem engarrafa aqui é sempre alguém
que está aprendendo"*. Os dois passam a responder.

**A coruja entra mesmo sem destrancar nada**, porque é um dos três do original e porque, no dia em que a
vassoura vier, ela já está aqui — a pergunta existe e é só ligá-la.

### E a costura da Bruxa do Coven fechou com uma linha

A fatia dela deixou o `temFamiliar` a responder sempre que sim, com a nota de que seria **a única linha a
mudar** quando os familiares chegassem. Foi exatamente isso: o método passou a perguntar de verdade, e mais
nada se mexeu.

**A prova do coven quebrou com essa mudança, e quebrou certo** — ela negociava sem familiar nenhum. Arranjou um
sapo antes de negociar, e ganhou uma irmã: a que confere que **sem familiar a bruxa não fala de negócio**.

### Desvios declarados

1. **O gato é o do próprio jogo.** O original tem um `EntityWitchCat` seu, mas aceita também a <b>jaguatirica
   do jogo</b> — e quem herdou esse papel hoje é o gato, que até tem a variante **preta** que um gato de bruxa
   pede. Portar um bicho novo cuja única diferença é ser sempre preto, num jogo que já tem gatos pretos, seria
   peso sem ganho.
2. **O macaco-voador fica de fora.** Ele está no original, mas **não é familiar**: não entra no
   `canBecomeFamiliar` nem dá maestria nenhuma. É outra coisa, para outra fatia.
3. **As animações são as do jogo, não as do original.** Lá há contas sobre os campos do bicho de 2014 — a
   coruja abre as asas quando voa. Aqui ficam a cabeça que acompanha quem olha e o passo. É menos do que o
   original fazia.

**O espelho, outra vez.** Os dois modelos ligam o `mirror` em cada parte, mas só vale onde é ligado **antes**
das caixas: na cabeça e nas pernas do sapo, e na cabeça da coruja. Nas outras partes é o espelho morto que este
porte já encontrou às centenas, e aqui ele está só onde de fato valia.

**Guardas:** `OccultaFamiliarGameTest`, com cinco — os três números do original e as três listas de doze nomes;
só domado e só dos três feitios vira familiar (e o **lobo** é a prova pelo avesso: domável, do jogo, e não
serve); vincular dá nome e é **um de cada vez**; cada bicho destranca a sua maestria **e só a sua**; e quem cai
perde o fio sem perder o bicho.

**A foto:** os três lado a lado — o gato do jogo e os dois do ofício.

## Fatia H — a Invocação do Ars Magica 2

A **Invocação** é a peça 61 do Ars Magica 2, e é a primeira deste porte que põe um **bicho** no mundo em vez de
pôr um efeito em quem já está ali. Lançada num ponto do chão, ela traz um **esqueleto com arco**; lançada em
cima de alguém, traz o esqueleto no lugar dele e **o resto da etapa cai na invocação**, que é o
`applyStageToEntity` do original — uma frase que diga *Invocação + Cura* cura o que acabou de chegar.

**Ela custa 400 de mana e 120 de desgaste**, e puxa **Fim** e **Vida**, com 0,01 de deslocamento. São os
números do `Summon.java`.

### O que faz dela uma invocação, e não um esqueleto nascido na cara do mago

Esta é a parte que é fácil portar errado, e que esteve errada neste arquivo antes de haver prova. O original
não se contenta em criar o bicho: ele chama o `makeSummon_PlayerFaction`, que **troca o lado dele**.

- **A lista de alvos é limpa e refeita.** Bate em quem bater nela (`HurtByTargetGoal`), e procura **monstro,
  geleia e ghast**.
- **Mas nunca outra invocação.** É o `SummonEntitySelector`, uma linha só no original, e é o que impede duas
  invocações do mesmo mago de se matarem.
- **E ela anda atrás de quem a chamou.** É o `EntityAISummonFollowOwner`, traduzido inteiro no
  `SummonFollowOwnerGoal`: segue a partir de dez blocos, desiste aos vinte, e **teleporta** para a moldura de
  cinco por cinco em volta do dono quando o caminho não deu e ela está a doze blocos ou mais. Sem isso, uma
  invocação fica presa atrás da primeira parede.

**Um desvio aqui, declarado:** no jogo de 2014 havia também de se trocar o `EntityAIAttackOnCollide`, porque lá
a vontade de bater trazia o alvo **dentro** dela. Hoje quem escolhe o alvo é só a lista de alvos, e por isso a
troca de lado é essa lista e mais nada — a vontade de bater que o bicho já tem bate em quem a lista der.

### O prazo, e a trela

**Quatro mil e oitocentas batidas**, que são quatro minutos, multiplicadas pelo modificador de Duração — um
deles dá 10560. Acabado o prazo, ela **morre**: cinco mil de dano do `unsummon`, que é mais do que qualquer
bicho tem. Morre, e não desaparece calada — é o `DamageSourceUnsummon` do original.

**E o tipo de dano novo passa por cima da invulnerabilidade, e só dela.** O original liga uma única bandeira no
`DamageSourceUnsummon` — a que vale em modo criativo —, e **não** liga a que ignora armadura, que os outros
danos dele (Fogo, Gélido, Relâmpago, Vento) ligam. Com cinco mil de dano a diferença não se vê em bicho nenhum,
mas a bandeira é a dele.

E ela morre também se quem a chamou **morreu, saiu do mundo, ou está a mais de trinta blocos** — os 900 ao
quadrado do `AMEventHandler`. É a trela, e é o que faz a invocação ser companhia e não um bicho largado no
mapa.

**Menos se ela tiver nome próprio.** Aí ela não morre: **se solta**. Perde o dono, perde o prazo, e fica no
mundo como bicho de ninguém. É o `revertAI` do original, com uma diferença declarada: lá as vontades antigas
voltam de uma cópia que ele guardou antes de as trocar, e aqui ela se solta com as vontades que tem.

**O relógio é do mundo, e não do bicho.** O original conta o prazo na batida de cada criatura; aqui se olha de
**vinte em vinte batidas**, para todo o mundo de uma vez. Varrer todo bicho a cada batida é caro para nada: o
prazo tem 4800 batidas e a trela tem trinta blocos, e um segundo de folga em qualquer dos dois não se vê.

### O teto é um, e cheio ela ainda gasta a mana

No original o teto sobe para dois com a perícia **`ExtraSummon`** — que está no ramo dos **Talentos**, em
(230, 210). Esse ramo **não está portado**, e não está por uma razão que já está escrita neste documento: a
árvore deste porte só sabe guardar **peças de feitiço**, e os Talentos não são peças. Fica o um.

**E com o teto cheio ela ainda dá certo.** O original se lê ao avesso: o `applyEffectBlock` dele só devolve
`false` quando a criatura **não nasceu**; com o teto cheio ele manda a frase *"Você não pode ter mais
invocações."* e devolve `true` — ou seja, **a mana se gasta**. É castigo por lançar sem olhar, e é de propósito.

### O que fica de fora, declarado

**O Filactério de Cristal e o Invocador.** No original, o que vem não é sempre um esqueleto: põe-se um
**filactério** na receita do feitiço, e o bicho que está preso nele é o que nasce. Só que encher um filactério
exige o `TileEntitySummoner` — uma **máquina ligada à rede de energia do Ars Magica 2**, que é a metade do mod
que este porte não trouxe. Sem ela, o filactério é um item que ninguém consegue usar.

O código do original já responde a isso sozinho: *"se o tipo não foi escrito, é `Skeleton`"*. **Este porte fica
nesse padrão**, que é o mesmo que quem joga o Ars Magica 2 vê enquanto não tem a máquina. Inventar outro jeito
de encher o filactério seria acréscimo, e acréscimo é outra fatia.

Ficam de fora, pela mesma razão: a **galinha de batalha** e a **vaca do inferno**, que são dois bichos próprios
do original que só aparecem quando se invoca galinha ou vaca com modificadores certos — e não há como invocar
galinha ou vaca sem filactério.

### Onde ela está na árvore

Ramo da **Defesa**, ponto **verde**, em **(267, 135)**, pendurada no **Vida por Mana**. São as coordenadas do
`SkillTreeManager`, lidas lá.

**Guardas:** `ArcanaSummonGameTest`, com oito — o esqueleto com arco que não mira quem o chamou; que ele caça o
zumbi ao lado; o teto de um e a segunda que só avisa; que não se invoca em cima de uma invocação; o prazo e a
Duração; a morte ao fim do prazo; a trela, com a sem nome que se desfaz e a batizada que se solta; e o lugar
dela na árvore.

**A foto:** a aba de **Defesa** do Óculus, com o Vida por Mana e a Invocação acesos e a linha entre os dois —
que é o que diz que ela está no lugar certo e com a figura certa. A figura é a do original, o
`components/Summon.png`.

## Fatia I — o Necromante (acréscimo)

**Esta fatia não é porte.** Tudo o que está aqui foi acrescentado, e está escrito separado por isso: quem vier
conferir o Ars Magica 2 não vai achar nada disto lá.

Ela existe por uma pergunta que a [fatia anterior](#fatia-h--a-invocação-do-ars-magica-2) deixou no ar. A
Invocação do original é boa e fica pequena: traz **um** bicho, **nu**, e sempre o **mesmo** — e fica assim
porque a parte que a abria, o Filactério de Cristal, precisa de uma máquina que este porte não trouxe. Metade
da ideia dela ficou sem uso.

O acréscimo devolve as três coisas que faltavam — **quantos**, **o quê** e **com o quê** —, e devolve cada uma
por um caminho diferente, de propósito.

### Quantos: a Legião

Um modificador novo. Cada cópia sobe o teto em **um**, e **dobra** a conta da etapa. Uma Invocação já custa 400
de mana: dois soldados custam 1600, três custam 3200.

**O preço é o maior do mod, e é o ponto.** Um exército tem de doer, senão não é uma escolha — é só o que se faz
sempre. O que se quer é que o mago pese se quer três esqueletos ou se quer poder lançar outra coisa no mesmo
minuto.

No original isto é a perícia **`ExtraSummon`**, que sobe o teto de um para dois e **não é peça de feitiço**: ela
é do ramo dos **Talentos**, que não está portado porque a árvore daqui só guarda peças. Trazê-la como peça é a
única forma de ela caber — e a figura é a dela, a do próprio Ars Magica 2.

### O quê: Erguer os Mortos

Uma essência nova, irmã da Invocação: traz **zumbi com espada** em vez de esqueleto com arco.

**Nem um é melhor que o outro**, e isso foi medido: mesmo preço, mesmo desgaste, mesmo prazo, mesma vaga. Um
atira de longe, o outro bate de perto, e é só isso. Se um fosse melhor, a escolha não seria escolha.

Afinidade só do **Fim**. A Invocação do original puxa Fim **e Vida**, e a Vida está lá porque o que ela traz é
uma criatura viva; um morto que se ergue não é.

A figura é a da Invocação do original com o **miolo tingido de verde** — a moldura dourada fica, que é o que
faz as peças serem da mesma família. Um acréscimo não tem figura de origem, e inventar uma de fora destoaria
de 117 outras.

### Com o quê: a panóplia, que não se compra

E esta é a parte de que a fatia gosta. **A armadura, a arma e a montaria não são peças**: elas vêm da
**Afinidade** de quem chama.

Porque é o que o Ars Magica 2 faria. A Afinidade dele não se compra — ela **pega**, sozinha, de tanto lançar a
mesma coisa —, e já é assim que o mod dá a respiração a quem nada e a resistência a quem anda no fim. **Um
necromante não vira necromante comprando uma perícia; ele vira de tanto chamar mortos.**

O que conta é a profundidade no **Fim**, que é a Afinidade da própria Invocação:

| Fim | o que vem |
|---|---|
| abaixo de 0,25 | nada: nu, como no original |
| 0,25 | couro, e a espada de pedra |
| 0,50 | ferro |
| 0,75 | diamante |
| 0,90 | e a **montaria**: um cavalo esquelético, já montado |

**O arco não sobe de grau**, porque não há arco de ferro. O que o esqueleto ganha com a panóplia é a armadura —
e é o que faz dele um atirador que **aguenta**, em vez de um atirador melhor. A arma nua vem sempre, nos dois
casos: é ela que diz o que cada um é.

**Nada do que vem vestido cai.** Uma invocação que largasse diamante ao fim do prazo seria uma fábrica de
diamante, e o prazo é de quatro minutos.

**E um elmo não deixa queimar ao sol.** É regra do próprio jogo, e não foi preciso escrever nada para ela
valer: a partir do couro, o exército do necromante deixa de evaporar ao meio-dia. Quem está raso ainda vê o
esqueleto pegar fogo, que é o que o Ars Magica 2 sempre fez.

### Três decisões que ficam escritas

1. **A montaria não ocupa vaga.** Ela tem o mesmo prazo e a mesma trela do que a monta, mas não conta no teto —
   fazê-la contar seria dizer que um necromante a cavalo tem metade do exército.
2. **O teto do original continua sendo o teto do original.** Sem Legião nenhuma, é **um**. O acréscimo não mexe
   em nada de quem não o usa.
3. **O apego ganhou um campo**, a bandeira de montaria, e ele é **opcional no disco**: o que foi guardado antes
   desta fatia volta como soldado, que é o que era.

### Onde elas estão na árvore

Penduradas na Invocação, em (267, 135): **Erguer os Mortos** logo acima, em (267, 90), de verde; e a **Legião**
ao lado, em (312, 90), de **vermelho** — o último degrau, que é onde um exército pertence.

**Guardas:** `ArcanaNecromancyGameTest`, com oito — o zumbi com espada que não mira quem o ergueu; a Legião que
sobe o teto um por cópia e a quarta que não entra; o preço que dobra de cada vez; os quatro degraus da
panóplia; o ferro no meio do caminho; o arco que nunca sobe; a montaria que não ocupa vaga; e o lugar das duas
na árvore.

**As fotos:** a Invocação **nua**, que é a do original, e ao lado o exército — três a cavalo, de diamante, onde
o original deixa um esqueleto de mãos a abanar. E a prova de tela **confere o que fotografa**: se faltar um
soldado, uma montaria ou um cavaleiro, ela falha em vez de tirar uma foto ruim em silêncio — o que foi
exatamente o que aconteceu da primeira vez que ela correu.

## A Vassoura, e a dívida da coruja (2026-10-02)

A fatia dos Familiares entregou a **coruja** com a maestria dela escrita e **sem nada para destrancar** — o
javadoc dizia *"a vassoura não está portada"*, a tabela do ramo dizia *"nada ainda"*, e ficou assim. Esta fatia
é essa dívida paga.

### O que é a vassoura, no original

São **três itens e um bicho**, e vale separar porque é fácil confundi-los:

| | o que é | de onde vem |
| --- | --- | --- |
| **Vassoura** | ingrediente, e só | bancada: dois gravetos sobre três mudas de espinheiro-alvar |
| **Unguento do Voo** | bebida que envenena | Caldeirão de Pote, seis coisas |
| **Vassoura Encantada** | o item que se põe no chão | **Rito da Infusão do Céu** |
| **a vassoura posta** | o bicho que se monta | pôr a encantada no chão |

**A vassoura comum não voa**, e isso é do original: ela existe para ser oferecida no círculo. E o Unguento do
Voo, bebido, dá **Veneno III por um minuto** — é um mau negócio de propósito, porque ele também não é para se
beber.

### A conta do voo, que é o que faz dela uma vassoura

O `EntityBroom` tem uma conta própria, e é dela que vem a sensação de montar uma vassoura em vez de pilotar
um avião:

1. **Ela acelera devagar e para devagar.** O empurrão começa em **0,07** e sobe até **0,35**, um centésimo da
   diferença de cada vez, enquanto se vai ganhando velocidade; soltando, desce pelo mesmo caminho. Não há botão
   de turbo: há inércia.
2. **O teto é de velocidade, não de aceleração**: **0,9** sem nada.
3. **Subir e descer saem do olhar, com zona morta.** A inclinação só conta fora da faixa de **−0,5 a 0,2**, e
   descer conta **pela metade**. É o que impede a vassoura de mergulhar a cada olhadela.
4. **Quem vai nela chega inteiro**, por mais alto que tenha vindo.

### E é aqui que a coruja serve

Quem tem a **maestria da vassoura** — a do familiar coruja — ganha **0,2 de empurrão** e **0,3 de teto**, e
passa a **frear sozinho** ao largar o acelerador em vez de deslizar. É o `riderHasOwlFamiliar` do original, lido
**uma vez, ao montar**, como lá.

A prova `theOwlMakesHerFasterAndGivesHerBrakes` é onde a dívida fica paga: ela vincula a coruja, monta, e
confere que a vassoura sabe disso.

### A tinta, e o que o desenho faz com ela

Tinta na mão **pinta as cerdas** em vez de montar. E há duas coisas aí que só uma foto mostra:

- **Só as cerdas levam a cor**; o cabo é sempre madeira. Por isso o modelo tem **duas** partes e não uma.
- **A tabela de cores é a da lã de 2014**, e não a do jogo — as dezesseis do `fleeceColorTable`, copiadas uma a
  uma. Sem tinta, a vassoura é **castanha**, que é a cor 12.

O modelo são **dez caixas**: o cabo e **nove cerdas** em leque, cada uma com a sua inclinação, nenhuma igual à
outra. O espelho do original é o **morto** de sempre — ligado nas dez partes, mas sempre depois das caixas —, e
por isso nenhuma o leva aqui.

**Um engano do original que fica:** a nona cerda é a única que ele desloca de `-0.5333334` em vez de `-0.5`.
Um terço de um dezesseis avos, que ninguém vê. Fica.

### O que ficou de fora, declarado

1. **A Infusão do Céu.** O rito do original faz **duas** coisas: dá a Vassoura Encantada **e** infunde quem o
   faz com a Infusão do Céu, que é um ramo inteiro de poderes. Só a vassoura está portada; a infusão precisa do
   sistema de infusões, que é outra fatia.
2. **O Cozimento do Voo Alto** (`riderHasSoaringBrew`), que soma metade do que a coruja soma — 0,1 de empurrão
   e 0,3 de teto. A conta dele **está escrita** no `BroomEntity`, pronta, para o dia em que o cozimento vier.
3. **A poção que o Unguento pede.** O original quer uma **Poção de Rapidez longa e de arremesso**, pelo número
   de poção da 1.7.10 (8258). A tabela do Caldeirão de Pote deste porte casa por **item**, e uma poção de hoje
   leva o que ela é num **componente** e não no item — por isso aqui entra a poção de arremesso, qualquer que
   seja.

### Uma coisa que a foto apanhou e o verde não

A primeira foto desta fatia saiu com **três sombras e nenhuma vassoura**. O original translada **um bloco para
cima antes de virar o modelo**, e eu tinha posto 0,375 — a vassoura estava enterrada, inteira, debaixo do chão.
Nenhuma das seis provas de servidor diria isso.

E a segunda saiu com as vassouras **de pé**, o que me pareceu errado até eu virá-las de lado: elas estavam
deitadas, e eu estava olhando pela ponta. A foto que fica é a de lado, por isso.

**Guardas:** `OccultaBroomGameTest`, com seis — os números da conta de voo; a tinta que pinta em vez de montar;
montar de mão vazia; **a coruja**; a encantada que fica no chão quando a vassoura se desfaz; e a vassoura que
varre o giz.

**A foto:** três de lado — por pintar, de roxo e de vermelho.

## Os ritos que faltavam — primeira leva: os que chamam (2026-10-02)

O Witchery tem **79 ritos**; este porte tinha **22**. Esta é a primeira leva do resto, e são os quatro que não
pedem nada que o porte ainda não tenha.

### Chamar uma criatura, e o teto que ele olha

O `RiteSummonCreature` não chama nada antes de **olhar o teto**. São três camadas de sete por sete em cima do
círculo, com os cantos de fora, e o que estiver sólido ali conta — **mais de um estorvo e o rito desiste**,
devolvendo o que se ofereceu.

E o bloco **do meio conta por cem**. Ou seja: uma laje em cima do glifo já chega para ele recusar. É o que
impede alguém de chamar um Wither dentro de uma caixa de obsidiana, e é metade do rito.

**Um engano do original que fica:** ele percorre o desenho até o **penúltimo** z, e por isso a fila de trás
nunca é olhada. O teto que ele mede é de sete por **seis**.

Com ele vieram dois ritos: a **Bruxa** (a do próprio jogo, não a do coven — dois mil de poder, anel de
dezesseis no de fora) e o **Wither** (caveira, Vapor de Diamante, pérola, **um aldeão vivo**, quatro mil de
poder).

### Chamar os Bichos, que não cria nada

O `RiteCallCreatures` é o mais bonito dos quatro, porque ele **não faz bicho nenhum**: ele **traz**. De
sessenta em sessenta batidas olha **um oitavo** do mundo em volta — uma caixa de cento e vinte e oito blocos
num dos oito cantos, quatro por baixo do círculo e quatro por cima — e teleporta até **dois** dos bichos que
achar ali. Rodando os oito cantos, ele acaba por varrer tudo o que há à volta.

Duzentas e cinquenta voltas, e **três bruxas**: sozinha, ninguém chama o mato inteiro.

### A Chuva de Sapos

Quatro raios, um de trinta em trinta batidas, e ao **quarto o céu fecha** — de cinco a quinze minutos de chuva.
Daí em diante caem **sapos**, de oito a dezessete de cada vez, num anel de cinco a dezesseis blocos e de oito a
catorze acima do chão.

**Os sapos têm hora para acabar**: meio minuto, e somem. É o `setTimeToLive` do original, e sem ele a
brincadeira deixava o mapa cheio de sapos para sempre — são dezessete de cada vez, por duzentas voltas.

O sapo é o **do porte**, o da fatia dos familiares. Não foi preciso trazer bicho nenhum.

### O sacrifício vivo, que faltava à maquinaria

Dois destes ritos pedem um **aldeão vivo** dentro do círculo, e o porte não sabia pedir vivos. O
`SacrificeLiving` entrou, com uma coisa dele que vale ser dita: **ele não entra na conta de antes**. O
`isMatch` do original devolve sempre que sim, e por isso o círculo aceita começar sem o bicho lá — quem
descobre que falta é o **passo**, e aí o rito desiste e devolve o resto.

É de propósito, e é o que faz um rito começar e morrer à vista de quem o fez.

### E os recados de recusa

O `RiteRegistry.RiteError` do original: um tambor, e a frase em vermelho para quem começou o rito. Sem ele, um
rito que recusa parece um rito quebrado — e metade dos que recusam, recusam por coisas que se arranjam: um
teto tapado, um coven pequeno, um aldeão que saiu andando.

### O que ficou de fora desta leva

O **Imp** e o **Demônio**, porque são bichos do Witchery que ainda não estão portados — e com eles os quatro
ritos que os chamam.

**Guardas:** `OccultaSummonRitesGameTest`, com sete — os quatro na lista; o bicho que vem com o céu livre; a
laje em cima do glifo que já chega para recusar; o coven pequeno demais; o porco que é **trazido** e não
criado; o sapo com prazo; e o vivo que falta.

**Uma prova ao lado ficou instável por causa desta.** A `theCatcherBringsTheReckoningDown` diz no próprio
comentário que *"a suíte corre num mundo só, e o que as provas ao lado puserem também entra na conta"* — e uma
classe de provas nova muda onde as arenas caem. Ela falhou uma vez e passou na seguinte. **Não foi
estabilizada à força**: está apontada, com as outras duas instáveis do projeto.

## Os ritos que faltavam — segunda leva: a Praga, e o chão que faltava ao anel (2026-10-02)

Esta leva traz **um** rito, e dois consertos na maquinaria por baixo dele que valem mais do que ele.

### A Maldição da Praga

O `RiteBlight`: um anel de **oitenta blocos** de raio que cresce a partir do círculo e mata o que encontra.
Quem está na faixa fica **cego** dois minutos; um aldeão em cada dez vira **zumbi**, com a mesma cara e o
mesmo tamanho; uma vaca em cada vinte vira **cogumelada** e um bicho em cada três **morre**; e o chão **seca** —
a grama vai embora, a flor e a plantação viram arbusto morto, a terra arada vira areia, e o que era grama,
terra ou micélio vira areia ou terra pelada.

**A faixa é só a do anel.** O rito compara a distância com o anel de agora e com o de antes, e por isso quem
está no miolo já percorrido não apanha a praga outra vez. Sem isso, ficar parado no meio custava uma praga por
volta.

### O chão que faltava ao anel

O `drawPixel` do original procura o primeiro sólido com ar em cima **subindo e descendo** até `height`. Este
porte só subia — a metade de baixo nunca foi escrita, desde a fatia dos círculos.

O efeito era invisível no plano e grosseiro numa encosta: **descendo um barranco, o anel simplesmente não
tocava no chão**. Os dois ritos que já herdavam da maquinaria — a Fertilidade e a Maldição da Cegueira —
estavam com o mesmo buraco, e ficam consertados de carona.

### E o gato atravessa a maquinaria

O `enhanced` do original é a **maestria da maldição**, a do familiar gato, e ele não muda o que um rito de
maldição faz: muda **quanto**. Na Praga, o chão seca um em cada **quatro** em vez de um em cada cinco.

A bandeira não existia neste porte — quando a maquinaria foi escrita, os familiares não estavam portados.
Agora ela atravessa o anel inteiro e chega a quem herda dele. É a **segunda** coisa que o gato destranca, e a
primeira que se vê no chão.

### O que ficou de fora desta leva

O **Inferno na Terra**, que é o outro grande dos que crescem: ele pede o **Coração de Demônio** e a **Pedra de
Caminho**, e nenhum dos dois está portado. A maquinaria dele já está pronta — falta só o que ele come.

**Guardas:** `OccultaBlightGameTest`, com cinco — a Praga na lista; o chão que seca em areia e terra, e que
**não** seca todo (é sorteio, não varredura); a pedra que não seca; o aldeão que vira zumbi; e os dois números
do gato.

### Um erro meu que vale ficar escrito

A suíte de tela desta sessão **falhou**, e a culpa foi minha: rodei `compileJava` **enquanto ela corria**. É
exatamente a regra que este documento já tinha — *"não compilar enquanto a suíte de tela corre"* —, escrita
aqui depois de o mesmo erro ter acontecido em 2026-09-26. Repeti-o.

E o modo como eu a corria escondia o estrago: `./gradlew runClientGameTest | tail -3` devolve o código de
saída do `tail`, e não o do Gradle. **A suíte passou a correr com o log inteiro guardado.**

## Os ritos que faltavam — terceira leva: as Maldições (2026-10-03)

Esta leva não traz ritos: traz um **sistema**, e os nove ritos que mexem nele.

### Uma maldição não é um efeito de poção

É um **número guardado em quem a tem**. Não acaba sozinha, não sai com leite, não aparece no canto da tela, e
não se vê de fora. Só outro rito a tira — e tirar é uma **aposta**.

São cinco, e cada uma morde de um jeito:

| maldição | o que faz |
| --- | --- |
| **Azar** | um em vinte: pancada, lentidão, fraqueza, cegueira — e **largar o que se tem na mão** |
| **Fervura** | pega fogo sozinho, mas **só em terra quente** e com o céu aberto |
| **Pesadelo Acordado** | o **Pesadelo** aparece, acordado, à procura de quem o tem |
| **Loucura** | vê bichos que **não existem**, e ouve coisas que não estão lá |
| **Afundar** | dentro da água, descer é mais rápido e subir é mais devagar |

**O grau manda em tudo**: quantas vezes, quão forte, e **quantas opções** o azar tem para escolher — duas no
grau um, seis a partir do cinco. A sexta é largar o que se tem na mão, e é a que dói.

### As visões, que são a melhor ideia do original

A Loucura não chama bichos: chama **visões**. Uma tem a cara de um creeper, outra de uma aranha, outra de um
zumbi — e são desenhadas com o **modelo e a pele do próprio jogo**, sem um pixel de diferença, porque é disso
que depende acreditar nelas.

E elas **não são nada**:

- **não fazem dano** — o ataque devolve que acertou e não tira nada;
- **não levam dano** — bater nelas é bater no ar;
- **apagam-se sozinhas** — um em quinze batidas perdem um de vida, e somem;
- e **o barulho é só para quem as vê**. O chiado da aranha e o gemido do zumbi vão num pacote **a uma pessoa
  só**. Quem estiver ao lado não ouve nada.

É isto que faz delas horror e não bicho: vê-se um creeper vindo, corre-se, e no meio da fuga ele desaparece —
e mais ninguém viu nada.

### Tirar uma maldição é uma aposta

Esta é a parte que é fácil portar errado, porque o caminho óbvio é fazer tirar ser sempre tirar. No original
não é:

| o rito contra a maldição | o que sai |
| --- | --- |
| mais forte | sai — menos **uma vez em vinte**, em que **sobe um grau** |
| mais fraco | **sobe um grau** — a não ser uma vez em quatro, em que sai |
| igual | sai três vezes em quatro; na quarta, **sobe** |

Quem tenta tirar uma maldição de grau cinco com um rito de grau um **quase sempre a piora**. É por isso que os
ritos de tirar também querem coven e gato: não para pôr, para **conseguir tirar**.

### E o gato, outra vez

A maestria da maldição soma **um grau** — ao pôr e ao tirar. O coven soma **um** a partir de três bruxas e
**dois** com seis. Um coven cheio com gato põe uma maldição de grau **quatro** onde uma bruxa sozinha põe uma
de grau um. É a terceira coisa que o gato destranca neste porte.

### O Cozimento do Grotesco

Quatro dos cinco ritos de amaldiçoar o pedem, e por isso ele veio junto. Bebe-se, e por **um minuto** nada de
vivo consegue chegar a quatro blocos de quem o bebeu — tudo é empurrado para trás. Chefes, golens e bruxas não
se empurram; o quarto que o original poupa é o demônio, que não está portado.

Faz sentido que seja ele o ingrediente das maldições: para amaldiçoar alguém não é preciso força, é preciso
que **ninguém chegue perto do círculo**.

### Um engano do original que fica

**O Afundar não pega em gente.** O `handleCurseEffects` guarda o trecho inteiro atrás de um
`!(entity instanceof EntityPlayer)` — e **dentro** dele há um ramo escrito para jogador que nunca pode correr.
O autor quis que pegasse em gente e escreveu o contrário.

O porte guarda o engano, e os dois ritos que a põem e a tiram ficam na mesma — também ficam no original. A
prova `sinkingDoesNotTouchPeople` é onde isto está escrito.

### O que ficou de fora

A **Maldição da Fervura** (pôr). Ela pede **Sangue Infernal**, que sai da Destilaria a partir de um **Coração
de Demônio** — e o demônio não está portado. O rito de **tirar** a Fervura veio, porque esse não o pede: quem
apanhar a fervura por outro caminho tem como se livrar dela.

**Guardas:** `OccultaCurseGameTest`, com seis — as cinco maldições e os nove ritos; o número que fica e que não
abaixa; **o rito fraco que piora mais do que tira** (duzentas voltas, porque o que se mede é a tendência); o
forte que quase sempre tira, mas não sempre; o Afundar que não pega em gente; o Grotesco que empurra; e a
visão que não machuca, não se machuca e se apaga.

## Os ritos que faltavam — quarta leva: os que mexem no próprio círculo (2026-10-03)

Quatro ritos, e os três primeiros são as peças que faltavam para o círculo ser uma **ferramenta** e não só um
lugar onde se oferece coisa.

### Empurrar e puxar

Os `RiteProtectionCircleRepulsive` e `RiteProtectionCircleAttractive` são o **mesmo rito com o sinal
trocado**, e por isso aqui são um só. O de **Proteção** empurra tudo para fora de quatro blocos; o de
**Aprisionamento** puxa tudo de volta para dentro. Os dois comem **0,8 de poder de altar por batida** e correm
**para sempre**, até o altar secar.

**Gente não se mexe, e o dragão também não.** É do original, e é o que os torna utilizáveis: um anel que
empurrasse quem o fez seria uma armadilha para o dono.

E o de puxar **só puxa quem está na borda**, a partir de raio menos um — quem já está no meio fica quieto.
Sem isso o anel cuspia os bichos para o centro e eles saltavam para sempre.

**Uma conta esquisita do original que fica como está.** Ela calcula a direção do empurrão pela distância
elevada à **quarta potência**, confere que não passa de 6⁴ — e então **deita o número fora** e troca-o por um
valor fixo: 0,22 na horizontal e 0,12 na vertical. Ou seja, toda a conta elaborada só serve para decidir o
**sinal**. E no eixo de cima há um engano a mais: os dois ramos do `if` dão o **mesmo** valor, para cima.

### Os minérios que sobem

O `RiteTransposeOres` desce **uma camada de cada vez**, de dez em dez batidas, varre um quadrado de oito
blocos de lado e **arranca** de lá o minério, pondo-o como item em cima do círculo. Trinta camadas, mais cinco
por bruxa, ou até à rocha-mãe.

**E com o coven cheio ele leva dois feitios em vez de um.** É o `covenSize == 6 ? 2 : 1` do original: sozinha,
uma bruxa traz só ferro; com seis, traz ouro também.

### Repintar o giz

O `RiteGlyphicTransformation` é o mais prestável do ofício e o menos espalhafatoso: larga-se giz de uma cor
dentro do círculo e **um anel inteiro muda de giz**. Qual deles muda depende de **quantos gizes** se largou —
um muda o de dentro, dois o do meio, três o de fora.

Sem ele, trocar o giz de um anel de quarenta glifos é quarenta picaretadas e quarenta riscos.

**Só um giz de cada vez**: largando duas cores, ele conta a primeira que achar e ignora as outras. E gasta
**um** da pilha, seja ela de que tamanho for — o resto fica no chão.

O desenho dos três anéis é o do original, e com ele vem o engano de sempre: a varredura vai até o
**penúltimo** z, e a fila de trás nunca é olhada.

### O que ficou de fora desta leva

- **Mudança de Clima**, que pede a **Asa de Coruja**;
- **Casca de Gelo**, que pede o **Coração Gelado** e o **Gelo Perpétuo**;
- **Pedra Espectral**, que pede quatro itens do ramo dos espíritos que ainda não vieram.

Nenhum deles é difícil — é só que o que eles comem ainda não existe.

**Guardas:** `OccultaCircleRitesGameTest`, com cinco — os quatro na lista; o empurrão que pega no bicho e
**não em gente**, e que vai para fora; o puxão que só pega **na borda** e que vai para dentro, sem subir; o
minério que sobe e o ouro que só vem com coven cheio; o giz que repinta; e a recusa quando não há giz nenhum.

## Os ritos que faltavam — quinta leva: o prado, e as bonecas corrompidas (2026-10-03)

### O Poder da Natureza

O `RiteNaturesPower` é **a Praga escrita ao avesso**. De segundo em segundo ele escolhe um ponto ao acaso
dentro do raio, procura o chão, e **enche um círculo de três blocos** com grama — virando pedra, areia e
cascalho em terra viva, e plantando em cima mudas, flores, cogumelos e grama alta. Cento e cinquenta voltas,
mais cinco por bruxa.

Três coisas dele que valem ser ditas:

1. **Ele faz água.** Dois por cento das casas viram água — mas **setenta** por cento se já houver água ao
   lado. É assim que nascem poças em vez de pingos soltos.
2. **A borda é esfarrapada.** Ao riscar cada linha do círculo, uma vez em cinco ele encolhe-a de um lado. É o
   que faz o prado não ter cara de círculo desenhado.
3. **E ele não planta debaixo de folha.** Onde já houver copa, só o chão muda — senão o prado crescia por
   baixo da floresta.

**A lista do que nasce é a dele, com os pesos dele**: a grama alta aparece **seis vezes** na lista de vinte, e
cada flor uma só. É assim que um prado fica com cara de prado e não de canteiro.

### Corromper as bonecas

O `RiteCursePoppets` quebra até **dez** Bonecas de Proteção contra Vodu de quem o vínculo prender — e é assim
que se desarma alguém que se escondeu atrás delas.

**E ele exige a maestria da maldição.** Sem o familiar gato, o rito **recusa** e devolve o que se ofereceu,
com um recado. É o **único rito deste porte que pede um familiar para correr**, e é a quarta coisa que o gato
destranca.

A ordem do original fica: a **primeira** boneca de proteção gasta-se a guardar as outras. Quem se guardou bem
sobrevive ao rito com uma boneca a menos, e não com nenhuma.

### Uma prova da primeira leva ficou frágil, e foi consertada

A `callingBeastsBringsThemInsteadOfMakingThem` passou a falhar ao entrar esta leva. A causa não era o código:
o rito varre **cento e vinte e oito blocos** em volta, a suíte corre num **mundo só**, e as provas das
maldições enchem esse mundo de **porcos às centenas** — o rito trazia os delas e nunca chegava ao da prova.

Ela passou a usar **galinha**, que mais nenhuma prova usa. É a segunda vez nesta sessão que o mundo
compartilhado das provas morde: da primeira foi o Apanhador de Sonhos.

## O Lobisomem (2026-10-03)

O primeiro dos três grandes que faltavam ao Ars Occulta. Esta fatia traz o **bicho** — a licantropia do
jogador, que é um ramo com dez graus e uma demanda, fica para a seguinte.

### Ele não é um monstro: é uma doença

Um lobisomem **não nasce do mundo**. Ele é um **aldeão** que virou, na primeira lua cheia, e que volta a ser
aldeão quando a lua passar — **com a profissão e as trocas que tinha**. É por isso que matar um lobisomem numa
aldeia custa um aldeão, e é por isso que o ofício inteiro o trata como doença e não como bicho.

O aldeão doente é indistinguível por fora: troca, trabalha, dorme. **Criança não vira** — é a única
misericórdia do original.

### Só a prata o fere

E esta é a coisa que define o bicho: **tudo o que não for prata lhe tira um ponto de vida**, por mais
encantada que seja a espada. Com **oitenta** de vida, isso são oitenta pancadas. A prata tira dano a sério —
vez e meia, até quinze de cada vez.

Ele ainda **arromba portas**, caça gente e aldeãos, ganha **dez de armadura** por cima da que tem, e **não
apanha veneno**: tira-o de si de dois em dois segundos.

**A prata vem dele mesmo.** O Pó de Prata cai de lobisomem — um em três —, e a Espada de Prata é uma espada
de ouro com oito pós à volta. O original não tem pudor com a galinha e o ovo: o primeiro lobisomem mata-se a
pancada, oitenta vezes, e os outros com o que ele deixou.

**Desvio declarado, e é melhor do que o original.** Lá a pergunta "isto é prata?" é feita ao **nome do
material** da espada — `"SILVER"`. O jogo de hoje não guarda isso, e por isso aqui a pergunta é feita a uma
**etiqueta**: `thaumcraft:silver_weapons`. Qualquer mod que traga prata pode pôr a espada dele na etiqueta e
ela passa a ferir lobisomem, sem este porte saber nada sobre esse mod.

### O acônito

A planta do mato que já estava portada ganhou **para que serve**: cozida no Caldeirão da Bruxa dá o efeito do
**Acônito**, sessenta segundos, e quem o tem no corpo **não vira** na lua cheia — e um lobisomem que o apanhe
não volta a ser aldeão enquanto durar.

É um efeito que não faz nada por si: existe só para ser **perguntado**. E é a melhor razão que aquela planta
tem para ser plantada ao pé de uma aldeia.

### A lua, que mudou de casa

No jogo de 2014 a fase da lua era `dimensionType.moonPhase(dayTime)`. O jogo de hoje **mudou o tempo de
lugar**: há relógios (`WorldClock`), linhas do tempo (`Timeline`) e marcas, e a lua passou a ser uma linha do
tempo de **192000 batidas** — que são os mesmos oito dias de sempre. A conta fica a mesma, e a fase zero
continua sendo a cheia.

### O modelo

Dez caixas traduzidas do `ModelWolfman`, e a graça delas é que ele **não é um homem com cabeça de lobo**: o
tronco inclina-se para a frente, as pernas são de bicho — coxa e canela em dois pedaços, dobradas ao
contrário —, os braços caem até o chão e há cauda. Ele corre como um lobo e levanta-se como um homem.

Duas coisas do original ficam: o **braço direito nasce meio bloco mais para dentro** do que o esquerdo, e as
pernas têm um **teto na dobra** (`Math.max(..., -0.8)`) que as impede de abrir para trás ao correr.

### O que ficou de fora, declarado

- **A licantropia do jogador**: dez graus, as duas formas (lobo e lobisomem), a demanda dos pedaços de mundo
  visitados, o uivo que chama a matilha. É a fatia seguinte.
- **O virote de prata**, porque dano de longe não conta como prata no original e o virote tinha caminho
  próprio.
- **O Altar do Lobo** e a **Estátua**, que são do ramo da licantropia.

**Guardas:** `OccultaWerewolfGameTest`, com sete — os números do bicho; a pancada de diamante que tira **um**;
a prata que tira a sério; o couro grosso; o veneno que não cola; a fase zero da lua; o acônito que segura; e o
aldeão que vira guardando a profissão.

**As fotos:** de frente, entre um aldeão e um lobo do jogo — o que ele era e o que ele não é; e de lado, que é
onde se vê a perna dobrada ao contrário e a cauda. **A segunda custou três tentativas**: virar um bicho já
posto não vira o corpo dele, só a cabeça, porque o corpo tem conta própria. Quem tem de virar é a câmara.

## O Vampiro (2026-10-03)

O segundo dos três grandes. Como no lobisomem, esta fatia traz o **bicho**; a vampirice do jogador fica para
depois, e o `Vampirism.é()` já está lá como costura — uma linha, e nada mais, quando ela vier.

### Ele é o contrário do Lobisomem em tudo

O lobisomem é um aldeão doente, de força bruta, que a prata resolve. O vampiro é uma coisa que **pensa**: tem
casa, tem rotina, e tem um plano.

1. **De noite**, se não tem aldeia, procura uma a cento e vinte e oito blocos e **vai para lá num sopro de
   fumaça** — sem andar o caminho.
2. **Lá, bebe.** Cada mordida num aldeão tem **uma chance em dez** de ser um gole de verdade: cura-se de
   quatro e conta quatro para o jantar. As outras nove **não fazem dano nenhum**, e é isso que faz uma aldeia
   com um vampiro ficar de pé por semanas em vez de amanhecer vazia.
3. **Cheio** — vinte —, volta ao caixão e **enche um Crisol de Sangue** que esteja a seis blocos. É para isto
   que o crisol existe, e ele estava neste porte desde setembro à espera de quem o enchesse.
4. **De dia** volta ao caixão de qualquer maneira, esquece a aldeia, e **pega fogo** se o sol o apanhar.

### E uma espada não o mata

Esta é a outra metade do bicho, e vem do `checkForVampireDeath`. Ele leva o dano, cai a zero de vida — **e não
morre**. Só o levam:

- **fogo**, venha de onde vier — e o sol é fogo;
- **sufocar** numa parede, ou cair no **vazio**;
- e a mão de outro **vampiro**, de um **lobisomem** ou de um **chefe**.

Quem quiser matar um vampiro com uma espada tem de o prender ao sol. É a coisa mais vampiro que este mod faz.

### Três traduções declaradas

1. **O morto-vivo mudou de casa.** No jogo de 2014 era um método do bicho (`getCreatureAttribute`); hoje é uma
   **etiqueta de dados**, e o vampiro entra em `minecraft:undead`. Com isso a poção de cura fere e a de veneno
   não pega, sem uma linha de código.
2. **Não há etiqueta de chefe** no jogo de hoje. Os dois que há — o dragão e o Wither — ficam escritos à mão, e
   isso está dito no código.
3. **A roupa dele não veio.** O original tem um `ModelVampire` que é o corpo de gente com um conjunto de
   armadura de vampiro por cima; o conjunto é um ramo próprio e não está portado. Fica o corpo de gente com a
   pele do original, que é o que dele se vê de qualquer maneira.

**Guardas:** `OccultaVampireGameTest`, com seis — os números; o gole que cura e conta; o crisol que ele enche;
**a espada que não o mata**; o fogo, a parede e o vazio que matam; e o outro vampiro, que é a única mão viva
que lhe chega.

## A Baba Yaga (2026-10-03)

O terceiro dos três grandes, e o mais curto de escrever — porque ela é uma bruxa do jogo levada ao extremo e
depois **torcida**.

### O que faz dela um chefe não é a vida

São **quinhentos** de vida, mas o que a torna um chefe é o **teto**: **nenhuma pancada lhe tira mais de
quinze**. Não importa a espada, o encantamento ou a poção. São trinta e quatro golpes no mínimo — e com
**magia**, que vale **quinze por cento**, são duzentos e vinte.

E ela **não se deixa alcançar**:

- **Ela salta.** Sempre que o caminho dela fecha — ou uma vez em cinquenta, a esmo —, aparece a oito blocos
  de quem a persegue, pelo caminho do enderman.
- **E não deixa fugir pelo ar.** Quem estiver caindo ou voando apanha **Lentidão VI** por dez segundos, uma
  vez em vinte.
- **Ela atira os cozimentos do ofício** — teias, espinhos, tinta, gelo, infecção — e, duas vezes em três, as
  poções de arremesso do jogo, escolhidas pelo que o alvo está fazendo, como a bruxa do jogo escolhe.
- **E bebe as dela**: resistência ao fogo quando arde, cura quando está ferida, rapidez quando o alvo está
  longe. Enquanto bebe, anda um quarto mais devagar — é o `AttributeModifier` do original, com o mesmo
  número.

### E ela pode ser chamada

Com **dono**, ela deixa de ser inimiga dele e vira outra coisa: de cinco em cinco segundos, se ele estiver a
oito blocos, **larga no chão** os ingredientes do ofício — e **some ao fim de trinta segundos**.

É a Baba da bola de cristal do original: uma **visita**, não uma conquista. Quem a chama não ganha um chefe
morto; ganha cinco minutos de uma velha de mau humor largando pó espectral no quintal.

### Duas traduções declaradas

1. **O modelo é o da bruxa do jogo.** O `ModelBabaYaga` do original é o modelo da bruxa com o chapéu trocado e
   a verruga tirada; o que dela se vê, de perto e de longe, é a **pele** — e essa é a dele.
2. **Os cozimentos que ela atira** são os que este porte tem. O original atira sete, e três deles — o do Sapo,
   o do Hitchcock e o do Definhar — ainda não existem aqui; os outros cinco ficam, com os pesos ajustados para
   a lista ficar do mesmo tamanho.

**Guardas:** `OccultaBabaYagaGameTest`, com sete — os números; **mil de dano que tiram quinze**; a magia que
dói menos; o salto que sai do lugar; o presente que é sempre do ofício; a visita que acaba; a lentidão de quem
voa; e o que ela atira, que não a fere.

## A Pedra de Caminho e o Escravizado (2026-10-03)

Duas das quatro miudezas que faltavam. Vieram juntas porque fecham buracos uma da outra: a Pedra de Caminho é
o que os **ritos de teleporte** comem, e o Escravizado é o que faltava ao **Cozimento da Ressurreição**.

### A Pedra de Caminho: o menor ofício que há no mod

Uma pedra lisa não é nada. O que faz dela uma Pedra de Caminho é **largá-la no chão**, e o resto é geometria
de giz. São três anéis e três pedras:

| O que se larga | Onde | O que acontece |
| --- | --- | --- |
| Pedra lisa | **anel miúdo** (as 8 casas em volta, giz do Alhures) | até **oito** viram **Pedra Presa** ao lugar; os oito glifos **estouram** |
| Pedra lisa, com alguém de pé no anel | o mesmo anel | **uma** vira **Pedra Sangrada**, presa àquele bicho — e custa **quatro mil de poder** ao altar mais perto |
| Pedra presa (de lugar ou de bicho) | **anel pequeno** (os 12 glifos de raio dois) | ela **se gasta** e leva **tudo o que estiver a quatro blocos** do meio: bicho, gente e item largado |

Não precisa de altar para prender a um lugar, não precisa de ritual, não precisa de bruxa. É a primeira coisa
que alguém faz com giz do Alhures antes de saber para que ele serve — e é de propósito.

Os quatro mil de poder da Pedra Sangrada, ao contrário, são **muitos de propósito**: um altar nu não os tem.
Sem poder, **nada se gasta** — o anel fica de pé e a pedra fica lisa, e dá para tentar outra vez quando o
altar crescer.

#### Um engano do original, corrigido

O `isSmallBlockCircle` varre as nove casas em que o meio do anel pode estar e, quando acha o anel em
`coord + co`, devolve **`coord - co`** — o avesso do meio que acabou de achar. Com a pedra bem no meio o
primeiro da lista é `(0,0)` e o engano não aparece, porque o avesso de nada é nada; é só quando ela **cai de
lado** — que é como ela sempre cai, quando alguém a atira para dentro do círculo — que a porta se abre no
lugar errado.

Aqui **o meio é onde o anel está**. A prova `theRingCentreIsWhereTheRingIs` larga a pedra uma casa a leste e
exige o meio de verdade.

#### O que fica igual, mesmo parecendo engano

**A pedra que sobra vai pela porta junto.** O original larga o resto do monte no chão, morre, e só então varre
o que está dentro do anel — e o resto do monte está dentro do anel. Fica assim: é faithful, e faz sentido no
mundo (o que está no círculo, vai).

#### Traduções declaradas

1. **Um item largado em vez de uma entidade própria.** No original a pedra é uma classe de item largado
   própria (`EntityItemWaystone`), e o mod troca o item largado por ela ao entrar no mundo. Aqui a batida é a
   de qualquer item largado, e a pergunta é pelo item. Dá o mesmo e poupa uma entidade — e os dois segundos de
   espera com as quarenta batidas entre olhadas do original são o que faz isso não custar nada.
2. **O nome do mundo.** Em 2014 cada mundo tinha um nome em código — "Overworld", "Nether" — e era esse que a
   pedra mostrava. Hoje um mundo é uma marca; a pedra pergunta por um texto em `dimension.<espaço>.<nome>` e,
   não havendo, mostra a marca crua. Os três do jogo estão traduzidos.
3. **Uma varredura em vez de duas.** O original procura o alvo do anel em duas voltas — primeiro gente, depois
   bicho. Aqui é uma só, com a gente a valer mais. Dá o mesmo, porque a gente sempre ganha de qualquer bicho,
   por mais perto que ele esteja.

#### O que fica de fora, declarado

- A **Pedra Afinada** e o **Espírito Dominado** largados num anel miúdo de **giz de Ritual** fazem nascer um
  Espírito. É o mesmo método, no mesmo lugar do original — mas pede o `EntitySpirit`, que este porte ainda não
  tem.
- A pedra presa, segurada na mão, mostra o lugar dela por uma **câmara remota** — um pacote de rede e uma tela
  próprios.
- A **Inibição do Alhures** segura o que o porte sabe mandar: o teleporte da Pedra de Caminho. No original ela
  cancela o `EnderTeleportEvent` e por isso segura também o salto do enderman e a pérola; isso fica para
  quando houver um lugar só deles de onde perguntar.

**Guardas:** `OccultaWaystoneGameTest`, com oito — os números; o anel miúdo que prende e se gasta; os oito de
uma vez com o resto de volta; o que acontece sem altar; a gente que ganha do bicho; a Pedra Sangrada que
guarda quem e não onde; **o meio do anel que é onde o anel está**; o anel pequeno que leva o que há; e a
inibição que segura. E `OccultaWaystoneClientTest`, com as três pedras na barra e a dica da pedra presa.

### O Escravizado: o que faltava ao Cozimento da Ressurreição

O `PotionEnslaved` não é domar. Um bicho escravizado continua o bicho que era — o zumbi continua zumbi, e
morde quem encontrar. Mudam duas coisas, e só essas duas:

1. **ele nunca mais escolhe o escravizador por alvo** — e se já o tinha, larga;
2. **ele briga as brigas do escravizador**: quem bater em quem o escravizou passa a ser alvo dele.

Por isso o efeito é **infinito**: ele não faz nada por si, está lá para ser perguntado. Um laço não acaba
sozinho.

O miolo da segunda é o **relógio de vingança**: a vontade guarda o número da última vez que o dono foi ferido
e só se acende quando ele **muda**. Sem isso, um escravo cujo dono levasse uma pancada ficaria a reacender-se
para sempre contra o mesmo agressor, mesmo depois de ele ter fugido ou morrido.

Isto é o que faltava ao `BrewActionRaising`. No original, **quem levanta os mortos fica dono deles** — e sem o
laço o frasco era uma arma que mordia quem a atirava: o zumbi nascia hostil, e o primeiro a quem ele chegava
era quem estava de pé ao lado do estouro.

#### E deles não cai nada

O `EntityUtil.setNoDrops` também faltava, e virou classe própria (`NoDrops`), porque o ofício inteiro precisa
dele: é a marca que se põe em **tudo o que o mod faz nascer**. O bicho é um bicho de verdade, mas o que ele
tem no corpo **não veio do mundo**, e por isso não volta para ele. Sem isso, levantar mortos era uma fábrica
de carne podre: um frasco, uma dúzia de zumbis, uma dúzia de carnes podres, outra vez.

**Nunca de gente**, como no original: o `isNoDrops` pergunta `!(entity instanceof EntityPlayer)` antes de
olhar o NBT, e é de propósito.

#### Quem fez o cozimento

O `modifiers.caster` do original não existia neste porte, e agora existe: o frasco atirado leva quem o atirou,
e o frasco bebido leva quem o bebeu. Fica **nulo** quando não se sabe — numa nuvem que já estava no chão, por
exemplo. Quase nenhum efeito precisa dele; os que precisam, precisam muito.

#### Traduções declaradas

1. **UUID em vez de nome.** O original guarda o **nome** de quem escravizou e acha a pessoa pelo nome. Aqui se
   guarda o **UUID**, o que é estritamente melhor: um nome muda, e no original um bicho escravizado por alguém
   que trocasse de nome ficava preso a um nome que não existia mais.
2. **Os dois chefes do jogo em vez da interface.** O `canCreatureBeEnslaved` pergunta
   `instanceof IBossDisplayData`, que é a interface da barra de chefe de 2014. Hoje não há interface nem
   etiqueta de chefe: o que há são os dois chefes do jogo, e é por eles que se pergunta. O demônio e o
   diabrete, que o original também exclui, ainda não existem aqui.
3. **A mira se atalha na cabeça.** No original o `onLivingSetAttackTarget` é um evento do Forge que corre
   depois de o alvo já estar posto e o desfaz. Aqui se atalha na cabeça do `setTarget` — o mesmo visto de mais
   perto, com a vantagem de o alvo nunca chegar a existir, nem por uma batida.

**Guardas:** `OccultaEnslaveGameTest`, com oito — quem não se escraviza; **o escravo que nunca mira no dono**;
o que ele continua mirando; o laço que não se põe duas vezes; a vontade que entra pela batida e não entra
duas vezes; o morto levantado que é de quem o levantou e de quem não cai nada; o levantado por ninguém; e a
gente, de quem cai sempre.

### Um achado da suíte: o jogador de mentira está sempre em criativo

O `makeMockServerPlayerInLevel` entrega um `ServerPlayer` cuja classe **sobrescreve o `gameMode()` com a
palavra `CREATIVE` escrita à mão** — e o `setGameMode` não o tira de lá. Como a primeira coisa que o
`asValidTarget` do `Mob` olha é se o alvo está em criativo, **nenhum bicho consegue mirar nele**, e qualquer
prova de mira contra esse jogador passa por engano.

Quem serve é o **`makeMockServerPlayer(GameType)`**, que diz o modo que se pediu. Ele não entra no mundo — e
para uma prova de mira não precisa, porque a mira não pergunta onde o alvo está.

(E a dificuldade também conta: em **paz**, o `canAttack` recusa qualquer gente como alvo. A prova a põe em
fácil e a devolve ao que era num `finally`.)

## O apetrecho do Caçador de Bruxas (2026-10-03)

A primeira metade da fatia do Caçador: o que ele leva. O **caçador** em si vem a seguir.

### A Besta de Mão: o gesto é que é a arma

O `ItemHandBow` não é um arco. Um arco se puxa e se solta; esta se **carrega**, e o que ela tem dentro fica lá
até alguém soltar o gatilho. São dois gestos, e os dois começam do mesmo jeito:

- **De pé, vazia:** segurar carrega.
- **De pé, carregada:** segurar e soltar atira. Meio segundo já bota o virote fora; **um segundo inteiro
  bota-o crítico**.
- **Agachado:** segurar **troca o virote** pelo seguinte que houver na mochila, e **devolve o que estava
  dentro** — por isso trocar não custa munição. Três estalos, aos cinco, dez e quinze tiques, dizem que a
  troca está acontecendo.

É o gesto que faz dela o que ela é: quem caça o que a espada não mata precisa de escolher a munição **com o
bicho em cima**, e agachar-se é exatamente o tempo que isso devia custar. Não há menu nenhum.

### Os cinco virotes

| Virote | O que faz |
| --- | --- |
| **de Madeira** (estaca) | o comum — e é de madeira que se mata vampiro |
| **Anulador** | chupa magia de quem acerta |
| **Anulador, com o conjunto vestido** | **limpa**: tira todo efeito menos os três que são castigo, e derruba o poder pela metade |
| **de Osso** (sagrado) | **uma vez e meia** contra morto-vivo e coisa do inferno |
| **que Parte** | três de uma vez, num leque de vinte graus, por metade do dano cada |
| **de Prata** | a **única coisa de longe** que fere um lobisomem |

A drenagem forte deixa de propósito o **veneno**, o **definhar** e a **cegueira**: tirá-los seria **curar**
quem se acertou.

E o virote de prata fechou um buraco que o porte já tinha escrito no próprio `Silver`: lá dizia, por escrito,
que o caminho do virote de prata não existia. Agora existe, e o `éDePrata` pergunta por ele.

### Chupar o poder: dois poços em vez de três

O `reducePowerLevels` do original toca em três poços — o do Witchery, o do Ars Magica e o do Thaumcraft —
porque lá eram três mods. Aqui são **dois**, e estão no mesmo jar: a **mana** do Ars Arcana (que é o mesmo poço
que lá era a energia de infusão <i>e</i> a mana do Ars Magica) e o **vis das varinhas** que a pessoa carrega.

**Tradução declarada:** o gancho do Thaumcraft chama o `consumeVisFromInventory`, que aplica o desconto da
ponteira. Aqui o vis se tira **cru**: uma ponteira boa faz uma varinha **gastar** menos, e não a protege de
quem a está esvaziando.

### As roupas: couro para proteger, ferro para durar

São quatro peças em três feitios — a **lisa**, a **prateada** e a **da aurora**, que é prateada <i>e</i> com
alho (é o `ItemHunterClothes(casa, true, true)` do original, e por isso vale contra os dois).

A proteção é a de couro — um, três, dois e um — e a durabilidade é a do **ferro**. É a melhor piada do mod:
quem caça o que a espada não mata anda de casaco, e o casaco aguenta.

**O que vale é o conjunto**, e só o conjunto inteiro: protege de **magia** uma vez em quatro, de **maldição**
nove vezes em dez, faz o virote anulador **drenar com força** — e, se for prateado, protege de **lobo**.

**E o conjunto cobra**: quem o veste **não pode usar boneca nenhuma**. É o preço inteiro do ofício, e é o
maior que o mod cobra: quem caça bruxas não anda com a magia das bruxas no bolso. No original isso é o
`findBoundPoppetInWorld`, que devolve nada **antes** de procurar — e por isso nenhuma boneca se gasta, ela só
não é achada.

A peça **certa** contra a pancada certa — prata contra lobisomem, alho contra vampiro — vale **duas vezes e
meia**, **não se gasta** nessa pancada, e **queima quem bateu**. A roupa não é armadura: é uma armadilha
vestida.

#### A conta do Forge, traduzida

No Forge de então cada peça dizia quanto absorvia: o comum era `armadura / 25`, e a peça certa dizia
`armadura * 2,5 / 25`. A armadura comum já é aplicada pelo jogo de hoje antes de o nosso código correr, e por
isso o que se tira a mais é **só o que falta** — `armadura * 1,5 / 25` por peça certa. Com o conjunto prateado
inteiro, que dá sete de armadura, são quarenta e dois por cento a menos do que já sobrou.

**Desvio declarado:** o original poupa do desgaste **peça a peça**; aqui se poupa o conjunto quando alguma peça
é a certa. Com o conjunto todo do mesmo feitio — o único caso em que as proteções valem — dá o mesmo.

### E elas não são armadura de folha

Esta foi a descoberta da fatia, e custou uma tela para aparecer.

A primeira tentativa pôs as roupas como **camada de armadura** do jogo de hoje, com a folha do original. Saiu
um caçador de **chapéu certo e sem casaco**: o tronco ficava com a camisa da pele por baixo. A folha parecia
meio vazia.

Ela não estava vazia: ela é de **cento e vinte e oito por sessenta e quatro**, e o original a lê com um
**modelo próprio**, o `ModelHunterClothes`, que não é a camada de armadura do jogo — é um `ModelBiped` com
**quatro caixas a mais**:

- o **chapéu**, em três andares presos uns aos outros: a **aba**, de treze por um por treze, presa à cabeça; o
  **meio**, de oito por dois por oito, preso à aba; e o **topo**, de sete por dois por sete, preso ao meio;
- e o **casaco**, de onze por dez por seis, preso ao tronco, que desce dez pontos **abaixo** da cintura.

É o chapéu que faz um caçador de bruxas ser reconhecido de longe, e ele não cabe numa folha. Por isso as
roupas vão pelo mesmo caminho dos **Abafadores**: um `ArmorRenderer` com modelo próprio, caixa por caixa, nos
números do original. E são **dois** modelos, não um — o do peito com `0,4` de folga e o das pernas com `0,01`:
o casaco tem de sobrar do corpo, as calças têm de colar à perna.

**E a cor de fábrica deixou de ser componente.** Posta como componente, toda peça dizia "Tingida" na dica sem
ninguém lhe ter tocado. O original dá a cor **na pergunta**, e é o que se faz aqui: o desenhista pergunta ao
item, e quem pintar a peça escreve por cima. As peças entram na etiqueta `minecraft:dyeable`, que é como se
tingem hoje.

### E as receitas, que faltavam até à raiz

Vieram dois itens que o porte não tinha e de que **a Pedra de Caminho também já precisava**: o
**Catalisador Nulo** — estrela do Nether, diamante, pederneira e seis pérolas do Alhures — e o **Couro
Anulado**, oito couros em volta de um catalisador. É com ele que o conjunto se costura, e é por isso que ele
protege de maldição: é feito do que não deixa magia passar.

As vinte e uma receitas são as do original, letra por letra, incluindo a **prateação** (pó de prata, acônito e
linha em volta da peça lisa) e o **alho** (alho e linha em volta da prateada) — e é por isso que a da aurora é
prateada: ela **vem** da prateada.

**Guardas:** `OccultaHunterGearGameTest`, com oito — os números; o que cada virote é e como volta ao chão; **o
virote de prata que é dano de prata**; a drenagem forte que limpa menos os três castigos; a fraca que não
limpa nada; o conjunto que só vale inteiro e tira as bonecas; **a roupa da aurora que também é prateada**; a
peça certa contra a pancada certa; e a besta vazia que não atira. E `OccultaHunterGearClientTest`, com o
caçador visto de fora e a dica do casaco.

### E uma prova que mexia no mundo inteiro

A prova `anEnslavedMobNeverTargetsItsEnslaver` da fatia anterior **mudava a dificuldade** do mundo e a repunha
num `finally`. As provas correm todas no mesmo mundo, em lote, e a dificuldade é do mundo inteiro: enquanto a
prova corria, as vizinhas viam outra. Agora ela **confere** a dificuldade em vez de a mexer, e falha dizendo o
que precisa se o mundo estiver em paz.

## O Caçador de Bruxas (2026-10-03)

A segunda metade da fatia: o homem.

### Ele é a resposta do mundo ao ofício

O `EntityWitchHunter` não nasce de ódio nem de escuridão. Nasce porque **alguém fez magia negra** — e vem
atrás dessa pessoa **pelo nome**.

O relógio tem três voltas, e a melhor parte dele é a **demora**:

1. Alguém espeta uma boneca de vodu ou amaldiçoa um vizinho. **Uma vez em dez**, isso é **notado**.
2. **Dois minutos depois**, e só a partir daí, há **uma chance em cem** por volta de o mundo mandar alguém.
3. E então **dois caçadores** aparecem entre três e oito blocos de distância, já sabendo de quem vieram
   buscar, e quem os chamou ouve um som que não ouviu antes.

O que torna isto bom não é o perigo: é que entre o feitiço e a batida à porta passam minutos, e às vezes nada
acontece. Quem joga não liga uma coisa à outra na primeira vez — liga na terceira, e aí já é tarde para
desaprender a magia.

### E a lista do que ele caça é curta

Morto-vivo, coisa do inferno, bruxa, lobisomem, vampiro — e **gente**, mas só a que for bruxa, lobisomem,
vampira, ou **a que ele veio buscar**.

Repare no que **não** está nela: aldeão, bicho, creeper, esqueleto comum. **Ele não é um monstro — é um homem
com um trabalho**, e o trabalho é curto. É a diferença entre um caçador de bruxas e um zumbi, e ela está toda
nesta lista.

### O que o faz durar não é a vida

São trinta de vida, mas **nenhuma pancada lhe tira mais de nove** — o mesmo truque da Baba Yaga, e pela mesma
razão: contra quem leva a armadura certa, o número que importa é o teto.

E ele **não se fere** por mão de **guarda de aldeia** nem de **outro caçador**: eles são do mesmo lado, e o
original diz isso com um `instanceof` em vez de uma facção.

**O veneno não pega nele.** De segundo em segundo ele se limpa — é a primeira coisa que uma bruxa tenta, e a
primeira que não funciona.

### E ele escolhe a munição

É a coisa que mais o faz parecer gente. O `attackEntityWithRangedAttack` do original olha o alvo antes de
atirar: **prata** contra lobisomem, **osso** contra morto-vivo, e — uma vez em quatro — o **anulador** contra
tudo o mais. Contra vampiro, uma vez em três, o virote sai **a arder**.

Dele caem **virotes de madeira**, e raramente dois anuladores: quem o mata fica com a munição dele.

### As roupas dele estão pintadas, não vestidas

O `ModelWitchHunter` é um bípede com **três caixas a mais** — a aba do chapéu, de **catorze** por um por
catorze; o topo, de seis por dois por seis; e a saia do casaco, de dez por onze por cinco, presa ao tronco. E
são **três peles**, sorteadas ao nascer.

Por isso ele **não veste** as peças do conjunto, embora elas existam: vesti-lo punha dois casacos um por cima
do outro. E repare que a aba dele é **maior** que a das roupas que se podem vestir — catorze contra treze. Um
caçador de verdade tem o chapéu que ninguém mais tem.

### Traduções declaradas

1. **UUID em vez de nome**, para quem ele veio buscar — pela mesma razão do Escravizado.
2. **O vampirismo de jogador** é a outra razão por que eles aparecem no original: um vampiro de grau dez,
   malvisto numa aldeia, atrai caçadores. Isso pede a vampirice de jogador, que este porte ainda não tem;
   quando vier, é no relógio que se pergunta, ao lado do que já está ali.
3. **O aparecimento natural** fica de fora: o original deixa o caçador nascer à noite como monstro comum,
   além de vir pelo relógio. Aqui só vem pelo relógio — que é o que o torna o que ele é.

**Guardas:** `OccultaWitchHunterGameTest`, com oito — os números; **mil de dano que tiram nove**; o seu lado,
que não o fere; **a lista curta** do que ele caça, com o aldeão, o porco e o creeper de fora; a gente, só a que
ele veio buscar; o veneno que não pega; a besta que o faz atirar; **o relógio que a magia negra põe a correr**;
e os dois que vêm sabendo de quem se trata.

## As poções que faltavam (2026-10-03)

As vinte e três do Witchery que o porte ainda não tinha. Com elas, das quarenta e três do original ficam de
fora só as que são **maldição** (e essas estão feitas, por outro caminho) e as que pedem gente transformada.

### As que mexem no golpe, e porque estão todas juntas

Seis delas se perguntam **no mesmo instante** — entre o golpe e a vida — e por isso moram no mesmo lugar, o
`OccultaHurt`, pela ordem de registro do original. A ordem importa: o que uma tira, a seguinte já não vê.

| Poção | O que faz ao golpe |
| --- | --- |
| **Enregelado** | o fogo dói **um a menos por grau** — e do terceiro grau em diante pode chegar a zero |
| **Enrolado em Vinha** | o fogo dói **até quatro vezes mais** |
| **Absorver Magia** | come **um quinto por grau** do dano mágico, e em gente o vira **mana** |
| **Refletir Dano** | manda **um décimo por grau** de volta a quem bateu, e o que volta **sai do que chega** |
| **Repelir Agressor** | empurra quem bateu de perto |
| **Não Sentir Dor** | paga o resto com **fome** em vez de vida |

As duas primeiras puxam o fogo para lados opostos, e é a melhor prova da fatia: se a ordem entre elas se
perder, as duas param de fazer sentido.

E repare no **piso** do Enregelado, que é o detalhe que é fácil perder: até o segundo grau o fogo sempre
deixa **um ponto**; é só do terceiro em diante que ele pode não passar de todo. Não é o quanto que muda com o
grau — é o chão.

### As três que mexem na morte

- **Reencarnar**: do corpo levanta-se outra coisa, e o que se levanta diz o que o morto era — de bicho ou de
  aranha sai bicho de teia, de tudo o mais sai morto-vivo. E **já odeia quem matou**.
- **Guardar o Que Se Tem**: quem morre não larga nada.
- **Guardar o Que Se Bebeu**: quem morre acorda com as mesmas poções no corpo. Vai num apego, porque o jogador
  que morre e o que acorda são, para o jogo, **dois objetos diferentes**.

### E o Colorido, que custou duas telas

O `PotionColorful` **não faz nada**. Pinta quem o tem da cor do grau — as dezesseis tintas do jogo, pela ordem
delas — e é só isso. É a melhor piada do Witchery: o cozimento mais difícil de acertar sem efeito nenhum.

A primeira tentativa pintava **nada**, e a razão é boa de saber: **as poções de um bicho não vão para quem
joga**. O jogo de hoje manda ao cliente as poções do *próprio* jogador e mais nada — um porco com uma poção no
corpo é, do lado de quem olha, um porco qualquer. Em 2014 era igual, e o original escapava porque pintava o
bicho **dentro** do desenho, que lá corria com o bicho do servidor à mão.

Por isso a cor viaja num **apego sincronizado**, posto e tirado na batida de quem a tem, e o desenhista lê o
apego. É menos uma cor do que um fato: este está pintado, e desta cor.

**Traduções declaradas:**
1. O original são duas chamadas de `glColor3f` em volta do desenho **inteiro**, camadas incluídas. Aqui é o
   `getModelTint`, que é a cor do **corpo** — a lã de uma ovelha e a armadura de um esqueleto ficam da cor
   delas. Em troca, a poção passa a conviver com o piscar de quem levou uma pancada, coisa que no original ela
   apagava.
2. A segunda tela foi feita com **porcos** e não com ovelhas, e não por capricho: o corpo de uma ovelha é a
   pele tosquiada, e a lã é uma camada — uma ovelha pintada parece uma ovelha branca.

### O Redimensionar, que era duzentas linhas e agora é uma

O `PotionResizing` muda o tamanho de quem o tem. Em 2014 isso custava **reflexão**, método a método, um por
feitio de bicho, porque o jogo de então não deixava mudar o tamanho de uma entidade de fora.

Hoje o jogo tem um **atributo** para isso. O que lá eram duzentas linhas aqui é a escala — e o original
**encolhe nos graus pares e aumenta nos ímpares**, que é o que se faz.

### O que o leite não tira

A lista do `setIncurable` cresceu de quatro para doze, e é quase toda de coisas **ruins**. É de propósito: o
que o ofício faz a alguém de propósito não se lava com um balde de leite. As duas boas que estão nela — a
Máscara de Gás e a Barriga Forte — são as que se bebem **antes** de uma coisa perigosa, e perdê-las ao beber
leite no meio seria uma morte estúpida.

### E onde cada uma se coze

Doze delas ganharam ingrediente no caldeirão, com os poderes e as durações do original — e o **Colorido** são
dezesseis cozimentos, um por tinta, todos do mesmo efeito em graus diferentes. Para isso o cozimento ganhou
uma **força de partida**, que é o que diz qual tinta; sem ela as dezesseis dariam a mesma cor.

**Fica de fora, declarado:** a **Mal Ajustada** e o **Repelir Agressor** pedem a <b>Sarça</b>, e a
**Paralisia** pede o **Coração de Demônio** — nenhum dos dois está portado. As poções existem e funcionam; o
que falta é o ingrediente. E quatro delas não se cozem de todo, porque no original também não: a **Corda
Mortal** vem do Cozimento da Ressurreição ritualizado, a **Paralisia** do vampiro, o **Enjoado** do estômago
cheio, e a **Adoração** dos goblins.

### Três enganos de leitura, corrigidos

Os nomes de item do 1.7.10 são números com letras, e três estavam mal lidos — dois deles já commitados na
fatia do Caçador:

| Campo | Era lido como | É |
| --- | --- | --- |
| `field_151064_bs` | pérola do Alhures | **creme de magma** |
| `field_151119_aD` | açúcar | **bola de argila** |

O primeiro estava no **Catalisador Nulo** (seis cremes de magma, não seis pérolas) e na receita que o
multiplica; o segundo era o ingrediente da **Fortuna**. Os dois foram confirmados contra o próprio original:
`field_151064_bs` é o cozimento de resistência ao fogo, que este porte já tinha mapeado para o creme de magma,
e `field_151119_aD` é o Pote de Barro Mole, que este porte já tinha mapeado para a bola de argila.

### E uma instável, consertada

A prova `callingBeastsBringsThemInsteadOfMakingThem` falhava de vez em quando desde a fatia dos ritos. A razão
era boa: o rito varre uma caixa de **cento e vinte e oito blocos** por canto e traz **dois** bichos de cada
vez; as provas correm todas no mesmo mundo, e um canto desses apanha as arenas das vizinhas. Com uma galinha,
o rito trazia as galinhas das outras provas e nunca chegava à desta.

Agora usa um **camelo**, que nenhuma outra prova usa. Fica uma das duas instáveis de pé — a do Apanhador de
Sonhos —, e o cartão da isolação continua aberto.

**Guardas:** `OccultaPotionsGameTest`, com doze — os números; **o fogo puxado para os dois lados**, com o piso
do Enregelado; a magia comida e só a magia; o reflexo que sai do que chega; o empurrão; a fome que paga; a
Corda Mortal que não faz nada e então mata; a Aura que queima o lado e não quem a tem; a Mal Ajustada que
despe perto do fim; a escala que vai e volta; a aranha que sai do bicho; o leite que não tira estas; e que
todas as doze se cozem. E `OccultaColorfulClientTest`, com os oito porcos.

## O goblin (2026-10-03)

A última das quatro miudezas, e a mais estranha do mod inteiro: **um bicho que trabalha**.

### Ele não é domado — é preso

Não há comida que o amanse, não há ovo que o invoque, não há ordem que ele obedeça. O que há é uma **corda**,
e um goblin na corda faz três coisas que nenhum outro bicho do jogo faz:

1. **apanha o que está no chão** e o carrega na mão;
2. **cava**, se lhe puserem uma picareta na mão;
3. e **larga o que cavou** no primeiro baú grande que ache.

Fechado o ciclo, um goblin preso ao pé de um baú numa caverna trabalha sozinho até alguém o desprender. É a
única automação do Witchery, e ela é um bicho com uma corda ao pescoço.

**E o gesto de mandar nele é o melhor detalhe.** Não há menu: clicar nele com uma picareta na mão **dá-lhe a
picareta**, e clicar outra vez **tira-lhe o que ele tiver**. É assim que se diz a um goblin o que fazer, e é
assim que se recebe o que ele fez.

### E o jeito como ele escolhe onde cavar

Ele **vira-se para um lado a esmo** e olha em frente quatro blocos. O que estiver ali, se for cavável, é o que
ele cava.

Não há plano, não há área marcada. Um goblin na corda numa caverna **abre buraco**, e para onde ele abre é
problema de quem o levou lá. Ao fim de **quinze** olhadas falhadas ele desiste de olhar em frente e **olha
para baixo** — que é como ele acaba por cavar um poço debaixo dos próprios pés.

### A conta da coragem

É a única coisa que decide o que um goblin é, e são **três**:

- **sozinho**, ele foge de gente e do guarda da aldeia;
- **em três**, a oito blocos uns dos outros, ele deixa de fugir — e **caça aldeão**.

É a mesma conta vista dos dois lados (o `shouldAvoid` e o `isEntityApplicable` do original são o mesmo
`if`), e é por ela que eles andam sempre em bando.

**E ele trepa paredes**, como uma aranha. Uma cerca não o segura.

### Traduções declaradas

1. **Onde ele procura o baú.** O original varre a **lista inteira de blocos com alma do mundo**, e apanha um
   `Throwable` em volta disso porque a lista muda enquanto ele a lê. Aqui se varre o que está **à volta
   dele**, casa a casa, no mesmo alcance de vinte e quatro — é a mesma resposta sem ler o mundo inteiro, e sem
   apanhar erros que não deviam acontecer.
2. **O que ele cava** era uma lista de `Material` do 1.7.10 — pedra, areia, terra, barro e chão. Hoje são as
   etiquetas do jogo, que dizem o mesmo e deixam qualquer mod entrar nelas.

### O que fica de fora, declarado

- O **Koboldite**: o minério, o lingote, a picareta e o que ela faz ao que se cava. É uma **linha de material
  inteira** do original, e com ela vêm os números do goblin que mais mudam — a picareta de koboldite cava de
  quatro em quatro batidas em vez de sessenta (**quinze vezes mais depressa**) e funde metade do minério que
  apanha. O goblin cava com qualquer picareta, e isso é tudo o que ele faz hoje.
- Os dois **chefes** goblins, o **Gulg** e o **Mog** — quatrocentos de vida cada — pendem da linha do
  koboldite e da infusão, e vêm com elas.
- O **comércio**: solto e numa aldeia, o goblin do original é um mercador. Fica para quando houver o resto da
  aldeia goblin.
- A **Estátua de Adoração** e a vontade de a adorar. O estado de adorar **está feito** — é um dos três que ele
  sincroniza, e todas as vontades de trabalho já o respeitam —, e só falta o bloco que o liga.

**Guardas:** `OccultaGoblinGameTest`, com seis — os números; **a conta da coragem**, com o que está longe a não
contar; a picareta que a corda troca de mão, e que solto ele não aceita; o que ele cava e o que não cava; o
apanhar que só vale na corda e de mãos vazias; o largar que poupa a ferramenta; e a parede que ele trepa. E
`OccultaGoblinClientTest`, com os quatro ofícios ao lado de um aldeão, para a altura e o nariz se verem.

## Uma Lã de Morcego a dobrar, tirada (2026-10-03)

O `OccultaBatWool` era de antes do `OccultaDrops`, e ficou. Os dois punham Lã de Morcego na queda do morcego —
o primeiro **uma vez em três, sem olhar a mão de quem matou**, e o segundo as duas contas certas do original
(uma em três sem a Arthana, três em quatro com ela, e certa com o Saque).

O que isso dava era **lã a dobrar**: quem matasse um morcego tinha a conta do `OccultaDrops` <i>e</i> mais uma
em três por cima, com ou sem faca. O javadoc dele dizia, por escrito, que a Arthana "ainda não está portada" —
e ela está, há fatias.

Tirado. A conta certa é a do `OccultaDrops`, e sempre foi.

## A licantropia do jogador — o corpo e a lua (2026-10-03)

A primeira metade da maior coisa do Witchery. O que ela traz é **ser** lobisomem; o que falta é **subir de
grau**, e isso vem a seguir.

### O que define um lobisomem não são os poderes — é o preço

Ao virar bicho ele **larga tudo o que veste**. A armadura cai sempre; e sendo **lobo**, cai também o que ele
tem na mão, porque um lobo não tem mãos. Nada de espada, nada de escudo, nada de armadura.

E **não escolhe quando**. A lua cheia escolhe por ele: de duas em duas segundos o mundo olha, e

- de **gente**, em lua cheia, ele **vira lobo**;
- de **bicho**, fora da lua cheia, ele **volta a ser gente**.

Em troca vêm a **visão noturna** e o **veneno limpo** — as duas coisas boas de ser bicho.

### Três coisas o seguram, e só três

| O quê | O que faz |
| --- | --- |
| **Amuleto da Lua**, na mochila | **trava a forma** onde ela estiver: a lua passa e não lhe toca |
| **Acônito**, no corpo | não deixa a transformação acontecer de todo |
| **O grau** | do **segundo** ele manda na mudança; do **quinto** pode escolher ser **lobisomem** |

Abaixo do segundo grau, a lua manda e ele obedece. É a parte do mod que mais se parece com uma maldição, e é
de propósito.

O **Amuleto da Lua** é a única coisa que ele **não larga** ao virar bicho — e isso não é um detalhe: largá-lo
seria perder, no chão, a única coisa capaz de desfazer a transformação.

Segurado na mão, ele muda a forma à vontade, e **demora menos quanto maior o grau** — a conta
`(grau - 1) * 4` do original, que faz de um lobisomem velho uma coisa que muda quase de imediato.

### As duas tabelas, que contam a história

| | o lobo | o lobisomem |
| --- | --- | --- |
| **velocidade** | meia já no primeiro grau, uma e três quartos no décimo | dois décimos, e só do quinto |
| **vida** | nada até o sétimo, doze no décimo | **vinte de uma vez ao quinto**, quarenta no décimo |
| **queda** | perdoa dois blocos cedo, cinco no fim | nada até o quinto, sete no fim |
| **teto da pancada** | quatro, baixando a dois | quatro, baixando a dois |

O lobo **é depressa desde o princípio**: ele é a forma que foge e que persegue. O lobisomem **não vale nada
até o quinto grau** — zeros em tudo — e então, de uma vez, ganha vinte de vida e quatro de dano. É a forma
que se **ganha**, não a que se recebe.

E repare no **teto da pancada**: é o único número que melhora **baixando**. Quatro no princípio, dois do
quinto em diante — e é ele que faz um lobisomem de grau alto difícil de matar, do mesmo jeito que faz a Baba
Yaga e o Caçador.

### Um lobo cabe onde uma pessoa não cabe

De lobo ele mede **oito décimos** de altura em vez de um e oito: passa por baixo de um alçapão e entra numa
toca de um bloco. As duas formas sobem um **degrau de um bloco inteiro**.

É a melhor razão para virar lobo que o mod tem, e é a única que não é um número de combate.

### Como se pega

Um **Lobisomem** que derrube alguém abaixo de **um quarto da vida** passa-lhe a licantropia, uma vez em
quatro. O mesmo golpe faz de um **aldeão** um lobisomem. E o **conjunto prateado do caçador** protege de todo
— que é a razão de ele existir.

### E o desenho

No original, um jogador transformado é desenhado por um **bicho de mentira**: um `EntityWolf` guardado à
parte, com a posição e o passo copiados a cada quadro. Era o jeito de 2014.

Hoje o jogo separa o que se desenha do que existe, e por isso aqui não há bicho nenhum: o desenho do jogador
é **atalhado** e no lugar dele vai o modelo do lobo ou do lobisomem, com o **mesmo estado**. Para o lobisomem
o estado serve tal como vem; para o lobo se enche um estado de lobo com o que o do jogador tem, e os números
que só um lobo tem ficam em repouso.

**E atalhar o desenho inteiro é o certo, não um atalho**: um lobo não veste nada, e as camadas que o jogo
desenharia por cima — armadura, capa, elitro — não têm onde se pôr num bicho. O jogo já lhe tirou tudo isso
das mãos quando ele mudou de forma.

A forma viaja num **apego sincronizado**, pela mesma razão da cor do Colorido: o cliente precisa de a saber e
as poções de um jogador não bastam.

### A costura fechou

O `Lycanthropy`, escrito na fatia do bicho como "a única linha que muda quando a licantropia vier", mudou — e
foi **só** ele. As roupas prateadas que ardem em quem as veste, o Caçador que escolhe o virote de prata, o que
a prata faz doer: tudo passou a saber a resposta certa sem se tocar em mais nada.

E ganhou uma segunda pergunta, que o original também tem: `é()` responde por quem está **em forma de bicho**,
e `éMesmoDeGente()` por quem **é** lobisomem mesmo estando de gente. É a diferença entre o que a prata fere e
o que o Caçador vem buscar — um lobisomem de dia é gente para a prata, e não é para o caçador.

### O que falta, declarado

- **A escada dos dez graus**: a Estátua do Lobisomem e o que ela pede em cada degrau. Sem ela, quem apanhar a
  licantropia fica no **grau um** — a lua manda nele e ele não manda em nada. É fiel ao que é ser recém-mordido,
  mas não há caminho para a frente até a escada vir, e **o Amuleto da Lua só sai da estátua**.
- **Os poderes**: o uivo nas três formas que ele toma, a armadura rasgada ao nono grau, o osso que sai da
  terra ao terceiro, a fome que a caça mata ao quarto, e o contágio ao décimo.
- O **Chifre da Caça**, que a estátua dá ao quarto grau.

> *As três coisas vieram nas duas fatias seguintes: **A escada dos dez graus** e **Os poderes do lobisomem**.*

**Guardas:** `OccultaWerewolfPlayerGameTest`, com nove — os números; o grau que para em dez e volta à forma de
gente ao chegar a zero; a forma de bicho que conta como lobisomem e a de gente que não; **o que virar bicho
custa**, com a mão do lobo e as mãos do lobisomem; o Amuleto que não cai; a lua que manda e as duas coisas que
não a deixam; o que cada forma dá e o que ela tira ao voltar; as duas tabelas linha por linha; o mando aos
dois graus e o lobisomem aos cinco; e **o lobo que cabe onde uma pessoa não cabe**. E
`OccultaWerewolfPlayerClientTest`, com as quatro telas da volta inteira.

## A escada dos dez graus — o Altar do Lobo (2026-10-03)

A outra metade da maior coisa do Witchery. A primeira trouxe **ser** lobisomem; esta traz **subir**.

### Dez degraus, e nenhum deles é o seguinte

Quem apanha a licantropia fica no **grau um**, e no grau um a lua manda nele e ele não manda em nada. Daí até
ao décimo quem dá os degraus é o **Altar do Lobo** — um a um, e nunca dois.

Os três primeiros são **coisas na mão**, e são de propósito os mais fáceis:

| degrau | o que ela pede | o que ela dá |
| --- | --- | --- |
| 1 → 2 | **três barras de ouro** | o grau, e um **Amuleto da Lua** |
| 2 → 3 | **trinta carnes de carneiro cruas** | o grau |
| 3 → 4 | **dez línguas de cachorro** | o grau |

Do quarto em diante ela deixa de pedir coisas e passa a pedir **feitos** — e cada feito só conta na **forma
certa**:

| degrau | o feito | a forma |
| --- | --- | --- |
| 4 → 5 | o **Caçador Cornudo** morto; ela dá o **Chifre da Caça** | qualquer |
| 5 → 6 | **dez monstros mortos no ar** | de bicho, e **sem os pés no chão** |
| 6 → 7 | **uivar em dezesseis lugares** diferentes | de **lobo**, e de noite |
| 7 → 8 | **seis lobos amansados** com o focinho | de **lobo** |
| 8 → 9 | **trinta porcos-zumbis** | de **lobisomem** |
| 9 → 10 | **uma pessoa** — aldeão ou jogador | de bicho |

É isso que faz da escada uma escada e não uma lista de compras: cada degrau **obriga a jogar de outro jeito**.
O quinto obriga a saltar, o sexto a andar, o sétimo a aproximar-se de um lobo bravo sem bater nele, o oitavo a
descer ao Nether de lobisomem, e o nono a fazer a coisa que ninguém faz por acaso.

### O ouro compra sempre, e isso é uma falha que fica

Do **segundo grau** em diante, chegar ao altar com três barras de ouro na mão compra um **Amuleto da Lua** —
sempre, e **antes** de ele olhar o degrau. Tem uma consequência curiosa: um lobisomem de grau dois que chegue
com ouro na mão **nunca passa do grau dois**, porque o altar lhe vende um amuleto em vez de lhe pedir o
carneiro.

É do original, letra por letra, e fica. A outra falha que fica é a do **Caçador**: matá-lo cumpre *qualquer*
pedido que esteja em curso, e não só o do quarto degrau — quem estiver uivando pelo mundo e matar um Caçador
que outro chamou sobe de graça. As duas estão declaradas no javadoc, porque tirá-las seria mudar a escada.

### O Caçador Cornudo

É o quinto degrau, e é o único que não se compra. O altar dá-lhe um **Chifre da Caça** — dois segundos de
sopro, e ele **parte-se**: aguenta um e gasta dois. O chifre não é uma ferramenta, é uma **vez**.

E o que vem é a **caça ao contrário**: o lobisomem, que é o que caça, chama o que caça lobisomens.

- **Quatrocentos de vida**, e **nenhuma pancada lhe tira mais de quinze** — vinte e sete golpes, no mínimo,
  enquanto ele **sara um por segundo**.
- **Ele entra com um estouro**: cento e cinquenta batidas de invulnerabilidade, começando com um quarto da
  vida e sarando vinte de dez em dez até os quatrocentos, e saindo com o estouro de seis do Wither. Esse
  tempo serve para uma coisa só, que é correr.
- **Ele atira**, uma vez em cinco e de segundo a segundo, e a flecha dele é mais forte **quanto mais longe**
  estiver o alvo — ao contrário do que se espera, e é do original.
- **Traz cães**: de duzentas a quinhentas batidas, um lobo raivoso com Regeneração II que não acaba.
- **Não se foge dele a pé**: quando o caminho fecha, ele aparece ao lado de quem persegue.
- E a pancada dele é **sete mais até quinze**, com **levantada** — ele bate, o chão se vai, e o golpe seguinte
  apanha quem está no ar.

Larga as **caveiras de wither** que ninguém mais larga fora do Nether, um **livro encantado**, o **Sangue
Demoníaco** e, uma vez em quatro, a **Lança do Caçador**.

### A lança que apara

A Lança do Caçador é **um ponto de dano acima de uma espada de diamante** e, na mão, **ninguém a empurra**. E
faz uma coisa que o aviso dela promete: quem **apara com ela** e apanha de alguém vivo chama, uma vez em
quatro, um **lobo bravo** que se vira contra quem bateu — e o lobo vem **a morrer**, com Definhamento II. Ele
não é um servo, é um **troco**.

No original ela apara porque é uma `ItemSword`, e na 1.7.10 **toda espada aparava**. Hoje só o escudo apara, e
por isso a lança passou a dizer por si mesma que apara: a **mesma metade**, num arco de noventa graus, pelo
jeito de hoje. Sem isso o aviso dela mentiria.

### O uivo é um gesto, e são três uivos

Uivar não tem tecla nem item: olha-se **direito para cima**, agacha-se, e aperta-se o botão de usar. Não está
escrito em parte nenhuma do jogo — quem descobre o uivo descobre-o por ter olhado para a lua.

E o mesmo gesto faz **três coisas diferentes**, pela ordem em que o original as pergunta:

1. no **sexto grau**, de lobo e de noite, ele **conta** o pedaço de mundo — e um lugar onde ele já uivou não
   conta, e é avisado disso em vermelho;
2. do **oitavo** em diante, de lobo, ele **chama cães**: dois mais o que o grau der, já mansos, com a Morte
   Certa de dez segundos. Eles vêm para morrer, e não deixam nem corpo **nem experiência**;
3. do **sétimo** em diante, de lobisomem, ele **prende**: tudo o que não é lobisomem nem vampiro, a dezesseis
   blocos, fica paralisado.

Os dois últimos esperam um minuto entre si. E repare na ordem: um **lobo de grau sete não tem uivo nenhum** —
o primeiro ramo quer grau seis, o segundo quer oito, e o terceiro quer lobisomem. É do original, e é o degrau
em que ele está aprendendo.

O **zero de experiência** dos cães sai por onde no original saía por reflexão: um acessório ao `xpReward` do
bicho. Sem isso, um lobisomem de grau dez tem uma fábrica de experiência que basta uivar para ligar.

### E correr é saltar

Em forma de bicho, quem **corre e pula** é atirado para a frente do tamanho do arranco do grau — e é isso que
faz do quinto degrau, que pede dez monstros mortos **no ar**, uma coisa que se consegue.

> *Esta fatia pôs o arranco a correr a cada batida, e isso estava errado: o original soma uma vez, no pulo. A
> fatia dos poderes corrige e acrescenta o que faltava, que é pular mais alto.*

E em forma de bicho a **arma na mão não vale nada**: a pancada vale **dois** se o que ele tem na mão tem dano
próprio, e só soma o dano do grau a **mãos vazias e a correr**. Um lobo com uma espada de diamante bate menos
do que um lobo sem nada, e é de propósito: a forma de bicho joga-se com as mãos vazias.

### A Cabeça de Lobo Empalhada, e o nome que mudou

O altar não se acha: **faz-se**, com três cabeças de lobo, quatro pedras e um raminho de acônito. E a cabeça
cai de um lobo morto **uma vez em doze**, que a Pilhagem melhora até quatro em doze.

Ela é o crânio do jogo com outro modelo — **dois blocos**, um que assenta e gira em dezesseis passos e outro
que se prega na parede —, e usa a **pele do lobo do jogo**, sem folha nova.

**O nome mudou, e não por gosto**: o original só lhe chama `wolfhead`, e esse nome já estava tomado — o ramo
do Mortuorum tem uma cabeça de lobo que é peça de costura. Duas coisas não podem ter o mesmo nome, e esta é a
**empalhada**.

### O que se desenhou

O **Altar do Lobo** é o modelo mais cheio deste porte: **trinta e oito peças**. Um pedestal de três lajes, um
degrau à frente onde se põe o que se traz, o **senhor** de pé — tronco de gente, cabeça de lobo, uma lança de
quarenta de comprido atrás das costas e uma perna dobrada, como quem acabou de parar de andar — e **dois
lobos** aos pés, cada um virado para fora por um ângulo diferente.

Os três encolhimentos do original — sete décimos no senhor e metade em cada lobo, cada um em volta do próprio
ponto — saem pelas escalas da peça, que é a mesma conta com muito menos linhas do que os seis `glTranslate` e
os três `glScaled` dele.

E o **Caçador** não é um gigante: é um homem com as proporções erradas. O peito mede vinte de largura, mais do
que o corpo inteiro de um aldeão, e os antebraços e as canelas são mais grossos do que os braços e as coxas. A
galhada é uma caixa **chata** de vinte por dezessete, presa à cabeça, e a lança é **duas folhas cruzadas** que
de qualquer ângulo parecem uma lâmina só. As pernas vêm dobradas de fábrica: ele **nunca está de pé direito**.

E **o corpo dele balança** cinco graus e meio no compasso do passo, com a onda triangular do original e não com
um seno — que é o que faz dele uma coisa **pesada** em vez de um boneco grande.

**Duas diferenças declaradas, as duas de desenho.** O original punha um **brilho verde correndo** por cima do
Caçador e da lança, com a folha do encantamento, e esse brilho era **opcional no próprio original** — um
ajuste dele. Aqui não está: o jeito de hoje de pintar um brilho por cima de um modelo é outro, e o que faz o
Caçador assustar é o tamanho e o balanço, que estão. E a lança, que o original punha na mão com três giros e
três deslocamentos seus — cem graus, cinquenta e um negativos, oitenta e um negativos —, vai na mão pelo jeito
que o jogo de hoje **tem** de segurar uma haste comprida, que é o do tridente. Os seis números dele eram
feitos à mão para chegar ao mesmo lugar.

### O que falta, declarado

A escada está inteira: **nenhum degrau falta**, e há prova disso. O que faltava eram os outros **poderes** do
lobisomem — a armadura rasgada, a fome que a caça mata, o osso que sai da terra, a queda que perdoa, o teto da
pancada e o contágio —, e esses vieram na fatia seguinte: **Os poderes do lobisomem**, mais abaixo.

Fica a **cabeça do cão-do-inferno**, que é o segundo tipo da cabeça empalhada e espera o bicho dela.

E uma terceira, pequena e do jeito de hoje: a cabeça na **mão** leva um **meio-giro** que o original não
escrevia. Na 1.7.10 quem o dava era o caminho do crânio do jogo, por fora do desenhista; hoje não há esse
caminho, e sem ele a cabeça aparecia de costas no inventário.

**Guardas:** `OccultaWerewolfLadderGameTest`, com dezesseis — os números de cada degrau; o altar, a cabeça, o
chifre, a lança e o Caçador no jogo; o grau zero que não é digno; o ouro que compra sempre e nunca o degrau; a
mão errada e a mão a meio que não custam nada; o grau que apaga o degrau; o lugar que só conta uma vez; o
Caçador que custa vinte e sete golpes e a espera que o leva aos quatrocentos; o chifre que só se dá uma vez e
o Caçador morto que cumpre o pedido; **a escada inteira do grau um ao dez**, que é a prova que carrega a
fatia; o feito que só conta no degrau dele; os dois uivos que chamam e que prendem; a pancada de um bicho com
arma na mão; e os três blocos que assentam onde se põem. E `OccultaWerewolfLadderClientTest`, com **oito
telas** medidas de um marco: o altar de frente, de perto e de lado, as duas cabeças, o Caçador de longe e de
perto, a lança na mão e as cinco coisas no inventário.

## Os poderes do lobisomem — o que a escada destranca (2026-10-03)

A terceira e última parte da licantropia. A primeira trouxe **ser**, a segunda trouxe **subir**, e esta traz
o que se ganha ao subir.

O que todos eles têm em comum vale dizer de uma vez: **nenhum se escolhe**. Não há tecla, não há item, não há
menu. Todos vêm do **grau** e da **forma**, e todos param sozinhos quando ele volta a ser gente.

### O salto, e uma correção

Um bicho **pula mais alto** — o `salto` do grau, somado de uma vez ao impulso do pulo — e, **correndo**, o
pulo também o atira **para a frente**, do tamanho do `arranco`. Um lobo de grau dez que corra e pule atravessa
quatro ou cinco blocos num salto só, e cai em cima do que estiver no caminho. É isso que faz do quinto degrau
— dez monstros mortos **no ar** — uma coisa que se consegue.

**E aqui houve um erro meu, que esta fatia corrige.** Na fatia da escada eu pus o arranco a correr **a cada
batida**, enquanto o jogador corresse. O original não faz isso: o `updateJump` dele corre no
`LivingJumpEvent`, **quando ele pula**, e soma uma vez. Um empurrão por batida é um lobo que acelera para
sempre e nunca mais para — e eu nem tinha visto, porque o salto em si não estava lá. Agora está onde o
original o tem, e com ele vem o que faltava: pular mais alto.

### A queda que perdoa

A distância da queda **encolhe** pelo tanto que o grau perdoa, e o que sobra é que dói. Um lobo de grau dez
perdoa cinco blocos; um lobisomem de grau dez, sete.

O original mexe na **distância** e não no dano, e a diferença importa: perdoando a distância, tudo o que o
jogo conta em cima dela — o dano, o barulho, o pó, a Queda Suave — continua batendo certo. Um lobo de grau dez
cai cinco blocos e não **caiu** de todo.

### O que lhe tiram, que é o que o faz duro

São dois números, e trabalham um em cima do outro:

1. a **resistência** *subtrai* — tira do golpe o que o grau aguenta, e **não vale para fogo**;
2. e o **teto** *corta* — nenhuma pancada passa dele. É o único número da tabela que **melhora baixando**:
   quatro no princípio, **dois** do quinto grau em diante.

Duas pancadas escapam ao teto, e são as duas que fazem sentido: a de **outro lobisomem**, que bate tão duro
quanto ele, e a de **prata** — que, em vez de ser cortada, **soma cinco**. É isso que faz da prata a única
coisa que mata um lobisomem de grau alto em tempo útil, e é o que dá sentido a toda a linha do Caçador de
Bruxas.

E quatro danos ficam de fora de tudo: **o vazio, a parede, o afogamento e a queda**. Um lobisomem de grau dez
que caia de cem blocos morre como qualquer um.

### A armadura rasgada

Do **nono grau** e só de lobisomem, cada golpe escolhe **uma peça de armadura ao acaso** de quem apanhou e
lhe tira **um quarto da vida dela**. O que não se gasta é **arrancado logo**, e o que se gastar até o fim cai
no chão com cinco segundos antes de se poder apanhar outra vez.

Arrancar só vale contra **gente**, como no original: é um poder feito para o combate entre jogadores, e é o
que faz de um lobisomem de grau nove uma coisa contra a qual não adianta vestir ferro.

**Uma diferença, declarada:** o original gasta a peça em nome de **quem bateu** — o jeito da 1.7.10 de gastar
uma coisa pedia um jogador e não perguntava de quem ela era. Aqui ela se gasta em nome de **quem a veste**,
que é o jeito de hoje e é o certo: quebrando, é no corpo dele que ela quebra.

### A fome que a caça mata

Do **quarto grau** e em forma de bicho, cada coisa **viva** que ele mata o **alimenta** — oito de comida e
quase uma barra inteira de fartura, de uma vez. É mais do que qualquer comida do jogo dá, e é a razão de um
lobisomem nunca precisar de cozinhar.

**Morto-vivo não alimenta**, e é a única regra: carne podre não sustenta ninguém.

### O osso que sai da terra

Do **terceiro grau** e só de lobo, bater **agachado** em grama, areia, terra, micélio ou gravilha a tira de
uma vez — sem ferramenta e sem demora, porque um lobo não tem mãos. E cavando **terra**, uma vez em vinte sai
um **osso** — dois, se a sorte for de uma em cinco —, e depois disso nada mais sai por **um minuto**.

É a menor coisa que um lobisomem faz e a que mais o faz parecer um cão. E o minuto é o que impede que cavar
terra de lobo seja uma fábrica de ossos.

### E o contágio, que é o fim da escada

Do **décimo grau** e em forma de bicho, quem ele derrubar abaixo de **um quarto da vida** apanha a
licantropia, uma vez em quatro. Um **aldeão** vira lobisomem ali mesmo; uma **pessoa** fica no grau um, com
uma vida inteira de luas pela frente.

As contas são as mesmas da mordida do Lobisomem do mundo, e as guardas também: o **conjunto prateado** protege
e quem já é lobisomem não volta ao princípio. É o que faz da escada uma coisa que **se espalha**, e é o único
poder do mod cujo efeito é outro jogador.

### Três remendos no jogo, e por que eles se provam à parte

O salto, a queda e o osso não são chamados por nada deste porte: quem passa por eles é o **jogo**. São três
remendos — no pulo, na queda e na queda de um bloco —, e um remendo que se aplica mas não acerta no lugar
certo é indistinguível de um que não existe.

Por isso há uma prova que **chama o jogo e não o porte**, e ela repara se algum deles se soltar. A da queda
não olha a vida de quem caiu — um jogador de mentira está travado no criativo e nada lhe dói — e olha o que o
jogo **devolve**: sete blocos contam como queda a quem é gente e **não contam** a um lobo de grau dez, e a
diferença entre os dois é a prova de que o remendo está no caminho.

**Guardas:** `OccultaWerewolfPowersGameTest`, com nove — o salto mais alto e o arranco de quem corre; a queda
que perdoa e para em zero; o teto, a resistência, o fogo que a ignora e os quatro danos que passam inteiros; a
prata que soma em vez de ser cortada; a armadura rasgada ao nono e não ao oitavo nem de lobo; a fome que a
caça mata e o morto-vivo que não alimenta; o osso que sai uma vez e não duas; as patas que cavam terra e não
pedra; o contágio do décimo e as duas guardas dele; e **as três costuras**, que chamam o jogo.

## O corpo do vampiro — o sangue, a sede e o sol (2026-10-03)

A outra maldição do Witchery, e o avesso da licantropia. Vale pôr os dois lado a lado antes de tudo o mais,
porque é a comparação que explica os dois:

| | o lobisomem | o vampiro |
| --- | --- | --- |
| **quem manda** | a **lua**: ele não escolhe quando muda | **ele**: escolhe a forma, o poder, a hora |
| **o preço** | pontual — larga tudo o que veste, naquela noite | **constante**: tem de beber, todos os dias |
| **a tabela** | oito números por grau | **um**: o dano, que para em três |
| **a escada** | um **altar** que lhe diz o que fazer | um **teto** que sobe quando ele **lê** |
| **o que o mata** | a **prata** | o **sol** |

Um lobisomem é servo de alguma coisa. Um vampiro não é servo de ninguém — e por isso tudo o que ele tem, tem
de pagar.

### O sangue, que é três coisas ao mesmo tempo

O <b>poder de sangue</b> é a única coisa que sustenta um vampiro, e ele faz três trabalhos de uma vez:

- é a **comida**: cinco de sangue viram um de comida, e é o único jeito de um vampiro comer;
- é o **combustível** dos poderes;
- e é o **guarda-sol**: enquanto houver sangue, o sol só castiga. Zerado o sangue, o sol **mata**.

Tirar o sangue dele é tirar as três coisas de uma vez, e é isso que faz de um vampiro uma coisa que se
joga **com pressa**.

O teto cresce com o grau — **quinhentos mais duzentos e cinquenta por grau** — e cresce **pela metade** em
quem também é lobisomem do segundo grau para cima. É a única linha do mod em que as duas maldições se olham, e
o que ela diz é claro: ser as duas custa.

### Beber, que é tudo

Com o poder de **beber** escolhido, tocar num vivo a um bloco e três décimos — **dois e um**, se ele estiver
paralisado, porque a presa não foge — tira-lhe sangue. E o que sai depende de **quem é** e de **como está**:

- de quem está **desacordado** — adormecido, ou paralisado ao quinto grau — sai **tudo**;
- de quem está **acordado** saem **dois terços**: ele se debate.

Daí vem todo o jeito de jogar de um vampiro: **não se morde quem está de pé**. A poção da Paralisia e a Maçã
do Sono deixam de ser truques e passam a ser ferramentas de caça.

E beber demais **mata**. Acima de metade do sangue a mordida quase não dói; **abaixo**, cada gole fere — e é
isso que fará do segundo degrau da escada, que pede seis goles deixando o aldeão entre duzentos e cinquenta e
duzentos e oitenta, uma coisa de pulso firme.

Três regras a mais, e cada uma diz alguma coisa:

- **sangue de bicho** dá dois e **nunca passa de um quarto do teto**. É a regra mais elegante do mod porque
  não proíbe nada: quem não quiser morder gente sobrevive, e fica preso no primeiro grau para sempre;
- **sangue de lobisomem é veneno**: não dá nada e custa **quatro de dor**. As duas maldições não se misturam;
- e morder um aldeão **tem testemunhas**: todo guarda a dezesseis blocos que esteja de olhos abertos vem
  atrás de quem mordeu.

### O sol, nos seus quatro degraus

Ao sol — **céu aberto, de dia, e sem chuva**, que é o que faz de um temporal a melhor hora de um vampiro:

1. **sem sangue**, e passados os primeiros vinte segundos de vida, ele **morre ali**: morte direta, que
   armadura nenhuma apara;
2. **do quinto grau** em diante ele **aguenta**: perde sessenta de sangue e apanha Fraqueza IV, Lentidão e
   Fadiga. Um vampiro velho anda de dia — mal, devagar e **pagando**;
3. **abaixo do quinto**, o sol lhe **zera o sangue de uma vez**. Não há aguentar: há correr;
4. e, zerado o sangue de um jeito ou de outro, ele **pega fogo**.

A ordem é do original e tem uma consequência que vale guardar: um vampiro de grau baixo que ponha o nariz ao
sol **com o sangue cheio arde na mesma**, porque o sol lhe tirou tudo antes de perguntar.

E há uma piada cruel que o original faz e que fica: **a Resistência ao Fogo não salva um vampiro**. Ardendo
com ela no corpo, ele leva dois de uma dor que é só dele. A poção que salva todo mundo é inútil justamente
para quem mais arde.

### Comida não o alimenta

A barra de um vampiro sobe por **uma porta só**, que é o sangue. Pode mastigar o que quiser e não lhe faz
nada — e é isso que faz da sede uma coisa que não se contorna com um baú de pão.

**O jeito mudou, e para melhor.** O original deixa a comida entrar e, na batida seguinte, **apaga a barra
inteira** quando repara que ela subiu — um porrete: quem comesse um pão perdia também o que já tinha dentro.
Aqui a comida simplesmente **não alimenta**, no lugar exato onde um alimento conta. E há um ganho que o
original não tinha: uma **maçã dourada ainda cura** um vampiro. O que ela deixa de fazer é sustentá-lo, que é
o que o mod queria dizer.

### E sem sangue nem comida, a maldição da sede

**Fraqueza IX, Lentidão II e Fadiga II.** Não é um aviso: é um fim de jogo em câmara lenta, e é o que um
vampiro vê quando percebeu tarde demais que a noite ia acabar.

### O painel de comando

Um vampiro não tem menu, não tem livro aberto, não tem roda. Tem **uma barra que desce** e **uma palavra** que
diz o que o clique vai fazer, e é o painel inteiro. A tecla **V** passa ao poder seguinte e, com Ctrl, liga e
desliga a visão.

A barra só aparece a quem é vampiro, e some no instante em que ele deixa de ser.

### O que falta, declarado

**Esta fatia não tinha porta de entrada**, e era preciso dizê-lo com todas as letras: nela só se virava
vampiro por comando. O caminho do original é um só e é longo — a **Lilith**, que se chama com um rito de
arame e um crânio, de noite, com um cálice de sangue de galinha tirado com a Boline. Ela veio na fatia
seguinte, **A porta de entrada**, mais abaixo, e com ela vieram a entrada e a **cura**.

Falta também, por ordem do que vem a seguir:

- a **forma de morcego** — o voo, a queda que não dói, e o gole pequeno. A pergunta já está escrita no
  `VampirePowers.emMorcego`, que hoje responde sempre que não: é a mesma costura que o `Lycanthropy` foi
  antes de a licantropia existir, e que fechou com uma linha;
- os outros **três poderes** — a velocidade, o transfixar do olhar — e os **três supremos**: o enxame de
  morcegos, o teleporte e a tempestade;
- a **escada dos dez graus**, que já está toda lida e é diferente da do lobisomem: o sangue cheio ao primeiro,
  seis goles medidos num aldeão ao segundo, dez minutos de noite ao terceiro, Lilith ao sexto, quatro aldeias
  ao sétimo, um aldeão engaiolado ao oitavo e o próprio sangue ao nono;
- o **Livro do Vampiro**, que é o que levanta o teto do grau — sem ele, nenhum feito conta;
- e o **Caixão**, a **Rosa de Sangue** e a **Guirlanda de Alho**, que são a casa dele e o que o fere.

**Guardas:** `OccultaVampirePlayerGameTest`, com doze — os números; o teto do sangue e o que ser híbrido
custa; o sangue cheio que sobe ao segundo grau e o teto de grau que o segura; o teto que nunca desce; os dois
terços de quem se debate e a mordida que fere abaixo da metade; o sangue de bicho que para num quarto; a
comida que não o alimenta e o sangue que alimenta; a maldição da sede; **o sol nos seus quatro degraus**; o
sangue de lobisomem que é veneno; a roda dos poderes e o interruptor da visão; a cura que devolve o sangue de
gente; e o corpo de quem é gente, que faz sangue sozinho. E `OccultaVampirePlayerClientTest`, com as quatro
telas da barra — sem ela, com ela cheia, com a sede a apertar, e sem ela outra vez.

## A porta de entrada — o rito, Elle e Lilith (2026-10-03)

A fatia anterior deixou o corpo do vampiro pronto e **sem porta**. Esta é a porta — e vale dizer de uma vez o
que ela é, porque o Witchery escondeu-a melhor do que escondeu qualquer outra coisa:

> Desenhe um círculo de fio-armadilha com um crânio de esqueleto no meio. Mate uma galinha em cima dele com
> uma faca que o mod nunca diz que serve para isso. Leve o cálice que ela encheu até o crânio, **de noite**, e
> toque. Uma mulher aparece. Siga-a até à lava. Lá, ela deixa de ser ela — e o que fica no lugar **tem de ser
> vencido**. Vencida, ela enche o seu cálice do próprio sangue. Beba.

Nada disto está escrito em lugar nenhum do jogo.

### O rito, e uma falha que fica

Um **crânio de esqueleto** no meio, **oito pós de redstone** à volta dele, um **anel de fio-armadilha** de
sete por sete com os cantos cortados, e **quatro tochas** nos cantos. O chão de todo o quadrado sólido, os
dois andares por cima vazios.

**O quarto noroeste do anel não é olhado.** O autor copiou o arco sudoeste duas vezes e esqueceu o outro.
Quem construir o círculo inteiro passa; quem deixar cinco fios de fora no noroeste **também passa**. Fica como
está, porque corrigir seria pedir mais do que o original pede.

Com um **cálice de sangue de galinha** na mão, tocar no crânio de noite, a céu aberto e no mundo de cima
chama **Elle** — e o crânio vai-se num raio de verdade, que acende o que estiver perto.

### Elle, que procura lava

Ela não é um chefe nem um servo: é um **guia**, e o que ela procura diz tudo sobre quem a mandou.

Recém-chamada, Elle não tem casa. De dez em dez batidas ela olha à volta, a quinze blocos, à procura de um
**lago de lava** — lava de verdade, com dois andares de ar por cima e **seis blocos de raio**. Achando um, ela
**faz dele a casa** e **esquece quem a chamou**.

Chegando lá, ela conta: às vinte batidas fala, às quarenta **deixa de existir** — e no lugar dela fica
**Lilith**, com um estouro de seis.

É por isso que o rito não é o fim: quem chamar Elle no meio de um campo fica com uma convidada que não tem
para onde ir. **O jogador tem de a levar até à lava** — ou cavar até ela, ou fazer-lhe um lago de treze
blocos de boca. O mod nunca o diz; ela é que mostra, voando sempre para o mesmo lado.

E ela **não arde**, porque a casa dela é um lago de lava.

### Lilith, que não se pode matar

É a melhor ideia do mod inteiro, e é por isso que ela merece ser dita devagar.

Ela é um chefe a sério: **duzentos de vida**, **teto de doze por pancada**, e **sara cinco por segundo** — o
que, por si só, a torna invencível. Há duas maneiras de lhe tirar a cura, e as duas são lições:

- **enregelá-la** ou **enfraquecê-la**, e então ela sara só um;
- ou **devolver-lhe o fogo dela**: uma bola de fogo grande na cara tira-lhe a cura por dez segundos. O mod
  responde assim a quem reparar que ela se cura com fogo na mão.

Enquanto isso ela **apaga a Resistência ao Fogo** de quem estiver a trinta e dois blocos e chove bolas de
fogo pequenas em cima deles — a primeira coisa que ela tira é a poção que o jogador bebeu para a enfrentar —,
e atira **feitiços**: metade das vezes que ataca, e de cada três desses, um é fogo e dois são símbolos.

**E então ela não morre.** Levando o golpe que a mataria, ela volta à vida cheia, perde o que a prendia, fica
**amiga**, e **aparece ao lado** de quem a venceu. O combate com ela nunca foi um combate: foi uma **prova**.

*(E o `/kill` também não a mata. Ele chama o mesmo morrer que o combate chama, e ela responde do mesmo jeito.
Descobri isso numa foto que saiu errada, e é o melhor atestado que a fatia podia ter.)*

### Os cinco feitiços dela

Vêm do sistema de **símbolos** do mod — o das infusões, que este porte ainda não tem. Estão aqui porque **ela
os atira**, e sem eles o combate seria só bolas de fogo. Quando o sistema vier, é daqui que eles saem.

Os pesos contam a história: **Flipendo** e **Attraho** cinco vezes mais prováveis do que os outros três. Ela
passa o combate a **empurrar e a puxar** — a atirar o jogador para longe e a trazê-lo de volta —, e só de vez
em quando cega, prende ou queima.

E o **Ignianima** é o mais bonito dos cinco: ele dói **mais quanto mais ferida ela estiver**. Uma Lilith
inteira queima por dois; uma Lilith quase vencida queima por nove. O combate fica **pior à medida que se
ganha**, e é de propósito.

### O que ela dá

Amiga, ela olha o que se traz na mão, e some depois — é uma visita, e uma só:

| o que se traz | o que ela faz |
| --- | --- |
| um **Cálice**, de quem não é vampiro | o enche do **sangue dela**: bebê-lo é **virar vampiro** |
| **alho**, de quem é vampiro | **cura** — e é a única cura que há |
| uma **papoula**, ao sexto grau | dá o sétimo, que é o do voo de morcego |
| qualquer outra coisa encantável | **a encanta** como uma mesa de nível quarenta, e de graça |

### O Cálice e a Boline

O **Cálice de Vidro** enche de três maneiras, e as três são degraus diferentes: com **sangue de galinha**
sacrificada com a Boline sobre o rito — e esse serve para chamar, não para beber —; com o **sangue de
Lilith**; e com o **próprio sangue** de um vampiro do nono grau, que gasta cento e vinte e cinco de poder para
o encher, porque o nono degrau da escada pede que ele beba o seu.

Beber um cálice de sangue que **não** é de galinha, não sendo já vampiro, vira. É a entrada do mod inteiro, e
é de propósito que ela seja tão estreita: quem não procurou Lilith não entra por acaso.

A **Boline** é a faca de colher do ofício: bate como uma de madeira e dura como uma de ferro, e corta folha,
teia, grama, trepadeira e fio-armadilha **sem se gastar**. O que ela faz de especial — sacrificar a galinha —
o mod nunca explica.

### O que se desenhou

**Lilith** é o modelo mais estranho deste porte: chifres virados para trás, dois dentes a sair da boca, um
nariz de um pixel, e **duas asas chatas** presas aos braços que descem até abaixo dos pés.

E a **saia** são duas peças iguais no mesmo lugar — o truque mais bonito do modelo: uma segue a perna que está
mais **atrás** e a outra a que está mais à **frente**, de modo que andando a saia se abre sozinha. Parada, a
da frente fica em dois décimos e a saia fecha.

Os braços dela nunca param: mesmo imóvel há um balanço de cinco centésimos no ombro e no cotovelo, tirado do
relógio do mundo e não do passo. É o que a faz parecer **viva** em vez de posta.

**Elle** é, por fora, **gente** — o original usa o corpo de um biped tal e qual —, e é de propósito: ela não
parece um monstro. Parece uma mulher parada no meio do campo.

E o **feitiço** no ar é uma chapa virada para quem olha, com a figura da bola de neve do jogo tingida da cor
do símbolo. Não é um modelo: é uma **mancha de luz**, e cada um dos cinco tem a sua cor e o seu tamanho, de
modo que se aprende a reconhecê-los de longe.

### O que falta, declarado

A porta está inteira: dá para virar vampiro, e dá para deixar de ser. O que falta do ramo do vampiro é o que
vem **depois** da porta:

- a **forma de morcego** e os outros três poderes, e os três supremos;
- a **escada dos dez graus**, de que esta fatia já traz dois degraus — o **sexto**, que é a papoula na mão de
  Lilith, e o **nono**, que é o cálice do próprio sangue;
- o **Livro do Vampiro**, que levanta o teto do grau, sem o qual nenhum feito conta;
- e o **Caixão**, a **Rosa de Sangue** e a **Guirlanda de Alho**.

**Guardas:** `OccultaLilithGameTest`, com sete — o rito que se lê desenhado e não se lê sem uma tocha, com
coisa em cima, ou com o crânio errado; a galinha que enche o cálice, e só com a Boline na mão e o rito
debaixo; o sangue dela que vira e o de galinha que não vira; **Lilith que não se pode matar**, que é a prova
que carrega a fatia; o que ela dá e a cura pelo alho; Elle que não acha casa num campo seco; o lago que é um
lago e a poça que não é; e as cinco coisas no jogo. E `OccultaLilithClientTest`, com cinco telas: Lilith de
frente, de perto e de lado, Elle, e a porta no inventário.

## A forma de morcego e os cinco poderes (2026-10-03)

A fatia anterior deixou a **porta**: dá para virar vampiro e dá para deixar de ser. Esta é o que vem depois
dela — e o que vem depois dela é uma lista curta que diz tudo sobre o que um vampiro é no Witchery:

| o poder | custa | ao grau |
| --- | --- | --- |
| **beber** | nada | primeiro |
| **prender pelo olhar** | 50 | segundo |
| a **velocidade** | 10 | quarto |
| a **forma de morcego** | 50, e **1 por volta do relógio** | sétimo |
| o **Supremo** | 50, e uma das cinco cargas | décimo |

**Nenhum dos cinco é um golpe.** Prender deixa a presa quieta, correr o leva mais depressa, o morcego o leva
por cima, e os três Supremos mudam o tempo, chamam bichos ou o põem noutro lugar. Um vampiro de décimo grau
tem cinco poderes e nenhum deles serve para ganhar uma briga. Ele ganha por **chegar antes**.

### A escada que não salta: ela não chega lá

A conta de quantos poderes ele tem não é o grau. É uma tabela — `{0,1,2,2,3,3,3,4,4,4,5}` — e por isso subir
de grau nem sempre dá um poder novo. Passar ao poder seguinte **para no último que o grau dá**, e a volta
seguinte o devolve a *nenhum*: um vampiro de segundo grau nunca vê a palavra "morcego" na tela.

É assim que o mod conta a escada sem escrever uma linha. A palavra aparece no dia em que ele a merece.

### A velocidade, que dobra

O ramo mais curioso do original. Cada uso **dobra** a Rapidez que ele já tem — dois, quatro, oito — e o grau
diz até onde: `ceil((grau-3)/2)` doses. Um vampiro de quarto grau corre **uma vez**; um de décimo, quatro.

E cada dose **soma três segundos** ao que já estava correndo em vez de recomeçar. Quem quiser a velocidade
cheia tem de a construir dose a dose **antes** de precisar dela — e quem a deixar acabar recomeça do dois.

### Prender, que é metade do laço

Cinquenta de sangue, e só em **gente**: aldeão, jogador ou guarda. Um bicho não se prende olhando, e um
**aldeão que vira** também não, porque o que corre nele já é outra maldição.

Quem apanha fica paralisado por `5 + grau/2 + max(0,(grau-4)/2)` segundos, no **quarto** grau da poção — ou
no **quinto**, do oitavo grau em diante. E é esse número que faz tudo: do quarto para cima a presa **conta
como desacordada** e dá todo o sangue que se lhe pede, em vez de dois terços.

Prender e beber é o laço inteiro de um vampiro, e é por isso que o prender abre **dois graus antes** da
velocidade.

*(Falta, declarado: o original soma três segundos a quem veste as **roupas de vampiro**, que este porte ainda
não tem.)*

### A forma de morcego — e ela não é um morcego: são três

Cinquenta para entrar, **um por volta do relógio** para ficar, e sair é de graça. Nela ele mede **três
décimos por seis**, com os olhos a oito décimos da altura: a menor caixa que o mod dá a um jogador, e a
câmara desce para meio metro do chão. Ele voa, e voando a queda não conta.

E a pancada dele **não vale nada**: **menos seis** de dano. No original são duas tabelas diferentes, e a do
morcego **substitui** a do vampiro em vez de se somar — de modo que um vampiro de décimo grau, que bate três
a mais, em forma de morcego bate seis a menos. O gole de sangue dele também passa de dez a dois: um morcego
não tem boca para mais.

É o poder que mais muda o jogo e o que menos serve para brigar, e é **de propósito**.

**E ele não é um morcego.** O original desenha o bicho de mentira **três vezes**: um no lugar do jogador e
dois atrás, a três quartos de bloco, seis décimos mais baixos, a oito décimos do tamanho e com as asas duas e
sete batidas fora de compasso. O que atravessa um vale no Witchery é uma **nuvenzinha** de morcegos. Nenhuma
linha do mod o diz: quem vira morcego descobre que virou **vários**.

#### Um erro do autor que fica

Os dois de trás saem de uma volta do vetor do olhar, e o original pede essa volta **em graus** a um método que
a conta **em radianos**: escreve `90` e `-180` onde queria noventa e cento e oitenta graus. Noventa radianos,
descontadas as voltas inteiras, dão cento e dezesseis graus e meio; o segundo pedido devolve o vetor ao outro
lado.

O resultado é **simétrico por acaso** — um de cada banda, atrás dele. É o que se vê no jogo, e por isso fica
como está: os números do original, e não os que ele queria.

### Os três Supremos

Escolhem-se no **Crisol de Sangue**, cheio, ao décimo grau, e as três coisas que os escolhem **dizem** o que
dão: a **alcachofra-d'água**, que é planta de lago, chama a **tempestade**; a **lã de morcego** chama o
**enxame**; e o **osso** — o que resta de um morto — chama o **caminho de casa**. Ninguém precisa de ler isso
em lugar nenhum.

Cada um vem com **cinco usos**, e escolher outro **troca** o que havia, levando o que sobrava com ele.

- **A tempestade** é o mais calado e o mais útil: cinco a quinze minutos de chuva com trovão. Um temporal tira
  o sol, e sem sol um vampiro **anda de dia**. Ele não ataca ninguém — muda o mundo para caber nele. Chovendo
  já, não faz nada.
- **O enxame** são quinze morcegos que vão ao que ele estiver olhando a trinta e dois blocos, doem quatro e
  **morrem no primeiro corpo** que apanham. Não são servos: são **tiros que voam torto**. Param em paredes,
  perdem o alvo quando ele vira a cara, e de nenhum deles cai nada — quinze por uso seriam uma fábrica de
  couro.
- **O caminho de casa** o leva à cama dele; e estando **em casa**, a seis blocos dela, leva-o à **aldeia mais
  perto**. O Supremo da colheita leva o vampiro ao rebanho dele.

### Duas linhas que vinham junto

O relógio do vampiro ganhou o que lhe faltava do mesmo pedaço do original: **ele não se afoga** — o ar volta
ao cheio enquanto estiver na água —, e é isso que faz do fundo de um lago o único lugar onde ele está a salvo
**de dia**. O original não o diz em lugar nenhum.

E **não se é lobo e morcego** ao mesmo tempo. No original as duas maldições partilham um único contador de
forma e a mistura é impossível por construção; aqui são dois apegos separados, e por isso a regra é escrita à
mão: de lobo não se vira morcego, e **virando lobo o morcego cai**. A lua ganha.

### Um defeito antigo que esta fatia desenterrou

O **Amuleto Voador** do Maleficium tirava as asas de **quem quer que as tivesse**. A conta dele era "sem o
amuleto e com asas, tira as asas", e isso apanhava a poção de Voo, o criativo, outro mod — e a forma de
morcego, que perdia o voo na batida seguinte à de o ganhar.

O `ArcanaEffects` já tinha resolvido isto com uma **marca** de quem recebeu asas dele, e o javadoc dele até o
diz. O amuleto passou a usar a mesma marca. Foi o morcego que o encontrou: um jogador que caía do céu sem
explicação nenhuma, três de dano de cada vez.

### O que se desenhou, e as duas costuras

O botão de usar **é** o poder: com um poder escolhido, o clique direito deixa de abrir baús e passa a ser
esse poder. É o painel de comando inteiro de um vampiro — ele não tem menu, não tem roda, não tem varinha.
Tem uma palavra no canto da tela e o botão que já usava. Por isso o clique é engolido mesmo com o **beber**
escolhido, que não se usa no ar: um vampiro com a boca pronta não põe uma tocha na parede por acidente.

E a palavra diz outra coisa no Supremo: o nome do dom com os **usos que restam** entre parênteses. É o único
número que um vampiro vê além do sangue, e saber que ele está em zero **antes** de apertar é tudo.

Falhando — por falta de sangue, de grau ou de forma —, o que se ouve é um **toque de caixa**, e nenhuma
palavra. O mod nunca explica por quê.

**Declarado:** a espera de meio segundo entre dois usos é, no original, um contador que desce uma vez por
batida. Aqui é a **batida em que ele usou**, e a espera se mede contra ela — a mesma espera, sem olhar todo
jogador do mundo sessenta vezes por segundo para tirar um do nada.

### O que falta, declarado

- a **escada dos dez graus** do vampiro, de que já há três degraus — o sexto e o nono da fatia da porta, e
  agora o décimo, que é o Crisol;
- o **Livro do Vampiro**, que levanta o teto do grau, sem o qual nenhum feito conta;
- as **roupas de vampiro**, que somam três segundos ao prender;
- e o **Caixão**, a **Rosa de Sangue** e a **Guirlanda de Alho**.

**Guardas:** `OccultaVampirePowersGameTest`, com treze — os cinco poderes e a escada que para onde o grau
para; só gente se prende, e o que o grau faz à paralisia; a velocidade que dobra e para, e que de morcego não
há; o morcego que custa cinquenta e um por volta, que **bate menos do que gente** e a quem a queda não pega,
e que não se mistura com o lobo; os cinco usos que o Crisol dá; a tempestade que não se chama duas vezes; o
enxame de que nada cai e o morcego dele que se gasta no primeiro corpo; o caminho de casa; o criativo que
nunca paga; a visão que é um interruptor; e a espera que conta mesmo quando falha. E
`OccultaVampireBatClientTest`, com seis telas: de gente, os três por trás, os três de frente, os três no ar, o
Supremo com as cargas, e o morcego do enxame.

## A escada dos dez graus do vampiro, e o livro que a destranca (2026-10-03)

As fatias anteriores deram o corpo, a porta e os poderes. Esta é a **escada** — e ela é o contrário da do
lobisomem em tudo, o que conta tudo sobre as duas maldições.

O lobisomem tem uma **estátua** que lhe diz o que fazer: ele chega, ela manda, ele volta. O vampiro **não tem
ninguém**. Ninguém lhe diz nada, nada no jogo aponta para o degrau seguinte, e o que ele sobe, sobe por ter
reparado.

O que ele tem é um **livro**. E o livro não é um manual.

### Observações de um Imortal

É o diário de um erudito condenado que jantou com um vampiro e anotou o que ele contou — em reticências,
meias-frases e dois desenhos à pressa. O autor não sabia que estava escrevendo as instruções de uma escada.

Lê-lo **levanta o teto do grau** até o número de páginas que ele tem, e isso é tudo o que ele faz. Um
vampiro sem livro sobe ao segundo e ao terceiro grau e **para ali para sempre**, por mais aldeões que morda —
e nada no jogo lhe diz por quê. É a coisa mais cruel que este mod faz, e é o que põe o livro no centro do
ramo.

E o livro chega **rasgado**: vem sem nenhuma das nove páginas. Cada capítulo pede um número delas, e um
capítulo que peça mais do que o exemplar tem **não abre** — a seta fica apagada. O Witchery nunca escreve
"falta-te uma página". Ele mostra a seta que não anda.

**De onde ele vem:** há um garantido no baú da **livraria de aldeia**, e a receita dele pede uma **Estrela
do Nether**. Quem o quiser antes de ter matado o Wither tem de o encontrar.

**De onde vêm as páginas:** elas **só caem de quem morre pela mão de quem já traz um livro incompleto**. É a
única coisa do mod que funciona assim. Quem nunca achou o primeiro exemplar nunca verá uma página cair, por
mais que mate. E a lista de quem as larga conta uma história — **chefes** sempre, **aldeões** uma em dez,
**zumbis-porcos e endermen** nove em cem, qualquer **morto-vivo** duas em cem: elas estão com os aldeões, com
os mortos e com as coisas que andam entre mundos. Quem quiser o livro inteiro tem de fazer o que o vampiro do
diário fez.

### Os dez degraus

| degrau | o que ele pede |
| --- | --- |
| 1 | **virar** — beber sangue de Lilith, ou de outro vampiro, num Cálice |
| 2 | **encher o sangue** até o teto, uma vez |
| 3 | **cinco aldeões** mordidos sem os esvaziar |
| 4 | **dez minutos de noite**, acordado |
| 5 | **queimar-se dez vezes** com o sol engarrafado |
| 6 | **vinte Blazes** |
| 7 | **Lilith outra vez**, com uma papoula |
| 8 | **quatro aldeias** diferentes |
| 9 | **cinco aldeões em gaiolas** |
| 10 | **fazer outro vampiro** |

Repare na forma deles. Três pedem **moderação** — morder sem matar —, dois pedem que ele **ande**, um pede
que ele se **machuque**, e o último pede que ele **faça o que lhe fizeram**. Não há um único degrau que peça
matar um chefe ou achar um tesouro. A escada do vampiro é uma escada de **hábitos**, e é por isso que ela
leva tanto tempo.

### Morder sem esvaziar, que são dois degraus

De quinhentos de sangue, parar entre **duzentos e cinquenta e duzentos e oitenta** são três goles e meio. A
faixa é estreita de propósito, e a crueldade está no resto: beber **demais** não falha a mordida, **apaga a
conta inteira**. Quatro aldeões bem mordidos e um mal mordido valem zero.

O nono degrau pede o mesmo, só que em **gaiolas**: um anel de barras de ferro de dois andares à volta do
aldeão, das dezesseis posições ao menos **quinze** com barra — uma fresta, e só uma —, e por cima um teto
inteiro de nove blocos. O livro descreve a gaiola páginas antes de o jogador ter de a construir, e não diz
para quê.

### Queimar-se de propósito, que é o melhor degrau do mod

Para **aguentar** o sol, ele tem de aprender a **levar** o sol — e a única maneira de o levar sem morrer é em
doses. As doses vêm da **Granada Solar**, que é sol engarrafado, e a granada vem do **Coletor de Luz**.

E o Coletor não se enche à pressa. Põe-se nele uma **Esfera de Quartzo** vazia, e ela sobe **de um em um**:
cada degrau precisa de um **Sensor de Luz Solar** encostado a marcar exatamente um a mais do que ela já tem.
Não serve pô-la ao meio-dia e esperar. **Encher a esfera é ver um dia inteiro nascer** — e quem a tirar a
meio perde a manhã, porque não há meio-sol.

Cheia, ela dá uma granada. Atirada, a granada voa, **para no ar onde bate** — a gravidade dela some no
impacto — e fica ali um minuto a alumiar, com um bloco de luz a acompanhá-la. Ao fim do minuto estoura,
devolve a esfera, e queima os mortos-vivos a três blocos.

**E um vampiro é um morto-vivo.** Dez vezes. Nada no jogo explica por que alguém haveria de se queimar de
propósito; o livro o diz em reticências, e quem não o ler nunca saberá.

### O Caixão, e o fim da escada

O Caixão é uma **cama com tampa**. O jogo trata-o como cama — guarda o ponto de renascer, estoura no Nether
—, mas agachado o clique **abre e fecha a tampa**, as duas metades ao mesmo tempo, e de tampa fechada não se
dorme. E ela não levanta debaixo de um bloco: um caixão enterrado fica enterrado.

Por fora é preto; por dentro é **azul**, que é o forro. O modelo são seis peças e uma tampa de três chapas
que gira em volta da borda esquerda, com a curva cúbica dos baús — e é essa curva que a faz parecer pesada.

E ele é a última coisa que a escada pede. O **décimo degrau** quer quatro coisas ao mesmo tempo e nenhuma
delas é um golpe: um **Cálice do próprio sangue**, uma presa **presa** — paralisia no quinto grau, que é o
que o prender de um vampiro do oitavo grau dá —, a presa **vazia**, e um **Caixão a quatro blocos**.

Fecha o círculo: o que o fez vampiro foi um cálice de sangue dado por alguém ao pé de um caixão, longe do
olhar do sol. A última página do livro mostra o erudito a receber esse cálice, e acaba aí.

### Duas correções e um desvio declarado

**O teto do grau tem chão.** No original ele nasce em zero, e os dois primeiros degraus — encher o sangue e
os cinco aldeões — **não perguntam** por ele; só do terceiro em diante é que alguém o olha. O porte tinha
copiado a pergunta para um lugar só, e com isso um vampiro recém-nascido ficava preso no primeiro grau para
sempre. Agora o chão é escrito — **três** —, e o que o jogador sente é idêntico ao original. A prova que
dizia o contrário foi corrigida e diz porquê.

**A espera não trava quem acabou de nascer.** A espera de meio segundo entre poderes mede-se contra a batida
do último uso, e ela começava em zero — de modo que um jogador nas suas dez primeiras batidas não conseguia
usar poder nenhum. Começa agora meio segundo atrás.

**O desvio:** a folha do livro traz o **pedaço da marcação que este livro usa** — quebra de linha, título,
cor, figura e a seta com o número de páginas que ela pede. A marcação inteira do original tem modelos, listas
de itens e marcadores, e serve os outros três livros do Witchery, que não estão portados. Quando eles vierem,
é daqui que saem.

### E duas provas antigas que esta fatia desenterrou

A suíte corre num **mundo só**, e as arenas são postas lado a lado: trinta e duas provas novas mudaram quem
fica ao lado de quem, e duas provas antigas que dependiam disso caíram.

A do **apanhador de sonhos** contava com o algodão à volta baixar a conta, mas o algodão só conta **até
dois** — e se as provas ao lado já tiverem gasto os dois, o dela não tem onde entrar. Agora ela conta o que
já está ali antes de exigir a descida.

A do **chamado dos bichos** largava um camelo a quarenta blocos, que é dentro da arena de outra prova — e
essa arena varre-o quando se arruma. Agora o camelo fica dentro da arena dela, a dez blocos, que é mais do
que os cinco e meio que o rito pede para o considerar longe; o alcance de cento e vinte e oito está provado à
parte.

**E uma falha minha, declarada:** as provas da fatia anterior — os cinco poderes do vampiro — **não estavam
na lista de entrada do Fabric**, e por isso nunca correram. Entraram agora, com as desta fatia, e a conta da
suíte passou de 1074 a 1106. Três coisas que elas apanharam estão corrigidas acima.

### O que falta, declarado

- a **Rosa de Sangue** e a **Guirlanda de Alho**, que são o que fere um vampiro;
- as **roupas de vampiro**, que somam três segundos ao prender;
- e a **Granada Duplicadora**, que é o segundo modo da mesma criatura e pede a Seguidora do tipo cinco.

**Guardas:** `OccultaVampireLadderGameTest`, com doze — os números dos dez degraus; **o teto que para no
terceiro sem livro**, que é a prova que carrega a fatia; os cinco aldeões e a mordida a mais que apaga tudo;
a gaiola de quinze barras com uma fresta e não duas; a noite que não conta de dia; os vinte Blazes e a ovelha
que não vale; as dez queimaduras; as quatro aldeias que se esquecem quando o grau muda; as quatro coisas do
décimo degrau, uma a uma; as páginas que só caem para quem já tem o livro; o Coletor que sobe de um em um e
dá a granada cheio; e a tampa do Caixão que abre as duas metades e não abre debaixo de um bloco. E
`OccultaVampireBookClientTest`, com oito telas: o índice, o rito desenhado, a gaiola desenhada, a seta
apagada, o caixão fechado e aberto, o coletor pela metade e cheio, e as coisas novas no inventário.

## O Ent e o Lobisomem com os corpos do Mo'Creatures (2026-10-03)

**Esta fatia quebra a regra do porte de propósito, e é a primeira que o faz.**

Tudo o resto deste repositório é fiel: quando o original está errado, fica errado; quando o original é feio,
fica feio. Aqui não. Dois modelos do Witchery foram **trocados pelos do Mo'Creatures** porque os do Witchery
são simples demais, e porque um bicho que não impressiona não assusta — e tanto o Ent como o Lobisomem são
bichos cujo trabalho inteiro é assustar.

### O que havia, e o que há

**O Ent do Witchery** são dezesseis caixas: um paralelepípedo de dezesseis por quarenta e oito, quatro
borrões de folha por cima, duas tábuas por braços e oito palitos por raízes. Funciona, mas não é uma árvore
que anda — é uma caixa com cara.

**O do Mo'Creatures** são quarenta e quatro peças: braços em cinco pedaços cada (ombro, braço, pulso, mão e
**dedos**), pernas em cinco (perna, coxa, joelho, tornozelo e um pé que se inclina quinze graus para a
frente, como raiz que pisa), uma cara com pescoço, rosto, testa, **nariz** e boca, e uma copa de **dezesseis
blocos de folha** em dois andares à volta de um tronco.

E ela anda: braços e pernas em compasso oposto, os pulsos com um balanço lento tirado do relógio do mundo, e
a **copa inteira vira com a cabeça** — dezesseis blocos de folha a rodar quando ele olha para o lado.

**O Lobisomem do Witchery** é um boneco de jogador com um focinho: o mesmo corpo de seis caixas que todo
biped tem, com uma cabeça de lobo por cima. De longe não se distingue de um zumbi de chapéu.

**O do Mo'Creatures** são quarenta e quatro peças: uma cabeça em nove — crânio, focinho, nariz, **dentes de
cima e de baixo**, boca, duas orelhas e duas suíças —, pescoço em dois pedaços inclinados, peito e barriga
separados, **rabo em quatro segmentos**, braços e pernas em três pedaços cada, e **cinco dedos em cada mão**.

Dez dedos. É o detalhe que diz tudo: o do Witchery tem dois cubos por mãos.

### O que não mudou

**Nada de comportamento.** O Ent continua sendo o Ent do Witchery — o que o faz nascer, o que ele larga, como
ele bate, o pender de planta por cima do passo. O Lobisomem continua sendo o do Witchery em tudo o que não é
desenho, e a troca apanha de uma vez as **três** coisas que vestiam aquele corpo: o bicho, o aldeão que vira,
e a **forma de lobisomem de um jogador**.

**Os tamanhos.** O Ent do Mo'Creatures é de um bicho de quase oito blocos; o deste mod mede três de caixa e
desenhava-se com quatro e meio. Os **seis décimos** de escala põem o corpo novo exatamente onde o velho
estava, de modo que a troca seja de **feitio** e não de tamanho.

**As malhas antigas ficam escritas.** O `WolfmanModel` continua no repositório, inteiro e comentado, e o que
sobrou dele em uso é a **camada**. Quem quiser o lobisomem do Witchery de volta troca uma linha no
`ThaumcraftClient`.

### As texturas vêm junto, e tinham de vir

Um corpo novo pede o desenho que foi feito para ele: as coordenadas de textura do Mo'Creatures não têm nada
que ver com as do Witchery, e a pele antiga no corpo novo daria um borrão. Então vieram as duas — o
`ent_oak` e o `brownwerewolf` —, e com elas a casca com musgo e os olhos cor de brasa do Ent, e o pelo
castanho com os olhos vermelhos e os dentes do Lobisomem.

### E a regra, para o que vier depois

Esta é a exceção, não o novo costume. O resto do porte continua fiel, e qualquer outra troca de gosto tem de
ser pedida e escrita aqui do mesmo jeito: **o que se trocou, por que se trocou, e o que ficou guardado para
quem quiser o original de volta**.

## A Rosa de Sangue e a Guirlanda de Alho (2026-10-03)

As duas pontas do ramo do vampiro: a que **prende** e a que **defende**. E a primeira é, sem exagero, a coisa
mais sinistra que este mod tem.

### A Rosa de Sangue

Uma flor pequena que **se lembra de quem pisou nela**. Quem passar por cima deixa o nome lá dentro, e a flor
**fecha** — muda de desenho, e quem olhar vê que ela comeu. Depois disso, um **Frasco de Vínculo** encostado
nela sai **cheio daquela pessoa**, sem que ela jamais tenha sido tocada.

É isso: um **vínculo à distância**. Todo o resto do ofício que prende alguém — a boneca, a maldição, o
espelho — precisa de um fio de quem se quer, e um fio pede um **encontro**. A rosa não pede. Planta-se no
caminho de alguém e espera-se.

**E ela não se colhe.** Quebrada de qualquer jeito, não deixa nada. Só a **Boline** a tira do chão — e tirada
com a Boline ela sai **com quem tem dentro**, de modo que se pode arrancar a flor que apanhou alguém e
levá-la para casa. Plantada outra vez, volta cheia.

No Altar ela vale **dois de poder**, contando até dez.

#### De onde vem a primeira, e um elo que falta

Um **baú vazio**, com **quatro flores** à volta e **água por baixo**, passado a **Mutandis Extremis**: o baú
some, e no lugar das quatro flores ficam quatro rosas.

**Declarado:** no original esta corrente tem um elo no meio. O baú comum vira um **Apanha-Erva**, e é o **Baú
de Sanguessugas** — que também guarda nomes, e também se faz assim — que vira as rosas. Nenhum dos dois está
portado, e por isso o baú comum faz aqui o que o Baú de Sanguessugas fazia lá. Quando eles vierem, a corrente
ganha o elo de volta.

### A Guirlanda de Alho

Cinco cabeças de alho enfiadas num cordel e penduradas numa parede. É a coisa mais barata do ramo — cinco
alhos e dois fios — e a única defesa contra um vampiro que não pede ofício nenhum.

Um **vampiro** que encoste nela é **empurrado**, com o mesmo empurrão do anel de proteção do ofício, e isso
vale tanto para o bicho como para **um jogador que seja vampiro**. E um vampiro que **bata nela** para a
arrancar **pega fogo**, um segundo.

Repare no que isso significa para quem joga de vampiro: a casa de qualquer aldeão com uma guirlanda à porta
passa a ser um lugar de onde ele é **cuspido para fora** sem poder sequer tirar o alho sem se queimar. É a
primeira coisa deste mod que torna o jogador **indesejado na própria aldeia**, e custa cinco alhos.

No criativo ela não faz nada, nem empurra nem queima.

### O que se desenhou

O modelo da guirlanda são **vinte e nove caixas** numa chapa de trinta e dois, e a graça está em como uma
cabeça de alho é feita: um **talo** de um pixel de grosso com **quatro chapas penduradas nele**, cada uma mais
larga do que a de cima — três, cinco, sete e quatro. É a silhueta de um bolbo visto de fora, estreito no
pescoço e bojudo no meio, feita com quatro caixas e **nenhuma rotação**.

As cinco penduram-se em **ziguezague**, e os quatro cordéis as ligam em **V**, com as caixas encolhidas
quatro décimos — que é o truque de 2014 para um fio parecer um fio e não uma tábua.

E a rosa tem **dois desenhos**, aberto e fechado. A diferença entre eles é tudo o que denuncia que ela comeu
alguém: iguais, a flor deixaria de avisar e passaria a ser uma armadilha perfeita, que é exatamente o que ela
não deve ser.

### Uma prova antiga que esta fatia derrubou

A da **Pedra de Caminho** largava o destino a quarenta blocos para o lado — que é dentro da arena de outra
prova, e essa arena varre o que lá estiver quando se arruma. Passou a largá-lo **quarenta blocos para cima**,
onde arena nenhuma mora. É a terceira prova deste porte a cair por causa disso, e as três ficaram
independentes da vizinhança.

**Guardas:** `OccultaRoseAndGarlicGameTest`, com cinco — a rosa que se lembra de quem pisou e o frasco que o
tira de lá, que é a prova que carrega a fatia; a Boline que a colhe com quem ela guarda, e a replantação que
o devolve; os dois de poder no Altar; a guirlanda que se pendura numa parede e cai com ela; e a que não
estorva ninguém que não seja vampiro. E `OccultaRoseAndGarlicClientTest`, com quatro telas: as duas rosas
lado a lado, a guirlanda de frente e de lado, e as duas no inventário.

## O Demônio, e o Coração que ele vende (2026-10-04)

O Witchery tem um vendedor que não é um aldeão. É alto, tem chifres, não se mata batendo, e vende uma coisa
que nenhuma outra parte do mod dá: um **Coração de Demônio**.

### Primeiro, uma lição que esta fatia ensinou

Os fontes de 1.7.10 estão em nomes SRG: `Items.field_151064_bs`. Eu li `field_151064_bs` e escrevi **pó de
blaze**, porque estava ao lado de uma vara de blaze e o número era vizinho do `field_151065_br`, que é o pó.

`field_151064_bs` é **creme de magma**.

Um nome errado e três coisas saem erradas de uma vez: a moeda que o demônio cobra, o que ele deixa quando
morre, e **o que o faz estourar**. E a armadilha dele deixa de funcionar, porque o pó de blaze é justamente o
que o **manda embora** no rito de banir — quem lesse o mod porteado aprenderia o contrário do que o original
ensina.

As tabelas que traduzem isso estão no maven da Forge e agora estão baixadas em
`Base Extras/mcp_stable_12-1.7.10/`. **Nenhum campo SRG se adivinha mais.**

### O que ele é, por dentro

Um **golem**. Literalmente: o original estende o golem de ferro, e isso explica tudo o que ele faz de
estranho. Anda com o passo duro do golem, atira as pernas na **curva de triângulo** em vez do seno de gente,
e **oscila seis graus e meio** de lado a cada passo.

Cem de vida — e o número que conta é outro: **nada lhe tira mais do que quinze de uma vez**. Uma espada de
diamante encantada ao máximo demora o mesmo que uma de madeira a derrubá-lo, e o que decide a luta deixa de
ser a arma e passa a ser a **paciência** de quem bate. É o jeito do original de dizer, sem uma linha de
texto, que ele não é para matar.

De perto bate **sete mais até quinze**, e com o golpe vem um **empurrão para cima** — o truque do golem, que
manda quem lhe chega perto pelos ares. De longe atira **bolas de fogo grandes**, as do ghast, e cobra quinze.
É imune ao fogo, **não se afoga nem gasta o ar**, e **brilha sempre**: o `getBrightness` dele devolve um, de
modo que um demônio parado no escuro parece trazer a luz com ele.

E ele **some sozinho** se ninguém estiver perto, como qualquer bicho. Só o demônio **chamado por alguém**
fica — e é o rito de chamar que o marca. O Inferno na Terra não marca os dele, e por isso o rito mais caro do
mod dá demônios que vão embora se ninguém estiver olhando.

### O negócio

Ele é um **Merchant** — abre a mesma tela de trocas de um aldeão, com a mesma barra e os mesmos botões, e é
isso que faz a cena funcionar: a interface diz "mercador" e a criatura diz "demônio", e o jogador tem de
decidir em qual das duas acreditar.

A lista se monta nesta ordem, e a ordem é tudo:

1. **tantos livros encantados quantas trocas vão caber** — de seis a nove —, cada um com um encantamento
   sorteado e um preço que sobe com o grau;
2. uma em quatro vezes cada, **Pó Espectral** e **Língua de Cão**; e uma em seis e dois terços cada, **Sopa
   de Pedra Vermelha**, **duas lágrimas de ghast** por um diamante e **duas pérolas do fim** por um diamante;
3. a lista se **embaralha**;
4. o **Coração** é enfiado num dos **três primeiros lugares**;
5. e então se **corta** no número de trocas que saiu.

Como os livros entram primeiro e enchem a lista, o embaralhar é o que decide quais das coisas do ofício
sobrevivem ao corte. E o Coração, enfiado **depois** de embaralhar, nunca é cortado: um demônio pode não ter
uma única coisa do ofício para vender, mas tem sempre o coração.

Cada troca se faz **duas vezes** — sete de um pedido novo menos as cinco que o original desconta. Um demônio
vende dois corações e nunca mais.

#### A moeda, e a armadilha

Cada troca é cobrada **numa moeda sorteada só para ela**, e não uma por demônio: **um em cinco** em vara de
blaze, um em cinco em **creme de magma**, um em dez em diamante, um em quatro em esmeralda, e o resto em
ouro. O preço é o mesmo em valor; o que muda é quantas peças dele cabem numa esmeralda. O Coração sorteia a
dele à parte, e custa trinta peças se for ouro, ou três.

E então: **pagar a um demônio com a matéria do inferno faz o demônio estourar**. Ele aceita a vara, aceita o
creme, entrega o que prometeu — e **cinquenta batidas depois**, que é o tempo exato de quem fez o negócio se
virar e começar a andar, explode com força três e fogo.

No original o campo que conta essas batidas se chama `tryEscape`: o estouro é a **fuga** dele. **Ele não
morre nele** — abre um buraco no chão e vai embora. Quem fica no buraco é quem pagou.

Dois em cinco das trocas do mundo são cobradas em matéria do inferno. Nada no jogo avisa. É a melhor
armadilha deste mod porque não está escondida em lugar nenhum: está escrita na **moeda que ele pediu**, e
quem souber ler a moeda nunca cai nela.

### O Coração

Um bloco que **bate**. De vinte e cinco em vinte e cinco batidas toca uma batida de coração no lugar onde
está, e quem entrar numa casa com um coração no canto ouve a casa pulsando antes de ver por quê. O som é
tocado **do lado de cá** — um som mandado pelo servidor chegaria a toda a gente ao mesmo tempo e perderia o
que ele tem de bom, que é **vir daquele canto**.

É a **fonte de poder de Altar mais forte** que conta mais do que uma vez: quarenta cada, contando até dois.
Oitenta de poder em dois blocos, quando oitenta blocos de grama dão cento e sessenta e pedem oitenta blocos.
E no **Mundo dos Espíritos** ele torna o pesadelo **demoníaco**, que é a coisa mais perigosa que aquele lugar
tem.

Também é o que acende o **Inferno na Terra** e o que entra no cozimento da **Paralisia**. Ele pede um coração
para dar corações.

No alambique ele dá, com Vapor de Diamante, **quatro Sangues Infernais e um Mal Refinado**; com pedra do
Nether, **areia das almas e dois Sangues Infernais**.

**Declarado:** no original ele entra ainda em três coisas que este porte não tem — o **Ânimo Infernal** no
caldeirão, a **Língua do Diabo** na bancada e a **Estátua da Deusa**. As três ficam esperando as peças delas.

#### E o que o coração destrancou em quem já estava aqui

O **Reflexo** — o demônio que guarda a cela de um espelho — larga **um Coração** quando morre, sempre, e isso
estava faltando desde a fatia dos espelhos porque o coração não existia. Agora existe.

E a **bruxa do coven** volta a ter o pedido dela. A lista de pedidos do original tem **sete**, e aqui eram
**três**, porque as coisas que os outros quatro pediam não existiam. Com o Coração, a **Pedra Necrótica** e o
**Cozimento Grotesco** no mod, entram mais três — fica faltando o que pede uma **Bola de Cristal**.

#### E come-se

Por **dois minutos**: Vida Extra V, Regeneração II, Força III, Rapidez III e Resistência ao Fogo III. É mais
poder do que qualquer outra coisa deste mod dá de uma vez. Com ele vêm **Cegueira** pelos mesmos dois minutos
e **Fome II** por três.

E comê-lo **põe fogo em quem come**, por **dois minutos e doze segundos**.

A Resistência ao Fogo dura **dois minutos**. **O fogo passa dela por doze segundos** — e é nesses doze
segundos, cego, com a proteção acabada e as chamas ainda acesas, que o negócio se cobra. Nenhuma linha do
jogo o diz. É a mesma piada da moeda, contada outra vez: o preço está todo escrito nos números, e os números
estão todos à vista.

A ordem em que os efeitos entram importa e é a do original: os efeitos primeiro, o fogo depois. É por isso
que os primeiros dois minutos não doem.

### Os ritos

**Banir** apaga, de segundo em segundo, tudo o que é **de lá** a nove blocos do círculo — sem dano, sem luta,
sem queda: some. É o botão de desfazer de quem chamou mais do que devia, e por isso é barato: **pó de blaze**
e uma pedra.

**Declarado:** a lista do original tem cinco nomes — o Demônio, a Morte, o Senhor do Tormento, o Imp e o
Reflexo. Entram os **dois que existem aqui**, e a lista já espera os outros três.

**Chamar** tem dois ritos, e a diferença entre eles diz o que o mod pensa de quem joga: o primeiro pede Mal
Refinado, pó de blaze, uma pérola do fim e um **aldeão vivo**; o segundo troca o aldeão por **duas pedras
sintonizadas**, uma delas carregada. O segundo se chama "caro" no código do original — ou seja, as pedras
custam mais do que um aldeão custa a quem não se importa com aldeões.

**O Inferno na Terra** é um rito que **se abre em círculo**, e o que ele faz enquanto cresce é **estragar o
chão**: onde o anel passa, a terra, a grama, o micélio, a terra arada e a areia viram **pedra do Nether** —
uma casa em duas no terço de dentro, uma em quatro na metade, uma em seis no resto. É isso que deixa no fim
uma mancha de inferno densa no centro e esfarrapada nas pontas, em vez de um disco. Pedra não estraga;
madeira não estraga. Ele **come o que é vivo e deixa o que é construído**.

Com a **maestria da maldição**, a grama alta e as flores que o anel atravessa **pegam fogo** em vez de só
sumirem.

E quando o círculo chega ao tamanho dele, o rito **não acaba**: de duas em duas segundas cospe uma criatura
do Nether no meio, **para sempre**, duzentos de poder por vez. **Dois em cem** é um **Demônio**, oito um
ghast, trinta um blaze, vinte um cubo de magma, e o resto zumbis-porcos.

Dois por cento é de propósito. Quem quiser um coração **compra**; quem quiser um demônio **espera**.

Ele só pega **no Mundo de Cima**, **de noite**, e o preço de acendê-lo diz o resto: Sopa de Pedra Vermelha,
um **Coração de Demônio**, uma Pedra de Caminho, uma **Estrela do Nether**, um **aldeão vivo** e cinco mil de
poder — e os **três anéis** de glifos, dezesseis infernais dentro, vinte e oito de outro-lugar no meio e
quarenta infernais fora. É o único rito do mod que pede os três anéis cheios.

**Declarado:** o original tranca o fogo atrás de uma opção de configuração; este porte não tem arquivo de
configuração e deixa o fogo sempre ligado para quem tem a maestria, que é o que a opção faz por omissão. E a
Pedra de Caminho Ligada, que no original entra como oferta opcional, fica para quando ela vier.

### O que se desenhou

Cento e vinte e oito por trinta e dois de textura, e **sete caixas só na cabeça** — cara, dois chifres de um
pixel que sobem oito, dois dentes que descem do lábio de cima, um focinho e um lábio de baixo. O original
lhes dá nome, uma a uma, que era o jeito de então de pôr sete desenhos numa peça só.

Braços de **vinte** de comprido, que lhe chegam abaixo dos joelhos e balançam na mesma curva de triângulo das
pernas, com um **desconto de dois décimos** que os deixa pendurados um dedo à frente do corpo. No golpe, o
**braço direito** sai de dois radianos atrás e desce em dez quadros — o martelo do golem de ferro. O esquerdo
fica onde estava: o original não o toca, e é por isso que ele parece bater com um lado só.

E duas **asas chatas** de catorze por vinte e um, sem grossura nenhuma, presas às costas em ângulos
diferentes uma da outra: uma a trinta e oito graus do corpo, a outra virada ao contrário. Elas nunca se mexem
e ele não voa. Estão ali para dizer o que ele é.

#### E o Coração não é um bloco: é um modelo que bate

Esta parte eu fiz errado primeiro, e a tela mostrou. Eu tinha pegado o `demonHeart.png` do Witchery como
textura de bloco e feito uma caixinha de oito por treze com ela. A textura é de **trinta e dois por trinta e
dois** e não é uma face de bloco nenhuma — é a **chapa de um modelo**, com as peças espalhadas pelo canto
como num bicho. Na tela saiu um recorte vermelho achatado dentro de uma moldura.

No original o Coração é um `BlockContainer`, e o `BlockContainer` do jogo de então devolve tipo de desenho
**−1**: o bloco não se desenha. Quem o põe no mundo é um desenhista de alma com **dez caixas** — quatro de
músculo e **seis de cano**, cinco deles finos e torcidos cada um para o seu lado, como veias cortadas.

E então o que faz o bloco inteiro funcionar: o **músculo incha e desincha**, numa onda de seno, entre 1,11 e
1,20 do tamanho dele, com período de **vinte e cinco batidas**. Os canos ficam parados.

Vinte e cinco batidas é **exatamente** o intervalo do som de coração. O inchaço e a batida são a mesma
batida: ouve-se o coração e, olhando, vê-se o coração fazer o barulho. Sem o inchaço — que foi o que eu tinha
—, o som vinha de um enfeite parado, e o melhor bloco de atmosfera do mod virava um erro de áudio.

O modelo de bloco deste porte não tem, por isso, uma única caixa: só diz de que cor são as lascas quando
alguém o parte.

As duas saem **da mesma esquina da chapa** e nenhuma é espelhada: o original liga o espelho depois de criar
as caixas, e depois de criada a caixa o espelho não faz mais nada. Eu havia espelhado a esquerda, que é o
engano do espelho morto outra vez.

**Guardas:** `OccultaDemonGameTest`, com nove — o teto de quinze, que é o que o torna uma coisa de negociar
em vez de matar; **a matéria do inferno que tem nome**, com o pó de blaze de fora e o creme de magma dentro;
o estouro, que ele não morre nele e que fere quem está ao lado; a lista que tem sempre um coração nos três
primeiros lugares e duas vendas por troca; **o fogo que passa da proteção por doze segundos**, que é a prova
que carrega a fatia; o Coração como a fonte de Altar mais forte que conta duas vezes; o demônio que só fica
se foi chamado; o Banir que só apanha o que é de lá; as cinco fatias do Inferno na Terra; e o chão que ele
estraga, onde o que é vivo vira pedra do Nether e o que é construído fica. E `OccultaDemonClientTest`, com
seis telas: ele de frente, de lado e de trás — porque uma chapa posta no ângulo trocado desaparece quando se
olha de frente para ela —, o Coração no chão, o Coração de perto, onde o músculo inchando se vê, e o Coração
no inventário.

## O bicho de estimação da bruxa, e o olho dele (2026-10-04)

A fatia do coven deixou um pedido pela metade, e a fatia do Demônio — que foi mexer na lista de pedidos para
lhe devolver o do Coração — deu com ele.

### O que estava errado

A bruxa do coven tem dois pedidos de brigar. Aqui eles se fechavam **matando**: ela soltava uma aranha comum,
e quando não havia nenhuma aranha viva num raio de vinte e quatro blocos ela considerava o pedido feito.

No original ela não olha o mundo à procura de um corpo. **Ela olha a mão.**

O bicho que ela solta é o **bicho de estimação dela**: cem de vida, cinco de dano — cinco vezes o que uma
aranha tem —, com o nome dela em cima, já virado para quem aceitou. O zumbi dela leva ainda um **crânio** na
cabeça. E nele vem **pendurada** uma coisa que não é dele: um **olho de aranha**, ou uma **carne podre**, com
o nome dela escrito e a **marca dela** por dentro.

Morto o bicho, essa coisa cai no chão, um bloco acima dele. Levada de volta, ela a reconhece pela marca.

E a marca é tudo. Sem ela, qualquer olho de aranha do bolso fechava o pedido de qualquer bruxa, e aquele bicho
de cem de vida não servia para nada. Com ela, **o pedido é o bicho dela e mais nenhum** — e é por isso que o
original se dá o trabalho de guardar um UUID dentro de um olho de aranha.

### O que isso trouxe de novo

O `WITCExtraDrops` do Witchery: **pendurar uma coisa num bicho** para que ela caia quando ele morrer. É o
avesso do `setNoDrops`, que cala o que o bicho tem no corpo; aqui se lhe acrescenta uma coisa que **não veio
do mundo** e que só existe para ser trazida de volta.

No original é uma lista no NBT do bicho, lida pelo `LivingDeathEvent`. Aqui é um apego com a lista de pilhas,
lido pelo `OccultaEvents` — a mesma forma que o `NoDrops` já tinha.

### E o que o bicho dela não leva

**Fiel ao original:** o bicho **não passa pelo nascimento comum**. Nada de armadura sorteada, nada de ajuste
por dificuldade. Ele é feito à mão, posto no mundo e mandado atacar — e é só isso. Este porte estava chamando
o nascimento comum, o que dava à aranha dela uma chance de nascer com efeitos de dificuldade que o original
nunca lhe deu.

**Guardas:** `OccultaCovenGameTest` ganha a do olho — a coisa dela serve a ela, a da outra bruxa não serve, e
um olho de aranha do bolso não serve a ninguém; o bicho solto tem cem de vida, o nome dela e a coisa
pendurada; e morto, a coisa cai. E a conta dos pedidos sobe de três para seis.

## As duas portas, e a chave que nasce com uma delas (2026-10-04)

A madeira do ofício estava toda portada — tora, folha, muda, tábua, escada e laje das três árvores — menos as
**portas**. E as portas são a parte que interessa, porque uma delas não é uma porta: é uma **fechadura**.

### A Porta de Sorveira

Ela **não abre** para quem não traz a chave dela. E a chave dela é uma só: a que **nasceu com a porta**, no
instante em que alguém a pôs no chão, marcada com aquelas três contas e aquele mundo. Não há como fazer
outra — nenhuma receita faz uma chave.

Quem a quebrar sem a chave fica com **vinte e quatro gravetos**. A porta não volta.

É a única tranca deste mod que não depende de nada vivo: nem de ofício, nem de poder, nem de altar. É
madeira, e a chave está no bolso de alguém. O Witchery não a faz cara — **seis tábuas** — porque o preço dela
não é o da matéria: é ter de **cuidar** de uma chave.

E o cuidar começa logo: ao ser posta, a porta **larga a chave no chão**, aos pés de quem a pôs. Ela cai como
qualquer coisa cai. Quem não a apanhar fica do lado de fora da própria casa.

#### A conta das duas metades

A chave sabe uma casa só — a **de baixo** — e quem toca a porta toca onde quer. Tocando pela cabeça, é a
casa de baixo que se procura. Sem esta conta, a porta abriria pela cintura e não pela cabeça, que é o tipo de
erro que ninguém vê num teste e toda a gente vê no jogo.

E a procura é no **inventário inteiro**, não na mão. Uma chave de casa não se leva na mão, e uma tranca que
obrigasse a isso não seria uma tranca: seria um estorvo.

### O Chaveiro

Duas chaves fazem um chaveiro; um chaveiro mais uma chave faz um chaveiro maior. Cada porta entra **uma vez
só** — pôr duas chaves da mesma porta na bancada não dobra nada.

Ele vale por todas as chaves que tem, e é por isso que existe: quem tem três casas trancadas já não carrega
três chaves, carrega uma argola. E quem perde a argola perde as três.

No original eram **duas receitas** sem forma, escritas à mão, uma para cada caso. Aqui é **uma**, que faz as
duas coisas: na bancada pode estar um chaveiro ou nenhum, e o resto são chaves.

### A Porta de Amieiro

É só uma porta, e é de propósito. Ela existe para que a de sorveira não seja a única porta do ofício e para
que escolher a trancada seja uma **escolha**.

**Fiel ao original:** ela leva a **folha da porta de carvalho**, não uma folha própria. O Witchery faz isso de
propósito — a porta comum do ofício é indistinguível de uma porta comum —, e aqui fica igual.

### O que ficou de fora

O original tem ainda uma **Porta de Gelo**, que é do ramo do gelo perpétuo, e a quirk do amieiro de avisar os
vizinhos ao abrir — que no jogo de hoje já é o que uma porta faz sozinha.

**Guardas:** `OccultaDoorsGameTest`, com seis — a porta que não se mexe de mãos vazias, não se mexe com a
chave de outra porta e abre com a dela **do fundo do inventário**, que é a prova que carrega a fatia; a chave
que vale pelas duas metades; os vinte e quatro gravetos de quem arromba e a porta de volta para quem tinha a
chave; o chaveiro que guarda cada porta uma vez só e vale por todas; e a de amieiro, que abre para quem a
empurrar. E `OccultaDoorsClientTest`, com três telas: as duas na parede fechadas, abertas, e as quatro peças
no inventário.

## O gelo que não derrete (2026-10-04)

O Witchery tem um bloco de gelo com uma diferença só, e a diferença é tudo: **ele não derrete**.

Um bloco de gelo ao sol é um relógio. Este não tem relógio nenhum — e é por não ter que se pode construir com
ele. Por isso ele tem **escada, laje, cerca, portão, placa e porta**, que o gelo do mundo nunca teve, e por
isso a neve ganhou **escada, laje e placa** no mesmo pacote.

Ele é duro onde o gelo é mole — dois de dureza e cinco de resistência, contra meio do gelo comum — e escorrega
igual.

### A esfera

As duas maneiras de o fazer usam a mesma conta: o `BlockActionSphere`, que é o **método de Bresenham em três
dimensões**. Risca-se um círculo e, para cada ponto dele, risca-se outro perpendicular, espelhando os oito
octantes nos três eixos: trinta e dois pontos por volta, sem uma única raiz quadrada. Era como se faziam estas
coisas quando a máquina não dava para mais, e continua sendo mais rápido do que a conta direta.

**Fiel ao original:** o raio **desconta um** antes de começar. Uma esfera de raio oito tem casca de sete. Não
é engano — é a conta a começar de dentro —, mas quem não souber faz uma bolha um bloco menor do que pediu.

E a casca é só a casca: quem a usa risca a casca com gelo e depois **enche** o de dentro, trocando por ar a
água que lá estiver. É isso que faz uma casca de gelo no fundo de um lago ficar com **bolha de ar** dentro em
vez de uma casca à volta de um afogamento.

**Engano do original que fica:** na varredura em X do recheio, ele pergunta pelo bloco em `(realX, x, posZ)` —
o contador do laço no lugar da altura. A parada por casca, nesse sentido, é lida na altura errada, e o recheio
às vezes atravessa a casca de lado. Fica como está: a bolha sai com o mesmo feitio torto que o jogo de 2014
lhe dá.

### As duas maneiras

O **Cozimento da Casca de Gelo** é o único cozimento do mod que **constrói**. Todos os outros fazem alguma
coisa a quem bebe ou a quem apanha o frasco; este deixa um lugar diferente no mapa, e o lugar fica.

Derramado no chão, abre uma bola oca com o raio que a força der — mais um, ou mais dois acima do quarto grau.
Acertando alguém, dá-lhe **Arrepio** por dez segundos e abre a bola à volta dele, que é como ficar emparedado.

E há quem não se emparede: um **demônio**, um **blaze**, um **Ent**, um **golem de ferro** ou um chefe. Em vez
da bola, fora do Nether, fica-lhes uma **água correndo aos pés** — que é o jeito do original de dizer que o
frasco se gastou neles sem pegar.

O **Rito da Expansão Gelada** faz a mesma bola, maior e devagar: cresce de cinco em cinco batidas até **oito**
com duas bruxas, **doze** até cinco, **dezesseis** acima disso. A cada passo par ele risca a casca no raio de
agora e risca **ar** dois raios para dentro, de modo que a bola se abre por fora e se esvazia por dentro ao
mesmo tempo.

É o único rito deste porte que **desiste e devolve** o que se ofereceu: sozinha, ninguém o faz. E o preço diz
o que ele vale — uma espada de diamante, um **Coração Congelado** e uma Pedra Sintonizada Carregada. O que
fica é uma casa.

### O Coração Congelado

Uma **Agulha de Gelo** enfiada num **Coração de Creeper**, com uma lágrima de ghast por baixo. É a chave das
duas coisas acima.

**Declarado:** no original, comê-lo **apaga os efeitos de infusão** de quem o come — é o botão de desfazer
daquele ramo. A infusão não está portada; ele come-se e não faz nada, e quando a infusão vier é aqui que isto
entra.

### A porta de gelo, e uma exceção que o tempo apagou

No original a Porta de Gelo é uma classe à parte por uma razão só: no jogo de 2014, o gelo **não contava como
chão sólido**, e uma porta comum não ficava de pé sobre ele. O `BlockPerpetualIceDoor` existia para abrir essa
exceção.

No jogo de hoje a conta é outra — o que decide é o **feitio** do que está por baixo, e o gelo perpétuo é um
cubo inteiro. Qualquer porta já fica de pé sobre ele. Eu tinha escrito a exceção na mesma; a prova mostrou que
ela não fazia nada, e ela saiu. A prova ficou, a dizer o contrário: as duas portas ficam de pé, e no ar
nenhuma fica.

**Declarado:** o **portão** e as **placas** usam os sons de carvalho e de pedra do jogo, porque o som de
uma porta é hoje uma coisa registrada e o original não tinha nenhuma registrada para gelo. O bloco soa a
vidro, como o gelo.

**Guardas:** `OccultaIceGameTest`, com seis — o gelo sem relógio, que é a prova que carrega a fatia; a esfera
oca e o raio que desconta um; a bola posta no mundo, com casca a dois e meio vazio; as portas sobre o gelo; os
três tamanhos do rito; e o raio do cozimento com quem o aguenta e quem não. E `OccultaIceClientTest`, com três
telas: a família de gelo em fila, a de neve, e as peças no inventário.

## O cozimento que fica preso na maçaneta (2026-10-04)

O ofício tem quatro jeitos de um frasco se espalhar. Três deles arrebentam: de uma vez, em névoa, em poça. O
quarto **não arrebenta**. Ele fica.

### O gatilho

Um frasco com uma **cabeça de zumbi** no caldeirão não se gasta em quem acerta. Acertando um **botão**, uma
**alavanca**, uma **porta** ou uma **placa de pressão**, ele troca a peça por uma **gêmea amaldiçoada** e
espera ali, calado.

Quem mexer nela leva o cozimento inteiro na cara, e a peça **volta a ser o que era**.

Acertando qualquer outra coisa — uma parede, um bicho, o chão —, não faz nada. É o único jeito de espalhar do
mod que pode ser **desperdiçado**, e é de propósito: ele vale por acertar o lugar certo.

### E ela não se vê

É a armadilha mais limpa que este mod tem. Um botão amaldiçoado é **exatamente** um botão: mesmo desenho —
porque o estado de bloco dele aponta para o modelo do botão de pedra do jogo, e não para um modelo nosso —,
mesma queda, mesmo barulho, mesma peça no botão do meio. Não tem item, não tem receita, não aparece no
criativo. Ninguém a põe no mundo: ela **acontece** a uma peça que já estava ali.

A única maneira de saber é ter visto o frasco bater nela.

### A conta

Dois frascos da **mesma receita** no mesmo botão não se trocam: **somam**. A peça fica armada para duas
pessoas seguidas, e a conta desce de um em um até acabar. Um de receita **diferente** troca o que lá estava,
e a conta volta a um.

Gasta a última carga, a peça volta ao normal. É a diferença entre uma armadilha e uma praga: a armadilha
acaba.

### As sete peças

Os dois botões, a alavanca, a porta de carvalho e as três placas — de madeira, de pedra e de **neve**, que é
a do próprio mod, e que só agora existe para poder ser amaldiçoada.

A porta de ferro não entra, e a razão é a do jogo e não a do mod: ela não se abre com a mão.

A **placa** é a pior das quatro famílias. As outras precisam que alguém **decida** mexer; a placa só precisa
que alguém **passe**. E ela dispara quando passa de solta a pisada, e não a cada batida em que há alguém em
cima — senão, quem ficasse parado nela levava o cozimento sem parar.

### A alma

A maldição mora numa alma de bloco com a receita inteira dentro, a conta das cargas e o **nome de quem
atirou** — porque o que o cozimento fizer é feito em nome dele, e quem vem atrás tem de saber de quem foi.

Numa porta, ela mora na **metade de baixo**, como a chave da porta de sorveira: a porta tem duas casas e a
alma é uma só.

**Guardas:** `OccultaCursedBlocksGameTest`, com cinco — o frasco que troca o botão pela gêmea com a mesma
face e o mesmo rumo, que é a prova que carrega a fatia; o frasco que se perde numa pedra; os dois da mesma
receita que somam e o diferente que troca; a última carga que devolve o botão ao que era; e a lista das sete
peças, com a porta de ferro e a pedra de fora. E `OccultaCursedBlocksClientTest`, com duas telas — e é
a única tela deste porte cujo acerto é a **ausência** de diferença: em cima as peças do mundo, em baixo as
gêmeas, e as duas filas iguais.

## As três sarças e o nenúfar que salta (2026-10-04)

O ofício tem três plantas que não se plantam para colher: plantam-se para **estorvar**.

### A Sarça Selvagem, e o machado de ouro

Ela espinha quem passa — um de dano, o do cato — e é **duríssima**: vinte de dureza, a mesma da obsidiana.
Não se atravessa uma sarça com pressa, e é de propósito: o tempo que ela custa a cortar é o tempo em que ela
está espinhando quem a corta.

E então a parte que ninguém descobre sozinho: **cortá-la a espalha**. Ela tenta nascer nas oito casas à
volta, metade das vezes em cada uma, parando na primeira que pegar em dois de cada três casos. Quem a quiser
tirar do caminho a multiplica.

**A não ser com um machado de ouro.** É a única ferramenta no mundo que a corta sem a espalhar, e o original
não o diz em lugar nenhum — nem no livro, nem na dica, nem no nome da ferramenta. Está escrito numa linha de
código e em mais lado nenhum.

É por isso que isto tem de estar escrito numa prova: é a única coisa deste porte que guarda a lembrança de um
detalhe que nenhum jogador descobriria sem ler o mod.

### A Sarça do Fim

A mesma planta com o outro gesto: em vez de espinhar, **manda quem lhe toca para longe**. Até quinhentos
blocos para cada lado, num lugar que ela escolhe e ninguém vê.

A conta é a de uma pérola do fim de quem não sabe mirar: sorteia o lugar, **desce** até achar chão, **sobe**
até caber uma pessoa de pé com dois blocos de ar por cima. Não cabendo em lugar nenhum dentro de sessenta e
quatro blocos de altura, ela desiste e quem passou fica onde estava.

Cortada, ela não se espalha. Só a Selvagem faz isso.

### A Sarça do Vazio

Ela faz metade de cada: atira para longe como a do Fim, e **apaga a magia** à volta dela.

**Nenhum círculo acende a trinta e dois blocos de uma Sarça do Vazio.** Não falha, não aborta, não avisa — o
glifo do meio simplesmente não responde. É a única coisa neste mod que **desliga o ofício**, e a única defesa
possível contra um coven que já sabe o que está fazendo.

Plantá-la à volta de uma casa é dizer: aqui não se faz nada. E ela brilha de leve, para quem a plantou saber
onde o seu próprio silêncio começa.

**Diferença declarada de feitio:** o original guarda uma **lista** das sarças do vazio do mundo, que cada uma
preenche ao carregar e esvazia ao sumir, e pergunta a essa lista. Aqui se olha a bola na hora. A razão é que
a pergunta só se faz quando um círculo acende — uma coisa rara e lenta por natureza — e uma lista que se
mantém sozinha tem de acertar o carregar, o descarregar, o quebrar e o gravar; errando um deles, fica um
silêncio onde não há sarça nenhuma. Olhar na hora não erra.

### O Lírio-Saltador

Um nenúfar que brilha e dá a quem lhe pisa **Rapidez** e **Salto V** por meio segundo.

Um nenúfar sozinho é um degrau. Uma fileira deles é uma **estrada**: quem a percorrer atravessa um pântano
aos saltos, por cima da água, sem nunca tocar nela.

E o efeito só se põe em quem **ainda não o tem** — é assim no original, e faz diferença: quem já traz um
salto de outra coisa não o perde para este, que é mais fraco em tempo. O nenúfar **não atrapalha** quem já
está voando.

### De onde vêm

A do Vazio e o nenúfar têm receita. As outras duas vêm de uma **mutação**: uma **cana** ou um **cato**
cercados de musgo-espanhol, com água nas quatro quinas de baixo, passados ao Mutandis Extremis — a cana vira
Sarça do Fim, o cato vira Sarça Selvagem, e a coluna inteira se transforma de uma vez.

**Declarado:** no original esta mutação pede ainda **quatro Apanha-Ervas** nas diagonais, cada um segurando a
coisa certa — pérolas do fim para a cana, farinha de osso e pó de blaze para o cato —, e quem a faz é a
**Vara Mutante** e não o Mutandis. Nem o Apanha-Erva nem a vara estão portados; o Mutandis Extremis faz aqui o
que a vara fazia lá, como já faz com o Baú de Sanguessugas das Rosas de Sangue. Quando eles vierem, a conta
volta ao que era.

**Guardas:** `OccultaBramblesGameTest`, com sete — a Selvagem que se espalha ao ser cortada e **o machado de
ouro que a corta limpa**, que é a prova que carrega a fatia; a do Fim que nunca se espalha; o espinho e os
quinhentos blocos; o silêncio de trinta e dois da do Vazio, que se apaga quando ela sai; o salto do nenúfar,
no quinto grau e por meio segundo; e as duas mutações, uma por prova, porque montar as
duas na mesma arena fazia o musgo de uma cair em cima da outra. E `OccultaBramblesClientTest`, com quatro
telas: as três sarças em fila, as mesmas de noite — que é onde a do Vazio se vê brilhar —, o nenúfar numa
poça, e as quatro no inventário.

## O Apanha-Erva, e uma conta que estava declarada (2026-10-04)

A fatia das sarças ficou com um buraco declarado: as duas mutações pediam, no original, **quatro
Apanha-Ervas** nas diagonais, cada um com a coisa certa na boca — e o Apanha-Erva não existia.

Agora existe, e a conta é a do original.

### O que ele é

Uma planta que **segura o que lhe dão**. Clicada de mão cheia, tira **uma** coisa da mão e fica com ela à
vista; clicada outra vez, larga o que tinha no chão.

A ordem importa e é a do original: **cheio, ele sempre larga**, mesmo que quem o toque traga outra coisa na
mão. Não há como trocar o que ele segura sem primeiro o esvaziar — e é essa regra que faz dele uma peça de
receita em que se pode confiar, porque quem monta um quadrado de quatro sabe exatamente o que está em cada
um.

Parece um enfeite e não é. Um Apanha-Erva com uma pérola do fim na boca não é uma planta bonita: é meia
receita de uma **Sarça do Fim**.

### O que se desenhou

Ele não se desenha como bloco — o `BlockContainer` da 1.7.10 devolve tipo de desenho −1 — e quem o põe no
mundo é um desenhista de alma com **dez caixas**: quatro **folhas chatas** de oito por oito deitadas no
chão, cada uma inclinada trinta graus para o seu lado; dois pedaços de **caule** tortos em sentidos opostos,
que é o que lhe dá o jeito de planta vergada; e quatro **pétalas** de um pixel abrindo em volta da boca.

As folhas são chapas sem grossura nenhuma. Vistas de cima são uma estrela de quatro pontas; vistas de lado,
quase desaparecem — e é por isso que um Apanha-Erva no chão parece uma moita até alguém lhe pôr alguma coisa
na boca.

E o que ele segura **roda devagar**, a três quartos do tamanho, logo acima da boca. O giro é o que faz a
coisa ser **vista**: um item parado num canto some na paisagem; um item que roda chama o olho, e é por ele
que se lê uma receita de quatro Apanha-Ervas sem precisar de chegar perto.

### De onde ele vem

Um **baú vazio**, com **quatro tufos de grama** à volta e **água por baixo**, passado ao Mutandis Extremis: o
baú some e no lugar dos quatro tufos ficam quatro Apanha-Ervas.

É o `isMutatableChest` do original, inteiro — e é a conta que este documento já esperava desde a fatia da
Rosa de Sangue, onde ficou escrito que "o baú comum vira um Apanha-Erva". Agora vira.

O baú tem de estar **vazio**, como lá: o que estiver dentro não se perde por um descuido.

### E as sarças voltam à conta certa

A mutação da **cana** pede agora, além do musgo e da água, **quatro Apanha-Ervas com pérolas do fim** nas
diagonais. A do **cato** pede **dois com farinha de osso e dois com pó de blaze** — e a ordem não importa,
que é como o original conta.

Fica de pé a declaração que resta: quem faz estas mutações, no original, é a **Vara Mutante**, e aqui é o
Mutandis Extremis. A vara ainda não está portada.

**Guardas:** `OccultaGrassperGameTest`, com três — o pegar uma e o largar sempre, que é a prova que carrega a
fatia; o que ele larga ao ser quebrado; e o baú de grama que o faz, com o baú cheio recusado. E as duas
provas de mutação das sarças passam a montar os quatro Apanha-Ervas, com uma linha a mais que diz que **sem
eles ainda não é**. E `OccultaGrassperClientTest`, com três telas: um vazio e um cheio lado a lado, o
quadrado de quatro de uma mutação, e ele no inventário.

#### Uma lição de onde se larga o que uma alma guarda

As duas provas que caíram primeiro ensinaram duas coisas.

A primeira: **o que uma alma de bloco guarda larga-se no `preRemoveSideEffects` dela**, e não no
`affectNeighborsAfterRemoval` do bloco. Quando o bloco some, a alma já foi, e o que ela tinha ia com ela.
O jogo de hoje dá este aviso à alma **antes** de a tirar, e é o único lugar de onde ainda se vê o que estava
lá dentro.

A segunda: **o musgo-espanhol cai ao primeiro aviso de vizinho novo**, porque ele se pendura e não se aguenta
sozinho. Montar um quadrado de mutação põe o musgo **por último** — e isso não é um truque de prova, é como a
coisa se monta no jogo.

## O Baú de Sanguessugas, e o elo que faltava (2026-10-04)

A fatia da Rosa de Sangue deixou escrito aqui que a corrente dela tinha um elo a menos:

> O baú comum vira um **Apanha-Erva**, e é o **Baú de Sanguessugas** — que também guarda nomes, e também se
> faz assim — que vira as rosas. Nenhum dos dois está portado, e por isso o baú comum faz aqui o que o Baú de
> Sanguessugas fazia lá. Quando eles vierem, a corrente ganha o elo de volta.

Os dois vieram. A corrente está inteira.

### O que ele é

Por fora, um baú com **sacos de sangue** na frente, cuja tampa abre em **quatro quartos** que se afastam uns
dos outros em vez de dobrar numa dobradiça. Por dentro, um baú comum de vinte e sete lugares.

O que ele tem a mais é a **memória**: ele anota o nome de quem o abre — até **três**, os mais recentes, e
ninguém duas vezes — e um **Frasco de Vínculo** usado nele sai com um desses nomes.

É a armadilha mais paciente deste mod. Não fere, não prende, não some: fica ali, parecendo um baú
interessante, e espera que alguém tenha curiosidade.

E ele anota **antes** de abrir. Quem desistir no meio do caminho já deixou o nome — é aí que ela pega.

### E é honesta

Os sacos aparecem **um por nome**, em relevo na frente — por cima do desenho de sacos que a própria folha já
traz. É um relevo de **um pixel**, e vê-se de perto: o original fez a frente do baú parecer cheia de sacos e
depois pôs os de verdade por cima dela.

A ideia é que ele é honesto: o que é preciso para não cair nele está do lado de fora, antes de se tocar em
nada. Quem cai, cai por não ter olhado de perto.

### Duas regras do frasco

Ele **não devolve o nome de quem pergunta**: ninguém se prende a si mesmo abrindo o próprio baú.

E só devolve o nome de quem está **no mundo agora**. Um nome de alguém que saiu fica guardado para quando ele
voltar, em vez de dar um frasco que não prende ninguém — e nesse caso o baú só range, que é o original
dizendo "ainda não".

### De onde ele vem

Um **baú armadilhado** vazio, com **quatro trepadeiras** à volta e **água nas quatro quinas de baixo**,
passado ao Mutandis Extremis.

O original pede o **armadilhado** e não o comum, e a escolha é dele: o que vai nascer dali é uma armadilha, e
ela começa numa armadilha.

### O que se desenhou

Oito peças numa chapa de sessenta e quatro: um corpo de catorze por nove, **quatro quartos de tampa** e os
**três sacos**.

A tampa em quatro quartos é a ideia inteira do bloco. Ela não dobra: os quatro pedaços afastam-se uns dos
outros em **três eixos ao mesmo tempo**, cada um para o seu canto. Um baú comum range; este **floresce**.

A curva é a dos baús do jogo — um menos o cubo do que falta —, de modo que ele abre depressa no princípio e
vai parando no fim, como uma coisa pesada que cede.

**Guardas:** `OccultaLeechChestGameTest`, com cinco — os três nomes e os vinte e sete lugares; o nome anotado
uma vez só, que é a prova que carrega a fatia; o frasco que nunca devolve o nome de quem pergunta; o baú
armadilhado que o faz, com o comum recusado; e a **Rosa de Sangue pedindo o Baú de Sanguessugas**, com o baú
comum agora recusado — que é o elo declarado se fechando. E `OccultaLeechChestClientTest`, com três telas: os
quatro baús com zero, um, dois e três nomes, um de lado onde a tampa se vê, e ele no inventário.

**E uma ordem que importa:** o desenhista vira o modelo de cabeça para baixo **antes** de o girar para o lado
em que foi posto. Girando primeiro, o giro sai espelhado e a frente do baú — onde moram os sacos — fica do
lado de lá. Foi assim que esta fatia descobriu que a ordem do original não era um acaso.

## Os sons do ofício (2026-10-04)

Até aqui, o Ars Occulta **soava a Minecraft**. Um devorador rugia no lugar do lobisomem, um bloco de notas
estalava no lugar do baú, um papagaio piava no lugar da coruja e um sapo do pântano coaxava no lugar do sapo
da bruxa. Estava declarado, e era honesto, mas era um remendo — e o Thaumcraft, que veio primeiro, já tinha os
**cento e dezessete arquivos dele** no `sounds.json` desde o princípio. Só o ofício é que ficou de fora.

Esta fatia fecha isso. São os **sessenta e quatro eventos** e os **noventa arquivos** do jar de 2014, nos
mesmos agrupamentos em que ele os tinha — um evento pode ter mais de um arquivo, e o jogo sorteia qual toca.

### O nome deles

O original os chamava `witchery:mob.wolfman.howl`, `witchery:random.mantrap`. Aqui eles se chamam
`thaumcraft:occulta.mob.wolfman.howl` e `thaumcraft:occulta.random.mantrap`: **o nome que o original lhes
deu**, com `occulta.` na frente para não se confundirem com os do Thaumcraft, que vivem no mesmo
`sounds.json`. Os arquivos ficam em `sounds/occulta/`, na árvore em que vieram.

### O que trocou de som

| Quem | Soava a | Soa a |
| --- | --- | --- |
| **O lobisomem** | devorador | o uivo, a fala, o golpe e a morte dele — e **uma fala em vinte é um uivo** |
| **A estátua do lobo** | devorador | o **Senhor dos Lobos** |
| **A lua apanhando alguém** | rugido | o **uivo** |
| **O lobisomem comendo** | comer genérico | o **mastigar** dele |
| **A Baba Yagá** | bruxa do jogo | a voz dela viva, e a morte dela |
| **A bruxa do coven** | bruxa do jogo | **calada** quando parada, como no original — e **a voz da Baba** quando fala |
| **A coruja e o sapo** | papagaio e sapo | os do ofício |
| **O duende** | aldeão | o dele |
| **O Reflexo** | gente levando dano | a fala, o golpe e a morte dele |
| **O pesadelo** | vex | os três dele |
| **Lilith** | bruxa do jogo | os dela |
| **O giz** | areia caindo | o **giz** |
| **O chifre da caça** | chifre de cabra | o **chifre** |
| **O caldeirão fervendo** | coluna de bolhas | o **blop** |
| **O Coração de Demônio** | coração do Protetor | o **coração** |
| **O vampiro sumindo** | fogo apagando | o **poof** |
| **Beber sangue** | beber genérico | o **gole** |
| **A hipnose** | ilusionista | a **hipnose** |
| **O Espelho chamando a cara** | invocador | a **fala do Reflexo** |
| **Atravessar o Espelho** | chape de água | o **chape** dele |
| **Os caçadores chegando** | corno de ataque | o **"eles vêm"** |

### O que ficou igual, e é de propósito

Nem tudo soava emprestado. O original também usa sons do próprio jogo em muitos lugares, e esses já estavam
certos: o **fizz** de quem volta a ser gente, o **estouro do Wither** na entrada do Caçador Cornudo, o
**toque de caixa** de quando um poder não dá — e o **golpe** da Baba Yagá e da bruxa do coven, que no original
são os da bruxa do Minecraft mesmo.

### E dois que o original trocou

O **pesadelo** toca o arquivo chamado `nightmare_dead` quando leva um golpe, e o chamado `nightmare_hit`
quando morre. Quem escreveu o Witchery trocou os dois, e o jogo de 2014 toca assim desde 2014.

Fica trocado aqui também, com o porquê escrito no javadoc. O porte copia o original — inclusive onde o
original se enganou.

E o `playWitchTalk` da bruxa do coven **recebe um volume e não o usa**: ele sempre manda um. Fica assim.

### Os que ainda não têm onde tocar

Dos sessenta e quatro, **vinte e três** são de coisas que ainda não vieram: o diabrete, o macaco de asas, o
Senhor do Tormento, Leonardo, o Treefyd, a banshee, o poltergeist, o espectro, a Marca Escura, o Gulg, o Mog,
o regatear do duende, a bengala-espada, o arco de mão e a adivinhação do amor.

Eles ficam **registrados e esperando**. Quando a fatia deles vier, o som já está lá — e é o certo, em vez de
ser um empréstimo que depois alguém teria de vir trocar. Foi por isso que esta fatia veio antes delas.

**Guardas:** `OccultaSoundsGameTest`, com três — os **sessenta e quatro** eventos registrados; os que esta
fatia foi buscar, um de cada canto; e a prova que importa, que é **cada arquivo que o `sounds.json` promete
estar no jar**. Um som registrado sem arquivo não dá erro nenhum: ele só não toca, e ninguém descobre até
estar jogando.

## As duas armadilhas, e a mordida que pega (2026-10-04)

Um bloco só com uma chave virada, e as duas coisas que ele faz não têm nada a ver uma com a outra.

### A de ferro

Rasa — pouco mais de um pixel de alto —, **sem colisão nenhuma**, e **invisível para quem não a pôs**. Quem
pisa nela leva **quatro de dano de bigorna** e fica **trinta segundos preso no lugar**, com a paralisia no
terceiro grau.

O dano de bigorna é do original e vale repetir, porque quase se adivinhou como cacto: o
`field_82728_o` é o `anvil`. As tabelas do MCP continuam pagando o trabalho que deu baixá-las.

Ela **nasce disparada**. Quem a põe no chão tem de se abaixar e armá-la com um clique, e outro clique volta a
desarmá-la — que é também o jeito de recarregá-la, porque ela não se recarrega sozinha. Armada, ela leva
**vinte batidas** para ficar sensível: o tempo de quem a armou tirar o pé de cima dela.

E quem está no **criativo** leva o dano e não leva a paralisia. É a única misericórdia que ela tem.

### E ela se esconde

Uma armadilha **armada**, **de ferro** e **posta por alguém** fica a **três décimos de opaca** para todos
menos para quem a pôs. Num chão de pedra isso é quase nada.

Disparada, deixa de se esconder — já não serve de nada. Sem dono, também não: é o caso de quem a põe por
comando. E a de lobo nunca se esconde, que não é para pegar gente.

No original isto vem com uma segunda parte que **fica de fora, declarada**: ele também apaga a **caixa de
seleção** da armadilha escondida, pelo `DrawBlockHighlightEvent`. Aqui ela continua aparecendo quando se olha
para ela de perto. Fica para quando houver uma fatia de ganchos de desenho; o que ela tem hoje é a
transparência, que é a parte que se vê de longe.

### A de lobo, que não é uma armadilha

A **Armadilha de Lobo** — a prateada — não espera que um lobisomem passe. Ela **chama um**.

Posta ao pé de um **Altar do Lobo**, com uma **ovelha na corda** a oito blocos, ela espera a **lua cheia**,
espera **uma volta do relógio** com tudo no lugar — e põe um lobisomem no mato, a dezesseis ou trinta e dois
blocos. E anota **qual**.

Depois ela só aceita **aquele**. Nenhum outro lobisomem a dispara. Quando o certo pisa nela, ela o
**torna contagioso**, com o som do Senhor dos Lobos.

A corda é o que faz a diferença. Uma ovelha solta não serve: ela tem de estar **presa**, porque o original
quer que alguém a tenha levado até lá de propósito. E a ovelha que se solta no meio da espera **desfaz a
espera** — o relógio volta a zero.

Em troca de tudo isso, ela **não volta para a mão**: quebrá-la não devolve nada, porque a prata se gastou no
que ela fez.

Que é dizer: a Armadilha de Lobo não é uma armadilha. É o **fim de uma receita**, e a ovelha é o anzol.

### O que isto corrige

E aqui está o que esta fatia foi mesmo buscar.

Até agora, **todo** lobisomem deste porte passava licantropia a quem mordesse, abaixo de um quarto de vida e
uma vez em quatro. Estava errado. No original, a mordida de um lobisomem **só pega se ele for contagioso**, e
a **única** coisa no mod inteiro que torna um lobisomem contagioso é esta armadilha.

Com a chave no lugar, as contas do original aparecem e são outras:

- um **aldeão** vira abaixo de um quarto de vida — e vira num aldeão que **não é contagioso**, de modo que o
  contágio **para na primeira geração**;
- uma **pessoa** apanha a doença **sem conta de vida nenhuma e sem sorteio**. Uma mordida basta.

A diferença no jogo é grande. Antes, andar num mato de lobisomens era um risco de se apanhar a doença por
azar. Agora **não é risco nenhum** — e quem quer a doença **tem de a preparar**: a ovelha, o altar, a lua, a
Armadilha de Lobo e o pé do bicho em cima dela.

O contágio anda nos dois sentidos pela corrente do aldeão: um aldeão contagioso vira um lobisomem contagioso,
e esse, ao voltar a ser aldeão com a lua, leva a chave consigo. É o `convertToVillager` do original
passando a chave de mão em mão.

### O que se desenhou

Vinte peças numa chapa de trinta e dois. A **base** é uma barra de dez; os dois **discos** são as molas; a
**placa** no meio é o gatilho, e ela afunda meio pixel ao disparar. E os dois **arcos** levam cada um as suas
duas hastes e os seus **cinco dentes**.

Os dentes e as hastes são **filhos dos arcos**, e é por isso que ela funciona com dois números: basta girar o arco
e tudo o que está pregado nele gira junto. Armada, 0; disparada, **1,2 radiano** cada um, em sentidos opostos,
e os dentes se encontram no meio.

**E a mesma ordem do Baú de Sanguessugas:** o virar de cabeça para baixo — que aqui é uma meia volta em Z, e
espelha o X de passagem — vem **antes** do giro para o lado em que ela foi posta.

### E um que o Baú de Sanguessugas escondeu

As quatro armadilhas lado a lado da tela de prova encontraram um erro que estava no porte desde o baú.

O jogo de hoje **não desenha na hora**: ele junta tudo o que lhe mandam e desenha depois, de uma vez. A peça
do modelo é **uma só**, compartilhada por todas as armadilhas do mundo — de modo que mexer no ângulo dela
antes de mandá-la faz com que, na hora de desenhar, **todas saiam com o ângulo da última**.

Na primeira tela, as quatro apareceram **armadas**, inclusive a disparada: o ângulo que valeu foi o da última
submetida. O jeito certo é não tocar na peça — gira-se a **pilha de poses** à volta do eixo dela, e cada
submissão leva o seu próprio giro.

E não se via antes porque **é preciso haver duas no mesmo quadro com poses diferentes**. O Baú de
Sanguessugas tinha o mesmo erro e passou: numa sala de baús todos fechados, a pose do último é a de todos.
Ficou corrigido nos dois.

### E mais dois, no mesmo baú

Olhar para a armadilha fez olhar outra vez para o baú, e ele tinha **outros dois**.

O primeiro: ele estava **virado ao contrário**, meia volta. A tabela de giros dele tinha sido copiada da
armadilha, e as duas **não podem ter a mesma tabela**: a armadilha vira o modelo de cabeça para baixo com um
**giro de meia volta em Z**, que troca o sinal de X e Y; o baú vira com uma **escala negativa em Y e Z**, que
troca o sinal de Y e Z. Os dois modos viram o modelo e deixam-no olhando para lados opostos, e por isso o
original dá a cada um a sua tabela.

Com a tabela errada, os **sacos de sangue ficavam no fundo** — do lado em que ninguém os vê. Era por isso que
a fatia do baú custou tanto a fotografá-los: a tela estava olhando para as costas dele.

O segundo: o **item** era uma folha achatada. O original liga o mesmo desenhista ao bloco e ao item, de modo
que um Baú de Sanguessugas na mochila é o baú de verdade — tampa de quatro quartos e tudo —, fechado e sem
saco nenhum, porque sem alma ele cai no lado de "ninguém o abriu ainda". Agora é.

Na **mão e no inventário** ela aparece **armada e deitada**, sem giro nenhum. O original liga o mesmo
desenhista ao bloco e ao item, e sem alma o modelo cai no lado de "ainda não disparou" — de modo que o item
mostra a armadilha como ela é quando serve para alguma coisa.

### De onde elas vêm

A de ferro: três ferros, duas **tesouras** e uma **placa de pressão pesada** no meio.

A de lobo: a de ferro no meio, quatro **pós de prata** nas quinas, dois **catalisadores nulos** acima e
abaixo, e dois **acônitos** aos lados. O original tem uma segunda receita que troca o pó de prata por um
lingote de prata quando algum mod oferecer um; aqui não há lingote de prata, e **fica de fora, declarado**.

**Guardas:** `OccultaBeartrapGameTest`, com sete — os números; ela nascendo disparada e o clique que a arma;
as vinte batidas antes de morder; ela apanhando quem pisa, com dano e paralisia; a de lobo ignorando uma
ovelha; a de lobo recusando um lobisomem que não é o dela; e a prova que carrega a fatia, que é a **mordida
que não pega** num lobisomem comum. E `OccultaBeartrapClientTest`, com três telas — as quatro lado a lado
(disparada, armada, escondida e a de lobo), os dentes de perto, e as duas no inventário.

## O Apanha-Bicho, e os dois familiares que saem dele (2026-10-04)

Uma planta que **engole o que é pequeno**. Um morcego, uma lepisma, uma bolha de gosma ou de magma do
**menor tamanho** — e só do menor — desaparecem nela, e ela passa a mostrar o que apanhou.

A gosma grande é o detalhe que diz o que ela é. Uma bolha de tamanho dois passa por cima dela e nada lhe
acontece: o Apanha-Bicho não é uma armadilha, é um **passarinheiro**. O que ele apanha é o que não machuca
ninguém.

### Ela se ouve

De vez em quando ela faz o **barulho do bicho lá dentro**, uma vez em vinte e quatro batidas de desenho. Um
Apanha-Bicho cheio não se vê de longe: **ouve-se**. É o detalhe que faz dela o que ela é — não é um enfeite
com uma cor diferente, é uma planta com um morcego vivo dentro reclamando.

### Soltar e levar

**Agacha-se e clica** para soltar o bicho. Sem agachar não acontece nada, e o original pede isso de
propósito: ninguém esvazia um Apanha-Bicho por acidente ao passar a mão por ele.

O morcego sai **por cima**, que é o único jeito de ele não ficar entalado. Os outros saem **ao lado de quem
abriu**, do lado para onde ele está — uma lepisma que saísse debaixo dos pés de quem a soltou seria uma
crueldade mesmo para o original.

E ela **cai com o bicho dentro**: quebrá-la devolve o Apanha-Bicho ainda cheio, e pô-lo noutro lugar põe o
bicho com ele. É assim que se leva um morcego para longe.

### Cinco nomes num item só

No original isto eram **cinco itens** — um por bicho, cada um com o seu nome. Aqui é **um item só**, e o que
ele apanhou viaja no feitio do bloco, que o jogo de hoje já sabe guardar num item e copiar na queda.

O **nome** e a **cara**, porém, são os do original: o item lê o que leva dentro, se chama em conformidade e
**muda de desenho** — são as mesmas cinco folhas do bloco. Quem tem um na mochila vê, sem o pôr no chão, o
que há nele: a boca aberta se estiver vazio, a boca fechada em volta de um morcego se não estiver.

**Uma linha a mais, declarada:** o jogo de hoje escreve o feitio guardado na dica do item por conta própria,
de modo que um Apanha-Bicho com morcego diz "caught: bat" debaixo do nome. O original não tinha essa linha
porque não tinha o mecanismo.

### De onde ele vem

Uma **teia**, com **quatro mudas de amieiro** à volta, **água por baixo** — e um **zumbi** ao lado, que é o
que se gasta.

As mudas têm de ser de **amieiro**, e a escolha é do original: o amieiro é a árvore que ele associa ao que
prende e ao que guarda, e é a mesma madeira das portas que só a bruxa abre.

### E as duas que saem dele

A **Coruja**: a mesma teia, com **dois Apanha-Bichos de morcego** ao lado, água por baixo, **três
Apanha-Ervas com Mutandis Extremis** e **um com a Pedra Sintonizada carregada** nas diagonais — e um
**lobo**, que é o que se gasta.

O **Sapo**: a mesma coisa, com **gosma** no lugar do morcego e um **jaguatirica** no lugar do lobo.

É a receita mais longa do ramo das plantas, e vale olhar para ela inteira: um morcego apanhado numa planta,
um lobo ao lado, e a planta trocando um pelo outro. **Cada** Apanha-Bicho do feitio certo vira um bicho, de
modo que quem puser os quatro leva quatro — e os quatro Apanha-Ervas ficam de boca vazia, porque foi o que
seguravam que se gastou.

E isto fecha uma corrente que estava partida pelo meio. A coruja e o sapo são os **familiares** do ofício, e
até aqui eles existiam no mod sem ter de onde vir: quem os queria tinha de os pedir por comando. Agora eles
vêm de onde vinham.

O bicho que a mutação gasta **não morre**: ele **desaparece**, com os pós de gosma e o som da morte dele por
cima. A diferença importa — nada cai dele, e nada o conta como morto.

### Duas coisas que ficam de fora, declaradas

A primeira é o **Piolho Parasítico**, que sai de um Apanha-Bicho **com lepisma** e pede mais quatro
Apanha-Ervas com coisas que ainda não existem — a Língua de Cão entre elas. O piolho não está portado.

A segunda é o **bit do morcego de loja**: no original, um morcego que tenha sido feito mercador pelo **Encanto
da Polinésia** carrega essa marca para dentro do Apanha-Bicho e a leva de volta ao sair. O encanto não está
portado, de modo que nenhum morcego tem a marca, e a marca não se guarda. Quando ele vier, é uma chave a mais
no feitio.

E uma terceira, menor: o original sorteia até **vinte bolhas** à procura de uma do menor tamanho e, não
achando nenhuma, **larga uma bola de gosma no chão**. O jogo de hoje deixa pôr o tamanho da bolha à mão, de
modo que as vinte tentativas nunca falham e a bola de gosma é um caminho que já não se percorre. O código dele
está escrito; é só nunca chamado.

**Guardas:** `OccultaCritterSnareGameTest`, com cinco — os números; ela engolindo o morcego; a prova que
carrega a fatia, que é a **gosma grande não caber e a pequena caber**; a teia de amieiro com o zumbi, e sem o
zumbi; e a Coruja pedindo tudo o que ela pede, com o lobo e a pedra sintonizada a serem tirados um por um. E
`OccultaCritterSnareClientTest`, com três telas — os cinco lado a lado, um de perto, e o item com o morcego
no inventário.

## A Mina de Planta, e os quatro efeitos do projétil (2026-10-04)

Uma flor que **não é uma flor**. Ela parece uma papoula, um dente-de-leão ou um arbusto seco — as três
plantas mais inofensivas que o jogo tem — e quem passa por cima dela leva o que o projétil de bruxa leva.

São **doze**, que é o que dá cruzar as três caras com os quatro efeitos. E a cara **não diz nada** sobre o
efeito: uma papoula de teias e uma papoula de espinhos são a mesma papoula, pelo mesmo desenho, porque o
original faz o ícone depender só de `(meta >>> 2) & 3`. Quem a planta sabe o que ela é; quem passa, não.

Ela é **duríssima de explodir** — mil de resistência, mais do que a obsidiana —, de modo que não se abre
caminho num campo de minas com TNT. E **nada cai dela**: uma mina desarmada é uma mina gasta.

### Os quatro efeitos, que não são só dela

Os efeitos vivem num lugar à parte, `WitchProjectile`, porque no original eles vivem no
`EntityWitchProjectile` e são usados por **mais de uma coisa**: as minas, os cozimentos atirados e os
frascos. Portá-los aqui destrava os quatro cozimentos quando a fatia deles vier.

- a **teia** enche a cruz à volta de onde bateu — onze blocos, quinze reforçada;
- a **tinta** cega num raio de quatro blocos, e **quanto mais perto, mais tempo**: vinte segundos no meio,
  nada na borda. Todo bicho que estava perseguindo alguém **perde o alvo**, que é o que ela faz de melhor;
- os **espinhos** plantam um cato, e o chão **vira areia** debaixo dele — é por isso que o cato do ofício
  deixa uma mancha de deserto por onde passou. Acertando num cato que já existe, ele **sobe ao topo da
  coluna** e cresce dali, de modo que acertar duas vezes no mesmo lugar faz uma coluna mais alta e não duas;
- e o **brotar** faz nascer um galho **para fora da face em que bateu**, largando folhas pelo caminho uma vez
  em quatro. Crescendo **para cima**, ele **levanta para o topo** tudo o que estiver vivo até dois blocos
  acima — que é como o original manda alguém para o céu numa árvore sem lhe tocar.

Todos passam pelo mesmo crivo: **só põem bloco onde não há bloco sólido**. A única exceção é a teia, que
passa por cima de uma **camada de neve** — uma exceção escrita de propósito no original, que não se adivinha.

E a madeira do galho é a **do lugar**: batendo num tronco, o galho é daquele tronco; batendo noutra coisa,
sorteia entre a do jogo e a do ofício.

### Dois que ficam de fora, declarados

O primeiro: o original guarda também o **feitio** da madeira em que bateu, de modo que bater numas **tábuas**
de bétula dá um galho de bétula. Aqui só o tronco leva o feitio consigo; tábuas e folhas dão carvalho ou
sorveira, conforme a família. O caminho curto custava uma tabela de doze entradas que o original tinha de
graça no número.

O segundo: o `BlockProtect.canBreak`, que é o gancho de proteção de território do original. Não há equivalente
portado.

### E um rótulo escrito à mão

O cato precisa saber **em que chão ele pega**, e o original pergunta pelo **material** do bloco — ele
aceita nove deles. Os materiais sumiram do jogo.

A tradução óbvia seria pelos rótulos de hoje, e ela **não funciona**: o `#minecraft:dirt` da 26.2 são **três
blocos** — terra, terra grossa e terra enraizada — e **não inclui a grama**. Custou uma volta de suíte
descobrir isso, com a prova a dizer que o chão não virava areia e o rol a ter trinta e seis entradas sem a
que importava.

Por isso o rol vai escrito à mão em `tags/block/cactus_ground.json`, com os blocos que tinham aqueles nove
materiais em 2014 — e quem jogar pode mexer nele, que é a vantagem de ser rótulo e não lista no código.

**Guardas:** `OccultaPlantMineGameTest`, com seis — os números, incluindo **a grama estar no rol**, que é a
linha que parece boba e não é; a prova que carrega a fatia, que é a **cara não dizer o efeito**; e os quatro
efeitos, um por prova, com a de brotos a verificar que a ovelha **subiu com o galho**. E
`OccultaPlantMineClientTest`, com duas telas: as doze em fileiras de quatro — onde se vê que são três
desenhos e não doze — e três delas no inventário, com nomes diferentes e caras iguais aos pares.

**E uma lição de arena:** a mina de espinhos troca o chão por **areia**, e areia sem nada por baixo **cai**.
O chão da prova passou a ter **duas camadas**; com uma só, ela falharia dizendo que o chão não virou areia
quando ele virou e foi-se embora.

E, de caminho, a prova dos **caçadores de bruxas** deixou de passar por sorte. Eles nascem num anel de três a
oito blocos, e quem os põe **desce até achar chão**: num chão de dez por dez, metade do anel caía fora dele e
o caçador ia parar no fundo do mundo, vivo e longe da vista. O chão da arena passou a cobrir o anel inteiro.

## A Paliçada, e as estacas que apontam (2026-10-04)

Uma cerca de **estacas apontadas** que **fere quem encosta**: três de dano de cato, que é dano que a armadura
não para. Não é uma cerca que se pula; é uma cerca que se **contorna**.

Vinte e cinco de dureza, mais do que a obsidiana. Quem puser uma paliçada à volta de alguma coisa pode ir
dormir. E nada que ande no chão tenta atravessá-la, porque o original devolve que por ali não se anda.

### As estacas

A ponta é o que a torna o que ela é, e ela é feita de **cinco caixas**: o corpo até **meio bloco** de alto e,
por cima dele, **quatro degraus** que vão estreitando — quatro centésimos de bloco por lado e oito centésimos
e meio de alto cada um — com a **textura do topo** do tronco em todas as faces. É a conta do original, e dela
sai uma estaca que termina em bico.

Havendo **outra paliçada por cima**, a estaca deixa de apontar e vai a direito até o teto. Duas empilhadas
são um muro sem frestas, e é o que separa uma cerca de uma parede.

E as juntas: sem vizinhos, **uma** estaca no meio; com vizinhos de um lado, **duas**; com vizinhos nos dois
eixos, **quatro** — e nessas as duas primeiras começam a apontar **mais acima** que as outras duas, de modo
que a cruz fica com as pontas desencontradas. É um detalhe gratuito do original e está portado.

### Nove madeiras e um gelo, que é o que importa

São **nove madeiras** — as seis do jogo e as três do ofício — e um **gelo**.

E aqui está a conta que carrega a fatia: no original as nove madeiras são **um bloco só** com nove números, e
o `canConnectFenceTo` pergunta se o bloco do lado é **este mesmo bloco**. Por isso um carvalho e uma sorveira
dão as mãos. A de gelo é **outro bloco**, e por isso uma paliçada de gelo encostada numa de madeira fica de
pé sozinha ao lado dela.

Esse detalhe **perde-se** ao portar cada madeira como um bloco seu, que é o jeito moderno de fazer madeiras —
e seria preciso um rótulo para o recuperar, e dois rótulos para manter o gelo de fora. Por isso aqui elas
continuam sendo **um bloco com nove chaves**, como lá.

### O que se desenhou

Oito feitios de geometria — as quatro juntas, cada uma apontada e a direito —, com as texturas vindo de fora.
Dez filhos por feitio, um por madeira e um para o gelo: **oitenta modelos**, gerados.

E o feitio do bloco tem **duzentas e oitenta e oito** entradas, que é cruzar as nove madeiras com as trinta e
duas combinações de quatro lados e um de cima. Mais trinta e duas para o gelo. Também gerados — à mão seria
trabalho de copista.

### De onde ela vem

**Oito troncos** à volta de uma **Exalação do Cornudo**, e saem **nove** paliçadas da madeira dos troncos.
Nove receitas, uma por madeira.

**Guardas:** `OccultaStockadeGameTest`, com quatro — os números; a prova que carrega a fatia, que é as **nove
madeiras darem as mãos e o gelo não**; ela ferindo quem encosta; e a empilhada que vira parede, com a de
baixo deixando de apontar e a de cima continuando. E `OccultaStockadeClientTest`, com três telas: a fileira
das dez, onde se vê a casca de cada uma e o gelo sozinho no fim; as pontas — uma solta, duas empilhadas e uma
cruz; e as três no inventário.

**E uma prova que passava por sorte:** a do **estouro do Demônio** perguntava se a ovelha ao lado dele
**morria**. O estouro reparte o dano por raios sorteados, e a mesma ovelha no mesmo lugar ora cai ora fica
com um fio de vida — mudar de arena basta para virar a moeda. Ela passou a perguntar se a ovelha **levou**,
que é o que o estouro tem de provar.

## O vidro que se fecha, a lã que se tinge e a luz que não se apanha (2026-10-05)

Três blocos pequenos que não têm nada a ver uns com os outros, menos uma coisa: os três servem ao
**vampiro**.

### O Vidro Sombreado

Um vidro tingido que **se fecha com redstone**. Sem corrente, ele deixa passar a luz como qualquer vidro;
com corrente, ele escurece e **a luz para ali**.

É uma persiana, e serve ao ofício por uma razão só: um vampiro queima ao sol, e uma casa de vidro sombreado é
uma casa com janelas que se fecham **de dentro**. Quem o inventou pensou nisso.

São as **dezesseis cores**, cada uma com as suas duas folhas — a aberta e a fechada. A fechada é mais escura
e deixa ver menos, que é o que se espera de uma persiana corrida.

**Uma mudança declarada:** no original isto são **dois blocos**, `shadedglass` e `shadedglass_active`, porque
o jogo de 2014 não deixava a opacidade à luz mudar de um feitio para outro do mesmo bloco. Hoje deixa — o
`getLightDampening` recebe o feitio —, e por isso aqui é **um bloco com uma chave**. O que se vê e o que a luz
faz são os mesmos; o que mudou foi o número de nomes no registro.

### A Lã Ensanguentada

Um **vampiro do quarto grau** com uma **Agulha de Osso** fura a si mesmo sobre um bloco de **lã branca** e
tinge-a com o próprio sangue: **cento e vinte e cinco** de poder, que é o mesmo que um primeiro gole lhe dá.

Só a **branca** serve. O original pergunta pelo número zero da lã, e faz sentido: é a única que ainda tem
lugar para outra cor.

E a lã tinta, **no forno**, vira **Pano Escuro** — o tecido de que se fazem as roupas do ofício. É um caminho
curioso e vale dizê-lo inteiro: para ter uma capa de bruxa é preciso um vampiro, uma ovelha branca e um forno.

O Pano Escuro entra aqui porque a lã precisa dele para ter para onde ir. As roupas que o gastam ainda não
vieram.

### O Globo de Luz

Uma bolinha de luz no ar, de dois pixels, que ilumina como uma tocha e meia e larga uma chama **duas vezes em
três**. Não estorva a passagem, não cai de nada e **não se apanha** — nem com o clique do meio.

Ela não é um bloco que se põe: é o que o **símbolo da luz** deixa onde foi lançado. Por isso **não tem item**
e não está na aba do criativo, exatamente como no original. Fica aqui à espera dos símbolos, que são a fatia
grande que ainda falta.

E é a coisa mais barata de quebrar do mod inteiro: dureza zero.

**Guardas:** `OccultaShadedGlassGameTest`, com cinco — os números; a prova que carrega a fatia, que é o
**vidro comer a luz toda fechado e nenhuma aberto**; a redstone que o fecha e o abre; as dezesseis cores com
o item levando a sua; e a agulha tingindo a lã **só na mão de um vampiro** com sangue que chegue. E
`OccultaShadedGlassClientTest`, com três telas: as dezesseis em duas fileiras, aberta por cima e fechada por
baixo, para se ver a diferença lado a lado; a lã ao lado de uma lã branca e o globo aceso de noite; e as
quatro coisas no inventário.

## O Homem de Vime, e o segundo caminho para o Caçador (2026-10-05)

O **Feixe de Vime** é, por fora, um fardo de galhos amarrados — um tronco com casca de vime, que se deita em
qualquer dos três eixos como um tronco se deita. Por dentro, é a peça de que se constrói uma coisa só.

### A figura

O **Homem de Vime**: oito blocos de altura, duas colunas de largura, com os **braços abertos** no meio. Acesa
com um isqueiro, ela **arde** — doze fogos, no peito, na barriga, nas pernas e nas duas pontas dos braços — e
de dentro dela sai o **Caçador Cornudo**, de pé, com a entrada que estoura.

É a única coisa deste ramo que se **constrói** em vez de se pôr. E é o segundo caminho para o Caçador: o
**Chifre da Caça** chama-o de qualquer lugar, mas o Homem de Vime pede que alguém o levante, o encha de
sangue e lhe ponha fogo. O primeiro é um pedido; o segundo é um sacrifício.

### E o sangue é que faz

São **dois feixes**: o **simples**, que são nove mudas atadas, e o **ensanguentado**, que é o simples passado
por **Sangue Infernal** — cinco de cada vez.

E só o ensanguentado serve. A mesma figura, bloco por bloco, feita de feixes simples **não acende**: o molde
do original pergunta pelo número um do feixe em cada um dos dezenove lugares. O que faz o Homem de Vime é o
sangue, não a forma — a de feixes simples é madeira empilhada com jeito.

Mas o **caminho até os pés** usa qualquer feixe. O original desce e anda para trás com
`block == WICKER_BUNDLE`, sem olhar o número, e só depois compara com o molde. É uma distinção fina e está
portada: quem puser um feixe simples debaixo da figura não a estraga, só muda onde o molde começa a ler.

### O molde

Dezenove lugares cheios e **dezesseis vazios** — e os vazios são o que lhe dá contorno. Sem eles, uma parede
de feixes acenderia.

E falta um: o **(+1, +1)**, o ombro direito, que o molde do original **não pergunta**. Ele checa ali o bloco
de baixo outra vez, por descuido de quem o escreveu, e o jogo de 2014 aceita desde então uma figura com
aquele lugar cheio ou vazio. Fica assim.

### Como ele se procura

A conta é curiosa e vale copiá-la inteira: a partir do feixe em que se bateu, o original descobre **em que
eixo** a figura foi construída olhando os quatro vizinhos — e desiste se houver vizinhos nos dois eixos ou em
nenhum. Depois **desce** até os pés e **anda para trás** até à coluna mais baixa do eixo. Só então compara.

Quer dizer que o isqueiro acende a figura a partir de **qualquer** feixe dela, e não só do pé.

### E dois fogos não ficam

Postos os doze, ficam **dez**. Dois deles — o do peito e o do ombro — ficam **cercados de fogo** depois de
todos postos: sem chão por baixo e sem vime ao lado que ainda seja vime, o jogo apaga-os no mesmo instante.

O original perde-os pela mesma razão — ele também põe os doze com aviso aos vizinhos — e não faz diferença
nenhuma: o vime arde a **vinte de espalhar**, que é mais do que qualquer tronco, e os outros dez voltam a
acendê-los antes de a figura cair. A prova pede **dez ou mais**, e não doze, por isso.

**Guardas:** `OccultaWickerManGameTest`, com cinco — os números; a figura ensanguentada sendo reconhecida; a
prova que carrega a fatia, que é a **mesma figura em feixes simples não ser reconhecida**; a acesa com os
doze fogos e o Caçador de pé; e o feixe sozinho, que não é figura nenhuma. E `OccultaWickerManClientTest`,
com três telas: o Homem inteiro visto do chão, os dois feixes lado a lado — um deitado, para se ver a tampa —
e os dois no inventário.

## O Candelabro e o Cálice, e o altar que enfim os conta (2026-10-05)

Dois enfeites, e o fim de uma lista de espera. O miolo do Altar da Bruxa já dizia, em letra miúda, que
**ficavam de fora, por agora, o candelabro e o cálice** — e esta fatia tira-lhes o «por agora».

### O que eles valem

O altar olha o que está posto **em cima de cada uma das suas seis pedras** e conta:

| posto em cima | o que soma |
| --- | --- |
| tocha | **um** à velocidade |
| **candelabro** | **dois** à velocidade |
| cálice vazio | **um** ao teto |
| cálice cheio | **dois** ao teto |

E de cada coisa conta-se **uma só**. O candelabro e a tocha disputam o **mesmo lugar** — quem já tem
candelabro não ganha nada por pôr uma tocha ao lado —, e o segundo cálice não vale nada.

Repare na conta: um altar com candelabro e cálice cheio recarrega **três vezes** mais depressa e tem o
**triplo** do teto. É mais do que a caveira de gente dá, e custa muito menos.

### O candelabro

Cinco velas num pé de ferro, quatro em volta e uma no meio — e a do meio é **cinco mais comprida** que as
outras, com o pratinho dela **dois mais abaixo**, o que a faz sobressair pelo dobro. Dá **luz cheia** e
**arde sempre**: não há feitio aceso e apagado, há o candelabro.

As chamas são **pós**, e não peças do modelo: cinco chamas com cinco fumos, e cada par só aparece em **três
de cada quatro** batidas. É esse sorteio que dá ao fogo dele o piscar irregular de vela de verdade.

### O cálice

Uma taça de ouro feita de **caixas chatas** — quatro paredes de espessura zero e um fundo de espessura zero.
É como se fazia um copo oco antes de haver jeito melhor: uma caixa sem espessura desenha as duas faces no
mesmo lugar, uma virada para cada lado, e o resultado é uma parede que se vê por dentro e por fora. O
**líquido** é outra chapa chata, solta da taça, que só se desenha quando ela está cheia.

E ele põe-se **cheio ou vazio** e fica como o puseram. Não se enche no lugar: o que o enche é a **Sopa de
Redstone**, na bancada, antes de ele descer ao altar — que é a única razão de a Sopa de Redstone existir.

### O cheio, e onde ele passou a morar

No original são **dois itens** e **um bloco com dois números**, e a alma do bloco guarda a mesma coisa que o
número, com os dois a serem postos de acordo um com o outro a cada mudança — três lugares para um booleano.

Aqui são os mesmos **dois itens**, mas o cheio vive **só no feitio do bloco**, que é onde o jogo de hoje
guarda esse tipo de coisa e já o manda pela rede sozinho. As duas almas — a do cálice e a do candelabro —
ficaram **vazias**: existem só porque um bloco que se desenha por fora precisa de uma alma a que o
desenhista se pendure, que é exatamente o que elas são no original (a do candelabro, lá, não tem uma única
linha que não seja um `canFalse`).

### As caixas de textura negativas

As do cálice são **negativas** no original: `(0, -5)`, `(-5, 4)`, `(-4, 18)`. Não é engano. A folha repete-se
nas duas direções, e ler cinco acima do topo de uma folha de trinta e dois é o mesmo que ler na linha vinte e
sete. **Ficam como estão** — mudá-las para o número de dentro daria o mesmo desenho e deixaria de se parecer
com o original.

### O chão de que precisam

O original pede que o bloco de baixo **tranque o passo e tape a luz**. Aqui pede-se que a **face de cima
dele seja firme**, que é o que o jogo de hoje pergunta às tochas e dá o mesmo resultado em todos os blocos
que existiam em 2014.

**Guardas:** `OccultaChaliceGameTest`, com quatro — a luz cheia do candelabro; a queda dele sem chão; os dois
itens pondo o mesmo bloco de dois jeitos e devolvendo cada um o seu; e a queda do cálice. E, no
`OccultaAltarGameTest`, a prova que carrega a fatia: **o candelabro somando dois, a tocha ao lado não somando
nada, o cálice vazio somando um e o cheio dois, e o segundo cálice não somando nada**. E
`OccultaChaliceClientTest`, com quatro telas: o candelabro à meia-noite, para se ver a luz; os dois cálices
lado a lado; o altar posto como se usa; e os três itens no inventário.

## A Bola de Cristal, e as dezessete profecias (2026-10-05)

A ideia mais bonita do Witchery, e a que mais custa a explicar a quem nunca a viu: **a profecia não prevê o
futuro, ela o fabrica**.

### Como funciona

Uma esfera de vidro num pé de três degraus. Batida, ela procura **outro jogador** num retângulo de cinco por
dois por cinco à volta de si e lê a sorte **dele** — só se não houver mais ninguém ali é que lê a de quem
bateu.

Quer dizer que a Bola de Cristal é feita **para duas pessoas**. Quem a tem em casa não lê o próprio futuro;
lê o dos outros, e são os outros que carregam a profecia. É a única coisa deste mod que só serve em
companhia.

Custa **quinhentos** de poder de um altar a dezesseis blocos, por leitura, e tem **cem batidas** de recarga.
E para ler a sorte de outro é preciso ser **vidente** — o que só se consegue fazendo o **Rito da Infusão do
Futuro**, que é o mesmo que faz aparecer a bola. É o único rito do mod que muda alguma coisa **em quem o
faz** e não no mundo: a bola é um objeto e qualquer um a pode roubar; o que não se rouba é saber usá-la.

### E então ela persegue você

Dita a profecia, o mod guarda-a e passa a vigiar o jogador por três portas: o **golpe levado**, a **batida** e
o **bloco partido**. Enquanto ela está **em dia** — oito minutos —, só se cumpre se o mundo a cumprir.
Passado o prazo, ela fica **atrasada**, e a cada batida há cinco por cento de chance de o mod a **forçar**:

- a da **queda** põe cascalho nos nove blocos debaixo dele e **esvazia seis** por baixo deles;
- a da **água** faz o mesmo, com três de fundo e água dentro — e repare na piada: a profecia que promete o
  menor dos incômodos é a que faz o maior estrago na casa de quem a leva;
- a da **briga** faz nascer o bicho a dois ou quatro blocos dele, já olhando para ele;
- a do **Nether** manda-lhe um recado e um blaze.

Passada **meia hora** ela fica **muito velha**, e algumas afrouxam o que pedem: a do diamante deixa de pedir
o minério e passa a aceitar **pedra qualquer**. Quer dizer que quanto mais alguém demora a achar um diamante,
mais perto está de lhe cair um na mão — a profecia dizendo «eu avisei».

E há uma que **nunca se força**: a do **tesouro enterrado**, a quem o original desliga as duas portas do
cumprimento por si próprio. Com razão: um baú que aparecesse debaixo de quem estivesse parado não seria um
tesouro enterrado, seria um baú aparecendo. Essa só acontece a quem cava.

### As dezessete

| id | peso | o que diz |
| --- | --- | --- |
| 1 | 13 | vai topar com um **zumbi** |
| 2 | 13 | vai levar uma **flecha** |
| 3 | 3 | vai encontrar um **Ent** |
| 4 | 13 | vai **cair** |
| 5 | 8 | vai se afogar em **ferro** (8 a 20 de uma vez) |
| 6 | 3 | vai achar um **diamante** |
| 7 | 3 | vai achar uma **esmeralda** |
| 8 | 2 | vai achar um **tesouro enterrado** |
| 9 | 2 | vai **se apaixonar** |
| 10 | 2 | irritou a **Baba Yaga** |
| 11 | 2 | **agradou** à Baba Yaga |
| 12 | 3 | vai fazer um **amigo** (um lobo dele) |
| 13 | 13 | vai ser **salvo por uma coruja** |
| 14 | 13 | vai ser **salvo por um lobo** |
| 15 | 13 | vai **se molhar** |
| 16 | 3 | vai ao **Nether** |
| 17 | 13 | vai juntar **carvão** (10 a 20) |

**Uma de cada vez.** Quem já tem uma por cumprir não ganha outra: bater na bola outra vez **repete o recado**
da que ele já tem. É de propósito — a profecia é para se viver, não para se colecionar.

A do **Nether** só se diz a quem **já esteve lá**. Faz sentido: dizer a um principiante que ele vai ao Nether
não é ler a sorte, é dar-lhe uma missão.

### O salvamento, que é a melhor delas

A do salvamento é a única que se cumpre **no pior momento possível**: o gancho dela não é o de a profecia
vencer, é o de **alguém bater em quem foi avisado**. No instante em que ele leva um golpe, nasce ao lado dele
uma coruja — ou um lobo — que vai direto a quem o atacou.

A coruja é **passageira**: vive trezentas batidas, ou menos se aquilo de que ela o salvou morrer primeiro, e
depois estoura num pó e vai-se. E não larga nada — no original porque ela sabe que é temporária, aqui porque
leva a marca do `NoDrops`, que dá no mesmo e já existia.

### O amor, que é a mais descarada

A profecia do amor faz nascer um **aldeão** a quatro ou seis blocos do jogador, com uma tarefa própria —
cortejá-lo — enfiada no topo da lista. Ele anda atrás dele, solta corações e, quando chega perto, **faz um
filho**. O jogador não é consultado, não há cortejo nenhum, e o que sai dali é um bebê aldeão. É uma piada de
2014 e está portada como estava.

**O que mudou:** o aldeão de hoje pensa por **cérebro** e não por tarefas, e o cérebro dele quer levá-lo para
a cama e para o posto de trabalho. A tarefa entra na lista de tarefas, que ainda roda, e ganha sempre que o
cérebro não tiver para onde ir — de modo que o aldeão apaixonado às vezes para no meio do caminho para ir
dormir. O original não tinha esse problema porque o aldeão dele não tinha cérebro.

### O oito que queria dizer outra coisa

O original escreve, na profecia da queda, `y > 8`: oito blocos de margem por baixo do buraco, num mundo que
acabava no **zero**. O mundo de hoje acaba em **menos sessenta e quatro**, e oito à letra proibiria a
profecia em quase toda a parte. O que se mede aqui é a **distância ao fundo do mundo**, que é o que o oito
queria dizer.

E o chão que ela sabe abrir é um rótulo próprio — `thaumcraft:soft_ground` —, porque o `#minecraft:dirt` de
hoje são três blocos e **não inclui a grama**, que era metade do que o original aceitava.

### O miolo que respira

A casca de dentro da esfera **pulsa com a hora do mundo**: toma-se a hora, dá-se o resto por cento e
sessenta, mede-se a distância desse resto a oitenta e tira-se isso de cem. O que sai vai de vinte a cem e
volta, num vaivém de oito segundos.

Quer dizer que **todas as bolas do mundo respiram ao mesmo tempo**, porque todas leem a mesma hora. Numa casa
com duas, elas batem juntas. O original não fez isso por acaso.

A conta mora na **alma do bloco** e não no desenhista: é uma conta de números, e o servidor também a tem de
poder ver, nem que seja só para a provar.

### O engano do original, portado

Ao escolher a vítima entre os jogadores por perto, o original mede sempre a distância de **quem bateu** à
bola, e não a do jogador que está olhando. A conta dá o mesmo número em todas as voltas, de modo que o que
ele realmente escolhe é o **último da lista**, e não o mais perto. Com um só a assistir — que é o caso de
quase sempre — não faz diferença nenhuma. **Fica como está.**

### Dois desvios declarados

- **O que cai da profecia do minério guarda-se como item e não como pilha**, porque a lista das dezessete é
  montada quando a classe carrega, e nessa altura uma pilha ainda não se pode fazer: os componentes dos itens
  só se ligam depois. A pilha faz-se na hora de cair.
- **O Óleo do Acaso pede «a poção», qualquer que seja**, e não a Poção de Visão Noturna — o mesmo desvio já
  declarado para o Unguento do Voo, e pela mesma razão: a tabela do pote casa por item, e uma poção de hoje
  leva o que é num componente.

**Guardas:** `OccultaCrystalBallGameTest`, com cinco — as dezessete com os números e os prazos do original; a
marca de vidente; a de uma só de cada vez, com a segunda leitura repetindo a primeira; a prova que carrega a
fatia, que é **a profecia da queda abrindo o chão depois do prazo**; a recarga de cem batidas; e o vaivém do
miolo. E `OccultaCrystalBallClientTest`, com três telas: a bola num pedestal, a mesma oitenta batidas depois
— para se ver o miolo noutro tom — e os dois itens no inventário.

## O Item Posto, e a Arthana deitada no altar (2026-10-05)

Um bloco que **não é nada**: não tem forma, não tem textura, não se fabrica, não aparece em aba nenhuma e
não tem item próprio. O que ele faz é guardar **uma coisa deitada no chão** e desenhá-la ali, parada, virada
para o lado de quem a pôs.

### Por que ele existe

Porque **o altar precisa de saber o que está em cima dele**, e um item largado no chão não serve: ele rola,
se junta a outro igual, o jogo o apanha quando alguém passa por perto, e some ao fim de cinco minutos. Nada
disso vale para um altar que conta o que tem em cima de cada pedra.

A resposta do original é simples e boa: a coisa **vira bloco**. E um bloco fica onde o puseram.

### O que ele dá ao altar

A **Arthana** deitada numa das seis pedras **dobra o alcance** do altar: dezesseis blocos viram trinta e
dois. É um gesto, e vale ver o que ele diz: a faca que abre o que os bichos guardam, pousada na pedra, faz o
altar alcançar mais longe. O original não explica, e não precisa.

No original há mais dois que se deitam no mesmo bloco e que ainda não foram portados: o **Ramo Místico**,
que soma ao poder de encanto, e o **Pentáculo de Kobolditas**, que **dobra** a velocidade de recarga. Entram
quando entrarem.

### Como se põe e como se tira

Com a Arthana na mão, clicando no **topo de uma pedra de altar** com ar por cima. Ela sai do inventário e
fica deitada ali. Partindo o bloco, ela volta — a não ser no **criativo**.

No original, essa exceção é feita acendendo o **número oito** do bloco quando quem o parte está no criativo,
e o `getDrops` dele olha esse número antes de largar o que quer que seja. Aqui a pergunta é feita direto no
`playerWillDestroy`, que dá no mesmo e poupa um feitio inteiro só para dizer «foi o criativo».

E a **tabela de despojos dele é vazia de propósito**: o que estava deitado nele é largado pelo próprio bloco,
que é o único que sabe o que era.

### A quietude

A coisa é desenhada como um item largado no chão, menos as duas coisas que o tornariam vivo: ela **não
boia** e **não gira**. O original escreve isso à mão, sobrepondo o `shouldBob` e o `shouldSpreadItems` do
desenhista de itens.

Essa quietude é o ponto todo. Uma faca largada no chão de um altar é lixo; uma faca **deitada** nele é um
instrumento. O original gastou um bloco inteiro para fazer essa diferença, e ela só se vê porque a coisa
está parada.

**Guardas:** `OccultaPlacedItemGameTest`, com quatro — o bloco guardando o que lhe deitaram e o botão do meio
tirando a coisa e não o bloco; a queda com o chão; o lado para onde ele está virado; e a prova que carrega a
fatia, que é a **faca deitada dobrando o alcance do altar**, com a segunda faca não somando nada. E
`OccultaPlacedItemClientTest`, com duas telas: a faca deitada numa pedra de altar e as quatro voltas lado a
lado.

## A Caveira do Chamado e o Ovo do Infinito (2026-10-05)

As duas coisas que se põem no chão e **puxam o mundo para elas**: uma puxa os mortos, a outra puxa o poder.

### A Caveira do Chamado

Uma caveira de esqueleto que, **acordada**, puxa os mortos-vivos para si de até **sessenta e quatro blocos**
à volta.

Mas não de uma vez. De **cinco em cinco segundos** ela acorda **um oitavo do mundo** — um quadrante de
sessenta e quatro blocos, acima ou abaixo — e manda andar na direção dela tudo o que ali for morto-vivo.
Oito voltas e ela deu a volta ao mundo inteiro: **quarenta segundos** para um giro completo.

Essa roda é o que torna a caveira útil em vez de absurda. Se ela puxasse tudo de uma vez, uma armadilha com
uma caveira acesa seria uma panela de zumbis ao fim de meio minuto. Puxando um oitavo de cada vez, eles
chegam **aos poucos e por um lado** — que é como se faz uma armadilha e não um massacre.

E os oito quadrantes do original são copiados à letra, porque a ordem deles é esquisita: os quatro primeiros
são os de **baixo** e os quatro últimos os de **cima**, e os de cima **não estão na mesma ordem** dos de
baixo — o quinto é o canto noroeste, o sexto o sudeste, o sétimo o sudoeste e o oitavo o nordeste. Fica como
está.

**A Pedra Necrótica acende, e volta a tirar.** A caveira posta está dormindo. Com a pedra na mão, clicando
numa que dorme ela **acorda**, com chamas e um relincho de cavalo esquelético; clicando numa acordada, ela
**estoura** e volta para o chão como item. Não há como apagar uma caveira sem a levantar — acender uma é
uma decisão, e desligá-la custa ir lá buscá-la.

Ela é **inquebrável** e aguenta mil de explosão. Quem faz uma armadilha de mortos-vivos não quer que o
primeiro deles a parta.

**Ela não tem modelo próprio:** o original pega a **cabeça de esqueleto do jogo** e lhe troca a pele — uma
folha para a que dorme e outra para a acordada. Aqui é o mesmo, com a camada de modelo do jogo pedida
emprestada. E o **giro vai na pilha e não na peça**, porque o jogo de hoje desenha depois e a peça é uma só:
mexer no ângulo dela faria com que todas as caveiras do mundo saíssem com o ângulo da última.

**O morto-vivo é um rótulo.** O original pergunta pelo *atributo de criatura* do bicho, que era um dos cinco
que a versão de 2014 tinha. Hoje a mesma pergunta se faz pelo `#minecraft:undead`, que é o que o encanto do
Golpe Sagrado também usa — e que quem jogar pode mexer.

### O Ovo do Infinito

Por fora é um Ovo de Dragão. Por dentro é o avesso dele: **ele não foge**. O ovo do jogo é uma piada —
bate-se nele e ele salta para outro lugar —, e este fica onde o puseram. O original consegue isso escrevendo
duas funções vazias por cima das do ovo: a do clique e a da pancada.

Perto de um altar ele vale **mil** de natureza, quatro vezes o ovo de dragão, que já era o que mais valia de
longe. E posto **em cima** de uma das seis pedras, ele **multiplica por dez** o teto e a velocidade do altar
inteiro.

Dez. Não é um enfeite como a caveira ou o candelabro: é o fim da escala. Um altar com um Ovo do Infinito em
cima deixa de ter contas que valha a pena fazer.

E ele **não se fabrica**. O original não lhe dá receita nenhuma, nem rito, nem despojo: ele existe na aba do
criativo e mais nada. É um objeto de quem constrói mundos, e não de quem joga neles.

**Ele não estende o Ovo de Dragão**, estende o que ele estende — o bloco que cai. A razão é de carpintaria:
o Ovo de Dragão de hoje tranca o molde dele a si próprio, e um bloco que o estenda não consegue dar o seu. O
que se herdava dele era a forma, a queda e a cor do pó, e as três estão aqui à mão.

### O que o altar passou a contar

Com esta fatia, a lista do altar fica assim:

| em cima de uma pedra | o que faz |
| --- | --- |
| caveira de esqueleto / wither / gente | soma 1 / 2 / 3 ao teto e à velocidade |
| tocha | soma 1 à velocidade |
| candelabro | soma 2 à velocidade |
| cálice vazio / cheio | soma 1 / 2 ao teto |
| Arthana deitada | **dobra** o alcance |
| **Ovo do Infinito** | **multiplica por dez** o teto e a velocidade |

Faltam só dois: o **Ramo Místico** e o **Pentáculo de Kobolditas**, que se deitam no mesmo bloco que a
Arthana.

**Guardas:** `OccultaAlluringSkullGameTest`, com seis — a caveira dormindo e a luz dela; a queda com o que a
segura; a prova que carrega a fatia, que é **o chamado pondo um zumbi a andar**; a ovelha que não o ouve; o
ovo multiplicando o altar por dez; e o ovo não fugindo de quem lhe bate. E
`OccultaAlluringSkullClientTest`, com três telas: as duas caveiras lado a lado, uma em cada parede de um
pilar, e o ovo em cima de um altar.

**E uma lição da arena:** um bicho acabado de nascer numa prova ainda **não tocou o chão**, e o jogo não
traça caminho nenhum para quem está no ar. O chamado achava o zumbi, contava-o, mandava-o andar — e ele
ficava parado. Vinte batidas de espera e passou. É o terceiro jeito que a arena de uma prova tem de mentir,
depois de **cair** e de estar **girada**.

## A Infusão, e a primeira delas: o Outro Lugar (2026-10-05)

É o maior passo que o ofício dá, e vale dizê-lo por extenso: até aqui, **tudo o que a bruxa faz está fora
dela** — o caldeirão, o círculo de giz, o altar, o boneco, o espelho. A infusão é a primeira coisa que ela
faz **a si própria**.

### O rito que mata quase

O Rito da Infusão faz **cem de dano mágico** a tudo o que for gente num raio de quatro blocos, e infunde
**quem sobreviver**.

Cem. Um jogador de armadura cheia e coração cheio tem vinte. O que salva quem se infunde não é aguentar o
golpe: é **ter mais vida do que o golpe tira**, o que só se consegue com cozimentos, com absorção ou com
resistência. A infusão é uma coisa que **se sobrevive**, e o original nunca fingiu o contrário.

Quem sobreviver fica com **duzentas cargas** e o teto nelas.

### E a carga não volta sozinha

É a decisão de desenho mais importante deste ramo: **não há recarga passiva**. O que o rito der é o que há,
e cada poder gasta. Para encher outra vez é preciso ou **refazer o rito** — quatro mil de altar e quase
morrer — ou ir a uma **Estátua de Adoração**, que dá trinta de cada vez e ainda não está portada.

A infusão não é uma barra de mana: é um **cantil**. Quem se infunde anda a contar as goladas.

E há uma crueldade no original que está portada: tentar um poder **sem carga bastante** não só recusa como
**apaga o que sobrava**. Quem tem seis e tenta um de dez fica com zero. O que o equipamento de fora chama —
o `aquireEnergy` — só recusa; o que a própria infusão gasta por dentro é que castiga.

### A Mão de Bruxa

A infusão **não faz nada sozinha**. Tudo o que ela sabe fazer passa pela **Mão de Bruxa**: segurá-la, socar
com ela, largá-la — tudo isso chega à infusão de quem a tem, e sem ela a infusão fica calada.

E ela **não se fabrica**. Cai de uma **bruxa morta**, uma vez em três — ou uma em duas, se quem a matou
tinha a **Arthana** na mão.

Junte as duas coisas e veja o que o original está dizendo: para usar o poder que você pôs dentro de si, você
precisa da mão de alguém que o tinha. O ofício não é gentil.

### A Infusão do Outro Lugar

É a do **enderman**, e dá quatro coisas — as quatro formas de **não estar onde se está**:

| o que se faz | o que acontece | custa |
| --- | --- | --- |
| segurar e largar a Mão | salta para onde se está olhando | **1** |
| agachado, segurar três segundos e largar | guarda o **lugar de voltar** | — |
| agachado, largar antes disso | volta para ele, de onde quer que se esteja | **2** |
| socar um bicho | atira-o — e a si — **oito blocos para cima** | **2** |
| agachado, socar um bicho | **o leva consigo** para o lugar de voltar | **4** |

Repare no último: ele é a razão de a infusão existir. Levar **outra pessoa**, à força, de qualquer
distância, para um lugar que você escolheu, é o poder mais bruto que este mod dá — e custa quatro de
duzentos.

### O salto que sonda

A conta do alcance é do original e é esquisita de boa: o alcance **cresce enquanto se segura** — quarenta
blocos de partida, e mais vinte por segundo —, e a cada segundo o jogo **diz se há onde chegar**: um tinir se
há, um estouro se não há.

Quem segura a Mão está **sondando o mundo à frente**, e o ouve. É um mecanismo de mira feito só com som, e
funciona.

### Desvios declarados

- **A trava depois de um salto é a do jogo.** O original tranca a Mão por mil e quinhentos milésimos de
  segundo num número que ele escreve na própria peça; aqui é a trava que uma bola de ender usa, e por isso
  **se vê** no inventário. São trinta batidas, que é o mesmo tempo.
- **O soco é um gancho de fora.** O original sobrepõe o `onLeftClickEntity` do item; aqui é o
  `AttackEntityCallback`, que engole o golpe do mesmo jeito.
- **O Espírito do Outro Lugar pede «a poção», qualquer que seja**, pelo mesmo motivo já declarado para o
  Unguento do Voo.

### O que falta deste ramo

As outras **três infusões** — a do Mundo (com os símbolos que se desenham no ar), a da Luz e a Infernal
(com os poderes de bicho) —, a **Estátua de Adoração** que enche o cantil, e a **barra de poder** na tela. O
estado já atravessa a rede para o lado do cliente, à espera dela.

**Guardas:** `OccultaInfusionGameTest`, com seis — ninguém nascendo infundido; o rito enchendo o cantil; a
prova que carrega a fatia, que é o **cantil esvaziando e castigando**; o encher que não passa do teto; o
lugar de voltar guardando o mundo em que se estava; e os números todos. E `OccultaInfusionClientTest`, com
duas telas: as duas coisas no inventário e a Mão segurada.

## A Infusão da Luz, e a barra de poder (2026-10-05)

A segunda das quatro. Se a do Outro Lugar é a de **não estar onde se está**, esta é a de **não ser visto, e
pôr paredes onde não há**. Ela não mata ninguém: tudo o que faz é com **luz** — luz que se dobra à volta de
quem a tem, e luz que endurece e vira muro.

### O que ela dá

| o que se faz | o que acontece | custa |
| --- | --- | --- |
| segurar a Mão | de trinta em trinta batidas, fica **invisível** — e **tudo o que o perseguia a vinte blocos perde o alvo** | **1** |
| agachado, largar antes de um segundo, olhando um **bicho** | ergue à volta dele uma **gaiola de luz** | **3** |
| …olhando o **topo** de um bloco | levanta um **escudo de três colunas** à sua frente | **3** |
| …olhando o **lado** de um bloco | faz brotar dali uma **parede de dezesseis** naquele rumo | **3** |
| socar um bicho | se houver três blocos de ar **quatro acima dele**, ele é posto lá e **trancado numa gaiola** | **5** |

Esse último é o poder mais útil deste mod e ninguém diz isso em voz alta: **tirar uma coisa do chão e
trancá-la no ar** resolve qualquer luta sem um golpe. Custa cinco de duzentos e só pede que haja céu.

### A metade que ninguém espera

A invisibilidade de trinta batidas só esconde. O que faz esta infusão valer é a segunda metade: **tudo o que
estava perseguindo você perde o alvo**. Quem já vinha atrás de você deixa de saber para onde ia — e isso não
é esconder, é **desfazer a perseguição**.

E a luz some quando o poder acaba: a invisibilidade é renovada de trinta em trinta batidas, e **tirada** no
instante em que você larga a Mão ou a carga acaba. O original é explícito nisso. Quem se esconde com luz
emprestada fica visível quando o empréstimo acaba.

### O alvo cru

O original lê o campo do alvo **direto**. O jogo de hoje passa o `getTarget` por um filtro que recusa, entre
outros, quem está no criativo — e por isso o porte usa o `getTargetUnchecked`, que é o campo. **Quem está
perseguindo você está perseguindo você.**

Essa mesma coisa teve consequências na arena das provas, e vale guardá-las:

- o `setTarget` de qualquer bicho passa o alvo pelo mesmo filtro, de modo que **nenhum bicho mira num
  jogador criativo**;
- o jogador de mentira que entra no mundo (`makeMockServerPlayerInLevel`) nasce **criativo** e não há como o
  tirar de lá — nem pelo modo de jogo, nem mexendo nas capacidades à mão;
- o outro (`makeMockServerPlayer`) aceita o modo de jogo de partida, mas **não tem ligação de rede**, e por
  isso não se lhe podem dar poções.

Por isso as duas metades do poder são provadas **em duas provas**: o esquecer com o jogador da sobrevivência,
e o esconder com o que está no mundo.

### A barra de poder

Um **tubo de vidro** de oito por trinta e dois, encostado à direita da tela e no meio dela, que se enche de
baixo para cima com a carga que a pessoa tem.

E o que o enche **muda com a infusão**: a do Outro Lugar o enche com a textura do **portal**, a da Luz com a
da **neve**. É um detalhe pequeno do original e é o que torna a barra legível de relance — não se precisa de
ler um número para saber qual delas se tem.

Ela só aparece a quem **está infundido**. Sem infusão não há tubo nenhum.

**Fica de fora, declarado:** a **segunda barra**, a dos poderes de bicho da Infusão Infernal, que o original
desenha ao lado desta com a textura de argila. Ela entra com a infusão dela.

### O Fantasma da Luz

O que o rito pede, e o **mais barato dos quatro**: dois mil de poder em vez de quatro mil. É por ele que
quase toda gente começa.

**Guardas:** no `OccultaInfusionGameTest`, mais duas — o **esquecer** e o **esconder**. E, no
`OccultaInfusionClientTest`, mais uma tela: a **barra de poder** cheia a dois terços com a textura do portal.

## A Infusão Infernal, e o Ânimo que o Coração de Demônio esperava (2026-10-05)

A terceira das quatro, e a que muda **o que você é para os outros**. As outras duas lhe dão coisas para
fazer; esta lhe dá **gente**.

### O que ela dá

| o que se faz | o que acontece | custa |
| --- | --- | --- |
| agachado, socar um bicho | ele passa a ser **seu** | **5** |
| socar um bicho sem agachar | **todos os seus**, a cinquenta blocos, vão **atrás dele** | **1** |
| agachado, largar a Mão olhando o chão | todos os seus **largam o alvo e vão para ali** | — |

Repare na diferença entre o segundo e o terceiro: um manda **atacar**, o outro manda **ir**. Com os dois,
quem tem esta infusão deixa de lutar — ele **aponta**. E o terceiro é **de graça**, porque sem ele um
exército não é um exército: é uma matilha.

O alcance é de **cinquenta blocos** para os lados e **quinze** para cima e para baixo. Não é um raio de
comando curto: é meio bairro.

### O Ânimo Infernal

O que o rito pede, e o cozimento mais caro dos três: **quatro mil** de poder, e leva dentro o **Coração de
Demônio** e o **Mal Refinado**. O Coração de Demônio estava portado desde a fatia dele à espera de ter para
onde ir, e é aqui que ele vai.

Bebido, dá **Veneno II por um minuto** <i>e</i> **Deperecimento III por três** — e é o único dos quatro que
mata de verdade quem o beber.

### O giz

O anel deste rito é todo de **giz infernal**: dezesseis dentro e vinte e oito no meio. É o único dos três
que pede esse giz, e isso diz-lhe, antes de ele acabar, o que você vai ser.

### O que fica de fora, declarado

O **sacrifício**: agachado, socar outra vez um bicho que já é seu o mata e **lhe toma o poder**. Os poderes
de bicho são um ramo inteiro do original — **vinte e cinco deles**, treze famílias e uma **segunda barra**
na tela — e entram numa fatia só sua. Até lá, sacrificar um escravo toca o tambor de «não dá».

A lista, para quando essa fatia vier: aranha e aranha-das-cavernas (teia e trepar), creeper (estourar e
engolir raios), morcego e coruja (voar e visão noturna), lula (tinta e respirar na água), ghast e blaze
(bolas de fogo), homem-porco, zumbi, esqueleto, cubo de magma, slime e sapo (saltar), peixinho-de-prata,
jaguatirica, lobo e cavalo (correr), enderman, e os sete de **curar** — ovelha, vaca, galinha, porco,
aldeão e cogumelada.

**Guardas:** no `OccultaInfusionGameTest`, mais uma — **tomar e apontar**, que é a fatia inteira numa prova
só: o soco agachado escraviza por cinco, e o soco sem agachar manda o escravo atrás de quem o levou. E a
barra de poder ganha a textura da **pedra do Nether**, que é a desta infusão.

## Os poderes de bicho (2026-10-05)

A ideia mais estranha do Witchery e a melhor: a Infusão Infernal **não lhe dá poderes**. Ela deixa-o
**tirá-los de quem os tem**.

Toma-se um bicho para si, leva-se para onde se quiser, e então **mata-se** — e o que ele sabia fazer passa a
ser seu. Um de cada vez: tomar o poder de outra espécie **apaga** o que se tinha.

### Os vinte e cinco

| nº | bicho | o que ele dá |
| --- | --- | --- |
| 1, 2 | aranha-das-cavernas, aranha | **teia** onde se olha; e trepar paredes, travar a queda sob um teto, e parar na parede agachado |
| 3 | creeper | **estourar** em si próprio (três, ou seis por duas cargas se segurar); e **engolir raios**, enchendo a infusão em vinte e cinco |
| 4, 24 | morcego, coruja | visão noturna; e o **voo**: segurando o pular se sobe, e a queda nunca passa de cinco |
| 5 | lula | **cegar** quem se olha; nadar quinze por cento mais depressa; e **não se afogar** |
| 6 | ghast | **bola de fogo grande** |
| 7 | blaze | **três bolas pequenas** em leque |
| 8 | homem-porco | Resistência III e Força III; e **engolir fogo**, com resistência a ele de presente |
| 9 | zumbi | Resistência II e Força I |
| 10 | esqueleto | uma **flecha**, com a força do arco do jogo — crítica ao segundo cheio |
| 11, 12, 25 | cubo de magma, slime, sapo | Impulso IV; subir mais no ar; e **não cair** |
| 13–16 | peixinho-de-prata, jaguatirica, lobo, cavalo | Velocidade IV; e andar **quarenta e cinco por cento mais depressa**, sempre |
| 17 | enderman | o **salto** da Infusão do Outro Lugar, emprestado |
| 18–23 | ovelha, vaca, galinha, porco, aldeão, cogumelada | **curar meio coração** |

### O que a lista diz

Metade dela são bichos de capoeira que só sabem **curar**, e esses dão **uma carga** em vez de dez. Matar
uma ovelha para se curar meio coração é um péssimo negócio, e o original quis que fosse: o poder de bicho é
para os bichos que **custam a apanhar**.

E repare quais são os melhores. O **ghast** e o **enderman** são os mais caros de encher e os mais fortes de
usar. Os **que correm** — peixinho-de-prata, jaguatirica, lobo, cavalo — são dos mais fáceis de apanhar e
dão um acréscimo de velocidade **maior do que o da poção e que não acaba**. É a troca ao contrário, e é de
propósito: quem anda muito escolhe o lobo, quem luta escolhe o ghast.

### As cargas, e as duas que se gastam

Um bicho dá **dez** cargas (um, se for de capoeira), até um teto de **vinte**. E usar um poder custa
**duas coisas ao mesmo tempo**: uma carga de **infusão** <i>e</i> o que o poder pedir de carga de **bicho**.

Os três poderes que **engolem golpes** — o raio do creeper, o fogo do homem-porco, o afogamento da lula —
custam carga de **infusão** e não de bicho. Quer dizer que eles funcionam mesmo com o bicho vazio, enquanto
houver infusão.

E os poderes de **andar** não custam nada. Trepar, voar, nadar e correr são de graça, para sempre. É o
melhor que o ramo tem, e o original não o cobra.

### Onde eles correm

Os poderes de andar correm **no cliente**, a cada batida, e é assim no original por uma razão boa: mexer na
velocidade de quem joga só fica macio se for do lado dele. Feito do lado do servidor, o jogador veria o
próprio passo a corrigir-se de volta duas vezes por segundo.

O que torna isso possível aqui é a carga de bicho **atravessar a rede**: o lado de cá sabe que poder a
pessoa tem sem ter de perguntar.

### A segunda barra

Ao lado da barra da infusão, dez pixels mais para dentro, fica a dos **poderes de bicho**. Essa não é um
tubo que se enche: é uma **pilha de riscos**, um por carga — porque elas são poucas, no máximo vinte, e
contar vinte riscos é mais rápido do que medir um nível.

### Dois desvios declarados

- **A teia da aranha nasce onde o olhar bate**, até dezesseis blocos, em vez de ser atirada como frasco: o
  porte não tem ainda a entidade que atira frascos de ingrediente. O resultado no chão é o mesmo; o que se
  perde é o arco da coisa no ar.
- **O creeper pergunta à fonte do dano** se o golpe veio de um raio. O original descobre isso **lendo a
  pilha de chamadas** à procura do `onStruckByLightning`, porque em 2014 não havia como perguntar.

**Guardas:** `OccultaBeastPowerGameTest`, com quatro — os vinte e cinco com os números e as cargas do
original; cada poder saindo do bicho certo; a prova que carrega a fatia, que é o **sacrifício** (o primeiro
soco toma, o segundo mata e toma o poder); e a troca de poder, que apaga o que se tinha e soma só até vinte.
E, no `OccultaInfusionClientTest`, mais uma tela: as **duas barras** lado a lado.

## A Vara Mística e os símbolos (2026-10-05)

A parte do mod em que **o gesto é a interface**. Não há menu, não há lista, não há botão: há um desenho que
se sabe ou não se sabe fazer.

### Como se lança um feitiço

Segura-se o botão com a **Vara Mística** na mão e move-se a cabeça. Cada **sete graus** de giro contam um
**traço** — cima, baixo, direita, esquerda —, e quinze traços é o máximo. Quando o que foi desenhado bate com
um dos desenhos da tabela, o nome do feitiço aparece. Largando a vara, ele sai.

E cada símbolo tem **vários desenhos**, um ou dois por grau: o de grau um é curto, e os de dois e três
repetem traços para ficarem mais longos. Quanto mais comprido o desenho, mais forte o feitiço — e mais fácil
de errar.

### O que ele custa

O custo **dobra por grau**: um feitiço de custo um gasta uma carga no grau um, duas no dois e quatro no
três. E o grau comprido só vale o grau dele a quem tiver **Adoração** bastante — sem ela, um desenho de grau
três é lançado como **grau um** e o esforço foi para nada.

É o que liga os símbolos à **Estátua de Adoração**. Enquanto ela não estiver portada, os graus dois e três
só se alcançam no criativo. **Fica declarado.**

### A ordem das recusas

A vara recusa por cinco razões, e a ordem delas conta uma história: primeiro se pergunta se **há feitiço**,
depois se **há infusão**, depois se **a infusão serve** — os imperdoáveis só se lançam com a Infernal —,
depois se o feitiço **está de molho**, e só no fim se **há carga**. Cada recusa tem o seu recado.

### Desenhado de cá, lançado de lá

Quem lê o desenho é o **lado do cliente**: é lá que a cabeça do jogador se move. Ele manda dizer ao servidor
**qual** símbolo e de que **grau**, e o servidor os guarda até a vara ser largada.

É o único jeito de o gesto ser confiável. Lido do lado do servidor, o atraso da rede faria o desenho sair
torto; lido do lado de cá e **confirmado** do outro, o que se desenha é o que sai. E o servidor **confere**:
um recado que diga um símbolo que não existe é largado sem mais.

### Os catorze desta fatia

De trinta e um do original, catorze:

| símbolo | o que faz | custa |
| --- | --- | --- |
| **Accio** | puxa para si tudo o que estiver largado à volta de onde a bola bateu: oito décimos de bloco no grau um, três no dois, **nove** no três | 1 |
| **Aguamenti** | água onde a bola bate — e **no Nether só no grau três**, porque lá ela some | 1 |
| **Alohomora** | abre ou fecha a porta em que a bola bate | 1 |
| **Confundus** | náusea de dez segundos em quem a bola acertar | 1 |
| **Ennervate** | tira a **lentidão**, a **fraqueza** e a **náusea** — e a bola dele **cai** em vez de voar a direito | 1 |
| **Episkey** | cura, e **cobra a comida por ela** | 1 |
| **Expelliarmus** | desarma: o que estiver na mão cai no chão | 1 |
| **Flipendo** | empurra o que a bola acertar; nos graus dois e três, tudo a três ou seis blocos | 1 |
| **Impedimenta** | lentidão II por trinta segundos, nunca em quem o lançou | 1 |
| **Incendio** | fogo onde a bola bate; nos graus dois e três, pega fogo a tudo a três ou seis blocos. E **acende o Homem de Vime** | 1 |
| **Lumos** | um **Globo de Luz** onde a bola bate | 1 |
| **Nox** | tira **tudo o que der luz** num cubo de dez blocos à volta de quem o lança | 50 |
| **Protego** | um **escudo de luz** à frente, com a parede de três colunas da Infusão da Luz | 1 |
| **Stupefy** | **lentidão X por cinco minutos** em quem a bola acertar | 5 |

Três deles — **Episkey**, **Protego** e **Nox** — não atiram nada: agem a partir de quem os lança. Os
outros onze atiram a bola.

E dois merecem uma linha a mais:

- o **Episkey** é o único feitiço de cura do mod, e **não é de graça**: quem é curado perde da barriga o
  que ganhou de vida e fica com náusea quatro segundos. Curar alguém é **passar-lhe a conta**. Quem não
  tem barriga — tudo o que não é gente — se cura sem pagar nada, e é assim no original;
- o **Protego** tem o desenho mais curto que há, **dois traços**, o que faz dele o único que se pode
  acertar por acidente.

### Os dezessete que faltavam

Dez deles entraram na fatia «Mais dez símbolos, e as três imperdoáveis», logo abaixo. Os **sete** que
sobram pedem coisas que ainda não estão portadas: as **portas do ofício** (Colloportus), o **Tormento**
(Tormentum), a **Marca Negra** (Morsmordre) e o **Leonard** (os quatro dele). Os números e os desenhos
deles já estão levantados do original, traço por traço.

E dois detalhes do original que ficam como estão:

- **o Flipendo empurra quem o lançou, no grau dois.** A pergunta que o original faz é «se o raio for três,
  ou se o alvo não for quem lançou», e o «ou» deixa o próprio passar. É um descuido, e é engraçado.
- **o desenho do Nox vem com grau zero.** Não é engano: com grau zero o custo é **metade**, e o original
  preferiu escrever o preço assim a mexer no número.

### A bola

A mesma para todos os símbolos que atiram alguma coisa: um **quadrado sempre virado para quem olha**,
pintado da **cor do símbolo** e a pouco mais de metade de opaco. É por ela que se reconhece de longe qual
feitiço vem vindo.

A cor e o tamanho **não estão na entidade**: estão no símbolo, e a entidade só leva o número dele. É por
isso que o número atravessa a rede — para o lado de cá poder perguntar de que cor é a bola que está vendo.

E ela deixa um rastro de **gosma**, meio bloco acima de si. Uma maldição deixaria, em vez disso, pó de poção
e uma chama; nenhum destes seis é maldição, mas a pergunta já está feita.

**A folha da bola é a da bola de neve do jogo**, e não uma do mod. Parece engano e não é: o original **tem**
uma folha própria para isto — a `spelleffect.png` — e **não a usa**. O desenhista dele liga o atlas dos
itens e pede o ícone da bola de neve, que é redondo e branco, para o poder pintar de qualquer cor. A folha
própria ficou no mod sem ninguém lhe chamar.

### De onde vem a vara

Do **Rito da Árvore**, só de noite, com um **Galho de Ent** e o **Unguento Místico** — que é um cozimento de
três mil que leva diamante, muda, Coração de Creeper e Sangue Infernal.

E ela também se **deita no altar**, como a Arthana: aí o altar ganha **um de poder de encanto**. Com isso, a
única coisa que falta ao altar do original é o **pentáculo de kobolditas**.

**Guardas:** `OccultaSymbolGameTest`, com sete — a tabela de desenhos (a prova que carrega a fatia); os
catorze com nome e o desenho de dois traços do Protego; o custo que dobra por grau, com o grau zero do Nox;
o feitiço que se prepara antes de se lançar; o grau comprido que precisa de Adoração; o Incendio pondo fogo;
e a vara deitada no altar. E `OccultaSymbolClientTest`, com duas
telas: a vara no inventário e as quatro bolas lado a lado, para se verem as cores.

## A Infusão do Mundo, e a onda de choque (2026-10-05)

A quarta e última, e a que menos parece magia. As outras três fazem coisas que só a magia faz —
teleportar, apagar a luz, tomar bichos para si. Esta faz **peso**: ela pega no chão e no metal e os usa
como um ferreiro usaria, se um ferreiro tivesse quarenta toneladas de braço.

### O que ela dá

| o que se faz | o que acontece | custa |
| --- | --- | --- |
| cair mais de três blocos em terra mole | o bloco de baixo é **arrancado** e cai em item; a queda não dói | **5** |
| o mesmo, **agachado** | um **estouro de força três** no lugar dele; a queda também não dói | **10** |
| socar com a Mão quem tem **metal** no corpo | ele **voa** na direção do olhar, com três décimos de salto | **2** |
| o mesmo, **agachado** | voa com **um e meio** de salto, que é para cima | **4** |
| segurar a Mão **agachado**, passados dois segundos | de quatro em quatro batidas, todo o **metal largado** a seis blocos vem para a mão | **1** |
| e, na mesma batida, todo o **minério** a seis blocos | funde-se sozinho em lingote | **2** por minério |
| largar olhando para um **bicho** | ele é **desarmado**: o metal que tinha na mão cai no chão | **2** |
| largar olhando para o **topo** de um bloco | uma **coluna de seis** blocos sobe **três níveis**, com quem estiver nela | **2** |
| largar olhando para o **lado** de um bloco | ele é **arrancado da parede e atirado** | **3** |
| largar **agachado** olhando para um minério | funde-se em **dois** lingotes | **2** |
| largar olhando para **nada**, depois de a segurar | a **onda de choque** | **6 por segundo** |

Repare na coluna da direita: o único poder que escala com o tempo é a onda, e ela escala nas duas pontas —
o raio é `2 × segundos + 2` e o preço é `6 × segundos`. Segurar a Mão dez segundos abre um anel de vinte e
dois blocos de raio e custa sessenta cargas, que é quase um terço do cantil.

### O metal é a fraqueza

O soco e o desarmamento só pegam em quem tem **ferro, ouro ou malha**. A lista do original tem **vinte e
cinco** coisas, escritas à mão: as dez ferramentas de ferro e de ouro, os doze pedaços de armadura dos três
metais, os dois lingotes e a pepita de ouro.

Nada de diamante. Nada de couro. Nada de pedra.

Quer dizer que, contra quem tem esta infusão, **a boa armadura é um perigo** e a armadura ruim é segurança.
É a única vez em todo o mod em que o original faz isso, e é uma ideia melhor do que a maior parte do que
ele faz: o caro vira risco.

Aqui a lista é o rótulo `thaumcraft:earth_metal`, de modo que quem jogar possa mexer nela. A **pepita de
ferro** fica de fora porque não existia em 2014, e a **netherita** também, pela mesma razão.

### A onda de choque

É o poder mais bonito do mod de se ver e o mais caro, e vale a sua própria classe. Um **anel de chão que
se levanta e volta a cair**, abrindo-se a partir de quem o fez, um bloco de raio por batida. O que estiver
na crista leva **oito de dano** e é atirado para longe com o mesmo empurrão do círculo de proteção.

A parte que importa é a segunda metade: ele **não é um estouro**. Dois blocos de fundura sobem um nível na
crista e são postos de volta atrás dela, de modo que, **passada a onda, o terreno está como estava**. Uma
onda que deixasse cratera seria só uma bomba lenta; esta é um poder de bruxa, e a prova que carrega a fatia
é exatamente essa — o chão volta.

O anel é desenhado com o **algoritmo do círculo de Bresenham**, à letra, com um oitavo andado e espelhado
nas outras sete partes. É o desenho que um jogo de 1985 usaria para uma circunferência, e é por isso que a
onda tem o aspecto quadrado que tem. Foi portado tal e qual, incluindo os espelhos na ordem em que o
original os escreve.

### A Rocha

O bloco que foi arrancado da parede, a caminho de quem estiver à frente. **Seis de dano** onde bater, e
nada mais. Ela não se fabrica e não serve para nada na mão: existe para voar — no original ela é item só
porque o projétil dele precisa de um item para se desenhar, e aqui é o mesmo.

E nem todo bloco se atira. São os **vinte e quatro** que o original lista à mão — terra, grama, micélio,
pedra, pedregulho, areia, cascalho, arenito, argila, terracota, tijolo, pedra do Nether e as escadas e lajes
deles —, e com uma condição a mais: o bloco tem de estar **solto por trás**. É o que faz do poder uma
escolha e não um botão: só se arranca da parede o bloco que já estava à beira de não ter parede.

O original escreve essa condição com quatro ramos espelhados e **os nomes dos lados trocados** — o
`BlockSide` dele chama NORTE ao oeste do jogo e ESTE ao norte, que é um engano famoso da 1.7.10 —, mas os
quatro dizem a mesma coisa, e é essa. Aqui ela é uma linha.

### A Alma do Mundo

O que o rito pede: **quatro mil** de poder, e leva dentro uma **Pedra Afinada** — que já pediu uma viagem
para se carregar — mais a maçã dourada encantada, a raiz de mandrágora e uma muda de sorveira. Bebida, dá
**Veneno II por um minuto**, como as outras três. Nenhuma delas se bebe.

O anel do rito é o **mesmo do rito da Luz**: dezesseis dentro e vinte e oito no meio, no eixo do giz comum.

### Desvios declarados

1. **O dicionário de minérios morreu.** O original pergunta ao dicionário de 2014 que lingote sai de cada
   minério: qualquer coisa chamada `oreX` com um `ingotX` do outro lado servia. Esse dicionário não existe
   mais. Hoje a pergunta é feita aos **rótulos** que o jogo já tem — `#minecraft:iron_ores` e
   `#minecraft:gold_ores` —, mais o **cobre**, que em 2014 não era do jogo mas hoje é e cai exatamente na
   regra que o original escreveu. E o que fica no lugar é **ardósia** se o minério era de ardósia, em vez
   da pedra que o original punha sempre.

2. **O ímã tem dois cuidados que o original não tem.** A conta do puxão é estranha de propósito: o original
   divide as **três** componentes pelo módulo da **primeira**, o que faz do empurrão em X sempre um bloco e
   dos outros dois um múltiplo de quantos blocos o item está desalinhado em X. O resultado é o puxão aos
   saltos que se vê no original, e foi portado assim. Mas o módulo ganhou um **piso** — sem ele, um item
   exatamente alinhado em X divide por zero e sai com a posição estragada — e cada componente é **cortada
   ao cubo de seis blocos**, para um item quase alinhado não ser atirado para fora do mundo. São os dois
   cuidados mínimos para o porte aguentar o que o original escreveu.

3. **A fundição de longe para quando a carga acaba.** O original segue varrendo o cubo inteiro e falha num
   minério por vez. Como ficar sem carga **apaga o que sobrava**, o que acontece ao mundo é o mesmo nos
   dois; a diferença é que o original toca o tambor da falha até mil cento e oitenta e três vezes seguidas.

4. **O empurrão em quem é gente.** O original manda um pacote próprio ao cliente para empurrar outro
   jogador, porque na 1.7.10 o movimento de um jogador não atravessava a rede de outro jeito. Hoje
   atravessa: `setDeltaMovement` com `hurtMarked` faz o jogo mandar o pacote certo sozinho, e o efeito é o
   mesmo.

5. **A lista do que não se mexe.** A `BlockProtect` do original recusa o que tem alma, a rocha-mãe, o ovo
   de dragão e dois blocos do próprio Witchery — a Força e a Barreira — que ainda não estão portados.
   Ficam os três primeiros; os outros dois entram quando os blocos entrarem.

### O que a arena de prova ensinou

A arena do `runGameTest` tem **oito blocos de altura e uma tampa de barreira** por cima, no oitavo nível.
De pé no sétimo, os **olhos** de quem olha já estão dentro da tampa, e o traçado para baixo acerta nela e
não no chão — o que, com este poder, levanta a tampa e não a coluna. A prova da coluna é montada a meia
altura por causa disso, e os dez blocos de chão firme que o poder exige descem por baixo do piso da arena,
até o fundo do mundo.

**Guardas:** o `OccultaOverworldInfusionGameTest`, com dez — os números do original; o anel do rito, que é
o **mesmo do rito da Luz** e só se distingue dele pelo frasco que está no chão; o que ela chama de metal
(ferro, ouro e malha, nunca o diamante); a queda que arranca o chão e não dói; o soco que só atira quem tem
metal, de pé e agachado; o ímã que puxa o metal e deixa o resto; o desarmamento; a coluna que sobe três
níveis; a fundição de perto que dá dois lingotes; e a **onda de choque que põe o chão de volta**, que é a
que carrega a fatia. E o `OccultaInfusionClientTest` ganhou a quinta tela: a barra de poder cheia da
textura da **terra**, que é a desta infusão.

## O koboldite, o regatear do goblin e o Pentáculo (2026-10-05)

Esta fatia fecha o **altar**. Era a última peça dele que faltava, e ela não se faz com o que o mundo dá:
leva um metal que **só sai de um goblin**.

### O metal que não se mina

Não há minério de koboldite. Não há forno que o faça, não há caldeirão que o cozinhe, não há rito que o
invoque. Ele entra no jogo por **uma porta só**: vender comida ou minério a um goblin paga, **uma vez em
três**, em pó de koboldite em vez de esmeralda.

E do pó até o lingote vai uma escada de três degraus, que o goblin abre **um de cada vez**:

| degrau | dá-se | recebe-se |
| --- | --- | --- |
| primeiro | **9 de pó** e 5 pepitas de ouro | 1 **pepita de koboldite** |
| segundo | **16 de pó** e 1 lingote de ouro | **2** pepitas |
| terceiro | **9 pepitas** e 1 esmeralda | 1 **lingote** |

Qual degrau ele mostra depende de **quantas trocas ele já tem** — e, como os ofícios um e dois nunca
oferecem mais nada, isso é o mesmo que perguntar quantos degraus já se subiram com ele. Subida a escada
toda, ele passa a oferecer ouro por esmeralda, que é o fundo do poço do aldeão do original.

Some as contas e veja o tamanho da coisa: um lingote são nove pepitas, que são quatro ou cinco trocas da
escada, que são **cento e tal medidas de pó**, que são **cento e tal vendas com sorte** — porque o pó é uma
em três. E o Pentáculo leva **quatro lingotes e quatro pepitas**.

### O goblin é um aldeão de outra espécie

As duas tabelas de quantidade que ele usa são, **à letra**, as do aldeão da 1.7.10 — a de quanto se dá por
uma esmeralda e a do ferreiro, com os números negativos a querer dizer o contrário dos positivos (*uma
pepita por tantas peças* em vez de *tantas pepitas por uma peça*). O que muda é a **moeda**: onde o aldeão
pede esmeraldas, o goblin pede **pepitas de koboldite**.

Os quatro ofícios: o **zero** vende comida e lã e troca cascalho por esmeralda e pederneira; o **um** e o
**dois** são os do koboldite, e são os únicos que **não se baralham** — uma escada baralhada não é escada;
o **três** é o ferreiro; o **quatro**, o açougueiro e o curtidor.

E ele **não dá experiência**. O aldeão do original larga esferas a quem regateia com ele; o `useRecipe` do
goblin não as larga, e é de propósito. Com ele não se sobe de nível, e a barra de progresso também não
aparece.

### Um goblin no mato não vende nada

O original só o deixa regatear se ele tiver **aldeia**: a coleção de aldeias de 2014 respondia se havia uma
a trinta e dois blocos. E ele também não regateia **na corda** — um goblin com picareta na mão é um
empregado, e um empregado não vende.

Juntas, as duas regras dizem uma coisa só: para comprar koboldite é preciso **achar uma aldeia com
goblins**. É o começo da linha, e é a parte que leva tempo de jogo.

### O Pentáculo, e o altar fechado

`sks / kdk / sks` — quatro lingotes, quatro pepitas e um diamante. Deitado no altar com o Item Posto, ele
**dobra a recarga** dele.

E repare na ordem da conta: primeiro soma-se tudo o que soma — caveiras, tocha ou candelabro, cálice —,
**depois** o Pentáculo dobra, e **só então** o Ovo do Infinito multiplica por dez. Os dois juntos dão
**vinte vezes** a velocidade de um altar pelado.

Com ele, o altar do porte tem **todas** as peças do original.

### Desvios declarados

1. **A aldeia de 2014 virou pontos de interesse.** A `VillageCollection` não existe mais. O que hoje lhe
   corresponde é a conta de camas e postos de trabalho que o `isCloseToVillage` faz por seções de dezesseis
   blocos; **duas seções** são os trinta e dois blocos do original. As peças de aldeia do porte entram nas
   piscinas das aldeias do jogo, de modo que os goblins moram em aldeias de verdade e a conta vale.

2. **O balcão fecha quando o goblin morre, e não quando quem compra se afasta.** É o que o original faz — o
   balcão dele só pergunta se quem está do outro lado é o mesmo — e também o que o aldeão de hoje faz.

3. **A reputação não entra.** O original, ao recarregar a loja, dá um ponto de reputação na aldeia a quem
   regateou. A reputação de hoje é um sistema de gossip entre aldeões, que não é a mesma coisa e que um
   goblin não tem como alimentar. O que fica é o resto do costume: quarenta batidas depois de se lhe esgotar
   a última troca, ele põe mais uma e ganha **regeneração por dez segundos**.

4. **A picareta de koboldite fica para depois**, com o elmo e o abafador de orelhas. Ela cavaria quinze vezes mais
   depressa e fundiria metade do minério, e é a única coisa da linha que não é precisa para fechar o altar.

**Guardas:** no `OccultaGoblinGameTest`, mais três — a **escada do koboldite**, que é a prova que carrega a
fatia e confere os três degraus pela ordem e o fim dela; o **pó que cai no lugar da esmeralda**, contado em
duzentos goblins, com o pó a sair menos vezes que a esmeralda; e o goblin **no mato**, que não regateia e não
dá nível. E no `OccultaAltarGameTest`, o **Pentáculo**, que dobra a recarga, dobra depois de a tocha somar, e
não dobra duas vezes. E duas telas no `OccultaGoblinClientTest`: os quatro pedaços do metal no inventário e o
**balcão** aberto no primeiro degrau da escada.

## A Estátua de Adoração (2026-10-05)

É a **única coisa do mod que enche uma infusão**. Sem ela, a infusão é um cantil que se enche uma vez e
acabou: o rito custa quatro mil de altar e quase mata, e o que ele der é o que há. Com ela, é uma barra que
volta.

### Ela precisa de goblins

De cinco em cinco segundos a estátua conta quantos goblins a adoram num cubo de oito blocos — e manda
adorar os que ainda não adoram. O que ela faz depende do número:

| adoradores | o que acontece |
| --- | --- |
| **cinco** | o dono, a sessenta e quatro blocos ou menos, ganha **trinta de carga** por pulso |
| **dez** | o dono ganha **Adoração** |
| **quinze** | a Adoração sobe para o **segundo nível** |

O segundo nível é o que o **terceiro grau dos símbolos** pede. Quer dizer que lançar um Accio comprido
custa, lá atrás na cadeia, **quinze goblins num cubo de oito blocos** — que é uma aldeia inteira de goblins
junta à volta de um ídolo com a sua cara. O ofício não é discreto nesta ponta.

E eles não obedecem sempre: a estátua manda, e o goblin atende **duas vezes em três**. Com isso, vinte
goblins à volta dela nunca dão vinte adoradores — dão catorze ou quinze, e é por isso que o terceiro degrau
custa o que custa. Cada um fica meio minuto e depois sai quando lhe dá na gana, com dois terços de chance
por batida.

E ela conta **antes** de mandar: quem acabou de ser chamado só entra na conta do pulso seguinte. Uma
estátua recém-posta leva uns segundos a pagar.

### Ela tem a cara de alguém

O desenhista dela é o **boneco de sempre**, desenhado **duas vezes** com a mesma malha: primeiro com a
**pele do dono**, puxada a setenta por cento para parecer pedra, e depois com uma **folha de pedra
translúcida** por cima. O que se vê é a cara de alguém **debaixo** de pedra, e não uma pedra com uma cara
pintada. É uma diferença pequena de desenho e é toda a graça da coisa.

E é um **boneco de criança**: o original liga o `isChild` do `ModelBiped`, que desenha a cabeça a três
quartos e o resto a metade, cada um deslocado para baixo. Uma estátua de cabeça grande, que é o que a faz
parecer ídolo e não enfeite. As duas contas — `0.75` com um bloco de descida, `0.5` com um bloco e meio —
são portadas à letra.

A malha é assada **duas vezes**, com tamanhos de folha diferentes: a pedra do Witchery é de **sessenta e
quatro por trinta e dois**, que é o formato de pele de 2014, e a pele de quem jogar é de **sessenta e quatro
por sessenta e quatro**, que é o de hoje. Elas encaixam porque o formato antigo é, à letra, a metade de
cima do novo.

E o item na mão é a **mesma estátua**: o original prende o desenhista da alma também ao item, e aqui isso é
um `SpecialModelRenderer`. Sem ele, o item seria um quadrado chapado com uma folha desenhada para um boneco
— que é o engano que a lição da *chapa de modelo* deste porte já ensinou a evitar.

### Uma estátua de bancada não é de ninguém

Ela sai da bancada — `sks / " s " / s s`, um lingote de koboldite e cinco pedras — **pelada**, sem dono, e
assim não faz absolutamente nada. Para lhe dar uma cara é preciso o **Rito de Prender a Estátua**: ela
entra no círculo com uma flor de beladona, uma papoila e um dente-de-leão, mais quatro mil de poder, e sai
com o nome e o número de quem o fez.

É de propósito que seja assim, e diz o que a coisa é: ela não é uma máquina de encher infusões, é um
**ídolo**. Os goblins não adoram a estátua — adoram **você**.

E, posta, ela guarda o dono que vinha no item, e **não** quem a pôs. Uma estátua presa a alguém, roubada e
posta por outro, continua enchendo a infusão do primeiro. Partida, ela leva o dono consigo.

O item leva o nome do dono atrás do seu — `Estátua de Adoração (Fulano)` —, que é o `ClassItemBlock` do
original e uma ideia boa: sem isso, quatro estátuas num baú são quatro itens iguais.

### Desvios declarados

1. **A pele vem do número, e não do nome.** Em 2014 bastava o nome de quem jogava para ir buscar uma pele;
   hoje é preciso o número, e o servidor de peles não responde a quem joga sozinho sem rede. Não
   respondendo, fica a pele de sempre — que é o que o original também fazia quando o download falhava.

2. **O braço e a perna esquerdos são os direitos ao espelho.** É o formato de pele de 2014, que o original
   usa e que o porte mantém para a folha de pedra encaixar. Com uma pele de hoje nota-se: a manga esquerda
   da estátua é a direita virada.

3. **Os deuses goblins entram na fatia seguinte a esta** — e entraram: veja «Os deuses goblins: o Mog e
   o Gulg», logo abaixo. A Estrela do Nether na estátua chama-os, e com quinze adoradores eles vêm
   sozinhos de vez em quando.

**Guardas:** o `OccultaStatueGameTest`, com sete — os números do original; a estátua de bancada que **não é
de ninguém** e nem conta; o rito que lhe dá uma cara; o **dono que atravessa o item**, partida e posta
outra vez; os **três degraus**, que é a prova que carrega a fatia e vai até conferir que a Adoração II
destrava o terceiro grau dos símbolos; o teto, que ela nunca passa; e a ordem de contar antes de chamar. E o `OccultaStatueClientTest`, com duas telas: a estátua com goblins
ajoelhados à volta e a estátua na grade de quem a tem.

## Os deuses goblins: o Mog e o Gulg (2026-10-05)

Dois chefes de **quatrocentos de vida** cada, e a graça deles não está em nenhum dos dois: está na
**distância entre eles**.

### A conta que é a luta toda

| distância um do outro | quanto do dano passa | o murro do Gulg |
| --- | --- | --- |
| **três blocos ou menos** | **nada** — os dois são invencíveis | 6 + d20, e um bloco de voo |
| até seis | um quinto | 6 + d15, oito décimos |
| até nove | metade | 6 + d10, meio bloco |
| até dezesseis | quatro quintos | 6 + d6, dois décimos |
| mais longe | tudo | 6 + d4, e nada de voo |

E, por cima de tudo isso, **nunca mais de quinze por golpe**. Um diamante encantado na cara de um deus
goblin vale o mesmo que uma pedra.

Leia a tabela duas vezes e veja o que ela diz: a **mesma distância** que os torna invencíveis torna o Gulg
um martelo. Juntos, são uma parede que mata; separados, são dois bichos grandes que se matam. A luta inteira
é **sobre separá-los** — e não há nisto um só poder novo, só uma conta de distância escrita duas vezes com
o sinal trocado. É o melhor desenho de chefe que o Witchery tem.

### E os dois trabalham contra isso

- o **Gulg** tem uma vontade própria que o leva de volta para **seis blocos** do Mog, de até sessenta e
  quatro de distância. Afastá-lo uma vez não basta: é preciso **mantê-lo** afastado;
- o **Mog** fica longe e atira. Preso sem caminho até quem o persegue, de cinco em cinco segundos ele
  **salta dezesseis blocos para trás dele**, como um enderman — e é o que impede que se o mate de cima de
  uma torre.

E os dois **se curam**: um de vida por segundo, sempre. Quem não os separa não os mata: fica ali batendo até
acabar a comida.

### O que cada um é

O **Mog** é o arqueiro. Cinco de armadura, um arco na mão, e uma assinatura: a flecha dele sai com uma e
meia vezes a velocidade de sempre — e com **duas e meia** contra quem estiver **no ar**. Saltar à frente do
Mog é uma má ideia. Perdendo o arco, ele arranja outro: uma vez em cem batidas, do nada, com um estalo.

O **Gulg** é o murro. Oito de armadura, resistência a empurrões **um** — a máxima —, braços de dezesseis em
vez de catorze e um peito de dez por oito por seis. Um barril com punhos.

Nenhum dos dois caça goblins, nem o outro. Um deus goblin que caçasse goblins não seria um deus goblin.

### Como eles chegam

Pela **Estátua de Adoração**, de duas maneiras:

1. uma **Estrela do Nether** na mão, clicada na estátua, com o **dono** dela e **cinco adoradores**. A
   estrela some — e com ela somem **cinco goblins**, que o original mata com dano mágico. É a única vez em
   todo o mod em que uma coisa boa se paga com a vida de quem a adorava: você não pede os deuses, você **os
   compra**;
2. ou sozinhos, com **quinze adoradores**, uma vez em mil pulsos — que são umas quatro horas de jogo com a
   estátua cheia. O original chama a isto uma *chance*; quem já estava ali lhe chama outra coisa.

Eles vêm sempre **aos pares**, e não vêm se já houver um deles por perto. E acordam com **cento e cinquenta
batidas de invencibilidade**, curando vinte por décimo de segundo, que é o despertar do Wither.

### O que eles largam

Uma a três **pepitas de koboldite** e uma peça de **malha encantada ao nível trinta**. Fecha-se o círculo: o
metal que só sai de um goblin sai, em dobro, dos deuses deles.

### Desvios declarados

1. **O arco do Mog não cai.** No original ele cai e some em cinco segundos, que é o jeito de 2014 de dizer
   «este arco não é seu». Hoje o tempo de vida de um item largado não se mexe de fora, e a **chance de
   queda zero** diz a mesma coisa sem rodeios.

2. **A Aljava do Mog e a Cinta do Gulg entram na fatia seguinte a esta** — e entraram: veja «A roupa de
   goblin», logo abaixo. Metade das vezes, cada deus larga a sua.

3. **A chance de eles virem sozinhos é a do ajuste padrão do original**, que são dez em cem multiplicados
   pelo centésimo que ele escreve à mão. O original deixa mexer nisso num arquivo de ajustes; aqui é um
   número.

**Guardas:** o `OccultaGoblinGodsGameTest`, com seis — os números do original; a **conta da distância**,
que é a prova que carrega a fatia; o murro do Gulg, que é o espelho dela; os dois no mundo, colados e
afastados, com o teto de quinze; os deuses que não caçam os seus; e a Estrela do Nether, que os chama e come
cinco adoradores. E o `OccultaGoblinGodsClientTest`, com a tela dos dois lado a lado e um goblin comum entre
eles para a escala.

## A roupa de goblin (2026-10-05)

Três peças, e uma ideia que fecha o que os deuses começaram.

### As três

| peça | onde | o que faz |
| --- | --- | --- |
| **Fita Torcida** | cabeça | quem **estiver olhando** para quem a tem, a dezesseis blocos, fica **enjoado** cinco segundos — e, se for gente, é **virado ao contrário** na hora. Um bicho fica **fraco** em vez disso: não tem tela para lhe virar |
| **Aljava do Mog** | peito | o arco dispara **sem flecha**, e a flecha que sai faz **três vezes** o dano em quem estiver **no ar** e deixa **Fraqueza** dez segundos |
| **Cinta do Gulg** | pernas | o **murro de mão vazia** faz **cinco** de dano — valor fixo, nem mais nem menos — e atira quem apanha **um bloco para cima** |

A Fita é a única que se fabrica: `iii / iai`, quatro lingotes de koboldite e uma **Pedra Afinada
carregada**. As outras duas **não se fazem**: caem dos deuses goblins, metade das vezes cada uma.

### E a ideia

**Dois jogadores a oito blocos** um do outro, um com a Aljava e outro com a Cinta, ganham os dois
**Resistência II**.

É a **mesma conta de distância** que faz o Mog e o Gulg invencíveis — a tabela de três, seis, nove e
dezesseis blocos —, virada para quem joga. O Mog e o Gulg eram fortes juntos; as roupas deles continuam a
sê-lo.

Repare no que isso quer dizer de desenho: é o único par de peças do mod inteiro que **só vale a dois**. Uma
pessoa com as duas vestidas **não ganha nada** com isso — o original confere expressamente que o outro não é
você. Para o par valer, é preciso matar os **dois** deuses, ter sorte nas **duas** quedas, e ter com quem
jogar.

### O cone de quem olha

A Fita usa o cone do enderman, com a folga do original: `produto > 1 − 0,025 / distância`. Ele **aperta com
a distância** — de longe é preciso olhar bem certo, de perto basta ter a pessoa à frente — e exige linha de
vista.

E não pega em quem traz uma **abóbora na cabeça**, que é o original dizendo que quem não vê não se
desnorteia.

### O desenho

Um bípede comum com **quatro caixas a mais** presas ao tronco — a **aljava** e as **três flechas** dentro
dela —, tombadas vinte graus. São elas que fazem a silhueta: de costas, quem tem a Aljava do Mog vê-se de
longe. As folgas são as do original: o peito com `0,61` e as pernas com zero.

São **três folhas**, uma por casa. O original tem uma quarta de cada, para a tinta, e não a usa: estas
peças não se pintam.

### Desvios declarados

1. **As flechas sem fim entram por outra porta.** O original cancela o disparo do jogo e atira uma flecha
   sua, de graça, pelo `ArrowLooseEvent` do Forge. Aqui, quando o arco procura flecha e não acha nenhuma, a
   Aljava **lhe dá uma** que não está na mochila de ninguém: o jogo a atira e gasta uma pilha que não
   existe. O efeito é o mesmo, e o caminho é mais curto.

2. **A flecha não se apanha do chão**, como no original — lá por o tipo de apanha ser dois, aqui por ela
   nascer marcada. Sem isso, a Aljava não seria flechas sem fim: seria uma **fábrica de flechas**.

3. **A marca da flecha é um apego, e não uma etiqueta.** O `WITCMogged` do original é uma etiqueta no
   bicho; aqui é um apego persistente, que é o que lhe corresponde hoje.

4. **O virar de quem olha** é feito com o teleporte do servidor, que é o que o pacote de posição do original
   fazia à mão.

**Guardas:** o `OccultaGoblinClothesGameTest`, com cinco — os números do original; o **par**, que é a prova
que carrega a fatia e confere que uma pessoa com as duas peças não ganha nada; a Fita, que só pega em quem
está olhando e não pega em quem traz abóbora; o murro da Cinta, que só vale de mão vazia; e a flecha da
Aljava, que vale o triplo no ar e deixa Fraqueza sempre. E o `OccultaGoblinClothesClientTest`, com duas
telas: dois manequins, um de frente e um de costas — e é o de costas que importa, porque é onde a aljava se
vê — e as três peças no cinto.

## Mais dez símbolos, e as três imperdoáveis (2026-10-05)

De catorze para **vinte e quatro** dos trinta e um. E com eles entra a única **porta trancada** da tabela.

### As três imperdoáveis

| símbolo | o que faz | custa |
| --- | --- | --- |
| **Avada Kedavra** | **mata na hora** quem for gente — e só onde houver briga entre jogadores. Em bicho: **duzentos** no que pode ser escravizado, numa bruxa, num Ent ou num golem de até duzentos de vida; **vinte e cinco** no resto | **101** |
| **Crucio** | dói, e mais nada: **quatro mais quatro por grau** em gente, quatro em bicho | 5 |
| **Imperio** | **escraviza** o que a bola acertar, que é o mesmo escravizar da Infusão Infernal | 10 |

As três **só se lançam com a Infusão Infernal no corpo**. É a única coisa de todo o ramo dos símbolos que
pede uma infusão em particular, e o original é explícito sobre o que a define: imperdoável é a maldição que
**não tem apontamento no livro** — o que não se pode aprender, só se pode tomar.

E repare no preço do Avada Kedavra: **cento e um**, que é mais do que o cantil inteiro de uma infusão
recém-feita, que são cem. Não é um feitiço que se use: é um que se junta para usar **uma vez**.

### As duas maldições que não são imperdoáveis

| símbolo | o que faz | custa |
| --- | --- | --- |
| **Carnosa Diem** | lançado **em si próprio**: tira um décimo da vida e devolve **dez de carga de infusão** | 1 |
| **Ignianima** | queima tudo a um bloco e meio — e **dói mais quanto pior** estiver quem o lança | 2 |

O Ignianima é a melhor conta do mod inteiro. Com a vida cheia são **dois** de dano; acima de quinze, três;
acima de dez, cinco; e abaixo disso, **seis mais metade do que falta**. Quem o lança sabendo disso lança-o
sangrando.

E em gente o dano é ainda **multiplicado pela vida máxima dela sobre vinte** — de modo que um jogador com
coração reforçado apanha **mais**, e não menos. É o avesso do que toda a gente espera de uma armadura de
vida.

O Carnosa Diem é a única coisa do mod que **troca vida por poder sem passar por ninguém**, e é por isso que
é maldição sem ser imperdoável: não faz mal a mais ninguém.

### Os cinco que sobram

| símbolo | o que faz | custa |
| --- | --- | --- |
| **Attraho** | o avesso do Flipendo: **puxa** o que for vivo para quem o lançou — dois blocos, três, **nove** | 1 |
| **Cave Inimicum** | **endurece** o que é mole: terra, grama, micélio, pedregulho e tábua viram **pedra**; tijolo de pedra vira **tijolo**; areia vira **arenito**; argila vira **terracota**; e uma **porta de madeira** vira uma de **ferro** | 1 |
| **Defodio** | **cava**: o que for terra, argila, areia, neve, gelo ou pedra desaparece e cai em item | 3 |
| **Flagrate** | risca um **glifo infernal** na parede para onde se olha | 1 |
| **Meteolojinx Recanto** | **para a chuva** | **100** |

O Cave Inimicum e o Defodio são os dois que mexem em bloco, e os dois num **quadrado da face**: um bloco no
grau um, três por três no dois, cinco por cinco no três — desenhado **no plano da face** em que a bola
bateu, de modo que acertar no chão pega um tapete e acertar numa parede pega um painel.

E o Meteolojinx Recanto é o segundo preço mais alto do mod, atrás do Avada Kedavra, e não há nele poder
nenhum: ele só muda o tempo. É o original dizendo o que vale um dia de sol.

### Desvios declarados

1. **O que destranca quatro deles não entra.** ~~O original tranca o Ignianima, o Carnosa Diem e o
   Morsmordre atrás de uma entrada no livro de bruxaria.~~ **Corrigido na fatia das roupas de bruxa,
   depois de se ler o original outra vez:** não é um livro, é um **gole**. O Carnosa Diem, o Ignianima, o
   Morsmordre e o Tormentum têm cada um a sua <b>chave de saber</b>, e quem a dá é um **Cozimento de
   Alma** — da Fome, da Angústia, do Medo e do Tormento. Bebe-se, e o feitiço fica sabido para sempre.

   Os três primeiros caem do **Diabrete** e o quarto do **Senhor do Tormento**, e nenhum dos dois está
   portado — de modo que a tranca fica de fora na mesma, mas pelo motivo certo. O que **não** tem chave
   nenhuma é que é *imperdoável*: o Avada Kedavra, o Crucio e o Imperio não se aprendem, e é isso que os
   separa das outras maldições.

2. **A briga entre jogadores é a de hoje.** O original pergunta ao servidor se o PvP está ligado; aqui
   pergunta-se ao próprio jogador, com o `canHarmPlayer` do jogo, que responde a mesma coisa e ainda conta
   com equipes.

3. **O dano demoníaco é o dano mágico.** O `DemonicDamageSource` do original é um tipo próprio só para
   contornar armadura; o porte usa o dano mágico do jogo, que faz o mesmo.

4. **O que o Defodio cava é um rótulo.** O original lista sete *materiais* de 2014 — argila, neve, terra,
   grama, gelo, pedra e areia —, e o material «rock» de então era largo demais para se escrever todo. Aqui
   é o rótulo `thaumcraft:defodio`, com as pedras do mundo e do Nether, que é o que ele queria dizer.

**Guardas:** no `OccultaSymbolGameTest`, mais três — as **três imperdoáveis**, que não servem à Infusão da
Luz e servem à Infernal, com as duas maldições comuns a servirem a qualquer uma; a escada do **Ignianima**,
conferida nas quatro bandas dela; e os **desenhos** dos novos, traço por traço. E o
`OccultaSymbolClientTest` passou de quatro bolas a **oito**, para as cores e os tamanhos das maldições se
verem ao lado dos dos feitiços comuns.

## A Tina de Prata (2026-10-05)

Uma bacia de ferro que se encosta a uma fornalha e **apanha o que escorre**.

### O que ela faz

De segundo em segundo ela olha os quatro lados. Numa máquina que tenha uma **casa de saída** com
**lingotes de ouro**, se a pilha estiver maior do que da última vez que ela olhou, há **uma chance em
cinco** de aparecer um **pó de prata** dentro dela.

Repare no que ela olha: não é o ouro **que está lá**, é o ouro **que apareceu**. Uma fornalha cheia de ouro
parada não lhe dá nada; uma fornalha que acabou de fundir mais um lingote, sim. O original não explica a
física e não precisa — o que ele diz é que **fundir ouro suja alguma coisa**, e a tina é onde a sujeira
assenta.

E ela sabe distinguir o ouro que entrou do ouro que saiu porque só olha para a prateleira de onde se pode
**tirar** e na qual não se pode **pôr**. É a casa de resultado de uma fornalha, e nada mais.

Não tem tela, não tem botão e não tem receita de dentro: **clica-se nela e tira-se o que lá está**. E é de
longe o jeito mais barato de arranjar prata no ofício — o outro é matar quem a traz.

### O corpo dela conta o estado

É o melhor exemplo do mod de um bloco que **se explica pela forma**, e não por um número numa tela:

- **os bicos**: de cada lado em que houver uma máquina — uma coisa com alma —, ela mostra um bico virado
  para ela. Uma tina entre duas fornalhas tem dois bicos; uma tina no meio do campo não tem nenhum;
- **as camadas**: o pó lá dentro sobe em **oito pedacinhos**, um por cada oito pós. De fora vê-se quanto
  ela já juntou.

São vinte e uma caixas, portadas número por número, numa folha de sessenta e quatro por trinta e dois.

### Desvio declarado

**O relógio é dela, e não da fornalha.** O original é avisado pelo Forge sempre que a alma de um vizinho
muda, e olha só nessa hora. O jogo de hoje não tem esse aviso; aqui ela olha sozinha, **uma vez por
segundo**. O que se vê é o mesmo — a pilha que cresce dá pó —, só que a conta é feita por relógio e não
por sobressalto.

**Guardas:** o `OccultaSilverVatGameTest`, com três — os números do original; a tina que nasce vazia,
conta as camadas e devolve o que tem quando se clica nela; e o **ouro que cresce**, que é a prova que
carrega a fatia: vinte voltas com a pilha parada não dão nada, e sessenta com ela crescendo dão. E o
`OccultaSilverVatClientTest`, com três tinas lado a lado — uma sozinha, uma com fornalhas ao lado e uma
cheia.

## Os três fantasmas do Braseiro (2026-10-08)

O **Espectro**, a **Banshee** e o **Poltergeist** — e, com eles, as **quatro receitas do Braseiro que
faltavam**. O bloco fica com as oito do original, e é a primeira máquina do ofício a ficar completa.

### O toque, que é a ideia inteira

Os três saem de uma fogueira que arde meio minuto e são, os três, variações de uma ideia só, escrita num
método de vinte linhas chamado `touchOfDeath`: **o dano deles não é um número**.

O Espectro leva **quinze por cento da vida máxima** de quem toca. A Banshee leva **dez**, de tudo o que
estiver a seis blocos. E os dois levam isso **por fora da armadura**, por fora da Resistência, por fora de
tudo.

Leia o que isso faz com o jogo. Contra o Espectro, uma couraça de netherita vale **exatamente o mesmo que
nada** — a prova do `OccultaGhostGameTest` põe dois zumbis lado a lado, um pelado e um vestido da cabeça aos
pés, e os dois perdem três de vinte. E **vida a mais é pior**: um golem de ferro, com cem de vida, perde
quinze por toque, enquanto o zumbi perde três. Sete toques e os dois morrem igual. Não há nada no jogo que
se possa fazer para ser mais resistente a um Espectro.

### E do outro lado: quinze, e nunca mais

O contrário também é verdade, e é o que torna os três o que são: **nenhum golpe lhes tira mais de quinze**.
Não há espada que os mate depressa, não há poção que ajude, não há queda que resolva. Quarenta de vida com
teto de quinze são **três golpes no mínimo**, com o que quer que seja.

Isso muda o que eles são. Não são bichos que se matam — são bichos de que se **foge**. E é por isso que o
jogo os gasta de outra maneira: o fetiche, que vem a seguir, **consome** espíritos em vez de os matar.

### A Banshee, e a melhor piada do mod

Ela **não bate em ninguém**: o dano de ataque dela é zero, escrito assim no original. O que ela faz é
gritar, de cinco em cinco segundos, e enquanto houver alguém no alcance ela continua gritando uma vez por
segundo. Um décimo da vida por segundo, por fora de tudo, a seis blocos.

Correr seis blocos é tudo o que é preciso — e, com a vida caindo um décimo por segundo, é tudo o que dá
tempo de fazer.

Há uma saída, e é a melhor piada que o Witchery tem: **quem traz abafadores de orelhas não ouve**. O grito
que fura armadura de netherita não fura duas almofadas de couro e lã nas orelhas.

### O Poltergeist, que não briga

Três de ataque, vinte de vida, **invisível para sempre** — ele nasce com a poção, e ela não acaba. O que
ele faz não é brigar: é **bagunçar**. De cinco em cinco segundos ele procura uma coisa para estragar, e a
ordem importa:

1. um **quadro** ou moldura de item a dezesseis blocos: chegando perto, parte-o;
2. senão, com **quem o chamou** a oito blocos, o **baú mais perto** que tenha alguma coisa dentro: chegando
   perto, **atira uma coisa para fora dele**;
3. senão, qualquer **item largado** no chão: chuta-o.

Repare na segunda. Ele só mexe nos baús **enquanto quem o chamou está por perto**. Não é um ladrão — é uma
assombração doméstica, e ela precisa de plateia. (O caldeirão e o braseiro ficam de fora da lista de baús, e
é o original sendo bonzinho: são as duas coisas em que se perde uma receita inteira por um item a menos.)

E ele é o preço de chamar os outros: **cada fantasma que sai do braseiro tem cinco por cento de chance de
trazer um Poltergeist atrás**, a seis ou dez blocos. Chamar os mortos às vezes chama o que não se pediu — e
aquele fica.

### As quatro receitas que faltavam

| Receita | As três coisas | Arde | Altar? |
|---|---|---|---|
| Chamar Espectro | losna, lã de morcego, pó de cemitério | 30 s | sim |
| Chamar Banshee | losna, medo condensado, pó de cemitério | 30 s | sim |
| Chamar Poltergeist | losna, mal refinado, vontade concentrada | 45 s | **não** |
| Drenar o Crescimento | medo condensado, maçã bichada, pó de cemitério | 1 min | **não** |

As três primeiras não fazem nada enquanto ardem — só faíscam de cinco em cinco segundos — e **fazem tudo
quando acabam**. É a maneira certa de a coisa se sentir: não dá para desistir no meio.

Para isso o `BrazierRecipes.Recipe` ganhou um segundo gancho, o `burnt`, ao lado do `burning` que já tinha.

**O Drenar o Crescimento** é a oitava, e a mais estranha das oito. De cinco em cinco tiques ela aponta um
lugar ao acaso num quadrado de sete por sete em volta — e a altura dele **varre de baixo para cima**, de
dois abaixo do braseiro a três acima, num ciclo de trinta tiques. Se o que estiver ali for uma plantação
crescida, ela **desfaz um passo do crescimento dela** e, com isso, **cura um décimo da vida** a cada
morto-vivo a três blocos.

E então ela **guarda dois**, que são **oitocentos tiques a mais de fogueira**. É a única das oito que se
alimenta do que faz: enquanto houver trigo em volta, ela não acaba. O braseiro ganhou o campo que guarda
isso.

### Desvios declarados

1. **O que o toque faz é dano.** O original põe a vida mais baixa **à mão** e depois avisa o jogo de que
   houve dano, com zero de dano, só para o grito e a animação saírem. Aqui é um **tipo de dano próprio**,
   o `thaumcraft:touch_of_death`, posto nos três rótulos que o jogo já tem — «não passa pela armadura»,
   «não passa pelos efeitos», «não passa pelos encantamentos». O efeito é o mesmo e o caminho é honesto: o
   dano é dano, e não uma subtração escondida. O que se perde com isso é a **absorção**, que o original
   também contornava e o jogo de hoje não deixa contornar por rótulo.

2. **A armadura do Espectro é a propriedade, não um método.** O original escreve
   `getTotalArmorValue() + 2`, com teto em vinte; aqui são dois na base da propriedade de armadura, que o
   jogo soma à das peças vestidas. O teto não vem porque nunca se chega lá: ele só apanha capacete e peito,
   e nem sempre.

3. **O toque do Espectro não soma encantamentos.** O original lhe soma a Afiação, a Repulsão e o Fogo da
   arma dele. Mas ele **nunca tem arma** — as peças que ele apanha ao nascer são só capacete e peito —, de
   modo que as três somas são sempre zero.

4. **O braço levantado do Poltergeist sobe liso.** O original escreve `vaiEVem(i - par4, 15)`, e o `par4`
   ali é o **giro da cabeça em graus** — não a fração de tique, que era o que a conta do golem de ferro, de
   onde ele a copiou, tinha nesse lugar. No original o braço levantado tremia conforme o bicho virava a
   cabeça. Aqui desconta-se a fração de tique, que é o que a conta quer.

5. **O que o braseiro guardou acaba com a fogueira que o guardou.** O original **nunca** zera o que
   guardou, e com isso toda receita posta naquele braseiro depois de um Drenar arde oitocentos tiques a
   mais por planta secada, para sempre. Isso não é desenho, é esquecimento.

6. **O baú mais perto procura-se pelas fatias.** O original varre a lista de almas carregadas do mundo
   inteiro, que o jogo de hoje não dá. Varrer trinta e três blocos ao cubo seriam trinta e cinco mil
   perguntas de cinco em cinco segundos; aqui pergunta-se às fatias em volta, que é onde as almas moram.

7. **O que o Drenar seca é a `CropBlock`.** O original pergunta por `IPlantable` e pelo tipo de planta ser
   `Crop`; hoje a `CropBlock` é exatamente esse conjunto — o trigo, as cenouras, as batatas, a beterraba —
   e as plantas do ofício herdam dela.

8. **Os nomes das quatro receitas antigas foram corrigidos.** Elas estavam com nomes inventados na fatia do
   Braseiro — «Sinal de Fumaça», «Fogo da Força» — e passam a ser os do original: **Névoa de Cemitério**,
   **Angústia dos Mortos**, **Fortificação do Cadáver** e **Véu Mortal**. E o Chamar Poltergeist ganhou
   nome, que no original **não tem**: a chave dele está no código e não no arquivo de textos, de modo que
   no jogo de 2014 ele aparece sem tradução.

### Os dois bonecos

O `ModelSpectre` serve o Espectro **e** a Banshee, e a diferença entre eles é uma bandeira: com ela, os
braços ficam estendidos para a frente — é o Espectro vindo buscar alguém; sem ela, caídos — é a Banshee, que
não precisa de mãos. E a **boca** é uma caixa sem fundura presa à cabeça, que só aparece quando o bicho está
gritando; na Banshee ela abre e os braços se levantam de lado ao mesmo tempo.

O `ModelPoltergeist` tem **quatro braços** de dois por dezoito. Os de dentro andam ao passo e os de fora a
metade dele, de modo que os quatro nunca estão na mesma posição — é isso que o faz parecer que tem mais
braços do que tem.

As transparências são as do original e dizem o que cada um é: o Espectro a **quinze centésimos** enquanto
está apagado, que é como ele nasce, e a **seis décimos** depois; a Banshee a **sete décimos**, porque ela
não se esconde; e o Poltergeist a **quatro décimos** — e além disso invisível por poção, de modo que só se
vê o que ele faz, nunca ele.

**Guardas:** o `OccultaGhostGameTest`, com onze — os números dos três; o **toque que ignora a armadura**,
que é a prova que carrega a fatia; a vida a mais que é pior; o toque que não pega no invulnerável; o teto de
quinze; o grito a seis blocos e não a dez; os **abafadores**; a Banshee que não bate; o Poltergeist que
nasce invisível, que esvazia baús **só com o dono por perto** e que chuta o que está no chão; as oito
receitas do braseiro; a fogueira que deixa um fantasma atrás; e o Drenar, que seca uma planta e cura um
zumbi com o que lhe tirou. E o `OccultaGhostClientTest`, com os cinco jeitos que os três têm, em fila.

## O Espírito (2026-10-08)

Uma lanterna de papel do tamanho de um punho que **deriva** pelo mundo dos sonhos. Quatro de vida, e nada
nela magoa ninguém — o ataque de quatro que ela tem no papel nunca sai, porque não há meta que lhe mande
bater.

**É moeda.** Os cinco efeitos de fetiche custam **três espíritos cada**, e não há outra maneira de os
conseguir. Quem quiser um Espantalho que grite tem de atravessar e trazer três destes de lá. É por isso que
ele vem antes dos fetiches, e não depois.

### Ele deriva, e isso não é um detalhe

As quatro metas de voo do Witchery — vaguear, pousar, ir para casa e a tentação — são a mesma ideia escrita
quatro vezes, e vale dizer qual é, porque ela não é a do jogo de hoje: **nenhuma delas acha caminho**. Elas
não pedem à navegação que leve o bicho a um lugar. Elas **empurram a velocidade dele** um bocadinho por vez
na direção que querem, e antes de empurrar perguntam se a **linha reta** até lá está livre, passando a caixa
de choque pelo caminho de metro em metro.

Por isso um espírito se mexe como se mexe: ele não contorna paredes, não sobe escadas, não desiste — bate,
perde o rumo, escolhe outro ponto ao acaso e volta a derivar. Trocar isso pela navegação voadora do jogo de
hoje daria um bicho que anda bem e **não se parece nada com ele**.

As quatro estão no `FlyerGoals`, velocidade a velocidade, e ficam para os voadores que vierem.

### Não se doma, e vem ver

Clicar nele não faz nada — o original devolve «não» ao toque, e é de propósito. O que o traz perto é a
**Vontade Concentrada** na mão: ele deriva até ela e sobe um bocadinho para a cheirar. E se quem a segura
**se mexer**, ele perde o interesse por cem batidas.

Não é um bicho que se segue. É um bicho que se espera.

### Os dois itens, e a bússola que não se gasta

O **Espírito Dominado**, usado num bloco, solta um espírito que **fica**. O **Espírito Dominado da Aldeia**
solta um que tem **dez segundos de vida** e o rumo da **aldeia mais perto** — e, quando o prazo acaba, some
num estouro e **devolve o item**.

É a melhor ideia pequena do Witchery: uma bússola de aldeia feita de fantasma, que aponta o caminho e se
devolve a quem for atrás dela. Quem a largar e não a seguir, perde-a; quem a seguir, fica com ela outra vez.

E há um terceiro feitio, que não larga nada: é o que a **Pedra de Caminho** usa.

### E com isso cai a lacuna declarada da Pedra de Caminho

A fatia da Pedra de Caminho deixou um ramo de fora, escrito lá: a **Pedra Sintonizada** ou o **Espírito
Dominado** largados num **anel miúdo de giz de Ritual** chamam um espírito. Ficava de fora porque pedia este
bicho. Agora está.

Repare no giz, porque é o que separa as duas coisas: os três ramos da Pedra de Caminho pedem o giz **do
Alhures**, e este pede o **de Ritual**. Não é descuido do original — é a geometria que move coisas contra a
geometria que chama coisas.

### Onde ele nasce

O original põe-no na lista de nascimentos dos **nove tipos de bioma de terra** do mundo de cima, com peso
um e de dois a cinco de cada vez, e depois **o recusa em todo lugar que não seja o mundo dos sonhos**, numa
linha do próprio bicho. São os dois lados da mesma conta, e é por isso que a lista é larga: a tranca não
está nela.

Depois disso: acima de sessenta, em grama ou areia, com mais de oito de luz, e uma chance em dez. É um bicho
de campo aberto e de dia — do outro lado.

### Desvios declarados

1. **A lista de biomas é «o mundo de cima» inteiro**, em vez dos nove tipos nomeados. Inclui o mar, e o mar
   não o dá nunca: ele pede grama ou areia por baixo.

2. **O pó dele é o pó colorido do jogo.** O original tem um seu, o `NaturePowerFX`: um ponto parado de um
   décimo de bloco que corre oito quadros de uma folha e some em dez batidas — e que não se mexe, porque
   nasce com o andar desligado e a gravidade que lhe passam fica guardada sem nunca ser usada. Aqui é o pó
   colorido do jogo, que é o que este mod já usa em todo lugar onde o original pedia aquele.

3. **A aldeia se procura com a procura de estruturas.** O original vai buscar o gerador de aldeias do mundo
   **por reflexão**, com três nomes alternativos para o campo, e lhe pergunta onde está a mais perto. O jogo
   de hoje responde à mesma pergunta sem reflexão nenhuma.

4. **A meta de seguir o dono é a do jogo**, e não a voadora do original. Ela **não corre nunca**, porque o
   Espírito não se doma — está lá porque está lá, e é a marca de um bicho que o Witchery pensou em domar e
   depois não domou.

5. **A meta de ir para casa é só a forma dela que não carrega nada.** O original tem três — sem carga, com
   item na mão e com bicho às costas — e as outras duas são da vassoura e da coruja, que entregam coisas. O
   Espírito usa a primeira.

6. **Sem gravidade, em vez de um andar reescrito.** O original reescreve o método de andar inteiro, e o que
   sobra, no ar, é o atrito de nove décimos e **nenhuma queda**. Dizer isso ao jogo de hoje é uma linha.

**Guardas:** o `OccultaSpiritEntityGameTest`, com nove — os números dele; a Vontade Concentrada e só ela; o
toque que não o doma; o **nascimento que não acontece fora do outro lado**, conferido quarenta vezes; o
Espírito Dominado que solta um que fica; o **da Aldeia que devolve o item**, que é a prova que carrega a
fatia; o feitio da Pedra de Caminho, que não devolve nada; os dois itens que sabem qual deles é qual; o anel
miúdo de giz de Ritual, que fecha a lacuna da fatia da Pedra de Caminho; e a **linha reta**, conferida com
uma parede no meio, que é a conta que as quatro metas de voo partilham. E o
`OccultaSpiritEntityClientTest`, com quatro em fila — um dourado e três pintados —, porque a cor é do pó e o
pó é o que de verdade se vê dele.

## Os fetiches (2026-10-08)

O **Espantalho**, a **Escada de Bruxa** e o **Ídolo de Treant** — três blocos com uma alma só, e a razão
por que o Espírito e os três fantasmas vieram antes deles.

### Um fetiche vazio é um espantalho de palha

Nenhum dos três faz nada ao ser posto. O que os torna uma coisa é um **efeito** preso a eles por um rito,
e o que o rito come são **espíritos** — de quatro espécies, em contas diferentes:

| Efeito | Espíritos | Espectros | Banshees | Poltergeists |
|---|---|---|---|---|
| Proteção de Vodu | 3 | 1 | 1 | 1 |
| Sentinela | 3 | 3 | 0 | 0 |
| Grito | 3 | 0 | 2 | 0 |
| Desorientação | 3 | 0 | 0 | 2 |
| Caminhar Fantasma | 3 | 1 | 1 | 0 |
| *(Morte)* | 0 | 5 | 5 | 5 |

Repare na coluna dos espíritos: ela é **três em todas**. Não há efeito de fetiche que se consiga sem três
idas ao outro lado, e é isso que faz do Espírito a moeda do ramo.

### Quem cabe primeiro

É a melhor regra da fatia, e vale dizê-la em voz alta: **o rito não escolhe o efeito**. Ele pega no
**primeiro da lista cuja conta couber** no que estiver dentro do círculo, e gasta exatamente o que esse
pede.

Quem quiser a Sentinela e levar três espectros **e** duas banshees leva, em vez dela, a **Proteção de
Vodu**, que é a primeira e pede menos de cada. Isso faz da ordem da lista uma regra do jogo: **para ter o
que se quer, leva-se o que ele pede e não mais**.

E os bichos **se gastam**: somem, um a um, com um pó de portal e um estalo. Não morrem — não largam nada,
não dão experiência, não contam para nada.

### Os cinco, um a um

- **Proteção de Vodu** não faz nada por si. Quem o lê são as **bonecas**: o rito que come as bonecas de
  proteção uma a uma passa a comer **uma só** se a vítima estiver a dezesseis blocos de um fetiche com
  este efeito. É o único dos cinco que funciona sem o dono saber que está funcionando.
- **Sentinela**: por cada um que o alarme ache, nasce um **Espectro** a um bloco dele, já marcado e com
  trinta segundos de vida. E são **dois** se houver um só — o espantalho que acha um intruso manda dois
  contra ele, e o que acha cinco manda um a cada.
- **Grito**: o fetiche grita e **manda redstone** enquanto o alarme está levantado. O mais longe de todos:
  dezesseis blocos, o dobro dos outros. Na Escada de Bruxa ele grita **calado** — uma escada de penas não
  tem boca.
- **Desorientação**: quem chegar **armado ou vestido** e **olhando para o fetiche**, dentro de um arco de
  quarenta e cinco graus, é **virado ao contrário**. Quem passar de lado não é tocado; quem vier ver o que
  é, perde-se. E os bichos de menos de cinquenta de vida largam o alvo e apanham outro.
- **Caminhar Fantasma**: a quem andar em espírito por perto, o fetiche **salta a próxima perda de
  manifestação**. O mais quieto dos cinco e o mais útil de todos.

### O alarme, que dispara pela ausência

De segundo em segundo, com um efeito que procure alguma coisa, a alma olha em volta. Há **seis modos**,
que se rodam com a **Boline**:

| Modo | Levanta quando |
|---|---|
| 0 | há **gente** que não está na lista |
| 1 | há **gente** que está na lista |
| 2 | há **o que for** que não está na lista |
| 3 | **nem todos** os conhecidos estão presentes |
| 4 | **nenhum** dos conhecidos está presente |
| 5 | nunca — e é como ele nasce |

Os modos três e quatro são o que fazem do Espantalho uma coisa diferente de um alarme. Eles disparam pela
**ausência**: um espantalho que conhece as suas vacas e avisa quando falta uma é a melhor ideia do bloco,
e está em duas linhas.

As listas se escrevem com o **Kit de Taglock** e se apagam com um **balde**. As espécies **agrupáveis** —
aldeão, goblin, ovelha, vaca, cogumelo, galinha, porco, cavalo, morcego, lula e bruxa de coven — entram
por **nome de espécie**, e todo o resto por **nome próprio**: é o que separa «as minhas vacas» de «aquela
vaca».

E ele **nunca** vê cadáveres, ilusões, espíritos nem familiares. Um fetiche não se assusta com o que a
bruxa pôs lá — e o espírito está na lista porque ele é a moeda com que o próprio fetiche foi pago.

### A cópia do outro lado

Posto **no mundo dos sonhos**, o fetiche se põe **também no mundo de cima**, nas mesmas coordenadas, se lá
houver ar — e essa segunda peça é **espectral**: não tem caixa de choque, não se quebra, e desenha-se a
seis décimos. Tudo o que se muda numa se copia para a outra.

É a melhor ideia do bloco: o espantalho que vigia o mundo de cima **não está no mundo de cima**. Quem o
quiser desligar tem de ir dormir.

### Desvios declarados

1. **O Taglock passou a dizer se prendeu gente ou bicho.** Até aqui não fazia diferença — todo o ofício
   prende gente —, e o Espantalho é a primeira coisa do mod que precisa de saber a diferença. Os frascos
   antigos se leem como gente, que é o que quase todos são.

2. **A lista de fetiches de pé é uma lista.** O original percorre a lista de almas carregadas do mundo e
   pergunta a cada uma se é um fetiche. Aqui eles se apontam ao nascer, que é o que este mod já faz com as
   prateleiras de bonecas.

3. **Se um bicho é familiar de alguém se pergunta do avesso.** O vínculo mora em quem o tem, e não no
   bicho, de modo que a pergunta corre a lista de quem está no mundo. Com meia dúzia de jogadores é uma
   conta de nada, uma vez por segundo.

4. **A Proteção de Vodu está escrita do avesso.** O original tem um `strength > 1` e um laço que gasta
   bonecas uma a uma; aqui a primeira boneca sempre se gasta e o laço das outras é que é saltado. Dá no
   mesmo, e é mais fácil de ler.

5. **A tabela de cores é a de 2014**, e passou a morar no `FleeceColours` porque agora são três coisas que
   pintam com ela — a vassoura, o espantalho e o ídolo. Sem ela, um espantalho sem tinta sairia do ciano do
   jogo de hoje em vez do verde-azulado baço do original.

6. **A Morte está na lista e não chama nada.** O sexto efeito custa cinco de cada fantasma e nenhum
   espírito, e a conta é um aviso: quem puser quinze fantasmas dentro de um círculo de giz não ia ficar com
   um espantalho. O efeito está na lista para a conta ficar certa — ele é o último, e por isso só cabe
   quando nenhum dos cinco cabe —, mas **o que ele chamava ainda não existe**. A Morte é outra fatia.

**Guardas:** o `OccultaFetishGameTest`, com dez — o preço de cada efeito e a ordem deles; **quem cabe
primeiro**, que é a prova que carrega a fatia; o que não cabe e por isso não gasta nada; a Sentinela que
sai quando é ela que cabe; os seis modos; a Boline que os roda; o **alarme que dispara pela ausência**; a
redstone do Grito; o que ele nunca vê; a Sentinela que manda **dois** contra quem está sozinho; o fetiche
que se larga a si próprio com tudo dentro; o balde que apaga as listas; e as três receitas de montagem. E
o `OccultaFetishClientTest`, com os três lado a lado, quatro espantalhos pintados e um espectral.

## As roupas de bruxa (2026-10-09)

O **Chapéu de Bruxa**, o **Manto de Bruxa**, o **Manto de Necromante** e o **Chapéu da Baba Yaga** — e,
com eles, o **Couro Impregnado** de que os quatro se fazem.

### Elas protegem como couro, e isso é o que têm de menos interessante

Um de proteção no chapéu, três no manto, e a durabilidade do couro. O original escreve
`ArmorMaterial.CLOTH` e pronto — **sem** a piada da durabilidade de ferro que ele prega às roupas de
caçador. Um chapéu de bruxa se gasta como um chapéu de couro, e é de propósito: ele não é para levar
pancada, é para cozinhar com ele na cabeça.

### O que elas valem: o frasco a mais

Nenhuma linha do item diz isto. A conta mora na **Chaleira** e no **Caldeirão**, e é ela que faz das
roupas a peça mais importante do ramo do caldeirão.

**Na Chaleira**, ao tirar um frasco:

| Quem | Um **segundo** frasco | E um **terceiro** |
|---|---|---|
| Chapéu de Bruxa | +35% | — |
| Chapéu da Baba Yaga | +25% | **+25%** |
| Manto de Bruxa, num cozimento que **não** é de Erguer | +35% | — |
| Manto de Necromante, num cozimento **de Erguer** | +35% | — |
| Familiar com maestria de cozimento | +5% | +5% (só com o chapéu da Baba) |

O chapéu e o manto **não se excluem**: com os dois, a chance de um segundo frasco é de **setenta por
cento**. E os dois mantos se excluem entre si **por tipo de cozimento** — o de Necromante só ajuda no de
Erguer, e o de Bruxa em todos os outros. É a parte que ninguém adivinha, e é a mais bonita da conta.

**No Caldeirão** não é chance, é contagem: o chapéu vale um, cada manto vale um, e o **chapéu da Baba
vale dois sozinho**. Cada nível é um frasco a mais, até três.

Duas peças **quase dobram o que uma bruxa produz**. E com isso cai a lacuna declarada da fatia do
Caldeirão, que dizia que o rendimento maior de chapéu e túnica ficava de fora porque «nenhuma dessas
coisas existe ainda».

### O chapéu da Baba Yaga

Ele não é o chapéu de bruxa pintado de outra cor. São **quatro caixas encaixadas umas nas outras**, cada
uma menor e mais torta que a anterior — um cone **amassado**, que se dobra sobre si próprio. E é o
único dos quatro que **não se tinge**: um chapéu assim é de alguém.

E é o único do mod inteiro que dá chance de um **terceiro** frasco.

### Duas escalas, e não uma

O original monta **dois bonecos**: um inchado a 0,61 e outro a 0,45, e escolhe entre eles pela **casa** da
peça — a cabeça e as pernas levam o magro, o peito e os pés o gordo. É de 2014 e não faz sentido nenhum,
mas é o que dá o volume que ele dá, e por isso fica.

### Desvios declarados

1. **Uma folha, e não duas.** O original tem duas, como o couro do jogo: uma que leva tinta e uma por cima
   que não leva. A segunda, a `witchclothes_overlay.png`, está **vazia** — sessenta e quatro por sessenta
   e quatro de nada. Aqui desenha-se uma vez só, com a folha tingida.

2. **O conserto é um rótulo.** O original escreve quatro receitas sem forma, uma por peça, que juntam
   couro impregnado à peça gasta. O jogo de hoje faz isso sozinho com o rótulo do material.

3. **O rendimento de quem tem prática continua de fora.** No Caldeirão, o original multiplica o que as
   roupas dão pela **perícia de engarrafar** do jogador, que é um sistema que este porte ainda não tem.
   Aqui as roupas somam sozinhas.

4. **A cor de fábrica é um número, e não um componente.** Como nas roupas de caçador: posta como
   componente, toda peça dizia «Tingida» sem ninguém lhe ter tocado.

**Guardas:** o `OccultaWitchClothesGameTest`, com sete — os números das quatro; que elas são couro; o
chapéu da Baba, que é o único que recusa tinta; o **chapéu e o manto que somam setenta por cento**, que é
a prova que carrega a fatia, com os dois mantos trocando de lugar conforme o cozimento; o terceiro frasco
que só a Baba dá; a contagem do Caldeirão; as cinco receitas; e o couro que as conserta. E o
`OccultaWitchClothesClientTest`, com quatro bonecos — o conjunto sem tinta, o de Necromante com as
ombreiras, o da Baba e um pintado de verde.

## O Diabrete (2026-10-09)

O único bicho do mod que **negocia**. Não se doma com comida, não se mata por despojo e não obedece a
quem não lhe pagou — e tudo nele é uma conta de paciência.

### Primeiro é preciso comprá-lo

Ele vem de um rito que pede mal refinado, sangue infernal, uma pérola do Alhures, uma Pedra Sintonizada
e cinco mil de poder — e é o **único rito de chamar que pede um coven**. Uma bruxa sozinha não o traz: um
Diabrete não vem obedecer, vem negociar, e uma negociação precisa de testemunhas.

Chegado, ele é hostil. O que o compra é um **Contrato de Posse assinado com o sangue de quem o leva** —
um taglock de si próprio preso ao papel — e ele cobra, além disso, **vinte e cinco níveis de
experiência**, que come na hora. É a única coisa do mod que se paga com níveis.

Fechado o negócio, ele ganha um **nome de demônio** e o lugar onde está vira a **casa** dele: daí em
diante não se afasta mais de dezesseis blocos. Os nomes são cem, e em quatro de cada cinco vezes saem
**dois juntos** — «Krakus Ehnnat» —, de modo que quase nunca se repetem. Alguns aparecem três vezes na
lista do original, e isso fica: uma lista com repetições é uma lista com peso.

### E depois é preciso agradar-lhe

Ele tem uma conta de **afeição** que sobe com **coisas brilhantes** na mão de quem o tem. Vinte e uma
coisas lhe agradam, e a tabela diz o que ele é:

| Coisa | Vale | Coisa | Vale |
|---|---|---|---|
| Bloco de Diamante | **72** | Bloco de Lápis | 7 |
| Bloco de Esmeralda | 27 | Bloco de Redstone | 5 |
| Machado e Picareta de Diamante | 24 | Lágrima de Ghast | 4 |
| Espada e Enxada de Diamante | 16 | Esmeralda, Machado e Picareta de Ouro | 3 |
| Estrela do Nether | 16 | Espada e Enxada de Ouro | 2 |
| Bloco de Ouro | 9 | Lingote de Ouro, Vara de Blaze, Pá de Ouro | 1 |
| Diamante e Pá de Diamante | 8 | | |

Repare na conta: um bloco de diamante vale setenta e dois, que são **nove diamantes a oito**. Ele não
conta valor — conta **brilho**, e um bloco brilha tanto quanto o que o faz. E uma picareta de diamante
vale vinte e quatro, que são três diamantes: ele paga o feitio.

A afeição **desce um de cinco em cinco minutos**. Com ela em zero e mais de uma hora de vida, há uma
chance em cem por volta de ele simplesmente **ir embora**: «o contrato está cumprido». Quem o quer tem de
continuar a dar-lhe coisas.

### Os segredos

Com afeição de **vinte** para cima, três minutos desde o último presente e um sorteio de
`1 / max(1, 10 − (afeição − 20))` — ou seja, **tanto mais provável quanto mais ele gosta** —, ele
retribui, e a afeição volta a zero. Pela ordem:

1. o **Cozimento de Alma da Fome**, que ensina o **Carnosa Diem**;
2. o do **Medo**, que ensina o **Morsmordre**;
3. o da **Angústia**, que ensina o **Ignianima**;
4. um **Contrato do Tormento**;
5. e daí em diante um ingrediente ao acaso de sete.

**Os três primeiros são a única porta para três dos trinta e um feitiços.** Não há receita, não há rito e
não há livro: há um bicho que só dá o que quer, quando quer — e, se ninguém lhe der atenção, vai embora
levando os segredos consigo.

### O gole, e o que ele corrige

Os Cozimentos de Alma são a única coisa do mod que se aprende **bebendo**. Bebe-se, e o feitiço fica
sabido para sempre — morrer não o tira.

E isto corrige o desvio declarado da fatia dos símbolos, que dizia que o Ignianima, o Carnosa Diem e o
Morsmordre eram trancados por «uma entrada no livro de bruxaria». Não são: são trancados por um gole. O
que **não** tem chave nenhuma é que é *imperdoável* — o Avada Kedavra, o Crucio e o Imperio não se
aprendem de maneira nenhuma, e é por isso que só se lançam com a Infusão Infernal.

### Ligar e desligar

Um **Coração de Demônio** o liga por uma hora: ele **engorda** uma vez e meia de lado — não cresce para
cima, engorda —, bate pelo dobro, nenhum golpe lhe tira mais de cinco e ele solta chama. Uma **Agulha de
Gelo** o apaga na hora, e ele pergunta porquê.

E, ligado, ele **não lança contratos**: «há poder demais para pensar».

### Desvios declarados

1. **As asas vão onde o desenho está.** O original declara, só para elas, uma folha de cento e vinte e
   oito por trinta e dois enquanto o resto do boneco usa sessenta e quatro por sessenta e quatro — e em
   2014 isso valia peça a peça. Mas a conta não fecha: com trinta e dois de altura, uma asa de vinte e um
   a partir de vinte e um **passa de baixo da folha**. O desenho está feito para o sessenta e quatro, e é
   a linha do tamanho que está errada; aqui elas ficam em (23, 21) na folha de verdade.

2. **O que ele lança fica de fora.** Com um contrato preso a outra pessoa na mão, o original manda que ele o lance
   contra ela. Essa família de contratos — o do Blaze, o de Evaporar, o do Toque de Fogo, o de
   Resistir ao Fogo e o de Fundir — é a fatia dos **demônios**, e nenhum deles existe ainda. Fica o
   Contrato de Posse, que é o que o compra, e o do Tormento, que ele dá.

3. **O Cozimento de Alma do Tormento não tem de onde vir.** Ele existe, ensina o Tormentum e bebe-se —
   mas quem o larga é o **Senhor do Tormento**, que pede a dimensão do Tormento. E o Tormentum também
   ainda não está portado, de modo que os dois se esperam.

4. **O que ele caça, caça por uma conta e não por uma lista.** O original se passa a si próprio como
   filtro de alvos e responde «gente, se eu for selvagem; o meu alvo, se eu for de alguém». Aqui a meta
   de caçar gente só vale enquanto ele for selvagem, que dá no mesmo com uma linha a menos.

**Guardas:** o `OccultaImpGameTest`, com oito — os números dele; a tabela das coisas brilhantes, com a
conta do bloco que vale nove; as quantidades dos presentes; o contrato, que não serve em branco nem
assinado por outro e cobra vinte e cinco níveis; os **quatro segredos pela ordem**, que é a prova que
carrega a fatia; o que ele recusa; o coração que o liga e a agulha que o apaga; **o gole que ensina o
feitiço**; e as três receitas. E o `OccultaImpClientTest`, com quatro — dois de frente e dois de costas,
um de cada ligado —, porque o que ele faz com poder é engordar.
