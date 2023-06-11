package pl.projekt.bsk.connection;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.concurrent.Task;
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

    public void sendFile(File file, String cipherMode) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int bytes = 0;

                try {
                    byte[] fileBytesEncoded;
                    MessageHeader header;

                    if(cipherMode.equals("AES/CBC/PKCS5Padding")) {
                        IvParameterSpec iv = EncryptionUtils.generateIv();
                        fileBytesEncoded = EncryptionUtils.encryptData(cipherMode, Files.readAllBytes(file.toPath()),
                                KeyStorage.getSessionKey().get(), iv);
                        header = new MessageHeader(file.getName(), fileBytesEncoded.length, MESSAGE_TYPE_FILE, ENCRYPTION_TYPE_CBC, iv.getIV());
                    } else {
                        fileBytesEncoded = EncryptionUtils.encryptData(cipherMode, Files.readAllBytes(file.toPath()),
                                KeyStorage.getSessionKey().get(), null);
                        header = new MessageHeader(file.getName(), fileBytesEncoded.length, MESSAGE_TYPE_FILE, ENCRYPTION_TYPE_ECB, null);
                    }

                    InputStream fileBytesEncodedStream = new ByteArrayInputStream(fileBytesEncoded);

                    // send encoded file header to client
                    byte[] encryptedHeaderBytes = EncryptionUtils.encryptMessageHeader(header, KeyStorage.getSessionKey().get());
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
                return null;
            }
        };
        new Thread(task).start();
    }

    public void sendText(String text, String cipherMode) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int bytes = 0;

                try {
                    byte[] textBytesEncoded;
                    MessageHeader header;

                    if(cipherMode.equals("AES/CBC/PKCS5Padding")) {
                        IvParameterSpec iv = EncryptionUtils.generateIv();
                        textBytesEncoded = EncryptionUtils.encryptData(cipherMode, text.getBytes(StandardCharsets.UTF_8),
                                KeyStorage.getSessionKey().get(), iv);
                        header = new MessageHeader(null, textBytesEncoded.length, MESSAGE_TYPE_TEXT, ENCRYPTION_TYPE_CBC, iv.getIV());
                    } else {
                        textBytesEncoded = EncryptionUtils.encryptData(cipherMode, text.getBytes(StandardCharsets.UTF_8),
                                KeyStorage.getSessionKey().get(), null);
                        header = new MessageHeader(null, textBytesEncoded.length, MESSAGE_TYPE_TEXT, ENCRYPTION_TYPE_ECB, null);
                    }

                    InputStream fileBytesEncodedStream = new ByteArrayInputStream(textBytesEncoded);

                    // send encoded file header to client
                    byte[] encryptedHeaderBytes = EncryptionUtils.encryptMessageHeader(header, KeyStorage.getSessionKey().get());
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
                return null;
            }
        };
        new Thread(task).start();
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

            KeyStorage.setSessionKey(Optional.of(EncryptionUtils.decryptSessionKey(encryptedSessionKeyBytes, KeyStorage.getPrivateKey())));
            System.out.println("Received session key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startConnection();

        if(KeyStorage.getSessionKey().isEmpty())
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
