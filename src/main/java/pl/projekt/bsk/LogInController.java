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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class LogInController {
    @FXML
    private PasswordField passwordField;

    @FXML
    public void onLoginButtonClick() throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, InvalidKeySpecException {
        HelloApplication app = new HelloApplication();
        String password = passwordField.getText();

        String sha = EncryptionUtils.createSha256(password);

        File file = new File(Constants.PASSWORD_SHA_DIR);
        Scanner scanner = new Scanner(file);

        String passwordSha = scanner.nextLine();
        scanner.close();

        if(!sha.equals(passwordSha)){
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
