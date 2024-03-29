package pl.projekt.bsk;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.projekt.bsk.connection.Receiver;
import pl.projekt.bsk.connection.Sender;

import java.io.File;


public class CipherChatController {

    @FXML
    private TextArea textToSend;
    @FXML
    private TextField targetIPInput;
    @FXML
    private TextField serverPortField;
    @FXML
    private TextField filePath;
    @FXML
    private TextField directoryPath;
    @FXML
    private Button runButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button sendButton;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Label runStatusLabel;
    @FXML
    private Circle connectionStatus;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private RadioButton rButtonCBC;

    private int serverPort;
    private String cipherMode = "AES/CBC/PKCS5Padding";
    private Runnable senderRunnable;
    private Runnable receiverRunnable;
    private File selectedFile;
    private File receivedFileDirectory = new File(Constants.DEFAULT_FILES_DIR);

    @FXML
    public void onRunButtonClick() {
        if(serverPortField.getText().equals("")
                || !serverPortField.getText().matches("^[0-9]{1,5}$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Connection error");
            alert.setContentText("Please enter port");
            alert.showAndWait();
        }else {
            serverPort = Integer.parseInt(serverPortField.getText());
            receiverRunnable = new Receiver(serverPort, receivedFileDirectory, progressBar, textToSend);
            Thread receiverThread = new Thread(receiverRunnable);
            receiverThread.setDaemon(true);
            receiverThread.start();

            runButton.setDisable(true);
            runStatusLabel.setText("Listening on port: " + serverPort);
            runStatusLabel.setDisable(false);

            directoryPath.setText(receivedFileDirectory.toString());
        }
    }

    @FXML
    public void onConnectButtonClick(){
        if(targetIPInput.getText().equals("")
                || !targetIPInput.getText().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Connection error");
            alert.setContentText("Please enter IP and port");
            alert.showAndWait();
        } else {
            String targetIP = targetIPInput.getText().split(":")[0];
            int targetPort = Integer.parseInt(targetIPInput.getText().split(":")[1]);

            //connect to other device
            senderRunnable = new Sender(targetIP, targetPort);
            Thread senderThread = new Thread(senderRunnable);
            senderThread.setDaemon(true);
            senderThread.start();

            // wait for connection to be established without blocking UI
            new Thread(() -> {
                while(!((Sender)senderRunnable).isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //if connection was successful

                connectionStatus.setFill(Color.GREEN);
                connectButton.setDisable(true);
                sendButton.setDisable(false);

            }).start();
        }
    }

    @FXML
    public void onChooseFileButtonClick(ActionEvent e) {

        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog((Stage)((Node)e.getSource()).getScene().getWindow());
        if(selectedFile != null){
            filePath.setText(selectedFile.toString());
        }
    }

    @FXML
    public void onChooseDirectoryClick(ActionEvent e){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        receivedFileDirectory = directoryChooser.showDialog((Stage)((Node)e.getSource()).getScene().getWindow());
        if (receivedFileDirectory != null) {
            directoryPath.setText(receivedFileDirectory.toString());
            ((Receiver)receiverRunnable).setReceivedFileDirectory(receivedFileDirectory.getAbsolutePath() + "\\");
        }
    }

    @FXML
    public void onSendButtonClick() {
        if((selectedFile == null || !selectedFile.exists()) && textToSend.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Sending error");
            alert.setContentText("Please choose file or type text to send");
            alert.showAndWait();
        }else {
            if(!textToSend.getText().isEmpty())
                ((Sender)senderRunnable).sendText(textToSend.getText(), cipherMode);
            if (selectedFile != null && selectedFile.exists())
                ((Sender)senderRunnable).sendFile(selectedFile, cipherMode);
        }

    }

    @FXML
    public void setCipherMode(ActionEvent e) {
        if(rButtonCBC.isSelected())
            cipherMode = "AES/CBC/PKCS5Padding";
        else
            cipherMode = "AES/ECB/PKCS5Padding";
    }
}