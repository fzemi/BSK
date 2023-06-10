package pl.projekt.bsk.utils;

import pl.projekt.bsk.Constants;
import pl.projekt.bsk.KeyStorage;
import pl.projekt.bsk.connection.MessageHeader;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EncryptionUtils {

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(Constants.SECRET_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(Constants.RSA_KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] createSha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static void setUpKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException {
        File privateKeyFile = new File(Constants.PRIVATE_KEY_DIR);
        File publicKeyFile = new File(Constants.PUBLIC_KEY_DIR);

        if(privateKeyFile.length() == 0 || publicKeyFile.length() == 0){
            try {
                KeyPair keyPair = generateRsaKeyPair();

                //encrypt private key with password
                byte[] encryptedPrivateKey = encryptData("AES/CBC/PKCS5Padding", keyPair.getPrivate().getEncoded(),
                        new SecretKeySpec(Files.readAllBytes(new File(Constants.PASSWORD_SHA_DIR).toPath()), "AES"),
                        Constants.PRIVATE_KEY_IV);

                Files.write(privateKeyFile.toPath(), encryptedPrivateKey);
                Files.write(publicKeyFile.toPath(), keyPair.getPublic().getEncoded());

                KeyStorage.setPrivateKey(keyPair.getPrivate());
                KeyStorage.setPublicKey(keyPair.getPublic());
                KeyStorage.setSessionKey(Optional.empty());
                KeyStorage.setReceivedPublicKey(Optional.empty());

            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        else {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] decryptedPrivateKey = decryptData("AES/CBC/PKCS5Padding", Files.readAllBytes(privateKeyFile.toPath()),
                    new SecretKeySpec(Files.readAllBytes(new File(Constants.PASSWORD_SHA_DIR).toPath()), "AES"),
                    Constants.PRIVATE_KEY_IV);

            KeyStorage.setPrivateKey(kf.generatePrivate(new PKCS8EncodedKeySpec(decryptedPrivateKey)));
            KeyStorage.setPublicKey(kf.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(publicKeyFile.toPath()))));
            KeyStorage.setSessionKey(Optional.empty());
            KeyStorage.setReceivedPublicKey(Optional.empty());
        }
    }



    public static byte[] encryptData(String algorithm, byte[] input, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(input);
    }

    public static byte[] decryptData(String algorithm, byte[] cipherText, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        System.out.println("DATA: " + cipherText.length);
        return cipher.doFinal(cipherText);
    }

    public static byte[] encryptSessionKey(SecretKey sessionKey, PublicKey publicKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher encryptor = Cipher.getInstance("RSA");
        encryptor.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] sessionKeyBytes = sessionKey.getEncoded();
        return encryptor.doFinal(sessionKeyBytes);
    }

    public static SecretKey decryptSessionKey(byte[] ciphertext, PrivateKey privateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher decryptor = Cipher.getInstance("RSA");
        decryptor.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] sessionKeyBytes = decryptor.doFinal(ciphertext);
        return new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
    }

    public static byte[] encryptMessageHeader(MessageHeader header, SecretKey sessionKey) throws IOException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        ObjectMapper mapper = new ObjectMapper();
        String messageHeaderString = mapper.writeValueAsString(header);

        Cipher encrpytor = Cipher.getInstance("AES/ECB/PKCS5Padding");
        encrpytor.init(Cipher.ENCRYPT_MODE, sessionKey);

        byte[] headerBytes = messageHeaderString.getBytes(StandardCharsets.UTF_8);

        return encrpytor.doFinal(headerBytes);
    }

    public static MessageHeader decryptMessageHeader(byte[] ciphertext, SecretKey sessionKey) throws IOException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decryptor = Cipher.getInstance("AES/ECB/PKCS5Padding");
        decryptor.init(Cipher.DECRYPT_MODE, sessionKey);

        System.out.println("HEADER: " + ciphertext.length);

        byte[] headerBytes = decryptor.doFinal(ciphertext);
        String messageHeaderString = new String(headerBytes, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(messageHeaderString, MessageHeader.class);
    }
}
