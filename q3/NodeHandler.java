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

            String[] parts = payload.split(";");
            if (parts[0].equals("SEARCH")) {
                node.processMessage(payload);
            } else if (parts[0].equals("FOUND")) {
                node.log("!!! SUCESSO !!! Arquivo '" + parts[1] + "' encontrado no nó " + parts[2]);
            }

        } catch (Exception e) {
            node.log("Erro no handler: " + e.getMessage());
        }
    }
}