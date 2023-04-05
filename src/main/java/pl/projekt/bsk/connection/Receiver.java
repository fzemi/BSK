package pl.projekt.bsk.connection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Receiver implements Runnable {
    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    private OutputStream out;
    private InputStream in;

    private String receivedFileDirectory;  // TODO: set this value

    public Receiver(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();

            in = socket.getInputStream();
            out = socket.getOutputStream();
            out.write("connected".getBytes(StandardCharsets.UTF_8), 0, 9);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveFile() throws Exception {
        int bytes = 0;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(receivedFileDirectory);

            // receive file from client
            byte[] buffer = new byte[1024];  // TODO: change this value to constant (create file with CONST values)
            while ((bytes = in.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes);
                fileOutputStream.flush();
            }
            fileOutputStream.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        start();
        while(!Thread.interrupted()) {
            try {
                receiveFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
