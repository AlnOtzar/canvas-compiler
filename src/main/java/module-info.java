module mx.unam.aragon.interprete {
    requires javafx.controls;
    requires javafx.fxml;


    opens mx.unam.aragon.interprete to javafx.fxml;
    exports mx.unam.aragon.interprete;
}