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

    public KeyStorage(KeyPair keyPair, SecretKey sessionKey) {
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        receivedPublicKey = Optional.empty();
    }
}
