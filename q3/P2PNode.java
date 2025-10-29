package q3;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class P2PNode implements Runnable {

    private final int nodeId;
    private final int port;
    private final String successorHost;
    private final int successorPort;
    private final String predecessorHost;
    private final int predecessorPort;
    private final Set<String> localFiles = new HashSet<>();

    public P2PNode(int nodeId, int port, String successorHost, int successorPort,
                   String predecessorHost, int predecessorPort) {
        this.nodeId = nodeId;
        this.port = port;
        this.successorHost = successorHost;
        this.successorPort = successorPort;
        this.predecessorHost = predecessorHost;
        this.predecessorPort = predecessorPort;
        initializeFiles();
    }

    public P2PNode(int nodeId, int port, int successorPort) throws Exception {
        // usado pelo RingLauncher (localhost)
        this.nodeId = nodeId;
        this.port = port;
        this.successorPort = successorPort;
        this.successorHost = "localhost";
        this.predecessorPort = port - 1 >= 6000 ? port - 1 : 6005;
        this.predecessorHost = "localhost";
        initializeFiles();
    }

    @Override
    public void run() {
        log("Nó iniciado. Porta: " + port + " | Sucessor: " + successorPort + " | Predecessor: " + predecessorPort);
        new Thread(this::startServer).start();
        startConsole();
    }

    private void startConsole() {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            System.out.println("Para buscar: SEARCH <arquivo> HORARIO|ANTIHORARIO ou SAIR");
            while ((input = console.readLine()) != null) {
                if (input.equalsIgnoreCase("SAIR")) break;
                if (input.toUpperCase().startsWith("SEARCH")) {
                    String[] parts = input.split(" ");
                    if (parts.length == 3) {
                        String file = parts[1].trim();
                        String direction = parts[2].trim().toUpperCase();
                        startSearch(file, direction);
                    } else {
                        log("Erro na entrada de dados. Tente outra vez!");
                    }
                } else {
                    log("Comando desconhecido.");
                }
            }
        } catch (Exception e) {
            log("Erro no console: " + e.getMessage());
        }
    }

    public void startSearch(String file) {
        startSearch(file, "HORARIO");
    }

    public void startSearch(String file, String direction) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            String payload = "SEARCH;" + file + ";" + nodeId + ";" + host + ";" + port + ";" + direction;
            processMessage(payload);
        } catch (Exception e) {
            log("Erro ao iniciar busca: " + e.getMessage());
        }
    }

    private void startServer() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                new Thread(new NodeHandler(client, this)).start();
            }
        } catch (Exception e) {
            log("ERRO no servidor: " + e.getMessage());
        }
    }

    public void processMessage(String payload) {
        try {
            String[] parts = payload.split(";");
            if (parts.length < 6 || !parts[0].equals("SEARCH")) {
                log("Mensagem mal formatada: " + payload);
                return;
            }

            String file = parts[1];
            int originId = Integer.parseInt(parts[2]);
            String originHost = parts[3];
            int originPort = Integer.parseInt(parts[4]);
            String direction = parts[5];

            if (isResponsible(file)) {
                log("ARQUIVO ENCONTRADO! '" + file + "' neste nó (" + nodeId + ")");
                sendMessage(originHost, originPort, "FOUND;" + file + ";" + nodeId);
            } else {
                String nextHost;
                int nextPort;
                if (direction.equals("HORARIO")) {
                    nextHost = successorHost;
                    nextPort = successorPort;
                } else {
                    nextHost = predecessorHost;
                    nextPort = predecessorPort;
                }
                log("Encaminhando '" + file + "' para " + nextPort + " [" + direction + "]");
                sendMessage(nextHost, nextPort, payload);
            }

        } catch (Exception e) {
            log("Erro processando mensagem: " + e.getMessage());
        }
    }

    public void sendMessage(String host, int port, String payload) {
        try (Socket socket = new Socket(host, port);
             OutputStream os = socket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os)) {
            dos.writeUTF(payload);
            log("Enviado para " + host + ":" + port + " -> " + payload);
        } catch (Exception e) {
            log("Erro enviando mensagem para " + host + ":" + port + " -> " + e.getMessage());
        }
    }

    private boolean isResponsible(String file) {
        int min = nodeId * 10 + 1;
        int max = nodeId * 10 + 10;
        try {
            int fileNum = Integer.parseInt(file.replaceAll("(?i)arquivo", ""));
            return fileNum >= min && fileNum <= max;
        } catch (Exception e) {
            return false;
        }
    }

    private void initializeFiles() {
        int min = nodeId * 10 + 1;
        int max = nodeId * 10 + 10;
        log("Responsável pelos arquivos " + min + " a " + max);
    }

    public void log(String msg) {
        System.out.println("[" + nodeId + " | " + java.time.LocalTime.now() + "] " + msg);
    }
}
