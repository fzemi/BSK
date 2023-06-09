package pl.projekt.bsk.connection;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import pl.projekt.bsk.Constants;
import pl.projekt.bsk.KeyStorage;
import pl.projekt.bsk.utils.EncryptionUtils;

import javax.crypto.spec.IvParameterSpec;

import static pl.projekt.bsk.Constants.*;

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

            // send encoded file header to client
            MessageHeader header = new MessageHeader(file.getName(), file.length(), MESSAGE_TYPE_FILE, ENCRYPTION_TYPE_CBC, iv.getIV());
            byte[] encryptedHeaderBytes = EncryptionUtils.encryptMessageHeader(header, KeyStorage.getSessionKey());
            sendSize(encryptedHeaderBytes.length);
            out.write(encryptedHeaderBytes, 0, encryptedHeaderBytes.length);

            // send encoded file to client
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

    private void receiveSessionKey(){
        try {
            System.out.println("Sending public key");
            int publicKeySize = KeyStorage.getPublicKey().getEncoded().length;
            sendSize(publicKeySize);
            out.write(KeyStorage.getPublicKey().getEncoded(), 0, publicKeySize);
            out.flush();

            int encryptedSessionKeySize = receiveSize();
            byte[] encryptedSessionKeyBytes = new byte[encryptedSessionKeySize];
            in.read(encryptedSessionKeyBytes, 0, encryptedSessionKeySize);

            KeyStorage.setReceivedSessionKey(Optional.of(EncryptionUtils.decryptSessionKey(encryptedSessionKeyBytes, KeyStorage.getPrivateKey())));
            System.out.println("Received session key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startConnection();

        if(KeyStorage.getReceivedSessionKey().isEmpty())
            receiveSessionKey();
    }

    private int receiveSize() throws IOException {
        byte[] sizeBytes = new byte[4];
        in.read(sizeBytes, 0, 4);
        return ByteBuffer.wrap(sizeBytes).getInt();
    }

    private void sendSize(int size) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(size).array());
    }
}
