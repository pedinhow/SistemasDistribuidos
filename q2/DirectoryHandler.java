package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DirectoryHandler implements Runnable {

    private Socket socket;
    private DataInputStream fluxoEntrada;
    private DataOutputStream fluxoSaida;

    public DirectoryHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            fluxoEntrada = new DataInputStream(socket.getInputStream());
            fluxoSaida = new DataOutputStream(socket.getOutputStream());

            String message = fluxoEntrada.readUTF();

            if (message.startsWith("REGISTER:")) {
                // Formato: REGISTER:serviceName:ip:porta
                String[] parts = message.substring(9).split(":");
                String serviceName = parts[0];
                String address = parts[1] + ":" + parts[2];
                DirectoryServer.registerService(serviceName, address);
                fluxoSaida.writeUTF("OK");

            } else if (message.startsWith("DISCOVER:")) {
                // Formato: DISCOVER:serviceName
                String serviceName = message.substring(9);
                String address = DirectoryServer.discoverService(serviceName);
                fluxoSaida.writeUTF(address);
            }

        } catch (IOException e) {
            System.out.println("Conexão com " + socket.getInetAddress() + " perdida.");
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (fluxoEntrada != null) fluxoEntrada.close();
            if (fluxoSaida != null) fluxoSaida.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}