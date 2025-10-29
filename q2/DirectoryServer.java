package q2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryServer {

    // Mapa thread-safe: "NomeDoServico" -> Lista de "ip:porta" [cite: 88, 249]
    static Map<String, List<String>> serviceRegistry = new ConcurrentHashMap<>();

    // Contador atômico para o balanceamento Round Robin [cite: 97, 258]
    static AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        System.out.println("Servidor de Diretório (Base) rodando na porta " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nova conexão: " + socket.getInetAddress());

                // Cria uma nova thread para lidar com a conexão
                DirectoryHandler handler = new DirectoryHandler(socket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        }
    }

    /**
     * Registra um novo serviço.
     */
    public static void registerService(String serviceName, String address) {
        serviceRegistry.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>())
                .add(address);
        System.out.println("[REGISTRO] Serviço " + serviceName + " registrado em " + address);
    }

    /**
     * [cite_start]Descobre um serviço usando balanceamento Round Robin. [cite: 92-97, 253-258]
     */
    public static String discoverService(String serviceName) {
        List<String> addresses = serviceRegistry.get(serviceName);

        if (addresses == null || addresses.isEmpty()) {
            System.out.println("[DESCOBERTA] Serviço " + serviceName + " não encontrado.");
            return "NOT_FOUND";
        }

        // Lógica do Round Robin
        int index = roundRobinCounter.getAndIncrement() % addresses.size();
        String address = addresses.get(index);

        System.out.println("[DESCOBERTA] Serviço " + serviceName + " requisitado. " +
                "Encaminhando para (Índice " + index + "): " + address);
        return address;
    }
}