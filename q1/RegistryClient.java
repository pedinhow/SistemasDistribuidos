package q1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RegistryClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        String host = "localhost";
        int porta = 5000;

        System.out.println("Conectando ao Mini-DNS como Registrador...");
        try (Socket socket = new Socket(host, porta);
             DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream());
             DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream())) {

            // 1. Se identifica como Registrador
            fluxoSaida.writeUTF("REGISTER");

            // 2. Envia as atualizações necessárias
            System.out.println("Enviando atualização para servidor1...");
            fluxoSaida.writeUTF("UPDATE:servidor1:192.168.0.111"); // Novo IP
            System.out.println(fluxoEntrada.readUTF()); // Lê "OK"

            Thread.sleep(1000); // Pausa para simulação

            System.out.println("Enviando atualização para servidor4...");
            fluxoSaida.writeUTF("UPDATE:servidor4:192.168.0.444"); // Novo IP
            System.out.println(fluxoEntrada.readUTF()); // Lê "OK"

            Thread.sleep(1000); // Pausa para simulação

            System.out.println("Enviando atualização para servidor9...");
            fluxoSaida.writeUTF("UPDATE:servidor9:192.168.0.999"); // Novo IP
            System.out.println(fluxoEntrada.readUTF()); // Lê "OK"

            System.out.println("Atualizações concluídas. Fechando cliente.");

        } catch (IOException e) {
            System.err.println("Erro no cliente registrador: " + e.getMessage());
        }
    }
}
