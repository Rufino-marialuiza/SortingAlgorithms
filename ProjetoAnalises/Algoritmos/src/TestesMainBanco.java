import java.util.Arrays;
import java.util.Random;
import java.sql.*;

public class TestesMainBanco {

    private static Connection conexao;

    public static void main(String[] args) {

        try {
            iniciarBanco();

            int[] tamanhos = {50000, 150000, 300000};

            int totalExecucoes = 25;
            int warmUp = 5;

            for (int n : tamanhos) {

                System.out.println("\n========================================");
                System.out.println("TAMANHO DO VETOR: " + n);
                System.out.println("========================================");

                // Gera entradas reproduzíveis
                int[] baseAleatorio = gerarAleatorio(n);
                int[] baseOrdenado = Arrays.copyOf(baseAleatorio, n);

                Arrays.sort(baseOrdenado);

                int[] baseInverso = inverterVetor(baseOrdenado);
                int[] baseParcial = gerarParcialmenteOrdenado(baseOrdenado);

                // Testes de ordenação
                executarExperimentoOrdenacao(
                    "Aleatório", baseAleatorio, totalExecucoes, warmUp
                );

                executarExperimentoOrdenacao(
                    "Ordenado", baseOrdenado, totalExecucoes, warmUp
                );

                executarExperimentoOrdenacao(
                    "Inverso", baseInverso, totalExecucoes, warmUp
                );

                executarExperimentoOrdenacao(
                    "Parcialmente Ordenado", baseParcial,
                    totalExecucoes, warmUp
                );

                // Testes de busca
                executarExperimentoBusca(
                    baseOrdenado, totalExecucoes, warmUp
                );
            }

            System.out.println("\nTODOS OS TESTES FORAM FINALIZADOS!");

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();

        } finally {
            fecharBanco();
        }
    }

    // ==========================================
    // BANCO DE DADOS
    // ==========================================

