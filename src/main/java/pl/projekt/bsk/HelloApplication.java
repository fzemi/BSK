package pl.projekt.bsk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

public class HelloApplication extends Application {

    private static Stage stg;

    @Override
    public void start(Stage stage) throws IOException {
        stg = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("log-in.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("BSK");
        stage.setScene(loginScene);
        stage.show();

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        System.out.println(Arrays.toString(iv));
    }

    public void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        stg.getScene().setRoot(pane);
        stg.sizeToScene();
    }

    public static void main(String[] args) {
        launch();
    }
}