package q1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            // identifica o tipo do cliente
            String clientType = inputStream.readUTF();

            if ("SUBSCRIBE".equals(clientType)) {
                System.out.println("Cliente " + socket.getInetAddress() + " se inscreveu para atualizações.");
                MiniDNSServer.subscribers.add(this);
                handleSubscribedClient();
            } else if ("REGISTER".equals(clientType)) {
                System.out.println("Cliente " + socket.getInetAddress() + " se conectou como Registrador.");
                handleRegisterClient();
            }

        } catch (IOException e) {
            System.out.println("Cliente " + socket.getInetAddress() + " desconectou.");
        } finally {
            MiniDNSServer.subscribers.remove(this);
            closeConnections();
        }
    }

    // loop dos clientes requisitantes (query/subscribe)
    private void handleSubscribedClient() throws IOException {
        String message;
        while ((message = inputStream.readUTF()) != null) {
            if (message.startsWith("QUERY:")) {
                String name = message.substring(6);
                String ip = MiniDNSServer.dnsMap.getOrDefault(name, "NOT_FOUND");
                sendMessage("[RESPOSTA] IP de " + name + ": " + ip);
            }
        }
    }

    // loop para clientes registradores (update)
    private void handleRegisterClient() throws IOException {
        String message;
        while ((message = inputStream.readUTF()) != null) {
            if (message.startsWith("UPDATE:")) {
                // Formato: UPDATE:nome:ip [cite: 82, 234]
                String[] parts = message.substring(7).split(":");
                if (parts.length == 2) {
                    String name = parts[0];
                    String ip = parts[1];

                    MiniDNSServer.dnsMap.put(name, ip);
                    System.out.println("REGISTRO ATUALIZADO: " + name + " -> " + ip);
                    sendMessage("[OK] Registro de " + name + " atualizado.");

                    // dispara a notificação para todos os assinantes
                    MiniDNSServer.notifySubscribers(name, ip);
                }
            }
        }
    }

    // metodo para enviar mensagem para este cliente
    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem para " + socket.getInetAddress() + ": " + e.getMessage());
            MiniDNSServer.subscribers.remove(this);
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}