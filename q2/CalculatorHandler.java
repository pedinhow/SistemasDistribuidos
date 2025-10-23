package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CalculatorHandler implements Runnable {

    private Socket socket;

    public CalculatorHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream fluxoEntrada = new DataInputStream(socket.getInputStream());
             DataOutputStream fluxoSaida = new DataOutputStream(socket.getOutputStream())) {

            // Formato esperado: OPERACAO:num1:num2 (ex: "SOMA:10:5")
            String request = fluxoEntrada.readUTF();

            String[] parts = request.split(":");
            if (parts.length != 3) {
                fluxoSaida.writeUTF("ERRO: Formato inválido. Use OPERACAO:num1:num2");
                return;
            }

            String operation = parts[0].toUpperCase();
            double num1 = Double.parseDouble(parts[1]);
            double num2 = Double.parseDouble(parts[2]);
            double result = 0;

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
                        fluxoSaida.writeUTF("ERRO: Divisão por zero.");
                        return;
                    }
                    result = num1 / num2;
                    break;
                default:
                    fluxoSaida.writeUTF("ERRO: Operação desconhecida.");
                    return;
            }

            // Envia o resultado
            fluxoSaida.writeUTF(String.valueOf(result));

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
