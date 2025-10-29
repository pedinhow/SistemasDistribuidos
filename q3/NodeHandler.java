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
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            String payload = in.readUTF();
            node.log("Mensagem recebida: " + payload);
            node.processMessage(payload);
        } catch (Exception e) {
            node.log("Erro no handler: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}