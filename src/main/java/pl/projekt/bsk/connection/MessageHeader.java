package pl.projekt.bsk.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageHeader implements Serializable {
    private String filename;
    private long fileSize;
    private byte messageType;
    private byte encryptionMethod;
    private byte[] iv;
}
