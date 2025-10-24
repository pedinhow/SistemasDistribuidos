package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class CalculatorServer {

    private int myPort;

    public CalculatorServer(int myPort) {
        this.myPort = myPort;
    }

    public void startServer() throws IOException {

        // 1. Iniciar o servidor multithread para atender clientes
        ServerSocket serverSocket = new ServerSocket(myPort);
        System.out.println("Servidor de Calculadora (Worker) rodando na porta " + myPort);

        // 2. Registrar-se no Servidor de Diretório
        registerWithDirectory();

        try {
            while (true) {
                // Aguarda conexões de clientes (redirecionados pelo Diretório)
                Socket clientSocket = serverSocket.accept();
                System.out.println("Worker(" + myPort + "): Recebeu conexão do cliente " + clientSocket.getInetAddress());

                // Cria uma thread para fazer o cálculo
                CalculatorHandler handler = new CalculatorHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private void registerWithDirectory() {
        String directoryHost = "localhost";
        int directoryPort = 5000; // Porta do q2.DirectoryServer

        try (Socket socket = new Socket(directoryHost, directoryPort);
             DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream());
             DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream())) {

            // Formato: REGISTER:serviceName:ip:porta
            String registerMessage = "REGISTER:Calculadora:localhost:" + myPort;
            fluxoSaida.writeUTF(registerMessage);

            String response = fluxoEntrada.readUTF();
            if ("OK".equals(response)) {
                System.out.println("Worker(" + myPort + "): Registrado com sucesso no Diretório.");
            } else {
                System.out.println("Worker(" + myPort + "): Falha ao registrar no Diretório.");
            }

        } catch (UnknownHostException e) {
            System.err.println("Host do diretório não encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao Servidor de Diretório: " + e.getMessage());
        }
    }

    /**
     * Argumento principal para definir a porta deste worker.
     * Ex: java q2.CalculatorServer 9001
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: Forneça a porta para este servidor.");
            System.out.println("Uso: java q2.CalculatorServer <porta>");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            CalculatorServer server = new CalculatorServer(port);
            server.startServer();
        } catch (NumberFormatException e) {
            System.out.println("Porta inválida.");
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor de calculadora: " + e.getMessage());
        }
    }
}

// TODO APARECE DUAS VEZES O 9001