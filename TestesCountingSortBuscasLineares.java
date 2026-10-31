package ordenaçao;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Testes de correcao e experimentos de CountingSort e buscas lineares. */
public class TestesCountingSortBuscasLineares {
    private static final int EXECUCOES = 25;
    private static final int WARM_UP = 5;
    private static final long SEMENTE = 2026L;
    // Torna o resultado das buscas observavel fora do trecho cronometrado.
    private static volatile int resultadoBusca;

    public static void main(String[] args) {
        if (args.length == 0) {
            testarCorrecao();
            System.out.println("Todos os testes de CountingSort e buscas lineares passaram.");
            return;
        }
        if (!args[0].equals("--experimentos") || (args.length != 1 && args.length != 4)) {
            throw new IllegalArgumentException("Uso: TestesCountingSortBuscasLineares [--experimentos [n1 n2 n3]]");
        }
        int[] tamanhos = {50000, 150000, 500000};
        if (args.length == 4) {
            for (int i = 0; i < tamanhos.length; i++) {
                tamanhos[i] = Integer.parseInt(args[i + 1]);
                verificar(tamanhos[i] > 0, "Os tamanhos devem ser positivos.");
            }
        }
        testarCorrecao();
        System.err.println("Java: " + System.getProperty("java.version")
                + "; SO: " + System.getProperty("os.name")
                + "; semente: " + SEMENTE + "; execucoes: " + EXECUCOES
                + "; warm-up: " + WARM_UP);
        System.out.println("algoritmo,tamanho,cenario,amostras,media_ns,status");
        Random random = new Random(SEMENTE);
        for (int n : tamanhos) {
            int[] aleatorio = gerarAleatorio(n, random);
            int[] ordenado = aleatorio.clone();
            Arrays.sort(ordenado);
            int[] inverso = ordenado.clone();
            for (int i = 0; i < n / 2; i++) {
                trocar(inverso, i, n - 1 - i);
            }
            int[][] bases = {aleatorio, ordenado, inverso, parcialmenteOrdenado(ordenado, random)};
            String[] cenarios = {"aleatorio", "ordenado", "inverso", "parcial"};
            for (int i = 0; i < bases.length; i++) {
                medirOrdenacao(bases[i], cenarios[i], false);
                medirOrdenacao(bases[i], cenarios[i], true);
            }
            medirBusca(ordenado, false);
            medirBusca(ordenado, true);
        }
    }

