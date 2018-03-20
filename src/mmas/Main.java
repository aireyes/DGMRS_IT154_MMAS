package mmas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mmas.fxml"));
        primaryStage.setTitle("MMAS");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("test");
            try {
                System.exit(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
