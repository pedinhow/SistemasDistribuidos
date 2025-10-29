package q1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class QueryClient {

    private static final String HOST = "172.17.232.64";
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());

        // se inscreve para receber atualizacoes
        outputStream.writeUTF("SUBSCRIBE");
        System.out.println("Conectado ao Mini-DNS como Requisitante (assinante).");
        System.out.println("Digite um nome (ex: servidor1) para consultar o IP.");
        System.out.println("Aguardando consultas ou atualizações do servidor...");

        // inicia a thread para ouvir o servidor
        Thread listenerThread = new Thread(new ServerListener(inputStream));
        listenerThread.start();

        // loop principal para enviar consultas
        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                if (name.equalsIgnoreCase("sair")) {
                    break;
                }
                outputStream.writeUTF("QUERY:" + name);
            }
        } finally {
            System.out.println("Fechando cliente.");
            scanner.close();
            socket.close();
        }
    }
}

// runnable para ouvir o servidor em uma thread separada
class ServerListener implements Runnable {
    private final DataInputStream inputStream;

    public ServerListener(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            String messageFromServer;
            while ((messageFromServer = inputStream.readUTF()) != null) {
                System.out.println(messageFromServer);
            }
        } catch (IOException e) {
            System.out.println("Conexão com o servidor perdida.");
        }
    }
}