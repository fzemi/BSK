package pl.projekt.bsk.connection;

import lombok.Setter;
import pl.projekt.bsk.Constants;
import pl.projekt.bsk.KeyStorage;
import pl.projekt.bsk.utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

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
        this.receivedFileDirectory = receivedFileDirectory == null ? (System.getProperty("user.home") + "\\Downloads\\") : receivedFileDirectory.getAbsolutePath() + "\\";
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();

            in = socket.getInputStream();
            out = socket.getOutputStream();
            out.write("connected".getBytes(StandardCharsets.UTF_8), 0, 9);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveFile() throws Exception {
        int bytes = 0;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int headerSize = receiveSize();
            byte[] encryptedHeaderBytes = new byte[headerSize];
            in.read(encryptedHeaderBytes, 0, headerSize);
            MessageHeader header = EncryptionUtils.decryptMessageHeader(encryptedHeaderBytes, KeyStorage.getSessionKey().get());

            System.out.println(header.getFilename() + " " + header.getFileSize());

            // receive file from client
            byte[] buffer = new byte[BUFFER_SIZE];
            while (header.getFileSize() > 0 && outputStream.size() < header.getFileSize() &&
                    (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, header.getFileSize()))) != -1) {
                outputStream.write(buffer, 0, bytes);
                System.out.println("Received " + bytes + " bytes");
                outputStream.flush();
            }

            byte[] decodedBytes;
            if(header.getEncryptionMethod() == Constants.ENCRYPTION_TYPE_CBC) {
                decodedBytes = EncryptionUtils.decryptData("AES/CBC/PKCS5Padding", outputStream.toByteArray(),
                        KeyStorage.getSessionKey().get(), new IvParameterSpec(header.getIv()));
            } else {
                decodedBytes = EncryptionUtils.decryptData("AES/ECB/PKCS5Padding", outputStream.toByteArray(),
                        KeyStorage.getSessionKey().get(), null);
            }

            String path = receivedFileDirectory + header.getFilename();
            Files.write(new File(path).toPath(), decodedBytes);

            System.out.println("Pobrano plik");

            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendSessionKey()  {
        try {
            System.out.println("Waiting for public key...");
            int keySize = receiveSize();
            byte[] publicKeyBytes = new byte[keySize];
            in.read(publicKeyBytes, 0, keySize);
            System.out.println("Received public key");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            KeyStorage.setReceivedPublicKey(Optional.ofNullable(kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes))));

            SecretKey sessionKey = EncryptionUtils.generateKey();
            KeyStorage.setSessionKey(Optional.of(sessionKey));

            byte[] encodedSessionKeyBytes = EncryptionUtils.encryptSessionKey(KeyStorage.getSessionKey().get(),
                    KeyStorage.getReceivedPublicKey().get());

            sendSize(encodedSessionKeyBytes.length);

            out.write(encodedSessionKeyBytes, 0, encodedSessionKeyBytes.length);
            out.flush();
            System.out.println("Sent session key");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidKeyException | BadPaddingException | IllegalBlockSizeException |
                 NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private int receiveSize() throws IOException {
        byte[] sizeBytes = new byte[4];
        in.read(sizeBytes, 0, 4);
        return ByteBuffer.wrap(sizeBytes).getInt();
    }

    private void sendSize(int size) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(size).array());
    }

    @Override
    public void run() {
        start();

        if (KeyStorage.getSessionKey().isEmpty())
            sendSessionKey();

        while(!Thread.interrupted()) {
            try {
                receiveFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
