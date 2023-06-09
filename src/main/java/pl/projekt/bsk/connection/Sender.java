package pl.projekt.bsk.connection;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.stream.Stream;

import lombok.Getter;
import pl.projekt.bsk.KeyStorage;
import pl.projekt.bsk.utils.EncryptionUtils;

import javax.crypto.spec.IvParameterSpec;

import static pl.projekt.bsk.Constants.BUFFER_SIZE;

public class Sender implements Runnable {
    private Socket socket;
    private String ip;
    private int port;
    private InputStream in;
    private OutputStream out;

    @Getter
    private boolean connected = false;


    public Sender(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void startConnection() {
        try {
            socket = new Socket(ip, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();

            byte[] connectionCheck = new byte[BUFFER_SIZE];
            in.read(connectionCheck, 0, 9);

            if(!(new String(connectionCheck, 0, 9).equals("connected"))) {
                throw new Exception("Connection failed");
            }
            connected = true;
            System.out.println("Connected to: " + ip + ":" + port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) throws Exception {
        int bytes = 0;

        try {
            String algorithm = "AES/CBC/PKCS5Padding";

            IvParameterSpec iv = EncryptionUtils.generateIv();

            byte[] fileBytesEncoded = EncryptionUtils.encryptData(algorithm, Files.readAllBytes(file.toPath()),
                    KeyStorage.getSessionKey(), iv);

            InputStream fileBytesEncodedStream = new ByteArrayInputStream(fileBytesEncoded);
            long fileSize = file.length();

            //TODO dodać wysyłanie typu i nazwy plików
            byte[] header = ByteBuffer.allocate(Long.BYTES + 16).putLong(fileSize).put(Long.BYTES, iv.getIV()).array();
            out.write(header);


            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytes = fileBytesEncodedStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
//                out.flush();
            }


            fileBytesEncodedStream.close();
            System.out.println("Koniec wysyłania");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startConnection();
    }
}