    private static void iniciarBanco() throws SQLException {

        conexao = DriverManager.getConnection(
            "jdbc:sqlite:resultados.db"
        );

        try (Statement stmt = conexao.createStatement()) {

            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=FULL;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS resultados (
                    tipo TEXT,
                    tamanho INTEGER,
                    cenario TEXT,
                    algoritmo TEXT,
                    repeticao INTEGER,
                    tempo_ns INTEGER,
                    status TEXT,

                    PRIMARY KEY (
                        tipo,
                        tamanho,
                        cenario,
                        algoritmo,
                        repeticao
                    )
                );
            """);
        }

        System.out.println("Banco de dados conectado!");
    }

    private static void fecharBanco() {

        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                System.out.println("Banco de dados fechado.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar banco: " + e.getMessage());
        }
    }

    private static boolean resultadoExiste(
        String tipo,
        int tamanho,
        String cenario,
        String algoritmo,
        int repeticao
    ) throws SQLException {

        String sql = """
            SELECT 1 FROM resultados
            WHERE tipo = ?
            AND tamanho = ?
            AND cenario = ?
            AND algoritmo = ?
            AND repeticao = ?
        """;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            stmt.setInt(2, tamanho);
            stmt.setString(3, cenario);
            stmt.setString(4, algoritmo);
            stmt.setInt(5, repeticao);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void salvarResultado(
        String tipo,
        int tamanho,
        String cenario,
        String algoritmo,
        int repeticao,
        long tempo,
        String status
    ) throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO resultados
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            stmt.setInt(2, tamanho);
            stmt.setString(3, cenario);
            stmt.setString(4, algoritmo);
            stmt.setInt(5, repeticao);
            stmt.setLong(6, tempo);
            stmt.setString(7, status);

            stmt.executeUpdate();
        }
    }

    private static void mostrarMedia(
        String tipo,
        int tamanho,
        String cenario,
        String algoritmo,
        int totalMedicoes
    ) throws SQLException {

        String sql = """
            SELECT AVG(tempo_ns), COUNT(*)
            FROM resultados
            WHERE tipo = ?
            AND tamanho = ?
            AND cenario = ?
            AND algoritmo = ?
            AND status = 'OK'
        """;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            stmt.setInt(2, tamanho);
            stmt.setString(3, cenario);
            stmt.setString(4, algoritmo);

            try (ResultSet rs = stmt.executeQuery()) {

                double mediaNs = rs.getDouble(1);
                int quantidade = rs.getInt(2);

                double mediaMs = mediaNs / 1_000_000.0;

                System.out.printf(
                    "Algoritmo: %-20s | Média: %10.3f ms | Medições: %d/%d%n",
                    algoritmo,
                    mediaMs,
                    quantidade,
                    totalMedicoes
                );
            }
        }
    }

    // ==========================================
    // EXPERIMENTOS DE ORDENAÇÃO
    // ==========================================

    private static void executarExperimentoOrdenacao(
        String nomeCenario,
        int[] base,
        int total,
        int warmUp
    ) throws SQLException {

        System.out.println("\n--- Cenário: " + nomeCenario + " ---");

        String[] nomesAlgoritmos = {
            "BubbleSort",
            "BubbleSortOtimizado",
            "SelectionSort",
            "selectionSortEstavel",
            "InsertionSort",
            "MergeSort",
            "QuickSort",
            "QuickSort+Shuffle",
            "CountingSort",
            "Arrays.sort()"
        };

        for (String alg : nomesAlgoritmos) {

            System.out.println("\nTestando: " + alg);

            for (int i = 0; i < total; i++) {

                int repeticao = i - warmUp + 1;

                // Pula medições já salvas anteriormente
                if (i >= warmUp &&
                    resultadoExiste(
                        "ORDENACAO",
                        base.length,
                        nomeCenario,
                        alg,
                        repeticao
                    )) {

                    continue;
                }

                int[] copia = Arrays.copyOf(base, base.length);

                long inicio = System.nanoTime();

                // Executa o algoritmo
                switch (alg) {

                    case "BubbleSort":
                        Algoritmos.bubbleSort(copia);
                        break;

                    case "BubbleSortOtimizado":
                        Algoritmos.bubbleSortOtimizado(copia);
                        break;

                    case "SelectionSort":
                        Algoritmos.selectionSort(copia);
                        break;

                    case "selectionSortEstavel":
                        Algoritmos.selectionSortEstavel(copia);
                        break;

                    case "InsertionSort":
                        Algoritmos.insertion(copia);
                        break;

                    case "MergeSort":
                        Algoritmos.mergeSort(copia, 0, copia.length - 1);
                        break;

                    case "QuickSort":
                        Algoritmos.quickSort(copia, 0, copia.length - 1);
                        break;

                    case "QuickSort+Shuffle":
                        Algoritmos.quickSortShuffle(copia);
                        break;

                    case "CountingSort":
                        Algoritmos.countingSort(copia);
                        break;

                    case "Arrays.sort()":
                        Arrays.sort(copia);
                        break;
                }

                long fim = System.nanoTime();

                long tempoDecorrido = fim - inicio;

                // Verificação fora da medição
                boolean correto = estaOrdenado(copia);

                if (!correto) {
                    System.out.println(
                        "ERRO: " + alg + " não ordenou corretamente!"
                    );
                }

                // Salva apenas as 20 medições válidas
                if (i >= warmUp) {

                    salvarResultado(
                        "ORDENACAO",
                        base.length,
                        nomeCenario,
                        alg,
                        repeticao,
                        tempoDecorrido,
                        correto ? "OK" : "ERRO"
                    );

                    System.out.println(
                        "Execução " + repeticao + "/20 salva."
                    );
                }
            }

            // Consulta a média diretamente no banco
            mostrarMedia(
                "ORDENACAO",
                base.length,
                nomeCenario,
                alg,
                total - warmUp
            );
        }
    }

    // ==========================================
    // EXPERIMENTOS DE BUSCA
    // ==========================================

    private static void executarExperimentoBusca(
        int[] ordenado,
        int total,
        int warmUp
    ) throws SQLException {

        System.out.println("\n--- Busca pelo elemento -1 (ausente) ---");

        String[] nomes = {
            "Linear Iterativa",
            "Linear Recursiva",
            "Binaria Iterativa",
            "Binaria Recursiva",
            "Linear Duas Pontas"
        };

        final int alvo = -1;

        for (String alg : nomes) {

            System.out.println("\nTestando: " + alg);

            boolean estouro = false;

            for (int i = 0; i < total; i++) {

                int repeticao = i - warmUp + 1;

                // Pula resultados já registrados
                if (i >= warmUp &&
                    resultadoExiste(
                        "BUSCA",
                        ordenado.length,
                        "Elemento ausente (-1)",
                        alg,
                        repeticao
                    )) {

                    continue;
                }

                int res = -2;

                long inicio = System.nanoTime();

                try {

                    switch (alg) {

                        case "Linear Iterativa":
                            res = Algoritmos.buscaLinear(
                                ordenado, alvo
                            );
                            break;

                        case "Linear Recursiva":
                            res = Algoritmos.buscaLinearRec(
                                ordenado, alvo, 0
                            );
                            break;

                        case "Binaria Iterativa":
                            res = Algoritmos.buscaBinaria(
                                ordenado, alvo
                            );
                            break;

                        case "Binaria Recursiva":
                            res = Algoritmos.buscaBinariaRec(
                                ordenado, alvo, 0, ordenado.length - 1
                            );
                            break;

                        case "Linear Duas Pontas":
                            res = Algoritmos.buscaLinearDuasPontas(
                                ordenado, alvo
                            );
                            break;
                    }

                } catch (StackOverflowError e) {

                    estouro = true;

                    if (i >= warmUp) {

                        salvarResultado(
                            "BUSCA",
                            ordenado.length,
                            "Elemento ausente (-1)",
                            alg,
                            repeticao,
                            0,
                            "STACK_OVERFLOW"
                        );
                    }

                    System.out.println(
                        "StackOverflowError na execução " + (i + 1)
                    );

                    break;
                }

                long fim = System.nanoTime();

                long tempoDecorrido = fim - inicio;

                // Verificação fora da medição
                boolean correto = (res == -1);

                if (!correto) {
                    System.out.println(
                        "ERRO: " + alg + " retornou " + res
                    );
                }

                if (i >= warmUp) {

                    salvarResultado(
                        "BUSCA",
                        ordenado.length,
                        "Elemento ausente (-1)",
                        alg,
                        repeticao,
                        tempoDecorrido,
                        correto ? "OK" : "ERRO"
                    );

                    System.out.println(
                        "Execução " + repeticao + "/20 salva."
                    );
                }
            }

            if (estouro) {

                System.out.println(
                    "Algoritmo: " + alg +
                    " | StackOverflowError (n = " + ordenado.length + ")"
                );

            } else {

                mostrarMedia(
                    "BUSCA",
                    ordenado.length,
                    "Elemento ausente (-1)",
                    alg,
                    total - warmUp
                );
            }
        }
    }

    // ==========================================
    // GERAÇÃO DOS VETORES
    // ==========================================

    private static int[] gerarAleatorio(int n) {

        Random rand = new Random(123456789L + n);

        int[] arr = new int[n];

        for (int i = 0; i < n; i++) {
            arr[i] = rand.nextInt(100000) + 1;
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

        Random rand = new Random(987654321L + arr.length);

        int qtd = (int) (arr.length * 0.15);

        // Seleciona posições sem repetição
        int[] indices = new int[arr.length];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        for (int i = 0; i < qtd; i++) {

            int j = i + rand.nextInt(arr.length - i);

            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;
        }

        // Guarda os valores das posições selecionadas
        int[] valores = new int[qtd];

        for (int i = 0; i < qtd; i++) {
            valores[i] = arr[indices[i]];
        }

        // Embaralha esses valores
        for (int i = qtd - 1; i > 0; i--) {

            int j = rand.nextInt(i + 1);

            int temp = valores[i];
            valores[i] = valores[j];
            valores[j] = temp;
        }

        // Coloca os valores embaralhados nas posições selecionadas
        for (int i = 0; i < qtd; i++) {
            arr[indices[i]] = valores[i];
        }

        return arr;
    }

    // ==========================================
    // VERIFICAÇÃO
    // ==========================================

    private static boolean estaOrdenado(int[] arr) {

        for (int i = 0; i < arr.length - 1; i++) {

            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }

        return true;
    }
}
