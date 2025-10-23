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

    // Mapa thread-safe: "NomeDoServico" -> Lista de "ip:porta"
    static Map<String, List<String>> serviceRegistry = new ConcurrentHashMap<>();

    // Contador atômico para o balanceamento Round Robin
    static AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        int porta = 5000;
        ServerSocket serverSocket = new ServerSocket(porta);
        System.out.println("Servidor de Diretório rodando na porta " + porta);

        try {
            while (true) {
                // Aguarda conexões (de Clientes ou Servidores de Calculadora)
                Socket socket = serverSocket.accept();
                System.out.println("Nova conexão: " + socket.getInetAddress());

                // Cria uma nova thread para lidar com a conexão
                DirectoryHandler handler = new DirectoryHandler(socket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } finally {
            serverSocket.close();
        }
    }

    /**
     * Registra um novo serviço ou adiciona um novo provedor a um serviço existente.
     */
    public static void registerService(String serviceName, String address) {
        // computeIfAbsent garante a criação thread-safe da lista
        serviceRegistry.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>())
                .add(address);
        System.out.println("[REGISTRO] Serviço " + serviceName + " registrado em " + address);
        System.out.println("Registro atual: " + serviceRegistry);
    }

    /**
     * Descobre um serviço usando balanceamento Round Robin.
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
