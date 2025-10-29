package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CalculatorServer {

    private final int port;
    private static final String DIRECTORY_HOST = "172.17.232.64";
    private static final int DIRECTORY_PORT = 5000; // Porta do q2.DirectoryServer

    public CalculatorServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        // registrar-se no servidor de diretório
        registerWithDirectory();

        // iniciar o servidor multithread para atender clientes
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de Calculadora (Worker) rodando na porta " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Worker(" + port + "): Recebeu conexão do cliente " + clientSocket.getInetAddress());

                CalculatorHandler handler = new CalculatorHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        }
    }

    private void registerWithDirectory() {
        // serviços que este servidor oferece
        String[] services = {"SOMA", "SUBTRACAO", "MULTIPLICACAO", "DIVISAO"};
        System.out.println("Worker(" + port + "): Registrando serviços no Diretório...");

        for (String service : services) {
            try (Socket socket = new Socket(DIRECTORY_HOST, DIRECTORY_PORT);
                 DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                 DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {

                // formato: REGISTER:serviceName:ip:porta [cite: 88]
                String registerMessage = "REGISTER:" + service + ":localhost:" + port;
                outputStream.writeUTF(registerMessage);

                String response = inputStream.readUTF();
                if ("OK".equals(response)) {
                    System.out.println("Worker(" + port + "): Serviço " + service + " registrado com sucesso.");
                } else {
                    System.out.println("Worker(" + port + "): Falha ao registrar " + service + ".");
                }

            } catch (IOException e) {
                System.err.println("Erro ao conectar ao Servidor de Diretório: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java q2.CalculatorServer <porta>");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            new CalculatorServer(port).startServer();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}