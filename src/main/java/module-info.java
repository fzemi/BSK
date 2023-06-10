module pl.projekt.bsk {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires com.fasterxml.jackson.databind;


    opens pl.projekt.bsk to javafx.fxml;
    exports pl.projekt.bsk;
    exports pl.projekt.bsk.connection;
}