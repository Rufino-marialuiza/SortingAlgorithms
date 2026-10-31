# CountingSort e buscas lineares

Branch: `codex/counting-sort-buscas-lineares`.

As implementações estão em `Algoritmos.java`, no pacote `ordenaçao` já utilizado pelo projeto. As buscas existentes foram revisadas mantendo as assinaturas utilizadas por `TestesMain`.

| Método | Resultado | Tempo | Memória auxiliar |
| --- | --- | --- | --- |
| `countingSort(int[] v)` | Ordena o próprio vetor em ordem crescente | O(n + k) | O(n + k) |
| `buscaLinear(int[] v, int x)` | Primeiro índice de `x`, ou -1 | Melhor O(1), pior O(n) | O(1) |
| `buscaLinearRec(int[] v, int x)` | Primeiro índice de `x`, ou -1 | Melhor O(1), pior O(n) | O(n) no pior caso |
| `buscaLinearRec(int[] v, int x, int i)` | Primeiro índice de `x` a partir de `i`, ou -1 | Pior O(n - i), para 0 <= i <= n | O(n - i) no pior caso |

No CountingSort, `k` é o maior valor da entrada. O domínio é `[1, 100000]`, conforme o enunciado; valores fora dele geram `IllegalArgumentException` antes de alterar o vetor. Vetores vazios são aceitos. O algoritmo conta frequências, acumula as contagens e preenche um vetor auxiliar da direita para a esquerda, preservando a ordem relativa dos valores iguais. Embora essa estabilidade não seja distinguível em um `int[]`, ela decorre dessa construção. Todos os métodos esperam vetores não nulos.

As buscas percorrem as posições sem interrupção antecipada por comparação de ordem. Assim, buscar `-1` percorre todo o vetor, como no experimento do projeto. Na versão com índice inicial, índices negativos são rejeitados e índices a partir do tamanho retornam -1.

## Compilar e testar

No terminal, a partir da pasta do projeto, com um JDK instalado:

```powershell
javac -encoding UTF-8 -d build Algoritmos.java TestesCountingSortBuscasLineares.java
java -cp build ordenaçao.TestesCountingSortBuscasLineares
```

Os testes verificam vetores vazios, unitários, ordenados, inversos, repetidos, limites do domínio e entradas inválidas. Também comparam o CountingSort com `Arrays.sort` em 100 vetores aleatórios e em um vetor de 500.000 elementos; as buscas são verificadas para elementos presentes e ausentes, incluindo a primeira ocorrência e o índice inicial da recursão.

## Experimentos

```powershell
java -cp build ordenaçao.TestesCountingSortBuscasLineares --experimentos
java -cp build ordenaçao.TestesCountingSortBuscasLineares --experimentos 500 1500 3000
```

A primeira chamada usa 50.000, 150.000 e 500.000 elementos. A segunda usa tamanhos menores para permitir medir a busca recursiva com a pilha padrão nesta máquina. É possível fornecer outros três tamanhos positivos. Não misture tamanhos diferentes em uma comparação direta.

- Valores pseudoaleatórios em `[1, 100000]`, com semente 2026.
- CountingSort e `Arrays.sort` nos cenários aleatório, ordenado, inverso e parcialmente ordenado.
- No cenário parcial, aproximadamente 15% das posições distintas são selecionadas, e somente seus elementos são embaralhados.
- Cada algoritmo recebe uma cópia da mesma entrada original. A cópia fica fora da medição.
- Cada combinação concluída tem 25 execuções, com descarte das 5 primeiras e média das 20 restantes, em nanossegundos, usando `System.nanoTime()`.
- A validação ocorre depois da medição; a ordenação é comparada com a saída de referência para conferir também a preservação dos valores.
- Ambas as buscas recebem o vetor ordenado e procuram `-1`. A versão recursiva continua sendo recursiva e pode causar `StackOverflowError`; nesse caso, o CSV registra a falha, deixa a média vazia e não aproveita amostras parciais.

## Resultados locais

Os arquivos `resultados-counting-sort-buscas-lineares.csv` e `resultados-counting-sort-buscas-lineares-reduzidos.csv` guardam execuções reais com Java 26.0.1 e sistema identificado pela JVM como Windows 10, sem ajuste de tamanho da pilha. A coluna `amostras` indica as amostras usadas na média, não as execuções de aquecimento.

Nos três tamanhos maiores, CountingSort e busca iterativa concluíram todas as execuções, enquanto a busca recursiva apresentou `StackOverflowError`. Nos tamanhos 500, 1.500 e 3.000, os três algoritmos concluíram. Os testes de correção também passaram.

Para 500.000 elementos, esta execução obteve:

| Cenário | CountingSort (ms) | Arrays.sort (ms) |
| --- | ---: | ---: |
| Aleatório | 6,554 | 33,612 |
| Ordenado | 2,676 | 0,326 |
| Inverso | 1,906 | 0,622 |
| Parcial | 2,634 | 19,117 |

O CountingSort se beneficia do domínio limitado e tem custo O(n + k) nos quatro cenários; não depende de comparações entre pares para ordenar. Nesta medição, foi mais rápido nas entradas grandes aleatórias e parciais. `Arrays.sort` foi mais rápido nas ordenadas e inversas. Para entradas pequenas, percorrer o vetor de contagens pode custar mais que ordenar por comparações.

As buscas lineares têm custo O(n) para o alvo ausente, mas a recursiva também consome uma chamada de pilha por posição. O estouro observado nos tamanhos maiores é uma limitação prática dessa implementação em Java. As médias não necessariamente crescem de forma monotônica: por exemplo, a busca iterativa com 50.000 elementos foi mais lenta que com 150.000 nesta execução. Aquecimento/JIT, coleta de lixo, cache e atividade do sistema podem influenciar os tempos; cinco execuções de aquecimento não garantem estabilização. Esses dados descrevem esta execução e não demonstram uma proporção exata entre tamanho e tempo.

Este material cobre o CountingSort e as buscas lineares e pode subsidiar o relatório do grupo. A comparação com buscas binárias e os resultados dos demais algoritmos precisam ser integrados pelo grupo.

## Integração com o restante do projeto

`TestesMain.java` já chama as assinaturas dos três métodos desta parte. Entretanto, o programa geral ainda não compila porque referencia `bubbleSort(int[])`, `selectionSort(int[])` e `mergeSort(int[])`, que não existem na versão atual de `Algoritmos.java` (o MergeSort existente recebe três argumentos). Essas pendências pertencem às outras partes; os comandos acima permitem executar esta entrega independentemente delas.
