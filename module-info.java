module unam.aragon.mx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.cup.runtime;
    requires jdk.jdi;

    exports unam.aragon.mx;          // para que otros módulos puedan ver las clases públicas
    opens unam.aragon.mx to javafx.fxml;  // para permitir reflexión a JavaFX (FXMLLoader)
}
