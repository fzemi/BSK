package pl.projekt.bsk;

import javafx.scene.control.Alert;
import pl.projekt.bsk.utils.EncryptionUtils;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class LogInController {
    @FXML
    private PasswordField passwordField;

    @FXML
    public void onLoginButtonClick() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        HelloApplication app = new HelloApplication();
        String password = passwordField.getText();

        byte[] sha = EncryptionUtils.createSha256(password);

        File file = new File(Constants.PASSWORD_SHA_DIR);
        byte[] shaFromFile = Files.readAllBytes(file.toPath());

        boolean correct = true;

        for(int i = 0; i < shaFromFile.length; i++){
            if(sha[i] != shaFromFile[i]){
                correct = false;
                break;
            }
        }

        if(!correct){
            passwordField.setText("");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Wrong password");
            alert.showAndWait();
        } else {
            EncryptionUtils.setUpKeys();
            app.changeScene("hello-view.fxml");
        }
    }
}
