package q1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class QueryClient {

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int porta = 5000;

        // Conecta ao servidor
        Socket socket = new Socket(host, porta);

        DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream());
        DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream());

        // 1. Se inscreve para receber atualizações
        fluxoSaida.writeUTF("SUBSCRIBE");
        System.out.println("Conectado ao Mini-DNS como Requisitante (assinante).");
        System.out.println("Digite um nome (ex: servidor1) para consultar o IP.");
        System.out.println("Aguardando consultas ou atualizações do servidor...");

        // 2. Inicia a thread para ouvir o servidor
        ServerListener listener = new ServerListener(fluxoEntrada);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        // 3. Loop principal para enviar consultas (entrada do usuário)
        Scanner teclado = new Scanner(System.in);
        try {
            while (teclado.hasNextLine()) {
                String nome = teclado.nextLine();
                if (nome.equalsIgnoreCase("sair")) {
                    break;
                }
                // Envia a consulta
                fluxoSaida.writeUTF("QUERY:" + nome);
            }
        } finally {
            System.out.println("Fechando cliente.");
            teclado.close();
            socket.close();
        }
    }
}

// Classe Runnable para ouvir o servidor em uma thread separada
class ServerListener implements Runnable {
    private DataInputStream fluxoEntrada;

    public ServerListener(DataInputStream fluxoEntrada) {
        this.fluxoEntrada = fluxoEntrada;
    }

    @Override
    public void run() {
        try {
            String messageFromServer;
            // Fica em loop lendo mensagens do servidor
            while ((messageFromServer = fluxoEntrada.readUTF()) != null) {
                // Imprime qualquer mensagem recebida (seja resposta ou push notification)
                System.out.println(messageFromServer);
            }
        } catch (IOException e) {
            System.out.println("Conexão com o servidor perdida.");
        }
    }
}