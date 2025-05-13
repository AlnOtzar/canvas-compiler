package mx.unam.aragon.interprete;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Compilador extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Compilador.class.getResource("Vista.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("KinderFES");
        stage.setScene(scene);
        ((CompiladorController)fxmlLoader.getController()).setEscena(scene);
        //((CompiladorController)fxmlLoader.getController()).iniciar();
        stage.show();
        stage.requestFocus();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}