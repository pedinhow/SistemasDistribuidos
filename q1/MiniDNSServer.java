package q1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MiniDNSServer {
    // mapa thread-safe para armazenar os registros DNS
    static Map<String, String> dnsMap = new ConcurrentHashMap<>();

    // lista thread-safe para manter os clientes requisitantes
    static List<ClientHandler> subscribers = new CopyOnWriteArrayList<>();
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        initializeDnsMap();
        System.out.println("Servidor Mini-DNS (Base) rodando na porta " + PORT);
        System.out.println("Registros DNS atuais: " + dnsMap);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Aguarda conexões
                System.out.println("Nova conexão: " + clientSocket.getInetAddress().getHostAddress());

                // cria uma nova thread para lidar com o cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
    }

    private static void initializeDnsMap() {
        // dados iniciais citados na prática offline
        dnsMap.put("servidor1", "192.168.0.10");
        dnsMap.put("servidor2", "192.168.0.20");
        dnsMap.put("servidor3", "192.168.0.30");
        dnsMap.put("servidor4", "192.168.0.40");
        dnsMap.put("servidor5", "192.168.0.50");
        dnsMap.put("servidor6", "192.168.0.60");
        dnsMap.put("servidor7", "192.168.0.70");
        dnsMap.put("servidor8", "192.168.0.80");
        dnsMap.put("servidor9", "192.168.0.90");
        dnsMap.put("servidor10", "192.168.0.100");
    }

    // metodo para notificar todos os assinantes sobre uma atualização
    public static void notifySubscribers(String name, String ip) {
        String updateMessage = "[PUSH_NOTIFICATION] Binding dinâmico: " + name + " agora é " + ip;
        System.out.println("Notificando " + subscribers.size() + " assinantes: " + updateMessage);

        for (ClientHandler subscriber : subscribers) {
            subscriber.sendMessage(updateMessage);
        }
    }
}