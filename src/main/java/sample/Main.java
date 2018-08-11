package sample;

import com.jconf.jconf.Jconf;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.util.HashMap;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Jconf c = new Jconf("settings/settings");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
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
