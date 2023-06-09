package pl.projekt.bsk;

import lombok.Getter;

import javax.crypto.spec.IvParameterSpec;

public final class Constants {
    public static final int BUFFER_SIZE = 1024;

    public static final String DEFAULT_FILES_DIR = System.getProperty("user.home") + "/Downloads/";

    public static final String PRIVATE_KEY_DIR = "src/main/resources/pl/projekt/bsk/private/key.private";

    public static final String PUBLIC_KEY_DIR = "src/main/resources/pl/projekt/bsk/public/key.public";

    public static final String PASSWORD_SHA_DIR = "src/main/resources/pl/projekt/bsk/password";

    public static final int SECRET_KEY_SIZE = 128;

    public static final int RSA_KEY_SIZE = 1024;

    private static final byte[] ivBytes = {-110, 52, -81, -61, 69, 40, 31, 96, 87, 71, 102, -125, 62, -17, -18, -102};

    public static final IvParameterSpec PRIVATE_KEY_IV = new IvParameterSpec(ivBytes);

    public static final String PASSWORD = "password";

    //TODO zrobić enuma z typami plików, wysyłać wartości z enuma, a nie stringi

    private Constants(){} //Prevents class from being instantiated
}
