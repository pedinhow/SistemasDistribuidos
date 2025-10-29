package q2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DirectoryHandler implements Runnable {

    private final Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public DirectoryHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            String message = inputStream.readUTF();

            if (message.startsWith("REGISTER:")) {
                // formato: REGISTER:serviceName:ip:porta
                String[] parts = message.substring(9).split(":");
                String serviceName = parts[0];
                String address = parts[1] + ":" + parts[2];
                DirectoryServer.registerService(serviceName, address);
                outputStream.writeUTF("OK");

            } else if (message.startsWith("DISCOVER:")) {
                // formato: DISCOVER:serviceName
                String serviceName = message.substring(9);
                String address = DirectoryServer.discoverService(serviceName);
                outputStream.writeUTF(address);
            }

        } catch (IOException e) {
            System.out.println("Conexão com " + socket.getInetAddress() + " perdida.");
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}