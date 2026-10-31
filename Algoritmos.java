package ordenaçao;

import java.util.Random;

public class Algoritmos {
	
	public static void insertion(int[] v) {
		int n=v.length;
		
		for(int i=1;i<n-1;i++) {
			int chave = v[i];
			int p = i-1;
			while(p>=0 && v[p]>chave) {
				v[p+1]=v[p];
				p--;
			}
			v[p+1]=chave;
			
		}
	}
	
	public static void mergeSort(int[] v, int inicio, int fim) {
        if (v.length == 1) return;
        else {
        	int meio = inicio + (fim - inicio) / 2;

            mergeSort(v, inicio, meio);
            mergeSort(v, meio+1,fim);
            merge(v, inicio, meio, fim);
        }
    }

    private static void merge(int[] v, int inicio, int meio, int fim) {
    	int n1 = meio - inicio + 1;
        int n2 = fim - meio;

        int[] esquerda = new int[n1];
        int[] direita = new int[n2];

        for (int i = 0; i < n1; i++) {
            esquerda[i] = v[inicio + i];
        }
        for (int j = 0; j < n2; j++) {
            direita[j] = v[meio + 1 + j];
        }

        int i = 0, j = 0, k = inicio;
        while (i < n1 && j < n2) {
            if (esquerda[i] <= direita[j]) {
                v[k++] = esquerda[i++];
            } else {
                v[k++] = direita[j++];
            }
        }

        while (i < n1) {
            v[k++] = esquerda[i++];
        }

        while (j < n2) {
            v[k++] = direita[j++];
        }
    }
    
    
    public static void quickSort (int[] v, int left, int right) {
        if (left < right) {
            int pivot = partition(v, left, right);
            quickSort(v, left, pivot - 1);
            quickSort(v, pivot + 1, right);
        }
    }

    private static int partition(int[] v, int left, int right) {
        int p = v[left];
        int i = left + 1;
        int j = right;
        
        while (i <= j) {
            if (v[i] <= p) {
                i++;
            } else if (v[j] > p) {
                j--;
            } else {
                swap(v, i, j);
            }
        }
        
        swap(v, left, j);
        return j;
    }

    private static void swap(int[] v, int i, int j) {
        int temp = v[i];
        v[i] = v[j];
        v[j] = temp;
    }
    
    public static void quickSortShuffle(int[] v) { //embaralha
        Random rand = new Random();
        
        for (int i = v.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = v[i];
            v[i] = v[j];
            v[j] = temp;
        }
        quickSort(v, 0, v.length - 1);
    }
    
    /**
     * Ordena de forma estavel os valores do intervalo [1, 100000] do projeto.
     * Tempo O(n + k) e memoria auxiliar O(n + k), sendo k o maior valor.
     */
    public static void countingSort(int[] v) {
        if (v.length == 0) {
            return;
        }

        int maior = 0;
        for (int valor : v) {
            if (valor < 1 || valor > 100000) {
                throw new IllegalArgumentException("Os valores devem estar entre 1 e 100000.");
            }
            if (valor > maior) {
                maior = valor;
            }
        }

        int[] contagem = new int[maior + 1];
        for (int valor : v) {
            contagem[valor]++;
        }
        // A soma acumulada indica o fim do intervalo de cada valor na saida.
        for (int valor = 1; valor <= maior; valor++) {
            contagem[valor] += contagem[valor - 1];
        }

        int[] ordenado = new int[v.length];
        // Percorrer de tras para frente preserva a ordem dos valores iguais.
        for (int i = v.length - 1; i >= 0; i--) {
            int valor = v[i];
            ordenado[--contagem[valor]] = valor;
        }
        System.arraycopy(ordenado, 0, v, 0, v.length);
    }

    /** Retorna o primeiro indice de x ou -1. Tempo O(n), espaco O(1). */
    public static int buscaLinear(int[] v, int x) {
        for (int i = 0; i < v.length; i++) {
            if (v[i] == x) {
                return i;
            }
        }
        return -1;
    }

    /** Inicia a busca recursiva na primeira posicao do vetor. */
    public static int buscaLinearRec(int[] v, int x) {
        return buscaLinearRec(v, x, 0);
    }

    /**
     * Retorna o primeiro indice de x a partir de i ou -1.
     * Tempo e pilha O(n - i); vetores grandes podem causar StackOverflowError.
     */
    public static int buscaLinearRec(int[] v, int x, int i) {
        if (i < 0) {
            throw new IllegalArgumentException("O indice inicial nao pode ser negativo.");
        }
        if (i >= v.length) {
            return -1;
        }
        if (v[i] == x) {
            return i;
        }
        return buscaLinearRec(v, x, i + 1);
    }

    // Busca binária iterativa
    public static int buscaBinaria(int[] v, int x) {
        int esq = 0, dir = v.length - 1;
        while (esq <= dir) {
            int meio = esq + (dir - esq) / 2;

            if (v[meio] == x) {
                return meio;}
            if (v[meio] < x){
                 esq = meio + 1;}
            else {dir = meio - 1;}
        }
        return -1;
    }

    // Busca binária recursiva
    public static int buscaBinariaRec(int[] v, int x, int esq, int dir) {
        if (esq > dir) {
            return -1;}
        int meio = esq + (dir - esq) / 2;

        if (v[meio] == x) {
            return meio;}
        if (v[meio] < x){
             return buscaBinariaRec(v, x, meio + 1, dir);}
        return buscaBinariaRec(v, x, esq, meio - 1);
    }

    // Busca linear iterativa de duas pontas
    public static int buscaLinearDuasPontas(int[] v, int x) {
        int i = 0, j = v.length - 1;
        while (i <= j) {
            if (v[i] == x) 
                {return i;}
            if (v[j] == x){
                 return j;}
            i++;
            j--;
        }
        return -1;
    }

        
}
