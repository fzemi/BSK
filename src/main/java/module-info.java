module pl.projekt.bsk {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;


    opens pl.projekt.bsk to javafx.fxml;
    exports pl.projekt.bsk;
}