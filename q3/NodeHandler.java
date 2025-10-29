package q3;

import java.io.DataInputStream;
import java.net.Socket;

public class NodeHandler implements Runnable {

    private final Socket socket;
    private final P2PNode node;

    public NodeHandler(Socket socket, P2PNode node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String payload = dis.readUTF();
            node.log("Recebido: " + payload);

            if (payload.startsWith("SEARCH")) {
                node.processMessage(payload);
            } else if (payload.startsWith("FOUND")) {
                handleFound(payload.split(";"));
            }
        } catch (Exception e) {
            node.log("Erro handler: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception e) {}
        }
    }

    private void handleFound(String[] parts) {
        if (parts.length < 3) return;
        String file = parts[1];
        String foundAt = parts[2];
        node.log("!!! SUCESSO !!! O arquivo '" + file + "' foi encontrado no Nó " + foundAt);
    }
}