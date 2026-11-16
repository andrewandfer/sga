package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ApartaSuites;
import model.enums.RolUsuario;
import model.excepciones.ExcepcionSGA;
import model.modelo.Empleado;

public class App extends Application {
    ApartaSuites apartaSuites = ApartaSuites.getInstance();
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/views/registroHuesped.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Aparta-Suites UQ - Iniciar sesión");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) throws ExcepcionSGA {
        ApartaSuites apartaSuites = ApartaSuites.getInstance();
        Empleado empleado = new Empleado("1","j", RolUsuario.ADMINISTRADOR,"2","1");
        apartaSuites.registrarEmpleado(empleado);
        launch(args);
    }
}
