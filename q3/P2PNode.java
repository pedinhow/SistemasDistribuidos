package q3;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class P2PNode {

    private final String nodeId; // ex: "P0"
    private final int port;
    private final String successorHost = "localhost";
    private final int successorPort;
    private final Set<String> localFiles = new HashSet<>();

    public P2PNode(String nodeId, int port, int successorPort) {
        this.nodeId = nodeId;
        this.port = port;
        this.successorPort = successorPort;
        this.initializeFiles();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Uso: java q3.P2PNode <meu_id> <minha_porta> <porta_sucessor>");
            System.err.println("Exemplo (P0): java q3.P2PNode P0 6000 6001");
            System.err.println("Exemplo (P5): java q3.P2PNode P5 6005 6000 (fecha o anel)");
            System.exit(1);
        }
        try {
            String id = args[0];
            int port = Integer.parseInt(args[1]);
            int successorPort = Integer.parseInt(args[2]);
            new P2PNode(id, port, successorPort).start();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar nó: " + e.getMessage());
        }
    }

    public void start() {
        log("Nó " + nodeId + " iniciado. Ouvindo na porta " + port + ". Sucessor na porta " + successorPort);
        new Thread(this::startServer).start(); // inicia o servidor
        startConsole(); // inicia o cliente
    }

    private void startConsole() {
        try (BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Comandos: SEARCH <arquivo> (ex: SEARCH arquivo25) | SAIR");
            System.out.print(nodeId + "> ");
            String userInput;
            while ((userInput = consoleIn.readLine()) != null) {
                if ("SAIR".equalsIgnoreCase(userInput)) break;

                if (userInput.toUpperCase().startsWith("SEARCH ")) {
                    String[] parts = userInput.split(" ");
                    if (parts.length == 2) {
                        String file = parts[1];
                        // formato da mensagem: "SEARCH;ARQUIVO;ID_ORIGEM;PORTA_ORIGEM"
                        String payload = "SEARCH;" + file + ";" + this.nodeId + ";" + this.port;
                        processMessage(payload);
                    } else {
                        log("Formato inválido.");// [cite: 116, 284]
                    }
                } else {
                    log("Comando desconhecido.");
                }
                System.out.print(nodeId + "> ");
            }
        } catch (Exception e) {
            log("Erro no console: " + e.getMessage());
        }
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // cada conexão é tratada em uma nova thread
                new Thread(new NodeHandler(clientSocket, this)).start();
            }
        } catch (Exception e) {
            log("ERRO no Servidor: " + e.getMessage());
        }
    }

    public void processMessage(String payload) {
        try {
            String[] parts = payload.split(";");
            if (parts.length < 4 || !parts[0].equals("SEARCH")) {
                log("Mensagem mal formatada recebida: " + payload);
                return;
            }
            String file = parts[1];
            String originId = parts[2];
            int originPort = Integer.parseInt(parts[3]);

            if (isResponsible(file)) {
                log("ARQUIVO ENCONTRADO! '" + file + "' está neste nó (" + nodeId + ").");
                log("(Busca originada por: " + originId + ")");
                // envia a resposta de volta para a origem
                String response = "FOUND;" + file + ";" + this.nodeId;
                sendMessage(originPort, response);
            } else {
                // se não está aqui, encaminho para o sucessor
                log("Arquivo '" + file + "' não está aqui. Encaminhando para " + successorPort + "...");
                sendMessage(successorPort, payload); // encaminha a mensagem original
            }
        } catch (Exception e) {
            log("Erro ao processar mensagem: " + e.getMessage());
        }
    }

    public void sendMessage(int destinationPort, String payload) {
        log("Enviando (plano): " + payload + " para " + destinationPort);
        try (Socket socket = new Socket(successorHost, destinationPort);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
            outputStream.writeUTF(payload);
        } catch (Exception e) {
            log("ERRO ao encaminhar para " + destinationPort + ": " + e.getMessage());
        }
    }

    private boolean isResponsible(String file) {
        try {
            int idNum = Integer.parseInt(nodeId.replace("P", ""));
            int fileNum = Integer.parseInt(file.replaceAll("(?i)arquivo", ""));
            // define o intervalo de responsabilidade
            int min = (idNum * 10) + 1;
            int max = (idNum * 10) + 10;
            return (fileNum >= min && fileNum <= max);
        } catch (NumberFormatException e) {
            log("Formato de arquivo ou ID inválido: " + file + " / " + nodeId);
            return false;
        }
    }

    private void initializeFiles() {
        // apenas para log, a lógica real está em isResponsible
        int idNum = Integer.parseInt(nodeId.replace("P", ""));
        int min = (idNum * 10) + 1;
        int max = (idNum * 10) + 10;
        log("Este nó é responsável pelos arquivos " + min + " a " + max + ".");
    }

    public void log(String message) {
        System.out.println("[" + nodeId + " | " + java.time.LocalTime.now() + "] " + message);
    }
}