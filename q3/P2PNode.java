package q3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class P2PNode {

    private final String nodeId;   // P0, P1, ...
    private final int port;
    private final String successorHost;
    private final int successorPort;
    private final String predecessorHost;
    private final int predecessorPort;
    private final String myIp;
    private final Set<String> localFiles = new HashSet<>();

    public P2PNode(String nodeId, int port, String successorHost, int successorPort,
                   String predecessorHost, int predecessorPort, String myIp) { // <-- MUDOU
        this.nodeId = nodeId;
        this.port = port;
        this.successorHost = successorHost;
        this.successorPort = successorPort;
        this.predecessorHost = predecessorHost;
        this.predecessorPort = predecessorPort;
        this.myIp = myIp;
        this.initializeFiles();
    }

    public static void main(String[] args) {
        if (args.length < 7) {
            System.err.println("Uso: java q3.P2PNode <id> <porta> <sucessorHost> <sucessorPort> <antecessorHost> <antecessorPort> <meuIp>");
            System.exit(1);
        }

        String id = args[0];
        int port = Integer.parseInt(args[1]);
        String succHost = args[2];
        int succPort = Integer.parseInt(args[3]);
        String predHost = args[4];
        int predPort = Integer.parseInt(args[5]);
        String myIp = args[6];

        P2PNode node = new P2PNode(id, port, succHost, succPort, predHost, predPort, myIp);
        node.start();
    }

    public void start() {
        log("Nó iniciado. IP: " + myIp + ", Porta: " + port + ", Sucessor: " + successorHost + ":" + successorPort + ", Antecessor: " + predecessorHost + ":" + predecessorPort);
        new Thread(this::startServer).start();
        startConsole();
    }

    private void startConsole() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Comandos: SEARCH <arquivoX> HORARIO|ANTIHORARIO | SAIR");
        while (true) {
            System.out.print(nodeId + "> ");
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("SAIR")) break;

            String[] parts = input.split(" ");
            if (parts.length != 3) {
                log("Erro na entrada de dados. Tente outra vez!");
                continue;
            }

            String command = parts[0];
            String file = parts[1];
            String direction = parts[2].toUpperCase();

            if (!command.equalsIgnoreCase("SEARCH") ||
                    (!direction.equals("HORARIO") && !direction.equals("ANTIHORARIO"))) {
                log("Erro na entrada de dados. Tente outra vez!");
                continue;
            }

            String payload = "SEARCH;" + file + ";" + nodeId + ";" + this.myIp + ";" + port + ";" + direction;
            processMessage(payload);
        }
        sc.close();
    }

    private void startServer() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                new Thread(new NodeHandler(client, this)).start();
            }
        } catch (IOException e) {
            log("Erro no servidor: " + e.getMessage());
        }
    }

    public void processMessage(String payload) {
        try {
            String[] parts = payload.split(";");
            if (parts.length < 6) {
                log("Mensagem: " + payload);
                return;
            }

            String type = parts[0];
            if (type.equals("SEARCH")) {
                String file = parts[1];
                String originId = parts[2];
                String originHost = parts[3];
                int originPort = Integer.parseInt(parts[4]);
                String direction = parts[5];

                if (isResponsible(file)) {
                    log("ARQUIVO ENCONTRADO! '" + file + "' neste nó (" + nodeId + ")");
                    String response = "FOUND;" + file + ";" + nodeId;
                    sendMessage(originHost, originPort, response);
                } else {
                    log("Arquivo '" + file + "' não está aqui. Encaminhando...");
                    if (direction.equals("HORARIO")) {
                        sendMessage(successorHost, successorPort, payload);
                    } else {
                        sendMessage(predecessorHost, predecessorPort, payload);
                    }
                }
            } else if (type.equals("FOUND")) {
                String file = parts[1];
                String foundAtNode = parts[2];
                log("!!! SUCESSO DA BUSCA !!!");
                log("O arquivo '" + file + "' foi localizado no Nó " + foundAtNode + ".");
            }

        } catch (Exception e) {
            log("Erro processando mensagem: " + e.getMessage());
        }
    }

    public void sendMessage(String host, int port, String payload) {
        try (Socket socket = new Socket(host, port);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
            outputStream.writeUTF(payload);
            log("Enviado: " + payload + " para " + host + ":" + port);
        } catch (Exception e) {
            log("Erro enviando mensagem: " + e.getMessage());
        }
    }

    private boolean isResponsible(String file) {
        int idNum = Integer.parseInt(nodeId.replace("P", ""));
        int fileNum = Integer.parseInt(file.replaceAll("(?i)arquivo", ""));
        int min = idNum * 10 + 1;
        int max = idNum * 10 + 10;
        return fileNum >= min && fileNum <= max;
    }

    private void initializeFiles() {
        int idNum = Integer.parseInt(nodeId.replace("P", ""));
        int min = idNum * 10 + 1;
        int max = idNum * 10 + 10;
        for (int i = min; i <= max; i++) localFiles.add("arquivo" + i);
        log("Arquivos locais: " + localFiles);
    }
    public void log(String msg) {
        System.out.println("[" + nodeId + " | " + java.time.LocalTime.now() + "] " + msg);
    }
}