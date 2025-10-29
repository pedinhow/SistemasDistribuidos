package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CalculatorHandler implements Runnable {

    private final Socket socket;

    public CalculatorHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            // Formato esperado: OPERACAO:num1:num2 (ex: "SOMA:10:5")
            String request = inputStream.readUTF();
            String[] parts = request.split(":");

            if (parts.length != 3) {
                outputStream.writeUTF("ERRO: Formato inválido. Use OPERACAO:num1:num2");
                return;
            }

            String operation = parts[0].toUpperCase();
            double num1 = Double.parseDouble(parts[1]);
            double num2 = Double.parseDouble(parts[2]);
            double result;

            switch (operation) {
                case "SOMA":
                    result = num1 + num2;
                    break;
                case "SUBTRACAO":
                    result = num1 - num2;
                    break;
                case "MULTIPLICACAO":
                    result = num1 * num2;
                    break;
                case "DIVISAO":
                    if (num2 == 0) {
                        outputStream.writeUTF("ERRO: Divisão por zero.");
                        return;
                    }
                    result = num1 / num2;
                    break;
                default:
                    outputStream.writeUTF("ERRO: Operação desconhecida.");
                    return;
            }
            outputStream.writeUTF(String.valueOf(result));

        } catch (IOException e) {
            System.out.println("Cliente " + socket.getInetAddress() + " encerrou.");
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter números.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
