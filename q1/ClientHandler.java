package q1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// Implementa Runnable para ser executada em uma Thread
public class ClientHandler implements Runnable {

    private Socket socket;
    private DataOutputStream fluxoSaida;
    private DataInputStream fluxoEntrada;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Cria canais de entrada e saída [cite: 1169, 1180]
            fluxoEntrada = new DataInputStream(socket.getInputStream());
            fluxoSaida = new DataOutputStream(socket.getOutputStream());

            // Identifica o tipo de cliente
            String clientType = fluxoEntrada.readUTF();

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
            // Remove da lista de assinantes e fecha recursos
            MiniDNSServer.subscribers.remove(this);
            closeConnections();
        }
    }

    // Loop para clientes Requisitantes (Query/Subscribe)
    private void handleSubscribedClient() throws IOException {
        String message;
        while ((message = fluxoEntrada.readUTF()) != null) {
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
        while ((message = fluxoEntrada.readUTF()) != null) {
            if (message.startsWith("UPDATE:")) {
                // Formato: UPDATE:nome:ip
                String[] parts = message.substring(7).split(":");
                if (parts.length == 2) {
                    String name = parts[0];
                    String ip = parts[1];

                    // Atualiza o mapa principal
                    MiniDNSServer.dnsMap.put(name, ip);
                    System.out.println("REGISTRO ATUALIZADO: " + name + " -> " + ip);

                    // Envia a resposta de OK para o registrador
                    sendMessage("[OK] Registro de " + name + " atualizado.");

                    // Dispara a notificação para todos os assinantes
                    MiniDNSServer.broadcastUpdate(name, ip);
                }
            }
        }
    }

    // Método para enviar mensagem para este cliente
    public void sendMessage(String message) {
        try {
            fluxoSaida.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem para " + socket.getInetAddress() + ": " + e.getMessage());
            // Se falhar, remove da lista e fecha
            MiniDNSServer.subscribers.remove(this);
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (fluxoEntrada != null) fluxoEntrada.close();
            if (fluxoSaida != null) fluxoSaida.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
