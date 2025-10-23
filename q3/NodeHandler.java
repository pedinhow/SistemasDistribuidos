package q3;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class NodeHandler implements Runnable {

    private final P2PNode node;
    private final Socket socket;

    public NodeHandler(P2PNode node, Socket socket) {
        this.node = node;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream())) {

            String message = fluxoEntrada.readUTF();
            node.log("(Log de Recebimento) Mensagem recebida: '" + message + "'"); //

            String[] parts = message.split(":");
            if (parts.length < 2) {
                node.log("ERRO: Mensagem mal formatada recebida.");
                return;
            }

            String command = parts[0];
            String fileName = parts[1];

            switch (command) {
                case "SEARCH":
                    handleSearch(fileName, parts);
                    break;
                case "FOUND":
                    handleFound(fileName, parts);
                    break;
                default:
                    node.log("ERRO: Comando desconhecido '" + command + "'.");
            }

        } catch (IOException e) {
            node.log("ERRO: Falha na thread handler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) { /* Ignora */ }
        }
    }

    /**
     * Processa uma mensagem de busca.
     *
     */
    private void handleSearch(String fileName, String[] parts) {
        if (parts.length != 3) {
            node.log("ERRO: Mensagem SEARCH mal formatada.");
            return;
        }

        int originPort = Integer.parseInt(parts[2]);

        // 1. Verifica se o arquivo está neste nó
        if (node.hasFile(fileName)) {
            node.log("...Arquivo '" + fileName + "' ENCONTRADO! Respondendo para origem (Porta " + originPort + ").");

            // Envia a resposta de volta para a ORIGEM
            String response = "FOUND:" + fileName + ":" + node.id;
            node.sendMessage(originPort, response);

        } else {
            // 2. Se não está, repassa para o SUCESSOR
            node.log("...Arquivo '" + fileName + "' não encontrado. Repassando para sucessor (Porta " + node.successorPort + ").");

            // Repassa a mensagem original, sem alterá-la
            String originalMessage = "SEARCH:" + fileName + ":" + originPort;
            node.sendMessage(node.successorPort, originalMessage);
        }
    }

    /**
     * Processa uma mensagem de resposta (FOUND).
     */
    private void handleFound(String fileName, String[] parts) {
        if (parts.length != 3) {
            node.log("ERRO: Mensagem FOUND mal formatada.");
            return;
        }

        String foundAtNodeId = parts[2];

        // Este é o nó que iniciou a busca. Imprime a resposta final.
        node.log("!!! SUCESSO DA BUSCA !!!");
        node.log("O arquivo '" + fileName + "' foi localizado no Nó P" + foundAtNodeId + ".");
    }
}
