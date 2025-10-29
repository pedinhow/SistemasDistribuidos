package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class CalculatorClient {

    private final String directoryHost = "localhost";
    private final int directoryPort = 5000;

    public static void main(String[] args) {
        new CalculatorClient().run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Cliente de Calculadora Distribuída iniciado.");
        System.out.println("Use o formato: OPERACAO NUM1 NUM2 (ex: SOMA 10 5)");
        System.out.println("Operações: SOMA, SUBTRACAO, MULTIPLICACAO, DIVISAO");
        System.out.println("Digite 'SAIR' para fechar.");

        while (true) {
            try {
                System.out.print("\n> ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("SAIR")) break;

                String[] parts = input.split(" ");
                if (parts.length != 3) {
                    System.out.println("Formato inválido. Ex: SOMA 10 5");
                    continue;
                }
                String operation = parts[0].toUpperCase();
                String num1 = parts[1];
                String num2 = parts[2];

                // descobrir o serviço no Diretório
                String serviceAddress = discoverService(operation);
                if ("NOT_FOUND".equals(serviceAddress)) {
                    System.out.println("Erro: Serviço '" + operation + "' não encontrado no diretório.");
                    continue;
                }

                System.out.println("Serviço '" + operation + "' descoberto em: " + serviceAddress + " (via Round Robin)");

                // chamar o serviço de calculadora
                String result = callCalculator(serviceAddress, operation, num1, num2);
                System.out.println("Resultado: " + result);

            } catch (Exception e) {
                System.err.println("Erro na comunicação: " + e.getMessage());
            }
        }
        scanner.close();
        System.out.println("Cliente encerrado.");
    }

    private String discoverService(String serviceName) throws IOException {
        try (Socket socket = new Socket(directoryHost, directoryPort);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {

            outputStream.writeUTF("DISCOVER:" + serviceName);
            return inputStream.readUTF(); // retorna o endereço
        }
    }

    private String callCalculator(String serviceAddress, String op, String n1, String n2) throws IOException {
        String[] addressParts = serviceAddress.split(":");
        String host = addressParts[0];
        int port = Integer.parseInt(addressParts[1]);

        try (Socket socket = new Socket(host, port);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {

            outputStream.writeUTF(op + ":" + n1 + ":" + n2);
            return inputStream.readUTF(); // retorna o resultado
        }
    }
}