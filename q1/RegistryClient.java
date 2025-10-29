package q1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RegistryClient {

    private static final String HOST = "172.17.232.64";
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Conectando ao Mini-DNS como Registrador...");
        try (Socket socket = new Socket(HOST, PORT);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {

            // se identifica como Registrador
            outputStream.writeUTF("REGISTER");

            // envia as atualizações necessárias
            System.out.println("Enviando atualização para servidor1...");
            outputStream.writeUTF("UPDATE:servidor1:192.168.0.111");
            System.out.println(inputStream.readUTF());
            Thread.sleep(1000);

            System.out.println("Enviando atualização para servidor4...");
            outputStream.writeUTF("UPDATE:servidor4:192.168.0.444");
            System.out.println(inputStream.readUTF());
            Thread.sleep(1000);

            System.out.println("Enviando atualização para servidor9...");
            outputStream.writeUTF("UPDATE:servidor9:192.168.0.999");
            System.out.println(inputStream.readUTF());

            System.out.println("Atualizações concluídas. Fechando cliente.");

        } catch (IOException e) {
            System.err.println("Erro no cliente registrador: " + e.getMessage());
        }
    }
}