    private static void testarCorrecao() {
        int[][] casos = {{}, {1}, {100000}, {7, 7, 7}, {1, 2, 3},
                {100000, 9, 2, 1}, {4, 1, 4, 100000, 1, 20}};
        for (int[] caso : casos) {
            conferirOrdenacao(caso);
        }
        Random random = new Random(SEMENTE);
        for (int i = 0; i < 100; i++) {
            int[] vetor = gerarAleatorio(random.nextInt(200), random);
            conferirOrdenacao(vetor);
            conferirBuscas(vetor, -1);
            for (int valor : vetor) {
                conferirBuscas(vetor, valor);
            }
        }
        // Inclui o maior tamanho do experimento e os limites do dominio.
        conferirOrdenacao(gerarAleatorio(500000, random));
        conferirBuscas(new int[0], -1);
        int[] valores = {1, 3, 3, 9, 100000};
        for (int alvo : new int[] {1, 3, 9, 100000, -1, 2, 100001}) {
            conferirBuscas(valores, alvo);
        }
        verificar(Algoritmos.buscaLinearRec(valores, 3, 2) == 2, "Busca a partir de indice.");
        verificar(Algoritmos.buscaLinearRec(valores, 1, 1) == -1, "Respeitar indice inicial.");
        verificar(Algoritmos.buscaLinearRec(valores, 1, valores.length) == -1, "Fim do vetor.");
        esperarArgumentoInvalido(() -> Algoritmos.buscaLinearRec(valores, 1, -1));
        for (int invalido : new int[] {-1, 0, 100001, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            int[] vetor = {3, invalido, 1};
            int[] original = vetor.clone();
            esperarArgumentoInvalido(() -> Algoritmos.countingSort(vetor));
            verificar(Arrays.equals(vetor, original), "Entrada invalida nao deve ser alterada.");
        }
    }

    private static void conferirOrdenacao(int[] entrada) {
        int[] esperado = entrada.clone();
        int[] obtido = entrada.clone();
        Arrays.sort(esperado);
        Algoritmos.countingSort(obtido);
        verificar(Arrays.equals(esperado, obtido), "CountingSort divergiu de Arrays.sort.");
    }

    private static void conferirBuscas(int[] vetor, int alvo) {
        int esperado = -1;
        // Percurso inverso: a ultima atribuicao corresponde a primeira ocorrencia.
        for (int i = vetor.length - 1; i >= 0; i--) {
            if (vetor[i] == alvo) esperado = i;
        }
        verificar(Algoritmos.buscaLinear(vetor, alvo) == esperado, "Busca linear iterativa.");
        verificar(Algoritmos.buscaLinearRec(vetor, alvo) == esperado, "Busca linear recursiva.");
        verificar(Algoritmos.buscaLinearRec(vetor, alvo, 0) == esperado, "Busca com indice zero.");
    }

    private static void medirOrdenacao(int[] base, String cenario, boolean biblioteca) {
        int[] esperado = base.clone();
        Arrays.sort(esperado);
        long soma = 0;
        for (int i = 0; i < EXECUCOES; i++) {
            int[] copia = base.clone();
            long inicio = System.nanoTime();
            if (biblioteca) Arrays.sort(copia);
            else Algoritmos.countingSort(copia);
            long tempo = System.nanoTime() - inicio;
            verificar(Arrays.equals(esperado, copia), "Ordenacao incorreta no experimento.");
            if (i >= WARM_UP) soma += tempo;
        }
        imprimir(biblioteca ? "Arrays.sort" : "CountingSort", base.length, cenario, soma);
    }

    private static void medirBusca(int[] ordenado, boolean recursiva) {
        String nome = recursiva ? "Linear Recursiva" : "Linear Iterativa";
        long soma = 0;
        for (int i = 0; i < EXECUCOES; i++) {
            int resultado;
            long inicio = System.nanoTime();
            try {
                resultado = recursiva ? Algoritmos.buscaLinearRec(ordenado, -1)
                        : Algoritmos.buscaLinear(ordenado, -1);
            } catch (StackOverflowError erro) {
                // Nao publicar media de uma combinacao que nao completou as 25 execucoes.
                System.out.printf("%s,%d,ordenado-alvo-ausente,0,,StackOverflowError%n",
                        nome, ordenado.length);
                return;
            }
            long tempo = System.nanoTime() - inicio;
            resultadoBusca = resultado;
            verificar(resultadoBusca == -1, "Busca deveria retornar -1.");
            if (i >= WARM_UP) soma += tempo;
        }
        imprimir(nome, ordenado.length, "ordenado-alvo-ausente", soma);
    }

    private static void imprimir(String algoritmo, int n, String cenario, long soma) {
        System.out.printf(Locale.ROOT, "%s,%d,%s,%d,%.2f,OK%n",
                algoritmo, n, cenario, EXECUCOES - WARM_UP,
                soma / (double) (EXECUCOES - WARM_UP));
    }

    private static int[] gerarAleatorio(int n, Random random) {
        int[] vetor = new int[n];
        for (int i = 0; i < n; i++) vetor[i] = random.nextInt(100000) + 1;
        return vetor;
    }

    private static int[] parcialmenteOrdenado(int[] ordenado, Random random) {
        int[] vetor = ordenado.clone();
        int[] posicoes = new int[vetor.length];
        for (int i = 0; i < posicoes.length; i++) posicoes[i] = i;
        int quantidade = (int) Math.round(vetor.length * 0.15);
        // Seleciona posicoes distintas; apenas esses elementos sao embaralhados.
        for (int i = 0; i < quantidade; i++) {
            trocar(posicoes, i, i + random.nextInt(posicoes.length - i));
        }
        for (int i = quantidade - 1; i > 0; i--) {
            trocar(vetor, posicoes[i], posicoes[random.nextInt(i + 1)]);
        }
        return vetor;
    }

    private static void trocar(int[] vetor, int i, int j) {
        int temporario = vetor[i];
        vetor[i] = vetor[j];
        vetor[j] = temporario;
    }

    private static void esperarArgumentoInvalido(Runnable operacao) {
        try {
            operacao.run();
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError("Era esperada uma IllegalArgumentException.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
