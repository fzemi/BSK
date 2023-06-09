package pl.projekt.bsk;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

public class KeyStorage {

    @Getter(onMethod_={@Synchronized})
    @Setter(onMethod_={@Synchronized})
    private static PublicKey publicKey;

    @Getter(onMethod_={@Synchronized})
    @Setter(onMethod_={@Synchronized})
    private static PrivateKey privateKey;

    @Getter(onMethod_={@Synchronized})
    @Setter(onMethod_={@Synchronized})
    private static Optional<SecretKey> sessionKey;

    @Getter(onMethod_={@Synchronized})
    @Setter(onMethod_={@Synchronized})
    private static Optional<PublicKey> receivedPublicKey;

//    @Getter(onMethod_={@Synchronized})
//    @Setter(onMethod_={@Synchronized})
//    private static Optional<SecretKey> receivedSessionKey;

    public KeyStorage(KeyPair keyPair, SecretKey sessionKey) {
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
//        KeyStorage.sessionKey = sessionKey;
        receivedPublicKey = Optional.empty();
//        receivedSessionKey = Optional.empty();
    }

//    public static synchronized void setReceivedPublicKey(PublicKey receivedPublicKey) {
//        receivedPublicKey = Optional.of(receivedPublicKey);
//    }

//    public synchronized void setReceivedSessionKey(SecretKey sessionKey) {
//        this.receivedSessionKey = Optional.of(sessionKey);
//    }

//    public static synchronized PublicKey getReceivedPublicKey() {
//        while (receivedPublicKey.isEmpty()) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return receivedPublicKey.get();
//    }
//
//    public synchronized SecretKey getReceivedSessionKey() {
//        while (receivedSessionKey.isEmpty()) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return receivedSessionKey.get();
//    }
}
