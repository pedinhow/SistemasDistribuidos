package q1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MiniDNSServer {

    // Mapa thread-safe para armazenar os registros DNS
    static Map<String, String> dnsMap = new ConcurrentHashMap<>();

    // Lista thread-safe para manter os clientes "assinantes" (Requisitantes)
    static List<ClientHandler> subscribers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        // Preenche o mapa inicial
        initializeDNS();

        int porta = 5000;
        // Cria o socket servidor [cite: 1218]
        ServerSocket serverSocket = new ServerSocket(porta);
        System.out.println("Servidor Mini-DNS rodando na porta " + porta);
        System.out.println("Registros DNS atuais: " + dnsMap);

        try {
            while (true) {
                // Aguarda e aceita conexões de clientes [cite: 1228, 768]
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão: " + clientSocket.getInetAddress().getHostAddress());

                // Cria uma nova thread para lidar com o cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static void initializeDNS() {
        // Dados iniciais conforme a prática [cite: 1793-1797]
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

    // Método para notificar todos os assinantes sobre uma atualização
    public static void broadcastUpdate(String name, String ip) {
        String updateMessage = "[PUSH_NOTIFICATION] Binding dinâmico: " + name + " agora é " + ip;
        System.out.println("Notificando " + subscribers.size() + " assinantes: " + updateMessage);

        for (ClientHandler subscriber : subscribers) {
            subscriber.sendMessage(updateMessage);
        }
    }
}
