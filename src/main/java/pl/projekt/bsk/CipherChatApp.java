package pl.projekt.bsk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.projekt.bsk.utils.EncryptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

public class CipherChatApp extends Application {

    private static Stage stg;

    @Override
    public void start(Stage stage) throws IOException {
        stg = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(CipherChatApp.class.getResource("log-in.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("BSK");
        stage.setScene(loginScene);
        stage.show();
    }

    public void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        stg.getScene().setRoot(pane);
        stg.sizeToScene();
        stg.centerOnScreen();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        if(!Files.exists(new File(Constants.PASSWORD_SHA_DIR).toPath())){
            String password = Constants.PASSWORD;
            byte[] sha = EncryptionUtils.createSha256(password);
            File file = new File(Constants.PASSWORD_SHA_DIR);
            Files.write(file.toPath(), sha);
        }

        launch();
    }
}