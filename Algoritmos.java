package ordenaçao;

import java.util.Random;

public class Algoritmos {
	
	public void insertion(int[] v) {
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
	
	public void mergeSort(int[] v, int inicio, int fim) {
        if (v.length == 1) return;
        else {
        	int meio = inicio + (fim - inicio) / 2;

            mergeSort(v, inicio, meio);
            mergeSort(v, meio+1,fim);
            merge(v, inicio, meio, fim);
        }
    }

    private void merge(int[] v, int inicio, int meio, int fim) {
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
    
    
    public void quickSort (int[] v, int left, int right) {
        if (left < right) {
            int pivot = partition(v, left, right);
            quickSort(v, left, pivot - 1);
            quickSort(v, pivot + 1, right);
        }
    }

    private int partition(int[] v, int left, int right) {
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

    private void swap(int[] v, int i, int j) {
        int temp = v[i];
        v[i] = v[j];
        v[j] = temp;
    }
    
    public void quickSortShuffle(int[] v) { //embaralha
        Random rand = new Random();
        
        for (int i = v.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = v[i];
            v[i] = v[j];
            v[j] = temp;
        }
        quickSort(v, 0, v.length - 1);
    }
}
