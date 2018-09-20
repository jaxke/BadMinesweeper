package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        AnchorPane root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        primaryStage.setTitle("BMS");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        // Would otherwise focus the width textfield
        root.requestFocus();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
