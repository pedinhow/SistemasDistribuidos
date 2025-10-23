package q3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
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
     * Preenche o "disco" local com os arquivos pelos quais este nó é responsável.
     *
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
     */
    public boolean hasFile(String fileName) {
        return files.contains(fileName);
    }

    /**
     * Imprime uma mensagem de log formatada para este nó.
     *
     */
    public void log(String message) {
        System.out.println("[Nó P" + this.id + " | Porta " + this.port + "]: " + message);
    }

    /**
     * Inicia uma busca pelo arquivo.
     * Este é o ponto de entrada quando o usuário digita no console.
     *
     */
    public void startSearch(String fileName) {
        log("Iniciando busca pelo arquivo '" + fileName + "'.");

        // 1. Verifica localmente primeiro
        if (hasFile(fileName)) {
            log("...Arquivo '" + fileName + "' ENCONTRADO localmente.");
            return;
        }

        // 2. Se não encontrou, envia para o sucessor
        log("...Arquivo não é local. Repassando para o sucessor (Porta " + successorPort + ").");
        // A mensagem é: TIPO:ARQUIVO:PORTA_ORIGEM
        String message = "SEARCH:" + fileName + ":" + this.port;
        sendMessage(successorPort, message);
    }

    /**
     * Método auxiliar para enviar uma mensagem TCP para uma porta.
     *
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
     * [cite: 1655]
     */
    @Override
    public void run() {
        log("Servidor P2P iniciado. Ouvindo mensagens...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Aguarda uma conexão (de um antecessor ou uma resposta)
                Socket clientSocket = serverSocket.accept();

                // Dispara um Handler multithread para processar a requisição
                // Isso é crucial para evitar deadlock no anel
                NodeHandler handler = new NodeHandler(this, clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            log("ERRO: Servidor P2P falhou. " + e.getMessage());
        }
    }
}
