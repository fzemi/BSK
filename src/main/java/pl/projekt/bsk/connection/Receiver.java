package pl.projekt.bsk.connection;

import lombok.Setter;
import pl.projekt.bsk.KeyStorage;
import pl.projekt.bsk.utils.EncryptionUtils;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static pl.projekt.bsk.Constants.BUFFER_SIZE;

public class Receiver implements Runnable {
    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    private OutputStream out;
    private InputStream in;

    @Setter
    private String receivedFileDirectory;

    public Receiver(int port, File receivedFileDirectory) {
        this.port = port;
        this.receivedFileDirectory = receivedFileDirectory == null ? (System.getProperty("user.home") + "/Downloads/") : receivedFileDirectory.getAbsolutePath();
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

        DataInputStream dataIn = new DataInputStream(in);
        long fileSize = dataIn.readLong();
        IvParameterSpec iv = new IvParameterSpec(dataIn.readNBytes(16));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // receive file from client
            byte[] buffer = new byte[BUFFER_SIZE];
            while (fileSize > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                outputStream.write(buffer, 0, bytes);
//                fileOutputStream.flush();
            }

            String algorithm = "AES/CBC/PKCS5Padding";
            byte[] decodedBytes = EncryptionUtils.decryptData(algorithm, outputStream.toByteArray(),
                    KeyStorage.getSessionKey(), iv);

            Files.write(new File(receivedFileDirectory + "receivedFile.txt").toPath(), decodedBytes);

            outputStream.close();


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
