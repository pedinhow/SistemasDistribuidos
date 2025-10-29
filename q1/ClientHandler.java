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

            // Identifica o tipo de cliente
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

    // Loop para clientes Requisitantes (Query/Subscribe)
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

    // Loop para clientes Registradores (Update)
    private void handleRegisterClient() throws IOException {
        String message;
        while ((message = inputStream.readUTF()) != null) {
            if (message.startsWith("UPDATE:")) {
                // Formato: UPDATE:nome:ip [cite: 82, 234]
                String[] parts = message.substring(7).split(":");
                if (parts.length == 2) {
                    String name = parts[0];
                    String ip = parts[1];

                    // (Opcional: A prática de SD não exigia essa trava, mas a de Segurança sim)
                    // if (!name.equals("servidor1") && !name.equals("servidor4") && !name.equals("servidor9")) {
                    //     sendMessage("[ERRO] Permissão negada para atualizar " + name);
                    //     continue;
                    // }

                    MiniDNSServer.dnsMap.put(name, ip);
                    System.out.println("REGISTRO ATUALIZADO: " + name + " -> " + ip);
                    sendMessage("[OK] Registro de " + name + " atualizado.");

                    // Dispara a notificação para todos os assinantes [cite: 83, 236]
                    MiniDNSServer.notifySubscribers(name, ip);
                }
            }
        }
    }

    // Método para enviar mensagem para este cliente
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