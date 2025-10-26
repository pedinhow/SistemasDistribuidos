package q3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner; // Usaremos Scanner para o console
import java.util.Set;

// Implementa Runnable para ser o "servidor" do nó
public class P2PNode implements Runnable {

    final int id;
    final int port;
    final int successorPort;
    private final Set<String> files = new HashSet<>();

    public P2PNode(int id, int port, int successorPort) {
        this.id = id;
        this.port = port;
        this.successorPort = successorPort;
        initializeFiles(); // Preenche o "disco" deste nó
    }

    /**
     * Ponto de entrada principal. Inicia o nó a partir da linha de comando.
     * (Inspirado no RingNode de Segurança Computacional)
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Uso: java q3.P2PNode <meu_id_num> <minha_porta> <porta_sucessor>");
            System.err.println("Exemplo (P0): java q3.P2PNode 0 6000 6001");
            System.err.println("Exemplo (P5): java q3.P2PNode 5 6005 6000 (fecha o anel)");
            System.exit(1);
        }

        try {
            int id = Integer.parseInt(args[0]);
            int porta = Integer.parseInt(args[1]);
            int portaSucessor = Integer.parseInt(args[2]);

            P2PNode node = new P2PNode(id, porta, portaSucessor);
            node.iniciar(); // Inicia as threads de servidor e console

        } catch (Exception e) {
            System.err.println("Erro ao iniciar nó: " + e.getMessage());
        }
    }

    /**
     * Inicia as duas threads principais:
     * 1. O servidor (para ouvir outros nós).
     * 2. O console (para ouvir o usuário).
     */
    public void iniciar() {
        log("Nó P" + id + " iniciado. Ouvindo na porta " + port + ". Sucessor na porta " + successorPort);

        // 1. Inicia o servidor (este próprio objeto Runnable) em uma nova thread
        Thread serverThread = new Thread(this);
        serverThread.start();

        // 2. Inicia o listener do console na thread principal
        iniciarConsole();
    }

    /**
     * Ouve o console para comandos do usuário (ex: BUSCAR arquivoX).
     * (Lógica adaptada do RingNode de Segurança Computacional)
     */
    private void iniciarConsole() {
        Scanner teclado = new Scanner(System.in);
        System.out.println("Comandos: buscar <arquivo> (ex: buscar arquivo25) | sair");

        while (true) {
            System.out.print("P" + id + "> ");
            String userInput = teclado.nextLine();

            if ("sair".equalsIgnoreCase(userInput)) break;

            if (userInput.toLowerCase().startsWith("buscar ")) {
                String[] partes = userInput.split(" ");
                if (partes.length == 2) {
                    String arquivo = partes[1].trim();
                    // Inicia a busca a partir deste nó
                    // Usamos uma nova thread para a busca não bloquear o console
                    new Thread(() -> startSearch(arquivo)).start();
                } else {
                    log("Formato inválido.");
                }
            } else {
                log("Comando desconhecido.");
            }
        }
        teclado.close();
        log("Encerrando console. O servidor continuará rodando.");
        // Nota: O servidor continuará rodando em background.
        // Você pode querer adicionar System.exit(0) aqui se quiser que o nó todo pare.
    }

    /**
     * Preenche o "disco" local com os arquivos pelos quais este nó é responsável.
     * (Original de P2PNode.java)
     */
    private void initializeFiles() {
        int startFile = (this.id * 10) + 1;
        int endFile = startFile + 9;
        for (int i = startFile; i <= endFile; i++) {
            files.add("arquivo" + i);
        }
    }

    /**
     * Verifica se o arquivo pertence a este nó.
     * (Original de P2PNode.java)
     */
    public boolean hasFile(String fileName) {
        // Tornando a verificação case-insensitive para robustez
        return files.contains(fileName.toLowerCase());
    }

    /**
     * Imprime uma mensagem de log formatada para este nó.
     * (Original de P2PNode.java)
     */
    public void log(String message) {
        System.out.println("[Nó P" + this.id + " | Porta " + this.port + "]: " + message);
    }

    /**
     * Inicia uma busca pelo arquivo.
     * (Original de P2PNode.java)
     */
    public void startSearch(String fileName) {
        log("Iniciando busca pelo arquivo '" + fileName + "'.");

        // Trata o nome do arquivo para o padrão (ex: "ARQUIVO25" -> "arquivo25")
        String normalizedFileName = fileName.toLowerCase();

        // 1. Verifica localmente primeiro
        if (hasFile(normalizedFileName)) {
            log("...Arquivo '" + normalizedFileName + "' ENCONTRADO localmente.");
            return;
        }

        // 2. Se não encontrou, envia para o sucessor
        log("...Arquivo não é local. Repassando para o sucessor (Porta " + successorPort + ").");
        // A mensagem é: TIPO:ARQUIVO:PORTA_ORIGEM
        // Usamos a porta deste nó como o "ID de origem"
        String message = "SEARCH:" + normalizedFileName + ":" + this.port;
        sendMessage(successorPort, message);
    }

    /**
     * Método auxiliar para enviar uma mensagem TCP para uma porta.
     * (Original de P2PNode.java)
     */
    public void sendMessage(int destinationPort, String message) {
        try (Socket socket = new Socket("localhost", destinationPort);
             DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream())) {

            log("(Log de Envio) Enviando '" + message + "' para Porta " + destinationPort);
            fluxoSaida.writeUTF(message);

        } catch (IOException e) {
            log("ERRO: Falha ao enviar mensagem para " + destinationPort + ". " + e.getMessage());
        }
    }

    /**
     * Este é o "Lado Servidor" do nó.
     * Fica em loop infinito ouvindo conexões.
     * (Original de P2PNode.java)
     */
    @Override
    public void run() {
        log("Servidor P2P iniciado. Ouvindo mensagens...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Aguarda uma conexão (de um antecessor ou uma resposta)
                Socket clientSocket = serverSocket.accept();

                // Dispara um Handler multithread para processar a requisição
                NodeHandler handler = new NodeHandler(this, clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("ERRO: Servidor P2P falhou. " + e.getMessage());
        }
    }
}