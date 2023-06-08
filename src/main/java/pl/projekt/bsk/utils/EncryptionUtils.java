package pl.projekt.bsk.utils;

import pl.projekt.bsk.Constants;
import pl.projekt.bsk.KeyStorage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

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

    public static String createSha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void setUpKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File privateKeyFile = new File(Constants.PRIVATE_KEY_DIR);
        File publicKeyFile = new File(Constants.PUBLIC_KEY_DIR);

        if(privateKeyFile.length() == 0 || publicKeyFile.length() == 0){
            try {
                KeyPair keyPair = generateRsaKeyPair();

                //save encrypted private key to file
                Files.write(privateKeyFile.toPath(), keyPair.getPrivate().getEncoded());
                Files.write(publicKeyFile.toPath(), keyPair.getPublic().getEncoded());

                KeyStorage.setPrivateKey(keyPair.getPrivate());
                KeyStorage.setPublicKey(keyPair.getPublic());

            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        else {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            KeyStorage.setPrivateKey(kf.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(privateKeyFile.toPath()))));
            KeyStorage.setPublicKey(kf.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(publicKeyFile.toPath()))));
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
}
