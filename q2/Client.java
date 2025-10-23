package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private String directoryHost = "localhost";
    private int directoryPort = 5000;

    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        Scanner teclado = new Scanner(System.in);
        System.out.println("Cliente de Calculadora Distribuída iniciado.");
        System.out.println("Use o formato: OPERACAO NUM1 NUM2 (ex: SOMA 10 5)");
        System.out.println("Use 'DIVISAO' para dividir. Digite 'SAIR' para fechar.");

        while (true) {
            try {
                System.out.print("\n> ");
                String input = teclado.nextLine();

                if (input.equalsIgnoreCase("SAIR")) {
                    break;
                }

                String[] parts = input.split(" ");
                if (parts.length != 3) {
                    System.out.println(
                            "Formato inválido. Ex: SOMA 10 5 ou SUBTRACAO 100 20");
                    continue;
                }

                String operation = parts[0].toUpperCase();
                String num1 = parts[1];
                String num2 = parts[2];

                // 1. Descobrir o serviço no Diretório
                String serviceAddress = discoverService("Calculadora");

                if ("NOT_FOUND".equals(serviceAddress)) {
                    System.out.println("Erro: Serviço 'Calculadora' não encontrado no diretório.");
                    continue;
                }

                // 2. Chamar o serviço de calculadora
                String result = callCalculator(serviceAddress, operation, num1, num2);
                System.out.println("Resultado: " + result);

            } catch (Exception e) {
                System.err.println("Erro na comunicação: " + e.getMessage());
            }
        }
        teclado.close();
        System.out.println("Cliente encerrado.");
    }

    /**
     * Conecta ao Servidor de Diretório e pede a localização de um serviço.
     */
    private String discoverService(String serviceName) throws IOException {
        try (Socket socket = new Socket(directoryHost, directoryPort);
             DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream());
             DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream())) {

            // Envia o pedido de descoberta
            fluxoSaida.writeUTF("DISCOVER:" + serviceName);

            // Retorna o endereço (ip:porta) ou "NOT_FOUND"
            return fluxoEntrada.readUTF();
        }
    }

    /**
     * Conecta diretamente ao Servidor de Calculadora e executa a operação.
     */
    private String callCalculator(String serviceAddress, String op, String n1, String n2)
            throws UnknownHostException, IOException {

        String[] addressParts = serviceAddress.split(":");
        String host = addressParts[0];
        int port = Integer.parseInt(addressParts[1]);

        try (Socket socket = new Socket(host, port);
             DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream());
             DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream())) {

            // Envia a operação no formato "OPERACAO:num1:num2"
            fluxoSaida.writeUTF(op + ":" + n1 + ":" + n2);

            // Retorna o resultado
            return fluxoEntrada.readUTF();
        }
    }
}
