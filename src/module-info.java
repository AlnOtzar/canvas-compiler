module unam.aragon.mx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.cup.runtime;
    requires jdk.jdi;

    exports unam.aragon.mx;
    opens unam.aragon.mx to javafx.fxml;
}
