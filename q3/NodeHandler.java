package q3;

import java.io.DataInputStream;
import java.net.Socket;

public class NodeHandler implements Runnable {

    private final Socket socket;
    private final P2PNode node; // Referência de volta ao nó principal

    public NodeHandler(Socket socket, P2PNode node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            String payload = inputStream.readUTF();
            node.log("Mensagem recebida (bruta): " + payload);

            String[] parts = payload.split(";");

            // Lógica de roteamento: SEARCH ou FOUND
            if (parts[0].equals("SEARCH")) {
                node.processMessage(payload); // Deixa o nó principal processar
            } else if (parts[0].equals("FOUND")) {
                handleFound(parts);
            }

        } catch (Exception e) {
            node.log("ERRO no Handler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) { /* ignora */ }
        }
    }

    private void handleFound(String[] parts) {
        // Formato: "FOUND;ARQUIVO;ID_ONDE_ACHOU"
        if (parts.length < 3) return;
        String file = parts[1];
        String foundAtNodeId = parts[2];

        // Esta é a resposta final chegando na origem
        node.log("!!! SUCESSO DA BUSCA !!!");
        node.log("O arquivo '" + file + "' foi localizado no Nó " + foundAtNodeId + ".");
    }
}