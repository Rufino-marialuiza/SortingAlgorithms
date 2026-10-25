package ordenaçao;

import java.util.Arrays;
import java.util.Random;

public class TestesMain {

	public static void main (String[] args) {
		int[] tamanhos = {50000, 150000, 500000};
        int totalExecucoes = 25;
        int warmUp = 5;
        
        for (int n : tamanhos) {
            System.out.println("\n========================================");
            System.out.println("TAMANHO DO VETOR: " + n);
            System.out.println("========================================");

            // Gera entradas base para os 4 cenários
            int[] baseAleatorio = gerarAleatorio(n);
            int[] baseOrdenado = Arrays.copyOf(baseAleatorio, n);
            Arrays.sort(baseOrdenado);
            int[] baseInverso = inverterVetor(baseOrdenado);
            int[] baseParcial = gerarParcialmenteOrdenado(baseOrdenado);

            // Testando nos cenários de ordenação
            executarExperimentoOrdenacao("Aleatório", baseAleatorio, totalExecucoes, warmUp);
            executarExperimentoOrdenacao("Ordenado", baseOrdenado, totalExecucoes, warmUp);
            executarExperimentoOrdenacao("Inverso", baseInverso, totalExecucoes, warmUp);
            executarExperimentoOrdenacao("Parcialmente Ordenado", baseParcial, totalExecucoes, warmUp);
        }
	}
	
	private static void executarExperimentoOrdenacao(String nomeCenario, int[] base, int total, int warmUp) {
        System.out.println("\n--- Cenário: " + nomeCenario + " ---");

        String[] nomesAlgoritmos = {
            "BubbleSort", "SelectionSort", "InsertionSort", 
            "MergeSort", "QuickSort", "QuickSort+Shuffle", 
            "CountingSort", "Arrays.sort()"
        };

        for (String alg : nomesAlgoritmos) {
            long somaTempos = 0;
            int validacoesComSucesso = 0;

            for (int i = 0; i < total; i++) {
                int[] copia = Arrays.copyOf(base, base.length);

                long inicio = System.nanoTime();
                
                // Executa o algoritmo correspondente
                switch (alg) {
                    case "BubbleSort": Algoritmos.bubbleSort(copia); break;
                    case "SelectionSort": Algoritmos.selectionSort(copia); break;
                    case "InsertionSort": Algoritmos.insertion(copia); break;
                    case "MergeSort": Algoritmos.mergeSort(copia); break;
                    case "QuickSort": Algoritmos.quickSort(copia, 0, copia.length - 1); break;
                    case "QuickSort+Shuffle": Algoritmos.quickSortShuffle(copia); break;
                    case "CountingSort": Algoritmos.countingSort(copia); break;
                    case "Arrays.sort()": Arrays.sort(copia); break;
                }

                long fim = System.nanoTime();
                long tempoDecorrido = fim - inicio;

                if (estaOrdenado(copia)) {
                    validacoesComSucesso++;
                }

                // Descarta as 5 primeiras execuções (warm-up)
                if (i >= warmUp) {
                    somaTempos += tempoDecorrido;
                }
            }

            long mediaTempoMs = (somaTempos / (total - warmUp)) / 1_000_000;
            System.out.printf("Algoritmo: %-18s | Média: %6d ms\n", alg, mediaTempoMs);
        }
    }

    private static int[] gerarAleatorio(int n) {
        Random rand = new Random();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = rand.nextInt(100000) + 1; // Intervalo [1, 100.000]
        }
        return arr;
    }

    private static int[] inverterVetor(int[] arr) {
        int[] inv = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            inv[i] = arr[arr.length - 1 - i];
        }
        return inv;
    }

    private static int[] gerarParcialmenteOrdenado(int[] ordenado) {
        int[] arr = Arrays.copyOf(ordenado, ordenado.length);
        Random rand = new Random();
        int qtdTrocas = (int) (arr.length * 0.15); // 15% dos elementos embaralhados
        for (int k = 0; k < qtdTrocas; k++) {
            int i = rand.nextInt(arr.length);
            int j = rand.nextInt(arr.length);
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    private static boolean estaOrdenado(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) return false;
        }
        return true;
    }
}
